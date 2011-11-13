/*
Copyright (c) 2007-2009, Dennis M. Sosnoski.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jibx.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import junit.framework.TestCase;

import org.jibx.binding.Loader;
import org.jibx.binding.Utility;
import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.MungedClass;
import org.jibx.binding.def.BindingDefinition;
import org.jibx.extras.DocumentComparator;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.codegen.custom.SchemasetCustom;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.validation.NameMergeVisitor;
import org.jibx.schema.validation.PrevalidationVisitor;
import org.jibx.schema.validation.NameRegistrationVisitor;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.schema.validation.ValidationProblem;
import org.jibx.schema.validation.ValidationVisitor;

/**
 * Base class for all schema tests. This binds and loads the schema classes during initialization, and provides access
 * methods for working with the schema classes.
 */
public class SchemaTestBase extends TestCase
{

    private static final String SCHEMA_CLASS = "org.jibx.schema.elements.SchemaElement";
    private static final String BINDING_FILE = "schema-xsprefix-binding.xml";
    private static final String BINDING_NAME = "schema_xsprefix_binding";
    private static final String EXTRACT_FILE = "schema-extract-binding.xml";
    private static final String EXTRACT_NAME = "schema_extract_binding";
    
    private static final IBindingFactory m_bindingFactory;
    static {
        ClassLoader loader;
        try {
            
            // set paths to be used for loading referenced classes
            loader = SchemaTestBase.class.getClassLoader();
            URL[] urls = Loader.getClassPaths();
            String[] paths = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                paths[i] = urls[i].getFile();
            }
            ClassCache.setPaths(paths);
            ClassFile.setPaths(paths);
            
        } catch (MalformedURLException e) {
            throw new RuntimeException("Internal error - unable to set classpaths");
        }
        IBindingFactory factory = null;
        try {
            
            // look up the mapped class and existing binding factory
            Class mclas = Class.forName(SCHEMA_CLASS);
            factory = BindingDirectory.getFactory(BINDING_NAME, mclas);
            
        } catch (Exception e) { /* just fall through */ }
        if (factory == null) {
            try {
                
                // set paths to be used for loading referenced classes
                URL[] urls = Loader.getClassPaths();
                String[] paths = new String[urls.length];
                for (int i = 0; i < urls.length; i++) {
                    paths[i] = urls[i].getFile();
                }
                ClassCache.setPaths(paths);
                ClassFile.setPaths(paths);
                
                // find the binding definitions
                InputStream is1 = loader.getResourceAsStream(BINDING_FILE);
                if (is1 == null) {
                    throw new RuntimeException("Schema binding definition not found");
                }
                InputStream is2 = loader.getResourceAsStream(EXTRACT_FILE);
                if (is2 == null) {
                    throw new RuntimeException("Schema extract binding definition not found");
                }
                
                // process the binding
                BoundClass.reset();
                MungedClass.reset();
                BindingDefinition.reset();
                BindingDefinition def1 = Utility.loadBinding(BINDING_FILE, BINDING_NAME, is1, null, true);
                BindingDefinition def2 = Utility.loadBinding(EXTRACT_FILE, EXTRACT_NAME, is2, null, true);
                def1.generateCode(false, false);
                def2.generateCode(false, false);
                
                // finish binding factory with information on classes used
                ClassFile[][] lists = MungedClass.fixDispositions();
                def1.addClassList(lists[0], lists[1]);
                def2.addClassList(lists[0], lists[1]);
                
                // output the modified class files
                MungedClass.writeChanges();
                
                // look up the mapped class and associated binding factory
                Class mclas = Class.forName(SCHEMA_CLASS);
                factory = BindingDirectory.getFactory(BINDING_NAME, mclas);
                
            } catch (JiBXException e) {
                throw new RuntimeException("JiBXException: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException("IOException: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("ClassNotFoundException: " + e.getMessage());
            }
        }
        m_bindingFactory = factory;
    }
    
    protected ValidationContext m_validationContext;
    
    protected NameRegister m_nameRegister;
    
    protected void setUp() throws Exception {
        m_validationContext = new ValidationContext();
    }
    
    /**
     * Read a schema definition into model.
     * 
     * @param is schema input stream
     * @param vctx validation context
     * @return schema element
     * @throws Exception
     */
    protected SchemaElement readSchema(InputStream is, ValidationContext vctx) throws Exception {
        final IUnmarshallingContext ictx = m_bindingFactory.createUnmarshallingContext();
        ictx.setDocument(is, null);
        ictx.setUserContext(vctx);
        SchemaElement schema = (SchemaElement)ictx.unmarshalElement();
        m_nameRegister = schema.getRegister();
        return schema;
    }
    
    /**
     * Read a schema definition into model.
     * 
     * @param text schema text
     * @param vctx validation context
     * @return schema element
     * @throws Exception
     */
    protected SchemaElement readSchema(String text, ValidationContext vctx) throws Exception {
        return readSchema(new ByteArrayInputStream(text.getBytes("utf-8")), vctx);
    }
    
    /**
     * Validate a schema definition.
     * 
     * @param schema schema element
     * @param vctx validation context
     */
    protected void validateSchema(SchemaElement schema, ValidationContext vctx) {
        TreeWalker tctx = new TreeWalker(vctx, vctx);
        tctx.walkSchema(schema, new PrevalidationVisitor(vctx));
        vctx.clearTraversed();
        tctx.walkSchema(schema, new NameRegistrationVisitor(vctx));
        vctx.clearTraversed();
        tctx.walkSchema(schema, new NameMergeVisitor(vctx));
        vctx.clearTraversed();
        tctx.walkSchema(schema, new ValidationVisitor(vctx));
    }
    
    /**
     * Check for validation problem.
     * 
     * @param vctx
     * @return <code>true</code> if any problem
     */
    protected boolean hasProblem(ValidationContext vctx) {
        return vctx.getProblems().size() > 0;
    }
    
    /**
     * Get validation problem report.
     * 
     * @param vctx
     * @return problem text
     */
    protected String getProblemText(ValidationContext vctx) {
        StringWriter writer = new StringWriter();
        writer.append("Problems found in schema definition");
        ArrayList problems = vctx.getProblems();
        for (int i = 0; i < problems.size(); i++) {
            writer.append('\n');
            ValidationProblem prob = (ValidationProblem)problems.get(i);
            writer.append(prob.getDescription());
        }
        return writer.toString();
    }
    
    /**
     * Write schema to text string.
     * 
     * @param schema schema element
     * @return output schema text
     * @throws Exception
     */
    protected String writeSchema(SchemaElement schema) throws Exception {
        StringWriter writer = new StringWriter();
        IMarshallingContext ictx = m_bindingFactory.createMarshallingContext();
        ictx.setOutput(writer);
        ictx.setIndent(2);
        ictx.marshalDocument(schema);
        return writer.toString();
    }
    
    /**
     * Verify that output schema matches original input. Fails the test if there's any difference between the two
     * versions of the schema.
     * 
     * @param text1 original schema text
     * @param text2 output schema text
     * @throws Exception
     */
    protected void verifySchema(String text1, String text2) throws Exception {
        StringReader rdr1 = new StringReader(text1);
        StringReader rdr2 = new StringReader(text2);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream pstr = new PrintStream(bos);
        DocumentComparator comp = new DocumentComparator(pstr);
        boolean match = comp.compare(rdr1, rdr2);
        if (!match) {
            pstr.close();
            fail("Error roundtripping schema:\n" + new String(bos.toByteArray()) + "\nOriginal schema:\n" + text1
                + "\nOutput schema:\n" + text2);
        }
    }
    
    /**
     * Read and validate schema, with one or more validation errors expected.
     * 
     * @param text schema text
     * @param count number of errors expected
     * @param msg text for wrong number of errors found
     * @throws Exception on unexpected error
     */
    protected void runErrors(String text, int count, String msg) throws Exception {
        SchemaElement schema = readSchema(text, m_validationContext);
        validateSchema(schema, m_validationContext);
        assertEquals(msg, count, m_validationContext.getProblems().size());
    }
    
    /**
     * Read and validate schema, with a particular validation error expected.
     * 
     * @param text schema text
     * @param msg text for wrong number of errors found
     * @throws Exception on unexpected error
     */
    protected void runOneError(String text, String msg) throws Exception {
        SchemaElement schema = readSchema(text, m_validationContext);
        validateSchema(schema, m_validationContext);
        assertTrue(msg, hasProblem(m_validationContext));
    }
    
    /**
     * Prepare schema model for use.
     * 
     * @param text schema text
     * @return schema
     * @throws Exception on unexpected error
     */
    protected SchemaElement prepareSchema(String text) throws Exception {
        m_validationContext.reset();
        SchemaElement schema = readSchema(text, m_validationContext);
        validateSchema(schema, m_validationContext);
        assertFalse(getProblemText(m_validationContext), hasProblem(m_validationContext));
        return schema;
    }
    
    /**
     * Run schema round-trip with validation, with no validation errors.
     * 
     * @param text schema text
     * @return schema
     * @throws Exception on unexpected error
     */
    protected SchemaElement runNoErrors(String text) throws Exception {
        SchemaElement schema = prepareSchema(text);
        verifySchema(text, writeSchema(schema));
        return schema;
    }

    /**
     * Load and validate schema directly. This takes the root schema resolver as input, and loads the schema along with
     * all associated schemas. Only the main schema document is actually validated, though.
     *
     * @param resolve resolver for all schema texts
     * @return root schema
     * @throws Exception
     */
    protected SchemaElement loadSchema(TestResolver resolve) throws Exception {
        SchemaElement schema = readSchema(resolve.getContent(), m_validationContext);
        schema.setResolver(resolve);
        m_validationContext.setSchema(resolve.getName(), schema);
        validateSchema(schema, m_validationContext);
        assertFalse(getProblemText(m_validationContext), hasProblem(m_validationContext));
        verifySchema(resolve.getText(), writeSchema(schema));
        return schema;
    }
    
    /**
     * Load a code generation customization.
     *
     * @param text customizations document text
     * @return loaded customization
     * @throws Exception
     */
    protected SchemasetCustom loadCustomization(String text) throws Exception {
        SchemasetCustom custom = new SchemasetCustom((SchemasetCustom)null);
        IBindingFactory fact = BindingDirectory.getFactory("xsdcodegen_customs_binding",
            "org.jibx.schema.codegen.custom");
        IUnmarshallingContext ictx = fact.createUnmarshallingContext();
        ictx.setDocument(new StringReader(text));
        ictx.setUserContext(m_validationContext);
        ((IUnmarshallable)custom).unmarshal(ictx);
        return custom;
    }

    /**
     * Resolver used for handling schema text strings directly.
     */
    protected static class TestResolver implements ISchemaResolver
    {
        /** Schema document. */
        private final String m_document;
        
        /** Schema name. */
        private final String m_name;
        
        /** Map from schema name to resolver. */
        private final Map m_schemaMap;
        
        public TestResolver(String document, String name, Map map) {
            m_document = document;
            m_name = name;
            m_schemaMap = map;
        }

        public InputStream getContent() throws IOException {
            return new ByteArrayInputStream(m_document.getBytes("UTF-8"));
        }

        public String getId() {
            return m_name;
        }

        public String getName() {
            return m_name;
        }

        public ISchemaResolver resolve(String loc, String tns) throws IOException {
            return (ISchemaResolver)m_schemaMap.get(loc);
        }
        
        public String getText() {
            return m_document;
        }
    }
}
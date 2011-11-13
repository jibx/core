/*
 * Copyright (c) 2009-2010, Dennis M. Sosnoski All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.schema.validation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jibx.extras.DocumentComparator;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.schema.ISchemaResolver;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.elements.IncludeElement;
import org.jibx.schema.elements.SchemaElement;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Utility methods for schema validation.
 * 
 * @author Dennis M. Sosnoski
 */
public class ValidationUtils
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(ValidationUtils.class.getName());
    
    /**
     * Validate an ordered array of schemas. To assure proper handling of chameleon schemas, schemas with target
     * namespaces need to precede schemas with no target namespace in the array.
     * 
     * @param schemas schemas to be validated
     * @param vctx validation context to be used
     */
    public static void validateSchemas(SchemaElement[] schemas, ValidationContext vctx) {
        
        // start by clearing name registers for previously-validated schemas
        for (Iterator iter = vctx.iterateSchemas(); iter.hasNext();) {
            ((SchemaElement)iter.next()).getRegister().reset();
        }
        
        // run full schema validation
        TreeWalker wlkr = new TreeWalker(vctx, vctx);
        s_logger.debug("Beginning schema prevalidation pass");
        vctx.clearTraversed();
        s_logger.debug("Beginning schema prevalidation pass");
        for (int i = 0; i < schemas.length; i++) {
            wlkr.walkSchema(schemas[i], new PrevalidationVisitor(vctx));
            s_logger.debug("After prevalidation schema " + schemas[i].getResolver().getName() +
                " has effective namespace " + schemas[i].getEffectiveNamespace());
        }
        s_logger.debug("Beginning name registration pass");
        vctx.clearTraversed();
        for (int i = 0; i < schemas.length; i++) {
            wlkr.walkSchema(schemas[i], new NameRegistrationVisitor(vctx));
        }
        s_logger.debug("Beginning name merge pass");
        vctx.clearTraversed();
        for (int i = 0; i < schemas.length; i++) {
            wlkr.walkSchema(schemas[i], new NameMergeVisitor(vctx));
        }
        s_logger.debug("Beginning validation pass");
        vctx.clearTraversed();
        for (int i = 0; i < schemas.length; i++) {
            wlkr.walkSchema(schemas[i], new ValidationVisitor(vctx));
            s_logger.debug("After validation schema " + schemas[i].getResolver().getName() +
                " has effective namespace " + schemas[i].getEffectiveNamespace());
        }
    }
    
    /**
     * Load and validate a list of schemas.
     * 
     * @param resolves resolvers for schemas to be loaded
     * @param uri effective namespace used for generation when no namespaced schemas are found (<code>null</code> if
     * none)
     * @param vctx context to use for validating schemas
     * @return schemas in validation order
     * @throws JiBXException on unrecoverable error in schemas
     * @throws IOException on error reading schemas
     */
    public static SchemaElement[] load(Collection resolves, String uri, ValidationContext vctx)
    throws JiBXException, IOException {
        IBindingFactory factory = BindingDirectory.getFactory(SchemaUtils.XS_PREFIX_BINDING, SchemaElement.class);
        IUnmarshallingContext ictx = factory.createUnmarshallingContext();
        int count = resolves.size();
        int offset = 0;
        SchemaElement[] schemas = new SchemaElement[count];
        for (Iterator iter = resolves.iterator(); iter.hasNext();) {
            
            // unmarshal document to construct schema structure
            ISchemaResolver resolver = (ISchemaResolver)iter.next();
            ictx.setDocument(resolver.getContent(), resolver.getName(), null);
            ictx.setUserContext(vctx);
            SchemaElement schema = new SchemaElement();
            ((IUnmarshallable)schema).unmarshal(ictx);
            
            // set resolver for use during schema processing
            schemas[offset++] = schema;
            schema.setResolver(resolver);
            String id = resolver.getId();
            vctx.setSchema(id, schema);
            
            // verify schema roundtripping if debug enabled
            if (s_logger.isDebugEnabled()) {
                try {
                    
                    // determine encoding of input document
                    String enc = ((UnmarshallingContext)ictx).getInputEncoding();
                    if (enc == null) {
                        enc = "UTF-8";
                    }
                    
                    // marshal root object back out to document in memory
                    IMarshallingContext mctx = factory.createMarshallingContext();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mctx.setIndent(2);
                    mctx.marshalDocument(schema, "UTF-8", null, bos);
                    
                    // compare with original input document
                    InputStreamReader brdr =
                        new InputStreamReader(new ByteArrayInputStream(bos.toByteArray()), "UTF-8");
                    InputStreamReader frdr = new InputStreamReader(resolver.getContent(), enc);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream pstream = new PrintStream(baos);
                    DocumentComparator comp = new DocumentComparator(pstream);
                    if (comp.compare(frdr, brdr)) {
                        
                        // report schema roundtripped successfully
                        s_logger.debug("Successfully roundtripped schema " + id);
                        
                    } else {
                        
                        // report problems in roundtripping schema
                        s_logger.debug("Errors in roundtripping schema " + id);
                        pstream.flush();
                        s_logger.debug(baos.toString());
                        
                    }
                    
                } catch (XmlPullParserException e) {
                    s_logger.debug("Error during schema roundtripping", e);
                }
            }
        }
        
        // to correctly handle namespaces for includes, process namespaced schemas first
        ArrayList ordereds = new ArrayList();
        vctx.clearTraversed();
        for (int i = 0; i < count; i++) {
            SchemaElement schema = schemas[i];
            if (schema.getTargetNamespace() != null) {
                ordereds.add(schema);
            }
        }
        
        // check for no namespaced schemas, but effective namespace supplied
        if (ordereds.size() == 0 && uri != null) {
            
            // generate a schema to include all the no-namespace schemas into namespace
            SchemaElement schema = new SchemaElement();
            schema.setTargetNamespace(uri);
            SyntheticSchemaResolver resolver = new SyntheticSchemaResolver();
            for (int i = 0; i < count; i++) {
                SchemaElement inclschema = schemas[i];
                inclschema.setEffectiveNamespace(uri);
                IncludeElement include = new IncludeElement();
                ISchemaResolver inclresolver = inclschema.getResolver();
                include.setLocation(inclresolver.getId());
                resolver.addResolver(inclresolver);
                schema.getSchemaChildren().add(include);
            }
            schema.setResolver(resolver);
            ordereds.add(schema);
        }
        
        // add no-namespace schemas to list
        for (int i = 0; i < count; i++) {
            SchemaElement schema = schemas[i];
            if (schema.getTargetNamespace() == null) {
                ordereds.add(schema);
            }
        }
        
        // validate the schemas in order
        SchemaElement[] ordschemas = (SchemaElement[])ordereds.toArray(new SchemaElement[ordereds.size()]);
        validateSchemas(ordschemas, vctx);
        return ordschemas;
    }
    
    /**
     * Resolver for synthesized schema, used when no-namespace schemas are being generated within a namespace.
     */
    private static class SyntheticSchemaResolver implements ISchemaResolver
    {
        private final Map m_locResolver = new HashMap();
        
        public void addResolver(ISchemaResolver resolver) {
            m_locResolver.put(resolver.getId(), resolver);
        }
        
        public InputStream getContent() throws IOException {
            throw new IOException("Source not available");
        }
        
        public String getName() {
            return "synthetic";
        }
        
        public String getId() {
            return "synthetic";
        }
        
        public ISchemaResolver resolve(String loc, String tns) throws IOException {
            return (ISchemaResolver)m_locResolver.get(loc);
        }
    }
}
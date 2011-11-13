/*
 * Copyright (c) 2008-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jibx.binding.model.BindingOrganizer;
import org.jibx.extras.DocumentComparator;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.schema.INamed;
import org.jibx.schema.ISchemaResolver;
import org.jibx.schema.SchemaContextTracker;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.SchemaVisitor;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.UrlResolver;
import org.jibx.schema.codegen.custom.ComponentExtension;
import org.jibx.schema.codegen.custom.GlobalExtension;
import org.jibx.schema.codegen.custom.SchemaCustom;
import org.jibx.schema.codegen.custom.SchemasetCustom;
import org.jibx.schema.elements.FilteredSegmentList;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SchemaLocationBase;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.UnionElement;
import org.jibx.schema.support.SchemaTypes;
import org.jibx.schema.validation.NameMergeVisitor;
import org.jibx.schema.validation.PrevalidationVisitor;
import org.jibx.schema.validation.NameRegistrationVisitor;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.schema.validation.ValidationProblem;
import org.jibx.schema.validation.ValidationVisitor;
import org.jibx.util.InsertionOrderedSet;
import org.jibx.util.LazyList;
import org.jibx.util.NameUtilities;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Schema refactoring tool. Although many of the methods in this class use <code>public</code> access, they are intended
 * for use only by the JiBX developers and may change from one release to the next. To make use of this class from your
 * own code, call the {@link #main(String[])} method with an appropriate argument list.
 * 
 * @author Dennis M. Sosnoski
 */
public class Refactory
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(Refactory.class.getName());
    
    /** Default type replacements applied. */
    private static final QName[] DEFAULT_REPLACEMENTS =
        new QName[] {
            SchemaTypes.ANY_URI.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.DURATION.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.ENTITIES.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.ENTITY.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.GDAY.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.GMONTH.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.GMONTHDAY.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.GYEAR.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.GYEARMONTH.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.ID.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.IDREF.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.IDREFS.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.LANGUAGE.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.NAME.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.NEGATIVE_INTEGER.getQName(), SchemaTypes.INTEGER.getQName(),
            SchemaTypes.NON_NEGATIVE_INTEGER.getQName(), SchemaTypes.INTEGER.getQName(),
            SchemaTypes.NON_POSITIVE_INTEGER.getQName(), SchemaTypes.INTEGER.getQName(),
            SchemaTypes.NORMALIZED_STRING.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.NCNAME.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.NMTOKEN.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.NMTOKENS.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.NOTATION.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.POSITIVE_INTEGER.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.TOKEN.getQName(), SchemaTypes.STRING.getQName(),
            SchemaTypes.UNSIGNED_BYTE.getQName(), SchemaTypes.BYTE.getQName(),
            SchemaTypes.UNSIGNED_INT.getQName(), SchemaTypes.INT.getQName(),
            SchemaTypes.UNSIGNED_LONG.getQName(), SchemaTypes.LONG.getQName(),
            SchemaTypes.UNSIGNED_SHORT.getQName(), SchemaTypes.SHORT.getQName()
            };
    
    /** Mask for schema elements which derive from a type. */
    private static final long TYPE_DERIVE_MASK =
        SchemaBase.ELEMENT_MASKS[SchemaBase.EXTENSION_TYPE] | SchemaBase.ELEMENT_MASKS[SchemaBase.RESTRICTION_TYPE];
    
    /** Mask for schema elements which define a type. */
    private static final long TYPE_DEFINE_MASK =
        SchemaBase.ELEMENT_MASKS[SchemaBase.COMPLEXTYPE_TYPE] | SchemaBase.ELEMENT_MASKS[SchemaBase.SIMPLETYPE_TYPE];
    
    /** Mask for schema elements which block name inheritance downward. */
    private static final long BLOCK_NAME_INHERIT_MASK =
        TYPE_DERIVE_MASK | SchemaBase.ELEMENT_MASKS[SchemaBase.UNION_TYPE];
    
    /** Code generation customizations. */
    private final SchemasetCustom m_global;
    
    /** Root URL for schemas. */
    private final URL m_schemaRoot;
    
    /** Root directory for schemas. */
    private final File m_schemaDir;
    
    /** Target directory for code generation. */
    private final File m_targetDir;
    
    /** Context for loading and processing schemas. */
    private final ValidationContext m_validationContext;
    
    /** Definitions to be generated (may be global schema definitions, or reused nested components with classes). */
    private ArrayList m_definitions;
    
    /** Directory for constructed bindings. */
    private BindingOrganizer m_bindingDirectory;
    
    /**
     * Constructor.
     * 
     * @param parms command line parameters
     */
    public Refactory(RefactoryCommandLine parms) {
        m_global = parms.getCustomRoot();
        m_global.setSubstitutions(DEFAULT_REPLACEMENTS);
        m_schemaRoot = parms.getSchemaRoot();
        m_schemaDir = parms.getSchemaDir();
        m_targetDir = parms.getGeneratePath();
        m_validationContext = new ValidationContext();
    }
    
    /**
     * Constructor used by tests. This uses supplied schemas and skips writing to the file system.
     * 
     * @param custom
     * @param vctx
     */
    public Refactory(SchemasetCustom custom, ValidationContext vctx) {
        m_global = custom;
        m_global.setSubstitutions(DEFAULT_REPLACEMENTS);
        m_schemaRoot = null;
        m_schemaDir = null;
        m_targetDir = null;
        m_validationContext = vctx;
    }
    
    /**
     * Find the most specific schemaset owning a schema. If multiple matches are found which are not in line of
     * containment the first match is returned and the conflict is reported as an error.
     * 
     * @param schema
     * @param custom schema set customization
     * @return owning schemaset, <code>null</code> if none
     */
    private SchemasetCustom findSchemaset(SchemaElement schema, SchemasetCustom custom) {
        LazyList childs = custom.getChildren();
        SchemasetCustom owner = null;
        String name = schema.getResolver().getName();
        for (int i = 0; i < childs.size(); i++) {
            Object child = childs.get(i);
            if (child instanceof SchemasetCustom) {
                SchemasetCustom schemaset = (SchemasetCustom)child;
                if (schemaset.isInSet(name, schema)) {
                    SchemasetCustom match = findSchemaset(schema, schemaset);
                    if (match != null) {
                        if (owner == null) {
                            owner = match;
                        } else {
                            m_validationContext.addError("schema-set overlap on schema " + name + " (first match "
                                + ValidationProblem.componentDescription(owner) + ')', match);
                        }
                    }
                }
            }
        }
        return owner == null ? custom : owner;
    }
    
    /**
     * Validate the schemas.
     * 
     * @param schemas schemas to be validated
     */
    public void validateSchemas(SchemaElement[] schemas) {
        
        // validate the schemas and report any problems
        TreeWalker wlkr = new TreeWalker(m_validationContext, m_validationContext);
        s_logger.debug("Beginning schema prevalidation pass");
        m_validationContext.clearTraversed();
        s_logger.debug("Beginning schema prevalidation pass");
        for (int i = 0; i < schemas.length; i++) {
            wlkr.walkSchema(schemas[i], new PrevalidationVisitor(m_validationContext));
            s_logger.debug("After prevalidation schema " + schemas[i].getResolver().getName() +
                " has effective namespace " + schemas[i].getEffectiveNamespace());
        }
        s_logger.debug("Beginning name registration pass");
        m_validationContext.clearTraversed();
        for (int i = 0; i < schemas.length; i++) {
            wlkr.walkSchema(schemas[i], new NameRegistrationVisitor(m_validationContext));
        }
        s_logger.debug("Beginning name merge pass");
        m_validationContext.clearTraversed();
        for (int i = 0; i < schemas.length; i++) {
            wlkr.walkSchema(schemas[i], new NameMergeVisitor(m_validationContext));
        }
        s_logger.debug("Beginning validation pass");
        m_validationContext.clearTraversed();
        for (int i = 0; i < schemas.length; i++) {
            wlkr.walkSchema(schemas[i], new ValidationVisitor(m_validationContext));
            s_logger.debug("After validation schema " + schemas[i].getResolver().getName() +
                " has effective namespace " + schemas[i].getEffectiveNamespace());
        }
    }
    
    /**
     * Load and validate the root schema list.
     * 
     * @param list resolvers for schemas to be loaded
     * @return schemas in validation order
     * @throws JiBXException on unrecoverable error in schemas
     * @throws IOException on error reading schemas
     */
    private SchemaElement[] load(List list) throws JiBXException, IOException {
        IBindingFactory factory = BindingDirectory.getFactory(SchemaUtils.XS_PREFIX_BINDING, SchemaElement.class);
        IUnmarshallingContext ictx = factory.createUnmarshallingContext();
        int count = list.size();
        SchemaElement[] schemas = new SchemaElement[count];
        int fill = 0;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            
            // unmarshal document to construct schema structure
            UrlResolver resolver = (UrlResolver)iter.next();
            ictx.setDocument(resolver.getContent(), resolver.getName(), null);
            ictx.setUserContext(m_validationContext);
            Object obj = ictx.unmarshalElement();
            
            // set resolver for use during schema processing
            SchemaElement schema = (SchemaElement)obj;
            schemas[fill++] = schema;
            schema.setResolver(resolver);
            String id = resolver.getId();
            m_validationContext.setSchema(id, schema);
            
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
                    mctx.marshalDocument(obj, "UTF-8", null, bos);
                    
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
        Set schemaset = new HashSet(count);
        ArrayList ordereds = new ArrayList();
        m_validationContext.clearTraversed();
        for (int i = 0; i < count; i++) {
            SchemaElement schema = schemas[i];
            if (schema.getTargetNamespace() != null) {
                
                // add namespaced schema to both reached set and processing order list
                ordereds.add(schema);
                schemaset.add(schema);
                
                // add any child include and imports only to reached set
                FilteredSegmentList childs = schema.getSchemaChildren();
                for (int j = 0; j < childs.size(); j++) {
                    Object child = childs.get(j);
                    if (child instanceof SchemaLocationBase) {
                        schemaset.add(((SchemaLocationBase)child).getReferencedSchema());
                    }
                }
            }
        }
        
        // add any schemas not already covered
        for (int i = 0; i < count; i++) {
            SchemaElement schema = schemas[i];
            if (!schemaset.contains(schema)) {
                ordereds.add(schema);
            }
        }
        
        // validate the schemas in order
        SchemaElement[] ordschemas = (SchemaElement[])ordereds.toArray(new SchemaElement[ordereds.size()]);
        validateSchemas(ordschemas);
        return ordschemas;
    }
    
    /**
     * Validate and apply customizations to loaded schemas.
     * 
     * @return <code>true</code> if successful, <code>false</code> if error
     */
    public boolean customizeSchemas() {
        
        // TODO: remove this once union handling fully implemented
        SchemaVisitor visitor = new SchemaVisitor() {
            
            public void exit(UnionElement node) {
                OpenAttrBase parent = node.getParent();
                int count = parent.getChildCount();
                for (int i = 0; i < count; i++) {
                    if (parent.getChild(i) == node) {
                        SimpleRestrictionElement empty = new SimpleRestrictionElement();
                        empty.setBase(SchemaTypes.STRING.getQName());
                        parent.replaceChild(i, empty);
                        break;
                    }
                }
            }
            
        };
        TreeWalker wlkr = new TreeWalker(null, new SchemaContextTracker());
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            wlkr.walkElement((SchemaElement)iter.next(), visitor);
        }
        
        // validate the customizations
        m_global.validate(m_validationContext);
        
        // create the package directory, using default package from root schemaset
        String dfltpack = m_global.getPackage();
        if (dfltpack == null) {
            dfltpack = "";
        }
        
        // link each schema to a customization, creating a default customization if necessary
        int count = 0;
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            ISchemaResolver resolver = schema.getResolver();
            s_logger.debug("Assigning customization for schema " + ++count + ": " + resolver.getName());
            SchemasetCustom owner = findSchemaset(schema, m_global);
            SchemaCustom custom = owner.forceCustomization(resolver.getName(), resolver.getId(), schema,
                m_validationContext);
            custom.validate(m_validationContext);
            custom.extend(null, m_validationContext);
        }
        
        // check all the customizations
        m_global.checkSchemas(m_validationContext);
        return !reportProblems(m_validationContext);
    }
    
    /**
     * Process substitutions and deletions defined by extensions. This builds the cross-reference information for the
     * global definition components of the schemas while removing references to deleted components.
     * 
     * @return <code>true</code> if any changes to the schemas, <code>false</code> if not
     */
    private boolean processExtensions() {
        
        // first clear all the cross reference information
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            int count = schema.getChildCount();
            for (int i = 0; i < count; i++) {
                SchemaBase child = schema.getChild(i);
                Object obj = child.getExtension();
                if (obj instanceof GlobalExtension) {
                    ((GlobalExtension)obj).resetDependencies();
                }
            }
        }
        
        // process each loaded schema for deletions and cross referencing
        int index = 0;
        m_validationContext.clearTraversed();
        boolean modified = false;
        // Level level = TreeWalker.setLogging(s_logger.getLevel());
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            m_validationContext.enterSchema(schema);
            s_logger.debug("Applying extensions to schema " + ++index + ": " + schema.getResolver().getName());
            int count = schema.getChildCount();
            boolean instmod = false;
            for (int i = 0; i < count; i++) {
                SchemaBase child = schema.getChild(i);
                Object obj = child.getExtension();
                if (obj instanceof GlobalExtension) {
                    
                    // apply extension to global definition element
                    ComponentExtension exten = (ComponentExtension)obj;
                    if (exten.isRemoved()) {
                        
                        // just eliminate this definition from the schema
                        schema.detachChild(i);
                        instmod = true;
                        
                    } else {
                        
                        // process the definition to remove references to deleted components
                        exten.applyAndCountUsage(m_validationContext);
                        
                    }
                }
            }
            if (instmod) {
                schema.compactChildren();
                modified = true;
            }
            m_validationContext.exitSchema();
            
        }
        // TreeWalker.setLogging(level);
        return modified;
    }

    /**
     * Apply extensions and normalize all schemas. This may be a multipass process, since applying extensions may create
     * the opportunity for further normalizations and vice versa.
     */
    public void applyAndNormalize() {
        
        // loop until no modifications, with at least one pass of extensions and normalizations
        boolean modified = true;
        while (processExtensions() || modified) {
            
            // normalize all the schema definitions
            modified = false;
            for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
                SchemaElement schema = (SchemaElement)iter.next();
                int count = schema.getChildCount();
                boolean instmod = false;
                for (int i = 0; i < count; i++) {
                    SchemaBase child = schema.getChild(i);
                    Object obj = child.getExtension();
                    if (obj instanceof GlobalExtension) {
                        GlobalExtension global = (GlobalExtension)obj;
                        global.normalize();
                        if (global.isRemoved()) {
                            
                            // just eliminate this definition from the schema
                            schema.detachChild(i);
                            instmod = true;
                            
                        }
                    }
                }
                if (instmod) {
                    schema.compactChildren();
                    modified = true;
                }
            }
            
        }
        
        // finish by flagging global definitions requiring separate classes
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            SchemaCustom custom = findSchemaset(schema, m_global).getCustomization(schema.getResolver().getId());
            if (custom.isGenerateAll()) {
                int count = schema.getChildCount();
                for (int i = 0; i < count; i++) {
                    SchemaBase comp = schema.getChild(i);
                    if (comp.type() == SchemaBase.ELEMENT_TYPE || comp.type() == SchemaBase.COMPLEXTYPE_TYPE) {
                        Object obj = comp.getExtension();
                        if (obj instanceof GlobalExtension) {
                            ((GlobalExtension)obj).setIncluded(true);
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Set include for definition " + SchemaUtils.describeComponent(comp));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes the schemas to remove unused global definitions.
     */
    public void pruneDefinitions() {
        
        // start by recursively checking for removable global definitions
        int index = 0;
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            s_logger.debug("Checking for unused definitions in schema " + ++index + ": "
                + schema.getResolver().getName());
            int count = schema.getChildCount();
            for (int i = 0; i < count; i++) {
                SchemaBase child = schema.getChild(i);
                Object exten = child.getExtension();
                if (exten instanceof GlobalExtension) {
                    
                    // check if global definition is unused and not specifically required
                    ((GlobalExtension)exten).checkRemovable();
                    
                }
            }
        }
        
        // next remove all the definitions flagged in the first step
        index = 0;
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            s_logger.debug("Deleting unused definitions in schema " + ++index + ": " + schema.getResolver().getName());
            int count = schema.getChildCount();
            boolean modified = false;
            for (int i = 0; i < count; i++) {
                SchemaBase child = schema.getChild(i);
                Object exten = child.getExtension();
                if (exten instanceof GlobalExtension && ((ComponentExtension)exten).isRemoved()) {
                    
                    // remove the definition from schema
                    schema.detachChild(i);
                    modified = true;
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(" Removed definition " + ((INamed)child).getQName());
                    }
                    
                }
            }
            if (modified) {
                schema.compactChildren();
            }
        }
    }
    
    /**
     * Write schema definitions to file system.
     *
     * @param destdir destination directory
     * @throws JiBXException on error in marshalling
     * @throws IOException on error writing
     */
    private void writeSchemas(File destdir) throws JiBXException, IOException {
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            File file = new File(destdir, schema.getResolver().getName());
            OutputStream stream = new FileOutputStream(file);
            Writer writer = new OutputStreamWriter(stream, "utf-8");
            IBindingFactory factory = BindingDirectory.getFactory(SchemaUtils.XS_PREFIX_BINDING, SchemaElement.class);
            IMarshallingContext ictx = factory.createMarshallingContext();
            ictx.setOutput(writer);
            ictx.setIndent(2);
            ictx.marshalDocument(schema);
            writer.close();
        }
    }

    /**
     * Report problems using console output. This clears the problem list after they've been reported, to avoid multiple
     * reports of the same problems.
     * 
     * @param vctx
     * @return <code>true</code> if one or more errors, <code>false</code> if not
     */
    private static boolean reportProblems(ValidationContext vctx) {
        ArrayList probs = vctx.getProblems();
        boolean error = false;
        if (probs.size() > 0) {
            for (int j = 0; j < probs.size(); j++) {
                ValidationProblem prob = (ValidationProblem)probs.get(j);
                String text;
                if (prob.getSeverity() >= ValidationProblem.ERROR_LEVEL) {
                    error = true;
                    text = "Error: " + prob.getDescription();
                    s_logger.error(text);
                } else {
                    text = "Warning: " + prob.getDescription();
                    s_logger.info(text);
                }
                System.out.println(text);
            }
        }
        probs.clear();
        return error;
    }
    
    /**
     * Add the schemas specified by customizations to the set to be loaded.
     * 
     * @param base root URL for schemas
     * @param dir root directory for schemas
     * @param custom schema set customization
     * @param fileset set of schema files to be loaded
     * @throws MalformedURLException
     */
    private static void addCustomizedSchemas(URL base, File dir, SchemasetCustom custom, InsertionOrderedSet fileset)
        throws MalformedURLException {
        
        // first check for name match patterns supplied
        String[] names = custom.getNames();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                SchemaNameFilter filter = new SchemaNameFilter();
                String name = names[i];
                filter.setPattern(name);
                s_logger.debug("Matching file names to schemaset pattern '" + name + '\'');
                String[] matches = dir.list(filter);
                for (int j = 0; j < matches.length; j++) {
                    String match = matches[j];
                    fileset.add(new UrlResolver(match, new URL(base, match)));
                    s_logger.debug("Added schema from schemaset pattern match: " + match);
                }
            }
        }
        
        // next check all child customizations
        LazyList childs = custom.getChildren();
        for (int i = 0; i < childs.size(); i++) {
            Object child = childs.get(i);
            if (child instanceof SchemaCustom) {
                String name = ((SchemaCustom)child).getName();
                if (name != null) {
                    try {
                        fileset.add(new UrlResolver(name, new URL(base, name)));
                        s_logger.debug("Added schema from customizations: " + name);
                    } catch (MalformedURLException e) {
                        System.out.println("Error adding schema from customizations: " + name);
                    }
                }
            } else if (child instanceof SchemasetCustom) {
                addCustomizedSchemas(base, dir, (SchemasetCustom)child, fileset);
            }
        }
    }
    
    /**
     * Run the schema refactoring using command line parameters.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        TreeWalker.setLogging(Level.ERROR);
        RefactoryCommandLine parms = new RefactoryCommandLine();
        if (args.length > 0 && parms.processArgs(args)) {
            
            // build set of schemas specified on command line (including via wildcards)
            InsertionOrderedSet fileset = new InsertionOrderedSet();
            URL base = parms.getSchemaRoot();
            File basedir = parms.getSchemaDir();
            SchemaNameFilter filter = new SchemaNameFilter();
            boolean err = false;
            for (Iterator iter = parms.getExtraArgs().iterator(); iter.hasNext();) {
                String name = (String)iter.next();
                if (name.indexOf('*') >= 0) {
                    if (basedir == null) {
                        System.err.println("File name pattern argument not allowed for non-file base: '" + name + '\'');
                    } else {
                        filter.setPattern(name);
                        s_logger.debug("Matching file names to command line pattern '" + name + '\'');
                        String[] matches = basedir.list(filter);
                        for (int i = 0; i < matches.length; i++) {
                            String match = matches[i];
                            fileset.add(new UrlResolver(match, new URL(base, match)));
                        }
                    }
                } else {
                    if (basedir == null) {
                        URL url = new URL(base, name);
                        s_logger.debug("Adding schema URL from command line: " + url.toExternalForm());
                        fileset.add(new UrlResolver(name, url));
                    } else {
                        File sfile = new File(basedir, name);
                        if (sfile.exists()) {
                            s_logger.debug("Adding schema file from command line: " + sfile.getCanonicalPath());
                            fileset.add(new UrlResolver(name, new URL(base, name)));
                        } else {
                            System.err.println("Schema file from command line not found: " + sfile.getAbsolutePath());
                            err = true;
                        }
                    }
                }
            }
            if (!err) {
                
                // add any schemas specified in customizations
                addCustomizedSchemas(base, basedir, parms.getCustomRoot(), fileset);
                
                // load the full set of schemas
                Refactory inst = new Refactory(parms);
                SchemaElement[] schemas = inst.load(fileset.asList());
                if (!reportProblems(inst.m_validationContext)) {
                    if (inst.customizeSchemas()) {
                        System.out.println("Loaded and validated " + fileset.size() + " schemas");
                        
                        // apply the customizations to the schema
                        inst.applyAndNormalize();
                        inst.pruneDefinitions();
                        
                        // revalidate and write the schemas
                        inst.validateSchemas(schemas);
                        inst.writeSchemas(parms.getGeneratePath());
                        
                    }
                }
            }
            
        } else {
            if (args.length > 0) {
                System.err.println("Terminating due to command line errors");
            } else {
                parms.printUsage();
            }
            System.exit(1);
        }
    }

    /**
     * File name pattern matcher.
     */
    private static class SchemaNameFilter implements FilenameFilter
    {
        /** Current match pattern. */
        private String m_pattern;
        
        /**
         * Set the match pattern.
         * 
         * @param pattern
         */
        public void setPattern(String pattern) {
            m_pattern = pattern;
        }
        
        /**
         * Check for file name match.
         * 
         * @param dir
         * @param name
         * @return match flag
         */
        public boolean accept(File dir, String name) {
            boolean match = NameUtilities.isPatternMatch(name, m_pattern);
            if (match) {
                s_logger.debug(" matched file name '" + name + '\'');
            }
            return match;
        }
    }
}
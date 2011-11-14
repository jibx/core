/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski. All rights reserved.
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.AST;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.BindingHolder;
import org.jibx.binding.model.BindingOrganizer;
import org.jibx.binding.model.ElementBase;
import org.jibx.binding.model.FormatElement;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.NamespaceElement;
import org.jibx.binding.model.StructureElement;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.runtime.Utility;
import org.jibx.schema.INamed;
import org.jibx.schema.IReference;
import org.jibx.schema.ISchemaResolver;
import org.jibx.schema.SchemaContextTracker;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.SchemaVisitor;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.UrlResolver;
import org.jibx.schema.codegen.custom.ComponentExtension;
import org.jibx.schema.codegen.custom.GlobalExtension;
import org.jibx.schema.codegen.custom.SchemaCustom;
import org.jibx.schema.codegen.custom.SchemaExtension;
import org.jibx.schema.codegen.custom.SchemasetCustom;
import org.jibx.schema.codegen.extend.ClassDecorator;
import org.jibx.schema.codegen.extend.NameConverter;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupRefElement;
import org.jibx.schema.elements.CommonTypeDerivation;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.GroupRefElement;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.elements.UnionElement;
import org.jibx.schema.support.SchemaTypes;
import org.jibx.schema.validation.ProblemConsoleLister;
import org.jibx.schema.validation.ProblemHandler;
import org.jibx.schema.validation.ProblemLogLister;
import org.jibx.schema.validation.ProblemMultiHandler;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.schema.validation.ValidationProblem;
import org.jibx.schema.validation.ValidationUtils;
import org.jibx.util.ClasspathUrlExtender;
import org.jibx.util.DummyClassLocator;
import org.jibx.util.IClassLocator;
import org.jibx.util.InsertionOrderedSet;
import org.jibx.util.LazyList;
import org.jibx.util.ResourceMatcher;

/**
 * Code generator from schema definition. Although many of the methods in this class use <code>public</code> access,
 * they are intended for use only by the JiBX developers and may change from one release to the next. To make use of
 * this class from your own code, call the {@link #main(String[])} method with an appropriate argument list.
 * 
 * @author Dennis M. Sosnoski
 */
public class CodeGen
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(CodeGen.class.getName());
    
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
    
    /** Target directory for code generation. */
    private final File m_targetDir;
    
    /** Context for loading and processing schemas. */
    private final ValidationContext m_validationContext;
    
    /** Package directory for generated classes. */
    private PackageOrganizer m_packageDirectory;
    
    /** Directory for constructed bindings. */
    private BindingOrganizer m_bindingDirectory;
    
    /** Root binding definition holder (set by {@link #writeBindings(String, String, List, ProblemHandler)}). */
    private BindingHolder m_rootHolder;
    
    /**
     * Constructor.
     * 
     * @param global schema customization root element
     * @param root URL for base of schema paths
     * @param target destination directory for code generation
     */
    public CodeGen(SchemasetCustom global, URL root, File target) {
        m_global = global;
        addDefaultSubstitutions(m_global);
        m_targetDir = target;
        m_validationContext = new ValidationContext();
    }
    
    /**
     * Constructor used by tests. This uses supplied schemas and skips writing to the file system.
     * 
     * @param custom
     * @param vctx
     */
    public CodeGen(SchemasetCustom custom, ValidationContext vctx) {
        m_global = custom;
        addDefaultSubstitutions(custom);
        m_targetDir = null;
        m_validationContext = vctx;
    }
    
    /**
     * Get the validation context used for processing schemas.
     *
     * @return context
     */
    public ValidationContext getSchemaValidationContext() {
        return m_validationContext;
    }
    
    /**
     * Add default type substitutions to set currently defined.
     *
     * @param custom
     */
    private static void addDefaultSubstitutions(SchemasetCustom custom) {
        QName[] subs = custom.getSubstitutions();
        if (subs == null) {
            custom.setSubstitutions(DEFAULT_REPLACEMENTS);
        } else {
            QName[] newsubs = new QName[subs.length + DEFAULT_REPLACEMENTS.length];
            System.arraycopy(subs, 0, newsubs, 0, subs.length);
            System.arraycopy(DEFAULT_REPLACEMENTS, 0, newsubs, subs.length, DEFAULT_REPLACEMENTS.length);
            custom.setSubstitutions(newsubs);
        }
    }
    
    /**
     * Find the most specific schemaset owning a schema. If multiple matches are found which are not in line of
     * containment the first match is returned and the conflict is reported as an error.
     * 
     * @param schema
     * @param custom schema set customization
     * @return owning schemaset, <code>null</code> if none
     */
    public SchemasetCustom findSchemaset(SchemaElement schema, SchemasetCustom custom) {
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
     * Scan schemas to find the default prefixes used for namespaces.
     *
     * @param iter schema iterator
     */
    public void setDefaultPrefixes(Iterator iter) {
        while (iter.hasNext()) {
            SchemaElement schema = (SchemaElement)iter.next();
            ArrayList decls = schema.getNamespaceDeclarations();
            for (int i = 0; i < decls.size(); i++) {
                String prefix = (String)decls.get(i++);
                m_bindingDirectory.addDefaultPrefix((String)decls.get(i), prefix);
            }
        }
    }
    
    /**
     * Validate and apply customizations to loaded schemas.
     * 
     * @param pack package to be used by default for no-namespaced schemas (non-<code>null</code>)
     * @param handler validation problem handler
     * @return <code>true</code> if successful, <code>false</code> if error
     */
    public boolean customizeSchemas(String pack, ProblemHandler handler) {
        
        // TODO: remove this once list and union handling fully implemented
        SchemaVisitor visitor = new SchemaVisitor() {
            
            private void replaceSimpleType(OpenAttrBase node) {
                OpenAttrBase parent = node.getParent();
                int count = parent.getChildCount();
                for (int i = 0; i < count; i++) {
                    if (parent.getChild(i) == node) {
                        SimpleRestrictionElement empty = new SimpleRestrictionElement();
                        empty.setBase(SchemaTypes.STRING.getQName());
                        parent.replaceChild(i, empty);
                        return;
                    }
                }
            }
            
/*            public void exit(ListElement node) {
                replaceSimpleType(node);
            }   */
            
            public void exit(UnionElement node) {
                replaceSimpleType(node);
            }
            
        };
        TreeWalker wlkr = new TreeWalker(null, new SchemaContextTracker());
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            wlkr.walkElement((SchemaElement)iter.next(), visitor);
        }
        
//        // TODO: test code for annotating schema
//        try {
//            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//            final Document document = builder.newDocument();
//            visitor = new SchemaVisitor() {
//                
//                public void exit(AnnotatedBase node) {
//                    if (node.getAnnotation() == null) {
//                        StringBuffer buff = new StringBuffer();
//                        buff.append(node.name());
//                        if (node instanceof INamed && ((INamed)node).getName() != null) {
//                            buff.append(" name='");
//                            buff.append(((INamed)node).getName());
//                            buff.append('\'');
//                        } else if (node instanceof IReference && ((IReference)node).getRef() != null) {
//                            buff.append(" ref='");
//                            buff.append(((IReference)node).getRef().getName());
//                            buff.append('\'');
//                        }
//                        AnnotationElement anno = new AnnotationElement();
//                        node.setAnnotation(anno);
//                        DocumentationElement doc = new DocumentationElement();
//                        doc.addContent(document.createTextNode(buff.toString()));
//                        anno.getItemsList().add(doc);
//                    }
//                }
//                
//            };
//            for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
//                wlkr.walkElement((SchemaElement)iter.next(), visitor);
//            }
//        } catch (ParserConfigurationException e) {
//            /* ignore any error */
//        }
        
        // validate the customizations
        m_global.validate(m_validationContext);
        
        // create the package directory, using default package from root schemaset
        String dfltpack = m_global.getPackage();
        if (dfltpack == null) {
            dfltpack = pack;
        }
        m_packageDirectory = new PackageOrganizer(m_targetDir, dfltpack);
        
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
            String pname = custom.getPackage();
            PackageHolder holder = null;
            if (pname == null) {
                String uri = schema.getEffectiveNamespace();
                if (uri == null) {
                    uri = "";
                }
                holder = m_packageDirectory.getPackageForUri(uri);
            } else {
                holder = m_packageDirectory.getPackage(pname);
            }
            custom.extend(holder, m_validationContext);
        }
        
        // check all the customizations
        m_global.checkSchemas(m_validationContext);
        return !m_validationContext.reportProblems(handler);
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
                        if (global.isRemoved() ||
                            (!global.isIncluded() && global.isPreferInline() && global.getOverrideType() != null)) {
                            
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
        TreeWalker wlkr = new TreeWalker(null, new SchemaContextTracker());
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            SchemaCustom custom = ((SchemaExtension)schema.getExtension()).getCustom();
            if (custom.isGenerateAll()) {
                int count = schema.getChildCount();
                for (int i = 0; i < count; i++) {
                    
                    // check if this global needs a class
                    SchemaBase comp = schema.getChild(i);
                    boolean include = false;
                    switch (comp.type()) {
/*                        case SchemaBase.ATTRIBUTE_TYPE:
                            include = !custom.isAttributeInlined();
                            break;  */
                        
                        case SchemaBase.ATTRIBUTEGROUP_TYPE:
                        case SchemaBase.GROUP_TYPE:
                            
                            // for attribute group or group, only force separate class if multiple components
                            ValueCountVisitor visitor = new ValueCountVisitor();
                            wlkr.walkChildren(comp, visitor);
                            include = visitor.getCount() > 1;
                            break;
                            
                        case SchemaBase.COMPLEXTYPE_TYPE:
                        case SchemaBase.ELEMENT_TYPE:
                            include = true;
                            break;
                            
                        case SchemaBase.SIMPLETYPE_TYPE:
                            
                            // for simpleType definition, only need separate class if an enumeration
                            if (comp.getChildCount() > 0) {
                                SchemaBase deriv = ((SimpleTypeElement)comp).getDerivation();
                                include = deriv != null && deriv.type() == SchemaBase.RESTRICTION_TYPE &&
                                    ((SimpleRestrictionElement)deriv).getFacetsList().size() > 0;
                            }
                            break;
                            
                    }
                    if (include) {
                        
                        // flag separate class for global
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
     * Check if an item has an associated name. If the component associated with the item has a name, this just returns
     * that name converted to base name form. The only exception is for inlined global type definitions, which are
     * treated as unnamed.
     * 
     * @param item
     * @return name associated name, or <code>null</code> if none
     */
    private String checkDirectName(Item item) {
        AnnotatedBase comp = item.getSchemaComponent();
        if (comp instanceof INamed || comp instanceof IReference) {
            
            // check for an inlined global type definition
            boolean usename = true;
            if (comp.isGlobal()) {
                if ((comp.bit() & TYPE_DEFINE_MASK) != 0 && !(item instanceof DefinitionItem)) {
                    usename = false;
                }
            }
            if (usename) {
                
                // use name from schema component
                String name = null;
                if (comp instanceof INamed) {
                    name = ((INamed)comp).getName();
                }
                if (name == null && comp instanceof IReference) {
                    name = ((IReference)comp).getRef().getName();
                }
                if (name != null) {
                    NameConverter nconv = item.getComponentExtension().getGlobal().getNameConverter();
                    return nconv.toBaseName(name);
                }
            }
        }
        return null;
    }
    
    /**
     * Derive the base name for an item. If not forced, the only time a name will be returned is when the item is a
     * reference to a non-type definition. If forced, this will try other alternatives for names including the text
     * "Enumeration" for an enumeration group, the base type name for a type derivation, the schema type name for a
     * value of a schema type, or finally the schema component element name.
     * 
     * @param item
     * @param force name forced flag
     * @return name (<code>null</code> if to use inherited name when <code>force == false</code>)
     */
    private String deriveName(Item item, boolean force) {
        
        // try alternatives, in decreasing preference order
        AnnotatedBase comp = item.getSchemaComponent();
        String text = null;
        if (force) {
            
            if (item instanceof ReferenceItem) {
                text = ((ReferenceItem)item).getDefinition().getName();
            } else if (item instanceof GroupItem && ((GroupItem)item).isEnumeration()) {
                text = "Enumeration";
            } else if ((TYPE_DERIVE_MASK & comp.bit()) != 0 && ((CommonTypeDerivation)comp).getBase() != null) {
                text = ((CommonTypeDerivation)comp).getBase().getName();
            } else if (item instanceof ValueItem) {
                text = ((ValueItem)item).getSchemaType().getName();
            } else {
                text = comp.name();
            }
            
        } else if (item instanceof ReferenceItem && (TYPE_DEFINE_MASK & comp.type()) == 0) {
            
            // use name from definition in the case of anything except a type definition
            text = ((ReferenceItem)item).getDefinition().getName();
        }
        
        if (text == null) {
            return null;
        } else {
            return item.getComponentExtension().getGlobal().getNameConverter().toBaseName(text);
        }
    }
    
    /**
     * Compact group structures. This eliminates redundant groupings, in the form of groups with only one child, which
     * child is a group referencing the same schema component as the parent group, from the data structure
     * representation.
     *
     * @param group
     */
    private void compactGroups(GroupItem group) {
        Item child;
        while (group.getChildCount() == 1 && (child = group.getFirstChild()) instanceof GroupItem &&
            !child.isTopmost()) {
            group.adoptChildren((GroupItem)child);
        }
        for (child = group.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof GroupItem) {
                compactGroups((GroupItem)child);
            }
        }
    }
    
    /**
     * Assemble a name from a base name and an optional prefix. If the prefix is supplied, this first converts the base
     * name to embedded form, then appends the prefix.
     *
     * @param prefix
     * @param base
     * @return name
     */
    private String assembleName(String prefix, String base) {
        if (prefix == null) {
            return base;
        } else {
            return prefix + NameUtils.toNameWord(base);
        }
    }
    
    /**
     * Set the basic names to be used for a structure of items. For named components of the schema definition the names
     * used are simply the converted XML local names, for other components more complex rules apply (see {@link
     * #deriveName(Item,boolean)}. This method calls itself recursively to handle nested groups.
     * 
     * @param prefix text to be prefixed to names within inlined group (<code>null</code> if none)
     * @param group item group to be assigned
     * @param force force name derivation flag
     */
    private void assignNames(String prefix, GroupItem group, boolean force) {
        
        // use existing name if set, otherwise derive from context if necessary
        String name = group.getName();
        AnnotatedBase comp = group.getSchemaComponent();
        int type = comp.type();
        boolean propagate = group.getChildCount() == 1 &&
            (type == SchemaBase.ELEMENT_TYPE || type == SchemaBase.ATTRIBUTE_TYPE);
        if (name == null) {
            name = checkDirectName(group);
            if (name == null && force) {
                propagate = false;
                name = deriveName(group, true);
            }
        }
        
        // set name needed for this structure (as either value or class)
        if (name != null) {
            if (group.getClassName() == null) {
                NameConverter conv = group.getComponentExtension().getGlobal().getNameConverter();
                group.setClassName(conv.toJavaClassName(name));
            }
            if (group.getName() == null) {
                group.setName(NameUtils.toNameLead(assembleName(prefix, name)));
            }
        }
        
        // set passed-down prefix to this name if inline group with element name and more than one child, or multiple
        //  nested children and no passed-in prefix
        if (group.isInline() && name != null) {
            if (comp.type() == SchemaBase.ELEMENT_TYPE && SchemaUtils.isNamed(comp)) {
                boolean passprefix = false;
                if (group.getChildCount() > 1) {
                    passprefix = true;
                } else if (prefix == null) {
                    passprefix = true;
                    GroupItem childgroup = group;
                    while (childgroup.getChildCount() == 1) {
                        Item childchild = childgroup.getFirstChild();
                        if (childchild instanceof GroupItem) {
                            childgroup = (GroupItem)childchild;
                        } else {
                            passprefix = false;
                            break;
                        }
                    }
                }
                if (passprefix) {
                    prefix = assembleName(prefix, name);
                }
            }
        }
        
        // propagate name downward if group is inline and single nested item without its own name
        Item head = group.getFirstChild();
        if (propagate) {
            
            // name can be inherited, but continue recursion for child group
            if (head instanceof GroupItem) {
                GroupItem childgroup = (GroupItem)head;
                assignNames(childgroup.isInline() ? prefix : null, childgroup, false);
            }
            
        } else {
            
            // process all child items with definite name assignments
            for (Item item = head; item != null; item = item.getNext()) {
                if (item instanceof GroupItem) {
                    GroupItem childgroup = (GroupItem)item;
                    assignNames(childgroup.isInline() ? prefix : null, childgroup, true);
                } else {
                    if (item.getName() == null) {
                        String childname = checkDirectName(item);
                        if (childname == null) {
                            childname = deriveName(item, true);
                        }
                        item.setName(NameUtils.toNameLead(assembleName(prefix, childname)));
                    }
                }
            }
            
        }
    }
    
    /**
     * Compute the complexity of a structure. In order to find the complexity of a structure all items of the structure
     * must first be checked for inlining, which in turn requires checking their complexity. That makes this method
     * mutually recursive with {@link #checkInline(DefinitionItem, int, List)}.
     * 
     * @param group
     * @param depth nesting depth
     * @param defs list of generated definitions
     * @return complexity (0, 1, or 2 for anything more than a single value)
     */
    private int computeComplexity(GroupItem group, int depth, List defs) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(SchemaUtils.getIndentation(depth) + "counting values for "
                + (group instanceof DefinitionItem ? "definition " : "group ")
                + SchemaUtils.describeComponent(group.getSchemaComponent()));
        }
        
        // count the actual values in the structure
        int count = 0;
        for (Item item = group.getFirstChild(); item != null; item = item.getNext()) {
            
            // get the schema customization information
            SchemaCustom custom = ((SchemaExtension)item.getSchemaComponent().getSchema().getExtension()).getCustom();
            
            // handle inlining of references
            if (item instanceof ReferenceItem) {
                
                // first make sure the definition has been checked for inlining
                ReferenceItem reference = (ReferenceItem)item;
                DefinitionItem definition = reference.getDefinition();
                checkInline(definition, depth + 1, defs);
                if (definition.isInline()) {
                    
                    // convert the reference to an inline copy of the definition
                    item = reference.inlineReference();
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(SchemaUtils.getIndentation(depth) + "converted reference to "
                            + SchemaUtils.describeComponent(definition.getSchemaComponent()) + " to inline group");
                    }
                    
                }
            }
            
            // handle actual item count, and inlining of child group (may be new, from converted reference)
            if (item instanceof GroupItem) {
                
                // check count for nested group
                GroupItem grpitem = (GroupItem)item;
                int grpcount = computeComplexity(grpitem, depth + 1, defs);
                
                // avoid inlining if an enumeration, or an extension reference; or the nested group is optional or a
                //  collection and has more than one item (or a single item which is itself optional or a collection,
                //  which will be counted as multiple items); or the main group is a choice, and the nested group is a
                //  compositor with more than one element, and the first element is optional (or the first element child
                //  of that element, if inlined) - but allow override from customizations
                boolean inline = true;
                ComponentExtension exten = grpitem.getComponentExtension();
                if (exten.isSeparateClass()) {
                    inline = false;
                } else {
                    if (grpitem.isEnumeration()) {
                        inline = false;
                    } else if (grpitem.isCollection() || grpitem.isOptional()) {
                        if (grpcount > 1) {
                            inline = false;
                        } else {
                            
                            // must be single child, but block inlining if that child is optional or an element or
                            //  attribute with simple value (since we need an object for the optional part, separate
                            //  from the simple value)
                            Item child;
                            GroupItem childgrp = grpitem;
                            boolean named = false;
                            while ((child = childgrp.getFirstChild()) instanceof GroupItem) {
                                childgrp = (GroupItem)child;
                                if (childgrp.isOptional()) {
                                    inline = false;
                                    break;
                                }
                                if (!named && childgrp.isTopmost()) {
                                    int type = childgrp.getSchemaComponent().type();
                                    named = type == SchemaBase.ATTRIBUTE_TYPE || type == SchemaBase.ELEMENT_TYPE;
                                }
                            }
                            if (named && child instanceof ValueItem) {
                                inline = false;
                            }
                            
                        }
                    } else if (grpcount > 1 && group.getSchemaComponent().type() == SchemaBase.CHOICE_TYPE) {
                        
                        // assume no inlining, but dig into structure to make sure first non-inlined element is required
                        inline = false;
                        Item child = grpitem.getFirstChild();
                        while (!child.isOptional()) {
                            if (child.getSchemaComponent().type() == SchemaBase.ELEMENT_TYPE &&
                                !(child instanceof GroupItem && ((GroupItem)child).isInline())) {
                                
                                // required element with simple value or separate class, safe to inline
                                inline = true;
                                break;
                                
                            } else if (child instanceof GroupItem) {
                                child = ((GroupItem)child).getFirstChild();
                                if (child == null) {
                                    
                                    // empty group, safe to inline
                                    inline = true;
                                    break;
                                    
                                }
                            } else {
                                
                                // required reference item, safe to inline
                                inline = true;
                                break;
                                
                            }
                        }
                        
                    }
                }
                if (inline) {
                    
                    // inline the group
                    if (!grpitem.isInline()) {
                        grpitem.setInline(true);
                    }
                    count += grpcount;
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(SchemaUtils.getIndentation(depth) + "inlining "
                            + (grpitem instanceof DefinitionItem ? "definition " : "group ")
                            + SchemaUtils.describeComponent(grpitem.getSchemaComponent()) + " with item count " +
                            count);
                    }
                    
                } else {
                    
                    // force separate class for group
                    grpitem.setInline(false);
                    count++;
                    
                }
                
            } else {
                count++;
            }
            
            // bump up the complexity if the item is repeated (optionally inlining collection wrappers)
            if (!custom.isNullCollectionAllowed() && item.isCollection()) {
                count++;
            }
        }
        return count > 1 ? 2 : count;
    }
    
    /**
     * Check if a group consists only of a single non-repeating item, which is not an enumeration.
     * 
     * @param group
     * @return <code>true</code> if simple group, <code>false</code> if repeated, multiple, or enumeration
     */
    private boolean isSimple(GroupItem group) {
        if (group.isEnumeration() || group.getChildCount() > 1) {
            return false;
        } else {
            for (Item item = group.getFirstChild(); item != null; item = item.getNext()) {
                if (item.isCollection()) {
                    return false;
                } else if (item instanceof GroupItem && !isSimple((GroupItem)item)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Convert nested groups which are not inlined to freestanding definitions. This calls itself recursively to process
     * nested groups, except those nested within groups converted to definitions.
     *
     * @param group
     * @param defs list of generated definitions
     */
    private void convertToDefinitions(GroupItem group, List defs) {
        Item item = group.getFirstChild();
        while (item != null) {
            if (item instanceof GroupItem) {
                GroupItem childgrp = (GroupItem)item;
                if (childgrp.isInline()) {
                    convertToDefinitions(childgrp, defs);
                } else {
                    DefinitionItem def = childgrp.convertToDefinition();
                    def.setChecked(true);
                    OpenAttrBase ancestor = group.getSchemaComponent();
                    while (!ancestor.isGlobal()) {
                        ancestor = ancestor.getParent();
                    }
                    GlobalExtension global = (GlobalExtension)ancestor.getExtension();
                    PackageHolder pack = global.getPackage();
                    String clasname = def.getClassName();
                    NameConverter nconv = global.getNameConverter();
                    boolean useinner = global.isUseInnerClasses();
                    ClassDecorator[] decorators = global.getClassDecorators();
                    BindingHolder holder = m_bindingDirectory.getRequiredBinding(def.getSchemaComponent().getSchema());
                    ClassHolder clas = pack.addClass(clasname, nconv, decorators, useinner,
                        childgrp.isEnumeration(), holder);
                    def.setGenerateClass(clas);
                    defs.add(def);
                    s_logger.debug("Added definition class " + clas.getFullName());
                }
            }
            item = item.getNext();
        }
    }

    /**
     * Check if a global definition structure is to be inlined. This method is mutually recursive with {@link
     * #computeComplexity(GroupItem, int, List)}. The two methods together determine the inlining status of all items.
     * 
     * @param def
     * @param depth nesting depth
     * @param defs list of generated definitions
     */
    private void checkInline(DefinitionItem def, int depth, List defs) {
        if (def.isChecked()) {
            def.setReferenced(true);
        } else if (def.isInline()) {
            def.setChecked(true);
        } else {
            
            // flag checked and clear referenced to detect circular references
            def.setChecked(true);
            def.setReferenced(false);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug(SchemaUtils.getIndentation(depth) + "checking inlining of definition "
                    + SchemaUtils.describeComponent(def.getSchemaComponent())
                    + (def.isInlineBlocked() ? " (inlining blocked)" : ""));
            }
            
            // inline references where appropriate, and count the values defined
            int count = computeComplexity(def, depth, defs);
            
            // determine the representation based on customizations or complexity
            GlobalExtension exten = (GlobalExtension)def.getComponentExtension();
            if (exten.isInlined()) {
                if (def.isInlineBlocked()) {
                    s_logger.error("Cannot inline " + SchemaUtils.describeComponent(def.getSchemaComponent()) + " because of use as collection value, type for global element, or other usage");
                } else {
                    def.setInline(true);
                }
            } else if (!exten.isSeparateClass() && !def.isInlineBlocked() && (exten.isPushInline() || count == 0 ||
                (count == 1 && isSimple(def)) || (exten.isPreferInline() && def.getReferenceCount() == 1))) {
                if (def.isReferenced()) {
                    s_logger.debug(SchemaUtils.getIndentation(depth) + "blocking inlining of self-referencing definition " +
                         SchemaUtils.describeComponent(def.getSchemaComponent()));
                } else {
                    
                    // set state as inlined
                    def.setInline(true);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(SchemaUtils.getIndentation(depth) + "inlining definition "
                            + SchemaUtils.describeComponent(def.getSchemaComponent()) +
                            " with item count " + count);
                    }
                    
                    // convert non-inlined child components to definitions if multiple use
                    if (def.getReferenceCount() > 1) {
                        convertToDefinitions(def, defs);
                    }
                    
                }
            }
        }
    }
    
    /**
     * Set the name and namespace URI for a concrete &lt;mapping> binding component. This is the same logic as used in
     * the {@link StructureClassHolder} equivalent.
     *
     * @param qname qualified name to be set (<code>null</code> if none)
     * @param mapping concrete mapping definition
     * @param holder binding containing the mapping
     */
    private void setName(QName qname, MappingElementBase mapping, BindingHolder holder) {
        if (qname != null) {
            String name = qname.getName();
            mapping.setName(name);
            String uri = qname.getUri();
            holder.addNamespaceUsage(uri);
            if (!Utility.safeEquals(uri, holder.getElementDefaultNamespace())) {
                mapping.setUri(uri);
            }
        }
    }
    
    /**
     * Build the structure of items to be used in code generation for each global definition.
     *
     * @return constructed definition items
     */
    private ArrayList buildItemStructures() {
        ArrayList items = new ArrayList();
        ItemVisitor itembuilder = new ItemVisitor();
        int index = 0;
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            m_validationContext.enterSchema(schema);
            s_logger.info("Building item structure for schema " + ++index + ": " + schema.getResolver().getName());
            int count = schema.getChildCount();
            for (int i = 0; i < count; i++) {
                SchemaBase child = schema.getChild(i);
                if (child.getExtension() instanceof GlobalExtension) {
                    
                    // create the definition
                    GlobalExtension global = (GlobalExtension)child.getExtension();
                    DefinitionItem definition = global.getDefinition();
                    if (definition == null) {
                        definition = itembuilder.buildGlobal((AnnotatedBase)child);
                        if (s_logger.isInfoEnabled()) {
                            s_logger.info("Constructed item structure for " + SchemaUtils.describeComponent(child)
                                + ":\n" + definition.describe());
                        }
                    } else if (s_logger.isInfoEnabled()) {
                        s_logger.info("Found existing item structure for " + SchemaUtils.describeComponent(child)
                            + ":\n" + definition.describe());
                    }
                    items.add(definition);
                    
                    // set the names on the definition so they'll be available for inlining
                    NameConverter nconv = global.getNameConverter();
                    String dfltname = nconv.toBaseName(((INamed)child).getName());
                    if (!definition.isFixedName()) {
                        String name = global.getBaseName();
                        if (name == null) {
                            name = NameUtils.toNameLead(dfltname);
                        }
                        definition.setName(name);
                    }
                    if (!definition.isFixedClassName()) {
                        String name = global.getClassName();
                        if (name == null) {
                            name = global.getNameConverter().toJavaClassName(dfltname);
                        }
                        definition.setClassName(name);
                    }
                    
                    // force class generation if required
                    if (global.isIncluded()) {
                        definition.setInlineBlocked(true);
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Forcing class generation for " + SchemaUtils.describeComponent(child));
                        }
                    }
    
                }
            }
        }
        return items;
    }

    /**
     * Convert the item model used for global elements, if they're the only reference to a global type. This just
     * prevents generating two separate classes, one for the type and one for the element.
     *
     * @param items global definition items
     * @return map from base type definition to singleton element definition using that type
     */
    private Map convertTypeIsomorphicElements(ArrayList items) {
        
        // check for special (but common) case of single global element using global complexType
        ArrayList checkitems = new ArrayList();
        for (int i = 0; i < items.size(); i++) {
            DefinitionItem definition = (DefinitionItem)items.get(i);
            if (!definition.isPregenerated()) {
                
                // record all simple element definition items separately for next pass
                AnnotatedBase child = definition.getSchemaComponent();
                if (child.type() == SchemaBase.ELEMENT_TYPE && definition.getChildCount() == 1) {
                    Item item = definition.getFirstChild();
                    if (item instanceof ReferenceItem) {
                        DefinitionItem basedef = ((ReferenceItem)item).getDefinition();
                        if (!basedef.isPregenerated()) {
                            AnnotatedBase comp = basedef.getSchemaComponent();
                            if (comp.type() != SchemaBase.SIMPLETYPE_TYPE) {
                                checkitems.add(definition);
                            }
                        }
                    }
                }
            }
        }
        
        // build map from type definition used by element to flag for single vs. multiple usage
        Map usemap = new HashMap();
        for (int i = 0; i < checkitems.size(); i++) {
            
            // make sure the type definition is not using a pregenerated class (since can't be sure won't be reused)
            ReferenceItem reference = (ReferenceItem)((DefinitionItem)checkitems.get(i)).getFirstChild();
            DefinitionItem basedef = reference.getDefinition();
            if (!basedef.hasDirectGenerateClass()) {
                Boolean usedirect = (Boolean)usemap.get(basedef);
                if (usedirect == null) {
                    
                    // first time type was referenced, flag to use directly
                    usemap.put(basedef, Boolean.TRUE);
                    
                } else if (usedirect.booleanValue()) {
                    
                    // second time type was referenced, flag to use separate classes
                    usemap.put(basedef, Boolean.FALSE);
                    
                }
            }
        }
        
        // flag type-isomorphic elements as inlined, building map from type to isomorphic element definition
        HashMap typeinstmap = new HashMap();
        for (int i = 0; i < checkitems.size(); i++) {
            DefinitionItem elemdef = (DefinitionItem)checkitems.get(i);
            ReferenceItem reference = (ReferenceItem)elemdef.getFirstChild();
            DefinitionItem basedef = reference.getDefinition();
            if (!basedef.hasDirectGenerateClass() && ((Boolean)usemap.get(basedef)).booleanValue()) {
                
                // single element definition using type, make sure abstract state matches on element and type
                ElementElement element = (ElementElement)elemdef.getSchemaComponent();
                AnnotatedBase refcomp = basedef.getSchemaComponent();
                if (!(refcomp instanceof ComplexTypeElement) ||
                    ((ComplexTypeElement)refcomp).isAbstract() == element.isAbstract()) {
                    
                    // abstract state matches, force class for type but none for element
                    basedef.setInlineBlocked(true);
                    elemdef.setInlineBlocked(true);
                    typeinstmap.put(basedef, elemdef);
                    elemdef.setTypeIsomorphic(true);
                    
                }
            }
        }
        
        // flag type-isomorphic elements as inlined, building map from type to isomorphic element definition
/*        HashMap typeinstmap = new HashMap();
        for (int i = 0; i < checkitems.size(); i++) {
            DefinitionItem elemdef = (DefinitionItem)checkitems.get(i);
            ReferenceItem reference = (ReferenceItem)elemdef.getFirstChild();
            DefinitionItem basedef = reference.getDefinition();
            if (!basedef.hasDirectGenerateClass() && basedef.getReferenceCount() == 1) {
                
                // single element definition using type, force class for type but none for element
                elemdef.setInlineBlocked(false);
                elemdef.setInline(true);
                typeinstmap.put(basedef, elemdef);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Forcing inlining of type-isomorphic "
                        + SchemaUtils.describeComponent(elemdef.getSchemaComponent()));
                }
                
            }
        }   */
        return typeinstmap;
    }

    /**
     * Build list of definitions to be generated. Where possible, references are converted to inline definitions. Where
     * they can't be converted, new definitions are created for separate class generation.
     *
     * @param items
     * @return definitions to be processed
     */
    private ArrayList inlineReferences(ArrayList items) {
        ArrayList defs = new ArrayList();
        for (int i = 0; i < items.size(); i++) {
            
            // make sure definition has been checked for inlining (may add new definitions directly to list)
            DefinitionItem def = (DefinitionItem)items.get(i);
            checkInline(def, 1, defs);
            
            // add definition to list if not inlined or pregenerated or type-isomorphic
            if (!def.isInline() && !def.hasDirectGenerateClass() && !def.isTypeIsomorphic()) {
                GlobalExtension global = (GlobalExtension)def.getComponentExtension();
                defs.add(def);
                PackageHolder pack = global.getPackage();
                String cname = def.getClassName();
                NameConverter nconv = global.getNameConverter();
                ClassDecorator[] decorators = global.getClassDecorators();
                boolean userinner = global.isUseInnerClasses();
                BindingHolder holder = m_bindingDirectory.getRequiredBinding(def.getSchemaComponent().getSchema());
                ClassHolder clas = pack.addClass(cname, nconv, decorators, userinner, def.isEnumeration(), holder);
                def.setGenerateClass(clas);
            }
        }
        return defs;
    }

    /**
     * Check if no-namespace namespace is used in any of the schemas.
     *
     * @return <code>true</code> if no-namespace used, <code>false</code> if not
     */
    private boolean checkNoNamespacedUsed() {
        SchemaNameVisitor visitor = new SchemaNameVisitor();
        TreeWalker walker = new TreeWalker(null, new SchemaContextTracker());
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            walker.walkSchema((SchemaElement)iter.next(), visitor);
        }
        return visitor.isNoNamespaceUsed();
    }

    /**
     * Build classes and bindings for all definitions, except those for singleton element definitions using a type.
     *
     * @param defs definitions to be generated
     * @param typeinst map from base type definition to singleton element definition using that type
     */
    private void buildClassesAndBindings(ArrayList defs, Map typeinst) {
        
        // process all type definitions
        for (int i = 0; i < defs.size(); i++) {
            
            // compact again after inlining and converting extension references
            DefinitionItem definition = (DefinitionItem)defs.get(i);
            compactGroups(definition);
            
            // build the binding component for this definition
            ClassHolder clas = (ClassHolder)definition.getGenerateClass();
            OpenAttrBase comp = definition.getSchemaComponent();
            SchemaElement schema = comp.getSchema();
            BindingHolder holder = m_bindingDirectory.getRequiredBinding(schema);
            if (definition.isEnumeration()) {
                
                // construct format and add to binding
                FormatElement format = new FormatElement();
                format.setTypeName(clas.getBindingName());
                format.setQName(definition.getQName());
                ((EnumerationClassHolder)clas).setBinding(format);
                m_bindingDirectory.addFormat(format);
                
            } else {
                
                // construct mapping element
                MappingElementBase mapping = new MappingElement();
                mapping.setClassName(clas.getBindingName());
                if (comp.type() == SchemaBase.ELEMENT_TYPE) {
                    
                    // abstract or concrete mapping for element
                    ElementElement element = (ElementElement)comp;
                    setName(element.getEffectiveQName(), mapping, holder);
                    mapping.setAbstract(element.isAbstract());
                    QName group = element.getSubstitutionGroup();
                    if (group != null) {
                        ElementElement base = m_validationContext.findElement(group);
                        DefinitionItem basedef = ((GlobalExtension)base.getExtension()).getDefinition();
                        mapping.setExtendsName(basedef.getGenerateClass().getFullName());
                    }
                    
                } else {
                    
                    // abstract mapping for type definition or group
                    mapping.setAbstract(true);
                    QName qname = definition.getQName();
                    mapping.setTypeQName(qname);
                    String uri = qname.getUri();
                    if (uri != null) {
                        m_bindingDirectory.addTypeNameReference(holder, uri, schema);
                    }
                    
                }
                
                // add the mapping to binding and set on class
                holder.addMapping(mapping);
                ((StructureClassHolder)clas).setBinding(mapping);
                DefinitionItem elementdef = (DefinitionItem)typeinst.get(definition);
                if (elementdef != null) {
                    
                    // create mapping for element name linked to type
                    ElementElement element = (ElementElement)elementdef.getSchemaComponent();
                    SchemaElement elschema = element.getSchema();
                    MappingElementBase elmapping = new MappingElement();
                    elmapping.setClassName(clas.getBindingName());
                    elmapping.setAbstract(element.isAbstract());
                    
                    // handle linking to substitution group head using extends mapping
                    QName group = element.getSubstitutionGroup();
                    if (group != null) {
                        ElementElement base = m_validationContext.findElement(group);
                        DefinitionItem basedef = ((GlobalExtension)base.getExtension()).getDefinition();
                        elmapping.setExtendsName(basedef.getGenerateClass().getFullName());
                    }
                    
                    // create single structure child invoking the type mapping
                    holder = m_bindingDirectory.getRequiredBinding(elschema);
                    setName(element.getEffectiveQName(), elmapping, holder);
                    StructureElement struct = new StructureElement();
                    QName qname = mapping.getTypeQName();
                    String uri = qname.getUri();
                    if (uri != null) {
                        m_bindingDirectory.addTypeNameReference(holder, uri, schema);
                    }
                    struct.setMapAsQName(qname);
                    elmapping.addChild(struct);
                    holder.addMapping(elmapping);
                    
                }
            }
            
            // set the definition for the class (and create any required secondary classes)
            clas.buildDataStructure(definition, holder);
        }
    }

    /**
     * Initialize the bindings to be generated. This configures the binding directory, then creates the required
     * bindings and sets their prefixes.
     */
    private void initializeBindings() {
        m_bindingDirectory = new BindingOrganizer(false, false, false, true, true, true);
        Map namebindings = new HashMap();
        Map nsbindings = new HashMap();
        for (Iterator iter = m_validationContext.iterateSchemas(); iter.hasNext();) {
            
            // check binding handling for schema
            SchemaElement schema = (SchemaElement)iter.next();
            SchemaExtension exten = (SchemaExtension)schema.getExtension();
            String uri = schema.getEffectiveNamespace();
            String prefix = exten.getPrefix();
            BindingHolder holder;
            String name = exten.getBindingFileName();
            boolean dflt = schema.isElementQualifiedDefault() || uri == null;
            if (name != null) {
                
                // specific binding name, create the binding or link to existing
                holder = (BindingHolder)namebindings.get(name);
                if (holder == null) {
                    holder = m_bindingDirectory.getBinding(schema);
                    if (holder == null) {
                        holder = m_bindingDirectory.addBinding(schema, uri, prefix, dflt);
                    }
                    holder.setFileName(name);
                    namebindings.put(name, holder);
                } else {
                    m_bindingDirectory.addBindingObject(schema, holder);
                }
                
            } else if (exten.isForceBinding()) {
                
                // binding needed for this schema, create it
                holder = m_bindingDirectory.addBinding(schema, uri, prefix, dflt);
                
            } else {
                
                // need binding for namespace, create or link to existing
                holder = (BindingHolder)nsbindings.get(uri);
                if (holder == null) {
                    holder = m_bindingDirectory.addBinding(schema, uri, prefix, dflt);
                    nsbindings.put(uri, holder);
                } else {
                    m_bindingDirectory.addBindingObject(schema, holder);
                }
                
            }
        }
    }

    /**
     * Generate the data model. This first builds a representation of all the data items from the schema definitions,
     * then determines which items can be inlined and which need separate class representations, and finally builds the
     * actual data model class and binding definitions.
     * 
     * @param verbose verbose output flag
     * @param usenns no-namespace namespace used flag
     * @param typemap map from qualified name of type (or attributeGroup, or group) to pregenerated mapping definition
     * @param elemmap map from qualified name of element to pregenerated mapping definition
     * @return root package for binding
     */
    public String buildDataModel(boolean verbose, boolean usenns, Map elemmap, Map typemap) {
        
        // build the item structure for each definition and link to pregenerated classes
        ArrayList items = buildItemStructures();
        for (int i = 0; i < items.size(); i++) {
            DefinitionItem definition = (DefinitionItem)items.get(i);
            QName qname = definition.getQName();
            TypeData clas = null;
            switch (definition.getSchemaComponent().type()) {
                case SchemaBase.ATTRIBUTEGROUP_TYPE:
                case SchemaBase.COMPLEXTYPE_TYPE:
                case SchemaBase.GROUP_TYPE:
                case SchemaBase.SIMPLETYPE_TYPE:
                {
                    clas = (TypeData)typemap.get(qname);
                    break;
                }
                case SchemaBase.ELEMENT_TYPE:
                {
                    clas = (TypeData)elemmap.get(qname);
                    break;
                }
            }
            if (clas != null) {
                definition.setGenerateClass(clas);
                definition.setInlineBlocked(true);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Found class " + clas.getFullName() + " for " + qname);
                }
            }
        }
        
        // convert elements based on types and compact all items
        Map typeinstmap = convertTypeIsomorphicElements(items);
        for (int i = 0; i < items.size(); i++) {
            compactGroups((DefinitionItem)items.get(i));
        }
        
        // inline references where appropriate
        initializeBindings();
        ArrayList defs = inlineReferences(items);
        
        // assign class and property names for all items
        for (int i = 0; i < defs.size(); i++) {
            DefinitionItem def = (DefinitionItem)defs.get(i);
            compactGroups(def);
            assignNames(null, def, true);
            if (s_logger.isInfoEnabled()) {
                s_logger.info("After assigning names for "
                    + SchemaUtils.describeComponent(def.getSchemaComponent()) + ":\n" + def.describe());
            }
        }
        
        // convert extension references for all global type definitions
        for (int i = 0; i < defs.size(); i++) {
            ((DefinitionItem)defs.get(i)).convertTypeReference();
        }
        
        // classify all the items by form of content
        for (int i = 0; i < items.size(); i++) {
            DefinitionItem def = (DefinitionItem)items.get(i);
            def.classifyContent();
            if (s_logger.isInfoEnabled()) {
                s_logger.info("After inlining and classification for " +
                    SchemaUtils.describeComponent(def.getSchemaComponent())
                    + ":\n" + def.describe());
            }
        }
        
        // build the actual class and binding structure
        setDefaultPrefixes(m_validationContext.iterateSchemas());
        buildClassesAndBindings(defs, typeinstmap);
        
        // build the actual classes
        AST ast = AST.newAST(AST.JLS3);
        ArrayList packs = m_packageDirectory.getPackages();
        PackageHolder rootpack = null;
        for (int i = 0; i < packs.size(); i++) {
            PackageHolder pack = ((PackageHolder)packs.get(i));
            if (pack.getClassCount() > 0) {
                if (rootpack == null) {
                    PackageHolder scan = pack;
                    while (scan != null) {
                        if (scan.getClassCount() > 0 || scan.getSubpackageCount() > 1) {
                            rootpack = scan;
                        }
                        scan = scan.getParent();
                    }
                }
                pack.generate(verbose, ast, m_bindingDirectory);
            }
        }
        
        // return the root package name
        if (rootpack == null) {
            rootpack = m_packageDirectory.getPackage("");
        }
        return rootpack.getName();
    }

    /**
     * Get the binding directory.
     *
     * @return directory
     */
    public BindingOrganizer getBindingDirectory() {
        return m_bindingDirectory;
    }
    
    /**
     * Write the binding definitions file(s). This method can only be used after
     * {@link #buildDataModel(boolean, boolean, Map, Map)} is called.
     *
     * @param name root binding definition file name (use customization, or default, if <code>null</code>)
     * @param pack target package for binding (<code>null</code> if unspecified)
     * @param pregens pregenerated bindings to be included in root binding
     * @param handler validation error and code generation problem handler
     * @throws JiBXException
     * @throws IOException
     */
    public void writeBindings(String name, String pack, List pregens, ProblemHandler handler)
    throws JiBXException, IOException {
        if (name == null) {
            name = m_global.getBindingFileName();
            if (name == null) {
                name = "binding.xml";
            }
        }
        m_rootHolder = m_bindingDirectory.configureFiles(name, pack, pregens);
        IClassLocator iloc = new DummyClassLocator();
        org.jibx.binding.model.ValidationContext vctx = new org.jibx.binding.model.ValidationContext(iloc);
        if (m_bindingDirectory.validateBindings(m_rootHolder, m_targetDir, vctx)) {
            m_bindingDirectory.writeBindings(m_targetDir);
        } else {
            reportBindingProblems(vctx, handler);
            throw new JiBXException("Terminating due to errors in bindings");
        }
    }
    
    /**
     * Get the root binding definition. This is only allowed after the call to
     * {@link #writeBindings(String, String, List, ProblemHandler)}.
     * 
     * @return root binding element
     */
    public BindingElement getRootBinding() {
        return m_rootHolder.getBinding();
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
/*    private static void addCustomizedSchemas(URL base, File dir, SchemasetCustom custom, InsertionOrderedSet fileset)
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
*/    
    /**
     * Get the package directory used for code generation.
     * 
     * @return directory
     */
    public PackageOrganizer getPackageDirectory() {
        return m_packageDirectory;
    }
    
    /**
     * Generate code from a list of schemas.
     * 
     * @param verbose verbose output flag
     * @param usens namespace to be used when no schemas with namespaces are being generated (<code>null</code> if not
     * specified)
     * @param dfltpkg default package for no-namespace schemas (<code>null</code> if not specified)
     * @param bindname name for root binding (<code>null</code> if not specified)
     * @param fileset list of resolvers for schemas to be generated
     * @param inclpaths list of paths for bindings to be used for matching schema definitions (empty if none)
     * @param model file to be used for dumping generated data model (<code>null</code> if none)
     * @param handler validation error and code generation problem handler
     * @return <code>true</code> if successful, <code>false</code> if failure
     * @throws JiBXException
     * @throws IOException
     */
    public boolean generate(boolean verbose, String usens, String dfltpkg, String bindname, List fileset,
        List inclpaths, File model, ProblemHandler handler) throws JiBXException, IOException {
        
        // load the full set of schemas
        SchemaElement[] schemas = ValidationUtils.load(fileset, usens, m_validationContext);
        if (!m_validationContext.reportProblems(handler)) {
            if (dfltpkg == null) {
                dfltpkg = "dflt";
            }
            if (customizeSchemas(dfltpkg, handler)) {
                
                // report on schemas loaded
                StringBuffer buff = new StringBuffer();
                buff.append("Loaded and validated " + fileset.size() + " specified schema(s)");
                int refcount = m_validationContext.getSchemaCount() - fileset.size();
                if (refcount > 0) {
                    buff.append(" and " + refcount + " referenced schema(s)");
                }
                if (verbose) {
                    buff.append(':');
                    handler.report(buff.toString());
                    listSchemas(schemas, m_validationContext, handler);
                } else {
                    handler.report(buff.toString());
                }
                
                // apply the schema customizations and revalidate modified schema
                applyAndNormalize();
                pruneDefinitions();
                ValidationUtils.validateSchemas(schemas, m_validationContext);
                if (!m_validationContext.reportProblems(handler)) {
                    
                    // load any precompiled bindings
                    Map elemmap = new HashMap();
                    Map typemap = new HashMap();
                    List includes = new ArrayList();
                    boolean usenns = false;
                    for (Iterator iter = inclpaths.iterator(); iter.hasNext();) {
                        String path = (String)iter.next();
                        URL url;
                        File file = null;
                        if (ClasspathUrlExtender.isClasspathUrl(path)) {
                            url = ClasspathUrlExtender.buildURL(null, path);
                        } else {
                            file = new File(path);
                            if (!file.isAbsolute())
                                file = new File(m_targetDir, path);
                            url = file.toURI().toURL();
                        }
                        try {
                            BindingElement binding = processPregeneratedBinding(url, elemmap, typemap, handler);
                            if (!usenns) {
                                usenns = checkNoNamespace(binding);
                            }
                            try {
                                org.jibx.binding.model.IncludeElement include =
                                    new org.jibx.binding.model.IncludeElement();
                                if (file == null) {
                                    include.setIncludePath(path);
                                } else {
                                    include.setIncludePath(relativeFilePath(m_targetDir, file));
                                }
                                include.setPrecompiled(true);
                                includes.add(include);
                            } catch (IOException e) {
                                handler.terminate("Error processing path " + path, e);
                                return false;
                            }
                        } catch (IOException e) {
                            handler.terminate("Pregenerated binding not found with path: " + path);
                            return false;
                        }
                    }
                    
                    // generate code and bindings, using any existing bindings supplied
                    buildDataModel(verbose, usenns, elemmap, typemap);
                    List packages = m_packageDirectory.getPackages();
                    PackageHolder top = null;
                    for (Iterator iter = packages.iterator(); iter.hasNext();) {
                        PackageHolder holder = (PackageHolder)iter.next();
                        if (holder.getClassCount() > 0) {
                            if (top == null) {
                                top = holder;
                            } else {
                                
                                // find common parent package of all packages so far processed
                                outer: while (true) {
                                    PackageHolder scan = holder;
                                    while (true) {
                                        if (scan == top) {
                                            break outer;
                                        } else if (scan.getName().length() < top.getName().length()) {
                                            top = top.getParent();
                                            break;
                                        } else {
                                            scan = scan.getParent();
                                        }
                                    }
                                }
                            
                            }
                        }
                    }
                    String rootpack = top == null ? null : top.getName();
                    writeBindings(bindname, rootpack, includes, handler);
                    
                    // print the total number of classes generated
                    listGeneratedPackages(packages, handler);
                    
                    // check if prior data model dump file provided for comparison
                    if (model != null) {
                        if (model.exists()) {
                            
                            // read the data model file
                            BufferedReader reader = new BufferedReader(new FileReader(model));
                            StringObjectPair[] image = DataModelUtils.readImage(reader);
                            String diff = DataModelUtils.imageDiff(image, DataModelUtils.getImage(m_packageDirectory));
                            if (diff == null) {
                                handler.report("No difference found from data model file " + model.getPath());
                            } else {
                                handler.report(diff);
                            }
                            
                        } else {
                            handler.report("Difference data model file " + model.getPath() + " not found");
                        }
                    }
                    
                    // check if dump file to be output
                    if (model != null) {
                        
                        // delete the file if it already exists
                        if (model.exists()) {
                            model.delete();
                        }
                        model.createNewFile();
                        
                        // now print out the class details by package
                        BufferedWriter writer = new BufferedWriter(new FileWriter(model));
                        DataModelUtils.writeImage(m_packageDirectory, writer);
                        writer.close();
                        
                    }
                } else {
                    handler.terminate("Terminating due to errors in normalized schemas - please report the problem");
                    return false;
                }
            } else {
                handler.terminate("Terminating due to errors in customized schemas");
                return false;
            }
        } else {
            handler.terminate("Terminating due to errors in input schemas");
            return false;
        }
        return true;
    }

    /**
     * List the schemas in use.
     *
     * @param schemas
     * @param vctx
     * @param handler
     */
    private static void listSchemas(SchemaElement[] schemas, ValidationContext vctx, ProblemHandler handler) {
        Set topset = new HashSet();
        for (int i = 0; i < schemas.length; i++) {
            SchemaElement schema = schemas[i];
            topset.add(schema);
            handler.report(" " + "top-level schema " + schema.getResolver().getName());
        }
        for (Iterator iter = vctx.iterateSchemas(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            if (!topset.contains(schema)) {
                handler.report(" " + "referenced schema " + schema.getResolver().getName());
            }
        }
    }

    /**
     * List the number of classes in each package, and the totals.
     *
     * @param packages
     * @param handler
     */
    private static void listGeneratedPackages(List packages, ProblemHandler handler) {
        int top = 0;
        int total = 0;
        StringBuffer buff = new StringBuffer();
        for (Iterator iter = packages.iterator(); iter.hasNext();) {
            PackageHolder hold = (PackageHolder)iter.next();
            int count = hold.getClassCount();
            if (count > 0) {
                int topcount = hold.getTopClassCount();
                top += topcount;
                buff.setLength(0);
                buff.append("Generated " + topcount + " top-level classes ");
                if (count != topcount) {
                    buff.append("(plus " + (count-topcount) + " inner classes) ");
                }
                buff.append("in package " + hold.getName());
                handler.report(buff.toString());
                total += count;
            }
        }
        if (total == top) {
            handler.report("Total classes in model: " + top);
        } else {
            handler.report("Total top-level classes in model: " + top);
            handler.report("Total classes (including inner classes) in model: " + total);
        }
    }

    /**
     * Build class data for pregenerated class.
     *
     * @param cname class name
     * @param simple simple value flag
     * @return data
     */
    private static TypeData buildClassData(String cname, boolean simple) {
        String bname = cname;
        String fname = bname.replace('$', '.');
        return new TypeData(fname, bname, true, simple);
    }

    /**
     * Accumulate all format and mapping definitions, including those found in included bindings. For each abstract
     * mapping or named format found, the type name is associated with the class data in the type map; for each concrete
     * mapping found, the element name (and namespace) is associated with the class data in the element map. Included
     * bindings are handled with recursive calls.
     *
     * @param binding binding definition root
     * @param elemmap map from element qualified name to class data
     * @param typemap map from type qualified name to class data
     */
    private static void accumulateBindingDefinitions(BindingElement binding, Map elemmap, Map typemap) {
        ArrayList childs = binding.topChildren();
        for (int i = 0; i < childs.size(); i++) {
            ElementBase element = (ElementBase)childs.get(i);
            if (element.type() == ElementBase.INCLUDE_ELEMENT) {
                
                // use recursive call to add nested definitions in included binding
                BindingElement inclbinding = ((org.jibx.binding.model.IncludeElement)element).getBinding();
                if (inclbinding != null) {
                    accumulateBindingDefinitions(inclbinding, elemmap,
                        typemap);
                }
                
            } else if (element.type() == ElementBase.MAPPING_ELEMENT) {
                
                // handle mapping as type if abstract with type name, or as element if concrete
                MappingElementBase mapping = (MappingElementBase)element;
                String cname = mapping.getClassName();
                TypeData data = buildClassData(cname, false);
                if (mapping.isAbstract()) {
                    QName qname = mapping.getTypeQName();
                    if (qname != null) {
                        typemap.put(qname, data);
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Added class " + cname + " for type " + qname);
                        }
                    }
                } else {
                    QName qname = new QName(mapping.getNamespace().getUri(), mapping.getName());
                    elemmap.put(qname, data);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Added class " + cname + " for element " + qname);
                    }
                }
                
            } else if (element.type() == ElementBase.FORMAT_ELEMENT) {
                
                // add named format as simple type definition
                FormatElement format = (FormatElement)element;
                String name = format.getTypeName();
                if (name != null) {
                    TypeData data = buildClassData(name, true);
                    typemap.put(format.getQName(), data);
                }
                
            }
        }
    }

    /**
     * Load and validate binding and process all format and mapping definitions, including those in included bindings.
     *
     * @param url binding definition path
     * @param elemmap map from element qualified name to class data
     * @param typemap map from type qualified name to class data
     * @param handler validation error and problem handler
     * @return binding
     * @throws JiBXException
     * @throws IOException
     */
    public static BindingElement processPregeneratedBinding(URL url, Map elemmap, Map typemap, ProblemHandler handler)
        throws JiBXException, IOException {
        
        // get binding definition file name from path
        String name = "";
        String path = url.getPath();
        if (path != null) {
            name = path;
            int split = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
            if (split > 0) {
                name = name.substring(split+1);
            }
            split = name.lastIndexOf('.');
            name = name.substring(0, split);
        }
        
        // construct object model for binding
        org.jibx.binding.model.ValidationContext vctx =
            new org.jibx.binding.model.ValidationContext(new DummyClassLocator());
        BindingElement binding = BindingElement.readBinding(url.openStream(), name, null, true,
            vctx);
        binding.setBaseUrl(url);
        vctx.setBindingRoot(binding);
        
        // validate the binding definition
        binding.runValidation(vctx);
        
        // list validation errors
        if (vctx.getProblems().size() > 0) {
            reportBindingProblems(vctx, handler);
            if (vctx.getErrorCount() > 0 || vctx.getFatalCount() > 0) {
                throw new JiBXException("Errors in binding");
            }
        }
        
        // add all the mapping and format definitions in binding to qualified name maps
        accumulateBindingDefinitions(binding, elemmap, typemap);
        return binding;
    }

    /**
     * Report problems found in binding.
     * 
     * @param vctx
     * @param handler
     */
    private static void reportBindingProblems(org.jibx.binding.model.ValidationContext vctx, ProblemHandler handler) {
        StringBuffer buff = new StringBuffer();
        ArrayList problems = vctx.getProblems();
        for (int i = 0; i < problems.size(); i++) {
            org.jibx.binding.model.ValidationProblem prob =
                (org.jibx.binding.model.ValidationProblem)problems.get(i);
            buff.setLength(0);
            buff.append(prob.getSeverity() >= org.jibx.binding.model.ValidationProblem.ERROR_LEVEL ?
                "Error: " : "Warning: ");
            buff.append(prob.getDescription());
            handler.report(buff.toString());
        }
    }

    /**
     * Find the steps in the canonical path to a file.
     *
     * @param file
     * @return steps
     * @throws IOException
     */
    public static List findPathSteps(File file) throws IOException {
        LinkedList steps = new LinkedList();
        steps.addFirst(file.getName());
        File parent = file;
        while ((parent = parent.getParentFile()) != null) {
            steps.addFirst(parent.getName());
        }
        return steps;
    }
    
    /**
     * Construct a relative file path.
     *
     * @param dir start directory for path
     * @param file supplied file path
     * @return relative file path
     * @throws IOException
     */
    public static String relativeFilePath(File dir, File file) throws IOException {
        
        // first find any common portion of the paths
        List dirsteps = findPathSteps(dir);
        List filesteps = findPathSteps(file);
        
        // scan past any common portion of the paths
        Iterator diriter = dirsteps.iterator();
        Iterator fileiter = filesteps.iterator();
        boolean abs = dir.isAbsolute();
        boolean diff = false;
        String filestep = null;
        while (diriter.hasNext() && fileiter.hasNext()) {
            String dirstep = (String)diriter.next();
            filestep = (String)fileiter.next();
            if (!dirstep.equals(filestep)) {
                diff = true;
                break;
            } else {
                abs = false;
            }
        }
        if (!diriter.hasNext())
            filestep = (String)fileiter.next();	// Relative to target directory
        
        // check whether relative path can be used
        if (abs) {
            return file.getCanonicalPath();
        } else {
            
            // create relative path to common ancestor directory
            StringBuffer buff = new StringBuffer();
            if (diff) {
                buff.append("..");
                buff.append(File.separatorChar);
            }
            while (diriter.hasNext()) {
                buff.append("..");
                buff.append(File.separatorChar);
                diriter.next();
            }
            
            // extend path to the actual file 
            buff.append(filestep);
            while (fileiter.hasNext()) {
                buff.append(File.separatorChar);
                buff.append(fileiter.next());
            }
            return buff.toString();
        }
    }

    /**
     * Check if a binding definition uses the no-namespace namespace. This calls itself to recursively check on included
     * bindings.
     *
     * @param binding
     * @return <code>true</code> if no-namespace namespace used, <code>false</code> if not
     */
    public static boolean checkNoNamespace(BindingElement binding) {
        loop: for (Iterator childiter = binding.topChildIterator(); childiter.hasNext();) {
            ElementBase child = (ElementBase)childiter.next();
            switch (child.type()) {
                
                case ElementBase.NAMESPACE_ELEMENT:
                    if (((NamespaceElement)child).getPrefix() == null) {
                        return true;
                    }
                    break;
                    
                case ElementBase.INCLUDE_ELEMENT:
                    if (checkNoNamespace(((org.jibx.binding.model.IncludeElement)child).getBinding())) {
                        return true;
                    }
                    break;
                    
                case ElementBase.MAPPING_ELEMENT:
                    break loop;
                    
            }
        }
        return false;
    }

    /**
     * Run the binding generation using command line parameters.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        TreeWalker.setLogging(Level.ERROR);
        CodeGenCommandLine parms = new CodeGenCommandLine();
        if (args.length > 0 && parms.processArgs(args)) {
            
            // build set of schemas specified on command line (including via wildcards)
            final InsertionOrderedSet fileset = new InsertionOrderedSet();
            URL base = parms.getSchemaRoot();
            File basedir = parms.getSchemaDir();
            List errors = ResourceMatcher.matchPaths(basedir, base, parms.getExtraArgs(),
                new ResourceMatcher.ReportMatch() {
                    public void foundMatch(String path, URL url) {
                        fileset.add(new UrlResolver(path, url));
                    }
                });
            if (errors.size() > 0) {
                for (Iterator iter = errors.iterator(); iter.hasNext();) {
                    s_logger.error(iter.next());
                }
                System.exit(2);
            }
            
            // handle code generation from schemas
            ProblemMultiHandler handler = new ProblemMultiHandler();
            handler.addHandler(new ProblemConsoleLister());
            handler.addHandler(new ProblemLogLister(s_logger));
            CodeGen inst = new CodeGen(parms.getCustomRoot(), parms.getSchemaRoot(),
                parms.getGeneratePath());
            inst.generate(parms.isVerbose(), parms.getUsingNamespace(), parms.getNonamespacePackage(),
                parms.getBindingName(), fileset.asList(), parms.getIncludePaths(), parms.getModelFile(), handler);
            
        } else {
            if (args.length > 0) {
                s_logger.error("Terminating due to command line or customization errors");
            } else {
                parms.printUsage();
            }
            System.exit(1);
        }
    }

    /**
     * Visitor to count the number of values in a definition.
     */
    private static class ValueCountVisitor extends SchemaVisitor
    {
        // count of attributes, elements, atttributeGroup references, and group references
        private int m_count;
        
        public int getCount() {
            return m_count;
        }
        
        public boolean visit(AttributeElement node) {
            m_count++;
            return false;
        }
        
        public boolean visit(AttributeGroupRefElement node) {
            m_count++;
            return false;
        }
        
        public boolean visit(ElementElement node) {
            m_count++;
            return false;
        }
        
        public boolean visit(GroupRefElement node) {
            m_count++;
            return false;
        }
    }
    
    /**
     * Visitor for checking element namespace usage in schema definitions. This just accumulates the set of namespaces
     * used by element definitions.
     */
    private static class SchemaNameVisitor extends SchemaVisitor
    {
        /** Set of namespace URIs used by element definitions. */
        private final Set m_uris;
        
        /**
         * Constructor.
         */
        public SchemaNameVisitor() {
            m_uris = new HashSet();
        }
        
        /**
         * Check if a single namespace is used for all element definitions.
         *
         * @return <code>true</code> if single namespace, <code>false</code> if not
         */
        public boolean isSingleNamespace() {
            return m_uris.size() <= 1;
        }
        
        /**
         * Check if the no-namespace namespace is used by one or more elements.
         *
         * @return <code>true</code> if no-namespace used, <code>false</code> if not
         */
        public boolean isNoNamespaceUsed() {
            return m_uris.contains(null);
        }

        /**
         * Accumulate namespace used by element definition.
         *
         * @param node
         */
        public void exit(ElementElement node) {
            QName qname = node.getQName();
            if (qname != null) {
                m_uris.add(qname.getUri());
            }
            super.exit(node);
        }
    }
}
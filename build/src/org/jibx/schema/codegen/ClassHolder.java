/*
 * Copyright (c) 2006-2010, Dennis M. Sosnoski. All rights reserved.
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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.jibx.binding.classes.ClassItem;
import org.jibx.binding.model.BindingHolder;
import org.jibx.binding.model.ElementBase;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.schema.INamed;
import org.jibx.schema.IReference;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.codegen.custom.ComponentExtension;
import org.jibx.schema.codegen.custom.NestingCustomBase;
import org.jibx.schema.codegen.custom.SchemaCustom;
import org.jibx.schema.codegen.custom.SchemaExtension;
import org.jibx.schema.codegen.extend.ClassDecorator;
import org.jibx.schema.codegen.extend.NameConverter;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AnnotationElement;
import org.jibx.schema.elements.AnnotationItem;
import org.jibx.schema.elements.DocumentationElement;
import org.jibx.schema.elements.FilteredSegmentList;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.util.LazyList;
import org.jibx.util.UniqueNameSet;
import org.w3c.dom.Node;

/**
 * Information for a class to be included in code generated from schema.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class ClassHolder extends TypeData implements IClassHolder
{
    // collection constants
    protected static final String COLLECTION_VARIABLE_TYPE = "java.util.List";
    protected static final String COLLECTION_VARIABLE_NAME = "list";
    
    // general definitions used in binding code generation
//    protected static final String STATIC_UNMARSHAL_METHOD = "_unmarshal_if_present";
//    protected static final String STRUCTURE_INTERFACE = "org.jibx.v2.MappedStructure";
//    protected static final String MARSHAL_METHOD = "_marshal";
//    protected static final String WRITER_TYPE = "org.jibx.v2.XmlWriter";
//    protected static final String WRITER_VARNAME = "wrtr";
//    protected static final String UNMARSHAL_METHOD = "_unmarshal";
//    protected static final String QNAME_TYPE = "org.jibx.runtime.QName";
//    protected static final String TYPE_INTERFACE = "org.jibx.v2.MappedType";
//    protected static final String TYPE_NAME_METHOD = "_get_type_qname";
//    protected static final String TYPE_NAME_VARIABLE = "_type_name";
//    protected static final String ELEMENT_INTERFACE = "org.jibx.v2.MappedElement";
//    protected static final String ELEMENT_NAME_METHOD = "_get_element_qname";
//    protected static final String ELEMENT_NAME_VARIABLE = "_element_name";
//    protected static final String INSTANCE_VARNAME = "inst";
    
    // reader definitions
//    protected static final String READER_TYPE = "org.jibx.v2.XmlReader";
//    protected static final String READER_VARNAME = "rdr";
//    protected static final String READER_CHECK_START_TAG_METHOD = "checkStartTag";
    
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(ClassHolder.class.getName());
    
    /** Schema documentation generator. */
    private static final SchemaDocumentationGenerator s_generator;
    static {
        try {
            s_generator = new SchemaDocumentationGenerator();
        } catch (JiBXException e) {
            s_logger.fatal("Error loading schema extract binding", e);
            throw new IllegalStateException("Internal error - error loading schema extract binding: " + e.getMessage());
        }
    }
    
    /** Null transformer used to output text form of documentation. */
    private static final Transformer s_transformer;
    static {
        try {
            s_transformer = TransformerFactory.newInstance().newTransformer();
            s_transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (TransformerConfigurationException e) {
            s_logger.fatal("Error creating empty transformer", e);
            throw new IllegalStateException("Internal error - error creating empty transformer: " + e.getMessage());
        } catch (TransformerFactoryConfigurationError e) {
            s_logger.fatal("Error creating empty transformer", e);
            throw new IllegalStateException("Internal error - error creating empty transformer: " + e.getMessage());
        }
    }
    
    //
    // Private instance data.
    
    /** Simple class name. */
    private final String m_name;
    
    /** Superclass to be extended as part of schema model (<code>null</code> if none). */
    private TypeData m_superClass;
    
    /** Superclass name to be extended if extension not required by model (<code>null</code> if none). */
    private String m_superName;
    
    /** Class generated flag. */
    private boolean m_generated;
    
    /** Builder for class. */
    private ClassBuilder m_classBuilder;
    
    /** Customizations for the schema containing the schema component matching this class. */
    private SchemaCustom m_schemaCustom;
    
    //
    // Data shared with subclasses.
    
    /** Package containing class. */
    protected final PackageHolder m_package;
    
    /** Holder for class binding definition. */
    protected final BindingHolder m_holder;
    
    /** Name conversion handler. */
    protected final NameConverter m_nameConverter;
    
    /** Base class name (for use when generating separate classes for nested structures). */
    protected final String m_baseName;
    
    /** Decorators to be called in order during class code generation. */
    protected final ClassDecorator[] m_decorators;
    
    /** Use inner classes for substructures flag. */
    protected final boolean m_useInnerClasses;
    
    /** Holders for inner classes defined within this class (<code>null</code> if an inner class). */
    protected final LazyList m_inners;
    
    /** Containing class (<code>null</code> if not an inner class). */
    protected final ClassHolder m_outerClass;
    
    /** Tracker for imports. */
    protected final ImportsTracker m_importsTracker;
    
    /** Class used for initializing list instances. */
    protected String m_listImplClass = "java.util.ArrayList";
    
    /** Value names used in class. */
    protected UniqueNameSet m_nameSet;
    
    /**
     * Constructor.
     * 
     * @param name class name
     * @param base base class name
     * @param pack package information
     * @param holder binding holder
     * @param nconv name converter
     * @param decorators class decorators
     * @param inner use inner classes for substructures
     * @param simple simple value flag
     */
    public ClassHolder(String name, String base, PackageHolder pack, BindingHolder holder, NameConverter nconv,
        ClassDecorator[] decorators, boolean inner, boolean simple) {
        super(buildName(name, pack), simple);
        m_package = pack;
        m_holder = holder;
        m_nameConverter = nconv;
        m_name = name;
        m_baseName = base;
        m_decorators = decorators;
        m_useInnerClasses = inner;
        m_importsTracker = new ImportsTracker(pack.getName());
        m_outerClass = null;
        m_inners = new LazyList();
        m_nameSet = new UniqueNameSet();
        m_nameSet.add(name);
        m_importsTracker.addLocalType(name, getFullName());
    }

    /**
     * Build fully-qualified class name
     *
     * @param name simple class name
     * @param pack package information
     * @return fully-qualified class name
     */
    private static String buildName(String name, PackageHolder pack) {
        StringBuffer buff = new StringBuffer();
        buff.append(pack.getName());
        if (buff.length() > 0) {
            buff.append('.');
        }
        buff.append(name);
        return buff.toString();
    }
    
    /**
     * Constructor for creating a child inner class definition.
     * 
     * @param name class name
     * @param context parent class
     * @param simple simple value flag
     */
    protected ClassHolder(String name, ClassHolder context, boolean simple) {
        super(context.getFullName() + '.' + name, context.getBindingName() + '$' + name, simple);
        m_package = context.m_package;
        m_holder = context.m_holder;
        m_nameConverter = context.m_nameConverter;
        m_name = name;
        m_baseName = null;
        m_decorators = context.m_decorators;
        m_useInnerClasses = true;
        m_importsTracker = context.m_importsTracker;
        m_outerClass = context;
        m_inners = new LazyList();
        m_nameSet = new UniqueNameSet();
        ClassHolder scan = this;
        while (scan != null) {
            m_nameSet.add(scan.getName());
            scan = scan.m_outerClass;
        }
        m_importsTracker.addLocalType(name, getFullName());
        m_package.addInnerClass(this);
    }
    
    /**
     * Get the schema customization associated with root schema component matching this class.
     *
     * @return schema customization
     */
    public SchemaCustom getSchemaCustom() {
        return m_schemaCustom;
    }
    
    /**
     * Extract schema documentation from an element. This just checks for an <b>annotation</b> element on the supplied
     * element, and if found returns a text string consisting of the content of all <b>documentation</b> element(s). The
     * returned documentation text is suitable for use as JavaDoc content, with any JavaDoc end sequences ('*' followed
     * by '/') substituted with a space added.
     *
     * @param element
     * @return content of <b>documentation</b> elements, or <code>null</code> if none
     */
    protected String extractDocumentation(AnnotatedBase element) {
        if (m_schemaCustom.isJavaDocDocumentation()) {
            StringWriter writer = new StringWriter();
            AnnotationElement anno = element.getAnnotation();
            if (anno != null) {
                FilteredSegmentList items = anno.getItemsList();
                for (int i = 0; i < items.size(); i++) {
                    AnnotationItem item = (AnnotationItem)items.get(i);
                    if (item instanceof DocumentationElement) {
                        DocumentationElement doc = (DocumentationElement)item;
                        List contents = doc.getContent();
                        if (contents != null) {
                            for (Iterator iter = contents.iterator(); iter.hasNext();) {
                                Node node = (Node)iter.next();
                                DOMSource source = new DOMSource(node);
                                StreamResult result = new StreamResult(writer);
                                try {
                                    s_transformer.transform(source, result);
                                } catch (TransformerException e) {
                                    s_logger.error("Failed documentation output transformation", e);
                                }
                            }
                        }
                    }
                }
            }
            StringBuffer buff = writer.getBuffer();
            if (buff.length() > 0) {
                
                // make sure there's no embedded comment end marker
                int index = buff.length();
                while ((index = buff.lastIndexOf("*/", index)) >= 0) {
                    buff.replace(index, index+2, "* /");
                }
                return buff.toString();
            }
        }
        return null;
    }
    
    /**
     * Describe the schema component associated with a node. If the component for the supplied node is a named element
     * or attribute, this just returns a combination of the name and type of that component. Otherwise, it moves up the
     * node tree until it finds a named element or attribute, terminating if any parent has more than one child. If it
     * can't find an element or attribute name but does find a named type, it uses that type as the name. If all else
     * fails, it just returns the type of the highest level component found.
     *
     * @param node
     * @return name
     */
    protected static String describe(DataNode node) {
        DataNode current = node;
        OpenAttrBase last;
        String name = null;
        String alttext = null;
        do {
            OpenAttrBase comp = current.getSchemaComponent();
            last = comp;
            if (comp instanceof INamed) {
                String check = ((INamed)comp).getName();
                if (check != null) {
                    int type = comp.type();
                    if (type == SchemaBase.ATTRIBUTE_TYPE || type == SchemaBase.ELEMENT_TYPE) {
                        name = check;
                    } else if (alttext == null) {
                        alttext = "'" + check + "' " + comp.name();
                    }
                }
            }
            if (name == null && comp instanceof IReference) {
                QName ref = ((IReference)comp).getRef();
                if (ref != null) {
                    name = ref.getName();
                }
            }
            if (name != null) {
                return "'" + name + "' " + comp.name();
            }
            current = current.getParent();
        } while (current != null && ((ParentNode)current).getChildren().size() == 1);
        if (alttext == null) {
            return last.name();
        } else {
            return alttext;
        }
    }
    
    /**
     * Derive group names from the containing group prefix and the simple name of the group.
     *
     * @param group
     * @param container (<code>null</code> if none)
     * @return name
     */
//    static String deriveGroupName(GroupItem group, Group container) {
//        String prefix = null;
//        if (container != null) {
//            prefix = group.getClassName();
//            String prior = container.getPrefix();
//            if (prior == null) {
//                prefix = NameConverter.toNameLead(prefix);
//            } else {
//                prefix = prior + NameConverter.toNameWord(prefix);
//            }
//            prefix = container.uniqueChildPrefix(prefix);
//        }
//        return prefix;
//    }
    
    /**
     * Import the type associated with an item, if not directly accessible
     *
     * @param value
     */
    protected void importValueType(DataNode value) {
        String type = value.getType();
        if (type != null) {
            while (type.endsWith("[]")) {
                type = type.substring(0, type.length()-2);
            }
            if (!ClassItem.isPrimitive(type)) {
                ClassHolder outer = this;
                while (outer.m_outerClass != null) {
                    outer = outer.m_outerClass;
                }
                String topname = outer.getFullName();
                if (!type.equals(topname) && (!type.startsWith(topname) || type.charAt(topname.length()) != '.')) {
                    m_importsTracker.addImport(type, false);
                }
            }
        }
    }
    
    /**
     * Convert an item structure to a class representation. Subclasses need to override this method for thie own
     * handling, but should call the base class implementatino first to initialize the schema customization link.
     *
     * @param group item group
     * @param bindhold associated binding definition holder
     */
    public void buildDataStructure(GroupItem group, BindingHolder bindhold) {
        m_schemaCustom = ((SchemaExtension)group.getSchemaComponent().getSchema().getExtension()).getCustom();
    }

    /**
     * Get containing package.
     * 
     * @return package
     */
    public PackageHolder getPackage() {
        return m_package;
    }
    
    /**
     * Get simple name.
     * 
     * @return name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Get containing class of inner class.
     * 
     * @return outer containing class, or <code>null</code> if not an inner class
     */
    public IClassHolder getOuterClass() {
        return m_outerClass;
    }
    
    /**
     * Get base class to be extended.
     *
     * @return base (<code>null</code> if none)
     */
    public TypeData getSuperClass() {
        return m_superClass;
    }

    /**
     * Set superclass to be extended.
     *
     * @param sclas (<code>null</code> if none)
     */
    public void setSuperClass(TypeData sclas) {
        m_superClass = sclas;
        if (sclas != null) {
            boolean imported = m_importsTracker.addImport(sclas.getFullName(), false);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Set superclass of " + getFullName() + " to " + sclas.getFullName() +
                    (imported ? " (imported)" : ""));
            }
        }
    }
    
    /**
     * Check if superclass is forced by schema model.
     *
     * @return <code>true</code> if superclass forced, <code>false</code> if not
     */
    public boolean isSuperClassForced() {
        return m_superClass != null;
    }
    
    /**
     * Get name of base class to be extended.
     *
     * @return base (<code>null</code> if none)
     */
    public String getSuperClassName() {
        if (m_superClass == null) {
            return m_superName;
        } else {
            return m_superClass.getFullName();
        }
    }
    
    /**
     * Set name of base class to be extended. This method can only be used if a superclass has not been forced by the
     * schema model. It is always safe to use this method if {@link #getSuperClassName()} returns <code>null</code>.
     *
     * @param base fully-qualified class name of base class (<code>null</code> if none)
     */
    public void setSuperClassName(String base) {
        if (m_superClass == null) {
            m_superName = base;
            if (base != null) {
                boolean imported = m_importsTracker.addImport(base, false);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Set superclass of " + getFullName() + " to " + base +
                        (imported ? " (imported)" : ""));
                }
            }
        } else {
            throw new IllegalArgumentException("Error - superclass is forced by schema model and cannot be overridden");
        }
    }

    /**
     * Set name of list implementation class to be used for initializing instances.
     *
     * @param list fully-qualified class name of list implementation (non-<code>null</code>)
     */
    public void setListImplementation(String list) {
        if (list == null) {
            throw new IllegalStateException("Internal error - list implementation class cannot be null");
        } else {
            m_listImplClass = list;
        }
    }

    /**
     * Check if the class has been generated. This should always be called before calling {@link
     * #generate(boolean, SourceBuilder)}, in order to prevent multiple generation passes over the same class.
     *
     * @return <code>true</code> if generated, <code>false</code> if not
     */
    public boolean isGenerated() {
        return m_generated;
    }
    
    /**
     * Get the builder for this class.
     *
     * @return builder
     */
    protected ClassBuilder getBuilder() {
        return m_classBuilder;
    }
    
    /**
     * Get the interfaces implemented by this class.
     *
     * @return interface names
     */
    public String[] getInterfaces() {
        return m_classBuilder.getInterfaces();
    }
    
    /**
     * Get imports information.
     *
     * @return imports
     */
    public ImportsTracker getImports() {
        return m_importsTracker;
    }
    
    /**
     * Get the fields defined in this class.
     *
     * @return fields
     */
    public FieldDeclaration[] getFields() {
        return m_classBuilder.getFields();
    }
    
    /**
     * Get the methods defined in this class.
     *
     * @return methods
     */
    public MethodDeclaration[] getMethods() {
        return m_classBuilder.getMethods();
    }

    /**
     * Add import for class. If the requested import doesn't conflict with the current set it's added, otherwise it's
     * ignored.
     * 
     * @param fqname fully qualified class name
     * @return <code>true</code> if added as import
     */
    public boolean addImport(String fqname) {
        return m_importsTracker.addImport(fqname, false);
    }

    /**
     * Get the name to be used for a type. If the type has been imported this returns the short form of the name;
     * otherwise it just returns the fully-qualified name.
     * 
     * @param type fully qualified class name
     * @return name
     */
    public String getTypeName(String type) {
        return m_importsTracker.getName(type);
    }

    /**
     * Add an interface to this class definition.
     *
     * @param interf interface type
     */
    public void addInterface(String interf) {
        m_classBuilder.addInterface(interf);
    }

    /**
     * Add separately-constructed field to this class definition.
     *
     * @param field
     */
    public void addField(FieldDeclaration field) {
        m_classBuilder.addField(field);
    }

    /**
     * Add separately-constructed method declaration to this class definition.
     *
     * @param method
     */
    public void addMethod(MethodDeclaration method) {
        m_classBuilder.addMethod(method);
    }

    /**
     * Add separately-constructed inner class declaration to this class definition.
     *
     * @param type
     */
    public void addType(TypeDeclaration type) {
        m_classBuilder.addType(type);
    }

    /**
     * Get a sorted array of the field names and types defined in this class.
     *
     * @return sorted pairs
     */
    public StringPair[] getSortedFields() {
        return m_classBuilder.getSortedFields();
    }
    
    /**
     * Initialize the class construction. This is a support method for use by subclasses, which handles common setup
     * including superclass generation.
     *
     * @param verbose 
     * @param builder
     * @param root data structure tree root node
     */
    protected void initClass(boolean verbose, ClassBuilder builder, ParentNode root) {
        
        // make sure the builder has been set
        m_classBuilder = builder;
        
        // call decorators for initialization processing
        for (int i = 0; i < m_decorators.length; i++) {
            m_decorators[i].start(this);
        }
        
        // include documentation and/or schema in class comment if enabled
        StringBuffer buff = new StringBuffer();
        GroupItem group = (GroupItem)root.getItem();
        String text = root.getDocumentation();
        if (text != null) {
            buff.append(text);
        }
        if (m_schemaCustom.isSchemaFragmentDocumentation()) {
            text = s_generator.generate(group, m_schemaCustom.isDeleteAnnotations());
            if (text != null) {
                if (buff.length() > 0) {
                    buff.append(SchemaDocumentationGenerator.COMMENT_LEAD_TEXT);
                    buff.append(SchemaDocumentationGenerator.COMMENT_LEAD_TEXT);
                }
                buff.append(text);
            }
        }
        if (buff.length() > 0) {
            m_classBuilder.addSourceComment(buff.toString());
        }
        
        // force generation of superclass, and include all names from that
        // TODO: maintain separate name list for those which are public/protected, vs. private
        if (m_superClass != null && !m_superClass.isPregenerated()) {
            if (!m_superClass.isPregenerated()) {
                ClassHolder superclas = (ClassHolder)m_superClass;
                superclas.getPackage().generate(verbose, m_superClass, m_classBuilder.getAST());
                m_nameSet.addAll(superclas.m_nameSet);
            }
        }
        
        // set flag to avoid re-generation
        m_generated = true;
    }
    
    /**
     * Finish class construction. This is a support method for use by subclasses, which handles common completion
     * processing
     * 
     * @param binding binding definition component for this class
     */
    protected void finishClass(ElementBase binding) {
        for (int i = 0; i < m_decorators.length; i++) {
            m_decorators[i].finish(binding, this);
        }
        String basename = getSuperClass() == null ? getSuperClassName() : getSuperClass().getFullName();
        if (basename != null) {
            m_classBuilder.setSuperclass(basename);
        }
    }
    
    /**
     * Generate any inner classes of this class.
     *
     * @param verbose 
     * @param builder class source file builder
     */
    protected void generateInner(boolean verbose, SourceBuilder builder) {
        for (int i = 0; i < m_inners.size(); i++) {
            ((ClassHolder)m_inners.get(i)).generate(verbose, builder);
        }
    }

    /**
     * Generate this class. Subclasses must implement this method to first do the appropriate setup and then call
     * {@link #initClass(boolean, ClassBuilder, ParentNode)} before doing their own code generation.
     * 
     * @param verbose 
     * @param builder class source file builder
     */
    public abstract void generate(boolean verbose, SourceBuilder builder);

    /**
     * Information for a data structure component of a class definition. The class data structure is defined by a tree
     * of these components, with the interior nodes of the tree representing groupings which may need to be reflected in
     * the actual data representation and/or the constructed binding. Because of this difference in purpose different
     * subclasses are used for the interior nodes vs. the leaf nodes.
     */
    protected static abstract class DataNode
    {
        /** Maximum number of characters of documentation text to include in description. */
        private static final int DESCRIPTION_DOCUMENTATION_LIMIT = 20;

        /** Associated item. */
        private final Item m_item;
        
        /** Parent node (<code>null</code> if none defined, only allowed for root node of tree). */
        private final ParentNode m_parent;
        
        /** Element or attribute name flag. */
        private final boolean m_named;
        
        /** Flag for an optional item. */
        private final boolean m_optional;
        
        /** Flag for an ignored item. */
        private final boolean m_ignored;
        
        /** Flag for a collection item. */
        private final boolean m_collection;
        
        /** Documentation extracted from schema for this data node. */
        private String m_documentation;
        
        /** Value type name. */
        private String m_type;
        
        /** Selection property name for 'if' method construction (only used with group selectors, <code>null</code> if
         no selector for group). */
        private String m_selectPropName;
        
        /** Selection constant name (only used with group selectors, <code>null</code> if no selector for group). */
        private String m_selectConstName;

        /** Property name for value (<code>null</code> if no property). Even interior nodes may have property names, in
         the case of a choice between different alternatives. */
        private String m_propName;
        
        /** Field name for value (<code>null</code> if no field). */
        private String m_fieldName;
        
        /** Get-method name for value (<code>null</code> if no get-method). */
        private String m_getMethodName;
        
        /** Set-method name for value (<code>null</code> if no set-method). */
        private String m_setMethodName;
        
        /** Test-method name for value (<code>null</code> if no test-method). */
        private String m_testMethodName;
        
        /** Flag-method name for value (<code>null</code> if no flag-method). */
        private String m_flagMethodName;
        
        /**
         * Constructor. This automatically links the newly constructed node to the parent node.
         * 
         * @param item associated item
         * @param parent parent node
         */
        public DataNode(Item item, ParentNode parent) {
            m_item = item;
            m_parent = parent;
            AnnotatedBase comp = item.getSchemaComponent();
            int comptype = comp.type();
            m_named = (comptype == SchemaBase.ATTRIBUTE_TYPE || comptype == SchemaBase.ELEMENT_TYPE) &&
                ((INamed)comp).getName() != null;
            Item topmost = item.getTopmost();
            boolean optional = topmost.isOptional();
            if (parent != null && ((GroupItem)parent.getItem()).isInline() && !parent.isNamed()) {
                optional = optional || parent.isOptional();
            }
            m_optional = optional;
            m_ignored = item.isIgnored();
            if (m_ignored) {
                m_type = null;
                m_collection = false;
            } else {
                boolean collection = topmost.isCollection();
                if (item instanceof ValueItem) {
                    
                    // value item will always be a primitive or wrapper value
                    JavaType jtype = ((ValueItem)item).getType();
                    m_type = jtype.getPrimitiveName();
                    if (m_type == null || topmost.isOptional() || topmost.isCollection() || parent.isCollection()) {
                        m_type = jtype.getClassName();
                    }
                    
                } else if (item instanceof ReferenceItem) {
                    
                    // reference item as value will always be a reference to the definition class
                    m_type = ((ReferenceItem)item).getDefinition().getGenerateClass().getFullName();
                    
                } else if (item instanceof AnyItem) {
                    
                    // xs:any handling determines value type
                    switch (item.getComponentExtension().getAnyType()) {
                        
                        case NestingCustomBase.ANY_DISCARD:
                            m_type = null;
                            collection = false;
                            break;
                            
                        case NestingCustomBase.ANY_DOM:
                            m_type = "org.w3c.dom.Element";
                            break;
                            
                        case NestingCustomBase.ANY_MAPPED:
                            m_type = "java.lang.Object";
                            break;
                            
                        default:
                            throw new IllegalStateException("Internal error - unknown xs:any handling");
                        
                    }
                    
                } else if (!((GroupItem)item).isInline()) {
                    
                    // group item as value will always be a reference to the group class
                    m_type = ((GroupItem)item).getGenerateClass().getFullName();
                    
                }
                m_collection = collection;
            }
            if (parent != null) {
                parent.addChild(this);
            }
        }
        
        /**
         * Adjust name based on group nesting.
         * TODO: needs switch to control
         */
//        public void adjustName() {
//            if (!m_item.isFixedName() && m_container != null) {
//                String prefix = m_container.getPrefix();
//                if (prefix != null) {
//                    m_item.setName(prefix + NameConverter.toNameWord(m_item.getEffectiveName()));
//                }
//            }
//        }

        /**
         * Get associated item.
         *
         * @return item
         */
        public Item getItem() {
            return m_item;
        }
        
        /**
         * Get the associated schema component.
         *
         * @return component
         */
        public AnnotatedBase getSchemaComponent() {
            return m_item.getSchemaComponent();
        }

        /**
         * Get parent node.
         *
         * @return parent
         */
        public ParentNode getParent() {
            return m_parent;
        }
        
        /**
         * Check if this is an interior node. This method is overridden by each subclass to return the appropriate
         * result.
         *
         * @return <code>true</code> if node with children, <code>false</code> if not
         */
        public abstract boolean isInterior();

        /**
         * Check if value is ignored.
         * 
         * @return ignored
         */
        public boolean isIgnored() {
            return m_ignored;
        }

        /**
         * Check if value is optional.
         * 
         * @return optional
         */
        public boolean isOptional() {
            return m_optional;
        }

        /**
         * Check if a collection value.
         * 
         * @return <code>true</code> if collection
         */
        public boolean isCollection() {
            return m_collection;
        }
        
        /**
         * Check if an xs:list value.
         *
         * @return <code>true</code> if list
         */
        public boolean isList() {
            return m_item.getSchemaComponent().type() == SchemaBase.LIST_TYPE;
        }
        
        /**
         * Check if an xs:any value.
         *
         * @return <code>true</code> if any
         */
        public boolean isAny() {
            return m_item.getSchemaComponent().type() == SchemaBase.ANY_TYPE;
        }
        
        /**
         * Check if a name (element or attribute) is associated with this node.
         *
         * @return <code>true</code> if named
         */
        public boolean isNamed() {
            return m_named;
        }
        
        /**
         * Get the name associated with a node.
         *
         * @return name, or <code>null</code> if none
         */
        public QName getQName() {
            if (m_named) {
                return ((INamed)m_item.getSchemaComponent()).getQName();
            } else {
                return null;
            }
        }
        
        /**
         * Check if a reference (element or attribute) is associated with this node.
         *
         * @return <code>true</code> if named
         */
        public boolean isReference() {
            AnnotatedBase comp = m_item.getSchemaComponent();
            return comp instanceof IReference && ((IReference)comp).getRef() != null;
        }
        
        /**
         * Get the reference name associated with a node.
         *
         * @return reference name, or <code>null</code> if none
         */
        public QName getReferenceQName() {
            AnnotatedBase comp = m_item.getSchemaComponent();
            if (comp instanceof IReference) {
                return ((IReference)comp).getRef();
            } else {
                return null;
            }
        }

        /**
         * Get schema documentation for this node.
         *
         * @return documentation
         */
        public String getDocumentation() {
            return m_documentation;
        }

        /**
         * Set schema documentation for this node.
         *
         * @param text
         */
        public void setDocumentation(String text) {
            m_documentation = text;
        }

        /**
         * Get the value type name.
         *
         * @return type (<code>null</code> if no type associated with value, only on group)
         */
        public String getType() {
            return m_type;
        }
        
        /**
         * Get the value type name in binding form. For an object type, this differs from the standard fully-qualified
         * name in that it uses '$' rather than '.' to delimit inner class names.
         *
         * @return binding type (<code>null</code> if no type associated with value, only on group)
         */
        public String getBindingType() {
            if (m_item instanceof ReferenceItem) {
                return ((ReferenceItem)m_item).getDefinition().getGenerateClass().getBindingName();
            } else {
                if (m_item instanceof GroupItem) {
                    TypeData genclass = ((GroupItem)m_item).getGenerateClass();
                    if (genclass != null) {
                        return genclass.getBindingName();
                    }
                }
                return m_type;
            }
        }

        /**
         * Get selection property name (used for 'if' method generation). This is only used with group selectors, and is
         * <code>null</code> if the containing group does not use a selector.
         *
         * @return name (<code>null</code> if no selector for group)
         */
        public String getSelectPropName() {
            return m_selectPropName;
        }

        /**
         * Set selection property name (used for 'if' method generation). This is only used with group selectors.
         *
         * @param name (<code>null</code> if no selector for group)
         */
        public void setSelectPropName(String name) {
            m_selectPropName = name;
        }

        /**
         * Get selection constant name. This is only used with group selectors, and is <code>null</code> if the
         * containing group does not use a selector.
         *
         * @return name (<code>null</code> if no selector for group)
         */
        public String getSelectConstName() {
            return m_selectConstName;
        }

        /**
         * Set selection constant name. This is only used with group selectors.
         *
         * @param name (<code>null</code> if no selector for group)
         */
        public void setSelectConstName(String name) {
            m_selectConstName = name;
        }

        /**
         * Get property name for value.
         *
         * @return name (<code>null</code> if none)
         */
        public String getPropName() {
            return m_propName;
        }

        /**
         * Set property name for value.
         *
         * @param name name (<code>null</code> if none)
         */
        public void setPropName(String name) {
            m_propName = name;
        }
        
        /**
         * Get field name used for value.
         *
         * @return name (<code>null</code> if no field)
         */
        public String getFieldName() {
            return m_fieldName;
        }
        
        /**
         * Set field name used for value.
         *
         * @param name (<code>null</code> if no field)
         */
        public void setFieldName(String name) {
            m_fieldName = name;
        }
        
        /**
         * Get get-method name used for value.
         *
         * @return name (<code>null</code> if no get-method)
         */
        public String getGetMethodName() {
            return m_getMethodName;
        }
        
        /**
         * Set get-method name used for value.
         *
         * @param name (<code>null</code> if no get-method)
         */
        public void setGetMethodName(String name) {
            m_getMethodName = name;
        }
        
        /**
         * Get set-method name used for value.
         *
         * @return name (<code>null</code> if no set-method)
         */
        public String getSetMethodName() {
            return m_setMethodName;
        }
        
        /**
         * Set set-method name used for value.
         *
         * @param name (<code>null</code> if no set-method)
         */
        public void setSetMethodName(String name) {
            m_setMethodName = name;
        }
        
        /**
         * Get test-method name used for value.
         *
         * @return name (<code>null</code> if no set-method)
         */
        public String getTestMethodName() {
            return m_testMethodName;
        }

        /**
         * Set test-method name used for value.
         *
         * @param name (<code>null</code> if no set-method)
         */
        public void setTestMethodName(String name) {
            m_testMethodName = name;
        }

        /**
         * Get flag-method name used for value.
         *
         * @return name (<code>null</code> if no set-method)
         */
        public String getFlagMethodName() {
            return m_flagMethodName;
        }

        /**
         * Set flag-method name used for value.
         *
         * @param name (<code>null</code> if no flag-method)
         */
        public void setFlagMethodName(String name) {
            m_flagMethodName = name;
        }

        /**
         * Generate the node description.
         *
         * @param depth current nesting depth
         * @return description
         */
        public abstract String describe(int depth);

        /**
         * Append documentation text to description. This appends a potentially truncated version of the documentation
         * for the component to the description text under construction, also trimming whitespace and replacing line
         * breaks with pipe characters to keep the description text to a single line.
         *
         * @param buff
         */
        protected void appendDocText(StringBuffer buff) {
            String doctext = getDocumentation();
            if (doctext != null) {
                buff.append(" (");
                if (doctext.length() > DESCRIPTION_DOCUMENTATION_LIMIT) {
                    doctext = doctext.substring(0, DESCRIPTION_DOCUMENTATION_LIMIT) + "...";
                }
                doctext = doctext.trim().replace('\n', '|').replace('\r', '|');
                buff.append(doctext);
                buff.append(')');
            }
        }

        /**
         * Append selection constant text to description, if selection constant defined.
         *
         * @param buff
         */
        protected void appendSelectConstText(StringBuffer buff) {
            if (getSelectConstName() != null) {
                buff.append(" (selection ");
                buff.append(getSelectConstName());
                buff.append(')');
            }
        }
    }
    
    /**
     * Information for a leaf node of the data structure tree.
     */
    protected static class LeafNode extends DataNode
    {
        /**
         * Constructor. This automatically links to the containing node.
         * 
         * @param item
         * @param parent
         */
        public LeafNode(Item item, ParentNode parent) {
            super(item, parent);
        }
        
        /**
         * Check if this is an interior node (always <code>false</code>).
         *
         * @return <code>false</code> for value component
         */
        public boolean isInterior() {
            return false;
        }

        /**
         * Generate the node description.
         *
         * @param depth current nesting depth
         * @return description
         */
        public String describe(int depth) {
            StringBuffer buff = new StringBuffer(depth + 40);
            buff.append(SchemaUtils.getIndentation(depth));
            if (isOptional()) {
                buff.append("optional ");
            }
            if (isIgnored()) {
                buff.append("ignored ");
            }
            if (isCollection()) {
                buff.append("collection ");
            }
            buff.append("leaf ");
            buff.append(getItem().getName());
            if (getFieldName() != null) {
                buff.append(" field ");
                buff.append(getFieldName());
                buff.append(" of type ");
                buff.append(getType());
            }
            buff.append(" for schema component ");
            buff.append(SchemaUtils.describeComponent(getItem().getSchemaComponent()));
            appendSelectConstText(buff);
            appendDocText(buff);
            return buff.toString();
        }
    }
    
    /**
     * Information for an interior node of the data structure tree. Depending on the type of the associated schema
     * component a selector field may be used to track which of a set of alternatives is actually present.
     */
    protected static class ParentNode extends DataNode
    {
        /** Type of selector handling needed for group. Selector fields are used with mutually exclusive
         alternatives. The values are based on {@link NestingCustomBase#SELECTION_UNCHECKED} and alternatives. */
        private final int m_selectorType;
        
        /** Selection state exposed to user flag. */
        private final boolean m_selectorExposed;
        
        /** Prefix for all contained value names (<code>null</code> if none used). */
        private final String m_prefix;
        
        /** Values in this group. */
        private final ArrayList m_values;
        
        /** Flag for a collection node that wraps some other structure (rather than a simple leaf node). */
        private boolean m_complexCollection;
        
        /** Field name for selector  */
        private String m_selectField;
        
        /** Method name for selection check method. */
        private String m_selectCheckMethod;
        
        /** Method name for selection set method. */
        private String m_selectSetMethod;
        
        /** Flag for selection requirement checked. */
        private boolean m_selectChecked;
        
        /** Flag for selection needed. */
        private boolean m_selectNeeded;
        
        /**
         * Constructor. This derives the prefix used for all contained value names by appending the class name set for
         * this group to the prefix used for the containing group.
         * 
         * @param group associated item group
         * @param parent containing node
         */
        public ParentNode(GroupItem group, ParentNode parent) {
            super(group, parent);
            // TODO: this really should be a separate pass to handle fixed class names
            String prefix = NameUtils.toNameWord(group.getEffectiveClassName());
            m_prefix = prefix;
            int select = NestingCustomBase.SELECTION_UNCHECKED;
            boolean exposed = false;
            ComponentExtension extension = group.getComponentExtension();
            switch (group.getSchemaComponent().type()) {
                case SchemaBase.CHOICE_TYPE:
                    select = extension.getChoiceType();
                    exposed = extension.isChoiceExposed();
                    break;
                case SchemaBase.UNION_TYPE:
                    select = extension.getUnionType();
                    exposed = extension.isUnionExposed();
                    break;
            }
            if (select != NestingCustomBase.SELECTION_UNCHECKED) {
                
                // no selector needed if handled by parent, or if no choices
                if ((parent != null && parent.getSchemaComponent() == group.getSchemaComponent()) ||
                    group.getChildCount() <= 1) {
                    select = NestingCustomBase.SELECTION_UNCHECKED;
                    exposed = false;
                }
            }
            m_selectorType = select;
            m_selectorExposed = exposed;
            m_values = new ArrayList();
        }
        
        /**
         * Check if this is an interior node (always <code>true</code>).
         *
         * @return <code>true</code> for structure component
         */
        public boolean isInterior() {
            return true;
        }

        /**
         * Check if a selector field is required for this group.
         *
         * @return selector
         */
        public boolean isSelectorNeeded() {
            if (!m_selectChecked) {
                if (m_selectorType != NestingCustomBase.SELECTION_UNCHECKED) {
                    
                    // before reporting selector needed, make sure at least once child is going to be present
                    for (int i = 0; i < m_values.size(); i++) {
                        DataNode node = (DataNode)m_values.get(i);
                        if (!node.isIgnored()) {
                            m_selectNeeded = true;
                            break;
                        }
                    }
                    
                }
                m_selectChecked = true;
            }
            return m_selectNeeded;
        }

        /**
         * Check if a selector field may be required for this group.
         *
         * @return selector
         */
        public boolean isSelectorType() {
            return m_selectorType != NestingCustomBase.SELECTION_UNCHECKED;
        }

        /**
         * Get the selector type.
         *
         * @return type
         */
        public int getSelectorType() {
            return m_selectorType;
        }

        /**
         * Check if selector state should be exposed to user.
         *
         * @return selector
         */
        public boolean isSelectorExposed() {
            return m_selectorExposed;
        }
        
        /**
         * Adjust name based on group nesting. This has special handling for the case of &lt;sequence> compositors,
         * substituting the name of the first value in the sequence for the value name if a fixed name has not been
         * assigned to the sequence.
         */
        public void adjustName() {
            Item item = getItem();
            if (!item.isFixedName()) {
                if (item.getSchemaComponent().type() == SchemaBase.SEQUENCE_TYPE) {
                    if (m_values.size() > 0) {
                        Item childitem = ((DataNode)m_values.get(0)).getItem();
                        if (item != childitem) {
                            item.setName(childitem.getEffectiveName());
                        }
                    }
                }
            }
        }
        
        /**
         * Get prefix for value names in group.
         *
         * @return prefix (<code>null</code> if none used)
         */
        public String getPrefix() {
            return m_prefix;
        }

        /**
         * Add a child node (which may be another parent) to this parent. This method is normally only used by the
         * superclass, when creating a new instance. The instance must be fully initialized before it is added.
         *
         * @param value
         */
        protected void addChild(DataNode value) {
            m_values.add(value);
            if (isCollection()) {
                
                // make this a complex collection if the added child is something more than a simple value
                if (value.getSchemaComponent() != getSchemaComponent() &&
                    value.isInterior() || value.isCollection()) {
                    m_complexCollection = true;
                }
            }
        }

        /**
         * Get child nodes of this parent. The returned list is "live", but should never be modified.
         *
         * @return values
         */
        public ArrayList getChildren() {
            return m_values;
        }

        /**
         * Check if this is a collection that wraps some other structure (rather than a simple leaf node).
         *
         * @return <code>true</code> if wrapper collection
         */
        public boolean isComplexCollection() {
            return m_complexCollection;
        }

        /**
         * Get selector field name.
         *
         * @return name (<code>null</code> if no selector for group)
         */
        public String getSelectField() {
            return m_selectField;
        }

        /**
         * Set selector field name.
         *
         * @param name (<code>null</code> if no selector for group)
         */
        public void setSelectField(String name) {
            m_selectField = name;
        }

        /**
         * Get selector set method name.
         *
         * @return name (<code>null</code> if no selector set method for group)
         */
        public String getSelectSetMethod() {
            return m_selectSetMethod;
        }

        /**
         * Set selector set method name.
         *
         * @param name (<code>null</code> if no selector set method for group)
         */
        public void setSelectSetMethod(String name) {
            m_selectSetMethod = name;
        }

        /**
         * Get selector check method name.
         *
         * @return name (<code>null</code> if no selector check method for group)
         */
        public String getSelectCheckMethod() {
            return m_selectCheckMethod;
        }

        /**
         * Set selector check method name.
         *
         * @param name (<code>null</code> if no selector check method for group)
         */
        public void setSelectCheckMethod(String name) {
            m_selectCheckMethod = name;
        }
        
        /**
         * Generate the subtree description.
         *
         * @param depth current nesting depth
         * @return description
         */
        public String describe(int depth) {
            StringBuffer buff = new StringBuffer(depth + 40);
            buff.append(SchemaUtils.getIndentation(depth));
            if (isOptional()) {
                buff.append("optional ");
            }
            if (isIgnored()) {
                buff.append("ignored ");
            }
            if (isCollection()) {
                if (isComplexCollection()) {
                    buff.append("complex ");
                }
                buff.append("collection ");
            }
            buff.append("node ");
            buff.append(getItem().getName());
            buff.append(" for schema component ");
            buff.append(SchemaUtils.describeComponent(getItem().getSchemaComponent()));
            if (getFieldName() != null) {
                buff.append(" field ");
                buff.append(getFieldName());
                buff.append(" of type ");
                buff.append(getType());
            }
            if (m_selectorType != NestingCustomBase.SELECTION_UNCHECKED) {
                buff.append(" (with selector)");
            }
            appendSelectConstText(buff);
            appendDocText(buff);
            for (int i = 0; i < m_values.size(); i++) {
                buff.append('\n');
                DataNode value = (DataNode)m_values.get(i);
                buff.append(value.describe(depth+1));
            }
            return buff.toString();
        }
    }
    
    /**
     * Builder for an unmarshalling presence test method. This is based around the structure of a schema sequence, which
     * may have any number of mixed required and optional elements. The presence test checks if the current element name
     * matches one of those in the sequence up to and including the first <i>required</i> element name.
     */
//    private class UnmarshalPresenceTestBuilder
//    {
//        /** Builder for the logical or expression. */
//        private final InfixExpressionBuilder m_expression;
//        
//        /** Implicit namespace in use at point of evaluation. */
//        private final String m_implicitNamespace;
//        
//        /** Expression completed flag (set when a required element is added to expression). */
//        private boolean m_complete;
//        
//        /**
//         * Constructor.
//         * 
//         * @param implns implicit namespace in use at point of evaluation
//         */
//        public UnmarshalPresenceTestBuilder(String implns) {
//            m_expression = m_classBuilder.buildInfix(Operator.OR);
//            m_implicitNamespace = implns;
//        }
//        
//        /**
//         * Add an element to the presence test expression. If the expression is already complete (with a required
//         * element found), the call is ignored.
//         *
//         * @param ns
//         * @param name
//         * @param required
//         */
//        public void addElement(String ns, String name, boolean required) {
//            if (!m_complete) {
//                InvocationBuilder call = m_classBuilder.createNormalMethodCall(READER_VARNAME,
//                    READER_CHECK_START_TAG_METHOD);
//                if ((m_implicitNamespace == null && ns != null) ||
//                    (m_implicitNamespace != null && !m_implicitNamespace.equals(ns))) {
//                    call.addStringLiteralOperand(ns);
//                }
//                call.addStringLiteralOperand(name);
//                m_expression.addOperand(call);
//                if (required) {
//                    m_complete = true;
//                }
//            }
//        }
//        
//        /**
//         * Get the expression.
//         *
//         * @return expression
//         */
//        public InfixExpressionBuilder getExpression() {
//            return m_expression;
//        }
//    }
}
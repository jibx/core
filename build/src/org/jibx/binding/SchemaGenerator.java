/*
Copyright (c) 2004-2008, Dennis M. Sosnoski
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

package org.jibx.binding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.CollectionElement;
import org.jibx.binding.model.ContainerElementBase;
import org.jibx.binding.model.DefinitionContext;
import org.jibx.binding.model.ElementBase;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.NestingAttributes;
import org.jibx.binding.model.NestingElementBase;
import org.jibx.binding.model.StructureElement;
import org.jibx.binding.model.StructureElementBase;
import org.jibx.binding.model.TemplateElementBase;
import org.jibx.binding.model.ValidationContext;
import org.jibx.binding.model.ValidationProblem;
import org.jibx.binding.model.ValueElement;
import org.jibx.binding.util.ObjectStack;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.ValidationException;
import org.jibx.util.IClassLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Binding generator. This loads the specified input classes and processes them
 * to generate a default binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class SchemaGenerator
{
    /** Generator version. */
    private static String CURRENT_VERSION = "0.4";
    
    /** Schema namespace URI. */
    private static final String XSD_URI = "http://www.w3.org/2001/XMLSchema";
    
    /** Fixed XML namespace. */
    public static final String XML_URI = "http://www.w3.org/XML/1998/namespace";
    
    /** Fixed XML namespace namespace. */
    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    
    /** Set of object types mapped to schema types. */
    private static HashMap s_objectTypeMap = new HashMap();
    
    static {
        s_objectTypeMap.put("java.lang.Boolean", "xsd:boolean");
        s_objectTypeMap.put("java.lang.Byte", "xsd:byte");
        s_objectTypeMap.put("java.lang.Character", "xsd:unsignedInt");
        s_objectTypeMap.put("java.lang.Double", "xsd:double");
        s_objectTypeMap.put("java.lang.Float", "xsd:float");
        s_objectTypeMap.put("java.lang.Integer", "xsd:int");
        s_objectTypeMap.put("java.lang.Long", "xsd:long");
        s_objectTypeMap.put("java.lang.Short", "xsd:short");
        s_objectTypeMap.put("java.math.BigDecimal", "xsd:decimal");
        s_objectTypeMap.put("java.math.BigInteger", "xsd:integer");
//#!j2me{
        s_objectTypeMap.put("java.sql.Date", "xsd:date");
        s_objectTypeMap.put("java.sql.Time", "xsd:time");
        s_objectTypeMap.put("java.sql.Timestamp", "xsd:dateTime");
//#j2me}
        s_objectTypeMap.put("java.util.Date", "xsd:dateTime");
        s_objectTypeMap.put("byte[]", "xsd:base64");
    }
    
    /** Set of primitive types mapped to schema types. */
    private static HashMap s_primitiveTypeMap = new HashMap();
    
    static {
        s_primitiveTypeMap.put("boolean", "xsd:boolean");
        s_primitiveTypeMap.put("byte", "xsd:byte");
        s_primitiveTypeMap.put("char", "xsd:unsignedInt");
        s_primitiveTypeMap.put("double", "xsd:double");
        s_primitiveTypeMap.put("float", "xsd:float");
        s_primitiveTypeMap.put("int", "xsd:int");
        s_primitiveTypeMap.put("long", "xsd:long");
        s_primitiveTypeMap.put("short", "xsd:short");
    }
    
    /** Show verbose output flag. */
    private boolean m_verbose;
    
    /** Use qualified elements default in schema flag. */
    private boolean m_isElementQualified;
    
    /** Use qualified attributes default in schema flag. */
    private boolean m_isAttributeQualified;
    
    /** Indentation sequence per level of nesting. */
    private String m_indentSequence;
    
    /** Map from namespaces to schemas. */
    private HashMap m_schemaMap;
    
    /** Locator for finding classes referenced by binding. */
    private IClassLocator m_classLocator;
    
    /** Document used for all schema definitions. */
    private Document m_document;
    
    /** Stack of structure definitions in progress (used to detect cycles). */
    private ObjectStack m_structureStack;
    
    /**
     * Constructor with only paths supplied. This just initializes all other
     * options disabled.
     * 
     * @param paths class paths to be checked for classes referenced by bindings
     */
    public SchemaGenerator(ArrayList paths) {
        m_structureStack = new ObjectStack();
        m_schemaMap = new HashMap();
        
        // set paths to be used for loading referenced classes
        String[] parray = (String[])paths.toArray(new String[paths.size()]);
        ClassCache.setPaths(parray);
        ClassFile.setPaths(parray);
        
        // set class locator
        m_classLocator = new ClassCache.ClassCacheLocator();
        try {
            
            // create the document used for all schemas
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            m_document = dbf.newDocumentBuilder().newDocument();
            
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Parser configuration error " +
                e.getMessage());
        } catch (FactoryConfigurationError e) {
            throw new IllegalStateException("Factory configuration error " +
                e.getMessage());
        }
    }
    
    /**
     * Constructor with settings specified.
     *
     * @param verbose report binding details and results
     * @param equal use element form default qualified flag
     * @param aqual use attribute form default qualified flag
     * @param paths class paths to be checked for classes referenced by bindings
     */
    public SchemaGenerator(boolean verbose, boolean equal, boolean aqual,
        ArrayList paths) {
        this(paths);
        m_verbose = verbose;
        m_isElementQualified = equal;
        m_isAttributeQualified = aqual;
        m_indentSequence = "  ";
    }

    /**
     * Set control flag for verbose processing reports.
     * 
     * @param verbose report verbose information in processing bindings flag
     */
    public void setVerbose(boolean verbose) {
        m_verbose = verbose;
    }

    /**
     * Set control flag for element qualified default schema.
     * 
     * @param qual element qualified default schemas flag
     */
    public void setElementQualified(boolean qual) {
        m_isElementQualified = qual;
    }

    /**
     * Set control flag for attribute qualified default schema.
     * 
     * @param qual attribute qualified default schemas flag
     */
    public void setAttributeQualified(boolean qual) {
        m_isAttributeQualified = qual;
    }

    /**
     * Get array of generated schemas.
     * 
     * @return array of schema elements
     */
    public Element[] getSchemas() {
        Element[] schemas = new Element[m_schemaMap.size()];
        int fill = 0;
        for (Iterator iter = m_schemaMap.values().iterator(); iter.hasNext();) {
            schemas[fill++] = (Element)iter.next();
        }
        return schemas;
    }
    
    /**
     * Generate indentation to proper depth for current item. This creates the
     * indentation text and appends it to the supplied parent. The generated
     * indentation is appropriate for the close tag of the parent element; if
     * a child element is to be added following this indentation it needs to
     * use an additional leading indent.
     *
     * @param parent element to contain indented child item
     */
    private void indentForClose(Element parent) {
        StringBuffer buff = new StringBuffer(20);
        buff.append('\n');
        Element ancestor = parent;
        boolean count = false;
        while (ancestor != null) {
            if (count) {
                buff.append(m_indentSequence);
            }
            ancestor = (Element)ancestor.getParentNode();
            count = true;
        }
        parent.appendChild(m_document.createTextNode(buff.toString()));
    }
    
    /**
     * Add comment with appropriate indentation.
     *
     * @param parent element to contain indented child item
     * @param text comment text
     */
    private void addComment(Element parent, String text) {
        if (parent.getChildNodes().getLength() == 0) {
            indentForClose(parent);
        }
        parent.appendChild(m_document.createTextNode(m_indentSequence));
        parent.appendChild(m_document.createComment(text));
        indentForClose(parent);
    }
    
    /**
     * Add child element with appropriate indentation. This generates and
     * returns the child element after adding it to the supplied parent,
     * allowing further modification of the new child element.
     *
     * @param parent element to contain indented child item
     * @param name child element name
     */
    private Element addChildElement(Element parent, String name) {
        if (parent.getChildNodes().getLength() == 0) {
            indentForClose(parent);
        }
        parent.appendChild(m_document.createTextNode(m_indentSequence));
        Element element = m_document.createElementNS(XSD_URI, name);
        element.setPrefix(parent.getPrefix());
        parent.appendChild(element);
        indentForClose(parent);
        return element;
    }
    
    /**
     * Get innermost containing definition context.
     * 
     * @return innermost definition context containing this element
     */
    public DefinitionContext getDefinitions() {
        int index = 0;
        while (index < m_structureStack.size()) {
            NestingElementBase nest =
                (NestingElementBase)m_structureStack.peek(index++);
            if (nest.getDefinitions() != null) {
                return nest.getDefinitions();
            }
        }
        throw new IllegalStateException
            ("Internal error: no definition context");
    }

    /**
     * Process a structure component (structure or collection element) with no
     * name and no child components. This adds the appropriate type of element
     * or any definition to the container schema element.
     * 
     * @param comp structure component to be processed
     * @param egroup schema element to contain element definitions
     * @param agroup schema element to contain attribute definitions
     */
    private void defineEmptyStructureComponent(StructureElementBase comp,
        Element egroup, Element agroup) {
        NestingElementBase parent =
            (NestingElementBase)m_structureStack.peek(0);
        boolean only = parent.children().size() == 1;
        if (comp.type() == ElementBase.COLLECTION_ELEMENT) {
            
            // collection may define type or not
            CollectionElement collection = (CollectionElement)comp;
            String itype = collection.getItemTypeClass().getName();
            DefinitionContext dctx = getDefinitions();
            TemplateElementBase templ = dctx.getSpecificTemplate(itype);
            Element element = null;
            if (! (templ instanceof MappingElement)) {
                if (only) {
                    addComment(egroup, " Replace \"any\" with details of " +
                        "content to complete schema ");
                    element = addChildElement(egroup, "any");
                } else {
                    addComment(egroup,
                        " No mapping for items of collection at " +
                        ValidationException.describe(collection) + " ");
                    addComment(egroup,
                        " Fill in details of content to complete schema ");
                }
            } else {
                element = addChildElement(egroup, "element");
                String name = ((MappingElementBase)templ).getName();
                if (element.getPrefix() == null) {
                    name = "tns:" + name;
                }
                element.setAttribute("ref", name);
            }
            if (element != null) {
                element.setAttribute("minOccurs", "0");
                element.setAttribute("maxOccurs", "unbounded");
            }
            
        } else {
            
            // check for reference to a mapped class
            StructureElement structure = (StructureElement)comp;
            TemplateElementBase templ = structure.getEffectiveMapping();
            if (! (templ instanceof MappingElement)) {
                
                // unknown content, leave it to user to fill in details
                if (only) {
                    addComment(egroup, " Replace \"any\" with details of " +
                        "content to complete schema ");
                    addChildElement(egroup, "any");
                } else {
                    addComment(egroup, " No mapping for structure at " +
                        ValidationException.describe(structure) + " ");
                    addComment(egroup,
                        " Fill in details of content here to complete schema ");
                }
                
            } else {
                MappingElementBase mapping = (MappingElementBase)templ;
                if (mapping.isAbstract()) {
                    
                    // check name to be used for instance of type
                    String ename = structure.getName();
                    if (ename == null) {
                        ename = mapping.getName();
                    }
                    if (ename == null) {
                        
                        // no schema equivalent, embed definition directly
                        addComment(egroup, "No schema representation for " +
                            "directly-embedded type, inlining definition");
                        addComment(egroup, "Add element name to structure at " +
                            ValidationException.describe(structure) +
                            " to avoid inlining");
                        defineList(mapping.children(), egroup, agroup, false);
                        
                    } else {
                        
                        // handle abstract mapping element as reference to type
                        Element element = addChildElement(egroup, "element");
                        String tname = simpleClassName(mapping.getClassName());
                        if (element.getPrefix() == null) {
                            tname = "tns:" + tname;
                        }
                        element.setAttribute("type", tname);
                        String name = structure.getName();
                        if (name == null) {
                            name = mapping.getName();
                        }
                        element.setAttribute("name", name);
                        if (structure.isOptional()) {
                            element.setAttribute("minOccurs", "0");
                        }
                    }
                    
                } else {
                    
                    // concrete mapping, check for name overridden
                    String sname = structure.getName();
                    String mname = mapping.getName();
                    if (sname != null && !sname.equals(mname)) {
                        
                        // inline definition for overridden name
                        addComment(egroup, "No schema representation for " +
                            "element reference with different name, inlining " +
                            "definition");
                        addComment(egroup,
                            "Remove name on mapping reference at " +
                            ValidationException.describe(structure) +
                            " to avoid inlining");
                        defineList(mapping.children(), egroup, agroup, false);
                        
                    } else {
                        
                        // use element reference for concrete mapping
                        Element element = addChildElement(egroup, "element");
                        String tname = simpleClassName(mapping.getClassName());
                        if (element.getPrefix() == null) {
                            tname = "tns:" + tname;
                        }
                        element.setAttribute("ref", tname);
                        if (structure.isOptional()) {
                            element.setAttribute("minOccurs", "0");
                        }
                    }
                    
                }
            }
            
        }
    }

    /**
     * Process a structure component (structure or collection element) within a
     * list of child components. This adds the appropriate type of element or
     * any definition to the container, if necessary calling other methods for
     * recursive handling of nested child components.
     * 
     * @param comp structure component to be processed
     * @param egroup schema element to contain element definitions
     * @param agroup schema element to contain attribute definitions
     * @param mult allow any number of occurrences of components flag
     */
    private void defineStructureComponent(StructureElementBase comp,
        Element egroup, Element agroup, boolean mult) {
        
        // check for element defined by binding component
        if (comp.getName() != null) {
            
            // create basic element definition for name
            Element element = addChildElement(egroup, "element");
            element.setAttribute("name", comp.getName());
            if (mult) {
                element.setAttribute("minOccurs", "0");
                element.setAttribute("maxOccurs", "unbounded");
            } else if (comp.isOptional()) {
                element.setAttribute("minOccurs", "0");
            }
            
            // check for children present
            if (comp.children().size() > 0) {
                defineNestedStructure(comp, element);
            } else {
                
                // nest complex type definition for actual content
                Element type = addChildElement(element, "complexType");
                Element seq = addChildElement(type, "sequence");
                
                // process the content description
                defineEmptyStructureComponent(comp, seq, type);
            }
            
        } else if (comp.children().size() > 0) {
            
            // handle child components with recursive call
            boolean coll = comp.type() == ElementBase.COLLECTION_ELEMENT;
            m_structureStack.push(comp);
            defineList(comp.children(), egroup, agroup, coll);
            m_structureStack.pop();
            
        } else {
            
            // handle empty structure definition inline
            defineEmptyStructureComponent(comp, egroup, agroup);
        }
    }

    /**
     * Create the schema definition list for a binding component list. This
     * builds the sequence of elements and attributes defined by the binding
     * components, including nested complex types for elements with structure.
     * 
     * @param comps binding component list
     * @param egroup schema element to contain element definitions
     * @param agroup schema element to contain attribute definitions
     * @param mult allow any number of occurrences of components flag
     */
    private void defineList(ArrayList comps, Element egroup, Element agroup,
        boolean mult) {
        
        // handle all nested elements of container
        for (int i = 0; i < comps.size(); i++) {
            ElementBase child = (ElementBase)comps.get(i);
            switch (child.type()) {
                
                case ElementBase.COLLECTION_ELEMENT:
                case ElementBase.STRUCTURE_ELEMENT:
                {
                    defineStructureComponent((StructureElementBase)child,
                        egroup, agroup, mult);
                    break;
                }
                    
                case ElementBase.MAPPING_ELEMENT:
                {
                    // nested mapping definitions not handled
                    System.err.println("Error: nested mapping not supported " +
                        "(class " + ((MappingElementBase)child).getClassName() +
                        ")");
                    break;
                }
                    
                case ElementBase.TEMPLATE_ELEMENT:
                {
                    // templates to be added once usable in binding
                    System.err.println
                        ("Error: template component not yet supported");
                    break;
                }
                    
                case ElementBase.VALUE_ELEMENT:
                {
                    // get type information for value
                    ValueElement value = (ValueElement)child;
                    String tname = value.getType().getName();
                    String stype = (String)s_primitiveTypeMap.get(tname);
                    if (stype == null) {
                        stype = (String)s_objectTypeMap.get(tname);
                        if (stype == null) {
                            stype = "xsd:string";
                        }
                    }
                    
                    // build schema element or attribute for value
                    Element element;
                    int style = value.getStyle();
                    if (style == NestingAttributes.ATTRIBUTE_STYLE) {
                        
                        // append attribute as child of type
                        element = addChildElement(agroup, "attribute");
                        if (!value.isOptional()) {
                            element.setAttribute("use", "required");
                        }
                        
                    } else if (style == NestingAttributes.ELEMENT_STYLE) {
                        
                        // append simple element as child of grouping
                        element = addChildElement(egroup, "element");
                        if (mult) {
                            element.setAttribute("minOccurs", "0");
                            element.setAttribute("maxOccurs", "unbounded");
                        } else if (value.isOptional()) {
                            element.setAttribute("minOccurs", "0");
                        }
                        
                    } else {
                        
                        // other types are not currently handled
                        System.err.println("Error: value type " +
                            value.getEffectiveStyleName() + " not supported");
                        break;
                        
                    }
                    
                    // set common attributes on definition
                    element.setAttribute("name", value.getName());
                    element.setAttribute("type", stype);
                    break;
                }
            }
        }
    }

    /**
     * Create the schema definition for a nested structure. This defines a
     * complex type, if necessary calling itself recursively for elements which
     * are themselves complex types. In the special case where the container
     * element is a mapping which extends an abstract base class this generates
     * the complex type as an extension of the base class complex type.
     * 
     * @param container binding definition element containing nested structure
     * @param parent schema element to hold the definition
     * @return constructed complex type
     */
    private Element defineNestedStructure(ContainerElementBase container,
        Element parent) {
        
        // create complex type as holder for definition
        Element type = addChildElement(parent, "complexType");
        
        // check whether ordered or unordered container
        Element group;
        ArrayList childs = container.children();
        if (container.isOrdered()) {
            
            // define content list as sequence
            group = addChildElement(type, "sequence");
            
            // check for mapping which extends another mapping
/*            if (container instanceof MappingElement) {
                MappingElement mapping = (MappingElement)container;
                MappingElement extended = mapping.getExtendsMapping();
                if (extended != null) {
                    
                    // now see if the mapping can extend base complex type
                    boolean extend = false;
                    for (int i = 0; i < childs.size(); i++) {
                        ElementBase child = (ElementBase)childs.get(i);
                        if (child instanceof StructureElement) {
                            StructureElement struct = (StructureElement)child;
                            if (struct.getMapAsMapping() == extended) {
                                if (struct.getName() == null) {
                                    extend = true;
                                    
                                }
                            }
                        }
                        if (child instanceof IComponent) {
                            
                        }
                    }
                }
            }   */
            
        } else {
            group = addChildElement(type, "all");
        }
        
        // handle all nested elements of container
        m_structureStack.push(container);
        defineList(childs, group, type,
            container.type() == ElementBase.COLLECTION_ELEMENT);
        m_structureStack.pop();
        return type;
    }
    
    /**
     * Generate a schema from a binding using supplied classpaths. If the schema
     * for the binding namespace (or default namespace) already exists the
     * definitions from this binding are added to the existing schema; otherwise
     * a new schema is created and added to the collection defined.
     * 
     * @param binding root element of binding
     */
    private void generateSchema(BindingElement binding) {
        
        // process each mapping definition for binding
        m_structureStack.push(binding);
        ArrayList tops = binding.topChildren();
        for (int i = 0; i < tops.size(); i++) {
            ElementBase top = (ElementBase)tops.get(i);
            if (top.type() == ElementBase.MAPPING_ELEMENT) {
                
                // find or create schema for mapped class
                MappingElementBase mapping = (MappingElementBase)top;
                String uri = mapping.getNamespace().getUri();
                Element schema = (Element)m_schemaMap.get(uri);
                if (schema == null) {
                    
                    // build new schema element for this namespace
                    schema = m_document.createElementNS(XSD_URI, "schema");
                    m_schemaMap.put(uri, schema);
                    
                    // set qualification attributes if needed
                    if (m_isElementQualified) {
                        schema.setAttribute("elementFormDefault", "qualified");
                    }
                    if (m_isAttributeQualified) {
                        schema.setAttribute("attributeFormDefault",
                            "qualified");
                    }
                    
                    // add namespace declarations to element
                    if (uri == null) {
                        schema.setPrefix("xsd");
                    } else {
                        schema.setAttribute("targetNamespace", uri);
                        schema.setAttributeNS(XMLNS_URI, "xmlns:tns", uri);
                        schema.setAttributeNS(XMLNS_URI, "xmlns", XSD_URI);
                    }
                    schema.setAttributeNS(XMLNS_URI, "xmlns:xsd", XSD_URI);
                    
                    // add spacing for first child node
                    indentForClose(schema);
                }
                
                // add spacer and comment before actual definition
                indentForClose(schema);
                String cname = mapping.getClassName();
                addComment(schema, " Created from mapping for class " +
                    cname + " ");
                if (mapping.isAbstract()) {
                    
                    // add mapping as global type in binding
                    Element type = defineNestedStructure(mapping, schema);
                    type.setAttribute("name", simpleClassName(cname));
                    
                } else {
                    
                    // add mapping as global element in binding
                    Element element = addChildElement(schema, "element");
                    element.setAttribute("name", mapping.getName());
                    
                    // check type of mapping definition
                    if (mapping.getMarshaller() != null ||
                        mapping.getUnmarshaller() != null) {
                        
                        // use "any" for custom marshaller/unmarshaller
                        Element type = addChildElement(element, "complexType");
                        Element seq = addChildElement(type, "sequence");
                        addComment(seq, " Replace \"any\" with details of " +
                            "content to complete schema ");
                        addChildElement(seq, "any");
                        
                    } else {
                        
                        // use complex type for embedded definition
                        defineNestedStructure(mapping, element);
                        
                    }
                }
            }
        }
        m_structureStack.pop();
    }
    
    /**
     * Process a binding definition for schema generation. This first validates
     * the binding definition, and if it is valid then handles schema generation
     * from the binding.
     * 
     * @param binding root element of binding
     * @exception JiBXException if error in generating the schema
     */
    public void generate(BindingElement binding) throws JiBXException {
        
        // validate the binding definition
        ValidationContext vctx = new ValidationContext(m_classLocator);
        binding.runValidation(vctx);
        boolean usable = true;
        if (vctx.getProblems().size() > 0) {
            
            // report problems found
            System.err.println("Problems found in binding " +
                binding.getName());
            ArrayList probs = vctx.getProblems();
            for (int i = 0; i < probs.size(); i++) {
                ValidationProblem prob = (ValidationProblem)probs.get(i);
                System.err.println(prob.getDescription());
                if (prob.getSeverity() > ValidationProblem.WARNING_LEVEL) {
                    usable = false;
                }
            }
        }
        
        // check if binding usable for schema generation
        if (usable) {
            generateSchema(binding);
        } else {
            System.err.println
                ("Binding validation errors prevent schema generation");
            System.exit(1);
        }
    }

    /**
     * Get simple class name.
     * 
     * @param cname class name with full package specification
     * @return class name only
     */
    private String simpleClassName(String cname) {
        int split = cname.lastIndexOf('.');
        if (split >= 0) {
            cname = cname.substring(split+1);
        }
        return cname;
    }

    /**
     * Main method for running compiler as application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                
                // check for various flags set
                boolean verbose = false;
                boolean edflt = true;
                boolean adflt = false;
                ArrayList paths = new ArrayList();
                int offset = 0;
                for (; offset < args.length; offset++) {
                    String arg = args[offset];
                    if ("-v".equalsIgnoreCase(arg)) {
                        verbose = true;
                    } else if ("-e".equalsIgnoreCase(arg)) {
                        edflt = false;
                    } else if ("-a".equalsIgnoreCase(arg)) {
                        adflt = true;
                    } else if ("-p".equalsIgnoreCase(arg)) {
                        paths.add(args[++offset]);
                    } else {
                        break;
                    }
                }
                
                // set up path and binding lists
                String[] vmpaths = Utility.getClassPaths();
                for (int i = 0; i < vmpaths.length; i++) {
                    paths.add(vmpaths[i]);
                }
                ArrayList bindings = new ArrayList();
                for (int i = offset; i < args.length; i++) {
                    bindings.add(args[i]);
                }
                
                // report on the configuration
                System.out.println("Running schema generator version " +
                    CURRENT_VERSION);
                if (verbose) {
                    System.out.println("Using paths:");
                    for (int i = 0; i < paths.size(); i++) {
                        System.out.println(" " + paths.get(i));
                    }
                    System.out.println("Using input bindings:");
                    for (int i = 0; i < bindings.size(); i++) {
                        System.out.println(" " + bindings.get(i));
                    }
                }
                
                // process all specified binding files
                SchemaGenerator schemagen = new SchemaGenerator(verbose, edflt,
                    adflt, paths);
                for (int i = 0; i < bindings.size(); i++) {
                    
                    // read binding from file
                    String bpath = (String)bindings.get(i);
                    String name = Utility.fileName(bpath);
                    File file = new File(bpath);
                    BindingElement binding = Utility.validateBinding(name,
                        new URL("file://" + file.getAbsolutePath()),
                        new FileInputStream(file));
                    
                    // convert the binding to a schema
                    if (binding != null) {
                        schemagen.generate(binding);
                    }
                }
                
                // output each schema to separate file
                Element[] schemas = schemagen.getSchemas();
                for (int i = 0; i < schemas.length; i++) {
                    
                    // get base name for output file from last part of namespace
                    Element schema = schemas[i];
                    String tns = schema.getAttribute("targetNamespace");
                    String name = tns;
                    if (name.length() == 0) {
                        // fix this to relate back to binding
                        name = (String)bindings.get(0);
                        int split = name.lastIndexOf('.');
                        if (split >= 0) {
                            name = name.substring(0, split);
                        }
                    } else {
                        int split = name.lastIndexOf('/');
                        if (split >= 0) {
                            name = name.substring(split+1);
                        }
                    }
                    try {
                        
                        // write schema to output file
                        name += ".xsd";
                        FileOutputStream out = new FileOutputStream(name);
                        Transformer transformer =
                            TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty("indent", "no");
                        DOMSource source = new DOMSource(schema);
                        Result result = new StreamResult(out);
                        transformer.transform(source, result);
                        out.close();
                        System.out.print("Wrote schema " + name);
                        if (tns.length() == 0) {
                            System.out.println(" for default namespace");
                        } else {
                            System.out.println(" for namespace " + tns);
                        }
                        
                    } catch (TransformerConfigurationException e) {
                        e.printStackTrace();
                    } catch (TransformerFactoryConfigurationError e) {
                        e.printStackTrace();
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                
            } catch (JiBXException ex) {
                ex.printStackTrace();
                System.exit(1);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(2);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(3);
            }
            
        } else {
            System.out.println
                ("\nUsage: java org.jibx.binding.SchemaGenerator [-v] [-e]" +
                " [-a] [-p path]* binding1 binding2 ...\nwhere:" +
                "\n -v  turns on verbose output," +
                "\n -e  sets elementFormDefault=\"false\" for the schemas," +
                "\n -a  sets attributeFormDefault=\"true\" for the schemas, " +
                "and\n -p  gives a path component for looking up the classes " +
                "referenced in the binding\nThe binding# files are " +
                "different bindings to be used for schema generation.\n");
            System.exit(1);
        }
    }
}

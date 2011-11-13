/*
Copyright (c) 2004-2009, Dennis M. Sosnoski.
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.ClassItem;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.CollectionElement;
import org.jibx.binding.model.ContainerElementBase;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.NamespaceElement;
import org.jibx.binding.model.StructureElement;
import org.jibx.binding.model.StructureElementBase;
import org.jibx.binding.model.ValueElement;
import org.jibx.binding.util.ObjectStack;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Binding generator. This loads the specified input classes and processes them
 * to generate a default binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class BindingGenerator
{
    /** Generator version. */
    private static String CURRENT_VERSION = "0.4";
    
    /** Set of objects treated as primitives. */
    private static HashSet s_objectPrimitiveSet = new HashSet();
    
    static {
        s_objectPrimitiveSet.add("java.lang.Boolean");
        s_objectPrimitiveSet.add("java.lang.Byte");
        s_objectPrimitiveSet.add("java.lang.Character");
        s_objectPrimitiveSet.add("java.lang.Double");
        s_objectPrimitiveSet.add("java.lang.Float");
        s_objectPrimitiveSet.add("java.lang.Integer");
        s_objectPrimitiveSet.add("java.lang.Long");
        s_objectPrimitiveSet.add("java.lang.Short");
        s_objectPrimitiveSet.add("java.math.BigDecimal");
        s_objectPrimitiveSet.add("java.math.BigInteger");
//#!j2me{
        s_objectPrimitiveSet.add("java.sql.Date");
        s_objectPrimitiveSet.add("java.sql.Time");
        s_objectPrimitiveSet.add("java.sql.Timestamp");
//#j2me}
        s_objectPrimitiveSet.add("java.util.Date");
    }
    
    /** Show verbose output flag. */
    private boolean m_verbose;
    
    /** Use camel case for XML names flag. */
    private boolean m_mixedCase;
    
    /** Namespace URI for elements. */
    private String m_namespaceUri;
    
    /** Class names to mapped element names map. */
    private HashMap m_mappedNames;
    
    /** Class names to properties list map. */
    private HashMap m_beanNames;
    
    /** Class names to deserializers map for typesafe enumerations. */
    private HashMap m_enumerationNames;
    
    /** Stack of structure definitions in progress (used to detect cycles). */
    private ObjectStack m_structureStack;
    
    /** Class names bound as nested structures. */
    private HashSet m_structureNames;
    
    /** Class names to be treated like interfaces (not mapped directly). */
    private HashSet m_ignoreNames;
    
    /**
     * Default constructor. This just initializes all options disabled.
     */
    public BindingGenerator() {
        m_mappedNames = new HashMap();
        m_structureStack = new ObjectStack();
        m_structureNames = new HashSet();
        m_ignoreNames = new HashSet();
    }
    
    /**
     * Constructor with settings specified.
     *
     * @param verbose report binding details and results
     * @param mixed use camel case in element names
     * @param uri namespace URI for element bindings
     */
    public BindingGenerator(boolean verbose, boolean mixed, String uri) {
        this();
        m_verbose = verbose;
        m_mixedCase = mixed;
        m_namespaceUri = uri;
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
     * Set control flag for camel case element naming.
     * 
     * @param camel use camel case element naming flag
     */
    public void setCamelCase(boolean camel) {
        m_mixedCase = camel;
    }
    
    /**
     * Indent to proper depth for current item.
     *
     * @param pw output print stream to be indented
     */
    private void nestingIndent(PrintStream pw) {
        for (int i = 0; i < m_structureStack.size(); i++) {
            pw.print(' ');
        }
    }
    
    /**
     * Convert class or unprefixed field name to element or attribute name.
     *
     * @param base class or simple field name to be converted
     * @return element or attribute name
     */
    private String convertName(String base) {
        StringBuffer name = new StringBuffer();
        name.append(Character.toLowerCase(base.charAt(0)));
        if (m_mixedCase) {
            name.append(base.substring(1));
        } else {
            boolean ignore = true;
            for (int i = 1; i < base.length(); i++) {
                char chr = base.charAt(i);
                if (Character.isUpperCase(chr)) {
                    chr = Character.toLowerCase(chr);
                    if (ignore) {
                        int next = i + 1;
                        ignore = next >= base.length() ||
                            Character.isUpperCase(base.charAt(next));
                    }
                    if (ignore) {
                        name.append(chr);
                    } else {
                        name.append('-');
                        name.append(chr);
                        ignore = true;
                    }
                } else {
                    name.append(chr);
                    ignore = false;
                }
            }
        }
        return name.toString();
    }
    
    /**
     * Generate structure element name from class name using set conversions.
     *
     * @param cname class name to be converted
     * @return element name for instances of class
     */
    public String elementName(String cname) {
        int split = cname.lastIndexOf('.');
        if (split >= 0) {
            cname = cname.substring(split+1);
        }
        while ((split = cname.indexOf('$')) >= 0) {
            cname = cname.substring(0, split) + cname.substring(split+1);
        }
        return convertName(cname);
    }
    
    /**
     * Generate structure element name from class name using set conversions.
     *
     * @param fname field name to be converted
     * @return element name for instances of class
     */
    private String valueName(String fname) {
        String base = fname;
        if (base.startsWith("m_")) {
            base = base.substring(2);
        } else if (base.startsWith("_")) {
            base = base.substring(1);
        } else if (base.startsWith("f") && base.length() > 1) {
            if (Character.isUpperCase(base.charAt(1))) {
                base = base.substring(1);
            }
        }
        return convertName(base);
    }
    
    /**
     * Construct the list of child binding components that define the binding
     * structure for fields of a particular class. This binds all
     * non-final/non-static/non-transient fields of the class, if necessary
     * creating nested structure elements for unmapped classes referenced by the
     * fields.
     * 
     * @param cf class information
     * @param contain binding structure container element
     * @throws JiBXException on error in binding generation
     */
    private void defineFields(ClassFile cf, ContainerElementBase contain)
        throws JiBXException {
        
        // process all non-final/non-static/non-transient fields of class
        JavaClass clas = cf.getRawClass();
        Field[] fields = clas.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!field.isFinal() && !field.isStatic() && !field.isTransient()) {
                
                // find type of handling needed for field type
                boolean simple = false;
                boolean object = true;
                boolean attribute = true;
                String fname = field.getName();
                String tname = field.getType().toString();
                if (ClassItem.isPrimitive(tname)) {
                    simple = true;
                    object = false;
                } else if (s_objectPrimitiveSet.contains(tname)) {
                    simple = true;
                } else if (tname.equals("byte[]")) {
                    simple = true;
                    attribute = false;
                } else if (tname.startsWith("java.")) {
                    
                    // check for standard library classes we can handle
                    ClassFile pcf = ClassCache.requireClassFile(tname);
                    ClassItem init = pcf.getInitializerMethod
                        ("(Ljava/lang/String;)");
                    if (init != null) {
                        simple = pcf.getMethod("toString",
                            "()Ljava/lang/String;") != null;
                        attribute = false;
                    }
                    
                }
                
                // check type of handling to use for value
                if (m_enumerationNames.get(tname) != null) {
                    
                    // define a value using deserializer method
                    String mname = (String)m_enumerationNames.get(tname);
                    int split = mname.lastIndexOf('.');
                    ClassFile dcf =
                        ClassCache.requireClassFile(mname.substring(0, split));
                    ClassItem dser =
                        dcf.getStaticMethod(mname.substring(split+1),
                        "(Ljava/lang/String;)");
                    if (dser == null || !tname.equals(dser.getTypeName())) {
                        throw new JiBXException("Deserializer method not " +
                            "found for enumeration class " + tname);
                    }
                    ValueElement value = new ValueElement();
                    value.setFieldName(fname);
                    value.setName(valueName(fname));
                    value.setUsageName("optional");
                    value.setStyleName("element");
                    value.setDeserializerName(mname);
                    contain.addChild(value);
                    
                } else if (m_mappedNames.get(tname) != null) {
                    
                    // use mapping definition for class
                    StructureElement structure = new StructureElement();
                    structure.setUsageName("optional");
                    structure.setFieldName(fname);
                    if (((String)m_mappedNames.get(tname)).length() == 0) {
                        
                        // add a name for reference to abstract mapping
                        structure.setName(elementName(tname));
                    }
                    contain.addChild(structure);
                    if (m_verbose) {
                        nestingIndent(System.out);
                        System.out.println("referenced existing binding for " +
                            tname);
                    }
                    
                } else if (simple) {
                    
                    // define a simple value
                    ValueElement value = new ValueElement();
                    value.setFieldName(fname);
                    value.setName(valueName(fname));
                    if (object) {
                        value.setUsageName("optional");
                    }
                    if (!attribute) {
                        value.setStyleName("element");
                    }
                    contain.addChild(value);
                    
                } else if (tname.endsWith("[]")) {
                    
                    // array, check if item type has a mapping
                    String bname = tname.substring(0, tname.length()-2);
                    if (m_mappedNames.get(bname) == null) {
                        
                        // no mapping, use collection with inline structure
                        // TODO: fill it in
                        throw new JiBXException("Base element type " + bname +
                            " must be mapped");
                        
                    } else {
                        
                        // mapping for type, use simple collection
                        CollectionElement collection = new CollectionElement();
                        collection.setUsageName("optional");
                        collection.setFieldName(fname);
                        contain.addChild(collection);
                    }
                    
                } else {
                    
                    // no defined handling, check for collection vs. structure
                    ClassFile pcf = ClassCache.requireClassFile(tname);
                    StructureElementBase element;
                    if (pcf.isImplements("Ljava/util/List;")) {
                        
                        // create a collection for list subclass
                        System.err.println("Warning: field " + fname +
                            " requires mapped implementation of item classes");
                        element = new CollectionElement();
                        element.setComment(" add details of collection items " +
                            "to complete binding definition ");
                        element.setFieldName(fname);
                        
                        // specify factory method if just typed as interface
                        if ("java.util.List".equals(tname)) {
                            element.setFactoryName
                                ("org.jibx.runtime.Utility.arrayListFactory");
                        }
                        
                    } else if (pcf.isInterface() ||
                        m_ignoreNames.contains(tname)) {
                        
                        // create mapping reference with warning for interface
                        nestingIndent(System.err);
                        System.err.println("Warning: reference to interface " +
                            "or abstract class " + tname +
                            " requires mapped implementation");
                        element = new StructureElement();
                        element.setFieldName(fname);
                        
                    } else {
                        
                        // handle other types of structures directly
                        element = createStructure(pcf, fname);
                    }
                    
                    // finish with common handling
                    element.setUsageName("optional");
                    contain.addChild(element);
                    
                }
            }
        }
    }
    
    /**
     * Construct the list of child binding components that define the binding
     * structure corresponding to properties of a particular class. This binds
     * the specified properties of the class, using get/set methods, if
     * necessary creating nested structure elements for unmapped classes
     * referenced by the properties.
     * 
     * @param cf class information
     * @param props list of properties specified for class
     * @param internal allow private get/set methods flag
     * @param contain binding structure container element
     * @throws JiBXException on error in binding generation
     */
    private void defineProperties(ClassFile cf, ArrayList props,
        boolean internal, ContainerElementBase contain) throws JiBXException {
        
        // process all properties of class
        for (int i = 0; i < props.size(); i++) {
            String pname = (String)props.get(i);
            String base = Character.toUpperCase(pname.charAt(0)) +
                pname.substring(1);
            String gname = "get" + base;
            ClassItem gmeth = cf.getMethod(gname, "()");
            if (gmeth == null) {
                throw new JiBXException("No method " + gname +
                    "() found for property " + base + " in class " +
                    cf.getName());
            } else {
                String tname = gmeth.getTypeName();
                String sname = "set" + base;
                String sig = "(" + gmeth.getSignature().substring(2) + ")V";
                ClassItem smeth = cf.getMethod(sname, sig);
                if (smeth == null) {
                    throw new JiBXException("No method " + sname + "(" + tname +
                        ") found for property " + base + " in class " +
                        cf.getName());
                } else {
                    
                    // find type of handling needed for field type
                    boolean simple = false;
                    boolean object = true;
                    boolean attribute = true;
                    if (ClassItem.isPrimitive(tname)) {
                        simple = true;
                        object = false;
                    } else if (s_objectPrimitiveSet.contains(tname)) {
                        simple = true;
                    } else if (tname.equals("byte[]")) {
                        simple = true;
                        attribute = false;
                    } else if (tname.startsWith("java.")) {
                        
                        // check for standard library classes we can handle
                        ClassFile pcf = ClassCache.requireClassFile(tname);
                        if (pcf.getInitializerMethod("()") != null) {
                            simple = pcf.getMethod("toString",
                                "()Ljava/lang/String;") != null;
                            attribute = false;
                        }
                        
                    }
                    if (simple) {
                        
                        // define a simple value
                        ValueElement value = new ValueElement();
                        value.setGetName(gname);
                        value.setSetName(sname);
                        value.setName(valueName(pname));
                        if (object) {
                            value.setUsageName("optional");
                        }
                        if (!attribute) {
                            value.setStyleName("element");
                        }
                        contain.addChild(value);
                        
                    } else if (m_enumerationNames.get(tname) != null) {
                        
                        // define a value using deserializer method
                        String mname = (String)m_enumerationNames.get(tname);
                        int split = mname.lastIndexOf('.');
                        ClassFile dcf = ClassCache.
                            requireClassFile(mname.substring(0, split));
                        ClassItem dser =
                            dcf.getStaticMethod(mname.substring(split+1),
                            "(Ljava/lang/String;)");
                        if (dser == null || !tname.equals(dser.getTypeName())) {
                            throw new JiBXException("Deserializer method not " +
                                "found for enumeration class " + tname);
                        }
                        ValueElement value = new ValueElement();
                        value.setGetName(gname);
                        value.setSetName(sname);
                        value.setName(valueName(pname));
                        value.setUsageName("optional");
                        value.setStyleName("element");
                        value.setDeserializerName(mname);
                        contain.addChild(value);
                        
                    } else if (m_mappedNames.get(tname) != null) {
                        
                        // use mapping definition for class
                        StructureElement structure = new StructureElement();
                        structure.setUsageName("optional");
                        structure.setGetName(gname);
                        structure.setSetName(sname);
                        if (((String)m_mappedNames.get(tname)).length() == 0) {
                            
                            // add a name for reference to abstract mapping
                            structure.setName(elementName(tname));
                        }
                        contain.addChild(structure);
                        if (m_verbose) {
                            nestingIndent(System.out);
                            System.out.println
                                ("referenced existing binding for " + tname);
                        }
                        
                    } else if (tname.endsWith("[]")) {
                        
                        // array, only supported for mapped base type
                        String bname = tname.substring(0, tname.length()-2);
                        if (m_mappedNames.get(bname) == null) {
                            throw new JiBXException("Base element type " +
                                bname + " must be mapped");
                        } else {
                            StructureElement structure = new StructureElement();
                            structure.setUsageName("optional");
                            structure.setGetName(gname);
                            structure.setSetName(sname);
                            structure.setMarshallerName
                                ("org.jibx.extras.TypedArrayMapper");
                            structure.setUnmarshallerName
                                ("org.jibx.extras.TypedArrayMapper");
                            contain.addChild(structure);
                        }
                        
                    } else {
                        
                        // no defined handling, check collection vs. structure
                        ClassFile pcf = ClassCache.requireClassFile(tname);
                        StructureElementBase element;
                        if (pcf.isImplements("Ljava/util/List;")) {
                            
                            // create a collection for list subclass
                            System.err.println("Warning: property " + pname +
                                " requires mapped implementation of item " +
                                "classes");
                            element = new CollectionElement();
                            element.setComment(" add details of collection " +
                                "items to complete binding definition ");
                            element.setGetName(gname);
                            element.setSetName(sname);
                            
                            // specify factory method if just typed as interface
                            if ("java.util.List".equals(tname)) {
                                element.setFactoryName("org.jibx.runtime." +
                                    "Utility.arrayListFactory");
                            }
                            
                        } else if (pcf.isInterface() ||
                            m_ignoreNames.contains(tname)) {
                            
                            // mapping reference with warning for interface
                            nestingIndent(System.err);
                            System.err.println("Warning: reference to " +
                                "interface or abstract class " + tname +
                                " requires mapped implementation");
                            element = new StructureElement();
                            element.setGetName(gname);
                            element.setSetName(sname);
                            
                        } else {
                            
                            // handle other types of structures directly
                            element = createStructure(pcf, pname);
                        }
                        
                        // finish with common handling
                        element.setUsageName("optional");
                        contain.addChild(element);
                        
                    }
                }
            }
        }
    }
    
    /**
     * Construct the list of child binding components that define the binding
     * structure corresponding to a particular class. This binds all
     * non-final/non-static/non-transient fields of the class, if necessary
     * creating nested structure elements for unmapped classes referenced by the
     * fields.
     * 
     * @param cf class information
     * @param contain binding structure container element
     * @throws JiBXException on error in binding generation
     */
    private void defineStructure(ClassFile cf, ContainerElementBase contain)
        throws JiBXException {
        
        // process basic fields or property list
        Object props = m_beanNames.get(cf.getName());
        if (props == null) {
            defineFields(cf, contain);
        } else {
            defineProperties(cf, (ArrayList)props, true, contain);
        }
        
        // check if superclass may have data for binding
        ClassFile sf = cf.getSuperFile();
        String sname = sf.getName();
        if (!"java.lang.Object".equals(sname) &&
            !m_ignoreNames.contains(sname)) {
            if (m_mappedNames.get(sname) != null) {
                StructureElement structure = new StructureElement();
                structure.setMapAsName(sname);
                structure.setName(elementName(sname));
                contain.addChild(structure);
            } else if (m_beanNames.get(sname) != null) {
                defineProperties(sf, (ArrayList)m_beanNames.get(sname),
                    false, contain);
            } else if (sf.getRawClass().getFields().length > 0) {
                nestingIndent(System.err);
                System.err.println("Warning: fields from base class " + sname +
                    " of class " + cf.getName() + " not handled by generated " +
                    "binding; use mapping or specify property list");
                contain.setComment(" missing information for base class " +
                    sname + " ");
            }
        }
    }
    
    /**
     * Create the structure element for a particular class. This maps all
     * non-final/non-static/non-transient fields of the class, if necessary
     * creating nested structures.
     * 
     * @param cf class information
     * @param fname name of field supplying reference
     * @throws JiBXException on error in binding generation
     */
    private StructureElement createStructure(ClassFile cf, String fname)
        throws JiBXException {
        JavaClass clas = cf.getRawClass();
        if (m_verbose) {
            nestingIndent(System.out);
            System.out.println("creating nested structure definition for " +
                clas.getClassName());
        }
        String cname = clas.getClassName();
        for (int i = 0; i < m_structureStack.size(); i++) {
            if (cname.equals(m_structureStack.peek(i))) {
                StringBuffer buff =
                    new StringBuffer("Error: recursive use of ");
                buff.append(cname);
                buff.append(" requires <mapping>:\n ");
                while (i >= 0) {
                    buff.append(m_structureStack.peek(i--));
                    buff.append(" -> ");
                }
                buff.append(cname);
                throw new JiBXException(buff.toString());
            }
        }
        if (cname.startsWith("java.")) {
            nestingIndent(System.err);
            System.err.println("Warning: trying to create structure for " +
                cname);
        } else if (m_structureNames.contains(cname)) {
            nestingIndent(System.err);
            System.err.println("Warning: repeated usage of class " +
                cname + "; consider adding to mapping list");
        } else {
            m_structureNames.add(cname);
        }
        m_structureStack.push(cname);
        StructureElement element = new StructureElement();
        element.setFieldName(fname);
        element.setName(valueName(fname));
        defineStructure(cf, element);
        if (element.children().isEmpty()) {
            throw new JiBXException("No content found for class " + cname);
        }
        m_structureStack.pop();
        if (m_verbose) {
            nestingIndent(System.out);
            System.out.println("completed nested structure definition for " +
                clas.getClassName());
        }
        return element;
    }

    /**
     * Create the mapping element for a particular class. This maps all
     * non-final/non-static/non-transient fields of the class, if necessary
     * creating nested structures.
     * 
     * @param cf class information
     * @param abstr force abstract mapping flag
     * @throws JiBXException on error in binding generation
     */
    private MappingElementBase createMapping(ClassFile cf, boolean abstr)
        throws JiBXException {
        JavaClass clas = cf.getRawClass();
        if (m_verbose) {
            System.out.println("\nBuilding mapping definition for " +
                clas.getClassName());
        }
        MappingElementBase element = new MappingElement();
        element.setAbstract(abstr || clas.isAbstract() || clas.isInterface());
        String name = clas.getClassName();
        element.setClassName(name);
        if (abstr) {
            element.setAbstract(true);
        } else {
            element.setName((String)m_mappedNames.get(name));
        }
        m_structureStack.push(name);
        defineStructure(cf, element);
        m_structureStack.pop();
        return element;
    }
    
    private static boolean isMappable(String cname) {
        if ("java.lang.String".equals(cname)) {
            return false;
        } else if ("java.lang.Object".equals(cname)) {
            return false;
        } else if (ClassItem.isPrimitive(cname)) {
            return false;
        } else {
            return !s_objectPrimitiveSet.contains(cname);
        }
    }

    /**
     * Get the set of data classes passed to or returned by a list of methods
     * within a class. The classes returned exclude primitive types, wrappers,
     * <code>java.lang.String</code>, and <code>java.lang.Object</code>.
     * Exception classes thrown by the methods are also optionally accumulated.
     * 
     * @param cname target class name
     * @param mnames method names to be checked
     * @param dataset set for accumulation of data classes (optional, data
     * classes not recorded if <code>null</code>)
     * @param exceptset set for accumulation of exception classes (optional,
     * data classes not recorded if <code>null</code>)
     * @throws JiBXException on error in loading class information
     */
    public static void findClassesUsed(String cname, ArrayList mnames,
        HashSet dataset, HashSet exceptset) throws JiBXException {
        ClassFile cf = ClassCache.getClassFile(cname);
        if (cf != null) {
            for (int i = 0; i < mnames.size(); i++) {
                String mname = (String)mnames.get(i);
                ClassItem mitem = cf.getMethod(mname, "");
                if (mitem == null) {
                    System.err.println("Method " + mname +
                        " not found in class " + cname);
                } else {
                    if (dataset != null) {
                        String type = mitem.getTypeName();
                        if (type != null && isMappable(type)) {
                            dataset.add(type);
                        }
                        String[] args = mitem.getArgumentTypes();
                        for (int j = 0; j < args.length; j++) {
                            type = args[j];
                            if (isMappable(type)) {
                                dataset.add(args[j]);
                            }
                        }
                    }
                    if (exceptset != null) {
                        String[] excepts = mitem.getExceptions();
                        for (int j = 0; j < excepts.length; j++) {
                            exceptset.add(excepts[j]);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Generate a set of bindings using supplied classpaths and class names.
     * 
     * @param names list of class names to be included in binding
     * @param abstracts set of classes to be handled with abstract mappings in
     * binding
     * @param customs map of customized class names to marshaller/unmarshaller
     * class names
     * @param beans map of class names to supplied lists of properties
     * @param enums map of typesafe enumeration classes to deserializer methods
     * @param ignores list of non-interface classes to be treated as interfaces
     * (no mapping, but mapped subclasses are used at runtime)
     * @exception JiBXException if error in generating the binding definition
     */
    public BindingElement generate(ArrayList names, HashSet abstracts,
        HashMap customs, HashMap beans, HashMap enums, ArrayList ignores)
        throws JiBXException {
        
        // print current version information
        System.out.println("Running binding generator version " +
            CURRENT_VERSION);
        
        // add all classes with mappings to tracking map
        m_mappedNames.clear();
        for (int i = 0; i < names.size(); i++) {
            
            // make sure class can potentially be handled automatically
            boolean drop = false;
            String name = (String)names.get(i);
            ClassFile cf = ClassCache.requireClassFile(name);
            if (cf.isImplements("Ljava/util/List;") ||
                cf.isImplements("Ljava/util/Map;")) {
                System.err.println("Warning: referenced class " + name +
                    " is a collection class that cannot be mapped " +
                    "automatically; dropped from mapped list in binding");
                drop = true;
            } else if (cf.isInterface()) {
                System.err.println("Warning: interface " + name +
                    " is being handled as abstract mapping");
                abstracts.add(name);
                drop = true;
            } else if (cf.isAbstract()) {
                System.err.println("Warning: mapping abstract class " + name +
                    "; make sure actual subclasses are mapped as extending " +
                    "this abstract mapping");
                abstracts.add(name);
                drop = true;
            }
            if (drop) {
                names.remove(i--);
            } else {
                m_mappedNames.put(name, elementName(name));
            }
        }
        for (Iterator iter = abstracts.iterator(); iter.hasNext();) {
            m_mappedNames.put((String)iter.next(), "");
        }
        for (Iterator iter = customs.keySet().iterator(); iter.hasNext();) {
            String name = (String)iter.next();
            m_mappedNames.put(name, elementName(name));
        }
        
        // create set for ignores
        m_ignoreNames.clear();
        m_ignoreNames.addAll(ignores);
        
        // create binding with optional namespace
        BindingElement binding = new BindingElement();
        binding.setStyleName("attribute");
        if (m_namespaceUri != null) {
            NamespaceElement namespace = new NamespaceElement();
            namespace.setComment(" namespace for all elements of binding ");
            namespace.setDefaultName("elements");
            namespace.setUri(m_namespaceUri);
            binding.addTopChild(namespace);
        }
        
        // add mapping for each specified class
        m_structureStack.clear();
        m_structureNames.clear();
        m_beanNames = beans;
        m_enumerationNames = enums;
        for (int i = 0; i < names.size(); i++) {
            String cname = (String)names.get(i);
            if (!abstracts.contains(cname)) {
                ClassFile cf = ClassCache.requireClassFile(cname);
                MappingElementBase mapping = createMapping(cf, false);
                mapping.setComment(" generated mapping for class " + cname);
                binding.addTopChild(mapping);
            }
        }
        for (Iterator iter = abstracts.iterator(); iter.hasNext();) {
            String cname = (String)iter.next();
            ClassFile cf = ClassCache.requireClassFile(cname);
            MappingElementBase mapping = createMapping(cf, true);
            mapping.setComment(" generate abstract mapping for class " + cname);
            binding.addTopChild(mapping);
        }
        
        // finish with custom mapping definitions
        for (Iterator iter = customs.keySet().iterator(); iter.hasNext();) {
            String cname = (String)iter.next();
            String mname = (String)customs.get(cname);
            MappingElementBase mapping = new MappingElement();
            mapping.setComment(" specified mapping for class " + cname);
            mapping.setClassName(cname);
            mapping.setName((String)m_mappedNames.get(cname));
            mapping.setMarshallerName(mname);
            mapping.setUnmarshallerName(mname);
            binding.addTopChild(mapping);
        }
        
        // list classes handled if verbose
        if (m_verbose) {
            for (int i = 0; i < names.size(); i++) {
                m_structureNames.add(names.get(i));
            }
            m_structureNames.addAll(customs.keySet());
            String[] classes = (String[])m_structureNames.
                toArray(new String[m_structureNames.size()]);
            Arrays.sort(classes);
            System.out.println("\nClasses included in binding:");
            for (int i = 0; i < classes.length; i++) {
                System.out.println(" " + classes[i]);
            }
        }
        return binding;
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
                boolean mixed = false;
                boolean verbose = false;
                String uri = null;
                String fname = "binding.xml";
                ArrayList paths = new ArrayList();
                HashMap enums = new HashMap();
                HashSet abstracts = new HashSet();
                ArrayList ignores = new ArrayList();
                HashMap customs = new HashMap();
                HashMap beans = new HashMap();
                int offset = 0;
                for (; offset < args.length; offset++) {
                    String arg = args[offset];
                    if ("-v".equalsIgnoreCase(arg)) {
                        verbose = true;
                    } else if ("-b".equalsIgnoreCase(arg)) {
                        String plist = args[++offset];
                        int split = plist.indexOf("=");
                        String bname = plist.substring(0, split);
                        ArrayList props = new ArrayList();
                        int base = ++split;
                        while ((split = plist.indexOf(',', split)) >= 0) {
                            props.add(plist.substring(base, split));
                            base = ++split;
                        }
                        props.add(plist.substring(base));
                        beans.put(bname, props);
                    } else if ("-c".equalsIgnoreCase(arg)) {
                        String custom = args[++offset];
                        int split = custom.indexOf('=');
                        if (split > 0) {
                            customs.put(custom.substring(0, split),
                                custom.substring(split+1));
                        } else {
                            System.err.println
                                ("Unable to interpret customization " + custom);
                        }
                    } else if ("-e".equalsIgnoreCase(arg)) {
                        String cname;
                        String mname;
                        String enumf = args[++offset];
                        int split = enumf.indexOf('=');
                        if (split > 0) {
                            cname = enumf.substring(0, split);
                            mname = enumf.substring(split+1);
                            if (mname.indexOf('.') < 0) {
                                mname = cname + '.' + mname;
                            }
                        } else {
                            cname = enumf;
                            split = cname.lastIndexOf('.');
                            if (split >= 0) {
                                mname = cname + '.' + "get" +
                                    cname.substring(split+1);
                            } else {
                                mname = cname + '.' + "get" + cname;
                            }
                        }
                        enums.put(cname, mname);
                    } else if ("-i".equalsIgnoreCase(arg)) {
                        ignores.add(args[++offset]);
                    } else if ("-m".equalsIgnoreCase(arg)) {
                        mixed = true;
                    } else if ("-f".equalsIgnoreCase(arg)) {
                        fname = args[++offset];
                    } else if ("-n".equalsIgnoreCase(arg)) {
                        uri = args[++offset];
                    } else if ("-p".equalsIgnoreCase(arg)) {
                        paths.add(args[++offset]);
                    } else if ("-a".equalsIgnoreCase(arg)) {
                        abstracts.add(args[++offset]);
                    } else {
                        break;
                    }
                }
                
                // set up path and binding lists
                String[] vmpaths = Utility.getClassPaths();
                for (int i = 0; i < vmpaths.length; i++) {
                    paths.add(vmpaths[i]);
                }
                ArrayList names = new ArrayList();
                for (int i = offset; i < args.length; i++) {
                    if (!abstracts.contains(args[i])) {
                        names.add(args[i]);
                    }
                }
                
                // report on the configuration
                if (verbose) {
                    System.out.println("Using paths:");
                    for (int i = 0; i < paths.size(); i++) {
                        System.out.println(" " + paths.get(i));
                    }
                    System.out.println("Using class names:");
                    for (int i = 0; i < names.size(); i++) {
                        System.out.println(" " + names.get(i));
                    }
                    if (abstracts.size() > 0) {
                        System.out.println("Using abstract class names:");
                        for (Iterator iter = abstracts.iterator();
                            iter.hasNext();) {
                            System.out.println(" " + iter.next());
                        }
                    }
                    if (customs.size() > 0) {
                        System.out.println
                            ("Using custom marshaller/unmarshallers:");
                        Iterator iter = customs.keySet().iterator();
                        while (iter.hasNext()) {
                            String key = (String)iter.next();
                            System.out.println(" " + customs.get(key) +
                                " for " + key);
                        }
                    }
                    if (beans.size() > 0) {
                        System.out.println("Using bean property lists:");
                        Iterator iter = beans.keySet().iterator();
                        while (iter.hasNext()) {
                            String key = (String)iter.next();
                            System.out.print(" " + key + " with properties:");
                            ArrayList props = (ArrayList)beans.get(key);
                            for (int i = 0; i < props.size(); i++) {
                                System.out.print(i > 0 ? ", " : " ");
                                System.out.print(props.get(i));
                            }
                        }
                    }
                    if (enums.size() > 0) {
                        System.out.println("Using enumeration classes:");
                        Iterator iter = enums.keySet().iterator();
                        while (iter.hasNext()) {
                            String key = (String)iter.next();
                            System.out.println(" " + key +
                                " with deserializer " + enums.get(key));
                        }
                    }
                }
                
                // set paths to be used for loading referenced classes
                String[] parray =
                    (String[])paths.toArray(new String[paths.size()]);
                ClassCache.setPaths(parray);
                ClassFile.setPaths(parray);
                
                // generate the binding
                BindingGenerator generate =
                    new BindingGenerator(verbose, mixed, uri);
                BindingElement binding = generate.generate(names, abstracts,
                    customs, beans, enums, ignores);
                
                // marshal binding out to file
                IBindingFactory bfact =
                    BindingDirectory.getFactory("normal", BindingElement.class);
                IMarshallingContext mctx = bfact.createMarshallingContext();
                mctx.setIndent(2);
                mctx.marshalDocument(binding, "UTF-8", null,
                    new FileOutputStream(fname));
                
            } catch (JiBXException ex) {
                ex.printStackTrace(System.out);
                System.exit(1);
            } catch (FileNotFoundException e) {
                e.printStackTrace(System.out);
                System.exit(2);
            }
            
        } else {
            System.out.println
                ("\nUsage: java org.jibx.binding.BindingGenerator [options] " +
                "class1 class2 ...\nwhere options are:\n -a  abstract class\n" +
                " -b  bean property class,\n -c  custom mapped class,\n" +
                " -e  enumeration class,\n -f  binding file name,\n" +
                " -i  ignore class,\n -m  use mixed case XML names (instead " +
                "of hyphenated),\n -n  namespace URI,\n -p  class " +
                "loading path component,\n -v  turns on verbose output\n" +
                "The class# files are different classes to be included in " +
                "binding (references from\nthese classes will also be " +
                "included).\n");
            System.exit(1);
        }
    }
}

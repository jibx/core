/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.custom.classes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jibx.custom.CustomUtils;
import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.QName;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.IClassLocator;
import org.jibx.util.InsertionOrderedMap;
import org.jibx.util.InsertionOrderedSet;
import org.jibx.util.StringArray;

/**
 * Class customization information. This supports direct class customizations (such as the corresponding element name,
 * when building a concrete mapping) and also acts as a container for individual fields and/or properties.
 * 
 * @author Dennis M. Sosnoski
 */
public class ClassCustom extends NestingBase implements IApply
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "create-type", "deserializer", "element-name", "enum-value-method", "excludes",
            "factory", "form", "includes", "name", "optionals", "requireds", "serializer", "type-name", "use-super" },
            NestingBase.s_allowedAttributes);
    
    /** Element name in XML customization file. */
    public static final String ELEMENT_NAME = "class";
    
    // value set information
    public static final int FORM_DEFAULT = 0;
    public static final int FORM_CONCRETE_MAPPING = 1;
    public static final int FORM_ABSTRACT_MAPPING = 2;
    public static final int FORM_STRING = 3;
    
    public static final EnumSet s_representationEnum =
        new EnumSet(FORM_DEFAULT, new String[] { "default", "concrete-mapping", "abstract-mapping", "simple" });
    
    // values specific to class level
    private String m_name;
    
    private String m_elementName;
    
    private String m_typeName;
    
    private String m_createType;
    
    private String m_factoryMethod;
    
    private String m_enumValueMethod;
    
    private int m_form;
    
    private String[] m_includes;
    
    private String[] m_excludes;
    
    private boolean m_useSuper;
    
    private String[] m_requireds;
    
    private String[] m_optionals;
    
    private String m_serializer;
    
    private String m_deserializer;
    
    // list of contained items
    private final ArrayList m_children;
    
    // values filled in by apply() method
    private boolean m_isApplied;
    
    private QName m_typeQName;
    
    private QName m_elementQName;
    
    private IClass m_classInformation;
    
    private InsertionOrderedMap m_memberMap;
    
    /**
     * Constructor.
     * 
     * @param parent
     * @param name class simple name (without package)
     */
    /* package */ClassCustom(NestingBase parent, String name) {
        super(parent);
        m_name = name;
        m_children = new ArrayList();
        m_useSuper = true;
        m_form = -1;
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Get fully-qualified class name.
     * 
     * @return class name
     */
    public String getName() {
        PackageCustom parent = (PackageCustom)getParent();
        String pack = parent.getName();
        if (pack.length() > 0) {
            return pack + '.' + m_name;
        } else {
            return m_name;
        }
    }
    
    /**
     * Get simple class name.
     * 
     * @return class name
     */
    public String getSimpleName() {
        return m_name;
    }
    
    /**
     * Get the element name to be used for this class in a concrete mapping.
     * 
     * @return element name
     */
    public String getElementName() {
        return m_elementName;
    }
    
    /**
     * Get the qualified element name to be used for this class in a concrete mapping.
     * 
     * @return element name
     */
    public QName getElementQName() {
        return m_elementQName;
    }
    
    /**
     * Get the type name to be used for this class in an abstract mapping.
     * 
     * @return type name
     */
    public String getTypeName() {
        return m_typeName;
    }
    
    /**
     * Get the type name to be used when creating an instance of this class.
     * 
     * @return type name
     */
    public String getCreateType() {
        return m_createType;
    }
    
    /**
     * Set the type name to be used when creating an instance of this class.
     * 
     * @param type
     */
    public void setCreateType(String type) {
        m_createType = type;
    }
    
    /**
     * Get serializer method.
     *
     * @return serializer
     */
    public String getSerializer() {
        return m_serializer;
    }
    
    /**
     * Get deserializer method.
     *
     * @return deserializer
     */
    public String getDeserializer() {
        return m_deserializer;
    }
    
    /**
     * Get the method used to retrieve the text value for an enum class.
     * 
     * @return method name
     */
    public String getEnumValueMethod() {
        return m_enumValueMethod;
    }
    
    /**
     * Get the factory method to be used when creating an instance of this class.
     * 
     * @return method name
     */
    public String getFactoryMethod() {
        return m_factoryMethod;
    }
    
    /**
     * Get the qualified type name to be used for this class in an abstract mapping.
     * 
     * @return type qname
     */
    public QName getTypeQName() {
        return m_typeQName;
    }
    
    /**
     * Get the representation code.
     * 
     * @return value from {@link #s_representationEnum} enumeration
     */
    public int getForm() {
        return m_form;
    }
    
    /**
     * Get list of names to be excluded from class representation.
     * 
     * @return excludes (<code>null</code> if none)
     */
    public String[] getExcludes() {
        return m_excludes;
    }
    
    /**
     * Get list of names to be included in class representation.
     * 
     * @return includes (<code>null</code> if none)
     */
    public String[] getIncludes() {
        return m_includes;
    }
    
    /**
     * Check for superclass to be included in binding.
     * 
     * @return <code>true</code> if superclass included, <code>false</code> if not
     */
    public boolean isUseSuper() {
        return m_useSuper;
    }
    
    /**
     * Check if this is a directly instantiable class (not an interface, and not abstract)
     * 
     * @return <code>true</code> if instantiable, <code>false</code> if not
     */
    public boolean isConcreteClass() {
        return !(m_classInformation.isAbstract() || m_classInformation.isInterface());
    }
    
    /**
     * Check if class represents a simple text value.
     *
     * @return text value flag
     */
    public boolean isSimpleValue() {
        return m_form == FORM_STRING;
    }
    
    /**
     * Check if abstract mapping required for class.
     * 
     * @return abstract mapping flag
     */
    public boolean isAbstractMappingForced() {
        return m_form == FORM_ABSTRACT_MAPPING;
    }
    
    /**
     * Check if concrete mapping required for class. If a 'form' setting is defined for the class it returns the flag
     * based on that setting, and otherwise returns based on the nesting abstract mapping flag.
     * 
     * @return abstract mapping flag
     */
    public boolean isConcreteMappingForced() {
        return m_form == FORM_CONCRETE_MAPPING;
    }
    
    /**
     * Get list of children.
     * 
     * @return list
     */
    public List getChildren() {
        return m_children;
    }
    
    /**
     * Add child.
     * 
     * @param child
     */
    protected void addChild(CustomBase child) {
        if (child.getParent() == this) {
            m_children.add(child);
        } else {
            throw new IllegalStateException("Internal error: child not linked");
        }
    }
    
    /**
     * Form set text method. This is intended for use during unmarshalling. TODO: add validation
     * 
     * @param text
     * @param ictx
     */
    private void setFormText(String text, IUnmarshallingContext ictx) {
        m_form = s_representationEnum.getValue(text);
    }
    
    /**
     * Form get text method. This is intended for use during marshalling.
     * 
     * @return text
     */
    private String getFormText() {
        return s_representationEnum.getName(m_form);
    }
    
    /**
     * Build map from member names to read access methods. This assumes that each public, non-static, no-argument
     * method which returns a value and has a name beginning with "get" or "is" is a property read access method. It
     * maps the corresponding property name to the method, and returns the map.
     * 
     * @param methods
     * @param inclset set of member names to be included (<code>null</code> if not specified)
     * @param exclset set of member names to be excluded (<code>null</code> if not specified, ignored if inclset is
     * non-<code>null</code>)
     * @return map
     */
    private Map mapPropertyReadMethods(IClassItem[] methods, Set inclset, Set exclset) {
        
        // check all methods for property read access matches
        InsertionOrderedMap getmap = new InsertionOrderedMap();
        for (int i = 0; i < methods.length; i++) {
            IClassItem item = methods[i];
            String name = item.getName();
            int flags = item.getAccessFlags();
            if (item.getArgumentCount() == 0 && (flags & Modifier.STATIC) == 0 && (flags & Modifier.PUBLIC) != 0 && 
                ((name.startsWith("get") && !item.getTypeName().equals("void")) || (name.startsWith("is") &&
                item.getTypeName().equals("boolean")))) {
                
                // have what appears to be a getter, check if it should be used
                String memb = ValueCustom.memberNameFromGetMethod(name);
                boolean use = true;
                if (inclset != null) {
                    use = inclset.contains(memb.toLowerCase());
                } else if (exclset != null) {
                    use = !exclset.contains(memb.toLowerCase());
                }
                if (use) {
                    getmap.put(memb, item);
                }
                
            }
        }
        return getmap;
    }
    
    /**
     * Build map from member names to write access methods. This assumes that each public, non-static, single-argument
     * method which returns void and has a name beginning with "set" is a property write access method. It maps the
     * corresponding property name to the method, and returns the map.
     * 
     * @param methods
     * @param inclset set of member names to be included (<code>null</code> if not specified)
     * @param exclset set of member names to be excluded (<code>null</code> if not specified, ignored if inclset is
     * non-<code>null</code>)
     * @return map
     */
    private Map mapPropertyWriteMethods(IClassItem[] methods, Set inclset, Set exclset) {
        
        // check all methods for property write access matches
        InsertionOrderedMap setmap = new InsertionOrderedMap();
        for (int i = 0; i < methods.length; i++) {
            IClassItem item = methods[i];
            String name = item.getName();
            int flags = item.getAccessFlags();
            if (item.getArgumentCount() == 1 && (flags & Modifier.STATIC) == 0 && (flags & Modifier.PUBLIC) != 0 &&
                name.startsWith("set") && item.getTypeName().equals("void")) {
                
                // have what appears to be a setter, check if it should be used
                String memb = ValueCustom.memberNameFromSetMethod(name);
                boolean use = true;
                if (inclset != null) {
                    use = inclset.contains(memb.toLowerCase());
                } else if (exclset != null) {
                    use = !exclset.contains(memb.toLowerCase());
                }
                if (use) {
                    setmap.put(memb, item);
                }
                
            }
        }
        return setmap;
    }
    
    /**
     * Build map from member names to fields. This includes all non-static and non-transient fields of the class.
     * 
     * @param fields
     * @param prefs prefixes to be stripped in deriving names
     * @param suffs suffixes to be stripped in deriving names
     * @param inclset set of member names to be included (<code>null</code> if not specified)
     * @param exclset set of member names to be excluded (<code>null</code> if not specified, ignored if inclset is
     * non-<code>null</code>)
     * @return map
     */
    private Map mapFields(IClassItem[] fields, String[] prefs, String[] suffs, Set inclset, Set exclset) {
        
        // check all fields for use as members
        InsertionOrderedMap fieldmap = new InsertionOrderedMap();
        for (int i = 0; i < fields.length; i++) {
            IClassItem item = fields[i];
            if ((item.getAccessFlags() & (Modifier.FINAL | Modifier.STATIC | Modifier.TRANSIENT)) == 0) {
                
                // not final, static, or transient, so check if it should be used
                String name = item.getName();
                String memb = ValueCustom.memberNameFromField(name, prefs, suffs);
                boolean use = true;
                if (inclset != null) {
                    use = inclset.contains(memb.toLowerCase());
                } else if (exclset != null) {
                    use = !exclset.contains(memb.toLowerCase());
                }
                if (use) {
                    fieldmap.put(memb, item);
                }
            }
        }
        return fieldmap;
    }
    
    /**
     * Find the most specific type for a property based on the access methods.
     * 
     * @param gmeth read access method (<code>null</code> if not defined)
     * @param smeth write access method (<code>null</code> if not defined)
     * @param icl
     * @return most specific type name
     */
    private String findPropertyType(IClassItem gmeth, IClassItem smeth, IClassLocator icl) {
        String type;
        if (gmeth == null) {
            if (smeth == null) {
                throw new IllegalArgumentException("Internal error: no access methods known");
            } else {
                type = smeth.getArgumentType(0);
            }
        } else if (smeth == null) {
            type = gmeth.getTypeName();
        } else {
            String gtype = gmeth.getTypeName();
            String stype = smeth.getArgumentType(0);
            IClass gclas = icl.getRequiredClassInfo(gtype);
            if (gclas.isSuperclass(stype) || gclas.isImplements(stype)) {
                type = gtype;
            } else {
                type = stype;
            }
        }
        return type;
    }
    
    /**
     * Utility method to strip any leading non-alphanumeric characters from an array of name strings.
     * 
     * @param names (<code>null</code> if none)
     * @return array of stripped names (<code>null</code> if none)
     */
    private static String[] stripNames(String[] names) {
        if (names == null) {
            return null;
        } else {
            String[] strips = null;
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                int index = 0;
                while (index < name.length()) {
                    char chr = name.charAt(index);
                    if (Character.isLetter(chr) || Character.isDigit(chr)) {
                        break;
                    } else {
                        index++;
                    }
                }
                if (index > 0) {
                    if (strips == null) {
                        strips = new String[names.length];
                        System.arraycopy(names, 0, strips, 0, names.length);
                    }
                    strips[i] = name.substring(index);
                }
            }
            return (strips == null) ? names : strips;
        }
    }
    
    /**
     * Classify an array of names as elements or attributes, based on leading flag characters ('@' for an attribute,
     * '<' for an element).
     *
     * @param names (<code>null</code> if none)
     * @param elems set of element names
     * @param attrs set of attribute names
     */
    private static void classifyNames(String[] names, Set elems, Set attrs) {
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                if (name.length() > 0) {
                    char chr = name.charAt(0);
                    Set addset = null;
                    Set altset = null;
                    if (chr == '@') {
                        addset = attrs;
                        altset = elems;
                    } else if (chr == '/') {
                        addset = elems;
                        altset = attrs;
                    }
                    if (addset != null) {
                        name = name.substring(1).toLowerCase();
                        addset.add(name);
                        if (altset.contains(name)) {
                            throw new IllegalArgumentException("Name " + name + " used as both element and attribute");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Apply customizations to class to fill out members. The name handling gets tricky, because of the variety of
     * options provided. The user can specify value names to be included and/or excluded for the class, and also value
     * names to be treated as optional or required. If an 'includes' list is given, only the values on that list are
     * processed from the class, along with any values with their own child elements. If an 'excludes' list is given,
     * only the names not on that list are processed. If neither an 'includes' nor an 'excludes' list is present, all
     * values defined by the class will be processed. It is an error if the same name occurs on both an 'excludes' list
     * and any other list, or if a name on the 'excludes' list has a child element present. It is also an error if the
     * same name occurs on both the 'optionals' and 'requireds' list. Each list name can also be flagged with a leading
     * indicator character to say whether the value should be represented as an attribute ('@') or element ('<'). The
     * order of child elements is also partially determined by the lists, with the 'includes' list processed first, then
     * the 'requireds' list, then the 'optionals' list, then the child elements, then any values not yet processed.
     * 
     * @param icl class locator
     */
    public void apply(IClassLocator icl) {
        
        // check for repeated call (happens due to package-level apply()
        if (m_isApplied) {
            return;
        }
        m_isApplied = true;
        
        // initialize class information
        m_classInformation = icl.getClassInfo(getName());
        if (m_classInformation == null) {
            throw new IllegalStateException("Internal error: unable to find class " + m_name);
        }
        
        // skip any member handling if simple value
        if (m_form == FORM_STRING) {
            return;
        }
        
        // inherit namespace directly from package level, if not specified
        String ns = getSpecifiedNamespace();
        if (ns == null) {
            ns = getParent().getNamespace();
        }
        setNamespace(ns);
        
        // set the name(s) to be used if mapped
        String cname = convertName(getSimpleName());
        if (m_elementName == null) {
            m_elementName = cname;
        }
        m_elementQName = new QName(getNamespace(), m_elementName);
        if (m_typeName == null) {
            m_typeName = cname;
        }
        m_typeQName = new QName(getNamespace(), m_typeName);
        
        // initialize maps with existing member customizations
        HashMap namemap = new HashMap();
        for (Iterator iter = getChildren().iterator(); iter.hasNext();) {
            ValueCustom memb = (ValueCustom)iter.next();
            String name = memb.getBaseName();
            if (name != null) {
                namemap.put(name, memb);
            }
        }
        
        // generate sets of names to be included or ignored, and optional/requires, elements/attributes
        InsertionOrderedSet nameset = new InsertionOrderedSet();
        Set exclset = null;
        Set inclset = null;
        Set elemset = Collections.EMPTY_SET;
        Set attrset = Collections.EMPTY_SET;
        Set optset = Collections.EMPTY_SET;
        Set reqset = Collections.EMPTY_SET;
        if (m_includes != null || m_optionals != null || m_requireds != null) {
            
            // initialize the ordered set of names to be included
            String[] optionals = stripNames(m_optionals);
            String[] requireds = stripNames(m_requireds);
            if (m_includes != null) {
                nameset.addAll(stripNames(m_includes));
            } else {
                if (requireds != null) {
                    nameset.addAll(requireds);
                }
                if (optionals != null) {
                    nameset.addAll(optionals);
                }
            }
            
            // build sets of includeds, optionals, and requireds
            inclset = CustomUtils.noCaseNameSet(stripNames(m_includes));
            if (optionals != null) {
                optset = CustomUtils.noCaseNameSet(optionals);
            }
            if (requireds != null) {
                reqset = CustomUtils.noCaseNameSet(requireds);
            }
            
            // classify names as elements or attributes
            elemset = new HashSet();
            attrset = new HashSet();
            classifyNames(m_requireds, elemset, attrset);
            classifyNames(m_optionals, elemset, attrset);
            classifyNames(m_includes, elemset, attrset);
            
        }
        if (m_excludes != null) {
            exclset = CustomUtils.noCaseNameSet(stripNames(m_excludes));
        }
        
        // first find members from property access methods
        Map getmap = null;
        Map setmap = null;
        IClassItem[] methods = m_classInformation.getMethods();
        GlobalCustom global = getGlobal();
        if (global.isOutput()) {
            
            // find properties using read access methods
            getmap = mapPropertyReadMethods(methods, inclset, exclset);
            if (global.isInput()) {
                
                // find properties using write access methods
                setmap = mapPropertyWriteMethods(methods, inclset, exclset);
                
                // discard any read-only properties
                for (Iterator iter = getmap.keySet().iterator(); iter.hasNext();) {
                    if (!setmap.containsKey(iter.next())) {
                        iter.remove();
                    }
                }
                
            }
        }
        if (global.isInput()) {
            
            // find properties using write access methods
            setmap = mapPropertyWriteMethods(methods, inclset, exclset);
            
        }
        
        // find members from fields
        IClassItem[] fields = m_classInformation.getFields();
        Map fieldmap = mapFields(fields, getStripPrefixes(), getStripSuffixes(), inclset, exclset);
        
        // get list of names selected for use by options
        if (m_includes == null) {
            if (isPropertyAccess()) {
                if (global.isOutput()) {
                    nameset.addAll(getmap.keySet());
                } else {
                    nameset.addAll(setmap.keySet());
                }
            } else {
                for (Iterator iter = fieldmap.keySet().iterator(); iter.hasNext();) {
                    String name = (String)iter.next();
                    IClassItem field = (IClassItem)fieldmap.get(name);
                    int access = field.getAccessFlags();
                    if (!Modifier.isStatic(access) && !Modifier.isTransient(access)) {
                        nameset.add(name);
                    }
                }
            }
        }
        
        // process all members found in class
        m_memberMap = new InsertionOrderedMap();
        boolean auto = !getName().startsWith("java.") && !getName().startsWith("javax.");
        List names = nameset.asList();
        for (Iterator iter = names.iterator(); iter.hasNext();) {
            
            // get basic member information
            String name = (String)iter.next();
            String lcname = name.toLowerCase();
            ValueCustom cust = null;
            IClassItem gmeth = (IClassItem)(getmap == null ? null : getmap.get(name));
            IClassItem smeth = (IClassItem)(setmap == null ? null : setmap.get(name));
            IClassItem field = (IClassItem)(fieldmap == null ? null : fieldmap.get(name));
            
            // find the optional/required setting
            Boolean isreq = null;
            if (optset.contains(lcname)) {
                isreq = Boolean.FALSE;
            }
            if (reqset.contains(lcname)) {
                isreq = Boolean.TRUE;
            }
            
            // find the style setting
            Integer style = null;
            if (attrset.contains(lcname)) {
                style = ATTRIBUTE_STYLE_INTEGER;
            } else if (elemset.contains(lcname)) {
                style = ELEMENT_STYLE_INTEGER;
            }
            
            // check for existing customization
            if (namemap.containsKey(name)) {
                
                // fill in data missing from existing member customization
                cust = (ValueCustom)namemap.get(name);
                
            } else if (auto) {
                if (isPropertyAccess()) {
                    if (gmeth != null || smeth != null) {
                        
                        // create value information from methods
                        cust = new ValueCustom(this, name);
                        
                    }
                } else if (field != null) {
                    
                    // create value information from field
                    cust = new ValueCustom(this, name);
                    
                }
            }
            
            // add customization to map
            if (cust != null) {
                if (isPropertyAccess()) {
                    cust.fillDetails(null, gmeth, smeth, icl, isreq, style);
                } else {
                    cust.fillDetails(field, null, null, icl, isreq, style);
                }
                m_memberMap.put(name, cust);
            }
        }
        
        // check for any supplied customizations that haven't been matched
        for (Iterator iter = namemap.keySet().iterator(); iter.hasNext();) {
            String name = (String)iter.next();
            String lcname = name.toLowerCase();
            if (!m_memberMap.containsKey(name)) {
                
                // find the optional/required setting
                Boolean req = null;
                if (optset != null && optset.contains(lcname)) {
                    req = Boolean.FALSE;
                }
                if (reqset != null && reqset.contains(lcname)) {
                    req = Boolean.TRUE;
                }
                
                // find the style setting
                Integer style = null;
                if (attrset.contains(lcname)) {
                    style = ATTRIBUTE_STYLE_INTEGER;
                } else if (elemset.contains(lcname)) {
                    style = ELEMENT_STYLE_INTEGER;
                }
                
                // complete the customization and add to map
                ValueCustom cust = (ValueCustom)namemap.get(name);
                cust.fillDetails(m_classInformation, req, style);
                m_memberMap.put(name, cust);
                
            }
        }
    }
    
    /**
     * Get customization information for a member by name. This method may only be called after
     * {@link #apply(IClassLocator)}.
     * 
     * @param name
     * @return customization, or <code>null</code> if none
     */
    public ValueCustom getMember(String name) {
        return (ValueCustom)m_memberMap.get(name);
    }
    
    /**
     * Get actual class information. This method may only be called after {@link #apply(IClassLocator)}.
     * 
     * @return class information
     */
    public IClass getClassInformation() {
        return m_classInformation;
    }
    
    /**
     * Get collection of members in class.
     * 
     * @return members
     */
    public Collection getMembers() {
        return m_memberMap.values();
    }
}
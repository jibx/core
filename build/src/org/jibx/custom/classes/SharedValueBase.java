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

import org.jibx.binding.classes.ClassItem;
import org.jibx.util.IClass;
import org.jibx.util.StringArray;

/**
 * Base class for all value customization information. This includes inherited values shared with customization
 * extensions (in particular, the WSDL extensions).
 * 
 * TODO: should this include more of the WSDL ValueCustom extensions? Look into how type mappings are handled in the
 * BindGen code (uses bound type in WSDL)
 * 
 * @author Dennis M. Sosnoski
 */
public class SharedValueBase extends CustomBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "actual-type", "create-type", "factory", "item-name", "item-type", "required" });
    
    //
    // Internal instance data
    
    /** Value name as used in code. */
    private String m_baseName;
    
    /** Element representation forced flag. */
    private boolean m_elementForced;
    
    /** Stated type, as used in code. */
    private String m_statedType;
    
    /** Type used when working with the value (actual type from customization, if supplied, or stated type). */
    private String m_workingType;
    
    /** Type of item values in collection. */
    private String m_itemWorkingType;
    
    /** Name for item elements in collection. */
    private String m_itemWorkingName;
    
    /** Primitive value flag. */
    private boolean m_primitive;
    
    /** Repeated value flag. */
    private boolean m_collection;
    
    //
    // Values handled by binding
    
    /** Style used for representation (<code>null</code> if unspecified and derived from type). */
    private Integer m_style;
    
    /** Element or attribute name from customization (<code>null</code> if none). */
    private String m_xmlName;
    
    /** 'actual-type' attribute value (<code>null</code> if none). */
    private String m_actualType;
    
    /** 'create-type' attribute value (<code>null</code> if none). */
    private String m_createType;
    
    /** 'factory' attribute value (<code>null</code> if none). */
    private String m_factoryMethod;
    
    /** 'required' attribute value (<code>null</code> if none). */
    private Boolean m_required;
    
    /** 'item-type' attribute value (<code>null</code> if none). */
    private String m_itemType;
    
    /** 'item-name' attribute value (<code>null</code> if none). */
    private String m_itemName;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    protected SharedValueBase(SharedNestingBase parent) {
        super(parent);
    }
    
    /**
     * Constructor with name known.
     * 
     * @param parent
     * @param name
     */
    protected SharedValueBase(SharedNestingBase parent, String name) {
        this(parent);
        m_baseName = name;
    }
    
    /**
     * Convenience method to access the containing class customization element.
     *
     * @return class customization
     */
    public ClassCustom getClassCustom() {
        return (ClassCustom)getParent();
    }
    
    /**
     * Get value name as used in code. This is the actual name with any prefix or suffix stripped and the initial letter
     * converted to lowercase unless the second letter is uppercase.
     * 
     * @return name
     */
    public String getBaseName() {
        return m_baseName;
    }
    
    /**
     * Set value name as used in code. This is only for use by subclasses.
     * 
     * @param name
     * @see SharedValueBase#getBaseName()
     */
    protected void setBaseName(String name) {
        m_baseName = name;
    }
    
    /**
     * Get stated type of value, as declared in code.
     * 
     * @return stated type
     */
    public String getStatedType() {
        return m_statedType;
    }
    
    /**
     * Get working type of member. This is the actual type from customization, if supplied, or stated type
     * 
     * @return working type
     */
    public String getWorkingType() {
        return m_workingType;
    }
    
    /**
     * Convert case of member name derived from name used in code. If the supplied name starts with an uppercase letter
     * followed by a lowercase letter, the initial letter is converted to lowercase in order to obtain a standard form
     * of the name.
     * 
     * @param name
     * @return converted name
     */
    public static String convertMemberNameCase(String name) {
        if (name.length() > 0) {
            char lead = name.charAt(0);
            if (name.length() > 1) {
                if (Character.isUpperCase(lead) && Character.isLowerCase(name.charAt(1))) {
                    StringBuffer buff = new StringBuffer(name);
                    buff.setCharAt(0, Character.toLowerCase(lead));
                    name = buff.toString();
                }
            } else {
                name = name.toLowerCase();
            }
        }
        return name;
    }
    
    /**
     * Get style code to apply to value.
     * 
     * @return value from {@link NestingBase#s_valueStyleEnum} enumeration
     */
    public int getStyle() {
        if (m_style == null) {
            return ((NestingBase)getParent()).getValueStyle(m_workingType);
        } else {
            return m_style.intValue();
        }
    }
    
    /**
     * Set style code to apply to value. This method is only intended for use by subclasses.
     * 
     * @param style
     */
    protected void setStyle(Integer style) {
        m_style = style;
    }
    
    /**
     * Get XML element or attribute name from customization.
     * 
     * @return name (<code>null</code> if none)
     */
    public String getXmlName() {
        return m_xmlName;
    }
    
    /**
     * Set XML element or attribute name from customization. This method is only intended for use by subclasses.
     * 
     * @param name
     */
    public void setXmlName(String name) {
        m_xmlName = name;
    }
    
    /**
     * Get 'actual-type' attribute value.
     * 
     * @return member actual type (<code>null</code> if none)
     */
    public String getActualType() {
        return m_actualType;
    }
    
    /**
     * Get 'create-type' attribute value.
     * 
     * @return type used for creating new instance (<code>null</code> if none)
     */
    public String getCreateType() {
        return m_createType;
    }
    
    /**
     * Get 'factory' attribute value.
     * 
     * @return method used for creating new instance (<code>null</code> if none)
     */
    public String getFactoryMethod() {
        return m_factoryMethod;
    }
    
    /**
     * Check if value is required.
     * 
     * @return <code>true</code> if required, <code>false</code> if not
     */
    public boolean isRequired() {
        if (m_required == null) {
            if (m_primitive) {
                return getParent().isPrimitiveRequired(m_workingType);
            } else {
                return getParent().isObjectRequired(m_workingType);
            }
        } else {
            return m_required.booleanValue();
        }
    }
    
    /**
     * Check if element required. This is really only relevant when the value represents a collection of child elements,
     * since it means a wrapper element is needed.
     *
     * @return <code>true</code> if element required, <code>false</code> if not
     */
    public boolean isElementForced() {
        return m_elementForced;
    }
    
    /**
     * Set element required. This method is only intended for use by subclasses.
     */
    protected void setElementForced() {
        m_elementForced = true;
    }
    
    /**
     * Style get text method. This is intended for use during marshalling. TODO: add validation
     * 
     * @return text
     */
    private String getStyleText() {
        if (m_style == null) {
            return null;
        } else {
            return NestingBase.s_valueStyleEnum.getName(m_style.intValue());
        }
    }
    
    /**
     * Check if collection member.
     * 
     * @return <code>true</code> if collection, <code>false</code> if not
     */
    public boolean isCollection() {
        return m_collection;
    }
    
    /**
     * Get item type.
     * 
     * @return item type
     */
    public String getItemType() {
        return m_itemWorkingType;
    }
    
    /**
     * Set item type. This method is intended only for use by subclasses.
     * 
     * @param type
     */
    protected void setItemType(String type) {
        m_itemWorkingType = type;
    }
    
    /**
     * Get item element name.
     * 
     * @return item name
     */
    public String getItemName() {
        return m_itemWorkingName;
    }
    
    /**
     * Set item name. This method is intended only for use by subclasses.
     * 
     * @param name
     */
    protected void setItemName(String name) {
        m_itemWorkingName = name;
    }
    
    /**
     * Complete customization information based on supplied type. If the type information has not previously been set,
     * this will set it. It will also derive the appropriate XML name, if not previously set. This method is only
     * intended for use by subclasses.
     * 
     * @param info value type information
     * @param req required member flag (<code>null</code> if unspecified)
     * @param style representation style (<code>null</code> if unspecified)
     */
    protected void fillType(IClass info, Boolean req, Integer style) {
        String type = info.getName();
        m_statedType = type;
        if (m_actualType == null) {
            m_workingType = type;
        } else {
            m_workingType = m_actualType;
        }
        if (m_xmlName == null) {
            m_xmlName = getParent().convertName(m_baseName);
        }
        m_collection = type.endsWith("[]") || info.isImplements("Ljava/util/Collection;");
        m_primitive = ClassItem.isPrimitive(m_workingType);
        // TODO: check consistency of setting
        if (m_required == null) {
            m_required = req;
        }
        if (m_style == null) {
            m_style = style;
            if (style != null && style.intValue() == NestingBase.ELEMENT_VALUE_STYLE) {
                m_elementForced = true;
            }
        }
        if (!m_primitive && m_createType == null && m_factoryMethod == null) {
            ClassCustom cust = getGlobal().getClassCustomization(m_workingType);
            if (cust != null) {
                m_createType = cust.getCreateType();
                m_factoryMethod = cust.getFactoryMethod();
            }
        }
        if (isCollection()) {
            m_itemWorkingType = m_itemType;
            m_itemWorkingName = m_itemName;
        }
    }
}
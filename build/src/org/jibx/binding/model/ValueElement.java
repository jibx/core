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

package org.jibx.binding.model;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.QName;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.StringArray;

/**
 * Model component for <b>value</b> element. This element defines a value that
 * can be represented as a simple text string, which may be expressed as an
 * attribute, element, or text component of the XML document.
 *
 * @author Dennis M. Sosnoski
 */
public class ValueElement extends ElementBase implements IComponent
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "constant", "format", "ident",
        "nillable", "style" },
        new StringArray(new StringArray
        (NameAttributes.s_allowedAttributes, 
        PropertyAttributes.s_allowedAttributes),
        StringAttributes.s_allowedAttributes));
    
    //
    // Value set information
	
    public static final int CDATA_STYLE = 2;
    public static final int TEXT_STYLE = 3;
    
    private static final EnumSet s_styleEnum =
        new EnumSet(NestingAttributes.s_styleEnum, CDATA_STYLE,
        new String[] { "cdata", "text" });
    
    public static final int NONE_IDENT = 0;
    public static final int DEF_IDENT = 1;
    public static final int REF_IDENT = 2;
/*    public static final int AUTO_IDENT = 3;   */
    
    /*package*/ static final EnumSet s_identEnum = new EnumSet(NONE_IDENT,
        new String[] { "none", "def", "ref"/*, "auto"*/ });
	
	//
	// Instance data
    
    /** Supplied constant value. */
    private String m_constantValue;
    
    /** Supplied style name. */
    private String m_styleName;
    
    /** Actual selected style. */
    private int m_styleIndex;
    
    /** Supplied identity name. */
    private String m_identName = s_identEnum.getName(NONE_IDENT);
    
    /** Nillable object flag. */
    private boolean m_isNillable;
    
    /** Actual selected identity. */
    private int m_identIndex;
	
    /** Name attributes information for value. */
    private NameAttributes m_nameAttrs;
	
    /** Property attributes information for value. */
    private PropertyAttributes m_propertyAttrs;
    
    /** String attributes information for value. */
    private StringAttributes m_stringAttrs;
    
    /**
     * Constructor.
     */
    public ValueElement() {
        super(VALUE_ELEMENT);
        m_nameAttrs = new NameAttributes();
        m_propertyAttrs = new PropertyAttributes();
        m_stringAttrs = new StringAttributes();
    }
    
    /**
     * Get constant value.
     * 
     * @return constant value, or <code>null</code> if not a constant
     */
    public String getConstantValue() {
        return m_constantValue;
    }
    
    /**
     * Set constant value.
     * 
     * @param value constant value, or <code>null</code> if not a constant
     */
    public void setConstantValue(String value) {
        m_constantValue = value;
    }
    
    /**
     * Get style string value.
     * 
     * @return style string value
     */
    public String getStyleName() {
        return m_styleName;
    }
    
    /**
     * Get style value. This call is only meaningful after validation.
     * 
     * @return style value
     */
    public int getStyle() {
        return m_styleIndex;
    }
    
    /**
     * Set style name.
     * 
     * @param name style name (<code>null</code> if to use inherited default)
     */
    public void setStyleName(String name) {
        m_styleName = name;
    }
    
    /**
     * Get name for style that applies to this value. This call is only
     * meaningful after validation.
     * 
     * @return name for style
     */
    public String getEffectiveStyleName() {
        return s_styleEnum.getName(m_styleIndex);
    }
    
    /**
     * Set style that applies to this value. If the specified style is different
     * from the nested default it is applied directly, otherwise this value is
     * configured to use the default. This method should therefore only be used
     * when the nested settings are considered fixed.
     * TODO: implement this with parent links
     * 
     * @param style style value
     */
    public void setEffectiveStyle(int style) {
        m_styleIndex = style;
        m_styleName = s_styleEnum.getName(style);
    }
    
    /**
     * Get identity string value.
     * 
     * @return identity string value
     */
    public String getIdentName() {
        return m_identName;
    }
    
    /**
     * Get identity value. This call is only meaningful after validation.
     * 
     * @return identity value
     */
    public int getIdent() {
        return m_identIndex;
    }
    
    /**
     * Set identity name.
     * 
     * @param name identity name
     */
    public void setIdentName(String name) {
        m_identName = name;
    }
    
    //
    // Name attribute delegate methods
    
    /**
     * Get name.
     * 
     * @return name text
     */
    public String getName() {
        return m_nameAttrs.getName();
    }
    
    /**
     * Set name.
     * 
     * @param name text for name
     */
    public void setName(String name) {
        m_nameAttrs.setName(name);
    }

    /**
     * Get specified namespace URI.
     * 
     * @return namespace URI (<code>null</code> if not set)
     */
    public String getUri() {
        return m_nameAttrs.getUri();
    }

    /**
     * Set namespace URI.
     * 
     * @param uri namespace URI (<code>null</code> if not set)
     */
    public void setUri(String uri) {
        m_nameAttrs.setUri(uri);
    }

    /**
     * Get specified namespace prefix.
     * 
     * @return namespace prefix (<code>null</code> if not set)
     */
    public String getPrefix() {
        return m_nameAttrs.getPrefix();
    }

    /**
     * Set namespace prefix.
     * 
     * @param prefix namespace prefix (<code>null</code> if not set)
     */
    public void setPrefix(String prefix) {
        m_nameAttrs.setPrefix(prefix);
    }
    
    /**
     * Get effective namespace information. This call is only meaningful after
     * validation.
     * 
     * @return effective namespace information
     */
    public NamespaceElement getNamespace() {
        return m_nameAttrs.getNamespace();
    }
    
    //
    // Property attribute delegate methods
    
    /**
     * Get usage name.
     * 
     * @return usage name
     */
    public String getUsageName() {
        return m_propertyAttrs.getUsageName();
    }
    
    /**
     * Get usage value. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return usage value
     */
    public int getUsage() {
        return m_propertyAttrs.getUsage();
    }
    
    /**
     * Set usage name.
     * 
     * @param name usage name
     */
    public void setUsageName(String name) {
        m_propertyAttrs.setUsageName(name);
    }
    
    /**
     * Set usage value.
     * 
     * @param use value
     */
    public void setUsage(int use) {
        m_propertyAttrs.setUsage(use);
    }
    
    /**
     * Check if property is defined. This method is only meaningful after
     * a call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return <code>true</code> if property defined, <code>false</code> if not
     */
    public boolean hasProperty() {
        return m_propertyAttrs.hasProperty();
    }
    
    /**
     * Get declared type name.
     * 
     * @return type name (or <code>null</code> if none)
     */
    public String getDeclaredType() {
        return m_propertyAttrs.getDeclaredType();
    }
    
    /**
     * Set declared type name.
     * 
     * @param type name (or <code>null</code> if none)
     */
    public void setDeclaredType(String type) {
        m_propertyAttrs.setDeclaredType(type);
    }
    
    /**
     * Get field name.
     * 
     * @return field name (or <code>null</code> if none)
     */
    public String getFieldName() {
        return m_propertyAttrs.getFieldName();
    }
    
    /**
     * Get field information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return field information (or <code>null</code> if none)
     */
    public IClassItem getField() {
        return m_propertyAttrs.getField();
    }
    
    /**
     * Set field name.
     * 
     * @param field field name (or <code>null</code> if none)
     */
    public void setFieldName(String field) {
        m_propertyAttrs.setFieldName(field);
    }
    
    /**
     * Get test method name.
     * 
     * @return test method name (or <code>null</code> if none)
     */
    public String getTestName() {
        return m_propertyAttrs.getTestName();
    }
    
    /**
     * Get test method information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return test method information (or <code>null</code> if none)
     */
    public IClassItem getTest() {
        return m_propertyAttrs.getTest();
    }
    
    /**
     * Set test method name.
     * 
     * @param test test method name (or <code>null</code> if none)
     */
    public void setTestName(String test) {
        m_propertyAttrs.setTestName(test);
    }
    
    /**
     * Get get method name.
     * 
     * @return get method name (or <code>null</code> if none)
     */
    public String getGetName() {
        return m_propertyAttrs.getGetName();
    }
    
    /**
     * Get get method information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return get method information (or <code>null</code> if none)
     */
    public IClassItem getGet() {
        return m_propertyAttrs.getGet();
    }
    
    /**
     * Get type for value loaded to stack. This call is only meaningful after a
     * call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return get value type (or <code>null</code> if none)
     */
    public IClass getGetType() {
        return m_propertyAttrs.getGetType();
    }
    
    /**
     * Set get method name.
     * 
     * @param get get method name (or <code>null</code> if none)
     */
    public void setGetName(String get) {
        m_propertyAttrs.setGetName(get);
    }
    
    /**
     * Get set method name.
     * 
     * @return set method name (or <code>null</code> if none)
     */
    public String getSetName() {
        return m_propertyAttrs.getSetName();
    }
    
    /**
     * Get set method information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return set method information (or <code>null</code> if none)
     */
    public IClassItem getSet() {
        return m_propertyAttrs.getSet();
    }
    
    /**
     * Get type for value stored from stack. This call is only meaningful after
     * a call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return set value type (or <code>null</code> if none)
     */
    public IClass getSetType() {
        return m_propertyAttrs.getSetType();
    }
    
    /**
     * Set set method name.
     * 
     * @param set set method name (or <code>null</code> if none)
     */
    public void setSetName(String set) {
        m_propertyAttrs.setSetName(set);
    }
    
    /**
     * Check if nillable object.
     * 
     * @return nillable flag
     */
    public boolean isNillable() {
        return m_isNillable;
    }

    /**
     * Set nillable flag.
     * 
     * @param nillable flag
     */
    public void setNillable(boolean nillable) {
        m_isNillable = nillable;
    }
    
    /**
     * Check if this value implicitly uses the containing object. This call
     * is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return <code>true</code> if using the containing object,
     * <code>false</code> if own value
     */
    public boolean isImplicit() {
        return m_propertyAttrs.isImplicit();
    }
    
    //
    // String attribute delegate methods
    
    /**
     * Get default value text.
     * 
     * @return default value text
     */
    public String getDefaultText() {
        return m_stringAttrs.getDefaultText();
    }
    
    /**
     * Get default value. This call is only meaningful after validation.
     * 
     * @return default value object
     */
    public Object getDefault() {
        return m_stringAttrs.getDefault();
    }
    
    /**
     * Set default value text.
     * 
     * @param value default value text
     */
    public void setDefaultText(String value) {
        m_stringAttrs.setDefaultText(value);
    }
    
    /**
     * Get enum value method information. This method is only usable after a
     * call to {@link #validate(ValidationContext)}.
     * 
     * @return enum value method information (or <code>null</code> if none)
     */
    public IClassItem getEnumValue() {
        return m_stringAttrs.getEnumValue();
    }
    
    /**
     * Get enum value method name.
     * 
     * @return enum value method name (or <code>null</code> if none)
     */
    public String getEnumValueName() {
        return m_stringAttrs.getEnumValueName();
    }
    
    /**
     * Set enum value method name.
     * 
     * @param name enum value method name (<code>null</code> if none)
     */
    public void setEnumValueName(String name) {
        m_stringAttrs.setEnumValueName(name);
    }

    /**
     * Get base format name.
     * 
     * @return referenced base format
     */
    public String getFormatName() {
        return m_stringAttrs.getFormatName();
    }
    
    /**
     * Set base format name.
     * 
     * @param name referenced base format
     */
    public void setFormatName(String name) {
        m_stringAttrs.setFormatName(name);
    }
    
    /**
     * Get format qualified name.
     * 
     * @return format qualified name (<code>null</code> if none)
     */
    public QName getFormatQName() {
        return m_stringAttrs.getFormatQName();
    }
    
    /**
     * Set format qualified name. This method changes the label value to match
     * the qualified name.
     * 
     * @param qname format qualified name (<code>null</code> if none)
     */
    public void setFormatQName(QName qname) {
        m_stringAttrs.setFormatQName(qname);
    }
    
    /**
     * Get serializer name.
     * 
     * @return fully qualified class and method name for serializer (or
     * <code>null</code> if none)
     */
    public String getSerializerName() {
        return m_stringAttrs.getSerializerName();
    }
    
    /**
     * Get serializer method information. This call is only meaningful after
     * validation.
     * 
     * @return serializer information (or <code>null</code> if none)
     */
    public IClassItem getSerializer() {
        return m_stringAttrs.getSerializer();
    }
    
    /**
     * Set serializer method name.
     * 
     * @param name fully qualified class and method name for serializer
     */
    public void setSerializerName(String name) {
        m_stringAttrs.setSerializerName(name);
    }
    
    /**
     * Get deserializer name.
     * 
     * @return fully qualified class and method name for deserializer (or
     * <code>null</code> if none)
     */
    public String getDeserializerName() {
        return m_stringAttrs.getDeserializerName();
    }
    
    /**
     * Get deserializer method information. This call is only meaningful after
     * validation.
     * 
     * @return deserializer information (or <code>null</code> if none)
     */
    public IClassItem getDeserializer() {
        return m_stringAttrs.getDeserializer();
    }
    
    /**
     * Set deserializer method name.
     * 
     * @param name fully qualified class and method name for deserializer
     */
    public void setDeserializerName(String name) {
        m_stringAttrs.setDeserializerName(name);
    }
    
    //
    // IComponent implementation methods (also delegated name methods)

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#hasAttribute()
     */
    public boolean hasAttribute() {
        return m_styleIndex == NestingAttributes.ATTRIBUTE_STYLE;
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#hasContent()
     */
    public boolean hasContent() {
        return m_styleIndex != NestingAttributes.ATTRIBUTE_STYLE;
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#isOptional()
     */
    public boolean isOptional() {
        return m_propertyAttrs.getUsage() == PropertyAttributes.OPTIONAL_USAGE;
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#hasName()
     */
    public boolean hasName() {
        return m_nameAttrs.getName() != null;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#getType()
     */
    public IClass getType() {
        return m_propertyAttrs.getType();
    }
    
    //
    // Validation methods
    
    /**
     * Make sure all attributes are defined.
     *
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // set the style value
        if (m_styleName != null) {
            m_styleIndex = s_styleEnum.getValue(m_styleName);
            if (m_styleIndex < 0) {
                vctx.addError("Value \"" + m_styleName +
                    "\" is not a valid choice for style");
            }
        } else {
            m_styleIndex = vctx.getParentElement().getDefaultStyle();
        }
        
        // validate ID classification
        m_identIndex = s_identEnum.getValue(m_identName);
        if (m_identIndex < 0) {
            vctx.addError("Value \"" + m_identName +
                " is not a valid choice for ident");
        }
        
        // validate basic attribute groups
        m_propertyAttrs.prevalidate(vctx);
        m_nameAttrs.setIsAttribute
            (m_styleIndex == NestingAttributes.ATTRIBUTE_STYLE);
        m_nameAttrs.prevalidate(vctx);
        if (m_styleIndex == CDATA_STYLE || m_styleIndex == TEXT_STYLE) {
            if (m_nameAttrs.getName() != null) {
                vctx.addFatal("Values with \"text\" or \"cdata\" style " +
                    "cannot have names");
            }
        } else {
            if (m_nameAttrs.getName() == null) {
                vctx.addFatal
                    ("Missing required name for element or attribute value");
            }
        }
        
        // make sure value is not constant
        if (m_constantValue == null) {
            
            // make sure nillable only for optional element with object
            if (m_isNillable) {
                if (m_styleIndex != NestingAttributes.ELEMENT_STYLE) {
                    vctx.addFatal("nillable can only be used with element style");
                }
            }
            
            // process ID classification
            if (m_identIndex == DEF_IDENT/* || m_identIndex == AUTO_IDENT*/) {
                boolean valid = false;
                IClass gclas = m_propertyAttrs.getGetType();
                if (gclas != null) {
                    String gtype = gclas.getName();
                    if (gtype.equals("java.lang.String")) {
                        vctx.getContextObject().setIdChild(this, vctx);
                        valid = true;
                    }
                }
                if (!valid) {
                    vctx.addError("ID property must supply a " +
                        "java.lang.String value");
                }
            }
            
            // check string attributes only if valid to this point
            if (!vctx.isSkipped(this)) {
                
                // handle string attributes based on identity type
                if (m_identIndex == REF_IDENT) {
                    if (m_propertyAttrs.isImplicit()) {
                        vctx.addFatal
                            ("No property value - ID reference can only " +
                            "be used with a property of the appropriate type");
                    }
                    if (m_stringAttrs.getDeserializerName() != null ||
                        m_stringAttrs.getSerializerName() != null ||
                        m_stringAttrs.getFormatName() != null ||
                        m_stringAttrs.getDefaultText() != null) {
                        vctx.addWarning("String attributes serializer, " +
                            "deserializer, format, and default are " +
                            "prohibited with ID references");
                    }
                } else if (m_propertyAttrs.isFlagOnly()) {
                    if (m_stringAttrs.getDeserializerName() != null ||
                        m_stringAttrs.getSerializerName() != null ||
                        m_stringAttrs.getFormatName() != null ||
                        m_stringAttrs.getDefaultText() != null) {
                        vctx.addWarning("String attributes serializer, " +
                            "deserializer, format, and default are " +
                            "prohibited for flag value");
                    }
                } else {
                    m_stringAttrs.setType(m_propertyAttrs.getType());
                    m_stringAttrs.prevalidate(vctx);
                }
                super.prevalidate(vctx);
            }
            
        } else {
            
            // check prohibited attributes with constant value
            if (m_identIndex != NONE_IDENT) {
                vctx.addFatal("ident value must be \"none\" for constant");
            } else if (m_propertyAttrs.hasProperty() ||
                m_propertyAttrs.getDeclaredType() != null) {
                vctx.addFatal
                    ("Property attributes cannot be used with constant");
            } else if (m_stringAttrs.getDefaultText() != null ||
                m_stringAttrs.getDeserializerName() != null ||
                m_stringAttrs.getSerializerName() != null ||
                m_stringAttrs.getFormatName() != null) {
                vctx.addFatal("String attributes cannot be used with constant");
            } else if (m_isNillable) {
                vctx.addFatal("nillable cannot be used with constant");
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // validate basic attributes
        m_nameAttrs.validate(vctx);
        m_propertyAttrs.validate(vctx);
        if (!vctx.isSkipped(this)) {
            
            // check identity references for compatible objects
            if (m_identIndex == REF_IDENT) {
                String type = m_propertyAttrs.getType().getName();
                if (!vctx.getBindingRoot().isIdClass(type)) {
                    vctx.addError("No ID definitions for compatible type");
                }
            }
            super.validate(vctx);
        }
    }
}
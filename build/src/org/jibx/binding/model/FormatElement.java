/*
Copyright (c) 2004-2007, Dennis M. Sosnoski
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

import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.StringArray;

/**
 * Model component for <b>format</b> element. This element defines conversion to
 * and from simple unstructured text representations.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
 
public class FormatElement extends ElementBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "label", "type" },
        StringAttributes.s_allowedAttributes);
    
    /** Format label. */
    private String m_label;
    
    /** Format qualified name. */
    private QName m_qname;
    
    /** Default format for type flag. */
    private boolean m_isDefault;
    
    /** Name of value type. */
    private String m_typeName;
    
    /** Value type information. */
    private IClass m_type;
    
    /** String attributes information for value. */
    private StringAttributes m_stringAttrs;
    
    /**
     * Constructor.
     */
    public FormatElement() {
        super(FORMAT_ELEMENT);
        m_stringAttrs = new StringAttributes();
    }
    
    /**
     * Get format label.
     * 
     * @return format label (<code>null</code> if none)
     */
    public String getLabel() {
        return m_label;
    }
    
    /**
     * Set format label. This method changes the qualified name to match the
     * label.
     * 
     * @param label format label (<code>null</code> if none)
     */
    public void setLabel(String label) {
        m_label = label;
        m_qname = (label == null) ? null : new QName(label);
    }
    
    /**
     * Get format qualified name.
     * 
     * @return format qualified name (<code>null</code> if none)
     */
    public QName getQName() {
        return m_qname;
    }
    
    /**
     * Set format qualified name. This method changes the label value to match
     * the qualified name.
     * 
     * @param qname format qualified name (<code>null</code> if none)
     */
    public void setQName(QName qname) {
        m_qname = qname;
        m_label = (qname == null) ? null : qname.toString();
    }
    
    /**
     * Check if default format for type.
     * 
     * @return <code>true</code> if default for type, <code>false</code> if not
     */
    public boolean isDefaultFormat() {
        return m_isDefault;
    }
    
    /**
     * Set default format for type.
     * 
     * @param dflt <code>true</code> if default for type, <code>false</code> if
     * not
     */
    public void setDefaultFormat(boolean dflt) {
        m_isDefault = dflt;
    }
    
    /**
     * Get value type. This method is only usable after a
     * call to {@link #validate}.
     * 
     * @return default value object
     */
    public IClass getType() {
        return m_type;
    }
    
    /**
     * Get value type name.
     * 
     * @return value type name
     */
    public String getTypeName() {
        return m_typeName;
    }
    
    /**
     * Set value type name.
     * 
     * @param value type name
     */
    public void setTypeName(String value) {
        m_typeName = value;
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
    
    /**
     * Get base format information. This method is only usable after a
     * call to {@link #validate}.
     * 
     * @return base format element (or <code>null</code> if none)
     */
    public FormatElement getBaseFormat() {
        return m_stringAttrs.getBaseFormat();
    }
    
    //
    // Validation methods
    
    /**
     * JiBX access method to set format label as qualified name.
     * 
     * @param label format label text (<code>null</code> if none)
     * @param ictx unmarshalling context
     * @throws JiBXException on deserialization error
     */
    private void setQualifiedLabel(String label, IUnmarshallingContext ictx)
        throws JiBXException {
        setQName(QName.deserialize(label, ictx));
    }
    
    /**
     * JiBX access method to get format label as qualified name.
     * 
     * @param ictx marshalling context
     * @return format label text (<code>null</code> if none)
     * @throws JiBXException on deserialization error
     */
    private String getQualifiedLabel(IMarshallingContext ictx)
        throws JiBXException {
        return QName.serialize(getQName(), ictx);
    }
    
    /**
     * Make sure all attributes are defined.
     *
     * @param uctx unmarshalling context
     * @exception JiBXException on unmarshalling error
     */
    private void preSet(IUnmarshallingContext uctx) throws JiBXException {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Set default flag based on whether name supplied or not.
     * TODO: use explicit flag for 2.0
     */
    private void postSet() {
        m_isDefault = m_label == null;
    }
    
    /**
     * Prevalidate attributes of element in isolation. Note that this adds the
     * format information to the context, which is necessary because the string
     * attributes for values need to have access to the format information for
     * their own prevalidation. This is the only type of registration which is
     * done during the prevalidation pass.
     *
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {
        
        // prevalidate this format
        if (m_typeName != null) {
            m_type = vctx.getClassInfo(m_typeName);
            if (m_type != null) {
                
                // set the type information
                m_stringAttrs.setType(m_type);
                m_stringAttrs.prevalidate(vctx);
                
                // now add to context (except when run during setup; kludgy)
                if (vctx.getParentElement() != null) {
                    vctx.getFormatDefinitions().addFormat(this, vctx);
                }
                
            } else {
                vctx.addFatal("Unable to find type " + m_typeName);
            }
        } else {
            vctx.addFatal("Missing required type name");
        }
        super.prevalidate(vctx);
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        m_stringAttrs.validate(vctx);
        super.validate(vctx);
    }
}
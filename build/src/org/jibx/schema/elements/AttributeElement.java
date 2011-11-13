/*
Copyright (c) 2006-2010, Dennis M. Sosnoski. All rights reserved.

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

package org.jibx.schema.elements;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.schema.INamed;
import org.jibx.schema.IReference;
import org.jibx.schema.attributes.DefRefAttributeGroup;
import org.jibx.schema.attributes.FormChoiceAttribute;
import org.jibx.schema.support.Conversions;
import org.jibx.schema.support.QNameConverter;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base representation for both local and global <b>attribute</b> element
 * definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class AttributeElement extends AnnotatedBase implements INamed, IReference
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "default", "fixed", "type", "use" },
        DefRefAttributeGroup.s_allowedAttributes,
        FormChoiceAttribute.s_allowedAttributes,
        AnnotatedBase.s_allowedAttributes);
    
    //
    // Value set information
    
    public static final int OPTIONAL_USE = 0;
    public static final int PROHIBITED_USE = 1;
    public static final int REQUIRED_USE = 2;
    
    public static final EnumSet s_useValues = new EnumSet(OPTIONAL_USE,
        new String[] { "optional", "prohibited", "required"});
    
    //
    // Instance data
    
    /** Filtered list of inline type definition elements (zero or one only). */
    private final FilteredSegmentList m_inlineTypeList;
    
    /** Name or reference. */
    private DefRefAttributeGroup m_defRef;
    
    /** Form of name. */
    private FormChoiceAttribute m_formChoice;
    
    /** 'type' attribute value. */
    private QName m_type;
    
    /** 'use' attribute value type code. */
    private int m_useType;
    
    /** 'default' attribute value. */
    private String m_default;
    
    /** 'fixed' attribute value. */
    private String m_fixed;
    
    /** Attribute definition (from 'ref' attribute - <code>null</code> if
     none). */
    private AttributeElement m_refElement;
    
    /** Simple type definition (from 'type' attribute, or inline definition -
     <code>null</code> if none). */
    private CommonTypeDefinition m_typeDefinition;

    /** Qualified name. */
    private QName m_qname;

    /**
     * Constructor.
     */
    public AttributeElement() {
    	super(ATTRIBUTE_TYPE);
        m_inlineTypeList = new FilteredSegmentList(getChildrenWritable(),
            ELEMENT_MASKS[SIMPLETYPE_TYPE], this);
        m_defRef = new DefRefAttributeGroup(this);
        m_formChoice = new FormChoiceAttribute(this);
        m_useType = -1;
    }

    /**
     * Clear any type information. This method is only visible for internal use,
     * to be called in the process of setting new type information.
     */
    private void clearType() {
        m_inlineTypeList.clear();
        m_defRef.setRef(null);
        m_refElement = null;
        m_type = null;
        m_typeDefinition = null;
    }
    
    //
    // Base class overrides

    /* (non-Javadoc)
     * @see org.jibx.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
    
    //
    // Access methods
    
    /**
     * Get 'type' attribute value.
     * 
     * @return type (<code>null</code> if not set)
     */
    public QName getType() {
        return m_type;
    }
    
    /**
     * Set 'type' attribute value. Note that this method should only be used
     * prior to validation, since it will only set the type name and not link
     * the actual type information.
     * 
     * @param type (<code>null</code> if not set)
     */
    public void setType(QName type) {
        clearType();
        m_type = type;
    }
    
    /**
     * Get 'default' attribute value.
     * 
     * @return default (<code>null</code> if not set)
     */
    public String getDefault() {
        return m_default;
    }

    /**
     * Set the 'default' attribute value.
     * 
     * @param dflt (<code>null</code> if not set)
     */
    public void setDefault(String dflt) {
        m_default = dflt;
    }

    /**
     * Get 'fixed' attribute value.
     * 
     * @return fixed (<code>null</code> if not set)
     */
    public String getFixed() {
        return m_fixed;
    }

    /**
     * Set 'fixed' attribute value.
     * 
     * @param fixed (<code>null</code> if not set)
     */
    public void setFixed(String fixed) {
        m_fixed = fixed;
    }

    /**
     * Get 'use' attribute type code.
     * 
     * @return type code applied to this attribute
     */
    public int getUse() {
        return m_useType >= 0 ? m_useType : OPTIONAL_USE;
    }

    /**
     * Set 'use' attribute type code.
     * 
     * @param code (<code>-1</code> to unset)
     */
    public void setUse(int code) {
        if (code >= 0) {
            s_useValues.checkValue(code);
        }
        m_useType = code;
    }

    /**
     * Get 'use' attribute text.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getUseText() {
        return s_useValues.getName(m_useType);
    }

    /**
     * Set 'use' attribute text. This method is provided only for use when
     * unmarshalling.
     * 
     * @param text
     * @param ictx
     */
    private void setUseText(String text, IUnmarshallingContext ictx) {
        m_useType = Conversions.convertEnumeration(text, s_useValues,
            "use", ictx);
    }
    
    /**
     * Get qualified name set directly on attribute. This method is only usable
     * after prevalidation.
     * 
     * @return qname (<code>null</code> if a reference)
     */
    public QName getQName() {
        return m_qname;
    }
    
    /**
     * Get effective qualified name for attribute (whether defined directly, or
     * by reference). This method is only usable after prevalidation.
     * 
     * @return qname
     */
    public QName getEffectiveQName() {
        if (m_qname == null) {
            return m_defRef.getRef();
        } else {
            return m_qname;
        }
    }
    
    /**
     * Get the referenced attribute declaration. This method is only usable
     * after validation.
     *
     * @return referenced attribute declaration, or <code>null</code> if not a
     * reference
     */
    public AttributeElement getReference() {
        return m_refElement;
    }
    
    /**
     * Check if the attribute uses an inline type definition.
     *
     * @return <code>true</code> if inline, <code>false</code> if not
     */
    public boolean isInlineType() {
        return m_inlineTypeList.size() == 1;
    }
    
    /**
     * Get type definition. This returns the actual type definition for the
     * attribute, irrespective of whether the attribute uses an attribute
     * reference, a type reference, or an inline type definition. It is only
     * usable after validation.
     * 
     * @return type definition
     */
    public CommonTypeDefinition getTypeDefinition() {
        if (m_defRef.getRef() != null) {
            return m_refElement.getTypeDefinition();
        } else if (m_typeDefinition != null) {
            return m_typeDefinition;
        } else {
            throw new IllegalStateException("Internal error: no type definition");
        }
    }

    /**
     * Set type definition. If the supplied type definition is a global type it
     * is used by reference; if a local type it is used inline.
     *
     * @param def type definition
     */
    public void setTypeDefinition(CommonTypeDefinition def) {
        
        // clear existing type information and set new
        clearType();
        m_typeDefinition = def;
        
        // check form of type definition
        if (def.getQName() == null) {
            
            // embed inline type definition
            m_inlineTypeList.add(def);
            def.setParent(this);
            
        } else {
            
            // set type reference
            m_type = def.getQName();
        }
    }
    
    //
    // Delegated methods
    
    /**
     * Get 'name' attribute value.
     * 
     * @return name
     * @see org.jibx.schema.attributes.DefRefAttributeGroup#getName()
     */
    public String getName() {
        return m_defRef.getName();
    }

    /**
     * Get 'ref' attribute value.
     * 
     * @return ref
     * @see org.jibx.schema.attributes.DefRefAttributeGroup#getRef()
     */
    public QName getRef() {
        return m_defRef.getRef();
    }

    /**
     * Set 'name' attribute value.
     * 
     * @param name
     * @see org.jibx.schema.attributes.DefRefAttributeGroup#setName(java.lang.String)
     */
    public void setName(String name) {
        m_defRef.setName(name);
    }

    /**
     * Set 'ref' attribute value.
     * 
     * @param ref
     * @see org.jibx.schema.attributes.DefRefAttributeGroup#setRef(org.jibx.runtime.QName)
     */
    public void setRef(QName ref) {
        clearType();
        m_defRef.setRef(ref);
    }

    /**
     * Get 'form' attribute type code.
     * 
     * @return form
     * @see org.jibx.schema.attributes.FormChoiceAttribute#getForm()
     */
    public int getForm() {
        return m_formChoice.getForm();
    }

    /**
     * Get 'form' attribute name value.
     *  
     * @return form
     * @see org.jibx.schema.attributes.FormChoiceAttribute#getFormText()
     */
    public String getFormText() {
        return m_formChoice.getFormText();
    }

    /**
     * Set 'form' attribute type code.
     * 
     * @param type
     * @see org.jibx.schema.attributes.FormChoiceAttribute#setForm(int)
     */
    public void setForm(int type) {
        m_formChoice.setForm(type);
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // prevalidate the attributes
        m_defRef.prevalidate(vctx);
        m_formChoice.prevalidate(vctx);
        
        // patch qualified name with schema effective namespace
        SchemaElement schema = vctx.getCurrentSchema();
        String ens = schema.getEffectiveNamespace();
        QNameConverter.patchQNameNamespace(ens, m_type);
        
        // check whether global or local definition
        if (isGlobal()) {
            
            // make sure name is supplied
            if (getName() == null) {
                vctx.addError("The 'name' attribute is required for a global definition", this);
            } else {
                m_qname = new QName(ens, getName());
            }
            
            // make sure prohibited attributes are not present
            if (getRef() != null || getForm() != -1 || m_useType != -1) {
                vctx.addError("The 'ref', 'form', and 'use' attributes are prohibited for a global attribute definition", this);
            }
            
        } else {
            
            // make sure name or reference is supplied
            if (getName() == null && getRef() == null) {
                vctx.addError("Either a 'name' attribute or a 'ref' attribute is required for a local attribute definition", this);
            }
            
            // generate qname if name supplied
            if (getName() != null) {
                boolean def = schema.isAttributeQualifiedDefault();
                String uri = null;
                if (m_formChoice.isQualified(def)) {
                    uri = ens;
                }
                m_qname = new QName(uri, getName());
            }
        }
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // start with validating the attributes
        m_defRef.validate(vctx);
        m_formChoice.validate(vctx);
        
        // check type of definition
        QName ref = getRef();
        if (ref == null) {
            
            // set the type definition
            if (m_type == null) {
                
                // verify inline type definition
                if (m_inlineTypeList.size() == 1) {
                    m_typeDefinition = (CommonTypeDefinition)m_inlineTypeList.get(0);
                } else if (m_inlineTypeList.size() == 0) {
                    vctx.addWarning("No type defined", this);
                } else {
                    vctx.addFatal("Only one inline type definition allowed", this);
                }
                
            } else {
                
                // look up referenced type definition
                m_typeDefinition = vctx.findType(m_type);
                if (m_typeDefinition == null) {
                    vctx.addFatal("Referenced type '" + m_type + "' is not defined", this);
                }
            }
            
        } else {
            
            // make sure element reference is defined
            m_refElement = vctx.findAttribute(ref);
            if (m_refElement == null) {
                vctx.addFatal("Referenced attribute '" + ref + "' is not defined", this);
            }
            
            // check for any conflicting attributes
            if (m_type != null) {
                vctx.addError("'type' attribute not allowed with 'ref' attribute", this);
            }
            
        }
        
        // handle base class validation if still going
        if (!vctx.isSkipped(this)) {
            super.validate(vctx);
        }
    }
}
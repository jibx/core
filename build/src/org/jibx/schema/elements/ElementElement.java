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

package org.jibx.schema.elements;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.schema.IArity;
import org.jibx.schema.INamed;
import org.jibx.schema.IReference;
import org.jibx.schema.attributes.DefRefAttributeGroup;
import org.jibx.schema.attributes.FormChoiceAttribute;
import org.jibx.schema.attributes.OccursAttributeGroup;
import org.jibx.schema.support.QNameConverter;
import org.jibx.schema.types.AllEnumSet;
import org.jibx.schema.types.Count;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * &lt;element> element definition. The same code is used for both global and local element definitions, with the
 * differences checked during validation.
 * 
 * TODO: implement common base class for attribute and element?
 * 
 * @author Dennis M. Sosnoski
 */
public class ElementElement extends AnnotatedBase implements IArity, INamed, IReference
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes = new StringArray(new String[] { "abstract", "block",
        "default", "final", "fixed", "nillable", "substitutionGroup", "type" },
        DefRefAttributeGroup.s_allowedAttributes, FormChoiceAttribute.s_allowedAttributes,
        OccursAttributeGroup.s_allowedAttributes, AnnotatedBase.s_allowedAttributes);
    
    /** Mask bits for inline type definition child. */
    private long INLINE_TYPE_MASK = ELEMENT_MASKS[COMPLEXTYPE_TYPE] | ELEMENT_MASKS[SIMPLETYPE_TYPE];
    
    /** Mask bits for identity constraint children. */
    private long IDENTITY_CONSTRAINT_MASK = ELEMENT_MASKS[KEY_TYPE] | ELEMENT_MASKS[KEYREF_TYPE] |
        ELEMENT_MASKS[UNIQUE_TYPE];
    
    //
    // Value set information
    
    public static final int EXTENSION_BLOCK = 0;
    
    public static final int RESTRICTION_BLOCK = 1;
    
    public static final int SUBSTITUTION_BLOCK = 2;
    
    public static final EnumSet s_blockValues = new EnumSet(EXTENSION_BLOCK, new String[] { "extension", "restriction",
        "substitution" });
    
    public static final int EXTENSION_FINAL = 0;
    
    public static final int RESTRICTION_FINAL = 1;
    
    public static final EnumSet s_derivationValues = new EnumSet(EXTENSION_FINAL, new String[] { "extension",
        "restriction" });
    
    //
    // Instance data
    
    /** Filtered list of inline type definition elements (zero or one only). */
    private final FilteredSegmentList m_inlineTypeList;
    
    /** Filtered list of identity constraint elements (zero or more). */
    private final FilteredSegmentList m_identityConstraintList;
    
    /** Name or reference. */
    private DefRefAttributeGroup m_defRef;
    
    /** Form of name. */
    private FormChoiceAttribute m_formChoice;
    
    /** Occurs attribute group. */
    private OccursAttributeGroup m_occurs;
    
    /** 'type' attribute value. */
    private QName m_type;
    
    /** 'default' attribute value. */
    private String m_default;
    
    /** 'fixed' attribute value. */
    private String m_fixed;
    
    /** 'abstract' attribute value. */
    private boolean m_abstract;
    
    /** 'nillable' attribute value. */
    private boolean m_nillable;
    
    /** 'block' attribute value. */
    private AllEnumSet m_block;
    
    /** 'final' attribute value. */
    private AllEnumSet m_final;
    
    /** 'substitutionGroup' attribute information. */
    private QName m_substitutionGroup;
    
    /** Element definition (from 'ref' attribute - <code>null</code> if none). */
    private ElementElement m_refElement;
    
    /**
     * Complex or simple type definition (from 'type' attribute, or inline definition - <code>null</code> if none).
     */
    private CommonTypeDefinition m_typeDefinition;
    
    /** Qualified name (only defined after validation). */
    private QName m_qname;
    
    /**
     * Constructor.
     */
    public ElementElement() {
        super(ELEMENT_TYPE);
        m_inlineTypeList = new FilteredSegmentList(getChildrenWritable(), INLINE_TYPE_MASK, this);
        m_identityConstraintList = new FilteredSegmentList(getChildrenWritable(), IDENTITY_CONSTRAINT_MASK,
            m_inlineTypeList, this);
        m_defRef = new DefRefAttributeGroup(this);
        m_formChoice = new FormChoiceAttribute(this);
        m_occurs = new OccursAttributeGroup(this);
        m_block = new AllEnumSet(s_blockValues, "block");
        m_final = new AllEnumSet(s_derivationValues, "final");
    }
    
    /**
     * Clear any type information. This method is only visible for internal use, to be called in the process of setting
     * new type information.
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
    
    /*
     * (non-Javadoc)
     * 
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
     * Set 'type' attribute value. Note that this method should only be used prior to validation, since it will only set
     * the type name and not link the actual type information.
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
     * Check 'abstract' attribute value.
     * 
     * @return abstract attribute value
     */
    public boolean isAbstract() {
        return m_abstract;
    }
    
    /**
     * Set 'abstract' attribute value.
     * 
     * @param abs abstract attribute value
     */
    public void setAbstract(boolean abs) {
        m_abstract = abs;
    }
    
    /**
     * Check 'nillable' attribute value.
     * 
     * @return nillable attribute value (<code>null</code> if not set)
     */
    public boolean isNillable() {
        return m_nillable;
    }
    
    /**
     * Set 'nillable' attribute value.
     * 
     * @param nil nillable attribute value (<code>null</code> if not set)
     */
    public void setNillable(boolean nil) {
        m_nillable = nil;
    }
    
    /**
     * Get 'final' attribute.
     * 
     * @return final
     */
    public AllEnumSet getFinal() {
        return m_final;
    }
    
    /**
     * Get 'block' attribute.
     * 
     * @return block
     */
    public AllEnumSet getBlock() {
        return m_block;
    }
    
    /**
     * Get 'substitutionGroup' attribute value.
     * 
     * @return substitutionGroup (<code>null</code> if not set)
     */
    public QName getSubstitutionGroup() {
        return m_substitutionGroup;
    }
    
    /**
     * Set 'substitutionGroup' attribute value.
     * 
     * @param qname (<code>null</code> if not set)
     */
    public void setSubstitutionGroup(QName qname) {
        m_substitutionGroup = qname;
    }
    
    /**
     * Get the referenced element declaration. This method is only usable after validation.
     * 
     * @return referenced element definition, or <code>null</code> if not a reference
     */
    public ElementElement getReference() {
        return m_refElement;
    }
    
    /**
     * Get qualified name set directly on element. This method is only usable
     * after prevalidation.
     * 
     * @return qname (<code>null</code> if a reference)
     */
    public QName getQName() {
        return m_qname;
    }
    
    /**
     * Get effective qualified name for element (whether defined directly, or by
     * reference). This method is only usable after prevalidation.
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
     * Check if the element uses an inline type definition.
     * 
     * @return <code>true</code> if inline, <code>false</code> if not
     */
    public boolean isInlineType() {
        return m_inlineTypeList.size() == 1;
    }
    
    /**
     * Get type definition. This returns the actual type definition for the element, irrespective of whether the element
     * uses an element reference, a type reference, or an inline type definition. It is only usable after validation.
     * 
     * @return type definition (<code>null</code> if empty type definition)
     */
    public CommonTypeDefinition getTypeDefinition() {
        if (m_defRef.getRef() != null) {
            return m_refElement.getTypeDefinition();
        } else {
            return m_typeDefinition;
        }
    }
    
    /**
     * Set type definition (either inline, or as reference).
     * 
     * @param def inline type definition
     */
    public void setTypeDefinition(CommonTypeDefinition def) {
        
        // clear existing type information and set new
        clearType();
        m_typeDefinition = def;
        
        // check form of type definition
        if (def != null) {
            if (def.getQName() == null) {
                
                // embed inline type definition
                m_inlineTypeList.add(def);
                def.setParent(this);
                
            } else {
                
                // set type reference
                m_type = def.getQName();
            }
        }
    }

    /**
     * Get list of identity constraint child elements.
     *
     * @return list
     */
    public FilteredSegmentList getIdentityConstraintList() {
        return m_identityConstraintList;
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
    
    /**
     * Get 'maxOccurs' attribute value.
     * 
     * @return count
     * @see org.jibx.schema.attributes.OccursAttributeGroup#getMaxOccurs()
     */
    public Count getMaxOccurs() {
        return m_occurs.getMaxOccurs();
    }
    
    /**
     * Get 'minOccurs' attribute value.
     * 
     * @return count
     * @see org.jibx.schema.attributes.OccursAttributeGroup#getMinOccurs()
     */
    public Count getMinOccurs() {
        return m_occurs.getMinOccurs();
    }
    
    /**
     * Set 'maxOccurs' attribute value.
     * 
     * @param count
     * @see org.jibx.schema.attributes.OccursAttributeGroup#setMaxOccurs(org.jibx.schema.types.Count)
     */
    public void setMaxOccurs(Count count) {
        m_occurs.setMaxOccurs(count);
    }
    
    /**
     * Get 'maxOccurs' attribute value.
     * 
     * @param count
     * @see org.jibx.schema.attributes.OccursAttributeGroup#setMinOccurs(org.jibx.schema.types.Count)
     */
    public void setMinOccurs(Count count) {
        m_occurs.setMinOccurs(count);
    }
    
    //
    // Validation methods
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // prevalidate the attributes
        m_defRef.prevalidate(vctx);
        m_formChoice.prevalidate(vctx);
        m_occurs.prevalidate(vctx);
        
        // patch qualified names with schema effective namespace
        SchemaElement schema = vctx.getCurrentSchema();
        String ens = schema.getEffectiveNamespace();
        QNameConverter.patchQNameNamespace(ens, m_substitutionGroup);
        QNameConverter.patchQNameNamespace(ens, m_type);
        
        // check whether global or local definition
        if (isGlobal()) {
            
            // make sure name is supplied for global element
            if (getName() == null) {
                vctx.addError("The 'name' attribute is required for a global definition", this);
            } else {
                m_qname = new QName(ens, getName());
            }
            
            // make sure prohibited attributes are not present
            if (getRef() != null || getForm() != -1 || getMinOccurs() != null || getMaxOccurs() != null) {
                vctx.addError("The 'ref', 'form', 'minOccurs' and 'maxOccurs' attributes are prohibited for a global element definition", this);
            }
            
        } else {
            
            // make sure name or reference is supplied for local element
            if (getName() == null && getRef() == null) {
                vctx.addError(
                    "Either a 'name' attribute or a 'ref' attribute is required for a local element definition", this);
            }
            
            // generate qname if name supplied
            if (getName() != null) {
                boolean def = schema.isElementQualifiedDefault();
                String uri = null;
                if (m_formChoice.isQualified(def)) {
                    uri = ens;
                }
                m_qname = new QName(uri, getName());
            }
            
            // make sure prohibited attributes are not present
            if (getSubstitutionGroup() != null || getBlock().isPresent() || getFinal().isPresent()) {
                vctx.addError("The 'substitutionGroup', 'block', and 'final' attributes are prohibited for a local element definition", this);
            }
        }
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // start with validating the attributes
        m_defRef.validate(vctx);
        m_formChoice.validate(vctx);
        
        // check type of definition
        QName ref = getRef();
        if (ref != null) {
            
            // make sure element reference is defined
            m_refElement = vctx.findElement(ref);
            if (m_refElement == null) {
                vctx.addFatal("Referenced element '" + ref + "' is not defined", this);
            }
            
            // check for any conflicting attributes
            if (m_type != null) {
                vctx.addError("'type' attribute not allowed with 'ref' attribute", this);
            }
            
        } else {
            
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
        }
        
        // handle base class validation if still going
        if (!vctx.isSkipped(this)) {
            super.validate(vctx);
        }
    }
}
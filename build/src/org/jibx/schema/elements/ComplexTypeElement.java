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

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.types.AllEnumSet;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * <b>complexType</b> element definition.
 *
 * @author Dennis M. Sosnoski
 */
public class ComplexTypeElement extends CommonTypeDefinition
implements IComplexStructure
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "abstract", "block", "final", "mixed" },
        CommonTypeDefinition.s_allowedAttributes);
    
    /** Mask bits for content type child elements. */
    private long CONTENT_TYPE_MASK = ELEMENT_MASKS[COMPLEXCONTENT_TYPE] |
        ELEMENT_MASKS[SIMPLECONTENT_TYPE];
    
    /** Mask bits for content definition child elements. */
    private long CONTENT_DEFINITION_MASK = ELEMENT_MASKS[ALL_TYPE] |
        ELEMENT_MASKS[CHOICE_TYPE] | ELEMENT_MASKS[GROUP_TYPE] |
        ELEMENT_MASKS[SEQUENCE_TYPE];
    
    /** Mask bits for attribute child elements. */
    private long ATTRIBUTE_MASK = ELEMENT_MASKS[ATTRIBUTE_TYPE] |
        ELEMENT_MASKS[ATTRIBUTEGROUP_TYPE];
    
    /** Mask bits for attribute child elements. */
    private long ANYATTRIBUTE_MASK = ELEMENT_MASKS[ANYATTRIBUTE_TYPE];
    
    //
    // Instance data
    
    /** Filtered list of content type definition (simpleContent or
     complexContent, one only and only if no direct content definition). */
    private final FilteredSegmentList m_contentTypeList;
    
    /** Filtered list of direct content definition (group reference or
     compositor, one only, only if no content type). */
    private final FilteredSegmentList m_contentDefinitionList;
    
    /** Filtered list of attribute definitions (only if no content type). */
    private final FilteredSegmentList m_attributeList;
    
    /** Filtered list of anyAttribute definitions (zero or one, only if no
     content type). */
    private final FilteredSegmentList m_anyAttributeList;
    
    /** 'abstract' attribute value. */
    private Boolean m_abstract;
    
    /** 'mixed' attribute value. */
    private Boolean m_mixed;
    
    /** 'block' attribute value. */
    private AllEnumSet m_block;
    
    /** 'final' attribute value. */
    private AllEnumSet m_final;

    /**
     * Constructor.
     */
    public ComplexTypeElement() {
        super(COMPLEXTYPE_TYPE);
        m_contentTypeList = new FilteredSegmentList(getChildrenWritable(),
            CONTENT_TYPE_MASK, this);
        m_contentDefinitionList = new FilteredSegmentList(getChildrenWritable(),
            CONTENT_DEFINITION_MASK, m_contentTypeList, this);
        m_attributeList = new FilteredSegmentList(getChildrenWritable(),
            ATTRIBUTE_MASK, m_contentDefinitionList, this);
        m_anyAttributeList = new FilteredSegmentList(getChildrenWritable(),
            ANYATTRIBUTE_MASK, m_attributeList, this);
        m_block = new AllEnumSet(ElementElement.s_derivationValues,
            "block");
        m_final = new AllEnumSet(ElementElement.s_derivationValues,
            "final");
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
    
    /* (non-Javadoc)
     * @see org.jibx.schema.CommonTypeDefinition#isComplexType()
     */
    public boolean isComplexType() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.elements.CommonTypeDefinition#isPredefinedType()
     */
    public boolean isPredefinedType() {
        return false;
    }
    
    //
    // Access methods
    
    /**
     * Check 'abstract' attribute value.
     *
     * @return abstract
     */
    public boolean isAbstract() {
        return m_abstract != null && m_abstract.booleanValue();
    }
    
    /**
     * Get 'abstract' attribute value.
     *
     * @return abstract
     */
    public Boolean getAbstract() {
        return m_abstract;
    }
    
    /**
     * Set 'abstract' attribute value.
     *
     * @param abs
     */
    public void setAbstract(boolean abs) {
        m_abstract = abs ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /**
     * Check 'mixed' attribute value.
     *
     * @return mixed
     */
    public boolean isMixed() {
        return m_mixed != null && m_mixed.booleanValue();
    }
    
    /**
     * Get 'mixed' attribute value.
     *
     * @return mixed
     */
    public Boolean getMixed() {
        return m_mixed;
    }
    
    /**
     * Set 'mixed' attribute value.
     *
     * @param mixed
     */
    public void setMixed(boolean mixed) {
        m_mixed = mixed ? Boolean.TRUE : Boolean.FALSE;
    }
    
    /**
     * Get 'block' attribute value.
     *
     * @return block
     */
    public AllEnumSet getBlock() {
        return m_block;
    }
    
    /**
     * Get 'final' attribute value.
     *
     * @return final
     */
    public AllEnumSet getFinal() {
        return m_final;
    }
    
    /**
     * Get content type element.
     * 
     * @return content type definition, or <code>null</code> if none
     */
    public CommonContentBase getContentType() {
        return m_contentTypeList.size() > 0 ?
            (CommonContentBase)m_contentTypeList.get(0) : null;
    }
    
    /**
     * Set content type element.
     *
     * @param element content type definition, or <code>null</code> if none
     */
    public void setContentType(CommonContentBase element) {
        m_contentTypeList.clear();
        if (element != null) {
            m_contentTypeList.add(element);
        }
    }
    
    /**
     * Get content definition particle.
     *
     * @return content definition particle, or <code>null</code> if none
     */
    public CommonCompositorBase getContentDefinition() {
        return m_contentDefinitionList.size() > 0 ?
            (CommonCompositorBase)m_contentDefinitionList.get(0) : null;
    }
    
    /**
     * Set content definition particle.
     *
     * @param element content definition particle, or <code>null</code> if none
     */
    public void setContentDefinition(CommonCompositorBase element) {
        m_contentDefinitionList.clear();
        if (element != null) {
            m_contentDefinitionList.add(element);
        }
    }
    
    /**
     * Get list of <b>attribute</b> child elements. This list must be empty when
     * a <b>simpleContent</b> or <b>complexContent</b> definition is used.
     *
     * @return list of attributes
     */
    public FilteredSegmentList getAttributeList() {
        return m_attributeList;
    }
    
    /**
     * Get <b>anyAttribute</b> child element.
     * 
     * @return element, or <code>null</code> if none
     */
    public AnyAttributeElement getAnyAttribute() {
        return m_anyAttributeList.size() > 0 ?
            (AnyAttributeElement)m_anyAttributeList.get(0) : null;
    }
    
    /**
     * Set <b>anyAttribute</b> child element.
     *
     * @param element element, or <code>null</code> if unsetting
     */
    public void setAnyAttribute(AnyAttributeElement element) {
        m_anyAttributeList.clear();
        if (element != null) {
            m_anyAttributeList.add(element);
        }
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // check whether global or local definition
        if (!isGlobal()) {
            
            // make sure local type prohibited attributes are not present
            if (getAbstract() != null || getBlock().isPresent() || getFinal().isPresent()) {
                vctx.addError("The 'abstract', 'block', and 'final' attributes are prohibited for a local definition", this);
            }
        }
        
        // check form of definition
        if (m_contentTypeList.size() > 0) {
            if (m_contentTypeList.size() > 1) {
                vctx.addError("Can only have one <complexContent> or <simpleContent> child", this);
            }
            if (m_contentDefinitionList.size() > 0) {
                vctx.addError("Child content particles not allowed with <complexContent> or <simpleContent>", this);
            }
            if (m_attributeList.size() > 0) {
                vctx.addError("Child <attribute> and <attributeGroup> elements not allowed with <complexContent> or <simpleContent>", this);
            }
            if (m_anyAttributeList.size() > 0) {
                vctx.addError("Child <anyAttribute> elements not allowed with <complexContent> or <simpleContent>", this);
            }
        } else if (m_contentDefinitionList.size() > 1) {
            vctx.addError("Can only have one <all>/<choice>/<group>/<sequence> child", this);
        }
        if (m_anyAttributeList.size() > 1) {
            vctx.addError("Only one <anyAttribute> child allowed", this);
        }
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
}
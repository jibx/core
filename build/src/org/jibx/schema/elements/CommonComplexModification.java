/*
 * Copyright (c) 2006-2010, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of JiBX nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.schema.elements;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.validation.ValidationContext;

/**
 * Base representation for all complex type modification elements. This includes
 * both <b>extension</b> and <b>restriction</b>, but only when used with
 * complex types.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class CommonComplexModification extends CommonTypeDerivation
implements IComplexStructure
{
    /** Mask bits for content definition child elements. */
    private long CONTENT_DEFINITION_MASK = ELEMENT_MASKS[ALL_TYPE]
        | ELEMENT_MASKS[CHOICE_TYPE] | ELEMENT_MASKS[GROUP_TYPE]
        | ELEMENT_MASKS[SEQUENCE_TYPE];

    /** Mask bits for attribute child elements. */
    private long ATTRIBUTE_MASK = ELEMENT_MASKS[ATTRIBUTE_TYPE]
        | ELEMENT_MASKS[ATTRIBUTEGROUP_TYPE];

    /** Mask bits for attribute child elements. */
    private long ANYATTRIBUTE_MASK = ELEMENT_MASKS[ANYATTRIBUTE_TYPE];

    //
    // Instance data

    /** Filtered list of content definitions. */
    private final FilteredSegmentList m_contentDefinitionList;

    /** Filtered list of attribute definitions. */
    private final FilteredSegmentList m_attributeList;

    /** Filtered list of anyAttribute definitions (zero or one). */
    private final FilteredSegmentList m_anyAttributeList;

    /**
     * Constructor.
     * 
     * @param type actual element type
     */
    public CommonComplexModification(int type) {
        super(type);
        m_contentDefinitionList = new FilteredSegmentList(
            getChildrenWritable(), CONTENT_DEFINITION_MASK, this);
        m_attributeList = new FilteredSegmentList(getChildrenWritable(),
            ATTRIBUTE_MASK, m_contentDefinitionList, this);
        m_anyAttributeList = new FilteredSegmentList(getChildrenWritable(),
            ANYATTRIBUTE_MASK, m_attributeList, this);
    }

    //
    // Base class overrides

    /* (non-Javadoc)
     * @see org.jibx.schema.elements.CommonTypeDerivation#isComplexType()
     */
    public boolean isComplexType() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.elements.IComplexStructure#getContentDefinition()
     */
    public CommonCompositorBase getContentDefinition() {
        return m_contentDefinitionList.size() > 0 ?
            (CommonCompositorBase)m_contentDefinitionList.get(0) : null;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.elements.IComplexStructure#setContentDefinition(org.jibx.schema.elements.CommonCompositorDefinition)
     */
    public void setContentDefinition(CommonCompositorBase element) {
        m_contentDefinitionList.clear();
        if (element != null) {
            m_contentDefinitionList.add(element);
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.elements.IComplexStructure#getAttributeList()
     */
    public FilteredSegmentList getAttributeList() {
        return m_attributeList;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.elements.IComplexStructure#getAnyAttribute()
     */
    public AnyAttributeElement getAnyAttribute() {
        return m_anyAttributeList.size() > 0
            ? (AnyAttributeElement)m_anyAttributeList.get(0) : null;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.elements.IComplexStructure#setAnyAttribute(org.jibx.schema.elements.AnyAttributeElement)
     */
    public void setAnyAttribute(AnyAttributeElement element) {
        m_anyAttributeList.clear();
        if (element != null) {
            m_anyAttributeList.add(element);
        }
    }
    
    //
    // Validation methods
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {

        // check form of definition
        if (m_anyAttributeList.size() > 1) {
            vctx.addError("Only one <anyAttribute> child allowed", this);
        }

        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
}
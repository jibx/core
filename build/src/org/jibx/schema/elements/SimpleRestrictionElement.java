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
import org.jibx.schema.validation.ValidationContext;

/**
 * <b>restriction</b> element definition used for simple content.
 *
 * @author Dennis M. Sosnoski
 */
public class SimpleRestrictionElement extends CommonSimpleModification
{
    /** Mask bits for inline base type definition. */
    private long INLINE_TYPE_MASK = ELEMENT_MASKS[SIMPLETYPE_TYPE];
    
    /** Mask bits for facet child elements. */
    private long FACETS_MASK = ELEMENT_MASKS[ENUMERATION_TYPE] |
        ELEMENT_MASKS[FRACTIONDIGITS_TYPE] | ELEMENT_MASKS[LENGTH_TYPE] |
        ELEMENT_MASKS[MAXEXCLUSIVE_TYPE] | ELEMENT_MASKS[MAXINCLUSIVE_TYPE] |
        ELEMENT_MASKS[MAXLENGTH_TYPE] | ELEMENT_MASKS[MINEXCLUSIVE_TYPE] |
        ELEMENT_MASKS[MININCLUSIVE_TYPE] | ELEMENT_MASKS[MINLENGTH_TYPE] |
        ELEMENT_MASKS[PATTERN_TYPE] | ELEMENT_MASKS[TOTALDIGITS_TYPE] |
        ELEMENT_MASKS[WHITESPACE_TYPE];
    
    //
    // Instance data
    
    /** Filtered list of inline base type definition element. */
    private final FilteredSegmentList m_inlineBaseList;
    
    /** Filtered list of facet elements. */
    private final FilteredSegmentList m_facetsList;
    
    /**
     * Constructor.
     */
    public SimpleRestrictionElement() {
        super(RESTRICTION_TYPE);
        m_inlineBaseList = new FilteredSegmentList(getChildrenWritable(),
            INLINE_TYPE_MASK, this);
        m_facetsList = new FilteredSegmentList(getChildrenWritable(),
            FACETS_MASK, m_inlineBaseList, this);
    }

    //
    // Base class overrides

    /* (non-Javadoc)
     * @see org.jibx.schema.elements.CommonTypeDerivation#isComplexType()
     */
    public boolean isComplexType() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.elements.CommonTypeDerivation#isExtension()
     */
    public boolean isExtension() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
    
    //
    // Accessor methods
    
    /**
     * Get inline base type definition element.
     * 
     * @return inline base type, or <code>null</code> if none
     */
    public SimpleTypeElement getDerivation() {
        return m_inlineBaseList.size() > 0 ?
            (SimpleTypeElement)m_inlineBaseList.get(0) : null;
    }

    /**
     * Set inline base type definition element.
     *
     * @param element inline base type, or <code>null</code> if unsetting
     */
    public void setDerivation(SimpleTypeElement element) {
        m_inlineBaseList.clear();
        if (element != null) {
            m_inlineBaseList.add(element);
        }
    }

    /**
     * Get list of child facet elements.
     *
     * @return list of facets
     */
    public FilteredSegmentList getFacetsList() {
        return m_facetsList;
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.elements.CommonTypeDerivation#isBaseRequired()
     */
    protected boolean isBaseRequired() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // check for missing or conflicting type information
        if (m_inlineBaseList.size() == 0 && getBase() == null) {
            vctx.addError("Must have 'base' attribute or inline <simpleType> definition", this);
        }
        if (m_inlineBaseList.size() > 0 && getBase() != null) {
            vctx.addError("Can only use 'base' attribute without inline <simpleType> definition", this);
        }
        if (m_inlineBaseList.size() > 1) {
            vctx.addError("Only one inline <simpleType> definition allowed", this);
        }
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
}
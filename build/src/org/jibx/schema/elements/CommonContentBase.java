/*
Copyright (c) 2006, Dennis M. Sosnoski
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

package org.jibx.schema.elements;

import org.jibx.schema.validation.ValidationContext;

/**
 * Common base for <b>simpleContent</b> and <b>complexContent</b> elements.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class CommonContentBase extends AnnotatedBase
{
    /** Mask bits for content derivation child elements. */
    private long CONTENT_DERIVATION_MASK = ELEMENT_MASKS[EXTENSION_TYPE] |
        ELEMENT_MASKS[RESTRICTION_TYPE];
    
    //
    // Instance data
    
    /** Filtered list of content derivation elements (must be exactly one). */
    private final FilteredSegmentList m_contentDerivationList;

    /**
     * Constructor.
     * 
     * @param type actual element type
     */
    public CommonContentBase(int type) {
        super(type);
        m_contentDerivationList = new FilteredSegmentList(getChildrenWritable(),
            CONTENT_DERIVATION_MASK, this);
    }
    
    //
    // Access methods
    
    /**
     * Check if a complex content definition.
     * 
     * @return <code>true</code> if complex content, <code>false</code> if
     * simple content
     */
    public abstract boolean isComplexContent();

    /**
     * Get derivation child element. This is either an &lt;extension> or a
     * &lt;restriction> element.
     *
     * @return derivation element, or <code>null</code> if not yet set
     */
    public CommonTypeDerivation getDerivation() {
        return m_contentDerivationList.size() > 0 ?
            (CommonTypeDerivation)m_contentDerivationList.get(0) : null;
    }

    /**
     * Set derivation child element. This is either an &lt;extension> or a
     * &lt;restriction> element.
     *
     * @param element derivation element, or <code>null</code> if unsetting
     */
    public void setDerivation(CommonTypeDerivation element) {
        m_contentDerivationList.clear();
        if (element != null) {
            m_contentDerivationList.add(element);
            element.setParent(this);
        }
    }
    
    //
    // Validation methods

    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // make sure exactly one content definition item is present
        if (m_contentDerivationList.size() == 0 ) {
            vctx.addError("Missing required <extension> or <restriction> child element", this);
        } else if (m_contentDerivationList.size() > 1) {
            vctx.addError("Only one <extension> or <restriction> child element allowed", this);
        }
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
}
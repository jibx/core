/*
Copyright (c) 2006-2008, Dennis M. Sosnoski
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

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.types.AllEnumSet;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Representation for a <b>simpleType</b> element.
 *
 * @author Dennis M. Sosnoski
 */
public class SimpleTypeElement extends CommonTypeDefinition
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "final" },
        CommonTypeDefinition.s_allowedAttributes);
    
    /** Mask bits for content derivation child elements. */
    private long CONTENT_DERIVATION_MASK = ELEMENT_MASKS[LIST_TYPE] |
        ELEMENT_MASKS[RESTRICTION_TYPE] | ELEMENT_MASKS[UNION_TYPE];
    
    //
    // Value set information
    
    public static final int LIST_FINAL = 0;
    public static final int RESTRICTION_FINAL = 1;
    public static final int UNION_FINAL = 2;

    public static final EnumSet s_simpleDerivationValues = new EnumSet(LIST_FINAL,
        new String[] { "list", "restriction", "union"});
    
    //
    // Instance data
    
    /** Filtered list of content derivation elements (must be exactly one). */
    private final FilteredSegmentList m_contentDerivationList;
    
    /** 'final' attribute value. */
    private AllEnumSet m_final;

    /**
     * Constructor.
     */
    public SimpleTypeElement() {
        super(SIMPLETYPE_TYPE);
        m_contentDerivationList = new FilteredSegmentList(getChildrenWritable(),
            CONTENT_DERIVATION_MASK, this);
        m_final = new AllEnumSet(s_simpleDerivationValues, "final");
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
        return false;
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
     * Get 'final' attribute value.
     *
     * @return final
     */
    public AllEnumSet getFinal() {
        return m_final;
    }

    /**
     * Get derivation child element.
     *
     * @return derivation element, or <code>null</code> if not yet set
     */
    public SchemaBase getDerivation() {
        return m_contentDerivationList.size() > 0 ?
            (SchemaBase)m_contentDerivationList.get(0) : null;
    }

    /**
     * Set derivation child element.
     *
     * @param element derivation element, or <code>null</code> if unsetting
     */
    public void setDerivation(SchemaBase element) {
        m_contentDerivationList.clear();
        if (element != null) {
            m_contentDerivationList.add(element);
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
            if (getFinal().isPresent()) {
                vctx.addError("The 'final' attribute is prohibited for a local definition", this);
            }
        }
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
}
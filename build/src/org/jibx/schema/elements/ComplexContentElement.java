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

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.util.StringArray;

/**
 * Representation for a <b>complexContent</b> element.
 *
 * @author Dennis M. Sosnoski
 */
public class ComplexContentElement extends CommonContentBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "mixed" },
        AnnotatedBase.s_allowedAttributes);
    
    //
    // Instance data
    
    /** 'mixed' attribute value. */
    private Boolean m_mixed;

    /**
     * Constructor.
     */
    public ComplexContentElement() {
        super(COMPLEXCONTENT_TYPE);
    }
    
    //
    // Base class overrides
    
    /* (non-Javadoc)
     * @see org.jibx.schema.elements.CommonContentBase#isComplexContent()
     */
    public boolean isComplexContent() {
        return true;
    }

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
     * Get 'mixed' attribute value.
     *
     * @return mixed (<code>null</code> if not set)
     */
    public Boolean getMixed() {
        return m_mixed;
    }

    /**
     * Set 'mixed' attribute value.
     *
     * @param mixed (<code>null</code> if not set)
     */
    public void setMixed(Boolean mixed) {
        m_mixed = mixed;
    }
 }
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
import org.jibx.runtime.QName;
import org.jibx.schema.INamed;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Model component for <b>notation</b> element, which can only be used as a
 * direct child of the <b>schema</b> element.
 *
 * @author Dennis M. Sosnoski
 */
public class NotationElement extends AnnotatedBase implements INamed
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "name", "public", "system" },
        AnnotatedBase.s_allowedAttributes);
    
    /** "public" attribute value. */
    private String m_public;
    
    /** "system" attribute code for element. */
    private String m_system;
    
    /** 'name' attribute value. */
    private String m_name;

    /** Qualified name (only defined after validation). */
    private QName m_qname;
    
    /**
     * Constructor.
     */
    public NotationElement() {
        super(NOTATION_TYPE);
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
     * Get "public" attribute value.
     * 
     * @return public attribute value
     */
    public String getPublic() {
        return m_public;
    }

    /**
     * Set "public" attribute value.
     * 
     * @param publc public attribute value
     */
    public void setPublic(String publc) {
        m_public = publc;
    }

    /**
     * Get "system" attribute value.
     * 
     * @return system attribute value
     */
    public String getSystem() {
        return m_system;
    }

    /**
     * Set "system" attribute value.
     * 
     * @param systm system attribute value
     */
    public void setSystem(String systm) {
        m_system = systm;
    }
    
    /**
     * Get 'name' attribute value.
     *
     * @return name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Set 'name' attribute value.
     *
     * @param name
     */
    public void setName(String name) {
        m_name = name;
    }
    
    /**
     * Get qualified name for element. This method is only usable after
     * prevalidation.
     *
     * @return qname (<code>null</code> if not defined)
     */
    public QName getQName() {
        return m_qname;
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // check for missing items
        if (m_name == null) {
            vctx.addError("Missing required 'name' attribute", this);
        } else {
            m_qname = new QName(vctx.getCurrentSchema().getEffectiveNamespace(),
                getName());
        }
        if (m_public == null) {
            vctx.addError("Missing required 'public' attribute", this);
        }
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
}
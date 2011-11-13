/*
 * Copyright (c) 2008, Dennis M. Sosnoski. All rights reserved.
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

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base representation for <b>selector</b> and <b>field</b> elements. Inner classes are used for the subclass elements.
 * 
 * @author Dennis M. Sosnoski
 */
public class SelectionBase extends AnnotatedBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes = new StringArray(new String[] { "xpath" },
        AnnotatedBase.s_allowedAttributes);
    
    //
    // Instance data
    
    /** 'xpath' attribute value. */
    private String m_xpath;
    
    /**
     * Constructor.
     * 
     * @param type
     */
    public SelectionBase(int type) {
        super(type);
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
     * Get 'xpath' attribute value.
     * 
     * @return xpath
     */
    public String getXPath() {
        return m_xpath;
    }
    
    /**
     * Set 'xpath' attribute value.
     * 
     * @param xpath
     */
    public void setXPath(String xpath) {
        m_xpath = xpath;
    }
    
    //
    // Validation methods
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (getXPath() == null) {
            vctx.addError("The 'xpath' attribute is required for 'selection' and 'field' elements", this);
        }
        super.prevalidate(vctx);
    }
    
    //
    // Actual element subclasses
    
    public static class SelectorElement extends SelectionBase
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public SelectorElement() {
            super(SELECTOR_TYPE);
        }
    }
    
    public static class FieldElement extends SelectionBase
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public FieldElement() {
            super(FIELD_TYPE);
        }
    }
}
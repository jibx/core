/*
 * Copyright (c) 2006-2008, Dennis M. Sosnoski All rights reserved.
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
import org.jibx.schema.IArity;
import org.jibx.schema.attributes.OccursAttributeGroup;
import org.jibx.schema.types.Count;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * <b>anyAttribute</b> element definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class AnyElement extends WildcardBase implements IArity
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(WildcardBase.s_allowedAttributes,
        OccursAttributeGroup.s_allowedAttributes);
    
    //
    // Instance data
    
    /** Occurs attribute group. */
    private OccursAttributeGroup m_occurs;
    
    /**
     * Constructor.
     */
    public AnyElement() {
        super(ANY_TYPE);
        m_occurs = new OccursAttributeGroup(this);
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
    // Delegated methods
    
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

    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // prevalidate the attributes
        m_occurs.prevalidate(vctx);
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // validate the attributes
        m_occurs.validate(vctx);
        
        // handle base class validation
        super.validate(vctx);
    }
}
/*
Copyright (c) 2006-2007, Dennis M. Sosnoski
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

import org.jibx.schema.IArity;
import org.jibx.schema.attributes.OccursAttributeGroup;
import org.jibx.schema.types.Count;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base class for all complex content model compositors. This includes the
 * special case of the &lt;group> (reference) compositor, which doesn't contain
 * any children but acts as a placeholder for the compositor in the &lt;group>
 * definition.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class CommonCompositorBase extends AnnotatedBase implements IArity
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(OccursAttributeGroup.s_allowedAttributes,
        AnnotatedBase.s_allowedAttributes);
    
    //
    // Instance data
    
    /** Attribute values for specify occurance constraints. */
    private OccursAttributeGroup m_occurs;

    /**
     * Constructor.
     * 
     * @param type element type
     */
    protected CommonCompositorBase(int type) {
    	super(type);
        m_occurs = new OccursAttributeGroup(this);
    }
    
    //
    // Access methods
    
    //
    // Attribute delegate methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.attributes.OccursAttributeGroup#getMaxOccurs()
     */
    public Count getMaxOccurs() {
        return m_occurs.getMaxOccurs();
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.attributes.OccursAttributeGroup#getMinOccurs()
     */
    public Count getMinOccurs() {
        return m_occurs.getMinOccurs();
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.attributes.OccursAttributeGroup#setMaxOccurs(org.jibx.schema.attributes.Count)
     */
    public void setMaxOccurs(Count count) {
        m_occurs.setMaxOccurs(count);
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.attributes.OccursAttributeGroup#setMinOccurs(org.jibx.schema.attributes.Count)
     */
    public void setMinOccurs(Count count) {
        m_occurs.setMinOccurs(count);
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.AnnotatedBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // validate the attributes
        m_occurs.prevalidate(vctx);
        
        // continue with base class prevalidation
        super.prevalidate(vctx);
    }
}
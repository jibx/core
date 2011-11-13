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

package org.jibx.schema.attributes;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.types.Count;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Schema occurs attribute group.
 * 
 * @author Dennis M. Sosnoski
 */
public class OccursAttributeGroup extends AttributeBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "maxOccurs", "minOccurs" });
    
    /** 'minOccurs' attribute value (<code>null</code> if not set). */
    private Count m_minOccurs;
    
    /** 'maxOccurs' attribute value (<code>null</code> if not set). */
    private Count m_maxOccurs;
    
    /**
     * Constructor.
     * 
     * @param owner owning element
     */
    public OccursAttributeGroup(SchemaBase owner) {
        super(owner);
    }
    
    /**
     * Factory method for use during unmarshalling. This gets the owning element
     * from the unmarshalling context, and creates an instance of the attribute
     * tied to that element.
     *
     * @param ictx
     * @return constructed instance
     */
    private static OccursAttributeGroup
        unmarshalFactory(IUnmarshallingContext ictx) {
        return new OccursAttributeGroup((SchemaBase)ictx.getStackTop());
    }
    
    /**
     * Get 'maxOccurs' attribute value.
     * 
     * @return count (<code>null</code> if not set)
     */
    public Count getMaxOccurs() {
        return m_maxOccurs;
    }
    
    /**
     * Set 'maxOccurs' attribute value.
     * 
     * @param count (<code>null</code> if unsetting)
     */
    public void setMaxOccurs(Count count) {
        m_maxOccurs = count;
    }
    
    /**
     * Get 'minOccurs' attribute value.
     * 
     * @return minimum count (<code>null</code> if not set)
     */
    public Count getMinOccurs() {
        return m_minOccurs;
    }
    
    /**
     * Set 'minOccurs' attribute value.
     * 
     * @param count (<code>null</code> if unsetting)
     */
    public void setMinOccurs(Count count) {
        m_minOccurs = count;
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (m_minOccurs != null) {
            if (m_minOccurs.isUnbounded()) {
                vctx.addError("'minOccurs' value cannot be 'unbounded'", getOwner());
            } else if (m_maxOccurs != null && !m_maxOccurs.isUnbounded() &&
                m_maxOccurs.getCount() < m_minOccurs.getCount()) {
                vctx.addError("'minOccurs' cannot be larger than 'maxOccurs'", getOwner());
            }
        }
    }
}
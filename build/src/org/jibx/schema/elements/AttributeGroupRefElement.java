/*
Copyright (c) 2006-2009, Dennis M. Sosnoski.
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
import org.jibx.schema.IReference;
import org.jibx.schema.support.QNameConverter;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Definition for embedded <b>attributeGroup</b> element (attribute group
 * reference).
 *
 * @author Dennis M. Sosnoski
 */
public class AttributeGroupRefElement extends AnnotatedBase implements IReference
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "ref" },
        AnnotatedBase.s_allowedAttributes);
    
    //
    // Instance data
    
    /** Reference definition. */
    private QName m_ref;
    
    /** Referenced element (filled in by validation). */
    private AttributeGroupElement m_refGroup;
    
    /**
     * Constructor.
     */
    public AttributeGroupRefElement() {
    	super(ATTRIBUTEGROUP_TYPE);
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
     * Get 'ref' attribute value.
     * 
     * @return ref
     */
    public QName getRef() {
        return m_ref;
    }
    
    /**
     * Set 'ref' attribute value.
     * 
     * @param ref
     */
    public void setRef(QName ref) {
        m_ref = ref;
    }
    
    /**
     * Get the referenced attributeGroup declaration. This method is only usable
     * after validation.
     *
     * @return referenced group definition
     */
    public AttributeGroupElement getReference() {
        return m_refGroup;
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (m_ref == null) {
            vctx.addFatal("'ref' attribute is required for attributeGroup reference", this);
        } else {
            SchemaElement schema = vctx.getCurrentSchema();
            String ens = schema.getEffectiveNamespace();
            QNameConverter.patchQNameNamespace(ens, m_ref);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // make sure element reference is defined
        m_refGroup = vctx.findAttributeGroup(m_ref);
        if (m_refGroup == null) {
            vctx.addFatal("Referenced attributeGroup '" + m_ref + "' is not defined", this);
        }
        
        // handle base class validation
        super.validate(vctx);
    }
}
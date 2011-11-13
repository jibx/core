/*
Copyright (c) 2006-2009, Dennis M. Sosnoski
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
import org.jibx.runtime.QName;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.support.QNameConverter;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Schema defRef attribute group.
 * 
 * @author Dennis M. Sosnoski
 */
public class DefRefAttributeGroup extends AttributeBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "name", "ref" });
    
    /** Name definition. */
    private String m_name;
    
    /** Reference definition. */
    private QName m_ref;
    
    /**
     * Constructor.
     * 
     * @param owner owning element
     */
    public DefRefAttributeGroup(SchemaBase owner) {
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
    private static DefRefAttributeGroup
        unmarshalFactory(IUnmarshallingContext ictx) {
        return new DefRefAttributeGroup((SchemaBase)ictx.getStackTop());
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
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (m_ref != null && m_name != null) {
            vctx.addError("Cannot use both 'ref' and 'name'", getOwner());
        }
        SchemaElement schema = vctx.getCurrentSchema();
        String ens = schema.getEffectiveNamespace();
        QNameConverter.patchQNameNamespace(ens, m_ref);
    }
}
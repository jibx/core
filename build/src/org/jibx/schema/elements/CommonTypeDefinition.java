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

import org.jibx.runtime.QName;
import org.jibx.schema.INamed;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base representation for all type definition elements.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class CommonTypeDefinition extends AnnotatedBase implements INamed
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "name" },
        AnnotatedBase.s_allowedAttributes);
    
    /** 'name' attribute value. */
    private String m_name;
    
    /** Qualified name. */
    protected QName m_qname;
    
    /**
     * Constructor.
     * 
     * @param type actual element type
     */
    public CommonTypeDefinition(int type) {
        super(type);
    }
    
    /**
     * Check if a complex type definition.
     * 
     * @return <code>true</code> if complex type, <code>false</code> if simple
     * type
     */
    public abstract boolean isComplexType();
    
    /**
     * Check if a predefined type definition.
     * 
     * @return <code>true</code> if predefined, <code>false</code> if user type
     */
    public abstract boolean isPredefinedType();

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
     * Get qualified name for type. This method is only usable after validation.
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
        
        // check whether global or local definition
        if (isGlobal()) {
            
            // make sure name is supplied for global complex type
            if (getName() == null) {
                vctx.addError("The 'name' attribute is required for a global definition", this);
            } else {
                String ens = vctx.getCurrentSchema().getEffectiveNamespace();
                m_qname = new QName(ens, getName());
            }
            
        } else {
            
            // make sure local type prohibited name attribute is not present
            if (getName() != null) {
                vctx.addError("The 'name' attribute is prohibited for a local definition", this);
            }
        }
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
}
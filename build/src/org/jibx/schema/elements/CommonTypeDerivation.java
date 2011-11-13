/*
Copyright (c) 2006-2010, Dennis M. Sosnoski. All rights reserved.

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
import org.jibx.schema.support.QNameConverter;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base class for all <b>extension</b> and <b>restriction</b> element
 * variations.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class CommonTypeDerivation extends AnnotatedBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes = new StringArray(
        new String[] { "base" }, AnnotatedBase.s_allowedAttributes);
    
    //
    // Instance data

    /** 'base' attribute value. */
    private QName m_base;
    
    /** Referenced type definition (<code>null</code> if none). */
    private CommonTypeDefinition m_typeDef;
    
	/**
     * Constructor.
     * 
     * @param type actual element type
     */
    protected CommonTypeDerivation(int type) {
    	super(type);
    }

    //
    // Access methods

    /**
     * Get 'base' attribute value.
     * 
     * @return attribute value
     */
    public QName getBase() {
        return m_base;
    }

    /**
     * Set 'base' attribute value.
     * 
     * @param base attribute value
     */
    public void setBase(QName base) {
        m_base = base;
    }
    
    /**
     * Get the base type definition. This method is only usable after
     * validation.
     *
     * @return base type
     */
    public CommonTypeDefinition getBaseType() {
        return m_typeDef;
    }

    /**
     * Check if complex type derivation.
     *
     * @return <code>true</code> if complex type derivation, <code>false</code>
     * if simple type derivation
     */
    public abstract boolean isComplexType();

    /**
     * Check if extension derivation.
     *
     * @return <code>true</code> if extension, <code>false</code> if restriction
     */
    public abstract boolean isExtension();
    
    //
    // Validation methods
    
    /**
     * Check if base attribute value is required for this element. This allows
     * subclasses to override the default required status.
     * 
     * @return <code>true</code> if base attribute required, <code>false</code>
     * if not
     */
    protected boolean isBaseRequired() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (getBase() == null) {
            if (isBaseRequired()) {
                vctx.addError("The 'base' attribute is required for an extension or restriction", this);
            }
        } else {
            SchemaElement schema = vctx.getCurrentSchema();
            String ens = schema.getEffectiveNamespace();
            QNameConverter.patchQNameNamespace(ens, m_base);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // look up referenced type definition
        if (m_base != null) {
            m_typeDef = vctx.findType(m_base);
        }
        if (isBaseRequired() && m_typeDef == null) {
            vctx.addFatal("Referenced type '" + m_base + "' is not defined", this);
        }

        // continue with parent class validation
        super.validate(vctx);
    }
}
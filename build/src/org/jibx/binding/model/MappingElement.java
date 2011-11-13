/*
Copyright (c) 2004-2008, Dennis M. Sosnoski
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

package org.jibx.binding.model;

import java.util.ArrayList;

/**
 * Model component for <b>mapping</b> element of normal binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class MappingElement extends MappingElementBase
{
    /** Mapping extended by this mapping. */
    private MappingElement m_extendsMapping;
    
    /** Constructability verified flag. */
    private boolean m_constructVerified;
	
	/**
	 * Default constructor.
	 */
	public MappingElement() {
        super(MAPPING_ELEMENT);
        m_topChildren = new ArrayList();
    }
    
    /**
     * Get mapping extended by this one.
     * 
     * @return mapping extended by this one
     */
    public MappingElement getExtendsMapping() {
        return m_extendsMapping;
    }
    
    /**
     * Verify that instances of the mapped class can be constructed. This
     * method may be called during the {@link #validate(ValidationContext)}
     * processing of other elements. If this mapping has any extensions, the
     * check is ignored.
     * TODO: check that at least one of the extensions can be created
     *
     * @param vctx
     */
    public void verifyConstruction(ValidationContext vctx) {
        if (!m_constructVerified && getExtensionTypes().size() == 0) {
            verifyConstruction(vctx, getHandledClass());
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // make sure we can construct instances of concrete mapped class
        if (!isAbstract()) {
            verifyConstruction(vctx, getHandledClass());
            m_constructVerified = true;
        }
        
        // run base class validation
        super.validate(vctx);
    }

    /**
     * Special validation method to link extension mappings to base mappings.
     * This is called as a special step following registration, so that the
     * normal validation pass can make use of the linkage information.
     * 
     * @param vctx validation context
     */
    public void validateExtension(ValidationContext vctx) {
        String extend = getExtendsName();
        if (extend != null) {
            
            // find the base class mapping
            TemplateElementBase base =
                vctx.getDefinitions().getSpecificTemplate(extend);
            if (base instanceof MappingElement) {
                if (base != this) {
                    
                    // check base class using custom marshaller/unmarshaller
                    MappingElement mbase = (MappingElement)base;
                    if (mbase.getMarshaller() != null ||
                        mbase.getUnmarshaller() != null) {
                        vctx.addError("Cannot extend a mapping using custom " +
                            "marshaller/unmarshaller");
                    }
                    
                    // add reference to base class
                    mbase.addExtensionType(this);
                    m_extendsMapping = mbase;
                    
                    // check for circular references
                    MappingElementBase mark = this;
                    boolean skip = true;
                    while (mbase != null) {
                        if (mbase == mark) {
                            vctx.addError("Circular 'extends' reference chain",
                                mbase);
                            break;
                        } else if (skip) {
                            skip = false;
                        } else {
                            mark = mbase;
                            skip = true;
                        }
                        mbase = mbase.getExtendsMapping();
                    }
                    
                }
            } else {
                vctx.addFatal("No mapping found for class " + extend);
            }
        }
    }
}
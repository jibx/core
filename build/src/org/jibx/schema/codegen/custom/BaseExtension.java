/*
 * Copyright (c) 2007-2008, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen.custom;

import org.apache.log4j.Logger;
import org.jibx.runtime.QName;
import org.jibx.schema.elements.OpenAttrBase;

/**
 * Base extension information for any schema component. This is the basic extension structure that applies to each
 * schema component.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class BaseExtension
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(BaseExtension.class.getName());
    
    //
    // Arity types
    
    public static final int ARITY_OPTIONAL_SINGLETON = 0;
    public static final int ARITY_REQUIRED_SINGLETON = 1;
    public static final int ARITY_OPTIONAL_COLLECTION = 2;
    public static final int ARITY_REQUIRED_COLLECTION = 3;
    
    //
    // Instance data
    
    /** Annotated schema definition component. */
    private final OpenAttrBase m_component;
    
    /** Type replacement implementation (<code>null</code> if no replacements at this level). */
    private TypeReplacer m_typeReplacer;
    
    /**
     * Constructor.
     * 
     * @param comp
     */
    public BaseExtension(OpenAttrBase comp) {
        m_component = comp;
        comp.setExtension(this);
    }

    /**
     * Get schema component.
     *
     * @return component
     */
    public OpenAttrBase getComponent() {
        return m_component;
    }

    /**
     * Set type replacer. This type replacer will apply to this extension and any child extensions which do not have
     * their own type replacers.
     *
     * @param replacer
     */
    public void setTypeReplacer(TypeReplacer replacer) {
        m_typeReplacer = replacer;
    }

    /**
     * Get the replacement type to be substituted for a supplied type. This starts with this extension and then scans
     * up the tree to find the first extension with a type replacer defined. If a type replacer is found at any level
     * it is applied to the supplied type. If multiple extensions in the path up the tree have type replacers defined,
     * only the first type replacer is used.
     *
     * @param qname original type
     * @return substitute type (<code>null</code> if deletion; original type, if no substitution defined)
     */
    public QName getReplacementType(QName qname) {
        
        // use direct navigation up tree to avoid call overhead
        BaseExtension ancestor = this;
        while (ancestor != null) {
            if (ancestor.m_typeReplacer != null) {
                return ancestor.m_typeReplacer.getReplacement(qname);
            }
            ancestor = (BaseExtension)ancestor.getComponent().getParent().getExtension();
        }
        return qname;
    }
}
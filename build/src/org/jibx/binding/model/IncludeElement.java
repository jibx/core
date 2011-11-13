/*
Copyright (c) 2004-2010, Dennis M. Sosnoski.
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

import java.io.IOException;
import java.net.URL;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.util.ClasspathUrlExtender;
import org.jibx.util.StringArray;

/**
 * Model component for <b>include</b> element of binding definition. During
 * prevalidation this reads the included binding definition. All further
 * processing of the included components needs to be handled directly by the
 * tree walking code in {@link org.jibx.binding.model.TreeContext}, since the
 * components of the included binding need to be treated as though they were
 * direct children of the container of this element (and accessed in the
 * appropriate order).
 *
 * @author Dennis M. Sosnoski
 */
public class IncludeElement extends NestingElementBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "path", "precompiled" });
    
    /** Path to included binding definition. */
    private String m_includePath;
    
    /** Included binding precompiled flag. */
    private boolean m_precompiled;
    
    /** Object model for included binding. */
    private BindingElement m_binding;
    
    /**
     * Constructor.
     */
    public IncludeElement() {
        super(INCLUDE_ELEMENT);
    }
    
    /**
     * Set path to included binding.
     * 
     * @param path
     */
    public void setIncludePath(String path) {
        m_includePath = path;
    }
    
    /**
     * Get path to included binding.
     * 
     * @return path
     */
    public String getIncludePath() {
        return m_includePath;
    }
    
    /**
     * Get the included binding model. This call is only valid after
     * prevalidation.
     * 
     * @return binding element, or <code>null</code> if redundant include
     */
    public BindingElement getBinding() {
        return m_binding;
    }
    
    /**
     * Check if precompiled binding.
     *
     * @return <code>true</code> if precompiled, <code>false</code> if not
     */
    public boolean isPrecompiled() {
        return m_precompiled;
    }
    
    /**
     * Set precompiled binding flag.
     *
     * @param precomp
     */
    public void setPrecompiled(boolean precomp) {
        m_precompiled = precomp;
    }
    
    //
    // Validation methods
    
    /**
     * Make sure all attributes are defined.
     *
     * @param uctx unmarshalling context
     * @exception JiBXException on unmarshalling error
     */
    private void preSet(IUnmarshallingContext uctx) throws JiBXException {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (m_includePath == null) {
            vctx.addFatal("No include path specified");
        } else {
            try {
                
                // locate the innermost binding element
                int limit = vctx.getNestingDepth();
                BindingElement binding = null;
                for (int i = 1; i < limit; i++) {
                    ElementBase parent = vctx.getParentElement(i);
                    if (parent instanceof BindingElement) {
                        binding = (BindingElement)parent;
                        break;
                    }
                }
                if (binding == null) {
                    throw new JiBXException("No binding element found");
                }
                
                // access the included binding as input stream
                URL base = binding.getBaseUrl();
                URL url = ClasspathUrlExtender.buildURL(base, m_includePath);
                String path = url.toExternalForm();
                BindingElement root = vctx.getBindingRoot();
                if (root.addIncludePath(path, m_precompiled)) {
                    m_binding = root.getIncludeBinding(url, binding, vctx);
                }
                
            } catch (JiBXException e) {
                vctx.addFatal("Error unmarshalling included binding '" + m_includePath + "': " + e.getMessage());
            } catch (IOException e) {
                vctx.addFatal("Error accessing included binding with path \"" +
                    m_includePath + "\": " + e.getMessage());
            }
        }
    }
}
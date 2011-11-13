/*
Copyright (c) 2004-2009, Dennis M. Sosnoski.
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

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.util.StringArray;

/**
 * Model component for <b>namespace</b> element of binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class NamespaceElement extends ElementBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "default", "prefix", "uri" });
    
    //
    // Enumeration for namespace usage.
    
    public static final int NODEFAULT_USAGE = 0;
    public static final int ELEMENTS_USAGE = 1;
    public static final int ATTRIBUTES_USAGE = 2;
    public static final int ALLDEFAULT_USAGE = 3;
    
    public static final EnumSet s_defaultEnum =
        new EnumSet(NODEFAULT_USAGE,
        new String[] { "none", "elements", "attributes", "all" });

    //
    // Actual instance data
    
    /** Default type name. */
    private String m_defaultName = s_defaultEnum.getName(NODEFAULT_USAGE);
    
    /** Actual selected default. */
    private int m_defaultIndex;
    
    /** Namespace URI. */
    private String m_uri;

    /** Namespace prefix (may be <code>null</code>, but not ""). */
    private String m_prefix;
    
    /**
     * Constructor.
     */
    public NamespaceElement() {
        super(NAMESPACE_ELEMENT);
    }
	
	/**
	 * Get prefix.
	 * 
	 * @return prefix text
	 */
	public String getPrefix() {
		return m_prefix;
	}
	
	/**
	 * Set prefix.
	 * 
	 * @param text prefix text
	 */
	public void setPrefix(String text) {
        m_prefix = text;
	}
	
	/**
	 * Get namespace URI.
	 * 
	 * @return namespace URI (<code>null</code> if no-namespace namespace)
	 */
	public String getUri() {
		return m_uri;
	}
	
	/**
	 * Set namespace URI.
	 * 
	 * @param uri namespace URI (<code>null</code> if no-namespace namespace)
	 */
	public void setUri(String uri) {
		m_uri = uri;
	}
    
    /**
     * Set namespace default type name.
     * 
     * @param name namespace default type
     */
    public void setDefaultName(String name) {
        m_defaultName = name;
    }
    
    /**
     * Get namespace default type name.
     * 
     * @return namespace default type name
     */
    public String getDefaultName() {
        return m_defaultName;
    }

    /**
     * Check if default namespace for attributes. This method is only meaningful
     * after a call to {@link #prevalidate(ValidationContext)}.
     *
     * @return <code>true</code> if default namespace for attributes,
     * <code>false</code> if not
     */
    public boolean isAttributeDefault() {
        return m_defaultIndex == ATTRIBUTES_USAGE ||
            m_defaultIndex == ALLDEFAULT_USAGE;
    }

    /**
     * Check if default namespace for elements. This method is only meaningful
     * after a call to {@link #prevalidate(ValidationContext)}.
     *
     * @return <code>true</code> if default namespace for elements,
     * <code>false</code> if not
     */
    public boolean isElementDefault() {
        return m_defaultIndex == ELEMENTS_USAGE ||
            m_defaultIndex == ALLDEFAULT_USAGE;
    }
    
    //
    // Validation methods
    
    /**
     * Make sure all attributes are defined.
     *
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Prevalidate attributes of element in isolation.
     *
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {
        m_defaultIndex = s_defaultEnum.getValue(m_defaultName);
        if (m_defaultIndex < 0) {
            vctx.addError("Value \"" + m_defaultName +
                "\" is not a valid choice for namespace default usage");
        } else if (m_prefix == null && m_uri != null && vctx.isOutBinding() &&
            m_defaultIndex != ELEMENTS_USAGE) {
            vctx.addError("Prefix required for namespace on output binding unless default='elements' used");
        }
    }
}
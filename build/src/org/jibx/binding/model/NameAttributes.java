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

import org.jibx.runtime.Utility;
import org.jibx.util.StringArray;

/**
 * Model component for <b>name</b> attribute group in binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class NameAttributes extends AttributeBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "name", "ns" });
    // TODO: add "prefix" to set for 2.0
    
	/** Name text. */
	private String m_name;
	
	/** Namespace URI. */
	private String m_uri;
    
    /** Namespace prefix. */
    private String m_prefix;
    
    /** Name represents an attribute flag. */
    private boolean m_isAttribute;
    
    /** Namespace definition used by this name. */
    private NamespaceElement m_namespace;
    
    /**
     * Default constructor.
     */
    public NameAttributes() {}
    
    /**
     * Set flag for an attribute name. This information is necessary for
     * resolving the namespace definition to be used with a name, but has to be
     * determined by the element owning this attribute group. It must be set (if
     * different from the default of <code>false</code>) prior to validation.
     * 
     * @param isattr flag for name represents an attribute
     */
    public void setIsAttribute(boolean isattr) {
        m_isAttribute = isattr;
    }
    
    /**
     * Get flag for an attribute name.
     * 
     * @return <code>true</code> if an attribute, <code>false</code> if an
     * element
     */
    public boolean isAttribute() {
        return m_isAttribute;
    }
	
	/**
	 * Get name.
	 * 
	 * @return name text
	 */
	public String getName() {
		return m_name;
	}
	
	/**
	 * Set name.
	 * 
	 * @param name text for name
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * Get specified namespace URI.
	 * 
	 * @return namespace URI (<code>null</code> if not set)
	 */
	public String getUri() {
		return m_uri;
	}
	
	/**
	 * Set namespace URI.
	 * 
	 * @param uri namespace URI (<code>null</code> if not set)
	 */
	public void setUri(String uri) {
		m_uri = uri;
	}
    
    /**
     * Get specified namespace prefix.
     * 
     * @return namespace prefix (<code>null</code> if not set)
     */
    public String getPrefix() {
        return m_prefix;
    }
    
    /**
     * Set namespace prefix.
     * 
     * @param prefix namespace prefix (<code>null</code> if not set)
     */
    public void setPrefix(String prefix) {
        m_prefix = prefix;
    }
    
    /**
     * Get effective namespace definition. This call can only be used after
     * validation.
     * 
     * @return definition for namespace used by this name
     */
    public NamespaceElement getNamespace() {
        return m_namespace;
    }
    
    //
    // Overrides of base class methods
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.AttributeBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        if (m_name != null) {
            DefinitionContext dctx = null;
            ElementBase elem = vctx.peekElement();
            if (elem instanceof ContainerElementBase) {
                dctx = ((ContainerElementBase)elem).getDefinitions();
            }
            if (dctx == null) {
                dctx = vctx.getDefinitions();
            }
            if (m_isAttribute) {
                m_namespace = dctx.getAttributeNamespace(this);
            } else {
                m_namespace = dctx.getElementNamespace(this, vctx);
                String uri = getUri();
                if (uri != null && m_namespace == null) {
                    vctx.addError("No namespace definition for URI " + uri);
                }
            }
        }
        super.validate(vctx);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof NameAttributes) {
            NameAttributes comp = (NameAttributes)obj;
            if (Utility.safeEquals(m_name, comp.m_name)) {
                if (m_namespace == null) {
                    return comp.m_namespace == null;
                } else {
                    return Utility.safeEquals(m_namespace.getUri(),
                        comp.m_namespace.getUri());
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (m_uri == null) {
            return m_name.hashCode();
        } else {
            return m_name.hashCode() + m_uri.hashCode();
        }
    }
}
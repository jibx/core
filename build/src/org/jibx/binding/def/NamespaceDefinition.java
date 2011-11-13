/*
Copyright (c) 2003-2009, Dennis M. Sosnoski.
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

package org.jibx.binding.def;

/**
 * Namespace definition from binding.
 *
 * @author Dennis M. Sosnoski
 */
public class NamespaceDefinition
{
    //
    // Enumeration for namespace usage.
    
    /*package*/ static final int NODEFAULT_USAGE = 0;
    /*package*/ static final int ELEMENTS_USAGE = 1;
    /*package*/ static final int ATTRIBUTES_USAGE = 2;
    /*package*/ static final int ALLDEFAULT_USAGE = 3;

    //
    // Actual instance data
    
    /** Namespace URI. */
    private String m_uri;

    /** Namespace prefix (may be <code>null</code>, but not ""). */
    private String m_prefix;
    
    /** Index in namespace table for binding. */
    private int m_index;

    /** Use by default for nested elements. */
    private boolean m_elementDefault;

    /** Use by default for nested attributes. */
    private boolean m_attributeDefault;

    /**
     * Constructor.
     *
     * @param uri namespace URI
     * @param prefix namespace prefix (may be <code>null</code> for default
     * namespace, but not "")
     * @param usage code for default usage of namespace
     */
    public NamespaceDefinition(String uri, String prefix, int usage) {
        m_uri = uri;
        m_prefix = prefix;
        m_elementDefault = 
             (usage == ALLDEFAULT_USAGE) || (usage == ELEMENTS_USAGE);
        m_attributeDefault = 
             (usage == ALLDEFAULT_USAGE) || (usage == ATTRIBUTES_USAGE);
    }

    /**
     * Check if default namespace for attributes.
     *
     * @return <code>true</code> if default namespace for attributes,
     * <code>false</code> if not
     */
    public boolean isAttributeDefault() {
        return m_attributeDefault;
    }

    /**
     * Check if default namespace for elements.
     *
     * @return <code>true</code> if default namespace for elements,
     * <code>false</code> if not
     */
    public boolean isElementDefault() {
        return m_elementDefault;
    }
    
    /**
     * Set prefix for namespace.
     *
     * @param prefix namespace prefix (may be <code>null</code>, but not "")
     */
    public void setPrefix(String prefix) {
        m_prefix = prefix;
    }

    /**
     * Get prefix for namespace.
     *
     * @return namespace prefix (may be <code>null</code>, but not "")
     */
    public String getPrefix() {
        return m_prefix;
    }

    /**
     * Get namespace URI.
     *
     * @return namespace URI
     */
    public String getUri() {
        return m_uri;
    }

    /**
     * Set namespace index.
     *
     * @param index namespace index
     */
    public void setIndex(int index) {
        m_index = index;
    }

    /**
     * Get namespace index.
     *
     * @return namespace index
     */
    public int getIndex() {
        return m_index;
    }

    /**
     * Instance builder with supplied values. Used for canned definitions.
     * 
     * @param uri namespace URI
     * @param prefix namespace prefix
     */
    public static NamespaceDefinition buildNamespace(String uri,
        String prefix) {
        return new NamespaceDefinition(uri, prefix, NODEFAULT_USAGE);
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("namespace " + m_uri);
        if (m_attributeDefault || m_elementDefault) {
            System.out.print(" (default ");
            if (m_elementDefault) {
                System.out.print("elements");
                if (m_attributeDefault) {
                    System.out.print(" and attributes");
                }
            } else {
                System.out.print("attributes");
            }
            System.out.print(')');
        }
        if (m_prefix != null) {
            System.out.print(" (prefix " + m_prefix + ")");
        }
        System.out.println();
    }
}
/*
Copyright (c) 2003-2007, Dennis M. Sosnoski
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

import org.jibx.binding.classes.MethodBuilder;
import org.jibx.runtime.JiBXException;

/**
 * Named value definition from binding. This is a component of all items
 * in the mapping corresponding to elements or attributes in the document.
 *
 * @author Dennis M. Sosnoski
 */
public class NameDefinition
{
    /** Element or attribute name. */
    private final String m_name;

    /** Element or attribute namespace URI. */
    private String m_namespace;

    /** Flag for attribute name. */
    private final boolean m_isAttribute;

    /** Namespace index used for marshalling (derived from nesting). */
    private int m_namespaceIndex;

    /**
     * Constructor.
     *
     * @param name
     * @param ns 
     * @param attr flag for attribute name
     */
    public NameDefinition(String name, String ns, boolean attr) {
        m_name = name;
        m_namespace = ns;
        m_isAttribute = attr;
    }
    
    /**
     * Get the local name.
     *
     * @return name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Get the namespace URI.
     *
     * @return namespace (<code>null</code> if no-namespace namespace)
     */
    public String getNamespace() {
        return m_namespace;
    }

    /**
     * Check if namespace URI is null.
     *
     * @return <code>true</code> if URI null, <code>false</code> if not
     */

    public boolean isNullUri() {
        return m_namespace == null;
    }

    /**
     * Generate code to push namespace URI.
     *
     * @param mb method builder
     */

    public void genPushUri(MethodBuilder mb) {
        if (m_namespace == null) {
            mb.appendACONST_NULL();
        } else {
            mb.appendLoadConstant(m_namespace);
        }
    }

    /**
     * Generate code to push name.
     *
     * @param mb method builder
     */

    public void genPushName(MethodBuilder mb) {
        mb.appendLoadConstant(m_name);
    }

    /**
     * Generate code to push namespace URI followed by name.
     *
     * @param mb method builder
     */

    public void genPushUriPair(MethodBuilder mb) {
        genPushUri(mb);
        genPushName(mb);
    }

    /**
     * Generate code to push namespace index followed by name.
     *
     * @param mb method builder
     */

    public void genPushIndexPair(MethodBuilder mb) {
        mb.appendLoadConstant(m_namespaceIndex);
        genPushName(mb);
    }

    /**
     * Finds the index for the namespace used with a name. If no explicit
     * namespace has been set it uses the appropriate default. This is a
     * separate operation from the unmarshalling in order to properly handle
     * namespace definitions as children of the named binding component.
     *
     * @param defc definition context for namespaces
     * @throws JiBXException if error in namespace handling
     */

    public void fixNamespace(DefinitionContext defc) throws JiBXException {
        if (m_namespace == null) {
            m_namespace = defc.getDefaultURI(m_isAttribute);
            m_namespaceIndex = defc.getDefaultIndex(m_isAttribute);
        } else {
            try {
                m_namespaceIndex = defc.getNamespaceIndex
                    (m_namespace, m_isAttribute);
            } catch (JiBXException ex) {
                throw new JiBXException("Undefined or unusable namespace \"" +
                    m_namespace + '"');
            }
        }
    }
    
    // DEBUG
    public String toString() {
        if (m_namespace == null) {
            return m_name;
        } else {
            return "{" + m_namespace + "}:" + m_name;
        }
    }
}

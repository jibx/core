/*
 * Copyright (c) 2007-2009 Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.custom.classes;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.util.StringArray;

/**
 * Base class for all binding customizations that can contain other customizations. This includes inherited values
 * shared with customization extensions (in particular, the WSDL extensions).
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class SharedNestingBase extends CustomBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "javadoc-formatter", "name-style", "namespace", "namespace-style", "require",
        "use-javadocs" });
    
    // values inherited through nesting
    private String m_javadocFormatter;
    
    private String m_namespace;
    
    private Integer m_namespaceStyle;
    
    private Integer m_nameStyle;
    
    private Integer m_require;
    
    private Boolean m_useJavaDocs;
    
    // value set by subclasses
    private String m_actualNamespace;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public SharedNestingBase(SharedNestingBase parent) {
        super(parent);
    }
    
    //
    // Access methods for values inherited through nesting
    
    /**
     * Get JavaDoc formatter class name.
     * 
     * @return namespace style
     */
    public String getFormatterClass() {
        if (m_javadocFormatter == null) {
            if (getParent() == null) {
                return IDocumentFormatter.DEFAULT_IMPLEMENTATION;
            } else {
                return getParent().getFormatterClass();
            }
        } else {
            return m_javadocFormatter;
        }
    }
    
    /**
     * Get namespace specified on this element. This method is only intended for use by subclasses - otherwise the
     * {@link #getNamespace()} method should instead be used.
     * 
     * @return namespace (<code>null</code> if none)
     */
    protected String getSpecifiedNamespace() {
        return m_namespace;
    }
    
    /**
     * Get namespace derivation style.
     * 
     * @return namespace style
     */
    public int getNamespaceStyle() {
        if (m_namespaceStyle == null) {
            if (getParent() == null) {
                return DERIVE_BY_PACKAGE;
            } else {
                return getParent().getNamespaceStyle();
            }
        } else {
            return m_namespaceStyle.intValue();
        }
    }
    
    /**
     * Set namespace derivation style.
     * 
     * @param style (<code>null</code> if none at this level)
     */
    public void setNamespaceStyle(Integer style) {
        m_namespaceStyle = style;
    }
    
    /**
     * Get name style.
     * 
     * @return name style
     */
    public int getNameStyle() {
        if (m_nameStyle == null) {
            if (getParent() == null) {
                return CAMEL_CASE_NAMES;
            } else {
                return getParent().getNameStyle();
            }
        } else {
            return m_nameStyle.intValue();
        }
    }
    
    /**
     * Set name style.
     * 
     * @param style (<code>null</code> if none at this level)
     */
    public void setNameStyle(Integer style) {
        m_nameStyle = style;
    }
    
    /**
     * Get the namespace for schema definitions. This value must be set by subclasses using the
     * {@link #setNamespace(String)} method.
     * 
     * @return schema namespace
     */
    public final String getNamespace() {
        return m_actualNamespace;
    }
    
    /**
     * Set the namespace to be used for schema definitions. This method is only intended for use by subclasses.
     * 
     * @param ns
     */
    protected void setNamespace(String ns) {
        m_actualNamespace = ns;
    }
    
    /**
     * Check if primitive value should be treated as required. If not set at any nesting level, the default is
     * <code>true</code>.
     * 
     * @param type primitive type
     * @return <code>true</code> if required value, <code>false</code> if not
     */
    public boolean isPrimitiveRequired(String type) {
        if (m_require == null) {
            if (getParent() == null) {
                return true;
            } else {
                return getParent().isPrimitiveRequired(type);
            }
        } else {
            return m_require.intValue() == REQUIRE_ALL || m_require.intValue() == REQUIRE_PRIMITIVES;
        }
    }
    
    /**
     * Check if object value should be treated as required. If not set at any nesting level, the default is
     * <code>false</code>.
     * 
     * @param type object type
     * @return <code>true</code> if required value, <code>false</code> if not
     */
    public boolean isObjectRequired(String type) {
        if (m_require == null) {
            if (getParent() == null) {
                return false;
            } else {
                return getParent().isObjectRequired(type);
            }
        } else {
            return m_require.intValue() == REQUIRE_ALL || m_require.intValue() == REQUIRE_OBJECTS;
        }
    }
    
    /**
     * Check if JavaDocs should be used for documentation. If not set at any nesting level, the default is
     * <code>true</code>.
     * 
     * @return force names flag
     */
    public boolean isUseJavaDocs() {
        if (m_useJavaDocs == null) {
            if (getParent() == null) {
                return true;
            } else {
                return getParent().isUseJavaDocs();
            }
        } else {
            return m_useJavaDocs.booleanValue();
        }
    }
    
    /**
     * Convert class or unprefixed field name to element or attribute name using current format.
     * 
     * @param base class or simple field name to be converted
     * @return element or attribute name
     */
    public String convertName(String base) {
        return convertName(base, getNameStyle());
    }
    
    /**
     * Name style set text method. This is intended for use during unmarshalling. TODO: add validation
     * 
     * @param text
     * @param ictx
     */
    private void setNameStyleText(String text, IUnmarshallingContext ictx) {
        if (text == null) {
            m_nameStyle = null;
        } else {
            m_nameStyle = new Integer(s_nameStyleEnum.getValue(text));
        }
    }
    
    /**
     * Name style get text method. This is intended for use during marshalling.
     * 
     * @return text
     */
    private String getNameStyleText() {
        if (m_nameStyle == null) {
            return null;
        } else {
            return s_nameStyleEnum.getName(m_nameStyle.intValue());
        }
    }
    
    /**
     * Namespace style set text method. This is intended for use during unmarshalling. TODO: add validation
     * 
     * @param text
     * @param ictx
     */
    private void setNamespaceStyleText(String text, IUnmarshallingContext ictx) {
        if (text == null) {
            m_namespaceStyle = null;
        } else {
            m_namespaceStyle = new Integer(s_namespaceStyleEnum.getValue(text));
        }
    }
    
    /**
     * Namespace style get text method. This is intended for use during marshalling.
     * 
     * @return text
     */
    private String getNamespaceStyleText() {
        if (m_namespaceStyle == null) {
            return null;
        } else {
            return s_namespaceStyleEnum.getName(m_namespaceStyle.intValue());
        }
    }
    
    /**
     * Required set text method. This is intended for use during unmarshalling. TODO: add validation
     * 
     * @param text
     * @param ictx
     */
    private void setRequireText(String text, IUnmarshallingContext ictx) {
        if (text == null) {
            m_require = null;
        } else {
            m_require = new Integer(s_requireEnum.getValue(text));
        }
    }
    
    /**
     * Required get text method. This is intended for use during marshalling.
     * 
     * @return text
     */
    private String getRequireText() {
        if (m_require == null) {
            return null;
        } else {
            return s_requireEnum.getName(m_require.intValue());
        }
    }
}
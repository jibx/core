/*
Copyright (c) 2004-2005, Dennis M. Sosnoski
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

/**
 * Model component for <b>template</b> element of binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class TemplateElement extends TemplateElementBase
{
    /** Template label. */
    private String m_label;
    
    /** Default template for type flag. */
    private boolean m_isDefault;
    
    /** Schema type name for xsi:type. */
    private NameAttributes m_typeNameAttrs;
    
    /** Base schema type name for xsi:type. */
    private NameAttributes m_baseNameAttrs;

    /** Base template extended by this one. */
    private TemplateElement m_extendsMapping;
    
	/**
	 * Default constructor.
	 */
	public TemplateElement() {
        super(TEMPLATE_ELEMENT);
        m_typeNameAttrs = new NameAttributes();
    }
    
    /**
     * Get template label.
     * 
     * @return template label (<code>null</code> if none)
     */
    public String getLabel() {
        return m_label;
    }
    
    /**
     * Set template label.
     * 
     * @param label template label (<code>null</code> if none)
     */
    public void setLabel(String label) {
        m_label = label;
    }
    
    /**
     * Check if default template for type.
     * 
     * @return <code>true</code> if default for type, <code>false</code> if not
     */
    public boolean isDefaultTemplate() {
        return m_isDefault;
    }
    
    /**
     * Set default template for type flag.
     * 
     * @param dflt <code>true</code> if default for type, <code>false</code> if
     * not
     */
    public void setDefaultTemplate(boolean dflt) {
        m_isDefault = dflt;
    }
    
    //
    // Type name attribute delegate methods
    
    /**
     * Get type name.
     * 
     * @return type name text
     */
    public String getTypeName() {
        return m_typeNameAttrs.getName();
    }
    
    /**
     * Set type name.
     * 
     * @param name text for type name
     */
    public void setTypeName(String name) {
        m_typeNameAttrs.setName(name);
    }
    
    /**
     * Get namespace URI specified for type.
     * 
     * @return type namespace URI (<code>null</code> if not set)
     */
    public String getTypeUri() {
        return m_typeNameAttrs.getUri();
    }
    
    /**
     * Set type namespace URI.
     * 
     * @param uri type namespace URI (<code>null</code> if not set)
     */
    public void setTypeUri(String uri) {
        m_typeNameAttrs.setUri(uri);
    }
    
    /**
     * Get namespace prefix specified for type.
     * 
     * @return type namespace prefix (<code>null</code> if not set)
     */
    public String getTypePrefix() {
        return m_typeNameAttrs.getPrefix();
    }
    
    /**
     * Set type namespace prefix.
     * 
     * @param prefix namespace prefix (<code>null</code> if not set)
     */
    public void setTypePrefix(String prefix) {
        m_typeNameAttrs.setPrefix(prefix);
    }
    
    /**
     * Get effective namespace information for type. This call is only
     * meaningful after validation.
     * 
     * @return effective namespace information
     */
    public NamespaceElement getTypeNamespace() {
        return m_typeNameAttrs.getNamespace();
    }
    
    /**
     * Get template extended by this one.
     * 
     * @return template extended by this one
     */
    public TemplateElement getExtendsMapping() {
        return m_extendsMapping;
    }
    
    //
    // Base type name attribute delegate methods
    
    /**
     * Get base type name.
     * 
     * @return base type name text
     */
    public String getBaseName() {
        return m_baseNameAttrs.getName();
    }
    
    /**
     * Set base type name.
     * 
     * @param name text for base type name
     */
    public void setBaseName(String name) {
        m_baseNameAttrs.setName(name);
    }
    
    /**
     * Get namespace URI specified for base type.
     * 
     * @return base type namespace URI (<code>null</code> if not set)
     */
    public String getBaseUri() {
        return m_baseNameAttrs.getUri();
    }
    
    /**
     * Set base type namespace URI.
     * 
     * @param uri base type namespace URI (<code>null</code> if  if not set)
     */
    public void setBaseUri(String uri) {
        m_baseNameAttrs.setUri(uri);
    }
    
    /**
     * Get namespace URI specified for base type.
     * 
     * @return base type namespace prefix (<code>null</code> if not set)
     */
    public String getBasePrefix() {
        return m_baseNameAttrs.getPrefix();
    }
    
    /**
     * Set base type namespace prefix.
     * 
     * @param prefix base type namespace prefix (<code>null</code> if not set)
     */
    public void setBasePrefix(String prefix) {
        m_baseNameAttrs.setPrefix(prefix);
    }
    
    /**
     * Get effective namespace information for base type. This call is only
     * meaningful after validation.
     * 
     * @return effective namespace information
     */
    public NamespaceElement getBaseNamespace() {
        return m_typeNameAttrs.getNamespace();
    }
    
    //
    // Validation methods
    
    /**
     * Prevalidate attributes of element in isolation.
     *
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {
        m_typeNameAttrs.prevalidate(vctx);
        super.prevalidate(vctx);
    }
}
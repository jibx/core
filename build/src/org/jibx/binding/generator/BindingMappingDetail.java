/*
 * Copyright (c) 2007, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.binding.generator;

import java.util.Map;

import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.runtime.QName;

/**
 * Holder for the details of how a class is going to be mapped. This information is needed in order to determine how to
 * reference the mapped class when it's used within another mapping definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class BindingMappingDetail
{
    /** Fully-qualified class name of mapped class. */
    private final String m_type;
    
    /** Generate abstract mapping flag. */
    private boolean m_useAbstract;
    
    /** Generate concrete mapping flag. */
    private boolean m_useConcrete;
    
    /** Flag for class extended by other mapped class(es). */
    private boolean m_isExtended;
    
    /** Abstract mapping type name (<code>null</code> if none). */
    private final QName m_typeQName;
    
    /** Concrete mapping element name (<code>null</code> if none). */
    private QName m_elementQName;
    
    /** Concrete mapping type extended by this one. */
    private String m_extendsType;
    
    /** Map from access method name to property customization information for properties covered by this mapping
     (including inherited ones). */
    private Map m_propertyMethodMap;
    
    /** Flag for mapping(s) has been generated. */
    private boolean m_isGenerated;
    
    /** Abstract mapping definition (<code>null</code> if none). */
    private MappingElement m_abstractMapping;
    
    /** Concrete mapping definition (<code>null</code> if none). */
    private MappingElementBase m_concreteMapping;
    
    /**
     * Constructor.
     * 
     * @param type fully-qualified mapped class name
     * @param aname abstract mapping type name
     * @param cname concrete mapping element name
     * @param stype superclass for extension (<code>null</code> if not an extension mapping)
     */
    protected BindingMappingDetail(String type, QName aname, QName cname, String stype) {
        m_type = type;
        m_typeQName = aname;
        m_elementQName = cname;
        m_extendsType = stype;
    }
    
    /**
     * Get fully-qualified name of mapped class.
     *
     * @return class name
     */
    public String getType() {
        return m_type;
    }

    /**
     * Get the abstract &lt;mapping> for the target class.
     *
     * @return abstract <mapping>, <code>null</code> if none
     */
    public MappingElement getAbstractMapping() {
        return m_abstractMapping;
    }
    
    /**
     * Set the abstract &lt;mapping> for the target class.
     *
     * @param abs abstract <mapping>, <code>null</code> if none
     */
    public void setAbstractMapping(MappingElement abs) {
        m_abstractMapping = abs;
    }
    
    /**
     * Get the concrete &lt;mapping> for the target class.
     *
     * @return concrete <mapping>, <code>null</code> if none
     */
    public MappingElementBase getConcreteMapping() {
        return m_concreteMapping;
    }
    
    /**
     * Set the concrete &lt;mapping> for the target class.
     *
     * @param con concrete <mapping>, <code>null</code> if none
     */
    public void setConcreteMapping(MappingElementBase con) {
        m_concreteMapping = con;
    }
    
    /**
     * Check for target class extended by other mapped class(es).
     *
     * @return <code>true</code> if extended, <code>false</code> if not
     */
    public boolean isExtended() {
        return m_isExtended;
    }
    
    /**
     * Set target class extended by other mapped class(es). Setting this <code>true</code> forces a concrete
     * &lt;mapping> to be used.
     *
     * @param ext
     */
    public void setExtended(boolean ext) {
        m_isExtended = ext;
        if (ext) {
            setUseConcrete(true);
        }
    }
    
    /**
     * Check if this &lt;mapping> has been generated.
     *
     * @return <code>true</code> if generated, <code>false</code> if not
     */
    public boolean isGenerated() {
        return m_isGenerated;
    }
    
    /**
     * Set flag for &lt;mapping> generated.
     *
     * @param gen
     */
    public void setGenerated(boolean gen) {
        m_isGenerated = gen;
    }
    
    /**
     * Check if an abstract &lt;mapping> used for this class.
     *
     * @return <code>true</code> if abstract mapping used, <code>false</code> if not
     */
    public boolean isUseAbstract() {
        return m_useAbstract;
    }
    
    /**
     * Set flag for abstract &lt;mapping> used for this class.
     *
     * @param abs <code>true</code> if abstract mapping used, <code>false</code> if not
     */
    public void setUseAbstract(boolean abs) {
        m_useAbstract = abs;
    }
    
    /**
     * Check if a concrete &lt;mapping> used for this class.
     *
     * @return <code>true</code> if concrete mapping used, <code>false</code> if not
     */
    public boolean isUseConcrete() {
        return m_useConcrete;
    }
    
    /**
     * Set flag for concrete &lt;mapping> used for this class.
     *
     * @param con <code>true</code> if concrete mapping used, <code>false</code> if not
     */
    public void setUseConcrete(boolean con) {
        m_useConcrete = con;
        if (!con && m_isExtended) {
            throw new IllegalStateException("Internal error - concrete <mapping> must be used for extensions");
        }
    }
    
    /**
     * Get the element name used for a concrete mapping. Note that this method will always return a
     * non-<code>null</code> result if a concrete mapping is being used, but may also return a non-<code>null</code>
     * result even if there is no concrete mapping - so check first using the {@link #isUseConcrete()} method.
     *
     * @return element name, or <code>null</code> if not defined
     */
    public QName getElementQName() {
        return m_elementQName;
    }
    
    /**
     * Set the element name used for a concrete mapping.
     *
     * @param qname element name, or <code>null</code> if not defined
     */
    public void setElementQName(QName qname) {
        m_elementQName = qname;
    }
    
    /**
     * Get the fully-qualified class name of the type extended by this &lt;mapping>.
     *
     * @return class name, or <code>null</code> if none
     */
    public String getExtendsType() {
        return m_extendsType;
    }
    
    /**
     * Get the type name used for an abstract mapping. Note that this method will always return a non-<code>null</code>
     * result if an abstract mapping is being used, but may also return a non-<code>null</code> result even if there is
     * no abstract mapping - so check first using the {@link #isUseConcrete()} method.
     *
     * @return type name, or <code>null</code> if not defined
     */
    public QName getTypeQName() {
        return m_typeQName;
    }
    
    /**
     * Get map from access method name to property customization information for properties covered by this
     * &lt;mapping>. This map includes both properties defined directly, and those inherited from any base mapping.
     * 
     * @return map (non-<code>null</code> after &lt;mapping> constructed)
     */
    public Map getAccessMethodMap() {
        return m_propertyMethodMap;
    }
    
    /**
     * Set map from access method name to property customization information for properties covered by this
     * &lt;mapping>. This must be done at the time the &lt;mapping> is constructed.
     * 
     * @param map (non-<code>null</code>, use empty map if not applicable)
     */
    public void setAccessMethodMap(Map map) {
        m_propertyMethodMap = map;
    }
}
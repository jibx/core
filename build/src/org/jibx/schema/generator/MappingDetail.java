package org.jibx.schema.generator;

import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.runtime.QName;

/**
 * Holder for the details of how a mapping is to be represented in a schema. Each mapping is converted to a complex
 * type, but the complex type may be either global or local to a containing global element. In the case of a mapping
 * used as the base for one or more extension types, both a global complex type and a global element that references the
 * type are required. This also tracks the content form for the complex type definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class MappingDetail
{
    /** Mapping to be generated. */
    private final MappingElementBase m_mapping;
    
    /** Schema type extension base mapping. */
    private final MappingElement m_extensionBase;
    
    /** Has child element(s) flag. */
    private final boolean m_hasChild;
    
    /** Has child text(s) flag. */
    private final boolean m_hasText;
    
    /** Has attribute(s) flag. */
    private final boolean m_hasAttribute;
    
    /** Type name (ignored if not generated as complex type). */
    private final QName m_typeName;
    
    /**
     * Element/group/attributeGroup name (ignored if not generated as any of these).
     */
    private final QName m_otherName;
    
    /** Substitution group base name. */
    private QName m_substitutionName;
    
    /** Generate as complex type flag. */
    private boolean m_isType;
    
    /** Generate as element flag. */
    private boolean m_isElement;
    
    /**
     * Generate as group/attributeGroup flag. If set, will be generated as either a group (if elements defined), an
     * attributeGroup (if attributes defined), or both.
     */
    private boolean m_isGroup;
    
    /**
     * Constructor.
     * 
     * @param map mapping definition
     * @param haschild has child element(s) flag
     * @param hastext has child text(s) flag
     * @param base base mapping for schema type extension
     * @param tname name as type
     * @param oname name as element/group/attributeGroup
     */
    public MappingDetail(MappingElementBase map, boolean haschild, boolean hastext, MappingElement base, QName tname,
        QName oname) {
        m_mapping = map;
        m_extensionBase = base;
        m_hasChild = haschild;
        m_hasText = hastext;
        m_hasAttribute = map.getAttributeComponents().size() > 0;
        m_typeName = tname;
        m_otherName = oname;
        if (map.isAbstract() && map.getTypeName() != null) {
            m_isType = true;
        } else if (!map.isAbstract()) {
            m_isElement = true;
        }
    }
    
    /**
     * Check if generating as an element.
     * 
     * @return flag
     */
    public boolean isElement() {
        return m_isElement;
    }
    
    /**
     * Set generating as an element.
     * 
     * @param gen
     */
    public void setElement(boolean gen) {
        m_isElement = gen;
    }
    
    /**
     * Check if generating as a group.
     * 
     * @return flag
     */
    public boolean isGroup() {
        return m_isGroup;
    }
    
    /**
     * Set generating as a group.
     * 
     * @param gen
     */
    public void setGroup(boolean gen) {
        m_isGroup = gen;
    }
    
    /**
     * Check if generating as a group.
     * 
     * @return flag
     */
    public boolean isType() {
        return m_isType;
    }
    
    /**
     * Set generating as a type.
     * 
     * @param gen
     */
    public void setType(boolean gen) {
        m_isType = gen;
    }
    
    /**
     * Get base mapping for schema type extension.
     * 
     * @return extension base
     */
    public MappingElement getExtensionBase() {
        return m_extensionBase;
    }
    
    /**
     * Check if attribute component present.
     * 
     * @return flag
     */
    public boolean hasAttribute() {
        return m_hasAttribute;
    }
    
    /**
     * Check if child element component present.
     * 
     * @return flag
     */
    public boolean hasChild() {
        return m_hasChild;
    }
    
    /**
     * Check if text component present.
     * 
     * @return flag
     */
    public boolean hasText() {
        return m_hasText;
    }
    
    /**
     * Get mapping.
     * 
     * @return mapping
     */
    public MappingElementBase getMapping() {
        return m_mapping;
    }
    
    /**
     * Get name for type.
     * 
     * @return name
     */
    public QName getTypeName() {
        return m_typeName;
    }
    
    /**
     * Get element name.
     * 
     * @return element name for concrete mapping (<code>null</code> if abstract)
     */
    public QName getOtherName() {
        return m_otherName;
    }
    
    /**
     * Get substitution group base name.
     * 
     * @return substitution group base name
     */
    public QName getSubstitution() {
        return m_substitutionName;
    }
    
    /**
     * Set substitution group base name.
     * 
     * @param qname
     */
    public void setSubstitution(QName qname) {
        m_substitutionName = qname;
    }
}
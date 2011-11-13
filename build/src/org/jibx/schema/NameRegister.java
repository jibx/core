/*
 * Copyright (c) 2006-2008, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema;

import java.util.HashMap;
import java.util.Iterator;

import org.jibx.runtime.QName;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupElement;
import org.jibx.schema.elements.CommonTypeDefinition;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.GroupElement;
import org.jibx.schema.support.SchemaTypes;

/**
 * Holder for registration of all global components of a schema by name.
 * 
 * @author Dennis M. Sosnoski
 */
public class NameRegister
{
    /** Direct attribute definitions. */
    private HashMap m_globalAttributeMap;
    
    /**
     * External attribute definitions (lazy create, <code>null</code> if unused).
     */
    private HashMap m_importedAttributeMap;
    
    /** Direct attribute group definitions. */
    private HashMap m_globalAttributeGroupMap;
    
    /**
     * External attribute group definitions (lazy create, <code>null</code> if unused).
     */
    private HashMap m_importedAttributeGroupMap;
    
    /** Direct element definitions. */
    private HashMap m_globalElementMap;
    
    /**
     * External element definitions (lazy create, <code>null</code> if unused).
     */
    private HashMap m_importedElementMap;
    
    /** Direct group definitions. */
    private HashMap m_globalGroupMap;
    
    /**
     * External group definitions (lazy create, <code>null</code> if unused).
     */
    private HashMap m_importedGroupMap;
    
    /** Direct type definitions. */
    private HashMap m_globalTypeMap;
    
    /** External type definitions (lazy create, <code>null</code> if unused). */
    private HashMap m_importedTypeMap;
    
    /**
     * Constructor.
     */
    public NameRegister() {
        m_globalAttributeMap = new HashMap();
        m_globalAttributeGroupMap = new HashMap();
        m_globalElementMap = new HashMap();
        m_globalGroupMap = new HashMap();
        m_globalTypeMap = new HashMap();
    }
    
    /**
     * Reset register for reuse.
     */
    public void reset() {
        m_globalAttributeMap.clear();
        m_globalAttributeGroupMap.clear();
        m_globalElementMap.clear();
        m_globalGroupMap.clear();
        m_globalTypeMap.clear();
    }
    
    //
    // ValidationContext implementation methods
    
    /**
     * Register global attribute in the current schema definition.
     * 
     * @param qname name
     * @param def attribute definition
     * @return prior registered definition (<code>null</code> if none)
     */
    public AttributeElement registerAttribute(QName qname, AttributeElement def) {
        return (AttributeElement)m_globalAttributeMap.put(qname, def);
    }
    
    /**
     * Register global attribute group in the current schema definition.
     * 
     * @param qname name
     * @param def attribute definition
     * @return prior registered definition (<code>null</code> if none)
     */
    public AttributeGroupElement registerAttributeGroup(QName qname, AttributeGroupElement def) {
        return (AttributeGroupElement)m_globalAttributeGroupMap.put(qname, def);
    }
    
    /**
     * Register global element in the current schema definition.
     * 
     * @param qname name
     * @param def element definition
     * @return prior registered definition (<code>null</code> if none)
     */
    public ElementElement registerElement(QName qname, ElementElement def) {
        return (ElementElement)m_globalElementMap.put(qname, def);
    }
    
    /**
     * Register global group in the current schema definition.
     * 
     * @param qname name
     * @param def attribute definition
     * @return prior registered definition (<code>null</code> if none)
     */
    public GroupElement registerGroup(QName qname, GroupElement def) {
        return (GroupElement)m_globalGroupMap.put(qname, def);
    }
    
    /**
     * Register global type in the current schema definition.
     * 
     * @param qname name
     * @param def attribute definition
     * @return prior registered definition (<code>null</code> if none)
     */
    public CommonTypeDefinition registerType(QName qname, CommonTypeDefinition def) {
        return (CommonTypeDefinition)m_globalTypeMap.put(qname, def);
    }
    
    /**
     * Find value in main or backup map. If the (non-<code>null</code>) value is present in the main map it is
     * returned directly; otherwise, if the backup map is non-<code>null</code> it is checked.
     * 
     * @param key
     * @param map1 main map
     * @param map2 backup map (<code>null</code> if none)
     * @return value (<code>null</code> if value for key not in either map)
     */
    private Object findInMaps(Object key, HashMap map1, HashMap map2) {
        Object obj = map1.get(key);
        if (obj == null && map2 != null) {
            obj = map2.get(key);
        }
        return obj;
    }
    
    /**
     * Find global attribute by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public AttributeElement findAttribute(QName qname) {
        return (AttributeElement)findInMaps(qname, m_globalAttributeMap, m_importedAttributeMap);
    }
    
    /**
     * Find attribute group by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public AttributeGroupElement findAttributeGroup(QName qname) {
        AttributeGroupElement agrp = (AttributeGroupElement)m_globalAttributeGroupMap.get(qname);
        if (agrp == null && m_importedAttributeGroupMap != null) {
            agrp = (AttributeGroupElement)m_importedAttributeGroupMap.get(qname);
        }
        return agrp;
    }
    
    /**
     * Find global element by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public ElementElement findElement(QName qname) {
        ElementElement elem = (ElementElement)m_globalElementMap.get(qname);
        if (elem == null && m_importedElementMap != null) {
            elem = (ElementElement)m_importedElementMap.get(qname);
        }
        return elem;
    }
    
    /**
     * Find group by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public GroupElement findGroup(QName qname) {
        GroupElement grp = (GroupElement)m_globalGroupMap.get(qname);
        if (grp == null && m_importedGroupMap != null) {
            grp = (GroupElement)m_importedGroupMap.get(qname);
        }
        return grp;
    }
    
    /**
     * Find global type by name.
     * 
     * @param qname name
     * @return definition, or <code>null</code> if not registered
     */
    public CommonTypeDefinition findType(QName qname) {
        if (IComponent.SCHEMA_NAMESPACE.equals(qname.getUri())) {
            return SchemaTypes.getSchemaType(qname.getName());
        } else {
            CommonTypeDefinition type = (CommonTypeDefinition)m_globalTypeMap.get(qname);
            if (type == null && m_importedTypeMap != null) {
                type = (CommonTypeDefinition)m_importedTypeMap.get(qname);
            }
            return type;
        }
    }
    
    /**
     * Merge definitions directly into this register.
     * 
     * @param mrg register supplying definitions to be merged
     */
    public void mergeDefinitions(NameRegister mrg) {
        m_globalAttributeMap.putAll(mrg.m_globalAttributeMap);
        m_globalAttributeGroupMap.putAll(mrg.m_globalAttributeGroupMap);
        m_globalElementMap.putAll(mrg.m_globalElementMap);
        m_globalGroupMap.putAll(mrg.m_globalGroupMap);
        m_globalTypeMap.putAll(mrg.m_globalTypeMap);
    }
    
    /**
     * Merge one QName map into another, changing the namespace URI for keys in the source map.
     * 
     * @param uri namespace URI to be used for keys from source map
     * @param source
     * @param target
     */
    private void mergeMapNamespaced(String uri, HashMap source, HashMap target) {
        if (!source.isEmpty()) {
            for (Iterator iter = source.keySet().iterator(); iter.hasNext();) {
                QName oldname = (QName)iter.next();
                QName newname = new QName(uri, oldname.getName());
                target.put(newname, source.get(oldname));
            }
        }
    }
    
    /**
     * Merge external definitions into this register.
     * 
     * @param uri namespace URI to be used for merged external definitions
     * @param mrg register supplying external definitions
     */
    public void mergeDefinitionsNamespaced(String uri, NameRegister mrg) {
        mergeMapNamespaced(uri, mrg.m_globalAttributeMap, m_globalAttributeMap);
        mergeMapNamespaced(uri, mrg.m_globalAttributeGroupMap, m_globalAttributeGroupMap);
        mergeMapNamespaced(uri, mrg.m_globalElementMap, m_globalElementMap);
        mergeMapNamespaced(uri, mrg.m_globalGroupMap, m_globalGroupMap);
        mergeMapNamespaced(uri, mrg.m_globalTypeMap, m_globalTypeMap);
    }
    
    /**
     * Merge one map into another, where the source map may be empty and the target map may be <code>null</code>. If
     * the source map is nonempty but the target is <code>null</code>, this creates a new map for the target and
     * returns that map; otherwise, the map returned is always the same as the target map passed in.
     * 
     * @param source
     * @param target (<code>null</code> if none)
     * @return target (possibly changed, if the supplied target was <code>null</code>)
     */
    private HashMap mergeLazyMap(HashMap source, HashMap target) {
        if (!source.isEmpty()) {
            if (target == null) {
                return new HashMap(source);
            } else {
                target.putAll(source);
                return target;
            }
        } else {
            return target;
        }
    }
    
    /**
     * Merge external definitions into this register.
     * 
     * @param mrg register supplying external definitions
     */
    public void mergeImportedDefinitions(NameRegister mrg) {
        m_importedAttributeMap = mergeLazyMap(mrg.m_globalAttributeMap, m_importedAttributeMap);
        m_importedAttributeGroupMap = mergeLazyMap(mrg.m_globalAttributeGroupMap, m_importedAttributeGroupMap);
        m_importedElementMap = mergeLazyMap(mrg.m_globalElementMap, m_importedElementMap);
        m_importedGroupMap = mergeLazyMap(mrg.m_globalGroupMap, m_importedGroupMap);
        m_importedTypeMap = mergeLazyMap(mrg.m_globalTypeMap, m_importedTypeMap);
    }
}
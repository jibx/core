/*
 * Copyright (c) 2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Partial implementation of {@link java.util.Map} which provides a merged view of a defaults map with an overrides map.
 * Although this can be used as a map for most purposes, methods which return live views of the keys or values in the
 * map only take into account the overrides, not the defaults.
 */
public class ChainedMap implements Map
{
    /** Default values map. */
    private final Map m_defaults;
    
    /** Override values map. */
    private final Map m_overrides;
    
    /**
     * Constructor.
     * 
     * @param defaults map providing defaults for keys not set directly
     */
    public ChainedMap(Map defaults) {
        m_defaults = defaults;
        m_overrides = new HashMap();
    }
    
    /**
     * Clear all override key-value pairs. This only effects the overrides, not the defaults.
     */
    public void clear() {
        m_defaults.clear();
    }

    /**
     * Check if a key has a defined value. This will return <code>true</code> if the key is present in the overrides map
     * with a non-null value, or if the key is not present in the overrides map but is present in the defaults map.
     *
     * @param key
     * @return <code>true</code> if key defined, <code>false</code> if not
     */
    public boolean containsKey(Object key) {
        if (m_overrides.containsKey(key)) {
            return m_overrides.get(key) != null;
        } else {
            return m_defaults.containsKey(key);
        }
    }

    /**
     * Check if a value is present. This only checks for the value in the overrides map.
     *
     * @param value
     * @return <code>true</code> if value present as an override, <code>false</code> if not
     */
    public boolean containsValue(Object value) {
        return m_overrides.containsValue(value);
    }

    /**
     * Get the set of entries. This only returns the entries in the overrides map.
     *
     * @return override entries
     */
    public Set entrySet() {
        return m_overrides.entrySet();
    }

    /**
     * Get value for key. If the key is present in the overrides map, the value from that map is returned; otherwise,
     * the value for the key in the defaults map is returned.
     *
     * @param key
     * @return value (<code>null</code> if key not present)
     */
    public Object get(Object key) {
        if (m_overrides.containsKey(key)) {
            return m_overrides.get(key);
        } else {
            return m_defaults.get(key);
        }
    }

    /**
     * Check if no overrides are defined.
     *
     * @return <code>true</code> if no overrides, <code>false</code> if any present
     */
    public boolean isEmpty() {
        return m_overrides.isEmpty();
    }

    /**
     * Get the set of keys. This only returns the keys in the overrides map.
     *
     * @return keys
     */
    public Set keySet() {
        return m_overrides.keySet();
    }

    /**
     * Set an override value. This just adds the key-value pair to the override map.
     *
     * @param key
     * @param value
     * @return previous value for key (from default map, if not present in overrides)
     */
    public Object put(Object key, Object value) {
        Object prior;
        if (m_overrides.containsKey(key)) {
            prior = m_overrides.put(key, value);
        } else {
            m_overrides.put(key, value);
            prior = m_defaults.get(key);
        }
        return prior;
    }

    /**
     * Add all key-value pairs from another map into the overrides map.
     *
     * @param map
     */
    public void putAll(Map map) {
        m_overrides.putAll(map);
    }

    /**
     * Remove a key-value pair. If the key was previously present in the overrides map it is simply removed from that
     * map. If it was not present in the overrides map but is present in the defaults map, a null entry is added to the
     * overrides map for that key.
     *
     * @param key
     * @return previous value for key
     */
    public Object remove(Object key) {
        if (m_overrides.containsKey(key)) {
            return m_overrides.remove(key);
        } else {
            return m_defaults.remove(key);
        }
    }

    /**
     * Get the number of entries in the map. This returns the entry count for the overrides map only.
     *
     * @return entry count
     */
    public int size() {
        return m_overrides.size();
    }

    /**
     * Get the values. This returns only the values in the overrides map.
     *
     * @return values
     */
    public Collection values() {
        return m_overrides.values();
    }
}
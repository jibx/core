/*
Copyright (c) 2006-2007, Dennis M. Sosnoski
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

package org.jibx.binding.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jibx.binding.model.EmptyArrayList;

/**
 * Map supporting multiple values for a single key. The multiple value concept
 * doesn't really fit with the standard collections idea of a map, so this
 * provides its own variation of a map interface rather than extend the standard
 * one.
 *
 * @author Dennis M. Sosnoski
 */
public class MultipleValueMap
{
    /** Backing map from key to value or array of values. */
    private final HashMap m_backingMap;
    
    /** Actual number of values (not keys) present in map. */
    private int m_valueCount;
    
    /** Last lookup key (<code>null</code> if none, or if value changed). */
    private Object m_lastKey;
    
    /** Last lookup value (<code>null</code> if none, or if value changed). */
    private Object m_lastValue;
    
    /**
     * Constructor.
     */
    public MultipleValueMap() {
        super();
        m_backingMap = new HashMap();
    }
    
    /**
     * Internal cached lookup.
     * 
     * @param key
     * @return value
     */
    private Object getMapped(Object key) {
        if (key != m_lastKey) {
            m_lastKey = key;
            m_lastValue = m_backingMap.get(key);
        }
        return m_lastValue;
    }

    /**
     * Clear all entries.
     */
    public void clear() {
        m_backingMap.clear();
        m_valueCount = 0;
    }

    /**
     * Get number of values present for key.
     * 
     * @param key
     * @return value count
     */
    public int getCount(Object key) {
        Object obj = getMapped(key);
        if (obj instanceof MultipleValueList) {
            return ((MultipleValueList)obj).size();
        } else if (obj == null) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Get indexed value for key.
     * 
     * @param key
     * @param index
     * @return value
     */
    public Object get(Object key, int index) {
        Object obj = getMapped(key);
        if (obj instanceof MultipleValueList) {
            return ((MultipleValueList)obj).get(index);
        } else if (obj == null) {
            throw new IndexOutOfBoundsException("No value present for key");
        } else if (index == 0) {
            return obj;
        } else {
            throw new IndexOutOfBoundsException("Only one value present for key");
        }
    }

    /**
     * Add value for key.
     * 
     * @param key
     * @param value
     */
    public void add(Object key, Object value) {
        
        // first force caching of current value
        getMapped(key);
        
        // update value as appropriate
        if (m_lastValue == null) {
            m_backingMap.put(key, value);
            m_lastValue = value;
        } else if (m_lastValue instanceof MultipleValueList) {
            ((MultipleValueList)m_lastValue).add(value);
        } else {
            MultipleValueList list = new MultipleValueList();
            list.add(m_lastValue);
            list.add(value);
            m_backingMap.put(key, list);
            m_lastValue = list;
        }
        m_valueCount++;
    }
    
    /**
     * Get all values for key. This returns the value(s) from the map and
     * returns them in the form of a list.
     * 
     * @param key
     * @return list of values
     */
    public ArrayList get(Object key) {
        Object obj = m_backingMap.get(key);
        if (obj instanceof MultipleValueList) {
            return (MultipleValueList)obj;
        } else if (obj == null) {
            return EmptyArrayList.INSTANCE;
        } else {
            MultipleValueList list = new MultipleValueList();
            list.add(obj);
            return list;
        }
    }
    
    /**
     * Extract all values for key. This removes the value(s) from the map and
     * returns them in the form of a list.
     * 
     * @param key
     * @return prior list of values
     */
    public ArrayList extract(Object key) {
        ArrayList result = get(key);
        m_backingMap.remove(key);
        return result;
    }

    /**
     * Get number of keys.
     * 
     * @return key count
     */
    public int keySize() {
        return m_backingMap.size();
    }

    /**
     * Get number of values.
     * 
     * @return value count
     */
    public int valueSize() {
        return m_valueCount;
    }
    
    /**
     * Get iterator over only the multiple-valued keys present in the map.
     *
     * @return iterator
     */
    public Iterator multipleIterator() {
        return new MultipleIterator();
    }
    
    //
    // Delegated methods

    /**
     * Check key present in map.
     * 
     * @param key
     * @return key present flag
     */
    public boolean containsKey(Object key) {
        return m_backingMap.containsKey(key);
    }

    /**
     * Check if map is empty.
     * 
     * @return empty flag
     */
    public boolean isEmpty() {
        return m_backingMap.isEmpty();
    }

    /**
     * Get key set.
     * 
     * @return set of keys
     */
    public Set keySet() {
        return m_backingMap.keySet();
    }
    
    /**
     * List used for multiple values. This is just a marker, so that the actual
     * values can be anything at all (including lists).
     */
    private static class MultipleValueList extends ArrayList {
        public MultipleValueList() {}
    }
    
    /**
     * Iterator for only the multiple-valued keys in the map.
     */
    public class MultipleIterator implements Iterator
    {
        /** Current key value has been consumed flag. */
        private boolean m_isConsumed;
        
        /** Current key, <code>null</code> if past end. */
        private Object m_currentKey;
        
        /** Iterator through keys present in map. */
        private Iterator m_keyIterator;
        
        /**
         * Constructor. This initializes the key iterator and next key values.
         */
        protected MultipleIterator() {
            m_keyIterator = keySet().iterator();
            m_isConsumed = true;
        }
        
        /**
         * Advance to next multiple-valued key in map.
         */
        private void advance() {
            if (m_isConsumed) {
                m_isConsumed = false;
                m_currentKey = null;
                while (m_keyIterator.hasNext()) {
                    Object key = m_keyIterator.next();
                    if (getCount(key) > 1) {
                        m_currentKey = key;
                        break;
                    }
                }
            }
        }
        
        /**
         * Check for another multiple-valued key present.
         *
         * @return <code>true</code> if present, <code>false</code> if not
         */
        public boolean hasNext() {
            advance();
            return m_currentKey != null;
        }

        /**
         * Get the next multiple-valued key in map. This returns the current
         * next key, advancing to the next next key.
         *
         * @return next multiple-valued key
         */
        public Object next() {
            advance();
            if (m_currentKey == null) {
                throw new NoSuchElementException("Past end of list");
            } else {
                m_isConsumed = true;
                return m_currentKey;
            }
        }

        /**
         * Remove current multiple-valued key.
         */
        public void remove() {
            m_keyIterator.remove();
        }
    }
}
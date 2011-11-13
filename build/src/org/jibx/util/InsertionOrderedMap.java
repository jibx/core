/*
Copyright (c) 2007, Dennis M. Sosnoski
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

package org.jibx.util;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Map with keys iterated in insertion order. This is similar to the Java 1.4
 * java.util.LinkedHashMap class, but compatible with earlier JVM versions. It
 * also guarantees insertion ordering only for iterating through the key values,
 * not for other iterations. This implementation is optimized for insert-only
 * maps.
 */
public class InsertionOrderedMap implements Map
{
    private final Map m_baseMap;
    private final ArrayList m_insertList;
    
    public InsertionOrderedMap() {
        m_baseMap = new HashMap();
        m_insertList = new ArrayList();
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        m_baseMap.clear();
        m_insertList.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return m_baseMap.containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return m_baseMap.containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        return m_baseMap.entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return m_baseMap.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return m_baseMap.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        return new ListSet(m_insertList);
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        if (!m_baseMap.containsKey(key)) {
            m_insertList.add(key);
        }
        return m_baseMap.put(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
        for (Iterator iter = t.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry)iter.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        if (m_baseMap.containsKey(key)) {
            m_insertList.remove(key);
            return m_baseMap.remove(key);
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return m_baseMap.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values() {
        return new ValueCollection();
    }
    
    /**
     * Get list of keys in order added. The returned list is live, and will
     * grow or shrink as pairs are added to or removed from the map.
     *
     * @return key list
     */
    public ArrayList keyList() {
        return m_insertList;
    }
    
    /**
     * Set implementation backed by a list.
     */
    protected static class ListSet extends AbstractSet
    {
        private final List m_list;
        
        public ListSet(List list) {
            m_list = list;
        }
        
        public Iterator iterator() {
            return m_list.iterator();
        }

        public int size() {
            return m_list.size();
        }
    }
    
    protected class ValueCollection extends AbstractCollection
    {
        public Iterator iterator() {
            return new ValueIterator();
        }

        public int size() {
            return m_insertList.size();
        }
    }
    
    protected class ValueIterator implements Iterator
    {
        private int m_index = -1;
        
        public boolean hasNext() {
            return m_index < m_insertList.size()-1;
        }

        public Object next() {
            if (m_index < m_insertList.size()-1) {
                Object key = m_insertList.get(++m_index);
                return m_baseMap.get(key);
            } else {
                throw new NoSuchElementException("Past end of list");
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("Internal error - remove() not supported");
        }
    }
}
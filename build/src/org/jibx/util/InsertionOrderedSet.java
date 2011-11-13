/*
Copyright (c) 2007-2009, Dennis M. Sosnoski.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Set with values iterated in insertion order. This is similar to the Java 1.4
 * java.util.LinkedHashSet class, but compatible with earlier JVM versions. This
 * implementation is for insert-only sets.
 */
public class InsertionOrderedSet implements Set
{
    private final Set m_baseMap;
    private final ArrayList m_insertList;
    
    public InsertionOrderedSet() {
        m_baseMap = new HashSet();
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
     * @see java.util.Set#isEmpty()
     */
    public boolean isEmpty() {
        return m_baseMap.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Set#size()
     */
    public int size() {
        return m_baseMap.size();
    }
    
    /* (non-Javadoc)
     * @see java.util.Set#add(java.lang.Object)
     */
    public boolean add(Object o) {
        if (m_baseMap.contains(o)) {
            return false;
        } else {
            m_baseMap.add(o);
            m_insertList.add(o);
            return true;
        }
    }

    /* (non-Javadoc)
     * @see java.util.Set#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c) {
        boolean changed = false;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            Object item = (Object)iter.next();
            if (add(item)) {
                changed = true;
            }
        }
        return changed;
    }

    /* (non-Javadoc)
     * @see java.util.Set#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return m_baseMap.contains(o);
    }

    /* (non-Javadoc)
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        return m_baseMap.containsAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Set#iterator()
     */
    public Iterator iterator() {
        return m_insertList.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.Set#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("add-only set");
    }

    /* (non-Javadoc)
     * @see java.util.Set#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("add-only set");
    }

    /* (non-Javadoc)
     * @see java.util.Set#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("add-only set");
    }

    /* (non-Javadoc)
     * @see java.util.Set#toArray()
     */
    public Object[] toArray() {
        return m_insertList.toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Set#toArray(T[])
     */
    public Object[] toArray(Object[] a) {
        return m_insertList.toArray(a);
    }
    
    /**
     * Convenience method to add every item in an array.
     *
     * @param objs
     */
    public void addAll(Object[] objs) {
        for (int i = 0; i < objs.length; i++) {
            add(objs[i]);
        }
    }

    /**
     * Get list of values in order added. The returned list is a static copy of
     * the current list.
     *
     * @return list
     */
    public List asList() {
        return Collections.unmodifiableList(m_insertList);
    }
}
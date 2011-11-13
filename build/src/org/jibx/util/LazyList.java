/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski All rights reserved.
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

import java.util.AbstractList;
import java.util.Iterator;

/**
 * List implementation with lazy array construction and modification tracking. The lazy array construction is a minor
 * optimization, to save the added overhead of a backing array for lists which are frequently empty. The modification
 * tracking feature supports filtered list construction with result caching.
 * 
 * @author Dennis M. Sosnoski
 */
public class LazyList extends AbstractList
{
    /** Singleton iterator for empty collection. */
    public static final Iterator EMPTY_ITERATOR = new Iterator() {
        
        public boolean hasNext() {
            return false;
        }
        
        public Object next() {
            throw new IllegalStateException("Internal error - no next");
        }
        
        public void remove() {
            throw new IllegalStateException("Internal error - nothing to remove");
        }
        
    };
    
    /** Unmodifiable empty list instance. */
    public static final LazyList EMPTY_LIST = new LazyList() {
        
        public void add(int index, Object element) {
            throw new UnsupportedOperationException("Internal error: Unmodifiable list");
        }
        
        public Object remove(int index) {
            throw new UnsupportedOperationException("Internal error: Unmodifiable list");
        }
        
        protected void removeRange(int from, int to) {
            throw new UnsupportedOperationException("Internal error: Unmodifiable list");
        }
    };
    
    /** Number of items currently present in list. */
    private int m_size;
    
    /** Maximum number of items allowed before resizing. */
    private int m_limit;
    
    /** Backing array (lazy instantiation, <code>null</code> if not used). */
    private Object[] m_array;
    
    /**
     * Make sure space is available for adding to the list. This grows the size of the backing array, if necessary.
     * 
     * @param count
     */
    private void makeSpace(int count) {
        if (m_limit - m_size < count) {
            Object[] copy;
            if (m_array == null) {
                copy = new Object[count + 4];
            } else {
                copy = new Object[m_size * 2 + count];
                System.arraycopy(m_array, 0, copy, 0, m_size);
            }
            m_array = copy;
            m_limit = copy.length;
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#get(int)
     */
    public Object get(int index) {
        if (index >= 0 && index < m_size) {
            return m_array[index];
        } else {
            throw new IndexOutOfBoundsException("Index " + index + " is out of valid range 0-" + (m_size - 1));
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractCollection#size()
     */
    public int size() {
        return m_size;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {
        if (index >= 0 && index <= m_size) {
            makeSpace(1);
            if (index < m_size) {
                System.arraycopy(m_array, index, m_array, index + 1, m_size - index);
            }
            m_array[index] = element;
            m_size++;
            modCount++;
        } else {
            throw new IndexOutOfBoundsException("Index " + index + " is out of valid range 0-" + m_size);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#iterator()
     */
    public Iterator iterator() {
        if (m_size == 0) {
            return EMPTY_ITERATOR;
        } else {
            return super.iterator();
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#remove(int)
     */
    public Object remove(int index) {
        if (index >= 0 && index < m_size) {
            Object item = m_array[index];
            int start = index + 1;
            System.arraycopy(m_array, start, m_array, index, m_size - start);
            m_array[--m_size] = null;
            modCount++;
            return item;
        } else {
            throw new IndexOutOfBoundsException("Index " + index + " is out of valid range 0-" + (m_size - 1));
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#set(int, java.lang.Object)
     */
    public Object set(int index, Object element) {
        if (index >= 0 && index < m_size) {
            Object item = m_array[index];
            m_array[index] = element;
            return item;
        } else {
            throw new IndexOutOfBoundsException("Index " + index + " is out of valid range 0-" + (m_size - 1));
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.util.AbstractList#removeRange(int, int)
     */
    protected void removeRange(int from, int to) {
        if (from >= 0 && to <= m_size) {
            int length = m_size - to;
            if (length > 0) {
                System.arraycopy(m_array, to, m_array, from, length);
            }
            m_size -= to - from;
            for (int i = m_size; i > to;) {
                m_array[--i] = null;
            }
            modCount++;
        } else {
            throw new IndexOutOfBoundsException("Range of " + from + "-" + to + " exceeds valid range 0-" + m_size);
        }
    }
    
    /**
     * Get modify counter. This supports tracking changes to determine when cached filters need to be updated.
     * 
     * @return count
     */
    public int getModCount() {
        return modCount;
    }
    
    /**
     * Remove range of values. This is just a public version of the protected base class method
     * {@link #removeRange(int, int)}
     * 
     * @param from
     * @param to
     */
    public void remove(int from, int to) {
        removeRange(from, to);
    }
    
    /**
     * Compact the list, removing any <code>null</code> values.
     */
    public void compact() {
        int offset = 0;
        while (offset < m_size) {
            if (m_array[offset] == null) {
                break;
            } else {
                offset++;
            }
        }
        if (offset < m_size) {
            int fill = offset;
            while (++offset < m_size) {
                if (m_array[offset] != null) {
                    m_array[fill++] = m_array[offset];
                }
            }
            m_size = fill;
            modCount++;
        }
    }
}
/*
Copyright (c) 2006, Dennis M. Sosnoski
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

package org.jibx.schema.elements;

import java.util.AbstractList;

import org.jibx.util.LazyList;

/**
 * Virtual list generated from a backing list by filtering on the element types.
 * This exposes a segment of the backing list through the filter, with multiple
 * filters used to expose the entire backing list piecemeal. It can only be used
 * with lists of elements.
 *
 * @author Dennis M. Sosnoski
 */
public class FilteredSegmentList extends AbstractList
{
    /** Base list. */
    private final LazyList m_list;
    
    /** Mask for element types to match in filter. */
    private final long m_matchBits;
    
    /** Filter for elements prior to this filter in list. */
    private final FilteredSegmentList m_prior;
    
    /** Element owning this list. */
    private final OpenAttrBase m_owner;
    
    /** Last modify count matching cached values. */
    private int m_lastModify;
    
    /** Cached filtered list start index in base list. */
    private int m_startIndex;
    
    /** Cached size of filtered list. */
    private int m_size;
    
    /**
     * Dummy default constructor for unmarshalling.
     */
    private FilteredSegmentList() {
        throw new IllegalStateException("Default constructor is not valid");
    }
    
    /**
     * Constructor with everything specified.
     * 
     * @param list backing list
     * @param match included element types mask
     * @param prior filter which comes before this one (<code>null</code> if
     * none)
     * @param owner element owning this list
     */
    public FilteredSegmentList(LazyList list, long match,
        FilteredSegmentList prior, OpenAttrBase owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Internal error - list must have owner");
        }
        m_list = list;
        m_matchBits = match;
        m_prior = prior;
        m_owner = owner;
        m_lastModify = -1;
    }
    
    /**
     * Constructor with no prior filter.
     * 
     * @param list backing list
     * @param match included element types mask
     * @param owner element owning this list
     */
    public FilteredSegmentList(LazyList list, long match, OpenAttrBase owner) {
        this(list, match, null, owner);
    }
    
    /**
     * Update modify count to show cached state is current. This propagates to
     * any prior filter(s).
     */
    private void setModify() {
        m_lastModify = m_list.getModCount();
        if (m_prior != null) {
            m_prior.setModify();
        }
    }
    
    /**
     * Synchronize filter to current list state. If the cached state is not
     * current this updates the cached state to reflect the current state of the
     * backing list.
     */
    private void sync() {
        if (m_lastModify != m_list.getModCount()) {
            
            // updating cache, first update preceding filters
            if (m_prior == null) {
                m_startIndex = 0;
            } else {
                m_prior.sync();
                m_startIndex = m_prior.m_startIndex + m_prior.m_size;
            }
            
            // scan through list to find end of items included by this filter
            int index = m_startIndex;
            while (index < m_list.size()) {
                SchemaBase item = (SchemaBase)m_list.get(index);
                if ((item.bit() & m_matchBits) != 0) {
                    index++;
                } else {
                    break;
                }
            }
            m_size = index - m_startIndex;
            
            // update modify count for cached state
            m_lastModify = m_list.getModCount();
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractList#get(int)
     */
    public Object get(int index) {
        sync();
        if (index >= 0 && index < m_size) {
            return m_list.get(m_startIndex + index);
        } else {
            throw new IndexOutOfBoundsException("Index " + index +
                " is out of valid range 0-" + (m_size-1));
        }
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    public int size() {
        sync();
        return m_size;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {
        sync();
        if (index >= 0 && index <= m_size) {
            
            // make sure added element is allowed by this filter
            SchemaBase item = (SchemaBase)element;
            if ((item.bit() & m_matchBits) != 0) {
                
                // add the element into backing list
                m_list.add(m_startIndex + index, item);
                
                // set parent link for element
                item.setParent(m_owner);
                
                // mark this and prior filters as current
                m_size++;
                setModify();
                modCount++;
                
            } else {
                throw new IllegalArgumentException("Element of type \"" +
                    item.name() + "\" is not allowed by filter");
            }
        } else {
            throw new IndexOutOfBoundsException("Index " + index +
                " is out of valid range 0-" + m_size);
        }
    }
    
    /**
     * Removes the item at the index position.
     *
     * @param index
     * @return removed item
     */
    public Object remove(int index) {
        sync();
        if (index >= 0 && index < m_size) {
            
            // remove the element from backing list
            Object item = m_list.remove(m_startIndex + index);
            ((SchemaBase)item).setParent(null);
            
            // mark this and prior filters as current
            m_size--;
            setModify();
            modCount++;
            return item;
            
        } else {
            throw new IndexOutOfBoundsException("Index " + index +
                " is out of valid range 0-" + (m_size-1));
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        sync();
        for (int i = 0; i < m_size; i++) {
            if (m_list.get(m_startIndex + i) == o) {
                m_list.remove(m_startIndex + i);
                ((SchemaBase)o).setParent(null);
                m_size--;
                setModify();
                modCount++;
                return true;
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractList#set(int, java.lang.Object)
     */
    public Object set(int index, Object element) {
        sync();
        if (index >= 0 && index < m_size) {
            
            // make sure replacement element is allowed by this filter
            SchemaBase item = (SchemaBase)element;
            if ((item.bit() & m_matchBits) != 0) {
                
                // set the element in backing list
                item.setParent(m_owner);
                Object prior = m_list.set(m_startIndex + index, item);
                ((SchemaBase)prior).setParent(null);
                return prior;
                
            } else {
                throw new IllegalArgumentException("Element of type \"" +
                    item.name() + "\" is not allowed by filter");
            }
        } else {
            throw new IndexOutOfBoundsException("Index " + index +
                " is out of valid range 0-" + (m_size-1));
        }
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#removeRange(int, int)
     */
    protected void removeRange(int from, int to) {
        sync();
        if (from >= 0 && to <= m_size) {
            
            // clear the parent linkages
            for (int i = from; i < to; i++) {
                ((SchemaBase)m_list.get(i)).setParent(null);
            }
            
            // delete all items from underlying list
            m_list.remove(from, to);
            
            // mark this and prior filters as current
            m_size -= to - from;
            setModify();
            modCount++;
            
        } else {
            throw new IndexOutOfBoundsException("Range of " + from + "-" + to +
                " exceeds valid range 0-" + m_size);
        }
    }
}
/*
 * Copyright (c) 2006-2007, Dennis M. Sosnoski. All rights reserved.
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

import java.util.Iterator;


/**
 * Hash map for counting references to <code>Object</code> keys. The map implementation is not very efficient when
 * resizing, but works well when the size of the map is known in advance or when accesses are substantially more common
 * than adds.
 * 
 * @author Dennis M. Sosnoski
 */
public class ReferenceCountMap
{
    /** Default fill fraction allowed before growing table. */
    private static final double DEFAULT_FILL = 0.3d;
    
    /** Minimum size used for hash table. */
    private static final int MINIMUM_SIZE = 63;
    
    /** Number of entries present in table. */
    private int m_entryCount;
    
    /** Entries allowed before growing table. */
    private int m_entryLimit;
    
    /** Size of array used for keys. */
    private int m_arraySize;
    
    /** Offset added (modulo table size) to slot number on collision. */
    private int m_hitOffset;
    
    /** Array of key table slots. */
    private Object[] m_keyTable;
    
    /** Array of value table slots. */
    private int[] m_valueTable;
    
    /**
     * Constructor with count.
     * 
     * @param count number of values to assume in initial sizing of table
     */
    public ReferenceCountMap(int count) {
        
        // compute initial table size (ensuring odd)
        m_arraySize = Math.max((int)(count / DEFAULT_FILL), MINIMUM_SIZE);
        m_arraySize += (m_arraySize + 1) % 2;
        
        // initialize the table information
        m_entryLimit = (int)(m_arraySize * DEFAULT_FILL);
        m_hitOffset = m_arraySize / 2;
        m_keyTable = new Object[m_arraySize];
        m_valueTable = new int[m_arraySize];
    }
    
    /**
     * Default constructor.
     */
    public ReferenceCountMap() {
        this(0);
    }
    
    /**
     * Copy (clone) constructor.
     * 
     * @param base instance being copied
     */
    public ReferenceCountMap(ReferenceCountMap base) {
        
        // copy the basic occupancy information
        m_entryCount = base.m_entryCount;
        m_entryLimit = base.m_entryLimit;
        m_arraySize = base.m_arraySize;
        m_hitOffset = base.m_hitOffset;
        
        // copy table of items
        m_keyTable = new Object[m_arraySize];
        System.arraycopy(base.m_keyTable, 0, m_keyTable, 0, m_arraySize);
        m_valueTable = new int[m_arraySize];
        System.arraycopy(base.m_valueTable, 0, m_valueTable, 0, m_arraySize);
    }
    
    /**
     * Step the slot number for an entry. Adds the collision offset (modulo the table size) to the slot number.
     * 
     * @param slot slot number to be stepped
     * @return stepped slot number
     */
    private final int stepSlot(int slot) {
        return (slot + m_hitOffset) % m_arraySize;
    }
    
    /**
     * Find free slot number for entry. Starts at the slot based directly on the hashed key value. If this slot is
     * already occupied, it adds the collision offset (modulo the table size) to the slot number and checks that slot,
     * repeating until an unused slot is found.
     * 
     * @param slot initial slot computed from key
     * @return slot at which entry was added
     */
    private final int freeSlot(int slot) {
        while (m_keyTable[slot] != null) {
            slot = stepSlot(slot);
        }
        return slot;
    }
    
    /**
     * Standard base slot computation for a key.
     * 
     * @param key key value to be computed
     * @return base slot for key
     */
    private final int standardSlot(Object key) {
        return (key.hashCode() & Integer.MAX_VALUE) % m_arraySize;
    }
    
    /**
     * Standard find key in table. This method may be used directly for key lookup using either the
     * <code>hashCode()</code> method defined for the key objects or the <code>System.identityHashCode()</code>
     * method, and either the <code>equals()</code> method defined for the key objects or the <code>==</code>
     * operator, as selected by the hash technique constructor parameter. To implement a hash class based on some other
     * methods of hashing and/or equality testing, define a separate method in the subclass with a different name and
     * use that method instead. This avoids the overhead caused by overrides of a very heavily used method.
     * 
     * @param key to be found in table
     * @return index of matching key, or <code>-index-1</code> of slot to be used for inserting key in table if not
     * already present (always negative)
     */
    private int standardFind(Object key) {
        
        // find the starting point for searching table
        int slot = standardSlot(key);
        
        // scan through table to find target key
        while (m_keyTable[slot] != null) {
            
            // check if we have a match on target key
            if (m_keyTable[slot].equals(key)) {
                return slot;
            } else {
                slot = stepSlot(slot);
            }
            
        }
        return -slot - 1;
    }
    
    /**
     * Reinsert an entry into the hash map. This is used when the table is being directly modified, and does not adjust
     * the count present or check the table capacity.
     * 
     * @param slot position of entry to be reinserted into hash map
     * @return <code>true</code> if the slot number used by the entry has has changed, <code>false</code> if not
     */
    private boolean reinsert(int slot) {
        Object key = m_keyTable[slot];
        m_keyTable[slot] = null;
        return assignSlot(key, m_valueTable[slot]) != slot;
    }
    
    /**
     * Internal remove pair from the table. Removes the pair from the table by setting the key entry to
     * <code>null</code> and adjusting the count present, then chains through the table to reinsert any other pairs
     * which may have collided with the removed pair. If the associated value is an object reference, it should be set
     * to <code>null</code> before this method is called.
     * 
     * @param slot index number of pair to be removed
     */
    private void internalRemove(int slot) {
        
        // delete pair from table
        m_keyTable[slot] = null;
        m_entryCount--;
        while (m_keyTable[(slot = stepSlot(slot))] != null) {
            
            // reinsert current entry in table to fill holes
            reinsert(slot);
            
        }
    }
    
    /**
     * Restructure the table. This is used when the table is increasing or decreasing in size, and works directly with
     * the old table representation arrays. It inserts pairs from the old arrays directly into the table without
     * adjusting the count present or checking the table size.
     * 
     * @param keys array of keys
     * @param values array of values
     */
    private void restructure(Object[] keys, int[] values) {
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null) {
                assignSlot(keys[i], values[i]);
            }
        }
    }
    
    /**
     * Assign slot for entry. Starts at the slot found by the hashed key value. If this slot is already occupied, it
     * steps the slot number and checks the resulting slot, repeating until an unused slot is found. This method does
     * not check for duplicate keys, so it should only be used for internal reordering of the tables.
     * 
     * @param key to be added to table
     * @param value associated value for key
     * @return slot at which entry was added
     */
    private int assignSlot(Object key, int value) {
        int offset = freeSlot(standardSlot(key));
        m_keyTable[offset] = key;
        m_valueTable[offset] = value;
        return offset;
    }
    
    /**
     * Increment a use count in the table. If the key object is already present in the table this adds one to the
     * reference count; if not present, this adds the key with an initial reference count of one.
     * 
     * @param key referenced object (non-<code>null</code>)
     * @return incremented use count
     */
    public int incrementCount(Object key) {
        
        // first validate the parameters
        if (key == null) {
            throw new IllegalArgumentException("null key not supported");
        } else {
            
            // check space available
            int min = m_entryCount + 1;
            if (min > m_entryLimit) {
                
                // find the array size required
                int size = m_arraySize;
                int limit = m_entryLimit;
                while (limit < min) {
                    size = size * 2 + 1;
                    limit = (int)(size * DEFAULT_FILL);
                }
                
                // set parameters for new array size
                m_arraySize = size;
                m_entryLimit = limit;
                m_hitOffset = size / 2;
                
                // restructure for larger arrays
                Object[] keys = m_keyTable;
                m_keyTable = new Object[m_arraySize];
                int[] values = m_valueTable;
                m_valueTable = new int[m_arraySize];
                restructure(keys, values);
            }
            
            // find slot of table
            int offset = standardFind(key);
            if (offset >= 0) {
                
                // replace existing value for key
                return ++m_valueTable[offset];
                
            } else {
                
                // add new pair to table
                m_entryCount++;
                offset = -offset - 1;
                m_keyTable[offset] = key;
                m_valueTable[offset] = 1;
                return 1;
                
            }
        }
    }
    
    /**
     * Find an entry in the table.
     * 
     * @param key key for entry to be returned
     * @return value for key, or zero if key not found
     */
    public final int getCount(Object key) {
        int slot = standardFind(key);
        if (slot >= 0) {
            return m_valueTable[slot];
        } else {
            return 0;
        }
    }
    
    /**
     * Get number of entries in map.
     * 
     * @return entry count
     */
    public int size() {
        return m_entryCount;
    }
    
    /**
     * Get iterator for keys in map. The returned iterator is not safe, so the iterator behavior is undefined if the map
     * is modified.
     * 
     * @return iterator
     */
    public Iterator iterator() {
        return SparseArrayIterator.buildIterator(m_keyTable);
    }
    
    /**
     * Get array of keys in map.
     * 
     * @return key array
     */
    public Object[] keyArray() {
        Object[] keys = new Object[m_entryCount];
        int fill = 0;
        for (int i = 0; i < m_arraySize; i++) {
            if (m_keyTable[i] != null) {
                keys[fill++] = m_keyTable[i];
            }
        }
        return keys;
    }
    
    /**
     * Construct a copy of the table.
     * 
     * @return shallow copy of table
     */
    public Object clone() {
        return new ReferenceCountMap(this);
    }
    
    /**
     * Clear all keys and counts.
     */
    public void clear() {
        for (int i = 0; i < m_keyTable.length; i++) {
            if (m_keyTable[i] != null) {
                m_keyTable[i] = null;
                m_valueTable[i] = 0;
            }
        }
    }
}
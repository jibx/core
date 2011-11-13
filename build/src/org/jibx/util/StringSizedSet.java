/*
Copyright (c) 2009, Dennis M. Sosnoski. All rights reserved.

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

/**
 * Fixed size hash set of <code>String</code> values.
 *
 * @author Dennis M. Sosnoski
 */
public class StringSizedSet
{
    /** Default fill fraction for sizing of tables. */
    public static final double DEFAULT_FILL_FRACTION = 0.4;
    
    /** Size of array used for keys. */
    protected final int m_arraySize;
    
	/** Array of key table slots. */
	protected final String[] m_keyTable;

    /** Offset added (modulo table size) to slot number on collision. */
    protected final int m_hitOffset;

	/**
	 * Constructor with full specification.
	 *
	 * @param count number of values to assume in sizing of table
	 * @param fill fraction fill for table (maximum of <code>0.7</code>, to
	 * prevent excessive collisions)
	 */
	public StringSizedSet(int count, double fill) {
        
        // make sure the fill fraction is in a reasonable range
        if (fill <= 0.0 || fill > 0.7) {
            throw new IllegalArgumentException("Fill fraction of " + fill +
                " is out of allowed range");
        }

        // compute initial table size (ensuring odd)
        int size = Math.max((int) (count / fill), 11);
        size += (size + 1) % 2;
        m_arraySize = size;

        // initialize the table information
        m_keyTable = new String[size];
        m_hitOffset = m_arraySize / 2;
	}

	/**
	 * Constructor with only value count specified. Uses default fill fraction
	 * and miss value.
	 *
	 * @param count number of values to assume in initial sizing of table
	 */
	public StringSizedSet(int count) {
		this(count, DEFAULT_FILL_FRACTION);
	}

    /**
     * Step the slot number for an entry. Adds the collision offset (modulo
     * the table size) to the slot number.
     *
     * @param slot slot number to be stepped
     * @return stepped slot number
     */
    private final int stepSlot(int slot) {
        return (slot + m_hitOffset) % m_arraySize;
    }

    /**
     * Find free slot number for entry. Starts at the slot based directly
     * on the hashed key value. If this slot is already occupied, it adds
     * the collision offset (modulo the table size) to the slot number and
     * checks that slot, repeating until an unused slot is found.
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
     * Standard base slot computation for a key. This method may be used
     * directly for key lookup using either the <code>hashCode()</code> method
     * defined for the key objects or the <code>System.identityHashCode()</code>
     * method, as selected by the hash technique constructor parameter. To
     * implement a hash class based on some other methods of hashing and/or
     * equality testing, define a separate method in the subclass with a
     * different name and use that method instead. This avoids the overhead
     * caused by overrides of a very heavily used method.
     *
     * @param key key value to be computed
     * @return base slot for key
     */
    private final int standardSlot(String key) {
        return (key.hashCode() & Integer.MAX_VALUE) % m_arraySize;
    }

    /**
     * Standard find key in table. This uses <code>equals</code> comparisons for
     * the key values.
     *
     * @param key to be found in table
     * @return index of matching key, or <code>-index-1</code> of slot to be
     * used for inserting key in table if not already present (always negative)
     */
    private int standardFind(String key) {

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
        return -slot-1;
    }

	/**
	 * Add a key to the set. If the key is already present in the set, this does
	 * nothing.
	 *
	 * @param key key to be added to table (non-<code>null</code>)
	 * @return <code>true</code> if added, <code>false</code> if already present
	 * value if key not previously present in table
	 */
	public boolean add(String key) {

		// first validate the parameters
		if (key == null) {
			throw new IllegalArgumentException("null key not supported");
		} else {

			// check space and duplicate key
			int offset = standardFind(key);
			if (offset >= 0) {
			    return false;
			} else {

				// add new key to table
				offset = -offset - 1;
				m_keyTable[offset] = key;
				return true;

			}
		}
	}

	/**
	 * Check if an entry is present in the table.
	 * 
	 * @param key key to be found
	 * @return <code>true</code> if key found in table, <code>false</code>
	 * if not
	 */
	public final boolean contains(String key) {
		return standardFind(key) >= 0;
	}

    /**
     * Set the table to the empty state.
     */
    public void clear() {
        for (int i = 0; i < m_keyTable.length; i++) {
            m_keyTable[i] = null;
        }
    }
}
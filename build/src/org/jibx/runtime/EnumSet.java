/*
Copyright (c) 2004, Dennis M. Sosnoski
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

package org.jibx.runtime;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Named value set support class. This provides convenience methods to support
 * working with a set of named <code>static final int</code> values, including
 * translating them to and from <code>String</code> representations. It's
 * intended for use with relatively small nonnegative int values.
 *
 * @author Dennis M. Sosnoski
 */
public class EnumSet
{
    /** Maximum <code>int</code> value supported for enumerations. */
    public static final int VALUE_LIMIT = 512;
    
    /** Actual item definitions (used for extensions). */
    private final EnumItem[] m_items;
    
    /** Enumeration names in index number order. */
    private final String[] m_indexedNames;
    
    /** Enumeration names in sort order. */
    private final String[] m_orderedNames;
    
    /** Index values corresponding to sorted names. */
    private final int[] m_orderedIndexes;

	/**
	 * Constructor from array of enumeration items. The supplied items can be in
     * any order, and the numeric values do not need to be contiguous (but must
     * be unique, nonnegative, and should be fairly small). Note that this
     * constructor will reorder the items in the supplied array as a side
     * effect.
	 *
	 * @param items array of enumeration items (will be reordered)
	 */
	public EnumSet(EnumItem[] items) {
        m_items = items;
        if (items.length > 0) {
            
            // first sort items in ascending name order
            Arrays.sort(items, new Comparator() {
                public int compare(Object a, Object b) {
                    return ((EnumItem)a).m_name.compareTo(((EnumItem)b).m_name);
                }
            });
            
            // populate arrays for name lookup
            m_orderedNames = new String[items.length];
            m_orderedIndexes = new int[items.length];
            int high = -1;
            for (int i = 0; i < items.length; i++) {
                EnumItem item = items[i];
                if (item.m_value < 0) {
                    throw new IllegalArgumentException("Negative item value " +
                        item.m_value + " not allowed");
                } else if (item.m_value > high) {
                    high = item.m_value;
                    if (high >= VALUE_LIMIT) {
                        throw new IllegalArgumentException
                            ("Enumeration with value " +
                            high + " too large to be used.");
                    }
                }
                m_orderedNames[i] = item.m_name;
                m_orderedIndexes[i] = item.m_value;
            }
            
            // populate array for indexed lookup of names
            m_indexedNames = new String[high+1];
            for (int i = 0; i < items.length; i++) {
                EnumItem item = items[i];
                if (m_indexedNames[item.m_value] == null) {
                    m_indexedNames[item.m_value] = item.m_name;
                } else {
                    throw new IllegalArgumentException
                        ("Duplicate index value " + item.m_value);
                }
            }
            
        } else {
            m_indexedNames = new String[0];
            m_orderedNames = new String[0];
            m_orderedIndexes = new int[0];
        }
	}

	/**
	 * Constructor from array of names. The value associated with each name is
     * just the position index in the array added to the start value.
	 *
     * @param start item value for first added name
	 * @param names array of names (no <code>null</code> entries allowed)
	 */
	public EnumSet(int start, String[] names) {
        this(buildItems(start, names));
	}

    /**
     * Constructor from existing enumeration with added names. The value
     * associated with each name is just the position index in the array added
     * to the start value.
     *
     * @param base base enumeration to be extended
     * @param start item value for first added name
     * @param names array of names (no <code>null</code> entries allowed)
     */
    public EnumSet(EnumSet base, int start, String[] names) {
        this(mergeItems(base, start, names));
    }

    /**
     * Generate array of enumeration items from array of names. The value
     * associated with each name is just the position index in the array added
     * to the start value.
     *
     * @param start item value for first added name
     * @param names array of names (no <code>null</code> entries allowed)
     */
    private static EnumItem[] buildItems(int start, String[] names) {
        EnumItem[] items = new EnumItem[names.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = new EnumItem(start+i, names[i]);
        }
        return items;
    }

    /**
     * Generate array of enumeration items from base enumeration and array of
     * names. The value associated with each name is just the position index in
     * the array added to the start value.
     *
     * @param base base enumeration to be extended
     * @param start item value for first added name
     * @param names array of names (no <code>null</code> entries allowed)
     */
    private static EnumItem[] mergeItems(EnumSet base, int start,
        String[] names) {
        int prior = base.m_items.length;
        EnumItem[] merges = new EnumItem[prior + names.length];
        System.arraycopy(base.m_items, 0, merges, 0, prior);
        for (int i = 0; i < names.length; i++) {
            merges[prior+i] = new EnumItem(start+i, names[i]);
        }
        return merges;
    }
    
    /**
     * Get name for value if defined.
     *
     * @param value enumeration value
     * @return name for value, or <code>null</code> if not defined
     */
    public String getName(int value) {
        if (value >= 0 && value < m_indexedNames.length) {
            return m_indexedNames[value];
        } else {
            return null;
        }
    }
    
    /**
     * Get name for value. If the supplied value is not defined in the
     * enumeration this throws an exception.
     *
     * @param value enumeration value
     * @return name for value
     */
    public String getNameChecked(int value) {
        if (value >= 0 && value < m_indexedNames.length) {
            String name = m_indexedNames[value];
            if (name != null) {
                return name;
            }
        }
        throw new IllegalArgumentException("Value " + value + " not defined");
    }
    
    /**
     * Get value for name if defined.
     *
     * @param name possible enumeration name
     * @return value for name, or <code>-1</code> if not found in enumeration
     */
    public int getValue(String name) {
        int base = 0;
        int limit = m_orderedNames.length - 1;
        while (base <= limit) {
            int cur = (base + limit) >> 1;
            int diff = name.compareTo(m_orderedNames[cur]);
            if (diff < 0) {
                limit = cur - 1;
            } else if (diff > 0) {
                base = cur + 1;
            } else if (m_orderedIndexes != null) {
                return m_orderedIndexes[cur];
            } else {
                return cur;
            }
        }
        return -1;
    }
    
    /**
     * Get value for name. If the supplied name is not present in the
     * enumeration this throws an exception.
     *
     * @param name enumeration name
     * @return value for name
     */
    public int getValueChecked(String name) {
        int index = getValue(name);
        if (index >= 0) {
            return index;
        } else {
            throw new IllegalArgumentException("Name " + name + " not defined");
        }
    }
    
    /**
     * Check value with exception. Throws an exception if the supplied value is
     * not defined by this enumeration.
     *
     * @param value
     */
    public void checkValue(int value) {
        if (value < 0 || value >= m_indexedNames.length ||
            m_indexedNames[value] == null) {
            throw new IllegalArgumentException("Value " + value +
                " not defined");
        }
    }
    
    /**
     * Get maximum index value in enumeration set.
     * 
     * @return maximum
     */
    public int maxIndex() {
        return m_indexedNames.length - 1;
    }
    
    /**
     * Enumeration pair information. This gives an <code>int</code> value along
     * with the associated <code>String</code> representation.
     */
    public static class EnumItem {
        public final int m_value;
        public final String m_name;
        
        public EnumItem(int value, String name) {
            m_value = value;
            m_name = name;
        }
    }
}
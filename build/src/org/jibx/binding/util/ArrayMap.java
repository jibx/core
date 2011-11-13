/*
Copyright (c) 2003-2005, Dennis M. Sosnoski
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

/**
 * Array with reverse mapping from values to indices. This operates as the
 * combination of an array with ordinary int indices and a hashmap from values
 * back to the corresponding index position. Values are assured to be unique.
 *
 * @author Dennis M. Sosnoski
 */
public class ArrayMap
{
    /** Array of values. */
    private ArrayList m_array;
    
    /** Map from values to indices. */
    private HashMap m_map;

    /**
     * Default constructor.
     */
    public ArrayMap() {
        m_array = new ArrayList();
        m_map = new HashMap();
    }

    /**
     * Constructor with initial capacity supplied.
     *
     * @param size initial capacity for array map
     */
    public ArrayMap(int size) {
        m_array = new ArrayList(size);
        m_map = new HashMap(size);
    }

    /**
     * Get value for index. The index must be within the valid range (from 0 to
     * one less than the number of values present).
     *
     * @param index number to be looked up
     * @return value at that index position
     */
    public Object get(int index) {
        return m_array.get(index);
    }

    /**
     * Find existing object. If the supplied value object is present in the
     * array map its index position is returned.
     *
     * @param obj value to be found
     * @return index number assigned to value, or <code>-1</code> if not found
     */
    public int find(Object obj) {
        Integer index = (Integer)m_map.get(obj);
        if (index == null) {
            return -1;
        } else {
            return index.intValue();
        }
    }

    /**
     * Add object. If the supplied value object is already present in the array
     * map the existing index position is returned.
     *
     * @param obj value to be added
     * @return index number assigned to value
     */
    public int findOrAdd(Object obj) {
        Integer index = (Integer)m_map.get(obj);
        if (index == null) {
            index = IntegerCache.getInteger(m_array.size());
            m_map.put(obj, index);
            m_array.add(obj);
        }
        return index.intValue();
    }

    /**
     * Get count of values present.
     *
     * @return number of values in array map
     */
    public int size() {
        return m_array.size();
    }
}
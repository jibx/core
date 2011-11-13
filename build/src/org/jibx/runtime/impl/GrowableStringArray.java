/*
Copyright (c) 2000-2009, Dennis M. Sosnoski.
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

package org.jibx.runtime.impl;

import java.lang.reflect.Array;

/**
 * Growable <code>String</code> array with type specific access methods. This
 * implementation is unsynchronized in order to provide the best possible
 * performance for typical usage scenarios, so explicit synchronization must
 * be implemented by a wrapper class or directly by the application in cases
 * where instances are modified in a multithreaded environment.
 *
 * @author Dennis M. Sosnoski
 */
public class GrowableStringArray
{
    /** Default initial array size. */
    public static final int DEFAULT_SIZE = 8;

    /** Size of the current array. */
    private int m_countLimit;
    
    /** The number of values currently present in the array. */
    private int m_countPresent;

    /** Maximum size increment for growing array. */
    private int m_maximumGrowth;

    /** The underlying array used for storing the data. */
    private String[] m_baseArray;

    /**
     * Constructor with full specification.
     *
     * @param size number of <code>String</code> values initially allowed in
     * array
     * @param growth maximum size increment for growing array
     */
    public GrowableStringArray(int size, int growth) {
        String[] array = new String[size];
        m_countLimit = size;
        m_maximumGrowth = growth;
        m_baseArray = array;
    }

    /**
     * Constructor with initial size specified.
     *
     * @param size number of <code>String</code> values initially allowed in
     * array
     */
    public GrowableStringArray(int size) {
        this(size, Integer.MAX_VALUE);
    }

    /**
     * Default constructor.
     */
    public GrowableStringArray() {
        this(DEFAULT_SIZE);
    }

    /**
     * Copy (clone) constructor.
     *
     * @param base instance being copied
     */
    public GrowableStringArray(GrowableStringArray base) {
        this(base.m_countLimit, base.m_maximumGrowth);
        System.arraycopy(base.m_baseArray, 0, m_baseArray, 0, 
            base.m_countPresent);
        m_countPresent = base.m_countPresent;
    }

    /**
     * Copy data after array resize. This just copies the entire contents of the
     * old array to the start of the new array. It should be overridden in cases
     * where data needs to be rearranged in the array after a resize.
     * 
     * @param base original array containing data
     * @param grown resized array for data
     */
    private void resizeCopy(Object base, Object grown) {
        System.arraycopy(base, 0, grown, 0, Array.getLength(base));
    }

    /**
     * Discards values for a range of indices in the array. Clears references to
     * removed values.
     * 
     * @param from index of first value to be discarded
     * @param to index past last value to be discarded
     */
    private void discardValues(int from, int to) {
        for (int i = from; i < to; i++) {
            m_baseArray[i] = null;
        }
    }

    /**
     * Increase the size of the array to at least a specified size. The array
     * will normally be at least doubled in size, but if a maximum size
     * increment was specified in the constructor and the value is less than
     * the current size of the array, the maximum increment will be used
     * instead. If the requested size requires more than the default growth, 
     * the requested size overrides the normal growth and determines the size
     * of the replacement array.
     * 
     * @param required new minimum size required
     */
    private void growArray(int required) {
        int size = Math.max(required,
            m_countLimit + Math.min(m_countLimit, m_maximumGrowth));
        String[] grown = new String[size];
        resizeCopy(m_baseArray, grown);
        m_countLimit = size;
        m_baseArray = grown;
    }

    /**
     * Ensure that the array has the capacity for at least the specified
     * number of values.
     * 
     * @param min minimum capacity to be guaranteed
     */
    public final void ensureCapacity(int min) {
        if (min > m_countLimit) {
            growArray(min);
        }
    }

    /**
     * Overwrite an existing value in the array.
     *
     * @param index position of value to be overwritten
     * @param value value to be added
     */
    public void set(int index, String value) {
        if (index < m_countPresent) {
            m_baseArray[index] = value;
        } else {
            throw new IllegalArgumentException("Index value out of range");
        }
    }

    /**
     * Add a value at the end of the array.
     *
     * @param value value to be added
     */
    public void add(String value) {
        int index = getAddIndex();
        m_baseArray[index] = value;
    }

    /**
     * Add an array of values at the end of the array.
     *
     * @param values values to be added
     */
    public void addAll(String[] values) {
        ensureCapacity(m_countPresent+values.length);
        for (int i = 0; i < values.length; i++) {
            m_baseArray[m_countPresent++] = values[i];
        }
    }

    /**
     * Remove some number of values from the end of the array.
     *
     * @param count number of values to be removed
     * @exception ArrayIndexOutOfBoundsException on attempt to remove more than
     * the count present
     */
    public void remove(int count) {
        int start = m_countPresent - count;
        if (start >= 0) {
            discardValues(start, m_countPresent);
            m_countPresent = start;
        } else {
            throw new ArrayIndexOutOfBoundsException
                ("Attempt to remove too many values from array");
        }
    }

    /**
     * Get a value from the array.
     *
     * @param index index of value to be returned
     * @return value from stack
     * @exception ArrayIndexOutOfBoundsException on attempt to access outside
     * valid range
     */
    public String get(int index) {
        if (m_countPresent > index) {
            return m_baseArray[index];
        } else {
            throw new ArrayIndexOutOfBoundsException
                ("Attempt to access past end of array");
        }
    }

    /**
     * Constructs and returns a simple array containing the same data as held
     * in this array.
     *
     * @return array containing a copy of the data
     */
    public String[] toArray() {
        String[] copy = new String[m_countPresent];
        System.arraycopy(m_baseArray, 0, copy, 0, m_countPresent);
        return copy;
    }

    /**
     * Duplicates the object with the generic call.
     *
     * @return a copy of the object
     */
    public Object clone() {
        return new GrowableStringArray(this);
    }

    /**
     * Gets the array offset for appending a value to those in the array. If the
     * underlying array is full, it is grown by the appropriate size increment
     * so that the index value returned is always valid for the array in use by
     * the time of the return.
     * 
     * @return index position for added element
     */
    private int getAddIndex() {
        int index = m_countPresent++;
        if (m_countPresent > m_countLimit) {
            growArray(m_countPresent);
        }
        return index;
    }

    /**
     * Get the number of values currently present in the array.
     * 
     * @return count of values present
     */
    public int size() {
        return m_countPresent;
    }

    /**
     * Check if array is empty.
     * 
     * @return <code>true</code> if array empty, <code>false</code> if not
     */
    public boolean isEmpty() {
        return m_countPresent == 0;
    }

    /**
     * Set the array to the empty state.
     */
    public void clear() {
        discardValues(0, m_countPresent);
        m_countPresent = 0;
    }
}
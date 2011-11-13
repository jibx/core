/*
Copyright (c) 2000-2006, Dennis M. Sosnoski
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

import java.lang.reflect.Array;

/**
 * Growable <code>String</code> stack with type specific access methods. This
 * implementation is unsynchronized in order to provide the best possible
 * performance for typical usage scenarios, so explicit synchronization must
 * be implemented by a wrapper class or directly by the application in cases
 * where instances are modified in a multithreaded environment.
 *
 * @author Dennis M. Sosnoski
 */
public class StringStack
{
    /** Default initial array size. */
    public static final int DEFAULT_SIZE = 8;

    /** Size of the current array. */
    private int m_countLimit;
    
    /** The number of values currently present in the stack. */
    private int m_countPresent;

    /** Maximum size increment for growing array. */
    private int m_maximumGrowth;

    /** The underlying array used for storing the data. */
    private String[] m_baseArray;

    /**
     * Constructor with full specification.
     *
     * @param size number of <code>String</code> values initially allowed in
     * stack
     * @param growth maximum size increment for growing stack
     */
    public StringStack(int size, int growth) {
        String[] array = new String[size];
        m_countLimit = size;
        m_maximumGrowth = growth;
        m_baseArray = array;
    }

    /**
     * Constructor with initial size specified.
     *
     * @param size number of <code>String</code> values initially allowed in
     * stack
     */
    public StringStack(int size) {
        this(size, Integer.MAX_VALUE);
    }

    /**
     * Default constructor.
     */
    public StringStack() {
        this(DEFAULT_SIZE);
    }

    /**
     * Copy (clone) constructor.
     *
     * @param base instance being copied
     */
    public StringStack(StringStack base) {
        this(base.m_countLimit, base.m_maximumGrowth);
        System.arraycopy(base.m_baseArray, 0, m_baseArray, 0, 
            base.m_countPresent);
        m_countPresent = base.m_countPresent;
    }

    /**
     * Constructor from array of strings.
     *
     * @param strings array of strings for initial contents
     */
    public StringStack(String[] strings) {
        this(strings.length);
        System.arraycopy(strings, 0, m_baseArray, 0, strings.length);
        m_countPresent = strings.length;
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
     * Discards values for a range of indices in the array. Checks if the
     * values stored in the array are object references, and if so clears 
     * them. If the values are primitives, this method does nothing.
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
     * Push a value on the stack.
     *
     * @param value value to be added
     */
    public void push(String value) {
        int index = getAddIndex();
        m_baseArray[index] = value;
    }

    /**
     * Pop a value from the stack.
     *
     * @return value from top of stack
     * @exception ArrayIndexOutOfBoundsException on attempt to pop empty stack
     */
    public String pop() {
        if (m_countPresent > 0) {
            String value = m_baseArray[--m_countPresent];
            m_baseArray[m_countPresent] = null;
            return value;
        } else {
            throw new ArrayIndexOutOfBoundsException
                ("Attempt to pop empty stack");
        }
    }

    /**
     * Pop multiple values from the stack. The last value popped is the
     * one returned.
     *
     * @param count number of values to pop from stack (must be strictly
     * positive)
     * @return value from top of stack
     * @exception ArrayIndexOutOfBoundsException on attempt to pop past end of
     * stack
     */
    public String pop(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        } else if (m_countPresent >= count) {
            m_countPresent -= count;
            String value = m_baseArray[m_countPresent];
            discardValues(m_countPresent, m_countPresent + count);
            return value;
        } else {
            throw new ArrayIndexOutOfBoundsException
                ("Attempt to pop past end of stack");
        }
    }

    /**
     * Copy a value from the stack. This returns a value from within
     * the stack without modifying the stack.
     *
     * @param depth depth of value to be returned
     * @return value from stack
     * @exception ArrayIndexOutOfBoundsException on attempt to peek past end of
     * stack
     */
    public String peek(int depth) {
        if (m_countPresent > depth) {
            return m_baseArray[m_countPresent - depth - 1];
        } else {
            throw new ArrayIndexOutOfBoundsException
                ("Attempt to peek past end of stack");
        }
    }

    /**
     * Copy top value from the stack. This returns the top value without
     * removing it from the stack.
     *
     * @return value at top of stack
     * @exception ArrayIndexOutOfBoundsException on attempt to peek empty stack
     */
    public String peek() {
        return peek(0);
    }

    /**
     * Constructs and returns a simple array containing the same data as held
     * in this stack. Note that the items will be in reverse pop order, with
     * the last item to be popped from the stack as the first item in the
     * array.
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
        return new StringStack(this);
    }

    /**
     * Gets the array offset for appending a value to those in the stack.
     * If the underlying array is full, it is grown by the appropriate size
     * increment so that the index value returned is always valid for the 
     * array in use by the time of the return.
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
     * Get the number of values currently present in the stack.
     * 
     * @return count of values present
     */
    public int size() {
        return m_countPresent;
    }

    /**
     * Check if stack is empty.
     * 
     * @return <code>true</code> if stack empty, <code>false</code> if not
     */
    public boolean isEmpty() {
        return m_countPresent == 0;
    }

    /**
     * Set the stack to the empty state.
     */
    public void clear() {
        discardValues(0, m_countPresent);
        m_countPresent = 0;
    }
}
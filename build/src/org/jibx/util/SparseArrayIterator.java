/*
 * Copyright (c) 2000-2007, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of JiBX nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator class for sparse values in an array. This type of iterator
 * can be used for an object array which has references interspersed with
 * <code>null</code>s.
 *
 * @author Dennis M. Sosnoski
 */
public class SparseArrayIterator implements Iterator
{
    /** Empty iterator. */
    public static final SparseArrayIterator EMPTY_ITERATOR =
        new SparseArrayIterator(new Object[0]);
    
    /** Array supplying values for iteration. */
    private Object[] m_array;

    /** Offset of next iteration value. */
    private int m_offset;

    /**
     * Internal constructor.
     *
     * @param array array containing values to be iterated
     */
    private SparseArrayIterator(Object[] array) {
        m_array = array;
        m_offset = -1;
        advance();
    }

    /**
     * Advance to next iteration value. This advances the current position in
     * the array to the next non-<code>null</code> value.
     *
     * @return <code>true</code> if element available, <code>false</code> if
     * not
     */
    protected boolean advance() {
        while (++m_offset < m_array.length) {
            if (m_array[m_offset] != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for iteration element available.
     *
     * @return <code>true</code> if element available, <code>false</code> if
     * not
     */
    public boolean hasNext() {
        return m_offset < m_array.length;
    }

    /**
     * Get next iteration element.
     *
     * @return next iteration element
     * @exception NoSuchElementException if past end of iteration
     */
    public Object next() {
        if (m_offset < m_array.length) {
            Object result = m_array[m_offset];
            advance();
            return result;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Remove element from iteration. This optional operation is not supported
     * and always throws an exception.
     *
     * @exception UnsupportedOperationException for unsupported operation
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Build iterator.
     *
     * @param array array containing values to be iterated (may be
     * <code>null</code>)
     * @return constructed iterator
     */
    public static Iterator buildIterator(Object[] array) {
        if (array == null || array.length == 0) {
            return EMPTY_ITERATOR;
        } else {
            return new SparseArrayIterator(array);
        }
    }
}
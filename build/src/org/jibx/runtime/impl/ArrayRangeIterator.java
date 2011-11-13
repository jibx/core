/*
Copyright (c) 2000-2005, Dennis M. Sosnoski.
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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator class for values contained in an array range. This type of iterator
 * can be used for any contiguous range of items in an object array.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */
public class ArrayRangeIterator implements Iterator
{
	/** Empty iterator used whenever possible. */
	public static final ArrayRangeIterator EMPTY_ITERATOR =
		new ArrayRangeIterator(null, 0, 0);

	/** Array supplying values for iteration. */
	protected Object[] m_array;

	/** Offset of next iteration value. */
	protected int m_offset;

	/** Ending offset for values. */
	protected int m_limit;

	/**
	 * Internal constructor.
	 *
	 * @param array array containing values to be iterated
	 * @param start starting offset in array
	 * @param limit offset past end of values
	 */
	private ArrayRangeIterator(Object[] array, int start, int limit) {
		m_array = array;
		m_offset = start;
		m_limit = limit;
	}

	/**
	 * Check for iteration element available.
	 *
	 * @return <code>true</code> if element available, <code>false</code> if
	 * not
	 */
	public boolean hasNext() {
		return m_offset < m_limit;
	}

	/**
	 * Get next iteration element.
	 *
	 * @return next iteration element
	 * @exception NoSuchElementException if past end of iteration
	 */
	public Object next() {
		if (m_offset < m_limit) {
			return m_array[m_offset++];
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
	 * @param start starting offset in array
	 * @param limit offset past end of values
	 * @return constructed iterator
	 */
	public static Iterator buildIterator(Object[] array, int start, int limit) {
		if (array == null || start >= limit) {
			return EMPTY_ITERATOR;
		} else {
			return new ArrayRangeIterator(array, start, limit);
		}
	}
}
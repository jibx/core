/*
 * Copyright (c) 2000-2001 Sosnoski Software Solutions, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package org.jibx.runtime.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator class for sparse values in an array. This type of iterator
 * can be used for an object array which has references interspersed with
 * <code>null</code>s.
 *
 * @author Dennis M. Sosnoski
 * @version 1.1
 */
public class SparseArrayIterator implements Iterator
{
	/** Array supplying values for iteration. */
	protected Object[] m_array;

	/** Offset of next iteration value. */
	protected int m_offset;

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
			return ArrayRangeIterator.EMPTY_ITERATOR;
		} else {
			return new SparseArrayIterator(array);
		}
	}
}
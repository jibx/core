/*
Copyright (c) 2004-2008, Dennis M. Sosnoski
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

package org.jibx.binding.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Unmodifiable empty array list. This defines a singleton instance of itself,
 * which can then be used whereever an empty list is convenient. This class is
 * required to support methods which return instances of java.util.ArrayList in
 * order to guarantee random access to the returned list in constant time as
 * part of the method contract. java.util.Collection.EMPTY_LIST is not an
 * instance of java.util.ArrayList, so it cannot be used.
 *
 * @author Dennis M. Sosnoski
 */
public class EmptyArrayList extends ArrayList
{
    public static final EmptyArrayList INSTANCE = new EmptyArrayList();
    
    private EmptyArrayList() {}
    
	public void add(int index, Object element) {
		throw new UnsupportedOperationException("List is not modifiable");
	}

	public boolean add(Object o) {
        throw new UnsupportedOperationException("List is not modifiable");
	}

	public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("List is not modifiable");
	}

	public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException("List is not modifiable");
	}

	public void ensureCapacity(int minCapacity) {
        throw new UnsupportedOperationException("List is not modifiable");
	}

    public void clear() {
        throw new UnsupportedOperationException("List is not modifiable");
    }

    public Object remove(int index) {
        throw new UnsupportedOperationException("List is not modifiable");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("List is not modifiable");
    }

    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("List is not modifiable");
    }

    public Object set(int index, Object element) {
        throw new UnsupportedOperationException("List is not modifiable");
    }

    public void trimToSize() {
        throw new UnsupportedOperationException("List is not modifiable");
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("List is not modifiable");
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("List is not modifiable");
    }
}
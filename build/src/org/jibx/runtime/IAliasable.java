/*
Copyright (c) 2002-2009, Dennis M. Sosnoski.
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

/**
 * Nameable extension interface definition. This interface must be implemented
 * by a marshaller {@link org.jibx.runtime.IMarshaller} or unmarshaller {@link
 * org.jibx.runtime.IUnmarshaller} that can use different top-level element
 * names. Although it does not define any methods, it designates the marshaller
 * or unmarshaller as being usable with a namespace and element name, and
 * particular bound class name, defined within a binding.
 *
 * If this interface is implemented by a marshaller or unmarshaller class used
 * with a specified element name in a binding the binding compiler will actually
 * generate a subclass of the original class. The subclass uses a standard
 * no-argument constructor, but calls a superclass constructor with the
 * specified element name information. The superclass code can
 * then make use of the specified name in marshalling and/or unmarshalling.
 * 
 * For a marshaller-only class, the required constructor takes a pair of
 * arguments, with the first argument the namespace URI index and the second the
 * actual element name string. For an unmarshaller-only class, the constructor
 * must take a pair of arguments, with the first argument the namespace URI and
 * the second the element name. For a class which is both a marshaller and an
 * unmarshaller, the constructor must take three arguments, with the first
 * argument the namespace URI string, the second the namespace URI index, and
 * the third the actual element name.
 *
 * @author Dennis M. Sosnoski
 */
public interface IAliasable {}
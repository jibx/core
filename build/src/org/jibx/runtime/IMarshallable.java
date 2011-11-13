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
 * Marshallable interface definition. This interface must be implemented by all
 * classes which can be marshalled as independent units (not just as children of
 * other objects). Classes implementing this interface may either marshal
 * themselves directly (if there's only one marshalling format defined), or
 * obtain an instance of the appropriate marshaller from the context and use
 * that. This interface is automatically added by the binding compiler to all
 * classes targeted by &lt;mapping> elements in a binding.
 *
 * @author Dennis M. Sosnoski
 */
public interface IMarshallable
{
    /**
     * Get the name of the class or type associated with the &lt;mapping>
     * definition.
     *
     * @return fully-qualified class name, or type name
     */
    String JiBX_getName();
    
    /**
     * Marshal self. This method call is responsible for all handling of the
     * marshalling of an object to XML text.
     *
     * @param ctx marshalling context
     * @throws JiBXException on error in marshalling process
     */
    void marshal(IMarshallingContext ctx) throws JiBXException;
}
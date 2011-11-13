/*
Copyright (c) 2002-2008, Dennis M. Sosnoski.
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
 * Marshaller interface definition. This interface must be implemented
 * by the handler for marshalling an object.<p>
 *
 * Instances of classes implementing this interface must be serially
 * reusable, meaning they can store state information while in the process
 * of marshalling an object but must reset all state when called to
 * marshal another object after the first one is done (even if the first
 * object throws an exception during marshalling).
 *
 * The JiBX framework will only create one instance of a marshaller class (per
 * marshalling context) for each mapped class using that marshaller. Generally
 * the marshaller instance will not be called recursively, but this may happen
 * in cases where the binding definition includes recursive mappings and the
 * marshaller uses other marshallers (as opposed to handling all children
 * directly).
 *
 * @author Dennis M. Sosnoski
 */
public interface IMarshaller
{
    /**
     * Check if marshaller represents an extension mapping. This is used by the
     * framework in generated code to verify compatibility of objects being
     * marshalled using an abstract mapping.
     *
     * @param mapname marshaller mapping name (generally the class name to be
     * handled, or abstract mapping type name)
     * @return <code>true</code> if this mapping is an extension of the abstract
     * mapping, <code>false</code> if not
     */
    boolean isExtension(String mapname);
    
    /**
     * Marshal instance of handled class. This method call is responsible
     * for all handling of the marshalling of an object to XML text. It is
     * called at the point where the start tag for the associated element
     * should be generated.
     *
     * @param obj object to be marshalled (may be <code>null</code> if property
     * is not optional)
     * @param ctx XML text output context
     * @throws JiBXException on error in marshalling process
     */
    void marshal(Object obj, IMarshallingContext ctx) throws JiBXException;
}
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
 * Unmarshaller interface definition. This interface must be implemented
 * by the handler for unmarshalling an object.
 *
 * Instances of classes implementing this interface must be serially
 * reusable, meaning they can store state information while in the process
 * of unmarshalling an object but must reset all state when called to
 * unmarshal another object after the first one is done (even if the first
 * object throws an exception during unmarshalling).
 *
 * The JiBX framework will only create one instance of an unmarshaller class
 * (per unmarshalling context) for each mapped class using that unmarshaller.
 * Generally the unmarshaller instance will not be called recursively, but this
 * may happen in cases where the binding definition includes recursive mappings
 * and the unmarshaller uses other unmarshallers (as opposed to handling all
 * children directly).
 *
 * @author Dennis M. Sosnoski
 */
public interface IUnmarshaller
{
    /**
     * Check if instance present in XML. This method can be called when the
     * unmarshalling context is positioned at or just before the start of the
     * data corresponding to an instance of this mapping. It verifies that the
     * expected data is present.
     *
     * @param ctx unmarshalling context
     * @return <code>true</code> if expected parse data found,
     * <code>false</code> if not
     * @throws JiBXException on error in unmarshalling process
     */
    boolean isPresent(IUnmarshallingContext ctx) throws JiBXException;
    
    /**
     * Unmarshal instance of handled class. This method call is responsible
     * for all handling of the unmarshalling of an object from XML text,
     * including creating the instance of the handled class if an instance is
     * not supplied. When it is called the unmarshalling context is always
     * positioned at or just before the start tag corresponding to the start of
     * the class data.
     *
     * @param obj object to be unmarshalled (may be <code>null</code>)
     * @param ctx unmarshalling context
     * @return unmarshalled object (may be <code>null</code>)
     * @throws JiBXException on error in unmarshalling process
     */
    Object unmarshal(Object obj, IUnmarshallingContext ctx)
        throws JiBXException;
}
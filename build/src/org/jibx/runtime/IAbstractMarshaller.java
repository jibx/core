/*
Copyright (c) 2002,2003, Dennis M. Sosnoski.
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
 * Abstract base marshaller interface definition. This interface must be
 * implemented by the handler for marshalling an object as an instance of a
 * binding with extension mappings.<p>
 *
 * This extension to the normal marshaller interface allows the base
 * marshalling to determine the proper marshaller implementation to use at
 * runtime. The code needs to check that the object to be marshalled has a
 * marshaller that extends this base mapping.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public interface IAbstractMarshaller extends IMarshaller
{
    /**
     * Marshal instance of class with mapping extending this abstract mapping.
     * This method call is responsible for all handling of the marshalling of an
     * appropriate object to XML text. It is called at the point where the start
     * tag for the associated element should be generated.
     *
     * @param obj object to be marshalled (may be <code>null</code>, in the case
     * of a non-optional property with no value supplied)
     * @param ctx XML text output context
     * @throws JiBXException on error in marshalling process
     */
     
    public void baseMarshal(Object obj, IMarshallingContext ctx)
        throws JiBXException;
}

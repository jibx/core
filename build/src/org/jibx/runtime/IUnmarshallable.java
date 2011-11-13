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
 * Unmarshallable interface definition. This interface must be implemented by
 * all classes which can be unmarshalled as independent units (not just as
 * children of other objects). Classes implementing this interface may either
 * unmarshal themselves directly (if there's only one unmarshalling format
 * defined), or obtain an instance of the appropriate unmarshaller from the
 * context and use that.
 *
 * @author Dennis M. Sosnoski
 */
public interface IUnmarshallable
{
    /**
     * Get the name of the class associated with the &lt;mapping> definition.
     *
     * @return fully-qualified class name
     */
    String JiBX_className();
    
    /**
     * Unmarshal self. This method call is responsible for all handling of the
     * unmarshalling of the object from XML text.
     *
     * @param ctx unmarshalling context
     * @throws JiBXException on error in unmarshalling process
     */
    void unmarshal(IUnmarshallingContext ctx) throws JiBXException;
}

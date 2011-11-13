/*
Copyright (c) 2008, Dennis M. Sosnoski.
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

import java.io.IOException;

/**
 * Input buffer interface. This extends the basic byte array buffer interface
 * with methods specifically for input. Client code needs to obey the access
 * rules stated in the method documentation, including the documentation for the
 * base interface methods.
 *
 * @author Dennis M. Sosnoski
 */
public interface IInByteBuffer extends IByteBuffer
{
    /**
     * Require some number of bytes of data. When this call is made the buffer
     * can discard all data up to the current offset, and may move retained data
     * within the buffer array and read more data from the data source to make
     * the requested number of bytes available. This call may cause the byte
     * array buffer to be replaced, so {@link #getBuffer()},
     * {@link #getLimit()}, and {@link #getOffset()} must all be called again
     * before any further use of the buffer.
     * 
     * @param size desired number of bytes
     * @return <code>true</code> if request satisfied, <code>false</code> if end
     * with less than request available
     * @throws IOException 
     */
    boolean require(int size) throws IOException;
    
    /**
     * Get offset past the end of data in buffer.
     *
     * @return offset past end of data
     */
    int getLimit();
}
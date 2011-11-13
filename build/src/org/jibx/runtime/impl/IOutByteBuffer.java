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
 * Output buffer interface. This extends the basic byte array buffer interface
 * with methods specifically for output. Client code needs to obey the access
 * rules stated in the method documentation, including the documentation for the
 * base interface methods.
 *
 * @author Dennis M. Sosnoski
 */
public interface IOutByteBuffer extends IByteBuffer
{
    /**
     * Free at least some number of bytes of space in the byte array. If no
     * reserve is specified, this call may write all data up to the current
     * offset to the output stream, and if necessary will replace the byte array
     * with a larger array. If a reserve is specified, only data up to the
     * reserve will be written, and any remaining data will be moved down to the
     * start of the (possibly new) byte array on return. Both {@link 
     * #getBuffer()} and {@link IByteBuffer#getOffset()} must always be called
     * again before any further use of the buffer.
     * 
     * @param reserve offset of data to be preserved in buffer (nothing
     * preserved if greater than or equal to current offset)
     * @param size desired number of bytes
     * @throws IOException 
     */
    void free(int reserve, int size) throws IOException;
    
    /**
     * Empty the buffer. Writes all data from the buffer to the output stream,
     * resetting the offset to the logical start of the buffer (which may not be
     * offset zero, since implementations may add some sort of header). This
     * method is guaranteed not to replace the byte array, but
     * {@link IByteBuffer#getOffset()} must always be called again before any
     * further use of the buffer.
     * 
     * @throws IOException 
     */
    void flush() throws IOException;
}
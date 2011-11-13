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
 * Input or output buffer interface. It exposes a byte array buffer directly for
 * use by client code, to allow efficient buffering of data without copying.
 * This obviously requires client code to be careful to obey the access rules
 * stated in the method documentation.
 *
 * @author Dennis M. Sosnoski
 */
public interface IByteBuffer
{
    /**
     * Get the byte array buffer.
     *
     * @return array
     */
    byte[] getBuffer();
    
    /**
     * Get the current offset. For an input buffer this is the index of the next
     * byte to be read, for an output buffer it is the index to store the next
     * byte to be written. After reading or writing data, the {@link
     * #setOffset(int)} method must be used to update the current offset before
     * any other operations are performed on the buffer.
     *
     * @return offset
     */
    int getOffset();
    
    /**
     * Set the current offset. This method must be used to update the internal
     * buffer state after reading or writing any data.
     * 
     * @param offset 
     */
    void setOffset(int offset);
    
    /**
     * Complete usage of the buffer. This method should be called whenever the
     * application is done reading or writing the buffer. After this method is
     * called the buffer may need to be reinitialized (such as by setting a new
     * input or output stream) before it is reused.
     * 
     * @throws IOException 
     */
    void finish() throws IOException;
}
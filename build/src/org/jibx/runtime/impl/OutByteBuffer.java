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
import java.io.OutputStream;

/**
 * Byte buffer wrapping an output stream. Clients need to obey the interface
 * access rules.
 *
 * @author Dennis M. Sosnoski
 */
public class OutByteBuffer implements IOutByteBuffer
{
    /** Default output buffer size. */
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
    /** Stream for byte output. */
    private OutputStream m_stream;
    
    /** Buffer for output bytes. */
    private byte[] m_buffer;
    
    /** Current offset for adding bytes to buffer. */
    private int m_offset;
    
    /**
     * Constructor with size specified.
     * 
     * @param size initial buffer size in bytes
     */
    public OutByteBuffer(int size) {
        m_buffer = new byte[size];
    }
    
    /**
     * Constructor using default buffer size.
     */
    public OutByteBuffer() {
        this(DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * Reset to initial state for reuse.
     */
    public void reset() {
        m_offset = 0;
        m_stream = null;
    }
    
    /**
     * Set output stream. If an output stream is currently open when this is
     * called the existing stream is flushed and closed, with any errors
     * ignored.
     *
     * @param os stream
     */
    public void setOutput(OutputStream os) {
        try {
            finish();
        } catch (IOException e) { /* deliberately empty */ }
        reset();
        m_stream = os;
    }
    
    //
    // IOutByteBuffer implementation
    
    /**
     * Get the byte array buffer.
     *
     * @return array
     */
    public byte[] getBuffer() {
        return m_buffer;
    }
    
    /**
     * Get the index of the next byte to be read. After reading data, the {@link
     * #setOffset(int)} method must be used to update the current offset before
     * any other operations are performed on the buffer.
     *
     * @return offset
     */
    public int getOffset() {
        return m_offset;
    }
    
    /**
     * Set the current offset. This must be used to update the stored buffer
     * state after reading any data.
     * 
     * @param offset 
     */
    public void setOffset(int offset) {
        m_offset = offset;
    }
    
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
    public void free(int reserve, int size) throws IOException {
        if (m_buffer.length - m_offset < size) {
            
            // check if reserve sets limit on write
            if (reserve < m_offset) {
                
                // write what we can, then check for array resize needed
                m_stream.write(m_buffer, 0, reserve);
                m_offset = m_offset - reserve;
                int need = reserve + size;
                if (need > m_buffer.length) {
                    
                    // resize array, copying data between reserve and offset
                    byte[] newbuf = new byte[Math.max(need, m_buffer.length+2)];
                    System.arraycopy(m_buffer, reserve, newbuf, 0, m_offset);
                    m_buffer = newbuf;
                    
                } else {
                    
                    // just copy data in array down to start
                    System.arraycopy(m_buffer, reserve, m_buffer, 0, m_offset);
                    
                }
                
            } else {
                
                // just write all present and replace array if needed
                m_stream.write(m_buffer, 0, m_offset);
                if (size > m_buffer.length) {
                    m_buffer = new byte[Math.max(size, m_buffer.length*2)];
                }
                m_offset = 0;
                
            }
        }
    }
    
    /**
     * Empty the buffer. Writes all data from the buffer to the output stream,
     * resetting the offset to the start of the buffer.
     * 
     * @throws IOException 
     */
    public void flush() throws IOException {
        if (m_offset > 0) {
            m_stream.write(m_buffer, 0, m_offset);
            m_offset = 0;
        }
    }
    
    /**
     * Complete usage of the current stream. This method should be called
     * whenever the application is done writing to the buffered stream. Once
     * this method is called, a call to {@link #setOutput(OutputStream)} is
     * required before the buffer can again be used.
     * 
     * @throws IOException 
     */
    public void finish() throws IOException {
        if (m_stream != null) {
            flush();
            m_stream.close();
            m_stream = null;
        }
    }
}
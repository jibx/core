/*
Copyright (c) 2008-2009, Dennis M. Sosnoski.
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
import java.io.InputStream;

/**
 * Byte buffer wrapping an input stream. Clients need to obey the interface
 * access rules.
 *
 * @author Dennis M. Sosnoski
 */
public class InByteBuffer implements IInByteBuffer
{
    /** Default input buffer size. */
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    
    /** Stream for byte input. */
    private InputStream m_stream;
    
    /** Flag for end of stream reached. */
    private boolean m_isEnd;
    
    /** Buffer for input bytes. */
    private byte[] m_buffer;
    
    /** Offset past end of bytes in buffer. */
    private int m_limit;
    
    /** Current offset for removing bytes from buffer. */
    private int m_offset;
    
    /**
     * Constructor with size specified.
     * 
     * @param size initial buffer size in bytes
     */
    public InByteBuffer(int size) {
        m_buffer = new byte[size];
    }
    
    /**
     * Constructor using default buffer size.
     */
    public InByteBuffer() {
        this(DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * Read data into the buffer to assure at least a minimum number of bytes
     * are available. Any retained data is first copied down to the start of the
     * buffer array. Next, data is read from the wrapped stream into the
     * available space in the buffer. The actual number of characters read by a
     * call to this method is normally between one and the space available in
     * the buffer array.
     * 
     * @param size minimum number of bytes required
     * @return <code>true</code> if data has been read into buffer,
     * <code>false</code> if not
     * @throws IOException on error reading from wrapped stream
     */
    private boolean fillBuffer(int size) throws IOException {
        if (m_isEnd) {
            return false;
        } else {
            
            // first check for new buffer needed
            byte[] oldbuf = m_buffer;
            if (m_buffer.length < size) {
                byte[] newbuf = new byte[Math.max(size, m_buffer.length*2)];
                m_buffer = newbuf;
            }
            
            // copy any remaining data to start of buffer
            int rem = m_limit - m_offset;
            if (rem > 0 && (m_offset > 0 || m_buffer != oldbuf)) {
                System.arraycopy(oldbuf, m_offset, m_buffer, 0, rem);
            }
            m_offset = 0;
            
            // read to at least the required number of bytes
            m_limit = rem;
            while (m_limit < size) {
                int max = m_buffer.length - m_limit;
                int actual = m_stream.read(m_buffer, m_limit, max);
                if (actual >= 0) {
                    m_limit += actual;
                } else {
                    m_isEnd = true;
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Reset to initial state for reuse.
     */
    public void reset() {
        m_isEnd = false;
        m_limit = 0;
        m_offset = 0;
        m_stream = null;
    }
    
    /**
     * Set the actual input stream to be used for this buffer. If an input
     * stream is currently open when this is called the existing stream is
     * closed and any buffered data discarded, with any errors ignored.
     *
     * @param is stream
     */
    public void setInput(InputStream is) {
        try {
            finish();
        } catch (IOException e) { /* deliberately empty */ }
        reset();
        m_stream = is;
    }
    
    //
    // IInByteBuffer implementation
    
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
     * Get offset past the end of data in buffer.
     *
     * @return offset past end of data
     */
    public int getLimit() {
        return m_limit;
    }
    
    /**
     * Require some number of bytes of data. When this call is made the buffer
     * can discard all data up to the current offset, and will copy retained
     * data down to the start of the buffer array and read more data from the
     * input stream if necessary to make the requested number of bytes
     * available. This call may cause the byte array buffer to be replaced, so
     * {@link #getBuffer()}, {@link #getLimit()}, and {@link #getOffset()} must
     * all be called again before any further use of the buffer.
     * 
     * @param size desired number of bytes
     * @return <code>true</code> if request satisfied, <code>false</code> if not
     * @throws IOException 
     */
    public boolean require(int size) throws IOException {
        if (m_limit - m_offset < size) {
            return fillBuffer(size);
        } else {
            return true;
        }
    }
    
    /**
     * Complete usage of the current stream. This method should be called
     * whenever the application is done reading from the buffered stream. Once
     * this method is called, a call to {@link #setInput(InputStream)} is
     * required before the buffer can again be used.
     * 
     * @throws IOException 
     */
    public void finish() throws IOException {
        if (m_stream != null) {
            m_stream.close();
            m_stream = null;
        }
    }
}
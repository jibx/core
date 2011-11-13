/*
Copyright (c) 2004-2008, Dennis M. Sosnoski.
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
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Wrapper for input buffer that supports multiple character encodings. This is
 * needed because the XPP3 pull parser does not support detecting the character
 * encoding for a document based on the content of the document. If used with a
 * common encoding this performs the conversion to characters using an inner
 * reader class; otherwise, this creates the appropriate reader type
 *
 * @author Dennis M. Sosnoski
 */
public class InputStreamWrapper
{
    /** Input buffer. */
    private IInByteBuffer m_byteBuffer;
    
    /** Name of encoding to be used for stream. */
    private String m_encodingName;
    
    /** Cached reference to byte array used by buffer. */
    private byte[] m_buffer;
    
    /** Offset past end of bytes in buffer. */
    private int m_endOffset;
    
    /** Current offset for generating character from buffer. */
    private int m_emptyOffset;
    
    /** Scan position offset used for lookahead in buffer. */
    private int m_scanOffset;
    
    /**
     * Set the input buffer.
     *
     * @param buff
     */
    public void setBuffer(IInByteBuffer buff) {
        m_byteBuffer = buff;
        m_buffer = buff.getBuffer();
        m_endOffset = buff.getLimit();
        m_emptyOffset = buff.getOffset();
    }
    
    /**
     * Get input buffer.
     *
     * @return buffer, <code>null</code> if none set
     */
    public IInByteBuffer getBuffer() {
        return m_byteBuffer;
    }
    
    /**
     * Set encoding for stream. This call is only valid if the encoding has not
     * been set previously, and if the encoding is a recognized type.
     *
     * @param enc character encoding used for input from stream
     * (<code>null</code> if to be determined from XML input)
     * @throws IOException if unknown encoding, or encoding already set
     */
    public void setEncoding(String enc) throws IOException {
        if (m_encodingName == null) {
            m_encodingName = enc;
        } else {
            throw new IOException("Encoding has already been set for stream");
        }
    }
    
    /**
     * Reads data into the buffer. The actual number of bytes read by a call to
     * this method is normally between one and the space available in the buffer
     * array.
     * 
     * @return <code>true</code> if data has been read into buffer,
     * <code>false</code> if not
     * @throws IOException on error reading from stream
     */
    private boolean fillBuffer() throws IOException {
        m_byteBuffer.setOffset(m_emptyOffset);
        int remain = m_endOffset - m_emptyOffset;
        boolean result = m_byteBuffer.require(remain+1);
        m_buffer = m_byteBuffer.getBuffer();
        m_endOffset = m_byteBuffer.getLimit();
        m_emptyOffset = m_byteBuffer.getOffset();
        return result;
    }
    
    /**
     * Reads data into the buffer to at least a minimum number of bytes. Any
     * retained data is first copied down to the start of the buffer array.
     * Next, data is read from the wrapped stream into the available space in
     * the buffer until the end of the input stream is reached or at least the
     * requested number of bytes are present in the buffer.
     * 
     * @param min number of bytes required
     * @return <code>true</code> if buffer contains at least the required byte
     * count on return, <code>false</code> if not
     * @throws IOException on error reading from wrapped stream
     */
    private boolean require(int min) throws IOException {
        boolean result = true;
        if (m_endOffset - m_emptyOffset < min) {
            m_byteBuffer.setOffset(m_emptyOffset);
            result = m_byteBuffer.require(min);
            m_buffer = m_byteBuffer.getBuffer();
            m_endOffset = m_byteBuffer.getLimit();
            m_emptyOffset = m_byteBuffer.getOffset();
        }
        return result;
    }
    
    /**
     * Check if a character is XML whitespace.
     * 
     * @param chr
     * @return <code>true</code> if whitespace, <code>false</code> if not
     */
    private boolean isWhite(int chr) {
        return chr == ' ' || chr == 0x09 || chr == 0x0A || chr == 0x0D;
    }
    
    /**
     * Reads a space or equals ('=') delimited token from the scan position in
     * the buffer. This treats bytes in the buffer as equivalent to characters.
     * Besides ending a token on a delimitor, it also ends a token after adding
     * a greater-than ('>') character.
     * 
     * @return token read from buffer
     * @throws IOException on error reading from wrapped stream
     */
    private String scanToken() throws IOException {
        boolean skipping = true;
        StringBuffer buff = new StringBuffer();
        while (require(m_scanOffset+1)) {
            char chr = (char)m_buffer[m_scanOffset++];
            if (skipping) {
                if (!isWhite(chr)) {
                    skipping = false;
                    buff.append(chr);
                    if (chr == '=') {
                        return buff.toString();
                    }
                }
            } else if (isWhite(chr) || chr == '=') {
                m_scanOffset--;
                return buff.toString();
            } else {
                buff.append(chr);
                if (chr == '>') {
                    return buff.toString();
                }
            }
        }
        return null;
    }
    
    /**
     * Reads a quote delimited token from the scan position in the buffer. This
     * treats bytes in the buffer as equivalent to characters, and skips past
     * any leading whitespace.
     * 
     * @return token read from buffer
     * @throws IOException on error reading from wrapped stream
     */
    private String scanQuoted() throws IOException {
        boolean skipping = true;
        int quot = 0;
        StringBuffer buff = new StringBuffer();
        while (require(m_scanOffset+1)) {
            char chr = (char)m_buffer[m_scanOffset++];
            if (skipping) {
                if (!isWhite(chr)) {
                    if (chr == '"' || chr == '\'') {
                        skipping = false;
                        quot = chr;
                    } else {
                        break;
                    }
                }
            } else if (chr == quot) {
                return buff.toString();
            } else {
                buff.append(chr);
            }
        }
        return null;
    }
    
    /**
     * Get reader for wrapped input stream. This creates and returns a reader
     * using the appropriate encoding, if necessary reading and examining the
     * first part of the stream (including the XML declaration, if present) to
     * determine the encoding.
     *
     * @return reader
     * @throws IOException if error reading from document or creating a reader
     * for the encoding found
     */
    public Reader getReader() throws IOException {
        
        // check if we need to determine an encoding
        if (m_encodingName == null) {
            
            // try to get enough input to decide if anything other than default
            m_encodingName = "UTF-8";
            if (require(4)) {
                
                // get first four bytes for initial determination
                int bom = (((m_buffer[0] << 8) + (m_buffer[1] & 0xFF) << 8) +
                    (m_buffer[2] & 0xFF) << 8) + (m_buffer[3] & 0xFF);
                if (bom == 0x3C3F786D) {
                    
                    // read encoding declaration with single byte characters
                    m_scanOffset = 2;
                    String token = scanToken();
                    if ("xml".equals(token)) {
                        while ((token = scanToken()) != null &&
                            !"?>".equals(token)) {
                            if ("encoding".equals(token)) {
                                if ("=".equals(scanToken())) {
                                    token = scanQuoted();
                                    if (token != null) {
                                        m_encodingName = token;
                                        break;
                                    }
                                }
                            } else if ("=".equals(token)) {
                                scanQuoted();
                            }
                        }
                    }
                    
                } else if (bom == 0x0000FEFF || bom == 0xFFFE0000 ||
                    bom == 0x0000FFFE || bom == 0xFEFF0000) {
                    
                    // just use generic UCS-4 and let libraries figure it out
                    m_encodingName = "UCS-4";
                    
                } else if ((bom & 0xFFFFFF00) == 0xEFBBBF00) {
                    
                    // UTF-8 as specified by byte order mark
                    m_encodingName = "UTF-8";
                    
                } else {
                    int upper = bom & 0xFFFF0000;
                    if (upper == 0xFEFF0000 || bom == 0x003C003F) {
                        
                        // assume UTF-16BE for 16-bit BE
                        m_encodingName = "UTF-16BE";
                        
                    } else if (upper == 0xFFFE0000 || bom == 0x3C003F00) {
                        
                        // assume UTF-16LE for 16-bit LE
                        m_encodingName = "UTF-16LE";
                        
                    } else if (bom == 0x4C6FA794){
                        
                        // just because we can, even though nobody should
                        m_encodingName = "EBCDIC";
                    }
                }
            }
        }
        if (m_encodingName.equalsIgnoreCase("UTF-8")) {
            return new WrappedStreamUTF8Reader();
        } else if (m_encodingName.equalsIgnoreCase("ISO-8859-1") ||
            m_encodingName.equalsIgnoreCase("ASCII")) {
            return new WrappedStreamISO88591Reader();
        } else {
            return new InputStreamReader(new WrappedStream(), m_encodingName);
        }
    }
    
    /**
     * Get encoding for input document. This call may not return an accurate
     * result until after {@link #getReader} is called.
     *
     * @return character encoding for input document
     */
    public String getEncoding() {
        return m_encodingName;
    }
    
    /**
     * Close document input. Completes reading of document input, including
     * closing the input medium.
     *
     * @throws IOException on error closing document
     */
    public void close() throws IOException {
        if (m_byteBuffer != null) {
            m_byteBuffer.finish();
        }
        reset();
    }
    
    /**
     * Reset to initial state for reuse.
     */
    public void reset() {
        m_scanOffset = 0;
        m_endOffset = 0;
        m_emptyOffset = 0;
        m_encodingName = null;
    }
    
    /**
     * Stream that just uses the enclosing class to buffer input from the
     * wrapped stream.
     */
    private class WrappedStream extends InputStream
    {
        /* (non-Javadoc)
         * @see java.io.InputStream#available()
         */
        public int available() throws IOException {
            if (m_emptyOffset >= m_endOffset) {
                m_byteBuffer.require(1);
                m_buffer = m_byteBuffer.getBuffer();
                m_emptyOffset = m_byteBuffer.getOffset();
                m_endOffset = m_byteBuffer.getLimit();
            }
            return m_endOffset - m_emptyOffset ;
        }
        
        /* (non-Javadoc)
         * @see java.io.InputStream#close()
         */
        public void close() throws IOException {
            InputStreamWrapper.this.close();
        }
        
        /* (non-Javadoc)
         * @see java.io.InputStream#read(byte[], int, int)
         */
        public int read(byte[] b, int off, int len) throws IOException {
            int avail;
            int actual = 0;
            while (len > (avail = m_endOffset - m_emptyOffset)) {
                System.arraycopy(m_buffer, m_emptyOffset, b, off, avail);
                off += avail;
                len -= avail;
                actual += avail;
                m_emptyOffset = m_endOffset;
                if (!fillBuffer()) {
                    return actual == 0 ? -1 : actual;
                }
            }
            System.arraycopy(m_buffer, m_emptyOffset, b, off, len);
            m_emptyOffset += len;
            return actual + len;
        }
        
        /* (non-Javadoc)
         * @see java.io.InputStream#read(byte[])
         */
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
        
        /* (non-Javadoc)
         * @see java.io.InputStream#skip(long)
         */
        public long skip(long n) throws IOException {
            int avail;
            long remain = n;
            while (remain > (avail = m_endOffset - m_emptyOffset)) {
                remain -= avail;
                m_emptyOffset = m_endOffset;
                if (!fillBuffer()) {
                    return n-remain;
                }
            }
            m_emptyOffset += remain;
            return n;
        }
        
        /* (non-Javadoc)
         * @see java.io.InputStream#read()
         */
        public int read() throws IOException {
            if (m_emptyOffset >= m_endOffset && !fillBuffer()) {
                return -1;
            } else {
                return m_buffer[m_emptyOffset++];
            }
        }
    }
    
    /**
     * Reader for input stream using UTF-8 encoding. This uses the enclosing
     * class to buffer input from the stream, interpreting it as characters on
     * demand.
     */
    private class WrappedStreamUTF8Reader extends Reader
    {
        /* (non-Javadoc)
         * @see java.io.Reader#close()
         */
        public void close() throws IOException {
            InputStreamWrapper.this.close();
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#read(char[], int, int)
         */
        public int read(char[] b, int off, int len) throws IOException {
            
            // load up local variables for conversion loop
            int end = off + len;
            int empty = m_emptyOffset;
            byte[] buff = m_buffer;
            while (off < end) {
                
                // fill buffer if less than maximum byte count in character
                if (empty + 3 > m_endOffset) {
                    m_emptyOffset = empty;
                    fillBuffer();
                    empty = m_emptyOffset;
                    if (empty == m_endOffset) {
                        int actual = len + off - end;
                        return actual > 0 ? actual : -1;
                    }
                }
                
                // check for single-byte vs multi-byte character next
                int byt = buff[empty++];
                if (byt >= 0) {
                    
                    // single-byte character, just store to output array
                    b[off++] = (char)byt;
                    
                } else if ((byt & 0xE0) == 0xC0) {
                    
                    // double-byte character, check bytes available and store
                    if (empty < m_endOffset) {
                        // TODO: check second byte value
                        b[off++] = (char)(((byt & 0x1F) << 6) +
                            (buff[empty++] & 0x3F));
                    } else {
                        throw new IOException("UTF-8 conversion error");
                    }
                    
                } else if ((byt & 0xF0) == 0xE0) {
                    
                    // three-byte character, check bytes available and store
                    if (empty + 1 < m_endOffset) {
                        // TODO: check second and third byte values
                        int byt2 = buff[empty++] & 0x3F;
                        b[off++] = (char)((((byt & 0x0F) << 6) +
                            byt2 << 6) + (buff[empty++] & 0x3F));
                    } else {
                        throw new IOException("UTF-8 conversion error");
                    }
                } else {
                    // TODO: implement surrogate pair handling
                    throw new IOException("Surrogate pairs not yet supported");
                }
            }
            m_emptyOffset = empty;
            return len;
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#read(char[])
         */
        public int read(char[] b) throws IOException {
            return read(b, 0, b.length);
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#read()
         */
        public int read() throws IOException {
            
            // fill buffer if less than maximum byte count in character
            if (m_emptyOffset + 3 > m_endOffset) {
                fillBuffer();
                if (m_emptyOffset == m_endOffset) {
                    return -1;
                }
            }
            
            // check for single-byte vs multi-byte character next
            int byt = m_buffer[m_emptyOffset++];
            if (byt >= 0) {
                
                // single-byte character, just store to output array
                return byt & 0xFF;
                
            } else if ((byt & 0xE0) == 0xC0) {
                
                // double-byte character, check bytes available and store
                if (m_emptyOffset < m_endOffset) {
                    // TODO: check second byte value
                    return ((byt & 0x1F) << 6) +
                        (m_buffer[m_emptyOffset++] & 0x3F);
                } else {
                    throw new IOException("UTF-8 conversion error");
                }
                
            } else if ((byt & 0xF0) == 0xE0) {
                
                // three-byte character, check bytes available and store
                if (m_emptyOffset + 1 < m_endOffset) {
                    // TODO: check second and third byte values
                    int byt2 = m_buffer[m_emptyOffset++] & 0x3F;
                    return (((byt & 0x0F) << 6) +
                        byt2 << 6) + (m_buffer[m_emptyOffset++] & 0x3F);
                } else {
                    throw new IOException("UTF-8 conversion error");
                }
                
            } else {
                // TODO: implement surrogate pair handling
                throw new IOException("Surrogate pairs not yet supported");
            }
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#ready()
         */
        public boolean ready() throws IOException {
            return m_emptyOffset + 2 < m_endOffset;
        }
    }
    
    /**
     * Reader for input stream using ISO8859-1 encoding. This uses the enclosing
     * class to buffer input from the stream, interpreting it as characters on
     * demand.
     */
    private class WrappedStreamISO88591Reader extends Reader
    {
        /* (non-Javadoc)
         * @see java.io.Reader#close()
         */
        public void close() throws IOException {
            InputStreamWrapper.this.close();
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#read(char[], int, int)
         */
        public int read(char[] b, int off, int len) throws IOException {
            
            // load up local variables for conversion loop
            int end = off + len;
            int empty = m_emptyOffset;
            byte[] buff = m_buffer;
            while (off < end) {
                
                // make sure there's data in buffer
                int avail = m_endOffset - empty;
                if (avail == 0) {
                    m_emptyOffset = empty;
                    if (fillBuffer()) {
                        empty = m_emptyOffset;
                        avail = m_endOffset - empty;
                    } else {
                        int actual = len + off - end;
                        return actual > 0 ? actual : -1;
                    }
                }
                
                // find count of bytes to convert to characters
                int use = end - off;
                if (use > avail) {
                    use = avail;
                }
                
                // convert bytes directly to characters
                int limit = empty + use;
                while (empty < limit) {
                    b[off++] = (char)(buff[empty++] & 0xFF);
                }
            }
            m_emptyOffset = empty;
            return len;
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#read(char[])
         */
        public int read(char[] b) throws IOException {
            return read(b, 0, b.length);
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#read()
         */
        public int read() throws IOException {
            if (m_emptyOffset >= m_endOffset && !fillBuffer()) {
                return -1;
            } else {
                return m_buffer[m_emptyOffset++] & 0xFF;
            }
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#ready()
         */
        public boolean ready() throws IOException {
            return m_emptyOffset < m_endOffset;
        }
    }
}
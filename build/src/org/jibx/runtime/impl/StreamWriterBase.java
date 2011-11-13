/*
Copyright (c) 2004-2010, Dennis M. Sosnoski. All rights reserved.

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
import java.io.UnsupportedEncodingException;

/**
 * Base handler for marshalling text document to an output stream. This needs to
 * be subclassed with implementation methods specific to the encoding used.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class StreamWriterBase extends XMLWriterBase
{
    //
    // Defined entities and special sequences as bytes
    
    protected final byte[] m_quotEntityBytes;
    protected final byte[] m_ampEntityBytes;
    protected final byte[] m_gtEntityBytes;
    protected final byte[] m_ltEntityBytes;
    protected final byte[] m_cdataStartBytes;
    protected final byte[] m_cdataEndBytes;
    
    /** Output buffer. */
    private IOutByteBuffer m_byteBuffer;
    
    /** Name of encoding used for stream. */
    private final String m_encodingName;
    
    /** Original writer (only used when created using copy constructor,
     <code>null</code> otherwise). */
    private final StreamWriterBase m_baseWriter;
    
    /** Cached reference to byte array used by buffer. */
    protected byte[] m_buffer;
    
    /** Current fill offset in buffer byte array. */
    protected int m_fillOffset;
    
    /** Byte sequences for prefixes of namespaces in scope. */
    protected byte[][] m_prefixBytes;
    
    /** Byte sequences for prefixes of extension namespaces in scope. */
    protected byte[][][] m_extensionBytes;
    
    /** Indent tags for pretty-printed text. */
    private boolean m_indent;
    
    /** Base number of characters in indent sequence (end of line only). */
    private int m_indentBase;
    
    /** Number of extra characters in indent sequence per level of nesting. */
    private int m_indentPerLevel;
    
    /** Raw text for indentation sequences. */
    private byte[] m_indentSequence;
    
    /**
     * Constructor with supplied buffer.
     *
     * @param enc character encoding used for output to streams (upper case)
     * @param uris ordered array of URIs for namespaces used in document (must
     * be constant; the value in position 0 must always be the empty string "",
     * and the value in position 1 must always be the XML namespace
     * "http://www.w3.org/XML/1998/namespace")
     */
    protected StreamWriterBase(String enc, String[] uris) {
        super(uris);
        m_encodingName = enc;
        m_baseWriter = null;
        m_prefixBytes = new byte[uris.length][];
        try {
            m_quotEntityBytes = "&quot;".getBytes(m_encodingName);
            m_ampEntityBytes = "&amp;".getBytes(m_encodingName);
            m_gtEntityBytes = "&gt;".getBytes(m_encodingName);
            m_ltEntityBytes = "&lt;".getBytes(m_encodingName);
            m_cdataStartBytes = "<![CDATA[".getBytes(m_encodingName);
            m_cdataEndBytes = "]]>".getBytes(m_encodingName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException
                ("Internal error - unsupported encoding " + m_encodingName);
        }
    }
    
    /**
     * Copy constructor. This takes the stream and encoding information from a
     * supplied instance, while setting a new array of namespace URIs. It's
     * intended for use when invoking one binding from within another binding.
     *
     * @param base instance to be used as base for writer
     * @param uris ordered array of URIs for namespaces used in document
     * (see {@link #StreamWriterBase(String, String[])})
     */
    public StreamWriterBase(StreamWriterBase base, String[] uris) {
        super(base, uris);
        m_encodingName = base.m_encodingName;
        m_baseWriter = base;
        m_prefixBytes = new byte[uris.length][];
        m_byteBuffer = base.m_byteBuffer;
        m_buffer = base.m_buffer;
        m_fillOffset = base.m_fillOffset;
        m_indent = base.m_indent;
        m_indentBase = base.m_indentBase;
        m_indentPerLevel = base.m_indentPerLevel;
        m_indentSequence = base.m_indentSequence;
        byte[][][] extbytes = base.m_extensionBytes;
        if (extbytes != null) {
            m_extensionBytes = new byte[extbytes.length][][];
            System.arraycopy(extbytes, 0, m_extensionBytes, 0,
                m_extensionBytes.length);
        }
        m_quotEntityBytes = base.m_quotEntityBytes;
        m_ampEntityBytes = base.m_ampEntityBytes;
        m_gtEntityBytes = base.m_gtEntityBytes;
        m_ltEntityBytes = base.m_ltEntityBytes;
        m_cdataStartBytes = base.m_cdataStartBytes;
        m_cdataEndBytes = base.m_cdataEndBytes;
    }
    
    /**
     * Set the byte buffer.
     *
     * @param buff
     */
    public void setBuffer(IOutByteBuffer buff) {
        m_byteBuffer = buff;
        m_buffer = buff.getBuffer();
        m_fillOffset = buff.getOffset();
    }
    
    /**
     * Get the name of the character encoding used by this writer.
     *
     * @return encoding
     */
    public String getEncodingName() {
        return m_encodingName;
    }
    
    /**
     * Set namespace URIs. This forces a reset of the writer, clearing any
     * buffered output. It is intended to be used only for reconfiguring an
     * existing writer for reuse.
     *
     * @param uris ordered array of URIs for namespaces used in document
     * @throws IOException 
     */
    public void setNamespaceUris(String[] uris) throws IOException {
        reset();
        boolean diff = false;
        String[] olds = getNamespaces();
        if (olds.length == uris.length) {
            for (int i = 0; i < uris.length; i++) {
                if (!uris[i].equals(olds[i])) {
                    diff = true;
                    break;
                }
            }
        } else {
            diff = true;
        }
        if (diff) {
            internalSetUris(uris);
            m_prefixBytes = new byte[uris.length][];
            defineNamespace(0, "");
            defineNamespace(1, "xml");
        }
    }
    
    /**
     * Set nesting indentation. This is advisory only, and implementations of
     * this interface are free to ignore it. The intent is to indicate that the
     * generated output should use indenting to illustrate element nesting.
     *
     * @param count number of character to indent per level, or disable
     * indentation if negative (zero means new line only)
     * @param newline sequence of characters used for a line ending
     * (<code>null</code> means use the single character '\n')
     * @param indent whitespace character used for indentation
     */
    public void setIndentSpaces(int count, String newline, char indent) {
        if (count >= 0) {
            try {
                if (newline == null) {
                    newline = "\n";
                }
                m_indent = true;
                byte[] base = newline.getBytes(m_encodingName);
                m_indentBase = base.length;
                byte[] per = new String(new char[]
                    { indent }).getBytes(m_encodingName);
                m_indentPerLevel = count * per.length;
                int length = m_indentBase + m_indentPerLevel * 10;
                m_indentSequence = new byte[length];
                for (int i = 0; i < length; i++) {
                    if (i < newline.length()) {
                        m_indentSequence[i] = base[i];
                    } else {
                        int index = (i - m_indentBase) % per.length;
                        m_indentSequence[i] = per[index];
                    }
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException
                    ("Encoding " + m_encodingName + " not recognized by JVM");
            }
        } else {
            m_indent = false;
        }
    }
    
    /**
     * Make at least the requested number of bytes available in the output
     * buffer. If necessary, the output buffer will be replaced by a larger
     * buffer.
     *
     * @param length number of bytes space to be made available
     * @throws IOException if error writing to document
     */
    protected void makeSpace(int length) throws IOException {
        if (m_fillOffset + length > m_buffer.length) {
            m_byteBuffer.setOffset(m_fillOffset);
            m_byteBuffer.free(m_fillOffset, length);
            m_buffer = m_byteBuffer.getBuffer();
            m_fillOffset = m_byteBuffer.getOffset();
        }
    }
    
    /**
     * Report that namespace has been undefined.
     *
     * @param index post-translation namespace URI index number
     */
    protected void undefineNamespace(int index) {
        if (index < m_prefixBytes.length) {
            m_prefixBytes[index] = null;
        } else if (m_extensionBytes != null) {
            index -= m_prefixes.length;
            for (int i = 0; i < m_extensionBytes.length; i++) {
                int length = m_extensionBytes[i].length;
                if (index < length) {
                    m_extensionBytes[i][index] = null;
                    break;
                } else {
                    index -= length;
                }
            }
        } else {
            throw new IllegalArgumentException("Index out of range");
        }
    }
    
    /**
     * Write namespace prefix to output. This internal method is used to throw
     * an exception when an undeclared prefix is used.
     *
     * @param index namespace URI index number
     * @throws IOException if error writing to document
     */
    protected void writePrefix(int index) throws IOException {
        try {
            byte[] bytes = null;
            index = translateNamespace(index);
            if (index < m_prefixBytes.length) {
                bytes = m_prefixBytes[index];
            } else if (m_extensionBytes != null) {
                index -= m_prefixes.length;
                for (int i = 0; i < m_extensionBytes.length; i++) {
                    int length = m_extensionBytes[i].length;
                    if (index < length) {
                        bytes = m_extensionBytes[i][index];
                        break;
                    } else {
                        index -= length;
                    }
                }
            }
            if (bytes.length > 0) {
                makeSpace(bytes.length);
                System.arraycopy(bytes, 0, m_buffer, m_fillOffset, bytes.length);
                m_fillOffset += bytes.length;
            }
        } catch (NullPointerException ex) {
            throw new IOException("Namespace URI has not been declared.");
        }
    }
    
    /**
     * Write entity bytes to output. 
     *
     * @param bytes actual bytes to be written
     * @param offset starting offset in buffer
     * @return offset for next data byte in buffer
     */
    protected int writeEntity(byte[] bytes, int offset) {
        System.arraycopy(bytes, 0, m_buffer, offset, bytes.length);
        return offset + bytes.length;
    }
    
    /**
     * Append extension namespace URIs to those in mapping.
     *
     * @param uris namespace URIs to extend those in mapping
     */
    public void pushExtensionNamespaces(String[] uris) {
        super.pushExtensionNamespaces(uris);
        byte[][] items = new byte[uris.length][];
        if (m_extensionBytes == null) {
            m_extensionBytes = new byte[][][] { items };
        } else {
            int length = m_extensionBytes.length;
            byte[][][] grow = new byte[length+1][][];
            System.arraycopy(m_extensionBytes, 0, grow, 0, length);
            grow[length] = items;
            m_extensionBytes = grow;
        }
    }
    
    /**
     * Remove extension namespace URIs. This removes the last set of
     * extension namespaces pushed using {@link #pushExtensionNamespaces}.
     */
    public void popExtensionNamespaces() {
        super.popExtensionNamespaces();
        int length = m_extensionBytes.length;
        if (length == 1) {
            m_extensionBytes = null;
        } else {
            byte[][][] shrink = new byte[length-1][][];
            System.arraycopy(m_extensionBytes, 0, shrink, 0, length-1);
            m_extensionBytes = shrink;
        }
    }
    
    /**
     * Request output indent. Output the line end sequence followed by the
     * appropriate number of indent characters.
     * 
     * @param bias indent depth difference (positive or negative) from current
     * element nesting depth
     * @throws IOException on error writing to document
     */
    public void indent(int bias) throws IOException {
        if (m_indent) {
            int length = m_indentBase +
                (getNestingDepth() + bias) * m_indentPerLevel;
            if (length > 0) {
                flagContent();
                if (length > m_indentSequence.length) {
                    int use = Math.max(length,
                        m_indentSequence.length*2 - m_indentBase);
                    byte[] grow = new byte[use];
                    System.arraycopy(m_indentSequence, 0, grow, 0,
                        m_indentSequence.length);
                    for (int i = m_indentSequence.length; i < use; i++) {
                        grow[i] = grow[m_indentBase];
                    }
                    m_indentSequence = grow;
                }
                makeSpace(length);
                System.arraycopy(m_indentSequence, 0, m_buffer, m_fillOffset,
                    length);
                m_fillOffset += length;
            }
        }
    }
    
    /**
     * Request output indent. Output the line end sequence followed by the
     * appropriate number of indent characters for the current nesting level.
     * 
     * @throws IOException on error writing to document
     */
    public void indent() throws IOException {
        indent(0);
    }
    
    /**
     * Flush document output. Forces out all output generated to this point.
     *
     * @throws IOException on error writing to document
     */
    public void flush() throws IOException {
        flagContent();
        m_byteBuffer.setOffset(m_fillOffset);
        m_byteBuffer.flush();
        m_fillOffset = m_byteBuffer.getOffset();
        if (m_baseWriter != null) {
            m_baseWriter.m_fillOffset = m_fillOffset;
        }
    }
    
    /**
     * Close document output. Completes writing of document output, including
     * closing the output medium.
     *
     * @throws IOException on error writing to document
     */
    public void close() throws IOException {
        flush();
        m_byteBuffer.finish();
    }
}
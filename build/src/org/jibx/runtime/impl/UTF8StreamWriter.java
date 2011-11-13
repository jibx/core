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

import org.jibx.runtime.IXMLWriter;

/**
 * Handler for marshalling text document to a UTF-8 output stream.
 *
 * @author Dennis M. Sosnoski
 */
public class UTF8StreamWriter extends StreamWriterBase
{
    /** Conversion buffer for prefixes; */
    private byte[] m_converts;
    
    /**
     * Constructor with supplied buffer.
     *
    * @param uris ordered array of URIs for namespaces used in document (must
     * be constant; the value in position 0 must always be the empty string "",
     * and the value in position 1 must always be the XML namespace
     * "http://www.w3.org/XML/1998/namespace")
     */
    public UTF8StreamWriter(String[] uris) {
        super("UTF-8", uris);
        defineNamespace(0, "");
        defineNamespace(1, "xml");
    }
    
    /**
     * Copy constructor. This takes the stream from a supplied instance, while
     * setting a new array of namespace URIs. It's intended for use when
     * invoking one binding from within another binding.
     *
     * @param base instance to be used as base for writer
     * @param uris ordered array of URIs for namespaces used in document
     * (see {@link #UTF8StreamWriter(String[])})
     */
    public UTF8StreamWriter(UTF8StreamWriter base, String[] uris) {
        super(base, uris);
        defineNamespace(0, "");
        defineNamespace(1, "xml");
    }
    
    /**
     * Write markup text to output. Markup text can be written directly to the
     * output without the need for any escaping, but still needs to be properly
     * encoded.
     *
     * @param text markup text to be written
     * @throws IOException if error writing to document
     */
    protected void writeMarkup(String text) throws IOException {
        int length = text.length();
        makeSpace(length * 3);
        int fill = m_fillOffset;
        for (int i = 0; i < length; i++) {
            char chr = text.charAt(i);
            if (chr > 0x7F) {
                if (chr > 0x7FF) {
                    m_buffer[fill++] = (byte)(0xE0 + (chr >> 12));
                    m_buffer[fill++] = (byte)(0x80 + ((chr >> 6) & 0x3F));
                    m_buffer[fill++] = (byte)(0x80 + (chr & 0x3F));
                } else {
                    m_buffer[fill++] = (byte)(0xC0 + (chr >> 6));
                    m_buffer[fill++] = (byte)(0x80 + (chr & 0x3F));
                }
            } else {
                m_buffer[fill++] = (byte)chr;
            }
        }
        m_fillOffset = fill;
    }
    
    /**
     * Write markup character to output. Markup text can be written directly to
     * the output without the need for any escaping, but still needs to be
     * properly encoded.
     *
     * @param chr markup character to be written
     * @throws IOException if error writing to document
     */
    protected void writeMarkup(char chr) throws IOException {
        makeSpace(3);
        if (chr > 0x7F) {
            if (chr > 0x7FF) {
                m_buffer[m_fillOffset++] = (byte)(0xE0 + (chr >> 12));
                m_buffer[m_fillOffset++] = (byte)(0x80 + ((chr >> 6) & 0x3F));
                m_buffer[m_fillOffset++] = (byte)(0x80 + (chr & 0x3F));
            } else {
                m_buffer[m_fillOffset++] = (byte)(0xC0 + (chr >> 6));
                m_buffer[m_fillOffset++] = (byte)(0x80 + (chr & 0x3F));
            }
        } else {
            m_buffer[m_fillOffset++] = (byte)chr;
        }
    }
    
    /**
     * Report that namespace has been defined.
     *
     * @param index post-translation namespace URI index number
     * @param prefix prefix used for namespace
     */
    protected void defineNamespace(int index, String prefix) {
        int limit = prefix.length() * 3;
        if (m_converts == null) {
            m_converts = new byte[limit];
        } else if (limit > m_converts.length) {
            m_converts = new byte[limit];
        }
        int fill = 0;
        for (int i = 0; i < prefix.length(); i++) {
            char chr = prefix.charAt(i);
            if (chr > 0x7F) {
                if (chr > 0x7FF) {
                    m_converts[fill++] = (byte)(0xE0 + (chr >> 12));
                    m_converts[fill++] = (byte)(0x80 + ((chr >> 6) & 0x3F));
                    m_converts[fill++] = (byte)(0x80 + (chr & 0x3F));
                } else {
                    m_converts[fill++] = (byte)(0xC0 + (chr >> 6));
                    m_converts[fill++] = (byte)(0x80 + (chr & 0x3F));
                }
            } else {
                m_converts[fill++] = (byte)chr;
            }
        }
        byte[] trim;
        if (fill > 0) {
            trim = new byte[fill+1];
            System.arraycopy(m_converts, 0, trim, 0, fill);
            trim[fill] = ':';
        } else {
            trim = new byte[0];
        }
        if (index < m_prefixBytes.length) {
            m_prefixBytes[index] = trim;
        } else if (m_extensionBytes != null) {
            index -= m_prefixBytes.length;
            for (int i = 0; i < m_extensionBytes.length; i++) {
                int length = m_extensionBytes[i].length;
                if (index < length) {
                    m_extensionBytes[i][index] = trim;
                } else {
                    index -= length;
                }
            }
        } else {
            throw new IllegalArgumentException("Index out of range");
        }
    }
    
    /**
     * Write attribute text to output. This needs to write the text with any
     * appropriate escaping.
     *
     * @param text attribute value text to be written
     * @throws IOException if error writing to document
     */
    protected void writeAttributeText(String text) throws IOException {
        int length = text.length();
        makeSpace(length * 6);
        int fill = m_fillOffset;
        for (int i = 0; i < length; i++) {
            char chr = text.charAt(i);
            if (chr == '"') {
                fill = writeEntity(m_quotEntityBytes, fill);
            } else if (chr == '&') {
                fill = writeEntity(m_ampEntityBytes, fill);
            } else if (chr == '<') {
                fill = writeEntity(m_ltEntityBytes, fill);
            } else if (chr == '>' && i > 2 && text.charAt(i-1) == ']' &&
                text.charAt(i-2) == ']') {
                m_buffer[fill++] = (byte)']';
                m_buffer[fill++] = (byte)']';
                fill = writeEntity(m_gtEntityBytes, fill);
            } else if (chr < 0x20) {
                if (chr != 0x9 && chr != 0xA && chr != 0xD) {
                    throw new IOException("Illegal character code 0x" +
                        Integer.toHexString(chr) + " in attribute value text");
                } else {
                    m_buffer[fill++] = (byte)chr;
                }
            } else {
                if (chr > 0x7F) {
                    if (chr > 0x7FF) {
                        if (chr > 0xD7FF && (chr < 0xE000 || chr == 0xFFFE ||
                            chr == 0xFFFF || chr > 0x10FFFF)) {
                            throw new IOException("Illegal character code 0x" +
                                Integer.toHexString(chr) +
                                " in attribute value text");
                        } else {
                            m_buffer[fill++] = (byte)(0xE0 + (chr >> 12));
                            m_buffer[fill++] =
                                (byte)(0x80 + ((chr >> 6) & 0x3F));
                            m_buffer[fill++] = (byte)(0x80 + (chr & 0x3F));
                        }
                    } else {
                        m_buffer[fill++] = (byte)(0xC0 + (chr >> 6));
                        m_buffer[fill++] = (byte)(0x80 + (chr & 0x3F));
                    }
                } else {
                    m_buffer[fill++] = (byte)chr;
                }
            }
        }
        m_fillOffset = fill;
    }
    
    /**
     * Write ordinary character data text content to document.
     *
     * @param text content value text
     * @throws IOException on error writing to document
     */
    public void writeTextContent(String text) throws IOException {
        flagTextContent();
        int length = text.length();
        makeSpace(length * 5);
        int fill = m_fillOffset;
        for (int i = 0; i < length; i++) {
            char chr = text.charAt(i);
            if (chr == '&') {
                fill = writeEntity(m_ampEntityBytes, fill);
            } else if (chr == '<') {
                fill = writeEntity(m_ltEntityBytes, fill);
            } else if (chr == '>' && i > 2 && text.charAt(i-1) == ']' &&
                text.charAt(i-2) == ']') {
                fill = writeEntity(m_gtEntityBytes, fill);
            } else if (chr < 0x20) {
                if (chr != 0x9 && chr != 0xA && chr != 0xD) {
                    throw new IOException("Illegal character code 0x" +
                        Integer.toHexString(chr) + " in content text");
                } else {
                    m_buffer[fill++] = (byte)chr;
                }
            } else {
                if (chr > 0x7F) {
                    if (chr > 0x7FF) {
                        if (chr > 0xD7FF && (chr < 0xE000 || chr == 0xFFFE ||
                            chr == 0xFFFF || chr > 0x10FFFF)) {
                            throw new IOException("Illegal character code 0x" +
                                Integer.toHexString(chr) + " in content text");
                        } else {
                            m_buffer[fill++] = (byte)(0xE0 + (chr >> 12));
                            m_buffer[fill++] =
                                (byte)(0x80 + ((chr >> 6) & 0x3F));
                            m_buffer[fill++] = (byte)(0x80 + (chr & 0x3F));
                        }
                    } else {
                        m_buffer[fill++] = (byte)(0xC0 + (chr >> 6));
                        m_buffer[fill++] = (byte)(0x80 + (chr & 0x3F));
                    }
                } else {
                    m_buffer[fill++] = (byte)chr;
                }
            }
        }
        m_fillOffset = fill;
    }
    
    /**
     * Write CDATA text to document.
     *
     * @param text content value text
     * @throws IOException on error writing to document
     */
    public void writeCData(String text) throws IOException {
        flagTextContent();
        int length = text.length();
        makeSpace(length * 3 + 12);
        int fill = m_fillOffset;
        fill = writeEntity(m_cdataStartBytes, fill);
        for (int i = 0; i < length; i++) {
            char chr = text.charAt(i);
            if (chr == '>' && i > 2 && text.charAt(i-1) == ']' &&
                text.charAt(i-2) == ']') {
                throw new IOException("Sequence \"]]>\" is not allowed " +
                    "within CDATA section text");
            } else if (chr < 0x20) {
                if (chr != 0x9 && chr != 0xA && chr != 0xD) {
                    throw new IOException("Illegal character code 0x" +
                        Integer.toHexString(chr) + " in content text");
                } else {
                    m_buffer[fill++] = (byte)chr;
                }
            } else {
                if (chr > 0x7F) {
                    if (chr > 0x7FF) {
                        if (chr > 0xD7FF && (chr < 0xE000 || chr == 0xFFFE ||
                            chr == 0xFFFF || chr > 0x10FFFF)) {
                            throw new IOException("Illegal character code 0x" +
                                Integer.toHexString(chr) +
                                " in CDATA section text");
                        } else {
                            m_buffer[fill++] = (byte)(0xE0 + (chr >> 12));
                            m_buffer[fill++] =
                                (byte)(0x80 + ((chr >> 6) & 0x3F));
                            m_buffer[fill++] = (byte)(0x80 + (chr & 0x3F));
                        }
                    } else {
                        m_buffer[fill++] = (byte)(0xC0 + (chr >> 6));
                        m_buffer[fill++] = (byte)(0x80 + (chr & 0x3F));
                    }
                } else {
                    m_buffer[fill++] = (byte)chr;
                }
            }
        }
        m_fillOffset = writeEntity(m_cdataEndBytes, fill);
    }
    
    /**
     * Create a child writer instance to be used for a separate binding. The
     * child writer inherits the stream and encoding from this writer, while
     * using the supplied namespace URIs.
     * 
     * @param uris ordered array of URIs for namespaces used in document
     * (see {@link #UTF8StreamWriter(String[])})
     * @return child writer
     * @throws IOException 
     */
    public IXMLWriter createChildWriter(String[] uris) throws IOException {
        flagContent();
        return new UTF8StreamWriter(this, uris);
    }
}
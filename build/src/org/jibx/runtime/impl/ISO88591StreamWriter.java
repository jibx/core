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
public class ISO88591StreamWriter extends StreamWriterBase
{
    /**
     * Constructor with supplied buffer.
     *
    * @param uris ordered array of URIs for namespaces used in document (must
     * be constant; the value in position 0 must always be the empty string "",
     * and the value in position 1 must always be the XML namespace
     * "http://www.w3.org/XML/1998/namespace")
     */
    public ISO88591StreamWriter(String[] uris) {
        super("ISO-8859-1", uris);
        try {
            defineNamespace(0, "");
            defineNamespace(1, "xml");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Copy constructor. This uses the stream and actual buffer from a supplied
     * instance, while setting a new array of namespace URIs. It's intended for
     * use when invoking one binding from within another binding.
     *
     * @param base instance to be used as base for writer
     * @param uris ordered array of URIs for namespaces used in document
     * (see {@link #ISO88591StreamWriter(String[])})
     */
    public ISO88591StreamWriter(ISO88591StreamWriter base, String[] uris) {
        super(base, uris);
        m_prefixBytes = new byte[uris.length][];
        try {
            defineNamespace(0, "");
            defineNamespace(1, "xml");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
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
        makeSpace(length);
        int fill = m_fillOffset;
        for (int i = 0; i < length; i++) {
            char chr = text.charAt(i);
            if (chr > 0xFF) {
                throw new IOException("Unable to write character code 0x" +
                    Integer.toHexString(chr) + " in encoding ISO-8859-1");
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
        makeSpace(1);
        if (chr > 0xFF) {
            throw new IOException("Unable to write character code 0x" +
                Integer.toHexString(chr) + " in encoding ISO-8859-1");
        } else {
            m_buffer[m_fillOffset++] = (byte)chr;
        }
    }
    
    /**
     * Report that namespace has been defined.
     *
     * @param index post-translation namespace URI index number
     * @param prefix prefix used for namespace
     * @throws IOException if error writing to document
     */
    protected void defineNamespace(int index, String prefix)
        throws IOException {
        byte[] buff;
        if (prefix.length() > 0) {
            buff = new byte[prefix.length()+1];
            for (int i = 0; i < buff.length-1; i++) {
                char chr = prefix.charAt(i);
                if (chr > 0xFF) {
                    throw new IOException("Unable to write character code 0x" +
                        Integer.toHexString(chr) + " in encoding ISO-8859-1");
                } else {
                    buff[i] = (byte)chr;
                }
            }
            buff[buff.length-1] = ':';
        } else {
            buff = new byte[0];
        }
        if (index < m_prefixBytes.length) {
            m_prefixBytes[index] = buff;
        } else if (m_extensionBytes != null) {
            index -= m_prefixBytes.length;
            for (int i = 0; i < m_extensionBytes.length; i++) {
                int length = m_extensionBytes[i].length;
                if (index < length) {
                    m_extensionBytes[i][index] = buff;
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
                if (chr > 0xFF) {
                    if (chr > 0xD7FF && (chr < 0xE000 || chr == 0xFFFE ||
                        chr == 0xFFFF || chr > 0x10FFFF)) {
                        throw new IOException("Illegal character code 0x" +
                            Integer.toHexString(chr) +
                            " in attribute value text");
                    } else {
                        m_fillOffset = fill;
                        makeSpace(length - i + 8);
                        fill = m_fillOffset;
                        m_buffer[fill++] = (byte)'&';
                        m_buffer[fill++] = (byte)'#';
                        m_buffer[fill++] = (byte)'x';
                        for (int j = 12; j >= 0; j -= 4) {
                            int nib = (chr >> j) & 0xF;
                            if (nib < 10) {
                                m_buffer[fill++] = (byte)('0' + nib);
                            } else {
                                m_buffer[fill++] = (byte)('A' + nib);
                            }
                        }
                        m_buffer[fill++] = (byte)';';
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
                if (chr > 0xFF) {
                    if (chr > 0xD7FF && (chr < 0xE000 || chr == 0xFFFE ||
                        chr == 0xFFFF || chr > 0x10FFFF)) {
                        throw new IOException("Illegal character code 0x" +
                            Integer.toHexString(chr) +
                            " in character data text");
                    } else {
                        m_fillOffset = fill;
                        makeSpace(length - i + 8);
                        fill = m_fillOffset;
                        m_buffer[fill++] = (byte)'&';
                        m_buffer[fill++] = (byte)'#';
                        m_buffer[fill++] = (byte)'x';
                        for (int j = 12; j >= 0; j -= 4) {
                            int nib = (chr >> j) & 0xF;
                            if (nib < 10) {
                                m_buffer[fill++] = (byte)('0' + nib);
                            } else {
                                m_buffer[fill++] = (byte)('A' + nib);
                            }
                        }
                        m_buffer[fill++] = (byte)';';
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
        makeSpace(length + 12);
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
                if (chr > 0xFF) {
                    throw new IOException("Character code 0x" +
                        Integer.toHexString(chr) +
                        " not allowed by encoding in CDATA section text");
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
     * (see {@link #ISO88591StreamWriter(String[])})
     * @return child writer
     * @throws IOException 
     */
    public IXMLWriter createChildWriter(String[] uris) throws IOException {
        flagContent();
        return new ISO88591StreamWriter(this, uris);
    }
}
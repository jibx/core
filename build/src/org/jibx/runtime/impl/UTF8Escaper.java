/*
Copyright (c) 2004-2005, Dennis M. Sosnoski.
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
import java.io.Writer;

import org.jibx.runtime.ICharacterEscaper;

/**
 * Handler for writing UTF output stream (for any form of UTF, despite the
 * name). This code is specifically for XML 1.0 and would require changes for
 * XML 1.1 (to handle the added legal characters, rather than throwing an
 * exception).
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class UTF8Escaper implements ICharacterEscaper
{
    /** Singleton instance of class. */
    private static final UTF8Escaper s_instance = new UTF8Escaper();
    
    /**
     * Private constructor to prevent external creation.
     */
    
    private UTF8Escaper() {}
    
    /**
     * Write attribute value with character entity substitutions. This assumes
     * that attributes use the regular quote ('"') delimitor.
     *
     * @param text attribute value text
     * @param writer sink for output text
     * @throws IOException on error writing to document
     */

    public void writeAttribute(String text, Writer writer) throws IOException {
        int mark = 0;
        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if (chr == '"') {
                writer.write(text, mark, i-mark);
                mark = i+1;
                writer.write("&quot;");
            } else if (chr == '&') {
                writer.write(text, mark, i-mark);
                mark = i+1;
                writer.write("&amp;");
            } else if (chr == '<') {
                writer.write(text, mark, i-mark);
                mark = i+1;
                writer.write("&lt;");
            } else if (chr == '>' && i > 2 && text.charAt(i-1) == ']' &&
                text.charAt(i-2) == ']') {
                writer.write(text, mark, i-mark-2);
                mark = i+1;
                writer.write("]]&gt;");
            } else if (chr < 0x20) {
                if (chr != 0x9 && chr != 0xA && chr != 0xD) {
                    throw new IOException("Illegal character code 0x" +
                        Integer.toHexString(chr) + " in attribute value text");
                }
            } else if (chr > 0xD7FF && (chr < 0xE000 || chr == 0xFFFE ||
                chr == 0xFFFF || chr > 0x10FFFF)) {
                throw new IOException("Illegal character code 0x" +
                    Integer.toHexString(chr) + " in attribute value text");
            }
        }
        writer.write(text, mark, text.length()-mark);
    }
    
    /**
     * Write content value with character entity substitutions.
     *
     * @param text content value text
     * @param writer sink for output text
     * @throws IOException on error writing to document
     */

    public void writeContent(String text, Writer writer) throws IOException {
        int mark = 0;
        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if (chr == '&') {
                writer.write(text, mark, i-mark);
                mark = i+1;
                writer.write("&amp;");
            } else if (chr == '<') {
                writer.write(text, mark, i-mark);
                mark = i+1;
                writer.write("&lt;");
            } else if (chr == '>' && i > 2 && text.charAt(i-1) == ']' &&
                text.charAt(i-2) == ']') {
                writer.write(text, mark, i-mark-2);
                mark = i+1;
                writer.write("]]&gt;");
            } else if (chr < 0x20) {
                if (chr != 0x9 && chr != 0xA && chr != 0xD) {
                    throw new IOException("Illegal character code 0x" +
                        Integer.toHexString(chr) + " in content text");
                }
            } else if (chr > 0xD7FF && (chr < 0xE000 || chr == 0xFFFE ||
                chr == 0xFFFF || chr > 0x10FFFF)) {
                throw new IOException("Illegal character code 0x" +
                    Integer.toHexString(chr) + " in content text");
            }
        }
        writer.write(text, mark, text.length()-mark);
    }
    
    /**
     * Write CDATA to document. This writes the beginning and ending sequences
     * for a CDATA section as well as the actual text, verifying that only
     * characters allowed by the encoding are included in the text.
     *
     * @param text content value text
     * @param writer sink for output text
     * @throws IOException on error writing to document
     */

    public void writeCData(String text, Writer writer) throws IOException {
        writer.write("<![CDATA[");
        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if (chr == '>' && i > 2 && text.charAt(i-1) == ']' &&
                text.charAt(i-2) == ']') {
                throw new IOException("Sequence \"]]>\" is not allowed " +                    "within CDATA section text");
            } else if (chr < 0x20) {
                if (chr != 0x9 && chr != 0xA && chr != 0xD) {
                    throw new IOException("Illegal character code 0x" +
                        Integer.toHexString(chr) + " in CDATA section");
                }
            } else if (chr > 0xD7FF &&
                (chr < 0xE000 || chr == 0xFFFE || chr == 0xFFFF)) {
                throw new IOException("Illegal character code 0x" +
                    Integer.toHexString(chr) + " in CDATA section");
            }
        }
        writer.write(text);
        writer.write("]]>");
    }
    
    /**
     * Get instance of escaper.
     *
     * @return escaper instance
     */
    
    public static ICharacterEscaper getInstance() {
        return s_instance;
    }
}
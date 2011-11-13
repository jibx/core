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
import java.io.Writer;

import org.jibx.runtime.ICharacterEscaper;
import org.jibx.runtime.IXMLWriter;

/**
 * Generic handler for marshalling text document to a writer. This is the
 * most general output handler since it can be used with any character encoding
 * and and output writer.
 *
 * @author Dennis M. Sosnoski
 */
public class GenericXMLWriter extends XMLWriterBase
{
    /** Writer for text output. */
    private Writer m_writer;
    
    /** Escaper for character data content output. */
    private ICharacterEscaper m_escaper;
    
    /** Indent tags for pretty-printed text. */
    private boolean m_indent;
    
    /** Base number of characters in indent sequence (end of line only). */
    private int m_indentBase;
    
    /** Number of extra characters in indent sequence per level of nesting. */
    private int m_indentPerLevel;
    
    /** Raw text for indentation sequences. */
    private char[] m_indentSequence;
    
    /**
     * Constructor.
     *
     * @param uris ordered array of URIs for namespaces used in document (must
     * be constant; the value in position 0 must always be the empty string "",
     * and the value in position 1 must always be the XML namespace
     * "http://www.w3.org/XML/1998/namespace")
     */
    public GenericXMLWriter(String[] uris) {
        super(uris);
    }
    
    /**
     * Copy constructor. This takes the writer from a supplied instance, while
     * setting a new array of namespace URIs. It's intended for use when
     * invoking one binding from within another binding.
     *
     * @param base instance to be used as base for writer
     * @param uris ordered array of URIs for namespaces used in document
     * (see {@link #GenericXMLWriter(String[])})
     */
    public GenericXMLWriter(GenericXMLWriter base, String[] uris) {
        super(base, uris);
        setOutput(base.m_writer, base.m_escaper);
        m_indent = base.m_indent;
        m_indentBase = base.m_indentBase;
        m_indentPerLevel = base.m_indentPerLevel;
        m_indentSequence = base.m_indentSequence;
    }
    
    /**
     * Set output writer and escaper. If an output writer is currently open when
     * this is called the existing writer is flushed and closed, with any
     * errors ignored.
     *
     * @param outw writer for document data output
     * @param escaper character escaper for chosen encoding
     */
    public void setOutput(Writer outw, ICharacterEscaper escaper) {
        try {
            close();
        } catch (IOException e) { /* deliberately empty */ }
        m_writer = outw;
        m_escaper = escaper;
        reset();
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
            if (newline == null) {
                newline = "\n";
            }
            m_indent = true;
            m_indentBase = newline.length();
            m_indentPerLevel = count;
            int length = newline.length() + count * 10;
            m_indentSequence = new char[length];
            for (int i = 0; i < length; i++) {
                if (i < newline.length()) {
                    m_indentSequence[i] = newline.charAt(i);
                } else {
                    m_indentSequence[i] = indent;
                }
            }
        } else {
            m_indent = false;
        }
    }
    
    /**
     * Write markup text to output. Markup text can be written directly to the
     * output without the need for any escaping.
     *
     * @param text markup text to be written
     * @throws IOException if error writing to document
     */
    protected void writeMarkup(String text) throws IOException {
        m_writer.write(text);
    }
    
    /**
     * Write markup character to output. Markup text can be written directly to
     * the output without the need for any escaping.
     *
     * @param chr markup character to be written
     * @throws IOException if error writing to document
     */
    protected void writeMarkup(char chr) throws IOException {
        m_writer.write(chr);
    }
    
    /**
     * Report that namespace has been defined.
     *
     * @param index post-translation namespace URI index number
     * @param prefix prefix used for namespace
     */
    protected void defineNamespace(int index, String prefix) {}
    
    /**
     * Report that namespace has been undefined.
     *
     * @param index post-translation namespace URI index number
     */
    protected void undefineNamespace(int index) {}
    
    /**
     * Write namespace prefix to output. This internal method is used to handle
     * translation of namespace indexes to internal form, and also to throw an
     * exception when an undeclared prefix is used.
     *
     * @param index namespace URI index number
     * @throws IOException if error writing to document
     */
    protected void writePrefix(int index) throws IOException {
        try {
            String text = getNamespacePrefix(index);
            if (text.length() > 0) {
                m_writer.write(text);
                m_writer.write(':');
            }
        } catch (NullPointerException ex) {
            throw new IOException("Namespace URI has not been declared.");
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
        m_escaper.writeAttribute(text, m_writer);
    }
    
    /**
     * Write ordinary character data text content to document. This needs to
     * write the text with any appropriate escaping.
     *
     * @param text content value text
     * @throws IOException on error writing to document
     */
    public void writeTextContent(String text) throws IOException {
        flagTextContent();
        m_escaper.writeContent(text, m_writer);
    }
    
    /**
     * Write CDATA text to document. This needs to write the text with any
     * appropriate escaping.
     *
     * @param text content value text
     * @throws IOException on error writing to document
     */
    public void writeCData(String text) throws IOException {
        flagTextContent();
        m_escaper.writeCData(text, m_writer);
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
            flagContent();
            int length = m_indentBase +
                (getNestingDepth() + bias) * m_indentPerLevel;
            if (length > m_indentSequence.length) {
                int use = Math.max(length,
                    m_indentSequence.length*2 - m_indentBase);
                char[] grow = new char[use];
                System.arraycopy(m_indentSequence, 0, grow, 0,
                    m_indentSequence.length);
                for (int i = m_indentSequence.length; i < use; i++) {
                    grow[i] = grow[m_indentBase];
                }
                m_indentSequence = grow;
            }
            m_writer.write(m_indentSequence, 0, length);
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
        // internal flush only, do not pass through to writer
        flagContent();
    }
    
    /**
     * Close document output. Completes writing of document output, including
     * closing the output medium.
     *
     * @throws IOException on error writing to document
     */
    public void close() throws IOException {
        flush();
        if (m_writer != null) {
            m_writer.close();
            m_writer = null;
        }
    }
    
    /**
     * Create a child writer instance to be used for a separate binding. The
     * child writer inherits the stream and encoding from this writer, while
     * using the supplied namespace URIs.
     * 
     * @param uris ordered array of URIs for namespaces used in document
     * (see {@link #GenericXMLWriter(String[])})
     * @return child writer
     */
    public IXMLWriter createChildWriter(String[] uris) {
        return new GenericXMLWriter(this, uris);
    }
}
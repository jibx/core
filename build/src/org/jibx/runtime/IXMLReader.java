/*
Copyright (c) 2005-2009, Dennis M. Sosnoski.
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

package org.jibx.runtime;

import java.io.IOException;

/**
 * XML reader interface used for input of unmarshalled document. This interface
 * allows easy substitution of different parsers or other input sources.
 *
 * @author Dennis M. Sosnoski
 */
public interface IXMLReader
{
    //
    // Event types reported by nextEvent() method
    static final int START_DOCUMENT = 0;
    static final int END_DOCUMENT = 1;
    static final int START_TAG = 2;
    static final int END_TAG = 3;
    static final int TEXT = 4;
    static final int CDSECT = 5;
    static final int ENTITY_REF = 6;
    static final int IGNORABLE_WHITESPACE = 7;
    static final int PROCESSING_INSTRUCTION = 8;
    static final int COMMENT = 9;
    static final int DOCDECL = 10;
    
    /**
     * Initialize reader.
     * 
     * @throws IOException 
     */
    void init() throws IOException;

    /**
     * Build current parse input position description.
     *
     * @return text description of current parse position
     */
    String buildPositionString();
    
    /**
     * Advance to next parse event of input document.
     *
     * @return parse event type code
     * @throws JiBXException if error reading or parsing document
     */
    int nextToken() throws JiBXException;
    
    /**
     * Advance to next binding component of input document. This is a
     * higher-level operation than {@link #nextToken()}, which consolidates text
     * content and ignores parse events for components such as comments and PIs.
     *
     * @return parse event type code
     * @throws JiBXException if error reading or parsing document
     */
    int next() throws JiBXException;
    
    /**
     * Gets the current parse event type, without changing the current parse
     * state.
     *
     * @return parse event type code
     * @throws JiBXException if error parsing document
     */
    int getEventType() throws JiBXException;
    
    /**
     * Get element name from the current start or end tag.
     *
     * @return local name if namespace handling enabled, full name if namespace
     * handling disabled
     * @throws IllegalStateException if not at a start or end tag (optional)
     */
    String getName();
    
    /**
     * Get element namespace from the current start or end tag.
     *
     * @return namespace URI if namespace handling enabled and element is in a
     * namespace, empty string otherwise
     * @throws IllegalStateException if not at a start or end tag (optional)
     */
    String getNamespace();

    /**
     * Get element prefix from the current start or end tag.
     *
     * @return prefix text (<code>null</code> if no prefix)
     * @throws IllegalStateException if not at a start or end tag
     */
    String getPrefix();
    
    /**
     * Get the number of attributes of the current start tag.
     *
     * @return number of attributes
     * @throws IllegalStateException if not at a start tag (optional)
     */
    int getAttributeCount();
    
    /**
     * Get an attribute name from the current start tag.
     *
     * @param index attribute index
     * @return local name if namespace handling enabled, full name if namespace
     * handling disabled
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getAttributeName(int index);
    
    /**
     * Get an attribute namespace from the current start tag.
     *
     * @param index attribute index
     * @return namespace URI if namespace handling enabled and attribute is in a
     * namespace, empty string otherwise
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getAttributeNamespace(int index);

    /**
     * Get an attribute prefix from the current start tag.
     * 
     * @param index attribute index
     * @return prefix for attribute (<code>null</code> if no prefix present)
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getAttributePrefix(int index);
    
    /**
     * Get an attribute value from the current start tag.
     *
     * @param index attribute index
     * @return value text
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getAttributeValue(int index);
    
    /**
     * Get an attribute value from the current start tag.
     *
     * @param ns namespace URI for expected attribute (may be <code>null</code>
     * or the empty string for the empty namespace)
     * @param name attribute name expected
     * @return attribute value text, or <code>null</code> if missing
     * @throws IllegalStateException if not at a start tag
     */
    String getAttributeValue(String ns, String name);
    
    /**
     * Get current text. When positioned on a TEXT event this returns the actual
     * text; for CDSECT it returns the text inside the CDATA section; for
     * COMMENT, DOCDECL, or PROCESSING_INSTRUCTION it returns the text inside
     * the structure.
     *
     * @return text for current event
     */
    String getText();
    
    /**
     * Get current element nesting depth. The returned depth always includes the
     * current start or end tag (if positioned on a start or end tag).
     *
     * @return element nesting depth
     */
    int getNestingDepth();
    
    /**
     * Get number of namespace declarations active at depth.
     *
     * @param depth element nesting depth
     * @return number of namespaces active at depth
     * @throws IllegalArgumentException if invalid depth
     */
    int getNamespaceCount(int depth);
    
    /**
     * Get namespace URI.
     *
     * @param index declaration index
     * @return namespace URI
     * @throws IllegalArgumentException if invalid index
     */
    String getNamespaceUri(int index);
    
    /**
     * Get namespace prefix.
     *
     * @param index declaration index
     * @return namespace prefix, <code>null</code> if a default namespace
     * @throws IllegalArgumentException if invalid index
     */
    String getNamespacePrefix(int index);
    
    /**
     * Get document name.
     *
     * @return document name, <code>null</code> if not known
     */
    String getDocumentName();
    
    /**
     * Get current source line number.
     *
     * @return line number from source document, <code>-1</code> if line number
     * information not available
     */
    int getLineNumber();
    
    /**
     * Get current source column number.
     *
     * @return column number from source document, <code>-1</code> if column
     * number information not available
     */
    int getColumnNumber();

    /**
     * Get namespace URI associated with prefix.
     * 
     * @param prefix namespace prefix to be matched (<code>null</code> for
     * default namespace)
     * @return associated URI (<code>null</code> if prefix not defined)
     */
    String getNamespace(String prefix);

    /**
     * Return the input encoding, if known. This is only valid after parsing of
     * a document has been started.
     *
     * @return input encoding (<code>null</code> if unknown)
     */
    String getInputEncoding();

    /**
     * Return namespace processing flag.
     *
     * @return namespace processing flag (<code>true</code> if namespaces are
     * processed by reader, <code>false</code> if not)
     */
    boolean isNamespaceAware();
}
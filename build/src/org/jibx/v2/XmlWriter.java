/*
 * Copyright (c) 2004-2007, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.v2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;

/**
 * XML writer interface used for output of marshalled document. This interface allows easy substitution of different
 * output formats, including parse event stream equivalents. This makes heavy use of state information, so each method
 * call defined is only valid in certain states.
 * 
 * @author Dennis M. Sosnoski
 */
public interface XmlWriter
{
    /**
     * Get the marshalling context associated with this writer. The marshalling context tracks higher-level information
     * about the conversion of XML into a Java object structure.
     *
     * @return context
     */
    MarshallingContext getContext();
    
    /**
     * Get the current validation context for this writer. The validation context is used both for tracking problems,
     * and to determine the appropriate handling when a problem occurs.
     * 
     * @return context
     */
    ValidationContext getValidationContext();
    
    /**
     * Get the current element nesting depth. Elements are only counted in the depth returned when they're officially
     * open - after the start tag has been output and before the end tag has been output.
     * 
     * @return number of nested elements at current point in output
     */
    int getNestingDepth();
    
    /**
     * Get the number of namespaces currently defined. This is equivalent to the index of the next extension namespace
     * added.
     * 
     * @return namespace count
     */
    int getNamespaceCount();
    
    /**
     * Set nesting indentation. This is advisory only, and implementations of this interface are free to ignore it. The
     * intent is to indicate that the generated output should use indenting to illustrate element nesting.
     * 
     * @param count number of character to indent per level, or disable indentation if negative (zero means new line
     * only)
     * @param newline sequence of characters used for a line ending (<code>null</code> means use the single character
     * '\n')
     * @param indent whitespace character used for indentation
     */
    void setIndent(int count, String newline, char indent);
    
    /**
     * Set attribute double-quote character usage. If this is <code>true</code>, the double-quote (") character is
     * preferred for attribute values; if <code>false</code> (the default), the single-quote (') character is preferred.
     * This setting is advisory only, and implementations of this interface are free to ignore it.
     * 
     * @param prefdbl prefer double-quote flag
     */
    void setAttributeDoubleQuote(boolean prefdbl);
    
    /**
     * Set boolean numeric value usage. If this is <code>true</code>, numeric values ('0' and '1') are preferred for
     * boolean values; if <code>false</code> (the default), text values ('false' and 'true') are preferred. This setting
     * is advisory only, and implementations of this interface are free to ignore it.
     * 
     * @param prefnum prefer numeric boolean value flag
     */
    void setBooleanNumeric(boolean prefnum);
    
    /**
     * Set the implicit namespace used for elements unless otherwise specified. The namespace must have been opened
     * prior to this call.
     *
     * @param ns namespace URI for element (may be the empty string for the no-namespace namespace)
     * @return prior implicit namespace
     */
    String setImplicitNamespace(String ns);
    
    /**
     * Write the start tag for an element in the implicit namespace.
     * 
     * @param name element name
     * @throws JiBXException on unrecoverable error
     */
    void startTag(String name) throws JiBXException;
    
    /**
     * Write the start tag for an element.
     * 
     * @param ns namespace URI for element (may be the empty string for the no-namespace namespace)
     * @param name unqualified element name
     * @throws JiBXException on unrecoverable error
     */
    void startTag(String ns, String name) throws JiBXException;
    
    /**
     * Declare a namespace, make it the implicit namespace, and write the start tag for an element in that namespace.
     * This is just a shortcut for the sequence of calls {@link #openNamespace(String, String)}, {@link
     * #setImplicitNamespace(String)}, {@link #startTag(String)}.
     * 
     * @param ns namespace URI for element (may be the empty string for the no-namespace namespace)
     * @param prefix prefix to be used for the namespace (use "" for default namespace declaration)
     * @param name unqualified element name
     * @throws JiBXException on unrecoverable error
     */
    void startTagImplicit(String ns, String prefix, String name) throws JiBXException;
    
    /**
     * Add a namespace declaration to the next element start tag. If the namespace is already open this call does
     * nothing.
     * 
     * @param ns namespace URI (may be the empty string for the no-namespace namespace)
     * @param prefix prefix to be used for the namespace (use "" for default namespace declaration)
     * @throws JiBXException on unrecoverable error
     */
    void openNamespace(String ns, String prefix) throws JiBXException;
    
    /**
     * Add no-namespace text attribute to current open start tag. This is only valid with an open start tag.
     * 
     * @param name unqualified attribute name
     * @param value text value for attribute
     * @throws JiBXException on unrecoverable error
     */
    void addTextAttribute(String name, String value) throws JiBXException;
    
    /**
     * Add text attribute to current open start tag. This is only valid with an open start tag.
     * 
     * @param ns namespace URI (may be the empty string for the no-namespace namespace)
     * @param name unqualified attribute name
     * @param value text value for attribute
     * @throws JiBXException on unrecoverable error
     */
    void addTextAttribute(String ns, String name, String value) throws JiBXException;
    
    /**
     * Add optional no-namespace text attribute to current open start tag. This is only valid with an open start tag.
     * 
     * @param name unqualified attribute name
     * @param value text value for attribute (<code>null</code> if no value)
     * @throws JiBXException on unrecoverable error
     */
    void addOptionalTextAttribute(String name, String value) throws JiBXException;
    
    /**
     * Add optional text attribute to current open start tag. This is only valid with an open start tag.
     * 
     * @param ns namespace URI (may be the empty string for the no-namespace namespace)
     * @param name unqualified attribute name
     * @param value text value for attribute (<code>null</code> if no value)
     * @throws JiBXException on unrecoverable error
     */
    void addOptionalTextAttribute(String ns, String name, String value) throws JiBXException;
    
    /**
     * Select text content as the destination for a conversion.
     * 
     * @throws JiBXException on unrecoverable error
     */
    void selectContent() throws JiBXException;
    
    /**
     * Select a child element in the implicit namespace as the destination for a conversion.
     * 
     * @param name element name
     * @throws JiBXException on unrecoverable error
     */
    void selectContent(String name) throws JiBXException;
    
    /**
     * Select a child element as the destination for a conversion.
     * 
     * @param ns namespace URI (may be the empty string for the no-namespace namespace)
     * @param name element name
     * @throws JiBXException on unrecoverable error
     */
    void selectContent(String ns, String name) throws JiBXException;
    
    /**
     * Select a no-namespace attribute as the destination for a conversion. This is only valid with an open start tag.
     * 
     * @param name unqualified attribute name
     * @throws JiBXException on unrecoverable error
     */
    void selectAttribute(String name) throws JiBXException;
    
    /**
     * Select an attribute as the destination for a conversion. This is only valid with an open start tag.
     * 
     * @param ns namespace URI (may be the empty string for the no-namespace namespace)
     * @param name unqualified attribute name
     * @throws JiBXException on unrecoverable error
     */
    void selectAttribute(String ns, String name) throws JiBXException;
    
    /**
     * Write a <code>String</code> value to the current text selection.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convert(String value) throws JiBXException;
    
    /**
     * Write an <code>int</code> value to the current text selection.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convert(int value) throws JiBXException;
    
    /**
     * Write a <code>long</code> value to the current text selection.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convert(long value) throws JiBXException;
    
    /**
     * Write a <code>float</code> value to the current text selection.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convert(float value) throws JiBXException;
    
    /**
     * Write a <code>double</code> value to the current text selection.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convert(double value) throws JiBXException;
    
    /**
     * Write a <code>boolean</code> value to the current text selection.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convert(boolean value) throws JiBXException;
    
    /**
     * Write a <code>byte[]</code> value to the current text selection using base64Binary encoding.
     * 
     * @param value attribute value
     * @throws JiBXException on unrecoverable error
     */
    void convert(byte[] value) throws JiBXException;
    
    /**
     * Write a <code>long</code> milliseconds time value to the current text selection as an xsd:dateTime.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convertDateTime(long value) throws JiBXException;
    
    /**
     * Write a <code>long</code> milliseconds time value and associated nanosecond count to the current text selection
     * as an xsd:dateTime.
     * 
     * @param value
     * @param nanos 
     * @throws JiBXException on unrecoverable error
     */
    void convertDateTime(long value, int nanos) throws JiBXException;
    
    /**
     * Write a <code>long</code> milliseconds time value to the current text selection as an xsd:date.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convertDate(long value) throws JiBXException;
    
    /**
     * Write a <code>long</code> milliseconds time value to the current text selection as an xsd:time.
     * 
     * @param value
     * @throws JiBXException on unrecoverable error
     */
    void convertTime(long value) throws JiBXException;
    
    /**
     * Write a <code>long</code> milliseconds time value and associated nanosecond count to the current text selection
     * as an xsd:time.
     * 
     * @param value
     * @param nanos 
     * @throws JiBXException on unrecoverable error
     */
    void convertTime(long value, int nanos) throws JiBXException;
    
    /**
     * Write a qualified name value to the current text selection. The qualified name is presented as a pair consisting
     * of namespace URI and local name in order to allow flexible use.
     * 
     * @param ns namespace URI (empty string if no-namespace namespace)
     * @param name local name
     * @throws JiBXException on unrecoverable error
     */
    void convertQName(String ns, String name) throws JiBXException;
    
    /**
     * Write a <code>BigDecimal</code> value to the current text selection.
     * 
     * @param value (non-<code>null</code>)
     * @throws JiBXException on unrecoverable error
     */
    void convert(BigDecimal value) throws JiBXException;
    
    /**
     * Write a <code>BigInteger</code> value to the current text selection.
     * 
     * @param value (non-<code>null</code>)
     * @throws JiBXException on unrecoverable error
     */
    void convert(BigInteger value) throws JiBXException;
    
    /**
     * Write a text value as the content of the current element. This writes the corresponding end tag after writing the
     * value.
     * 
     * @param value content value
     * @throws JiBXException on unrecoverable error
     */
    void addText(String value) throws JiBXException;
    
    /**
     * Write end tag for current open element.
     * 
     * @throws JiBXException on unrecoverable error
     */
    void endTag() throws JiBXException;
    
    /**
     * Handle a missing required element value from the implicit namespace in the generated document.
     *
     * @param name
     * @throws JiBXException on unrecoverable error
     */
    void handleMissingElement(String name) throws JiBXException;
    
    /**
     * Handle a missing required element value in the generated document.
     *
     * @param ns
     * @param name
     * @throws JiBXException on unrecoverable error
     */
    void handleMissingElement(String ns, String name) throws JiBXException;
    
    /**
     * Handle a missing required attribute value from the implicit namespace in the generated document.
     *
     * @param name
     * @throws JiBXException on unrecoverable error
     */
    void handleMissingAttribute(String name) throws JiBXException;
    
    /**
     * Handle a missing required attribute value in the generated document.
     *
     * @param ns
     * @param name
     * @throws JiBXException on unrecoverable error
     */
    void handleMissingAttribute(String ns, String name) throws JiBXException;
    
    /**
     * Write XML declaration to document. This can only be called before any other methods in the interface are called.
     * 
     * @param version XML version text
     * @param encoding text for encoding attribute (unspecified if <code>null</code>)
     * @param standalone text for standalone attribute (unspecified if <code>null</code>)
     * @throws JiBXException on error writing to document
     */
    void writeXMLDecl(String version, String encoding, String standalone) throws JiBXException;
    
    /**
     * Write ordinary character data text content to document.
     * 
     * @param text content value text (must not be <code>null</code>)
     * @throws JiBXException on error writing to document
     */
    void writeTextContent(String text) throws JiBXException;
    
    /**
     * Write CDATA text to document.
     * 
     * @param text content value text (must not be <code>null</code>)
     * @throws JiBXException on error writing to document
     */
    void writeCData(String text) throws JiBXException;
    
    /**
     * Write comment to document.
     * 
     * @param text comment text (must not be <code>null</code>)
     * @throws JiBXException on error writing to document
     */
    void writeComment(String text) throws JiBXException;
    
    /**
     * Write entity reference to document.
     * 
     * @param name entity name (must not be <code>null</code>)
     * @throws JiBXException on error writing to document
     */
    void writeEntityRef(String name) throws JiBXException;
    
    /**
     * Write DOCTYPE declaration to document.
     * 
     * @param name root element name
     * @param sys system ID (<code>null</code> if none, must be non-<code>null</code> for ID to be used)
     * @param pub ID (<code>null</code> if none)
     * @param subset internal subset (<code>null</code> if none)
     * @throws JiBXException on error writing to document
     */
    void writeDocType(String name, String sys, String pub, String subset) throws JiBXException;
    
    /**
     * Write processing instruction to document.
     * 
     * @param target processing instruction target name (must not be <code>null</code>)
     * @param data processing instruction data (must not be <code>null</code>)
     * @throws JiBXException on error writing to document
     */
    void writePI(String target, String data) throws JiBXException;
    
    /**
     * Request output indent. The writer implementation should normally indent output as appropriate. This method can be
     * used to request indenting of output that might otherwise not be indented. The normal effect when used with a
     * text-oriented writer should be to output the appropriate line end sequence followed by the appropriate number of
     * indent characters for the current nesting level.
     * 
     * @throws JiBXException on error writing to document
     */
    void indent() throws JiBXException;
    
    /**
     * Flush document output. Writes any buffered data to the output medium. This does <b>not</b> flush the output
     * medium itself, only any internal buffering within the writer.
     * 
     * @throws JiBXException on error writing to document
     */
    void flush() throws JiBXException;
    
    /**
     * Close document output. Completes writing of document output, including flushing and closing the output medium.
     * 
     * @throws JiBXException on error writing to document
     */
    void close() throws JiBXException;
    
    /**
     * Reset to initial state for reuse. The context is serially reusable, as long as this method is called to clear any
     * retained state information between uses. It is automatically called when output is set.
     */
    void reset();
    
    /**
     * Get namespace URIs for mapping. This gets the full ordered array of namespaces known in the binding used for this
     * marshalling, where the index number of each namespace URI is the namespace index used to lookup the prefix when
     * marshalling a name in that namespace. The returned array must not be modified.
     * 
     * @return array of namespaces
     */
    String[] getNamespaces();
    
    /**
     * Get URI for namespace.
     * 
     * @param index namespace URI index number
     * @return namespace URI text, or <code>null</code> if the namespace index is invalid
     */
    String getNamespaceUri(int index);
    
    /**
     * Get current prefix defined for namespace.
     * 
     * @param index namespace URI index number
     * @return current prefix text, or <code>null</code> if the namespace is not currently mapped
     */
    String getNamespacePrefix(int index);
    
    /**
     * Get index of namespace mapped to prefix. This can be an expensive operation with time proportional to the number
     * of namespaces defined, so it should be used with care.
     * 
     * @param prefix text to match (non-<code>null</code>, use "" for default prefix)
     * @return index namespace URI index number mapped to prefix
     */
    int getPrefixIndex(String prefix);
    
    /**
     * Append extension namespace URIs to those in mapping.
     * 
     * @param uris namespace URIs to extend those in mapping
     */
    void pushExtensionNamespaces(String[] uris);
    
    /**
     * Remove extension namespace URIs. This removes the last set of extension namespaces pushed using
     * {@link #pushExtensionNamespaces}.
     */
    void popExtensionNamespaces();
    
    /**
     * Get extension namespace URIs added to those in mapping. This gets the current set of extension definitions. The
     * returned arrays must not be modified.
     * 
     * @return array of arrays of extension namespaces (<code>null</code> if none)
     */
    String[][] getExtensionNamespaces();
    
    /**
     * Open the specified namespaces for use. This method is normally only called internally, when namespace
     * declarations are actually written to output. It is exposed as part of this interface to allow for special
     * circumstances where namespaces are being written outside the usual processing. The namespaces will remain open
     * for use until the current element is closed.
     * 
     * @param nums array of namespace indexes defined by this element (must be constant, reference is kept until
     * namespaces are closed)
     * @param prefs array of namespace prefixes mapped by this element (no <code>null</code> values, use "" for
     * default namespace declaration)
     * @return array of indexes for namespaces not previously active (the ones actually needing to be declared, in the
     * case of text output)
     * @throws JiBXException on error writing to document
     */
    int[] openNamespaces(int[] nums, String[] prefs) throws JiBXException;
}
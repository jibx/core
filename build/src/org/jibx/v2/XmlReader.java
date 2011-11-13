/*
 * Copyright (c) 2005-2007, Dennis M. Sosnoski. All rights reserved.
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
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * XML reader interface used for input to the unmarshalling code. This interface allows easy substitution of different
 * parsers or other input sources.
 * 
 * @author Dennis M. Sosnoski
 */
public interface XmlReader
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
     * Get the current validation context for this reader. The validation context is used both for tracking problems,
     * and to determine the appropriate handling when a problem occurs.
     * 
     * @return context
     */
    ValidationContext getValidationContext();
    
    /**
     * Push a validation context on this reader. The supplied validation context is popped after processing the end tag
     * for the current element.
     * 
     * @param vctx context
     */
    void pushValidationContext(ValidationContext vctx);
    
    /**
     * Get the unmarshalling context associated with this reader. The unmarshalling context tracks higher-level
     * information about the conversion of XML into a Java object structure.
     * 
     * @return context
     */
    UnmarshallingContext getBindingContext();
    
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
     * Advance to next binding component of input document. This is a higher-level operation than {@link #nextToken()},
     * which consolidates text content and ignores parse events for components such as comments and PIs.
     * 
     * @return parse event type code
     * @throws JiBXException if error reading or parsing document
     */
    int next() throws JiBXException;
    
    /**
     * Gets the current parse event type, without changing the current parse state.
     * 
     * @return parse event type code
     * @throws JiBXException if error parsing document
     */
    int getEventType() throws JiBXException;
    
    /**
     * Get element name from the current start or end tag.
     * 
     * @return local name if namespace handling enabled, full name if namespace handling disabled
     * @throws IllegalStateException if not at a start or end tag (optional)
     */
    String getName();
    
    /**
     * Get element namespace from the current start or end tag.
     * 
     * @return namespace URI if namespace handling enabled and element is in a namespace, empty string otherwise
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
     * Set the implicit namespace used for elements unless otherwise specified.
     *
     * @param ns namespace URI for element (may be the empty string for the no-namespace namespace)
     * @return prior implicit namespace
     */
    String setImplicitNamespace(String ns);
    
    /**
     * Advance to a start or end tag, and verify it is the named start tag in the implicit namespace.
     *
     * @param name element name
     * @return <code>true</code> if tag found, <code>false</code> if not (recoverable error case)
     * @throws JiBXException on unrecoverable error
     */
    boolean requireStartTag(String name) throws JiBXException;
    
    /**
     * Advance to a start or end tag, and verify it is the named start tag.
     *
     * @param ns namespace URI for element (may be the empty string for the no-namespace namespace)
     * @param name element name
     * @return <code>true</code> if tag found, <code>false</code> if not (exception not thrown)
     * @throws JiBXException on unrecoverable error
     */
    boolean requireStartTag(String ns, String name) throws JiBXException;
    
    /**
     * Advance to a start or end tag, and check if it is the named start tag in the implicit namespace.
     *
     * @param name element name
     * @return <code>true</code> if match, <code>false</code> if not
     * @throws JiBXException on unrecoverable error
     */
    boolean checkStartTag(String name) throws JiBXException;
    
    /**
     * Advance to a start or end tag, and verify it is the named start tag.
     *
     * @param ns namespace URI for element (may be the empty string for the no-namespace namespace)
     * @param name element name
     * @return <code>true</code> if match, <code>false</code> if not
     * @throws JiBXException on unrecoverable error
     */
    boolean checkStartTag(String ns, String name) throws JiBXException;
    
    /**
     * Advance to the next start or end tag, and verify it is the close tag for the current open element.
     * 
     * @throws JiBXException on unrecoverable error
     */
    void requireEndTag() throws JiBXException;
    
    /**
     * Get current element text. This is only valid with an open start tag, and reads past the text content of the
     * element, leaving the reader positioned on the next element start or end tag following the text.
     * 
     * @return text for current element (may be <code>null</code>, in the case of a recoverable error)
     * @throws IllegalStateException if not at a start tag
     * @throws JiBXException on unrecoverable error
     */
    String getElementText() throws JiBXException;
    
    /**
     * Get current text. When positioned on a TEXT event this returns the actual text; for CDSECT it returns the text
     * inside the CDATA section; for COMMENT, DOCDECL, or PROCESSING_INSTRUCTION it returns the text inside the
     * structure.
     * 
     * @return text for current event (may be <code>null</code>, in the case of a recoverable error)
     * @throws JiBXException on unrecoverable error
     */
    String getText() throws JiBXException;
    
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
     * @return local name if namespace handling enabled, full name if namespace handling disabled
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getAttributeName(int index);
    
    /**
     * Get an attribute namespace from the current start tag.
     * 
     * @param index attribute index
     * @return namespace URI if namespace handling enabled and attribute is in a namespace, empty string otherwise
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
     * Get the index of a no-namespace attribute from the current start tag.
     * 
     * @param name attribute name
     * @return attribute index (<code>-1</code> if not found)
     * @throws IllegalStateException if not at a start tag
     */
    int getAttributeIndex(String name);
    
    /**
     * Get the index of an attribute from the current start tag.
     * 
     * @param ns namespace URI for attribute (may be the empty string for the no-namespace namespace)
     * @param name attribute name
     * @return attribute index (<code>-1</code> if not found)
     * @throws IllegalStateException if not at a start tag
     */
    int getAttributeIndex(String ns, String name);
    
    /**
     * Get a required text attribute value from the current start tag.
     * 
     * @param index attribute index (error if negative)
     * @return value text (may be <code>null</code>, in the case of a recoverable error)
     * @throws IllegalStateException if not at a start tag or invalid index
     * @throws JiBXException on unrecoverable error
     */
    String getAttributeText(int index) throws JiBXException;
    
    /**
     * Read a required text attribute value from the current start tag with whitespace collapsed.
     * 
     * @param index attribute index (error if negative)
     * @return value text (may be <code>null</code>, in the case of a recoverable error)
     * @throws IllegalStateException if not at a start tag or invalid index
     * @throws JiBXException on unrecoverable error
     */
    String getAttributeCollapsed(int index) throws JiBXException;
    
    /**
     * Read an optional text attribute value from the current start tag.
     * 
     * @param name attribute name
     * @return value text, <code>null</code> if attribute not present
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getOptionalAttributeText(String name);
    
    /**
     * Read an optional text attribute value from the current start tag.
     * 
     * @param ns namespace URI for attribute (may be the empty string for the no-namespace namespace)
     * @param name attribute name
     * @return value text, <code>null</code> if attribute not present
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getOptionalAttributeText(String ns, String name);
    
    /**
     * Read a required text attribute value from the current start tag.
     * 
     * @param name attribute name
     * @return value text, <code>null</code> if attribute not present and recoverable error
     * @throws JiBXException if attribute not present and unrecoverable error
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getRequiredAttributeText(String name) throws JiBXException;
    
    /**
     * Read a required text attribute value from the current start tag.
     * 
     * @param ns namespace URI for attribute (may be the empty string for the no-namespace namespace)
     * @param name attribute name
     * @return value text, <code>null</code> if attribute not present and recoverable error
     * @throws JiBXException if attribute not present and unrecoverable error
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String getRequiredAttributeText(String ns, String name) throws JiBXException;
    
    /**
     * Select the current text content for conversion.
     * 
     * @throws JiBXException on unrecoverable error
     */
    void selectText() throws JiBXException;
    
    /**
     * Select an attribute value from the current start tag as text for conversion.
     * 
     * @param index attribute index (error if negative)
     * @throws IllegalStateException if not at a start tag or invalid index
     * @throws JiBXException on unrecoverable error
     */
    void selectAttribute(int index) throws JiBXException;
    
    /**
     * Select an optional no-namespace attribute value from the current start tag as text for conversion.
     * 
     * @param name attribute name
     * @return <code>true</code> if attribute present, <code>false</code> if not
     * @throws IllegalStateException if not at a start tag
     */
    boolean selectOptionalAttribute(String name);
    
    /**
     * Select a required no-namespace attribute value from the current start tag as text for conversion.
     * 
     * @param name attribute name
     * @throws IllegalStateException if not at a start tag
     * @throws JiBXException on unrecoverable error
     */
    void selectRequiredAttribute(String name) throws JiBXException;
    
    /**
     * Select an optional attribute value from the current start tag as text for conversion.
     * 
     * @param ns namespace URI for attribute (may be the empty string for the no-namespace namespace)
     * @param name attribute name
     * @return <code>true</code> if attribute present, <code>false</code> if not
     * @throws IllegalStateException if not at a start tag
     */
    boolean selectOptionalAttribute(String ns, String name);
    
    /**
     * Select a required attribute value from the current start tag as text for conversion.
     * 
     * @param ns namespace URI for attribute (may be the empty string for the no-namespace namespace)
     * @param name attribute name
     * @throws IllegalStateException if not at a start tag
     * @throws JiBXException on unrecoverable error
     */
    void selectRequiredAttribute(String ns, String name) throws JiBXException;
    
    /**
     * Convert a <code>String</code> value from the current source selection. This is an empty conversion, which always
     * just returns the text.
     *
     * @return text (<code>null</code> if selection missing)
     * @throws JiBXException if unrecoverable conversion error
     */
    String convertString() throws JiBXException;
    
    /**
     * Convert an <code>int</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>0</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    int convertIntPrimitive() throws JiBXException;
    
    /**
     * Convert an <code>Integer</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>null</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    Integer convertInteger() throws JiBXException;
    
    /**
     * Convert a <code>long</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>0</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    long convertLongPrimitive() throws JiBXException;
    
    /**
     * Convert a <code>Long</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>null</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    Long convertLong() throws JiBXException;
    
    /**
     * Convert a <code>float</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>0</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    long convertFloatPrimitive() throws JiBXException;
    
    /**
     * Convert a <code>Float</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>null</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    Long convertFloat() throws JiBXException;
    
    /**
     * Convert a <code>boolean</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>false</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    long convertBooleanPrimitive() throws JiBXException;
    
    /**
     * Convert a <code>Boolean</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>null</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    Long convertBoolean() throws JiBXException;
    
    /**
     * Convert a <code>byte[]</code> value from the current source selection using base64Binary encoding. This always
     * uses whitespace collapsed processing.
     * 
     * @return converted value (<code>null</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    byte[] convertBase64() throws JiBXException;
    
    /**
     * Convert a <code>Date</code> value from the current source selection. This always uses whitespace collapsed
     * processing.
     * 
     * @return converted value (<code>null</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    Date convertDateTime() throws JiBXException;
    
    /**
     * Convert a <code>BigDecimal</code> value from the current source selection. This always uses whitespace
     * collapsed processing.
     * 
     * @return converted value (<code>null</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    BigDecimal convertBigDecimal() throws JiBXException;
    
    /**
     * Convert a <code>BigInteger</code> value from the current source selection. This always uses whitespace
     * collapsed processing.
     * 
     * @return converted value (<code>null</code> if selection missing or in error)
     * @throws JiBXException if unrecoverable conversion error
     */
    BigInteger convertBigInteger() throws JiBXException;
    
    /**
     * Read current element text. This is only valid with an open start tag, and reads past the corresponding end tag
     * after reading the value.
     * 
     * @return text for current event
     * @throws IllegalStateException if not at a start tag or invalid index
     */
    String readText();
    
    /**
     * Creat instance of class for element name. This implements substitution group handling, by checking the current
     * element start tag name against the expected element name, and if they're not the same finding the appropriate
     * class based on the substitution group rooted on the expected element name (which must be a global element name).
     *
     * @param root global root element name, including namespace URI, in "lname{uri}" form
     * @param rdr reader
     * @param inst supplied instance of root element class or subclass (<code>null</code> if none)
     * @return instance of appropriate class to use for unmarshalling (may be the same as the provided instance)
     */
    Object createElementInstance(String root, XmlReader rdr, Object inst);
    
    /**
     * Validate instance of class for type name. This implements type substitution handling, by checking for an override
     * xsi:type specification on the current element start tag, and if the type is different from the default finding
     * the appropriate class.
     *
     * @param dflt global default complexType name, including namespace URI, in "lname{uri}" form
     * @param rdr reader
     * @param inst supplied instance of default type class or subclass (<code>null</code> if none)
     * @return instance of appropriate class to use for unmarshalling (may be the same as the provided instance)
     */
    Object createTypeInstance(String dflt, XmlReader rdr, Object inst);
    
    /**
     * Get current element nesting depth. The returned depth always includes the current start or end tag (if positioned
     * on a start or end tag).
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
     * @return line number from source document, <code>-1</code> if line number information not available
     */
    int getLineNumber();
    
    /**
     * Get current source column number.
     * 
     * @return column number from source document, <code>-1</code> if column number information not available
     */
    int getColumnNumber();
    
    /**
     * Get namespace URI associated with prefix.
     * 
     * @param prefix to be found
     * @return associated URI (<code>null</code> if prefix not defined)
     */
    String getNamespace(String prefix);
    
    /**
     * Return the input encoding, if known. This is only valid after parsing of a document has been started.
     * 
     * @return input encoding (<code>null</code> if unknown)
     */
    String getInputEncoding();
    
    /**
     * Return namespace processing flag.
     * 
     * @return namespace processing flag (<code>true</code> if namespaces are processed by reader, <code>false</code>
     * if not)
     */
    boolean isNamespaceAware();
}
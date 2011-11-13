/*
Copyright (c) 2002-2008, Dennis M. Sosnoski.
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

import java.io.InputStream;
import java.io.Reader;

/**
 * User interface for deserializer from XML. This provides methods used to set
 * up and control the marshalling process, as well as access to the
 * unmarshalling object stack while unmarshalling.
 *
 * @author Dennis M. Sosnoski
 */
public interface IUnmarshallingContext {

    /**
     * Set document to be parsed from stream.
     *
     * @param ins stream supplying document data
     * @param enc document input encoding, or <code>null</code> if to be
     * determined by parser
     * @throws JiBXException if error creating parser
     */
    void setDocument(InputStream ins, String enc) throws JiBXException;

    /**
     * Set document to be parsed from reader.
     *
     * @param rdr reader supplying document data
     * @throws JiBXException if error creating parser
     */
    void setDocument(Reader rdr) throws JiBXException;

    /**
     * Set named document to be parsed from stream.
     *
     * @param ins stream supplying document data
     * @param name document name
     * @param enc document input encoding, or <code>null</code> if to be
     * determined by parser
     * @throws JiBXException if error creating parser
     */
    void setDocument(InputStream ins, String name, String enc)
        throws JiBXException;

    /**
     * Set named document to be parsed from reader.
     *
     * @param rdr reader supplying document data
     * @param name document name
     * @throws JiBXException if error creating parser
     */
    void setDocument(Reader rdr, String name) throws JiBXException;

    /**
     * Reset unmarshalling information. This releases all references to
     * unmarshalled objects and prepares the context for potential reuse.
     * It is automatically called when input is set.
     */
    void reset();

    /**
     * Unmarshal the current element. If not currently positioned at a start
     * or end tag this first advances the parse to the next start or end tag.
     * There must be an unmarshalling defined for the current element, and this
     * unmarshalling is used to build an object from that element.
     *
     * @return unmarshalled object from element
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    Object unmarshalElement() throws JiBXException;

    /**
     * Unmarshal document from stream to object. The effect of this is the same
     * as if {@link #setDocument} were called, followed by {@link
     * #unmarshalElement}
     *
     * @param ins stream supplying document data
     * @param enc document input encoding, or <code>null</code> if to be
     * determined by parser
     * @return unmarshalled object
     * @throws JiBXException if error creating parser
     */
    Object unmarshalDocument(InputStream ins, String enc) throws JiBXException;

    /**
     * Unmarshal document from reader to object. The effect of this is the same
     * as if {@link #setDocument} were called, followed by {@link
     * #unmarshalElement}
     *
     * @param rdr reader supplying document data
     * @return unmarshalled object
     * @throws JiBXException if error creating parser
     */
    Object unmarshalDocument(Reader rdr) throws JiBXException;

    /**
     * Unmarshal named document from stream to object. The effect of this is the
     * same as if {@link #setDocument} were called, followed by {@link
     * #unmarshalElement}
     *
     * @param ins stream supplying document data
     * @param name document name
     * @param enc document input encoding, or <code>null</code> if to be
     * determined by parser
     * @return unmarshalled object
     * @throws JiBXException if error creating parser
     */
    Object unmarshalDocument(InputStream ins, String name, String enc)
        throws JiBXException;

    /**
     * Unmarshal named document from reader to object. The effect of this is the
     * same as if {@link #setDocument} were called, followed by {@link
     * #unmarshalElement}
     *
     * @param rdr reader supplying document data
     * @param name document name
     * @return unmarshalled object
     * @throws JiBXException if error creating parser
     */
    Object unmarshalDocument(Reader rdr, String name) throws JiBXException;

    /**
     * Return the supplied document name.
     *
     * @return supplied document name (<code>null</code> if none)
     */
    String getDocumentName();

    /**
     * Check if next tag is start of element. If not currently positioned at a
     * start or end tag this first advances the parse to the next start or end
     * tag.
     *
     * @param ns namespace URI for expected element (may be <code>null</code>
     * or the empty string for the empty namespace)
     * @param name element name expected
     * @return <code>true</code> if at start of element with supplied name,
     * <code>false</code> if not
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    boolean isAt(String ns, String name) throws JiBXException;

    /**
     * Check if next tag is a start tag. If not currently positioned at a
     * start or end tag this first advances the parse to the next start or
     * end tag.
     *
     * @return <code>true</code> if at start of element, <code>false</code> if
     * at end
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    boolean isStart() throws JiBXException;

    /**
     * Check if next tag is an end tag. If not currently positioned at a
     * start or end tag this first advances the parse to the next start or
     * end tag.
     *
     * @return <code>true</code> if at end of element, <code>false</code> if
     * at start
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    boolean isEnd() throws JiBXException;

    /**
     * Find the unmarshaller for a particular class in the current context.
     *
     * @param mapname unmarshaller mapping name (generally the class name to be
     * handled, or abstract mapping type name)
     * @return unmarshalling handler for class
     * @throws JiBXException if unable to create unmarshaller
     */
    IUnmarshaller getUnmarshaller(String mapname) throws JiBXException;
    
    /**
     * Set a user context object. This context object is not used directly by
     * JiBX, but can be accessed by all types of user extension methods. The
     * context object is automatically cleared by the {@link #reset()} method,
     * so to make use of this you need to first call the appropriate version of
     * the <code>setDocument()</code> method, then this method, and finally the
     * {@link #unmarshalElement} method.
     * 
     * @param obj user context object, or <code>null</code> if clearing existing
     * context object
     * @see #getUserContext()
     */
    void setUserContext(Object obj);
    
    /**
     * Get the user context object.
     * 
     * @return user context object, or <code>null</code> if no context object
     * set
     * @see #setUserContext(Object)
     */
    Object getUserContext();

    /**
     * Push created object to unmarshalling stack. This must be called before
     * beginning the unmarshalling of the object. It is only called for objects
     * with structure, not for those converted directly to and from text.
     *
     * @param obj object being unmarshalled
     */
    void pushObject(Object obj);

    /**
     * Pop unmarshalled object from stack.
     *
     * @throws JiBXException if stack empty
     */
    void popObject() throws JiBXException;
    
    /**
     * Get current unmarshalling object stack depth. This allows tracking
     * nested calls to unmarshal one object while in the process of
     * unmarshalling another object. The bottom item on the stack is always the
     * root object being unmarshalled.
     *
     * @return number of objects in unmarshalling stack
     */
    int getStackDepth();
    
    /**
     * Get object from unmarshalling stack. This stack allows tracking nested
     * calls to unmarshal one object while in the process of unmarshalling
     * another object. The bottom item on the stack is always the root object
     * being unmarshalled.
     *
     * @param depth object depth in stack to be retrieved (must be in the range
     * of zero to the current depth minus one).
     * @return object from unmarshalling stack
     */
    Object getStackObject(int depth);
    
    /**
     * Get top object on unmarshalling stack. This is safe to call even when no
     * objects are on the stack.
     *
     * @return object from unmarshalling stack, or <code>null</code> if none
     */
    Object getStackTop();
}
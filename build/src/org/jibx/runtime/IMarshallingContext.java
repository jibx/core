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

import java.io.OutputStream;
import java.io.Writer;

/**
 * User interface for serializer to XML. This provides methods used to set up
 * and control the marshalling process, as well as access to the marshalling
 * object stack while marshalling.
 *
 * @author Dennis M. Sosnoski
 */
public interface IMarshallingContext
{
    /**
     * Set output stream with encoding and escaper. This forces handling of the
     * output stream to use the Java character encoding support with the
     * supplied escaper.
     *
     * @param outs stream for document data output
     * @param enc document output encoding, or <code>null</code> uses UTF-8
     * default
     * @param esc escaper for writing characters to stream
     * @throws JiBXException if error setting output
     */
    void setOutput(OutputStream outs, String enc, ICharacterEscaper esc)
        throws JiBXException;
    
    /**
     * Set output stream and encoding.
     *
     * @param outs stream for document data output
     * @param enc document output encoding, or <code>null</code> uses UTF-8
     * default
     * @throws JiBXException if error setting output
     */
    void setOutput(OutputStream outs, String enc) throws JiBXException;
    
    /**
     * Set output writer and escaper.
     *
     * @param outw writer for document data output
     * @param esc escaper for writing characters
     */
    void setOutput(Writer outw, ICharacterEscaper esc);
    
    /**
     * Set output writer. This assumes the standard UTF-8 encoding.
     *
     * @param outw writer for document data output
     */
    void setOutput(Writer outw);

    /**
     * Get the writer being used for output.
     *
     * @return XML writer used for output
     */
    IXMLWriter getXmlWriter();

    /**
     * Set the writer being used for output.
     *
     * @param xwrite XML writer used for output
     */
    void setXmlWriter(IXMLWriter xwrite);
    
    /**
     * Get current nesting indent spaces. This returns the number of spaces used
     * to show indenting, if used.
     *
     * @return number of spaces indented per level, or negative if indentation
     * disabled
     */
    int getIndent();
    
    /**
     * Set nesting indent spaces. This is advisory only, and implementations of
     * this interface are free to ignore it. The intent is to indicate that the
     * generated output should use indenting to illustrate element nesting.
     *
     * @param count number of spaces to indent per level, or disable
     * indentation if negative
     */
    void setIndent(int count);
    
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
    void setIndent(int count, String newline, char indent);
        
    /**
     * Reset to initial state for reuse. The context is serially reusable,
     * as long as this method is called to clear any retained state information
     * between uses. It is automatically called when output is set.
     */
    void reset();
    
    /**
     * Start document, writing the XML declaration. This can only be validly
     * called immediately following one of the set output methods; otherwise the
     * output document will be corrupt.
     *
     * @param enc document encoding, <code>null</code> uses UTF-8 default
     * @param alone standalone document flag, <code>null</code> if not
     * specified
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    void startDocument(String enc, Boolean alone) throws JiBXException;
    
    /**
     * Start document with output stream and encoding. The effect is the same
     * as from first setting the output stream and encoding, then making the
     * call to start document.
     *
     * @param enc document encoding, <code>null</code> uses UTF-8 default
     * @param alone standalone document flag, <code>null</code> if not
     * specified
     * @param outs stream for document data output
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    void startDocument(String enc, Boolean alone, OutputStream outs)
        throws JiBXException;
    
    /**
     * Start document with writer. The effect is the same as from first
     * setting the writer, then making the call to start document.
     *
     * @param enc document encoding, <code>null</code> uses UTF-8 default
     * @param alone standalone document flag, <code>null</code> if not
     * specified
     * @param outw writer for document data output
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    void startDocument(String enc, Boolean alone, Writer outw)
        throws JiBXException;
    
    /**
     * End document. Finishes all output and closes the document. Note that if
     * this is called with an imcomplete marshalling the result will not be
     * well-formed XML.
     *
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    void endDocument() throws JiBXException;
    
    /**
     * Marshal document from root object without XML declaration. This can only
     * be validly called immediately following one of the set output methods;
     * otherwise the output document will be corrupt. The effect of this method
     * is the same as the sequence of a call to marshal the root object using
     * this context followed by a call to {@link #endDocument}.
     *
     * @param root object at root of structure to be marshalled, which must have
     * a top-level mapping in the binding
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    void marshalDocument(Object root) throws JiBXException;
    
    /**
     * Marshal document from root object. This can only be validly called
     * immediately following one of the set output methods; otherwise the output
     * document will be corrupt. The effect of this method is the same as the
     * sequence of a call to {@link #startDocument(String, Boolean)}, a call to
     * marshal the root object using this context, and finally a call to
     * {@link #endDocument}.
     *
     * @param root object at root of structure to be marshalled, which must have
     * a top-level mapping in the binding
     * @param enc document encoding, <code>null</code> uses UTF-8 default
     * @param alone standalone document flag, <code>null</code> if not
     * specified
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    void marshalDocument(Object root, String enc, Boolean alone)
        throws JiBXException;
    
    /**
     * Marshal document from root object to output stream with encoding. The
     * effect of this method is the same as the sequence of a call to {@link
     * #startDocument(String, Boolean)}, a call to marshal the root object using
     * this context, and finally a call to {@link #endDocument}.
     *
     * @param root object at root of structure to be marshalled, which must have
     * a top-level mapping in the binding
     * @param enc document encoding, <code>null</code> uses UTF-8 default
     * @param alone standalone document flag, <code>null</code> if not
     * specified
     * @param outs stream for document data output
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    void marshalDocument(Object root, String enc, Boolean alone,
        OutputStream outs) throws JiBXException;
    
    /**
     * Marshal document from root object to writer. The effect of this method
     * is the same as the sequence of a call to {@link #startDocument(String,
     * Boolean)}, a call to marshal the root object using this context, and
     * finally a call to {@link #endDocument}.
     *
     * @param root object at root of structure to be marshalled, which must have
     * a top-level mapping in the binding
     * @param enc document encoding, <code>null</code> uses UTF-8 default
     * @param alone standalone document flag, <code>null</code> if not
     * specified
     * @param outw writer for document data output
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    void marshalDocument(Object root, String enc, Boolean alone,
        Writer outw) throws JiBXException;
    
    /**
     * Set a user context object. This context object is not used directly by
     * JiBX, but can be accessed by all types of user extension methods. The
     * context object is automatically cleared by the {@link #reset()} method,
     * so to make use of this you need to first call the appropriate version of
     * the <code>setOutput()</code> method, then this method, and finally one of
     * the <code>marshalDocument</code> methods which uses the previously-set
     * output (not the ones which take a stream or writer as parameter, since
     * they call <code>setOutput()</code> themselves).
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
     * Push created object to marshalling stack. This must be called before
     * beginning the marshalling of the object. It is only called for objects
     * with structure, not for those converted directly to and from text.
     *
     * @param obj object being marshalled
     */
    void pushObject(Object obj);

    /**
     * Pop marshalled object from stack.
     *
     * @throws JiBXException if no object on stack
     */
    void popObject() throws JiBXException;
    
    /**
     * Get current marshalling object stack depth. This allows tracking
     * nested calls to marshal one object while in the process of marshalling
     * another object. The bottom item on the stack is always the root object
     * of the marshalling.
     *
     * @return number of objects in marshalling stack
     */
    int getStackDepth();
    
    /**
     * Get object from marshalling stack. This stack allows tracking nested
     * calls to marshal one object while in the process of marshalling
     * another object. The bottom item on the stack is always the root object
     * of the marshalling.
     *
     * @param depth object depth in stack to be retrieved (must be in the range
     * of zero to the current depth minus one).
     * @return object from marshalling stack
     */
    Object getStackObject(int depth);
    
    /**
     * Get top object on marshalling stack. This is safe to call even when no
     * objects are on the stack.
     *
     * @return object from marshalling stack, or <code>null</code> if none
     */
    Object getStackTop();
    
    /**
     * Find the marshaller for a particular class in the current context.
     *
     * @param mapname marshaller mapping name (generally the class name to be
     * handled, or abstract mapping type name)
     * @return marshalling handler for class
     * @throws JiBXException on any error (possibly wrapping other exception)
     */
    IMarshaller getMarshaller(String mapname) throws JiBXException;
    
    /**
     * Use namespace indexes from a separate binding, as identified by that
     * binding's factory class name. The target binding must be a precompiled
     * base binding of the binding used to create this marshalling context,
     * either directly or by way of some other precompiled base binding(s).
     *
     * @param factname binding factory class name for binding defining
     * namespaces
     */
    void pushNamespaces(String factname);
    
    /**
     * End use of namespace indexes from a separate binding. This will undo the
     * effect of the most-recent call to {@link #pushNamespaces(String)},
     * restoring whatever namespace usage was in effect prior to that call.
     */
    void popNamespaces();
}
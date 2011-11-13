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

package org.jibx.runtime.impl;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jibx.runtime.IExtensibleWriter;
import org.jibx.runtime.IXMLWriter;

/**
 * Writer generating StAX parse event stream output.
 * 
 * @author Dennis M. Sosnoski
 */
public class StAXWriter extends XMLWriterNamespaceBase implements IExtensibleWriter
{
    /** Target for parse event stream. */
    private XMLStreamWriter m_writer;
    
    /**
     * Constructor.
     *
     * @param uris ordered array of URIs for namespaces used in document (must
     * be constant; the value in position 0 must always be the empty string "",
     * and the value in position 1 must always be the XML namespace
     * "http://www.w3.org/XML/1998/namespace")
     */
    public StAXWriter(String[] uris) {
        super(uris);
    }
    
    /**
     * Constructor with writer supplied.
     *
     * @param uris ordered array of URIs for namespaces used in document (must
     * be constant; the value in position 0 must always be the empty string "",
     * and the value in position 1 must always be the XML namespace
     * "http://www.w3.org/XML/1998/namespace")
     * @param wrtr StAX writer for parse event output
     */
    public StAXWriter(String[] uris, XMLStreamWriter wrtr) {
        this(uris);
        m_writer = wrtr;
    }
    
    /**
     * Copy constructor. This initializes the writer and extension namespace
     * information from an existing instance.
     *
     * @param base existing instance
     * @param uris ordered array of URIs for namespaces used in document
     */
    public StAXWriter(StAXWriter base, String[] uris) {
        super(base, uris);
        m_writer = base.m_writer;
    }
    
    /**
     * Initialize writer.
     */
    public void init() {
    }
    
    /**
     * Set StAX writer.
     * 
     * @param wrtr StAX writer for parse event output
     */
    public void setWriter(XMLStreamWriter wrtr) {
        m_writer = wrtr;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.XMLWriterNamespaceBase#defineNamespace(int, java.lang.String)
     */
    protected void defineNamespace(int index, String prefix)
        throws IOException {
/*        try {
            
            // inform writer of new namespace usage
            String uri = getNamespaceUri(index);
            if (prefix.length() == 0) {
                m_writer.setDefaultNamespace(uri);
            } else {
                m_writer.setPrefix(prefix, uri);
            }
            
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }   */
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.XMLWriterNamespaceBase#undefineNamespace(int)
     */
    protected void undefineNamespace(int index) {}

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#setIndentSpaces(int, java.lang.String, char)
     */
    public void setIndentSpaces(int count, String newline, char indent) {}

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#writeXMLDecl(java.lang.String, java.lang.String, java.lang.String)
     */
    public void writeXMLDecl(String version, String encoding, String standalone) throws IOException {
        try {
            m_writer.writeStartDocument(encoding, version);
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#startTagOpen(int, java.lang.String)
     */
    public void startTagOpen(int index, String name) throws IOException {
        try {
            
            // write start element, without or with namespace
            if (index == 0) {
                m_writer.writeStartElement(name);
            } else {
                m_writer.writeStartElement(getNamespacePrefix(index), name,
                    getNamespaceUri(index));
            }
            
            // increment nesting for any possible content
            incrementNesting();
            
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#startTagNamespaces(int, java.lang.String, int[], java.lang.String[])
     */
    public void startTagNamespaces(int index, String name, int[] nums,
        String[] prefs) throws IOException {
        try {
            
            // find the namespaces actually being declared
            int[] deltas = openNamespaces(nums, prefs);
            
            // open the start tag
            startTagOpen(index, name);
            
            // write the namespace declarations
            for (int i = 0; i < deltas.length; i++) {
                int slot = deltas[i];
                String prefix = internalNamespacePrefix(slot);
                String uri = internalNamespaceUri(slot);
                if (prefix.length() > 0) {
                    m_writer.writeNamespace(prefix, uri);
                } else {
                    m_writer.writeDefaultNamespace(uri);
                }
            }
            
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#addAttribute(int, java.lang.String, java.lang.String)
     */
    public void addAttribute(int index, String name, String value)
        throws IOException {
        try {
            if (index == 0) {
                m_writer.writeAttribute(name, value);
            } else {
                m_writer.writeAttribute(getNamespacePrefix(index),
                    getNamespaceUri(index), name, value);
            }
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#closeStartTag()
     */
    public void closeStartTag() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#closeEmptyTag()
     */
    public void closeEmptyTag() throws IOException {
        try {
            m_writer.writeEndElement();
            decrementNesting();
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#startTagClosed(int, java.lang.String)
     */
    public void startTagClosed(int index, String name) throws IOException {
        startTagOpen(index, name);
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#endTag(int, java.lang.String)
     */
    public void endTag(int index, String name) throws IOException {
        // not valid approach in general, but okay for StAX case
        closeEmptyTag();
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#writeTextContent(java.lang.String)
     */
    public void writeTextContent(String text) throws IOException {
        try {
            m_writer.writeCharacters(text);
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#writeCData(java.lang.String)
     */
    public void writeCData(String text) throws IOException {
        try {
            m_writer.writeCData(text);
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#writeComment(java.lang.String)
     */
    public void writeComment(String text) throws IOException {
        try {
            m_writer.writeComment(text);
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#writeEntityRef(java.lang.String)
     */
    public void writeEntityRef(String name) throws IOException {
        try {
            m_writer.writeEntityRef(name);
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#writeDocType(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void writeDocType(String name, String sys, String pub, String subset)
        throws IOException {
        try {
            StringBuffer buff = new StringBuffer();
            buff.append("<!DOCTYPE ");
            buff.append(name);
            buff.append(' ');
            if (sys != null) {
                if (pub == null) {
                    buff.append("SYSTEM \"");
                    buff.append(sys);
                } else {
                    buff.append("PUBLIC \"");
                    buff.append(pub);
                    buff.append("\" \"");
                    buff.append(sys);
                }
                buff.append('"');
            }
            if (subset != null) {
                buff.append('[');
                buff.append(subset);
                buff.append(']');
            }
            buff.append('>');
            m_writer.writeDTD(buff.toString());
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#writePI(java.lang.String, java.lang.String)
     */
    public void writePI(String target, String data) throws IOException {
        try {
            m_writer.writeProcessingInstruction(target, data);
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to stream: " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#indent()
     */
    public void indent() throws IOException {}

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#flush()
     */
    public void flush() throws IOException {
        // internal flush only, do not pass through to writer
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IXMLWriter#close()
     */
    public void close() throws IOException {
        try {
            m_writer.close();
        } catch (XMLStreamException e) {
            throw new IOException("Error closing stream: " + e.getMessage());
        }
    }
    
    /**
     * Create a child writer instance to be used for a separate binding. The
     * child writer inherits the output handling from this writer, while using
     * the supplied namespace URIs.
     * 
     * @param uris ordered array of URIs for namespaces used in document
     * (see {@link #StAXWriter(String[])})
     * @return child writer
     */
    public IXMLWriter createChildWriter(String[] uris) {
        return new StAXWriter(this, uris);
    }
}
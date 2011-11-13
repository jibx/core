/*
Copyright (c) 2005-2008, Dennis M. Sosnoski.
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
import java.io.InputStream;
import java.io.Reader;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Factory for creating XMLPull parser instances.
 * 
 * @author Dennis M. Sosnoski
 */
public class XMLPullReaderFactory implements IXMLReaderFactory
{
    /** Default parser factory name when nothing else found. */
    private static final String DEFAULT_PARSER_NAME =
        "org.xmlpull.mxp1.MXParserFactory";
    
    /** Singleton instance of class. */
    private static final XMLPullReaderFactory s_instance;
    static {
        
        // first try getting factory defined by property value
        XmlPullParserFactory factory = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = XMLPullReaderFactory.class.getClassLoader();
        }
        try {
            String name =
                System.getProperty(XmlPullParserFactory.PROPERTY_NAME);
            if (name != null && (name = name.trim()).length() > 0) {
                factory = XmlPullParserFactory.newInstance(name,
                    loader.getClass());
            }
        } catch (Exception ex) { /* deliberately empty */ }
        
        // if no luck that way, try getting it directly
        if (factory == null) {
            try {
                factory = XmlPullParserFactory.newInstance();
            } catch (Exception ex) { /* deliberately empty */ }
            if (factory == null) {
                throw new RuntimeException("Unable to create XMLPull parser");
            }
        }
        s_instance = new XMLPullReaderFactory(factory);
    }
    
    /** Factory used for constructing parser instances. */
    private final XmlPullParserFactory m_factory;
    
    /**
     * Internal constructor.
     * 
     * @param factory 
     */
    private XMLPullReaderFactory(XmlPullParserFactory factory) {
        m_factory = factory;
    }
    
    /**
     * Get instance of factory.
     * 
     * @return factory instance
     */
    public static XMLPullReaderFactory getInstance() {
        return s_instance;
    }
    
    /**
     * Create new parser instance.
     * 
     * @param nsf enable namespace processing on parser flag
     * @return parser instance
     * @throws XmlPullParserException on error creating parser
     */
    private XmlPullParser createParser(boolean nsf)
        throws XmlPullParserException {
        XmlPullParser parser = m_factory.newPullParser();
        if (nsf) {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        }
        return parser;
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.IXMLReaderFactory#createReader(java.io.InputStream, java.lang.String, java.lang.String, boolean)
     */
    public IXMLReader createReader(InputStream is, String name, String enc,
        boolean nsf) throws JiBXException {
        try {
            return recycleReader(new XMLPullReader(createParser(nsf)), is,
                name, enc);
        } catch (XmlPullParserException e) {
            throw new JiBXException("Error creating parser", e);
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.IXMLReaderFactory#createReader(java.io.Reader, java.lang.String, boolean)
     */
    public IXMLReader createReader(Reader rdr, String name, boolean nsf)
        throws JiBXException {
        try {
            return recycleReader(new XMLPullReader(createParser(nsf)),
                rdr, name);
        } catch (XmlPullParserException e) {
            throw new JiBXException("Error creating parser", e);
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.IXMLReaderFactory#recycleReader(org.jibx.runtime.IXMLReader, java.io.InputStream, java.lang.String, java.lang.String)
     */
    public IXMLReader recycleReader(IXMLReader old, InputStream is, String name,
        String enc) throws JiBXException {
        ((XMLPullReader)old).setDocument(is, name, enc);
        return old;
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.IXMLReaderFactory#recycleReader(org.jibx.runtime.IXMLReader, java.io.Reader, java.lang.String)
     */
    public IXMLReader recycleReader(IXMLReader old, Reader rdr, String name)
        throws JiBXException {
        ((XMLPullReader)old).setDocument(rdr, name);
        return old;
    }

    /**
     * Wrapper for an XMLPull parser implementation. Since the internal parser
     * API was originally based on XMLPull, this basically just delegates all
     * the calls with minimal processing.
     */
    private static class XMLPullReader implements IXMLReader
    {
        /** Actual parser. */
        private final XmlPullParser m_parser;
        
        /** Document name. */
        private String m_docName;
        
        /** Byte buffer used when stream set directly (lazy create,
         <code>null</code> if not used). */
        private InByteBuffer m_byteBuffer;
        
        /** Wrapper for supplied input stream (lazy create, <code>null</code>
         if not used). */
        private InputStreamWrapper m_streamWrapper;
        
        /** Input document character encoding (<code>null</code> if unknown) */
        private String m_encoding;
        
        /**
         * Constructor used by factory.
         * 
         * @param parser
         */
        private XMLPullReader(XmlPullParser parser) {
            m_parser = parser;
        }

        /**
         * Set document to be parsed from input stream.
         * 
         * @param is document input stream
         * @param name document name (<code>null</code> if unknown)
         * @param enc document character encoding (<code>null</code> if unknown)
         * @throws JiBXException on parser configuration error
         */
        private void setDocument(InputStream is, String name, String enc)
            throws JiBXException {
            try {
                if (enc == null) {
                    if (m_streamWrapper == null) {
                        m_streamWrapper = new InputStreamWrapper();
                        m_byteBuffer = new InByteBuffer();
                        m_streamWrapper.setBuffer(m_byteBuffer);
                    } else {
                        m_streamWrapper.reset();
                    }
                    m_byteBuffer.setInput(is);
                    setDocument(m_streamWrapper.getReader(), name);
                    m_encoding = m_streamWrapper.getEncoding();
                } else {
                    m_docName = name;
                    m_encoding = enc;
                    m_parser.setInput(is, enc);
                }
            } catch (XmlPullParserException e) {
                throw new JiBXException("Error initializing parser", e);
            } catch (IOException e) {
                throw new JiBXException("Error reading from stream", e);
            }
        }

        /**
         * Set document to be parsed from reader.
         * 
         * @param rdr document reader
         * @param name document name (<code>null</code> if unknown)
         * @throws JiBXException on parser configuration error
         */
        private void setDocument(Reader rdr, String name) throws JiBXException {
            try {
                m_docName = name;
                m_encoding = null;
                m_parser.setInput(rdr);
            } catch (XmlPullParserException e) {
                throw new JiBXException("Error initializing parser", e);
            }
        }
        
        /**
         * Format error message from exception.
         * 
         * @param e root cause exception
         */
        private String describeException(Exception e) {
            return "Error parsing document " + buildPositionString() + ": " +
                e.getMessage();
        }
        
        public void init() {}

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#buildPositionString()
         */
        public String buildPositionString() {
            String base = "(line " + m_parser.getLineNumber() + ", col " +
                m_parser.getColumnNumber();
            if (m_docName != null) {
                base += ", in " + m_docName;
            }
            return base + ')';
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#nextToken()
         */
        public int nextToken() throws JiBXException {
            try {
                return m_parser.nextToken();
            } catch (IOException e) {
                throw new JiBXException("Error accessing document", e);
            } catch (XmlPullParserException e) {
                throw new JiBXException
                    ("Error parsing document " + buildPositionString(), e);
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#next()
         */
        public int next() throws JiBXException {
            try {
                return m_parser.next();
            } catch (IOException e) {
                throw new JiBXException("Error accessing document", e);
            } catch (XmlPullParserException e) {
                throw new JiBXException
                    ("Error parsing document " + buildPositionString(), e);
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getEventType()
         */
        public int getEventType() throws JiBXException {
            try {
                return m_parser.getEventType();
            } catch (XmlPullParserException e) {
                throw new JiBXException
                    ("Error parsing document " + buildPositionString(), e);
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getName()
         */
        public String getName() {
            String name = m_parser.getName();
            if (name == null) {
                throw new IllegalStateException
                    ("Internal state error: not at start or end tag");
            } else {
                return name;
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getNamespace()
         */
        public String getNamespace() {
            String uri = m_parser.getNamespace();
            if (uri == null) {
                throw new IllegalStateException
                    ("Internal state error: not at start or end tag");
            } else {
                return uri;
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getPrefix()
         */
        public String getPrefix() {
            return m_parser.getPrefix();
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getAttributeCount()
         */
        public int getAttributeCount() {
            int count = m_parser.getAttributeCount();
            if (count < 0) {
                throw new IllegalStateException
                    ("Internal state error: not at start tag");
            } else {
                return count;
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getAttributeName(int)
         */
        public String getAttributeName(int index) {
            try {
                return m_parser.getAttributeName(index);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException(describeException(e));
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getAttributeNamespace(int)
         */
        public String getAttributeNamespace(int index) {
            try {
                return m_parser.getAttributeNamespace(index);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException(describeException(e));
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getAttributePrefix(int)
         */
        public String getAttributePrefix(int index) {
            try {
                return m_parser.getAttributePrefix(index);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException(describeException(e));
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getAttributeValue(int)
         */
        public String getAttributeValue(int index) {
            try {
                return m_parser.getAttributeValue(index);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException(describeException(e));
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getAttributeValue(java.lang.String, java.lang.String)
         */
        public String getAttributeValue(String ns, String name) {
            try {
                return m_parser.getAttributeValue(ns, name);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException(describeException(e));
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getText()
         */
        public String getText() {
            return m_parser.getText();
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getNestingDepth()
         */
        public int getNestingDepth() {
            return m_parser.getDepth();
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getNamespaceCount(int)
         */
        public int getNamespaceCount(int depth) {
            try {
                return m_parser.getNamespaceCount(depth);
            } catch (XmlPullParserException e) {
                throw new IllegalArgumentException(describeException(e));
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getNamespaceUri(int)
         */
        public String getNamespaceUri(int index) {
            try {
                return m_parser.getNamespaceUri(index);
            } catch (XmlPullParserException e) {
                throw new IllegalArgumentException(describeException(e));
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getNamespacePrefix(int)
         */
        public String getNamespacePrefix(int index) {
            try {
                return m_parser.getNamespacePrefix(index);
            } catch (XmlPullParserException e) {
                throw new IllegalArgumentException(describeException(e));
            }
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getDocumentName()
         */
        public String getDocumentName() {
            return m_docName;
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getLineNumber()
         */
        public int getLineNumber() {
            return m_parser.getLineNumber();
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getColumnNumber()
         */
        public int getColumnNumber() {
            return m_parser.getColumnNumber();
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getNamespace(java.lang.String)
         */
        public String getNamespace(String prefix) {
            return m_parser.getNamespace(prefix);
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#getInputEncoding()
         */
        public String getInputEncoding() {
            return m_encoding;
        }

        /* (non-Javadoc)
         * @see org.jibx.runtime.IXMLReader#isNamespaceAware()
         */
        public boolean isNamespaceAware() {
            return m_parser.getFeature
                (XmlPullParser.FEATURE_PROCESS_NAMESPACES);
        }
    }
}
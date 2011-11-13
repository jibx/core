/*
Copyright (c) 2005, Dennis M. Sosnoski.
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

import java.io.InputStream;
import java.io.Reader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;

/**
 * Factory for creating XMLPull parser instances.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public class StAXReaderFactory implements IXMLReaderFactory
{
    /** Singleton instance of class. */
    private static final StAXReaderFactory s_instance = new StAXReaderFactory();
    
    /** Factory used for constructing parser instances. */
    private final XMLInputFactory m_factory;
    
    /** Namespace processing state configured on factory. */
    private boolean m_isNamespaceEnabled;
    
    /**
     * Internal constructor.
     */
    private StAXReaderFactory() {
        XMLInputFactory factory;
        try {
            factory = XMLInputFactory.newInstance();
        } catch (FactoryConfigurationError e) {
            Thread thread = Thread.currentThread();
            ClassLoader cl = thread.getContextClassLoader();
            thread.setContextClassLoader
                (StAXReaderFactory.class.getClassLoader());
            try {
                factory = XMLInputFactory.newInstance();
            } finally {
                thread.setContextClassLoader(cl);
            }
        }
        m_factory = factory;
        m_isNamespaceEnabled = true;
    }
    
    /**
     * Get instance of factory.
     * 
     * @return factory instance
     */
    public static StAXReaderFactory getInstance() {
        return s_instance;
    }
    
    /**
     * Create new parser instance. In order to avoid thread safety issues the
     * caller must have a lock on the factory object.
     * 
     * @param nsf enable namespace processing on parser flag
     * @throws JiBXException on error creating parser
     */
    private void setNamespacesState(boolean nsf) throws JiBXException {
        if (nsf != m_isNamespaceEnabled) {
            try {
                m_factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE,
                    nsf ? Boolean.TRUE : Boolean.FALSE);
                m_isNamespaceEnabled = nsf;
            } catch (IllegalArgumentException e) {
                throw new JiBXException
                    ("Unable to create parser with required namespace handling");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.IXMLReaderFactory#createReader(java.io.InputStream, java.lang.String, java.lang.String, boolean)
     */
    public IXMLReader createReader(InputStream is, String name, String enc,
        boolean nsf) throws JiBXException {
        try {
            synchronized (m_factory) {
                setNamespacesState(nsf);
                if (enc == null) {
                    XMLStreamReader rdr = m_factory.createXMLStreamReader(is);
                    return new StAXReaderWrapper(rdr, name, nsf);
                } else {
                    XMLStreamReader rdr =
                        m_factory.createXMLStreamReader(is, enc);
                    return new StAXReaderWrapper(rdr, name, nsf);
                }
            }
        } catch (XMLStreamException e) {
            throw new JiBXException("Error creating parser", e);
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.IXMLReaderFactory#createReader(java.io.Reader, java.lang.String, boolean)
     */
    public IXMLReader createReader(Reader rdr, String name, boolean nsf)
        throws JiBXException {
        try {
            synchronized (m_factory) {
                setNamespacesState(nsf);
                return new StAXReaderWrapper(m_factory.createXMLStreamReader(rdr),
                    name, nsf);
            }
        } catch (XMLStreamException e) {
            throw new JiBXException("Error creating parser", e);
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.IXMLReaderFactory#recycleReader(org.jibx.runtime.IXMLReader, java.io.InputStream, java.lang.String, java.lang.String)
     */
    public IXMLReader recycleReader(IXMLReader old, InputStream is, String name,
        String enc) throws JiBXException {
        return createReader(is, name, enc, old.isNamespaceAware());
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.impl.IXMLReaderFactory#recycleReader(org.jibx.runtime.IXMLReader, java.io.Reader, java.lang.String)
     */
    public IXMLReader recycleReader(IXMLReader old, Reader rdr, String name)
        throws JiBXException {
        return createReader(rdr, name, old.isNamespaceAware());
    }
}
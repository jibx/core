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

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;

/**
 * Interface for factories used to create XML reader instances. Instances of
 * this interface must be assumed to be single threaded.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public interface IXMLReaderFactory
{
    /**
     * Get new XML reader instance for document from input stream.
     *
     * @param is document input stream
     * @param name document name (<code>null</code> if unknown)
     * @param enc document character encoding (<code>null</code> if unknown)
     * @param nsf namespaces enabled flag
     * @return new reader instance for document
     * @throws JiBXException on parser configuration error
     */
    public IXMLReader createReader(InputStream is, String name, String enc,
        boolean nsf) throws JiBXException;

    /**
     * Get new XML reader instance for document from reader.
     * 
     * @param rdr document reader
     * @param name document name (<code>null</code> if unknown)
     * @param nsf namespaces enabled flag
     * @return new reader instance for document
     * @throws JiBXException on parser configuration error
     */
    public IXMLReader createReader(Reader rdr, String name, boolean nsf)
        throws JiBXException;

    /**
     * Recycle XML reader instance for new document from input stream. If the
     * supplied reader can be reused it will be configured for the new document
     * and returned; otherwise, a new reader will be created for the document.
     * The namespace enabled state of the returned reader is always the same as
     * that of the supplied reader.
     *
     * @param old reader instance to be recycled
     * @param is document input stream
     * @param name document name (<code>null</code> if unknown)
     * @param enc document character encoding (<code>null</code> if unknown)
     * @return new reader instance for document
     * @throws JiBXException on parser configuration error
     */
    public IXMLReader recycleReader(IXMLReader old, InputStream is, String name,
        String enc) throws JiBXException;

    /**
     * Recycle XML reader instance for document from reader. If the supplied
     * reader can be reused it will be configured for the new document and
     * returned; otherwise, a new reader will be created for the document. The
     * namespace enabled state of the returned reader is always the same as that
     * of the supplied reader.
     *
     * @param old reader instance to be recycled
     * @param rdr document reader
     * @param name document name (<code>null</code> if unknown)
     * @return new reader instance for document
     * @throws JiBXException on parser configuration error
     */
    public IXMLReader recycleReader(IXMLReader old, Reader rdr, String name)
        throws JiBXException;
}
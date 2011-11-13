/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Basic resolver supporting relative URL paths.
 * 
 * @author Dennis M. Sosnoski
 */
public class UrlResolver implements ISchemaResolver
{
    /** Schema document URL. */
    private final URL m_url;
    
    /** Schema name. */
    private final String m_name;
    
    /** Unique identifier for this schema document. */
    private final String m_id;
    
    /**
     * Constructor. This converts paths to a standard form by eliminating "./" and "../" relative path components.
     * 
     * @param path 
     * @param url
     */
    public UrlResolver(String path, URL url) {
        m_url = url;
        String file = url.getFile();
        int offset;
        int base = 0;
        while ((offset = file.indexOf("/./", base)) >= 0) {
            file = file.substring(0, offset) + file.substring(offset+2);
            base = offset;
        }
        base = 1;
        while (file.length() > 1 && (offset = file.indexOf("/../", base)) >= 0) {
            int prior = file.lastIndexOf('/', offset-1);
            if (prior > 0) {
                file = file.substring(0, prior) + file.substring(offset+3);
                base = prior;
            } else {
                base = offset + 3;
            }
        }
        m_id = url.getProtocol().toLowerCase() + "://" + url.getHost().toLowerCase() + file;
        path.replace('\\', '/');
        int start = path.lastIndexOf('/');
        m_name = path.substring(start+1);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ISchemaResolver#getContent()
     */
    public InputStream getContent() throws IOException {
        return m_url.openStream();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ISchemaResolver#getName()
     */
    public String getName() {
        return m_name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ISchemaResolver#getId()
     */
    public String getId() {
        return m_id;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ISchemaResolver#resolve(java.lang.String,java.lang.String)
     */
    public ISchemaResolver resolve(String loc, String tns) throws IOException {
        return new UrlResolver(loc, new URL(m_url, loc));
    }
    
    /**
     * Get the document URL.
     * 
     * @return url
     */
    public URL getUrl() {
        return m_url;
    }

    public boolean equals(Object obj) {
        if (obj instanceof UrlResolver) {
            return m_id.equals(((UrlResolver)obj).m_id);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return m_id.hashCode();
    }
}
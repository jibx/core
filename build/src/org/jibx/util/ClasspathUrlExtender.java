/*
 * Copyright (c) 2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Support class for accessing resources using classpath URLs. The {@link #buildURL(URL, String)} method must be used to
 * construct a URL for a resource on the classpath, and the {@link #setClassLoader(ClassLoader)} method must first be
 * used to set the classloader if it differs from the classloader used for loading this class.
 */
public class ClasspathUrlExtender
{
    /** Protocol name for classpath access. */
    public static final String CLASSPATH_PROTOCOL = "classpath";
    
    /** Singleton instance of handler for stream access to resource. */
    private static ClasspathHandler s_handler;
    
    /**
     * Set the classloader to be used for accessing resources.
     * 
     * @param loader
     */
    public static void setClassLoader(ClassLoader loader) {
        s_handler = new ClasspathHandler(loader);
    }
    
    /**
     * Check if a URL string represents a resource from the classpath.
     * 
     * @param url
     * @return <code>true</code> if classpath resource, <code>false</code> if not
     */
    public static boolean isClasspathUrl(String url) {
        return url.toLowerCase().startsWith(CLASSPATH_PROTOCOL);
    }
    
    /**
     * Construct a URL which may represent a resource from the classpath.
     * 
     * @param base URL base for relative references
     * @param path resource path
     * @return URL for access to resource
     * @throws MalformedURLException if not a valid URL format
     */
    public static URL buildURL(URL base, String path) throws MalformedURLException {
        if (s_handler == null) {
            s_handler = new ClasspathHandler(ClasspathUrlExtender.class.getClassLoader());
        }
        if (path.toLowerCase().startsWith(CLASSPATH_PROTOCOL + ":")) {
            return new URL(null, path, s_handler);
        } else if (base != null && CLASSPATH_PROTOCOL.equals(base.getProtocol())) {
            return new URL(base, path, s_handler);
        } else {
            return new URL(base, path);
        }
    }
    
    /**
     * Handler for opening a connection to a resource from the classpath.
     */
    public static class ClasspathHandler extends URLStreamHandler
    {
        /** The classloader to use for finding resources. */
        private final ClassLoader m_loader;

        /**
         * Constructor.
         * 
         * @param loader classloader used to find resources
         */
        public ClasspathHandler(ClassLoader loader) {
            m_loader = loader;
        }

        /* (non-Javadoc)
         * @see java.net.URLStreamHandler#openConnection(java.net.URL)
         */
        protected URLConnection openConnection(URL url) throws IOException {
            String path = url.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            URL alturl = m_loader.getResource(path);
            if (alturl == null) {
                throw new IOException("Classpath resource not found with path '" + path + '\'');
            } else {
                return alturl.openConnection();
            }
        }
    }
}
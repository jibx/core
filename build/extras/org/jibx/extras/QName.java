/*
Copyright (c) 2005, Dennis M. Sosnoski
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

package org.jibx.extras;

import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * Representation of a qualified name. This includes the serializer/deserializer
 * methods for the representation. It assumes that the actual namespace
 * declarations are being handled separately for marshalling.
 * 
 * @author Dennis M. Sosnoski
 */
public class QName
{
    private String m_uri;
    private String m_prefix;
    private String m_name;
    
    /**
     * Default constructor.
     */
    public QName() {}
    
    /**
     * Constructor from full set of components.
     * 
     * @param uri
     * @param prefix
     * @param name
     */
    public QName(String uri, String prefix, String name) {
        m_uri = uri;
        m_prefix = prefix;
        m_name = name;
    }
    
    /**
     * Get local name.
     * 
     * @return name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Set local name.
     * 
     * @param name name
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Get namespace prefix.
     * 
     * @return prefix
     */
    public String getPrefix() {
        return m_prefix;
    }

    /**
     * Set namespace prefix.
     * 
     * @param prefix prefix
     */
    public void setPrefix(String prefix) {
        m_prefix = prefix;
    }

    /**
     * Get namespace URI.
     * 
     * @return uri
     */
    public String getUri() {
        return m_uri;
    }

    /**
     * Set namespace URI.
     * 
     * @param uri uri
     */
    public void setUri(String uri) {
        m_uri = uri;
    }

    /**
     * JiBX deserializer method. This is intended for use as a deserializer for
     * instances of the class.
     * 
     * @param text value text
     * @param ictx unmarshalling context
     * @return created class instance
     * @throws JiBXException on error in unmarshalling
     */
    public static QName deserialize(String text, IUnmarshallingContext ictx)
        throws JiBXException {
        
        // check for prefix used in text representation
        int split = text.indexOf(':');
        if (split > 0) {
            
            // strip off prefix
            String prefix = text.substring(0, split);
            text = text.substring(split+1);
            
            // make sure there aren't multiple colons
            if (text.indexOf(':') >= 0) {
                throw new JiBXException("Not a valid QName");
            } else {
                
                // look up the namespace URI associated with the prefix
                String uri =
                    ((UnmarshallingContext)ictx).getNamespaceUri(prefix);
                if (uri == null) {
                    throw new JiBXException("Undefined prefix " + prefix);
                } else {
                    
                    // create an instance of class to hold all components
                    return new QName(uri, prefix, text);
                    
                }
            }
        } else {
            
            // get the default namespace URI
            String uri = ((UnmarshallingContext)ictx).getNamespaceUri("");
            if (uri == null) {
                uri = "";
            }
            
            // create an instance of class to hold all components
            return new QName(uri, "", text);
        }
    }
    
    /**
     * JiBX serializer method. This is intended for use as a serializer for
     * instances of the class. The namespace must be active in the output
     * document at the point where this is called.
     * 
     * @param qname instance to be serialized
     * @param ictx unmarshalling context
     * @return created class instance
     * @throws JiBXException on error in marshalling
     */
    public static String serialize(QName qname, IMarshallingContext ictx)
        throws JiBXException {
        
        // check if prefix is alread defined in document with correct URI
        IXMLWriter ixw = ((MarshallingContext)ictx).getXmlWriter();
        int index = -1;
        int tryidx = ixw.getPrefixIndex(qname.m_prefix);
        if (tryidx >= 0 &&
            qname.m_uri.equals(ixw.getNamespaceUri(tryidx))) {
            index = tryidx;
        }
        if (index < 0) {
            
            // prefix not defined, find the namespace index in binding
            String[] nss = ixw.getNamespaces();
            for (int i = 0; i < nss.length; i++) {
                if (nss[i].equals(qname.m_uri)) {
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                
                // namespace not in binding, check extensions
                String[][] nsss = ixw.getExtensionNamespaces();
                if (nsss != null) {
                    int base = nss.length;
                    outer: for (int i = 0; i < nsss.length; i++) {
                        nss = nsss[i];
                        for (int j = 0; j < nss.length; j++) {
                            if (nss[j].equals(qname.m_uri)) {
                                index = base + j;
                                break outer;
                            }
                        }
                        base += nss.length;
                    }
                }
                
            }
        }
        if (index >= 0) {
            
            // get prefix defined for namespace
            String prefix = ixw.getNamespacePrefix(index);
            if (prefix == null) {
                throw new JiBXException("Namespace URI " + qname.m_uri +
                    " cannot be used since it is not active");
            } else if (prefix.length() > 0) {
                return prefix + ':' + qname.m_name;
            } else {
                return qname.m_name;
            }
            
        } else {
            throw new JiBXException("Unknown namespace URI " + qname.m_uri);
        }
    }
}
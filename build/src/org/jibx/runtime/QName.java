/*
Copyright (c) 2005-2010, Dennis M. Sosnoski. All rights reserved.

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

import java.util.ArrayList;

import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * <p>Representation of a qualified name. This includes the JiBX
 * serializer/deserializer methods for the representation. It assumes that the
 * actual namespace declarations are being handled separately for
 * marshalling</p>
 * 
 * <p>Note that this implementation treats only the namespace and local name as
 * significant for purposes of comparing values. The prefix is held only as a
 * convenience, and the actual prefix used when writing a value may differ from
 * the prefix defined by the instance.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public class QName
{
    /** Namespace URI. */
    private String m_uri;
    
    /** Namespace prefix. */
    private String m_prefix;
    
    /** Local name. */
    private String m_name;

    /**
     * Constructor from full set of components.
     * 
     * @param uri namespace uri, <code>null</code> if no-namespace namespace
     * @param prefix namespace prefix, <code>null</code> if unspecified, empty
     * string if default namespace
     * @param name local name
     */
    public QName(String uri, String prefix, String name) {
        m_uri = uri;
        m_prefix = prefix;
        m_name = name;
    }
    
    /**
     * Constructor from namespace and local name. This constructor is provided
     * as a convenience for when the actual prefix used for a namespace is
     * irrelevant.
     * 
     * @param uri namespace uri, <code>null</code> if no-namespace namespace
     * @param name
     */
    public QName(String uri, String name) {
        this(uri, null, name);
    }
    
    /**
     * Constructor from local name only. This constructor is provided as a
     * convenience for names in the no-namespace namespace.
     * 
     * @param name
     */
    public QName(String name) {
        this(null, null, name);
    }
    
    //
    // Overrides of base class methods
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof QName) {
            QName qname = (QName)obj;
            if (m_name.equals(qname.getName())) {
                if (m_uri == null) {
                    return qname.getUri() == null;
                } else {
                    return m_uri.equals(qname.getUri());
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (m_uri == null ? 0 : m_uri.hashCode()) + m_name.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (m_uri == null) {
            return m_name;
        } else {
            return "{" + m_uri + "}:" + m_name;
        }
    }
    
    //
    // Access methods
    
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
     * @return prefix, <code>null</code> if unspecified, empty string if default
     * namespace
     */
    public String getPrefix() {
        return m_prefix;
    }

    /**
     * Set namespace prefix.
     * 
     * @param prefix prefix, <code>null</code> if unspecified, empty string if
     * default namespace
     */
    public void setPrefix(String prefix) {
        m_prefix = prefix;
    }

    /**
     * Get namespace URI.
     * 
     * @return uri namespace uri, <code>null</code> if no-namespace namespace
     */
    public String getUri() {
        return m_uri;
    }

    /**
     * Set namespace URI.
     * 
     * @param uri namespace uri, <code>null</code> if no-namespace namespace
     */
    public void setUri(String uri) {
        m_uri = uri;
    }
    
    //
    // JiBX conversion methods

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
        if (text == null) {
            return null;
        } else {
            
            // check for prefix used in text representation
            int split = text.indexOf(':');
            if (split > 0) {
                
                // strip off prefix
                String prefix = text.substring(0, split);
                text = text.substring(split+1);
                
                // look up the namespace URI associated with the prefix
                String uri =
                    ((UnmarshallingContext)ictx).getNamespaceUri(prefix);
                if (uri == null) {
                    throw new JiBXException("Undefined prefix " + prefix);
                } else {
                    
                    // create an instance of class to hold all components
                    return new QName(uri, prefix, text);
                    
                }
                
            } else {
                
                // create it using the default namespace URI
                String uri = ((UnmarshallingContext)ictx).getNamespaceUri(null);
                if (uri != null && uri.length() == 0) {
                    uri = null;
                }
                return new QName(uri, "", text);
            }
        }
    }
    
    /**
     * JiBX serializer method. This is intended for use as a serializer for
     * instances of the class. The namespace must be active in the output
     * document at the point where this is called.
     * 
     * @param qname value to be serialized
     * @param ictx unmarshalling context
     * @return created class instance
     * @throws JiBXException on error in marshalling
     */
    public static String serialize(QName qname, IMarshallingContext ictx)
        throws JiBXException {
        if (qname == null) {
            return null;
        } else {
            
            // check for specified prefix
            IXMLWriter ixw = ((MarshallingContext)ictx).getXmlWriter();
            int index = -1;
            String uri = qname.getUri();
            if (uri == null) {
                uri = "";
            }
            if (qname.getPrefix() != null) {
                
                // see if prefix already defined in document with correct URI
                int tryidx = ixw.getPrefixIndex(qname.getPrefix());
                if (tryidx >= 0 &&
                    uri.equals(ixw.getNamespaceUri(tryidx))) {
                    index = tryidx;
                }
            }
            
            // check if need to lookup prefix for namespace
            if (index < 0) {
                
                // prefix not defined, find the namespace index in binding
                if (uri == null) {
                    uri = "";
                }
                String[] nss = ixw.getNamespaces();
                for (int i = 0; i < nss.length; i++) {
                    if (nss[i].equals(uri)) {
                        index = i;
                        break;
                    }
                }
                if (index < 0) {
                    
                    // namespace not in binding, check extensions
                    String[][] nsss = ixw.getExtensionNamespaces();
                    if (nsss != null) {
                        int base = nss.length;
                        outer: for (int i = nsss.length-1; i >= 0; i--) {
                            nss = nsss[i];
                            for (int j = 0; j < nss.length; j++) {
                                if (nss[j].equals(uri)) {
                                    
                                    // adjust for other extension namespaces
                                    index = base + j;
                                    for (int k = 0; k < i; k++) {
                                        index += nsss[k].length;
                                    }
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
            
            // check if prefix is already defined in document with correct URI
            if (index >= 0) {
                
                // get prefix defined for namespace
                String prefix = ixw.getNamespacePrefix(index);
                if (prefix == null) {
                    throw new JiBXException("Namespace URI " + qname.getUri() +
                        " cannot be used since it is not active");
                } else if (prefix.length() > 0) {
                    return prefix + ':' + qname.getName();
                } else {
                    return qname.getName();
                }
                
            } else {
                throw new JiBXException("Unknown namespace URI " + qname.m_uri);
            }
        }
    }

    /**
     * JiBX deserializer method. This is intended for use as a deserializer for
     * a list made up of instances of the class.
     * 
     * @param text value text
     * @param ictx unmarshalling context
     * @return array of instances
     * @throws JiBXException on error in marshalling
     */
    public static QName[] deserializeList(String text,
        final IUnmarshallingContext ictx) throws JiBXException {
        
        // use basic qualified name deserializer to handle items
        IListItemDeserializer ldser = new IListItemDeserializer() {
            public Object deserialize(String text) throws JiBXException {
                return QName.deserialize(text, ictx);
            }
        };
        ArrayList list = Utility.deserializeList(text, ldser);
        if (list == null) {
            return null;
        } else {
            return (QName[])list.toArray(new QName[list.size()]);
        }
    }
    
    /**
     * JiBX serializer method. This is intended for use as a serializer for a
     * list made up of instances of the class. The namespace must be active in
     * the output document at the point where this is called.
     * 
     * @param qnames array of names to be serialized
     * @param ictx unmarshalling context
     * @return generated text
     * @throws JiBXException on error in marshalling
     */
    public static String serializeList(QName[] qnames, IMarshallingContext ictx)
        throws JiBXException {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < qnames.length; i++) {
            QName qname = qnames[i];
            if (qname != null) {
                if (buff.length() > 0) {
                    buff.append(' ');
                }
                buff.append(serialize(qname, ictx));
            }
        }
        return buff.toString();
    }
}
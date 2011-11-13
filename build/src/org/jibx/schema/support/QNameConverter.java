/*
Copyright (c) 2008-2010, Dennis M. Sosnoski. All rights reserved.

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

package org.jibx.schema.support;

import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.schema.elements.SchemaElement;

/**
 * Qualified name serializer/deserializer for use in schema definitions. This
 * uses special handling for values using the default namespace, checking if the
 * schema being processed is a no-namespace schema being included into a schema
 * with a namespace. If it is, the including namespace is used as the default.
 * 
 * @author Dennis M. Sosnoski
 */
public class QNameConverter
{
    /**
     * Qualified name serializer method for use within schema definitions.
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
            String prefix = qname.getPrefix();
            if (prefix != null) {
                
                // see if prefix already defined in document with correct URI
                int tryidx = ixw.getPrefixIndex(prefix);
                if (tryidx >= 0 &&
                    uri.equals(ixw.getNamespaceUri(tryidx))) {
                    index = tryidx;
                } else if ("".equals(prefix)) {
                    
                    // check for no-namespace schema imported into namespace
                    int depth = ictx.getStackDepth();
                    for (int i = 0; i < depth; i++) {
                        Object obj = ictx.getStackObject(i);
                        if (obj instanceof SchemaElement) {
                            SchemaElement schema = (SchemaElement)obj;
                            String ens = schema.getEffectiveNamespace();
                            if (ens != schema.getTargetNamespace()) {
                                return qname.getName();
                            } else {
                                break;
                            }
                        }
                    }
                    
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
            
            // check if prefix is alread defined in document with correct URI
            if (index >= 0) {
                
                // get prefix defined for namespace
                prefix = ixw.getNamespacePrefix(index);
                if (prefix == null) {
                    throw new JiBXException("Namespace URI " + qname.getUri() +
                        " cannot be used since it is not active");
                } else if (prefix.length() > 0) {
                    return prefix + ':' + qname.getName();
                } else {
                    return qname.getName();
                }
                
            } else {
                throw new JiBXException("Unknown namespace URI " + qname.getUri());
            }
        }
    }
    
    /**
     * Qualified name list serializer method for use within schema definitions.
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
    
    /**
     * Patch qualified name with effective namespace from schema. If the qualified name does not have a namespace, this
     * uses the namespace from the schema.
     *
     * @param uri effective namespace URI from schema
     * @param qname qualified name (call ignored if <code>null</code>)
     */
    public static void patchQNameNamespace(String uri, QName qname) {
        if (uri != null && qname != null && qname.getUri() == null) {
            qname.setUri(uri);
        }
    }
}
/*
 * Copyright (c) 2004-2008, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.ws.wsdl.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.util.StringIntSizedMap;
import org.jibx.ws.wsdl.model.Definitions;

/**
 * WSDL writer class. This handles writing generated WSDLs and schemas.
 * 
 * @author Dennis M. Sosnoski
 */
public class WsdlWriter
{
    /** Fixed URI for WSDL namespace. */
    public static final String WSDL_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/";
    
    /** Fixed prefix for WSDL namespace. */
    public static final String WSDL_NAMESPACE_PREFIX = "wsdl";
    
    /** Fixed URI for SOAP namespace. */
    public static final String SOAP_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
    
    /** Fixed prefix for SOAP namespace. */
    public static final String SOAP_NAMESPACE_PREFIX = "soap";
    
    /** Fixed prefix for WSDL target namespace. */
    public static final String DEFINITIONS_NAMESPACE_PREFIX = "wns";
    
    /** Namespaces defined in binding. */
    private StringIntSizedMap s_namespaceMap;
    
    /** Namespace index for the WSDL namespace. */
    private int s_wsdlNamespaceIndex;
    
    /** Map from extra namespace URIs to prefixes. */
    private Map m_uriPrefixMap;
    
    /** Namespace index for the SOAP namespace. */
    private int s_soapNamespaceIndex;
    
    /** Marshalling context. */
    private final MarshallingContext m_marshalContext;
    
    /**
     * Constructor.
     * 
     * @throws JiBXException on error creating marshaller
     */
    public WsdlWriter() throws JiBXException {
        
        // set the marshalling contexts
        IBindingFactory ifact = BindingDirectory.getFactory(Definitions.class);
        m_marshalContext = (MarshallingContext)ifact.createMarshallingContext();
        
        // initialize namespace URI to index map
        String[] nss = ifact.getNamespaces();
        s_namespaceMap = new StringIntSizedMap(nss.length);
        for (int i = 0; i < nss.length; i++) {
            s_namespaceMap.add(nss[i], i);
        }
        
        // create other statics used in code
        s_wsdlNamespaceIndex = s_namespaceMap.get(WSDL_NAMESPACE_URI);
        s_soapNamespaceIndex = s_namespaceMap.get(SOAP_NAMESPACE_URI);
    }
    
    /**
     * Write WSDL for service to output stream.
     * 
     * @param def WSDL definitions information
     * @param os destination output stream
     * @exception JiBXException on error creating WSDL output
     */
    public void writeWSDL(Definitions def, OutputStream os) throws JiBXException {
        
        // configure context for output stream
        m_marshalContext.setOutput(os, null);
        m_marshalContext.setIndent(2);
        m_marshalContext.setUserContext(def);
        
//        // set up information for namespace indexes and prefixes
//        Set uriset = def.getNamespaces();
//        String[] uris = new String[uriset.size()];
//        int[] indexes = new int[uris.length + 2];
//        String[] prefs = new String[uris.length + 2];
//        IXMLWriter writer = m_marshalContext.getXmlWriter();
//        int base = writer.getNamespaceCount();
//        int index = 0;
//        for (Iterator iter = uriset.iterator(); iter.hasNext();) {
//            String uri = (String)iter.next();
//            uris[index] = uri;
//            indexes[index] = base + index;
//            prefs[index++] = def.getPrefix(uri);
//        }
//        indexes[index] = s_wsdlNamespaceIndex;
//        prefs[index++] = WSDL_NAMESPACE_PREFIX;
//        indexes[index] = s_soapNamespaceIndex;
//        prefs[index] = SOAP_NAMESPACE_PREFIX;
//        
//        // add the namespace declarations to current element
//        writer.pushExtensionNamespaces(uris);
//        /*
//         * writer.openNamespaces(indexes, prefs); for (int i = 0; i < uris.length; i++) { String prefix = prefs[i];
//         * String name = prefix.length() > 0 ? "xmlns:" + prefix : "xmlns"; writer.addAttribute(0, name, uris[i]); }
//         */
//
//        // write start tag with added namespaces
//        m_marshalContext.startTagNamespaces(s_wsdlNamespaceIndex, "definitions", indexes, prefs);
//        m_marshalContext.attribute(0, "targetNamespace", def.getWsdlNamespace());
//        m_marshalContext.closeStartContent();
        
        // marshal out remaining data
        IMarshaller mar = m_marshalContext.getMarshaller(Definitions.class.getName());
        mar.marshal(def, m_marshalContext);
        m_marshalContext.endDocument();
//        
//        // finish with close tag
//        m_marshalContext.endTag(s_wsdlNamespaceIndex, "definitions");
//        m_marshalContext.endDocument();
    }
    
    public static class SchemaMarshaller implements IMarshaller
    {
        /** Marshalling context for schema. */
        private final MarshallingContext m_schemaContext;
        
        public SchemaMarshaller() throws JiBXException {
            IBindingFactory ifact = BindingDirectory.getFactory(SchemaUtils.XS_PREFIX_BINDING, SchemaElement.class);
            m_schemaContext = (MarshallingContext)ifact.createMarshallingContext();
        }
        
        public boolean isExtension(String mapname) {
            return false;
        }
        
        public void marshal(Object obj, IMarshallingContext ctx) throws JiBXException {
            try {
                m_schemaContext.setFromContext((MarshallingContext)ctx);
                ((IMarshallable)obj).marshal(m_schemaContext);
                m_schemaContext.getXmlWriter().flush();
            } catch (IOException e) {
                throw new JiBXException("Error writing schema", e);
            }
        }
    }
}
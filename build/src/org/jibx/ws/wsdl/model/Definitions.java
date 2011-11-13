/*
 * Copyright (c) 2004-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.ws.wsdl.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jibx.runtime.QName;

/**
 * Top-level component of WSDL definition.
 * TODO: modify to support multiple portTypes, bindings, and services
 * 
 * @author Dennis M. Sosnoski
 */
public class Definitions extends WsdlBase
{
    /** Transport specification for SOAP over HTTP. */
    public static final String HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";
    
    /** Supported style value. */
    public static final String STYLE_DOCUMENT = "document";
    
    /** Prefix for WSDL target namespace. */
    private String m_wsdlPrefix;
    
    /** Target namespace for WSDL. */
    private String m_wsdlNamespace;
    
    /** Name for port type. */
    private String m_portTypeName;
    
    /** Name for binding. */
    private String m_bindingName;
    
    /** Name for service. */
    private String m_serviceName;
    
    /** Name for port. */
    private String m_portName;
    
    /** Schema definition holders. */
    private ArrayList m_schemas;
    
    /** Message definitions. */
    private ArrayList m_messages;
    
    /** Operation definitions. */
    private ArrayList m_operations;
    
    /** Documentation for the portType. */
    private List m_portTypeDocumentation;
    
    /** Service location URL. */
    private String m_serviceLocation;
    
    /** Namespaces referenced from WSDL. */
    private Set m_namespaceUris;
    
    /**
     * Default constructor. This is only used by the unmarshalling code.
     */
    private Definitions() {}
    
    /**
     * Standard constructor.
     * 
     * @param tname port type name
     * @param bname binding name
     * @param sname service name
     * @param pname port name
     * @param wpfx prefix for WSDL target namespace
     * @param wuri WSDL target namespace
     */
    public Definitions(String tname, String bname, String sname, String pname, String wpfx, String wuri) {
        this();
        m_portTypeName = tname;
        m_bindingName = bname;
        m_serviceName = sname;
        m_portName = pname;
        m_wsdlPrefix = wpfx;
        m_wsdlNamespace = wuri;
        m_schemas = new ArrayList();
        m_messages = new ArrayList();
        m_operations = new ArrayList();
        m_namespaceUris = new HashSet();
        m_namespaceUris.add(wuri);
        addNamespaceDeclaration(wpfx, wuri);
    }
    
    /**
     * Set service location.
     * 
     * @param sloc service location URL string
     */
    public void setServiceLocation(String sloc) {
        m_serviceLocation = sloc;
    }
    
    /**
     * Add message definition.
     * 
     * @param msg message definition
     */
    public void addMessage(Message msg) {
        m_messages.add(msg);
    }
    
    /**
     * Add operation definition.
     * 
     * @param op operation definition
     */
    public void addOperation(Operation op) {
        m_operations.add(op);
    }
    
    /**
     * Get port type name.
     * 
     * @return port type name
     */
    public String getPortTypeName() {
        return m_portTypeName;
    }
    
    /**
     * Get port type qualified name
     *
     * @return port type qualified name
     */
    public QName getPortTypeQName() {
        return new QName(m_wsdlNamespace, m_portTypeName);
    }
    
    /**
     * Get binding name.
     * 
     * @return binding name
     */
    public String getBindingName() {
        return m_bindingName;
    }
    
    /**
     * Get binding qualified name
     *
     * @return binding qualified name
     */
    public QName getBindingQName() {
        return new QName(m_wsdlNamespace, m_bindingName);
    }
    
    /**
     * Get service name.
     * 
     * @return service name
     */
    public String getServiceName() {
        return m_serviceName;
    }
    
    /**
     * Get port name.
     * 
     * @return port name
     */
    public String getPortName() {
        return m_portTypeName;
    }
    
    /**
     * Get WSDL target namespace prefix.
     * 
     * @return target namespace prefix
     */
    public String getWsdlPrefix() {
        return m_wsdlPrefix;
    }
    
    /**
     * Get WSDL target namespace URI.
     * 
     * @return target namespace
     */
    public String getWsdlNamespace() {
        return m_wsdlNamespace;
    }
    
    /**
     * Get schema definition holders.
     * 
     * @return schemas
     */
    public ArrayList getSchemas() {
        return m_schemas;
    }
    
    /**
     * Get service location.
     * 
     * @return service location URL string
     */
    public String getServiceLocation() {
        return m_serviceLocation;
    }
    
    /**
     * Get portType documentation.
     * 
     * @return list of nodes
     */
    public List getPortTypeDocumentation() {
        return m_portTypeDocumentation;
    }
    
    /**
     * Set portType documentation.
     * 
     * @param nodes list of nodes
     */
    public void setPortTypeDocumentation(List nodes) {
        m_portTypeDocumentation = nodes;
    }
    
    /**
     * Get messages.
     * 
     * @return list of messages
     */
    public ArrayList getMessages() {
        return m_messages;
    }
    
    /**
     * Get operations.
     * 
     * @return list of operations
     */
    public ArrayList getOperations() {
        return m_operations;
    }
    
    /**
     * Add namespace to set declared in WSDL. This just uses numbered prefixes.
     * 
     * @param uri
     */
    public void addNamespace(String uri) {
        if (!m_namespaceUris.contains(uri)) {
            addNamespaceDeclaration("ns"+m_namespaceUris.size(), uri);
            m_namespaceUris.add(uri);
        }
    }
}
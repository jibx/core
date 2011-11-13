/*
 * Copyright (c) 2007, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.ws.wsdl.tools.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jibx.custom.classes.IApply;
import org.jibx.custom.classes.IDocumentFormatter;
import org.jibx.custom.classes.SharedNestingBase;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.schema.generator.FormatterCache;
import org.jibx.util.IClassLocator;
import org.jibx.util.StringArray;

/**
 * Global customization information for WSDL generation. This extends the binding customization model to include the
 * information used for service definitions.
 * 
 * @author Dennis M. Sosnoski
 */
public class WsdlCustom extends NestingBase implements IApply
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "wsdl-namespace" }, NestingBase.s_allowedAttributes);
    
    /** Customization value from unmarshalling. */
    private String m_wsdlNamespace;
    
    /** List of Fault definitions. */
    private final ArrayList m_faultList;
    
    /** Map from fully-qualified class name to Fault information. */
    private final Map m_faultMap;
    
    /** List of services, in order added. */
    private final ArrayList m_serviceList;
    
    /** Map from fully-qualified class name to service information. */
    private final Map m_serviceMap;
    
    /** Class locator. */
    private IClassLocator m_locator;
    
    /** JavaDoc formatter instance cache. */
    private FormatterCache m_formatCache;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public WsdlCustom(SharedNestingBase parent) {
        super(parent);
        m_faultList = new ArrayList();
        m_faultMap = new HashMap();
        m_serviceList = new ArrayList();
        m_serviceMap = new HashMap();
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Get the namespace for WSDL definitions of services.
     * 
     * @return WSDL namespace (<code>null</code> if unspecified)
     */
    public String getWsdlNamespace() {
        return m_wsdlNamespace;
    }
    
    /**
     * Set the namespace for WSDL definitions of services.
     * 
     * @param uri WSDL namespace (<code>null</code> if to be derived from service class name)
     */
    public void setWsdlNamespace(String uri) {
        m_wsdlNamespace = uri;
    }
    
    /**
     * Get list of Faults.
     * 
     * @return fault list
     */
    public List getFaults() {
        return m_faultList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.SharedNestingBase#getNameStyle()
     */
    public int getNameStyle() {
        return CAMEL_CASE_NAMES;
    }
    
    /**
     * Get fault customization information. This method should only be used after the {@link #apply(IClassLocator)}
     * method is called.
     * 
     * @param type fully qualified class name
     * @return fault customization (<code>null</code> if none)
     */
    public FaultCustom getFaultCustomization(String type) {
        return (FaultCustom)m_faultMap.get(type);
    }
    
    /**
     * Force fault customization information. This method should only be used after the {@link #apply(IClassLocator)}
     * method is called. If the fault customization information has not previously been created, it will be created by
     * this call.
     * 
     * @param type fully qualified exception class name
     * @return fault customization (<code>null</code> if none)
     */
    public FaultCustom forceFaultCustomization(String type) {
        FaultCustom fault = (FaultCustom)m_faultMap.get(type);
        if (fault == null) {
            fault = new FaultCustom(this, type);
            fault.apply(m_locator);
            m_faultMap.put(type, fault);
        }
        return fault;
    }
    
    /**
     * Get list of services.
     * 
     * @return service list
     */
    public List getServices() {
        return m_serviceList;
    }
    
    /**
     * Get service customization information. This method should only be used after the {@link #apply(IClassLocator)}
     * method is called.
     * 
     * @param type fully qualified class name
     * @return service customization (<code>null</code> if none)
     */
    public ServiceCustom getServiceCustomization(String type) {
        return (ServiceCustom)m_serviceMap.get(type);
    }
    
    /**
     * Add new service customization. This creates the service customization, using defaults, and adds it to the
     * internal structures. This method should only be used after first calling {@link #getServiceCustomization(String)}
     * and obtaining a <code>null</code> result.
     * 
     * @param type fully qualified class name
     * @return service customization
     */
    public ServiceCustom addServiceCustomization(String type) {
        ServiceCustom service = new ServiceCustom(this, type);
        service.apply(m_locator, getFormatter(service));
        m_serviceList.add(service);
        m_serviceMap.put(type, service);
        return service;
    }
    
    /**
     * Get a JavaDoc formatter instance for a class.
     *
     * @param custom customization information
     * @return formatter
     */
    public IDocumentFormatter getFormatter(SharedNestingBase custom) {
        return m_formatCache.getFormatter(custom);
    }
    
    /**
     * Unmarshalling factory. This gets the containing element and the name so that the standard constructor can be
     * used.
     * 
     * @param ictx
     * @return created instance
     */
    private static WsdlCustom factory(IUnmarshallingContext ictx) {
        return new WsdlCustom((SharedNestingBase)getContainingObject(ictx));
    }
    
    /**
     * Apply customizations to services to fill out members.
     * 
     * @param icl class locator
     */
    public void apply(IClassLocator icl) {
        
        // save locator for later use (when services are added)
        m_locator = icl;
        m_formatCache = new FormatterCache(icl);
        
        // inherit namespace directly from package level, if not specified
        String ns = getSpecifiedNamespace();
        if (ns == null) {
            ns = getParent().getNamespace();
        }
        setNamespace(ns);
        
        // fix Faults and create map
        for (int i = 0; i < m_faultList.size(); i++) {
            FaultCustom fault = (FaultCustom)m_faultList.get(i);
            fault.apply(icl);
            m_faultMap.put(fault.getExceptionType(), fault);
        }
        
        // register services with names supplied (priority over generated names)
        for (int i = 0; i < m_serviceList.size(); i++) {
            ServiceCustom service = (ServiceCustom)m_serviceList.get(i);
            String name = service.getServiceName();
            if (name != null) {
                if (registerName(name, service) != name) {
                    throw new IllegalStateException("Duplicate service name " + name);
                }
            }
        }
        
        // fix services and create map
        for (int i = 0; i < m_serviceList.size(); i++) {
            // TODO: this largely duplicates addServiceCustomization() - are both necessary?
            ServiceCustom service = (ServiceCustom)m_serviceList.get(i);
            service.apply(icl, getFormatter(service));
            m_serviceMap.put(service.getClassName(), service);
        }
    }
}
/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski. All rights reserved.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jibx.binding.model.EmptyArrayList;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.util.StringArray;

/**
 * Base class for all element structures in WSDL definition which allow arbitrary attributes from outside the WSDL
 * namespace.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class OpenAttrBase extends WsdlBase
{
    /** Extra attributes associated with element (lazy create, <code>null</code> if unused). */
    private ArrayList m_attributes;
    
    //
    // Overrides of base class methods

    /**
     * Get the WSDL target namespace.
     *
     * @return namespace
     */
    public String getNamespace() {
        return "http://schemas.xmlsoap.org/wsdl/";
    }
    
    //
    // Binding methods
    
    /**
     * Pre-get method called during marshalling. This first calls the base class implementation to handle namespaces,
     * then writes any extra attributes to the element start tag.
     * 
     * @param ictx marshalling context
     * @throws JiBXException on error
     */
    protected void preget(IMarshallingContext ictx) throws JiBXException {
        
        // call base class implementation to handle namespaces
        super.preget(ictx);
        
        // write all extra attributes to element start tag
        if (m_attributes != null) {
            IXMLWriter writer = ictx.getXmlWriter();
            for (int i = 0; i < m_attributes.size();) {
                String name = (String)m_attributes.get(i++);
                String uri = (String)m_attributes.get(i++);
                String value = (String)m_attributes.get(i++);
                int index = writer.getNamespaceCount();
                while (--index >= 0 && !uri.equals(writer.getNamespaceUri(index)));
                if (index >= 0) {
                    try {
                        writer.addAttribute(index, name, value);
                    } catch (IOException e) {
                        throw new JiBXException("Error writing attribute", e);
                    }
                } else {
                    throw new JiBXException("Namespace uri \"" + uri + "\" is not defined");
                }
            }
        }
    }
    
    //
    // Access methods
    
    /**
     * Get read-only list of extra attributes. Entries in this list are triplets, consisting of attribute name,
     * namespace, and value.
     * 
     * @return extra attribute list
     */
    public final List getExtraAttributes() {
        if (m_attributes == null || m_attributes.size() == 0) {
            return EmptyArrayList.INSTANCE;
        } else {
            return Collections.unmodifiableList(m_attributes);
        }
    }
    
    /**
     * Clear extra attribute list.
     */
    public final void clearExtraAttributes() {
        if (m_attributes != null) {
            m_attributes.clear();
        }
    }
    
    /**
     * Add extra attribute.
     * 
     * @param name attribute name
     * @param uri attribute namespace URI
     * @param value attribute value
     */
    public final void addExtraAttribute(String name, String uri, String value) {
        if (m_attributes == null) {
            m_attributes = new ArrayList();
        }
        m_attributes.add(name);
        m_attributes.add(uri);
        m_attributes.add(value);
    }
    
    //
    // Validation methods
    
    /**
     * Validate attributes of element from schema namespace. This allows any number of attributes from other namespaces
     * on the element.
     * 
     * @param ictx unmarshalling context
     * @param attrs attributes array
     * @exception JiBXException on unmarshalling error
     */
    protected void validateAttributes(IUnmarshallingContext ictx, StringArray attrs) throws JiBXException {
        
        // handle basic validation
        readNamespaces(ictx);
        validateAttributes(ictx, true, attrs);
        
        // loop through all attributes of current element
        UnmarshallingContext uctx = (UnmarshallingContext)ictx;
        clearExtraAttributes();
        for (int i = 0; i < uctx.getAttributeCount(); i++) {
            
            // collect attributes from non-schema namespaces
            String ns = uctx.getAttributeNamespace(i);
            if (ns != null && ns.length() > 0 && !getNamespace().equals(ns)) {
                addExtraAttribute(uctx.getAttributeName(i), ns, uctx.getAttributeValue(i));
            }
        }
    }
}
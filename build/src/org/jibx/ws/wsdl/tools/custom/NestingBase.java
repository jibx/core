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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jibx.custom.classes.CustomBase;
import org.jibx.custom.classes.SharedNestingBase;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.util.StringArray;

/**
 * Base class for nested WSDL customizations that can contain other customizations.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class NestingBase extends SharedNestingBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "service-base", "set-actions", "use-nillable", "wrapped" },
            SharedNestingBase.s_allowedAttributes);
    
    // values inherited through nesting
    private Boolean m_wrapped;
    
    private Boolean m_setActions;
    
    private Boolean m_useNillable;
    
    private String m_serviceBase;
    
    // set of unique names at level
    private final Map m_namedChildMap;
    
    // TODO: add WSDL namespace prefix, schema namespace prefix, WSDL structuring
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public NestingBase(SharedNestingBase parent) {
        super(parent);
        m_namedChildMap = new HashMap();
    }
    
    //
    // Getters for values inherited through nesting
    
    /**
     * Check wrapped flag.
     * 
     * @return wrapped flag
     */
    public boolean isWrapped() {
        if (m_wrapped == null) {
            if (this instanceof WsdlCustom || getParent() == null) {
                return true;
            } else {
                return ((NestingBase)getParent()).isWrapped();
            }
        } else {
            return m_wrapped.booleanValue();
        }
    }
    
    /**
     * Check if soapAction should be set.
     * 
     * @return soapAction flag
     */
    public boolean isSoapAction() {
        if (m_setActions == null) {
            if (this instanceof WsdlCustom || getParent() == null) {
                return true;
            } else {
                return ((NestingBase)getParent()).isSoapAction();
            }
        } else {
            return m_setActions.booleanValue();
        }
    }
    
    /**
     * Check if xsi:nillable should be used for optional values (rather than minOccurs='0').
     * 
     * @return xsi:nillable flag
     */
    public boolean isNillable() {
        if (m_useNillable == null) {
            if (this instanceof WsdlCustom || getParent() == null) {
                return false;
            } else {
                return ((NestingBase)getParent()).isNillable();
            }
        } else {
            return m_useNillable.booleanValue();
        }
    }
    
    /**
     * Get the service base address.
     * 
     * @return base address
     */
    public String getServiceBase() {
        if (m_serviceBase == null) {
            if (this instanceof WsdlCustom || getParent() == null) {
                return "http://localhost:8080/axis2/services";
            } else {
                return ((NestingBase)getParent()).getServiceBase();
            }
        } else {
            return m_serviceBase;
        }
    }
    
    /**
     * Get child by name.
     * 
     * @param name
     * @return named child, <code>null</code> if name not registered
     */
    public CustomBase getChild(String name) {
        return (CustomBase)m_namedChildMap.get(name);
    }
    
    /**
     * Register a child name. If the base name supplied has already been used by a different child, the name will be
     * modified by adding a numeric suffix to make it unique. Once a name has been registered for a child, calling this
     * method again with that name is guaranteed to just return that same name. Depending on the nesting level, the type
     * of child may take different forms. This doesn't care what the names represent, it just makes sure they're unique.
     * 
     * @param base proposed name
     * @param child named child
     * @return allowed name
     */
    public String registerName(String base, CustomBase child) {
        int index = 0;
        String name = base;
        Object value;
        while ((value = m_namedChildMap.get(name)) != null && value != child) {
            name = base + ++index;
        }
        m_namedChildMap.put(name, child);
        return name;
    }
    
    /**
     * Get WSDL definitions namespace.
     * 
     * @return WSDL namespace
     */
    public abstract String getWsdlNamespace();
    
    /**
     * Gets the parent element link from the unmarshalling stack. This method is for use by factories during
     * unmarshalling.
     * 
     * @param ictx unmarshalling context
     * @return containing class
     */
    protected static SharedNestingBase getContainingClass(IUnmarshallingContext ictx) {
        Object parent = ictx.getStackTop();
        int depth = 0;
        if (parent instanceof Collection) {
            parent = ictx.getStackObject(++depth);
        }
        return (SharedNestingBase)parent;
    }
}
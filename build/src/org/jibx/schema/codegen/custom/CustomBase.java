/*
 * Copyright (c) 2007-2008, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen.custom;

import java.util.Collection;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base class for all schema customizations. This defines a way to navigate up the tree of nested customizations without
 * making assumptions about the specific type of the containing components.
 * 
 * @author Dennis M. Sosnoski
 */
public class CustomBase
{
    /** Parent element (<code>null</code> if none). */
    private NestingCustomBase m_parent;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public CustomBase(NestingCustomBase parent) {
        m_parent = parent;
    }
    
    /**
     * Get container.
     * 
     * @return container
     */
    public NestingCustomBase getParent() {
        return m_parent;
    }
    
    /**
     * Set container.
     *
     * @param parent
     */
    protected void setParent(NestingCustomBase parent) {
        m_parent = parent;
    }
    
    /**
     * Get schema customizations parent.
     * 
     * @return schema customization
     */
    public SchemaRootBase getSchemaRoot() {
        NestingCustomBase parent = m_parent;
        while (!(parent instanceof SchemaRootBase)) {
            parent = parent.getParent();
        }
        return (SchemaRootBase)parent;
    }
    
    /**
     * Validate attributes of element. This is designed to be called during unmarshalling as part of the pre-set method
     * processing when a subclass instance is being created.
     * 
     * @param ictx unmarshalling context
     * @param attrs attributes array
     */
    protected void validateAttributes(IUnmarshallingContext ictx, StringArray attrs) {
        
        // setup for attribute access
        ValidationContext vctx = (ValidationContext)ictx.getUserContext();
        UnmarshallingContext uctx = (UnmarshallingContext)ictx;
        
        // loop through all attributes of current element
        for (int i = 0; i < uctx.getAttributeCount(); i++) {
            
            // check if nonamespace attribute is in the allowed set
            String name = uctx.getAttributeName(i);
            if (uctx.getAttributeNamespace(i).length() == 0) {
                if (attrs.indexOf(name) < 0) {
                    vctx.addWarning("Undefined attribute " + name, this);
                }
            }
        }
    }
    
    /**
     * Gets the parent element link from the unmarshalling stack. This method is for use by factories during
     * unmarshalling.
     * 
     * @param ictx unmarshalling context
     * @return containing class
     */
    protected static Object getContainingObject(IUnmarshallingContext ictx) {
        Object parent = ictx.getStackTop();
        if (parent instanceof Collection) {
            parent = ictx.getStackObject(1);
        }
        return parent;
    }
}
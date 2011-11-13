/*
 * Copyright (c) 2003-2008, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of JiBX nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.binding.def;

import java.util.ArrayList;

import org.jibx.binding.classes.ClassFile;
import org.jibx.runtime.JiBXException;

/**
 * Interface for mapping definitions.
 * 
 * @author Dennis M. Sosnoski
 */
public interface IMapping extends ILinkable
{
    /**
     * Get class name handled by mapping.
     * 
     * @return name of class bound by mapping
     */
    String getBoundType();
    
    /**
     * Get class name of type to be assumed for references to this mapping.
     * 
     * @return reference type class name name
     */
    String getReferenceType();
    
    /**
     * Get binding component implementing mapping. This call is only valid for
     * mappings with child components, not for mappings defined using
     * marshallers or unmarshallers.
     * 
     * @return binding component implementing this mapping
     */
    IComponent getImplComponent();
    
    /**
     * Get marshaller class used for mapping.
     * 
     * @return marshaller class information
     * @throws JiBXException if error in configuration
     */
    ClassFile getMarshaller() throws JiBXException;
    
    /**
     * Get unmarshaller class used for mapping.
     * 
     * @return unmarshaller class information
     * @throws JiBXException if error in configuration
     */
    ClassFile getUnmarshaller() throws JiBXException;
    
    /**
     * Get mapped element name.
     * 
     * @return mapped element name information (may be <code>null</code> if no
     * element name defined for mapping)
     */
    NameDefinition getName();
    
    /**
     * Get type name.
     * 
     * @return qualified type name, in text form (<code>null</code> if
     * unnamed)
     */
    String getTypeName();
    
    /**
     * Get the mapping name used in binding tables.
     *
     * @return name
     */
    String getMappingName();
    
    /**
     * Add namespace. This adds a namespace definition to those active for the
     * mapping.
     * 
     * @param ns namespace definition to be added
     * @throws JiBXException if error in defining namespace
     */
    void addNamespace(NamespaceDefinition ns) throws JiBXException;
    
    /**
     * Check if mapping is abstract.
     * 
     * @return <code>true</code> if an abstract mapping, <code>false</code>
     * if not
     */
    boolean isAbstract();
    
    /**
     * Check if mapping has extensions.
     * 
     * @return <code>true</code> if one or more mappings extend this mapping,
     * <code>false</code> if not
     */
    boolean isBase();
    
    /**
     * Add extension to abstract mapping. This call is only valid for abstract
     * mappings.
     * 
     * @param mdef extension mapping definition
     * @throws JiBXException if configuration error
     */
    void addExtension(MappingDefinition mdef) throws JiBXException;
    
    /**
     * Build reference to mapping. Constructs and returns the component for
     * handling the mapping.
     * 
     * @param parent containing binding definition structure
     * @param objc current object context
     * @param type mapped value type
     * @param prop property definition (may be <code>null</code>)
     * @return constructed mapping reference component
     * @throws JiBXException if configuration error
     */
    IComponent buildRef(IContainer parent, IContextObj objc, String type,
        PropertyDefinition prop) throws JiBXException;
    
    /**
     * Get namespaces defined for mapping.
     * 
     * @return namespace definitions (may be <code>null</code> if none)
     */
    ArrayList getNamespaces();
    
    /**
     * Generate required code for mapping.
     * 
     * @param force add marshaller/unmarshaller classes for abstract non-base
     * mappings flag (not passed on to children)
     * @throws JiBXException if error in transformation
     */
    void generateCode(boolean force) throws JiBXException;
    
    /**
     * Get the actual binding for a mapping. This is only usable with mappings
     * defined by a binding; if the mapping is instead defined by specifying
     * marshaller and unmarshaller classes this will just return null.
     *
     * @return binding structure, or <code>null</code> if none
     */
    ITypeBinding getBinding();
}
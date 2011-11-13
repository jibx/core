/*
 * Copyright (c) 2008, Dennis M. Sosnoski. All rights reserved.
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

import org.jibx.runtime.JiBXException;

/**
 * Interface for accessing methods used in a type binding. All the methods
 * accessed by this interface are public, static, methods.
 * 
 * @author Dennis M. Sosnoski
 */
public interface ITypeBinding
{
    /**
     * Get the method which creates an instance of the bound class if one does
     * not already exist. This method takes a reference to the bound type as the
     * first parameter and the unmarshalling context as the second parameter. If
     * the passed reference is non-<code>null</code> that reference will
     * simply be returned; otherwise, a new instance will be created and
     * returned. This method also handles any appropriate pre-set processing for
     * the instance.
     * 
     * @return create method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getCreateMethod() throws JiBXException;
    
    /**
     * Get the method which handles unmarshalling completion. This method takes
     * a reference to the bound type as the first parameter and the
     * unmarshalling context as the second parameter. There is no return value
     * from the method.
     * 
     * @return complete method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getCompleteMethod() throws JiBXException;
    
    /**
     * Get the method which handles preparation for marshalling an instance.
     * This method takes a reference to the bound type as the first parameter
     * and the unmarshalling context as the second parameter. There is no return
     * value from the method.
     * 
     * @return prepare method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getPrepareMethod() throws JiBXException;
    
    /**
     * Get the method which checks attributes to determine if an instance is
     * present. This method takes the unmarshalling context as the only
     * parameter. It returns <code>true</code> if an attribute of the mapping
     * is found, <code>false</code> if not.
     * 
     * @return test method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getAttributePresentTestMethod() throws JiBXException;
    
    /**
     * Get the method which unmarshals attributes into an instance. This method
     * takes a reference to the bound type as the first parameter and the
     * unmarshalling context as the second parameter. The return value is the
     * unmarshalled instance, which may not be the same instance as was passed
     * as a parameter.
     * 
     * @return attribute unmarshal method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getAttributeUnmarshalMethod() throws JiBXException;
    
    /**
     * Get the method which marshals attributes from an instance. This method
     * takes a reference to the bound type as the first parameter and the
     * unmarshalling context as the second parameter. There is no return value
     * from the method.
     * 
     * @return attribute marshal method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getAttributeMarshalMethod() throws JiBXException;
    
    /**
     * Get the method which checks child elements to determine if an instance is
     * present. This method takes the unmarshalling context as the only
     * parameter. It returns <code>true</code> if an attribute of the mapping
     * is found, <code>false</code> if not.
     * 
     * @return content test method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getContentPresentTestMethod() throws JiBXException;
    
    /**
     * Get the method which unmarshals child elements and character data content
     * into an instance. This method takes a reference to the bound type as the
     * first parameter and the unmarshalling context as the second parameter.
     * There is no return value from the method. The return value is the
     * unmarshalled instance, which may not be the same instance as was passed
     * as a parameter.
     * 
     * @return content unmarshal method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getContentUnmarshalMethod() throws JiBXException;
    
    /**
     * Get the method which marshals child elements and character data content
     * from an instance. This method takes a reference to the bound type as the
     * first parameter and the unmarshalling context as the second parameter.
     * There is no return value from the method.
     * 
     * @return content marshal method, or <code>null</code> if none
     * @throws JiBXException on error in code generation
     */
    String getContentMarshalMethod() throws JiBXException;
}
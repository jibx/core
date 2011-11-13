/*
Copyright (c) 2003-2008, Dennis M. Sosnoski.
All rights reserved.

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

package org.jibx.binding.def;

import org.jibx.binding.classes.ContextMethodBuilder;
import org.jibx.runtime.JiBXException;

/**
 * Child component (attribute or content) interface definition. This interface
 * provides the basic hooks for generating code from the binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public interface IComponent extends ILinkable
{
    /**
     * Check if component is an optional item.
     *
     * @return <code>true</code> if optional, <code>false</code> if required
     */
    public boolean isOptional();
    
    /**
     * Check if component defines one or more attribute values of the
     * containing element.
     *
     * @return <code>true</code> if one or more attribute values defined for
     * containing element, <code>false</code> if not
     */
    public boolean hasAttribute();
    
    /**
     * Generate code to test for attribute present. This generates code that
     * tests if a child is present as determined by attributes of the containing
     * start tag. It leaves the result of the test (zero if missing, nonzero if
     * present) on the stack. This call is only valid if this component has one
     * or more attributes for the containing element.
     *
     * @param mb method builder
     * @throws JiBXException if configuration error
     */
    public void genAttrPresentTest(ContextMethodBuilder mb)
        throws JiBXException;
    
    /**
     * Generate attribute unmarshalling code. This is called within the code
     * generation for the unmarshaller of the class associated with the
     * containing element. It needs to generate the necessary code for handling
     * the unmarshalling operation, leaving the unmarshalled object
     * reference on the stack.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException;

    /**
     * Generate attribute marshalling code. This is called within the code
     * generation for the marshaller of the class associated with the
     * containing element. It needs to generate the necessary code for handling
     * the marshalling operation, consuming the marshalled object
     * reference from the stack.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException;

    /**
     * Check if component defines one or more elements or text values as
     * children of the containing element. This method is only valid after the
     * call to {@link #setLinkages()}.
     *
     * @return <code>true</code> if one or more content values defined
     * for containing element, <code>false</code> if not
     */
    public boolean hasContent();
    
    /**
     * Generate code to test for content present. This generates code that
     * tests if a required element is present, leaving the result of the test
     * (zero if missing, nonzero if present) on the stack. This call is only
     * valid if this component has one or more content components for the
     * containing element.
     *
     * @param mb method builder
     * @throws JiBXException if configuration error
     */
    public void genContentPresentTest(ContextMethodBuilder mb)
        throws JiBXException;

    /**
     * Generate element or text unmarshalling code. This is called within the
     * code generation for the unmarshaller of the class associated with the
     * containing element. It needs to generate the necessary code for
     * handling the unmarshalling operation, leaving the unmarshalled object
     * reference on the stack.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException;

    /**
     * Generate element or text marshalling code. This is called within the
     * code generation for the marshaller of the class associated with the
     * containing element. It needs to generate the necessary code for
     * handling the marshalling operation, consuming the marshalled object
     * reference from the stack.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genContentMarshal(ContextMethodBuilder mb) throws JiBXException;

    /**
     * Generate code to create new instance of object. This is called within the
     * code generation for the unmarshaller of the class associated with the
     * containing element. It needs to generate the necessary code for creating
     * an instance of the object to be unmarshalled, leaving the object
     * reference on the stack.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genNewInstance(ContextMethodBuilder mb) throws JiBXException;
    
    /**
     * Get type expected by component.
     *
     * @return fully qualified class name of expected type
     */
    public String getType();
    
    /**
     * Check if component defines an ID value for instances of context object.
     *
     * @return <code>true</code> if ID value defined for instances,
     * <code>false</code> if not
     */
    public boolean hasId();

    /**
     * Generate code to load ID value of instance to stack. The generated code
     * should assume that the top of the stack is the reference for the
     * containing object. It must consume this and leave the actual ID value
     * on the stack (as a <code>String</code>).
     *
     * @param mb method builder
     * @throws JiBXException if configuration error
     */
    public void genLoadId(ContextMethodBuilder mb) throws JiBXException;
    
    /**
     * Get element wrapper name. If the component defines an element as the
     * container for content, this returns the name information for that
     * element.
     *
     * @return component element name, <code>null</code> if no wrapper element
     */
    public NameDefinition getWrapperName();
    
    // DEBUG
    public void print(int depth);
}
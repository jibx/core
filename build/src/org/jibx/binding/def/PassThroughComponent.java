/*
Copyright (c) 2003, Dennis M. Sosnoski
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
 * Default component decorator. This just passes through all method calls to
 * the wrapped component.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class PassThroughComponent implements IComponent
{
    /** Property value binding component. */
    protected IComponent m_component;

    /**
     * No argument constructor. This requires the component to be set later,
     * using the {@link #setWrappedComponent} method.
     */

    protected PassThroughComponent() {}

    /**
     * Constructor.
     *
     * @param comp wrapped component
     */

    protected PassThroughComponent(IComponent comp) {
        m_component = comp;
    }

    /**
     * Set the wrapped component.
     *
     * @param comp wrapped component
     */

    protected void setWrappedComponent(IComponent comp) {
        m_component = comp;
    }
    
    //
    // IComponent interface method definitions

    public boolean isOptional() {
        return m_component.isOptional();
    }

    public boolean hasAttribute() {
        return m_component.hasAttribute();
    }

    public void genAttrPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        m_component.genAttrPresentTest(mb);
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        m_component.genAttributeUnmarshal(mb);
    }

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        m_component.genAttributeMarshal(mb);
    }

    public boolean hasContent() {
        return m_component.hasContent();
    }

    public void genContentPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        m_component.genContentPresentTest(mb);
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        m_component.genContentUnmarshal(mb);
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        m_component.genContentMarshal(mb);
    }
    
    public void genNewInstance(ContextMethodBuilder mb) throws JiBXException {
        m_component.genNewInstance(mb);
    }

    public String getType() {
        return m_component.getType();
    }

    public boolean hasId() {
        return m_component.hasId();
    }

    public void genLoadId(ContextMethodBuilder mb) throws JiBXException {
        m_component.genLoadId(mb);
    }
    
    public NameDefinition getWrapperName() {
        return m_component.getWrapperName();
    }

    public void setLinkages() throws JiBXException {
        m_component.setLinkages();
    }
    
    // DEBUG
    public void print(int depth) {
        throw new IllegalStateException
            ("Method must be overridden by subclass" +
            getClass().getName());
    }
}

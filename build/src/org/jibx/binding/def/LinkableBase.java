/*
Copyright (c) 2003-2008, Dennis M. Sosnoski
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

import org.jibx.runtime.JiBXException;

/**
 * Base class for components that can be linked from multiple locations within
 * the binding definition structure. The implemented basic behavior is a simple
 * pass-through component, with the addition of recursion checking during the
 * linking phase.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class LinkableBase extends PassThroughComponent
{
    /** Flag for linkage in progress. */
    private boolean m_isLinking;
    
    /** Flag for linkage complete. */
    private boolean m_isLinked;

    /**
     * No argument constructor. This requires the component to be set later,
     * using the {@link
     * org.jibx.binding.def.PassThroughComponent#setWrappedComponent} method.
     */
    protected LinkableBase() {}

    /**
     * Constructor.
     *
     * @param wrap wrapped binding component
     */
    public LinkableBase(IComponent wrap) {
        super(wrap);
    }

    /**
     * Handler for recursion. If recursion is found during linking this method
     * will be called. The base class implementation does nothing, but may be
     * overridden by subclases to implement the appropriate behavior.
     */
    protected void handleRecursion() {}
    
    /**
     * Check if linkage processing for this component is complete.
     *
     * @return <code>true</code> if complete, <code>false</code> if not
     */
    protected boolean isLinked() {
        return m_isLinked;
    }

    //
    // IComponent interface method definitions

    // subclasses should call the base class implementation if they override
    //  this method
    public void setLinkages() throws JiBXException {
        if (m_isLinking) {
            handleRecursion();
        } else {
            if (!m_isLinked) {
                m_isLinking = true;
                m_component.setLinkages();
                m_isLinking = false;
                m_isLinked = true;
            }
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.println("linkable wrapper");
        m_component.print(depth+1);
    }
}
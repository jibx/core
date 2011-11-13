/*
Copyright (c) 2003-2004, Dennis M. Sosnoski
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

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Property reference with marshaller and unmarshaller. This handles loading
 * and storing the property value, calling the supplied marshaller and
 * unmarshaller for all else.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public class DirectProperty implements IComponent
{
    /** Property definition. */
    private final PropertyDefinition m_property;
    
    /** Property value direct binding. */
    private final DirectObject m_direct;

    /**
     * Constructor.
     *
     * @param prop property definition
     * @param direct object direct binding information
     */
    public DirectProperty(PropertyDefinition prop, DirectObject direct) {
        m_property = prop;
        m_direct = direct;
    }
    
    //
    // IComponent interface method definitions

    public boolean isOptional() {
        return m_property.isOptional();
    }

    public boolean hasAttribute() {
        return false;
    }

    public void genAttrPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        throw new IllegalStateException
            ("Internal error - no attributes allowed");
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        throw new IllegalStateException
            ("Internal error - no attributes allowed");
    }

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        throw new IllegalStateException
            ("Internal error - no attributes allowed");
    }

    public boolean hasContent() {
        return true;
    }

    public void genContentPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        m_direct.genContentPresentTest(mb);
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        
        // check type of property binding
        if (m_property.isImplicit()) {
            
            // load null to force create of new object for item in collection
            mb.appendACONST_NULL();
            m_direct.genContentUnmarshal(mb);
            
        } else if (m_property.isThis()) {
            
            // load existing object to unmarshal for "this" reference
            mb.loadObject();
            m_direct.genContentUnmarshal(mb);
            
        } else {
            
        
            // start by generating code to load owning object so can finish by
            //  storing to property
            mb.loadObject();
            BranchWrapper ifpres = null;
            BranchWrapper tosave = null;
            if (m_property.isOptional()) {
            
                // generate code to check presence for the case of an optional
                //  item, with branch if so; if not present, set a null value
                //  with branch to be targeted at property store.
                m_direct.genContentPresentTest(mb);
                ifpres = mb.appendIFNE(this);
                mb.appendACONST_NULL();
                tosave = mb.appendUnconditionalBranch(this);
            }
            
            // generate unmarshalling code for not optional, or optional and
            //  present; get existing instance or create a new one and handle
            //  attribute unmarshalling
            mb.targetNext(ifpres);
            mb.loadObject();
            m_property.genLoad(mb);
            m_direct.genContentUnmarshal(mb);
            mb.appendCreateCast(m_property.getSetValueType());
            mb.targetNext(tosave);
            m_property.genStore(mb);
        }
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
    
        // check type of property binding
        if (m_property.isImplicit()) {
        
            // marshal object already loaded on stack
            m_direct.genContentMarshal(mb);
            
        } else if (m_property.isThis()) {
            
            // load object to marshal for "this" reference
            mb.loadObject();
            m_direct.genContentMarshal(mb);
            
        } else {
        
            // start by generating code to load the actual object reference
            mb.loadObject();
            m_property.genLoad(mb);
            BranchWrapper missing = null;
            if (m_property.isOptional()) {
            
                // generate code to check null for the case of an optional item,
                //  with branch if so; if present, just reload object and fall
                //  through.
                missing = mb.appendIFNULL(this);
                mb.loadObject();
                m_property.genLoad(mb);
            }
        
            // generate code for actual marshalling if not optional, or optional
            //  and nonnull; then finish by setting target for optional with
            //  null value case
            m_direct.genContentMarshal(mb);
            mb.targetNext(missing);
        }
    }
    
    public void genNewInstance(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no instance creation");
    }
    
    public String getType() {
        return m_property.getTypeName();
    }

    public boolean hasId() {
        return false;
    }

    public void genLoadId(ContextMethodBuilder mb) throws JiBXException {
        throw new IllegalStateException("Internal error - no id defined");
    }
    
    public NameDefinition getWrapperName() {
        return m_direct.getWrapperName();
    }

    public void setLinkages() throws JiBXException {
        m_direct.setLinkages();
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        if (m_property == null) {
            System.out.println("direct implicit property");
        } else {
            System.out.println("direct property using " +
                m_property.toString());
        }
        m_direct.print(depth+1);
    }
}

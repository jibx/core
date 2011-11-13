/*
Copyright (c) 2003-2009, Dennis M. Sosnoski.
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
import org.jibx.binding.util.IntegerCache;
import org.jibx.runtime.JiBXException;

/**
 * Property reference with binding defined by component. This handles loading
 * and storing the property value, calling the wrapped component methods for
 * everything else.
 *
 * @author Dennis M. Sosnoski
 */
public class ComponentProperty extends PassThroughComponent
{
    /** Property definition. */
    private final PropertyDefinition m_property;

    /** Skip marshalling code tests flag. */
    private boolean m_skipMarshal;
    
    /** Fake content to force unmarshal to create an object. */
    private boolean m_forceUnmarshal;

    /**
     * Constructor.
     *
     * @param prop actual property definition
     * @param impl component that defines marshalling and unmarshalling
     * @param skip flag for marshalling code tests to be skipped
     */
    public ComponentProperty(PropertyDefinition prop, IComponent impl,
        boolean skip) {
        super(impl);
        m_property = prop;
        m_skipMarshal = skip;
    }
    
    /**
     * Set flag for skipping marshalling presence test code generation.
     * 
     * @param skip <code>true</code> if skipping, <code>false</code> if not
     */
    public void setSkipping(boolean skip) {
        m_skipMarshal = skip;
    }
    
    /**
     * Set flag to force unmarshalling to create an object.
     * 
     * @param force <code>true</code> if skipping, <code>false</code> if not
     */
    public void setForceUnmarshal(boolean force) {
        m_forceUnmarshal = force;
    }
    
    /**
     * Get the property information. This is a kludge used by the ElementWrapper
     * code to store a <code>null</code> value directly to the property when
     * unmarshalling a missing or xsi:nil element.
     * 
     * @return property information
     */
    public PropertyDefinition getProperty() {
        return m_property;
    }
    
    /**
     * Generate the code to load, and if necessary create, the object instance
     * to be unmarshalled.
     *
     * @param mb
     * @throws JiBXException
     */
    private void genLoadUnmarshalInstance(ContextMethodBuilder mb)
        throws JiBXException {
        
        // get the actual 
        if (m_property.isImplicit()) {
            
            // just create a new instance for an item from a collection
            mb.appendACONST_NULL();
            m_component.genNewInstance(mb);
            
        } else if (m_property.isThis()) {
            
            // just initialize the existing instance
            mb.loadObject();
            m_component.genNewInstance(mb);
            
        } else {
            
            // create or initialize the object instance for unmarshalling
            mb.loadObject();
            m_property.genLoad(mb);
            mb.appendCreateCast(m_property.getGetValueType(),
                m_component.getType());
            m_component.genNewInstance(mb);
            
        }
    }

    /**
     * Generate the code to store the unmarshalled object instance.
     *
     * @param mb
     * @throws JiBXException
     */
    private void genStoreUnmarshalInstance(ContextMethodBuilder mb)
        throws JiBXException {
        
        // convert the type if necessary, then store result to property
        mb.appendCreateCast(m_component.getType(),
            m_property.getSetValueType());
        if (!m_property.isThis()) {
            if (!m_property.isImplicit()) {
                m_property.genStore(mb);
            }
        }
    }
    
    //
    // IComponent interface method definitions (overrides of defaults)

    public boolean isOptional() {
        return m_property.isOptional();
    }

    public boolean hasContent() {
        return m_forceUnmarshal || super.hasContent();
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        
        // load object reference for storing result if needed
        boolean thisref = m_property.isThis();
        if (!m_property.isImplicit() && !thisref &&
            (!super.hasContent() || m_property.isLoadable())) {
            mb.loadObject();
        }
        BranchWrapper tosave = null;
        if (m_property.isOptional() && !thisref) {
            
            // branch to unmarshal if attribute(s) present; if not present,
            //  set a null reference and branch to property store.
            m_component.genAttrPresentTest(mb);
            BranchWrapper ifpres = mb.appendIFNE(this);
            mb.appendACONST_NULL();
            tosave = mb.appendUnconditionalBranch(this);
            mb.targetNext(ifpres);
        }
        
        // handle attribute unmarshalling
        genLoadUnmarshalInstance(mb);
        m_component.genAttributeUnmarshal(mb);
        mb.targetNext(tosave);
        
        // check if content also present
        if (super.hasContent() && !m_property.isLoadable()) {
            if (!m_property.isImplicit() && !thisref) {
                
                // let the content unmarshalling store the instance
                int slot = mb.addLocal(null,
                    ClassItem.typeFromName(m_component.getType()));
                Integer index = IntegerCache.getInteger(slot);
                mb.setKeyValue(this, index);
                
            }
        } else {
            
            // no content present or loadable, just store directly
            genStoreUnmarshalInstance(mb);
            
        }
    }

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_skipMarshal) {
            
            // just generate pass-through marshal code generation
            mb.appendCreateCast(m_component.getType());
            m_component.genAttributeMarshal(mb);
            
        } else {
        
            // start by generating code to load the actual object reference
            if (!m_property.isImplicit()) {
                mb.loadObject();
                m_property.genLoad(mb);
            }
            BranchWrapper ifpres = null;
            BranchWrapper toend = null;
            if (m_property.isOptional()) {
            
                // generate code to check nonnull for the case of an optional item,
                //  with branch if so; if not present, just pop the copy with branch
                //  to be targeted past end.
                mb.appendDUP();
                ifpres = mb.appendIFNONNULL(this);
                mb.appendPOP();
                toend = mb.appendUnconditionalBranch(this);
            }
        
            // generate code for actual marshalling if not optional, or optional and
            //  nonnull; then finish by setting target for optional with
            //  null value case
            mb.targetNext(ifpres);
            m_component.genAttributeMarshal(mb);
            mb.targetNext(toend);
        }
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        
        // first check that there really is content to be unmarshalled
        if (!m_forceUnmarshal || super.hasContent()) {
            
            // load object reference for storing result if needed
            boolean thisref = m_property.isThis();
            if (!m_property.isImplicit() && !thisref) {
                mb.loadObject();
            }
            BranchWrapper tostore = null;
            BranchWrapper toloaded = null;
            if (m_property.isOptional() && !thisref) {
                
                // see if already checked for attributes present
                if (hasAttribute()) {
                    
                    // attributes included, so if reference is non-null it means
                    //  the attributes were processed and the reference can just
                    //  be used without (re)initialization
                    if (!m_property.isImplicit()) {
                        if (m_property.isLoadable()) {
                            mb.loadObject();
                            m_property.genLoad(mb);
                        } else {
                            Integer index = (Integer)mb.getKeyValue(this);
                            mb.appendLoadLocal(index.intValue());
                        }
                    }
                    mb.appendCreateCast(m_component.getType());
                    mb.appendDUP();
                    toloaded = mb.appendIFNONNULL(this);
                    mb.appendPOP();
                    
                }
                
                // now check if content present
                m_component.genContentPresentTest(mb);
                BranchWrapper havecont = mb.appendIFNE(this);
                
                // force store of null reference if content not found
                mb.appendACONST_NULL();
                tostore = mb.appendUnconditionalBranch(this);
                
                // load reference and initialize existing instance, or create a
                //  new instance if none exists
                mb.targetNext(havecont);
                genLoadUnmarshalInstance(mb);
                
            } else if (hasAttribute()) {
                
                // generate code to just load the reference, which was created
                //  or initialized during attribute unmarshalling 
                if (!m_property.isImplicit() && !thisref) {
                    if (m_property.isLoadable()) {
                        mb.loadObject();
                        m_property.genLoad(mb);
                    } else {
                        Integer index = (Integer)mb.getKeyValue(this);
                        mb.appendLoadLocal(index.intValue());
                    }
                }
                mb.appendCreateCast(m_component.getType());
                
            } else {
                
                // create or initialize instance to be unmarshalled
                genLoadUnmarshalInstance(mb);
                
            }
            
            // unmarshal the content to the instance, and store it
            mb.targetNext(toloaded);
            m_component.genContentUnmarshal(mb);
            mb.targetNext(tostore);
            genStoreUnmarshalInstance(mb);
        }
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_forceUnmarshal) {
            if (m_property.isImplicit()) {
                
                // discard the object reference
                mb.appendPOP();
                
            }
        } else {
            if (m_skipMarshal) {
                    
                    // just generate pass-through marshal code generation
                    mb.appendCreateCast(m_component.getType());
                    m_component.genContentMarshal(mb);
                    
            } else {
                
                // start by generating code to load the actual object reference
                if (!m_property.isImplicit()) {
                    mb.loadObject();
                    m_property.genLoad(mb);
                }
                BranchWrapper ifpres = null;
                BranchWrapper tonext = null;
                if (m_property.isOptional()) {
                    
                    // generate code to check nonull for the case of an optional item,
                    //  with branch if so; if not present, just pop the copy with branch
                    //  to be targeted past end.
                    mb.appendDUP();
                    ifpres = mb.appendIFNONNULL(this);
                    mb.appendPOP();
                    tonext = mb.appendUnconditionalBranch(this);
                }
                
                // generate code for actual marshalling if not optional, or optional and
                //  nonnull; then finish by setting target for optional with null value
                //  case
                mb.targetNext(ifpres);
                m_component.genContentMarshal(mb);
                mb.targetNext(tonext);
                
            }
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("component " + m_property.toString());
        if (m_skipMarshal) {
            System.out.print(" (pass-through marshal)");
        }
        System.out.println();
        m_component.print(depth+1);
    }
}
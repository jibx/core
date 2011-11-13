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

import java.util.ArrayList;

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Structure binding definition. This handles one or more child components,
 * which may be ordered or unordered.
 *
 * @author Dennis M. Sosnoski
 */
public class NestedStructure extends NestedBase
{
    //
    // Method definitions used in code generation
    
    private static final String CHECK_ISSTART_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.isStart";
    private static final String CHECK_ISSTART_SIGNATURE = "()Z";
    private static final String SKIP_ELEMENT_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.skipElement";
    private static final String SKIP_ELEMENT_SIGNATURE = "()V";
    private static final String THROW_EXCEPTION_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.throwNameException";
    private static final String THROW_EXCEPTION_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
    
    //
    // Instance data
    
    /** Child supplying ID for bound class. */
    private IComponent m_idChild;

    /** Flag for choice of child content (used by subclasses). */
    protected final boolean m_isChoice;
    
    /** Flag for duplicate values allowed when unmarshalling unordered group. */
    private final boolean m_allowDuplicates;
    
    /** Flag for structure has associated object. */
    private boolean m_hasObject;
    
    /** Flag for already linked (to avoid multiple passes). */
    private boolean m_isLinked;

    /**
     * Constructor.
     *
     * @param parent containing binding definition context
     * @param objc current object context
     * @param ord ordered content flag
     * @param choice choice content flag
     * @param flex flexible element handling flag
     * @param ctx define context for structure flag
     * @param hasobj has associated object flag
     * @param dupl allow duplicates in unordered group flag
     */
    public NestedStructure(IContainer parent, IContextObj objc,
        boolean ord, boolean choice, boolean flex, boolean ctx,
        boolean hasobj, boolean dupl) {
        super(parent, objc, ord, flex, ctx);
        m_isChoice = choice;
        m_hasObject = hasobj;
        m_allowDuplicates = dupl;
    }
    
    /**
     * Set the object context.
     * 
     * @param objc object context
     */
    public void setObjectContext(IContextObj objc) {
        m_hasObject = false;
    }
    
    /**
     * Check if the structure is just a mapping reference. This is used to
     * short-circuit the code generation to avoid multiple layers of binding
     * methods. As written, the result is only valid prior to a call to
     * {@link #setLinkages()}.
     *
     * @return <code>true</code> if a mapping reference, <code>false</code> if
     * not
     */
    public boolean isMappingReference() {
        return m_contents.size() == 1 &&
            m_contents.get(0) instanceof MappingReference;
    }
    
    //
    // IComponent interface method definitions
    
    public void genNewInstance(ContextMethodBuilder mb) {
        if (isMappingReference()) {
            throw new IllegalStateException
                ("Internal error - no instance creation");
        }
    }

    public boolean hasAttribute() {
        return m_attributes != null && m_attributes.size() > 0;
    }

    public void genAttrPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_attributes != null && m_attributes.size() > 0) {
            
            // if single possibility just test it directly
            int count = m_attributes.size();
            if (count == 1) {
                ((IComponent)m_attributes.get(0)).genAttrPresentTest(mb);
            } else {
                
                // generate code for chained test with branches to found exit
                BranchWrapper[] tofound = new BranchWrapper[count];
                for (int i = 0; i < count; i++) {
                    IComponent comp = (IComponent)m_attributes.get(i);
                    comp.genAttrPresentTest(mb);
                    tofound[i] = mb.appendIFNE(this);
                }
                
                // fall off end of loop to push "false" on stack and jump to end
                mb.appendICONST_0();
                BranchWrapper toend = mb.appendUnconditionalBranch(this);
                
                // generate found target to push "true" on stack and continue
                for (int i = 0; i < count; i++) {
                    mb.targetNext(tofound[i]);
                }
                mb.appendICONST_1();
                mb.targetNext(toend);
                
            }
        } else {
            throw new IllegalStateException
                ("Internal error - no attributes present");
        }
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_attributes != null && m_attributes.size() > 0) {
            for (int i = 0; i < m_attributes.size(); i++) {
                IComponent attr = (IComponent)m_attributes.get(i);
                attr.genAttributeUnmarshal(mb);
            }
        } else {
            throw new IllegalStateException
                ("Internal error - no attributes present");
        }
    }

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_attributes != null && m_attributes.size() > 0) {
            for (int i = 0; i < m_attributes.size(); i++) {
                IComponent attr = (IComponent)m_attributes.get(i);
                attr.genAttributeMarshal(mb);
            }
        } else {
            throw new IllegalStateException
                ("Internal error - no attributes present");
        }
    }

    public boolean hasContent() {
        return m_contents.size() > 0;
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_contents.size() > 0) {
        
            // check for ordered or unordered content
            if (m_isOrdered) {
                
                // should never get here on choice, but just in case
                if (m_isChoice) {
                    throw new IllegalStateException("Internal error - generation model uses ordered choice");
                }
            
                // just generate unmarshal code for each component in order
                for (int i = 0; i < m_contents.size(); i++) {
                    IComponent child = (IComponent)m_contents.get(i);
                    child.genContentUnmarshal(mb);
                }
                
            } else {
                
                // start by finding the number of required elements
                int count = m_contents.size();
                int nreq = 0;
                boolean dupl = m_allowDuplicates;
                if (m_isChoice) {
                    dupl = true;
                } else {
                    for (int i = 0; i < count; i++) {
                        if (!((IComponent)m_contents.get(i)).isOptional()) {
                            nreq++;
                        }
                    }
                }
                
                // create array for tracking elements seen
                boolean useflag = nreq > 0 || !dupl;
                if (useflag) {
                    mb.appendLoadConstant(count);
                    mb.appendCreateArray("boolean");
                    mb.defineSlot(this, ClassItem.typeFromName("boolean[]"));
                }
            
                // generate unmarshal loop code that checks for each component,
                //  branching to the next component until one is found and
                //  exiting the loop only when no component is matched (or in
                //  the case of flexible unmarshalling, only exiting the loop
                //  when the enclosing end tag is seen). this uses the array(s)
                //  of booleans to track elements seen and detect duplicates.
                BranchWrapper link = null;
                // TODO: initialize default values
                BranchTarget first = mb.appendTargetNOP();
                BranchWrapper[] toends;
                if (m_isChoice) {
                    toends = new BranchWrapper[count+1];
                } else {
                    toends = new BranchWrapper[1];
                }
                for (int i = 0; i < count; i++) {
                    
                    // start with basic test code
                    if (link != null) {
                        mb.targetNext(link);
                    }
                    IComponent child = (IComponent)m_contents.get(i);
                    child.genContentPresentTest(mb);
                    link = mb.appendIFEQ(this);
                    
                    // check for duplicate (if enforced)
                    if (!dupl) {
                        genFlagTest(true, i, "Duplicate element ",
                            child.getWrapperName(), mb);
                    }
                    
                    // set flag for element seen
                    if (useflag && !(child.isOptional() && dupl)) {
                        mb.appendLoadLocal(mb.getSlot(this));
                        mb.appendLoadConstant(i);
                        mb.appendLoadConstant(1);
                        mb.appendASTORE("boolean");
                    }
                    
                    // generate actual unmarshalling code
                    child.genContentUnmarshal(mb);
                    BranchWrapper next = mb.appendUnconditionalBranch(this);
                    if (m_isChoice) {
                        toends[i+1] = next;
                    } else {
                        next.setTarget(first, mb);
                    }
                }
                
                // handle comparison fall through depending on flexible flag 
                if (m_isFlexible) {
                    if (link != null) {
                        
                        // exit loop if not positioned at element start
                        mb.targetNext(link);
                        mb.loadContext();
                        mb.appendCallVirtual(CHECK_ISSTART_NAME,
                            CHECK_ISSTART_SIGNATURE);
                        toends[0] = mb.appendIFEQ(this);
                        
                        // ignore unknown element and loop back to start
                        mb.loadContext();
                        mb.appendCallVirtual(SKIP_ELEMENT_NAME,
                            SKIP_ELEMENT_SIGNATURE);
                        mb.appendUnconditionalBranch(this).setTarget(first, mb);
                        
                    }
                } else {
                    
                    // set final test failure branch to fall through loop
                    toends[0] = link;
                }
                
                // patch all branches that exit loop
                mb.targetNext(toends);
                
                // handle required element present tests
                if (nreq > 0) {
                    for (int i = 0; i < count; i++) {
                        IComponent child = (IComponent)m_contents.get(i);
                        if (!child.isOptional()) {
                            genFlagTest(false, i, "Missing required element ",
                                child.getWrapperName(), mb);
                        }
                    }
                }
                mb.freeSlot(this);
            
            }
        } else {
            throw new IllegalStateException
                ("Internal error - no content present");
        }
    }

    /**
     * Helper method to generate test code for value in boolean array. If the
     * test fails, the generated code throws an exception with the appropriate
     * error message.
     * 
     * @param cond flag setting resulting in exception
     * @param pos position of element in list of child components
     * @param msg basic error message when test fails
     * @param name
     * @param mb
     */
    private void genFlagTest(boolean cond, int pos, String msg,
        NameDefinition name, ContextMethodBuilder mb) {
        
        // generate code to load array item value
        mb.appendLoadLocal(mb.getSlot(this));
        mb.appendLoadConstant(pos);
        mb.appendALOAD("boolean");
        
        // append branch for case where test is passed
        BranchWrapper ifgood;
        if (cond) {
            ifgood = mb.appendIFEQ(this);
        } else {
            ifgood = mb.appendIFNE(this);
        }
        
        // generate exception for test failed
        mb.loadContext();
        mb.appendLoadConstant(msg);
        if (name == null) {
            mb.appendACONST_NULL();
            mb.appendLoadConstant("(unknown name, position " + pos +
                " in binding structure)");
        } else {
            name.genPushUriPair(mb);
        }
        mb.appendCallVirtual(THROW_EXCEPTION_NAME, THROW_EXCEPTION_SIGNATURE);
        
        // set target for success branch on test
        mb.targetNext(ifgood);
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_contents.size() > 0) {
            for (int i = 0; i < m_contents.size(); i++) {
                IComponent content = (IComponent)m_contents.get(i);
                content.genContentMarshal(mb);
            }
        } else {
            throw new IllegalStateException
                ("Internal error - no content present");
        }
    }

    public String getType() {
        if (m_hasObject) {
            return super.getType();
        } else if (m_attributes != null && m_attributes.size() > 0) {
            return ((IComponent)m_attributes.get(0)).getType();
        } else if (m_contents.size() > 0) {
            return ((IComponent)m_contents.get(0)).getType();
        } else {
            throw new IllegalStateException("Internal error - " +
                "no type defined for structure");
        }
    }
    
    public boolean hasId() {
        return m_idChild != null;
    }
    
    public void genLoadId(ContextMethodBuilder mb) throws JiBXException {
        if (m_idChild == null) {
            throw new IllegalStateException("No ID child defined");
        } else {
            m_idChild.genLoadId(mb);
        }
    }

    public void setLinkages() throws JiBXException {
        if (!m_isLinked) {
            
            // set flag first in case of recursive reference
            m_isLinked = true;
        
            // process all child components to link and sort by type
            int i = 0;
            while (i < m_contents.size()) {
                IComponent comp = (IComponent)m_contents.get(i);
                comp.setLinkages();
                if (comp.hasAttribute()) {
                    if (m_attributes == null) {
                        m_attributes = new ArrayList();
                    }
                    m_attributes.add(comp);
                }
                if (!comp.hasContent()) {
                    m_contents.remove(i);
                } else {
                    i++;
                }
            }
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("structure " +
            (m_isChoice ? "choice" : (m_isOrdered ? "ordered" : "unordered")));
        if (m_allowDuplicates) {
            System.out.print(", duplicates allowed");
        }
        if (isFlexible()) {
            System.out.print(", flexible");
        }
        if (m_idChild != null) {
            System.out.print(" (ID)");
        }
        System.out.println();
        for (int i = 0; i < m_contents.size(); i++) {
            IComponent comp = (IComponent)m_contents.get(i);
            comp.print(depth+1);
        }
        if (m_attributes != null) {
            for (int i = 0; i < m_attributes.size(); i++) {
                IComponent comp = (IComponent)m_attributes.get(i);
                comp.print(depth+1);
            }
        }
    }
}
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

import java.util.ArrayList;

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Base class for structure and collection binding definitions. This handles one
 * or more child components, which may be ordered or unordered.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class NestedBase extends BindingBuilder.ContainerBase
implements IComponent, IContainer
{
    /** Context object for this definition. */
    private IContextObj m_contextObject;
    
    /** Flag for context defined at level. */
    private final boolean m_hasContext;

    /** Flag for ordered child content (used by subclasses). */
    protected final boolean m_isOrdered;

    /** Flag for flexible element handling (used by subclasses). */
    protected final boolean m_isFlexible;
    
    /** Definition context for container (may be same as parent). */
    private final DefinitionContext m_defContext;

    /** Included attribute definitions (lazy create, only if needed). */
    protected ArrayList m_attributes;

    /** Nested content definitions (initially used for all child components). */
    protected ArrayList m_contents;

    /**
     * Constructor.
     *
     * @param contain containing binding definition context
     * @param objc current object context
     * @param ord ordered content flag
     * @param flex flexible element handling flag
     * @param defc define context for structure flag
     */
    public NestedBase(IContainer contain, IContextObj objc,
        boolean ord, boolean flex, boolean defc) {
        
        // set base class defaults
        super(contain);
        m_styleDefault = m_autoLink = m_accessLevel = m_nameStyle = -1;

        // initialize members at this level
        m_contextObject = objc;
        m_contents = new ArrayList();
        m_isOrdered = ord;
        m_isFlexible = flex;
        m_hasContext = defc;
        if (defc) {
            m_defContext = new DefinitionContext(contain);
        } else {
            m_defContext = contain.getDefinitionContext();
        }
    }
    
    /**
     * Set the object context.
     * 
     * @param objc object context
     */
    public void setObjectContext(IContextObj objc) {
        m_contextObject = objc;
    }
    
    /**
     * Get the attribute children of this mapping.
     *
     * @return list of attribute children (<code>null</code> if none; should not
     * be modified)
     */
    public ArrayList getAttributes() {
        return m_attributes;
    }
    
    /**
     * Get the content children of this mapping.
     *
     * @return list of content children (should not be modified)
     */
    public ArrayList getContents() {
        return m_contents;
    }
    
    /**
     * Add child component to nested structure. All components are initially
     * assumed to contain content. When {@link #setLinkages} is called the
     * components are checked to determine whether they actually supply
     * attribute(s), content, or both.
     *
     * @param comp child component to be added to structure
     */
    public void addComponent(IComponent comp) {
        m_contents.add(comp);
    }
    
    /**
     * Check if flexible unmarshalling.
     * 
     * @return flexible flag
     */
    public boolean isFlexible() {
        return m_isFlexible;
    }
    
    //
    // IContainer interface method definitions

    public boolean isContentOrdered() {
        return m_isOrdered;
    }

    public boolean hasNamespaces() {
        return m_hasContext && m_defContext.hasNamespace();
    }

    public BindingDefinition getBindingRoot() {
        return m_container.getBindingRoot();
    }

    public DefinitionContext getDefinitionContext() {
        return m_defContext;
    }
    
    //
    // IComponent interface method definitions

    public boolean isOptional() {
        
        // optional if and only if all child components are optional
        if (m_attributes != null) {
            for (int i = 0; i < m_attributes.size(); i++) {
                if (!((IComponent)m_attributes.get(i)).isOptional()) {
                    return false;
                }
            }
        }
        for (int i = 0; i < m_contents.size(); i++) {
            if (!((IComponent)m_contents.get(i)).isOptional()) {
                return false;
            }
        }
        return true;
    }

    public boolean hasContent() {
        return m_contents.size() > 0;
    }

    public void genContentPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_contents.size() > 0) {
            
            // if single possiblity just test it directly
            int count = m_contents.size();
            if (count == 1) {
                ((IComponent)m_contents.get(0)).genContentPresentTest(mb);
            } else {
                
                // generate code for chained test with branches to found exit
                BranchWrapper[] tofound = new BranchWrapper[count];
                for (int i = 0; i < count; i++) {
                    IComponent comp = (IComponent)m_contents.get(i);
                    comp.genContentPresentTest(mb);
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
                ("Internal error - no content present");
        }
    }

    public String getType() {
        return m_contextObject.getBoundClass().getClassName();
    }
    
    public NameDefinition getWrapperName() {
        if (m_contents.size() == 1) {
            return ((IComponent)m_contents.get(0)).getWrapperName();
        } else {
            return null;
        }
    }
}
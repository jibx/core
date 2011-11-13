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

import java.util.ArrayList;

import org.jibx.binding.classes.ContextMethodBuilder;
import org.jibx.runtime.JiBXException;

/**
 * Reference to a mapping definition. This is used as a placeholder when
 * building the component structure of a binding definition. It's necessary
 * because the referenced mapping may not have been parsed yet. During the
 * linkage phase that follows parsing this looks up the appropriate mapping
 * definition and sets up the corresponding component structure. Thereafter it
 * operates as a simple pass-through wrapper for the top child component.
 *
 * @author Dennis M. Sosnoski
 */
public class MappingReference extends PassThroughComponent
{
    /** Containing binding definition structure. */
    private final IContainer m_container;
    
    /** Property definition. */
    private final PropertyDefinition m_property;
    
    /** Flag for nillable element. */
    private final boolean m_isNillable;
    
    /** Fully qualified name of mapped type. */
    private String m_type;
    
    /** Ordinary name of type for abstract mapping. */
    private String m_referenceText;
    
    /** Qualified name of type for abstract mapping. */
    private String m_referenceQName;
    
    /** Context object. */
    private final IContextObj m_contextObject;
    
    /** Name from reference (only allowed with abstract mappings) */
    private final NameDefinition m_name;
    
    /** Synthetic reference added to empty collection flag */
    private final boolean m_isSynthetic;
    
    /** Referenced mapping. */
    private IMapping m_mapping;
    
    /** Generated wrapped component, used when checking for both attributes
      and elements present. */
    private IComponent m_wrappedReference;

    /**
     * Constructor from property and type.
     *
     * @param contain containing binding definition structure
     * @param prop property definition
     * @param type fully qualified name of mapped type
     * @param reftext ordinary text name for abstract mapping reference
     * (<code>null</code> if not specified)
     * @param refqname qualified type name for abstract mapping reference
     * (<code>null</code> if not specified)
     * @param objc current object context
     * @param name reference name definition (only allowed with abstract
     * mappings)
     * @param synth sythentic reference added to empty collection flag
     * @param nillable flag for nillable element
     */
    public MappingReference(IContainer contain, PropertyDefinition prop,
        String type, String reftext, String refqname, IContextObj objc,
        NameDefinition name, boolean synth, boolean nillable) {
        super();
        m_container = contain;
        m_property = prop;
        m_type = type;
        m_referenceText = reftext;
        m_referenceQName = refqname;
        m_contextObject = objc;
        m_name = name;
        m_isSynthetic = synth;
        m_isNillable = nillable;
    }
    
    public IMapping getMapping() {
        return m_mapping;
    }
    
    //
    // IComponent interface method definitions (overrides of defaults)

    public boolean isOptional() {
        return m_property.isOptional();
    }

    public String getType() {
        return m_type;
    }

    public void setLinkages() throws JiBXException {
        
        // find the mapping being used
        DefinitionContext defc = m_container.getDefinitionContext();
        IMapping mdef = null;
        if (m_referenceText != null) {
            mdef = defc.getClassMapping(m_referenceText);
            if (mdef == null) {
                mdef = defc.getClassMapping(m_referenceQName);
            }
        }
        if (mdef == null) {
            mdef = defc.getClassMapping(m_type);
        }
        m_mapping = mdef;
        IComponent wrap = null;
        PropertyDefinition prop = m_property;
        if (mdef == null) {
            
            // generate generic mapping to unknown type
            if (m_name != null) {
                throw new JiBXException
                    ("Name not allowed for generic mapping of type " + m_type);
            }
            wrap = new DirectGeneric(m_container, m_type, prop);
            
        } else if (m_isSynthetic && mdef.isAbstract() && !mdef.isBase()) {
            
            // collection reference to abstract non-base mapping as generic
            wrap = new DirectGeneric(m_container, m_type, prop);
            
        } else {
            
            // check for reference from collection
            if (prop.isImplicit()) {
                prop.setOptional(false);
            }
            
            // add mapping namespaces to context (only if no element on mapping)
            if (mdef.getName() == null) {
                mdef.setLinkages();
                ArrayList nss = mdef.getNamespaces();
                if (nss != null) {
                    for (int i = 0; i < nss.size(); i++) {
                        defc.addImpliedNamespace((NamespaceDefinition)nss.get(i));
                    }
                }
            }
            
            // generate wrapped component for all calls
            String type = mdef.getBoundType();
            if (prop.getTypeName() == null) {
                prop = new PropertyDefinition(type, m_contextObject,
                    prop.isOptional());
            }
            if (m_name == null) {
                wrap = mdef.buildRef(m_container, m_contextObject,
                    prop.getTypeName(), prop);
            } else {
                
                // force property required for internal structure
                PropertyDefinition propcopy = new PropertyDefinition(prop);
                propcopy.setOptional(false);
                wrap = mdef.buildRef(m_container, m_contextObject,
                    prop.getTypeName(), propcopy);
                m_wrappedReference = wrap;
                if (mdef.getName() == null) {
                    
                    // create the replacement components
                    IComponent icomp = wrap;
                    wrap = new ElementWrapper(defc, m_name, icomp, m_isNillable);
                    if (prop.isImplicit()) {
                        ((ElementWrapper)wrap).setDirect(true);
                    }
                    if (prop.isOptional()) {
                        
                        // make element optional
                        if (icomp instanceof ComponentProperty) {
                            ((ComponentProperty)icomp).setSkipping(true);
                        }
                        ((ElementWrapper)wrap).setOptionalNormal(true);
                        ((ElementWrapper)wrap).setStructureObject(true);
                        ((ElementWrapper)wrap).setDirect(true);
                        wrap = new OptionalStructureWrapper(wrap, prop,
                            true);
                    }
                    
                } else {
                    throw new JiBXException
                        ("Name not allowed for reference to mapping of type " +
                        type + ", which already defines a name");
                }
            }
            
            // set type based on mapping
            m_type = mdef.getReferenceType();
        }
        
        // link actual component into structure
        setWrappedComponent(wrap);
        super.setLinkages();
    }

    /**
     * Patch the generated code to remove the unmarshalled object when it's a
     * "this" reference with both elements and attributes.
     *
     * @param mb
     * @throws JiBXException
     */
    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        super.genContentUnmarshal(mb);
        
        // check for unmarshalling fix needed
        if (m_wrappedReference != null && m_property.isThis() &&
            m_wrappedReference.hasAttribute() &&
            m_wrappedReference.hasContent()) {
            mb.appendPOP();
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("mapping reference to " +
            ((m_referenceText == null) ? m_type : m_referenceText));
        if (m_property != null) {
            System.out.print(" using " + m_property.toString());
        }
        System.out.println();
        if (m_component != null) {
            m_component.print(depth+1);
        }
    }
}
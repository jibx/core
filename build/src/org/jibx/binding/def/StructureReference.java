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

import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.ContextMethodBuilder;
import org.jibx.runtime.JiBXException;

/**
 * Reference to a structure definition. This is used as a placeholder when
 * building the component structure of a binding definition. It's necessary
 * because the referenced structure may not have been parsed yet. During the
 * linkage phase that follows parsing this looks up the appropriate structure
 * definition and sets up the corresponding component structure. Thereafter it
 * operates as a simple pass-through wrapper for the top child component.
 *
 * @author Dennis M. Sosnoski
 */
public class StructureReference extends PassThroughComponent
{
    /** Containing binding component. */
    private final IContainer m_container;
    
    /** Containing binding definition structure. */
    private final IContextObj m_contextObject;
    
    /** Property definition (may be <code>null</code>). */
    private final PropertyDefinition m_property;
    
    /** Identifier for referenced structure definition. */
    private final String m_label;
    
    /** Flag for marshalling code generation to be skipped by component. */
    private boolean m_skipMarshal;
    
    /** Object load needed for marshalling flag (used with object binding). */
    private boolean m_needLoad;

    /**
     * Constructor.
     *
     * @param contain containing binding component
     * @param label reference structure identifier
     * @param prop property definition (may be <code>null</code>)
     * @param hasname element name used with reference flag
     * @param cobj context object
     */

    public StructureReference(IContainer contain, String label,
        PropertyDefinition prop, boolean hasname, IContextObj cobj) {
        super();
        m_container = contain;
        m_contextObject = cobj;
        m_property = prop;
        m_skipMarshal = hasname && prop != null && prop.isOptional();
        m_label = label;
    }
    
    //
    // IComponent interface method definitions (overrides of defaults)

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_needLoad) {
            mb.loadObject();
        }
        m_component.genAttributeMarshal(mb);
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_needLoad) {
            mb.loadObject();
        }
        m_component.genContentMarshal(mb);
    }

    public void setLinkages() throws JiBXException {
        
        // find the structure being used
        DefinitionContext defc = m_container.getDefinitionContext();
        IComponent impl = defc.getNamedStructure(m_label);
        
        // verify compatible use of structure
        String type = (m_property == null) ?
            m_contextObject.getBoundClass().getClassName() :
            m_property.getTypeName();
        ClassFile cf = ClassCache.requireClassFile(type);
        String itype = impl.getType();
        if (!cf.isSuperclass(itype)) {
            throw new JiBXException("Reference to structure " + m_label +
                " has object of type " + type + " rather than required " +
                itype);
        }
        
        // generate component matching mapping type
        IComponent wrap;
        if (impl instanceof DirectObject) {
            wrap = new DirectProperty(m_property, (DirectObject)impl);
        } else if (m_property == null || m_property.isImplicit()) {
            wrap = impl;
            m_needLoad = impl instanceof ObjectBinding;
        } else {
            wrap = new ComponentProperty(m_property, impl, m_skipMarshal);
        }
        
        // set the wrapped component used for all other calls
        setWrappedComponent(wrap);
        m_component.setLinkages();
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("structure reference to " + m_label);
        if (m_property != null) {
            System.out.println(" using " + m_property.toString());
        }
        System.out.println();
    }
}

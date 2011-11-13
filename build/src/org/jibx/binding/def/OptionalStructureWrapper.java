/*
Copyright (c) 2003-2007, Dennis M. Sosnoski
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
 * Component decorator for optional structure with associated property. This
 * just handles necessary glue code generation for the marshalling operations,
 * where the presence of the structure needs to be tested before actually
 * handling tag generation.
 */
public class OptionalStructureWrapper extends PassThroughComponent
{
    /** Property definition. */
    private final PropertyDefinition m_property;

    /** Load object for marshalling code generation flag. */
    private final boolean m_loadMarshal;
    
    /**
     * Constructor.
     *
     * @param wrap wrapped binding component
     * @param load flag for need to load object for marshalling code
     */

    public OptionalStructureWrapper(IComponent wrap, PropertyDefinition prop,
        boolean load) {
        super(wrap);
        m_property = prop;
        m_loadMarshal = load;
    }

    //
    // IComponent interface method definitions

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        mb.loadObject();
        BranchWrapper ifmiss = m_property.genTest(mb);
        super.genAttributeMarshal(mb);
        mb.targetNext(ifmiss);
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        mb.loadObject();
        BranchWrapper ifmiss = m_property.genTest(mb);
        if (m_loadMarshal) {
            mb.loadObject();
            m_property.genLoad(mb);
        }
        super.genContentMarshal(mb);
        mb.targetNext(ifmiss);
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("optional structure wrapper " + m_property.toString());
        if (m_loadMarshal) {
            System.out.print(" (load marshal)");
        }
        System.out.println();
        m_component.print(depth+1);
    }
}
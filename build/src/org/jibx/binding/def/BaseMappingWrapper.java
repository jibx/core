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


import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Component decorator for abstract base mapping from extension mapping. This
 * just handles necessary glue code generation.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class BaseMappingWrapper extends PassThroughComponent
{
    /**
     * Constructor.
     *
     * @param wrap wrapped binding component
     */

    public BaseMappingWrapper(IComponent wrap) {
        super(wrap);
    }

    //
    // IComponent interface method definitions

    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        mb.loadObject();
        super.genAttributeUnmarshal(mb);
    }

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        mb.loadObject();
        super.genAttributeMarshal(mb);
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        mb.loadObject();
        super.genContentUnmarshal(mb);
        mb.appendPOP();
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        mb.loadObject();
        super.genContentMarshal(mb);
    }
    
    public void genNewInstance(ContextMethodBuilder mb) throws JiBXException {
        throw new IllegalStateException
            ("Internal error - no new instance for base class");
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.println("base mapping wrapper");
        m_component.print(depth+1);
    }
}

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

/**
 * Containing binding definitions structure interface. This interface is
 * implemented by components corresponding to elements in the binding definition
 * that can contain other elements. It supplies a context for resolving
 * reference to mappings, formats, or labeled structures, and for inherited
 * settings.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public interface IContainer
{
    /**
     * Check if content children are ordered.
     *
     * @return <code>true</code> if ordered, <code>false</code> if not
     */

    public boolean isContentOrdered();
    
    /**
     * Get default style for value expression.
     *
     * @return default style type for values
     */

    public int getStyleDefault();

    /**
     * Get definition context for binding element.
     *
     * @return binding definition context
     */

    public DefinitionContext getDefinitionContext();

    /**
     * Get root of binding definition.
     *
     * @return binding definition root
     */

    public BindingDefinition getBindingRoot();
}

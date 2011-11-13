/*
Copyright (c) 2004-2008, Dennis M. Sosnoski.
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

package org.jibx.binding.model;

import org.jibx.util.IClass;

/**
 * Child component interface definition. This is the basic interface implemented
 * by every binding definition element that actually participates in the nested
 * structure of a binding (as opposed to elements such as <b>format</b>
 * elements, which are simply convenience shortcuts). It defines the hooks used
 * to handle structure validation of a binding definition model.
 *
 * @author Dennis M. Sosnoski
 */
public interface IComponent
{
    /**
     * Check if component is an optional item.
     *
     * @return <code>true</code> if optional, <code>false</code> if required
     */
    boolean isOptional();
    
    /**
     * Check if component defines one or more attribute values of the
     * containing element. This method is only valid after validation.
     *
     * @return <code>true</code> if one or more attribute values defined for
     * containing element, <code>false</code> if not
     */
    boolean hasAttribute();

    /**
     * Check if component defines one or more elements or text values as
     * children of the containing element. This method is only valid after
     * validation.
     *
     * @return <code>true</code> if one or more content values defined
     * for containing element, <code>false</code> if not
     */
    boolean hasContent();
    
    /**
     * Check if component has a name.
     * 
     * @return <code>true</code> if component has a name, <code>false</code> if
     * not
     */
    boolean hasName();
    
    /**
     * Get name.
     * 
     * @return name text
     */
    String getName();

    /**
     * Get specified namespace URI.
     * 
     * @return namespace URI (<code>null</code> if not set)
     */
    String getUri();
    
    /**
     * Get value type information. This call is only meaningful after
     * prevalidation.
     * 
     * @return type information
     */
    IClass getType();
    
    /**
     * Check if this structure implicitly uses the containing object. This call
     * is only meaningful after prevalidation.
     * 
     * @return <code>true</code> if using the containing object,
     * <code>false</code> if own object
     */
    boolean isImplicit();
}
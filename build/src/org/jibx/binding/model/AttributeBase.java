/*
Copyright (c) 2004-2005, Dennis M. Sosnoski
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

/**
 * Base class for all attribute group structures in binding definition model.
 * This just provides the basic validation hooks.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
 
public abstract class AttributeBase
{    
    /**
     * Prevalidate attribute information. The prevalidation step is used to
     * check attribute values in isolation, such as the settings for enumerated
     * values and class file information. This empty base class implementation
     * should be overridden by each subclass that requires prevalidation
     * handling.
     * 
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {}
    
    /**
     * Validate attribute information. The validation step is used for checking
     * the interactions between attributes, such as references to named elements
     * and namespace usage. The {@link #prevalidate} method will always be
     * called for every component in the binding definition before this method
     * is called for any component. This empty base class implementation should
     * be overridden by each subclass that requires validation handling.
     * 
     * @param vctx validation context
     */
    public void validate(ValidationContext vctx) {}
}
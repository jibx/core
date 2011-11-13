/*
 * Copyright (c) 2006, Dennis M. Sosnoski All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.schema;

import org.jibx.schema.validation.ValidationContext;

/**
 * Schema component interface. This just provides validation method hooks. The validation contract says that the
 * {@link #prevalidate(ValidationContext)} method will always be called for every component in the schema definition
 * before the {@link #validate(ValidationContext)} method is called for any component. These two methods represent the
 * beginning and end phases of the validation process - other steps (such as registration) may be handled in between
 * these two phases.
 * 
 * @author Dennis M. Sosnoski
 */
public interface IComponent
{
    /** Schema namespace URI. */
    public static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    
    /**
     * Prevalidate component information. The prevalidation step is used to check isolated aspects of a component, such
     * as the settings for enumerated values.
     * 
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx);
    
    /**
     * Validate component information. The validation step is used for checking the interactions between components,
     * such as name references to other components. The validation contract says that the {@link
     * #prevalidate(ValidationContext)} method will always be called for every component in the schema definition before
     * this method is called for any component.
     * 
     * @param vctx validation context
     */
    public void validate(ValidationContext vctx);
}
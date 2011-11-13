/*
 * Copyright (c) 2007, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema.codegen.custom;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.util.StringArray;

/**
 * Generator customization.
 * 
 * TODO: use separate subclasses for the different types of generation, or an interface? looks like there'll only be a
 * few alternatives (normal class, enumeration, collection). or have generators for different types of fields, and such?
 * that gives the maximum flexibility, but also adds a lot of complexity. at a minimum, need to support different types
 * of generators for enumeration, choice, union, and collection value types. would also like to support different
 * validation method generators, orthogonal to the other variations. finally, want to support different JavaDoc
 * formatters. perhaps best to use a separate class for each.
 * 
 * @author Dennis M. Sosnoski
 */
public class GeneratorCustom extends CustomBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes = new StringArray(new String[] { "class" });
    
    /** Generator class name. */
    private String m_class;
    
    /** Parameter values for generator class instance. */
    private String[] m_parameters;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public GeneratorCustom(NestingCustomBase parent) {
        super(parent);
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Get class name.
     * 
     * @return class
     */
    public String getClassName() {
        return m_class;
    }

    /**
     * Get parameter values.
     *
     * @return parameters
     */
    public String[] getParameters() {
        return m_parameters;
    }
}
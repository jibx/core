/*
 * Copyright (c) 2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen;

/**
 * Information for a class matching a schema definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class TypeData
{
    /** Fully-qualified class name. */
    private final String m_fullName;
    
    /** Class name as used for binding (with '$' marker for inner class). */
    private final String m_bindingName;
    
    /** Pregenerated class flag. */
    private final boolean m_pregenerated;
    
    /** Class represents simple value flag. */
    private final boolean m_simple;

    /**
     * Constructor with all values specified.
     * 
     * @param fullname fully-qualified class name in standard form ('.' as inner class separator)
     * @param bindname fully-qualified class name as used in binding (with '$' inner class separator)
     * @param pregen pregenerated class flag
     * @param simple simple value flag
     */
    public TypeData(String fullname, String bindname, boolean pregen, boolean simple) {
        m_fullName = fullname;
        m_bindingName = bindname;
        m_pregenerated = pregen;
        m_simple = simple;
    }
    
    /**
     * Constructor for new top-level class.
     * 
     * @param fullname fully-qualified class name
     * @param simple simple value flag
     */
    public TypeData(String fullname, boolean simple) {
        this(fullname, fullname, false, simple);
    }

    /**
     * Constructor from new top-level or inner class.
     * 
     * @param fullname fully-qualified class name in standard form ('.' as inner class separator)
     * @param bindname fully-qualified class name as used in binding (with '$' inner class separator)
     * @param simple simple value flag
     */
    public TypeData(String fullname, String bindname, boolean simple) {
        this(fullname, bindname, false, simple);
    }

    /**
     * Get fully-qualified name.
     * 
     * @return name
     */
    public String getFullName() {
        return m_fullName;
    }

    /**
     * Get fully-qualified name as used in binding. This differs from the standard fully-qualified name in that it uses
     * '$' rather than '.' to delimit inner class names.
     * 
     * @return name
     */
    public String getBindingName() {
        return m_bindingName;
    }
    
    /**
     * Check if pregenerated class.
     *
     * @return <code>true</code> if pregenerated, <code>false</code> if not
     */
    public boolean isPregenerated() {
        return m_pregenerated;
    }
    
    /**
     * Check if class represents a simple value.
     *
     * @return <code>true</code> if simple value, <code>false</code> if not
     */
    public boolean isSimpleValue() {
        return m_simple;
    }
}
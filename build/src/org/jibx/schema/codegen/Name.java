/*
 * Copyright (c) 2007, Dennis M. Sosnoski. All rights reserved.
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
 * Name representation for {@link Item} and related structures. Names may be shared between different levels of the item
 * structure in some cases (such as an element that contains only a single value, with several layers of indirection),
 * and this class supports name sharing while retaining the ability to modify the actual name text (necessary to avoid
 * name conflicts in the generated code).
 * 
 * @author Dennis M. Sosnoski
 */
public class Name
{
    /** Flag for name fixed by user request. */
    private final boolean m_fixed;
    
    /** Name checked (and possibly adjusted) for conflicts flag. */
    private boolean m_checked;
    
    /** Actual name text. */
    private String m_text;
    
    /**
     * Default constructor. This just creates a non-fixed name with no initial value.
     */
    public Name() {
        m_fixed = false;
    }
    
    /**
     * Constructor.
     * 
     * @param name fixed name text (<code>null</code> if not fixed)
     */
    public Name(String name) {
        m_text = name;
        m_fixed = m_text != null;
    }
    
    /**
     * Copy constructor.
     * 
     * @param base
     */
    public Name(Name base) {
        m_fixed = base.m_fixed;
        m_checked = base.m_checked;
        m_text = base.m_text;
    }

    /**
     * Check if name is fixed by configuration.
     *
     * @return <code>true</code> if fixed, <code>false</code> if not
     */
    public boolean isFixed() {
        return m_fixed;
    }

    /**
     * Check if name has been checked for conflicts. This flag is used by the actual class generated code ({@link
     * ClassHolder}) to track which names have already been entered into the set of names used by a class.
     *
     * @return checked
     */
    public boolean isChecked() {
        return m_checked;
    }

    /**
     * Set flag for name checked for conflicts. This flag is used by the actual class generated code ({@link
     * ClassHolder}) to track which names have already been entered into the set of names used by a class.
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        m_checked = checked;
    }

    /**
     * Get item name.
     * 
     * @return name (<code>null</code> if unspecified)
     */
    public String getText() {
        return m_text;
    }
    
    /**
     * Set item name. It is an error to call this method if {@link #isFixed()} returns true.
     * 
     * @param name (<code>null</code> if unspecified)
     */
    public void setText(String name) {
        if (m_fixed || name.indexOf('-') > 0) {
            throw new IllegalStateException("Internal error - attempt to change configured name");
        } else {
            m_text = name;
        }
    }
    
    /**
     * Generate printable description of name. This is intended for use in logging output.
     *
     * @return description
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append('\'');
        if (m_text == null) {
            buffer.append("not set");
        } else {
            buffer.append(m_text);
        }
        buffer.append("' (");
        if (m_fixed) {
            buffer.append("fixed, ");
        }
        if (m_checked) {
            buffer.append("checked, ");
        }
        buffer.append("id=");
        buffer.append(hashCode());
        buffer.append(')');
        return buffer.toString();
    }
}
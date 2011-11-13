/*
Copyright (c) 2005-2006, Dennis M. Sosnoski
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

package org.jibx.binding.util;

import org.jibx.runtime.IntStack;

/**
 * Stack for values that depend on the level of nesting, where only some of the
 * levels change the current value.
 *
 * @author Dennis M. Sosnoski
 */
public class SparseStack
{
    /** Current item. */
    private Object m_current;
    
    /** Current nesting level. */
    private int m_level;
    
    /** Levels with different items (paired with m_items stack). */
    private IntStack m_levels;
    
    /** Stack of different items (paired with m_levels stack). */
    private ObjectStack m_items;
    
    /**
     * Constructor with initial value.
     * 
     * @param current initial value
     */
    public SparseStack(Object current) {
        
        // initialize so peek always works
        m_levels = new IntStack();
        m_levels.push(-1);
        m_items = new ObjectStack();
        m_items.push(current);
        m_current = current;
    }
    
    /**
     * Constructor with no initial value.
     */
    public SparseStack() {
        this(null);
    }
    
    /**
     * Get current object.
     * 
     * @return current
     */
    public Object getCurrent() {
        return m_current;
    }
    
    /**
     * Set current object.
     * 
     * @param obj set the current object
     */
    public void setCurrent(Object obj) {
        m_current = obj;
    }
    
    /**
     * Enter a level of nesting.
     */
    public void enter() {
        if (m_current != m_items.peek()) {
            m_levels.push(m_level);
            m_items.push(m_current);
        }
        m_level++;
    }
    
    /**
     * Exit a level of nesting with changed item returned.
     * 
     * @return item that was active until this exit, or <code>null</code> if
     * same item still active
     */
    public Object exit() {
        m_level--;
        if (m_level == m_levels.peek()) {
            Object obj = m_current;
            m_levels.pop();
            m_current = m_items.pop();
            return obj;
        } else {
            return null;
        }
    }
}
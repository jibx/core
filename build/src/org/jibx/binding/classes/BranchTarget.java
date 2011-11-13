/*
Copyright (c) 2004, Dennis M. Sosnoski
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

package org.jibx.binding.classes;

import org.apache.bcel.generic.InstructionHandle;

/**
 * Wrapper for branch target information. This preserves a snapshot of the stack
 * state for the branch target, allowing it to be matched against the stack
 * state for the branch source.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class BranchTarget
{
    /** Actual wrapped instruction handle. */
    private final InstructionHandle m_targetHandle;
    
    /** Stack state for branch target. */
    private final String[] m_stackTypes;
    
    /**
     * Constructor.
     *
     * @param hand instruction handle
     * @param types array of types of values on stack 
     */

    /*package*/ BranchTarget(InstructionHandle hand, String[] types) {
        m_targetHandle = hand;
        m_stackTypes = types;
    }

    /**
     * Get actual target instruction.
     *
     * @return handle for target instruction
     */

    /*package*/ InstructionHandle getInstruction() {
        return m_targetHandle;
    }

    /**
     * Get stack state information.
     *
     * @return array of type names on stack
     */

    /*package*/ String[] getStack() {
        return m_stackTypes;
    }
    
    /**
     * Matches the branch target stack state against the supplied stack state.
     *
     * @param types array of types of values on stack
     * @return <code>true</code> if stack states match, <code>false</code> if
     * not
     */
     
    /*package*/ boolean matchStacks(String[] types) {
        
        // match stack states
        if (types.length == m_stackTypes.length) {
            for (int i = 0; i < types.length; i++) {
                if (!types[i].equals(m_stackTypes[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
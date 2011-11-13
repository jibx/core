/*
Copyright (c) 2003-2008, Dennis M. Sosnoski.
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

import java.util.HashMap;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.Type;

/**
 * Builder for simple methods that may just pass checked exceptions on to
 * caller.
 *
 * @author Dennis M. Sosnoski
 */
public class ExceptionMethodBuilder extends MethodBuilder
{
    /** Map for object to variable assignments. */
    private HashMap m_slotMap;
    
    /**
     * Constructor with types specified.
     *
     * @param name method name to be built
     * @param ret method return type
     * @param args types of arguments
     * @param cf owning class file information
     * @param access flags for method access
     */
    public ExceptionMethodBuilder(String name, Type ret, Type[] args,
        ClassFile cf, int access) {
        super(name, ret, args, cf, access);
    }

    /**
     * Constructor from signature.
     *
     * @param name method name to be built
     * @param sig method signature
     * @param cf owning class file information
     * @param access flags for method access
     */
    public ExceptionMethodBuilder(String name, String sig,
        ClassFile cf, int access) {
        super(name, Type.getReturnType(sig), Type.getArgumentTypes(sig),
            cf, access);
    }

    /**
     * Constructor from signature for public, final method.
     *
     * @param name method name to be built
     * @param sig method signature
     * @param cf owning class file information
     */
    public ExceptionMethodBuilder(String name, String sig, ClassFile cf) {
        super(name, Type.getReturnType(sig), Type.getArgumentTypes(sig),
            cf, Constants.ACC_PUBLIC | Constants.ACC_FINAL);
    }

    /**
     * Define local variable slot for object. The current code in the method
     * must have the initial value for the variable on the stack
     *
     * @param obj owning object of slot
     * @param type variable type
     * @return slot number
     */
    public int defineSlot(Object obj, Type type) {
        if (m_slotMap == null) {
            m_slotMap = new HashMap();
        }
        LocalVariableGen var = createLocal("var" + m_slotMap.size(), type);
        m_slotMap.put(obj, var);
        return var.getIndex();
    }

    /**
     * Check if local variable slot defined for object.
     *
     * @param obj owning object of slot
     * @return local variable slot assigned to object, or <code>-1</code> if
     * none
     */

    public int getSlot(Object obj) {
        if (m_slotMap != null) {
            LocalVariableGen var = (LocalVariableGen)m_slotMap.get(obj);
            if (var != null) {
                return var.getIndex();
            }
        }
        return -1;
    }

    /**
     * Free local variable slot for object. This clears the usage of the slot
     * (if one has been defined for the object) so it can be reused for other
     * purposes.
     *
     * @param obj owning object of slot
     */

    public void freeSlot(Object obj) {
        if (m_slotMap != null) {
            LocalVariableGen var = (LocalVariableGen)m_slotMap.get(obj);
            if (var != null) {
                var.setEnd(getLastInstruction());
                m_slotMap.remove(obj);
            }
        }
    }

    /**
     * Process accumulated exceptions. Just adds the checked exceptions that
     * may be thrown within the body to the list for this method, passing them
     * on to the caller for handling.
     */
    protected void handleExceptions() {
        for (int i = 0; i < m_exceptions.size(); i++) {
            m_generator.addException((String)m_exceptions.get(i));
        }
    }
}

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

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;

/**
 * Builder for marshal and unmarshal methods. Adds exception accumulation with
 * actual handling provided by the subclass.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class MarshalUnmarshalBuilder extends ContextMethodBuilder
{
    /**
     * Constructor. This sets up for constructing the marshal or unmarshal
     * method.
     *
     * @param name method name to be built
     * @param ret method return type
     * @param args types of arguments
     * @param mf method generation class file information
     * @param access flags for method access
     * @param obj variable slot for current object
     * @param type marshalled or unmarshalled class name
     * @param ctx variable slot for marshalling/unmarshalling context
     * @param ctype context type as defined in method
     */
    protected MarshalUnmarshalBuilder(String name, Type ret, Type[] args,
        ClassFile mf, int access, int obj, String type, int ctx, String ctype) {
        super(name, ret, args, mf, access, obj, type, ctx, ctype);
    }

    /**
     * Add exception handler code. This method must be implemented by each
     * subclass to provide the appropriate handling code.
     * 
     * @return handle for first instruction in handler
     */
    public abstract InstructionHandle genExceptionHandler();

    /**
     * Process accumulated exceptions. Sets up an exception handler framework
     * and then calls the {@link #genExceptionHandler} method to build the
     * handler body.
     */
    protected void handleExceptions() {
        int index = m_exceptions.indexOf(FRAMEWORK_EXCEPTION_CLASS);
        if (index >= 0) {
            m_generator.addException(FRAMEWORK_EXCEPTION_CLASS);
            m_exceptions.remove(index);
        }
        if (m_exceptions.size() > 0) {
            InstructionHandle begin = getFirstInstruction();
            InstructionHandle end = getLastInstruction();
            InstructionHandle handle = genExceptionHandler();
            for (int i = 0; i < m_exceptions.size(); i++) {
                m_generator.addExceptionHandler(begin, end, handle,
                    (ObjectType)ClassItem.
                    typeFromName((String)m_exceptions.get(i)));
            }
        }
    }
}
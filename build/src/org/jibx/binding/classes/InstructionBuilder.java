/*
Copyright (c) 2003, Dennis M. Sosnoski
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

import java.security.InvalidParameterException;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;

/**
 * Instruction builder. Extends the basic instruction construction tools in
 * BCEL with some convenience methods.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class InstructionBuilder extends InstructionFactory
{
    /**
     * Constructor.
     *
     * @param cg class generation information
     * @param cp constant pool generator
     */

    public InstructionBuilder(ClassGen cg, ConstantPoolGen cp) {
        super(cg, cp);
    }

    /**
     * Get constant pool generator.
     *
     * @return constant pool generator for class
     */

    public ConstantPoolGen getConstantPoolGen() {
        return cp;
    }

    /**
     * Create load constant instruction. Builds the most appropriate type of
     * instruction for the value.
     *
     * @param value constant value to be loaded
     * @return generated instruction information
     */

    public CompoundInstruction createLoadConstant(int value) {
        return new PUSH(cp, value);
    }

    /**
     * Create load constant instruction. Loads a <code>String</code> reference
     * from the constant pool.
     *
     * @param value constant value to be loaded
     * @return generated instruction information
     */

    public CompoundInstruction createLoadConstant(String value) {
        return new PUSH(cp, value);
    }

    /**
     * Create load constant instruction. Loads an unwrapped primitive value or
     * String from the constant pool.
     *
     * @param value constant value to be loaded
     * @return generated instruction information
     */

    public CompoundInstruction createLoadConstant(Object value) {
        if (value instanceof Boolean) {
            return new PUSH(cp, (Boolean)value);
        } else if (value instanceof Character) {
            return new PUSH(cp, (Character)value);
        } else if (value instanceof Number) {
            return new PUSH(cp, (Number)value);
        } else if (value instanceof String) {
            return new PUSH(cp, (String)value);
        } else {
            throw new InvalidParameterException
                ("Internal code generation error!");
        }
    }

    /**
     * Create getfield instruction. Uses the field information to generate
     * the instruction.
     *
     * @param item information for field to be set
     * @return generated instruction information
     */

    public FieldInstruction createGetField(ClassItem item) {
        String cname = item.getClassFile().getName();
        String fname = item.getName();
        return new GETFIELD(cp.addFieldref(cname, fname, item.getSignature()));
    }

    /**
     * Create putfield instruction. Uses the field information to generate
     * the instruction.
     *
     * @param item information for field to be set
     * @return generated instruction information
     */

    public FieldInstruction createPutField(ClassItem item) {
        String cname = item.getClassFile().getName();
        String fname = item.getName();
        return new PUTFIELD(cp.addFieldref(cname, fname, item.getSignature()));
    }

    /**
     * Create getstatic instruction. Uses the field information to generate
     * the instruction.
     *
     * @param item information for field to be set
     * @return generated instruction information
     */

    public FieldInstruction createGetStatic(ClassItem item) {
        String cname = item.getClassFile().getName();
        String fname = item.getName();
        return new GETSTATIC(cp.addFieldref(cname, fname, item.getSignature()));
    }

    /**
     * Create putstatic instruction. Uses the field information to generate
     * the instruction.
     *
     * @param item information for field to be set
     * @return generated instruction information
     */

    public FieldInstruction createPutStatic(ClassItem item) {
        String cname = item.getClassFile().getName();
        String fname = item.getName();
        return new PUTSTATIC(cp.addFieldref(cname, fname, item.getSignature()));
    }

    /**
     * Create invoke instruction for static method. Uses the method information
     * to generate the instruction.
     *
     * @param item information for method to be called
     * @return generated instruction information
     */

    public InvokeInstruction createCallStatic(ClassItem item) {
        String cname = item.getClassFile().getName();
        String mname = item.getName();
        int index = cp.addMethodref(cname, mname, item.getSignature());
        return new INVOKESTATIC(index);
    }

    /**
     * Create invoke instruction for virtual method. Uses the method information
     * to generate the instruction.
     *
     * @param item information for method to be called
     * @return generated instruction information
     */

    public InvokeInstruction createCallVirtual(ClassItem item) {
        String cname = item.getClassFile().getName();
        String mname = item.getName();
        int index = cp.addMethodref(cname, mname, item.getSignature());
        return new INVOKEVIRTUAL(index);
    }

    /**
     * Create invoke instruction for interface method. Uses the method
     * information to generate the instruction.
     *
     * @param item information for method to be called
     * @return generated instruction information
     */

    public InvokeInstruction createCallInterface(ClassItem item) {
        String cname = item.getClassFile().getName();
        String mname = item.getName();
        String signature = item.getSignature();
        return createInvoke(cname, mname, Type.getReturnType(signature),
            Type.getArgumentTypes(signature), Constants.INVOKEINTERFACE);
    }

    /**
     * Create invoke static method instruction from signature.
     *
     * @param method fully qualified class and method name
     * @param signature method signature in standard form
     * @return generated instruction information
     */

    public InvokeInstruction createCallStatic(String method, String signature) {
        int split = method.lastIndexOf('.');
        String cname = method.substring(0, split);
        String mname = method.substring(split+1);
        int index = cp.addMethodref(cname, mname, signature);
        return new INVOKESTATIC(index);
    }

    /**
     * Create invoke virtual method instruction from signature.
     *
     * @param method fully qualified class and method name
     * @param signature method signature in standard form
     * @return generated instruction information
     */

    public InvokeInstruction createCallVirtual(String method, 
        String signature) {
        int split = method.lastIndexOf('.');
        String cname = method.substring(0, split);
        String mname = method.substring(split+1);
        int index = cp.addMethodref(cname, mname, signature);
        return new INVOKEVIRTUAL(index);
    }

    /**
     * Create invoke interface method instruction from signature.
     *
     * @param method fully qualified interface and method name
     * @param signature method signature in standard form
     * @return generated instruction information
     */

    public InvokeInstruction createCallInterface(String method, 
        String signature) {
        int split = method.lastIndexOf('.');
        String cname = method.substring(0, split);
        String mname = method.substring(split+1);
        return createInvoke(cname, mname, Type.getReturnType(signature),
            Type.getArgumentTypes(signature), Constants.INVOKEINTERFACE);
    }

    /**
     * Create invoke initializer instruction from signature.
     *
     * @param name fully qualified class name
     * @param signature method signature in standard form
     * @return generated instruction information
     */

    public InvokeInstruction createCallInit(String name, String signature) {
        int index = cp.addMethodref(name, "<init>", signature);
        return new INVOKESPECIAL(index);
    }
}
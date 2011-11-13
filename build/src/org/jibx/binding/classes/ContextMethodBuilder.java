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

import org.apache.bcel.Constants;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.Type;

/**
 * Builder for binding methods with a context and, optionally, a current object.
 * Tracks the current object reference and the context object reference
 * positions in the local variables table.
 *
 * @author Dennis M. Sosnoski
 */
public class ContextMethodBuilder extends ExceptionMethodBuilder
{
    /** Variable slot for current object reference. */
    private int m_objectSlot;
    
    /** Object type as accessed by method (<code>null</code> if none). */
    private String m_objectType;
    
    /** Variable slot for context reference. */
    private int m_contextSlot;
    
    /** Context type as accessed by method. */
    private String m_contextType;
    
    /** Context type as accessed by method. */
    private final boolean m_isStatic;

    /**
     * Constructor with types specified. This sets up for constructing a
     * binding method that uses a current object and a marshalling or
     * unmarshalling context.
     *
     * @param name method name to be built
     * @param ret method return type
     * @param args types of arguments
     * @param cf owning class file information
     * @param access flags for method access
     * @param obj variable slot for current object (negative value if to be
     * defined later)
     * @param type current object type as defined in method (<code>null</code>
     * if none)
     * @param ctx variable slot for marshalling/unmarshalling context
     * @param ctype context type as defined in method
     */
    public ContextMethodBuilder(String name, Type ret, Type[] args,
        ClassFile cf, int access, int obj, String type, int ctx, String ctype) {
        super(name, ret, args, cf, access);
        m_objectSlot = obj;
        m_objectType = type;
        m_contextSlot = ctx;
        m_contextType = ctype;
        m_isStatic = (access & Constants.ACC_STATIC) != 0;
        addException(FRAMEWORK_EXCEPTION_CLASS);
    }

    /**
     * Constructor from signature.
     *
     * @param name method name to be built
     * @param sig method signature
     * @param cf owning class file information
     * @param access flags for method access
     * @param obj variable slot for current object (negative value if to be
     * defined later)
     * @param type current object type
     * @param ctx variable slot for marshalling/unmarshalling context
     * @param ctype context type as defined in method
     */
    public ContextMethodBuilder(String name, String sig,
        ClassFile cf, int access, int obj, String type, int ctx, String ctype) {
        this(name, Type.getReturnType(sig), Type.getArgumentTypes(sig),
            cf, access, obj, type, ctx, ctype);
    }

    /**
     * Constructor from signature for public, final method.
     *
     * @param name method name to be built
     * @param sig method signature
     * @param cf owning class file information
     * @param obj variable slot for current object (negative value if to be
     * defined later)
     * @param type current object type
     * @param ctx variable slot for marshalling/unmarshalling context
     * @param ctype context type as defined in method
     */
    public ContextMethodBuilder(String name, String sig, ClassFile cf,
        int obj, String type, int ctx, String ctype) {
        this(name, sig, cf, Constants.ACC_PUBLIC|Constants.ACC_FINAL,
            obj, type, ctx, ctype);
    }

    /**
     * Set current object slot. Sets the local variable position of the current
     * object, as required when the object is actually created within the
     * method.
     *
     * @param slot local variable slot for current object
     */
    public void setObjectSlot(int slot) {
        m_objectSlot = slot;
    }

    /**
     * Append instruction to load object to stack.
     */
    public void loadObject() {
        if (m_objectType == null) {
            throw new IllegalStateException("Internal error - no object type");
        } else {
            appendLoadLocal(m_objectSlot);
        }
    }

    /**
     * Append instruction to store object from stack.
     */
    public void storeObject() {
        if (m_objectType == null) {
            throw new IllegalStateException("Internal error - no object type");
        } else {
            if (m_objectSlot < 0) {
                LocalVariableGen var = createLocal("obj",
                    ClassItem.typeFromName(m_objectType));
                m_objectSlot = var.getIndex();
            } else {
                appendCreateCast(m_objectType);
                appendStoreLocal(m_objectSlot);
            }
        }
    }

    /**
     * Append instruction(s) to load object to stack as specified type.
     *
     * @param type loaded type expected on stack
     */
    public void loadObject(String type) {
        if (m_objectType == null) {
            throw new IllegalStateException("Internal error - no object type");
        } else {
            appendLoadLocal(m_objectSlot);
            if (!m_objectType.equals(type)) {
                appendCreateCast(m_objectType, type);
            }
        }
    }

    /**
     * Append instruction to load context to stack.
     */
    public void loadContext() {
        appendLoadLocal(m_contextSlot);
    }

    /**
     * Append instruction(s) to load context to stack as specified type.
     *
     * @param type loaded type expected on stack
     */
    public void loadContext(String type) {
        appendLoadLocal(m_contextSlot);
        if (!m_contextType.equals(type)) {
            appendCreateCast(m_contextType, type);
        }
    }
    
    /**
     * Check if method is static.
     * 
     * @return <code>true</code> if static, <code>false</code> if not
     */
    public boolean isStaticMethod() {
        return m_isStatic;
    }
    
    /**
     * Construct fully-qualified class and method name for method under
     * construction.
     * 
     * @return fully-qualified class and method name
     */
    public String getFullName() {
        return getClassFile().getName() + '.' +  getName();
    }
}
/*
Copyright (c) 2003-2008, Dennis M. Sosnoski
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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.CompoundInstruction;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFGE;
import org.apache.bcel.generic.IFLT;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.IFNULL;
import org.apache.bcel.generic.IF_ICMPNE;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.jibx.runtime.JiBXException;
import org.jibx.util.StringStack;

/**
 * Method builder. Organizes and tracks the creation of a method, providing
 * convenience methods for common operations. This is customized for the needs
 * of JiBX, with some predetermined settings as appropriate. It supplies hash
 * code and equality checking based on the method signature and actual byte
 * code of the generated method, ignoring the method name.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class MethodBuilder extends BindingMethod
{
    //
    // Constants for code generation.
    
    public static final String FRAMEWORK_EXCEPTION_CLASS =
        "org.jibx.runtime.JiBXException";
    
    public static final String EXCEPTION_CONSTRUCTOR_SIGNATURE1 =
        "(Ljava/lang/String;)V";
    
    public static final String EXCEPTION_CONSTRUCTOR_SIGNATURE2 =
        "(Ljava/lang/String;Ljava/lang/Throwable;)V";
    
    public static final int SYNTHETIC_ACCESS_FLAG = 0x1000;
    
    //
    // Static data.
    
    /** Table of argument name lists (generated as needed). */
    protected static ArrayList s_argNameLists = new ArrayList();
    
    //
    // Actual instance data
    
    /** Builder for class instructions. */
    protected InstructionBuilder m_instructionBuilder;
    
    /** List of instructions in method definition. */
    private InstructionList m_instructionList;
    
    /** List of types currently on stack. */
    private StringStack m_stackState;
    
    /** Generator for constructing method. */
    protected MethodGen m_generator;
    
    /** Actual generated method information. */
    protected Method m_method;
    
    /** Method class item information. */
    protected ClassItem m_item;
    
    /** Value types associated with local variable slots. */
    private ArrayList m_localTypes;
    
    /** Exceptions needing to be handled in method (lazy create,
     <code>null</code> if not used). */
    protected ArrayList m_exceptions;
    
    /** Accumulated hash code from adding instructions. */
    protected int m_hashCode;
    
    /** Branch to be aimed at next appended instruction. */
    protected BranchWrapper[] m_targetBranches;
    
    /** Map for initialized properties (lazy create, <code>null</code> if not
     used). */
    protected HashMap m_valueMap;

    /**
     * Constructor. This sets up for constructing a method with public access.
     *
     * @param name method name to be built
     * @param ret method return type
     * @param args types of arguments
     * @param cf owning class file information
     * @param access flags for method access
     */
    protected MethodBuilder(String name, Type ret, Type[] args,
        ClassFile cf, int access) {
        super(cf);
        
        // make sure the dummy argument names are defined
        if (args.length >= s_argNameLists.size()) {
            
            // append to end of argument names list
            for (int i = s_argNameLists.size(); i <= args.length; i++) {
                String[] list = new String[i];
                if (i > 0) {
                    Object last = s_argNameLists.get(i-1);
                    System.arraycopy(last, 0, list, 0, i-1);
                    list[i-1] = "arg" + i;
                }
                s_argNameLists.add(list);
            }
            
        }
        
        // create the method generator with empty instruction list
        String[] names = (String[])s_argNameLists.get(args.length);
        m_instructionList = new InstructionList();
        m_stackState = new StringStack();
        m_instructionBuilder = cf.getInstructionBuilder();
        m_generator = new MethodGen(access | SYNTHETIC_ACCESS_FLAG, ret, args,
            names, name, cf.getName(), m_instructionList, cf.getConstPoolGen());
        
        // initialize local variables for method parameters
        m_localTypes = new ArrayList();
        if ((access & Constants.ACC_STATIC) == 0) {
            m_localTypes.add(cf.getName());
        }
        for (int i = 0; i < args.length; i++) {
            m_localTypes.add(args[i].toString());
            if (args[i].getSize() > 1) {
                m_localTypes.add(null);
            }
        }
    }

    /**
     * Get name of method being constructed.
     *
     * @return name of method being constructed
     */
    public String getName() {
        return m_generator.getName();
    }
    
    /**
     * Get signature.
     *
     * @return signature for method
     */
    public String getSignature() {
        return m_generator.getSignature();
    }
    
    /**
     * Get access flags.
     *
     * @return flags for access type of method
     */
    public int getAccessFlags() {
        return m_generator.getAccessFlags();
    }
    
    /**
     * Set access flags.
     *
     * @param flags access type to be set
     */
    public void setAccessFlags(int flags) {
        m_generator.setAccessFlags(flags);
    }
    
    /**
     * Get the actual method. This can only be called once code generation is
     * completed (after the {@link #codeComplete(boolean)} method is called).
     *
     * @return constructed method information
     */
    public Method getMethod() {
        if (m_method == null) {
            throw new IllegalStateException("Method still under construction");
        } else {
            return m_method;
        }
    }
    
    /**
     * Add keyed value to method definition.
     *
     * @param key retrieval key
     * @param value keyed value
     * @return prior value for key
     */
    public Object setKeyValue(Object key, Object value) {
        if (m_valueMap == null) {
            m_valueMap = new HashMap();
        }
        return m_valueMap.put(key, value);
    }
    
    /**
     * Get local variable for object.
     * 
     * @param key object key for local variable
     * @return local variable
     */
    public Object getKeyValue(Object key) {
        return m_valueMap == null ? null : m_valueMap.get(key);
    }

    /**
     * Add exception to those needing handling.
     *
     * @param name fully qualified name of exception class
     */
    public void addException(String name) {
        if (m_exceptions == null) {
            m_exceptions = new ArrayList();
        }
        if (!m_exceptions.contains(name)) {
            m_exceptions.add(name);
        }
    }

    /**
     * Add exceptions thrown by called method to those needing handling.
     *
     * @param method information for method to be handled
     */
    public void addMethodExceptions(ClassItem method) {
        String[] excepts = method.getExceptions();
        if (excepts != null) {
            for (int i = 0; i < excepts.length; i++) {
                addException(excepts[i]);
            }
        }
    }

    /**
     * Get first instruction in method.
     *
     * @return handle for first instruction in method
     */
    protected InstructionHandle getFirstInstruction() {
        return m_instructionList.getStart();
    }

    /**
     * Get last instruction in method.
     *
     * @return handle for last instruction in method
     */
    protected InstructionHandle getLastInstruction() {
        return m_instructionList.getEnd();
    }

    /**
     * Target branches if pending. This implements setting the target of
     * branch instructions supplied using the {@link #targetNext} method.
     *
     * @param inst handle for appended instruction
     */
    protected final void setTarget(InstructionHandle inst) {
        if (m_targetBranches != null) {
            
            // TODO: fix this ugly kludge with code rewrite
            // adjust stack with POPs if need to match size
            String[] types = m_stackState.toArray();
            if (m_targetBranches.length > 0) {
                boolean match = true;
                int depth = m_targetBranches[0].getStackState().length;
                for (int i = 1; i < m_targetBranches.length; i++) {
                    if (depth != m_targetBranches[i].getStackState().length) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    if (depth > types.length) {
                        BranchWrapper merge = new BranchWrapper
                            (m_instructionList.insert(inst, new GOTO(null)),
                            types, this);
                        String[] stack = m_targetBranches[0].getStackState();
                        m_stackState = new StringStack(stack);
                        InstructionHandle poph = m_instructionList.
                            insert(inst, InstructionConstants.POP);
                        for (int i = 0; i < m_targetBranches.length; i++) {
                            m_targetBranches[i].setTarget(poph, stack, this);
                        }
                        m_stackState.pop();
                        while (m_stackState.size() > types.length) {
                            m_instructionList.insert(inst,
                                InstructionConstants.POP);
                            m_stackState.pop();
                        }
                        merge.setTarget(inst, m_stackState.toArray(), this);
                        m_targetBranches = null;
                        return;
                    } else {
                        while (depth < types.length) {
                            m_instructionList.insert(inst,
                                InstructionConstants.POP);
                            m_stackState.pop();
                            types = m_stackState.toArray();
                        }
                    }
                }
            }
            
            // set all branch targets
            for (int i = 0; i < m_targetBranches.length; i++) {
                m_targetBranches[i].setTarget(inst, types, this);
            }
            m_targetBranches = null;
        }
    }
    
    /**
     * Generate description of current stack state.
     *
     * @return stack state description
     */
    private String describeStack() {
        StringBuffer buff = new StringBuffer();
        String[] types = m_stackState.toArray();
        for (int i = 0; i < types.length; i++) {
            buff.append("  ");
            buff.append(i);
            buff.append(": ");
            buff.append(types[i]);
            buff.append('\n');
        }
        return buff.toString();
    }

    /**
     * Verify that a pair of value types represent compatible types. This checks
     * for equal types or downcast object types.
     *
     * @param type actual known type of value
     * @param need type needed
     */
    private void verifyCompatible(String type, String need) {
        if (!need.equals(type)) {
            try {
                if ("<null>".equals(type)) {
                    if (ClassItem.isPrimitive(need)) {
                        throw new IllegalStateException
                            ("Internal error: Expected " + need +
                            " on stack , found null");
                    }
                } else if ("java.lang.Object".equals(need)) {
                    if (ClassItem.isPrimitive(type)) {
                        throw new IllegalStateException("Internal error: " +
                            "Expected object reference on stack, found " +
                            type + "\n full stack:\n" + describeStack());
                    }
                } else {
                    boolean match = false;
                    if ("int".equals(need)) {
                        match = "boolean".equals(type) ||
                            "short".equals(type) || "char".equals(type) ||
                            "byte".equals(type);
                    } else if ("int".equals(type)) {
                        match = "boolean".equals(need) ||
                            "short".equals(need) || "char".equals(need) ||
                            "byte".equals(need);
                    }
                    if (!match && !ClassItem.isAssignable(type, need)) {
                        throw new IllegalStateException
                            ("Internal error: Expected " + need +
                            " on stack, found " + type + "\n full stack:\n" +
                            describeStack());
                    }
                }
            } catch (JiBXException e) {
                throw new RuntimeException
                    ("Internal error: Attempting to compare types " + need +
                    " and " + type);
            }
        }
    }

    /**
     * Verify that at least the specified number of items are present on the
     * stack.
     *
     * @param count minimum number of items required
     */
    private void verifyStackDepth(int count) {
        if (m_stackState.size() < count) {
            throw new IllegalStateException
                ("Internal error: Too few values on stack\n full stack:\n" +
                describeStack());
        }
    }

    /**
     * Verify the top value in the stack state resulting from the current
     * instruction list.
     *
     * @param t1 expected type for top item on stack
     */
    private void verifyStack(String t1) {
        verifyStackDepth(1);
        verifyCompatible(m_stackState.peek(), t1);
    }
    
    /**
     * Verify the top value in the stack state resulting from the current
     * instruction list is an array.
     *
     * @return array item type
     */
    private String verifyArray() {
        verifyStackDepth(1);
        String type = m_stackState.peek();
        if (type.endsWith("[]")) {
            return type.substring(0, type.length()-2);
        } else {
            throw new IllegalStateException
                ("Internal error: Expected array type on stack , found " +
                type);
        }
    }
    
    /**
     * Verify the top value in the stack state resulting from the current
     * instruction list is an array of the specified type.
     *
     * @param type array item type
     */
    private void verifyArray(String type) {
        String atype = verifyArray();
        if (!atype.equals(type)) {
            throw new IllegalStateException
                ("Internal error: Expected array of " + type +
                " on stack , found array of " + atype);
        }
    }
    
    /**
     * Verify the top two values in the stack state resulting from the current
     * instruction list.
     *
     * @param t1 expected type for first item on stack
     * @param t2 expected type for second item on stack
     */
    private void verifyStack(String t1, String t2) {
        verifyStackDepth(2);
        verifyCompatible(m_stackState.peek(), t1);
        verifyCompatible(m_stackState.peek(1), t2);
    }

    /**
     * Verify the top values in the stack state resulting from the current
     * instruction list. This form checks only the actual call parameters.
     *
     * @param types expected parameter types on stack
     */
    private void verifyCallStack(String[] types) {
        
        // make sure there are enough items on stack
        int count = types.length;
        verifyStackDepth(count);
        
        // verify all parameter types for call
        for (int i = 0; i < count; i++) {
            int slot = count - i - 1;
            verifyCompatible(m_stackState.peek(slot), types[i]);
        }
    }

    /**
     * Verify the top values in the stack state resulting from the current
     * instruction list. This form checks both the object being called and the
     * actual call parameters.
     *
     * @param clas name of method class
     * @param types expected parameter types on stack
     */
    private void verifyCallStack(String clas, String[] types) {
        
        // start by verifying object reference
        int count = types.length;
        verifyStackDepth(count+1);
        verifyCompatible(m_stackState.peek(count), clas);
        
        // check values for call parameters
        verifyCallStack(types);
    }

    /**
     * Verify that the top value in the stack state resulting from the current
     * instruction list is an object reference.
     */
    private void verifyStackObject() {
        verifyStackDepth(1);
        String top = m_stackState.peek();
        if (ClassItem.isPrimitive(top)) {
            throw new IllegalStateException("Internal error: " +
                "Expected object reference on stack , found " +
                 m_stackState.peek() + "\n full stack:\n" + describeStack());
        }
    }

    /**
     * Append IFEQ branch instruction to method.
     *
     * @param src object responsible for generating branch
     * @return wrapper for appended conditional branch
     */
    public BranchWrapper appendIFEQ(Object src) {
        verifyStack("int");
        BranchHandle hand = m_instructionList.append(new IFEQ(null));
        setTarget(hand);
        m_stackState.pop();
        return new BranchWrapper(hand, m_stackState.toArray(), src);
    }

    /**
     * Append IFGE branch instruction to method.
     *
     * @param src object responsible for generating branch
     * @return wrapper for appended conditional branch
     */
    public BranchWrapper appendIFGE(Object src) {
        verifyStack("int");
        BranchHandle hand = m_instructionList.append(new IFGE(null));
        setTarget(hand);
        m_stackState.pop();
        return new BranchWrapper(hand, m_stackState.toArray(), src);
    }

    /**
     * Append IFLT branch instruction to method.
     *
     * @param src object responsible for generating branch
     * @return wrapper for appended conditional branch
     */
    public BranchWrapper appendIFLT(Object src) {
        verifyStack("int");
        BranchHandle hand = m_instructionList.append(new IFLT(null));
        setTarget(hand);
        m_stackState.pop();
        return new BranchWrapper(hand, m_stackState.toArray(), src);
    }

    /**
     * Append IFNE branch instruction to method.
     *
     * @param src object responsible for generating branch
     * @return wrapper for appended conditional branch
     */
    public BranchWrapper appendIFNE(Object src) {
        verifyStack("int");
        BranchHandle hand = m_instructionList.append(new IFNE(null));
        setTarget(hand);
        m_stackState.pop();
        return new BranchWrapper(hand, m_stackState.toArray(), src);
    }

    /**
     * Append IFNONNULL branch instruction to method.
     *
     * @param src object responsible for generating branch
     * @return wrapper for appended conditional branch
     */
    public BranchWrapper appendIFNONNULL(Object src) {
        verifyStackObject();
        BranchHandle hand = m_instructionList.append(new IFNONNULL(null));
        setTarget(hand);
        m_stackState.pop();
        return new BranchWrapper(hand, m_stackState.toArray(), src);
    }

    /**
     * Append IFNULL branch instruction to method.
     *
     * @param src object responsible for generating branch
     * @return wrapper for appended conditional branch
     */
    public BranchWrapper appendIFNULL(Object src) {
        verifyStackObject();
        BranchHandle hand = m_instructionList.append(new IFNULL(null));
        setTarget(hand);
        m_stackState.pop();
        return new BranchWrapper(hand, m_stackState.toArray(), src);
    }

    /**
     * Append IF_ICMPNE branch instruction to method.
     *
     * @param src object responsible for generating branch
     * @return wrapper for appended conditional branch
     */
    public BranchWrapper appendIF_ICMPNE(Object src) {
        verifyStack("int", "int");
        BranchHandle hand = m_instructionList.append(new IF_ICMPNE(null));
        setTarget(hand);
        m_stackState.pop(2);
        return new BranchWrapper(hand, m_stackState.toArray(), src);
    }

    /**
     * Append unconditional branch instruction to method.
     *
     * @param src object responsible for generating branch
     * @return wrapper for appended unconditional branch
     */
    public BranchWrapper appendUnconditionalBranch(Object src) {
        BranchHandle hand = m_instructionList.append(new GOTO(null));
        setTarget(hand);
        BranchWrapper wrapper =
            new BranchWrapper(hand, m_stackState.toArray(), src);
        m_stackState = null;
        return wrapper;
    }

    /**
     * Append compound instruction to method.
     *
     * @param ins instruction to be appended
     */
    private void append(CompoundInstruction ins) {
        setTarget(m_instructionList.append(ins));
    }

    /**
     * Append instruction to method.
     *
     * @param ins instruction to be appended
     */
    private void append(Instruction ins) {
        setTarget(m_instructionList.append(ins));
    }

    /**
     * Create load constant instruction and append to method. Builds the most
     * appropriate type of instruction for the value.
     *
     * @param value constant value to be loaded
     */
    public void appendLoadConstant(int value) {
        append(m_instructionBuilder.createLoadConstant(value));
        m_stackState.push("int");
    }

    /**
     * Create load constant instruction and append to method. Loads a
     * <code>String</code> reference from the constant pool.
     *
     * @param value constant value to be loaded
     */
    public void appendLoadConstant(String value) {
        if (value != null && value.length() > 0x7FFF) {
            throw new IllegalArgumentException("Internal error - value too long");
        }
        append(m_instructionBuilder.createLoadConstant(value));
        m_stackState.push("java.lang.String");
    }

    /**
     * Create load constant instruction and append to method. Loads an
     * unwrapped primitive value from the constant pool.
     *
     * @param value constant value to be loaded
     */
    public void appendLoadConstant(Object value) {
        append(m_instructionBuilder.createLoadConstant(value));
        if (value instanceof Integer || value instanceof Character ||
            value instanceof Short || value instanceof Boolean ||
            value instanceof Byte) {
            m_stackState.push("int");
        } else if (value instanceof Long) {
            m_stackState.push("long");
        } else if (value instanceof Float) {
            m_stackState.push("float");
        } else if (value instanceof Double) {
            m_stackState.push("double");
        } else {
            throw new IllegalArgumentException("Unknown argument type");
        }
    }

    /**
     * Create getfield instruction and append to method. Uses the target field
     * information to generate the instruction.
     *
     * @param item information for field to be gotton
     */
    public void appendGetField(ClassItem item) {
        verifyStack(item.getClassFile().getName());
        append(m_instructionBuilder.createGetField(item));
        m_stackState.pop();
        m_stackState.push(item.getTypeName());
    }

    /**
     * Create getstatic instruction and append to method. Uses the target field
     * information to generate the instruction.
     *
     * @param item information for field to be set
     */
    public void appendGetStatic(ClassItem item) {
        append(m_instructionBuilder.createGetStatic(item));
        m_stackState.push(item.getTypeName());
    }

    /**
     * Create get instruction and append to method. This generates either a
     * getstatic or a getfield instruction, as appropriate.
     *
     * @param item information for field to be gotten
     */
    public void appendGet(ClassItem item) {
        if (item.isStatic()) {
            appendGetStatic(item);
        } else {
            appendGetField(item);
        }
    }

    /**
     * Create putfield instruction and append to method. Uses the target field
     * information to generate the instruction.
     *
     * @param item information for field to be set
     */
    public void appendPutField(ClassItem item) {
        String tname = item.getTypeName();
        verifyStack(tname, item.getClassFile().getName());
        append(m_instructionBuilder.createPutField(item));
        m_stackState.pop(2);
    }

    /**
     * Create putstatic instruction and append to method. Uses the target field
     * information to generate the instruction.
     *
     * @param item information for field to be set
     */
    public void appendPutStatic(ClassItem item) {
        verifyStack(item.getTypeName());
        append(m_instructionBuilder.createPutStatic(item));
        m_stackState.pop();
    }

    /**
     * Create put instruction and append to method. This generates either a
     * putstatic or a putfield instruction, as appropriate.
     *
     * @param item information for field to be gotten
     */
    public void appendPut(ClassItem item) {
        if (item.isStatic()) {
            appendPutStatic(item);
        } else {
            appendPutField(item);
        }
    }

    /**
     * Create invoke instruction for static, member, or interface method and
     * append to method. Uses the target method information to generate the
     * correct instruction.
     *
     * @param item information for method to be called
     */
    public void appendCall(ClassItem item) {
        
        // process based on call type
        String[] types = item.getArgumentTypes();
        int count = types.length;
        if (item.getClassFile().isInterface()) {
            
            // process parameters and object reference for interface call
            verifyCallStack(item.getClassFile().getName(), types);
            append(m_instructionBuilder.createCallInterface(item));
            m_stackState.pop(count+1);
            
        } else if ((item.getAccessFlags() & Constants.ACC_STATIC) != 0) {
            
            // process only parameters for static call
            verifyCallStack(types);
            append(m_instructionBuilder.createCallStatic(item));
            if (count > 0) {
                m_stackState.pop(count);
            }
            
        } else {
            
            // process parameters and object reference for normal method call
            verifyCallStack(item.getClassFile().getName(), types);
            append(m_instructionBuilder.createCallVirtual(item));
            m_stackState.pop(count+1);
        }
        
        // adjust stack state to reflect result of call
        if (!"void".equals(item.getTypeName())) {
            m_stackState.push(item.getTypeName());
        }
    }

    /**
     * Create invoke static method instruction from signature and append to
     * method.
     *
     * @param method fully qualified class and method name
     * @param signature method signature in standard form
     */
    public void appendCallStatic(String method, String signature) {
        
        // verify all call parameters on stack
        String[] types = ClassItem.getParametersFromSignature(signature);
        verifyCallStack(types);
        
        // generate the actual method call
        append(m_instructionBuilder.createCallStatic(method, signature));
        
        // change stack state to reflect result of call
        if (types.length > 0) {
            m_stackState.pop(types.length);
        }
        String result = ClassItem.getTypeFromSignature(signature);
        if (!"void".equals(result)) {
            m_stackState.push(result);
        }
    }

    /**
     * Create invoke virtual method instruction from signature and append to
     * method.
     *
     * @param method fully qualified class and method name
     * @param signature method signature in standard form
     */
    public void appendCallVirtual(String method, String signature) {
        
        // verify all call parameters and object reference on stack
        String[] types = ClassItem.getParametersFromSignature(signature);
        int split = method.lastIndexOf('.');
        if (split < 0) {
            throw new IllegalArgumentException
                ("Internal error: Missing class name on method " + method);
        }
        verifyCallStack(method.substring(0, split), types);
        
        // generate the actual method call
        append(m_instructionBuilder.createCallVirtual(method, signature));
        
        // change stack state to reflect result of call
        m_stackState.pop(types.length+1);
        String result = ClassItem.getTypeFromSignature(signature);
        if (!"void".equals(result)) {
            m_stackState.push(result);
        }
    }

    /**
     * Create invoke interface method instruction from signature and append to
     * method.
     *
     * @param method fully qualified interface and method name
     * @param signature method signature in standard form
     */
    public void appendCallInterface(String method, String signature) {
        
        // verify all call parameters and object reference on stack
        String[] types = ClassItem.getParametersFromSignature(signature);
        int split = method.lastIndexOf('.');
        if (split < 0) {
            throw new IllegalArgumentException
                ("Internal error: Missing class name on method " + method);
        }
        verifyCallStack(method.substring(0, split), types);
        
        // generate the actual method call
        append(m_instructionBuilder.createCallInterface(method, signature));
        
        // change stack state to reflect result of call
        m_stackState.pop(types.length+1);
        String result = ClassItem.getTypeFromSignature(signature);
        if (!"void".equals(result)) {
            m_stackState.push(result);
        }
    }

    /**
     * Append instruction to create instance of class.
     *
     * @param name fully qualified class name
     */
    public void appendCreateNew(String name) {
        append(m_instructionBuilder.createNew(name));
        m_stackState.push(name);
    }

    /**
     * Create invoke initializer instruction from signature and append to
     * method.
     *
     * @param name fully qualified class name
     * @param signature method signature in standard form
     */
    public void appendCallInit(String name, String signature) {
        
        // verify all call parameters and object reference on stack
        String[] types = ClassItem.getParametersFromSignature(signature);
        verifyCallStack(name, types);
        
        // generate the actual method call
        append(m_instructionBuilder.createCallInit(name, signature));
        
        // change stack state to reflect result of call
        m_stackState.pop(types.length+1);
    }

    /**
     * Append instruction to create instance of array.
     *
     * @param type fully qualified type name of array elements
     */
    public void appendCreateArray(String type) {
        if (ClassItem.isPrimitive(type)) {
            String sig = Utility.getSignature(type);
            append(new NEWARRAY(Utility.typeOfSignature(sig)));
        } else if (type.endsWith("[]")) {
            String cname = Utility.getSignature(type + "[]");
            append(new MULTIANEWARRAY(m_instructionBuilder.
                getConstantPoolGen().addClass(cname), (short)1));
        } else {
            append(new ANEWARRAY(m_instructionBuilder.
                getConstantPoolGen().addClass(type)));
        }
        m_stackState.pop();
        m_stackState.push(type + "[]");
    }

    /**
     * Append check cast instruction (if needed).
     *
     * @param from fully qualified name of current type
     * @param to fully qualified name of desired type
     */
    public void appendCreateCast(String from, String to) {
        
        // verify current top of stack
        verifyStack(from);
        
        // check if any change of type
        if (!from.equals(to)) {
            
            // generate instruction and change stack state to match
            append(m_instructionBuilder.
                createCast(ClassItem.typeFromName(from),
                ClassItem.typeFromName(to)));
            m_stackState.pop();
            m_stackState.push(to);
        }
    }

    /**
     * Append check cast instruction from object (if needed).
     *
     * @param to fully qualified name of desired type
     */
    public void appendCreateCast(String to) {
        
        // verify current top of stack
        verifyStackObject();
        
        // check if any change of type
        if (!m_stackState.peek().equals(to)) {
            
            // generate instruction and change stack state to match
            append(m_instructionBuilder.
                createCast(Type.OBJECT, ClassItem.typeFromName(to)));
            m_stackState.pop();
            m_stackState.push(to);
        }
    }

    /**
     * Append instanceof check instruction.
     *
     * @param to fully qualified name of type to check
     */
    public void appendInstanceOf(String to) {
        
        // make sure the stack has an object reference
        verifyStackObject();
        
        // see if anything actually needs to be checked
        if ("java.lang.Object".equals(to)) {
            append(InstructionConstants.POP);
            appendLoadConstant(1);
        } else {
            append(m_instructionBuilder.
                createInstanceOf((ReferenceType)ClassItem.typeFromName(to)));
        }
        
        // change stack state to reflect results of added code
        m_stackState.pop();
        m_stackState.push("int");
    }
    
    /**
     * Add local variable to method. The current code in the method must have
     * the initial value for the variable on the stack. The scope of the
     * variable is defined from the last instruction to the end of the
     * method unless otherwise modified.
     * 
     * @param name local variable name (may be <code>null</code> to use default)
     * @param type variable type
     */
    protected LocalVariableGen createLocal(String name, Type type) {
        
        // verify top of stack
        verifyStack(type.toString());
        
        // create name if needed
        if (name == null) {
             name = "var" + m_generator.getLocalVariables().length;
        }
        
        // allocation local and store value
        LocalVariableGen var = m_generator.addLocalVariable
            (name, type, getLastInstruction(), null);
        append(InstructionFactory.createStore(type, var.getIndex()));
        
        // save type information for local variable slot
        int slot = var.getIndex();
        while (slot >= m_localTypes.size()) {
            m_localTypes.add(null);
        }
        m_localTypes.set(slot, type.toString());
        
        // change stack state to reflect result
        m_stackState.pop();
        return var;
    }
    
    /**
     * Add local variable to method. The current code in the method must have
     * the initial value for the variable on the stack. The scope of the
     * variable is defined from the preceding instruction to the end of the
     * method.
     * 
     * @param name local variable name
     * @param type variable type
     * @return local variable slot number
     */
    public int addLocal(String name, Type type) {
        LocalVariableGen var = createLocal(name, type);
        return var.getIndex();
    }

    /**
     * Append instruction to load local variable.
     *
     * @param slot local variable slot to load
     */
    public void appendLoadLocal(int slot) {
        String type = (String)m_localTypes.get(slot);
        if (type == null) {
            throw new IllegalArgumentException
                ("Internal error: No variable defined at position " + slot);
        }
        append(InstructionFactory.
            createLoad(ClassItem.typeFromName(type), slot));
        m_stackState.push(type);
    }

    /**
     * Append instruction to store local variable.
     *
     * @param slot local variable slot to store
     */
    public void appendStoreLocal(int slot) {
        String type = (String)m_localTypes.get(slot);
        if (type == null) {
            throw new IllegalArgumentException
                ("Internal error: No variable defined at position " + slot);
        }
        verifyStack(type);
        append(InstructionFactory.
            createStore(ClassItem.typeFromName(type), slot));
        m_stackState.pop();
    }

    /**
     * Append instruction to increment local integer variable.
     *
     * @param inc amount of incrment
     * @param slot local variable slot to load
     */
    public void appendIncrementLocal(int inc, int slot) {
        String type = (String)m_localTypes.get(slot);
        if (type == null) {
            throw new IllegalArgumentException
                ("Internal error: No variable defined at position " + slot);
        } else if (!"int".equals(type)) {
            throw new IllegalArgumentException("Internal error: Variable at " +
                slot + " is " + type + ", not int");
        }
        append(new IINC(slot, inc));
    }

    /**
     * Append simple return.
     */
    public void appendReturn() {
        append(InstructionConstants.RETURN);
        m_stackState = null;
    }

    /**
     * Append typed return.
     *
     * @param type returned type (may be <code>Type.VOID</code>)
     */
    public void appendReturn(Type type) {
        
        // verify and return the object reference
        if (type != Type.VOID) {
            verifyStack(type.toString());
        }
        append(InstructionFactory.createReturn(type));
        
        // set open stack state for potential continuation code
        m_stackState = null;
    }

    /**
     * Append typed return.
     *
     * @param type returned type (may be <code>void</code>)
     */
    public void appendReturn(String type) {
        
        // verify stack and generate return
        if ("void".equals(type)) {
            append(InstructionConstants.RETURN);
        } else {
            verifyStack(type);
            if (ClassItem.isPrimitive(type)) {
                if ("int".equals(type) || "char".equals(type) ||
                    "short".equals(type) || "boolean".equals(type)) {
                    append(InstructionConstants.IRETURN);
                } else if ("long".equals(type)) {
                    append(InstructionConstants.LRETURN);
                } else if ("float".equals(type)) {
                    append(InstructionConstants.FRETURN);
                } else if ("double".equals(type)) {
                    append(InstructionConstants.DRETURN);
                } else {
                    throw new IllegalArgumentException("Unknown argument type");
                }
            } else {
                append(InstructionConstants.ARETURN);
            }
        }
        
        // set open stack state for potential continuation code
        m_stackState = null;
    }

    /**
     * Append exception throw.
     */
    public void appendThrow() {
        append(InstructionConstants.ATHROW);
        m_stackState = null;
    }
    
    /**
     * Append appropriate array load to the instruction list.
     * 
     * @param type array item type expected
     */
    public void appendALOAD(String type) {
        verifyStack("int");
        m_stackState.pop();
        verifyArray(type);
        if ("byte".equals(type) || "boolean".equals(type)) {
            append(InstructionConstants.BALOAD);
        } else if ("char".equals(type)) {
            append(InstructionConstants.CALOAD);
        } else if ("double".equals(type)) {
            append(InstructionConstants.DALOAD);
        } else if ("float".equals(type)) {
            append(InstructionConstants.FALOAD);
        } else if ("int".equals(type)) {
            append(InstructionConstants.IALOAD);
        } else if ("long".equals(type)) {
            append(InstructionConstants.LALOAD);
        } else if ("short".equals(type)) {
            append(InstructionConstants.SALOAD);
        } else {
            append(InstructionConstants.AALOAD);
        }
        m_stackState.pop();
        m_stackState.push(type);
    }
    
    /**
     * Append an AASTORE to the instruction list. Doesn't actually check the
     * types, just the count of items present.
     */
    public void appendAASTORE() {
        verifyStackDepth(3);
        String vtype = m_stackState.pop();
        verifyStack("int");
        m_stackState.pop();
        String atype = verifyArray();
        verifyCompatible(vtype, atype);
        m_stackState.pop();
        append(InstructionConstants.AASTORE);
    }
    
    /**
     * Append the appropriate array store to the instruction list.
     * 
     * @param type array item type expected
     */
    public void appendASTORE(String type) {
        verifyStackDepth(3);
        String vtype = m_stackState.pop();
        verifyStack("int");
        m_stackState.pop();
        String atype = verifyArray();
        verifyCompatible(vtype, atype);
        m_stackState.pop();
        if ("byte".equals(type) || "boolean".equals(type)) {
            append(InstructionConstants.BASTORE);
        } else if ("char".equals(type)) {
            append(InstructionConstants.CASTORE);
        } else if ("double".equals(type)) {
            append(InstructionConstants.DASTORE);
        } else if ("float".equals(type)) {
            append(InstructionConstants.FASTORE);
        } else if ("int".equals(type)) {
            append(InstructionConstants.IASTORE);
        } else if ("long".equals(type)) {
            append(InstructionConstants.LASTORE);
        } else if ("short".equals(type)) {
            append(InstructionConstants.SASTORE);
        } else {
            append(InstructionConstants.AASTORE);
        }
    }
    
    /**
     * Append an ACONST_NULL to the instruction list.
     */
    public void appendACONST_NULL() {
        append(InstructionConstants.ACONST_NULL);
        m_stackState.push("<null>");
    }
    
    /**
     * Append an ARRAYLENGTH to the instruction list.
     */
    public void appendARRAYLENGTH() {
        verifyArray();
        append(InstructionConstants.ARRAYLENGTH);
        m_stackState.pop();
        m_stackState.push("int");
    }
    
    /**
     * Append an DCMPG to the instruction list.
     */
    public void appendDCMPG() {
        verifyStack("double", "double");
        append(InstructionConstants.DCMPG);
        m_stackState.pop(2);
        m_stackState.push("int");
    }
    
    /**
     * Append a DUP to the instruction list.
     */
    public void appendDUP() {
        verifyStackDepth(1);
        String type = m_stackState.peek();
        if ("long".equals(type) || "double".equals(type)) {
            throw new IllegalStateException
                ("Internal error: DUP splits long value");
        }
        append(InstructionConstants.DUP);
        m_stackState.push(m_stackState.peek());
    }
    
    /**
     * Append a DUP2 to the instruction list.
     */
    public void appendDUP2() {
        verifyStackDepth(1);
        append(InstructionConstants.DUP2);
        m_stackState.push(m_stackState.peek());
    }
    
    /**
     * Append a DUP_X1 to the instruction list.
     */
    public void appendDUP_X1() {
        verifyStackDepth(2);
        String type = m_stackState.peek();
        if ("long".equals(type) || "double".equals(type)) {
            throw new IllegalStateException
                ("Internal error: DUP_X1 splits long value");
        }
        append(InstructionConstants.DUP_X1);
        String hold0 = m_stackState.pop();
        String hold1 = m_stackState.pop();
        m_stackState.push(hold0);
        m_stackState.push(hold1);
        m_stackState.push(hold0);
    }
    
    /**
     * Append an FCMPG to the instruction list.
     */
    public void appendFCMPG() {
        verifyStack("float", "float");
        append(InstructionConstants.FCMPG);
        m_stackState.pop(2);
        m_stackState.push("int");
    }
    
    /**
     * Append an IASTORE to the instruction list. Doesn't actually check the
     * types, just the count of items present.
     */
    public void appendIASTORE() {
        verifyStackDepth(3);
        append(InstructionConstants.IASTORE);
        m_stackState.pop(3);
    }
    
    /**
     * Append an ICONST_0 to the instruction list.
     */
    public void appendICONST_0() {
        append(InstructionConstants.ICONST_0);
        m_stackState.push("int");
    }
    
    /**
     * Append an ICONST_1 to the instruction list.
     */
    public void appendICONST_1() {
        append(InstructionConstants.ICONST_1);
        m_stackState.push("int");
    }
    
    /**
     * Append an ISUB to the instruction list.
     */
    public void appendISUB() {
        verifyStack("int", "int");
        append(InstructionConstants.ISUB);
        m_stackState.pop(1);
    }
    
    /**
     * Append an IXOR to the instruction list.
     */
    public void appendIXOR() {
        verifyStack("int", "int");
        append(InstructionConstants.IXOR);
        m_stackState.pop(1);
    }
    
    /**
     * Append an LCMP to the instruction list.
     */
    public void appendLCMP() {
        verifyStack("long", "long");
        append(InstructionConstants.LCMP);
        m_stackState.pop(2);
        m_stackState.push("int");
    }
    
    /**
     * Append a POP to the instruction list.
     */
    public void appendPOP() {
        verifyStackDepth(1);
        String type = m_stackState.peek();
        if ("long".equals(type) || "double".equals(type)) {
            throw new IllegalStateException
                ("Internal error: POP splits long value");
        }
        append(InstructionConstants.POP);
        m_stackState.pop();
    }
    
    /**
     * Append a POP2 to the instruction list.
     */
    public void appendPOP2() {
        verifyStackDepth(1);
        String type = m_stackState.peek();
        if (!"long".equals(type) && !"double".equals(type)) {
            throw new IllegalStateException
                ("Internal error: POP2 requires long value");
        }
        append(InstructionConstants.POP2);
        m_stackState.pop();
    }
    
    /**
     * Append a SWAP to the instruction list.
     */
    public void appendSWAP() {
        verifyStackDepth(2);
        append(InstructionConstants.SWAP);
        String hold0 = m_stackState.pop();
        String hold1 = m_stackState.pop();
        m_stackState.push(hold0);
        m_stackState.push(hold1);
    }
    
    /**
     * Append instructions to exchange a single-word value on the top of the
     * stack with the double-word value below it on the stack.
     */
    public void appendSWAP1For2() {
        verifyStackDepth(2);
        append(InstructionConstants.DUP_X2);
        append(InstructionConstants.POP);
        String hold0 = m_stackState.pop();
        String hold1 = m_stackState.pop();
        m_stackState.push(hold0);
        m_stackState.push(hold1);
    }
    
    /**
     * Append a compound instruction to the list as a branch target.
     *
     * @param inst compound instruction to be appended as branch target
     * @return branch target information
     */
    private BranchTarget appendTargetInstruction(CompoundInstruction inst) {
        String[] types = m_stackState.toArray();
        InstructionHandle hand = m_instructionList.append(inst);
        return new BranchTarget(hand, types);
    }
    
    /**
     * Append an instruction to the list as a branch target.
     *
     * @param inst instruction to be appended as branch target
     * @return branch target information
     */
    private BranchTarget appendTargetInstruction(Instruction inst) {
        String[] types = m_stackState.toArray();
        InstructionHandle hand = m_instructionList.append(inst);
        return new BranchTarget(hand, types);
    }
    
    /**
     * Append a NOP to the instruction list as a branch target.
     *
     * @return branch target information
     */
    public BranchTarget appendTargetNOP() {
        return appendTargetInstruction(InstructionConstants.NOP);
    }
    
    /**
     * Append an ACONST_NULL to the instruction list as a branch target.
     *
     * @return branch target information
     */
    public BranchTarget appendTargetACONST_NULL() {
        BranchTarget target = appendTargetInstruction
            (InstructionConstants.ACONST_NULL);
        m_stackState.push("<null>");
        return target;
    }
    
    /**
     * Append a load constant instruction as a branch target. Builds the most
     * appropriate type of instruction for the value.
     *
     * @param value constant value to be loaded
     * @return branch target information
     */
    public BranchTarget appendTargetLoadConstant(int value) {
        BranchTarget target = appendTargetInstruction(m_instructionBuilder.
            createLoadConstant(value));
        m_stackState.push("int");
        return target;
    }
    
    /**
     * Append a load constant instruction as a branch target. Loads a
     * <code>String</code> reference from the constant pool.
     *
     * @param value constant value to be loaded
     * @return branch target information
     */
    public BranchTarget appendTargetLoadConstant(String value) {
        BranchTarget target = appendTargetInstruction(m_instructionBuilder.
            createLoadConstant(value));
        m_stackState.push("java.lang.String");
        return target;
    }
    
    /**
     * Append instruction to create instance of class as a branch target.
     *
     * @param name fully qualified class name
     * @return branch target information
     */
    public BranchTarget appendTargetCreateNew(String name) {
        BranchTarget target =
            appendTargetInstruction(m_instructionBuilder.createNew(name));
        m_stackState.push(name);
        return target;
    }

    /**
     * Internal append instruction to create instance of class. This is used by
     * subclasses when they need access to the actual instruction handle.
     *
     * @param name fully qualified class name
     */
    protected InstructionHandle internalAppendCreateNew(String name) {
        InstructionHandle handle = m_instructionList.append
            (m_instructionBuilder.createNew(name));
        m_stackState.push(name);
        return handle;
    }

    /**
     * Check if top item on stack is a long value.
     * 
     * @return <code>true</code> if long value, <code>false</code> if not
     */
    public boolean isStackTopLong() {
        verifyStackDepth(1);
        String type = m_stackState.peek();
        return "long".equals(type) || "double".equals(type);
    }

    /**
     * Initialize stack state to match branch source. This can be used to set
     * the expected stack state following an unconditional transfer of control
     * instruction. The principle here is that the code to be generated must be
     * reached by a branch, so the stack state must match that of the branch
     * source.
     *
     * @param branch wrapper for branch to be for stack initialization
     */
    public void initStackState(BranchWrapper branch) {
        m_stackState = new StringStack(branch.getStackState());
    }

    /**
     * Initialize stack state to partially match branch source. This can be used
     * to set the expected stack state following an unconditional transfer of
     * control instruction. The specified number of items are removed from the
     * branch stack, with the assumption that code to add these items will be
     * appended before the branch join point is reached.
     *
     * @param branch wrapper for branch to be for stack initialization
     * @param pop number of items to be removed from branch source stack state
     */
    public void initStackState(BranchWrapper branch, int pop) {
        m_stackState = new StringStack(branch.getStackState());
        if (pop > 0) {
            m_stackState.pop(pop);
        }
    }

    /**
     * Initialize stack state to array of value types. This can be used to set
     * the expected stack state following an unconditional transfer of control
     * instruction.
     *
     * @param types array of type names on stack
     */
    protected void initStackState(String[] types) {
        m_stackState = new StringStack(types);
    }

    /**
     * Set branch target as next instruction added to method. This effectively
     * sets up a state trigger for the next append operation. The appended
     * instruction is set as the target for the branch. This requires that
     * instructions are only appended using the methods supplied in this class.
     *
     * @param branch wrapper for branch to be aimed at next instruction (may be
     * <code>null</code>, in which case nothing is done)
     */
    public void targetNext(BranchWrapper branch) {
        if (branch != null) {
            if (m_targetBranches == null) {
                m_targetBranches = new BranchWrapper[] { branch };
                if (m_stackState == null) {
                    m_stackState = new StringStack(branch.getStackState());
                }
            } else {
                int length = m_targetBranches.length;
                BranchWrapper[] wrappers = new BranchWrapper[length+1];
                System.arraycopy(m_targetBranches, 0, wrappers, 0, length);
                wrappers[length] = branch;
                m_targetBranches = wrappers;
            }
        }
    }

    /**
     * Set branch targets as next instruction added to method. This effectively
     * sets up a state trigger for the next append operation. The appended
     * instruction is set as the target for all the branches. This requires that
     * instructions are only appended using the methods supplied in this class.
     *
     * @param branches wrappers for branches to be aimed at next instruction
     * (may be <code>null</code>, in which case nothing is done)
     */
    public void targetNext(BranchWrapper[] branches) {
        if (branches != null && branches.length > 0) {
            if (m_targetBranches == null) {
                m_targetBranches = branches;
                if (m_stackState == null) {
                    m_stackState = new StringStack(branches[0].getStackState());
                }
            } else {
                int offset = m_targetBranches.length;
                int length = offset + branches.length;
                BranchWrapper[] wrappers = new BranchWrapper[length];
                System.arraycopy(m_targetBranches, 0, wrappers, 0, offset);
                System.arraycopy(branches, 0, wrappers, offset,
                    branches.length);
                m_targetBranches = wrappers;
            }
        }
    }

    /**
     * Process accumulated exceptions. Each subclass must implement this
     * method to perform the appropriate handling of the checked exceptions
     * that may be thrown in the constructed method.
     */
    protected abstract void handleExceptions();

    /**
     * Complete method construction. Finalizes the instruction list and
     * generates the byte code for the constructed method, then computes the
     * hash code based on the byte code. If requested, an appropriate suffix is
     * tacked on the end of the supplied name in order to make sure that it will
     * not be duplicated (even in a superclass or subclass).
     * 
     * @param suffix add suffix to make method name unique
     */
    public void codeComplete(boolean suffix) {
        if (m_targetBranches != null) {
            throw new IllegalStateException
                ("Method complete with pending branch target");
        }
        if (m_exceptions != null) {
            handleExceptions();
        }
        if (suffix) {
            m_generator.setName(getClassFile().
                makeUniqueMethodName(m_generator.getName()));
        }
        m_generator.setMaxStack();
        m_generator.setMaxLocals();
        m_instructionList.setPositions(true);
        m_method = m_generator.getMethod();
        m_instructionList.dispose();
        m_hashCode = computeMethodHash(m_method);
    }
    
    /**
     * Get the method item.
     *
     * @return method item information
     */
    public ClassItem getItem() {
        if (m_item == null) {
            throw new IllegalStateException("Method not added to class");
        } else {
            return m_item;
        }
    }

    /**
     * Get hash code. This is based only on the byte code in the method, and
     * is only valid after the {@link #codeComplete} method is called.
     * 
     * @return hash code based on code sequence
     */
    public int hashCode() {
        if (m_method == null) {
            throw new IllegalStateException("Method still under construction");
        } else {
            return m_hashCode;
        }
    }

    /**
     * Add constructed method to class. Makes the method callable, generating
     * the method information.
     * 
     * @return added method information
     */
    public ClassItem addMethod() {
        if (m_method == null) {
            throw new IllegalStateException("Method not finalized.");
        } else {
            m_item = getClassFile().addMethod(m_method);
            return m_item;
        }
    }
}
/*
Copyright (c) 2003-2010, Dennis M. Sosnoski.
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

package org.jibx.binding.def;

import java.util.ArrayList;

import org.jibx.binding.classes.BranchWrapper;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.ClassItem;
import org.jibx.binding.classes.ContextMethodBuilder;
import org.jibx.binding.classes.MethodBuilder;
import org.jibx.binding.model.ClassUtils;
import org.jibx.binding.util.IntegerCache;
import org.jibx.runtime.JiBXException;

/**
 * Property definition from binding. This organizes shared information for
 * bindings linked to fields or get/set methods of an object, and provides
 * methods for related code generation.
 *
 * @author Dennis M. Sosnoski
 */
public class PropertyDefinition
{
    //
    // Constants and such related to code generation.
    
    // recognized test-method signatures.
    private static final String[] TEST_METHOD_SIGNATURES =
    {
        "(Lorg/jibx/runtime/IMarshallingContext;)Z",
        "()Z"
    };
    
    // recognized get-method signatures.
    private static final String[] GET_METHOD_SIGNATURES =
    {
        "(Lorg/jibx/runtime/IMarshallingContext;)",
        "()"
    };
    
    // recognized flag-method signatures.
    private static final String[] FLAG_METHOD_SIGNATURES =
    {
        "(ZLorg/jibx/runtime/IMarshallingContext;)",
        "(Z)"
    };
    
    //
    // Actual instance data
    
    /** Reference to "this" property of object flag. */
    private boolean m_isThis;
    
    /** Reference to implicit value from collection. */
    private boolean m_isImplicit;

    /** Optional item flag. */
    private boolean m_isOptional;

    /** Containing object context. */
    private final IContextObj m_objContext;

    /** Fully qualified name of actual type of value. */
    private final String m_typeName;
    
    /** Fully qualified name of declared type of value loaded. */
    private final String m_getValueType;
    
    /** Fully qualified name of declared type of value stored. */
    private final String m_setValueType;

    /** Information for field (if given, may be <code>null</code>). */
    private final ClassItem m_fieldItem;

    /** Information for test method (if given, may be <code>null</code>). */
    private final ClassItem m_testMethod;
    
    /** Information for flag method (if given, may be <code>null</code>). */
    private final ClassItem m_flagMethod;

    /** Information for get method (if given, may be <code>null</code>). */
    private final ClassItem m_getMethod;

    /** Information for set method (if given, may be <code>null</code>). */
    private final ClassItem m_setMethod;

    /**
     * Constructor.
     *
     * @param parent containing binding definition structure
     * @param obj containing object context
     * @param type fully qualified name of type
     * @param isthis "this" object reference flag
     * @param opt optional property flag
     * @param fname containing object field name for property (may be
     * <code>null</code>)
     * @param test containing object method to test for property present (may be
     * <code>null</code>)
     * @param flag containing object method to flag property present (may be
     * <code>null</code>)
     * @param get containing object method to get property value (may be
     * <code>null</code>)
     * @param set containing object method to set property value (may be
     * <code>null</code>)
     * @throws JiBXException if configuration error
     */
    public PropertyDefinition(IContainer parent, IContextObj obj, String type,
        boolean isthis, boolean opt, String fname, String test, String flag,
        String get, String set) throws JiBXException {
        m_objContext = obj;
        m_isThis = isthis;
        m_isOptional = opt;
        ClassFile cf = m_objContext.getBoundClass().getClassFile();
        m_isImplicit = false;
        String dtype = null;
        String gtype = null;
        String stype = null;
        if (isthis) {
            if (type == null) {
                dtype = gtype = stype = cf.getName();
            } else {
                dtype = gtype = stype = type;
            }
        }
        if (fname == null) {
            m_fieldItem = null;
        } else {
            m_fieldItem = cf.getField(fname);
            dtype = gtype = stype = m_fieldItem.getTypeName();
        }
        if (test == null) {
            m_testMethod = null;
        } else {
            m_testMethod = cf.getMethod(test, TEST_METHOD_SIGNATURES);
            if (m_testMethod == null) {
                throw new JiBXException("test-method " + test +
                    " not found in class " + cf.getName());
            }
        }
        if (get == null) {
            m_getMethod = null;
        } else {
            m_getMethod = cf.getMethod(get, GET_METHOD_SIGNATURES);
            if (m_getMethod == null) {
                throw new JiBXException("get-method " + get +
                    " not found in class " + cf.getName());
            } else {
                gtype = m_getMethod.getTypeName();
                if (dtype == null) {
                    dtype = gtype;
                }
            }
        }
        if (set == null) {
            m_setMethod = null;
        } else {
            
            // need to handle overloads, so generate possible signatures
            ArrayList sigs = new ArrayList();
            if (m_getMethod != null) {
                String psig = ClassUtils.getSignature(gtype);
                sigs.add("(" + psig +
                    "Lorg/jibx/runtime/IUnmarshallingContext;" + ")V");
                sigs.add("(" + psig + ")V");
            }
            if (type != null) {
                String psig = ClassUtils.getSignature(type);
                sigs.add("(" + psig +
                    "Lorg/jibx/runtime/IUnmarshallingContext;" + ")V");
                sigs.add("(" + psig + ")V");
            }
            if (m_fieldItem != null) {
                String psig = m_fieldItem.getSignature();
                sigs.add("(" + psig +
                    "Lorg/jibx/runtime/IUnmarshallingContext;" + ")V");
                sigs.add("(" + psig + ")V");
            }
            sigs.add
                ("(Ljava/lang/Object;Lorg/jibx/runtime/IUnmarshallingContext;)V");
            sigs.add("(Ljava/lang/Object;)V");
            
            // set method needs verification of argument and return type
            ClassItem setmeth = cf.getMethod(set,
                (String[])sigs.toArray(new String[0]));
            if (setmeth == null) {
                
                // nothing known about signature, try anything by name
                setmeth = cf.getMethod(set, "");
                if (setmeth != null) {
                    if (!setmeth.getTypeName().equals("void") ||
                        setmeth.getArgumentCount() > 2) {
                        setmeth = null;
                    } else if (setmeth.getArgumentCount() == 2) {
                        String xtype = setmeth.getArgumentType(1);
                        if (!"org.jibx.runtime.IUnmarshallingContext".equals(xtype)) {
                            setmeth = null;
                        }
                    }
                }
            }
            
            // check if method found
            m_setMethod = setmeth;
            if (m_setMethod == null) {
                throw new JiBXException("set-method " + set +
                    " not found in class " + cf.getName());
            } else {
                stype = m_setMethod.getArgumentType(0);
                if (dtype == null) {
                    dtype = stype;
                }
            }
        }
        if (flag == null) {
            m_flagMethod = null;
        } else {
            m_flagMethod = cf.getMethod(flag, FLAG_METHOD_SIGNATURES);
            if (m_flagMethod == null) {
                throw new JiBXException("flag-method " + flag +
                    " not found in class " + cf.getName());
            } else if (stype == null) {
                stype = "java.lang.String";
                if (dtype == null) {
                    dtype = stype;
                }
            }
        }
        if (gtype == null) {
            gtype = "java.lang.Object";
        }
        m_getValueType = gtype;
        m_setValueType = stype;

        // check that enough information is supplied
        BindingDefinition root = parent.getBindingRoot();
        if (!isthis && m_fieldItem == null) {
            if (root.isInput() && m_setMethod == null && m_flagMethod == null) {
                throw new JiBXException
                    ("Missing way to set value for input binding");
            }
            if (root.isOutput() && m_getMethod == null &&
                (m_flagMethod == null || m_testMethod == null)) {
                throw new JiBXException
                    ("Missing way to get value for output binding");
            }
        }
        
        // check that type information is consistent
        if (type == null) {
            m_typeName = dtype;
        } else {
            m_typeName = type;
            boolean valid = true;
            if (isthis) {
                valid = ClassItem.isAssignable(dtype, type);
            } else {
                if (root.isInput()) {
                    valid = ClassItem.isAssignable(type, m_setValueType) ||
                        ClassItem.isAssignable(m_setValueType, type);
                }
                if (valid && root.isOutput()) {
                    valid = ClassItem.isAssignable(type, m_getValueType) ||
                        ClassItem.isAssignable(m_getValueType, type);
                }
            }
            if (!valid) {
                throw new JiBXException
                    ("Incompatible types for property definition");
            }
        }
    }

    /**
     * Constructor for "this" object reference.
     *
     * @param obj containing object context
     * @param opt optional property flag
     */
    public PropertyDefinition(IContextObj obj, boolean opt) {
        m_objContext = obj;
        m_isThis = true;
        m_isImplicit = false;
        m_isOptional = opt;
        ClassFile cf = m_objContext.getBoundClass().getClassFile();
        m_fieldItem = m_testMethod = m_flagMethod = m_getMethod = m_setMethod = null;
        m_typeName = m_getValueType = m_setValueType = cf.getName();
    }

    /**
     * Constructor for implicit object reference.
     *
     * @param type object type supplied
     * @param obj containing object context
     * @param opt optional property flag
     */
    public PropertyDefinition(String type, IContextObj obj, boolean opt) {
        m_objContext = obj;
        m_isImplicit = true;
        m_isThis = false;
        m_isOptional = opt;
        m_fieldItem = m_testMethod = m_flagMethod = m_getMethod = m_setMethod = null;
        m_typeName = m_getValueType = m_setValueType = type;
    }
    
    /**
     * Copy constructor.
     * 
     * @param original
     */
    public PropertyDefinition(PropertyDefinition original) {
        m_isThis = original.m_isThis;
        m_isImplicit = original.m_isImplicit;
        m_isOptional = original.m_isOptional;
        m_objContext = original.m_objContext;
        m_typeName = original.m_typeName;
        m_getValueType = original.m_getValueType;
        m_setValueType = original.m_setValueType;
        m_fieldItem = original.m_fieldItem;
        m_testMethod = original.m_testMethod;
        m_flagMethod = original.m_flagMethod;
        m_getMethod = original.m_getMethod;
        m_setMethod = original.m_setMethod;
    }

    /**
     * Check if property is "this" reference for object.
     *
     * @return <code>true</code> if reference to "this", <code>false</code> if
     * not
     */
    public boolean isThis() {
        return m_isThis;
    }

    /**
     * Check if property is implicit value from collection.
     *
     * @return <code>true</code> if implicit, <code>false</code> if not
     */
    public boolean isImplicit() {
        return m_isImplicit;
    }
    
    /**
     * Switch property from "this" to "implicit".
     */
    public void switchProperty() {
        m_isThis = false;
        m_isImplicit = true;
    }

    /**
     * Check if property is optional.
     *
     * @return <code>true</code> if optional, <code>false</code> if required
     */
    public boolean isOptional() {
        return m_isOptional;
    }

    /**
     * Set flag for an optional property.
     *
     * @param opt <code>true</code> if optional property, <code>false</code> if
     * not
     */
    public void setOptional(boolean opt) {
        m_isOptional = opt;
    }
    
    /**
     * Check if the value can be loaded.
     * 
     * @return <code>true</code> if loadable, <code>false</code> if not
     */
    public boolean isLoadable() {
        return m_getMethod != null ||  m_fieldItem != null;
    }

    /**
     * Get property name. If a field is defined this is the same as the field;
     * otherwise it is either the get method name (with leading "get" stripped,
     * if present) or the set method (with leading "set" stripped, if present),
     * whichever is found.
     * 
     * @return name for this property
     */
    public String getName() {
        if (m_isThis) {
            return "this";
        } else if (m_fieldItem != null) {
            return m_fieldItem.getName();
        } else if (m_getMethod != null) {
            String name = m_getMethod.getName();
            if (name.startsWith("get") && name.length() > 3) {
                name = name.substring(3);
            }
            return name;
        } else if (m_setMethod != null) {
            String name = m_setMethod.getName();
            if (name.startsWith("set") && name.length() > 3) {
                name = name.substring(3);
            }
            return name;
        } else {
            return "item";
        }
    }

    /**
     * Get declared type fully qualified name.
     *
     * @return fully qualified class name of declared type
     */
    public String getTypeName() {
        return m_typeName;
    }

    /**
     * Get value type as fully qualified name for loaded property value.
     *
     * @return fully qualified class name of value type
     */
    public String getGetValueType() {
        return m_getValueType;
    }

    /**
     * Get value type as fully qualified name for stored property value.
     *
     * @return fully qualified class name of value type
     */
    public String getSetValueType() {
        return m_setValueType;
    }

    /**
     * Check if property has presence test. Code needs to be generated to check
     * for the presence of the property if it is optional and either a test
     * method is defined or the value is an object reference.
     *
     * @return <code>true</code> if presence test needed, <code>false</code> if
     * not
     */
    public boolean hasTest() {
        return isOptional() && !isImplicit() &&
            (m_testMethod != null || !ClassItem.isPrimitive(m_typeName));
    }

    /**
     * Check if property is test only.
     *
     * @return <code>true</code> if test-only property, <code>false</code> if
     * not
     */
    public boolean isTestOnly() {
        return m_testMethod != null && m_getMethod == null &&
            m_fieldItem == null;
    }

    /**
     * Check if property has flag method.
     *
     * @return <code>true</code> if flag method defined, <code>false</code> if
     * not
     */
    public boolean hasFlag() {
        return m_flagMethod != null;
    }

    /**
     * Check if property is flag only.
     *
     * @return <code>true</code> if flag-only property, <code>false</code> if
     * not
     */
    public boolean isFlagOnly() {
        return m_flagMethod != null && m_getMethod == null &&
            m_fieldItem == null;
    }
    
    /**
     * Append instruction to duplicate the value on the stack. This will use the
     * appropriate instruction for the size of the value.
     *
     * @param mb
     */
    private void duplicateValue(MethodBuilder mb) {
        if ("long".equals(m_typeName) || "double".equals(m_typeName)) {
            mb.appendDUP2();
        } else {
            mb.appendDUP();
        }
    }
    
    /**
     * Append instruction to pop the value from the stack. This will use the
     * appropriate instruction for the size of the value.
     *
     * @param mb
     */
    private void discardValue(MethodBuilder mb) {
        if ("long".equals(m_typeName) || "double".equals(m_typeName)) {
            mb.appendPOP2();
        } else {
            mb.appendPOP();
        }
    }

    /**
     * Generate code to test if property is present. The generated code
     * assumes that the top of the stack is the reference for the containing
     * object, and consumes this value for the test. The target for the
     * returned branch instruction must be set by the caller.
     *
     * @param mb method builder
     * @return wrapper for branch instruction taken when property is missing
     * @throws JiBXException 
     */
    public BranchWrapper genTest(ContextMethodBuilder mb) throws JiBXException {
        
        // first check for supplied test method
        if (m_testMethod != null) {
            
            // generate call to test method to check for property present
            mb.addMethodExceptions(m_testMethod);
            if (m_testMethod.isStatic()) {
                discardValue(mb);
            }
            if (m_testMethod.getArgumentCount() > 0) {
                mb.loadContext();
            }
            mb.appendCall(m_testMethod);
            return mb.appendIFEQ(this);
            
        } else if (!ClassItem.isPrimitive(m_typeName) && !m_isImplicit) {
            if (!m_isThis) {
                
                // generated instruction either loads a field value or calls a
                //  "get" method, as appropriate
                if (m_getMethod == null) {
                	genLoad(mb);
                } else {
                    if (m_getMethod.isStatic()) {
                        discardValue(mb);
                    }
                    if (m_getMethod.getArgumentCount() > 0) {
                        mb.loadContext();
                    }
                    mb.addMethodExceptions(m_getMethod);
                    mb.appendCall(m_getMethod);
                }
                
            }
            return mb.appendIFNULL(this);
            
        } else {
            return null;
        }
    }

    /**
     * Generate code to call flag method with value on stack. The generated code
     * assumes that the reference to the containing object and the value to be
     * stored have already been pushed on the stack. It consumes these, leaving
     * nothing. If the property value is not directly accessible from the
     * context of the method being generated this automatically constructs an
     * access method and uses that method.
     *
     * @param mb method builder
     * @throws JiBXException if configuration error
     */
    public void genFlag(MethodBuilder mb) throws JiBXException {
        
        // first check direct access to property from method class
        ClassFile from = mb.getClassFile();
        ClassItem access = m_flagMethod;
        if (!from.isAccessible(access)) {
            access = m_objContext.getBoundClass().
                getStoreMethod(access, mb.getClassFile());
        }
        
        // generated instruction calls the "flag" method
        if (access.isMethod()) {
            if (access.getArgumentCount() > 1) {
                ((ContextMethodBuilder)mb).loadContext();
            }
            mb.addMethodExceptions(access);
            mb.appendCall(access);
        }
        if (access.isStatic()) {
            discardValue(mb);
        }
    }

    /**
     * Generate code to load property value to stack. The generated code
     * assumes that the top of the stack is the reference for the containing
     * object. It consumes this and leaves the actual value on the stack. If
     * the property value is not directly accessible from the context of the
     * method being generated this automatically constructs an access method
     * and uses that method.
     *
     * @param mb method builder
     * @throws JiBXException if configuration error
     */
    public void genLoad(ContextMethodBuilder mb) throws JiBXException {
        
        // nothing to be done if called on "this" or implicit reference
        if (!m_isThis && !m_isImplicit) {
        
            // first check direct access to property from method class
            ClassFile from = mb.getClassFile();
            ClassItem access = m_getMethod;
            if (access == null) {
                access = m_fieldItem;
            }
            if (access != null && !from.isAccessible(access)) {
                access = m_objContext.getBoundClass().
                    getLoadMethod(access, mb.getClassFile());
            }
        
            // generated instruction either loads a field value or calls a "get"
            //  method, as appropriate
            if (access == null) {
                Integer index = (Integer)mb.getKeyValue(this);
                discardValue(mb);
                if (index == null) {
                    mb.appendACONST_NULL();
                } else {
                    mb.appendLoadLocal(index.intValue());
                }
            } else {
                if (access.isStatic()) {
                    discardValue(mb);
                }
                if (access.isMethod()) {
                    if (access.getArgumentCount() > 0) {
                        mb.loadContext();
                    }
                    mb.addMethodExceptions(access);
                    mb.appendCall(access);
                } else {
                    mb.appendGet(access);
                }
            }
            
            // cast value if necessary to assure correct type
            mb.appendCreateCast(m_getValueType, m_typeName);
            
        }
    }

    /**
     * Generate code to store property value from stack. The generated code
     * assumes that the reference to the containing object and the value to be
     * stored have already been pushed on the stack. It consumes these, leaving
     * nothing. If the property value is not directly accessible from the
     * context of the method being generated this automatically constructs an
     * access method and uses that method.
     *
     * @param mb method builder
     * @throws JiBXException if configuration error
     */
    public void genStore(MethodBuilder mb) throws JiBXException {
        
        // ignore call if no way of setting value
        if (!m_isThis && !m_isImplicit && m_fieldItem == null &&
            m_setMethod == null) {
            mb.appendPOP();
            mb.appendPOP();
        } else {
            
            // check for cast needed to convert to actual type
            if (!ClassItem.isPrimitive(m_setValueType)) {
                mb.appendCreateCast(m_setValueType);
            }
            
            // nothing to be done if called on "this" or implicit reference
            if (!m_isThis && !m_isImplicit) {
            
                // first check direct access to property from method class
                ClassFile from = mb.getClassFile();
                ClassItem access = m_setMethod;
                if (access == null) {
                    access = m_fieldItem;
                }
                if (!from.isAccessible(access)) {
                    access = m_objContext.getBoundClass().
                        getStoreMethod(access, mb.getClassFile());
                }
                
                // save to local if no way of getting value
                if (m_getMethod == null && m_fieldItem == null) {
                    duplicateValue(mb);
                    Integer index = (Integer)mb.getKeyValue(this);
                    if (index == null) {
                        int slot = mb.addLocal(null,
                            ClassItem.typeFromName(m_setValueType));
                        index = IntegerCache.getInteger(slot);
                        mb.setKeyValue(this, index);
                    } else {
                        mb.appendStoreLocal(index.intValue());
                    }
                }
                
                // generated instruction either stores a field value or calls a
                //  "set" method, as appropriate
                if (access.isMethod()) {
                    if (access.getArgumentCount() > 1) {
                        
                        // this test is ugly, needed because of backfill method
                        //  calls from ValueChild
                        if (mb instanceof ContextMethodBuilder) {
                            ((ContextMethodBuilder)mb).loadContext();
                        } else {
                            mb.appendACONST_NULL();
                        }
                    }
                    mb.addMethodExceptions(access);
                    mb.appendCall(access);
                } else {
                    mb.appendPut(access);
                }
                if (access.isStatic()) {
                    discardValue(mb);
                }
            }
        }
    }
    
    // DEBUG
    public String toString() {
        StringBuffer text = new StringBuffer();
        if (m_isOptional) {
            text.append("optional ");
        }
        text.append("property ");
        if (m_isThis) {
            text.append("\"this\" ");
        } else if (m_isImplicit) {
            text.append("from collection ");
        } else if (m_fieldItem != null) {
            text.append(m_fieldItem.getName() + " ");
        } else {
            if (m_getMethod != null) {
                text.append("from " + m_getMethod.getName() + " ");
            }
            if (m_setMethod != null) {
                text.append("to " + m_setMethod.getName() + " ");
            }
        }
        if (m_flagMethod != null) {
            text.append("flag " + m_flagMethod.getName() + " ");
        }
        if (m_testMethod != null) {
            text.append("test " + m_testMethod.getName() + " ");
        }
        if (m_typeName != null) {
            text.append( "("+ m_typeName + ")");
        }
        return text.toString();
    }
}
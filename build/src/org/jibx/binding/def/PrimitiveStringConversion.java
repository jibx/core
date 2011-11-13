/*
Copyright (c) 2003-2009, Dennis M. Sosnoski
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Primitive string conversion handling. Class for handling serialization
 * converting a primitive type to and from <code>String</code> values.
 *
 * @author Dennis M. Sosnoski
 */
public class PrimitiveStringConversion extends StringConversion
{
    //
    // Static class references
    
    private static ClassFile s_unmarshalClass;
    {
        try {
            s_unmarshalClass = ClassCache.requireClassFile
                ("org.jibx.runtime.impl.UnmarshallingContext");
        } catch (JiBXException ex) { /* no handling required */ }
    }
    
    //
    // enum for comparison types of primitive values
    private static final int INT_TYPE = 0;
    private static final int LONG_TYPE = 1;
    private static final int FLOAT_TYPE = 2;
    private static final int DOUBLE_TYPE = 3;
    
    //
    // Constants for code generation.
    
    /** Class providing basic conversion methods. */
    private static final String UTILITY_CLASS_NAME =
        "org.jibx.runtime.Utility";
    
    /** Unmarshal method signature leading portion. */
    private static final String UNMARSHAL_SIG_LEAD =
        "(Ljava/lang/String;Ljava/lang/String;";
    
    /** Constant argument type array for finding conversion methods. */
    private static final Class[] SINGLE_STRING_ARGS =
        new Class[] { String.class };

    //
    // Actual instance data
    
    /** Marshalling requires conversion to text flag. */
    private boolean m_isMarshalText;
    
    /** Unmarshalling requires conversion to text flag. */
    private boolean m_isUnmarshalText;
        
    /** Unmarshalling context method for optional attribute. */
    private ClassItem m_unmarshalOptAttribute;
    
    /** Unmarshalling context method for optional element. */
    private ClassItem m_unmarshalOptElement;
    
    /** Unmarshalling context method for required attribute. */
    private ClassItem m_unmarshalReqAttribute;
    
    /** Unmarshalling context method for required element. */
    private ClassItem m_unmarshalReqElement;
    
    /** Comparison and marshal type of value (INT_TYPE, LONG_TYPE, FLOAT_TYPE,
     or DOUBLE_TYPE) */
    private int m_valueType;
    
    /** Name of value type on stack. */
    private String m_stackType;
    
    /**
     * Constructor. Initializes conversion handling based on the supplied
     * inherited handling.
     *
     * @param type name of primitive type handled by conversion
     * @param inherit conversion information inherited by this conversion
     */
    protected PrimitiveStringConversion(String type,
        PrimitiveStringConversion inherit) {
        super(type, inherit);
        m_isMarshalText = inherit.m_isMarshalText;
        m_isUnmarshalText = inherit.m_isUnmarshalText;
        m_unmarshalOptAttribute = inherit.m_unmarshalOptAttribute;
        m_unmarshalOptElement = inherit.m_unmarshalOptElement;
        m_unmarshalReqAttribute = inherit.m_unmarshalReqAttribute;
        m_unmarshalReqElement = inherit.m_unmarshalReqElement;
        m_valueType = inherit.m_valueType;
        m_stackType = inherit.m_stackType;
    }

    /**
     * Constructor. Initializes conversion handling based on argument values.
     * This form is only used for constructing the default set of conversions.
     *
     * @param cls class of primitive type handled by conversion
     * @param dflt default value object (wrapped value, or <code>String</code>
     * or <code>null</code> with special deserializer)
     * @param code primitive type code
     * @param ts name of utility class static method for converting value to 
     * <code>String</code>
     * @param fs name of utility class static method for converting
     * <code>String</code> to value
     * @param uattr unmarshalling context method name for attribute value
     * @param uelem unmarshalling context method name for element value
     */
    public PrimitiveStringConversion(Class cls, Object dflt, String code,
        String ts, String fs, String uattr, String uelem) {
        super(dflt, UTILITY_CLASS_NAME+'.'+ts, null, UTILITY_CLASS_NAME+'.'+fs,
            cls.getName());
        m_isMarshalText = m_isUnmarshalText = false;
        String sig = UNMARSHAL_SIG_LEAD + code + ')' + code;
        m_unmarshalOptAttribute = s_unmarshalClass.getMethod(uattr, sig);
        m_unmarshalOptElement = s_unmarshalClass.getMethod(uelem, sig);
        sig = UNMARSHAL_SIG_LEAD + ')' + code;
        m_unmarshalReqAttribute = s_unmarshalClass.getMethod(uattr, sig);
        m_unmarshalReqElement = s_unmarshalClass.getMethod(uelem, sig);
        if (cls == Long.TYPE) {
            m_valueType = LONG_TYPE;
            m_stackType = "long";
        } else if (cls == Float.TYPE) {
            m_valueType = FLOAT_TYPE;
            m_stackType = "float";
        } else if (cls == Double.TYPE) {
            m_valueType = DOUBLE_TYPE;
            m_stackType = "double";
        } else {
            m_valueType = INT_TYPE;
            m_stackType = "int";
        }
    }

    /**
     * Generate code to convert <code>String</code> representation. The
     * code generated by this method assumes that the <code>String</code>
     * value has already been pushed on the stack. It consumes this and
     * leaves the converted value on the stack.
     *
     * @param mb method builder
     */
    public void genFromText(ContextMethodBuilder mb) {
        
        // handle whitespace conversion, if required
        if (m_converter != null) {
            mb.appendCall(m_converter);
        }
        
        // check if a deserializer is used for this type
        if (m_deserializer != null) {
            
            // just generate call to the deserializer (adding any checked
            //  exceptions thrown by the deserializer to the list needing
            //  handling)
            mb.addMethodExceptions(m_deserializer);
            if (m_deserializer.getArgumentCount() > 1) {
                mb.loadContext();
            }
            mb.appendCall(m_deserializer);
        }
    }
    
    /**
     * Push default value on stack. Just adds the appropriate instruction to
     * the list for the method.
     *
     * @param mb method builder
     */
    protected void pushDefault(ContextMethodBuilder mb) {
        mb.appendLoadConstant(m_default);
    }

    /**
     * Generate code to parse and convert optional attribute or element. The
     * code generated by this method assumes that the unmarshalling context
     * and name information for the attribute or element have already
     * been pushed on the stack. It consumes these and leaves the converted
     * value (or default value, if the item itself is missing) on the stack.
     *
     * @param attr item is an attribute (vs element) flag
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genParseOptional(boolean attr, ContextMethodBuilder mb)
        throws JiBXException {
        
        // choose between custom deserializer or standard built-in method
        if (m_isUnmarshalText) {
            
            // first part of generated instruction sequence is to push the
            //  default value text, then call the appropriate unmarshalling
            //  context method to get the value as a String
            String dflt;
            if (m_default instanceof String || m_default == null) {
                dflt = (String)m_default;
            } else {
                dflt = m_default.toString();
            }
            mb.appendLoadConstant(dflt);
            String name = attr ? UNMARSHAL_OPT_ATTRIBUTE : 
                UNMARSHAL_OPT_ELEMENT;
            mb.appendCallVirtual(name, UNMARSHAL_OPT_SIGNATURE);
            
            // second part is to generate call to deserializer
            genFromText(mb);
            
        } else {
            
            // generated instruction sequence just pushes the unwrapped default
            //  value, then calls the appropriate unmarshalling context method
            //  to get the value as a primitive
            pushDefault(mb);
            mb.appendCall(attr ?
                m_unmarshalOptAttribute : m_unmarshalOptElement);
            
        }
    }

    /**
     * Generate code to parse and convert required attribute or element. The
     * code generated by this method assumes that the unmarshalling context and
     * name information for the attribute or element have already been pushed
     * on the stack. It consumes these and leaves the converted value on the
     * stack.
     *
     * @param attr item is an attribute (vs element) flag
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genParseRequired(boolean attr, ContextMethodBuilder mb)
        throws JiBXException {
        
        // choose between custom deserializer or standard built-in method
        if (m_isUnmarshalText) {
            
            // first part of generated instruction sequence is a call to
            //  the appropriate unmarshalling context method to get the value
            //  as a String
            String name = attr ? UNMARSHAL_REQ_ATTRIBUTE : 
                UNMARSHAL_REQ_ELEMENT;
            mb.appendCallVirtual(name, UNMARSHAL_REQ_SIGNATURE);
            
            // second part is to generate call to deserializer
            genFromText(mb);
            
        } else {
            
            // generated instruction sequence just calls the appropriate
            //  unmarshalling context method to get the value as a primitive
            mb.appendCall(attr ?
                m_unmarshalReqAttribute : m_unmarshalReqElement);
            
        }
    }

    /**
     * Generate code to check if an optional value is not equal to the default.
     * The code generated by this method assumes that the actual value to be
     * converted has already been pushed on the stack. It consumes this,
     * leaving the converted text reference on the stack if it's not equal to
     * the default value.
     *
     * @param type fully qualified class name for value on stack
     * @param mb method builder
     * @param extra count of extra values to be popped from stack if missing
     * @return handle for branch taken when value is equal to the default
     * (target must be set by caller)
     * @throws JiBXException if error in configuration
     */
    protected BranchWrapper genToOptionalText(String type,
        ContextMethodBuilder mb, int extra) throws JiBXException {
    
        // set instructions based on value size
        if (m_valueType == LONG_TYPE || m_valueType == DOUBLE_TYPE) {
            mb.appendDUP2();
        } else {
            mb.appendDUP();
        }
        extra++;
    
        // first add code to check if the value is different from the default,
        //  by duplicating the value, pushing the default, and executing the
        //  appropriate branch comparison
        
        // TODO: this should not be done inline, but necessary for now
        Object value = m_default;
        if (m_isUnmarshalText) {
            try {
                String mname = m_deserializer.getName();
                String cname = m_deserializer.getClassFile().getName();
                Class clas = ClassFile.loadClass(cname);
                if (clas == null) {
                    throw new JiBXException("Deserializer class " + cname +
                        " not found for converting default value");
                } else {
                    
                    // try first to find a declared method, then a public one
                    Method meth;
                    try {
                        meth = clas.getDeclaredMethod(mname,
                            SINGLE_STRING_ARGS);
                        meth.setAccessible(true);
                    } catch (NoSuchMethodException ex) {
                        meth = clas.getMethod(mname, SINGLE_STRING_ARGS);
                    }
                    String text;
                    if (value instanceof String || value == null) {
                        text = (String)value;
                    } else {
                        text = value.toString();
                    }
                    value = meth.invoke(null, new Object[] { text });
                    
                }
            } catch (IllegalAccessException ex) {
                throw new JiBXException("Conversion method not accessible", ex);
            } catch (InvocationTargetException ex) {
                throw new JiBXException("Internal error", ex);
            } catch (NoSuchMethodException ex) {
                throw new JiBXException("Internal error", ex);
            }
        }
        mb.appendLoadConstant(value);
        
        BranchWrapper ifne = null;
        switch (m_valueType) {
        
            case LONG_TYPE:
                mb.appendLCMP();
                break;
        
            case FLOAT_TYPE:
                mb.appendFCMPG();
                break;
        
            case DOUBLE_TYPE:
                mb.appendDCMPG();
                break;
        
            default:
                ifne = mb.appendIF_ICMPNE(this);
                break;
            
        }
        if (ifne == null) {
            ifne = mb.appendIFNE(this);
        }
        
        // generate code for branch not taken case, popping the value from
        //  stack along with extra parameters, and branching past using code
        genPopValues(extra, mb);
        BranchWrapper toend = mb.appendUnconditionalBranch(this);
        mb.targetNext(ifne);
        genToText(m_stackType, mb);
        return toend;
    }
    
    /**
     * Convert text representation into default value object. This override of
     * the base class method uses reflection to call the actual deserialization
     * method, returning the wrapped result value. If a custom deserializer is
     * defined this just returns the <code>String</code> value directly.
     *
     * @param text value representation to be converted
     * @return converted default value object
     * @throws JiBXException on conversion error
     */
    protected Object convertDefault(String text) throws JiBXException {
        if (!m_isUnmarshalText) {
            try {
                String mname = m_deserializer.getName();
                String cname = m_deserializer.getClassFile().getName();
                Class clas = ClassFile.loadClass(cname);
                if (clas == null) {
                    throw new JiBXException("Deserializer class " + cname +
                        " not found for converting default value");
                } else {
                    
                    // try first to find a declared method, then a public one
                    Method meth;
                    try {
                        meth = clas.getDeclaredMethod(mname,
                            SINGLE_STRING_ARGS);
                        meth.setAccessible(true);
                    } catch (NoSuchMethodException ex) {
                        meth = clas.getMethod(mname, SINGLE_STRING_ARGS);
                    }
                    return meth.invoke(null, new Object[] { text });
                    
                }
            } catch (IllegalAccessException ex) {
                throw new JiBXException("Conversion method not accessible", ex);
            } catch (InvocationTargetException ex) {
                throw new JiBXException("Internal error", ex);
            } catch (NoSuchMethodException ex) {
                throw new JiBXException("Internal error", ex);
            }
        } else {
            return text;
        }
    }

    /**
     * Check if the type handled by this conversion is of a primitive type.
     *
     * @return <code>true</code> to indicate primitive type
     */
    public boolean isPrimitive() {
        return true;
    }

    /**
     * Set serializer for conversion. This override of the base class method
     * sets a flag to indicate that values must be converted to text before
     * they are written to a document after executing the base class processing.
     *
     * @param ser fully qualified class and method name of serializer
     * @throws JiBXException if serializer not found or not usable
     */
    protected void setSerializer(String ser) throws JiBXException {
        super.setSerializer(ser, true);
        m_isMarshalText = true;
    }

    /**
     * Set whitespace converter for conversion. This override of the base class
     * method sets a flag to indicate that values must be read from a document
     * as text and converted as a separate step after executing the base class
     * processing.
     *
     * @param wsconv fully qualified class and method name of whitespace
     * converter
     * @throws JiBXException if whitespace converter not found or not usable
     */
    protected void setWhitespaceConverter(String wsconv) throws JiBXException {
        super.setWhitespaceConverter(wsconv);
        m_isUnmarshalText = true;
    }

    /**
     * Set deserializer for conversion. This override of the base class method
     * sets a flag to indicate that values must be read from a document as text
     * and converted as a separate step after executing the base class
     * processing.
     *
     * @param deser fully qualified class and method name of deserializer
     * @throws JiBXException if deserializer not found or not usable
     */
    protected void setDeserializer(String deser) throws JiBXException {
        super.setDeserializer(deser);
        m_isUnmarshalText = true;
    }

    /**
     * Derive from existing formatting information. This allows constructing
     * a new instance from an existing format of the same or an ancestor
     * type, with the properties of the existing format copied to the new
     * instance except where overridden by the supplied values.
     *
     * @param type fully qualified name of class handled by conversion
     * (<code>null</code> if inherited)
     * @param ser fully qualified name of serialization method
     * (<code>null</code> if inherited)
     * @param conv fully qualified name of whitespace conversion method
     * (<code>null</code> if inherited)
     * @param dser fully qualified name of deserialization method
     * (<code>null</code> if inherited)
     * @param dflt default value text (<code>null</code> if inherited)
     * @return new instance initialized from existing one
     * @throws JiBXException if error in configuration information
     */
    public StringConversion derive(String type, String ser, String conv,
        String dser, String dflt) throws JiBXException {
        if (type == null) {
            type = m_typeName;
        }
        StringConversion inst = new PrimitiveStringConversion(type, this);
        if (ser != null) {
            inst.setSerializer(ser, true);
        }
        if (conv != null) {
            inst.setWhitespaceConverter(conv);
        }
        if (dser != null) {
            inst.setDeserializer(dser);
        }
        if (dflt != null) {
            inst.m_default = inst.convertDefault(dflt);
        }
        return inst;
    }
}
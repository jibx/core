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

import org.apache.bcel.classfile.Utility;

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * String conversion handling. Defines serialization handling for converting
 * to and from a <code>String</code> value. This uses an inheritance approach,
 * where each serialization definition is initialized based on the handling
 * set for the containing definition of the same (or parent class) type.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class StringConversion
{
    //
    // Constants for code generation.
    
    protected static final String UNMARSHAL_OPT_ATTRIBUTE =
        "org.jibx.runtime.impl.UnmarshallingContext.attributeText";
    protected static final String UNMARSHAL_OPT_ELEMENT =
        "org.jibx.runtime.impl.UnmarshallingContext.parseElementText";
    protected static final String UNMARSHAL_OPT_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)" +
        "Ljava/lang/String;";
    protected static final String UNMARSHAL_REQ_ATTRIBUTE =
        "org.jibx.runtime.impl.UnmarshallingContext.attributeText";
    protected static final String UNMARSHAL_REQ_ELEMENT =
        "org.jibx.runtime.impl.UnmarshallingContext.parseElementText";
    protected static final String UNMARSHAL_REQ_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
    protected static final String MARSHAL_ATTRIBUTE =
        "org.jibx.runtime.impl.MarshallingContext.attribute";
    protected static final String MARSHAL_ELEMENT =
        "org.jibx.runtime.impl.MarshallingContext.element";
    protected static final String MARSHAL_SIGNATURE =
        "(ILjava/lang/String;Ljava/lang/String;)" +
        "Lorg/jibx/runtime/impl/MarshallingContext;";
    protected static final String COMPARE_OBJECTS_METHOD =
        "org.jibx.runtime.Utility.isEqual";
    protected static final String COMPARE_OBJECTS_SIGNATURE =
        "(Ljava/lang/Object;Ljava/lang/Object;)Z";
    protected static final String[] WHITESPACE_CONVERT_SIGNATURES =
    {
        "(Ljava/lang/String;)Ljava/lang/String;"
    };
    protected static final String[] DESERIALIZER_SIGNATURES =
    {
        "(Ljava/lang/String;)",
        "(Ljava/lang/String;Lorg/jibx/runtime/IUnmarshallingContext;)"
    };
    
    // values used for name in marshalling; must be 1 or 2
    public static final int MARSHAL_NAME_VALUES = 2;

    //
    // Actual instance data

    /** Default value used for this type (wrapper for primitives, otherwise
     <code>String</code> or <code>null</code>). */
    protected Object m_default;

    /** Serializer method information. */
    protected ClassItem m_serializer;
    
    /** Whitespace conversion method information. */
    protected ClassItem m_converter;

    /** Deserializer method information. */
    protected ClassItem m_deserializer;
    
    /** Fully qualified name of class handled by conversion. */
    protected String m_typeName;
    
    /** Signature of class handled by conversion. */
    protected String m_typeSignature;
    
    /**
     * Constructor. This internal form only initializes the type information.
     *
     * @param type fully qualified name of class handled by conversion
     */
    private StringConversion(String type) {
        m_typeName = type;
        m_typeSignature = Utility.getSignature(type);
    }

    /**
     * Constructor. Initializes conversion handling based on the supplied
     * inherited handling.
     *
     * @param type fully qualified name of class handled by conversion
     * @param inherit conversion information inherited by this conversion
     */
    protected StringConversion(String type, StringConversion inherit) {
        this(type);
        m_default = inherit.m_default;
        m_serializer = inherit.m_serializer;
        m_converter = inherit.m_converter;
        m_deserializer = inherit.m_deserializer;
    }

    /**
     * Constructor. Initializes conversion handling based on argument values.
     * This form is only used for constructing the default set of conversions.
     * Because of this, it throws an unchecked exception on error.
     *
     * @param dflt default value object (wrapped value for primitive types,
     * otherwise <code>String</code>)
     * @param ser fully qualified name of serialization method
     * (<code>null</code> if none)
     * @param conv fully qualified name of whitespace conversion method
     * (<code>null</code> if none)
     * @param deser fully qualified name of deserialization method
     * (<code>null</code> if none)
     * @param type fully qualified name of class handled by conversion
     */
    /*package*/ StringConversion(Object dflt, String ser, String conv,
        String deser, String type) {
        this(type);
        m_default = dflt;
        try {
            if (ser != null) {
                setSerializer(ser, false);
            }
            if (conv != null) {
                setWhitespaceConverter(conv);
            }
            if (deser != null) {
                setDeserializer(deser);
            }
        } catch (JiBXException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    /**
     * Get name of type handled by this conversion.
     *
     * @return fully qualified class name of type handled by conversion
     */
    public String getTypeName() {
        return m_typeName;
    }

    /**
     * Generate code to convert <code>String</code> representation. The
     * code generated by this method assumes that the <code>String</code>
     * value has already been pushed on the stack. It consumes this and
     * leaves the converted value on the stack.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public abstract void genFromText(ContextMethodBuilder mb)
        throws JiBXException;

    /**
     * Generate code to parse and convert optional attribute or element. This
     * abstract base class method must be implemented by every subclass. The
     * code generated by this method assumes that the unmarshalling context
     * and name information for the attribute or element have already
     * been pushed on the stack. It consumes these and leaves the converted
     * value (or converted default value, if the item itself is missing) on
     * the stack.
     *
     * @param attr item is an attribute (vs element) flag
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public abstract void genParseOptional(boolean attr, ContextMethodBuilder mb)
        throws JiBXException;

    /**
     * Generate code to parse and convert required attribute or element. This
     * abstract base class method must be implemented by every subclass. The
     * code generated by this method assumes that the unmarshalling context and
     * name information for the attribute or element have already been pushed
     * on the stack. It consumes these and leaves the converted value on the
     * stack.
     *
     * @param attr item is an attribute (vs element) flag
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public abstract void genParseRequired(boolean attr, ContextMethodBuilder mb)
        throws JiBXException;

    /**
     * Generate code to write <code>String</code> value to generated document.
     * The code generated by this method assumes that the marshalling context,
     * the name information, and the actual value to be converted have already
     * been pushed on the stack. It consumes these, leaving the marshalling
     * context on the stack.
     *
     * @param attr item is an attribute (vs element) flag
     * @param mb method builder
     */
    public void genWriteText(boolean attr, ContextMethodBuilder mb) {
        
        // append code to call the appropriate generic marshalling context
        //  String method
        String name = attr ? MARSHAL_ATTRIBUTE : MARSHAL_ELEMENT;
        mb.appendCallVirtual(name, MARSHAL_SIGNATURE);
    }

    /**
     * Generate code to pop values from stack.
     *
     * @param count number of values to be popped
     * @param mb method builder
     */
    public void genPopValues(int count, ContextMethodBuilder mb) {
        while (--count >= 0) {
            if (mb.isStackTopLong()) {
                mb.appendPOP2();
            } else {
                mb.appendPOP();
            }
        }
    }

    /**
     * Generate code to check if an optional value is not equal to the default.
     * This abstract base class method must be implemented by every subclass.
     * The code generated by this method assumes that the actual value to be
     * converted has already been pushed on the stack. It consumes this,
     * leaving the converted text reference on the stack if it's not equal to
     * the default value.
     *
     * @param type fully qualified class name for value on stack
     * @param mb method builder
     * @param extra count of extra words to be popped from stack if missing
     * @return handle for branch taken when value is equal to the default
     * (target must be set by caller)
     * @throws JiBXException if error in configuration
     */
    protected abstract BranchWrapper genToOptionalText(String type,
        ContextMethodBuilder mb, int extra) throws JiBXException;

    /**
     * Generate code to convert value to a <code>String</code>. The code
     * generated by this method assumes that the actual value to be converted
     * has already been pushed on the stack. It consumes this, leaving the
     * converted text reference on the stack.
     *
     * @param type fully qualified class name for value on stack
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genToText(String type, ContextMethodBuilder mb)
        throws JiBXException {
        
        // check if a serializer is used for this type
        if (m_serializer != null) {
            
            // just generate call to the serializer (adding any checked
            //  exceptions thrown by the serializer to the list needing
            //  handling)
            if (!isPrimitive()) {
                mb.appendCreateCast(type, m_serializer.getArgumentType(0));
            }
            mb.addMethodExceptions(m_serializer);
            if (m_serializer.getArgumentCount() > 1) {
                mb.loadContext();
            }
            mb.appendCall(m_serializer);
            
        } else {
            
            // make sure this is a string
            mb.appendCreateCast(type, "java.lang.String");
        }
    }

    /**
     * Generate code to convert and write optional value to generated document.
     * The generated code first tests if the value is the same as the supplied
     * default, and if so skips writing. The code assumes that the marshalling
     * context, the name information, and the actual value to be converted have
     * already been pushed on the stack. It consumes these, leaving only the 
     * marshalling context on the stack.
     *
     * @param attr item is an attribute (vs element) flag
     * @param type fully qualified class name for value on stack
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genWriteOptional(boolean attr, String type,
        ContextMethodBuilder mb) throws JiBXException {
        
        // start with code to convert value to String, if it's not equal to the
        //  default value
        BranchWrapper toend = genToOptionalText(type, mb, MARSHAL_NAME_VALUES);
        
        // next use standard write code, followed by targeting branch
        genWriteText(attr, mb);
        if (toend != null) {
            mb.targetNext(toend);
        }
    }

    /**
     * Generate code to convert and write required value to generated document.
     * The code generated by this method assumes that the marshalling context,
     * the name information, and the actual value to be converted have already
     * been pushed on the stack. It consumes these, leaving the returned
     * marshalling context on the stack.
     *
     * @param attr item is an attribute (vs element) flag
     * @param type fully qualified class name for value on stack
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genWriteRequired(boolean attr, String type,
        ContextMethodBuilder mb) throws JiBXException {
        
        // generate code to convert to text, followed by code to marshal text
        genToText(type, mb);
        genWriteText(attr, mb);
    }

    /**
     * Check if the type handled by this conversion is of a primitive type.
     *
     * @return <code>true</code> if a primitive type, <code>false</code> if an
     * object type
     */
    public abstract boolean isPrimitive();

    /**
     * Set serializer for conversion. This finds the named static method and
     * sets it as the serializer to be used for this conversion. The serializer
     * method is expected to take a single argument of either the handled
     * type or a superclass or interface of the handled type, and to return a 
     * <code>String</code> result.
     *
     * @param ser fully qualified class and method name of serializer
     * @param variant allow variants of the specified type
     * @throws JiBXException if serializer not found or not usable
     */
    protected void setSerializer(String ser, boolean variant) throws JiBXException {
        
        // build all possible signature variations
        String[] tsigs;
        if (variant) {
            tsigs = ClassItem.getSignatureVariants(m_typeName);
        } else {
            tsigs = new String[] { Utility.getSignature(m_typeName) };
        }
        String[] msigs = new String[tsigs.length*2];
        for (int i = 0; i < tsigs.length; i++) {
            msigs[i*2] = "(" + tsigs[i] + ")Ljava/lang/String;";
            msigs[i*2+1] = "(" + tsigs[i] +
                "Lorg/jibx/runtime/IMarshallingContext;)Ljava/lang/String;";
        }
        
        // find a matching static method
        ClassItem method = ClassItem.findStaticMethod(ser, msigs);
        
        // report error if method not found
        if (method == null) {
            throw new JiBXException("Serializer " + ser + " not found");
        } else {
            m_serializer = method;
        }
    }

    /**
     * Set whitespace converter for conversion. This finds the named static
     * method and sets it as the whitespace converter to be used for this
     * conversion. The whitespace converter method is expected to take a
     * single argument of type <code>String</code>, and to return the same.
     *
     * @param wsconv fully qualified class and method name of whitespace
     * converter
     * @throws JiBXException if whitespace converter not found or not usable
     */
    protected void setWhitespaceConverter(String wsconv) throws JiBXException {
        
        // find a matching static method
        m_converter =
            ClassItem.findStaticMethod(wsconv, WHITESPACE_CONVERT_SIGNATURES);
        
        // report error if method not found or incompatible
        if (m_converter == null) {
            throw new JiBXException("Whitespace converter " + wsconv + " not found");
        }
    }

    /**
     * Set deserializer for conversion. This finds the named static method and
     * sets it as the deserializer to be used for this conversion. The
     * deserializer method is expected to take a single argument of type
     * <code>String</code>, and to return a value of the handled type or a
     * subtype of that type.
     *
     * @param deser fully qualified class and method name of deserializer
     * @throws JiBXException if deserializer not found or not usable
     */
    protected void setDeserializer(String deser) throws JiBXException {
        
        // find a matching static method
        ClassItem method =
            ClassItem.findStaticMethod(deser, DESERIALIZER_SIGNATURES);
        
        // report error if method not found or incompatible
        if (method == null) {
            throw new JiBXException("Deserializer " + deser + " not found");
        } else if (ClassItem.isAssignable(method.getTypeName(), m_typeName)) {
            m_deserializer = method;
        } else {
            throw new JiBXException("Deserializer " + deser +
                " returns wrong type");
        }
    }
    
    /**
     * Convert text representation into default value object. Each subclass
     * must implement this with the appropriate conversion handling.
     *
     * @param text value representation to be converted
     * @return converted default value object
     * @throws JiBXException on conversion error
     */
    protected abstract Object convertDefault(String text) throws JiBXException;

    /**
     * Derive from existing formatting information. This abstract base class
     * method must be implemented by every subclass. It allows constructing
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
    public abstract StringConversion derive(String type, String ser,
        String conv, String dser, String dflt) throws JiBXException;
}
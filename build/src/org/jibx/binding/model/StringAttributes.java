/*
Copyright (c) 2004-2009, Dennis M. Sosnoski.
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

package org.jibx.binding.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.StringArray;

/**
 * Model component for <i>string</i> attribute group in binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class StringAttributes extends AttributeBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "default", "deserializer",
        "enum-value-method", "serializer", "whitespace" });
    // TODO: add "format" for 2.0
    
    //
    // Constants and such related to code generation.
    
    // signature variants allowed for serializer
    private static final String[] SERIALIZER_SIGNATURE_VARIANTS =
    {
        "Lorg/jibx/runtime/IMarshallingContext;",
        "Ljava/lang/Object;",
        ""
    };
    
    // signatures allowed for deserializer
    private static final String[] DESERIALIZER_SIGNATURES =
    {
        "(Ljava/lang/String;Lorg/jibx/runtime/IUnmarshallingContext;)",
        "(Ljava/lang/String;Ljava/lang/Object;)",
        "(Ljava/lang/String;)"
    };
    
    // signatures allowed for enum-value-method
    private static final String ENUM_VALUE_METHOD_SIGNATURE =
        "()Ljava/lang/String;";
    
    // signature required for constructor from string
    private static final String STRING_CONSTRUCTOR_SIGNATURE =
        "(Ljava/lang/String;)";
    
    // classes of arguments to constructor or deserializer
    private static final Class[] STRING_CONSTRUCTOR_ARGUMENT_CLASSES =
    {
        java.lang.String.class
    };
    
    //
    // Enumeration for whitespace processing.
    
    public static final int PRESERVE_WHITESPACE = 0;
    public static final int REPLACE_WHITESPACE = 1;
    public static final int COLLAPSE_WHITESPACE = 2;
    public static final int TRIM_WHITESPACE = 3;
    
    public static final EnumSet s_whitespaceEnum =
        new EnumSet(PRESERVE_WHITESPACE,
        new String[] { "preserve", "replace", "collapse", "trim" });
    
    //
    // Instance data.
    
    /** Referenced format name. */
    private String m_formatName;
    
    /** Format qualified name. */
    private QName m_formatQName;
    
    /** Default value text. */
    private String m_defaultText;
    
    /** Serializer fully qualified class and method name. */
    private String m_serializerName;
    
    /** Whitespace handling name. */
    private String m_whitespaceName;
    
    /** Actual selected whitespace handling. */
    private int m_whitespaceIndex;
    
    /** Deserializer fully qualified class and method name. */
    private String m_deserializerName;
    
    /** Enum value method name. */
    private String m_enumValueName;
    
    /** Base format for conversions. */
    private FormatElement m_baseFormat;
    
    /** Value type class. */
    private IClass m_typeClass;
    
    /** Default value object. */
    private Object m_default;
	
	/** Serializer method (or toString equivalent) information. */
	private IClassItem m_serializerItem;
    
    /** Deserializer method (or constructor from string) information. */
    private IClassItem m_deserializerItem;
    
    /** Method used to get text representation of an enum. */
    private IClassItem m_enumValueItem;
	
	/**
	 * Default constructor.
	 */
	public StringAttributes() {}
    
    /**
     * Set value type. This needs to be set by the owning element prior to
     * validation. Even though the type is an important part of the string
     * information, it's treated as a separate item of information because it
     * needs to be used as part of the property attributes.
     * 
     * @param type value type
     */
    public void setType(IClass type) {
        m_typeClass = type;
    }
    
    /**
     * Get value type.
     * 
     * @return value type
     */
    public IClass getType() {
        return m_typeClass;
    }
    
    /**
     * Get base format name.
     * 
     * @return referenced base format
     */
    public String getFormatName() {
        return m_formatName;
    }
    
    /**
     * Set base format name.
     * 
     * @param name referenced base format
     */
    public void setFormatName(String name) {
        m_formatName = name;
        m_formatQName = (name == null) ? null : new QName(name);
    }
    
    /**
     * Get format qualified name.
     * 
     * @return format qualified name (<code>null</code> if none)
     */
    public QName getFormatQName() {
        return m_formatQName;
    }
    
    /**
     * Set format qualified name. This method changes the label value to match
     * the qualified name.
     * 
     * @param qname format qualified name (<code>null</code> if none)
     */
    public void setFormatQName(QName qname) {
        m_formatQName = qname;
        m_formatName = (qname == null) ? null : qname.toString();
    }
    
    /**
     * Get default value text.
     * 
     * @return default value text
     */
    public String getDefaultText() {
        return m_defaultText;
    }
    
    /**
     * Get default value. This method is only usable after a
     * call to {@link #validate(ValidationContext)}.
     * 
     * @return default value object
     */
    public Object getDefault() {
        return m_default;
    }
    
    /**
     * Set default value text.
     * 
     * @param value default value text
     */
    public void setDefaultText(String value) {
        m_defaultText = value;
    }
    
    /**
     * Get serializer name.
     * 
     * @return fully qualified class and method name for serializer (or
     * <code>null</code> if none)
     */
    public String getSerializerName() {
        return m_serializerName;
    }
    
    /**
     * Get serializer method information. This method is only usable after a
     * call to {@link #validate(ValidationContext)}.
     * 
     * @return serializer information (or <code>null</code> if none)
     */
    public IClassItem getSerializer() {
        return m_serializerItem;
    }
    
    /**
     * Set serializer method name.
     * 
     * @param name fully qualified class and method name for serializer
     */
    public void setSerializerName(String name) {
        m_serializerName = name;
    }
    
    /**
     * Set whitespace handling type name.
     * 
     * @param name whitespace handling type
     */
    public void setDefaultName(String name) {
        m_whitespaceName = name;
    }
    
    /**
     * Get whitespace handling type name.
     * 
     * @return whitespace handling type
     */
    public String getDefaultName() {
        return m_whitespaceName;
    }
    
    /**
     * Get deserializer name.
     * 
     * @return fully qualified class and method name for deserializer (or
     * <code>null</code> if none)
     */
    public String getDeserializerName() {
        return m_deserializerName;
    }
    
    /**
     * Get deserializer method information. This method is only usable after a
     * call to {@link #validate(ValidationContext)}.
     * 
     * @return deserializer information (or <code>null</code> if none)
     */
    public IClassItem getDeserializer() {
        return m_deserializerItem;
    }
    
    /**
     * Set deserializer method name.
     * 
     * @param name fully qualified class and method name for deserializer
     */
    public void setDeserializerName(String name) {
        m_deserializerName = name;
    }
    
    /**
     * Get enum value method name.
     * 
     * @return enum value method name (or <code>null</code> if none)
     */
    public String getEnumValueName() {
        return m_enumValueName;
    }
    
    /**
     * Get enum value method information. This method is only usable after a
     * call to {@link #validate(ValidationContext)}.
     * 
     * @return enum value method information (or <code>null</code> if none)
     */
    public IClassItem getEnumValue() {
        return m_enumValueItem;
    }
    
    /**
     * Set enum value method name.
     * 
     * @param name enum value method name (<code>null</code> if none)
     */
    public void setEnumValueName(String name) {
        m_enumValueName = name;
    }
    
    /**
     * Get base format information. This method is only usable after a
     * call to {@link #validate(ValidationContext)}.
     * 
     * @return base format element (or <code>null</code> if none)
     */
    public FormatElement getBaseFormat() {
        return m_baseFormat;
    }
    
    /**
     * JiBX access method to set format label as qualified name.
     * 
     * @param label format label text (<code>null</code> if none)
     * @param ictx unmarshalling context
     * @throws JiBXException on deserialization error
     */
    private void setQualifiedFormat(String label, IUnmarshallingContext ictx)
        throws JiBXException {
        setFormatQName(QName.deserialize(label, ictx));
    }
    
    /**
     * JiBX access method to get format label as qualified name.
     * 
     * @param ictx marshalling context
     * @return format label text (<code>null</code> if none)
     * @throws JiBXException on deserialization error
     */
    private String getQualifiedFormat(IMarshallingContext ictx)
        throws JiBXException {
        return QName.serialize(getFormatQName(), ictx);
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.AttributeBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // check the specified whitespace handling
        if (m_whitespaceName != null) {
            m_whitespaceIndex = s_whitespaceEnum.getValue(m_whitespaceName);
            if (m_whitespaceIndex < 0) {
                vctx.addError("Value \"" + m_whitespaceName +
                    "\" is not a valid choice for whitespace handling");
            }
        }
        
        // make sure the type has been configured
        if (m_typeClass == null) {
            vctx.addFatal("Missing type information for conversion to string");
        } else if (vctx.isLookupSupported()) {
            
            // get the base format (if any)
            DefinitionContext dctx = vctx.getDefinitions();
            if (m_formatName == null) {
                m_baseFormat = dctx.getBestFormat(m_typeClass);
            } else {
                m_baseFormat = dctx.getNamedFormat(m_formatName);
                if (m_baseFormat == null) {
                    String name = m_formatName;
                    if (name.startsWith("{}")) {
                        name = name.substring(2);
                    }
                    vctx.addError("Unknown format " + name);
                }
            }
            
            // check for a Java 5 enumeration
            boolean isenum = false;
            IClass sclas = m_typeClass;
            while ((sclas = sclas.getSuperClass()) != null) {
                if (sclas.getName().equals("java.lang.Enum")) {
                    isenum = true;
                    break;
                }
            }
            
            // find the enum value method, if specified
            if (m_enumValueName != null) {
                if (isenum) {
                    if (m_serializerName == null &&
                        m_deserializerName == null) {
                        m_enumValueItem = m_typeClass.getMethod(m_enumValueName,
                            ENUM_VALUE_METHOD_SIGNATURE);
                        if (m_enumValueItem == null) {
                            vctx.addError("Nonstatic 'enum-value-method' " +
                                m_enumValueName + " not found in class " +
                                m_typeClass.getName());
                        }
                    } else {
                        vctx.addError("'enum-value-method' cannot be used with 'serializer' or 'deserializer'");
                    }
                } else {
                    vctx.addError("'enum-value-method' may only be used with Java 5 enum classes");
                }
            }
            
            // check specified serializer and deserializer
            String tname = m_typeClass.getName();
            if (vctx.isOutBinding()) {
                if (m_serializerName == null) {
                    if (m_enumValueItem == null) {
                        
                        // try to find an inherited serializer
                        FormatElement ances = m_baseFormat;
                        while (ances != null) {
                            m_serializerItem = ances.getSerializer();
                            if (m_serializerItem == null) {
                                ances = ances.getBaseFormat();
                            } else {
                                break;
                            }
                        }
                        if (m_serializerItem == null) {
                            IClassItem item = m_typeClass.getMethod("toString",
                                "()Ljava/lang/String;");
                            if (item == null) {
                                vctx.addError("toString method not found");
                            }
                        }
                        
                    }
                } else {
                
                    // build all possible signature variations
                    String[] tsigs = ClassUtils.
                        getSignatureVariants(tname, vctx);
                    int vcnt = SERIALIZER_SIGNATURE_VARIANTS.length;
                    String[] msigs = new String[tsigs.length * vcnt];
                    for (int i = 0; i < tsigs.length; i++) {
                        for (int j = 0; j < vcnt; j++) {
                            msigs[i*vcnt + j] = "(" + tsigs[i] +
                                SERIALIZER_SIGNATURE_VARIANTS[j] +
                                ")Ljava/lang/String;";
                        }
                    }
                
                    // find a matching static method
                    m_serializerItem = ClassUtils.
                        findStaticMethod(m_serializerName, msigs, vctx);
                    if (m_serializerItem == null) {
                        if (m_serializerName.indexOf('.') > 0) {
                            vctx.addError("Static serializer method " + m_serializerName + " not found");
                        } else {
                            vctx.addError("Need class name for static method " + m_serializerName);
                        }
                    }
                    
                }
            }
            if (vctx.isInBinding() || m_defaultText != null) {
                if (m_deserializerName == null) {
                    if (isenum) {
                        if (m_enumValueItem == null) {
                            m_deserializerItem = m_typeClass.
                            getMethod("valueOf", "(Ljava/lang/String;)");
                        }
                    } else {
                        
                        // try to find an inherited deserializer
                        FormatElement ances = m_baseFormat;
                        while (ances != null) {
                            m_deserializerItem = ances.getDeserializer();
                            if (m_deserializerItem == null) {
                                ances = ances.getBaseFormat();
                            } else {
                                break;
                            }
                        }
                        if (m_deserializerItem == null) {
                            
                            // try to find constructor from string as last resort
                            m_deserializerItem = m_typeClass.
                                getInitializerMethod(STRING_CONSTRUCTOR_SIGNATURE);
                            if (m_deserializerItem == null) {
                                
                                // error unless predefined formats
                                if (vctx.getNestingDepth() > 0) {
                                    StringBuffer buff = new StringBuffer();
                                    buff.append("Need deserializer or constructor from string");
                                    if (!vctx.isInBinding()) {
                                        buff.append(" for default value of type ");
                                        buff.append(tname);
                                    } else {
                                        buff.append(" for type ");
                                        buff.append(tname);
                                    }
                                    vctx.addError(buff.toString());
                                }
                                
                            }
                        }
                    }
                } else {
                
                    // find a matching static method
                    m_deserializerItem = ClassUtils.
                        findStaticMethod(m_deserializerName,
                            DESERIALIZER_SIGNATURES, vctx);
                    if (m_deserializerItem == null) {
                        if (m_deserializerName.indexOf('.') > 0) {
                            vctx.addError("Static deserializer method " + m_deserializerName + " not found");
                        } else {
                            vctx.addError("Need class name for static method " + m_deserializerName);
                        }
                    } else {
                        String result = m_deserializerItem.getTypeName();
                        if (!ClassUtils.isAssignable(result, tname, vctx)) {
                            vctx.addError("Static deserializer method " +
                                m_deserializerName +
                                " has incompatible result type");
                        }
                    }
                    
                }
            }
            
            // check for default value to be converted
            if (m_defaultText != null && m_deserializerItem != null) {
                
                // first load the class to handle conversion
                IClass iclas = m_deserializerItem.getOwningClass();
                Class clas = iclas.loadClass();
                Exception ex = null;
                boolean construct = false;
                try {
                    if (clas == null) {
                        vctx.addError("Unable to load class " +
                            iclas.getName() +
                            " for converting default value of type " + tname);
                    } else if (m_deserializerItem.isInitializer()) {
                        
                        // invoke constructor to process default value
                        construct = true;
                        Constructor cons = clas.getConstructor
                            (STRING_CONSTRUCTOR_ARGUMENT_CLASSES);
                        try {
                            cons.setAccessible(true);
                        } catch (Exception e) { /* deliberately left empty */ }
                        Object[] args = new Object[1];
                        args[0] = m_defaultText;
                        m_default = cons.newInstance(args);
                        
                    } else {
                        
                        // invoke deserializer to convert default value
                        String mname = m_deserializerItem.getName();
                        Method deser = clas.getDeclaredMethod(mname,
                            STRING_CONSTRUCTOR_ARGUMENT_CLASSES);
                        try {
                            deser.setAccessible(true);
                        } catch (Exception e) { /* deliberately left empty */ }
                        Object[] args = new Object[1];
                        args[0] = m_defaultText;
                        m_default = deser.invoke(null, args);
                        
                    }
                } catch (SecurityException e) {
                    StringBuffer buff = new StringBuffer("Unable to access ");
                    if (construct) {
                        buff.append("constructor from string");
                    } else {
                        buff.append("deserializer ");
                        buff.append(m_deserializerName);
                    }
                    buff.append(" for converting default value of type ");
                    buff.append(tname);
                    vctx.addError(buff.toString());
                } catch (NoSuchMethodException e) {
                    StringBuffer buff = new StringBuffer("Unable to find ");
                    if (construct) {
                        buff.append("constructor from string");
                    } else {
                        buff.append("deserializer ");
                        buff.append(m_deserializerName);
                    }
                    buff.append(" for converting default value of type ");
                    buff.append(tname);
                    vctx.addError(buff.toString());
                } catch (IllegalArgumentException e) {
                    ex = e;
                } catch (InstantiationException e) {
                    ex = e;
                } catch (IllegalAccessException e) {
                    ex = e;
                } catch (InvocationTargetException e) {
                    ex = e;
                } finally {
                    if (ex != null) {
                        StringBuffer buff = new StringBuffer("Error calling ");
                        if (construct) {
                            buff.append("constructor from string");
                        } else {
                            buff.append("deserializer ");
                            buff.append(m_deserializerName);
                        }
                        buff.append(" for converting default value of type ");
                        buff.append(tname);
                        vctx.addError(buff.toString());
                    }
                }
            }
        }
        super.prevalidate(vctx);
    }
}
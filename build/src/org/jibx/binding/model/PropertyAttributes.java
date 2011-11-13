/*
Copyright (c) 2004-2010, Dennis M. Sosnoski.
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

import java.util.ArrayList;

import org.jibx.runtime.EnumSet;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.StringArray;

/**
 * Model component for <i>property</i> attribute group in binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class PropertyAttributes extends AttributeBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "field", "flag-method", "get-method",
        "set-method", "test-method", "type", "usage" });
    
    // recognized test-method signatures.
    private static final String[] TEST_METHOD_SIGNATURES =
    {
        "(Lorg/jibx/runtime/IMarshallingContext;)Z",
        "()Z"
    };
    
    // recognized flag-method signatures.
    private static final String[] FLAG_METHOD_SIGNATURES =
    {
        "(ZLorg/jibx/runtime/IMarshallingContext;)",
        "(Z)"
    };
    
    // recognized get-method signatures.
    private static final String[] GET_METHOD_SIGNATURES =
    {
        "(Lorg/jibx/runtime/IMarshallingContext;)",
        "()"
    };
    
    //
	// Value set information
	
	public static final int REQUIRED_USAGE = 0;
	public static final int OPTIONAL_USAGE = 1;
    public static final int OPTIONAL_IN_USAGE = 2;
    public static final int OPTIONAL_OUT_USAGE = 3;
    private static final EnumSet s_usageEnum = new EnumSet(REQUIRED_USAGE,
        new String[] { "required", "optional", "opt-in", "opt-out" });
	
	//
	// Instance data.
	
	/** Usage type code. */
	private int m_usage;
    
    /** Usage name. */
    private String m_usageName = s_usageEnum.getName(REQUIRED_USAGE);
    
    /** Property type name. */
    private String m_declaredType;
    
    /** Property field name. */
    private String m_fieldName;
    
    /** Test method name. */
    private String m_testName;
    
    /** Flag method name. */
    private String m_flagName;
    
    /** Get method name. */
    private String m_getName;
    
    /** Set method name. */
    private String m_setName;
    
    /** Type for value loaded on stack. */
    private IClass m_getType;
    
    /** Type for value stored from stack. */
    private IClass m_setType;
    
    /** Property type information. */
    private IClass m_type;
	
	/** Property field information. */
	private IClassItem m_fieldItem;
	
	/** Test method information. */
	private IClassItem m_testItem;
    
    /** Flag method information. */
    private IClassItem m_flagItem;
	
	/** Get method information. */
	private IClassItem m_getItem;
	
	/** Set method information. */
	private IClassItem m_setItem;
    
    /** Flag for no actual property definition. */
    private boolean m_isImplicit;
    
    /**
     * Get usage name.
     * 
     * @return usage name
     */
    public String getUsageName() {
        return s_usageEnum.getName(m_usage);
    }
    
    /**
     * Get usage value. This method is only usable after a call to {@link
     * #prevalidate(ValidationContext)}.
     * 
     * @return usage value
     */
    public int getUsage() {
        return m_usage;
    }
    
    /**
     * Set usage name.
     * 
     * @param name usage name
     */
    public void setUsageName(String name) {
        m_usageName = name;
    }
    
    /**
     * Set usage value.
     * 
     * @param use value
     */
    public void setUsage(int use) {
        m_usage = use;
        m_usageName = s_usageEnum.getName(use);
    }
    
    /**
     * Check if property is defined. This method is only usable after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return <code>true</code> if property defined, <code>false</code> if not
     */
    public boolean hasProperty() {
        return !m_isImplicit && m_type != null;
    }
    
    /**
     * Get declared type name.
     * 
     * @return declared type name (or <code>null</code> if none)
     */
    public String getDeclaredType() {
        return m_declaredType;
    }
    
    /**
     * Set declared type name.
     * 
     * @param type declared type name (or <code>null</code> if none)
     */
    public void setDeclaredType(String type) {
        m_declaredType = type;
    }
    
    /**
     * Get field name.
     * 
     * @return field name (or <code>null</code> if none)
     */
    public String getFieldName() {
        return m_fieldName;
    }
    
    /**
     * Get field information. This method is only usable after a call to {@link
     * #prevalidate(ValidationContext)}.
     * 
     * @return field information (or <code>null</code> if none)
     */
    public IClassItem getField() {
        return m_fieldItem;
    }
    
    /**
     * Set field name.
     * 
     * @param field field name (or <code>null</code> if none)
     */
    public void setFieldName(String field) {
        m_fieldName = field;
    }
    
    /**
     * Get test method name.
     * 
     * @return test method name (or <code>null</code> if none)
     */
    public String getTestName() {
        return m_testName;
    }
	
	/**
	 * Get test method information. This method is only usable after a call to
     * {@link #prevalidate(ValidationContext)}.
	 * 
	 * @return test method information (or <code>null</code> if none)
	 */
	public IClassItem getTest() {
		return m_testItem;
	}
	
	/**
	 * Set test method name.
	 * 
	 * @param test test method name (or <code>null</code> if none)
	 */
	public void setTestName(String test) {
        m_testName = test;
	}
    
    /**
     * Get flag method name.
     * 
     * @return flag method name (or <code>null</code> if none)
     */
    public String getFlagName() {
        return m_flagName;
    }
    
    /**
     * Get flag method information. This method is only usable after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return flag method information (or <code>null</code> if none)
     */
    public IClassItem getFlag() {
        return m_flagItem;
    }
    
    /**
     * Set flag method name.
     * 
     * @param flag flag method name (or <code>null</code> if none)
     */
    public void setFlagName(String flag) {
        m_flagName = flag;
    }
    
    /**
     * Get get method name.
     * 
     * @return get method name (or <code>null</code> if none)
     */
    public String getGetName() {
        return m_getName;
    }
	
	/**
	 * Get get method information. This method is only usable after a call to
     * {@link #prevalidate(ValidationContext)}.
	 * 
	 * @return get method information (or <code>null</code> if none)
	 */
	public IClassItem getGet() {
		return m_getItem;
	}
    
    /**
     * Get type for value loaded to stack. This method is only usable after a
     * call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return get value type (or <code>null</code> if none)
     */
    public IClass getGetType() {
        return m_getType;
    }
	
	/**
	 * Set get method name.
	 * 
	 * @param get get method name (or <code>null</code> if none)
	 */
	public void setGetName(String get) {
        m_getName = get;
	}
    
    /**
     * Get set method name.
     * 
     * @return set method name (or <code>null</code> if none)
     */
    public String getSetName() {
        return m_setName;
    }
    
    /**
     * Get set method information. This method is only usable after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return set method information (or <code>null</code> if none)
     */
    public IClassItem getSet() {
        return m_setItem;
    }
    
    /**
     * Get type for value stored from stack. This method is only usable after a
     * call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return set value type (or <code>null</code> if none)
     */
    public IClass getSetType() {
        return m_setType;
    }
    
    /**
     * Set set method name.
     * 
     * @param set set method name (or <code>null</code> if none)
     */
    public void setSetName(String set) {
        m_setName = set;
    }
    
    /**
     * Get type information. This method is only usable after a call to {@link
     * #prevalidate(ValidationContext)}.
     * 
     * @return type information (or <code>null</code> if none)
     */
    public IClass getType() {
        return m_type;
    }
    
    /**
     * Check if empty property definition. Empty property definitions occur
     * because every <b>collection</b>, <b>structure</b>, and <b>value</b>
     * element has associated property attributes but these may not actually
     * reference a property (when using the containing object). This call is
     * only meaningful after prevalidation.
     * 
     * @return <code>true</code> if implicit property, <code>false</code> if not
     */
    public boolean isImplicit() {
        return m_isImplicit;
    }
    
    /**
     * Check if property consists only of flag. This call is only meaningful
     * after prevalidation.
     * 
     * @return <code>true</code> if flag property, <code>false</code> if not
     */
    public boolean isFlagOnly() {
        return m_flagItem != null && m_testItem != null;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.AttributeBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // check usage value
        if (m_usageName != null) {
            m_usage = s_usageEnum.getValue(m_usageName);
            if (m_usage < 0) {
                vctx.addError("Value \"" + m_usageName +
                    "\" is not a valid choice for usage");
            }
        } else {
            m_usage = vctx.getParentElement().getDefaultStyle();
        }
        
        // handle basic lookups and checks
        ContainerElementBase parent = vctx.getParentContainer();
        IClass cobj = parent.getChildObjectType();
        String dtype = null;
        String gtype = null;
        String stype = null;
        boolean err = false;
        m_isImplicit = true;
        if (m_fieldName != null) {
            
            // field means this is real (not implicit)
            m_isImplicit = false;
            if (vctx.isLookupSupported()) {
                
                // look up the field information
                m_fieldItem = cobj.getField(m_fieldName);
                if (m_fieldItem == null) {
                    vctx.addFatal("Nonstatic field " + m_fieldName +
                        " not found in class " + cobj.getName());
                    err = true;
                } else {
                    dtype = gtype = stype = m_fieldItem.getTypeName();
                }
                
            }
        }
        if (m_testName != null) {
            
            // make sure only used with optional
            if (m_usage == REQUIRED_USAGE) {
                vctx.addError("test-method can only be used with optional property");
            } else if (vctx.isLookupSupported()) {
                
                // look up the method information
                m_testItem = cobj.getMethod(m_testName, TEST_METHOD_SIGNATURES);
                if (m_testItem == null) {
                    vctx.addError("Nonstatic test-method " + m_testName +
                        " not found in class " + cobj.getName());
                }
                
            }
        }
        if (m_flagName != null) {
            
            // flag-method means this is real (not implicit)
            m_isImplicit = false;
            stype = "java.lang.Object";
            if (m_testName != null) {
                gtype = stype;
            }
            if (vctx.isLookupSupported()) {
                
                // look up the method information
                m_flagItem = cobj.getMethod(m_flagName, FLAG_METHOD_SIGNATURES);
                if (m_flagItem == null) {
                    vctx.addError("Nonstatic flag-method " + m_flagName +
                        " not found in class " + cobj.getName());
                }
                
            }
        }
        if (m_getName != null) {
            
            // get-method means this is real (not implicit)
            m_isImplicit = false;
            if (vctx.isLookupSupported()) {
                
                // look up the get method by name (no overload possible)
                m_getItem = cobj.getMethod(m_getName, GET_METHOD_SIGNATURES);
                if (m_getItem == null) {
                    vctx.addFatal("Nonstatic get-method " + m_getName +
                        " not found in class " + cobj.getName());
                    err = true;
                } else {
                    gtype = m_getItem.getTypeName();
                    if (dtype == null) {
                        dtype = gtype;
                    }
                }
                
            }
            
            // check for only get-method supplied when both directions needed
            if (vctx.isInBinding() && m_fieldName == null &&
                m_setName == null) {
                vctx.addError("Need field or set-method for input handling");
            }
        }
        if (m_setName != null) {
            
            // set-method means this is real (not implicit)
            m_isImplicit = false;
            if (vctx.isLookupSupported()) {
                
                // need to handle overloads, so generate possible signatures
                ArrayList sigs = new ArrayList();
                if (m_getItem != null) {
                    String psig = ClassUtils.getSignature(gtype);
                    sigs.add("(" + psig +
                        "Lorg/jibx/runtime/IUnmarshallingContext;" + ")V");
                    sigs.add("(" + psig + ")V");
                }
                if (m_declaredType != null) {
                    String psig = ClassUtils.getSignature(m_declaredType);
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
                
                // match any of the possible signatures
                m_setItem = cobj.getMethod(m_setName,
                    (String[])sigs.toArray(new String[0]));
                if (m_setItem == null) {
                    
                    // nothing known about signature, try anything by name
                    m_setItem = cobj.getMethod(m_setName, "");
                    if (m_setItem != null) {
                        if (!m_setItem.getTypeName().equals("void") ||
                            m_setItem.getArgumentCount() > 2) {
                            m_setItem = null;
                        } else if (m_setItem.getArgumentCount() == 2) {
                            String xtype = m_setItem.getArgumentType(1);
                            if (!"org.jibx.runtime.IUnmarshallingContext".equals(xtype)) {
                                m_setItem = null;
                            }
                        }
                    }
                    if (m_setItem != null) {
                        
                        // make sure resulting type is compatible
                        String type = m_setItem.getArgumentType(0);
                        if (m_declaredType != null &&
                            !ClassUtils.isAssignable(m_declaredType, type, vctx)) {
                            m_setItem = null;
                        } else if (dtype != null &&
                            !ClassUtils.isAssignable(type, dtype, vctx)) {
                            m_setItem = null;
                        } else if (gtype != null &&
                            !ClassUtils.isAssignable(type, gtype, vctx)) {
                            m_setItem = null;
                        }
                        if (m_setItem != null) {
                            dtype = type;
                        }
                    }
                }
                
                // check set-method found
                if (m_setItem == null) {
                    vctx.addFatal("Nonstatic set-method " + m_setName +
                        " with argument of appropriate type not found in class " +
                        cobj.getName());
                    err = true;
                } else {
                    stype = m_setItem.getArgumentType(0);
                    if (dtype == null) {
                        dtype = stype;
                    }
                }
            }
            
            // check for only set-method supplied when both directions needed
            if (vctx.isOutBinding() && m_fieldName == null &&
                m_getName == null) {
                vctx.addError("Need field or get-method for output handling");
            }
        }
        
        // set the property type information
        String tname = m_declaredType;
        if (tname == null) {
            tname = dtype;
            if (tname == null) {
                tname = cobj.getName();
            }
        } else if (dtype == null) {
            dtype = gtype = stype = tname;
        }
        m_type = vctx.getClassInfo(tname);
        if (m_type == null) {
            vctx.addFatal("Unable to load class " + tname);
        } else if (vctx.getContextObject() instanceof CollectionElement) {
            
            // forbid access specifications for child of collection
            if (m_fieldName != null || m_getName != null || m_setName != null) {
                vctx.addWarning("Property access attributes (field, " +
                    "get-method, set-method) ignored for collection item");
            }
            
        } else if (!err && !m_isImplicit && vctx.isLookupSupported()) {
            
            // check that type information is consistent
            boolean valid = true;
            
            // require access specifications for child of non-collection
            if (vctx.isInBinding()) {
                if (stype == null) {
                    vctx.addError("No way to set property value");
                    stype = "java.lang.Object";
                } else {
                    valid = ClassUtils.isAssignable(tname, stype, vctx) ||
                        ClassUtils.isAssignable(stype, tname, vctx);
                }
                m_setType = vctx.getClassInfo(stype);
            }
            if (gtype == null) {
                if (vctx.isOutBinding()) {
                    vctx.addError("No way to get property value");
                    m_getType = vctx.getClassInfo("java.lang.Object");
                }
            } else {
                if (valid) {
                    valid = ClassUtils.isAssignable(tname, gtype, vctx) ||
                        ClassUtils.isAssignable(gtype, tname, vctx);
                }
                m_getType = vctx.getClassInfo(gtype);
            }
            if (!valid) {
                vctx.addError("Incompatible types used in property definition");
            }
        }
        super.prevalidate(vctx);
    }
}
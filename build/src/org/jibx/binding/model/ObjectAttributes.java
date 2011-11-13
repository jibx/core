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

import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.StringArray;

/**
 * Model component for <i>object</i> attribute group in binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class ObjectAttributes extends AttributeBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "create-type", "factory", "marshaller",
        "nillable", "post-set", "pre-get", "pre-set", "unmarshaller" });
    
	//
	// Constants and such related to code generation.
	
	// recognized marshal hook method (pre-get) signatures.
	private static final String[] MARSHAL_HOOK_SIGNATURES =
	{
		"(Lorg/jibx/runtime/IMarshallingContext;)V",
		"(Ljava/lang/Object;)V",
		"()V"
	};
	
	// recognized factory hook method signatures.
	private static final String[] FACTORY_HOOK_SIGNATURES =
	{
		"(Lorg/jibx/runtime/IUnmarshallingContext;)",
		"(Ljava/lang/Object;)",
		"()"
	};
	
	// recognized unmarshal hook method (pre-set, post-set) signatures.
	private static final String[] UNMARSHAL_HOOK_SIGNATURES =
	{
		"(Lorg/jibx/runtime/IUnmarshallingContext;)V",
		"(Ljava/lang/Object;)V",
		"()V"
	};
    
    // marshaller/unmarshaller definitions
    private static final String UNMARSHALLER_INTERFACE =
        "org.jibx.runtime.IUnmarshaller";
    private static final String MARSHALLER_INTERFACE =
        "org.jibx.runtime.IMarshaller";
    private static final String UNMARSHALLER_INTERFACETYPE =
        "Lorg/jibx/runtime/IUnmarshaller;";
    private static final String MARSHALLER_INTERFACETYPE =
        "Lorg/jibx/runtime/IMarshaller;";
	
	//
	// Instance data.
    
    /** Factory method name (fully qualified, including package and class). */
    private String m_factoryName;
    
    /** Pre-set method name. */
    private String m_preSetName;
    
    /** Post-set method name. */
    private String m_postSetName;
    
    /** Pre-get method name. */
    private String m_preGetName;
    
    /** Object marshaller class name. */
    private String m_marshallerName;
    
    /** Object unmarshaller class name. */
    private String m_unmarshallerName;
    
    /** Nillable object flag. */
    private boolean m_isNillable;
    
    /** Instance type for creation (fully qualified, including package and
     class). */
    private String m_createType;
	
	/** Factory method information. */
	private IClassItem m_factoryItem;
	
	/** Pre-set method information. */
	private IClassItem m_preSetItem;
	
	/** Post-set method information. */
	private IClassItem m_postSetItem;
	
	/** Pre-get method information. */
	private IClassItem m_preGetItem;
	
	/** Object marshaller class. */
	private IClass m_marshallerClass;
	
	/** Object unmarshaller class. */
	private IClass m_unmarshallerClass;
    
    /** Class to use for new instance creation. */
    private IClass m_createClass;
	
	/**
	 * Constructor.
	 */
	public ObjectAttributes() {}
    
    /**
     * Get factory method name.
     * 
     * @return fully-qualified factory class and method name (or
     * <code>null</code> if none)
     */
    public String getFactoryName() {
        return m_factoryName;
    }
	
	/**
	 * Get factory method information. This method is only usable after a
     * call to {@link #prevalidate(ValidationContext)}.
	 * 
	 * @return factory method information (or <code>null</code> if none)
	 */
	public IClassItem getFactory() {
		return m_factoryItem;
	}
	
	/**
	 * Set factory method name.
	 * 
	 * @param name fully qualified class and method name for object factory
	 */
	public void setFactoryName(String name) {
        m_factoryName = name;
	}
    
    /**
     * Get pre-set method name.
     * 
     * @return pre-set method name (or <code>null</code> if none)
     */
    public String getPresetName() {
        return m_preSetName;
    }
	
	/**
	 * Get pre-set method information. This method is only usable after a
     * call to {@link #prevalidate(ValidationContext)}.
	 * 
	 * @return pre-set method information (or <code>null</code> if none)
	 */
	public IClassItem getPreset() {
		return m_preSetItem;
	}
	
	/**
	 * Set pre-set method name.
	 * 
	 * @param name member method name to be called before unmarshalling
	 */
	public void setPresetName(String name) {
		m_preSetName = name;
	}
    
    /**
     * Get post-set method name.
     * 
     * @return post-set method name (or <code>null</code> if none)
     */
    public String getPostsetName() {
        return m_postSetName;
    }
	
	/**
	 * Get post-set method information. This method is only usable after a
     * call to {@link #prevalidate(ValidationContext)}.
	 * 
	 * @return post-set method information (or <code>null</code> if none)
	 */
	public IClassItem getPostset() {
		return m_postSetItem;
	}
	
	/**
	 * Set post-set method name.
	 * 
	 * @param name member method name to be called after unmarshalling
	 */
	public void setPostsetName(String name) {
		m_postSetName = name;
	}
    
    /**
     * Get pre-get method name.
     * 
     * @return pre-get method name (or <code>null</code> if none)
     */
    public String getPregetName() {
        return m_preGetName;
    }
	
	/**
	 * Get pre-get method information. This method is only usable after a
     * call to {@link #prevalidate(ValidationContext)}.
	 * 
	 * @return pre-get method information (or <code>null</code> if none)
	 */
	public IClassItem getPreget() {
		return m_preGetItem;
	}
	
	/**
	 * Set pre-get method name.
	 * 
	 * @param name member method name to be called before marshalling
	 */
	public void setPregetName(String name) {
		m_preGetName = name;
	}
    
    /**
     * Get marshaller class name.
     * 
     * @return marshaller class name (or <code>null</code> if none)
     */
    public String getMarshallerName() {
        return m_marshallerName;
    }
	
	/**
	 * Get marshaller class information. This method is only usable after a
     * call to {@link #prevalidate(ValidationContext)}.
	 * 
	 * @return class information for marshaller (or <code>null</code> if none)
	 */
	public IClass getMarshaller() {
		return m_marshallerClass;
	}
	
	/**
	 * Set marshaller class name.
	 * 
	 * @param name class name to be used for marshalling
	 */
	public void setMarshallerName(String name) {
		m_marshallerName = name;
	}
    
    /**
     * Get unmarshaller class name.
     * 
     * @return unmarshaller class name (or <code>null</code> if none)
     */
    public String getUnmarshallerName() {
        return m_unmarshallerName;
    }
	
	/**
	 * Get unmarshaller class information. This method is only usable after a
     * call to {@link #prevalidate(ValidationContext)}.
	 * 
	 * @return class information for unmarshaller (or <code>null</code> if none)
	 */
	public IClass getUnmarshaller() {
		return m_unmarshallerClass;
	}
	
	/**
	 * Set unmarshaller class name.
	 * 
	 * @param name class name to be used for unmarshalling
	 */
	public void setUnmarshallerName(String name) {
		m_unmarshallerName = name;
	}
    
    /**
     * Check if nillable object.
     * 
     * @return nillable flag
     */
    public boolean isNillable() {
        return m_isNillable;
    }

    /**
     * Set nillable flag.
     * 
     * @param nillable flag
     */
    public void setNillable(boolean nillable) {
        m_isNillable = nillable;
    }
    
    /**
     * Get type to be used for creating new instance.
     * 
     * @return class name for type to be created (or <code>null</code> if none)
     */
    public String getCreateType() {
        return m_createType;
    }
    
    /**
     * Get new instance creation class information. This method is only usable
     * after a call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return class information for type to be created (or <code>null</code> if
     * none)
     */
    public IClass getCreateClass() {
        return m_createClass;
    }
    
    /**
     * Set new instance type class name.
     * 
     * @param name class name to be used for creating new instance
     */
    public void setCreateType(String name) {
        m_createType = name;
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.AttributeBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // first check for actual object association
        IClass iclas;
        ElementBase element = vctx.getParentElement(0);
        if (element instanceof StructureElementBase) {
            if (!((StructureElementBase)element).hasObject()) {
                if (m_factoryName != null) {
                    vctx.addWarning("No object for structure; factory attribute ignored");
                    m_factoryName = null;
                }
                if (m_preSetName != null) {
                    vctx.addWarning("No object for structure; pre-set attribute ignored");
                    m_preSetName = null;
                }
                if (m_preGetName != null) {
                    vctx.addWarning("No object for structure; pre-get attribute ignored");
                    m_preGetName = null;
                }
                if (m_postSetName != null) {
                    vctx.addWarning("No object for structure; post-set attribute ignored");
                    m_postSetName = null;
                }
                if (m_marshallerName != null) {
                    vctx.addWarning("No object for structure; marshaller attribute ignored");
                    m_marshallerName = null;
                }
                if (m_unmarshallerName != null) {
                    vctx.addWarning("No object for structure; unmarshaller attribute ignored");
                    m_unmarshallerName = null;
                }
                if (m_createType != null) {
                    vctx.addWarning("No object for structure; create-type attribute ignored");
                    m_createType = null;
                }
                if (m_isNillable) {
                    vctx.addError("No object for structure; nillable attribute forbidden");
                    m_isNillable = false;
                }
                return;
            } else {
                iclas = ((StructureElementBase)element).getType();
            }
        } else if (element instanceof MappingElementBase) {
            iclas = ((MappingElementBase)element).getHandledClass();
        } else {
            throw new IllegalStateException
                ("Unknown element for object attributes");
        }
        String type = iclas.getName();
        
        // first check for marshaller and unmarshaller classes
        if (m_marshallerName != null) {
            if (vctx.isOutBinding()) {
                IClass mclas = vctx.getClassInfo(m_marshallerName);
                if (mclas == null) {
                    vctx.addError("Marshaller class " + m_marshallerName + " not found");
                } else if (vctx.isLookupSupported() && !mclas.isImplements(MARSHALLER_INTERFACETYPE)) {
                    vctx.addError("Marshaller class " + m_marshallerName + " does not implement interface " + MARSHALLER_INTERFACE);
                } else {
                    m_marshallerClass = mclas;
                }
            } else {
                vctx.addWarning("marshaller attribute ignored for input-only binding");
            }
        }
        if (m_unmarshallerName != null) {
            if (vctx.isInBinding()) {
                
                // get the unmarshaller information
                IClass uclas = vctx.getClassInfo(m_unmarshallerName);
                if (uclas == null) {
                    vctx.addError("Unmarshaller class " + m_unmarshallerName + " not found");
                } else if (vctx.isLookupSupported() && !uclas.isImplements(UNMARSHALLER_INTERFACETYPE)) {
                    vctx.addError("Unmarshaller class " + m_unmarshallerName + " does not implement interface " + UNMARSHALLER_INTERFACE);
                } else {
                    m_unmarshallerClass = uclas;
                }
                
                // check for incompatible attributes
                if (m_factoryName != null) {
                    vctx.addWarning("unmarshaller supplied, factory attribute ignored");
                    m_factoryName = null;
                }
                if (m_createType != null) {
                    vctx.addWarning
                        ("unmarshaller supplied, create-type attribute ignored");
                    m_createType = null;
                }
            } else {
                vctx.addWarning("unmarshaller attribute ignored for output-only binding");
            }
        }
        
        // make sure both are supplied if either is supplied
        if (vctx.isInBinding() && vctx.isOutBinding()) {
            if (m_unmarshallerName != null && m_marshallerName == null) {
                vctx.addError("Marshaller is required if unmarshaller is supplied");
            } else if (m_marshallerName != null && m_unmarshallerName == null) {
                vctx.addError("Unmarshaller is required if marshaller is supplied");
            }
        }
        
        // next check for factory supplied
        if (m_factoryName != null) {
            if (vctx.isInBinding()) {
                if (vctx.isLookupSupported()) {
                    
                    // find factory method and verify signature
                    m_factoryItem = ClassUtils.findStaticMethod(m_factoryName,
                        FACTORY_HOOK_SIGNATURES, vctx);
                    if (m_factoryItem == null) {
                        if (m_factoryName.indexOf('.') > 0) {
                            vctx.addError("Static factory method " + m_factoryName + " not found");
                        } else {
                            vctx.addError("Need class name for static method " + m_factoryName);
                        }
                    } else {
                        String ftype = m_factoryItem.getTypeName();
                        if (!ClassUtils.isAssignable(ftype, type, vctx) &&
                            !ClassUtils.isAssignable(type, ftype, vctx))
                        vctx.addError("Static factory method " + m_factoryName + " return type is not compatible with " + type);
                    }
                }
                
                // check for incompatible attributes
                if (m_createType != null) {
                    vctx.addWarning("unmarshaller supplied, create-type attribute ignored");
                    m_createType = null;
                }
                
            } else {
                vctx.addWarning("factory attribute ignored for output-only binding");
            }
        }
        
        // check for create-type attribute
        if (m_createType != null) {
            if (vctx.isInBinding()) {
                
                // find create type class and verify compatibility
                m_createClass = vctx.getClassInfo(m_createType);
                if (m_createClass == null) {
                    vctx.addError("create-type class " + m_createType + " not found");
                } else if (vctx.isLookupSupported()) {
                    if (!ClassUtils.isAssignable(m_createType, type, vctx) &&
                        !ClassUtils.isAssignable(type, m_createType, vctx))
                    vctx.addError("create-type " + m_createType + " is not compatible with expected type " + type);
                }
                
            } else {
                vctx.addWarning
                    ("create-type attribute ignored for output-only binding");
            }
        }
        
        // handle pre-set, post-set, and pre-get methods
        if (vctx.isLookupSupported()) {
            if (vctx.isInBinding()) {
                if (m_preSetName != null) {
                    m_preSetItem = iclas.getMethod(m_preSetName,
                        UNMARSHAL_HOOK_SIGNATURES);
                    if (m_preSetItem == null) {
                        vctx.addError("Nonstatic pre-set method " + m_preSetName + " not found");
                    }
                }
                if (m_postSetName != null) {
                    m_postSetItem = iclas.getMethod(m_postSetName,
                        UNMARSHAL_HOOK_SIGNATURES);
                    if (m_postSetItem == null) {
                        vctx.addError("Nonstatic post-set method " + m_postSetName + " not found");
                    }
                }
            }
            if (vctx.isOutBinding()) {
                if (m_preGetName != null) {
                    m_preGetItem = iclas.getMethod(m_preGetName,
                        MARSHAL_HOOK_SIGNATURES);
                    if (m_preGetItem == null) {
                        vctx.addError("Nonstatic pre-get method " + m_preGetName + " not found");
                    }
                }
            }
        }
    }
}
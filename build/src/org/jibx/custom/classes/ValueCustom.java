/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.custom.classes;

import java.lang.reflect.Modifier;

import org.apache.bcel.generic.Type;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.Utility;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.IClassLocator;
import org.jibx.util.StringArray;

/**
 * Member field or property customization information.
 * 
 * @author Dennis M. Sosnoski
 */
public class ValueCustom extends SharedValueBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "attribute", "element", "field", "get-method", "property-name", "set-method" },
        SharedValueBase.s_allowedAttributes);
    
    //
    // Internal instance data
    
    /** Private property flag. */
    private boolean m_private;
    
    //
    // Values handled by binding
    
    /** 'field' attribute value (<code>null</code> if none). */
    private String m_fieldName;
    
    /** 'get-method' attribute value (<code>null</code> if none). */
    private String m_getName;
    
    /** 'set-method' attribute value (<code>null</code> if none). */
    private String m_setName;
    
    /** 'property-name' attribute value (<code>null</code> if none). */
    private String m_propertyName;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    protected ValueCustom(SharedNestingBase parent) {
        super(parent);
    }
    
    /**
     * Constructor with name known.
     * 
     * @param parent
     * @param name
     */
    protected ValueCustom(SharedNestingBase parent, String name) {
        super(parent, name);
    }
    
    /**
     * Get the member name for a property from the read method name. This means stripping off the leading "get" or "is"
     * prefix, then case-converting the result.
     * 
     * @param name
     * @return member name
     * @see #convertMemberNameCase(String)
     * @see #memberNameFromSetMethod(String)
     * @see #memberNameFromField(String, String[], String[])
     */
    public static String memberNameFromGetMethod(String name) {
        if (name.startsWith("get")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        }
        return convertMemberNameCase(name);
    }
    
    /**
     * Get the member name for a property from the write method name. This means stripping off the leading "set" prefix,
     * then case-converting the result.
     * 
     * @param name
     * @return member name
     * @see #convertMemberNameCase(String)
     * @see #memberNameFromGetMethod(String)
     * @see #memberNameFromField(String, String[], String[])
     */
    public static String memberNameFromSetMethod(String name) {
        if (name.startsWith("set")) {
            name = name.substring(3);
        }
        return convertMemberNameCase(name);
    }
    
    /**
     * Get the member name for a field from the field name. This means stripping off and leading field name prefix
     * and/or trailing suffix, then case-converting the result.
     * 
     * @param name
     * @param prefs field prefixes to be stripped
     * @param suffs field suffixes to be stripped
     * @return member name
     * @see #convertMemberNameCase(String)
     * @see #memberNameFromGetMethod(String)
     * @see #memberNameFromSetMethod(String)
     */
    public static String memberNameFromField(String name, String[] prefs, String[] suffs) {
        if (prefs != null) {
            for (int i = 0; i < prefs.length; i++) {
                if (name.startsWith(prefs[i])) {
                    name = name.substring(prefs[i].length());
                    break;
                }
            }
        }
        if (suffs != null) {
            for (int i = 0; i < suffs.length; i++) {
                if (name.endsWith(prefs[i])) {
                    name = name.substring(name.length() - prefs[i].length());
                    break;
                }
            }
        }
        return convertMemberNameCase(name);
    }
    
    /**
     * Set element name method. This is intended for use during unmarshalling, so it needs to allow for being called
     * with a <code>null</code> value. TODO: add validation
     * 
     * @param text (<code>null</code> if attribute not present)
     * @param ictx
     */
    private void setElement(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            setXmlName(text);
            setElementForced();
            setStyle(new Integer(NestingBase.ELEMENT_VALUE_STYLE));
        }
    }
    
    /**
     * Set attribute name method. This is intended for use during unmarshalling, so it needs to allow for being called
     * with a <code>null</code> value. TODO: add validation
     * 
     * @param text (<code>null</code> if attribute not present)
     * @param ictx
     */
    private void setAttribute(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            setXmlName(text);
            setStyle(new Integer(NestingBase.ATTRIBUTE_VALUE_STYLE));
        }
    }
    
    /**
     * Check if member represents a property.
     * 
     * @return <code>true</code>
     */
    public boolean isProperty() {
        return true;
    }
    
    /**
     * Check if a private member.
     *
     * @return <code>true</code> if private, <code>false</code> if not
     */
    public boolean isPrivate() {
        return m_private;
    }
    
    /**
     * Get 'field' attribute value.
     * 
     * @return 'field' value (<code>null</code> if none)
     */
    public String getFieldName() {
        return m_fieldName;
    }
    
    /**
     * Get 'get-method' attribute name.
     * 
     * @return 'get' attribute name (<code>null</code> if none)
     */
    public String getGetName() {
        return m_getName;
    }
    
    /**
     * Get 'set-method' attribute name.
     * 
     * @return 'set-method' attribute name (<code>null</code> if none)
     */
    public String getSetName() {
        return m_setName;
    }
    
    /**
     * Get 'property-name' attribute value.
     * 
     * @return property name (<code>null</code> if none)
     */
    public String getPropertyName() {
        return m_propertyName;
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    protected void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Post-set method that handles checking attribute combinations and sets the actual member name.
     * 
     * @throws JiBXException
     */
    protected void postSet() throws JiBXException {
        if (m_fieldName != null) {
            if (m_getName != null || m_setName != null || m_propertyName != null) {
                throw new JiBXException("'get-name' or 'set-name' or 'property-name' attributes cannot be used together with 'field' attribute on <value> element");
            }
            ClassCustom clas = (ClassCustom)getParent();
            setBaseName(memberNameFromField(m_fieldName, clas.getStripPrefixes(), clas.getStripSuffixes()));
        } else if (m_getName == null && m_setName == null && m_propertyName == null) {
            throw new JiBXException("Either 'field', 'get-name', 'set-name', or 'property-name' attribute is required on <value> element");
        } else {
            if (m_setName == null) {
                if (m_getName == null) {
                    setBaseName(m_propertyName);
                } else {
                    setBaseName(memberNameFromGetMethod(m_getName));
                }
            } else {
                setBaseName(memberNameFromSetMethod(m_setName));
            }
        }
    }
     
    /**
     * Complete customization information based on either field or access method information.
     * 
     * @param field (<code>null</code> if none)
     * @param gmeth read access method (<code>null</code> if none)
     * @param smeth write access method (<code>null</code> if none)
     * @param icl class locator
     * @param req required member flag (<code>null</code> if unknown)
     * @param style representation style (<code>null</code> if unspecified)
     */
    /* package */void fillDetails(IClassItem field, IClassItem gmeth, IClassItem smeth, IClassLocator icl, Boolean req,
        Integer style) {
        
        // fill in missing details for either field or property
        String type = null;
        if (field == null) {
            if (gmeth != null) {
                if (m_getName == null) {
                    m_getName = gmeth.getName();
                }
                type = gmeth.getTypeName();
            }
            if (smeth != null) {
                if (m_setName == null) {
                    m_setName = smeth.getName();
                }
                if (type == null) {
                    type = smeth.getArgumentType(0);
                }
            }
            m_private = (gmeth == null || Modifier.isPrivate(gmeth.getAccessFlags())) &&
                (smeth == null || Modifier.isPrivate(smeth.getAccessFlags()));
        } else {
            if (m_fieldName == null) {
                m_fieldName = field.getName();
            }
            m_private = Modifier.isPrivate(field.getAccessFlags());
            type = field.getTypeName();
        }
        
        // fill in the details of type information
        IClass info = icl.getClassInfo(type);
        fillType(info, req, style);
        if (isCollection()) {
            if (getItemType() == null) {
                String tname = getWorkingType();
                if (tname.endsWith("[]")) {
                    
                    // set item type directly from array type
                    setItemType(tname.substring(0, tname.length() - 2));
                    
                } else {
                    
                    // find generic signature for type
                    String sig;
                    if (field != null) {
                        sig = field.getGenericsSignature();
                    } else {
                        if (gmeth == null) {
                            sig = smeth.getGenericsSignature();
                        } else {
                            sig = gmeth.getGenericsSignature();
                        }
                    }
                    
                    // find type based on field or method signature
                    if (sig != null) {
                        int start = sig.indexOf('<');
                        int end = sig.lastIndexOf('>');
                        if (start > 0 && end > 0 && start < end) {
                            String tsig = sig.substring(start + 1, end);
                            if (tsig.indexOf('<') >= 0 || tsig.indexOf('+') >= 0) {
                                System.out.println("Warning: generic signature '" + tsig + "' ignored");
                            } else {
                                setItemType(Type.getType(tsig).toString());
                            }
                        }
                    }
                    
                }
            }
            if (getItemType() == null) {
                setItemType("java.lang.Object");
            }
            
            // derive the item name if not already set
            if (getItemName() == null) {
                setItemName(deriveItemName(getXmlName(), getItemType(), getParent().getNameStyle()));
            }
            
        }
    }
    
    /**
     * Complete customization information based on whatever field or access method information has been set.
     * 
     * @param info containing class information
     * @param req required member flag (<code>null</code> if unknown)
     * @param style representation style (<code>null</code> if unspecified)
     */
    /* package */void fillDetails(IClass info, Boolean req, Integer style) {
        IClassItem field = null;
        if (m_fieldName != null) {
            field = info.getField(m_fieldName);
            if (field == null) {
                throw new IllegalArgumentException("Field " + m_fieldName + " not found in class " + info.getName());
            }
        }
        IClassItem gmeth = null;
        if (m_getName != null) {
            gmeth = info.getBestMethod(m_getName, null, Utility.EMPTY_STRING_ARRAY);
            if (gmeth == null) {
                throw new IllegalArgumentException("get method " + m_getName + " not found in class " + info.getName());
            }
        }
        IClassItem smeth = null;
        if (m_setName != null) {
            smeth = info.getBestMethod(m_setName, "void", null);
            if (smeth == null) {
                throw new IllegalArgumentException("gset method " + m_setName + " not found in class " + info.getName());
            }
        }
        fillDetails(field, gmeth, smeth, info.getLocator(), req, style);
    }
    
    /**
     * Factory method for creating instances during unmarshalling.
     *
     * @param ictx
     * @return instance
     */
    private static ValueCustom factory(IUnmarshallingContext ictx) {
        return new ValueCustom(((ClassCustom)getContainingObject(ictx)));
    }
}
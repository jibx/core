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

package org.jibx.ws.wsdl.tools.custom;

import java.util.List;

import org.apache.log4j.Logger;
import org.jibx.custom.classes.SharedValueBase;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.util.IClass;
import org.jibx.util.StringArray;

/**
 * Method parameter or return value customization information.
 * 
 * @author Dennis M. Sosnoski
 */
public class ValueCustom extends SharedValueBase
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(ValueCustom.class.getName());
    
    /** Style for elements used to wrap parameter values in request element. */
    private static final Integer ELEMENT_STYLE = new Integer(org.jibx.custom.classes.NestingBase.ELEMENT_VALUE_STYLE);
    
    /** Enumeration of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "element", "name" }, SharedValueBase.s_allowedAttributes);
    
    //
    // Internal instance data
    
    private String m_boundType; // internal use, not included in binding

    /** Documentation text (as org.w3.dom.Node components). */
    private List m_documentation;
    
    /**
     * Constructor.
     * 
     * @param parent
     * @param name
     */
    protected ValueCustom(NestingBase parent, String name) {
        super(parent, name);
    }
    
    /**
     * Get value type to be bound. This is the same as the plain value type for a simple (non-collection); for an array
     * value, it's just the array item type; and for a non-array collection it takes the same form as a generic type
     * declaration, with the actual item type enclosed in a less-than/greater-than sign pair following the base type.
     * 
     * @return parmaterized type
     */
    public String getBoundType() {
        return m_boundType;
    }
    
    /**
     * Get value documentation node list. This method should only be used after the
     * {@link #complete(IClass, List, Boolean, String)} method is called.
     * 
     * @return list of documentation nodes (<code>null</code> if none)
     */
    public List getDocumentation() {
        return m_documentation;
    }

    /**
     * Complete customization information based on supplied type. If the type information has not previously been set,
     * this will set it. It will also derive the appropriate XML name, if not previously set.
     * 
     * @param info value type information
     * @param docs default documentation text (<code>null</code> if none)
     * @param req required member flag (<code>null</code> if unspecified)
     * @param itype item type from signature (<code>null</code> if unknown)
     */
    /* package */void complete(IClass info, List docs, Boolean req, String itype) {
        if (s_logger.isDebugEnabled()) {
            StringBuffer buff = new StringBuffer();
            if (getBaseName() == null) {
                buff.append("Completing return value");
            } else {
                buff.append("Completing value ");
                buff.append(getBaseName());
            }
            s_logger.debug(buff.toString());
        }
        fillType(info, req, ELEMENT_STYLE);
        if (isCollection()) {
            
            // make sure the item type is set
            if (getItemType() == null) {
                String tname = getWorkingType();
                if (tname.endsWith("[]")) {
                    
                    // set item type directly from array type
                    setItemType(tname.substring(0, tname.length() - 2));
                    
                } else if (itype != null) {
                    
                    // set item type from signature
                    setItemType(itype);
                    
                } else {
                    setItemType("java.lang.Object");
                }
            }
            
            // derive the item name if not already set
            if (getItemName() == null) {
                setItemName(deriveItemName(getXmlName(), getItemType(), getParent().getNameStyle()));
            }
            
            // set type for binding definition
            String type = info.getName();
            if (itype == null) {
                if (type.endsWith("[]")) {
                    m_boundType = type.substring(0, type.length() - 2);
                } else {
                    m_boundType = type;
                }
            } else {
                m_boundType = type + '<' + itype + '>';
            }
            
        } else {
            m_boundType = info.getName();
        }
    }
    
    /**
     * Make sure all attributes are defined when unmarshalling (only used by binding).
     * 
     * @param uctx unmarshalling context
     */
    protected void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Set element name method. This is intended for use during unmarshalling, so it needs to allow for being called
     * with a <code>null</code> value.
     * 
     * @param text (<code>null</code> if attribute not present)
     * @param ictx
     */
    private void setElement(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            setXmlName(text);
            setElementForced();
            setStyle(new Integer(org.jibx.custom.classes.NestingBase.ELEMENT_VALUE_STYLE));
        }
    }
    
    /**
     * Parameter value unmarshalling factory. This gets the containing element and the name so that the standard
     * constructor can be used.
     * 
     * @param ictx
     * @return created instance
     * @throws JiBXException
     */
    private static ValueCustom parameterFactory(IUnmarshallingContext ictx) throws JiBXException {
        String name = ((UnmarshallingContext)ictx).attributeText(null, "name");
        s_logger.debug("Creating parameter instance with name " + name);
        return new ValueCustom((OperationCustom)getContainingObject(ictx), name);
    }
    
    /**
     * Return value unmarshalling factory. This gets the containing element so that the standard constructor can be
     * used.
     * 
     * @param ictx
     * @return created instance
     */
    private static ValueCustom returnFactory(IUnmarshallingContext ictx) {
        return new ValueCustom((OperationCustom)getContainingObject(ictx), "return");
    }
}
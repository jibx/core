/*
 * Copyright (c) 2007, Dennis M. Sosnoski All rights reserved.
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

import java.util.Collection;
import java.util.List;

import org.jibx.custom.classes.CustomBase;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.IClassLocator;
import org.jibx.util.Types;

/**
 * Fault data customization information.
 * TODO: include this in the customizations file structure - child of service element?
 * 
 * @author Dennis M. Sosnoski
 */
public class FaultCustom extends CustomBase
{
    // fault data customization information
    private String m_exceptionType;
    
    private String m_fieldName;
    
    private String m_dataType;
    
    private String m_faultName;
    
    private String m_elementName;
    
    private List m_documentation;
    
    /**
     * Constructor.
     * 
     * @param parent
     * @param type fully-qualified exception class name
     */
    protected FaultCustom(NestingBase parent, String type) {
        super(parent);
        m_exceptionType = type;
    }
    
    /**
     * Get fully-qualified exception class name.
     * 
     * @return type
     */
    public String getExceptionType() {
        return m_exceptionType;
    }
    
    /**
     * Get Fault name. This method should only be used after the {@link #apply(IClassLocator)} method is called.
     * 
     * @return parmaterized type
     */
    public String getFaultName() {
        return m_faultName;
    }
    
    /**
     * Get XML element name for exception data. This method should only be used after the {@link #apply(IClassLocator)}
     * method is called.
     * 
     * @return name
     */
    public String getElementName() {
        return m_elementName;
    }
    
    /**
     * Get fully-qualified name of exception data class.
     * 
     * @return parmaterized type
     */
    public String getDataType() {
        return m_dataType;
    }
    
    /**
     * Get value documentation node list. This method should only be used after the {@link #apply(IClassLocator)} method
     * is called.
     * 
     * @return list of documentation nodes (<code>null</code> if none)
     */
    public List getDocumentation() {
        return m_documentation;
    }
    
    /**
     * Apply customizations to fault to fill out members.
     * 
     * @param icl class locator
     */
    public void apply(IClassLocator icl) {
        String simple = m_exceptionType.substring(m_exceptionType.lastIndexOf('.') + 1);
        if (simple.endsWith("Exception")) {
            simple = simple.substring(0, simple.length() - 9);
        }
        if (m_elementName == null) {
            m_elementName = getParent().convertName(simple);
        }
        if (m_faultName == null) {
            m_faultName = m_elementName + "Fault";
        }
        IClass clas = icl.getRequiredClassInfo(m_exceptionType);
        if (m_fieldName == null) {
            IClassItem[] fields = clas.getFields();
            for (int i = 0; i < fields.length; i++) {
                IClassItem item = fields[i];
                String type = item.getTypeName();
                if (!Types.isSimpleValue(type)) {
                    IClass info = icl.getRequiredClassInfo(type);
                    if (info.isModifiable()) {
                        m_fieldName = item.getName();
                        m_dataType = type;
                        break;
                    }
                }
            }
            if (m_fieldName == null) {
                throw new IllegalStateException("No data object field found for exception class " + m_exceptionType);
            }
        } else {
            IClassItem field = clas.getField(m_fieldName);
            if (field == null) {
                throw new IllegalStateException("Field " + m_fieldName + " not found in exception class "
                    + m_exceptionType);
            } else {
                m_dataType = field.getTypeName();
            }
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
    private static FaultCustom throwsFactory(IUnmarshallingContext ictx) throws JiBXException {
        UnmarshallingContext uctx = (UnmarshallingContext)ictx;
        Object parent = uctx.getStackTop();
        int depth = 0;
        if (parent instanceof Collection) {
            parent = uctx.getStackObject(++depth);
        }
        return new FaultCustom((OperationCustom)parent, uctx.attributeText(null, "class"));
    }
}
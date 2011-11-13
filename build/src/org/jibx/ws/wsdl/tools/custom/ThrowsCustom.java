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

import java.util.List;

import org.jibx.custom.classes.CustomBase;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.util.StringArray;

/**
 * Method throws customization information. This just defines the actual exceptions to be handled for a method
 * 
 * @author Dennis M. Sosnoski
 */
public class ThrowsCustom extends CustomBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes = new StringArray(new String[] { "class" });
    
    // value customization information
    private String m_type;
    
    private List m_documentation;
    
    /**
     * Constructor.
     * 
     * @param parent
     * @param type fully-qualified class name thrown
     */
    protected ThrowsCustom(NestingBase parent, String type) {
        super(parent);
        m_type = type;
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Get fully-qualified class name thrown.
     * 
     * @return type
     */
    public String getType() {
        return m_type;
    }
    
    /**
     * Get value documentation node list. This method should only be used after the {@link #complete(List)} method is
     * called.
     * 
     * @return list of documentation nodes (<code>null</code> if none)
     */
    public List getDocumentation() {
        return m_documentation;
    }
    
    /**
     * Complete customization information using supplied default documentation.
     * 
     * @param docs default documentation text (<code>null</code> if none)
     */
    /* package */void complete(List docs) {
        if (m_documentation == null) {
            m_documentation = docs;
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
    private static ThrowsCustom throwsFactory(IUnmarshallingContext ictx) throws JiBXException {
        return new ThrowsCustom((OperationCustom)getContainingObject(ictx), ((UnmarshallingContext)ictx).attributeText(
            null, "class"));
    }
}
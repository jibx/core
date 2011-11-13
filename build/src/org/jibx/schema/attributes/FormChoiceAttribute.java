/*
Copyright (c) 2006, Dennis M. Sosnoski
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

package org.jibx.schema.attributes;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.support.Conversions;
import org.jibx.util.StringArray;

/**
 * Attribute to set form of name (qualified or unqualified).
 *
 * @author Dennis M. Sosnoski
 */
public class FormChoiceAttribute extends AttributeBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "form" });
    
    //
    // Value set information
    
    public static final int QUALIFIED_FORM = 0;
    public static final int UNQUALIFIED_FORM = 1;
    
    public static final EnumSet s_formValues = new EnumSet(QUALIFIED_FORM,
        new String[] { "qualified", "unqualified"});
    
    //
    // Instance data
    
    /** 'form' attribute type code (<code>-1</code> if not set). */
    private int m_formType;
    
    /**
     * Constructor.
     * 
     * @param owner owning element
     */
    public FormChoiceAttribute(SchemaBase owner) {
        super(owner);
        m_formType = -1;
    }
    
    /**
     * Factory method for use during unmarshalling. This gets the owning element
     * from the unmarshalling context, and creates an instance of the attribute
     * tied to that element.
     *
     * @param ictx
     * @return constructed instance
     */
    private static FormChoiceAttribute
        unmarshalFactory(IUnmarshallingContext ictx) {
        return new FormChoiceAttribute((SchemaBase)ictx.getStackTop());
    }
    
    //
    // Access methods
    
    /**
     * Get 'form' attribute type code.
     * 
     * @return type
     */
    public int getForm() {
        return m_formType;
    }
    
    /**
     * Set 'form' attribute type code.
     * 
     * @param type
     */
    public void setForm(int type) {
        s_formValues.checkValue(type);
        m_formType = type;
    }
    
    /**
     * Get 'form' attribute text.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getFormText() {
        if (m_formType >= 0) {
            return s_formValues.getName(m_formType);
        } else {
            return null;
        }
    }
    
    /**
     * Set 'form' attribute text. This method is provided only for use when
     * unmarshalling.
     * 
     * @param text
     * @param ictx
     */
    private void setFormText(String text, IUnmarshallingContext ictx) {
        m_formType = Conversions.convertEnumeration(text, s_formValues,
            "form", ictx);
    }
    
    //
    // Convenience method based on default
    
    /**
     * Check if qualified.
     * 
     * @param def default if not overridden
     * @return <code>true</code> if qualified, <code>false</code> if not
     */
    public boolean isQualified(boolean def) {
        if (m_formType >= 0) {
            return m_formType == FormChoiceAttribute.QUALIFIED_FORM;
        } else {
            return def;
        }
    }
}
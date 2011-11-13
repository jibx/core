/*
Copyright (c) 2004-2005, Dennis M. Sosnoski
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

import org.jibx.runtime.EnumSet;
import org.jibx.util.StringArray;

/**
 * Model component for <b>nesting</b> attribute group in binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class NestingAttributes extends AttributeBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "value-style" });
    
	//
	// Value set information
	
	public static final int ATTRIBUTE_STYLE = 0;
	public static final int ELEMENT_STYLE = 1;
    
    /*package*/ static final EnumSet s_styleEnum = new EnumSet(ATTRIBUTE_STYLE,
        new String[] { "attribute", "element" });
	
	//
	// Instance data
    
    /** Supplied style name. */
    private String m_styleName;
	
	/** Actual selected style. */
	private int m_styleIndex;
    
    /**
     * Get style string value.
     * 
     * @return style string value (<code>null</code> if undefined at this level)
     */
    public String getStyleName() {
        return m_styleName;
    }
    
    /**
     * Get style value. This method is only usable after a call to {@link
     * #validate}.
     * 
     * @return style value
     */
    public int getStyle() {
        return m_styleIndex;
    }
    
    /**
     * Set style name.
     * 
     * @param name style name (<code>null</code> to undefine style at this
     * level)
     */
    public void setStyleName(String name) {
        m_styleName = name;
    }
    
    //
    // Overrides of base class methods
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.AttributeBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (m_styleName == null) {
            NestingElementBase parent = vctx.getParentElement();
            if (parent == null) {
                m_styleIndex = ELEMENT_STYLE;
            } else {
                m_styleIndex = parent.getDefaultStyle();
            }
        } else {
            int style = s_styleEnum.getValue(m_styleName);
            if (style < 0) {
                vctx.addError("Value \"" + m_styleName +
                    "\" is not a valid style");
            } else {
                m_styleIndex = style;
            }
        }
        super.prevalidate(vctx);
    }
}
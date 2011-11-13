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

package org.jibx.schema.elements;

import java.util.ArrayList;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.support.Conversions;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Model component for <b>annotation</b> element.
 *
 * @author Dennis M. Sosnoski
 */
public class AnnotationElement extends OpenAttrBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "id" });
    
    /** Mask bits for annotation item child elements. */
    private long ITEMS_MASK = ELEMENT_MASKS[APPINFO_TYPE] |
        ELEMENT_MASKS[DOCUMENTATION_TYPE];
    
    //
    // Instance data
    
    /** Filtered list of annotation items. */
    private final FilteredSegmentList m_itemsList;
    
    /** "id" attribute value. */
    private String m_id;
    
    /** Annotation items. */
    private ArrayList m_items;
    
    /**
     * Constructor.
     */
    public AnnotationElement() {
    	super(ANNOTATION_TYPE);
        m_itemsList = new FilteredSegmentList(getChildrenWritable(),
            ITEMS_MASK, this);
    }
    
    //
    // Base class overrides

    /* (non-Javadoc)
     * @see org.jibx.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
    
    //
    // Access methods

    /**
     * Get list of annotation item (<b>appInfo</b> and/or <b>documentation</b>)
     * child elements.
     *
     * @return list of attributes
     */
    public FilteredSegmentList getItemsList() {
        return m_itemsList;
    }

    /**
     * Get "id" attribute value.
     * 
     * @return id attribute value
     */
    public String getId() {
        return m_id;
    }

    /**
     * Set "id" value for element.
     * 
     * @param id id attribute value
     */
    public void setId(String id) {
        m_id = id;
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // check for valid "id" attribute value
        m_id = Conversions.deserializeNCName(m_id, vctx, this);
        
        // continue with parent class prevalidation
        super.prevalidate(vctx);
    }
}
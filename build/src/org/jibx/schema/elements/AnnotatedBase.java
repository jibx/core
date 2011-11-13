/*
Copyright (c) 2006-2008, Dennis M. Sosnoski
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

import org.jibx.schema.support.Conversions;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base class for all element structures in schema definition which support
 * annotations. The 'id' attribute handling is also implemented in this class,
 * since it goes together with the annotation support in the schema for schema.
 * Finally, this class maintains the parent element relationship.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class AnnotatedBase extends OpenAttrBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "id" });
    
    //
    // Instance data
    
    /** Annotation for this element (<code>null</code> if none). */
    private AnnotationElement m_annotation;
    
    /** "id" attribute value. */
    private String m_id;
    
    /**
     * Constructor.
     * 
     * @param type element type
     */
    protected AnnotatedBase(int type) {
    	super(type);
    }
    
    /**
     * Get annotation.
     * 
     * @return annotation element (<code>null</code> if none)
     */
    public final AnnotationElement getAnnotation() {
        return m_annotation;
    }
    
    /**
     * Set annotation.
     * 
     * @param ann annotation element (<code>null</code> if none)
     */
    public final void setAnnotation(AnnotationElement ann) {
        m_annotation = ann;
        if (ann != null) {
            ann.setParent(this);
        }
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
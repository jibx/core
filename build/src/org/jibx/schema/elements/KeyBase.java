/*
 * Copyright (c) 2008-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.elements;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.schema.support.QNameConverter;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base representation for identity constraint elements. The actual identity constraint elements are represented as
 * static inner classes.
 * 
 * @author Dennis M. Sosnoski
 */
public class KeyBase extends AnnotatedBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes = new StringArray(new String[] { "name" },
        AnnotatedBase.s_allowedAttributes);
    
    //
    // Instance data
    
    /** Filtered list of selector element (one only). */
    private final FilteredSegmentList m_selectorList;
    
    /** Filtered list of field elements (one or more). */
    private final FilteredSegmentList m_fieldList;
    
    /** 'name' attribute value. */
    private String m_name;
    
    /**
     * Constructor.
     * 
     * @param type
     */
    public KeyBase(int type) {
        super(type);
        m_selectorList = new FilteredSegmentList(getChildrenWritable(), ELEMENT_MASKS[SELECTOR_TYPE], this);
        m_fieldList = new FilteredSegmentList(getChildrenWritable(), ELEMENT_MASKS[FIELD_TYPE], m_selectorList, this);
    }
    
    //
    // Base class overrides
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
    
    //
    // Access methods
    
    /**
     * Get 'name' attribute value.
     * 
     * @return name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Set 'name' attribute value.
     * 
     * @param name
     */
    public void setName(String name) {
        m_name = name;
    }
    
    /**
     * Get <b>selector</b> child element.
     * 
     * @return element, or <code>null</code> if none
     */
    public SelectionBase.SelectorElement getSelector() {
        return m_selectorList.size() > 0 ? (SelectionBase.SelectorElement)m_selectorList.get(0) : null;
    }
    
    /**
     * Set <b>selector</b> child element.
     * 
     * @param element element, or <code>null</code> if unsetting
     */
    public void setSelector(SelectionBase.SelectorElement element) {
        m_selectorList.clear();
        if (element != null) {
            m_selectorList.add(element);
        }
    }

    /**
     * Get list of <b>field</b> child elements.
     *
     * @return list
     */
    public FilteredSegmentList getFieldList() {
        return m_fieldList;
    }
    
    //
    // Validation methods
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (getName() == null) {
            vctx.addError("The 'name' attribute is required for identity constraint elements", this);
        }
        super.prevalidate(vctx);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // make sure the expected child elements are present
        if (m_selectorList.size() != 1) {
            vctx.addError("One and only one 'selector' child element is required", this);
        }
        if (m_fieldList.size() == 0) {
            vctx.addError("At least one 'field' child element is required", this);
        }
        
        // handle base class validation if still going
        if (!vctx.isSkipped(this)) {
            super.validate(vctx);
        }
    }
    
    //
    // Actual element subclasses
    
    public static class KeyElement extends KeyBase
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public KeyElement() {
            super(KEY_TYPE);
        }
    }
    
    public static class KeyrefElement extends KeyBase
    {
        /** List of allowed attribute names. */
        public static final StringArray s_allowedAttributes = new StringArray(new String[] { "refer" },
            KeyBase.s_allowedAttributes);
        
        //
        // Instance data
        
        /** 'refer' attribute value. */
        private QName m_refer;
        
        /**
         * Constructor. Just sets element type in base class.
         */
        public KeyrefElement() {
            super(KEYREF_TYPE);
        }
        
        /**
         * Get 'refer' attribute value.
         * 
         * @return refer
         */
        public QName getRefer() {
            return m_refer;
        }
        
        /**
         * Set 'refer' attribute value.
         * 
         * @param refer
         */
        public void setName(QName refer) {
            m_refer = refer;
        }
        
        //
        // Validation methods
        
        /* (non-Javadoc)
         * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
         */
        public void prevalidate(ValidationContext vctx) {
            if (m_refer == null) {
                vctx.addFatal("'refer' attribute is required by keyref element", this);
            } else {
                
                // patch qualified names with schema effective namespace
                SchemaElement schema = vctx.getCurrentSchema();
                String ens = schema.getEffectiveNamespace();
                QNameConverter.patchQNameNamespace(ens, m_refer);
                
            }
        }
    }
    
    public static class UniqueElement extends KeyBase
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public UniqueElement() {
            super(UNIQUE_TYPE);
        }
    }
}
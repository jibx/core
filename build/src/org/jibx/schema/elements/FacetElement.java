/*
 * Copyright (c) 2006-2008, Dennis M. Sosnoski All rights reserved.
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

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Facet implementation. This base class is used for all facets, with static inner subclasses for the actual facets.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class FacetElement extends AnnotatedBase
{
    //
    // Facet bit masks
    
    public static final int LENGTH_FACET_BIT = 0x0001;
    public static final int MINLENGTH_FACET_BIT = 0x0002;
    public static final int MAXLENGTH_FACET_BIT = 0x0004;
    public static final int PATTERN_FACET_BIT = 0x0008;
    public static final int ENUMERATION_FACET_BIT = 0x0010;
    public static final int WHITESPACE_FACET_BIT = 0x0020;
    public static final int MAXINCLUSIVE_FACET_BIT = 0x0040;
    public static final int MAXEXCLUSIVE_FACET_BIT = 0x0080;
    public static final int MININCLUSIVE_FACET_BIT = 0x0100;
    public static final int MINEXCLUSIVE_FACET_BIT = 0x0200;
    public static final int TOTALDIGITS_FACET_BIT = 0x0400;
    public static final int FRACTIONDIGITS_FACET_BIT = 0x0800;
    
    //
    // Facet element tables
    
    /** Ordered array of indexes for facet elements. */
    public static final int[] FACET_ELEMENT_INDEXES = { ENUMERATION_TYPE, FRACTIONDIGITS_TYPE, LENGTH_TYPE,
        MAXEXCLUSIVE_TYPE, MAXINCLUSIVE_TYPE, MAXLENGTH_TYPE, MINEXCLUSIVE_TYPE, MININCLUSIVE_TYPE, MINLENGTH_TYPE,
        PATTERN_TYPE, TOTALDIGITS_TYPE, WHITESPACE_TYPE };
    
    /** Ordered array of names of just the facet elements. */
    public static final String[] FACET_ELEMENT_NAMES;
    
    /** Mask for facet elements. */
    public static final long FACET_ELEMENT_MASK;
    
    static {
        String[] names = new String[FACET_ELEMENT_INDEXES.length];
        long mask = 0;
        for (int i = 0; i < FACET_ELEMENT_INDEXES.length; i++) {
            int index = FACET_ELEMENT_INDEXES[i];
            names[i] = ELEMENT_NAMES[index];
            mask |= ELEMENT_MASKS[index];
        }
        FACET_ELEMENT_NAMES = names;
        FACET_ELEMENT_MASK = mask;
    };
    
    //
    // Instance data
    
    /** Facet bit mask. */
    private final int m_bitMask;
    
    /** Facet exclusion mask. */
    private final int m_excludesMask;
    
    /**
     * Constructor.
     * 
     * @param type
     * @param bit mask
     * @param exclude exclusion bit mask
     */
    protected FacetElement(int type, int bit, int exclude) {
        super(type);
        m_bitMask = bit;
        m_excludesMask = exclude;
    }
    
    /**
     * Get facet bit mask.
     * 
     * @return bit mask
     */
    public int getBitMask() {
        return m_bitMask;
    }
    
    /**
     * Get excludes bit mask.
     * 
     * @return bit mask
     */
    public int getExcludesMask() {
        return m_excludesMask;
    }
    
    //
    // Actual facet subclasses
    
    public abstract static class FixedFacet extends FacetElement
    {
        /** List of allowed attribute names (including "id" from base). */
        public static final StringArray s_allowedAttributes = new StringArray(new String[] { "fixed", "value" },
            AnnotatedBase.s_allowedAttributes);
        
        //
        // Instance data
        
        /** "fixed" attribute value. */
        private Boolean m_fixed;
        
        /**
         * Constructor. Just passes on the element type to base class.
         * 
         * @param type
         * @param bit mask
         * @param exclude exclusion bit mask
         */
        public FixedFacet(int type, int bit, int exclude) {
            super(type, bit, exclude);
        }
        
        /**
         * Check if fixed. This convenience method just returns the default if the attribute value has not been set.
         * 
         * @return <code>true</code> if fixed, <code>false</code> if not
         */
        public boolean isFixed() {
            return m_fixed == null ? false : m_fixed.booleanValue();
        }
        
        /**
         * Get "fixed" attribute value.
         * 
         * @return fixed attribute value (<code>null</code> if not set)
         */
        public Boolean getFixed() {
            return m_fixed;
        }
        
        /**
         * Set "fixed" attribute value.
         * 
         * @param fixed fixed attribute value (<code>null</code> if unsetting)
         */
        public void setFinal(Boolean fixed) {
            m_fixed = fixed;
        }
        
        //
        // Validation methods
        
        /**
         * Make sure all attributes are defined.
         * 
         * @param uctx unmarshalling context
         * @exception JiBXException on unmarshalling error
         */
        protected void preset(IUnmarshallingContext uctx) throws JiBXException {
            validateAttributes(uctx, s_allowedAttributes);
            super.preset(uctx);
        }
    }
    
    public abstract static class NumFacet extends FixedFacet
    {
        /** "value" attribute value. */
        private int m_value;
        
        /**
         * Constructor. Just passes on the element type to base class.
         * 
         * @param type
         * @param bit mask
         * @param exclude exclusion bit mask
         */
        public NumFacet(int type, int bit, int exclude) {
            super(type, bit, exclude);
        }
        
        /**
         * Get "value" attribute value.
         * 
         * @return value attribute value
         */
        public int getValue() {
            return m_value;
        }
        
        /**
         * Set "value" attribute value.
         * 
         * @param value value attribute value
         */
        public void setValue(int value) {
            m_value = value;
        }
        
        //
        // Validation methods
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
         */
        public void prevalidate(ValidationContext vctx) {
            
            // check for valid attribute values
            if (m_value < 0) {
                vctx.addError("'value' attribute must not be negative", this);
            }
            
            // continue with parent class prevalidation
            super.prevalidate(vctx);
        }
    }
    
    public static class TotalDigits extends FixedFacet
    {
        /** "value" attribute value. */
        private int m_value;
        
        /**
         * Constructor.
         */
        public TotalDigits() {
            super(TOTALDIGITS_TYPE, TOTALDIGITS_FACET_BIT, TOTALDIGITS_FACET_BIT);
        }
        
        /**
         * Get "value" attribute value.
         * 
         * @return value attribute value
         */
        public int getValue() {
            return m_value;
        }
        
        /**
         * Set "value" attribute value.
         * 
         * @param value value attribute value
         */
        public void setValue(int value) {
            m_value = value;
        }
        
        //
        // Validation methods
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
         */
        public void prevalidate(ValidationContext vctx) {
            
            // check for valid attribute values
            if (m_value <= 0) {
                vctx.addError("'value' attribute must be strictly positive", this);
            }
            
            // continue with parent class prevalidation
            super.prevalidate(vctx);
        }
    }
    
    public static class FractionDigits extends NumFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public FractionDigits() {
            super(FRACTIONDIGITS_TYPE, FRACTIONDIGITS_FACET_BIT, FRACTIONDIGITS_FACET_BIT);
        }
    }
    
    public static class Length extends NumFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public Length() {
            super(LENGTH_TYPE, LENGTH_FACET_BIT, LENGTH_FACET_BIT | MINLENGTH_FACET_BIT | MAXLENGTH_FACET_BIT);
        }
    }
    
    public static class MinLength extends NumFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public MinLength() {
            super(MINLENGTH_TYPE, MINLENGTH_FACET_BIT, LENGTH_FACET_BIT | MINLENGTH_FACET_BIT);
        }
    }
    
    public static class MaxLength extends NumFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public MaxLength() {
            super(MAXLENGTH_TYPE, MAXLENGTH_FACET_BIT, LENGTH_FACET_BIT | MAXLENGTH_FACET_BIT);
        }
    }
    
    public abstract static class TextFacet extends FixedFacet
    {
        /** "value" attribute value. */
        private String m_value;
        
        /**
         * Constructor. Just passes on the element type to base class.
         * 
         * @param type
         * @param bit mask
         * @param exclude exclusion bit mask
         */
        public TextFacet(int type, int bit, int exclude) {
            super(type, bit, exclude);
        }
        
        /**
         * Get "value" attribute value.
         * 
         * @return value attribute value
         */
        public String getValue() {
            return m_value;
        }
        
        /**
         * Set "value" attribute value.
         * 
         * @param value value attribute value
         */
        public void setValue(String value) {
            m_value = value;
        }
    }
    
    public static class MinExclusive extends TextFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public MinExclusive() {
            super(MINEXCLUSIVE_TYPE, MINEXCLUSIVE_FACET_BIT, MINEXCLUSIVE_FACET_BIT | MININCLUSIVE_FACET_BIT);
        }
    }
    
    public static class MinInclusive extends TextFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public MinInclusive() {
            super(MININCLUSIVE_TYPE, MININCLUSIVE_FACET_BIT, MINEXCLUSIVE_FACET_BIT | MININCLUSIVE_FACET_BIT);
        }
    }
    
    public static class MaxExclusive extends TextFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public MaxExclusive() {
            super(MAXEXCLUSIVE_TYPE, MAXEXCLUSIVE_FACET_BIT, MAXEXCLUSIVE_FACET_BIT | MAXINCLUSIVE_FACET_BIT);
        }
    }
    
    public static class MaxInclusive extends TextFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public MaxInclusive() {
            super(MAXINCLUSIVE_TYPE, MAXINCLUSIVE_FACET_BIT, MAXEXCLUSIVE_FACET_BIT | MAXINCLUSIVE_FACET_BIT);
        }
    }
    
    public static class WhiteSpace extends TextFacet
    {
        //
        // Value set information
        
        public static final int PRESERVE_WHITESPACE = 1;
        
        public static final int REPLACE_WHITESPACE = 2;
        
        public static final int COLLAPSE_WHITESPACE = 3;
        
        public static final EnumSet s_finalValues = new EnumSet(PRESERVE_WHITESPACE, new String[] { "preserve",
            "replace", "collapse" });
        
        //
        // Instance data
        
        private int m_whitespaceType;
        
        /**
         * Constructor. Just sets element type in base class.
         */
        public WhiteSpace() {
            super(WHITESPACE_TYPE, WHITESPACE_FACET_BIT, WHITESPACE_FACET_BIT);
        }
        
        /**
         * Get whitespace handling type code.
         * 
         * @return type code for whitespace handling
         */
        public int getWhitespaceType() {
            return m_whitespaceType;
        }
        
        //
        // Validation methods
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
         */
        public void prevalidate(ValidationContext vctx) {
            
            // check for valid attribute values
            m_whitespaceType = s_finalValues.getValue(getValue());
            if (m_whitespaceType < 0) {
                vctx.addError("'whitespace' attribute value '" + getValue() + "' is not allowed", this);
            }
            
            // continue with parent class prevalidation
            super.prevalidate(vctx);
        }
    }
    
    public abstract static class NoFixedFacet extends FacetElement
    {
        /** List of allowed attribute names (including "id" from base). */
        public static final StringArray s_allowedAttributes = new StringArray(new String[] { "value" },
            AnnotatedBase.s_allowedAttributes);
        
        //
        // Instance data
        
        /** "value" attribute value. */
        private String m_value;
        
        /**
         * Constructor. Just passes on the element type to base class.
         * 
         * @param type
         * @param bit mask
         * @param exclude exclusion bit mask
         */
        public NoFixedFacet(int type, int bit, int exclude) {
            super(type, bit, exclude);
        }
        
        /**
         * Get "value" attribute value.
         * 
         * @return value attribute value
         */
        public String getValue() {
            return m_value;
        }
        
        /**
         * Set "value" attribute value.
         * 
         * @param value value attribute value
         */
        public void setValue(String value) {
            m_value = value;
        }
        
        //
        // Validation methods
        
        /**
         * Make sure all attributes are defined.
         * 
         * @param uctx unmarshalling context
         * @exception JiBXException on unmarshalling error
         */
        protected void preset(IUnmarshallingContext uctx) throws JiBXException {
            validateAttributes(uctx, s_allowedAttributes);
            super.preset(uctx);
        }
    }
    
    public static class Enumeration extends NoFixedFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public Enumeration() {
            super(ENUMERATION_TYPE, ENUMERATION_FACET_BIT, 0);
        }
    }
    
    public static class Pattern extends NoFixedFacet
    {
        /**
         * Constructor. Just sets element type in base class.
         */
        public Pattern() {
            super(PATTERN_TYPE, PATTERN_FACET_BIT, 0);
        }
    }
}
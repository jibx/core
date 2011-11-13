/*
Copyright (c) 2006-2009, Dennis M. Sosnoski.
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

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.schema.support.QNameConverter;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * <b>union</b> element definition.
 *
 * @author Dennis M. Sosnoski
 */
public class UnionElement extends AnnotatedBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "memberTypes" },
        AnnotatedBase.s_allowedAttributes);
    
    /** Mask bits for inline base type definition. */
    private long INLINE_TYPE_MASK = ELEMENT_MASKS[SIMPLETYPE_TYPE];
    
    //
    // Instance data
    
    /** Filtered list of inline base type definition elements. */
    private final FilteredSegmentList m_inlineBaseList;
    
    /** 'memberTypes' attribute value. */
    private QName[] m_memberTypes;
    
    /** Actual definitions corresponding to 'memberTypes' attribute values (set during validation). */
    private CommonTypeDefinition[] m_memberTypeDefinitions;
    
    /**
     * Constructor.
     */
    public UnionElement() {
        super(UNION_TYPE);
        m_inlineBaseList = new FilteredSegmentList(getChildrenWritable(),
            INLINE_TYPE_MASK, this);
    }
    
    //
    // Base class overrides
    
    /* (non-Javadoc)
     * @see org.jibx.binding.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
    
    //
    // Accessor methods
    
    /**
     * Get 'memberTypes' attribute value.
     * 
     * @return attribute value, or <code>null</code> if none
     */
    public QName[] getMemberTypes() {
        return m_memberTypes;
    }

    /**
     * Set 'memberTypes' attribute value.
     * 
     * @param bases member types, or <code>null</code> if none
     */
    public void setMemberTypes(QName[] bases) {
        m_memberTypes = bases;
    }
    
    /**
     * Get referenced member type definitions. This method can only be called after validation.
     *
     * @return member types, or <code>null</code> if none
     */
    public CommonTypeDefinition[] getMemberTypeDefinitions() {
        return m_memberTypeDefinitions;
    }

    /**
     * Get list of inline member type definitions.
     *
     * @return inline types
     */
    public FilteredSegmentList getInlineBaseList() {
        return m_inlineBaseList;
    }
    
    //
    // Validation methods

    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // check for missing or conflicting type information
        if (m_inlineBaseList.size() == 0 && m_memberTypes == null) {
            vctx.addError("Must have 'memberTypes' attribute or inline <simpleType> definitions", this);
        }
        
        // patch qualified names with schema effective namespace
        if (m_memberTypes != null) {
            SchemaElement schema = vctx.getCurrentSchema();
            String ens = schema.getEffectiveNamespace();
            for (int i = 0; i < m_memberTypes.length; i++) {
                QNameConverter.patchQNameNamespace(ens, m_memberTypes[i]);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // look up referenced type definitions
        if (m_memberTypes != null) {
            m_memberTypeDefinitions = new CommonTypeDefinition[m_memberTypes.length];
            for (int i = 0; i < m_memberTypes.length; i++) {
                QName tname = m_memberTypes[i];
                CommonTypeDefinition def = vctx.findType(tname);
                m_memberTypeDefinitions[i] = def;
                if (def == null) {
                    vctx.addFatal("Referenced type '" + tname + "' is not defined", this);
                }
            }
        }

        // continue with parent class validation
        super.validate(vctx);
    }
}
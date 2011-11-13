/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski. All rights reserved.
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
 * <b>list</b> element definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class ListElement extends AnnotatedBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "itemType" }, AnnotatedBase.s_allowedAttributes);
    
    /** Mask bits for inline base type definition. */
    private long INLINE_TYPE_MASK = ELEMENT_MASKS[SIMPLETYPE_TYPE];
    
    //
    // Instance data
    
    /** Filtered list of inline base type definition element. */
    private final FilteredSegmentList m_inlineBaseList;
    
    /** 'itemType' attribute value. */
    private QName m_itemType;
    
    /** Actual definition corresponding to 'itemType' attribute value (set during validation). */
    private CommonTypeDefinition m_itemTypeDefinition;
    
    /**
     * Constructor.
     */
    public ListElement() {
        super(LIST_TYPE);
        m_inlineBaseList = new FilteredSegmentList(getChildrenWritable(), INLINE_TYPE_MASK, this);
    }
    
    //
    // Base class overrides
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
    
    //
    // Accessor methods
    
    /**
     * Get 'itemType' attribute value.
     * 
     * @return attribute value, or <code>null</code> if none
     */
    public QName getItemType() {
        return m_itemType;
    }
    
    /**
     * Set 'itemType' attribute value.
     * 
     * @param base attribute value, or <code>null</code> if none
     */
    public void setItemType(QName base) {
        m_itemType = base;
    }
    
    /**
     * Get referenced item type definition. This method can only be called after validation.
     * 
     * @return item type, or <code>null</code> if none
     */
    public CommonTypeDefinition getItemTypeDefinition() {
        return m_itemTypeDefinition;
    }
    
    /**
     * Get inline base type definition element.
     * 
     * @return inline base type, or <code>null</code> if none
     */
    public SimpleTypeElement getDerivation() {
        return m_inlineBaseList.size() > 0 ? (SimpleTypeElement)m_inlineBaseList.get(0) : null;
    }
    
    /**
     * Set inline base type definition element.
     * 
     * @param element inline base type, or <code>null</code> if unsetting
     */
    public void setDerivation(SimpleTypeElement element) {
        m_inlineBaseList.clear();
        if (element != null) {
            m_inlineBaseList.add(element);
        }
    }
    
    //
    // Validation methods
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // check for missing or conflicting type information
        if (m_inlineBaseList.size() == 0 && m_itemType == null) {
            vctx.addError("Must have 'itemType' attribute or inline <simpleType> definition", this);
        }
        if (m_inlineBaseList.size() > 0 && m_itemType != null) {
            vctx.addError("Can only use 'itemType' attribute without inline <simpleType> definition", this);
        }
        if (m_inlineBaseList.size() > 1) {
            vctx.addError("Only one inline <simpleType> definition allowed", this);
        }
        
        // patch qualified name with schema effective namespace
        SchemaElement schema = vctx.getCurrentSchema();
        String ens = schema.getEffectiveNamespace();
        QNameConverter.patchQNameNamespace(ens, m_itemType);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // look up referenced type definition
        if (m_itemType != null) {
            m_itemTypeDefinition = vctx.findType(m_itemType);
            if (m_itemTypeDefinition == null) {
                vctx.addFatal("Referenced type '" + m_itemType + "' is not defined", this);
            }
        }
        
        // continue with parent class validation
        super.validate(vctx);
    }
}
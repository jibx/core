/*
Copyright (c) 2004-2008, Dennis M. Sosnoski
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

import java.util.ArrayList;
import java.util.Iterator;

import org.jibx.util.StringArray;

/**
 * Model component for elements that can contain other component elements.
 * TODO: The list of child elements here conflicts with that in BindingElement;
 * should change the type hierarchy to better reflect usage
 *
 * @author Dennis M. Sosnoski
 */
public abstract class NestingElementBase extends ElementBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "value-style" });
    
    /** Value style attribute information. */
    private NestingAttributes m_nestingAttrs;
    
    /** Definition context for this nesting (created by validation). */
    private DefinitionContext m_defContext;
    
    /** List of child elements. */
    private ArrayList m_children;
	
	/**
	 * Constructor.
     * 
     * @param type element type code
	 */
    protected NestingElementBase(int type) {
        super(type);
        m_nestingAttrs = new NestingAttributes();
        m_children = new ArrayList();
	}
    
    /**
     * Add child element.
     * TODO: should be ElementBase argument, but JiBX doesn't allow yet
     * 
     * @param child element to be added as child of this element
     */
    public final void addChild(Object child) {
        m_children.add(child);
    }
    
    /**
     * Get list of child elements.
     * 
     * @return list of child elements (never <code>null</code>)
     */
    public final ArrayList children() {
        return m_children;
    }
    
    /**
     * Get iterator for child elements.
     * 
     * @return iterator for child elements
     */
    public final Iterator childIterator() {
        return m_children.iterator();
    }
    
    /**
     * Get definition context. This method may only be called after validation.
     * 
     * @return definition context, or <code>null</code> if no definition context
     * for this element
     */
    public final DefinitionContext getDefinitions() {
        return m_defContext;
    }

    /**
     * Set definition context.
     * 
     * @param ctx definition context to be set
     */
    /*package*/ void setDefinitions(DefinitionContext ctx) {
        m_defContext = ctx;
    }
    
    //
    // Nesting attribute delegate methods
    
    /**
     * Get style name set on this nesting element.
     * 
     * @return style string value (<code>null</code> if undefined at this level)
     */
    public String getStyleName() {
        return m_nestingAttrs.getStyleName();
    }
    
    /**
     * Get style value set on this nesting element. This call is only meaningful
     * after validation.
     * 
     * @return style value (<code>-1</code> if undefined at this level)
     */
    public int getStyle() {
        return m_nestingAttrs.getStyle();
    }
    
    /**
     * Set style name on this nesting element.
     * 
     * @param name style name (<code>null</code> to undefine style at this
     * level)
     */
    public void setStyleName(String name) {
        m_nestingAttrs.setStyleName(name);
    }
    
    /**
     * Get default style value for child components. This call is only
     * meaningful after validation.
     * 
     * @return default style value for child components (<code>-1</code> if not
     * defined at this level)
     */
    public int getDefaultStyle() {
        return m_nestingAttrs.getStyle();
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        m_nestingAttrs.prevalidate(vctx);
        super.prevalidate(vctx);
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        if (m_defContext == null) {
            m_defContext = vctx.getCurrentDefinitions();
        }
        m_nestingAttrs.validate(vctx);
        super.validate(vctx);
    }
}
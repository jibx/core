/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen.custom;

import java.util.Iterator;
import java.util.List;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.QName;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaPath;
import org.jibx.schema.support.Conversions;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.LazyList;

/**
 * Class for all schema component elements, with the exception of the &lt;schema> element itself. Almost all of these
 * schema elements can contain other elements, so this extends the nesting base to handle inherited values.
 * 
 * @author Dennis M. Sosnoski
 */
public class ComponentCustom extends NestingCustomBase
{
    //
    // Value set information
    
    public static final int INLINE_DEFAULT = 0;
    public static final int INLINE_BLOCK = 1;
    public static final int INLINE_PREFER = 2;
    
    public static final EnumSet s_inlineValues = new EnumSet(INLINE_DEFAULT,
        new String[] { "default", "block", "prefer"});
    
    //
    // Fixed fields.
    
    /** Schema element name. */
    private final String m_elementName;
    
    //
    // Bound fields.
    
    /** Path to component (<code>null</code> if not specified). */
    private String m_path;
    
    /** Component position in siblings of same type (<code>null</code> if not specified). */
    private String m_position;
    
    /** Component name, if relevant. */
    private String m_componentName;
    
    /** Ignore component flag. */
    private boolean m_ignore;
    
    /** Exclude component flag. */
    private boolean m_exclude;
    
    /** Code for inlining. */
    private int m_inline = INLINE_DEFAULT;
    
    /** Corresponding generated class name (<code>null</code> if not specified). */
    private String m_className;
    
    /** Base name for corresponding property in generated code (<code>null</code> if not specified). */
    private String m_baseName;
    
    /** Actual type to be used. */
    private QName m_type;
    
    /**
     * Constructor.
     * 
     * @param name schema element name
     * @param parent
     */
    public ComponentCustom(String name, NestingCustomBase parent) {
        super(parent);
        m_elementName = name;
    }
    
    /**
     * Get the schema element name for the component.
     *
     * @return name
     */
    public final String getElementName() {
        return m_elementName;
    }

    /**
     * Build the schema path for this customization.
     *
     * @param vctx validation context
     * @return path constructed path, or <code>null</code> if error
     */
    public final SchemaPath buildPath(ValidationContext vctx) {
        return SchemaPath.buildPath(m_path, getElementName(), m_componentName, m_position, this, vctx);
    }
    
    /**
     * Check if schema component is to be ignored (allowed, but not processed, in unmarshalling). This is only
     * applicable to element definitions.
     *
     * @return <code>true</code> if ignored, <code>false</code> if not
     */
    public boolean isIgnored() {
        return m_ignore;
    }
    
    /**
     * Check if schema component is to be excluded.
     *
     * @return <code>true</code> if ignored, <code>false</code> if not
     */
    public boolean isExcluded() {
        return m_exclude;
    }
    
    /**
     * Check if schema component is to be generated inline.
     *
     * @return <code>true</code> if inlined, <code>false</code> if not
     */
    public boolean isInlined() {
        return m_inline == INLINE_PREFER;
    }
    
    /**
     * Check if schema component is to be generated as a separate class.
     *
     * @return <code>true</code> if separate class, <code>false</code> if not
     */
    public boolean isSeparateClass() {
        return m_inline == INLINE_BLOCK;
    }
    
    /**
     * Get name to be used for generated class.
     *
     * @return class name (<code>null</code> if not set)
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Get base name for corresponding property.
     *
     * @return property name (<code>null</code> if not set)
     */
    public String getBaseName() {
        return m_baseName;
    }
    
    /**
     * Set the inline text value. This method is provided only for use when unmarshalling.
     * 
     * @param text (<code>null</code> if not set)
     * @param ictx
     */
    private void setInline(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            m_inline = Conversions.convertEnumeration(text, s_inlineValues, "inline", ictx);
        }
    }

    /**
     * Apply customizations to a schema extension. This also finds matches for any child customizations, and applies
     * the child customizations recursively. The method may be called multiple times for different component extensions,
     * so it must not modify the customization information itself.
     *
     * @param exten target schema extension
     * @param vctx validation context
     */
    public final void apply(ComponentExtension exten, ValidationContext vctx) {
        if (validate(vctx)) {
            exten.setCustom(this);
            if (isExcluded()) {
                
                // flag component removed from schema
                exten.setRemoved(true);
                
                // warn that any child customizations are ignored
                if (getChildren().size() > 0) {
                    vctx.addWarning("Child customizations ignored for skipped component", this);
                }
                
            } else {
                
                // apply customizations to extension
                exten.setTypeReplacer(this);
                exten.setOverrideType(m_type);
                
                // match and apply child customizations
                LazyList childs = getChildren();
                for (int i = 0; i < childs.size(); i++) {
                    ComponentCustom child = (ComponentCustom)childs.get(i);
                    SchemaPath path = child.buildPath(vctx);
                    if (path != null) {
                        List matches = path.partialMatchMultiple(0, path.getPathLength()-1, exten.getComponent());
                        if (matches.size() == 0) {
                            vctx.addWarning("No matches found for customization expression", child);
                        } else {
                            for (Iterator iter = matches.iterator(); iter.hasNext();) {
                                OpenAttrBase target = (OpenAttrBase)iter.next();
                                child.apply((ComponentExtension)target.getExtension(), vctx);
                            }
                        }
                    }
                }
            }
        }
    }
}
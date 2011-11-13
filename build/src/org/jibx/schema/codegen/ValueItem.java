/*
 * Copyright (c) 2007-2008, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen;

import org.jibx.runtime.QName;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.elements.AnnotatedBase;

/**
 * Information for an item of a predefined type to be included in code generated from schema.
 * 
 * @author Dennis M. Sosnoski
 */
public class ValueItem extends Item
{
    /** Predefined type reference. */
    private final JavaType m_type;
    
    /** Original schema type. */
    private final QName m_schemaType;

    /** Attribute data present flag. */
    private boolean m_attributePresent;

    /** Element data present flag. */
    private boolean m_elementPresent;

    /** Character data content data present flag. */
    private boolean m_contentPresent;
    
    /**
     * Copy constructor. This creates a copy with a new parent.
     * 
     * @param original
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent
     */
    private ValueItem(ValueItem original, Item ref, GroupItem parent) {
        super(original, ref, original.getComponentExtension(), parent);
        if (isOptional() || isCollection()) {
            throw new IllegalStateException("Internal error - value item should never be repeating or optional");
        }
        m_type = original.m_type;
        m_schemaType = original.m_schemaType;
    }
    
    /**
     * Constructor.
     * 
     * @param comp schema component extension
     * @param type schema type name
     * @param ref schema type equivalent (<code>null</code> if not appropriate)
     * @param parent containing structure (<code>null</code> if a top-level structure)
     */
    /*package*/ ValueItem(AnnotatedBase comp, QName type, JavaType ref, GroupItem parent) {
        super(comp, parent);
        m_type = ref;
        m_schemaType = type;
    }
    
    /**
     * Get the simple type for this value.
     *
     * @return type
     */
    public JavaType getType() {
        return m_type;
    }
    
    /**
     * Get schema type name.
     *
     * @return name
     */
    public QName getSchemaType() {
        return m_schemaType;
    }

    /**
     * Copy the item under a different parent.
     *
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent
     * @return copy
     */
    protected Item copy(Item ref, GroupItem parent) {
        return new ValueItem(this, ref, parent);
    }
    
    /**
     * Build a description of the item.
     *
     * @param depth current nesting depth
     * @param classified include classification details flag
     * @return description
     */
    protected String describe(int depth, boolean classified) {
        StringBuffer buff = new StringBuffer(depth + 50);
        buff.append(leadString(depth));
        buff.append("value of type ");
        buff.append(m_type.getClassName());
        if (m_type.getPrimitiveName() != null) {
            buff.append(" (");
            buff.append(m_type.getPrimitiveName());
            buff.append(")");
        }
        buff.append(" with value name ");
        buff.append(getName());
        buff.append(": ");
        buff.append(SchemaUtils.describeComponent(getSchemaComponent()));
        buff.append('\n');
        return buff.toString();
    }

    /**
     * Check if an attribute is part of this item. This is only <code>true</code> for items corresponding to attribute
     * definitions, and groupings including these items which do not define an element name.
     *
     * @return <code>true</code> if attribute
     */
    public boolean isAttributePresent() {
        return m_attributePresent;
    }

    /**
     * Check if a child elements is part of this item. This is <code>true</code> for all items corresponding to element
     * definitions, and all groupings which include such an item. 
     *
     * @return <code>true</code> if content
     */
    public boolean isElementPresent() {
        return m_elementPresent;
    }

    /**
     * Check if character data content is part of this item. This is <code>true</code> for all items corresponding to
     * simpleContent definitions, and all groupings which include such an item. 
     *
     * @return <code>true</code> if content
     */
    public boolean isContentPresent() {
        return m_contentPresent;
    }
}
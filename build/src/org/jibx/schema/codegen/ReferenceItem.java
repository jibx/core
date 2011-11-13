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

package org.jibx.schema.codegen;

import org.apache.log4j.Logger;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.codegen.custom.ComponentExtension;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.SchemaBase;

/**
 * Information for a reference to a global definition. The reference may be replaced with an inlined copy of the
 * definition during code generation.
 * 
 * @author Dennis M. Sosnoski
 */
public class ReferenceItem extends Item
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(GroupItem.class.getName());
    
    /** Referenced type structure definition. */
    private final DefinitionItem m_definition;
    
    /**
     * Copy constructor.
     * 
     * @param original
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent
     */
    private ReferenceItem(ReferenceItem original, Item ref, GroupItem parent) {
        super(original, ref, original.getComponentExtension(), parent);
        m_definition = original.m_definition;
    }
    
    /**
     * Internal constructor for direct reference.
     * 
     * @param comp schema component
     * @param parent containing structure (<code>null</code> if a top-level structure)
     * @param def referenced definition
     */
    /*package*/ ReferenceItem(AnnotatedBase comp, GroupItem parent, DefinitionItem def) {
        super(comp, parent);
        m_definition = def;
        def.countReference();
    }
    
    /**
     * Internal constructor for converting group to reference. This is used when an embedded group is converted to a
     * separate definition, as needed for class reuse.
     * 
     * @param group
     * @param def
     */
    /*package*/ ReferenceItem(GroupItem group, DefinitionItem def) {
        super(group, null, group.getComponentExtension(), group.getParent());
        m_definition = def;
        def.countReference();
    }
    
    /**
     * Get the referenced structure.
     *
     * @return reference
     */
    public DefinitionItem getDefinition() {
        return m_definition;
    }
    
    /**
     * Inline the referenced structure. This replaces the reference with a deep copy of the definition, copying the
     * reference name and optional/repeated information over to the definition.
     * 
     * @return replacement group
     */
    public Item inlineReference() {
// This should not be necessary any more, since the group handling is supposed to give equivalent structure
//        int type = comp.type();
//        if (m_definition.getChildCount() == 1 &&
//            (type == SchemaBase.ELEMENT_TYPE || type == SchemaBase.ATTRIBUTE_TYPE)) {
//            
//            // inlining attribute or element just substitutes the definition for the reference
//            item = m_definition.getFirstChild().copy(this, getParent());
//            if (s_logger.isDebugEnabled()) {
//                s_logger.debug("Inlining reference to " + SchemaUtils.describeComponent(comp) +
//                    " using direct substitution");
//            }
//            
//        } else {
//      }
        
        // decide which component to link to inlined group based on reference component type
        AnnotatedBase refcomp = getSchemaComponent();
        int type = refcomp.type();
        ComponentExtension ext = (type == SchemaBase.ATTRIBUTE_TYPE || type == SchemaBase.ELEMENT_TYPE) ?
            getComponentExtension() : m_definition.getComponentExtension();
        
        // create new group from definition group
        GroupItem group = new GroupItem(this, ext);
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Inlining reference to " + SchemaUtils.describeComponent(m_definition.getSchemaComponent()) +
                " from " + SchemaUtils.describeComponent(refcomp) + " as new group");
        }
        
        // replace this reference with the inlined group
        getParent().replaceChild(this, group);
        return group;
    }
    
    /**
     * Copy the item under a different parent.
     *
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent
     * @return copy
     */
    protected Item copy(Item ref, GroupItem parent) {
        return new ReferenceItem(this, ref, parent);
    }
    
    /**
     * Classify the content of this item as attribute, element, and/or character data content, and as requiring content
     * of some form if appropriate. If this is an element or attribute reference the actual referencing component is
     * used for classification purposes, since that will determine the classification. Otherwise, the actual definition
     * is used as the source of the information.
     */
    protected void classifyContent() {
        int type = getSchemaComponent().type();
        if (type == SchemaBase.ATTRIBUTE_TYPE || type == SchemaBase.ELEMENT_TYPE) {
            
            // just use the actual component for classification
            super.classifyContent();
            
        } else {
            
            // find the parent group with a different schema component
            GroupItem parent = findDisjointParent();
            if (parent != null) {
                
                // assume the characteristics of the referenced definition
                m_definition.classifyContent();
                if (m_definition.isAttributePresent()) {
                    parent.forceAttributePresent();
                }
                if (m_definition.isElementPresent()) {
                    parent.forceElementPresent();
                }
                if (m_definition.isContentPresent()) {
                    parent.forceContentPresent();
                }
                if (!m_definition.isAllOptional()) {
                    parent.forceRequiredPresent();
                }
            }
        }
    }

    /**
     * Build a description of the reference.
     *
     * @param depth current nesting depth
     * @param classified include classification details flag
     * @return description
     */
    protected String describe(int depth, boolean classified) {
        StringBuffer buff = new StringBuffer(depth + 50);
        buff.append(leadString(depth));
        buff.append("reference to ");
        if (m_definition.isInline()) {
            buff.append("inlined ");
        }
        buff.append(SchemaUtils.describeComponent(m_definition.getSchemaComponent()));
        buff.append(" with value name ");
        buff.append(getName());
        buff.append(": ");
        buff.append(SchemaUtils.describeComponent(getSchemaComponent()));
        buff.append('\n');
        return buff.toString();
    }
}
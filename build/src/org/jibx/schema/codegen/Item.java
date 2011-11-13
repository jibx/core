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

import org.jibx.schema.SchemaUtils;
import org.jibx.schema.codegen.custom.ComponentExtension;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.CommonTypeDerivation;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.SchemaBase;

/**
 * Base class for code generation items. Each instance corresponds to a particular schema component, and this base class
 * tracks that schema component (by way of the extension information), along with related details and linkage
 * information. The linkage uses embedded list links, which allows replacing one instance with another with minimal
 * overhead.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class Item
{
    /** Corresponding schema component extension. */
    private final ComponentExtension m_componentExtension;
    
    /** Flag for topmost item associated with a particular schema component. */
    private final boolean m_topmost;
    
    /** Flag for an optional item. In cases where multiple items are associated with the same schema component, this
     is only meaningful for the topmost item. */
    private final boolean m_optional;
    
    /** Flag for a collection item. In cases where multiple items are associated with the same schema component, this
     is only meaningful for the topmost item. */
    private final boolean m_collection;
    
    /** Flag for a nillable item. In cases where multiple items are associated with the same schema component, this
     is only meaningful for the topmost item. */
    private final boolean m_nillable;
    
    /** Item is handled by subclassing flag. */
    private boolean m_implicit;
    
    /** Containing group item. */
    private GroupItem m_parent;
    
    /** Next item in list (<code>null</code> if none). */
    protected Item m_next;
    
    /** Preceding item in list (<code>null</code> if none). */
    protected Item m_last;
    
    /** Actual name to be used for item (<code>null</code> if to be inherited). */
    private String m_name;
    
    /**
     * Basic constructor. This uses the schema component to determine all information other than the parent item group,
     * including the optional/nillable/collection flags. As a special case, if the parent group is associated with the
     * same component this sets all three of these flags <code>false</code> to avoid redundant handling.
     * 
     * @param comp schema component
     * @param parent containing group (<code>null</code> if a top-level group)
     */
    protected Item(AnnotatedBase comp, GroupItem parent) {
        
        // save basic values
        m_componentExtension = (ComponentExtension)comp.getExtension();
        m_parent = parent;
        
        // set other values based on extension and component information
        if (parent == null || parent.getSchemaComponent() != comp) {
            m_topmost = true;
            m_optional = m_componentExtension.isOptional();
            m_collection = m_componentExtension.isRepeated();
            m_nillable = comp.type() == SchemaBase.ELEMENT_TYPE && ((ElementElement)comp).isNillable();
        } else {
            m_topmost = m_optional = m_collection = m_nillable = false;
        }
        m_name = m_componentExtension.getBaseName();
    }
    
    /**
     * Copy constructor. This creates a copy with a new parent.
     * 
     * @param original
     * @param ref reference (for name override; <code>null</code> if none)
     * @param ext component extension to be linked with copy
     * @param parent (non-<code>null</code>)
     */
    protected Item(Item original, Item ref, ComponentExtension ext, GroupItem parent) {
        m_componentExtension = ext;
        m_parent = parent;
        if (parent == null || parent.getSchemaComponent() != m_componentExtension.getComponent()) {
            m_topmost = true;
            Item top = original.getTopmost();
            m_optional = top.m_optional;
            m_collection = top.m_collection;
            m_nillable = top.m_nillable;
        } else {
            m_topmost = m_optional = m_collection = m_nillable = false;
        }
        if (ref == null || original.isFixedName()) {
            m_name = original.m_name;
        } else {
            m_name = ref.m_name;
        }
    }

    /**
     * Replace the parent for this item.
     *
     * @param parent
     */
    protected void reparent(GroupItem parent) {
        m_parent = parent;
    }
    
    /**
     * Get schema component corresponding to this item.
     * 
     * @return schema component
     */
    public AnnotatedBase getSchemaComponent() {
        return (AnnotatedBase)m_componentExtension.getComponent();
    }
    
    /**
     * Get schema component annotation corresponding to this item.
     * 
     * @return schema component
     */
    public ComponentExtension getComponentExtension() {
        return m_componentExtension;
    }

    /**
     * Get containing group item.
     *
     * @return group (<code>null</code> if a top-level group)
     */
    public GroupItem getParent() {
        return m_parent;
    }
    
    /**
     * Check if the name is fixed by configuration.
     *
     * @return <code>true</code> if fixed, <code>false</code> if not
     */
    public boolean isFixedName() {
        return m_componentExtension.getBaseName() != null;
    }

    /**
     * Get effective item name, applying inheritance if necessary.
     * 
     * @return name
     */
    public String getEffectiveName() {
        Item item = this;
        while (item.m_name == null) {
            item = item.m_parent;
            if (item == null) {
                throw new IllegalStateException("Inherited name with nothing to inherit");
            }
        }
        return item.m_name;
    }

    /**
     * Get name set directly for this item.
     * 
     * @return name (<code>null</code> if to be inherited)
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Set name directly for this item. It is an error to call this method if the name is fixed.
     * 
     * @param name (<code>null</code> if to be inherited)
     */
    public void setName(String name) {
        if (isFixedName()) {
            throw new IllegalStateException("Internal error - attempt to change configured name");
        } else {
            m_name = name;
        }
    }
    
    /**
     * Get next item in list.
     *
     * @return next
     */
    public Item getNext() {
        return m_next;
    }

    /**
     * Check if topmost item for a particular schema component. The methods {@link #isCollection()},
     * {@link #isOptional()}, {@link GroupItem#isAllOptional()}, {@link GroupItem#isAttributePresent()},
     * {@link GroupItem#isContentPresent()}, and {@link GroupItem#isElementPresent()} are all only meaningful for the
     * topmost item associated with a schema component.
     *
     * @return topmost
     */
    public boolean isTopmost() {
        return m_topmost;
    }
    
    /**
     * Get the topmost item associated with the same schema component as this item.  The methods {@link
     * #isCollection()}, {@link #isOptional()}, {@link GroupItem#isAllOptional()}, {@link
     * GroupItem#isAttributePresent()}, {@link GroupItem#isContentPresent()}, and {@link GroupItem#isElementPresent()}
     * are all only meaningful for the topmost item associated with a schema component.
     *
     * @return topmost
     */
    public Item getTopmost() {
        Item top = this;
        while (!top.isTopmost()) {
            top = top.m_parent;
        }
        return top;
    }

    /**
     * Check if item is optional. This method is only meaningful for the topmost item associated with a particular
     * schema component (those for which {@link #isTopmost()} returns <code>true</code>).
     * 
     * @return optional
     */
    public boolean isOptional() {
        return m_optional;
    }

    /**
     * Check if item is ignored. This method is only meaningful for the topmost item associated with a particular
     * schema component (those for which {@link #isTopmost()} returns <code>true</code>).
     * 
     * @return optional
     */
    public boolean isIgnored() {
        return getComponentExtension().isIgnored();
    }

    /**
     * Check if a collection item. This method is only meaningful for the topmost item associated with a particular
     * schema component (those for which {@link #isTopmost()} returns <code>true</code>).
     * 
     * @return <code>true</code> if collection
     */
    public boolean isCollection() {
        return m_collection;
    }

    /**
     * Check if the item is represented implicitly by subclassing.
     *
     * @return implicit
     */
    public boolean isImplicit() {
        return m_implicit;
    }

    /**
     * Set item represented implicitly by subclassing flag.
     *
     * @param implicit
     */
    public void setImplicit(boolean implicit) {
        m_implicit = implicit;
    }

    /**
     * Copy the item under a different parent. This is intended for replacing a reference with a copy, and allows the
     * reference to override settings from the original.
     *
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent
     * @return copy
     */
    protected abstract Item copy(Item ref, GroupItem parent);
    
    /**
     * Find the nearest ancestor group which relates to a different schema component.
     *
     * @return ancestor with different schema component, or <code>null</code> if none
     */
    protected GroupItem findDisjointParent() {
        AnnotatedBase comp = getSchemaComponent();
        GroupItem parent = getParent();
        while (parent != null && parent.getSchemaComponent() == comp) {
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * Classify the content of this item as attribute, element, and/or character data content, and as requiring content
     * of some form if appropriate. This needs to be done as a separate step after construction in order to handle
     * references, which must assume the content of the definition, and also to work after inlining. This base class
     * implementation does the classification based solely on the schema component type. Any subclasses which override
     * this method should generally call the base class implementation before doing their own classification handling,
     * unless they use a substitute component.
     */
    protected void classifyContent() {
        if (m_topmost) {
            
            // find the parent group with a different schema component
            GroupItem parent = findDisjointParent();
            if (parent != null) {
                
                // flag content type for that parent group
                AnnotatedBase comp = getSchemaComponent();
                switch (comp.type()) {
                    
                    case SchemaBase.ANY_TYPE:
                    case SchemaBase.ELEMENT_TYPE:
                        
                        // set required component present if appropriate
                        if (!m_optional) {
                            parent.forceRequiredPresent();
                        }
                        // fall through for common handling
                        
                    case SchemaBase.GROUP_TYPE:
                        parent.forceElementPresent();
                        break;
                        
                    case SchemaBase.EXTENSION_TYPE:
                        CommonTypeDerivation deriv = (CommonTypeDerivation)comp;
                        if (!deriv.isComplexType()) {
                            parent.forceContentPresent();
                            parent.forceRequiredPresent();
                        }
                        break;
                    
                    case SchemaBase.ANYATTRIBUTE_TYPE:
                    case SchemaBase.ATTRIBUTE_TYPE:
                        
                        // set required component present if appropriate
                        if (!m_optional) {
                            parent.forceRequiredPresent();
                        }
                        // fall through for common handling
                        
                    case SchemaBase.ATTRIBUTEGROUP_TYPE:
                        parent.forceAttributePresent();
                        break;
                        
                }
            }
        }
    }

    /**
     * Generate a description of the item. For items with nested items this will show the complete structure.
     *
     * @param depth current nesting depth
     * @param classified include classification details flag
     * @return description
     */
    protected abstract String describe(int depth, boolean classified);
    
    /**
     * Generate the standard leading text for description of the item.
     *
     * @param depth current nesting depth
     * @return leading text for description
     */
    protected String leadString(int depth) {
        StringBuffer buff = new StringBuffer(SchemaUtils.getIndentation(depth));
        if (isOptional()) {
            buff.append("optional ");
        }
        if (isCollection()) {
            buff.append("repeating ");
        }
        return buff.toString();
    }
}
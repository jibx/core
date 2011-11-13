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
import org.jibx.runtime.QName;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.codegen.custom.ComponentExtension;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.ComplexExtensionElement;
import org.jibx.schema.elements.SchemaBase;

/**
 * Information for a grouping of components (attributes, elements, compositors, and/or wildcards). This is used for
 * both local groupings and global definitions.
 * 
 * @author Dennis M. Sosnoski
 */
public class GroupItem extends Item
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(GroupItem.class.getName());
    
    /** Flag for enumeration value. */
    private boolean m_enumeration;
    
    /** Inline references to this structure. */
    private boolean m_inline;
    
    /** Name to be used for generated class (<code>null</code> if inherited). */
    private String m_className;
    
    /** Number of child items in group. */
    private int m_size;
    
    /** First child (<code>null</code> if none). */
    private Item m_head;
    
    /** Last child (<code>null</code> if none). */
    private Item m_tail;

    /** Generated class information (<code>null</code> if inlined). */
    private TypeData m_generateClass;

    /** Flag for all child nodes are optional. In cases where multiple items are associated with the same schema
     component, this is only meaningful for the topmost item. */
    private boolean m_allOptional;

    /** Attribute data present flag. */
    private boolean m_attributePresent;

    /** Element data present flag. */
    private boolean m_elementPresent;

    /** Character data content data present flag. */
    private boolean m_contentPresent;
    
    /**
     * Internal constructor. This is used both for creating a new child group directly, and by the {@link
     * DefinitionItem} subclass.
     * 
     * @param comp schema component (should be the simpleType component in the case of an enumeration)
     * @param parent (<code>null</code> if none)
     */
    protected GroupItem(AnnotatedBase comp, GroupItem parent) {
        super(comp, parent);
        ComponentExtension exten = getComponentExtension();
        m_className = exten.getClassName();
        m_allOptional = true;
    }

    /**
     * Copy constructor. This creates a deep copy with a new parent.
     * 
     * @param original
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent (non-<code>null</code>)
     */
    /*package*/ GroupItem(GroupItem original, Item ref, GroupItem parent) {
        super(original, ref, original.getComponentExtension(), parent);
        m_enumeration = original.m_enumeration;
        for (Item child = original.getFirstChild(); child != null; child = child.getNext()) {
            appendChild(child.copy(null, this));
        }
        m_inline = original.m_inline;
        m_className = original.m_className;
        if (parent == null || parent.getSchemaComponent() != getSchemaComponent()) {
            GroupItem top = (GroupItem)original.getTopmost();
            m_allOptional = top.m_allOptional;
            m_attributePresent = top.m_attributePresent;
            m_elementPresent = top.m_elementPresent;
            m_contentPresent = top.m_contentPresent;
        }
    }
    
    /**
     * Constructor from a reference. This is only used for inlining a referenced definition. It merges usage information
     * from the reference with a deep copy of the item structure of the definition.
     * 
     * @param reference
     * @param ext component extension to be linked with inlined definition
     */
    /*package*/ GroupItem(ReferenceItem reference, ComponentExtension ext) {
        super(reference, reference, ext, reference.getParent());
        m_inline = true;
        DefinitionItem definition = reference.getDefinition();
        int type = reference.getSchemaComponent().type();
        if (type == SchemaBase.ATTRIBUTEGROUP_TYPE || type == SchemaBase.GROUP_TYPE) {
            m_allOptional = definition.isAllOptional();
        }
        m_attributePresent = definition.isAttributePresent();
        m_elementPresent = definition.isElementPresent();
        m_contentPresent = definition.isContentPresent();
        appendChild(new GroupItem(definition, reference, this));
    }

    /**
     * Check if this value represents an enumeration.
     *
     * @return enumeration
     */
    public boolean isEnumeration() {
        return m_enumeration;
    }
    
    /**
     * Set value represents an enumeration flag.
     *
     * @param enumeration
     */
    public void setEnumeration(boolean enumeration) {
        m_enumeration = enumeration;
    }

    /**
     * Append an item to the list of children.
     *
     * @param item
     */
    private void appendChild(Item item) {
        if (m_head == null) {
            m_head = m_tail = item;
        } else {
            m_tail.m_next = item;
            item.m_last = m_tail;
            m_tail = item;
        }
        m_size++;
    }

    /**
     * Add a child grouping structure.
     *
     * @param comp schema component
     * @return structure
     */
    public GroupItem addGroup(AnnotatedBase comp) {
        GroupItem group = new GroupItem(comp, this);
        appendChild(group);
        return group;
    }
    
    /**
     * Add a child reference structure.
     *
     * @param comp schema component
     * @param ref referenced definition item
     * @return reference
     */
    public ReferenceItem addReference(AnnotatedBase comp, DefinitionItem ref) {
        ReferenceItem reference = new ReferenceItem(comp, this, ref);
        appendChild(reference);
        return reference;
    }
    
    /**
     * Add a child value.
     *
     * @param comp schema component extension
     * @param type schema type name
     * @param ref schema type equivalent (<code>null</code> if not appropriate)
     * @return value
     */
    public ValueItem addValue(AnnotatedBase comp, QName type, JavaType ref) {
        ValueItem item = new ValueItem(comp, type, ref, this);
        appendChild(item);
        return item;
    }

    /**
     * Add a child any.
     *
     * @param comp schema component
     * @return value
     */
    public AnyItem addAny(AnnotatedBase comp) {
        AnyItem item = new AnyItem(comp, this);
        appendChild(item);
        return item;
    }

    /**
     * Replace an item in this group with another item.
     *
     * @param current
     * @param replace
     */
    /*package*/ void replaceChild(Item current, Item replace) {
        Item last = current.m_last;
        Item next = current.m_next;
        if (last == null) {
            m_head = replace;
        } else {
            last.m_next = replace;
        }
        replace.m_last = last;
        if (next == null) {
            m_tail = replace;
        } else {
            next.m_last = replace;
        }
        replace.m_next = next;
    }
    
    /**
     * Adopt the child items from another group as the child items of this group.
     *
     * @param group
     */
    void adoptChildren(GroupItem group) {
        m_size = group.m_size;
        m_head = group.m_head;
        m_tail = group.m_tail;
        for (Item item = m_head; item != null; item = item.m_next) {
            item.reparent(this);
        }
    }

    /**
     * Check if structure to be inlined.
     *
     * @return inline
     */
    public boolean isInline() {
        return m_inline;
    }

    /**
     * Set structure to be inlined flag.
     *
     * @param inline
     */
    public void setInline(boolean inline) {
        m_inline = inline;
    }

    /**
     * Get effective item name, applying inheritance if necessary.
     * 
     * @return name
     */
    public String getEffectiveClassName() {
        GroupItem item = this;
        while (item.getClassName() == null) {
            item = item.getParent();
            if (item == null) {
                throw new IllegalStateException("Inherited class name with nothing to inherit");
            }
        }
        return item.getClassName();
    }

    /**
     * Get class name set directly for this group.
     *
     * @return name (<code>null</code> if to be inherited)
     */
    public String getClassName() {
        return m_className;
    }
    
    /**
     * Check if the class name is fixed by configuration.
     *
     * @return <code>true</code> if fixed, <code>false</code> if not
     */
    public boolean isFixedClassName() {
        return getComponentExtension().getClassName() != null;
    }
    
    /**
     * Set class name directly for this group. It is an error to call this method if the class name is fixed.
     * 
     * @param name (<code>null</code> if to be inherited)
     */
    public void setClassName(String name) {
        if (isFixedClassName()) {
            throw new IllegalStateException("Internal error - attempt to change configured class name");
        } else {
            m_className = name;
        }
    }

    /**
     * Get the number of items present in the group.
     *
     * @return count
     */
    public int getChildCount() {
        return m_size;
    }

    /**
     * Get head item in list grouped by this structure.
     * 
     * @return item (<code>null</code> if none)
     */
    public Item getFirstChild() {
        return m_head;
    }
    
    /**
     * Get information for class to be generated.
     *
     * @return class
     */
    public TypeData getGenerateClass() {
        return m_generateClass;
    }

    /**
     * Set information for class to be generated. If this group is a complexType extension and the base type is not
     * being inlined, this sets the generated class to extend the base type class.
     *
     * @param clas
     */
    public void setGenerateClass(TypeData clas) {
        m_generateClass = clas;
    }

    /**
     * Check if this group represents an extension reference.
     * 
     * @return <code>true</code> if extension reference, <code>false</code> if not
     */
    public boolean isExtensionReference() {
        return m_head instanceof ReferenceItem && m_head.getSchemaComponent().type() == SchemaBase.EXTENSION_TYPE;
    }

    /**
     * Handle groups which consist of a single type reference, or of an extension type reference, by subclassing the
     * class generated for the reference.
	 * TODO: instead use separate extension test, since this won't be called for embedded types
     */
    public void convertTypeReference() {
        if (m_head instanceof ReferenceItem && (m_head == m_tail ||
            m_head.getSchemaComponent() instanceof ComplexExtensionElement)) {
            DefinitionItem def = ((ReferenceItem)m_head).getDefinition();
            if (isEnumeration() == def.isEnumeration()) {
                TypeData base = def.getGenerateClass();
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Setting base class for " + m_generateClass.getFullName() + " to " +
                        base.getFullName());
                }
                ((ClassHolder)m_generateClass).setSuperClass(base);
                m_head.setImplicit(true);
            }
        }
    }

    /**
     * Copy the item under a different parent.
     *
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent
     * @return copy
     */
    protected Item copy(Item ref, GroupItem parent) {
        return new GroupItem(this, ref, parent);
    }
    
    /**
     * Set attribute present in group. This cascades the attribute present flag upward through containing groups until
     * one is found which defines an element name.
     */
    protected void forceAttributePresent() {
        if (!m_attributePresent && getSchemaComponent().type() != SchemaBase.ELEMENT_TYPE) {
            GroupItem parent = getParent();
            if (parent != null) {
                parent.forceAttributePresent();
            }
        }
        m_attributePresent = true;
    }
    
    /**
     * Set element present in group. This cascades the element present flag upward through containing groups until
     * one is found which defines an element name.
     */
    protected void forceElementPresent() {
        if (!m_elementPresent && getSchemaComponent().type() != SchemaBase.ELEMENT_TYPE) {
            GroupItem parent = getParent();
            if (parent != null) {
                parent.forceElementPresent();
            }
        }
        m_elementPresent = true;
    }
    
    /**
     * Set character data content present in group. This cascades the content present flag upward through all containing
     * groups until one is found which defines an element name.
     */
    protected void forceContentPresent() {
        if (!m_contentPresent && getSchemaComponent().type() != SchemaBase.ELEMENT_TYPE) {
            GroupItem parent = getParent();
            if (parent != null) {
                parent.forceContentPresent();
            }
        }
        m_contentPresent = true;
    }
    
    /**
     * Set required item present in group. This cascades the required item present flag upward through all containing
     * groups until one is found which defines either a wrapping element or a compositor other than a required sequence
     * (because a required item present within a required sequence means that there will always be something present in
     * the document, while any other type of compositor does not have this meaning).
     */
    protected void forceRequiredPresent() {
        if (m_allOptional) {
            int type = getSchemaComponent().type();
            boolean skip = false;
            switch (type) {
                
                case SchemaBase.ELEMENT_TYPE:
                case SchemaBase.CHOICE_TYPE:
                case SchemaBase.ALL_TYPE:
                    skip = true;
                    break;
                    
                case SchemaBase.SEQUENCE_TYPE:
                    skip = isOptional();
                    break;
                    
            }
            if (!skip) {
                GroupItem parent = getParent();
                if (parent != null) {
                    parent.forceContentPresent();
                }
            }
        }
        m_allOptional = false;
    }
    
    /**
     * Classify the content of this item as attribute, element, and/or character data content. For a group item, this
     * just needs to call the corresponding method for each child item.
     */
    protected void classifyContent() {
        
        // handle basic classification for this component
        super.classifyContent();
        
        // classify each child component
        for (Item item = m_head; item != null; item = item.getNext()) {
            item.classifyContent();
        }
        
        // force off the 'all optional' state regardless of content if a required element or attribute
        if (m_allOptional && !isOptional()) {
            switch (getSchemaComponent().type()) {
                
                case SchemaBase.ANY_TYPE:
                case SchemaBase.ELEMENT_TYPE:
                case SchemaBase.ANYATTRIBUTE_TYPE:
                case SchemaBase.ATTRIBUTE_TYPE:
                    m_allOptional = false;
                    break;
                    
            }
        }
    }
    
    /**
     * Convert an embedded group to a freestanding definition. This creates a definition using a cloned copy of the
     * structure of this group, then replaces this group with a reference to the definition.
     * TODO: just adopt the child items, rather than cloning? minor performance gain.
     *
     * @return definition
     */
    public DefinitionItem convertToDefinition() {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Converting " + SchemaUtils.describeComponent(getSchemaComponent()) +
                " to freestanding definition");
        }
        GroupItem parent = this;
        while (! (parent instanceof DefinitionItem)) {
            parent = parent.getParent();
        }
        DefinitionItem def = new DefinitionItem(this);
        if (def.getClassName() == null) {
            def.setClassName(getEffectiveClassName());
        }
        if (def.getName() == null) {
            def.setName(getEffectiveName());
        }
        ReferenceItem ref = new ReferenceItem(this, def);
        getParent().replaceChild(this, ref);
        return def;
    }
    
    /**
     * Build description of nested items.
     *
     * @param depth current nesting depth
     * @param classified include classification details flag
     * @return description
     */
    public String nestedString(int depth, boolean classified) {
        depth++;
        Item child = getFirstChild();
        StringBuffer buff = new StringBuffer(400);
        while (child != null) {
            buff.append(child.describe(depth, classified));
            child = child.getNext();
        }
        return buff.toString();
    }

    /**
     * Check if all immediate child nodes are optional. This is needed when handling code generation for a
     * reference to a group or attributeGroup handled as a separate object, since that object only needs to be
     * present if one or more of the values are present. This method is only meaningful for the topmost item associated
     * with a particular schema component (those for which {@link Item#isTopmost()} returns <code>true</code>).
     *
     * @return <code>true</code> if all child nodes optional, <code>false</code> if not
     */
    public boolean isAllOptional() {
        return m_allOptional;
    }

    /**
     * Check if an attribute is part of this item. This is only <code>true</code> for items corresponding to attribute
     * definitions, and groupings including these items which do not define an element name. This method is only
     * meaningful for the topmost item associated with a particular schema component (those for which {@link
     * Item#isTopmost()} returns <code>true</code>).
     *
     * @return <code>true</code> if attribute
     */
    public boolean isAttributePresent() {
        return m_attributePresent;
    }

    /**
     * Check if a child elements is part of this item. This is <code>true</code> for all items corresponding to element
     * definitions, and all groupings which include such an item. This method is only meaningful for the topmost item
     * associated with a particular schema component (those for which {@link Item#isTopmost()} returns
     * <code>true</code>).
     *
     * @return <code>true</code> if content
     */
    public boolean isElementPresent() {
        return m_elementPresent;
    }

    /**
     * Check if character data content is part of this item. This is <code>true</code> for all items corresponding to
     * simpleContent definitions, and all groupings which include such an item. This method is only meaningful for the
     * topmost item associated with a particular schema component (those for which {@link Item#isTopmost()} returns
     * <code>true</code>).
     *
     * @return <code>true</code> if content
     */
    public boolean isContentPresent() {
        return m_contentPresent;
    }
    
    /**
     * Generate a description of the item, including all nested items.
     *
     * @param depth current nesting depth
     * @param classified include classification details flag
     * @return description
     */
    protected String describe(int depth, boolean classified) {
        StringBuffer buff = new StringBuffer(depth + 50);
        buff.append(leadString(depth));
        if (isInline()) {
            buff.append("inlined ");
        }
        if (m_enumeration) {
            buff.append("enumeration ");
        }
        AnnotatedBase comp = getSchemaComponent();
        if (isTopmost()) {
            if (classified) {
                if (m_attributePresent) {
                    if (m_contentPresent) {
                        buff.append("attribute+content ");
                    } else {
                        buff.append("attribute ");
                    }
                } else if (m_contentPresent) {
                    buff.append("content ");
                }
            }
        } else {
            buff.append("duplicate ");
        }
        buff.append("group with class name ");
        buff.append(getClassName());
        buff.append(" and value name ");
        buff.append(getName());
        if (classified && isTopmost()) {
            buff.append(m_allOptional ? " (all items optional)" : " (not all items optional)");
        }
        buff.append(": ");
        buff.append(SchemaUtils.describeComponent(comp));
        buff.append('\n');
        buff.append(nestedString(depth, classified));
        return buff.toString();
    }
}
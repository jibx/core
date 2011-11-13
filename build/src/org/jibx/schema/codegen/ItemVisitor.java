/*
 * Copyright (c) 2006-2010, Dennis M. Sosnoski. All rights reserved.
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
import org.jibx.schema.SchemaContextTracker;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.SchemaVisitor;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.codegen.custom.GlobalExtension;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AnyElement;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupRefElement;
import org.jibx.schema.elements.CommonCompositorDefinition;
import org.jibx.schema.elements.CommonTypeDefinition;
import org.jibx.schema.elements.ComplexExtensionElement;
import org.jibx.schema.elements.ComplexRestrictionElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.GroupRefElement;
import org.jibx.schema.elements.ListElement;
import org.jibx.schema.elements.SimpleExtensionElement;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.elements.UnionElement;
import org.jibx.schema.support.SchemaTypes;

/**
 * Visitor to build the code generation items corresponding to a component. 
 */
public class ItemVisitor extends SchemaVisitor
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(ItemVisitor.class.getName());
    
    /** Extension information for the global definition being constructed. */
    private GlobalExtension m_global;
    
    /** Group currently being constructed. */
    private GroupItem m_group;
    
    /** Nesting depth, tracked for indenting of debug information. */
    private int m_nestingDepth;
    
    /**
     * Build the item structure corresponding to a schema global definition component. This sets the structure on the
     * global component extension before filling in the details, so that circular references won't cause a problem.
     *
     * @param comp
     * @return constructed structure
     */
    public DefinitionItem buildGlobal(AnnotatedBase comp) {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(SchemaUtils.getIndentation(m_nestingDepth) + "Building structure for global definition " +
                SchemaUtils.describeComponent(comp));
            m_nestingDepth++;
        }
        DefinitionItem definition = new DefinitionItem(comp);
        m_group = definition;
        m_global = (GlobalExtension)comp.getExtension();
        m_global.setDefinition(definition);
        TreeWalker wlkr = new TreeWalker(null, new SchemaContextTracker());
        wlkr.walkElement(comp, this);
        if (s_logger.isDebugEnabled()) {
            m_nestingDepth--;
            s_logger.debug(SchemaUtils.getIndentation(m_nestingDepth) + "Completed structure for global definition " +
                SchemaUtils.describeComponent(comp) + " with " + m_group.getChildCount() + " child items");
        }
        return definition;
    }
    
    /**
     * Build the item structure corresponding to a particular schema component. The supplied component can be a nested
     * type definition or a nested compositor. This method may be called recursively, so it needs to save and restore
     * the entry state.
     *
     * @param isenum enumeration flag
     * @param comp schema component (should be the simpleType component in the case of an enumeration)
     * @return constructed structure
     */
    private GroupItem buildStructure(boolean isenum, AnnotatedBase comp) {
        
        // first build the structure
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(SchemaUtils.getIndentation(m_nestingDepth) + "building structure for component " +
                SchemaUtils.describeComponent(comp));
            m_nestingDepth++;
        }
        GroupItem hold = m_group;
        m_group = hold.addGroup(comp);
        m_group.setEnumeration(isenum);
        
        // walk the nested definition to add details to structure
        TreeWalker wlkr = new TreeWalker(null, new SchemaContextTracker());
        wlkr.walkChildren(comp, this);
        
        // return the structure
        GroupItem ret = m_group;
        if (s_logger.isDebugEnabled()) {
            m_nestingDepth--;
            s_logger.debug(SchemaUtils.getIndentation(m_nestingDepth) + "completed structure for component " +
                SchemaUtils.describeComponent(comp) + " with " + m_group.getChildCount() + " child items");
        }
        m_group = hold;
        return ret;
    }
    
    /**
     * Add a reference to a global definition to the structure.
     *
     * @param comp referencing schema component
     * @param ref referenced schema component
     */
    private void addReference(AnnotatedBase comp, AnnotatedBase ref) {
        DefinitionItem definition = ((GlobalExtension)ref.getExtension()).getDefinition();
        if (definition == null) {
            GroupItem holdstruct = m_group;
            int holddepth = m_nestingDepth;
            definition = buildGlobal(ref);
            m_group = holdstruct;
            m_nestingDepth = holddepth;
        }
        m_group.addReference(comp, definition);
    }
    
    /**
     * Get the data type information for a built-in schema type.
     *
     * @param def schema type definition
     * @return type information
     */
    private JavaType getSchemaType(CommonTypeDefinition def) {
        String name = def.getName();
        JavaType type = (JavaType)m_global.getSchemaTypes().get(name);
        if (type == null) {
            throw new IllegalArgumentException("Unknown schema type '" + name + '\'');
        } else {
            return type;
        }
    }
    
    /**
     * Build an item from a type reference. For a predefined schema type this will be a simple {@link ValueItem}
     * wrapped in a {@link GroupItem}; for a global type it will be a reference to a global definition.
     *
     * @param comp
     * @param def
     */
    private void addTypeRefItem(AnnotatedBase comp, CommonTypeDefinition def) {
        if (def.isPredefinedType()) {
            GroupItem group = m_group.addGroup(comp);
            group.addValue(comp, def.getQName(), getSchemaType(def));
        } else {
            addReference(comp, def);
        }
    }
    
    //
    // Visit methods for generating values to classes

    /**
     * Visit &lt;any> definition.
     * 
     * @param node
     * @return <code>false</code> to block further expansion
     */
    public boolean visit(AnyElement node) {
        if (!SchemaUtils.isProhibited(node)) {
            m_group.addAny(node);
        }
        return false;
    }
    
    /**
     * Visit &lt;attribute> definition.
     * 
     * @param node
     * @return <code>false</code> to block further expansion
     */
    public boolean visit(AttributeElement node) {
        if (node.getUse() != AttributeElement.PROHIBITED_USE) {
            
            // check for direct definition (rather than reference)
            AttributeElement refattr = node.getReference();
            if (refattr == null) {
                if (node.getType() == null) {
                    
                    // create a group for the embedded definition
                    buildStructure(false, node);
                    
                } else {
                    
                    // handle reference to global or predefined type
                    addTypeRefItem(node, node.getTypeDefinition());
                }
                
            } else {
                
                // use reference to definition structure
                addReference(node, node.getReference());
                
            }
        }
        return false;
    }
    
    /**
     * Visit &lt;attributeGroup> reference.
     * 
     * @param node
     * @return <code>false</code> to block further expansion
     */
    public boolean visit(AttributeGroupRefElement node) {
        addReference(node, node.getReference());
        return false;
    }
    
    /**
     * Visit compositor.
     * 
     * @param node
     * @return <code>false</code> to block further expansion
     */
    public boolean visit(CommonCompositorDefinition node) {
        buildStructure(false, node);
        return false;
    }

    /**
     * Visit complex type &lt;extension> definition. This adds a reference item for the base type, then continues
     * expansion to handle the items added by extension.
     *
     * @param node
     * @return <code>true</code> to continue expansion
     */
    public boolean visit(ComplexExtensionElement node) {
        CommonTypeDefinition base = node.getBaseType();
        if (base != SchemaTypes.ANY_TYPE) {
            addReference(node, base);
        }
        return true;
    }

    /**
     * Visit complex type &lt;restriction> definition. This adds a reference item for the base type, blocking further
     * expansion.
     *
     * @param node
     * @return <code>false</code> to end expansion
     */
    public boolean visit(ComplexRestrictionElement node) {
        CommonTypeDefinition base = node.getBaseType();
        if (base != SchemaTypes.ANY_TYPE) {
            addReference(node, base);
        }
        return false;
    }

    /**
     * Visit &lt;element> definition.
     * 
     * @param node
     * @return <code>false</code> to block further expansion
     */
    public boolean visit(ElementElement node) {
        if (!SchemaUtils.isProhibited(node)) {
            
            // check if direct definition
            ElementElement refelem = node.getReference();
            if (refelem == null) {
                if (node.getType() == null || node.getType().equals(SchemaTypes.ANY_TYPE.getQName())) {
                    
                    // create a group for the embedded definition
                    buildStructure(false, node);
                    
                } else {
                    
                    // handle reference to global or predefined type
                    addTypeRefItem(node, node.getTypeDefinition());
                }
                
            } else {
                
                // use reference to definition structure
                addReference(node, refelem);
                
            }
        }
        return false;
    }
    
    /**
     * Visit &lt;group> reference.
     * 
     * @param node
     * @return <code>false</code> to block further expansion
     */
    public boolean visit(GroupRefElement node) {
        addReference(node, node.getReference());
        return false;
    }
    
    /**
     * Visit &lt;list> element. This adds a collection value matching the type of list.
     *
     * @param node
     * @return <code>false</code> to block further expansion
     */
    public boolean visit(ListElement node) {
        QName type = node.getItemType();
        if (type == null) {
            buildStructure(false, node);
        } else {
            addTypeRefItem(node, node.getItemTypeDefinition());
        }
        return false;
    }
    
    /**
     * Visit simple type &lt;extension> element.
     *
     * @param node
     * @return <code>true</code> to continue expansion
     */
    public boolean visit(SimpleExtensionElement node) {
        addTypeRefItem(node, node.getBaseType());
        return true;
    }
    
    /**
     * Visit simple type &lt;restriction> element.
     *
     * @param node
     * @return <code>false</code> to block further expansion
     */
    public boolean visit(SimpleRestrictionElement node) {
        CommonTypeDefinition type = node.getBaseType();
        if (type == null) {
            buildStructure(false, node.getDerivation());
        } else {
            addTypeRefItem(node, type);
        }
        return false;
    }
    
    /**
     * Visit &lt;simpleType> element. This checks for the special case of a type definition which consists of an
     * enumeration, and adds a group to represent the enumeration if found.
     *
     * @param node
     * @return <code>true</code> to continue expansion, unless processed as group
     */
    public boolean visit(SimpleTypeElement node) {
        if (SchemaUtils.isEnumeration(node)) {
            
            // check if already an associated group (as will be for global type definition)
            if (m_group.getSchemaComponent() == node) {
                m_group.setEnumeration(true);
            } else {
                buildStructure(true, node);
                return false;
            }
        }
        return true;
    }

    /**
     * Visit &lt;union> element. This directly builds a structure matching the component types of the union, with the
     * nested types handled directly and the referenced types added separately.
     * 
     * @param node
     * @return <code>true</code> to expand any inline types
     */
    public boolean visit(UnionElement node) {
        GroupItem struct = buildStructure(false, node);
        CommonTypeDefinition[] types = node.getMemberTypeDefinitions();
        if (types != null) {
            for (int i = 0; i < types.length; i++) {
                CommonTypeDefinition type = types[i];
                if (type.isPredefinedType()) {
                    struct.addValue(node, type.getQName(), getSchemaType(type));
                } else {
                    GroupItem hold = m_group;
                    m_group = struct;
                    addReference(node, type);
                    m_group = hold;
                }
            }
        }
        return false;
    }
}
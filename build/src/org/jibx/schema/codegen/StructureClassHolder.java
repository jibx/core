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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.jibx.binding.model.BindingHolder;
import org.jibx.binding.model.BuiltinFormats;
import org.jibx.binding.model.CollectionElement;
import org.jibx.binding.model.ContainerElementBase;
import org.jibx.binding.model.ElementBase;
import org.jibx.binding.model.FormatElement;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.NestingAttributes;
import org.jibx.binding.model.PropertyAttributes;
import org.jibx.binding.model.StructureElement;
import org.jibx.binding.model.StructureElementBase;
import org.jibx.binding.model.ValueElement;
import org.jibx.runtime.QName;
import org.jibx.runtime.Utility;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.codegen.custom.NestingCustomBase;
import org.jibx.schema.codegen.custom.SchemaRootBase;
import org.jibx.schema.codegen.extend.ClassDecorator;
import org.jibx.schema.codegen.extend.NameConverter;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.CommonCompositorBase;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.util.Types;
import org.jibx.util.UniqueNameSet;

/**
 * Information for a data class to be included in code generated from schema.
 * 
 * @author Dennis M. Sosnoski
 */
public class StructureClassHolder extends ClassHolder
{
    private static final String LIST_DESERIALIZE_PREFIX = "deserialize";
    
    private static final String LIST_SERIALIZE_PREFIX = "serialize";
    
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(StructureClassHolder.class.getName());
    
    /** Default format definitions map. */
    private static final Map s_formatMap;
    static {
        s_formatMap = new HashMap();
        for (int i = 0; i < BuiltinFormats.s_builtinFormats.length; i++) {
            FormatElement format = BuiltinFormats.s_builtinFormats[i];
            s_formatMap.put(format.getTypeName(), format);
        }
    }
    
    /** Flag for collection present in class. */
    private boolean m_collectionPresent;
    
    /** Root node for data structure of class. */
    private ParentNode m_dataRoot;
    
    /** Binding definition element for this class. */
    private ContainerElementBase m_bindingElement;
    
    /** Selection property names used in class (lazy create, <code>null</code> if none). */
    protected UniqueNameSet m_selectSet;
    
    /**
     * Constructor.
     * 
     * @param name class name
     * @param base base class name
     * @param pack package information
     * @param holder binding holder
     * @param nconv name converter
     * @param decorators class decorators
     * @param inner use inner classes for substructures
     */
    public StructureClassHolder(String name, String base, PackageHolder pack, BindingHolder holder, NameConverter nconv,
        ClassDecorator[] decorators, boolean inner) {
        super(name, base, pack, holder, nconv, decorators, inner, false);
    }
    
    /**
     * Constructor for creating a child inner class definition.
     * 
     * @param name class name
     * @param context parent class
     */
    private StructureClassHolder(String name, StructureClassHolder context) {
        super(name, context, false);
    }
    
    /**
     * Derive group names from the containing group prefix and the simple name of the group.
     * 
     * @param group
     * @param container (<code>null</code> if none)
     * @return name
     */
    // static String deriveGroupName(GroupItem group, Group container) {
    // String prefix = null;
    // if (container != null) {
    // prefix = group.getClassName();
    // String prior = container.getPrefix();
    // if (prior == null) {
    // prefix = NameConverter.toNameLead(prefix);
    // } else {
    // prefix = prior + NameConverter.toNameWord(prefix);
    // }
    // prefix = container.uniqueChildPrefix(prefix);
    // }
    // return prefix;
    // }
    
    /**
     * Populate a class data representation tree based on a supplied item tree. The mapping between the two trees is not
     * one-to-one since item groupings may be ignored where irrelevant.
     * 
     * @param struct root item in tree
     * @param supertext schema documentation passed in for item tree
     * @param parent containing data node
     * @param bindhold associated binding definition holder
     */
    private void addToTree(GroupItem struct, String supertext, ParentNode parent, BindingHolder bindhold) {
        if (struct.getChildCount() > 1) {
            supertext = null;
        }
        for (Item item = struct.getFirstChild(); item != null; item = item.getNext()) {
            
            // first check for ignored group
            if (item.isIgnored()) {
                new ParentNode((GroupItem)item, parent);
            } else {
                
                // check for documentation available on schema component
                String doctext = supertext;
                if (item.isTopmost()) {
                    String curtext = extractDocumentation(item.getSchemaComponent());
                    if (curtext != null) {
                        doctext = curtext;
                    }
                }
                if (item.isCollection()) {
                    m_collectionPresent = true;
                }
                if (item instanceof GroupItem) {
                    GroupItem group = (GroupItem)item;
                    if (group.isInline()) {
                        if (group.getChildCount() > 0) {
                            
                            // create a new group for an inlined compositor only if it's <all> or <choice> or nested
                            ParentNode into = parent;
                            AnnotatedBase comp = item.getSchemaComponent();
                            if (comp instanceof CommonCompositorBase) {
                                if (comp.type() == SchemaBase.ALL_TYPE || comp.type() == SchemaBase.CHOICE_TYPE
                                    || comp.getParent() instanceof CommonCompositorBase) {
                                    into = new ParentNode(group, parent);
                                    into.setDocumentation(doctext);
                                    doctext = null;
                                }
                            } else {
                                
                                // create a new group for a non-compositor only if
                                // different schema component
                                if (struct.getSchemaComponent() != comp
                                    && group.getFirstChild().getSchemaComponent() != comp) {
                                    into = new ParentNode(group, parent);
                                    into.setDocumentation(doctext);
                                    doctext = null;
                                }
                                
                            }
                            addToTree(group, doctext, into, bindhold);
                            into.adjustName();
                            
                        } else {
                            
                            // just create parent node with no children matching group for optional empty grouping (so
                            //  that it can be generated using a presence flag)
                            new ParentNode(group, parent);
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Added empty parent for " +
                                    SchemaUtils.describeComponent(group.getSchemaComponent()));
                            }
                            
                        }
                    } else {
                        
                        // create a new class and populate that
                        ClassHolder child;
                        String text = group.getEffectiveClassName();
                        if (m_useInnerClasses) {
                            if (m_nameSet.contains(text)) {
                                StructureClassHolder outer = this;
                                while (outer != null) {
                                    if (outer.getName().equals(text)) {
                                        text += "Inner";
                                        break;
                                    } else {
                                        outer = (StructureClassHolder)outer.m_outerClass;
                                    }
                                }
                            }
                            text = m_nameSet.add(text);
                            child = group.isEnumeration() ? new EnumerationClassHolder(text, this)
                                : (ClassHolder)new StructureClassHolder(text, this);
                            m_inners.add(child);
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Added inner class " + child.getFullName());
                            }
                        } else {
                            String fullname = m_baseName + text;
                            child = m_package.addClass(fullname, m_baseName, m_nameConverter, m_decorators,
                                group.isEnumeration(), m_holder);
                            m_importsTracker.addImport(child.getFullName(), true);
                            text = child.getName();
                            if (group.isEnumeration()) {
                                FormatElement format = new FormatElement();
                                format.setTypeName(child.getBindingName());
                                ((EnumerationClassHolder)child).setBinding(format);
                                bindhold.addFormat(format);
                            }
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Added derived class " + child.getFullName());
                            }
                        }
                        if (!group.isFixedClassName()) {
                            group.setClassName(text);
                        }
                        group.setGenerateClass(child);
                        DataNode value = new LeafNode(group, parent);
                        value.setDocumentation(doctext);
                        if (!group.isEnumeration()) {
                            group.convertTypeReference();
                            importValueType(value);
                        }
                        child.buildDataStructure(group, bindhold);
                        
                    }
                    
                } else {
                    DataNode value = new LeafNode(item, parent);
                    value.setDocumentation(doctext);
                    importValueType(value);
                }
            }
        }
    }
    
    /**
     * Convert an item structure to a class representation. This may include creating subsidiary classes (either as
     * inner classes, or as separate standalone classes), where necessary.
     * 
     * @param group item group
     * @param bindhold associated binding definition holder
     */
    public void buildDataStructure(GroupItem group, BindingHolder bindhold) {
        super.buildDataStructure(group, bindhold);
        if (group.isEnumeration()) {
            throw new IllegalArgumentException("Internal error - group is an enumeration");
        } else {
            
            // populate the actual definition structure
            m_dataRoot = new ParentNode(group, null);
            m_dataRoot.setDocumentation(extractDocumentation(group.getSchemaComponent()));
            addToTree(group, null, m_dataRoot, bindhold);
            
            // import the list type if needed
            int reptype = getSchemaCustom().getRepeatType();
            if ((reptype == SchemaRootBase.REPEAT_LIST || reptype == SchemaRootBase.REPEAT_TYPED) &&
                m_collectionPresent) {
                m_importsTracker.addImport(COLLECTION_VARIABLE_TYPE, false);
                m_importsTracker.addImport(m_listImplClass, false);
            }
        }
    }
    
    /**
     * Set the binding component linked to this class.
     * 
     * @param container binding definition element (&lt;mapping> or &lt;structure>)
     */
    public void setBinding(ContainerElementBase container) {
        m_bindingElement = container;
    }
    
    /**
     * Recursively add all inner enumeration classes as formats to a &lt;mapping> definition. This is used to create the
     * &lt;format> elements for all nested enumerations, which need to be direct children of the &lt;mapping> element
     * for the top-level class.
     * 
     * @param mapping
     */
    private void addInnerFormats(MappingElementBase mapping) {
        for (int i = 0; i < m_inners.size(); i++) {
            ClassHolder inner = (ClassHolder)m_inners.get(i);
            if (inner.isSimpleValue()) {
                FormatElement format = new FormatElement();
                format.setTypeName(inner.getBindingName());
                ((EnumerationClassHolder)inner).setBinding(format);
                mapping.addTopChild(format);
            } else {
                ((StructureClassHolder)inner).addInnerFormats(mapping);
            }
        }
    }
    
    /**
     * Add all fixed names in a group to the set of names defined for this class. This calls itself recursively to
     * handle nested groups.
     * 
     * @param wrapper
     */
    private void addFixedNames(ParentNode wrapper) {
        ArrayList values = wrapper.getChildren();
        for (int i = 0; i < values.size(); i++) {
            DataNode value = (DataNode)values.get(i);
            Item item = value.getItem();
            boolean addname = item.isFixedName();
            if (value instanceof ParentNode) {
                ParentNode childgrp = (ParentNode)value;
                addFixedNames(childgrp);
                addname = addname && (childgrp.isSelectorNeeded() || wrapper.isSelectorNeeded());
            }
            if (addname) {
                String name = item.getEffectiveName();
                if (!m_nameSet.add(name).equals(name)) {
                    // TODO: pass in the validation context, create an error
                    throw new IllegalStateException("Name '" + name + "' cannot be used twice in same context");
                }
            }
        }
    }
    
    /**
     * Convert base name to collection name. If using a <code>java.util.List</code> representation the name is converted
     * to singular form and "List" is appended; if using an array representation the name is converted to plural form.
     *
     * @param base
     * @param item
     */
    private void setCollectionName(String base, Item item) {
        String name;
        if (getSchemaCustom().getRepeatType() == SchemaRootBase.REPEAT_ARRAY) {
            name = m_nameConverter.pluralize(base);
            if (!name.equals(base)) {
                s_logger.debug("Converted name " + base + " to " + name);
            }
        } else {
            String singular = m_nameConverter.depluralize(base);
            if (!singular.equals(base)) {
                s_logger.debug("Converted name " + base + " to " + singular);
            }
            name = singular + "List";
        }
        item.setName(m_nameSet.add(name));
    }

    /**
     * Handle value name assignments for a group within this class. This calls itself recursively to handle nested
     * groups.
     * 
     * @param parent
     * @param innamed flag for parent group name already fixed
     */
    private void fixFlexibleNames(ParentNode parent, boolean innamed) {
        
        // check for group which uses a selector (choice or union)
        String suffix = null;
        ArrayList nodes = parent.getChildren();
        if (parent.isSelectorNeeded()) {
            
            // add the actual variable name used to record current state
            Item item = parent.getItem();
            item.setName(m_nameSet.add(m_nameConverter.toBaseName(item.getEffectiveName()) + "Select"));
            
            // create value name set if first time used
            if (m_selectSet == null) {
                m_selectSet = new UniqueNameSet();
            }
            
            // generate constant for each child value
            if (parent.getSchemaComponent().type() == SchemaBase.UNION_TYPE) {
                suffix = "_Form";
            } else {
                suffix = "_Choice";
            }
            
        }
        
        // handle name conversions and recording
        for (int i = 0; i < nodes.size(); i++) {
            DataNode node = (DataNode)nodes.get(i);
            Item item = node.getItem();
            String name = null;
            if (node instanceof ParentNode) {
                
                // use recursive call to set child group names (adopting name for group if same as first child, for
                //  group inside choice, in order to avoid adding the same name twice for non-conflicting usages); also
                //  adopt the child name if this is a collection, or if the first child is an implicit value, or if the
                //  group item is unnamed and there's only one child
                ArrayList childvals = ((ParentNode)node).getChildren();
                if (childvals.size() > 0) {
                    if (!item.isFixedName() && innamed) {
                        item.setName(parent.getItem().getName());
                    }
                    boolean passname = false;
                    if (childvals.size() == 1) {
                        
                        // want to set the name at this level and pass it down when this is an element or attribute and
                        //  there's only one child component all the way down to a leaf
                        passname = item.isFixedName() || innamed;
                        int type = item.getSchemaComponent().type();
                        if (!passname && (type == SchemaBase.ATTRIBUTE_TYPE ||
                            type == SchemaBase.ELEMENT_TYPE)) {
                            
                            // check for single value all the way down
                            ArrayList childs = childvals;
                            boolean collect = node.isCollection();
                            while (childs.size() == 1) {
                                DataNode child = (DataNode)childs.get(0);
                                if (child.isCollection()) {
                                    collect = true;
                                }
                                if (child instanceof LeafNode) {
                                    
                                    // reached leaf with single path, so set name at current level and pass down
                                    name = item.getEffectiveName();
                                    if (collect) {
                                        setCollectionName(name, item);
                                    } else {
                                        item.setName(m_nameSet.add(name));
                                    }
                                    passname = true;
                                    break;
                                    
                                } else {
                                    childs = ((ParentNode)child).getChildren();
                                }
                            }
                        }
                    }
                    fixFlexibleNames((ParentNode)node, passname);
                }
                
            } else if (item.isFixedName()) {
                name = item.getName();
            } else if (innamed) {
                if (!item.isFixedName()) {
                    item.setName(parent.getItem().getName());
                }
                name = item.getName();
            } else {
                
                // convert and add the value name
                name = item.getEffectiveName();
                if (node.isCollection()) {
                    setCollectionName(name, item);
                    name = item.getName();
                } else {
                    item.setName(m_nameSet.add(name));
                }
            }
            
            // handle selection naming, if needed
            if (parent.isSelectorNeeded()) {
                if (name == null) {
                    name = item.getEffectiveName();
                    if (node.isCollection()) {
                        
                        // pluralize name for collection, as will be done with the actual property name
                        // TODO: really need to use name structure and references, to avoid duplicated efforts like this
                        name = m_nameConverter.pluralize(name);
                        
                    }
                }
                name = m_selectSet.add(NameUtils.toNameWord(name));
                node.setSelectPropName(name);
                node.setSelectConstName(m_nameConverter.toConstantName(name + suffix));
            }
        }
    }

    /**
     * Generate the code to check and set the selection on any containing selector group. This should be used when
     * setting any value, including inside selector methods (if used), since selector groups may be nested.
     * 
     * @param value
     * @param block
     * @param builder
     */
    private void generateSelectorSet(DataNode value, BlockBuilder block, ClassBuilder builder) {
        ParentNode group;
        while ((group = value.getParent()) != null) {
            if (group.isSelectorNeeded()) {
                int type = group.getSelectorType();
                if (type == NestingCustomBase.SELECTION_CHECKEDSET || type == NestingCustomBase.SELECTION_CHECKEDBOTH) {
                    
                    // when using select method call, just call that method (it will call containing group method, if
                    // any)
                    InvocationBuilder call = builder.createMemberMethodCall(group.getSelectSetMethod());
                    call.addVariableOperand(value.getSelectConstName());
                    block.addCall(call);
                    break;
                    
                } else {
                    
                    // if setting directly, set this one and continue up to next containing group
                    block.addAssignVariableToField(value.getSelectConstName(), group.getSelectField());
                    
                }
            }
            value = group;
        }
    }

    /**
     * Generate the code to check the selection on any containing selector group. This should be used when getting any
     * value, including inside selector methods (if used), since selector groups may be nested.
     * 
     * @param value
     * @param block
     * @param builder
     */
    private void generateSelectorCheck(DataNode value, BlockBuilder block, ClassBuilder builder) {
        ParentNode group;
        while ((group = value.getParent()) != null) {
            if (group.isSelectorNeeded()) {
                int type = group.getSelectorType();
                if (type == NestingCustomBase.SELECTION_CHECKEDBOTH ||
                    type == NestingCustomBase.SELECTION_OVERRIDEBOTH) {
                    
                    // when using select method call, just call that method (it will call containing group method, if
                    // any)
                    InvocationBuilder call = builder.createMemberMethodCall(group.getSelectCheckMethod());
                    call.addVariableOperand(value.getSelectConstName());
                    block.addCall(call);
                    break;
                    
                }
            }
            value = group;
        }
    }
    
    /**
     * Generate a test method for a value, if it's part of a group with a selector.
     *
     * @param node
     * @param seldesc containing group description
     * @param valdesc description of this value within group
     * @param builder
     */
    private void checkIfMethod(DataNode node, String seldesc, String valdesc, ClassBuilder builder) {
        if (node.getParent().isSelectorNeeded()) {
            MethodBuilder ifmeth = builder.addMethod("if" + node.getSelectPropName(), "boolean");
            ifmeth.setPublic();
            ifmeth.addSourceComment("Check if " + valdesc + " is current selection for " + seldesc + '.');
            ifmeth.addSourceComment("");
            ifmeth.addSourceComment("@return", " <code>true</code> if selection, <code>false</code> if not");
            InfixExpressionBuilder testexpr = builder.buildNameOp(node.getParent().getSelectField(), Operator.EQUALS);
            testexpr.addVariableOperand(node.getSelectConstName());
            ifmeth.createBlock().addReturnExpression(testexpr);
        }
    }
    
    /**
     * Set the optional state of a <b>structure<b/> or <b>collection<b/> element in the binding. The name for the
     * <b>structure<b/> or <b>collection<b/> must be set before calling this method, since the presence or absence of
     * a name determines whether optional status is passed down from a parent.
     * 
     * @param value node
     * @param force optional state forced flag
     * @param struct binding structure
     */
    private void setStructureOptional(DataNode value, boolean force, StructureElementBase struct) {
        boolean optional = value.isOptional();
        if (!optional && struct.getName() == null) {
            
            // no name for structure, so see if we can make the whole structure optional
            Item item = value.getItem();
            if (item instanceof GroupItem && item.isTopmost()) {
                optional = ((GroupItem)item).isAllOptional();
            } else if (getSchemaCustom().isStructureOptional() && item instanceof ReferenceItem) {
                optional = ((ReferenceItem)item).getDefinition().isAllOptional();
            }
            
        }
        if (optional) {
            struct.setUsage(PropertyAttributes.OPTIONAL_USAGE);
        }
    }
    
    /**
     * Set the name and namespace URI for a &lt;value> binding component. The value style must be set before making this
     * call, since element and attribute elements are handled differently.
     *
     * @param qname qualified name to be set (<code>null</code> if none)
     * @param holder binding containing the value definition
     * @param value binding component
     */
    private static void setName(QName qname, BindingHolder holder, ValueElement value) {
        if (qname != null) {
            if (value.getName() != null) {
                throw new IllegalStateException("Internal error - trying to overwrite name '" + value.getName() + "' with '" + qname.getName() + '\'');
            }
            value.setName(qname.getName());
            String uri = qname.getUri();
            int style = value.getStyle();
            if (style == NestingAttributes.ATTRIBUTE_STYLE) {
                if (uri != null) {
                    holder.addNamespaceUsage(uri);
                }
                value.setUri(uri);
            } else if (style == NestingAttributes.ELEMENT_STYLE) {
                holder.addNamespaceUsage(uri);
                if (!Utility.safeEquals(uri, holder.getElementDefaultNamespace())) {
                    value.setUri(uri);
                }
            }
        }
    }
    
    /**
     * Set the name and namespace URI for a &lt;structure> or &lt;collection> binding component.
     *
     * @param qname qualified name to be set (<code>null</code> if none)
     * @param holder binding containing the structure or collection definition
     * @param struct binding component
     */
    private static void setName(QName qname, BindingHolder holder, StructureElementBase struct) {
        if (qname != null) {
            if (struct.getName() != null) {
                throw new IllegalStateException("Internal error - trying to overwrite name '" + struct.getName() + "' with '" + qname.getName() + '\'');
            }
            String name = qname.getName();
            String uri = qname.getUri();
            if (name.charAt(0) == '{') {
                int split = name.indexOf('}');
                uri = name.substring(1, split);
                name = name.substring(split+1);
            }
            struct.setName(name);
            holder.addNamespaceUsage(uri);
            if (!Utility.safeEquals(uri, holder.getElementDefaultNamespace())) {
                struct.setUri(uri);
            }
        }
    }
    
    /**
     * Build a &lt;value> binding component for a field.
     * 
     * @param node
     * @param wrapname
     * @param gname
     * @param sname
     * @param holder
     * @return constructed binding component
     */
    private ValueElement buildValueBinding(DataNode node, QName wrapname, String gname, String sname,
        BindingHolder holder) {
        
        // add <value> element to binding structure for simple (primitive or text) value
        ValueElement value = new ValueElement();
        if (gname == null) {
            value.setDeclaredType(node.getBindingType());
        } else {
            value.setGetName(gname);
            value.setSetName(sname);
        }
        setValueHandlingOptions(node.getItem(), value, holder);
        
        // set test method if needed to pick between alternatives
        ParentNode wrapper = node.getParent();
        if (wrapper.isSelectorNeeded()) {
            value.setTestName("if" + node.getSelectPropName());
            value.setUsage(PropertyAttributes.OPTIONAL_USAGE);
        } else if (node.isOptional()) {
            value.setUsage(PropertyAttributes.OPTIONAL_USAGE);
        }
        
        // get the schema component supplying an element or attribute name
        AnnotatedBase comp = node.getSchemaComponent();
        if (wrapname == null) {
            
            // scan up structure to find parent linked to a different schema component
            // TODO: find a better way to handle this in the node or item structures
            ParentNode parent = wrapper;
            while (parent != null && parent.getSchemaComponent() == comp) {
                parent = parent.getParent();
            }
            
            // set name from node and style from component type
            if (parent == null) {
                
                // all parents linked to same component, treat as special case with name already set
                value.setEffectiveStyle(ValueElement.TEXT_STYLE);
                
            } else {
                
                // name will have been passed down from containing element
                // (since attributes handled directly)
                if (comp.type() == SchemaBase.ELEMENT_TYPE) {
                    
                    // value is an element, set the name directly
                    value.setEffectiveStyle(NestingAttributes.ELEMENT_STYLE);
                    ElementElement elem = (ElementElement)comp;
                    if (SchemaUtils.isOptionalElement(elem)) {
                        // TODO: this is needed because the optional status doesn't inherit downward for embedded items,
                        // as when a simpleType is nested inside an optional attribute. should the code be changed to
                        // inherit instead?
                        value.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                    }
                    
                } else if (comp.type() == SchemaBase.ATTRIBUTE_TYPE) {
                    
                    // value is an attribute, set the name directly
                    value.setEffectiveStyle(NestingAttributes.ATTRIBUTE_STYLE);
                    AttributeElement attr = (AttributeElement)comp;
                    if (SchemaUtils.isOptionalAttribute(attr)) {
                        // TODO: this is needed because the optional status doesn't inherit downward for embedded items, as
                        // when a simpleType is nested inside an optional attribute. should the code be changed to
                        // inherit instead?
                        value.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                    }
                    
                } else {
                    value.setEffectiveStyle(ValueElement.TEXT_STYLE);
                }
                setName(node.getQName(), holder, value);
            }
        } else {
            value.setEffectiveStyle(NestingAttributes.ELEMENT_STYLE);
            setName(wrapname, holder, value);
        }
        return value;
    }
    
    /**
     * Set the field and get/set access method names for a property.
     * 
     * @param basename
     * @param node
     */
    private void setMemberNames(String basename, DataNode node) {
        
        // define the field and get/set names
        String propname = node.getPropName();
        s_logger.debug("Adding property " + propname);
        node.setFieldName(m_nameConverter.toFieldName(m_nameConverter.toBaseName(propname)));
        String getpref = "boolean".equals(node.getType()) ? "is" : "get";
        node.setGetMethodName(getpref + propname);
        node.setSetMethodName("set" + propname);
    }
    
    /**
     * Add a simple property to the class. This adds the actual field definition, along with the appropriate access
     * methods.
     * 
     * @param basename
     * @param node
     * @param builder
     */
    private void addSimpleProperty(String basename, DataNode node, ClassBuilder builder) {
        
        // set the field and method names
        setMemberNames(basename, node);
        
        // make sure the type is defined
        String type = node.getType();
        if (type == null) {
            
            // type can be null in case of xs:any with discard handling, but otherwise invalid
            if (node.isAny()) {
                return;
            } else {
                throw new IllegalStateException("Internal error - no type for property");
            }
            
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found type " + type + " for property " + basename);
        }
        
        // generate the field as a simple value
        String fname = node.getFieldName();
        FieldBuilder field = builder.addField(fname, type);
        field.setPrivate();
        
        // add get method definition (unchecked, but result meaningless if not the selected group item)
        MethodBuilder getmeth = builder.addMethod(node.getGetMethodName(), type);
        getmeth.setPublic();
        StringBuffer buff = new StringBuffer();
        buff.append("Get the ");
        String descript = describe(node);
        buff.append(descript);
        buff.append(" value.");
        String document = findDocumentation(false, node);
        if (document != null) {
            buff.append(' ');
            buff.append(document);
        }
        getmeth.addSourceComment(buff.toString());
        getmeth.addSourceComment("");
        getmeth.addSourceComment("@return", " value");
        BlockBuilder block = getmeth.createBlock();
        generateSelectorCheck(node, block, builder);
        block.addReturnNamed(fname);
        
        // add the set method definition
        MethodBuilder setmeth = builder.addMethod(node.getSetMethodName(), "void");
        setmeth.setPublic();
        buff.replace(0, 3, "Set");
        setmeth.addSourceComment(buff.toString());
        setmeth.addSourceComment("");
        String nonres = NameUtils.convertReserved(basename);
        setmeth.addSourceComment("@param", " " + nonres);
        setmeth.addParameter(nonres, type);
        block = setmeth.createBlock();
        generateSelectorSet(node, block, builder);
        block.addAssignVariableToField(nonres, fname);
        
        // call decorators for added value processing
        for (int i = 0; i < m_decorators.length; i++) {
            m_decorators[i].valueAdded(nonres, false, type, field.getDeclaration(), getmeth.getDeclaration(),
                setmeth.getDeclaration(), descript, this);
        }
    }
    
    /**
     * Add a multiple-valued property to the class. This adds the actual field definition, along with the appropriate
     * access methods.
     * 
     * @param basename
     * @param node
     * @param builder
     */
    private void addRepeatedProperty(String basename, DataNode node, ClassBuilder builder) {
        
        // set up the member names
        String propname = node.getPropName();
        setMemberNames(basename, node);
        
        // find the basic value type (which may require digging down to the leaf node, for a complex collection)
        String basetype;
        int collcount = node.isCollection() ? 1 : 0;
        DataNode nested = node;
        while ((basetype = nested.getType()) == null && nested.isInterior() &&
            ((ParentNode)nested).getChildren().size() > 0) {
            nested = (DataNode)((ParentNode)nested).getChildren().get(0);
            if (nested.isCollection()) {
                collcount++;
            }
        }
        if (basetype == null) {
            
            // type can be null in case of xs:any with discard handling, but otherwise invalid
            if (node.isAny()) {
                return;
            } else {
                basetype = "java.lang.String";
            }
            
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Found type " + basetype + " (" + collcount + " layers of collection nesting) for property "
                + propname);
        }
        
        // find the types to be used for field and actual instance
        String type;
        Type fieldtype;
        Type insttype;
        int reptype = getSchemaCustom().getRepeatType();
        if (reptype == SchemaRootBase.REPEAT_TYPED) {
            fieldtype = builder.createParameterizedType(COLLECTION_VARIABLE_TYPE, basetype);
            insttype = builder.createType(basetype);
            while (--collcount > 0) {
                fieldtype = builder.createParameterizedType(COLLECTION_VARIABLE_TYPE, fieldtype);
                insttype = builder.createParameterizedType(COLLECTION_VARIABLE_TYPE, insttype);
            }
            type = basetype;
            insttype = builder.createParameterizedType(m_listImplClass, insttype);
        } else if (reptype == SchemaRootBase.REPEAT_LIST || node.isAny()) {
            fieldtype = builder.createType(COLLECTION_VARIABLE_TYPE);
            insttype = builder.createType(m_listImplClass);
            if (collcount > 1) {
                type = COLLECTION_VARIABLE_TYPE;
            } else {
                type = basetype;
            }
        } else {
            type = basetype;
            while (--collcount > 0) {
                type += "[]";
            }
            fieldtype = builder.createType(type + "[]");
            insttype = null;
        }
        
        // get documentation and description text
        String document = findDocumentation(true, node);
        String descript = describe(node);
        String term = getSchemaCustom().getRepeatType() == SchemaRootBase.REPEAT_ARRAY ? "array" : "list";
        
        // generate the field as a collection
        String fname = node.getFieldName();
        FieldBuilder field = builder.addField(fname, fieldtype);
        if (insttype != null) {
            field.setInitializer(builder.newInstance(insttype));
        }
        field.setPrivate();
        
        // add get method definition (unchecked, but result meaningless if not the selected group item)
        MethodBuilder getmeth = builder.addMethod(node.getGetMethodName(), (Type)builder.clone(fieldtype));
        getmeth.setPublic();
        StringBuffer buff = new StringBuffer();
        buff.append("Get the ");
        buff.append(term);
        buff.append(" of ");
        buff.append(descript);
        buff.append(" items.");
        if (document != null) {
            buff.append(' ');
            buff.append(document);
        }
        getmeth.addSourceComment(buff.toString());
        getmeth.addSourceComment("");
        getmeth.addSourceComment("@return", " " + term);
        getmeth.createBlock().addReturnNamed(fname);
        
        // add the set method definition
        MethodBuilder setmeth = builder.addMethod(node.getSetMethodName(), "void");
        setmeth.setPublic();
        buff.replace(0, 3, "Set");
        setmeth.addSourceComment(buff.toString());
        setmeth.addSourceComment("");
        setmeth.addSourceComment("@param", " " + COLLECTION_VARIABLE_NAME);
        setmeth.addParameter(COLLECTION_VARIABLE_NAME, (Type)builder.clone(fieldtype));
        BlockBuilder block = setmeth.createBlock();
        generateSelectorSet(node, block, builder);
        block.addAssignVariableToField(COLLECTION_VARIABLE_NAME, fname);
        
        // process list and collection differently for binding
        Item item = node.getItem();
        if (!node.isCollection()) {
            
            // determine format conversion handling for type
            String valsername = null;
            String valdesername = null;
            String valuename = null;
            FormatElement format = (FormatElement)s_formatMap.get(type);
            boolean passctx = false;
            if (format != null) {
                valsername = format.getSerializerName();
                valdesername = format.getDeserializerName();
                if (valsername == null && !"java.lang.String".equals(type)) {
                    valuename = "toString";
                }
                passctx = "org.jibx.runtime.QName".equals(type);
            } else if (item instanceof ReferenceItem) {
                DefinitionItem def = ((ReferenceItem)item).getDefinition();
                if (def.isEnumeration()) {
                    EnumerationClassHolder genclas = (EnumerationClassHolder)def.getGenerateClass();
                    valsername = EnumerationClassHolder.CONVERTFORCE_METHOD;
                    valuename = genclas.getName() + ".toString";
                }
            } else {
                throw new IllegalStateException("Internal error - invalid list type");
            }
            
            // add list serializer method to class
            String sername = LIST_SERIALIZE_PREFIX + propname;
            MethodBuilder sermeth = builder.addMethod(sername, "java.lang.String");
            sermeth.addParameter("values", (Type)builder.clone(fieldtype));
            if (passctx) {
                SingleVariableDeclaration decl = sermeth.addParameter("ictx", "org.jibx.runtime.IMarshallingContext");
                decl.modifiers().add(decl.getAST().newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
                sermeth.addThrows("org.jibx.runtime.JiBXException");
            }
            sermeth.setPublicStatic();
            sermeth.addSourceComment("Serializer for " + descript + ' ' + term + '.');
            sermeth.addSourceComment("");
            sermeth.addSourceComment("@param", " values");
            sermeth.addSourceComment("@return", " text");
            
            // create a simple null return for null parameter string
            BlockBuilder nullblock = builder.newBlock();
            nullblock.addReturnNull();
            
            // create block for actual serialization when parameter non-null
            BlockBuilder serblock = builder.newBlock();
            NewInstanceBuilder newbuff = builder.newInstance("java.lang.StringBuffer");
            serblock.addLocalVariableDeclaration("java.lang.StringBuffer", "buff", newbuff);
            
            // create body of loop to handle the conversion
            BlockBuilder forblock = builder.newBlock();
            
            // append space to buffer unless empty
            InfixExpressionBuilder lengthexpr = builder.buildInfix(Operator.GREATER);
            lengthexpr.addOperand(builder.createNormalMethodCall("buff", "length"));
            lengthexpr.addNumberLiteralOperand("0");
            InvocationBuilder appendcall = builder.createNormalMethodCall("buff", "append");
            appendcall.addCharacterLiteralOperand(' ');
            BlockBuilder spaceblock = builder.newBlock();
            spaceblock.addExpressionStatement(appendcall);
            forblock.addIfStatement(lengthexpr, spaceblock);
            
            // load the current value from array
            if (reptype == SchemaRootBase.REPEAT_TYPED) {
                forblock.addLocalVariableDeclaration(type, "value", builder.createNormalMethodCall("iter", "next"));
            } else if (reptype == SchemaRootBase.REPEAT_LIST) {
                CastBuilder castexpr = builder.buildCast(type);
                castexpr.addOperand(builder.createNormalMethodCall("iter", "next"));
                forblock.addLocalVariableDeclaration(type, "value", castexpr);
            } else {
                forblock.addLocalVariableDeclaration(type, "value", builder.buildArrayIndexAccess("values", "index"));
            }
            
            // append the current value to the buffer
            appendcall = builder.createNormalMethodCall("buff", "append");
            if (valuename != null) {
                appendcall.addOperand(builder.createNormalMethodCall("value", valuename));
            } else if (valdesername != null) {
                InvocationBuilder sercall = builder.createStaticMethodCall(valsername);
                sercall.addVariableOperand("value");
                if (passctx) {
                    sercall.addVariableOperand("ictx");
                }
                appendcall.addOperand(sercall);
            } else {
                appendcall.addVariableOperand("value");
            }
            forblock.addExpressionStatement(appendcall);
            
            // build the for loop around the conversion
            if (reptype == SchemaRootBase.REPEAT_TYPED) {
                Type itertype = builder.createParameterizedType("java.util.Iterator", type);
                serblock.addIteratedForStatement("iter", itertype,
                    builder.createNormalMethodCall("values", "iterator"), forblock);
            } else if (reptype == SchemaRootBase.REPEAT_LIST) {
                serblock.addIteratedForStatement("iter", builder.createType("java.util.Iterator"),
                    builder.createNormalMethodCall("values", "iterator"), forblock);
            } else {
                serblock.addIndexedForStatement("index", "values", forblock);
            }
            
            // finish non-null serialization block with buffer conversion
            serblock.addReturnExpression(builder.createNormalMethodCall("buff", "toString"));
            
            // finish with the if statement that decides which to execute
            InfixExpressionBuilder iftest = builder.buildNameOp("values", Operator.EQUALS);
            iftest.addNullOperand();
            sermeth.createBlock().addIfElseStatement(iftest, nullblock, serblock);
            
            // add list deserializer method to class
            String desername = LIST_DESERIALIZE_PREFIX + propname;
            MethodBuilder desermeth = builder.addMethod(desername, (Type)builder.clone(fieldtype));
            desermeth.addParameter("text", "java.lang.String");
            if (passctx) {
                SingleVariableDeclaration decl = desermeth.addParameter("ictx",
                    "org.jibx.runtime.IUnmarshallingContext");
                decl.modifiers().add(decl.getAST().newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
            }
            desermeth.setPublicStatic();
            desermeth.addSourceComment("Deserializer for " + descript + ' ' + term + '.');
            desermeth.addSourceComment("");
            desermeth.addSourceComment("@param", " text");
            desermeth.addSourceComment("@return", " values");
            desermeth.addSourceComment("@throws", " org.jibx.runtime.JiBXException on conversion error");
            desermeth.addThrows("org.jibx.runtime.JiBXException");
            block = desermeth.createBlock();
            
            // build instance creation for anonymous inner class to handle deserialization
            NewInstanceBuilder newinst = builder.newInstance("org.jibx.runtime.IListItemDeserializer");
            ClassBuilder anonclas = newinst.addAnonymousInnerClass();
            MethodBuilder innermeth = anonclas.addMethod("deserialize", "java.lang.Object");
            if (passctx) {
                innermeth.addThrows("org.jibx.runtime.JiBXException");
            }
            innermeth.addParameter("text", "java.lang.String");
            innermeth.setPublic();
            BlockBuilder innerblock = innermeth.createBlock();
            if (valdesername == null) {
                innerblock.addReturnNamed("text");
            } else {
                InvocationBuilder desercall = builder.createStaticMethodCall(valdesername);
                desercall.addVariableOperand("text");
                if (passctx) {
                    desercall.addVariableOperand("ictx");
                }
                innerblock.addReturnExpression(desercall);
            }
            block.addLocalVariableDeclaration("org.jibx.runtime.IListItemDeserializer", "ldser", newinst);
            
            // build call using anonymous inner class to deserialize to untyped collection
            InvocationBuilder desercall = builder.createStaticMethodCall("org.jibx.runtime.Utility.deserializeList");
            desercall.addVariableOperand("text");
            desercall.addVariableOperand("ldser");
            
            // handle the return as appropriate
            if (reptype == SchemaRootBase.REPEAT_TYPED) {
                CastBuilder castexpr = builder.buildCast((Type)builder.clone(fieldtype));
                castexpr.addOperand(desercall);
                block.addReturnExpression(castexpr);
            } else if (reptype == SchemaRootBase.REPEAT_LIST) {
                block.addReturnExpression(desercall);
            } else {
                
                // save deserialization result list to local variable
                block.addLocalVariableDeclaration("java.util.List", "list", desercall);
                
                // create null return block
                BlockBuilder ifnull = builder.newBlock();
                ifnull.addReturnNull();
                
                // create non-null return with conversion to array
                BlockBuilder ifnonnull = builder.newBlock();
                InvocationBuilder toarraycall = builder.createNormalMethodCall("list", "toArray");
                NewArrayBuilder newarray = builder.newArrayBuilder(type);
                newarray.setSize(builder.createNormalMethodCall("list", "size"));
                toarraycall.addOperand(newarray);
                CastBuilder castexpr = builder.buildCast((Type)builder.clone(fieldtype));
                castexpr.addOperand(toarraycall);
                ifnonnull.addReturnExpression(castexpr);
                
                // finish with the if statement that decides which to execute
                iftest = builder.buildNameOp("list", Operator.EQUALS);
                iftest.addNullOperand();
                block.addIfElseStatement(iftest, ifnull, ifnonnull);
            }
            
        }
        
        // call decorators for added value processing
        for (int i = 0; i < m_decorators.length; i++) {
            m_decorators[i].valueAdded(basename, true, basetype, field.getDeclaration(), getmeth.getDeclaration(),
                setmeth.getDeclaration(), descript, this);
        }
    }
    
    /**
     * Add a flag property to the class. This adds the actual field definition, along with the appropriate access
     * methods.
     * 
     * @param basename
     * @param node
     * @param builder
     */
    private void addFlagProperty(String basename, DataNode node, ClassBuilder builder) {
        
        // define the flag field and method names
        String propname = node.getPropName();
        s_logger.debug("Adding property " + propname);
        String fname = m_nameConverter.toFieldName(basename);
        node.setFieldName(fname);
        node.setTestMethodName("if" + propname + "Present");
        node.setFlagMethodName("flag" + propname + "Present");
        String nonres = NameUtils.convertReserved(basename);
        
        // generate the field as a simple boolean value
        FieldBuilder field = builder.addField(fname, "boolean");
        field.setPrivate();
        
        // add test method definition
        MethodBuilder testmeth = builder.addMethod(node.getTestMethodName(), "boolean");
        testmeth.setPublic();
        StringBuffer buff = new StringBuffer();
        buff.append("Check if the ");
        String descript = describe(node);
        buff.append(descript);
        buff.append(" is present.");
        String document = findDocumentation(false, node);
        if (document != null) {
            buff.append(' ');
            buff.append(document);
        }
        testmeth.addSourceComment(buff.toString());
        testmeth.addSourceComment("");
        testmeth.addSourceComment("@return", " <code>true</code> if present, <code>false</code> if not");
        BlockBuilder block = testmeth.createBlock();
        generateSelectorCheck(node, block, builder);
        block.addReturnNamed(fname);
        
        // add the flag method definition
        MethodBuilder flagmeth = builder.addMethod(node.getFlagMethodName(), "void");
        flagmeth.setPublic();
        buff.setLength(0);
        buff.append("Set flag for ");
        buff.append(descript);
        buff.append(" present.");
        if (document != null) {
            buff.append(' ');
            buff.append(document);
        }
        flagmeth.addSourceComment(buff.toString());
        flagmeth.addSourceComment("");
        flagmeth.addSourceComment("@param", " " + nonres);
        flagmeth.addParameter(nonres, "boolean");
        block = flagmeth.createBlock();
        generateSelectorSet(node, block, builder);
        block.addAssignVariableToField(nonres, fname);
        
        // call decorators for added value processing
        for (int i = 0; i < m_decorators.length; i++) {
            m_decorators[i].valueAdded(nonres, false, "boolean", field.getDeclaration(), null, null, descript, this);
        }
    }

    /**
     * Find the schema documentation associated with a data node. If the node has documentation set and does not have a
     * separate class, this just returns the documentation from that node. Otherwise, it moves up the node tree until it
     * finds a documented node, terminating if any parent has more than one child or when it reaches the node matching
     * the root of the class data structure.
     *
     * @param top use topmost documentation found flag
     * @param node starting node
     * @return documentation
     */
    private String findDocumentation(boolean top, DataNode node) {
        
        // first check if separate class for node (in which case documentation goes with class)
        Item item = node.getItem();
        if (item instanceof GroupItem && ((GroupItem)item).getGenerateClass() != null) {
            node = node.getParent();
        }
        
        // scan for documentation which can be used for node
        String text = null;
        while (node != m_dataRoot) {
            String thisdoc = node.getDocumentation();
            if (thisdoc != null) {
                text = thisdoc;
                if (!top) {
                    break;
                }
            }
            ParentNode parent = node.getParent();
            if (parent.getChildren().size() == 1) {
                node = parent;
            } else {
                break;
            }
        }
        return text;
    }
    
    /**
     * Generate the fields and methods for a wrapper around one or more properties. This calls itself recursively to
     * handle nested wrappers.
     * 
     * @param parent
     */
    private void addToClass(ParentNode parent) {
        
        // first check if grouping requires a selector field
        ClassBuilder builder = getBuilder();
        ArrayList nodes = parent.getChildren();
        Item grpitem = parent.getItem();
        String seldesc = null;
        if (parent.isSelectorNeeded()) {
            
            // build the selector field
            String basename = grpitem.getEffectiveName();
            String fieldname = m_nameConverter.toFieldName(basename);
            parent.setSelectField(fieldname);
            builder.addIntField(fieldname, "-1").setPrivate();
            
            // build the basic description information
            AnnotatedBase comp = parent.getSchemaComponent();
            StringBuffer buff = new StringBuffer();
            if (parent.getQName() != null) {
                buff.append(parent.getQName().getName());
                buff.append(' ');
            }
            String descript;
            if (comp.type() == SchemaBase.UNION_TYPE) {
                descript = "form";
                buff.append("union");
            } else {
                descript = "choice";
                buff.append("choice");
            }
            seldesc = buff.toString();
            buff.insert(0, "Clear the ");
            buff.append(" selection.");
            
            // create constants for each alternative value
            String namesuffix = NameUtils.toNameWord(basename);
            boolean expose = parent.isSelectorExposed();
            int index = 0;
            for (int i = 0; i < nodes.size(); i++) {
                DataNode node = (DataNode)nodes.get(i);
                if (!node.isIgnored()) {
                    FieldBuilder field = builder.addIntField(node.getSelectConstName(), Integer.toString(index++));
                    if (expose) {
                        field.setPublicStaticFinal();
                        field.addSourceComment(namesuffix + " value when " + node.getItem().getEffectiveName() +
                            " is set");
                    } else {
                        field.setPrivateStaticFinal();
                    }
                }
            }
            
            // check for selector set methods used
            int seltype = parent.getSelectorType();
            
            // add the selector set method
            String selectname = "set" + namesuffix;
            String resetname = "clear" + namesuffix;
            parent.setSelectSetMethod(selectname);
            MethodBuilder setmeth = builder.addMethod(selectname, "void");
            setmeth.setPrivate();
            BlockBuilder block = setmeth.createBlock();
            setmeth.addParameter(descript, "int");
            
            // start by setting any containing selectors
            generateSelectorSet(parent, block, builder);
            
            // check for state check needed on set
            if (seltype == NestingCustomBase.SELECTION_OVERRIDESET ||
                seltype == NestingCustomBase.SELECTION_OVERRIDEBOTH) {
                
                // set overrides prior state, just set new state directly
                block.addAssignVariableToField(descript, fieldname);
                
            } else {
                
                // create the set block for when there's no current choice
                BlockBuilder assignblock = builder.newBlock();
                assignblock.addAssignVariableToField(descript, fieldname);
                
                // create the exception thrown when choice does not match current setting
                BlockBuilder throwblock = builder.newBlock();
                throwblock.addThrowException("IllegalStateException", "Need to call " + resetname
                    + "() before changing existing " + descript);
                
                // finish with the if statement that decides which to execute
                InfixExpressionBuilder iftest = builder.buildNameOp(fieldname, Operator.EQUALS);
                iftest.addNumberLiteralOperand("-1");
                InfixExpressionBuilder elsetest = builder.buildNameOp(fieldname, Operator.NOT_EQUALS);
                elsetest.addVariableOperand(descript);
                block.addIfElseIfStatement(iftest, elsetest, assignblock, throwblock);
                
            }
            
            // check for state check needed on get
            if (seltype == NestingCustomBase.SELECTION_CHECKEDBOTH ||
                seltype == NestingCustomBase.SELECTION_OVERRIDEBOTH) {
                
                // add the selector check method
                String checkname = "check" + namesuffix;
                parent.setSelectCheckMethod(checkname);
                MethodBuilder checkmeth = builder.addMethod(checkname, "void");
                checkmeth.setPrivate();
                block = checkmeth.createBlock();
                checkmeth.addParameter(descript, "int");
                
                // start by setting any containing selectors
                generateSelectorCheck(parent, block, builder);
                
                // create the exception thrown when current state is wrong
                BlockBuilder throwblock = builder.newBlock();
                InfixExpressionBuilder strcat = builder.buildStringConcatenation("State mismatch when accessing " +
                    descript + " value: current state is ");
                strcat.addVariableOperand(fieldname);
                throwblock.addThrowException("IllegalStateException", strcat);
                
                // finish with the if statement that decides which to execute
                InfixExpressionBuilder unequaltest = builder.buildNameOp(fieldname, Operator.NOT_EQUALS);
                unequaltest.addVariableOperand(descript);
                InfixExpressionBuilder unsettest = builder.buildNameOp(fieldname, Operator.NOT_EQUALS);
                unsettest.addNumberLiteralOperand("-1");
                InfixExpressionBuilder iftest = builder.buildInfix(Operator.CONDITIONAL_AND);
                iftest.addOperand(unequaltest);
                iftest.addOperand(unsettest);
                block.addIfStatement(iftest, throwblock);
                
            }
            
            // add selector clear method (public, so documented)
            MethodBuilder resetmeth = builder.addMethod(resetname, "void");
            resetmeth.setPublic();
            resetmeth.addSourceComment(buff.toString());
            block = resetmeth.createBlock();
            block.addAssignToName(block.numberLiteral("-1"), fieldname);
            
            // add state check method if needed
            if (expose) {
                MethodBuilder statemethod = builder.addMethod("state" + namesuffix, "int");
                statemethod.setPublic();
                statemethod.addSourceComment("Get the current " + seldesc + " state.");
                statemethod.addSourceComment("@return", " state");
                statemethod.createBlock().addReturnNamed(fieldname);
            }
            
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Created selector for grouping component "
                    + SchemaUtils.describeComponent(grpitem.getSchemaComponent()) + " in class " + getFullName());
            }
        }
        
        // generate all values in group
        for (int i = 0; i < nodes.size(); i++) {
            
            // check if there's a separate value for this child node
            DataNode node = (DataNode)nodes.get(i);
            Item item = node.getItem();
            if (!node.isIgnored() && !item.isImplicit()) {
                
                // get the base name and property name to be used for item
                boolean repeat = node.isCollection() || node.isList();
                String basename = item.getEffectiveName();
                String workingname = basename;
                if (repeat) {
                    String plural = m_nameConverter.pluralize(workingname);
                    if (!plural.equals(workingname)) {
                        workingname = m_nameSet.add(plural);
                    }
                }
                String propname = NameUtils.toNameWord(workingname);
                node.setPropName(propname);
                
                // generate test method, if inside selector group
                checkIfMethod(node, seldesc, propname, builder);
                
                // handle wrapper or simple property as appropriate
                if (node.isInterior()) {
                    ParentNode nestedparent = (ParentNode)node;
                    if (nestedparent.isCollection()) {
                        
                        // add property matching this node
                        addRepeatedProperty(basename, nestedparent, builder);
                        
                    } else if (nestedparent.getChildren().size() == 0) {
                        if (nestedparent.isOptional() || parent.isSelectorNeeded()) {
                            
                            // empty branch of tree, just add a boolean flag to indicate presence
                            addFlagProperty(basename, nestedparent, builder);
                            
                        }
                    } else {
                        
                        // just process nesting directly
                        addToClass(nestedparent);
                    }
                    
                } else {
                    
                    // add property matching this node
                    if (repeat) {
                        addRepeatedProperty(basename, node, builder);
                    } else {
                        addSimpleProperty(basename, node, builder);
                    }
                }
            }
        }
    }
    
    /**
     * Build the binding structure element for a reference to a class.
     * 
     * @param leaf reference node
     * @param def target definition
     * @param single flag for single child
     * @param holder holder for binding definition
     * @param bindcomp containing binding element
     * @return binding structure
     */
    private StructureElement addReferenceStructure(LeafNode leaf, DefinitionItem def, boolean single,
        BindingHolder holder, ContainerElementBase bindcomp) {
        
        // first check if a new structure element is needed in the binding for the reference
        StructureElement struct = null;
        boolean keep = false;
        int type = leaf.getSchemaComponent().type();
        if (single && bindcomp instanceof StructureElement) {
            struct = (StructureElement)bindcomp;
            keep = struct.getGetName() == null && struct.getSetName() == null && struct.getFieldName() == null &&
                !struct.isChoice() && struct.getDeclaredType() == null && struct.getMapAsQName() == null &&
                (struct.getName() == null || (type != SchemaBase.ATTRIBUTE_TYPE && type != SchemaBase.ELEMENT_TYPE));
        }
        if (!keep) {
            struct = new StructureElement();
        }
        if (def.getSchemaComponent().type() == SchemaBase.ELEMENT_TYPE) {
            
            // element definition reference, invoke concrete mapping by class name
            struct.setDeclaredType(leaf.getBindingType());
            
        } else {
            
            // not an element reference, so map by abstract mapping 'type' name (which may or may not be a type)
            QName qname = def.getQName();
            String uri = qname.getUri();
            if (uri != null) {
                holder.addTypeNameReference(uri, def.getSchemaComponent().getSchema());
            }
            struct.setMapAsQName(qname);
            
        }
        return struct;
    }

    /**
     * Create a new <b>collection</b> element for the binding. This initializes the create and declared types of the
     * collection as appropriate, along with the wrapper name and optional status.
     * 
     * @param wrapname name to be used for wrapper collection or structure, <code>null</code> if none
     * @param wrapopt wrapper element optional flag (should be <code>false</code> if wrapname is <code>null</code>)
     * @param holder binding definition tracking information
     * @param node data node associated with collection
     * @return collection element
     */
    private CollectionElement newCollection(QName wrapname, boolean wrapopt, BindingHolder holder, DataNode node) {
        CollectionElement collect = new CollectionElement();
        int reptype = getSchemaCustom().getRepeatType();
        boolean list = reptype == SchemaRootBase.REPEAT_LIST || reptype == SchemaRootBase.REPEAT_TYPED;
        if (list) {
            collect.setCreateType(m_listImplClass);
        }
        if (wrapname == null) {
            if (node.isOptional()) {
                collect.setUsage(PropertyAttributes.OPTIONAL_USAGE);
            }
        } else {
            setName(wrapname, holder, collect);
            if (wrapopt) {
                collect.setUsage(PropertyAttributes.OPTIONAL_USAGE);
            }
        }
        return collect;
    }

    /**
     * Set serializer/deserializer options for a &lt;value> component of the binding. If the item defining the item is a
     * reference, this uses the definition type name as the format and makes sure the definition namespace is defined
     * within the binding being generated. If the item defining the item is a builtin type, this sets the format and/or
     * serializer/deserializer methods based on the type definition.
     *
     * @param item
     * @param value
     * @param holder
     */
    private void setValueHandlingOptions(Item item, ValueElement value, BindingHolder holder) {
        if (item instanceof ReferenceItem) {
            ReferenceItem refitem = (ReferenceItem)item;
            DefinitionItem defitem = refitem.getDefinition();
            QName qname = defitem.getQName();
            if (qname != null) {
                value.setFormatQName(qname);
                String uri = qname.getUri();
                if (uri != null) {
                    holder.addTypeNameReference(uri, defitem.getSchemaComponent().getSchema());
                }
            }
        } else if (item instanceof ValueItem) {
            ValueItem valitem = (ValueItem)item;
            JavaType jtype = valitem.getType();
            value.setFormatName(jtype.getFormat());
            value.setSerializerName(jtype.getSerializerMethod());
            value.setDeserializerName(jtype.getDeserializerMethod());
        }
    }

    /**
     * Generate the binding for a parent node of the data structure tree. This calls itself recursively to handle nested
     * subtrees.
     * 
     * TODO: This needs a more structured approach to creating the binding, which probably involves trying to merge the
     * binding components down a particular branch of the tree as long as there's only one child (creating new
     * structures as needed when the child has a name and there's already a name, or the child has a property and
     * there's already a property, etc.)
     * 
     * @param parent node to be added to binding
     * @param wrapname name to be used for wrapper collection or structure, <code>null</code> if none
     * @param wrapopt wrapper element optional flag (should be <code>false</code> if wrapname is <code>null</code>)
     * @param single parent node binding component can be modified by child flag (single path from parent)
     * @param bindcomp binding definition component corresponding to the parent node
     * @param holder binding definition tracking information
     */
    private void addToBinding(ParentNode parent, QName wrapname, boolean wrapopt, boolean single,
        ContainerElementBase bindcomp, BindingHolder holder) {
        
        // generate for each child node in turn
        ArrayList children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            DataNode child = (DataNode)children.get(i);
            Item item = child.getItem();
            String propname = child.getPropName();
            if (child.isIgnored()) {
                
                // create structure for element to be ignored in unmarshalling
                StructureElement struct = new StructureElement();
                setName(child.getQName(), holder, struct);
                struct.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                bindcomp.addChild(struct);
                
            } else if (item.isImplicit()) {
                
                // implicit item uses superclass data, not a field, so just handle with map-as structure
                DefinitionItem def = ((ReferenceItem)item).getDefinition();
                QName qname = def.getQName();
                AnnotatedBase comp = def.getSchemaComponent();
                int type = comp.type();
                StructureElement struct = new StructureElement();
                if (type == SchemaBase.ELEMENT_TYPE) {
                    
                    // reference to global element definition, just set it directly as concrete mapping reference
                    struct.setMapAsName(getSuperClass().getBindingName());
                    
                } else {
                    
                    // set reference to abstract mapping without name, since this is an implicit reference
                    String uri = qname.getUri();
                    if (uri != null) {
                        holder.addTypeNameReference(uri, def.getSchemaComponent().getSchema());
                    }
                    struct.setMapAsQName(qname);
                    
                }
                bindcomp.addChild(struct);
                
            } else if (child instanceof ParentNode) {
                
                // set up for binding generation alternatives
                ParentNode subparent = (ParentNode)child;
                boolean empty = subparent.getChildren().size() == 0;
                boolean recurse = true;
                ContainerElementBase wrapcomp = bindcomp;
                StructureElementBase newcomp = null;
                QName newname = null;
                boolean newopt = false;
                if (subparent.isCollection() && !empty) {
                    
                    // always create a new <collection> element for the binding to match a collection parent node
                    CollectionElement collect = newCollection(wrapname, wrapopt, holder, subparent);
                    wrapcomp.addChild(collect);
                    newcomp = collect;
                    newname = subparent.getQName();
                    
                } else {
                    
                    // check for wrapper <structure> element needed (with nested name, or multiple values, or all)
                    boolean all = item.getSchemaComponent().type() == SchemaBase.ALL_TYPE;
                    boolean multi = subparent.getChildren().size() > 1;
                    if ((wrapname != null && (subparent.isNamed() || multi)) || (all && multi)) {
                        StructureElement struct = new StructureElement();
                        struct.setOrdered(!all);
                        setName(wrapname, holder, struct);
                        if (wrapopt) {
                            struct.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                        }
                        wrapcomp.addChild(struct);
                        wrapcomp = struct;
                        if (!empty && bindcomp.type() == ElementBase.COLLECTION_ELEMENT &&
                            getSchemaCustom().getRepeatType() != SchemaRootBase.REPEAT_ARRAY) {
                            
                            // dig into child node(s) to find the item type
                            DataNode nested = subparent;
                            String type = null;
                            while (((ParentNode)nested).getChildren().size() > 0) {
                                nested = (DataNode)((ParentNode)nested).getChildren().get(0);
                                if (nested.isCollection() || nested instanceof LeafNode) {
                                    type = nested.getBindingType();
                                    break;
                                }
                            }
                            struct.setDeclaredType(type);
                            
                        }
                    } else {
                        newname = wrapname;
                        newopt = wrapopt;
                    }
                    
                    // check for name associated with this node
                    if (subparent.isNamed()) {
                        
                        // check if this is an attribute
                        AnnotatedBase comp = item.getSchemaComponent();
                        if (comp.type() == SchemaBase.ATTRIBUTE_TYPE) {
                            
                            // handle attribute with embedded definition
                            ValueElement value = new ValueElement();
                            value.setEffectiveStyle(NestingAttributes.ATTRIBUTE_STYLE);
                            setName(subparent.getQName(), holder, value);
                            DataNode nested = subparent;
                            while ((nested = (DataNode)((ParentNode)nested).getChildren().get(0)).isInterior());
                            value.setGetName(((LeafNode)nested).getGetMethodName());
                            value.setSetName(((LeafNode)nested).getSetMethodName());
                            setValueHandlingOptions(item, value, holder);
                            wrapcomp.addChild(value);
                            if (SchemaUtils.isOptionalAttribute((AttributeElement)comp)) {
                                value.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                            }
                            if (nested.isList()) {
                                String nestname = nested.getPropName();
                                value.setSerializerName(getBindingName() + '.' + LIST_SERIALIZE_PREFIX + nestname);
                                value.setDeserializerName(getBindingName() + '.' + LIST_DESERIALIZE_PREFIX + nestname);
                            } else if (getSchemaCustom().isForceTypes()) {
                                value.setDeclaredType(nested.getBindingType());
                            }
                            recurse = false;
                            
                        } else if (subparent.getChildren().size() == 1) {
                            
                            // wrapper for an embedded structure or value, just pass name
                            newname = subparent.getQName();
                            newopt = subparent.isOptional();
                            
                        } else {
                            
                            // create a <structure> element, using the name supplied
                            StructureElement struct = new StructureElement();
                            setName(subparent.getQName(), holder, struct);
                            wrapcomp.addChild(struct);
                            newcomp = struct;
                            newname = null;
                            newopt = false;
                            
                        }
                    }
                }
                
                // set 'if' method and optional if inside a choice
                if (parent.isSelectorNeeded()) {
                    if (newcomp == null) {
                        newcomp = new StructureElement();
                        setName(newname, holder, newcomp);
                        newname = null;
                        wrapcomp.addChild(newcomp);
                    }
                    newcomp.setTestName("if" + child.getSelectPropName());
                    newcomp.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                    newopt = false;
                }
                
                // handle parent with no children as flag-only value
                if (empty) {
                    
                    // make sure there's a structure element
                    StructureElementBase struct = newcomp;
                    if (struct == null) {
                        
                        // create a <structure> element, using the wrapping name supplied
                        if (newname == null) {
                            throw new IllegalStateException("Internal error - no wrapping name for empty structure");
                        } else {
                            struct = new StructureElement();
                            setName(newname, holder, struct);
                            wrapcomp.addChild(struct);
                            newcomp = struct;
                        }
                    }
                    
                    // set flag and test methods on structure
                    struct.setFlagName(subparent.getFlagMethodName());
                    struct.setTestName(subparent.getTestMethodName());
                    setStructureOptional(subparent, newopt, struct);
                    
                } else {
                    
                    // add choice handling for this structure
                    if (subparent.isSelectorNeeded()) {
                        if (newcomp == null) {
                            newcomp = new StructureElement();
                            setStructureOptional(subparent, false, newcomp);
                            wrapcomp.addChild(newcomp);
                        }
                        newcomp.setChoice(true);
                        newcomp.setOrdered(false);
                    }
                    
                    // check for new binding component created for this node
                    if (recurse) {
                        if (newcomp == null) {
                            addToBinding(subparent, newname, newopt, single && children.size() == 1, wrapcomp, holder);
                        } else {
                            newcomp.setGetName(subparent.getGetMethodName());
                            newcomp.setSetName(subparent.getSetMethodName());
                            if (getSchemaCustom().isForceTypes()) {
                                newcomp.setDeclaredType(subparent.getBindingType());
                            }
                            addToBinding(subparent, newname, newopt, true, newcomp, holder);
                        }
                    }
                    
                }
                
            } else {
                LeafNode leaf = (LeafNode)child;
                String gname = leaf.getGetMethodName();
                String sname = leaf.getSetMethodName();
                if (leaf.isAny()) {
                    
                    // add structure binding with details determined by xs:any handling
                    int anytype = item.getComponentExtension().getAnyType();
                    StructureElementBase struct = (leaf.isCollection() && anytype != NestingCustomBase.ANY_DOM) ?
                        (StructureElementBase)new CollectionElement() : (StructureElementBase)new StructureElement();
                    String mapper;
                    switch (anytype) {
                        
                        case NestingCustomBase.ANY_DISCARD:
                            
                            // use discard mapper to skip past arbitrary element(s) when unmarshalling
                            mapper = leaf.isCollection() ? "org.jibx.extras.DiscardListMapper" :
                                "org.jibx.extras.DiscardElementMapper";
                            struct.setDeclaredType("java.lang.Object");
                            gname = sname = null;
                            break;
                            
                        case NestingCustomBase.ANY_DOM:
                            
                            // use DOM mapper to marshal/unmarshal arbitrary element(s)
                            mapper = leaf.isCollection() ? "org.jibx.extras.DomListMapper" :
                                "org.jibx.extras.DomElementMapper";
                            break;
                            
                        case NestingCustomBase.ANY_MAPPED:
                            
                            // create item <structure> child for case of list, otherwise just handle directly 
                            mapper = null;
                            if (leaf.isCollection()) {
                                StructureElement itemstruct = new StructureElement();
                                itemstruct.setDeclaredType("java.lang.Object");
                                struct.addChild(itemstruct);
                                struct.setCreateType(m_listImplClass);
                            }
                            break;
                            
                        default:
                            throw new IllegalStateException("Internal error - unknown xs:any handling");
                        
                    }
                    if (leaf.isOptional()) {
                        struct.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                    }
                    struct.setGetName(gname);
                    struct.setSetName(sname);
                    struct.setMarshallerName(mapper);
                    struct.setUnmarshallerName(mapper);
                    bindcomp.addChild(struct);
                    
                } else {
                    
                    // set the names to be used for value
                    if (leaf.isCollection() || leaf.isList()) {
                        
                        // process list and collection differently for binding
                        if (leaf.isCollection()) {
                            
                            // create a new collection element
                            CollectionElement collect = newCollection(wrapname, wrapopt, holder, leaf);
                            bindcomp.addChild(collect);
                            
                            // fill in the collection details
                            collect.setGetName(gname);
                            collect.setSetName(sname);
                            if (parent.isSelectorNeeded()) {
                                collect.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                                collect.setTestName("if" + leaf.getSelectPropName());
                            }
                            int reptype = getSchemaCustom().getRepeatType();
                            if (reptype == SchemaRootBase.REPEAT_LIST || reptype == SchemaRootBase.REPEAT_TYPED) {
                                collect.setCreateType(m_listImplClass);
                                if (gname == null) {
                                    collect.setDeclaredType(COLLECTION_VARIABLE_TYPE);
                                }
                            }
                            
                            // check the content (if any) for <collection>
                            boolean usevalue = true;
                            String usetype = leaf.getType();
                            if (item instanceof ReferenceItem) {
                                DefinitionItem def = ((ReferenceItem)item).getDefinition();
                                TypeData defclas = def.getGenerateClass();
                                if (defclas.isSimpleValue()) {
                                    usetype = defclas.getBindingName();
                                } else {
                                    
                                    // reference to mapped class, configure <collection> to handle it properly
                                    usevalue = false;
                                    if (def.getSchemaComponent().type() == SchemaBase.ELEMENT_TYPE) {
                                        
                                        // must be a non-abstract <mapping>, so use it directly
                                        collect.setItemTypeName(defclas.getBindingName());
                                        
                                    } else {
                                        
                                        // abstract mapping reference, create child <structure> with map-as type
                                        StructureElement struct = new StructureElement();
                                        QName qname = def.getQName();
                                        String uri = qname.getUri();
                                        if (uri != null) {
                                            holder.addTypeNameReference(uri, def.getSchemaComponent().getSchema());
                                        }
                                        struct.setMapAsQName(qname);
                                        if (leaf.isNamed()) {
                                            setName(leaf.getQName(), holder, struct);
                                        }
                                        collect.addChild(struct);
                                        
                                    }
                                    
                                }
                                
                            } else if (item instanceof GroupItem) {
                                
                                // handle group directly if a structure class, else just as <value>
                                TypeData groupclas = ((GroupItem)item).getGenerateClass();
                                if (groupclas.isSimpleValue()) {
                                    usetype = groupclas.getBindingName();
                                } else {
                                    
                                    // add <structure> element to be filled in by inner class generation
                                    usevalue = false;
                                    StructureClassHolder classholder = ((StructureClassHolder)groupclas);
                                    StructureElement struct = new StructureElement();
                                    struct.setDeclaredType(classholder.getBindingName());
                                    setName(leaf.getQName(), holder, struct);
                                    
                                    // set component for dependent class generation
                                    classholder.setBinding(struct);
                                    collect.addChild(struct);
                                    
                                }
                                
                            }
                            if (usevalue) {
                                
                                // add <value> element to collection for simple (primitive or text) value
                                ValueElement value = new ValueElement();
                                value.setEffectiveStyle(NestingAttributes.ELEMENT_STYLE);
                                if (leaf.isNamed()) {
                                    setName(leaf.getQName(), holder, value);
                                }
                                setValueHandlingOptions(item, value, holder);
                                value.setDeclaredType(usetype);
                                collect.addChild(value);
                                
                            }
                            
                        } else {
                            
                            // handle list serialization and deserialization directly
                            ValueElement value = buildValueBinding(leaf, null, gname, sname, holder);
                            value.setSerializerName(getBindingName() + '.' + LIST_SERIALIZE_PREFIX + propname);
                            value.setDeserializerName(getBindingName() + '.' + LIST_DESERIALIZE_PREFIX + propname);
                            bindcomp.addChild(value);
                            
                        }
                        
                    } else {
                        
                        // add <structure> wrapper if name passed in with name on value
                        ContainerElementBase contain = bindcomp;
                        boolean consing = single && children.size() == 1;
                        if (wrapname != null && (leaf.isNamed() || leaf.isReference())) {
                            StructureElement struct = new StructureElement();
                            setName(wrapname, holder, struct);
                            String type = leaf.getBindingType();
                            if (gname == null) {
                                struct.setDeclaredType(type);
                            } else if (!Types.isSimpleValue(type)) {
                                
                                // apply access methods to wrapper only if this is a complex value
                                struct.setGetName(gname);
                                struct.setSetName(sname);
                                gname = sname = null;
                                if (getSchemaCustom().isForceTypes()) {
                                    struct.setDeclaredType(type);
                                }
                                
                            }
                            if (wrapopt) {
                                struct.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                            }
                            contain.addChild(struct);
                            contain = struct;
                            consing = true;
                            wrapname = null;
                            wrapopt = false;
                        }
                        
                        // build the appropriate binding representation
                        StructureElement struct = null;
                        if (item instanceof ReferenceItem) {
                            
                            // handle reference directly if a structure class, else just as value
                            DefinitionItem def = ((ReferenceItem)item).getDefinition();
                            TypeData defclas = def.getGenerateClass();
                            if (!defclas.isSimpleValue()) {
                                struct = addReferenceStructure(leaf, def, consing, holder, contain);
                            }
                            
                        } else if (item instanceof GroupItem) {
                            
                            // handle group directly if a structure class, else just as value
                            TypeData groupclas = ((GroupItem)item).getGenerateClass();
                            if (!groupclas.isSimpleValue()) {
                                
                                // create a new <structure> element for reference
                                struct = new StructureElement();
                                StructureClassHolder refclas = (StructureClassHolder)groupclas;
                                if (gname == null) {
                                    struct.setDeclaredType(refclas.getBindingName());
                                }
                                
                                // set the binding component to be filled in by inner class generation
                                refclas.setBinding(struct);
                                
                            }
                            
                        }
                        if (struct == null) {
                            
                            // add simple <value> binding for field
                            ValueElement value = buildValueBinding(leaf, wrapname, gname, sname, holder);
                            if (getSchemaCustom().isForceTypes()) {
                                value.setDeclaredType(leaf.getBindingType());
                            }
                            if (wrapopt) {
                                value.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                            }
                            contain.addChild(value);
                            
                        } else {
                            
                            // fill in structure attributes
                            struct.setGetName(gname);
                            struct.setSetName(sname);
                            if (getSchemaCustom().isForceTypes()) {
                                struct.setDeclaredType(leaf.getBindingType());
                            }
                            
                            // set the name and optional status from wrapper or item
                            setName(wrapname == null ? leaf.getQName() : wrapname, holder, struct);
                            setStructureOptional(leaf, wrapopt, struct);
                            
                            // common <choice> handling for structure
                            if (parent.isSelectorNeeded()) {
                                struct.setUsage(PropertyAttributes.OPTIONAL_USAGE);
                                struct.setTestName("if" + leaf.getSelectPropName());
                            }
                            
                            // add to containing binding component
                            if (struct != contain) {
                                contain.addChild(struct);
                            }
                            
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate this class.
     * 
     * @param verbose 
     * @param builder class source file builder
     */
    public void generate(boolean verbose, SourceBuilder builder) {
        
        // setup the class builder
        String name = getName();
        ClassBuilder clasbuilder;
        if (m_outerClass == null) {
            clasbuilder = builder.newMainClass(name, false);
        } else {
            clasbuilder = builder.newInnerClass(name, m_outerClass.getBuilder(), false);
        }
        
        // handle the common initialization
        initClass(verbose, clasbuilder, m_dataRoot);
        
        // add nested <format> definitions to <mapping>
        if (m_bindingElement instanceof MappingElement) {
            addInnerFormats((MappingElementBase)m_bindingElement);
        }
        
        // add choice handling if needed
        if (m_dataRoot.isSelectorNeeded()) {
            m_bindingElement.setChoice(true);
            m_bindingElement.setOrdered(false);
        }
        
        // fix all the value names
        String fullname = getFullName();
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Generating class " + fullname + ":\n" + m_dataRoot.describe(0));
        }
        addFixedNames(m_dataRoot);
        fixFlexibleNames(m_dataRoot, false);
        
        // make class abstract if appropriate
        AnnotatedBase comp = m_dataRoot.getSchemaComponent();
        boolean abs = false;
        int type = comp.type();
        if (type == SchemaBase.ELEMENT_TYPE) {
            abs = ((ElementElement)comp).isAbstract();
        } else if (type == SchemaBase.COMPLEXTYPE_TYPE) {
            abs = ((ComplexTypeElement)comp).isAbstract();
        }
        if (abs) {
            getBuilder().setAbstract();
        }
        
        // generate the class data structure
        addToClass(m_dataRoot);
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Class " + fullname + " after fields generated:\n" + m_dataRoot.describe(0));
        }
        addToBinding(m_dataRoot, null, false, false, m_bindingElement, m_holder);
        
        // generate the binding code
        // m_classBuilder.addInterface("org.jibx.v2.MappedStructure");
        
        // finish with subclass generation
        generateInner(verbose, builder);
        finishClass(m_bindingElement);
        
        // check for empty <format> elements to be deleted from <mapping>
        if (m_bindingElement instanceof MappingElement) {
            MappingElementBase mapping = (MappingElementBase)m_bindingElement;
            ArrayList childs = mapping.topChildren();
            int fill = 0;
            for (int i = 0; i < childs.size(); i++) {
                Object child = childs.get(i);
                boolean keep = true;
                if (child instanceof FormatElement) {
                    FormatElement format = (FormatElement)child;
                    keep = format.getDefaultText() != null || format.getDeserializerName() != null ||
                        format.getEnumValueName() != null || format.getSerializerName() != null;
                }
                if (keep) {
                    childs.set(fill++, child);
                }
            }
            while (fill < childs.size()) {
                childs.remove(childs.size()-1);
            }
        }
    }
}
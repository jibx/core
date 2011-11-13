/*
Copyright (c) 2004-2009, Dennis M. Sosnoski.
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
import java.util.HashSet;

import org.jibx.binding.util.ObjectStack;
import org.jibx.util.IClass;
import org.jibx.util.IClassLocator;

/**
 * Handles walking the tree structure of a binding model, tracking
 * order-dependent state information collected along the way.
 *
 * @author Dennis M. Sosnoski
 */
public class TreeContext
{
    /** Global definition context (outside of binding). */
    private DefinitionContext m_globalContext;
    
    /** Binding element model root (may be <code>null</code>, if not configured
     by caller). */
    private BindingElement m_bindingRoot;
    
    /** Stack of items for parent hierarchy to current node in tree. */
    private ObjectStack m_treeHierarchy;
    
    /** Class locator set by environment code. */
    private IClassLocator m_locator;
    
    /** Set of elements to be skipped in walking tree. */
    private HashSet m_skipSet;
    
    /**
     * Internal null constructor.
     */
    private TreeContext() {}
    
    /**
     * Constructor.
     * 
     * @param iloc class locator to be used
     */
    public TreeContext(IClassLocator iloc) {
        m_treeHierarchy = new ObjectStack();
        m_locator = iloc;
        m_skipSet = new HashSet();
    }
    
    /**
     * Get a secondary context for the same tree as this instance. The secondary
     * context shares the same skip set, context, and binding root as the
     * original context. This allows activites invoked during the touring of the
     * original tree to start subtours of their own part of the tree.
     *
     * @return new context linked to original context
     */
    public TreeContext getChildContext() {
        TreeContext child = new TreeContext();
        child.m_bindingRoot = m_bindingRoot;
        child.m_globalContext = m_globalContext;
        child.m_locator = m_locator;
        child.m_skipSet = m_skipSet;
        child.m_treeHierarchy = new ObjectStack(m_treeHierarchy);
        return child;
    }
    
    /**
     * Set the global definition context. This context is external to the actual
     * binding definition, providing defaults that can be overridden by values
     * set within the actual binding.
     * 
     * @param dctx global definition context
     */
    public void setGlobalDefinitions(DefinitionContext dctx) {
        m_globalContext = dctx;
    }
    
    /**
     * Tour complete binding model tree. This tours the entire binding model,
     * starting from the root binding element. Using this method automatically
     * sets the root binding element for access by processing performed during
     * the tour. It <b>must</b> be used for the binding element in order to
     * handle included binding definitions properly.
     * 
     * @param root binding element root of tree
     * @param visitor target visitor for element notifications
     */
    public void tourTree(BindingElement root, ModelVisitor visitor) {
        
        // set up binding root reference for access during processing
        BindingElement hold = m_bindingRoot;
        m_bindingRoot = root;
        
        // run the actual tour
        tourTree((ElementBase)root, visitor);
        
        // restore prior binding root reference
        m_bindingRoot = hold;
    }
    
    /**
     * Tour binding model tree. This recursively traverses the binding model
     * tree rooted in the supplied element, notifying the visitor of each
     * element visited during the traversal. Elements with fatal errors are
     * skipped in processing, along with all child elements. The method may
     * itself be called recursively.
     * 
     * @param root node of tree to be toured
     * @param visitor target visitor for element notifications
     */
    public void tourTree(ElementBase root, ModelVisitor visitor) {
        
        // check for fatal error on element
        if (m_skipSet.contains(root)) {
            return;
        }
        
        // visit the actual root of tree
        boolean expand = false;
        m_treeHierarchy.push(root);
        switch (root.type()) {
            
            case ElementBase.BINDING_ELEMENT:
                expand = visitor.visit((BindingElement)root);
                break;
                
            case ElementBase.COLLECTION_ELEMENT:
                expand = visitor.visit((CollectionElement)root);
                break;
                
            case ElementBase.FORMAT_ELEMENT:
                visitor.visit((FormatElement)root);
                break;
                
            case ElementBase.INCLUDE_ELEMENT:
                expand = visitor.visit((IncludeElement)root);
                break;
                
            case ElementBase.INPUT_ELEMENT:
                expand = visitor.visit((InputElement)root);
                break;
                
            case ElementBase.MAPPING_ELEMENT:
                if (root instanceof MappingElement) {
                    expand = visitor.visit((MappingElement)root);
                } else {
                    expand = visitor.visit((PrecompiledMappingElement)root);
                }
                break;
                
            case ElementBase.NAMESPACE_ELEMENT:
                visitor.visit((NamespaceElement)root);
                break;
                
            case ElementBase.OUTPUT_ELEMENT:
                expand = visitor.visit((OutputElement)root);
                break;
                
            case ElementBase.SPLIT_ELEMENT:
                expand = visitor.visit((SplitElement)root);
                break;
                
            case ElementBase.STRUCTURE_ELEMENT:
                expand = visitor.visit((StructureElement)root);
                break;
                
            case ElementBase.TEMPLATE_ELEMENT:
                expand = visitor.visit((TemplateElement)root);
                break;
                
            case ElementBase.VALUE_ELEMENT:
                visitor.visit((ValueElement)root);
                break;
            
            default:
                throw new IllegalStateException
                    ("Internal error: unknown element type");
                
        }
        
        // check for expansion needed
        if (expand && !m_skipSet.contains(root)) {
            if (root instanceof IncludeElement) {
                
                // include just delegates to the included binding element
                BindingElement binding = ((IncludeElement)root).getBinding();
                if (binding != null) {
                    m_treeHierarchy.pop();
                    tourTree((ElementBase)binding, visitor);
                    m_treeHierarchy.push(root);
                }
                
            } else if (root instanceof NestingElementBase) {
                
                // process each container child as root of own tree
                ArrayList childs = null;
                if (root instanceof MappingElement) {
                    childs = ((MappingElementBase)root).topChildren();
                    for (int i = 0; i < childs.size(); i++) {
                        tourTree((ElementBase)childs.get(i), visitor);
                    }
                }
                if (root instanceof BindingElement) {
                    childs = ((BindingElement)root).topChildren();
                } else {
                    childs = ((NestingElementBase)root).children();
                }
                for (int i = 0; i < childs.size(); i++) {
                    tourTree((ElementBase)childs.get(i), visitor);
                }
            }
        }
        
        // exit the actual root of tree
        switch (root.type()) {
            
            case ElementBase.BINDING_ELEMENT:
                visitor.exit((BindingElement)root);
                break;
                
            case ElementBase.COLLECTION_ELEMENT:
                visitor.exit((CollectionElement)root);
                break;
                
            case ElementBase.INCLUDE_ELEMENT:
                visitor.exit((IncludeElement)root);
                break;
                
            case ElementBase.INPUT_ELEMENT:
                visitor.exit((InputElement)root);
                break;
                
            case ElementBase.MAPPING_ELEMENT:
                if (root instanceof MappingElement) {
                    visitor.exit((MappingElement)root);
                } else {
                    visitor.exit((PrecompiledMappingElement)root);
                }
                break;
                
            case ElementBase.OUTPUT_ELEMENT:
                visitor.exit((OutputElement)root);
                break;
                
            case ElementBase.SPLIT_ELEMENT:
                visitor.exit((SplitElement)root);
                break;
                
            case ElementBase.STRUCTURE_ELEMENT:
                visitor.exit((StructureElement)root);
                break;
                
            case ElementBase.TEMPLATE_ELEMENT:
                visitor.exit((TemplateElement)root);
                break;
                
            case ElementBase.VALUE_ELEMENT:
                visitor.exit((ValueElement)root);
                break;
            
            default:
                break;
                
        }
        m_treeHierarchy.pop();
    }
    
    /**
     * Get depth of nesting in binding.
     * 
     * @return nesting depth
     */
    public int getNestingDepth() {
        return m_treeHierarchy.size();
    }
    
    /**
     * Peek current element of hierarchy.
     * 
     * @return current element
     */
    protected ElementBase peekElement() {
        return (ElementBase)m_treeHierarchy.peek();
    }
    
    /**
     * Check if a component is being skipped due to a fatal error.
     *
     * @param obj component to be checked
     * @return flag for component being skipped
     */
    public boolean isSkipped(Object obj) {
        return m_skipSet.contains(obj);
    }
    
    /**
     * Add element to set to be skipped.
     * 
     * @param skip
     */
    protected void addSkip(Object skip) {
        if (skip instanceof ElementBase) {
            m_skipSet.add(skip);
        }
    }
    
    /**
     * Get root element of binding.
     * 
     * @return root element of binding
     * @throws IllegalStateException if no root element known
     */
    public BindingElement getBindingRoot() {
        if (m_bindingRoot == null) {
            throw new IllegalStateException("No binding root defined");
        } else {
            return m_bindingRoot;
        }
    }
    
    /**
     * Set root element of binding. This should be called by the user if an
     * element other than the binding element is going to be used as the root
     * for a tour.
     * 
     * @param root root element of binding
     */
    public void setBindingRoot(BindingElement root) {
        m_bindingRoot = root;
    }
    
    /**
     * Get containing element. This is equivalent to the generation
     * <code>1</code> parent, except that it checks for the case where there's
     * no parent present.
     * 
     * @return binding definition component for parent element, or
     * <code>null</code> if no parent
     */
    public NestingElementBase getParentElement() {
        if (m_treeHierarchy.size() > 1) {
            return (NestingElementBase)m_treeHierarchy.peek(1);
        } else {
            return null;
        }
    }
    
    /**
     * Get containing element at generation level. All except the zero-level
     * containing element are guaranteed to be instances of {@link
     * org.jibx.binding.model.NestingElementBase}.
     * 
     * @param level generation level of parent
     * @return binding definition component for parent at level
     */
    public ElementBase getParentElement(int level) {
        return (ElementBase)m_treeHierarchy.peek(level);
    }
    
    /**
     * Get parent container information. This returns the innermost containing
     * binding component which refers to an object.
     * 
     * @return innermost containing element referencing bound object
     */
    public ContainerElementBase getParentContainer() {
        int index = 1;
        while (index < m_treeHierarchy.size()) {
            NestingElementBase nest =
                (NestingElementBase)m_treeHierarchy.peek(index++);
            if (nest instanceof ContainerElementBase) {
                return (ContainerElementBase)nest;
            }
        }
        throw new IllegalStateException("Internal error: no container");
    }
    
    /**
     * Get parent container with linked object. This returns the innermost
     * containing binding component which defines a context object.
     * 
     * @return innermost containing element defining a context object
     */
    public ContainerElementBase getContextObject() {
        int index = 1;
        while (index < m_treeHierarchy.size()) {
            NestingElementBase nest =
                (NestingElementBase)m_treeHierarchy.peek(index++);
            if (nest instanceof ContainerElementBase) {
                ContainerElementBase contain = (ContainerElementBase)nest;
                if (contain.hasObject()) {
                    return contain;
                }
            }
        }
        throw new IllegalStateException("Internal error: no context object");
    }
    
    /**
     * Check if binding supports input.
     * 
     * @return <code>true</code> if input binding, <code>false</code> if not
     */
    public boolean isInBinding() {
        return m_bindingRoot == null ? true : m_bindingRoot.isInBinding();
    }
    
    /**
     * Check if binding supports output.
     * 
     * @return <code>true</code> if output binding, <code>false</code> if not
     */
    public boolean isOutBinding() {
        return m_bindingRoot == null ? true : m_bindingRoot.isOutBinding();
    }
    
    /**
     * Get innermost containing definition context.
     * 
     * @return innermost definition context containing this element
     */
    public DefinitionContext getDefinitions() {
        int index = 1;
        while (index < m_treeHierarchy.size()) {
            NestingElementBase nest =
                (NestingElementBase)m_treeHierarchy.peek(index++);
            if (nest.getDefinitions() != null) {
                return nest.getDefinitions();
            }
        }
        if (m_globalContext == null) {
            throw new IllegalStateException
                ("Internal error: no definition context");
        } else {
            return m_globalContext;
        }
    }
    
    /**
     * Get definition context for innermost nesting element. If the context for
     * this element isn't already defined it's created by the call.
     * 
     * @return definition context for innermost nesting element
     */
    public DefinitionContext getCurrentDefinitions() {
        NestingElementBase parent = getParentElement();
        DefinitionContext dctx = parent.getDefinitions();
        if (dctx == null) {
            dctx = new DefinitionContext(getDefinitions());
            parent.setDefinitions(dctx);
        }
        return dctx;
    }
    
    /**
     * Get definition context for innermost nesting element for use by a
     * <b>format</b> (or <b>namespace</b>). If the context for this element
     * isn't already defined it's created by the call, along with the contexts
     * for any containing elements. This is ugly, but necessary to keep the tree
     * structure of contexts from getting split when other items are added by
     * the registration pass (since the formats are registered in the
     * prevalidation pass).
     * 
     * @return definition context for innermost nesting element
     */
    public DefinitionContext getFormatDefinitions() {
        NestingElementBase parent = getParentElement();
        DefinitionContext dctx = parent.getDefinitions();
        if (dctx == null) {
            
            // scan to find innermost nesting with context
            int index = 1;
            DefinitionContext pctx = null;
            while (++index < m_treeHierarchy.size()) {
                NestingElementBase nest =
                    (NestingElementBase)m_treeHierarchy.peek(index);
                pctx = nest.getDefinitions();
                if (pctx != null) {
                    break;
                }
            }
            
            // add contexts for all ancestors to level
            while (index >= 2) {
                dctx = new DefinitionContext(pctx);
                NestingElementBase nest =
                    (NestingElementBase)m_treeHierarchy.peek(--index);
                nest.setDefinitions(dctx);
                pctx = dctx;
            }
        }
        return dctx;
    }
    
    /**
     * Check if class lookup is supported. If this returns <code>false</code>,
     * lookup methods return only place holder class information.
     *
     * @return <code>true</code> if class lookup supported, <code>false</code>
     * if only place holder information returned
     */
    public boolean isLookupSupported() {
        return m_locator.isLookupSupported();
    }
    
    /**
     * Get class information. Finds a class by name using the class locator
     * configured by the environment code.
     *
     * @param name fully-qualified name of class to be found
     * @return class information, or <code>null</code> if class not found
     */
    public IClass getClassInfo(String name) {
        return m_locator.getClassInfo(name);
    }
    
    /**
     * Get required class information. Finds a class by name using the class
     * locator configured by the environment code. If the class cannot be found
     * a runtime exception is thrown.
     *
     * @param name fully-qualified name of class to be found
     * @return class information
     */
    public IClass getRequiredClassInfo(String name) {
        IClass iclas = m_locator.getClassInfo(name);
        if (iclas == null) {
            throw new IllegalStateException("Internal error: class " + name +
                " cannot be found");
        } else {
            return iclas;
        }
    }
    
    /**
     * Push node on tree. This is provided for use during validation, when the
     * context may be changed by recursive checks outside the normal tree
     * traversal.
     *
     * @param node
     */
    public void pushNode(ElementBase node) {
        m_treeHierarchy.push(node);
    }
    
    /**
     * Pop node from tree. This is provided for use during validation, when the
     * context may be changed by recursive checks outside the normal tree
     * traversal.
     *
     * @return node
     */
    public ElementBase popNode() {
        return (ElementBase)m_treeHierarchy.pop();
    }
}
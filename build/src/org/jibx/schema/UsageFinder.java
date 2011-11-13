/*
 * Copyright (c) 2006-2007, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jibx.schema.ISchemaListener;
import org.jibx.schema.NameRegister;
import org.jibx.schema.SchemaContextTracker;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.SchemaVisitor;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupRefElement;
import org.jibx.schema.elements.CommonTypeDefinition;
import org.jibx.schema.elements.ComplexExtensionElement;
import org.jibx.schema.elements.ComplexRestrictionElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.GroupRefElement;
import org.jibx.schema.elements.ListElement;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SimpleExtensionElement;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.UnionElement;
import org.jibx.util.ReferenceCountMap;

/**
 * Visitor to generate usage counts for schema components. This is somewhat messy, since there are so many different
 * types of references in schema: 'ref' (attribute, attributeGroup, element, and group, to reference a global definition
 * of same type), 'type' (attribute and element, reference a global type definition), 'base' (extension and
 * restriction), 'itemType' (list), 'memberTypes' (union), 'substitutionGroup' (element), and 'refer' (unique).
 * References can be recursively expanded by matching the reference set against the set of components processed, and
 * processing any new references until the closure is obtained.
 * 
 * @author Dennis M. Sosnoski
 */
public class UsageFinder
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(UsageFinder.class.getName());
    
    /** Visitor used for finding references. */
    private final UsageVisitor m_visitor;
    
    /**
     * Constructor.
     */
    public UsageFinder() {
        m_visitor = new UsageVisitor();
    }
    
    /**
     * Get map of reference counts per component.
     * 
     * @return count map
     */
    public ReferenceCountMap getUsageMap() {
        return m_visitor.getUsageMap();
    }
    
    /**
     * Get the set of definitions referenced as optional or repeating.
     * 
     * @return set
     */
    public Set getNonSingletonSet() {
        return m_visitor.getNonSingletonSet();
    }
    
    /**
     * Add usage counts for a schema tree. This counts all references from the supplied schema, including references in
     * other schemas referenced by the schema.
     * 
     * @param schema
     */
    public void countSchemaTree(SchemaElement schema) {
        TreeWalker wlkr = new TreeWalker(null, m_visitor.getListener());
        wlkr.walkSchema(schema, m_visitor);
    }
    
    /**
     * Add usage counts for the reference closure of a definition.
     * 
     * @param comp definition to be processed
     */
    public void addReferenceClosure(AnnotatedBase comp) {
        m_visitor.addReferenceClosure(comp);
    }
    
    /**
     * Add usage counts for the reference closure of a supplied list of components.
     * 
     * @param list starting schema components
     */
    public void addReferenceClosure(List list) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            addReferenceClosure((AnnotatedBase)iter.next());
        }
    }
    
    /**
     * Set the register to be used for resolving name references. This is not needed if counting references from a
     * schema tree using {@link #countSchemaTree(SchemaElement)}, which always sets the register associated with the
     * supplied schema.
     * 
     * @param reg
     */
    public void setNameRegister(NameRegister reg) {
        m_visitor.setNameRegister(reg);
    }
    
    /**
     * Clear the accumulated usage counts.
     */
    public void reset() {
        m_visitor.reset();
    }
    
    /**
     * Visitor to accumulate usage of definitions.
     */
    private static class UsageVisitor extends SchemaVisitor
    {
        /** Tracker for schema context information. */
        private final SchemaContextTracker m_tracker;
        
        /** Usage counts found for each global definition. */
        private final ReferenceCountMap m_usageMap;
        
        /** Set of definitions referenced as non-required or repeating items. */
        private final Set m_nonSingletonSet;
        
        /** Added references list (<code>null</code> if unused). */
        private ArrayList m_newReferences;
        
        /**
         * Constructor.
         */
        public UsageVisitor() {
            m_tracker = new SchemaContextTracker();
            m_usageMap = new ReferenceCountMap();
            m_nonSingletonSet = new HashSet();
        }
        
        /**
         * Get the schema change listener for this visitor.
         * 
         * @return listener
         */
        public ISchemaListener getListener() {
            return m_tracker;
        }
        
        /**
         * Convenience method for incrementing a use count. If tracking of added references is enabled this also adds
         * the referenced object to the list if it's a first-time reference.
         * 
         * @param obj referenced object (<code>null</code> if none, ignored if a schema type)
         */
        private void countUse(Object obj) {
            if (obj != null) {
                AnnotatedBase comp = (AnnotatedBase)obj;
                if (comp.getParent() != null) {
                    s_logger.debug(" incrementing usage count for " + SchemaUtils.describeComponent(comp));
                    if (m_usageMap.incrementCount(comp) == 1 && m_newReferences != null) {
                        if (comp.getParent() instanceof SchemaElement) {
                            s_logger.debug("  (first use for component, added to new references list)");
                            m_newReferences.add(obj);
                        } else if (((AnnotatedBase)obj).getParent() != null) {
                            throw new IllegalStateException("Internal error: non-global in usage counts");
                        }
                    }
                }
            }
        }
        
        /**
         * Convenience method for recording a non-singleton reference.
         * 
         * @param obj referenced object (<code>null</code> if none)
         */
        private void addNonSingleton(Object obj) {
            if (obj != null) {
                if (m_nonSingletonSet.add(obj)) {
                    s_logger.debug(" flagged non-singleton use of " +
                        SchemaUtils.describeComponent((AnnotatedBase)obj));
                }
            }
        }
        
        /**
         * Add usage counts for the reference closure of a definition. This counts all references from the definition,
         * then all references from the definitions referenced by the original definition, and so on until no new
         * references are found. This method may be called repeatedly, with the final results representing the closure
         * of the union of the specified definitions (or the union of the closure, since these are the same).
         * 
         * @param comp definition to be processed
         */
        public void addReferenceClosure(AnnotatedBase comp) {
            
            // create list for new references found during pass
            if (m_newReferences == null) {
                m_newReferences = new ArrayList();
            }
            
            // add all supplied components which have not already been processed
            s_logger.debug("adding reference closure for " + SchemaUtils.describeComponent(comp));
            int base = m_newReferences.size();
            countUse(comp);
            
            // loop processing new components until no more new components found
            TreeWalker wlkr = new TreeWalker(null, m_tracker);
            while (m_newReferences.size() > base) {
                int limit = m_newReferences.size();
                if (base > 0) {
                    s_logger.debug(" found " + (limit-base) + " new references in expansion pass");
                }
                for (int i = base; i < limit; i++) {
                    
                    // make sure component is a user type (not schema type)
                    AnnotatedBase ref = (AnnotatedBase)m_newReferences.get(i);
                    OpenAttrBase parent = ref.getParent();
                    if (parent instanceof SchemaElement) {
                        
                        // expand reference from this component
                        s_logger.debug(" adding reference closure for " + SchemaUtils.describeComponent(ref));
                        m_tracker.setNameRegister(((SchemaElement)parent).getRegister());
                        wlkr.walkElement(ref, this);
                        
                    } else {
                        s_logger.debug(" reference closure with non-global definition " + ref);
                    }
                }
                base = limit;
            }
        }
        
        /**
         * Set the register to be used for resolving name references.
         * 
         * @param reg
         */
        public void setNameRegister(NameRegister reg) {
            m_tracker.setNameRegister(reg);
        }
        
        /**
         * Get map of reference counts per component.
         * 
         * @return count map
         */
        public ReferenceCountMap getUsageMap() {
            return m_usageMap;
        }
        
        /**
         * Get the set of definitions referenced as optional or repeating.
         * 
         * @return set
         */
        public Set getNonSingletonSet() {
            return m_nonSingletonSet;
        }
        
        /**
         * Clear all state information. This allows an instance to be reused.
         */
        public void reset() {
            m_usageMap.clear();
            m_nonSingletonSet.clear();
            m_newReferences = null;
        }
        
        //
        // Exit methods for counting references
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.AttributeElement)
         */
        public void exit(AttributeElement node) {
            countUse(node.getReference());
            if (node.getType() != null) {
                countUse(node.getTypeDefinition());
            }
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.AttributeGroupElement)
         */
        public void exit(AttributeGroupRefElement node) {
            countUse(node.getReference());
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.ComplexExtensionElement)
         */
        public void exit(ComplexExtensionElement node) {
            countUse(node.getBaseType());
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.ComplexRestrictionElement)
         */
        public void exit(ComplexRestrictionElement node) {
            countUse(node.getBaseType());
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.ElementElement)
         */
        public void exit(ElementElement node) {
            if (node.getRef() !=  null) {
                
                // record element reference
                countUse(node.getReference());
                if (!SchemaUtils.isSingletonElement(node)) {
                    addNonSingleton(node.getReference());
                }
                
            } else if (node.getType() != null) {
                
                // record type reference
                countUse(node.getTypeDefinition());
                if (!SchemaUtils.isSingletonElement(node)) {
                    addNonSingleton(node.getTypeDefinition());
                }
                
            }
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.GroupElement)
         */
        public void exit(GroupRefElement node) {
            countUse(node.getReference());
            if (!SchemaUtils.isSingleton(node)) {
                addNonSingleton(node);
            }
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.ListElement)
         */
        public void exit(ListElement node) {
            countUse(node.getItemTypeDefinition());
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.SimpleExtensionElement)
         */
        public void exit(SimpleExtensionElement node) {
            countUse(node.getBaseType());
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.SimpleRestrictionElement)
         */
        public void exit(SimpleRestrictionElement node) {
            if (node.getBase() != null) {
                countUse(node.getBaseType());
            }
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.SchemaVisitor#exit(org.jibx.schema.elements.UnionElement)
         */
        public void exit(UnionElement node) {
            CommonTypeDefinition[] types = node.getMemberTypeDefinitions();
            if (types != null) {
                for (int i = 0; i < types.length; i++) {
                    countUse(types[i]);
                }
            }
        }
    }
}
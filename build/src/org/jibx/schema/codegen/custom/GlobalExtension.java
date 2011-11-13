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

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jibx.schema.INamed;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.codegen.DefinitionItem;
import org.jibx.schema.codegen.PackageHolder;
import org.jibx.schema.codegen.extend.ClassDecorator;
import org.jibx.schema.codegen.extend.NameConverter;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;

/**
 * Extension information for a schema global definition component. This adds reference tracking to the basic extension
 * information, along with a map for child components of the definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class GlobalExtension extends ComponentExtension
{
    /** Logger for class. */
    static final Logger s_logger = Logger.getLogger(GlobalExtension.class.getName());
    
    /** Containing schema extension. */
    private final SchemaExtension m_schemaExtension;
    
    /** Component to be specifically included in code generation (requiring a separate class). */
    private boolean m_included;
    
    /** Number of references to this definition. */
    private int m_referenceCount;
    
    /** List of extensions for components referencing this definition. */
    private ArrayList m_references;
    
    /** List of global definitions used by this definition (one entry per reference, may contain duplicates). */
    private ArrayList m_dependencies;

    /** Definition item for this global definition. */
    private DefinitionItem m_definition;
    
    /**
     * Constructor.
     * 
     * @param schemext extension for containing schema
     * @param comp actual component
     */
    public GlobalExtension(SchemaExtension schemext, OpenAttrBase comp) {
        super(comp, null);
        m_schemaExtension = schemext;
    }
    
    //
    // Methods delegated to schema extension
    
    /**
     * Get map from schema type local name to type information.
     *
     * @return map
     */
    public Map getSchemaTypes() {
        return m_schemaExtension.getSchemaTypes();
    }
    
    /**
     * Get name converter used for this component.
     *
     * @return converter
     */
    public NameConverter getNameConverter() {
        return m_schemaExtension.getNameConverter();
    }
    
    /**
     * Get code generation decorators used for this component.
     *
     * @return converter
     */
    public ClassDecorator[] getClassDecorators() {
        return m_schemaExtension.getClassDecorators();
    }

    /**
     * Get package for class generation.
     *
     * @return package
     */
    public PackageHolder getPackage() {
        return m_schemaExtension.getPackage();
    }
    
    /**
     * Check if inlining of definition is pushed (even when multiple values are involved).
     *
     * @return inline
     */
    public boolean isPushInline() {
        int type = getComponent().type();
        if (type == SchemaBase.ATTRIBUTEGROUP_TYPE || type == SchemaBase.GROUP_TYPE) {
            return m_schemaExtension.isInlineGroups();
        } else {
            return false;
        }
    }
    
    /**
     * Check if inlining of definition preferred. Inlining of xs:attributeGroup and xs:group definitions is always
     * preferred.
     *
     * @return inline
     */
    public boolean isPreferInline() {
        return m_schemaExtension.isPreferInline();
    }
    
    /**
     * Check if inner classes should be used for substructures.
     *
     * @return inner
     */
    public boolean isUseInnerClasses() {
        return m_schemaExtension.isUseInnerClasses();
    }
    
    //
    // Instance methods

    /**
     * Check if component specifically included in code generation (requiring a separate class).
     *
     * @return included
     */
    public boolean isIncluded() {
        return m_included;
    }

    /**
     * Set flag for component specifically included in code generation (requiring a separate class).
     *
     * @param included
     */
    public void setIncluded(boolean included) {
        m_included = included;
    }

    /**
     * Add reference extension.
     *
     * @param anno
     */
    public void addReference(ComponentExtension anno) {
        if (m_references == null) {
            m_references = new ArrayList();
        }
        m_references.add(anno);
        m_referenceCount++;
    }
    
    /**
     * Get referencing extension by index position.
     *
     * @param index
     * @return reference
     */
    public ComponentExtension getReference(int index) {
        return (ComponentExtension)m_references.get(index);
    }

    /**
     * Add dependency extension.
     *
     * @param anno
     */
    public void addDependency(ComponentExtension anno) {
        if (m_dependencies == null) {
            m_dependencies = new ArrayList();
        }
        m_dependencies.add(anno);
    }
    
    /**
     * Get the number of dependencies for this component.
     *
     * @return count
     */
    public int getDependencyCount() {
        if (m_dependencies == null) {
            return 0;
        } else {
            return m_dependencies.size();
        }
    }
    
    /**
     * Get dependency extension by index position.
     *
     * @param index
     * @return reference
     */
    public GlobalExtension getDependency(int index) {
        return (GlobalExtension)m_dependencies.get(index);
    }
    
    /**
     * Reset the dependencies and references of this component. This must be called before beginning a reference
     * tracking pass, to clear any information from prior passes.
     */
    public void resetDependencies() {
        if (m_dependencies != null) {
            m_dependencies.clear();
        }
        if (m_references != null) {
            m_references.clear();
        }
        m_referenceCount = 0;
    }
    
    /**
     * Check if the global definition can be removed from the schema. If it can, this adjusts the usage counts for all
     * dependencies of the definition, forcing a check of each dependency as the counts are adjusted.
     */
    public void checkRemovable() {
        if (!isRemoved()) {
            if (m_referenceCount > 0) {
                if (s_logger.isDebugEnabled()) {
                    OpenAttrBase component = getComponent();
                    s_logger.debug("Retaining " + ((INamed)component).getQName() + " from schema " +
                        ((SchemaElement)component.getParent()).getResolver().getName() + " with " + m_referenceCount +
                        " references");
                }
            } else if (!m_included && !isRemoved() && m_referenceCount == 0) {
                
                // flag definition for deletion
                setRemoved(true);
                if (s_logger.isDebugEnabled()) {
                    OpenAttrBase component = getComponent();
                    s_logger.debug(" Flagging deletion of " + ((INamed)component).getQName() + " from schema " +
                        ((SchemaElement)component.getParent()).getResolver().getName());
                }
                
                // recursively adjust the usage counts for dependencies
                for (int j = 0; j < getDependencyCount(); j++) {
                    GlobalExtension dependency = getDependency(j);
                    dependency.m_referenceCount--;
                    dependency.checkRemovable();
                }
            }
        }
    }

    /**
     * Get definition item.
     * 
     * @return item
     */
    public DefinitionItem getDefinition() {
        return m_definition;
    }

    /**
     * Set definition item.
     * 
     * @param item
     */
    public void setDefinition(DefinitionItem item) {
        m_definition = item;
    }

    /**
     * Normalize the schema definition component. This recursively traverses the schema model tree rooted in the
     * global component, normalizing each child component.
     */
    public void normalize() {
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Normalization invoked for global " + SchemaUtils.componentPath(getComponent()));
        }
        while (getOverrideType() == null && normalize(0));
    }
}
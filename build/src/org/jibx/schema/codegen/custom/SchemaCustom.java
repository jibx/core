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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jibx.binding.util.ObjectStack;
import org.jibx.custom.CustomUtils;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.schema.INamed;
import org.jibx.schema.SchemaContextTracker;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.SchemaVisitor;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.codegen.PackageHolder;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.FacetElement;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SchemaPath;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Individual schema customization information.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaCustom extends SchemaRootBase
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(SchemaCustom.class.getName());
    
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "excludes", "force-types", "includes", "name", "namespace" },
        SchemaRootBase.s_allowedAttributes);
    
    //
    // Bound instance data
    
    /** Schema name. */
    private String m_name;
    
    /** Schema namespace. */
    private String m_namespace;
    
    /** Always specify property types flag. */
    private boolean m_forceTypes;
    
    /** Global names included in code generation. */
    private String[] m_includes;
    
    /** Global names excluded from code generation. */
    private String[] m_excludes;
    
    //
    // Internal instance data
    
    /** Schema definition. */
    private SchemaElement m_schema;
    
    /** Extension attached to actual schema element (only used for children). */
    private SchemaExtension m_extension;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public SchemaCustom(SchemasetCustom parent) {
        super(parent);
    }
    
    /**
     * Constructor for constructing instance directly.
     * 
     * @param parent
     * @param name schema name
     * @param namespace target namespace URI
     * @param includes definition names to be included in generation
     * @param excludes definition names to be excluded from generation
     */
    public SchemaCustom(SchemasetCustom parent, String name, String namespace, String[] includes, String[] excludes) {
        this(parent);
        m_name = name;
        m_namespace = namespace;
        m_includes = includes;
        m_excludes = excludes;
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Get the names of definitions to be included in generation.
     *
     * @return names
     */
    public String[] getIncludes() {
        return m_includes;
    }
    
    /**
     * Set the names of definitions to be included in generation. This only has any effect on the code generation if
     * called before {@link #extend(PackageHolder, ValidationContext)} is called.
     *
     * @param includes
     */
    public void setIncludes(String[] includes) {
        m_includes = includes;
    }
    
    /**
     * Get the names of definitions to be excluded from generation.
     *
     * @return names
     */
    public String[] getExcludes() {
        return m_includes;
    }
    
    /**
     * Set the names of definitions to be excluded from generation. This only has any effect on the code generation if
     * called before {@link #extend(PackageHolder, ValidationContext)} is called.
     *
     * @param excludes
     */
    public void setExcludes(String[] excludes) {
        m_excludes = excludes;
    }
    
    /**
     * Get schema name.
     * 
     * @return name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Set schema name.
     * 
     * @param name
     */
    public void setName(String name) {
        m_name = name;
    }
    
    /**
     * Get schema namespace.
     * 
     * @return namespace
     */
    public String getNamespace() {
        return m_namespace;
    }
    
    /**
     * Set schema namespace.
     * 
     * @param namespace
     */
    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }
    
    /**
     * Check if type specifications forced for schema.
     * 
     * @return <code>true</code> if types forced, <code>false</code> if not
     */
    public boolean isForceTypes() {
        return m_forceTypes;
    }
    
    /**
     * Set type specifications forced for schema.
     * 
     * @param force <code>true</code> if types forced, <code>false</code> if not
     */
    public void setForceTypes(Boolean force) {
        m_forceTypes = force.booleanValue();
    }
    
    /**
     * Get schema definition.
     * 
     * @return schema
     */
    public SchemaElement getSchema() {
        return m_schema;
    }
    
    /**
     * Set schema definition.
     * 
     * @param name
     * @param schema
     */
    public void setSchema(String name, SchemaElement schema) {
        if (m_name == null) {
            m_name = name;
        }
        if (m_namespace == null) {
            m_namespace = schema.getEffectiveNamespace();
        }
        m_schema = schema;
    }
    
    /**
     * Check if this customization matches a particular schema.
     * 
     * @param name
     * @param schema
     * @return <code>true</code> if a match, <code>false</code> if not
     */
    public boolean checkMatch(String name, SchemaElement schema) {
        return (m_name == null || (m_name.equals(name)))
            && (m_namespace == null || (m_namespace.equals(schema.getEffectiveNamespace())));
    }
    
    /**
     * Build the extensions tree for a global definition.
     * 
     * @param visitor
     * @param wlkr
     * @param anno
     */
    private void extendGlobal(ExtensionBuilderVisitor visitor, TreeWalker wlkr, GlobalExtension anno) {
        visitor.setRoot(anno);
        wlkr.walkChildren(anno.getComponent(), visitor);
    }
    
    /**
     * Strip the annotation components (at any level) from a schema definitions.
     */
    public void stripAnnotations() {
            
        // first delete all annotation children of the schema element itself
        TreeWalker wlkr = new TreeWalker(null, new SchemaContextTracker());
        for (int i = 0; i < m_schema.getChildCount(); i++) {
            if (m_schema.getChild(i).type() == SchemaBase.ANNOTATION_TYPE) {
                m_schema.detachChild(i);
            }
        }
        m_schema.compactChildren();
        
        // now remove all embedded annotations within any definitions
        wlkr.walkSchema(m_schema, new AnnotationDeletionVisitor());
    }
    
    /**
     * Evaluate the remaining path for a customization after the first step, and apply it to the extension for each
     * matching schema component. If no matches are found or multiple matches are found this generates a warning.
     *
     * @param path customization path
     * @param match starting point for path
     * @param custom customization information
     * @param vctx validation context
     */
    private static void applyRemainingCustomizationPath(SchemaPath path, OpenAttrBase match, ComponentCustom custom,
        ValidationContext vctx) {
        List matches = path.partialMatchMultiple(1, path.getPathLength()-1, match);
        if (matches.size() == 0) {
            vctx.addWarning("No matches found for customization expression", custom);
        } else {
            if (matches.size() > 1) {
                vctx.addWarning("Found " + matches.size() + " matches for customization expression", custom);
            }
            for (Iterator iter = matches.iterator(); iter.hasNext();) {
                OpenAttrBase target = (OpenAttrBase)iter.next();
                custom.apply((ComponentExtension)target.getExtension(), vctx);
            }
        }
    }

    /**
     * Build the schema extension structure. This first builds extensions for all the global definitions in the schema,
     * marking the ones specified to be included or excluded from the schema, and for all the child components of the
     * non-excluded globals. It then applies the customizations to the extensions.
     * 
     * @param pack package for generated classes (<code>null</code> if no code generation)
     * @param vctx validation context
     */
    public void extend(PackageHolder pack, ValidationContext vctx) {
        
        // build the basic include and exclude sets
        Set inclset = Collections.EMPTY_SET;
        Set exclset = Collections.EMPTY_SET;
        if (m_includes != null) {
            inclset = CustomUtils.nameSet(m_includes);
        }
        if (m_excludes != null) {
            exclset = CustomUtils.nameSet(m_excludes);
        }
        
        // check for any conflicts between the two
        if (!inclset.isEmpty() && !exclset.isEmpty()) {
            for (Iterator iter = inclset.iterator(); iter.hasNext();) {
                Object next = iter.next();
                if (exclset.contains(next)) {
                    vctx.addError("Name '" + next.toString() + "' cannot be on both include and exclude list", this);
                }
            }
        }
        
        // build the extensions for each global definition component (and descendant components)
        m_extension = new SchemaExtension(m_schema, this, pack);
        List globals = m_schema.getTopLevelChildren();
        ExtensionBuilderVisitor builder = new ExtensionBuilderVisitor();
        // Level level = TreeWalker.setLogging(s_logger.getLevel());
        Set foundset = Collections.EMPTY_SET;
        if (m_includes != null || m_excludes != null) {
            foundset = new HashSet();
        }
        TreeWalker wlkr = new TreeWalker(null, new SchemaContextTracker());
        for (Iterator iter = globals.iterator(); iter.hasNext();) {
            OpenAttrBase global = (OpenAttrBase)iter.next();
            if (global instanceof INamed) {
                
                // set up the basic global definition extension
                GlobalExtension exten = new GlobalExtension(m_extension, global);
                String name = ((INamed)global).getName();
                boolean exclude = exclset.contains(name);
                exten.setRemoved(exclude);
                if (exclude) {
                    foundset.add(name);
                }
                boolean include = inclset.contains(name);
                exten.setIncluded(include);
                if (include) {
                    foundset.add(name);
                }
                
                // initialize extensions for full tree of non-excluded global definition components
                if (!exten.isRemoved()) {
                    extendGlobal(builder, wlkr, exten);
                } else if (global.type() == SchemaBase.SIMPLETYPE_TYPE ||
                    global.type() == SchemaBase.COMPLEXTYPE_TYPE) {
                    setReplacement(((INamed)global).getQName(), null);
                }
            }
        }
        // TreeWalker.setLogging(level);
        
        // report any names not found
        if (foundset.size() < exclset.size() + inclset.size()) {
            Set mergeset = new HashSet();
            mergeset.addAll(exclset);
            mergeset.addAll(inclset);
            for (Iterator iterator = mergeset.iterator(); iterator.hasNext();) {
                String name = (String)iterator.next();
                if (!foundset.contains(name)) {
                    vctx.addWarning("Name '" + name + "' not found in schema", this);
                }
            }
        }
        
        // use child customizations to amend the generated extensions
        int size = getChildren().size();
        for (int i = 0; i < size; i++) {
            ComponentCustom custom = (ComponentCustom)getChildren().get(i);
            SchemaPath path = custom.buildPath(vctx);
            if (path != null) {
                if (path.isWildStart()) {
                    vctx.addError("Top level customizations cannot use wildcard as first step", custom);
                } else {
                    
                    // match only the first path step
                    OpenAttrBase match = path.partialMatchUnique(0, 0, m_schema);
                    if (s_logger.isDebugEnabled()) {
                        if (match == null) {
                            s_logger.debug("No global schema component found for customization " + custom);
                        } else {
                            s_logger.debug("Matched customization " + custom + " to global schema component "
                                + SchemaUtils.describeComponent(match));
                        }
                    }
                    if (match != null) {
                        String name = ((INamed)match).getName();
                        GlobalExtension exten = (GlobalExtension)match.getExtension();
                        if (custom.isExcluded()) {
                            
                            // check if customization applies to descendant of global definition component
                            if (path.getPathLength() == 1) {
                                
                                // force exclude if generation skipped by customization
                                if (exten.isIncluded()) {
                                    vctx.addWarning("Name '" + name
                                        + "' is on include list for schema, but excluded by customization", custom);
                                }
                                exten.setIncluded(false);
                                exten.setRemoved(true);
                                
                            } else {
                                
                                // apply customization to target component extension(s)
                                applyRemainingCustomizationPath(path, match, custom, vctx);
                                
                            }
                            
                        } else {
                            
                            // check for customization for global excluded at schema level
                            if (exten.isRemoved()) {
                                vctx.addWarning("Name '" + name
                                    + "' is on excludes list for schema, but has a customization", custom);
                                exten.setRemoved(false);
                                extendGlobal(builder, wlkr, exten);
                            }
                            
                            // check if customization applies to descendant of global definition component
                            if (path.getPathLength() > 1) {
                                applyRemainingCustomizationPath(path, match, custom, vctx);
                            } else {
                                custom.apply((ComponentExtension)match.getExtension(), vctx);
                            }
                            
                        }
                    }
                }
            }
        }
        
        // flag extensions for facets to be removed from schema
        SchemaVisitor visitor = new FacetRemoverVisitor(this);
        for (Iterator iter = globals.iterator(); iter.hasNext();) {
            OpenAttrBase global = (OpenAttrBase)iter.next();
            ComponentExtension exten = (ComponentExtension)global.getExtension();
            if (exten != null && !exten.isRemoved()) {
                wlkr.walkElement(global, visitor);
            }
        }
    }

    /**
     * Factory used during unmarshalling.
     * 
     * @param ictx
     * @return instance
     */
    private static SchemaCustom factory(IUnmarshallingContext ictx) {
        return new SchemaCustom((SchemasetCustom)getContainingObject(ictx));
    }

    /**
     * Visitor to delete annotations from schema components.
     */
    private static class AnnotationDeletionVisitor extends SchemaVisitor
    {
        /**
         * Visit any component of schema definition. This just deletes any annotation present.
         * 
         * @param node
         * @return <code>true</code> to continue expansion
         */
        public boolean visit(AnnotatedBase node) {
            node.setAnnotation(null);
            return true;
        }
    }
    
    /**
     * Visitor to build basic extensions for schema components. This also sets class and base names for the extensions,
     * if the component has a name.
     */
    private static class ExtensionBuilderVisitor extends SchemaVisitor
    {
        /** Extension for root component being expanded. */
        private GlobalExtension m_root;
        
        /**
         * Set the extension for the root of the schema definition component to be expanded.
         * 
         * @param root
         */
        public void setRoot(GlobalExtension root) {
            m_root = root;
        }
        
        /**
         * Visit any component of schema definition. This just creates the extension for the component.
         * 
         * @param node
         * @return <code>true</code> to continue expansion
         */
        public boolean visit(AnnotatedBase node) {
            node.setExtension(new ComponentExtension(node, m_root));
            return true;
        }
    }
    
    /**
     * Visitor to flag extensions to remove unused facets. This relies on each customization being set as the type
     * substitution handler for the corresponding extension.
     */
    private static class FacetRemoverVisitor extends SchemaVisitor
    {
        /** Stack of active customizations. */
        private ObjectStack m_customStack;
        
        /** Currently active customization. */
        private NestingCustomBase m_currentCustom;
        
        /**
         * Constructor.
         * 
         * @param root customization for root element being processed
         */
        public FacetRemoverVisitor(SchemaCustom root) {
            m_customStack = new ObjectStack();
            m_currentCustom = root;
        }
        
        /**
         * Exit the generic precursor class of all elements which can have customizations. This just pops the saved
         * customization for the higher level off the stack.
         * 
         * @param node
         */
        public void exit(AnnotatedBase node) {
            super.exit(node);
            m_currentCustom = (NestingCustomBase)m_customStack.pop();
        }
        
        /**
         * Visit a facet element. This first calls the handling for the supertype, in order to activate a customization
         * that applies to this particular element, then checks if the facet element subtype is to be included in the
         * code generation.
         * 
         * @param node
         * @return <code>true</code> if continuing expansion, <code>false</code> if not
         */
        public boolean visit(FacetElement node) {
            boolean ret = super.visit(node);
            if ((m_currentCustom.getActiveFacetsMask() & node.bit()) == 0) {
                ((ComponentExtension)node.getExtension()).setRemoved(true);
            }
            return ret;
        }
        
        /**
         * Visit the generic precursor class of all elements which can have customizations. This saves the current
         * customization on the stack, then checks for one associated with the current element and makes that active if
         * found.
         * 
         * @param node
         * @return <code>true</code> if continuing expansion, <code>false</code> if not
         */
        public boolean visit(AnnotatedBase node) {
            m_customStack.push(m_currentCustom);
            NestingCustomBase custom = (NestingCustomBase)(((ComponentExtension)node.getExtension())).getCustom();
            if (custom != null) {
                m_currentCustom = custom;
            }
            return super.visit(node);
        }
    }
}
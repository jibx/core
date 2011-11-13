/*
Copyright (c) 2004-2011, Dennis M. Sosnoski. All rights reserved.

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
import java.util.HashMap;

import org.jibx.runtime.Utility;
import org.jibx.util.IClass;

/**
 * Definition context information. This is used to track definitions of items
 * that can be referenced by other items. The contexts are nested, so that names
 * not found in a context may be defined by a containing context. The access
 * methods take this into account, automatically delegating to the containing
 * context (if defined) when a lookup fails.
 *
 * @author Dennis M. Sosnoski
 */
public class DefinitionContext
{
    /** Link to containing definition context. */
    private final DefinitionContext m_outerContext;

    /** Namespace used by default at this level for attributes. */
    private NamespaceElement m_attributeDefault;

    /** Namespace used by default at this level for elements. */
    private NamespaceElement m_elementDefault;
    
    /** Namespaces defined at level (lazy create). */
    private ArrayList m_namespaces;

    /** Mapping from prefix to namespace definition (lazy create). */
    private HashMap m_prefixMap;

    /** Mapping from URI to namespace definition (lazy create). */
    private HashMap m_uriMap;
    
    /** Map from element names to mappings defined at level (lazy create). */
    private HashMap m_mappingMap;
    
    /** Class hierarchy context for format definitions (lazy create). */
    private ClassHierarchyContext m_formatContext;
    
    /** Class hierarchy context for template definitions (lazy create). */
    private ClassHierarchyContext m_templateContext;
    
    /** Named binding components (lazy create). */
    private HashMap m_namedStructureMap;
    
    /**
     * Constructor.
     * 
     * @param outer containing definition context (<code>null</code> if
     * at root of tree)
     */
    protected DefinitionContext(DefinitionContext outer) {
        m_outerContext = outer;
    }
    
    /**
     * Copy a context for use by an included binding. The duplicated context has
     * the same containing context as the original, and shared reference
     * structures for formats and templates, but only a static copy of the
     * namespace definitions.
     * 
     * @return context copy for included binding
     */
    /*package*/ DefinitionContext getIncludeCopy() {
        DefinitionContext dupl = new DefinitionContext(m_outerContext);
        if (m_mappingMap == null) {
            m_mappingMap = new HashMap();
        }
        dupl.m_mappingMap = m_mappingMap;
        if (m_formatContext == null) {
            m_formatContext =
                new ClassHierarchyContext(getContainingFormatContext());
        }
        dupl.m_formatContext = m_formatContext;
        if (m_templateContext == null) {
            m_templateContext =
                new ClassHierarchyContext(getContainingTemplateContext());
        }
        dupl.m_templateContext = m_templateContext;
        return dupl;
    }

    /**
     * Inject namespaces from this context into another context. This is
     * intended for includes, where the included binding inherits the
     * namespace declarations of the containing binding.
     *
     * @param to
     */
    /*package*/ void injectNamespaces(DefinitionContext to) {
        to.m_attributeDefault = m_attributeDefault;
        to.m_elementDefault = m_elementDefault;
        if (m_namespaces != null) {
            to.m_namespaces = new ArrayList(m_namespaces);
            to.m_prefixMap = new HashMap(m_prefixMap);
            to.m_uriMap = new HashMap(m_uriMap);
        }
    }
    
    /**
     * Get containing context.
     * 
     * @return containing context information (<code>null</code> if at root of
     * tree)
     */
    public DefinitionContext getContaining() {
        return m_outerContext;
    }

    /**
     * Get containing format context.
     * 
     * @return innermost containing context for format definitions
     * (<code>null</code> none defined)
     */
    private ClassHierarchyContext getContainingFormatContext() {
        if (m_outerContext == null) {
            return null;
        } else {
            return m_outerContext.getFormatContext();
        }
    }
    
    /**
     * Get current format context.
     * 
     * @return innermost context for format definitions (<code>null</code> none
     * defined)
     */
    private ClassHierarchyContext getFormatContext() {
        if (m_formatContext == null) {
            return getContainingFormatContext();
        } else {
            return m_formatContext;
        }
    }

    /**
     * Get containing template context.
     * 
     * @return innermost containing context for template definitions
     * (<code>null</code> none defined)
     */
    private ClassHierarchyContext getContainingTemplateContext() {
        if (m_outerContext == null) {
            return null;
        } else {
            return m_outerContext.getTemplateContext();
        }
    }
    
    /**
     * Get current template context.
     * 
     * @return innermost context for template definitions (<code>null</code> none
     * defined)
     */
    private ClassHierarchyContext getTemplateContext() {
        if (m_templateContext == null) {
            return getContainingTemplateContext();
        } else {
            return m_templateContext;
        }
    }

    /**
     * Add namespace to set defined at this level.
     *
     * @param def namespace definition element to be added
     * @return problem information, or <code>null</code> if no problem
     */
/*    public ValidationProblem addNamespace(NamespaceElement def) {
        
        // initialize structures if first namespace definition
        if (m_namespaces == null) {
            m_namespaces = new ArrayList();
            m_prefixMap = new HashMap();
            m_uriMap = new HashMap();
        }

        // check for conflict as default for attributes
        if (def.isAttributeDefault()) {
            if (m_attributeDefault == null) {
                m_attributeDefault = def;
            } else {
                return new ValidationProblem
                    ("Conflicting attribute namespaces", def);
            }
        }

        // check for conflict as default for elements
        if (def.isElementDefault()) {
            if (m_elementDefault == null) {
                m_elementDefault = def;
            } else {
                return new ValidationProblem
                    ("Conflicting element namespaces", def);
            }
        }

        // check for conflict on prefix
        String prefix = def.getPrefix();
        if (m_prefixMap.get(prefix) != null) {
            return new ValidationProblem("Namespace prefix conflict", def);
        }
        
        // check for duplicate definition of same URI
        String uri = def.getUri();
        Object prior = m_uriMap.get(uri);
        if (prior != null && ((NamespaceElement)prior).getPrefix() != null) {
            // TODO: is this needed? multiple prefixes should be allowed
            return null;
        }

        // add only if successful in all tests
        m_namespaces.add(def);
        m_prefixMap.put(prefix, def);
        m_uriMap.put(uri, def);
        return null;
    }   */
    
    /**
     * Get the default namespace applied to element definitions. If there's no
     * default namespace at this level of definitions this moves up the nested
     * definitions contexts until one is found.
     * 
     * @return default namespace (<code>null</code> if none)
     */
    public NamespaceElement getElementDefaultNamespace() {
        DefinitionContext defctx = this;
        while (defctx.m_elementDefault == null) {
            defctx = defctx.getContaining();
            if (defctx == null) {
                return null;
            }
        }
        return defctx.m_elementDefault;
    }
    
    /**
     * Get namespace for prefix.
     * 
     * @param prefix
     * @return namespace definition in this context, <code>null</code> if none
     */
    public NamespaceElement getNamespaceForPrefix(String prefix) {
        if (m_prefixMap == null) {
            return null;
        } else {
            return (NamespaceElement)m_prefixMap.get(prefix);
        }
    }

    /**
     * Check for namespace using the same prefix. This also intializes the
     * namespace structures for this context the first time the method is
     * called.
     *
     * @param def
     * @return namespace definition using same prefix, <code>null</code> if none
     */
    private NamespaceElement checkDuplicatePrefix(NamespaceElement def) {
        
        // create structures if not already done
        if (m_namespaces == null) {
            m_namespaces = new ArrayList();
            m_prefixMap = new HashMap();
            m_uriMap = new HashMap();
        }
        
        // check for conflict (or duplicate) on prefix
        String prefix = def.getPrefix();
        return (NamespaceElement)m_prefixMap.get(prefix);
    }

    /**
     * Add namespace to internal tables.
     *
     * @param def
     * @param report report default conflicts as error flag
     * @return problem information, or <code>null</code> if no problem
     */
    private ValidationProblem internalAddNamespace(NamespaceElement def,
        boolean report) {

        // check for conflict as default for attributes
        if (def.isAttributeDefault()) {
            if (m_attributeDefault == null) {
                m_attributeDefault = def;
            } else if (report) {
                return new ValidationProblem
                    ("Conflicting attribute namespaces", def);
            }
        }

        // check for conflict as default for elements
        if (def.isElementDefault()) {
            if (m_elementDefault == null) {
                m_elementDefault = def;
            } else if (report) {
                return new ValidationProblem
                    ("Conflicting element namespaces", def);
            }
        }
        
        // add namespace to tables
        String uri = def.getUri();
        String prefix = def.getPrefix();
        m_namespaces.add(def);
        m_prefixMap.put(prefix, def);
        m_uriMap.put(uri, def);
        return null;
    }

    /**
     * Add namespace to set defined at this level.
     *
     * @param def namespace definition element to be added (duplicates ignored)
     * @return problem information, or <code>null</code> if no problem
     */
    public ValidationProblem addNamespace(NamespaceElement def) {
        String uri = def.getUri();
        NamespaceElement dup = checkDuplicatePrefix(def);
        if (dup == null || uri.equals(dup.getUri())) {
            
            // no conflicts, add it
            return internalAddNamespace(def, true);
        
        } else {
            String prefix = def.getPrefix();
            if (prefix == null) {
                return new ValidationProblem
                    ("Default namespace conflict", def);
            } else {
                return new ValidationProblem
                    ("Namespace prefix conflict for prefix '" + prefix +
                    '\'', def);
            }
        }
    }

    /**
     * Add namespace declaration to set defined at this level.
     *
     * @param def namespace definition to be added (duplicates ignored)
     * @param ref binding element referencing the namespace
     * @return problem information, or <code>null</code> if no problem
     */
    public ValidationProblem addImpliedNamespace(NamespaceElement def,
        ElementBase ref) {
        String uri = def.getUri();
        NamespaceElement dup = checkDuplicatePrefix(def);
        DefinitionContext ctx = this;
        String prefix = def.getPrefix();
        while (dup == null && (ctx = ctx.getContaining()) != null) {
            dup = getNamespaceForPrefix(prefix);
        }
        if (dup == null) {
            
            // check for no definition of same URI with prefix
            NamespaceElement prior = (NamespaceElement)m_uriMap.get(uri);
            if (prior == null || prior.getPrefix() == null) {
                
                // no conflicts, add it (but only use defaults if no others set)
                return internalAddNamespace(def, false);
                
            }
            
        } else if (!uri.equals(dup.getUri())) {
            if (prefix == null) {
                return new ValidationProblem
                    ("Default namespace conflict on mapping reference", ref);
            } else {
                return new ValidationProblem
                    ("Namespace conflict on mapping reference, for prefix '" + prefix +
                    '\'', ref);
            }
        }
        return null;
    }

    /**
     * Get namespace definition for element name.
     * TODO: handle multiple prefixes for namespace, proper screening
     *
     * @param name attribute group defining name
     * @param vctx validation context in use
     * @return namespace definition, or <code>null</code> if none that matches
     */
    public NamespaceElement getElementNamespace(NameAttributes name,
        ValidationContext vctx) {
        String uri = name.getUri();
        String prefix = name.getPrefix();
        NamespaceElement ns = null;
        if (uri != null) {
            if (m_uriMap != null) {
                ns = (NamespaceElement)m_uriMap.get(uri);
                if (ns != null) {
                    if (prefix == null ) {
                        prefix = ns.getPrefix();
                    } else if (!prefix.equals(ns.getPrefix())) {
                        ns = null;
                    }
                }
            }
        } else if (prefix != null) {
            if (m_prefixMap != null) {
                ns = (NamespaceElement)m_prefixMap.get(prefix);
            }
        } else {
            ns = m_elementDefault;
        }
        if (ns == null && m_outerContext != null) {
            ns = m_outerContext.getElementNamespace(name, vctx);
            if (ns != null && m_prefixMap != null) {
                NamespaceElement cns =
                    (NamespaceElement)m_prefixMap.get(ns.getPrefix());
                if (cns != null && ns != cns &&
                    !(Utility.safeEquals(ns.getUri(), cns.getUri()) &&
                    Utility.safeEquals(ns.getPrefix(), cns.getPrefix()))) {
                    StringBuffer buff = new StringBuffer();
                    if (uri == null) {
                        buff.append("Conflict between namespace definitions for '");
                        buff.append(cns.getUri());
                        if (ns.getUri() == null) {
                            buff.append("' and no-namespace usage");
                        } else {
                            buff.append("' and '");
                            buff.append(ns.getUri());
                            buff.append('\'');
                        }
                    } else if ("".equals(uri)){
                        buff.append("Unable to use non-namespaced elements because namespace '");
                        buff.append(cns.getUri());
                        buff.append("' is the default");
                    } else {
                        buff.append("Unable to use namespace URI '");
                        buff.append(uri);
                        buff.append("' due to prefix conflict with namespace '");
                        buff.append(cns.getUri());
                        buff.append('\'');
                    }
                    vctx.addError(buff.toString());
                }
            }
        }
        return ns;
    }

    /**
     * Get namespace definition for attribute name.
     * TODO: handle multiple prefixes for namespace, proper screening
     *
     * @param name attribute group defining name
     * @return namespace definition, or <code>null</code> if none that matches
     */
    public NamespaceElement getAttributeNamespace(NameAttributes name) {
        String uri = name.getUri();
        String prefix = name.getPrefix();
        NamespaceElement ns = null;
        if (uri != null) {
            if (m_uriMap != null) {
                ns = (NamespaceElement)m_uriMap.get(uri);
                if (ns != null && prefix != null) {
                    if (!prefix.equals(ns.getPrefix())) {
                        ns = null;
                    }
                }
            }
        } else if (prefix != null) {
            if (m_prefixMap != null) {
                ns = (NamespaceElement)m_prefixMap.get(prefix);
            }
        } else {
            ns = m_attributeDefault;
        }
        if (ns == null && m_outerContext != null) {
            ns = m_outerContext.getAttributeNamespace(name);
        }
        return ns;
    }
    
    /**
     * Add format to set defined at this level.
     *
     * @param def format definition element to be added
     * @param vctx validation context in use
     */
    public void addFormat(FormatElement def, ValidationContext vctx) {
        if (m_formatContext == null) {
            m_formatContext =
                new ClassHierarchyContext(getContainingFormatContext());
        }
        if (def.isDefaultFormat()) {
            IClass clas = def.getType();
            m_formatContext.addTypedComponent(clas, def, vctx);
        }
        String label = def.getLabel();
        if (label != null) {
            m_formatContext.addNamedComponent(label, def, vctx);
        }
    }

    /**
     * Get specific format definition for type. Finds with an exact match
     * on the class name, checking the containing definitions if a format
     * is not found at this level.
     *
     * @param type fully qualified class name to be converted
     * @return conversion definition for class, or <code>null</code> if not
     * found
     */
    public FormatElement getSpecificFormat(String type) {
        ClassHierarchyContext ctx = getFormatContext();
        if (ctx == null) {
            return null;
        } else {
            return (FormatElement)ctx.getSpecificComponent(type);
        }
    }
    
    /**
     * Get named format definition. Finds the format with the supplied
     * name, checking the containing definitions if the format is not found
     * at this level.
     *
     * @param name conversion name to be found
     * @return conversion definition with specified name, or <code>null</code>
     * if no conversion with that name
     */
    public FormatElement getNamedFormat(String name) {
        ClassHierarchyContext ctx = getFormatContext();
        if (ctx == null) {
            return null;
        } else {
            return (FormatElement)ctx.getNamedComponent(name);
        }
    }

    /**
     * Get best format definition for class. Finds the format based on the
     * inheritance hierarchy for the supplied class. If a specific format for
     * the actual class is not found (either in this or a containing level) this
     * returns the most specific superclass format.
     *
     * @param clas information for target conversion class
     * @return conversion definition for class, or <code>null</code> if no
     * compatible conversion defined
     */
    public FormatElement getBestFormat(IClass clas) {
        ClassHierarchyContext ctx = getFormatContext();
        if (ctx == null) {
            return null;
        } else {
            return (FormatElement)ctx.getMostSpecificComponent(clas);
        }
    }
    
    /**
     * Add mapped name to set defined at this level.
     * 
     * @param name mapped name
     * @param def mapping definition
     * @param vctx validation context 
     */
    public void addMappedName(NameAttributes name, MappingElementBase def,
        ValidationContext vctx) {
        if (m_mappingMap == null) {
            m_mappingMap = new HashMap();
        }
        String fullname = name.getName();
        NamespaceElement namespace = getElementNamespace(name, vctx);
        if (namespace != null && namespace.getUri() != null) {
            fullname = '{' + namespace.getUri() + '}' + fullname;
        }
        if (m_mappingMap.containsKey(fullname)) {
            if (vctx.isInBinding()) {
                vctx.addError
                    ("Duplicate mapping name not allowed for unmarshalling");
            }
        } else {
            m_mappingMap.put(fullname, def);
        }
    }

    /**
     * Add template or mapping to set defined at this level.
     *
     * @param def template definition element to be added
     * @param vctx validation context in use
     */
    public void addTemplate(MappingElementBase def, ValidationContext vctx) {
        if (m_templateContext == null) {
            m_templateContext =
                new ClassHierarchyContext(getContainingTemplateContext());
        }
        if (def.isDefaultTemplate()) {
            IClass clas = def.getHandledClass();
            if (vctx.isInBinding() ||
                m_templateContext.getSpecificComponent(clas.getName()) == null) {
                m_templateContext.addTypedComponent(clas, def, vctx);
            }
        }
        if (def.getTypeName() != null) {
            m_templateContext.addNamedComponent(def.getTypeName(),
                def, vctx);
        }
    }

    /**
     * Get specific template definition for type. Finds with an exact match
     * on the class name, checking the containing definitions if a template
     * is not found at this level.
     *
     * @param type fully qualified class name to be converted
     * @return template definition for type, or <code>null</code> if not
     * found
     */
    public TemplateElementBase getSpecificTemplate(String type) {
        ClassHierarchyContext ctx = getTemplateContext();
        if (ctx == null) {
            return null;
        } else {
            return (TemplateElementBase)ctx.getSpecificComponent(type);
        }
    }
    
    /**
     * Get named template definition. Finds the template with the supplied
     * name, checking the containing definitions if the template is not found
     * at this level.
     * TODO: Make this specific to TemplateElement in 2.0
     *
     * @param name conversion name to be found
     * @return template definition for class, or <code>null</code> if no
     * template with that name
     */
    public TemplateElementBase getNamedTemplate(String name) {
        ClassHierarchyContext ctx = getTemplateContext();
        if (ctx == null) {
            return null;
        } else {
            return (TemplateElementBase)ctx.getNamedComponent(name);
        }
    }

    /**
     * Checks if a class is compatible with one or more templates. This checks
     * based on the inheritance hierarchy for the supplied class, looks for the
     * class or interface itself as well as any subclasses or implementations.
     *
     * @param clas information for target class
     * @return <code>true</code> if compatible type, <code>false</code> if not
     */
    public boolean isCompatibleTemplateType(IClass clas) {
        ClassHierarchyContext chctx = getTemplateContext();
        if (chctx == null) {
            return false;
        } else {
            return chctx.isCompatibleType(clas);
        }
    }
    
    /**
     * Add named structure to set defined in this context. For named structures
     * only the definition context associated with the binding element should be
     * used. This is a kludge, but will go away in 2.0.
     *
     * @param def structure definition
     * @return problem information, or <code>null</code> if no problem
     */
    public ValidationProblem addNamedStructure(ContainerElementBase def) {

        // create structure if not already done
        if (m_namedStructureMap == null) {
            m_namedStructureMap = new HashMap();
        }

        // check for conflict on label before adding to definitions
        String label = def.getLabel();
        if (m_namedStructureMap.get(label) == null) {
            m_namedStructureMap.put(label, def);
            return null;
        } else {
            return new ValidationProblem("Duplicate label \"" + label + '"',
                def);
        }
    }

    /**
     * Get labeled structure definition within this context. For named
     * structures only the definition context associated with the binding
     * element should be used. This is a kludge, but will go away in 2.0.
     * 
     * @param label structure definition label
     * @return structure definition with specified label, or <code>null</code>
     * if not defined
     */
    public ContainerElementBase getNamedStructure(String label) {
        if (m_namedStructureMap == null) {
            return null;
        } else {
            return (ContainerElementBase)m_namedStructureMap.get(label);
        }
    }
    
    /**
     * Get the namespaces defined in this context
     *
     * @return namespace definitions (may be <code>null</code> if none)
     */
    public ArrayList getNamespaces() {
        return m_namespaces;
    }
}
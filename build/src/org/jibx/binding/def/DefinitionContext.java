/*
Copyright (c) 2003-2009, Dennis M. Sosnoski.
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

package org.jibx.binding.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.MethodBuilder;
import org.jibx.binding.util.ArrayMap;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;

/**
 * Nesting level for definitions in binding. This tracks namespace and mapping
 * definitions that apply to all enclosed items.
 *
 * @author Dennis M. Sosnoski
 */
public class DefinitionContext
{
    /** Containing binding definition component. */
    private final IContainer m_container;

    /** Containing definition context. */
    private final DefinitionContext m_context;

    /** Namespace used by default at this level for attributes. */
    private NamespaceDefinition m_attributeDefault;

    /** Namespace used by default at this level for elements. */
    private NamespaceDefinition m_elementDefault;

    /** Namespaces defined at level (lazy create). */
    private ArrayList m_namespaces;

    /** Mapping from URI to namespace definition (lazy create). */
    private Map m_uriMap;

    /** Mapping from override namespace definition to original (lazy create). */
    private Map m_overrideMap;

    /** Mapping from fully qualified class name to mapping index (lazy
     create). */
    private ArrayMap m_classMap;

    /** Class mappings defined at level (lazy create). */
    private ArrayList m_mappings;
    
    /** Map from signatures to <code>String</code> conversions. */
    private Map m_convertMap;
    
    /** Map from format qnames to <code>String</code> conversions. */
    private Map m_formatMap;
    
    /** Named binding components (only for root context of a binding). */
    private Map m_namedStructureMap;

    /**
     * Constructor. Uses the containing context to establish the hierarchy for
     * resolving namespaces and class mappings.
     *
     * @param contain containing binding definition component
     */
    public DefinitionContext(IContainer contain) {
        m_container = contain;
        m_context = contain.getDefinitionContext();
        // TODO: make these lazy
        m_convertMap = new HashMap();
        m_formatMap = new HashMap();
        if (m_context == null) {
            m_namedStructureMap = new HashMap();
        }
    }

    /**
     * Check for duplicate namespace definition. This also initializes the
     * namespace structures for this context the first time the method is
     * called.
     *
     * @param def
     * @return duplicate flag (either complete duplicate, or prior definition
     * of same URI with prefix is present)
     */
    private boolean checkDuplicateNamespace(NamespaceDefinition def) {
        
        // create structures if not already done
        if (m_namespaces == null) {
            m_namespaces = new ArrayList();
            m_uriMap = new HashMap();
        }
        
        // check for conflict (or duplicate) definition
        String uri = def.getUri();
        NamespaceDefinition prior = (NamespaceDefinition)m_uriMap.get(uri);
        return prior != null && (prior.getPrefix() != null || def.getPrefix() == null);
    }

    /**
     * Add namespace to internal tables.
     *
     * @param def
     */
    private void internalAddNamespace(NamespaceDefinition def) {
        String uri = def.getUri();
        String prefix = def.getPrefix();
        def.setIndex(m_container.getBindingRoot().
            getNamespaceUriIndex(uri, prefix));
        m_namespaces.add(def);
        m_uriMap.put(uri, def);
    }

    /**
     * Add namespace to set defined at this level. If the new namespace
     * conflicts with an existing namespace at this level (in terms of default
     * usage or prefix) this throws an exception.
     *
     * @param def namespace definition to be added (duplicates ignored)
     */
    public void addNamespace(NamespaceDefinition def) {
        
        // override prior defaults with this namespace definition
        if (def.isAttributeDefault()) {
            m_attributeDefault = def;
        }
        if (def.isElementDefault()) {
            m_elementDefault = def;
        }
        if (checkDuplicateNamespace(def)) {
            
            // replace current definition for URI if this one sets defaults
            if (def.isAttributeDefault() || def.isElementDefault()) {
                NamespaceDefinition prior =
                    (NamespaceDefinition)m_uriMap.put(def.getUri(), def);
                def.setIndex(prior.getIndex());
                if (m_overrideMap == null) {
                    m_overrideMap = new HashMap();
                }
                m_overrideMap.put(def, prior);
            }
            
        } else {
            
            // no conflicts, add it
            internalAddNamespace(def);
            
        }
    }

    /**
     * Add namespace declaration to set defined at this level. This method
     * treats all namespaces as though they were declared with default="none".
     * If the new namespace prefix conflicts with an existing namespace this
     * throws an exception.
     *
     * @param def namespace definition to be added (duplicates ignored)
     */
    public void addImpliedNamespace(NamespaceDefinition def) {
        if (!checkDuplicateNamespace(def)) {
            internalAddNamespace(def);
        }
    }

    /**
     * Add class mapping to set defined at this level. If the new mapping
     * conflicts with an existing one at this level it throws an exception.
     *
     * @param def mapping definition to be added
     * @throws JiBXException on mapping definition conflict
     */
    public void addMapping(IMapping def) throws JiBXException {

        // create structure if not already done
        if (m_mappings == null) {
            m_classMap = new ArrayMap();
            m_mappings = new ArrayList();
        }

        // check for conflict on class name before adding to definitions
        String name = def.getTypeName();
        if (name == null) {
            name = def.getReferenceType();
        }
        int index = m_classMap.findOrAdd(name);
        if (index < m_mappings.size()) {
            if (def.getTypeName() == null) {
                throw new JiBXException
                    ("Conflicting mappings for class " + name);
            } else {
                throw new JiBXException
                    ("Conflicting mappings for type name " + name);
            }
        } else {
            m_mappings.add(def);
        }
    }

    /**
     * Add named structure component to set defined at this level. If the name
     * conflicts with an existing one at this level it throws an exception.
     *
     * @param name component name to be set
     * @param comp named component
     * @throws JiBXException on mapping definition conflict
     */
    public void addNamedStructure(String name, IComponent comp)
        throws JiBXException {
        if (m_namedStructureMap == null) {
            m_context.addNamedStructure(name, comp);
        } else {
            m_namedStructureMap.put(name, comp);
        }
    }

    /**
     * Get the default namespace for a contained name. Elements and attributes
     * are treated separately, since namespace handling differs between the two.
     *
     * @param attr flag for attribute name
     * @return default namespace URI, or <code>null</code> if none
     */
    private NamespaceDefinition getDefaultNamespace(boolean attr) {
        NamespaceDefinition ns;
        if (attr) {
            ns = m_attributeDefault;
        } else {
            ns = m_elementDefault;
        }
        if (ns == null && m_context != null) {
            ns = m_context.getDefaultNamespace(attr);
        }
        return ns;
    }

    /**
     * Get the default namespace URI for a contained name. Elements and
     * attributes are treated separately, since namespace handling differs
     * between the two.
     *
     * @param attr flag for attribute name
     * @return default namespace URI, or <code>null</code> if none
     */
    public String getDefaultURI(boolean attr) {
        NamespaceDefinition ns = getDefaultNamespace(attr);
        if (ns == null) {
            return null;
        } else {
            return ns.getUri();
        }
    }

    /**
     * Get the default namespace index for a contained name. Elements and
     * attributes are treated separately, since namespace handling differs
     * between the two.
     *
     * @param attr flag for attribute name
     * @return default namespace index
     */
    public int getDefaultIndex(boolean attr) {
        NamespaceDefinition ns = getDefaultNamespace(attr);
        if (ns == null) {
            return 0;
        } else {
            return ns.getIndex();
        }
    }

    /**
     * Get namespace index for a given URI. Finds the prefix for a URI in a
     * name contained by this level, throwing an exception if the URI is not
     * found or does not have a prefix.
     *
     * @param uri namespace URI to be found
     * @param attr flag for attribute name
     * @return namespace index for URI
     * @throws JiBXException if URI not defined or not usable
     */
    public int getNamespaceIndex(String uri, boolean attr)
        throws JiBXException {

        // check for namespace URI defined at this level
        if (m_uriMap != null) {
            NamespaceDefinition ns = (NamespaceDefinition)m_uriMap.get(uri);
            while (ns != null) {
                if (!attr || ns.getPrefix() != null) {
                    return ns.getIndex();
                } else if (m_overrideMap != null){
                    ns = (NamespaceDefinition)m_overrideMap.get(ns);
                } else {
                    break;
                }
            }
        }

        // if all else fails, try the higher level
        if (m_context == null) {
            throw new JiBXException("Namespace URI \"" + uri +
                "\" not defined or not usable");
        } else {
            return m_context.getNamespaceIndex(uri, attr);
        }
    }

    /**
     * Get mapping definition for class if defined at this level.
     *
     * @param name fully qualified class name
     * @return mapping definition for class, or <code>null</code> if not defined
     */
    public IMapping getMappingAtLevel(String name) {

        // check for class mapping defined at this level
        if (m_classMap != null) {
            
            // check for definition at this level
            int index = m_classMap.find(name);
            if (index >= 0) {
                return (IMapping)m_mappings.get(index);
            }
        }
        return null;
    }

    /**
     * Get mapping definition for class. Finds the mapping for a fully
     * qualified class name, throwing an exception if no mapping is defined.
     * This can only be used during the linkage phase.
     *
     * @param name fully qualified class name
     * @return mapping definition for class, or <code>null</code> if not defined
     */
    public IMapping getClassMapping(String name) {

        // check for class mapping defined at this level
        IMapping def = getMappingAtLevel(name);
        if (def == null && m_context != null) {

            // try finding definition at higher level
            def = m_context.getClassMapping(name);
            
        }
        return def;
    }

    /**
     * Get nested structure by name. Finds the nested structure with the given
     * name, throwing an exception if no component with that name is defined.
     *
     * @param name component name to be found
     * @return component with given name
     * @throws JiBXException if name not defined
     */
    public IComponent getNamedStructure(String name) throws JiBXException {

        // check for named component defined at this level
        IComponent comp = null;
        if (m_namedStructureMap != null) {
            comp = (IComponent)m_namedStructureMap.get(name);
        }
        if (comp == null) {
            if (m_context == null) {
                throw new JiBXException("Referenced label \"" + name +
                    "\" not defined"); 
            } else {
                comp = m_context.getNamedStructure(name);
            }
        }
        return comp;
    }

    /**
     * Get mapping definitions at level.
     *
     * @return mapping definitions, <code>null</code> if none defined at level
     */
    public ArrayList getMappings() {
        return m_mappings;
    }

    /**
     * Get specific conversion definition for type. Finds with an exact match
     * on the class name, checking the containing definitions if a conversion
     * is not found at this level.
     *
     * @param name fully qualified class name to be converted
     * @return conversion definition for class, or <code>null</code> if not
     * found
     */
    public StringConversion getSpecificConversion(String name) {
        StringConversion conv = (StringConversion)m_convertMap.get(name);
        if (conv == null && m_context != null) {
            conv = m_context.getSpecificConversion(name);
        }
        return conv;
    }

    /**
     * Get conversion definition for class. Finds the conversion based on a
     * fully qualified class name. If a specific conversion for the actual
     * class is not found (either in this or a containing level) this returns
     * the generic object conversion.
     *
     * @param clas information for target conversion class
     * @return conversion definition for class
     */
    public StringConversion getConversion(ClassFile clas) {
        
        // use conversions for superclasses only 
        StringConversion conv = getSpecificConversion(clas.getName());
        if (conv == null) {
            return getNamedConversion(BindingDefinition.OBJECT_DEFAULT_NAME);
        } else {
            return conv;
        }
    }

    /**
     * Get named conversion definition. Finds the conversion with the supplied
     * name, checking the containing definitions if the conversion is not found
     * at this level.
     *
     * @param name conversion name to be found
     * @return conversion definition for class
     */
    public StringConversion getNamedConversion(QName name) {
        StringConversion conv = (StringConversion)m_formatMap.get(name);
        if (conv == null && m_context != null) {
            conv = m_context.getNamedConversion(name);
        }
        return conv;
    }

    /**
     * Add named conversion. Checks for duplicate conversions defined within
     * a level with the same name.
     *
     * @param name format name for this conversion
     * @param conv conversion definition for class
     * @throws JiBXException if duplicate conversion definition
     */
    public void addConversion(QName name, StringConversion conv)
        throws JiBXException {
        if (m_formatMap.put(name, conv) != null) {
            throw new JiBXException("Duplicate conversion defined with name " +
                name);
        }
    }

    /**
     * Set specific conversion definition for type. Sets the conversion based
     * on a type signature, checking for duplicate conversions defined within
     * a level.
     *
     * @param conv conversion definition for class
     * @throws JiBXException if duplicate conversion definition
     */
    public void setConversion(StringConversion conv)
        throws JiBXException {
        if (m_convertMap.put(conv.getTypeName(), conv) != null) {
            throw new JiBXException("Duplicate conversion defined for type " +
                conv.getTypeName());
        }
    }

    /**
     * Sets a named conversion definition.
     *
     * @param name format name for this conversion
     * @param conv conversion definition for class
     * @throws JiBXException if duplicate conversion definition
     */
    public void setNamedConversion(QName name, StringConversion conv)
        throws JiBXException {
        addConversion(name, conv);
    }

    /**
     * Sets a conversion definition by both type and name. Both the type and
     * name are checked for duplicate conversions defined within a level.
     *
     * @param name format name for this conversion
     * @param conv conversion definition for class
     * @throws JiBXException if duplicate conversion definition
     */
    public void setDefaultConversion(QName name, StringConversion conv)
        throws JiBXException {
        addConversion(name, conv);
        setConversion(conv);
    }
    
    /**
     * Check if one or more namespaces are defined in this context.
     *
     * @return <code>true</code> if namespaces are defined, <code>false</code>
     * if not
     */
    public boolean hasNamespace() {
        return m_namespaces != null && m_namespaces.size() > 0;
    }
    
    /**
     * Get the namespaces defined in this context
     *
     * @return namespace definitions (may be <code>null</code> if none)
     */
    public ArrayList getNamespaces() {
        return m_namespaces;
    }

    /**
     * Internal method to generate code to fill array with namespace indexes.
     * The code generated to this point must have the array reference on the
     * stack.
     *
     * @param nss namespaces to be handled
     * @param mb method builder for generated code
     */
    private void genFillNamespaceIndexes(ArrayList nss, MethodBuilder mb) {
        if (nss != null) {
            for (int i = 0; i < nss.size(); i++) {
                mb.appendDUP();
                mb.appendLoadConstant(i);
                mb.appendLoadConstant
                    (((NamespaceDefinition)nss.get(i)).getIndex());
                mb.appendIASTORE();
            }
        }
    }

    /**
     * Internal method to generate code to fill array with namespace prefixes.
     * The code generated to this point must have the array reference on the
     * stack.
     *
     * @param nss namespaces to be handled
     * @param mb method builder for generated code
     */
    private void genFillNamespacePrefixes(ArrayList nss, MethodBuilder mb) {
        if (nss != null) {
            for (int i = 0; i < nss.size(); i++) {
                mb.appendDUP();
                mb.appendLoadConstant(i);
                String prefix = ((NamespaceDefinition)nss.get(i)).getPrefix();
                if (prefix == null) {
                    prefix = "";
                }
                mb.appendLoadConstant(prefix);
                mb.appendAASTORE();
            }
        }
    }

    /**
     * Generate code for loading namespace index and URI arrays. The code
     * creates the arrays and leaves the references on the stack.
     *
     * @param mb method builder for generated code
     */
    public void genLoadNamespaces(MethodBuilder mb) {
        
        // first create the array of namespace indexes
        int count = m_namespaces == null ? 0 : m_namespaces.size();
        mb.appendLoadConstant(count);
        mb.appendCreateArray("int");
        genFillNamespaceIndexes(m_namespaces, mb);
        
        // next create the array of prefixes
        mb.appendLoadConstant(count);
        mb.appendCreateArray("java.lang.String");
        genFillNamespacePrefixes(m_namespaces, mb);
    }

    /**
     * Generate code. Executes code generation for each top-level mapping
     * defined in this binding, which in turn propagates the code generation
     * all the way down.
     *
     * @param verbose flag for verbose output
     * @param force create marshaller/unmarshaller even for abstract non-base
     * mappings flag
     * @throws JiBXException if error in transformation
     */
    public void generateCode(boolean verbose, boolean force)
        throws JiBXException {
        if (m_mappings != null) {
            for (int i = 0; i < m_mappings.size(); i++) {
                IMapping mapping = (IMapping)m_mappings.get(i);
                if (verbose) {
                    System.out.println("Generating code for mapping " +
                        mapping.getBoundType());
                }
                ((IMapping)m_mappings.get(i)).generateCode(force);
            }
        }
    }
    
    /**
     * Links extension mappings to their base mappings. This must be done before
     * the more general linking step in order to determine which abstract
     * mappings are standalone and which are extended by other mappings
     *
     * @throws JiBXException if error in linking
     */
    public void linkMappings() throws JiBXException {
        
        // check if any mappings are defined
        if (m_mappings != null) {
            for (int i = 0; i < m_mappings.size(); i++) {
                Object obj = m_mappings.get(i);
                if (obj instanceof MappingDefinition) {
                    ((MappingDefinition)obj).linkMappings();
                }
            }
        }
    }
    
    /**
     * Set linkages between binding components. This is called after all the
     * basic information has been set up. All linkage to higher level
     * components should be done by this method, in order to prevent problems
     * due to the order of definitions between components. For the definition
     * context this calls the same method on all mappings defined in this
     * context.
     *
     * @throws JiBXException if error in configuration
     */
    public void setLinkages() throws JiBXException {
        
        // check if any mappings are defined
        if (m_mappings != null) {
            for (int i = 0; i < m_mappings.size(); i++) {
                Object obj = m_mappings.get(i);
                if (obj instanceof MappingDefinition) {
                    ((MappingDefinition)obj).setLinkages();
                }
            }
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("context");
        if (m_namespaces != null) {
            System.out.print(" (ns#=" + m_namespaces.size() + ')');
        }
        if (m_mappings != null) {
            System.out.print(" (mp#=" + m_mappings.size() + ')');
        }
        if (m_namedStructureMap != null) {
            System.out.print(" (nm#=" + m_namedStructureMap.size() + ')');
        }
        if (m_convertMap != null) {
            System.out.print(" (cv#=" + m_convertMap.size() + ')');
        }
        if (m_formatMap != null) {
            System.out.print(" (fm#=" + m_formatMap.size() + ')');
        }
        System.out.println();
        if (m_namespaces != null) {
            for (int i = 0; i < m_namespaces.size(); i++) {
                NamespaceDefinition ndef =
                    (NamespaceDefinition)m_namespaces.get(i);
                ndef.print(depth+1);
            }
        }
        if (m_mappings != null) {
            for (int i = 0; i < m_mappings.size(); i++) {
                Object obj = m_mappings.get(i);
                if (obj instanceof MappingDefinition) {
                    MappingDefinition mdef = (MappingDefinition)obj;
                    mdef.print(depth+1);
                } else if (obj instanceof MappingDirect) {
                    MappingDirect mdir = (MappingDirect)obj;
                    mdir.print(depth+1);
                } else {
                    BindingDefinition.indent(depth+1);
                    System.out.println("unexpected type " +
                        obj.getClass().getName());
                }
            }
        }
    }
}
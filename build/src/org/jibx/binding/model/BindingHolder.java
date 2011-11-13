/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.binding.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jibx.runtime.Utility;
import org.jibx.util.InsertionOrderedSet;
import org.jibx.util.LazyList;

/**
 * External data for a binding definition. Along with the actual mapping definitions, this tracks namespace references
 * and usages.
 * 
 * @author Dennis M. Sosnoski
 */
public class BindingHolder
{
    /** Organizer managing this holder. */
    private final BindingOrganizer m_organizer;
    
    /** Namespace URI associated with this binding (<code>null</code> if no-namespace binding). */
    private final String m_namespace;
    
    /** Namespace used by default for elements flag. */
    private final boolean m_elementDefault;
    
    /** Set of namespaces referenced in qualified names in this bindings. */
    private final InsertionOrderedSet m_referencedNamespaces;
    
    /** Set of namespaces used in element or attribute names in binding. */
    private final InsertionOrderedSet m_usedNamespaces;
    
    /** Prefix to be used for this namespace (<code>null</code> if unspecified). */
    private String m_prefix;
    
    /** Actual binding definition. */
    private BindingElement m_binding;
    
    /** Pull namespaces used in element or attribute names up to root binding flag (set when a type reference to this
     binding is seen). */
    private boolean m_pullUpNamespaces;
    
    /** Binding namespace used for element or attribute flag. */
    private boolean m_namespaceUsed;
    
    /** Binding finalized flag. */
    private boolean m_finished;
    
    /** Name for file to be written from binding. */
    private String m_fileName;
    
    /** List of mapping definitions in binding. */
    private final LazyList m_mappings;
    
    /**
     * Constructor.
     * TODO: add a way of handling pregenerated bindings, so that namespaces can be properly tracked?
     * 
     * @param uri (<code>null</code> if no-namespace binding)
     * @param dflt namespace is default for elements flag
     * @param dir directory managing this holder
     */
    public BindingHolder(String uri, boolean dflt, BindingOrganizer dir) {
        m_organizer = dir;
        m_namespace = uri;
        m_elementDefault = dflt;
        m_referencedNamespaces = new InsertionOrderedSet();
        m_usedNamespaces = new InsertionOrderedSet();
        m_binding = new BindingElement();
        m_mappings = new LazyList();
    }
    
    /**
     * Get namespace URI associated with this binding.
     * 
     * @return namespace (<code>null</code> if no-namespace)
     */
    public String getNamespace() {
        return m_namespace;
    }
    
    /**
     * Get namespace prefix for this binding.
     * 
     * @return prefix (<code>null</code> if not specified)
     */
    public String getPrefix() {
        return m_prefix;
    }
    
    /**
     * Set namespace prefix for this binding. Method used by the binding organizer code to configure binding.
     * 
     * @param prefix (<code>null</code> if not specified)
     */
    /*default*/ void setPrefix(String prefix) {
        m_prefix = prefix;
    }
    
    /**
     * Get default namespace URI for elements defined in this binding.
     * 
     * @return namespace (<code>null</code> if no-namespace)
     */
    public String getElementDefaultNamespace() {
        return m_elementDefault ? m_namespace : null;
    }
    
    /**
     * Get the binding element.
     * 
     * @return binding
     */
    public BindingElement getBinding() {
        return m_binding;
    }
    
    /**
     * Set the binding element. This method is provided so that the generated binding element can be replaced by one
     * which has been read in from a file after being written.
     * 
     * @param bind
     */
    public void setBinding(BindingElement bind) {
        m_binding = bind;
    }
    
    /**
     * Add a format definition to the binding. In actual generation, formats may be moved to a root binding definition.
     *
     * @param format
     */
    public void addFormat(FormatElement format) {
        m_organizer.addFormat(format);
    }
    
    /**
     * Add usage of namespace for an element or attribute name in binding.
     * 
     * @param uri referenced namespace URI (<code>null</code> if no-namespace)
     */
    public void addNamespaceUsage(String uri) {
        if (Utility.safeEquals(uri, m_namespace)) {
            m_namespaceUsed = true;
        } else {
            m_organizer.addNamespaceUsage(this, uri);
            m_usedNamespaces.add(uri);
        }
    }
    
    /**
     * Add reference from this binding to a type name defined in the same or another binding.
     * 
     * @param uri namespace URI for type name
     * @param obj object associated with referenced binding
     */
    public void addTypeNameReference(String uri, Object obj) {
        m_organizer.addTypeNameReference(this, uri, obj);
    }
    
    /**
     * Internal check method to verify that the binding is still modifiable.
     */
    private void checkModifiable() {
        if (m_finished) {
            throw new IllegalStateException("Internal error - attempt to modify binding after finalized");
        }
    }
    
    /**
     * Get the file name to be used for this file.
     * 
     * @return name (<code>null</code> if not set)
     */
    public String getFileName() {
        return m_fileName;
    }
    
    /**
     * Set the file name to be used for this file.
     * 
     * @param name
     */
    public void setFileName(String name) {
        m_fileName = name;
    }
    
    /**
     * Add a mapping definition to the binding.
     *
     * @param mapping
     */
    public void addMapping(MappingElementBase mapping) {
        checkModifiable();
        m_mappings.add(mapping);
    }
    
    /**
     * Get the number of mapping definitions present in this binding.
     *
     * @return count
     */
    public int getMappingCount() {
        return m_mappings.size();
    }
    
    /**
     * Get the number of mapping definitions present in this binding.
     *
     * @return count
     */
    public Iterator iterateMappings() {
        return m_mappings.iterator();
    }

    /**
     * Get the binding associated with a particular control object.
     * 
     * @param obj object associated with binding (can be namespace URI, if only one binding per namespace)
     * @return binding holder
     */
    public BindingHolder getRequiredBinding(Object obj) {
        return m_organizer.getRequiredBinding(obj);
    }
    
    /**
     * Get a set of all the namespace URIs referenced by a qualified name in this binding. Method used by the binding
     * organizer code to configure binding.
     *
     * @return live namespace list
     */
    /*default*/ Set getReferencedNamespaces() {
        return m_referencedNamespaces;
    }
    
    /**
     * Get a set of all the namespace URIs used by element or attributes names in this binding. Method used by the
     * binding organizer code to configure binding.
     *
     * @return live namespace list
     */
    /*default*/ Set getUsedNamespaces() {
        return m_usedNamespaces;
    }
    
    /**
     * Force namespaces to be pulled up to the root binding. Method used by the binding organizer code to configure
     * binding.
     */
    /*default*/ void forcePullUpNamespaces() {
        m_pullUpNamespaces = true;
    }
    
    /**
     * Check if used namespaces need to be pulled up to the root binding. Method used by the binding organizer code to
     * configure binding.
     *
     * @return pull up flag
     */
    /*default*/ boolean isPullUpNamespaces() {
        return m_pullUpNamespaces;
    }
    
    /**
     * Check if the namespace associated with this binding should be used as the default for definitions in the binding.
     * Method used by the binding organizer code to configure binding.
     *
     * @return pull up flag
     */
    /*default*/ boolean isNamespaceElementDefault() {
        return m_elementDefault;
    }
    
    /**
     * Check if the namespace associated with this binding is actually used by any element or attribute definitions in
     * the binding. Method used by the binding organizer code to configure binding.
     *
     * @return pull up flag
     */
    /*default*/ boolean isBindingNamespaceUsed() {
        return m_namespaceUsed;
    }
    
    /**
     * Finishes building the binding. Method used by the binding organizer code to configure binding.
     *
     * @param formats format elements to be used in binding
     * @param includes include elements to be used in binding
     * @param outernss namespaces inherited by this binding
     * @param nsdfltpref map from namespace URI to default prefix when used in a namespace declaration within the
     * binding (<code>null</code> values for namespace used as the default)
     * @param nsfrcdpref map from namespace URI to prefix when used in a binding namespace definition (prefix must be
     * non-<code>null</code> and non-empty)
     */
    /*default*/ void finish(Collection formats, Collection includes, Set outernss, Map nsdfltpref, Map nsfrcdpref) {
        
        // make sure not already done
        checkModifiable();
        
        // add all required namespace declarations to binding element
        List topchilds = m_binding.topChildren();
        for (Iterator iter = m_referencedNamespaces.iterator(); iter.hasNext();) {
            String uri = (String)iter.next();
            if (uri != null) {
                String prefix = (String)nsfrcdpref.get(uri);
                if (prefix == null || prefix.length() == 0) {
                    throw new IllegalStateException("Internal error - no prefix for declaration of namespace " + uri);
                }
                m_binding.addNamespaceDecl(prefix, uri);
            }
        }
       
        // add namespace element for the definitions in this binding
        boolean elemdflt = m_elementDefault;
        String dfltprfx = (String)nsdfltpref.get(m_namespace);
        String prefix = m_prefix == null ? dfltprfx : m_prefix;
        if ("".equals(prefix)) {
            elemdflt = true;
        }
        if (m_namespace != null && m_namespaceUsed &&
            (!outernss.contains(m_namespace) || (elemdflt && !"".equals(dfltprfx)))) {
            NamespaceElement ns = new NamespaceElement();
            ns.setUri(m_namespace);
            if (elemdflt) {
                ns.setDefaultName("elements");
            }
            if (!"".equals(prefix)) {
                ns.setPrefix(prefix);
            }
            topchilds.add(ns);
        }
        
        // add all other required namespace definitions
        for (Iterator iter = m_usedNamespaces.iterator(); iter.hasNext();) {
            String uri = (String)iter.next();
            if (uri != null) {
                NamespaceElement ns = new NamespaceElement();
                ns.setUri(uri);
                ns.setPrefix((String)nsdfltpref.get(uri));
                topchilds.add(ns);
            }
        }
        
        // add formats, screening out any with no actual definitions to allow default handling of Java 5 enums
        //  (note that this may add namespace references, for type names on formats)
        for (Iterator iter = formats.iterator(); iter.hasNext();) {
            FormatElement format = (FormatElement)iter.next();
            if (format.getDefaultText() != null || format.getDeserializerName() != null ||
                format.getEnumValueName() != null || format.getSerializerName() != null || format.getQName() != null) {
                topchilds.add(format);
                // qualified names are not used directly for formats (instead using {uri}:lname), so no need for
                //  namespace declaration
//                QName qname = format.getQName();
//                if (qname != null) {
//                    // TODO: can I be sure that a prefix has been set?
//                    m_referencedNamespaces.add(qname.getUri());
//                }
            }
        }
        
        // follow formats with includes and accumulated mappings
        topchilds.addAll(includes);
        topchilds.addAll(m_mappings);
        m_finished = true;
    }
}
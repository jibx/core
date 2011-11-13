/*
 * Copyright (c) 2008-2011, Dennis M. Sosnoski. All rights reserved.
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

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.schema.INamed;
import org.jibx.schema.IReference;
import org.jibx.schema.SchemaContextTracker;
import org.jibx.schema.SchemaVisitor;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.codegen.custom.ComponentExtension;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AnnotationElement;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.KeyBase;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.util.InsertionOrderedSet;
import org.jibx.util.UniqueNameSet;

/**
 * Visitor to write a filtered view of a schema definition matching the data structure of a class. This is used when
 * schema fragments are included in class documentation.
 */
public class SchemaDocumentationGenerator
{
    /** Leading text for comment lines. */
    public static final String COMMENT_LEAD_TEXT = "\n * ";

    /** Schema definitions namespace URI. */
    private static final String SCHEMA_DEFINITIONS_NS = "http://www.w3.org/2001/XMLSchema";

    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(SchemaDocumentationGenerator.class.getName());
    
    /** Extract binding factory. */
    private final IBindingFactory m_factory;
    
    /** Schema definitions namespace index. */
    private final int m_schemaIndex;
    
    /** Schema definitions namespace prefix. */
    private final String m_schemaPrefix;
    
    /** Set of namespace URIs defined in binding. */
    private final Set m_namespaceSet;
    
    /** Marshaller instance for writing schema fragments. */
    private final MarshallingContext m_context;
    
    /**
     * Constructor.
     * 
     * @throws JiBXException on error loading binding information
     */
    public SchemaDocumentationGenerator() throws JiBXException {
        m_factory = BindingDirectory.getFactory("schema_extract_binding", "org.jibx.schema.codegen");
        int index = 0;
        String[] uris = m_factory.getNamespaces();
        m_namespaceSet = new HashSet();
        for (int i = 1; i < uris.length; i++) {
            m_namespaceSet.add(uris[i]);
            if (SCHEMA_DEFINITIONS_NS.equals(uris[i])) {
                index = i;
            }
        }
        if (index < 0) {
            throw new JiBXException("Schema namespace definition not found");
        }
        m_schemaIndex = index;
        m_schemaPrefix = m_factory.getPrefixes()[index];
        m_context = (MarshallingContext)m_factory.createMarshallingContext();
        m_context.setIndent(2, COMMENT_LEAD_TEXT, ' ');
    }
    
    /**
     * Scan schema component references from item tree. This recursively constructs (1) a map from schema components
     * represented by separate classes to the corresponding class information, (2) a set of schema global definitions
     * included in the item tree, and (3) a set of namespace URIs for referenced components.
     *
     * @param group item grouping to be processed
     * @param comptoclas map from schema component to corresponding {@link ClassHolder}
     * @param refcomps set of schema global definitions incorporated into this tree
     * @param uritoprefix map from namespaces used by referenced definitions to the corresponding prefixes
     */
    private void scanItemTree(GroupItem group, Map comptoclas, Set refcomps, Map uritoprefix) {
        OpenAttrBase topcomp = group.getSchemaComponent();
        for (Item item = group.getFirstChild(); item != null; item = item.getNext()) {
            if (!item.isIgnored()) {
                
                // start by checking if we've crossed into a different schema definition
                OpenAttrBase comp = item.getSchemaComponent();
                while (comp != topcomp) {
                    if (comp.isGlobal()) {
                        if (!(item instanceof ReferenceItem)) {
                            refcomps.add(comp);
                        }
                        QName qname = ((INamed)comp).getQName();
                        if (qname == null) {
                            throw new IllegalStateException("Internal error - no name on global definition");
                        } else {
                            uritoprefix.put(qname.getUri(), qname.getPrefix());
                        }
                        break;
                    } else {
                        comp = comp.getParent();
                    }
                }
                
                // now check the actual item type
                if (item instanceof GroupItem) {
                    
                    // check non-inlined group which uses separate class
                    GroupItem childgroup = (GroupItem)item;
                    if (!childgroup.isIgnored()) {
                        if (childgroup.isInline()) {
                            scanItemTree(childgroup, comptoclas, refcomps, uritoprefix);
                        } else {
                            
                            // add component to class mapping to stop schema fragment generation when reached
                            TypeData genclas = childgroup.getGenerateClass();
                            if (genclas == null) {
                                throw new IllegalStateException("Internal error - no generate class");
                            } else {
                                comptoclas.put(childgroup.getSchemaComponent(), genclas);
                            }
                            
                            // check for type definition or reference used (with associated namespace reference)
                            QName tname = null;
                            comp = item.getSchemaComponent();
                            switch (comp.type()) {
                                case SchemaBase.ATTRIBUTE_TYPE:
                                    tname = ((AttributeElement)comp).getType();
                                    break;
                                case SchemaBase.ELEMENT_TYPE:
                                    tname = ((ElementElement)comp).getType();
                                    break;
                            }
                            if (tname != null) {
                                uritoprefix.put(tname.getUri(), tname.getPrefix());
                            }
                            QName rname = null;
                            if (comp instanceof IReference) {
                                rname = ((IReference)comp).getRef();
                            }
                            if (rname != null) {
                                uritoprefix.put(rname.getUri(), rname.getPrefix());
                            }
                        }
                    }
                    
                } else if (item instanceof ReferenceItem) {
                    
                    // make sure namespace collected for reference
                    DefinitionItem def = ((ReferenceItem)item).getDefinition();
                    AnnotatedBase defcomp = def.getSchemaComponent();
                    if (defcomp instanceof INamed) {
                        QName qname = ((INamed)defcomp).getQName();
                        if (qname == null && defcomp instanceof IReference) {
                            qname = ((IReference)defcomp).getRef();
                        }
                        if (qname != null) {
                            uritoprefix.put(qname.getUri(), qname.getPrefix());
                        }
                    }
                    TypeData genclas = def.getGenerateClass();
                    if (genclas != null) {
                        comptoclas.put(defcomp, genclas);
                    }
                    
                }
            }
        }
        
        // also check for substitution group used with element
        if (topcomp.type() == SchemaBase.ELEMENT_TYPE) {
            QName sname = ((ElementElement)topcomp).getSubstitutionGroup();
            if (sname != null) {
                uritoprefix.put(sname.getUri(), sname.getPrefix());
            }
        }
    }
    
    /**
     * Escape a special character in a text string.
     *
     * @param chr
     * @param escape
     * @param text
     * @param buff
     */
    private void escapeText(char chr, String escape, String text, StringBuffer buff) {
        int base = 0;
        int scan;
        while ((scan = text.indexOf(chr, base)) >= 0) {
            buff.append(text.substring(base, scan));
            buff.append(escape);
            base = scan + 1;
        }
        buff.append(text.substring(base));
    }

    /**
     * Generate documentation from the schema component corresponding to a class.
     *
     * @param group item group for class
     * @param dropanno delete annotations from schema documentation flag
     * @return schema extract documentation
     */
    public String generate(GroupItem group, boolean dropanno) {
        if (group.isIgnored()) {
            return null;
        } else {
            
            // scan the item tree structure to find schema components needing special handling
            Map comptoclas = new HashMap();
            InsertionOrderedSet refcomps = new InsertionOrderedSet();
            AnnotatedBase grpcomp = group.getSchemaComponent();
            refcomps.add(grpcomp);
            Map uritoprefix = new HashMap();
            if (grpcomp instanceof INamed) {
                QName qname = ((INamed)grpcomp).getQName();
                if (qname == null && grpcomp instanceof IReference) {
                    qname = ((IReference)grpcomp).getRef();
                }
                if (qname != null && qname.getUri() != null) {
                    uritoprefix.put(qname.getUri(), qname.getPrefix());
                }
            }
            scanItemTree(group, comptoclas, refcomps, uritoprefix);
            
            // get full set of namespaces and corresponding prefixes needed for extracts
            UniqueNameSet prefset = new UniqueNameSet();
            prefset.add(m_schemaPrefix);
            for (Iterator iter = uritoprefix.keySet().iterator(); iter.hasNext();) {
                String uri = (String)iter.next();
                if (!SCHEMA_DEFINITIONS_NS.equals(uri)) {
                    String prefix = (String)uritoprefix.get(uri);
                    if (prefix == null) {
                        prefix = "ns";
                    }
                    prefix = prefset.add(prefix);
                    uritoprefix.put(uri, prefix);
                }
                
            }
            
            // set the writer to be used for output
            StringWriter strwriter = new StringWriter();
            m_context.setOutput(strwriter);
            
            // define namespaces as extension to those used in binding
            int count = uritoprefix.size();
            String[] uris = (String[])uritoprefix.keySet().toArray(new String[count]);
            IXMLWriter xmlwriter = m_context.getXmlWriter();
            int base = xmlwriter.getNamespaceCount();
            xmlwriter.pushExtensionNamespaces(uris);
            
            // build the arrays of namespace indexes and prefixes for use on marshalled root elements
            int length = count;
            if (!uritoprefix.containsKey(null)) {
                length++;
            }
            String[] prefixes = new String[length];
            int[] indexes = new int[length];
            int fill = 0;
            for (int i = 0; i < count; i++) {
                String uri = uris[i];
                if (uri != null) {
                    prefixes[fill] = (String)uritoprefix.get(uris[i]);
                    indexes[fill] = base+fill;
                    fill++;
                }
            }
            prefixes[fill] = m_schemaPrefix;
            indexes[fill] = m_schemaIndex;
            
            // build schema extracts from main component and all referenced components
            String clasname = group.getGenerateClass().getFullName();
            TreeWalker walker = new TreeWalker(null, new SchemaContextTracker());
            boolean ref = false;
            List list = refcomps.asList();
            for (Iterator iter = list.iterator(); iter.hasNext();) {
                
                // add a blank line separating components
                if (ref) {
                    strwriter.write(COMMENT_LEAD_TEXT);
                } else {
                    ref = true;
                }
                
                // add any necessary namespace declarations
                AnnotatedBase comp = (AnnotatedBase)iter.next();
                for (Iterator jter = uritoprefix.keySet().iterator(); jter.hasNext();) {
                    String uri = (String)jter.next();
                    comp.addNamespaceDeclaration((String)uritoprefix.get(uri), uri);
                }
                
                // write the documentation
                DocumentationVisitor visitor = new DocumentationVisitor(comptoclas, clasname, comp, dropanno, ref,
                    indexes, prefixes);
                walker.walkElement(comp, visitor);
                strwriter.flush();
                
            }
            
            // convert generated schema fragment to plain text for embedding
            String text = strwriter.toString();
            StringBuffer buff = new StringBuffer(text.length() + text.length() / 4);
            if (text.indexOf('&') >= 0) {
                escapeText('&', "&amp;", text, buff);
                text = buff.toString();
                buff = new StringBuffer(text.length() + text.length() / 4);
            }
            buff.append("Schema fragment(s) for this class:");
            buff.append(COMMENT_LEAD_TEXT);
            buff.append("<pre>");
            buff.append(COMMENT_LEAD_TEXT);
            escapeText('<', "&lt;", text, buff);
            buff.append(COMMENT_LEAD_TEXT);
            buff.append("</pre>");
            
            // make sure there's no embedded comment end marker
            int index = buff.length();
            while ((index = buff.lastIndexOf("*/", index)) >= 0) {
                buff.replace(index, index+2, "* /");
            }
            return buff.toString();
        }
    }
    
    /**
     * Visitor to write the filtered view of a schema definition matching the data structure of a class. This uses a
     * supplied map for components which are represented by separate classes, which need to be replaced in the filtered
     * view by a reference to the appropriate class.
     */
    private class DocumentationVisitor extends SchemaVisitor
    {
        /** Map from schema components with separate classes to the class information. */
        private final Map m_componentClassMap;
        
        /** Fully-qualified name of class containing documentation. */
        private final String m_className;
        
        /** Root component to be documented. */
        private final AnnotatedBase m_component;
        
        /** Delete annotations from schema documentation flag. */
        private final boolean m_dropAnnotations;
        
        /** Reference component with separate class flag. */
        private final boolean m_reference;
        
        /** Namespace indexes for use on marshalling root element. */
        private final int[] m_nsIndexes;
        
        /** Namespace prefixes for use on marshalling root element. */
        private final String[] m_nsPrefixes;
        
        /**
         * Constructor.
         * 
         * @param comptoclas map from schema components to class information
         * @param clasname fully-qualified class name to be stripped from class references
         * @param comp top-level component for documentation
         * @param dropanno delete annotations from schema documentation flag
         * @param ref reference component with separate class flag
         * @param indexes namespace indexes for use on marshalling root element
         * @param prefixes namespace prefixes for use on marshalling root element
         */
        public DocumentationVisitor(Map comptoclas, String clasname, AnnotatedBase comp, boolean dropanno, boolean ref,
            int[] indexes, String[] prefixes) {
            m_componentClassMap = comptoclas;
            m_className = clasname;
            m_component = comp;
            m_dropAnnotations = dropanno;
            m_reference = ref;
            m_nsIndexes = indexes;
            m_nsPrefixes = prefixes;
        }
        
        /**
         * Exit a schema node. This just writes the end tag for the node.
         *
         * @param node
         */
        public void exit(SchemaBase node) {
            try {
                m_context.endTag(m_schemaIndex, node.name());
            } catch (JiBXException e) {
                s_logger.fatal("Binding failed", e);
                throw new IllegalStateException("Internal error - binding failed: " + e.getMessage());
            }
        }

        /**
         * Exit an annotation element. If annotations are being deleted, this just returns without calling the
         * next-level method, so that no close tag will be written.
         *
         * @param node
         */
        public void exit(AnnotationElement node) {
            if (!m_dropAnnotations) {
                super.exit(node);
            }
        }

        /**
         * Exit an element element. If the extension says the element is excluded, this just returns without calling the
         * next-level method, so that no close tag will be written.
         *
         * @param node
         */
        public void exit(ElementElement node) {
            Object extension = node.getExtension();
            if (!(extension instanceof ComponentExtension) || !((ComponentExtension)extension).isIgnored()) {
                super.exit(node);
            }
        }

        /**
         * Exit an identity constraint element. This just always returns immediately, since the identity constraint
         * elements are handled in-line in the binding.
         *
         * @param node
         */
        public void exit(KeyBase node) {
        }

        /**
         * Visit a schema node. This first writes the start tag for the node.  If the schema node is represented by a
         * separate class this then just writes text content referencing that class, and returns blocking further
         * expansion; otherwise, it just returns for further expansion requested.
         *
         * @param node
         * @return <code>true</code> if expanding content, <code>false</code> if content replaced by reference
         */
        public boolean visit(SchemaBase node) {
            boolean expand = false;
            try {
                
                // first marshal the element start tag and attributes
                if (node == m_component) {
                    int[] copy = new int[m_nsIndexes.length];
                    System.arraycopy(m_nsIndexes, 0, copy, 0, m_nsIndexes.length);
                    m_context.startTagNamespaces(m_schemaIndex, node.name(), copy, m_nsPrefixes);
                } else {
                    m_context.startTagAttributes(m_schemaIndex, node.name());
                }
                IMarshaller marshaller = m_context.getMarshaller(node.getClass().getName());
                marshaller.marshal(node, m_context);
                m_context.closeStartContent();
                
                // check for separate class reference to determine content handling
                IClassHolder clas = (IClassHolder)m_componentClassMap.get(node);
                if (clas != null && (m_reference || node != m_component)) {
                    
                    // replace content with comment reference to class holding data
                    String name = clas.getFullName();
                    StringBuffer text = new StringBuffer();
                    int length = m_className.length();
                    if (name.startsWith(m_className) && name.length() > length && name.charAt(length) == '.') {
                        text.append(" Reference to inner class ");
                        text.append(name.substring(length+1));
                    } else {
                        text.append(" Reference to class ");
                        text.append(name);
                    }
                    text.append(' ');
                    IXMLWriter writer = m_context.getXmlWriter();
                    writer.indent();
                    writer.writeComment(text.toString());
                    
                } else {
                    expand = true;
                }
                
            } catch (IOException e) {
                s_logger.fatal("Write failed", e);
                throw new IllegalStateException("Internal error - write failed: " + e.getMessage());
            } catch (JiBXException e) {
                s_logger.fatal("Error writing schema XML representation", e);
                throw new IllegalStateException("Internal error - error writing schema XML representation: " + e.getMessage());
            }
            return expand;
        }

        /**
         * Visit an annotation element. If annotations are being deleted this just returns without calling the
         * next-level method, so that the element will be ignored.
         *
         * @param node
         * @return <code>false</code> if annotations to be deleted, otherwise the result of the next-level method
         */
        public boolean visit(AnnotationElement node) {
            if (m_dropAnnotations) {
                return false;
            } else {
                return super.visit(node);
            }
        }

        /**
         * Visit an element element. If the extension says this is excluded the element is dropped from the generated
         * schema fragment.
         *
         * @param node
         * @return <code>false</code> if element excluded, otherwise the result of the next-level method
         */
        public boolean visit(ElementElement node) {
            Object extension = node.getExtension();
            if (extension instanceof ComponentExtension && ((ComponentExtension)extension).isIgnored()) {
                return false;
            } else {
                return super.visit(node);
            }
        }

        /**
         * Visit an identity constraint element. This just always returns <code>false</code>, since the identity
         * constraint elements are handled in-line in the binding.
         *
         * @param node
         * @return <code>false</code> to block further expansion
         */
        public boolean visit(KeyBase node) {
            return false;
        }
    }
}
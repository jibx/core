/*
Copyright (c) 2004, Dennis M. Sosnoski
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

package org.jibx.extras;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.ProcessingInstruction;
import org.dom4j.QName;
import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * <p>Base implementation for custom marshaller/unmarshallers to dom4j
 * representation. This provides the basic code used for both single element and
 * content list handling.</p>
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class Dom4JMapperBase extends DocumentModelMapperBase
{
    /** dom4j component construction factory. */
    private static DocumentFactory s_factory = DocumentFactory.getInstance();
    
    /** Current default namespace URI (<code>null</code> if not determined). */
    protected String m_defaultNamespaceURI;
    
    /** Current default namespace index. */
    protected int m_defaultNamespaceIndex;
    
    /**
     * Get index number for declared namespace.
     *
     * @param ns namespace of interest
     * @return namespace index number, or <code>-1</code> if not declared or
     * masked
     */
    
    private int findNamespaceIndex(Namespace ns) {
        if (Namespace.NO_NAMESPACE.equals(ns)) {
            return 0;
        } else if (Namespace.XML_NAMESPACE.equals(ns)) {
            return 1;
        } else {
            String prefix = ns.getPrefix();
            if (prefix == null || prefix.length() == 0) {
                if (m_defaultNamespaceURI == null) {
                    int index = m_xmlWriter.getPrefixIndex("");
                    if (index >= 0) {
                        m_defaultNamespaceURI = getNamespaceUri(index);
                        m_defaultNamespaceIndex = index;
                        if (m_defaultNamespaceURI.equals(ns.getURI())) {
                            return index;
                        } else {
                            return -1;
                        }
                    } else {
                        return -1;
                    }
                } else {
                    return m_defaultNamespaceURI.equals(ns.getURI()) ?
                        m_defaultNamespaceIndex : -1;
                }
            } else {
                int index = m_xmlWriter.getPrefixIndex(prefix);
                if (index >= 0) {
                    return getNamespaceUri(index).equals(ns.getURI()) ?
                        index : -1;
                } else {
                    return -1;
                }
            }
        }
    }
    
    /**
     * Marshal content list.
     *
     * @param content list of content items to marshal
     * @exception JiBXException on error in marshalling
     * @exception IOException on error writing to output
     */
    
    protected void marshalContent(List content)
        throws JiBXException, IOException {
        int size = content.size();
        for (int i = 0; i < size; i++) {
            Node node = (Node)content.get(i);
            switch (node.getNodeType()) {
                
                case Node.CDATA_SECTION_NODE:
                    m_xmlWriter.writeCData(node.getText());
                    break;
                
                case Node.COMMENT_NODE:
                    m_xmlWriter.writeComment(node.getText());
                    break;
                
                case Node.ELEMENT_NODE:
                    marshalElement((Element)node);
                    break;
                
                case Node.ENTITY_REFERENCE_NODE:
                    m_xmlWriter.writeEntityRef(node.getName());
                    break;
                
                case Node.PROCESSING_INSTRUCTION_NODE:
                    m_xmlWriter.writePI(((ProcessingInstruction)node).
                        getTarget(), node.getText());
                    break;
                
                case Node.TEXT_NODE:
                    m_xmlWriter.writeTextContent(node.getText());
                    break;
                
                default:
                    break;
            }
        }
    }
    
    /**
     * Marshal element with all attributes and content.
     *
     * @param element element to be marshalled
     * @exception JiBXException on error in marshalling
     * @exception IOException on error writing to output
     */
    
    protected void marshalElement(Element element)
        throws JiBXException, IOException {
        
        // accumulate all needed namespace declarations
        int size = element.nodeCount();
        Namespace ns = element.getNamespace();
        int nsi = findNamespaceIndex(ns);
        ArrayList nss = null;
        boolean hascontent = false;
        int defind = -1;
        String defuri = null;
        for (int i = 0; i < size; i++) {
            Node node = element.node(i);
            if (node instanceof Namespace) {
                Namespace dns = (Namespace)node;
                if (findNamespaceIndex(dns) < 0) {
                    if (nss == null) {
                        nss = new ArrayList();
                    }
                    nss.add(dns);
                    String prefix = dns.getPrefix();
                    if (prefix == null || prefix.length() == 0) {
                        defind = nss.size() - 1;
                        defuri = dns.getURI();
                    }
                }
            } else {
                hascontent = true;
            }
        }
        
        // check for namespace declarations required
        String[] uris = null;
        if (nss == null) {
            m_xmlWriter.startTagOpen(nsi, element.getName());
        } else {
            int base = getNextNamespaceIndex();
            if (defind >= 0) {
                m_defaultNamespaceIndex = base + defind;
                m_defaultNamespaceURI = defuri;
            }
            uris = new String[nss.size()];
            int[] nums = new int[nss.size()];
            String[] prefs = new String[nss.size()];
            for (int i = 0; i < uris.length; i++) {
                Namespace addns = (Namespace)nss.get(i);
                uris[i] = addns.getURI();
                nums[i] = base + i;
                prefs[i] = addns.getPrefix();
                if (nsi < 0 && ns.equals(addns)) {
                    nsi = base + i;
                }
            }
            m_xmlWriter.pushExtensionNamespaces(uris);
            m_xmlWriter.startTagNamespaces(nsi, element.getName(), nums, prefs);
            if (defind >= 0) {
                m_defaultNamespaceIndex = defind;
                m_defaultNamespaceURI = defuri;
            }
        }
        
        // add attributes if present
        if (element.attributeCount() > 0) {
            for (int i = 0; i < element.attributeCount(); i++) {
                Attribute attr = element.attribute(i);
                int index = findNamespaceIndex(attr.getNamespace());
                m_xmlWriter.addAttribute(index, attr.getName(),
                     attr.getValue());
            }
        }
        
        // check for content present
        if (hascontent) {
            m_xmlWriter.closeStartTag();
            marshalContent(element.content());
            m_xmlWriter.endTag(nsi, element.getName());
        } else {
            m_xmlWriter.closeEmptyTag();
        }
        
        // pop namespaces if defined by element
        if (nss != null) {
            m_xmlWriter.popExtensionNamespaces();
            if (defind >= 0) {
                m_defaultNamespaceURI = null;
            }
        }
    }
    
    /**
     * Unmarshal element content. This unmarshals everything up to the
     * containing element close tag, adding each component to the content list
     * supplied. On return, the parse position will always be at an END_TAG.
     *
     * @param content list for unmarshalled content
     * @exception JiBXException on error in unmarshalling
     * @exception IOException on error reading input
     */
    
    protected void unmarshalContent(List content)
        throws JiBXException, IOException {
        
        // loop until end of containing element found
        loop: while (true) {
            int cev = m_unmarshalContext.currentEvent();
            switch (cev) {
                
                case IXMLReader.CDSECT:
                    content.add(s_factory.
                        createCDATA(m_unmarshalContext.getText()));
                    break;
                
                case IXMLReader.COMMENT:
                    content.add(s_factory.
                        createComment(m_unmarshalContext.getText()));
                    break;
                
                case IXMLReader.END_TAG:
                    break loop;
                
                case IXMLReader.ENTITY_REF:
                    if (m_unmarshalContext.getText() == null) {
                        content.add(s_factory.
                            createEntity(m_unmarshalContext.getName(), null));
                        break;
                    } else {
                        content.add(s_factory.createText(accumulateText()));
                        continue loop;
                    }
                
                case IXMLReader.PROCESSING_INSTRUCTION:
                    {
                        String text = m_unmarshalContext.getText();
                        int index = 0;
                        while (++index < text.length() &&
                            !isWhitespace(text.charAt(index)));
                        if (index < text.length()) {
                            String target = text.substring(0, index);
                            while (++index < text.length() &&
                                isWhitespace(text.charAt(index)));
                            String data = text.substring(index);
                            content.add(s_factory.
                                createProcessingInstruction(target, data));
                        } else {
                            content.add(s_factory.
                                createProcessingInstruction(text, ""));
                        }
                    }
                    break;
                
                case IXMLReader.START_TAG:
                    content.add(unmarshalElement());
                    continue loop;
                
                case IXMLReader.TEXT:
                    content.add(s_factory.createText(accumulateText()));
                    continue loop;
                    
            }
            m_unmarshalContext.nextToken();
        }
    }
    
    /**
     * Unmarshal element with all attributes and content. This must be called
     * with the unmarshalling context positioned at a START_TAG event.
     *
     * @return unmarshalled element
     * @exception JiBXException on error in unmarshalling
     * @exception IOException on error reading input
     */
    
    protected Element unmarshalElement() throws JiBXException, IOException {
        
        // start by creating the actual element
        QName qname = QName.get(m_unmarshalContext.getName(),
            m_unmarshalContext.getPrefix(), m_unmarshalContext.getNamespace());
        Element element = s_factory.createElement(qname);
        
        // add all namespace declarations to element
        int ncount = m_unmarshalContext.getNamespaceCount();
        for (int i = 0; i < ncount; i++) {
            String prefix = m_unmarshalContext.getNamespacePrefix(i);
            String uri = m_unmarshalContext.getNamespaceUri(i);
            element.addNamespace(prefix, uri);
        }
        
        // add all attributes to element
        int acount = m_unmarshalContext.getAttributeCount();
        for (int i = 0; i < acount; i++) {
            String prefix = m_unmarshalContext.getAttributePrefix(i);
            String uri = m_unmarshalContext.getAttributeNamespace(i);
            String name = m_unmarshalContext.getAttributeName(i);
            String value = m_unmarshalContext.getAttributeValue(i);
            qname = QName.get(name, prefix, uri);
            element.addAttribute(qname, value);
        }
        
        // add all content to element
        int event = m_unmarshalContext.nextToken();
        if (event != IXMLReader.END_TAG) {
            unmarshalContent(element.content());
        }
        m_unmarshalContext.nextToken();
        return element;
    }
}
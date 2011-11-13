/*
Copyright (c) 2004-2010, Dennis M. Sosnoski.
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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>Base implementation for custom marshaller/unmarshallers to DOM
 * representation. This provides the basic code used for both single element and
 * content list handling.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public class DomMapperBase extends DocumentModelMapperBase
{
    /** Actual document instance (required by DOM). */
    protected Document m_document;             
    
    /** Current default namespace URI (<code>null</code> if not determined). */
    protected String m_defaultNamespaceURI;
    
    /** Current default namespace index. */
    protected int m_defaultNamespaceIndex;
    
    /**
     * Constructor. Initializes the document used by this
     * marshaller/unmarshaller instance as the owner of all DOM components.
     * 
     * @throws JiBXException on error creating document
     */
    protected DomMapperBase() throws JiBXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        try {
            m_document = dbf.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new JiBXException("Unable to create DOM document", e);
        }
    }
    
    /**
     * Get index number for declared namespace.
     *
     * @param prefix namespace prefix (<code>null</code> if none)
     * @param uri namespace URI (empty string if none)
     * @return namespace index number, or <code>-1</code> if not declared or
     * masked
     */
    
    private int findNamespaceIndex(String prefix, String uri) {
        if ((prefix == null || "".equals(prefix)) &&
            (uri == null || "".equals(uri))) {
            return 0;
        } else if ("xml".equals(prefix) && XML_NAMESPACE.equals(uri)) {
            return 1;
        } else {
            if (prefix == null) {
                if (m_defaultNamespaceURI == null) {
                    int index = m_xmlWriter.getPrefixIndex("");
                    if (index >= 0) {
                        m_defaultNamespaceURI = getNamespaceUri(index);
                        m_defaultNamespaceIndex = index;
                        if (m_defaultNamespaceURI.equals(uri)) {
                            return index;
                        } else {
                            return -1;
                        }
                    } else {
                        return -1;
                    }
                } else {
                    return m_defaultNamespaceURI.equals(uri) ?
                        m_defaultNamespaceIndex : -1;
                }
            } else {
                int index = m_xmlWriter.getPrefixIndex(prefix);
                if (index >= 0) {
                    return getNamespaceUri(index).equals(uri) ?
                        index : -1;
                } else {
                    return -1;
                }
            }
        }
    }
    
    /**
     * Marshal node.
     *
     * @param node node to be marshalled
     * @exception JiBXException on error in marshalling
     * @exception IOException on error writing to output
     */
    
    protected void marshalNode(Node node) throws JiBXException, IOException {
        switch (node.getNodeType()) {
            
            case Node.CDATA_SECTION_NODE:
                m_xmlWriter.writeCData(node.getNodeValue());
                break;
            
            case Node.COMMENT_NODE:
                m_xmlWriter.writeComment(node.getNodeValue());
                break;
            
            case Node.ELEMENT_NODE:
                marshalElement((Element)node);
                break;
            
            case Node.ENTITY_REFERENCE_NODE:
                m_xmlWriter.writeEntityRef(node.getNodeName());
                break;
            
            case Node.PROCESSING_INSTRUCTION_NODE:
                m_xmlWriter.writePI(node.getNodeName(),
                    node.getNodeValue());
                break;
            
            case Node.TEXT_NODE:
                m_xmlWriter.writeTextContent(node.getNodeValue());
                break;
            
            default:
                break;
        }
    }
    
    /**
     * Marshal node list.
     *
     * @param content list of nodes to marshal
     * @exception JiBXException on error in marshalling
     * @exception IOException on error writing to output
     */
    
    protected void marshalContent(NodeList content)
        throws JiBXException, IOException {
        int size = content.getLength();
        for (int i = 0; i < size; i++) {
            marshalNode(content.item(i));
        }
    }
    
    /**
     * Add namespace information to list.
     * 
     * @param prefix
     * @param uri
     * @param nss
     */
    private void addNamespace(String prefix, String uri, ArrayList nss) {
        nss.add(prefix == null ? "" : prefix);
        nss.add(uri == null ? "" : uri);
    }
    
    /**
     * Check if a pair of strings are equivalent, meaning either equal or one
     * empty and the other <code>null</code>.
     * 
     * @param a non-<code>null</code> value
     * @param b comparison value (may be <code>null</code>)
     * @return <code>true</code> if equivalent, <code>false</code> if not
     */
    private boolean isEquivalent(String a, String b) {
        return a.equals(b) || (a.length() == 0 && b == null);
    }

    /**
     * Add namespace information to list if not already present.
     * 
     * @param prefix
     * @param uri
     * @param nss
     */
    private void addNamespaceUnique(String prefix, String uri, ArrayList nss) {
        boolean found = false;
        for (int i = 0; i < nss.size(); i += 2) {
            if (isEquivalent((String)nss.get(i), prefix) &&
                isEquivalent((String)nss.get(i+1), uri)) {
                found = true;
                break;
            }
        }
        if (!found) {
            addNamespace(prefix, uri, nss);
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
        
        // accumulate namespace declarations from this element
        ArrayList nss = null;
        NamedNodeMap attrs = element.getAttributes();
        int size = attrs.getLength();
        for (int i = 0; i < size; i++) {
            Attr attr = (Attr)attrs.item(i);
            if (XMLNS_NAMESPACE.equals(attr.getNamespaceURI())) {
                
                // found namespace declaration, convert to simple prefix
                String declpref = attr.getLocalName();
                if ("xmlns".equals(declpref)) {
                    declpref = null;
                }
                String decluri = attr.getValue();
                if (findNamespaceIndex(declpref, decluri) < 0) {
                    if (nss == null) {
                        nss = new ArrayList();
                    }
                    addNamespace(declpref, decluri, nss);
                }
            }
        }
        
        // check that namespace used by element name is defined
        String prefix = element.getPrefix();
        String uri = element.getNamespaceURI();
        int nsi = findNamespaceIndex(prefix, uri);
        if (nsi < 0) {
            if (nss == null) {
                nss = new ArrayList();
            }
            addNamespaceUnique(prefix, uri, nss);
        }
        
        // check that every namespace used by an attribute is defined
        for (int i = 0; i < size; i++) {
            Attr attr = (Attr)attrs.item(i);
            if (!XMLNS_NAMESPACE.equals(attr.getNamespaceURI())) {
                
                // found normal attribute, check namespace and prefix
                String attruri = attr.getNamespaceURI();
                if (attruri != null) {
                    String attrpref = attr.getPrefix();
                    if (findNamespaceIndex(attrpref, attruri) < 0) {
                        if (nss == null) {
                            nss = new ArrayList();
                        }
                        addNamespaceUnique(attrpref, attruri, nss);
                    }
                }
            }
        }
        
        // check for default namespace setting
        int defind = -1;
        String defuri = null;
        if (nss != null) {
            for (int i = 0; i < nss.size(); i += 2) {
                if ("".equals(nss.get(i))) {
                    defind = i / 2;
                    defuri = (String)nss.get(i+1);
                }
            }
        }
        
        // check for namespace declarations required
        String[] uris = null;
        String name = element.getLocalName();
        if (name == null) {
            name = element.getTagName();
        }
        if (nss == null) {
            m_xmlWriter.startTagOpen(nsi, name);
        } else {
            int base = getNextNamespaceIndex();
            if (defind >= 0) {
                m_defaultNamespaceIndex = base + defind;
                m_defaultNamespaceURI = defuri;
            }
            int length = nss.size() / 2;
            uris = new String[length];
            int[] nums = new int[length];
            String[] prefs = new String[length];
            for (int i = 0; i < length; i++) {
                prefs[i] = (String)nss.get(i*2);
                uris[i] = (String)nss.get(i*2+1);
                nums[i] = base + i;
                if (nsi < 0 && uri.equals(uris[i])) {
                    if ((prefix == null && prefs[i] == "") ||
                        (prefix != null && prefix.equals(prefs[i]))) {
                        nsi = base + i;
                    }
                }
            }
            m_xmlWriter.pushExtensionNamespaces(uris);
            m_xmlWriter.startTagNamespaces(nsi, name, nums, prefs);
            if (defind >= 0) {
                m_defaultNamespaceIndex = defind;
                m_defaultNamespaceURI = defuri;
            }
        }
        
        // add attributes if present
        for (int i = 0; i < size; i++) {
            Attr attr = (Attr)attrs.item(i);
            if (!XMLNS_NAMESPACE.equals(attr.getNamespaceURI())) {
                int index = 0;
                String apref = attr.getPrefix();
                if (apref != null) {
                    index = findNamespaceIndex(apref, attr.getNamespaceURI());
                }
                String aname = attr.getLocalName();
                if (aname == null) {
                    aname = attr.getName();
                }
                m_xmlWriter.addAttribute(index, aname, attr.getValue());
            }
        }
        
        // check for content present
        NodeList nodes = element.getChildNodes();
        size = nodes.getLength();
        if (size > 0) {
            m_xmlWriter.closeStartTag();
            marshalContent(element.getChildNodes());
            m_xmlWriter.endTag(nsi, name);
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
     * Unmarshal single node. This unmarshals the next node from the input
     * stream, up to the close tag of the containing element.
     *
     * @return unmarshalled node
     * @exception JiBXException on error in unmarshalling
     * @exception IOException on error reading input
     */
    
    protected Node unmarshalNode() throws JiBXException, IOException {
        while (true) {
            int cev = m_unmarshalContext.currentEvent();
            switch (cev) {
                
                case IXMLReader.CDSECT:
                    {
                        String text = m_unmarshalContext.getText();
                        m_unmarshalContext.nextToken();
                        return m_document.createCDATASection(text);
                    }
                
                case IXMLReader.COMMENT:
                    {
                        String text = m_unmarshalContext.getText();
                        m_unmarshalContext.nextToken();
                        return m_document.createComment(text);
                    }
                
                case IXMLReader.END_TAG:
                    return null;
                
                case IXMLReader.ENTITY_REF:
                    if (m_unmarshalContext.getText() == null) {
                        String name = m_unmarshalContext.getName();
                        m_unmarshalContext.nextToken();
                        return m_document.createEntityReference(name);
                    } else {
                        String text = accumulateText();
                        return m_document.createTextNode(text);
                    }
                
                case IXMLReader.PROCESSING_INSTRUCTION:
                    {
                        String text = m_unmarshalContext.getText();
                        m_unmarshalContext.nextToken();
                        int index = 0;
                        while (++index < text.length() &&
                            !isWhitespace(text.charAt(index)));
                        if (index < text.length()) {
                            String target = text.substring(0, index);
                            while (++index < text.length() &&
                                isWhitespace(text.charAt(index)));
                            String data = text.substring(index);
                            return m_document.
                                createProcessingInstruction(target, data);
                        } else {
                            return m_document.
                                createProcessingInstruction(text, "");
                        }
                    }
                
                case IXMLReader.START_TAG:
                    return unmarshalElement();
                
                case IXMLReader.TEXT:
                    return m_document.createTextNode(accumulateText());
                    
                default:
                    m_unmarshalContext.nextToken();
                    
            }
        }
    }
    
    /**
     * Unmarshal node content. This unmarshals everything up to the containing
     * element close tag, adding each component to the content list supplied. On
     * return, the parse position will always be at an END_TAG.
     *
     * @param parent node to which children are to be added 
     * @exception JiBXException on error in unmarshalling
     * @exception IOException on error reading input
     */
    
    protected void unmarshalContent(Node parent)
        throws JiBXException, IOException {
        Node node;
        while ((node = unmarshalNode()) != null) {
            parent.appendChild(node);
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
        String uri = m_unmarshalContext.getNamespace();
        String prefix = m_unmarshalContext.getPrefix();
        String name = m_unmarshalContext.getName();
        if (prefix != null) {
            name = prefix + ':' + name;
        }
        Element element = m_document.createElementNS(uri, name);
        
        // add all namespace declarations to element
        int ncount = m_unmarshalContext.getNamespaceCount();
        for (int i = 0; i < ncount; i++) {
            prefix = m_unmarshalContext.getNamespacePrefix(i);
            uri = m_unmarshalContext.getNamespaceUri(i);
            if (prefix == null) {
                element.setAttributeNS(XMLNS_NAMESPACE, "xmlns", uri);
            } else {
                element.setAttributeNS(XMLNS_NAMESPACE, "xmlns:" + prefix, uri);
            }
        }
        
        // add all attributes to element
        int acount = m_unmarshalContext.getAttributeCount();
        for (int i = 0; i < acount; i++) {
            prefix = m_unmarshalContext.getAttributePrefix(i);
            uri = m_unmarshalContext.getAttributeNamespace(i);
            name = m_unmarshalContext.getAttributeName(i);
            if (prefix != null) {
                name = prefix + ':' + name;
            }
            String value = m_unmarshalContext.getAttributeValue(i);
            element.setAttributeNS(uri, name, value);
        }
        
        // add all content to element
        int event = m_unmarshalContext.nextToken();
        if (event != IXMLReader.END_TAG) {
            unmarshalContent(element);
        }
        m_unmarshalContext.nextToken();
        return element;
    }
}
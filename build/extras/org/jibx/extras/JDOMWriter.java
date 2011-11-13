/*
Copyright (c) 2004-2008, Dennis M. Sosnoski.
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

import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jibx.runtime.impl.XMLWriterNamespaceBase;

/**
 * JDOM implementation of XML writer interface. The <code>Document</code> that is
 * created can be accessed by using <code>getDocument()</code>.
 * 
 * @author Andreas Brenk
 * @version 1.0
 */
public class JDOMWriter extends XMLWriterNamespaceBase {

    /**
     * The JDOM <code>Document</code> this writer is creating.
     */
    private Document document;
    
    /**
     * The currently open <code>Element</code> that is used for add* methods.
     */
    private Element currentElement;
    
    /**
     * Creates a new instance with the given namespace URIs.
     */
    public JDOMWriter(String[] namespaces) {
        super(namespaces);
        reset();
    }
    
    /**
     * Creates a new instance with the given Document as target for marshalling.
     * 
     * @param document must not be null
     */
    public JDOMWriter(String[] namespaces, Document document) {
        this(namespaces);
        this.document = document;
        if(document.hasRootElement()) {
            this.currentElement = document.getRootElement();
        }
    }

    /**
     * Creates a new instance with the given Element as target for marshalling.
     * 
     * @param currentElement must not be null
     */
    public JDOMWriter(String[] namespaces, Element currentElement) {
        this(namespaces, currentElement.getDocument());
        this.currentElement = currentElement;
    }
    
    /**
     * Does nothing.
     */
    public void init() {
        // do nothing
    }
    
    /**
     * Does nothing.
     */
    public void setIndentSpaces(int count, String newline, char indent) {
        // do nothing
    }

    /**
     * Does nothing.
     */
    public void writeXMLDecl(String version, String encoding, String standalone) throws IOException {
        // do nothing
    }

    public void startTagOpen(int index, String name) throws IOException {
        Element newElement = new Element(name, getNamespace(index));
        
        if(this.currentElement == null) {
            this.document.setRootElement(newElement);
        } else {
            this.currentElement.addContent(newElement);
        }
        
        this.currentElement = newElement;
    }

    public void startTagNamespaces(int index, String name, int[] nums, String[] prefs) throws IOException {
        // find the namespaces actually being declared
        int[] deltas = openNamespaces(nums, prefs);
        
        // create the start tag for element
        startTagOpen(index, name);
        
        // add namespace declarations to open element
        for (int i = 0; i < deltas.length; i++) {
            int slot = deltas[i];
            this.currentElement.addNamespaceDeclaration(getNamespace(slot));
        }
    }

    public void addAttribute(int index, String name, String value)
            throws IOException {
        this.currentElement.setAttribute(name, value, getNamespace(index));
    }

    public void closeStartTag() throws IOException {
        incrementNesting();
    }

    public void closeEmptyTag() throws IOException {
        incrementNesting();
        decrementNesting();
        this.currentElement = this.currentElement.getParentElement();
    }

    public void startTagClosed(int index, String name) throws IOException {
        startTagOpen(index, name);
        closeStartTag();
    }

    public void endTag(int index, String name) throws IOException {
        decrementNesting();
        this.currentElement = this.currentElement.getParentElement();
    }

    public void writeTextContent(String text) throws IOException {
        this.currentElement.addContent(new Text(text));
    }

    public void writeCData(String text) throws IOException {
        this.currentElement.addContent(new CDATA(text));
    }

    public void writeComment(String text) throws IOException {
        this.currentElement.addContent(new Comment(text));
    }

    public void writeEntityRef(String name) throws IOException {
        this.currentElement.addContent(new EntityRef(name));
    }

    public void writeDocType(String name, String sys, String pub, String subset)
            throws IOException {
        DocType docType;
        if(null != pub) {
            docType = new DocType(name, pub, sys);
        } else if(null != sys) {
            docType = new DocType(name, sys);
        } else {
            docType = new DocType(name);
        }
        if(null != subset) {
            docType.setInternalSubset(subset);
        }
        this.document.setDocType(docType);
    }

    public void writePI(String target, String data) throws IOException {
        this.currentElement.addContent(new ProcessingInstruction(target, data));
    }

    /**
     * Does nothing.
     */
    public void indent() throws IOException {
        // do nothing
    }

    /**
     * Does nothing.
     */
    public void flush() throws IOException {
        // do nothing
    }
    
    /**
     * Does nothing.
     */
    public void close() throws IOException {
        // do nothing
    }

    public void reset() {
        super.reset();
        this.document = new Document();
        this.currentElement = null;
    }
    
    /**
     * @return the JDOM <code>Document</code> this writer created.
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Does nothing.
     */
    protected void defineNamespace(int index, String prefix) throws IOException {
        // do nothing
    }

    /**
     * Does nothing.
     */
    protected void undefineNamespace(int index) {
        // do nothing
    }

    /**
     * This will retrieve (if in existence) or create (if not) a 
     * <code>Namespace</code> for the supplied namespace index.
     */
    private Namespace getNamespace(int index) {
        String prefix = getNamespacePrefix(index);
        String uri = getNamespaceUri(index);
        if(prefix == null) {
            return Namespace.getNamespace(uri);
        } else {
            return Namespace.getNamespace(prefix, uri);
        }
    }
}

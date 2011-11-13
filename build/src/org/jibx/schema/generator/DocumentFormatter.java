/*
 * Copyright (c) 2007-2008, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.generator;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jibx.custom.classes.IDocumentFormatter;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Formatter for JavaDoc conversion to XML documentation components.
 */
public class DocumentFormatter implements IDocumentFormatter
{
    /** Document used for constructing DOM components. */
    private final Document m_document;
    
    /**
     * Constructor.
     */
    public DocumentFormatter() {
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            m_document = fact.newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException("Internal error: unable to create DOM builder", e);
        }
    }
    
    /**
     * Reformat a segment of JavaDoc text as either a CDATA section (if it contains embedded HTML tags) or a simple text
     * node. This also replaces line breaks with single spaces, so that the output format will not use indenting based
     * on the original supplied text.
     * 
     * @param jdoc raw JavaDoc text
     * @return formatted text
     */
    protected Node reformDocSegment(String jdoc) {
        StringBuffer buff = new StringBuffer(jdoc);
        int index = 0;
        boolean dirty = false;
        while (index < buff.length()) {
            char chr = buff.charAt(index);
            if (chr < 0x20) {
                if (chr == '\n' || chr == '\r') {
                    if ((index > 0 && buff.charAt(index) == ' ')
                        || (index + 1 < buff.length() && buff.charAt(index + 1) == ' ')) {
                        buff.deleteCharAt(index);
                    } else {
                        buff.setCharAt(index, ' ');
                    }
                } else {
                    buff.deleteCharAt(index);
                }
            } else {
                dirty = dirty || chr == '&' || chr == '<';
                index++;
            }
        }
        String text = buff.toString();
        if (dirty) {
            return m_document.createCDATASection(text);
        } else {
            return m_document.createTextNode(text);
        }
    }
    
    /**
     * Convert JavaDoc text to a list of formatted nodes.
     * 
     * @param jdoc JavaDoc text (may be <code>null</code>)
     * @return formatted representation (may be <code>null</code>)
     */
    public List docToNodes(String jdoc) {
        if (jdoc != null) {
            jdoc = jdoc.trim();
            if (jdoc.length() > 0) {
                List nodes = new ArrayList();
                boolean dirty = jdoc.indexOf('<') >= 0;
                if (dirty) {
                    String ldoc = jdoc.toLowerCase();
                    int split;
                    int base = 0;
                    while ((split = ldoc.indexOf("<pre>", base)) > 0) {
                        if (split > base) {
                            nodes.add(reformDocSegment(jdoc.substring(base, split)));
                        }
                        int end = ldoc.lastIndexOf("</pre>");
                        if (end < 0) {
                            end = ldoc.length();
                        }
                        nodes.add(reformDocSegment(jdoc.substring(split, end)));
                        base = end + 1;
                    }
                    if (base < jdoc.length()) {
                        nodes.add(reformDocSegment(jdoc.substring(base)));
                    }
                } else {
                    nodes.add(reformDocSegment(jdoc));
                }
                return nodes;
            }
        }
        return null;
    }
    
    /**
     * Get formatted documentation from class, in the form of a list of <code>org.w3c.dom.Node</code> instances.
     * 
     * @param info class information
     * @return formatted documentation (<code>null</code> if none)
     */
    public List getClassDocumentation(IClass info) {
        return docToNodes(info.getJavaDoc());
    }
    
    /**
     * Get formatted documentation from class item, in the form of a list of <code>org.w3c.dom.Node</code> instances.
     * 
     * @param info item information
     * @return formatted documentation (<code>null</code> if none)
     */
    public List getItemDocumentation(IClassItem info) {
        return docToNodes(info.getJavaDoc());
    }
}
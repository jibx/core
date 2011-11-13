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

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * <p>Base implementation for custom marshaller/unmarshallers to any document
 * model representation. This class just provides a few basic operations that
 * are used by the representation-specific subclasses.</p>
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class DocumentModelMapperBase
{
    /** Fixed XML namespace. */
    public static final String XML_NAMESPACE = 
        "http://www.w3.org/XML/1998/namespace";
    
    /** Fixed XML namespace namespace. */
    public static final String XMLNS_NAMESPACE =
        "http://www.w3.org/2000/xmlns/";
        
    /** Writer for direct output as XML. */
    protected IXMLWriter m_xmlWriter;
    
    /** Context being used for unmarshalling. */
    protected UnmarshallingContext m_unmarshalContext;
    
    /**
     * Get namespace URI for index.
     *
     * @param index namespace index to look up
     * @return uri namespace URI at index position
     */
    
    protected String getNamespaceUri(int index) {
        String[] uris = m_xmlWriter.getNamespaces();
        if (index < uris.length) {
            return uris[index];
        } else {
            index -= uris.length;
            String[][] uriss = m_xmlWriter.getExtensionNamespaces();
            if (uriss != null) {
                for (int i = 0; i < uriss.length; i++) {
                    uris = uriss[i];
                    if (index < uris.length) {
                        return uris[index];
                    } else {
                        index -= uris.length;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Get next namespace index.
     *
     * @return next namespace index
     */
    
    protected int getNextNamespaceIndex() {
        int count = m_xmlWriter.getNamespaces().length;
        String[][] uriss = m_xmlWriter.getExtensionNamespaces();
        if (uriss != null) {
            for (int i = 0; i < uriss.length; i++) {
                count += uriss[i].length;
            }
        }
        return count;
    }
    
    /**
     * Accumulate text content. This consolidates consecutive text and entities
     * to a single string.
     *
     * @return consolidated text string
     * @exception JiBXException on error in unmarshalling
     */
    
    protected String accumulateText() throws JiBXException {
        String text = m_unmarshalContext.getText();
        StringBuffer buff = null;
        while (true) {
            int cev = m_unmarshalContext.nextToken();
            if (cev == IXMLReader.TEXT ||
                (cev == IXMLReader.ENTITY_REF &&
                m_unmarshalContext.getText() != null)) {
                if (buff == null) {
                    buff = new StringBuffer(text);
                }
                buff.append(m_unmarshalContext.getText());
            } else {
                break;
            }
        }
        if (buff == null) {
            return text;
        } else {
            return buff.toString();
        }
    }
    
    /**
     * Check if a character is a space character.
     *
     * @param chr character to be checked
     * @return <code>true</code> if whitespace, <code>false</code> if not
     */
    
    protected boolean isWhitespace(char chr) {
        if (chr <= 0x20) {
            return chr == 0x20 || chr == 0x09 || chr == 0x0A || chr == 0x0D;
        } else {
            return false;
        }
    }
}
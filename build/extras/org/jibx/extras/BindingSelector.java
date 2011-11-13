/*
Copyright (c) 2003-2004, Dennis M. Sosnoski
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

import java.io.OutputStream;
import java.io.Writer;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * Binding selector that supports versioned XML documents. This looks for a
 * <i>version</i> attribute on the root element of the document, and selects the
 * mapping to be used for unmarshalling based on the value. It also supports
 * selecting the version for marshalling based on a supplied version argument
 * value.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class BindingSelector
{
    /** URI of version selection attribute. */
    private final String m_attributeUri;
    
    /** Name of version selection attribute. */
    private final String m_attributeName;
    
    /** Array of version names. */
    private final String[] m_versionTexts;
    
    /** Array of bindings corresponding to versions. */
    private final String[] m_versionBindings;
    
    /** Basic unmarshalling context used to determine document version. */
    private final UnmarshallingContext m_context;
    
    /** Stream for marshalling output. */
    private OutputStream m_outputStream;
    
    /** Encoding for output stream. */
    private String m_outputEncoding;
    
    /** Output writer for marshalling. */
    private Writer m_outputWriter;
    
    /** Indentation for marshalling. */
    private int m_outputIndent;
    
    /**
     * Constructor.
     *
     * @param uri version selection attribute URI (<code>null</code> if none)
     * @param name version selection attribute name
     * @param versions array of version texts (first is default)
     * @param bindings array of binding names corresponding to versions
     */
    
    public BindingSelector(String uri, String name, String[] versions,
        String[] bindings) {
        m_attributeUri = uri;
        m_attributeName = name;
        m_versionTexts = versions;
        m_versionBindings = bindings;
        m_context = new UnmarshallingContext();
        m_outputIndent = -1;
    }
    
    /**
     * Get initial unmarshalling context. This gives access to the unmarshalling
     * context used before the specific version is determined. The document
     * information must be set for this context before calling {@link
     * #unmarshalVersioned}.
     *
     * @return initial unmarshalling context
     */
    
    public IUnmarshallingContext getContext() {
        return m_context;
    }
    
    /**
     * Set output stream and encoding.
     *
     * @param outs stream for document data output
     * @param enc document output encoding, or <code>null</code> for default
     */
     
    public void setOutput(OutputStream outs, String enc) {
        m_outputStream = outs;
        m_outputEncoding = enc;
    }
    
    /**
     * Set output writer.
     *
     * @param outw writer for document data output
     */
    
    public void setOutput(Writer outw) {
        m_outputWriter = outw;
    }
    
    /**
     * Set nesting indent spaces.
     *
     * @param indent number of spaces to indent per level, or disable
     * indentation if negative
     */
    
    public void setIndent(int indent) {
        m_outputIndent = indent;
    }
    
    /**
     * Marshal according to supplied version.
     *
     * @param obj root object to be marshalled
     * @param version identifier for version to be used in marshalling
     * @throws JiBXException if error in marshalling
     */
    
    public void marshalVersioned(Object obj, String version)
        throws JiBXException {
        
        // look up version in defined list
        String match = (version == null) ? m_versionTexts[0] : version;
        for (int i = 0; i < m_versionTexts.length; i++) {
            if (match.equals(m_versionTexts[i])) {
                
                // version found, create marshaller for the associated binding
                IBindingFactory fact = BindingDirectory.
                    getFactory(m_versionBindings[i], obj.getClass());
                MarshallingContext context =
                    (MarshallingContext)fact.createMarshallingContext();
                
                // configure marshaller for writing document
                context.setIndent(m_outputIndent);
                if (m_outputWriter == null) {
                    if (m_outputStream == null) {
                        throw new JiBXException("Output not configured");
                    } else {
                        context.setOutput(m_outputStream, m_outputEncoding);
                    }
                } else {
                    context.setOutput(m_outputWriter);
                }
                
                // output object as document
                context.startDocument(m_outputEncoding, null);
                ((IMarshallable)obj).marshal(context);
                context.endDocument();
                return;
                
            }
        }
        
        // error if unknown version in document
        throw new JiBXException("Unrecognized document version " + version);
    }
    
    /**
     * Unmarshal according to document version.
     *
     * @param clas expected class mapped to root element of document (used only
     * to look up the binding)
     * @return root object unmarshalled from document
     * @throws JiBXException if error in unmarshalling
     */
    
    public Object unmarshalVersioned(Class clas) throws JiBXException {
        
        // get the version attribute value (using first value as default)
        m_context.toStart();
        String version = m_context.attributeText(m_attributeUri,
            m_attributeName, m_versionTexts[0]);
        
        // look up version in defined list
        for (int i = 0; i < m_versionTexts.length; i++) {
            if (version.equals(m_versionTexts[i])) {
                
                // version found, create unmarshaller for the associated binding
                IBindingFactory fact = BindingDirectory.
                    getFactory(m_versionBindings[i], clas);
                UnmarshallingContext context =
                    (UnmarshallingContext)fact.createUnmarshallingContext();
                
                // return object unmarshalled using binding for document version
                context.setFromContext(m_context);
                return context.unmarshalElement();
                
            }
        }
        
        // error if unknown version in document
        throw new JiBXException("Unrecognized document version " + version);
    }
}
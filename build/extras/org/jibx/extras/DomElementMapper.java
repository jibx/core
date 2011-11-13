/*
Copyright (c) 2004-2010, Dennis M. Sosnoski. All rights reserved.

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

import org.jibx.runtime.IAliasable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.w3c.dom.Element;

/**
 * <p>Custom element marshaller/unmarshaller to DOM representation. This allows
 * you to mix data binding and document model representations for XML within the
 * same application. You simply use this marshaller/unmarshaller with a linked
 * object of type <code>org.w3c.dom.Element</code> (the actual runtime type -
 * the declared type is ignored and can be anything). If a name is supplied on a
 * reference that element name will always be matched when unmarshalling but
 * will be ignored when marshalling (with the actual DOM element name used). If
 * a namespace is supplied but no name, any element which does <i>not</i> use
 * that namespace will be unmarshalled. Finally, if no name is supplied this
 * will unmarshal a single element with any name.</p>
 * 
 * 
 * @author Dennis M. Sosnoski
 */
public class DomElementMapper extends DomMapperBase
    implements IMarshaller, IUnmarshaller, IAliasable
{
    /** Root element namespace URI. */
    private final String m_uri;
    
    /** Root element name. */
    private final String m_name;
    
    /**
     * Default constructor.
     * 
     * @throws JiBXException on error creating document
     */
    public DomElementMapper() throws JiBXException {
        m_uri = null;
        m_name = null;
    }
    
    /**
     * Aliased constructor. This takes a name definition for the element. It'll
     * be used by JiBX when a name is supplied by the mapping which references
     * this custom marshaller/unmarshaller.
     *
     * @param uri namespace URI for the top-level element
     * @param index namespace index corresponding to the defined URI within the
     * marshalling context definitions
     * @param name local name for the top-level element
     * @throws JiBXException on error creating document
     */
    public DomElementMapper(String uri, int index, String name)
        throws JiBXException {
        
        // save the simple values
        m_uri = uri;
        m_name = name;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.runtime.IMarshaller#isExtension(java.lang.String)
     */
    public boolean isExtension(String mapname) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IMarshaller#marshal(java.lang.Object,
     *  org.jibx.runtime.IMarshallingContext)
     */
    public void marshal(Object obj, IMarshallingContext ictx)
        throws JiBXException {
        
        // make sure the parameters are as expected
        if (!(obj instanceof Element)) {
            throw new JiBXException("Mapped object not an org.w3c.dom.Element");
        } else {
            try {
                
                // marshal element and all content with only leading indentation
                m_xmlWriter = ictx.getXmlWriter();
                m_xmlWriter.indent();
                int indent = ictx.getIndent();
                ictx.setIndent(-1);
                m_defaultNamespaceURI = null;
                marshalElement((Element)obj);
                ictx.setIndent(indent);
                
            } catch (IOException e) {
                throw new JiBXException("Error writing to document", e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#isPresent(org.jibx.runtime.IUnmarshallingContext)
     */
    public boolean isPresent(IUnmarshallingContext ictx) throws JiBXException {
        if (m_name == null) {
            if (!(ictx instanceof UnmarshallingContext)) {
                throw new JiBXException
                    ("Unmarshalling context not of expected type");
            } else {
                UnmarshallingContext ctx = (UnmarshallingContext)ictx;
                if (ctx.isEnd()) {
                    return false;
                } else if (m_uri == null) {
                    return true;
                } else {
                    return !m_uri.equals(ctx.getElementNamespace());
                }
            }
        } else {
            return ictx.isAt(m_uri, m_name);
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#unmarshal(java.lang.Object,
     *  org.jibx.runtime.IUnmarshallingContext)
     */
    public Object unmarshal(Object obj, IUnmarshallingContext ictx)
        throws JiBXException {
        
        // verify the entry conditions
        if (!(ictx instanceof UnmarshallingContext)) {
            throw new JiBXException
                ("Unmarshalling context not of expected type");
        } else if (m_name != null && !ictx.isAt(m_uri, m_name)) {
            ((UnmarshallingContext)ictx).throwStartTagNameError(m_uri, m_name);
        }
        
        // position to element start tag
        m_unmarshalContext = (UnmarshallingContext)ictx;
        m_unmarshalContext.toStart();
        
        // unmarshal element to document model
        try {
			return unmarshalElement();
		} catch (IOException e) {
            throw new JiBXException("Error reading from document", e);
		}
    }
}
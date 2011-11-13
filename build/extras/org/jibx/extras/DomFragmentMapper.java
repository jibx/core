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

import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

/**
 * <p>Custom content list marshaller/unmarshaller to DOM representation. This
 * allows you to mix data binding and document model representations for XML
 * within the same application. You simply use this marshaller/unmarshaller with
 * a linked object type of <code>org.w3c.dom.DocumentFragment</code> (the actual
 * runtime type - the declared type is ignored and can be anything). When
 * unmarshalling it will create a fragment to hold any content up to the close
 * tag for the enclosing element in the list. When marshalling, it will simply
 * write out any content directly.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public class DomFragmentMapper extends DomMapperBase
    implements IMarshaller, IUnmarshaller
{
    /**
     * Default constructor.
     * 
     * @throws JiBXException on configuration error
     */
    public DomFragmentMapper() throws JiBXException {
        super();
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
        if (!(obj instanceof DocumentFragment)) {
            throw new JiBXException
                ("Mapped object not an org.w3c.dom.DocumentFragment");
        } else {
            try {
                    
                // marshal document fragment with no indentation
                m_xmlWriter = ictx.getXmlWriter();
                int indent = ictx.getIndent();
                ictx.setIndent(-1);
                m_defaultNamespaceURI = null;
                marshalContent(((DocumentFragment)obj).getChildNodes());
                ictx.setIndent(indent);
                
            } catch (IOException e) {
                throw new JiBXException("Error writing to document", e);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#isPresent(org.jibx.runtime.IUnmarshallingContext)
     */
    public boolean isPresent(IUnmarshallingContext ctx) throws JiBXException {
        if (!(ctx instanceof UnmarshallingContext)) {
            throw new JiBXException
                ("Unmarshalling context not of expected type");
        } else {
            return ((UnmarshallingContext)ctx).currentEvent() !=
                IXMLReader.END_TAG;
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#unmarshal(java.lang.Object,
     *  org.jibx.runtime.IUnmarshallingContext)
     */
    public Object unmarshal(Object obj, IUnmarshallingContext ictx)
        throws JiBXException {
        
        // verify the entry conditions
        boolean created = false;
        DocumentFragment frag = null;
        if (obj == null) {
            frag = m_document.createDocumentFragment();
            created = true;
        } else if (obj instanceof DocumentFragment) {
            frag = (DocumentFragment)obj;
        } else {
            throw new JiBXException
                ("Supplied object is not an org.w3c.dom.DocumentFragment");
        }
        if (!(ictx instanceof UnmarshallingContext)) {
            throw new JiBXException
                ("Unmarshalling context not of expected type");
        }
        
        // unmarshal content to document model
        m_unmarshalContext = (UnmarshallingContext)ictx;
        try {
            Node node;
            while ((node = unmarshalNode()) != null) {
                frag.appendChild(node);
            }
            if (created && !frag.hasChildNodes()) {
                return null;
            } else {
                return frag;
            }
		} catch (IOException e) {
            throw new JiBXException("Error reading from document", e);
		}
    }
}
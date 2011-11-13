/*
Copyright (c) 2005-2008, Dennis M. Sosnoski.
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

import org.jibx.runtime.IAliasable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * <p>Abstract base custom marshaller/unmarshaller for an object reference. This
 * marshals the reference as an empty element with a single IDREF attribute, and
 * unmarshals an element with the same structure to create a reference to the
 * object with that ID value. To use this class you need to create a subclass
 * with a constructor using the same signature as the one provided (calling the
 * base class constructor from your subclass constructor) and implement the
 * abstract {@link #getIdValue} method in your subclass. You can also override
 * the provided {@link #getAttributeName} method to change the name used for the
 * IDREF attribute. Note that this class can only be used when the definitions
 * precede the references in the XML document; if a referenced ID is not defined
 * the unmarshaller throws an exception.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class IdRefMapperBase
    implements IMarshaller, IUnmarshaller, IAliasable {
    
    private String m_uri;
    private int m_index;
    private String m_name;
    
    /**
     * Aliased constructor taking a name definition for the element. The
     * subclass version will be used by JiBX to define the element name to be
     * used with this custom marshaller/unmarshaller.
     *
     * @param uri namespace URI for the top-level element
     * @param index namespace index corresponding to the defined URI within the
     * marshalling context definitions
     * @param name local name for the top-level element
     */
    public IdRefMapperBase(String uri, int index, String name) {
        m_uri = uri;
        m_index = index;
        m_name = name;
    }
    
    /**
     * Get the ID value from object being marshalled.
     *
     * @return ID value
     */
    protected abstract String getIdValue(Object item);
    
    /**
     * Method which can be overridden to supply a different name for the ID
     * reference attribute. The attribute name used by default is just "ref".
     */
    protected String getAttributeName() {
        return "ref";
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
        if (obj == null) {
            return;
        } else if (!(ictx instanceof MarshallingContext)) {
            throw new JiBXException("Invalid context type for marshaller");
        } else {
            
            // generate the element start tag
            MarshallingContext ctx = (MarshallingContext)ictx;
            ctx.startTagAttributes(m_index, m_name);
            
            // add attribute reference to object ID
            ctx.attribute(0, getAttributeName(), getIdValue(obj));
            
            // close start tag for empty element
            ctx.closeStartEmpty();
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#isPresent(org.jibx.runtime.IUnmarshallingContext)
     */
    public boolean isPresent(IUnmarshallingContext ctx) throws JiBXException {
        return ctx.isAt(m_uri, m_name);
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#unmarshal(java.lang.Object,
     *  org.jibx.runtime.IUnmarshallingContext)
     */
    public Object unmarshal(Object obj, IUnmarshallingContext ictx)
        throws JiBXException {
        
        // make sure we're at the appropriate start tag
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        if (!ctx.isAt(m_uri, m_name)) {
            return null;
        }
        
        // get object reference for ID
        obj = ctx.attributeExistingIDREF(null, getAttributeName(), 0);
        
        // skip past the element
        ctx.parsePastEndTag(m_uri, m_name);
        return obj;
    }
}
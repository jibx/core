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

import java.util.HashMap;

import org.jibx.runtime.IAliasable;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * <p>Abstract base custom marshaller/unmarshaller for an object that may have
 * multiple references. The first time an object is seen when marshalling the
 * full XML representation is generated; successive uses of the same object then
 * use XML references to the object ID. To use this class you need to create a
 * subclass with a constructor using the same signature as the one provided
 * (calling the base class constructor from your subclass constructor) and
 * implement the abstract {@link #getIdValue(java.lang.Object)} method in your subclass. You can
 * also override the provided {@link #getAttributeName()} method to change the
 * name used for the IDREF attribute, which must not match the name of an
 * attribute used in the normal marshalled form of the object. The name used for
 * this marshaller/unmarshaller in the mapping definition must match the name
 * used for the base object type being handled.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class IdDefRefMapperBase
    implements IMarshaller, IUnmarshaller, IAliasable {
    
    private String m_uri;
    private int m_index;
    private String m_name;
    
    /**
     * Aliased constructor taking a name definition for reference elements. The
     * subclass version will be used by JiBX to define the element name to be
     * used with this custom marshaller/unmarshaller.
     *
     * @param uri namespace URI for the top-level element
     * @param index namespace index corresponding to the defined URI within the
     * marshalling context definitions
     * @param name local name for the reference element
     */
    public IdDefRefMapperBase(String uri, int index, String name) {
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
            
            // check if ID already defined
            MarshallingContext ctx = (MarshallingContext)ictx;
            HashMap map = ctx.getIdMap();
            String id = getIdValue(obj);
            Object value = map.get(id);
            if (value == null) {
                if (obj instanceof IMarshallable) {
                    
                    // new id, write full representation and add to map
                    map.put(id, obj);
                    ((IMarshallable)obj).marshal(ctx);
                    
                } else {
                    throw new JiBXException("Object of type " +
                        obj.getClass().getName() + " is not marshallable");
                }
            } else if (value.equals(obj)) {
                
                // generate reference to previously-defined item
                ctx.startTagAttributes(m_index, m_name);
                ctx.attribute(0, getAttributeName(), id);
                ctx.closeStartEmpty();
                
            } else {
                throw new JiBXException("Duplicate definition for ID " + id);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#isPresent(org.jibx.runtime.IUnmarshallingContext)
     */
    public boolean isPresent(IUnmarshallingContext ictx) throws JiBXException {
        return ictx.isAt(m_uri, m_name);
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
        } else {
            
            // check for reference to existing ID
            String id = ctx.attributeText(null, getAttributeName(), null);
            if (id == null) {
                
                // no ID value supplied, unmarshal full definition
                obj = ctx.unmarshalElement();
                
            } else {
                
                // find object based on ID
                obj = ctx.findID(id, 0);
                ctx.parsePastEndTag(m_uri, m_name);
                if (obj == null) {
                    ctx.throwStartTagException("Reference to undefined ID " +
                        id);
                }
            }
        }
        return obj;
    }
}
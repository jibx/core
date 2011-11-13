/*
Copyright (c) 2003-2008, Dennis M. Sosnoski.
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

import java.util.ArrayList;

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
 * <p>Custom marshaller/unmarshaller for <code>Object[]</code> instances. This
 * handles mapping arrays typed as <code>java.lang.Object[]</code>, where each
 * item in the array must be of a mapped type. If a name is specified by the
 * mapping definition that name is used as a wrapper around the elements
 * representing the items in the array; otherwise, the elements are just handled
 * inline.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public class ObjectArrayMapper
    implements IMarshaller, IUnmarshaller, IAliasable {
    
    private static final Object[] DUMMY_ARRAY = {};
    
    private String m_uri;
    private int m_index;
    private String m_name;
    private ArrayList m_holder;
    
    /**
     * Default constructor. This just sets up for an XML representation with no
     * element wrapping the actual item structures. It'll be used by JiBX when
     * no name information is supplied by the mapping which references this
     * custom marshaller/unmarshaller.
     */
    public ObjectArrayMapper() {
        m_uri = null;
        m_index = 0;
        m_name = null;
    }
    
    /**
     * Aliased constructor. This takes a name definition for the top-level
     * wrapper element. It'll be used by JiBX when a name is supplied by the
     * mapping which references this custom marshaller/unmarshaller.
     *
     * @param uri namespace URI for the top-level element
     * @param index namespace index corresponding to the defined URI within the
     * marshalling context definitions
     * @param name local name for the top-level element
     */
    public ObjectArrayMapper(String uri, int index, String name) {
        m_uri = uri;
        m_index = index;
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
        if (obj == null) {
            if (m_name == null) {
                throw new JiBXException
                    ("null array not allowed without wrapper");
            }
        } else if (!(DUMMY_ARRAY.getClass().isInstance(obj))) {
            throw new JiBXException("Invalid object type for marshaller");
        } else if (!(ictx instanceof MarshallingContext)) {
            throw new JiBXException("Invalid object type for marshaller");
        } else {
            
            // start by generating start tag for container
            MarshallingContext ctx = (MarshallingContext)ictx;
            Object[] array = (Object[])obj;
            if (m_name != null) {
                ctx.startTag(m_index, m_name);
            }
        
            // loop through all entries in array
            for (int i = 0; i < array.length; i++) {
                Object item = array[i];
                if (item instanceof IMarshallable) {
                    ((IMarshallable)item).marshal(ctx);
                } else if (item == null) {
                    throw new JiBXException("Null value at offset " + i +
                        " not supported");
                } else {
                    throw new JiBXException("Array value of type " +
                        item.getClass().getName() + " at offset " + i +
                        " is not marshallable");
                }
            }
        
            // finish with end tag for container element
            if (m_name != null) {
                ctx.endTag(m_index, m_name);
            }
                
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
        if (m_name != null) {
            if (ctx.isAt(m_uri, m_name)) {
                ctx.parsePastStartTag(m_uri, m_name);
            } else {
                return null;
            }
        }
        
        // create new array if needed
        if (m_holder == null) {
            m_holder = new ArrayList();
        }
        
        // process all items present in document
        while (!ctx.isEnd()) {
            Object item = ctx.unmarshalElement();
            m_holder.add(item);
        }
        
        // discard close tag if used
        if (m_name != null) {
            ctx.parsePastEndTag(m_uri, m_name);
        }
        
        // return array containing all items
        Object[] result = m_holder.toArray();
        m_holder.clear();
        return result;
    }
}
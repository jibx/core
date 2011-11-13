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

import java.lang.reflect.Array;
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
 * <p>Custom marshaller/unmarshaller for reference arrays of a particular type.
 * This handles mapping arrays typed as <code>object-type[]</code>, where the
 * <i>object-type</i> is any class name (not a primitive type). All items in the
 * array must be of a mapped type. If a name is specified by the mapping
 * definition that name is used as a wrapper around the elements representing
 * the items in the array; otherwise, the elements are just handled inline.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public class TypedArrayMapper
    implements IMarshaller, IUnmarshaller, IAliasable {
    
    private static final Object[] DUMMY_ARRAY = {};
    
    private String m_uri;
    private int m_index;
    private String m_name;
    private Object[] m_baseArray;
    private ArrayList m_holder;
    
    /**
     * Aliased constructor. This takes a name definition for the top-level
     * wrapper element. It'll be used by JiBX when a name is supplied by the
     * mapping which references this custom marshaller/unmarshaller.
     *
     * @param uri namespace URI for the top-level element
     * @param index namespace index corresponding to the defined URI within the
     * marshalling context definitions
     * @param name local name for the top-level element
     * @param type class name for type of items in array
     */
    public TypedArrayMapper(String uri, int index, String name, String type) {
        
        // save the simple values
        m_uri = uri;
        m_index = index;
        m_name = name;
        
        // strip trailing array bracket sets from type (at least one)
        int dimen = 0;
        while (type.endsWith("[]")) {
            type = type.substring(0, type.length()-2);
            dimen++;
        }
        
        // now get the class used for array items
        try {
            
            // first try loading item class from context classloader
            Class clas = null;
            ClassLoader loader =
                Thread.currentThread().getContextClassLoader();
            if (loader != null) {
                try {
                    clas = loader.loadClass(type);
                } catch (ClassNotFoundException e) { /* fall through */ }
            }
            if (clas == null) {
                
                // if not found, try the loader that loaded this class
                clas = UnmarshallingContext.class.getClassLoader().
                    loadClass(type);
            }
            
            // create a dummy base array of specified type
            int[] dimens = new int[dimen];
            m_baseArray = (Object[])Array.newInstance(clas, dimens);
            
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException
                ("Error loading array item class " + type + ": " +
                    e.getMessage());
        }
    }
    
    /**
     * Class only constructor. This just sets up for an XML representation with
     * no element wrapping the actual item structures. It'll be used by JiBX
     * when no name information is supplied by the mapping which references this
     * custom marshaller/unmarshaller.
     * 
     * @param type class name for type of items in array
     */
    public TypedArrayMapper(String type) {
        this(null, 0, null, type);
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
        } else if (!(ictx instanceof MarshallingContext)) {
            throw new JiBXException("Marshalling context not of expected type");
        } else {
            
            // verify object as a handled array type
            Class clas = obj.getClass();
            if (!clas.isArray()) {
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
                    if (item == null) {
                        throw new JiBXException("Null value at offset " + i +
                            " not supported");
                    } else if (item instanceof IMarshallable) {
                        ((IMarshallable)item).marshal(ctx);
                    } else {
                        throw new JiBXException("Array item of type " +
                            item.getClass().getName() + " does not implement " +
                            "org.jibx.runtime.IMarshallable");
                    }
                }
        
                // finish with end tag for container element
                if (m_name != null) {
                    ctx.endTag(m_index, m_name);
                }
                
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
        Object[] result = m_holder.toArray(m_baseArray);
        m_holder.clear();
        return result;
    }
}
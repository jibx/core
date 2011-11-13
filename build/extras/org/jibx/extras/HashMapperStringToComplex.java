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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 * <p>Custom marshaller/unmarshaller for <code>java.util.Map</code>
 * instances. This handles mapping hash maps with simple keys and complex values
 * to and from XML. There are a number of limitations, though. First off, the
 * key objects are marshalled as simple text values, using the
 * <code>toString()</code> method to convert them to <code>String</code>. When
 * unmarshalling the keys are always treated as <code>String</code> values. The
 * corresponding values can be any complex type with a &lt;mapping> defined in
 * the binding. The name of the top-level element in the XML structure can be
 * configured in the binding definition, but the rest of the names are
 * predefined and set in the code (though the namespace configured for the
 * top-level element will be used with all the names).</p>
 * 
 * <p>The net effect is that the XML structure will always be of the form:</p>
 *
 * <pre>&lt;map-name size="3">
 *   &lt;entry key="38193">
 *     &lt;customer state="WA" zip="98059">
 *       &lt;name first-name="John" last-name="Smith"/>
 *       &lt;street>12345 Happy Lane&lt;/street>
 *       &lt;city>Plunk&lt;/city>
 *     &lt;/customer>
 *   &lt;/entry>
 *   &lt;entry key="39122">
 *     &lt;customer state="WA" zip="98094">
 *       &lt;name first-name="Sally" last-name="Port"/>
 *       &lt;street>932 Easy Street&lt;/street>
 *       &lt;city>Fort Lewis&lt;/city>
 *     &lt;/customer>
 *   &lt;/entry>
 *   &lt;entry key="83132">
 *     &lt;customer state="WA" zip="98059">
 *       &lt;name first-name="Mary" last-name="Smith"/>
 *       &lt;street>12345 Happy Lane&lt;/street>
 *       &lt;city>Plunk&lt;/city>
 *     &lt;/customer>
 *   &lt;/entry>
 * &lt;/map-name></pre>
 *
 * <p>where "map-name" is the configured top-level element name, the "size"
 * attribute is the number of pairs in the hash map, and the "entry" elements
 * are the actual entries in the hash map.</p>
 *
 * <p>This is obviously not intended to handle all types of hash maps, but it
 * should be useful as written for many applications and easily customized to
 * handle other requirements.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public class HashMapperStringToComplex
    implements IMarshaller, IUnmarshaller, IAliasable {
        
    private static final int DEFAULT_SIZE = 10;
    
    private String m_uri;
    private int m_index;
    private String m_name;
    
    /**
     * Default constructor. This uses a pre-defined name for the top-level
     * element. It'll be used by JiBX when no name information is supplied by
     * the mapping which references this custom marshaller/unmarshaller.
     */
    public HashMapperStringToComplex() {
        m_uri = null;
        m_index = 0;
        m_name = "hashmap";
    }
    
    /**
     * Aliased constructor. This takes a name definition for the top-level
     * element. It'll be used by JiBX when a name is supplied by the mapping
     * which references this custom marshaller/unmarshaller.
     *
     * @param uri namespace URI for the top-level element (also used for all
     * other names within the binding)
     * @param index namespace index corresponding to the defined URI within the
     * marshalling context definitions
     * @param name local name for the top-level element
     */
    public HashMapperStringToComplex(String uri, int index, String name) {
        m_uri = uri;
        m_index = index;
        m_name = name;
    }
    
    /**
     * Method which can be overridden to supply a different name for the wrapper
     * element attribute used to give the number of items present. If present,
     * this attribute is used when unmarshalling to set the initial size of the
     * hashmap. It will be generated when marshalling if the supplied name is
     * non-<code>null</code>.
     */
    protected String getSizeAttributeName() {
        return "size";
    }
    
    /**
     * Method which can be overridden to supply a different name for the element
     * used to represent each item in the map.
     */
    protected String getEntryElementName() {
        return "entry";
    }
    
    /**
     * Method which can be overridden to supply a different name for the
     * attribute defining the key value for each item in the map.
     */
    protected String getKeyAttributeName() {
        return "key";
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
        if (!(obj instanceof Map)) {
            throw new JiBXException("Invalid object type for marshaller");
        } else if (!(ictx instanceof MarshallingContext)) {
            throw new JiBXException("Invalid object type for marshaller");
        } else {
            
            // start by generating start tag for container
            MarshallingContext ctx = (MarshallingContext)ictx;
            Map map = (Map)obj;
            ctx.startTagAttributes(m_index, m_name).
                attribute(m_index, getSizeAttributeName(), map.size()).
                closeStartContent();
            
            // loop through all entries in map
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                ctx.startTagAttributes(m_index, getEntryElementName());
                ctx.attribute(m_index, getKeyAttributeName(),
                    entry.getKey().toString());
                ctx.closeStartContent();
                if (entry.getValue() instanceof IMarshallable) {
                    ((IMarshallable)entry.getValue()).marshal(ctx);
                    ctx.endTag(m_index, getEntryElementName());
                } else {
                    throw new JiBXException("Mapped value is not marshallable");
                }
            }
            
            // finish with end tag for container element
            ctx.endTag(m_index, m_name);
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
            ctx.throwStartTagNameError(m_uri, m_name);
        }
        
        // create new hashmap if needed
        int size = ctx.attributeInt(m_uri,
            getSizeAttributeName(), DEFAULT_SIZE);
        Map map = (Map)obj;
        if (map == null) {
            map = new HashMap(size);
        }
        
        // process all entries present in document
        ctx.parsePastStartTag(m_uri, m_name);
        while (ctx.isAt(m_uri, getEntryElementName())) {
            Object key = ctx.attributeText(m_uri, getKeyAttributeName(), null);
            ctx.parsePastStartTag(m_uri, getEntryElementName());
            Object value = ctx.unmarshalElement();
            map.put(key, value);
            ctx.parsePastEndTag(m_uri, getEntryElementName());
        }
        ctx.parsePastEndTag(m_uri, m_name);
        return map;
    }
}
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IAliasable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.Utility;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * <p>Custom marshaller/unmarshaller for <code>java.util.Map</code>
 * instances. This handles mapping hash maps with string keys and values that
 * match basic schema datatypes to and from XML. The key objects are marshalled
 * as simple text values, using the <code>toString()</code> method to convert
 * them to <code>String</code> if they are not already of that type. When
 * unmarshalling the keys are always treated as <code>String</code> values. The
 * corresponding values can be any of the object types corresponding to basic
 * schema data types, and are marshalled with xsi:type attributes to specify the
 * type of each value. The types currently supported are <code>Byte</code>,
 * <code>Double</code>, <code>Float</code>, <code>Integer</code>,
 * <code>Long</code>, <code>java.util.Date</code> (as xsd:dateTime),
 * <code>java.sql.Date</code> (as xsd:date), <code>java.sql.Time</code> (as
 * xsd:time), <code>java.math.BigDecimal</code> (with no exponent allowed, as
 * xsd:decimal), and <code>java.math.BigInteger</code> (as xsd:integer). The
 * xsd:type attribute is checked when unmarshalling values to select the proper
 * deserialization and value type. The name of the top-level element in the XML
 * structure can be configured in the binding definition, but the rest of the
 * names are predefined and set in the code (though the namespace configured for
 * the top-level element will be used with all the names).</p>
 * 
 * <p>The net effect is that the XML structure will always be of the form:</p>
 *
 * <pre>&lt;map-name size="6" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
 *   &lt;entry key="name" xsi:type="xsd:string">John Smith&lt;/entry>
 *   &lt;entry key="street" xsi:type="xsd:string">12345 Happy Lane&lt;/entry>
 *   &lt;entry key="city" xsi:type="xsd:string">Plunk&lt;/entry>
 *   &lt;entry key="state" xsi:type="xsd:string">WA&lt;/entry>
 *   &lt;entry key="rating" xsi:type="xsd:int">6&lt;/entry>
 *   &lt;entry key="joined" xsi:type="xsd:dateTime">2002-08-06T00:13:31Z&lt;/entry>
 * &lt;/map-name></pre>
 *
 * <p>where "map-name" is the configured top-level element name, the "size"
 * attribute is the number of pairs in the hash map, and the "entry" elements
 * are the actual entries in the hash map.</p>
 *
 * <p>For unmarshalling this requires an active namespace declaration with a
 * prefix for the schema namespace. All xsi:type attribute values must use this
 * prefix. If more than one prefix is declared for the schema namespace, the
 * innermost one declared must be used.</p>
 * 
 * @author Dennis M. Sosnoski
 */
public class HashMapperStringToSchemaType
    implements IMarshaller, IUnmarshaller, IAliasable {
    
    //
    // Basic constants used in code
    
    private static final String SIZE_ATTRIBUTE_NAME = "size";
    private static final String ENTRY_ELEMENT_NAME = "entry";
    private static final String KEY_ATTRIBUTE_NAME = "key";
    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String XSI_NAMESPACE_URI =
        "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSD_NAMESPACE_URI =
        "http://www.w3.org/2001/XMLSchema";
    private static final String[] SCHEMA_NAMESPACE_URIS =
    {
        XSI_NAMESPACE_URI, XSD_NAMESPACE_URI
    };
    private static final String XSI_NAMESPACE_PREFIX = "xsi";
    private static final String XSD_NAMESPACE_PREFIX = "xsd";
    private static final String[] SCHEMA_NAMESPACE_PREFIXES =
    {
        XSI_NAMESPACE_PREFIX, XSD_NAMESPACE_PREFIX
    };
    private static final String XSD_PREFIX_LEAD = "xsd:";
    private static final int DEFAULT_SIZE = 10;
    
    //
    // Supported XML schema type correspondences enumeration
    
    // numeric values for types
    public static final int BOOLEAN_TYPE = 0;
    public static final int BYTE_TYPE = 1;
    public static final int DOUBLE_TYPE = 2;
    public static final int FLOAT_TYPE = 3;
    public static final int INT_TYPE = 4;
    public static final int LONG_TYPE = 5;
    public static final int SHORT_TYPE = 6;
    public static final int DATETIME_TYPE = 7;
    public static final int DECIMAL_TYPE = 8;
    public static final int INTEGER_TYPE = 9;
    public static final int BYTERRAY_TYPE = 10;
    public static final int STRING_TYPE = 11;
//#!j2me{
    public static final int DATE_TYPE = 12;
    public static final int TIME_TYPE = 13;
//#j2me}
    
    // enumeration definition (string order must match numeric list, above)
    private static final EnumSet s_javaTypesEnum = new EnumSet(BOOLEAN_TYPE,
        new String[] { "java.lang.Boolean", "java.lang.Byte",
        "java.lang.Double", "java.lang.Float", "java.lang.Integer",
        "java.lang.Long", "java.lang.Short", "java.util.Date",
        "java.math.BigDecimal", "java.math.BigInteger", "byte[]",
        "java.lang.String",
//#!j2me{
        "java.sql.Date", "java.sql.Time",
//#j2me}
        } );
    
    // corresponding schema types (string order must match numeric list, above)
    private static final EnumSet s_schemaTypesEnum = new EnumSet(BOOLEAN_TYPE,
        new String[] { "boolean", "byte", "double", "float", "int", "long",
        "short", "dateTime", "decimal", "integer", "base64Binary", "string",
//#!j2me{
        "date", "time",
//#j2me}
        } );
    
    //
    // Member fields
    
    private String m_uri;
    private int m_index;
    private String m_name;
    
    /**
     * Default constructor. This uses a pre-defined name for the top-level
     * element. It'll be used by JiBX when no name information is supplied by
     * the mapping which references this custom marshaller/unmarshaller.
     */
    public HashMapperStringToSchemaType() {
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
    public HashMapperStringToSchemaType(String uri, int index, String name) {
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
        if (!(obj instanceof Map)) {
            throw new JiBXException("Invalid object type for marshaller");
        } else if (!(ictx instanceof MarshallingContext)) {
            throw new JiBXException("Invalid object type for marshaller");
        } else {
            
            // start by setting up added namespaces
            MarshallingContext ctx = (MarshallingContext)ictx;
            IXMLWriter xwrite = ctx.getXmlWriter();
            int ixsi = xwrite.getNamespaces().length;
            String[][] extens = xwrite.getExtensionNamespaces();
            if (extens != null) {
                for (int i = 0; i < extens.length; i++) {
                    ixsi += extens[i].length;
                }
            }
            xwrite.pushExtensionNamespaces(SCHEMA_NAMESPACE_URIS);
            
            // generate start tag for containing element
            Map map = (Map)obj;
            ctx.startTagNamespaces(m_index, m_name, new int[] { ixsi, ixsi+1 },
                SCHEMA_NAMESPACE_PREFIXES).
                attribute(m_index, SIZE_ATTRIBUTE_NAME, map.size()).
                closeStartContent();
            
            // loop through all entries in hashmap
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                
                // first make sure we have a value
                Map.Entry entry = (Map.Entry)iter.next();
                Object value = entry.getValue();
                if (value != null) {
                    
                    // write element with key attribute
                    ctx.startTagAttributes(m_index, ENTRY_ELEMENT_NAME);
                    ctx.attribute(m_index, KEY_ATTRIBUTE_NAME,
                        entry.getKey().toString());
                    
                    // translate value object class to schema type
                    String tname = value.getClass().getName();
                    int type = s_javaTypesEnum.getValue(tname);
                    if (type < 0) {
                        throw new JiBXException("Value of type " + tname +
                            " with key " + entry.getKey() +
                            " is not a supported type");
                    }
                    
                    // generate xsi:type attribute for value
                    ctx.attribute(ixsi, TYPE_ATTRIBUTE_NAME,
                        XSD_PREFIX_LEAD + s_schemaTypesEnum.getName(type));
                    ctx.closeStartContent();
                    
                    // handle the actual value conversion based on type
                    switch (type) {
                        
                        case BOOLEAN_TYPE:
                            ctx.content(Utility.serializeBoolean
                                (((Boolean)value).booleanValue()));
                            break;
                            
                        case BYTE_TYPE:
                            ctx.content(Utility.
                                serializeByte(((Byte)value).byteValue()));
                            break;
                            
                        case DOUBLE_TYPE:
                            ctx.content(Utility.
                                serializeDouble(((Double)value).doubleValue()));
                            break;
                            
                        case FLOAT_TYPE:
                            ctx.content(Utility.
                                serializeFloat(((Float)value).floatValue()));
                            break;
                            
                        case INT_TYPE:
                            ctx.content(((Integer)value).intValue());
                            break;
                            
                        case LONG_TYPE:
                            ctx.content(Utility.
                                serializeLong(((Long)value).longValue()));
                            break;
                            
                        case SHORT_TYPE:
                            ctx.content(Utility.
                                serializeShort(((Short)value).shortValue()));
                            break;
                            
                        case DATETIME_TYPE:
                            ctx.content(Utility.serializeDateTime((Date)value));
                            break;
                            
//#!j2me{
                        case DATE_TYPE:
                            ctx.content(Utility.
                                serializeSqlDate((java.sql.Date)value));
                            break;
                            
                        case TIME_TYPE:
                            ctx.content(Utility.
                                serializeSqlTime((java.sql.Time)value));
                            break;
//#j2me}
                            
                        case BYTERRAY_TYPE:
                            ctx.content(Utility.serializeBase64((byte[])value));
                            break;
                            
                        case DECIMAL_TYPE:
                        case INTEGER_TYPE:
                        case STRING_TYPE:
                            ctx.content(value.toString());
                            break;
                    }
                    
                    // finish with close tag for entry element
                    ctx.endTag(m_index, ENTRY_ELEMENT_NAME);
                }
            }
            
            // finish with end tag for container element
            ctx.endTag(m_index, m_name);
            xwrite.popExtensionNamespaces();
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
        
        // lookup the prefixes assigned to required namespaces
        int nscnt = ctx.getActiveNamespaceCount();
        String xsdlead = null;
        for (int i = nscnt-1; i >= 0; i--) {
            String uri = ctx.getActiveNamespaceUri(i);
            if (XSD_NAMESPACE_URI.equals(uri)) {
                String prefix = ctx.getActiveNamespacePrefix(i);
                if (!"".equals(prefix)) {
                    xsdlead = prefix + ':';
                    break;
                }
            }
        }
        if (xsdlead == null) {
            throw new JiBXException
                ("Missing required schema namespace declaration");
        }
        
        // create new hashmap if needed
        int size = ctx.attributeInt(m_uri, SIZE_ATTRIBUTE_NAME, DEFAULT_SIZE);
        Map map = (Map)obj;
        if (map == null) {
            map = new HashMap(size);
        }
        
        // process all entries present in document
        ctx.parsePastStartTag(m_uri, m_name);
        String tdflt = xsdlead + "string";
        while (ctx.isAt(m_uri, ENTRY_ELEMENT_NAME)) {
            
            // unmarshal key and type from start tag attributes
            Object key = ctx.attributeText(m_uri, KEY_ATTRIBUTE_NAME);
            String tname = ctx.attributeText(XSI_NAMESPACE_URI,
                TYPE_ATTRIBUTE_NAME, tdflt);
            
            // convert type name to type index number
            int type = -1;
            if (tname.startsWith(xsdlead)) {
                type = s_schemaTypesEnum.
                    getValue(tname.substring(xsdlead.length()));
            }
            if (type < 0) {
                throw new JiBXException("Value of type " + tname +
                    " with key " + key + " is not a supported type");
            }
            
            // deserialize content as specified type
            String text = ctx.parseElementText(m_uri, ENTRY_ELEMENT_NAME);
            Object value = null;
            switch (type) {
                
                case BOOLEAN_TYPE:
                    value = Utility.parseBoolean(text) ?
                        Boolean.TRUE : Boolean.FALSE;
                    break;
                    
                case BYTE_TYPE:
                    value = new Byte(Utility.parseByte(text));
                    break;
                    
                case DOUBLE_TYPE:
                    value = new Double(Utility.parseDouble(text));
                    break;
                    
                case FLOAT_TYPE:
                    value = new Float(Utility.parseFloat(text));
                    break;
                    
                case INT_TYPE:
                    value = new Integer(Utility.parseInt(text));
                    break;
                    
                case LONG_TYPE:
                    value = new Long(Utility.parseLong(text));
                    break;
                    
                case SHORT_TYPE:
                    value = new Short(Utility.parseShort(text));
                    break;
                    
                case DATETIME_TYPE:
                    value = Utility.deserializeDateTime(text);
                    break;
                    
//#!j2me{
                case DATE_TYPE:
                    value = Utility.deserializeSqlDate(text);
                    break;
                    
                case TIME_TYPE:
                    value = Utility.deserializeSqlTime(text);
                    break;
//#j2me}
                    
                case BYTERRAY_TYPE:
                    value = Utility.deserializeBase64(text);
                    break;
                    
                case DECIMAL_TYPE:
                    value = new BigDecimal(text);
                    break;
                    
                case INTEGER_TYPE:
                    value = new BigInteger(text);
                    break;
                    
                case STRING_TYPE:
                    value = text;
                    break;
            }
            
            // add key-value pair to map
            map.put(key, value);
        }
        
        // finish by skipping past wrapper end tag
        ctx.parsePastEndTag(m_uri, m_name);
        return map;
    }
}
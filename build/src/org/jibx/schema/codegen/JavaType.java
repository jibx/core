/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.schema.codegen;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Java types corresponding to schema types. The schema type list here should always match that in
 * {@link org.jibx.schema.support.SchemaTypes}. As a special case, an instance of this class is also used to represent
 * the special &lt;any> schema component.
 * 
 * @author Dennis M. Sosnoski
 */
public class JavaType
{
    /** Predefined schema simple type correspondences (note not all are defined yet). */
    private static final Map s_schemaTypesMap;
    static {
        // TODO define correspondences and add handling for current nulls
        Map map = new HashMap();
        addType("anySimpleType", "java.lang.String", map);
        addType("anyURI", "java.lang.String", map);
        addType("base64Binary", "byte[]", map);
        addType("boolean", "boolean", "java.lang.Boolean", "org.jibx.runtime.Utility.ifBoolean", map);
        addType("byte", "byte", "java.lang.Byte", "org.jibx.runtime.Utility.ifByte", map);
        addType("date", null, "java.sql.Date", "org.jibx.runtime.Utility.ifDate", map);
        addType("dateTime", null, "java.util.Date", "org.jibx.runtime.Utility.ifDateTime", map);
        addType("decimal", "java.math.BigDecimal", map);
        addType("double", "double", "java.lang.Double", map);
        addType("duration", "java.lang.String", map);
        addType("ENTITY", "java.lang.String", map);
        addType("ENTITIES", "java.lang.String", map);
        addType("float", "float", "java.lang.Float", map);
        addType("gDay", "java.lang.String", map);
        addType("gMonth", "java.lang.String", map);
        addType("gMonthDay", "java.lang.String", map);
        addType("gYear", "java.lang.String", map);
        addType("gYearMonth", "java.lang.String", map);
        addType("hexBinary", "byte[]", map);
        addType("ID", "java.lang.String", map);
        addType("IDREF", "java.lang.String", map);
        addType("IDREFS", "java.lang.String", map);
        addType("int", "int", "java.lang.Integer", "org.jibx.runtime.Utility.ifInt", map);
        addType("integer", null, "java.math.BigInteger", "org.jibx.runtime.Utility.ifInteger", map);
        addType("language", "java.lang.String", map);
        addType("long", "long", "java.lang.Long", "org.jibx.runtime.Utility.ifLong", map);
        addType("Name", "java.lang.String", map);
        addType("negativeInteger", "java.lang.String", map);
        addType("nonNegativeInteger", "java.lang.String", map);
        addType("nonPositiveInteger", "java.lang.String", map);
        addType("normalizedString", "java.lang.String", map);
        addType("NCName", "java.lang.String", map);
        addType("NMTOKEN", "java.lang.String", map);
        addType("NMTOKENS", "java.lang.String", map);
        addType("NOTATION", "java.lang.String", map);
        addType("positiveInteger", "java.lang.String", map);
        addType("QName", "org.jibx.runtime.QName", map);
        addType("short", "short", "java.lang.Short", "org.jibx.runtime.Utility.ifShort", map);
        addType("string", "java.lang.String", map);
        addType("time", null, "java.sql.Time", "org.jibx.runtime.Utility.ifTime", map);
        addType("token", "java.lang.String", map);
        addType("unsignedByte", "java.lang.String", map);
        addType("unsignedInt", "java.lang.String", map);
        addType("unsignedLong", "java.lang.String", map);
        addType("unsignedShort", "java.lang.String", map);
        s_schemaTypesMap = Collections.unmodifiableMap(map);    }
    
    /** &lt;any> schema component type. */
    public static final JavaType s_anyType = new JavaType(null, null, "org.w3c.dom.Element");
    
    /** &lt;anyAttribute> schema component type. */
    public static final JavaType s_anyAttributeType = new JavaType(null, null, "org.w3c.dom.Attribute");
    
    /** Schema type local name (may be needed for special handling in binding - ID and IDREF, in particular). */
    private final String m_schemaName;
    
    /** Fully qualified primitive type name (<code>null</code> if none). */
    private final String m_primitiveName;
    
    /** Fully qualified object type name (non-<code>null</code>). */
    private final String m_fqName;
    
    /** JiBX format name (for types requiring special handling, <code>null</code> otherwise). */
    private final String m_format;
    
    /** Method to convert instance of type to a text string. */
    private final String m_serializerMethod;
    
    /** Method to convert text string to instance of type. */
    private final String m_deserializerMethod;
    
    /** Method to check if a text string matches the format for this type (<code>null</code> if unused). */
    private final String m_checkMethod;
    
    /**
     * Constructor supporting special handling. This uses a string value for any types without specific Java equivalents
     * defined.
     * 
     * @param slname schema type local name
     * @param pname primitive type name (<code>null</code> if none)
     * @param fqname object type fully-qualified name (non-<code>null</code>)
     * @param format JiBX format name (<code>null</code> if none)
     * @param ser fully-qualified serializer class and method name (<code>null</code> if none)
     * @param dser fully-qualified deserializer class and method name (<code>null</code> if none)
     * @param check fully-qualified check class and method name (<code>null</code> if none)
     */
    public JavaType(String slname, String pname, String fqname, String format, String ser, String dser, String check) {
        if (fqname == null) {
            throw new IllegalArgumentException("Internal error - object type required for definition");
        }
        m_schemaName = slname;
        m_primitiveName = pname;
        m_fqName = fqname;
        m_format = format;
        m_serializerMethod = ser;
        m_deserializerMethod = dser;
        m_checkMethod = check;
    }
    
    /**
     * Basic constructor.
     * 
     * @param slname schema type local name
     * @param pname primitive type name (<code>null</code> if none)
     * @param fqname object type fully-qualified name
     */
    private JavaType(String slname, String pname, String fqname) {
        this(slname, pname, fqname, null, null, null, null);
    }
    
    /**
     * Helper method for adding object-only types to map.
     * 
     * @param lname schema type local name
     * @param fqname fully-qualified java object type name
     */
    private static void addType(String lname, String fqname, Map map) {
        addType(lname, null, fqname, null, map);
    }
    
    /**
     * Helper method for adding types without check methods to map.
     * 
     * @param lname schema type local name
     * @param pname primitive type name (<code>null</code> if object type)
     * @param fqname fully-qualified java object type name
     */
    private static void addType(String lname, String pname, String fqname, Map map) {
        addType(lname, null, fqname, null, map);
    }
    
    /**
     * Helper method for creating instances and adding them to map.
     * 
     * @param lname schema type local name
     * @param pname primitive type name (<code>null</code> if object type)
     * @param fqname fully-qualified java object type name (<code>null</code> if primitive type)
     * @param check check method name (<code>null</code> if none)
     */
    private static void addType(String lname, String pname, String fqname, String check, Map map) {
        map.put(lname, new JavaType(lname, pname, fqname, null, null, null, check));
    }
    
    //
    // Static access method
    
    /**
     * Get map from schema type local name to Java type.
     * 
     * @return map
     */
    public static Map getTypeMap() {
        return s_schemaTypesMap;
    }
    
    //
    // Access methods
    
    /**
     * Get schema type local name. This is only required because the binding generation needs to implement special
     * handling for ID and IDREF values.
     * 
     * @return schema type local name
     */
    public String getSchemaName() {
        return m_schemaName;
    }
    
    /**
     * Get fully-qualified object type name.
     * 
     * @return fully-qualified name
     */
    public String getClassName() {
        return m_fqName;
    }
    
    /**
     * Get primitive type name.
     * 
     * @return primitive type, <code>null</code> if none
     */
    public String getPrimitiveName() {
        return m_primitiveName;
    }
    
    /**
     * Get format.
     * 
     * @return format
     */
    public String getFormat() {
        return m_format;
    }
    
    /**
     * Get serializer method name.
     * 
     * @return name (<code>null</code> if none)
     */
    public String getSerializerMethod() {
        return m_serializerMethod;
    }
    
    /**
     * Get deserializer method name.
     * 
     * @return name (<code>null</code> if none)
     */
    public String getDeserializerMethod() {
        return m_deserializerMethod;
    }
    
    /**
     * Get check method name.
     * 
     * @return name (<code>null</code> if none)
     */
    public String getCheckMethod() {
        return m_checkMethod;
    }
}
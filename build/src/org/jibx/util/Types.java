/*
Copyright (c) 2007-2009, Dennis M. Sosnoski
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

package org.jibx.util;

import java.util.HashMap;

import org.jibx.runtime.QName;

/**
 * Mapping information to and from schema types.
 */
public class Types
{
    public static final String SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    public static final QName STRING_QNAME = new QName(SCHEMA_NAMESPACE, "string");
    
    /** Set of object types mapped to schema types. */
    private static HashMap s_objectTypeMap = new HashMap();
    
    static {
        s_objectTypeMap.put("java.lang.Boolean", new QName(SCHEMA_NAMESPACE, "boolean"));
        s_objectTypeMap.put("java.lang.Byte", new QName(SCHEMA_NAMESPACE, "byte"));
        s_objectTypeMap.put("java.lang.Character", new QName(SCHEMA_NAMESPACE, "unsignedInt"));
        s_objectTypeMap.put("java.lang.Double", new QName(SCHEMA_NAMESPACE, "double"));
        s_objectTypeMap.put("java.lang.Float", new QName(SCHEMA_NAMESPACE, "float"));
        s_objectTypeMap.put("java.lang.Integer", new QName(SCHEMA_NAMESPACE, "int"));
        s_objectTypeMap.put("java.lang.Long", new QName(SCHEMA_NAMESPACE, "long"));
        s_objectTypeMap.put("java.lang.Short", new QName(SCHEMA_NAMESPACE, "short"));
        s_objectTypeMap.put("java.lang.String", STRING_QNAME);
        s_objectTypeMap.put("java.math.BigDecimal", new QName(SCHEMA_NAMESPACE, "decimal"));
        s_objectTypeMap.put("java.math.BigInteger", new QName(SCHEMA_NAMESPACE, "integer"));
        s_objectTypeMap.put("java.util.Date", new QName(SCHEMA_NAMESPACE, "dateTime"));
//#!j2me{
        s_objectTypeMap.put("java.sql.Date", new QName(SCHEMA_NAMESPACE, "date"));
        s_objectTypeMap.put("java.sql.Time", new QName(SCHEMA_NAMESPACE, "time"));
        s_objectTypeMap.put("java.sql.Timestamp", new QName(SCHEMA_NAMESPACE, "dateTime"));
        s_objectTypeMap.put("org.joda.time.LocalDate", new QName(SCHEMA_NAMESPACE, "date"));
        s_objectTypeMap.put("org.joda.time.DateMidnight", new QName(SCHEMA_NAMESPACE, "date"));
        s_objectTypeMap.put("org.joda.time.LocalTime", new QName(SCHEMA_NAMESPACE, "time"));
        s_objectTypeMap.put("org.joda.time.DateTime", new QName(SCHEMA_NAMESPACE, "dateTime"));
//#j2me}
        s_objectTypeMap.put("byte[]", new QName(SCHEMA_NAMESPACE, "base64"));
        s_objectTypeMap.put("org.jibx.runtime.QName", new QName(SCHEMA_NAMESPACE, "QName"));
    }
    
    /** Set of primitive types mapped to schema types. */
    private static HashMap s_primitiveTypeMap = new HashMap();
    
    static {
        s_primitiveTypeMap.put("boolean", new QName(SCHEMA_NAMESPACE, "boolean"));
        s_primitiveTypeMap.put("byte", new QName(SCHEMA_NAMESPACE, "byte"));
        s_primitiveTypeMap.put("char", new QName(SCHEMA_NAMESPACE, "unsignedInt"));
        s_primitiveTypeMap.put("double", new QName(SCHEMA_NAMESPACE, "double"));
        s_primitiveTypeMap.put("float", new QName(SCHEMA_NAMESPACE, "float"));
        s_primitiveTypeMap.put("int", new QName(SCHEMA_NAMESPACE, "int"));
        s_primitiveTypeMap.put("long", new QName(SCHEMA_NAMESPACE, "long"));
        s_primitiveTypeMap.put("short", new QName(SCHEMA_NAMESPACE, "short"));
    }

    /**
     * Check if type represents a simple text value.
     *
     * @param type primitive type name, or fully qualified class name
     * @return simple value flag
     */
    public static boolean isSimpleValue(String type) {
        return s_primitiveTypeMap.containsKey(type) ||
            s_objectTypeMap.containsKey(type) || "void".equals(type);
    }
    
    /**
     * Get the schema type for a simple text value.
     *
     * @param type primitive type name, or fully qualified class name
     * @return schema type
     */
    public static QName schemaType(String type) {
        QName stype = (QName)s_primitiveTypeMap.get(type);
        if (stype == null) {
            stype = (QName)s_objectTypeMap.get(type);
        }
        return stype;
    }
}
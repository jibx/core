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

package org.jibx.schema.support;

import java.util.HashMap;

import org.jibx.runtime.QName;
import org.jibx.schema.INamed;
import org.jibx.schema.elements.CommonTypeDefinition;
import org.jibx.schema.elements.SchemaBase;

/**
 * Representations for predefined schema types. These are structured as elements to be consistent with user definitions,
 * but are only generated as static instances. Note that the schema type list here should always match that in
 * {@link org.jibx.schema.codegen.JavaType}.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class SchemaTypes
{
    /** Predefined schema simple types. */
    private static final HashMap s_schemaTypesMap = new HashMap();
    
    public static final SchemaSimpleType ANY_TYPE = addType("anyType", false);
   
    public static final SchemaSimpleType ANY_SIMPLE_TYPE = addType("anySimpleType", false);
    
    public static final SchemaSimpleType ANY_URI = addType("anyURI", false);
    
    public static final SchemaSimpleType BASE64_BINARY = addType("base64Binary", false);
    
    public static final SchemaSimpleType BOOLEAN_TYPE = addType("boolean", false);
    
    public static final SchemaSimpleType BYTE = addType("byte", false);
    
    public static final SchemaSimpleType DATE = addType("date", false);
    
    public static final SchemaSimpleType DATETIME = addType("dateTime", false);
    
    public static final SchemaSimpleType DECIMAL = addType("decimal", false);
    
    public static final SchemaSimpleType DOUBLE = addType("double", false);
    
    public static final SchemaSimpleType DURATION = addType("duration", false);
    
    public static final SchemaSimpleType ENTITY = addType("ENTITY", true);
    
    public static final SchemaSimpleType ENTITIES = addType("ENTITIES", false);
    
    public static final SchemaSimpleType FLOAT = addType("float", false);
    
    public static final SchemaSimpleType GDAY = addType("gDay", false);
    
    public static final SchemaSimpleType GMONTH = addType("gMonth", false);
    
    public static final SchemaSimpleType GMONTHDAY = addType("gMonthDay", false);
    
    public static final SchemaSimpleType GYEAR = addType("gYear", false);
    
    public static final SchemaSimpleType GYEARMONTH = addType("gYearMonth", false);
    
    public static final SchemaSimpleType HEX_BINARY = addType("hexBinary", false);
    
    public static final SchemaSimpleType ID = addType("ID", true);
    
    public static final SchemaSimpleType IDREF = addType("IDREF", true);
    
    public static final SchemaSimpleType IDREFS = addType("IDREFS", false);
    
    public static final SchemaSimpleType INT = addType("int", false);
    
    public static final SchemaSimpleType INTEGER = addType("integer", false);
    
    public static final SchemaSimpleType LANGUAGE = addType("language", true);
    
    public static final SchemaSimpleType LONG = addType("long", false);
    
    public static final SchemaSimpleType NAME = addType("Name", true);
    
    public static final SchemaSimpleType NEGATIVE_INTEGER = addType("negativeInteger", false);
    
    public static final SchemaSimpleType NON_NEGATIVE_INTEGER = addType("nonNegativeInteger", false);
    
    public static final SchemaSimpleType NON_POSITIVE_INTEGER = addType("nonPositiveInteger", false);
    
    public static final SchemaSimpleType NORMALIZED_STRING = addType("normalizedString", true);
    
    public static final SchemaSimpleType NCNAME = addType("NCName", true);
    
    public static final SchemaSimpleType NMTOKEN = addType("NMTOKEN", true);
    
    public static final SchemaSimpleType NMTOKENS = addType("NMTOKENS", false);
    
    public static final SchemaSimpleType NOTATION = addType("NOTATION", false);
    
    public static final SchemaSimpleType POSITIVE_INTEGER = addType("positiveInteger", false);
    
    public static final SchemaSimpleType QNAME = addType("QName", false);
    
    public static final SchemaSimpleType SHORT = addType("short", false);
    
    public static final SchemaSimpleType STRING = addType("string", true);
    
    public static final SchemaSimpleType TIME = addType("time", false);
    
    public static final SchemaSimpleType TOKEN = addType("token", true);
    
    public static final SchemaSimpleType UNSIGNED_BYTE = addType("unsignedByte", false);
    
    public static final SchemaSimpleType UNSIGNED_INT = addType("unsignedInt", false);
    
    public static final SchemaSimpleType UNSIGNED_LONG = addType("unsignedLong", false);
    
    public static final SchemaSimpleType UNSIGNED_SHORT = addType("unsignedShort", false);
    
    /**
     * Helper method for creating instances and adding them to map.
     * 
     * @param name type local name
     * @param isstring type derived from string flag
     */
    private static SchemaSimpleType addType(String name, boolean isstring) {
        SchemaSimpleType type = new SchemaSimpleType(name, isstring);
        s_schemaTypesMap.put(name, type);
        return type;
    }
    
    /**
     * Get predefined schema type.
     * 
     * @param name local name
     * @return schema type with name, or <code>null</code> if none
     */
    public static CommonTypeDefinition getSchemaType(String name) {
        return (CommonTypeDefinition)s_schemaTypesMap.get(name);
    }
    
    /**
     * Simple schema type representation.
     */
    public static class SchemaSimpleType extends CommonTypeDefinition implements INamed
    {
        
        /** Qualified name. */
        private final QName m_qname;
        
        /** String-derived type flag. */
        private final boolean m_string;
        
        /**
         * Constructor.
         * 
         * @param name schema type local name
         * @param isstring type derived from string flag
         */
        protected SchemaSimpleType(String name, boolean isstring) {
            super(SchemaBase.SIMPLETYPE_TYPE);
            m_qname = new QName(SCHEMA_NAMESPACE, name);
            m_string = isstring;
        }
        
        //
        // Base class overrides
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.CommonTypeDefinition#isComplexType()
         */
        public boolean isComplexType() {
            return false;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.jibx.schema.elements.CommonTypeDefinition#isPredefinedType()
         */
        public boolean isPredefinedType() {
            return true;
        }
        
        //
        // Access methods
        
        /**
         * Get 'name' attribute value.
         * 
         * @return name
         */
        public String getName() {
            return m_qname.getName();
        }
        
        /**
         * Get qualified name for element. This method is only usable after validation.
         * 
         * @return qname
         */
        public QName getQName() {
            return m_qname;
        }
        
        /**
         * Check for schema type derived from string.
         * 
         * @return <code>true</code> if derived from string, <code>false</code> if not
         */
        public boolean isString() {
            return m_string;
        }
    }
}
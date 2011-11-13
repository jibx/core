
package org.jibx.schema.elements;

import org.jibx.schema.SchemaTestBase;

/**
 * Test handling of simple schemas.
 */
public class EmptySchema extends SchemaTestBase
{
    public static final String EMPTY_SCHEMA =
        "<schema xmlns='http://www.w3.org/2001/XMLSchema'/>";
        
    public static final String EMPTY_SCHEMA_ATTRIBUTES =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'/>";
    
    public static final String EMPTY_SCHEMA_PREFIX =
        "<xsd:schema targetNamespace='urn:anything'" +
        "    xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'/>";
    
    public static final String EMPTY_SCHEMA_EXTRA_NAMESPACES =
        "<xsd:schema targetNamespace='urn:anything'" +
        "    xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:abc='urn:a' xmlns='urn:b'" +
        "    elementFormDefault='qualified'/>";
    
    public static final String EMPTY_SCHEMA_EXTRA_ATTRIBUTE =
        "<xsd:schema targetNamespace='urn:anything'" +
        "    xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:abc='urn:a' xmlns='urn:b' abc:xyz='123'" +
        "    xmlns:def='urn:d' elementFormDefault='qualified'/>";
    
    public static final String EMPTY_SCHEMA_UNKNOWN_ATTRIBUTE =
        "<xsd:schema targetNamespace='urn:anything'" +
        "    xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:abc='urn:a' xmlns='urn:b' xyz='123'" +
        "    elementFormDefault='qualified'/>";
    
    public static final String EMPTY_SCHEMA_ALL_ATTRIBUTES1 =
        "<xsd:schema xml:lang='en' targetNamespace='urn:anything'" +
        "    xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified' attributeFormDefault='qualified'" +
        "    blockDefault='#all' finalDefault='#all' version='1.0'/>";
    
    public static final String EMPTY_SCHEMA_ALL_ATTRIBUTES2 =
        "<xsd:schema xml:lang='en' targetNamespace='urn:anything'" +
        "    xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='unqualified' attributeFormDefault='unqualified'" +
        "    blockDefault='extension restriction substitution'" +
        "    finalDefault='extension restriction' version='1.0'/>";
    
    public static final String EMPTY_SCHEMA_UNKNOWN_VALUES =
        "<xsd:schema xml:lang='en' targetNamespace='urn:anything'" +
        "    xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='xxx' attributeFormDefault='yyy'" +
        "    blockDefault='extension zzz restriction'" +
        "    finalDefault='extension restriction www' version='1.0'/>";
        
    public void testEmpty() throws Exception {
        runNoErrors(EMPTY_SCHEMA);
    }
    
    public void testEmptyAttributes() throws Exception {
        runNoErrors(EMPTY_SCHEMA_ATTRIBUTES);
    }
    
    public void testEmptyPrefix() throws Exception {
        runNoErrors(EMPTY_SCHEMA_PREFIX);
    }
    
    public void testEmptyExtraNamespaces() throws Exception {
        runNoErrors(EMPTY_SCHEMA_EXTRA_NAMESPACES);
    }
    
    public void testEmptyExtraAttribute() throws Exception {
        runNoErrors(EMPTY_SCHEMA_EXTRA_ATTRIBUTE);
    }
    
    public void testEmptyUnknownAttribute() throws Exception {
        runOneError(EMPTY_SCHEMA_UNKNOWN_ATTRIBUTE,
            "Unknown attribute not reported");
    }
    
    public void testEmptyAllAttributes1() throws Exception {
        runNoErrors(EMPTY_SCHEMA_ALL_ATTRIBUTES1);
    }
    
    public void testEmptyAllAttributes2() throws Exception {
        runNoErrors(EMPTY_SCHEMA_ALL_ATTRIBUTES2);
    }
    
    public void testEmptyUnknownValues() throws Exception {
        runErrors(EMPTY_SCHEMA_UNKNOWN_VALUES, 4, 
            "Unknown attribute value not reported");
    }
}
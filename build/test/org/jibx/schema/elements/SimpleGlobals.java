
package org.jibx.schema.elements;

import org.jibx.schema.SchemaTestBase;

/**
 * Test handling of simple schemas with only global elements or attributes and
 * no nested structure.
 */
public class SimpleGlobals extends SchemaTestBase
{
    public static final String SIMPLE_ELEMENT_BLANK_SCHEMA =
        "<schema xmlns='http://www.w3.org/2001/XMLSchema'>\n" +
        "  <element name='simple' type='string'/>\n" +
        "</schema>";
        
    public static final String SIMPLE_ATTRIBUTE_BLANK_SCHEMA =
        "<schema xmlns='http://www.w3.org/2001/XMLSchema'>\n" +
        "  <attribute name='simple' type='string'/>\n" +
        "</schema>";
        
    public static final String SIMPLE_ELEMENT_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <element name='simple' type='string'/>\n" +
        "</schema>";
        
    public static final String SIMPLE_ATTRIBUTE_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <attribute name='simple' type='string'/>\n" +
        "</schema>";
    
    public static final String COMPLEX_ELEMENT_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <element name='simple' type='string' substitutionGroup='subs' nillable='true' default='abc' final='#all' block='extension restriction'>\n" +
        "    <annotation>\n" +
        "      <documentation>this should mean something</documentation>\n" +
        "    </annotation>\n" +
        "  </element>\n" +
        "</schema>";
        
    public static final String COMPLEX_ATTRIBUTE_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <attribute name='simple1' type='int'>\n" +
        "    <annotation>\n" +
        "      <documentation>this should mean something</documentation>\n" +
        "    </annotation>\n" +
        "  </attribute>\n" +
        "  <attribute name='simple2' type='int' default='5'/>\n" +
        "</schema>";
    
    public static final String COMPLEX_ELEMENT_ERROR_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <element name='simple' form='unqualified' minOccurs='0' maxOccurs='1' substitutionGroup='subs' nillable='true' default='abc' final='#all' block='extension restriction'/>\n" +
        "</schema>";
    
    public static final String COMPLEX_ATTRIBUTE_ERROR_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <attribute name='simple1' form='qualified' use='optional' type='int'/>\n" +
        "</schema>";
    
    public static final String ELEMENT_TYPE_REFERENCE_ERROR_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <element name='simple' type='unknown'/>\n" +
        "</schema>";
    
    public void testSimpleElementBlank() throws Exception {
        runNoErrors(SIMPLE_ELEMENT_BLANK_SCHEMA);
    }
    
    public void testSimpleAttributeBlank() throws Exception {
        runNoErrors(SIMPLE_ATTRIBUTE_BLANK_SCHEMA);
    }
    
    public void testSimpleElement() throws Exception {
        runNoErrors(SIMPLE_ELEMENT_SCHEMA);
    }
    
    public void testSimpleAttribute() throws Exception {
        runNoErrors(SIMPLE_ATTRIBUTE_SCHEMA);
    }
    
    public void testComplexElement() throws Exception {
        runNoErrors(COMPLEX_ELEMENT_SCHEMA);
    }
    
    public void testComplexAttribute() throws Exception {
        runNoErrors(COMPLEX_ATTRIBUTE_SCHEMA);
    }
    
    public void testComplexElementError() throws Exception {
        runOneError(COMPLEX_ELEMENT_ERROR_SCHEMA,
            "Invalid attributes on global <element> not reported");
    }
    
    public void testComplexAttributeError() throws Exception {
        runOneError(COMPLEX_ATTRIBUTE_ERROR_SCHEMA,
            "Invalid attributes on global <attribute> not reported");
    }
    
    public void testElementTypeReferenceError() throws Exception {
        runOneError(ELEMENT_TYPE_REFERENCE_ERROR_SCHEMA,
            "Undefined type reference on <element> not reported");
    }
}
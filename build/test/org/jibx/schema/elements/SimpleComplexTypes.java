
package org.jibx.schema.elements;

import org.jibx.schema.SchemaTestBase;

/**
 * Test handling of schemas with simple complex type definitions.
 */
public class SimpleComplexTypes extends SchemaTestBase
{
    public static final String COMPLEX_EMPTY_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple'/>\n" +
        "</schema>";
    
    public static final String COMPLEX_SEQUENCE_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "    </sequence>\n" +
        "  </complexType>\n" +
        "</schema>";
    
    public static final String COMPLEX_SEQUENCE_ATTRIBUTES_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple'>\n" +
        "    <choice minOccurs='1' maxOccurs='3'>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "      <element ref='tns:simple'/>\n" +
        "    </choice>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "    <attribute name='registered' default='true' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='simple' type='string'/>\n" +
        "</schema>";
        
    public static final String COMPLEX_EMBEDDED_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <element name='complex'>\n" +
        "    <complexType>\n" +
        "      <sequence>\n" +
        "        <element name='name' minOccurs='1' type='string'/>\n" +
        "        <element name='age' type='int'/>\n" +
        "      </sequence>\n" +
        "      <attribute name='male' use='required' type='boolean'/>\n" +
        "    </complexType>\n" +
        "  </element>\n" +
        "</schema>";
    
    public static final String COMPLEX_REFERENCED_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "    <attribute name='registered' default='true' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='simple' type='tns:simple'/>\n" +
        "</schema>";
    
    public void testComplexEmpty() throws Exception {
        runNoErrors(COMPLEX_EMPTY_SCHEMA);
    }
    
    public void testComplexSequence() throws Exception {
        runNoErrors(COMPLEX_SEQUENCE_SCHEMA);
    }
    
    public void testComplexSequenceAttributes() throws Exception {
        runNoErrors(COMPLEX_SEQUENCE_ATTRIBUTES_SCHEMA);
    }
    
    public void testComplexEmbedded() throws Exception {
        runNoErrors(COMPLEX_EMBEDDED_SCHEMA);
    }
    
    public void testComplexReferenced() throws Exception {
        runNoErrors(COMPLEX_REFERENCED_SCHEMA);
    }
}
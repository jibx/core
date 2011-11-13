
package org.jibx.schema.elements;

import org.jibx.schema.SchemaTestBase;

/**
 * Test handling of schemas with simple simple type definitions.
 */
public class SimpleSimpleTypes extends SchemaTestBase
{
    public static final String PREDEFINED_SIMPLE_TYPES =
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' elementFormDefault='qualified' attributeFormDefault='unqualified'>\n" +
        "	<xs:element name='String' type='xs:string'/>\n" +
        "	<xs:element name='Boolean' type='xs:boolean'/>\n" +
        "	<xs:element name='Decimal' type='xs:decimal'/>\n" +
        "	<xs:element name='Float' type='xs:float'/>\n" +
        "	<xs:element name='Double' type='xs:double'/>\n" +
        "	<xs:element name='Date' type='xs:date'/>\n" +
        "	<xs:element name='DateTime' type='xs:dateTime'/>\n" +
        "	<xs:element name='Duration' type='xs:duration'/>\n" +
        "	<xs:element name='Day' type='xs:gDay'/>\n" +
        "	<xs:element name='Month' type='xs:gMonth'/>\n" +
        "	<xs:element name='MonthDay' type='xs:gMonthDay'/>\n" +
        "	<xs:element name='Year' type='xs:gYear'/>\n" +
        "	<xs:element name='Base64Bin' type='xs:base64Binary'/>\n" +
        "	<xs:element name='HexBin' type='xs:hexBinary'/>\n" +
        "	<xs:element name='QName' type='xs:QName'/>\n" +
        "	<xs:element name='Notation' type='xs:NOTATION'/>\n" +
        "	<xs:element name='AnyURI' type='xs:anyURI'/>\n" +
        "	<xs:element name='YearMonth' type='xs:gYearMonth'/>\n" +
        "	<xs:element name='Time' type='xs:time'/>\n" +
        "	<xs:element name='PositiveInteger' type='xs:positiveInteger'/>\n" +
        "	<xs:element name='NegativeInteger' type='xs:negativeInteger'/>\n" +
        "	<xs:element name='NonNegativeInteger' type='xs:nonNegativeInteger'/>\n" +
        "	<xs:element name='NonpositiveInteger' type='xs:nonPositiveInteger'/>\n" +
        "	<xs:element name='UnsignedShort' type='xs:unsignedShort'/>\n" +
        "	<xs:element name='UnsignedLong' type='xs:unsignedLong'/>\n" +
        "	<xs:element name='UnsignedInt' type='xs:unsignedInt'/>\n" +
        "	<xs:element name='UnsignedByte' type='xs:unsignedByte'/>\n" +
        "	<xs:element name='Long' type='xs:long'/>\n" +
        "	<xs:element name='Short' type='xs:short'/>\n" +
        "	<xs:element name='Int' type='xs:int'/>\n" +
        "	<xs:element name='Byte' type='xs:byte'/>\n" +
        "	<xs:element name='Token' type='xs:token'/>\n" +
        "	<xs:element name='Language' type='xs:language'/>\n" +
        "	<xs:element name='NMTOKEN' type='xs:NMTOKEN'/>\n" +
        "	<xs:element name='NMTOKENS' type='xs:NMTOKENS'/>\n" +
        "	<xs:element name='ID' type='xs:ID'/>\n" +
        "	<xs:element name='IDRef' type='xs:IDREF'/>\n" +
        "	<xs:element name='Entity' type='xs:ENTITY'/>\n" +
        "	<xs:element name='Entities' type='xs:ENTITIES'/>\n" +
        "	<xs:element name='Name' type='xs:Name'/>\n" +
        "	<xs:element name='NameToken' type='xs:NMTOKEN'/>\n" +
        "	<xs:element name='NCName' type='xs:NCName'/>\n" +
        "	<xs:element name='NormalizedString' type='xs:normalizedString'/>\n" +
        "	<xs:element name='Integer' type='xs:integer'/>\n" +
        "</xs:schema>";
        
    public static final String SIMPLE_RESTRICTION_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <restriction base='string'/>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String COMPLEX_RESTRICTION_SCHEMA1 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <restriction base='string'>\n" +
        "      <length value='5'/>\n" +
        "      <whiteSpace value='replace'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String COMPLEX_RESTRICTION_SCHEMA2 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <restriction base='int'>\n" +
        "      <minExclusive value='1'/>\n" +
        "      <maxInclusive value='10' fixed='true'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String COMPLEX_RESTRICTION_SCHEMA3 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <restriction base='token'>\n" +
        "      <enumeration value='poor'/>\n" +
        "      <enumeration value='fair'/>\n" +
        "      <enumeration value='good'/>\n" +
        "      <enumeration value='excellent'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String RESTRICTION_FACET_ERROR_SCHEMA =
        // invalid attributes on facets
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <restriction base='string'>\n" +
        "      <pattern value='*' fixed='false'/>\n" +
        "      <whiteSpace value='replace' name='3'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String RESTRICTION_REFERENCE_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='base'>\n" +
        "    <restriction base='string'>\n" +
        "      <minLength value='4'/>\n" +
        "      <maxLength value='10'/>\n" +
        "      <whiteSpace value='collapse'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='simple'>\n" +
        "    <restriction base='tns:base'>\n" +
        "      <enumeration value='poor'/>\n" +
        "      <enumeration value='fair'/>\n" +
        "      <enumeration value='good'/>\n" +
        "      <enumeration value='excellent'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String SIMPLE_LIST_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <list itemType='string'/>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String RESTRICTION_LIST_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <list>\n" +
        "      <simpleType>\n" +
        "        <restriction base='int'>\n" +
        "          <minExclusive value='1'/>\n" +
        "          <maxInclusive value='10' fixed='true'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "    </list>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String REFERENCE_LIST_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='base'>\n" +
        "    <restriction base='string'>\n" +
        "      <minLength value='4'/>\n" +
        "      <maxLength value='10'/>\n" +
        "      <whiteSpace value='collapse'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='simple'>\n" +
        "    <list>\n" +
        "      <simpleType>\n" +
        "        <restriction base='int'>\n" +
        "          <minExclusive value='1'/>\n" +
        "          <maxInclusive value='10' fixed='true'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "    </list>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String LIST_ERROR_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <list itemType='string'>\n" +
        "      <simpleType>\n" +
        "        <restriction base='int'>\n" +
        "          <minExclusive value='1'/>\n" +
        "          <maxInclusive value='10' fixed='true'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "    </list>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String UNION_REFERENCE_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='rating'>\n" +
        "    <restriction base='int'>\n" +
        "      <minExclusive value='1'/>\n" +
        "      <maxInclusive value='10' fixed='true'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='simple'>\n" +
        "    <union memberTypes='date dateTime tns:rating'/>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String UNION_INLINE_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple'>\n" +
        "    <union>\n" +
        "      <simpleType>\n" +
        "        <restriction base='int'>\n" +
        "          <minExclusive value='1'/>\n" +
        "          <maxInclusive value='10' fixed='true'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "      <simpleType>\n" +
        "        <restriction base='string'>\n" +
        "          <enumeration value='unknown'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "    </union>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public static final String UNION_MIXED_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='simple1'>\n" +
        "    <union memberTypes='tns:simple2'>\n" +
        "      <simpleType>\n" +
        "        <restriction base='int'>\n" +
        "          <minExclusive value='1'/>\n" +
        "          <maxInclusive value='10' fixed='true'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "      <simpleType>\n" +
        "        <restriction base='string'>\n" +
        "          <enumeration value='unknown'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "    </union>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='simple2'>\n" +
        "    <restriction base='string'>\n" +
        "      <enumeration value='poor'/>\n" +
        "      <enumeration value='fair'/>\n" +
        "      <enumeration value='good'/>\n" +
        "      <enumeration value='excellent'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "</schema>";
    
    public void testPredefinedSimpleTypes() throws Exception {
        runNoErrors(PREDEFINED_SIMPLE_TYPES);
    }
    
    public void testSimpleRestriction() throws Exception {
        runNoErrors(SIMPLE_RESTRICTION_SCHEMA);
    }
    
    public void testComplexRestrictionSchema1() throws Exception {
        runNoErrors(COMPLEX_RESTRICTION_SCHEMA1);
    }
    
    public void testComplexRestrictionSchema2() throws Exception {
        runNoErrors(COMPLEX_RESTRICTION_SCHEMA2);
    }
    
    public void testComplexRestrictionSchema3() throws Exception {
        runNoErrors(COMPLEX_RESTRICTION_SCHEMA3);
    }
    
    public void testRestrictionFacetError() throws Exception {
        runOneError(RESTRICTION_FACET_ERROR_SCHEMA,
            "facets with illegal attribute not reported");
    }
    
    public void testRestrictionReference() throws Exception {
        runNoErrors(RESTRICTION_REFERENCE_SCHEMA);
    }
    
    public void testSimpleList() throws Exception {
        runNoErrors(SIMPLE_LIST_SCHEMA);
    }
    
    public void testRestrictionList() throws Exception {
        runNoErrors(RESTRICTION_LIST_SCHEMA);
    }
    
    public void testReferenceList() throws Exception {
        runNoErrors(REFERENCE_LIST_SCHEMA);
    }
    
    public void testListError() throws Exception {
        runOneError(LIST_ERROR_SCHEMA,
            "<list> with embedded type and itemType attribute not reported");
    }
    
    public void testUnionReference() throws Exception {
        runNoErrors(UNION_REFERENCE_SCHEMA);
    }
    
    public void testUnionInline() throws Exception {
        runNoErrors(UNION_INLINE_SCHEMA);
    }
    
    public void testUnionMixed() throws Exception {
        runNoErrors(UNION_MIXED_SCHEMA);
    }
}
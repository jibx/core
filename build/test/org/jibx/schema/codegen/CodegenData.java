
package org.jibx.schema.codegen;

import java.util.HashMap;
import java.util.Map;

import org.jibx.schema.SchemaTestBase;

/**
 * Data used by code generation tests.
 */
public class CodegenData extends SchemaTestBase
{
    static final String MAIN_SCHEMA1 =
        "<schema targetNamespace='urn:something'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:ins='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <import namespace='urn:anything' schemaLocation='INCLUDED_SCHEMA1'/>\n" +
        "  <element name='element' type='ins:simple3'/>\n" +
        "  <element name='extra' type='string'/>\n" +
        "</schema>";
    static final String INCLUDED_SCHEMA1 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple1'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='rated' type='tns:rating'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <simpleType name='simple2'>\n" +
        "    <list itemType='string'/>\n" +
        "  </simpleType>\n" +
        "  <complexType name='simple3'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:simple1'/>\n" +
        "      <element name='mixed' type='tns:mixedUnion'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='rated' type='tns:rating'/>\n" +
        "    <attribute name='index' type='integer'/>\n" +
        "  </complexType>\n" +
        "  <simpleType name='rating'>\n" +
        "    <restriction base='int'>\n" +
        "      <minExclusive value='1'/>\n" +
        "      <maxInclusive value='10' fixed='true'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='mixedUnion'>\n" +
        "    <union memberTypes='date dateTime tns:rating'/>\n" +
        "  </simpleType>\n" +
        "</schema>";
    // the result schema reflects union replacement by string in current codebase
    static final String RESULT_SCHEMA1_A =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple1'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='rated' type='int'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <complexType name='simple3'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:simple1'/>\n" +
//        "      <element name='mixed' type='tns:mixedUnion'/>\n" +
        "      <element name='mixed' type='xs:string'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='rated' type='int'/>\n" +
        "    <attribute name='index' type='integer'/>\n" +
        "  </complexType>\n" +
//        "  <simpleType name='mixedUnion'>\n" +
//        "    <union memberTypes='date dateTime tns:rating'/>\n" +
//        "  </simpleType>\n" +
        "</schema>";
    static final String CUSTOMIZATION1_B =
        "<schema-set>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      type-substitutions='tns:rating xs:float xs:integer xs:int'/>\n" +
        "</schema-set> ";
    // the result schema reflects union replacement by string in current codebase
    static final String RESULT_SCHEMA1_B =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:xs='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple1'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='rated' type='xs:float'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <complexType name='simple3'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:simple1'/>\n" +
//        "      <element name='mixed' type='tns:mixedUnion'/>\n" +
        "      <element name='mixed' type='xs:string'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='rated' type='xs:float'/>\n" +
        "    <attribute name='index' type='xs:int'/>\n" +
        "  </complexType>\n" +
//        "  <simpleType name='mixedUnion'>\n" +
//        "    <union memberTypes='date dateTime xs:int'/>\n" +
//        "  </simpleType>\n" +
        "</schema>";
    static final String CUSTOMIZATION1_C =
        "<schema-set>\n" +
        "  <schema xmlns:tns='urn:anything' namespace='urn:anything' excludes='rating'/>\n" +
        "</schema-set> ";
    // the result schema reflects union replacement by string in current codebase
    static final String RESULT_SCHEMA1_C =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple1'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <complexType name='simple3'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:simple1'/>\n" +
//      "      <element name='mixed' type='tns:mixedUnion'/>\n" +
        "      <element name='mixed' type='xs:string'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='index' type='integer'/>\n" +
        "  </complexType>\n" +
//        "  <simpleType name='mixedUnion'>\n" +
//        "    <union memberTypes='date dateTime'/>\n" +
//        "  </simpleType>\n" +
        "</schema>";
    static final String MAIN_SCHEMA2 =
        "<schema targetNamespace='urn:something'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:ins='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <import namespace='urn:anything' schemaLocation='INCLUDED_SCHEMA2'/>\n" +
        "  <element name='element' type='ins:simple3'/>\n" +
        "</schema>";
    static final String INCLUDED_SCHEMA2 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple1'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='rated' type='tns:rating'/>\n" +
        "      <element ref='tns:timestamp'/>\n" +
        "      <element name='altstamp' type='tns:DateTime_NoID'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='timestamp' type='tns:DateTime'/>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <simpleType name='simple2'>\n" +
        "    <list itemType='string'/>\n" +
        "  </simpleType>\n" +
        "  <complexType name='simple3'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:simple1'/>\n" +
        "      <element name='mixed' type='tns:mixedUnion'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='rated' type='tns:rating'/>\n" +
        "  </complexType>\n" +
        "  <simpleType name='rating'>\n" +
        "    <restriction base='int'>\n" +
        "      <minExclusive value='1'/>\n" +
        "      <maxInclusive value='10' fixed='true'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='mixedUnion'>\n" +
        "    <union memberTypes='date dateTime tns:rating'/>\n" +
        "  </simpleType>\n" +
        "  <complexType name='DateTime'>\n" +
        "    <simpleContent>\n" +
        "      <extension base='tns:DateTime_NoID'>\n" +
        "        <attribute name='id' type='tns:ID'/>\n" +
        "      </extension>\n" +
        "    </simpleContent>\n" +
        "  </complexType>\n" +
        "  <simpleType name='ID'>\n" +
        "    <restriction base='ID'/>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='DateTime_NoID'>\n" +
        "    <restriction base='dateTime'/>\n" +
        "  </simpleType>" +
        "</schema>";
    static final String CUSTOMIZATION2_A =
        "<schema-set>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      type-substitutions='tns:rating xs:float'/>\n" +
        "</schema-set> ";
    // the result schema reflects union and ID replacement by string in current codebase
    static final String RESULT_SCHEMA2_A =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:xs='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple1'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='rated' type='xs:float'/>\n" +
        "      <element ref='tns:timestamp'/>\n" +
        "      <element name='altstamp' type='dateTime'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='timestamp' type='tns:DateTime'/>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <complexType name='simple3'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:simple1'/>\n" +
//        "      <element name='mixed' type='tns:mixedUnion'/>\n" +
        "      <element name='mixed' type='xs:string'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='rated' type='xs:float'/>\n" +
        "  </complexType>\n" +
//        "  <simpleType name='mixedUnion'>\n" +
//        "    <union memberTypes='date dateTime xs:int'/>\n" +
//        "  </simpleType>\n" +
        "  <complexType name='DateTime'>\n" +
        "    <simpleContent>\n" +
        "      <extension base='dateTime'>\n" +
        "        <attribute name='id' type='xs:string'/>\n" +
        "      </extension>\n" +
        "    </simpleContent>\n" +
        "  </complexType>\n" +
        "</schema>";
    static final String CUSTOMIZATION2_B =
        "<schema-set>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      type-substitutions='tns:rating xs:float tns:DateTime_NoID xs:date' excludes='ID'/>\n" +
        "</schema-set> ";
    // the result schema reflects union replacement by string in current codebase
    static final String RESULT_SCHEMA2_B =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:xs='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <complexType name='simple1'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='rated' type='xs:float'/>\n" +
        "      <element ref='tns:timestamp'/>\n" +
        "      <element name='altstamp' type='xs:date'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='timestamp' type='tns:DateTime'/>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <complexType name='simple3'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:simple1'/>\n" +
//        "      <element name='mixed' type='tns:mixedUnion'/>\n" +
        "      <element name='mixed' type='xs:string'/>\n" +
        "    </sequence>\n" +
        "    <attribute name='rated' type='xs:float'/>\n" +
        "  </complexType>\n" +
//        "  <simpleType name='mixedUnion'>\n" +
//        "    <union memberTypes='date dateTime xs:int'/>\n" +
//        "  </simpleType>\n" +
        "  <complexType name='DateTime'>\n" +
        "    <simpleContent>\n" +
        "      <extension base='xs:date'/>\n" +
        "    </simpleContent>\n" +
        "  </complexType>\n" +
        "</schema>";
    static final String MAIN_SCHEMA3 =
        "<schema targetNamespace='urn:something'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:ins='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <import namespace='urn:anything' schemaLocation='INCLUDED_SCHEMA3'/>\n" +
        "  <element name='element' type='ins:simple3'/>\n" +
        "</schema>";
    static final String INCLUDED_SCHEMA3 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <simpleType name='shortString'>\n" +
        "    <restriction base='string'>\n" +
        "      <maxLength value='10'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='enum1'>\n" +
        "    <restriction base='string'>\n" +
        "      <enumeration value='A1'/>\n" +
        "      <enumeration value='B2'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='enum2'>\n" +
        "    <restriction base='tns:shortString'>\n" +
        "      <enumeration value='a1'/>\n" +
        "      <enumeration value='b2'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <complexType name='simple1'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:name'/>\n" +
        "      <element ref='tns:rated'/>\n" +
        "    </sequence>\n" +
        "    <attributeGroup ref='tns:enum1Group'/>\n" +
        "    <attributeGroup ref='tns:enum23Group'/>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <simpleType name='simple2'>\n" +
        "    <list itemType='string'/>\n" +
        "  </simpleType>\n" +
        "  <attributeGroup name='enum1Group'>\n" +
        "    <attribute name='enum1' use='required' type='tns:enum1'/>\n" +
        "  </attributeGroup>\n" +
        "  <attributeGroup name='enum23Group'>\n" +
        "    <attribute name='enum2' use='required' type='tns:enum2'/>\n" +
        "    <attribute name='enum3' use='required'>\n" +
        "      <simpleType>\n" +
        "        <restriction base='string'>\n" +
        "          <enumeration value='x1'/>\n" +
        "          <enumeration value='y2'/>\n" +
        "          <enumeration value='z3'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "    </attribute>\n" +
        "  </attributeGroup>\n" +
        "  <element name='name' type='string'/>\n" +
        "  <element name='rated' type='tns:rating'/>\n" +
        "  <element name='mixed' type='tns:mixedUnion'/>\n" +
        "  <complexType name='simple3'>\n" +
        "    <all>\n" +
        "      <element ref='tns:simple1'/>\n" +
        "      <element ref='tns:mixed'/>\n" +
        "    </all>\n" +
        "    <attribute name='rated' type='tns:rating'/>\n" +
        "    <attributeGroup ref='tns:enum1Group'/>\n" +
        "    <attributeGroup ref='tns:enum23Group'/>\n" +
        "  </complexType>\n" +
        "  <simpleType name='rating'>\n" +
        "    <restriction base='int'>\n" +
        "      <minExclusive value='1'/>\n" +
        "      <maxInclusive value='10' fixed='true'/>\n" +
        "    </restriction>\n" +
        "  </simpleType>\n" +
        "  <simpleType name='mixedUnion'>\n" +
        "    <union memberTypes='date dateTime tns:rating'/>\n" +
        "  </simpleType>\n" +
        "  <complexType name='nestedType'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:name' minOccurs='0' maxOccurs='9'/>\n" +
        "      <element name='enums'>\n" +
        "        <complexType>\n" +
        "          <sequence>\n" +
        "            <element name='enum' maxOccurs='9'>\n" +
        "              <complexType>\n" +
        "                <attributeGroup ref='tns:enum1Group'/>\n" +
        "              </complexType>\n" +
        "            </element>\n" +
        "          </sequence>\n" +
        "        </complexType>\n" +
        "      </element>\n" +
        "    </sequence>\n" +
        "  </complexType>\n" +
        "</schema>";
    static final String CUSTOMIZATION3_A =
        "<schema-set>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      type-substitutions='tns:rating xs:float'/>\n" +
        "</schema-set> ";
    // the result schema reflects union replacement by string in current codebase
    static final String RESULT_SCHEMA3_A =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:xs='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <xs:simpleType name='enum1'>\n" +
        "    <xs:restriction base='string'>\n" +
        "      <xs:enumeration value='A1'/>\n" +
        "      <xs:enumeration value='B2'/>\n" +
        "    </xs:restriction>\n" +
        "  </xs:simpleType>\n" +
        "  <xs:simpleType name='enum2'>\n" +
        "    <xs:restriction base='string'>\n" +
        "      <xs:enumeration value='a1'/>\n" +
        "      <xs:enumeration value='b2'/>\n" +
        "    </xs:restriction>\n" +
        "  </xs:simpleType>\n" +
        "  <xs:complexType name='simple1'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element ref='tns:name'/>\n" +
        "      <xs:element ref='tns:rated'/>\n" +
        "    </xs:sequence>\n" +
        "    <attributeGroup ref='tns:enum1Group'/>\n" +
        "    <attributeGroup ref='tns:enum23Group'/>\n" +
        "    <xs:attribute type='boolean' use='required' name='male'/>\n" +
        "  </xs:complexType>\n" +
        "  <element name='simple1' type='tns:simple1'/>\n" +
        "  <attributeGroup name='enum1Group'>\n" +
        "    <attribute name='enum1' use='required' type='tns:enum1'/>\n" +
        "  </attributeGroup>\n" +
        "  <attributeGroup name='enum23Group'>\n" +
        "    <attribute name='enum2' use='required' type='tns:enum2'/>\n" +
        "    <attribute name='enum3' use='required'>\n" +
        "      <simpleType>\n" +
        "        <restriction base='string'>\n" +
        "          <enumeration value='x1'/>\n" +
        "          <enumeration value='y2'/>\n" +
        "          <enumeration value='z3'/>\n" +
        "        </restriction>\n" +
        "      </simpleType>\n" +
        "    </attribute>\n" +
        "  </attributeGroup>\n" +
        "  <element name='name' type='string'/>\n" +
        "  <element name='rated' type='xs:float'/>\n" +
//        "  <element name='mixed' type='tns:mixedUnion'/>" +
        "  <element name='mixed' type='xs:string'/>" +
        "  <complexType name='simple3'>\n" +
        "    <all>\n" +
        "      <element ref='tns:simple1'/>\n" +
        "      <element ref='tns:mixed'/>\n" +
        "    </all>\n" +
        "    <attribute name='rated' type='xs:float'/>\n" +
        "    <attributeGroup ref='tns:enum1Group'/>\n" +
        "    <attributeGroup ref='tns:enum23Group'/>\n" +
        "  </complexType>\n" +
//        "  <simpleType name='mixedUnion'>\n" +
//        "    <union memberTypes='date dateTime xs:int'/>\n" +
//        "  </simpleType>\n" +
        "  <complexType name='nestedType'>\n" +
        "    <sequence>\n" +
        "      <element ref='tns:name' minOccurs='0' maxOccurs='9'/>\n" +
        "      <element name='enums'>\n" +
        "        <complexType>\n" +
        "          <sequence>\n" +
        "            <element name='enum' maxOccurs='9'>\n" +
        "              <complexType>\n" +
        "                <attributeGroup ref='tns:enum1Group'/>\n" +
        "              </complexType>\n" +
        "            </element>\n" +
        "          </sequence>\n" +
        "        </complexType>\n" +
        "      </element>\n" +
        "    </sequence>\n" +
        "  </complexType>\n" +
        "</schema>";
    // customizations used by code generation test
    static final String CUSTOMIZATION3_B =
        "<schema-set enumeration-type='simple'>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      type-substitutions='tns:rating xs:float' generate-all='false'/>\n" +
        "</schema-set>";
    static final String CUSTOMIZATION3_C =
        "<schema-set enumeration-type='simple'>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      type-substitutions='tns:rating xs:float' generate-all='false' prefer-inline='true'/>\n" +
        "</schema-set>";
    static final String CUSTOMIZATION3_D =
        "<schema-set enumeration-type='simple'>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      type-substitutions='tns:rating xs:float' generate-all='false' prefer-inline='true'>\n" +
        "    <simpleType name='enum2' exclude='true'/>\n" +
        "    <complexType name='simple1' exclude='true'/>\n" +
        "  </schema>\n" +
        "</schema-set>";
    // TODO: make this work
    static final String CUSTOMIZATION3_E =
        "<schema-set enumeration-type='simple'>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      type-substitutions='tns:rating xs:float' generate-all='false'>\n" +
        "    <simpleType name='enum2' exclude='true'/>\n" +
        "    <element path='complexType[@name=simple3]/sequence/element[1]' ignore='true'/>\n" +
        "  </schema>\n" +
        "</schema-set>";
    
    static final String OTA_PROFILETYPE_SIMPLIFIED_MAIN_UNQUALIFIED =
        "<schema targetNamespace='urn:something'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:ins='urn:anything'>\n" +
        "  <import namespace='urn:anything' schemaLocation='OTA_PROFILETYPE_SIMPLIFIED_INCLUDE_UNQUALIFIED'/>\n" +
        "  <element name='element' type='ins:ProfileType'/>\n" +
        "</schema>";
    static final String OTA_PROFILETYPE_SIMPLIFIED_INCLUDE_UNQUALIFIED =
        "<xs:schema targetNamespace='urn:anything'" +
        "    xmlns:xs='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'>\n" +
        "  <xs:complexType name='FormattedTextTextType'>\n" +
        "    <xs:simpleContent>\n" +
        "       <xs:extension base='xs:string'>\n" +
        "         <xs:attribute name='Formatted' type='xs:boolean' use='optional'/>\n" +
        "       </xs:extension>\n" +
        "    </xs:simpleContent>\n" +
        "  </xs:complexType>\n" +
        "  <xs:complexType name='ParagraphType'>\n" +
        "    <xs:choice minOccurs='0' maxOccurs='unbounded'>\n" +
        "      <xs:element name='Text' type='tns:FormattedTextTextType'/>\n" +
        "      <xs:element name='Image' type='xs:string'/>\n" +
        "      <xs:element name='ListItem'>\n" +
        "        <xs:complexType>\n" +
        "          <xs:simpleContent>\n" +
        "            <xs:extension base='tns:FormattedTextTextType'>\n" +
        "              <xs:attribute name='ListItem' type='xs:integer' use='optional'/>\n" +
        "            </xs:extension>\n" +
        "          </xs:simpleContent>\n" +
        "        </xs:complexType>\n" +
        "      </xs:element>\n" +
        "    </xs:choice>\n" +
        "    <xs:attribute name='Name' type='xs:string' use='optional'/>\n" +
        "    <xs:attribute name='ParagraphNumber' type='xs:nonNegativeInteger' use='optional'/>\n" +
        "   </xs:complexType>\n" +
        "   <xs:attributeGroup name='PrivacyGroup'>\n" +
        "     <xs:attribute name='ShareSynchInd' use='optional'>\n" +
        "       <xs:simpleType>\n" +
        "         <xs:restriction base='xs:NMTOKEN'>\n" +
        "           <xs:enumeration value='Yes'/>\n" +
        "           <xs:enumeration value='No'/>\n" +
        "           <xs:enumeration value='Inherit'/>\n" +
        "         </xs:restriction>\n" +
        "       </xs:simpleType>\n" +
        "     </xs:attribute>\n" +
        "     <xs:attribute name='ShareMarketInd' use='optional'>\n" +
        "       <xs:simpleType>\n" +
        "         <xs:restriction base='xs:NMTOKEN'>\n" +
        "           <xs:enumeration value='Yes'/>\n" +
        "           <xs:enumeration value='No'/>\n" +
        "           <xs:enumeration value='Inherit'/>\n" +
        "         </xs:restriction>\n" +
        "       </xs:simpleType>\n" +
        "     </xs:attribute>\n" +
        "  </xs:attributeGroup>\n" +
        "  <xs:attributeGroup name='TelephoneAttributesGroup'>\n" +
        "    <xs:attribute name='CountryAccessCode' type='xs:string' use='optional'/>\n" +
        "    <xs:attribute name='AreaCityCode' type='xs:string' use='optional'/>\n" +
        "    <xs:attribute name='PhoneNumber' type='xs:string' use='required'/>\n" +
        "    <xs:attribute name='Extension' type='xs:string' use='optional'/>\n" +
        "  </xs:attributeGroup>\n" +
        "  <xs:attributeGroup name='TelephoneGroup'>\n" +
        "    <xs:attributeGroup ref='tns:PrivacyGroup'/>\n" +
        "    <xs:attributeGroup ref='tns:TelephoneAttributesGroup'/>\n" +
        "  </xs:attributeGroup>\n" +
        "  <xs:simpleType name='RPH_Type'>\n" +
        "    <xs:restriction base='xs:string'>\n" +
        "      <xs:pattern value='[0-9]{1,8}'/>\n" +
        "    </xs:restriction>\n" +
        "  </xs:simpleType>\n" +
        "  <xs:attributeGroup name='TelephoneInfoGroup'>\n" +
        "    <xs:attributeGroup ref='tns:TelephoneGroup'/>\n" +
        "    <xs:attribute name='RPH' type='tns:RPH_Type' use='optional'/>\n" +
        "  </xs:attributeGroup>\n" +
        "  <xs:complexType name='PhonePrefType'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element name='Telephone'>\n" +
        "        <xs:complexType>\n" +
        "          <xs:attributeGroup ref='tns:TelephoneInfoGroup'/>\n" +
        "        </xs:complexType>\n" +
        "      </xs:element>\n" +
        "    </xs:sequence>\n" +
        "  </xs:complexType>\n" +
        "  <xs:complexType name='CommonPrefType'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element name='NamePref' type='xs:string' minOccurs='0' maxOccurs='unbounded'/>\n" +
        "      <xs:element name='PhonePref' type='tns:PhonePrefType' minOccurs='0' maxOccurs='unbounded'/>\n" +
        "    </xs:sequence>\n" +
        "    <xs:attributeGroup ref='tns:PrivacyGroup'/>\n" +
        "  </xs:complexType>\n" +
        "  <xs:complexType name='PreferencesType'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element name='PrefCollection' maxOccurs='unbounded'>\n" +
        "        <xs:complexType>\n" +
        "          <xs:sequence>\n" +
        "            <xs:element name='CommonPref' type='tns:CommonPrefType' minOccurs='0' maxOccurs='unbounded'/>\n" +
        "            <xs:element name='VehicleRentalPref' type='xs:string' minOccurs='0' maxOccurs='unbounded'/>\n" +
        "          </xs:sequence>\n" +
        "          <xs:attributeGroup ref='tns:PrivacyGroup'/>\n" +
        "          <xs:attribute name='TravelPurpose' type='xs:string' use='optional'/>\n" +
        "        </xs:complexType>\n" +
        "      </xs:element>\n" +
        "    </xs:sequence>\n" +
        "    <xs:attributeGroup ref='tns:PrivacyGroup'/>\n" +
        "  </xs:complexType>\n" +
        "  <xs:complexType name='ProfileType'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element name='Accesses' type='xs:string' minOccurs='0'/>\n" +
        "      <xs:element name='PrefCollections' type='tns:PreferencesType' minOccurs='0'/>\n" +
        "    </xs:sequence>\n" +
        "    <xs:attribute name='RPH' type='tns:RPH_Type' use='optional'/>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>";
    
    static final String OTA_PROFILETYPE_SIMPLIFIED_MAIN =
        "<schema targetNamespace='urn:something'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:ins='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <import namespace='urn:anything' schemaLocation='OTA_PROFILETYPE_SIMPLIFIED_INCLUDE'/>\n" +
        "  <element name='element' type='ins:CommonProfileTypeType'/>\n" +
        "</schema>";
    static final String OTA_PROFILETYPE_SIMPLIFIED_INCLUDE =
        "<xs:schema targetNamespace='urn:anything'" +
        "    xmlns:xs='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <xs:complexType name='FormattedTextTextType'>\n" +
        "    <xs:simpleContent>\n" +
        "       <xs:extension base='xs:string'>\n" +
        "         <xs:attribute name='Formatted' type='xs:boolean' use='optional'/>\n" +
        "       </xs:extension>\n" +
        "    </xs:simpleContent>\n" +
        "  </xs:complexType>\n" +
        "  <xs:complexType name='ParagraphType'>\n" +
        "    <xs:choice minOccurs='0' maxOccurs='unbounded'>\n" +
        "      <xs:element name='Text' type='tns:FormattedTextTextType'/>\n" +
        "      <xs:element name='Image' type='xs:string'/>\n" +
        "      <xs:element name='ListItem'>\n" +
        "        <xs:complexType>\n" +
        "          <xs:simpleContent>\n" +
        "            <xs:extension base='tns:FormattedTextTextType'>\n" +
        "              <xs:attribute name='ListItem' type='xs:integer' use='optional'/>\n" +
        "            </xs:extension>\n" +
        "          </xs:simpleContent>\n" +
        "        </xs:complexType>\n" +
        "      </xs:element>\n" +
        "    </xs:choice>\n" +
        "    <xs:attribute name='Name' type='xs:string' use='optional'/>\n" +
        "    <xs:attribute name='ParagraphNumber' type='xs:nonNegativeInteger' use='optional'/>\n" +
        "   </xs:complexType>\n" +
        "   <xs:attributeGroup name='PrivacyGroup'>\n" +
        "     <xs:attribute name='ShareSynchInd' use='optional'>\n" +
        "       <xs:simpleType>\n" +
        "         <xs:restriction base='xs:NMTOKEN'>\n" +
        "           <xs:enumeration value='Yes'/>\n" +
        "           <xs:enumeration value='No'/>\n" +
        "           <xs:enumeration value='Inherit'/>\n" +
        "         </xs:restriction>\n" +
        "       </xs:simpleType>\n" +
        "     </xs:attribute>\n" +
        "     <xs:attribute name='ShareMarketInd' use='optional'>\n" +
        "       <xs:simpleType>\n" +
        "         <xs:restriction base='xs:NMTOKEN'>\n" +
        "           <xs:enumeration value='Yes'/>\n" +
        "           <xs:enumeration value='No'/>\n" +
        "           <xs:enumeration value='Inherit'/>\n" +
        "         </xs:restriction>\n" +
        "       </xs:simpleType>\n" +
        "     </xs:attribute>\n" +
        "  </xs:attributeGroup>\n" +
        "  <xs:attributeGroup name='TelephoneAttributesGroup'>\n" +
        "    <xs:attribute name='CountryAccessCode' type='xs:string' use='optional'/>\n" +
        "    <xs:attribute name='AreaCityCode' type='xs:string' use='optional'/>\n" +
        "    <xs:attribute name='PhoneNumber' type='xs:string' use='required'/>\n" +
        "    <xs:attribute name='Extension' type='xs:string' use='optional'/>\n" +
        "  </xs:attributeGroup>\n" +
        "  <xs:attributeGroup name='TelephoneGroup'>\n" +
        "    <xs:attributeGroup ref='tns:PrivacyGroup'/>\n" +
        "    <xs:attributeGroup ref='tns:TelephoneAttributesGroup'/>\n" +
        "  </xs:attributeGroup>\n" +
        "  <xs:simpleType name='RPH_Type'>\n" +
        "    <xs:restriction base='xs:string'>\n" +
        "      <xs:pattern value='[0-9]{1,8}'/>\n" +
        "    </xs:restriction>\n" +
        "  </xs:simpleType>\n" +
        "  <xs:attributeGroup name='TelephoneInfoGroup'>\n" +
        "    <xs:attributeGroup ref='tns:TelephoneGroup'/>\n" +
        "    <xs:attribute name='RPH' type='tns:RPH_Type' use='optional'/>\n" +
        "  </xs:attributeGroup>\n" +
        "  <xs:complexType name='PhonePrefType'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element name='Telephone'>\n" +
        "        <xs:complexType>\n" +
        "          <xs:attributeGroup ref='tns:TelephoneInfoGroup'/>\n" +
        "        </xs:complexType>\n" +
        "      </xs:element>\n" +
        "    </xs:sequence>\n" +
        "  </xs:complexType>\n" +
        "  <xs:complexType name='CommonPrefType'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element name='NamePref' type='xs:string' minOccurs='0' maxOccurs='unbounded'/>\n" +
        "      <xs:element name='PhonePref' type='tns:PhonePrefType' minOccurs='0' maxOccurs='unbounded'/>\n" +
        "    </xs:sequence>\n" +
        "    <xs:attributeGroup ref='tns:PrivacyGroup'/>\n" +
        "  </xs:complexType>\n" +
        "  <xs:complexType name='PreferencesType'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element name='PrefCollection' maxOccurs='unbounded'>\n" +
        "        <xs:complexType>\n" +
        "          <xs:sequence>\n" +
        "            <xs:element name='CommonPref' type='tns:CommonPrefType' minOccurs='0' maxOccurs='unbounded'/>\n" +
        "            <xs:element name='VehicleRentalPref' type='xs:string' minOccurs='0' maxOccurs='unbounded'/>\n" +
        "          </xs:sequence>\n" +
        "          <xs:attributeGroup ref='tns:PrivacyGroup'/>\n" +
        "          <xs:attribute name='TravelPurpose' type='xs:string' use='optional'/>\n" +
        "        </xs:complexType>\n" +
        "      </xs:element>\n" +
        "    </xs:sequence>\n" +
        "    <xs:attributeGroup ref='tns:PrivacyGroup'/>\n" +
        "  </xs:complexType>\n" +
        "  <xs:complexType name='CommonProfileTypeType'>\n" +
        "    <xs:sequence>\n" +
        "      <xs:element name='Accesses' type='xs:string' minOccurs='0'/>\n" +
        "      <xs:element name='PrefCollections' type='tns:PreferencesType' minOccurs='0'/>\n" +
        "    </xs:sequence>\n" +
        "    <xs:attribute name='RPH' type='tns:RPH_Type' use='optional'/>\n" +
        "  </xs:complexType>\n" +
        "</xs:schema>";
    static final String OTA_PROFILETYPE_SIMPLIFIED_CUSTOMIZATION_A =
        "<schema-set enumeration-type='simple'>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      prefer-inline='true'/>\n" +
        "</schema-set>";
    static final String OTA_PROFILETYPE_SIMPLIFIED_CUSTOMIZATION_B =
        "<schema-set enumeration-type='simple' repeated-type='list'>\n" +
        "  <schema xmlns:tns='urn:anything' xmlns:xs='http://www.w3.org/2001/XMLSchema' namespace='urn:anything'\n" +
        "      generate-all='false' prefer-inline='true'>\n" +
        "    <name-converter strip-prefixes='Common' strip-suffixes='Type Group'/>\n" +
        "  </schema>\n" +
        "</schema-set>";
    
    private static final Map MAP = new HashMap();
    static final TestResolver RESOLVER1 = new TestResolver(MAIN_SCHEMA1, "MAIN_SCHEMA1", MAP);
    static final TestResolver RESOLVER2 = new TestResolver(MAIN_SCHEMA2, "MAIN_SCHEMA2", MAP);
    static final TestResolver RESOLVER3 = new TestResolver(MAIN_SCHEMA3, "MAIN_SCHEMA3", MAP);
    static final TestResolver OTA_PROFILETYPE_SIMPLIFIED_UNQUALIFIED_RESOLVER =
        new TestResolver(OTA_PROFILETYPE_SIMPLIFIED_MAIN_UNQUALIFIED, "OTA_PROFILETYPE_SIMPLIFIED_MAIN_UNQUALIFIED",
        MAP);
    static final TestResolver OTA_PROFILETYPE_SIMPLIFIED_RESOLVER =
        new TestResolver(OTA_PROFILETYPE_SIMPLIFIED_MAIN, "OTA_PROFILETYPE_SIMPLIFIED_MAIN", MAP);
    static {
        MAP.put(RESOLVER1.getName(), RESOLVER1);
        MAP.put("INCLUDED_SCHEMA1", new TestResolver(INCLUDED_SCHEMA1, "INCLUDED_SCHEMA1", MAP));
        MAP.put(RESOLVER2.getName(), RESOLVER2);
        MAP.put("INCLUDED_SCHEMA2", new TestResolver(INCLUDED_SCHEMA2, "INCLUDED_SCHEMA2", MAP));
        MAP.put(RESOLVER3.getName(), RESOLVER3);
        MAP.put("INCLUDED_SCHEMA3", new TestResolver(INCLUDED_SCHEMA3, "INCLUDED_SCHEMA3", MAP));
        MAP.put(OTA_PROFILETYPE_SIMPLIFIED_UNQUALIFIED_RESOLVER.getName(),
            OTA_PROFILETYPE_SIMPLIFIED_UNQUALIFIED_RESOLVER);
        MAP.put("OTA_PROFILETYPE_SIMPLIFIED_INCLUDE_UNQUALIFIED",
            new TestResolver(OTA_PROFILETYPE_SIMPLIFIED_INCLUDE_UNQUALIFIED,
            "OTA_PROFILETYPE_SIMPLIFIED_INCLUDE_UNQUALIFIED", MAP));
        MAP.put(OTA_PROFILETYPE_SIMPLIFIED_RESOLVER.getName(), OTA_PROFILETYPE_SIMPLIFIED_RESOLVER);
        MAP.put("OTA_PROFILETYPE_SIMPLIFIED_INCLUDE",
            new TestResolver(OTA_PROFILETYPE_SIMPLIFIED_INCLUDE, "OTA_PROFILETYPE_SIMPLIFIED_INCLUDE", MAP));
    }
}

package org.jibx.schema.elements;

import org.jibx.schema.SchemaTestBase;

/**
 * Test handling of schemas with simple group and attributeGroup components.
 */
public class SimpleGroups extends SchemaTestBase
{
    public static final String GROUP_EMPTY_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <group name='simple'>\n" +
        "    <sequence/>\n" +
        "  </group>\n" +
        "</schema>";
    
    public static final String ATTRIBUTEGROUP_EMPTY_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <attributeGroup name='simple'/>\n" +
        "</schema>";
    
    public static final String GROUP_SIMPLE_SCHEMA1 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <group name='simple'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "    </sequence>\n" +
        "  </group>\n" +
        "</schema>";
    
    public static final String GROUP_SIMPLE_SCHEMA2 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <group name='simple'>\n" +
        "    <choice>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "    </choice>\n" +
        "  </group>\n" +
        "</schema>";
    
    public static final String GROUP_SIMPLE_SCHEMA3 =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <group name='simple'>\n" +
        "    <all>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "    </all>\n" +
        "  </group>\n" +
        "</schema>";
    
    public static final String ATTRIBUTEGROUP_SIMPLE_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <attributeGroup name='simple'>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "    <attribute name='registered' default='true' type='boolean'/>\n" +
        "  </attributeGroup>\n" +
        "</schema>";
    
    public static final String GROUP_REF_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <group name='group'>\n" +
        "    <choice>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "    </choice>\n" +
        "  </group>\n" +
        "  <complexType name='simple'>\n" +
        "    <sequence>\n" +
        "      <element name='real' type='boolean'/>\n" +
        "      <group ref='tns:group'/>\n" +
        "      <element name='local' type='string'/>\n" +
        "    </sequence>\n" +
        "  </complexType>\n" +
        "</schema>";
    
    public static final String ATTRIBUTEGROUP_REF_SCHEMA =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    xmlns:tns='urn:anything'" +
        "    elementFormDefault='qualified'>\n" +
        "  <attributeGroup name='group'>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "    <attribute name='registered' default='true' type='boolean'/>\n" +
        "  </attributeGroup>\n" +
        "  <complexType name='simple'>\n" +
        "    <sequence>\n" +
        "      <element name='name' minOccurs='1' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "    </sequence>\n" +
        "    <attributeGroup ref='tns:group'/>\n" +
        "  </complexType>\n" +
        "</schema>";
    
    public void testGroupEmpty() throws Exception {
        runNoErrors(GROUP_EMPTY_SCHEMA);
    }
    
    public void testAttributeGroupEmpty() throws Exception {
        runNoErrors(ATTRIBUTEGROUP_EMPTY_SCHEMA);
    }
    
    public void testGroupSimpleSchema1() throws Exception {
        runNoErrors(GROUP_SIMPLE_SCHEMA1);
    }
    
    public void testGroupSimpleSchema2() throws Exception {
        runNoErrors(GROUP_SIMPLE_SCHEMA2);
    }
    
    public void testGroupSimpleSchema3() throws Exception {
        runNoErrors(GROUP_SIMPLE_SCHEMA3);
    }
    
    public void testAttributeGroupSimple() throws Exception {
        runNoErrors(ATTRIBUTEGROUP_SIMPLE_SCHEMA);
    }
    
    public void testGroupRef() throws Exception {
        runNoErrors(GROUP_REF_SCHEMA);
    }
    
    public void testAttributeGroupRef() throws Exception {
        runNoErrors(ATTRIBUTEGROUP_REF_SCHEMA);
    }
}
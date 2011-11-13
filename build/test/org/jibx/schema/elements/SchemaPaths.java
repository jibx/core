
package org.jibx.schema.elements;

import org.jibx.schema.SchemaTestBase;

/**
 * Test handling of schemas paths.
 */
public class SchemaPaths extends SchemaTestBase
{
    public static final String TEST_SCHEMA =
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
        "  <complexType name='complex'>\n" +
        "    <sequence>\n" +
        "      <element name='name' type='string'/>\n" +
        "      <element name='age' type='int'/>\n" +
        "      <element name='quality'>\n" +
        "        <simpleType>\n" +
        "          <restriction base='token'>\n" +
        "            <enumeration value='poor'/>\n" +
        "            <enumeration value='fair'/>\n" +
        "            <enumeration value='good'/>\n" +
        "            <enumeration value='excellent'/>\n" +
        "          </restriction>\n" +
        "        </simpleType>\n" +
        "      </element>\n" +
        "    </sequence>\n" +
        "    <attribute name='male' use='required' type='boolean'/>\n" +
        "    <attribute name='registered' default='true' type='boolean'/>\n" +
        "  </complexType>\n" +
        "  <element name='complex' type='tns:complex'/>\n" +
        "</schema>";
    
    private void verifyNoProblems() {
        assertFalse(getProblemText(m_validationContext), hasProblem(m_validationContext));
    }
    
    public void testDirectSpecification() throws Exception {
        SchemaElement schema = prepareSchema(TEST_SCHEMA);
        SchemaPath path = SchemaPath.buildPath(null, "complexType", "simple", null, this, m_validationContext);
        verifyNoProblems();
        OpenAttrBase match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ComplexTypeElement);
        path = SchemaPath.buildPath(null, "complexType", null, "1", this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ComplexTypeElement);
        path = SchemaPath.buildPath(null, "complexType", "simple", "1", this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ComplexTypeElement);
    }
    
    public void testDirectPath() throws Exception {
        SchemaElement schema = prepareSchema(TEST_SCHEMA);
        SchemaPath path = SchemaPath.buildPath("complexType[@name=simple]", "complexType", null, null, this,
            m_validationContext);
        verifyNoProblems();
        OpenAttrBase match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ComplexTypeElement);
        path = SchemaPath.buildPath("complexType[1]", "complexType", null, null, this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ComplexTypeElement);
        path = SchemaPath.buildPath("complexType[@name=simple][1]", "complexType", null, null, this,
            m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ComplexTypeElement);
        path = SchemaPath.buildPath("[@name=simple][1]", "complexType", null, null, this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ComplexTypeElement);
    }
    
    public void testPathAndSpecification() throws Exception {
        SchemaElement schema = prepareSchema(TEST_SCHEMA);
        SchemaPath path = SchemaPath.buildPath("complexType[@name=complex]/", "attribute", "male", null, this,
            m_validationContext);
        verifyNoProblems();
        OpenAttrBase match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched",
            match instanceof AttributeElement && ((AttributeElement)match).getName().equals("male"));
        path = SchemaPath.buildPath("complexType[1]/", "attribute", null, "2", this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched",
            match instanceof AttributeElement && ((AttributeElement)match).getName().equals("registered"));
        path = SchemaPath.buildPath("complexType[2]/", "sequence", null, null, this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof SequenceElement);
    }
    
    public void testWildcardPath() throws Exception {
        SchemaElement schema = prepareSchema(TEST_SCHEMA);
        SchemaPath path = SchemaPath.buildPath("complexType[@name=complex]/*/element[@name=age]", "element", null, null,
            this, m_validationContext);
        verifyNoProblems();
        OpenAttrBase match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched",
            match instanceof ElementElement && ((ElementElement)match).getName().equals("age"));
        path = SchemaPath.buildPath("complexType[1]/*/element[3]", "element", null, null, this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ElementElement && ((ElementElement)match).getName() == null);
        path = SchemaPath.buildPath("complexType[1]/*/[3]", "element", null, null, this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ElementElement && ((ElementElement)match).getName() == null);
        path = SchemaPath.buildPath("complexType[1]/**/[3]", "element", null, null, this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof ElementElement && ((ElementElement)match).getName() == null);
        path = SchemaPath.buildPath("complexType[2]/**/[3]", "enumeration", null, null, this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        verifyNoProblems();
        assertTrue("Path not matched", match instanceof FacetElement.Enumeration &&
            ((FacetElement.Enumeration)match).getValue().equals("good"));
    }
    
    public void testErrors() throws Exception {
        SchemaElement schema = prepareSchema(TEST_SCHEMA);
        SchemaPath path = SchemaPath.buildPath("complexType[1][1]/", "element", null, null, this, m_validationContext);
        assertTrue("Multiple position predicates on path step", hasProblem(m_validationContext));
        m_validationContext.getProblems().clear();
        path = SchemaPath.buildPath("complexType[1][@name=xxx]/", "element", null, null, this, m_validationContext);
        assertTrue("Predicates in wrong order on path step", hasProblem(m_validationContext));
        m_validationContext.getProblems().clear();
        path = SchemaPath.buildPath("complexType[1]/*/[3]", "element", null, "2", this, m_validationContext);
        assertTrue("Position predicate mismatch with specified value", hasProblem(m_validationContext));
        m_validationContext.getProblems().clear();
        path = SchemaPath.buildPath("complexType[1]/*/[@name=xxx]", "element", "yyy", null, this, m_validationContext);
        assertTrue("Name predicate mismatch with specified value", hasProblem(m_validationContext));
        m_validationContext.getProblems().clear();
        path = SchemaPath.buildPath("complexType[1]/**/", "element", null, null, this, m_validationContext);
        verifyNoProblems();
        OpenAttrBase match = path.matchUnique(schema);
        assertTrue("Multiple matches for path expression", hasProblem(m_validationContext));
        m_validationContext.getProblems().clear();
        assertNull("Multiple matches for path expression", match);
        path = SchemaPath.buildPath("complexType[2]/**/[7]", "enumeration", null, null, this, m_validationContext);
        verifyNoProblems();
        match = path.matchUnique(schema);
        assertTrue("No match for path expression", hasProblem(m_validationContext));
        m_validationContext.getProblems().clear();
        assertNull("No match for path expression", match);
    }
}
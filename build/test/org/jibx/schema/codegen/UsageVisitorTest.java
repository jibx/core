
package org.jibx.schema.codegen;

import org.jibx.runtime.QName;
import org.jibx.schema.SchemaTestBase;
import org.jibx.schema.UsageFinder;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.util.ReferenceCountMap;

/**
 * Test usage visitor operation for counting references.
 */
public class UsageVisitorTest extends SchemaTestBase
{
    public static final String REFERENCE_SCHEMA =
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
    
    private void checkAttributeUsage(String name, ReferenceCountMap map, int count) {
        Object key = m_nameRegister.findAttribute(new QName("urn:anything", name));
        assertEquals("Usage count error on attribute '" + name + '\'', count, map.getCount(key));
    }
    
    private void checkElementUsage(String name, ReferenceCountMap map, int count) {
        Object key = m_nameRegister.findElement(new QName("urn:anything", name));
        assertEquals("Usage count error on element '" + name + '\'', count, map.getCount(key));
    }
    
    private void checkTypeUsage(String name, ReferenceCountMap map, int count) {
        Object key = m_nameRegister.findType(new QName("urn:anything", name));
        assertEquals("Usage count error on type '" + name + '\'', count, map.getCount(key));
    }
    
    public void testComplexReferenced() throws Exception {
        SchemaElement root = runNoErrors(REFERENCE_SCHEMA);
        if (!hasProblem(m_validationContext)) {
            UsageFinder usage = new UsageFinder();
            usage.countSchemaTree(root);
            ReferenceCountMap map = usage.getUsageMap();
            checkTypeUsage("simple1", map, 1);
            checkElementUsage("simple1", map, 1);
            checkTypeUsage("simple2", map, 0);
            checkTypeUsage("simple3", map, 0);
            checkTypeUsage("rating", map, 3);
            checkTypeUsage("mixedUnion", map, 1);
        }
    }
}
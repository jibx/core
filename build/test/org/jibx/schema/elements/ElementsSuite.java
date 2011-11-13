package org.jibx.schema.elements;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ElementsSuite extends TestCase
{
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(EmptySchema.class);
        suite.addTestSuite(Annotations.class);
        suite.addTestSuite(SimpleGlobals.class);
        suite.addTestSuite(SimpleComplexTypes.class);
        suite.addTestSuite(SimpleSimpleTypes.class);
        suite.addTestSuite(SimpleGroups.class);
        return suite;
    }
}

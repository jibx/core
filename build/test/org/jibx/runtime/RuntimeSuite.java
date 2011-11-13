package org.jibx.runtime;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RuntimeSuite extends TestCase
{
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(UtilityTest.class);
        suite.addTestSuite(WhitespaceConversionsTest.class);
        suite.addTestSuite(JodaConvertTest.class);
        suite.addTestSuite(JiBXConstrainedParseExceptionTest.class);
        return suite;
    }
}

package org.jibx.ws.wsdl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class WsdlSuite extends TestCase
{
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(SignatureParserTest.class);
        suite.addTestSuite(ServiceCustomTest.class);
        return suite;
    }
}

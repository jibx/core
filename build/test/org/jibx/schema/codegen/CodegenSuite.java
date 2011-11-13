package org.jibx.schema.codegen;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class CodegenSuite extends TestCase
{
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(UsageVisitorTest.class);
        suite.addTestSuite(TypeReplacementTest.class);
        suite.addTestSuite(CodeGenerationTest.class);
        return suite;
    }
}

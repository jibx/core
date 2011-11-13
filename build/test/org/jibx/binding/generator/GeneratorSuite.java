package org.jibx.binding.generator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class GeneratorSuite extends TestCase
{
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(BindingGeneratorCommandLineTest.class);
        suite.addTestSuite(ClassCustomTest.class);
        suite.addTestSuite(GeneratorTest.class);
        suite.addTestSuite(NestingBaseTest.class);
        return suite;
    }
}

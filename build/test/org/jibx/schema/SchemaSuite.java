package org.jibx.schema;

import org.jibx.schema.codegen.CodegenSuite;
import org.jibx.schema.elements.ElementsSuite;
import org.jibx.schema.elements.SchemaPaths;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SchemaSuite extends TestCase
{
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(ElementsSuite.suite());
        suite.addTestSuite(SchemaPaths.class);
        suite.addTest(CodegenSuite.suite());
        return suite;
    }
}

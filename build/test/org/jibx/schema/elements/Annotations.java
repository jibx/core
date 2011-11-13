
package org.jibx.schema.elements;

import org.jibx.schema.SchemaTestBase;

/**
 * Test handling of simple schemas with annotations.
 */
public class Annotations extends SchemaTestBase
{
    public static final String EMPTY_ANNOTATION =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <annotation/>\n" +
        "</schema>";

    public static final String DOCUMENTATION_ANNOTATION =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <annotation>\n" +
        "    <documentation source='urn:a' xml:lang='en'>\n" +
        "      this is <b xmlns='urn:b'>all</b> good" +
        "    </documentation>\n" +
        "  </annotation>\n" +
        "</schema>";

    public static final String APPINFO_ANNOTATION =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <annotation>\n" +
        "    <appinfo>\n" +
        "      <j:jibx xmlns:j='http://www.jibx.org'>special good stuff</j:jibx>\n" +
        "    </appinfo>\n" +
        "  </annotation>\n" +
        "</schema>";

    public static final String MULTIPLE_ANNOTATION =
        "<schema targetNamespace='urn:anything'" +
        "    xmlns='http://www.w3.org/2001/XMLSchema'" +
        "    elementFormDefault='qualified'>\n" +
        "  <annotation>\n" +
        "    <appinfo>\n" +
        "      <j:jibx xmlns:j='http://www.jibx.org'>special good stuff</j:jibx>\n" +
        "    </appinfo>\n" +
        "    <documentation>\n" +
        "      this is <b xmlns='urn:b'>all</b> good" +
        "    </documentation>\n" +
        "    <documentation xml:lang='en'>\n" +
        "      this is <b xmlns='urn:b'>all</b> good" +
        "    </documentation>\n" +
        "    <appinfo>\n" +
        "      <j:jibx xmlns:j='http://www.jibx.org' source='http://www.jibx.org'>\n" +
        "        more good stuff" +
        "      </j:jibx>\n" +
        "    </appinfo>\n" +
        "    <appinfo>\n" +
        "      <j:jibx xmlns:j='http://www.jibx.org'>last good stuff</j:jibx>\n" +
        "    </appinfo>\n" +
        "  </annotation>\n" +
        "</schema>";

    public void testEmptyAnnotation() throws Exception {
        runNoErrors(EMPTY_ANNOTATION);
    }
    
    public void testDocumentationAnnotation() throws Exception {
        runNoErrors(DOCUMENTATION_ANNOTATION);
    }
    
    public void testAppInfoAnnotation() throws Exception {
        runNoErrors(APPINFO_ANNOTATION);
    }
    
    public void testMultipleAnnotation() throws Exception {
        runNoErrors(MULTIPLE_ANNOTATION);
    }
}
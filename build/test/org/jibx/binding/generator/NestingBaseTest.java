/*
Copyright (c) 2007, Dennis M. Sosnoski
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jibx.binding.generator;

import org.jibx.custom.classes.CustomBase;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.custom.classes.NestingBase;
import org.jibx.custom.classes.PackageCustom;


/**
 * Test code for nested property handling.
 */
public class NestingBaseTest extends CustomizationTestBase
{
    public static final String SIMPLE_CUSTOM =
        "<custom property-access='true' require='primitives'/>";
    
    public static final String SIMPLE_PACKAGE =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package property-access='false' name='org.jibx.binding'\n" +
        "      namespace='urn:binding'/>\n" +
        "</custom>";
    
    public static final String NESTED_PACKAGE =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package property-access='false' name='org.jibx.binding'\n" +
        "      namespace='urn:binding'>\n" +
        "    <package property-access='true' name='generator'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    public static final String MULTIPLE_PACKAGE =
        "<custom property-access='true' force-classes='true'\n" +
        "    namespace-style='fixed'>\n" +
        "  <package property-access='false' name='org.jibx.binding'>\n" +
        "    <package property-access='true' name='generator'\n" +
        "        namespace='urn:binding.generator'/>\n" +
        "  </package>\n" +
        "  <package name='org.jibx.runtime' namespace='urn:runtime'/>\n" +
        "  <package property-access='false' name='org.jibx.extras'\n" +
        "    namespace-style='none'/>\n" +
        "</custom>";
    
    public static final String MULTIPLE_PACKAGE_HYPHENATED =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package name-style='hyphenated' name='org.jibx.binding'>\n" +
        "    <package property-access='true' name='generator'\n" +
        "        namespace='urn:binding.generator'/>\n" +
        "  </package>\n" +
        "  <package name='org.jibx.runtime' namespace='urn:runtime'/>\n" +
        "  <package property-access='false' name='org.jibx.extras'/>\n" +
        "</custom>";
    
    public static final String MULTIPLE_PACKAGE_UPPERCAMELCASE =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package name-style='upper-camel-case' name='org.jibx.binding'>\n" +
        "    <package property-access='true' name='generator'\n" +
        "        namespace='urn:binding.generator'/>\n" +
        "  </package>\n" +
        "  <package name='org.jibx.runtime' namespace='urn:runtime'/>\n" +
        "  <package property-access='false' name='org.jibx.extras'/>\n" +
        "</custom>";
    
    public static final String MULTIPLE_PACKAGE_DOTTED =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package name-style='dotted' name='org.jibx.binding'>\n" +
        "    <package property-access='true' name='generator'\n" +
        "        namespace='urn:binding.generator'/>\n" +
        "  </package>\n" +
        "  <package name='org.jibx.runtime' namespace='urn:runtime'/>\n" +
        "  <package property-access='false' name='org.jibx.extras'/>\n" +
        "</custom>";
    
    public static final String MULTIPLE_PACKAGE_UNDERSCORED =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package name-style='underscored' name='org.jibx.binding'>\n" +
        "    <package property-access='true' name='generator'\n" +
        "        namespace='urn:binding.generator'/>\n" +
        "  </package>\n" +
        "  <package name='org.jibx.runtime' namespace='urn:runtime'/>\n" +
        "  <package property-access='false' name='org.jibx.extras'/>\n" +
        "</custom>";
    
    public void testSimpleCustom() throws Exception {
        GlobalCustom custom = readCustom(SIMPLE_CUSTOM);
        assertFalse("add-constructors default", custom.isAddConstructors());
        assertFalse("force-classes default", custom.isForceClasses());
        assertTrue("input default", custom.isInput());
        assertTrue("output default", custom.isOutput());
        assertTrue("map-abstract default", custom.isMapAbstract());
        assertEquals("name style default", CustomBase.CAMEL_CASE_NAMES, custom.getNameStyle());
        assertEquals("value style default", NestingBase.ATTRIBUTE_VALUE_STYLE, custom.getValueStyle("int"));
        assertFalse("require setting", custom.isObjectRequired("java.lang.String"));
        assertTrue("require setting", custom.isPrimitiveRequired("int"));
        assertTrue("property-access setting", custom.isPropertyAccess());
        assertEquals("get root", custom, custom.getGlobal());
        assertNull("empty namespace",custom.getNamespace());
    }
    
    public void testSimplePackage() throws Exception {
        GlobalCustom custom = readCustom(SIMPLE_PACKAGE);
        PackageCustom pack = custom.getPackage("org.jibx.binding");
        assertTrue("map-abstract default", pack.isMapAbstract());
        assertEquals("name style default", CustomBase.CAMEL_CASE_NAMES, pack.getNameStyle());
        assertEquals("value style default", NestingBase.ATTRIBUTE_VALUE_STYLE, pack.getValueStyle("int"));
        assertFalse("require setting", pack.isObjectRequired("java.lang.String"));
        assertTrue("require setting", pack.isPrimitiveRequired("int"));
        assertFalse("property-access setting", pack.isPropertyAccess());
        assertEquals("get root", custom, pack.getGlobal());
        assertEquals("package namespace", "urn:binding", pack.getNamespace());
    }
    
    public void testNestedPackage() throws Exception {
        GlobalCustom custom = readCustom(NESTED_PACKAGE);
        PackageCustom pack = custom.getPackage("org.jibx.binding.generator");
        assertTrue("map-abstract default", pack.isMapAbstract());
        assertEquals("name style default", CustomBase.CAMEL_CASE_NAMES, pack.getNameStyle());
        assertEquals("value style default", NestingBase.ATTRIBUTE_VALUE_STYLE, pack.getValueStyle("int"));
        assertFalse("require setting", pack.isObjectRequired("java.lang.String"));
        assertTrue("require setting", pack.isPrimitiveRequired("int"));
        assertTrue("property-access setting", pack.isPropertyAccess());
        assertEquals("get root", custom, pack.getGlobal());
        assertEquals("package namespace", "urn:binding/generator", pack.getNamespace());
    }
    
    public void testMultiplePackage() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PACKAGE);
        PackageCustom pack = custom.getPackage("org.jibx.binding.generator");
        assertTrue("property-access setting", pack.isPropertyAccess());
        assertEquals("package namespace", "urn:binding.generator", pack.getNamespace());
        pack = custom.getPackage("org.jibx.runtime");
        assertTrue("property-access setting", pack.isPropertyAccess());
        assertEquals("package namespace", "urn:runtime", pack.getNamespace());
        pack = custom.getPackage("org.jibx.extras");
        assertFalse("property-access setting", pack.isPropertyAccess());
        assertNull("package namespace", pack.getNamespace());
        assertEquals("simple name", "a", pack.convertName("a"));
        assertEquals("simple name", "a", pack.convertName("A"));
        assertEquals("simple name", "aName", pack.convertName("aName"));
        assertEquals("leading underscore name", "aName",
            pack.convertName("__aName"));
        assertEquals("embedded underscores name", "aName",
            pack.convertName("a__Name"));
        assertEquals("complex name", "aBCDefgH",
            pack.convertName("aB_cDefgH"));
    }
    
    public void testHyphenated() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PACKAGE_HYPHENATED);
        PackageCustom pack = custom.getPackage("org.jibx.binding.generator");
        assertEquals("simple name", "a", pack.convertName("a"));
        assertEquals("simple name", "a", pack.convertName("A"));
        assertEquals("simple name", "a-name", pack.convertName("aName"));
        assertEquals("leading underscore name", "a-name",
            pack.convertName("__aName"));
        assertEquals("embedded underscores name", "a-name",
            pack.convertName("a__Name"));
        assertEquals("complex name", "a-b-c-defg-h",
            pack.convertName("aB_cDefgH"));
    }
    
    public void testUpperCamelCase() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PACKAGE_UPPERCAMELCASE);
        PackageCustom pack = custom.getPackage("org.jibx.binding.generator");
        assertEquals("simple name", "A", pack.convertName("a"));
        assertEquals("simple name", "A", pack.convertName("A"));
        assertEquals("simple name", "AName", pack.convertName("aName"));
        assertEquals("leading underscore name", "AName",
            pack.convertName("__aName"));
        assertEquals("embedded underscores name", "AName",
            pack.convertName("a__Name"));
        assertEquals("complex name", "ABCDefgH",
            pack.convertName("aB_cDefgH"));
    }
    
    public void testDotted() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PACKAGE_DOTTED);
        PackageCustom pack = custom.getPackage("org.jibx.binding.generator");
        assertEquals("simple name", "a", pack.convertName("a"));
        assertEquals("simple name", "a", pack.convertName("A"));
        assertEquals("simple name", "a.name", pack.convertName("aName"));
        assertEquals("leading underscore name", "a.name",
            pack.convertName("__aName"));
        assertEquals("embedded underscores name", "a.name",
            pack.convertName("a__Name"));
        assertEquals("complex name", "a.b.c.defg.h",
            pack.convertName("aB_cDefgH"));
    }
    
    public void testUnderscored() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PACKAGE_UNDERSCORED);
        PackageCustom pack = custom.getPackage("org.jibx.binding.generator");
        assertEquals("simple name", "a", pack.convertName("a"));
        assertEquals("simple name", "a", pack.convertName("A"));
        assertEquals("simple name", "a_name", pack.convertName("aName"));
        assertEquals("leading underscore name", "a_name",
            pack.convertName("__aName"));
        assertEquals("embedded underscores name", "a_name",
            pack.convertName("a__Name"));
        assertEquals("complex name", "a_b_c_defg_h",
            pack.convertName("aB_cDefgH"));
    }
}
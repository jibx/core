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

import java.util.Collection;

import org.jibx.custom.classes.ClassCustom;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.custom.classes.ValueCustom;

/**
 * Test code for class handling.
 */
public class ClassCustomTest extends CustomizationTestBase
{
    public static final String SIMPLE_PROPERTIES_CLASS =
        "<custom property-access='false' force-classes='true'>\n" +
        "  <package property-access='true' name='org.jibx.binding'>\n" +
        "    <class name='generator.DataClass1'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    public static final String MULTIPLE_FIELDS_CLASSES =
        "<custom property-access='false' force-classes='true'\n" +
        "    strip-prefixes='m_ s_'>\n" +
        "  <package name-style='camel-case' name='org.jibx.binding'\n" +
        "      require='none'>\n" +
        "    <class name='generator.DataClass2' require='all'/>\n" +
        "    <class name='generator.DataClass1' optionals='int' requireds='string'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    public static final String MULTIPLE_PROPERTIES_CLASSES =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package name-style='camel-case' name='org.jibx.binding'\n" +
        "      require='all'>\n" +
        "    <class name='generator.DataClass2'\n" +
        "        strip-prefixes='s_ m_' excludes='transient static'>\n" +
        "      <value field='m_dataClass1s' required='false'\n" +
        "          item-type='org.jibx.binding.generator.DataClass1'/>\n" +
        "    </class>\n" +
        "    <class name='generator.DataClass1' optionals='string linked int'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    public static final String NONSTANDARD_PROPERTIES_CLASSES =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package name-style='camel-case' name='org.jibx.binding'\n" +
        "      require='all'>\n" +
        "    <class name='generator.DataClass2'\n" +
        "        strip-prefixes='s_ m_' excludes='transient static'>\n" +
        "      <value field='m_dataClass1s' required='false'\n" +
        "          item-type='org.jibx.binding.generator.DataClass1'/>\n" +
        "      <value get-method='getValue' set-method='setNonstandardValue' required='false'/>\n" +
        "    </class>\n" +
        "    <class name='generator.DataClass1' optionals='string linked int'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    public void testSimplePropertiesClass() throws Exception {
        GlobalCustom custom = readCustom(SIMPLE_PROPERTIES_CLASS);
        ClassCustom clas = custom.getClassCustomization("org.jibx.binding.generator.DataClass1");
        assertTrue("property-access setting", clas.isPropertyAccess());
        Collection members = clas.getMembers();
        assertEquals("property count", 4, members.size());
        ValueCustom member = clas.getMember("boolean");
        assertNotNull("boolean member", member);
        assertEquals("boolean type", "boolean", member.getWorkingType());
        assertEquals("boolean name", "boolean", member.getXmlName());
        assertTrue("boolean required", member.isRequired());
        member = clas.getMember("int");
        assertNotNull("int member", member);
        assertEquals("int type", "int", member.getWorkingType());
        assertEquals("int name", "int", member.getXmlName());
        assertTrue("int required", member.isRequired());
        member = clas.getMember("linked");
        assertNotNull("linked member", member);
        assertEquals("linked type", "org.jibx.binding.generator.DataClass1", member.getWorkingType());
        assertEquals("linked name", "linked", member.getXmlName());
        assertFalse("linked required", member.isRequired());
        member = clas.getMember("string");
        assertNotNull("string member", member);
        assertEquals("string type", "java.lang.String", member.getWorkingType());
        assertEquals("string name", "string", member.getXmlName());
        assertFalse("string required", member.isRequired());
    }
    
    public void testMultipleFieldsClasses() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_FIELDS_CLASSES);
        ClassCustom clas = custom.getClassCustomization("org.jibx.binding.generator.DataClass2");
        Collection members = clas.getMembers();
        assertEquals("property count", 1, members.size());
        ValueCustom member = clas.getMember("dataClass1s");
        assertNotNull("dataClass1s member", member);
        assertEquals("dataClass1s type", "java.util.List", member.getWorkingType());
        assertEquals("dataClass1s name", "dataClass1s", member.getXmlName());
        assertTrue("dataClass1s collection", member.isCollection());
        assertEquals("dataClass1s type", "java.lang.Object", member.getItemType());
        assertEquals("dataClass1s name", "dataClass1", member.getItemName());
        clas = custom.getClassCustomization("org.jibx.binding.generator.DataClass1");
        members = clas.getMembers();
        assertEquals("property count", 4, members.size());
        member = clas.getMember("boolean");
        assertNotNull("boolean member", member);
        assertEquals("boolean type", "boolean", member.getWorkingType());
        assertEquals("boolean name", "boolean", member.getXmlName());
        assertFalse("boolean required", member.isRequired());
        member = clas.getMember("int");
        assertNotNull("int member", member);
        assertEquals("int type", "int", member.getWorkingType());
        assertEquals("int name", "int", member.getXmlName());
        assertFalse("int required", member.isRequired());
        member = clas.getMember("linked");
        assertNotNull("linked member", member);
        assertEquals("linked type", "org.jibx.binding.generator.DataClass1", member.getWorkingType());
        assertEquals("linked name", "linked", member.getXmlName());
        assertFalse("linked required", member.isRequired());
        member = clas.getMember("string");
        assertNotNull("string member", member);
        assertEquals("string type", "java.lang.String", member.getWorkingType());
        assertEquals("string name", "string", member.getXmlName());
        assertTrue("string required", member.isRequired());
    }
    
    public void testMultiplePropertiesClasses() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PROPERTIES_CLASSES);
        ClassCustom clas = custom.getClassCustomization("org.jibx.binding.generator.DataClass2");
        Collection members = clas.getMembers();
        assertEquals("property count", 1, members.size());
        ValueCustom member = clas.getMember("dataClass1s");
        assertNotNull("dataClass1s member", member);
        assertEquals("dataClass1s type", "java.util.List", member.getWorkingType());
        assertEquals("dataClass1s name", "dataClass1s", member.getXmlName());
        assertFalse("dataClass1s required", member.isRequired());
        assertTrue("dataClass1s collection", member.isCollection());
        assertEquals("dataClass1s type", "org.jibx.binding.generator.DataClass1", member.getItemType());
        assertEquals("dataClass1s name", "dataClass1", member.getItemName());
        clas = custom.getClassCustomization("org.jibx.binding.generator.DataClass1");
        members = clas.getMembers();
        assertEquals("property count", 4, members.size());
        member = clas.getMember("boolean");
        assertNotNull("boolean member", member);
        assertEquals("boolean type", "boolean", member.getWorkingType());
        assertEquals("boolean name", "boolean", member.getXmlName());
        assertTrue("boolean required", member.isRequired());
        member = clas.getMember("int");
        assertNotNull("int member", member);
        assertEquals("int type", "int", member.getWorkingType());
        assertEquals("int name", "int", member.getXmlName());
        assertFalse("int required", member.isRequired());
        member = clas.getMember("linked");
        assertNotNull("linked member", member);
        assertEquals("linked type", "org.jibx.binding.generator.DataClass1", member.getWorkingType());
        assertEquals("linked name", "linked", member.getXmlName());
        assertFalse("linked required", member.isRequired());
        member = clas.getMember("string");
        assertNotNull("string member", member);
        assertEquals("string type", "java.lang.String", member.getWorkingType());
        assertEquals("string name", "string", member.getXmlName());
        assertFalse("string required", member.isRequired());
    }
    
    public void testNonstandardPropertiesClasses() throws Exception {
        GlobalCustom custom = readCustom(NONSTANDARD_PROPERTIES_CLASSES);
        ClassCustom clas = custom.getClassCustomization("org.jibx.binding.generator.DataClass2");
        Collection members = clas.getMembers();
        assertEquals("property count", 2, members.size());
        ValueCustom member = clas.getMember("dataClass1s");
        assertNotNull("dataClass1s member", member);
        assertEquals("dataClass1s type", "java.util.List", member.getWorkingType());
        assertEquals("dataClass1s name", "dataClass1s", member.getXmlName());
        assertFalse("dataClass1s required", member.isRequired());
        assertTrue("dataClass1s collection", member.isCollection());
        assertEquals("dataClass1s type", "org.jibx.binding.generator.DataClass1", member.getItemType());
        assertEquals("dataClass1s name", "dataClass1", member.getItemName());
        assertEquals("property count", 2, members.size());
        member = clas.getMember("nonstandardValue");
        assertNotNull("value member", member);
        assertEquals("value type", "int", member.getWorkingType());
        assertEquals("value name", "nonstandardValue", member.getXmlName());
        assertFalse("value required", member.isRequired());
        assertFalse("value collection", member.isCollection());
    }
}
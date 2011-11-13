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
import org.jibx.custom.classes.CustomBase;
import org.jibx.custom.classes.ValueCustom;

import junit.framework.TestCase;

/**
 * Test code for command line processing.
 */
public class BindingGeneratorCommandLineTest extends TestCase
{
    private static String[] OVERRIDE_ARGS = new String[] {
        "--property-access=false",
        "--require=none",
        "--namespace-style=fixed",
        "--namespace=http://www.sosnoski.com/ws",
        "--name-style=camel-case",
        "--strip-prefixes=s_ m_"
    };
    
    public void testOverrides() throws Exception {
        BindGenCommandLine cmd = new BindGenCommandLine();
        assertTrue("Basic argument processing", cmd.processArgs(OVERRIDE_ARGS));
        ClassCustom clas = cmd.getGlobal().addClassCustomization("org.jibx.binding.generator.DataClass1");
        assertFalse("property-access setting", clas.isPropertyAccess());
        assertEquals("namespace-style setting", CustomBase.DERIVE_FIXED, clas.getNamespaceStyle());
        assertEquals("namespace setting", "http://www.sosnoski.com/ws", clas.getNamespace());
        assertEquals("name-style setting", CustomBase.CAMEL_CASE_NAMES, clas.getNameStyle());
        assertEquals("derived name", "dataClass1", clas.getElementName());
        Collection members = clas.getMembers();
        assertEquals("property count", 4, members.size());
        ValueCustom member = clas.getMember("boolean");
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
        assertFalse("string required", member.isRequired());
    }
}
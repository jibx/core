/*
Copyright (c) 2007-2009, Dennis M. Sosnoski.
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

import java.net.URL;
import java.util.ArrayList;

import org.jibx.binding.Loader;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.BindingHolder;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.StructureElement;
import org.jibx.binding.model.ValueElement;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.runtime.QName;
import org.jibx.util.ReferenceCountMap;

/**
 * Test code for binding generation.
 */
public class GeneratorTest extends CustomizationTestBase
{
    public static final String MULTIPLE_PROPERTIES_CLASSES1 =
        "<custom property-access='true' force-classes='true'>\n" +
        "  <package name='org.jibx.binding'>\n" +
        "    <class name='generator.DataClass2'\n" +
        "        strip-prefixes='s_ m_' excludes='transient static'>\n" +
        "      <value field='m_dataClass1s'\n" +
        "          item-type='org.jibx.binding.generator.DataClass1'/>\n" +
        "    </class>\n" +
        "    <class name='generator.DataClass1'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    public static final String MULTIPLE_PROPERTIES_CLASSES2 =
        "<custom property-access='true' force-classes='true' namespace-style='fixed'\n" +
        "    namespace='http://www.jibx.org/test'>\n" +
        "  <package name='org.jibx.binding'>\n" +
        "    <class name='generator.DataClass2' element-name='class2'\n" +
        "        type-name='class2' strip-prefixes='s_ m_'>\n" +
        "      <value field='m_dataClass1s' required='true'\n" +
        "          item-type='org.jibx.binding.generator.DataClass1'/>\n" +
        "    </class>\n" +
        "    <class name='generator.DataClass1' includes='string linked int'\n" +
        "        element-name='class1' type-name='class1'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        
        // set paths to be used for loading referenced classes
        URL[] urls = Loader.getClassPaths();
        String[] paths = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            paths[i] = urls[i].getFile();
        }
        ClassCache.setPaths(paths);
        ClassFile.setPaths(paths);
    }

    public void testExpandReferences() {
        GlobalCustom custom = new GlobalCustom();
        BindGen gen = new BindGen(custom);
        ReferenceCountMap refmap = new ReferenceCountMap();
        gen.expandReferences("org.jibx.binding.generator.DataClass2Java5", refmap);
        assertEquals("original class references", 0, refmap.getCount("org.jibx.binding.generator.DataClass2Java5"));
        assertEquals("referenced class references", 2, refmap.getCount("org.jibx.binding.generator.DataClass1"));
    }
    
    public void testMultiplePropertiesRefCounts() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PROPERTIES_CLASSES1);
        BindGen gen = new BindGen(custom);
        ReferenceCountMap refmap = new ReferenceCountMap();
        gen.expandReferences("org.jibx.binding.generator.DataClass2Java5", refmap);
        assertEquals("original class references", 0, refmap.getCount("org.jibx.binding.generator.DataClass2"));
        assertEquals("referenced class references", 2, refmap.getCount("org.jibx.binding.generator.DataClass1"));
        gen.expandReferences("org.jibx.binding.generator.DataClass2Java5", refmap);
        assertEquals("original class references", 0, refmap.getCount("org.jibx.binding.generator.DataClass2Java5"));
        assertEquals("referenced class references", 3, refmap.getCount("org.jibx.binding.generator.DataClass1"));
    }

    public void testDefaultPackageBinding() throws Exception {
        GlobalCustom custom = new GlobalCustom();
        custom.initClasses();
        custom.fillClasses();
        BindGen gen = new BindGen(custom);
        ArrayList types = new ArrayList();
        types.add("DefaultPackageClass");
        gen.generate(null, types);
        gen.finish("binding");
        BindingHolder hold = gen.getBinding(null);
        assertNotNull("no-namespace binding", hold);
        BindingElement binding = hold.getBinding();
        ArrayList childs = binding.topChildren();
        assertEquals("child count", 2, childs.size());
        Object child = childs.get(0);
        assertTrue("child type", child instanceof MappingElement);
        MappingElementBase mapping = (MappingElementBase)child;
        assertEquals("mapped class", mapping.getClassName(), "DefaultPackageClass");
        assertEquals("mapped items", 5, mapping.children().size());
    }

    public void testSingleClassBinding() throws Exception {
        GlobalCustom custom = new GlobalCustom();
        custom.initClasses();
        custom.fillClasses();
        BindGen gen = new BindGen(custom);
        ArrayList types = new ArrayList();
        types.add("org.jibx.binding.generator.DataClass1");
        gen.generate(null, types);
        gen.finish("binding");
        BindingHolder hold = gen.getBinding("http://jibx.org/binding/generator");
        assertNotNull("default namespace binding", hold);
        BindingElement binding = hold.getBinding();
        ArrayList childs = binding.topChildren();
        assertEquals("child count", 3, childs.size());
        Object child = childs.get(1);
        assertTrue("child type", child instanceof MappingElement);
        MappingElementBase mapping = (MappingElementBase)child;
        assertEquals("mapped class", mapping.getClassName(), "org.jibx.binding.generator.DataClass1");
        assertEquals("mapped items", 4, mapping.children().size());
    }
    
    public void testMultiplePropertiesClasses1() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PROPERTIES_CLASSES1);
        BindGen gen = new BindGen(custom);
        ArrayList types = new ArrayList();
        types.add("org.jibx.binding.generator.DataClass1");
        types.add("org.jibx.binding.generator.DataClass2");
        gen.generate(null, types);
        gen.finish("binding");
        BindingHolder hold = gen.getBinding("http://jibx.org/binding/generator");
        assertNotNull("default namespace binding", hold);
        BindingElement binding = hold.getBinding();
        ArrayList childs = binding.topChildren();
        assertEquals("child count", 5, childs.size());
        Object child = childs.get(1);
        assertTrue("child type", child instanceof MappingElement);
        MappingElementBase mapping = (MappingElementBase)child;
        assertTrue("expected abstract mapping first", mapping.isAbstract());
        assertEquals("mapped class", mapping.getClassName(), "org.jibx.binding.generator.DataClass1");
        QName qname = mapping.getTypeQName();
        assertNotNull("type name", qname);
        assertEquals("default type name", "dataClass1", qname.getName());
        assertEquals("default type namespace", "http://jibx.org/binding/generator", qname.getUri());
        assertEquals("mapped items", 4, mapping.children().size());
        // order of child values inside mapping not specified for class, so don't check
        child = childs.get(2);
        assertTrue("child type", child instanceof MappingElement);
        mapping = (MappingElementBase)child;
        assertFalse("expected concrete mapping second", mapping.isAbstract());
        assertEquals("mapped class", mapping.getClassName(), "org.jibx.binding.generator.DataClass1");
        assertEquals("default element name", "dataClass1", mapping.getName());
        assertEquals("mapped items", 1, mapping.children().size());
        child = mapping.children().get(0);
        assertTrue("child type", child instanceof StructureElement);
        StructureElement struct = (StructureElement)child;
        assertEquals("mapping reference structure content", 0, struct.children().size());
        qname = struct.getMapAsQName();
        assertNotNull("type name", qname);
        assertEquals("reference type name", "dataClass1", qname.getName());
        assertEquals("reference type namespace", "http://jibx.org/binding/generator", qname.getUri());
    }
    
    public void testMultiplePropertiesClasses2() throws Exception {
        GlobalCustom custom = readCustom(MULTIPLE_PROPERTIES_CLASSES2);
        BindGen gen = new BindGen(custom);
        ArrayList types = new ArrayList();
        types.add("org.jibx.binding.generator.DataClass1");
        types.add("org.jibx.binding.generator.DataClass2");
        gen.generate(null, types);
        gen.finish("binding");
        BindingHolder hold = gen.getBinding("http://www.jibx.org/test");
        assertNotNull("specified namespace binding", hold);
        BindingElement binding = hold.getBinding();
        ArrayList childs = binding.topChildren();
        assertEquals("child count", 5, childs.size());
        Object child = childs.get(1);
        assertTrue("child type", child instanceof MappingElement);
        MappingElementBase mapping = (MappingElementBase)child;
        assertTrue("expected abstract mapping first", mapping.isAbstract());
        assertEquals("mapped class", mapping.getClassName(), "org.jibx.binding.generator.DataClass1");
        QName qname = mapping.getTypeQName();
        assertNotNull("type name", qname);
        assertEquals("specified type name", "class1", qname.getName());
        assertEquals("specified type namespace", "http://www.jibx.org/test", qname.getUri());
        ArrayList mapchilds = mapping.children();
        assertEquals("mapped items", 3, mapchilds.size());
        child = mapchilds.get(0);
        assertTrue("child type", child instanceof ValueElement);
        ValueElement value = (ValueElement)child;
        assertEquals("get method", "getString", value.getGetName());
        assertEquals("set method", "setString", value.getSetName());
        assertEquals("value style", "element", value.getStyleName());
        assertEquals("element name", "string", value.getName());
        child = mapchilds.get(1);
        assertTrue("child type", child instanceof StructureElement);
        StructureElement struct = (StructureElement)child;
        assertEquals("get method", "getLinked", struct.getGetName());
        assertEquals("set method", "setLinked", struct.getSetName());
        assertEquals("element name", "linked", struct.getName());
        child = mapchilds.get(2);
        assertTrue("child type", child instanceof ValueElement);
        value = (ValueElement)child;
        assertEquals("get method", "getInt", value.getGetName());
        assertEquals("set method", "setInt", value.getSetName());
        assertEquals("value style", "attribute", value.getStyleName());
        assertEquals("element name", "int", value.getName());
        child = childs.get(2);
        assertTrue("child type", child instanceof MappingElement);
        mapping = (MappingElementBase)child;
        assertFalse("expected concrete mapping second", mapping.isAbstract());
        assertEquals("mapped class", mapping.getClassName(), "org.jibx.binding.generator.DataClass1");
        assertEquals("specified element name", "class1", mapping.getName());
        assertEquals("mapped items", 1, mapping.children().size());
        child = mapping.children().get(0);
        assertTrue("child type", child instanceof StructureElement);
        struct = (StructureElement)child;
        assertEquals("mapping reference structure content", 0, struct.children().size());
        qname = struct.getMapAsQName();
        assertNotNull("type name", qname);
        assertEquals("reference type name", "class1", qname.getName());
        assertEquals("reference type namespace", "http://www.jibx.org/test", qname.getUri());
    }
}
/*
Copyright (c) 2004, Dennis M. Sosnoski
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

package extras;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import org.jibx.extras.JDOMWriter;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

/** 
 * JUnit <code>TestCase</code> against <code>JDOMWriter</code>.
 * 
 * Most tests marshal a JDOMContact object to a JDOM <code>Document</code> and
 * compare it to a <code>Document</code> based on a static XML file.
 * 
 * As JDOMs equals() methods are based on identity comparison is done using
 * <code>XMLOutputter</code> and comparing the resulting <code>String</code>s.
 * 
 * @author Andreas Brenk
 * @version 1.0
 */
public class JDOMWriterTest extends TestCase {

    private static final String FIRSTNAME = "Tom";
    private static final String LASTNAME = "Sawyer";
    private static final String PHONE = "1-800-555-1212";
    private static final String INFO1 = "Additional Info First Line";
    private static final String INFO2 = "Additional Info Second Line";
    private static final String EMPTY_NAMESPACE = "";
    private static final String XML_NAMESPACE = "http://www.w3.org/XML/1998/namespace";
    private static final String[] STD_NAMESPACES = new String[] { EMPTY_NAMESPACE, XML_NAMESPACE };
       
    public JDOMWriterTest(String name) {
        super(name);
    }

    /**
     * Test a simple binding.
     */
    public void testSimpleBinding() throws Exception {
        final JDOMContact0 contact = new JDOMContact0(FIRSTNAME, LASTNAME, PHONE);
        
        final Document actualDocument = marshalActualDocument(contact);
        final Document expectedDocument = readExpectedOutput("test/extras/jdomcontact0.xml");
                
        final String actualOutput = formatDocument(actualDocument);
        final String expectedOutput = formatDocument(expectedDocument);
        
        saveActualOutput(actualOutput);
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    /**
     * Test a binding with namespaces.
     */
    public void testNamespaceBinding() throws Exception {
        JDOMContact1 contact = new JDOMContact1(FIRSTNAME, LASTNAME, PHONE, new JDOMContactInformation1(INFO1, INFO2));
        
        final Document actualDocument = marshalActualDocument(contact);
        final Document expectedDocument = readExpectedOutput("test/extras/jdomcontact1.xml");
        
        final String actualOutput = formatDocument(actualDocument);
        final String expectedOutput = formatDocument(expectedDocument);

        saveActualOutput(actualOutput);
        
        assertEquals(expectedOutput, actualOutput);
    }
    
    /**
     * Test marshalling an object into an existing empty Document.
     */
    public void testExistingDocumentWithoutRootNode() throws Exception {
        final JDOMContact0 contact = new JDOMContact0(FIRSTNAME, LASTNAME, PHONE);
        final Document existingDocument = new Document();
        
        final Document returnedDocument = marshalActualDocument(contact, existingDocument);
        final Document expectedDocument = readExpectedOutput("test/extras/jdomcontact0.xml");
                
        final String actualOutput = formatDocument(existingDocument);
        final String expectedOutput = formatDocument(expectedDocument);
        
        saveActualOutput(actualOutput);
        
        assertSame(existingDocument, returnedDocument);
        assertEquals(expectedOutput, actualOutput);
    }

    /**
     * Test marshalling an object into an existing Document with root Element.
     */
    public void testExistingDocumentWithRootNode() throws Exception {
        final JDOMContact0 contact = new JDOMContact0(FIRSTNAME, LASTNAME, PHONE);
        final Element rootElement = new Element("root"); 
        final Document existingDocument = new Document(rootElement);
        
        final Document returnedDocument = marshalActualDocument(contact, existingDocument);
        final Document expectedDocument = readExpectedOutput("test/extras/jdomDocumentContact0.xml");
        
        final String actualOutput = formatDocument(existingDocument);
        final String expectedOutput = formatDocument(expectedDocument);
        
        saveActualOutput(actualOutput);
        
        assertSame(existingDocument, returnedDocument);
        assertEquals(expectedOutput, actualOutput);
    }
    
    /**
     * Test marshalling an object into an existing Document with root and content Elements.
     */
    public void testExistingDocumentAndCurrentElement() throws Exception {
        final JDOMContact0 contact = new JDOMContact0(FIRSTNAME, LASTNAME, PHONE);
        final Element rootElement = new Element("root");
        final Element currentElement = new Element("current");
        rootElement.addContent(new Element("pre").setText("before current"));
        rootElement.addContent(currentElement);
        rootElement.addContent(new Element("post").setText("after current"));
        final Document existingDocument = new Document(rootElement);
        
        final Document returnedDocument = marshalActualDocument(contact, currentElement);
        final Document expectedDocument = readExpectedOutput("test/extras/jdomElementContact0.xml");
                
        final String actualOutput = formatDocument(existingDocument);
        final String expectedOutput = formatDocument(expectedDocument);
        
        saveActualOutput(actualOutput);
        
        assertSame(existingDocument, returnedDocument);
        assertEquals(expectedOutput, actualOutput);
    }
    
    /**
     * Test valid constructor usages.
     */
    public void testValidContructorUsage() throws Exception {
        Element element;
        Document document;
        JDOMWriter jdomWriter = null;
        
        // valid
        jdomWriter = new JDOMWriter(STD_NAMESPACES);
        jdomWriter = new JDOMWriter(new String[] { EMPTY_NAMESPACE, XML_NAMESPACE, "urn:myNs1", "urn:myNs2" });
        
        document = new Document();
        
        // valid
        jdomWriter = new JDOMWriter(STD_NAMESPACES, document);
        
        element = new Element("element");
        document = new Document(element);
        
        // valid
        jdomWriter = new JDOMWriter(STD_NAMESPACES, document);
        jdomWriter = new JDOMWriter(STD_NAMESPACES, element);
        
        assertNotNull(jdomWriter);
    }
    
    /**
     * Test invalid constructor usages.
     */
    public void testInvalidConstructorUsage() throws Exception {
        JDOMWriter jdomWriter = null;
       
        // invalid: empty and xml namespace have to be defined
        try {
            jdomWriter = new JDOMWriter(new String[0]);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch(ArrayIndexOutOfBoundsException e) {}
        try {
            jdomWriter = new JDOMWriter(new String[1]);
            fail("expected ArrayIndexOutOfBoundsException");
        } catch(ArrayIndexOutOfBoundsException e) {}
        
        // invalid: Document is null
        try {
            jdomWriter = new JDOMWriter(STD_NAMESPACES, (Document) null);
            fail("expected NullPointerException");
        } catch(NullPointerException e) {}
        
        // invalid: Element is null
        try {
            jdomWriter = new JDOMWriter(STD_NAMESPACES, (Element) null);
            fail("expected NullPointerException");
        } catch(NullPointerException e) {}
        
        // invalid: Element has no parent Document
        try {
            jdomWriter = new JDOMWriter(STD_NAMESPACES, new Element("element"));
            fail("expected NullPointerException");
        } catch(NullPointerException e) {}
        
        assertNull(jdomWriter);
    }
    
    /**
     * Test adding and removing extension namespaces.
     */
    public void testExtensionNamespaces() throws Exception {
        final JDOMWriter jdomWriter = new JDOMWriter(STD_NAMESPACES);
        String[][] ns;

        ns = jdomWriter.getExtensionNamespaces();
        assertNull(ns);
        
        jdomWriter.pushExtensionNamespaces(new String[] { "urn:myNs1", "urn:myNs2" });
        
        ns = jdomWriter.getExtensionNamespaces();
        assertEquals(1, ns.length);
        assertEquals("urn:myNs1", ns[0][0]);
        assertEquals("urn:myNs2", ns[0][1]);
        
        jdomWriter.pushExtensionNamespaces(new String[] { "urn:myNs3" });
        
        ns = jdomWriter.getExtensionNamespaces();
        assertEquals(2, ns.length);
        assertEquals(2, ns[0].length);
        assertEquals(1, ns[1].length);
        assertEquals("urn:myNs1", ns[0][0]);
        assertEquals("urn:myNs2", ns[0][1]);
        assertEquals("urn:myNs3", ns[1][0]);
        
        jdomWriter.popExtensionNamespaces();
        
        ns = jdomWriter.getExtensionNamespaces();
        assertEquals(1, ns.length);
        assertEquals(2, ns[0].length);
        assertEquals("urn:myNs1", ns[0][0]);
        assertEquals("urn:myNs2", ns[0][1]);
        
        jdomWriter.popExtensionNamespaces();
        
        ns = jdomWriter.getExtensionNamespaces();
        assertNull(ns);
    }
    
    /**
     * Test changing namespace prefixes.
     */
    public void testNamespacePrefixes() throws Exception {
        JDOMWriter jdomWriter = new JDOMWriter(new String[] { EMPTY_NAMESPACE, XML_NAMESPACE, "urn:myNs1", "urn:myNs2" });
        
        assertEquals("", jdomWriter.getNamespaceUri(0));
        assertEquals("http://www.w3.org/XML/1998/namespace", jdomWriter.getNamespaceUri(1));
        assertEquals("urn:myNs1", jdomWriter.getNamespaceUri(2));
        assertEquals("urn:myNs2", jdomWriter.getNamespaceUri(3));
        
        assertEquals("", jdomWriter.getNamespacePrefix(0));
        assertEquals("xml", jdomWriter.getNamespacePrefix(1));
        assertNull("unexpected prefix defined", jdomWriter.getNamespacePrefix(2));
        assertNull("unexpected prefix defined", jdomWriter.getNamespacePrefix(3));
        
        jdomWriter.startTagNamespaces(0, "element1", new int[] {2, 3}, new String[] {"myns1", "myns2"});
        jdomWriter.closeStartTag();

        assertEquals("myns1", jdomWriter.getNamespacePrefix(2));
        assertEquals("myns2", jdomWriter.getNamespacePrefix(3));
        
        jdomWriter.startTagNamespaces(0, "element2", new int[] {2}, new String[] {"prefix"});
        
        assertEquals("prefix", jdomWriter.getNamespacePrefix(2));
        assertEquals("myns2", jdomWriter.getNamespacePrefix(3));
        
        jdomWriter.closeEmptyTag();
        
        assertEquals("myns1", jdomWriter.getNamespacePrefix(2));
        assertEquals("myns2", jdomWriter.getNamespacePrefix(3));
        
        jdomWriter.endTag(0, "element1");
        
        assertNull("unexpected prefix defined", jdomWriter.getNamespacePrefix(2));
        assertNull("unexpected prefix defined", jdomWriter.getNamespacePrefix(3));
    }
    
    /**
     * Helper method to read a file to a <code>Document</code>.
     * 
     * @param filename the name of a XML file to read
     * @return a Document based on the given file 
     */
    private static Document readExpectedOutput(String filename) throws IOException, JDOMException {
        final SAXBuilder builder = new SAXBuilder();
        final InputStream input = new FileInputStream(filename);
        
        return builder.build(input);
    }
    
    /**
     * Helper method to format a <code>Document</code> to String.
     * 
     * @param document the Document to format
     * @return a String containing XML
     */
    private static String formatDocument(Document document) throws IOException {        
        final XMLOutputter outputter = new XMLOutputter();
        final StringWriter writer = new StringWriter();
        
        outputter.output(document, writer);
        
        return writer.getBuffer().toString();
    }
    
    /**
     * Helper method to marshal an Object to a Document.
     * 
     * @param root the Object to marshal
     * @return the Document representation
     */
    private static Document marshalActualDocument(Object root) throws JiBXException {
        final IBindingFactory bfact = BindingDirectory.getFactory(root.getClass());
        final IMarshallingContext mctx = bfact.createMarshallingContext();
        
        final JDOMWriter jdomWriter = new JDOMWriter(bfact.getNamespaces());
        
        return marshalDocument(root, mctx, jdomWriter);
    }
    
    private static Document marshalActualDocument(Object root, Document document) throws JiBXException {
        final IBindingFactory bfact = BindingDirectory.getFactory(root.getClass());
        final IMarshallingContext mctx = bfact.createMarshallingContext();
        
        final JDOMWriter jdomWriter = new JDOMWriter(bfact.getNamespaces(), document);
        
        return marshalDocument(root, mctx, jdomWriter);
    }
    
    private static Document marshalActualDocument(Object root, Element element) throws JiBXException {
        final IBindingFactory bfact = BindingDirectory.getFactory(root.getClass());
        final IMarshallingContext mctx = bfact.createMarshallingContext();
        
        final JDOMWriter jdomWriter = new JDOMWriter(bfact.getNamespaces(), element);
        
        return marshalDocument(root, mctx, jdomWriter);
    }
    
    private static Document marshalDocument(Object root, IMarshallingContext mctx, JDOMWriter jdomWriter) throws JiBXException {
        mctx.setXmlWriter(jdomWriter);
        mctx.marshalDocument(root);
        
        return jdomWriter.getDocument();
    }
    
    /**
     * Helper method to save the actual output to temp.xml
     * 
     * @param actualOutput the output generated by marshalling
     */
    private static void saveActualOutput(String actualOutput) throws IOException {
        File fout = new File("temp.xml");
        fout.delete();
        FileOutputStream fos = new FileOutputStream(fout);
        fos.write(actualOutput.getBytes());
        fos.close();
    }
}

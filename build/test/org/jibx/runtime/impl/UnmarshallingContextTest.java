/*
 * Created on Feb 5 2008
 */ 

package org.jibx.runtime.impl;

import org.jibx.runtime.IXMLReader;
import org.jibx.runtime.JiBXConstrainedParseException;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.JiBXParseException;

import junit.framework.TestCase;

/**
 * Stubbed-out implementation of IXMLReader to be passed into the
 * unmarshalling context.
 * @author Joshua Davies
 */
class TestDocument implements IXMLReader {
	private int[] m_events;
	private String m_namespace;
	private String m_tagName;
	private String m_textContent;
	private String m_attributeName;
	private String m_attributeValue;
	
	private int nextEvent = 0;
	
	/**
	 * Describe one single element that should be pulled out of this reader instance.
	 * @param events
	 * @param namespace
	 * @param tagName
	 * @param textContent
	 */
	public TestDocument(int[] events, String namespace, String tagName, String textContent) {
		m_events = events;
		m_namespace = namespace;
		m_tagName = tagName;
		m_textContent = textContent;
	}
	
	/**
	 * Describe one single attribute that should be pulled out of this reader instance.
	 * @param attributeName
	 * @param attributeValue
	 */
	public TestDocument(String attributeName, String attributeValue) {
		m_attributeName = attributeName;
		m_attributeValue = attributeValue;
	}
	
	public void init() {}
	
	public int nextToken() {
		return ++nextEvent;
	}

	public int next() throws JiBXException {
		return ++nextEvent;
	}
	
	public String getName() {
		return m_tagName;
	}
	
	public String getNamespace() {
		return m_namespace;
	}
	
	public int getEventType() {
		return m_events[nextEvent];
	}
	
	public String getText() {
		return m_textContent;
	}
	
	public String buildPositionString() {
		return null;
	}

	public int getAttributeCount() {
		return 0;
	}

	public String getAttributeName(int index) {
		return m_attributeName;
	}

	public String getAttributeNamespace(int index) {
		return null;
	}

	public String getAttributePrefix(int index) {
		return null;
	}

	public String getAttributeValue(int index) {
		return m_attributeValue;
	}

	public String getAttributeValue(String ns, String name) {
		return m_attributeValue;
	}

	public int getColumnNumber() {
		return 0;
	}

	public String getDocumentName() {
		return null;
	}

	public String getInputEncoding() {
		return null;
	}

	public int getLineNumber() {
		return 0;
	}

	public String getNamespace(String prefix) {
		return null;
	}

	public int getNamespaceCount(int depth) {
		return 0;
	}

	public String getNamespacePrefix(int index) {
		return null;
	}

	public String getNamespaceUri(int index) {
		return null;
	}

	public int getNestingDepth() {
		return 0;
	}

	public String getPrefix() {
		return null;
	}

	public boolean isNamespaceAware() {
		return false;
	}
}

/**
 * Test the unmarshalling context.
 * @author Joshua Davies
 */
public class UnmarshallingContextTest extends TestCase {
	/**
	 * Verify that malformed integer content throws a properly
	 * formed JiBXParseException.
	 */
	public void testParseContentInt() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try	{
			uctx.parseContentInt("namespace", "tag");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed integer attributes throw a properly
	 * formed JiBXParseException.
	 */
	public void testParseAttributeInt()	{
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try	{
			uctx.attributeInt("namespace", "attr");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed integer attributes throw a properly
	 * formed JiBXParseException.
	 */
	public void testParseAttributeIntDefault()	{
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try	{
			uctx.attributeInt("namespace", "attr", 5);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed enum-constrained attributes throw a properly
	 * formed JiBXConstrainedParseException. 
	 */
	public void testParseAttributeEnumeration() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		String[] enums = new String[] {"def", "ghi"}; 
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeEnumeration("namespace", "attr", enums, new int[] {1, 2});
			fail("Expected JiBXConstrainedParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXConstrainedParseException(null, "abc", enums, "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed enum-constrained attributes throw a properly
	 * formed JiBXConstrainedParseException. 
	 */
	public void testParseAttributeEnumerationDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		String[] enums = new String[] {"def", "ghi"}; 
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeEnumeration("namespace", "attr", enums, new int[] {1, 2}, 1);
			fail("Expected JiBXConstrainedParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXConstrainedParseException(null, "abc", enums, "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed enum-constrained content throws a properly
	 * formed JiBXConstrainedParseException. 
	 */
	public void testParseContentEnumeration() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		String[] enums = new String[] {"def", "ghi"}; 
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseContentEnumeration("namespace", "tag", enums, new int[] {1, 2});
			fail("Expected JiBXConstrainedParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXConstrainedParseException(null, "abc", enums, "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed enum-constrained content throws a properly
	 * formed JiBXConstrainedParseException. 
	 */
	public void testParseContentEnumerationDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		String[] enums = new String[] {"def", "ghi"}; 
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementEnumeration("namespace", "tag", enums, new int[] {1, 2}, 1);
			fail("Expected JiBXConstrainedParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXConstrainedParseException(null, "abc", enums, "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed short content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeShort() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeShort("namespace", "attr");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed short content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeShortDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeShort("namespace", "attr", (short) 1);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed short content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseContentShort() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseContentShort("namespace", "tag");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed short content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseElementShort() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementShort("namespace", "tag", (byte) 1);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed byte content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeByte() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeByte("namespace", "attr");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed byte content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeByteDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeByte("namespace", "attr", (byte) 1);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed byte content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseContentByte() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseContentByte("namespace", "tag");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed byte content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseElementByte() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementByte("namespace", "tag", (byte) 1);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed char content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeChar() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeChar("namespace", "attr");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed char content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeCharDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeChar("namespace", "attr", 'a');
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed char content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseContentChar() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseContentChar("namespace", "tag");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed char content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseElementChar() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementChar("namespace", "tag", 'a');
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed long content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeLong() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeLong("namespace", "attr");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed long content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeLongDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeLong("namespace", "attr", 1L);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed long content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseContentLong() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementLong("namespace", "tag");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed long content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseElementLong() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementLong("namespace", "tag", 1L);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed boolean content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeBool() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeBoolean("namespace", "attr");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed boolean content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeBoolDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeBoolean("namespace", "attr", false);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed boolean content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseContentBool() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementBoolean("namespace", "tag");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed boolean content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseElementBool() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementBoolean("namespace", "tag", false);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed float content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeFloat() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeFloat("namespace", "attr");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed float content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeFloatDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeFloat("namespace", "attr", (float) 1.0);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed float content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseContentFloat() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementFloat("namespace", "tag");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed float content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseElementFloat() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementFloat("namespace", "tag", (float) 1.0);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed double content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeDouble() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeDouble("namespace", "attr");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed double content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseAttributeDoubleDefault() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument("attr", "abc"));
		try {
			uctx.attributeDouble("namespace", "attr", 1.0);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "attr", null));
		}
	}
	
	/**
	 * Verify that malformed double content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseContentDouble() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementDouble("namespace", "tag");
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
	
	/**
	 * Verify that malformed float content throws a properly formed
	 * JiBXParseException.
	 */
	public void testParseElementDouble() {
		UnmarshallingContext uctx = new UnmarshallingContext();
		uctx.setDocument(new TestDocument(new int [] {IXMLReader.START_TAG, IXMLReader.TEXT, IXMLReader.END_TAG}, 
				"namespace", "tag", "abc"));
		try {
			uctx.parseElementDouble("namespace", "tag", 1.0);
			fail("Expected JiBXParseException to be thrown");
		} catch (JiBXException e) {
			assertEquals(e, new JiBXParseException(null, "abc", "namespace", "tag", null));
		}
	}
}

package org.jibx.runtime;

import junit.framework.TestCase;

/**
 * Verify the permutations of the constrained parse exception (specifically,
 * string creation).
 */
public class JiBXConstrainedParseExceptionTest extends TestCase {
	public void testOneValue() {
		JiBXConstrainedParseException e = new JiBXConstrainedParseException(
				"msg", "value", new String[] {"abc"});
		assertTrue(e.getMessage().endsWith(".  Acceptable values are 'abc'."));
	}
	
	public void testTwoValues() {
		JiBXConstrainedParseException e = new JiBXConstrainedParseException(
				"msg", "value", new String[] {"abc", "def"});
		assertTrue(e.getMessage().endsWith(".  Acceptable values are 'abc', 'def'."));
	}
	
	public void testThreeValues() {
		JiBXConstrainedParseException e = new JiBXConstrainedParseException(
				"msg", "value", new String[] {"abc", "def", "ghi"});
		assertTrue(e.getMessage().endsWith(".  Acceptable values are 'abc', 'def', 'ghi'."));
	}
}

/*
 * Created on Mar 6, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */

package org.jibx.runtime;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * @author dennis
 */
 
public class UtilityTest extends TestCase {
	
	private static long LMS_PER_DAY = (long)24*60*60*1000;
	private DateFormat m_dateTimeFormat =
		new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
	private DateFormat m_dateFormat =
		new SimpleDateFormat("yyyy-MM-dd");
	private DateFormat m_yearFormat =
		new SimpleDateFormat("yyyy");
	private Calendar m_calendar = 
		new GregorianCalendar(TimeZone.getTimeZone("UTC"));

	/**
	 * Constructor for UtilityTest.
	 * @param arg0
	 */
	public UtilityTest(String arg0) {
		super(arg0);
		m_dateTimeFormat.setCalendar(m_calendar);
		m_dateFormat.setCalendar(m_calendar);
		m_yearFormat.setCalendar(m_calendar);
	}

	public void testParseInt() throws JiBXException {
		assertEquals(0, Utility.parseInt("0"));
		assertEquals(2000000000, Utility.parseInt("2000000000"));
		assertEquals(-2000000000, Utility.parseInt("-2000000000"));
		assertEquals(2000000000, Utility.parseInt("+2000000000"));
		try {
			Utility.parseInt("-");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseInt("+");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseInt("20000000000");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseInt("-20000000000");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseInt("+20000000000");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseInt("2000000X");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testSerializeInt() throws JiBXException {
		assertEquals("0", Utility.serializeInt(0));
		assertEquals("2000000", Utility.serializeInt(2000000));
		assertEquals("2000000000", Utility.serializeInt(2000000000));
		assertEquals("-2000000", Utility.serializeInt(-2000000));
		assertEquals("-2000000000", Utility.serializeInt(-2000000000));
	}

	public void testParseLong() throws JiBXException {
		assertEquals(0, Utility.parseLong("0"));
		assertEquals(2000000000, Utility.parseLong("2000000000"));
		assertEquals(-2000000000, Utility.parseLong("-2000000000"));
		assertEquals(2000000000, Utility.parseLong("+2000000000"));
		assertEquals(200000000000000L, Utility.parseLong("200000000000000"));
		assertEquals(-200000000000000L, Utility.parseLong("-200000000000000"));
		assertEquals(200000000000000L, Utility.parseLong("+200000000000000"));
		try {
			Utility.parseLong("-");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseLong("+");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseLong("2000000X");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testSerializeLong() {
		assertEquals("0", Utility.serializeLong(0));
		assertEquals("2000000", Utility.serializeLong(2000000));
		assertEquals("2000000000000000",
			Utility.serializeLong(2000000000000000L));
		assertEquals("-2000000", Utility.serializeLong(-2000000));
		assertEquals("-2000000000000000",
			Utility.serializeLong(-2000000000000000L));
	}
	
	public void testParseShort() throws JiBXException {
		assertEquals(0, Utility.parseShort("0"));
		assertEquals(20000, Utility.parseShort("20000"));
		assertEquals(-20000, Utility.parseShort("-20000"));
		assertEquals(20000, Utility.parseShort("+20000"));
		try {
			Utility.parseShort("-");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseShort("+");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseShort("2000000");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseShort("2000X");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testSerializeShort() {
		assertEquals("0", Utility.serializeShort((short)0));
		assertEquals("20000", Utility.serializeShort((short)20000));
		assertEquals("-20000", Utility.serializeShort((short)-20000));
	}
	
	public void testParseByte() throws JiBXException {
		assertEquals(0, Utility.parseByte("0"));
		assertEquals(100, Utility.parseByte("100"));
		assertEquals(-100, Utility.parseByte("-100"));
		assertEquals(100, Utility.parseByte("+100"));
		try {
			Utility.parseByte("-");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseByte("+");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseByte("128");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseByte("10X");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testSerializeByte() {
		assertEquals("0", Utility.serializeByte((byte)0));
		assertEquals("100", Utility.serializeByte((byte)100));
		assertEquals("-100", Utility.serializeByte((byte)-100));
	}

	public void testParseBoolean() throws JiBXException {
		assertTrue(Utility.parseBoolean("true"));
		assertTrue(Utility.parseBoolean("1"));
		assertFalse(Utility.parseBoolean("false"));
		assertFalse(Utility.parseBoolean("0"));
		try {
			Utility.parseBoolean("x");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseBoolean("2");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseBoolean("+1");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testSerializeBoolean() {
		assertEquals("true", Utility.serializeBoolean(true));
		assertEquals("false", Utility.serializeBoolean(false));
	}

	public void testParseChar() throws JiBXException {
		assertEquals(0, Utility.parseChar("0"));
		assertEquals(100, Utility.parseChar("100"));
		assertEquals(1000, Utility.parseChar("+1000"));
		assertEquals(65000, Utility.parseChar("65000"));
		try {
			Utility.parseChar("-");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseChar("+");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseChar("-10");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseChar("69000");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testSerializeChar() {
		assertEquals("0", Utility.serializeChar((char)0));
		assertEquals("100", Utility.serializeChar((char)100));
		assertEquals("60000", Utility.serializeChar((char)60000));
	}

	public void testParseFloat() throws JiBXException {
		assertEquals(1.0f, Utility.parseFloat("1.0"), 0.000001f);
		assertEquals(1000000000.0f, Utility.parseFloat("1000000000.0"), 100.0f);
		assertEquals(0.0000000001f,
			Utility.parseFloat("0.0000000001"), 1.0e-17f);
		assertEquals(0, Float.compare(-0.0f, Utility.parseFloat("-0")));
		assertEquals(0, Float.compare(Float.NEGATIVE_INFINITY,
			Utility.parseFloat("-INF")));
		assertEquals(0, Float.compare(Float.POSITIVE_INFINITY,
			Utility.parseFloat("INF")));
		assertEquals(0, Float.compare(Float.NaN, Utility.parseFloat("NaN")));
		try {
			Utility.parseFloat("NAN");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseFloat("+INF");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseFloat("1E+2.5");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testSerializeFloat() {
		assertEquals("1.0", Utility.serializeFloat(1.0f));
		assertEquals("1.0E9", Utility.serializeFloat(1000000000.0f));
		assertEquals("1.0E-10", Utility.serializeFloat(0.0000000001f));
		assertEquals("-INF", Utility.serializeFloat(Float.NEGATIVE_INFINITY));
		assertEquals("INF", Utility.serializeFloat(Float.POSITIVE_INFINITY));
		assertEquals("NaN", Utility.serializeFloat(Float.NaN));
	}

	public void testParseDouble() throws JiBXException {
		assertEquals(1.0d, Utility.parseDouble("1.0"), 1.0e-12d);
		assertEquals(1000000000.0d, Utility.parseDouble("1000000000.0"), 0.01d);
		assertEquals(0.0000000001d,
			Utility.parseDouble("0.0000000001"), 1.0e-22d);
		assertEquals(0, Double.compare(-0.0d, Utility.parseDouble("-0")));
		assertEquals(0, Double.compare(Double.NEGATIVE_INFINITY,
			Utility.parseDouble("-INF")));
		assertEquals(0, Double.compare(Double.POSITIVE_INFINITY,
			Utility.parseDouble("INF")));
		assertEquals(0, Double.compare(Double.NaN, Utility.parseDouble("NaN")));
		try {
			Utility.parseDouble("NAN");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDouble("+INF");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDouble("1E+2.5");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testSerializeDouble() {
		assertEquals("1.0", Utility.serializeDouble(1.0d));
		assertEquals("1.0E9", Utility.serializeDouble(1000000000.0d));
		assertEquals("1.0E-10", Utility.serializeDouble(0.0000000001d));
		assertEquals("-INF", Utility.serializeDouble(Double.NEGATIVE_INFINITY));
		assertEquals("INF", Utility.serializeDouble(Double.POSITIVE_INFINITY));
		assertEquals("NaN", Utility.serializeDouble(Double.NaN));
	}

	public void testParseYear() {
	}

	public void testParseYearMonth() {
	}

	public void testParseDate() throws JiBXException {
		assertEquals(0, Utility.parseDate("1970-01-01"));
		assertEquals(LMS_PER_DAY, Utility.parseDate("1970-01-02"));
		assertEquals(LMS_PER_DAY,
			Utility.parseDate("0001-03-01") - Utility.parseDate("0001-02-28"));
		assertEquals(LMS_PER_DAY,
			Utility.parseDate("0001-01-01") - Utility.parseDate("-0001-12-31"));
		assertEquals(LMS_PER_DAY,
			Utility.parseDate("-0001-03-01") -
			Utility.parseDate("-0001-02-28"));
		assertEquals(LMS_PER_DAY*2,
			Utility.parseDate("-0004-03-01") -
			Utility.parseDate("-0004-02-28"));
		try {
			Utility.parseDate("+1970-01-01");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("197X-01-01");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("1970-1-01");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("1970-01-32");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("1970-02-29");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("01-01-01");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("0001-02-29");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("0000-01-01");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("-0001-02-29");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDate("-0003-02-29");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testParseDateTime() throws JiBXException {
		assertEquals(0, Utility.parseDateTime("1970-01-01T00:00:00.000"));
		assertEquals(0, Utility.parseDateTime("1970-01-01T00:00:00"));
		assertEquals(1, Utility.parseDateTime("1970-01-01T00:00:00.001"));
		assertEquals(1000, Utility.parseDateTime("1970-01-01T00:00:01"));
		assertEquals(60*1000, Utility.parseDateTime("1970-01-01T00:01:00"));
		assertEquals(60*60*1000, Utility.parseDateTime("1970-01-01T01:00:00Z"));
        assertEquals(0, Utility.parseDateTime("1969-12-31T24:00:00Z"));
		assertEquals(60*60*1000,
			Utility.parseDateTime("1970-01-01T02:00:00+01:00"));
		assertEquals(LMS_PER_DAY,
			Utility.parseDateTime("1970-01-02T00:00:00Z"));
		assertEquals(LMS_PER_DAY,
			Utility.parseDateTime("0001-03-01T00:00:00Z") -
			Utility.parseDateTime("0001-02-28T00:00:00Z"));
		assertEquals(1000, Utility.parseDateTime("0001-01-01T00:00:00Z") -
			Utility.parseDateTime("-0001-12-31T23:59:59Z"));
		assertEquals(LMS_PER_DAY,
			Utility.parseDateTime("0001-01-01T00:00:00Z") -
			Utility.parseDateTime("-0001-12-31T00:00:00Z"));
		assertEquals(LMS_PER_DAY,
			Utility.parseDateTime("-0001-03-01T00:00:00Z") -
			Utility.parseDateTime("-0001-02-28T00:00:00Z"));
		assertEquals(LMS_PER_DAY*2,
			Utility.parseDateTime("-0004-03-01T00:00:00Z") -
			Utility.parseDateTime("-0004-02-28T00:00:00Z"));
		try {
			Utility.parseDateTime("+1970-01-01T00:00:00.000");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("197X-01-01T00:00:00.000");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("1970-1-01T00:00:00.000");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("1970-01-01T00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("1970-01-32T00:00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("1970-02-29T00:00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("1970-01-01T60:00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("1970-01-01T-5:00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("01-01-01T00:00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("0001-02-29T00:00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("0000-01-01T00:00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("-0001-02-29T00:00:00");
			fail();
		} catch (JiBXException ex) {}
		try {
			Utility.parseDateTime("-0003-02-29T00:00:00");
			fail();
		} catch (JiBXException ex) {}
	}

	public void testDeserializeDateTime() {
	}
	
	private void trySerializeYear(String dt)
		throws JiBXException, ParseException {
		Date date = m_yearFormat.parse(dt);
		String result = Utility.serializeYear(date);
		assertEquals(dt, result);
	}
	
	private void tryRoundtripYear(String dt) throws JiBXException {
		long time = Utility.parseYear(dt);
		String result = Utility.serializeYear(new Date(time));
//		System.out.println("Parsed date " + dt + " to value " + time +
//			", serialized as " + result);
		assertEquals(dt, result);
	}

	public void testSerializeYear() throws JiBXException, ParseException {
		trySerializeYear("1970");
		trySerializeYear("1969");
		trySerializeYear("1968");
		trySerializeYear("1967");
		trySerializeYear("1966");
		trySerializeYear("1965");
		trySerializeYear("1800");
		trySerializeYear("1600");
		trySerializeYear("1999");
		trySerializeYear("2000");
		trySerializeYear("2999");
		trySerializeYear("3000");
		trySerializeYear("9999");
		tryRoundtripYear("1970");
		tryRoundtripYear("1969");
		tryRoundtripYear("1968");
		tryRoundtripYear("1967");
		tryRoundtripYear("1966");
		tryRoundtripYear("1965");
		tryRoundtripYear("1800");
		tryRoundtripYear("1600");
		tryRoundtripYear("1999");
		tryRoundtripYear("2000");
		tryRoundtripYear("2001");
		tryRoundtripYear("2002");
		tryRoundtripYear("2999");
		tryRoundtripYear("3000");
		tryRoundtripYear("9999");
		
		// the following values can only be tested roundtrip (because Java
		//  conversion insists on a discontinuity in Gregorian calendar)
		tryRoundtripYear("12999");
		tryRoundtripYear("1300");
		tryRoundtripYear("0300");
		tryRoundtripYear("0001");
		tryRoundtripYear("-0001");
		tryRoundtripYear("-0004");
		tryRoundtripYear("-0005");
		tryRoundtripYear("-0199");
		tryRoundtripYear("-0198");
	}

	public void testSerializeYearMonth() throws JiBXException {
		assertEquals("1970-01", Utility.serializeYearMonth(new Date(0)));
	}
	
	private void trySerializeDate(String dt)
		throws JiBXException, ParseException {
        Date date = m_dateFormat.parse(dt);
        String result = Utility.serializeDate(date);
        assertEquals(dt, result);
	}
	
	// ignores trailing time zone information
	private void tryRoundtripDate(String dt) throws JiBXException {
		long time = Utility.parseDate(dt);
		String result = Utility.serializeDate(new Date(time));
//		System.out.println("Parsed date " + dt + " to value " + time +
//			", serialized as " + result);
		assertEquals(dt.substring(0, result.length()), result);
	}
    
//#!j2me{
    private void trySerializeSqlDate(String dt)
        throws JiBXException, ParseException {
        java.sql.Date date = java.sql.Date.valueOf(dt);
        String result = Utility.serializeSqlDate(date);
        assertEquals(dt, result);
    }
    
    // ignores trailing time zone information
    private void tryRoundtripSqlDate(String dt) throws JiBXException {
        java.sql.Date date1 = Utility.deserializeSqlDate(dt);
        int split = dt.indexOf('-', 1);
        java.sql.Date date2 = java.sql.Date.valueOf(dt.substring(0, split+6));
        assertEquals(date1, date2);
        String result = Utility.serializeSqlDate(date1);
        assertEquals(dt.substring(0, result.length()), result);
    }
    
    private void tryRoundtripOnlySqlDate(String dt) throws JiBXException {
        java.sql.Date date1 = Utility.deserializeSqlDate(dt);
        String result = Utility.serializeSqlDate(date1);
        assertEquals(dt, result);
    }
//#j2me}

	public void testSerializeDate() throws JiBXException, ParseException {
		trySerializeDate("1970-01-01");
		trySerializeDate("1970-02-01");
		trySerializeDate("1970-01-02");
		trySerializeDate("1969-12-31");
		trySerializeDate("1969-01-01");
		trySerializeDate("1969-02-28");
		trySerializeDate("1969-03-01");
		trySerializeDate("1968-01-01");
		trySerializeDate("1968-02-29");
		trySerializeDate("1968-03-01");
		trySerializeDate("1967-01-01");
		trySerializeDate("1966-01-01");
		trySerializeDate("1965-01-01");
		trySerializeDate("1800-01-01");
		trySerializeDate("1600-11-11");
		trySerializeDate("1999-12-31");
		trySerializeDate("2000-01-01");
		trySerializeDate("2000-02-29");
		trySerializeDate("2000-12-31");
		trySerializeDate("2001-01-01");
		trySerializeDate("2001-12-31");
		trySerializeDate("2002-01-01");
        trySerializeDate("2002-02-13");
		trySerializeDate("2999-12-31");
		trySerializeDate("3000-01-01");
		trySerializeDate("3000-12-31");
		trySerializeDate("9999-12-31");
		tryRoundtripDate("1970-01-01");
		tryRoundtripDate("1970-02-01");
		tryRoundtripDate("1970-01-02");
		tryRoundtripDate("1969-12-31");
		tryRoundtripDate("1969-01-01");
		tryRoundtripDate("1969-02-28");
		tryRoundtripDate("1969-03-01");
		tryRoundtripDate("1968-01-01");
		tryRoundtripDate("1968-02-29");
		tryRoundtripDate("1968-03-01");
		tryRoundtripDate("1967-01-01");
		tryRoundtripDate("1966-01-01");
		tryRoundtripDate("1965-01-01");
		tryRoundtripDate("1800-01-01");
		tryRoundtripDate("1600-11-11");
		tryRoundtripDate("1999-12-31");
		tryRoundtripDate("2000-01-01");
		tryRoundtripDate("2000-02-29");
		tryRoundtripDate("2000-12-31");
		tryRoundtripDate("2001-01-01");
		tryRoundtripDate("2001-12-31");
		tryRoundtripDate("2002-01-01");
        tryRoundtripDate("2002-02-13");
		tryRoundtripDate("2999-12-31");
		tryRoundtripDate("3000-01-01");
		tryRoundtripDate("3000-12-31");
		tryRoundtripDate("9999-12-31");
		tryRoundtripDate("1970-01-01Z");
		tryRoundtripDate("1969-03-01+10:30");
		tryRoundtripDate("1800-01-01-04:00");
		tryRoundtripDate("1600-11-11Z");
		tryRoundtripDate("3000-12-31+00:00");
		tryRoundtripDate("9999-12-31-12:00");
		
		// the following values can only be tested roundtrip (because Java
		//  conversion insists on a discontinuity in Gregorian calendar)
		tryRoundtripDate("12999-12-31");
		tryRoundtripDate("1300-01-01");
		tryRoundtripDate("0300-12-31");
		tryRoundtripDate("0001-01-01");
		tryRoundtripDate("-0001-12-31");
		tryRoundtripDate("-0004-02-29");
		tryRoundtripDate("-0004-03-01");
		tryRoundtripDate("-0004-12-31");
		tryRoundtripDate("-0005-02-28");
		tryRoundtripDate("-0005-03-01");
		tryRoundtripDate("-0199-12-31");
		tryRoundtripDate("-0198-01-01");
	}

//#!j2me{
    public void testSerializeSqlDate() throws JiBXException, ParseException {
        trySerializeSqlDate("1970-01-01");
        trySerializeSqlDate("1970-02-01");
        trySerializeSqlDate("1970-01-02");
        trySerializeSqlDate("1969-12-31");
        trySerializeSqlDate("1969-01-01");
        trySerializeSqlDate("1969-02-28");
        trySerializeSqlDate("1969-03-01");
        trySerializeSqlDate("1968-01-01");
        trySerializeSqlDate("1968-02-29");
        trySerializeSqlDate("1968-03-01");
        trySerializeSqlDate("1967-01-01");
        trySerializeSqlDate("1966-01-01");
        trySerializeSqlDate("1965-01-01");
        trySerializeSqlDate("1800-01-01");
        trySerializeSqlDate("1600-11-11");
        trySerializeSqlDate("1999-12-31");
        trySerializeSqlDate("2000-01-01");
        trySerializeSqlDate("2000-02-29");
        trySerializeSqlDate("2000-12-31");
        trySerializeSqlDate("2001-01-01");
        trySerializeSqlDate("2001-12-31");
        trySerializeSqlDate("2002-01-01");
        trySerializeSqlDate("2002-02-13");
        trySerializeSqlDate("2999-12-31");
        trySerializeSqlDate("3000-01-01");
        trySerializeSqlDate("3000-12-31");
        trySerializeSqlDate("9999-12-31");
        tryRoundtripSqlDate("1970-01-01");
        tryRoundtripSqlDate("1970-02-01");
        tryRoundtripSqlDate("1970-01-02");
        tryRoundtripSqlDate("1969-12-31");
        tryRoundtripSqlDate("1969-01-01");
        tryRoundtripSqlDate("1969-02-28");
        tryRoundtripSqlDate("1969-03-01");
        tryRoundtripSqlDate("1968-01-01");
        tryRoundtripSqlDate("1968-02-29");
        tryRoundtripSqlDate("1968-03-01");
        tryRoundtripSqlDate("1967-01-01");
        tryRoundtripSqlDate("1966-01-01");
        tryRoundtripSqlDate("1965-01-01");
        tryRoundtripSqlDate("1800-01-01");
        tryRoundtripSqlDate("1600-11-11");
        tryRoundtripSqlDate("1999-12-31");
        tryRoundtripSqlDate("2000-01-01");
        tryRoundtripSqlDate("2000-02-29");
        tryRoundtripSqlDate("2000-12-31");
        tryRoundtripSqlDate("2001-01-01");
        tryRoundtripSqlDate("2001-12-31");
        tryRoundtripSqlDate("2002-01-01");
        tryRoundtripSqlDate("2002-02-13");
        tryRoundtripSqlDate("2999-12-31");
        tryRoundtripSqlDate("3000-01-01");
        tryRoundtripSqlDate("3000-12-31");
        tryRoundtripSqlDate("9999-12-31");
        // value fails on Windows only, with error thrown by java.sql.Date
        //tryRoundtripSqlDate("12999-12-31");
        tryRoundtripSqlDate("1970-01-01Z");
        tryRoundtripSqlDate("1969-03-01+10:30");
        tryRoundtripSqlDate("1800-01-01-04:00");
        tryRoundtripSqlDate("1600-11-11Z");
        tryRoundtripSqlDate("3000-12-31+00:00");
        tryRoundtripSqlDate("9999-12-31-12:00");
        
        // the following values can only be tested roundtrip (because Java
        //  conversion insists on a discontinuity in Gregorian calendar)
        tryRoundtripOnlySqlDate("1300-01-01");
        tryRoundtripOnlySqlDate("0300-12-31");
        tryRoundtripOnlySqlDate("0001-01-01");
        tryRoundtripOnlySqlDate("-0001-12-31");
        // should work, but inconsistency in conversions at present
//        tryRoundtripOnlySqlDate("-0004-02-29");
        tryRoundtripOnlySqlDate("-0004-03-01");
        tryRoundtripOnlySqlDate("-0004-12-31");
        tryRoundtripOnlySqlDate("-0005-02-28");
        tryRoundtripOnlySqlDate("-0005-03-01");
        tryRoundtripOnlySqlDate("-0199-12-31");
        tryRoundtripOnlySqlDate("-0198-01-01");
    }
    
    public void testSerializeTimestamp() throws JiBXException {
        Timestamp ts = Timestamp.valueOf("2006-09-07 15:37:10.123456");
        String text = Utility.serializeTimestamp(ts);
        assertTrue("Timestamp serialization error",
            text.startsWith("2006-09-0"));
        assertTrue("Timestamp serialization error",
            text.endsWith(":37:10.123456Z"));
        assertEquals("Timestamp deserialization error", ts,
            Utility.deserializeTimestamp(text));
    }
//#j2me}
	
	private void trySerializeDateTime(String dt)
		throws JiBXException, ParseException {
		Date date = m_dateTimeFormat.parse(dt);
		String result = Utility.serializeDateTime(date);
		assertEquals(dt, result);
	}
	
	private void tryRoundtripDateTime(String dt) throws JiBXException {
		long time = Utility.parseDateTime(dt);
		String result = Utility.serializeDateTime(new Date(time));
//		System.out.println("Parsed date " + dt + " to value " + time +
//			", serialized as " + result);
		assertEquals(dt, result);
	}

	public void testSerializeDateTime() throws JiBXException, ParseException {
		assertEquals("1970-01-01T00:00:00Z",
			Utility.serializeDateTime(new Date(0)));
		trySerializeDateTime("1970-01-01T00:00:00Z");
		trySerializeDateTime("1970-01-01T01:00:00Z");
		trySerializeDateTime("1970-01-01T00:01:00Z");
		trySerializeDateTime("1970-01-01T00:00:01Z");
		trySerializeDateTime("1970-01-01T23:59:59Z");
		trySerializeDateTime("1970-02-01T00:00:00Z");
		trySerializeDateTime("1970-01-02T00:00:00Z");
		trySerializeDateTime("1969-12-31T23:59:59Z");
		trySerializeDateTime("1969-01-01T00:00:00Z");
		trySerializeDateTime("1969-02-28T00:00:00Z");
		trySerializeDateTime("1969-03-01T00:00:00Z");
		trySerializeDateTime("1968-01-01T00:00:00Z");
		trySerializeDateTime("1968-02-29T00:00:00Z");
		trySerializeDateTime("1968-03-01T00:00:00Z");
		trySerializeDateTime("1967-01-01T00:00:00Z");
		trySerializeDateTime("1966-01-01T00:00:00Z");
		trySerializeDateTime("1965-01-01T00:00:00Z");
		trySerializeDateTime("1800-01-01T00:00:00Z");
		trySerializeDateTime("1600-11-11T23:59:59Z");
		trySerializeDateTime("1999-12-31T23:59:59Z");
		trySerializeDateTime("2000-01-01T00:00:00Z");
		trySerializeDateTime("2000-02-29T00:00:00Z");
		trySerializeDateTime("2000-12-31T23:59:59Z");
		trySerializeDateTime("2001-01-01T00:00:00Z");
        trySerializeDateTime("2001-02-28T18:54:31Z");
		trySerializeDateTime("2001-12-31T23:59:59Z");
		trySerializeDateTime("2002-01-01T00:00:00Z");
		trySerializeDateTime("2999-12-31T23:59:59Z");
		trySerializeDateTime("3000-01-01T00:00:00Z");
		trySerializeDateTime("3000-12-31T23:59:59Z");
		trySerializeDateTime("9999-12-31T23:59:59Z");
		tryRoundtripDateTime("1970-01-01T00:00:00Z");
		tryRoundtripDateTime("1970-01-01T01:00:00Z");
		tryRoundtripDateTime("1970-01-01T00:01:00Z");
		tryRoundtripDateTime("1970-01-01T00:00:01Z");
		tryRoundtripDateTime("1970-01-01T23:59:59Z");
		tryRoundtripDateTime("1970-02-01T00:00:00Z");
		tryRoundtripDateTime("1970-01-02T00:00:00Z");
		tryRoundtripDateTime("1969-12-31T23:59:59Z");
		tryRoundtripDateTime("1969-01-01T00:00:00Z");
		tryRoundtripDateTime("1969-02-28T00:00:00Z");
		tryRoundtripDateTime("1969-03-01T00:00:00Z");
		tryRoundtripDateTime("1968-01-01T00:00:00Z");
		tryRoundtripDateTime("1968-02-29T00:00:00Z");
		tryRoundtripDateTime("1968-03-01T00:00:00Z");
		tryRoundtripDateTime("1967-01-01T00:00:00Z");
		tryRoundtripDateTime("1966-01-01T00:00:00Z");
		tryRoundtripDateTime("1965-01-01T00:00:00Z");
		tryRoundtripDateTime("1800-01-01T00:00:00Z");
		tryRoundtripDateTime("1600-11-11T23:59:59Z");
		tryRoundtripDateTime("1999-12-31T23:59:59Z");
		tryRoundtripDateTime("2000-01-01T00:00:00Z");
		tryRoundtripDateTime("2000-02-29T00:00:00Z");
		tryRoundtripDateTime("2000-12-31T23:59:59Z");
		tryRoundtripDateTime("2001-01-01T00:00:00Z");
        tryRoundtripDateTime("2001-02-28T18:54:31Z");
		tryRoundtripDateTime("2001-12-31T23:59:59Z");
		tryRoundtripDateTime("2002-01-01T00:00:00Z");
		tryRoundtripDateTime("2999-12-31T23:59:59Z");
		tryRoundtripDateTime("3000-01-01T00:00:00Z");
		tryRoundtripDateTime("3000-12-31T23:59:59Z");
		tryRoundtripDateTime("9999-12-31T23:59:59Z");
		
		// the following values can only be tested roundtrip (because Java
		//  conversion insists on a discontinuity in Gregorian calendar)
		tryRoundtripDateTime("12999-12-31T23:59:59Z");
		tryRoundtripDateTime("1300-01-01T00:00:00Z");
		tryRoundtripDateTime("0300-12-31T23:59:59Z");
		tryRoundtripDateTime("0001-01-01T00:00:00Z");
		tryRoundtripDateTime("-0001-12-31T23:59:59Z");
		tryRoundtripDateTime("-0004-02-29T23:59:59Z");
		tryRoundtripDateTime("-0004-03-01T23:59:59Z");
		tryRoundtripDateTime("-0199-12-31T23:59:59Z");
		tryRoundtripDateTime("-0198-01-01T00:00:00Z");
	}
    
    private boolean equalsBytes(byte[] a, byte[] b) {
        if (a == null) {
            return b == null;
        } else if (b == null) {
            return false;
        } else if (a.length != b.length) {
            return false;
        } else {
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    
	public void testParseBase64() throws JiBXException {
		assertTrue(equalsBytes(Utility.parseBase64(""), new byte[0]));
        assertTrue(equalsBytes(Utility.parseBase64("Cg=="), "\n".getBytes()));
        assertTrue(equalsBytes(Utility.parseBase64("d2hhdA=="),
            "what".getBytes()));
        assertTrue(equalsBytes(Utility.parseBase64
            ("d2hhdCB3aWxsIHByaW50IG91dA=="),
            "what will print out".getBytes()));
        assertTrue(equalsBytes(Utility.parseBase64
            ("d2hhdCAgd2lsbCAgIHByaW50ICAgICBvdXQgICAgICA="),
            "what  will   print     out      ".getBytes()));
        assertTrue(equalsBytes(Utility.parseBase64
            ("d2hhdCAgd2lsbCAgIHByaW50ICAgICBvdXQ="),
            "what  will   print     out".getBytes()));
	}

	public void testSerializeBase64() {
		assertEquals(Utility.serializeBase64(new byte[0]), "");
		assertEquals(Utility.serializeBase64("\n".getBytes()), "Cg==");
		assertEquals(Utility.serializeBase64("what".getBytes()), "d2hhdA==");
		assertEquals(Utility.serializeBase64("what will print out".getBytes()),
            "d2hhdCB3aWxsIHByaW50IG91dA==");
		assertEquals(Utility.serializeBase64
            ("what  will   print     out      ".getBytes()),
            "d2hhdCAgd2lsbCAgIHByaW50ICAgICBvdXQgICAgICA=");
		assertEquals(Utility.serializeBase64
            ("what  will   print     out".getBytes()),
            "d2hhdCAgd2lsbCAgIHByaW50ICAgICBvdXQ=");
	}

	public void testIsEqual() {
		assertTrue(Utility.isEqual(null, null));
		assertFalse(Utility.isEqual(null, "text"));
		assertFalse(Utility.isEqual("text", null));
		assertTrue(Utility.isEqual("text", "text"));
	}
    
	public static void main(String[] args) {
		String[] names = { UtilityTest.class.getName() };
		junit.textui.TestRunner.main(names);
	}
}

/*
Copyright (c) 2009, Dennis M. Sosnoski. All rights reserved.

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

package org.jibx.runtime;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import junit.framework.TestCase;

/**
 * Joda date/time conversion tests.
 *
 * @author Dennis M. Sosnoski
 */
public class JodaConvertTest extends TestCase
{
    private static int OFFSET_HOURS = 13;
    private static int OFFSET_MILLIS = OFFSET_HOURS*60*60*1000;
    private static final String OFFSET_TEXT = "+" + OFFSET_HOURS + ":00";
    
    protected void setUp() throws Exception {
        DateTimeZone.setDefault(DateTimeZone.forOffsetHours(OFFSET_HOURS));
    }

    public void testDeserializeLocalDate() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeLocalDate(null));
        LocalDate date = JodaConvert.deserializeLocalDate("2008-02-28");
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 28, date.getDayOfMonth());
        date = JodaConvert.deserializeLocalDate("2008-02-29Z");
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 29, date.getDayOfMonth());
        date = JodaConvert.deserializeLocalDate("2008-02-29-24:00");
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 29, date.getDayOfMonth());
        try {
            JodaConvert.deserializeUTCDateTime("2007-02-29");
            fail("Invalid day number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateTime("2007-13-29");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateTime("2007-00-29");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
    }
    
    public void testSerializeLocalDate() throws JiBXException {
        assertEquals("2008-02-28", JodaConvert.serializeLocalDate(new LocalDate(2008, 2, 28)));
        assertEquals("2008-02-29", JodaConvert.serializeLocalDate(new LocalDate(2008, 2, 29)));
        assertEquals("2008-03-01", JodaConvert.serializeLocalDate(new LocalDate(2008, 3, 1)));
    }
    
    public void testDeserializeZonedDateMidnight() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeZonedDateMidnight(null));
        DateMidnight date = JodaConvert.deserializeZonedDateMidnight("2008-02-28");
        DateTimeZone zone = date.getZone();
        assertEquals(OFFSET_MILLIS, zone.getOffset(date));
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 28, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        date = JodaConvert.deserializeZonedDateMidnight("2008-02-29Z");
        zone = date.getZone();
        assertEquals(0, zone.getOffset(date));
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 29, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        date = JodaConvert.deserializeZonedDateMidnight("2008-02-29-12:30");
        zone = date.getZone();
        assertEquals("Expected -12:30 timezone", -750*60*1000, zone.getOffset(date));
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 29, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        try {
            JodaConvert.deserializeZonedDateMidnight("2007-02-29");
            fail("Invalid day number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeZonedDateMidnight("2007-13-29");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeZonedDateMidnight("2007-00-29");
            fail("Invalid month number");
        } catch (Exception ex) {}
    }
    
    public void testDeserializeDefaultDateMidnight() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeLocalDateMidnight(null));
        DateMidnight date = JodaConvert.deserializeLocalDateMidnight("2008-02-28");
        DateTimeZone zone = date.getZone();
        assertEquals(OFFSET_MILLIS, zone.getOffset(date));
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 28, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        date = JodaConvert.deserializeLocalDateMidnight("2008-02-29Z");
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 29, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        date = JodaConvert.deserializeLocalDateMidnight("2008-02-29-24:00");
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 29, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        try {
            JodaConvert.deserializeLocalDateMidnight("2007-02-29");
            fail("Invalid day number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeLocalDateMidnight("2007-13-29");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeLocalDateMidnight("2007-00-29");
            fail("Invalid month number");
        } catch (Exception ex) {}
    }
    
    public void testDeserializeUTCDateMidnight() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeUTCDateMidnight(null));
        DateMidnight date = JodaConvert.deserializeUTCDateMidnight("2008-02-28");
        DateTimeZone zone = date.getZone();
        assertEquals("UTC timezone expected", "UTC", zone.getID());
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 28, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        date = JodaConvert.deserializeUTCDateMidnight("2008-02-29Z");
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 29, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        date = JodaConvert.deserializeUTCDateMidnight("2008-02-29-24:00");
        assertEquals("Wrong value", 2008, date.getYear());
        assertEquals("Wrong value", 2, date.getMonthOfYear());
        assertEquals("Wrong value", 29, date.getDayOfMonth());
        assertEquals("Wrong value", 0, date.getHourOfDay());
        assertEquals("Wrong value", 0, date.getMinuteOfHour());
        assertEquals("Wrong value", 0, date.getSecondOfMinute());
        try {
            JodaConvert.deserializeUTCDateMidnight("2007-02-29");
            fail("Invalid day number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateMidnight("2007-13-29");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateMidnight("2007-00-29");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
    }
    
    public void testSerializeUnzonedDateMidnight() throws JiBXException {
        assertEquals("2008-02-28", JodaConvert.serializeUnzonedDateMidnight(new DateMidnight(2008, 2, 28)));
        assertEquals("2008-02-29", JodaConvert.serializeUnzonedDateMidnight(new DateMidnight(2008, 2, 29)));
        assertEquals("2008-03-01", JodaConvert.serializeUnzonedDateMidnight(new DateMidnight(2008, 3, 1)));
    }
    
    public void testSerializeZonedDateMidnight() throws JiBXException {
        assertEquals("2008-02-28" + OFFSET_TEXT, JodaConvert.serializeZonedDateMidnight(new DateMidnight(2008, 2, 28)));
        assertEquals("2008-02-29" + OFFSET_TEXT, JodaConvert.serializeZonedDateMidnight(new DateMidnight(2008, 2, 29)));
        assertEquals("2008-03-01" + OFFSET_TEXT, JodaConvert.serializeZonedDateMidnight(new DateMidnight(2008, 3, 1)));
    }
    
    public void testSerializeUTCDateMidnight() throws JiBXException {
        assertEquals("2008-02-28Z", JodaConvert.serializeUTCDateMidnight(new DateMidnight(2008, 2, 28)));
        assertEquals("2008-02-29Z", JodaConvert.serializeUTCDateMidnight(new DateMidnight(2008, 2, 29)));
        assertEquals("2008-03-01Z", JodaConvert.serializeUTCDateMidnight(new DateMidnight(2008, 3, 1)));
        assertEquals("2008-02-28Z", JodaConvert.serializeUTCDateMidnight(new DateMidnight(2008, 2, 28, DateTimeZone.UTC)));
        assertEquals("2008-02-29Z", JodaConvert.serializeUTCDateMidnight(new DateMidnight(2008, 2, 29, DateTimeZone.UTC)));
        assertEquals("2008-03-01Z", JodaConvert.serializeUTCDateMidnight(new DateMidnight(2008, 3, 1, DateTimeZone.UTC)));
    }
    
    public void testDeserializeLocalTime() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeLocalTime(null));
        LocalTime time = JodaConvert.deserializeLocalTime("01:02:03Z");
        assertEquals("Wrong value", 1, time.getHourOfDay());
        assertEquals("Wrong value", 2, time.getMinuteOfHour());
        assertEquals("Wrong value", 3, time.getSecondOfMinute());
        time = JodaConvert.deserializeLocalTime("02:05:06.123-02:00");
        assertEquals("Wrong value", 2, time.getHourOfDay());
        assertEquals("Wrong value", 5, time.getMinuteOfHour());
        assertEquals("Wrong value", 6, time.getSecondOfMinute());
        assertEquals("Wrong value", 123, time.getMillisOfSecond());
        time = JodaConvert.deserializeLocalTime("02:05:06.123");
        assertEquals("Wrong value", 2, time.getHourOfDay());
        assertEquals("Wrong value", 5, time.getMinuteOfHour());
        assertEquals("Wrong value", 6, time.getSecondOfMinute());
        assertEquals("Wrong value", 123, time.getMillisOfSecond());
        time = JodaConvert.deserializeLocalTime("24:00:00");
        assertEquals("Wrong value", 0, time.getHourOfDay());
        assertEquals("Wrong value", 0, time.getMinuteOfHour());
        assertEquals("Wrong value", 0, time.getSecondOfMinute());
        try {
            JodaConvert.deserializeLocalTime("24:05:06.123-02:00");
            fail("Invalid hour number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeLocalTime("02:60:47.123-02:00");
            fail("Invalid minute number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeLocalTime("02:05:61.123-02:00");
            fail("Invalid second number");
        } catch (JiBXException ex) {}
    }
    
    public void testSerializeUnzonedLocalTime() throws JiBXException {
        assertEquals("01:02:03", JodaConvert.serializeUnzonedLocalTime(new LocalTime(1, 2, 3)));
        assertEquals("23:59:59", JodaConvert.serializeUnzonedLocalTime(new LocalTime(23, 59, 59)));
        assertEquals("00:00:00.123", JodaConvert.serializeUnzonedLocalTime(new LocalTime(0, 0, 0, 123)));
    }
    
    public void testSerializeUTCLocalTime() throws JiBXException {
        assertEquals("01:02:03Z", JodaConvert.serializeUTCLocalTime(new LocalTime(1, 2, 3)));
        assertEquals("23:59:59Z", JodaConvert.serializeUTCLocalTime(new LocalTime(23, 59, 59)));
        assertEquals("00:00:00.123Z", JodaConvert.serializeUTCLocalTime(new LocalTime(0, 0, 0, 123)));
    }
    
    public void testDeserializeZonedDateTime() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeZonedDateTime(null));
        DateTime time = JodaConvert.deserializeZonedDateTime("2008-02-28T01:02:03");
        DateTimeZone zone = time.getZone();
        assertEquals("Default timezone expected", OFFSET_MILLIS, zone.getOffset(time));
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        assertEquals("Wrong value", 28, time.getDayOfMonth());
        assertEquals("Wrong value", 1, time.getHourOfDay());
        assertEquals("Wrong value", 2, time.getMinuteOfHour());
        assertEquals("Wrong value", 3, time.getSecondOfMinute());
        time = JodaConvert.deserializeUTCDateTime("2008-02-28T01:02:03Z");
        zone = time.getZone();
        assertEquals("UTC timezone expected", "UTC", zone.getID());
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        assertEquals("Wrong value", 28, time.getDayOfMonth());
        assertEquals("Wrong value", 1, time.getHourOfDay());
        assertEquals("Wrong value", 2, time.getMinuteOfHour());
        assertEquals("Wrong value", 3, time.getSecondOfMinute());
        time = JodaConvert.deserializeUTCDateTime("2008-02-29T24:00:00Z");
        zone = time.getZone();
        assertEquals("UTC timezone expected", "UTC", zone.getID());
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 3, time.getMonthOfYear());
        assertEquals("Wrong value", 1, time.getDayOfMonth());
        assertEquals("Wrong value", 0, time.getHourOfDay());
        assertEquals("Wrong value", 0, time.getMinuteOfHour());
        assertEquals("Wrong value", 0, time.getSecondOfMinute());
        time = JodaConvert.deserializeZonedDateTime("2008-02-29T02:05:06.123-02:30");
        zone = time.getZone();
        assertEquals("Expected -02:30 timezone", -150*60*1000, zone.getOffset(time));
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        assertEquals("Wrong value", 29, time.getDayOfMonth());
        assertEquals("Wrong value", 2, time.getHourOfDay());
        assertEquals("Wrong value", 5, time.getMinuteOfHour());
        assertEquals("Wrong value", 6, time.getSecondOfMinute());
        assertEquals("Wrong value", 123, time.getMillisOfSecond());
        try {
            JodaConvert.deserializeZonedDateTime("2007-02-29T02:05:06.123-02:00");
            fail("Invalid day number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeZonedDateTime("2007-13-29T02:05:06.123-02:00");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeZonedDateTime("2007-00-29T02:05:06.123-02:00");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeZonedDateTime("2007-00-29T24:05:06.123-02:00");
            fail("Invalid hour number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeZonedDateTime("2007-00-29T02:60:60.123-02:00");
            fail("Invalid minute number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeZonedDateTime("2007-00-29T02:05:60.123-02:00");
            fail("Invalid second number");
        } catch (JiBXException ex) {}
    }
    
    public void testDeserializeUTCDateTime() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeUTCDateTime(null));
        DateTime time = JodaConvert.deserializeUTCDateTime("2008-02-28T01:02:03");
        DateTimeZone zone = time.getZone();
        assertEquals("UTC timezone expected", "UTC", zone.getID());
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        assertEquals("Wrong value", 28, time.getDayOfMonth());
        assertEquals("Wrong value", 1, time.getHourOfDay());
        assertEquals("Wrong value", 2, time.getMinuteOfHour());
        assertEquals("Wrong value", 3, time.getSecondOfMinute());
        time = JodaConvert.deserializeUTCDateTime("2008-02-28T01:02:03Z");
        zone = time.getZone();
        assertEquals("UTC timezone expected", "UTC", zone.getID());
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        assertEquals("Wrong value", 28, time.getDayOfMonth());
        assertEquals("Wrong value", 1, time.getHourOfDay());
        assertEquals("Wrong value", 2, time.getMinuteOfHour());
        assertEquals("Wrong value", 3, time.getSecondOfMinute());
        time = JodaConvert.deserializeUTCDateTime("2008-02-29T02:05:06.123-02:00");
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        assertEquals("Wrong value", 29, time.getDayOfMonth());
        assertEquals("Wrong value", 4, time.getHourOfDay());
        assertEquals("Wrong value", 5, time.getMinuteOfHour());
        assertEquals("Wrong value", 6, time.getSecondOfMinute());
        assertEquals("Wrong value", 123, time.getMillisOfSecond());
        try {
            JodaConvert.deserializeUTCDateTime("2007-02-29T02:05:06.123-02:00");
            fail("Invalid day number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateTime("2007-13-29T02:05:06.123-02:00");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateTime("2007-00-29T02:05:06.123-02:00");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateTime("2007-00-29T24:05:06.123-02:00");
            fail("Invalid hour number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateTime("2007-00-29T02:60:60.123-02:00");
            fail("Invalid minute number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateTime("2007-00-29T02:05:60.123-02:00");
            fail("Invalid second number");
        } catch (JiBXException ex) {}
    }
    
    public void testDeserializeDefaultDateTime() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeLocalDateTime(null));
        DateTime time = JodaConvert.deserializeLocalDateTime("2008-02-28T01:02:03Z");
        DateTimeZone zone = time.getZone();
        assertEquals(OFFSET_MILLIS, zone.getOffset(time));
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        if (OFFSET_HOURS < -1) {
            assertEquals("Wrong value", 27, time.getDayOfMonth());
            assertEquals("Wrong value", 25+OFFSET_HOURS, time.getHourOfDay());
        } else {
            assertEquals("Wrong value", 28, time.getDayOfMonth());
            assertEquals("Wrong value", 1+OFFSET_HOURS, time.getHourOfDay());
        }
        assertEquals("Wrong value", 2, time.getMinuteOfHour());
        assertEquals("Wrong value", 3, time.getSecondOfMinute());
        time = JodaConvert.deserializeLocalDateTime("2008-02-29T02:05:06.123-02:00");
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        if (OFFSET_HOURS < -4) {
            assertEquals("Wrong value", 28, time.getDayOfMonth());
            assertEquals("Wrong value", 28+OFFSET_HOURS, time.getHourOfDay());
        } else {
            assertEquals("Wrong value", 29, time.getDayOfMonth());
            assertEquals("Wrong value", 4+OFFSET_HOURS, time.getHourOfDay());
        }
        assertEquals("Wrong value", 5, time.getMinuteOfHour());
        assertEquals("Wrong value", 6, time.getSecondOfMinute());
        assertEquals("Wrong value", 123, time.getMillisOfSecond());
        try {
            JodaConvert.deserializeLocalDateTime("2007-02-29T02:05:06.123-02:00");
            fail("Invalid day number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeLocalDateTime("2007-13-29T02:05:06.123-02:00");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeLocalDateTime("2007-00-29T02:05:06.123-02:00");
            fail("Invalid month number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeUTCDateTime("2007-00-29T24:05:06.123-02:00");
            fail("Invalid hour number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeLocalDateTime("2007-00-29T02:58:60.123-02:00");
            fail("Invalid minute number");
        } catch (JiBXException ex) {}
        try {
            JodaConvert.deserializeLocalDateTime("2007-00-29T02:05:60.123-02:00");
            fail("Invalid second number");
        } catch (JiBXException ex) {}
    }
    
    public void testDeserializeStrictUTCDateTime() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeStrictUTCDateTime(null));
        DateTime time = JodaConvert.deserializeStrictUTCDateTime("2008-02-28T01:02:03Z");
        DateTimeZone zone = time.getZone();
        assertEquals("UTC timezone expected", "UTC", zone.getID());
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        assertEquals("Wrong value", 28, time.getDayOfMonth());
        assertEquals("Wrong value", 1, time.getHourOfDay());
        assertEquals("Wrong value", 2, time.getMinuteOfHour());
        assertEquals("Wrong value", 3, time.getSecondOfMinute());
        time = JodaConvert.deserializeStrictUTCDateTime("2008-02-29T02:05:06.123-02:00");
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        assertEquals("Wrong value", 29, time.getDayOfMonth());
        assertEquals("Wrong value", 4, time.getHourOfDay());
        assertEquals("Wrong value", 5, time.getMinuteOfHour());
        assertEquals("Wrong value", 6, time.getSecondOfMinute());
        assertEquals("Wrong value", 123, time.getMillisOfSecond());
        try {
            JodaConvert.deserializeStrictUTCDateTime("2007-02-29T02:05:06.123");
            fail("Accepted value without time zone");
        } catch (JiBXException ex) {}
    }
    
    public void testDeserializeStrictDefaultDateTime() throws JiBXException {
        assertNull("Null input", JodaConvert.deserializeStrictLocalDateTime(null));
        DateTime time = JodaConvert.deserializeStrictLocalDateTime("2008-02-28T01:02:03Z");
        DateTimeZone zone = time.getZone();
        assertEquals(OFFSET_MILLIS, zone.getOffset(time));
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        if (OFFSET_HOURS < -1) {
            assertEquals("Wrong value", 27, time.getDayOfMonth());
            assertEquals("Wrong value", 25+OFFSET_HOURS, time.getHourOfDay());
        } else {
            assertEquals("Wrong value", 28, time.getDayOfMonth());
            assertEquals("Wrong value", 1+OFFSET_HOURS, time.getHourOfDay());
        }
        assertEquals("Wrong value", 2, time.getMinuteOfHour());
        assertEquals("Wrong value", 3, time.getSecondOfMinute());
        time = JodaConvert.deserializeStrictLocalDateTime("2008-02-29T02:05:06.123-02:00");
        assertEquals("Wrong value", 2008, time.getYear());
        assertEquals("Wrong value", 2, time.getMonthOfYear());
        if (OFFSET_HOURS < -4) {
            assertEquals("Wrong value", 28, time.getDayOfMonth());
            assertEquals("Wrong value", 28+OFFSET_HOURS, time.getHourOfDay());
        } else {
            assertEquals("Wrong value", 29, time.getDayOfMonth());
            assertEquals("Wrong value", 4+OFFSET_HOURS, time.getHourOfDay());
        }
        assertEquals("Wrong value", 5, time.getMinuteOfHour());
        assertEquals("Wrong value", 6, time.getSecondOfMinute());
        assertEquals("Wrong value", 123, time.getMillisOfSecond());
        try {
            JodaConvert.deserializeStrictLocalDateTime("2007-02-29T02:05:06.123");
            fail("Accepted value without time zone");
        } catch (JiBXException ex) {}
    }
    
    public void testSerializeZonedDateTime() throws JiBXException {
        assertEquals("2008-02-28T05:06:07" + OFFSET_TEXT, JodaConvert.serializeZonedDateTime(new DateTime(2008, 2, 28, 5, 6, 7, 0)));
        assertEquals("2008-02-29T00:00:00.123" + OFFSET_TEXT, JodaConvert.serializeZonedDateTime(new DateTime(2008, 2, 29, 0, 0, 0, 123)));
        assertEquals("2008-03-01T23:59:59.999" + OFFSET_TEXT, JodaConvert.serializeZonedDateTime(new DateTime(2008, 3, 1, 23, 59, 59, 999)));
        assertEquals("2008-02-28T05:06:07Z", JodaConvert.serializeZonedDateTime(new DateTime(2008, 2, 28, 5, 6, 7, 0, DateTimeZone.UTC)));
    }
    
    public void testSerializeUTCDateTime() throws JiBXException {
        assertEquals("2008-02-27T23:06:07Z", JodaConvert.serializeUTCDateTime(new DateTime(2008, 2, 28, 12, 6, 7, 0, DateTimeZone.forOffsetHours(13))));
        assertEquals("2008-02-29T01:06:07Z", JodaConvert.serializeUTCDateTime(new DateTime(2008, 2, 28, 12, 6, 7, 0, DateTimeZone.forOffsetHours(-13))));
        assertEquals("2008-03-01T23:59:59.999Z", JodaConvert.serializeUTCDateTime(new DateTime(2008, 3, 1, 23, 59, 59, 999, DateTimeZone.UTC)));
    }
}
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

/**
 * Utility class supplying static methods for Joda date/time conversions. The actual serializer/deserializer methods are
 * all public so that they'll be included in JavaDocs; support methods are protected, so that they'll be exposed for
 * user classes extending this class.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class JodaConvert
{
    /**
     * Deserialize date value from text as local date without time zone. Date values are expected to match W3C XML
     * Schema standard format as CCYY-MM-DD, with optional leading sign and trailing time zone (with the time zone
     * ignored in this case). This method follows standard JiBX deserializer usage requirements by accepting a
     * <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static LocalDate deserializeLocalDate(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return new LocalDate(Utility.parseDate(text), DateTimeZone.UTC);
        }
    }

    /**
     * Serialize local date value to general date text without time zone. Date/time values are formatted in W3C XML
     * Schema standard format as CCYY-MM-DD, with optional leading sign included if necessary.
     *
     * @param date date to be converted
     * @return converted date text
     */
    public static String serializeLocalDate(LocalDate date) {
        return Utility.serializeDate(date.toDateTimeAtStartOfDay(DateTimeZone.UTC).getMillis());
    }
    
    /**
     * Deserialize date value as a midnight date in the specified time zone.
     *
     * @param text
     * @param zone 
     * @return date
     * @throws JiBXException
     */
    protected static DateMidnight deserializeDateMidnight(String text, DateTimeZone zone) throws JiBXException {
        
        // start by validating the length and basic format
        if (!Utility.ifDate(text)) {
            throw new JiBXException("Invalid date format");
        }
    
        // handle year, month, and day conversion
        int split = text.indexOf('-', 1);
        int year = Utility.parseInt(text.substring(0, split));
        if (year == 0) {
            throw new JiBXException("Year value 0 is not allowed");
        }
        int month = Utility.parseDigits(text, split+1, 2);
        if (month < 1 || month > 12) {
            throw new JiBXException("Month value out of range");
        }
        int day = Utility.parseDigits(text, split+4, 2);
        boolean leap = (year%4 == 0) && !((year%100 == 0) && (year%400 != 0));
        int[] starts = leap ? Utility.MONTHS_LEAP : Utility.MONTHS_NONLEAP;
        if (day < 1 || day > (starts[month]-starts[month-1])) {
            throw new JiBXException("Day value out of range");
        }
        
        // return resulting value
        return new DateMidnight(year, month, day, zone);
    }
    
    /**
     * Find the date/time zone for an xs:date or xs:dateTime text representation.
     *
     * @param text xs:date or xs:dateTime value
     * @param dflt default zone to be returned if no zone specified
     * @return zone
     * @throws JiBXException
     */
    protected static DateTimeZone findZone(String text, DateTimeZone dflt) throws JiBXException {

        // check for UTC time zone offset indicator
        int length = text.length();
        if (text.charAt(length-1) == 'Z') {
            return DateTimeZone.UTC;
        } else {
            
            // make sure time zone start offset is not year/month separator in date
            int offset = length - 6;
            if (text.indexOf('-', 1) < offset) {
                
                // check for time zone offset present as +/-hh:mm
                char chr = text.charAt(offset);
                if (chr == '-' || chr == '+') {
                    int hour = Utility.parseDigits(text, offset+1, 2);
                    int minute = Utility.parseDigits(text, offset+4, 2);
                    if (hour > 14 || minute > 59 || (hour == 14 && minute != 0)) {
                        throw new JiBXException("Invalid time zone offset: " + text);
                    } else if (chr == '-') {
                        return DateTimeZone.forOffsetHoursMinutes(-hour, minute);
                    } else {
                        return DateTimeZone.forOffsetHoursMinutes(hour, minute);
                    }
                }
            }
            return dflt;
        }
    }

    /**
     * Deserialize date value from text as midnight date in specified (or default) time zone. Date values are expected
     * to match W3C XML Schema standard format as CCYY-MM-DD, with optional leading sign and trailing time zone. If the
     * time zone indication is included a zone matching the offset is used for the date; otherwise the default time zone
     * is used. This method follows standard JiBX deserializer usage requirements by accepting a <code>null</code>
     * input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static DateMidnight deserializeZonedDateMidnight(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            DateTimeZone zone = findZone(text, DateTimeZone.getDefault());
            return deserializeDateMidnight(text, zone);
        }
    }

    /**
     * Deserialize date value from text as midnight date in default (local) time zone. Date values are expected to match
     * W3C XML Schema standard format as CCYY-MM-DD, with optional leading sign and trailing time zone (with the time
     * zone ignored in this case). This method follows standard JiBX deserializer usage requirements by accepting a
     * <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static DateMidnight deserializeLocalDateMidnight(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return deserializeDateMidnight(text, DateTimeZone.getDefault());
        }
    }

    /**
     * Deserialize date value from text as midnight date in UTC time zone. Date values are expected to match W3C XML
     * Schema standard format as CCYY-MM-DD, with optional leading sign and trailing time zone (with the time zone
     * ignored in this case). This method follows standard JiBX deserializer usage requirements by accepting a
     * <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static DateMidnight deserializeUTCDateMidnight(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return deserializeDateMidnight(text, DateTimeZone.UTC);
        }
    }

    /**
     * Serialize midnight date value to general date text without time zone. Date values are formatted in W3C XML Schema
     * standard format as CCYY-MM-DD, with optional leading sign included if necessary.
     *
     * @param date date to be converted
     * @return converted date text
     */
    public static String serializeUnzonedDateMidnight(DateMidnight date) {
        long millis = date.getMillis();
        int offset = date.getZone().getOffset(millis);
        return Utility.serializeDate(millis + offset);
    }

    /**
     * Serialize midnight date value to general date text with time zone offset. Date values are formatted in W3C XML
     * Schema standard format as CCYY-MM-DD, with optional leading sign included if necessary and trailing time zone
     * offset.
     *
     * @param date date to be converted
     * @return converted date text
     */
    public static String serializeZonedDateMidnight(DateMidnight date) {
        
        // get base time and offset
        long millis = date.getMillis();
        int offset = date.getZone().getOffset(millis);
        
        // start serialization with the year, month, and day
        StringBuffer buff = new StringBuffer(25);
        Utility.formatYearMonthDay((millis + offset) + Utility.TIME_BASE, buff);
        
        // finish with time zone offset
        Utility.serializeOffset(offset, buff);
        return buff.toString();
    }

    /**
     * Serialize midnight date value to general date text with UTC time zone. Date values are formatted in W3C XML
     * Schema standard format as CCYY-MM-DDZ, with optional leading sign included if necessary.
     *
     * @param date date to be converted
     * @return converted date text
     */
    public static String serializeUTCDateMidnight(DateMidnight date) {
        
        // get base time and offset
        long millis = date.getMillis();
        int offset = date.getZone().getOffset(millis);
        
        // start serialization with the year, month, and day
        StringBuffer buff = new StringBuffer(25);
        Utility.formatYearMonthDay((millis + offset) + Utility.TIME_BASE, buff);
        
        // finish with time zone offset
        buff.append('Z');
        return buff.toString();
    }
    
    /**
     * Deserialize time value from text as local time. Time values are expected to match W3C XML Schema standard format
     * as hh:mm:ss.fff, with optional trailing time zone (with the time zone ignored in this case). This method follows
     * standard JiBX deserializer usage requirements by accepting a <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static LocalTime deserializeLocalTime(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return new LocalTime(Utility.parseTimeNoOffset(text, 0, text.length()), DateTimeZone.UTC);
        }
    }

    /**
     * Serialize local time value to general date text without time zone. Time values are formatted in W3C XML Schema
     * standard format as hh:mm:ss.fff.
     *
     * @param time time to be converted
     * @return converted time text
     */
    public static String serializeUnzonedLocalTime(LocalTime time) {
        StringBuffer buff = new StringBuffer(12);
        Utility.serializeTime(time.getMillisOfDay(), buff);
        return buff.toString();
    }

    /**
     * Serialize local time value to general date text with UTC time zone. Time values are formatted in W3C XML Schema
     * standard format as hh:mm:ss.fffZ, with optional leading sign included if necessary.
     *
     * @param time time to be converted
     * @return converted time text
     */
    public static String serializeUTCLocalTime(LocalTime time) {
        StringBuffer buff = new StringBuffer(13);
        Utility.serializeTime(time.getMillisOfDay(), buff);
        buff.append('Z');
        return buff.toString();
    }

    /**
     * Parse date/time value from text. Date values are expected to match W3C XML Schema standard format as
     * CCYY-MM-DDThh:mm:ss.fff, with optional leading sign and trailing time zone (with the time zone used to convert
     * the value to the target time zone). If the time zone is absent and not required the target time zone is assumed.
     *
     * @param text
     * @param full zoned time required flag (exception thrown if missing)
     * @param zone time zone used for returned date/time
     * @return converted date/time
     * @throws JiBXException
     */
    protected static DateTime parseDateTime(String text, boolean full, DateTimeZone zone) throws JiBXException {
        
        // split text to convert portions separately
        int split = text.indexOf('T');
        if (split < 0) {
            throw new JiBXException("Missing 'T' separator in dateTime");
        }
        int length = text.length();
        long milli = Utility.parseDate(text.substring(0, split)) +
            Utility.parseTimeNoOffset(text, split+1, length);
        
        // check for time zone offset specified
        int start = split + 9;
        boolean utc = false;
        if (length > start) {
    
            // adjust for time zone
            utc = true;
            if (text.charAt(length-1) != 'Z') {
                char chr = text.charAt(length-6);
                if (chr == '-' || chr == '+') {
                    int hour = Utility.parseDigits(text, length-5, 2);
                    int minute = Utility.parseDigits(text, length-2, 2);
                    if (hour > 14 || minute > 59 || (hour == 14 && minute != 0)) {
                        throw new JiBXException("Invalid time zone offset: " + text);
                    } else {
                        int offset = ((hour*60)+minute)*60*1000;
                        if (chr == '-') {
                            milli += offset;
                        } else {
                            milli -= offset;
                        }
                    }
                }
            }
            
        } else if (full) {
            throw new JiBXException("Missing required time zone offset: " + text);
        }
        
        // convert to target time zone
        if (!utc) {
            milli -= zone.getOffset(milli);
        }
        return new DateTime(milli, zone);
    }

    /**
     * Deserialize date/time value from text in specified (or default) time zone. Date/time values are expected to match
     * W3C XML Schema standard format as CCYY-MM-DDThh:mm:ss.fff, with optional leading sign and trailing time zone. If 
     * the time zone indication is included a zone matching the offset is used for the date; otherwise the default time
     * zone is used. This method follows standard JiBX deserializer usage requirements by accepting a <code>null</code>
     * input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static DateTime deserializeZonedDateTime(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            DateTimeZone zone = findZone(text, DateTimeZone.getDefault());
            return parseDateTime(text, false, zone);
        }
    }
    
    /**
     * Deserialize UTC date/time value from text into the UTC time zone. Date values are expected to match W3C XML
     * Schema standard format as CCYY-MM-DDThh:mm:ss.fff, with optional leading sign and trailing time zone (with the
     * time zone used to convert the value to UTC, and UTC assumed if no time zone is specified). This method follows
     * standard JiBX deserializer usage requirements by accepting a <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static DateTime deserializeUTCDateTime(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return new DateTime(Utility.parseDateTime(text), DateTimeZone.UTC);
        }
    }
    
    /**
     * Deserialize date/time value from text into the local (default) time zone. Date values are expected to match W3C
     * XML Schema standard format as CCYY-MM-DDThh:mm:ss.fff, with optional leading sign and trailing time zone (with
     * the time zone used to convert the value to the local time zone, and the local time zone assumed if no time zone
     * is specified). This method follows standard JiBX deserializer usage requirements by accepting a <code>null</code>
     * input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static DateTime deserializeLocalDateTime(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return parseDateTime(text, false, DateTimeZone.getDefault());
        }
    }

    /**
     * Deserialize UTC date/time value from text with time zone required. Date values are expected to match W3C XML
     * Schema standard format as CCYY-MM-DDThh:mm:ss.fff, with optional leading sign but required trailing time zone
     * (with the time zone used to convert the value to UTC). This method follows standard JiBX deserializer usage
     * requirements by accepting a <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static DateTime deserializeStrictUTCDateTime(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return parseDateTime(text, true, DateTimeZone.UTC);
        }
    }
    
    /**
     * Deserialize local (default zone) date/time value from text with time zone required. Date values are expected to
     * match W3C XML Schema standard format as CCYY-MM-DDThh:mm:ss.fff, with optional leading sign but required trailing
     * time zone (with the time zone used to convert the value to the local time zone). This method follows standard
     * JiBX deserializer usage requirements by accepting a <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static DateTime deserializeStrictLocalDateTime(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return parseDateTime(text, true, DateTimeZone.getDefault());
        }
    }

    /**
     * Serialize date/time to general dateTime text. Date/time values are formatted in W3C XML Schema standard format as
     * CCYY-MM-DDThh:mm:ss.fff, with optional leading sign included if necessary. Time zone information is <i>always</i>
     * included in the output, with the value determined by the zone used for the date/time.
     *
     * @param time date/time to be converted
     * @return converted date/time text
     */
    public static String serializeZonedDateTime(DateTime time) {
        
        // get offset and adjusted time value
        int offset = time.getZone().getOffset(time.getMillis());
        long msec = time.getMillis() + offset;
        
        // start serialization with the year, month, and day
        StringBuffer buff = new StringBuffer(25);
        int extra = Utility.formatYearMonthDay(msec + Utility.TIME_BASE, buff);
        
        // append the time for full form
        buff.append('T');
        Utility.serializeTime(extra, buff);
        
        // finish with time zone offset
        Utility.serializeOffset(offset, buff);
        return buff.toString();
    }

    /**
     * Serialize date/time to general dateTime text. Date/time values are formatted in W3C XML Schema standard format as
     * CCYY-MM-DDThh:mm:ss.fff, with optional leading sign included if necessary. Time zone information is <i>always</i>
     * included in the output, as 'Z' to indicate UTC.
     *
     * @param time date/time to be converted
     * @return converted date/time text
     */
    public static String serializeUTCDateTime(DateTime time) {
        return Utility.serializeDateTime(time.getMillis(), true);
    }
}
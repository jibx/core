/*
Copyright (c) 2002-2010, Dennis M. Sosnoski. All rights reserved.

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

import java.lang.reflect.Array;
//#!j2me{
import java.sql.Time;
import java.sql.Timestamp;
//#j2me}
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Utility class supplying static methods. Date serialization is based on the
 * algorithms published by Peter Baum (http://www.capecod.net/~pbaum). All date
 * handling is done according to the W3C Schema specification, which uses a
 * proleptic Gregorian calendar with no year 0. Note that this differs from the
 * Java date handling, which uses a discontinuous Gregorian calendar.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class Utility
{
    /** Empty array of strings. */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    /** Minimum size for array returned by {@link #growArray}. */
    public static final int MINIMUM_GROWN_ARRAY_SIZE = 16;
    
    /** Number of milliseconds in a minute. */
    private static final int MSPERMINUTE = 60000;
    
    /** Number of milliseconds in an hour. */
    private static final int MSPERHOUR = MSPERMINUTE*60;
    
    /** Number of milliseconds in a day. */
    private static final int MSPERDAY = MSPERHOUR*24;
    
    /** Number of milliseconds in a day as a long. */
    private static final long LMSPERDAY = (long)MSPERDAY;
    
    /** Number of milliseconds in a (non-leap) year. */
    private static final long MSPERYEAR = LMSPERDAY*365;
    
    /** Average number of milliseconds in a year within century. */
    private static final long MSPERAVGYEAR = (long)(MSPERDAY*365.25);
    
    /** Number of milliseconds in a normal century. */
    private static final long MSPERCENTURY = (long)(MSPERDAY*36524.25);
    
    /** Millisecond value of base time for internal representation. This gives
     the bias relative to January 1 of the year 1 C.E. */
    static final long TIME_BASE = 1969*MSPERYEAR + (1969/4 - 19 + 4)*LMSPERDAY;
    
    /** Day number for start of month in non-leap year. */
    static final int[] MONTHS_NONLEAP =
    {
        0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365
    };

    /** Day number for start of month in non-leap year. */
    static final int[] MONTHS_LEAP =
    {
        0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366
    };

    /** Millisecond count prior to start of month in March 1-biased year. */
    private static final long[] BIAS_MONTHMS =
    {
        0*LMSPERDAY, 0*LMSPERDAY, 0*LMSPERDAY, 0*LMSPERDAY,
        31*LMSPERDAY, 61*LMSPERDAY, 92*LMSPERDAY, 122*LMSPERDAY,
        153*LMSPERDAY, 184*LMSPERDAY, 214*LMSPERDAY, 245*LMSPERDAY,
        275*LMSPERDAY, 306*LMSPERDAY, 337*LMSPERDAY
    };
    
    /** Date for setting change to Gregorian calendar. */
    private static Date BEGINNING_OF_TIME = new Date(Long.MIN_VALUE);
    
    /** Pad character for base64 encoding. */
	private static final char PAD_CHAR = '=';
    
    /** Characters used in base64 encoding. */
    private static final char[] s_base64Chars = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
		'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
		'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
		'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
		'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
		'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', '+', '/'
	};
    
    /** Values corresponding to characters used in bas64 encoding. */
	private static final byte[] s_base64Values = new byte[128];
    static {
        for (int i = 0; i < s_base64Values.length; i++) {
            s_base64Values[i] = -1;
        }
        s_base64Values[PAD_CHAR] = 0;
        for (int i = 0; i < s_base64Chars.length; i++) {
            s_base64Values[s_base64Chars[i]] = (byte)i;
        }
    }

    /**
     * Parse digits in text as integer value. This internal method is used
     * for number values embedded within lexical structures. Only decimal
     * digits can be included in the text range parsed.
     *
     * @param text text to be parsed
     * @param offset starting offset in text
     * @param length number of digits to be parsed
     * @return converted positive integer value
     * @throws JiBXException on parse error
     */
    static int parseDigits(String text, int offset, int length)
        throws JiBXException {

        // check if overflow a potential problem
        int value = 0;
        if (length > 9) {

            // use library parse code for potential overflow
            try {
                value = Integer.parseInt(text.substring(offset, offset+length));
            } catch (NumberFormatException ex) {
                throw new JiBXException(ex.getMessage());
            }

        } else {

            // parse with no overflow worries
            int limit = offset + length;
            while (offset < limit) {
                char chr = text.charAt(offset++);
                if (chr >= '0' && chr <= '9') {
                    value = value * 10 + (chr - '0');
                } else {
                    throw new JiBXException("Non-digit in number value");
                }
            }

        }
        return value;
    }

    /**
     * Parse integer value from text. Integer values are parsed with optional
     * leading sign flag, followed by any number of digits.
     *
     * @param text text to be parsed
     * @return converted integer value
     * @throws JiBXException on parse error
     */
    public static int parseInt(String text) throws JiBXException {

        // make sure there's text to be processed
        text = text.trim();
        int offset = 0;
        int limit = text.length();
        if (limit == 0) {
            throw new JiBXException("Empty number value");
        }

        // check leading sign present in text
        boolean negate = false;
        char chr = text.charAt(0);
        if (chr == '-') {
            if (limit > 9) {

                // special case to make sure maximum negative value handled
                try {
                    return Integer.parseInt(text);
                } catch (NumberFormatException ex) {
                    throw new JiBXException(ex.getMessage());
                }

            } else {
                negate = true;
                offset++;
            }
        } else if (chr == '+') {
            offset++;
        }
        if (offset >= limit) {
            throw new JiBXException("Invalid number format");
        }

        // handle actual value conversion
        int value = parseDigits(text, offset, limit-offset);
        if (negate) {
            return -value;
        } else {
            return value;
        }
    }
    
    /**
     * Serialize int value to text.
     *
     * @param value int value to be serialized
     * @return text representation of value
     */
    public static String serializeInt(int value) {
        return Integer.toString(value);
    }
    
    /**
     * Check if a text string is a valid boolean representation.
     *
     * @param text
     * @return <code>true</code> if valid boolean, <code>false</code> if not
     */
    public static boolean ifBoolean(String text) {
        return "0".equals(text) || "1".equals(text) || "true".equals(text) ||
            "false".equals(text);
    }

    /**
     * Parse long value from text. Long values are parsed with optional
     * leading sign flag, followed by any number of digits.
     *
     * @param text text to be parsed
     * @return converted long value
     * @throws JiBXException on parse error
     */
    public static long parseLong(String text) throws JiBXException {

        // make sure there's text to be processed
        text = text.trim();
        int offset = 0;
        int limit = text.length();
        if (limit == 0) {
            throw new JiBXException("Empty number value");
        }

        // check leading sign present in text
        boolean negate = false;
        char chr = text.charAt(0);
        if (chr == '-') {
            negate = true;
            offset++;
        } else if (chr == '+') {
            offset++;
        }
        if (offset >= limit) {
            throw new JiBXException("Invalid number format");
        }

        // check if overflow a potential problem
        long value = 0;
        if (limit-offset > 18) {

            // pass text to library parse code (less leading +)
            if (chr == '+') {
                text = text.substring(1);
            }
            try {
                value = Long.parseLong(text);
            } catch (NumberFormatException ex) {
                throw new JiBXException(ex.getMessage());
            }

        } else {

            // parse with no overflow worries
            while (offset < limit) {
                chr = text.charAt(offset++);
                if (chr >= '0' && chr <= '9') {
                    value = value * 10 + (chr - '0');
                } else {
                    throw new JiBXException("Non-digit in number value");
                }
            }
            if (negate) {
                value = -value;
            }

        }
        return value;
    }

    /**
     * Serialize long value to text.
     *
     * @param value long value to be serialized
     * @return text representation of value
     */
    public static String serializeLong(long value) {
        return Long.toString(value);
    }

    /**
     * Convert gYear text to Java date. Date values are expected to be in
     * W3C XML Schema standard format as CCYY, with optional leading sign.
     *
     * @param text text to be parsed
     * @return start of year date as millisecond value from 1 C.E.
     * @throws JiBXException on parse error
     */
    public static long parseYear(String text) throws JiBXException {
    
        // start by validating the length
        text = text.trim();
        boolean valid = true;
        int minc = 4;
        char chr = text.charAt(0);
        if (chr == '-') {
            minc = 5;
        } else if (chr == '+') {
            valid = false;
        }
        if (text.length() < minc) {
            valid = false;
        }
        if (!valid) {
            throw new JiBXException("Invalid year format");
        }
    
        // handle year conversion
        int year = parseInt(text);
        if (year == 0) {
            throw new JiBXException("Year value 0 is not allowed");
        }
        if (year > 0) {
            year--;
        }
        long day = ((long)year)*365 + year/4 - year/100 + year/400;
        return day*MSPERDAY - TIME_BASE;
    }

    /**
     * Parse short value from text. Short values are parsed with optional
     * leading sign flag, followed by any number of digits.
     *
     * @param text text to be parsed
     * @return converted short value
     * @throws JiBXException on parse error
     */
    public static short parseShort(String text) throws JiBXException {
        int value = parseInt(text);
        if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
            throw new JiBXException("Value out of range");
        }
        return (short)value;
    }

    /**
     * Serialize short value to text.
     *
     * @param value short value to be serialized
     * @return text representation of value
     */
    public static String serializeShort(short value) {
        return Short.toString(value);
    }

    /**
     * Parse byte value from text. Byte values are parsed with optional
     * leading sign flag, followed by any number of digits.
     *
     * @param text text to be parsed
     * @return converted byte value
     * @throws JiBXException on parse error
     */
    public static byte parseByte(String text) throws JiBXException {
        int value = parseInt(text);
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            throw new JiBXException("Value out of range");
        }
        return (byte)value;
    }

    /**
     * Serialize byte value to text.
     *
     * @param value byte value to be serialized
     * @return text representation of value
     */
    public static String serializeByte(byte value) {
        return Byte.toString(value);
    }

    /**
     * Parse boolean value from text. Boolean values are parsed as either text
     * "true" and "false", or "1" and "0" numeric equivalents.
     *
     * @param text text to be parsed
     * @return converted boolean value
     * @throws JiBXException on parse error
     */
    public static boolean parseBoolean(String text) throws JiBXException {
        text = text.trim();
        if ("true".equals(text) || "1".equals(text)) {
            return true;
        } else if ("false".equals(text) || "0".equals(text)) {
            return false;
        } else {
            throw new JiBXException("Invalid boolean value");
        }
    }

    /**
     * Deserialize boolean value from text. If the text is <code>null</code>
     * this just returns <code>null</code>; otherwise it returns the wrapped
     * parsed value.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted Boolean value
     * @throws JiBXException on parse error
     */
    public static Boolean deserializeBoolean(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return parseBoolean(text) ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    /**
     * Serialize boolean value to text. This serializes the value using the
     * text representation as "true" or "false".
     *
     * @param value boolean value to be serialized
     * @return text representation of value
     */
    public static String serializeBoolean(boolean value) {
        return value ? "true" : "false";
    }

    /**
     * Serialize boolean value to text. If the value is <code>null</code> this
     * just returns <code>null</code>; otherwise it serializes the value using
     * {@link #serializeBoolean(boolean)}.
     *
     * @param value value to be serialized
     * @return text representation of value
     */
    public static String serializeBoolean(Boolean value) {
        if (value == null) {
            return null;
        } else {
            return serializeBoolean(value.booleanValue());
        }
    }

    /**
     * Parse char value from text as unsigned 16-bit integer. Char values are
     * parsed with optional leading sign flag, followed by any number of digits.
     *
     * @param text text to be parsed
     * @return converted char value
     * @throws JiBXException on parse error
     */
    public static char parseChar(String text) throws JiBXException {
        int value = parseInt(text);
        if (value < Character.MIN_VALUE || value > Character.MAX_VALUE) {
            throw new JiBXException("Value out of range");
        }
        return (char)value;
    }

    /**
     * Serialize char value to text as unsigned 16-bit integer.
     *
     * @param value char value to be serialized
     * @return text representation of value
     */
    public static String serializeChar(char value) {
        return Integer.toString(value);
    }

    /**
     * Parse char value from text as character value. This requires that the
     * string must be of length one.
     *
     * @param text text to be parsed
     * @return converted char value
     * @throws JiBXException on parse error
     */
    public static char parseCharString(String text) throws JiBXException {
        if (text.length() == 1) {
            return text.charAt(0);
        } else {
            throw new JiBXException("Input must be a single character");
        }
    }

    /**
     * Deserialize char value from text as character value. This requires that
     * the string must be null or of length one.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted char value
     * @throws JiBXException on parse error
     */
    public static char deserializeCharString(String text) throws JiBXException {
        if (text == null) {
            return 0;
        } else {
            return parseCharString(text);
        }
    }

    /**
     * Serialize char value to text as string of length one.
     *
     * @param value char value to be serialized
     * @return text representation of value
     */
    public static String serializeCharString(char value) {
        return String.valueOf(value);
    }

    /**
     * Parse float value from text. This uses the W3C XML Schema format for
     * floats, with the exception that it will accept "+NaN" and "-NaN" as
     * valid formats. This is not in strict compliance with the specification,
     * but is included for interoperability with other Java XML processing.
     *
     * @param text text to be parsed
     * @return converted float value
     * @throws JiBXException on parse error
     */
    public static float parseFloat(String text) throws JiBXException {
        text = text.trim();
        if ("-INF".equals(text)) {
            return Float.NEGATIVE_INFINITY;
        } else if ("INF".equals(text)) {
            return Float.POSITIVE_INFINITY;
        } else {
            try {
                return Float.parseFloat(text);
            } catch (NumberFormatException ex) {
                throw new JiBXException(ex.getMessage());
            }
        }
    }

    /**
     * Serialize float value to text.
     *
     * @param value float value to be serialized
     * @return text representation of value
     */
    public static String serializeFloat(float value) {
        if (Float.isInfinite(value)) {
            return (value < 0.0f) ? "-INF" : "INF";
        } else {
            return Float.toString(value);
        }
    }

    /**
     * Parse double value from text. This uses the W3C XML Schema format for
     * doubles, with the exception that it will accept "+NaN" and "-NaN" as
     * valid formats. This is not in strict compliance with the specification,
     * but is included for interoperability with other Java XML processing.
     *
     * @param text text to be parsed
     * @return converted double value
     * @throws JiBXException on parse error
     */
    public static double parseDouble(String text) throws JiBXException {
        text = text.trim();
        if ("-INF".equals(text)) {
            return Double.NEGATIVE_INFINITY;
        } else if ("INF".equals(text)) {
            return Double.POSITIVE_INFINITY;
        } else {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ex) {
                throw new JiBXException(ex.getMessage());
            }
        }
    }

    /**
     * Serialize double value to text.
     *
     * @param value double value to be serialized
     * @return text representation of value
     */
    public static String serializeDouble(double value) {
        if (Double.isInfinite(value)) {
            return (value < 0.0f) ? "-INF" : "INF";
        } else {
            return Double.toString(value);
        }
    }

    /**
     * Convert gYearMonth text to Java date. Date values are expected to be in
     * W3C XML Schema standard format as CCYY-MM, with optional
     * leading sign.
     *
     * @param text text to be parsed
     * @return start of month in year date as millisecond value
     * @throws JiBXException on parse error
     */
    public static long parseYearMonth(String text) throws JiBXException {

        // start by validating the length and basic format
        text = text.trim();
        boolean valid = true;
        int minc = 7;
        char chr = text.charAt(0);
        if (chr == '-') {
            minc = 8;
        } else if (chr == '+') {
            valid = false;
        }
        int split = text.length() - 3;
        if (text.length() < minc) {
            valid = false;
        } else {
            if (text.charAt(split) != '-') {
                valid = false;
            }
        }
        if (!valid) {
            throw new JiBXException("Invalid date format");
        }

        // handle year and month conversion
        int year = parseInt(text.substring(0, split));
        if (year == 0) {
            throw new JiBXException("Year value 0 is not allowed");
        }
        int month = parseDigits(text, split+1, 2) - 1;
        if (month < 0 || month > 11) {
            throw new JiBXException("Month value out of range");
        }
        boolean leap = (year%4 == 0) && !((year%100 == 0) && (year%400 != 0));
        if (year > 0) {
            year--;
        }
        long day = ((long)year)*365 + year/4 - year/100 + year/400 +
            (leap ? MONTHS_LEAP : MONTHS_NONLEAP)[month];
        return  day*MSPERDAY - TIME_BASE;
    }

    /**
     * Convert date text to Java date. Date values are expected to be in
     * W3C XML Schema standard format as CCYY-MM-DD, with optional
     * leading sign and trailing time zone (though the time zone is ignored
     * in this case).
     * 
     * Note that the returned value is based on UTC, which matches the
     * definition of <code>java.util.Date</code> but will typically not be the
     * expected value if you're using a <code>java.util.Calendar</code> on the
     * result. In this case you probably want to instead use {@link
     * #deserializeSqlDate(String)}, which <i>does</i> adjust the value to match
     * the local time zone.
     *
     * @param text text to be parsed
     * @return start of day in month and year date as millisecond value
     * @throws JiBXException on parse error
     */
    public static long parseDate(String text) throws JiBXException {

        // start by validating the length and basic format
        if (!ifDate(text)) {
            throw new JiBXException("Invalid date format");
        }

        // handle year, month, and day conversion
        int split = text.indexOf('-', 1);
        int year = parseInt(text.substring(0, split));
        if (year == 0) {
            throw new JiBXException("Year value 0 is not allowed");
        }
        int month = parseDigits(text, split+1, 2) - 1;
        if (month < 0 || month > 11) {
            throw new JiBXException("Month value out of range");
        }
        long day = parseDigits(text, split+4, 2) - 1;
        boolean leap = (year%4 == 0) && !((year%100 == 0) && (year%400 != 0));
        int[] starts = leap ? MONTHS_LEAP : MONTHS_NONLEAP;
        if (day < 0 || day >= (starts[month+1]-starts[month])) {
            throw new JiBXException("Day value out of range");
        }
        if (year > 0) {
            year--;
        }
        day += ((long)year)*365 + year/4 - year/100 + year/400 + starts[month];
        return day*MSPERDAY - TIME_BASE;
    }

    /**
     * Deserialize date from text. Date values are expected to match W3C XML
     * Schema standard format as CCYY-MM-DD, with optional leading sign and
     * trailing time zone (though the time zone is ignored in this case). This
     * method follows standard JiBX deserializer usage requirements by accepting
     * a <code>null</code> input.
     * 
     * Note that the returned value is based on UTC, which matches the
     * definition of <code>java.util.Date</code> but will typically not be the
     * expected value if you're using a <code>java.util.Calendar</code> on the
     * result. In this case you probably want to instead use {@link
     * #deserializeSqlDate(String)}, which <i>does</i> adjust the value to match
     * the local time zone.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code>
     * input
     * @throws JiBXException on parse error
     */
    public static Date deserializeDate(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return new Date(parseDate(text));
        }
    }

//#!j2me{
    /**
     * Deserialize SQL date from text. Date values are expected to match W3C XML
     * Schema standard format as CCYY-MM-DD, with optional leading sign and
     * trailing time zone (though the time zone is ignored in this case). This
     * method follows standard JiBX deserializer usage requirements by accepting
     * a <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code>
     * input
     * @throws JiBXException on parse error
     */
    public static java.sql.Date deserializeSqlDate(String text)
        throws JiBXException {
        if (text == null) {
            return null;
        } else {
            
            // make sure date is valid
            if (!ifDate(text)) {
                throw new JiBXException("Invalid date format");
            }

            // handle year, month, and day conversion
            int split = text.indexOf('-', 1);
            int year = parseInt(text.substring(0, split));
            if (year == 0) {
                throw new JiBXException("Year value 0 is not allowed");
            }
            int month = parseDigits(text, split+1, 2) - 1;
            if (month < 0 || month > 11) {
                throw new JiBXException("Month value out of range");
            }
            int day = parseDigits(text, split+4, 2) - 1;
            boolean leap = (year%4 == 0) && !((year%100 == 0) && (year%400 != 0));
            int[] starts = leap ? MONTHS_LEAP : MONTHS_NONLEAP;
            if (day < 0 || day >= (starts[month+1]-starts[month])) {
                throw new JiBXException("Day value out of range");
            }
            if (year < 0) {
                year++;
            }
            
            // set it into a calendar
            GregorianCalendar cal;
            if (year < 1800) {
                cal = new GregorianCalendar();
                cal.setGregorianChange(BEGINNING_OF_TIME);
                cal.clear();
                cal.set(year, month, day+1);
            } else {
                cal = new GregorianCalendar(year, month, day+1);
            }
            return new java.sql.Date(cal.getTime().getTime());
        }
    }
//#j2me}

    /**
     * Parse general time value from text. Time values are expected to be in W3C
     * XML Schema standard format as hh:mm:ss.fff, with optional leading sign
     * and trailing time zone. This method ignores any time zone offset, just
     * converting the value as supplied.
     *
     * @param text text to be parsed
     * @param start offset of first character of time value
     * @param length number of characters in time value
     * @return converted time as millisecond value
     * @throws JiBXException on parse error
     */
    public static long parseTimeNoOffset(String text, int start, int length)
        throws JiBXException {

        // validate time value following date
        long milli = 0;
        boolean valid = length > (start+7) &&
            (text.charAt(start+2) == ':') &&
            (text.charAt(start+5) == ':');
        if (valid) {
            int hour = parseDigits(text, start, 2);
            int minute = parseDigits(text, start+3, 2);
            int second = parseDigits(text, start+6, 2);
            if (hour > 24 || minute > 59 || second > 60 ||
                (hour == 24 && (minute != 0 || second != 0))) {
                valid = false;
            } else {

                // convert to base millisecond in day
                milli = (((hour*60)+minute)*60+second)*1000;
                int base = start + 8;
                if (length > base) {

                    // check for trailing fractional second
                    if (text.charAt(base) == '.') {
                        int end = base + 1;
                        while (end < length) {
                            char chr = text.charAt(end);
                            if (chr < '0' || chr > '9') {
                                break;
                            } else {
                                end++;
                            }
                        }
                        double fraction = Double.parseDouble
                            (text.substring(base, end));
                        valid = hour < 24 || fraction == 0.0;
                        milli += fraction*1000.0;
                    }
                }
            }
        }

        // check for valid result
        if (valid) {
            return milli;
        } else {
            throw new JiBXException("Invalid time format: " + text);
        }
    }

    /**
     * Parse general time value from text. Time values are expected to be in W3C
     * XML Schema standard format as hh:mm:ss.fff, with optional leading sign
     * and trailing time zone. This method always adjusts the returned value to
     * UTC if a time zone offset is supplied.
     *
     * @param text text to be parsed
     * @param start offset of first character of time value
     * @param length number of characters in time value
     * @return converted time as millisecond value
     * @throws JiBXException on parse error
     */
    public static long parseTime(String text, int start, int length)
        throws JiBXException {
        
        // parse time and check for offset value
        long milli = parseTimeNoOffset(text, start, length);
        start += 8;
        if (length > start) {

            // adjust for time zone
            if (text.charAt(length-1) == 'Z') {
                length--;
            } else {
                char chr = text.charAt(length-6);
                if (chr == '-' || chr == '+') {
                    int hour = parseDigits(text, length-5, 2);
                    int minute = parseDigits(text, length-2, 2);
                    if (hour > 14 || minute > 59 || (hour == 14 && minute != 0)) {
                        throw new JiBXException("Invalid time zone offset: " +
                            text);
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
        }
        return milli;
    }

    /**
     * Parse general dateTime value from text. Date values are expected to be in
     * W3C XML Schema standard format as CCYY-MM-DDThh:mm:ss.fff, with optional
     * leading sign and trailing time zone. This method always adjusts the
     * returned value to UTC if a time zone offset is supplied.
     *
     * @param text text to be parsed
     * @return converted date as millisecond value
     * @throws JiBXException on parse error
     */
    public static long parseDateTime(String text) throws JiBXException {

        // split text to convert portions separately
        int split = text.indexOf('T');
        if (split < 0) {
            throw new JiBXException("Missing 'T' separator in dateTime");
        }
        return parseDate(text.substring(0, split)) +
            parseTime(text, split+1, text.length());
    }

    /**
     * Deserialize date from general dateTime text. Date values are expected to
     * match W3C XML Schema standard format as CCYY-MM-DDThh:mm:ss, with
     * optional leading minus sign and trailing seconds decimal, as necessary.
     * This method follows standard JiBX deserializer usage requirements by
     * accepting a <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted date, or <code>null</code> if passed <code>null</code>
     * input
     * @throws JiBXException on parse error
     */
    public static Date deserializeDateTime(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return new Date(parseDateTime(text));
        }
    }

//#!j2me{
    /**
     * Deserialize timestamp from general dateTime text. Timestamp values are
     * represented in the same way as regular dates, but allow more precision in
     * the fractional second value (down to nanoseconds). This method follows
     * standard JiBX deserializer usage requirements by accepting a
     * <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted timestamp, or <code>null</code> if passed
     * <code>null</code> input
     * @throws JiBXException on parse error
     */
    public static Timestamp deserializeTimestamp(String text)
        throws JiBXException {
        if (text == null) {
            return null;
        } else {
            
            // check for fractional second value present
            int split = text.indexOf('.');
            int nano = 0;
            if (split > 0) {
                
                // make sure there aren't multiple decimal points
                if (text.indexOf('.', split+1) > 0) {
                    throw new JiBXException("Not a valid timestamp value");
                }
                
                // scan through all digits following decimal point
                int limit = text.length();
                int scan = split;
                while (++scan < limit) {
                    char chr = text.charAt(scan);
                    if (chr < '0' || chr > '9') {
                        break;
                    }
                }
                
                // parse digits following decimal point
                int length = scan - split - 1;
                if (length > 9) {
                    length = 9;
                }
                nano = parseDigits(text, split+1, length);
                
                // convert to number of nanoseconds
                while (length < 9) {
                    nano *= 10;
                    length++;
                }
                
                // strip fractional second off text
                if (scan < limit) {
                    text = text.substring(0, split) + text.substring(scan);
                } else {
                    text = text.substring(0, split);
                }
            }
            
            // return timestamp value with nanoseconds
            Timestamp stamp = new Timestamp(parseDateTime(text));
            stamp.setNanos(nano);
            return stamp;
        }
    }

    /**
     * Deserialize time from text. Time values obey the rules of the time
     * portion of a dataTime value. This method follows standard JiBX
     * deserializer usage requirements by accepting a <code>null</code> input.
     *
     * @param text text to be parsed (may be <code>null</code>)
     * @return converted time, or <code>null</code> if passed <code>null</code>
     * input
     * @throws JiBXException on parse error
     */
    public static Time deserializeSqlTime(String text)
        throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return new Time(parseTime(text, 0, text.length()));
        }
    }
//#j2me}

    /**
     * Format year number consistent with W3C XML Schema definitions, using a
     * minimum of four digits padded with zeros if necessary. A leading minus
     * sign is included for years prior to 1 C.E.
     *
     * @param year number to be formatted
     * @param buff text formatting buffer
     */
    protected static void formatYearNumber(long year, StringBuffer buff) {
        
        // start with minus sign for dates prior to 1 C.E.
        if (year <= 0) {
            buff.append('-');
            year = -(year-1);
        }
        
        // add padding if needed to bring to length of four
        if (year < 1000) {
            buff.append('0');
            if (year < 100) {
                buff.append('0');
                if (year < 10) {
                    buff.append('0');
                }
            }
        }
        
        // finish by converting the actual year number
        buff.append(year);
    }

    /**
     * Format a positive number as two digits. This uses an optional leading
     * zero digit for values less than ten.
     *
     * @param value number to be formatted (<code>0</code> to <code>99</code>)
     * @param buff text formatting buffer
     */
    protected static void formatTwoDigits(int value, StringBuffer buff) {
        if (value < 10) {
            buff.append('0');
        }
        buff.append(value);
    }

    /**
     * Format time in milliseconds to year number. The resulting year number
     * format is consistent with W3C XML Schema definitions, using a minimum
     * of four digits padded with zeros if necessary. A leading minus sign is
     * included for years prior to 1 C.E.
     *
     * @param value time in milliseconds to be converted (from 1 C.E.)
     * @param buff text formatting buffer
     */
    protected static void formatYear(long value, StringBuffer buff) {
        
        // find the actual year and month number; this uses a integer arithmetic
        //  conversion based on Baum, first making the millisecond count
        //  relative to March 1 of the year 0 C.E., then using simple arithmetic
        //  operations to compute century, year, and month; it's slightly
        //  different for pre-C.E. values because of Java's handling of divisions.
        long time = value + 306*LMSPERDAY + LMSPERDAY*3/4;
        long century = time / MSPERCENTURY;             // count of centuries
        long adjusted = time + (century - (century/4)) * MSPERDAY;
        int year = (int)(adjusted / MSPERAVGYEAR);      // year in March 1 terms
        if (adjusted < 0) {
            year--;
        }
        long yms = adjusted + LMSPERDAY/4 - (year * 365 + year/4) * LMSPERDAY;
        int yday = (int)(yms / LMSPERDAY);              // day number in year
        int month = (5*yday + 456) / 153;               // (biased) month number
        if (month > 12) {                               // convert start of year
            year++;
        }
        
        // format year to text
        formatYearNumber(year, buff);
    }

    /**
     * Format time in milliseconds to year number and month number. The 
     * resulting year number format is consistent with W3C XML Schema
     * definitions, using a minimum of four digits for the year and exactly
     * two digits for the month.
     *
     * @param value time in milliseconds to be converted (from 1 C.E.)
     * @param buff text formatting buffer
     * @return number of milliseconds into month
     */
    protected static long formatYearMonth(long value, StringBuffer buff) {
        
        // find the actual year and month number; this uses a integer arithmetic
        //  conversion based on Baum, first making the millisecond count
        //  relative to March 1 of the year 0 C.E., then using simple arithmetic
        //  operations to compute century, year, and month; it's slightly
        //  different for pre-C.E. values because of Java's handling of divisions.
        long time = value + 306*LMSPERDAY + LMSPERDAY*3/4;
        long century = time / MSPERCENTURY;             // count of centuries
        long adjusted = time + (century - (century/4)) * MSPERDAY;
        int year = (int)(adjusted / MSPERAVGYEAR);      // year in March 1 terms
        if (adjusted < 0) {
            year--;
        }
        long yms = adjusted + LMSPERDAY/4 - (year * 365 + year/4) * LMSPERDAY;
        int yday = (int)(yms / LMSPERDAY);              // day number in year
        if (yday == 0) {                                // special for negative
            boolean bce = year < 0;
            if (bce) {
                year--;
            }
            int dcnt = year % 4 == 0 ? 366 : 365;
            if (!bce) {
                year--;
            }
            yms += dcnt * LMSPERDAY;
            yday += dcnt;
        }
        int month = (5*yday + 456) / 153;               // (biased) month number
        long rem = yms - BIAS_MONTHMS[month] - LMSPERDAY;   // ms into month
        if (month > 12) {                               // convert start of year
            year++;
            month -= 12;
        }
        
        // format year and month as text
        formatYearNumber(year, buff);
        buff.append('-');
        formatTwoDigits(month, buff);
        
        // return extra milliseconds into month
        return rem;
    }

    /**
     * Format time in milliseconds to year number, month number, and day
     * number. The resulting year number format is consistent with W3C XML
     * Schema definitions, using a minimum of four digits for the year and
     * exactly two digits each for the month and day.
     *
     * @param value time in milliseconds to be converted (from 1 C.E.)
     * @param buff text formatting buffer
     * @return number of milliseconds into day
     */
    protected static int formatYearMonthDay(long value, StringBuffer buff) {
        
        // convert year and month
        long extra = formatYearMonth(value, buff);
        
        // append the day of month
        int day = (int)(extra / MSPERDAY) + 1;
        buff.append('-');
        formatTwoDigits(day, buff);
        
        // return excess of milliseconds into day
        return (int)(extra % MSPERDAY);
    }

    /**
     * Serialize time to general gYear text. Date values are formatted in
     * W3C XML Schema standard format as CCYY, with optional
     * leading sign included if necessary.
     *
     * @param time time to be converted, as milliseconds from January 1, 1970
     * @return converted gYear text
     */
    public static String serializeYear(long time) {
        StringBuffer buff = new StringBuffer(6);
        formatYear(time + TIME_BASE, buff);
        return buff.toString();
    }

    /**
     * Serialize date to general gYear text. Date values are formatted in
     * W3C XML Schema standard format as CCYY, with optional
     * leading sign included if necessary.
     *
     * @param date date to be converted
     * @return converted gYear text
     */
    public static String serializeYear(Date date) {
        return serializeYear(date.getTime());
    }

    /**
     * Serialize time to general gYearMonth text. Date values are formatted in
     * W3C XML Schema standard format as CCYY-MM, with optional
     * leading sign included if necessary.
     *
     * @param time time to be converted, as milliseconds from January 1, 1970
     * @return converted gYearMonth text
     */
    public static String serializeYearMonth(long time) {
        StringBuffer buff = new StringBuffer(12);
        formatYearMonth(time + TIME_BASE, buff);
        return buff.toString();
    }

    /**
     * Serialize date to general gYearMonth text. Date values are formatted in
     * W3C XML Schema standard format as CCYY-MM, with optional
     * leading sign included if necessary.
     *
     * @param date date to be converted
     * @return converted gYearMonth text
     */
    public static String serializeYearMonth(Date date) {
        return serializeYearMonth(date.getTime());
    }

    /**
     * Serialize time to general date text. Date values are formatted in
     * W3C XML Schema standard format as CCYY-MM-DD, with optional
     * leading sign included if necessary.
     *
     * @param time time to be converted, as milliseconds from January 1, 1970
     * @return converted date text
     */
    public static String serializeDate(long time) {
        StringBuffer buff = new StringBuffer(12);
        formatYearMonthDay(time + TIME_BASE, buff);
        return buff.toString();
    }

    /**
     * Serialize date to general date text. Date values are formatted in
     * W3C XML Schema standard format as CCYY-MM-DD, with optional
     * leading sign included if necessary.
     * 
     * Note that the conversion is based on UTC, which matches the
     * definition of <code>java.util.Date</code> but may not match the value as
     * serialized using <code>java.util.Calendar</code> (which assumes values
     * are in the local time zone). To work with values in the local time zone
     * you want to instead use {@link #serializeSqlDate(java.sql.Date)}.
     *
     * @param date date to be converted
     * @return converted date text
     */
    public static String serializeDate(Date date) {
        return serializeDate(date.getTime());
    }

//#!j2me{
    /**
     * Serialize SQL date to general date text. Date values are formatted in
     * W3C XML Schema standard format as CCYY-MM-DD, with optional
     * leading sign included if necessary. This interprets the date value with
     * respect to the local time zone, as fits the definition of the
     * <code>java.sql.Date</code> type.
     *
     * @param date date to be converted
     * @return converted date text
     */
    public static String serializeSqlDate(java.sql.Date date) {
        
        // values should be normalized to midnight in zone, but no guarantee
        //  so convert using the lame calendar approach
        GregorianCalendar cal = new GregorianCalendar();
        cal.setGregorianChange(BEGINNING_OF_TIME);
        cal.setTime(date);
        StringBuffer buff = new StringBuffer(12);
        int year = cal.get(Calendar.YEAR);
        if (date.getTime() < 0) {
            if (cal.get(Calendar.ERA) == GregorianCalendar.BC) {
                year = -year + 1;
            }
        }
        formatYearNumber(year, buff);
        buff.append('-');
        formatTwoDigits(cal.get(Calendar.MONTH)+1, buff);
        buff.append('-');
        formatTwoDigits(cal.get(Calendar.DAY_OF_MONTH), buff);
        return buff.toString();
    }
//#j2me}

    /**
     * Serialize time zone offset to buffer. Time zone offset values are always
     * formatted in W3C XML Schema standard format as +/-hh:mm (even if the
     * offset is zero).
     *
     * @param offset milliseconds to be subtracted to get UTC from local time
     * @param buff buffer for appending time text
     */
    public static void serializeExplicitOffset(int offset, StringBuffer buff) {
        
        // start with sign of offset, and convert to absolute value
        int abs;
        if (offset < 0) {
            abs = -offset;
            buff.append('-');
        } else {
            abs = offset;
            buff.append('+');
        }
        
        // append the hour and minute
        formatTwoDigits(abs/MSPERHOUR, buff);
        abs = abs % MSPERHOUR;
        buff.append(':');
        formatTwoDigits(abs/MSPERMINUTE, buff);
        
    }

    /**
     * Serialize time zone offset to buffer. Time zone offset values are
     * formatted in W3C XML Schema standard format as +/-hh:mm, with 'Z' used if
     * the offset is zero.
     *
     * @param offset milliseconds to be subtracted to get UTC from local time
     * @param buff buffer for appending time text
     */
    public static void serializeOffset(int offset, StringBuffer buff) {
        if (offset == 0) {
            buff.append('Z');
        } else {
            serializeExplicitOffset(offset, buff);
        }
    }

    /**
     * Serialize time to general time text in buffer. Time values are formatted
     * in W3C XML Schema standard format as hh:mm:ss, with optional trailing
     * seconds decimal, as necessary. This form uses a supplied buffer to
     * support flexible use, including with dateTime combination values.
     *
     * @param time time to be converted, as milliseconds in day
     * @param buff buffer for appending time text
     */
    public static void serializeTime(int time, StringBuffer buff) {
        
        // append the hour, minute, and second
        formatTwoDigits(time/MSPERHOUR, buff);
        time = time % MSPERHOUR;
        buff.append(':');
        formatTwoDigits(time/MSPERMINUTE, buff);
        time = time % MSPERMINUTE;
        buff.append(':');
        formatTwoDigits(time/1000, buff);
        time = time % 1000;
        
        // check if decimals needed on second
        if (time > 0) {
            buff.append('.');
            buff.append(time / 100);
            time = time % 100;
            if (time > 0) {
                buff.append(time / 10);
                time = time % 10;
                if (time > 0) {
                    buff.append(time);
                }
            }
        }
    }

    /**
     * Serialize time to general dateTime text. Date values are formatted in
     * W3C XML Schema standard format as CCYY-MM-DDThh:mm:ss, with optional
     * leading sign and trailing seconds decimal, as necessary.
     *
     * @param time time to be converted, as milliseconds from January 1, 1970
     * @param zone flag for trailing 'Z' to be appended to indicate UTC
     * @return converted dateTime text
     */
    public static String serializeDateTime(long time, boolean zone) {
        
        // start with the year, month, and day
        StringBuffer buff = new StringBuffer(25);
        int extra = formatYearMonthDay(time + TIME_BASE, buff);
        
        // append the time for full form
        buff.append('T');
        serializeTime(extra, buff);
        
        // return full text with optional trailing zone indicator
        if (zone) {
            buff.append('Z');
        }
        return buff.toString();
    }

    /**
     * Serialize time to general dateTime text. This method is provided for
     * backward compatibility. It generates the dateTime text without the
     * trailing 'Z' to indicate UTC.
     *
     * @param time time to be converted, as milliseconds from January 1, 1970
     * @return converted dateTime text
     */
    public static String serializeDateTime(long time) {
        return serializeDateTime(time, false);
    }

    /**
     * Serialize date to general dateTime text. Date values are formatted in
     * W3C XML Schema standard format as CCYY-MM-DDThh:mm:ssZ, with optional
     * leading sign and trailing seconds decimal, as necessary.
     *
     * @param date date to be converted
     * @return converted dateTime text
     */
    public static String serializeDateTime(Date date) {
        return serializeDateTime(date.getTime(), true);
    }

//#!j2me{
    /**
     * Serialize timestamp to general dateTime text. Timestamp values are
     * represented in the same way as regular dates, but allow more precision in
     * the fractional second value (down to nanoseconds).
     *
     * @param stamp timestamp to be converted
     * @return converted dateTime text
     */
    public static String serializeTimestamp(Timestamp stamp) {
        
        // check for nanosecond value to be included
        int nano = stamp.getNanos();
        if (nano > 0) {
            
            // convert the number of nanoseconds to text
            String value = serializeInt(nano);
            
            // pad with leading zeros if less than 9 digits
            StringBuffer digits = new StringBuffer(9);
            if (value.length() < 9) {
                int lead = 9 - value.length();
                for (int i = 0; i < lead; i++) {
                    digits.append('0');
                }
            }
            digits.append(value);
            
            // strip trailing zeros from value
            int last = 9;
            while (--last >= 0) {
                if (digits.charAt(last) != '0') {
                    break;
                }
            }
            digits.setLength(last+1);
            
            // finish by appending to rounded time with decimal separator
            long time = stamp.getTime();
            return serializeDateTime(time - time % 1000, false) + '.' +
                digits + 'Z';
            
        } else {
            return serializeDateTime(stamp.getTime(), true);
        }
    }

    /**
     * Serialize time to standard text. Time values are formatted in W3C XML
     * Schema standard format as hh:mm:ss, with optional trailing seconds
     * decimal, as necessary. The standard conversion does not append a time
     * zone indication.
     *
     * @param time time to be converted
     * @return converted time text
     */
    public static String serializeSqlTime(Time time) {
        StringBuffer buff = new StringBuffer(12);
        serializeTime((int)time.getTime(), buff);
        return buff.toString();
    }
//#j2me}
    
    /**
     * General object comparison method. Don't know why Sun hasn't seen fit to
     * include this somewhere, but at least it's easy to write (over and over
     * again).
     * 
     * @param a first object to be compared
     * @param b second object to be compared
     * @return <code>true</code> if both objects are <code>null</code>, or if
     * <code>a.equals(b)</code>; <code>false</code> otherwise
     */
    public static boolean isEqual(Object a, Object b) {
        return (a == null) ? b == null : a.equals(b);
    }

    /**
     * Find text value in enumeration. This first does a binary search through
     * an array of allowed text matches. If a separate array of corresponding
     * values is supplied, the value at the matched position is returned;
     * otherwise the match index is returned directly.
     *
     * @param target text to be found in enumeration
     * @param enums ordered array of texts included in enumeration
     * @param vals array of values to be returned for corresponding text match
     * positions (position returned directly if this is <code>null</code>)
     * @return enumeration value for target text
     * @throws JiBXException if target text not found in enumeration
     */
    public static int enumValue(String target, String[] enums, int[] vals)
        throws JiBXException {
        int base = 0;
        int limit = enums.length - 1;
        while (base <= limit) {
            int cur = (base + limit) >> 1;
            int diff = target.compareTo(enums[cur]);
            if (diff < 0) {
                limit = cur - 1;
            } else if (diff > 0) {
                base = cur + 1;
            } else if (vals != null) {
                return vals[cur];
            } else {
                return cur;
            }
        }
        throw new JiBXException("Target value \"" + target +
            "\" not found in enumeration");
    }

    /**
     * Decode a chunk of data from base64 encoding. The length of a chunk is
     * always 4 characters in the base64 representation, but may be 1, 2, or 3
     * bytes of data, as determined by whether there are any pad characters at
     * the end of the base64 representation 
     *
     * @param base starting offset within base64 character array
     * @param chrs character array for base64 text representation
     * @param fill starting offset within byte data array
     * @param byts byte data array
     * @return number of decoded bytes
     */
	private static int decodeChunk(int base, char[] chrs, int fill,
        byte[] byts) {
        
        // find the byte count to be decoded
		int length = 3;
		if (chrs[base+3] == PAD_CHAR) {
			length = 2;
			if (chrs[base+2] == PAD_CHAR) {
				length = 1;
			}
		}
        
        // get 6-bit values
		int v0 = s_base64Values[chrs[base+0]];
		int v1 = s_base64Values[chrs[base+1]];
		int v2 = s_base64Values[chrs[base+2]];
		int v3 = s_base64Values[chrs[base+3]];
        
        // convert and store bytes of data
		switch (length) {
			case 3:
				byts[fill+2] = (byte)(v2 << 6 | v3);
			case 2:
				byts[fill+1] = (byte)(v1 << 4 | v2 >> 2);
			case 1:
				byts[fill] = (byte)(v0 << 2 | v1 >> 4);
				break;
		}
		return length;
	}

    /**
     * Parse base64 data from text. This converts the base64 data into a byte
     * array of the appopriate length. In keeping with the recommendations,
     *
     * @param text text to be parsed (may include extra characters)
     * @return byte array of data
     * @throws JiBXException if invalid character in base64 representation
     */
	public static byte[] parseBase64(String text) throws JiBXException {
        
        // convert raw text to base64 character array
        char[] chrs = new char[text.length()];
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);
            if (chr < 128 && s_base64Values[chr] >= 0) {
                chrs[length++] = chr;
            }
        }
        
        // check the text length
		if (length % 4 != 0) {
			throw new JiBXException
                ("Text length for base64 must be a multiple of 4");
		} else if (length == 0) {
			return new byte[0];
		}
        
        // find corresponding byte count for data
		int blength = length / 4 * 3;
		if (chrs[length-1] == PAD_CHAR) {
			blength--;
			if (chrs[length-2] == PAD_CHAR) {
				blength--;
			}
		}
        
        // convert text to actual bytes of data
		byte[] byts = new byte[blength];
		int fill = 0;
		for (int i = 0; i < length; i += 4) {
			fill += decodeChunk(i, chrs, fill, byts);
		}
		if (fill != blength) {
			throw new JiBXException
                ("Embedded padding characters in byte64 text");
		}
		return byts;
	}

    /**
     * Parse base64 data from text. This converts the base64 data into a byte
     * array of the appopriate length. In keeping with the recommendations,
     *
     * @param text text to be parsed (may be null, or include extra characters)
     * @return byte array of data
     * @throws JiBXException if invalid character in base64 representation
     */
    public static byte[] deserializeBase64(String text) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            return parseBase64(text);
        }
    }

    /**
     * Encode a chunk of data to base64 encoding. Converts the next three bytes
     * of data into four characters of text representation, using padding at the
     * end of less than three bytes of data remain.
     *
     * @param base starting offset within byte array
     * @param byts byte data array
     * @param buff buffer for encoded text
     */
	public static void encodeChunk(int base, byte[] byts, StringBuffer buff) {
        
        // get actual byte data length to be encoded
		int length = 3;
		if (base + length > byts.length) {
			length = byts.length - base;
		}
        
        // convert up to three bytes of data to four characters of text
		int b0 = byts[base];
		int value = (b0 >> 2) & 0x3F;
		buff.append(s_base64Chars[value]);
		if (length > 1) {
			int b1 = byts[base+1];
			value = ((b0 & 3) << 4) + ((b1 >> 4) & 0x0F);
			buff.append(s_base64Chars[value]);
			if (length > 2) {
				int b2 = byts[base+2];
				value = ((b1 & 0x0F) << 2) + ((b2 >> 6) & 3);
				buff.append(s_base64Chars[value]);
				value = b2 & 0x3F;
				buff.append(s_base64Chars[value]);
			} else {
				value = (b1 & 0x0F) << 2;
				buff.append(s_base64Chars[value]);
				buff.append(PAD_CHAR);
			}
		} else {
			value = (b0 & 3) << 4;
			buff.append(s_base64Chars[value]);
			buff.append(PAD_CHAR);
			buff.append(PAD_CHAR);
		}
	}

    /**
     * Serialize byte array to base64 text. In keeping with the specification,
     * this adds a line break every 76 characters in the encoded representation.
     *
     * @param byts byte data array
     * @return base64 encoded text
     */
	public static String serializeBase64(byte[] byts) {
        StringBuffer buff = new StringBuffer((byts.length + 2) / 3 * 4);
		for (int i = 0; i < byts.length; i += 3) {
			encodeChunk(i, byts, buff);
            if (i > 0 && i % 57 == 0 && (i + 3) < byts.length) {
                buff.append("\r\n");
            }
		}
		return buff.toString();
	}
    
    /**
     * Resize array of arbitrary type.
     *
     * @param size new aray size
     * @param base array to be resized
     * @return resized array, with all data to minimum of the two sizes copied
     */
    public static Object resizeArray(int size, Object base) {
        int prior = Array.getLength(base);
        if (size == prior) {
            return base;
        } else {
            Class type = base.getClass().getComponentType();
            Object copy = Array.newInstance(type, size);
            int count = Math.min(size, prior);
            System.arraycopy(base, 0, copy, 0, count);
            return copy;
        }
    }
    
    /**
     * Grow array of arbitrary type. The returned array is double the size of
     * the original array, providing this is at least the defined minimum size.
     *
     * @param base array to be grown
     * @return array of twice the size as original array, with all data copied
     */
    public static Object growArray(Object base) {
        int length = Array.getLength(base);
        Class type = base.getClass().getComponentType();
        int newlen = Math.max(length*2, MINIMUM_GROWN_ARRAY_SIZE);
        Object copy = Array.newInstance(type, newlen);
        System.arraycopy(base, 0, copy, 0, length);
        return copy;
    }
    
    /**
     * Factory method to create a <code>java.util.ArrayList</code> as the
     * implementation of a <code>java.util.List</code>.
     *
     * @return new <code>java.util.ArrayList</code>
     */
    public static List arrayListFactory() {
        return new ArrayList();
    }
    
    /**
     * Convert whitespace-separated list of values. This implements the
     * whitespace skipping, calling the supplied item deserializer for handling
     * each individual value.
     * 
     * @param text value to be converted
     * @param ideser list item deserializer
     * @return list of deserialized items (<code>null</code> if input
     * <code>null</code>, or if nonrecoverable error)
     * @throws JiBXException if error in deserializing text
     */
    public static ArrayList deserializeList(String text,
        IListItemDeserializer ideser) throws JiBXException {
        if (text == null) {
            return null;
        } else {
            
            // scan text to find whitespace breaks between items
            ArrayList items = new ArrayList();
            int length = text.length();
            int base = 0;
            boolean space = true;
            for (int i = 0; i < length; i++) {
                char chr = text.charAt(i);
                switch (chr) {
                    
                    case 0x09:
                    case 0x0A:
                    case 0x0D:
                    case ' ':
                        // ignore if preceded by space
                        if (!space) {
                            String itext = text.substring(base, i);
                            items.add(ideser.deserialize(itext));
                            space = true;
                        }
                        base = i + 1;
                        break;
                        
                    default:
                        space = false;
                        break;
                }
            }
            
            // finish last item
            if (base < length) {
                String itext = text.substring(base);
                items.add(ideser.deserialize(itext));
            }
            
            // check if any items found
            if (items.size() > 0) {
                return items;
            } else {
                return null;
            }
        }
    }

    /**
     * Deserialize a list of whitespace-separated tokens into an array of
     * strings.
     * 
     * @param text value text
     * @return created class instance
     * @throws JiBXException on error in marshalling
     */
    public static String[] deserializeTokenList(String text) throws JiBXException {
        
        // use basic qualified name deserializer to handle items
        IListItemDeserializer ldser = new IListItemDeserializer() {
            public Object deserialize(String text) throws JiBXException {
                return text;
            }
        };
        ArrayList list = Utility.deserializeList(text, ldser);
        if (list == null) {
            return null;
        } else {
            return (String[])list.toArray(new String[list.size()]);
        }
    }
    
    /**
     * Serialize an array of strings into a whitespace-separated token list.
     * 
     * @param tokens array of strings to be serialized
     * @return list text
     */
    public static String serializeTokenList(String[] tokens) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < tokens.length; i++) {
            if (buff.length() > 0) {
                buff.append(' ');
            }
            buff.append(tokens[i]);
        }
        return buff.toString();
    }
    
    /**
     * Safe equals test. This does an equals comparison for objects which may be
     * <code>null</code>.
     *
     * @param a
     * @param b
     * @return <code>true</code> if both <code>null</code> or a.equals(b),
     * <code>false</code> otherwise
     */
    public static boolean safeEquals(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }
    
    /**
     * Check if a substring matches a supplied value.
     *
     * @param match comparison text
     * @param text string to be compared
     * @param offset starting offset for comparison
     * @return <code>true</code> if substring match, <code>false</code> if not
     */
    public static boolean ifEqualSubstring(String match, String text,
        int offset) {
        int length = match.length();
        if (text.length() >= offset + length) {
            for (int i = offset; i < length; i++) {
                if (match.charAt(i) != text.charAt(offset+i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Check if a text string is a valid integer subtype representation.
     *
     * @param text (non-<code>null</code>)
     * @param digitmax maximum number of digits allowed
     * @param abslimit largest absolute value allowed (<code>null</code> if no
     * limit)
     * @return <code>true</code> if valid representation, <code>false</code> if
     * not
     */
    public static boolean ifInIntegerRange(String text, int digitmax,
        String abslimit) {
        if (text.length() >= 1) {
            
            // first skip past a leading sign character
            int length = text.length();
            int first = 0;
            boolean negate = false;
            char chr = text.charAt(0);
            if (chr == '-') {
                negate = true;
                first = 1;
            } else if (chr == '+') {
                first = 1;
            }
            
            // scan for any non-digit characters, and to find first non-zero
            for (int i = first; i < length; i++) {
                chr = text.charAt(i);
                if (chr < '0' || chr > '9') {
                    return false;
                } else if (chr == '0' && i == first) {
                    first++;
                }
            }
            
            // check if the digit count is allowed
            int digitcnt = length - first;
            if (digitcnt > digitmax) {
                return false;
            } else if (digitcnt == digitmax) {
                String number = text;
                if (first > 0) {
                    number = text.substring(first);
                }
                if (negate) {
                    return abslimit.compareTo(number) > 0;
                } else {
                    return abslimit.compareTo(number) >= 0;
                }
            } else {
                return true;
            }
            
        } else {
            return false;
        }
    }
    
    /**
     * Check if a text string is a valid byte representation.
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if valid byte, <code>false</code> if not
     */
    public static boolean ifByte(String text) {
        return ifInIntegerRange(text, 3, "128");
    }
    
    /**
     * Check if a text string is a valid short representation.
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if valid short, <code>false</code> if not
     */
    public static boolean ifShort(String text) {
        return ifInIntegerRange(text, 5, "32768");
    }
    
    /**
     * Check if a text string is a valid int representation.
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if valid int, <code>false</code> if not
     */
    public static boolean ifInt(String text) {
        return ifInIntegerRange(text, 10, "2147483648");
    }
    
    /**
     * Check if a text string is a valid long representation.
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if valid long, <code>false</code> if not
     */
    public static boolean ifLong(String text) {
        return ifInIntegerRange(text, 19, "9223372036854775808");
    }
    
    /**
     * Check if a text string is a valid integer representation.
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if valid integer, <code>false</code> if not
     */
    public static boolean ifInteger(String text) {
        return ifInIntegerRange(text, Integer.MAX_VALUE, null);
    }
    
    /**
     * Check if a portion of a text string consists of decimal digits.
     *
     * @param text
     * @param offset starting offset
     * @param limit ending offset plus one (<code>false</code> return if the
     * text length is less than this value)
     * @return <code>true</code> if digits, <code>false</code> if not
     */
    public static boolean ifDigits(String text, int offset, int limit) {
        if (text.length() < limit) {
            return false;
        } else {
            for (int i = offset; i < limit; i++) {
                char chr = text.charAt(i);
                if (chr < '0' || chr > '9') {
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Check if a text string is a valid decimal representation.
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if valid decimal, <code>false</code> if not
     */
    public static boolean ifDecimal(String text) {
        
        // empty string is never a valid decimal
        if (text.length() == 0) {
            return false;
        }
        
        // first skip past a leading sign character
        int length = text.length();
        int first = 0;
        char chr = text.charAt(0);
        if (chr == '-') {
            first = 1;
        } else if (chr == '+') {
            first = 1;
        }
        
        // string consisting only of sign character is never a valid decimal
        if (first == length) {
            return false;
        }
        
        // scan for any non-digit characters
        boolean decimal = false;
        for (int i = first; i < length; i++) {
            chr = text.charAt(i);
            if (chr < '0' || chr > '9') {
                if (!decimal && chr == '.') {
                    decimal = true;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Check if a portion of a text string is a bounded string of decimal
     * digits. This is used for checking fixed-length decimal fields with a
     * maximum value.
     *
     * @param text
     * @param offset
     * @param bound
     * @return <code>true</code> if bounded decimal, <code>false</code> if not
     */
    public static boolean ifFixedDigits(String text, int offset, String bound) {
        int length = bound.length();
        boolean lessthan = false;
        for (int i = 0; i < length; i++) {
            char chr = text.charAt(offset + i);
            if (chr < '0' || chr > '9') {
                return false;
            } else if (!lessthan) {
                int diff = bound.charAt(i) - chr;
                if (diff > 0) {
                    lessthan = true;
                } else if (diff < 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Check if a text string ends with a valid zone suffix. This accepts an
     * empty suffix, the single letter 'Z', or an offset of +/-HH:MM.
     *
     * @param text
     * @param offset
     * @return <code>true</code> if valid suffix, <code>false</code> if not
     */
    public static boolean ifZoneSuffix(String text, int offset) {
        int length = text.length();
        if (length <= offset) {
            return true;
        } else {
            char chr = text.charAt(offset);
            if (length == offset+1 && chr == 'Z') {
                return true;
            } else if (length == offset+6 && (chr == '+' || chr == '-') &&
                text.charAt(offset+3) == ':') {
                if (ifEqualSubstring("14", text, offset+1)) {
                    return ifEqualSubstring("00", text, offset+4);
                } else {
                    return ifFixedDigits(text, offset+1, "13") &&
                        ifFixedDigits(text, offset+4, "59");
                }
            } else {
                return false;
            }
        }
    }
    
    /**
     * Check if a text string follows the schema date format. This does not
     * assure that the text string is actually a valid date representation,
     * since it doesn't fully check the value ranges (such as the day number
     * range for a particular month).
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if date format, <code>false</code> if not
     */
    public static boolean ifDate(String text) {
        if (text.length() < 10) {
            return false;
        } else {
            int base = 0;
            int split = text.indexOf('-');
            if (split == 0) {
                base = 1;
                split = text.indexOf('-', 1);
            }
            if (split > 0 && text.length() - split >= 5 &&
                ifDigits(text, base, split) && text.charAt(split+3) == '-' &&
                ifFixedDigits(text, split+1, "12") &&
                ifFixedDigits(text, split+4, "31")) {
                return ifZoneSuffix(text, split+6);
            } else {
                return false;
            }
        }
    }
    
    /**
     * Check if a text string ends with a valid time suffix. This matches the
     * format HH:MM:SS with optional training fractional seconds and optional
     * time zone.
     *
     * @param text
     * @param offset
     * @return <code>true</code> if valid suffix, <code>false</code> if not
     */
    public static boolean ifTimeSuffix(String text, int offset) {
        if (text.length() - offset >= 8 &&
            (ifEqualSubstring("24:00:00", text, offset) ||
            (ifFixedDigits(text, offset, "23") &&
            text.charAt(offset+2) == ':' &&
            ifFixedDigits(text, offset+3, "59") &&
            text.charAt(offset+5) == ':' &&
            ifFixedDigits(text, offset+6, "60")))) {
            int base = offset + 8;
            int length = text.length();
            if (base < length && text.charAt(base) == '.') {
                while (++base < length) {
                    char chr = text.charAt(base);
                    if (chr < '0' || chr > '9') {
                        break;
                    }
                }
            }
            return ifZoneSuffix(text, base);
        } else {
            return false;
        }
    }
    
    /**
     * Check if a text string follows the schema dateTime format. This does not
     * assure that the text string is actually a valid dateTime representation,
     * since it doesn't fully check the value ranges (such as the day number
     * range for a particular month).
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if date format, <code>false</code> if not
     */
    public static boolean ifDateTime(String text) {
        if (text.length() < 19) {
            return false;
        } else {
            int base = 0;
            int split = text.indexOf('-');
            if (split == 0) {
                base = 1;
                split = text.indexOf('-', 1);
            }
            if (split > 0 && text.length() - split >= 14 &&
                ifDigits(text, base, split) && text.charAt(split+3) == '-' &&
                ifFixedDigits(text, split+1, "12") &&
                ifFixedDigits(text, split+4, "31") &&
                text.charAt(split+6) == 'T') {
                return ifTimeSuffix(text, split+7);
            } else {
                return false;
            }
        }
    }
    
    /**
     * Check if a text string follows the schema dateTime format. This does not
     * assure that the text string is actually a valid dateTime representation,
     * since it doesn't fully check the value ranges (such as the day number
     * range for a particular month).
     *
     * @param text (non-<code>null</code>)
     * @return <code>true</code> if date format, <code>false</code> if not
     */
    public static boolean ifTime(String text) {
        return ifTimeSuffix(text, 0);
    }
}
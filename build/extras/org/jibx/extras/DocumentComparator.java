/*
Copyright (c) 2003-2009, Dennis M. Sosnoski.
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

package org.jibx.extras;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import org.jibx.runtime.Utility;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * XML document comparator. This uses XMLPull parsers to read a pair of
 * documents in parallel, comparing the streams of components seen from the two
 * documents. The comparison ignores differences in whitespace separating
 * elements, but in non-schema mode treats whitespace as significant within
 * elements with only character data content. 
 * 
 * @author Dennis M. Sosnoski
 */
public class DocumentComparator
{
    /** Parser for first document. */
    protected XmlPullParser m_parserA;

    /** Parser for second document. */
    protected XmlPullParser m_parserB;
    
    /** Print stream for reporting differences. */
    protected PrintStream m_differencePrint;
    
    /** Compare using schema data value adjustments flag. */
    protected boolean m_schemaCompare;

    /**
     * Constructor with schema adjustments flag specified. Builds the actual
     * parsers and sets up for comparisons.
     *
     * @param print print stream for reporting differences
     * @param schema use schema adjustments in comparisons flag
     * @throws XmlPullParserException on error creating parsers
     */
    public DocumentComparator(PrintStream print, boolean schema)
        throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        m_parserA = factory.newPullParser();
        m_parserB = factory.newPullParser();
        m_differencePrint = print;
        m_schemaCompare = schema;
    }

    /**
     * Constructor. Builds the actual parser.
     *
     * @param print print stream for reporting differences
     * @throws XmlPullParserException on error creating parsers
     */
    public DocumentComparator(PrintStream print) throws XmlPullParserException {
        this(print, false);
    }

    /**
     * Build parse input position description.
     *
     * @param parser for which to build description
     * @return text description of current parse position
     */
    protected static String buildPositionString(XmlPullParser parser) {
        return " line " + parser.getLineNumber() + ", col " +
            parser.getColumnNumber();
    }
    
    /**
     * Build name string.
     * 
     * @param ns namespace URI
     * @param name local name
     * @return printable names string
     */
    protected static String buildName(String ns, String name) {
        if ("".equals(ns)) {
            return name;
        } else {
            return "{" + ns + '}' + name;
        }
    }

    /**
     * Prints error description text. The generated text include position
     * information from both documents. 
     *
     * @param msg error message text
     */
    protected void printError(String msg) {
        if (m_differencePrint != null) {
            m_differencePrint.println(msg + " - from " +
                buildPositionString(m_parserA) + " to " +
                buildPositionString(m_parserB));
        }
    }
    
    /**
     * Check if a name/namespace pair matches a schema namespace location
     * attribute.
     *
     * @param name
     * @param ns
     * @return <code>true</code> if a schema namespace location,
     * <code>false</code> if not
     */
    private static boolean isSchemaLocation(String name, String ns) {
        return ("http://www.w3.org/2001/XMLSchema-instance".equals(ns) &&
            ("schemaLocation".equals(name) ||
            "noNamespaceSchemaLocation".equals(name)));
    }
    
    /**
     * Normalize a decimal value for comparison. A leading '+' sign is ignored,
     * while a leading '-' is kept. If no non-zero digits are seen a "0" value
     * is returned; otherwise, all significant digits are kept and returned
     * (including a decimal point only if there are fraction digits)
     *
     * @param text
     * @return normalized value
     */
    private static String normalizeDecimal(String text) {
        
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
        
        // scan to find first and last non-zero characters
        int last = -1;
        int point = -1;
        for (int i = first; i < length; i++) {
            chr = text.charAt(i);
            if (chr == '0') {
                if (i == first) {
                    first++;
                }
            } else if (chr == '.') {
                point = i;
            } else {
                last = i;
            }
        }
        
        // first check for case of no significant digits
        if (last < 0) {
            return negate ? "-0" : "0";
        }
        
        // return a normalized representation
        StringBuffer buff = new StringBuffer(length);
        if (negate) {
            buff.append('-');
        }
        if (point >= 0) {
            
            // decimal point seen, and at least one digit seen
            if (first < point) {
                
                // digit seen prior to decimal point
                if (last > point) {
                    buff.append(text.substring(first, last + 1));
                } else {
                    buff.append(text.substring(first, point));
                }
                
            } else {
                
                // no digits prior to decimal point, so must be after
                buff.append(text.substring(point, last + 1));
                
            }
        } else {
            
            // no decimal point, just go from first non-zero digit
            buff.append(text.substring(first));
            
        }
        return buff.toString();
    }

    /**
     * Check for equal values. If the schema compare flag is configured, this
     * applies some basic schema rules in the comparisons, allowing '0' to match
     * 'false' and '1' to match 'true', and comparing values using date or
     * dateTime structure as decoded values.
     *
     * @param texta
     * @param textb
     * @return <code>true</code> if values match, <code>false</code> if not
     */
    protected boolean equalValues(String texta, String textb) {
        if (texta == null) {
            return textb == null;
        } else if (textb == null) {
            return false;
        } else if (texta.equals(textb)) {
            return true;
        } else if (m_schemaCompare) {
            String trima = texta.trim();
            String trimb = textb.trim();
            if (trima.equals(trimb)) {
                return true;
            } else if (("0".equals(trima) && "false".equals(trimb)) ||
                ("1".equals(trima) && "true".equals(trimb)) ||
                ("0".equals(trimb) && "false".equals(trima)) ||
                ("1".equals(trimb) && "true".equals(trima))) {
                return true;
            } else if (Utility.ifDate(trima)) {
                try {
                    return Utility.parseDate(trima) == Utility.parseDate(trimb);
                } catch (Exception e) {
                    return false;
                }
            } else if (Utility.ifTime(trima)) {
                try {
                    return Utility.parseTime(trima, 0, trima.length()) ==
                        Utility.parseTime(trimb, 0, trimb.length());
                } catch (Exception e) {
                    return false;
                }
            } else if (Utility.ifDateTime(trima)) {
                try {
                    return Utility.parseDateTime(trima) ==
                        Utility.parseDateTime(trimb);
                } catch (Exception e) {
                    return false;
                }
            } else if (Utility.ifDecimal(trima) && Utility.ifDecimal(trimb)){
                return normalizeDecimal(trima).equals(normalizeDecimal(trimb));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Verifies that the attributes on the current start tags match. Any
     * mismatches are printed immediately.
     *
     * @return <code>true</code> if the attributes match, <code>false</code> if
     * not
     */
    protected boolean matchAttributes() {
        int counta = m_parserA.getAttributeCount();
        int countb = m_parserB.getAttributeCount();
        boolean[] flags = new boolean[countb];
        boolean match = true;
        for (int i = 0; i < counta; i++) {
            String name = m_parserA.getAttributeName(i);
            String ns = m_parserA.getAttributeNamespace(i);
            String value = m_parserA.getAttributeValue(i);
            boolean found = false;
            for (int j = 0; j < countb; j++) {
                if (name.equals(m_parserB.getAttributeName(j)) &&
                    ns.equals(m_parserB.getAttributeNamespace(j))) {
                    flags[j] = true;
                    if (!equalValues(value, m_parserB.getAttributeValue(j))) {
                        if (match) {
                            printError("Attribute mismatch");
                            match = false;
                        }
                        m_differencePrint.println("  attribute " +
                            buildName(ns, name) + " value '" + value +
                            "' != '" + m_parserB.getAttributeValue(j) + '\'');
                    }
                    found = true;
                    break;
                }
            }
            if (!found && !(m_schemaCompare && isSchemaLocation(name, ns))) {
                if (match) {
                    printError("Attribute mismatch");
                    match = false;
                }
                m_differencePrint.println("  attribute " +
                    buildName(ns, name) + "=\"" + value +
                    "\" is missing from second document");
            }
        }
        for (int i = 0; i < countb; i++) {
            if (!flags[i]) {
                String ns = m_parserB.getAttributeNamespace(i);
                String name = m_parserB.getAttributeName(i);
                if (!m_schemaCompare || !isSchemaLocation(name, ns)) {
                    if (match) {
                        printError("Attribute mismatch");
                        match = false;
                    }
                    m_differencePrint.println("  attribute " +
                        buildName(ns, name) +
                        " is missing from first document");
                }
            }
        }
        return match;
    }

    /**
     * Check if two text strings match, ignoring leading and trailing spaces.
     * Any mismatch is printed immediately, with the supplied lead text.
     *
     * @param texta
     * @param textb
     * @param lead error text lead
     * @return <code>true</code> if the texts match, <code>false</code> if
     * not
     */
    protected boolean matchText(String texta, String textb, String lead) {
        if (equalValues(texta, textb)) {
            return true;
        } else if (!m_schemaCompare && texta.trim().equals(textb.trim())) {
            return true;
        } else {
            printError(lead);
            if (m_differencePrint != null) {
                m_differencePrint.println(" \"" + texta +
                "\" (length " + texta.length() + " vs. \"" +
                textb + "\" (length " + textb.length() + ')');

            }
            return false;
        }
    }

    /**
     * Verifies that the current start or end tag names match.
     *
     * @return <code>true</code> if the names match, <code>false</code> if not
     */
    protected boolean matchNames() {
        return m_parserA.getName().equals(m_parserB.getName()) &&
            m_parserA.getNamespace().equals(m_parserB.getNamespace());
    }

    /**
     * Compares a pair of documents by reading them in parallel from a pair of
     * parsers. The comparison ignores differences in whitespace separating
     * elements, but treats whitespace as significant within elements with only
     * character data content. 
     *
     * @param rdra reader for first document to be compared
     * @param rdrb reader for second document to be compared
     * @return <code>true</code> if the documents are the same,
     * <code>false</code> if they're different
     */
    public boolean compare(Reader rdra, Reader rdrb) {
        try {
        
            // set the documents and initialize
            m_parserA.setInput(rdra);
            m_parserB.setInput(rdrb);
            boolean content = false;
            String texta = "";
            String textb = "";
            boolean same = true;
            while (true) {
                
                // start by collecting and moving past text content
                if (m_parserA.getEventType() == XmlPullParser.TEXT) {
                    texta = m_parserA.getText();
                    m_parserA.next();
                }
                if (m_parserB.getEventType() == XmlPullParser.TEXT) {
                    textb = m_parserB.getText();
                    m_parserB.next();
                }
                
                // now check actual tag state
                int typea = m_parserA.getEventType();
                int typeb = m_parserB.getEventType();
                if (typea != typeb) {
                    printError("Different document structure");
                    return false;
                } else if (typea == XmlPullParser.START_TAG) {
                    
                    // compare start tags, attributes, and prior text
                    content = true;
                    if (!matchNames()) {
                        printError("Different start tags");
                        return false;
                    } else {
                        if (!matchAttributes()) {
                            same = false;
                        }
                        if (!matchText(texta, textb,
                            "Different text content between elements")) {
                            same = false;
                        }
                    }
                    texta = textb = "";
                    
                } else if (typea == XmlPullParser.END_TAG) {
                    
                    // compare end tags and prior text
                    if (!matchNames()) {
                        printError("Different end tags");
                        return false;
                    }
                    if (content) {
                        if (!matchText(texta, textb, "Different text content")) {
                            same = false;
                        }
                        content = false;
                    } else {
                        if (!matchText(texta, textb,
                            "Different text content between elements")) {
                            same = false;
                        }
                    }
                    texta = textb = "";
                    
                } else if (typea == XmlPullParser.END_DOCUMENT) {
                    if (!same && m_differencePrint != null) {
                        m_differencePrint.flush();
                    }
                    return same;
                }
                
                // advance both parsers to next component
                m_parserA.next();
                m_parserB.next();
                
            }
        } catch (IOException ex) {
            if (m_differencePrint != null) {
                ex.printStackTrace(m_differencePrint);
                m_differencePrint.flush();
            }
            return false;
        } catch (XmlPullParserException ex) {
            if (m_differencePrint != null) {
                ex.printStackTrace(m_differencePrint);
                m_differencePrint.flush();
            }
            return false;
        }
    }
}
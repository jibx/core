/*
Copyright (c) 2003-2004, Dennis M. Sosnoski.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of Sosnoski Software Solutions, Inc. nor the names of its
   personnel may be used to endorse or promote products derived from this
   software without specific prior written permission.

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

package com.sosnoski.site;

import java.io.*;
import java.util.HashMap;

/**
 * Parse buffer with specific extension methods for HTML. This class 
 * implements extended matching and searching methods for parsing HTML text.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class HTMLParseBuffer extends ParseBuffer
{
	/** Single quote (apostrophe) delimitor character. */
	private static final char SINGLE_QUOTE_DELIMITOR = '\'';
	
	/** Double quote delimitor character. */
	private static final char DOUBLE_QUOTE_DELIMITOR = '"';
	
	/** Horizontal tab character (whitespace). */
	private static final int HORIZONTAL_TAB = 0x09;
	
	/** Line feed character (whitespace). */
	private static final int LINE_FEED = 0x0A;
	
	/** Carriage return character (whitespace). */
	private static final int CARRIAGE_RETURN = 0x0D;
	
	/** Space character (whitespace). */
	private static final int SPACE = 0x20;
    
    /** Hashmap for attribute values. */
    private HashMap m_attrMap;
	
	/**
	 * Constructor.
	 *
	 * @param reader reader supplying text
	 * @param length initial buffer size
	 */
	
	public HTMLParseBuffer(Reader reader, int length) {
		super(reader, length);
        m_attrMap = new HashMap();
	}
	
	/**
	 * Constructor. This version just uses a default size for the buffer.
	 * 
	 * @param reader reader supplying text
	 * @param subst text substitution table
	 */
	
	public HTMLParseBuffer(Reader reader) {
		super(reader);
        m_attrMap = new HashMap();
	}

	/**
	 * Skip past a target character, ignoring quoted text. Scans input
	 * looking for a character match. When a single or double quote is
	 * encountered, it skips past the closing quote before continuing to
	 * scan for a character match.
	 *
	 * @param match character value to be matched
	 * @exception IOException for input error or character not found
	 */
	
	public void skipPastCharQuotes(int match) throws IOException {
		int chr;
		while ((chr = read()) != match) {
			if (chr == SINGLE_QUOTE_DELIMITOR || 
				chr == DOUBLE_QUOTE_DELIMITOR) {
				skipPastChar(chr);
			} else if (chr == -1) {
				throw new IOException(END_OF_FILE_MESSAGE);
			}
		}
	}

	/**
	 * Check if a character represents a whitespace. This is coded using the
	 * standard HTML whitespace characters.
	 *
	 * @param chr character value to be checked
	 * @return <code>true</code> if a whitespace, <code>false</code> if not
	 */
	
	public static boolean isWhitespaceChar(int chr) {
		return (chr <= SPACE && (chr == SPACE || chr == HORIZONTAL_TAB ||
			chr == CARRIAGE_RETURN || chr == LINE_FEED));
	}

	/**
	 * Skip past whitespace characters in the input. Discards all input
	 * characters which are recognized as whitespace by the standard 
	 * check method (@link isWhitespaceChar).
	 *
	 * @return <code>true</code> if a non-whitespace found, <code>false</code>
	 * if error or end of file
	 */
	
	public boolean skipWhitespace() {
		try {
			int chr;
			while ((chr = read()) >= 0) {
				if (!isWhitespaceChar(chr)) {
					skip(-1);
					return true;
				}
			}
		} catch (IOException ex) {}
		return false;
	}

	/**
	 * Skip to a whitespace character in the input. Discards all input
	 * characters which are not recognized as whitespace by the standard 
	 * check method (@link isWhitespaceChar).
	 *
	 * @return <code>true</code> if a whitespace found, <code>false</code>
	 * if error or end of file
	 */
	
	public boolean skipToWhitespace() {
		try {
			int chr;
			while ((chr = read()) >= 0) {
				if (isWhitespaceChar(chr)) {
					skip(-1);
					return true;
				}
			}
		} catch (IOException ex) {}
		return false;
	}

	/**
	 * Skip to a whitespace character in the input, ignoring quoted text.
	 * Scans input looking for a whitespace character as recognized by the
	 * standard method (@see isWhitespaceChar). When a single or double quote
	 * is encountered, it skips past the closing quote before continuing to
	 * scan for a whitespace.
	 *
	 * @return <code>true</code> if a whitespace found, <code>false</code>
	 * if end of file
	 * @exception IOException for input error
	 */
	
	public boolean skipToWhitespaceQuotes() throws IOException {
		int chr;
		while ((chr = read()) >= 0) {
			if (isWhitespaceChar(chr)) {
				skip(-1);
				return true;
			} else {
				if (chr == SINGLE_QUOTE_DELIMITOR || 
					chr == DOUBLE_QUOTE_DELIMITOR) {
					skipPastChar(chr);
				}
			}
		}
		return false;
	}

	/**
	 * Skip past a name in the input. Scans past a name in the input, where
	 * the characters allowed are as defined for HTML.
	 *
	 * @return <code>true</code> if skipped past a valid name,
	 * <code>false</code> otherwise
	 * @exception IOException for input error
	 */
	
	public boolean skipPastName() throws IOException {
		
		// check for valid name start character
		int chr = read();
		if (Character.isLetter((char) chr) || chr == '_' || chr == ':') {
			
			// skip until end of name reached
			while ((chr = read()) >= 0) {
				if (!Character.isLetterOrDigit((char)(chr)) &&
					chr != '_' && chr != ':' && chr != '-' && chr != '.') {
					
					// return positioned past last character in name
					skip(-1);
					return true;
					
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Skip to and return next start or end tag. Scans from the current position
	 * to the next start or end tag in the text, skipping past comments and
	 * processing instructions. This assumes that the initial position is not
	 * inside a tag. Returns the tag name found as lower case, with input
	 * positioned either following the tag name or following the end of the tag,
     * depending on the parameter value.
	 * 
     * @param drop flag for attributes to be dropped
	 * @return next start or end tag name from text (lower case, with leading
	 * '/' if close tag, or <code>null</code> if end of file)
	 * @exception IOException on error reading input
	 */
	
	public String nextTag(boolean drop) throws IOException {
		int chr;
		while ((chr = read()) >= 0) {
			
			// skip until less than found
			if (chr == '<') {
				chr = read();
				if (chr == '!') {
					
					// comment or processing instruction, just skip past end
					if (read() == '-') {
						skipPastString("-->");
					} else {
						skipPastCharQuotes('>');
					}
					
				} else {
					
					// mark start of tag
					pushMark(-1);
					if (chr == '/') {
						chr = read();
					}
					if (Character.isLetter((char)chr)) {
						
						// start or end tag, get the actual name text
						while (Character.isLetterOrDigit((char)read()));
						String name = read(-popMark()-1).toLowerCase();
						
						// optionally skip past end of tag, escaping attributes
                        if (drop) {
                            skipPastCharQuotes('>');
                        }
						return name;
						
					} else {
						clearMark();
					}
					
				}
			}
		}
		return null;
	}
	
	/**
	 * Collect attributes from tag. This is only valid after a call to {@link
     * #nextTag}<code>(false)</code>. Returns with input positioned following
     * the end of the tag. All attribute names are returned as lower case.
	 * 
	 * @return map of attributes present on tag (reused, so only valid until
     * next call)
	 * @exception IOException on error reading input
	 */
	
	public HashMap attributes() throws IOException {
		int chr;
        m_attrMap.clear();
		while (skipWhitespace() && (chr = read()) >= 0 && chr != '>') {
            
            // accumulate the attribute name first
            pushMark(-1);
            if (Character.isLetter((char)chr)) {
                
                // get the attribute name text
                while (Character.isLetterOrDigit((char)read()));
                String name = read(-popMark()-1).toLowerCase();
                
                // check for attribute value supplied
                String value = name;
                skipWhitespace();
                chr = read();
                if (chr == '=') {
                    
                    // collect attribute value (quoted or unquoted)
                    skipWhitespace();
                    chr = read();
                    if (chr == SINGLE_QUOTE_DELIMITOR ||
                        chr == DOUBLE_QUOTE_DELIMITOR) {
                        pushMark();
                        skipPastChar(chr);
                        value = read(-popMark()-1);
                        read();
                    } else {
                        pushMark(-1);
                        skipToWhitespace();
                        value = read(-popMark());
                    }
                }
                
                // add attribute to map
                m_attrMap.put(name, value);
                
            } else {
                clearMark();
            }
		}
		return m_attrMap;
	}
	
	/**
	 * Skip past the attributes (if any) in the current tag. This is only valid
     * after a call to {@link #nextTag}<code>(false)</code>. Returns with input
     * positioned following the end of the tag.
	 * 
	 * @exception IOException on error reading input
	 */
	
	public void dropTag() throws IOException {
        skipPastCharQuotes('>');
	}
	
	/**
	 * Skip to end tag for tag. Scans from the current position to the close for
	 * the supplied tag, ignoring the contents of comments and processing
	 * instructions. Nested instances of the tag are handled by counting, so
	 * it's important that each occurrence of the tag is properly closed. If
	 * not, the results are undefined.
	 * 
	 * @param name target tag name (lower case)
	 * @exception IOException on error reading input
	 */
	
	public void skipToCloseTag(String name) throws IOException {
		
		// initialize for tag scanning loop
		int depth = 1;
		int chr;
		while ((chr = read()) >= 0) {
			
			// skip until less than found
			if (chr == '<') {
				
				// mark position and check handling
				pushMark(-1);
				chr = read();
				if (chr == '!') {
					
					// comment or processing instruction, just skip past end
					clearMark();
					if (read() == '-') {
						skipPastString("-->");
					} else {
						skipPastCharQuotes('>');
					}
					
				} else {
					
					// position for actual start of text
					boolean end = false;
					if (chr == '/') {
						end = true;
					} else {
						skip(-1);
					}
					
					// match all characters of text with lowercase compare
					boolean match = true;
					for (int i = 0; i < name.length(); i++) {
						chr = Character.toLowerCase((char)read());
						if (chr != name.charAt(i)) {
							match = false;
							break;
						}
					}
					
					// adjust nesting depth for start or end tag
					if (match) {
						if (end) {
							if (--depth == 0) {
								popMark();
								return;
							}
						} else {
							depth++;
						}
					}
					clearMark();
					
				}
			}
		}
		throw new EOFException(END_OF_FILE_MESSAGE);
	}
}
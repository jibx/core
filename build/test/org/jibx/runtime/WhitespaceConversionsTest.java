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

import junit.framework.TestCase;

/**
 * Whitespace conversion tests.
 *
 * @author Dennis M. Sosnoski
 */
public class WhitespaceConversionsTest extends TestCase
{
    public void testReplace() throws JiBXException {
        assertNull("Null input", WhitespaceConversions.replace(null));
        assertEquals("Empty string", "", WhitespaceConversions.replace(""));
        assertEquals("tab not converted", "a b", WhitespaceConversions.replace("a\tb"));
        assertEquals("LF not converted", "a b", WhitespaceConversions.replace("a\nb"));
        assertEquals("CR not converted", "a b", WhitespaceConversions.replace("a\rb"));
        assertEquals("Leading tab not converted", " ab", WhitespaceConversions.replace("\tab"));
        assertEquals("Leading LF not converted", " ab", WhitespaceConversions.replace("\nab"));
        assertEquals("Leading CR not converted", " ab", WhitespaceConversions.replace("\rab"));
        assertEquals("Trailing tab not converted", "ab ", WhitespaceConversions.replace("ab\t"));
        assertEquals("Trailing LF not converted", "ab ", WhitespaceConversions.replace("ab\n"));
        assertEquals("Trailing CR not converted", "ab ", WhitespaceConversions.replace("ab\r"));
    }
    
    public void testCollapse() throws JiBXException {
        assertNull("Null input", WhitespaceConversions.collapse(null));
        assertEquals("Empty string", "", WhitespaceConversions.collapse(""));
        assertEquals("Whitespace string", "", WhitespaceConversions.collapse("  \t\r\n "));
        assertEquals("tab not converted", "a b", WhitespaceConversions.collapse("a\tb"));
        assertEquals("LF not converted", "a b", WhitespaceConversions.collapse("a\nb"));
        assertEquals("CR not converted", "a b", WhitespaceConversions.collapse("a\rb"));
        assertEquals("Multiples not converted", "a b", WhitespaceConversions.collapse("a \t  b"));
        assertEquals("Leading tab not converted", "ab", WhitespaceConversions.collapse("\tab"));
        assertEquals("Leading LF not converted", "ab", WhitespaceConversions.collapse("\nab"));
        assertEquals("Leading CR not converted", "ab", WhitespaceConversions.collapse("\rab"));
        assertEquals("Trailing tab not converted", "ab", WhitespaceConversions.collapse("\tab"));
        assertEquals("Trailing LF not converted", "ab", WhitespaceConversions.collapse("\nab"));
        assertEquals("Trailing CR not converted", "ab", WhitespaceConversions.collapse("\rab"));
    }
    
    public void testTrim() throws JiBXException {
        assertNull("Null input", WhitespaceConversions.trim(null));
        assertEquals("Empty string", "", WhitespaceConversions.trim(""));
        assertEquals("Whitespace string", "", WhitespaceConversions.trim("  \t\r\n "));
        assertEquals("Leading spaces not converted", "ab", WhitespaceConversions.trim("  ab"));
        assertEquals("Leading tab not converted", "ab", WhitespaceConversions.trim("\tab"));
        assertEquals("Leading LF not converted", "ab", WhitespaceConversions.trim("\nab"));
        assertEquals("Leading CR not converted", "ab", WhitespaceConversions.trim("\rab"));
        assertEquals("Trailing spaces not converted", "ab", WhitespaceConversions.trim("ab   "));
        assertEquals("Trailing tab not converted", "ab", WhitespaceConversions.trim("\tab"));
        assertEquals("Trailing LF not converted", "ab", WhitespaceConversions.trim("\nab"));
        assertEquals("Trailing CR not converted", "ab", WhitespaceConversions.trim("\rab"));
        assertEquals("Leading/trailing spaces not converted", "ab", WhitespaceConversions.trim("   ab   "));
    }
}
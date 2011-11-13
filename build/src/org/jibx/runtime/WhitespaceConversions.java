/*
 * Copyright (c) 2009, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.runtime;

/**
 * Utilities for handling whitespace options.
 * 
 * @author Dennis M. Sosnoski
 */
public final class WhitespaceConversions
{
    /** Non-constructor for class with no instances. */
    private WhitespaceConversions() {}
    
    /**
     * Convert string value to whitespace-replaced form. The first character to be replaced must have been found prior
     * to this call.
     * 
     * @param text value to be converted
     * @param index first character offset to be dropped from result
     * @return collapsed string value
     */
    private static String convertReplaced(String text, int index) {
        
        // allocate and initialize copy to first replacement
        int length = text.length();
        StringBuffer buff = new StringBuffer(length - 1);
        for (int i = 0; i < index; i++) {
            buff.append(text.charAt(i));
        }
        
        // copy remainder of text, one character at a time
        while (index < length) {
            char chr = text.charAt(index++);
            switch (chr)
            {
                
                case 0x09:
                case 0x0A:
                case 0x0D:
                    // replace other whitespace with space character
                    buff.append(' ');
                    break;
                
                default:
                    // copy actual character to result buffer
                    buff.append(chr);
                    break;
                
            }
        }
        return buff.toString();
    }
    
    /**
     * Replace non-space whitespace in string. This first scans to see if any non-space whitespace is present, and if
     * so, invokes the actual conversion handling.
     * 
     * @param text value to be converted (<code>null</code> if none)
     * @return replaced string value (<code>null</code> if none)
     */
    public static String replace(String text) {
        
        // just exit if no text
        if (text == null) {
            return null;
        }
        
        // scan through characters to check if conversion needed
        int index;
        int length = text.length();
        loop: for (index = 0; index < length; index++) {
            switch (text.charAt(index))
            {
                
                case 0x09:
                case 0x0A:
                case 0x0D:
                    // exit when any non-space whitespace is found
                    break loop;
                
            }
        }
        
        // convert if character needs to be dropped
        if (index < length) {
            return convertReplaced(text, index);
        } else {
            return text;
        }
    }
    
    /**
     * Convert string value to whitespace-collapsed form. The first whitespace character must have been found prior to
     * this call.
     * 
     * @param text value to be converted
     * @param index first character offset to be dropped from result
     * @return collapsed string value
     */
    private static String convertCollapsed(String text, int index) {
        
        // allocate and initialize copy to first drop
        int length = text.length();
        StringBuffer buff = new StringBuffer(length - 1);
        if (index > 0) {
            
            // copy all the non-whitespace characters
            for (int i = 0; i < index; i++) {
                buff.append(text.charAt(i));
            }
            
            // append a single space to replace any whitespace sequence
            buff.append(' ');
        }
        
        // copy remainder of text, one character at a time
        boolean drop = true;
        while (++index < length) {
            char chr = text.charAt(index);
            switch (chr)
            {
                
                case 0x09:
                case 0x0A:
                case 0x0D:
                case ' ':
                    if (!drop) {
                        
                        // replace first whitespace in sequence with single space
                        buff.append(' ');
                        drop = true;
                        
                    }
                    break;
                
                default:
                    // copy character to result buffer
                    buff.append(chr);
                    drop = false;
                    break;
                
            }
        }
        
        // finish by deleting trailing whitespace
        if (drop && buff.length() > 0) {
            buff.setLength(buff.length()-1);
        }
        return buff.toString();
    }
    
    /**
     * Collapse whitespace in string. This first scans to see if any whitespace is present, and if so, invokes the
     * actual conversion handling.
     * 
     * @param text value to be converted (<code>null</code> if none)
     * @return collapsed string value (<code>null</code> if none)
     */
    public static String collapse(String text) {
        
        // just exit if no text
        if (text == null) {
            return null;
        }
        
        // scan through characters to check if conversion needed
        int index;
        int length = text.length();
        loop: for (index = 0; index < length; index++) {
            switch (text.charAt(index))
            {
                
                case 0x09:
                case 0x0A:
                case 0x0D:
                case ' ':
                    // exit when any whitespace is found
                    break loop;
                
            }
        }
        
        // convert if character needs to be dropped
        if (index < length) {
            return convertCollapsed(text, index);
        } else {
            return text;
        }
    }
    
    /**
     * Trim leading and trailing whitespace in string.
     * 
     * @param text value to be converted (<code>null</code> if none)
     * @return trimmed string value (<code>null</code> if none)
     */
    public static String trim(String text) {
        
        // just exit if nothing to be done
        if (text == null) {
            return null;
        }
        
        // scan through characters to skip leading whitespace
        int length = text.length();
        int start;
        loop: for (start = 0; start < length; start++) {
            switch (text.charAt(start))
            {
                
                case 0x09:
                case 0x0A:
                case 0x0D:
                case ' ':
                    break;
                    
                default:
                    // exit when any non-whitespace is found
                    break loop;
                
            }
        }
        if (start < length) {
            
            // scan through characters to skip trailing whitespace
            int end;
            loop: for (end = length-1; ; end--) {
                switch (text.charAt(end))
                {
                    
                    case 0x09:
                    case 0x0A:
                    case 0x0D:
                    case ' ':
                        break;
                        
                    default:
                        // exit when any non-whitespace is found
                        break loop;
                    
                }
            }
            
            // return only the trimmed portion of string
            end++;
            if (start > 0 || end < length) {
                return text.substring(start, end);
            } else {
                return text;
            }
            
        } else {
            return "";
        }
    }
}
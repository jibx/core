/*
 * Copyright (c) 2006-2010, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema.support;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.schema.validation.ValidationContext;

/**
 * Utilities for conversion of schema standard datatypes.
 * 
 * @author Dennis M. Sosnoski
 */
public final class Conversions
{
    /** Character types allowed as initial characters of a name. */
    public static final int NAMEINIT_CHARACTER_TYPES =
        Character.LOWERCASE_LETTER | Character.UPPERCASE_LETTER | Character.OTHER_LETTER | Character.TITLECASE_LETTER
            | Character.LETTER_NUMBER;
    
    /** Character types allowed as non-initial characters of a name. */
    public static final int NAMEFOLLOW_CHARACTER_TYPES =
        NAMEINIT_CHARACTER_TYPES | Character.NON_SPACING_MARK | Character.DECIMAL_DIGIT_NUMBER
            | Character.COMBINING_SPACING_MARK | Character.ENCLOSING_MARK | Character.MODIFIER_LETTER;
    
    /** Non-constructor for class with no instances. */
    private Conversions() {}
    
    /**
     * Convert normalized string value with validation. This handles the actual conversion of a normalized string value.
     * The first character to be dropped must have been found prior to this call.
     * 
     * @param text value to be converted
     * @param index first character offset to be dropped from result
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return normalized string value (<code>null</code> if nonrecoverable error)
     */
    private static String convertNormalizedString(String text, int index, String tname, ValidationContext vctx,
        Object obj) {
        
        // allocate and initialize copy to first drop
        int length = text.length();
        StringBuffer buff = new StringBuffer(length - 1);
        buff.append(text.substring(0, index));
        
        // copy remainder of text, one character at a time
        while (++index < length) {
            char chr = text.charAt(index);
            switch (chr)
            {
                
                case 0x09:
                case 0x0D:
                    // not allowed in normalized string
                    if (vctx.addError("Character 0x" + Integer.toHexString(text.charAt(index)) + " not allowed in "
                        + tname, obj)) {
                        break;
                    } else {
                        return null;
                    }
                    
                case 0x0A:
                    // drop character from sequence
                    break;
                
                default:
                    // copy character to result buffer
                    buff.append(chr);
                    break;
                
            }
        }
        return buff.toString();
    }
    
    /**
     * Validate normalized string value. This checks the text and, if necessary, converts it to valid form.
     * 
     * @param text value to be converted
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return normalized string value (<code>null</code> if nonrecoverable error)
     */
    public static String checkNormalizedString(String text, String tname, ValidationContext vctx, Object obj) {
        
        // scan through characters to check if conversion needed
        int index;
        int length = text.length();
        loop: for (index = 0; index < length; index++) {
            switch (text.charAt(index))
            {
                
                case 0x09:
                case 0x0D:
                    // not allowed in normalized string
                    if (vctx.addError("Character 0x" + Integer.toHexString(text.charAt(index)) + " not allowed in "
                        + tname, obj)) {
                        break loop;
                    } else {
                        return null;
                    }
                    
                case 0x0A:
                    // break into compaction loop
                    break loop;
                
            }
        }
        
        // convert if character needs to be dropped
        if (index < length) {
            return convertNormalizedString(text, index, tname, vctx, obj);
        } else {
            return text;
        }
    }
    
    /**
     * Deserialize normalized string value. This validates the text and, if necessary, converts it to standard form.
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param vctx validation context
     * @param obj object being validated
     * @return normalized string value (<code>null</code> if input <code>null</code>, or nonrecoverable error)
     */
    public static String deserializeNormalizedString(String text, ValidationContext vctx, Object obj) {
        if (text == null) {
            return null;
        } else {
            return checkNormalizedString(text, "normalizedString", vctx, obj);
        }
    }
    
    /**
     * Convert token-type value with validation. This handles the actual conversion of a value with no leading or
     * trailing spaces, no non-space whitespaces, . The first character to be dropped must have been found prior to this
     * call.
     * 
     * @param text value to be converted
     * @param index first character offset to be dropped from result
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return token value (<code>null</code> if nonrecoverable error)
     */
    private static String convertToken(String text, int index, String tname, ValidationContext vctx, Object obj) {
        
        // allocate and initialize copy to first drop
        int length = text.length();
        StringBuffer buff = new StringBuffer(length - 1);
        buff.append(text.substring(0, index));
        
        // copy remainder of text, one character at a time
        boolean space = text.charAt(index) == ' ';
        while (++index < length) {
            char chr = text.charAt(index);
            switch (chr)
            {
                
                case 0x09:
                case 0x0D:
                    // not allowed in token
                {
                    if (vctx.addError("Character 0x" + Integer.toHexString(text.charAt(index)) + " not allowed in "
                        + tname, obj)) {
                        break;
                    } else {
                        return null;
                    }
                }
                    
                case 0x20:
                    // check for valid space
                    if (space) {
                        if (vctx.addError("Multiple space not allowed in " + tname, obj)) {
                            break;
                        } else {
                            return null;
                        }
                    } else {
                        buff.append(chr);
                        space = true;
                    }
                    break;
                
                default:
                    // copy character to result buffer
                    buff.append(chr);
                    space = false;
                    break;
                
            }
        }
        return buff.toString();
    }
    
    /**
     * Validate token value. This validates the text and, if necessary, converts it to standard form.
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return token value (<code>null</code> if nonrecoverable error)
     */
    public static String checkToken(String text, String tname, ValidationContext vctx, Object obj) {
        
        // scan through characters to check if conversion needed
        int index;
        int length = text.length();
        boolean space = false;
        loop: for (index = 0; index < length; index++) {
            switch (text.charAt(index))
            {
                
                case 0x09:
                case 0x0D:
                    // not allowed in token
                {
                    if (vctx.addError("Character 0x" + Integer.toHexString(text.charAt(index)) + " not allowed in "
                        + tname, obj)) {
                        break loop;
                    } else {
                        return null;
                    }
                }
                    
                case 0x0A:
                    // break into compaction loop
                    break loop;
                
                case 0x20:
                    // check for valid space
                    if (index == 0) {
                        if (vctx.addError("Leading space not allowed in " + tname, obj)) {
                            break loop;
                        } else {
                            return null;
                        }
                    } else if (space) {
                        if (vctx.addError("Multiple space not allowed in " + tname, obj)) {
                            break loop;
                        } else {
                            return null;
                        }
                    } else {
                        space = true;
                    }
                    break;
                
                default:
                    space = false;
                    break;
                
            }
        }
        
        // convert if character needs to be dropped
        if (index < length) {
            return convertToken(text, index, tname, vctx, obj);
        } else {
            return text;
        }
    }
    
    /**
     * Deserialize token value. This validates the text and, if necessary, converts it to standard form.
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param vctx validation context
     * @param obj object being validated
     * @return token value (<code>null</code> if input <code>null</code>, or nonrecoverable error)
     */
    public static String deserializeToken(String text, ValidationContext vctx, Object obj) {
        if (text == null) {
            return null;
        } else {
            return checkToken(text, "token", vctx, obj);
        }
    }
    
    /**
     * Convert collapsed string value. The first character to be collapsed must must have been found prior to this call.
     * 
     * @param text value to be converted
     * @param index first character offset to be dropped from result
     * @return normalized string value
     */
    private static String convertCollapsed(String text, int index) {
        
        // allocate and initialize copy to first drop
        int length = text.length();
        StringBuffer buff = new StringBuffer(length - 1);
        buff.append(text.substring(0, index));
        
        // copy remainder of text, one character at a time
        boolean space = index >= 0 && text.charAt(index) == ' ';
        while (++index < length) {
            char chr = text.charAt(index);
            switch (chr)
            {
                
                case 0x09:
                case 0x0A:
                case 0x0D:
                case ' ':
                    // ignore if preceded by space, otherwise convert to space
                    if (!space) {
                        buff.append(' ');
                        space = true;
                    }
                    break;
                
                default:
                    // copy character to result buffer
                    buff.append(chr);
                    break;
                
            }
        }
        return buff.toString();
    }
    
    /**
     * Convert Name value with validation. This handles the actual conversion of a Name value by dropping illegal
     * characters. It should only be called when error recovery is enabled. The first character to be dropped must have
     * been found prior to this call.
     * 
     * @param text value to be converted
     * @param index first character offset to be dropped from result
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return Name value (<code>null</code> if nonrecoverable error)
     */
    public static String convertName(String text, int index, String tname, ValidationContext vctx, Object obj) {
        
        // allocate and initialize copy to first drop
        int length = text.length();
        StringBuffer buff = new StringBuffer(length - 1);
        buff.append(text.substring(0, index));
        
        // copy remainder of text, one character at a time
        while (++index < length) {
            
            // make sure character is valid for name
            char chr = text.charAt(index);
            int type = Character.getType(chr);
            if ((type & NAMEINIT_CHARACTER_TYPES) == 0) {
                if (chr != '_' && chr != ':') {
                    boolean valid = false;
                    if (index > 0) {
                        valid = (type & NAMEFOLLOW_CHARACTER_TYPES) != 0 || chr == '.' || chr == '-';
                        if (!valid) {
                            vctx.addError("Character '" + chr + "' not allowed as first character of " + tname, obj);
                        }
                    } else {
                        vctx.addError("Character '" + chr + "' not allowed in " + tname, obj);
                    }
                    if (valid) {
                        buff.append(chr);
                    }
                }
            }
            
        }
        return buff.toString();
    }
    
    /**
     * Validate Name value. This validates the text and, if necessary, converts it to valid form by dropping illegal
     * characters (only if error recovery is enabled).
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return Name value (<code>null</code> if nonrecoverable error)
     */
    public static String checkName(String text, String tname, ValidationContext vctx, Object obj) {
        
        // scan through characters to check if conversion needed
        int index = 0;
        int length = text.length();
        while (++index < length) {
            
            // make sure character is valid for name
            char chr = text.charAt(index);
            int type = Character.getType(chr);
            if ((type & NAMEINIT_CHARACTER_TYPES) == 0) {
                if (chr != '_' && chr != ':') {
                    if (index > 0) {
                        if ((type & NAMEFOLLOW_CHARACTER_TYPES) == 0 && chr != '.' && chr != '-') {
                            
                            // report invalid nostart character for name
                            if (vctx.addError("Character '" + chr + "' not allowed in " + tname, obj)) {
                                return convertName(text, index, tname, vctx, obj);
                            } else {
                                return null;
                            }
                        }
                    } else {
                        
                        // report invalid start character for name
                        if (vctx.addError("Character '" + chr + "' not allowed as first character of " + tname, obj)) {
                            return convertName(text, index, tname, vctx, obj);
                        } else {
                            return null;
                        }
                        
                    }
                }
            }
            
        }
        return text;
    }
    
    /**
     * Deserialize Name value. This validates the text and, if necessary, converts it to valid form by dropping illegal
     * characters (only if error recovery is enabled).
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param vctx validation context
     * @param obj object being validated
     * @return Name value (<code>null</code> if input <code>null</code>, or nonrecoverable error)
     */
    public static String deserializeName(String text, ValidationContext vctx, Object obj) {
        if (text == null) {
            return null;
        } else {
            return checkName(text, "Name", vctx, obj);
        }
    }
    
    /**
     * Convert NCName value with validation. This handles the actual conversion of an NCName value by dropping illegal
     * characters. It should only be called when error recovery is enabled. The first character to be dropped must have
     * been found prior to this call.
     * 
     * @param text value to be converted
     * @param index first character offset to be dropped from result
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return NCName value
     */
    private static String convertNCName(String text, int index, String tname, ValidationContext vctx, Object obj) {
        
        // allocate and initialize copy to first drop
        int length = text.length();
        StringBuffer buff = new StringBuffer(length - 1);
        buff.append(text.substring(0, index));
        
        // copy remainder of text, one character at a time
        while (++index < length) {
            
            // make sure character is valid for name
            char chr = text.charAt(index);
            int type = Character.getType(chr);
            if ((type & NAMEINIT_CHARACTER_TYPES) == 0) {
                if (chr != '_') {
                    boolean valid = false;
                    if (index > 0) {
                        valid = (type & NAMEFOLLOW_CHARACTER_TYPES) != 0 || chr == '.' || chr == '-';
                        if (!valid) {
                            vctx.addError("Character '" + chr + "' not allowed as first character of " + tname, obj);
                        }
                    } else {
                        vctx.addError("Character '" + chr + "' not allowed in " + tname, obj);
                    }
                    if (valid) {
                        buff.append(chr);
                    }
                }
            }
            
        }
        return buff.toString();
    }
    
    /**
     * Check NCName value. This validates the text and, if necessary, converts it to valid form by dropping illegal
     * characters (only if error recovery is enabled).
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return NCName value (<code>null</code> if nonrecoverable error)
     */
    public static String checkNCName(String text, String tname, ValidationContext vctx, Object obj) {
        
        // scan through characters to check if conversion needed
        int index = 0;
        int length = text.length();
        while (++index < length) {
            
            // make sure character is valid for name
            char chr = text.charAt(index);
            int type = Character.getType(chr);
            if ((type & NAMEINIT_CHARACTER_TYPES) == 0) {
                if (chr != '_') {
                    if (index > 0) {
                        if ((type & NAMEFOLLOW_CHARACTER_TYPES) == 0 && chr != '.' && chr != '-') {
                            
                            // report invalid nostart character for name
                            if (vctx.addError("Character '" + chr + "' not allowed in " + tname, obj)) {
                                return convertNCName(text, index, tname, vctx, obj);
                            } else {
                                return null;
                            }
                        }
                    } else {
                        
                        // report invalid start character for name
                        if (vctx.addError("Character '" + chr + "' not allowed as first character of " + tname, obj)) {
                            return convertNCName(text, index, tname, vctx, obj);
                        } else {
                            return null;
                        }
                        
                    }
                }
            }
            
        }
        return text;
    }
    
    /**
     * Deserialize NCName value. This validates the text and, if necessary, converts it to valid form by dropping
     * illegal characters (only if error recovery is enabled).
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param vctx validation context
     * @param obj object being validated
     * @return NCName value (<code>null</code> if input <code>null</code>, or nonrecoverable error)
     */
    public static String deserializeNCName(String text, ValidationContext vctx, Object obj) {
        if (text == null) {
            return null;
        } else {
            return checkNCName(text, "NCName", vctx, obj);
        }
    }
    
    /**
     * Convert NMTOKEN value with validation. This handles the actual conversion of an NMTOKEN value by dropping illegal
     * characters. It should only be called when error recovery is enabled. The first character to be dropped must have
     * been found prior to this call.
     * 
     * @param text value to be converted
     * @param index first character offset to be dropped from result
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return NMTOKEN value
     */
    private static String convertNMTOKEN(String text, int index, String tname, ValidationContext vctx, Object obj) {
        
        // allocate and initialize copy to first drop
        int length = text.length();
        StringBuffer buff = new StringBuffer(length - 1);
        buff.append(text.substring(0, index));
        
        // copy remainder of text, one character at a time
        while (++index < length) {
            
            // make sure character is valid for name
            char chr = text.charAt(index);
            if ((Character.getType(chr) & NAMEFOLLOW_CHARACTER_TYPES) == 0) {
                vctx.addError("Character '" + chr + "' not allowed in " + tname, obj);
            } else {
                buff.append(chr);
            }
        }
        return buff.toString();
    }
    
    /**
     * Check NMTOKEN value. This validates the text and, if necessary, converts it to valid form by dropping illegal
     * characters (only if error recovery is enabled).
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param tname type name
     * @param vctx validation context
     * @param obj object being validated
     * @return NMTOKEN value (<code>null</code> if nonrecoverable error)
     */
    public static String checkNMTOKEN(String text, String tname, ValidationContext vctx, Object obj) {
        
        // scan through characters to check if conversion needed
        int index = 0;
        int length = text.length();
        while (++index < length) {
            
            // make sure character is valid for name
            char chr = text.charAt(index);
            int type = Character.getType(chr);
            if ((type & NAMEFOLLOW_CHARACTER_TYPES) == 0) {
                if (vctx.addError("Character '" + chr + "' not allowed in " + tname, obj)) {
                    return convertNMTOKEN(text, index, tname, vctx, obj);
                } else {
                    return null;
                }
            }
            
        }
        return text;
    }
    
    /**
     * Deserialize NMTOKEN value. This validates the text and, if necessary, converts it to valid form by dropping
     * illegal characters (only if error recovery is enabled).
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param vctx validation context
     * @param obj object being validated
     * @return NMTOKEN value (<code>null</code> if input <code>null</code>, or nonrecoverable error)
     */
    public static String deserializeNMTOKEN(String text, ValidationContext vctx, Object obj) {
        if (text == null) {
            return null;
        } else {
            return checkNMTOKEN(text, "NMTOKEN", vctx, obj);
        }
    }
    
    /**
     * Check collapsed whitespace value. This checks the text and, if necessary, converts it to standard form.
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @return collapsed value
     */
    public static String checkCollapse(String text) {
        
        // scan through characters to check if conversion needed
        int index;
        int length = text.length();
        boolean space = false;
        loop: for (index = 0; index < length; index++) {
            switch (text.charAt(index))
            {
                
                case 0x09:
                case 0x0A:
                case 0x0D:
                    // break into compaction loop
                    break loop;
                
                case 0x20:
                    // check for valid space
                    if (index == 0 || space) {
                        break loop;
                    } else {
                        space = true;
                    }
                    break;
                
                default:
                    space = false;
                    break;
                
            }
        }
        
        // convert if needed
        if (index < length) {
            return convertCollapsed(text, index);
        } else {
            return text;
        }
    }
    
    /**
     * Validate and convert anyURI value.
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param vctx validation context
     * @return normalized string value (<code>null</code> if input <code>null</code>, or error)
     */
    public static String convertAnyUri(String text, ValidationContext vctx) {
        return checkCollapse(text);
    }
    
    /**
     * Validate and convert enumeration attribute value.
     * 
     * @param text value to be converted (may be <code>null</code>)
     * @param eset enumeration set
     * @param name attribute name
     * @param ictx unmarshalling context
     * @return converted value
     */
    public static int convertEnumeration(String text, EnumSet eset, String name, IUnmarshallingContext ictx) {
        if (text == null) {
            return -1;
        } else {
            ValidationContext vctx = (ValidationContext)ictx.getUserContext();
            String nctext = Conversions.deserializeNMTOKEN(text, vctx, ictx.getStackTop());
            int type = eset.getValue(nctext);
            if (type < 0) {
                vctx.addError("Illegal value \"" + text + "\" for '" + name + "' attribute", ictx.getStackTop());
            }
            return type;
        }
    }
}
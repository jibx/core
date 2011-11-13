/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen.extend;

import java.util.ArrayList;

import org.jibx.schema.codegen.NameUtils;
import org.jibx.util.NameUtilities;

/**
 * Converter for working with Java names.
 * 
 * @author Dennis M. Sosnoski
 */
public class DefaultNameConverter implements NameConverter
{
    /** Camelcase field names flag. */
    private boolean m_camelCase;
    
    /** Prefix used for normal field names (non-<code>null</code>, may be empty). */
    private String m_fieldPrefix;
    
    /** Suffix used for normal field names (non-<code>null</code>, may be empty). */
    private String m_fieldSuffix;
    
    /** Prefix used for static field names (non-<code>null</code>, may be empty). */
    private String m_staticPrefix;
    
    /** Suffix used for static field names (non-<code>null</code>, may be empty). */
    private String m_staticSuffix;
    
    /** Use underscores in field names flag (as substitute for special characters, and to split words). */
    private boolean m_underscore;
    
    /** Uppercase initial letter of field names flag. */
    private boolean m_upperInitial;
    
    /** Set of XML name prefixes to be discarded in conversions. */
    private String[] m_stripPrefixes;
    
    /** Set of XML name suffixes to be discarded in conversions. */
    private String[] m_stripSuffixes;
    
    /** Reusable array for words in name. */
    private ArrayList m_wordList;
    
    /**
     * Constructor.
     */
    public DefaultNameConverter() {
        m_fieldPrefix = "";
        m_fieldSuffix = "";
        m_staticPrefix = "";
        m_staticSuffix = "";
        m_camelCase = true;
        m_stripPrefixes = m_stripSuffixes = new String[0];
        m_wordList = new ArrayList();
    }
    
    /**
     * Copy constructor.
     * 
     * @param base instance used to initialize settings
     */
    public DefaultNameConverter(DefaultNameConverter base) {
        m_fieldPrefix = base.m_fieldPrefix;
        m_fieldSuffix = base.m_fieldSuffix;
        m_staticPrefix = base.m_staticPrefix;
        m_staticSuffix = base.m_staticSuffix;
        m_camelCase = base.m_camelCase;
        m_stripPrefixes = base.m_stripPrefixes;
        m_stripSuffixes = base.m_stripSuffixes;
        m_wordList = new ArrayList();
    }
    
    /**
     * Convert singular name to plural form.
     * 
     * @param name base name
     * @return plural name
     */
    public String pluralize(String name) {
        return NameUtilities.pluralize(name);
    }
    
    /**
     * Convert potentially plural name to singular form.
     * 
     * @param name base name
     * @return singularized name
     */
    public String depluralize(String name) {
        return NameUtilities.depluralize(name);
    }
    
    /**
     * Get prefix text for normal field names.
     * 
     * @return field prefix (non-<code>null</code>, may be empty)
     */
    public String getFieldPrefix() {
        return m_fieldPrefix;
    }
    
    /**
     * Set prefix text for normal field names.
     * 
     * @param pref field prefix (non-<code>null</code>, may be empty)
     */
    public void setFieldPrefix(String pref) {
        m_fieldPrefix = pref;
    }
    
    /**
     * Get suffix text for normal field names.
     * 
     * @return field suffix (non-<code>null</code>, may be empty)
     */
    public String getFieldSuffix() {
        return m_fieldPrefix;
    }
    
    /**
     * Set suffix text for normal field names.
     * 
     * @param suff field suffix (non-<code>null</code>, may be empty)
     */
    public void setFieldSuffix(String suff) {
        m_fieldPrefix = suff;
    }
    
    /**
     * Get prefix text for static field names.
     * 
     * @return field prefix (non-<code>null</code>, may be empty)
     */
    public String getStaticPrefix() {
        return m_staticPrefix;
    }
    
    /**
     * Set prefix text for static field names.
     * 
     * @param pref field prefix (non-<code>null</code>, may be empty)
     */
    public void setStaticPrefix(String pref) {
        m_staticPrefix = pref;
    }
    
    /**
     * Get suffix text for static field names.
     * 
     * @return field suffix (non-<code>null</code>, may be empty)
     */
    public String getStaticSuffix() {
        return m_staticPrefix;
    }
    
    /**
     * Set suffix text for static field names.
     * 
     * @param suff field suffix (non-<code>null</code>, may be empty)
     */
    public void setStaticSuffix(String suff) {
        m_staticPrefix = suff;
    }

    /**
     * Get the prefixes to be stripped when converting XML names.
     *
     * @return prefixes
     */
    public String[] getStripPrefixes() {
        return m_stripPrefixes;
    }

    /**
     * Set the prefixes to be stripped when converting XML names.
     *
     * @param prefixes
     */
    public void setStripPrefixes(String[] prefixes) {
        m_stripPrefixes = prefixes;
    }

    /**
     * Get the suffixes to be stripped when converting XML names.
     *
     * @return suffixes
     */
    public String[] getStripSuffixes() {
        return m_stripSuffixes;
    }

    /**
     * Set the suffixes to be stripped when converting XML names.
     *
     * @param suffixes
     */
    public void setStripSuffixes(String[] suffixes) {
        m_stripSuffixes = suffixes;
    }

    /**
     * Trim specified prefixes and/or suffixes from an XML name.
     *
     * @param xname XML name
     * @return trimmed name, with specified prefixes and/or suffixes removed
     */
    public String trimXName(String xname) {
        
        // first trim off a prefix on name
        for (int i = 0; i < m_stripPrefixes.length; i++) {
            String prefix = m_stripPrefixes[i];
            if (xname.startsWith(prefix) && xname.length() > prefix.length()) {
                xname = xname.substring(prefix.length());
                break;
            }
        }
        
        // next trim off a suffix on name
        for (int i = 0; i < m_stripSuffixes.length; i++) {
            String suffix = m_stripSuffixes[i];
            if (xname.endsWith(suffix) && xname.length() > suffix.length()) {
                xname = xname.substring(0, xname.length() - suffix.length());
                break;
            }
        }
        return xname;
    }
    
    /**
     * Split an XML name into words. This splits first on the basis of separator characters ('.', '-', and '_') in the
     * name, and secondly based on case (an uppercase character immediately followed by one or more lowercase characters
     * is considered a word, and multiple uppercase characters not followed immediately by a lowercase character are
     * also considered a word). Characters which are not valid as parts of identifiers in Java are dropped from the XML
     * name before it is split, and words starting with initial uppercase characters have the upper case dropped for
     * consistency. Note that this method is not threadsafe.
     * 
     * @param name
     * @return array of words
     */
    public String[] splitXMLWords(String name) {
        
        // start by finding a valid start character
        int offset = 0;
        while (!Character.isJavaIdentifierStart(name.charAt(offset))) {
            if (++offset >= name.length()) {
                return new String[0];
            }
        }
        
        // accumulate the list of words
        StringBuffer word = new StringBuffer();
        m_wordList.clear();
        boolean lastupper = false;
        while (offset < name.length()) {
            
            // check next character of name
            char chr = name.charAt(offset++);
            if (chr == '-' || chr == '.' || chr == '_') {
                
                // force split at each splitting character
                if (word.length() > 0) {
                    m_wordList.add(word.toString());
                    word.setLength(0);
                }
                
            } else if (Character.isJavaIdentifierPart(chr)) {
                
                // check case of valid identifier part character
                if (Character.isUpperCase(chr)) {
                    if (!lastupper && word.length() > 0) {
                        
                        // upper after lower, split before upper
                        m_wordList.add(word.toString());
                        word.setLength(0);
                        
                    }
                    lastupper = true;
                    
                } else {
                    if (lastupper) {
                        if (word.length() > 1) {
                            
                            // multiple uppers followed by lower, split before last
                            int split = word.length() - 1;
                            m_wordList.add(word.substring(0, split));
                            char start = Character.toLowerCase(word.charAt(split));
                            word.setLength(0);
                            word.append(start);
                            
                        } else if (word.length() > 0) {
                            
                            // single upper followed by lower, convert upper to lower
                            word.setCharAt(0, Character.toLowerCase(word.charAt(0)));
                            
                        }
                    }
                    lastupper = false;
                }
                word.append(chr);
            }
        }
        if (word.length() > 0) {
            m_wordList.add(word.toString());
        }
        
        // return array of words
        return (String[])m_wordList.toArray(new String[m_wordList.size()]);
    }
    
    /**
     * Check if a name needs to be converted from XML form due to invalid characters or embedded underscores.
     * 
     * @param name
     * @return <code>true</code> if invalid name, <code>false</code> if valid
     */
    protected static boolean isConversionNeeded(String name) {
        int length = name.length();
        if (length > 0) {
            if (Character.isJavaIdentifierStart(name.charAt(0))) {
                for (int i = 1; i < length; i++) {
                    char chr = name.charAt(i);
                    if (chr == '_' || !Character.isJavaIdentifierPart(chr)) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }
    
    /**
     * Convert a base name to a legal Java class name. This implementation avoids using class names from the default
     * import classes in <code>java.lang</code>, to avoid conflicts with the import handling in generated code.
     * 
     * @param name base name
     * @return converted name
     */
    public String toJavaClassName(String name) {
        
        // make sure initial letter is uppercase
        int index = 0;
        char chr;
        while ((chr = name.charAt(index)) == '_') {
            index++;
        }
        if (Character.isLowerCase(chr)) {
            name = name.substring(0, index) + Character.toUpperCase(chr) + name.substring(index+1);
        }
        
        // prevent conflicts with default import classes
        while (NameUtils.isDefaultImport(name)) {
            name = "_" + name;
        }
        return name;
    }
    
    /**
     * Convert an XML name to a Java value base name. The base name is in normalized camelcase form with leading lower
     * case (unless the first word of the name is all uppercase).
     * 
     * @param xname XML name
     * @return converted name
     */
    public String toBaseName(String xname) {
        String name = trimXName(xname);
        if (isConversionNeeded(name) || Character.isUpperCase(name.charAt(0))) {
            
            // split trimmed name into a series of words
            String[] words = splitXMLWords(name);
            if (words.length == 0) {
                return "x";
            }
            
            // use single underscore for empty result
            if (words.length == 0) {
                words = new String[] { "_" };
            }
            
            // form name by concatenating words with configured style
            StringBuffer buff = new StringBuffer();
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                if (i > 0 && m_underscore) {
                    buff.append('_');
                }
                if ((i == 0 && m_upperInitial) || (i > 0 && m_camelCase)) {
                    buff.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        buff.append(word.substring(1, word.length()));
                    }
                } else {
                    buff.append(word);
                }
            }
            name = buff.toString();
        }
        return name;
    }
    
    /**
     * Convert text to constant name. The returned name is guaranteed not to match a Java keyword.
     *
     * @param text raw text to be converted
     * @return constant name
     */
    public String toConstantName(String text) {
        
        // insert underscores to separate words of name, and convert invalid characters
        StringBuffer buff = new StringBuffer(text.length());
        boolean lastup = false;
        boolean multup = false;
        char lastchar = 0;
        for (int index = 0; index < text.length(); index++) {
            char chr = text.charAt(index);
            if (index == 0 && !Character.isJavaIdentifierStart(chr)) {
                buff.append('_');
            }
            if (Character.isJavaIdentifierPart(chr)) {
                if (lastup) {
                    if (Character.isUpperCase(chr)) {
                        multup = true;
                    } else {
                        lastup = false;
                        if (chr == '_') {
                            multup = false;
                        } else if (multup) {
                            buff.insert(buff.length()-1, '_');
                            multup = false;
                        }
                    }
                } else if (Character.isUpperCase(chr)) {
                    if (index > 0 && lastchar != '_') {
                        buff.append('_');
                    }
                    lastup = true;
                    multup = false;
                }
                lastchar = Character.toUpperCase(chr);
                buff.append(lastchar);
            }
        }
        if (buff.length() == 0) {
            buff.append('_');
        }
        String name = buff.toString();
        if (NameUtils.isReserved(name)) {
            name = "_" + name;
        }
        return name;
    }

    /**
     * Build a field name using supplied prefix and/or suffix.
     *
     * @param base normalized camelcase base name
     * @param prefix text to be added at start of name
     * @param suffix text to be added at end of name
     * @return field name
     */
    private String buildFieldName(String base, String prefix, String suffix) {
        
        // check for any change needed
        String name = base;
        int added = prefix.length() + suffix.length();
        boolean toupper = m_upperInitial && !Character.isUpperCase(name.charAt(0));
        if (added == 0) {
            
            // not adding prefix or suffix, check for case conversion
            if (toupper) {
                
                // convert first character to uppercase
                StringBuffer buff = new StringBuffer(name);
                buff.setCharAt(0, Character.toUpperCase(buff.charAt(0)));
                name = buff.toString();
                
            }
            
        } else {
            
            // append prefix and/or suffix, with case conversion if needed
            StringBuffer buff = new StringBuffer(name.length() + added);
            buff.append(prefix);
            int offset = buff.length();
            buff.append(name);
            if (toupper) {
                buff.setCharAt(offset, Character.toUpperCase(name.charAt(0)));
            }
            buff.append(suffix);
            name = buff.toString();
            
        }
        
        // make sure the result doesn't match a reserved name
        if (NameUtils.isReserved(name)) {
            name = "_" + name;
        }
        return name;
    }
    
    /**
     * Convert base name to normal field name. The returned name is guaranteed not to match a Java keyword.
     * 
     * @param base normalized camelcase base name
     * @return field name
     */
    public String toFieldName(String base) {
        String prefix = m_fieldPrefix;
        String suffix = m_fieldSuffix;
        return buildFieldName(base, prefix, suffix);
    }
    
    /**
     * Convert base name to static field name. The returned name is guaranteed not to match a Java keyword.
     * 
     * @param base normalized camelcase base name
     * @return field name
     */
    public String toStaticFieldName(String base) {
        String prefix = m_staticPrefix;
        String suffix = m_staticSuffix;
        return buildFieldName(base, prefix, suffix);
    }

    /**
     * Convert base name to property name (used for all method names). The property name is always in initial-upper
     * camelcase form.
     *
     * @param base normalized camelcase base name
     * @return property name in initial-upper camelcase form
     */
    public String toPropertyName(String base) {
        if (!Character.isUpperCase(base.charAt(0))) {
            
            // convert first character to uppercase
            StringBuffer buff = new StringBuffer(base);
            buff.setCharAt(0, Character.toUpperCase(buff.charAt(0)));
            return buff.toString();
            
        } else {
            
            // no change needed, just use base name directly
            return base;
            
        }
    }
    
    /**
     * Convert property name to read access method name.
     *
     * @param prop property name in initial-upper camelcase form
     * @return read access method name
     */
    public String toReadAccessMethodName(String prop) {
        return "get" + prop;
    }
    
    /**
     * Convert property name to write access method name.
     *
     * @param prop property name in initial-upper camelcase form
     * @return write access method name
     */
    public String toWriteAccessMethodName(String prop) {
        return "set" + prop;
    }
    
    /**
     * Convert property name to write access method name.
     *
     * @param prop property name in initial-upper camelcase form
     * @return write access method name
     */
    public String toTestAccessMethodName(String prop) {
        return "is" + prop;
    }
    
    /**
     * Convert property name to if set access method name (for value in set of alternatives).
     *
     * @param prop property name in initial-upper camelcase form
     * @return if set access method name
     */
    public String toIfSetAccessMethodName(String prop) {
        return "if" + prop;
    }
}
/*
 * Copyright (c) 2006-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen;

import java.util.HashSet;

/**
 * Support methods for working with Java names.
 * 
 * @author Dennis M. Sosnoski
 */
public class NameUtils
{
    /** Reserved words for Java (keywords and literals). */
    private static final HashSet s_reservedWords = new HashSet();
    static {
        
        // keywords
        s_reservedWords.add("abstract");
        s_reservedWords.add("assert");
        s_reservedWords.add("boolean");
        s_reservedWords.add("break");
        s_reservedWords.add("byte");
        s_reservedWords.add("case");
        s_reservedWords.add("catch");
        s_reservedWords.add("char");
        s_reservedWords.add("class");
        s_reservedWords.add("const");
        s_reservedWords.add("continue");
        
        s_reservedWords.add("default");
        s_reservedWords.add("do");
        s_reservedWords.add("double");
        s_reservedWords.add("else");
        s_reservedWords.add("enum");
        s_reservedWords.add("extends");
        s_reservedWords.add("final");
        s_reservedWords.add("finally");
        s_reservedWords.add("float");
        s_reservedWords.add("for");
        s_reservedWords.add("goto");
        
        s_reservedWords.add("if");
        s_reservedWords.add("implements");
        s_reservedWords.add("import");
        s_reservedWords.add("instanceof");
        s_reservedWords.add("int");
        s_reservedWords.add("interface");
        s_reservedWords.add("long");
        s_reservedWords.add("native");
        s_reservedWords.add("new");
        s_reservedWords.add("package");
        
        s_reservedWords.add("private");
        s_reservedWords.add("protected");
        s_reservedWords.add("public");
        s_reservedWords.add("return");
        s_reservedWords.add("short");
        s_reservedWords.add("static");
        s_reservedWords.add("strictfp");
        s_reservedWords.add("super");
        s_reservedWords.add("switch");
        s_reservedWords.add("synchronized");
        
        s_reservedWords.add("this");
        s_reservedWords.add("throw");
        s_reservedWords.add("throws");
        s_reservedWords.add("transient");
        s_reservedWords.add("try");
        s_reservedWords.add("void");
        s_reservedWords.add("volatile");
        s_reservedWords.add("while");
        
        // literals
        s_reservedWords.add("true");
        s_reservedWords.add("false");
        s_reservedWords.add("null");
    }
    
    /** Java default import class names (not exhaustive, but anything that might represent generated code conflict). */
    private static final HashSet s_defaultImportClassNames = new HashSet();
    static {
        
        // interfaces
        s_defaultImportClassNames.add("Appendable");
        s_defaultImportClassNames.add("CharSequence");
        s_defaultImportClassNames.add("Cloneable");
        s_defaultImportClassNames.add("Comparable");
        s_defaultImportClassNames.add("Iterable");
        s_defaultImportClassNames.add("Readable");
        s_defaultImportClassNames.add("Runnable");
        
        // classes
        s_defaultImportClassNames.add("Boolean");
        s_defaultImportClassNames.add("Byte");
        s_defaultImportClassNames.add("Character");
        s_defaultImportClassNames.add("Class");
        s_defaultImportClassNames.add("ClassLoader");
        s_defaultImportClassNames.add("Double");
        s_defaultImportClassNames.add("Enum");
        s_defaultImportClassNames.add("Float");
        s_defaultImportClassNames.add("Integer");
        s_defaultImportClassNames.add("Long");
        s_defaultImportClassNames.add("Math");
        s_defaultImportClassNames.add("Number");
        s_defaultImportClassNames.add("Object");
        s_defaultImportClassNames.add("Package");
        s_defaultImportClassNames.add("Short");
        s_defaultImportClassNames.add("String");
        s_defaultImportClassNames.add("StringBuffer");
        s_defaultImportClassNames.add("StringBuilder");
        s_defaultImportClassNames.add("System");
        s_defaultImportClassNames.add("Thread");
        s_defaultImportClassNames.add("Throwable");
        s_defaultImportClassNames.add("Void");
        
        // exceptions/errors
        s_defaultImportClassNames.add("Exception");
        s_defaultImportClassNames.add("Error");
    }
    
    /**
     * Check if a name is reserved in Java.
     * 
     * @param name
     * @return is reserved
     */
    public static boolean isReserved(String name) {
        return s_reservedWords.contains(name);
    }
    
    /**
     * Convert name if it is reserved in Java.
     * 
     * @param name
     * @return non-reserved name
     */
    public static String convertReserved(String name) {
        if (isReserved(name)) {
            return "_" + name;
        } else {
            return name;
        }
    }
    
    /**
     * Check if a class name is a default import in Java.
     * 
     * @param name
     * @return is reserved
     */
    public static boolean isDefaultImport(String name) {
        return s_defaultImportClassNames.contains(name);
    }
    
    /**
     * Convert a raw package name to a legal Java package name. The raw package name must be in standard package name
     * form, with periods separating the individual directory components of the package name.
     * 
     * @param raw basic package name, which may include illegal characters
     * @return sanitized package name
     */
    public static String sanitizePackageName(String raw) {
        StringBuffer buff = new StringBuffer(raw.length());
        boolean first = true;
        for (int i = 0; i < raw.length();) {
            char chr = buff.charAt(i);
            if (first) {
                if (Character.isJavaIdentifierStart(chr)) {
                    first = false;
                    i++;
                } else {
                    buff.deleteCharAt(i);
                }
            } else if (chr == '.') {
                first = true;
                i++;
            } else if (!Character.isJavaIdentifierPart(chr)) {
                buff.deleteCharAt(i);
            } else {
                i++;
            }
        }
        return buff.toString();
    }
    
    /**
     * Convert a word to a name component. This is intended for use when composing names, as when generating access
     * method names for a property. If the supplied word starts with one or more underscores, this first strips the
     * the underscores. If the resulting text starts with a lower case letter this then converts that character to upper
     * case. Finally, if the result either begins with a digit or is the word "Class" this prepends a leading underscore
     * back on.
     *
     * @param word
     * @return word with uppercase initial letter
     */
    public static String toNameWord(String word) {
        char chr;
        while ((chr = word.charAt(0)) == '_' && word.length() > 1) {
            word = word.substring(1);
        }
        if (Character.isLowerCase(chr)) {
            StringBuffer buff = new StringBuffer(word);
            buff.setCharAt(0, Character.toUpperCase(chr));
            word = buff.toString();
        }
        if ("Class".equals(word)) {
            return "_Class";
        } else if (Character.isDigit(word.charAt(0))) {
            return "_" + word;
        } else {
            return word;
        }
    }
    
    /**
     * Convert a word to a leading name component. If the supplied word starts with an uppercase letter which is not
     * followed by another uppercase letter, this converts the initial uppercase to lowercase.
     *
     * @param word
     * @return word with lowercase initial letter
     */
    public static String toNameLead(String word) {
        char chr = word.charAt(0);
        if (Character.isUpperCase(chr) && (word.length() < 2 || Character.isLowerCase(word.charAt(1)))) {
            StringBuffer buff = new StringBuffer(word);
            buff.setCharAt(0, Character.toLowerCase(chr));
            return buff.toString();
        } else {
            return word;
        }
    }
}
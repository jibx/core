/*
 * Copyright (c) 2009-2010, Dennis M. Sosnoski. All rights reserved.
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

/**
 * Interface for working with Java names during code generation.
 * 
 * @author Dennis M. Sosnoski
 */
public interface NameConverter
{
    /**
     * Convert singular name to plural form.
     * 
     * @param name base name
     * @return plural name
     */
    String pluralize(String name);
    
    /**
     * Convert potentially plural name to singular form.
     * 
     * @param name base name
     * @return singularized name
     */
    String depluralize(String name);
    
    /**
     * Convert an XML name to a Java value base name. The base name is in normalized camelcase form with leading lower
     * case (unless the first word of the name is all uppercase).
     * 
     * @param xname XML name
     * @return converted name
     */
    String toBaseName(String xname);
    
    /**
     * Convert a base name to a legal Java class name.
     * 
     * @param xname XML name
     * @return converted name
     */
    String toJavaClassName(String xname);
    
    /**
     * Convert text to constant name. The constant name must not match a Java keyword.
     *
     * @param text raw text to be converted
     * @return constant name
     */
    String toConstantName(String text);
    
    /**
     * Convert base name to normal field name. The field name must not match a Java keyword.
     * 
     * @param base normalized camelcase base name
     * @return field name
     */
    String toFieldName(String base);
    
    /**
     * Convert base name to static field name. The field name must not match a Java keyword.
     * 
     * @param base normalized camelcase base name
     * @return field name
     */
    String toStaticFieldName(String base);
    
    /**
     * Convert base name to property name (used for all method names). The property name is always in initial-upper
     * camelcase form.
     *
     * @param base normalized camelcase base name
     * @return property name in initial-upper camelcase form
     */
    String toPropertyName(String base);
    
    /**
     * Convert property name to read access method name.
     *
     * @param prop property name in initial-upper camelcase form
     * @return read access method name
     */
    String toReadAccessMethodName(String prop);
    
    /**
     * Convert property name to write access method name.
     *
     * @param prop property name in initial-upper camelcase form
     * @return write access method name
     */
    String toWriteAccessMethodName(String prop);
    
    /**
     * Convert property name to test access method name (for boolean value).
     *
     * @param prop property name in initial-upper camelcase form
     * @return test access method name
     */
    String toTestAccessMethodName(String prop);
    
    /**
     * Convert property name to if set access method name (for value in set of alternatives).
     *
     * @param prop property name in initial-upper camelcase form
     * @return if set access method name
     */
    String toIfSetAccessMethodName(String prop);

    /**
     * Trim specified prefixes and/or suffixes from an XML name.
     *
     * @param xname XML name
     * @return trimmed name, with specified prefixes and/or suffixes removed
     */
    public String trimXName(String xname);

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
    public String[] splitXMLWords(String name);
}
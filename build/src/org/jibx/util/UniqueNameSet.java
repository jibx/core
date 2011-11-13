/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Set of unique names for a context. This assures uniqueness as names are added to the set.
 * 
 * @author Dennis M. Sosnoski
 */
public class UniqueNameSet
{
    /** Set of names used. */
    private final Set m_nameSet;
    
    /**
     * Constructor.
     */
    public UniqueNameSet() {
        m_nameSet = new HashSet();
    }
    
    /**
     * Constructor from existing name collection. Creates a name set initialized to contain all the supplied names.
     * 
     * @param base 
     */
    public UniqueNameSet(Collection base) {
        m_nameSet = new HashSet(base);
    }
    
    /**
     * Copy constructor. Creates a name set initialized to contain all the names from another name set.
     * 
     * @param original
     */
    public UniqueNameSet(UniqueNameSet original) {
        this();
        addAll(original);
    }
    
    /**
     * Check if a name is already present in context.
     *
     * @param name
     * @return <code>true</code> if present, <code>false</code> if not
     */
    public boolean contains(String name) {
        return m_nameSet.contains(name);
    }
    
    /**
     * Add all the names from another name set to this set. This does not check for conflicts between the names in the
     * two sets.
     * 
     * @param other
     */
    public void addAll(UniqueNameSet other) {
        m_nameSet.addAll(other.m_nameSet);
    }
    
    /**
     * Add name to set. If the supplied name is already present, it is modified by appending a variable suffix. If the
     * supplied name ends with a digit, the suffix is generated starting with the letter 'a'; otherwise, it's generated
     * starting at '1'. Either way, the suffix is incremented as many times as necessary to obtain a unique name.
     * 
     * @param base name to try adding
     * @return assigned name
     */
    public String add(String base) {
        String name = base;
        int index = 1;
        boolean usealpha = base.length() > 0 && Character.isDigit(base.charAt(base.length()-1));
        while (m_nameSet.contains(name)) {
            if (usealpha) {
                String suffix = "";
                int value = index++ - 1;
                do {
                    int modulo = value % 52;
                    if (modulo > 26) {
                        suffix = (char)('A' + modulo - 26) + suffix;
                    } else {
                        suffix = (char)('a' + modulo) + suffix;
                    }
                    value /= 52;
                } while (value > 0);
                name = base + suffix;
            } else {
                name = base + index++;
            }
        }
        m_nameSet.add(name);
        return name;
    }
    
    /**
     * Get iterator for names in set.
     * 
     * @return iterator
     */
    public Iterator iterator() {
        return m_nameSet.iterator();
    }
}
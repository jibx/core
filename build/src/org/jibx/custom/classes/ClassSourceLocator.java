/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.custom.classes;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.runtime.JiBXException;
import org.jibx.util.IClass;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * Locator that supports both class file lookup and source file lookup.
 * 
 * @author Dennis M. Sosnoski
 */
public class ClassSourceLocator implements IClassSourceLocator
{
    /** Paths for source lookup. */
    private final String[] m_sourcePaths;
    
    /** Source file parser. */
    private final JavaDocBuilder m_builder;
    
    /** Set of classes parsed. */
    private final Set m_lookupSet;
    
    /**
     * Constructor.
     * 
     * @param paths source lookup paths (may be empty, but not <code>null</code>)
     */
    public ClassSourceLocator(String[] paths) {
        m_sourcePaths = paths;
        m_builder = new JavaDocBuilder();
        m_lookupSet = new HashSet();
    }
    
    /**
     * Check if class lookup is supported. This always returns <code>true</code>.
     *
     * @return <code>true</code>
     */
    public boolean isLookupSupported() {
        return true;
    }
    
    /**
     * Get the source code information for a class.
     * 
     * @param name fully-qualified class name (using '$' as inner class marker)
     * @return source code information, <code>null</code> if not available
     */
    public JavaClass getSourceInfo(String name) {
        int split = name.lastIndexOf('$');
        if (split >= 0) {
            JavaClass outer = getSourceInfo(name.substring(0, split));
            if (outer == null) {
                return null;
            } else {
                return outer.getNestedClassByName(name.substring(split + 1));
            }
        } else if (m_lookupSet.contains(name)) {
            return m_builder.getClassByName(name);
        } else {
            for (int i = 0; i < m_sourcePaths.length; i++) {
                StringBuffer buff = new StringBuffer();
                buff.append(m_sourcePaths[i]);
                int length = buff.length();
                if (File.separatorChar != '/') {
                    for (int j = 0; j < length; j++) {
                        if (buff.charAt(j) == '/') {
                            buff.setCharAt(j, File.separatorChar);
                        }
                    }
                }
                if (length > 0 || buff.charAt(length - 1) != File.separatorChar) {
                    buff.append(File.separatorChar);
                }
                buff.append(name.replace('.', File.separatorChar));
                buff.append(".java");
                File file = new File(buff.toString());
                if (file.exists()) {
                    try {
                        m_builder.addSource(file);
                        m_lookupSet.add(name);
                        return m_builder.getClassByName(name);
                    } catch (IOException e) {
                        throw new IllegalStateException("Unable to access source file " + buff.toString() + ": " +
                            e.getMessage());
                    }
                }
            }
            return null;
        }
    }
    
    /**
     * Get the information for a class.
     * 
     * @param name fully-qualified class name (using '$' as inner class marker)
     * @return class information, or <code>null</code> if not found
     */
    public IClass getClassInfo(String name) {
        try {
            ClassFile clas = ClassCache.getClassFile(name);
            if (clas == null) {
                return null;
            } else {
                return new ClassSourceWrapper(this, clas);
            }
        } catch (JiBXException e) {
            throw new IllegalStateException("Error loading class " + name + ": " + e.getMessage());
        }
    }
    
    /**
     * Get required class information. If the class cannot be found a runtime
     * exception is thrown.
     *
     * @param name fully-qualified name of class to be found
     * @return class information
     */
    public IClass getRequiredClassInfo(String name) {
        IClass iclas = getClassInfo(name);
        if (iclas == null) {
            throw new IllegalStateException("Internal error: class " + name +
                " cannot be found");
        } else {
            return iclas;
        }
    }
    
    /**
     * Load class.
     *
     * @param name fully-qualified class name
     * @return loaded class, or <code>null</code> if not found
     */
    public Class loadClass(String name) {
        return ClassFile.loadClass(name);
    }
}
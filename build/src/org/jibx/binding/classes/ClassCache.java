/*
Copyright (c) 2003-2009, Dennis M. Sosnoski
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

package org.jibx.binding.classes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.bcel.classfile.Utility;
import org.jibx.binding.model.ClassWrapper;
import org.jibx.runtime.JiBXException;
import org.jibx.util.IClass;
import org.jibx.util.IClassLocator;

/**
 * Cache for class files being modified. Handles loading and saving of class
 * files. Classes are loaded directly from the file system paths supplied on
 * initialization in preference to the system class path.
 *
 * @author Dennis M. Sosnoski
 */
public class ClassCache
{
    /** Singleton instance of class (created when paths set) */
    private static ClassCache s_instance;
    
    /** Classes to be preserved without modification. */
    private static HashSet s_preserveClasses;
    
    /** Paths to be searched for class files. */
    private String[] m_paths;
    
    /** Root directories corresponding to paths. */
    private File[] m_roots;
    
    /** Map from class names to actual class information. */
    private HashMap m_classMap;
    
    /**
     * Constructor. Discards jar file paths and normalizes all other paths
     * (except the empty path) to end with the system path separator character.
     *
     * @param paths ordered set of paths to be searched for class files
     */
    private ClassCache(String[] paths) {
        ArrayList keepers = new ArrayList();
        for (int i = 0; i < paths.length; i++) {
            File file = new File(paths[i]);
            if (file.isDirectory() && file.canWrite()) {
                keepers.add(paths[i]);
            }
        }
        m_paths = new String[keepers.size()];
        m_roots = new File[keepers.size()];
        for (int i = 0; i < keepers.size(); i++) {
            String path = (String)keepers.get(i);
            int length = path.length();
            if (length > 0) {
                if (File.separatorChar != '/') {
                    path = path.replace('/', File.separatorChar);
                }
                if (path.charAt(length-1) != File.separatorChar) {
                    path = path + File.separator;
                }
            }
            m_paths[i] = path;
            m_roots[i] = new File(path);
        }
        m_classMap = new HashMap();
    }
    
    /**
     * Get class information. Looks up the class in cache, and if not already
     * present tries to find it based on the class file search path list. If
     * the class file is found it is loaded and this method calls itself
     * recursively to load the whole hierarchy of superclasses.
     *
     * @param name fully-qualified name of class to be found
     * @return class information, or <code>null</code> if class not found
     * @throws JiBXException on any error accessing class file
     */
    private ClassFile getClassFileImpl(String name) throws JiBXException {
        
        // first try for match on fully-qualified class name
        Object match = m_classMap.get(name);
        if (match != null) {
            return (ClassFile)match;
        } else if (ClassItem.isPrimitive(name) || name.endsWith("[]")) {
            
            // create synthetic class file for array type
            ClassFile cf = new ClassFile(name, Utility.getSignature(name));
            m_classMap.put(name, cf);
            return cf;
            
        } else {
            try {
                
                // got to load it, convert to file path format
                ClassFile cf = null;
                String path = name.replace('.', File.separatorChar) + ".class";
                for (int i = 0; i < m_paths.length; i++) {
                
                    // check for file found off search path
                    File file = new File(m_paths[i], path);
                    if (file.exists()) {
                    
                        // load file information
                        cf = new ClassFile(name, m_roots[i], file);
                        break;
                        
                    }
                }
            
                // finally try loading (non-modifiable) class from classpath
                if (cf == null) {
                    cf = ClassFile.getClassFile(name);
                }
            
                // check for class found
                if (cf != null) {
                
                    // force loading of superclass as well (to root)
                    String sname = cf.getSuperName();
                    if (!name.equals(sname) && sname != null) {
                        ClassFile sf = getClassFileImpl(sname);
                        if (sf == null) {
                            throw new JiBXException("Superclass " + sname +
                                " of class " + name + " not found");
                        }
                        cf.setSuperFile(sf);
                    }
                    
                    // add class information to cache
                    m_classMap.put(name, cf);
                
                }
                return cf;
                
            } catch (IOException ex) {
                throw new JiBXException("Error loading class " + name);
            }
        }
    }
    
    /**
     * Add class name to set to be preserved without modification. If the class
     * information has already been loaded, this forces the modifiable setting
     * to unmodifiable.
     *
     * @param name fully-qualified class name
     */
    public static void addPreserveClass(String name) {
        s_preserveClasses.add(name);
        ClassFile cf = (ClassFile)s_instance.m_classMap.get(name);
        if (cf != null) {
            cf.setUnmodifiable();
        }
    }

    /**
     * Check if a class is to be preserved without modification.
     *
     * @param name fully-qualified class name
     * @return <code>true</code> if to be preserved, <code>false</code> if not
     */
    public static boolean isPreserveClass(String name) {
        return s_preserveClasses.contains(name);
    }

    /**
     * Get class information. Looks up the class in cache, and if not already
     * present tries to find it based on the class file search path list. If
     * the class file is found it is loaded along with all superclasses.
     *
     * @param name fully-qualified name of class to be found
     * @return class information, or <code>null</code> if class not found
     * @throws JiBXException on any error accessing class file
     */
    public static ClassFile getClassFile(String name) throws JiBXException {
        return s_instance.getClassFileImpl(name);
    }

    /**
     * Get required class information. Looks up the class in cache, and if not
     * already present tries to find it based on the class file search path
     * list. If the class is not found this throws a runtime exception.
     *
     * @param name fully-qualified name of class to be found
     * @return class information, or <code>null</code> if class not found
     * @throws JiBXException on any error accessing class file
     */
    public static ClassFile requireClassFile(String name) throws JiBXException {
        ClassFile cf = s_instance.getClassFileImpl(name);
        if (cf == null) {
            throw new IllegalArgumentException("Internal error - class " + name + " not found in classpath");
        } else {
            return cf;
        }
    }
    
    /**
     * Check if class information has been loaded.
     *
     * @param name full-qualified name of class to be checked
     * @return <code>true</code> if found
     */
    public static boolean hasClassFile(String name) {
        return s_instance.m_classMap.containsKey(name);
    }
    
    /**
     * Add created class information to cache.
     *
     * @param cf information for class to be added
     */
    /*package*/ static void addClassFile(ClassFile cf) {
        s_instance.m_classMap.put(cf.getName(), cf);
    }
    
    /**
     * Return the first modifiable directory path from the classpath in use.
     *
     * @return modifiable directory, or <code>null</code> if none
     */
    public static File getModifiablePath() {
        if (s_instance.m_roots.length > 0) {
            return s_instance.m_roots[0];
        } else {
            return null;
        }
    }
    
    /**
     * Set class paths to be searched. Discards jar file paths and normalizes
     * all other paths (except the empty path) to end with the system path
     * separator character.
     *
     * @param paths ordered set of paths to be searched for class files
     */
    public static void setPaths(String[] paths) {
        s_preserveClasses = new HashSet();
        s_instance = new ClassCache(paths);
    }
    
    /**
     * Locator using the cache.
     */
    public static class ClassCacheLocator implements IClassLocator
    {
        public boolean isLookupSupported() {
            return true;
        }
        
        public IClass getClassInfo(String name) {
            try {
                ClassFile cf = ClassCache.getClassFile(name);
                if (cf == null) {
                    return null;
                } else {
                    return new ClassWrapper(this, cf);
                }
            } catch (JiBXException e) {
                throw new IllegalStateException("Error loading class " + name + ": " + e.getMessage());
            }
        }
        public IClass getRequiredClassInfo(String name) {
            IClass iclas = getClassInfo(name);
            if (iclas == null) {
                throw new IllegalStateException("Internal error: class " + name + " cannot be found");
            } else {
                return iclas;
            }
        }
        public Class loadClass(String name) {
            return ClassFile.loadClass(name);
        }
    };
}
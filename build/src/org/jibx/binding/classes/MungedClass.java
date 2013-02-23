/*
Copyright (c) 2003-2009, Dennis M. Sosnoski.
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
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.bcel.Constants;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.JiBXException;

/**
 * Modifiable class handler. Each instance controls changes to a particular
 * class modified by one or more binding definitions. As methods are generated
 * they're checked for uniqueness. If an already-generated method is found with
 * the same characteristics (including byte code) as the one being generated,
 * the already-generated is used instead.
 *
 * @author Dennis M. Sosnoski
 */
public class MungedClass
{
    //
    // Constants and such related to code generation.
    
    /** Empty class file array. */
    private static final ClassFile[] EMPTY_CLASSFILE_ARRAY = {};
    
    /** Name and signature for generated methods without standard prefix. */
    private static final String[] EXTRA_METHODS_MATCHES =
    {
        "marshal", "(Lorg/jibx/runtime/IMarshallingContext;)V",
        "unmarshal", "(Lorg/jibx/runtime/IUnmarshallingContext;)V"
    };
    
    //
    // Static data.
    
    /** Munged class information. */
    private static ArrayList s_classes;
    
    /** Set of class names in list (used to assure uniqueness). */
    private static HashSet s_classNameSet;
    
    /** Map from generated class to binding information. */
    private static HashMap s_classMap;
    
    /** Map of directories already checked for JiBX classes. */
    private static HashMap s_directories;
    
    /** Map from class name to binding information. */
    private static HashMap s_nameMap;
    
    /** Munged classes to be unique-added at end of binding. */
    private static ArrayList s_pendingClasses;
    
    /** Munged class file information. */
    private ClassFile m_classFile;
    
    /** Map from method byte code and signature to method item. */
    private HashMap m_methodMap;
    
    /** Existing binding methods in class. */
    private ExistingMethod[] m_existingMethods;
    
    /** List of factory names for this class. */
    private String m_factoryList;

    /**
     * Constructor. This sets up for modifying a class with added methods.
     *
     * @param cf owning class file information
     */
    private MungedClass(ClassFile cf) {
        
        // initialize basic class information
        m_classFile = cf;
        m_methodMap = new HashMap();
        
        // get information for existing binding methods in this class
        String[] extras = EXTRA_METHODS_MATCHES;
        if (cf.isSuperclass("java.lang.Enum")) {
            
            // enum class, add the serialize/deserialize methods to patterns
            String[] copy = extras;
            int length = copy.length;
            extras = new String[length+4];
            System.arraycopy(copy, 0, extras, 0, length);
            extras[length++] = "_jibx_serialize";
            extras[length++] = "(" + cf.getSignature() + ")Ljava/lang/String;";
            extras[length++] = "_jibx_deserialize";
            extras[length] = "(Ljava/lang/String;)" + cf.getSignature();
        }
        ExistingMethod[] exists = cf.getBindingMethods
            (BindingDirectory.GENERATE_PREFIX, extras);
        if (exists != null) {
            for (int i = 0; i < exists.length; i++) {
                m_methodMap.put(exists[i], exists[i]);
            }
        }
        m_existingMethods = exists;
    }

    /**
     * Get munged class file information.
     *
     * @return class file information for bound class
     */
    /*package*/ ClassFile getClassFile() {
        return m_classFile;
    }

    /**
     * Delete pre-existing binding methods that are no longer needed.
     */
    private void purgeUnusedMethods() {
        if (m_existingMethods != null) {
            for (int i = 0; i < m_existingMethods.length; i++) {
                ExistingMethod method = m_existingMethods[i];
                if (!method.isUsed()) {
                    method.delete();
                }
            }
        }
    }

    /**
     * Get unique method. If a method matching the byte code of the supplied
     * method has already been defined the existing method is returned.
     * Otherwise the method is added to the definitions. If necessary, a number
     * suffix is appended to the method name to prevent conflicts with existing
     * names.
     *
     * @param builder method to be defined
     * @param suffix append name suffix to assure uniqueness flag
     * @return defined method item
     */
    /*package*/ BindingMethod getUniqueMethod(MethodBuilder builder,
        boolean suffix) {
        
        // try to find already added method with same characteristics
        if (builder.getClassFile() != m_classFile) {
            throw new IllegalStateException
                ("Internal error: wrong class for call");
        }
        builder.codeComplete(suffix);
        BindingMethod method = (BindingMethod)m_methodMap.get(builder);
        if (method == null) {
//          System.out.println("No match found for method " +
//              builder.getClassFile().getName()+ '.' + builder.getName() +
//              "; adding method");
            
            // create as new method
            builder.addMethod();
            m_methodMap.put(builder, builder);
            return builder;
            
        } else if (method instanceof ExistingMethod) {
            ((ExistingMethod)method).setUsed();
        }
//      System.out.println("Found " + method.getClassFile().getName()+
//          '.' + method.getName() + " as match for " + builder.getName());
        return method;
    }

    /**
     * Get unique generated support class. Allows tracking of all generated
     * classes for common handling with the bound classes. Each generated
     * support class is associated with a particular bound class.
     *
     * @param cf generated class file
     * @return unique class file information
     */
    public static ClassFile getUniqueSupportClass(ClassFile cf) {
        cf.codeComplete();
        Object value = s_classMap.get(cf);
        if (value == null) {
            if (!s_classNameSet.contains(cf.getName())) {
                s_classes.add(cf);
                s_classNameSet.add(cf.getName());
            }
            s_classMap.put(cf, cf);
            return cf;
        } else {
            ClassFile prior = (ClassFile)value;
            prior.incrementUseCount();
            return prior;
        }
    }

    /**
     * Check directory for JiBX generated files. Scans through all class files
     * in the target directory and loads any that start with the JiBX
     * identifier string which have not been added to the preserve list.
     *
     * @param root class path root for directory (non-<code>null</code>)
     * @param pack package relative to root directory
     * @throws JiBXException on configuration error
     */
    /*package*/ static void checkDirectory(File root, String pack)
        throws JiBXException {
    	if (root == null) {
    		throw new IllegalArgumentException("Should never be called with null root");
    	}
        try {
            File directory = new File
                (root, pack.replace('.', File.separatorChar));
            String cpath = directory.getCanonicalPath();
            if (s_directories.get(cpath) == null) {
                File[] matches = new File(cpath).listFiles(new JiBXFilter());
                for (int i = 0; i < matches.length; i++) {
                    File file = matches[i];
                    String name = file.getName();
                    int split = name.indexOf('.');
                    if (split >= 0) {
                        name = name.substring(0, split);
                    }
                    if (pack.length() > 0) {
                        name = pack + '.' + name;
                    }
                    if (!ClassCache.isPreserveClass(name)) {
                        ClassFile cf = ClassCache.requireClassFile(name);
                        s_classes.add(cf);
                        s_classMap.put(cf, cf);
                    }
                }
                s_directories.put(cpath, cpath);
            }
        } catch (IOException ex) {
            throw new JiBXException("Error loading class file", ex);
        }
    }

    /**
     * Add binding factory to class. The binding factories are accumulated as
     * a delimited string during generation, then dumped to a static field in
     * the class at the end.
     *
     * @param fact binding factory name
     */
    /*package*/ void addFactory(String fact) {
        String match = "|" + fact + "|";
        if (m_factoryList == null) {
            m_factoryList = match;
        } else if (m_factoryList.indexOf(match) < 0) {
            m_factoryList = m_factoryList + fact + "|";
        }
    }

    /**
     * Generate factory list. Adds or replaces the existing static array of
     * factories in the class.
     */
    /*package*/ void setFactoryList() {
        if (m_factoryList != null) {
            short access = Constants.ACC_PUBLIC | Constants.ACC_FINAL |
                 Constants.ACC_STATIC;
            m_classFile.updateField("java.lang.String",
                BindingDirectory.BINDINGLIST_NAME, access, m_factoryList);
        }
    }

    /**
     * Get modification tracking information for class.
     *
     * @param cf information for class to be modified (must be writable)
     * @return binding information for class
     * @throws JiBXException on configuration error
     */
    /*package*/ static MungedClass getInstance(ClassFile cf)
        throws JiBXException {
        String name = cf.getName();
        MungedClass inst = (MungedClass)s_nameMap.get(name);
        if (inst == null) {
            if (ClassCache.isPreserveClass(name)) {
                throw new IllegalStateException("Internal error - attempt to modify class in preserve set");
            }
            inst = new MungedClass(cf);
            s_nameMap.put(name, inst);
            if (cf.isComplete()) {
                if (s_classMap.get(cf) == null) {
                    if (!s_classNameSet.contains(name)) {
                        s_classes.add(inst);
                        s_classNameSet.add(name);
                    }
                    s_classMap.put(cf, cf);
                } else {
                    throw new IllegalStateException
                        ("Existing class conflicts with load");
                }
                String pack = cf.getPackage();
                File root = cf.getRoot();
                if (root != null) {
                	checkDirectory(root, pack);
                }
            }
        }
        inst.m_classFile.incrementUseCount();
        return inst;
    }

    /**
     * Add unique support class at end of binding process. This allows a class
     * to be constructed in steps during handling of one or more bindings, with
     * the class finished and checked for uniqueness only after all bindings
     * have been handled. The actual add of the class is done during the
     * {@link #fixDispositions()} handling.
     *
     * @param cf class file to be added as unique support class at end of
     * binding
     */
    public static void delayedAddUnique(ClassFile cf) {
        s_pendingClasses.add(cf);
    }
    
    /**
     * Add class file to set modified. This uses the class name as a identifier
     * to prevent duplicate entries in the list, so that classes which are still
     * under construction can be handled.
     *
     * @param cf
     */
    public static void addModifiedClass(ClassFile cf) {
        String name = cf.getName();
        if (!s_classNameSet.contains(name)) {
            if (ClassCache.isPreserveClass(name)) {
                throw new IllegalStateException("Internal error - attempt to modify class in preserve set");
            } else {
                s_classes.add(cf);
                s_classNameSet.add(name);
            }
        }
    }
    
    /**
     * Finalize class name assignments and dispositions.
     *
     * @return three-way array of class files, for modified, unchanged, and
     * deleted
     */
    public static ClassFile[][] fixDispositions() {
        
        // process all pending class adds
        for (int i = 0; i < s_pendingClasses.size(); i++) {
            getUniqueSupportClass((ClassFile)s_pendingClasses.get(i));
        }
        
        // finalize and categorize classes
        ArrayList writes = new ArrayList();
        ArrayList keeps = new ArrayList();
        ArrayList deletes = new ArrayList();
        for (int i = 0; i < s_classes.size(); i++) {
            Object obj = s_classes.get(i);
            ClassFile cf;
            if (obj instanceof MungedClass) {
                MungedClass inst = (MungedClass)obj;
                inst.purgeUnusedMethods();
                inst.setFactoryList();
                cf = inst.getClassFile();
            } else {
                cf = (ClassFile)obj;
            }
            if (cf.isModified()) {
                writes.add(cf);
            } else if (cf.getUseCount() > 0 || ClassCache.isPreserveClass(cf.getName())) {
                keeps.add(cf);
            } else {
                deletes.add(cf);
            }
        }
        
        // return changes in separate lists
        ClassFile[][] results = new ClassFile[3][];
        results[0] = (ClassFile[])writes.toArray(EMPTY_CLASSFILE_ARRAY);
        results[1] = (ClassFile[])keeps.toArray(EMPTY_CLASSFILE_ARRAY);
        results[2] = (ClassFile[])deletes.toArray(EMPTY_CLASSFILE_ARRAY);
        return results;
    }
    
    /**
     * Write all modified class files, and delete all obsolete class files from
     * previous runs of the binding compiler.
     *
     * @throws JiBXException on write error
     */
    public static void writeChanges() throws JiBXException {
        try {
            
            // write all modifications and delete unused old files
            for (int i = 0; i < s_classes.size(); i++) {
                Object obj = s_classes.get(i);
                ClassFile cf;
                if (obj instanceof MungedClass) {
                    MungedClass inst = (MungedClass)obj;
                    cf = inst.getClassFile();
                } else {
                    cf = (ClassFile)obj;
                }
                if (cf.isModified()) {
                    cf.writeFile();
                } else if (cf.getUseCount() == 0 && !ClassCache.isPreserveClass(cf.getName())) {
                    cf.delete();
                }
            }
            
        } catch (IOException ex) {
            throw new JiBXException("Error writing to file", ex);
        }
    }
    /**
     * Discard cached information and reset in preparation for a new binding
     * run.
     */
    public static void reset() {
        s_classes = new ArrayList();
        s_classNameSet = new HashSet();
        s_classMap = new HashMap();
        s_directories = new HashMap();
        s_nameMap = new HashMap();
        s_pendingClasses = new ArrayList();
    }
    
    /**
     * Filter for class files generated by JiBX.
     */
    private static class JiBXFilter implements FileFilter
    {
        public boolean accept(File file) {
            String name = file.getName();
            return name.startsWith(BindingDirectory.GENERATE_PREFIX) &&
            name.endsWith(".class");
        }
    }
}
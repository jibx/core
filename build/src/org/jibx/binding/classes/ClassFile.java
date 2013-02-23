/*
Copyright (c) 2003-2012, Dennis M. Sosnoski.
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.CodeException;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.classfile.ConstantValue;
import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Synthetic;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ClassPath;
import org.jibx.runtime.JiBXException;

/**
 * Class file information. Wraps the actual class file data as well as
 * associated management information.
 *
 * @author Dennis M. Sosnoski
 */
public class ClassFile
{
    /** Major version number for Java 7. */
	private static final int JAVA7_MAJOR_VERSION = 51;
	//
    // Constants for code generation.
    
    public static final int PRIVATE_ACCESS = 0;
    public static final int PACKAGE_ACCESS = 1;
    public static final int PROTECTED_ACCESS = 2;
    public static final int PUBLIC_ACCESS = 3;
    
    public static final int SYNTHETIC_ACCESS_FLAG = 0x1000;
    
    protected static final int PRIVATEFIELD_ACCESS =
        Constants.ACC_PRIVATE | SYNTHETIC_ACCESS_FLAG;
    protected static final ExistingMethod[] EMPTY_METHOD_ARRAY = {};
    protected static final byte[] EMPTY_BYTES = new byte[0];
    
    public static final ClassItem[] EMPTY_CLASS_ITEMS = new ClassItem[0];
    
    //
    // Class data.
    
    /** Singleton loader from classpath. */
    private static ClassPath s_loader;
    
    /** Direct class loader. */
    private static URLClassLoader s_directLoader;
        
    //
    // Actual instance data.

    /** Fully qualified class name. */
    private String m_name;

    /** Signature for class as type. */
    private String m_signature;
    
    /** Class as type. */
    private Type m_type;

    /** Directory root for class. */
    private File m_root;
    
    /** Load path (used to report source of unmodifiable class). */
    private String m_path;

    /** Actual class file information. */
    private File m_file;

    /** Class in same package as superclass flag. */
    private boolean m_isSamePackage;

    /** File is writable flag. */
    private boolean m_isWritable;
    
    /** Binding code can be added to class flag. */
    private boolean m_isExtendable;

    /** Super class of this class (set by caller, since it may require
     additional information to find the class file). */
    protected ClassFile m_superClass;
    
    /** Names of all interfaces directly implemented by this class. */
    protected String[] m_interfaceNames;
    
    /** Class files of interfaces extended by interface. */
    private ClassFile[] m_superInterfaces;
    
    /** All classes and interfaces of which this is an instance (lazy create,
     only if needed. */
    private String[] m_instanceOfs;
    
    /** All methods defined by this class or interface (lazy create, only if
     needed). */
    private Method[] m_methods;

    /** Base class information as loaded by BCEL. */
    private JavaClass m_curClass;

    /** Modified class generator (lazy create, only if needed). */
    private ClassGen m_genClass;

    /** Constant pool generator for modified class (lazy create, only if
     needed). */
    private ConstantPoolGen m_genPool;

    /** Instruction factory for modified class (lazy create, only if needed). */
    protected InstructionBuilder m_instBuilder;
    
    /** Map for method names with possibly generated suffixes (lazy create, only
     if needed). */
    private HashMap m_suffixMap;
    
    /** Map to class item information. */
    private HashMap m_itemMap;
    
    /** Flag for class modified. */
    private boolean m_isModified;
    
    /** Usage count for this class. */
    private int m_useCount;
    
    /** Hash code computation for class is current flag. */
    private boolean m_isHashCurrent;
    
    /** Cached hash code value for class. */
    private int m_hashCode;
    
    /** Depth of superclass hierarchy for class (lazy computation). */
    private int m_inheritDepth;
    
    /** Suffix number for making method names unique (lazy computation). */
    private int m_uniqueIndex;
    
    /** Added default constructor for class. */
    private ClassItem m_defaultConstructor;

    /**
     * Constructor for preexisting class file. Loads the class data and
     * prepares it for use.
     *
     * @param name fully qualified class name
     * @param root directory root from class loading path list
     * @param file actual class file
     * @throws IOException if unable to open file
     * @throws JiBXException if error in reading class file
     */
    public ClassFile(String name, File root, File file)
        throws IOException, JiBXException {
        init(name, root.getPath(), new FileInputStream(file));
        m_root = root;
        m_path = root.getAbsolutePath();
        m_file = file;
        m_isWritable = file.canWrite() && !ClassCache.isPreserveClass(name);
        m_isExtendable = m_isWritable && m_curClass.getMajor() < JAVA7_MAJOR_VERSION;
    }

    /**
     * Constructor for synthetic placeholder classfile with no backing class
     * data.
     *
     * @param name fully qualified class name
     * @param sig corresponding class signature
     */
    public ClassFile(String name, String sig) {
        m_path = "<synthetic>";
        m_name = name;
        m_signature = sig;
        m_type = Type.getType(sig);
        m_interfaceNames = new String[0];
        m_superInterfaces = new ClassFile[0];
        m_itemMap = new HashMap();
    }

    /**
     * Constructor for new class file. Initializes the class data and
     * prepares it for use.
     *
     * @param name fully qualified class name
     * @param root directory root from class loading path list
     * @param sclas superclass of new class
     * @param access access flags for class
     * @param impls array of interfaces implemented by new class
     * (non-<code>null</code>, empty if none)
     * @throws JiBXException on error loading interface information
     */
    public ClassFile(String name, File root, ClassFile sclas, int access,
        String[] impls) throws JiBXException {
        String fname = name.replace('.', File.separatorChar)+".class";
        File file = new File(root, fname);
        m_name = name;
        m_signature = Utility.getSignature(name);
        m_type = ClassItem.typeFromName(name);
        m_root = root;
        m_path = "<new class>";
        m_superClass = sclas;
        if (impls == null) {
            throw new IllegalArgumentException
                ("Internal error - non-null array required");
        }
        m_interfaceNames = impls;
        m_file = file;
        m_isWritable = true;
        m_isExtendable = true;
        m_genClass = new ClassGen(name, sclas.getName(), "",
            access | SYNTHETIC_ACCESS_FLAG, impls);
        m_genPool = m_genClass.getConstantPool();
        int index = m_genPool.addUtf8("Synthetic");
        m_genClass.addAttribute(new Synthetic(index, 0, EMPTY_BYTES,
            m_genPool.getConstantPool()));
        m_instBuilder = new InstructionBuilder(m_genClass, m_genPool);
        m_itemMap = new HashMap();
        initInterface();
        ClassCache.addClassFile(this);
    }

    /**
     * Constructor for preexisting class file from classpath. Loads the class
     * data and prepares it for use.
     *
     * @param name fully qualified class name
     * @throws IOException if unable to open file
     * @throws JiBXException if error loading superclass or other support file
     */
    private ClassFile(String name, ClassPath.ClassFile cf) throws JiBXException,
        IOException {
        m_path = cf.getBase();
        init(name, cf.getPath(), cf.getInputStream());
    }

    /**
     * Constructor for preexisting class file from classpath. Loads the class
     * data and prepares it for use.
     *
     * @param name fully qualified class name
     * @return null if unable to find class file
     * @throws IOException if error reading file
     * @throws JiBXException if error loading superclass or other support file
     */
    public static ClassFile getClassFile(String name) throws IOException,
        JiBXException {
        
        // try out class path first, then BCEL system path
        ClassPath.ClassFile cf = null;
        try {
             cf = s_loader.getClassFile(name);
        } catch (IOException ex) {
            try {
                cf = ClassPath.SYSTEM_CLASS_PATH.getClassFile(name);
            } catch (IOException ex1) { /* deliberately left empty */ }
        }
        if (cf == null) {
            return null;
        } else {
            return new ClassFile(name, cf);
        }
    }

    /**
     * Internal initialization method. This is used to handle common
     * initialization for the constructors.
     *
     * @param name fully qualified class name
     * @param path class file path
     * @param ins input stream for class file data
     * @throws JiBXException if unable to load class file
     */
    private void init(String name, String path, InputStream ins)
        throws JiBXException {
        m_name = name;
        m_signature = Utility.getSignature(name);
        m_type = ClassItem.typeFromName(name);
        m_itemMap = new HashMap();
        if (path == null) {
            m_interfaceNames = new String[0];
        } else {
            String fname = name.replace('.', File.separatorChar) + ".class";
            ClassParser parser = new ClassParser(ins, fname);
            try {
                m_curClass = parser.parse();
                m_interfaceNames = m_curClass.getInterfaceNames();
                if (m_interfaceNames == null) {
                    m_interfaceNames = new String[0];
                }
            } catch (Exception ex) {
                throw new JiBXException("Error reading path " +
                    path + " for class " + name);
            }
        }
        initInterface();
    }
    
    /**
     * Retrieve superinterfaces for an interface class. These are collected at
     * initialization so that we can support getting the full set of methods
     * later without worrying about throwing an exception.
     * 
     * @throws JiBXException on error loading interface information
     */
    private void initInterface() throws JiBXException {
        if (isInterface() && m_interfaceNames.length > 0) {
            ClassFile[] supers = new ClassFile[m_interfaceNames.length];
            for (int i = 0; i < m_interfaceNames.length; i++) {
                supers[i] = ClassCache.requireClassFile(m_interfaceNames[i]);
            }
            m_superInterfaces = supers;
        } else {
            m_superInterfaces = new ClassFile[0];
        }
    }

    /**
     * Check if class is an interface. This only checks existing classes,
     * assuming that no generated classes are interfaces.
     *
     * @return <code>true</code> if an interface, <code>false</code> if not
     */
    public boolean isInterface() {
        return m_curClass != null && m_curClass.isInterface();
    }

    /**
     * Check if class is abstract. This only checks existing classes,
     * assuming that no generated classes are abstract.
     *
     * @return <code>true</code> if an abstract class, <code>false</code> if not
     */
    public boolean isAbstract() {
        return m_curClass != null && m_curClass.isAbstract();
    }

    /**
     * Check if class is an array. This only checks existing classes,
     * assuming that no generated classes are arrays.
     *
     * @return <code>true</code> if an array class, <code>false</code> if not
     */
    public boolean isArray() {
        return m_name.endsWith("[]");
    }

    /**
     * Check if class is modifiable.
     *
     * @return <code>true</code> if class is modifiable, <code>false</code> if
     * not
     */
    public boolean isModifiable() {
        return m_isWritable && !isInterface();
    }
    
    /**
     * Check if binding methods can be added to class.
     *
     * @return <code>true</code> if methods can be added, <code>false</code> if not
     */
    public boolean isExtendable() {
    	return m_isExtendable;
    }

    /**
     * Get fully qualified class name.
     *
     * @return fully qualified name for class
     */
    public String getName() {
        return m_name;
    }

    /**
     * Get signature for class as type.
     *
     * @return signature for class used as type
     */
    public String getSignature() {
        return m_signature;
    }

    /**
     * Get class as type.
     *
     * @return class as type
     */
    public Type getType() {
        return m_type;
    }

    /**
     * Get package name.
     *
     * @return package name for class
     */
    public String getPackage() {
        int split = m_name.lastIndexOf('.');
        if (split >= 0) {
            return m_name.substring(0, split);
        } else {
            return "";
        }
    }

    /**
     * Get root directory for load path.
     *
     * @return root directory in path used for loading file
     */
    public File getRoot() {
        return m_root;
    }

    /**
     * Get actual file for class.
     *
     * @return file used for class
     */
    public File getFile() {
        return m_file;
    }

    /**
     * Get raw current class information.
     *
     * @return raw current class information
     */
    public JavaClass getRawClass() {
        if (m_curClass == null) {
            throw new IllegalStateException
                ("No loadable class information for " + m_name);
        } else {
            return m_curClass;
        }
    }

    /**
     * Get superclass name.
     *
     * @return fully qualified name of superclass
     */
    public String getSuperName() {
        if (m_curClass == null) {
            return null;
        } else {
            return m_curClass.getSuperclassName();
        }
    }

    /**
     * Set superclass information.
     *
     * @param sclas superclass information
     */
    public void setSuperFile(ClassFile sclas) {
        m_superClass = sclas;
        m_isSamePackage = getPackage().equals(sclas.getPackage());
    }

    /**
     * Get superclass information.
     *
     * @return super class information as loaded (<code>null</code> if no
     * superclass - java.lang.Object, interface, or primitive)
     */
    public ClassFile getSuperFile() {
        return m_superClass;
    }

    /**
     * Get names of all interfaces implemented by class.
     *
     * @return names of all interfaces implemented directly by class
     * (non-<code>null</code>, empty array if none)
     */
    public String[] getInterfaces() {
        return m_interfaceNames;
    }

    /**
     * Add interface to class. The interface is added to the class if not
     * already defined.
     *
     * @param intf fully qualified interface name
     * @return <code>true</code> if added, <code>false</code> if already present
     */
    public boolean addInterface(String intf) {
        ClassGen gen = getClassGen();
        String[] intfs = gen.getInterfaceNames();
        for (int i = 0; i < intfs.length; i++) {
            if (intf.equals(intfs[i])) {
                return false;
            }
        }
        gen.addInterface(intf);
        m_isModified = true;
        m_instanceOfs = null;
        return true;
    }

    /**
     * Accumulate interface signatures recursively.
     *
     * @param intfs names of interfaces implemented
     * @param map map for interfaces already accumulated
     * @param accs accumulated interface names
     * @throws JiBXException if configuration error
     */
    protected void accumulateInterfaces(String[] intfs, HashMap map,
        ArrayList accs) throws JiBXException {
        for (int i = 0; i < intfs.length; i++) {
            String name = intfs[i];
            if (map.get(name) == null) {
                ClassFile cf = ClassCache.requireClassFile(name);
                String sig = cf.getSignature();
                map.put(name, sig);
                accs.add(sig);
                String[] inherits = cf.m_curClass.getInterfaceNames();
                accumulateInterfaces(inherits, map, accs);
            }
        }
    }

    /**
     * Get signatures for all types of which instances of this type are
     * instances.
     *
     * @return all signatures suppored by instances
     * @throws JiBXException if configuration error
     */
    public String[] getInstanceSigs() throws JiBXException {
        if (m_instanceOfs == null) {
            
            // check for an array class
            String name = getName();
            if (name.endsWith("[]")) {
                
                // accumulate prefix by stripping suffixes
                String prefix = "";
                do {
                    name = name.substring(0, name.length()-2);
                    prefix = prefix + '[';
                } while (name.endsWith("[]"));
                
                // check for a primitive base type on array
                String[] bsigs;
                if (ClassItem.isPrimitive(name)) {
                    bsigs = new String[1];
                    bsigs[0] = ClassItem.getPrimitiveSignature(name);
                } else {
                    ClassFile bcf = ClassCache.requireClassFile(name);
                    bsigs = bcf.getInstanceSigs();
                }
                
                // derive array signatures from signatures for base type
                String[] asigs = new String[bsigs.length+1];
                for (int i = 0; i < bsigs.length; i++) {
                    asigs[i] = prefix + bsigs[i];
                }
                asigs[bsigs.length] = "Ljava/lang/Object;";
                m_instanceOfs = asigs;
                
            } else {
                
                // walk all classes and interfaces to find signatures
                HashMap map = new HashMap();
                ArrayList iofs = new ArrayList();
                ClassFile cur = this;
                while (cur != null) {
                    String sig = cur.getSignature();
                    map.put(name, sig);
                    iofs.add(sig);
                    accumulateInterfaces(cur.getInterfaces(), map, iofs);
                    cur = cur.getSuperFile();
                }
                String[] sigs = new String[iofs.size()];
                m_instanceOfs = (String[])iofs.toArray(sigs);
                
            }
        }
        return m_instanceOfs;
    }

    /**
     * Check if class implements an interface.
     *
     * @param sig signature of interface to be checked
     * @return <code>true</code> if interface is implemented by class,
     * <code>false</code> if not
     * @throws JiBXException if configuration error
     */
    public boolean isImplements(String sig) throws JiBXException {
        String[] sigs = getInstanceSigs();
        for (int i = 0; i < sigs.length; i++) {
            if (sig.equals(sigs[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if another class is a superclass of this one.
     *
     * @param name of superclass to be checked
     * @return <code>true</code> if named class is a superclass of this one,
     * <code>false</code> if not
     */
    public boolean isSuperclass(String name) {
        ClassFile cur = this;
        while (cur != null) {
            if (cur.getName().equals(name)) {
                return true;
            } else {
                cur = cur.getSuperFile();
            }
        }
        return false;
    }
    
    /**
     * Get array of fields defined by class.
     * 
     * @return array of fields defined by class
     */
    public ClassItem[] getFieldItems() {
        if (m_curClass == null) {
            return EMPTY_CLASS_ITEMS;
        } else {
            Field[] fields = m_curClass.getFields();
            ClassItem[] items = new ClassItem[fields.length];
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                items[i] = new ClassItem(field.getName(), this, field);
            }
            return items;
        }
    }

    /**
     * Get internal information for field. This can only be used with
     * existing classes, and only checks for fields that are actually members
     * of the class (not superclasses).
     *
     * @param name field name
     * @return field information, or <code>null</code> if field not found
     */
    protected Field getDefinedField(String name) {

        // check for match to field name defined in class
        Field[] fields = m_curClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getName().equals(name)) {
                return fields[i];
            }
        }
        return null;
    }

    /**
     * Get internal information for field. This can only be used with existing
     * classes. If the field is not found directly, superclasses are checked for
     * inherited fields matching the supplied name.
     *
     * @param name field name
     * @return field information, or <code>null</code> if field not found
     */
    protected Field getAccessibleField(String name) {
        
        // always return not found for unloadable class
        if (m_curClass == null) {
            return null;
        } else {
    
            // check for match to field name defined in class
            Field field = getDefinedField(name);
            if (field == null) {
        
                // try match to field inherited from superclass
                if (m_superClass != null) {
                    field = m_superClass.getAccessibleField(name);
                    if (field != null && (!m_isSamePackage ||
                        field.isPrivate()) && !field.isPublic() &&
                        !field.isProtected()) {
                        field = null;
                    }
                }
                
            }
            return field;
        }
    }

    /**
     * Get information for field. This can only be used with existing classes,
     * and only checks for fields that are actually members of the class (not
     * superclasses).
     *
     * @param name field name
     * @return field information, or <code>null</code> if field not found
     */
    public ClassItem getDirectField(String name) {
        Field field = getAccessibleField(name);
        if (field == null) {
            return null;
        } else {
            ClassItem item = (ClassItem)m_itemMap.get(field);
            if (item == null) {
                item = new ClassItem(name, this, field);
                m_itemMap.put(field, item);
            }
            return item;
        }
    }

    /**
     * Get information for field. This can only be used with existing classes.
     * If the field is not found directly, superclasses are checked for
     * inherited fields matching the supplied name.
     *
     * @param name field name
     * @return field information
     * @throws JiBXException if field not found
     */
    public ClassItem getField(String name) throws JiBXException {
        Field field = getAccessibleField(name);
        if (field == null) {
            throw new JiBXException("Field " + name +
                " not found in class " + m_name);
        } else {
            ClassItem item = (ClassItem)m_itemMap.get(field);
            if (item == null) {
                item = new ClassItem(name, this, field);
                m_itemMap.put(field, item);
            }
            return item;
        }
    }
    
    /**
     * Get array of methods defined by class or interface. In the case of an
     * interface, this merges all methods from superinterfaces in the array
     * returned.
     * 
     * @return array of methods defined by class
     */
    private Method[] getMethods() {
        if (m_methods == null) {
            
            // start with methods defined directly
            Method[] methods = null;
            if (m_genClass != null) {
                methods = m_genClass.getMethods();
            } else {
                methods = m_curClass.getMethods();
            }
            if (m_curClass.isInterface() && m_superInterfaces.length > 0) {
                
                // for interface extending other interfaces, merge methods
                ArrayList merges = new ArrayList();
                for (int i = 0; i < methods.length; i++) {
                    merges.add(methods[i]);
                }
                for (int i = 0; i < m_superInterfaces.length; i++) {
                    methods = m_superInterfaces[i].getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        merges.add(methods[j]);
                    }
                }
                
                // set merged array
                methods = (Method[])merges.toArray(new Method[merges.size()]);
            }
            
            // cache the created method array
            m_methods = methods;
        }
        return m_methods;
    }
    
    /**
     * Get array of methods defined by class.
     * 
     * @return array of methods defined by class
     */
    public ClassItem[] getMethodItems() {
        if (m_curClass == null) {
            return EMPTY_CLASS_ITEMS;
        } else {
            Method[] methods = getMethods();
            ClassItem[] items = new ClassItem[methods.length];
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                items[i] = new ClassItem(method.getName(), this, method);
            }
            return items;
        }        
    }

    /**
     * Get internal information for method without respect to potential trailing
     * arguments or return value. This can only be used with existing classes.
     * If the method is not found directly, superclasses are checked for
     * inherited methods matching the supplied name. This compares the supplied
     * partial signature against the actual method signature, and considers it
     * a match if the actual sigature starts with the supplied signature..
     *
     * @param name method name
     * @param sig partial method signature to be matched
     * @return method information, or <code>null</code> if method not found
     */
    protected Method getAccessibleMethod(String name, String sig) {
        
        // only check loadable classes
        if (m_curClass != null) {
    
            // check for match to method defined in class
            Method[] methods = getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals(name)) {
                    if (method.getSignature().startsWith(sig)) {
                        return method;
                    }
                }
            }
    
            // try match to method inherited from superclass
            if (m_superClass != null) {
                Method method = m_superClass.getAccessibleMethod(name, sig);
                if (method != null && ((m_isSamePackage &&
                    !method.isPrivate()) || method.isPublic() ||
                    method.isProtected())) {
                    return method;
                }
            }
            
        }
        return null;
    }

    /**
     * Get information for method without respect to potential trailing
     * arguments or return value. This can only be used with existing classes.
     * If the method is not found directly, superclasses are checked for
     * inherited methods matching the supplied name. This compares the supplied
     * partial signature against the actual method signature, and considers it
     * a match if the actual sigature starts with the supplied signature..
     *
     * @param name method name
     * @param sig partial method signature to be matched
     * @return method information, or <code>null</code> if method not found
     */
    public ClassItem getMethod(String name, String sig) {
        Method method = getAccessibleMethod(name, sig);
        if (method == null) {
            return null;
        } else {
            ClassItem item = (ClassItem)m_itemMap.get(method);
            if (item == null) {
                item = new ClassItem(name, this, method);
                m_itemMap.put(method, item);
            }
            return item;
        }
    }

    /**
     * Get information for method matching one of several possible signatures.
     * This can only be used with existing classes. If a match is not found
     * directly, superclasses are checked for inherited methods matching the
     * supplied name and signatures. The signature variations are checked in
     * the order supplied.
     *
     * @param name method name
     * @param sigs possible signatures for method (including return type)
     * @return method information, or <code>null</code> if method not found
     */
    public ClassItem getMethod(String name, String[] sigs) {
        Method method = null;
        for (int i = 0; method == null && i < sigs.length; i++) {
            method = getAccessibleMethod(name, sigs[i]);
        }
        if (method == null) {
            return null;
        } else {
            ClassItem item = (ClassItem)m_itemMap.get(method);
            if (item == null) {
                item = new ClassItem(name, this, method);
                m_itemMap.put(method, item);
            }
            return item;
        }
    }
    
    /**
     * Check for match to specified access level. This treats a field or method
     * as matching if the access level is the same as or more open than the
     * required level.
     * 
     * @param item information for field or method to be checked
     * @param access required access level for match
     * @return <code>true</code> if access level match, <code>false</code> if
     * not
     */
    private static boolean matchAccess(FieldOrMethod item, int access) {
        if (item.isPublic()) {
            return true;
        } else if (item.isProtected()) {
            return access <= PROTECTED_ACCESS;
        } else if (item.isPrivate()) {
            return access == PRIVATE_ACCESS;
        } else {
            return access <= PACKAGE_ACCESS;
        }
    }
    
    /**
     * Check if one type is assignment compatible with another type. This is an
     * ugly replacement for apparently broken BCEL code.
     * 
     * @param have type being checked
     * @param need type needed
     * @return <code>true</code> if compatible, <code>false</code> if not
     */
    private static boolean isAssignmentCompatible(Type have, Type need) {
        if (have.equals(need)) {
            return true;
        } else {
            try {
                return ClassItem.isAssignable(have.toString(), need.toString());
            } catch (JiBXException e) {
                throw new IllegalStateException
                    ("Internal error: Unable to access data for " +
                    have.toString() + " or " + need.toString() + ":\n" +
                    e.getMessage());
            }
        }
    }

    /**
     * Get information for best matching method. This tries to find a method
     * which matches the specified name, return type, and argument types. If an
     * exact match is not found it looks for a method with a return type that
     * is extended or implemented by the specified type and arguments that are
     * extended or implemented by the specified types. This can only be used
     * with existing classes. If the method is not found directly, superclasses
     * are checked for inherited methods.
     *
     * @param name method name
     * @param access access level required for matching methods
     * @param ret return value type (<code>null</code> if indeterminant)
     * @param args argument value types (<code>null</code> if indeterminant)
     * @return method information, or <code>null</code> if method not found
     */
    private Method getBestAccessibleMethod(String name, int access, Type ret,
        Type[] args) {
        
        // just fail for classes that aren't loadable
        if (m_curClass == null) {
            return null;
        }

        // check for match to method defined in class
        Method[] methods = getMethods();
        Method best = null;
        int diff = Integer.MAX_VALUE;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name) && matchAccess(method, access)) {
                
                // make sure the return type is compatible
                boolean match = true;
                int ndiff = 0;
                if (ret != null) {
                    Type type = method.getReturnType();
                    match = isAssignmentCompatible(ret, type);
                }
                if (match && args != null) {
                    
                    // check closeness of argument types
                    Type[] types = method.getArgumentTypes();
                    if (args.length == types.length) {
                        for (int j = 0; j < args.length; j++) {
                            Type type = types[j];
                            Type arg = args[j];
                            if (!type.equals(arg)) {
                                ndiff++;
                                match = isAssignmentCompatible(arg, type);
                                if (!match) {
                                    break;
                                }
                            }
                        }
                    } else {
                        match = false;
                    }
                }
                if (match && ndiff < diff) {
                    best = method;
                }
            }
        }
        if (best != null) {
            return best;
        }
        
        // try methods inherited from superclass if no match found
        if (m_superClass != null) {
            if (access < PROTECTED_ACCESS) {
                if (m_isSamePackage) {
                    access = PACKAGE_ACCESS;
                } else {
                    access = PROTECTED_ACCESS;
                }
            }
            return m_superClass.getBestAccessibleMethod(name, access, ret, args);
        } else {
            return null;
        }
    }

    /**
     * Get information for best matching method. This tries to find a method
     * which matches the specified name, return type, and argument types. If an
     * exact match is not found it looks for a method with a return type that
     * is extended or implemented by the specified type and arguments that are
     * extended or implemented by the specified types. This can only be used
     * with existing classes. If the method is not found directly, superclasses
     * are checked for inherited methods.
     *
     * @param name method name
     * @param ret return value type (<code>null</code> if indeterminant)
     * @param args argument value types (<code>null</code> if indeterminant)
     * @return method information, or <code>null</code> if method not found
     */
    public ClassItem getBestMethod(String name, String ret, String[] args) {
        Type rtype = null;
        if (ret != null) {
            rtype = ClassItem.typeFromName(ret);
        }
        Type[] atypes = null;
        if (args != null) {
            atypes = new Type[args.length];
            for (int i = 0; i < args.length; i++) {
                atypes[i] = ClassItem.typeFromName(args[i]);
            }
        }
        Method method =
            getBestAccessibleMethod(name, PRIVATE_ACCESS, rtype, atypes);
        if (method == null) {
            return null;
        }
        ClassItem item = (ClassItem)m_itemMap.get(method);
        if (item == null) {
            item = new ClassItem(name, this, method);
            m_itemMap.put(method, item);
        }
        return item;
    }

    /**
     * Get information for initializer. This can only be used with existing
     * classes. Only the class itself is checked for an initializer matching
     * the argument list signature.
     *
     * @param sig encoded argument list signature
     * @return method information, or <code>null</code> if method not found
     */
    public ClassItem getInitializerMethod(String sig) {
        
        // only check if loadable class
        if (m_curClass != null) {
    
            // check for match to method defined in class
            Method[] methods = getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals("<init>")) {
                    if (method.getSignature().startsWith(sig)) {
                        ClassItem item = (ClassItem)m_itemMap.get(method);
                        if (item == null) {
                            item = new ClassItem("<init>", this, method);
                            m_itemMap.put(method, item);
                        }
                        return item;
                    }
                }
            }
            
        }
        return null;
    }

    /**
     * Get information for static method without respect to return value. This
     * can only be used with existing classes. Only the class itself is checked
     * for a method matching the supplied name and argument list signature.
     *
     * @param name method name
     * @param sig encoded argument list signature
     * @return method information, or <code>null</code> if method not found
     */
    public ClassItem getStaticMethod(String name, String sig) {
        
        // only check if loadable class
        if (m_curClass != null) {
    
            // check for match to method defined in class
            Method[] methods = getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals(name) && method.isStatic()) {
                    if (method.getSignature().startsWith(sig)) {
                        ClassItem item = (ClassItem)m_itemMap.get(method);
                        if (item == null) {
                            item = new ClassItem(name, this, method);
                            m_itemMap.put(method, item);
                        }
                        return item;
                    }
                }
            }
            
        }
        return null;
    }

    /**
     * Get all binding methods currently defined in class. Binding methods are
     * generally identified by a supplied prefix, but additional methods
     * can be specified the the combination of exact name and signature. This
     * is a little kludgy, but necessary to handle the "marshal" method added
     * to mapped classes.
     *
     * @param prefix identifying prefix for binding methods
     * @param matches pairs of method name and signature to be matched as
     * exceptions to the prefix matching
     * @return existing binding methods
     */
    public ExistingMethod[] getBindingMethods(String prefix, String[] matches) {
        
        // return empty array if newly created class or unloadable class
        if (m_curClass == null) {
            return EMPTY_METHOD_ARRAY;
        }

        // check for binding methods defined in class
        Method[] methods = getMethods();
        int count = 0;
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String name = method.getName();
            if (name.startsWith(prefix)) {
                count++;
            } else {
                String sig = method.getSignature();
                for (int j = 0; j < matches.length; j += 2) {
                    if (name.equals(matches[j]) && sig.equals(matches[j+1])) {
                        count++;
                        break;
                    }
                }
            }
        }
        
        // generate array of methods found
        if (count == 0) {
            return EMPTY_METHOD_ARRAY;
        } else {
            ExistingMethod[] exists = new ExistingMethod[count];
            int fill = 0;
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                String name = method.getName();
                boolean match = name.startsWith(prefix);
                if (!match) {
                    String sig = method.getSignature();
                    for (int j = 0; j < matches.length; j += 2) {
                        if (name.equals(matches[j]) &&
                            sig.equals(matches[j+1])) {
                            match = true;
                            break;
                        }
                    }
                }
                if (match) {
                    ClassItem item = (ClassItem)m_itemMap.get(method);
                    if (item == null) {
                        item = new ClassItem(name, this, method);
                        m_itemMap.put(method, item);
                    }
                    exists[fill++] = new ExistingMethod(method, item, this);
                }
            }
            return exists;
        }
    }

    /**
     * Check accessible method. Check if a field or method in another class is
     * accessible from within this class.
     *
     * @param item field or method information
     * @return <code>true</code> if accessible, <code>false</code> if not
     */
    public boolean isAccessible(ClassItem item) {
        if (item.getClassFile() == this) {
            return true;
        } else {
            int access = item.getAccessFlags();
            if ((access & Constants.ACC_PUBLIC) != 0) {
                return true;
            } else if ((access & Constants.ACC_PRIVATE) != 0) {
                return false;
            } else if (getPackage().equals(item.getClassFile().getPackage())) {
                return true;
            } else if ((access & Constants.ACC_PROTECTED) != 0) {
                ClassFile target = item.getClassFile();
                ClassFile ancestor = this;
                while ((ancestor = ancestor.getSuperFile()) != null) {
                    if (ancestor == target) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    /**
     * Get generator for modifying class.
     *
     * @return generator for class
     */
    private ClassGen getClassGen() {
        if (m_genClass == null) {
            if (m_isWritable) {
                m_genClass = new ClassGen(m_curClass);
                m_genPool = m_genClass.getConstantPool();
                m_instBuilder = new InstructionBuilder(m_genClass, m_genPool);
                m_isHashCurrent = false;
            } else {
                throw new IllegalStateException("Internal error - cannot modify class " + getName() + " loaded from " + m_path);
            }
        }
        return m_genClass;
    }

    /**
     * Get constant pool generator for modifying class.
     *
     * @return constant pool generator for class
     */
    public ConstantPoolGen getConstPoolGen() {
        if (m_genPool == null) {
            getClassGen();
        }
        return m_genPool;
    }

    /**
     * Get instruction builder for modifying class.
     *
     * @return instruction builder for class
     */
    public InstructionBuilder getInstructionBuilder() {
        if (m_instBuilder == null) {
            getClassGen();
        }
        return m_instBuilder;
    }

    /**
     * Add method to class.
     * 
     * @param method method to be added
     * @return added method information
     */
    public ClassItem addMethod(Method method) {
        getClassGen().addMethod(method);
        setModified();
        String mname = method.getName();
        if (m_suffixMap != null && isSuffixName(mname)) {
            m_suffixMap.put(mname, method); 
        }
        m_methods = null;
        return new ClassItem(mname, this, method);
    }

    /**
     * Remove method from class.
     * 
     * @param method method to be removed
     */
    public void removeMethod(Method method) {
        getClassGen().removeMethod(method);
        setModified();
        String mname = method.getName();
        if (m_suffixMap != null && isSuffixName(mname)) {
            m_suffixMap.remove(mname); 
        }
        m_methods = null;
    }

    /**
     * Add field to class with initial <code>String</code> value. If a field
     * with the same name already exists, it is overwritten.
     *
     * @param type fully qualified class name of field type
     * @param name field name
     * @param access access flags for field
     * @param init initial value for field
     * @return field information
     */
    public ClassItem addField(String type, String name, int access, String init) {
        if (init != null && init.length() > 0x7FFF) {
            throw new IllegalArgumentException("Internal error - string too long");
        }
        deleteField(name);
        FieldGen fgen = new FieldGen(access,
            Type.getType(Utility.getSignature(type)), name, getConstPoolGen());
        fgen.setInitValue(init);
        Field field = fgen.getField();
        getClassGen().addField(field);
        m_isModified = true;
        m_isHashCurrent = false;
        return new ClassItem(name, this, field);
    }

    /**
     * Add field to class with initial <code>int</code> value. If a field with
     * the same name already exists, it is overwritten.
     *
     * @param type fully qualified class name of field type
     * @param name field name
     * @param access access flags for field
     * @param init initial value for field
     * @return field information
     */
    public ClassItem addField(String type, String name, int access, int init) {
        deleteField(name);
        FieldGen fgen = new FieldGen(access,
            Type.getType(Utility.getSignature(type)), name, getConstPoolGen());
        fgen.setInitValue(init);
        Field field = fgen.getField();
        getClassGen().addField(field);
        m_isModified = true;
        m_isHashCurrent = false;
        return new ClassItem(name, this, field);
    }

    /**
     * Update class field with initial <code>String</code> value. If the field
     * already exists with the same characteristics it is left unchanged;
     * otherwise any existing field with the same name is overwritten.
     *
     * @param type fully qualified class name of field type
     * @param name field name
     * @param access access flags for field
     * @param init initial value for field
     * @return field information
     */
    public ClassItem updateField(String type, String name, int access,
        String init) {
        
        // make sure the string length is acceptable
        if (init.length() > 0x7FFF) {
            throw new IllegalArgumentException("Internal error - string too long");
        }
        
        // first check for match with existing field
        Field[] fields = m_curClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getName().equals(name) &&
                field.getAccessFlags() == access) {
                String sig = field.getSignature();
                if (type.equals(Utility.signatureToString(sig, false))) {
                    ConstantValue cval = field.getConstantValue();
                    if (cval != null) {
                        int index = cval.getConstantValueIndex();
                        ConstantPool cp = m_curClass.getConstantPool();
                        Constant cnst = cp.getConstant(index);
                        if (cnst instanceof ConstantString) {
                            Object value = ((ConstantString)cnst).
                                getConstantValue(cp);
                            if (init.equals(value)) {
                                return new ClassItem(name,this, field);
                            }
                        }
                    }
                }
            }
        }
        
        // no exact match, so replace any existing field with same name
        deleteField(name);
        FieldGen fgen = new FieldGen(access,
            Type.getType(Utility.getSignature(type)), name, getConstPoolGen());
        fgen.setInitValue(init);
        Field field = fgen.getField();
        getClassGen().addField(field);
        m_isModified = true;
        m_isHashCurrent = false;
        return new ClassItem(name, this, field);
    }

    /**
     * Add field to class without initialization. If a field with the same name
     * already exists, it is overwritten.
     *
     * @param type fully qualified class name of field type
     * @param name field name
     * @param access access flags for field
     * @return field information
     */
    public ClassItem addField(String type, String name, int access) {
        deleteField(name);
        FieldGen fgen = new FieldGen(access,
            Type.getType(Utility.getSignature(type)), name, getConstPoolGen());
        Field field = fgen.getField();
        getClassGen().addField(field);
        m_isModified = true;
        m_isHashCurrent = false;
        return new ClassItem(name, this, field);
    }

    /**
     * Add private field to class without initialization. If a field
     * with the same name already exists, it is overwritten.
     *
     * @param type fully qualified class name of field type
     * @param name field name
     * @return field information
     */
    public ClassItem addPrivateField(String type, String name) {
        return addField(type, name, PRIVATEFIELD_ACCESS);
    }

    /**
     * Add default constructor to a class. The added default constructor just
     * calls the default constructor for the superclass. If the superclass
     * doesn't have a default constructor, this method is called recursively to
     * add one if possible.
     *
     * @return constructor information
     */
    public ClassItem addDefaultConstructor() {
        if (m_defaultConstructor == null) {
            
            // check for default constructor in superclass
            ClassItem cons = m_superClass.getInitializerMethod("()V");
            if (cons == null) {
                cons = m_superClass.addDefaultConstructor();
            }
            cons.makeAccessible(this);
            
            // add the public constructor method
            ExceptionMethodBuilder mb = new ExceptionMethodBuilder("<init>",
                Type.VOID, new Type[0], this, Constants.ACC_PUBLIC);
        
            // call the superclass constructor
            mb.appendLoadLocal(0);
            mb.appendCallInit(m_superClass.getName(), "()V");
            
            // finish with return
            mb.appendReturn();
            mb.codeComplete(false);
            m_defaultConstructor = mb.addMethod();
            
        }
        return m_defaultConstructor;
    }

    /**
     * Check if a method name matches the pattern for a generated unique suffix.
     *
     * @param name method name to be checked
     * @return <code>true</code> if name matches suffix pattern,
     * <code>false</code> if not
     */
    private static boolean isSuffixName(String name) {
        int last = name.length() - 1;
        for (int i = last; i > 0; i--) {
            char chr = name.charAt(i);
            if (chr == '_') {
                return i < last;
            } else if (!Character.isDigit(chr)) {
                break;
            }
        }
        return false;
    }

    /**
     * Make method name unique with generated suffix. The suffixed method name
     * is tracked so that it will not be used again.
     *
     * @param name base name before suffix is appended
     * @return name with unique suffix appended
     */
    public String makeUniqueMethodName(String name) {
        
        // check if map creation is needed
        if (m_suffixMap == null) {
            m_suffixMap = new HashMap();
            if (m_curClass != null) {
                Method[] methods = getMethods();
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    String mname = method.getName();
                    if (isSuffixName(mname)) {
                        m_suffixMap.put(mname, method);
                    }
                }
            }
        }
        
        // check if inheritance depth is needed
        if (m_inheritDepth == 0) {
            ClassFile cf = this;
            while ((cf = cf.getSuperFile()) != null) {
                m_inheritDepth++;
            }
        }
        
        // generate suffix to make name unique, trying low values first
        while (true) {
            String uname = name + '_' + m_inheritDepth + '_' + m_uniqueIndex;
            if (m_suffixMap.get(uname) == null) {
                return uname;
            } else {
                m_uniqueIndex++;
            }
        }
    }

    /**
     * Delete field from class.
     *
     * @param name field name
     * @return <code>true</code> if field was present, <code>false</code> if not
     */
    public boolean deleteField(String name) {
        ClassGen cg = getClassGen();
        Field field = cg.containsField(name);
        if (field == null) {
            return false;
        } else {
            cg.removeField(field);
            m_isModified = true;
            m_isHashCurrent = false;
            return true;
        }
    }

    /**
     * Delete method from class.
     *
     * @param name method name
     * @param sig method signature
     * @return <code>true</code> if method was present, <code>false</code> if not
     */
    public boolean deleteMethod(String name, String sig) {
        ClassGen cg = getClassGen();
        Method method = cg.containsMethod(name, sig);
        if (method == null) {
            return false;
        } else {
            cg.removeMethod(method);
            m_isModified = true;
            m_isHashCurrent = false;
            return true;
        }
    }

    /**
     * Get use count for class.
     *
     * @return use count for this class
     */
    public int getUseCount() {
        return m_useCount;
    }

    /**
     * Increment use count for class.
     *
     * @return use count (after increment)
     */
    public int incrementUseCount() {
        return ++m_useCount;
    }
    
    /**
     * Force class to unmodifiable state. This prevents any additions, changes,
     * deletions to the file.
     */
    public void setUnmodifiable() {
        if (m_isModified) {
            throw new IllegalStateException("Internal error - attempt to preserve already modified file");
        } else {
            m_isWritable = false;
        }
    }

    /**
     * Check if class has been modified.
     *
     * @return <code>true</code> if class is modified, <code>false</code> if not
     */
    public boolean isModified() {
        return m_isModified;
    }

    /**
     * Set class modified flag.
     */
    public void setModified() {
        if (!m_isModified) {
            if (m_isWritable) {
                m_isModified = true;
                MungedClass.addModifiedClass(this);
            } else {
                throw new IllegalStateException("Internal error - attempt to modify unmodifiable file");
            }
        }
    }

    /**
     * Check if class is in complete state.
     *
     * @return <code>true</code> if class is complete, <code>false</code> if not
     */
    public boolean isComplete() {
        return m_genClass == null;
    }

    /**
     * Computes a hash code based on characteristics of the class. The 
     * characteristics used in computing the hash code include the base class,
     * implemented interfaces, method names, field names, and package, but not
     * the actual class name or signature. The current static version of the
     * class is used for this computation, so if the class is being modified
     * {@link #codeComplete} should be called before this method. Note that this
     * is designed for use with the classes generated by JiBX, and is not
     * necessarily a good model for general usage.
     * 
     * @return computed hash code value
     */
    protected int computeHashCode() {
        
        // start with basic characteristics of class
        int hash = getPackage().hashCode();
        ClassFile sfile = getSuperFile();
        if (sfile != null) {
            hash += sfile.getName().hashCode();
        }
        String[] intfs = getInterfaces();
        for (int i = 0; i < intfs.length; i++) {
            hash += intfs[i].hashCode();
        }
        hash += m_curClass.getAccessFlags();
        
        // include field and method names
        Field[] fields = m_curClass.getFields();
        for (int i = 0; i < fields.length; i++) {
            hash = hash * 49 + fields[i].getName().hashCode();
        }
        Method[] methods = m_curClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            hash = hash * 49 + methods[i].getName().hashCode();
        }
        
        // finish with constant table simple values (except name and signature)
        Constant[] cnsts = m_curClass.getConstantPool().getConstantPool();
        for (int i = m_curClass.getClassNameIndex()+1; i < cnsts.length; i++) {
            Constant cnst = cnsts[i];
            if (cnst != null) {
                int value = 0;
                switch (cnst.getTag()) {
                    case Constants.CONSTANT_Double:
                        value = (int)Double.doubleToRawLongBits
                            (((ConstantDouble)cnst).getBytes());
                        break;
                    case Constants.CONSTANT_Float:
                        value = Float.floatToRawIntBits
                            (((ConstantFloat)cnst).getBytes());
                        break;
                    case Constants.CONSTANT_Integer:
                        value = ((ConstantInteger)cnst).getBytes();
                        break;
                    case Constants.CONSTANT_Long:
                        value = (int)((ConstantLong)cnst).getBytes();
                        break;
                    case Constants.CONSTANT_Utf8:
                        String text = ((ConstantUtf8)cnst).getBytes();
                        if (!text.equals(m_signature)) {
                            value = text.hashCode();
                        }
                        break;
                    default:
                        break;
                }
                hash = hash * 49 + value;
            }
        }
//      System.out.println("Hashed " + m_name + " to value " + hash);
        return hash;
    }

    /**
     * Finalize current modified state of class. This converts the modified
     * class state into a static form, then computes a hash code based on 
     * characteristics of the class. If the class has not been modified it
     * just computes the hash code. Note that this won't initialize the array
     * of superinterfaces if used with an interface, but that shouldn't be a
     * problem since we don't create any interfaces.
     */
    public void codeComplete() {
        if (m_genClass != null) {
            m_curClass = m_genClass.getJavaClass();
            m_interfaceNames = m_curClass.getInterfaceNames();
            if (m_interfaceNames == null) {
                m_interfaceNames = new String[0];
            }
            m_superInterfaces = new ClassFile[0];
            m_genClass = null;
        }
    }

    /**
     * Get hash code. This is based on most characteristics of the class,
     * including the actual methods, but excluding the class name. It is only
     * valid after the {@link #codeComplete} method is called.
     * 
     * @return hash code based on code sequence
     */
    public int hashCode() {
        if (!m_isHashCurrent) {
            if (m_genClass != null) {
                throw new IllegalStateException
                    ("Class still being constructed");
            }
            m_hashCode = computeHashCode();
            m_isHashCurrent = true;
        }
        return m_hashCode;
    }

    /**
     * Compare two field or method items to see if they're equal. This handles
     * only the comparisons that apply to both fields and methods. It does not
     * include comparing access flags, since these may be different due to
     * access requirements.
     * 
     * @param a first field or method item
     * @param b second field or method item
     * @return <code>true</code> if the equal, <code>false</code> if not
     */
    public static boolean equalFieldOrMethods(FieldOrMethod a,
        FieldOrMethod b) {
        return a.getName().equals(b.getName()) &&
            a.getSignature().equals(b.getSignature());
    }

    /**
     * Compare two methods to see if they're equal. This checks only the details
     * of the exception handling and actual code, not the name or signature.
     * 
     * @param a first method
     * @param b second method
     * @return <code>true</code> if the equal, <code>false</code> if not
     */
    public static boolean equalMethods(Method a, Method b) {
            
        // check the exceptions thrown by the method
        ExceptionTable etaba = a.getExceptionTable();
        ExceptionTable etabb = b.getExceptionTable();
        if (etaba != null && etabb != null) {
            String[] aexcepts = etaba.getExceptionNames();
            String[] bexcepts = etabb.getExceptionNames();
            if (!Arrays.equals(aexcepts, bexcepts)) {
                return false;
            }
        } else if (etaba != null || etabb != null) {
            return false;
        }
        
        // compare the exception handling details
        Code acode = a.getCode();
        Code bcode = b.getCode();
        CodeException[] acexs = acode.getExceptionTable();
        CodeException[] bcexs = bcode.getExceptionTable();
        if (acexs.length == bcexs.length) {
            for (int i = 0; i < acexs.length; i++) {
                CodeException acex = acexs[i];
                CodeException bcex = bcexs[i];
                if (acex.getCatchType() != bcex.getCatchType() ||
                    acex.getStartPC() != bcex.getStartPC() ||
                    acex.getEndPC() != bcex.getEndPC() ||
                    acex.getHandlerPC() != bcex.getHandlerPC()) {
                    return false;
                }
            }
        }
        
        // finally compare the actual byte codes
        return Arrays.equals(acode.getCode(), bcode.getCode());
    }

    /**
     * Check if objects are equal. Compares first based on hash code, then on
     * the actual contents of the class, including package, implemented
     * interfaces, superclass, methods, and fields (but not the actual class
     * name). It is only valid after the {@link #codeComplete} method is called.
     * 
     * @param obj 
     * @return <code>true</code> if equal objects, <code>false</code> if not
     */
    public boolean equals(Object obj) {
        if (obj instanceof ClassFile && obj.hashCode() == hashCode()) {
            
            // check basic details of the classes
            ClassFile comp = (ClassFile)obj;
            if (!org.jibx.runtime.Utility.isEqual(getPackage(),
                comp.getPackage()) || getSuperFile() != comp.getSuperFile() ||
                !Arrays.equals(getInterfaces(), comp.getInterfaces())) {
                return false;
            }
            JavaClass tjc = m_curClass;
            JavaClass cjc = comp.m_curClass;
            if (tjc.getAccessFlags() != cjc.getAccessFlags()) {
                return false;
            }
            
            // compare the defined fields
            Field[] tfields = tjc.getFields();
            Field[] cfields = cjc.getFields();
            if (tfields.length != cfields.length) {
                return false;
            }
            for (int i = 0; i < tfields.length; i++) {
                if (!equalFieldOrMethods(tfields[i], cfields[i])) {
                    return false;
                }
            }
            
            // compare the defined methods
            Method[] tmethods = tjc.getMethods();
            Method[] cmethods = cjc.getMethods();
            if (tmethods.length != cmethods.length) {
                return false;
            }
            for (int i = 0; i < tmethods.length; i++) {
                Method tmethod = tmethods[i];
                Method cmethod = cmethods[i];
                if (!equalFieldOrMethods(tmethod, cmethod) ||
                    !equalMethods(tmethod, cmethod)) {
                    return false;
                }
            }
        
            // finish with constant table values (correcting name and signature)
            Constant[] tcnsts = tjc.getConstantPool().getConstantPool();
            Constant[] ccnsts = cjc.getConstantPool().getConstantPool();
            if (tcnsts.length != ccnsts.length) {
                return false;
            }
            for (int i = tjc.getClassNameIndex()+1; i < tcnsts.length; i++) {
                Constant tcnst = tcnsts[i];
                Constant ccnst = ccnsts[i];
                if (tcnst != null && ccnst != null) {
                    int tag = tcnst.getTag();
                    if (tag != ccnst.getTag()) {
                        return false;
                    }
                    boolean equal = true;
                    switch (tag) {
                        case Constants.CONSTANT_Double:
                            equal = ((ConstantDouble)tcnst).getBytes() ==
                                ((ConstantDouble)ccnst).getBytes();
                            break;
                        case Constants.CONSTANT_Float:
                            equal = ((ConstantFloat)tcnst).getBytes() ==
                                ((ConstantFloat)ccnst).getBytes();
                            break;
                        case Constants.CONSTANT_Integer:
                            equal = ((ConstantInteger)tcnst).getBytes() ==
                                ((ConstantInteger)ccnst).getBytes();
                            break;
                        case Constants.CONSTANT_Long:
                            equal = ((ConstantLong)tcnst).getBytes() ==
                                ((ConstantLong)ccnst).getBytes();
                            break;
                        case Constants.CONSTANT_Utf8:
                            String ttext = ((ConstantUtf8)tcnst).getBytes();
                            String ctext = ((ConstantUtf8)ccnst).getBytes();
                            if (ttext.equals(m_signature)) {
                                equal = ctext.equals(comp.m_signature);
                            } else {
                                equal = ttext.equals(ctext);
                            }
                            break;
                        default:
                            break;
                    }
                    if (!equal) {
                        return false;
                    }
                } else if (tcnst != null || ccnst != null) {
                    return false;
                }
            }
            
            // if nothing failed, the classes are equal
            return true;
            
        } else {
            return false;
        }
    }

    /**
     * Delete class file information. Deletes the class file for this class,
     * if it exists. Does nothing if no class file has been generated.
     */
    public void delete() {
        if (m_file.exists()) {
            m_file.delete();
        }
    }

    /**
     * Write out modified class information. Writes the modified class file to
     * an output stream.
     *
     * @param os output stream for writing modified class
     * @throws IOException if error writing to file
     */
    public void writeFile(OutputStream os) throws IOException {
        codeComplete();
        m_curClass.dump(new BufferedOutputStream(os));
        os.close();
    }

    /**
     * Write out modified class information. Writes the modified class file
     * back out to the original file. If the class file has not been modified,
     * the original file is kept unchanged.
     *
     * @throws IOException if error writing to file
     */
    public void writeFile() throws IOException {
        if (m_isModified) {
            OutputStream os = new FileOutputStream(m_file);
            writeFile(os);
        }
    }

    /**
     * Derive generated class name. This generates a JiBX class name from the
     * name of this class, using the supplied prefix and suffix information. The
     * derived class name is always in the same package as this class.
     *
     * @param prefix generated class name prefix
     * @param suffix generated class name suffix
     * @return derived class name
     */
    public String deriveClassName(String prefix, String suffix) {
        String pack = "";
        String tname = m_name;
        int split = tname.lastIndexOf('.');
        if (split >= 0) {
            pack = tname.substring(0, split+1);
            tname = tname.substring(split+1); 
        }
        return pack + prefix + tname + suffix;
    }
    
    /**
     * Set class paths to be searched.
     *
     * @param paths ordered set of paths to be searched for class files
     */
    public static void setPaths(String[] paths) {
        
        // create full path string with separators for BCEL loader
        StringBuffer full = new StringBuffer();
        for (int i = 0; i < paths.length; i++) {
            if (i > 0) {
                full.append(File.pathSeparatorChar);
            }
            full.append(paths[i]);
        }
        s_loader = new ClassPath(full.toString());
        
        // create direct classloader for access to classes during binding
        URL[] urls = new URL[paths.length];
        try {
            
            // generate array of component file or directory URLs
            for (int i = 0; i < urls.length; i++) {
                urls[i] = new File(paths[i]).toURI().toURL();
            }
            
            // initialize classloader with full array of path URLs
            s_directLoader = new URLClassLoader(urls,
                ClassFile.class.getClassLoader());
            
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException
                ("Error initializing classloading: " + ex.getMessage());
        }
    }

    /**
     * Try loading class from classpath.
     *
     * @param name fully qualified name of class to be loaded
     * @return loaded class, or <code>null</code> if not found
     */
    public static Class loadClass(String name) {
        try {
            return s_directLoader.loadClass(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
    /**
     * Get the classloader used for classes referenced in the binding.
     *
     * @return classloader
     */
    public static ClassLoader getClassLoader() {
        return s_directLoader;
    }
}
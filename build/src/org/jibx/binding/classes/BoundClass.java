/*
 * Copyright (c) 2003-2009, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of JiBX nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.binding.classes;

import java.io.File;
import java.util.HashMap;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.generic.Type;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.JiBXException;

/**
 * Bound class handler. Each instance controls and organizes information for a
 * class included in one or more binding definitions.
 * 
 * @author Dennis M. Sosnoski
 */
public class BoundClass
{
    //
    // Constants and such related to code generation.

    /** Prefix used for access methods. */
    private static final String ACCESS_PREFIX = BindingDirectory.GENERATE_PREFIX
        + "access_";

    /** Empty argument type array. */
    private static final Type[] EMPTY_TYPE_ARGS = {};

    //
    // Static data.

    /**
     * Map from bound class name (or bound and munged combination) to binding
     * information.
     */
    private static HashMap s_nameMap;

    /** Package of first modifiable class. */
    private static String s_modifyPackage;

    /** Root for package of first modifiable class. */
    private static File s_modifyRoot;
    
    /** Name to be used for code generation proxy class. */
    private static String s_mungeName = BindingDirectory.GENERATE_PREFIX +
        "MungeAdapter";

    /** Class used for code generation proxy with unmodifiable classes (lazy
     create, <code>null</code> if not yet needed). */
    private static MungedClass s_genericMunge;

    //
    // Actual instance data.

    /** Bound class file information. */
    private final ClassFile m_boundClass;

    /** Munged version of target class (<code>null</code> if target class
     * completely unmodifiable). */
    private final MungedClass m_directMungedClass;

    /** Class receiving code generated for target class (may be same as {@link
     * #m_directMungedClass}. */
    private final MungedClass m_mungedClass;

    /**
     * Map from field or method to load access method (lazy create,
     * <code>null</code> if not used).
     */
    private HashMap m_loadMap;

    /**
     * Map from field or method to store access method (lazy create,
     * <code>null</code> if not used).
     */
    private HashMap m_storeMap;

    /**
     * Constructor.
     * 
     * @param bound target class file information
     * @param dmunge information for bound class (<code>null</code> if not
     * modifiable)
     * @param munge class file for class hosting generated code (may be same as
     * {@link dmunge}
     */
    private BoundClass(ClassFile bound, MungedClass dmunge, MungedClass munge) {
        m_boundClass = bound;
        m_directMungedClass = dmunge;
        m_mungedClass = munge;
    }

    /**
     * Get bound class file information.
     * 
     * @return class file information for bound class
     */
    public ClassFile getClassFile() {
        return m_boundClass;
    }

    /**
     * Get bound class file name.
     * 
     * @return name of bound class
     */
    public String getClassName() {
        return m_boundClass.getName();
    }

    /**
     * Get direct munged class file information, if available.
     * 
     * @return class file information for class receiving binding code
     */
    public ClassFile getDirectMungedFile() {
    	if (m_directMungedClass == null) {
    		throw new IllegalStateException("Internal error - no direct access to class " + m_boundClass.getName());
    	}
    	return m_directMungedClass.getClassFile();
    }

    /**
     * Get munged class file information.
     * 
     * @return class file information for class receiving binding code
     */
    public ClassFile getMungedFile() {
        return m_mungedClass.getClassFile();
    }

    /**
     * Check if class being changed directly.
     * 
     * @return <code>true</code> if bound class is being modified,
     * <code>false</code> if using a surrogate
     */
    public boolean isDirectAccess() {
        return m_boundClass == m_mungedClass.getClassFile();
    }
    
    /**
     * Check if class being changed directly with restrictions. This is used for
     * the special case of modifiable Java 7 class files, which still require a
     * separate class for most of the code generation but are modified directly
     * with binding factory information and basic implementations of the binding
     * interface methods.
     * 
     * @return <code>true</code> if bound class can be modified directly with
     * basic binding information, <code>false</code> if using a surrogate
     */
    public boolean isLimitedDirectAccess() {
    	return isDirectAccess() || m_boundClass.isModifiable();
    }

    /**
     * Get load access method for member of this class. If the access method
     * does not already exist it's created by this call. If the access method
     * does exist but without access from the context class, the access
     * permission on the method is broadened (from package to protected or
     * public, or from protected to public).
     * 
     * @param item field or method to be accessed
     * @param from context class from which access is required
     * @return the item itself if it's accessible from the required context, an
     * access method that is accessible if the item is not itself
     * @throws JiBXException on configuration error
     */
    public ClassItem getLoadMethod(ClassItem item, ClassFile from)
        throws JiBXException {

        // initialize tracking information for access methods if first time
        if (m_loadMap == null) {
            m_loadMap = new HashMap();
        }

        // check if a new access method needed
        BindingMethod method = (BindingMethod)m_loadMap.get(item);
        if (method == null) {

            // set up for constructing new method
            String name = ACCESS_PREFIX + "load_" + item.getName();
            ClassFile cf = item.getClassFile();
            Type type = Type.getType(Utility.getSignature(item.getTypeName()));
            MethodBuilder mb = new ExceptionMethodBuilder(name, type,
                EMPTY_TYPE_ARGS, cf, Constants.ACC_PUBLIC);

            // add the actual access method code
            mb.appendLoadLocal(0);
            if (item.isMethod()) {
                mb.addMethodExceptions(item);
                mb.appendCall(item);
            } else {
                mb.appendGetField(item);
            }
            mb.appendReturn(type);

            // track unique instance of this method
            method = m_directMungedClass.getUniqueMethod(mb, true);
            m_loadMap.put(item, method);
        }

        // make sure method is accessible
        method.makeAccessible(from);
        return method.getItem();
    }

    /**
     * Get store access method for member of this class. If the access method
     * does not already exist it's created by this call. If the access method
     * does exist but without access from the context class, the access
     * permission on the method is broadened (from package to protected or
     * public, or from protected to public).
     * 
     * @param item field or method to be accessed
     * @param from context class from which access is required
     * @return the item itself if it's accessible from the required context, an
     * access method that is accessible if the item is not itself
     * @throws JiBXException on configuration error
     */
    public ClassItem getStoreMethod(ClassItem item, ClassFile from)
        throws JiBXException {

        // initialize tracking information for access methods if first time
        if (m_storeMap == null) {
            m_storeMap = new HashMap();
        }

        // check if a new access method needed
        BindingMethod method = (BindingMethod)m_storeMap.get(item);
        if (method == null) {

            // set up for constructing new method
            String name = ACCESS_PREFIX + "store_" + item.getName();
            ClassFile cf = item.getClassFile();
            Type type;
            if (item.isMethod()) {
                String sig = item.getSignature();
                int start = sig.indexOf('(');
                int end = sig.indexOf(')');
                type = Type.getType(sig.substring(start + 1, end));
            } else {
                type = Type.getType(Utility.getSignature(item.getTypeName()));
            }
            MethodBuilder mb = new ExceptionMethodBuilder(name, Type.VOID,
                new Type[] { type }, cf, Constants.ACC_PUBLIC);

            // add the actual access method code
            mb.appendLoadLocal(0);
            mb.appendLoadLocal(1);
            if (item.isMethod()) {
                mb.addMethodExceptions(item);
                mb.appendCall(item);
            } else {
                mb.appendPutField(item);
            }
            mb.appendReturn();

            // track unique instance of this method
            method = m_directMungedClass.getUniqueMethod(mb, true);
            m_storeMap.put(item, method);
        }

        // make sure method is accessible
        method.makeAccessible(from);
        return method.getItem();
    }

    /**
     * Get unique method. Just delegates to the modified class handling, with
     * unique suffix appended to method name.
     * 
     * @param builder method to be defined
     * @return defined method item
     * @throws JiBXException on configuration error
     */
    public BindingMethod getUniqueMethod(MethodBuilder builder)
        throws JiBXException {
        return m_mungedClass.getUniqueMethod(builder, true);
    }

    /**
     * Get unique method. Just delegates to the modified class handling. The
     * supplied name is used without change.
     * 
     * @param builder method to be defined
     * @return defined method item
     * @throws JiBXException on configuration error
     */
    public BindingMethod getUniqueNamed(MethodBuilder builder)
        throws JiBXException {
    	if (builder.getClassFile() == m_boundClass) {
    		return m_directMungedClass.getUniqueMethod(builder, false);
    	} else {
    		return m_mungedClass.getUniqueMethod(builder, false);
    	}
    }

    /**
     * Add binding factory to class. Makes sure that there's no surrogate class
     * for code generation, then delegates to the modified class handling.
     * 
     * @param fact binding factory name
     */
    public void addFactory(String fact) {
        if (isLimitedDirectAccess()) {
        	m_directMungedClass.addFactory(fact);
        } else {
            throw new IllegalStateException(
                "Internal error: not directly modifiable class");
        }
    }

    /**
     * Create binding information for class. This creates the combination of
     * bound class and (if different) munged class and adds it to the internal
     * tables.
     * 
     * @param key text identifier for this bound class and munged class
     * combination
     * @param bound class information for bound class
     * @param dmunge information for bound class (<code>null</code> if not
     * modifiable)
     * @param munge information for class receiving generated code (may be the
     * same as {@link dmunge}
     * @return binding information for class
     */
    private static BoundClass createInstance(String key, ClassFile bound,
        MungedClass dmunge, MungedClass munge) {
        BoundClass inst = new BoundClass(bound, dmunge, munge);
        s_nameMap.put(key, inst);
        return inst;
    }

    /**
     * Find or create binding information for class. If the combination of bound
     * class and munged class already exists it's returned directly, otherwise
     * it's created and returned.
     * 
     * @param bound class information for bound class
     * @param munge information for surrogate class receiving generated code
     * @return binding information for class
     */
    private static BoundClass findOrCreateInstance(ClassFile bound,
        MungedClass munge) {
        String key = bound.getName() + ':' + munge.getClassFile().getName();
        BoundClass inst = (BoundClass)s_nameMap.get(key);
        if (inst == null) {
            inst = createInstance(key, bound, null, munge);
        }
        return inst;
    }

    /**
     * Get binding information for class. This finds the class in which code
     * generation for the target class takes place. Normally this class will be
     * the target class itself, but in cases where the target class is not
     * modifiable an alternate class will be used. This can take two forms. If
     * the context class is provided and it is a subclass of the target class,
     * code for the target class is instead added to the context class. If there
     * is no context class, or if the context class is not a subclass of the
     * target class, a unique catch-all class is used.
     * 
     * @param cf bound class information
     * @param context context class for code generation, or <code>null</code>
     * if no context
     * @return binding information for class
     * @throws JiBXException on configuration error
     */
    public static BoundClass getInstance(ClassFile cf, BoundClass context)
        throws JiBXException {

        // check if new instance needed for this class
        BoundClass inst = (BoundClass)s_nameMap.get(cf.getName());
        if (inst == null) {

            // load the basic class information and check for extendable
            if (!cf.isInterface() && cf.isModifiable()) {
            	if (cf.isExtendable()) {
                	
	                // return configuration for all modification direct to class
	                MungedClass munge = MungedClass.getInstance(cf);
					inst = createInstance(cf.getName(), cf, munge, munge);
                	
                } else {
                	
	                // return configuration with separate munge file
	                MungedClass dmunge = MungedClass.getInstance(cf);
					inst = createInstance(cf.getName(), cf, dmunge,
					    getGenericMunge());
                	
                }
            	
            	// set information used for munge class if not already set
            	if (s_modifyRoot == null) {
            		s_modifyRoot = cf.getRoot();
            	}
            	if (s_modifyPackage == null) {
            		s_modifyPackage = cf.getPackage();
            		if (s_modifyPackage != null && s_modifyPackage.length() == 0) {
            			s_modifyPackage = null;
            		}
            	}

            } else {

                // see if the context class is a subclass
                if (context != null
                    && context.getClassFile().isSuperclass(cf.getName())) {

                    // find or create munge with subclass as surrogate
                    inst = findOrCreateInstance(cf, context.m_mungedClass);

                } else {

                    // use catch-all munge class as surrogate for all else
                    inst = findOrCreateInstance(cf, getGenericMunge());

                }
            }
        }
        return inst;
    }

	/**
	 * Get the generic munge class. If one does not already exist, this will
	 * create it.
	 *
	 * @return munge
	 * @throws JiBXException
	 */
	private static MungedClass getGenericMunge() throws JiBXException {
		if (s_genericMunge == null) {
		    String mname;
		    if (s_modifyPackage == null) {
		        mname = s_mungeName;
		        MungedClass.checkDirectory(s_modifyRoot, "");
		    } else {
		        mname = s_modifyPackage + '.' + s_mungeName;
		        MungedClass.checkDirectory(s_modifyRoot,
		            s_modifyPackage);
		    }
		    ClassFile base = ClassCache
		        .requireClassFile("java.lang.Object");
		    int acc = Constants.ACC_PUBLIC | Constants.ACC_ABSTRACT;
		    ClassFile gen = new ClassFile(mname, s_modifyRoot,
		        base, acc, new String[0]);
		    gen.addDefaultConstructor();
		    s_genericMunge = MungedClass.getInstance(gen);
		    MungedClass.delayedAddUnique(gen);
		}
		return s_genericMunge;
	}

    /**
     * Get binding information for class. This version takes a fully-qualified
     * class name, calling the paired method if necessary to create a new
     * instance.
     * 
     * @param name fully qualified name of bound class
     * @param context context class for code generation, or <code>null</code>
     * if no context
     * @return binding information for class
     * @throws JiBXException on configuration error
     */
    public static BoundClass getInstance(String name, BoundClass context)
        throws JiBXException {

        // check if new instance needed for this class
        BoundClass inst = (BoundClass)s_nameMap.get(name);
        if (inst == null) {
            ClassFile cf = ClassCache.requireClassFile(name);
            return getInstance(cf, context);
        }
        return inst;
    }

    /**
     * Discard cached information and reset in preparation for a new binding
     * run.
     */
    public static void reset() {
        s_nameMap = new HashMap();
        s_modifyPackage = null;
        s_modifyRoot = null;
        s_genericMunge = null;
        s_mungeName = null;
    }

    /**
     * Set override modification information. This allows the binding to
     * control directly the root directory and package for added classes, and
     * also to set the binding name used as a modifier on the generic munge
     * adapter class. It may be called multiple times with <code>null</code>
     * values for unknown parameters that may later be overridden.
     * 
     * @param root classpath root directory for added classes
     * @param pkg package for added classes
     * @param name binding name
     */
    public static void setModify(File root, String pkg, String name) {
    	if (s_modifyRoot == null) {
    		s_modifyRoot = root;
    	}
    	if (s_modifyPackage == null && pkg != null && pkg.length() > 0) {
    		s_modifyPackage = pkg;
    	}
    	if (s_mungeName == null) {
    		s_mungeName = BindingDirectory.GENERATE_PREFIX + name + "MungeAdapter";
    	}
    }

    /**
     * Derive generated class name for bound class. This generates a JiBX class
     * name from the name of this class, using the supplied prefix and suffix
     * information. The derived class name is always in the same package as the
     * munged class for this class.
     * 
     * @param prefix generated class name prefix
     * @param suffix generated class name suffix
     * @return derived class name
     */
    public String deriveClassName(String prefix, String suffix) {
        String pack = m_mungedClass.getClassFile().getPackage();
        if (pack.length() > 0) {
            pack += '.';
        }
        String tname = m_boundClass.getName();
        int split = tname.lastIndexOf('.');
        if (split >= 0) {
            tname = tname.substring(split + 1);
        }
        return pack + prefix + tname + suffix;
    }
}
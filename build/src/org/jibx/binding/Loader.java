/*
Copyright (c) 2003-2010, Dennis M. Sosnoski.
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

package org.jibx.binding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.MungedClass;
import org.jibx.binding.def.BindingDefinition;
import org.jibx.runtime.JiBXException;
import org.jibx.util.ClasspathUrlExtender;

/**
 * Binding classloader. This is intended to substitute for the System
 * classloader (i.e., the one used for loading user classes). It first processes
 * one or more binding definitions, caching the binary classes modified by the
 * bindings. It then uses these modified forms of the classes when they're
 * requested for loading.
 *
 * @author Dennis M. Sosnoski
 */
public class Loader extends URLClassLoader
{
    /** Binding definitions used by loader. */
    private ArrayList m_bindings;
    
    /** Flag for bindings compiled into class code. */
    private boolean m_isBound;
    
    /** Map of classes modified by binding. */
    private HashMap m_classMap;
    
    /**
     * Constructor with classpath URLs and parent classloader supplied. Sets up
     * the paths for both actual classloading and finding classes to be bound.
     *
     * @param paths array of classpath URLs
     * @param parent classloader used for delegation loading
     */
    public Loader(URL[] paths, ClassLoader parent) {
        
        // configure the base class
        super(paths, parent);
        m_bindings = new ArrayList();
        m_classMap = new HashMap();
        
        // find all the file URLs in path
        ArrayList fpaths = new ArrayList(paths.length);
        for (int i = 0; i < paths.length; i++) {
            URL path = paths[i];
            if ("file".equals(path.getProtocol())) {
                fpaths.add(path.getPath());
            }
        }
        
        // set paths to be used for loading referenced classes
        String[] dirs = (String[])fpaths.toArray(new String[0]);
        ClassCache.setPaths(dirs);
        ClassFile.setPaths(dirs);
        ClasspathUrlExtender.setClassLoader(ClassFile.getClassLoader());
            
        // reset static information accumulation for binding
        BoundClass.reset();
        MungedClass.reset();
        BindingDefinition.reset();
    }
    
    /**
     * Constructor with classpath URLs supplied. This uses the supplied
     * classpaths, delegating directly to the parent classloader of the normal
     * System classloader.
     *
     * @param paths array of classpath URLs
     */
    public Loader(URL[] paths) {
        this(paths, ClassLoader.getSystemClassLoader().getParent());
    }
    
    /**
     * Default constructor. This reads the standard class path and uses it for
     * locating classes used by the binding, delegating directly to the parent
     * classloader of the normal System classloader.
     * 
     * @exception MalformedURLException on error in classpath URLs
     */
    public Loader() throws MalformedURLException {
        this(getClassPaths());
    }
    
    /**
     * Reset loader information. This discards all prior bindings and clears the
     * internal state in preparation for loading a different set of bindings. It
     * is not possible to clear the loaded classes, though, so any new bindings
     * must refer to different classes from those previously loaded.
     */
    public void reset() {
        m_bindings.clear();
        m_classMap.clear();
        m_isBound = false;
        BoundClass.reset();
        MungedClass.reset();
        BindingDefinition.reset();
    }
    
    /**
     * Method builds an array of URL for items in the class path.
     *
     * @return array of classpath URLs
     * @throws MalformedURLException 
     */
    public static URL[] getClassPaths() throws MalformedURLException {
        String[] paths = Utility.getClassPaths();
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < urls.length; i++) {
            urls[i] = new File(paths[i]).toURI().toURL();
        }
        return urls;
    }
    
    /**
     * Load binding definition. This may be called multiple times to load
     * multiple bindings, but only prior to the bindings being compiled. The
     * reader form of the call is generally preferred, since the document
     * encoding may not be properly interpreted from a stream.
     *
     * @param fname binding definition full name
     * @param sname short form of name to use as the default name of the binding
     * @param is input stream for binding definition document
     * @param url URL for binding definition (<code>null</code> if not
     * available)
     * @exception IllegalStateException if called after bindings have been
     * compiled
     * @exception IOException if error reading the binding
     * @exception JiBXException if error in processing the binding definition
     */
    public void loadBinding(String fname, String sname, InputStream is, URL url)
        throws JiBXException, IOException {
        
        // error if called after bindings have been compiled
        if (m_isBound) {
            throw new IllegalStateException
                ("Call not allowed after bindings compiled");
        } else {
            m_bindings.add(Utility.loadBinding(fname, sname, is, url, true));
        }
    }
    
    /**
     * Load binding definition from file path. This may be called multiple times
     * to load multiple bindings, but only prior to the bindings being compiled.
     *
     * @param path binding definition file path
     * @exception IllegalStateException if called after bindings have been
     * compiled
     * @exception IOException if error reading the file
     * @exception JiBXException if error in processing the binding definition
     */
    public void loadFileBinding(String path) throws JiBXException, IOException {
    
        // error if called after bindings have been compiled
        if (m_isBound) {
            throw new IllegalStateException
                ("Call not allowed after bindings compiled");
        } else {
            m_bindings.add(Utility.loadFileBinding(path, true));
        }
    }
    
    /**
     * Load binding definition from file path. This may be called multiple times
     * to load multiple bindings, but only prior to the bindings being compiled.
     *
     * @param path binding definition file path
     * @exception IllegalStateException if called after bindings have been
     * compiled
     * @exception IOException if error reading the file
     * @exception JiBXException if error in processing the binding definition
     */
    public void loadResourceBinding(String path)
        throws JiBXException, IOException {
    
        // error if called after bindings have been compiled
        if (m_isBound) {
            throw new IllegalStateException
                ("Call not allowed after bindings compiled");
        } else {
            String fname = path;
            int split = fname.lastIndexOf('/');
            if (split >= 0) {
                fname = fname.substring(split+1);
            }
            InputStream is = getResourceAsStream(path);
            if (is == null) {
                throw new IOException("Resource " + path + " not found");
            } else {
                String bname = Utility.bindingFromFileName(fname);
                loadBinding(fname, bname, is, null);
            }
        }
    }
    
    /**
     * Process the binding definitions. This compiles the bindings into the
     * classes, saving the modified classes for loading when needed.
     *
     * @exception JiBXException if error in processing the binding definition
     */
    public void processBindings() throws JiBXException {
        if (!m_isBound) {
            
            // handle code generation from bindings
            int count = m_bindings.size();
            for (int i = 0; i < count; i++) {
                BindingDefinition binding =
                    (BindingDefinition)m_bindings.get(i);
                binding.generateCode(false, false);
            }
            
            // finish binding factories with information on classes used
            ClassFile[][] lists = MungedClass.fixDispositions();
            for (int i = 0; i < count; i++) {
                BindingDefinition binding =
                    (BindingDefinition)m_bindings.get(i);
                binding.addClassList(lists[0], lists[1]);
            }
            
            // build hashmap of modified classes
            count = lists[0].length;
            for (int i = 0; i < count; i++) {
                ClassFile clas = lists[0][i];
                m_classMap.put(clas.getName(), clas);
            }
            
            // finish by setting flag for binding done
            m_isBound = true;
        }
    }
    
    /**
     * Check if a class has been modified by a binding. If bindings haven't been
     * compiled prior to this call they will be compiled automatically when this
     * method is called.
     * 
     * @param name fully qualified package and class name to be found
     * @return <code>true</code> if class modified by binding,
     * <code>false</code> if not
     */
    protected boolean isBoundClass(String name) {
        
        // first complete binding processing if not already done
        if (!m_isBound) {
            try {
                processBindings();
            } catch (JiBXException e) {
                e.printStackTrace();
            }
        }
        return m_classMap.containsKey(name);
    }
    
    /**
     * Find and load class by name. If the named class has been modified by a
     * binding this loads the modified binary class; otherwise, it just uses the
     * base class implementation to do the loading. If bindings haven't been
     * compiled prior to this call they will be compiled automatically when this
     * method is called.
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     * 
     * @param name fully qualified package and class name to be found
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        
        // check if class has been modified by binding
        if (isBoundClass(name)) {
			try {
				
				// convert class information to byte array
                ClassFile clas = (ClassFile)m_classMap.get(name);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				clas.writeFile(bos);
                byte[] bytes = bos.toByteArray();
                return defineClass(name, bytes, 0, bytes.length);
				
			} catch (IOException e) {
				throw new ClassNotFoundException
                    ("Unable to load modified class " + name);
			}
        } else {
            
            // just use base class handling
            return super.findClass(name);
            
        }
    }
    
    /**
     * Version of bind-on-demand loader which will not delegate handling of
     * classes included in the binding definition. Somewhat dangerous to use,
     * since it may result in loading multiple versions of the same class
     * (with and without binding).
     */
    public static class NondelegatingLoader extends Loader
    {
        /**
         * @throws MalformedURLException
         */
        public NondelegatingLoader() throws MalformedURLException {
            super(getClassPaths(), ClassLoader.getSystemClassLoader());
        }

        /* (non-Javadoc)
         * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
         */
        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (isBoundClass(name)) {
                Class clas = findLoadedClass(name);
                if (clas == null) {
                    clas = findClass(name);
                }
                return clas;
            } else {
                return super.loadClass(name, resolve);
            }
        }
    }
}
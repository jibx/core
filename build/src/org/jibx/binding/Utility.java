/*
Copyright (c) 2003-2012, Dennis M. Sosnoski
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.def.BindingBuilder;
import org.jibx.binding.def.BindingDefinition;
import org.jibx.binding.def.MappingBase;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.IncludeElement;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.ValidationContext;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * Binding compiler support class. Supplies common methods for use in compiling
 * binding definitions.
 *
 * @author Dennis M. Sosnoski
 */
public class Utility
{
    // buffer size for copying stream input
    private static final int COPY_BUFFER_SIZE = 1024;
    
    // private constructor to prevent any instance creation
    private Utility() {}

    /**
     * Read contents of stream into byte array.
     *
     * @param is input stream to be read
     * @return array of bytes containing all data from stream
     * @throws IOException on stream access error
     */
    private static byte[] getStreamData(InputStream is) throws IOException {
        byte[] buff = new byte[COPY_BUFFER_SIZE];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int count;
        while ((count = is.read(buff)) >= 0) {
            os.write(buff, 0, count);
        }
        return os.toByteArray();
    }
    
    /**
     * Recurse through jar file path component, adding all jars referenced from
     * the original jar to the path collection. Silently ignores problems
     * loading jar files.
     *
     * @param path jar path component
     * @param paths set of paths processed (added to by call)
     */
    private static void recursePathJars(String path, ArrayList paths) {
        try {
            
            // check class path information in jar file
            JarFile jfile = new JarFile(path, false);
            Manifest mfst = jfile.getManifest();
            if (mfst != null) {
                
                // look for class path information from manifest
                Attributes attrs = mfst.getMainAttributes();
                String cpath = (String)attrs.get(Attributes.Name.CLASS_PATH);
                if (cpath != null) {
                
                    // set base path for all relative references
                    if (File.separatorChar != '/') {
                        path = path.replace('/', File.separatorChar);
                    }
                    int split = path.lastIndexOf(File.separatorChar);
                    String base = (split >= 0) ?
                        path.substring(0, split+1) : "";
                
                    // process all references in jar class path
                    while (cpath != null) {
                        split = cpath.indexOf(' ');
                        String item;
                        if (split >= 0) {
                            item = cpath.substring(0, split);
                            cpath = cpath.substring(split+1).trim();
                        } else {
                            item = cpath;
                            cpath = null;
                        }
                        String ipath = base + item;
                        if (!paths.contains(ipath)) {
                            paths.add(ipath);
                            split = ipath.lastIndexOf('.');
                            if (split >= 0 && "jar".equalsIgnoreCase
                                (ipath.substring(split+1))) {
                                recursePathJars(ipath, paths);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) { /* silently ignore problems in loading */ }
    }
    
    /**
     * Method builds a string array of items in the class path.
     *
     * @return array of classpath components
     */
    public static String[] getClassPaths() {
        
        // get all class path components
        String path = System.getProperty("java.class.path");
        ArrayList paths = new ArrayList();
        int start = 0;
        int mark;
        while (path != null) {
            mark = path.indexOf(File.pathSeparatorChar, start);
            String item;
            if (mark >= 0) {
                item = path.substring(start, mark);
            } else {
                item = path.substring(start);
                path = null;
            }
            if (!paths.contains(item)) {
                paths.add(item);
                int split = item.lastIndexOf('.');
                if (split >= 0 &&
                    "jar".equalsIgnoreCase(item.substring(split+1))) {
                    recursePathJars(item, paths);
                }
            }
            start = mark + 1;
        }
        paths.add(".");
        String[] clsspths = new String[paths.size()];
        paths.toArray(clsspths);
        return clsspths;
    }
    
    /**
     * Extract base file name from a full path.
     *
     * @param path full file path
     * @return file name component from path
     */
    public static String fileName(String path) {
        if (File.separatorChar != '/') {
            path = path.replace('/', File.separatorChar);
        }
        int split = path.lastIndexOf(File.separatorChar);
        return path.substring(split+1);
    }
    
    /**
     * Get the default binding name from a supplied file name.
     *
     * @param fname simple file name (without leading path information)
     * @return default binding name
     */
    public static String bindingFromFileName(String fname) {
        String sname = fname;
        int split = sname.indexOf('.');
        if (split > 0) {
            sname = sname.substring(0, split);
        }
        return BindingDirectory.convertName(sname);
    }

    /**
     * Validate binding definition. If issues are found in the binding the
     * issues are printed directly to the console.
     *
     * @param name identifier for binding definition
     * @param url URL for binding definition (<code>null</code> if not
     * available)
     * @param is input stream for reading binding definition
     * @return root element of binding model if binding is valid,
     * <code>null</code> if one or more errors in binding
     */
    public static BindingElement validateBinding(String name, URL url,
        InputStream is) {
        try {
            ValidationContext vctx = BindingElement.newValidationContext();
            BindingElement root =
                BindingElement.validateBinding(name, url, is, vctx);
            if (vctx.getErrorCount() == 0 && vctx.getFatalCount() == 0) {
                return root;
            }
        } catch (JiBXException ex) {
            System.err.println("Unable to process binding " + name);
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * Load validated binding definition. This first reads the input stream into
     * a memory buffer, then parses the data once for validation and a second
     * time for the actual binding definition construction. If any errors are
     * found in the binding definition validation the construction is skipped
     * and an exception is thrown.
     *
     * @param fname binding definition full name
     * @param sname short form of name to use as the default name of the binding
     * @param istrm input stream for binding definition document
     * @param url URL for binding definition (<code>null</code> if not
     * available)
     * @param test validate binding flag
     * @return constructed binding definition
     * @exception FileNotFoundException if path cannot be accessed
     * @exception JiBXException if error in processing the binding definition
     * @throws IOException if error reading the binding
     */
    public static BindingDefinition loadBinding(String fname, String sname,
        InputStream istrm, URL url, boolean test)
        throws JiBXException, IOException {
        
        // read stream into memory buffer
        byte[] data = getStreamData(istrm);
        
        // validate using binding object model
        boolean valid = true;
        ClassFile cf = null;
        String tpack = null;
        String bname = null;
        if (test) {
            BindingElement root = validateBinding(fname, url,
                new ByteArrayInputStream(data));
            if (root == null) {
                valid = false;
            } else {
                
                // find package of first mapping to use for added classes
                cf = findMappedClass(root);
                tpack = root.getTargetPackage();
                if (tpack == null && cf != null) {
                    tpack = cf.getPackage();
                }
                bname = root.getName();
                
            }
        }
        if (valid) {
            try {
                
                // construct the binding definition code generator
                UnmarshallingContext uctx = new UnmarshallingContext();
                uctx.setDocument(new ByteArrayInputStream(data), fname, null);
                if (cf != null) {
                    
                    // set target root and package for created classes
                    if (bname == null) {
                        bname = sname;
                    }
                    BoundClass.setModify(cf.getRoot(), tpack, bname);
                    
                }
                BindingDefinition bdef =
                    BindingBuilder.unmarshalBindingDefinition(uctx, sname, url);
                
                // set package and class if not validated
                if (!test) {
                    
                    // a kludge, but needed to support skipping validation
                    ArrayList maps = bdef.getDefinitionContext().getMappings();
                    if (maps != null) {
                        
                        // set up package information from mapped class
                        for (int i = 0; i < maps.size(); i++) {
                            Object child = maps.get(i);
                            if (child instanceof MappingBase) {
                                
                                // end scan if a real mapping is found
                                MappingBase mapbase = (MappingBase)child;
                                cf = mapbase.getBoundClass().getMungedFile();
                                if (mapbase.getBoundClass().isDirectAccess()) {
                                    break;
                                }
                                
                            }
                        }
                    }
                }
                
                // set up binding root based on first mapping
                File root = null;
                if (tpack == null) {
                    tpack = bdef.getDefaultPackage();
                }
                if (cf == null) {
                    root = ClassCache.getModifiablePath();
                    if (root == null) {
                        throw new IllegalStateException("Need modifiable directory on classpath for storing generated factory class file");
                    }
                    if (tpack == null) {
                        tpack = "";
                    }
                } else {
                    root = cf.getRoot();
                    if (tpack == null) {
                        tpack = cf.getPackage();
                    }
                }
                bdef.setFactoryLocation(tpack, root);
                return bdef;
                
            } catch (JiBXException e) {
                throw new JiBXException
                    ("\n*** Error during code generation for file '" + fname +
                    "' - please enter a bug report for this error in Jira if " +
                    "the problem is not listed as fixed on the online status " +
                    "page ***\n", e);
            }
            
        } else {
            throw new JiBXException("Binding " + fname +
                " is unusable because of validation errors");
        }
    }

    /**
     * Recursively search through binding definitions for a modifiable mapped
     * class. This is used to determine the default package for code generation.
     * 
     * @param root binding element at root of definition
     * @return modifiable mapped class, or <code>null</code> if none
     */
    private static ClassFile findMappedClass(BindingElement root) {
        ArrayList childs = root.topChildren();
        if (childs != null) {
            
            // recursively search for modifiable mapped class
            for (int i = childs.size() - 1; i >= 0 ; i--) {
                Object child = childs.get(i);
                if (child instanceof MappingElement) {
                    
                    // end scan if a real mapping is found
                    MappingElementBase map = (MappingElementBase)child;
                    ClassFile cf = map.getHandledClass().getClassFile();
                    if (!cf.isInterface() && cf.isModifiable()) {
                        return cf;
                    }
                    
                } else if (child instanceof IncludeElement) {
                    
                    // recurse on included binding
                    BindingElement bind = ((IncludeElement)child).getBinding();
                    if (bind != null) {
                        ClassFile cf = findMappedClass(bind);
                        if (cf != null) {
                            return cf;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Load binding definition from file.
     *
     * @param path file path for binding definition
     * @param valid validate binding flag
     * @return constructed binding definition
     * @exception IOException if error accessing file
     * @exception JiBXException if error in processing the binding definition
     */
    public static BindingDefinition loadFileBinding(String path, boolean valid)
        throws JiBXException, IOException {
        
        // extract basic name of binding file from path
        File file = new File(path);
        String fname = fileName(file.getAbsolutePath());
        String sname = bindingFromFileName(fname);
        
        // construct and return the binding definition
        return loadBinding(fname, sname, new FileInputStream(file),
            file.toURI().toURL(), valid);
    }
}
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
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.verifier.VerificationResult;
import org.apache.bcel.verifier.Verifier;
import org.apache.bcel.verifier.VerifierFactory;
import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.BranchWrapper;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.MungedClass;
import org.jibx.binding.def.BindingDefinition;
import org.jibx.runtime.JiBXException;
import org.jibx.util.ClasspathUrlExtender;

/**
 * Binding compiler. This version checks the modified and generated classes
 * by loading them and listing method information.
 *
 * @author Dennis M. Sosnoski
 */
public class Compile
{
    private boolean m_verbose1;
    private boolean m_verbose2;
    private boolean m_load;
    private boolean m_verify;
    private boolean m_trackBranches;
    private boolean m_errorOverride;
    private boolean m_skipValidate;
    
    /**
     * Default constructor. This just initializes all options disabled.
     */

    public Compile() {}
    
    /**
     * Constructor with settings specified.
     *
     * @param verbose1 report binding details and results
     * @param verbose2 report second pass binding details
     * @param load test load modified classes to validate
     * @param verify use BCEL validation of modified classes
     * @param track keep tracking information for source of branch generation
     * @param over override code generation error handling
     */
    public Compile(boolean verbose1, boolean verbose2, boolean load,
        boolean verify, boolean track, boolean over) {
        m_verbose1 = verbose1;
        m_verbose2 = verbose2;
        m_load = load;
        m_verify = verify;
        m_trackBranches = track;
        m_errorOverride = over;
    }
    
    /**
     * Verify generated and modified files using BCEL verifier. This provides a
     * more comprehensive listing of errors than just loading a class in the
     * JVM.
     *
     * @param file information for class to be verified
     * @return <code>true</code> if successfully verified, <code>false</code> if
     * problem found (automatically reported)
     */
    private boolean verifyBCEL(ClassFile file) {
        try {
            
            // construct verifier for class file
            Verifier verifier = VerifierFactory.getVerifier(file.getName());
            
            // run validation in stages with error handling for each stage
            boolean verified = false;
            VerificationResult vr = verifier.doPass1();
            if (vr.getStatus() == VerificationResult.VERIFIED_OK) {
                vr = verifier.doPass2();
                if (vr.getStatus() == VerificationResult.VERIFIED_OK) {
                    Method[] methods = file.getRawClass().getMethods();
                    for (int j = 0; j < methods.length; j++) {
                        vr = verifier.doPass3a(j);
                        if (vr.getStatus() == VerificationResult.VERIFIED_OK) {
                            vr = verifier.doPass3b(j);
                        }
                        if (vr.getStatus() == VerificationResult.VERIFIED_OK) {
                            verified = true;
                        } else {
                            System.out.println
                                ("Verification failure on method " +
                                methods[j].getName() + " of class " +
                                file.getName() + ":");
                            System.out.println("  " + vr.toString());
                        }
                    }
                } else {
                    System.out.println("Verification failure on class " +
                        file.getName() + ":");
                    System.out.println("  " + vr.toString());
                }
            } else {
                System.out.println("Verification failure on class " +
                    file.getName() + ":");
                System.out.println("  " + vr.toString());
            }
            return verified;
            
        } catch (Exception ex) {    // catch BCEL errors
            System.out.println("BCEL failure:");
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * Set control flag for test loading generated/modified classes.
     * 
     * @param load test load generated/modified classes flag
     */
    public void setLoad(boolean load) {
        m_load = load;
    }

    /**
     * Set control flag for verbose processing reports.
     * 
     * @param verbose report verbose information in processing bindings flag
     */
    public void setVerbose(boolean verbose) {
        m_verbose1 = verbose;
    }

    /**
     * Set control flag for verifying generated/modified classes with BCEL.
     * 
     * @param verify use BCEL verification for generated/modified classes flag
     */
    public void setVerify(boolean verify) {
        m_verify = verify;
    }

    /**
     * Set control flag for skipping binding validation. This flag is intended
     * only for use while processing the binding model components within JiBX.
     * Otherwise it'd be impossible to correct errors in the binding validation.
     * 
     * @param skip test load generated/modified classes flag
     */
    public void setSkipValidate(boolean skip) {
        m_skipValidate = skip;
    }
    
    /**
     * Compile a set of bindings using supplied classpaths.
     *
     * @param paths list of paths for loading classes
     * @param files list of binding definition files
     * @exception JiBXException if error in processing the binding definition
     */
    public void compile(String[] paths, String[] files) throws JiBXException {
        try {
            
            // include current version information in verbose output
            if (m_verbose1) {
                System.out.println("Running binding compiler version " +
                    BindingDefinition.CURRENT_VERSION_NAME);
            }
            
            // set paths to be used for loading referenced classes
            ClassCache.setPaths(paths);
            ClassFile.setPaths(paths);
            ClasspathUrlExtender.setClassLoader(ClassFile.getClassLoader());
            
            // reset static information accumulation for binding
            BoundClass.reset();
            MungedClass.reset();
            BindingDefinition.reset();
            BranchWrapper.setTracking(m_trackBranches);
            BranchWrapper.setErrorOverride(m_errorOverride);
            
            // load all supplied bindings
            BindingDefinition[] defs = new BindingDefinition[files.length];
            for (int i = 0; i < files.length; i++) {
                defs[i] = Utility.loadFileBinding(files[i], !m_skipValidate);
                if (m_verbose1) {
                    defs[i].print();
                }
            }
            
            // modify the class files with JiBX hooks
            for (int i = 0; i < defs.length; i++) {
                try {
                    defs[i].generateCode(m_verbose1, m_verbose2);
                } catch (RuntimeException e) {
                    throw new JiBXException
                        ("\n*** Error during code generation for file '" +
                        files[i] + "' -\n this may be due to an error in " +
                        "your binding or classpath, or to an error in the " +
                        "JiBX code ***\n", e);
                }
            }
            
            // get the lists of class names modified, kept unchanged, and unused
            ClassFile[][] lists = MungedClass.fixDispositions();
            
            // add class used list to each binding factory and output files
            for (int i = 0; i < defs.length; i++) {
                defs[i].addClassList(lists[0], lists[1]);
            }
            MungedClass.writeChanges();
            
            // report modified file results to user
            ClassFile[] adds = lists[0];
            int addcount = adds.length;
            if (m_verbose1) {
                System.out.println("\nWrote " + addcount + " files");
            }
            if (m_verbose1 || m_load) {
                
                // generate class paths as URLs if needed for test loading
                URL[] urls = null;
                if (m_load) {
                    urls = new URL[paths.length];
                    for (int i = 0; i < urls.length; i++) {
                        urls[i] = new File(paths[i]).toURI().toURL();
                    }
                }
                for (int i = 0; i < addcount; i++) {
                    
                    // write class file to bytes
                    ClassFile file = adds[i];
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    file.writeFile(bos);
                    byte[] bytes = bos.toByteArray();
                    if(m_verbose1){
                        System.out.println("\n " + file.getName() +
                            " output file size is " + bytes.length + " bytes");
                    }
                    
                    // verify using BCEL verifier
                    if (m_verify) {
                        verifyBCEL(file);
                    }
                    
                    // load to JVM and list method information from class
                    if (m_load) {
                        DirectLoader cloader = new DirectLoader(urls);
                        Class clas = cloader.load(file.getName(), bytes);
                        if (m_verbose1) {
                            java.lang.reflect.Method[] methods =
                                clas.getDeclaredMethods();
                            System.out.println(" Found " + methods.length +
                                " methods:");
                            for (int j = 0; j < methods.length; j++) {
                                java.lang.reflect.Method method = methods[j];
                                System.out.println("  " +
                                    method.getReturnType().getName() + " " +
                                    method.getName());
                            }
                        }
                    }
                }
            }
            
            // report summary information for files unchanged or deleted
            if (m_verbose1) {
                ClassFile[] keeps = lists[1];
                System.out.println("\nKept " + keeps.length + " files unchanged:");
                for (int i = 0; i < keeps.length; i++) {
                    System.out.println(" " + keeps[i].getName());
                }
                ClassFile[] dels = lists[2];
                System.out.println("\nDeleted " + dels.length + " files:");
                for (int i = 0; i < dels.length; i++) {
                    System.out.println(" " + dels[i].getName());
                }
            }
            
        } catch (IOException ex) {
            throw new JiBXException("IOException in compile", ex);
        } catch (ExceptionInInitializerError ex) {
            throw new JiBXException("Error during initialization;" +
                " is jibx-run.jar in load classpath?", ex.getException());
        } catch (Throwable ex) {
            throw new JiBXException("Error running binding compiler", ex);
        }
    }
    
    /**
     * Main method for running compiler as application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                
                // check for various flags set
                boolean verbose1 = false;
                boolean verbose2 = false;
                boolean load = false;
                boolean verify = false;
                boolean track = false;
                boolean over = false;
                boolean skip = false;
                int offset = 0;
                for (; offset < 5 && offset < args.length; offset++) {
                    String arg = args[offset];
                    if ("-v".equalsIgnoreCase(arg)) {
                        if (verbose1) {
                            verbose2 = true;
                        } else {
                            verbose1 = true;
                        }
                    } else if ("-l".equalsIgnoreCase(arg)) {
                        load = true;
                    } else if ("-b".equalsIgnoreCase(arg)) {
                        verify = true;
                    } else if ("-o".equalsIgnoreCase(arg)) {
                        over = true;
                    } else if ("-s".equalsIgnoreCase(arg)) {
                        skip = true;
                    } else if ("-t".equalsIgnoreCase(arg)) {
                        track = true;
                    } else {
                        break;
                    }
                }
                
                // set up path and binding lists
                String[] clsspths = Utility.getClassPaths();
                String[] bindings = new String[args.length - offset];
                System.arraycopy(args, offset, bindings, 0, bindings.length);
                
                // report on the configuration
                if (verbose1) {
                    System.out.println("Using paths:");
                    for (int i = 0; i < clsspths.length; i++) {
                        System.out.println(" " + clsspths[i]);
                    }
                    System.out.println("Using bindings:");
                    for (int i = 0; i < bindings.length; i++) {
                        System.out.println(" " + bindings[i]);
                    }
                }
                
                // compile the bindings
                Compile compiler =
                    new Compile(verbose1, verbose2, load, verify, track, over);
                compiler.setSkipValidate(skip);
                compiler.compile(clsspths, bindings);
                
            } catch (JiBXException ex) {
                ex.printStackTrace(System.out);
                System.exit(1);
            }
            
        } else {
            System.out.println
                ("\nUsage: java org.jibx.binding.Compile [-b] [-l] [-v] " +
                "binding1 binding2 ...\nwhere:\n -b  turns on BCEL " +
                "verification (debug option),\n -l  turns on test loading of " +
                "modified or generated classes for validation, and\n" +
                " -v  turns on verbose output\nThe bindingn files are " +
                "different bindings to be compiled.\n");
            System.exit(1);
        }
    }
    
    /**
     * Direct class loader. This is optionally used for test loading the
     * modified class files to make sure they're still valid.
     */
    private static class DirectLoader extends URLClassLoader
    {
        protected DirectLoader(URL[] urls) {
            super(urls, DirectLoader.class.getClassLoader());
        }
        
        protected Class load(String name, byte[] data) {
            return defineClass(name, data, 0, data.length);
        }
    }
}
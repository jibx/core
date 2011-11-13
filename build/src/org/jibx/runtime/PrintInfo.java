/*
 * Copyright (c) 2008, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * Utility program to print information about the runtime build and, optionally,
 * about bindings.
 * 
 * @author Dennis M. Sosnoski
 */
public class PrintInfo
{
    public static void main(String[] args) {
        System.out.print("JiBX distribution ");
        System.out.print(IBindingFactory.CURRENT_VERSION_NAME);
        System.out.print(", version code ");
        System.out.print(Integer
            .toHexString(IBindingFactory.CURRENT_VERSION_NUMBER));
        System.out.print("\n(compatible with binding compiler version codes ");
        System.out.print(Integer
            .toHexString(IBindingFactory.CURRENT_VERSION_NUMBER >> 16));
        System.out.println("XXXX)");
        boolean usage = false;
        if (args.length > 0) {
            
            // check for supported arguments
            boolean verbose = false;
            String bname = null;
            String cname = null;
            String pname = null;
            for (int offset = 0; offset < args.length; offset++) {
                String arg = args[offset];
                if ("-v".equalsIgnoreCase(arg)) {
                    verbose = true;
                } else if ("-b".equalsIgnoreCase(arg)) {
                    bname = args[++offset];
                } else if ("-c".equalsIgnoreCase(arg)) {
                    cname = args[++offset];
                } else if ("-p".equalsIgnoreCase(arg)) {
                    pname = args[++offset];
                } else {
                    if (!"-?".equals(arg)) {
                        System.err.print("Unknown argument '");
                        System.out.print(arg);
                        System.out.println('\'');
                    }
                    usage = true;
                    break;
                }
            }
            if (cname == null) {
                if (bname != null && pname == null) {
                    System.err.println("Binding name requires package name or class name.");
                    usage = true;
                } else if (bname == null && pname != null) {
                    System.err.println("Package name requires binding name.");
                    usage = true;
                }
            } else if (pname != null) {
                if (cname.indexOf('.') > 0 && !cname.startsWith(pname)) {
                    System.err.println("When class and package names are both provided, the class must be in the package");
                    usage = true;
                } else {
                    cname = pname + '.' + cname;
                }
            }
            if (usage) {
                System.out
                    .print("\nUsage: java org.jibx.runtime.PrintInfo "
                        + "[-b bname] [-c cname] [-p pname] [-v]\nwith the optional"
                        + " parameters:\n -b bname  gives a binding name\n"
                        + " -c cname  gives a class name\n"
                        + " -p pname  gives a package name, and\n"
                        + " -v        turns on verbose output\n"
                        + "If none of the name arguments are supplied the program "
                        + "will just print the\nruntime version information and "
                        + "exit. If one or more of the name parameters are\n"
                        + "supplied, it will attempt to find a binding and list "
                        + "the binding information.\nThe valid combinations of "
                        + "arguments are a fully-qualified class name by itself,\n"
                        + "when the class has a single binding; or a binding name "
                        + "along with either a class\nname or a package name. The "
                        + "target class or package must either be in the\n"
                        + "classpath used for executing the program, or in the "
                        + "path from the current\ndirectory. The 'verbose' flag "
                        + "controls the level of detail included in the\nlisting.");
            } else {
                try {
                    
                    IBindingFactory factory;
                    URL[] urls = new URL[] { new File(".").toURL() };
                    URLClassLoader loader = new URLClassLoader(urls);
                    if (cname == null) {
                        factory = BindingDirectory.getFactory(bname, pname,
                            loader);
                    } else {
                        Class clas = loader.loadClass(cname);
                        if (bname == null) {
                            factory = BindingDirectory.getFactory(clas);
                        } else {
                            factory = BindingDirectory.getFactory(bname, clas,
                                loader);
                        }
                    }
                    System.out.print("Found binding '");
                    System.out.print(factory.getBindingName());
                    System.out.print("' ");
                    int major = factory.getMajorVersion();
                    int minor = factory.getMinorVersion();
                    if (major != 0 || minor != 0) {
                        System.out.print("(version ");
                        System.out.print(major);
                        System.out.print(", ");
                        System.out.print(minor);
                        System.out.print(") ");
                    }
                    System.out.println();
                    System.out.print(" compiled with JiBX distribution ");
                    System.out.print(factory.getCompilerDistribution());
                    System.out.print(", version code ");
                    System.out.println(Integer.toHexString(factory
                        .getCompilerVersion()));
                    String[] nss = factory.getNamespaces();
                    String[] prefs = factory.getPrefixes();
                    if (prefs != null) {
                        if (nss.length > 2) {
                            System.out.print("Defines ");
                            System.out.print((nss.length - 2));
                            System.out.println(" namespace(s):");
                            for (int i = 1; i < prefs.length; i++) {
                                System.out.print(" ");
                                System.out.print(nss[i]);
                                if (prefs[i] == null) {
                                    System.out.println(" (default namespace)");
                                } else {
                                    System.out.print(" (prefix ");
                                    System.out.print(prefs[i]);
                                    System.out.println(')');
                                }
                            }
                        }
                    }
                    listBaseBindings(factory, verbose);
                    listClasses(factory, verbose);
                    listAbstracts(factory, verbose);
                    if (verbose) {
                        System.out.println("Classes containing binding code:");
                        String[] classes = factory.getBindingClasses();
                        for (int i = 0; i < classes.length; i++) {
                            System.out.print(" ");
                            System.out.println(classes[i]);
                        }
                    }
                } catch (JiBXException ex) {
                    ex.printStackTrace(System.out);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                
            }
        } else {
            System.out.println("Use '-?' argument for usage details");
        }
    }
    
    /**
     * List class information from factory.
     * 
     * @param factory
     * @param verbose
     */
    private static void listClasses(IBindingFactory factory, boolean verbose) {
        String[] classes = factory.getMappedClasses();
        String[] names = factory.getElementNames();
        String[] uris = factory.getElementNamespaces();
        String[] mars = factory.getMarshallerClasses();
        String[] umars = factory.getUnmarshallerClasses();
        System.out.print("Includes information for ");
        System.out.print(classes.length);
        System.out.println(" mapping(s):");
        for (int i = 0; i < classes.length; i++) {
            String clas = classes[i];
            String name = null;
            String mar = null;
            if (mars != null && i < mars.length) {
                mar = mars[i];
            }
            String umar = null;
            if (umars != null && i < umars.length) {
                umar = umars[i];
            }
            if ((i < names.length && (name = names[i]) != null) || mar != null
                || umar != null) {
                if (name == null) {
                    System.out.print(" class ");
                    System.out.print(clas);
                    System.out.println(" abstract mapping");
                } else {
                    System.out.print(" class ");
                    System.out.print(clas);
                    System.out.print(" mapped to element ");
                    System.out.println(UnmarshallingContext.buildNameString(
                        uris[i], name));
                }
                if (verbose) {
                    if (mar != null) {
                        System.out.print("  marshaller class ");
                        System.out.println(mar);
                    }
                    if (umar != null) {
                        System.out.print("  unmarshaller class ");
                        System.out.println(umar);
                    }
                }
            } else if (verbose) {
                System.out.print(" reference to class or type ");
                System.out.println(clas);
            }
        }
    }
    
    /**
     * List precompiled base bindings from factory.
     * 
     * @param factory
     * @param verbose
     */
    private static void listBaseBindings(IBindingFactory factory,
        boolean verbose) {
        String[] bases = factory.getBaseBindings();
        if (bases.length > 0) {
            String[] factories = factory.getBaseBindingFactories();
            Map xlatemap = factory.getNamespaceTranslationTableMap();
            System.out.println("Uses precompiled base binding(s):");
            for (int i = 0; i < bases.length; i++) {
                System.out.print(" ");
                System.out.print(bases[i]);
                System.out.print(" with binding factory ");
                System.out.println(factories[i]);
                int[] xlate = (int[])xlatemap.get(factories[i]);
                if (verbose && xlate != null) {
                    System.out.println("  namespace translations:");
                    System.out.print("  ");
                    for (int j = 0; j < xlate.length; j++) {
                        System.out.print(' ');
                        System.out.print(xlate[j]);
                    }
                }
                System.out.println();
            }
        }
    }
    
    /**
     * List abstract mappings from factory.
     * 
     * @param factory
     * @param verbose
     */
    private static void listAbstracts(IBindingFactory factory, boolean verbose) {
        String[][] abs = factory.getAbstractMappings();
        int length = abs[0].length;
        if (length > 0) {
            System.out.print("Defines ");
            System.out.print(length);
            System.out.println(" abstract mapping(s):");
            for (int i = 0; i < length; i++) {
                String cname = abs[IBindingFactory.ABMAP_CLASSNAME_INDEX][i];
                String tname = abs[IBindingFactory.ABMAP_MAPPINGNAME_INDEX][i];
                System.out.print(" class ");
                System.out.print(cname);
                if (!cname.equals(tname)) {
                    System.out.print(" (type \"");
                    System.out.print(tname);
                    System.out.print("\")");
                }
                System.out.println();
                if (verbose) {
                    int[] nss = factory.getAbstractMappingNamespaces(i);
                    if (nss.length > 0) {
                        System.out.print("  using namespace(s):");
                        for (int j = 0; j < nss.length; j++) {
                            System.out.print(" ");
                            System.out.print(nss[j]);
                        }
                        System.out.println();
                    }
                    String name = abs[IBindingFactory.ABMAP_CREATEMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  create method ");
                        System.out.println(name);
                    }
                    name = abs[IBindingFactory.ABMAP_COMPLETEMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  complete method ");
                        System.out.println(name);
                    }
                    name = abs[IBindingFactory.ABMAP_PREPAREMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  prepare method ");
                        System.out.println(name);
                    }
                    name = abs[IBindingFactory.ABMAP_ATTRPRESMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  attribute presence test method ");
                        System.out.println(name);
                    }
                    name = abs[IBindingFactory.ABMAP_ATTRUMARMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  attribute unmarshal method ");
                        System.out.println(name);
                    }
                    name = abs[IBindingFactory.ABMAP_ATTRMARMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  attribute marshal method ");
                        System.out.println(name);
                    }
                    name = abs[IBindingFactory.ABMAP_CONTPRESMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  content presence test method ");
                        System.out.println(name);
                    }
                    name = abs[IBindingFactory.ABMAP_CONTUMARMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  content unmarshal method ");
                        System.out.println(name);
                    }
                    name = abs[IBindingFactory.ABMAP_CONTMARMETH_INDEX][i];
                    if (name != null) {
                        System.out.print("  content marshal method ");
                        System.out.println(name);
                    }
                }
            }
        }
    }
}
/*
Copyright (c) 2003-2005, Dennis M. Sosnoski
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.jibx.runtime.JiBXException;

/**
 * Bind-on-load class runner. This uses a binding loader to compile a binding,
 * then loads and calls the main execution class for an application substituting
 * the classes modified by the binding.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
 
public class Run
{
    private static final String BINDING_LIST_RESOURCE = "jibx_bindings.txt";
    private static final String DEFAULT_BINDING_RESOURCE = "jibx_binding.xml";
    
    private Run() {}
    
    /**
     * Accumulate list of bindings from stream.
     *
     * @param is stream to be read for list of bindings (one per line)
     * @param bindings accumulated collection of bindings
     */
    private static void addBindings(InputStream is, ArrayList bindings)
        throws IOException {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = rdr.readLine()) != null) {
            if (line.length() > 0) {
                bindings.add(line);
            }
        }
    }
    
    /**
     * Main method for bind-on-load handling.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length >= 1) {
            try {
            
                // first get binding definitions and target class information
                ArrayList files = new ArrayList();
                ArrayList resources = new ArrayList();
                int index = 0;
                String target = null;
                while (index < args.length) {
                    String arg = args[index++];
                    if ("-b".equals(arg)) {
                        if (index < args.length) {
                            files.add(args[index++]);
                        } else {
                            System.err.println("Missing binding file and " +
                                "target class following '-b'");
                        }
                    } else if ("-l".equals(arg)) {
                        if (index < args.length) {
                            FileInputStream is =
                                new FileInputStream(args[index++]);
                            addBindings(is, files);
                            is.close();
                        } else {
                            System.err.println("Missing binding list file " +
                                "and target class following '-l'");
                        }
                    } else if ("-r".equals(arg)) {
                        if (index < args.length) {
                            resources.add(args[index++]);
                        } else {
                            System.err.println("Missing binding resource and " +
                                "target class following '-r'");
                        }
                    } else {
                        target = arg;
                        break;
                    }
                }
                
                // make sure we have a target class name
                if (target != null) {
                    
                    // save class name and create loader
                    Loader loader = new Loader();
                
                    // check binding resources if no specified bindings
                    if (files.size() == 0 && resources.size() == 0) {
                        InputStream is =
                            loader.getResourceAsStream(BINDING_LIST_RESOURCE);
                        if (is == null) {
                            String name = target.replace('.', '/') + "_" +
                                BINDING_LIST_RESOURCE;
                            is = loader.getResourceAsStream(name);
                        }
                        if (is != null) {
                            addBindings(is, resources);
                            is.close();
                        } else {
                            String name = DEFAULT_BINDING_RESOURCE;
                            is = loader.getResourceAsStream(name);
                            if (is == null) {
                                name = target.replace('.', '/') + "_" +
                                    DEFAULT_BINDING_RESOURCE;
                                is = loader.getResourceAsStream(name);
                            }
                            if (is != null) {
                                resources.add(name);
                                is.close();
                            }
                            
                        }
                    }
                    
                    // make sure at least one binding has been specified
                    if (files.size() == 0 && resources.size() == 0) {
                        System.err.println("No bindings found");
                    } else {
                
                        // compile all bindings
                        for (int i = 0; i < files.size(); i++) {
                            loader.loadFileBinding((String)files.get(i));
                        }
                        for (int i = 0; i < resources.size(); i++) {
                            String path = (String)resources.get(i);
                            InputStream is = loader.getResourceAsStream(path);
                            if (is == null) {
                                throw new IOException("Resource " + path +
                                    " not found on classpath");
                            }
                            String fname = Utility.fileName(path);
                            loader.loadBinding(fname,
                                Utility.bindingFromFileName(fname), is, null);
                        }
                        loader.processBindings();
                
                        // load the target class using custom class loader
                        Class clas = loader.loadClass(target);
                    
                        // invoke the "main" method of the application class
                        Class[] ptypes = new Class[] { args.getClass() };
                        Method main = clas.getDeclaredMethod("main", ptypes);
                        String[] pargs = new String[args.length-index];
                        System.arraycopy(args, index, pargs, 0, pargs.length);
                        Thread.currentThread().setContextClassLoader(loader);
                        main.invoke(null, new Object[] { pargs });
                        
                    }
                }
                
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (JiBXException e) {
				e.printStackTrace();
			} catch (IOException e) {
                e.printStackTrace();
            }
            
        } else {
            System.out.println
                ("Usage: org.jibx.binding.Run [-b binding-file][-l list-file]" +
                    "[-r binding-resource] main-class args...");
        }
    }
}
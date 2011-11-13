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

package org.jibx.match;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jibx.binding.Loader;

/**
 * Test program for the JiBX framework. Works with sets of three input
 * parameters: binding-resource, mapped-class, in-file. Unmarshals documents
 * from files using the binding defined for the mapped class, then marshals them
 * back out using the same bindings and compares the results. In case of a
 * comparison error the output file is left as <i>temp.xml</i>.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public class TestLoader
{
    private static final Class[] RUNNER_PARAM_TYPES =
    {
        String.class, String.class, String.class
    };
    
    private TestLoader() {}
    
    public static void main(String[] args) {
        if (args.length >= 3 && args.length % 3 == 0) {
            
            // delete generated output file if present
            File temp = new File("temp.xml");
            if (temp.exists()) {
                temp.delete();
            }
            
            // process each set of three arguments
            boolean err = false;
            int offset = 0;
            String[] pargs = new String[3];
            ClassLoader base = Thread.currentThread().getContextClassLoader();
            for (; offset < args.length; offset += 3) {
                try {
                
                    // load the test runner class using new custom class loader
                    Thread.currentThread().setContextClassLoader(base);
                    Loader loader = new Loader();
                    loader.loadResourceBinding(args[offset]);
                    
                    // invoke the "runTest" method of the runner class
                    Thread.currentThread().setContextClassLoader(loader);
                    Class clas = loader.loadClass("org.jibx.match.TestRunner");
                    Method test = clas.getDeclaredMethod("runTest",
                        RUNNER_PARAM_TYPES);
                    pargs[0] = args[offset+1];
                    pargs[1] = args[offset+2];
                    pargs[2] = args[offset+2];
                    Boolean result = (Boolean)test.invoke(null, pargs);
                    if (!result.booleanValue()) {
                        err = true;
                        break;
                    }
                } catch (InvocationTargetException ex) {
                    ex.getTargetException().printStackTrace();
                    err = true;
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    err = true;
                    break;
                }
            }
            
            // take error exit if difference found
            if (err) {
                System.err.println("Error on argument set: " + args[offset] +
                    ", " + args[offset+1] + ", " + args[offset+2]);
                System.err.println("File path " + temp.getAbsolutePath());
                System.exit(1);
            }
            
        } else {
            System.err.println("Requires arguments in sets of three:\n" +
                " binding-resource mapped-class in-file\n" +
                "Leaves output as temp.xml in case of error");
            System.exit(1);
        }
    }
}

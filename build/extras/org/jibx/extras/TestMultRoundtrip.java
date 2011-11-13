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

package org.jibx.extras;

import java.io.File;

/**
 * Test program for the JiBX framework. Works with three or four command line
 * arguments: mapped-class, binding-name, in-file, and out-file (optional, only
 * needed if different from in-file). You can also supply a multiple of four
 * input arguments, in which case each set of four is processed in turn (in this
 * case the out-file is required). Unmarshals documents from files using the
 * specified bindings for the mapped classes, then marshals them back out using
 * the same bindings and compares the results. In case of a comparison error the
 * output file is left as <i>temp.xml </i>.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public class TestMultRoundtrip {

    public static void main(String[] args) {
        if (args.length == 3 || (args.length > 0 && args.length % 4 == 0)) {
            
            // delete generated output file if present
            File temp = new File("temp.xml");
            if (temp.exists()) {
                temp.delete();
            }
            
            // process input arguments
            int base = 0;
            boolean err = false;
            String fin = null;
            String fout = null;
            while (base < args.length) {
                
                // run test with one argument set
                fin = args[base+2];
                fout = (args.length < base+4) ? fin : args[base+3];
                try {
                    if (!TestRoundtrip.runTest(args[base], args[base+1], fin,
                        fout)) {
                        err = true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
//                      System.err.println(ex.getMessage());
                    err = true;
                }
            
                // take error exit if difference found
                if (err) {
                    System.err.println("Error round-tripping class: " + args[base] +
                        "\n with input file " + fin + " and output compared to " +
                        fout);
                    System.err.println("Saved output document file path " +
                        temp.getAbsolutePath());
                    System.exit(1);
                }
                
                // advance to next argument set
                base += 4;
            }
            
        } else {
            System.err.println("Usage: java TestMultRoundtrip mapped-class" +
                " binding-name in-file [out-file]\n where out-file is only" +
                " required if the output document is different from\nthe" +
                " input document. Leaves output as temp.xml in case of error");
            System.exit(1);
        }
    }
}


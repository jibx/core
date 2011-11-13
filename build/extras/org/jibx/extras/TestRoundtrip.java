/*
Copyright (c) 2003, Dennis M. Sosnoski
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;

import org.jibx.runtime.*;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Test program for the JiBX framework. Works with two or three command line
 * arguments: mapped-class, in-file, and out-file (optional, only needed if
 * different from in-file). You can also supply a multiple of three input
 * arguments, in which case each set of three is processed in turn (in this case
 * the out-file is required). Unmarshals documents from files using the binding
 * defined for the mapped class, then marshals them back out using the same
 * bindings and compares the results. In case of a comparison error the output
 * file is left as <i>temp.xml</i>.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public class TestRoundtrip {
    
    protected static boolean runTest(String mname, String bname, String fin,
        String fout) throws IOException, JiBXException, XmlPullParserException {
        
        // look up the mapped class and associated binding factory
        Class mclas;
        try {
            URL[] urls = new URL[] { new File(".").toURL() };
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            if (parent == null) {
                parent = TestMultRoundtrip.class.getClassLoader();
            }
            ClassLoader loader = new URLClassLoader(urls, parent);
            Thread.currentThread().setContextClassLoader(loader);
            mclas = loader.loadClass(mname);
        } catch (ClassNotFoundException ex) {
            System.err.println("Class " + mname + " not found");
            return false;
        }
        IBindingFactory bfact;
        if (bname == null) {
            bfact = BindingDirectory.getFactory(mclas);
        } else {
            bfact = BindingDirectory.getFactory(bname, mclas);
        }
        
        // unmarshal document to construct objects
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        Object obj = uctx.unmarshalDocument(new FileInputStream(fin), null);
        String encoding = ((UnmarshallingContext)uctx).getInputEncoding();
        if (!mclas.isInstance(obj)) {
            System.err.println("Unmarshalled result not expected type");
            return false;
        }
        
        // marshal root object back out to document in memory
        IMarshallingContext mctx = bfact.createMarshallingContext();
        mctx.setIndent(2);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mctx.marshalDocument(obj, "UTF-8", null, bos);
        
        // compare with output document to be matched
        InputStreamReader brdr = new InputStreamReader
            (new ByteArrayInputStream(bos.toByteArray()), "UTF-8");
        InputStreamReader frdr = new InputStreamReader
            (new FileInputStream(fout), encoding);
        DocumentComparator comp = new DocumentComparator(System.err);
        if (comp.compare(frdr, brdr)) {
            return true;
        } else {
            
            // save file before returning failure
            try {
                FileOutputStream fos = new FileOutputStream("temp.xml");
                fos.write(bos.toByteArray());
                fos.close();
            } catch (IOException ex) {
                System.err.println("Error writing to temp.xml: " +
                    ex.getMessage());
            }
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length == 2 || (args.length > 0 && args.length % 3 == 0)) {
            
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
                fin = args[base+1];
                fout = (args.length < base+3) ? fin : args[base+2];
                try {
                    if (!runTest(args[base], null, fin, fout)) {
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
                base += 3;
            }
            
        } else {
            System.err.println("Usage: java TestRoundtrip mapped-class" +
                " in-file [out-file]\n where out-file is only required if the" +
                " output document is different from\nthe input document. " +
                "Leaves output as temp.xml in case of error");
            System.exit(1);
        }
    }
}


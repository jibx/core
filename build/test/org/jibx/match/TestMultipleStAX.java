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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jibx.extras.*;
import org.jibx.runtime.*;
import org.jibx.runtime.impl.StAXWriter;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Test program for the JiBX framework. Works with sets of five input
 * parameters: mapped-class, in-binding, in-file, out-binding, compare-file.
 * Unmarshals documents from files using the specified in-bindings for the
 * specified classes, then marshals them back out using the same or different
 * bindings and compares the results with match files. In case of a comparison
 * error the output file is left as <i>temp.xml</i>.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public class TestMultipleStAX {
    
    protected static boolean runTest(String mname, String bin, String fin,
        String bout, String cout)
        throws IOException, JiBXException, XmlPullParserException {
        
        // look up the mapped class
        Class mclas;
        try {
            mclas = TestMultipleStAX.class.getClassLoader().loadClass(mname);
        } catch (ClassNotFoundException ex) {
            System.err.println("Class " + mname + " not found");
            return false;
        }
        
        // get binding factories for the named bindings
        IBindingFactory ibf = BindingDirectory.getFactory(bin, mclas);
        IBindingFactory obf = BindingDirectory.getFactory(bout, mclas);
        
        // unmarshal document to construct bean
        IUnmarshallingContext uctx = ibf.createUnmarshallingContext();
        Object obj = uctx.unmarshalDocument(new FileInputStream(fin), null);
        if (!mclas.isInstance(obj)) {
            System.err.println("Unmarshalled result not expected type");
            return false;
        }
        
        // marshal bean back out to document in memory
        IMarshallingContext mctx = obf.createMarshallingContext();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            XMLOutputFactory ofact = XMLOutputFactory.newInstance();
            XMLStreamWriter wrtr = ofact.createXMLStreamWriter(bos, "UTF-8");
            mctx.setXmlWriter(new StAXWriter(obf.getNamespaces(), wrtr));
            mctx.marshalDocument(obj);
        } catch (XMLStreamException e) {
            throw new JiBXException("Error creating writer", e);
        }
        
        // compare with expected result document
        InputStreamReader brdr = new InputStreamReader
            (new ByteArrayInputStream(bos.toByteArray()), "UTF-8");
        InputStreamReader frdr = new InputStreamReader
            (new FileInputStream(cout), "UTF-8");
        DocumentComparator comp = new DocumentComparator(System.err);
        if (comp.compare(frdr, brdr)) {
            return true;
        } else {
            
            // save file before returning failure
            try {
                File fout = new File("temp.xml");
                fout.delete();
                FileOutputStream fos = new FileOutputStream(fout);
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
        if (args.length >= 5 && args.length % 5 == 0) {
            
            // delete generated output file if present
            File temp = new File("temp.xml");
            if (temp.exists()) {
                temp.delete();
            }
            
            // process each set of four arguments
            boolean err = false;
            int base = 0;
            for (; base < args.length; base += 5) {
                try {
                    if (!runTest(args[base], args[base+1], args[base+2],
                        args[base+3], args[base+4])) {
                        err = true;
                        break;
                    }
                } catch (Exception ex) {
                    System.err.println("Exception: " + ex.getMessage());
                    ex.printStackTrace();
                    err = true;
                    break;
                }
            }
            
            // take error exit if difference found
            if (err) {
                System.err.println("Error on argument set: " +
                    args[base] + ", " + args[base+1] + ", " +
                    args[base+2] + ", " + args[base+3] + ", " + args[base+4]);
                System.exit(1);
            }
            
        } else {
            System.err.println("Requires arguments in sets of five:\n" +
                " mapped-class in-binding in-file out-binding compare-file\n" +
                "Leaves output as temp.xml in case of error");
            System.exit(1);
        }
    }
}
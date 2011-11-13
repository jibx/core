/*
Copyright (c) 2003-2004, Dennis M. Sosnoski
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

import org.jibx.extras.*;
import org.jibx.runtime.*;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Test runner for the JiBX framework. This has a single static method, which
 * runs a test using a mapped class to unmarshal an input file and then
 * compares the result to an match file. In case of a comparison error the
 * output file is left as <i>temp.xml</i>. This is a separate class to allow
 * easy use with a custom classloader.
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class TestRunner
{
    private TestRunner() {}
    
    public static Boolean runTest(String mname, String fin, String fcomp,
        boolean save)
        throws IOException, JiBXException, XmlPullParserException {
        
        // look up the mapped class and associated binding factory
        Class mclas;
        try {
            mclas = TestSimple.class.getClassLoader().loadClass(mname);
        } catch (ClassNotFoundException ex) {
            System.err.println("Class " + mname + " not found");
            return Boolean.FALSE;
        }
        IBindingFactory bfact = BindingDirectory.getFactory(mclas);
        
        // unmarshal document to construct objects
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        Object obj = uctx.unmarshalDocument(new FileInputStream(fin), fin, null);
        if (!mclas.isInstance(obj)) {
            System.err.println("Unmarshalled result not expected type");
            return Boolean.FALSE;
        }
        
        // determine encoding of input document
        String enc = ((UnmarshallingContext)uctx).getInputEncoding();
        
        // marshal root object back out to document in memory
        IMarshallingContext mctx = bfact.createMarshallingContext();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mctx.setIndent(2);
        mctx.marshalDocument(obj, enc, null, bos);
        
        // compare with match document
        InputStreamReader brdr = new InputStreamReader
            (new ByteArrayInputStream(bos.toByteArray()), enc);
        InputStreamReader frdr = new InputStreamReader
            (new FileInputStream(fcomp), enc);
        DocumentComparator comp = new DocumentComparator(System.err);
        boolean match = comp.compare(frdr, brdr);
        if (!match || save) {
            
            // save file before returning
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
        }
        return match ? Boolean.TRUE : Boolean.FALSE;
    }
    
    public static Boolean runTest(String mname, String fin, String fcomp)
        throws IOException, JiBXException, XmlPullParserException {
        return runTest(mname, fin, fcomp, false);
    }
}

/*
Copyright (c) 2003-2004, Dennis M. Sosnoski
Copyright (c) 2025, Daniel Kruegler
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

import simple.StringHolder;

import org.jibx.extras.*;
import org.jibx.runtime.*;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * Test for issue <a href="https://github.com/jibx/core/issues/17">#17</a>
 * 
 * @author Daniel Kruegler
 */
public class TestStringHolder {
    
    protected static boolean runTest() throws Exception {
    	String fin = "stringholder.xml";
    	// The following are character sequences from the Unicode supplementary plane,
    	// more specifically: U+1f427 U+10402 U+20000:
		String value = "\ud83d\udc27\uD801\uDC02\uD840\uDC00";
		StringHolder obj = new StringHolder();
		obj.property.setValue(value);
        
        IBindingFactory bfact = BindingDirectory.getFactory(obj.getClass());
        
        String enc = "UTF-8";
        
        // marshal root object back out to document in memory
        IMarshallingContext mctx = bfact.createMarshallingContext();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mctx.setIndent(2);
        mctx.marshalDocument(obj, enc, null, bos);

        // unmarshal document to construct objects
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        Object objRead = uctx.unmarshalDocument(new FileInputStream(fin), null);
        if (!(objRead instanceof StringHolder)) {
            System.err.println("Unmarshalled result not expected type");
            return false;
        }
        
        StringHolder shRead = (StringHolder) objRead;
        if (!value.equals(shRead.property.getValue())) {
            System.err.println("Unmarshalled StringHolder not expected content");
            return false;
        }
        
        // compare with original input document
        InputStreamReader brdr = new InputStreamReader
            (new ByteArrayInputStream(bos.toByteArray()), enc);
        InputStreamReader frdr = new InputStreamReader
            (new FileInputStream(fin), enc);
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
        if (args.length == 0) {

            // delete generated output file if present
            File temp = new File("temp.xml");
            if (temp.exists()) {
                temp.delete();
            }
            
        	// process each set of two arguments
            boolean err = false;
            try {
                if (!runTest()) {
                    err = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
//              System.err.println(ex.getMessage());
                err = true;
            }
            
            // take error exit if difference found
            if (err) {
                System.err.println("Error: Test failed");
                System.exit(1);
            }
            
        } else {
            System.err.println("Requires no arguments:\n" +
                " mapped-class in-file\n" +
                "Leaves output as temp.xml in case of error");
            System.exit(1);
        }
    }
}

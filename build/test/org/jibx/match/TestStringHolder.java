/*
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

import java.io.ByteArrayOutputStream;

import simple.StringHolder;

import org.jibx.extras.*;
import org.jibx.runtime.*;

/**
 * Test for issue <a href="https://github.com/jibx/core/issues/17">#17</a>
 * 
 * @author Daniel Kruegler
 */
public class TestStringHolder {
    
    protected static boolean runTest() throws Exception {
    	// The following are character sequences from the Unicode supplementary plane:
		String value = "\ud83d\udc27\uD801\uDC02\uD840\uDC00";
		StringHolder obj = new StringHolder();
		obj.property.setValue(value);
        
        IBindingFactory bfact = BindingDirectory.getFactory(obj.getClass());
        
        // marshal root object back out to document in memory
        IMarshallingContext mctx = bfact.createMarshallingContext();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mctx.setIndent(2);
        mctx.marshalDocument(obj, "UTF-8", null, bos);
        return true;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
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

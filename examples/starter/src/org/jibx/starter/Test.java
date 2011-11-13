
package org.jibx.starter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;


public class Test
{
    /**
     * Unmarshal the sample document from a file, then marshal it back out to
     * another file.
     */
	public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -cp ... " +
                "org.jibx.starter.Test in-file out-file");
            System.exit(0);
        }
		try {
			
            // note that you can use multiple bindings with the same class, in
            //  which case you need to use the getFactory() call that takes the
            //  binding name as the first parameter
            IBindingFactory bfact = BindingDirectory.getFactory(Customer.class);
            
            // unmarshal customer information from file
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            FileInputStream in = new FileInputStream(args[0]);
            Customer customer = (Customer)uctx.unmarshalDocument(in, null);
            
            // you can add code here to alter the unmarshalled customer
            
			// marshal object back out to file (with nice indentation, as UTF-8)
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			FileOutputStream out = new FileOutputStream(args[1]);
			mctx.marshalDocument(customer, "UTF-8", null, out);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            System.exit(1);
		} catch (JiBXException e) {
			e.printStackTrace();
            System.exit(1);
		}
	}
}
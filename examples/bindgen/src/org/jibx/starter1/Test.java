
package org.jibx.starter1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;

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
     * 
     * @param args 
     */
	public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -cp ... " +
                "org.jibx.starter1.Test in-file out-file");
            System.exit(0);
        }
		try {
            
            // unmarshal customer information from file
            IBindingFactory bfact = BindingDirectory.getFactory(Order.class);
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            FileInputStream in = new FileInputStream(args[0]);
            Order order = (Order)uctx.unmarshalDocument(in, null);
            
            // compute the total amount of the order
            float total = 0.0f;
            for (Iterator<Item> iter = order.getItems().iterator(); iter.hasNext();) {
                Item item = iter.next();
                total += item.getPrice() * item.getQuantity();
            }
            order.setTotal(new Float(total));
            
			// marshal object back out to file (with nice indentation, as UTF-8)
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			FileOutputStream out = new FileOutputStream(args[1]);
			mctx.setOutput(out, null);
			mctx.marshalDocument(order);
			System.out.println("Unmarshalled and marshalled order with " + 
			    order.getItems().size() + " items and total value " + total);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            System.exit(1);
		} catch (JiBXException e) {
			e.printStackTrace();
            System.exit(1);
		}
	}
}
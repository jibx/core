package org.jibx.starter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jibx.extras.DocumentComparator;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;

public class Test
{
    /**
     * Roundtrip all files in a directory of sample documents.
     * 
     * @param root
     * @return <code>true</code> if all successful, <code>false</code> if any error
     */
    private static boolean processDirectory(File root, IBindingFactory factory) {
        
        // process all the files in this directory
        File[] files = root.listFiles();
        boolean success = true;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory()) {
                try {
                    
                    // unmarshal document into object
                    IUnmarshallingContext uctx = factory.createUnmarshallingContext();
                    uctx.setDocument(new FileInputStream(file), null);
                    Object object = uctx.unmarshalElement();
                    String encoding = ((UnmarshallingContext)uctx).getInputEncoding();
                    
                    // marshal object back out to document in memory
                    IMarshallingContext mctx = factory.createMarshallingContext();
                    mctx.setIndent(2);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mctx.setOutput(bos, "UTF-8");
                    ((IMarshallable)object).marshal(mctx);
                    mctx.endDocument();
                    
                    // compare with original input document
                    InputStreamReader brdr = new InputStreamReader
                        (new ByteArrayInputStream(bos.toByteArray()), "UTF-8");
                    InputStreamReader frdr = new InputStreamReader(new FileInputStream(file), encoding);
                    DocumentComparator comp = new DocumentComparator(System.err, true);
                    System.out.println("Comparing round-tripped file '" + file.getPath() + "' with original file");
                    if (comp.compare(frdr, brdr)) {
                        System.out.println("Successfully round-tripped file '" + file.getPath() + '\'');
                    } else {
                        
                        // save output for comparison failure
                        try {
                            FileOutputStream fos = new FileOutputStream(file.getName());
                            fos.write(bos.toByteArray());
                            fos.close();
                        } catch (IOException e) {
                            System.err.println("Error writing to file '" + file.getName() + "': " + e.getMessage());
                            success = false;
                        }
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error on file '" + file.getPath() + '\'');
                    e.printStackTrace();
                    success = false;
                }
            }
        }
        return success;
    }
    
    /**
     * Processes a directory of sample documents, unmarshalling each document and then marshalling it back out and
     * comparing the result with the original input document.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -cp ... " + "org.jibx.starter.Test binding-package directory-path");
            System.exit(1);
        }
        
        // setup the directory path
        File root = new File(args[1]);
        if (!root.isDirectory()) {
            System.err.println("Root directory '" + args[1] + "' not found");
            System.exit(2);
        }
        
        // get the binding factory
        IBindingFactory factory = null;
        try {
            factory = BindingDirectory.getFactory("binding", args[0]);
        } catch (JiBXException e) {
            System.err.println("Binding factory not found - did you remember to compile the binding?");
            e.printStackTrace();
            System.exit(3);
        }
        
        // process all the data files
        if (!processDirectory(root, factory)) {
            System.exit(4);
        }
    }
}
/*
Copyright (c) 2007-2009, Dennis M. Sosnoski.
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

package org.jibx.binding.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.jibx.binding.Loader;
import org.jibx.binding.Utility;
import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.MungedClass;
import org.jibx.binding.def.BindingDefinition;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.schema.validation.ValidationProblem;

/**
 * Test code for class handling.
 */
public class CustomizationTestBase extends TestCase
{
    private static final String ROOT_CLASS = "org.jibx.custom.classes.GlobalCustom";
    private static final String BINDING_FILE = "class-customs-binding.xml";
    private static final String BINDING_NAME = "class_customs_binding";
    
    protected static final IBindingFactory m_bindingFactory;
    static {
        ClassLoader loader;
        try {
            
            // set paths to be used for loading referenced classes
            loader = CustomizationTestBase.class.getClassLoader();
            URL[] urls = Loader.getClassPaths();
            String[] paths = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                paths[i] = urls[i].getFile();
            }
            ClassCache.setPaths(paths);
            ClassFile.setPaths(paths);
            
        } catch (MalformedURLException e) {
            throw new RuntimeException("Internal error - unable to set classpaths");
        }
        IBindingFactory factory = null;
        try {
            
            // look up the mapped class and existing binding factory
            Class mclas = Class.forName(ROOT_CLASS);
            factory = BindingDirectory.getFactory(BINDING_NAME, mclas);
            
        } catch (Exception e) { /* just fall through */ }
        if (factory == null) {
            try {
                
                // find the binding definition
                InputStream is = loader.getResourceAsStream(BINDING_FILE);
                if (is == null) {
                    throw new RuntimeException("Customizations binding definition not found");
                }
                
                // process the binding
                BoundClass.reset();
                MungedClass.reset();
                BindingDefinition.reset();
                BindingDefinition def = Utility.loadBinding(BINDING_FILE, BINDING_NAME, is, null, true);
                def.generateCode(false, false);
                
                // finish binding factory with information on classes used
                ClassFile[][] lists = MungedClass.fixDispositions();
                def.addClassList(lists[0], lists[1]);
                
                // output the modified class files
                MungedClass.writeChanges();
                
                // look up the mapped class and associated binding factory
                Class mclas = Class.forName(ROOT_CLASS);
                factory = BindingDirectory.getFactory(BINDING_NAME, mclas);
                
            } catch (JiBXException e) {
                e.printStackTrace();
                throw new RuntimeException("JiBXException: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException("IOException: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("ClassNotFoundException: " +
                    e.getMessage());
            }
        }
        m_bindingFactory = factory;
    }
    
    /**
     * Read a customization into model from input stream.
     * 
     * @param is input stream
     * @return root element
     * @throws Exception 
     */
    protected GlobalCustom readCustom(InputStream is) throws Exception {
        IUnmarshallingContext ictx = m_bindingFactory.createUnmarshallingContext();
        ValidationContext vctx = new ValidationContext();
        ictx.setDocument(is, null);
        ictx.setUserContext(vctx);
        GlobalCustom custom = new GlobalCustom();
        ((IUnmarshallable)custom).unmarshal(ictx);
        List problems = vctx.getProblems();
        if (problems.size() > 0) {
            StringBuffer buff = new StringBuffer();
            for (int i = 0; i < problems.size(); i++) {
                ValidationProblem prob = (ValidationProblem)problems.get(i);
                buff.append(prob.getSeverity() >=
                    ValidationProblem.ERROR_LEVEL ? "Error: " : "Warning: ");
                buff.append(prob.getDescription());
                buff.append('\n');
            }
            fail(buff.toString());
        }
        custom.fillClasses();
        return custom;
    }
    
    /**
     * Read a customization into model from string.
     * 
     * @param text customization document text
     * @return root element
     * @throws Exception 
     */
    protected GlobalCustom readCustom(String text) throws Exception {
        return readCustom(new ByteArrayInputStream(text.getBytes("utf-8")));
    }
}
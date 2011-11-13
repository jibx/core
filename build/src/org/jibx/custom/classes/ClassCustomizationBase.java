/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.custom.classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jibx.binding.Utility;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.custom.CustomizationCommandLineBase;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.schema.validation.ValidationProblem;
import org.jibx.util.IClassLocator;

/**
 * Command line processor for customizable tools working with Java classes.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class ClassCustomizationBase extends CustomizationCommandLineBase
{
    /** Ordered array of usage lines. */
    private static final String[] BASE_USAGE_LINES =
        new String[] { " -p path,... class loading paths",
            " -s path,... source paths" };
    
    /** List of class paths. */
    private List m_classPaths;
    
    /** List of source paths. */
    private List m_sourcePaths;
    
    /**
     * Constructor.
     * 
     * @param lines
     */
    protected ClassCustomizationBase(String[] lines) {
        super(mergeUsageLines(lines, BASE_USAGE_LINES));
        m_classPaths = new ArrayList();
        m_sourcePaths = new ArrayList();
    }
    
    /**
     * Split items from a comma-delimited list.
     *
     * @param text comma-delimited list
     * @param values target list of item values
     */
    protected static void splitItems(String text, List values) {
        int base = 0;
        int split;
        while ((split = text.indexOf(',', base)) >= 0) {
            values.add(text.substring(base, split));
            base = split + 1;
        }
        values.add(text.substring(base));
    }
    
    /**
     * Check if an extension parameter is recognized. Subclasses which override this method should call the base class
     * method before doing their own checks, and only perform their own checks if this method returns
     * <code>false</code>..
     * 
     * @param alist argument list
     * @return <code>true</code> if parameter processed, <code>false</code> if unknown
     */
    protected boolean checkParameter(ArgList alist) {
        boolean match = true;
        String arg = alist.current();
        if ("-p".equalsIgnoreCase(arg)) {
            splitItems(alist.next(), m_classPaths);
        } else if ("-s".equalsIgnoreCase(arg)) {
            splitItems(alist.next(), m_sourcePaths);
        } else {
            match = super.checkParameter(alist);
        }
        return match;
    }

    /**
     * Finish processing of command line parameters. This adds the JVM classpath directories to the set of paths
     * specified on the command line. Subclasses which override this method need to call this base class implementation
     * as part of their processing.
     * 
     * @param alist 
     */
    protected void finishParameters(ArgList alist) {
        
        // add JVM class path directories to those specified on command line
        String[] vmpaths = Utility.getClassPaths();
        for (int i = 0; i < vmpaths.length; i++) {
            m_classPaths.add(vmpaths[i]);
        }
        
        // set paths to be used for loading referenced classes
        String[] parray = (String[])m_classPaths.toArray(new String[m_classPaths.size()]);
        ClassCache.setPaths(parray);
        ClassFile.setPaths(parray);
    }
    
    /**
     * Print any extension details. This method may be overridden by subclasses to print extension parameter values for
     * verbose output, but the base class implementation should be called first.
     */
    protected void verboseDetails() {
        System.out.println("Using class loading paths:");
        for (int i = 0; i < m_classPaths.size(); i++) {
            System.out.println(" " + m_classPaths.get(i));
        }
        System.out.println("Using source loading paths:");
        for (int i = 0; i < m_sourcePaths.size(); i++) {
            System.out.println(" " + m_sourcePaths.get(i));
        }
        System.out.println("Starting from classes:");
        List types = getExtraArgs();
        for (int i = 0; i < types.size(); i++) {
            System.out.println(" " + types.get(i));
        }
    }
    
    /**
     * Load the customizations file. This method must load the specified customizations file, or create a default
     * customizations instance, of the appropriate type.
     *
     * @param path customization file path
     * @return <code>true</code> if successful, <code>false</code> if an error
     * @throws JiBXException 
     * @throws IOException 
     */
    protected boolean loadCustomizations(String path) throws JiBXException, IOException {
        
        // load customizations and check for errors
        String[] spaths = (String[])m_sourcePaths.toArray(new String[m_sourcePaths.size()]);
        ValidationContext vctx = new ValidationContext();
        loadCustomizations(path, new ClassSourceLocator(spaths), vctx);
        ArrayList probs = vctx.getProblems();
        if (probs.size() > 0) {
            for (int i = 0; i < probs.size(); i++) {
                ValidationProblem prob = (ValidationProblem)probs.get(i);
                System.out.print(prob.getSeverity() >=
                    ValidationProblem.ERROR_LEVEL ? "Error: " : "Warning: ");
                System.out.println(prob.getDescription());
            }
            if (vctx.getErrorCount() > 0 || vctx.getFatalCount() > 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Load the customizations file. This method must load the specified customizations file, or create a default
     * customizations instance, of the appropriate type.
     * 
     * @param path customizations file path, <code>null</code> if none
     * @param loc class locator
     * @param vctx validation context
     * @throws JiBXException
     * @throws IOException
     */
    protected abstract void loadCustomizations(String path, IClassLocator loc, ValidationContext vctx)
        throws JiBXException, IOException;
}
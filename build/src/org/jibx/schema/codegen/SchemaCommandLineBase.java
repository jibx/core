/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jibx.custom.CustomizationCommandLineBase;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.codegen.custom.SchemasetCustom;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.schema.validation.ValidationProblem;
import org.jibx.util.ReflectionUtilities;

/**
 * Command line processing for tools working with schemas.
 * TODO: take schema root directory handling from CodeGenCommandLine
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class SchemaCommandLineBase extends CustomizationCommandLineBase
{
    /** Ordered array of extra usage lines. */
    private static final String[] EXTRA_USAGE_LINES = new String[] {
        " -s       schema root directory path" };
    
    /** Schema root URL path. */
    private String m_rootPath;
    
    /** Root URL for schemas. */
    private URL m_schemaRoot;
    
    /** Root directory for schemas (<code>null</code> if not a file system root). */
    private File m_schemaDir;
    
    /** Customizations model root. */
    private SchemasetCustom m_customRoot;
    
    /**
     * Constructor.
     */
    public SchemaCommandLineBase() {
        super(EXTRA_USAGE_LINES);
    }
    
    /**
     * Get root URL for schemas.
     *
     * @return directory
     */
    public URL getSchemaRoot() {
        return m_schemaRoot;
    }
    
    /**
     * Get root directory for schemas.
     *
     * @return directory (<code>null</code> if root is not a directory)
     */
    public File getSchemaDir() {
        return m_schemaDir;
    }
    
    /**
     * Get customizations model root.
     * 
     * @return customizations
     */
    public SchemasetCustom getCustomRoot() {
        return m_customRoot;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#checkParameter(org.jibx.binding.generator.CustomizationCommandLineBase.ArgList)
     */
    protected boolean checkParameter(ArgList alist) {
        boolean match = true;
        String arg = alist.current();
        if ("-s".equalsIgnoreCase(arg)) {
            m_rootPath = alist.next();
        } else {
            match = super.checkParameter(alist);
        }
        return match;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#verboseDetails
     */
    protected void verboseDetails() {
        System.out.println("Starting from schemas:");
        List schemas = getExtraArgs();
        for (int i = 0; i < schemas.size(); i++) {
            System.out.println(" " + schemas.get(i));
        }
    }

    /**
     * Finish processing of command line parameters. This just sets up the schema directory.
     * 
     * @param alist 
     */
    protected void finishParameters(ArgList alist) {
        super.finishParameters(alist);
        try {
            if (m_rootPath == null) {
                m_schemaRoot = new File(".").toURI().toURL();
                m_schemaDir = new File(".");
            } else {
                String path = m_rootPath;
                File pathfile = new File(path);
                if (pathfile.exists()) {
                    m_schemaDir = pathfile;
                    m_schemaRoot = pathfile.toURI().toURL();
                } else {
                    if (!path.endsWith("/")) {
                        path += '/';
                    }
                    m_schemaRoot = new URL(path);
                    if (m_schemaRoot.getProtocol().equals("file")) {
                        m_schemaDir = new File(m_schemaRoot.getPath());
                    }
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Root path '" + m_rootPath + "' not found as file and not recognized as URL");
            alist.setValid(false);
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
        ValidationContext vctx = new ValidationContext();
        m_customRoot = new SchemasetCustom((SchemasetCustom)null);
        if (path != null) {
            IBindingFactory fact = BindingDirectory.getFactory(SchemasetCustom.class);
            IUnmarshallingContext ictx = fact.createUnmarshallingContext();
            FileInputStream is = new FileInputStream(path);
            ictx.setDocument(is, null);
            ictx.setUserContext(vctx);
            ((IUnmarshallable)m_customRoot).unmarshal(ictx);
        }
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
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#applyOverrides(Map)
     */
    protected Map applyOverrides(Map overmap) {
        return ReflectionUtilities.applyKeyValueMap(overmap, m_customRoot);
    }
}
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

package org.jibx.schema.codegen;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jibx.custom.CustomizationCommandLineBase;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.codegen.custom.SchemasetCustom;
import org.jibx.schema.validation.ProblemConsoleLister;
import org.jibx.schema.validation.ProblemLogLister;
import org.jibx.schema.validation.ProblemMultiHandler;
import org.jibx.util.ReflectionUtilities;

/**
 * Command line processing specifically for the {@link CodeGen} class.
 * 
 * @author Dennis M. Sosnoski
 */
public class CodeGenCommandLine extends CustomizationCommandLineBase
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(CodeGenCommandLine.class.getName());
    
    /** Ordered array of extra usage lines. */
    private static final String[] EXTRA_USAGE_LINES = new String[] {
        " -b name      generated root binding name",
        " -d file      data model class structure for difference comparison",
        " -i path,...  include existing bindings (one or more), and use for matching\n" +
        "              schema global definitions",
        " -m file      file for dumping the generated data model class structure",
        " -n pack      default package for no-namespace schema definitions",
        " -p pack      default package for all schema definitions",
        " -s path      schema root directory path",
        " -u uri       namespace applied for code generation when no-namespaced schemas\n" +
        "              are found"};
    
    /** Default package for no-namespace schemas. */
    private String m_nonamespacePackage;
    
    /** Default package for all schemas. */
    private String m_defaultPackage;
    
    /** Schema root path. */
    private String m_rootPath;
    
    /** Name used for root binding. */
    private String m_bindingName;
    
    /** Namespace to be used for no-namespace schemas generated directly. */
    private String m_usingNamespace;
    
    /** Root URL for schemas. */
    private URL m_schemaRoot;
    
    /** Root directory for schemas (<code>null</code> if not a file system root). */
    private File m_schemaDir;
    
    /** File for dumping the generated class structure (<code>null</code> if none). */
    private File m_modelFile;
    
    /** File for checking differences in generated class structure (<code>null</code> if none). */
    private File m_differenceFile;
    
    /** Customizations model root. */
    private SchemasetCustom m_customRoot;
    
    /** List of existing bindings to be included and used for matching schema definitions. */
    private List m_includePaths;
    
    /**
     * Constructor.
     */
    public CodeGenCommandLine() {
        super(EXTRA_USAGE_LINES);
        m_includePaths = new ArrayList();
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
     * Get binding name.
     * 
     * @return name (<code>null</code> if not set)
     */
    public String getBindingName() {
        return m_bindingName;
    }
    
    /**
     * Get namespace to be used when no schemas with namespaces are being generated.
     * 
     * @return namespace URI (<code>null</code> if unspecified)
     */
    public String getUsingNamespace() {
        return m_usingNamespace;
    }
    
    /**
     * Get customizations model root.
     * 
     * @return customizations
     */
    public SchemasetCustom getCustomRoot() {
        return m_customRoot;
    }
    
    /**
     * Get default package for no-namespace schemas.
     *
     * @return package (<code>null</code> if not set)
     */
    public String getNonamespacePackage() {
        return m_nonamespacePackage;
    }
    
    /**
     * Get file to be used for dumping generated data model.
     *
     * @return dump file (<code>null</code> if none)
     */
    public File getModelFile() {
        return m_modelFile;
    }
    
    /**
     * Get file to be used for finding differences in generated data model.
     *
     * @return difference file (<code>null</code> if none)
     */
    public File getDifferenceFile() {
        return m_differenceFile;
    }
    
    /**
     * Get the list of paths for bindings to be used for matching schema definitions.
     *
     * @return paths (empty if no paths specified)
     */
    public List getIncludePaths() {
        return m_includePaths;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#checkParameter(org.jibx.binding.generator.CustomizationCommandLineBase.ArgList)
     */
    protected boolean checkParameter(ArgList alist) {
        boolean match = true;
        String arg = alist.current();
        if ("-b".equalsIgnoreCase(arg)) {
            m_bindingName = alist.next();
        } else if ("-d".equalsIgnoreCase(arg)) {
            m_differenceFile = new File(alist.next());
        } else if ("-i".equalsIgnoreCase(arg)) {
            String text = alist.next();
            int split;
            int base = 0;
            while ((split = text.indexOf(',', base)) >= 0) {
                m_includePaths.add(text.substring(base, split));
                base = split + 1;
            }
            m_includePaths.add(text.substring(base));
        } else if ("-m".equalsIgnoreCase(arg)) {
            m_modelFile = new File(alist.next());
        } else if ("-n".equalsIgnoreCase(arg)) {
            m_nonamespacePackage = alist.next();
        } else if ("-p".equalsIgnoreCase(arg)) {
            m_defaultPackage = alist.next();
        } else if ("-s".equalsIgnoreCase(arg)) {
            m_rootPath = alist.next();
        } else if ("-u".equalsIgnoreCase(arg)) {
            m_usingNamespace = alist.next();
        } else {
            match = super.checkParameter(alist);
        }
        return match;
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
                m_schemaDir = new File(".").getCanonicalFile();
                m_schemaRoot = m_schemaDir.toURI().toURL();
            } else {
                String path = m_rootPath;
                File pathfile = new File(path).getCanonicalFile();
                if (pathfile.exists()) {
                    if (pathfile.isDirectory()) {
                        m_schemaDir = pathfile;
                        m_schemaRoot = pathfile.toURI().toURL();
                    } else {
                        System.out.println("Schema root path '" + m_rootPath + "' must be a directory");
                        alist.setValid(false);
                    }
                } else {
	                if (!path.endsWith("/")) {
	                    path += '/';
	                }
                    m_schemaRoot = new URL(path);
	                if (m_schemaRoot.getProtocol().equals("file")) {
	                    m_schemaDir = new File(m_schemaRoot.getPath()).getCanonicalFile();
	                }
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Root path '" + m_rootPath + "' not found as file and not recognized as URL");
            alist.setValid(false);
        } catch (IOException e) {
            System.out.println("Error processing root path '" + m_rootPath + '\'');
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
        ProblemMultiHandler handler = new ProblemMultiHandler();
        handler.addHandler(new ProblemConsoleLister());
        handler.addHandler(new ProblemLogLister(s_logger));
        m_customRoot = SchemasetCustom.loadCustomizations(path, handler);
        
        // set specified default package on root customization element
        if (m_defaultPackage != null) {
            m_customRoot.setPackage(m_defaultPackage);
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
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#printUsage()
     */
    public void printUsage() {
        System.out.println("\nUsage: java org.jibx.schema.codegen.CodeGen "
            + "[options] schema1 schema2 ...\nwhere options are:");
        String[] usages = getUsageLines();
        for (int i = 0; i < usages.length; i++) {
            System.out.println(usages[i]);
        }
        System.out.println("The schema# files are different schemas to be included in "
            + "the generation\n(references from these schemas will also be included).\n");
    }
}
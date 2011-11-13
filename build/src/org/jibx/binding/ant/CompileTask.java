/*
Copyright (c) 2003, Andrew J. Glover
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
package org.jibx.binding.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.jibx.binding.Compile;
import org.jibx.runtime.JiBXException;

/**
 * Class alters user defined class files for JiBX functionality.  
 * @author aglover
 * @author pledbrook
 */
public class CompileTask extends Task {
    private boolean m_load;
    private boolean m_verbose;
    private Path m_classpath;
    private List m_fileSet;
    private List m_bindingFileSet;
    private String m_bindingFile;
    
    /**
     * Hook method called by ant framework to handle 
     * task initialization.
     * 
     */
    public void init() throws BuildException {
        //
        // Do aprent initialisation first.
        //
        super.init();
        
        //
        // Now create the lists that store the classpathsets and
        // the bindingsets.
        //
        m_fileSet = new ArrayList();
        m_bindingFileSet = new ArrayList();
    }
    
    /**
     * Returns an array of paths of all the files specified by the
     * <classpath> or <classpathset> tags. Note that the <classpath>
     * and <classpathset> tags cannot both be specified.
     */
    private String[] getPaths() {
        String[] pathArray = null;
        if (m_classpath != null) {
            //
            // If a <classpath> tag has been set, m_classpath will
            // not be null. In this case, just return the array of
            // paths directly.
            //
            pathArray = m_classpath.list();
        } else {
            //
            // Store the directory paths specified by each of the
            // <classpathset> tags.
            //
            pathArray = new String[m_fileSet.size()];
        
            for(int i = 0; i < m_fileSet.size(); i++) {
                FileSet fileSet = (FileSet)m_fileSet.get(i);
                File directory = fileSet.getDir(project);
                pathArray[i] = directory.getAbsolutePath();
            }
        }
        
        // make Ant classpath usable by application
        try {
            System.setProperty("java.class.path",
                ((AntClassLoader)CompileTask.class.getClassLoader()).getClasspath());
        } catch (Exception e) {
            System.err.println("Failed setting classpath from Ant task");
        }
        
        
        //
        // If verbose mode is on, print out the paths we are using.
        //
        if (m_verbose) {
            log("Using the following paths:");
            for (int i = 0; i < pathArray.length; i++) {
                log("  " + pathArray[i]);
            }
        }
        
        return pathArray;
    }
    
    /**
     * Returns an array of all the paths specified by the "binding"
     * attribute or the <bindingfileset> tags.
     */
    private String[] getBindings() {
        String[] bindings = null;
        if (m_bindingFileSet.size() == 0) {
            //
            // No <bindingfileset> tags have been specified, so use
            // the task's "binding" attribute instead.
            //
            bindings = new String[] { m_bindingFile };
        } else {
            //
            // For each fileset, get all the file paths included by it.
            //
            ArrayList paths = new ArrayList();
            for(int i = 0; i < m_bindingFileSet.size(); i++){
                FileSet bPath = (FileSet)m_bindingFileSet.get(i);
                
                DirectoryScanner dirScn = bPath.getDirectoryScanner(project);
                String[] bndingFiles = dirScn.getIncludedFiles();
                
                for(int x = 0; x < bndingFiles.length; x++){
                    String fullPath = dirScn.getBasedir()
                                      + System.getProperty("file.separator")
                                      + bndingFiles[x];
                    paths.add(fullPath);
                }            
            }
            
            //
            // Convert the ArrayList of binding files into a native array.
            //
            bindings = (String[])paths.toArray(new String[paths.size()]);
        }
        
        //
        // If verbose mode is on, print out the binding files
        // we are using.
        //
        if (m_verbose) {
            log("Using the following binding paths:");
            for (int i = 0; i < bindings.length; i++) {
                log("  " + bindings[i]);
            }
        }
            
        return bindings;
    }
    
    /**
     * This checks that the build file has specified the correct
     * mix of &lt;classpath&gt;, &lt;classpathset&gt;, &lt;bindingfileset&gt;
     * and "binding". The rules are as follows:
     * <ul>
     * <li>Either the "binding" attribute <b>or</b> &lt;bindingfileset&gt;
     *     tags must be set. Not both.</li>
     * <li>Either a single &lt;classpath&gt; tag <b>or</b> multiple
     *     &lt;classpathset&gt; tags must be set. Not both.</li>
     * </ul>
     * @throws BuildException
     */
    private void validateRequiredFields() throws BuildException {
        //
        // Check at least one of the binding file properties is set.
        //
        if (m_bindingFileSet.isEmpty() && m_bindingFile == null) {
            throw new BuildException("Either the binding attribute or at "
                                     + "least one bindingset nested element "
                                     + "must be defined.");
        }
        
        //
        // Make sure that the "binding" attribute has not been used with the
        // <bindingset> nested element.
        //
        if (!m_bindingFileSet.isEmpty() && m_bindingFile != null) {
            throw new BuildException("You cannot specify both a binding attribute and a "
                                     + "bindingset nested element.");
        }
        
        //
        // Check either <classpath> or <classpathset> has been set.
        //
        if(m_classpath == null && m_fileSet.isEmpty()) {
            throw new BuildException("You must specify either a classpath "
                                     + "or at least one nested classpathset element.");
        }
        
        //
        // Make sure that only one of the above is specified.
        //
        if (m_classpath != null && !m_fileSet.isEmpty()) {
            throw new BuildException("You cannot specify both a classpath and a "
                                     + "classpathset nested element.");
        }
    }
    
    /**
     * 
     * Hook method called by Ant. Method first determines
     * class file path from user defined value, then 
     * determines all binding files for class file weaving.
     * 
     */
    public void execute() throws BuildException{
        try{    
            validateRequiredFields();
            
            String[] pathArr = getPaths();
            String[] bindings = getBindings();
            
            Compile compiler = new Compile();
            
            //right now there is an issue with dynamically loading
            //class files with ant. it doesnt work unless the class
            //files are part of ANT's classpath. so I am forcing this 
            //to always be false until we can come up with a better solution.
            compiler.setLoad(m_load);
            
            compiler.setVerbose(m_verbose);
            
            compiler.compile(pathArr, bindings);
                    
        } catch(JiBXException jEx) {
            jEx.printStackTrace();
            throw new BuildException("JiBXException in JiBX binding compilation", jEx);
        }
    }
    
    /**
     * 
     * @param fSet
     */    
    public void addClassPathSet(FileSet fSet){
        m_fileSet.add(fSet);
    }
    
    /**
     * Returns the current classpath.
     */
    public Path getClasspath() {
        return m_classpath;
    }
    
    /**
     * Sets the classpath for this task. Multiple calls append the
     * new classpath to the current one, rather than overwriting it.
     * @param classpath The new classpath as a Path object.
     */
    public void setClasspath(Path classpath) {
        if (m_classpath == null) {
            m_classpath = classpath;
        }
        else {
            m_classpath.append(classpath);
        }
    }
    
    /**
     * Creates the classpath for this task and returns
     * it. If the classpath has already been created,
     * the method just returns that one.
     */
    public Path createClasspath() {
        if (m_classpath == null) {
            m_classpath = new Path(project);
        }
        
        return m_classpath;
    }

    /**
     * @param file
     */
    public void setBinding(String file) {
        m_bindingFile = file;
    }
    
    /**
     * 
     * @param bfSet
     */
    public void addBindingFileSet(FileSet bfSet) {
        m_bindingFileSet.add(bfSet);
    }
    
    /**
     * @param bool
     */
    public void setLoad(boolean bool) {
        m_load = bool;
    }
    
    /**
     * @param bool
     */
    public void setVerbose(boolean bool) {
        m_verbose = bool;
    }
}

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

package org.jibx.custom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Command line processor for all types of customizable tools. This just provides the basic handling of a customizations
 * file, target directory, and overrides of values in the customizations root object.
 * TODO: should extend SchemaCommandLinebase
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class CustomizationCommandLineBase
{
    /** Array of method parameter classes for single String parameter. */
    public static final Class[] STRING_PARAMETER_ARRAY = new Class[] { String.class };
    
    /** Array of classes for String and unmarshaller parameters. */
    public static final Class[] STRING_UNMARSHALLER_PARAMETER_ARRAY =
        new Class[] { String.class, IUnmarshallingContext.class };
    
    /** Number of leading characters in usage lines checked for uniqueness. */
    private static final int USAGE_UNIQUE_CHARS = 4;
    
    /** Ordered array of usage lines for parameters at this level. */
    private static final String[] COMMON_USAGE_LINES =
        new String[] {
            " -c path  input customizations file",
            " -t path  target directory for generated output (default is current directory)",
            " -w       wipe all existing files from generation directory (ignored if current\n" +
            "          directory)",
            " -v       verbose output flag" };
    
    /** Complete array of usage lines. */
    private final String[] m_usageLines;
    
    /** List of specified classes or files. */
    private List m_extraArgs;
    
    /** Target directory for output. */
    private File m_generateDirectory;
    
    /** Verbose output flag. */
    private boolean m_verbose;
    
    /**
     * Constructor. This just merges the usage line defined by subclasses with those at this level, and checks for any
     * duplication (by comparing the first n characters of the lines).
     * 
     * @param lines
     */
    protected CustomizationCommandLineBase(String[] lines) {
        m_usageLines = mergeUsageLines(lines, COMMON_USAGE_LINES);
        for (int i = 1; i < m_usageLines.length; i++) {
            String line = m_usageLines[i];
            if (m_usageLines[i-1].startsWith(line.substring(0, USAGE_UNIQUE_CHARS))) {
                throw new IllegalArgumentException("Internal error - duplicate parameter flags");
            }
        }
    }
    
    /**
     * Process command line arguments array.
     * 
     * @param args
     * @return <code>true</code> if valid, <code>false</code> if not
     * @throws JiBXException
     * @throws IOException
     */
    public boolean processArgs(String[] args) throws JiBXException, IOException {
        boolean wipe = false;
        String custom = null;
        String genpath = null;
        Map overrides = new HashMap();
        ArgList alist = new ArgList(args);
        m_extraArgs = new ArrayList();
        while (alist.hasNext()) {
            String arg = alist.next();
            if ("-c".equalsIgnoreCase(arg)) {
                custom = alist.next();
            } else if ("-t".equalsIgnoreCase(arg)) {
                genpath = alist.next();
            } else if ("-v".equalsIgnoreCase(arg)) {
                m_verbose = true;
            } else if ("-w".equalsIgnoreCase(arg)) {
                wipe = true;
            } else if (arg.startsWith("--") && arg.length() > 2 && Character.isLetter(arg.charAt(2))) {
                if (!putKeyValue(arg.substring(2), overrides)) {
                    alist.setValid(false);
                }
            } else if (!checkParameter(alist)) {
                if (arg.startsWith("-")) {
                    System.err.println("Unknown option flag '" + arg + '\'');
                    alist.setValid(false);
                } else {
                    m_extraArgs.add(alist.current());
                    break;
                }
            }
        }
        
        // collect the extra arguments at end
        while (alist.hasNext()) {
            String arg = alist.next();
            if (arg.startsWith("-")) {
                System.err.println("Command line options must precede all other arguments: error on '" + arg + '\'');
                alist.setValid(false);
                break;
            } else {
                m_extraArgs.add(arg);
            }
        }
        
        // check for valid command line arguments
        if (alist.isValid()) {
            
            // set output directory
            if (genpath == null) {
                m_generateDirectory = new File(".");
                wipe = false;
            } else {
                m_generateDirectory = new File(genpath);
            }
            if (m_generateDirectory.exists()) {
                if (!m_generateDirectory.isDirectory()) {
                    System.out.println("Target path '" + genpath + "' must be a directory");
                    alist.setValid(false);
                }
            } else {
                m_generateDirectory.mkdirs();
                wipe = false;
            }
            if (!m_generateDirectory.canWrite()) {
                System.err.println("Target directory " + m_generateDirectory.getPath() + " is not writable");
                alist.setValid(false);
            } else {
                
                // finish the command line processing
                finishParameters(alist);
                
                // report on the configuration
                if (m_verbose) {
                    verboseDetails();
                    System.out.println("Output to directory " + m_generateDirectory);
                }
                
                // clean generate directory if requested
                if (wipe) {
                    CustomUtils.clean(m_generateDirectory);
                }
                
                // load customizations and check for errors
                if (!loadCustomizations(custom)) {
                    alist.setValid(false);
                } else {
                    
                    // apply command line overrides to customizations
                    Map unknowns = applyOverrides(overrides);
                    if (!unknowns.isEmpty()) {
                        for (Iterator iter = unknowns.keySet().iterator(); iter.hasNext();) {
                            String key = (String)iter.next();
                            System.err.println("Unknown override key '" + key + '\'');
                        }
                        alist.setValid(false);
                    }
                    
                }
            }
            
        } else {
            printUsage();
        }
        return alist.isValid();
    }
    
    /**
     * Get generate directory.
     * 
     * @return directory
     */
    public File getGeneratePath() {
        return m_generateDirectory;
    }
    
    /**
     * Get extra arguments from command line. These extra arguments must follow all parameter flags.
     * 
     * @return args
     */
    public List getExtraArgs() {
        return m_extraArgs;
    }
    
    /**
     * Check if verbose output requested.
     *
     * @return verbose
     */
    public boolean isVerbose() {
        return m_verbose;
    }
    
    /**
     * Set a key=value definition in a map. This is a command line processing assist method that prints an error message
     * directly if the expected format is not found.
     * 
     * @param def
     * @param map
     * @return <code>true</code> if successful, <code>false</code> if error
     */
    public static boolean putKeyValue(String def, Map map) {
        int split = def.indexOf('=');
        if (split >= 0) {
            String key = def.substring(0, split);
            if (map.containsKey(key)) {
                System.err.println("Repeated key item: '" + def + '\'');
                return false;
            } else {
                map.put(key, def.substring(split + 1));
                return true;
            }
        } else {
            System.err.println("Missing '=' in expected key=value item: '" + def + '\'');
            return false;
        }
    }
    
    /**
     * Get the usage lines describing command line parameters.
     *
     * @return lines
     */
    protected String[] getUsageLines() {
        return m_usageLines;
    }
    
    /**
     * Merge two arrays of strings, returning an ordered array containing all the strings from both provided arrays.
     * 
     * @param base
     * @param adds
     * @return ordered merged
     */
    protected static String[] mergeUsageLines(String[] base, String[] adds) {
        if (adds.length == 0) {
            return base;
        } else {
            String fulls[] = new String[base.length + adds.length];
            System.arraycopy(base, 0, fulls, 0, base.length);
            System.arraycopy(adds, 0, fulls, base.length, adds.length);
            Arrays.sort(fulls);
            return fulls;
        }
    }
    
    /**
     * Check extension parameter. This method may be overridden by subclasses to process parameters beyond those known
     * to this base class.
     * 
     * @param alist argument list
     * @return <code>true</code> if parameter processed, <code>false</code> if unknown
     */
    protected boolean checkParameter(ArgList alist) {
        return false;
    }
    
    /**
     * Finish processing of command line parameters. This method may be overridden by subclasses to implement any added
     * processing after all the command line parameters have been handled.
     * 
     * @param alist 
     */
    protected void finishParameters(ArgList alist) {}
    
    /**
     * Print any extension details. This method may be overridden by subclasses to print extension parameter values for
     * verbose output.
     */
    protected void verboseDetails() {}
    
    /**
     * Load the customizations file. This method must load the specified customizations file, or create a default
     * customizations instance, of the appropriate type.
     *
     * @param path customization file path, <code>null</code> if none
     * @return <code>true</code> if successful, <code>false</code> if an error
     * @throws JiBXException 
     * @throws IOException 
     */
    protected abstract boolean loadCustomizations(String path) throws JiBXException, IOException;
    
    /**
     * Apply map of override values to customizations read from file or created as default.
     * 
     * @param overmap override key-value map
     * @return map for key/values not recognized
     */
    protected abstract Map applyOverrides(Map overmap);
    
    /**
     * Print usage information.
     */
    public abstract void printUsage();
    
    /**
     * Wrapper class for command line argument list.
     */
    protected static class ArgList
    {
        private int m_offset;
        
        private final String[] m_args;
        
        private boolean m_valid;
        
        /**
         * Constructor.
         * 
         * @param args
         */
        protected ArgList(String[] args) {
            m_offset = -1;
            m_args = args;
            m_valid = true;
        }
        
        /**
         * Check if another argument value is present.
         * 
         * @return <code>true</code> if argument present, <code>false</code> if all processed
         */
        public boolean hasNext() {
            return m_args.length - m_offset > 1;
        }
        
        /**
         * Get current argument value.
         * 
         * @return argument, or <code>null</code> if none
         */
        public String current() {
            return (m_offset >= 0 && m_offset < m_args.length) ? m_args[m_offset] : null;
        }
        
        /**
         * Get next argument value. If this is called with no argument value available it sets the argument list
         * invalid.
         * 
         * @return argument, or <code>null</code> if none
         */
        public String next() {
            if (++m_offset < m_args.length) {
                return m_args[m_offset];
            } else {
                m_valid = false;
                return null;
            }
        }
        
        /**
         * Set valid state.
         * 
         * @param valid
         */
        public void setValid(boolean valid) {
            m_valid = valid;
        }
        
        /**
         * Check if argument list valid.
         * 
         * @return <code>true</code> if valid, <code>false</code> if not
         */
        public boolean isValid() {
            return m_valid;
        }
    }
}
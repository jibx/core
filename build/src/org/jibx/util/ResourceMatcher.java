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

package org.jibx.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Supports finding resources relative to a file system base directory or base URL. When using a file system base
 * directory, the resource paths may include '*' wildcard match characters for the actual file names.
 * 
 * @author Dennis M. Sosnoski
 */
public class ResourceMatcher
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(ResourceMatcher.class.getName());

    /**
     * Find matches to resource paths. The paths may be either absolute, or relative to a file system directory or to a
     * general URL, but the paths may contain '*' wildcard match characters only if relative to a file system directory.
     * An error is reported for each invalid pattern, including wildcard patterns with no matching files and malformed
     * URLs.
     * 
     * @param basedir file system base directory (<code>null</code> if not using file system)
     * @param baseurl base URL for names (<code>null</code> if no base)
     * @param paths resource paths to be found, with wildcards allowed in name portion if using file system
     * @param report match reporting interface
     * @return error message list (empty list if no errors)
     * @throws IOException
     */
    public static List matchPaths(File basedir, URL baseurl, List paths, ReportMatch report)
    throws IOException {
        PatternMatcher filter = new PatternMatcher();
        List errors = new ArrayList();
        for (Iterator iter = paths.iterator(); iter.hasNext();) {
            String path = (String)iter.next();
            int wildstart = path.indexOf('*');
            if (wildstart >= 0) {
                if (basedir == null) {
                    errors.add("File name pattern argument not allowed for non-file base: '" + path + '\'');
                } else {
                    File dir = basedir.getCanonicalFile();
                    if (File.separatorChar != '/') {
                        path = path.replace('/', File.separatorChar);
                    }
                    int split = path.lastIndexOf(File.separatorChar);
                    String pattern = path;
                    if (split >= 0) {
                        if (wildstart < split) {
                            errors.add("Wildcard can only be used for file name, not in the directory path, for pattern '" + path + '\'');
                            continue;
                        } else {
                            String dirpath = path.substring(0, split);
                            dir = new File(dirpath);
                            if (!dir.isAbsolute()) {
                                dir = new File(basedir, dirpath);
                            }
                            dir = dir.getCanonicalFile();
                            pattern = path.substring(split+1);
                        }
                    }
                    filter.setPattern(pattern);
                    s_logger.debug("Matching file names to command line pattern '" + path + '\'');
                    String[] matches = dir.list(filter);
                    if (matches == null || matches.length == 0) {
                        errors.add("No files found matching command line pattern '" + pattern +
                            "' in directory " + dir.getAbsolutePath());
                    } else {
                        for (int i = 0; i < matches.length; i++) {
                            String match = matches[i];
                            report.foundMatch(match, new URL(dir.toURI().toURL(), match));
                        }
                    }
                }
            } else {
                
                // first try for a file, if base directory supplied
                if (basedir != null) {
                    File file = new File(path);
                    if (!file.isAbsolute()) {
                        file = new File(basedir, path);
                    }
                    if (file.exists()) {
                        report.foundMatch(path, file.toURI().toURL());
                        continue;
                    }
                }
                
                // build URL, with support for classpath protocol
                URL url = ClasspathUrlExtender.buildURL(baseurl, path);
                report.foundMatch(path, url);
                
            }
        }
        return errors;
    }

    /**
     * Report matches found to name patterns.
     */
    public interface ReportMatch
    {
        /**
         * Match found for pattern.
         * 
         * @param path
         * @param url
         */
        void foundMatch(String path, URL url);
    }

    /**
     * File name pattern matcher.
     */
    private static class PatternMatcher implements FilenameFilter
    {
        /** Current match pattern. */
        private String m_pattern;
        
        /**
         * Set the match pattern.
         * 
         * @param pattern
         */
        public void setPattern(String pattern) {
            m_pattern = pattern;
        }
        
        /**
         * Check for file name match.
         * 
         * @param dir
         * @param name
         * @return match flag
         */
        public boolean accept(File dir, String name) {
            boolean match = NameUtilities.isPatternMatch(name, m_pattern);
            if (match) {
                s_logger.debug(" matched file name '" + name + '\'');
            }
            return match;
        }
    }
}
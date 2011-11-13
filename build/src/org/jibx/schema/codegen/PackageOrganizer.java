/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski All rights reserved.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jibx.runtime.Utility;
import org.jibx.util.InsertionOrderedMap;

/**
 * Organizer for package information. This handles the conversions from namespace URIs to package names, and organizes
 * the packages in a tree structure.
 * 
 * @author Dennis M. Sosnoski
 */
public class PackageOrganizer
{
    //
    // Instance data
    
    /** Base directory for code generation. */
    private final File m_generateDirectory;
    
    /** Leading URI text to be matched (paired with replacement values). These values are matched using case-insensitive
     comparisons. */
    private String[] m_namespaceLeadMatches;
    
    /** Replacement text for URI matches (paired with leading URI texts). */
    private String[] m_namespaceLeadReplaces;
    
    /** Map from schema namespace URI to package (empty if unused). */
    private Map m_namespacePackageMap;
    
    /** Map from package to base directory for code generation (empty if unused). */
    private Map m_packageDirectoryMap;
    
    /** Array of case-insensitive strings to be discarded from start of authority component of URI when converting to
     package name. The default is the string "www." */
    private String[] m_authorityDiscards = new String[] { "www." };
    
    /** Map from namespace URI to package information. This is used in combination with the name-package map, since
     multiple URIs may be converted to the same package name. */
    private Map m_uriPackageMap;
    
    /** Map from package name to package information. This is used in combination with the URI-package map. */
    private InsertionOrderedMap m_namePackageMap;
    
    /** Package to use for no-namespace schema components. */
    private String m_noNamespacePackage;
    
    /**
     * Constructor.
     * 
     * @param basedir default base directory for code generation
     * @param npkg default package for no-namespace schema components
     */
    public PackageOrganizer(File basedir, String npkg) {
        m_generateDirectory = basedir;
        m_namespaceLeadMatches = Utility.EMPTY_STRING_ARRAY;
        m_namespaceLeadReplaces = Utility.EMPTY_STRING_ARRAY;
        m_namespacePackageMap = Collections.EMPTY_MAP;
        m_packageDirectoryMap = Collections.EMPTY_MAP;
        m_uriPackageMap = new HashMap();
        m_namePackageMap = new InsertionOrderedMap();
        m_noNamespacePackage = npkg;
        m_namePackageMap.put("", new PackageHolder("", basedir, null));
    }
    
    /**
     * Set the namespace lead replacement patterns. This consists of lead texts to be matches and paired replacement
     * texts. When a particular lead text is found in a URI the replacement text is substituted.
     * 
     * @param leads
     * @param repls
     */
    public void setNamespaceLeadReplaces(String[] leads, String[] repls) {
        m_namespaceLeadMatches = leads;
    }
    
    /**
     * Set map from namespace URIs to packages.
     * 
     * @param map String-to-String map
     */
    public void setNSPackageMap(Map map) {
        m_namespacePackageMap = map;
    }
    
    /**
     * Set map from package to base generation directory. If this is unset all packages are generated under the default
     * base directory, as are any packages not covered by this map. All subpackages of a package go under the parent
     * package unless otherwise specified by this map.
     * 
     * @param map String-to-File map
     */
    public void setPackageDirMap(Map map) {
        m_packageDirectoryMap = map;
    }
    
    /**
     * Check if a character is a hex digit.
     * 
     * @param chr
     * @return hex digit flag
     */
    private boolean isHexChar(char chr) {
        return (chr >= '0' && chr <= '9') || (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z');
    }
    
    /**
     * Get value of character as hex digit.
     * 
     * @param chr
     * @return hex digit value
     */
    private int hexValue(char chr) {
        if (chr >= '0' && chr <= '9') {
            return chr - '0';
        } else {
            return Character.toLowerCase(chr) - 'a' + 10;
        }
    }
    
    /**
     * Check if a character is an ASCII alpha character.
     * 
     * @param chr
     * @return alpha character flag
     */
    private static boolean isAsciiAlpha(char chr) {
        return (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z');
    }
    
    /**
     * Check if a character is an ASCII numeric character.
     * 
     * @param chr
     * @return numeric character flag
     */
    private static boolean isAsciiNum(char chr) {
        return chr >= '0' && chr <= '9';
    }
    
    /**
     * Check if a character is an ASCII alpha or numeric character.
     * 
     * @param chr
     * @return alpha or numeric character flag
     */
    private static boolean isAsciiAlphaNum(char chr) {
        return isAsciiAlpha(chr) || isAsciiNum(chr);
    }
    
    /**
     * Convert namespace URI to package name.
     * 
     * @param uri
     * @return package name
     */
    public String uriToPackage(String uri) {
        
        // first check for no-namespace namespace
        if (uri.length() == 0) {
            return m_noNamespacePackage;
        }
        
        // substitute URI lead text matches
        String pname = uri.toLowerCase();
        for (int i = 0; i < m_namespaceLeadMatches.length; i++) {
            String lead = m_namespaceLeadMatches[i];
            if (pname.startsWith(lead)) {
                pname = m_namespaceLeadReplaces[i] + pname.substring(lead.length());
                break;
            }
        }
        
        // force expected path separator characters
        pname = pname.replace('\\', '/');
        
        // discard leading scheme in namespace URI
        int split = pname.indexOf(':');
        if (split > 0 && isAsciiAlpha(pname.charAt(0))) {
            boolean scheme = true;
            for (int i = 0; i < split; i++) {
                char chr = pname.charAt(i);
                if (!isAsciiAlphaNum(chr) && chr != '-' && chr != '+' && chr != '.') {
                    scheme = false;
                    break;
                }
            }
            if (scheme) {
                pname = pname.substring(split + 1);
            }
        }
        
        // delete any port number component
        int base = pname.indexOf(':');
        if (base > 0) {
            int tail;
            for (tail = base + 1; tail < pname.length(); tail++) {
                if (!isAsciiNum(pname.charAt(tail))) {
                    break;
                }
            }
            pname = pname.substring(0, base) + pname.substring(tail);
        }
        
        // preprocess authority, if present, discarding leading matches
        if (pname.startsWith("//")) {
            pname = pname.substring(2);
            for (int i = 0; i < m_authorityDiscards.length; i++) {
                String match = m_authorityDiscards[i];
                if (pname.startsWith(match)) {
                    pname = pname.substring(match.length());
                }
            }
        }
        
        // treat '@' the same as authority (e.g. 'mailto:dms@sosnoski.com')
        pname = pname.replace('@', '.');
        
        // find end of authority (or first directory in path, don't care)
        int end = pname.indexOf('/');
        if (end < 0) {
            end = pname.length();
        }
        
        // reverse order of components in authority
        StringBuffer buff = new StringBuffer(pname.length());
        int mark = end;
        while ((split = pname.lastIndexOf('.', --mark)) >= 0) {
            if (buff.length() > 0) {
                buff.append(pname.substring(split, mark + 1));
            } else {
                buff.append(pname.substring(split + 1, mark + 1));
            }
            mark = split;
        }
        if (mark >= 0) {
            if (buff.length() > 0) {
                buff.append('.');
            }
            buff.append(pname.substring(0, mark + 1));
        }
        pname = pname.substring(end);
        
        // treat colons as path separators
        pname = pname.replace(':', '/');
        
        // delete file extension, if present
        split = pname.lastIndexOf('.');
        if (split > 0 && split > pname.lastIndexOf('/')) {
            pname = pname.substring(0, split);
        }
        buff.append(pname);
        
        // finish by converting path separators and special characters
        int index = 0;
        boolean first = true;
        while (index < buff.length()) {
            char chr = buff.charAt(index);
            boolean delete = false;
            if (chr == '.') {
                if (first) {
                    delete = true;
                } else {
                    first = true;
                }
            } else if (chr == '/') {
                if (first) {
                    delete = true;
                } else {
                    buff.setCharAt(index, '.');
                    first = true;
                }
            } else if (chr == '+') {
                buff.setCharAt(index, '_');
                first = false;
            } else if (chr == '%') {
                if (index + 2 < buff.length()) {
                    char chr1 = buff.charAt(index + 1);
                    char chr2 = buff.charAt(index + 2);
                    if (isHexChar(chr1) && isHexChar(chr2)) {
                        buff.setCharAt(index, (char)(hexValue(chr1) * 16 + hexValue(chr2)));
                        buff.delete(index + 1, index + 3);
                        continue;
                    }
                } else {
                    delete = true;
                }
            } else if (isAsciiAlpha(chr) || (!first && isAsciiNum(chr))) {
                first = false;
            } else {
                delete = true;
            }
            if (delete) {
                buff.deleteCharAt(index);
            } else {
                index++;
            }
        }
        if (first && buff.length() > 0) {
            buff.setLength(buff.length() - 1);
        }
        return buff.toString();
    }
    
    /**
     * Get package information based on package name.
     * 
     * @param pname
     * @return package information
     */
    public PackageHolder getPackage(String pname) {
        PackageHolder pkg = (PackageHolder)m_namePackageMap.get(pname);
        if (pkg == null) {
            
            // package doesn't exist yet, start by finding the parent package
            int split = pname.lastIndexOf('.');
            PackageHolder parent;
            if (split >= 0) {
                parent = getPackage(pname.substring(0, split));
            } else if (pname.length() > 0) {
                parent = getPackage("");
            } else {
                return new PackageHolder("", m_generateDirectory, null);
            }
            
            // check for specified generation directory
            File gendir = (File)m_packageDirectoryMap.get(pname);
            if (gendir == null) {
                File pardir = parent.getGenerateDirectory();
                if (pardir != null) {
                    
                    // set directory path based on parent directory
                    gendir = new File(pardir, pname.substring(split + 1));
                }
            }
            
            // create the package information
            pkg = new PackageHolder(pname, gendir, parent);
            m_namePackageMap.put(pname, pkg);
            
        }
        return pkg;
    }
    
    /**
     * Get the information for a package.
     * 
     * @param uri corresponding namespace URI (non-<code>null</code>, empty string for no namespace)
     * @return package information
     */
    public PackageHolder getPackageForUri(String uri) {
        
        // first check for existing package
        PackageHolder pkg = (PackageHolder)m_uriPackageMap.get(uri);
        if (pkg == null) {
            
            // first check direct mapping of namespace URI to package name
            String pname = null;
            if (m_namespacePackageMap != null) {
                pname = (String)m_namespacePackageMap.get(uri);
            }
            
            // otherwise convert URI to package name
            if (pname == null) {
                pname = uriToPackage(uri);
            }
            
            // get package and add to map
            pkg = getPackage(pname);
            m_uriPackageMap.put(uri, pkg);
            
        }
        return pkg;
    }
    
    /**
     * Get the defined packages. The returned list is guaranteed to be ordered such that parent packages precede child
     * packages.
     * 
     * @return packages
     */
    public ArrayList getPackages() {
        ArrayList keys = m_namePackageMap.keyList();
        ArrayList packs = new ArrayList();
        for (int i = 0; i < keys.size(); i++) {
            packs.add(m_namePackageMap.get(keys.get(i)));
        }
        return packs;
    }
}
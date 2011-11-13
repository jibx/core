/*
 * Copyright (c) 2009, Dennis M. Sosnoski. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Organizer for imports to a source file. This is a state-based organizer, which allows tentative imports to be
 * overridden up until the point where the map from fully-qualified class names to unqualified names is constructed by
 * calling {@link #getNameMap()}, then further non-conflicting imports are allowed until the final import list is
 * constructed by calling {@link #freeze(String)}.
 * 
 * @author Dennis M. Sosnoski
 */
public class ImportsTracker
{
    /** Package name for classes in source. */
    private final String m_packageName;
    
    /** Set of imported classes. */
    private final TreeSet m_importedTypes;
    
    /** Map from simple names of unqualified types to full names. */
    private final Map m_unqualifiedNameType;
    
    /** Set of unqualified type full names. */
    private final Map m_localTypeName;
    
    /** Map from class names in imports set to names used (<code>null</code> until {@link #getNameMap()} called). */
    private Map m_nameMap;
    
    /** Further imports blocked flag. */
    private boolean m_frozen;

    /**
     * Constructor.
     * 
     * @param pkgname containing package name
     */
    public ImportsTracker(String pkgname) {
        m_packageName = pkgname;
        m_importedTypes = new TreeSet();
        m_unqualifiedNameType = new HashMap();
        m_localTypeName = new HashMap();
    }
    
    /**
     * Add local definition name to those visible in class. If the name conflicts with an import, the import is removed
     * to force fully-qualified references.
     * 
     * @param name simple class name
     * @param fqname fully qualified class name
     */
    public void addLocalType(String name, String fqname) {
    
        // check for conflict with current import
        String prior = (String)m_unqualifiedNameType.get(name);
        if (prior != null) {
            if (!fqname.equals(prior)) {
                m_importedTypes.remove(prior);
            }
        }
        
        // add name to unqualified types
        m_unqualifiedNameType.put(name, fqname);
        m_localTypeName.put(fqname, name);
    }
    
    /**
     * Add import for class. If the requested import doesn't conflict with the current set it's added.
     * 
     * @param fqname fully qualified class name
     * @param force force replacement of current import
     * @return <code>true</code> if added as import
     */
    protected boolean addImport(String fqname, boolean force) {
        if (m_importedTypes.contains(fqname) || m_localTypeName.containsKey(fqname)) {
            return true;
        } else {
            
            // get simple class name
            boolean auto = false;
            String sname = fqname;
            int split = sname.lastIndexOf('.');
            if (split >= 0) {
                sname = sname.substring(split+1);
                auto = "java.lang".equals(fqname.substring(0, split));
            }
            
            // check for conflict with current import preventing addition
            if (m_unqualifiedNameType.containsKey(sname) && !(split < 0 || auto || force)) {
                return false;
            }
            
            // add import for type (overriding old import, if any)
            m_unqualifiedNameType.put(sname, fqname);
            if (split >= 0) {
                
                // make sure still modifiable, and add to imported type set
                if (m_frozen) {
                    throw new IllegalStateException("Internal error - attempt to add import after imports frozen");
                } else {
                    m_importedTypes.add(fqname);
                }
                
            }
            return true;
            
        }
    }
    
    /**
     * Check if type needs qualified references.
     * 
     * @param fqname fully qualified class name
     * @return <code>true</code> if needs qualification
     */
    public boolean isQualified(String fqname) {
        return !m_importedTypes.contains(fqname) && !m_localTypeName.containsKey(fqname);
    }
    
    /**
     * Get map from imported fully-qualified class names to short names. Once this method is called, overrides of
     * existing imports are blocked (since the existing imports may have been used), though added non-conflicting
     * imports can still be added.
     * 
     * @return map
     */
    public Map getNameMap() {
        if (m_nameMap == null) {
            
            // build name map reflecting all current imports
            m_nameMap = new HashMap(m_localTypeName);
            for (Iterator iter = m_importedTypes.iterator(); iter.hasNext();) {
                String impname = (String)iter.next();
                if (impname.startsWith("java.lang.") && impname.lastIndexOf('.') <= "java.lang.".length()) {
                    m_nameMap.put(impname, impname.substring("java.lang.".length()));
                } else {
                    int split = impname.lastIndexOf('.') + 1;
                    m_nameMap.put(impname, impname.substring(split));
                }
            }
            
        }
        return m_nameMap;
    }
    
    /**
     * Get the name to be used for a type. If the type has been imported this returns the short form of the name;
     * otherwise it just returns the fully-qualified name. This method forces a call to {@link #getNameMap()}, which in
     * turn blocks removing any imports later.
     * 
     * @param type fully-qualified type name
     * @return name
     */
    public String getName(String type) {
        Map map = getNameMap();
        String name = (String)map.get(type);
        if (name == null) {
            name = type;
        }
        return name;
    }
    
    /**
     * Freeze imports and return a list of imports.
     *
     * @param cname simple name of class (used to identify inner class references)
     * @return list
     */
    public List freeze(String cname) {
        List list = new ArrayList();
        for (Iterator iter = m_importedTypes.iterator(); iter.hasNext();) {
            String impname = (String)iter.next();
            int length = m_packageName.length();
            if (length > 0 && impname.length() > length && impname.startsWith(m_packageName)) {
                int split = impname.indexOf('.', length+1);
                if (split > 0 && !impname.substring(length+1, split).equals(cname)) {
                    list.add(impname);
                }
            } else if (!impname.startsWith("java.lang.") || impname.lastIndexOf('.') > "java.lang.".length()) {
                list.add(impname);
            }
        }
        return list;
    }
}
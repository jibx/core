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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Base class for external data used in constructing a namespaced file which may reference other files of the same type.
 * This class tracks both the referenced files and the corresponding namespace references, assigning prefixes for the
 * latter as appropriate. The namespace associated with this file is always given the prefix 'tns', while those used for
 * other files get prefixes of the form 'ns1', 'ns2', etc.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class HolderBase
{
    private final String m_namespace;
    
    private final Map m_nsPrefixMap;
    
    private boolean m_finished;
    
    private boolean m_existingFile;
    
    private String m_fileName;
    
    private Set m_referenceSet;
    
    /**
     * Constructor for a file being generated.
     * 
     * @param uri (<code>null</code> if no-namespace binding)
     */
    public HolderBase(String uri) {
        m_namespace = uri;
        m_nsPrefixMap = new HashMap();
        if (uri != null) {
            m_nsPrefixMap.put(uri, "tns");
        }
    }
    
    /**
     * Constructor for an existing file.
     * 
     * @param uri (<code>null</code> if no-namespace binding)
     * @param name file name
     */
    public HolderBase(String uri, String name) {
        this(uri);
        m_fileName = name;
        m_finished = m_existingFile = true;
    }
    
    /**
     * Check if file already exists.
     * 
     * @return <code>true</code> if existing file, <code>false</code> if not
     */
    public boolean isExistingFile() {
        return m_existingFile;
    }
    
    /**
     * Check if file is modifiable. This will always be <code>false</code> for an existing file, and will be set
     * <code>false</code> for new files when {@link #finish()} is called.
     * 
     * @return <code>true</code> if file is modifiable, <code>false</code> if not
     */
    public boolean isModifiable() {
        return !m_finished;
    }
    
    /**
     * Get the prefix for a namespace URI. The first time this is called for a particular namespace URI a prefix is
     * assigned and returned. This assumes that the default namespace is always the no-namespace.
     * 
     * @param uri
     * @return prefix
     */
    public String getPrefix(String uri) {
        if (uri == null) {
            return "";
        } else {
            String prefix = (String)m_nsPrefixMap.get(uri);
            if (prefix == null) {
                if (!isModifiable()) {
                    throw new IllegalStateException("Internal error - file is not modifiable");
                }
                prefix = "ns" + m_nsPrefixMap.size();
                m_nsPrefixMap.put(uri, prefix);
                addNamespaceDecl(prefix, uri);
            }
            return prefix;
        }
    }
    
    /**
     * Subclass hook method to handle adding a namespace declaration. The implementation of this method needs to set up
     * the namespace declaration for output in the generated XML.
     * 
     * @param prefix
     * @param uri
     */
    protected abstract void addNamespaceDecl(String prefix, String uri);
    
    /**
     * Get namespace URI associated with this file.
     * 
     * @return namespace (<code>null</code> if no-namespace)
     */
    public String getNamespace() {
        return m_namespace;
    }
    
    /**
     * Get the file name to be used for this file.
     * 
     * @return name (<code>null</code> if not set)
     */
    public String getFileName() {
        return m_fileName;
    }
    
    /**
     * Set the file name to be used for this file.
     * 
     * @param name
     */
    public void setFileName(String name) {
        if (!isModifiable()) {
            throw new IllegalStateException("Internal error - file is not modifiable");
        }
        m_fileName = name;
    }
    
    /**
     * Record a reference from this file to another file of the same type. This adds the reference to the set of
     * references. Self-references are ignored.
     * 
     * @param ref
     */
    public void addReference(HolderBase ref) {
        if (ref != this && (m_referenceSet == null || !m_referenceSet.contains(ref))) {
            if (!isModifiable()) {
                throw new IllegalStateException("Internal error - file is not modifiable");
            }
            if (ref == null) {
                throw new IllegalArgumentException("Reference cannot be to null");
            }
            if (m_referenceSet == null) {
                m_referenceSet = new HashSet();
            }
            m_referenceSet.add(ref);
        }
    }
    
    /**
     * Get the set of references from this file to other files of the same type.
     * 
     * @return references
     */
    public Set getReferences() {
        if (m_referenceSet == null) {
            return Collections.EMPTY_SET;
        } else {
            return m_referenceSet;
        }
    }
    
    /**
     * Implementation method for subclasses to complete the construction of the file. This includes processing
     * references to other files, as well as any other components of the file which need to be finalized. This method
     * must be called after all references have been added, allowing the subclass to add any necessary structures to the
     * file representation. Subclasses must call the base class implementation when their processing is completed.
     */
    public void finish() {
        m_finished = true;
    }
}
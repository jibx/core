/*
Copyright (c) 2004-2011, Dennis M. Sosnoski. All rights reserved.

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

package org.jibx.runtime.impl;

import java.io.IOException;
import java.util.Stack;

import org.jibx.runtime.IXMLWriter;

/**
 * Base implementation of XML writer interface namespace handling. This tracks
 * only the namespace declarations and the element nesting depth. It can be used
 * as a base class for all forms of output.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class XMLWriterNamespaceBase implements IXMLWriter
{
    /** Empty array for default return. */
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    
    /** URIs for namespaces. */
    protected String[] m_uris;
    
    /** Prefixes currently defined for namespaces. */
    protected String[] m_prefixes;
    
    /** Depth of nested tags. */
    private int m_nestingDepth;
    
    /** Stack of information for namespace declarations. */
    private Stack m_namespaceStack;
    
    /** Depth of top namespace declaration level. */
    private int m_namespaceDepth;
    
    /** Extension namespace URIs (<code>null</code> if not in use). */
    private String[][] m_extensionUris;
    
    /** Extension namespace prefixes (<code>null</code> if not in use). */
    private String[][] m_extensionPrefixes;
    
    /** Current namespace translation table (<code>null</code> if none). */
    private int[] m_translateTable;
    
    /** Stack of namespace translation tables in use. */
    private Stack m_translateTableStack;
    
    /**
     * Constructor.
     *
     * @param uris ordered array of URIs for namespaces used in document (must
     * be constant; the value in position 0 must always be the empty string "",
     * and the value in position 1 must always be the XML namespace
     * "http://www.w3.org/XML/1998/namespace")
     */
    public XMLWriterNamespaceBase(String[] uris) {
        m_uris = uris;
        m_prefixes = new String[uris.length];
        m_prefixes[0] = "";
        m_prefixes[1] = "xml";
        m_namespaceStack = new Stack();
        m_namespaceDepth = -1;
        m_translateTableStack = new Stack();
    }
    
    /**
     * Copy constructor. This initializes the extension namespace information
     * from an existing instance.
     *
     * @param base existing instance
     * @param uris ordered array of URIs for namespaces used in document
     */
    public XMLWriterNamespaceBase(XMLWriterNamespaceBase base, String[] uris) {
        this(uris);
        m_extensionUris = base.m_extensionUris;
        m_extensionPrefixes = base.m_extensionPrefixes;
        m_nestingDepth = base.m_nestingDepth;
    }

    /**
     * Report to subclass that namespace has been defined.
     *
     * @param index post-translation namespace URI index number
     * @param prefix prefix used for namespace
     * @throws IOException if error writing to document
     */
    protected abstract void defineNamespace(int index, String prefix)
        throws IOException;
    
    /**
     * Report to subclass that namespace has been undefined.
     *
     * @param index post-translation namespace URI index number
     */
    protected abstract void undefineNamespace(int index);
    
    /**
     * Set namespace URIs. It is provided so that subclasses can implement easy
     * reuse.
     *
     * @param uris ordered array of URIs for namespaces used in document
     */
    protected void internalSetUris(String[] uris) {
        m_uris = uris;
        m_prefixes = new String[uris.length];
        m_prefixes[0] = "";
        m_prefixes[1] = "xml";
    }
    
    /**
     * Set prefix for namespace.
     *
     * @param index post-translation namespace URI index number
     * @param prefix
     */
    private void setNamespacePrefix(int index, String prefix) {
        if (index < m_prefixes.length) {
            m_prefixes[index] = prefix;
        } else if (m_extensionUris != null) {
            index -= m_prefixes.length;
            for (int i = 0; i < m_extensionUris.length; i++) {
                int length = m_extensionUris[i].length;
                if (index < length) {
                    m_extensionPrefixes[i][index] = prefix;
                    break;
                } else {
                    index -= length;
                }
            }
        }
    }
    
    /**
     * Open the specified namespaces. Previously active namespace declarations
     * are not duplicated.
     *
     * @param nums array of namespace indexes defined by this element (reference
     * kept until namespaces are closed, values may be modified by this method)
     * @param prefs array of namespace prefixes mapped by this element (no
     * <code>null</code> values, use "" for default namespace declaration)
     * @return array of translated indexes for namespaces not previously active
     * (the ones actually needing to be declared, in the case of text output)
     * @throws IOException on error writing to document
     */
    public int[] openNamespaces(int[] nums, String[] prefs) throws IOException {
        
        // find the number of namespaces actually being declared
        int count = 0;
        loop: for (int i = 0; i < nums.length; i++) {
            
            // set prefix only if different and not changing prefix to default
            String newpref = prefs[i];
            String oldpref = getNamespacePrefix(nums[i]);
            boolean use = false;
            if (!newpref.equals(oldpref) &&
                (!"".equals(newpref) || oldpref == null)) {
                use = true;
                for (int j = 0; j < i; j++) {
                    if (nums[i] == nums[j]) {
                        if ("".equals(newpref)) {
                            
                            // ignore replacing prefix with default in same call
                            use = false;
                            break;
                            
                        } else {
                            
                            // multiple prefixes for namespace, keep only last
                            nums[j] = -1;
                            continue loop;
                            
                        }
                    }
                }
            }
            if (use) {
                count++;
            } else {
                nums[i] = -1;
            }
        }
        
        // check if there's actually any change
        int[] deltas = EMPTY_INT_ARRAY;
        if (count > 0) {
            
            // get the set of namespace indexes that are changing
            String[] priors = new String[count];
            if (count == nums.length) {
                
                // replace the full set, tracking the prior values
                deltas = nums;
                if (m_translateTable != null) {
                    for (int i = 0; i < deltas.length; i++) {
                        deltas[i] = m_translateTable[deltas[i]];
                    }
                }
                for (int i = 0; i < count; i++) {
                    int slot = deltas[i];
                    priors[i] = getNamespacePrefix(slot);
                    setNamespacePrefix(slot, prefs[i]);
                    defineNamespace(slot, prefs[i]);
                }
                
            } else {
                
                // replace only changed ones, tracking both indexes and priors
                int fill = 0;
                deltas = new int[count];
                for (int i = 0; i < nums.length; i++) {
                    int slot = nums[i];
                    if (slot >= 0) {
                        int xlate = translateNamespace(slot);
                        deltas[fill] = xlate;
                        priors[fill++] = getNamespacePrefix(slot);
                        setNamespacePrefix(xlate, prefs[i]);
                        defineNamespace(xlate, prefs[i]);
                    }
                }
            }
            
            // set up for undeclaring namespaces on close of element
            m_namespaceStack.push
                (new DeclarationInfo(m_nestingDepth, deltas, priors));
            m_namespaceDepth = m_nestingDepth;
            
        }
        return deltas;
    }
    
    /**
     * Ends the current innermost set of nested namespace definitions. Reverts
     * the namespaces involved to their previously-declared prefixes, and sets
     * up for ending the new innermost set.
     */
    private void closeNamespaces() {
        
        // revert prefixes for namespaces included in last declaration
        DeclarationInfo info = (DeclarationInfo)m_namespaceStack.pop();
        int[] deltas = info.m_deltas;
        String[] priors = info.m_priors;
        for (int i = deltas.length - 1; i >= 0; i--) {
            int index = deltas[i];
            undefineNamespace(index);
            if (index < m_prefixes.length) {
                m_prefixes[index] = priors[i];
            } else if (m_extensionUris != null) {
                index -= m_prefixes.length;
                for (int j = 0; j < m_extensionUris.length; j++) {
                    int length = m_extensionUris[j].length;
                    if (index < length) {
                        m_extensionPrefixes[j][index] = priors[i];
                    } else {
                        index -= length;
                    }
                }
            }
        }
        
        // set up for clearing next nested set
        if (m_namespaceStack.empty()) {
            m_namespaceDepth = -1;
        } else {
            m_namespaceDepth =
                ((DeclarationInfo)m_namespaceStack.peek()).m_depth;
        }
    }
    
    /**
     * Get the current element nesting depth. Elements are only counted in the
     * depth returned when they're officially open - after the start tag has
     * been output and before the end tag has been output.
     *
     * @return number of nested elements at current point in output
     */
    public final int getNestingDepth() {
        return m_nestingDepth;
    }
    
    /**
     * Get the number of namespaces currently defined. This is equivalent to the
     * index of the next extension namespace added.
     *
     * @return namespace count
     */
    public final int getNamespaceCount() {
        int count = m_uris.length;
        if (m_extensionUris != null) {
            for (int i = 0; i < m_extensionUris.length; i++) {
                count += m_extensionUris[i].length;
            }
        }
        return count;
    }
    
    /**
     * Increment the current nesting depth. Subclasses need to call this method
     * whenever an element start tag is written.
     */
    protected void incrementNesting() {
        m_nestingDepth++;
    }
    
    /**
     * Decrement the current nesting depth. Subclasses need to call this method
     * whenever an element end tag is written.
     */
    protected void decrementNesting() {
        --m_nestingDepth;
        if (m_nestingDepth >= 0) {
            while (m_nestingDepth == m_namespaceDepth) {
                closeNamespaces();
            }
        }
    }
    
    /**
     * Reset to initial state for reuse. Subclasses overriding this method need
     * to call this base class implementation during their processing.
     */
    public void reset() {
        m_nestingDepth = 0;
        m_namespaceDepth = -1;
        m_namespaceStack.clear();
        m_prefixes = new String[m_uris.length];
        m_prefixes[0] = "";
        m_prefixes[1] = "xml";
        m_extensionUris = null;
        m_extensionPrefixes = null;
        m_translateTable = null;
        m_translateTableStack.clear();
    }
    
    /**
     * Get namespace URIs for mapping. This gets the full ordered array of
     * namespaces known in the binding used for this marshalling, where the
     * index number of each namespace URI is the namespace index used to lookup
     * the prefix when marshalling a name in that namespace. The returned array
     * must not be modified.
     *
     * @return array of namespaces
     */
    public final String[] getNamespaces() {
        return m_uris;
    }
    
    /**
     * Get URI for translated namespace index.
     *
     * @param index namespace URI index number (post-translation)
     * @return namespace URI text, or <code>null</code> if the namespace index
     * is invalid
     */
    protected final String internalNamespaceUri(int index) {
        if (index < m_uris.length) {
            return m_uris[index];
        } else if (m_extensionUris != null) {
            index -= m_uris.length;
            for (int i = 0; i < m_extensionUris.length; i++) {
                int length = m_extensionUris[i].length;
                if (index < length) {
                    return m_extensionUris[i][index];
                } else {
                    index -= length;
                }
            }
        }
        return null;
    }
    
    /**
     * Get URI for namespace.
     *
     * @param index namespace URI index number
     * @return namespace URI text, or <code>null</code> if the namespace index
     * is invalid
     */
    public final String getNamespaceUri(int index) {
        return internalNamespaceUri(translateNamespace(index));
    }
    
    /**
     * Get current prefix defined for translated namespace index.
     *
     * @param index namespace URI index number (post-translation)
     * @return current prefix text, or <code>null</code> if the namespace is not
     * currently mapped
     */
    protected final String internalNamespacePrefix(int index) {
        if (index < m_prefixes.length) {
            return m_prefixes[index];
        } else if (m_extensionUris != null) {
            index -= m_prefixes.length;
            for (int i = 0; i < m_extensionUris.length; i++) {
                int length = m_extensionUris[i].length;
                if (index < length) {
                    return m_extensionPrefixes[i][index];
                } else {
                    index -= length;
                }
            }
        }
        return null;
    }
    
    /**
     * Get current prefix defined for namespace.
     *
     * @param index namespace URI index number
     * @return current prefix text, or <code>null</code> if the namespace is not
     * currently mapped
     */
    public final String getNamespacePrefix(int index) {
        return internalNamespacePrefix(translateNamespace(index));
    }
    
    /**
     * Get index of namespace mapped to prefix. If namespace translation is in
     * use, the returned index will be the value prior to translation. This can
     * be an expensive operation (regardless of whether translation is used or
     * not) with time proportional to the number of namespaces defined, so it
     * should be used with care.
     * 
     * @param prefix text to match  (non-<code>null</code>, use "" for default
     * prefix)
     * @return index namespace URI index number mapped to prefix
     */
    public final int getPrefixIndex(String prefix) {
        if (m_extensionPrefixes != null) {
            for (int i = m_extensionPrefixes.length-1; i >= 0; i--) {
                String[] prefixes = m_extensionPrefixes[i];
                for (int j = prefixes.length-1; j >= 0; j--) {
                    if (prefix.equals(prefixes[j])) {
                        int index = j + m_prefixes.length;
                        for (int k = i-1; k >= 0; k--) {
                            index += m_extensionPrefixes[k].length;
                        }
                        return index;
                    }
                }
            }
        }
        if (m_translateTable == null) {
            for (int i = m_prefixes.length-1; i >= 0; i--) {
                if (prefix.equals(m_prefixes[i])) {
                    return i;
                }
            }
        } else {
            for (int i = m_translateTable.length-1; i >= 0; i--) {
                int xlate = m_translateTable[i];
                if (prefix.equals(m_prefixes[xlate])) {
                    return xlate;
                }
            }
        }
        return -1;
    }
    
    /**
     * Grow array of array of strings.
     *
     * @param base array to be grown (<code>null</code> is treated as zero
     * length)
     * @param items array of strings to be added at end of base array
     * @return array with added array of items
     */
    protected static String[][] growArray(String[][] base, String[] items) {
        if (base == null) {
            return new String[][] { items };
        } else {
            int length = base.length;
            String[][] grow = new String[length+1][];
            System.arraycopy(base, 0, grow, 0, length);
            grow[length] = items;
            return grow;
        }
    }
    
    /**
     * Shrink array of array of strings.
     *
     * @param base array to be shrunk
     * @return array with last set of items eliminated (<code>null</code> if
     * empty)
     */
    protected static String[][] shrinkArray(String[][] base) {
        int length = base.length;
        if (length == 1) {
            return null;
        } else {
            String[][] shrink = new String[length-1][];
            System.arraycopy(base, 0, shrink, 0, length-1);
            return shrink;
        }
    }
    
    /**
     * Append extension namespace URIs to those in mapping.
     *
     * @param uris namespace URIs to extend those in mapping
     */
    public void pushExtensionNamespaces(String[] uris) {
        m_extensionUris = growArray(m_extensionUris, uris);
        m_extensionPrefixes =
            growArray(m_extensionPrefixes, new String[uris.length]);
    }
    
    /**
     * Remove extension namespace URIs. This removes the last set of
     * extension namespaces pushed using {@link #pushExtensionNamespaces}.
     */
    public void popExtensionNamespaces() {
        m_extensionUris = shrinkArray(m_extensionUris);
        m_extensionPrefixes = shrinkArray(m_extensionPrefixes);
    }
    
    /**
     * Get extension namespace URIs added to those in mapping. This gets the
     * current set of extension definitions. The returned arrays must not be
     * modified.
     *
     * @return array of arrays of extension namespaces (<code>null</code> if
     * none)
     */
    public final String[][] getExtensionNamespaces() {
        return m_extensionUris;
    }
    
    /**
     * Translate a namespace index number to match internal tables.
     *
     * @param index raw namespace index
     * @return namespace index number for internal tables
     */
    public int translateNamespace(int index) {
        if (m_translateTable != null && index < m_translateTable.length) {
            return m_translateTable[index];
        } else {
            return index;
        }
    }
    
    /**
     * Push a translation table to be used for converting namespace index
     * numbers passed as arguments to values used for internal lookup. This
     * allows a layer of indirection between the client code and the
     * namespace definitions, designed for use in supporting precompiled
     * bindings. The translated values must match the internal tables.
     *
     * @param table translation table to be used (<code>null</code> if no
     * change)
     */
    public void pushTranslationTable(int[] table) {
        m_translateTableStack.push(m_translateTable);
        if (table != null) {
            m_translateTable = table;
        }
    }
    
    /**
     * Pop a translation table used for converting namespace index numbers to
     * values matching the internal lookup.
     */
    public void popTranslationTable() {
        m_translateTable = (int[])m_translateTableStack.pop();
    }
    
    /**
     * Namespace declaration tracking information. This tracks all information
     * associated with an element that declares namespaces.
     */
    private static class DeclarationInfo
    {
        /** Depth of element making declaration. */
        public final int m_depth;
        
        /** Indexes of namespaces included in declarations. */
        public final int[] m_deltas;
        
        /** Prior prefixes for namespaces. */
        public final String[] m_priors;
        
        /** Simple constructor. */
        public DeclarationInfo(int depth, int[] deltas, String[] priors) {
            m_depth = depth;
            m_deltas = deltas;
            m_priors = priors;
        }
    }
}
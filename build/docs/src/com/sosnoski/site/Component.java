/*
Copyright (c) 2004, Dennis M. Sosnoski.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of Sosnoski Software Solutions, Inc. nor the names of its
   personnel may be used to endorse or promote products derived from this
   software without specific prior written permission.

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

package com.sosnoski.site;

import java.io.File;
import java.util.ArrayList;

/**
 * Common base class for all components of site definition.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public abstract class Component
{
    private String m_sourceDir;     // configured directory for source files
    private String m_accessDir;     // configured directory for output pages
    private ArrayList m_namedValues; // named value pairs for template
                                    // (lazy create, <code>null</code> if none)
	private ArrayList m_children;   // child components
                                    // (lazy create, <code>null</code> if none)
    
    /**
     * Default constructor. Used for unmarshalling.
     */
    
    protected Component() {}
    
    /**
     * Copy constructor. This copies the template instance, modifying the source
     * directory path to match 
     * 
     * @param base template to be copied
     * @param root base source directory
     * @param access override access directory
     */
    
    protected Component(Component base, File root, String access) {
        if (base.m_sourceDir == null) {
            m_sourceDir = root.getAbsolutePath();
        } else {
            m_sourceDir = applyFilePath(root, base.m_sourceDir).
                getAbsolutePath(); 
        }
        m_accessDir = access;
        if (base.m_namedValues != null) {
            m_namedValues = new ArrayList(base.m_namedValues);
        }
        if (base.m_children != null) {
            m_children = new ArrayList(base.m_children);
        }
    }

	/**
	 * Add named value for template to this component.
	 *
	 * @param value named value for template
	 */
	 
	public void addValue(Object value) {
        if (m_namedValues == null) {
            m_namedValues = new ArrayList();
        }
        m_namedValues.add(value);
	}

    /**
     * Add child component to this component.
     *
     * @param child component to be added as child
     */
     
    public void addChild(Object child) {
        if (m_children == null) {
            m_children = new ArrayList();
        }
        m_children.add(child);
    }

    /**
     * Get source directory path.
     *
     * @return source directory path
     */
     
    public String getSourceDir() {
        return m_sourceDir;
    }

    /**
     * Get access directory path.
     *
     * @return access directory path
     */
     
    public String getAccessDir() {
        return m_accessDir;
    }

	/**
	 * Get values to be added to template. Returns the collection of values for
     * this component.
	 *
	 * @return collection of values for template (<code>null</code> if none)
	 */
	 
	public ArrayList getValues() {
		return m_namedValues;
	}

	/**
	 * Get child items. Returns the collection of child components for this
     * site.
	 *
	 * @return collection of child components (<code>null</code> if no children)
	 */
	 
	public ArrayList getChildren() {
		return m_children;
	}

    //
    // Utility methods for combining paths

    /**
     * Construct access directory path. Applies an additional path to the
     * supplied base path. If the added path is absolute the base path is
     * ignored.
     *
     * @param base base directory path (cannot be <code>null</code>)
     * @param path added path from base (<code>null</code> if none)
     * @return derived directory path (may be the same as supplied
     * <code>base</code> value)
     */
    
    public static String applyAccessPath(String base, String path) {
        if (path == null) {
            return (base == null) ? "" : base;
        } else if (path.startsWith("http:")) {
            return path;
        } else {
            if (path.charAt(path.length()-1) != '/') {
                path = path + '/';
            }
            if (path.charAt(0) == '/') {
                return path.substring(1);
            } else if (base.length() == 0) {
                return path;
            } else {
                return base + path;
            }
        }
    }

    /**
     * Construct file directory path. Applies an additional path to the supplied
     * base path. If the additional path is absolute it replaces the base path
     * is ignored.
     *
     * @param base base directory path (cannot be <code>null</code>)
     * @param path added path from base (<code>null</code> if none)
     * @return derived directory path (may be the same as supplied
     * <code>base</code> value)
     */
    
    public static File applyFilePath(File base, String path) {
        if (path == null) {
            return base;
        } else {
            File file = new File(path);
            if (!file.isAbsolute()) {
                file = new File(base, path);
            }
            return file;
        }
    }

    /**
     * Generate relative path between files.
     *
     * @param from source location for path
     * @param to destination location for path
     * @return relative path from source to destination
     */
    
    public static String relativePath(String from, String to) {
        if (to.charAt(0) != '/' && !to.startsWith("http:")) {
            int mark;
            boolean lead = true;
            while ((mark = from.indexOf('/')) >= 0) {
                mark++;
                String head = from.substring(0, mark);
                from = from.substring(mark);
                if (lead && to.startsWith(head)) {
                    to = to.substring(mark);
                } else {
                    lead = false;
                    to = "../" + to;
                }
            }
        }
        return to;
    }
}
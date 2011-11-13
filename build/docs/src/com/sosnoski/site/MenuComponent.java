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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jibx.runtime.ValidationException;

/**
 * Common base class for components that actually participate in the menu
 * structure.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public abstract class MenuComponent extends Component
{
    // static map from ids to components
    public static HashMap s_idMap = new HashMap();  // path substitutions
    
    // items only used internally or through accessors
	private String m_label;         // menu label
    private String m_id;            // identifier for reference to this item
    private String m_setTarget;     // specified link target path
    private String m_linkDir;       // target directory for output pages
    private String m_linkTarget;    // target path for link
    
    /**
     * Default constructor. Used for unmarshalling.
     */
    
    protected MenuComponent() {}
    
    /**
     * Constructor from base component. This is used only when creating a
     * wrapper for an included menu.
     * 
     * @param base component to be wrapped
     * @param root base source directory
     * @param access override access directory
     * @param label menu label
     */
    
    protected MenuComponent(MenuComponent base, File root, String access,
        String label) {
        super(base, root, access);
        m_label = (label == null) ? base.m_label : label;
        m_id = base.m_id;
        m_setTarget = base.m_setTarget;
    }
    
    /**
     * Constructor from base non-component. This is used only when creating a
     * wrapper for an included site.
     * 
     * @param base non-component to be wrapped
     * @param root base source directory
     * @param access override access directory
     * @param label menu label
     */
    
    protected MenuComponent(Component base, File root, String access,
        String label) {
        super(base, root, access);
        m_label = label;
    }
    
	/**
	 * Set all linkages. This resolves linkages between different components in
	 * order to establish the target page for each component.
	 *
     * @param target destination directory for parent component
     * @exception ValidationException on configuration error
	 */
	 
	public final void setLinkages(String target) throws ValidationException {
        
        // define id if present on component
        if (m_id != null) {
            s_idMap.put(m_id, this);
        }
        
        // propagate linkage setting down the tree
        m_linkDir = Component.applyAccessPath(target, getAccessDir());
        ArrayList childs = getChildren();
        if (childs != null) {
            for (int i = 0; i < childs.size(); i++) {
                ((MenuComponent)childs.get(i)).setLinkages(m_linkDir);
            }
        }
        
        // check if target needs to be set
        if (m_setTarget == null) {
            m_linkTarget = findTarget(m_linkDir);
            if (m_id != null && m_linkTarget == null) {
                throw new ValidationException
                    ("id requires source file or path", this);
            }
        } else if (m_setTarget.charAt(0) == '/') {
            m_linkTarget = m_setTarget;
        } else {
            m_linkTarget = m_linkDir + m_setTarget;
        }
	}

    /**
     * Load all content. This processes the actual loading of text files for all
     * child items.
     *
     * @param source parent source directory path
     * @exception IOException on access error
     * @exception ValidationException on configuration error
     */
     
    public final void loadContent(File source)
        throws ValidationException, IOException {
        
        // propagate linkage setting down the tree
        source = applyFilePath(source, getSourceDir());
        loadComponent(source);
        ArrayList childs = getChildren();
        if (childs != null) {
            for (int i = 0; i < childs.size(); i++) {
                ((MenuComponent)childs.get(i)).loadContent(source);
            }
        }
    }

    /**
     * Find link target for this component.
     *
     * @param target destination directory for component
     * @return link target when this component is selected
     * @exception ValidationException on configuration error
     */
     
    protected abstract String findTarget(String target)
        throws ValidationException;

    /**
     * Load this component. This must be implemented by subclasses to perform
     * any required loading or initialization. It is called after all child
     * components have been loaded.
     *
     * @param source source directory path
     * @exception ValidationException on configuration error
     */
     
    protected abstract void loadComponent(File source)
        throws ValidationException;

    /**
     * Get label for this component.
     *
     * @return menu component label
     */
     
    public String getLabel() {
        return m_label;
    }

    /**
     * Get directory access path for output pages.
     *
     * @return target for menu click
     */
     
    public String getTargetDir() {
        return m_linkDir;
    }

	/**
	 * Get access path target for click on menu component.
	 *
	 * @return target for menu click
	 */
	 
	public String getTarget() {
        return m_linkTarget;
	}

    /**
     * Get relative path to target. This is only valid for components that
     * correspond to an actual site page.
     *
     * @param target full path to target (may be relative or absolute)
     * @return relative path to target
     */
     
    public String relativePath(String target) {
        if (m_linkTarget == null) {
            return null;
        } else {
            return relativePath(m_linkTarget, target);
        }
    }
}
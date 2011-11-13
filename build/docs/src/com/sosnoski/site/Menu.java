/*
Copyright (c) 2003-2004, Dennis M. Sosnoski.
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

import org.jibx.runtime.ValidationException;

/**
 * Information for a menu. Contains child items, allows setting default target
 * id from amoung the child items.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class Menu extends MenuComponent
{
    private String m_defaultId;     // target id for unexpanded menu clicked
    
    /**
     * Default constructor. Used for unmarshalling.
     */
    
    protected Menu() {}
    
    /**
     * Constructor from unmarshalled menu. This is only used for include files,
     * where a wrapper menu is used to replace the included menu with adjusted
     * source and access paths.
     * 
     * @param base menu to be wrapped
     * @param root base source directory
     * @param access override access directory
     * @param label label for this menu
     */
    
    public Menu(Menu base, File root, String access, String label) {
        super(base, root, access, label);
    }
    
    /**
     * Constructor from unmarshalled menu. This is only used for include files,
     * where a wrapper menu is used to replace the included menu with adjusted
     * source and access paths.
     * 
     * @param base menu to be wrapped
     * @param root base source directory
     * @param access override access directory
     * @param label label for this menu
     */
    
    public Menu(Site base, File root, String access, String label) {
        super(base, root, access, label);
    }

    /**
     * Find link target for this component.
     *
     * @param target destination directory for component
     * @return link target when this component is selected
     * @exception ValidationException on configuration error
     */
     
    protected String findTarget(String target) throws ValidationException {
        MenuComponent comp;
        if (m_defaultId == null) {
            comp = this;
            while (comp.getTarget() == null) {
                comp = (MenuComponent)comp.getChildren().get(0);
            }
        } else {
            comp = (MenuComponent)s_idMap.get(m_defaultId);
            if (comp == null) {
                throw new ValidationException("Target id value not defined",
                    this);
            }
        }
        return comp.getTarget();
    }

    /**
     * Load this component. Nothing to be done for a menu.
     *
     * @param source source directory path
     */
     
    protected void loadComponent(File source) {}
}
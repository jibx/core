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

import org.jibx.runtime.ValidationException;

/**
 * Holder for multiple menus making up a site.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class Site extends Component
{
    /**
     * Initialize site information. Links all menus and loads all component
     * pages, preparing the entire data structure for use.
     *
     * @param source parent source directory path
     * @exception IOException on access error
     * @exception ValidationException on configuration error
     */
     
    public final void initialize(File source)
        throws ValidationException, IOException {
        ArrayList childs = getChildren();
        if (childs != null) {
            for (int i = 0; i < childs.size(); i++) {
                ((Menu)childs.get(i)).setLinkages("");
            }
            for (int i = 0; i < childs.size(); i++) {
                ((Menu)childs.get(i)).loadContent(source);
            }
        }
    }

    /**
     * Add child component to this component. This uses special handling to
     * process included sites, ignoring <code>null</code> values.
     *
     * @param child component to be added as child
     */
     
    public void addChild(Object child) {
        if (child != null) {
            super.addChild(child);
        }
    }
}
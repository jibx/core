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
import java.io.FileReader;
import java.io.IOException;

import org.jibx.runtime.ValidationException;

/**
 * Information for a menu item. This allows for an id as well as a required file
 * path.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class Item extends MenuComponent
{
    private String m_fileName;      // source file name
	private Page m_page;			// actual page information

    /**
     * Find link target for this component.
     *
     * @param target destination directory for component
     * @return link target when this component is selected
     * @exception ValidationException on configuration error
     */
     
    protected String findTarget(String target) throws ValidationException {
        if (m_fileName == null) {
            return null;
        } else {
            
            // strip extension from file name to get base name
            String name = m_fileName;
            int split = name.lastIndexOf('.');
            if (split >= 0) {
                name = name.substring(0, split);
            }
            split = name.lastIndexOf(File.separatorChar);
            if (split >= 0) {
                name = name.substring(split+1);
            }
            
            // generate target page from same base name
            return target + name + ".html";
            
        }
    }

    /**
     * Load this component.
     *
     * @param source source directory path
     * @exception ValidationException on configuration error or access error
     */
     
    protected void loadComponent(File source) throws ValidationException {
        
        // load the actual page source
        if (m_fileName != null) {
            File file = null;
            try {
                file = new File(source, m_fileName);
                m_page = new Page(getTarget(),
                    new HTMLParseBuffer(new FileReader(file)));
            } catch (IOException e) {
                throw new ValidationException("Error processing file " +
                    file.toString(), e, this);
            }
        }
	}

	/**
	 * Get page body.
	 *
	 * @return page body for item (<code>null</code> if no page for item)
	 */
	 
	public String getBody() {
		return m_page == null ? null : m_page.getBody();
	}

	/**
	 * Get page header.
	 *
	 * @return page header for item (<code>null</code> if no page for item)
	 */
	 
	public String getHeader() {
		return m_page == null ? null : m_page.getHeader();
	}

    /**
     * Get page description.
     *
     * @return page header for item
     */
     
    public String getDescription() {
        return m_page == null ? null : m_page.getDescription();
    }

    /**
     * Get page keywords.
     *
     * @return page keywords for item
     */
     
    public String getKeywords() {
        return m_page == null ? null : m_page.getKeywords();
    }

	/**
	 * Get page title.
	 *
	 * @return page title for item (<code>null</code> if no page for item)
	 */
	 
	public String getTitle() {
		return m_page == null ? null : m_page.getTitle();
	}

	/**
	 * Check if page defined for item.
	 *
	 * @return <code>true</code> if item has a page, <code>false</code> if a
	 * menu item only
	 */
	 
	public boolean hasPage() {
		return m_page != null;
	}
}
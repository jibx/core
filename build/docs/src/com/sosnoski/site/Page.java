/*
Copyright (c) 2003-2004, Dennis M. Sosnoski
All rights reserved.

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

package com.sosnoski.site;

import java.io.IOException;
import java.util.HashMap;

/**
 * Information for a source file. This parses and organizes information from a
 * file associated with a menu item.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class Page
{
	private final String m_title;	// page title
	private final String m_header;	// header from page
    private final String m_description; // description for page
    private final String m_keywords;    // keywords for page
	private final String m_body;	// body text
	
	/**
	 * Constructor. Reads and saves page information.
	 *
	 * @param path path to page including this text
	 * @param pbuf buffer for parsing page text
	 * @exception IOException on access error
	 */
	
	/*package*/ Page(String path, HTMLParseBuffer pbuf) throws IOException {
		
		// process page source to extract components of interest
		String title = null;
        String descript = null;
        String keywords = null;
		String header = null;
		String body = null;
        String tag;
		while ((tag = pbuf.nextTag(true)) != null && !"body".equals(tag)) {
			
			// first check for a head element with possible title or description
			if ("head".equals(tag)) {
				while ((tag = pbuf.nextTag(false)) != null &&
                    !tag.equals("/head")) {
					if ("title".equals(tag)) {
						
						// read content of title element for title and header
                        pbuf.dropTag();
						pbuf.pushMark();
						pbuf.skipToCloseTag("title");
						title = pbuf.read(-pbuf.popMark());
						header = title;
						
					} else if ("meta".equals(tag)) {
                        
                        // check for description with content
                        HashMap amap = pbuf.attributes();
                        String name = (String)amap.get("name");
                        if (name != null) {
                            name = name.toLowerCase();
                            String content = (String)amap.get("content");
                            if ("description".equals(name)) {
                                descript = content;
                            } else if ("keywords".equals(name)) {
                                keywords = content;
                            }
                        }
                        
					} else {
                        pbuf.dropTag();
                    }
				}
			}
		}
        
        // make sure the page body is present
        if ("body".equals(tag)) {
            
            // mark position immediately following <body> tag for accumulation
            pbuf.pushMark();
            StringBuffer buff = new StringBuffer();
            
            // process all tags in body
            while ((tag = pbuf.nextTag(false)) != null &&
                !"/body".equals(tag)) {
                if ("h1".equals(tag)) {
                
                    // append text to start of header
                    int length = -pbuf.popMark();
                    buff.append(pbuf.read(length-"<h1".length()));
                    pbuf.dropTag();
                
                    // read or skip content of header
                    if (header == null) {
                        pbuf.pushMark();
                        pbuf.skipToCloseTag("h1");
                        header = pbuf.read(-pbuf.popMark());
                    } else {
                        pbuf.skipToCloseTag("h1");
                    }
                    
                    // skip past close tag and continue body accumulation
                    pbuf.nextTag(true);
                    pbuf.pushMark();
                }
            }
            
            // append last segment of text to accumulation
            int length = -pbuf.popMark();
            if (tag != null) {
                length -= tag.length() + 1;
            }
            buff.append(pbuf.read(length));
            body = buff.toString();
        
            // handle link substitutions in body
            int base = body.length();
            int last = -1;
            while ((base = body.lastIndexOf('%', base-1)) >= 0) {
            
                // check if possible match with letter following flag character
                if (last >= 0 && Character.isLetter(body.charAt(base+1))) {
                
                    // make sure all remaining characters letters or digits
                    boolean match = true;
                    for (int i = base+2; i < last; i++) {
                        if (!Character.isLetterOrDigit(body.charAt(i))) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                    
                        // handle the actual substitution
                        String key = body.substring(base+1, last);
                        MenuComponent comp = (MenuComponent)MenuComponent.s_idMap.get(key);
                        if (comp == null) {
                            throw new IOException("Target id \"" + key +                                "\" not defined");
                        } else {
                            String dest = comp.getTarget();
                            if (dest == null) {
                                throw new IOException("Target id \"" + key +
                                    "\" does not define a path");
                            } else {
                                String subst =
                                    MenuComponent.relativePath(path, dest);
                                body = body.substring(0, base) +
                                    MenuComponent.relativePath(path, dest) +
                                    body.substring(last+1);
                                last = -1;
                                continue;
                            }
                        }
                    
                    }
                }
            
                // save current flag position for next time through loop
                last = base;
            }
        
            // set values for page
            m_title = title == null ? header : title;
            m_description = descript;
            m_keywords = keywords;
            m_header = header;
            m_body = body;
            
        } else {
            throw new IOException("No body present in page");
        }
	}
	
	/**
	 * Get page body.
	 *
	 * @return page body
	 */
	 
	public String getBody() {
		return m_body;
	}

	/**
	 * Get page header.
	 *
	 * @return page header
	 */
	 
	public String getHeader() {
		return m_header;
	}

    /**
     * Get page description.
     *
     * @return page description
     */
     
    public String getDescription() {
        return m_description;
    }

    /**
     * Get page keywords.
     *
     * @return page keywords
     */
     
    public String getKeywords() {
        return m_keywords;
    }

	/**
	 * Get page title.
	 *
	 * @return page title
	 */
	 
	public String getTitle() {
		return m_title;
	}
}
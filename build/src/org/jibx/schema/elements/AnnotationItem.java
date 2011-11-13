/*
Copyright (c) 2006-2009, Dennis M. Sosnoski.
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

package org.jibx.schema.elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jibx.util.LazyList;
import org.w3c.dom.Node;

/**
 * Annotation item base class. The actual annotation elements are defined as
 * subclasses.
 */
public abstract class AnnotationItem extends SchemaBase
{
    /** Annotation item source. */
    private String m_source;
    
    /** Content of annotation item. */
    private List m_content;

	/**
     * Constructor.
     * 
     * @param type element type
     */
    protected AnnotationItem(int type) {
    	super(type);
        m_content = new ArrayList();
    }
    
    //
    // Base class overrides
    
    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaBase#getChildCount()
     */
    public int getChildCount() {
        return 0;
    }
    
    /* (non-Javadoc)
	 * @see org.jibx.schema.SchemaBase#getChildIterator()
	 */
	public Iterator getChildIterator() {
		return LazyList.EMPTY_ITERATOR;
	}
    
    //
    // Access methods

    /**
     * Get annotation item source.
     * 
     * @return item source
     */
    public String getSource() {
        return m_source;
    }

    /**
     * Set annotation item source.
     * 
     * @param source item source
     */
    public void setSource(String source) {
        m_source = source;
    }

    /**
     * Get annotation item content list. This is a list consisting of DOM
     * nodes.
     * 
     * @return annotation content list
     */
    public final List getContent() {
        return m_content;
    }

    /**
     * Clear annotation item content.
     */
    public final void clearContent() {
        m_content.clear();
    }

    /**
     * Add annotation item content node.
     * 
     * @param node annotation item content node
     */
    public final void addContent(Node node) {
        m_content.add(node);
    }
}
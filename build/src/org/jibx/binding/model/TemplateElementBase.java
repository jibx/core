/*
Copyright (c) 2004-2007, Dennis M. Sosnoski
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

package org.jibx.binding.model;

import java.util.ArrayList;
import java.util.Iterator;

import org.jibx.util.IClass;
import org.jibx.util.StringArray;

/**
 * Model component for elements that define how instances of a particular class
 * are converted to or from XML. This includes both <b>mapping</b> and
 * <b>template</b> elements.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class TemplateElementBase extends ContainerElementBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "class" },
        ContainerElementBase.s_allowedAttributes);
    
    /** Name of handled class. */
    private String m_className;
    
    /** Handled class information. */
    private IClass m_handledClass;

    /** List of child elements. */
    protected ArrayList m_topChildren;

    /** Templates or mappings that can be used in place of this one (as
     * substitution group using mapping, or xsi:type with template). */
    private ArrayList m_extensionTypes;
    
    /**
	 * Constructor.
     * 
     * @param type element type code
	 */
	public TemplateElementBase(int type) {
        super(type);
        m_extensionTypes = new ArrayList();
    }
    
    /**
     * Set mapped class name.
     * 
     * @param name mapped class name
     */
    public void setClassName(String name) {
        m_className = name;
    }
    
    /**
     * Get mapped class name.
     * 
     * @return class name
     */
    public String getClassName() {
        return m_className;
    }

    /**
     * Get handled class information. This call is only meaningful after
     * prevalidation.
     * 
     * @return mapped class information
     */
    public IClass getHandledClass() {
        return m_handledClass;
    }

    /**
     * Add template or mapping which derives from this one.
     * 
     * @param ext derived template or mapping information
     */
    protected void addExtensionType(TemplateElementBase ext) {
        m_extensionTypes.add(ext);
    }
    
    /**
     * Get templates or mappings which derive from this one.
     * 
     * @return list of derived templates or mappings
     */
    public ArrayList getExtensionTypes() {
        return m_extensionTypes;
    }

    /**
     * Check if default template for type. Needs to be implemented by subclasses
     * for common handling.
     * 
     * @return <code>true</code> if default for type, <code>false</code> if not
     */
    public abstract boolean isDefaultTemplate();

    /**
     * Add top-level child element.
     * 
     * @param child element to be added as child of this element
     */
    public void addTopChild(Object child) {
        m_topChildren.add(child);
    }

    /**
     * Get list of top-level child elements.
     * 
     * @return list of child elements, or <code>null</code> if none
     */
    public ArrayList topChildren() {
        return m_topChildren;
    }

    /**
     * Get iterator for top-level child elements.
     * 
     * @return iterator for child elements
     */
    public Iterator topChildIterator() {
        return m_topChildren.iterator();
    }
    
    //
    // Overrides of base class methods
    
    /* (non-Javadoc)
	 * @see org.jibx.binding.model.ElementBase#isOptional()
	 */
	public boolean isOptional() {
		throw new IllegalStateException
            ("Internal error: method should never be called");
	}

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IContextObj#getType()
     */
    public IClass getType() {
        return m_handledClass;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ContainerElementBase#isImplicit()
     */
    public boolean isImplicit() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ContainerElementBase#hasObject()
     */
    public boolean hasObject() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ContainerElementBase#getObjectType()
     */
    public IClass getObjectType() {
        return m_handledClass;
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (m_className == null) {
            vctx.addFatal("Class name is required");
        } else {
            m_handledClass = vctx.getClassInfo(m_className);
            if (m_handledClass == null) {
                vctx.addFatal("Cannot find information for class " +
                    m_className);
            } else {
                super.prevalidate(vctx);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ContainerElementBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // check each child component for compatible type
        ArrayList children = children();
        checkCompatibleChildren(vctx, m_handledClass, children);
        super.validate(vctx);
    }
}
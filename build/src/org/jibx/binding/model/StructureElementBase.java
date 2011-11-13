/*
Copyright (c) 2004-2009, Dennis M. Sosnoski.
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

import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.StringArray;

/**
 * Model component for elements that define the binding structure for an object
 * property. This is the base class for <i>structure</i> and <i>collection</i>
 * elements.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class StructureElementBase
extends ContainerElementBase implements IComponent
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new StringArray(PropertyAttributes.s_allowedAttributes,
        NameAttributes.s_allowedAttributes),
        ContainerElementBase.s_allowedAttributes);
    
    /** Property attributes information for nesting. */
    private PropertyAttributes m_propertyAttrs;

    /** Name attributes information for nesting. */
    private NameAttributes m_nameAttrs;
    
    /** Flag for child of <b>collection</b> element (only meaningful after
     prevalidation). */
    private boolean m_collectionItem;
    
	/**
	 * Constructor.
     * 
     * @param type element type code
	 */
    protected StructureElementBase(int type) {
        super(type);
        m_nameAttrs = new NameAttributes();
        m_propertyAttrs = new PropertyAttributes();
	}
    
    //
    // Name attribute delegate methods

    /**
     * Get name.
     * 
     * @return name text
     */
    public String getName() {
        return m_nameAttrs.getName();
    }

    /**
     * Set name.
     * 
     * @param name text for name
     */
    public void setName(String name) {
        m_nameAttrs.setName(name);
    }

    /**
     * Get specified namespace URI.
     * 
     * @return namespace URI (<code>null</code> if not set)
     */
    public String getUri() {
        return m_nameAttrs.getUri();
    }

    /**
     * Set namespace URI.
     * 
     * @param uri namespace URI (<code>null</code> if not set)
     */
    public void setUri(String uri) {
        m_nameAttrs.setUri(uri);
    }

    /**
     * Get specified namespace prefix.
     * 
     * @return namespace prefix (<code>null</code> if not set)
     */
    public String getPrefix() {
        return m_nameAttrs.getPrefix();
    }

    /**
     * Set namespace prefix.
     * 
     * @param prefix namespace prefix (<code>null</code> if not set)
     */
    public void setPrefix(String prefix) {
        m_nameAttrs.setPrefix(prefix);
    }
    
    /**
     * Get effective namespace information. This call is only meaningful after
     * validation.
     * 
     * @return effective namespace information
     */
    public NamespaceElement getNamespace() {
        return m_nameAttrs.getNamespace();
    }
    
    //
    // Property attribute delegate methods
    
    /**
     * Get usage name.
     * 
     * @return usage name
     */
    public String getUsageName() {
        return m_propertyAttrs.getUsageName();
    }
    
    /**
     * Get usage value. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return usage value
     */
    public int getUsage() {
        return m_propertyAttrs.getUsage();
    }
    
    /**
     * Set usage name.
     * 
     * @param name usage name
     */
    public void setUsageName(String name) {
        m_propertyAttrs.setUsageName(name);
    }
    
    /**
     * Set usage value.
     * 
     * @param use value
     */
    public void setUsage(int use) {
        m_propertyAttrs.setUsage(use);
    }
    
    /**
     * Check if property is defined. This method is only meaningful after
     * a call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return <code>true</code> if property defined, <code>false</code> if not
     */
    public boolean hasProperty() {
        return m_propertyAttrs.hasProperty();
    }
    
    /**
     * Check if property is flag only. This method is only meaningful after
     * a call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return <code>true</code> if flag property, <code>false</code> if not
     */
    public boolean isFlagOnly() {
        return m_propertyAttrs.isFlagOnly();
    }
    
    /**
     * Get declared type name.
     * 
     * @return type name (or <code>null</code> if none)
     */
    public String getDeclaredType() {
        return m_propertyAttrs.getDeclaredType();
    }
    
    /**
     * Set declared type name.
     * 
     * @param type name (or <code>null</code> if none)
     */
    public void setDeclaredType(String type) {
        m_propertyAttrs.setDeclaredType(type);
    }
    
    /**
     * Get field name.
     * 
     * @return field name (or <code>null</code> if none)
     */
    public String getFieldName() {
        return m_propertyAttrs.getFieldName();
    }
    
    /**
     * Get field information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return field information (or <code>null</code> if none)
     */
    public IClassItem getField() {
        return m_propertyAttrs.getField();
    }
    
    /**
     * Set field name.
     * 
     * @param field field name (or <code>null</code> if none)
     */
    public void setFieldName(String field) {
        m_propertyAttrs.setFieldName(field);
    }
    
    /**
     * Get test method name.
     * 
     * @return test method name (or <code>null</code> if none)
     */
    public String getTestName() {
        return m_propertyAttrs.getTestName();
    }
    
    /**
     * Get test method information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return test method information (or <code>null</code> if none)
     */
    public IClassItem getTest() {
        return m_propertyAttrs.getTest();
    }
    
    /**
     * Set test method name.
     * 
     * @param test test method name (or <code>null</code> if none)
     */
    public void setTestName(String test) {
        m_propertyAttrs.setTestName(test);
    }
    
    /**
     * Get flag method information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return flag method information (or <code>null</code> if none)
     */
    public IClassItem getFlag() {
        return m_propertyAttrs.getFlag();
    }
    
    /**
     * Set flag method name.
     * 
     * @param flag flag method name (or <code>null</code> if none)
     */
    public void setFlagName(String flag) {
        m_propertyAttrs.setFlagName(flag);
    }
    
    /**
     * Get get method name.
     * 
     * @return get method name (or <code>null</code> if none)
     */
    public String getGetName() {
        return m_propertyAttrs.getGetName();
    }
    
    /**
     * Get get method information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return get method information (or <code>null</code> if none)
     */
    public IClassItem getGet() {
        return m_propertyAttrs.getGet();
    }
    
    /**
     * Get type for value loaded to stack. This call is only meaningful after a
     * call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return get value type (or <code>null</code> if none)
     */
    public IClass getGetType() {
        return m_propertyAttrs.getGetType();
    }
    
    /**
     * Set get method name.
     * 
     * @param get get method name (or <code>null</code> if none)
     */
    public void setGetName(String get) {
        m_propertyAttrs.setGetName(get);
    }
    
    /**
     * Get set method name.
     * 
     * @return set method name (or <code>null</code> if none)
     */
    public String getSetName() {
        return m_propertyAttrs.getSetName();
    }
    
    /**
     * Get set method information. This call is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return set method information (or <code>null</code> if none)
     */
    public IClassItem getSet() {
        return m_propertyAttrs.getSet();
    }
    
    /**
     * Get type for value stored from stack. This call is only meaningful after
     * a call to {@link #prevalidate(ValidationContext)}.
     * 
     * @return set value type (or <code>null</code> if none)
     */
    public IClass getSetType()  {
        return m_propertyAttrs.getSetType();
    }
    
    /**
     * Set set method name.
     * 
     * @param set set method name (or <code>null</code> if none)
     */
    public void setSetName(String set) {
        m_propertyAttrs.setSetName(set);
    }
    
    /**
     * Check if this value implicitly uses the containing object. This call
     * is only meaningful after a call to
     * {@link #prevalidate(ValidationContext)}.
     * 
     * @return <code>true</code> if using the containing object,
     * <code>false</code> if own value
     */
    public boolean isImplicit() {
        return m_propertyAttrs.isImplicit();
    }
    
    //
    // Implementation methods

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#isOptional()
     */
    public boolean isOptional() {
        return m_propertyAttrs.getUsage() == PropertyAttributes.OPTIONAL_USAGE;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ContainerElementBase#hasObject()
     */
    public boolean hasObject() {
        return m_collectionItem || !isImplicit() || getDeclaredType() != null;
    }
    
    //
    // Implementation of methods from IComponent interface (used in extensions)

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#hasAttribute()
     */
    public boolean hasAttribute() {
        if (m_nameAttrs.getName() != null) {
            return false;
        } else {
            return getAttributeComponents().size() > 0;
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#hasContent()
     */
    public boolean hasContent() {
        if (m_nameAttrs.getName() != null) {
            return true;
        } else {
            return getContentComponents().size() > 0;
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#hasName()
     */
    public boolean hasName() {
        return m_nameAttrs.getName() != null;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.IComponent#getType()
     */
    public IClass getType() {
        return m_propertyAttrs.getType();
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ContainerElementBase#getType()
     */
    public IClass getObjectType() {
        return getType();
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        m_nameAttrs.prevalidate(vctx);
        m_propertyAttrs.prevalidate(vctx);
        m_collectionItem = vctx.getParentContainer().type() == COLLECTION_ELEMENT;
        if (!vctx.isSkipped(this)) {
            super.prevalidate(vctx);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // validate the attribute groups
        m_nameAttrs.validate(vctx);
        m_propertyAttrs.validate(vctx);
        if (!vctx.isSkipped(this)) {
            
            // check for way of constructing object instance on input
            if (m_propertyAttrs.hasProperty() && children().size() > 0) {
                verifyConstruction(vctx, m_propertyAttrs.getType());
            }
            
            // check use of text values in children of structure with name
            if (hasName()) {
                SequenceVisitor visitor = new SequenceVisitor(this, vctx);
                TreeContext tctx = vctx.getChildContext();
                tctx.tourTree(this, visitor);
            }
            
            // finish with superclass validation
            super.validate(vctx);
        }
    }
}
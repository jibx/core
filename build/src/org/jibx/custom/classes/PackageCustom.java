/*
 * Copyright (c) 2007-2008, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.custom.classes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.util.IClassLocator;
import org.jibx.util.StringArray;

/**
 * Package customization information.
 * 
 * @author Dennis M. Sosnoski
 */
public class PackageCustom extends NestingBase implements IApply
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "name" }, NestingBase.s_allowedAttributes);
    
    /** Element name in XML customization file. */
    public static final String ELEMENT_NAME = "package";
    
    // values specific to package level
    private String m_simpleName;
    
    private String m_fullName;
    
    // flag for namespace derivation done (from apply() method)
    private boolean m_fixedNamespace;
    
    // map from simple name to class information for classes in package
    private Map m_classMap;
    
    /**
     * Constructor. This has package access so that it can be used from the {@link GlobalCustom} class.
     * 
     * @param simple simple package name
     * @param full fully-qualified package name
     * @param parent
     */
    /* package */PackageCustom(String simple, String full, NestingBase parent) {
        super(parent);
        m_simpleName = simple;
        m_fullName = full;
        m_classMap = new HashMap();
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Get fully-qualified package name.
     * 
     * @return package name (empty string if default package)
     */
    public String getName() {
        return m_fullName;
    }
    
    /**
     * Get existing information for class in this package.
     * 
     * @param name simple class name (without package)
     * @return class information (<code>null</code> if no existing information)
     */
    public ClassCustom getClassCustomization(String name) {
        return (ClassCustom)m_classMap.get(name);
    }
    
    /**
     * Add information for class in this package. This just creates the basic class information structure and returns
     * it, without populating the class details.
     * 
     * @param name simple class name (without package)
     * @return class information (<code>null</code> if no existing information)
     */
    /* package */ClassCustom addClassCustomization(String name) {
        ClassCustom clas = new ClassCustom(this, name);
        m_classMap.put(name, clas);
        return clas;
    }
    
    /**
     * Fix the namespace for this package. If there's a parent package and that package namespace has not yet been
     * fixed, this first fixes the parent package namespace by means of a recursive call. 
     */
    public void fixNamespace() {
        if (!m_fixedNamespace) {
            
            // first fix parent package namespace
            SharedNestingBase parent = getParent();
            if (parent instanceof PackageCustom) {
                ((PackageCustom)parent).fixNamespace();
            }
            
            // derive namespace from parent setting, if not specified
            String ns = getSpecifiedNamespace();
            if (ns == null) {
                String pns = parent.getNamespace();
                ns = deriveNamespace(pns, m_fullName, getNamespaceStyle());
            }
            setNamespace(ns);
            m_fixedNamespace = true;
        }
    }
    
    /**
     * Apply customizations to default values. This fills in the information for classes in this package by deriving
     * information for fields or properties in each class. This is intended to be used once, after customizations have
     * been unmarshalled.
     * 
     * @param loc class locator
     */
    public void apply(IClassLocator loc) {
        
        // apply customizations for all contained classes
        for (Iterator iter = m_classMap.values().iterator(); iter.hasNext();) {
            ((ClassCustom)iter.next()).apply(loc);
        }
    }
}
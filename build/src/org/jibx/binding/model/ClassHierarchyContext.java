/*
Copyright (c) 2005-2009, Dennis M. Sosnoski
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.jibx.util.IClass;

/**
 * Context for components using a hierarchy of definitions based on class type.
 * This is used to track conversion definitions in the form of <b>format</b> and
 * <b>template</b> elements. The access methods take the different levels of
 * nesting into account, automatically delegating to the containing context (if
 * defined) when a lookup fails.
 *
 * @author Dennis M. Sosnoski
 */
public class ClassHierarchyContext
{
    /** Link to containing context. */
    private final ClassHierarchyContext m_outerContext;
    
    /** Map from type name to binding component. */
    private HashMap m_typeToComponentMap;
    
    /** Set of compatible type names. */
    private HashSet m_compatibleTypeSet;
    
    /** Map from format names to <code>String</code> conversions (lazy create). */
    private HashMap m_nameToComponentMap;
    
    /**
     * Constructor.
     * 
     * @param outer containing context (<code>null</code> if at root of tree)
     */
    protected ClassHierarchyContext(ClassHierarchyContext outer) {
        m_outerContext = outer;
        m_typeToComponentMap = new HashMap();
        m_compatibleTypeSet = new HashSet();
        m_nameToComponentMap = new HashMap();
    }
    
    /**
     * Get containing context.
     * 
     * @return containing context information (<code>null</code> if at root of
     * tree)
     */
    public ClassHierarchyContext getContaining() {
        return m_outerContext;
    }
    
    /**
     * Accumulate all the interfaces implemented (both directly and indirectly)
     * by a class.
     *
     * @param clas
     * @param intfset set of interfaces
     */
    private void accumulateInterfaces(IClass clas, Set intfset) {
        String[] interfaces = clas.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            String name = interfaces[i];
            if (!intfset.contains(name)) {
                intfset.add(name);
                IClass iclas = clas.getLocator().getClassInfo(name);
                if (iclas != null) {
                    accumulateInterfaces(iclas, intfset);
                }
            }
        }
    }
    
    /**
     * Add typed component to set defined at this level. This associates the
     * component with the type for class hierarchy-based lookups.
     *
     * @param clas class information to be associated with component
     * @param comp definition component to be added
     * @param vctx validation context in use
     */
    public void addTypedComponent(IClass clas, ElementBase comp,
        ValidationContext vctx) {
        String type = clas.getName();
        if (m_typeToComponentMap.put(type, comp) == null) {
            
            // new type, add all interfaces and supertypes to compatible set
            IClass sclas = clas;
            do {
                m_compatibleTypeSet.add(sclas.getName());
                if (vctx.isLookupSupported()) {
                    accumulateInterfaces(sclas, m_compatibleTypeSet);
                }
            } while ((sclas = sclas.getSuperClass()) != null);
            
        } else {
            vctx.addError("Duplicate conversion defined for type " + type,
                comp);
        }
    }
    
    /**
     * Add named component to set defined at this level.
     * TODO: Make this use qname instead of text
     *
     * @param label name to be associated with component
     * @param comp definition component to be added
     * @param vctx validation context in use
     */
    public void addNamedComponent(String label, ElementBase comp,
        ValidationContext vctx) {
        if (m_nameToComponentMap.put(label, comp) != null) {
            if (label.startsWith("{}")) {
                label = label.substring(2);
            }
            vctx.addError("Duplicate name " + label, comp);
        }
    }

    /**
     * Get specific binding component for type. Looks for an exact match on the
     * type name, checking the containing definitions if a matching component is
     * not found at this level.
     *
     * @param name fully qualified class name to be converted
     * @return binding component for class, or <code>null</code> if not
     * found
     */
    public ElementBase getSpecificComponent(String name) {
        ElementBase comp = null;
        if (m_typeToComponentMap != null) {
            comp = (ElementBase)m_typeToComponentMap.get(name);
        }
        if (comp == null && m_outerContext != null) {
            comp = m_outerContext.getSpecificComponent(name);
        }
        return comp;
    }
    
    /**
     * Get named binding component definition. Finds the component with the
     * supplied name, checking the containing definitions if the component is
     * not found at this level.
     *
     * @param name component name to be found
     * @return binding component with name, or <code>null</code> if not
     * found
     */
    public ElementBase getNamedComponent(String name) {
        ElementBase comp = null;
        if (m_nameToComponentMap != null) {
            comp = (ElementBase)m_nameToComponentMap.get(name);
        }
        if (comp == null && m_outerContext != null) {
            comp = m_outerContext.getNamedComponent(name);
        }
        return comp;
    }

    /**
     * Get best binding component for class. Finds the component based on a
     * fully qualified class name. If a specific component for the actual
     * class is not found (either in this or a containing level) this returns
     * the most specific superclass component.
     *
     * @param clas information for target class
     * @return binding component definition for class, or <code>null</code> if
     * none found
     */
    public ElementBase getMostSpecificComponent(IClass clas) {
        ElementBase comp = getSpecificComponent(clas.getName());
        while (comp == null) {
            IClass sclas = clas.getSuperClass();
            if (sclas == null) {
                break;
            }
            clas = sclas;
            comp = getSpecificComponent(clas.getName());
        }
        return comp;
    }

    /**
     * Checks if a class is compatible with one or more components. If a
     * specific component for the actual class is not found (either in this or a
     * containing level) this checks for components that handle subclasses or
     * implementations of the class.
     *
     * @param clas information for target class
     * @return <code>true</code> if compatible type, <code>false</code> if not
     */
    public boolean isCompatibleType(IClass clas) {
        if (m_compatibleTypeSet.contains(clas.getName())) {
            return true;
        } else if (m_outerContext != null) {
            return m_outerContext.isCompatibleType(clas);
        } else {
            return false;
        }
    }
}
/*
Copyright (c) 2008-2009, Dennis M. Sosnoski.
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

package org.jibx.binding.def;

import java.util.ArrayList;

import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.runtime.JiBXException;

/**
 * Concrete mapping defined by a precompiled binding. This is constructed from
 * the binding factory information for a binding which is referenced using a
 * precompiled='true' attribute.
 *
 * @author Dennis M. Sosnoski
 */
public class PrecompiledConcreteMapping implements IMapping
{
    /** Class linked to mapping. */
    private final ClassFile m_class;
    
    /** Name of this mapping in binding.. */
    private final String m_mappingName;

    /** Mapped element name. */
    private final NameDefinition m_name;
    
    /** Containing context for definition. */
    private final IContainer m_container;
    
    /** Binding factory name used for activating namespace translation on
     marshalling (<code>null</code> if translation not required). */
    private final String m_factoryName;
    
    /** Marshaller class. */
    private ClassFile m_marshaller;

    /** Unmarshaller class. */
    private ClassFile m_unmarshaller;
    
    /**
     * Constructor.
     *
     * @param mapname mapping name in binding
     * @param type bound class name
     * @param name element name definition
     * @param marname marshaller class name (<code>null</code> if none)
     * @param umarname unmarshaller class name (<code>null</code> if none)
     * @param parent containing context
     * @param xlated translated namespaces for binding flag
     * @param factname binding factory name
     * @throws JiBXException if class definition not found
     */
    public PrecompiledConcreteMapping(String mapname, String type,
        NameDefinition name, String marname, String umarname, IContainer parent,
        boolean xlated, String factname) throws JiBXException {
        m_class = ClassCache.requireClassFile(type);
        m_mappingName = mapname;
        parent.getBindingRoot().addMappingName(mapname);
        m_name = name;
        m_container = parent;
        m_factoryName = xlated ? factname : null;
        if (marname == null) {
            m_marshaller = null;
        } else {
            m_marshaller = ClassCache.requireClassFile(marname);
        }
        if (umarname == null) {
            m_unmarshaller = null;
        } else {
            m_unmarshaller = ClassCache.requireClassFile(umarname);
        }
    }
    
    /**
     * Links extension mappings to their base mappings. For precompiled mappings
     * this does nothing.
     *
     * @throws JiBXException if error in linking
     */
    public void linkMappings() throws JiBXException {
    }
    
    //
    // IMapping interface method definitions
    
    public String getBoundType() {
        return m_class.getName();
    }
    
    public String getReferenceType() {
        return m_mappingName;
    }
    
    public IComponent getImplComponent() {
        return null;
    }
    
    public ClassFile getMarshaller() {
        return m_marshaller;
    }
    
    public ClassFile getUnmarshaller() {
        return m_unmarshaller;
    }
    
    public NameDefinition getName() {
        return m_name;
    }

    public void addNamespace(NamespaceDefinition ns) throws JiBXException {
        throw new IllegalArgumentException
            ("Internal error - namespace cannot be added to precompiled mapping");
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isBase() {
        return false;
    }

    public void addExtension(MappingDefinition mdef) throws JiBXException {
        throw new IllegalArgumentException
            ("Internal error - extension cannot be added to precompiled mapping");
    }
    
    public IComponent buildRef(IContainer parent, IContextObj objc, String type,
        PropertyDefinition prop) throws JiBXException {
        DirectObject dobj = new DirectObject(m_container, null, m_class, false,
            m_marshaller, m_unmarshaller, m_mappingName, null, m_factoryName);
        return new DirectProperty(prop, dobj);
    }
    
    public ArrayList getNamespaces() {
        return null;
    }

    public void generateCode(boolean force) throws JiBXException {
    }

    public NameDefinition getWrapperName() {
        return m_name;
    }
    
    public ITypeBinding getBinding() {
        return null;
    }

    public String getMappingName() {
        return m_mappingName;
    }

    public String getTypeName() {
        return null;
    }
    
    //
    // IMapping interface method definitions

    public void setLinkages() throws JiBXException {
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("precompiled mapping class " + m_class.getName());
        System.out.println(" ('" + getMappingName() + "')");
    }
}
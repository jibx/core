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
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;

/**
 * Abstract mapping defined by a precompiled binding. This is constructed from
 * the binding factory information for a binding which is referenced using a
 * precompiled='true' attribute.
 *
 * @author Dennis M. Sosnoski
 */
public class PrecompiledAbstractMapping implements IMapping
{
    /** Namespace index translation required flag. */
    private final boolean m_translated;
    
    /** Class linked to mapping. */
    private final ClassFile m_class;
    
    /** Qualified type name. */
    private final String m_typeName;
    
    /** Binding structure defining the mapping. */
    private final PrecompiledBinding m_binding;
    
    /** Reference type of mapping, as fully qualified class name. */
    private final String m_referenceType;
    
    /** Namespaces used by this mapping. */
    private final ArrayList m_namespaces;
    
    /** Containing context for definition. */
    private final IContainer m_parent;
    
    /** Name used for mapping in binding tables. */
    private final String m_mappingName;
    
    /**
     * Constructor.
     *
     * @param type bound class name
     * @param tname qualified type name for abstract mapping (<code>null</code>
     * if none)
     * @param mapname abstract mapping name in binding
     * @param index abstract mapping index in binding
     * @param factory binding factory for mapping information
     * @param nsxlate namespace index translation table (<code>null</code> if
     * none)
     * @param parent containing context
     * @throws JiBXException if class definition not found
     */
    public PrecompiledAbstractMapping(String type, String tname, String mapname,
        int index, IBindingFactory factory, int[] nsxlate, IContainer parent)
        throws JiBXException {
        m_translated = nsxlate != null;
        m_class = ClassCache.requireClassFile(type);
        m_typeName = tname;
        parent.getBindingRoot().addMappingName((tname == null) ? type : tname);
        m_binding = new PrecompiledBinding(index, factory.getAbstractMappings(),
            m_translated, factory.getClass().getName());
        m_referenceType = type;
        m_namespaces = new ArrayList();
        m_parent = parent;
        m_mappingName = mapname;
        String[] uris = factory.getNamespaces();
        int[] nss = factory.getAbstractMappingNamespaces(index);
        for (int i = 0; i < nss.length; i++) {
            int nsi = nss[i];
            String uri = uris[nsi];
            NamespaceDefinition def = new NamespaceDefinition(uri, null,
                NamespaceDefinition.NODEFAULT_USAGE);
            if (nsxlate != null) {
                nsi = nsxlate[nsi];
            }
            def.setIndex(nsi);
            m_namespaces.add(def);
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
        return m_referenceType;
    }
    
    public IComponent getImplComponent() {
        return m_binding;
    }
    
    public ClassFile getMarshaller() {
        return null;
    }
    
    public ClassFile getUnmarshaller() {
        return null;
    }
    
    public NameDefinition getName() {
        return null;
    }

    public void addNamespace(NamespaceDefinition ns) throws JiBXException {
        throw new IllegalArgumentException
            ("Internal error - namespace cannot be added to precompiled mapping");
    }

    public boolean isAbstract() {
        return true;
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
        if (prop.isThis()) {
            
            // directly incorporate base mapping definition
            return new BaseMappingWrapper(m_binding);
            
        } else {
            
            // create reference to use mapping definition directly
            ComponentProperty comp =
                new ComponentProperty(prop, m_binding, false);
            if (m_binding.getAttributeUnmarshalMethod() == null &&
                m_binding.getContentUnmarshalMethod() == null) {
                comp.setForceUnmarshal(true);
            }
            return comp;
            
        }
    }
    
    public ArrayList getNamespaces() {
        return m_namespaces;
    }

    public void generateCode(boolean force) throws JiBXException {
    }

    public NameDefinition getWrapperName() {
        return null;
    }
    
    public ITypeBinding getBinding() {
        return m_binding;
    }
    
    public String getMappingName() {
        return m_mappingName;
    }

    public String getTypeName() {
        return m_typeName;
    }
    
    //
    // IMapping interface method definitions

    public void setLinkages() throws JiBXException {
        BindingDefinition binding = m_parent.getBindingRoot();
        for (int i = 0; i < m_namespaces.size(); i++) {
            NamespaceDefinition def = (NamespaceDefinition)m_namespaces.get(i);
            def.setPrefix(binding.getPrefix(def.getUri()));
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("precompiled abstract mapping " + m_typeName +
            " for class " + m_class.getName());
    }
}
/*
Copyright (c) 2003-2008, Dennis M. Sosnoski.
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

import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.ClassFile;
import org.jibx.runtime.JiBXException;

/**
 * Direct mapping using supplied marshaller and unmarshaller.
 *
 * @author Dennis M. Sosnoski
 */
public class MappingDirect extends MappingBase
{
    /** Direct mapping implementation. */
    private final DirectObject m_mappingImpl;
    
    /** Class file to use for added code. */
    private final BoundClass m_boundClass;
    
    /** Flag for abstract mapping. */
    private final boolean m_isAbstract;
    
    /** Flag for code added to class (if appropriate). */
    private boolean m_isGenerated;

    /**
     * Constructor.
     *
     * @param contain containing binding definition structure
     * @param type bound class name
     * @param tname qualified type name (<code>null</code> if not specified)
     * @param dir direct object information
     * @param abs abstract mapping flag
     * @throws JiBXException on mapping definition conflict
     */
    public MappingDirect(IContainer contain, String type, String tname,
        DirectObject dir, boolean abs) throws JiBXException {
        super(contain, type, tname, dir);
        m_mappingImpl = dir;
        m_boundClass = BoundClass.getInstance(type, null);
        m_isAbstract = abs;
    }

    /**
     * Get the mapped class information. This implements the method used by the
     * base class.
     *
     * @return information for mapped class
     */
    public BoundClass getBoundClass() {
        return m_boundClass;
    }
    
    //
    // IMapping interface method definitions
    
    public String getBoundType() {
        return m_mappingImpl.getTargetClass().getName();
    }
    
    public String getReferenceType() {
        return getBoundType();
    }
    
    public IComponent getImplComponent() {
        return m_component;
    }
    
    public ClassFile getMarshaller() throws JiBXException {
        return m_mappingImpl.getMarshaller();
    }
    
    public ClassFile getUnmarshaller() throws JiBXException {
        return m_mappingImpl.getUnmarshaller();
    }
    
    public NameDefinition getName() {
        return m_mappingImpl.getWrapperName();
    }

    public void addNamespace(NamespaceDefinition ns) {
        throw new IllegalStateException
            ("Internal error: no namespace definition possible");
    }

    public boolean isAbstract() {
        return m_isAbstract;
    }

    public boolean isBase() {
        return false;
    }

    public void addExtension(MappingDefinition mdef) {
        throw new IllegalStateException
            ("Internal error: no extension possible");
    }
    
    public IComponent buildRef(IContainer parent, IContextObj objc, String type,
        PropertyDefinition prop) throws JiBXException {
        return new DirectProperty(prop, m_mappingImpl);
    }
    
    public ArrayList getNamespaces() {
        return null;
    }
    
    public void generateCode(boolean force) throws JiBXException {
        if (!m_isGenerated) {
            if (m_boundClass.isLimitedDirectAccess()) {
                addIMarshallableMethod();
                addIUnmarshallableMethod();
            }
            m_isGenerated = true;
        }
    }
    
    public NameDefinition getWrapperName() {
        return null;
    }

    public void setLinkages() throws JiBXException {
        m_mappingImpl.setLinkages();
    }
    
    public ITypeBinding getBinding() {
        return null;
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.println("mapping direct " +
             m_mappingImpl.getTargetClass().getName());
    }
}
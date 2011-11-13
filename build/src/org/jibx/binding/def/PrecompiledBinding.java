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

import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.ContextMethodBuilder;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;

/**
 * Linkage to object marshalling/unmarshalling code for a class handled by an
 * abstract mapping in a precompiled binding. This is constructed from the
 * information in the binding factory, then used in the actual code generation
 * processing to call the methods when referenced.
 *
 * @author Dennis M. Sosnoski
 */
public class PrecompiledBinding implements IComponent, ITypeBinding
{
    //
    // Constants and such related to code generation.
    
    private static final String MARSHALLING_CONTEXT =
        "org.jibx.runtime.impl.MarshallingContext";
    private static final String UNMARSHALLING_CONTEXT =
        "org.jibx.runtime.impl.UnmarshallingContext";
    private static final String MARSHALLING_CONTEXT_SIG =
        "Lorg/jibx/runtime/impl/MarshallingContext;";
    private static final String UNMARSHALLING_CONTEXT_SIG =
        "Lorg/jibx/runtime/impl/UnmarshallingContext;";
    private static final String PRESENCE_TEST_SIG =
        "(" + UNMARSHALLING_CONTEXT_SIG + ")Z";
    private static final String PUSH_NAMESPACES_METHOD =
        "org.jibx.runtime.IMarshallingContext.pushNamespaces";
    private static final String PUSH_NAMESPACES_SIG =
        "(Ljava/lang/String;)V";
    private static final String POP_NAMESPACES_METHOD =
        "org.jibx.runtime.IMarshallingContext.popNamespaces";
    private static final String POP_NAMESPACES_SIG = "()V";

    //
    // Actual instance data.
    
    /** Class handled by binding. */
    private final ClassFile m_class;
    
    /** New instance method name. */
    private final String m_newInstanceName;
    
    /** Complete method name (<code>null</code> if none). */
    private final String m_completeName;
    
    /** Prepare method name (<code>null</code> if none). */
    private final String m_prepareName;
    
    /** Name for attribute presence test method (<code>null</code> if none). */
    private final String m_attributePresenceName;
    
    /** Name for content presence test method (<code>null</code> if none). */
    private final String m_contentPresenceName;
    
    /** Name for unmarshal attribute method (<code>null</code> if none). */
    private final String m_unmarshalAttributeName;
    
    /** Name for unmarshal content method (<code>null</code> if none). */
    private final String m_unmarshalContentName;
    
    /** Name for  marshal attribute method (<code>null</code> if none). */
    private final String m_marshalAttributeName;
    
    /** Name for  marshal content method (<code>null</code> if none). */
    private final String m_marshalContentName;
    
    /** Signature used for unmarshalling (and new instance) methods. */
    private final String m_unmarshalSignature;
    
    /** Signature used for complete method. */
    private final String m_completeSignature;
    
    /** Signature used for marshalling (and prepare) methods. */
    private final String m_marshalSignature;
    
    /** Binding factory name used for activating namespace translation on
     marshalling (<code>null</code> if translation not required). */
    private final String m_factoryName;

    /**
     * Constructor.
     *
     * @param index abstract mapping index in binding
     * @param abmaps abstract mapping information from binding
     * @param xlated translated namespaces for binding flag
     * @param factname binding factory name
     * @throws JiBXException on error loading class information
     */
    public PrecompiledBinding(int index, String[][] abmaps, boolean xlated,
        String factname) throws JiBXException {
        m_class = ClassCache.requireClassFile(abmaps[IBindingFactory.ABMAP_CLASSNAME_INDEX][index]);
        m_newInstanceName = abmaps[IBindingFactory.ABMAP_CREATEMETH_INDEX][index];
        m_completeName = abmaps[IBindingFactory.ABMAP_COMPLETEMETH_INDEX][index];
        m_prepareName = abmaps[IBindingFactory.ABMAP_PREPAREMETH_INDEX][index];
        m_attributePresenceName = abmaps[IBindingFactory.ABMAP_ATTRPRESMETH_INDEX][index];
        m_contentPresenceName = abmaps[IBindingFactory.ABMAP_CONTPRESMETH_INDEX][index];
        m_unmarshalAttributeName = abmaps[IBindingFactory.ABMAP_ATTRUMARMETH_INDEX][index];
        m_unmarshalContentName = abmaps[IBindingFactory.ABMAP_CONTUMARMETH_INDEX][index];
        m_marshalAttributeName = abmaps[IBindingFactory.ABMAP_ATTRMARMETH_INDEX][index];
        m_marshalContentName = abmaps[IBindingFactory.ABMAP_CONTMARMETH_INDEX][index];
        String classig = m_class.getSignature();
        String basesig = "(" + classig + UNMARSHALLING_CONTEXT_SIG + ")";
        m_unmarshalSignature = basesig + classig;
        m_completeSignature = basesig + "V";
        m_marshalSignature = "(" + classig + MARSHALLING_CONTEXT_SIG + ")V";
        m_factoryName = xlated ? factname : null;
    }
    
    //
    // IComponent interface method definitions
    
    public boolean isOptional() {
        return false;
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_unmarshalAttributeName != null) {
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCallStatic(m_unmarshalAttributeName, m_unmarshalSignature);
        }
    }

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_marshalAttributeName != null) {
            if (m_factoryName != null) {
                mb.loadContext();
                mb.appendLoadConstant(m_factoryName);
                mb.appendCallInterface(PUSH_NAMESPACES_METHOD,
                    PUSH_NAMESPACES_SIG);
            }
            mb.loadContext(MARSHALLING_CONTEXT);
            mb.appendCallStatic(m_marshalAttributeName, m_marshalSignature);
            if (m_factoryName != null) {
                mb.loadContext();
                mb.appendCallInterface(POP_NAMESPACES_METHOD,
                    POP_NAMESPACES_SIG);
            }
        }
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_unmarshalContentName != null) {
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCallStatic(m_unmarshalContentName, m_unmarshalSignature);
        }
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_marshalContentName != null) {
            if (m_factoryName != null) {
                mb.loadContext();
                mb.appendLoadConstant(m_factoryName);
                mb.appendCallInterface(PUSH_NAMESPACES_METHOD,
                    PUSH_NAMESPACES_SIG);
            }
            mb.loadContext(MARSHALLING_CONTEXT);
            mb.appendCallStatic(m_marshalContentName, m_marshalSignature);
            if (m_factoryName != null) {
                mb.loadContext();
                mb.appendCallInterface(POP_NAMESPACES_METHOD,
                    POP_NAMESPACES_SIG);
            }
        }
    }
    
    public void genNewInstance(ContextMethodBuilder mb) throws JiBXException {
        if (m_newInstanceName != null) {
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCallStatic(m_newInstanceName, m_unmarshalSignature);
        }
    }
    
    public void genAttrPresentTest(ContextMethodBuilder mb) throws JiBXException {
        if (m_attributePresenceName != null) {
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCallStatic(m_attributePresenceName, PRESENCE_TEST_SIG);
        }
    }

    public void genContentPresentTest(ContextMethodBuilder mb) throws JiBXException {
        if (m_contentPresenceName != null) {
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCallStatic(m_contentPresenceName, PRESENCE_TEST_SIG);
        }
    }

    public String getType() {
        return m_class.getName();
    }

    public boolean hasId() {
        return false;
    }

    public void genLoadId(ContextMethodBuilder mb) {
        throw new IllegalStateException("Internal error: id not usable with abstract precompiled binding");
    }
    
    public void setLinkages() {}
    
    public NameDefinition getWrapperName() {
        return null;
    }

    public boolean hasAttribute() {
        return m_unmarshalAttributeName != null ||
            m_marshalAttributeName != null;
    }

    public boolean hasContent() {
        return m_unmarshalContentName != null || m_marshalContentName != null;
    }
    
    //
    // ITypeBinding interface method definitions

    public String getAttributeMarshalMethod() throws JiBXException {
        return m_marshalAttributeName;
    }

    public String getAttributePresentTestMethod() throws JiBXException {
        return m_attributePresenceName;
    }

    public String getAttributeUnmarshalMethod() throws JiBXException {
        return m_unmarshalAttributeName;
    }

    public String getCompleteMethod() throws JiBXException {
        return m_completeName;
    }

    public String getContentMarshalMethod() throws JiBXException {
        return m_marshalContentName;
    }

    public String getContentPresentTestMethod() throws JiBXException {
        return m_contentPresenceName;
    }

    public String getContentUnmarshalMethod() throws JiBXException {
        return m_unmarshalContentName;
    }

    public String getCreateMethod() throws JiBXException {
        return m_newInstanceName;
    }

    public String getPrepareMethod() throws JiBXException {
        return m_prepareName;
    }

    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.println("precompiled binding for " + m_class.getName());
    }
}
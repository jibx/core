/*
Copyright (c) 2003-2005, Dennis M. Sosnoski
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

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Linkage to generic object with defined marshaller and/or unmarshaller. This
 * provides methods used to generate code for marshalling and unmarshalling
 * objects of types unknown at binding time, so long as they have mappings
 * defined.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class DirectGeneric implements IComponent
{
    //
    // Constants and such related to code generation.

    private static final String ISEND_METHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.isEnd";
    private static final String ISEND_SIGNATURE = "()Z";
    private static final String UNMARSHALELEMENT_METHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.unmarshalElement";
    private static final String UNMARSHALELEMENT_SIGNATURE =
        "()Ljava/lang/Object;";
    private static final String MARSHALLABLE_INTERFACE =
        "org.jibx.runtime.IMarshallable";
    private static final String MARSHALLABLE_METHOD =
        "org.jibx.runtime.IMarshallable.marshal";
    private static final String MARSHALLABLE_SIGNATURE =
        "(Lorg/jibx/runtime/IMarshallingContext;)V";

    //
    // Actual instance data.
    
    /** Type handled by this binding. */
    private final String m_type;

    /** Optional property definition. */
    private final PropertyDefinition m_property;

    /**
     * Constructor without implicit property.
     *
     * @param parent containing binding definition structure
     * @param type fully qualified class name of object type handled by this
     * binding (<code>null</code> if unspecified)
     */

    public DirectGeneric(IContainer parent, String type) {
        m_type = (type == null) ? "java.lang.Object" : type;
        m_property = null;
    }

    /**
     * Constructor with defined property.
     *
     * @param parent containing binding definition structure
     * @param type fully qualified class name of object type handled by this
     * binding (<code>null</code> if unspecified)
     * @param prop associated property information
     */

    public DirectGeneric(IContainer parent, String type,
        PropertyDefinition prop) {
        m_type = (type == null) ? "java.lang.Object" : type;
        m_property = prop;
    }

    /**
     * Generate presence test code for this mapping. The generated code just
     * checks that a start tag is next in the document, rather than an end tag.
     *
     * @param mb method builder
     */

    public void genTestPresent(ContextMethodBuilder mb) {
        
        // append code to call unmarshalling context method to check for end tag
        //  as next in document, then invert the result
        mb.loadContext();
        mb.appendCallVirtual(ISEND_METHOD, ISEND_SIGNATURE);
        mb.appendLoadConstant(1);
        mb.appendIXOR();
    }

    /**
     * Generate unmarshalling code for this mapping. The generated code just
     * calls the generic unmarshal element method, leaving the unmarshalled
     * object on the stack (after casting it, if necessary, to the appropriate
     * type).
     * TODO: Instead call unmarshalling method with class passed directly, for
     * better error reporting.
     *
     * @param mb method builder
     */

    public void genUnmarshal(ContextMethodBuilder mb) throws JiBXException {
        
        // check for optional property
        BranchWrapper tosave = null;
        if (m_property != null && m_property.isOptional()) {
            
            // generate code to check presence for the case of an optional
            //  item, with branch if so; if not present, set a null value
            //  with branch to be targeted at property store.
            genTestPresent(mb);
            BranchWrapper ifpres = mb.appendIFNE(this);
            mb.appendACONST_NULL();
            tosave = mb.appendUnconditionalBranch(this);
            mb.targetNext(ifpres);
        }
        
        // append code to call unmarshalling context method for generic element
        mb.loadContext();
        mb.appendCallVirtual(UNMARSHALELEMENT_METHOD,
            UNMARSHALELEMENT_SIGNATURE);
        mb.appendCreateCast(m_type);
        mb.targetNext(tosave);
        if (m_property != null && !m_property.isImplicit() &&
            !m_property.isThis()) {
            mb.loadObject();
            mb.appendSWAP();
            m_property.genStore(mb);
        }
    }

    /**
     * Generate marshalling code for this mapping. The generated code loads the
     * object reference and casts it to the generic marshal interface, then
     * calls the marshal method of that interface.
     *
     * @param mb method builder
     */

    public void genMarshal(ContextMethodBuilder mb) throws JiBXException {
        
        // append code to cast object and call generic marshal method
        BranchWrapper toend = null;
        if (m_property != null && !m_property.isImplicit()) {
            mb.loadObject();
            m_property.genLoad(mb);
            if (m_property.isOptional()) {
            
                // generate code to check nonnull for the case of an optional item,
                //  with branch if so; if not present, just pop the copy with branch
                //  to be targeted past end.
                mb.appendDUP();
                BranchWrapper ifpres = mb.appendIFNONNULL(this);
                mb.appendPOP();
                toend = mb.appendUnconditionalBranch(this);
                mb.targetNext(ifpres);
            }
        }
        mb.appendCreateCast(MARSHALLABLE_INTERFACE);
        mb.loadContext();
        mb.appendCallInterface(MARSHALLABLE_METHOD, MARSHALLABLE_SIGNATURE);
        mb.targetNext(toend);
    }
    
    //
    // IComponent interface method definitions

    public boolean isOptional() {
        return false;
    }

    public boolean hasAttribute() {
        return false;
    }

    public void genAttrPresentTest(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes defined");
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes defined");
    }

    public void genAttributeMarshal(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes defined");
    }

    public boolean hasContent() {
        return true;
    }

    public void genContentPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        genTestPresent(mb);
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        genUnmarshal(mb);
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        genMarshal(mb);
    }
    
    public void genNewInstance(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no instance creation");
    }

    public String getType() {
        return MARSHALLABLE_INTERFACE;
    }

    public boolean hasId() {
        return false;
    }

    public void genLoadId(ContextMethodBuilder mb) {
        throw new IllegalStateException("Internal error - no ID allowed");
    }
    
    public NameDefinition getWrapperName() {
        return null;
    }

    public void setLinkages() {}
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.println("direct generic reference" );
    }
}
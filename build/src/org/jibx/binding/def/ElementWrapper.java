/*
Copyright (c) 2003-2007, Dennis M. Sosnoski
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

import org.apache.bcel.generic.Type;
import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Component decorator for element definition. This associates an element name
 * with a component.
 *
 * @author Dennis M. Sosnoski
 */
public class ElementWrapper implements IComponent
{
    //
    // Constants and such related to code generation.
    
    private static final String UNMARSHAL_PARSESTARTATTRIBUTES =
        "org.jibx.runtime.impl.UnmarshallingContext.parseToStartTag";
    private static final String UNMARSHAL_PARSESTARTNOATTRIBUTES =
        "org.jibx.runtime.impl.UnmarshallingContext.parsePastStartTag";
    private static final String UNMARSHAL_PARSEPASTSTART =
        "org.jibx.runtime.impl.UnmarshallingContext.parsePastStartTag";
    private static final String UNMARSHAL_PARSESTARTSIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String UNMARSHAL_PARSEENDMETHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.parsePastCurrentEndTag";
    private static final String UNMARSHAL_PARSEENDSIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String UNMARSHAL_ISATMETHOD =
        "org.jibx.runtime.IUnmarshallingContext.isAt";
    private static final String UNMARSHAL_ISATSIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)Z";
    private static final String UNMARSHAL_SKIPELEMENTMETHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.parsePastElement";
    private static final String UNMARSHAL_SKIPELEMENTSIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String MARSHAL_WRITESTARTNAMESPACES =
        "org.jibx.runtime.impl.MarshallingContext.startTagNamespaces";
    private static final String MARSHAL_STARTNAMESPACESSIGNATURE =
        "(ILjava/lang/String;[I[Ljava/lang/String;)" +
        "Lorg/jibx/runtime/impl/MarshallingContext;";
    private static final String MARSHAL_WRITESTARTATTRIBUTES =
        "org.jibx.runtime.impl.MarshallingContext.startTagAttributes";
    private static final String MARSHAL_WRITESTARTNOATTRIBUTES =
        "org.jibx.runtime.impl.MarshallingContext.startTag";
    private static final String MARSHAL_WRITESTARTSIGNATURE =
        "(ILjava/lang/String;)Lorg/jibx/runtime/impl/MarshallingContext;";
    private static final String MARSHAL_CLOSESTARTCONTENT =
        "org.jibx.runtime.impl.MarshallingContext.closeStartContent";
    private static final String MARSHAL_CLOSESTARTEMPTY =
        "org.jibx.runtime.impl.MarshallingContext.closeStartEmpty";
    private static final String MARSHAL_CLOSESTARTSIGNATURE =
        "()Lorg/jibx/runtime/impl/MarshallingContext;";
    private static final String MARSHAL_WRITEENDMETHOD =
        "org.jibx.runtime.impl.MarshallingContext.endTag";
    private static final String MARSHAL_WRITEENDSIGNATURE =
        "(ILjava/lang/String;)Lorg/jibx/runtime/impl/MarshallingContext;";
    private static final String UNMARSHAL_PARSE_TO_START_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.parseToStartTag";
    private static final String UNMARSHAL_PARSE_TO_START_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    protected static final String UNMARSHAL_ATTRIBUTE_BOOLEAN_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.attributeBoolean";
    protected static final String UNMARSHAL_ATTRIBUTE_BOOLEAN_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;Z)Z";
    private static final String UNMARSHAL_SKIP_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.skipElement";
    private static final String UNMARSHAL_SKIP_SIGNATURE = "()V";
    protected static final String MARSHAL_STARTTAG_ATTRIBUTES =
        "org.jibx.runtime.impl.MarshallingContext.startTagAttributes";
    protected static final String MARSHAL_STARTTAG_SIGNATURE =
        "(ILjava/lang/String;)Lorg/jibx/runtime/impl/MarshallingContext;";
    protected static final String MARSHAL_CLOSESTART_EMPTY =
        "org.jibx.runtime.impl.MarshallingContext.closeStartEmpty";
    protected static final String MARSHAL_CLOSESTART_EMPTY_SIGNATURE =
        "()Lorg/jibx/runtime/impl/MarshallingContext;";
    protected static final String MARSHAL_ATTRIBUTE =
        "org.jibx.runtime.impl.MarshallingContext.attribute";
    protected static final String MARSHAL_SIGNATURE =
        "(ILjava/lang/String;Ljava/lang/String;)" +
        "Lorg/jibx/runtime/impl/MarshallingContext;";
    private static final String MARSHALLING_CONTEXT =
        "org.jibx.runtime.impl.MarshallingContext";
    private static final String UNMARSHALLING_CONTEXT =
        "org.jibx.runtime.impl.UnmarshallingContext";

    //
    // Actual instance data.
    
    /** Property value binding component. */
    private final IComponent m_component;

    /** Binding definition context. */
    private final DefinitionContext m_defContext;

    /** Element name information. */
    private final NameDefinition m_name;
    
    /** Flag for nillable element. */
    private final boolean m_isNillable;
    
    /** Flag for value from collection (TODO: fix this in update). */
    private boolean m_directAccess;
    
    /** Flag for optional ignored element (TODO: fix this in update). */
    private boolean m_optionalIgnored;
    
    /** Flag for optional normal element (TODO: fix this in update). */
    private boolean m_optionalNormal;
    
    /** Flag for optional structure object (TODO: fix this in update). */
    private boolean m_structureObject;

    /**
     * Constructor.
     *
     * @param defc definition context for this component
     * @param name element name definition
     * @param wrap wrapped binding component (may be <code>null</code>, in the
     * case of a throwaway component)
     * @param nillable flag for nillable element
     */
    public ElementWrapper(DefinitionContext defc, NameDefinition name,
        IComponent wrap, boolean nillable) {
        m_defContext = defc;
        m_name = name;
        m_component = wrap;
        m_isNillable = nillable;
    }

    /**
     * Set the direct access flag. This controls a variation in the code
     * generation to handle values loaded from a collection.
     *
     * @param direct <code>true</code> if direct access from collection,
     * <code>false</code> if not
     */
    public void setDirect(boolean direct) {
        m_directAccess = direct;
    }

    /**
     * Set flag for an optional ignored element.
     *
     * @param opt <code>true</code> if optional ignored element,
     * <code>false</code> if not
     */
    public void setOptionalIgnored(boolean opt) {
        m_optionalIgnored = opt;
    }

    /**
     * Set flag for an optional structure object.
     *
     * @param opt <code>true</code> if optional structure object,
     * <code>false</code> if not
     */
    public void setStructureObject(boolean opt) {
        m_structureObject = opt;
    }

    /**
     * Set flag for an optional normal element.
     *
     * @param opt <code>true</code> if optional normal element,
     * <code>false</code> if not
     */
    public void setOptionalNormal(boolean opt) {
        m_optionalNormal = opt;
    }

    //
    // IComponent interface method definitions

    public boolean isOptional() {
        return m_optionalNormal || m_optionalIgnored;
    }

    public boolean hasAttribute() {
        return false;
    }

    public void genAttrPresentTest(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes from child element");
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes from child element");
    }

    public void genAttributeMarshal(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no attributes from child element");
    }

    public boolean hasContent() {
        return true;
    }

    public void genContentPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        
        // create call to unmarshalling context method with namespace and
        //  name, then return result directly
        mb.loadContext();
        m_name.genPushUriPair(mb);
        mb.appendCallInterface(UNMARSHAL_ISATMETHOD, UNMARSHAL_ISATSIGNATURE);
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        
        // generate code to store null to property
        PropertyDefinition prop = null;
        if (m_component instanceof ComponentProperty) {
            prop = ((ComponentProperty)m_component).getProperty();
        }
        
        // check for optional empty wrapper present
        BranchWrapper ifmiss = null;
        BranchTarget loop = null;
        if (isOptional()) {
            
            // set first pass indicator for setting presence flag, along with
            //  loop target for repeated checks (when skipping repeated element)
            int first = -1;
            if (prop != null && prop.hasFlag()) {
                mb.appendICONST_1();
                first = mb.addLocal("first", Type.BOOLEAN);
                BranchWrapper tocheck = mb.appendUnconditionalBranch(this);
                mb.initStackState(tocheck);
                loop = mb.appendTargetLoadConstant(0);
                mb.appendStoreLocal(first);
                mb.targetNext(tocheck);
            } else {
                loop = mb.appendTargetNOP();
            }
            
            // check for element present, setting presence flag on first pass only
            genContentPresentTest(mb);
            if (prop != null && prop.hasFlag()) {
                mb.appendLoadLocal(first);
                BranchWrapper notfirst = mb.appendIFEQ(this);
                mb.appendDUP();
                mb.loadObject();
                mb.appendSWAP();
                prop.genFlag(mb);
                mb.targetNext(notfirst);
            }
            ifmiss = mb.appendIFEQ(this);
            
        } else if (prop != null && prop.hasFlag()) {
            
            // required element, make sure it's present and set flag
            mb.loadContext(UNMARSHALLING_CONTEXT);
            m_name.genPushUriPair(mb);
            mb.appendCallVirtual(UNMARSHAL_PARSE_TO_START_NAME,
                UNMARSHAL_PARSE_TO_START_SIGNATURE);
            mb.appendICONST_1();
            prop.genFlag(mb);
            
        }
        BranchWrapper ifnil = null;
        if (m_isNillable) {
            
            // make sure we're at the element
            mb.loadContext(UNMARSHALLING_CONTEXT);
            m_name.genPushUriPair(mb);
            mb.appendCallVirtual(UNMARSHAL_PARSE_TO_START_NAME,
                UNMARSHAL_PARSE_TO_START_SIGNATURE);
            
            // check for xsi:nil="true"
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendLoadConstant("http://www.w3.org/2001/XMLSchema-instance");
            mb.appendLoadConstant("nil");
            mb.appendICONST_0();
            mb.appendCallVirtual(UNMARSHAL_ATTRIBUTE_BOOLEAN_NAME,
                UNMARSHAL_ATTRIBUTE_BOOLEAN_SIGNATURE);
            BranchWrapper notnil = mb.appendIFEQ(this);
            
            // skip past the element before jumping to end
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCallVirtual(UNMARSHAL_SKIP_NAME,
                UNMARSHAL_SKIP_SIGNATURE);
            ifnil = mb.appendUnconditionalBranch(this);
            mb.targetNext(notnil);
        }
    
        // set up flags for controlling code generation paths
        boolean attr = m_component != null && m_component.hasAttribute();
        boolean cont = m_component != null && m_component.hasContent();
        
        // load the unmarshalling context followed by the namespace URI and
        //  element name.
        mb.loadContext(UNMARSHALLING_CONTEXT);
        m_name.genPushUriPair(mb);
        
        // check type of unmarshalling behavior required
        if (attr) {

            // unmarshal start tag with attribute(s)
            mb.appendCallVirtual(UNMARSHAL_PARSESTARTATTRIBUTES,
                UNMARSHAL_PARSESTARTSIGNATURE);
            m_component.genAttributeUnmarshal(mb);

            // generate code to parse past the start tag with another call
            //  to unmarshalling context
            mb.loadContext(UNMARSHALLING_CONTEXT);
            m_name.genPushUriPair(mb);
            mb.appendCallVirtual(UNMARSHAL_PARSEPASTSTART,
                UNMARSHAL_PARSESTARTSIGNATURE);
                
        } else if (cont) {
            
            // unmarshal start tag without attributes
            mb.appendCallVirtual(UNMARSHAL_PARSESTARTNOATTRIBUTES,
                UNMARSHAL_PARSESTARTSIGNATURE);
            
        } else {
            
            // unmarshal element discarding all content, then loop to check for
            //  repeat of element
            mb.appendCallVirtual(UNMARSHAL_SKIPELEMENTMETHOD,
                UNMARSHAL_SKIPELEMENTSIGNATURE);
            if (loop != null && ifmiss != null) {
                mb.appendUnconditionalBranch(this).setTarget(loop, mb);
                mb.targetNext(ifmiss);
                ifmiss = null;
                mb.appendPOP();
                mb.appendACONST_NULL();
            }
        }

        // unmarshal child content
        if (cont) {
            m_component.genContentUnmarshal(mb);
        }

        // next add code to push context, namespace and name, and call
        //  method to parse past end tag
        if (attr || cont) {
            mb.loadContext(UNMARSHALLING_CONTEXT);
            m_name.genPushUriPair(mb);
            mb.appendCallVirtual(UNMARSHAL_PARSEENDMETHOD,
                UNMARSHAL_PARSEENDSIGNATURE);
        }
        
        // check if need code to handle missing or nil element
        if (ifmiss != null || ifnil != null) {
            
            // handle branch conditions, jumping around code to set null
            BranchWrapper toend = mb.appendUnconditionalBranch(this);
            mb.targetNext(ifmiss);
            mb.targetNext(ifnil);
            
            // generate code to store null to property
            if (m_component instanceof ComponentProperty) {
                if (!prop.isImplicit()) {
                    mb.loadObject();
                    mb.appendACONST_NULL();
                    prop.genStore(mb);
                } else {
                    mb.appendACONST_NULL();
                }
            } else {
                mb.appendPOP();
                mb.appendACONST_NULL();
            }
            mb.targetNext(toend);
            
        }
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        
        // nothing to be done if optional ignored element
        if (!m_optionalIgnored) {
            boolean noprop = m_directAccess ||
                (!(m_component instanceof ComponentProperty) &&
                !(m_component instanceof NestedStructure));
            BranchWrapper ifmiss = null;
            if (m_isNillable) {
                
                // check for null object
                BranchWrapper ifhit;
                if (noprop) {
                    mb.appendDUP();
                    ifhit = mb.appendIFNONNULL(this);
                    mb.appendPOP();
                } else {
                    PropertyDefinition prop =
                        ((ComponentProperty)m_component).getProperty();
                    mb.loadObject();
                    prop.genLoad(mb);
                    ifhit = mb.appendIFNONNULL(this);
                }
                
                // generate empty element with xsi:nil="true"
                mb.loadContext(MARSHALLING_CONTEXT);
                m_name.genPushIndexPair(mb);
                mb.appendCallVirtual(MARSHAL_STARTTAG_ATTRIBUTES,
                    MARSHAL_STARTTAG_SIGNATURE);
                mb.appendLoadConstant(2);
                mb.appendLoadConstant("nil");
                mb.appendLoadConstant("true");
                mb.appendCallVirtual(MARSHAL_ATTRIBUTE, MARSHAL_SIGNATURE);
                mb.appendCallVirtual(MARSHAL_CLOSESTART_EMPTY,
                    MARSHAL_CLOSESTART_EMPTY_SIGNATURE);
                mb.appendPOP();
                ifmiss = mb.appendUnconditionalBranch(this);
                mb.targetNext(ifhit);
            }
        
            // set up flags for controlling code generation paths
            boolean attr = m_component != null && m_component.hasAttribute();
            boolean cont = m_component != null && m_component.hasContent();
            boolean needns = !m_structureObject  && m_defContext.hasNamespace();
        
            // duplicate object reference on stack if both attribute(s) and
            //  content
            if (attr && cont && noprop) {
                mb.appendDUP();
            }

            // load the context followed by namespace index and element name
            mb.loadContext(MARSHALLING_CONTEXT);
            m_name.genPushIndexPair(mb);
        
            // check type of marshalling behavior required
            if (attr || needns) {
            
                // check for namespace definition required
                if (needns) {
                
                    // marshal start tag with namespace(s)
                    m_defContext.genLoadNamespaces(mb);
                    mb.appendCallVirtual(MARSHAL_WRITESTARTNAMESPACES,
                        MARSHAL_STARTNAMESPACESSIGNATURE);
                    
                } else {

                    // marshal start tag with attribute(s)
                    mb.appendCallVirtual(MARSHAL_WRITESTARTATTRIBUTES,
                        MARSHAL_WRITESTARTSIGNATURE);
                
                }
            
                // handle attributes other than namespace declarations
                if (attr) {
                    if (noprop) {
                        
                        // discard marshalling context from stack before call
                        mb.appendPOP();
                        m_component.genAttributeMarshal(mb);
                        mb.loadContext(MARSHALLING_CONTEXT);
                        
                    } else {
                        m_component.genAttributeMarshal(mb);
                    }
                }
    
                // generate code to close the start tag with another call
                //  to marshalling context
                if (cont) {
                    mb.appendCallVirtual(MARSHAL_CLOSESTARTCONTENT,
                        MARSHAL_CLOSESTARTSIGNATURE);
                } else {
                    mb.appendCallVirtual(MARSHAL_CLOSESTARTEMPTY,
                        MARSHAL_CLOSESTARTSIGNATURE);
                }
                
            } else if (cont) {
            
                // marshal start tag without attributes
                mb.appendCallVirtual(MARSHAL_WRITESTARTNOATTRIBUTES,
                    MARSHAL_WRITESTARTSIGNATURE);
            
            } else {
            
                // marshal empty tag
                mb.appendCallVirtual(MARSHAL_WRITESTARTATTRIBUTES,
                    MARSHAL_WRITESTARTSIGNATURE);
                mb.appendCallVirtual(MARSHAL_CLOSESTARTEMPTY,
                    MARSHAL_CLOSESTARTSIGNATURE);
            
            }

            // handle child content if present
            if (cont) {
                if (noprop) {
                    
                    // discard marshalling context from stack before call
                    mb.appendPOP();
                    m_component.genContentMarshal(mb);
                    mb.loadContext(MARSHALLING_CONTEXT);
                    
                } else {
                    m_component.genContentMarshal(mb);
                }
                
                // write the element close tag
                m_name.genPushIndexPair(mb);
                mb.appendCallVirtual(MARSHAL_WRITEENDMETHOD,
                    MARSHAL_WRITEENDSIGNATURE);
            }
            mb.appendPOP();
            mb.targetNext(ifmiss);
        }
    }
    
    public void genNewInstance(ContextMethodBuilder mb) throws JiBXException {
        if (m_component == null) {
            throw new IllegalStateException
                ("Internal error - no wrapped component");
        } else {
            m_component.genNewInstance(mb);
        }
    }

    public String getType() {
        if (m_component == null) {
            throw new IllegalStateException
                ("Internal error - no wrapped component");
        } else {
            return m_component.getType();
        }
    }

    public boolean hasId() {
        if (m_component == null) {
            return false;
        } else {
            return m_component.hasId();
        }
    }

    public void genLoadId(ContextMethodBuilder mb) throws JiBXException {
        if (m_component == null) {
            throw new IllegalStateException
                ("Internal error - no wrapped component");
        } else {
            m_component.genLoadId(mb);
        }
    }
    
    public NameDefinition getWrapperName() {
        return m_name;
    }

    public void setLinkages() throws JiBXException {
        m_name.fixNamespace(m_defContext);
        if (m_component != null) {
            m_component.setLinkages();
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.println(toString());
        if (m_component != null) {
            m_component.print(depth+1);
        }
    }
    
    public String toString() {
        StringBuffer buff = new StringBuffer("element wrapper");
        if (m_name != null) {
            buff.append(' ');
            buff.append(m_name.toString());
        }
        if (m_directAccess) {
            buff.append(" direct");
        }
        if (m_optionalIgnored) {
            buff.append(" optional ignored");
        }
        if (m_optionalNormal) {
            buff.append(" optional");
        }
        if (m_structureObject) {
            buff.append(" structure object");
        }
        return buff.toString();
    }
}
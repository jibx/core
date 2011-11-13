/*
Copyright (c) 2003-2009, Dennis M. Sosnoski.
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

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Attribute or simple content value definition from binding. This organizes
 * information for anything that can be converted to and from a simple
 * <code>String</code>. Content values include both elements with only character
 * data content and text, as character data content or CDATA sections.
 *
 * @author Dennis M. Sosnoski
 */
public class ValueChild implements IComponent
{
    //
    // Ident type enumeration.

    /*package*/ static final int DIRECT_IDENT = 0;
    /*package*/ static final int AUTO_IDENT = 1;
    /*package*/ static final int DEF_IDENT = 2;
    /*package*/ static final int REF_IDENT = 3;
    
    //
    // Value style enumeration.
    
    /*package*/ static final int ATTRIBUTE_STYLE = 0;
    /*package*/ static final int ELEMENT_STYLE = 1;
    /*package*/ static final int TEXT_STYLE = 2;
    /*package*/ static final int CDATA_STYLE = 3;
    
    //
    // Constants for unmarshalling.

    /** Prefix used for backfill classes. */
    private static final String BACKFILL_SUFFIX = "_backfill_";
    
    private static final String[] BACKFILL_INTERFACES =
    {
        "org.jibx.runtime.impl.BackFillReference"
    };
    private static final String BACKFILL_METHODNAME = "backfill";
    private static final Type[] BACKFILL_METHODARGS =
    {
        Type.OBJECT
    };
    private static final String BOUNDREF_NAME = "m_obj";
    private static final String CHECK_ELEMENT_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.isAt";
    private static final String CHECK_ATTRIBUTE_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.hasAttribute";
    private static final String CHECK_SIGNATURE = 
        "(Ljava/lang/String;Ljava/lang/String;)Z";
    private static final String UNMARSHAL_DEFREF_ATTR_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.attributeExistingIDREF";
    private static final String UNMARSHAL_DEFREF_ELEM_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.parseElementExistingIDREF";
    private static final String UNMARSHAL_FWDREF_ATTR_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.attributeForwardIDREF";
    private static final String UNMARSHAL_FWDREF_ELEM_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.parseElementForwardIDREF";
    private static final String UNMARSHAL_DEFREF_SIGNATURE = 
        "(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/Object;";
    private static final String REGISTER_BACKFILL_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.registerBackFill";
    private static final String REGISTER_BACKFILL_SIGNATURE =
        "(ILorg/jibx/runtime/impl/BackFillReference;)V";
    private static final String DEFINE_ID_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.defineID";
    private static final String DEFINE_ID_SIGNATURE = 
        "(Ljava/lang/String;ILjava/lang/Object;)V";
    protected static final String UNMARSHAL_REQ_ATTRIBUTE =
        "org.jibx.runtime.impl.UnmarshallingContext.attributeText";
    protected static final String UNMARSHAL_REQ_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
    private static final String UNMARSHAL_TEXT_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.parseContentText";
    private static final String UNMARSHAL_TEXT_SIGNATURE =
        "()Ljava/lang/String;";
    private static final String UNMARSHAL_ELEMENT_TEXT_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.parseElementText";
    private static final String UNMARSHAL_ELEMENT_TEXT_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;";
    private static final String UNMARSHAL_PARSE_IF_START_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.parseIfStartTag";
    private static final String UNMARSHAL_PARSE_IF_START_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)Z";
    private static final String UNMARSHAL_PARSE_TO_START_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.parseToStartTag";
    private static final String UNMARSHAL_PARSE_TO_START_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String UNMARSHAL_PARSE_PAST_END_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.parsePastEndTag";
    private static final String UNMARSHAL_PARSE_PAST_END_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String UNMARSHAL_SKIPELEMENTMETHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.parsePastElement";
    private static final String UNMARSHAL_SKIPELEMENTSIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String MARSHAL_TEXT_NAME =
        "org.jibx.runtime.impl.MarshallingContext.writeContent";
    private static final String MARSHAL_CDATA_NAME =
        "org.jibx.runtime.impl.MarshallingContext.writeCData";
    private static final String MARSHAL_TEXT_SIGNATURE =
        "(Ljava/lang/String;)Lorg/jibx/runtime/impl/MarshallingContext;";
    private static final String UNMARSHALLING_THROWEXCEPTION_METHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.throwException";
    private static final String UNMARSHALLING_THROWEXCEPTION_SIGNATURE =
        "(Ljava/lang/String;)V";
    protected static final String MARSHAL_ATTRIBUTE =
        "org.jibx.runtime.impl.MarshallingContext.attribute";
    protected static final String MARSHAL_ELEMENT =
        "org.jibx.runtime.impl.MarshallingContext.element";
    protected static final String MARSHAL_SIGNATURE =
        "(ILjava/lang/String;Ljava/lang/String;)" +
        "Lorg/jibx/runtime/impl/MarshallingContext;";
    protected static final String MARSHAL_STARTTAG_ATTRIBUTES =
        "org.jibx.runtime.impl.MarshallingContext.startTagAttributes";
    protected static final String MARSHAL_STARTTAG_SIGNATURE =
        "(ILjava/lang/String;)Lorg/jibx/runtime/impl/MarshallingContext;";
    protected static final String MARSHAL_CLOSESTART_EMPTY =
        "org.jibx.runtime.impl.MarshallingContext.closeStartEmpty";
    protected static final String MARSHAL_CLOSESTART_EMPTY_SIGNATURE =
        "()Lorg/jibx/runtime/impl/MarshallingContext;";
    protected static final String UNMARSHAL_ATTRIBUTE_BOOLEAN_NAME =
        "org.jibx.runtime.impl.UnmarshallingContext.attributeBoolean";
    protected static final String UNMARSHAL_ATTRIBUTE_BOOLEAN_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;Z)Z";

    //
    // Actual instance data

    /** Containing binding definition structure. */
    private final IContainer m_container;

    /** Containing object context. */
    private final IContextObj m_objContext;

    /** Value style code. */
    private final int m_valueStyle;
    
    /** Constant value. */
    private final String m_constantValue;

    /** Ident type code. */
    private final int m_identType;

    /** Attribute or element name information. */
    private final NameDefinition m_name;
    
    /** Fully qualified name of type. */
    private final String m_type;
    
    /** Nillable element flag. */
    private final boolean m_isNillable;

    /** Linked property information. */
    private final PropertyDefinition m_property;
    
    /** Conversion handling for value. */
    private final StringConversion m_conversion;
    
    /** Mapping definition for object class supplying identifier. */
    private IMapping m_idRefMap;

    /**
     * Constructor. Saves the context information for later use.
     *
     * @param contain containing binding definition structure
     * @param objc containing object context
     * @param name element or attribute name information (may be
     * <code>null</code>)
     * @param prop property reference information
     * @param conv string conversion handler
     * @param style value style code
     * @param ident identifier type code
     * @param constant value for constant
     * @param nillable nillable element flag
     */
    public ValueChild(IContainer contain, IContextObj objc, NameDefinition name,
        PropertyDefinition prop, StringConversion conv, int style, int ident,
        String constant, boolean nillable) {
        m_container = contain;
        m_objContext = objc;
        m_name = name;
        m_property = prop;
        m_type = prop.getTypeName();
        m_conversion = conv;
        m_valueStyle = style;
        m_identType = ident;
        m_constantValue = constant;
        m_isNillable = nillable;
    }

    /**
     * Create backfill handler class if it does not already exist. This either
     * looks up the existing backfill handler class or creates a new one
     * specifically for this value.
     *
     * @return backfill handler class for value
     * @throws JiBXException if error in configuration
     */
    private ClassFile createBackfillClass() throws JiBXException {
        
        // create the new class
        BoundClass bc = m_objContext.getBoundClass();
        BindingDefinition def = m_container.getBindingRoot();
        String name = bc.getClassFile().deriveClassName(def.getPrefix(),
            BACKFILL_SUFFIX + m_property.getName());
        ClassFile base = ClassCache.requireClassFile("java.lang.Object");
        ClassFile cf = new ClassFile(name, bc.getClassFile().getRoot(),
            base, Constants.ACC_PUBLIC, BACKFILL_INTERFACES);
        
        // add member variable for bound class reference
        String type = bc.getClassFile().getName();
        ClassItem ref = cf.addPrivateField(type, BOUNDREF_NAME);
        
        // add the constructor taking bound class reference
        Type[] args = new Type[] { ClassItem.typeFromName(type) };
        MethodBuilder mb = new ExceptionMethodBuilder("<init>",
            Type.VOID, args, cf, (short)0);
        
        // call the superclass constructor
        mb.appendLoadLocal(0);
        mb.appendCallInit("java.lang.Object", "()V");
        
        // store bound class reference to member variable
        mb.appendLoadLocal(0);
        mb.appendLoadLocal(1);
        mb.appendPutField(ref);
        mb.appendReturn();
        mb.codeComplete(false);
        mb.addMethod();
        
        // add actual backfill interface implementation method
        mb = new ExceptionMethodBuilder(BACKFILL_METHODNAME, Type.VOID,
            BACKFILL_METHODARGS, cf, Constants.ACC_PUBLIC);
        mb.appendLoadLocal(0);
        mb.appendGetField(ref);
        mb.appendLoadLocal(1);
        mb.appendCreateCast(m_property.getSetValueType());
        m_property.genStore(mb);
        mb.appendReturn();
        mb.codeComplete(false);
        mb.addMethod();
        
        // return unique instance of class
        return MungedClass.getUniqueSupportClass(cf);
    }

    /**
     * Generate unmarshalling code for object identifier reference. The code
     * generated by this method assumes the unmarshalling context and name have
     * already been loaded to the stack, and these are consumed by the code.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    private void genParseIdRef(ContextMethodBuilder mb) throws JiBXException {
        
        // first part of generated instruction sequence is to check if optional
        //  value is present
        BranchWrapper ifmiss = null;
        if (m_property.isOptional()) {
            
            // use existing context reference and name information to check for
            //  attribute or element present
            String name = m_valueStyle == ValueChild.ATTRIBUTE_STYLE ?
                CHECK_ATTRIBUTE_NAME : CHECK_ELEMENT_NAME;
            mb.appendCallVirtual(name, CHECK_SIGNATURE);
            BranchWrapper ifpres = mb.appendIFNE(this);
            
            // push a null value to be stored as result for missing case
            mb.appendACONST_NULL();
            ifmiss = mb.appendUnconditionalBranch(this);
            
            // reload context reference and name information for use by actual
            //  unmarshalling call
            mb.targetNext(ifpres);
            mb.loadContext();
            m_name.genPushUriPair(mb);
            
        }
        
        // find index of target class ID map
        int index = m_container.getBindingRoot().
            getIdClassIndex(m_property.getTypeName());
        
        // check if forward references allowed
        if (m_container.getBindingRoot().isForwards()) {
            
            // generate call to unmarshal with forward allowed
            mb.appendLoadConstant(index);
            String name = m_valueStyle == ValueChild.ATTRIBUTE_STYLE ?
                UNMARSHAL_FWDREF_ATTR_NAME : UNMARSHAL_FWDREF_ELEM_NAME;
            mb.appendCallVirtual(name, UNMARSHAL_DEFREF_SIGNATURE);
            
            // check for null result returned
            mb.appendDUP();
            BranchWrapper ifdef = mb.appendIFNONNULL(this);
            
            // build and register backfill handler; start by loading the
            //  unmarshalling context, then load the index number of the target
            //  class and create an instance of the backfill handler
            ClassFile backclas = createBackfillClass();
            mb.loadContext();
            mb.appendLoadConstant(index);
            mb.appendCreateNew(backclas.getName());
            
            // duplicate the backfill handler reference, then load a reference
            //  to the owning object and call the initializer before calling
            //  the unmarshalling context to register the handler
            mb.appendDUP();
            mb.loadObject();        
            mb.appendCallInit(backclas.getName(), "(" + 
                m_objContext.getBoundClass().getClassFile().getSignature() +
                ")V");
            mb.appendCallVirtual(REGISTER_BACKFILL_NAME,
                REGISTER_BACKFILL_SIGNATURE);
            
            // set branch target for case where already defined
            mb.targetNext(ifdef);
            
        } else {
            
            // generate call to unmarshal with predefined ID required
            mb.appendLoadConstant(index);
            String name = m_valueStyle == ValueChild.ATTRIBUTE_STYLE ?
                UNMARSHAL_DEFREF_ATTR_NAME : UNMARSHAL_DEFREF_ELEM_NAME;
            mb.appendCallVirtual(name, UNMARSHAL_DEFREF_SIGNATURE);
        }
            
        // handle object type conversion if needed
        mb.appendCreateCast(m_property.getSetValueType());
        
        // store returned reference to property
        if (ifmiss != null) {
            mb.targetNext(ifmiss);
        }
        m_property.genStore(mb);
    }
    
    /**
     * Generate test if present code. This generates code that tests if the
     * child is present, leaving the result of the test (zero if missing,
     * nonzero if present) on the stack.
     *
     * @param mb unmarshal method builder
     * @throws JiBXException if configuration error
     */
    public void genIfPresentTest(UnmarshalBuilder mb) throws JiBXException {
        
        // make sure this is an appropriate call
        if (m_name == null) {
            throw new JiBXException("Method call on invalid value");
        }
        
        // load the unmarshalling context and name information, then call the
        //  appropriate method to test for item present
        mb.loadContext();
        m_name.genPushUriPair(mb);
        String name = (m_valueStyle == ValueChild.ATTRIBUTE_STYLE) ?
            CHECK_ATTRIBUTE_NAME : CHECK_ELEMENT_NAME;
        mb.appendCallVirtual(name, CHECK_SIGNATURE);
    }

    /**
     * Generate unmarshalling code. This internal method generates the
     * necessary code for handling the unmarshalling operation. The code
     * generated by this method restores the stack to the original state
     * when done.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    private void genUnmarshal(ContextMethodBuilder mb) throws JiBXException {
        
        // first part of generated instruction sequence is to preload object
        //  reference for later use, then load the unmarshalling context and
        //  the name information
        if (m_constantValue == null && !m_property.isImplicit()) {
            mb.loadObject();
        }
        
        // prepare for parsing the element or attribute name
        mb.loadContext();
        if (m_name != null) {
            m_name.genPushUriPair(mb);
        }
        
        // handle flag property
        boolean isatt = (m_valueStyle == ValueChild.ATTRIBUTE_STYLE);
        if (m_property.hasFlag()) {
            if (m_property.isOptional()) {
                
                // generate check for value present in document
                if (isatt) {
                    mb.appendCallVirtual(CHECK_ATTRIBUTE_NAME, CHECK_SIGNATURE);
                } else {
                    
                    // save copy of test result in case of element, to check
                    //  for skip needed
                    mb.appendCallVirtual(CHECK_ELEMENT_NAME, CHECK_SIGNATURE);
                    if (m_property.isFlagOnly()) {
                        mb.appendDUP_X1();
                    }
                    
                }
            } else {
                
                // required value, just make sure it's present and set 'true'
                if (isatt) {
                    mb.appendCallVirtual(UNMARSHAL_REQ_ATTRIBUTE,
                        UNMARSHAL_REQ_SIGNATURE);
                    mb.appendPOP();
                } else {
                    mb.appendCallVirtual(UNMARSHAL_PARSE_TO_START_NAME,
                        UNMARSHAL_PARSE_TO_START_SIGNATURE);
                }
                mb.appendICONST_1();
                
            }
            
            // store the presence flag value
            m_property.genFlag(mb);
            if (m_property.isFlagOnly()) {
                
                // content ignored, so just skip past element if present
                if (!isatt) {
                    BranchWrapper miss = mb.appendIFEQ(this);
                    mb.loadContext();
                    m_name.genPushUriPair(mb);
                    mb.appendCallVirtual(UNMARSHAL_SKIPELEMENTMETHOD,
                        UNMARSHAL_SKIPELEMENTSIGNATURE);
                    mb.appendPOP();
                    mb.targetNext(miss);
                }
                return;
                
            } else {
                
                // again prepare for parsing the element or attribute name and
                //  then storing the value
                if (m_constantValue == null && !m_property.isImplicit()) {
                    mb.loadObject();
                }
                mb.loadContext();
                if (m_name != null) {
                    m_name.genPushUriPair(mb);
                }
                
            }
        }
        
        // check if this is an identifier for object
        if (m_identType == DEF_IDENT || m_identType == AUTO_IDENT) {
            
            // always unmarshal identifier value as text, then duplicate for use
            //  if storing
            BindingDefinition.s_stringConversion.genParseRequired(isatt, mb);
            if (m_identType != AUTO_IDENT) {
                mb.appendDUP();
            }
            
            // load the context and swap to reorder, load the index for the
            //  class, and finally the ID'ed object, then call ID definition
            //  method
            mb.loadContext();
            mb.appendSWAP();
            int index = m_container.getBindingRoot().
                getIdClassIndex(m_property.getTypeName());
            mb.appendLoadConstant(index);
            mb.loadObject();
            mb.appendCallVirtual(DEFINE_ID_NAME, DEFINE_ID_SIGNATURE);
            
            // convert from text and store result using object reference loaded
            //  earlier
            if (m_identType != AUTO_IDENT) {
                m_conversion.genFromText(mb);
                m_property.genStore(mb);
            }

        } else if (m_identType == REF_IDENT) {
            
            // generate code for unmarshalling object ID
            genParseIdRef(mb);

        } else if (m_constantValue == null) {
            
            // unmarshal and convert value
            if (m_isNillable) {
                
                // first check for element present at all
                BranchWrapper ifmiss = null;
                if (m_property.isOptional()) {
                    mb.appendCallVirtual(CHECK_ELEMENT_NAME, CHECK_SIGNATURE);
                    ifmiss = mb.appendIFEQ(this);
                } else {
                    mb.appendCallVirtual(UNMARSHAL_PARSE_TO_START_NAME,
                        UNMARSHAL_PARSE_TO_START_SIGNATURE);
                }
                
                // check for xsi:nil="true"
                mb.loadContext();
                mb.appendLoadConstant("http://www.w3.org/2001/XMLSchema-instance");
                mb.appendLoadConstant("nil");
                mb.appendICONST_0();
                mb.appendCallVirtual(UNMARSHAL_ATTRIBUTE_BOOLEAN_NAME,
                    UNMARSHAL_ATTRIBUTE_BOOLEAN_SIGNATURE);
                BranchWrapper notnil = mb.appendIFEQ(this);
                
                // code to handle nil case just parses past end
                mb.loadContext();
                m_name.genPushUriPair(mb);
                mb.appendCallVirtual(UNMARSHAL_PARSE_PAST_END_NAME,
                    UNMARSHAL_PARSE_PAST_END_SIGNATURE);
                
                // merge path with element not present, which just loads null
                mb.targetNext(ifmiss);
                mb.appendACONST_NULL();
                m_conversion.genFromText(mb);
                BranchWrapper ifnil = mb.appendUnconditionalBranch(this);
                
                // read element text and process for not-nil case
                mb.targetNext(notnil);
                mb.loadContext();
                m_name.genPushUriPair(mb);
                mb.appendCallVirtual(UNMARSHAL_ELEMENT_TEXT_NAME,
                    UNMARSHAL_ELEMENT_TEXT_SIGNATURE);
                m_conversion.genFromText(mb);
                mb.targetNext(ifnil);
                
            } else if (m_valueStyle == ValueChild.TEXT_STYLE ||
                m_valueStyle == ValueChild.CDATA_STYLE) {
                
                // unmarshal text value directly and let handler convert
                mb.appendCallVirtual(UNMARSHAL_TEXT_NAME,
                    UNMARSHAL_TEXT_SIGNATURE);
                m_conversion.genFromText(mb);
                
            } else if (m_property.isOptional() &&
                (isatt || m_container.isContentOrdered())) {
                
                // parse value with possible default in ordered container
                m_conversion.genParseOptional(isatt, mb);
                
            } else {
                
                // parse required value, or value in unordered container
                m_conversion.genParseRequired(isatt, mb);
                
            }
            
            // handle object type conversion if needed
            if (!m_conversion.isPrimitive() && m_property != null) {
                String stype = m_conversion.getTypeName();
                String dtype = m_property.getSetValueType();
                mb.appendCreateCast(stype, dtype);
            }
            
            // store result using object reference loaded earlier
            m_property.genStore(mb);

        } else {
            
            // unmarshal and compare value
            BranchWrapper ifmiss = null;
            if (m_valueStyle == ValueChild.TEXT_STYLE ||
                m_valueStyle == ValueChild.CDATA_STYLE) {
                
                // unmarshal text value directly and let handler convert
                mb.appendCallVirtual(UNMARSHAL_TEXT_NAME,
                    UNMARSHAL_TEXT_SIGNATURE);
                
            } else if (m_property.isOptional() &&
                (isatt || m_container.isContentOrdered())) {
                
                // parse optional attribute or element value
                m_conversion.genParseOptional(isatt, mb);
                mb.appendDUP();
                ifmiss = mb.appendIFNULL(this);
                
            } else {
                
                // parse required attribute or element value
                m_conversion.genParseRequired(isatt, mb);
            }
            
            // compare unmarshalled value with required constant
            mb.appendDUP();
            mb.appendLoadConstant(m_constantValue);
            mb.appendCallVirtual("java.lang.String.equals",
                "(Ljava/lang/Object;)Z");
            BranchWrapper ifmatch = mb.appendIFNE(this);
            
            // throw exception on comparison error
            mb.appendCreateNew("java.lang.StringBuffer");
            mb.appendDUP();
            mb.appendLoadConstant("Expected constant value \"" +
                m_constantValue + "\", found \"");
            mb.appendCallInit("java.lang.StringBuffer",
                "(Ljava/lang/String;)V");
            mb.appendSWAP();
            mb.appendCallVirtual("java.lang.StringBuffer.append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
            mb.appendLoadConstant("\"");
            mb.appendCallVirtual("java.lang.StringBuffer.append",
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
            mb.appendCallVirtual("java.lang.StringBuffer.toString",
                "()Ljava/lang/String;");
            mb.loadContext();
            mb.appendSWAP();
            mb.appendCallVirtual(UNMARSHALLING_THROWEXCEPTION_METHOD,
                UNMARSHALLING_THROWEXCEPTION_SIGNATURE);
            mb.appendACONST_NULL();
            
            // finish by setting target for branch
            mb.targetNext(ifmatch);
            mb.targetNext(ifmiss);
            mb.appendPOP();
        }
    }

    /**
     * Generate marshalling code. This internal method generates the
     * necessary code for handling the marshalling operation.  The code
     * generated by this method restores the stack to the original state
     * when done.
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    private void genMarshal(ContextMethodBuilder mb) throws JiBXException {
        if (m_constantValue == null) {
            
            // first part of generated instruction sequence is to generate a
            //  check for an optional property present, then load the context
            //  and the name information (if present) for later use, then
            //  finally load the actual property value
            BranchWrapper ifmiss = null;
            String type = m_property.getTypeName();
            if (m_property.hasTest()) {
                mb.loadObject();
                ifmiss = m_property.genTest(mb);
            } else if (m_isNillable && !ClassItem.isPrimitive(type)) {
                
                // check for null object
                BranchWrapper ifhit;
                if (m_property.isImplicit()) {
                    mb.appendDUP();
                    ifhit = mb.appendIFNONNULL(this);
                    mb.appendPOP();
                } else {
                    mb.loadObject();
                    m_property.genLoad(mb);
                    ifhit = mb.appendIFNONNULL(this);
                }
                
                // generate empty element with xsi:nil="true"
                mb.loadContext();
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
            if (m_name != null) {
                
                // handle implicit property by first saving value to local, then
                //  reloading after the name information is on the stack
                Type tobj = ClassItem.typeFromName(type);
                if (m_property.isImplicit()) {
                    mb.defineSlot(this, tobj);
                }
                m_name.genPushIndexPair(mb);
                if (m_property.isImplicit()) {
                    mb.appendLoadLocal(mb.getSlot(this));
                    mb.freeSlot(this);
                }
            }
            if (!m_property.isImplicit()) {
                if (m_property.isFlagOnly()) {
                    mb.appendLoadConstant("");
                } else {
                    mb.loadObject();
                    m_property.genLoad(mb);
                }
            }
            
            // check for object identity definition (accessed through property)
            StringConversion convert = m_conversion;
            if (m_identType == REF_IDENT) {
                m_idRefMap.getImplComponent().genLoadId(mb);
                convert = BindingDefinition.s_stringConversion;
                type = "java.lang.String";
            }
            
            // convert to expected type if object
            if (!ClassItem.isPrimitive(type)) {
                mb.appendCreateCast(type);
            }
                
            // convert and marshal value
            boolean isatt = m_valueStyle == ValueChild.ATTRIBUTE_STYLE;
            if (m_valueStyle == ValueChild.TEXT_STYLE ||
                m_valueStyle == ValueChild.CDATA_STYLE) {
                convert.genToText(type, mb);
                String name = (m_valueStyle == ValueChild.TEXT_STYLE) ?
                    MARSHAL_TEXT_NAME : MARSHAL_CDATA_NAME;
                mb.appendCallVirtual(name, MARSHAL_TEXT_SIGNATURE);
            } else if (m_property.isOptional()) {
                convert.genWriteOptional(isatt, type, mb);
            } else {
                convert.genWriteRequired(isatt, type, mb);
            }
            
            // finish by setting target for missing optional property test
            mb.targetNext(ifmiss);
            
        } else {
            
            // just write constant value directly
            if (m_name != null) {
                m_name.genPushIndexPair(mb);
            }
            mb.appendLoadConstant(m_constantValue);
            switch (m_valueStyle) {
                case ATTRIBUTE_STYLE:
                    mb.appendCallVirtual(MARSHAL_ATTRIBUTE, MARSHAL_SIGNATURE);
                    break;
                case ELEMENT_STYLE:
                    mb.appendCallVirtual(MARSHAL_ELEMENT, MARSHAL_SIGNATURE);
                    break;
                case TEXT_STYLE:
                    mb.appendCallVirtual(MARSHAL_TEXT_NAME,
                        MARSHAL_TEXT_SIGNATURE);
                    break;
                case CDATA_STYLE:
                    mb.appendCallVirtual(MARSHAL_CDATA_NAME,
                        MARSHAL_TEXT_SIGNATURE);
                    break;
            }
        }
    }

    /**
     * Get property name. If the child has an associated property this returns
     * the name of that property.
     * 
     * @return name for child property
     */
    public String getPropertyName() {
        if (m_property == null) {
            return null;
        } else {
            return m_property.getName();
        }
    }
    
    /**
     * Check if implicit.
     * 
     * @return <code>true</code> if implicit, <code>false</code> if not
     */
    public boolean isImplicit() {
        return m_property.isThis();
    }
    
    /**
     * Switch property from "this" to "implicit".
     */
    public void switchProperty() {
        m_property.switchProperty();
    }
    
    //
    // IComponent interface method definitions

    public boolean isOptional() {
        return m_property.isOptional();
    }
    
    public boolean hasAttribute() {
        return m_valueStyle == ATTRIBUTE_STYLE;
    }

    public void genAttrPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        
        // make sure this is an appropriate call
        if (m_valueStyle != ATTRIBUTE_STYLE || m_name == null) {
            throw new JiBXException("Method call on invalid structure");
        }
        
        // generate load of the unmarshalling context and the name information,
        //  then just call the attribute check method
        mb.loadContext();
        m_name.genPushUriPair(mb);
        mb.appendCallVirtual(CHECK_ATTRIBUTE_NAME, CHECK_SIGNATURE);
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_valueStyle == ATTRIBUTE_STYLE) {
            genUnmarshal(mb);
        }
    }

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_valueStyle == ATTRIBUTE_STYLE) {
            genMarshal(mb);
        }
    }

    public boolean hasContent() {
        return m_valueStyle != ATTRIBUTE_STYLE;
    }
    

    public void genContentPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        
        // make sure this is an appropriate call
        if (m_valueStyle == ELEMENT_STYLE) {
            
            // generate load of the unmarshalling context and the name
            //  information, then just call the attribute check method
            mb.loadContext();
            m_name.genPushUriPair(mb);
            mb.appendCallVirtual(CHECK_ELEMENT_NAME, CHECK_SIGNATURE);
            
        } else if (m_valueStyle == ATTRIBUTE_STYLE) {
            throw new JiBXException("Method call on invalid structure");
        } else {
            
            // handle text content by always returned true
            mb.appendICONST_1();
            
        }
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_valueStyle != ATTRIBUTE_STYLE) {
            genUnmarshal(mb);
        }
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_valueStyle != ATTRIBUTE_STYLE) {
            genMarshal(mb);
        }
    }
    
    public void genNewInstance(ContextMethodBuilder mb) {
        throw new IllegalStateException
            ("Internal error - no instance creation");
    }
    
    public String getType() {
        return m_type;
    }

    public boolean hasId() {
        return m_identType == DEF_IDENT;
    }
    
    public void genLoadId(ContextMethodBuilder mub) throws JiBXException {
        m_property.genLoad(mub);
    }
    
    public NameDefinition getWrapperName() {
        return (m_valueStyle == ELEMENT_STYLE) ? m_name : null;
    }

    public void setLinkages() throws JiBXException {
        if (m_identType == REF_IDENT) {
            String type;
            if (m_property == null) {
                type = m_objContext.getBoundClass().getClassFile().getName();
            } else {
                type = m_property.getTypeName();
            }
            m_idRefMap = m_container.getDefinitionContext().
                getClassMapping(type);
            if (m_idRefMap == null) {
                throw new JiBXException("No mapping defined for " +
                    type + " used as IDREF target");
            }
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        if (m_valueStyle == ELEMENT_STYLE) {
            if (m_isNillable) {
                System.out.print("nillable ");
            }
            System.out.print("element");
        } else if (m_valueStyle == ATTRIBUTE_STYLE) {
            System.out.print("attribute");
        } else if (m_valueStyle == TEXT_STYLE) {
            System.out.print("text");
        } else if (m_valueStyle == CDATA_STYLE) {
            System.out.print("cdata");
        }
        if (m_name != null) {
            System.out.print(" " + m_name.toString());
        }
        if (m_property != null) {
            System.out.print(" from " + m_property.toString());
        }
        System.out.println();
    }
}
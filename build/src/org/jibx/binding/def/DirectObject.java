/*
Copyright (c) 2003-2010, Dennis M. Sosnoski. All rights reserved.

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
import org.apache.bcel.generic.Type;

import org.jibx.binding.classes.*;
import org.jibx.runtime.JiBXException;

/**
 * Linkage to object with supplied marshaller and unmarshaller. This provides
 * methods used to generate code for calling the supplied classes.
 *
 * @author Dennis M. Sosnoski
 */
public class DirectObject implements IComponent
{
    //
    // Constants and such related to code generation.

    private static final String GETUNMARSHALLER_METHOD =
        "org.jibx.runtime.IUnmarshallingContext.getUnmarshaller";
    private static final String GETUNMARSHALLER_SIGNATURE =
        "(Ljava/lang/String;)Lorg/jibx/runtime/IUnmarshaller;";
    private static final String GETMARSHALLER_METHOD =
        "org.jibx.runtime.IMarshallingContext.getMarshaller";
    private static final String GETMARSHALLER_SIGNATURE =
        "(Ljava/lang/String;)Lorg/jibx/runtime/IMarshaller;";
    private static final String MARSHALLER_MARSHAL_METHOD =
        "org.jibx.runtime.IMarshaller.marshal";
    private static final String MARSHALLER_MARSHAL_SIGNATURE =
        "(Ljava/lang/Object;Lorg/jibx/runtime/IMarshallingContext;)V";
    private static final String UNMARSHALLER_TESTPRESENT_METHOD =
        "org.jibx.runtime.IUnmarshaller.isPresent";
    private static final String UNMARSHALLER_TESTPRESENT_SIGNATURE =
        "(Lorg/jibx/runtime/IUnmarshallingContext;)Z";
    private static final String UNMARSHALLER_UNMARSHAL_METHOD =
        "org.jibx.runtime.IUnmarshaller.unmarshal";
    private static final String UNMARSHALLER_UNMARSHAL_SIGNATURE =
        "(Ljava/lang/Object;Lorg/jibx/runtime/IUnmarshallingContext;)" +
        "Ljava/lang/Object;";
    private static final String ABSTRACTMARSHALLER_INTERFACE =
        "org.jibx.runtime.IAbstractMarshaller";
    private static final String ABSTRACTMARSHAL_METHOD =
        "org.jibx.runtime.IAbstractMarshaller.baseMarshal";
    private static final String ABSTRACTMARSHAL_SIGNATURE =
        MARSHALLER_MARSHAL_SIGNATURE;
    private static final String ALIASABLE_INTERFACETYPE =
        "Lorg/jibx/runtime/IAliasable;";
    private static final String ANY_INIT_SIG = "()V";
    private static final String ANY_INITCLASS_SIG = "(Ljava/lang/String;)V";
    private static final String MARSHALUNMARSHAL_INIT_SIG =
        "(Ljava/lang/String;ILjava/lang/String;)V";
    private static final String MARSHALONLY_INIT_SIG = "(ILjava/lang/String;)V";
    private static final String UNMARSHALONLY_INIT_SIG =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String MARSHALUNMARSHAL_INITCLASS_SIG =
        "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V";
    private static final String MARSHALONLY_INITCLASS_SIG =
        "(ILjava/lang/String;Ljava/lang/String;)V";
    private static final String UNMARSHALONLY_INITCLASS_SIG =
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
    private static final String PUSH_NAMESPACES_METHOD =
        "org.jibx.runtime.IMarshallingContext.pushNamespaces";
    private static final String PUSH_NAMESPACES_SIG =
        "(Ljava/lang/String;)V";
    private static final String POP_NAMESPACES_METHOD =
        "org.jibx.runtime.IMarshallingContext.popNamespaces";
    private static final String POP_NAMESPACES_SIG = "()V";

    //
    // Actual instance data.

    /** Containing binding definition structure. */
    private final IContainer m_parent;
    
    /** Definition context for resolving names. */
    private final DefinitionContext m_defContext;

    /** Abstract mapping flag. If this is set the marshalling code will call the
      special interface method used to verify the type of a passed object and
      marshal it with the proper handling. */
    private final boolean m_isAbstract;
    
    /** Element name information (<code>null</code> if no bound element). */
    private final NameDefinition m_name;
    
    /** Class handled by this binding. */
    private final ClassFile m_targetClass;

    /** Marshaller base class. */
    private final ClassFile m_marshallerBase;

    /** Unmarshaller base class. */
    private final ClassFile m_unmarshallerBase;
    
    /** Mapping name supplied flag. */
    private final boolean m_fixedName;
    
    /** Binding factory name used for activating namespace translation on
     marshalling (<code>null</code> if translation not required). */
    private final String m_factoryName;
    
    /** Marshaller class (lazy create on first use if name supplied). */
    private ClassFile m_marshaller;

    /** Unmarshaller class (lazy create on first use if name supplied). */
    private ClassFile m_unmarshaller;
    
    /** Name used for the mapping in binding tables. */
    private String m_mappingName;

    /**
     * Constructor.
     *
     * @param parent containing binding definition structure
     * @param defc active definitions context
     * @param target class handled by this binding
     * @param abs abstract mapping flag
     * @param mcf marshaller class information (<code>null</code> if input only
     * binding)
     * @param ucf unmarshaller class information (<code>null</code> if output
     * only binding)
     * @param mapname mapping name in binding definition tables
     * (<code>null</code> if to be constructed)
     * @param name element name information (<code>null</code> if no element
     * name)
     * @param factname binding factory name for marshalling namespace
     * translation (<code>null</code> if no namespace translation)
     * @throws JiBXException if configuration error
     */
    public DirectObject(IContainer parent, DefinitionContext defc,
        ClassFile target, boolean abs, ClassFile mcf, ClassFile ucf,
        String mapname, NameDefinition name, String factname)
        throws JiBXException {
        
        // initialize the basic information
        m_parent = parent;
        m_defContext = (defc == null) ? m_parent.getDefinitionContext() : defc;
        m_isAbstract = abs;
        m_targetClass = target;
        m_marshallerBase = mcf;
        m_unmarshallerBase = ucf;
        m_name = name;
        m_fixedName = mapname != null;
        m_mappingName = mapname;
        m_factoryName = factname;
        
        // check for marshaller and/or unmarshaller configuration valid
        if (name == null) {
            
            // no name, make sure there's an appropriate constructor
            if (mcf != null) {
                if (mcf.getInitializerMethod(ANY_INIT_SIG) != null) {
                    m_marshaller = mcf;
                } else if (mcf.getInitializerMethod
                    (ANY_INITCLASS_SIG) == null) {
                    throw new JiBXException("Marshaller class " +
                        mcf.getName() + " requires name to be set");
                }
            }
            if (ucf != null) {
                if (ucf.getInitializerMethod(ANY_INIT_SIG) != null) {
                    m_unmarshaller = ucf;
                } else if (ucf.getInitializerMethod
                    (ANY_INITCLASS_SIG) == null) {
                    throw new JiBXException("Unmarshaller class " +
                        ucf.getName() + " requires name to be set");
                }
            }
        } 
        if (name != null) {
            
            // name supplied, make sure both support it
            if (mcf != null && !mcf.isImplements(ALIASABLE_INTERFACETYPE)) {
                throw new JiBXException("Marshaller class " +
                    mcf.getName() + " does not allow name to be set");
            }
            if (ucf != null && !ucf.isImplements(ALIASABLE_INTERFACETYPE)) {
                throw new JiBXException("Unmarshaller class " +
                    ucf.getName() + " does not allow name to be set");
            }
        }
    }

    /**
     * Load name used to identify mapping in binding tables.
     *
     * @param mb method builder
     */
    private void genLoadName(ContextMethodBuilder mb) throws JiBXException {
        if (m_mappingName == null) {
            
            // first set name in case called recursively
            BindingDefinition bdef = m_parent.getBindingRoot();
            m_mappingName =
                bdef.getMarshallerUnmarshallerName(m_targetClass.getName());
            
            // now generate the classes (if needed) and set the names
            String mclas = null;
            String uclas = null;
            if (bdef.isOutput()) {
                if (m_marshaller == null) {
                    createSubclass(true);
                }
                mclas = m_marshaller.getName();
            }
            if (bdef.isInput()) {
                if (m_unmarshaller == null) {
                    createSubclass(false);
                }
                uclas = m_unmarshaller.getName();
            }
            bdef.setMarshallerUnmarshallerClasses(m_mappingName, mclas, uclas);
            
        }
        mb.appendLoadConstant(m_mappingName);
    }

    /**
     * Create aliased subclass for marshaller or unmarshaller with element name
     * defined by binding. If the same aliasable superclass is defined for use
     * as both a marshaller and an unmarshaller a single subclass is generated
     * to handle both uses.
     *
     * @param out <code>true</code> if alias needed for marshalling,
     * <code>false</code> if for unmarshalling
     * @throws JiBXException on configuration error
     */
    private void createSubclass(boolean out) throws JiBXException {
            
        // find initializer call to be used
        ClassItem init = null;
        boolean dual = false;
        boolean classed = true;
        boolean named = false;
        ClassFile base = out ? m_marshallerBase : m_unmarshallerBase;
        
        // check for name supplied
        if (m_name == null) {
            init = base.getInitializerMethod(ANY_INITCLASS_SIG);
            classed = init != null;
        }
        if (init == null) {
            named = true;
            if (m_unmarshallerBase == m_marshallerBase) {
            
                // single class, look first for signature with class name
                init = base.getInitializerMethod
                    (MARSHALUNMARSHAL_INITCLASS_SIG);
                if (init == null) {
                    classed = false;
                    init = base.getInitializerMethod(MARSHALUNMARSHAL_INIT_SIG);
                }
                dual = true;
            
            } else {
            
                // using only one function, first check one way with class name
                String sig = out ?
                    MARSHALONLY_INITCLASS_SIG : UNMARSHALONLY_INITCLASS_SIG;
                init = base.getInitializerMethod(sig);
                if (init == null) {
                
                    // now check dual function with class name
                    sig = MARSHALUNMARSHAL_INITCLASS_SIG;
                    init = base.getInitializerMethod(sig);
                    dual = true;
                    if (init == null) {
                    
                        // now try alternatives without class name included
                        classed = false;
                        sig = out ? MARSHALONLY_INIT_SIG :
                            UNMARSHALONLY_INIT_SIG;
                        init = base.getInitializerMethod(sig);
                        dual = false;
                        if (init == null) {
                            sig = MARSHALUNMARSHAL_INIT_SIG;
                            init = base.getInitializerMethod(sig);
                            dual = true;
                        }
                    
                    }
                }
            }
        }
        
        // make sure the initializer is defined and public
        if (init == null || ((init.getAccessFlags() &
            Constants.ACC_PUBLIC) == 0)) {
            throw new JiBXException("No usable constructor for " +
                "marshaller or unmarshaller based on " + base.getName());
        }
        
        // get package and target name from bound class
        String tname = base.getName();
        int split = tname.lastIndexOf('.');
        if (split >= 0) {
            tname = tname.substring(split+1); 
        }
        
        // create the helper class
        BindingDefinition def = m_parent.getBindingRoot();
        String pack = def.getDefaultPackage();
        if (pack.length() > 0) {
            pack += '.';
        }
        String suffix = "";
        if (!m_fixedName) {
            split = m_mappingName.lastIndexOf('.') + 1;
            StringBuffer buff =
                new StringBuffer(m_mappingName.substring(split));
            for (int i = 0; i < buff.length(); i++) {
                char chr = buff.charAt(i);
                if ((i == 0 && !Character.isJavaIdentifierStart(chr)) ||
                    !Character.isJavaIdentifierPart(chr)) {
                    buff.setCharAt(i, '_');
                }
                suffix = buff.toString();
            }
            suffix = m_mappingName.substring(split).replace('-', '_').
                replace('[', '_').replace(']', '_');
        }
        String name = pack + def.getPrefix() + tname + suffix;
        String[] intfs = def.isInput() ? (def.isOutput() ?
            MappingDefinition.BOTH_INTERFACES :
            MappingDefinition.UNMARSHALLER_INTERFACES) :
            MappingDefinition.MARSHALLER_INTERFACES;
        ClassFile cf = new ClassFile(name, def.getDefaultRoot(),
            base, Constants.ACC_PUBLIC, intfs);
        
        // add the public constructor method
        ExceptionMethodBuilder mb = new ExceptionMethodBuilder("<init>",
            Type.VOID, new Type[0], cf, Constants.ACC_PUBLIC);
        
        // call the superclass constructor
        mb.appendLoadLocal(0);
        if (m_name == null) {
            if (named) {
                if (dual) {
                    mb.appendACONST_NULL();
                    mb.appendICONST_0();
                    mb.appendACONST_NULL();
                } else if (out) {
                    mb.appendICONST_0();
                    mb.appendACONST_NULL();
                } else {
                    mb.appendACONST_NULL();
                    mb.appendACONST_NULL();
                }
            }
        } else {
            if (dual) {
                m_name.genPushUri(mb);
                m_name.genPushIndexPair(mb);
            } else if (out) {
                m_name.genPushIndexPair(mb);
            } else {
                m_name.genPushUriPair(mb);
            }
        }
        if (classed) {
            mb.appendLoadConstant(m_targetClass.getName());
        }
        mb.appendCallInit(base.getName(), init.getSignature());
            
        // finish with return
        mb.appendReturn();
        mb.codeComplete(false);
        
        // add method and class
        mb.addMethod();
        cf = MungedClass.getUniqueSupportClass(cf);
        
        // save as appropriate type(s)
        if (dual) {
            m_marshaller = m_unmarshaller = cf;
        } else if (out) {
            m_marshaller = cf;
        } else {
            m_unmarshaller = cf;
        }
    }

    /**
     * Generate presence test code for this mapping. The generated code finds
     * the unmarshaller and calls the test method, leaving the result on the
     * stack.
     *
     * @param mb method builder
     * @throws JiBXException if error in generating code
     */
    public void genTestPresent(ContextMethodBuilder mb) throws JiBXException {
        
        // start with call to unmarshalling context method to get the
        //  unmarshaller instance
        mb.loadContext();
        genLoadName(mb);
        mb.appendCallInterface(GETUNMARSHALLER_METHOD,
            GETUNMARSHALLER_SIGNATURE);
        
        // call the actual unmarshaller test method with context as argument
        mb.loadContext();
        mb.appendCallInterface(UNMARSHALLER_TESTPRESENT_METHOD,
            UNMARSHALLER_TESTPRESENT_SIGNATURE);
    }

    /**
     * Generate unmarshalling code for this mapping. The generated code finds
     * and calls the unmarshaller with the object to be unmarshaller (which
     * needs to be loaded on the stack by the code prior to this call, but may
     * be <code>null</code>). The unmarshalled object (or <code>null</code> in
     * the case of a missing optional item) is left on the stack after this
     * call. The calling method generally needs to cast this object reference to
     * the appropriate type before using it.
     *
     * @param mb method builder
     * @throws JiBXException if error in generating code
     */
    public void genUnmarshal(ContextMethodBuilder mb) throws JiBXException {
        
        // start with call to unmarshalling context method to get the
        //  unmarshaller instance
        mb.loadContext();
        genLoadName(mb);
        mb.appendCallInterface(GETUNMARSHALLER_METHOD,
            GETUNMARSHALLER_SIGNATURE);
        
        // call the actual unmarshaller with object and context as arguments
        mb.appendSWAP();
        mb.loadContext();
        mb.appendCallInterface(UNMARSHALLER_UNMARSHAL_METHOD,
            UNMARSHALLER_UNMARSHAL_SIGNATURE);
    }

    /**
     * Generate marshalling code for this mapping. The generated code finds
     * and calls the marshaller, passing the object to be marshalled (which
     * should have been loaded to the stack by the prior generated code)..
     *
     * @param mb method builder
     * @throws JiBXException if error in configuration
     */
    public void genMarshal(ContextMethodBuilder mb) throws JiBXException {
        
        // start by pushing namespace translation, if needed
        if (m_factoryName != null) {
            mb.loadContext();
            mb.appendLoadConstant(m_factoryName);
            mb.appendCallInterface(PUSH_NAMESPACES_METHOD,
                PUSH_NAMESPACES_SIG);
        }
        
        // generate call to marshalling context method to get the marshaller
        //  instance
        mb.loadContext();
        genLoadName(mb);
        mb.appendCallInterface(GETMARSHALLER_METHOD, GETMARSHALLER_SIGNATURE);
        
        // check for an abstract mapping being used
        if (m_isAbstract) {
            
            // make sure returned marshaller implements required interface
            mb.appendCreateCast(ABSTRACTMARSHALLER_INTERFACE);
        
            // swap to reorder object and marshaller
            mb.appendSWAP();
        
            // call indirect marshaller for abstract base class with the object
            //  itself and context as arguments
            mb.loadContext();
            mb.appendCallInterface(ABSTRACTMARSHAL_METHOD,
                ABSTRACTMARSHAL_SIGNATURE);
                
        } else {
        
            // swap to reorder object and marshaller
            mb.appendSWAP();
        
            // call the direct marshaller with the object itself and context as
            //  arguments
            mb.loadContext();
            mb.appendCallInterface(MARSHALLER_MARSHAL_METHOD,
                MARSHALLER_MARSHAL_SIGNATURE);
        }
        
        // finish by popping namespace translation, if used
        if (m_factoryName != null) {
            mb.loadContext();
            mb.appendCallInterface(POP_NAMESPACES_METHOD,
                POP_NAMESPACES_SIG);
        }
    }

    /**
     * Get target class for mapping.
     *
     * @return target class information
     */
    public ClassFile getTargetClass() {
        return m_targetClass;
    }

    /**
     * Get marshaller class used for mapping. If a name has been supplied the
     * actual marshaller class is created by extending the base class the first
     * time this method is called.
     *
     * @return marshaller class information
     * @throws JiBXException if error in transformation
     */
    public ClassFile getMarshaller() throws JiBXException {
        if (m_marshaller == null && m_marshallerBase != null) {
            createSubclass(true);
        }
        return m_marshaller;
    }

    /**
     * Get unmarshaller class used for mapping. If a name has been supplied the
     * actual unmarshaller class is created by extending the base class the
     * first time this method is called.
     *
     * @return unmarshaller class information
     * @throws JiBXException if error in transformation
     */
    public ClassFile getUnmarshaller() throws JiBXException {
        if (m_unmarshaller == null && m_unmarshallerBase != null) {
            createSubclass(false);
        }
        return m_unmarshaller;
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
        return m_targetClass.getFile().getName();
    }

    public boolean hasId() {
        return false;
    }

    public void genLoadId(ContextMethodBuilder mb) {
        throw new IllegalStateException("Internal error - no ID allowed");
    }
    
    public NameDefinition getWrapperName() {
        return m_name;
    }

    public void setLinkages() throws JiBXException {
        if (m_name != null) {
            m_name.fixNamespace(m_defContext);
        }
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.println("direct marshaller/unmarshaller reference" );
    }
}
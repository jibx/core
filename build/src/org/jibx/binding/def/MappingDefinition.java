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

import java.util.ArrayList;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.Type;
import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.BranchTarget;
import org.jibx.binding.classes.BranchWrapper;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.ClassItem;
import org.jibx.binding.classes.ContextMethodBuilder;
import org.jibx.binding.classes.ExceptionMethodBuilder;
import org.jibx.binding.classes.MethodBuilder;
import org.jibx.binding.classes.MungedClass;
import org.jibx.runtime.JiBXException;

/**
 * Normal mapping with defined binding. This is used for a mapping definition
 * which includes detailed binding information (rather than marshaller and
 * unmarshaller classes which handle the binding directly).
 *
 * @author Dennis M. Sosnoski
 */
public class MappingDefinition extends MappingBase
{
    //
    // Constants and such related to code generation.
    
    // definitions used in generating the adapter class
    private static final String ADAPTERCLASS_SUFFIX = "_access";
    private static final String MARSHAL_METHODNAME = "marshal";
    private static final String BASEMARSHAL_METHODNAME = "baseMarshal";
    private static final String UNMARSHAL_METHODNAME = "unmarshal";
    private static final String ISPRESENT_METHODNAME = "isPresent";
    private static final String UNMARSHALCONTEXT_CLASS =
        "org.jibx.runtime.impl.UnmarshallingContext";
    private static final String MARSHALCONTEXT_CLASS =
        "org.jibx.runtime.impl.MarshallingContext";
    private static final String UNMARSHAL_ISATMETHOD =
        "org.jibx.runtime.IUnmarshallingContext.isAt";
    private static final String UNMARSHAL_ISATSIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)Z";
    private static final String GETINDEX_METHOD =
        "org.jibx.runtime.IMarshallable.JiBX_getIndex";
    private static final String UNMARSHALLERPRESENT_METHOD =
        "org.jibx.runtime.IUnmarshaller.isPresent";
    private static final String UNMARSHALLERPRESENT_SIGNATURE =
        "(Lorg/jibx/runtime/IUnmarshallingContext;)Z";
    private static final String UNMARSHALCONTEXT_INTERFACE =
        "org.jibx.runtime.IUnmarshallingContext";
    private static final String MARSHALCONTEXT_INTERFACE =
        "org.jibx.runtime.IMarshallingContext";
    private static final String CURRENTELEMENT_METHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.currentNameString";
    private static final String CURRENTELEMENT_SIGNATURE =
        "()Ljava/lang/String;";
    private static final String PARSERNEXT_METHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.next";
    private static final String PARSERNEXT_SIGNATURE = "()I";
    private static final String CLOSESTART_METHOD =
        "org.jibx.runtime.impl.MarshallingContext.closeStartContent";
    private static final String CLOSESTART_SIGNATURE =
        "()Lorg/jibx/runtime/impl/MarshallingContext;";
    private static final String ADDUNMARSHALLER_METHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.addUnmarshalling";
    private static final String ADDUNMARSHALLER_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
    private static final String REMOVEUNMARSHALLER_METHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.removeUnmarshalling";
    private static final String REMOVEUNMARSHALLER_SIGNATURE =
        "(Ljava/lang/String;)V";
    private static final String ADDMARSHALLER_METHOD =
        "org.jibx.runtime.impl.MarshallingContext.addMarshalling";
    private static final String ADDMARSHALLER_SIGNATURE =
        "(Ljava/lang/String;Ljava/lang/String;)V";
    private static final String REMOVEMARSHALLER_METHOD =
        "org.jibx.runtime.impl.MarshallingContext.removeMarshalling";
    private static final String REMOVEMARSHALLER_SIGNATURE =
        "(Ljava/lang/String;)V";
    private static final String PUSH_NAMESPACES_METHOD =
        "org.jibx.runtime.IMarshallingContext.pushNamespaces";
    private static final String PUSH_NAMESPACES_SIG =
        "(Ljava/lang/String;)V";
    private static final String POP_NAMESPACES_METHOD =
        "org.jibx.runtime.IMarshallingContext.popNamespaces";
    private static final String POP_NAMESPACES_SIG = "()V";
    private static final String EQUALS_METHODNAME = "java.lang.String.equals";
    private static final String EQUALS_SIGNATURE = "(Ljava/lang/Object;)Z";
    
    // argument list for the unmarshaller methods
    private static final Type[] ISPRESENT_METHOD_ARGS =
    {
        ClassItem.typeFromName("org.jibx.runtime.IUnmarshallingContext")
    };
    private static final Type[] UNMARSHAL_METHOD_ARGS =
    {
        Type.OBJECT,
        ClassItem.typeFromName("org.jibx.runtime.IUnmarshallingContext")
    };
    
    // argument list for the marshaller methods
    private static final Type[] MARSHAL_METHOD_ARGS =
    {
        Type.OBJECT,
        ClassItem.typeFromName("org.jibx.runtime.IMarshallingContext")
    };
    
    //
    // Data shared with other classes within package
    
    // interface list for adapter with unmarshaller only
    /*package*/ static final String[] UNMARSHALLER_INTERFACES =
    {
        UNMARSHALLER_INTERFACE
    };
    
    // interface list for adapter with marshaller only
    /*package*/ static final String[] MARSHALLER_INTERFACES =
    {
        MARSHALLER_INTERFACE
    };
    
    // interface list for adapter with both unmarshaller and marshaller
    /*package*/ static final String[] BOTH_INTERFACES =
    {
        UNMARSHALLER_INTERFACE, MARSHALLER_INTERFACE
    };
    
    //
    // Actual instance data.
    
    /** Containing binding definition structure. */
    private final IContainer m_container;
    
    /** Definition context for mapping. */
    private final DefinitionContext m_defContext;
    
    /** Class linked to mapping. */
    private final BoundClass m_class;

    /** Mapped element name (may be <code>null</code> if abstract mapping). */
    private final NameDefinition m_name;
    
    /** Abstract mapping flag. */
    private final boolean m_isAbstract;
    
    /** Name of abstract base type. */
    private final String m_baseType;
    
    /** Binding structure defining the mapping. */
    private final ObjectBinding m_binding;
    
    /** Abstract binding this one is based on (<code>null</code> if not an
     extension). */
    private IMapping m_baseMapping;
    
    /** Constructed marshaller class. */
    private ClassFile m_marshaller;

    /** Constructed unmarshaller class. */
    private ClassFile m_unmarshaller;
    
    /** Mappings which extend this one (<code>null</code> if none). */
    private ArrayList m_extensions;
    
    /** Reference type of mapping, as fully qualified class name. */
    private String m_referenceType;
    
    /**
     * Constructor.
     *
     * @param contain containing binding definition structure
     * @param defc definition context for this mapping
     * @param type bound class name
     * @param name mapped element name information (<code>null</code> if none)
     * @param tname qualified type name for abstract mapping (<code>null</code>
     * if none)
     * @param abs abstract mapping flag
     * @param base abstract mapping extended by this one
     * @param bind binding definition component (may be <code>null</code> if a
     * concrete mapping)
     * @param nillable flag for nillable element
     * @throws JiBXException if class definition not found
     */
    public MappingDefinition(IContainer contain, DefinitionContext defc,
        String type, NameDefinition name, String tname, boolean abs,
        String base, ObjectBinding bind, boolean nillable) throws JiBXException {
        super(contain, type, tname);
        m_binding = bind;
        if (name == null) {
            setWrappedComponent(bind);
        } else {
            setWrappedComponent(new ElementWrapper(defc, name, bind, nillable));
        }
        m_container = contain;
        m_defContext = defc;
        m_class = BoundClass.getInstance(type, null);
        m_referenceType = type == null ? "java.lang.Object" : type;
        m_name = name;
        m_isAbstract = abs;
        m_baseType = base;
    }

    /**
     * Check if one or more namespaces are defined for element.
     *
     * @return <code>true</code> if namespaces are defined, <code>false</code>
     * if not
     */
    /*package*/ boolean hasNamespace() {
        return m_defContext.hasNamespace();
    }

    /**
     * Generate code for loading namespace index and URI arrays.
     *
     * @param mb method builder for generated code
     */
    /*package*/ void genLoadNamespaces(MethodBuilder mb) {
        m_defContext.genLoadNamespaces(mb);
    }

    /**
     * Get the mapped class information. This implements the method used by the
     * base class.
     *
     * @return information for mapped class
     */
    public BoundClass getBoundClass() {
        return m_class;
    }
    
    /**
     * Links extension mappings to their base mappings. This must be done before
     * the more general linking step in order to determine which abstract
     * mappings are standalone and which are extended by other mappings
     *
     * @throws JiBXException if error in linking
     */
    public void linkMappings() throws JiBXException {
        if (m_baseType != null) {
            m_baseMapping = m_defContext.getClassMapping(m_baseType);
            if (m_baseMapping == null) {
                throw new JiBXException("Mapping for base class " + m_baseType +
                    " not defined");
            }
            m_baseMapping.addExtension(this);
        }
        m_defContext.linkMappings();
    }
    
    //
    // IMapping interface method definitions
    
    public String getBoundType() {
        return m_class.getClassName();
    }
    
    public String getReferenceType() {
        return m_referenceType;
    }
    
    public IComponent getImplComponent() {
        return m_component;
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
        m_defContext.addNamespace(ns);
    }

    public boolean isAbstract() {
        return m_isAbstract;
    }

    public boolean isBase() {
        return m_extensions != null && m_extensions.size() > 0;
    }

    public void addExtension(MappingDefinition mdef) throws JiBXException {
        if (m_extensions == null) {
            m_extensions = new ArrayList();
        }
        if (!m_extensions.contains(mdef)) {
            m_extensions.add(mdef);
        }
        ClassFile cf = mdef.getBoundClass().getClassFile();
        if (!cf.isSuperclass(m_referenceType) &&
            !cf.isImplements(m_referenceType)) {
            m_referenceType = "java.lang.Object";
        }
    }
    
    public IComponent buildRef(IContainer parent, IContextObj objc, String type,
        PropertyDefinition prop) throws JiBXException {
        if (prop.isThis()) {
            
            // directly incorporate base mapping definition
            return new BaseMappingWrapper(m_component);
            
        } else if (m_isAbstract && m_extensions == null) {
            
            // create reference to use mapping definition directly
            ComponentProperty comp =
                new ComponentProperty(prop, m_component, false);
            if (!hasAttribute() && !hasContent()) {
                comp.setForceUnmarshal(true);
            }
            return comp;
            
        } else {
            
            // create link to mapping definition
            DirectObject dobj = new DirectObject(m_container, null,
                m_class.getClassFile(), m_isAbstract || m_extensions != null,
                m_marshaller, m_unmarshaller, getMappingName(), null, null);
            return new DirectProperty(prop, dobj);
            
        }
    }
    
    public ArrayList getNamespaces() {
        return m_defContext.getNamespaces();
    }

    /**
     * Add abstract marshaller interface to handler class. This adds the
     * interface and generates the {@link
     * org.jibx.runtime.IAbstractMarshaller#baseMarshal(Object,
     * org.jibx.runtime.IMarshallingContext)} method responsible for passing
     * handling on to the appropriate extension class.
     *
     * @param cf handler class
     */
    private void generateAbstractMarshaller(ClassFile cf) {
        
        // build the implementation method
        ContextMethodBuilder mb = new ContextMethodBuilder(BASEMARSHAL_METHODNAME,
            Type.VOID, MARSHAL_METHOD_ARGS, cf,
            Constants.ACC_PUBLIC|Constants.ACC_FINAL,
            1, "java.lang.Object", 2, MARSHALCONTEXT_INTERFACE);
        mb.addException(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
        // TODO: optionally check for null value on object
        mb.loadContext();
        mb.loadObject();
        mb.appendCallVirtual("java.lang.Object.getClass",
            "()Ljava/lang/Class;");
        mb.appendCallVirtual("java.lang.Class.getName", "()Ljava/lang/String;");
        mb.appendCallInterface(GETMARSHALLER_METHOD, GETMARSHALLER_SIGNATURE);
        mb.appendDUP();
        mb.appendLoadConstant(getMappingName());
        mb.appendCallInterface(CHECKEXTENDS_FULLNAME, CHECKEXTENDS_SIGNATURE);
        BranchWrapper ifvalid = mb.appendIFNE(this);
         
        // generate and throw exception describing the problem
        mb.appendCreateNew("java.lang.StringBuffer");
        mb.appendDUP();
        mb.appendLoadConstant("Mapping for type ");
        mb.appendCallInit("java.lang.StringBuffer", "(Ljava/lang/String;)V");
        mb.appendDUP();
        mb.loadObject();
        mb.appendCallVirtual("java.lang.Object.getClass",
            "()Ljava/lang/Class;");
        mb.appendCallVirtual("java.lang.Class.getName", "()Ljava/lang/String;");
        mb.appendCallVirtual("java.lang.StringBuffer.append", 
            "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        mb.appendDUP();
        mb.appendLoadConstant(" must extend abstract mapping for " +
            "type " + m_class.getClassName());
        mb.appendCallVirtual("java.lang.StringBuffer.append", 
            "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
        mb.appendCallVirtual("java.lang.StringBuffer.toString",
            "()Ljava/lang/String;");
        mb.appendCreateNew(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
        mb.appendDUP_X1();
        mb.appendSWAP();
        mb.appendCallInit(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS,
            MethodBuilder.EXCEPTION_CONSTRUCTOR_SIGNATURE1);
        mb.appendThrow();
         
        // for valid extension mapping, just call the marshaller
        mb.targetNext(ifvalid);
        mb.loadObject();
        mb.loadContext();
        mb.appendCallInterface(MARSHALLERMARSHAL_METHOD,
            MARSHALLERMARSHAL_SIGNATURE);
        mb.appendReturn();
        mb.codeComplete(false);
        mb.addMethod();
        
        // add extended interface to constructed class
        cf.addInterface(ABSTRACTMARSHALLER_INTERFACE);
    }

    /**
     * Generate the {@link org.jibx.runtime.IMarshaller#isExtension(String)}
     * method to check if this mapping is extending a particular abstract
     * mapping.
     *
     * @param cf
     * @param hasname
     */
    private void generateIfExtendingCheck(ClassFile cf, boolean hasname) {
        
        // build the method
        ExceptionMethodBuilder xb = new ExceptionMethodBuilder
            (CHECKEXTENDS_METHODNAME, CHECKEXTENDS_SIGNATURE, cf,
            Constants.ACC_PUBLIC|Constants.ACC_FINAL);
        xb.appendLoadLocal(1);
        xb.appendLoadConstant(getMappingName());
        xb.appendCallVirtual(EQUALS_METHODNAME, EQUALS_SIGNATURE);
        ArrayList ifmatches = new ArrayList();
        ifmatches.add(xb.appendIFNE(this));
        IMapping base = m_baseMapping;
        while (base != null) {
            xb.appendLoadLocal(1);
            xb.appendLoadConstant(base.getMappingName());
            xb.appendCallVirtual(EQUALS_METHODNAME, EQUALS_SIGNATURE);
            ifmatches.add(xb.appendIFNE(this));
            if (base instanceof MappingDefinition) {
                base = ((MappingDefinition)base).m_baseMapping;
            } else {
                break;
            }
        }
        xb.appendICONST_0();
        xb.appendReturn("int");
        for (int i = 0; i < ifmatches.size(); i++) {
            xb.targetNext((BranchWrapper)ifmatches.get(i));
        }
        xb.appendICONST_1();
        xb.appendReturn("int");
        xb.codeComplete(false);
        xb.addMethod();
    }

    /**
     * Generate the {@link org.jibx.runtime.IUnmarshaller#unmarshal(Object,
     * org.jibx.runtime.IUnmarshallingContext)} method implementation.
     *
     * @param cf class to receive method
     * @param hasattr attribute definition present flag
     * @param hascont content definition present flag
     * @param hasname element name defined by this mapping flag
     * @throws JiBXException
     */
    private void generateUnmarshalImplementation(ClassFile cf, boolean hasattr,
        boolean hascont, boolean hasname) throws JiBXException {
        
        // build the unmarshal method for item; this just generates code
        //  to unmarshal attributes and content, first creating an
        //  instance of the class if one was not passed in, then
        //  returning the unmarshalled instance as the value of the call
        String type = m_class.getClassName();
        ContextMethodBuilder mb = new ContextMethodBuilder(UNMARSHAL_METHODNAME,
            Type.OBJECT, UNMARSHAL_METHOD_ARGS, cf,
            Constants.ACC_PUBLIC|Constants.ACC_FINAL, 1, type,
            2, UNMARSHALCONTEXT_INTERFACE);
        mb.addException(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
        
        // first part of generated code just checks if an object has
        //  been supplied; if it has, this can just go direct to
        //  unmarshalling
        mb.loadObject();
        BranchWrapper ifnnull = mb.appendIFNONNULL(this);
        
        // check for extension mapping handling required
        if (m_extensions != null) {
            
            // generate name comparison unless an abstract mapping
            BranchWrapper ifthis = null;
            if (hasname) {
                
                // test if at defined element name
                mb.addException(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
                mb.loadContext();
                m_name.genPushUriPair(mb);
                mb.appendCallInterface(UNMARSHAL_ISATMETHOD,
                    UNMARSHAL_ISATSIGNATURE);
                ifthis = mb.appendIFNE(this);
            }
            
            // build code to check each extension mapping in turn,
            //  keeping an instance of the unmarshaller for the matching
            //  extension
            BranchWrapper[] iffounds =
                new BranchWrapper[m_extensions.size()];
            for (int i = 0; i < iffounds.length; i++) {
                IMapping map = (IMapping)m_extensions.get(i);
                mb.loadContext();
                mb.appendLoadConstant(map.getMappingName());
                mb.appendCallInterface(GETUNMARSHALLER_METHOD,
                    GETUNMARSHALLER_SIGNATURE);
                mb.appendDUP();
                mb.loadContext();
                mb.appendCallInterface(UNMARSHALLERPRESENT_METHOD,
                    UNMARSHALLERPRESENT_SIGNATURE);
                iffounds[i] = mb.appendIFNE(this);
                mb.appendPOP();
            }
            
            // generate code to throw exception if no matching extension
            //  found
            mb.appendCreateNew("java.lang.StringBuffer");
            mb.appendDUP();
            mb.appendLoadConstant("Element ");
            mb.appendCallInit("java.lang.StringBuffer",
                "(Ljava/lang/String;)V");
            mb.appendDUP();
            mb.loadContext(UNMARSHALCONTEXT_CLASS);
            mb.appendCallVirtual(CURRENTELEMENT_METHOD,
                CURRENTELEMENT_SIGNATURE);
            mb.appendCallVirtual("java.lang.StringBuffer.append", 
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
            mb.appendDUP();
            mb.appendLoadConstant(" has no mapping that extends " +
                m_class.getClassName());
            mb.appendCallVirtual("java.lang.StringBuffer.append", 
                "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
            mb.appendCallVirtual("java.lang.StringBuffer.toString",
                "()Ljava/lang/String;");
            mb.appendCreateNew(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
            mb.appendDUP_X1();
            mb.appendSWAP();
            mb.appendCallInit(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS,
                MethodBuilder.EXCEPTION_CONSTRUCTOR_SIGNATURE1);
            mb.appendThrow();
            if (iffounds.length > 0) {
            
                // finish by calling unmarshaller for extension mapping
                //  found and returning the result with no further
                //  processing
                mb.initStackState(iffounds[0]);
                BranchTarget found = mb.appendTargetACONST_NULL();
                for (int i = 0; i < iffounds.length; i++) {
                    iffounds[i].setTarget(found, mb);
                }
                mb.loadContext();
                mb.appendCallInterface(UNMARSHALLERUNMARSHAL_METHOD,
                    UNMARSHALLERUNMARSHAL_SIGNATURE);
                mb.appendReturn("java.lang.Object");
            }
            
            // fall into instance creation if this mapping reference
            if (ifthis != null) {
                mb.targetNext(ifthis);
            }
            
        } else if (m_isAbstract) {
            
            // throw an exception when no instance supplied
            mb.appendCreateNew(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
            mb.appendDUP();
            mb.appendLoadConstant("Abstract mapping requires instance to " +
                "be supplied for class " + m_class.getClassName());
            mb.appendCallInit(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS,
                MethodBuilder.EXCEPTION_CONSTRUCTOR_SIGNATURE1);
            mb.appendThrow();
            
        }
        if (hasname) {
            
            // just create an instance of the (non-abstract) mapped class
            mb.appendACONST_NULL();
            genNewInstance(mb);
            mb.storeObject();
            
        }
        
        // define unmarshallings for child mappings of this mapping
        mb.targetNext(ifnnull);
        ArrayList maps = m_defContext.getMappings();
        if (maps != null && maps.size() > 0) {
            for (int i = 0; i < maps.size(); i++) {
                IMapping map = (IMapping)maps.get(i);
                if (!map.isAbstract() || map.isBase()) {
                    mb.loadContext(UNMARSHALCONTEXT_CLASS);
                    mb.appendLoadConstant(map.getMappingName());
                    NameDefinition mname = map.getName();
                    if (mname == null) {
                        mb.appendACONST_NULL();
                        mb.appendACONST_NULL();
                    } else {
                        map.getName().genPushUriPair(mb);
                    }
                    mb.appendLoadConstant(map.getUnmarshaller().getName());
                    mb.appendCallVirtual(ADDUNMARSHALLER_METHOD,
                        ADDUNMARSHALLER_SIGNATURE);
                }
            }
        }
        
        // load object and cast to type
        mb.loadObject();
        mb.appendCreateCast(type);
        
        // handle the actual unmarshalling
        if (hasattr) {
            m_component.genAttributeUnmarshal(mb);
        }
        if (hasattr || !hasname) {
            mb.loadContext(UNMARSHALCONTEXT_CLASS);
            mb.appendCallVirtual(PARSERNEXT_METHOD,
                PARSERNEXT_SIGNATURE);
            mb.appendPOP();
        }
        if (hascont) {
            m_component.genContentUnmarshal(mb);
        }
        
        // undefine unmarshallings for child mappings of this mapping
        if (maps != null && maps.size() > 0) {
            for (int i = 0; i < maps.size(); i++) {
                IMapping map = (IMapping)maps.get(i);
                if (!map.isAbstract() || map.isBase()) {
                    mb.loadContext(UNMARSHALCONTEXT_CLASS);
                    mb.appendLoadConstant(map.getMappingName());
                    mb.appendCallVirtual(REMOVEUNMARSHALLER_METHOD,
                        REMOVEUNMARSHALLER_SIGNATURE);
                }
            }
        }
        
        // finish by returning unmarshalled object reference
        mb.appendReturn("java.lang.Object");
        mb.codeComplete(false);
        mb.addMethod();
        
        // add interface if mapped class is directly unmarshallable
        if (hasname && m_class.isLimitedDirectAccess()) {
            addIUnmarshallableMethod();
        }
    }

    /**
     * Generate the {@link
     * org.jibx.runtime.IUnmarshaller#isPresent(org.jibx.runtime.IUnmarshallingContext)}
     * method implementation.
     *
     * @param cf class to receive method
     * @param hasname element name defined by this mapping flag
     */
    private void generateIsPresent(ClassFile cf, boolean hasname) {
        
        // create the method builder
        ContextMethodBuilder mb = new ContextMethodBuilder
            (ISPRESENT_METHODNAME, Type.BOOLEAN, ISPRESENT_METHOD_ARGS,
            cf, Constants.ACC_PUBLIC|Constants.ACC_FINAL, -1, null,
            1, UNMARSHALCONTEXT_INTERFACE);
        
        // generate name comparison unless an abstract mapping
        if (hasname) {
            
            // test if at defined element name
            mb.addException(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
            mb.loadContext();
            m_name.genPushUriPair(mb);
            mb.appendCallInterface(UNMARSHAL_ISATMETHOD,
                UNMARSHAL_ISATSIGNATURE);
        }
        
        // check for extension mapping handling required
        if (m_extensions != null) {
            
            // return immediately if this mapping name check successful
            BranchWrapper ifthis = null;
            if (hasname) {
                ifthis = mb.appendIFNE(this);
            }
            
            // build code to check each extension mapping in turn;
            //  return "true" if one matches, or "false" if none do
            mb.addException(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
            BranchWrapper[] iffounds =
                new BranchWrapper[m_extensions.size()];
            for (int i = 0; i < iffounds.length; i++) {
                IMapping map = (IMapping)m_extensions.get(i);
                mb.loadContext();
                mb.appendLoadConstant(map.getMappingName());
                mb.appendCallInterface(GETUNMARSHALLER_METHOD,
                    GETUNMARSHALLER_SIGNATURE);
                mb.loadContext();
                mb.appendCallInterface(UNMARSHALLERPRESENT_METHOD,
                    UNMARSHALLERPRESENT_SIGNATURE);
                iffounds[i] = mb.appendIFNE(this);
            }
            mb.appendICONST_0();
            mb.appendReturn("int");
            mb.initStackState(iffounds[0]);
            BranchTarget found = mb.appendTargetLoadConstant(1);
            if (ifthis != null) {
                ifthis.setTarget(found, mb);
            }
            for (int i = 0; i < iffounds.length; i++) {
                iffounds[i].setTarget(found, mb);
            }
            
        } else if (!hasname) {
            
            // mapping with no separate element name, just return "true"
            mb.appendICONST_1();
            
        }
        mb.appendReturn("int");
        mb.codeComplete(false);
        mb.addMethod();
    }

    /**
     * Generate the {@link org.jibx.runtime.IMarshaller#marshal(Object,
     * org.jibx.runtime.IMarshallingContext)} method implementation.
     *
     * @param cf class to receive method
     * @param hasattr attribute definition present flag
     * @param hascont content definition present flag
     * @throws JiBXException
     */
    private void generateMarshalImplementation(ClassFile cf, boolean hasattr,
        boolean hascont) throws JiBXException {
        
        // build the marshal implementation method; this loads the
        //  passed object and casts it to the target type, then handles
        //  marshalling first attributes and followed by content for the
        //  item
        ContextMethodBuilder mb = new ContextMethodBuilder
            (MARSHAL_METHODNAME, Type.VOID, MARSHAL_METHOD_ARGS, cf,
            Constants.ACC_PUBLIC|Constants.ACC_FINAL,
            1, "java.lang.Object", 2, MARSHALCONTEXT_INTERFACE);
        mb.addException(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
        
        // define marshallings for child mappings of this mapping
        ArrayList maps = m_defContext.getMappings();
        if (maps != null && maps.size() > 0) {
            for (int i = 0; i < maps.size(); i++) {
                IMapping map = (IMapping)maps.get(i);
                if (!map.isAbstract() || map.isBase()) {
                    mb.loadContext(MARSHALCONTEXT_CLASS);
                    mb.appendLoadConstant(map.getMappingName());
                    mb.appendLoadConstant(map.getMarshaller().getName());
                    mb.appendCallVirtual(ADDMARSHALLER_METHOD,
                        ADDMARSHALLER_SIGNATURE);
                }
            }
        }
        if (hasattr || hascont) {
            
            // start by pushing namespace translation for precompiled binding
            mb.loadContext();
            mb.appendLoadConstant(m_container.getBindingRoot().getFactoryName());
            mb.appendCallInterface(PUSH_NAMESPACES_METHOD,
                PUSH_NAMESPACES_SIG);
            
            // handle the actual marshalling
            mb.loadObject(m_class.getClassName());
            if (hasattr) {
                if (hascont) {
                    mb.appendDUP();
                }
                m_component.genAttributeMarshal(mb);
            }
            if (hasattr || m_isAbstract) {
                mb.loadContext(MARSHALCONTEXT_CLASS);
                mb.appendCallVirtual(CLOSESTART_METHOD,
                    CLOSESTART_SIGNATURE);
                mb.appendPOP();
            }
            if (hascont) {
                m_component.genContentMarshal(mb);
            }
            
            // pop namespace translation for precompiled binding
            mb.loadContext();
            mb.appendCallInterface(POP_NAMESPACES_METHOD,
                POP_NAMESPACES_SIG);
        }
        
        // undefine marshallings for child mappings of this mapping
        if (maps != null && maps.size() > 0) {
            for (int i = 0; i < maps.size(); i++) {
                IMapping map = (IMapping)maps.get(i);
                if (!map.isAbstract() || map.isBase()) {
                    mb.loadContext(MARSHALCONTEXT_CLASS);
                    mb.appendLoadConstant(map.getMappingName());
                    mb.appendCallVirtual(REMOVEMARSHALLER_METHOD,
                        REMOVEMARSHALLER_SIGNATURE);
                }
            }
        }
        
        // finish with plain return
        mb.appendReturn();
        mb.codeComplete(false);
        mb.addMethod();
    }

    public void generateCode(boolean force) throws JiBXException {
        
        // first call code generation for child mappings
        m_defContext.generateCode(false, false);
        if (!force && m_isAbstract && m_extensions == null) {
            return;
        }
    
        // create the helper class
        BindingDefinition def = m_container.getBindingRoot();
        String init = m_class.deriveClassName(def.getPrefix(),
            ADAPTERCLASS_SUFFIX);
        String name = init;
        int loop = 0;
        while (ClassCache.hasClassFile(name)) {
            name = init + ++loop;
        }
        ClassFile base = ClassCache.requireClassFile("java.lang.Object");
        String[] intfs = def.isInput() ?
            (def.isOutput() ? BOTH_INTERFACES : UNMARSHALLER_INTERFACES) :
            MARSHALLER_INTERFACES;
        ClassFile cf = new ClassFile(name, m_class.getMungedFile().getRoot(),
            base, Constants.ACC_PUBLIC, intfs);
        cf.addDefaultConstructor();
        
        // add marshaller/unmarshaller implementation methods
        boolean hasattr = m_component.hasAttribute();
        boolean hascont = m_component.hasContent();
        boolean hasname = !m_isAbstract && m_name != null;
        if (def.isInput()) {
            generateIsPresent(cf, hasname);
            generateUnmarshalImplementation(cf, hasattr, hascont, hasname);
        }
        if (def.isOutput()) {
            
            // generate the basic method
            generateMarshalImplementation(cf, hasattr, hascont);
            generateIfExtendingCheck(cf, hasname);
            
            // add interface if mapped class is directly marshallable
            if (hasname && m_class.isLimitedDirectAccess()) {
                addIMarshallableMethod();
            }
            
            // check for mapping with extensions to add extra method and
            //  interface
            if (m_extensions != null) {
                generateAbstractMarshaller(cf);
            }
        }
    
        // add as generated class
        m_marshaller = m_unmarshaller = MungedClass.getUniqueSupportClass(cf);
    }

    public NameDefinition getWrapperName() {
        return m_name;
    }

    public void setLinkages() throws JiBXException {
        if (!isLinked()) {
            super.setLinkages();
            m_defContext.setLinkages();
            if (m_name != null) {
                m_name.fixNamespace(m_defContext);
            }
        }
    }
    
    public ITypeBinding getBinding() {
        return m_binding;
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("mapping class " + m_class.getClassFile().getName());
        if (m_name != null) {
            System.out.print(" to element " + m_name.toString());
        }
        if (m_baseMapping != null) {
            System.out.print(" extends " + m_baseMapping.getBoundType());
        }
        if (m_isAbstract) {
            if (m_extensions != null) {
                System.out.print(" (abstract, " + m_extensions.size() +
                    " extensions)");
            } else {
                System.out.print(" (abstract)");
            }
        }
        System.out.println();
        m_defContext.print(depth+1);
        m_component.print(depth+1);
    }
}
/*
Copyright (c) 2003-2012, Dennis M. Sosnoski.
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
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.BranchWrapper;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.ClassItem;
import org.jibx.binding.classes.ContextMethodBuilder;
import org.jibx.binding.classes.ExceptionMethodBuilder;
import org.jibx.binding.classes.MarshalBuilder;
import org.jibx.binding.classes.MethodBuilder;
import org.jibx.binding.classes.UnmarshalBuilder;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.Utility;

/**
 * Binding modifiers that apply to a class reference. This adds the methods used
 * for handling binding operations to the object class (or a helper class), then
 * generates calls to the added methods as this binding definition is used.
 *
 * @author Dennis M. Sosnoski
 */
public class ObjectBinding extends PassThroughComponent
implements IComponent, IContextObj
{
    //
    // Constants and such related to code generation.
    
    // recognized marshal hook method (pre-get) signatures.
    private static final String[] MARSHAL_HOOK_SIGNATURES =
    {
        "(Lorg/jibx/runtime/IMarshallingContext;)V",
        "(Ljava/lang/Object;)V",
        "()V"
    };
    
    // recognized factory hook method signatures.
    private static final String[] FACTORY_HOOK_SIGNATURES =
    {
        "(Lorg/jibx/runtime/IUnmarshallingContext;)",
        "(Ljava/lang/Object;)",
        "()"
    };
    
    // recognized unmarshal hook method (pre-set, post-set) signatures.
    private static final String[] UNMARSHAL_HOOK_SIGNATURES =
    {
        "(Lorg/jibx/runtime/IUnmarshallingContext;)V",
        "(Ljava/lang/Object;)V",
        "()V"
    };
    
    // definitions used in generating calls to user defined methods
    private static final String UNMARSHAL_GETSTACKTOPMETHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.getStackTop";
    private static final String MARSHAL_GETSTACKTOPMETHOD =
        "org.jibx.runtime.impl.MarshallingContext.getStackTop";
    private static final String GETSTACKTOP_SIGNATURE =
        "()Ljava/lang/Object;";
    private static final String MARSHALLING_CONTEXT =
        "org.jibx.runtime.impl.MarshallingContext";
    private static final String UNMARSHALLING_CONTEXT =
        "org.jibx.runtime.impl.UnmarshallingContext";
    private static final String UNMARSHAL_PARAMETER_SIGNATURE =
        "(Lorg/jibx/runtime/impl/UnmarshallingContext;)";
    private static final String UNMARSHAL_PUSHOBJECTMETHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.pushObject";
    private static final String UNMARSHAL_PUSHTRACKEDOBJECTMETHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.pushTrackedObject";
    private static final String MARSHAL_PUSHOBJECTMETHOD =
        "org.jibx.runtime.impl.MarshallingContext.pushObject";
    private static final String PUSHOBJECT_SIGNATURE =
        "(Ljava/lang/Object;)V";
    private static final String UNMARSHAL_POPOBJECTMETHOD =
        "org.jibx.runtime.impl.UnmarshallingContext.popObject";
    private static final String MARSHAL_POPOBJECTMETHOD =
        "org.jibx.runtime.impl.MarshallingContext.popObject";
    private static final String POPOBJECT_SIGNATURE = "()V";
    private static final String UNMARSHAL_PARAMETER_NORET_SIGNATURE =
        "(Lorg/jibx/runtime/impl/UnmarshallingContext;)V";
    private static final String MARSHAL_PARAMETER_NORET_SIGNATURE =
        "(Lorg/jibx/runtime/impl/MarshallingContext;)V";
    
    // definitions for methods added to mapped class
    private static final String NEWINSTANCE_SUFFIX = "_newinstance";
    private static final String PREPARE_SUFFIX = "_prepare";
    private static final String COMPLETE_SUFFIX = "_complete";
    private static final String ATTR_PRESENCE_SUFFIX = "_attrTest";
    private static final String UNMARSHAL_ATTR_SUFFIX = "_unmarshalAttr";
    private static final String MARSHAL_ATTR_SUFFIX = "_marshalAttr";
    private static final String PRESENCE_SUFFIX = "_test";
    private static final String UNMARSHAL_SUFFIX = "_unmarshal";
    private static final String MARSHAL_SUFFIX = "_marshal";
    private static final Type UNMARSHALCONTEXT_TYPE =
        new ObjectType(UNMARSHALLING_CONTEXT);
    private static final Type[] UNMARSHALCONTEXT_ARGS =
    {
        UNMARSHALCONTEXT_TYPE
    };
    private static final Type MARSHALCONTEXT_TYPE =
        new ObjectType(MARSHALLING_CONTEXT);
    
    // definitions for source position tracking
    private static final String SOURCE_TRACKING_INTERFACE =
        "org.jibx.runtime.impl.ITrackSourceImpl";
    private static final String SETSOURCE_METHODNAME = "jibx_setSource";
    private static final Type[] SETSOURCE_ARGS =
    {
        Type.STRING, Type.INT, Type.INT
    };
    private static final String SOURCEDOCUMENT_FIELDNAME ="jibx_sourceDocument";
    private static final String SOURCELINE_FIELDNAME = "jibx_sourceLine";
    private static final String SOURCECOLUMN_FIELDNAME = "jibx_sourceColumn";
    private static final String SOURCENAME_METHODNAME = "jibx_getDocumentName";
    private static final String SOURCELINE_METHODNAME = "jibx_getLineNumber";
    private static final String SOURCECOLUMN_METHODNAME = 
        "jibx_getColumnNumber";
    private static final Type[] EMPTY_ARGS = {};

    //
    // Actual instance data.

    /** Containing binding definition structure. */
    private final IContainer m_container;
    
    /** Class linked to mapping. */
    private BoundClass m_class;

    /** Object factory method. */
    private final ClassItem m_factoryMethod;

    /** Preset method for object. */
    private final ClassItem m_preSetMethod;

    /** Postset method for object. */
    private final ClassItem m_postSetMethod;

    /** Preget method for object. */
    private final ClassItem m_preGetMethod;
    
    /** Type to be used for creating new instances. */
    private final ClassFile m_createClass;
    
    /** Create method fully-qualified name (<code>null</code> if not yet
     generated). */
    private String m_createName;
    
    /** Checked for complete method needed flag. */
    private boolean m_checkedComplete;
    
    /** Checked for prepare method needed flag. */
    private boolean m_checkedPrepare;
    
    /** Complete method name (<code>null</code> if none, or not yet checked). */
    private String m_completeName;
    
    /** Prepare method name (<code>null</code> if none, or not yet checked). */
    private String m_prepareName;
    
    /** Generated new instance method. */
    private ClassItem m_newInstanceMethod;
    
    /** Flag for recursion while generating attribute presence test. */
    private boolean m_lockAttributePresence;
    
    /** Flag for recursion while generating content presence test. */
    private boolean m_lockContentPresence;
    
    /** Flag for recursion while generating attribute unmarshal. */
    private boolean m_lockAttributeUnmarshal;
    
    /** Flag for recursion while generating attribute marshal. */
    private boolean m_lockAttributeMarshal;
    
    /** Flag for recursion while generating attribute unmarshal. */
    private boolean m_lockContentUnmarshal;
    
    /** Flag for recursion while generating attribute marshal. */
    private boolean m_lockContentMarshal;
    
    /** Name for attribute presence test method (<code>null</code> unless
     generation started). */
    private String m_attributePresenceName;
    
    /** Name for content presence test method (<code>null</code> unless
     generation started). */
    private String m_contentPresenceName;
    
    /** Signature used for unmarshal methods. */
    private String m_unmarshalSignature;
    
    /** Name for unmarshal attribute method (<code>null</code> unless
     generation started). */
    private String m_unmarshalAttributeName;
    
    /** Name for unmarshal content method (<code>null</code> unless
     generation started). */
    private String m_unmarshalContentName;
    
    /** Signature used for marshal methods. */
    private String m_marshalSignature;
    
    /** Name for  marshal attribute method (<code>null</code> unless
     generation started). */
    private String m_marshalAttributeName;
    
    /** Name for  marshal content method (<code>null</code> unless
     generation started). */
    private String m_marshalContentName;
    
    /** Generated attribute presence test method. */
    private ClassItem m_attributePresenceMethod;
    
    /** Generated content presence test method. */
    private ClassItem m_contentPresenceMethod;
    
    /** Generated unmarshal attribute method. */
    private ClassItem m_unmarshalAttributeMethod;
    
    /** Generated unmarshal content method. */
    private ClassItem m_unmarshalContentMethod;
    
    /** Generated marshal attribute method. */
    private ClassItem m_marshalAttributeMethod;
    
    /** Generated marshal content method. */
    private ClassItem m_marshalContentMethod;
    
    /** Child supplying instance identifier value. */
    private IComponent m_idChild;
    
    /** Fake content present flag, used when neither content nor attributes
     are present. */
    private boolean m_fakeContent;
    
    /** Flag for dummy object binding, used when a concrete mapping just adds a
     name around an abstract mapping. */
    private boolean m_mappingWrapper;

    /**
     * Constructor. This initializes the definition context to be the same as
     * the parent's. Subclasses may change this definition context if
     * appropriate.
     *
     * @param contain containing binding definition component
     * @param objc current object context
     * @param type fully qualified class name for bound object
     * @param fact user new instance factory method
     * @param pres user preset method for unmarshalling
     * @param posts user postset method for unmarshalling
     * @param pget user preget method for marshalling
     * @param ctype type to use for creating new instance (<code>null</code> if
     * not specified)
     * @throws JiBXException if method not found
     */
    public ObjectBinding(IContainer contain, IContextObj objc, String type,
        String fact, String pres, String posts, String pget, String ctype)
        throws JiBXException {
        
        // initialize the basics
        m_container = contain;
        BoundClass ctxc = (objc == null) ? null : objc.getBoundClass();
        m_class = BoundClass.getInstance(type, ctxc);
        ClassFile cf = m_class.getClassFile();
        if (ctype == null) {
            m_createClass = cf;
        } else {
            m_createClass = ClassCache.requireClassFile(ctype);
        }
        
        // check instance creation for unmarshalling
        if (fact == null) {
            m_factoryMethod = null;
        } else {
            
            // look up supplied static factory method
            int split = fact.lastIndexOf('.');
            if (split >= 0) {
                
                // verify the method is defined
                String cname = fact.substring(0, split);
                String mname = fact.substring(split+1);
                ClassFile mcf = ClassCache.requireClassFile(cname);
                m_factoryMethod = mcf.getMethod(mname,
                    FACTORY_HOOK_SIGNATURES);
                if (m_factoryMethod == null) {
                    throw new JiBXException("Factory method " + fact +
                        " not found");
                } else {
                    
                    // force access if necessary
                    m_factoryMethod.makeAccessible(cf);
                    
                }
            } else {
                m_factoryMethod = null;
            }
            if (m_factoryMethod == null) {
                throw new JiBXException("Factory method " + fact +
                    " not found.");
            }
        }
        
        // look up other method names as members of class
        if (pres == null) {
            m_preSetMethod = null;
        } else {
            m_preSetMethod = cf.getMethod(pres, UNMARSHAL_HOOK_SIGNATURES);
            if (m_preSetMethod == null) {
                throw new JiBXException("User method " + pres + " not found.");
            }
        }
        if (posts == null) {
            m_postSetMethod = null;
        } else {
            m_postSetMethod = cf.getMethod(posts, UNMARSHAL_HOOK_SIGNATURES);
            if (m_postSetMethod == null) {
                throw new JiBXException("User method " + posts + " not found.");
            }
        }
        if (pget == null) {
            m_preGetMethod = null;
        } else {
            m_preGetMethod = cf.getMethod(pget, MARSHAL_HOOK_SIGNATURES);
            if (m_preGetMethod == null) {
                throw new JiBXException("User method " + pget + " not found.");
            }
        }
    }

    /**
     * Generate code for calling a user supplied method. The object methods
     * support three signature variations, with no parameters, with the
     * marshalling or unmarshalling context, or with the owning object.
     *
     * @param in flag for unmarshalling method
     * @param method information for method being called
     * @param mb method builder for generated code
     */
    private void genUserMethodCall(boolean in, ClassItem method,
        ContextMethodBuilder mb) {
        
        // load object reference for virtual call
        if (!method.isStatic()) {
            mb.loadObject();
        }
        
        // check if parameter required for call
        if (method.getArgumentCount() > 0) {
            
            // generate code to load context, then get containing object if
            //  needed for call
            mb.loadContext();
            String type = method.getArgumentType(0);
            if ("java.lang.Object".equals(type)) {
                String name = in ? UNMARSHAL_GETSTACKTOPMETHOD :
                    MARSHAL_GETSTACKTOPMETHOD;
                mb.appendCallVirtual(name, GETSTACKTOP_SIGNATURE);
            }
        }
        
        // generate appropriate form of call to user method
        mb.appendCall(method);
        mb.addMethodExceptions(method);
    }

    /**
     * Generate create method for object. The generated method matches the
     * {@link ITypeBinding#getCreateMethod()} result signature.
     *
     * @throws JiBXException if error in generating code
     */
    private void genCreateMethod() throws JiBXException {
        if (m_newInstanceMethod == null && (!m_mappingWrapper ||
            m_factoryMethod != null || m_preSetMethod != null)) {
            
            // set up for constructing new method
            String name = m_container.getBindingRoot().getPrefix() +
                NEWINSTANCE_SUFFIX;
            ClassFile cf = m_class.getMungedFile();
            Type type = m_class.getClassFile().getType();
            ContextMethodBuilder mb = new ContextMethodBuilder(name, type,
                new Type[] {type, UNMARSHALCONTEXT_TYPE}, cf,
                Constants.ACC_PUBLIC|Constants.ACC_STATIC, 0,
                m_class.getClassName(), 1, UNMARSHALLING_CONTEXT);
            
            // check if actually need to create a new instance
            mb.loadObject();
            BranchWrapper haveinst = mb.appendIFNONNULL(this);
            
            // check for factory supplied to create instance
            boolean haveobj = true;
            if (m_factoryMethod == null) {
                if (m_createClass.isArray()) {
                    
                    // construct array instance directly with basic size
                    mb.appendLoadConstant(Utility.MINIMUM_GROWN_ARRAY_SIZE);
                    String tname = m_createClass.getName();
                    mb.appendCreateArray(tname.substring(0, tname.length()-2));
                    
                } else if (m_createClass.isInterface() ||
                    m_createClass.isAbstract()) {
                    
                    // cannot create instance, throw exception
                    mb.appendCreateNew(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
                    mb.appendDUP();
                    mb.appendLoadConstant("Cannot create instance of interface or abstract class " +
                        m_createClass.getName());
                    mb.appendCallInit(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS,
                        MethodBuilder.EXCEPTION_CONSTRUCTOR_SIGNATURE1);
                    mb.appendThrow();
                    haveobj = false;
                    
                } else {
                    
                    // check if we have a no argument constructor
                    ClassItem cons = m_createClass.getInitializerMethod("()V");
                    if (cons == null) {
                        if (m_container.getBindingRoot().isAddConstructors()) {
                            m_createClass.addDefaultConstructor();
                        } else {
                            
                            // cannot create instance, throw exception
                            mb.appendCreateNew(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS);
                            mb.appendDUP();
                            mb.appendLoadConstant("Cannot create instance of class " +
                                m_createClass.getName() + " (no default constructor)");
                            mb.appendCallInit(MethodBuilder.FRAMEWORK_EXCEPTION_CLASS,
                                MethodBuilder.EXCEPTION_CONSTRUCTOR_SIGNATURE1);
                            mb.appendThrow();
                            haveobj = false;
                            
                        }
                    } else {
                        cons.makeAccessible(m_class.getMungedFile());
                    }
                    if (haveobj) {
                        
                        // no factory, so create an instance, duplicate the
                        //  reference, and then call the null constructor
                        mb.appendCreateNew(m_createClass.getName());
                        mb.appendDUP();
                        mb.appendCallInit(m_createClass.getName(),"()V");
                        
                    }
                }
                
            } else {
                
                // generate call to factory method
                genUserMethodCall(true, m_factoryMethod, mb);
                mb.appendCreateCast(m_factoryMethod.getTypeName(),
                    m_class.getClassName());
                
            }
            
            // save created instance to be returned
            if (haveobj) {
                mb.storeObject();
            }
            
            // if preset method supplied add code to call it
            mb.targetNext(haveinst);
//            ** disable for now; can't take out of unmarshal methods, since
//            create not always called **
//            if (m_preSetMethod != null) {
//                genUserMethodCall(true, m_preSetMethod, mb);
//            }
            
            // finish method code with return of new instance
            mb.loadObject();
            mb.appendReturn(m_class.getClassName());
            m_newInstanceMethod = m_class.getUniqueMethod(mb).getItem();
        }
    }

    /**
     * Generate prepare for marshalling method for object, if needed. The
     * generated method matches the {@link ITypeBinding#getPrepareMethod()}
     * result signature.
     *
     * @throws JiBXException if error in generating code
     */
    private void genPrepareMethod() throws JiBXException {
        if (!m_checkedPrepare) {
//          ** disable for now; can't take out of marshal methods, since
//          prepare not always called **
//            if (m_preGetMethod != null &&
//                m_container.getBindingRoot().isOutput()) {
//                
//                // set up for constructing new method
//                String name = m_container.getBindingRoot().getPrefix() +
//                    PREPARE_SUFFIX;
//                Type[] args = new Type[2];
//                args[0] = m_class.getClassFile().getType();
//                args[1] = MARSHALCONTEXT_TYPE;
//                ContextMethodBuilder mb = new ContextMethodBuilder(name,
//                    Type.VOID, args, m_class.getMungedFile(),
//                    Constants.ACC_PUBLIC | Constants.ACC_STATIC, 0,
//                    m_class.getClassName(), 1, MARSHALLING_CONTEXT);
//                
//                // just generate embedded call to user method
//                genUserMethodCall(false, m_preGetMethod, mb);
//                mb.appendReturn();
//                
//                // add method to class
//                m_prepareName =
//                    m_class.getUniqueMethod(mb).getItem().getFullName();
//                
//            }
            
            // flag checked for next time called
            m_checkedPrepare = true;
        }
    }

    /**
     * Generate unmarshalling complete method for object, if needed. The
     * generated method matches the {@link ITypeBinding#getCompleteMethod()}
     * result signature.
     *
     * @throws JiBXException if error in generating code
     */
    private void genCompleteMethod() throws JiBXException {
        if (!m_checkedComplete) {
//          ** disable for now; can't take out of unmarshal methods, since
//          complete not always called **
//            if (m_postSetMethod != null &&
//                m_container.getBindingRoot().isInput()) {
//                
//                // set up for constructing new method
//                String name = m_container.getBindingRoot().getPrefix() +
//                    COMPLETE_SUFFIX;
//                Type[] args = new Type[2];
//                args[0] = m_class.getClassFile().getType();
//                args[1] = UNMARSHALCONTEXT_TYPE;
//                ContextMethodBuilder mb = new ContextMethodBuilder(name,
//                    Type.VOID, args, m_class.getMungedFile(),
//                    Constants.ACC_PUBLIC | Constants.ACC_STATIC, 0,
//                    m_class.getClassName(), 1, UNMARSHALLING_CONTEXT);
//                
//                // just generate embedded call to user method
//                genUserMethodCall(true, m_postSetMethod, mb);
//                mb.appendReturn();
//                
//                // add method to class
//                m_completeName =
//                    m_class.getUniqueMethod(mb).getItem().getFullName();
//                
//            }
            
            // flag checked for next time called
            m_checkedComplete = true;
        }
    }

    /**
     * Generate code to handle unmarshal source location tracking. This
     * convenience method generates the member variables and method used to
     * support setting the source location, the methods used to access the
     * information, and also adds the appropriate interfaces to the class.
     */
    private void genTrackSourceCode() {
    	if (m_class.isDirectAccess()) {
	        ClassFile cf = m_class.getDirectMungedFile();
	        if (!cf.isAbstract() &&
	            cf.addInterface(SOURCE_TRACKING_INTERFACE)) {
	        
	            // add position tracking fields to class
	            ClassItem srcname = cf.addPrivateField("java.lang.String;",
	                SOURCEDOCUMENT_FIELDNAME);
	            ClassItem srcline = cf.addPrivateField("int", SOURCELINE_FIELDNAME);
	            ClassItem srccol = cf.addPrivateField("int",
	                SOURCECOLUMN_FIELDNAME);
	        
	            // add method for setting the source information
	            MethodBuilder mb = new ExceptionMethodBuilder(SETSOURCE_METHODNAME,
	                Type.VOID, SETSOURCE_ARGS, cf, Constants.ACC_PUBLIC);
	            mb.appendLoadLocal(0);
	            mb.appendLoadLocal(1);
	            mb.appendPutField(srcname);
	            mb.appendLoadLocal(0);
	            mb.appendLoadLocal(2);
	            mb.appendPutField(srcline);
	            mb.appendLoadLocal(0);
	            mb.appendLoadLocal(3);
	            mb.appendPutField(srccol);
	            mb.appendReturn();
	            mb.codeComplete(false);
	            mb.addMethod();
	        
	            // add methods for getting the source information
	            mb = new ExceptionMethodBuilder(SOURCENAME_METHODNAME,
	                Type.STRING, EMPTY_ARGS, cf, Constants.ACC_PUBLIC);
	            mb.appendLoadLocal(0);
	            mb.appendGetField(srcname);
	            mb.appendReturn(Type.STRING);
	            mb.codeComplete(false);
	            mb.addMethod();
	            mb = new ExceptionMethodBuilder(SOURCELINE_METHODNAME,
	                Type.INT, EMPTY_ARGS, cf, Constants.ACC_PUBLIC);
	            mb.appendLoadLocal(0);
	            mb.appendGetField(srcline);
	            mb.appendReturn("int");
	            mb.codeComplete(false);
	            mb.addMethod();
	            mb = new ExceptionMethodBuilder(SOURCECOLUMN_METHODNAME,
	                Type.INT, EMPTY_ARGS, cf, Constants.ACC_PUBLIC);
	            mb.appendLoadLocal(0);
	            mb.appendGetField(srccol);
	            mb.appendReturn("int");
	            mb.codeComplete(false);
	            mb.addMethod();
	        }
    	}
    }
    
    /**
     * Generate attribute presence test method for object. The generated method
     * matches the {@link ITypeBinding#getAttributePresentTestMethod()} result
     * signature.
     *
     * @throws JiBXException if error in configuration
     */
    private void genAttributePresenceMethod() throws JiBXException {
        if (m_attributePresenceMethod == null) {
            
            // set up for constructing new method
            String name = m_container.getBindingRoot().getPrefix() +
                ATTR_PRESENCE_SUFFIX;
            ContextMethodBuilder meth = new ContextMethodBuilder(name,
                Type.BOOLEAN, UNMARSHALCONTEXT_ARGS, m_class.getMungedFile(),
                Constants.ACC_PUBLIC | Constants.ACC_STATIC, -1, null,
                0, UNMARSHALLING_CONTEXT);
            m_attributePresenceName = meth.getFullName();
            
            // generate the presence test code
            m_component.genAttrPresentTest(meth);
            meth.appendReturn(Type.BOOLEAN);
            
            // add method to class
            if (m_lockAttributePresence) {
                m_attributePresenceMethod =
                    m_class.getUniqueNamed(meth).getItem();
            } else {
                m_attributePresenceMethod =
                    m_class.getUniqueMethod(meth).getItem();
                m_attributePresenceName =
                    m_attributePresenceMethod.getFullName();
            }
            
        } else {
            m_lockAttributePresence = true;
        }
    }
    
    /**
     * Generate attribute unmarshal method for object. The generated method
     * matches the {@link ITypeBinding#getAttributeUnmarshalMethod()} result
     * signature.
     *
     * @throws JiBXException if error in configuration
     */
    private void genUnmarshalAttributeMethod() throws JiBXException {
        if (m_unmarshalAttributeName == null) {
            
            // set up for constructing new method
            String name = m_container.getBindingRoot().getPrefix() +
                UNMARSHAL_ATTR_SUFFIX;
            UnmarshalBuilder meth = new UnmarshalBuilder(name, 
                m_class.getClassFile(), m_class.getMungedFile());
            m_unmarshalAttributeName = meth.getFullName();
            m_unmarshalSignature = meth.getSignature();
            
            // if preset method supplied add code to call it
            if (m_preSetMethod != null) {
                meth.loadObject();
                genUserMethodCall(true, m_preSetMethod, meth);
            }
            
            // push object being unmarshalled to unmarshaller stack
            meth.loadContext();
            meth.loadObject();
            meth.appendCallVirtual(UNMARSHAL_PUSHTRACKEDOBJECTMETHOD,
                PUSHOBJECT_SIGNATURE);
            
            // generate the actual unmarshalling code in method
            meth.loadObject();
            m_component.genAttributeUnmarshal(meth);
            
            // pop object from unmarshal stack
            meth.loadContext();
            meth.appendCallVirtual(UNMARSHAL_POPOBJECTMETHOD,
                POPOBJECT_SIGNATURE);
            
            // if postset method supplied and no content add code to call it
            if (m_postSetMethod != null && !hasContent()) {
                genUserMethodCall(true, m_postSetMethod, meth);
            }
            
            // finish by returning object
            meth.loadObject();
            meth.appendReturn(m_class.getClassFile().getName());
            
            // add method to class
            if (m_lockAttributeUnmarshal) {
                m_unmarshalAttributeMethod =
                    m_class.getUniqueNamed(meth).getItem();
            } else {
                m_unmarshalAttributeMethod =
                    m_class.getUniqueMethod(meth).getItem();
                m_unmarshalAttributeName =
                    m_unmarshalAttributeMethod.getFullName();
            }
            
        } else {
            m_lockAttributeUnmarshal = true;
        }
    }

    /**
     * Generate attribute marshal method for object. The generated method
     * matches the {@link ITypeBinding#getAttributeMarshalMethod()} result
     * signature.
     *
     * @throws JiBXException if error in configuration
     */
    private void genMarshalAttributeMethod() throws JiBXException {
        if (m_marshalAttributeName == null) {
            
            // set up for constructing new method
            String name = m_container.getBindingRoot().getPrefix() +
                MARSHAL_ATTR_SUFFIX;
            MarshalBuilder meth = new MarshalBuilder(name, 
                m_class.getClassFile(), m_class.getMungedFile());
            m_marshalAttributeName = meth.getFullName();
            m_marshalSignature = meth.getSignature();
            
            // if preget method supplied add code to call it
            if (m_preGetMethod != null) {
                genUserMethodCall(false, m_preGetMethod, meth);
            }
            
            // push object being marshalled to marshaller stack
            meth.loadContext();
            meth.loadObject();
            meth.appendCallVirtual(MARSHAL_PUSHOBJECTMETHOD,
                PUSHOBJECT_SIGNATURE);
            
            // generate actual marshalling code
            meth.loadContext();
            m_component.genAttributeMarshal(meth);
            
            // pop object from stack
            meth.loadContext();
            meth.appendCallVirtual(MARSHAL_POPOBJECTMETHOD,
                POPOBJECT_SIGNATURE);
            
            // finish and add constructed method to class
            meth.appendReturn();
            if (m_lockAttributeMarshal) {
                m_marshalAttributeMethod =
                    m_class.getUniqueNamed(meth).getItem();
            } else {
                m_marshalAttributeMethod =
                    m_class.getUniqueMethod(meth).getItem();
                m_marshalAttributeName =
                    m_marshalAttributeMethod.getFullName();
            }
            
        } else {
            m_lockAttributeMarshal = true;
        }
    }
    
    /**
     * Generate content presence test method for object. The generated method
     * matches the {@link ITypeBinding#getContentPresentTestMethod()} result
     * signature.
     *
     * @throws JiBXException if error in configuration
     */
    private void genContentPresenceMethod() throws JiBXException {
        if (m_contentPresenceMethod == null) {
            
            // set up for constructing new method
            String name = m_container.getBindingRoot().getPrefix() +
                PRESENCE_SUFFIX;
            ContextMethodBuilder meth = new ContextMethodBuilder(name,
                Type.BOOLEAN, UNMARSHALCONTEXT_ARGS, m_class.getMungedFile(),
                Constants.ACC_PUBLIC | Constants.ACC_STATIC, -1, null,
                0, UNMARSHALLING_CONTEXT);
            m_contentPresenceName = meth.getFullName();
            
            // generate the presence test code
            if (m_fakeContent) {
                meth.appendLoadConstant(1);
            } else {
                m_component.genContentPresentTest(meth);
            }
            meth.appendReturn(Type.BOOLEAN);
            
            // add method to class
            if (m_lockContentPresence) {
                m_contentPresenceMethod =
                    m_class.getUniqueNamed(meth).getItem();
            } else {
                m_contentPresenceMethod =
                    m_class.getUniqueMethod(meth).getItem();
                m_contentPresenceName =
                    m_contentPresenceMethod.getFullName();
            }
            
        } else {
            m_lockContentPresence = true;
        }
    }

    /**
     * Generate content unmarshal method for object. The generated method
     * matches the {@link ITypeBinding#getContentUnmarshalMethod()} result
     * signature.
     *
     * @throws JiBXException
     */
    private void genUnmarshalContentMethod() throws JiBXException {
        if (m_unmarshalContentName == null) {
            
            // set up for constructing new method
            String name = m_container.getBindingRoot().getPrefix() +
                UNMARSHAL_SUFFIX;
            UnmarshalBuilder meth = new UnmarshalBuilder(name, 
                m_class.getClassFile(), m_class.getMungedFile());
            m_unmarshalContentName = meth.getFullName();
            m_unmarshalSignature = meth.getSignature();
            
            // if preset method supplied add code to call it
            if (!hasAttribute() && m_preSetMethod != null) {
                meth.loadObject();
                genUserMethodCall(true, m_preSetMethod, meth);
            }
            
            // push object being unmarshalled to unmarshaller stack
            meth.loadContext();
            meth.loadObject();
            String mname = hasAttribute() ? UNMARSHAL_PUSHOBJECTMETHOD :
                UNMARSHAL_PUSHTRACKEDOBJECTMETHOD;
            meth.appendCallVirtual(mname, PUSHOBJECT_SIGNATURE);
            
            // generate the actual unmarshalling code in method
            meth.loadObject();
            if (!m_fakeContent) {
                m_component.genContentUnmarshal(meth);
            }
            
            // pop object from unmarshal stack
            meth.loadContext();
            meth.appendCallVirtual(UNMARSHAL_POPOBJECTMETHOD,
                POPOBJECT_SIGNATURE);
            
            // if postset method supplied and no attributes add code to call
            if (m_postSetMethod != null) {
                genUserMethodCall(true, m_postSetMethod, meth);
            }
            
            // finish by returning object
            meth.loadObject();
            meth.appendReturn(m_class.getClassFile().getName());
            
            // add method to class
            if (m_lockContentUnmarshal) {
                m_unmarshalContentMethod =
                    m_class.getUniqueNamed(meth).getItem();
            } else {
                m_unmarshalContentMethod =
                    m_class.getUniqueMethod(meth).getItem();
                m_unmarshalContentName =
                    m_unmarshalContentMethod.getFullName();
            }
            
        } else {
            m_lockContentUnmarshal = true;
        }
    }

    /**
     * Generate content marshal method for object. The generated method
     * matches the {@link ITypeBinding#getContentMarshalMethod()} result
     * signature.
     *
     * @throws JiBXException
     */
    private void genMarshalContentMethod() throws JiBXException {
        if (m_marshalContentName == null) {
            
            // set up for constructing new method
            String name =
                m_container.getBindingRoot().getPrefix() + MARSHAL_SUFFIX;
            MarshalBuilder meth = new MarshalBuilder(name, 
                m_class.getClassFile(), m_class.getMungedFile());
            m_marshalContentName = meth.getFullName();
            m_marshalSignature = meth.getSignature();
            
            // if preget method supplied and no attributes add code to call it
            if (m_preGetMethod != null && !hasAttribute()) {
                genUserMethodCall(false, m_preGetMethod, meth);
            }
            
            // push object being marshalled to marshaller stack
            meth.loadContext();
            meth.loadObject();
            meth.appendCallVirtual(MARSHAL_PUSHOBJECTMETHOD,
                PUSHOBJECT_SIGNATURE);
            
            // generate actual marshalling code
            meth.loadContext();
            if (!m_fakeContent) {
                m_component.genContentMarshal(meth);
            }
            
            // pop object from stack
            meth.loadContext();
            meth.appendCallVirtual(MARSHAL_POPOBJECTMETHOD,
                POPOBJECT_SIGNATURE);
            
            // finish and add constructed method to class
            meth.appendReturn();
            if (m_lockContentMarshal) {
                m_marshalContentMethod =
                    m_class.getUniqueNamed(meth).getItem();
            } else {
                m_marshalContentMethod =
                    m_class.getUniqueMethod(meth).getItem();
                m_marshalContentName = m_marshalContentMethod.getFullName();
            }
            
        } else {
            m_lockContentMarshal = true;
        }
    }
    
    //
    // IContextObj interface method definitions
    
    public BoundClass getBoundClass() {
        return m_class;
    }

    public boolean setIdChild(IComponent child) {
        if (m_idChild == null) {
            m_idChild = child;
            return true;
        } else {
            return false;
        }
    }
    
    //
    // IComponent interface method definitions
    
    public boolean isOptional() {
        return false;
    }
    
    public boolean hasContent() {
        return m_fakeContent || super.hasContent();
    }

    public void genAttrPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_mappingWrapper) {
            super.genAttrPresentTest(mb);
        } else {
            
            // check if attribute presence method needs to be added to class
            if (m_attributePresenceMethod == null) {
                genAttributePresenceMethod();
            }
                            
            // generate code to call attribute presence test method
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCall(m_attributePresenceMethod);
            
        }
    }

    public void genContentPresentTest(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_mappingWrapper) {
            super.genContentPresentTest(mb);
        } else {
            
            // check if content presence method needs to be added to class
            if (m_contentPresenceMethod == null) {
                genContentPresenceMethod();
            }
                            
            // generate code to call attribute presence test method
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCall(m_contentPresenceMethod);
            
        }
    }

    public void genAttributeUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_mappingWrapper) {
            super.genAttributeUnmarshal(mb);
        } else {
            
            // check if unmarshal method needs to be added to class
            if (m_unmarshalAttributeMethod == null) {
                genUnmarshalAttributeMethod();
            }
            
            // generate code to call created unmarshal method
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCallStatic(m_unmarshalAttributeName, m_unmarshalSignature);
            
        }
    }

    public void genAttributeMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_mappingWrapper) {
            super.genAttributeMarshal(mb);
        } else {
                
            // check if marshal method needs to be added to class
            if (m_marshalAttributeMethod == null) {
                genMarshalAttributeMethod();
            }
            
            // generate code to call created marshal method
            mb.loadContext(MARSHALLING_CONTEXT);
            mb.appendCallStatic(m_marshalAttributeName, m_marshalSignature);
            
        }
    }

    public void genContentUnmarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_mappingWrapper) {
            super.genContentUnmarshal(mb);
        } else {
                    
            // check if unmarshal method needs to be added to class
            if (m_unmarshalContentMethod == null) {
                genUnmarshalContentMethod();
            }
            
            // generate code to call created unmarshal method
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCallStatic(m_unmarshalContentName, m_unmarshalSignature);
            
        }
    }

    public void genContentMarshal(ContextMethodBuilder mb)
        throws JiBXException {
        if (m_mappingWrapper) {
            super.genContentMarshal(mb);
        } else {
                        
            // check if marshal method needs to be added to class
            if (m_marshalContentMethod == null) {
                genMarshalContentMethod();
            }
            
            // generate code to call created marshal method
            mb.loadContext(MARSHALLING_CONTEXT);
            mb.appendCallStatic(m_marshalContentName, m_marshalSignature);
            
        }
    }
    
    public void genNewInstance(ContextMethodBuilder mb) throws JiBXException {
        genCreateMethod();
        if (m_newInstanceMethod == null) {
            super.genNewInstance(mb);
        } else {
                            
            // generate code to call new instance method
            mb.loadContext(UNMARSHALLING_CONTEXT);
            mb.appendCall(m_newInstanceMethod);
            
        }
    }

    public String getType() {
        return m_class.getClassName();
    }

    public boolean hasId() {
        return m_idChild != null;
    }

    public void genLoadId(ContextMethodBuilder mb) throws JiBXException {
        if (m_idChild == null) {
            throw new IllegalStateException("Internal error: no id defined");
        } else {
            m_idChild.genLoadId(mb);
        }
    }
    
    public void setLinkages() throws JiBXException {
        IComponent comp = m_component;
        MappingReference ref = null;
        if (comp instanceof NestedStructure) {
            NestedStructure struct = (NestedStructure)comp;
            if (struct.isMappingReference()) {
                ref = (MappingReference)struct.getContents().get(0);
            }
        }
        super.setLinkages();
        if (ref == null) {
            m_fakeContent = !super.hasAttribute() && !super.hasContent();
        } else {
            IMapping mapping = ref.getMapping();
            if (mapping != null && mapping.getBoundType().equals(getType())) {
                IComponent wrapped = ref.m_component;
                if (wrapped instanceof BaseMappingWrapper) {
                    BaseMappingWrapper wrapper = (BaseMappingWrapper)wrapped;
                    setWrappedComponent(wrapper.m_component);
                    m_mappingWrapper = true;
                }
            }
        }
        if (m_container.getBindingRoot().isTrackSource()) {
            genTrackSourceCode();
        }
    }
    
    //
    // ITypeBinding interface method definitions

    public String getAttributeMarshalMethod() throws JiBXException {
        if (m_container.getBindingRoot().isOutput() && hasAttribute() &&
            !m_mappingWrapper) {
            if (m_marshalAttributeMethod == null) {
                genMarshalAttributeMethod();
            }
            return m_marshalAttributeName;
        } else {
            return null;
        }
    }

    public String getAttributePresentTestMethod() throws JiBXException {
        if (m_container.getBindingRoot().isInput() && hasAttribute() &&
            !m_mappingWrapper) {
            if (m_attributePresenceMethod == null) {
                genAttributePresenceMethod();
            }
            return m_attributePresenceName;
        } else {
            return null;
        }
    }

    public String getAttributeUnmarshalMethod() throws JiBXException {
        if (m_container.getBindingRoot().isInput() && hasAttribute() &&
            !m_mappingWrapper) {
            if (m_unmarshalAttributeMethod == null) {
                genUnmarshalAttributeMethod();
            }
            return m_unmarshalAttributeName;
        } else {
            return null;
        }
    }

    public String getCompleteMethod() throws JiBXException {
        genCompleteMethod();
        return m_completeName;
    }

    public String getContentMarshalMethod() throws JiBXException {
        if (m_container.getBindingRoot().isOutput() && hasContent() &&
            !m_mappingWrapper) {
            if (m_marshalContentMethod == null) {
                genMarshalContentMethod();
            }
            return m_marshalContentName;
        } else {
            return null;
        }
    }

    public String getContentPresentTestMethod() throws JiBXException {
        if (m_container.getBindingRoot().isInput() && hasContent() &&
            !m_mappingWrapper) {
            if (m_contentPresenceMethod == null) {
                genContentPresenceMethod();
            }
            return m_contentPresenceName;
        } else {
            return null;
        }
    }

    public String getContentUnmarshalMethod() throws JiBXException {
        if (m_container.getBindingRoot().isInput() && hasContent() &&
            !m_mappingWrapper) {
            if (m_unmarshalContentMethod == null) {
                genUnmarshalContentMethod();
            }
            return m_unmarshalContentName;
        } else {
            return null;
        }
    }

    public String getCreateMethod() throws JiBXException {
        if (m_container.getBindingRoot().isInput() &&
            !m_mappingWrapper) {
            genCreateMethod();
            return m_newInstanceMethod.getFullName();
        } else {
            return null;
        }
    }

    public String getPrepareMethod() throws JiBXException {
        genPrepareMethod();
        return m_prepareName;
    }
    
    // DEBUG
    public void print(int depth) {
        BindingDefinition.indent(depth);
        System.out.print("object binding for " +
            m_class.getClassFile().getName());
        if (m_createClass != null) {
            System.out.print(" create class " + m_createClass.getName());
        }
        if (m_factoryMethod != null) {
            System.out.print(" factory=" + m_factoryMethod.getName());
        }
        if (m_preSetMethod != null) {
            System.out.print(" preset=" + m_preSetMethod.getName());
        }
        if (m_postSetMethod != null) {
            System.out.print(" postset=" + m_postSetMethod.getName());
        }
        if (m_preGetMethod != null) {
            System.out.print(" preget=" + m_preGetMethod.getName());
        }
        if (m_mappingWrapper) {
            System.out.print(" (mapping wrapper)");
        }
        System.out.println();
        m_component.print(depth+1);
    }
}
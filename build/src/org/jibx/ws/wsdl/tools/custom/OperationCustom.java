/*
 * Copyright (c) 2007-2008, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * JiBX nor the names of its contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.ws.wsdl.tools.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jibx.custom.CustomUtils;
import org.jibx.custom.classes.CustomBase;
import org.jibx.custom.classes.IDocumentFormatter;
import org.jibx.custom.classes.SharedNestingBase;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.IClassLocator;
import org.jibx.util.StringArray;
import org.jibx.ws.wsdl.tools.SignatureParser;

/**
 * Operation customization information. This supports direct operation customizations (such as the corresponding request
 * and/or response element name) and also acts as a container for parameter and/or return customizations.
 * 
 * @author Dennis M. Sosnoski
 */
public class OperationCustom extends NestingBase
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(OperationCustom.class.getName());
    
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "method-name", "operation-name", "optionals", "request-message",
            "request-wrapper", "requireds", "response-message", "response-wrapper", "soap-action" },
            SharedNestingBase.s_allowedAttributes);
    
    // values specific to class level
    private String m_methodName;
    
    private String m_operationName;
    
    private String m_requestMessageName;
    
    private String m_requestWrapperName;
    
    private String m_responseMessageName;
    
    private String m_responseWrapperName;
    
    private String m_soapAction;
    
    private List m_documentation;
    
    private String[] m_requireds;
    
    private String[] m_optionals;
    
    // list of contained parameter customizations
    private final ArrayList m_parameters;
    
    // contained result customization (null if none)
    private ValueCustom m_return;
    
    // list of contained throws customizations
    private final ArrayList m_throws;
    
    /**
     * Constructor.
     * 
     * @param parent
     * @param name method name
     */
    OperationCustom(NestingBase parent, String name) {
        super(parent);
        m_methodName = name;
        m_parameters = new ArrayList();
        m_throws = new ArrayList();
    }
    
    /**
     * Get the namespace for WSDL definitions of this service.
     * 
     * @return WSDL namespace
     */
    public String getWsdlNamespace() {
        return ((NestingBase)getParent()).getWsdlNamespace();
    }
    
    /**
     * Get method name.
     * 
     * @return name
     */
    public String getMethodName() {
        return m_methodName;
    }
    
    /**
     * Get the operation name.
     * 
     * @return operation name
     */
    public String getOperationName() {
        return m_operationName;
    }
    
    /**
     * Get request message name.
     * 
     * @return name
     */
    public String getRequestMessageName() {
        return m_requestMessageName;
    }
    
    /**
     * Get request wrapper element name.
     * 
     * @return name
     */
    public String getRequestWrapperName() {
        return m_requestWrapperName;
    }
    
    /**
     * Get response message name.
     * 
     * @return name
     */
    public String getResponseMessageName() {
        return m_responseMessageName;
    }
    
    /**
     * Get response wrapper name.
     * 
     * @return name
     */
    public String getResponseWrapperName() {
        return m_responseWrapperName;
    }
    
    /**
     * Get return value.
     * 
     * @return return
     */
    public ValueCustom getReturn() {
        return m_return;
    }
    
    /**
     * Get SOAPAction.
     * 
     * @return soapAction
     */
    public String getSoapAction() {
        return m_soapAction;
    }
    
    /**
     * Get operation documentation.
     * 
     * @return list of documentation nodes (<code>null</code> if none)
     */
    public List getDocumentation() {
        return m_documentation;
    }
    
    /**
     * Get list of children.
     * 
     * @return list
     */
    public ArrayList getParameters() {
        return m_parameters;
    }
    
    /**
     * Get list of throws customizations.
     * 
     * @return list
     */
    public ArrayList getThrows() {
        return m_throws;
    }
    
    /**
     * Add child.
     * 
     * @param child
     */
    protected void addChild(CustomBase child) {
        if (child.getParent() == this) {
            m_parameters.add(child);
        } else {
            throw new IllegalStateException("Internal error: child not linked");
        }
    }
    
    /**
     * Unmarshalling factory. This gets the containing element and the name so that the standard constructor can be
     * used.
     * 
     * @param ictx
     * @return created instance
     * @throws JiBXException
     */
    private static OperationCustom factory(IUnmarshallingContext ictx) throws JiBXException {
        String mname = ((UnmarshallingContext)ictx).attributeText(null, "method-name");
        s_logger.debug("Creating operation for operation name " + mname);
        return new OperationCustom((NestingBase)getContainingObject(ictx), mname);
    }
    
    /**
     * Check if type is a collection type (specifically collection, not array).
     * 
     * @param type
     * @return item type, <code>null</code> if not a collection type
     */
    private boolean isCollection(String type, IClassLocator icl) {
        IClass info = icl.getRequiredClassInfo(type);
        return info.isImplements("Ljava/util/Collection;");
    }
    
    /**
     * Parse parameter type.
     * 
     * @param parse
     * @return parameter type
     */
    private String parameterType(SignatureParser parse) {
        String itype = null;
        while (parse.next() != SignatureParser.TYPE_PARAMETERS_END_EVENT) {
            if (itype == null && parse.getEvent() == SignatureParser.TYPE_EVENT) {
                itype = parse.getType();
            }
        }
        return itype;
    }
    
    /**
     * Check if a particular value is required or optional.
     * 
     * @param name
     * @param reqset
     * @param optset
     * @return <code>TRUE</code> if required, <code>FALSE</code> if optional, <code>null</code> if unknown
     */
    private static Boolean checkRequired(String name, Set reqset, Set optset) {
        if (reqset != null && reqset.contains(name)) {
            return Boolean.TRUE;
        } else if (optset != null && optset.contains(name)) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }
    
    /**
     * Apply customizations to method to fill out parameter and return information.
     * 
     * @param method
     * @param icl
     * @param fmt
     */
    public void apply(IClassItem method, IClassLocator icl, IDocumentFormatter fmt) {
        
        // fill in missing details
        s_logger.debug("Applying operation from method " + m_methodName);
        if (m_operationName == null) {
            String name = convertName(m_methodName, CAMEL_CASE_NAMES);
            m_operationName = registerName(name, this);
            s_logger.debug("Set operation name " + m_operationName);
        } else if (!m_operationName.equals(registerName(m_operationName, this))) {
            throw new IllegalStateException("Operation name conflict for '" + m_operationName + '\'');
        }
        if (m_requestMessageName == null) {
            m_requestMessageName = m_operationName + "Message";
        }
        if (m_requestWrapperName == null) {
            m_requestWrapperName = m_operationName;
        }
        if (m_responseMessageName == null) {
            m_responseMessageName = m_operationName + "ResponseMessage";
        }
        if (m_responseWrapperName == null) {
            m_responseWrapperName = m_operationName + "Response";
        }
        if (m_soapAction == null && isSoapAction()) {
            m_soapAction = "urn:" + m_operationName;
        }
        if (m_documentation == null) {
            m_documentation = fmt.docToNodes(method.getJavaDoc());
        }
        
        // find parameter types, and item types for collections
        int count = method.getArgumentCount();
        String[] ptypes = new String[count];
        String[] pitypes = new String[count];
        String rtype = null;
        String ritype = null;
        String sig = method.getGenericsSignature();
        if (sig == null) {
            
            // no signature, just use basic type information
            for (int i = 0; i < count; i++) {
                String type = method.getArgumentType(i);
                ptypes[i] = type;
                if (isCollection(type, icl)) {
                    pitypes[i] = "java.lang.Object";
                }
            }
            rtype = method.getTypeName();
            if (isCollection(rtype, icl)) {
                ritype = "java.lang.Object";
            }
            
        } else {
            
            // parse the signature to check collection item types
            SignatureParser parse = new SignatureParser(sig);
            int index = 0;
            boolean inparms = false;
            while (parse.next() != SignatureParser.END_EVENT) {
                switch (parse.getEvent()) {
                    
                    case SignatureParser.METHOD_PARAMETERS_START_EVENT:
                        inparms = true;
                        index = 0;
                        break;
                    
                    case SignatureParser.METHOD_PARAMETERS_END_EVENT:
                        inparms = false;
                        break;
                    
                    case SignatureParser.TYPE_EVENT:
                        String type = parse.getType();
                        String itype = null;
                        if (parse.isParameterized()) {
                            String ptype = parameterType(parse);
                            IClass info = icl.getRequiredClassInfo(type);
                            if (info.isImplements("Ljava/util/Collection;")) {
                                itype = ptype;
                            }
                        }
                        if (inparms) {
                            ptypes[index] = type;
                            pitypes[index++] = itype;
                        } else {
                            rtype = type;
                            ritype = itype;
                        }
                        break;
                    
                }
            }
        }
        
        // create map of parameters using separate customizations
        Map namevalue = new HashMap();
        for (int i = 0; i < m_parameters.size(); i++) {
            ValueCustom value = (ValueCustom)m_parameters.get(i);
            namevalue.put(value.getBaseName(), value);
        }
        m_parameters.clear();
        
        // fill in the parameters and return customizations
        Set reqset = CustomUtils.noCaseNameSet(m_requireds);
        Set optset = CustomUtils.noCaseNameSet(m_optionals);
        for (int i = 0; i < count; i++) {
            String name = method.getParameterName(i);
            if (name == null) {
                name = "arg" + (i + 1);
            }
            ValueCustom value = (ValueCustom)namevalue.get(name);
            if (value == null) {
                value = new ValueCustom(this, name);
                s_logger.debug("Added customization for parameter " + name + " with type " + pitypes[i]);
            } else {
                s_logger.debug("Found customization for parameter " + name + " with type " + pitypes[i]);
            }
            m_parameters.add(value);
            Boolean req = checkRequired(name, reqset, optset);
            List docs = fmt.docToNodes(method.getParameterJavaDoc(i));
            value.complete(icl.getRequiredClassInfo(ptypes[i]), docs, req, pitypes[i]);
        }
        Boolean req = checkRequired("return", reqset, optset);
        String text = method.getReturnJavaDoc();
        boolean isname = false;
        if (text != null && Character.isJavaIdentifierStart(text.charAt(0))) {
            isname = true;
            for (int i = 1; i < text.length(); i++) {
                if (!Character.isJavaIdentifierPart(text.charAt(i))) {
                    isname = false;
                    break;
                }
            }
        }
        String name = "return";
        if (isname) {
            name = text;
            text = null;
        }
        List docs = fmt.docToNodes(text);
        if (m_return == null) {
            m_return = new ValueCustom(this, name);
        }
        m_return.complete(icl.getRequiredClassInfo(rtype), docs, req, ritype);
        
        // add throws information
        count = method.getExceptions().length;
        if (m_throws.size() == 0) {
            for (int i = 0; i < count; i++) {
                name = method.getExceptions()[i];
                ThrowsCustom thrw = new ThrowsCustom(this, name);
                thrw.complete(fmt.docToNodes(method.getExceptionJavaDoc(i)));
                m_throws.add(thrw);
            }
        }
    }
}
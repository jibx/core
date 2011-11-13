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

import java.lang.reflect.Modifier;
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

/**
 * Service customization information. This supports direct service customizations (such as the corresponding request
 * and/or response element name) and also acts as a container for parameter and/or return customizations.
 * 
 * @author Dennis M. Sosnoski
 */
public class ServiceCustom extends NestingBase
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(ServiceCustom.class.getName());
    
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "binding-name", "class", "excludes", "includes", "port-name", "port-type-name",
            "service-address", "service-name", "wsdl-namespace" }, NestingBase.s_allowedAttributes);
    
    // values specific to service level
    private final String m_className;
    
    private String m_serviceName;
    
    private String m_portName;
    
    private String m_bindingName;
    
    private String m_portTypeName;
    
    private String m_wsdlNamespace;
    
    private String m_serviceAddress;
    
    private List m_documentation;
    
    private String[] m_includes;
    
    private String[] m_excludes;
    
    // list of contained operation customizations
    private final ArrayList m_operations;
    
    // values filled in by apply() method
    private IClass m_classInformation;
    
    private String m_namespace;
    
    /**
     * Constructor.
     * 
     * @param parent
     * @param clas
     */
    public ServiceCustom(SharedNestingBase parent, String clas) {
        super(parent);
        m_className = clas;
        m_operations = new ArrayList();
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Get service class name.
     * 
     * @return class name
     */
    public String getClassName() {
        return m_className;
    }
    
    /**
     * Get the service name.
     * 
     * @return service name
     */
    public String getServiceName() {
        return m_serviceName;
    }
    
    /**
     * Get the port name.
     * 
     * @return port name
     */
    public String getPortName() {
        return m_portName;
    }
    
    /**
     * Get the binding name.
     * 
     * @return binding name
     */
    public String getBindingName() {
        return m_bindingName;
    }
    
    /**
     * Get the portType name.
     * 
     * @return portType name
     */
    public String getPortTypeName() {
        return m_portTypeName;
    }
    
    /**
     * Get the service address.
     * 
     * @return service address
     */
    public String getServiceAddress() {
        return m_serviceAddress;
    }
    
    /**
     * Get service documentation node list.
     * 
     * @return list of documentation nodes (<code>null</code> if none)
     */
    public List getDocumentation() {
        return m_documentation;
    }
    
    /**
     * Get list of method names to be excluded as operations.
     * 
     * @return excludes (<code>null</code> if none)
     */
    public String[] getExcludes() {
        return m_excludes;
    }
    
    /**
     * Get list of method names to be included as operations.
     * 
     * @return includes (<code>null</code> if none)
     */
    public String[] getIncludes() {
        return m_includes;
    }
    
    /**
     * Get list of children.
     * 
     * @return list
     */
    public ArrayList getOperations() {
        return m_operations;
    }
    
    /**
     * Get the namespace for WSDL definitions of this service. This value is set by the {@link #apply(IClassLocator)}
     * method.
     * 
     * @return WSDL namespace
     */
    public String getWsdlNamespace() {
        return m_wsdlNamespace;
    }
    
    /**
     * Add child.
     * 
     * @param child
     */
    protected void addChild(CustomBase child) {
        if (child.getParent() == this) {
            m_operations.add(child);
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
    private static ServiceCustom factory(IUnmarshallingContext ictx) throws JiBXException {
        String clas = ((UnmarshallingContext)ictx).attributeText(null, "class");
        s_logger.debug("Creating instance for class " + clas);
        return new ServiceCustom((SharedNestingBase)getContainingObject(ictx), clas);
    }

    /**
     * Derive service-specific namespace URI. The appends the service name to the supplied URI, adding a path separator
     * if necessary.
     *
     * @param uri base URI
     * @return service-specific URI
     */
    private String deriveServiceNamespace(String uri) {
        if (!uri.endsWith("/")) {
            uri = uri + '/';
        }
        uri = uri + m_serviceName;
        return uri;
    }

    /**
     * Apply customizations to service to fill out members.
     * 
     * @param icl class locator
     * @param fmt JavaDoc formatter
     */
    public void apply(IClassLocator icl, IDocumentFormatter fmt) {
        
        // find the service class information
        m_classInformation = icl.getRequiredClassInfo(m_className);
        
        // fill in any missing details
        int split = m_className.lastIndexOf('.');
        if (m_serviceName == null) {
            String simple = m_className.substring(split + 1);
            String name = convertName(simple, UPPER_CAMEL_CASE_NAMES);
            m_serviceName = registerName(name, this);
            s_logger.debug("Generated service name from class name: " + m_serviceName + " (base " + name + ')');
        } else if (!m_serviceName.equals(registerName(m_serviceName, this))) {
            throw new IllegalStateException("Service name conflict for '" + m_serviceName + '\'');
        }
        if (m_portName == null) {
            m_portName = m_serviceName + "Port";
        }
        if (m_bindingName == null) {
            m_bindingName = m_serviceName + "Binding";
        }
        if (m_portTypeName == null) {
            m_portTypeName = m_serviceName + "PortType";
        }
        String uri = getSpecifiedNamespace();
        if (uri == null) {
            
            // append service name to inherited (or derived from package) setting
            int style = getNamespaceStyle();
            String base = uri = deriveNamespace(getParent().getNamespace(), m_classInformation.getPackage(), style);
            if (style == DERIVE_BY_PACKAGE) {
                uri = deriveServiceNamespace(base);
            }
            s_logger.debug("Generated service namespace URI: " + uri + " (base " + base + ')');
            
        }
        setNamespace(uri);
        if (m_wsdlNamespace == null) {
            
            // no WSDL namespace set, check parent setting
            String ns = ((NestingBase)getParent()).getWsdlNamespace();
            if (ns == null) {
                
                // no setting, use class package and service name
                m_wsdlNamespace = packageToNamespace(packageOfType(m_className)) + '/' + m_serviceName;
                s_logger.debug("Generated WSDL namespace URI: " + m_wsdlNamespace);
                
            } else {
                
                // append service name to supplied setting
                m_wsdlNamespace = deriveServiceNamespace(ns);
                s_logger.debug("Derived WSDL namespace URI: " + m_wsdlNamespace);
                
            }
        }
        if (m_serviceAddress == null) {
            String base = getServiceBase();
            if (base != null) {
                StringBuffer buff = new StringBuffer(base);
                if (!base.endsWith("/")) {
                    buff.append('/');
                }
                buff.append(m_serviceName);
                m_serviceAddress = buff.toString();
                s_logger.debug("Generated service address: " + m_serviceAddress);
            }
        }
        if (m_documentation == null) {
            m_documentation = fmt.docToNodes(m_classInformation.getJavaDoc());
        }
        
        // register any predefined operation children
        for (int i = 0; i < m_operations.size(); i++) {
            OperationCustom op = (OperationCustom)m_operations.get(i);
            String name = op.getOperationName();
            if (name != null) {
                if (registerName(name, op) != name) {
                    throw new IllegalStateException("Duplicate operation name " + name);
                }
            }
        }
        
        // generate an operation for each exposed method in service class
        Set inclset = CustomUtils.noCaseNameSet(m_includes);
        Set exclset = CustomUtils.noCaseNameSet(m_excludes);
        Map opmap = new HashMap();
        for (int i = 0; i < m_operations.size(); i++) {
            OperationCustom op = (OperationCustom)m_operations.get(i);
            opmap.put(op.getMethodName().toLowerCase(), op);
            s_logger.debug("Added operation " + op.getOperationName());
        }
        IClassItem[] methods = m_classInformation.getMethods();
        for (int i = 0; i < methods.length; i++) {
            IClassItem method = methods[i];
            String name = method.getName();
            String lcname = name.toLowerCase();
            int access = method.getAccessFlags();
            OperationCustom op = (OperationCustom)opmap.get(lcname);
            if (op == null) {
                if (!Modifier.isStatic(access) && !Modifier.isTransient(access) && !"<init>".equals(name)) {
                    boolean use = true;
                    if (inclset != null) {
                        use = inclset.contains(lcname);
                    } else if (exclset != null) {
                        use = !exclset.contains(lcname);
                    }
                    if (use) {
                        op = new OperationCustom(this, name);
                        m_operations.add(op);
                        s_logger.debug("Generated operation for method " + name);
                    }
                }
            }
            if (op != null) {
                op.apply(method, icl, fmt);
            }
        }
    }
}
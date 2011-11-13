/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.ws.wsdl.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jibx.binding.model.EmptyArrayList;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.GrowableIntArray;
import org.jibx.runtime.impl.GrowableStringArray;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;
import org.jibx.util.StringIntSizedMap;
import org.jibx.util.StringSizedSet;

/**
 * Base class which provides validation hooks and support for extra namespaces. This base class for WSDL element
 * representations is based on the schema data model code.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class WsdlBase
{
    /** Fixed URI for WSDL namespace. */
    public static final String WSDL_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/";
    
    /** Fixed URI for SOAP namespace. */
    public static final String SOAP_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
    
    /** Extension data for application use. */
    private Object m_extension;
    
    /** Namespace definitions associated with this element (lazy create, <code>null</code> if unused). */
    private List m_namespaces;
    
    /** Documentation for component (lazy create, <code>null</code> if unused). */
    private List m_documentation;
    
    /**
     * Get extension data. The actual type of object used for extension data (if any) is defined by the application.
     *
     * @return extension
     */
    public Object getExtension() {
        return m_extension;
    }

    /**
     * Set extension data. The actual type of object used for extension data (if any) is defined by the application.
     *
     * @param extension
     */
    public void setExtension(Object extension) {
        m_extension = extension;
    }
    
    /**
     * Get namespace declarations list. Entries in this list consist of pairs, consisting of namespace prefix followed
     * by namespace URI. The empty string is used as the prefix for the default namespace.
     * 
     * @return extra attribute list
     */
    public final List getNamespaceDeclarations() {
        if (m_namespaces == null || m_namespaces.size() == 0) {
            return EmptyArrayList.INSTANCE;
        } else {
            return m_namespaces;
        }
    }
    
    /**
     * Clear namespace declarations list.
     */
    public final void clearNamespaceDeclarations() {
        if (m_namespaces != null) {
            m_namespaces.clear();
        }
    }
    
    /**
     * Add namespace declaration.
     * 
     * @param prefix namespace prefix
     * @param uri namespace URI
     */
    public final void addNamespaceDeclaration(String prefix, String uri) {
        if (m_namespaces == null) {
            m_namespaces = new ArrayList();
        }
        m_namespaces.add(prefix);
        m_namespaces.add(uri);
    }
    
    /**
     * Get documentation. This is the content of the optional &lt;documentation> child element, which consists of DOM
     * Nodes.
     *
     * @return documentation (<code>null</code> if no documentation present)
     */
    public List getDocumentation() {
        return m_documentation;
    }

    /**
     * Set documentation. This is the content of the optional &lt;documentation> child element, which consists of DOM
     * Nodes.
     *
     * @param documentation
     */
    public void setDocumentation(List documentation) {
        m_documentation = documentation;
    }

    /**
     * Pre-get method to be called by data binding while writing element start tag. The base class implementation just
     * writes out any extra namespaces defined on the element. Subclasses which override this implementation must call
     * the base implementation during their processing.
     * 
     * @param ictx marshalling context
     * @throws JiBXException on marshalling error
     */
    protected void preget(IMarshallingContext ictx) throws JiBXException {
        writeNamespaces(ictx);
    }
    
    /**
     * Pre-set method to be called by data binding while parsing element start tag. The base class implementation just
     * reads in any extra namespaces defined on the element. Subclasses which override this implementation must call the
     * base implementation during their processing.
     * 
     * @param ictx unmarshalling context
     * @throws JiBXException on error
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        readNamespaces(ictx);
    }
    
    /**
     * Validate attributes of element. This is designed to be called during unmarshalling as part of the pre-set method
     * processing when a subclass instance is being created. An error is reported if an element in the default namespace
     * is not in the declared list,
     * 
     * @param ictx unmarshalling context
     * @param other attributes from other namespaces allowed flag
     * @param attrs attributes array
     * @see #preset(IUnmarshallingContext)
     */
    protected void validateAttributes(IUnmarshallingContext ictx, boolean other, StringArray attrs) {
        
        // loop through all attributes of current element
        UnmarshallingContext uctx = (UnmarshallingContext)ictx;
        for (int i = 0; i < uctx.getAttributeCount(); i++) {
            String name = uctx.getAttributeName(i);
            String ns = uctx.getAttributeNamespace(i);
            if (ns == null || ns.length() == 0) {
                
                // check if no-namespace attribute in allowed set
                if (attrs.indexOf(name) < 0) {
                    ValidationContext vctx = (ValidationContext)ictx.getUserContext();
                    vctx.addError("Undefined attribute " + name, ictx.getStackTop());
                }
                
            } else if (WSDL_NAMESPACE_URI.equals(ns)) {
                
                // no unknown attributes from definition namespace are defined
                ValidationContext vctx = (ValidationContext)ictx.getUserContext();
                vctx.addError("Undefined attribute " + name, ictx.getStackTop());
                
            } else if (!other) {
                
                // warn on non-WSDL attribute present where forbidden
                ValidationContext vctx = (ValidationContext)ictx.getUserContext();
                String qname = UnmarshallingContext.buildNameString(ns, name);
                vctx.addWarning("Non-WSDL attribute not allowed " + qname, ictx.getStackTop());
                
            }
        }
    }
    
    /**
     * Collect namespace declarations from element. This is designed to be called during unmarshalling as part of the
     * pre-set method processing when a subclass instance is being created.
     * 
     * @param ictx unmarshalling context
     */
    protected void readNamespaces(IUnmarshallingContext ictx) {
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        int count = ctx.getNamespaceCount();
        if (count > 0) {
            m_namespaces = new ArrayList();
            for (int i = 0; i < count; i++) {
                String pref = ctx.getNamespacePrefix(i);
                if (pref == null) {
                    pref = "";
                }
                m_namespaces.add(pref);
                m_namespaces.add(ctx.getNamespaceUri(i));
            }
        } else {
            m_namespaces = null;
        }
    }
    
    /**
     * Create a unique prefix. If the requested prefix is already in use, this modifies the prefix by appending a
     * numeric suffix to create a unique variant.
     *
     * @param pref requested prefix
     * @param prefixes prefixes in use
     * @return unique prefix
     */
    private String makeUniquePrefix(String pref, StringSizedSet prefixes) {
        int loop = 0;
        while (prefixes.contains(pref)) {
            pref = pref + loop;
        }
        prefixes.add(pref);
        return pref;
    }

    /**
     * Write namespace declarations to element. This is designed to be called during marshalling as part of the pre-get
     * method processing when a subclass instance is being marshalled.
     * 
     * @param ictx marshalling context
     * @throws JiBXException on error writing
     */
    protected void writeNamespaces(IMarshallingContext ictx) throws JiBXException {
        if (m_namespaces != null) {
            try {
                
                // build sets of namespace uris and prefixes
                IXMLWriter writer = ictx.getXmlWriter();
                int curcnt = writer.getNamespaceCount();
                StringIntSizedMap uriindexes = new StringIntSizedMap(curcnt);
                int defcnt = m_namespaces.size() / 2;
                StringSizedSet prefixes = new StringSizedSet(curcnt + defcnt);
                for (int i = 0; i < curcnt; i++) {
                    uriindexes.add(writer.getNamespaceUri(i), i);
                    String prefix = writer.getNamespacePrefix(i);
                    if (prefix != null && prefix.length() > 0) {
                        prefixes.add(prefix);
                    }
                }
                
                // check namespace declarations on this element against writer state
                List uris = new ArrayList();
                GrowableIntArray indexes = new GrowableIntArray();
                GrowableStringArray prefs = new GrowableStringArray();
                int base = writer.getNamespaceCount();
                for (int i = 0; i < defcnt; i++) {
                    String pref = (String)m_namespaces.get(i * 2);
                    String uri = (String)m_namespaces.get(i * 2 + 1);
                    int index = uriindexes.get(uri);
                    if (index >= 0) {
                        if (writer.getNamespacePrefix(index) == null) {
                            indexes.add(index);
                            prefs.add(makeUniquePrefix(pref, prefixes));
                        }
                    } else {
                        indexes.add(base + uris.size());
                        prefs.add(makeUniquePrefix(pref, prefixes));
                        uris.add(uri);
                    }
                }
                
                // add the namespace declarations to current element
                writer.pushExtensionNamespaces((String[])uris.toArray(new String[uris.size()]));
                writer.openNamespaces(indexes.toArray(), prefs.toArray());
                for (int i = 0; i < prefs.size(); i++) {
                    String prefix = prefs.get(i);
                    String name = prefix.length() > 0 ? "xmlns:" + prefix : "xmlns";
                    writer.addAttribute(0, name, writer.getNamespaceUri(indexes.get(i)));
                }
                
            } catch (IOException e) {
                throw new JiBXException("Error writing output document", e);
            }
        }
    }

    /**
     * Prevalidate component information. The prevalidation step is used to check isolated aspects of a component, such
     * as the settings for enumerated values. This empty base class implementation should be overridden by each subclass
     * that requires prevalidation handling.
     * 
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {}
    
    /**
     * Validate component information. The validation step is used for checking the interactions between components,
     * such as name references to other components. The {@link #prevalidate} method will always be called for every
     * component in the schema definition before this method is called for any component. This empty base class
     * implementation should be overridden by each subclass that requires validation handling.
     * 
     * @param vctx validation context
     */
    public void validate(ValidationContext vctx) {}
}
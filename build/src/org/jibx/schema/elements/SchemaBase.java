/*
 * Copyright (c) 2006-2008 Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jibx.binding.model.EmptyArrayList;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.schema.IComponent;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base class for all element structures in schema definition model. This just provides the linkages for the schema
 * definition tree structure and related validation hooks, along with support for extra namespaces.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class SchemaBase implements IComponent
{
    //
    // Element type definitions.
    
    public static final int ALL_TYPE = 0;
    public static final int ANNOTATION_TYPE = 1;
    public static final int ANY_TYPE = 2;
    public static final int ANYATTRIBUTE_TYPE = 3;
    public static final int APPINFO_TYPE = 4;
    public static final int ATTRIBUTE_TYPE = 5;
    public static final int ATTRIBUTEGROUP_TYPE = 6;
    public static final int CHOICE_TYPE = 7;
    public static final int COMPLEXCONTENT_TYPE = 8;
    public static final int COMPLEXTYPE_TYPE = 9;
    public static final int DOCUMENTATION_TYPE = 10;
    public static final int ELEMENT_TYPE = 11;
    public static final int ENUMERATION_TYPE = 12;
    public static final int EXTENSION_TYPE = 13;
    public static final int FIELD_TYPE = 14;
    public static final int FRACTIONDIGITS_TYPE = 15;
    public static final int GROUP_TYPE = 16;
    public static final int IMPORT_TYPE = 17;
    public static final int INCLUDE_TYPE = 18;
    public static final int KEY_TYPE = 19;
    public static final int KEYREF_TYPE = 20;
    public static final int LENGTH_TYPE = 21;
    public static final int LIST_TYPE = 22;
    public static final int MAXEXCLUSIVE_TYPE = 23;
    public static final int MAXINCLUSIVE_TYPE = 24;
    public static final int MAXLENGTH_TYPE = 25;
    public static final int MINEXCLUSIVE_TYPE = 26;
    public static final int MININCLUSIVE_TYPE = 27;
    public static final int MINLENGTH_TYPE = 28;
    public static final int NOTATION_TYPE = 29;
    public static final int PATTERN_TYPE = 30;
    public static final int REDEFINE_TYPE = 31;
    public static final int RESTRICTION_TYPE = 32;
    public static final int SCHEMA_TYPE = 33;
    public static final int SELECTOR_TYPE = 34;
    public static final int SEQUENCE_TYPE = 35;
    public static final int SIMPLECONTENT_TYPE = 36;
    public static final int SIMPLETYPE_TYPE = 37;
    public static final int TOTALDIGITS_TYPE = 38;
    public static final int UNION_TYPE = 39;
    public static final int UNIQUE_TYPE = 40;
    public static final int WHITESPACE_TYPE = 41;
    
    /** Actual element names. */
    public static final String[] ELEMENT_NAMES;
    
    /** Bit masks for individual elements. */
    public static final long[] ELEMENT_MASKS;
    
    static {
        ELEMENT_NAMES = new String[WHITESPACE_TYPE + 1];
        ELEMENT_NAMES[ALL_TYPE] = "all";
        ELEMENT_NAMES[ANNOTATION_TYPE] = "annotation";
        ELEMENT_NAMES[ANY_TYPE] = "any";
        ELEMENT_NAMES[ANYATTRIBUTE_TYPE] = "anyAttribute";
        ELEMENT_NAMES[APPINFO_TYPE] = "appinfo";
        ELEMENT_NAMES[ATTRIBUTE_TYPE] = "attribute";
        ELEMENT_NAMES[ATTRIBUTEGROUP_TYPE] = "attributeGroup";
        ELEMENT_NAMES[CHOICE_TYPE] = "choice";
        ELEMENT_NAMES[COMPLEXCONTENT_TYPE] = "complexContent";
        ELEMENT_NAMES[COMPLEXTYPE_TYPE] = "complexType";
        ELEMENT_NAMES[DOCUMENTATION_TYPE] = "documentation";
        ELEMENT_NAMES[ELEMENT_TYPE] = "element";
        ELEMENT_NAMES[ENUMERATION_TYPE] = "enumeration";
        ELEMENT_NAMES[EXTENSION_TYPE] = "extension";
        ELEMENT_NAMES[FIELD_TYPE] = "field";
        ELEMENT_NAMES[FRACTIONDIGITS_TYPE] = "fractionDigits";
        ELEMENT_NAMES[GROUP_TYPE] = "group";
        ELEMENT_NAMES[IMPORT_TYPE] = "import";
        ELEMENT_NAMES[INCLUDE_TYPE] = "include";
        ELEMENT_NAMES[KEY_TYPE] = "key";
        ELEMENT_NAMES[KEYREF_TYPE] = "keyref";
        ELEMENT_NAMES[LENGTH_TYPE] = "length";
        ELEMENT_NAMES[LIST_TYPE] = "list";
        ELEMENT_NAMES[MAXEXCLUSIVE_TYPE] = "maxExclusive";
        ELEMENT_NAMES[MAXINCLUSIVE_TYPE] = "maxInclusive";
        ELEMENT_NAMES[MAXLENGTH_TYPE] = "maxLength";
        ELEMENT_NAMES[MINEXCLUSIVE_TYPE] = "minExclusive";
        ELEMENT_NAMES[MININCLUSIVE_TYPE] = "minInclusive";
        ELEMENT_NAMES[MINLENGTH_TYPE] = "minLength";
        ELEMENT_NAMES[NOTATION_TYPE] = "notation";
        ELEMENT_NAMES[PATTERN_TYPE] = "pattern";
        ELEMENT_NAMES[REDEFINE_TYPE] = "redefine";
        ELEMENT_NAMES[RESTRICTION_TYPE] = "restriction";
        ELEMENT_NAMES[SCHEMA_TYPE] = "schema";
        ELEMENT_NAMES[SELECTOR_TYPE] = "selector";
        ELEMENT_NAMES[SEQUENCE_TYPE] = "sequence";
        ELEMENT_NAMES[SIMPLECONTENT_TYPE] = "simpleContent";
        ELEMENT_NAMES[SIMPLETYPE_TYPE] = "simpleType";
        ELEMENT_NAMES[TOTALDIGITS_TYPE] = "totalDigits";
        ELEMENT_NAMES[UNION_TYPE] = "union";
        ELEMENT_NAMES[UNIQUE_TYPE] = "unique";
        ELEMENT_NAMES[WHITESPACE_TYPE] = "whiteSpace";
        ELEMENT_MASKS = new long[ELEMENT_NAMES.length];
        long mask = 1;
        for (int i = 0; i < ELEMENT_NAMES.length; i++) {
            if (ELEMENT_NAMES[i] == null) {
                throw new RuntimeException("Array not properly initialized for index " + i);
            }
            ELEMENT_MASKS[i] = mask;
            mask <<= 1;
        }
    };
    
    //
    // Instance data.
    
    /** Element type code. */
    private final int m_type;
    
    /** Parent element. */
    private OpenAttrBase m_parent;
    
    /** Extension data for application use. */
    private Object m_extension;
    
    /**
     * Namespace definitions associated with this element (lazy create, <code>null</code> if unused).
     */
    private ArrayList m_namespaces;
    
    /**
     * Constructor.
     * 
     * @param type element type code
     */
    protected SchemaBase(int type) {
        m_type = type;
    }
    
    /**
     * Get element type.
     * 
     * @return type code for this element
     */
    public final int type() {
        return m_type;
    }
    
    /**
     * Get element name.
     * 
     * @return type code for this element
     */
    public final String name() {
        return ELEMENT_NAMES[m_type];
    }
    
    /**
     * Get element bit mask.
     *
     * @return bit mask for this element
     */
    public final long bit() {
        return ELEMENT_MASKS[m_type];
    }
    
    /**
     * Get parent element.
     * 
     * @return parent element, <code>null</code> if none (schema element only)
     */
    public final OpenAttrBase getParent() {
        return m_parent;
    }
    
    /**
     * Set parent element. This method is provided for use by subclasses and other classes in this package (particularly
     * {@link FilteredSegmentList}).
     * 
     * @param parent
     */
    protected final void setParent(OpenAttrBase parent) {
        m_parent = parent;
    }
    
    /**
     * Get the ancestor schema element. It is an error to call this method with an element which is not part of a
     * schema, resulting in a runtime exception.
     *
     * @return schema
     */
    public final SchemaElement getSchema() {
        SchemaBase element = this;
        while (element.type() != SCHEMA_TYPE) {
            element = element.m_parent;
            if (element == null) {
                throw new IllegalStateException("Internal error - no ancestor schema element");
            }
        }
        return (SchemaElement)element;
    }
    
    /**
     * Check if this element represents a global definition.
     *
     * @return <code>true</code> if global, <code>false</code> if not
     */
    public final boolean isGlobal() {
        return m_parent instanceof SchemaElement;
    }
    
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
    public final ArrayList getNamespaceDeclarations() {
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
     * Get count of child elements.
     * 
     * @return child count
     */
    public abstract int getChildCount();
    
    /**
     * Get read-only iterator for child elements.
     * 
     * @return iterator
     */
    public abstract Iterator getChildIterator();
    
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
     * sets the parent element link and reads in any extra namespaces defined on the element. Subclasses which override
     * this implementation must call the base implementation during their processing.
     * 
     * @param ictx unmarshalling context
     * @throws JiBXException on error
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        Object parent = ictx.getStackTop();
        if (parent instanceof Collection) {
            parent = ictx.getStackObject(1);
        }
        if (parent instanceof OpenAttrBase) {
            m_parent = (OpenAttrBase)parent;
        }
        readNamespaces(ictx);
    }
    
    /**
     * Validate attributes of element. This is designed to be called during unmarshalling as part of the pre-set method
     * processing when a subclass instance is being created.
     * 
     * @param ictx unmarshalling context
     * @param extra allow extra attributes from other namespaces flag
     * @param attrs attributes array
     * @see #preset(IUnmarshallingContext)
     */
    protected static void validateAttributes(IUnmarshallingContext ictx, boolean extra, StringArray attrs) {
        
        // loop through all attributes of current element
        UnmarshallingContext uctx = (UnmarshallingContext)ictx;
        for (int i = 0; i < uctx.getAttributeCount(); i++) {
            String name = uctx.getAttributeName(i);
            String ns = uctx.getAttributeNamespace(i);
            if (ns == null || ns.length() == 0) {
                
                // check if schema attribute in allowed set
                if (attrs.indexOf(name) < 0) {
                    ValidationContext vctx = (ValidationContext)ictx.getUserContext();
                    vctx.addError("Undefined attribute " + name, ictx.getStackTop());
                }
                
            } else if (SCHEMA_NAMESPACE.equals(ns)) {
                
                // no attributes from schema namespace are defined
                ValidationContext vctx = (ValidationContext)ictx.getUserContext();
                vctx.addError("Undefined attribute " + name, ictx.getStackTop());
                
            } else if (!extra) {
                
                // warn on non-schema attribute present where forbidden
                ValidationContext vctx = (ValidationContext)ictx.getUserContext();
                String qname = UnmarshallingContext.buildNameString(ns, name);
                vctx.addWarning("Non-schema attribute not allowed " + qname, ictx.getStackTop());
                
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
                if (pref.equals("xs")) {
                    if (SCHEMA_NAMESPACE.equals(ctx.getNamespaceUri(i))) {
                        continue;
                    } else {
                        throw new RuntimeException("Cannot handle 'xs' prefix associated with non-schema namespace");
                    }
                }
                m_namespaces.add(pref);
                m_namespaces.add(ctx.getNamespaceUri(i));
            }
        } else {
            m_namespaces = null;
        }
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
                
                // set up information for namespace indexes and prefixes
                IXMLWriter writer = ictx.getXmlWriter();
                String[] uris = new String[m_namespaces.size() / 2];
                int[] indexes = new int[uris.length];
                String[] prefs = new String[uris.length];
                int base = writer.getNamespaceCount();
                for (int i = 0; i < uris.length; i++) {
                    indexes[i] = base + i;
                    prefs[i] = (String)m_namespaces.get(i * 2);
                    uris[i] = (String)m_namespaces.get(i * 2 + 1);
                }
                
                // add the namespace declarations to current element
                writer.pushExtensionNamespaces(uris);
                writer.openNamespaces(indexes, prefs);
                for (int i = 0; i < uris.length; i++) {
                    String prefix = prefs[i];
                    String name = prefix.length() > 0 ? "xmlns:" + prefix : "xmlns";
                    writer.addAttribute(0, name, uris[i]);
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
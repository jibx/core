/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen.custom;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.validation.ProblemHandler;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.schema.validation.ValidationProblem;
import org.jibx.util.LazyList;
import org.jibx.util.NameUtilities;
import org.jibx.util.StringArray;

/**
 * Customization information for a set of schemas.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemasetCustom extends SchemaRootBase
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(SchemasetCustom.class.getName());
    
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "names", "namespaces" }, SchemaRootBase.s_allowedAttributes);
    
    //
    // Unmarshalled data
    
    /** Schema name patterns. */
    private String[] m_names;
    
    /** Schema namespace patterns. */
    private String[] m_namespaces;
    
    //
    // Instance data
    
    /** Map from schema identifier to customization. */
    private final Map m_schemaMap;
    
    /**
     * Normal constructor.
     * 
     * @param parent
     */
    public SchemasetCustom(SchemasetCustom parent) {
        super(parent);
        m_schemaMap = new HashMap();
    }
    
    /**
     * Constructor with single schema customization as content.
     * 
     * @param child
     */
    public SchemasetCustom(SchemaCustom child) {
        super(null);
        child.setParent(this);
        getChildren().add(child);
        m_schemaMap = new HashMap();
    }
    /**
     * Get schema name match patterns.
     *
     * @return names (<code>null</code> if not set)
     */
    public String[] getNames() {
        return m_names;
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
     * Check if a schema is included in this set.
     * 
     * @param name schema file name
     * @param schema actual schema
     * @return <code>true</code> if in set, <code>false</code> if not
     */
    public boolean isInSet(String name, SchemaElement schema) {
        
        // check for match on file name
        boolean match = true;
        if (m_names != null) {
            match = false;
            for (int i = 0; i < m_names.length; i++) {
                if (NameUtilities.isPatternMatch(name, m_names[i])) {
                    match = true;
                    break;
                }
            }
        }
        
        // check for match on effective namespace
        if (m_namespaces != null) {
            match = false;
            String ns = schema.getEffectiveNamespace();
            if (ns == null) {
                ns = "";
            }
            for (int i = 0; i < m_namespaces.length; i++) {
                if (NameUtilities.isPatternMatch(ns, m_namespaces[i])) {
                    match = true;
                    break;
                }
            }
        }
        
        // return 'and' of two tests
        return match;
    }
    
    /**
     * Get existing schema customization information.
     * 
     * @param id schema identifier
     * @return customization
     */
    public SchemaCustom getCustomization(String id) {
        return (SchemaCustom)m_schemaMap.get(id);
    }
    
    /**
     * Get schema customization information, creating it if it doesn't already exist.
     * 
     * @param name schema file name
     * @param id unique identifier for schema
     * @param schema actual schema
     * @param vctx validation context for reporting errors
     * @return customization
     */
    public SchemaCustom forceCustomization(String name, String id, SchemaElement schema, ValidationContext vctx) {
        SchemaCustom custom = (SchemaCustom)m_schemaMap.get(name);
        if (custom == null) {
            
            // check for unique match to schema customization
            for (int i = 0; i < getChildren().size(); i++) {
                SchemaRootBase child = (SchemaRootBase)getChildren().get(i);
                if (child instanceof SchemaCustom && ((SchemaCustom)child).checkMatch(name, schema)) {
                    if (custom == null) {
                        custom = (SchemaCustom)child;
                    } else {
                        vctx.addError("Multiple matches to schema " + name + " (first match " +
                            ValidationProblem.componentDescription(custom) + ')', child);
                    }
                }
            }
            
            // create default customization if no match found
            if (custom == null) {
                custom = new SchemaCustom(this);
            }
            
            // add customization to map and link to schema
            m_schemaMap.put(id, custom);
            custom.setSchema(name, schema);
            schema.setExtension(custom);
            
        }
        return custom;
    }
    
    /**
     * Factory used during unmarshalling.
     *
     * @param ictx
     * @return instance
     */
    private static SchemasetCustom factory(IUnmarshallingContext ictx) {
        return new SchemasetCustom((SchemasetCustom)getContainingObject(ictx));
    }
    
    /**
     * Recursively check that each schema customization has been matched to a schema. A warning is generated for any
     * customization without a matching schema.
     *
     * @param vctx
     */
    public void checkSchemas(ValidationContext vctx) {
        
        // check for unique match to schema customization
        for (int i = 0; i < getChildren().size(); i++) {
            SchemaRootBase child = (SchemaRootBase)getChildren().get(i);
            if (child instanceof SchemaCustom) {
                SchemaCustom custom = (SchemaCustom)child;
                if (custom.getSchema() == null) {
                    StringBuffer buff = new StringBuffer("No schema loaded for customization");
                    if (custom.getName() != null) {
                        buff.append(" with name ");
                        buff.append(custom.getName());
                    } else if (custom.getNamespace() != null) {
                        buff.append(" with namespace ");
                        buff.append(custom.getNamespace());
                    }
                    vctx.addWarning(buff.toString(), custom);
                }
            } else if (child instanceof SchemasetCustom) {
                ((SchemasetCustom)child).checkSchemas(vctx);
            }
        }
    }

    /**
     * Validate and finalize customization information. This override of the base class implementation also invokes the
     * same method on any nested schemasets in order to make sure that the type substitution map and active facets mask
     * will be available for use by nested schemas.
     *
     * @param vctx validation context
     * @return <code>true</code> if valid, <code>false</code> if not
     */
    public boolean validate(ValidationContext vctx) {
        boolean valid = super.validate(vctx);
        if (valid) {
            LazyList children = getChildren();
            for (int i = 0; i < children.size(); i++) {
                Object child = children.get(i);
                if (child instanceof SchemasetCustom) {
                    if (!((SchemasetCustom)child).validate(vctx)) {
                        valid = false;
                    }
                }
            }
        }
        return valid;
    }
    
    /**
     * Load a customizations file. The specified file must exist and have as root element either a &lt;schema-set> or
     * &lt;schema> element.
     *
     * @param path customization file path (<code>null</code> if none)
     * @param handler validation problem handler
     * @return unmarshalled customizations, or <code>null</code> if errors
     * @throws JiBXException 
     * @throws IOException 
     */
    public static SchemasetCustom loadCustomizations(String path, ProblemHandler handler)
    throws JiBXException, IOException {
        
        // load customizations and check for errors
        SchemasetCustom custom = null;
        ValidationContext vctx = new ValidationContext();
        if (path == null) {
            custom = new SchemasetCustom((SchemasetCustom)null);
        } else {
            
            // unmarshal either a <schema-set> or <schema> element
            IBindingFactory fact = BindingDirectory.getFactory(SchemasetCustom.class);
            IUnmarshallingContext ictx = fact.createUnmarshallingContext();
            FileInputStream is = new FileInputStream(path);
            ictx.setDocument(is, null);
            ictx.setUserContext(vctx);
            Object obj = ictx.unmarshalElement();
            if (obj instanceof SchemasetCustom) {
                custom = (SchemasetCustom)obj;
            } else if (obj instanceof SchemaCustom) {
                custom = new SchemasetCustom((SchemaCustom)obj);
            } else {
                throw new IOException("Customization document root element must be 'schema-set' or 'schema'");
            }
            
        }
        if (vctx.reportProblems(handler)) {
            return null;
        }
        return custom;
    }
}
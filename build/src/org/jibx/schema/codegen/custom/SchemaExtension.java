/*
 * Copyright (c) 2008-2010, Dennis M. Sosnoski. All rights reserved.
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

import java.util.Map;

import org.jibx.schema.codegen.PackageHolder;
import org.jibx.schema.codegen.extend.ClassDecorator;
import org.jibx.schema.codegen.extend.NameConverter;
import org.jibx.schema.elements.SchemaElement;

/**
 * Extension information for a schema element.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaExtension extends BaseExtension
{
    /** Customization information for the schema. */
    private final SchemaCustom m_custom;
    
    /** Map from schema type name to Java type information. */
    private Map m_schemaTypes;
    
    /** Name converter instance (<code>null</code> if none set at level). */
    private NameConverter m_nameConverter;
    
    /** Decorators to be used in code generation (<code>null</code> if none set at level). */
    private ClassDecorator[] m_decorators;
    
    /** Package to be used for class generation (may be <code>null</code> if no code generation). */
    private final PackageHolder m_package;
    
    /** Force a binding for this schema flag. */
    private final boolean m_forceBinding;
    
    /** Binding file name (only allowed if single namespace, <code>null</code> if derived from schema name). */
    private final String m_bindingFileName;
    
    /** Prefix used for namespace (only allowed if single namespace, <code>null</code> if from schema). */
    private final String m_prefix;
    
    /** Inline xs:group and xs:attributeGroup definitions by default. */
    private final boolean m_inlineGroups;
    
    /** Prefer inline definitions. */
    private final boolean m_preferInline;
    
    /** Use inner classes for substructures. */
    private final boolean m_useInnerClasses;
    
    /** Always specify property types flag. */
    private boolean m_forceTypes;

    /**
     * Constructor.
     * 
     * @param schema root element of schema definition
     * @param custom schema customizations
     * @param pack package for code generated from schema
     */
    public SchemaExtension(SchemaElement schema, SchemaCustom custom, PackageHolder pack) {
        super(schema);
        m_schemaTypes = custom.getSchemaTypes();
        m_nameConverter = custom.getNameConverter();
        m_decorators = custom.getClassDecorators();
        m_package = pack;
        m_bindingFileName = custom.getBindingFileName();
        m_forceBinding = custom.isBindingPerSchema();
        m_prefix = custom.getPrefix();
        m_inlineGroups = custom.isInlineGroups();
        m_preferInline = custom.isPreferInline();
        m_useInnerClasses = custom.isUseInner();
        setTypeReplacer(custom);
        m_custom = custom;
    }

    /**
     * Get the containing global extension.
     *
     * @return global
     */
    public SchemaCustom getCustom() {
        return m_custom;
    }
    
    /**
     * Get map from schema type local name to type information.
     *
     * @return map
     */
    public Map getSchemaTypes() {
        return m_schemaTypes;
    }
    
    /**
     * Get name converter used for this component.
     *
     * @return converter
     */
    public NameConverter getNameConverter() {
        return m_nameConverter;
    }
    
    /**
     * Get code generation decorators used for this component.
     *
     * @return converter
     */
    public ClassDecorator[] getClassDecorators() {
        return m_decorators;
    }

    /**
     * Get package for class generation.
     *
     * @return package
     */
    public PackageHolder getPackage() {
        return m_package;
    }
    
    /**
     * Check if separate binding forced for schema.
     *
     * @return forced
     */
    public boolean isForceBinding() {
        return m_forceBinding;
    }
    
    /**
     * Get binding definition file name. The binding name may not be set if more than one namespace is used in the
     * schemas represented by this customization.
     *
     * @return name, <code>null</code> if to be derived from schema name
     */
    public String getBindingFileName() {
        return m_bindingFileName;
    }

    /**
     * Get prefix used for namespace. The prefix may not be set if more than one namespace is used in the schemas
     * represented by this customization.
     *
     * @return prefix, <code>null</code> if to be found from schema
     */
    public String getPrefix() {
        return m_prefix;
    }
    
    /**
     * Check whether xs:group and xs:attributeGroup definitions should be inlined by default.
     *
     * @return generate unused flag
     */
    public boolean isInlineGroups() {
        return m_inlineGroups;
    }
    
    /**
     * Check if inlining of definition preferred.
     *
     * @return inline
     */
    public boolean isPreferInline() {
        return m_preferInline;
    }
    
    /**
     * Check if inner classes should be used for substructures.
     *
     * @return inner
     */
    public boolean isUseInnerClasses() {
        return m_useInnerClasses;
    }
    
    /**
     * Check if type specifications forced for schema.
     * 
     * @return force
     */
    public boolean isForceTypes() {
        return m_forceTypes;
    }
}
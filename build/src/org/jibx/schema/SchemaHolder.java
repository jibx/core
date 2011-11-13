/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jibx.schema.attributes.FormChoiceAttribute;
import org.jibx.schema.elements.ImportElement;
import org.jibx.schema.elements.IncludeElement;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.util.HolderBase;
import org.jibx.util.UniqueNameSet;

/**
 * External data for a schema definition. This tracks references to other schemas, along with the associated namespace
 * information. The {@link #finish()} method actually generates the includes.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaHolder extends HolderBase
{
    /** Actual schema definition. */
    private final SchemaElement m_schema;
    
    /** Set of type names defined in schema (only for new schema definitions). */
    private final UniqueNameSet m_typeNameSet;
    
    /** Set of element names defined in schema (also used for group/attributeGroup; only for new definitions). */
    private final UniqueNameSet m_elementNameSet;
    
    /** Set of schemas imported into this schema. */
    private Set m_fixedSet;
    
    /**
     * Constructor for new schema definition file.
     * 
     * @param uri (<code>null</code> if no-namespace schema)
     */
    public SchemaHolder(String uri) {
        super(uri);
        m_schema = new SchemaElement();
        m_typeNameSet = new UniqueNameSet();
        m_elementNameSet = new UniqueNameSet();
        if (uri != null) {
            m_schema.setElementFormDefault(FormChoiceAttribute.QUALIFIED_FORM);
            m_schema.setTargetNamespace(uri);
            m_schema.addNamespaceDeclaration("tns", uri);
        }
    }
    
    /**
     * Constructor for existing schema definition file.
     * 
     * @param schema schema definition
     */
    public SchemaHolder(SchemaElement schema) {
        super(schema.getTargetNamespace(), schema.getResolver().getName());
        m_schema = schema;
        m_typeNameSet = m_elementNameSet = null;
    }
    
    /**
     * Get the schema definition.
     * 
     * @return definition
     */
    public SchemaElement getSchema() {
        return m_schema;
    }
    
    /**
     * Add type name to set defined. This assures uniqueness of the name used, if necessary modifying the supplied base
     * name to a unique alternative.
     * 
     * @param base name to try adding
     * @return name to be used for type
     */
    public String addTypeName(String base) {
        if (!isModifiable()) {
            throw new IllegalStateException("Internal error - file is not modifiable");
        }
        return m_typeNameSet.add(base);
    }
    
    /**
     * Add element name to set defined. This assures uniqueness of the name used, if necessary modifying the supplied
     * base name to a unique alternative. The same set of names is also used for groups and attributeGroups, even though
     * these name sets are separate in schema terms. Doing things this way avoids the possibility of an element name
     * matching a group name with the two representing different structures.
     * 
     * @param base name to try adding
     * @return name to be used for element
     */
    public String addElementName(String base) {
        if (!isModifiable()) {
            throw new IllegalStateException("Internal error - file is not modifiable");
        }
        return m_elementNameSet.add(base);
    }
    
    /**
     * Implementation method to handle adding a namespace declaration. This sets up the namespace declaration for output
     * in the generated XML.
     * 
     * @param prefix
     * @param uri
     */
    protected void addNamespaceDecl(String prefix, String uri) {
        m_schema.addNamespaceDeclaration(prefix, uri);
    }
    
    /**
     * Implementation method to handle references from this schema to other schemas. This adds import elements to the
     * constructed schema for all referenced schemas.
     */
    public void finish() {
        if (!isModifiable()) {
            throw new IllegalStateException("Internal error - file is not modifiable");
        }
        if (m_fixedSet == null) {
            m_fixedSet = new HashSet();
        }
        for (Iterator iter = getReferences().iterator(); iter.hasNext();) {
            SchemaHolder holder = (SchemaHolder)iter.next();
            if (!m_fixedSet.contains(holder)) {
                String ns = holder.getNamespace();
                String file = holder.getFileName();
                if (ns == null || ns.equals(getNamespace())) {
                    if (file != null) {
                        IncludeElement inc = new IncludeElement();
                        inc.setLocation(file);
                        m_schema.getSchemaChildren().add(inc);
                    }
                } else {
                    ImportElement imp = new ImportElement();
                    imp.setLocation(file);
                    imp.setNamespace(ns);
                    m_schema.getSchemaChildren().add(imp);
                    getPrefix(ns);
                }
                m_fixedSet.add(holder);
            }
        }
        super.finish();
    }
}
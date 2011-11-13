/*
Copyright (c) 2006-2008, Dennis M. Sosnoski.
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

package org.jibx.schema.elements;

import java.io.IOException;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.ISchemaResolver;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Model component for <b>import</b> element.
 *
 * @author Dennis M. Sosnoski
 */
public class ImportElement extends SchemaLocationBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "namespace" },
        SchemaLocationBase.s_allowedAttributes);
    
    /** 'namespace' attribute value. */
    private String m_namespace;
    
    /**
     * Constructor.
     */
    public ImportElement() {
    	super(IMPORT_TYPE);
    }
    
    //
    // Access methods
    
    public String getNamespace() {
        return m_namespace;
    }
    
    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }
    
    //
    // Base class overrides

    protected String getEffectiveNamespace() {
        return null;
    }

    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
    
    //
    // Base class overrides

    /**
     * Prevalidation handling. This loads and sets the referenced schema, using
     * either the 'namespace' or 'schemaLocation' attribute, or both.
     * 
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {
        String location = getLocation();
        if (location == null) {
            if (m_namespace == null) {
                
                // no way to find the referenced schema
                vctx.addFatal("Either 'namespace' or 'schemaLocation' value required", this);
                
            } else {
                
                // find schema based on namespace (must already be known)
                SchemaElement schema = vctx.getSchemaById(m_namespace);
                if (schema == null) {
                    vctx.addFatal("No known schema for namespace " + m_namespace, this);
                } else {
                    setReferencedSchema(schema);
                }
            }
            
        } else {
            try {
                
                // find or load referenced schema
                SchemaElement curschema = vctx.getCurrentSchema();
                ISchemaResolver resolver =
                    curschema.getResolver().resolve(location, m_namespace);
                SchemaElement schema = vctx.getSchemaById(resolver.getId());
                if (schema == null) {
                    schema = readSchema(vctx, resolver);
                    vctx.setSchema(resolver.getId(), schema);
                    schema.setResolver(resolver);
                }
                setReferencedSchema(schema);
                
            } catch (JiBXException e) {
                vctx.addFatal("Error loading schema: " + e.getMessage(), this);
            } catch (IOException e) {
                vctx.addFatal("Error loading schema: " + e.getMessage(), this);
            }
        }
        super.prevalidate(vctx);
    }
}
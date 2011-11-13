/*
Copyright (c) 2006-2009, Dennis M. Sosnoski.
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

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.ISchemaResolver;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base class for elements referencing an external schema. Subclasses need to
 * set the referenced schema during the prevalidation pass, so that the
 * referenced schema will be included in the prevalidation processing.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class SchemaLocationBase extends AnnotatedBase
{
	/** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "schemaLocation" },
        AnnotatedBase.s_allowedAttributes);
    
    //
    // Instance data
    
    /** 'schemaLocation' attribute value. */
    private String m_location;
    
    /** Referenced schema definition. */
    private SchemaElement m_schema;
    
    /**
     * Constructor.
     * 
     * @param type element type
     */
    protected SchemaLocationBase(int type) {
		super(type);
	}
    
    /**
     * Get the effective namespace to be applied to the referenced schema. This
     * must be implemented by subclasses to return the namespace to be applied
     * to the schema, if that namespace is different from what is specified in
     * the schema itself.
     *
     * @return namespace
     */
    protected abstract String getEffectiveNamespace();

    /**
     * Load a schema from a resolver.
     *
     * @param vctx validation context
     * @param resolver
     * @return loaded schema
     * @throws JiBXException
     * @throws IOException
     */
    protected SchemaElement readSchema(ValidationContext vctx,
        ISchemaResolver resolver) throws JiBXException, IOException {
        IBindingFactory factory = BindingDirectory.getFactory
            (SchemaUtils.XS_PREFIX_BINDING, SchemaElement.class);
        IUnmarshallingContext uctx = factory.createUnmarshallingContext();
        uctx.setDocument(resolver.getContent(), resolver.getId(), null);
        uctx.setUserContext(vctx);
        SchemaElement schema = new SchemaElement();
        ((IUnmarshallable)schema).unmarshal(uctx);
        return schema;
    }
    
    //
    // Access methods
    
    /**
     * Get 'schemaLocation' attribute value.
     *
     * @return 'schemaLocation' value
     */
    public String getLocation() {
        return m_location;
    }
    
    /**
     * Set 'schemaLocation' attribute value.
     *
     * @param location 'schemaLocation' value
     */
    public void setLocation(String location) {
        m_location = location;
    }
    
    /**
     * Set referenced schema. This method is supplied for the use of subclasses
     * which load the schema through some means other than the 'schemaLocation'
     * attribute value.
     *
     * @param schema schema element
     */
    protected void setReferencedSchema(SchemaElement schema) {
        m_schema = schema;
    }
    
    /**
     * Get referenced schema. This method is only usable after prevalidation.
     *
     * @return schema (<code>null</code> if loading failed)
     */
    public SchemaElement getReferencedSchema() {
        return m_schema;
    }
}
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

import org.jibx.runtime.JiBXException;
import org.jibx.schema.ISchemaResolver;
import org.jibx.schema.validation.ValidationContext;

/**
 * Base class for elements referencing an external schema by using a required
 * 'schemaLocation' attribute. During prevalidation this first reads the
 * referenced schema, so that it'll automatically be included in the
 * prevalidation pass.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class SchemaLocationRequiredBase extends SchemaLocationBase
{
    /**
     * Constructor.
     * 
     * @param type element type
     */
    protected SchemaLocationRequiredBase(int type) {
		super(type);
	}
    
    //
    // Base class overrides

    /**
     * Prevalidation for schema location subclass with the schema specified by a
     * required'schemaLocation' attribute.
     * 
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {
        String location = getLocation();
        if (location == null) {
            vctx.addFatal("Missing required 'schemaLocation' value", this);
        } else {
            try {
                
                // find or load referenced schema
                SchemaElement curschema = vctx.getCurrentSchema();
                ISchemaResolver resolver =
                    curschema.getResolver().resolve(location, null);
                SchemaElement schema = vctx.getSchemaById(resolver.getId());
                if (schema == null) {
                    schema = readSchema(vctx, resolver);
                    vctx.setSchema(resolver.getId(), schema);
                    schema.setResolver(resolver);
                }
                setReferencedSchema(schema);
                if (schema.getEffectiveNamespace() == null) {
                    schema.setEffectiveNamespace(curschema.getEffectiveNamespace());
                }
                
            } catch (JiBXException e) {
                vctx.addFatal("Error loading schema: " + e.getMessage(), this);
            } catch (IOException e) {
                vctx.addFatal("Error loading schema: " + e.getMessage(), this);
            }
        }
        super.prevalidate(vctx);
    }
}
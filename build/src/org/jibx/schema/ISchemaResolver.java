/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for resolving schema references which may be relative to a base location.
 */
public interface ISchemaResolver
{
    /**
     * Resolve a schema reference, which may be relative to this schema location. If a schema location is provided in
     * the call that location should be used to identify the schema; the target namespace should only be used when
     * the schema location is implicit.
     * 
     * @param loc target URL (<code>null</code> if none supplied)
     * @param tns target namespace URI (<code>null</code> if none supplied)
     * @return resolver for target
     * @throws IOException on resolve error
     */
    public ISchemaResolver resolve(String loc, String tns) throws IOException;
    
    /**
     * Get the schema name.
     *
     * @return name
     */
    public String getName();
    
    /**
     * Get unique identifier for this schema.
     * 
     * @return identifier
     */
    public String getId();
    
    /**
     * Get the content associated with this schema document.
     * 
     * @return input stream
     * @throws IOException on access error
     */
    public InputStream getContent() throws IOException;
}
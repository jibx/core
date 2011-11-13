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

package org.jibx.schema;

import java.util.HashSet;
import java.util.Set;

import org.jibx.binding.util.ObjectStack;
import org.jibx.schema.elements.SchemaElement;

/**
 * Current schema name context tracker. This tracks the current schema and the name register associated with that
 * schema.
 */
public class SchemaContextTracker implements ISchemaListener
{
    /** Schema global name register. */
    protected NameRegister m_nameRegister;
    
    /** Set of schema elements already visited. */
    private final Set m_traversedSchemas;
    
    /**
     * Schema element stack. The bottom item in this will always be the root schema element being traversed, while other
     * items represent referenced schemas. The top item will always be the current schema.
     */
    private final ObjectStack m_schemaStack;
    
    /**
     * Constructor.
     */
    public SchemaContextTracker() {
        m_traversedSchemas = new HashSet();
        m_schemaStack = new ObjectStack();
    }
    
    /**
     * Get name register. This requires the name register to have been set, throwing an exception if it has not.
     * 
     * @return name register (never <code>null</code>)
     */
    public NameRegister getNameRegister() {
        if (m_nameRegister == null) {
            throw new IllegalStateException("Internal error: name register has not been set");
        } else {
            return m_nameRegister;
        }
    }
    
    /**
     * Set name register. This is provided for cases where components are being processed individually, so that the user
     * can set the appropriate register for a component directly.
     * 
     * @param reg
     */
    public void setNameRegister(NameRegister reg) {
        m_nameRegister = reg;
    }
    
    /**
     * Get current schema element. This requires the schema to have been set, throwing an exception if it has not.
     * 
     * @return current schema element (never <code>null</code>)
     */
    public SchemaElement getCurrentSchema() {
        if (m_schemaStack.size() == 0) {
            throw new IllegalStateException("Internal error: schema has not been set");
        } else {
            return (SchemaElement)m_schemaStack.peek();
        }
    }
    
    /**
     * Clear the set of schemas that have been traversed. This must be called between passes on a set of schemas, so
     * that all the schemas will again be processed in the new pass.
     */
    public void clearTraversed() {
        m_traversedSchemas.clear();
    }
    
    //
    // ISchemaListener implementation
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ISchemaListener#enterSchema(org.jibx.schema.elements.SchemaElement)
     */
    public boolean enterSchema(SchemaElement schema) {
        if (m_traversedSchemas.contains(schema)) {
            return false;
        } else {
            m_traversedSchemas.add(schema);
            m_schemaStack.push(schema);
            m_nameRegister = schema.getRegister();
            return true;
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ISchemaListener#exitSchema()
     */
    public void exitSchema() {
        m_schemaStack.pop();
        if (m_schemaStack.size() > 0) {
            m_nameRegister = ((SchemaElement)m_schemaStack.peek()).getRegister();
        }
    }
}
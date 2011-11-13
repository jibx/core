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

package org.jibx.schema.validation;

import org.jibx.runtime.QName;
import org.jibx.schema.SchemaVisitor;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupElement;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.GroupElement;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SchemaLocationBase;
import org.jibx.schema.elements.SimpleTypeElement;

/**
 * Visitor for handling the registration of global definitions. This records the names for each child element of the
 * schema in the validation context. This must be run after {@link PrevalidationVisitor}.
 */
public class NameRegistrationVisitor extends SchemaVisitor
{
    /** Validation context. */
    private final ValidationContext m_context;

    /**
     * Constructor.
     * 
     * @param context
     */
    public NameRegistrationVisitor(ValidationContext context) {
        m_context = context;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.ElementBase)
     */
    public boolean visit(SchemaBase node) {
        // make sure nothing gets expanded by default
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.AttributeElement)
     */
    public boolean visit(AttributeElement node) {
        QName qname = node.getQName();
        if (qname != null) {
            m_context.registerAttribute(qname, node);
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.AttributeGroupElement)
     */
    public boolean visit(AttributeGroupElement node) {
        QName qname = node.getQName();
        if (qname != null) {
            m_context.registerAttributeGroup(qname, node);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.ComplexTypeElement)
     */
    public boolean visit(ComplexTypeElement node) {
        QName qname = node.getQName();
        if (qname != null) {
            m_context.registerType(qname, node);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.ElementElement)
     */
    public boolean visit(ElementElement node) {
        QName qname = node.getQName();
        if (qname != null) {
            m_context.registerElement(qname, node);
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.GroupElement)
     */
    public boolean visit(GroupElement node) {
        QName qname = node.getQName();
        if (qname != null) {
            m_context.registerGroup(qname, node);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.SchemaElement)
     */
    public boolean visit(SchemaElement node) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.SchemaLocationBase)
     */
    public boolean visit(SchemaLocationBase node) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jibx.schema.SchemaVisitor#visit(org.jibx.schema.SimpleTypeElement)
     */
    public boolean visit(SimpleTypeElement node) {
        QName qname = node.getQName();
        if (qname != null) {
            m_context.registerType(qname, node);
        }
        return false;
    }
}
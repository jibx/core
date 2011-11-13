/*
 * Copyright (c) 2007, Dennis M. Sosnoski All rights reserved.
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

import org.jibx.schema.elements.AllElement;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AnnotationElement;
import org.jibx.schema.elements.AnnotationItem;
import org.jibx.schema.elements.AnyAttributeElement;
import org.jibx.schema.elements.AnyElement;
import org.jibx.schema.elements.AppInfoElement;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupElement;
import org.jibx.schema.elements.AttributeGroupRefElement;
import org.jibx.schema.elements.ChoiceElement;
import org.jibx.schema.elements.CommonComplexModification;
import org.jibx.schema.elements.CommonCompositorDefinition;
import org.jibx.schema.elements.CommonTypeDefinition;
import org.jibx.schema.elements.ComplexContentElement;
import org.jibx.schema.elements.ComplexExtensionElement;
import org.jibx.schema.elements.ComplexRestrictionElement;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.DocumentationElement;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.GroupElement;
import org.jibx.schema.elements.GroupRefElement;
import org.jibx.schema.elements.ImportElement;
import org.jibx.schema.elements.IncludeElement;
import org.jibx.schema.elements.ListElement;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.RedefineElement;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SchemaLocationBase;
import org.jibx.schema.elements.SequenceElement;
import org.jibx.schema.elements.SimpleContentElement;
import org.jibx.schema.elements.SimpleExtensionElement;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.elements.UnionElement;

/**
 * Instance of {@link SchemaVisitor} that delegates to another instance. This is provided as a base class, allowing
 * selective overrides of normal visitor handling.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaVisitorDelegate extends SchemaVisitor
{
    /** Delegate visitor. */
    private final SchemaVisitor m_delegate;
    
    /**
     * Constructor.
     * 
     * @param delegate
     */
    public SchemaVisitorDelegate(final SchemaVisitor delegate) {
        super();
        m_delegate = delegate;
    }
    
    public void exit(AllElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(AnnotatedBase node) {
        m_delegate.exit(node);
    }
    
    public void exit(AnnotationElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(AnnotationItem node) {
        m_delegate.exit(node);
    }
    
    public void exit(AnyAttributeElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(AnyElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(AppInfoElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(AttributeElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(AttributeGroupElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(AttributeGroupRefElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(ChoiceElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(CommonComplexModification node) {
        m_delegate.exit(node);
    }
    
    public void exit(CommonCompositorDefinition node) {
        m_delegate.exit(node);
    }
    
    public void exit(CommonTypeDefinition node) {
        m_delegate.exit(node);
    }
    
    public void exit(ComplexContentElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(ComplexExtensionElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(ComplexRestrictionElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(ComplexTypeElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(DocumentationElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(SchemaBase node) {
        m_delegate.exit(node);
    }
    
    public void exit(ElementElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(GroupElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(GroupRefElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(ImportElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(IncludeElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(ListElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(OpenAttrBase node) {
        m_delegate.exit(node);
    }
    
    public void exit(RedefineElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(SchemaElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(SchemaLocationBase node) {
        m_delegate.exit(node);
    }
    
    public void exit(SequenceElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(SimpleContentElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(SimpleExtensionElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(SimpleRestrictionElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(SimpleTypeElement node) {
        m_delegate.exit(node);
    }
    
    public void exit(UnionElement node) {
        m_delegate.exit(node);
    }
    
    public boolean visit(AllElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AnnotatedBase node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AnnotationElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AnnotationItem node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AnyAttributeElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AnyElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AppInfoElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AttributeElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AttributeGroupElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(AttributeGroupRefElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(ChoiceElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(CommonComplexModification node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(CommonCompositorDefinition node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(CommonTypeDefinition node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(ComplexContentElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(ComplexExtensionElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(ComplexRestrictionElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(ComplexTypeElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(DocumentationElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(SchemaBase node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(ElementElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(GroupElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(GroupRefElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(ImportElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(IncludeElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(ListElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(OpenAttrBase node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(RedefineElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(SchemaElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(SchemaLocationBase node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(SequenceElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(SimpleContentElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(SimpleExtensionElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(SimpleRestrictionElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(SimpleTypeElement node) {
        return m_delegate.visit(node);
    }
    
    public boolean visit(UnionElement node) {
        return m_delegate.visit(node);
    }
}
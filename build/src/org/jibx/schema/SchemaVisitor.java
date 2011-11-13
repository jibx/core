/*
 * Copyright (c) 2004-2010, Dennis M. Sosnoski. All rights reserved.
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
import org.jibx.schema.elements.CommonCompositorBase;
import org.jibx.schema.elements.CommonCompositorDefinition;
import org.jibx.schema.elements.CommonContentBase;
import org.jibx.schema.elements.CommonTypeDefinition;
import org.jibx.schema.elements.CommonTypeDerivation;
import org.jibx.schema.elements.ComplexContentElement;
import org.jibx.schema.elements.ComplexExtensionElement;
import org.jibx.schema.elements.ComplexRestrictionElement;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.DocumentationElement;
import org.jibx.schema.elements.FacetElement;
import org.jibx.schema.elements.KeyBase;
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
import org.jibx.schema.elements.SchemaLocationRequiredBase;
import org.jibx.schema.elements.SelectionBase;
import org.jibx.schema.elements.SequenceElement;
import org.jibx.schema.elements.SimpleContentElement;
import org.jibx.schema.elements.SimpleExtensionElement;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.elements.UnionElement;

/**
 * Schema model visitor base class. This works with the {@link org.jibx.schema.TreeWalker} class for handling tree-based
 * operations on the schema definition. Subclasses can override any or all of the base class visit and exit methods,
 * including both those for abstract base classes and those for concrete classes, but should normally call the base
 * class implementation of the method in order to implement the class inheritance hierarchy handling.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class SchemaVisitor
{
    //
    // Visit methods for base classes
    
    /**
     * Visit element. This method will be called for every element in the model. The default implementation just returns
     * <code>true</code> to continue expansion of the tree.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SchemaBase node) {
        return true;
    }
    
    /**
     * Visit open attribute element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(OpenAttrBase node) {
        return visit((SchemaBase)node);
    }
    
    /**
     * Visit annotated element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AnnotatedBase node) {
        return visit((OpenAttrBase)node);
    }
    
    //
    // Visit methods for shared implementation classes
    
    /**
     * Visit annotation item element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AnnotationItem node) {
        return visit((SchemaBase)node);
    }
    
    /**
     * Visit compositor base element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(CommonCompositorBase node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit compositor element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(CommonCompositorDefinition node) {
        return visit((CommonCompositorBase)node);
    }
    
    /**
     * Visit complex type modification (<b>complexContent</b> or <b>simpleContent</b>)element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(CommonComplexModification node) {
        return visit((CommonTypeDerivation)node);
    }
    
    /**
     * Visit content element (<b>complexContent</b> or <b>simpleContent</b>).
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(CommonContentBase node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit type definition element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(CommonTypeDefinition node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit type derivation element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(CommonTypeDerivation node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit facet element.
     *
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(FacetElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit identity constraint element.
     *
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(KeyBase node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit schema location element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SchemaLocationBase node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit schema location required element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SchemaLocationRequiredBase node) {
        return visit((SchemaLocationBase)node);
    }
    
    /**
     * Visit selection element.
     *
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SelectionBase node) {
        return visit((AnnotatedBase)node);
    }
    
    //
    // Visit methods for concrete classes
    
    /**
     * Visit <b>all</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AllElement node) {
        return visit((CommonCompositorDefinition)node);
    }
    
    /**
     * Visit <b>annotation</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AnnotationElement node) {
        return visit((OpenAttrBase)node);
    }
    
    /**
     * Visit <b>any</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AnyElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>anyAttribute</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AnyAttributeElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>appinfo</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AppInfoElement node) {
        return visit((AnnotationItem)node);
    }
    
    /**
     * Visit <b>attribute</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AttributeElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>attributeGroup</b> element for definition.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AttributeGroupElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>attributeGroup</b> element for reference.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(AttributeGroupRefElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>choice</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(ChoiceElement node) {
        return visit((CommonCompositorDefinition)node);
    }
    
    /**
     * Visit <b>complexContent</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(ComplexContentElement node) {
        return visit((CommonContentBase)node);
    }
    
    /**
     * Visit <b>extension</b> element used for complex type.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(ComplexExtensionElement node) {
        return visit((CommonComplexModification)node);
    }
    
    /**
     * Visit <b>restriction</b> element used for complex type.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(ComplexRestrictionElement node) {
        return visit((CommonComplexModification)node);
    }
    
    /**
     * Visit <b>complexType</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(ComplexTypeElement node) {
        return visit((CommonTypeDefinition)node);
    }
    
    /**
     * Visit <b>documentation</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(DocumentationElement node) {
        return visit((AnnotationItem)node);
    }
    
    /**
     * Visit <b>element</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(ElementElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>field</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SelectionBase.FieldElement node) {
        return visit((SelectionBase)node);
    }
    
    /**
     * Visit <b>group</b> element for definition.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(GroupElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>group</b> element for reference.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(GroupRefElement node) {
        return visit((CommonCompositorBase)node);
    }
    
    /**
     * Visit <b>import</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(ImportElement node) {
        return visit((SchemaLocationBase)node);
    }
    
    /**
     * Visit <b>include</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(IncludeElement node) {
        return visit((SchemaLocationRequiredBase)node);
    }
    
    /**
     * Visit <b>key</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(KeyBase.KeyElement node) {
        return visit((KeyBase)node);
    }
    
    /**
     * Visit <b>keyref</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(KeyBase.KeyrefElement node) {
        return visit((KeyBase)node);
    }
    
    /**
     * Visit <b>list</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(ListElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>redefine</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(RedefineElement node) {
        return visit((SchemaLocationRequiredBase)node);
    }
    
    /**
     * Visit <b>schema</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SchemaElement node) {
        return visit((OpenAttrBase)node);
    }
    
    /**
     * Visit <b>sequence</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SequenceElement node) {
        return visit((CommonCompositorDefinition)node);
    }
    
    /**
     * Visit <b>simpleContent</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SimpleContentElement node) {
        return visit((CommonContentBase)node);
    }
    
    /**
     * Visit <b>extension</b> element for simple type.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SimpleExtensionElement node) {
        return visit((CommonTypeDerivation)node);
    }
    
    /**
     * Visit <b>restriction</b> element for simple type.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SimpleRestrictionElement node) {
        return visit((CommonTypeDerivation)node);
    }
    
    /**
     * Visit <b>selection</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SelectionBase.SelectorElement node) {
        return visit((SelectionBase)node);
    }
    
    /**
     * Visit <b>simpleType</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(SimpleTypeElement node) {
        return visit((CommonTypeDefinition)node);
    }
    
    /**
     * Visit <b>union</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(UnionElement node) {
        return visit((AnnotatedBase)node);
    }
    
    /**
     * Visit <b>unique</b> element.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code> if not
     */
    public boolean visit(KeyBase.UniqueElement node) {
        return visit((KeyBase)node);
    }
    
    //
    // Exit methods for base classes
    
    /**
     * Exit element.
     * 
     * @param node element being exited
     */
    public void exit(SchemaBase node) {}
    
    /**
     * Exit open attribute element.
     * 
     * @param node element being exited
     */
    public void exit(OpenAttrBase node) {
        exit((SchemaBase)node);
    }
    
    /**
     * Exit annotated element.
     * 
     * @param node element being exited
     */
    public void exit(AnnotatedBase node) {
        exit((OpenAttrBase)node);
    }
    
    //
    // Exit methods for shared implementation classes
    
    /**
     * Exit annotation item element.
     * 
     * @param node element being exited
     */
    public void exit(AnnotationItem node) {
        exit((SchemaBase)node);
    }
    
    /**
     * Exit complex type modification.
     * 
     * @param node element being exited
     */
    public void exit(CommonComplexModification node) {
        exit((CommonTypeDerivation)node);
    }
    
    /**
     * Exit compositor base element.
     * 
     * @param node element being exited
     */
    public void exit(CommonCompositorBase node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit compositor element.
     * 
     * @param node element being exited
     */
    public void exit(CommonCompositorDefinition node) {
        exit((CommonCompositorBase)node);
    }
    
    /**
     * Exit content element.
     * 
     * @param node element being exited
     */
    public void exit(CommonContentBase node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit type definition element.
     * 
     * @param node element being exited
     */
    public void exit(CommonTypeDefinition node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit common type derivation.
     * 
     * @param node element being exited
     */
    public void exit(CommonTypeDerivation node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit facet element.
     *
     * @param node element being exited
     */
    public void exit(FacetElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit identity constraint element.
     *
     * @param node element being exited
     */
    public void exit(KeyBase node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit schema location element.
     * 
     * @param node element being exited
     */
    public void exit(SchemaLocationBase node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit schema location element.
     * 
     * @param node element being exited
     */
    public void exit(SchemaLocationRequiredBase node) {
        exit((SchemaLocationBase)node);
    }
    
    /**
     * Exit selection element.
     *
     * @param node element being exited
     */
    public void exit(SelectionBase node) {
        exit((AnnotatedBase)node);
    }
    
    //
    // Exit methods for concrete classes
    
    /**
     * Exit <b>all</b> element.
     * 
     * @param node element being exited
     */
    public void exit(AllElement node) {
        exit((CommonCompositorDefinition)node);
    }
    
    /**
     * Exit <b>annotation</b> element.
     * 
     * @param node element being exited
     */
    public void exit(AnnotationElement node) {
        exit((OpenAttrBase)node);
    }
    
    /**
     * Exit <b>any</b> element.
     * 
     * @param node element being exited
     */
    public void exit(AnyElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>appinfo</b> element.
     * 
     * @param node element being exited
     */
    public void exit(AppInfoElement node) {
        exit((AnnotationItem)node);
    }
    
    /**
     * Exit <b>documentation</b> element.
     * 
     * @param node element being exited
     */
    public void exit(DocumentationElement node) {
        exit((AnnotationItem)node);
    }
    
    /**
     * Exit <b>anyAttribute</b> element.
     * 
     * @param node element being exited
     */
    public void exit(AnyAttributeElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>attribute</b> element.
     * 
     * @param node element being exited
     */
    public void exit(AttributeElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>attributeGroup</b> element for definition.
     * 
     * @param node element being exited
     */
    public void exit(AttributeGroupElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>attributeGroup</b> element for reference.
     * 
     * @param node element being exited
     */
    public void exit(AttributeGroupRefElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>choice</b> element.
     * 
     * @param node element being exited
     */
    public void exit(ChoiceElement node) {
        exit((CommonCompositorDefinition)node);
    }
    
    /**
     * Exit <b>complexContent</b> element.
     * 
     * @param node element being exited
     */
    public void exit(ComplexContentElement node) {
        exit((CommonContentBase)node);
    }
    
    /**
     * Exit <b>extension</b> element used for complex type.
     * 
     * @param node element being exited
     */
    public void exit(ComplexExtensionElement node) {
        exit((CommonComplexModification)node);
    }
    
    /**
     * Exit <b>restriction</b> element used for complex type.
     * 
     * @param node element being exited
     */
    public void exit(ComplexRestrictionElement node) {
        exit((CommonComplexModification)node);
    }
    
    /**
     * Exit <b>complexType</b> element.
     * 
     * @param node element being exited
     */
    public void exit(ComplexTypeElement node) {
        exit((CommonTypeDefinition)node);
    }
    
    /**
     * Exit <b>element</b> element.
     * 
     * @param node element being exited
     */
    public void exit(ElementElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>field</b> element.
     *
     * @param node element being exited
     */
    public void exit(SelectionBase.FieldElement node) {
        exit((SelectionBase)node);
    }
    
    /**
     * Exit <b>group</b> element for definition.
     * 
     * @param node element being exited
     */
    public void exit(GroupElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>group</b> element for reference.
     * 
     * @param node element being exited
     */
    public void exit(GroupRefElement node) {
        exit((CommonCompositorBase)node);
    }
    
    /**
     * Exit <b>import</b> element.
     * 
     * @param node element being exited
     */
    public void exit(ImportElement node) {
        exit((SchemaLocationBase)node);
    }
    
    /**
     * Exit <b>include</b> element.
     * 
     * @param node element being exited
     */
    public void exit(IncludeElement node) {
        exit((SchemaLocationRequiredBase)node);
    }
    
    /**
     * Exit <b>key</b> element.
     *
     * @param node element being exited
     */
    public void exit(KeyBase.KeyElement node) {
        exit((KeyBase)node);
    }
    
    /**
     * Exit <b>keyref</b> element.
     *
     * @param node element being exited
     */
    public void exit(KeyBase.KeyrefElement node) {
        exit((KeyBase)node);
    }
    
    /**
     * Exit <b>list</b> element.
     * 
     * @param node element being exited
     */
    public void exit(ListElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>redefine</b> element.
     * 
     * @param node element being exited
     */
    public void exit(RedefineElement node) {
        exit((SchemaLocationRequiredBase)node);
    }
    
    /**
     * Exit <b>schema</b> element.
     * 
     * @param node element being exited
     */
    public void exit(SchemaElement node) {
        exit((OpenAttrBase)node);
    }
    
    /**
     * Exit <b>sequence</b> element.
     * 
     * @param node element being exited
     */
    public void exit(SequenceElement node) {
        exit((CommonCompositorDefinition)node);
    }
    
    /**
     * Exit <b>simpleContent</b> element.
     * 
     * @param node element being exited
     */
    public void exit(SimpleContentElement node) {
        exit((CommonContentBase)node);
    }
    
    /**
     * Exit <b>extension</b> element for simple type.
     * 
     * @param node element being exited
     */
    public void exit(SimpleExtensionElement node) {
        exit((CommonTypeDerivation)node);
    }
    
    /**
     * Exit <b>restriction</b> element for simple type.
     * 
     * @param node element being exited
     */
    public void exit(SimpleRestrictionElement node) {
        exit((CommonTypeDerivation)node);
    }
    
    /**
     * Exit <b>selector</b> element.
     *
     * @param node element being exited
     */
    public void exit(SelectionBase.SelectorElement node) {
        exit((SelectionBase)node);
    }
    
    /**
     * Exit <b>simpleType</b> element.
     * 
     * @param node element being exited
     */
    public void exit(SimpleTypeElement node) {
        exit((CommonTypeDefinition)node);
    }
    
    /**
     * Exit <b>union</b> element.
     * 
     * @param node element being exited
     */
    public void exit(UnionElement node) {
        exit((AnnotatedBase)node);
    }
    
    /**
     * Exit <b>unique</b> element.
     *
     * @param node element being exited
     */
    public void exit(KeyBase.UniqueElement node) {
        exit((KeyBase)node);
    }
}
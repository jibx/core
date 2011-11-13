/*
Copyright (c) 2004-2008, Dennis M. Sosnoski.
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

package org.jibx.binding.model;

/**
 * Binding model visitor base class. This works with the {@link
 * org.jibx.binding.model.TreeContext} class for handling tree-based
 * operations on the binding definition. Subclasses can override any or all of
 * the base class visit and exit methods, including both those for abstract base
 * classes and those for concrete classes, but should normally call the base
 * class implementation of the method in order to implement the class
 * inheritance hierarchy handling. Elements in the binding definition are always
 * visited in tree order (down and across).
 *
 * @author Dennis M. Sosnoski
 */
public abstract class ModelVisitor
{
    //
    // Visit methods for base classes
    
    /**
     * Visit element. This method will be called for every element in the model.
     * 
     * @param node element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(ElementBase node) {
        return true;
    }
    
    /**
     * Visit nesting element. This method will be called for any form of nesting
     * element.
     * 
     * @param node nesting element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(NestingElementBase node) {
        return visit((ElementBase)node);
    }
    
    /**
     * Visit container element. This method will be called for any form of
     * container element.
     * 
     * @param node container element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(ContainerElementBase node) {
        return visit((NestingElementBase)node);
    }
    
    /**
     * Visit structure element. This method will be called for any form of
     * structure element.
     * 
     * @param node structure element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(StructureElementBase node) {
        return visit((ContainerElementBase)node);
    }
    
    /**
     * Visit template element. This method will be called for any form of
     * template element.
     * 
     * @param node template element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(TemplateElementBase node) {
        return visit((ContainerElementBase)node);
    }
    
    /**
     * Visit mapping element.
     * 
     * @param node mapping element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(MappingElementBase node) {
        return visit((TemplateElementBase)node);
    }
    
    //
    // Visit methods for concrete classes
    
    /**
     * Visit <b>binding</b> element.
     * 
     * @param node binding element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(BindingElement node) {
        return visit((NestingElementBase)node);
    }
    
    /**
     * Visit <b>collection</b> element.
     * 
     * @param node collection element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(CollectionElement node) {
        return visit((StructureElementBase)node);
    }
    
    /**
     * Visit <b>format</b> element.
     * 
     * @param node format element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(FormatElement node) {
        return visit((ElementBase)node);
    }
    
    /**
     * Visit <b>include</b> element.
     * 
     * @param node include element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(IncludeElement node) {
        return visit((ElementBase)node);
    }
    
    /**
     * Visit <b>input</b> element.
     * 
     * @param node input element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(InputElement node) {
        return visit((NestingElementBase)node);
    }
    
    /**
     * Visit <b>mapping</b> element in normal binding.
     * 
     * @param node mapping element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(MappingElement node) {
        return visit((MappingElementBase)node);
    }
    
    /**
     * Visit <b>namespace</b> element.
     * 
     * @param node namespace element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(NamespaceElement node) {
        return visit((ElementBase)node);
    }
    
    /**
     * Visit <b>output</b> element.
     * 
     * @param node output element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(OutputElement node) {
        return visit((NestingElementBase)node);
    }
    
    /**
     * Visit <b>mapping</b> element in precompiled binding.
     * 
     * @param node mapping element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(PrecompiledMappingElement node) {
        return visit((MappingElementBase)node);
    }
    
    /**
     * Visit <b>split</b> element.
     * 
     * @param node split element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(SplitElement node) {
        return visit((NestingElementBase)node);
    }
    
    /**
     * Visit <b>structure</b> element.
     * 
     * @param node structure element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(StructureElement node) {
        return visit((StructureElementBase)node);
    }
    
    /**
     * Visit <b>template</b> element.
     * 
     * @param node template element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(TemplateElement node) {
        return visit((TemplateElementBase)node);
    }
    
    /**
     * Visit <b>value</b> element.
     * 
     * @param node value element being visited
     * @return <code>true</code> if children to be processed, <code>false</code>
     * if not
     */
    public boolean visit(ValueElement node) {
        return visit((ElementBase)node);
    }
    
    //
    // Exit methods for base classes
    
    /**
     * Exit any element.
     * 
     * @param node element being exited
     */
    public void exit(ElementBase node) {}
    
    /**
     * Exit any nesting element.
     * 
     * @param node nesting element being exited
     */
    public void exit(NestingElementBase node) {
        exit((ElementBase)node);
    }
    
    /**
     * Exit any container element.
     * 
     * @param node container element being exited
     */
    public void exit(ContainerElementBase node) {
        exit((NestingElementBase)node);
    }
    
    /**
     * Exit any structure element.
     * 
     * @param node structure element being exited
     */
    public void exit(StructureElementBase node) {
        exit((ContainerElementBase)node);
    }
    
    /**
     * Exit any template element.
     * 
     * @param node template element being exited
     */
    public void exit(TemplateElementBase node) {
        exit((NestingElementBase)node);
    }
    
    /**
     * Exit any mapping element.
     * 
     * @param node template element being exited
     */
    public void exit(MappingElementBase node) {
        exit((TemplateElementBase)node);
    }
    
    //
    // Exit methods for concrete classes
    
    /**
     * Exit <b>binding</b> element.
     * 
     * @param node binding element being exited
     */
    public void exit(BindingElement node) {
        exit((NestingElementBase)node);
    }
    
    /**
     * Exit <b>collection</b> element.
     * 
     * @param node collection element being exited
     */
    public void exit(CollectionElement node) {
        exit((StructureElementBase)node);
    }
    
    /**
     * Exit <b>include</b> element.
     * 
     * @param node input element being exited
     */
    public void exit(IncludeElement node) {
        exit((ElementBase)node);
    }
    
    /**
     * Exit <b>input</b> element.
     * 
     * @param node input element being exited
     */
    public void exit(InputElement node) {
        exit((NestingElementBase)node);
    }
    
    /**
     * Exit <b>mapping</b> element in normal binding.
     * 
     * @param node mapping element being exited
     */
    public void exit(MappingElement node) {
        exit((MappingElementBase)node);
    }
    
    /**
     * Exit <b>output</b> element.
     * 
     * @param node output element being exited
     */
    public void exit(OutputElement node) {
        exit((NestingElementBase)node);
    }
    
    /**
     * Exit <b>mapping</b> element in precompiled binding.
     * 
     * @param node mapping element being exited
     */
    public void exit(PrecompiledMappingElement node) {
        exit((MappingElementBase)node);
    }
    
    /**
     * Exit <b>split</b> element.
     * 
     * @param node split element being exited
     */
    public void exit(SplitElement node) {
        exit((NestingElementBase)node);
    }
    
    /**
     * Exit <b>structure</b> element.
     * 
     * @param node structure element being exited
     */
    public void exit(StructureElement node) {
        exit((StructureElementBase)node);
    }
    
    /**
     * Exit <b>template</b> element.
     * 
     * @param node template element being exited
     */
    public void exit(TemplateElement node) {
        exit((TemplateElementBase)node);
    }
    
    /**
     * Exit <b>value</b> element.
     * 
     * @param node value element being exited
     */
    public void exit(ValueElement node) {
        exit((ElementBase)node);
    }
}
/*
Copyright (c) 2004-2011, Dennis M. Sosnoski. All rights reserved.

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

import java.util.ArrayList;

/**
 * Model visitor for handling item registration. This works with the {@link
 * org.jibx.binding.model.ValidationContext} class to handle registration of
 * items which can be referenced by name or by function (such as ID values
 * within an object structure). The only items of this type which are not
 * handled by this visitor are <b>format</b> definitions. The formats need to be
 * accessed during prevalidation, so they're registered during that pass.
 *
 * @author Dennis M. Sosnoski
 */
public class RegistrationVisitor extends ModelVisitor
{
    /** Validation context running this visitor. */
    private final ValidationContext m_context;
    
    /**
     * Constructor.
     * 
     * @param vctx validation context that will run this visitor
     */
    public RegistrationVisitor(ValidationContext vctx) {
        m_context = vctx;
    }
    
    /**
     * Visit binding model tree to handle registration.
     * 
     * @param root node of tree to be visited
     */
    public void visitTree(ElementBase root) {
        
        // first add all namespace declarations to contexts
        m_context.tourTree(root, new ModelVisitor() {

            /**
             * Merge namespaces from containing binding into included binding.
             * 
             * @param node
             * @return continue expansion flag
             */
            public boolean visit(IncludeElement node) {
                BindingElement binding = node.getBinding();
                if (binding != null && !node.isPrecompiled()) {
                    BindingElement contain =
                        (BindingElement)m_context.getParentElement();
                    DefinitionContext defs = binding.getDefinitions();
                    contain.getDefinitions().injectNamespaces(defs);
                }
                return super.visit(node);
            }
            
            /**
             * Add namespace definition to containing context.
             *
             * @param node
             * @return continue expansion flag
             */
            public boolean visit(NamespaceElement node) {
                ValidationProblem problem =
                    m_context.getCurrentDefinitions().addNamespace(node);
                if (problem != null) {
                    m_context.addProblem(problem);
                }
                return super.visit(node);
            }

            /**
             * Block expansion once a structure-type element is reached.
             *
             * @param node
             * @return <code>false</code>
             */
            public boolean visit(StructureElementBase node) {
                return false;
            }
            
        });
        
        // next handle adding references to table
        m_context.tourTree(root, this);
        
        // then handle mapping extension linkages with separate pass
        m_context.tourTree(root, new ModelVisitor() {
            
            // expand mapping elements in case child mappings are present
            public boolean visit(MappingElement node) {
                node.validateExtension(m_context);
                return true;
            }
            
            // don't bother expanding structure elements
            public boolean visit(StructureElementBase node) {
                return false;
            }
            
        });
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ModelVisitor#visit(org.jibx.binding.model.ContainerElementBase)
     */
    public boolean visit(ContainerElementBase node) {
        if (node.getLabel() != null) {
            ValidationProblem problem = m_context.getBindingRoot().
                getDefinitions().addNamedStructure(node);
            if (problem != null) {
                m_context.addProblem(problem);
            }
        }
        return super.visit(node);
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ModelVisitor#visit(org.jibx.binding.model.TemplateElementBase)
     */
    public boolean visit(TemplateElementBase node) {
        
        // check for a top-level definition
        if (m_context.getParentElement() instanceof BindingElement) {
            
            // add all namespace declarations from binding to template
            DefinitionContext pctx =
                m_context.getParentElement().getDefinitions();
            if (pctx != null) {
                ArrayList nss = pctx.getNamespaces();
                if (nss != null) {
                    DefinitionContext nctx = node.getDefinitions();
                    if (nctx == null) {
                        nctx = new DefinitionContext(pctx);
                        node.setDefinitions(nctx);
                    }
                    for (int i = 0; i < nss.size(); i++) {
                        nctx.addNamespace((NamespaceElement)nss.get(i));
                    }
                }
            }
        }
        return super.visit(node);
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ModelVisitor#visit(org.jibx.binding.model.MappingElementBase)
     */
    public boolean visit(MappingElementBase node) {
        DefinitionContext dctx = m_context.getCurrentDefinitions();
        dctx.addTemplate(node, m_context);
        if (!node.isAbstract()) {
            
            // force name validation to set the namespace information
            NameAttributes name = node.getNameAttributes();
            name.validate(m_context);
            if (name != null && name.getName() != null) {
                dctx.addMappedName(name, node, m_context);
            }
        }
        return super.visit(node);
    }
}
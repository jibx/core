/*
Copyright (c) 2005-2008, Dennis M. Sosnoski
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
 * Visitor for child tree of structure with an element definition. This
 * verifies that text and CDATA components are only used in ways consistent
 * with parsing (i.e., each use must be preceded by a required element).
 */
class SequenceVisitor extends ModelVisitor
{
    private final StructureElementBase m_baseStructure;
    private final ValidationContext m_validationContext;
    private boolean m_isTextAllowed;
    
    /**
     * Constructor.
     * 
     * @param base root of subtree being visited (<code>null</code> if not
     * a structure)
     * @param vctx validation context used for reporting errors
     */
    public SequenceVisitor(StructureElementBase base,
        ValidationContext vctx) {
        m_baseStructure = base;
        m_validationContext = vctx;
        m_isTextAllowed = true;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ModelVisitor#visit(org.jibx.binding.model.StructureElementBase)
     */
    public boolean visit(StructureElementBase node) {
        if (node != m_baseStructure && node.hasName()) {
            m_isTextAllowed = !node.isOptional();
            return false;
        } else {
            return true;
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ModelVisitor#visit(org.jibx.binding.model.ValueElement)
     */
    public boolean visit(ValueElement node) {
        switch (node.getStyle()) {
            case ValueElement.CDATA_STYLE:
            case ValueElement.TEXT_STYLE:
                if (m_isTextAllowed) {
                    m_isTextAllowed = false;
                } else {
                    m_validationContext.addError
                        ("Text value must be preceded by required element",
                        node);
                }
                break;
                
            case NestingAttributes.ELEMENT_STYLE:
                m_isTextAllowed = !node.isOptional();
                break;
            
            case NestingAttributes.ATTRIBUTE_STYLE:
                break;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ModelVisitor#exit(org.jibx.binding.model.StructureElementBase)
     */
    public void exit(StructureElementBase node) {
        if (node.hasName()) {
            m_isTextAllowed = !node.isOptional();
        }
    }
}
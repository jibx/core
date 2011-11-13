/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Abstract syntax tree new array expression builder. This adds convenience methods and control information to the base
 * builder.
 */
public class NewArrayBuilder extends ExpressionBuilderBase
{
    /** Array creation expression. */
    private final ArrayCreation m_arrayCreation;
    
    /**
     * Constructor.
     * 
     * @param source 
     * @param expr 
     */
    public NewArrayBuilder(ClassBuilder source, ArrayCreation expr) {
        super(source, expr);
        m_arrayCreation = expr;
    }
    
    /**
     * Add operand to expression. This just adds the supplied operand expression as a new initializer value.
     *
     * @param operand
     */
    protected void addOperand(Expression operand) {
        ArrayInitializer init = m_arrayCreation.getInitializer();
        if (init == null) {
            init = m_ast.newArrayInitializer();
            m_arrayCreation.setInitializer(init);
        }
        init.expressions().add(operand);
    }
    
    /**
     * Set the size of the array.
     * 
     * @param size
     */
    public void setSize(ExpressionBuilderBase size) {
        m_arrayCreation.dimensions().add(size.getExpression());
    }
}
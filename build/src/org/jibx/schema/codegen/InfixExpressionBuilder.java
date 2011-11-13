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

package org.jibx.schema.codegen;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

/**
 * Abstract syntax tree infix expression builder. This adds convenience methods and control information to the base
 * builder.
 */
public class InfixExpressionBuilder extends ExpressionBuilderBase
{
    /** Method invocation. */
    private final InfixExpression m_expression;
    
    /** Number of operands added to expression. */
    private int m_operandCount;
    
    /**
     * Constructor.
     * 
     * @param source 
     * @param expr 
     */
    public InfixExpressionBuilder(ClassBuilder source, InfixExpression expr) {
        super(source, expr);
        m_expression = expr;
    }
    
    /**
     * Constructor with left operand supplied.
     * 
     * @param source 
     * @param expr
     * @param operand
     */
    public InfixExpressionBuilder(ClassBuilder source, InfixExpression expr, Expression operand) {
        super(source, expr);
        m_expression = expr;
        addOperand(operand);
    }

    /**
     * Add operand to expression. If the right operand has not yet been set this will set it; otherwise, it will add the
     * operand as an extended operand of the expression. 
     *
     * @param operand
     */
    protected void addOperand(Expression operand) {
        if (m_operandCount == 0) {
            m_expression.setLeftOperand(operand);
        } else if (m_operandCount == 1) {
            m_expression.setRightOperand(operand);
        } else {
            m_expression.extendedOperands().add(operand);
        }
        m_operandCount++;
    }
}
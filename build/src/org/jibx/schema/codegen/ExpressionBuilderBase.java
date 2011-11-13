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

package org.jibx.schema.codegen;

import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Abstract syntax tree expression builder base. This is used for expressions with multiple component operands. It adds
 * convenience methods and control information to the base builder.
 */
public abstract class ExpressionBuilderBase extends ASTBuilderBase
{
    /** Source builder. */
    protected final ClassBuilder m_source;
    
    /** Expression under construction. */
    protected final Expression m_expression;
    
    /**
     * Constructor.
     * 
     * @param source 
     * @param expr
     */
    public ExpressionBuilderBase(ClassBuilder source, Expression expr) {
        super(source.getAST());
        m_source = source;
        m_expression = expr;
    }
    
    /**
     * Get expression. This is provided only for use by other classes in this package.
     *
     * @return expression
     */
    Expression getExpression() {
        return m_expression;
    }
    
    /**
     * Add operand to expression. This must be implemented by each subclass to handle adding another operand.
     *
     * @param operand
     */
    protected abstract void addOperand(Expression operand);
    
    /**
     * Add a local variable or field name operand to expression.
     *
     * @param name
     */
    public void addVariableOperand(String name) {
        addOperand(m_ast.newSimpleName(name));
    }
    
    /**
     * Add a string literal operand to expression.
     *
     * @param value
     */
    public void addStringLiteralOperand(String value) {
        StringLiteral strlit = m_ast.newStringLiteral();
        strlit.setLiteralValue(value);
        addOperand(strlit);
    }
    
    /**
     * Add a character literal operand to expression.
     *
     * @param value
     */
    public void addCharacterLiteralOperand(char value) {
        CharacterLiteral literal = m_ast.newCharacterLiteral();
        literal.setCharValue(value);
        addOperand(literal);
    }
    
    /**
     * Add a number literal operand to expression.
     *
     * @param value
     */
    public void addNumberLiteralOperand(String value) {
        addOperand(m_ast.newNumberLiteral(value));
    }
    
    /**
     * Add a <code>null</code> literal operand to expression.
     */
    public void addNullOperand() {
        addOperand(m_ast.newNullLiteral());
    }
    
    /**
     * Add another expression as an operand.
     *
     * @param builder expression builder
     */
    public void addOperand(ExpressionBuilderBase builder) {
        addOperand(builder.m_expression);
    }
}
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
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

/**
 * If statement builder. This wraps the AST if representation with convenience methods and added control information.
 * 
 * @author Dennis M. Sosnoski
 */
public class IfBuilder extends StatementBuilderBase
{
    /** Method invocation. */
    private final IfStatement m_if;
    
    /** "then" block of statement (automatically created). */
    private BlockBuilder m_thenBlock;
    
    /**
     * Constructor.
     * 
     * @param source 
     * @param expr expression 
     */
    public IfBuilder(ClassBuilder source, Expression expr) {
        super(source);
        m_if = source.getAST().newIfStatement();
        m_if.setExpression(expr);
        m_thenBlock = source.newBlock();
        m_if.setThenStatement(m_thenBlock.getStatement());
    }
    
    /**
     * Get the statement.
     *
     * @return statement
     */
    Statement getStatement() {
        return m_if;
    }
    
    /**
     * Get the "then" conditional block.
     *
     * @return block
     */
    public BlockBuilder getThen() {
        return m_thenBlock;
    }
    
    /**
     * Set the "else" conditional statement.
     *
     * @param stmt
     */
    public void setElse(StatementBuilderBase stmt) {
        m_if.setElseStatement(stmt.getStatement());
    }
}
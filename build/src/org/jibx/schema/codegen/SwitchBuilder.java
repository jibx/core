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

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

/**
 * Switch statement builder. This wraps the AST switch representation with convenience methods and added control
 * information.
 * 
 * @author Dennis M. Sosnoski
 */
public class SwitchBuilder extends StatementBuilderBase
{
    /** Method invocation. */
    private final SwitchStatement m_switch;
    
    /**
     * Constructor.
     * 
     * @param source 
     * @param expr expression 
     */
    public SwitchBuilder(ClassBuilder source, Expression expr) {
        super(source);
        m_switch = source.getAST().newSwitchStatement();
        m_switch.setExpression(expr);
    }
    
    /**
     * Get the statement.
     *
     * @return statement
     */
    Statement getStatement() {
        return m_switch;
    }
    
    /**
     * Check if a break statement is needed following the statement for a particular case.
     *
     * @param stmt
     * @return <code>true</code> if break needed, <code>false</code> if not
     */
    private static boolean isBreakNeeded(Statement stmt) {
        switch (stmt.getNodeType()) {
            
            case ASTNode.BLOCK:
            {
                List stmts = ((Block)stmt).statements();
                int size = stmts.size();
                if (size == 0) {
                    return true;
                } else {
                    return isBreakNeeded((Statement)stmts.get(size-1));
                }
            }
                
            case ASTNode.RETURN_STATEMENT:
            case ASTNode.THROW_STATEMENT:
                return false;
            
            default:
                return true;
        }
    }
    
    /**
     * Add case to switch statement with a named constant as the match value.
     *
     * @param name named constant
     * @param stmt statement to be executed
     */
    public void addNamedCase(String name, StatementBuilderBase stmt) {
        SwitchCase swcase = m_ast.newSwitchCase();
        swcase.setExpression(m_ast.newSimpleName(name));
        m_switch.statements().add(swcase);
        m_switch.statements().add(stmt.getStatement());
        if (isBreakNeeded(stmt.getStatement())) {
            m_switch.statements().add(m_ast.newBreakStatement());
        }
    }
    
    /**
     * Add case to switch statement with a number as the match value.
     *
     * @param value match value
     * @param stmt statement to be executed
     */
    public void addNumberCase(String value, StatementBuilderBase stmt) {
        SwitchCase swcase = m_ast.newSwitchCase();
        swcase.setExpression(numberLiteral(value));
        m_switch.statements().add(swcase);
        m_switch.statements().add(stmt.getStatement());
        if (isBreakNeeded(stmt.getStatement())) {
            m_switch.statements().add(m_ast.newBreakStatement());
        }
    }
    
    /**
     * Add default case to switch statement.
     *
     * @param stmt statement to be executed
     */
    public void addDefault(StatementBuilderBase stmt) {
        SwitchCase swcase = m_ast.newSwitchCase();
        m_switch.statements().add(swcase);
        m_switch.statements().add(stmt.getStatement());
        if (isBreakNeeded(stmt.getStatement())) {
            m_switch.statements().add(m_ast.newBreakStatement());
        }
    }
    
    /**
     * Add case to switch statement with new block for case code.
     *
     * @param expr
     * @return block
     */
    private BlockBuilder newCaseBlock(Expression expr) {
        SwitchCase swcase = m_ast.newSwitchCase();
        swcase.setExpression(expr);
        m_switch.statements().add(swcase);
        BlockBuilder block = m_source.newBlock();
        m_switch.statements().add(block.getStatement());
        return block;
    }
    
    /**
     * Add case to switch statement with returned block for code.
     *
     * @param name named constant
     * @return block
     */
    public BlockBuilder newNamedCase(String name) {
        return newCaseBlock(m_ast.newSimpleName(name));
    }
    
    /**
     * Add case to switch statement with returned block for code.
     *
     * @param value match value
     * @return block
     */
    public BlockBuilder newNumberCase(String value) {
        return newCaseBlock(numberLiteral(value));
    }
}
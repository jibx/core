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

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

/**
 * Block builder. This wraps the AST block representation with convenience methods and added control information.
 */
public class BlockBuilder extends StatementBuilderBase
{
    /** Compilation unit. */
    private final Block m_block;
    
    /**
     * Constructor.
     * 
     * @param source 
     * @param block
     */
    public BlockBuilder(ClassBuilder source, Block block) {
        super(source);
        m_block = block;
    }
    
    /**
     * Get the statement.
     *
     * @return statement
     */
    Statement getStatement() {
        return m_block;
    }

    /**
     * Append an assignment from an expression to a field or local variable.
     *
     * @param expr
     * @param name
     */
    public void addAssignToName(Expression expr, String name) {
        Assignment asgn = m_ast.newAssignment();
        asgn.setLeftHandSide(m_ast.newSimpleName(name));
        asgn.setRightHandSide(expr);
        m_block.statements().add(m_ast.newExpressionStatement(asgn));
    }
    
    /**
     * Append an assignment from a local variable to a field. This handles the case where the local variable name is the
     * same as the field name.
     *
     * @param vname
     * @param fname
     */
    public void addAssignVariableToField(String vname, String fname) {
        Assignment asgn = m_ast.newAssignment();
        if (fname.equals(vname)) {
            FieldAccess access = m_ast.newFieldAccess();
            access.setExpression(m_ast.newThisExpression());
            access.setName(m_ast.newSimpleName(fname));
            asgn.setLeftHandSide(access);
        } else {
            asgn.setLeftHandSide(m_ast.newSimpleName(fname));
        }
        asgn.setRightHandSide(m_ast.newSimpleName(vname));
        m_block.statements().add(m_ast.newExpressionStatement(asgn));
    }
    
    /**
     * Append a local variable declaration.
     *
     * @param type 
     * @param vname
     */
    public void addLocalVariableDeclaration(String type, String vname) {
        VariableDeclarationFragment vfrag = m_ast.newVariableDeclarationFragment();
        vfrag.setName(m_ast.newSimpleName(vname));
        VariableDeclarationStatement stmt = m_ast.newVariableDeclarationStatement(vfrag);
        stmt.setType(m_source.createType(type));
        m_block.statements().add(stmt);
    }
    
    /**
     * Append a local variable declaration with initializer expression. This variation takes the actual type as a
     * parameter.
     *
     * @param type 
     * @param vname
     * @param expr initializer expression
     */
    public void addLocalVariableDeclaration(Type type, String vname, ExpressionBuilderBase expr) {
        VariableDeclarationFragment vfrag = m_ast.newVariableDeclarationFragment();
        vfrag.setName(m_ast.newSimpleName(vname));
        vfrag.setInitializer(expr.getExpression());
        VariableDeclarationStatement stmt = m_ast.newVariableDeclarationStatement(vfrag);
        stmt.setType(type);
        m_block.statements().add(stmt);
    }
    
    /**
     * Append a local variable declaration with initializer expression. This variation takes the type name as a
     * parameter.
     *
     * @param tname 
     * @param vname
     * @param expr initializer expression
     */
    public void addLocalVariableDeclaration(String tname, String vname, ExpressionBuilderBase expr) {
        addLocalVariableDeclaration(m_source.createType(tname), vname, expr);
    }
    
    /**
     * Append a simple 'if' statement (no else).
     *
     * @param expr conditional expression
     * @param ifblock block executed when condition <code>true</code>
     */
    public void addIfStatement(ExpressionBuilderBase expr, BlockBuilder ifblock) {
        IfStatement ifstmt = m_ast.newIfStatement();
        ifstmt.setExpression(expr.getExpression());
        ifstmt.setThenStatement(ifblock.m_block);
        m_block.statements().add(ifstmt);
    }
    
    /**
     * Append an 'if-else' statement.
     *
     * @param expr conditional expression
     * @param ifblock block executed when condition <code>true</code>
     * @param elseblock block executed when condition <code>false</code>
     */
    public void addIfElseStatement(ExpressionBuilderBase expr, BlockBuilder ifblock, BlockBuilder elseblock) {
        IfStatement ifstmt = m_ast.newIfStatement();
        ifstmt.setExpression(expr.getExpression());
        ifstmt.setThenStatement(ifblock.m_block);
        ifstmt.setElseStatement(elseblock.m_block);
        m_block.statements().add(ifstmt);
    }
    
    /**
     * Append an 'if-else-if' statement.
     *
     * @param ifexpr if conditional expression
     * @param elsexpr if conditional expression
     * @param ifblock block executed when condition <code>true</code>
     * @param elseblock block executed when condition <code>false</code>
     */
    public void addIfElseIfStatement(ExpressionBuilderBase ifexpr, ExpressionBuilderBase elsexpr, BlockBuilder ifblock,
        BlockBuilder elseblock) {
        IfStatement ifstmt = m_ast.newIfStatement();
        ifstmt.setExpression(ifexpr.getExpression());
        ifstmt.setThenStatement(ifblock.m_block);
        IfStatement elseifstmt = m_ast.newIfStatement();
        elseifstmt.setExpression(elsexpr.getExpression());
        elseifstmt.setThenStatement(elseblock.m_block);
        ifstmt.setElseStatement(elseifstmt);
        m_block.statements().add(ifstmt);
    }
    
    /**
     * Append a three-part 'for' statement with an associated variable. This assumes the first part is a local variable
     * declaration with an initializer expression, while the other two parts are just expressions.
     *
     * @param name iteration variable name
     * @param type variable type
     * @param init variable initialization expression
     * @param test loop test expression (second part of 'for')
     * @param post post-loop expression (optional third part of 'for', <code>null</code> if none)
     * @param block statement body block
     */
    private void addForStatement(String name, Type type, Expression init, Expression test, Expression post,
        BlockBuilder block) {
        ForStatement stmt = m_ast.newForStatement();
        VariableDeclarationFragment declfrag = m_ast.newVariableDeclarationFragment();
        declfrag.setName(m_ast.newSimpleName(name));
        declfrag.setInitializer(init);
        VariableDeclarationExpression varexpr = m_ast.newVariableDeclarationExpression(declfrag);
        varexpr.setType(type);
        stmt.initializers().add(varexpr);
        stmt.setExpression(test);
        if (post != null) {
            stmt.updaters().add(post);
        }
        stmt.setBody(block.getStatement());
        m_block.statements().add(stmt);
    }
    
    /**
     * Append a standard 'for' statement using an iterator.
     *
     * @param name iteration variable name
     * @param type variable type (must be an iterator subclass or generic type)
     * @param init variable initialization expression
     * @param block statement body block
     */
    public void addIteratedForStatement(String name, Type type, ExpressionBuilderBase init, BlockBuilder block) {
        MethodInvocation methcall = m_ast.newMethodInvocation();
        methcall.setExpression(m_ast.newSimpleName(name));
        methcall.setName(m_ast.newSimpleName("hasNext"));
        addForStatement(name, type, init.getExpression(), methcall, null, block);
    }
    
    /**
     * Append a standard 'for' statement using an index variable over an array. The index is always initialized to '0',
     * and incremented each time the loop is executed until the size of the array is reached.
     *
     * @param name index variable name
     * @param array array name 
     * @param block statement body block
     */
    public void addIndexedForStatement(String name, String array, BlockBuilder block) {
        InfixExpression test = m_ast.newInfixExpression();
        test.setOperator(Operator.LESS);
        test.setLeftOperand(m_ast.newSimpleName(name));
        FieldAccess access = m_ast.newFieldAccess();
        access.setExpression(m_ast.newSimpleName(array));
        access.setName(m_ast.newSimpleName("length"));
        test.setRightOperand(access);
        PrefixExpression post = m_ast.newPrefixExpression();
        post.setOperator(PrefixExpression.Operator.INCREMENT);
        post.setOperand(m_ast.newSimpleName(name));
        addForStatement(name, m_ast.newPrimitiveType(PrimitiveType.INT), m_ast.newNumberLiteral("0"), test,
            post, block);
    }
    
    /**
     * Append a Java 5 "enhanced" 'for' statement.
     *
     * @param name iteration variable name
     * @param type iteration variable type
     * @param expr iteration source expression
     * @param block statement body block
     */
    public void addSugaredForStatement(String name, String type, ExpressionBuilderBase expr, BlockBuilder block) {
        EnhancedForStatement stmt = m_ast.newEnhancedForStatement();
        stmt.setExpression(expr.getExpression());
        SingleVariableDeclaration decl = m_ast.newSingleVariableDeclaration();
        decl.setName(m_ast.newSimpleName(name));
        decl.setType(m_source.createType(type));
        stmt.setParameter(decl);
        stmt.setBody(block.getStatement());
        m_block.statements().add(stmt);
    }
    
    /**
     * Append a statement returning the value of an expression.
     *
     * @param expr expression
     */
    public void addReturnExpression(ExpressionBuilderBase expr) {
        ReturnStatement ret = m_ast.newReturnStatement();
        ret.setExpression(expr.getExpression());
        m_block.statements().add(ret);
    }
    
    /**
     * Append a statement returning the value of a field or local variable.
     *
     * @param name field name
     */
    public void addReturnNamed(String name) {
        ReturnStatement ret = m_ast.newReturnStatement();
        ret.setExpression(m_ast.newSimpleName(name));
        m_block.statements().add(ret);
    }
    
    /**
     * Append a statement returning <code>null</code>.
     */
    public void addReturnNull() {
        ReturnStatement ret = m_ast.newReturnStatement();
        ret.setExpression(m_ast.newNullLiteral());
        m_block.statements().add(ret);
    }
    
    /**
     * Append a throw new exception statement.
     *
     * @param type exception type
     * @param text
     */
    public void addThrowException(String type, String text) {
        ThrowStatement thrwstmt = m_ast.newThrowStatement();
        ClassInstanceCreation exexpr = m_ast.newClassInstanceCreation();
        exexpr.setType(m_ast.newSimpleType(m_ast.newSimpleName(type)));
        exexpr.arguments().add(stringLiteral(text));
        thrwstmt.setExpression(exexpr);
        m_block.statements().add(thrwstmt);
    }
    
    /**
     * Append a throw new exception statement.
     *
     * @param type exception type
     * @param expr initializer expression
     */
    public void addThrowException(String type, ExpressionBuilderBase expr) {
        ThrowStatement thrwstmt = m_ast.newThrowStatement();
        ClassInstanceCreation exexpr = m_ast.newClassInstanceCreation();
        exexpr.setType(m_ast.newSimpleType(m_ast.newSimpleName(type)));
        exexpr.arguments().add(expr.getExpression());
        thrwstmt.setExpression(exexpr);
        m_block.statements().add(thrwstmt);
    }
    
    /**
     * Append a method call statement.
     *
     * @param call
     */
    public void addCall(InvocationBuilder call) {
        m_block.statements().add(m_ast.newExpressionStatement(call.getExpression()));
    }
    
    /**
     * Append a 'break' statement.
     */
    public void addBreak() {
        m_block.statements().add(m_ast.newBreakStatement());
    }
    
    /**
     * Append a 'switch' statement using a local variable or field name as the switch value.
     *
     * @param name
     * @return statement builder
     */
    public SwitchBuilder addSwitch(String name) {
        SwitchBuilder builder = new SwitchBuilder(m_source, m_ast.newSimpleName(name));
        m_block.statements().add(builder.getStatement());
        return builder;
    }
    
    /**
     * Append a 'switch' statement using a constructed expression as the switch value.
     *
     * @param expr
     * @return statement builder
     */
    public SwitchBuilder addSwitch(ExpressionBuilderBase expr) {
        SwitchBuilder builder = new SwitchBuilder(m_source, expr.getExpression());
        m_block.statements().add(builder.getStatement());
        return builder;
    }
    
    /**
     * Append an expression statement.
     *
     * @param expr
     */
    public void addExpressionStatement(ExpressionBuilderBase expr) {
        m_block.statements().add(m_ast.newExpressionStatement(expr.getExpression()));
    }
    
    /**
     * Append a constructed statement.
     *
     * @param stmt
     */
    public void addStatement(StatementBuilderBase stmt) {
        m_block.statements().add(stmt.getStatement());
    }
}
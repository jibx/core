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

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Abstract syntax tree builder base class. This wraps the AST with convenience methods.
 * 
 * @author Dennis M. Sosnoski
 */
public class ASTBuilderBase
{
    /** Actual AST instance. */
    protected final AST m_ast;
    
    /**
     * Constructor.
     * 
     * @param ast 
     */
    public ASTBuilderBase(AST ast) {
        m_ast = ast;
    }
    
    /**
     * Set the public access flag for a declaration.
     *
     * @param decl
     */
    public void setPublic(BodyDeclaration decl) {
        decl.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    }
    
    /**
     * Set the private access flag for a declaration.
     *
     * @param decl
     */
    public void setPrivate(BodyDeclaration decl) {
        decl.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    }
    
    /**
     * Set the static flag for a declaration.
     *
     * @param decl
     */
    public void setStatic(BodyDeclaration decl) {
        decl.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    }
    
    /**
     * Set the final flag for a declaration.
     *
     * @param decl
     */
    public void setFinal(BodyDeclaration decl) {
        decl.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    }

    /**
     * Set declaration as private final.
     *
     * @param decl
     */
    public void setPrivateFinal(BodyDeclaration decl) {
        setPrivate(decl);
        setFinal(decl);
    }

    /**
     * Set declaration as private static final.
     *
     * @param decl
     */
    public void setPrivateStaticFinal(BodyDeclaration decl) {
        setPrivate(decl);
        setStatic(decl);
        setFinal(decl);
    }

    /**
     * Set declaration as public static.
     *
     * @param decl
     */
    public void setPublicStatic(BodyDeclaration decl) {
        setPublic(decl);
        setStatic(decl);
    }

    /**
     * Set declaration as public static final.
     *
     * @param decl
     */
    public void setPublicStaticFinal(BodyDeclaration decl) {
        setPublic(decl);
        setStatic(decl);
        setFinal(decl);
    }
    
    /**
     * Create a string literal.
     *
     * @param value literal value
     * @return literal
     */
    public StringLiteral stringLiteral(String value) {
        StringLiteral literal = m_ast.newStringLiteral();
        literal.setLiteralValue(value);
        return literal;
    }
    
    /**
     * Create a number literal.
     * 
     * @param value literal value
     * @return literal
     */
    public NumberLiteral numberLiteral(String value) {
        return m_ast.newNumberLiteral(value);
    }
}
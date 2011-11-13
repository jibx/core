/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski. All rights reserved.
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
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;

/**
 * Abstract syntax tree body declaration builder base. This adds convenience methods and control information to the base
 * builder.
 */
public class BodyBuilderBase extends ASTBuilderBase
{
    /** Source builder. */
    protected final ClassBuilder m_source;
    
    /** Body declaration under construction. */
    protected final BodyDeclaration m_declaration;
    
    /**
     * Constructor.
     * 
     * @param source 
     * @param decl
     */
    public BodyBuilderBase(ClassBuilder source, BodyDeclaration decl) {
        super(source.getAST());
        m_source = source;
        m_declaration = decl;
    }
    
    /**
     * Set the public access flag.
     */
    public void setPublic() {
        m_declaration.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
    }
    
    /**
     * Set the private access flag.
     */
    public void setPrivate() {
        m_declaration.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
    }
    
    /**
     * Set the static flag.
     */
    public void setStatic() {
        m_declaration.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));
    }
    
    /**
     * Set the final flag.
     */
    public void setFinal() {
        m_declaration.modifiers().add(m_ast.newModifier(Modifier.ModifierKeyword.FINAL_KEYWORD));
    }

    /**
     * Set private final flags.
     */
    public void setPrivateFinal() {
        setPrivate();
        setFinal();
    }

    /**
     * Set private static final flags.
     */
    public void setPrivateStaticFinal() {
        setPrivate();
        setStatic();
        setFinal();
    }

    /**
     * Set public static flags.
     */
    public void setPublicStatic() {
        setPublic();
        setStatic();
    }

    /**
     * Set public static final flags.
     */
    public void setPublicStaticFinal() {
        setPublic();
        setStatic();
        setFinal();
    }
    
    /**
     * Add optionally tagged source comment for this body.
     *
     * @param name tag name (add comment without tag if <code>null</code>)
     * @param text comment text, <code>null</code> value ignored
     */
    public void addSourceComment(String name, String text) {
        if (text != null) {
            AST ast = m_source.getAST();
            TextElement element = ast.newTextElement();
            element.setText(text);
            TagElement tag = ast.newTagElement();
            tag.setTagName(name);
            tag.fragments().add(element);
            Javadoc javadoc = m_declaration.getJavadoc();
            if (javadoc == null) {
                javadoc = ast.newJavadoc();
                m_declaration.setJavadoc(javadoc);
            }
            javadoc.tags().add(tag);
        }
    }
    
    /**
     * Add untagged source comment for this body.
     *
     * @param text comment text, <code>null</code> value ignored
     */
    public void addSourceComment(String text) {
        addSourceComment(null, text);
    }
}
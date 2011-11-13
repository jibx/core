/*
 * Copyright (c) 2008, Dennis M. Sosnoski All rights reserved.
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

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Abstract syntax tree array cast expression builder. This adds convenience methods and control information to the base
 * builder.
 */
public class CastBuilder extends ExpressionBuilderBase
{
    /** Cast expression. */
    private final CastExpression m_cast;
    
    /** Flag for expression set. */
    private boolean m_set;
    
    /**
     * Constructor.
     * 
     * @param source 
     * @param expr 
     */
    public CastBuilder(ClassBuilder source, CastExpression expr) {
        super(source, expr);
        m_cast = expr;
    }
    
    /**
     * Add operand to expression. This just sets the supplied operand expression as the target, as long as the target
     * has not been set previously.
     *
     * @param operand
     */
    protected void addOperand(Expression operand) {
        if (m_set) {
            throw new IllegalStateException("Internal error: attempt to set cast expression more than once");
        } else {
            m_cast.setExpression(operand);
            m_set = true;
        }
    }
}
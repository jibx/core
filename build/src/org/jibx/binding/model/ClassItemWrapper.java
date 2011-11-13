/*
Copyright (c) 2004-2007, Dennis M. Sosnoski
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

import org.jibx.binding.classes.ClassItem;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;

/**
 * Wrapper for class field or method item information. This wraps the BCEL-based
 * class handling implementation to support the interface defined for use with
 * the binding model.
 *
 * @author Dennis M. Sosnoski
 */
public class ClassItemWrapper implements IClassItem
{
    private final IClass m_class;
    private final ClassItem m_item;
    
    /**
     * Constructor.
     * 
     * @param clas
     * @param item
     */
    protected ClassItemWrapper(IClass clas, ClassItem item) {
        m_class = clas;
        m_item = item;
    }
    
    /**
     * Get containing class information.
     *
     * @return class information
     */
    protected IClass getContainingClass() {
        return m_class;
    }
    
    /**
     * Get class item information.
     *
     * @return item information
     */
    protected ClassItem getClassItem() {
        return m_item;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getOwningClass()
     */
    public IClass getOwningClass() {
        return m_class;
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getName()
     */
    public String getName() {
        return m_item.getName();
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getJavaDoc()
     */
    public String getJavaDoc() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getType()
     */
    public String getTypeName() {
        return m_item.getTypeName();
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getReturnJavaDoc()
     */
    public String getReturnJavaDoc() {
        if (m_item.isMethod()) {
            return null;
        } else {
            throw new IllegalStateException("Internal error: not a method");
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getArgumentCount()
     */
    public int getArgumentCount() {
        return m_item.getArgumentCount();
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getArgumentType(int)
     */
    public String getArgumentType(int index) {
        return m_item.getArgumentTypes()[index];
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getParameterJavaDoc(int)
     */
    public String getParameterJavaDoc(int index) {
        if (m_item.isMethod()) {
            return null;
        } else {
            throw new IllegalStateException("Internal error: not a method");
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getParameterName(int)
     */
    public String getParameterName(int index) {
        return m_item.getParameterName(index);
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getAccessFlags()
     */
    public int getAccessFlags() {
        return m_item.getAccessFlags();
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getSignature()
     */
    public String getSignature() {
        return m_item.getSignature();
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#isMethod()
     */
    public boolean isMethod() {
        return m_item.isMethod();
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#isInitializer()
     */
    public boolean isInitializer() {
        return m_item.isInitializer();
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getExceptions()
     */
    public String[] getExceptions() {
        return m_item.getExceptions();
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getExceptionJavaDoc(int)
     */
    public String getExceptionJavaDoc(int index) {
        if (m_item.isMethod()) {
            return null;
        } else {
            throw new IllegalStateException("Internal error: not a method");
        }
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.IClassItem#getGenericsSignature()
     */
    public String getGenericsSignature() {
        return m_item.getGenericsSignature();
    }
}
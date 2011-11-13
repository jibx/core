/*
 * Copyright (c) 2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen.extend;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;

/**
 * Code generation decorator which changes the implementation class used for <code>java.util.List</code> instances.
 */
public class ListImplementationDecorator implements ClassDecorator
{
    /** List implementation class to be used. */
    private String m_listClass;
    
    /**
     * Set list class to be used.
     *
     * @param name
     */
    public void setListClass(String name) {
        m_listClass = name;
    }
    
    /**
     * Method called after completing code generation for the target class. Unused for this decorator.
     *
     * @param binding 
     * @param holder
     */
    public void finish(ElementBase binding, IClassHolder holder) {}
    
    /**
     * Method called before starting code generation for the target class. This just sets the list implementation class.
     *
     * @param holder
     */
    public void start(IClassHolder holder) {
        holder.setListImplementation(m_listClass);
    }
    
    /**
     * Method called after adding each data value to class. Unused for this decorator.
     * 
     * @param basename base name used for data value
     * @param collect repeated value flag
     * @param type value type (item value type, in the case of a repeated value)
     * @param field actual field
     * @param getmeth read access method
     * @param setmeth write access method
     * @param descript value description text
     * @param holder
     */
    public void valueAdded(String basename, boolean collect, String type, FieldDeclaration field,
        MethodDeclaration getmeth, MethodDeclaration setmeth, String descript, IClassHolder holder) {}
}
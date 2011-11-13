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
 * Interface implemented by class construction decorators used during code generation.
 * 
 * @author Dennis M. Sosnoski
 */
public interface ClassDecorator
{
    /**
     * Method called before starting code generation for the target class.
     *
     * @param holder
     */
    void start(IClassHolder holder);
    
    /**
     * Method called after adding each data value to class.
     * 
     * @param basename base name used for data value
     * @param collect repeated value flag
     * @param type value type (item value type, in the case of a repeated value)
     * @param field actual field
     * @param getmeth read access method (<code>null</code> if a flag value)
     * @param setmeth write access method (<code>null</code> if a flag value)
     * @param descript value description text
     * @param holder
     */
    void valueAdded(String basename, boolean collect, String type, FieldDeclaration field, MethodDeclaration getmeth,
        MethodDeclaration setmeth, String descript, IClassHolder holder);
    
    /**
     * Method called after completing code generation for the target class.
     *
     * @param binding binding definition element for class, a &lt;format> if the class is an enumeration, a &lt;mapping>
     * or &lt;structure> if it's a normal class 
     * @param holder
     */
    void finish(ElementBase binding, IClassHolder holder);
}
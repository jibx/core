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

package org.jibx.schema.codegen;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Interface for working with classes during code generation.
 * 
 * @author Dennis M. Sosnoski
 */
public interface IClassHolder
{
    /**
     * Get simple name.
     * 
     * @return name
     */
    String getName();

    /**
     * Get fully-qualified name.
     * 
     * @return name
     */
    String getFullName();
    
    /**
     * Get containing class of inner class.
     * 
     * @return outer containing class, or <code>null</code> if not an inner class
     */
    IClassHolder getOuterClass();
    
    /**
     * Check if superclass is forced by schema model.
     *
     * @return <code>true</code> if superclass forced, <code>false</code> if not
     */
    boolean isSuperClassForced();

    /**
     * Get name of base class to be extended.
     *
     * @return base (<code>null</code> if none)
     */
    String getSuperClassName();

    /**
     * Set name of base class to be extended. This method can only be used if {@link #isSuperClassForced()} returns
     * <code>false</code>.
     *
     * @param base fully-qualified class name of base class (<code>null</code> if none)
     */
    void setSuperClassName(String base);

    /**
     * Set name of list implementation class to be used for initializing instances.
     *
     * @param list fully-qualified class name of list implementation (non-<code>null</code>)
     */
    void setListImplementation(String list);

    /**
     * Add import for class. If the requested import doesn't conflict with the current set it's added, otherwise it's
     * ignored.
     * 
     * @param type fully qualified class name
     * @return <code>true</code> if added as import
     */
    boolean addImport(String type);

    /**
     * Get the name to be used for a type. If the type has been imported this returns the short form of the name;
     * otherwise it just returns the fully-qualified name.
     * 
     * @param type fully-qualified type name
     * @return name
     */
    String getTypeName(String type);
    
    /**
     * Get the interfaces implemented by this class.
     *
     * @return interface names
     */
    String[] getInterfaces();
    
    /**
     * Get the fields defined in this class.
     *
     * @return fields
     */
    FieldDeclaration[] getFields();
    
    /**
     * Get the methods defined in this class.
     *
     * @return methods
     */
    MethodDeclaration[] getMethods();

    /**
     * Add an interface to this class definition.
     *
     * @param interf interface type
     */
    void addInterface(String interf);
    
    /**
     * Add field declaration to class.
     *
     * @param field
     */
    void addField(FieldDeclaration field);
    
    /**
     * Add method declaration to class.
     *
     * @param method
     */
    void addMethod(MethodDeclaration method);
    
    /**
     * Add inner type declaration to class.
     *
     * @param type
     */
    void addType(TypeDeclaration type);
}
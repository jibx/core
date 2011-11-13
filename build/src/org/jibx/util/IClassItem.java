/*
Copyright (c) 2004-2005, Dennis M. Sosnoski
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

package org.jibx.util;

/**
 * Interface for field or method information. Provides the information needed
 * for access to the item.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public interface IClassItem
{
	/**
	 * Get owning class information.
	 *
	 * @return owning class information
	 */
	public IClass getOwningClass();
	
	/**
	 * Get item name.
	 *
	 * @return item name
	 */
	public String getName();
    
    /**
     * Get item JavaDoc description, if available.
     *
     * @return non-empty JavaDoc text (<code>null</code> if not available)
     */
    public String getJavaDoc();
	
	/**
	 * Get item type as fully qualified class name.
	 *
	 * @return item type name
	 */
	public String getTypeName();
    
    /**
     * Get return JavaDoc description for method, if available.
     *
     * @return non-empty JavaDoc text (<code>null</code> if not available)
     */
    public String getReturnJavaDoc();
	
	/**
	 * Get number of arguments for method.
	 *
	 * @return argument count for method, or <code>-1</code> if not a method
	 */
	public int getArgumentCount();
	
	/**
	 * Get argument type as fully qualified class name. This method will throw a
	 * runtime exception if called on a field.
	 *
	 * @param index argument number
	 * @return argument type name
	 */
	public String getArgumentType(int index);
    
    /**
     * Get method parameter name, if available. This method will throw a
     * runtime exception if called on a field.
     *
     * @param index parameter number
     * @return parameter name (<code>null</code> if not available)
     */
    public String getParameterName(int index);
    
    /**
     * Get method parameter JavaDoc description, if available. This method will
     * throw a runtime exception if called on a field.
     *
     * @param index parameter number
     * @return non-empty JavaDoc text (<code>null</code> if not available)
     */
    public String getParameterJavaDoc(int index);
	
	/**
	 * Get access flags.
	 *
	 * @return flags for access type of field or method
	 */
	public int getAccessFlags();
	
	/**
	 * Get field or method signature.
	 *
	 * @return encoded method signature
	 */
	public String getSignature();
	
	/**
	 * Check if item is a method.
	 *
	 * @return <code>true</code> if a method, <code>false</code> if a field
	 */
	public boolean isMethod();
    
    /**
     * Check if item is an initializer.
     *
     * @return <code>true</code> if an initializer, <code>false</code> if a
     * field or normal method
     */
    public boolean isInitializer();
	
	/**
	 * Get names of exceptions thrown by method.
	 *
	 * @return array of exceptions thrown by method, or <code>null</code> if
	 * a field
	 */
	public String[] getExceptions();
    
    /**
     * Get method throws JavaDoc description, if available. This method will
     * throw a runtime exception if called on a field.
     *
     * @param index exception index (into array returned by
     * {@link #getExceptions()}
     * @return non-empty JavaDoc text (<code>null</code> if not available)
     */
    public String getExceptionJavaDoc(int index);
    
    /**
     * Get the generics signature information for item.
     *
     * @return generics signature (<code>null</code> if none)
     */
    public String getGenericsSignature();
}
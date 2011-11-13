/*
Copyright (c) 2004-2009, Dennis M. Sosnoski.
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

import org.jibx.binding.classes.ClassFile;

/**
 * Interface for class file information. Provides access to class field and
 * method information.
 *
 * @author Dennis M. Sosnoski
 */
public interface IClass
{
    /**
     * Get class file information.
     * TODO: eliminate this sucker
     *
     * @return class file information
     */
    public ClassFile getClassFile();

	/**
	 * Get fully qualified class name.
	 *
	 * @return fully qualified name for class
	 */
	public String getName();

	/**
	 * Get signature for class as type.
	 *
	 * @return signature for class used as type
	 */
	public String getSignature();

	/**
	 * Get package name.
	 *
	 * @return package name for class
	 */
	public String getPackage();

	/**
	 * Get superclass.
	 *
	 * @return superclass information
	 */
	public IClass getSuperClass();

	/**
	 * Get names of all interfaces implemented directly by class.
	 *
	 * @return names of all interfaces implemented directly by class
	 * (non-<code>null</code>, empty array if none)
	 */
	public String[] getInterfaces();

	/**
	 * Get signatures for all types of which instances of this type are
	 * instances.
	 *
	 * @return all signatures supported by instances
	 */
	public String[] getInstanceSigs();

	/**
	 * Check if class implements an interface.
	 *
	 * @param sig signature of interface to be checked
	 * @return <code>true</code> if interface is implemented by class,
	 * <code>false</code> if not
	 */
	public boolean isImplements(String sig);

    /**
     * Check if class is abstract.
     *
     * @return <code>true</code> if class is abstract, <code>false</code> if not
     */
    public boolean isAbstract();

    /**
     * Check if class is an interface.
     *
     * @return <code>true</code> if class is an interface, <code>false</code> if
     * not
     */
    public boolean isInterface();

    /**
     * Check if class is modifiable.
     *
     * @return <code>true</code> if class is modifiable, <code>false</code> if
     * not
     */
    public boolean isModifiable();

	/**
	 * Check if another class is a superclass of this one.
	 *
	 * @param name potential superclass to be checked
	 * @return <code>true</code> if named class is a superclass of this one,
	 * <code>false</code> if not
	 */
	public boolean isSuperclass(String name);

	/**
	 * Get information for field. This only checks for fields that are actually
	 * members of the class (not superclasses).
     * TODO: make this work with both static and member fields
	 *
	 * @param name field name
	 * @return field information, or <code>null</code> if field not found
	 */
	public IClassItem getDirectField(String name);

	/**
	 * Get information for field. If the field is not found directly,
	 * superclasses are checked for inherited fields matching the supplied name.
     * TODO: make this work with both static and member fields
	 *
	 * @param name field name
	 * @return field information, or <code>null</code> if field not found
	 */
	public IClassItem getField(String name);

    /**
     * Get information for best matching method. This tries to find a method
     * which matches the specified name, return type, and argument types. If an
     * exact match is not found it looks for a method with a return type that
     * is extended or implemented by the specified type and arguments that are
     * extended or implemented by the specified types. If no match is found for
     * this class superclasses are checked.
     * TODO: make this work with both static and member methods
     *
     * @param name method name
     * @param type return value type name (<code>null</code> if indeterminant)
     * @param args argument value type names (<code>null</code> if
     * indeterminant)
     * @return method information, or <code>null</code> if method not found
     */
    public IClassItem getBestMethod(String name, String type, String[] args);

	/**
	 * Get information for method without respect to potential trailing
	 * arguments or return value. If the method is not found directly,
	 * superclasses are checked for inherited methods matching the supplied
	 * name. This compares the supplied partial signature against the actual
	 * method signature, and considers it a match if the actual sigature starts
	 * with the supplied signature.
     * TODO: make this work with both static and member methods
	 *
	 * @param name method name
	 * @param sig partial method signature to be matched
	 * @return method information, or <code>null</code> if method not found
	 */
	public IClassItem getMethod(String name, String sig);

	/**
	 * Get information for method matching one of several possible signatures.
	 * If a match is not found directly, superclasses are checked for inherited
	 * methods matching the supplied name and signatures. The signature
	 * variations are checked in the order supplied.
     * TODO: make this work with both static and member methods
	 *
	 * @param name method name
	 * @param sigs possible signatures for method (including return type)
	 * @return method information, or <code>null</code> if method not found
	 */
	public IClassItem getMethod(String name, String[] sigs);

	/**
	 * Get information for initializer. Only the class itself is checked for an
	 * initializer matching the argument list signature.
	 *
	 * @param sig encoded argument list signature
	 * @return method information, or <code>null</code> if method not found
	 */
	public IClassItem getInitializerMethod(String sig);

	/**
	 * Get information for static method without respect to return value. Only
	 * the class itself is checked for a method matching the supplied name and
	 * argument list signature.
	 *
	 * @param name method name
	 * @param sig encoded argument list signature
	 * @return method information, or <code>null</code> if method not found
	 */
	public IClassItem getStaticMethod(String name, String sig);

	/**
	 * Check accessible method. Check if a field or method in another class is
	 * accessible from within this class.
	 *
	 * @param item field or method information
	 * @return <code>true</code> if accessible, <code>false</code> if not
	 */
	public boolean isAccessible(IClassItem item);

    /**
     * Check if a value of this type can be directly assigned to another type.
     * This is basically the equivalent of the instanceof operator.
     *
     * @param other type to be assigned to
     * @return <code>true</code> if assignable, <code>false</code> if not
     */
    public boolean isAssignable(IClass other);

    /**
     * Load class in executable form.
     *
     * @return loaded class, or <code>null</code> if unable to load
     */
    public Class loadClass();

    /**
     * Get all methods of class.
     *
     * @return methods
     */
    public IClassItem[] getMethods();

    /**
     * Get all fields of class.
     *
     * @return fields
     */
    public IClassItem[] getFields();
    
    /**
     * Get the JavaDoc comment for this class.
     *
     * @return comment text, or <code>null</code> if none or no source available
     */
    public String getJavaDoc();
    
    /**
     * Get the locator which provided this class.
     *
     * @return locator
     */
    public IClassLocator getLocator();
}
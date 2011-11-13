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

/**
 * Locator for class information. Looks up classes using whatever method is
 * appropriate for the usage environment.
 *
 * @author Dennis M. Sosnoski
 */
public interface IClassLocator
{
    /**
     * Check if class lookup is supported. If this returns <code>false</code>,
     * lookup methods return only place holder class information.
     *
     * @return <code>true</code> if class lookup supported, <code>false</code>
     * if only place holder information returned
     */
    boolean isLookupSupported();
    
    /**
     * Get class information.
     *
     * @param name fully-qualified name of class to be found
     * @return class information, or <code>null</code> if class not found
     */
    IClass getClassInfo(String name);
    
    /**
     * Get required class information. This is just like {@link
     * #getClassInfo(String)}, but throws a runtime exception rather than
     * returning <code>null</code>.
     *
     * @param name fully-qualified name of class to be found
     * @return class information (non-<code>null</code>)
     */
    IClass getRequiredClassInfo(String name);
    
    /**
     * Load class.
     *
     * @param name fully-qualified class name
     * @return loaded class, or <code>null</code> if not found
     */
    Class loadClass(String name);
}
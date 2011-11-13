/*
Copyright (c) 2003-2008, Dennis M. Sosnoski.
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

package org.jibx.binding.classes;

import org.apache.bcel.classfile.Method;

/**
 * Information for an existing binding method.  It supplies hash code and
 * equality checking based on the method signature and actual byte code of the
 * generated method, ignoring the method name.
 *
 * @author Dennis M. Sosnoski
 */
public class ExistingMethod extends BindingMethod
{
    /** Class item information. */
    private ClassItem m_item;
    
    /** Actual method information. */
    private Method m_method;
    
    /** Accumulated hash code from adding instructions. */
    private int m_hashCode;
    
    /** Flag for method used in code. */
    private boolean m_used;

    /**
     * Constructor.
     *
     * @param method actual method information
     * @param item class item information for method
     * @param file class file information
     */
    public ExistingMethod(Method method, ClassItem item, ClassFile file) {
        super(file);
        m_item = item;
        m_method = method;
        m_hashCode = computeMethodHash(method);
//      System.out.println("Computed hash for existing method " +
//          m_classFile.getName() + '.' + method.getName() + " as " + m_hashCode);
    }

    /**
     * Get name of method.
     *
     * @return method name
     */
    public String getName() {
        return m_item.getName();
    }
    
    /**
     * Get signature.
     *
     * @return signature for method
     */
    public String getSignature() {
        return m_item.getSignature();
    }
    
    /**
     * Get access flags.
     *
     * @return flags for access type of method
     */
    public int getAccessFlags() {
        return m_item.getAccessFlags();
    }
    
    /**
     * Set access flags.
     *
     * @param flags access type to be set
     */
    public void setAccessFlags(int flags) {
        m_item.setAccessFlags(flags);
    }
    
    /**
     * Check method used status.
     *
     * @return method used status
     */
    public boolean isUsed() {
        return m_used;
    }
    
    /**
     * Set method used status.
     */
    public void setUsed() {
        m_used = true;
    }
    
    /**
     * Get the actual method.
     *
     * @return method information
     */
    public Method getMethod() {
        return m_method;
    }
    
    /**
     * Get the method item.
     *
     * @return method item information
     */
    public ClassItem getItem() {
        return m_item;
    }

    /**
     * Delete method from class.
     */
    public void delete() {
        getClassFile().removeMethod(m_method);
    }

    /**
     * Get hash code.
     *
     * @return hash code for this method
     */
    public int hashCode() {
        return m_hashCode;
    }
}
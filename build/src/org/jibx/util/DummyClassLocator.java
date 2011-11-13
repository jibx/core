/*
Copyright (c) 2009, Dennis M. Sosnoski.
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
 * Dummy class locator, used when no class information is available. This returns only place holder class information.
 *
 * @author Dennis M. Sosnoski
 */
public class DummyClassLocator implements IClassLocator
{
    /** Singleton instance of root class information. */
    private final IClass s_objectDummy = new DummyClassInfo("java.lang.Object", null);
    
    /**
     * Check if class lookup is supported. Always returns <code>false</code> to indicate that lookup methods return only
     * place holder class information.
     *
     * @return <code>false</code>
     */
    public boolean isLookupSupported() {
        return false;
    }
    
    /**
     * Get class information.
     *
     * @param name fully-qualified name of class to be found
     * @return class information, or <code>null</code> if class not found
     */
    public IClass getClassInfo(String name) {
        return new DummyClassInfo(name, s_objectDummy);
    }
    
    /**
     * Get required class information. This is just like {@link #getClassInfo(String)}, but throws a runtime exception
     * rather than returning <code>null</code>.
     *
     * @param name fully-qualified name of class to be found
     * @return class information (non-<code>null</code>)
     */
    public IClass getRequiredClassInfo(String name) {
        return new DummyClassInfo(name, s_objectDummy);
    }
    
    /**
     * Load class. This just loads and returns the class, if it's available on the classpath.
     *
     * @param name fully-qualified class name
     * @return loaded class, or <code>null</code> if not found
     */
    public Class loadClass(String name) {
        try {
            return DummyClassLocator.class.getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * (Very) partial implementation of class information interface, used as a place holder for classes.
     */
    private class DummyClassInfo implements IClass
    {
        private final String m_name;
        private final String m_package;
        private final String m_signature;
        private final IClass m_superclass;
        
        public DummyClassInfo(String name, IClass sclas) {
            m_name = name;
            int split = name.lastIndexOf('.');
            if (split < 0) {
                m_package = "";
            } else {
                m_package = name.substring(0, split);
            }
            m_signature = "L" + name.replace('.', '\\') + ';';
            m_superclass = sclas;
        }
        
        public IClassItem getBestMethod(String name, String type, String[] args) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public ClassFile getClassFile() {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public IClassItem getDirectField(String name) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public IClassItem getField(String name) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public IClassItem[] getFields() {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public IClassItem getInitializerMethod(String sig) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public String[] getInstanceSigs() {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public String[] getInterfaces() {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public String getJavaDoc() {
            return null;
        }

        public IClassLocator getLocator() {
            return DummyClassLocator.this;
        }

        public IClassItem getMethod(String name, String sig) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public IClassItem getMethod(String name, String[] sigs) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public IClassItem[] getMethods() {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public String getName() {
            return m_name;
        }

        public String getPackage() {
            return null;
        }

        public String getSignature() {
            return null;
        }

        public IClassItem getStaticMethod(String name, String sig) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public IClass getSuperClass() {
            return m_superclass;
        }

        public boolean isAbstract() {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public boolean isAccessible(IClassItem item) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public boolean isAssignable(IClass other) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public boolean isImplements(String sig) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public boolean isInterface() {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public boolean isModifiable() {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public boolean isSuperclass(String name) {
            throw new IllegalStateException("Internal error - class information not available");
        }

        public Class loadClass() {
            return DummyClassLocator.this.loadClass(m_name);
        }
    }
}
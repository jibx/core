/*
 * Copyright (c) 2007, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.custom.classes;

import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.ClassItem;
import org.jibx.binding.model.ClassWrapper;
import org.jibx.util.IClassItem;

import com.thoughtworks.qdox.model.JavaClass;

/**
 * Wrapper for class with added source information. This wraps the basic class handling implementation with added
 * support for retrieving information from source files.
 * 
 * @author Dennis M. Sosnoski
 */
public class ClassSourceWrapper extends ClassWrapper
{
    /**
     * Constructor.
     * 
     * @param loc
     * @param clas
     */
    public ClassSourceWrapper(IClassSourceLocator loc, ClassFile clas) {
        super(loc, clas);
    }
    
    /**
     * Build an item wrapper. This override of the base class implementation always creates a wrapper which will support
     * source operations.
     * 
     * @param item
     * @return wrapper
     */
    protected IClassItem buildItem(ClassItem item) {
        return new ClassItemSourceWrapper(this, item);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.model.IClass#getJavaDoc()
     */
    public String getJavaDoc() {
        IClassSourceLocator loc = (IClassSourceLocator)getLocator();
        JavaClass jc = loc.getSourceInfo(getClassFile().getName());
        if (jc != null) {
            String text = jc.getComment();
            if (text != null) {
                text = text.trim();
                if (text.length() > 0) {
                    return text;
                }
            }
        }
        return null;
    }
}
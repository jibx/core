/*
 * Copyright (c) 2004-2007, Dennis M. Sosnoski All rights reserved.
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

import org.jibx.binding.classes.ClassItem;
import org.jibx.binding.model.ClassItemWrapper;
import org.jibx.util.IClass;
import org.jibx.util.IClassLocator;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaParameter;
import com.thoughtworks.qdox.model.Member;
import com.thoughtworks.qdox.model.Type;

/**
 * Wrapper for class field or method item with added source information. This wraps the basic class handling
 * implementation with added support for retrieving information from source files.
 * 
 * @author Dennis M. Sosnoski
 */
public class ClassItemSourceWrapper extends ClassItemWrapper
{
    private boolean m_checkedSource;
    
    private Member m_itemSource;
    
    /**
     * Constructor
     * 
     * @param clas
     * @param item
     */
    /* package */ClassItemSourceWrapper(IClass clas, ClassItem item) {
        super(clas, item);
    }
    
    /**
     * Check for source method signature match.
     * 
     * @param method
     * @return <code>true</code> if match to this method, <code>false</code> if not
     */
    private boolean matchSignature(JavaMethod method) {
        boolean match = true;
        JavaParameter[] parms = method.getParameters();
        if (parms.length == getArgumentCount()) {
            for (int j = 0; j < parms.length; j++) {
                Type ptype = parms[j].getType();
                String type = ptype.getValue();
                int ndim = ptype.getDimensions();
                while (ndim-- > 0) {
                    type += "[]";
                }
                String comp = getArgumentType(j);
                if (!comp.equals(type)) {
                    if (type.indexOf('.') >= 0 || !comp.endsWith('.' + type)) {
                        match = false;
                        break;
                    }
                }
            }
        }
        return match;
    }
    
    /**
     * Internal method to get the source code information for this item.
     * 
     * @return source information
     */
    private Member getItemSource() {
        if (!m_checkedSource) {
            m_checkedSource = true;
            IClass clas = getContainingClass();
            IClassLocator loc = clas.getLocator();
            if (loc instanceof IClassSourceLocator) {
                IClassSourceLocator sloc = (IClassSourceLocator)loc;
                JavaClass jc = sloc.getSourceInfo(clas.getName());
                if (jc != null) {
                    if (isMethod()) {
                        String mname = getName();
                        JavaMethod[] methods = jc.getMethods();
                        for (int i = 0; i < methods.length; i++) {
                            JavaMethod method = methods[i];
                            if (mname.equals(method.getName())) {
                                if (matchSignature(method)) {
                                    m_itemSource = method;
                                    break;
                                }
                            }
                        }
                    } else {
                        m_itemSource = jc.getFieldByName(getName());
                    }
                }
            }
        }
        return m_itemSource;
    }
    
    /**
     * Return JavaDoc text only if non-empty.
     * 
     * @param text raw JavaDoc text
     * @return trimmed text if non-empty, otherwise <code>null</code>
     */
    private static String docText(String text) {
        if (text != null) {
            text = text.trim();
            if (text.length() > 0) {
                return text;
            }
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.model.IClassItem#getJavaDoc()
     */
    public String getJavaDoc() {
        Member src = getItemSource();
        if (src == null) {
            return null;
        } else if (isMethod()) {
            return docText(((JavaMethod)src).getComment());
        } else {
            return docText(((JavaField)src).getComment());
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.model.IClassItem#getReturnJavaDoc()
     */
    public String getReturnJavaDoc() {
        if (isMethod()) {
            JavaMethod jm = (JavaMethod)getItemSource();
            if (jm != null) {
                DocletTag tag = jm.getTagByName("return");
                if (tag != null) {
                    return docText(tag.getValue());
                }
            }
            return null;
        } else {
            throw new IllegalStateException("Internal error: not a method");
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.model.IClassItem#getParameterJavaDoc(int)
     */
    public String getParameterJavaDoc(int index) {
        if (isMethod()) {
            JavaMethod jm = (JavaMethod)getItemSource();
            if (jm != null) {
                String name = jm.getParameters()[index].getName();
                DocletTag[] tags = jm.getTagsByName("param");
                for (int i = 0; i < tags.length; i++) {
                    DocletTag tag = tags[i];
                    String[] parms = tag.getParameters();
                    if (parms != null && parms.length > 0 && name.equals(parms[0])) {
                        String text = tag.getValue().trim();
                        if (text.startsWith(name)) {
                            text = text.substring(name.length()).trim();
                        }
                        return docText(text);
                    }
                }
            }
            return null;
        } else {
            throw new IllegalStateException("Internal error: not a method");
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.model.IClassItem#getParameterName(int)
     */
    public String getParameterName(int index) {
        JavaMethod jm = (JavaMethod)getItemSource();
        String name;
        if (jm != null) {
            JavaParameter[] parameters = jm.getParameters();
            name = parameters[index].getName();
        } else {
            name = super.getParameterName(index);
        }
        return name;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.model.IClassItem#getExceptionJavaDoc(int)
     */
    public String getExceptionJavaDoc(int index) {
        if (isMethod()) {
            JavaMethod jm = (JavaMethod)getItemSource();
            if (jm != null) {
                String name = getExceptions()[index];
                DocletTag[] tags = jm.getTagsByName("throws");
                for (int i = 0; i < tags.length; i++) {
                    DocletTag tag = tags[i];
                    String[] parms = tag.getParameters();
                    if (parms != null && parms.length > 0 && name.equals(parms[0])) {
                        return docText(tag.getValue());
                    }
                }
            }
            return null;
        } else {
            throw new IllegalStateException("Internal error: not a method");
        }
    }
}
/*
 * Copyright (c) 2008-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.generator;

import java.util.HashMap;
import java.util.Map;

import org.jibx.custom.classes.IDocumentFormatter;
import org.jibx.custom.classes.SharedNestingBase;
import org.jibx.util.IClassLocator;

/**
 * Cache of JavaDoc formatter instances.
 */
public class FormatterCache
{
    /** Locator for class information (<code>null</code> if none). */
    private final IClassLocator m_locator;
    
    /** Map from class name to instance. This is used to keep track of JavaDoc formatter instances. */
    private final Map m_classInstances;
    
    /**
     * Constructor.
     * 
     * @param loc locator for class information (<code>null</code> if none)
     */
    public FormatterCache(IClassLocator loc) {
        m_locator = loc;
        m_classInstances = new HashMap();
    }
    
    /**
     * Get the JavaDoc formatter instance for a customization.
     *
     * @param custom customization information
     * @return formatter
     */
    public IDocumentFormatter getFormatter(SharedNestingBase custom) {
        String cname = custom.getFormatterClass();
        IDocumentFormatter format = (IDocumentFormatter)m_classInstances.get(cname);
        if (format == null) {
            Class clas = m_locator.loadClass(cname);
            if (clas == null) {
                throw new IllegalStateException("Cannot load document formatter class " + cname);
            }
            try {
                Object inst = clas.newInstance();
                if (!(inst instanceof IDocumentFormatter)) {
                    throw new IllegalStateException("Specified document formatter class does not implement IDocumentFormatter interface: " + cname);
                }
                m_classInstances.put(cname, inst);
                format = (IDocumentFormatter)inst;
            } catch (InstantiationException e) {
                throw new IllegalStateException("Cannot create instance of document formatter class " + cname);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot access document formatter class " + cname);
            }
        }
        return format;
    }
}
/*
 * Copyright (c) 2008, Dennis M. Sosnoski. All rights reserved.
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

import java.util.List;

import org.jibx.util.IClass;
import org.jibx.util.IClassItem;

/**
 * Formatter interface for JavaDoc conversion to XML documentation components.
 */
public interface IDocumentFormatter
{
    /** Default implementation class for interface. */
    static final String DEFAULT_IMPLEMENTATION = "org.jibx.schema.generator.DocumentFormatter";
    
    /**
     * Convert JavaDoc text to a list of formatted nodes.
     * 
     * @param jdoc JavaDoc text (may be <code>null</code>)
     * @return formatted representation (may be <code>null</code>)
     */
    List docToNodes(String jdoc);
    
    /**
     * Get formatted documentation from class. Implementations must return the documentation components in the form of
     * a list of <code>org.w3c.dom.Node</code> instances.
     * 
     * @param info class information
     * @return formatted documentation (<code>null</code> if none)
     */
    List getClassDocumentation(IClass info);
    
    /**
     * Get formatted documentation from class item. Implementations must return the documentation components in the form
     * of a list of <code>org.w3c.dom.Node</code> instances.
     * 
     * @param info class item information
     * @return formatted representation (<code>null</code> if none)
     */
    List getItemDocumentation(IClassItem info);
}
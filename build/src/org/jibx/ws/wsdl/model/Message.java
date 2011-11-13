/*
 * Copyright (c) 2004-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.ws.wsdl.model;

import java.util.List;

import org.jibx.runtime.QName;
import org.jibx.util.LazyList;

/**
 * WSDL object model components corresponding to a message definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class Message
{
    /** Actual message name. */
    private String m_name;
    
    /** Qualified name in WSDL target namespace. */
    private QName m_qName;
    
    /** Parts defined for this message. */
    private List m_parts;
    
    /**
     * Constructor from message name and singleton part.
     * 
     * @param name message name
     * @param tns target namespace
     */
    public Message(String name, String tns) {
        m_name = name;
        m_qName = new QName(tns, name);
        m_parts = new LazyList();
    }
    
    /**
     * Get message name.
     * 
     * @return message name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Get qualified name.
     *
     * @return qualified name
     */
    public QName getQName() {
        return m_qName;
    }

    /**
     * Set qualified name.
     *
     * @param name
     */
    public void setQName(QName name) {
        m_qName = name;
    }

    /**
     * Get message parts.
     * 
     * @return list of parts
     */
    public List getParts() {
        return m_parts;
    }
}
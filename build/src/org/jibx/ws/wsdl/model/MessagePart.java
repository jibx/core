/*
 * Copyright (c) 2004-2008, Dennis M. Sosnoski. All rights reserved.
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

import org.jibx.runtime.QName;

/**
 * WSDL object model components corresponding to a message definition part.
 * 
 * @author Dennis M. Sosnoski
 */
public class MessagePart
{
    /** Actual part name. */
    private String m_name;
    
    /** Referenced element. */
    private QName m_elementReference;
    
    /**
     * Constructor from part and element names.
     * 
     * @param mname message name
     * @param eref element qualified name
     */
    public MessagePart(String mname, QName eref) {
        m_name = mname;
        m_elementReference = eref;
    }
    
    /**
     * Get part name.
     * 
     * @return part name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Set part name.
     *
     * @param name
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Get referenced element name.
     * 
     * @return element name
     */
    public QName getElementReference() {
        return m_elementReference;
    }

    /**
     * Set referenced element name.
     *
     * @param ref
     */
    public void setElementReference(QName ref) {
        m_elementReference = ref;
    }
}
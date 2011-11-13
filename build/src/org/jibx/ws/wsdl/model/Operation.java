/*
 * Copyright (c) 2004-2007, Dennis M. Sosnoski. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

/**
 * WSDL object model component corresponding to an operation definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class Operation
{
    /** Actual operation name. */
    private String m_name;
    
    /** SOAP action. */
    private String m_soapAction;
    
    /** Documentation as node list (<code>null</code> if none). */
    private List m_documentation;
    
    /** Ordered message references for this operation. */
    private ArrayList m_messageRefs;
    
    /**
     * Constructor from operation name.
     * 
     * @param name operation name
     */
    public Operation(String name) {
        m_name = name;
        m_soapAction = "";
        m_messageRefs = new ArrayList();
    }
    
    /**
     * Add reference to input message. All input message(s) must be set before any output or fault messages are set.
     * 
     * @param msg input message
     */
    public void addInputMessage(Message msg) {
        m_messageRefs.add(new MessageReference(MessageReference.INPUT_REFERENCE, msg));
    }
    
    /**
     * Add reference to output message. All output message(s) must be set after any input messages and before any fault
     * messages are set.
     * 
     * @param msg output message
     */
    public void addOutputMessage(Message msg) {
        m_messageRefs.add(new MessageReference(MessageReference.OUTPUT_REFERENCE, msg));
    }
    
    /**
     * Add reference to fault message. All fault message(s) must be set after any input or output messages are set.
     * 
     * @param msg fault message
     */
    public void addFaultMessage(Message msg) {
        m_messageRefs.add(new MessageReference(MessageReference.FAULT_REFERENCE, msg));
    }
    
    /**
     * Get operation name.
     * 
     * @return operation name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Get soapAction.
     * 
     * @return soapAction
     */
    public String getSoapAction() {
        return m_soapAction;
    }
    
    /**
     * Set soapAction.
     * 
     * @param action
     */
    public void setSoapAction(String action) {
        m_soapAction = action;
    }
    
    /**
     * Get documentation.
     * 
     * @return list of nodes
     */
    public List getDocumentation() {
        return m_documentation;
    }
    
    /**
     * Set documentation.
     * 
     * @param nodes list of nodes
     */
    public void setDocumentation(List nodes) {
        m_documentation = nodes;
    }
    
    /**
     * Get message references for operation. The returned list is live, but should not be modified by the caller.
     * 
     * @return list of parts
     */
    public ArrayList getMessageReferences() {
        return m_messageRefs;
    }
}
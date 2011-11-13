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

/**
 * Reference to a message within an operation. Since messages may be referenced as input, output, or fault messages, the
 * appropriate type is tracked by this class, along with the actual message.
 * 
 * @author Dennis M. Sosnoski
 */
public class MessageReference
{
    /** Reference to message as input. */
    public static final int INPUT_REFERENCE = 0;
    
    /** Reference to message as output. */
    public static final int OUTPUT_REFERENCE = 1;
    
    /** Reference to message as fault. */
    public static final int FAULT_REFERENCE = 2;
    
    /** Type of message reference. */
    private int m_usage;
    
    /** Name for this reference. */
    private String m_name;
    
    /** Actual message. */
    private Message m_message;
    
    /**
     * Internal constructor used with JiBX binding.
     * 
     * @param usage reference type code
     */
    private MessageReference(int usage) {
        m_usage = usage;
    }
    
    /**
     * Constructor from part and element names.
     * 
     * @param usage reference type code
     * @param msg referenced message
     */
    public MessageReference(int usage, Message msg) {
        m_usage = usage;
        m_message = msg;
    }
    
    /**
     * Check if reference is to message as input.
     * 
     * @return <code>true</code> if input reference, <code>false</code> if not
     */
    public boolean isInput() {
        return m_usage == INPUT_REFERENCE;
    }
    
    /**
     * Check if reference is to message as output.
     * 
     * @return <code>true</code> if output reference, <code>false</code> if not
     */
    public boolean isOutput() {
        return m_usage == OUTPUT_REFERENCE;
    }
    
    /**
     * Check if reference is to message as fault.
     * 
     * @return <code>true</code> if fault reference, <code>false</code> if not
     */
    public boolean isFault() {
        return m_usage == FAULT_REFERENCE;
    }
    
    /**
     * Get name for this reference.
     * 
     * @return reference name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Set name for this reference.
     *
     * @param name
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * Get referenced message.
     * 
     * @return referenced message
     */
    public Message getMessage() {
        return m_message;
    }
    
    /**
     * Factory for creating input message reference templates. The actual referenced message information needs to be set
     * separately.
     * 
     * @return created reference
     */
    private static MessageReference inputReferenceFactory() {
        return new MessageReference(INPUT_REFERENCE);
    }
    
    /**
     * Factory for creating output message reference templates. The actual referenced message information needs to be
     * set separately.
     * 
     * @return created reference
     */
    private static MessageReference outputReferenceFactory() {
        return new MessageReference(OUTPUT_REFERENCE);
    }
    
    /**
     * Factory for creating fault message reference templates. The actual referenced message information needs to be set
     * separately.
     * 
     * @return created reference
     */
    private static MessageReference faultReferenceFactory() {
        return new MessageReference(FAULT_REFERENCE);
    }
}
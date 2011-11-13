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

package org.jibx.ws.wsdl.tools;

/**
 * Pull parser for generic method or field signature.
 * 
 * @author Dennis M. Sosnoski
 */
public class SignatureParser
{
    //
    // Signature events
    public static final int END_EVENT = 0;
    
    public static final int TYPE_EVENT = 1;
    
    public static final int METHOD_PARAMETERS_START_EVENT = 2;
    
    public static final int METHOD_PARAMETERS_END_EVENT = 3;
    
    public static final int TYPE_PARAMETERS_START_EVENT = 4;
    
    public static final int TYPE_PARAMETERS_END_EVENT = 5;
    
    //
    // Common types
    private static final String STRING_SIGNATURE = "java/lang/String;";
    
    private static final String STRING_TYPE = "java.lang.String";
    
    private static final String OBJECT_SIGNATURE = "java/lang/Object;";
    
    private static final String OBJECT_TYPE = "java.lang.Object";
    
    //
    // Instance data
    private final String m_signature;
    
    private int m_offset;
    
    private int m_event;
    
    private boolean m_isPrimitive;
    
    private boolean m_isParameterized;
    
    private String m_type;
    
    /**
     * Constructor.
     * 
     * @param sig signature attribute value
     */
    public SignatureParser(String sig) {
        if (sig.startsWith("Signature(") && sig.endsWith(")")) {
            m_signature = sig.substring(10, sig.length() - 1);
            m_event = -1;
        } else {
            throw new IllegalArgumentException("Internal error: not a valid Signature");
        }
    }
    
    /**
     * Check if type is parameterized. It is an error to call this if the current event is not {@link #TYPE_EVENT}.
     * 
     * @return <code>true</code> if parameterized type
     */
    public boolean isParameterized() {
        if (m_event == TYPE_EVENT) {
            return m_isParameterized;
        } else {
            throw new IllegalStateException("Internal error: not at TYPE_EVENT");
        }
    }
    
    /**
     * Check if type is a primitive. It is an error to call this if the current event is not {@link #TYPE_EVENT}.
     * 
     * @return <code>true</code> if primitive type
     */
    public boolean isPrimitive() {
        if (m_event == TYPE_EVENT) {
            return m_isPrimitive;
        } else {
            throw new IllegalStateException("Internal error: not at TYPE_EVENT");
        }
    }
    
    /**
     * Get current event.
     * 
     * @return event
     */
    public int getEvent() {
        return m_event;
    }
    
    /**
     * Get type. It is an error to call this if the current event is not {@link #TYPE_EVENT}.
     * 
     * @return type
     */
    public String getType() {
        if (m_event == TYPE_EVENT) {
            return m_type;
        } else {
            throw new IllegalStateException("Internal error: not at TYPE_EVENT");
        }
    }
    
    /**
     * Get next parse event.
     * 
     * @return event
     */
    public int next() {
        if (m_event == END_EVENT) {
            throw new IllegalStateException("Internal error: cannot advance parser");
        } else if (m_offset >= m_signature.length()) {
            m_event = END_EVENT;
        } else {
            
            // assume next event is a primitive type, then correct if necessary
            m_event = TYPE_EVENT;
            m_isPrimitive = true;
            m_isParameterized = false;
            char chr = m_signature.charAt(m_offset++);
            switch (chr) {
                
                // blocking start/end characters
                
                case '(':
                    m_event = METHOD_PARAMETERS_START_EVENT;
                    break;
                
                case ')':
                    m_event = METHOD_PARAMETERS_END_EVENT;
                    break;
                
                case '<':
                    m_event = TYPE_PARAMETERS_START_EVENT;
                    break;
                
                case '>':
                    m_event = TYPE_PARAMETERS_END_EVENT;
                    if (m_offset < m_signature.length() && m_signature.charAt(m_offset) == ';') {
                        m_offset++;
                    }
                    break;
                
                // primitive type indication characters
                
                case 'B':
                    m_type = "byte";
                    break;
                
                case 'C':
                    m_type = "char";
                    break;
                
                case 'D':
                    m_type = "double";
                    break;
                
                case 'F':
                    m_type = "float";
                    break;
                
                case 'I':
                    m_type = "int";
                    break;
                
                case 'J':
                    m_type = "long";
                    break;
                
                case 'S':
                    m_type = "short";
                    break;
                
                case 'V':
                    m_type = "void";
                    break;
                
                case 'Z':
                    m_type = "boolean";
                    break;
                
                // object type
                
                case 'L': {
                    m_isPrimitive = false;
                    if (m_signature.startsWith(STRING_SIGNATURE, m_offset)) {
                        m_offset += STRING_SIGNATURE.length();
                        m_type = STRING_TYPE;
                    } else if (m_signature.startsWith(OBJECT_SIGNATURE, m_offset)) {
                        m_offset += OBJECT_SIGNATURE.length();
                        m_type = OBJECT_TYPE;
                    } else {
                        StringBuffer buff = new StringBuffer();
                        boolean done = false;
                        while (m_offset < m_signature.length()) {
                            chr = m_signature.charAt(m_offset++);
                            if (chr == '/') {
                                buff.append('.');
                            } else if (chr == ';') {
                                done = true;
                                break;
                            } else if (chr == '<') {
                                done = true;
                                m_offset--;
                                m_isParameterized = true;
                                break;
                            } else {
                                buff.append(chr);
                            }
                        }
                        if (!done) {
                            throw new IllegalStateException("Internal error: cannot interpret type");
                        } else {
                            m_type = buff.toString();
                        }
                    }
                    break;
                }
                    
                    // error if anything else
                    
                default:
                    throw new IllegalStateException("Internal error: signature parse state");
                    
            }
        }
        return m_event;
    }
}
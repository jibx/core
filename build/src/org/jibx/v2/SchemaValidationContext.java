/*
 * Copyright (c) 2007, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.v2;

import org.jibx.runtime.JiBXException;

/**
 * Tracks the schema validation state. This includes order-dependent state information collected while walking the tree
 * structure of a schema model. Collects all errors and warnings and maintains a summary of the severity of the problems
 * found. For ease of use, this also wraps the schema name register with convenience methods for validation.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaValidationContext extends ValidationContext
{
    private Object m_object;
    
    public void setObject(Object obj) {
        m_object = obj;
    }
    
    public void requiredPatternCheck(String name, String value, String pattern) throws JiBXException {
        if (value == null) {
            addError(name + " is missing (null)", m_object);
        } else  {
        }
    }
    
    public void optionalPatternCheck(String name, String value, String pattern) throws JiBXException {
        if (value == null) {
            addError(name + " is missing (null)", m_object);
        } else  {
        }
    }
    
    public void requiredLengthCheck(String name, String value, int min, int max) throws JiBXException {
        if (value == null) {
            addError(name + " is missing (null)", m_object);
        } else  {
            int length = value.length();
            if (length < min || length > max) {
                addError(name + " must be a minimum of " + min + " and a maximum of " + max + " characters (found " +
                    length + " characters)", m_object);
            }
        }
    }
    
    public void optionalLengthCheck(String name, String value, int min, int max) throws JiBXException {
        if (value != null) {
            int length = value.length();
            if (length < min || length > max) {
                addError(name + " must be a minimum of " + min + " and a maximum of " + max + " characters (found " +
                    length + " characters)", m_object);
            }
        }
    }
}
/*
Copyright (c) 2004-2005, Dennis M. Sosnoski
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jibx.binding.model;

import org.jibx.runtime.ITrackSource;
import org.jibx.runtime.ValidationException;

/**
 * Problem reported by model validation. Provides the details for a specific
 * problem item.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
public class ValidationProblem
{
    // severity levels
    public static final int WARNING_LEVEL = 0;
    public static final int ERROR_LEVEL = 1;
    public static final int FATAL_LEVEL = 2;
    
    /** Problem severity level. */
    private final int m_severity;
    
    /** Supplied problem description message. */
    private final String m_message;
    
    /** Component that reported problem. */
    private final Object m_component;
    
    /**
     * Full constructor.
     * 
     * @param level severity level of problem
     * @param msg problem description
     * @param obj source object for validation error (may be <code>null</code>
     * if not specific to a particular component)
     */
    /*package*/ ValidationProblem(int level, String msg, Object obj) {
        m_severity = level;
        m_message = msg;
        m_component = obj;
    }
    
    /**
     * Create description text for a component of a binding definition.
     * 
     * @param obj binding definition component
     * @return description
     */
    public static String componentDescription(Object obj) {
        StringBuffer buff = new StringBuffer();
        if (obj instanceof ElementBase) {
            buff.append(ElementBase.ELEMENT_NAMES[((ElementBase)obj).type()]);
            buff.append(" element");
        } else {
            String cname = obj.getClass().getName();
            int split = cname.lastIndexOf('.');
            if (split >= 0) {
                cname = cname.substring(split+1);
            }
            buff.append(cname);
        }
        if (obj instanceof ITrackSource) {
            buff.append(" at ");
            buff.append(ValidationException.describe(obj));
        } else {
            buff.append(" at unknown location");
        }
        return buff.toString();
    }

    /**
     * Constructor using default (error) severity level.
     * 
     * @param msg problem description
     * @param obj source object for validation error
     */
    /*package*/ ValidationProblem(String msg, Object obj) {
        this(ERROR_LEVEL, msg, obj);
    }
    
    /**
     * Get the main binding definition item for the problem.
     * 
     * @return element or attribute at root of problem
     */
    public Object getComponent() {
        return m_component;
    }
    
    /**
     * Get problem description.
     * 
     * @return problem description
     */
    public String getDescription() {
        if (m_component == null) {
            return m_message;
        } else {
            return m_message + "; on " + componentDescription(m_component);
        }
    }
    
    /**
     * Get problem severity level.
     * 
     * @return severity level for problem
     */
    public int getSeverity() {
        return m_severity;
    }
}
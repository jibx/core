/*
Copyright (c) 2006, Dennis M. Sosnoski
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

package org.jibx.schema.types;

import org.jibx.runtime.JiBXException;
import org.jibx.runtime.Utility;

/**
 * Repetition count in a schema definition.
 * 
 * @author Dennis M. Sosnoski
 */
public class Count
{
    /** Predefined count of '0'. */
    public static final Count COUNT_ZERO = new Count(0, false);
    
    /** Predefined count of '1'. */
    public static final Count COUNT_ONE = new Count(1, false);
    
    /** Predefined count of 'unbounded'. */
    public static final Count COUNT_UNBOUNDED = new Count(0, true);
    
    /** Actual count for bounded value. */
    private final int m_count;
    
    /** Flag for unbounded value. */
    private final boolean m_unbounded;
    
    /**
     * Internal constructor.
     * 
     * @param count
     * @param unbounded
     */
    private Count(int count, boolean unbounded) {
        m_count = count;
        m_unbounded = unbounded;
    }
    
    /**
     * Get count value. This method throws an exception if used with an
     * unbounded value, so always try {@link #isUnbounded()} first.
     *
     * @return count
     */
    public int getCount() {
        if (m_unbounded) {
            throw new IllegalStateException("Cannot get count for unbounded value");
        } else {
            return m_count;
        }
    }

    /**
     * Check for unbounded count.
     *
     * @return unbounded flag
     */
    public boolean isUnbounded() {
        return m_unbounded;
    }
    
    /**
     * Check for count equal to a particular value. This is a convenience method
     * which avoids the need to separately check unbounded and then compare the
     * count.
     * 
     * @param value
     * @return equal flag
     */
    public boolean isEqual(int value) {
        return !m_unbounded && m_count == value;
    }
    
    /**
     * Check for count greater than a particular value. This is a convenience
     * method which avoids the need to separately check unbounded and then
     * compare the count.
     * 
     * @param value
     * @return greater than flag
     */
    public boolean isGreaterThan(int value) {
        return m_unbounded || m_count > value;
    }

    /**
     * Deserializer method for bounded values.
     * 
     * @param value text representation
     * @return instance of class
     * @throws JiBXException on conversion error
     */
    public static Count getBoundedCount(String value) throws JiBXException {
        if (value == null) {
            return null;
        } else {
            value = value.trim();
            if ("0".equals(value)) {
                return COUNT_ZERO;
            } else if ("1".equals(value)) {
                return COUNT_ONE;
            } else {
                return new Count(Utility.parseInt(value), false);
            }
        }
    }
    
    /**
     * Deserializer method.
     * 
     * @param value text representation
     * @return instance of class (<code>null</code> if none)
     * @throws JiBXException on conversion error
     */
    public static Count getCount(String value) throws JiBXException {
        if ("unbounded".equals(value)) {
            return COUNT_UNBOUNDED;
        } else {
            return getBoundedCount(value);
        }
    }
    
    /**
     * Check if a count attribute is equal to a specified value. If the count is <code>null</code>, the value is
     * taken as '1'.
     * 
     * @param value
     * @param count
     * @return <code>true</code> if value equal, <code>false</code> if not
     */
    public static boolean isCountEqual(int value, Count count) {
        if (count == null) {
            return value == 1;
        } else {
            return count.isEqual(value);
        }
    }
    
    /**
     * Conversion to text form.
     * 
     * @return count as text
     */
    public String toString() {
        if (m_unbounded) {
            return "unbounded";
        } else {
            return Utility.serializeInt(m_count);
        }
    }
}
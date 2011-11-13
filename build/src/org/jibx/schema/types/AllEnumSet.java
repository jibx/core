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

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.schema.validation.ValidationContext;

/**
 * Bit set based on a string enumeration list with the added option of '#all'.
 *
 * @author Dennis M. Sosnoski
 */
public class AllEnumSet
{
    /** Base enumeration. */
    private final EnumSet m_enum;
    
    /** Attribute name. */
    private final String m_name;
    
    /** Bit set for values from enumeration. */
    private final ShortBitSet m_bits;
    
    /** Flag for present (if <code>false</code>, other values ignored). */
    private boolean m_present;
    
    /** Flag for '#all' value. */
    private boolean m_all;

    /**
     * Constructor.
     * 
     * @param eset enumeration value set
     * @param name attribute name
     */
    public AllEnumSet(EnumSet eset, String name) {
        m_enum = eset;
        m_name = name;
        m_bits = new ShortBitSet();
        if (eset.maxIndex() > 15) {
            throw new IllegalArgumentException("Too many values in enumeration");
        }
    }
    
    //
    // Needed for JiBX, but should never be called.
    private AllEnumSet() {
        throw new IllegalStateException("no-arg constructor should never be called");
    }
    
    //
    // Access methods
    
    /**
     * Check if present.
     *
     * @return present
     */
    public boolean isPresent() {
        return m_present;
    }

    /**
     * Set present.
     *
     * @param present
     */
    public void setPresent(boolean present) {
        m_present = present;
    }

    /**
     * Check '#all' value.
     *
     * @return all
     */
    public boolean isAll() {
        return m_all;
    }

    /**
     * Set '#all' value.
     *
     * @param all
     */
    public void setAll(boolean all) {
        m_all = all;
    }

    /**
     * Add value to set.
     * 
     * @param value
     * @see org.jibx.schema.types.ShortBitSet#add(int)
     */
    public void add(int value) {
        m_present = true;
        if (!m_all) {
            m_bits.add(value);
        }
    }

    /**
     * Check if value in set.
     * 
     * @param value
     * @return <code>true</code> if value in set
     * @see org.jibx.schema.types.ShortBitSet#isSet(int)
     */
    public boolean isSet(int value) {
        return m_present && (m_all || m_bits.isSet(value));
    }

    /**
     * Remove value from set.
     * 
     * @param value
     * @see org.jibx.schema.types.ShortBitSet#remove(int)
     */
    public void remove(int value) {
        if (m_present) {
            if (m_all) {
                m_all = false;
                m_bits.setRange(0, m_enum.maxIndex());
            }
            m_bits.remove(value);
        }
    }
    
    /**
     * Serializer method for output as value list.
     * 
     * @return string value, or <code>null</code> if not present
     */
    public String toString() {
        if (m_present) {
            if (m_all) {
                return "#all";
            } else {
                StringBuffer buff = new StringBuffer();
                for (int i = 0; i <= m_enum.maxIndex(); i++) {
                    if (m_bits.isSet(i)) {
                        if (buff.length() > 0) {
                            buff.append(' ');
                        }
                        buff.append(m_enum.getName(i));
                    }
                }
                return buff.toString();
            }
        } else {
            return null;
        }
    }
    
    /**
     * Deserializer method for input as value list.
     * 
     * @param text string value, or <code>null</code> if not present
     * @param vctx
     * @param obj object being validated
     */
    public void fromString(String text, ValidationContext vctx, Object obj) {
        if (text == null) {
            m_present = false;
        } else {
            
            // indicate present
            m_present = true;
            m_all = false;
            m_bits.clear();
            
            // scan text to find whitespace breaks between items
            int length = text.length();
            int base = 0;
            boolean space = true;
            for (int i = 0; i < length; i++) {
                char chr = text.charAt(i);
                switch (chr) {
                    
                    case 0x09:
                    case 0x0A:
                    case 0x0D:
                    case ' ':
                        
                        // ignore if preceded by space
                        if (!space) {
                            
                            // process name from list
                            addName(text.substring(base, i), vctx, obj);
                            space = true;
                            
                        }
                        base = i + 1;
                        break;
                        
                    default:
                        space = false;
                        break;
                }
            }
            
            // finish last item
            if (base < length) {
                addName(text.substring(base), vctx, obj);
            }
            
        }
    }
    
    /**
     * Deserializer method for unmarshalling input as value list.
     * 
     * @param text string value, or <code>null</code> if not present
     * @param ictx
     */
    private void fromString(String text, IUnmarshallingContext ictx) {
        fromString(text, (ValidationContext)ictx.getUserContext(),
            ictx.getStackTop());
    }

    /**
     * Process name from text list. This validates the name and adds it to the
     * bit set.
     * 
     * @param name
     * @param vctx
     * @param obj
     */
    private void addName(String name, ValidationContext vctx, Object obj) {
        if (m_all) {
            vctx.addError("'#all' cannot be used with other values in '" +
                m_name + "' attribute", obj);
        } else if ("#all".equals(name)) {
            m_all = true;
        } else {
            int index = m_enum.getValue(name);
            if (index >= 0) {
                m_bits.add(index);
            } else {
                vctx.addError("'" + name + "' is not valid for '" +
                    m_name + "' attribute value", obj);
            }
        }
    }
}
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

/**
 * Bit set stored as a <code>short</code> value.
 *
 * @author Dennis M. Sosnoski
 */
public class ShortBitSet
{
    /** Array of bit masks. */
    private static final char[] s_bitMasks = {
        0x0001, 0x0002, 0x0004, 0x0008, 0x0010, 0x0020, 0x0040, 0x0080,
        0x0100, 0x0200, 0x0400, 0x0800, 0x1000, 0x2000, 0x4000, 0x8000
    };
    
    /** Mask for values in set. */
    private char m_bits;
    
    //
    // Access methods
    
    /**
     * Check for value in set.
     * 
     * @param value
     * @return <code>true</code> if in set, <code>false</code> if not
     */
    public boolean isSet(int value) {
        return (m_bits & s_bitMasks[value]) != 0;
    }
    
    /**
     * Include value in set.
     * 
     * @param value
     */
    public void add(int value) {
        m_bits |= s_bitMasks[value];
    }
    
    /**
     * Exclude value from set.
     * 
     * @param value
     */
    public void remove(int value) {
        m_bits &= ~s_bitMasks[value];
    }
    
    /**
     * Clear all values.
     */
    public void clear() {
        m_bits = 0;
    }
    
    /**
     * Set all values in range.
     * 
     * @param min minimum value in range
     * @param max maximum value in range
     */
    public void setRange(int min, int max) {
        int mask = (s_bitMasks[max] << 1) - s_bitMasks[min];
        m_bits |= mask;
    }
}
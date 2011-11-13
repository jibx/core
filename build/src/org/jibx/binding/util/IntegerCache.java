/*
Copyright (c) 2003-2005, Dennis M. Sosnoski
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

package org.jibx.binding.util;


/**
 * Cache of <code>Integer</code> values. This is designed on the assumption that
 * clients will start with zero and work their way up from there. By holding
 * created instances in an array it allows for full reuse.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public abstract class IntegerCache
{
    /** Initial set of index values supported. */
    private static Integer[] s_integers =
    {
        new Integer(0), new Integer(1), new Integer(2), new Integer(3),
        new Integer(4), new Integer(5), new Integer(6), new Integer(7)
    };

    /**
     * Get <code>Integer</code> for value.
     *
     * @param value non-negative integer value
     * @return corresponding <code>Integer</code> value
     */

    public static Integer getInteger(int value) {
        if (value >= s_integers.length) {
            
            // Note that in multithreaded operation there's a chance of two
            //  threads getting in a race condition here, but the worst that
            //  can happen is that the array gets extended more than it needs
            //  to be. Threads just using the array are safe - though if they
            //  get an old version they may again extend the array
            //  unnecessarily.
            synchronized(IntegerCache.class) {
                int size = s_integers.length * 3 / 2;
                if (size <= value) {
                    size = value + 1;
                }
                Integer[] ints = new Integer[size];
                System.arraycopy(s_integers, 0, ints, 0, s_integers.length);
                for (int i = 0; i < size; i++) {
                    ints[i] = new Integer(i);
                }
                s_integers = ints;
            }
        }
        return s_integers[value];
    }
}

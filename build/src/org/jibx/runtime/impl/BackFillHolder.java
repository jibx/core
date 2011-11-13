/*
Copyright (c) 2002,2003, Dennis M. Sosnoski.
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

package org.jibx.runtime.impl;

import java.util.ArrayList;

/**
 * Holder used to collect forward references to a particular object. The
 * references are processed when the object is defined.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class BackFillHolder
{
    /** Expected class name of tracked object. */
    private String m_class;
    
    /** List of references to this object. */
    private ArrayList m_list;
    
    /**
     * Constructor. Just creates the backing list.
     *
     * @param name expected class name of tracked object
     */
     
    public BackFillHolder(String name) {
        m_class = name;
        m_list = new ArrayList();
    }
    
    /**
     * Add forward reference to tracked object. This method is called by 
     * the framework when a reference item is created for the object
     * associated with this holder.
     *
     * @param ref backfill reference item
     */
    
    public void addBackFill(BackFillReference ref){
        m_list.add(ref);
    }
    
    /**
     * Define referenced object. This method is called by the framework
     * when the forward-referenced object is defined, and in turn calls each
     * reference to fill in the reference.
     *
     * @param obj referenced object
     */
    
    public void defineValue(Object obj) {
        for (int i = 0; i < m_list.size(); i++) {
            BackFillReference ref = (BackFillReference)m_list.get(i);
            ref.backfill(obj);
        }
    }
    
    /**
     * Get expected class name of referenced object.
     *
     * @return expected class name of referenced object
     */
    
    public String getExpectedClass() {
        return m_class;
    }
}

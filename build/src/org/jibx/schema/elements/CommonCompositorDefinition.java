/*
Copyright (c) 2006-2007, Dennis M. Sosnoski
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

package org.jibx.schema.elements;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Compositor for complex content model, including the special case of &lt;all>.
 * The subclasses implement the different models for how nested particles are
 * combined.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class CommonCompositorDefinition extends CommonCompositorBase
{
    /** Mask for child elements allowed by &lt;choice> and &lt;sequence>. */
    protected static long CHOICE_SEQUENCE_PARTICLE_MASK =
        ELEMENT_MASKS[ANY_TYPE] | ELEMENT_MASKS[CHOICE_TYPE] |
        ELEMENT_MASKS[ELEMENT_TYPE] | ELEMENT_MASKS[GROUP_TYPE] |
        ELEMENT_MASKS[SEQUENCE_TYPE];
    
    //
    // Instance data
    
    /** Filtered list of composited particle elements. */
    private final FilteredSegmentList m_particleList;

    /**
     * Constructor.
     * 
     * @param type element type
     * @param mask mask for allowed particle elements
     */
    protected CommonCompositorDefinition(int type, long mask) {
    	super(type);
        m_particleList = new FilteredSegmentList(getChildrenWritable(),
            mask, this);
    }
    
    //
    // Access methods

    /**
     * Get list of composited particles.
     *
     * @return list
     */
    public FilteredSegmentList getParticleList() {
        return m_particleList;
    }
    
    //
    // Overrides of base class methods

    /* (non-Javadoc)
     * @see org.jibx.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
}
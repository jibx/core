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

import org.jibx.schema.types.Count;
import org.jibx.schema.validation.ValidationContext;

/**
 * &lt;all> element definition. Even though &lt;all> is considered a compositor
 * by the schema specification, it has substantial restrictions on use.
 *
 * @author Dennis M. Sosnoski
 */
public class AllElement extends CommonCompositorDefinition
{
    /** Mask bits for allowed child elements. */
    private static long PARTICLE_MASK = ELEMENT_MASKS[ELEMENT_TYPE];

    /**
     * Constructor.
     */
    public AllElement() {
        super(ALL_TYPE, PARTICLE_MASK);
    }
    
    //
    // Validation methods
    
    /* (non-Javadoc)
     * @see org.jibx.schema.AnnotatedBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // handle base class prevalidation first so values will be available
        super.prevalidate(vctx);
        
        // check added constraints for <b>all</b>
        Count count = getMaxOccurs();
        if (count != null && (count.isUnbounded() || count.getCount() != 1)) {
            vctx.addError("The <all> element only allows 'maxOccurs' of '1'", this);
        }
        count =getMinOccurs();
        if (count != null && (count.isUnbounded() || count.getCount() > 1)) {
            vctx.addError("The <all> element only allows 'minOccurs' of '0' or '1'", this);
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // TODO handle checks to make sure each element child has minOccurs=0 or 1
        //  and maxOccurs=1
        
        // continue with base class prevalidation
        super.validate(vctx);
    }
}
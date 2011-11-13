/*
 * Copyright (c) 2006-2008, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema;

import org.jibx.schema.types.Count;

/**
 * Interface for schema components representing complex or simple values, which may be optional or repeating.
 */
public interface IArity
{
    /**
     * Get maximum number of times this item can occur.
     * 
     * @return count
     * @see org.jibx.schema.attributes.OccursAttributeGroup#getMaxOccurs()
     */
    public Count getMaxOccurs();
    
    /**
     * Set maximum number of times this item can occur.
     * 
     * @param count
     * @see org.jibx.schema.attributes.OccursAttributeGroup#setMaxOccurs(Count)
     */
    public void setMaxOccurs(Count count);
    
    /**
     * Get minimum number of times this item can occur.
     * 
     * @return count
     * @see org.jibx.schema.attributes.OccursAttributeGroup#getMinOccurs()
     */
    public Count getMinOccurs();
    
    /**
     * Set minimum number of times this item can occur.
     * 
     * @param count
     * @see org.jibx.schema.attributes.OccursAttributeGroup#setMinOccurs(Count)
     */
    public void setMinOccurs(Count count);
}
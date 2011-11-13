/*
Copyright (c) 2007-2009, Dennis M. Sosnoski
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

package org.jibx.schema.validation;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.impl.ITrackSourceImpl;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * Location of validation problem. An instance of this can be used in place of
 * an unmarshalled element in cases where the validation problem prevents the
 * creation of the element object.
 * TODO: move this out of the schema package, generalize
 *
 * @author Dennis M. Sosnoski
 */
public class ProblemLocation implements ITrackSourceImpl
{
    private String m_document;
    private int m_line;
    private int m_column;
    private String m_name;
    
    /**
     * Constructor. This initializes the location information from the context.
     * 
     * @param ictx
     */
    public ProblemLocation(IUnmarshallingContext ictx) {
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        ctx.trackObject(this);
        m_name = ctx.currentNameString();
    }
    
    /**
     * Get the element name.
     *
     * @return name
     */
    public String getName() {
        return m_name;
    }

    public void jibx_setSource(String name, int line, int column) {
        m_document = name;
        m_line = line;
        m_column = column;
    }
    
    public int jibx_getColumnNumber() {
        return m_column;
    }
    
    public String jibx_getDocumentName() {
        return m_document;
    }
    
    public int jibx_getLineNumber() {
        return m_line;
    }
}
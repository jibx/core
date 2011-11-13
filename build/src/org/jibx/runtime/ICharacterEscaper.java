/*
Copyright (c) 2004, Dennis M. Sosnoski.
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

package org.jibx.runtime;

import java.io.IOException;
import java.io.Writer;

/**
 * Escaper for character data to be written to output document. This allows
 * special character encodings to be handled appropriately on output. It's used
 * by the generic output handler class during document marshalling.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public interface ICharacterEscaper
{
    /**
     * Write attribute value with character entity substitutions. This assumes
     * that attributes use the regular quote ('"') delimitor.
     *
     * @param text attribute value text
     * @param writer sink for output text
     * @throws IOException on error writing to document
     */

    public void writeAttribute(String text, Writer writer) throws IOException;
    
    /**
     * Write content value with character entity substitutions.
     *
     * @param text content value text
     * @param writer sink for output text
     * @throws IOException on error writing to document
     */

    public void writeContent(String text, Writer writer) throws IOException;
    
    /**
     * Write CDATA to document. This writes the beginning and ending sequences
     * for a CDATA section as well as the actual text, verifying that only
     * characters allowed by the encoding are included in the text.
     *
     * @param text content value text
     * @param writer sink for output text
     * @throws IOException on error writing to document
     */

    public void writeCData(String text, Writer writer) throws IOException;
}
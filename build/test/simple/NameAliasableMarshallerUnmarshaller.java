/*
Copyright (c) 2009, Dennis M. Sosnoski.
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

package simple;

import org.jibx.runtime.IAliasable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

public class NameAliasableMarshallerUnmarshaller implements IMarshaller,
IUnmarshaller,IAliasable
{
    private final String m_uri;
    private final int m_index;
    private final String m_name;
    
    /**
     * Aliased constructor.
     *
     * @param uri namespace URI for the top-level element
     * @param index namespace index corresponding to the defined URI within the
     * marshalling context definitions
     * @param name local name for the top-level element
     */
    public NameAliasableMarshallerUnmarshaller(String uri, int index,
    String name) {
        m_uri = uri;
        m_index = index;
        m_name = name;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.runtime.IMarshaller#isExtension(java.lang.String)
     */
    public boolean isExtension(String mapname) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IMarshaller#marshal(java.lang.Object, org.jibx.runtime.IMarshallingContext)
     */
    public void marshal(Object obj, IMarshallingContext ictx)
        throws JiBXException {
        MarshallingContext ctx = (MarshallingContext)ictx;
        Name name = (Name)obj;
        ctx.startTag(m_index, m_name);
        ctx.element(m_index, "first-name", name.firstName);
        ctx.element(m_index, "last-name", name.lastName);
        ctx.endTag(m_index, m_name);
    }
    
    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#isPresent(org.jibx.runtime.IUnmarshallingContext)
     */
    public boolean isPresent(IUnmarshallingContext ctx) throws JiBXException {
        return ctx.isAt(m_uri, m_name);
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#unmarshal(java.lang.Object, org.jibx.runtime.IUnmarshallingContext)
     */
    public Object unmarshal(Object obj, IUnmarshallingContext ictx) throws JiBXException {
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        Name name = (Name)obj;
        if (name == null) {
            name = new Name();
        }
        ctx.parsePastStartTag(m_uri, m_name);
        name.firstName = ctx.parseElementText(m_uri, "first-name");
        name.lastName = ctx.parseElementText(m_uri, "last-name");
        ctx.parsePastEndTag(m_uri, m_name);
        return name;
    }
}
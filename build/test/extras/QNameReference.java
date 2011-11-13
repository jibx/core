/*
Copyright (c) 2005, Dennis M. Sosnoski
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

package extras;

import java.io.IOException;
import java.util.ArrayList;

import org.jibx.extras.QName;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;

public class QNameReference
{
    protected QName m_ref1;
    protected QName m_ref2;
    protected QName m_ref3;
    protected ArrayList m_namespaces;
    
    protected QName getRef() {
        return m_ref3;
    }
    protected void setRef(QName qname) {
        m_ref3 = qname;
    }
    
    protected void getNamespaces(IUnmarshallingContext ictx)
        throws JiBXException {
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        int count = ctx.getNamespaceCount();
        if (count > 0) {
            m_namespaces = new ArrayList();
            for (int i = 0; i < count; i++) {
                m_namespaces.add(ctx.getNamespacePrefix(i));
                m_namespaces.add(ctx.getNamespaceUri(i));
            }
        } else {
            m_namespaces = null;
        }
    }
    
    protected void setNamespaces(IMarshallingContext ictx) throws IOException {
        if (m_namespaces != null) {
            IXMLWriter writer = ((MarshallingContext)ictx).getXmlWriter();
            String[] uris = new String[m_namespaces.size()/2];
            int[] indexes = new int[uris.length];
            String[] prefs = new String[uris.length];
            int base = writer.getNamespaceCount();
            for (int i = 0; i < uris.length; i++) {
                indexes[i] = base + i;
                String pref = (String)m_namespaces.get(i*2);
                if (pref == null) {
                    pref = "";
                }
                prefs[i] = pref;
                uris[i] = (String)m_namespaces.get(i*2+1);
            }
            writer.pushExtensionNamespaces(uris);
            writer.openNamespaces(indexes, prefs);
            for (int i = 0; i < uris.length; i++) {
                String prefix = prefs[i];
                String name = prefix.length() > 0 ? "xmlns:" + prefix : "xmlns";
                writer.addAttribute(0, name, uris[i]);
            }
        }
    }
}
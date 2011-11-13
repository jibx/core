/*
 * Copyright (c) 2006, Dennis M. Sosnoski All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. Neither the name of JiBX nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.jibx.schema.elements;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.support.Conversions;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Base for wildcard element definitions.
 * 
 * @author Dennis M. Sosnoski
 */
public class WildcardBase extends AnnotatedBase
{
    /** List of allowed attribute names. */
    public static final StringArray s_allowedAttributes = new StringArray(
        new String[] { "namespace", "processContents" },
        AnnotatedBase.s_allowedAttributes);

    //
    // Value set information

    public static final int LAX_PROCESS = 0;
    public static final int SKIP_PROCESS = 1;
    public static final int STRICT_PROCESS = 2;
    public static final EnumSet s_processValues = new EnumSet(LAX_PROCESS,
        new String[] { "lax", "skip", "strict" });

    //
    // Instance data

    /** 'namespace' attribute value. */
    private String[] m_namespaces;

    /** 'processContents' attribute value. */
    private int m_processType;

    /**
     * Constructor.
     * 
     * @param type element type
     */
    public WildcardBase(int type) {
        super(type);
        m_processType = -1;
    }

    //
    // Accessor methods

    /**
     * Get 'namespace' attribute value.
     *
     * @return namespaces
     */
    public String[] getNamespaces() {
        return m_namespaces;
    }

    /**
     * Set 'namespace' attribute value.
     *
     * @param namespaces
     */
    public void setNamespaces(String[] namespaces) {
        m_namespaces = namespaces;
    }

    /**
     * Get 'processContents' attribute type code.
     * 
     * @return code (<code>-1</code> if not set)
     */
    public int getProcessContents() {
        return m_processType;
    }

    /**
     * Set 'processContents' attribute type code.
     * 
     * @param code (<code>-1</code> to unset)
     */
    public void setProcessContents(int code) {
        if (code >= 0) {
            s_processValues.checkValue(code);
        }
        m_processType = code;
    }

    /**
     * Get 'processContents' attribute text.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getProcessContentsText() {
        return s_processValues.getName(m_processType);
    }

    /**
     * Set 'processContents' attribute text. This method is provided only for
     * use when unmarshalling.
     * 
     * @param text
     * @param ictx
     */
    private void setProcessContentsText(String text,
        IUnmarshallingContext ictx) {
        m_processType = Conversions.convertEnumeration(text, s_processValues,
            "use", ictx);
    }

    //
    // Validation methods

    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.schema.ComponentBase#prevalidate(org.jibx.schema.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {

        // TODO check namespace list
        super.prevalidate(vctx);
    }
}
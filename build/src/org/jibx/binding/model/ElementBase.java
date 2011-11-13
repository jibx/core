/*
Copyright (c) 2004-2007, Dennis M. Sosnoski
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

package org.jibx.binding.model;

import org.jibx.runtime.ITrackSource;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.util.StringArray;

/**
 * Base class for all element structures in binding definition model. This just
 * provides the linkages for the binding definition tree structure and related
 * validation hooks.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
 
public abstract class ElementBase
{
    //
    // Element type definitions.
    
    public static final int BINDING_ELEMENT = 0;
    public static final int COLLECTION_ELEMENT = 1;
    public static final int FORMAT_ELEMENT = 2;
    public static final int MAPPING_ELEMENT = 3;
    public static final int NAMESPACE_ELEMENT = 4;
    public static final int STRUCTURE_ELEMENT = 5;
    public static final int TEMPLATE_ELEMENT = 6;
    public static final int VALUE_ELEMENT = 7;
    // special elements eliminated during splitting
    public static final int INCLUDE_ELEMENT = 8;
    public static final int SPLIT_ELEMENT = 9;
    public static final int INPUT_ELEMENT = 10;
    public static final int OUTPUT_ELEMENT = 11;
    
    public static final String[] ELEMENT_NAMES =
    {
        "binding", "collection", "format", "mapping", "namespace", "structure",
        "template", "value", "include", "split", "input", "output"
    };
    
    //
    // Instance data.
    
    /** Element type. */
    private final int m_type;
    
    /** Comment associated with element. */
    private String m_comment;
    
    /**
     * Constructor.
     * 
     * @param type element type code
     */
    protected ElementBase(int type) {
        m_type = type;
    }
    
    /**
     * Get element type.
     * 
     * @return type code for this element
     */
    public final int type() {
        return m_type;
    }
    
    /**
     * Get element name.
     * 
     * @return type code for this element
     */
    public final String name() {
        return ELEMENT_NAMES[m_type];
    }
    
    /**
     * Get element comment.
     * 
     * @return comment for this element
     */
    public final String getComment() {
        return m_comment;
    }
    
    /**
     * Set element comment.
     * 
     * @param text comment for this element
     */
    public final void setComment(String text) {
        m_comment = text;
    }
    
    /**
     * Validate attributes of element. This is designed to be called during
     * unmarshalling as part of the pre-set method processing when a subclass
     * instance is being created.
     *
     * @param ictx unmarshalling context
     * @param attrs attributes array
     */
    protected void validateAttributes(IUnmarshallingContext ictx,
        StringArray attrs) {
        
        // setup for attribute access
        int count = ictx.getStackDepth();
        BindingElement.UnmarshalWrapper wrapper =
            (BindingElement.UnmarshalWrapper)ictx.getStackObject(count-1);
        ValidationContext vctx = wrapper.getValidation();
        UnmarshallingContext uctx = (UnmarshallingContext)ictx;
        
        // loop through all attributes of current element
        for (int i = 0; i < uctx.getAttributeCount(); i++) {
            
            // check if nonamespace attribute is in the allowed set
            String name = uctx.getAttributeName(i);
            if (uctx.getAttributeNamespace(i).length() == 0) {
                if (attrs.indexOf(name) < 0) {
                    vctx.addWarning("Undefined attribute " + name, this);
                }
            }
        }
    }
    
    /**
     * Prevalidate element information. The prevalidation step is used to
     * check isolated aspects of an element, such as the settings for enumerated
     * values on the element and attributes. This empty base class
     * implementation should be overridden by each subclass that requires
     * prevalidation handling.
     * 
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {}
    
    /**
     * Validate element information. The validation step is used for checking
     * the interactions between elements, such as name references to other
     * elements. The {@link #prevalidate} method will always be called for every
     * element in the binding definition before this method is called for any
     * element. This empty base class implementation should be overridden by
     * each subclass that requires validation handling.
     * 
     * @param vctx validation context
     */
    public void validate(ValidationContext vctx) {}
    
    /**
     * Simple text representation of binding definition element. This uses the
     * element name, along with position information if present.
     * 
     * @return text representation
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append('<');
        buff.append(name());
        buff.append('>');
        buff.append(" element");
        if (this instanceof ITrackSource) {
            ITrackSource track = (ITrackSource)this;
            int line = track.jibx_getLineNumber();
            if (line >= 0) {
                buff.append(" (line ");
                buff.append(line);
                buff.append(", column ");
                buff.append(track.jibx_getColumnNumber());
                String dname = track.jibx_getDocumentName();
                if (dname == null) {
                    buff.append(')');
                } else {
                    buff.append(" in '");
                    buff.append(dname);
                    buff.append("')");
                }
            }
        }
        return buff.toString();
    }
}
/*
Copyright (c) 2004-2005, Dennis M. Sosnoski
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

import org.jibx.util.StringArray;

/**
 * Model component for <b>structure</b> attribute group in binding definition.
 *
 * @author Dennis M. Sosnoski
 */
public class StructureAttributes extends AttributeBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "allow-repeats", "choice", "flexible",
        "label", "ordered", "using" });
    
    //
    // Instance data.
    
    /** Flexible element handling flag. */
    private boolean m_isFlexible;
    
    /** Flag for ordered child content. */
    private boolean m_isOrdered;
    
    /** Flag for choice child content. */
    private boolean m_isChoice;
    
    /** Flag for repeated child elements to be ignored. */
    private boolean m_isAllowRepeats;
    
    /** Name for labeled child content defined elsewhere. */
    private String m_usingName;
    
    /** Name for labeled child content potentially referenced elsewhere. */
    private String m_labelName;

    /**
     * Constructor.
     */
    public StructureAttributes() {
        m_isOrdered = true;
    }
    
    /**
     * Get flexible flag.
     * 
     * @return flexible flag
     */
    public boolean isFlexible() {
        return m_isFlexible;
    }

    /**
     * Set flexible flag.
     * 
     * @param flexible
     */
    public void setFlexible(boolean flexible) {
        m_isFlexible = flexible;
    }

    /**
     * Check if child components are ordered.
     *
     * @return <code>true</code> if ordered, <code>false</code> if not
     */
    public boolean isOrdered() {
        return m_isOrdered;
    }
    
    /**
     * Set child components ordered flag.
     * 
     * @param ordered <code>true</code> if ordered, <code>false</code> if not
     */
    public void setOrdered(boolean ordered) {
        m_isOrdered = ordered;
    }

    /**
     * Check if child components are a choice.
     *
     * @return <code>true</code> if choice, <code>false</code> if not
     */
    public boolean isChoice() {
        return m_isChoice;
    }
    
    /**
     * Set child components choice flag.
     * 
     * @param choice <code>true</code> if choice, <code>false</code> if not
     */
    public void setChoice(boolean choice) {
        m_isChoice = choice;
    }

    /**
     * Check if repeated child elements are allowed.
     *
     * @return <code>true</code> if repeats allowed, <code>false</code> if not
     */
    public boolean isAllowRepeats() {
        return m_isAllowRepeats;
    }
    
    /**
     * Set repeated child elements allowed flag.
     * 
     * @param ignore <code>true</code> if repeated child elements to be allowed,
     * <code>false</code> if not
     */
    public void setAllowRepeats(boolean ignore) {
        m_isAllowRepeats = ignore;
    }
    
    /**
     * Get name for child component list definition.
     * 
     * @return text of name defining child components (<code>null</code> if
     * none)
     */
    public String getUsingName() {
        return m_usingName;
    }
    
    /**
     * Set name for child component list definition.
     * 
     * @param name text of name defining child components (<code>null</code> if
     * none)
     */
    public void setUsingName(String name) {
        m_usingName = name;
    }
    
    /**
     * Get label name for child component list.
     * 
     * @return label name text (<code>null</code> if none)
     */
    public String getLabelName() {
        return m_labelName;
    }
    
    /**
     * Set label name for child component list.
     * 
     * @param name label text for name (<code>null</code> if none)
     */
    public void setLabelName(String name) {
        m_labelName = name;
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.AttributeBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        if (m_isOrdered) {
            if (m_isChoice) {
                vctx.addError
                    ("choice='true' cannot be used with ordered children");
            }
            if (m_isFlexible) {
                vctx.addError
                    ("flexible='true' cannot be used with ordered children");
            }
            if (m_isAllowRepeats) {
                vctx.addError
                    ("allow-repeats='true' cannot be used with ordered children");
            }
        }
        if (m_isChoice && m_isFlexible) {
            vctx.addError
                ("choice='true' and flexible='true' cannot be used together");
        }
        if (m_isChoice && m_isAllowRepeats) {
            vctx.addError
                ("choice='true' and allow-repeats='true' cannot be used together");
        }
        super.prevalidate(vctx);
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.AttributeBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        if (m_usingName != null) {
            DefinitionContext dctx = vctx.getBindingRoot().getDefinitions();
            if (dctx.getNamedStructure(m_usingName) == null) {
                vctx.addError("Label \"" + m_usingName + "\" is not defined");
            } else {
                vctx.addWarning("The label/using approach is deprecated and " +
                    "will not be supported in the future - consider using an " +
                    "abstract mapping instead");
            }
        }
        if (m_labelName != null) {
            vctx.addWarning("The label/using approach is deprecated and " +
                "will not be supported in the future - consider using an " +
                "abstract mapping instead");
        }
        super.validate(vctx);
    }
}
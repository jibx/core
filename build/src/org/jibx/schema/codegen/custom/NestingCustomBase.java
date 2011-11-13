/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema.codegen.custom;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.QName;
import org.jibx.schema.elements.FacetElement;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.support.Conversions;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.LazyList;
import org.jibx.util.StringArray;

/**
 * Base class for all standard schema customizations that can contain other customizations.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class NestingCustomBase extends CustomBase implements TypeReplacer
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "any-handling", "choice-exposed", "choice-handling", "enforced-facets",
        "ignored-facets", "type-substitutions", "union-exposed", "union-handling" });
    
    //
    // Value set information
    
    public static final int ANY_DISCARD = 0;
    public static final int ANY_DOM = 1;
    public static final int ANY_MAPPED = 2;
    
    public static final EnumSet s_anyValues = new EnumSet(ANY_DISCARD,
        new String[] { "discard", "dom", "mapped"});
    
    public static final int SELECTION_UNCHECKED = 0;
    public static final int SELECTION_CHECKEDSET = 1;
    public static final int SELECTION_CHECKEDBOTH = 2;
    public static final int SELECTION_OVERRIDESET = 3;
    public static final int SELECTION_OVERRIDEBOTH = 4;
    
    public static final EnumSet s_selectionValues = new EnumSet(SELECTION_UNCHECKED,
        new String[] { "stateless", "checkset", "checkboth", "overset", "overboth" });
    
    //
    // Bound fields.
    
    /** List of type substitution pairs. */
    private QName[] m_substitutions;
    
    /** Mask for facets enforced at this level. */
    private long m_enforcedFacetsMask;
    
    /** Mask for facets ignored at this level. */
    private long m_ignoredFacetsMask;
    
    /** Expose choice selection state directly to user. */
    private Boolean m_choiceExposed;
    
    /** Code for xs:choice handling (<code>-1</code> if not set at level). */
    private int m_choiceCode = -1;
    
    /** Expose union selection state directly to user. */
    private Boolean m_unionExposed;
    
    /** Code for xs:union handling (<code>-1</code> if not set at level). */
    private int m_unionCode = -1;
    
    /** Code for xs:any representation (<code>-1</code> if not set at level). */
    private int m_anyCode = -1;
    
    /** Child customizations. */
    private final LazyList m_children;
    
    //
    // Constructed fields.
    
    /** Map of type substitutions. */
    private Map m_typeSubstitutionMap;
    
    /** Mask for facets active at this level (all facets not in scope of an ignore state). */
    private long m_activeFacetsMask;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public NestingCustomBase(NestingCustomBase parent) {
        super(parent);
        m_children = new LazyList();
    }

    /**
     * Get type substitution pairs list.
     *
     * @return substitutions
     */
    public QName[] getSubstitutions() {
        return m_substitutions;
    }
    
    /**
     * Set type substitution pairs list.
     *
     * @param subs
     */
    public void setSubstitutions(QName[] subs) {
        m_substitutions = subs;
    }
    
    /**
     * Set the list of facet elements to be enforced.
     *
     * @param facets
     * @param ictx
     */
    public void setEnforcedFacets(String[] facets, IUnmarshallingContext ictx) {
        ValidationContext vctx = (ValidationContext)ictx.getUserContext();
        long mask = 0;
        if (facets != null) {
            for (int i = 0; i < facets.length; i++) {
                String facet = facets[i];
                int index = Arrays.binarySearch(FacetElement.FACET_ELEMENT_NAMES, facet);
                if (index >= 0) {
                    mask |= SchemaBase.ELEMENT_MASKS[FacetElement.FACET_ELEMENT_INDEXES[index]];
                } else {
                    vctx.addError("'" + facet + "' is not a facet name", this);
                }
            }
        }
        m_enforcedFacetsMask = mask;
    }
    
    /**
     * Set the list of facet elements to be ignored.
     *
     * @param facets
     * @param ictx
     */
    public void setIgnoredFacets(String[] facets, IUnmarshallingContext ictx) {
        ValidationContext vctx = (ValidationContext)ictx.getUserContext();
        long mask = 0;
        if (facets != null) {
            for (int i = 0; i < facets.length; i++) {
                String facet = facets[i];
                int index = Arrays.binarySearch(FacetElement.FACET_ELEMENT_NAMES, facet);
                if (index >= 0) {
                    mask |= SchemaBase.ELEMENT_MASKS[FacetElement.FACET_ELEMENT_INDEXES[index]];
                } else {
                    vctx.addError("'" + facet + "' is not a facet name", this);
                }
            }
        }
        m_ignoredFacetsMask = mask;
    }
    
    /**
     * Get the bitmask of facet element flags to be processed.
     *
     * @return bitmask
     */
    public long getActiveFacetsMask() {
        return m_activeFacetsMask;
    }
    
    /**
     * Get child customizations.
     *
     * @return children
     */
    public LazyList getChildren() {
        return m_children;
    }
    
    /**
     * Set a type replacement.
     *
     * @param original
     * @param replace
     */
    protected void setReplacement(QName original, QName replace) {
        m_typeSubstitutionMap.put(original, replace);
    }
    
    /**
     * Get replacement type.
     *
     * @param qname
     * @return replacement type (<code>null</code> if deletion; original type, if no replacement defined)
     */
    public QName getReplacement(QName qname) {
        if (m_typeSubstitutionMap.containsKey(qname)) {
            return (QName)m_typeSubstitutionMap.get(qname);
        } else {
            return qname;
        }
    }
    
    /**
     * Get the xs:any handling type code to be applied for this component and all nested components. The default value
     * is {@link #ANY_DOM} if not overridden at any level.
     * 
     * @return code
     */
    public int getAnyType() {
        NestingCustomBase nest = this;
        while (nest != null) {
            if (nest.m_anyCode >= 0) {
                return nest.m_anyCode;
            } else {
                nest = nest.getParent();
            }
        }
        return ANY_DOM;
    }
    
    /**
     * Set the xs:any handling type code.
     * 
     * @param code handling code, <code>-1</code> if to be unset
     */
    public void setAnyType(int code) {
        if (code != -1) {
            s_anyValues.checkValue(code);
        }
        m_anyCode = code;
    }
    
    /**
     * Get the xs:any handling text value set specifically for this element.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getAnyHandling() {
        if (m_anyCode >= 0) {
            return s_anyValues.getName(m_anyCode);
        } else {
            return null;
        }
    }
    
    /**
     * Set the xs:any handling text value. This method is provided only for use when unmarshalling.
     * 
     * @param text (<code>null</code> if not set)
     * @param ictx
     */
    private void setAnyHandling(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            m_anyCode = Conversions.convertEnumeration(text, s_anyValues, "any-handling", ictx);
        }
    }
    
    /**
     * Check whether xs:choice selection states should be exposed to the user. The default is <code>false</code> if not
     * overridden at any level.
     *
     * @return expose choice state flag
     */
    public boolean isChoiceExposed() {
        NestingCustomBase nest = this;
        while (nest != null) {
            if (nest.m_choiceExposed != null) {
                return nest.m_choiceExposed.booleanValue();
            } else {
                nest = nest.getParent();
            }
        }
        return false;
    }
    
    /**
     * Get the xs:choice handling type code to be applied for this component and all nested components. The default
     * value is {@link #SELECTION_CHECKEDSET} if not overridden at any level.
     * 
     * @return code
     */
    public int getChoiceType() {
        NestingCustomBase nest = this;
        while (nest != null) {
            if (nest.m_choiceCode >= 0) {
                return nest.m_choiceCode;
            } else {
                nest = nest.getParent();
            }
        }
        return SELECTION_CHECKEDSET;
    }
    
    /**
     * Set the xs:choice handling type code.
     * 
     * @param code handling code, <code>-1</code> if to be unset
     */
    public void setChoiceType(int code) {
        if (code != -1) {
            s_selectionValues.checkValue(code);
        }
        m_choiceCode = code;
    }
    
    /**
     * Get the xs:choice handling text value set specifically for this element.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getChoiceHandling() {
        if (m_choiceCode >= 0) {
            return s_selectionValues.getName(m_choiceCode);
        } else {
            return null;
        }
    }
    
    /**
     * Set the xs:choice handling text value. This method is provided only for use when unmarshalling.
     * 
     * @param text (<code>null</code> if not set)
     * @param ictx
     */
    private void setChoiceHandling(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            m_choiceCode = Conversions.convertEnumeration(text, s_selectionValues, "choice-handling", ictx);
        }
    }
    
    /**
     * Check whether xs:union selection states should be exposed to the user. The default is <code>false</code> if not
     * overridden at any level.
     *
     * @return expose union state flag
     */
    public boolean isUnionExposed() {
        NestingCustomBase nest = this;
        while (nest != null) {
            if (nest.m_unionExposed != null) {
                return nest.m_unionExposed.booleanValue();
            } else {
                nest = nest.getParent();
            }
        }
        return false;
    }
    
    /**
     * Get the xs:union handling type code to be applied for this component and all nested components. The default
     * value is {@link #SELECTION_CHECKEDSET} if not overridden at any level.
     * 
     * @return code
     */
    public int getUnionType() {
        NestingCustomBase nest = this;
        while (nest != null) {
            if (nest.m_unionCode >= 0) {
                return nest.m_unionCode;
            } else {
                nest = nest.getParent();
            }
        }
        return SELECTION_CHECKEDSET;
    }
    
    /**
     * Set the xs:union handling type code.
     * 
     * @param code handling code, <code>-1</code> if to be unset
     */
    public void setUnionType(int code) {
        if (code != -1) {
            s_selectionValues.checkValue(code);
        }
        m_unionCode = code;
    }
    
    /**
     * Get the xs:union handling text value set specifically for this element.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getUnionHandling() {
        if (m_unionCode >= 0) {
            return s_selectionValues.getName(m_unionCode);
        } else {
            return null;
        }
    }
    
    /**
     * Set the xs:union handling text value. This method is provided only for use when unmarshalling.
     * 
     * @param text (<code>null</code> if not set)
     * @param ictx
     */
    private void setUnionHandling(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            m_unionCode = Conversions.convertEnumeration(text, s_selectionValues, "union-handling", ictx);
        }
    }
    
    /**
     * Check whether xs:attribute definitions should always be inlined, even when used in multiple locations. The
     * default is <code>false</code> if not overridden at any level.
     * TODO: implement in customizations
     *
     * @return inline attribute flag
     */
    public boolean isAttributeInlined() {
/*        NestingCustomBase nest = this;
        while (nest != null) {
            if (nest.m_choiceExposed != null) {
                return nest.m_choiceExposed.booleanValue();
            } else {
                nest = nest.getParent();
            }
        }   */
        return false;
    }
    
    /**
     * Validate and finalize customization information. This creates a new type substitution map and active facets mask,
     * or inherits unchanged values from the parent customization.
     *
     * @param vctx validation context
     * @return <code>true</code> if valid, <code>false</code> if not
     */
    public boolean validate(ValidationContext vctx) {
        NestingCustomBase parent = getParent();
        if (m_substitutions == null || m_substitutions.length == 0) {
            m_typeSubstitutionMap = parent.m_typeSubstitutionMap;
        } else if ((m_substitutions.length % 2) == 0) {
            if (parent == null) {
                m_typeSubstitutionMap = new HashMap();
            } else {
                m_typeSubstitutionMap = new HashMap(parent.m_typeSubstitutionMap);
            }
            for (int i = 0; i < m_substitutions.length; i += 2) {
                m_typeSubstitutionMap.put(m_substitutions[i], m_substitutions[i+1]);
            }
        } else {
            vctx.addError("Type substitution list must be pairs, not an odd number of names", this);
        }
        // TODO: implement the facet handling
        m_activeFacetsMask = SchemaBase.ELEMENT_MASKS[SchemaBase.ENUMERATION_TYPE];
        return true;
    }
}
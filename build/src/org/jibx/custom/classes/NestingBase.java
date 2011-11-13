/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.custom.classes;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.Utility;
import org.jibx.util.StringArray;
import org.jibx.util.Types;

/**
 * Base class for all standard binding customizations that can contain other customizations.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class NestingBase extends SharedNestingBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "force-mapping", "force-names", "map-abstract", "property-access",
            "strip-prefixes", "strip-suffixes", "value-style", "wrap-collections" },
            SharedNestingBase.s_allowedAttributes);
    
    // value style value set information
    public static final int ATTRIBUTE_VALUE_STYLE = 0;
    
    public static final int ELEMENT_VALUE_STYLE = 1;
    
    public static final Integer ATTRIBUTE_STYLE_INTEGER = new Integer(ATTRIBUTE_VALUE_STYLE);
    
    public static final Integer ELEMENT_STYLE_INTEGER = new Integer(ELEMENT_VALUE_STYLE);
    
    public static final EnumSet s_valueStyleEnum =
        new EnumSet(ATTRIBUTE_VALUE_STYLE, new String[] { "attribute", "element" });
    
    // values inherited through nesting
    private Integer m_valueStyle;
    
    private Boolean m_propertyAccess;
    
    private String[] m_stripPrefixes;
    
    private String[] m_stripSuffixes;
    
    private Boolean m_mapAbstract;
    
    private Boolean m_wrapCollections;
    
    private Boolean m_forceMapping;
    
    private Boolean m_forceNames;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public NestingBase(SharedNestingBase parent) {
        super(parent);
    }
    
    //
    // Access methods for values inherited through nesting
    
    /**
     * Check abstract mapping flag. If not set at any nesting level, the default is <code>true</code>.
     * 
     * @return abstract mapping flag
     */
    public boolean isMapAbstract() {
        if (m_mapAbstract == null) {
            if (getParent() == null) {
                return true;
            } else {
                return ((NestingBase)getParent()).isMapAbstract();
            }
        } else {
            return m_mapAbstract.booleanValue();
        }
    }
    
    /**
     * Set abstract mapping flag.
     * 
     * @param abs
     */
    public void setMapAbstract(Boolean abs) {
        m_mapAbstract = abs;
    }
    
    /**
     * Check force mapping flag. If not set at any nesting level, the default is <code>false</code>.
     * 
     * @return force mapping flag
     */
    public boolean isForceMapping() {
        if (m_forceMapping == null) {
            if (getParent() == null) {
                return false;
            } else {
                return ((NestingBase)getParent()).isForceMapping();
            }
        } else {
            return m_forceMapping.booleanValue();
        }
    }
    
    /**
     * Check force structure names flag. If not set at any nesting level, the default is <code>true</code>.
     * 
     * @return force names flag
     */
    public boolean isForceStructureNames() {
        if (m_forceNames == null) {
            if (getParent() == null) {
                return true;
            } else {
                return ((NestingBase)getParent()).isForceStructureNames();
            }
        } else {
            return m_forceNames.booleanValue();
        }
    }
    
    /**
     * Check wrap collections flag. If not set at any nesting level, the default is <code>false</code>.
     * 
     * @return wrap collections flag
     */
    public boolean isWrapCollections() {
        if (m_wrapCollections == null) {
            if (getParent() == null) {
                return false;
            } else {
                return ((NestingBase)getParent()).isWrapCollections();
            }
        } else {
            return m_wrapCollections.booleanValue();
        }
    }
    
    /**
     * Check property access mode flag. If not set at any nesting level, the default is <code>false</code>.
     * 
     * @return <code>true</code> if bean-style get/set methods to be used, <code>false</code> if fields to be used
     * directly
     */
    public boolean isPropertyAccess() {
        if (m_propertyAccess == null) {
            if (getParent() == null) {
                return false;
            } else {
                return ((NestingBase)getParent()).isPropertyAccess();
            }
        } else {
            return m_propertyAccess.booleanValue();
        }
    }
    
    /**
     * Get prefixes to be stripped from field names.
     * 
     * @return strip prefixes (<code>null</code> if none)
     */
    public String[] getStripPrefixes() {
        if (m_stripPrefixes == null) {
            if (getParent() == null) {
                return Utility.EMPTY_STRING_ARRAY;
            } else {
                return ((NestingBase)getParent()).getStripPrefixes();
            }
        } else {
            return m_stripPrefixes;
        }
    }
    
    /**
     * Get suffixes to be stripped from field names.
     * 
     * @return strip suffix (<code>null</code> if none)
     */
    public String[] getStripSuffixes() {
        if (m_stripSuffixes == null) {
            if (getParent() == null) {
                return Utility.EMPTY_STRING_ARRAY;
            } else {
                return ((NestingBase)getParent()).getStripSuffixes();
            }
        } else {
            return m_stripSuffixes;
        }
    }
    
    /**
     * Get value style code.
     * 
     * @param type value type name
     * @return value from {@link NestingBase#s_valueStyleEnum} enumeration
     */
    public int getValueStyle(String type) {
        if (m_valueStyle == null) {
            if (getParent() == null) {
                if (!"java.lang.String".equals(type) && !"byte[]".equals(type) && Types.isSimpleValue(type)) {
                    return ATTRIBUTE_VALUE_STYLE;
                } else {
                    return ELEMENT_VALUE_STYLE;
                }
            } else {
                return ((NestingBase)getParent()).getValueStyle(type);
            }
        } else {
            return m_valueStyle.intValue();
        }
    }
    
    /**
     * Set value style.
     * 
     * @param style (<code>null</code> if none at this level)
     */
    public void setValueStyle(Integer style) {
        m_valueStyle = style;
    }
    
    /**
     * Value style set text method. This is intended for use during unmarshalling. TODO: add validation
     * 
     * @param text
     * @param ictx
     */
    private void setValueStyleText(String text, IUnmarshallingContext ictx) {
        if (text == null) {
            m_valueStyle = null;
        } else {
            m_valueStyle = new Integer(s_valueStyleEnum.getValue(text));
        }
    }
    
    /**
     * Value style get text method. This is intended for use during marshalling.
     * 
     * @return text
     */
    private String getValueStyleText() {
        if (m_valueStyle == null) {
            return null;
        } else {
            return s_valueStyleEnum.getName(m_valueStyle.intValue());
        }
    }
}
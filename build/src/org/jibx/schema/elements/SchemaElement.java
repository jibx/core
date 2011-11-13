/*
Copyright (c) 2006-2007, Dennis M. Sosnoski
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

package org.jibx.schema.elements;

import org.apache.log4j.Logger;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.ISchemaResolver;
import org.jibx.schema.NameRegister;
import org.jibx.schema.TreeWalker;
import org.jibx.schema.attributes.FormChoiceAttribute;
import org.jibx.schema.support.Conversions;
import org.jibx.schema.types.AllEnumSet;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.LazyList;
import org.jibx.util.StringArray;

/**
 * Model component for <b>schema</b> element.
 *
 * @author Dennis M. Sosnoski
 */
public class SchemaElement extends OpenAttrBase
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(TreeWalker.class.getName());
    
    /** List of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "attributeFormDefault", "blockDefault",
        "elementFormDefault", "finalDefault", "id", "targetNamespace",
        "version" });
    
    /** Mask bits for schema reference child elements. */
    private static final long SCHEMA_REFERENCE_MASK = ELEMENT_MASKS[ANNOTATION_TYPE] |
        ELEMENT_MASKS[INCLUDE_TYPE] | ELEMENT_MASKS[IMPORT_TYPE] |
        ELEMENT_MASKS[REDEFINE_TYPE];
    
    /** Mask bits for top-level definition child elements. */
    private static final long TOP_LEVEL_DEFINITION_MASK = ELEMENT_MASKS[ANNOTATION_TYPE] |
        ELEMENT_MASKS[ATTRIBUTE_TYPE] | ELEMENT_MASKS[ATTRIBUTEGROUP_TYPE] |
        ELEMENT_MASKS[COMPLEXTYPE_TYPE] | ELEMENT_MASKS[ELEMENT_TYPE] |
        ELEMENT_MASKS[GROUP_TYPE] | ELEMENT_MASKS[NOTATION_TYPE] |
        ELEMENT_MASKS[SIMPLETYPE_TYPE];
    
    //
    // Instance data
    
    /** Filtered list of schema reference child elements. */
    private final FilteredSegmentList m_schemaChildren;
    
    /** Filtered list of top-level definition child elements. */
    private final FilteredSegmentList m_topLevelChildren;
    
    /** Schemas which directly reference this schema. */
    private final LazyList m_dependentSchemas;
    
    /** 'attributeFormDefault' attribute value (<code>-1</code> if not set). */
    private int m_attributeFormDefaultType;
    
    /** 'elementFormDefault' attribute value (<code>-1</code> if not set). */
    private int m_elementFormDefaultType;
    
    /** 'blockDefault' attribute value. */
    private AllEnumSet m_blockDefault;
    
    /** 'finalDefault' attribute value. */
    private AllEnumSet m_finalDefault;
    
    /** "id" attribute value. */
    private String m_id;
    
    /** 'targetNamespace' attribute value. */
    private String m_targetNamespace;
    
    /** Effective namespace overriding the target namespace (<code>null</code> if unused). */
    private String m_effectiveNamespace;
    
    /** 'version' attribute value. */
    private String m_version;
    
    /** Resolver for this schema. */
    private ISchemaResolver m_resolver;
    
    /** Register for names from this context. */
    private NameRegister m_register;

    /**
     * Constructor.
     */
    public SchemaElement() {
    	super(SCHEMA_TYPE);
        m_schemaChildren = new FilteredSegmentList(getChildrenWritable(),
            SCHEMA_REFERENCE_MASK, this);
        m_topLevelChildren = new FilteredSegmentList(getChildrenWritable(),
            TOP_LEVEL_DEFINITION_MASK, m_schemaChildren, this);
        m_dependentSchemas = new LazyList();
        m_blockDefault = new AllEnumSet(ElementElement.s_blockValues,
            "blockDefault");
        m_finalDefault = new AllEnumSet(ElementElement.s_derivationValues,
            "finalDefault");
        m_register = new NameRegister();
        m_attributeFormDefaultType = -1;
        m_elementFormDefaultType = -1;
    }
    
    //
    // Base class overrides

    /* (non-Javadoc)
     * @see org.jibx.schema.ElementBase#preset(org.jibx.runtime.IUnmarshallingContext)
     */
    protected void preset(IUnmarshallingContext ictx) throws JiBXException {
        validateAttributes(ictx, s_allowedAttributes);
        super.preset(ictx);
    }
    
    //
    // Access methods
    
    /**
     * Get list of schema-related child elements.
     *
     * @return child list
     */
    public FilteredSegmentList getSchemaChildren() {
        return m_schemaChildren;
    }

    /**
     * Get list of top-level definition child elements.
     *
     * @return child list
     */
    public FilteredSegmentList getTopLevelChildren() {
        return m_topLevelChildren;
    }
    
    /**
     * Get 'attributeFormDefault' attribute type code.
     * 
     * @return type
     */
    public int getAttributeFormDefault() {
        return m_attributeFormDefaultType;
    }
    
    /**
     * Set 'attributeFormDefault' attribute type code.
     * 
     * @param type
     */
    public void setAttributeFormDefault(int type) {
        FormChoiceAttribute.s_formValues.checkValue(type);
        m_attributeFormDefaultType = type;
    }
    
    /**
     * Get 'attributeFormDefault' attribute text.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getAttributeFormDefaultText() {
        if (m_attributeFormDefaultType >= 0) {
            return FormChoiceAttribute.s_formValues.
                getName(m_attributeFormDefaultType);
        } else {
            return null;
        }
    }
    
    /**
     * Set 'attributeFormDefault' attribute text. This method is provided only
     * for use when unmarshalling.
     * 
     * @param text
     * @param ictx
     */
    private void setAttributeFormDefaultText(String text,
        IUnmarshallingContext ictx) {
        m_attributeFormDefaultType = Conversions.convertEnumeration(text,
            FormChoiceAttribute.s_formValues, "attributeFormDefault", ictx);
    }
    
    /**
     * Get 'elementFormDefault' attribute type code.
     * 
     * @return type
     */
    public int getElementFormDefault() {
        return m_elementFormDefaultType;
    }
    
    /**
     * Set 'elementFormDefault' attribute type code.
     * 
     * @param type
     */
    public void setElementFormDefault(int type) {
        FormChoiceAttribute.s_formValues.checkValue(type);
        m_elementFormDefaultType = type;
    }
    
    /**
     * Get 'elementFormDefault' attribute text.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getElementFormDefaultText() {
        if (m_elementFormDefaultType >= 0) {
            return FormChoiceAttribute.s_formValues.
                getName(m_elementFormDefaultType);
        } else {
            return null;
        }
    }
    
    /**
     * Set 'elementFormDefault' attribute text. This method is provided only
     * for use when unmarshalling.
     * 
     * @param text
     * @param ictx
     */
    private void setElementFormDefaultText(String text,
        IUnmarshallingContext ictx) {
        m_elementFormDefaultType = Conversions.convertEnumeration(text,
            FormChoiceAttribute.s_formValues, "elementFormDefault", ictx);
    }

    /**
     * Get 'blockDefault' attribute.
     * 
     * @return block default
     */
    public AllEnumSet getBlock() {
        return m_blockDefault;
    }

    /**
     * Get 'finalDefault' attribute.
     * 
     * @return final default
     */
    public AllEnumSet getFinal() {
        return m_finalDefault;
    }
    
    /**
     * Get 'targetNamespace' attribute. Note that for most purposes the
     * {@link #getEffectiveNamespace()} method should be used instead, since
     * that method returns the namespace which should be used for definitions
     * within this schema.
     * 
     * @return target namespace (<code>null</code> if none)
     */
    public String getTargetNamespace() {
        return m_targetNamespace;
    }
    
    /**
     * Set 'targetNamespace' attribute.
     * 
     * @param tns target namespace (<code>null</code> if none)
     */
    public void setTargetNamespace(String tns) {
        m_targetNamespace = tns;
    }
    
    /**
     * Get the effective namespace which applies to this schema. This will
     * differ from the 'targetNamespace' attribute in the case where a
     * no-namespace schema is included (directly or indirectly) into a
     * namespaced schema.
     *
     * @return effective namespace (<code>null</code> if none)
     */
    public String getEffectiveNamespace() {
        if (m_targetNamespace == null) {
            return m_effectiveNamespace;
        } else {
            return m_targetNamespace;
        }
    }
    
    /**
     * Set the effective namespace to be applied to this schema.
     *
     * @param ens effective namespace (<code>null</code> if the same as 'targetNamespace' attribute)
     */
    public void setEffectiveNamespace(String ens) {
        m_effectiveNamespace = ens;
    }
    
    /**
     * Get 'version' attribute.
     * 
     * @return version
     */
    public String getVersion() {
        return m_version;
    }
    
    /**
     * Set 'version' attribute.
     * 
     * @param version
     */
    public void setVersion(String version) {
        m_version = version;
    }
    
    /**
     * Get resolver.
     *
     * @return resolver
     */
    public ISchemaResolver getResolver() {
        return m_resolver;
    }
    
    /**
     * Set resolver.
     *
     * @param resolver
     */
    public void setResolver(ISchemaResolver resolver) {
        m_resolver = resolver;
    }

    /**
     * Get register for named components of schema.
     *
     * @return register
     */
    public NameRegister getRegister() {
        return m_register;
    }
    
    //
    // Convenience methods based on defaults

    /**
     * Check if elements are qualified by default.
     * 
     * @return <code>true</code> if qualified, <code>false</code> if not
     */
    public boolean isElementQualifiedDefault() {
        return m_elementFormDefaultType == FormChoiceAttribute.QUALIFIED_FORM;
    }
    
    /**
     * Check if attributes are qualified by default.
     * 
     * @return <code>true</code> if qualified, <code>false</code> if not
     */
    public boolean isAttributeQualifiedDefault() {
        return m_attributeFormDefaultType == FormChoiceAttribute.QUALIFIED_FORM;
    }
    
    //
    // Validation methods
    
    /**
     * Get the schema name for logging validation.
     *
     * @return name
     */
    private String getName() {
        ISchemaResolver resolver = getResolver();
        if (resolver == null) {
            return "{no name}";
        } else {
            return resolver.getName();
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.schema.ComponentBase#validate(org.jibx.schema.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        s_logger.debug("Validating schema " + getName());
        super.validate(vctx);
    }

    public void prevalidate(ValidationContext vctx) {
        s_logger.debug("Prevalidating schema " + getName());
        super.prevalidate(vctx);
    }
}
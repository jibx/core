/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski. All rights reserved.
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

import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.schema.elements.FacetElement;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.validation.ProblemLocation;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.StringArray;

/**
 * Unmarshaller class for all nested customizations. This is used for all the customizations below the &lt;schema>
 * level.
 */
public class ComponentCustomUnmarshaller implements IUnmarshaller
{
    /** Attribute names allowed for all types of components. */
    public static final StringArray s_baseAttributes =
        new StringArray(new String[] { "inline", "path", "position" }, NestingCustomBase.s_allowedAttributes);
    
    /** Attribute names allowed for all excludable components. */
    public static final StringArray s_ignorableAttributes =
        new StringArray(new String[] { "exclude" }, s_baseAttributes);
    
    /** Mask for elements with values but no names. */
    private static final long s_unnamedValueMask;
    
    /** Allowed attribute names for customizing elements with values but no names. */
    public static final StringArray s_unnamedValueAttributes =
        new StringArray(new String[] { "class-name", "type", "value-name" }, s_ignorableAttributes);
    
    /** Mask for elements with values and names. */
    private static final long s_namedValueMask;
    
    /** Allowed attribute names for customizing elements with values and names. */
    public static final StringArray s_namedValueAttributes =
        new StringArray(new String[] { "name" }, s_unnamedValueAttributes);
    
    /** Mask for elements with names which can be ignored. */
    private static final long s_namedIgnorableValueMask;
    
    /** Allowed attribute names for customizing elements with values and names. */
    public static final StringArray s_namedIgnorableValueAttributes =
        new StringArray(new String[] { "ignore" }, s_namedValueAttributes);
    
    /** Mask for type definition elements. */
    private static final long s_typeDefinitionMask;
    
    /** Allowed attribute names for type definition elements. */
    public static final StringArray s_typeDefinitionAttributes =
        new StringArray(new String[] { "class-name", "name" }, s_ignorableAttributes);
    
    /** Mask for elements which are not deletable but do support nesting. */
    private static final long s_simpleNestingMask;
    
    /** Mask for elements which are deletable but do not support nesting. */
    private static final long s_deletableLeafMask;
    
    static {
        long[] masks = SchemaBase.ELEMENT_MASKS;
        long mask = masks[SchemaBase.ATTRIBUTE_TYPE];
        mask |= masks[SchemaBase.ELEMENT_TYPE];
        s_namedIgnorableValueMask = mask;
        mask |= masks[SchemaBase.ATTRIBUTEGROUP_TYPE];
        mask |= masks[SchemaBase.GROUP_TYPE];
        s_namedValueMask = mask;
        mask = masks[SchemaBase.ALL_TYPE];
        mask |= masks[SchemaBase.CHOICE_TYPE];
        mask |= masks[SchemaBase.SEQUENCE_TYPE];
        s_unnamedValueMask = mask;
        mask = masks[SchemaBase.COMPLEXTYPE_TYPE];
        mask |= masks[SchemaBase.SIMPLETYPE_TYPE];
        s_typeDefinitionMask = mask;
        mask = SchemaBase.COMPLEXCONTENT_TYPE;
        mask |= masks[SchemaBase.EXTENSION_TYPE];
        mask |= masks[SchemaBase.LIST_TYPE];
        mask |= masks[SchemaBase.RESTRICTION_TYPE];
        mask |= masks[SchemaBase.SIMPLECONTENT_TYPE];
        mask |= masks[SchemaBase.UNION_TYPE];
        s_simpleNestingMask = mask;
        mask = FacetElement.FACET_ELEMENT_MASK;
        mask |= masks[SchemaBase.ANY_TYPE];
        mask |= masks[SchemaBase.ANYATTRIBUTE_TYPE];
        s_deletableLeafMask = mask;
    };
    
    /**
     * Check if element present. If there's a start tag, we want to handle it.
     *
     * @param ctx
     * @return <code>true</code> if at a start tag
     * @throws JiBXException
     */
    public boolean isPresent(IUnmarshallingContext ctx) throws JiBXException {
        return ctx.isStart();
    }

    /**
     * Unmarshal the element. This matches the current start tag name to the corresponding schema component element,
     * then unmarshals the content based on the type of schema element (invoking the abstract unmarshaller defined
     * in the binding for the actual content).
     *
     * @param obj ignored
     * @param ictx unmarshalling context
     * @return unmarshalled instance
     * @throws JiBXException on error in document
     */
    public Object unmarshal(Object obj, IUnmarshallingContext ictx) throws JiBXException {
        
        // make sure current element name matches an allowed schema element
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        String name = ctx.getElementName();
        int index = Arrays.binarySearch(SchemaBase.ELEMENT_NAMES, name);
        if (index >= 0) {
            
            // check the type of element
            long mask = SchemaBase.ELEMENT_MASKS[index];
            StringArray attrs = null;
            if ((mask & s_namedIgnorableValueMask) != 0) {
                attrs = s_namedIgnorableValueAttributes;
            } else if ((mask & s_namedValueMask) != 0) {
                attrs = s_namedValueAttributes;
            } else if ((mask & s_unnamedValueMask) != 0) {
                attrs = s_unnamedValueAttributes;
            } else if ((mask & s_typeDefinitionMask) != 0) {
                attrs = s_typeDefinitionAttributes;
            } else if ((mask & s_simpleNestingMask) != 0) {
                attrs = s_baseAttributes;
            } else if ((mask & s_deletableLeafMask) != 0) {
                attrs = s_ignorableAttributes;
            }
            if (attrs != null) {
                
                // create an instance and unmarshal with the appropriate handling
                ComponentCustom comp = new ComponentCustom(name,
                    (NestingCustomBase)CustomBase.getContainingObject(ictx));
                comp.validateAttributes(ictx, attrs);
                ctx.getUnmarshaller("component-custom").unmarshal(comp, ctx);
                ctx.parsePastCurrentEndTag(null, name);
                return comp;
                
            } else {
                
                // report a validation error for unsupported element
                ValidationContext vctx = (ValidationContext)ictx.getUserContext();
                vctx.addFatal("No customizations allowed", new ProblemLocation(ictx));
                
            }
        } else {
            
            // report a validation error for unknown element
            ValidationContext vctx = (ValidationContext)ictx.getUserContext();
            vctx.addFatal("Unknown element", new ProblemLocation(ictx));
            
        }
        ctx.skipElement();
        return null;
    }
}
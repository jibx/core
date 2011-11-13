/*
 * Copyright (c) 2006-2007, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jibx.schema.elements.AllElement;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AnnotationElement;
import org.jibx.schema.elements.AnyAttributeElement;
import org.jibx.schema.elements.AnyElement;
import org.jibx.schema.elements.AppInfoElement;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupElement;
import org.jibx.schema.elements.AttributeGroupRefElement;
import org.jibx.schema.elements.ChoiceElement;
import org.jibx.schema.elements.ComplexContentElement;
import org.jibx.schema.elements.ComplexExtensionElement;
import org.jibx.schema.elements.ComplexRestrictionElement;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.DocumentationElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.FacetElement;
import org.jibx.schema.elements.GroupElement;
import org.jibx.schema.elements.GroupRefElement;
import org.jibx.schema.elements.ImportElement;
import org.jibx.schema.elements.IncludeElement;
import org.jibx.schema.elements.KeyBase;
import org.jibx.schema.elements.ListElement;
import org.jibx.schema.elements.NotationElement;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.RedefineElement;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SchemaLocationBase;
import org.jibx.schema.elements.SelectionBase;
import org.jibx.schema.elements.SequenceElement;
import org.jibx.schema.elements.SimpleContentElement;
import org.jibx.schema.elements.SimpleExtensionElement;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.elements.UnionElement;

/**
 * Handles walking the tree structure of schema model. This traverses the structure defined by the nesting of elements
 * and schema references in the XML representation.
 * 
 * @author Dennis M. Sosnoski
 */
public class TreeWalker
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(TreeWalker.class.getName());
    
    /** Selector for elements to be skipped when walking tree (<code>null</code> if unused). */
    private final ISkipElements m_skipSet;
    
    /** Listener for entering and exiting referenced schemas. (<code>null</code> if unused). */
    private final ISchemaListener m_schemaListener;
    
    /**
     * Constructor.
     * 
     * @param skip selector for elements to be skipped (<code>null</code> if none skipped)
     * @param listen schema reference listener (<code>null</code> if none)
     */
    public TreeWalker(ISkipElements skip, ISchemaListener listen) {
        m_skipSet = skip;
        m_schemaListener = listen;
    }
    
    /**
     * Control the logging level for this class. Since the generated logs at debug level can become huge, this gives a
     * way for external code to provide granular control over the logging.
     *
     * @param level
     * @return prior level
     */
    public static Level setLogging(Level level) {
        Level prior = s_logger.getLevel();
        s_logger.setLevel(level);
        return prior;
    }
    
    /**
     * Walk entire schema model.
     * 
     * @param schema root element of schema to be traversed
     * @param visitor target visitor for element notifications
     */
    public void walkSchema(SchemaElement schema, SchemaVisitor visitor) {
        if (schema != null) {
            if (m_schemaListener == null || m_schemaListener.enterSchema(schema)) {
                walkElement(schema, visitor);
                if (m_schemaListener != null) {
                    m_schemaListener.exitSchema();
                }
            }
        }
    }
    
    /**
     * Walk schema model element tree. This recursively traverses the schema model tree rooted in the supplied element,
     * including the element itself, notifying the visitor of each element visited during the traversal.
     * 
     * @param root node of tree to be toured
     * @param visitor target visitor for element notifications
     */
    public void walkElement(SchemaBase root, SchemaVisitor visitor) {
        
        // check for fatal error on element
        if (m_skipSet != null && m_skipSet.isSkipped(root)) {
            if (s_logger.isDebugEnabled() && root instanceof OpenAttrBase) {
                s_logger.debug("Skipping node " + SchemaUtils.componentPath((OpenAttrBase)root));
            }
            return;
        }
        
        // visit the actual root of tree
        boolean expand = false;
        if (s_logger.isDebugEnabled() && root instanceof OpenAttrBase) {
            s_logger.debug("Entering node " + SchemaUtils.componentPath((OpenAttrBase)root));
        }
        switch (root.type()) {
            
            case SchemaBase.ALL_TYPE:
                expand = visitor.visit((AllElement)root);
                break;
            
            case SchemaBase.ANNOTATION_TYPE:
                expand = visitor.visit((AnnotationElement)root);
                break;
            
            case SchemaBase.ANY_TYPE:
                expand = visitor.visit((AnyElement)root);
                break;
            
            case SchemaBase.ANYATTRIBUTE_TYPE:
                expand = visitor.visit((AnyAttributeElement)root);
                break;
            
            case SchemaBase.APPINFO_TYPE:
                expand = visitor.visit((AppInfoElement)root);
                break;
            
            case SchemaBase.ATTRIBUTE_TYPE:
                expand = visitor.visit((AttributeElement)root);
                break;
            
            case SchemaBase.ATTRIBUTEGROUP_TYPE:
                if (root instanceof AttributeGroupElement) {
                    expand = visitor.visit((AttributeGroupElement)root);
                } else {
                    expand = visitor.visit((AttributeGroupRefElement)root);
                }
                break;
            
            case SchemaBase.CHOICE_TYPE:
                expand = visitor.visit((ChoiceElement)root);
                break;
            
            case SchemaBase.COMPLEXCONTENT_TYPE:
                expand = visitor.visit((ComplexContentElement)root);
                break;
            
            case SchemaBase.COMPLEXTYPE_TYPE:
                expand = visitor.visit((ComplexTypeElement)root);
                break;
            
            case SchemaBase.DOCUMENTATION_TYPE:
                expand = visitor.visit((DocumentationElement)root);
                break;
            
            case SchemaBase.ELEMENT_TYPE:
                expand = visitor.visit((ElementElement)root);
                break;
            
            case SchemaBase.EXTENSION_TYPE:
                if (root instanceof ComplexExtensionElement) {
                    expand = visitor.visit((ComplexExtensionElement)root);
                } else {
                    expand = visitor.visit((SimpleExtensionElement)root);
                }
                break;
            
            case SchemaBase.FIELD_TYPE:
                expand = visitor.visit((SelectionBase.FieldElement)root);
                break;
            
            case SchemaBase.GROUP_TYPE:
                if (root instanceof GroupElement) {
                    expand = visitor.visit((GroupElement)root);
                } else {
                    expand = visitor.visit((GroupRefElement)root);
                }
                break;
            
            case SchemaBase.IMPORT_TYPE:
                expand = visitor.visit((ImportElement)root);
                break;
            
            case SchemaBase.INCLUDE_TYPE:
                expand = visitor.visit((IncludeElement)root);
                break;
            
            case SchemaBase.KEY_TYPE:
                expand = visitor.visit((KeyBase.KeyElement)root);
                break;
            
            case SchemaBase.KEYREF_TYPE:
                expand = visitor.visit((KeyBase.KeyrefElement)root);
                break;
            
            case SchemaBase.LIST_TYPE:
                expand = visitor.visit((ListElement)root);
                break;
            
            case SchemaBase.NOTATION_TYPE:
                expand = visitor.visit((NotationElement)root);
                break;
            
            case SchemaBase.REDEFINE_TYPE:
                expand = visitor.visit((RedefineElement)root);
                break;
            
            case SchemaBase.RESTRICTION_TYPE:
                if (root instanceof SimpleRestrictionElement) {
                    expand = visitor.visit((SimpleRestrictionElement)root);
                } else {
                    expand = visitor.visit((ComplexRestrictionElement)root);
                }
                break;
            
            case SchemaBase.SCHEMA_TYPE:
                expand = visitor.visit((SchemaElement)root);
                break;
            
            case SchemaBase.SELECTOR_TYPE:
                expand = visitor.visit((SelectionBase.SelectorElement)root);
                break;
            
            case SchemaBase.SEQUENCE_TYPE:
                expand = visitor.visit((SequenceElement)root);
                break;
            
            case SchemaBase.SIMPLECONTENT_TYPE:
                expand = visitor.visit((SimpleContentElement)root);
                break;
            
            case SchemaBase.SIMPLETYPE_TYPE:
                expand = visitor.visit((SimpleTypeElement)root);
                break;
            
            case SchemaBase.UNION_TYPE:
                expand = visitor.visit((UnionElement)root);
                break;
            
            case SchemaBase.UNIQUE_TYPE:
                expand = visitor.visit((KeyBase.UniqueElement)root);
                break;
            
            case SchemaBase.ENUMERATION_TYPE:
            case SchemaBase.FRACTIONDIGITS_TYPE:
            case SchemaBase.LENGTH_TYPE:
            case SchemaBase.MAXEXCLUSIVE_TYPE:
            case SchemaBase.MAXINCLUSIVE_TYPE:
            case SchemaBase.MAXLENGTH_TYPE:
            case SchemaBase.MINEXCLUSIVE_TYPE:
            case SchemaBase.MININCLUSIVE_TYPE:
            case SchemaBase.MINLENGTH_TYPE:
            case SchemaBase.PATTERN_TYPE:
            case SchemaBase.TOTALDIGITS_TYPE:
            case SchemaBase.WHITESPACE_TYPE:
                expand = visitor.visit((FacetElement)root);
                break;
            
            default:
                throw new IllegalStateException("Internal error: unknown element type");
                
        }
        
        // check for expansion needed
        if (expand && (m_skipSet == null || !m_skipSet.isSkipped(root))) {
            walkChildren(root, visitor);
        }
        
        // exit the actual root of tree
        switch (root.type()) {
            
            case SchemaBase.ALL_TYPE:
                visitor.exit((AllElement)root);
                break;
            
            case SchemaBase.ANNOTATION_TYPE:
                visitor.exit((AnnotationElement)root);
                break;
            
            case SchemaBase.ANY_TYPE:
                visitor.exit((AnyElement)root);
                break;
            
            case SchemaBase.ANYATTRIBUTE_TYPE:
                visitor.exit((AnyAttributeElement)root);
                break;
            
            case SchemaBase.APPINFO_TYPE:
                visitor.exit((AppInfoElement)root);
                break;
            
            case SchemaBase.ATTRIBUTE_TYPE:
                visitor.exit((AttributeElement)root);
                break;
            
            case SchemaBase.ATTRIBUTEGROUP_TYPE:
                if (root instanceof AttributeGroupElement) {
                    visitor.exit((AttributeGroupElement)root);
                } else {
                    visitor.exit((AttributeGroupRefElement)root);
                }
                break;
            
            case SchemaBase.CHOICE_TYPE:
                visitor.exit((ChoiceElement)root);
                break;
            
            case SchemaBase.COMPLEXCONTENT_TYPE:
                visitor.exit((ComplexContentElement)root);
                break;
            
            case SchemaBase.COMPLEXTYPE_TYPE:
                visitor.exit((ComplexTypeElement)root);
                break;
            
            case SchemaBase.DOCUMENTATION_TYPE:
                visitor.exit((DocumentationElement)root);
                break;
            
            case SchemaBase.ELEMENT_TYPE:
                visitor.exit((ElementElement)root);
                break;
            
            case SchemaBase.EXTENSION_TYPE:
                if (root instanceof ComplexExtensionElement) {
                    visitor.exit((ComplexExtensionElement)root);
                } else {
                    visitor.exit((SimpleExtensionElement)root);
                }
                break;
            
            case SchemaBase.FIELD_TYPE:
                visitor.exit((SelectionBase.FieldElement)root);
                break;
            
            case SchemaBase.GROUP_TYPE:
                if (root instanceof GroupElement) {
                    visitor.exit((GroupElement)root);
                } else {
                    visitor.exit((GroupRefElement)root);
                }
                break;
            
            case SchemaBase.IMPORT_TYPE:
                visitor.exit((ImportElement)root);
                break;
            
            case SchemaBase.INCLUDE_TYPE:
                visitor.exit((IncludeElement)root);
                break;
            
            case SchemaBase.KEY_TYPE:
                visitor.exit((KeyBase.KeyElement)root);
                break;
            
            case SchemaBase.KEYREF_TYPE:
                visitor.exit((KeyBase.KeyrefElement)root);
                break;
            
            case SchemaBase.LIST_TYPE:
                visitor.exit((ListElement)root);
                break;
            
            case SchemaBase.NOTATION_TYPE:
                visitor.exit((NotationElement)root);
                break;
            
            case SchemaBase.REDEFINE_TYPE:
                visitor.exit((RedefineElement)root);
                break;
            
            case SchemaBase.RESTRICTION_TYPE:
                if (root instanceof SimpleRestrictionElement) {
                    visitor.exit((SimpleRestrictionElement)root);
                } else {
                    visitor.exit((ComplexRestrictionElement)root);
                }
                break;
            
            case SchemaBase.SCHEMA_TYPE:
                visitor.exit((SchemaElement)root);
                break;
            
            case SchemaBase.SELECTOR_TYPE:
                visitor.exit((SelectionBase.SelectorElement)root);
                break;
            
            case SchemaBase.SEQUENCE_TYPE:
                visitor.exit((SequenceElement)root);
                break;
            
            case SchemaBase.SIMPLECONTENT_TYPE:
                visitor.exit((SimpleContentElement)root);
                break;
            
            case SchemaBase.SIMPLETYPE_TYPE:
                visitor.exit((SimpleTypeElement)root);
                break;
            
            case SchemaBase.UNION_TYPE:
                visitor.exit((UnionElement)root);
                break;
            
            case SchemaBase.UNIQUE_TYPE:
                visitor.exit((KeyBase.UniqueElement)root);
                break;
            
            case SchemaBase.ENUMERATION_TYPE:
            case SchemaBase.FRACTIONDIGITS_TYPE:
            case SchemaBase.LENGTH_TYPE:
            case SchemaBase.MAXEXCLUSIVE_TYPE:
            case SchemaBase.MAXINCLUSIVE_TYPE:
            case SchemaBase.MAXLENGTH_TYPE:
            case SchemaBase.MINEXCLUSIVE_TYPE:
            case SchemaBase.MININCLUSIVE_TYPE:
            case SchemaBase.MINLENGTH_TYPE:
            case SchemaBase.PATTERN_TYPE:
            case SchemaBase.TOTALDIGITS_TYPE:
            case SchemaBase.WHITESPACE_TYPE:
                visitor.exit((FacetElement)root);
                break;
            
            default:
                break;
            
        }
        if (s_logger.isDebugEnabled() && root instanceof OpenAttrBase) {
            s_logger.debug("Left node " + SchemaUtils.componentPath((OpenAttrBase)root));
        }
    }
    
    /**
     * Walk the descendants of a root element. This recursively traverses the schema model tree rooted in the supplied
     * element, excluding the element itself, notifying the visitor of each element visited during the traversal.
     * 
     * @param root node of tree to be toured
     * @param visitor target visitor for element notifications
     */
    public void walkChildren(SchemaBase root, SchemaVisitor visitor) {
        
        // first expand the annotation, if present
        if (root instanceof AnnotatedBase) {
            AnnotationElement anno = ((AnnotatedBase)root).getAnnotation();
            if (anno != null) {
                walkElement(anno, visitor);
            }
        }
        
        // perform special handling for element expansion
        if (root instanceof SchemaLocationBase) {
            
            // references just delegate to the included schema element
            SchemaElement schema = ((SchemaLocationBase)root).getReferencedSchema();
            s_logger.debug(" checking referenced schema " + schema.getResolver().getName());
            if (schema != null && (m_schemaListener == null || m_schemaListener.enterSchema(schema))) {
                walkElement(schema, visitor);
                if (m_schemaListener != null) {
                    m_schemaListener.exitSchema();
                }
            }
        }
        
        // handle normal element expansion
        if (root instanceof OpenAttrBase) {
            OpenAttrBase parent = (OpenAttrBase)root;
            int count = parent.getChildCount();
            for (int i = 0; i < count; i++) {
                walkElement(parent.getChild(i), visitor);
            }
        }
    }
}
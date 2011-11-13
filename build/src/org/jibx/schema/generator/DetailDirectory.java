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

package org.jibx.schema.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.CollectionElement;
import org.jibx.binding.model.ContainerElementBase;
import org.jibx.binding.model.IComponent;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.ModelVisitor;
import org.jibx.binding.model.NamespaceElement;
import org.jibx.binding.model.StructureElement;
import org.jibx.binding.model.StructureElementBase;
import org.jibx.binding.model.TemplateElementBase;
import org.jibx.binding.model.ValidationContext;
import org.jibx.binding.model.ValueElement;
import org.jibx.custom.classes.ClassCustom;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.runtime.QName;
import org.jibx.util.IClass;
import org.jibx.util.Types;

/**
 * Directory for components included in schema generation. This includes both &lt;mapping> elements of the bindings and
 * special formats, with the latter currently limited to enumeration types. The building code works from a supplied list
 * of bindings, walking the tree structure of the bindings to find all mappings and processing each mapping directly
 * using the lists of child components. It creates mapping details and sets flags for the types of access to each
 * mapping, as well as creating enumeration details and counting usage to select globals.
 * 
 * @author Dennis M. Sosnoski
 */
public class DetailDirectory
{
    /** Binding customization information. */
    private final GlobalCustom m_custom;
    
    /** Validation context for bindings. */
    private final ValidationContext m_context;
    
    /** Map from &lt;mapping> definition to mapping detail. */
    private final Map m_mappingMap;
    
    /** Map from class name to enumeration detail. */
    private final Map m_enumMap;
    
    /** Set of &lt;mapping> definitions used as base types. */
    private final Set m_forceTypeMappings;
    
    /**
     * Constructor.
     * 
     * @param custom binding customization information (used for creating names as needed)
     * @param vctx binding validation context
     */
    public DetailDirectory(GlobalCustom custom, ValidationContext vctx) {
        m_custom = custom;
        m_context = vctx;
        m_mappingMap = new HashMap();
        m_enumMap = new HashMap();
        m_forceTypeMappings = new HashSet();
    }
    
    /**
     * Populate the mapping directory from a supplied list of root bindings. This uses a visitor to analyze the
     * bindings, building the detail information to be used during the actual generation process.
     * 
     * @param bindings
     */
    public void populate(List bindings) {
        
        // analyze all the bindings
        AnalysisVisitor visitor = new AnalysisVisitor(m_context);
        for (Iterator iter = bindings.iterator(); iter.hasNext();) {
            m_context.tourTree((BindingElement)iter.next(), visitor);
        }
        
        // flag all mappings referenced as base types to be defined as types
        for (Iterator iter = m_forceTypeMappings.iterator(); iter.hasNext();) {
            MappingElement mapping = (MappingElement)iter.next();
            MappingDetail detail = (MappingDetail)m_mappingMap.get(mapping);
            detail.setType(true);
        }
    }
    
    /**
     * Check if a &lt;structure> element represents a type derivation. If the element is empty, has no name or property,
     * is required, and is a mapping reference, then it can be handled as a type derivation.
     * 
     * @param struct
     * @return <code>true</code> if a type derivation, <code>false</code> if not
     */
    private static boolean isTypeDerivation(StructureElement struct) {
        return struct.children().size() == 0 && !struct.hasName() && !struct.hasProperty() && !struct.isOptional()
            && (struct.getDeclaredType() != null || struct.getEffectiveMapping() != null);
    }
    
    /**
     * Check if class is an enumeration type.
     * 
     * @param clas
     * @return enumeration type flag
     */
    private boolean isEnumeration(IClass clas) {
        return clas.isSuperclass("java.lang.Enum");
    }
    
    /**
     * Check if class is a simple value type.
     * 
     * @param clas
     * @return simple value type flag
     */
    private boolean isSimpleValue(IClass clas) {
        return Types.isSimpleValue(clas.getName());
    }
    
    /**
     * Count the usage of an enumeration type. The first time this is called for a type it just creates the enumeration
     * detail, then if called again for the same type it flags it as a global.
     * 
     * @param type
     */
    private void countEnumUsage(String type) {
        EnumDetail detail = (EnumDetail)m_enumMap.get(type);
        if (detail == null) {
            ClassCustom custom = m_custom.addClassCustomization(type);
            detail = new EnumDetail(custom);
            m_enumMap.put(type, detail);
            if (custom.isForceMapping()) {
                detail.setGlobal(true);
            }
        } else {
            detail.setGlobal(true);
        }
    }
    
    /**
     * Check references to mappings or enumeration types from component children of binding container element. This
     * allows for skipping a base mapping reference, so that mappings with type extension components can be handled
     * (with the extension component processed separately, since it's a special case).
     * 
     * @param cont container element
     * @param base child element representing base mapping reference (<code>null</code> if none)
     */
    private void checkReferences(ContainerElementBase cont, ContainerElementBase base) {
        
        // process all child components of container
        ArrayList childs = cont.children();
        for (int i = 0; i < childs.size(); i++) {
            Object child = childs.get(i);
            if (child == base) {
                continue;
            } else if (child instanceof ValueElement) {
                
                // check value reference to enumeration type
                IClass type = ((ValueElement)child).getType();
                if (isEnumeration(type)) {
                    countEnumUsage(type.getName());
                }
                
            } else if (child instanceof CollectionElement) {
                
                // check for collection item type
                CollectionElement collect = (CollectionElement)child;
                IClass itype = collect.getItemTypeClass();
                if (isEnumeration(itype)) {
                    
                    // collection items are enumeration type, count directly
                    countEnumUsage(itype.getName());
                    
                } else if (!isSimpleValue(itype)) {
                    
                    // find implied mapping, if one defined
                    String type = itype.getName();
                    TemplateElementBase ref = collect.getDefinitions().getSpecificTemplate(type);
                    if (ref instanceof MappingElement) {
                        MappingElement mapref = (MappingElement)ref;
                        MappingDetail detail = forceMappingDetail(mapref);
                        detail.setElement(true);
                    }
                }
                
                // check for nested type reference(s)
                checkReferences(collect, null);
                
            } else if (child instanceof StructureElement) {
                
                // structure, check for nested definition
                StructureElement struct = (StructureElement)child;
                if (struct.children().size() > 0) {
                    
                    // just handle references from nested components
                    checkReferences(struct, null);
                    
                } else {
                    
                    // no nested definition, check for mapping reference
                    MappingElement ref = (MappingElement)struct.getEffectiveMapping();
                    if (ref == null) {
                        m_context.addError("No handling defined for empty structure with no mapping reference", struct);
                    } else {
                        
                        // get the referenced mapping information
                        MappingDetail detail = forceMappingDetail(ref);
                        if (struct.hasDirectName()) {
                            if (ref.isAbstract()) {
                                
                                // named element referencing the mapping implies it's a type definition
                                detail.setType(true);
                                
                            } else {
                                
                                // concrete with name should never happen
                                m_context.addError("No handling defined for name on concrete mapping reference", struct);
                                
                            }
                        } else {
                            if (ref.getName() == null) {
                                
                                // abstract inline treated as group
                                detail.setGroup(true);
                                
                            } else {
                                
                                // concrete treated as element reference
                                detail.setElement(true);
                                
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Create the detail information for a &lt;mapping>. This creates the detail information and adds it to the map,
     * then analyzes the structure of the mapping to find references to other mappings and to enumeration types.
     * 
     * @param map
     * @return detail
     */
    private MappingDetail addDetail(MappingElement map) {
        
        // check structure of mapping definition for schema type extension
        MappingElement base = null;
        StructureElement baseref = null;
        ArrayList contents = map.getContentComponents();
        if (contents.size() > 0) {
            
            // type extension requires reference as first content component
            Object content = contents.get(0);
            if (content instanceof StructureElement) {
                StructureElement struct = (StructureElement)content;
                if (isTypeDerivation(struct)) {
                    base = (MappingElement)struct.getEffectiveMapping();
                    baseref = struct;
                }
            }
        }
        
        // next search recursively for text and/or child element components this is done at this point (with loop
        //  recursion, if needed) so that the flags can be set when creating the detail instance
        boolean haschild = false;
        boolean hastext = false;
        ArrayList expands = new ArrayList();
        expands.add(map);
        for (int i = 0; i < expands.size(); i++) {
            
            // check for container with element name or text content
            ContainerElementBase contain = (ContainerElementBase)expands.get(i);
            contents = contain.getContentComponents();
            for (int j = 0; j < contents.size(); j++) {
                IComponent comp = (IComponent)contents.get(j);
                if (comp.hasName()) {
                    
                    // component with name means child element
                    haschild = true;
                    
                } else if (comp instanceof ValueElement) {
                    
                    // value with no name implies text content
                    hastext = true;
                    
                } else if (comp instanceof CollectionElement) {
                    
                    // collection implies child element (repeating)
                    haschild = true;
                    
                } else {
                    
                    // structure, check for mapping reference
                    StructureElement struct = (StructureElement)comp;
                    if (struct.children().size() > 0) {
                        
                        // add container structure to expansion list
                        expands.add(comp);
                        
                    } else {
                        MappingElementBase ref = (MappingElementBase)struct.getEffectiveMapping();
                        if (ref != null) {
                            
                            // mapping element reference, check if named
                            if (ref.getName() != null) {
                                haschild = true;
                            } else {
                                expands.add(ref);
                                m_forceTypeMappings.add(ref);
                            }
                        }
                    }
                }
            }
        }
        
        // get the names for this mapping
        ClassCustom custom = m_custom.addClassCustomization(map.getClassName());
        QName tname = map.getTypeQName();
        if (tname == null) {
            NamespaceElement dfltns = map.getDefinitions().getElementDefaultNamespace();
            if (dfltns != null) {
                tname = new QName(dfltns.getUri(), custom.getTypeQName().getName());
            } else {
                tname = custom.getTypeQName();
            }
        }
        QName oname = null;
        String name = map.getName();
        if (name == null) {
            oname = custom.getElementQName();
        } else {
            oname = new QName(map.getNamespace().getUri(), name);
        }
        
        // add new mapping detail based on information found
        MappingDetail detail = new MappingDetail(map, haschild, hastext, base, tname, oname);
        m_mappingMap.put(map, detail);
        
        // check for mapping to element name
        if (map.getName() == null) {
            
            // require base type generation as type
            if (base != null) {
                MappingDetail basedetail = forceMappingDetail(base);
                basedetail.setType(true);
            }
            
        } else {
            
            // force generating an element for this mapping
            detail.setElement(true);
        }
        
        // error if mapping extension doesn't map to type extension
        MappingElement extended = map.getExtendsMapping();
        if (extended != null) {
            
            // recursively check for extension type match
            boolean isext = false;
            MappingElement ancest = base;
            while (ancest != null) {
                if (ancest.getClassName().equals(extended.getClassName())) {
                    isext = true;
                    break;
                } else {
                    ancest = forceMappingDetail(ancest).getExtensionBase();
                }
            }
            if (isext) {
                
                // flag generation as element in substitution group
                MappingDetail extdetail = forceMappingDetail(extended);
                extdetail.setElement(true);
                detail.setSubstitution(extdetail.getOtherName());
                detail.setElement(true);
                
            } else {
                m_context.addError("'extends' mapping not usable as schema extension base", map);
            }
        }
        
        // process all references from this mapping
        checkReferences(map, baseref);
        return detail;
    }
    
    /**
     * Find detail information for a &lt;mapping>. If this is the first time a particular mapping was requested, a new
     * detail information will be created for that mapping and returned.
     * 
     * @param map
     * @return detail
     */
    protected MappingDetail forceMappingDetail(MappingElement map) {
        MappingDetail detail = (MappingDetail)m_mappingMap.get(map);
        if (detail == null) {
            detail = addDetail(map);
        }
        return detail;
    }
    
    /**
     * Get detail information for a &lt;mapping>. If the detail information does not exist, this throws an exception.
     * 
     * @param map
     * @return detail
     */
    public MappingDetail getMappingDetail(MappingElementBase map) {
        MappingDetail detail = (MappingDetail)m_mappingMap.get(map);
        if (detail == null) {
            throw new IllegalStateException("Detail not found");
        } else {
            return detail;
        }
    }
    
    /**
     * Get detail information for a simple type. If the detail information does not exist, this throws an exception.
     * 
     * @param type
     * @return detail
     */
    public EnumDetail getSimpleDetail(String type) {
        EnumDetail detail = (EnumDetail)m_enumMap.get(type);
        if (detail == null) {
            throw new IllegalStateException("Detail not found");
        } else {
            return detail;
        }
    }
    
    /**
     * Get all complex type details.
     * 
     * @return collection of {@link MappingDetail}
     */
    public Collection getComplexDetails() {
        return m_mappingMap.values();
    }
    
    /**
     * Get all simple type details.
     * 
     * @return collection of {@link EnumDetail}
     */
    public Collection getSimpleDetails() {
        return m_enumMap.values();
    }
    
    /**
     * Model visitor for analyzing the structure of bindings and determining the appropriate schema components.
     */
    public class AnalysisVisitor extends ModelVisitor
    {
        /** Validation context running this visitor. */
        private final ValidationContext m_context;
        
        /**
         * Constructor.
         * 
         * @param vctx validation context that will run this visitor
         */
        public AnalysisVisitor(ValidationContext vctx) {
            m_context = vctx;
        }
        
        /**
         * Visit mapping element. This just adds the mapping definition, if not already added.
         * 
         * @param node
         * @return expansion flag
         */
        public boolean visit(MappingElement node) {
            
            // check for nested mapping
            if (!(m_context.getParentElement() instanceof BindingElement)) {
                m_context.addWarning("No schema equivalent for nested type definitions - converting to global type");
            }
            
            // add the definition
            forceMappingDetail(node);
            return super.visit(node);
        }
        
        /**
         * Visit structure or collection element. This just stops the expansion, since the content of mapping
         * definitions is processed at the time the mapping is added.
         * 
         * @param node
         * @return <code>false</code> to block further expansion
         */
        public boolean visit(StructureElementBase node) {
            return false;
        }
    }
}
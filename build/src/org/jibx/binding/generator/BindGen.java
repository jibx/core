/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.binding.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.BindingHolder;
import org.jibx.binding.model.BindingOrganizer;
import org.jibx.binding.model.CollectionElement;
import org.jibx.binding.model.ElementBase;
import org.jibx.binding.model.FormatElement;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.NestingElementBase;
import org.jibx.binding.model.StructureElement;
import org.jibx.binding.model.StructureElementBase;
import org.jibx.binding.model.ValidationContext;
import org.jibx.binding.model.ValueElement;
import org.jibx.custom.classes.ClassCustom;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.custom.classes.ValueCustom;
import org.jibx.custom.classes.NestingBase;
import org.jibx.custom.classes.PackageCustom;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.runtime.Utility;
import org.jibx.schema.generator.SchemaGen;
import org.jibx.util.IClass;
import org.jibx.util.IClassLocator;
import org.jibx.util.ReferenceCountMap;
import org.jibx.util.Types;
import org.jibx.util.UniqueNameSet;

/**
 * Binding generator implementation. Although many of the methods in this class use <code>public</code> access, they are
 * intended for use only by the JiBX developers and may change from one release to the next. To make use of this class
 * from your own code, call the {@link #main(String[])} method with an appropriate argument list.
 * 
 * @author Dennis M. Sosnoski
 */
public class BindGen
{
    /** Binding generation customizations. */
    private final GlobalCustom m_global;
    
    /** Set of class names to be included. */
    private final Set m_includeSet;
    
    /** Set of class names to be ignored. */
    private final Set m_ignoreSet;
    
    /** Set of class names to be handled directly. */
    private final Set m_directSet;
    
    /** Set of class names subclassed by other classes in binding. */
    private final Set m_superSet;
    
    /** Set of class names possibly requiring format definitions. */
    private final Set m_formatSet;
    
    /** Map from fully-qualified class name to mapping details. */
    private final Map m_mappingDetailsMap;
    
    /** Map from namespace URI to {@link UniqueNameSet} for type names. */
    private final Map m_typeNamesMap;
    
    /** Map from namespace URI to {@link UniqueNameSet} for element names. */
    private final Map m_elementNamesMap;
    
    /** Target package for binding code generation. */
    private String m_targetPackage;
    
    /** Directory for bindings being built. */
    private BindingOrganizer m_directory;
    
    /**
     * Create a generator based on a particular set of customizations.
     * 
     * @param glob
     */
    public BindGen(GlobalCustom glob) {
        m_global = glob;
        m_includeSet = new HashSet();
        m_ignoreSet = new HashSet();
        m_directSet = new HashSet();
        m_superSet = new HashSet();
        m_formatSet = new HashSet();
        m_mappingDetailsMap = new HashMap();
        m_typeNamesMap = new HashMap();
        m_elementNamesMap = new HashMap();
        m_directory = new BindingOrganizer(glob.isForceClasses(), glob.isTrackSource(), glob.isAddConstructors(),
            glob.isInput(), glob.isOutput(), false);
    }
    
    /**
     * Check if a class represents a simple value. TODO: implement ClassCustom hooks for this purpose
     * 
     * @param type fully qualified class name
     * @return <code>true</code> if simple value, <code>false</code> if not
     */
    public boolean isValueClass(String type) {
        ClassCustom clas = m_global.addClassCustomization(type);
        if (clas.isSimpleValue()) {
            m_formatSet.add(type);
            return true;
        }
        IClass sinfo = clas.getClassInformation().getSuperClass();
        if (sinfo != null && "java.lang.Enum".equals(sinfo.getName())) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Check if a class needs to be included in the binding. This checks all members of the class and if necessary
     * superclasses, returning <code>true</code> if any member is ultimately found with a simple value.
     * 
     * @param type fully qualified class name
     * @return <code>true</code> if class to be included in binding, <code>false</code> if it should be skipped
     */
    public boolean checkInclude(String type) {
        if (m_includeSet.contains(type)) {
            return true;
        } else if (m_ignoreSet.contains(type)) {
            return false;
        } else {
            
            // check if any members are to be included
            boolean include = false;
            ClassCustom clas = m_global.addClassCustomization(type);
            if (clas.isSimpleValue()) {
                include = true;
            } else {
                
                // check each member to find anything to be included
                for (Iterator iter = clas.getMembers().iterator(); iter.hasNext();) {
                    ValueCustom memb = (ValueCustom)iter.next();
                    String mtype = memb.isCollection() ? memb.getItemType() : memb.getWorkingType();
                    if (Types.isSimpleValue(mtype) || isValueClass(mtype) || !m_global.isKnownMapping(mtype)
                        || checkInclude(mtype)) {
                        include = true;
                        break;
                    }
                }
                
            }
            if (!include) {
                
                // force superclass handling if appropriate
                if (clas.getMembers().size() > 0 && clas.isUseSuper()) {
                    String stype = clas.getClassInformation().getSuperClass().getName();
                    if (!"java.lang.Object".equals(stype) && checkInclude(stype)) {
                        include = true;
                    }
                }
            }
            if (include) {
                m_includeSet.add(type);
            } else {
                m_ignoreSet.add(type);
            }
            return include;
        }
    }
    
    /**
     * Expand all references from a class. This checks the types of all members of the class, counting references and
     * calling itself recursively for any types which are not primitives and not java.* or javax.* classes. It also
     * expands the superclass, if specified by the class customizations.
     * 
     * @param type fully qualified class name
     * @param refmap reference count map
     */
    public void expandReferences(String type, ReferenceCountMap refmap) {
        if (checkInclude(type)) {
            
            // skip any member processing if a simple value
            ClassCustom clas = m_global.addClassCustomization(type);
            if (clas.isSimpleValue()) {
                return;
            }
            
            // increment reference count if forced mapping requested for class
            if (clas.isForceMapping() || clas.isAbstractMappingForced() || clas.isConcreteMappingForced()) {
                refmap.incrementCount(type);
            }
            
            // expand all references from members
            for (Iterator iter = clas.getMembers().iterator(); iter.hasNext();) {
                ValueCustom memb = (ValueCustom)iter.next();
                String mtype = memb.isCollection() ? memb.getItemType() : memb.getWorkingType();
                if (!Types.isSimpleValue(mtype) && !isValueClass(mtype) && !m_global.isKnownMapping(mtype)) {
                    if ((mtype.startsWith("java.")) || (mtype.startsWith("javax."))) {
                        throw new IllegalStateException("No way to handle type " + mtype + ", referenced from " + type);
                    } else if (checkInclude(mtype)) {
                        if (refmap.incrementCount(mtype) <= 1) {
                            expandReferences(mtype, refmap);
                        }
                        m_directSet.add(mtype);
                    }
                }
            }
            
            // force superclass handling if appropriate
            if (clas.getMembers().size() > 0 && clas.isUseSuper()) {
                String stype = clas.getClassInformation().getSuperClass().getName();
                if (!"java.lang.Object".equals(stype) && !Types.isSimpleValue(stype) && !isValueClass(stype)
                    && !m_global.isKnownMapping(stype) && checkInclude(stype)) {
                    if (refmap.incrementCount(stype) <= 1) {
                        expandReferences(stype, refmap);
                    }
                    m_superSet.add(stype);
                }
            }
        }
    }
    
    /**
     * Set creation information for structure binding component. This includes the declared type, as well as the create
     * type and factory method.
     * 
     * @param memb
     * @param struct
     */
    private void setTypes(ValueCustom memb, StructureElementBase struct) {
        String type = memb.getActualType();
        if (type != null && !type.equals(memb.getStatedType())) {
            struct.setDeclaredType(type);
        }
        struct.setCreateType(memb.getCreateType());
        struct.setFactoryName(memb.getFactoryMethod());
    }
    
    /**
     * Define the details of a collection binding. Collection bindings may be empty (in the case where the item type is
     * directly mapped), have a &lt;value> child element, or have either a mapping reference or direct definition
     * &lt;structure> child element. This determines the appropriate form based on the item type.
     * 
     * @param itype item type
     * @param iname item name
     * @param coll
     * @param hold
     */
    public void defineCollection(String itype, String iname, CollectionElement coll, BindingHolder hold) {
        
        // check item type handling
        BindingMappingDetail detail = (BindingMappingDetail)m_mappingDetailsMap.get(itype);
        ClassCustom custom = m_global.getClassCustomization(itype);
        if (detail != null) {
            if (detail.isUseAbstract() && !detail.isExtended()) {
                
                // add <structure> with map-as for abstract mapping
                QName qname = detail.getTypeQName();
                if (iname == null) {
                    iname = custom.getElementName();
                }
                StructureElement struct = new StructureElement();
                if (qname == null) {
                    struct.setMapAsName(itype);
                } else {
                    
                    // set the type reference and namespace dependency
                    String uri = qname.getUri();
                    hold.addTypeNameReference(uri, uri);
                    struct.setMapAsQName(qname);
                    
                }
                struct.setName(iname);
                hold.addNamespaceUsage(hold.getNamespace());
                struct.setCreateType(custom.getCreateType());
                struct.setFactoryName(custom.getFactoryMethod());
                coll.addChild(struct);
                
            } else {
                
                // just set item-type for concrete mapping
                coll.setItemTypeName(itype);
                
            }
            
        } else if (m_global.isKnownMapping(itype)) {
            
            // just set item-type for known mapping
            coll.setItemTypeName(itype);
            
        } else if (Types.isSimpleValue(itype) || isValueClass(itype)) {
            
            // add <value> for simple type, or enum type with optional value method
            ValueElement value = new ValueElement();
            value.setName(iname);
            value.setDeclaredType(itype);
            coll.addChild(value);
            if (custom != null) {
                value.setDefaultText(custom.getEnumValueMethod());
            }
            
        } else {
            
            // embed structure definition within the collection
            StructureElement struct = new StructureElement();
            struct.setDeclaredType(itype);
            if (iname == null) {
                iname = custom.getElementName();
            }
            struct.setName(iname);
            hold.addNamespaceUsage(hold.getNamespace());
            fillStructure(custom, null, null, struct, hold);
            coll.addChild(struct);
            
        }
    }
    
    /**
     * Add binding details for the actual members of a class, excluding any members which have been handled separately.
     * 
     * @param cust class customization information
     * @param exmethmap map from property method names to be excluded to the corresponding property customizations
     * @param inmethmap map from property method names included in binding to the corresponding property customizations
     * (populated by this method, <code>null</code> if not needed)
     * @param parent containing binding component
     * @param hold binding holder
     */
    private void addMemberBindings(ClassCustom cust, Map exmethmap, Map inmethmap, NestingElementBase parent,
        BindingHolder hold) {
        
        // generate binding component for each member of class
        for (Iterator iter = cust.getMembers().iterator(); iter.hasNext();) {
            
            // skip this member if excluded property
            ValueCustom memb = (ValueCustom)iter.next();
            String gmeth = memb.getGetName();
            String smeth = memb.getSetName();
            if (memb.isProperty() && !memb.isPrivate()) {
                ValueCustom match = (ValueCustom)(gmeth != null ? exmethmap.get(gmeth) : exmethmap.get(smeth));
                if (match == null) {
                    
                    // add methods for included property to map implemented for this binding
                    if (inmethmap != null) {
                        if (gmeth != null) {
                            inmethmap.put(gmeth, memb);
                        }
                        if (smeth != null) {
                            inmethmap.put(smeth, memb);
                        }
                    }
                    
                } else {
                    continue;
                }
            }
            
            // check for repeated item
            if (memb.isCollection()) {
                
                // create collection element for field or property
                CollectionElement coll = new CollectionElement();
                if (memb.getFieldName() == null) {
                    coll.setGetName(gmeth);
                    coll.setSetName(memb.getSetName());
                } else {
                    coll.setFieldName(memb.getFieldName());
                }
                setTypes(memb, coll);
                
                // set the element name, if defined
                String name = memb.getXmlName();
                if ((cust.isWrapCollections() || memb.isElementForced()) && name != null) {
                    coll.setName(name);
                    hold.addNamespaceUsage(hold.getNamespace());
                }
                
                // check for optional value
                if (!memb.isRequired()) {
                    coll.setUsageName("optional");
                }
                
                // check for type defined
                String itype = memb.getItemType();
                if (itype != null) {
                    
                    // set name to be used for items in collection
                    String iname = memb.getItemName();
                    if (iname == null) {
                        
                        // use name based on item type
                        String simple = itype;
                        int split = simple.lastIndexOf('.');
                        if (split >= 0) {
                            simple = simple.substring(0, split);
                        }
                        iname = cust.convertName(simple);
                        
                    }
                    
                    // define the collection details
                    defineCollection(itype, iname, coll, hold);
                    
                    // check for special case of two unwrapped collections with same item-type and no children
                    ArrayList siblings = parent.children();
                    if (siblings.size() > 0 && coll.children().size() == 0 && coll.getName() == null) {
                        ElementBase sibling = (ElementBase)siblings.get(siblings.size()-1);
                        if (sibling.type() == ElementBase.COLLECTION_ELEMENT) {
                            CollectionElement lastcoll = (CollectionElement)sibling;
                            if (lastcoll.children().size() == 0 && lastcoll.getName() == null &&
                                Utility.safeEquals(lastcoll.getItemTypeName(), coll.getItemTypeName())) {
                                throw new IllegalStateException("Need to use wrapper element for collection member '" +
                                    memb.getBaseName() + "' of class " + cust.getName());
                            }
                        }
                    }
                    
                }
                parent.addChild(coll);
                
            } else {
                
                // check whether value or structure
                String wtype = memb.getWorkingType();
                if (Types.isSimpleValue(wtype) || isValueClass(wtype)) {
                    
                    // add <value> for simple type or enum
                    ValueElement value = new ValueElement();
                    if (memb.getFieldName() == null) {
                        value.setGetName(gmeth);
                        value.setSetName(memb.getSetName());
                    } else {
                        value.setFieldName(memb.getFieldName());
                    }
                    value.setName(memb.getXmlName());
                    hold.addNamespaceUsage(hold.getNamespace());
                   
                    // pass on optional value method for enum
                    ClassCustom icust = m_global.getClassCustomization(wtype);
                    if (icust != null) {
                        value.setEnumValueName(icust.getEnumValueMethod());
                    }
                    
                    // check for optional value
                    if (!memb.isRequired()) {
                        value.setUsageName("optional");
                    }
                    
                    // configure added property values
                    int style = memb.getStyle();
                    if (style == NestingBase.ATTRIBUTE_VALUE_STYLE) {
                        value.setStyleName("attribute");
                    } else if (style == NestingBase.ELEMENT_VALUE_STYLE) {
                        value.setStyleName("element");
                    } else {
                        value.setStyleName("text");
                        value.setName(null);
                    }
                    if (memb.getActualType() != null) {
                        value.setDeclaredType(memb.getActualType());
                    }
                    parent.addChild(value);
                    
                } else {
                    
                    // create <structure> with name and access information
                    StructureElement struct = new StructureElement();
                    if (memb.getFieldName() == null) {
                        struct.setGetName(gmeth);
                        struct.setSetName(memb.getSetName());
                    } else {
                        struct.setFieldName(memb.getFieldName());
                    }
                    setTypes(memb, struct);
                    
                    // check for optional value
                    if (!memb.isRequired()) {
                        struct.setUsageName("optional");
                    }
                    
                    // set details based on class handling
                    ClassCustom mcust = m_global.getClassCustomization(memb.getWorkingType());
                    fillStructure(mcust, memb, null, struct, hold);
                    parent.addChild(struct);
                    
                }
            }
        }
    }
    
    /**
     * Add binding details for the full representation of a class. This includes superclasses, if configured, and makes
     * use of any appropriate &lt;mapping> definitions as part of the binding.
     * 
     * @param cust class customization information
     * @param memb member customization information (<code>null</code> if implicit reference, rather than member)
     * @param inmethmap map from property method names included in binding to the corresponding property customizations,
     * (needed in case of interface or overridden methods; populated by this method, <code>null</code> if not needed)
     * @param struct structure element referencing the class
     * @param hold binding holder
     */
    private void fillStructure(ClassCustom cust, ValueCustom memb, Map inmethmap, StructureElement struct,
        BindingHolder hold) {
        
        // check for an actual <mapping> definition
        String type = cust.getName();
        BindingMappingDetail detail = (BindingMappingDetail)m_mappingDetailsMap.get(type);
        if (detail != null) {
            
            // use existing <mapping> definition for type
            if (detail.isUseAbstract() && !detail.isExtended()) {
                QName qname = detail.getTypeQName();
                String uri = qname.getUri();
                hold.addTypeNameReference(uri, uri);
                struct.setMapAsQName(qname);
                if (memb != null) {
                    struct.setName(memb.getXmlName());
                    hold.addNamespaceUsage(hold.getNamespace());
                }
            } else {
                struct.setMapAsName(cust.getName());
            }
            if (inmethmap != null) {
                inmethmap.putAll(detail.getAccessMethodMap());
            }
            
        } else if (!m_global.isKnownMapping(type)) {
            
            // add member reference information
            if (memb != null) {
                
                // check for type settings needed
                if (memb.getActualType() != null) {
                    struct.setDeclaredType(memb.getActualType());
                }
                if (memb.getCreateType() != null) {
                    struct.setCreateType(memb.getCreateType());
                }
                if (memb.getFactoryMethod() != null) {
                    struct.setFactoryName(memb.getFactoryMethod());
                }
                
                // add a name if an optional value
                if (cust.isForceStructureNames() || !memb.isRequired()) {
                    struct.setName(memb.getXmlName());
                    hold.addNamespaceUsage(hold.getNamespace());
                }
                
            }
            
            // check if need to use superclass in binding
            IClass clas = cust.getClassInformation();
            IClass sclas = clas.getSuperClass();
            String stype = sclas.getName();
            Map exmethmap = Collections.EMPTY_MAP;
            if (checkInclude(stype)) {
                
                // check if any added content from this class
                ClassCustom scust = m_global.getClassCustomization(stype);
                exmethmap = new HashMap();
                if (cust.getMembers().size() > 0) {
                    
                    // add child <structure> element for superclass, followed by content from this class
                    StructureElement sstruct = new StructureElement();
                    sstruct.setDeclaredType(stype);
                    fillStructure(scust, null, exmethmap, sstruct, hold);
                    struct.addChild(sstruct);
                    
                } else {
                    
                    // just replace class reference with superclass reference in existing <structure>
                    struct.setDeclaredType(stype);
                    fillStructure(scust, null, exmethmap, struct, hold);
                    
                }
                
            } else {
                
                // check for interface with abstract mapping to reference
                String[] intfs = sclas.getInterfaces();
                for (int i = 0; i < intfs.length; i++) {
                    String intf = intfs[i];
                    BindingMappingDetail idetail = (BindingMappingDetail)m_mappingDetailsMap.get(intf);
                    if (idetail != null && idetail.isUseAbstract() && idetail.getTypeQName() == null) {
                        
                        // reference abstract mapped interface
                        struct = new StructureElement();
                        struct.setMapAsName(intf);
                        exmethmap = idetail.getAccessMethodMap();
                        break;
                    }
                }
                
            }
            
            // fill in content details from this class
            if (inmethmap != null) {
                inmethmap.putAll(exmethmap);
            }
            if (cust.getMembers().size() > 0) {
                addMemberBindings(cust, exmethmap, inmethmap, struct, hold);
            }
            
        }
    }
    
    /**
     * Add the &lt;mapping> definition for a class to a binding. This creates either an abstract mapping with a type
     * name, or a concrete mapping with an element name, as determined by the passed-in mapping information. If the
     * class is a concrete mapping that extends or implements another class with an anonymous abstract mapping, the
     * created &lt;mapping> will extend that base mapping. TODO: type substitution requires extending the binding
     * definition
     * 
     * @param type fully qualified class name
     * @param detail mapping details
     */
    private void addMapping(String type, BindingMappingDetail detail) {
        
        // create the basic mapping structure(s)
        ClassCustom cust = m_global.addClassCustomization(type);
        MappingElementBase mainmapping = null;
        QName qname = null;
        MappingElementBase mapcon = null;
        IClass clas = cust.getClassInformation();
        if (detail.isUseConcrete()) {
            
            // create concrete mapping
            mapcon = createMapping(type, cust);
            qname = detail.getElementQName();
            mapcon.setName(qname.getName());
            detail.setConcreteMapping(mapcon);
            
            // make "concrete" mapping abstract if no class creation possible
            if (clas.isAbstract() || clas.isInterface()) {
                mapcon.setAbstract(true);
            }
            
            // fill details directly to this mapping unless abstract also created
            mainmapping = mapcon;
            
        }
        MappingElement mapabs = null;
        if (detail.isUseAbstract()) {
            
            // create abstract mapping
            mapabs = createMapping(type, cust);
            mapabs.setAbstract(true);
            qname = detail.getTypeQName();
            mapabs.setTypeQName(qname);
            detail.setAbstractMapping(mapabs);
            
            // use abstract mapping for details (concrete will reference this one)
            mainmapping = mapabs;
            
        }
        if (mainmapping != null) {
            
            // find the binding containing this mapping
            String uri = qname.getUri();
            BindingHolder hold = m_directory.getBinding(uri);
            if (mainmapping.isAbstract()) {
                hold.addTypeNameReference(uri, uri);
            }
            
            // check for superclass mapping to be extended (do this first for compatibility with schema type extension)
            StructureElement struct = null;
            String ptype = detail.getExtendsType();
            Map exmembmap = Collections.EMPTY_MAP;
            Map inmembmap = new HashMap();
            if (ptype == null) {
                
                // not extending a base mapping, check if need to include superclass
                IClass parent = clas.getSuperClass();
                if (cust.isUseSuper() && parent != null) {
                    ptype = parent.getName();
                    if (checkInclude(ptype)) {
                        struct = new StructureElement();
                        struct.setDeclaredType(ptype);
                        ClassCustom scust = m_global.getClassCustomization(ptype);
                        exmembmap = new HashMap();
                        fillStructure(scust, null, exmembmap, struct, hold);
                    }
                }
                
            } else {
                
                // create reference to parent mapping
                struct = new StructureElement();
                BindingMappingDetail pdetail = (BindingMappingDetail)m_mappingDetailsMap.get(ptype);
                if (!pdetail.isGenerated()) {
                    addMapping(ptype, pdetail);
                }
                exmembmap = pdetail.getAccessMethodMap();
                if (pdetail.isUseAbstract()) {
                    
                    // reference abstract mapped superclass
                    QName tname = pdetail.getTypeQName();
                    if (tname == null) {
                        throw new IllegalStateException("Internal error: unimplemented case of superclass " + ptype
                            + " to be extended by subclass " + type + ", without an abstract <mapping> ");
                    } else {
                        uri = tname.getUri();
                        hold.addTypeNameReference(uri, uri);
                        struct.setMapAsQName(tname);
                    }
                    
                } else {
                    
                    // reference concrete mapped superclass
                    struct.setMapAsName(ptype);
                    
                }
                
                // set extension for child concrete mapping
                if (mapcon != null) {
                    mapcon.setExtendsName(ptype);
                }
                
            }
            
            // add extension reference structure to mapping
            if (struct != null) {
                mainmapping.addChild(struct);
            }
            
            // add all details of class member handling to binding
            inmembmap.putAll(exmembmap);
            addMemberBindings(cust, exmembmap, inmembmap, mainmapping, hold);
            hold.addMapping(mainmapping);
            if (mapabs != null && mapcon != null) {
                
                // define content as structure reference to abstract mapping
                struct = new StructureElement();
                QName tname = detail.getTypeQName();
                uri = tname.getUri();
                hold.addTypeNameReference(uri, uri);
                struct.setMapAsQName(tname);
                mapcon.addChild(struct);
                hold.addMapping(mapcon);
                
            }
            
            // set the member property map for mapping
            detail.setAccessMethodMap(inmembmap);
            detail.setGenerated(true);
            
        } else {
            throw new IllegalStateException("Internal error: mapping detail for " + type
                + " with neither abstract nor concrete mapping");
        }
    }
    
    /**
     * Create and initialize a &lt;mapping> element.
     * 
     * @param type
     * @param cust
     * @return mapping
     */
    private MappingElement createMapping(String type, ClassCustom cust) {
        MappingElement mapabs;
        mapabs = new MappingElement();
        mapabs.setClassName(type);
        mapabs.setCreateType(cust.getCreateType());
        mapabs.setFactoryName(cust.getFactoryMethod());
        return mapabs;
    }
    
    /**
     * Add the details for mapping a class.
     * TODO: should add checks for unique particle attribution rule - need to check for duplicate element names without
     * a required element in between, and if found alter the names after the first; note that duplicates may be from the
     * base type, or from a group; also need to make sure attribute names are unique
     * 
     * @param abstr force abstract mapping flag
     * @param ename element name for concrete mapping (<code>null</code> if unspecified)
     * @param type fully-qualified class name
     * @return mapping details
     */
    private BindingMappingDetail addMappingDetails(Boolean abstr, QName ename, String type) {
        
        // check for existing detail
        BindingMappingDetail detail = (BindingMappingDetail)m_mappingDetailsMap.get(type);
        if (detail == null) {
            
            // provide warning when interface used with field access
            ClassCustom cust = m_global.getClassCustomization(type);
            IClass sclas = cust.getClassInformation();
            if (sclas.isInterface() && !cust.isPropertyAccess()) {
                System.out.println("Warning: generating mapping for interface " + type +
                    " without checking properties - consider setting property-access='true'");
            }
            
            // check if a superclass is base for extension
            String stype = null;
            Boolean isabs = null;
            while ((sclas = sclas.getSuperClass()) != null) {
                if (m_directSet.contains(sclas.getName())) {
                    stype = sclas.getName();
                    isabs = Boolean.valueOf(sclas.isAbstract());
                    break;
                }
            }
            // TODO: really should check for multiple superclasses/interfaces to
            // be handled (and generate a warning, since they can't be handled)
            if (stype == null) {
                
                // check if an interface is base for extension
                sclas = cust.getClassInformation();
                loop: while (sclas != null) {
                    String[] intfs = sclas.getInterfaces();
                    for (int i = 0; i < intfs.length; i++) {
                        String itype = intfs[i];
                        while (true) {
                            if (m_directSet.contains(itype)) {
                                stype = itype;
                                isabs = Boolean.TRUE;
                                break loop;
                            } else {
                                ClassCustom icust = m_global.getClassCustomization(itype);
                                if (icust == null) {
                                    break;
                                } else {
                                    
                                    // check for interface (should only ever be one) of interface
                                    String[] sintfs = icust.getClassInformation().getInterfaces();
                                    if (sintfs.length > 0) {
                                        itype = sintfs[0];
                                    } else {
                                        break;
                                    }
                                    
                                }
                            }
                        }
                    }
                    sclas = sclas.getSuperClass();
                }
            }
            if (stype != null) {
                BindingMappingDetail sdetail = addMappingDetails(isabs, null, stype);
                sdetail.setExtended(true);
            }
            
            // force type name to use same namespace as element name, if supplied
            QName tname;
            if (ename == null) {
                tname = cust.getTypeQName();
            } else {
                tname = new QName(ename.getUri(), cust.getTypeName());
            }
            
            // make sure the binding holder has been created for this namespace
            String uri = tname.getUri();
            if (m_directory.getBinding(uri) == null) {
                m_directory.addBinding(uri, uri, null, true);
            }
            
            // check or get element name to be used
            // TODO: shouldn't need to do this, since element and type in separate namespaces should be allowed
            if (isQNameUsed(ename, m_elementNamesMap)) {
                throw new IllegalStateException("Internal error - attempting to create mapping with element name " +
                    ename + " already used");
            } else if (ename == null) {
                ename = cust.getElementQName();
            }
            
            // fix names if necessary to avoid conflicts
            tname = fixTypeName(tname);
            ename = fixElementName(ename);
            
            // create mapping details as specified
            boolean abs = ((abstr == null) ? cust.isMapAbstract() : abstr.booleanValue()) ||
                cust.isAbstractMappingForced();
            detail = new BindingMappingDetail(type, tname, ename, stype);
            if (abs || !cust.isConcreteClass()) {
                detail.setUseAbstract(true);
            }
            
            // force concrete mapping if not abstract, or if requested, or if extends base or base for extension
            if ((!abs || cust.isConcreteMappingForced() || sclas != null || m_superSet.contains(type)
                || (abstr != null && !abstr.booleanValue())) && cust.isConcreteClass()) {
                detail.setUseConcrete(true);
            }
            m_mappingDetailsMap.put(type, detail);
            
            // check if package usable as target for binding code
            if (m_targetPackage == null && cust.getClassInformation().isModifiable()) {
                m_targetPackage = ((PackageCustom)cust.getParent()).getName();
            }
            
        } else if (abstr != null) {
            
            // mapping detail already generated, just make sure of variety
            if (abstr.booleanValue()) {
                detail.setUseAbstract(true);
            } else {
                detail.setUseConcrete(true);
            }
            
        }
        return detail;
    }
    
    /**
     * Check if a qualified name is already defined within a category of names.
     *
     * @param qname requested qualified name (<code>null</code> allowed, always returns <code>false</code>)
     * @param map namespace URI to {@link UniqueNameSet} map for category
     * @return <code>true</code> if used, <code>false</code> if not
     */
    private boolean isQNameUsed(QName qname, Map map) {
        if (qname != null) {
            UniqueNameSet nameset = (UniqueNameSet)map.get(qname.getUri());
            if (nameset != null) {
                return nameset.contains(qname.getName());
            }
        }
        return false;
    }
    
    /**
     * Fix local name to be unique within the appropriate namespace for a category of names.
     *
     * @param qname requested qualified name (<code>null</code> allowed, always returns <code>null</code>)
     * @param map namespace URI to {@link UniqueNameSet} map for category
     * @return unique version of qualified name
     */
    private QName fixQName(QName qname, Map map) {
        if (qname != null) {
            String uri = qname.getUri();
            UniqueNameSet nameset = (UniqueNameSet)map.get(uri);
            if (nameset == null) {
                nameset = new UniqueNameSet();
                map.put(uri, nameset);
            }
            String base = qname.getName();
            String name = nameset.add(base);
            if (name.equals(base)) {
                return qname;
            } else {
                return new QName(uri, name);
            }
        } else {
            return null;
        }
    }
    
    /**
     * Fix element local name to be unique within the appropriate namespace.
     *
     * @param qname requested qualified name (<code>null</code> allowed, always returns <code>null</code>)
     * @return unique version of qualified name
     */
    private QName fixElementName(QName qname) {
        return fixQName(qname, m_elementNamesMap);
    }

    /**
     * Fix type local name to be unique within the appropriate namespace.
     *
     * @param qname requested qualified name (<code>null</code> allowed, always returns <code>null</code>)
     * @return unique version of qualified name
     */
    private QName fixTypeName(QName qname) {
        return fixQName(qname, m_typeNamesMap);
    }

    /**
     * Find closure of references from a supplied list of classes. References counted, and direct references are
     * accumulated for handling. The supplied list may include generic classes with type parameters.
     * 
     * @param classes
     * @param refmap
     */
    private void findReferences(List classes, ReferenceCountMap refmap) {
        for (int i = 0; i < classes.size(); i++) {
            String type = (String)classes.get(i);
            int split = type.indexOf('<');
            if (split > 0) {
                type = type.substring(split + 1, type.length() - 1);
            }
            expandReferences(type, refmap);
        }
    }
    
    /**
     * Flag classes referenced more than once to be handled with &lt;mapping> definitions.
     * 
     * @param refmap
     */
    private void flagMultipleReferences(ReferenceCountMap refmap) {
        for (Iterator iter = refmap.iterator(); iter.hasNext();) {
            String type = (String)iter.next();
            if (refmap.getCount(type) > 1) {
                m_directSet.add(type);
            }
        }
    }
    
    /**
     * Add mapping details for classes referenced more than once, or classes with mapping forced.
     * 
     * @param refmap
     */
    private void addReferencedMappings(ReferenceCountMap refmap) {
        for (Iterator iter = refmap.iterator(); iter.hasNext();) {
            String type = (String)iter.next();
            if (refmap.getCount(type) > 1 && !m_mappingDetailsMap.containsKey(type)) {
                addMappingDetails(null, null, type);
            }
        }
    }
    
    /**
     * Generate the mapping definitions for classes referenced more than once.
     * 
     * @param refmap
     */
    private void generateReferencedMappings(ReferenceCountMap refmap) {
        for (Iterator iter = refmap.iterator(); iter.hasNext();) {
            String type = (String)iter.next();
            if (refmap.getCount(type) > 1) {
                BindingMappingDetail detail = (BindingMappingDetail)m_mappingDetailsMap.get(type);
                if (!detail.isGenerated()) {
                    addMapping(type, detail);
                }
            }
        }
    }
    
    /**
     * Generate mappings for a list of classes. The mapping details must have been configured before this method is
     * called.
     * 
     * @param classes
     */
    private void generateMappings(List classes) {
        for (int i = 0; i < classes.size(); i++) {
            String type = (String)classes.get(i);
            BindingMappingDetail detail = (BindingMappingDetail)m_mappingDetailsMap.get(type);
            if (!detail.isGenerated()) {
                addMapping(type, detail);
            }
        }
    }
    
    /**
     * Fix the base classes that are to be used as extension types. This step is needed to generate the substitution
     * group structures for classes which are both referenced directly and extended by other classes. The method must be
     * called after all mapping details have been constucted but before the actual binding generation.
     */
    private void fixBaseClasses() {
        for (Iterator iter = m_directSet.iterator(); iter.hasNext();) {
            String type = (String)iter.next();
            BindingMappingDetail detail = (BindingMappingDetail)m_mappingDetailsMap.get(type);
            if (detail != null && detail.isExtended()) {
                detail.setUseConcrete(true);
            }
        }
    }
    
    /**
     * Generate any required format definitions. Format definitions are used for all classes defining serializer or
     * deserializer methods.
     */
    private void generateFormats() {
        
        // add any required format definitions
        for (Iterator iter = m_formatSet.iterator(); iter.hasNext();) {
            String type = (String)iter.next();
            ClassCustom clas = m_global.getClassCustomization(type);
            if (clas.getSerializer() != null || clas.getDeserializer() != null) {
                FormatElement format = new FormatElement();
                format.setDeserializerName(clas.getDeserializer());
                format.setSerializerName(clas.getSerializer());
                format.setTypeName(type);
                m_directory.addFormat(format);
            }
        }
    }
    
    /**
     * Generate binding(s) for a list of classes. This creates a &lt;mapping> definition for each class in the list, and
     * either embeds &lt;structure> definitions or creates separate &lt;mapping>s for other classes referenced by these
     * classes. If all the classes use the same namespace only the binding for that namespace will be created;
     * otherwise, a separate binding will be created for each namespace.
     * 
     * @param abstr force abstract mapping flag (use both abstract and concrete if <code>null</code>)
     * @param classes class list
     */
    public void generate(Boolean abstr, List classes) {
        
        // start by expanding and counting references from supplied classes
        ReferenceCountMap refmap = new ReferenceCountMap();
        m_directSet.addAll(classes);
        findReferences(classes, refmap);
        flagMultipleReferences(refmap);
        
        // set the classes to be handled with <mapping> definitions
        for (int i = 0; i < classes.size(); i++) {
            BindingMappingDetail detail = addMappingDetails(abstr, null, (String)classes.get(i));
            if (abstr == null) {
                detail.setUseAbstract(true);
                detail.setUseConcrete(true);
            }
        }
        addReferencedMappings(refmap);
        fixBaseClasses();
        
        // generate the binding(s)
        generateMappings(classes);
        generateReferencedMappings(refmap);
        generateFormats();
    }
    
    /**
     * Generate binding(s) for lists of classes. This creates a &lt;mapping> definition for each class in the lists, and
     * either embeds &lt;structure> definitions or creates separate &lt;mapping>s for other classes referenced by these
     * classes. If all the classes use the same namespace only the binding for that namespace will be created;
     * otherwise, a separate binding will be created for each namespace.
     * 
     * @param qnames list of names for concrete mappings
     * @param concrs list of classes to be given concrete mappings
     * @param abstrs list of classes to be given abstract mappings
     */
    public void generateSpecified(ArrayList qnames, List concrs, List abstrs) {
        
        // start by expanding and counting references from supplied classes
        ReferenceCountMap refmap = new ReferenceCountMap();
        m_directSet.addAll(concrs);
        m_directSet.addAll(abstrs);
        findReferences(concrs, refmap);
        findReferences(abstrs, refmap);
        flagMultipleReferences(refmap);
        
        // set classes to be handled with mapping definitions
        for (int i = 0; i < concrs.size(); i++) {
            BindingMappingDetail detail = addMappingDetails(Boolean.FALSE, (QName)qnames.get(i), (String)concrs.get(i));
            if (detail.getElementQName() == null) {
                detail.setElementQName(fixElementName((QName)qnames.get(i)));
            }
        }
        for (int i = 0; i < abstrs.size(); i++) {
            addMappingDetails(Boolean.TRUE, null, (String)abstrs.get(i));
        }
        addReferencedMappings(refmap);
        fixBaseClasses();
        
        // generate the binding(s)
        generateMappings(concrs);
        generateMappings(abstrs);
        generateReferencedMappings(refmap);
        generateFormats();
    }
    
    /**
     * Get the mapping details for a class. This method should only be used after the
     * {@link #generate(Boolean, List)} method has been called.
     * 
     * @param type fully-qualified class name
     * @return mapping details, or <code>null</code> if none
     */
    public BindingMappingDetail getMappingDetail(String type) {
        return (BindingMappingDetail)m_mappingDetailsMap.get(type);
    }
    
    /**
     * Get the binding definition for a namespace, which must already have been created. This method should only be used
     * after the {@link #generate(Boolean, List)} method has been called. It delegates to the
     * {@link org.jibx.binding.model.BindingOrganizer} implementation.
     * 
     * @param uri
     * @return binding holder
     */
    public BindingHolder getBinding(String uri) {
        return m_directory.getRequiredBinding(uri);
    }
    
    /**
     * Adds a collection of namespace URIs to be referenced at root binding level.
     * 
     * @param uris
     */
    public void addRootUris(Collection uris) {
        m_directory.addRootUris(uris);
    }
    
    /**
     * Get the binding definition for a namespace, creating a new one if not previously defined. This method should only
     * be used after the {@link #generate(Boolean, List)} method has been called. It delegates to the
     * {@link org.jibx.binding.model.BindingOrganizer} implementation.
     * 
     * @param uri
     * @param dflt namespace is default for elements in binding flag
     * @return binding holder
     */
    public BindingHolder addBinding(String uri, boolean dflt) {
        BindingHolder hold = m_directory.getBinding(uri);
        if (hold == null) {
            hold = m_directory.addBinding(uri, uri, null, dflt);
        }
        return hold;
    }
    
    /**
     * Complete the generated bindings. This prepares the generated bindings for writing to the file system, if desired.
     *
     * @param name file name for root or singleton binding definition
     * @return holder for root binding definition
     */
    public BindingHolder finish(String name) {
        BindingHolder root = m_directory.configureFiles(name, m_targetPackage, Collections.EMPTY_LIST);
        return root;
    }
    
    /**
     * Write and validate the generated binding definition files.
     *
     * @param dir target directory (bindings not written if <code>null</code>)
     * @param loc class locator for binding validation (ignored if no target directory supplied)
     * @param root holder for root binding definition
     * @return binding definitions, or <code>null</code> if validation error
     * @throws IOException
     * @throws JiBXException
     */
    public List validateFiles(File dir, IClassLocator loc, BindingHolder root) throws IOException, JiBXException {
        
        // write the full set of binding definitions to target directory
        m_directory.writeBindings(dir);
        
        // validate the root binding (which will pull in all referenced bindings)
        File file = new File(dir, root.getFileName());
        ValidationContext vctx = new ValidationContext(loc);
        FileInputStream is = new FileInputStream(file);
        BindingElement binding = BindingElement.validateBinding(root.getFileName(), file.toURI().toURL(), is, vctx);
        if (binding == null || vctx.getErrorCount() > 0 || vctx.getFatalCount() > 0) {
            return null;
        } else {
            
            // build and return list of validated binding definitions
            List uris = m_directory.getKeys();
            List bindings = new ArrayList();
            bindings.add(binding);
            URL base = dir.toURI().toURL();
            for (Iterator iter = uris.iterator(); iter.hasNext();) {
                String uri = (String)iter.next();
                BindingHolder hold = getBinding(uri);
                if (hold != root) {
                    URL url = new URL(base, hold.getFileName());
                    if (binding.addIncludePath(url.toExternalForm(), false)) {
                        throw new IllegalStateException("Binding not found when read from file");
                    } else {
                        bindings.add(binding.getExistingIncludeBinding(url));
                    }
                }
            }
            return bindings;
        }
    }
    
    /**
     * Run the binding generation using command line parameters.
     * 
     * @param args
     * @throws JiBXException
     * @throws IOException
     */
    public static void main(String[] args) throws JiBXException, IOException {
        BindGenCommandLine parms = new BindGenCommandLine();
        if (args.length > 0 && parms.processArgs(args)) {
            
            // generate bindings for all classes
            BindGen gen = new BindGen(parms.getGlobal());
            gen.generate(parms.getAbstract(), parms.getExtraArgs());
            BindingHolder root = gen.finish(parms.getBindingName());
            List bindings = gen.validateFiles(parms.getGeneratePath(), parms.getLocator(), root);
            if (!parms.isBindingOnly()) {
                
                // generate schemas for all bindings
                SchemaGen sgen = new SchemaGen(parms.getLocator(), parms.getGlobal(), parms.getUriNames());
                List schemas = sgen.generate(bindings);
                SchemaGen.writeSchemas(parms.getGeneratePath(), schemas);
            }
            
        } else {
            if (args.length > 0) {
                System.err.println("Terminating due to command line errors");
            } else {
                parms.printUsage();
            }
            System.exit(1);
        }
    }
}
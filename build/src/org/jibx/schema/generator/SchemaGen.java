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

package org.jibx.schema.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jibx.binding.Utility;
import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.CollectionElement;
import org.jibx.binding.model.ContainerElementBase;
import org.jibx.binding.model.IComponent;
import org.jibx.binding.model.MappingElement;
import org.jibx.binding.model.MappingElementBase;
import org.jibx.binding.model.StructureElement;
import org.jibx.binding.model.StructureElementBase;
import org.jibx.binding.model.TemplateElementBase;
import org.jibx.binding.model.ValidationContext;
import org.jibx.binding.model.ValidationProblem;
import org.jibx.binding.model.ValueElement;
import org.jibx.custom.classes.ClassCustom;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.schema.MemoryResolver;
import org.jibx.schema.SchemaHolder;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.elements.AllElement;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AnnotationElement;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupElement;
import org.jibx.schema.elements.AttributeGroupRefElement;
import org.jibx.schema.elements.ChoiceElement;
import org.jibx.schema.elements.CommonCompositorDefinition;
import org.jibx.schema.elements.ComplexContentElement;
import org.jibx.schema.elements.ComplexExtensionElement;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.DocumentationElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.FilteredSegmentList;
import org.jibx.schema.elements.GroupElement;
import org.jibx.schema.elements.GroupRefElement;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.elements.SequenceElement;
import org.jibx.schema.elements.SimpleContentElement;
import org.jibx.schema.elements.SimpleExtensionElement;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.elements.FacetElement.Enumeration;
import org.jibx.schema.types.Count;
import org.jibx.schema.validation.ValidationUtils;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.IClassLocator;
import org.jibx.util.Types;
import org.w3c.dom.Node;

/**
 * Schema generator from binding definition. Although many of the methods in this class use <code>public</code> access,
 * they are intended for use only by the JiBX developers and may change from one release to the next. To make use of
 * this class from your own code, call the {@link #main(String[])} method with an appropriate argument list.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaGen
{
    /** Locator for class information (<code>null</code> if none). */
    private final IClassLocator m_locator;
    
    /** Binding customization information. */
    private final GlobalCustom m_custom;
    
    /** Map from fully-qualified class name to qualified simple type name. */
    private final Map m_classSimpletypes;
    
    /** Map from namespace to schema file name. */
    private final Map m_uriNames;
    
    /** Map from namespace to schema holder. */
    private final Map m_uriSchemas;
    
    /** JavaDoc formatter instance cache. */
    private final FormatterCache m_formatCache;
    
    /** Validation context for bindings. */
    private final ValidationContext m_context;
    
    /** Details for mappings and enum usages. */
    private final DetailDirectory m_detailDirectory;
    
    /**
     * Constructor.
     * 
     * @param loc locator for class information (<code>null</code> if none)
     * @param custom binding customization information (used for creating names as needed)
     * @param urinames map from namespace URI to schema file name
     */
    public SchemaGen(IClassLocator loc, GlobalCustom custom, Map urinames) {
        m_locator = loc;
        m_custom = custom;
        m_classSimpletypes = new HashMap();
        m_uriNames = urinames;
        m_uriSchemas = new HashMap();
        m_formatCache = new FormatterCache(loc);
        m_context = new ValidationContext(loc);
        m_detailDirectory = new DetailDirectory(custom, m_context);
    }
    
    /**
     * Find the schema to be used for a particular namespace. If this is the first time a particular namespace was
     * requested, a new schema will be created for that namespace and returned.
     * 
     * @param uri namespace URI (<code>null</code> if no namespace)
     * @return schema holder
     */
    public SchemaHolder findSchema(String uri) {
        SchemaHolder hold = (SchemaHolder)m_uriSchemas.get(uri);
        if (hold == null) {
            hold = new SchemaHolder(uri);
            m_uriSchemas.put(uri, hold);
            String sname;
            if (uri == null) {
                sname = (String)m_uriNames.get("");
                if (sname == null) {
                    sname = "nonamespace.xsd";
                }
            } else {
                sname = (String)m_uriNames.get(uri);
                if (sname == null) {
                    sname = uri.substring(uri.lastIndexOf('/') + 1) + ".xsd";
                }
            }
            hold.setFileName(sname);
        }
        return hold;
    }
    
    /**
     * Get the qualified name of the simple type used for a component, if a named simple type. If this returns
     * <code>null</code>, the {@link #buildSimpleType(IComponent)} method needs to be able to construct the
     * appropriate simple type definition.
     * 
     * @param comp
     * @return qualified type name, <code>null</code> if inline definition needed
     */
    private QName getSimpleTypeQName(IComponent comp) {
        IClass type = comp.getType();
        String tname = comp.getType().getName();
        QName qname = Types.schemaType(tname);
        if (qname == null) {
            qname = (QName)m_classSimpletypes.get(tname);
            if (qname == null) {
                ClassCustom custom = m_custom.getClassCustomization(type.getName());
                if (custom != null && custom.isSimpleValue()) {
                    qname = Types.STRING_QNAME;
                } else if (!type.isSuperclass("java.lang.Enum")) {
                    m_context.addWarning("No schema equivalent known for type - treating as string", comp);
                    qname = Types.STRING_QNAME;
                }
            }
        }
        return qname;
    }
    
    /**
     * Add class-level documentation to a schema component.
     * 
     * @param info class information (<code>null</code> if not available)
     * @param root
     */
    private void addDocumentation(IClass info, AnnotatedBase root) {
        ClassCustom custom = m_custom.getClassCustomization(info.getName());
        if (info != null && custom.isUseJavaDocs()) {
            List nodes = m_formatCache.getFormatter(custom).getClassDocumentation(info);
            if (nodes != null) {
                AnnotationElement anno = new AnnotationElement();
                DocumentationElement doc = new DocumentationElement();
                for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                    Node node = (Node)iter.next();
                    doc.addContent(node);
                }
                anno.getItemsList().add(doc);
                root.setAnnotation(anno);
            }
        }
    }
    
    /**
     * Set the documentation for a schema component matching a class member.
     * 
     * @param item
     * @param elem
     */
    private void setDocumentation(IClassItem item, AnnotatedBase elem) {
        ClassCustom custom = m_custom.getClassCustomization(item.getOwningClass().getName());
        if (custom != null && custom.isUseJavaDocs()) {
            List nodes = m_formatCache.getFormatter(custom).getItemDocumentation(item);
            if (nodes != null) {
                AnnotationElement anno = new AnnotationElement();
                DocumentationElement doc = new DocumentationElement();
                for (Iterator iter = nodes.iterator(); iter.hasNext();) {
                    Node node = (Node)iter.next();
                    doc.addContent(node);
                }
                anno.getItemsList().add(doc);
                elem.setAnnotation(anno);
            }
        }
    }
    
    /**
     * Add documentation for a particular field or property.
     * 
     * @param struct
     * @param elem
     */
    private void addItemDocumentation(StructureElementBase struct, AnnotatedBase elem) {
        IClassItem item = struct.getField();
        if (item == null) {
            item = struct.getGet();
            if (item == null) {
                item = struct.getSet();
            }
        }
        if (item != null) {
            setDocumentation(item, elem);
        }
    }
    
    /**
     * Add documentation for a particular field or property.
     * 
     * @param value
     * @param elem
     */
    private void addItemDocumentation(ValueElement value, AnnotatedBase elem) {
        IClassItem item = value.getField();
        if (item == null) {
            item = value.getGet();
            if (item == null) {
                item = value.getSet();
            }
        }
        if (item != null) {
            setDocumentation(item, elem);
        }
    }
    
    /**
     * Create the simple type definition for a type. This is only used for cases where
     * {@link #getSimpleTypeQName(IComponent)} returns <code>null</code>. The current implementation only supports
     * Java 5 Enum types, but in the future should be extended to support &lt;format>-type conversions with supplied
     * schema definitions. This code requires a Java 5+ JVM to execute, but uses reflection to allow this class to be
     * loaded on older JVMs.
     * 
     * @param type
     * @return type definition
     */
    private SimpleTypeElement buildSimpleType(IClass type) {
        EnumDetail detail = m_detailDirectory.getSimpleDetail(type.getName());
        try {
            Class clas = type.loadClass();
            Method valsmeth = clas.getMethod("values", (Class[])null);
            String totxtname = detail.getCustom().getEnumValueMethod();
            if (totxtname == null) {
                totxtname = "name";
            }
            Method namemeth = clas.getMethod(totxtname, (Class[])null);
            try {
                valsmeth.setAccessible(true);
            } catch (RuntimeException e) { /* deliberately empty */
            }
            try {
                namemeth.setAccessible(true);
            } catch (RuntimeException e) { /* deliberately empty */
            }
            Object[] values = (Object[])valsmeth.invoke(null, (Object[])null);
            SimpleTypeElement simple = new SimpleTypeElement();
            SimpleRestrictionElement restr = new SimpleRestrictionElement();
            restr.setBase(Types.STRING_QNAME);
            for (int i = 0; i < values.length; i++) {
                Enumeration enumel = new Enumeration();
                enumel.setValue(namemeth.invoke(values[i], (Object[])null).toString());
                restr.getFacetsList().add(enumel);
            }
            simple.setDerivation(restr);
            addDocumentation(detail.getCustom().getClassInformation(), simple);
            return simple;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Convenience method to create the simple type definition for the type of a component.
     * 
     * @param comp
     * @return type definition
     */
    private SimpleTypeElement buildSimpleType(IComponent comp) {
        return buildSimpleType(comp.getType());
    }
    
    /**
     * General object comparison method. Don't know why Sun hasn't seen fit to include this somewhere, but at least it's
     * easy to write (over and over again).
     * 
     * @param a first object to be compared
     * @param b second object to be compared
     * @return <code>true</code> if both objects are <code>null</code>, or if <code>a.equals(b)</code>;
     * <code>false</code> otherwise
     */
    private static boolean isEqual(Object a, Object b) {
        return (a == null) ? b == null : a.equals(b);
    }
    
    /**
     * Check if a name references a different schema.
     *
     * @param qname name to be checked
     * @param hold schema holder
     * @return <code>true</code> if different, <code>false</code> if the same
     */
    private static boolean isDifferentSchema(QName qname, SchemaHolder hold) {
        return qname != null && !isEqual(qname.getUri(), hold.getNamespace())
            && !isEqual(qname.getUri(), Types.SCHEMA_NAMESPACE);
    }
    
    /**
     * Check for dependency on another schema. This creates an import for the target schema, and assigns a prefix for
     * referencing the target schema namespace.
     * 
     * @param qname name referenced by this schema
     * @param hold schema holder
     */
    private void checkDependency(QName qname, SchemaHolder hold) {
        if (isDifferentSchema(qname, hold)) {
            String uri = qname.getUri();
            SchemaHolder tohold = findSchema(uri);
            hold.addReference(tohold);
            hold.getPrefix(uri);
        }
    }

    /**
     * Check for dependency between two schemas. This creates schema imports in both directions, and assigns a prefix
     * for referencing the target schema namespace from within the current schema.
     *
     * @param qname name referenced by this schema
     * @param hold schema holder
     */
/*    private void checkBidirectionalDependency(QName qname, SchemaHolder hold) {
        if (isDifferentSchema(qname, hold)) {
            String uri = qname.getUri();
            SchemaHolder tohold = findSchema(uri);
            hold.addReference(tohold);
            hold.getPrefix(uri);
            tohold.addReference(hold);
        }
    }   */
    
    /**
     * Build a schema element description from a binding content component.
     * 
     * @param comp source component
     * @param repeat repeated element flag
     * @param hold
     * @return element
     */
    private ElementElement buildElement(IComponent comp, boolean repeat, SchemaHolder hold) {
        
        // create the basic element structure
        ElementElement elem = new ElementElement();
        if (repeat) {
            elem.setMinOccurs(Count.COUNT_ZERO);
            elem.setMaxOccurs(Count.COUNT_UNBOUNDED);
        } else if (comp.isOptional()) {
            elem.setMinOccurs(Count.COUNT_ZERO);
        }
        
        // set or create the element type definition
        boolean isref = false;
        if (comp instanceof ValueElement) {
            QName qname = getSimpleTypeQName(comp);
            if (qname == null) {
                elem.setTypeDefinition(buildSimpleType(comp));
            } else {
                setElementType(qname, elem, hold);
            }
            addItemDocumentation((ValueElement)comp, elem);
        } else if (comp instanceof CollectionElement) {
            CollectionElement coll = (CollectionElement)comp;
            if (coll.children().size() > 0) {
                
                // collection with children, choice or sequence from order
                ComplexTypeElement type = new ComplexTypeElement();
                if (coll.isOrdered()) {
                    
                    // ordered children go to sequence element, repeating
                    SequenceElement seq = new SequenceElement();
                    type.setContentDefinition(seq);
                    
                } else {
                    
                    // unordered children go to repeated choice element
                    ChoiceElement choice = new ChoiceElement();
                    type.setContentDefinition(choice);
                    
                }
                type.setContentDefinition(buildCompositor(coll, 0, true, hold));
                elem.setTypeDefinition(type);
                
            } else {
                
                // empty collection, item-type must reference a concrete mapping
                String itype = coll.getItemTypeName();
                TemplateElementBase ref = coll.getDefinitions().getSpecificTemplate(itype);
                if (ref instanceof MappingElement) {
                    
                    // item type with concrete mapping, make it an element
                    MappingDetail detail = m_detailDirectory.getMappingDetail((MappingElementBase)ref);
                    ComplexTypeElement type = new ComplexTypeElement();
                    SequenceElement seq = new SequenceElement();
                    type.setContentDefinition(seq);
                    ElementElement item = new ElementElement();
                    setElementRef(detail.getOtherName(), item, hold);
                    item.setMinOccurs(Count.COUNT_ZERO);
                    item.setMaxOccurs(Count.COUNT_UNBOUNDED);
                    seq.getParticleList().add(item);
                    elem.setTypeDefinition(type);
                    addItemDocumentation(coll, item);
                    
                } else {
                    
                    // TODO: handle this with xs:any strict?
                    m_context.addWarning("Handling not implemented for unspecified mapping", coll);
                }
                
            }
        } else {
            
            // must be a structure, check children
            StructureElement struct = (StructureElement)comp;
            if (struct.children().size() > 0) {
                
                // structure with children, choice or sequence from order
                ComplexTypeElement type = new ComplexTypeElement();
                if (struct.isOrdered()) {
                    
                    // ordered children go to sequence element
                    SequenceElement seq = new SequenceElement();
                    type.setContentDefinition(seq);
                    
                } else {
                    
                    // unordered children go to repeated choice element
                    ChoiceElement choice = new ChoiceElement();
                    type.setContentDefinition(choice);
                }
                type.setContentDefinition(buildCompositor(struct, 0, false, hold));
                fillAttributes(struct, 0, type.getAttributeList(), hold);
                elem.setTypeDefinition(type);
                
            } else {
                
                // no children, must be a mapping reference
                MappingElementBase ref = (MappingElementBase)struct.getEffectiveMapping();
                if (ref == null) {
                    
                    // TODO: handle this with xs:any strict?
                    m_context.addWarning("Handling not implemented for unspecified mapping", struct);
                    
                } else {
                    
                    // implicit mapping reference, find the handling
                    MappingDetail detail = m_detailDirectory.getMappingDetail(ref);
                    if (detail.isElement()) {
                        
                        // structure with concrete mapping
                        setElementRef(detail.getOtherName(), elem, hold);
                        isref = true;
                        
                    } else {
                        
                        // set element type to that constructed from mapping
                        setElementType(detail.getTypeName(), elem, hold);
                        
                    }
                }
            }
            addItemDocumentation(struct, elem);
        }
        if (!isref) {
            elem.setName(comp.getName());
        }
        return elem;
    }
    
    /**
     * Add a compositor as a particle in another compositor. If the compositor being added has only one particle, the
     * particle is directly added to the containing compositor.
     *
     * @param part compositor being added as a particle
     * @param comp target compositor for add
     */
    private static void addCompositorPart(CommonCompositorDefinition part, CommonCompositorDefinition comp)  {
        FilteredSegmentList parts = part.getParticleList();
        Count maxo = part.getMaxOccurs();
        Count mino = part.getMinOccurs();
        if (parts.size() == 1 && (maxo == null || maxo.isEqual(1)) && (mino == null || mino.isEqual(1))) {
            comp.getParticleList().add(parts.get(0));
        } else if (parts.size() > 1) {
            comp.getParticleList().add(part);
        }
    }
    
    /**
     * Build compositor for type definition. This creates and returns the appropriate form of compositor for the
     * container, populated with particles matching the child element components of the binding container.
     * 
     * @param cont container element defining structure
     * @param offset offset for first child component to process
     * @param repeat repeated item flag
     * @param hold holder for schema under construction
     * @return constructed compositor
     */
    private CommonCompositorDefinition buildCompositor(ContainerElementBase cont, int offset, boolean repeat,
        SchemaHolder hold) {
        
        // start with the appropriate type of compositor
        CommonCompositorDefinition cdef;
        if (cont.isChoice()) {
            
            // choice container goes directly to choice compositor
            cdef = new ChoiceElement();
            
        } else if (cont.isOrdered()) {
            
            // ordered container goes directly to sequence compositor
            cdef = new SequenceElement();
            
        } else if (repeat) {
            
            // unordered repeat treated as repeated choice compositor
            cdef = new ChoiceElement();
            cdef.setMaxOccurs(Count.COUNT_UNBOUNDED);
            
        } else {
            
            // unordered non-repeat treated as all compositor
            // TODO: verify conditions for all
            cdef = new AllElement();
        }
        
        // generate schema equivalents for content components of container
        ArrayList comps = cont.getContentComponents();
        for (int i = offset; i < comps.size(); i++) {
            IComponent comp = (IComponent)comps.get(i);
            if (comp.hasName()) {
                
                // create element for named content component
                ElementElement element = buildElement(comp, repeat, hold);
                if (comp instanceof StructureElementBase) {
                    addItemDocumentation((StructureElementBase)comp, element);
                }
                cdef.getParticleList().add(element);
                
            } else if (comp instanceof ContainerElementBase) {
                ContainerElementBase contain = (ContainerElementBase)comp;
                boolean iscoll = comp instanceof CollectionElement;
                if (contain.children().size() > 0) {
                    
                    // no element name, but children; handle with recursive call
                    CommonCompositorDefinition part = buildCompositor(contain, 0, iscoll, hold);
                    addCompositorPart(part, cdef);
                    
                } else if (iscoll) {
                    
                    // collection without a wrapper element
                    CollectionElement coll = (CollectionElement)comp;
                    String itype = coll.getItemTypeName();
                    TemplateElementBase ref = coll.getDefinitions().getSpecificTemplate(itype);
                    if (ref instanceof MappingElement) {
                        
                        // item type with concrete mapping, make it an element
                        MappingDetail detail = m_detailDirectory.getMappingDetail((MappingElementBase)ref);
                        ElementElement item = new ElementElement();
                        QName oname = detail.getOtherName();
                        setElementRef(oname, item, hold);
                        item.setMinOccurs(Count.COUNT_ZERO);
                        item.setMaxOccurs(Count.COUNT_UNBOUNDED);
                        addItemDocumentation(coll, item);
                        cdef.getParticleList().add(item);
                        
                    } else {
                        
                        // TODO: handle this with xs:any strict?
                        m_context.addWarning("Handling not implemented for unspecified mapping", coll);
                    }
                    
                } else if (comp instanceof StructureElement) {
                    
                    // no children, must be mapping reference
                    StructureElement struct = (StructureElement)comp;
                    MappingElementBase ref = (MappingElementBase)struct.getEffectiveMapping();
                    if (ref == null) {
                        
                        // TODO: handle this with xs:any strict?
                        m_context.addWarning("Handling not implemented for unspecified mapping", struct);
                        
                    } else {
                        
                        // handle mapping reference based on form and name use
                        MappingDetail detail = m_detailDirectory.getMappingDetail(ref);
                        if (ref.isAbstract()) {
                            
                            // abstract inline treated as group
                            GroupRefElement group = new GroupRefElement();
                            setGroupRef(detail.getOtherName(), group, hold);
                            if (comp.isOptional()) {
                                group.setMinOccurs(Count.COUNT_ZERO);
                            }
                            cdef.getParticleList().add(group);
                            
                        } else {
                            
                            // concrete treated as element reference
                            ElementElement elem = new ElementElement();
                            setElementRef(detail.getOtherName(), elem, hold);
                            if (comp.isOptional()) {
                                elem.setMinOccurs(Count.COUNT_ZERO);
                            }
                            addItemDocumentation(struct, elem);
                            cdef.getParticleList().add(elem);
                            
                        }
                    }
                } else {
                    m_context.addError("Unsupported binding construct", comp);
                }
                
            } else {
                m_context.addError("Unsupported component", comp);
            }
        }
        
        // simplify by eliminating this level if only child a compositor
        if ((cont.isOrdered() || cont.isChoice()) && cdef.getParticleList().size() == 1) {
            Object child = cdef.getParticleList().get(0);
            if (child instanceof CommonCompositorDefinition) {
                cdef = (CommonCompositorDefinition)child;
            }
        }
        return cdef;
    }
    
    /**
     * Set group reference, adding schema reference if necessary.
     * 
     * @param qname reference name
     * @param group
     * @param hold schema holder
     */
    private void setGroupRef(QName qname, AttributeGroupRefElement group, SchemaHolder hold) {
        checkDependency(qname, hold);
        group.setRef(qname);
    }
    
    /**
     * Set group reference, adding schema reference if necessary.
     * 
     * @param qname reference name
     * @param group
     * @param hold schema holder
     */
    private void setGroupRef(QName qname, GroupRefElement group, SchemaHolder hold) {
        checkDependency(qname, hold);
        group.setRef(qname);
    }
    
    /**
     * Set element reference, adding schema reference if necessary.
     * 
     * @param qname reference name
     * @param elem
     * @param hold schema holder
     */
    private void setElementRef(QName qname, ElementElement elem, SchemaHolder hold) {
        checkDependency(qname, hold);
        elem.setRef(qname);
    }
    
    /**
     * Set attribute type, adding schema reference if necessary.
     * 
     * @param qname type name
     * @param elem
     * @param hold schema holder
     */
    private void setAttributeType(QName qname, AttributeElement elem, SchemaHolder hold) {
        elem.setType(qname);
        checkDependency(qname, hold);
    }
    
    /**
     * Set element type, adding schema reference if necessary.
     * 
     * @param qname type name
     * @param elem
     * @param hold schema holder
     */
    public void setElementType(QName qname, ElementElement elem, SchemaHolder hold) {
        elem.setType(qname);
        checkDependency(qname, hold);
    }
    
    /**
     * Set element substitution group, adding schema reference if necessary.
     * 
     * @param qname substitution group element name
     * @param elem
     * @param hold schema holder
     */
    private void setSubstitutionGroup(QName qname, ElementElement elem, SchemaHolder hold) {
        elem.setSubstitutionGroup(qname);
        checkDependency(qname, hold);
    }
    
    /**
     * Build attributes as part of complex type definition.
     * 
     * @param cont container element defining structure
     * @param offset offset for first child component to process
     * @param attrs complex type attribute list
     * @param hold holder for schema under construction
     */
    private void fillAttributes(ContainerElementBase cont, int offset, AbstractList attrs, SchemaHolder hold) {
        
        // add child attribute components
        ArrayList comps = cont.getAttributeComponents();
        for (int i = 0; i < comps.size(); i++) {
            IComponent comp = (IComponent)comps.get(i);
            if (comp instanceof ValueElement) {
                
                // simple attribute definition
                AttributeElement attr = new AttributeElement();
                attr.setName(comp.getName());
                QName qname = getSimpleTypeQName(comp);
                if (qname == null) {
                    attr.setTypeDefinition(buildSimpleType(comp));
                } else {
                    setAttributeType(qname, attr, hold);
                }
                if (!comp.isOptional()) {
                    attr.setUse(AttributeElement.REQUIRED_USE);
                }
                addItemDocumentation((ValueElement)comp, attr);
                attrs.add(attr);
                
            } else if (comp instanceof ContainerElementBase) {
                ContainerElementBase contain = (ContainerElementBase)comp;
                if (contain.children().size() > 0) {
                    
                    // handle children with recursive call
                    fillAttributes(contain, 0, attrs, hold);
                    
                } else if (comp instanceof StructureElement) {
                    
                    // no children, must be mapping reference
                    StructureElement struct = (StructureElement)comp;
                    if (struct.isOptional()) {
                        m_context.addError("No schema equivalent for optional abstract mapping with attributes", comp);
                    } else {
                        MappingElementBase ref = (MappingElementBase)struct.getEffectiveMapping();
                        if (ref != null && ref.isAbstract()) {
                            
                            // abstract inline treated as group
                            MappingDetail detail = m_detailDirectory.getMappingDetail(ref);
                            AttributeGroupRefElement group = new AttributeGroupRefElement();
                            setGroupRef(detail.getOtherName(), group, hold);
                            attrs.add(group);
                            
                        } else {
                            throw new IllegalStateException("Unsupported binding construct " + comp);
                        }
                    }
                } else {
                    m_context.addError("Unsupported binding construct", comp);
                }
                
            } else {
                m_context.addError("Unsupported component", comp);
            }
        }
    }
    
    /**
     * Check if a container element of the binding represents complex content.
     * 
     * @param cont
     * @return <code>true</code> if complex content, <code>false</code> if not
     */
    private static boolean isComplexContent(ContainerElementBase cont) {
        ArrayList contents = cont.getContentComponents();
        for (int i = 0; i < contents.size(); i++) {
            Object item = contents.get(i);
            if (item instanceof IComponent && ((IComponent)item).hasName()) {
                return true;
            } else if (item instanceof ContainerElementBase && isComplexContent((ContainerElementBase)item)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Build the complex type definition for a mapping.
     * 
     * @param detail mapping detail
     * @param hold target schema for definition
     * @return constructed complex type
     */
    private ComplexTypeElement buildComplexType(MappingDetail detail, SchemaHolder hold) {
        
        // create the type and compositor
        ComplexTypeElement type = new ComplexTypeElement();
        MappingElementBase mapping = detail.getMapping();
        MappingElementBase base = detail.getExtensionBase();
        if (base == null) {
            if (detail.isGroup()) {
                
                // create type using references to group and/or attributeGroup
                SequenceElement seq = new SequenceElement();
                if (detail.hasChild()) {
                    GroupRefElement gref = new GroupRefElement();
                    setGroupRef(detail.getOtherName(), gref, hold);
                    seq.getParticleList().add(gref);
                }
                type.setContentDefinition(seq);
                if (detail.hasAttribute()) {
                    AttributeGroupRefElement gref = new AttributeGroupRefElement();
                    setGroupRef(detail.getOtherName(), gref, hold);
                    type.getAttributeList().add(gref);
                }
                
            } else {
                
                // create type directly
                type.setContentDefinition(buildCompositor(mapping, 0, false, hold));
                fillAttributes(mapping, 0, type.getAttributeList(), hold);
                
            }
        } else {
            
            // create type as extension of base type
            MappingDetail basedet = m_detailDirectory.getMappingDetail(base);
            if (isComplexContent(base) || isComplexContent(mapping)) {
                
                // complex extension with complex content
                ComplexExtensionElement ext = new ComplexExtensionElement();
                setComplexExtensionBase(basedet.getTypeName(), ext, hold);
                CommonCompositorDefinition comp = buildCompositor(mapping, 1, false, hold);
                if (comp.getParticleList().size() > 0) {
                    ext.setContentDefinition(comp);
                }
                fillAttributes(mapping, 0, ext.getAttributeList(), hold);
                ComplexContentElement cont = new ComplexContentElement();
                cont.setDerivation(ext);
                type.setContentType(cont);
                
            } else {
                
                // simple extension with simple content
                SimpleExtensionElement ext = new SimpleExtensionElement();
                setSimpleExtensionBase(basedet.getTypeName(), ext, hold);
                fillAttributes(mapping, 0, ext.getAttributeList(), hold);
                SimpleContentElement cont = new SimpleContentElement();
                cont.setDerivation(ext);
                type.setContentType(cont);
                
            }
        }
        return type;
    }
    
    /**
     * Set the base for a complex extension type.
     * 
     * @param qname
     * @param ext
     * @param hold
     */
    private void setComplexExtensionBase(QName qname, ComplexExtensionElement ext, SchemaHolder hold) {
        ext.setBase(qname);
        checkDependency(qname, hold);
    }
    
    /**
     * Set the base for a simple extension type.
     * 
     * @param qname
     * @param ext
     * @param hold
     */
    private void setSimpleExtensionBase(QName qname, SimpleExtensionElement ext, SchemaHolder hold) {
        ext.setBase(qname);
        checkDependency(qname, hold);
    }
    
    /**
     * Add mapping to schema definitions. This generates the appropriate schema representation for the mapping based on
     * the detail flags, which may include group and/or attributeGroup, complexType, and element definitions.
     * 
     * @param detail
     */
    private void addMapping(MappingDetail detail) {
        
        // get the documentation to be used for type
        IClass info = null;
        if (m_locator != null) {
            info = m_locator.getClassInfo(detail.getMapping().getClassName());
        }
        
        // start by generating group/attributeGroup schema components
        MappingElementBase mapping = detail.getMapping();
        if (detail.isGroup()) {
            // TODO: extend base type for group/attributeGroup?
            QName qname = detail.getOtherName();
            SchemaHolder hold = findSchema(qname.getUri());
            if (detail.hasChild()) {
                GroupElement group = new GroupElement();
                group.setName(qname.getName());
                group.setDefinition(buildCompositor(mapping, 0, false, hold));
                addDocumentation(info, group);
                hold.getSchema().getTopLevelChildren().add(group);
            }
            if (detail.hasAttribute()) {
                AttributeGroupElement attgrp = new AttributeGroupElement();
                attgrp.setName(qname.getName());
                fillAttributes(mapping, 0, attgrp.getAttributeList(), hold);
                addDocumentation(info, attgrp);
                hold.getSchema().getTopLevelChildren().add(attgrp);
            }
        }
        
        // next generate the complex type definition
        if (detail.isType()) {
            
            // set up the basic definition structure
            QName qname = detail.getTypeName();
            SchemaHolder hold = findSchema(qname.getUri());
            ComplexTypeElement type = buildComplexType(detail, hold);
            type.setName(qname.getName());
            addDocumentation(info, type);
            hold.getSchema().getTopLevelChildren().add(type);
        }
        
        // finish by generating element definition
        if (detail.isElement()) {
            QName qname = detail.getOtherName();
            SchemaHolder hold = findSchema(qname.getUri());
            ElementElement elem = new ElementElement();
            elem.setName(qname.getName());
            setSubstitutionGroup(detail.getSubstitution(), elem, hold);
            if (detail.isType()) {
                setElementType(detail.getTypeName(), elem, hold);
            } else {
                
                // check for just an element wrapper around type reference
                MappingElementBase ext = detail.getExtensionBase();
                if (ext != null && !detail.hasAttribute() && mapping.getContentComponents().size() == 1) {
                    setElementType(ext.getTypeQName(), elem, hold);
                } else {
                    
                    // add documentation to element which is not also a type
                    addDocumentation(info, elem);
                    elem.setTypeDefinition(buildComplexType(detail, hold));
                }
            }
            hold.getSchema().getTopLevelChildren().add(elem);
        }
    }
    
    /**
     * Generate a list of schemas from a list of bindings. The two lists are not necessarily in any particular
     * relationship to each other.
     * 
     * @param bindings list of {@link BindingElement}
     * @return list of {@link SchemaHolder}
     */
    public ArrayList buildSchemas(List bindings) {
        
        // start by scanning all bindings to build mapping and enum details
        m_detailDirectory.populate(bindings);
        
        // process all the simple type definitions (formats or enums)
        Collection simples = m_detailDirectory.getSimpleDetails();
        for (Iterator iter = simples.iterator(); iter.hasNext();) {
            EnumDetail detail = (EnumDetail)iter.next();
            if (detail.isGlobal()) {
                ClassCustom custom = detail.getCustom();
                SimpleTypeElement type = buildSimpleType(custom.getClassInformation());
                type.setName(custom.getTypeName());
                SchemaHolder hold = findSchema(custom.getNamespace());
                hold.getSchema().getTopLevelChildren().add(type);
                m_classSimpletypes.put(custom.getName(), custom.getTypeQName());
            }
        }
        
        // process all the mapping definitions from directory
        Collection mappings = m_detailDirectory.getComplexDetails();
        for (Iterator iter = mappings.iterator(); iter.hasNext();) {
            MappingDetail detail = (MappingDetail)iter.next();
            addMapping(detail);
        }
        
        // report any problems found in processing bindings
        ArrayList probs = m_context.getProblems();
        boolean error = m_context.getErrorCount() > 0 || m_context.getFatalCount() > 0;
        if (probs.size() > 0) {
            System.out.print(error ? "Errors" : "Warnings");
            System.out.println(" in binding:");
            for (int j = 0; j < probs.size(); j++) {
                ValidationProblem prob = (ValidationProblem)probs.get(j);
                System.out.print(prob.getSeverity() >= ValidationProblem.ERROR_LEVEL ? "Error: " : "Warning: ");
                System.out.println(prob.getDescription());
            }
            if (error) {
                throw new RuntimeException("Errors found in bindings");
            }
        }
        return new ArrayList(m_uriSchemas.values());
    }

    /**
     * Finish the construction of schemas, setting links between the schemas and validating the schemas.
     * 
     * @param exists existing schemas potentially referenced (by name, rather than id) from constructed schemas
     * @return list of {@link SchemaHolder}
     */
    public ArrayList finishSchemas(Collection exists) {
        
        // fix all references between schemas
        ArrayList holders = new ArrayList(m_uriSchemas.values());
        SchemaElement[] schemas = new SchemaElement[holders.size()];
        for (int i = 0; i < holders.size(); i++) {
            SchemaHolder hold = (SchemaHolder)holders.get(i);
            hold.finish();
            schemas[i] = hold.getSchema();
        }
        
        // set up validation with in-memory schemas
        org.jibx.schema.validation.ValidationContext vctx = new org.jibx.schema.validation.ValidationContext();
        for (int i = 0; i < holders.size(); i++) {
            SchemaHolder holder = (SchemaHolder)holders.get(i);
            String id = holder.getFileName();
            SchemaElement schema = holder.getSchema();
            schema.setResolver(new MemoryResolver(id));
            vctx.setSchema(id, schema);
        }
        for (Iterator iter = exists.iterator(); iter.hasNext();) {
            SchemaElement schema = (SchemaElement)iter.next();
            vctx.setSchema(schema.getResolver().getName(), schema);
        }
        
        // validate the schemas and report any problems
        ValidationUtils.validateSchemas(schemas, vctx);
        ArrayList probs = vctx.getProblems();
        if (probs.size() > 0) {
            for (int j = 0; j < probs.size(); j++) {
                org.jibx.schema.validation.ValidationProblem prob =
                    (org.jibx.schema.validation.ValidationProblem)probs.get(j);
                System.out.print(prob.getSeverity() >=
                    org.jibx.schema.validation.ValidationProblem.ERROR_LEVEL ? "Error: " : "Warning: ");
                System.out.println(prob.getDescription());
            }
            if ((vctx.getErrorCount() > 0 || vctx.getFatalCount() > 0)) {
                throw new RuntimeException("Errors found in generated schemas");
            }
        }
        return holders;
    }

    /**
     * Generate a list of schemas from a list of bindings. The two lists are not necessarily in any particular
     * relationship to each other. This method is provided for backward compatibility; it just combines a call to
     * {@link #buildSchemas(List)} with a call to {@link #finishSchemas(Collection)} with an empty list of existing
     * schemas.
     * 
     * @param bindings list of {@link BindingElement}
     * @return list of {@link SchemaHolder}
     */
    public List generate(List bindings) {
        buildSchemas(bindings);
        return finishSchemas(Collections.EMPTY_LIST);
    }

    /**
     * Get details of schema handling of a mapping.
     * 
     * @param map
     * @return mapping details
     */
    public MappingDetail getMappingDetail(MappingElement map) {
        return m_detailDirectory.forceMappingDetail(map);
    }
    
    /**
     * Write a collection of schemas to a target directory.
     *
     * @param dir target directory
     * @param schemas list of {@link SchemaHolder}
     * @throws JiBXException
     * @throws IOException
     */
    public static void writeSchemas(File dir, Collection schemas) throws JiBXException, IOException {
        IBindingFactory fact = BindingDirectory.getFactory(SchemaUtils.XS_PREFIX_BINDING, SchemaElement.class);
        for (Iterator iter = schemas.iterator(); iter.hasNext();) {
            SchemaHolder holder = (SchemaHolder)iter.next();
            if (!holder.isExistingFile()) {
                IMarshallingContext ictx = fact.createMarshallingContext();
                File file = new File(dir, holder.getFileName());
                ictx.setOutput(new FileOutputStream(file), null);
                ictx.setIndent(2);
                ((IMarshallable)holder.getSchema()).marshal(ictx);
                ictx.getXmlWriter().flush();
            }
        }
    }

    /**
     * Run the schema generation using command line parameters.
     * 
     * @param args
     * @throws JiBXException
     * @throws IOException
     */
    public static void main(String[] args) throws JiBXException, IOException {
        SchemaGenCommandLine parms = new SchemaGenCommandLine();
        if (args.length > 0 && parms.processArgs(args)) {
            
            // read and validate all the binding definitions
            boolean valid = true;
            List binddefs = parms.getExtraArgs();
            ArrayList bindings = new ArrayList(binddefs.size());
            for (Iterator iter = binddefs.iterator(); iter.hasNext();) {
                String bpath = (String)iter.next();
                String name = Utility.fileName(bpath);
                File file = new File(bpath);
                try {
                    URL url = new URL("file://" + file.getAbsolutePath());
                    FileInputStream is = new FileInputStream(file);
                    try {
                        ValidationContext vctx = new ValidationContext(parms.getLocator());
                        BindingElement binding = BindingElement.validateBinding(name, url, is, vctx);
                        if (vctx.getErrorCount() == 0 && vctx.getFatalCount() == 0) {
                            bindings.add(binding);
                        } else {
                            valid = false;
                        }
                    } catch (JiBXException e) {
                        System.err.println("Unable to process binding " + name);
                        e.printStackTrace();
                        valid = false;
                    }
                    
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    valid = false;
                }
            }
            if (valid) {
                
                // generate schemas from the bindings
                SchemaGen gen = new SchemaGen(parms.getLocator(), parms.getGlobal(), parms.getUriNames());
                List schemas = gen.generate(bindings);
                writeSchemas(parms.getGeneratePath(), schemas);
                
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
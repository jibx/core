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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jibx.runtime.QName;
import org.jibx.schema.IArity;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.AttributeGroupRefElement;
import org.jibx.schema.elements.ChoiceElement;
import org.jibx.schema.elements.CommonCompositorDefinition;
import org.jibx.schema.elements.CommonTypeDefinition;
import org.jibx.schema.elements.CommonTypeDerivation;
import org.jibx.schema.elements.ComplexExtensionElement;
import org.jibx.schema.elements.ComplexRestrictionElement;
import org.jibx.schema.elements.ComplexTypeElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.FilteredSegmentList;
import org.jibx.schema.elements.GroupRefElement;
import org.jibx.schema.elements.ListElement;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SequenceElement;
import org.jibx.schema.elements.SimpleExtensionElement;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.elements.UnionElement;
import org.jibx.schema.support.SchemaTypes;
import org.jibx.schema.types.Count;
import org.jibx.schema.validation.ValidationContext;

/**
 * Extension information for all schema components other than the schema element itself. This is the basic extension
 * which associates schema components with values or classes for code generation.
 * 
 * @author Dennis M. Sosnoski
 */
public class ComponentExtension extends BaseExtension
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(ComponentExtension.class.getName());
    
    /** Containing global definition extension. */
    private final GlobalExtension m_global;
    
    /** Component dropped from schema definition. */
    private boolean m_removed;
    
    /** Optional component flag. */
    private boolean m_optional;
    
    /** Repeated component flag. */
    private boolean m_repeated;
    
    /** Customization information for this component. */
    private ComponentCustom m_custom;
    
    /** Override for type specified in schema (<code>null</code> if none). */
    private QName m_overrideType;
    
    /** Number of times a component is used in code generation. */
    private int m_useCount;
    
    /**
     * Constructor.
     * 
     * @param comp
     * @param global containing global definition extension (<code>null</code> allowed only as special case when
     * calling this constructor from the global extension subclass constructor)
     */
    public ComponentExtension(OpenAttrBase comp, GlobalExtension global) {
        super(comp);
        m_global = global == null ? (GlobalExtension)this : global;
        if (comp instanceof IArity) {
            
            // element can only be optional or repeated when embedded, but no need to check since counts are forbidden
            //  on a global definition
            IArity particle = (IArity)comp;
            m_repeated = SchemaUtils.isRepeated(particle);
            if (comp.type() == SchemaBase.ELEMENT_TYPE) {
                m_optional = SchemaUtils.isOptionalElement((ElementElement)comp);
            } else {
                m_optional = SchemaUtils.isOptional(particle);
            }
            
        } else if (comp.type() == SchemaBase.ATTRIBUTE_TYPE) {
            
            // attribute can never be repeated, and can only be optional when embedded (rather than a global definition)
            m_repeated = false;
            m_optional = global != null && ((AttributeElement)comp).getUse() == AttributeElement.OPTIONAL_USE;
            
        }
    }
    
    /**
     * Check if component to be removed from schema.
     * 
     * @return removed flag
     */
    public boolean isRemoved() {
        return m_removed;
    }
    
    /**
     * Set flag for component to be removed from schema.
     * 
     * @param removed
     */
    public void setRemoved(boolean removed) {
        m_removed = removed;
    }
    
    /**
     * Check if component is to be ignored.
     *
     * @return ignored flag
     */
    public boolean isIgnored() {
        if (m_custom == null) {
            return false;
        } else {
            return m_custom.isIgnored();
        }
    }
    
    /**
     * Check if optional component.
     * 
     * @return optional
     */
    public boolean isOptional() {
        return m_optional;
    }
    
    /**
     * Set optional component.
     * 
     * @param optional
     */
    public void setOptional(boolean optional) {
        m_optional = optional;
    }
    
    /**
     * Check if repeated component.
     * 
     * @return repeated
     */
    public boolean isRepeated() {
        return m_repeated;
    }
    
    /**
     * Set repeated component.
     * 
     * @param repeated
     */
    public void setRepeated(boolean repeated) {
        m_repeated = repeated;
    }
    
    /**
     * Check if schema component is to be generated inline.
     *
     * @return <code>true</code> if inlined, <code>false</code> if not
     */
    public boolean isInlined() {
        if (m_custom == null) {
            return false;
        } else {
            return m_custom.isInlined();
        }
    }
    
    /**
     * Check if schema component is to be generated as a separate class.
     *
     * @return <code>true</code> if separate class, <code>false</code> if not
     */
    public boolean isSeparateClass() {
        if (m_custom == null) {
            return false;
        } else {
            return m_custom.isSeparateClass();
        }
    }

    /**
     * Get the containing global extension.
     *
     * @return global
     */
    public GlobalExtension getGlobal() {
        return m_global;
    }

    /**
     * Get override type.
     * 
     * @return type name (<code>null</code> if none)
     */
    public QName getOverrideType() {
        return m_overrideType;
    }
    
    /**
     * Set override type.
     * 
     * @param qname type name (<code>null</code> if none)
     */
    public void setOverrideType(QName qname) {
        m_overrideType = qname;
    }
    
    /**
     * Increment the use count for the component.
     * 
     * @return incremented use count
     */
    public int incrementUseCount() {
        return ++m_useCount;
    }
    
    /**
     * Get the use count for the component.
     *
     * @return use count
     */
    public int getUseCount() {
        return m_useCount;
    }
    
    /**
     * Get name to be used for generated class.
     *
     * @return class name (<code>null</code> if not set)
     */
    public String getClassName() {
        if (m_custom == null) {
            return null;
        } else {
            return m_custom.getClassName();
        }
    }
    
    /**
     * Get base name for corresponding property.
     *
     * @return property name (<code>null</code> if not set)
     */
    public String getBaseName() {
        if (m_custom == null) {
            return null;
        } else {
            return m_custom.getBaseName();
        }
    }
    
    /**
     * Get customization information for this component.
     *
     * @return custom
     */
    ComponentCustom getCustom() {
        return m_custom;
    }

    /**
     * Set customization information for this component.
     *
     * @param custom
     */
    void setCustom(ComponentCustom custom) {
        m_custom = custom;
    }
    
    /**
     * Get the innermost customization which applies to this component.
     *
     * @return customization
     */
    private NestingCustomBase getContainingCustom() {
        if (m_custom == null) {
            Object extension = getComponent().getParent().getExtension();
            if (extension instanceof ComponentExtension) {
                return ((ComponentExtension)extension).getContainingCustom();
            } else {
                return ((SchemaExtension)extension).getCustom();
            }
        } else {
            return m_custom;
        }
    }
    
    /**
     * Get the xs:any handling type code to be applied for this component. The default value is
     * {@link NestingCustomBase#ANY_DOM} if not overridden at any level.
     * 
     * @return code
     */
    public int getAnyType() {
        return getContainingCustom().getAnyType();
    }
    
    /**
     * Get the xs:choice handling type code to be applied for this component. The default value is
     * {@link NestingCustomBase#SELECTION_CHECKEDSET} if not overridden at any level.
     * 
     * @return code
     */
    public int getChoiceType() {
        return getContainingCustom().getChoiceType();
    }
    
    /**
     * Check if xs:choice selection state should be exposed for this component.
     * 
     * @return exposed flag
     */
    public boolean isChoiceExposed() {
        return getContainingCustom().isChoiceExposed();
    }
    
    /**
     * Get the xs:union handling type code to be applied for this component. The default value is
     * {@link NestingCustomBase#SELECTION_CHECKEDSET} if not overridden at any level.
     * 
     * @return code
     */
    public int getUnionType() {
        return getContainingCustom().getUnionType();
    }
    
    /**
     * Check if xs:union selection state should be exposed for this component.
     * 
     * @return exposed flag
     */
    public boolean isUnionExposed() {
        return getContainingCustom().isUnionExposed();
    }

    /**
     * Check for type substitution on a type reference, then record the reference.
     * TODO: how to handle substitutions across namespaces? the problem here is that the returned name needs to be
     * correct in the use context, which may be using a different namespace for the same definition (due to includes of
     * no-namespace schemas into a namespaced schema). the current code is only an interim fix that will not always work
     *
     * @param type original type
     * @param vctx validation context
     * @return replacement type (may be the same as the original type; <code>null</code> if to be deleted)
     */
    private QName replaceAndReference(QName type, ValidationContext vctx) {
        QName repl = getReplacementType(type);
        if (repl != type && s_logger.isDebugEnabled()) {
            s_logger.debug("Replacing type " + type + " with type " + repl + " in component " +
                SchemaUtils.describeComponent(getComponent()));
        }
        while (repl != null) {
            CommonTypeDefinition def = vctx.findType(repl);
            if (def == null) {
                
                // try substituting the original namespace, to work with no-namespace schemas
                if (repl.getUri() == null && type.getUri() != null) {
                    repl = new QName(type.getUri(), repl.getName());
                    def = vctx.findType(repl);
                }
                if (def == null) {
                    throw new IllegalStateException("Internal error - type definition not found");
                }
            }
            GlobalExtension exten = (GlobalExtension)def.getExtension();
            if (exten == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No extension found for referenced type " + SchemaUtils.describeComponent(def));
                }
                return repl;
            } else {
                if (exten.isRemoved()) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Reference to deleted type " + SchemaUtils.describeComponent(def));
                    }
                    return null;
                } else {
                    if (exten.getOverrideType() == null) {
                        exten.addReference(this);
                        m_global.addDependency(exten);
                        return repl;
                    } else {
                        repl = exten.getOverrideType();
                    }
                }
            }
        }
        return repl;
    }
    
    /**
     * Check a reference to a component. If the reference component has been deleted this just returns
     * <code>false</code>. If the component has not been deleted it counts the reference on that component, and records
     * the dependency from this component before returning <code>true</code>. For convenience, this may be called with a
     * <code>null</code> argument, which just returns <code>true</code>.
     *
     * @param comp component (call ignored if <code>null</code>)
     * @return <code>true</code> if reference to be kept, <code>false</code> if deleted
     */
    private boolean checkReference(OpenAttrBase comp) {
        if (comp != null) {
            GlobalExtension exten = (GlobalExtension)comp.getExtension();
            if (exten == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("No extension found for referenced component " +
                        SchemaUtils.describeComponent(comp));
                }
            } else {
                if (exten.isRemoved()) {
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Referenced to deleted component " + SchemaUtils.describeComponent(comp));
                    }
                    return false;
                } else {
                    exten.addReference(this);
                    m_global.addDependency(exten);
                }
            }
        }
        return true;
    }

    /**
     * Remove a child element. This checks to make sure the removal is valid, and also handles logging of the change.
     *
     * @param index
     */
    private void removeChild(int index) {
        SchemaBase child = getComponent().getChild(index);
        switch (child.type())
        {
            case SchemaBase.ALL_TYPE:
            case SchemaBase.ANY_TYPE:
            case SchemaBase.ANYATTRIBUTE_TYPE:
            case SchemaBase.ATTRIBUTE_TYPE:
            case SchemaBase.ATTRIBUTEGROUP_TYPE:
            case SchemaBase.CHOICE_TYPE:
            case SchemaBase.COMPLEXTYPE_TYPE:
            case SchemaBase.ELEMENT_TYPE:
            case SchemaBase.ENUMERATION_TYPE:
            case SchemaBase.FRACTIONDIGITS_TYPE:
            case SchemaBase.GROUP_TYPE:
            case SchemaBase.LENGTH_TYPE:
            case SchemaBase.MAXEXCLUSIVE_TYPE:
            case SchemaBase.MAXINCLUSIVE_TYPE:
            case SchemaBase.MAXLENGTH_TYPE:
            case SchemaBase.MINEXCLUSIVE_TYPE:
            case SchemaBase.MININCLUSIVE_TYPE:
            case SchemaBase.MINLENGTH_TYPE:
            case SchemaBase.PATTERN_TYPE:
            case SchemaBase.SEQUENCE_TYPE:
            case SchemaBase.SIMPLETYPE_TYPE:
            case SchemaBase.TOTALDIGITS_TYPE:
            case SchemaBase.WHITESPACE_TYPE:
            {
                // definition component - just delete from the parent structure
                getComponent().detachChild(index);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Removed component " + SchemaUtils.describeComponent(child) +
                        " as per extension");
                }
                break;
            }
            
            default:
                throw new IllegalStateException("Internal error: element type '" + child.name() +
                    "' cannot be deleted");
        }
    }

    /**
     * Apply extensions to schema definition component, deleting components flagged for skipping and substituting types
     * as configured. This code is not intended to handle the deletion of global definition components, which should be
     * removed separately.
     * 
     * @param vctx validation context
     */
    public void applyAndCountUsage(ValidationContext vctx) {
        
        // handle type substitutions for this component
        OpenAttrBase component = getComponent();
        boolean delete = false;
        switch (component.type())
        {
            
            case SchemaBase.ATTRIBUTE_TYPE:
            {
                if (isIgnored()) {
                    delete = true;
                } else {
                    AttributeElement attr = ((AttributeElement)component);
                    if (m_overrideType != null) {
                        attr.setType(m_overrideType);
                    }
                    QName type = attr.getType();
                    if (type == null) {
                        AttributeElement ref = attr.getReference();
                        if (ref != null) {
                            delete = !checkReference(ref);
                        } else if (!attr.isInlineType()) {
                            attr.setType(SchemaTypes.ANY_SIMPLE_TYPE.getQName());
                        }
                    } else {
                        QName repl = replaceAndReference(type, vctx);
                        if (repl == null) {
                            // TODO optionally make sure the attribute is optional?
                            delete = true;
                        } else if (repl != type) {
                            attr.setType(repl);
                        }
                    }
                }
                break;
            }
            
            case SchemaBase.ATTRIBUTEGROUP_TYPE:
            {
                if (component instanceof AttributeGroupRefElement) {
                    delete = !checkReference(((AttributeGroupRefElement)component).getReference());
                }
                break;
            }
            
            case SchemaBase.ELEMENT_TYPE:
            {
                ElementElement elem = ((ElementElement)component);
                if (isIgnored()) {
                    
                    // reference or definition determines handling
                    QName ref = elem.getRef();
                    if (ref == null) {
                        
                        // definition, just delete all content
                        for (int i = 0; i < elem.getChildCount(); i++) {
                            elem.detachChild(i);
                        }
                        elem.compactChildren();
                        
                    } else {
                        
                        // reference may be to other namespace, so convert to qualified name string
                        StringBuffer buff = new StringBuffer();
                        buff.append('{');
                        if (ref.getUri() != null) {
                            buff.append(ref.getUri());
                        }
                        buff.append('}');
                        buff.append(ref.getName());
                        elem.setName(buff.toString());
                        elem.setRef(null);
                        
                    }
                    elem.setType(SchemaTypes.ANY_TYPE.getQName());
                    
                } else {
                    if (m_overrideType != null) {
                        elem.setType(m_overrideType);
                    }
                    QName type = elem.getType();
                    if (type == null) {
                        delete = !checkReference(elem.getReference());
                    } else {
                        QName repl = replaceAndReference(type, vctx);
                        if (repl == null) {
                            // TODO optionally make sure the element is optional?
                            delete = true;
                        } else if (repl != type) {
                            elem.setType(repl);
                        }
                    }
                }
                break;
            }
            
            case SchemaBase.EXTENSION_TYPE:
            case SchemaBase.RESTRICTION_TYPE:
            {
                CommonTypeDerivation deriv = (CommonTypeDerivation)component;
                QName type = deriv.getBase();
                if (type != null) {
                    QName repl = replaceAndReference(type, vctx);
                    if (repl == null) {
                        delete = true;
                    } else if (repl != type) {
                        deriv.setBase(repl);
                    }
                }
                break;
            }
            
            case SchemaBase.GROUP_TYPE:
            {
                if (component instanceof GroupRefElement) {
                    delete = !checkReference(((GroupRefElement)component).getReference());
                }
                break;
            }
                
            case SchemaBase.LIST_TYPE:
            {
                ListElement list = ((ListElement)component);
/*                    // not currently supported - is it needed?
                    if (m_overrideType != null) {
                        list.setItemType(m_overrideType);
                    }   */
                QName type = list.getItemType();
                if (type != null) {
                    QName repl = replaceAndReference(type, vctx);
                    if (repl == null) {
                        delete = true;
                    } else if (repl != type) {
                        list.setItemType(repl);
                    }
                }
                break;
            }
            
            case SchemaBase.UNION_TYPE:
            {
                UnionElement union = ((UnionElement)component);
                QName[] types = union.getMemberTypes();
                if (types != null) {
                    ArrayList repls = new ArrayList();
                    boolean changed = false;
                    for (int i = 0; i < types.length; i++) {
                        QName type = types[i];
                        QName repl = replaceAndReference(type, vctx);
                        changed = changed || repl != type;
                        if (repl != null) {
                            repls.add(repl);
                        }
                    }
                    if (changed) {
                        if (repls.size() > 0) {
                            union.setMemberTypes((QName[])repls.toArray(new QName[repls.size()]));
                        } else {
                            union.setMemberTypes(null);
                        }
                    }
                }
                break;
            }
                
        }
        if (delete) {
            
            // raise deletion to containing removable component
            SchemaBase parent = component;
            SchemaBase remove = null;
            loop: while (parent != null) {
                switch (parent.type())
                {
                    
                    case SchemaBase.ATTRIBUTE_TYPE:
                    case SchemaBase.ATTRIBUTEGROUP_TYPE:
                    case SchemaBase.ELEMENT_TYPE:
                    case SchemaBase.GROUP_TYPE:
                        remove = parent;
                        break loop;
                    
                    case SchemaBase.COMPLEXTYPE_TYPE:
                    case SchemaBase.SIMPLETYPE_TYPE:
                        remove = parent;
                        parent = parent.getParent();
                        break;
                    
                    default:
                        if (remove == null) {
                            parent = parent.getParent();
                            break;
                        } else {
                            break loop;
                        }
                        
                }
            }
            if (remove == null) {
                throw new IllegalStateException("Internal error: no removable ancestor found for component "
                    + SchemaUtils.describeComponent(component));
            } else {
                ((ComponentExtension)remove.getExtension()).setRemoved(true);
            }
            
        } else {
            
            // process all child component extensions
            boolean modified = false;
            for (int i = 0; i < component.getChildCount(); i++) {
                SchemaBase child = component.getChild(i);
                ComponentExtension exten = (ComponentExtension)child.getExtension();
                if (!exten.isRemoved()) {
                    exten.applyAndCountUsage(vctx);
                }
                if (exten.isRemoved()) {
                    removeChild(i);
                    modified = true;
                }
            }
            if (modified) {
                component.compactChildren();
            }
        }
    }

    /**
     * Try to replace type definition with substitute type from derivation. If 
     *
     * @param lead prefix text for indentation of logging messages
     * @param topcomp schema component being normalized
     * @param childcomp current child of schema component being normalized
     * @param derive type derivation supplying substitute type
     * @return <code>true</code> if type modified, <code>false</code> if not
     */
    private boolean substituteTypeDerivation(String lead, OpenAttrBase topcomp, OpenAttrBase childcomp,
        CommonTypeDerivation derive) {
        SchemaBase parent = topcomp;
        while (parent != null) {
            switch (parent.type())
            {
                
                case SchemaBase.ATTRIBUTE_TYPE:
                {
                    // set the attribute type to the specified type
                    AttributeElement attr = (AttributeElement)parent;
                    attr.setType(derive.getBase());
                    return true;
                }
                
                case SchemaBase.COMPLEXTYPE_TYPE:
                {
                    if (parent.isGlobal()) {
                        GlobalExtension global = (GlobalExtension)parent.getExtension();
                        if (!global.isIncluded() && global.isPreferInline()) {
                            ComplexTypeElement type = (ComplexTypeElement)parent;
                            ComponentExtension typeext = (ComponentExtension)type.getExtension();
                            typeext.setOverrideType(derive.getBase());
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug(lead + "set substitution for global complex type " +
                                    SchemaUtils.componentPath(type) + " to base type from derivation " +
                                    SchemaUtils.componentPath(childcomp));
                            }
                            return true;
                        }
                    }
                    break;
                }
                
                case SchemaBase.ELEMENT_TYPE:
                {
                    // set the element type to the restriction base
                    ElementElement elem = (ElementElement)parent;
                    elem.setTypeDefinition(derive.getBaseType());
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug(lead + "replaced element " + SchemaUtils.componentPath(elem) +
                            " type with base type from derivation " + SchemaUtils.componentPath(childcomp));
                    }
                    return true;
                }
                
                case SchemaBase.SIMPLETYPE_TYPE:
                {
                    // replace type only if global type definition
                    if (parent.isGlobal()) {
                        GlobalExtension global = (GlobalExtension)parent.getExtension();
                        if (!global.isIncluded()) {
                            SimpleTypeElement type = (SimpleTypeElement)parent;
                            ComponentExtension parentext = (ComponentExtension)type.getExtension();
                            parentext.setOverrideType(derive.getBase());
                            return true;
                        }
                    }
                    break;
                }
                
                case SchemaBase.UNION_TYPE:
                {
                    // this will be handled by union normalization, so just leave in place
                    return false;
                }
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Normalize the child schema definition. This recursively traverses the schema model tree rooted in the
     * component associated with this extension, normalizing each child component.
     * 
     * TODO: handle revalidation for changed subtrees
     * 
     * @param depth nesting depth for validation
     * @return <code>true</code> if any part of tree under this component modified, <code>false</code> if not
     */
    protected boolean normalize(int depth) {
        
        // initialize debug handling
        String path = null;
        String lead = null;
        OpenAttrBase topcomp = getComponent();
        if (s_logger.isDebugEnabled()) {
            path = SchemaUtils.componentPath(topcomp);
            lead = SchemaUtils.getIndentation(depth);
            s_logger.debug(lead + "entering normalization for node " + path);
        }
        
        // normalize each child component in turn
        int count = topcomp.getChildCount();
        boolean modified = false;
        boolean compact = false;
        for (int i = 0; i < count; i++) {
            SchemaBase child = topcomp.getChild(i);
            if (child instanceof OpenAttrBase) {
                
                // start by normalizing the child component content
                OpenAttrBase childcomp = (OpenAttrBase)child;
                ComponentExtension childext = (ComponentExtension)childcomp.getExtension();
                if (childext.normalize(depth+1)) {
                    modified = true;
                }
                
                // check for child components with special normalization handling
                switch (childcomp.type()) {
                    
                    case SchemaBase.ALL_TYPE:
                    case SchemaBase.CHOICE_TYPE:
                    case SchemaBase.SEQUENCE_TYPE:
                    {
                        // child component is a compositor, so see if it can be deleted or replaced
                        CommonCompositorDefinition compositor = (CommonCompositorDefinition)childcomp;
                        int size = compositor.getParticleList().size();
                        if (size == 0) {
                            
                            // empty compositor, just remove (always safe to do this)
                            childext.setRemoved(true);
                            modified = true;
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug(lead + "eliminated empty compositor " + path);
                            }
                            
                        } else if (size == 1) {
                            
                            // single component in compositor, first try to convert compositor to singleton
                            OpenAttrBase grandchild = (OpenAttrBase)compositor.getParticleList().get(0);
                            IArity particle = (IArity)grandchild;
                            if (SchemaUtils.isRepeated(compositor)) {
                                if (!SchemaUtils.isRepeated(particle)) {
                                    
                                    // repeated compositor with non-repeated particle, so pass repeat to particle
                                    if (s_logger.isDebugEnabled()) {
                                        s_logger.debug(lead + "passing repeat from compositor " +
                                            SchemaUtils.componentPath(childcomp) + " to only child " +
                                            SchemaUtils.describeComponent(grandchild));
                                    }
                                    particle.setMaxOccurs(compositor.getMaxOccurs());
                                    ((ComponentExtension)grandchild.getExtension()).setRepeated(true);
                                    compositor.setMaxOccurs(null);
                                    ((ComponentExtension)compositor.getExtension()).setRepeated(false);
                                    
                                } else if ((compositor.getMaxOccurs().isUnbounded() &&
                                    particle.getMaxOccurs().isUnbounded())) {
                                    
                                    // unbounded compositor with unbounded particle, just wipe repeat from compositor
                                    if (s_logger.isDebugEnabled()) {
                                        s_logger.debug(lead + "clearing unbounded from compositor " +
                                            SchemaUtils.componentPath(childcomp) + " with unbounded only child " +
                                            SchemaUtils.describeComponent(grandchild));
                                    }
                                    compositor.setMaxOccurs(null);
                                    ((ComponentExtension)compositor.getExtension()).setRepeated(false);
                                    
                                }
                            }
                            if (SchemaUtils.isOptional(compositor)) {
                                if (SchemaUtils.isOptional(particle)) {
                                    
                                    // optional compositor with optional particle, just wipe optional from compositor
                                    if (s_logger.isDebugEnabled()) {
                                        s_logger.debug(lead + "clearing optional from compositor " +
                                            SchemaUtils.componentPath(childcomp) + " with optional only child " +
                                            SchemaUtils.describeComponent(grandchild));
                                    }
                                    compositor.setMinOccurs(null);
                                    ((ComponentExtension)compositor.getExtension()).setOptional(false);
                                    
                                } else if (Count.isCountEqual(1, particle.getMinOccurs())) {
                                    
                                    // optional compositor with required particle, so pass optional to particle
                                    if (s_logger.isDebugEnabled()) {
                                        s_logger.debug(lead + "passing optional from compositor " +
                                            SchemaUtils.componentPath(childcomp) + " to required only child " +
                                            SchemaUtils.describeComponent(grandchild));
                                    }
                                    particle.setMinOccurs(Count.COUNT_ZERO);
                                    ((ComponentExtension)grandchild.getExtension()).setOptional(true);
                                    compositor.setMinOccurs(null);
                                    ((ComponentExtension)compositor.getExtension()).setOptional(false);
                                    
                                }
                            }
                            
                            // check if top component is also a compositor
                            if (topcomp instanceof CommonCompositorDefinition) {
                                
                                // nested compositor, check if can be simplified
                                if (SchemaUtils.isSingleton(compositor)) {
                                    
                                    // nested singleton compositor with only child, just replace with the child
                                    topcomp.replaceChild(i, grandchild);
                                    modified = true;
                                    if (s_logger.isDebugEnabled()) {
                                        s_logger.debug(lead + "replacing singleton compositor " +
                                            SchemaUtils.componentPath(childcomp) + " with only child " +
                                            SchemaUtils.describeComponent(grandchild));
                                    }
                                    
                                } else if (compositor instanceof ChoiceElement) {
                                    
                                    // replace choice with sequence to simplify handling, since just one particle
                                    if (s_logger.isDebugEnabled()) {
                                        s_logger.debug(lead + "substituting sequence for choice " +
                                            SchemaUtils.componentPath(childcomp) + " with only one child");
                                    }
                                    SequenceElement sequence = new SequenceElement();
                                    sequence.setAnnotation(compositor.getAnnotation());
                                    sequence.setExtension(compositor.getExtension());
                                    sequence.setMaxOccurs(compositor.getMaxOccurs());
                                    sequence.setMinOccurs(compositor.getMinOccurs());
                                    sequence.getParticleList().add(grandchild);
                                    topcomp.replaceChild(i, sequence);
                                    modified = true;
                                    
                                }
                            }
                        }
                        break;
                    }
                    
                    case SchemaBase.EXTENSION_TYPE:
                    {
                        if (childcomp instanceof SimpleExtensionElement) {
                            
                            // replace empty simple type extension with base type
                            SimpleExtensionElement extend = (SimpleExtensionElement)childcomp;
                            if (extend.getAttributeList().size() == 0 && extend.getAnyAttribute() == null) {
                                modified = substituteTypeDerivation(lead, topcomp, childcomp, extend);
                            }
                            
                        } else {
                            
                            // replace empty complex type extension with base type
                            ComplexExtensionElement extend = (ComplexExtensionElement)child;
                            if (extend.getContentDefinition() == null && extend.getAttributeList().size() == 0 &&
                                extend.getAnyAttribute() == null) {
                                modified = substituteTypeDerivation(lead, topcomp, childcomp, extend);
                            }
                            
                        }
                        break;
                    }
                    
                    case SchemaBase.RESTRICTION_TYPE:
                    {
                        if (childcomp instanceof SimpleRestrictionElement) {
                            
                            // replace simple type restriction with base type unless it has facets
                            SimpleRestrictionElement restrict = (SimpleRestrictionElement)childcomp;
                            if (restrict.getFacetsList().size() == 0 && restrict.getBase() != null) {
                                QName base = restrict.getBase();
                                if (base == null) {
                                    
                                    // derivation using inline base type, just eliminate the restriction
                                    
                                } else {
                                    modified = substituteTypeDerivation(lead, topcomp, childcomp, restrict);
                                }
                            }
                            
                        } else {
                            
                            // always replace complex type restriction with base type
                            ComplexRestrictionElement restrict = (ComplexRestrictionElement)child;
                            modified = substituteTypeDerivation(lead, topcomp, childcomp, restrict);
                            
                        }
                        break;
                    }
                }
                
                // delete child component if flagged for removal
                if (childext.isRemoved()) {
                    removeChild(i);
                    compact = true;
                }
            }
        }
        if (compact) {
            topcomp.compactChildren();
            modified = true;
        }
        
        // handle union normalization after all children have been normalized
        if (topcomp.type() == SchemaBase.UNION_TYPE) {
            
            // start by checking duplicates in the member types
            compact = false;
            UnionElement union = (UnionElement)topcomp;
            Set typeset = new HashSet();
            ArrayList keeptypes = new ArrayList();
            QName[] membertypes = union.getMemberTypes();
            if (membertypes != null) {
                for (int i = 0; i < membertypes.length; i++) {
                    QName type = membertypes[i];
                    if (typeset.contains(type)) {
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug(lead + "removed redundant member type " + type + " from " + path);
                        }
                    } else {
                        typeset.add(type);
                        keeptypes.add(type);
                    }
                }
            }
            
            // then check inline types for duplicates, simplifying where possible
            count = union.getChildCount();
            for (int i = 0; i < count; i++) {
                SchemaBase child = topcomp.getChild(i);
                if (child instanceof OpenAttrBase) {
                    
                    // child schema component must be an inline simple type definition
                    SimpleTypeElement simple = (SimpleTypeElement)child;
                    boolean keeper = false;
                    SchemaBase derivation = simple.getDerivation();
                    QName repltype = null;
                    if (derivation != null) {
                        
                        // keep the inline definition by default, but check for replacement cases
                        keeper = true;
                        if (derivation.type() == SchemaBase.RESTRICTION_TYPE) {
                            SimpleRestrictionElement innerrestrict = (SimpleRestrictionElement)derivation;
                            if (innerrestrict.getChildCount() == 0) {
                                
                                // treat empty restriction the same as a member type from list
                                repltype = innerrestrict.getBase();
                                if (typeset.contains(repltype)) {
                                    keeper = false;
                                }
                                
                            }
                        } else if (derivation.type() == SchemaBase.UNION_TYPE) {
                            UnionElement innerunion = (UnionElement)derivation;
                            QName[] innertypes = innerunion.getMemberTypes();
                            FilteredSegmentList innerinlines = innerunion.getInlineBaseList();
                            if (innertypes.length + innerinlines.size() == 1) {
                                
                                // replace child union with single type from union
                                if (innertypes.length == 1) {
                                    
                                    // global type reference, delete the child union and check if new type
                                    repltype = innertypes[0];
                                    if (typeset.contains(repltype)) {
                                        keeper = false;
                                    }
                                    
                                } else {
                                    
                                    // inline type definition, replace the child union with the inline type
                                    union.replaceChild(i, (SchemaBase)innerinlines.get(0));
                                    
                                }
                            }
                        }
                    }
                    if (keeper) {
                        if (repltype != null) {
                            
                            // convert unnecessary inline type to member type
                            typeset.add(repltype);
                            removeChild(i);
                            compact = true;
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug(lead + "converted inline type " + SchemaUtils.describeComponent(topcomp) +
                                    " to member type in " + path);
                            }
                            
                        }
                    } else {
                        
                        // remove inline definition that matches already-processed type
                        removeChild(i);
                        compact = true;
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug(lead + "removed redundant inline type from " + path);
                        }
                        
                    }
                }
            }
            
            // set the new list of member types for union
            QName[] newtypes = null;
            if (keeptypes.size() > 0) {
                newtypes = (QName[])keeptypes.toArray(new QName[keeptypes.size()]);
            }
            union.setMemberTypes(newtypes);
            
            // clean up for any deleted child components
            if (compact) {
                topcomp.compactChildren();
                modified = true;
            }
            
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug(lead + "exiting normalization for node " + path + " with modified " + modified);
        }
        return modified;
    }
}
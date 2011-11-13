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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jibx.binding.classes.ClassCache;
import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.util.IClass;
import org.jibx.util.IClassLocator;
import org.jibx.util.StringArray;

/**
 * Global customization information. This includes some options specific to the &lt;binding> element of the definition,
 * as well as controls for structuring of the generated binding(s). It handles the binding customization child elements
 * directly, by invoking the abstract unmarshallers for the child elements to process the content. It also allows for
 * extension elements which are not part of the binding customization structure, as long as the binding in use defines
 * the unmarshalling for these elements.
 * 
 * @author Dennis M. Sosnoski
 */
public class GlobalCustom extends NestingBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "add-constructors", "direction", "force-classes", "track-source" },
        NestingBase.s_allowedAttributes);
    
    /** Element name in XML customization file. */
    public static final String ELEMENT_NAME = "custom";
    
    //
    // Value set information
    
    public static final int IN_BINDING = 0;
    
    public static final int OUT_BINDING = 1;
    
    public static final int BOTH_BINDING = 2;
    
    /* package */static final EnumSet s_directionEnum =
        new EnumSet(IN_BINDING, new String[] { "input", "output", "both" });
    
    //
    // Instance data
    
    // structure for package hierarchy
    private Map m_packageMap;
    
    // class locator
    private final IClassLocator m_classLocator;
    
    // extension elements
    private List m_extensionChildren;
    
    // values applied directly to <binding> element (or to structure)
    private boolean m_addConstructors;
    
    private boolean m_forceClasses;
    
    private boolean m_trackSource;
    
    private boolean m_namespaceModular;
    
    private boolean m_isInput;
    
    private boolean m_isOutput;
    
    private ArrayList m_unmarshalledClasses;
    
    /**
     * Constructor with class locator supplied.
     * 
     * @param loc
     */
    public GlobalCustom(IClassLocator loc) {
        super(null);
        m_packageMap = new HashMap();
        m_classLocator = loc;
        PackageCustom dflt = new PackageCustom("", "", this);
        m_packageMap.put("", dflt);
        m_isInput = true;
        m_isOutput = true;
        m_unmarshalledClasses = new ArrayList();
    }
    
    /**
     * Constructor. This always creates the default package as the only direct child, since other packages will be
     * treated as children of the default package.
     */
    public GlobalCustom() {
        this(new ClassCache.ClassCacheLocator());
    }
    
    /**
     * Make sure all attributes are defined.
     * 
     * @param uctx unmarshalling context
     */
    private void preSet(IUnmarshallingContext uctx) {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /**
     * Get global customizations root.
     * 
     * @return global customization
     */
    public GlobalCustom getGlobal() {
        return this;
    }
    
    /**
     * Get list of unmarshalled classes. This list is populated by the custom unmarshalling code as the customizations
     * document is unmarshalled.
     *
     * @return list
     */
    public ArrayList getUnmarshalledClasses() {
        return m_unmarshalledClasses;
    }
    
    //
    // Access methods for values applied only at top level

    /**
     * Get 'add-constructors' setting.
     * 
     * @return 'add-constructors' value
     */
    public boolean isAddConstructors() {
        return m_addConstructors;
    }
    
    /**
     * Set 'add-constructors' value.
     * 
     * @param add 'add-constructors' value
     */
    public void setAddConstructors(boolean add) {
        m_addConstructors = add;
    }
    
    /**
     * Get 'force-classes' setting.
     * 
     * @return 'force-classes' value
     */
    public boolean isForceClasses() {
        return m_forceClasses;
    }
    
    /**
     * Set 'force-classes' value.
     * 
     * @param force 'force-classes' value
     */
    public void setForceClasses(boolean force) {
        m_forceClasses = force;
    }
    
    /**
     * Get 'track-source' attribute value.
     * 
     * @return 'track-source' value
     */
    public boolean isTrackSource() {
        return m_trackSource;
    }
    
    /**
     * Set 'track-source' value.
     * 
     * @param track 'track-source' value
     */
    public void setTrackSource(boolean track) {
        m_trackSource = track;
    }
    
    /**
     * Check for an input binding.
     * 
     * @return input flag
     */
    public boolean isInput() {
        return m_isInput;
    }
    
    /**
     * Set input binding flag.
     * 
     * @param input
     */
    public void setInput(boolean input) {
        m_isInput = input;
    }
    
    /**
     * Check for an output binding.
     * 
     * @return output flag
     */
    public boolean isOutput() {
        return m_isOutput;
    }
    
    /**
     * Set output binding falg.
     * 
     * @param output
     */
    public void setOutput(boolean output) {
        m_isOutput = output;
    }
    
    /**
     * Get class locator.
     * 
     * @return locator
     */
    protected IClassLocator getClassLocator() {
        return m_classLocator;
    }
    
    /**
     * Get class information.
     * 
     * @param type fully-qualified class name
     * @return information, or <code>null</code> if unable to load
     */
    public IClass getClassInfo(String type) {
        return m_classLocator.getClassInfo(type);
    }
    
    /**
     * Get the extension elements used in this customization. This does not include the &lt;package> or &lt;class> child
     * elements, which are added directly to the customization structures.
     * 
     * @return child list
     */
    public List getExtensionChildren() {
        if (m_extensionChildren == null) {
            return Collections.EMPTY_LIST;
        } else {
            return m_extensionChildren;
        }
    }
    
    /**
     * Internal method used during unmarshalling to add a child extension element.
     * 
     * @param child
     */
    protected void internalAddExtensionChild(Object child) {
        if (m_extensionChildren == null) {
            m_extensionChildren = new ArrayList();
        }
        m_extensionChildren.add(child);
    }
    
    /**
     * Add a child extension element. This both adds the child to the list and invokes the extension element's
     * {@link IApply#apply(IClassLocator)} method, if present.
     * 
     * @param child
     */
    public void addExtensionChild(Object child) {
        internalAddExtensionChild(child);
        if (child instanceof IApply) {
            ((IApply)child).apply(m_classLocator);
        }
    }
    
    /**
     * Check if a class is included in the customization information. This method does not alter the structures in any
     * way, it only checks if the class customization information is part of the existing structure.
     * 
     * @param type fully qualified class name
     * @return <code>true</code> if class includes, <code>false</code> if not
     */
    public boolean isClassUsed(String type) {
        int split = type.lastIndexOf('.');
        PackageCustom pack = (PackageCustom)m_packageMap.get(split < 0 ? "" : type.substring(0, split));
        if (pack == null) {
            return false;
        } else {
            return pack.getClassCustomization(type.substring(split + 1)) != null;
        }
    }
    
    /**
     * Get class customization information.
     * 
     * @param type fully qualified class name
     * @return class information (<code>null</code> if not defined)
     */
    public ClassCustom getClassCustomization(String type) {
        int split = type.lastIndexOf('.');
        PackageCustom pack = (PackageCustom)m_packageMap.get(split < 0 ? "" : type.substring(0, split));
        if (pack == null) {
            return null;
        } else {
            return pack.getClassCustomization(type.substring(split + 1));
        }
    }
    
    /**
     * Build new class customization information. This creates the customization information and adds it to the internal
     * structures, initializing all values based on the settings inherited from &lt;package> and &lt;global> elements of
     * the structure. This method should only be used after first calling {@link #getClassCustomization(String)} and
     * obtaining a <code>null</code> result.
     * 
     * @param type fully qualified class name
     * @return class information
     */
    private ClassCustom buildClassCustomization(String type) {
        int split = type.lastIndexOf('.');
        PackageCustom pack = split < 0 ? getPackage("") : getPackage(type.substring(0, split));
        ClassCustom clas = pack.addClassCustomization(type.substring(split + 1));
        return clas;
    }
    
    /**
     * Get class customization information, creating it if it doesn't already exist. This internal method supplies the
     * class information in uninitialized form, so that data can be unmarshalled before initialization.
     * 
     * @param type fully qualified class name
     * @return class information
     */
    private ClassCustom forceClassCustomization(String type) {
        ClassCustom custom = getClassCustomization(type);
        if (custom == null) {
            custom = buildClassCustomization(type);
        }
        return custom;
    }
    
    /**
     * Get initialized class customization information, creating it if it doesn't already exist.
     * 
     * @param type fully qualified class name
     * @return class information
     */
    public ClassCustom addClassCustomization(String type) {
        ClassCustom custom = getClassCustomization(type);
        if (custom == null) {
            custom = buildClassCustomization(type);
            ((PackageCustom)custom.getParent()).fixNamespace();
            custom.apply(m_classLocator);
        }
        return custom;
    }
    
    /**
     * Check if type represents a known mapping.
     * 
     * @param type fully qualified class name
     * @return known mapping flag
     */
    public boolean isKnownMapping(String type) {
        // TODO: add known mappings for org.w3c.dom.*, etc.
        return false;
    }
    
    /**
     * Direction set text method. This is intended for use during unmarshalling. TODO: add validation
     * 
     * @param text
     * @param ictx
     */
    private void setDirectionText(String text, IUnmarshallingContext ictx) {
        int direct = s_directionEnum.getValue(text);
        if (direct < 0) {
            throw new IllegalArgumentException("Value '" + text + "' not recognized for 'direction' attribute");
        } else {
            m_isInput = direct != OUT_BINDING;
            m_isOutput = direct != IN_BINDING;
        }
    }
    
    /**
     * Direction get text method. This is intended for use during marshalling.
     * 
     * @return text
     */
    private String getDirectionText() {
        if (m_isInput && m_isOutput) {
            return s_directionEnum.getName(BOTH_BINDING);
        } else if (m_isInput) {
            return s_directionEnum.getName(IN_BINDING);
        } else {
            return s_directionEnum.getName(OUT_BINDING);
        }
    }
    
    /**
     * Initialize the global default namespace, along with special classes with built-in defaults. This needs to be done
     * as a separate step before unmarshalling, so that the special classes are available for use.
     */
    public void initClasses() {
        
        // set default namespace
        if (getNamespace() == null) {
            setNamespace(getSpecifiedNamespace());
        }
        
        // create information for some special classes
        ClassCustom custom = forceClassCustomization("java.util.List");
        if (custom.getCreateType() == null && custom.getFactoryMethod() == null) {
            custom.setCreateType("java.util.ArrayList");
        }
        custom = forceClassCustomization("java.util.Set");
        if (custom.getCreateType() == null && custom.getFactoryMethod() == null) {
            custom.setCreateType("java.util.HashSet");
        }
        custom = forceClassCustomization("java.util.Collection");
        if (custom.getCreateType() == null && custom.getFactoryMethod() == null) {
            custom.setCreateType("java.util.ArrayList");
        }
    }
    
    /**
     * Fills in class information based on inspection of the actual class data. This needs to be done as a separate step
     * following unmarshalling, so that the full details of the unmarshalled customizations are available.
     */
    public void fillClasses() {
        
        // fill in details for all packages
        for (Iterator iter = m_packageMap.values().iterator(); iter.hasNext();) {
            PackageCustom pack = (PackageCustom)iter.next();
            pack.fixNamespace();
            pack.apply(m_classLocator);
        }
        
        // fill in details for any extension elements
        if (m_extensionChildren != null) {
            for (int i = 0; i < m_extensionChildren.size(); i++) {
                Object child = m_extensionChildren.get(i);
                if (child instanceof IApply) {
                    ((IApply)child).apply(m_classLocator);
                }
            }
        }
    }
    
    //
    // Package structure support
    
    /**
     * Get package customizations. If the requested package is already defined the existing instance will be returned,
     * otherwise a new instance will be created (along with any ancestor packages) and added to the structure.
     * 
     * @param name
     * @return package
     */
    public PackageCustom getPackage(String name) {
        
        // find existing package information
        PackageCustom pack = (PackageCustom)m_packageMap.get(name);
        if (pack == null) {
            
            // create new package information
            PackageCustom parent = null;
            int split = name.lastIndexOf('.');
            String simple = name;
            String full = name;
            if (split > 0) {
                parent = getPackage(name.substring(0, split));
                simple = name.substring(split + 1);
                full = parent.getName() + '.' + simple;
            } else if (name.length() > 0) {
                parent = getPackage("");
            }
            pack = new PackageCustom(simple, full, parent);
            m_packageMap.put(name, pack);
        }
        return pack;
    }
    
    /**
     * Unmarshaller implementation for class. This handles the nested structure of packages and classes, using the
     * abstract mappings defined by the binding to handle all the actual details.
     */
    public static class Mapper implements IUnmarshaller
    {
        public boolean isPresent(IUnmarshallingContext ictx) throws JiBXException {
            return true;
        }
        
        /**
         * Build the fully-qualified name for a package or class by appending the supplied name attribute value to the
         * fully-qualified name of the containing package.
         * 
         * @param contain
         * @param ctx
         * @throws JiBXException
         */
        private String buildFullName(PackageCustom contain, UnmarshallingContext ctx) throws JiBXException {
            String lead = "";
            if (contain != null) {
                lead = contain.getName() + '.';
            }
            return lead + ctx.attributeText(null, "name");
        }
        
        /**
         * Unmarshal <b>package</b> element. This calls itself recursively to handle nested <b>package</b> elements, and
         * calls {@link #unmarshalClass(GlobalCustom, PackageCustom, UnmarshallingContext)} to handle nested
         * <b>class</b> elements.
         *
         * @param global root customizations
         * @param contain containing package
         * @param ctx unmarshalling context
         * @return unmarshalled package data
         * @throws JiBXException
         */
        private PackageCustom unmarshalPackage(GlobalCustom global, PackageCustom contain, UnmarshallingContext ctx)
            throws JiBXException {
            
            // create class instance and populate mapped values
            PackageCustom inst = global.getPackage(buildFullName(contain, ctx));
            ctx.getUnmarshaller("package").unmarshal(inst, ctx);
            inst.fixNamespace();
            
            // handle nested package and class information
            while (ctx.isStart()) {
                String element = ctx.getName();
                if (PackageCustom.ELEMENT_NAME.equals(element)) {
                    unmarshalPackage(global, inst, ctx);
                } else if (ClassCustom.ELEMENT_NAME.equals(element)) {
                    unmarshalClass(global, inst, ctx);
                }
            }
            ctx.parsePastEndTag(null, PackageCustom.ELEMENT_NAME);
            return inst;
        }
        
        /**
         * Unmarshal <b>class</b> element. This calls itself recursively to handle nested <b>class</b> elements, and
         * calls {@link #unmarshalClass(GlobalCustom, PackageCustom, UnmarshallingContext)} to handle nested
         * <b>class</b> elements.
         *
         * @param global root customizations
         * @param contain containing package
         * @param ctx unmarshalling context
         * @return unmarshalled class data
         * @throws JiBXException
         */
        private ClassCustom unmarshalClass(GlobalCustom global, PackageCustom contain, UnmarshallingContext ctx)
            throws JiBXException {
            
            // create class instance and populate mapped values
            String name = buildFullName(contain, ctx);
            int split = name.lastIndexOf('.');
            PackageCustom pack = global.getPackage(split < 0 ? "" : name.substring(0, split));
            pack.fixNamespace();
            String simple = name.substring(split + 1);
            ClassCustom inst = pack.getClassCustomization(simple);
            if (inst == null) {
                inst = pack.addClassCustomization(simple);
            }
            ctx.getUnmarshaller("class").unmarshal(inst, ctx);
            ctx.parsePastEndTag(null, ClassCustom.ELEMENT_NAME);
            
            // add to list unmarshalled
            global.getUnmarshalledClasses().add(inst);
            return inst;
        }
        
        /**
         * Unmarshal root element of customizations. This expects to handle the actual root element of the binding
         * directly, meaning it should always be invoked by using the
         * {@link org.jibx.runtime.IUnmarshallable#unmarshal(IUnmarshallingContext)} method. The actual root element may
         * be anything, allowing the unmarshaller to be used for subclasses (with different names) of the outer class.
         *
         * @param obj root element object (must be an instance of the GlobalCustom type)
         * @param ictx unmarshalling context
         * @return unmarshalled root object element
         * @throws JiBXException
         */
        public Object unmarshal(Object obj, IUnmarshallingContext ictx) throws JiBXException {
            
            // initialize namespace and special classes
            GlobalCustom global = (GlobalCustom)obj;
            global.initClasses();
            
            // unmarshal the wrapper element directly
            UnmarshallingContext ctx = (UnmarshallingContext)ictx;
            while (ctx.isStart()) {
                
                // check type of child present
                String element = ctx.getName();
                if (PackageCustom.ELEMENT_NAME.equals(element)) {
                    unmarshalPackage(global, null, ctx);
                } else if (ClassCustom.ELEMENT_NAME.equals(element)) {
                    unmarshalClass(global, null, ctx);
                } else {
                    global.internalAddExtensionChild(ctx.unmarshalElement());
                }
                
            }
            return global;
        }
    }
}
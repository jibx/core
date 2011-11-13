/*
 * Copyright (c) 2007-2008, Dennis M. Sosnoski. All rights reserved.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.jibx.custom.classes.GlobalCustom;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.generator.SchemaGenCommandLine;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.IClassLocator;
import org.jibx.util.ReflectionUtilities;

/**
 * Command line processing specifically for the {@link BindGen} class.
 * 
 * @author Dennis M. Sosnoski
 */
public class BindGenCommandLine extends SchemaGenCommandLine
{
    /** Ordered array of extra usage lines. */
    private static final String[] EXTRA_USAGE_LINES =
        new String[] { " -a       force abstract mappings for specified classes",
            " -b name  generated root binding name (default is 'binding.xml')",
            " -m       force concrete mappings for specified classes",
            " -o       binding generation only flag, skip schema generation" };
    
    /**
     * <code>TRUE</code> if abstract mappings forced, <code>FALSE</code> if concrete mappings forced,
     * <code>null</code> if left to class settings.
     */
    private Boolean m_abstract;
    
    /** Customizations model root. */
    private GlobalCustom m_global;
    
    /** Name used for root binding. */
    private String m_bindingName = "binding.xml";
    
    /** Binding generation only flag (skip schema generation). */
    private boolean m_bindingOnly;
    
    /**
     * Constructor.
     */
    public BindGenCommandLine() {
        super(EXTRA_USAGE_LINES);
    }
    
    /**
     * Get force abstract mapping setting.
     * 
     * @return <code>TRUE</code> if abstract mappings forced, <code>FALSE</code> if concrete mappings forced,
     * <code>null</code> if left to class settings
     */
    public Boolean getAbstract() {
        return m_abstract;
    }
    
    /**
     * Get customizations model root.
     * 
     * @return customizations
     */
    public GlobalCustom getGlobal() {
        return m_global;
    }
    
    /**
     * Get binding name.
     * 
     * @return name
     */
    public String getBindingName() {
        return m_bindingName;
    }
    
    /**
     * Check if only binding generation to be done.
     *
     * @return <code>true</code> if only binding generation, <code>false</code> if both binding and schema
     */
    public boolean isBindingOnly() {
        return m_bindingOnly;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#checkParameter(org.jibx.binding.generator.CustomizationCommandLineBase.ArgList)
     */
    protected boolean checkParameter(ArgList alist) {
        boolean match = true;
        String arg = alist.current();
        if ("-a".equalsIgnoreCase(arg)) {
            m_abstract = Boolean.TRUE;
        } else if ("-b".equalsIgnoreCase(arg)) {
            m_bindingName = alist.next();
        } else if ("-m".equalsIgnoreCase(arg)) {
            m_abstract = Boolean.FALSE;
        } else if ("-o".equalsIgnoreCase(arg)) {
            m_bindingOnly = true;
        } else {
            match = super.checkParameter(alist);
        }
        return match;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.ClassCustomizationBase#loadCustomizations(String,IClassLocator.ValidationContext)
     */
    protected void loadCustomizations(String path, IClassLocator loc, ValidationContext vctx) throws JiBXException, IOException {
        
        // load or create customization information
        setLocator(loc);
        m_global = new GlobalCustom(loc);
        if (path != null) {
            IBindingFactory fact = BindingDirectory.getFactory("class_customs_binding", GlobalCustom.class);
            IUnmarshallingContext ictx = fact.createUnmarshallingContext();
            FileInputStream is = new FileInputStream(path);
            ictx.setDocument(is, null);
            ictx.setUserContext(vctx);
            ((IUnmarshallable)m_global).unmarshal(ictx);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#applyOverrides(Map)
     */
    protected Map applyOverrides(Map overmap) {
        Map unknowns = ReflectionUtilities.applyKeyValueMap(overmap, m_global);
        m_global.initClasses();
        m_global.fillClasses();
        return unknowns;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#printUsage()
     */
    public void printUsage() {
        System.out.println("\nUsage: java org.jibx.binding.generator.BindGen "
            + "[options] class1 class2 ...\nwhere options are:");
        String[] usages = getUsageLines();
        for (int i = 0; i < usages.length; i++) {
            System.out.println(usages[i]);
        }
        System.out.println("The class# files are different classes to be included in the binding (references\n" +
            "from these classes will also be included).\n");
    }
}
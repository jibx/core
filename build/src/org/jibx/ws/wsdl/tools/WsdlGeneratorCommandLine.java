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

package org.jibx.ws.wsdl.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.jibx.ws.wsdl.tools.custom.WsdlCustom;

/**
 * Command line processing specifically for the {@link Jibx2Wsdl} class.
 * 
 * @author Dennis M. Sosnoski
 */
public class WsdlGeneratorCommandLine extends SchemaGenCommandLine
{
    /** Ordered array of extra usage lines. */
    private static final String[] EXTRA_USAGE_LINES =
        new String[] { " -b       generated root binding name (default is 'binding.xml')",
        " -d use pure doc/lit (not wrapped) style",
        " -u binding,...;schema,... existing bindings and schemas (separated by\n" +
        "          semicolon) to be used for messages (referenced schemas are included\n" +
        "          in WSDL)",
        " -x class,... names of extra classes to be included in binding" };
    
    /** Global customizations model root. */
    private GlobalCustom m_global;
    
    /** WSDL customizations model root. */
    private WsdlCustom m_wsdlCustom;
    
    /** List of extra classes for binding. */
    private List m_extraTypes = new ArrayList();
    
    /** List of existing bindings for messages. */
    private List m_useBindings = new ArrayList();
    
    /** List of existing schemas for messages. */
    private List m_useSchemas = new ArrayList();
    
    /** Name used for root binding. */
    private String m_bindingName = "binding.xml";
    
    /** Pure doc/lit (not wrapped) flag. */
    private boolean m_docLit;
    
    /**
     * Constructor.
     */
    public WsdlGeneratorCommandLine() {
        super(EXTRA_USAGE_LINES);
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
     * Get WSDL customizations model root.
     * 
     * @return WSDL customizations
     */
    public WsdlCustom getWsdlCustom() {
        return m_wsdlCustom;
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
     * Get extra classes to be included in binding.
     * 
     * @return list
     */
    public List getExtraTypes() {
        return m_extraTypes;
    }
    
    /**
     * Get existing bindings to be used for message components.
     * 
     * @return list
     */
    public List getUseBindings() {
        return m_useBindings;
    }
    
    /**
     * Get existing schemas to be used for message components.
     * 
     * @return list
     */
    public List getUseSchemas() {
        return m_useSchemas;
    }
    
    /**
     * Check if using pure doc/lit (not wrapped) style.
     * 
     * @return <code>true</code> if doc/lit, <code>false</code> if not
     */
    public boolean isDocLit() {
        return m_docLit;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#checkParameter(org.jibx.binding.generator.CustomizationCommandLineBase.ArgList)
     */
    protected boolean checkParameter(ArgList alist) {
        String arg = alist.current();
        boolean match = true;
        if ("-b".equalsIgnoreCase(arg)) {
            m_bindingName = alist.next();
        } else if ("-d".equalsIgnoreCase(arg)) {
            m_docLit = true;
        } else if ("-u".equalsIgnoreCase(arg)) {
            String text = alist.next();
            int split = text.indexOf(';');
            if (split >= 0) {
                splitItems(text.substring(split+1), m_useSchemas);
                text = text.substring(0, split);
            }
            splitItems(text, m_useBindings);
        } else if ("-x".equalsIgnoreCase(arg)) {
            String text = alist.next();
            if (text != null) {
                int split;
                int base = 0;
                while ((split = text.indexOf(',', base)) > 0) {
                    m_extraTypes.add(text.substring(base, split));
                    base = split + 1;
                }
                m_extraTypes.add(text.substring(base));
            }
        } else {
            match = super.checkParameter(alist);
        }
        return match;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#loadCustomizations(String,IClassLocator,org.jibx.schema.validation.ValidationContext)
     */
    protected void loadCustomizations(String path, IClassLocator loc, ValidationContext vctx)
        throws JiBXException, IOException {
        
        // load or create customization information
        setLocator(loc);
        m_global = new GlobalCustom(loc);
        if (path == null) {
            m_global.setAddConstructors(true);
            m_global.setForceClasses(true);
            m_global.setMapAbstract(Boolean.TRUE);
        } else {
            IBindingFactory fact = BindingDirectory.getFactory(WsdlCustom.class);
            IUnmarshallingContext ictx = fact.createUnmarshallingContext();
            FileInputStream is = new FileInputStream(path);
            ictx.setDocument(is, null);
            ictx.setUserContext(vctx);
            ((IUnmarshallable)m_global).unmarshal(ictx);
        }
        
        // find or build WSDL customization
        WsdlCustom custom = null;
        List extens = m_global.getExtensionChildren();
        for (Iterator iter = extens.iterator(); iter.hasNext();) {
            Object exten = iter.next();
            if (exten instanceof WsdlCustom) {
                custom = (WsdlCustom)exten;
                break;
            }
        }
        if (custom == null) {
            custom = new WsdlCustom(m_global);
            m_global.addExtensionChild(custom);
        }
        m_wsdlCustom = custom;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#applyOverrides(Map)
     */
    protected Map applyOverrides(Map overmap) {
        Map unknowns = ReflectionUtilities.applyKeyValueMap(overmap, m_global);
        unknowns = ReflectionUtilities.applyKeyValueMap(unknowns, m_wsdlCustom);
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
        System.out.println("\nUsage: java org.jibx.wsdl.Jibx2Wsdl " + "[options] class1 class2 ...\nwhere options are:");
        String[] usages = getUsageLines();
        for (int i = 0; i < usages.length; i++) {
            System.out.println(usages[i]);
        }
        System.out.println("The class# files are different classes to be exposed as "
            + "services (references\nfrom these classes will automatically be "
            + "included in the generated bindings).\n");
    }
}
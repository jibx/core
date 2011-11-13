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

package org.jibx.schema.generator;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jibx.custom.classes.ClassCustomizationBase;
import org.jibx.custom.classes.CustomBase;
import org.jibx.custom.classes.GlobalCustom;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.IClassLocator;
import org.jibx.util.ReflectionUtilities;

/**
 * Command line processing specifically for the {@link SchemaGen} class.
 * TODO: Split this into abstract base class which the existing subclasdses can extend directly, plus separate subclass
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaGenCommandLine extends ClassCustomizationBase
{
    /** Ordered array of extra usage lines. */
    private static final String[] EXTRA_USAGE_LINES =
        new String[] { " -n uri=name,... schema namespace URI and file name pairs (default generates\n" +
            "          file names from URIs)" };
    
    /** Customizations model root. */
    private GlobalCustom m_global;
    
    /** Namespace URI to file name map. */
    private Map m_uriNames = new HashMap();
    
    /** Class locator used to complete customizations. */
    private IClassLocator m_locator;
    
    /**
     * Constructor for when class is used directly.
     */
    public SchemaGenCommandLine() {
        super(EXTRA_USAGE_LINES);
    }
    
    /**
     * Constructor used by subclasses.
     * 
     * @param lines
     */
    protected SchemaGenCommandLine(String[] lines) {
        super(mergeUsageLines(lines, EXTRA_USAGE_LINES));
    }
    
    /**
     * Get class locator.
     * 
     * @return locator
     */
    public IClassLocator getLocator() {
        return m_locator;
    }
    
    /**
     * Set class locator.
     *
     * @param locator
     */
    protected void setLocator(IClassLocator locator) {
        m_locator = locator;
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
     * Get schema namespace URI to name map.
     * 
     * @return map
     */
    public Map getUriNames() {
        return m_uriNames;
    }
    
    /**
     * Add uri=name pair to map.
     *
     * @param text
     * @return <code>true</code> if valid, <code>false</code> if not
     */
    private boolean addUriNamePair(String text) {
        int split = text.indexOf('=');
        if (split >= 0) {
            String key = text.substring(split);
            if (m_uriNames.containsKey(key)) {
                System.err.println("Duplicate namespace URI for uri=name parameter: " + key);
            } else {
                m_uriNames.put(key, text.substring(split+1));
                return true;
            }
        }
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jibx.binding.generator.CustomizationCommandLineBase#checkParameter(org.jibx.binding.generator.CustomizationCommandLineBase.ArgList)
     */
    protected boolean checkParameter(ArgList alist) {
        boolean match = true;
        String arg = alist.current();
        if ("-n".equalsIgnoreCase(arg)) {
            String text = alist.next();
            int base = 0;
            int split;
            boolean valid = true;
            while ((split = text.indexOf(',', base)) >= 0) {
                valid = addUriNamePair(text.substring(base, split)) && valid;
                base = split + 1;
            }
            valid = addUriNamePair(text.substring(base)) && valid;
            if (!valid) {
                alist.setValid(false);
            }
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
        m_global = new GlobalCustom(loc);
        m_global.setNamespaceStyle(new Integer(CustomBase.DERIVE_FIXED));
        if (path != null) {
            IBindingFactory fact = BindingDirectory.getFactory("class-customs-binding", GlobalCustom.class);
            IUnmarshallingContext ictx = fact.createUnmarshallingContext();
            FileInputStream is = new FileInputStream(path);
            ictx.setDocument(is, null);
            ictx.setUserContext(vctx);
            ((IUnmarshallable)m_global).unmarshal(ictx);
        }
        setLocator(loc);
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
        System.out.println("\nUsage: java org.jibx.schema.generator.SchemaGen "
            + "[options] binding1 binding2 ...\nwhere options are:");
        String[] usages = getUsageLines();
        for (int i = 0; i < usages.length; i++) {
            System.out.println(usages[i]);
        }
        System.out.println("The binding# files are different bindings to be included in the schema(s).\n" +
        	"Bindings referenced (using <include>) by the specified bindings will also be\nincluded.");
    }
}
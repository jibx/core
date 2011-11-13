/*
 * Copyright (c) 2003-2008, Dennis M. Sosnoski. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution. Neither the name of
 * Sosnoski Software Solutions, Inc. nor the names of its personnel may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sosnoski.site;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jibx.runtime.*;

/**
 * Web site builder. This processes an input site configuration file to generate the set of output pages. This extends
 * the base site configuration component class in order to allow consistent handling of value definitions, which may be
 * set by the command line arguments.
 * 
 * @author Dennis M. Sosnoski
 */
public class Builder extends Component
{
    private final File m_configFile; // XML site configuration file path
    
    private final File m_sourceRoot; // source root directory
    
    private final File m_imageRoot; // generated site image root directory
    
    private final Template m_template; // template for site page generation
    
    /**
     * Constructor. Initializes the Velocity configuration and sets up basic path information.
     * 
     * @param config site configuration file path
     * @param template velocity template file path
     * @param odir base output directory path
     * @throws Exception on error initializing template
     */
    private Builder(String config, String template, String odir) throws Exception {
        m_configFile = new File(config).getAbsoluteFile();
        m_sourceRoot = m_configFile.getParentFile();
        m_imageRoot = new File(odir).getAbsoluteFile();
        Velocity.init();
        m_template = Velocity.getTemplate(template);
        m_template.process();
    }
    
    /**
     * Get the source root directory. This is the directory containing the root configuration file.
     * 
     * @return source root directory
     */
    public File getSourceRoot() {
        return m_sourceRoot;
    }
    
    /**
     * Add value definitions to context. Prior values are saved for all added values to allow clean reversal at the
     * close of scope of a definition.
     * 
     * @param values named values to be added to context (may be <code>null</code>)
     * @param vctx context to received added values
     * @return prior values for names added to context (<code>null</code> if values parameter <code>null</code>)
     */
    private String[] addValues(ArrayList values, VelocityContext vctx) {
        if (values == null) {
            return null;
        } else {
            String[] priors = new String[values.size()];
            for (int i = 0; i < priors.length; i++) {
                Value value = (Value)values.get(i);
                priors[i] = (String)vctx.get(value.getName());
                vctx.put(value.getName(), value.getValue());
            }
            return priors;
        }
    }
    
    /**
     * Restore prior value definitions to context.
     * 
     * @param values named values to be added to context
     * @param priors values for names added to context
     * @param vctx context to received added values
     */
    private void restoreValues(ArrayList values, String[] priors, VelocityContext vctx) {
        for (int i = 0; i < priors.length; i++) {
            Value value = (Value)values.get(i);
            String prior = priors[i];
            if (prior == null) {
                vctx.remove(value.getName());
            } else {
                vctx.put(value.getName(), prior);
            }
        }
    }
    
    /**
     * Recurse through child components. This generates the pages for all components in the supplied list, calling
     * itself recursively for any which themselves have child components.
     * 
     * @param comps components to be processed
     * @param vctx context defined for containing component
     * @param actives list of active pages to this point
     * @throws Exception on error processing template or file
     */
    private void recurseChildren(ArrayList comps, VelocityContext vctx, List actives) throws Exception {
        for (int i = 0; i < comps.size(); i++) {
            
            // add values defined by component to context
            MenuComponent comp = (MenuComponent)comps.get(i);
            String[] priors = addValues(comp.getValues(), vctx);
            
            // check for an item with an associated page
            if (comp instanceof Item && ((Item)comp).hasPage()) {
                
                // add item information to context
                Item item = (Item)comp;
                vctx.put("page", item);
                actives.add(item);
                
                // set path back to site root
                String target = item.getTarget();
                StringBuffer buff = new StringBuffer();
                int mark = 0;
                while ((mark = target.indexOf(File.separatorChar, mark + 1)) >= 0) {
                    buff.append("../");
                }
                if (buff.length() == 0) {
                    buff.append(".");
                } else {
                    buff.setLength(buff.length() - 1);
                }
                vctx.put("root", buff.toString());
                
                // set up for output to target location
                File file = new File(m_imageRoot, target);
                System.out.println("Target file path is " + file.getPath() + " with root path " + buff);
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                // generate site page from template
                FileWriter writer = new FileWriter(file);
                m_template.initDocument();
                m_template.merge(vctx, writer);
                writer.close();
                actives.remove(item);
            }
            
            // process all child components recursively
            ArrayList childs = comp.getChildren();
            if (childs != null) {
                recurseChildren(comp.getChildren(), vctx, actives);
            }
            
            // restore original values to context
            if (priors != null) {
                restoreValues(comp.getValues(), priors, vctx);
            }
        }
    }
    
    /**
     * Site building method. This processes the site structure definition, generating the full set of pages for the
     * site.
     */
    private void buildSite() throws Exception {
        
        // unmarshal site description with this instance as root object
        IBindingFactory bfact = BindingDirectory.getFactory(Site.class);
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        uctx.setDocument(new FileInputStream(m_configFile), m_configFile.getName(), null);
        uctx.pushObject(this);
        Object obj = uctx.unmarshalElement();
        VelocityContext vctx = new VelocityContext();
        List actives = new ArrayList();
        vctx.put("actives", actives);
        if (obj instanceof Menu) {
            
            // handle basic linkages with source relative to configuration file
            Menu menu = (Menu)obj;
            menu.setLinkages("");
            menu.loadContent(m_configFile.getAbsoluteFile().getParentFile());
            
            // generate single-menu site image
            vctx.put("menu", menu);
            addValues(menu.getValues(), vctx);
            recurseChildren(menu.getChildren(), vctx, actives);
            
        } else {
            
            // generate multiple-menu site image
            Site site = (Site)obj;
            ArrayList menus = site.getChildren();
            site.initialize(m_sourceRoot);
            vctx.put("site", site);
            addValues(site.getValues(), vctx);
            for (int i = 0; i < menus.size(); i++) {
                Menu menu = (Menu)menus.get(i);
                vctx.put("menu", menu);
                String[] priors = addValues(menu.getValues(), vctx);
                recurseChildren(menu.getChildren(), vctx, actives);
                if (priors != null) {
                    restoreValues(menu.getValues(), priors, vctx);
                }
            }
            
        }
    }
    
    /**
     * Main method for site creation.
     * 
     * @param args
     */
    public static void main(String[] args) {
        if (args.length >= 3) {
            try {
                
                // create the class instance
                Builder build = new Builder(args[0], args[1], args[2]);
                
                // add all named value definitions from command line
                if (args.length > 3) {
                    for (int i = 3; i < args.length; i++) {
                        String arg = args[i];
                        int split = arg.indexOf('=');
                        String key;
                        String value;
                        if (split >= 0) {
                            key = arg.substring(0, split);
                            value = arg.substring(split + 1);
                        } else {
                            key = arg;
                            value = "";
                        }
                        build.addValue(new Value(key, value));
                    }
                }
                
                // handle the actual site generation
                build.buildSite();
                
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
            }
            
        } else {
            System.out.println("Usage: java com.sosnoski.site.Builder site-xml template root-dir [key[=value]]*\n" +
                "where:\n" +
                "  site-xml XML site description file\n" +
                "  template Velocity template file\n" +
                "  root-dir root directory for output site image" +
                "  key      name to be defined in context\n" +
                "  value    value to be defined for preceding name\n");
        }
    }
}
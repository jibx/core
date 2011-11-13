/*
Copyright (c) 2004, Dennis M. Sosnoski
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.sosnoski.site;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * <p>Custom unmarshaller for <b>&lt;include&gt;</b> elements in the binding.
 * This checks if an include is optional, and if so also checks to see if the
 * include should be used. If processed, the specified file is unmarshalled
 * using a separate unmarshalling context and the result is returned as a
 * {@link com.sosnoski.site.Menu}. Otherwise, <code>null</code> is returned, so
 * it's important to check the result rather than blindly assuming that it's an
 * object (generally by using an add-method to filter).</p>
 * 
 * <p>This isn't really ideal code, in that the element names used at the top
 * level of the binding definition are hardcoded into this class. Right now JiBX
 * doesn't provide any convenient way to avoid doing this, though.</p>
 * 
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class IncludeHandler implements IUnmarshaller
{
    private static final String INCLUDE_NAME = "include";
    private static final String ELEMENT_NAMESPACE = null;
    private static final String IFDEF_NAME = "if-defined";
    private static final String IFNDEF_NAME = "if-undefined";
    private static final String PATH_NAME = "path";
    private static final String ACCESSDIR_NAME = "access-dir";
    private static final String LABEL_NAME = "label";
    private static final String ATTR_NAMESPACE = null;
    
    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#isPresent(org.jibx.runtime.IUnmarshallingContext)
     */
     
    public boolean isPresent(IUnmarshallingContext ctx) throws JiBXException {
        return ctx.isAt(ELEMENT_NAMESPACE, INCLUDE_NAME);
    }
    
    /**
     * Check for value defined in context. Climbs up the stack of objects being
     * unmarshalled, checking for any named value definitions matching the
     * supplied name.
     * 
     * @param name value name to be found
     * @param ctx unmarshalling context
     * @return innermost matching value found, or <code>null</code> if none
     */
    
    private String getValue(String name, UnmarshallingContext ctx) {
        for (int i = 0; i < ctx.getStackDepth(); i++) {
            Object obj = ctx.getStackObject(i);
            if (obj instanceof Component) {
                ArrayList values = ((Component)obj).getValues();
                if (values != null) {
                    for (int j = values.size()-1; j >= 0 ; j--) {
                        Value value = (Value)values.get(j);
                        if (name.equals(value.getName())) {
                            return value.getValue();
                        }
                    }
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.jibx.runtime.IUnmarshaller#unmarshal(java.lang.Object,
     *  org.jibx.runtime.IUnmarshallingContext)
     */
     
    public Object unmarshal(Object obj, IUnmarshallingContext ictx)
        throws JiBXException {
        
        // make sure we're at the appropriate start tag
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        if (isPresent(ctx)) {
            
            // make sure there's a name for the document being parsed
            if (ctx.getDocumentName() == null) {
                throw new JiBXException("Unable to process include without " +                    "base document name " + ctx.buildPositionString());
            } else {
                
                // check for conditionals on include
                boolean use = true;
                String name = ctx.attributeText(ATTR_NAMESPACE,
                    IFDEF_NAME, null);
                if (name != null) {
                    use = getValue(name, ctx) != null;
                }
                name = ctx.attributeText(ATTR_NAMESPACE, IFNDEF_NAME, null);
                if (use && name != null) {
                    use = getValue(name, ctx) == null;
                }
                
                // get the remaining attribute values and ditch element
                String path = ctx.attributeText(ATTR_NAMESPACE, PATH_NAME);
                String access =
                    ctx.attributeText(ATTR_NAMESPACE, ACCESSDIR_NAME);
                String label = ctx.attributeText(ATTR_NAMESPACE,
                    LABEL_NAME, null);
                ctx.parsePastElement(ELEMENT_NAMESPACE, INCLUDE_NAME);
                
                // check if this include is active
                if (use) {
                    
                    // find the file to be included
                    int index = ctx.getStackDepth() - 1;
                    Builder build = (Builder)ctx.getStackObject(index);
                    File config = Component.
                        applyFilePath(build.getSourceRoot(), path);
                    File root = config.getParentFile();
                    
                    // create new unmarshalling context for included file
                    UnmarshallingContext inctx = (UnmarshallingContext)
                        BindingDirectory.getFactory(Site.class).
                        createUnmarshallingContext();
                    try {
                        inctx.setDocument(new FileInputStream
                            (new File(root, path)), path, null);
                    } catch (FileNotFoundException e) {
                        throw new JiBXException("Include file " + path +
                            " not found " + ctx.buildPositionString());
                    }
                    
                    // unmarshal the included document
                    obj = inctx.unmarshalElement();
                    if (obj instanceof Site) {
                        return new Menu((Site)obj, root, access, label);
                    } else if (obj instanceof Menu) {
                        return new Menu((Menu)obj, root, access, label);
                    } else {
                        throw new JiBXException("Root element of file " + path +
                            " is not a valid type " +
                            ctx.buildPositionString());
                    }
                    
                } else {
                    return null;
                }
            }
        }
        return null;
    }
}
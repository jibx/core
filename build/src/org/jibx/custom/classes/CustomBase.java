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

package org.jibx.custom.classes;

import java.util.Collection;

import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.NameUtilities;
import org.jibx.util.StringArray;

/**
 * Base class for all customizations. This defines a way to navigate up the tree of nested components without making
 * assumptions about the specific type of the containing components. This allows for other types of customizations,
 * beyond the binding customizations included directly in this package. This also includes enumeration definitions which
 * are used with both base and extension customizations.
 * 
 * @author Dennis M. Sosnoski
 */
public class CustomBase
{
    // name style value set information
    public static final int CAMEL_CASE_NAMES = 0;
    
    public static final int UPPER_CAMEL_CASE_NAMES = 1;
    
    public static final int HYPHENATED_NAMES = 2;
    
    public static final int DOTTED_NAMES = 3;
    
    public static final int UNDERSCORED_NAMES = 4;
    
    public static final EnumSet s_nameStyleEnum =
        new EnumSet(CAMEL_CASE_NAMES, new String[] { "camel-case", "upper-camel-case", "hyphenated", "dotted",
            "underscored" });
    
    // require value set information
    public static final int REQUIRE_NONE = 0;
    
    public static final int REQUIRE_PRIMITIVES = 1;
    
    public static final int REQUIRE_OBJECTS = 2;
    
    public static final int REQUIRE_ALL = 3;
    
    public static final EnumSet s_requireEnum =
        new EnumSet(REQUIRE_NONE, new String[] { "none", "primitives", "objects", "all" });
    
    // derive-namespace value set information
    public static final int DERIVE_NONE = 0;
    
    public static final int DERIVE_BY_PACKAGE = 1;
    
    public static final int DERIVE_FIXED = 2;
    
    public static final EnumSet s_namespaceStyleEnum =
        new EnumSet(DERIVE_NONE, new String[] { "none", "package", "fixed" });
    
    // parent element (null if none) - would be final, except for unmarshalling
    private SharedNestingBase m_parent;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public CustomBase(SharedNestingBase parent) {
        m_parent = parent;
    }
    
    /**
     * Get container.
     * 
     * @return container
     */
    public SharedNestingBase getParent() {
        return m_parent;
    }
    
    /**
     * Get global customizations root.
     * 
     * @return global customization
     */
    public GlobalCustom getGlobal() {
        CustomBase parent = m_parent;
        while (!(parent instanceof GlobalCustom)) {
            parent = parent.getParent();
        }
        return (GlobalCustom)parent;
    }
    
    /**
     * Convert class, method, or parameter name to XML name.
     * 
     * @param base class or simple field name to be converted
     * @param code conversion format style code
     * @return XML name
     */
    public static String convertName(String base, int code) {
        
        // strip off trailing array indication
        while (base.endsWith("[]")) {
            base = base.substring(0, base.length() - 2);
        }
        
        // skip any leading special characters in name
        int length = base.length();
        int offset = -1;
        char chr = 0;
        while (++offset < length && ((chr = base.charAt(offset)) == '_' || (chr == '$')));
        if (offset >= length) {
            return "_";
        } else {
            
            // make sure valid first character of name
            StringBuffer buff = new StringBuffer();
            if (Character.isDigit(chr)) {
                buff.append('_');
            }
            
            // scan for word splits in supplied name
            boolean split = true;
            boolean caps = false;
            while (offset < length) {
                
                // split if underscore or change to upper case, or upper ending
                chr = base.charAt(offset++);
                boolean nextlower = offset < length && !Character.isUpperCase(base.charAt(offset));
                if (chr == '_') {
                    split = true;
                    continue;
                } else if (Character.isUpperCase(chr)) {
                    if (!caps || nextlower) {
                        split = true;
                    }
                    if (code != CAMEL_CASE_NAMES && code != UPPER_CAMEL_CASE_NAMES) {
                        chr = Character.toLowerCase(chr);
                    }
                }
                if (split) {
                    
                    // convert word break
                    caps = !nextlower;
                    boolean tolower = false;
                    char separator = 0;
                    switch (code) {
                        
                        case CAMEL_CASE_NAMES: {
                            if (buff.length() != 0) {
                                chr = Character.toUpperCase(chr);
                                tolower = false;
                            } else {
                                tolower = nextlower || offset == length;
                            }
                            break;
                        }
                            
                        case UPPER_CAMEL_CASE_NAMES: {
                            tolower = false;
                            chr = Character.toUpperCase(chr);
                            break;
                        }
                            
                        case HYPHENATED_NAMES: {
                            separator = '-';
                            break;
                        }
                            
                        case DOTTED_NAMES: {
                            separator = '.';
                            break;
                        }
                            
                        case UNDERSCORED_NAMES: {
                            separator = '_';
                            break;
                        }
                            
                    }
                    if (separator > 0 && buff.length() > 0) {
                        buff.append(separator);
                    }
                    if (tolower) {
                        chr = Character.toLowerCase(chr);
                    }
                    split = false;
                    
                }
                if (chr != '$') {
                    
                    // no split, just append the character
                    buff.append(chr);
                    
                }
            }
            return buff.toString();
        }
    }
    
    /**
     * Derive name for item in a collection. If the supplied collection name ends in a recognized plural form the
     * derived item name is the singular version of the collection name. Otherwise, it is the converted name of the
     * collection item class, or just "item" if the class is unknown. TODO: internationalization?
     * 
     * @param cname collection name (<code>null</code> if none)
     * @param type item type (<code>null</code> if unknown)
     * @param code conversion format style code
     * @return item name
     */
    public static String deriveItemName(String cname, String type, int code) {
        if (cname != null) {
            String name = NameUtilities.depluralize(cname);
            if (!name.equals(cname)) {
                return name;
            }
        }
        if (type != null && !"java.lang.Object".equals(type)) {
            return convertName(type.substring(type.lastIndexOf('.') + 1), code);
        } else {
            return "item";
        }
    }
    
    /**
     * Get the package from a fully-qualified type name.
     * 
     * @param type fully-qualified type name
     * @return package of the type (empty string if in default package)
     */
    public static String packageOfType(String type) {
        int split = type.lastIndexOf('.');
        if (split >= 0) {
            return type.substring(0, split);
        } else {
            return "";
        }
    }
    
    /**
     * Create a namespace URL from a package path.
     * 
     * @param pkgpth fully-qualified package name
     * @return namespace based on package (<code>null</code> if none)
     */
    public static String packageToNamespace(String pkgpth) {
        int mark = pkgpth.indexOf('.');
        if (mark >= 0) {
            StringBuffer buff = new StringBuffer();
            buff.append("http://");
            String comp = pkgpth.substring(0, mark);
            int base = mark + 1;
            char delim = '.';
            if ("com".equals(comp) || "net".equals(comp) || "org".equals(comp)) {
                mark = pkgpth.indexOf('.', base);
                if (mark > 0) {
                    buff.append(pkgpth.substring(base, mark));
                } else {
                    buff.append(pkgpth.substring(base));
                }
                buff.append('.');
                base = mark + 1;
                delim = '/';
            }
            buff.append(comp);
            while (mark > 0) {
                buff.append(delim);
                base = mark + 1;
                mark = pkgpth.indexOf('.', base);
                if (mark > 0) {
                    buff.append(pkgpth.substring(base, mark));
                } else {
                    buff.append(pkgpth.substring(base));
                }
            }
            return buff.toString();
        } else {
            return null;
        }
    }
    
    /**
     * Derive namespace using specified technique.
     * 
     * @param uri base namespace URI (<code>null</code> if none)
     * @param pkgpth fully qualified package name
     * @param style namespace style code
     * @return derived namespace
     */
    public static String deriveNamespace(String uri, String pkgpth, int style) {
        switch (style) {
            case DERIVE_NONE:
                return null;
                
            case DERIVE_FIXED:
                return uri;
                
            case DERIVE_BY_PACKAGE: {
                if (uri == null) {
                    return packageToNamespace(pkgpth);
                } else if (pkgpth == null) {
                    return uri;
                } else {
                    
                    // append the last package to passed URI
                    String pack;
                    int start = pkgpth.lastIndexOf('.');
                    if (start >= 0) {
                        pack = pkgpth.substring(start + 1);
                    } else {
                        pack = pkgpth;
                    }
                    if (uri.endsWith("/")) {
                        return uri + pack;
                    } else {
                        return uri + '/' + pack;
                    }
                }
            }
                
            default:
                throw new IllegalStateException("Invalid style code");
        }
    }
    
    /**
     * Validate attributes of element. This is designed to be called during unmarshalling as part of the pre-set method
     * processing when a subclass instance is being created.
     * 
     * @param ictx unmarshalling context
     * @param attrs attributes array
     */
    protected void validateAttributes(IUnmarshallingContext ictx, StringArray attrs) {
        
        // setup for attribute access
        ValidationContext vctx = (ValidationContext)ictx.getUserContext();
        UnmarshallingContext uctx = (UnmarshallingContext)ictx;
        
        // loop through all attributes of current element
        for (int i = 0; i < uctx.getAttributeCount(); i++) {
            
            // check if nonamespace attribute is in the allowed set
            String name = uctx.getAttributeName(i);
            if (uctx.getAttributeNamespace(i).length() == 0) {
                if (attrs.indexOf(name) < 0) {
                    vctx.addWarning("Undefined attribute " + name, this);
                }
            }
        }
    }
    
    /**
     * Gets the parent element link from the unmarshalling stack. This method is for use by factories during
     * unmarshalling.
     * 
     * @param ictx unmarshalling context
     * @return containing class
     */
    protected static Object getContainingObject(IUnmarshallingContext ictx) {
        Object parent = ictx.getStackTop();
        if (parent instanceof Collection) {
            parent = ictx.getStackObject(1);
        }
        return parent;
    }
}
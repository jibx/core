/*
 * Copyright (c) 2007-2008, Dennis M. Sosnoski All rights reserved.
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

package org.jibx.schema;

import java.io.File;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jibx.runtime.ITrackSource;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AttributeElement;
import org.jibx.schema.elements.ElementElement;
import org.jibx.schema.elements.FacetElement;
import org.jibx.schema.elements.FilteredSegmentList;
import org.jibx.schema.elements.OpenAttrBase;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.types.Count;

/**
 * Utility methods for working with schema structures.
 * 
 * @author Dennis M. Sosnoski
 */
public class SchemaUtils
{
    /** Binding name for output with schema namespace prefix 'xs'. */
    public static final String XS_PREFIX_BINDING = "schema_xsprefix_binding";
    
    /** Binding name for output with schema namespace as default (no prefix). */
    public static final String NO_PREFIX_BINDING = "schema_noprefix_binding";
    
    /** Logger for class. */
    public static final Logger s_logger = Logger.getLogger(SchemaUtils.class.getName());
    
    /** String used as basis for indentation. */
    private static final String s_indentText = " . . . . . . . . . . . . . . . . . . . . ";
    
    /** Pregenerated indentation strings. */
    private static final String[] s_indents;
    static {
        s_indents = new String[s_indentText.length()];
        for (int i = 0; i < s_indents.length; i++) {
            s_indents[i] = s_indentText.substring(0, i);
        }
    };
    
    /**
     * Check if a particle is a repeated value.
     * 
     * @param part particle to be checked
     * @return <code>true</code> if repeated, <code>false</code> if not
     */
    public static boolean isRepeated(IArity part) {
        Count max = part.getMaxOccurs();
        return (max != null && (max.isGreaterThan(1)));
    }
    
    /**
     * Check if a particle is prohibited (no instances allowed).
     * 
     * @param part particle to be checked
     * @return <code>true</code> if prohibited, <code>false</code> if not
     */
    public static boolean isProhibited(IArity part) {
        return Count.isCountEqual(0, part.getMaxOccurs());
    }
    
    /**
     * Check if a particle is optional (zero instances allowed).
     * 
     * @param part particle to be checked
     * @return <code>true</code> if optional, <code>false</code> if not
     */
    public static boolean isOptional(IArity part) {
        return Count.isCountEqual(0, part.getMinOccurs());
    }
    
    /**
     * Check if an element is optional (zero instances allowed).
     * 
     * @param elem element to be checked
     * @return <code>true</code> if optional, <code>false</code> if not
     */
    public static boolean isOptionalElement(ElementElement elem) {
        return Count.isCountEqual(0, elem.getMinOccurs()) || (elem.isNillable() && !isRepeated(elem));
    }
    
    /**
     * Check if an attribute is optional (zero instances allowed).
     * 
     * @param attr attribute to be checked
     * @return <code>true</code> if optional, <code>false</code> if not
     */
    public static boolean isOptionalAttribute(AttributeElement attr) {
        return attr.getUse() == AttributeElement.OPTIONAL_USE;
    }
    
    /**
     * Check if a particle is a singleton (one, and only one, instance allowed).
     * 
     * @param part particle to be checked
     * @return <code>true</code> if singleton, <code>false</code> if not
     */
    public static boolean isSingleton(IArity part) {
        return Count.isCountEqual(1, part.getMinOccurs()) && Count.isCountEqual(1, part.getMaxOccurs());
    }
    
    /**
     * Check if an element is a singleton (one, and only one, instance allowed).
     * 
     * @param elem element to be checked
     * @return <code>true</code> if singleton, <code>false</code> if not
     */
    public static boolean isSingletonElement(ElementElement elem) {
        return !elem.isNillable() && isSingleton((IArity)elem);
    }
    
    /**
     * Check if a definition component is nillable (an element with nillable='true').
     *
     * @param comp
     * @return <code>true</code> if nillable, <code>false</code> if not
     */
    public static boolean isNillable(OpenAttrBase comp) {
        if (comp instanceof ElementElement) {
            return ((ElementElement)comp).isNillable();
        } else {
            return false;
        }
    }
    
    /**
     * Check if a definition component has a name.
     *
     * @param comp
     * @return <code>true</code> if named, <code>false</code> if not
     */
    public static boolean isNamed(OpenAttrBase comp) {
        if (comp instanceof INamed) {
            return ((INamed)comp).getName() != null;
        } else {
            return false;
        }
    }
    
    //
    // Logging support
    
    /**
     * Get indentation string. This returns a string of the requested number of indents to the maximum value supported,
     * and otherwise just returns the maximum indentation.
     *
     * @param depth
     * @return indentation string
     */
    public static String getIndentation(int depth) {
        if (depth < s_indents.length) {
            return s_indents[depth];
        } else {
            return s_indentText;
        }
    }
    
    /**
     * Get string description of component for use in logging.
     *
     * @param comp schema component
     * @return description
     */
    public static String describeComponent(SchemaBase comp) {
        StringBuffer buff = new StringBuffer();
        buff.append(comp.name());
        String name;
        if (comp instanceof INamed && (name = ((INamed)comp).getName()) != null) {
            buff.append(' ');
            buff.append(name);
        }
        if (comp instanceof ITrackSource) {
            ITrackSource track = (ITrackSource)comp;
            String path = track.jibx_getDocumentName();
            if (path != null) {
                buff.append(" (");
                int start = path.lastIndexOf(File.separatorChar) + 1;
                int end = path.length();
                if (path.endsWith(".xsd")) {
                    end -= 4;
                }
                buff.append(path.substring(start, end));
                int line = track.jibx_getLineNumber();
                if (line > 0) {
                    buff.append(':');
                    buff.append(line);
                }
                buff.append(")");
            }
        }
        return buff.toString();
    }
    
    /**
     * Get path to component.
     *
     * @param comp schema component
     * @return description
     */
    public static String componentPath(OpenAttrBase comp) {
        StringBuffer buff = new StringBuffer();
        StringBuffer segment = new StringBuffer();
        OpenAttrBase node = comp;
        while (node != null && node.type() != SchemaBase.SCHEMA_TYPE) {
            segment.append(node.name());
            String name;
            OpenAttrBase parent = node.getParent();
            if (node instanceof INamed && (name = ((INamed)node).getName()) != null) {
                segment.append("[@name=");
                segment.append(name);
                segment.append(']');
            } else {
                int index = 1;
                if (parent != null) {
                    for (Iterator iter = parent.getChildIterator(); iter.hasNext();) {
                        OpenAttrBase child = (OpenAttrBase)iter.next();
                        if (child == node) {
                            break;
                        } else if (child.type() == node.type()) {
                            index++;
                        }
                    }
                }
                if (index > 1) {
                    segment.append('[');
                    segment.append(index);
                    segment.append(']');
                }
            }
            if (buff.length() > 0) {
                segment.append('/');
            }
            buff.insert(0, segment.toString());
            segment.setLength(0);
            node = parent;
        }
        if (comp instanceof ITrackSource) {
            ITrackSource track = (ITrackSource)comp;
            String path = track.jibx_getDocumentName();
            if (path != null) {
                buff.append(" (");
                int start = path.lastIndexOf(File.separatorChar) + 1;
                int end = path.length();
                if (path.endsWith(".xsd")) {
                    end -= 4;
                }
                buff.append(path.substring(start, end));
                int line = track.jibx_getLineNumber();
                if (line > 0) {
                    buff.append(':');
                    buff.append(line);
                }
                buff.append(")");
            }
        }
        return buff.toString();
    }

    /**
     * Check if a particular schema definition component is an enumeration type definition. Formally, this returns
     * <code>true</code> if and only if the component is a &lt;simpleType> element which is a restriction using one or
     * more &lt;enumeration> facets.
     *
     * @param comp
     * @return <code>true</code> if an enumeration definition, <code>false</code> if not
     */
    public static boolean isEnumeration(AnnotatedBase comp) {
        if (comp.type() == SchemaBase.SIMPLETYPE_TYPE) {
            SimpleTypeElement type = (SimpleTypeElement)comp;
            if (type.getDerivation().type() == SchemaBase.RESTRICTION_TYPE) {
                SimpleRestrictionElement restrict = (SimpleRestrictionElement)type.getDerivation();
                FilteredSegmentList facets = restrict.getFacetsList();
                for (int i = 0; i < facets.size(); i++) {
                    FacetElement facet = (FacetElement)facets.get(i);
                    if (facet.type() == SchemaBase.ENUMERATION_TYPE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
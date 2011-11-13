/*
 * Copyright (c) 2009, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.Utility;

/**
 * Support methods for using reflection access to values.
 */
public class ReflectionUtilities
{
    /**
     * Apply a key/value map to an object instance. This uses reflection to match the keys to either set methods (with
     * names of the form setZZZText taking a single String parameter, or setZZZ taking a single String or primitive
     * wrapper parameter) or fields (named m_ZZZ). The ZZZ in the names is based on the key name, with hyphenation
     * converted to camel case (leading upper camel case, for the method names).
     * 
     * @param map
     * @param obj
     * @return map for key/values not found in the supplied object
     */
    public static Map applyKeyValueMap(Map map, Object obj) {
        Map missmap = new HashMap();
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            String key = (String)iter.next();
            Object value = map.get(key);
            boolean fail = true;
            Throwable t = null;
            try {
                
                // convert key to field and method name, and to alternate method name (used for value sets)
                StringBuffer buff = new StringBuffer(key);
                for (int i = 0; i < buff.length(); i++) {
                    char chr = buff.charAt(i);
                    if (chr == '-') {
                        buff.deleteCharAt(i);
                        buff.setCharAt(i, Character.toUpperCase(buff.charAt(i)));
                    }
                }
                String fname = "m_" + buff.toString();
                buff.setCharAt(0, Character.toUpperCase(buff.charAt(0)));
                String mname = "set" + buff.toString();
                String altname = mname + "Text";
                
                // first try to find a match for alternate method name, with text value and possibly unmarshaller
                Method method = null;
                Field field = null;
                String tname = null;
                Class clas = obj.getClass();
                outer: while (!clas.getName().equals("java.lang.Object")) {
                    Method[] methods = clas.getDeclaredMethods();
                    for (int i = 0; i < methods.length; i++) {
                        Method match = methods[i];
                        if (altname.equals(match.getName())) {
                            Class[] types = match.getParameterTypes();
                            if (types.length == 1 || (types.length == 2 && types[1] == IUnmarshallingContext.class)) {
                                if (types[0] == String.class) {
                                    tname = String.class.getName();
                                    try {
                                        match.setAccessible(true);
                                    } catch (SecurityException e) { /* deliberately empty */
                                    }
                                    method = match;
                                    break outer;
                                }
                            }
                        }
                    }
                    clas = clas.getSuperclass();
                }
                if (method == null) {
                    
                    // no match on alternate method name, next check for regular method
                    clas = obj.getClass();
                    outer: while (!clas.getName().equals("java.lang.Object")) {
                        Method[] methods = clas.getDeclaredMethods();
                        for (int i = 0; i < methods.length; i++) {
                            Method match = methods[i];
                            if (mname.equals(match.getName())) {
                                Class[] types = match.getParameterTypes();
                                if (types.length == 1) {
                                    tname = types[0].getName();
                                    try {
                                        match.setAccessible(true);
                                    } catch (SecurityException e) { /* deliberately empty */
                                    }
                                    method = match;
                                    break outer;
                                }
                            }
                        }
                        clas = clas.getSuperclass();
                    }
                    if (method == null) {
                        
                        // still no match, finally try to find as a field
                        clas = obj.getClass();
                        while (!clas.getName().equals("java.lang.Object")) {
                            try {
                                field = clas.getDeclaredField(fname);
                                try {
                                    field.setAccessible(true);
                                } catch (SecurityException e) { /* deliberately empty */
                                }
                                tname = field.getType().getName();
                                break;
                            } catch (NoSuchFieldException e) {
                                clas = clas.getSuperclass();
                            }
                        }
                        if (field == null) {
                            throw new IllegalArgumentException("Parameter " + key + " not found");
                        }
                    }
                }
                if ("boolean".equals(tname) || "java.lang.Boolean".equals(tname)) {
                    
                    // convert text to boolean value
                    if ("true".equals(value) || "1".equals(value)) {
                        value = Boolean.TRUE;
                    } else if ("false".equals(value) || "0".equals(value)) {
                        value = Boolean.FALSE;
                    } else {
                        throw new IllegalArgumentException("Unknown value '" + value
                            + "' for boolean parameter " + key);
                    }
                    
                } else if ("[Ljava.lang.String;".equals(tname)) {
                    
                    // deserialize token list to string array
                    try {
                        value = org.jibx.runtime.Utility.deserializeTokenList((String)value);
                    } catch (JiBXException e) {
                        throw new IllegalArgumentException("Error processing list value + '" + value +
                            "': " + e.getMessage());
                    }
                    
                } else if ("int".equals(tname) || "java.lang.Integer".equals(tname)) {
                    
                    // convert text to int value
                    try {
                        value = new Integer(Utility.parseInt((String)value));
                    } catch (JiBXException e) {
                        throw new IllegalArgumentException("Error processing int value + '" + value +
                            "': " + e.getMessage());
                    }
                    
                } else if ("long".equals(tname) || "java.lang.Long".equals(tname)) {
                    
                    // convert text to long value
                    try {
                        value = new Long(Utility.parseLong((String)value));
                    } catch (JiBXException e) {
                        throw new IllegalArgumentException("Error processing long value + '" + value +
                            "': " + e.getMessage());
                    }
                    
                    
                } else if ("float".equals(tname) || "java.lang.Float".equals(tname)) {
                    
                    // convert text to float value
                    try {
                        value = new Float(Utility.parseFloat((String)value));
                    } catch (JiBXException e) {
                        throw new IllegalArgumentException("Error processing float value + '" + value +
                            "': " + e.getMessage());
                    }
                    
                } else if ("double".equals(tname) || "java.lang.Double".equals(tname)) {
                    
                    // convert text to float value
                    try {
                        value = new Double(Utility.parseDouble((String)value));
                    } catch (JiBXException e) {
                        throw new IllegalArgumentException("Error processing double value + '" + value +
                            "': " + e.getMessage());
                    }
                    
                } else if (!"java.lang.String".equals(tname)) {
                    throw new IllegalArgumentException("Cannot handle value of type " + tname);
                }
                if (method != null) {
                    
                    // set value using method
                    if (method.getParameterTypes().length == 1) {
                        method.invoke(obj, new Object[] { value });
                    } else {
                        method.invoke(obj, new Object[] { value, null });
                    }
                    
                } else {
                    
                    // set value using field
                    field.set(obj, value);
                    
                }
                fail = false;
                
            } catch (IllegalAccessException e) {
                t = e;
            } catch (SecurityException e) {
                t = e;
            } catch (InvocationTargetException e) {
                t = e;
            } finally {
                if (t != null) {
                    throw new IllegalArgumentException(t.getMessage());
                }
            }
            if (fail) {
                missmap.put(key, value);
            }
        }
        return missmap;
    }
}
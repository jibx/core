/*
Copyright (c) 2007-2009, Dennis M. Sosnoski.
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

package org.jibx.runtime.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.Utility;

/**
 * Support class providing methods used by generated code and binding factory
 * initialization.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class RuntimeSupport
{
    /**
     * Split concatenated class or method names string into an array of
     * fully-qualified individual class and/or method names. This is used by the
     * binding factory initialization, so that arrays of class and method names
     * can be represented efficiently in the generated code. It takes as input a
     * string consisting of compacted fully-qualified class names separated by
     * '|' delimiter characters. If the compacted name starts with one or more
     * '.' or '$' characters the corresponding number of package and class name
     * levels are copied from the last name. Empty names are left
     * <code>null</code> in the array.
     *
     * @param blob compacted class names separated by '|' delimiters
     * @return expanded class names
     */
    public static String[] splitClassNames(String blob) {
        if (blob == null || blob.length() == 0) {
            return Utility.EMPTY_STRING_ARRAY;
        } else {
            GrowableStringArray names = new GrowableStringArray();
            String last = "";
            int split = -1;
            int index = 0;
            StringBuffer buff = new StringBuffer();
            while (split < blob.length()) {
                int base = split + 1;
                split = blob.indexOf('|', base);
                if (split < 0) {
                    split = blob.length();
                }
                if (split > base) {
                    int mark = 0;
                    while (base < split) {
                        char chr = blob.charAt(base);
                        if (chr == '.') {
                            mark = last.indexOf('.', mark) + 1;
                        } else if (chr == '$') {
                            mark = last.indexOf('$', mark) + 1;
                        } else {
                            break;
                        }
                        base++;
                    }
                    buff.setLength(0);
                    if (base == split) {
                        if (mark < last.length()) {
                            while (++mark < last.length()) {
                                char chr = last.charAt(mark);
                                if (chr == '.' || chr == '$') {
                                    break;
                                }
                            }
                        }
                        buff.append(last.substring(0, mark));
                    } else {
                        if (mark > 0) {
                            buff.append(last.substring(0, mark));
                        }
                        buff.append(blob.substring(base, split));
                    }
                    last = buff.toString().intern();
                    names.add(last);
                } else {
                    names.add(null);
                }
                index++;
            }
            return names.toArray();
        }
    }
    
    /**
     * Expand names URI indexes into an array of individual names URIs. This is
     * used by the generated binding factory code, to reduce the size of the
     * generated classes and methods. It takes as input a string where each
     * character is a namespace index, biased by +2 in order to avoid the use of
     * null characters, along with a table of corresponding namespace URIs. The
     * character value 1 is used as a marker for the case where no namespace is
     * associated with an index.
     *
     * @param blob string of characters representing namespace indexes
     * @param uris namespace URIs defined in binding
     * @return expanded class names
     */
    public static String[] expandNamespaces(String blob, String[] uris) {
        if (blob == null || blob.length() == 0) {
            return Utility.EMPTY_STRING_ARRAY;
        } else {
            String[] nameuris = new String[blob.length()];
            for (int i = 0; i < blob.length(); i++) {
                int index = blob.charAt(i) - 2;
                if (index >= 0) {
                    nameuris[i] = uris[index];
                }
            }
            return nameuris;
        }
    }
    
    /**
     * Split concatenated names string into an array of individual names. This
     * is used by the binding factory initialization, so that arrays of name
     * strings can be represented efficiently in the generated code. It takes
     * as input a string consisting of names separated by '|' delimiter
     * characters.
     *
     * @param blob element names separated by '|' delimiters
     * @return expanded class names
     */
    public static String[] splitNames(String blob) {
        if (blob == null || blob.length() == 0) {
            return Utility.EMPTY_STRING_ARRAY;
        } else {
            GrowableStringArray names = new GrowableStringArray();
            int split = -1;
            int index = 0;
            while (split < blob.length()) {
                int base = split + 1;
                split = blob.indexOf('|', base);
                if (split < 0) {
                    split = blob.length();
                }
                if (split > base) {
                    names.add(blob.substring(base, split));
                } else {
                    names.add(null);
                }
                index++;
            }
            return names.toArray();
        }
    }
    
    /**
     * Split concatenated ints string into an array of individual int values.
     * This is used by the binding factory initialization, so that arrays of
     * ints can be represented efficiently in the generated code.
     *
     * @param blob
     * @return expanded index array
     */
    public static int[] splitInts(String blob) {
        int[] indexes = new int[blob.length()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = blob.charAt(i) - 1;
        }
        return indexes;
    }
    
    /**
     * Parser factory class loader method. This is used during initialization to
     * check that a particular factory class is usable.
     * 
     * @param cname class name
     * @return reader factory instance
     * @throws RuntimeException on error creating class instance
     */
    public static IXMLReaderFactory createReaderFactory(String cname) {
        
        // try loading factory class from context loader
        Class clas = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            try {
                clas = loader.loadClass(cname);
            } catch (ClassNotFoundException e) { /* deliberately empty */ }
        }
        if (clas == null) {
            
            // next try the class loader that loaded the unmarshaller interface
            try {
                loader = IUnmarshallingContext.class.getClassLoader();
                clas = loader.loadClass(cname);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException
                    ("Unable to specified parser factory class " + cname);
            }
        }
        if (! (IXMLReaderFactory.class.isAssignableFrom(clas))) {
            throw new RuntimeException("Specified parser factory class " +
                cname + " does not implement IXMLReaderFactory interface");
        }
        
        // use static method to create parser factory class instance
        try {
            Method meth = clas.getMethod("getInstance", (Class[])null);
            return (IXMLReaderFactory)meth.invoke(null, (Object[])null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Specified parser factory class " +
                cname + " does not define static getInstance() method");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error on parser factory class " +
                cname + " getInstance() method call: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error on parser factory class " +
                cname + " getInstance() method call: " + e.getMessage());
        }
    }
    
    /**
     * Load the appropriate reader factory. This checks for a system property
     * specifying the factory to be used, and if not found checks for an
     * instance of one of the standard default factories.
     *
     * @return factory instance
     */
    public static IXMLReaderFactory loadFactory() {
        String prop = null;
        try {
            prop = System.getProperty("org.jibx.runtime.impl.parser");
        } catch (SecurityException e) {
            /* exception just means the value will be null */
        }
        if (prop == null) {
            
            // try XMLPull parser factory first
            IXMLReaderFactory fact = null;
            try {
                fact = RuntimeSupport.createReaderFactory
                    ("org.jibx.runtime.impl.XMLPullReaderFactory");
            } catch (Throwable e) {
                try {
                    fact = RuntimeSupport.createReaderFactory
                        ("org.jibx.runtime.impl.StAXReaderFactory");
                } catch (Throwable e1) {
                    throw new RuntimeException("Unable to load either XMLPull or StAX parser - check classpath for interface and implementation jars\nXMLPull error " +
                        e.getClass().getName() + ": " + e.getMessage() + "\nStAX error " + e1.getClass().getName() +
                        ": " + e1.getMessage());
                }
            }
            return fact;
            
        } else {
            
            // try loading factory class specified by property
            return RuntimeSupport.createReaderFactory(prop);
        }
    }
}
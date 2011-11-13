/*
Copyright (c) 2008, Dennis M. Sosnoski.
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
import java.util.HashMap;
import java.util.Map;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Base class for generated binding factories. This provides common
 * implementation code, so that the code does not need to be duplicated in every
 * generated binding factory.
 *
 * @author Dennis M. Sosnoski
 */
public abstract class BindingFactoryBase implements IBindingFactory
{
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    
    private final String m_bindingName;
    private final int m_majorVersion;
    private final int m_minorVersion;
    private final String[] m_bindingClasses;
    private final String[] m_mapNames;
    private final StringIntHashMap m_classIndexMap;
    private final String[] m_unmarshallers;
    private final String[] m_marshallers;
    private final String[] m_uris;
    private final String[] m_prefixes;
    private final String[] m_globalNames;
    private final String[] m_globalUris;
    private final String[] m_idClassNames;
    private final String[][] m_abstractMappingDetails;
    private final int[][] m_abstractMappingNamespaces;
    private final String[] m_baseNames;
    private final String m_baseHashes;
    private final String[] m_bindingFactories;
    private final Class[] m_marshallerClasses;
    private final Class[] m_unmarshallerClasses;
    private final Map m_bindingNamespaceTables;
    private final int m_hash;
    private boolean m_verified;
    private String[] m_bindingClassesClosure;
    private Map m_unmarshalMap;
    
    /**
     * Constructor used in generated binding factories.
     * 
     * @param name binding name
     * @param majorver binding major version number
     * @param minorver binding minor version number
     * @param boundnames blob of class names with code for this binding
     * @param mappednames blob of class or type names for mappings
     * @param umarnames unmarshaller class names blob (<code>null</code> if
     * output-only binding)
     * @param marnames marshaller class names blob (<code>null</code> if
     * input-only binding)
     * @param uris namespace URIs used by binding
     * @param prefixes namespace prefixes used by binding (<code>null</code>
     * if input-only binding)
     * @param gmapnames globally-mapped element names blob
     * @param gmapuris globally-mapped element namespaces blob
     * @param idclasses names of classes with IDs
     * @param abmapdetails abstract mapping details blob
     * @param abmapnss abstract mapping namespace indexes blob
     * @param prenames precompiled base binding names blob
     * @param prefacts base binding factory classes blob
     * @param prehashes base binding hashes blob
     * @param prensmaps namespace index mapping tables blobs for precompiled
     * bindings
     */
    protected BindingFactoryBase(String name, int majorver, int minorver,
        String boundnames, String mappednames, String umarnames,
        String marnames, String[] uris, String[] prefixes, String gmapnames,
        String gmapuris, String[] idclasses, String abmapdetails,
        String abmapnss, String prenames, String prefacts, String prehashes,
        String[] prensmaps) {
        m_bindingName = name;
        m_majorVersion = majorver;
        m_minorVersion = minorver;
        m_bindingClasses = RuntimeSupport.splitClassNames(boundnames);
        m_mapNames = RuntimeSupport.splitClassNames(mappednames);
        m_classIndexMap = new StringIntHashMap(m_mapNames.length, 0.1);
        for (int i = 0; i < m_mapNames.length; i++) {
            m_classIndexMap.add(m_mapNames[i], i);
        }
        if (marnames == null) {
            m_marshallers = null;
            m_marshallerClasses = new Class[0];
        } else {
            m_marshallers = RuntimeSupport.splitClassNames(marnames);
            m_marshallerClasses = new Class[m_marshallers.length];
        }
        if (umarnames == null) {
            m_unmarshallers = null;
            m_unmarshallerClasses = new Class[0];
        } else {
            m_unmarshallers = RuntimeSupport.splitClassNames(umarnames);
            m_unmarshallerClasses = new Class[m_unmarshallers.length];
        }
        m_uris = uris;
        m_prefixes = prefixes;
        m_globalNames = RuntimeSupport.splitNames(gmapnames);
        m_globalUris = RuntimeSupport.expandNamespaces(gmapuris, uris);
        m_idClassNames = idclasses;
        String[] names = RuntimeSupport.splitClassNames(abmapdetails);
        int abmapcount = names.length / ABMAP_COUNT;
        String[][] details = new String[ABMAP_COUNT][];
        for (int i = 0; i < ABMAP_COUNT; i++) {
            details[i] = new String[abmapcount];
        }
        for (int i = 0; i < names.length; i++) {
            details[i%ABMAP_COUNT][i/ABMAP_COUNT] = names[i];
        }
        m_abstractMappingDetails = details;
        int[][] nss = new int[abmapcount][];
        int base = 0;
        for (int i = 0; i < abmapcount; i++) {
            int length = abmapnss.charAt(base++);
            if (length == 1) {
                nss[i] = EMPTY_INT_ARRAY;
            } else {
                int[] indexes = new int[--length];
                for (int j = 0; j < length; j++) {
                    indexes[j] = abmapnss.charAt(base+j) - 1;
                }
                base += length;
                nss[i] = indexes;
            }
        }
        m_abstractMappingNamespaces = nss;
        m_baseNames = RuntimeSupport.splitNames(prenames);
        m_baseHashes = prehashes;
        m_bindingFactories = RuntimeSupport.splitClassNames(prefacts);
        Map tablemap = new HashMap();
        for (int i = 0; i < prensmaps.length; i++) {
            String blob = prensmaps[i];
            if (blob != null) {
                int[] indexes = RuntimeSupport.splitInts(blob);
                tablemap.put(m_bindingFactories[i], indexes);
            }
        }
        m_bindingNamespaceTables = tablemap;
        
        // compute the hash code for this binding
        int hash = name.hashCode() + majorver + minorver +
            boundnames.hashCode() + mappednames.hashCode();
        if (umarnames != null) {
            hash += umarnames.hashCode();
        }
        if (marnames != null) {
            hash += marnames.hashCode();
        }
        for (int i = 0; i < uris.length; i++) {
            hash += uris[i].hashCode();
        }
        if (prefixes != null) {
            for (int i = 0; i < prefixes.length; i++) {
                String prefix = prefixes[i];
                if (prefix != null) {
                    hash += prefix.hashCode();
                }
            }
        }
        hash += gmapnames.hashCode() + gmapuris.hashCode();
        if (idclasses != null) {
            for (int i = 0; i < idclasses.length; i++) {
                hash += idclasses[i].hashCode();
            }
        }
        hash += abmapdetails.hashCode() + abmapnss.hashCode() +
            prenames.hashCode() + prefacts.hashCode();
        m_hash = hash;
    }
    
    /**
     * Load a class. This first tries to load the specified class using the
     * classloader that loaded the binding factory instance, then tries the
     * thread context classloader, then finally tries the classloader used to
     * load this class.
     *
     * @param name fully qualified class name
     * @return loaded class, or <code>null</code> if class not found
     */
    public Class loadClass(String name) {
        
        // first try loading class from instance class loader
        Class clas = null;
        ClassLoader factldr = null;
        factldr = getClass().getClassLoader();
        try {
            clas = factldr.loadClass(name);
        } catch (ClassNotFoundException e) { /* fall through */ }
        if (clas == null) {
            
            // next try the context class loader, if set
            ClassLoader ctxldr = Thread.currentThread().getContextClassLoader();
            if (ctxldr != null) {
                try {
                    clas = ctxldr.loadClass(name);
                } catch (ClassNotFoundException e) { /* fall through */ }
            }
            if (clas == null) {
                
                // not found, try the loader that loaded this class
                ClassLoader thisldr = BindingFactoryBase.class.getClassLoader();
                if (thisldr != factldr && thisldr != ctxldr) {
                    try {
                        clas = thisldr.loadClass(name);
                    } catch (ClassNotFoundException e)
                        { /* fall through */ }
                }
            }
        }
        return clas;
    }

    /**
     * Create a new marshalling context.
     *
     * @return context
     * @throws JiBXException
     */
    public IMarshallingContext createMarshallingContext() throws JiBXException {
        return new MarshallingContext(m_mapNames, m_marshallers, m_uris, this);
    }

    /**
     * Create a new unmarshalling context.
     *
     * @return context
     * @throws JiBXException
     */
    public IUnmarshallingContext createUnmarshallingContext()
        throws JiBXException {
        return new UnmarshallingContext(m_unmarshallers.length, m_unmarshallers,
            m_globalUris, m_globalNames, m_idClassNames, this);
    }

    /**
     * Get the binding name.
     *
     * @return name
     */
    public String getBindingName() {
        return m_bindingName;
    }
    
    /**
     * Get hash for binding. The computed hash value is based on all the values
     * returned by all the methods of this interface, with the exception of the
     * {@link #getMarshallerClass(int)} and {@link #getUnmarshallerClass(int)}
     * methods returning <code>Class</code> objects.
     *
     * @return hash
     */
    public int getHash() {
        return m_hash;
    }

    /**
     * Get major version number.
     *
     * @return major version
     */
    public int getMajorVersion() {
        return m_majorVersion;
    }
    
    /**
     * Get minor version number.
     *
     * @return minor version
     */
    public int getMinorVersion() {
        return m_minorVersion;
    }

    /**
     * Get the classes used by the binding. Every class which includes code
     * generated by the binding compiler for this binding or any precompiled
     * base binding is included in the returned array. If {@link
     * #verifyBaseBindings()} has not already been invoked it will be invoked by
     * this call.
     *
     * @return fully-qualified class names
     * @throws JiBXException on base binding verification error
     */
    public String[] getBindingClasses() throws JiBXException {
        verifyBaseBindings();
        return m_bindingClassesClosure;
    }

    /**
     * Get global-mapped element names.
     *
     * @return names
     */
    public String[] getElementNames() {
        return m_globalNames;
    }
    
    /**
     * Get marshaller class names.
     *
     * @return array of class names
     */
    public String[] getMarshallerClasses() {
        return m_marshallers;
    }
    
    /**
     * Get unmarshaller class names.
     *
     * @return array of class names
     */
    public String[] getUnmarshallerClasses() {
        return m_unmarshallers;
    }

    /**
     * Get global-mapped element namespace URIs.
     *
     * @return uris
     */
    public String[] getElementNamespaces() {
        return m_globalUris;
    }

    /**
     * Get mapped class names (or type names, in the case of abstract mappings).
     * Returns array of fully-qualified class and/or type names, ordered by
     * index number of the class.
     *
     * @return array of class names
     */
    public String[] getMappedClasses() {
        return m_mapNames;
    }

    /**
     * Get map from fully-qualified class name to the index number of the class
     * used for accessing the arrays of class names. The value returned is the
     * index for the class in the arrays returned by {@link
     * #getMappedClasses()}, {@link #getMarshallerClasses()}, and {@link
     * #getUnmarshallerClasses()}, and can also be used as input for {@link
     * #getMarshallerClass(int)} and {@link #getUnmarshallerClass(int)}.
     *
     * @return map from fully-qualified class name to index number
     */
    public StringIntHashMap getClassIndexMap() {
        return m_classIndexMap;
    }
    
    /**
     * Get the linkage information for global abstract mappings included in the
     * binding. See {@link IBindingFactory#getAbstractMappings()} for details.
     *
     * @return method information array
     */
    public String[][] getAbstractMappings() {
        return m_abstractMappingDetails;
    }

    /**
     * Get the indexes of the namespaces used by an abstract mapping.
     *
     * @param index abstract mapping index, corresponding to the abstract
     * mapping information returned by {@link #getAbstractMappings()}.
     * @return namespace indexes, empty array if none
     */
    public int[] getAbstractMappingNamespaces(int index) {
        return m_abstractMappingNamespaces[index];
    }

    /**
     * Get the namespace URIs used by the binding.
     *
     * @return uris
     */
    public String[] getNamespaces() {
        return m_uris;
    }
    
    /**
     * Get the names of the separately-compiled base bindings used by this
     * binding.
     *
     * @return binding names
     */
    public String[] getBaseBindings() {
        return m_baseNames;
    }
    
    /**
     * Verify that separately-compiled base bindings used by this binding can be
     * loaded and are compatible with the base bindings used when this binding
     * was compiled.
     * 
     * @throws JiBXException on verification failure
     */
    public void verifyBaseBindings() throws JiBXException {
        if (!m_verified) {
            if (m_bindingFactories.length == 0) {
                m_bindingClassesClosure = m_bindingClasses;
            } else {
                
                // access binding factories to verify hashes and get classes
                GrowableStringArray classes = new GrowableStringArray();
                for (int i = 0; i < m_bindingFactories.length; i++) {
                    String fname = m_bindingFactories[i];
                    Class fclas = loadClass(fname);
                    if (fclas == null) {
                        throw new JiBXException("Unable to load precompiled base binding '" + m_baseNames[i] + "' used by binding '" + getBindingName() + '\'');
                    } else {
                        Exception ex = null;
                        try {
                            Method method = fclas.getMethod
                                (BindingDirectory.FACTORY_INSTMETHOD,
                                BindingDirectory.EMPTY_ARGS);
                            Object result = method.invoke(null, (Object[])null);
                            if (result instanceof IBindingFactory) {
                                IBindingFactory fact = (IBindingFactory)result;
                                int match = (m_baseHashes.charAt(i*2) << 16) +
                                m_baseHashes.charAt(i*2+1);
                                if (fact.getHash() != match) {
                                    throw new JiBXException("Precompiled base binding '" + m_baseNames[i] + "' has changed since binding '" + getBindingName() + "' was compiled");
                                } else {
                                    classes.addAll(fact.getBindingClasses());
                                }
                            } else {
                                throw new JiBXException("Classloader conflict for precompiled base binding '" + m_baseNames[i] + "' used by binding '" + getBindingName() + '\'');
                            }
                        } catch (IllegalAccessException e) {
                            ex = e;
                        } catch (SecurityException e) {
                            ex = e;
                        } catch (NoSuchMethodException e) {
                            ex = e;
                        } catch (InvocationTargetException e) {
                            ex = e;
                        }
                        if (ex != null) {
                            throw new JiBXException("Error creating factory for precompiled base binding '" + m_baseNames[i] + "' used by binding '" + getBindingName() + "':" + ex.getMessage());
                        }
                    }
                }
                m_bindingClassesClosure = classes.toArray();
                
            }
            m_verified = true;
        }
    }
    
    /**
     * Get the names of the binding factory classes for the separately-compiled
     * base bindings used by this binding.
     *
     * @return binding factory fully-qualified class names
     */
    public String[] getBaseBindingFactories() {
        return m_bindingFactories;
    }

    /**
     * Get a map from full-qualified binding factory names to an array of
     * <code>int</code> values used to convert namespace indexes in that binding
     * to this binding. If the binding uses the same namespaces as this binding
     * (or a subset of the same namespaces, with the same index values) there is
     * no entry in the map.
     *
     * @return map to namespace index translation
     */
    public Map getNamespaceTranslationTableMap() {
        return m_bindingNamespaceTables;
    }

    /**
     * Get the namespace prefixes used by the binding.
     *
     * @return prefixes
     */
    public String[] getPrefixes() {
        return m_prefixes;
    }
    /**
     * Get the mapping from element local name to class indexes. If a local name
     * is only used with a single namespace, the value for that name is an
     * <code>Integer</code> giving the index of the class mapped to the name; if
     * the local name is used with multiple namespaces, the value for that name
     * is an array with multiple <code>int</code> class indexes, for every class
     * mapped to the name.
     *
     * @return map from local name to class index array
     */
    public synchronized Map getUnmarshalMap() {
        if (m_unmarshalMap == null) {
            Map map = new HashMap();
            for (int i = 0; i < m_globalNames.length; i++) {
                String name = m_globalNames[i];
                if (name != null) {
                    Object value = map.get(name);
                    if (value instanceof Integer) {
                        int[] ints = new int[2];
                        ints[0] = ((Integer)value).intValue();
                        ints[1] = i;
                        map.put(name, ints);
                    } else if (value instanceof int[]) {
                        int[] olds = (int[])value;
                        int length = olds.length;
                        int[] ints = new int[length+1];
                        System.arraycopy(olds, 0, ints, 0, length);
                        ints[length] = i;
                        map.put(name, ints);
                    } else {
                        map.put(name, new Integer(i));
                    }
                }
            }
            m_unmarshalMap = map;
        }
        return m_unmarshalMap;
    }

    /**
     * Get the marshaller class for a mapping. This can only be used for global
     * mappings.
     *
     * @param index marshaller class index
     * @return marshaller class, or <code>null</code> if unable to load class
     */
    public Class getMarshallerClass(int index) {
        Class clas = m_marshallerClasses[index];
        if (clas == null) {
            clas = loadClass(m_marshallers[index]);
        }
        return clas;
    }

    /**
     * Get the unmarshaller class for a mapping. This can only be used for
     * global mappings.
     *
     * @param index unmarshaller class index
     * @return unmarshaller class, or <code>null</code> if unable to load class
     */
    public Class getUnmarshallerClass(int index) {
        Class clas = m_unmarshallerClasses[index];
        if (clas == null) {
            clas = loadClass(m_unmarshallers[index]);
        }
        return clas;
    }
}
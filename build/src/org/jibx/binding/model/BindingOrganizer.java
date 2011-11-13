/*
 * Copyright (c) 2008-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.binding.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jibx.binding.Utility;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.runtime.impl.MarshallingContext;
import org.jibx.util.InsertionOrderedMap;
import org.jibx.util.InsertionOrderedSet;
import org.jibx.util.LazyList;
import org.jibx.util.UniqueNameSet;

/**
 * Organizer for a set of bindings under construction. This tracks the individual bindings by default namespace or by
 * associated object (to allow multiple bindings using a single namespace), and manages the relationships between the
 * different bindings.
 * 
 * @author Dennis M. Sosnoski
 */
public class BindingOrganizer
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(BindingOrganizer.class.getName());
    
    /** Base prefix used when no prefix set for namespace. */
    private static final String BASE_PREFIX = "ns";
    
    //
    // Flags to initialize constructed bindings
    private final boolean m_forceClasses;
    private final boolean m_trackSource;
    private final boolean m_addConstructors;
    private final boolean m_inBinding;
    private final boolean m_outBinding;
    private final boolean m_trimWhitespace;
    
    /** Map from namespace URI to prefix, for bindings with prefix specified. */
    private final Map m_uriPrefixFixed;
    
    /** Map from namespace URI to default prefix for that namespace. */
    private final Map m_uriPrefixDefaults;
    
    /** List of unique key objects for bindings. Multiple key objects may be supplied for each binding, so this just
     tracks the one used when initially creating the binding. */
    private final List m_keyObjects;
    
    /** Map from object to binding holder for that namespace. */
    private final Map m_objectBindings;
    
    /** Map from namespace URIs referenced in bindings not using that namespace to first referencing binding. */
    private final Map m_referenceUriBinding;
    
    /** Set of namespace URIs needing to be defined in root binding. */
    private final Set m_nsRootUris;
    
    /** List of format definitions in bindings. */
    private final List m_formats;
    
    /** Flag for no-namespace namespace used from namespaced binding. */
    private boolean m_nonamespaceUsed;
    
    /** Bindings finalized flag. */
    private boolean m_finished;
    
    /**
     * Constructor taking flags used with constructed bindings.
     * 
     * @param force force classes flag
     * @param track track source flag
     * @param addcon add constructors flag
     * @param in input binding flag
     * @param out output binding flag
     * @param trim trim whitespace flag
     */
    public BindingOrganizer(boolean force, boolean track, boolean addcon, boolean in, boolean out, boolean trim) {
        s_logger.debug("Created binding organizer with force=" + force + ", track=" + track + ", addcon=" + addcon + ", in=" + in + ", out=" + out);
        m_uriPrefixFixed = new HashMap();
        m_forceClasses = force;
        m_trackSource = track;
        m_addConstructors = addcon;
        m_inBinding = in;
        m_outBinding = out;
        m_trimWhitespace = trim;
        m_uriPrefixDefaults = new HashMap();
        m_keyObjects = new ArrayList();
        m_objectBindings = new HashMap();
        m_referenceUriBinding = new InsertionOrderedMap();
        m_nsRootUris = new InsertionOrderedSet();
        m_formats = new LazyList();
    }

    /**
     * Add a binding to the set in use.
     * 
     * @param obj object associated with binding (can be namespace URI, if only one binding per namespace)
     * @param uri namespace URI (<code>null</code> if no namespace)
     * @param prefix namespace prefix (<code>null</code> if not specified, empty string if unprefixed default namespace)
     * @param dflt namespace is default for elements in binding flag
     * @return created binding holder
     */
    public BindingHolder addBinding(Object obj, String uri, String prefix, boolean dflt) {
        
        // check for errors with no-namespace namespace
        if (uri == null) {
            if (!dflt) {
                s_logger.error("No-namespace namespace must be element default");
                throw new IllegalArgumentException("Internal error - no-namespace namespace must be element default");
            }
            if (prefix != null && prefix.length() > 0) {
                s_logger.error("Cannot use prefix ('" + prefix + "' requested) for no-namespace namespace");
                throw new IllegalArgumentException("Configuration error - cannot use prefix ('" + prefix + "' requested) for no-namespace namespace");
            } else {
                prefix = "";
            }
        }
        
        // configure binding holder and prefix
        BindingHolder hold = new BindingHolder(uri, dflt, this);
        m_keyObjects.add(obj);
        m_objectBindings.put(obj, hold);
        if (prefix != null) {
            String prior = (String)m_uriPrefixFixed.get(uri);
            if (prior == null) {
                m_uriPrefixFixed.put(uri, prefix);
            } else if (!prior.equals(prefix)) {
                s_logger.error("Conflicting prefixes ('" + prefix + "' and '" + prior + "') for namespace " + uri);
               throw new IllegalArgumentException("Configuration error - conflicting prefixes ('" + prefix + "' and '" + prior + "') for namespace " + uri);
            }
            hold.setPrefix(prefix);
        }
        if (s_logger.isDebugEnabled()) {
            s_logger.debug("Added binding associatiated with " + obj + " with namespace " + uri + " and prefix " + prefix + " (dflt=" + dflt + ')');
        }
        
        // configure the actual binding element
        BindingElement binding = hold.getBinding();
        binding.setForceClasses(m_forceClasses);
        binding.setTrackSource(m_trackSource);
        binding.setAddConstructors(m_addConstructors);
        binding.setInBinding(m_inBinding);
        binding.setOutBinding(m_outBinding);
        binding.setTrimWhitespace(m_trimWhitespace);
        return hold;
    }

    /**
     * Get the binding associated with a particular control object and namespace, if defined.
     * 
     * @param obj object associated with binding
     * @return binding holder, or <code>null</code> if not yet defined
     */
    public BindingHolder getBinding(Object obj) {
        return (BindingHolder)m_objectBindings.get(obj);
    }

    /**
     * Get the binding associated with a particular control object.
     * 
     * @param obj object associated with binding (can be namespace URI, if only one binding per namespace)
     * @return binding holder
     */
    public BindingHolder getRequiredBinding(Object obj) {
        BindingHolder hold = (BindingHolder)m_objectBindings.get(obj);
        if (hold == null) {
            s_logger.error("Missing required binding for object " + obj);
            throw new IllegalStateException("Internal error - binding not defined for " + obj);
        } else {
            return hold;
        }
    }

    /**
     * Associate a control object with an existing binding. The control object is added in addition to any other control
     * objects for that binding, so that the binding will be returned when any of the control objects is used for look
     * up.
     * 
     * @param obj object associated with binding (can be namespace URI, if only one binding per namespace)
     * @param holder binding holder
     */
    public void addBindingObject(Object obj, BindingHolder holder) {
        m_objectBindings.put(obj, holder);
    }
    
    /**
     * Set the default prefix for a namespace, if none already set.
     *
     * @param uri namespace URI
     * @param pref prefix to be used (empty string if to be default namespace)
     */
    public void addDefaultPrefix(String uri, String pref) {
        if (!m_uriPrefixDefaults.containsKey(uri)) {
            m_uriPrefixDefaults.put(uri, pref);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Added default prefix '" + pref + "' for namespace " + uri);
            }
        }
    }
    
    /**
     * Force specified prefix to be used for binding.
     *
     * @param prefix
     * @param holder
     */
    public void forceBindingPrefix(String prefix, BindingHolder holder) {
        holder.setPrefix(prefix);
    }
    
    /**
     * General object comparison method. Don't know why Sun hasn't seen fit to include this somewhere, but at least it's
     * easy to write (over and over again).
     * 
     * @param a first object to be compared
     * @param b second object to be compared
     * @return <code>true</code> if both objects are <code>null</code>, or if <code>a.equals(b)</code>;
     * <code>false</code> otherwise
     */
    public static boolean isEqual(Object a, Object b) {
        return (a == null) ? b == null : a.equals(b);
    }
    
    /**
     * Internal check method to verify that the collection of bindings is still modifiable.
     */
    private void checkModifiable() {
        if (m_finished) {
            throw new IllegalStateException("Internal error - attempt to modify bindings after finalized");
        }
    }
    
    /**
     * Add usage of namespace for an element or attribute name in binding, assuring that the namespace definition will
     * be in-scope for that binding. If a namespace is defined by one binding and referenced by another, or if it's
     * referenced by more than one binding, it will be defined at the root binding level. This method is used as a
     * callback from the binding holder code, in order to track usage of namespaces across all bindings.
     * 
     * @param hold binding containing usage
     * @param uri referenced namespace URI (<code>null</code> if no-namespace)
     */
    /*default*/ void addNamespaceUsage(BindingHolder hold, String uri) {
        
        // first check if anything needs to be done
        checkModifiable();
        if (uri == null) {
            m_nonamespaceUsed = true;
        } else if (!MarshallingContext.XML_NAMESPACE.equals(uri) && !uri.equals(hold.getNamespace()) &&
            !m_nsRootUris.contains(uri)) {
            
            // check if any other binding has previously referenced this namespace
            BindingHolder refhold = (BindingHolder)m_referenceUriBinding.get(uri);
            if (refhold == null) {
                
                // first reference, just record it in map in case also used by another binding
                m_referenceUriBinding.put(uri, hold);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Recorded first usage of namespace " + uri + " from binding with namespace " + hold.getNamespace());
                }
                
            } else if (refhold != hold) {
                
                // multiple reference, add namespace binding definition to root binding
                m_nsRootUris.add(uri);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Added namespace definition for " + uri + " to root binding after reference from binding with namespace " + hold.getNamespace());
                }
                
            }
        }
    }
    
    /**
     * Add reference from binding to a type name defined in the same or another binding.
     * 
     * @param hold binding containing reference
     * @param uri namespace URI for type name
     * @param obj object associated with referenced binding
     */
    public void addTypeNameReference(BindingHolder hold, String uri, Object obj) {
        checkModifiable();
        if (uri != null && hold.getReferencedNamespaces().add(uri)) {
            BindingHolder tohold = getRequiredBinding(obj);
            if (hold != tohold) {
                tohold.forcePullUpNamespaces();
            }
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Added reference to binding with namespace " + uri +
                    " from binding with namespace " + hold.getNamespace());
            }
        }
    }
    
    /**
     * Add a format definition to the binding.
     *
     * @param format
     */
    public void addFormat(FormatElement format) {
        checkModifiable();
        m_formats.add(format);
    }
    
    /**
     * Iterate the collection of bindings. <code>null</code> values may be present in the collection, due to bindings
     * which have been eliminated.
     *
     * @return iterator
     */
    public Iterator iterateBindings() {
        return m_objectBindings.values().iterator();
    }
    
    /**
     * Adds a collection of namespace URIs to be referenced at root binding level.
     * 
     * @param uris
     */
    public void addRootUris(Collection uris) {
        m_nsRootUris.addAll(uris);
    }
    
    /**
     * Get the list of binding key objects.
     * 
     * @return keys
     */
    public List getKeys() {
        return m_keyObjects;
    }
    
    /**
     * Check if a character is an ASCII alpha character.
     * 
     * @param chr
     * @return alpha character flag
     */
    private static boolean isAsciiAlpha(char chr) {
        return (chr >= 'a' && chr <= 'z') || (chr >= 'A' && chr <= 'Z');
    }
    
    /**
     * Check if a character is an ASCII numeric character.
     * 
     * @param chr
     * @return numeric character flag
     */
    private static boolean isAsciiNum(char chr) {
        return chr >= '0' && chr <= '9';
    }
    
    /**
     * Check if a character is an ASCII alpha or numeric character.
     * 
     * @param chr
     * @return alpha or numeric character flag
     */
    private static boolean isAsciiAlphaNum(char chr) {
        return isAsciiAlpha(chr) || isAsciiNum(chr);
    }
    
    /**
     * Generate prefix if not already set for namespace.
     *
     * @param uri
     * @param prefset
     * @param uridfltprefs
     */
    private void generatePrefix(String uri, UniqueNameSet prefset, Map uridfltprefs) {
        if (uri != null && !uridfltprefs.containsKey(uri)) {
            String prefix = prefset.add(BASE_PREFIX);
            uridfltprefs.put(uri, prefix);
            if (s_logger.isDebugEnabled()) {
                s_logger.debug("Assigned generated prefix '" + prefix + "' for namespace " + uri);
            }
        }
    }

    /**
     * <p>Set the default prefixes to be used for namespaces. As part of constructing the map it also checks for
     * conflicts on specified prefixes for namespaces used at the root binding level. This is a complex process, due to
     * the way namespaces work across bindings with abstract mapping references. Since an abstract mapping doesn't
     * define a wrapper element, all namespaces used by the abstract mapping need to be declared on the containing
     * element. In practice, this means that any namespaces in the binding defining the abstract mapping need to be
     * merged into the set of namespaces in the binding making the reference, and these namespaces all must use distinct
     * prefixes. Furthermore, a static analysis of the namespace usage may not be enough to detect conflicts in usage,
     * since even if there are no conflicts in a set of binding definitions conflicts may be created when these bindings
     * are referenced from other bindings (either through direct includes, or as precompiled bindings).</p>
     *
     * <p>This code checks for any direct conflicts on namespaces with specified prefixes which are being defined in the
     * root binding and throws an exception if such a conflict is found, and creates guaranteed-unique prefixes in cases
     * where no prefixes are provided (either as specified values or as defaults). It does not prevent the user from
     * either specifying prefixes or using default prefixes which may result in conflicts when the bindings are used by
     * other bindings.</p>
     *
     * @param prefset prefixes in use
     * @param nsdfltpref map from namespace URI to default prefix (<code>null</code> value for namespace used as the
     * default allowed)
     * @return map from namespace URI to non-<code>null</code> and non-empty prefix (used for namespace declarations)
    */
    private Map buildDefaultPrefixes(UniqueNameSet prefset, Map nsdfltpref) {
        
        // start by checking for no-namespace namespace used
        if (m_nonamespaceUsed) {
            prefset.add("");
            nsdfltpref.put(null, "");
        }
        
        // check if namespaces are being defined in root binding
        if (m_nsRootUris.size() > 0) {
            
            // set prefix for every namespace used at root level (with unique value if constructed). this first pass
            //  attempts to use prefixes which have been set on bindings (if any).
            if (m_uriPrefixFixed.size() > 0) {
                s_logger.debug("Setting prefixes for namespaces used at root level based on prefixes specified for bindings...");
                for (Iterator iter = m_nsRootUris.iterator(); iter.hasNext();) {
                    String uri = (String)iter.next();
                    if (!nsdfltpref.containsKey(uri)) {
                        String prefix = (String)m_uriPrefixFixed.get(uri);
                        if (prefset.contains(prefix)) {
                            s_logger.debug("Conflicting use of prefix '" + prefix + "' for multiple namespaces used in root binding");
                            throw new IllegalStateException("Conflicting use of prefix '" + prefix + "' for multiple namespaces used in root binding - change one of the uses of this prefix");
                        } else if (prefix != null) {
                            nsdfltpref.put(uri, prefix);
                            prefset.add(prefix);
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Assigned prefix '" + prefix + "' for namespace " + uri);
                            }
                        }
                    }
                }
            }
            
            // second pass of setting prefix for namespaces used at root level uses configured defaults (if any)
            if (m_uriPrefixDefaults.size() > 0 && nsdfltpref.size() < m_nsRootUris.size()) {
                s_logger.debug("Setting prefixes for namespaces used at root level based on configured default prefixes...");
                for (Iterator iter = m_nsRootUris.iterator(); iter.hasNext();) {
                    String uri = (String)iter.next();
                    if (!nsdfltpref.containsKey(uri)) {
                        String prefix = (String)m_uriPrefixDefaults.get(uri);
                        if (!prefset.contains(prefix)) {
                            nsdfltpref.put(uri, prefix);
                            prefset.add(prefix);
                            if (s_logger.isDebugEnabled()) {
                                s_logger.debug("Assigned prefix '" + prefix + "' for namespace " + uri);
                            }
                        }
                    }
                }
            }
            
            // third pass of setting prefix for namespaces used at root level uses unique generated prefixes
            if (!prefset.contains(BASE_PREFIX)) {
                prefset.add(BASE_PREFIX);
            }
            if (nsdfltpref.size() < m_nsRootUris.size()) {
                s_logger.debug("Generating prefixes for remaining namespaces used at root level...");
                for (Iterator iter = m_nsRootUris.iterator(); iter.hasNext();) {
                    generatePrefix((String)iter.next(), prefset, nsdfltpref);
                }
            }
        } else {
            prefset.add(BASE_PREFIX);
        }
        
        // now that prefixes are configured for namespaces used at root level, add other default prefixes
        if (m_uriPrefixFixed.size() > 0) {
            s_logger.debug("Setting namespace prefixes based on prefixes specified for bindings...");
            for (Iterator iter = m_uriPrefixFixed.keySet().iterator(); iter.hasNext();) {
                String uri = (String)iter.next();
                if (!nsdfltpref.containsKey(uri)) {
                    String prefix = (String)m_uriPrefixFixed.get(uri);
                    if (!prefset.contains(prefix)) {
                        nsdfltpref.put(uri, prefix);
                        prefset.add(prefix);
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Assigned prefix '" + prefix + "' for namespace " + uri);
                        }
                    }
                }
            }
        }
        if (m_uriPrefixDefaults.size() > 0) {
            s_logger.debug("Setting namespace prefixes based on configured defaults...");
            for (Iterator iter = m_uriPrefixDefaults.keySet().iterator(); iter.hasNext();) {
                String uri = (String)iter.next();
                if (!nsdfltpref.containsKey(uri)) {
                    String prefix = (String)m_uriPrefixDefaults.get(uri);
                    if (!prefset.contains(prefix)) {
                        nsdfltpref.put(uri, prefix);
                        prefset.add(prefix);
                        if (s_logger.isDebugEnabled()) {
                            s_logger.debug("Assigned prefix '" + prefix + "' for namespace " + uri);
                        }
                    }
                }
            }
        }        
        
        // finally, collect all used and referenced namespaces and create default prefixes for any which don't already
        //  have them
        s_logger.debug("Finish prefix assignment for all namespaces referenced or used...");
        for (Iterator iter = m_keyObjects.iterator(); iter.hasNext();) {
            BindingHolder holder = (BindingHolder)m_objectBindings.get(iter.next());
            String uri = holder.getNamespace();
            if (holder.isBindingNamespaceUsed() && !holder.isNamespaceElementDefault()) {
                generatePrefix(uri, prefset, nsdfltpref);
            } else if (!nsdfltpref.containsKey(uri)) {
                nsdfltpref.put(uri, "");
            }
            for (Iterator jter = holder.getReferencedNamespaces().iterator(); jter.hasNext();) {
                generatePrefix((String)jter.next(), prefset, nsdfltpref);
            }
            for (Iterator jter = holder.getUsedNamespaces().iterator(); jter.hasNext();) {
                generatePrefix((String)jter.next(), prefset, nsdfltpref);
            }
        }
        
        // finish by building second map with an actual prefix for every namespace
        s_logger.debug("Generate prefixes for namespaces used as default...");
        Map frcedprfx = new HashMap();
        for (Iterator iter = nsdfltpref.keySet().iterator(); iter.hasNext();) {
            String uri = (String)iter.next();
            String prefix = (String)nsdfltpref.get(uri);
            if (prefix == null || prefix.length() == 0) {
                prefix = prefset.add(BASE_PREFIX);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Assigned generated prefix '" + prefix + "' for namespace " + uri);
                }
            }
            frcedprfx.put(uri, prefix);
        }
        return frcedprfx;
    }

    /**
     * Configure the names to be used for writing bindings to files. If only one binding has been defined, it just gets
     * the supplied name. If multiple bindings have been defined, a single root binding is constructed which includes
     * all the other bindings and defines namespaces for those bindings, all &lt;format> definitions are moved to that
     * root binding, and is is given the supplied name while the other bindings are given unique names within the same
     * directory.
     * 
     * @param rootname file name for root or singleton binding definition
     * @param trgtpack target package for binding (<code>null</code> if unspecified)
     * @param pregens pregenerated bindings to be included in root binding
     * @return root or singleton binding holder
     */
    public BindingHolder configureFiles(String rootname, String trgtpack, List pregens) {
        
        // make sure not already called
        checkModifiable();
        
        // discard any bindings with no mappings (unless only one binding)
        List objs;
        if (m_objectBindings.size() > 1) {
            objs = new ArrayList();
            for (Iterator iter = m_keyObjects.iterator(); iter.hasNext();) {
                Object obj = iter.next();
                BindingHolder hold = (BindingHolder)m_objectBindings.get(obj);
                if (hold.getMappingCount() > 0) {
                    objs.add(obj);
                } else {
                    m_objectBindings.put(obj, null);
                    if (s_logger.isDebugEnabled()) {
                        s_logger.debug("Eliminated empty binding associated with " + obj);
                    }
                }
            }
            m_keyObjects.clear();
            m_keyObjects.addAll(objs);
        } else {
            objs = m_keyObjects;
        }
        
        // finish set of namespace definitions for root binding, by checking for those both referenced in one binding
        //  and defined in another (multiple references already handled at time reference was reported), and for
        //  namespaces to be pulled up to root binding
        for (Iterator iter1 = objs.iterator(); iter1.hasNext();) {
            Object obj = iter1.next();
            BindingHolder hold = (BindingHolder)m_objectBindings.get(obj);
            String uri = hold.getNamespace();
            if (uri != null && !MarshallingContext.XML_NAMESPACE.equals(uri) && !m_nsRootUris.contains(uri) &&
                m_referenceUriBinding.get(uri) != null) {
                m_nsRootUris.add(uri);
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Added namespace " + uri + " to set defined in root binding based on reference and usage");
                }
            }
            if (hold.isPullUpNamespaces()) {
                for (Iterator iter2 = hold.getUsedNamespaces().iterator(); iter2.hasNext();) {
                    String useduri = (String)iter2.next();
                    if (useduri != null && !MarshallingContext.XML_NAMESPACE.equals(useduri)) {
                        m_nsRootUris.add(useduri);
                    }
                }
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("Added all namespaces used in binding associated with " + obj + " to set defined in root binding based on abstract mapping usage");
                }
            }
        }
        
        // define the namespace prefixes to be used (unless otherwise specified at individual binding level)
        UniqueNameSet prefset = new UniqueNameSet();
        Map uridfltpref = new HashMap();
        Map urifrcdpref = buildDefaultPrefixes(prefset, uridfltpref);
       
        // check if only single binding defined
        BindingHolder roothold = null;
        BindingElement rootbind;
        List includes = new ArrayList(pregens);
        if (objs.size() == 1) {
            
            // single binding, just write it using supplied name and added namespaces
            roothold = (BindingHolder)m_objectBindings.get(objs.get(0));
            rootbind = roothold.getBinding();
            
        } else {
            
            // first look for existing binding with supplied name
            for (Iterator iter = objs.iterator(); iter.hasNext();) {
                BindingHolder hold = (BindingHolder)m_objectBindings.get(iter.next());
                if (rootname.equals(hold.getFileName())) {
                    roothold = hold;
                    break;
                }
            }
            if (roothold == null) {
                
                // get or create no namespace binding
                roothold = getBinding(null);
                if (roothold == null) {
                    roothold = addBinding(null, null, "", true);
                }
            }
            rootbind = roothold.getBinding();
            
            // add root binding namespace to set declared at root
            if (roothold.isBindingNamespaceUsed()) {
                String uri = roothold.getNamespace();
                if (uri != null && !MarshallingContext.XML_NAMESPACE.equals(uri)) {
                    m_nsRootUris.add(uri);
                }
            }
            
            // set file names and add to list for root binding
            UniqueNameSet nameset = new UniqueNameSet();
            nameset.add(rootname);
            for (Iterator iter = objs.iterator(); iter.hasNext();) {
                BindingHolder holder = (BindingHolder)m_objectBindings.get(iter.next());
                if (holder != roothold) {
                    if (holder.getFileName() == null) {
                        
                        // get last part of namespace URI as file name candidate
                        String bindname;
                        String raw = holder.getNamespace();
                        if (raw == null) {
                            bindname = "nonamespaceBinding";
                        } else {
                            
                            // strip off protocol and any trailing slash
                            raw = raw.replace('\\', '/');
                            int split = raw.indexOf("://");
                            if (split >= 0) {
                                raw = raw.substring(split + 3);
                            }
                            while (raw.endsWith("/")) {
                                raw = raw.substring(0, raw.length()-1);
                            }
                            
                            // strip off host portion if present and followed by path
                            split = raw.indexOf('/');
                            if (split > 0 && raw.substring(0, split).indexOf('.') > 0) {
                                raw = raw.substring(split+1);
                            }
                            
                            // eliminate any invalid characters in name
                            StringBuffer buff = new StringBuffer();
                            int index = 0;
                            char chr = raw.charAt(0);
                            if (isAsciiAlpha(chr)) {
                                buff.append(chr);
                                index = 1;
                            } else {
                                buff.append('_');
                            }
                            boolean toupper = false;
                            while (index < raw.length()) {
                                chr = raw.charAt(index++);
                                if (isAsciiAlphaNum(chr)) {
                                    if (toupper) {
                                        chr = Character.toUpperCase(chr);
                                        toupper = false;
                                    }
                                    buff.append(chr);
                                } else if (chr == '.') {
                                    toupper = true;
                                } else if (chr == ':' || chr == '/') {
                                    buff.append('_');
                                }
                            }
                            buff.append("Binding");
                            bindname = buff.toString();
                        }
                        
                        // ensure uniqueness of the name
                        holder.setFileName(nameset.add(bindname) + ".xml");
                    }
                    
                    // finish construction of this binding
                    holder.finish(Collections.EMPTY_LIST, Collections.EMPTY_LIST, m_nsRootUris, uridfltpref,
                        urifrcdpref);
                    
                    // include within the root binding
                    IncludeElement include = new IncludeElement();
                    include.setIncludePath(holder.getFileName());
                    includes.add(include);
                    
                }
            }
        }
        
        // add required namespace definitions to root binding
        for (Iterator iter = m_nsRootUris.iterator(); iter.hasNext();) {
            String uri = (String)iter.next();
            if (!uri.equals(roothold.getNamespace())) {
                roothold.getUsedNamespaces().add(uri);
            }
        }
        
        // add namespace declarations needed by formats
        for (Iterator iter = m_formats.iterator(); iter.hasNext();) {
            FormatElement format = (FormatElement)iter.next();
            QName qname = format.getQName();
            if (qname != null) {
                String uri = qname.getUri();
                if (uri != null) {
                    roothold.getReferencedNamespaces().add(uri);
                }
            }
        }
        
        // finish building the root binding, using default prefixes where needed
        roothold.finish(m_formats, includes, Collections.EMPTY_SET, uridfltpref, urifrcdpref);
        
        // set the file name on the singleton or root binding
        roothold.setFileName(rootname);
        
        // set the binding name based on the file name
        rootbind.setName(Utility.bindingFromFileName(rootname));
        
        // mark the whole set now finished
        m_finished = true;
        
        // set the target package for code generation
        rootbind.setTargetPackage(trgtpack);
        return roothold;
    }

    /**
     * Validate the constructed bindings. The {@link #configureFiles(String, String, List)} must be called before this
     * method is called, in order to complete the root binding definition.
     * 
     * @param root root binding holder (returned by {@link #configureFiles(String, String, List)})
     * @param dir target directory for writing binding definitions
     * @param vctx validation context to use
     * @return <code>true</code> if valid, <code>false</code> if error
     * @throws IOException
     */
    public boolean validateBindings(BindingHolder root, File dir, ValidationContext vctx)
    throws IOException {
        BindingElement binding = root.getBinding();
        binding.setBaseUrl(dir.toURI().toURL());
        List keys = getKeys();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            BindingHolder holder = (BindingHolder)m_objectBindings.get(iter.next());
            if (holder != null && holder != root) {
                String path = new File(dir, holder.getFileName()).toURI().toURL().toExternalForm();
                if (binding.addIncludePath(path, false)) {
                    binding.addIncludeBinding(path, holder.getBinding());
                }
            }
        }
        vctx.setBindingRoot(binding);
        binding.runValidation(vctx.isLookupSupported(), vctx);
        return vctx.getErrorCount() == 0 && vctx.getFatalCount() == 0;
    }

    /**
     * Write the bindings to supplied destination path. The {@link #configureFiles(String, String, List)} must be called
     * before this method is called, in order to configure the file names and complete the root binding definition.
     * 
     * @param dir target directory for writing binding definitions
     * @throws JiBXException
     * @throws IOException
     */
    public void writeBindings(File dir) throws JiBXException, IOException {
        IBindingFactory fact = BindingDirectory.getFactory("normal", BindingElement.class);
        IMarshallingContext ictx = fact.createMarshallingContext();
        ictx.setIndent(2);
        List keys = getKeys();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            BindingHolder holder = (BindingHolder)m_objectBindings.get(iter.next());
            if (holder != null) {
                File file = new File(dir, holder.getFileName());
                ictx.setOutput(new FileOutputStream(file), null);
                ((IMarshallable)holder.getBinding()).marshal(ictx);
                ictx.getXmlWriter().flush();
            }
        }
    }
}
/*
Copyright (c) 2004-2009, Dennis M. Sosnoski.
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

package org.jibx.binding.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jibx.binding.classes.ClassCache;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallable;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.Utility;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.util.IClass;
import org.jibx.util.StringArray;

/**
 * Model component for <b>binding</b> element.
 *
 * @author Dennis M. Sosnoski
 */
public class BindingElement extends NestingElementBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "add-constructors", "direction",
        "force-classes", "forwards", "name", "package", "track-source",
        "trim-whitespace" }, NestingElementBase.s_allowedAttributes);
    
    //
    // Value set information
    
    public static final int IN_BINDING = 0;
    public static final int OUT_BINDING = 1;
    public static final int BOTH_BINDING = 2;
    
    /*package*/ static final EnumSet s_directionEnum = new EnumSet(IN_BINDING,
        new String[] { "input", "output", "both" });
    
    //
    // Instance data
    
    /** Binding name. */
    private String m_name;
    
    /** Binding direction. */
    private String m_direction;

    /** Input binding flag. */
    private boolean m_inputBinding;

    /** Output binding flag. */
    private boolean m_outputBinding;

    /** Support forward references to IDs flag. */
    private boolean m_forwardReferences;

    /** Generate souce tracking interface flag. */
    private boolean m_trackSource;

    /** Generate souce tracking interface flag. */
    private boolean m_forceClasses;
    
    /** Add default constructors where needed flag. */
    private boolean m_addConstructors;
    
    /** Trim whitespace for simple values (schema compatibility) flag. */
    private boolean m_trimWhitespace;
    
    /** Major version of binding. */
    private int m_majorVersion;
    
    /** Minor version of binding. */
    private int m_minorVersion;

    /** Package for generated context factory. */
    private String m_targetPackage;
    
    /** Precompiled binding flag. */
    private boolean m_precompiled;
    
    /** Base URL for use with relative include paths. */
    private URL m_baseUrl;
    
    /** Set of paths for includes. */
    private final Set m_includePaths;
    
    /** Set of paths for precompiled includes. */
    private final Set m_precompiledPaths;
    
    /** Map from include path to actual binding. */
    private final Map m_includeBindings;
    
    /** List of child elements. */
    private final ArrayList m_children;
    
    /** Set of class names which can be referenced by ID. */
    private Set m_idClassSet;
    
    /** List of namespace declarations to be added on output (lazy create,
     <code>null</code> if none). */
    private ArrayList m_namespaceDeclares;
    
    /**
     * Default constructor.
     */
    public BindingElement() {
        super(BINDING_ELEMENT);
        m_includePaths = new HashSet();
        m_precompiledPaths = new HashSet();
        m_includeBindings = new HashMap();
        m_children = new ArrayList();
        m_forwardReferences = true;
    }
    
    /**
     * Set binding name.
     * 
     * @param name binding definition name
     */
    public void setName(String name) {
        m_name = name;
    }
    
    /**
     * Get binding name.
     * 
     * @return binding definition name
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * Set forward references to IDs be supported in XML.
     *
     * @param forward <code>true</code> if forward references supported,
     * <code>false</code> if not
     */
    public void setForward(boolean forward) {
        m_forwardReferences = forward;
    }
    
    /**
     * Check if forward references to IDs must be supported in XML.
     *
     * @return <code>true</code> if forward references required,
     * <code>false</code> if not
     */
    public boolean isForward() {
        return m_forwardReferences;
    }
    
    /**
     * Set source position tracking for unmarshalling.
     *
     * @param track <code>true</code> if source position tracking enabled,
     * <code>false</code> if not
     */
    public void setTrackSource(boolean track) {
        m_trackSource = track;
    }
    
    /**
     * Check if source position tracking enabled for unmarshalling.
     *
     * @return <code>true</code> if source position tracking enabled,
     * <code>false</code> if not
     */
    public boolean isTrackSource() {
        return m_trackSource;
    }
    
    /**
     * Set force marshaller/unmarshaller class creation for top-level non-base
     * abstract mappings.
     *
     * @param force <code>true</code> if class generation forced,
     * <code>false</code> if not
     */
    public void setForceClasses(boolean force) {
        m_forceClasses = force;
    }
    
    /**
     * Check if marshaller/unmarshaller class creation for top-level non-base
     * abstract mappings is forced.
     *
     * @return <code>true</code> if class generation forced,
     * <code>false</code> if not
     */
    public boolean isForceClasses() {
        return m_forceClasses;
    }
    
    /**
     * Set default constructor generation.
     *
     * @param add <code>true</code> if constructors should be added,
     * <code>false</code> if not
     */
    public void setAddConstructors(boolean add) {
        m_addConstructors = add;
    }
    
    /**
     * Check if default constructor generation is enabled.
     *
     * @return <code>true</code> if default constructor generation enabled,
     * <code>false</code> if not
     */
    public boolean isAddConstructors() {
        return m_addConstructors;
    }
    
    /**
     * Set trim whitespace flag.
     *
     * @param trim <code>true</code> if whitespace should be trimmed from simple
     * values, <code>false</code> if not
     */
    public void setTrimWhitespace(boolean trim) {
        m_trimWhitespace = trim;
    }
    
    /**
     * Check if whitespace should be trimmed from simple values.
     *
     * @return trim whitespace flag
     */
    public boolean isTrimWhitespace() {
        return m_trimWhitespace;
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
     * Set major version number.
     *
     * @param ver major version
     */
    public void setMajorVersion(int ver) {
        m_majorVersion = ver;
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
     * Set minor version number.
     *
     * @param ver minor version
     */
    public void setMinorVersion(int ver) {
        m_minorVersion = ver;
    }

    /**
     * Set package for generated context factory class.
     * 
     * @param pack generated context factory package (<code>null</code> if
     * unspecified)
     */
    public void setTargetPackage(String pack) {
        m_targetPackage = pack;
    }
    
    /**
     * Get package for generated context factory class.
     * 
     * @return package for generated context factory (<code>null</code> if
     * unspecified)
     */
    public String getTargetPackage() {
        return m_targetPackage;
    }
    
    /**
     * Set base URL for relative include paths.
     * 
     * @param base
     */
    public void setBaseUrl(URL base) {
        m_baseUrl = base;
    }
    
    /**
     * Get base URL for relative include paths.
     * 
     * @return base URL
     */
    public URL getBaseUrl() {
        return m_baseUrl;
    }
    
    /**
     * Set the correct direction text. This should be used whenever the
     * individual in and out flags are set, so that modifications are output
     * correctly when a binding is marshalled.
     */
    private void setDirection() {
        int direct = m_inputBinding ? (m_outputBinding ? BOTH_BINDING : IN_BINDING) :
            OUT_BINDING;
        m_direction = s_directionEnum.getName(direct);
    }
    
    /**
     * Set binding component applies for marshalling XML.
     *
     * @param out <code>true</code> if binding supports output,
     * <code>false</code> if not
     */
    public void setOutBinding(boolean out) {
        m_outputBinding = out;
        setDirection();
    }
    
	/**
     * Check if this binding component applies for marshalling XML.
     *
	 * @return <code>true</code> if binding supports output, <code>false</code>
     * if not
	 */
	public boolean isOutBinding() {
		return m_outputBinding;
	}
    
    /**
     * Set binding component applies for unmarshalling XML.
     *
     * @param in <code>true</code> if binding supports input,
     * <code>false</code> if not
     */
    public void setInBinding(boolean in) {
        m_inputBinding = in;
        setDirection();
    }
    
	/**
     * Check if this binding component applies for unmarshalling XML.
     *
	 * @return <code>true</code> if binding supports input, <code>false</code>
     * if not
	 */
	public boolean isInBinding() {
		return m_inputBinding;
	}
	
    
    /**
     * Check if a precompiled binding.
     *
     * @return <code>true</code> if precompiled, <code>false</code> if not
     */
    public boolean isPrecompiled() {
        return m_precompiled;
    }

    /**
     * Set precompiled binding flag.
     *
     * @param precomp
     */
    public void setPrecompiled(boolean precomp) {
        m_precompiled = precomp;
    }

    /**
     * Add include path to set processed.
     *
     * @param path
     * @param precomp precompiled binding flag
     * @return <code>true</code> if new path, <code>false</code> if duplicate
     */
    public boolean addIncludePath(String path, boolean precomp) {
        if (m_includePaths.add(path)) {
            if (precomp || m_precompiled) {
                m_precompiledPaths.add(path);
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Get included binding. If the binding was supplied directly it's just
     * returned; otherwise, it's read from the URL. This method should only be
     * called if {@link #addIncludePath(String, boolean)} returns
     * <code>true</code>, so that each unique included binding is only processed
     * once.
     *
     * @param url binding path
     * @param root binding containing the include
     * @param vctx validation context
     * @return binding
     * @throws IOException
     * @throws JiBXException
     */
    public BindingElement getIncludeBinding(URL url, BindingElement root,
        ValidationContext vctx) throws IOException, JiBXException {
        String path = url.toExternalForm();
        BindingElement bind = (BindingElement)m_includeBindings.get(path);
        if (bind == null) {
            
            // get base name from path
            path = path.replace('\\', '/');
            int split = path.lastIndexOf('/');
            String fname = path;
            if (split >= 0) {
                fname = fname.substring(split+1);
            }
            
            // read stream to create object model
            InputStream is = url.openStream();
            bind = readBinding(is, fname, root,
                m_precompiledPaths.contains(path), vctx);
            
        }
        
        // set binding base, and context from root context
        bind.setBaseUrl(url);
        bind.setDefinitions(root.getDefinitions().getIncludeCopy());
        m_includeBindings.put(path, bind);
        return bind;
    }
    
    /**
     * Get existing included binding.
     *
     * @param url binding path
     * @return binding if it exists, otherwise <code>null</code>
     */
    public BindingElement getExistingIncludeBinding(URL url) {
        return (BindingElement)m_includeBindings.get(url.toExternalForm());
    }
    
    /**
     * Add binding accessible to includes. This allows bindings to be supplied
     * directly, without needing to be parsed from an input document.
     *
     * @param path URL string identifying the binding (virtual path)
     * @param bind
     */
    public void addIncludeBinding(String path, BindingElement bind) {
        if (addIncludePath(path, false)) {
            m_includeBindings.put(path, bind);
        }
    }
    
    /**
     * Add a class defined with a ID value. This is used to track the classes
     * with ID values for validating ID references in the binding. If the
     * binding uses global IDs, the actual ID class is added to the table along
     * with all interfaces implemented by the class and all superclasses, since
     * instances of the ID class can be referenced in any of those forms. If the
     * binding does not use global IDs, only the actual ID class is added, since
     * references must be type-specific.
     * 
     * @param clas information for class with ID value
     */
    public void addIdClass(IClass clas) {
        
        // create the set if not already present
        if (m_idClassSet == null) {
            m_idClassSet = new HashSet();
        }
        
        // add the class if not already present
        if (m_idClassSet.add(clas.getName())) {
            
            // new class, add all interfaces if not previously defined
            String[] inames = clas.getInterfaces();
            for (int i = 0; i < inames.length; i++) {
                m_idClassSet.add(inames[i]);
            }
            while (clas != null && m_idClassSet.add(clas.getName())) {
                clas = clas.getSuperClass();
            }
        }
    }
    
    /**
     * Check if a class can be referenced by ID. This just checks if any classes
     * compatible with the reference type are bound with ID values.
     *
     * @param name fully qualified name of class
     * @return <code>true</code> if class is bound with an ID,
     * <code>false</code> if not
     */
    public boolean isIdClass(String name) {
        if (m_idClassSet == null) {
            return false;
        } else {
            return m_idClassSet.contains(name);
        }
    }
    
    /**
     * Add top-level child element.
     * TODO: should be ElementBase argument, but JiBX doesn't allow yet
     * 
     * @param child element to be added as child of this element
     */
    public void addTopChild(Object child) {
        m_children.add(child);
    }
    
    /**
     * Get list of top-level child elements.
     * 
     * @return list of child elements, or <code>null</code> if none
     */
    public ArrayList topChildren() {
        return m_children;
    }
    
    /**
     * Get iterator for top-level child elements.
     * 
     * @return iterator for child elements
     */
    public Iterator topChildIterator() {
        return m_children.iterator();
    }
    
    /**
     * Add namespace declaration for output when marshalling.
     * 
     * @param prefix namespace prefix (<code>null</code> if none)
     * @param uri namespace URI (non-<code>null</code>)
     */
    public void addNamespaceDecl(String prefix, String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Internal error - namespace URI cannot be null");
        }
        if (m_namespaceDeclares == null) {
            m_namespaceDeclares = new ArrayList();
        }
        m_namespaceDeclares.add(prefix == null ? "" : prefix);
        m_namespaceDeclares.add(uri);
    }
    
    //
    // Overrides of base class methods.

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#hasAttribute()
     */
    public boolean hasAttribute() {
        throw new IllegalStateException
            ("Internal error: method should never be called");
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#hasContent()
     */
    public boolean hasContent() {
        throw new IllegalStateException
            ("Internal error: method should never be called");
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#isOptional()
     */
    public boolean isOptional() {
        throw new IllegalStateException
            ("Internal error: method should never be called");
    }
    
    /**
     * Get default style value for child components. This call is only
     * meaningful after validation.
     * 
     * @return default style value for child components
     */
    public int getDefaultStyle() {
        int style = super.getDefaultStyle();
        if (style < 0) {
            style = NestingAttributes.s_styleEnum.getValue("element");
        }
        return style;
    }
    
    //
    // Marshalling/unmarshalling methods
    
    /**
     * Marshalling hook method to add namespace declarations to &lt;binding>
     * element.
     *
     * @param ictx
     * @throws IOException
     */
    private void preGet(IMarshallingContext ictx) throws IOException {
        if (m_namespaceDeclares != null) {
            
            // set up information for namespace indexes and prefixes
            IXMLWriter writer = ictx.getXmlWriter();
            String[] uris = new String[m_namespaceDeclares.size()/2];
            int[] indexes = new int[uris.length];
            String[] prefs = new String[uris.length];
            int base = writer.getNamespaceCount();
            for (int i = 0; i < uris.length; i++) {
                indexes[i] = base + i;
                prefs[i] = (String)m_namespaceDeclares.get(i*2);
                uris[i] = (String)m_namespaceDeclares.get(i*2+1);
            }
            
            // add the namespace declarations to current element
            writer.pushExtensionNamespaces(uris);
            writer.openNamespaces(indexes, prefs);
            for (int i = 0; i < uris.length; i++) {
                String prefix = prefs[i];
                String name = prefix.length() == 0 ?
                    "xmlns" : "xmlns:" + prefix;
                writer.addAttribute(0, name, uris[i]);
            }
            
        }
    }
    
    //
    // Validation methods
    
    /**
     * Make sure all attributes are defined.
     *
     * @param ictx unmarshalling context
     * @exception JiBXException on unmarshalling error
     */
    private void preSet(IUnmarshallingContext ictx) throws JiBXException {
        
        // validate the attributes defined for this element
        validateAttributes(ictx, s_allowedAttributes);
        
        // get the unmarshal wrapper
        UnmarshalWrapper wrapper =
            (UnmarshalWrapper)ictx.getStackObject(ictx.getStackDepth()-1);
        if (wrapper.getContainingBinding() != null) {
            
            // check attributes not allowed on included binding
            BindingElement root = wrapper.getContainingBinding();
            ValidationContext vctx = wrapper.getValidation();
            UnmarshallingContext uctx = (UnmarshallingContext)ictx;
            for (int i = 0; i < uctx.getAttributeCount(); i++) {
                
                // check if nonamespace attribute is in the allowed set
                String name = uctx.getAttributeName(i);
                if (uctx.getAttributeNamespace(i).length() == 0) {
                    if (s_allowedAttributes.indexOf(name) >= 0) {
                        String value = uctx.getAttributeValue(i);
                        if ("direction".equals(name)) {
                            if (m_precompiled) {
                                boolean compat = true;
                                if (root.m_direction == null ||
                                    "both".equals(root.m_direction)) {
                                    compat = value.equals("both");
                                } else if ("input".equals(root.m_direction)) {
                                    compat = !value.equals("output");
                                } else if ("output".equals(root.m_direction)) {
                                    compat = !value.equals("input");
                                }
                                if (!compat) {
                                    vctx.addError("'direction' value on included precompiled binding is incompatible with root binding",
                                        this);
                                }
                            } else if (!value.equals(root.m_direction)) {
                                vctx.addError("'direction' value on included binding must match the root binding",
                                    this);
                            }
                        } else if ("track-source".equals(name)) {
                            if (!m_precompiled) {
                                boolean flag = Utility.parseBoolean(value);
                                if (root.m_trackSource != flag) {
                                    vctx.addError("'track-source' value on included binding must match the root binding",
                                        this);
                                }
                            }
                        } else if ("value-style".equals(name)) {
                            if (!m_precompiled) {
                                if (!value.equals(root.getStyleName())) {
                                    vctx.addError("'value-style' value on included binding must match the root binding",
                                        this);
                                }
                            }
                        } else if ("force-classes".equals(name)) {
                            if (!m_precompiled) {
                                boolean flag = Utility.parseBoolean(value);
                                if (root.m_forceClasses != flag) {
                                    vctx.addError("'force-classes' value on included binding must match the root binding",
                                        this);
                                }
                            }
                        } else if ("add-constructors".equals(name)) {
                            if (!m_precompiled) {
                                boolean flag = Utility.parseBoolean(value);
                                if (root.m_addConstructors != flag) {
                                    vctx.addError("'add-constructors' value on included binding must match the root binding",
                                        this);
                                }
                            }
                        } else if ("forwards".equals(name)) {
                            if (!m_precompiled) {
                                boolean flag = Utility.parseBoolean(value);
                                if (root.m_forwardReferences != flag) {
                                    vctx.addError("'forwards' value on included binding must match the root binding",
                                        this);
                                }
                            }
                        } else if ("trim-whitespace".equals(name)) {
                            if (!m_precompiled) {
                                boolean flag = Utility.parseBoolean(value);
                                if (root.m_trimWhitespace != flag) {
                                    vctx.addError("'trim-whitespace' value on included binding must match the root binding",
                                        this);
                                }
                            }
                        } else if (!"package".equals(name) && !"name".equals(name)) {
                            vctx.addError("Attribute '" + name + "' not allowed on included binding",
                                this);
                        }
                    }
                }
            }
            
        }
    }
    
    /**
     * Prevalidate all attributes of element in isolation.
     *
     * @param vctx validation context
     */
    public void prevalidate(ValidationContext vctx) {
        
        // set the direction flags
        int index = -1;
        if (m_direction != null) {
            index = s_directionEnum.getValue(m_direction);
            if (index < 0) {
                vctx.addError("Value \"" + m_direction +
                    "\" is not a valid choice for direction");
            }
        } else {
            index = BOTH_BINDING;
        }
        m_inputBinding = index == IN_BINDING || index == BOTH_BINDING;
        m_outputBinding = index == OUT_BINDING || index == BOTH_BINDING;
        
        // check for illegal characters in name
        if (m_name != null) {
            int length = m_name.length();
            for (int i = 0; i < length; i++) {
                if (!Character.isJavaIdentifierPart(m_name.charAt(i))) {
                    vctx.addError("Binding name '" + m_name +
                        " contains invalid characters (only Java identifier part characters allowed)",
                        this);
                    break;
                }
            }
        }
        super.prevalidate(vctx);
    }
    
    private static FormatElement buildFormat(String name, String type,
        boolean use, String sname, String dname, String dflt) {
        FormatElement format = new FormatElement();
        format.setLabel(name);
        format.setTypeName(type);
        format.setDefaultFormat(use);
        format.setSerializerName(sname);
        format.setDeserializerName(dname);
        format.setDefaultText(dflt);
        return format;
    }
    
    /**
     * Define a built-in format. This checks to make sure the type referenced by
     * the format is present in the classpath, since some of the build-in
     * formats apply to classes which are not part of the required runtime.
     *
     * @param format
     * @param dctx
     * @param vctx
     */
    private void defineBaseFormat(FormatElement format, DefinitionContext dctx,
        ValidationContext vctx) {
        if (vctx.getClassInfo(format.getTypeName()) != null) {
            format.prevalidate(vctx);
            format.validate(vctx);
            dctx.addFormat(format, vctx);
        }
    }

    /**
     * Run the actual validation of a binding model. This allows either partial
     * or full validation, with partial validation not requiring access to class
     * files.
     * 
     * @param full run full validation flag (requires access to class files)
     * @param vctx context for controlling validation
     */
    public void runValidation(boolean full, ValidationContext vctx) {
        
        // initially enable both directions for format setup
        m_inputBinding = true;
        m_outputBinding = true;
        
        // create outer definition context
        DefinitionContext dctx = new DefinitionContext(null);
        vctx.setGlobalDefinitions(dctx);
        for (int i = 0; i < BuiltinFormats.s_builtinFormats.length; i++) {
            defineBaseFormat(BuiltinFormats.s_builtinFormats[i], dctx, vctx);
        }
        FormatElement format = buildFormat("char.string", "char", false,
            "org.jibx.runtime.Utility.serializeCharString",
            "org.jibx.runtime.Utility.deserializeCharString", "0");
        format.setDefaultFormat(false);
        format.prevalidate(vctx);
        format.validate(vctx);
        dctx.addFormat(format, vctx);
        NamespaceElement ns = new NamespaceElement();
        ns.setDefaultName("all");
        ns.prevalidate(vctx);
        dctx.addNamespace(ns);
        // TODO: check for errors in basic configuration
        
        // create a definition context for the binding
        setDefinitions(new DefinitionContext(dctx));
        
        // run the actual validation
        vctx.prevalidate(this);
        RegistrationVisitor rvisitor = new RegistrationVisitor(vctx);
        rvisitor.visitTree(this);
        if (full) {
            vctx.validate(this);
        }
    }

    /**
     * Run the actual validation of a binding model. This form of call always
     * does a full validation.
     * 
     * @param vctx context for controlling validation
     */
    public void runValidation(ValidationContext vctx) {
        runValidation(true, vctx);
    }

    /**
     * Read a binding definition (possibly as an include) to construct binding
     * model.
     * 
     * @param is input stream for reading binding
     * @param fname name of input file (<code>null</code> if unknown)
     * @param contain containing binding (<code>null</code> if none)
     * @param precomp precompiled binding flag
     * @param vctx validation context used during unmarshalling
     * @return root of binding definition model
     * @throws JiBXException on error in reading binding
     */
    public static BindingElement readBinding(InputStream is, String fname,
        BindingElement contain, boolean precomp, ValidationContext vctx) throws JiBXException {
        
        // look up the binding factory
        String bindname = precomp ? "precomp" : "normal";
        IBindingFactory bfact =
            BindingDirectory.getFactory(bindname, "org.jibx.binding.model");
        
        // unmarshal document to construct objects
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        uctx.setDocument(is, fname, null);
        uctx.pushObject(new UnmarshalWrapper(vctx, contain));
        BindingElement binding = new BindingElement();
        binding.setPrecompiled(precomp);
        ((IUnmarshallable)binding).unmarshal(uctx);
        uctx.popObject();
        return binding;
    }

    /**
     * Read a binding definition to construct binding model. This method cannot
     * be used for precompiled bindings.
     * 
     * @param is input stream for reading binding
     * @param fname name of input file (<code>null</code> if unknown)
     * @param vctx validation context used during unmarshalling
     * @return root of binding definition model
     * @throws JiBXException on error in reading binding
     */
    public static BindingElement readBinding(InputStream is, String fname,
        ValidationContext vctx) throws JiBXException {
        return readBinding(is, fname, null, false, vctx);
    }
    
    /**
     * Validate a binding definition. This method cannot be used for precompiled
     * bindings.
     * 
     * @param name binding definition name
     * @param path binding definition URL
     * @param is input stream for reading binding
     * @param vctx validation context to record problems
     * @return root of binding definition model, or <code>null</code> if error
     * in unmarshalling
     * @throws JiBXException on error in binding XML structure
     */
    public static BindingElement validateBinding(String name, URL path,
        InputStream is, ValidationContext vctx) throws JiBXException {
        
        // construct object model for binding
        BindingElement binding = readBinding(is, name, vctx);
        binding.setBaseUrl(path);
        vctx.setBindingRoot(binding);
        
        // validate the binding definition
        binding.runValidation(vctx);
        
        // list validation errors
        ArrayList probs = vctx.getProblems();
        if (probs.size() > 0) {
            for (int i = 0; i < probs.size(); i++) {
                ValidationProblem prob = (ValidationProblem)probs.get(i);
                System.out.print(prob.getSeverity() >=
                    ValidationProblem.ERROR_LEVEL ? "Error: " : "Warning: ");
                System.out.println(prob.getDescription());
            }
        }
        return binding;
    }

    /**
     * Create a default validation context.
     * 
     * @return new validation context
     */
    public static ValidationContext newValidationContext() {
        return new ValidationContext(new ClassCache.ClassCacheLocator());
    }
    
/*    // test runner
    // This code only used in testing, to roundtrip binding definitions
    public static void test(String ipath, String opath, ValidationContext vctx)
        throws Exception {
        
        // validate the binding definition
        FileInputStream is = new FileInputStream(ipath);
        URL url = new URL("file://" + ipath);
        BindingElement binding = validateBinding(ipath, url, is, vctx);
        
        // marshal back out for comparison purposes
        IBindingFactory bfact =
            BindingDirectory.getFactory(BindingElement.class);
        IMarshallingContext mctx = bfact.createMarshallingContext();
        mctx.setIndent(2);
        mctx.marshalDocument(binding, null, null, new FileOutputStream(opath));
        System.out.println("Wrote output binding " + opath);
        
        // compare input document with output document
        DocumentComparator comp = new DocumentComparator(System.err);
        boolean match = comp.compare(new FileReader(ipath),
            new FileReader(opath));
        if (!match) {
            System.err.println("Mismatch from input " + ipath +
                " to output " + opath);
        }
    }
    
    // test runner
    public static void main(String[] args) throws Exception {
        
        // configure class loading
        String[] paths = new String[] { "." };
        ClassCache.setPaths(paths);
        ClassFile.setPaths(paths);
        ValidationContext vctx = newValidationContext();
        
        // process all bindings listed on command line
        for (int i = 0; i < args.length; i++) {
            try {
                String ipath = args[i];
                int split = ipath.lastIndexOf(File.separatorChar);
                String opath = "x" + ipath.substring(split+1);
                test(ipath, opath, vctx);
            } catch (Exception e) {
                System.err.println("Error handling binding " + args[i]);
                e.printStackTrace();
            }
        }
    }   */
    
    /**
     * Inner class as wrapper for binding element on unmarshalling. This
     * provides a handle for passing the validation context, allowing elements
     * to check for problems during unmarshalling.
     */
    public static class UnmarshalWrapper
    {
        private final ValidationContext m_validationContext;
        private final BindingElement m_containingBinding;
        
        protected UnmarshalWrapper(ValidationContext vctx,
            BindingElement contain) {
            m_validationContext = vctx;
            m_containingBinding = contain;
        }
        
        public ValidationContext getValidation() {
            return m_validationContext;
        }
        
        public BindingElement getContainingBinding() {
            return m_containingBinding;
        }
    }
}
/*
Copyright (c) 2004-2007, Dennis M. Sosnoski
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.util.IClass;
import org.jibx.util.IClassItem;
import org.jibx.util.StringArray;

/**
 * Model component for <b>collection</b> element of binding definition.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */
 
public class CollectionElement extends StructureElementBase
{
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes =
        new StringArray(new String[] { "add-method", "item-type", "iter-method",
        "load-method", "size-method", "store-method" },
        StructureElementBase.s_allowedAttributes);
    
    /** Load method name. */
    private String m_loadMethodName;
    
    /** Size method name. */
    private String m_sizeMethodName;
    
    /** Store method name. */
    private String m_storeMethodName;
    
    /** Add method name. */
    private String m_addMethodName;
    
    /** Iterator method name. */
    private String m_iterMethodName;
    
    /** Item type name. */
    private String m_itemTypeName;
    
    /** Load method information. */
    private IClassItem m_loadMethodItem;
    
    /** Size method information. */
    private IClassItem m_sizeMethodItem;
    
    /** Store method information. */
    private IClassItem m_storeMethodItem;
    
    /** Add method information. */
    private IClassItem m_addMethodItem;
    
    /** Iterator method information. */
    private IClassItem m_iterMethodItem;
    
    /** Item type information. */
    private IClass m_itemTypeClass;
    
	/**
	 * Default constructor.
	 */
	public CollectionElement() {
        super(COLLECTION_ELEMENT);
    }

    /**
     * Get item type name.
     * 
     * @return item type name (or <code>null</code> if none)
     */
    public String getItemTypeName() {
        return m_itemTypeName;
    }
    
    /**
     * Set item type name.
     * 
     * @param type item type name (or <code>null</code> if none)
     */
    public void setItemTypeName(String type) {
        m_itemTypeName = type;
    }

    /**
     * Get item type information. This call is only meaningful after
     * validation.
     * 
     * @return item type information
     */
    public IClass getItemTypeClass() {
        return m_itemTypeClass;
    }
    
    /**
     * Get add method name.
     * 
     * @return add method name (or <code>null</code> if none)
     */
    public String getAddMethodName() {
        return m_addMethodName;
    }
    
    /**
     * Set add method name.
     * 
     * @param name add method name (or <code>null</code> if none)
     */
    public void setAddMethodName(String name) {
        m_addMethodName = name;
    }

    /**
     * Get add method information. This call is only meaningful after
     * validation.
     * 
     * @return add method information (or <code>null</code> if none)
     */
    public IClassItem getAddMethodItem() {
        return m_addMethodItem;
    }
    
    /**
     * Get iterator method name.
     * 
     * @return iterator method name (or <code>null</code> if none)
     */
    public String getIterMethodName() {
        return m_iterMethodName;
    }
    
    /**
     * Set iterator method name.
     * 
     * @param name iterator method name (or <code>null</code> if none)
     */
    public void setIterMethodName(String name) {
        m_iterMethodName = name;
    }
    
    /**
     * Get iterator method information. This call is only meaningful after
     * validation.
     * 
     * @return iterator method information (or <code>null</code> if none)
     */
    public IClassItem getIterMethodItem() {
        return m_iterMethodItem;
    }
    
    /**
     * Get load method name.
     * 
     * @return load method name (or <code>null</code> if none)
     */
    public String getLoadMethodName() {
        return m_loadMethodName;
    }
    
    /**
     * Set load method name.
     * 
     * @param name load method name (or <code>null</code> if none)
     */
    public void setLoadMethodName(String name) {
        m_loadMethodName = name;
    }
    
    /**
     * Get load method information. This call is only meaningful after
     * validation.
     * 
     * @return load method information (or <code>null</code> if none)
     */
    public IClassItem getLoadMethodItem() {
        return m_loadMethodItem;
    }
    
    /**
     * Get size method name.
     * 
     * @return size method name (or <code>null</code> if none)
     */
    public String getSizeMethodName() {
        return m_sizeMethodName;
    }
    
    /**
     * Set size method name.
     * 
     * @param name size method name (or <code>null</code> if none)
     */
    public void setSizeMethodName(String name) {
        m_sizeMethodName = name;
    }
    
    /**
     * Get size method information. This call is only meaningful after
     * validation.
     * 
     * @return size method information (or <code>null</code> if none)
     */
    public IClassItem getSizeMethodItem() {
        return m_sizeMethodItem;
    }
    
    /**
     * Get store method name.
     * 
     * @return store method name (or <code>null</code> if none)
     */
    public String getStoreMethodName() {
        return m_storeMethodName;
    }
    
    /**
     * Set store method name.
     * 
     * @param name store method name (or <code>null</code> if none)
     */
    public void setStoreMethodName(String name) {
        m_storeMethodName = name;
    }
    
    /**
     * Get store method information. This call is only meaningful after
     * validation.
     * 
     * @return store method information (or <code>null</code> if none)
     */
    public IClassItem getStoreMethodItem() {
        return m_storeMethodItem;
    }
    
    //
    // Overrides of base class methods

    /**
     * Set ID property. This is never supported for an object coming from a
     * collection.
     *
     * @param child child defining the ID property
     * @return <code>true</code> if successful, <code>false</code> if ID
     * already defined
     */
    public boolean setIdChild(IComponent child) {
        throw new IllegalStateException
            ("Internal error: method should never be called");
    }
    
    /**
     * Check for object present. Always <code>true</code> for collection.
     *
     * @return <code>true</code>
     */
    public boolean hasObject() {
        return true;
    }
    
    /**
     * Check for attribute definition. Always <code>false</code> for collection.
     *
     * @return <code>false</code>
     */
    public boolean hasAttribute() {
        return false;
    }

    /**
     * Check for content definition. Always <code>true</code> for collection.
     *
     * @return <code>true</code>
     */
    public boolean hasContent() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jibx.binding.model.ContainerElementBase#getChildObjectType()
     */
    public IClass getChildObjectType() {
        return getItemTypeClass();
    }
    
    //
    // Validation methods

    /**
     * Make sure all attributes are defined.
     *
     * @param uctx unmarshalling context
     * @exception JiBXException on unmarshalling error
     */
    private void preSet(IUnmarshallingContext uctx) throws JiBXException {
        validateAttributes(uctx, s_allowedAttributes);
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#prevalidate(org.jibx.binding.model.ValidationContext)
     */
    public void prevalidate(ValidationContext vctx) {
        
        // first process attributes and check for errors
        super.prevalidate(vctx);
        if (!vctx.isSkipped(this)) {
            
            // check for ignored attributes
            if (isAllowRepeats()) {
                vctx.addWarning("'allow-repeats' attribute ignored on collection");
            }
            if (isChoice()) {
                vctx.addWarning("'choice' attribute ignored on collection");
            }
            
            // get the actual collection type and item type
            IClass clas = getType();
            if (clas == null) {
                clas = vctx.getContextObject().getObjectType();
            }
            String tname = m_itemTypeName;
            if (tname == null) {
                String ctype = clas.getName();
                if (ctype.endsWith("[]")) {
                    tname = ctype.substring(0, ctype.length()-2);
                } else {
                    tname = "java.lang.Object";
                }
            } 
            m_itemTypeClass = vctx.getClassInfo(tname);
            if (m_itemTypeClass == null) {
                vctx.addFatal("Can't find class " + tname);
            }
            
            // handle input and output bindings separately
            if (vctx.isInBinding()) {
                
                // check store techniques
                String sname = m_storeMethodName;
                String aname = m_addMethodName;
                if (sname != null && aname != null) {
                    vctx.addWarning("Both store-method and add-method supplied; using add-method");
                    sname = null;
                }
                if (vctx.isLookupSupported()) {
                    
                    // set defaults based on collection type if needed
                    if (sname == null && aname == null) {
                        if (clas.isSuperclass("java.util.ArrayList") ||
                            clas.isSuperclass("java.util.Vector") ||
                            clas.isImplements("Ljava/util/Collection;")) {
                            aname = "add";
                        } else if (!clas.getName().endsWith("[]")) {
                            vctx.addError("Need store-method or add-method for input binding");
                        }
                    }
                    
                    // find the actual method information
                    if (sname != null) {
                        m_storeMethodItem = clas.getBestMethod(sname,
                            null, new String[] { "int", tname });
                        if (m_storeMethodItem == null) {
                            vctx.addError("store-method " + sname +
                                " not found in class " + clas.getName());
                        }
                    }
                    if (aname != null) {
                        m_addMethodItem = clas.getBestMethod(aname,
                            null, new String[] { tname });
                        if (m_addMethodItem == null) {
                            vctx.addError("add-method " + aname +
                                " not found in class " + clas.getName());
                        }
                    }
                }
                
            }
            if (vctx.isOutBinding()) {
                
                // precheck load techniques
                String lname = m_loadMethodName;
                String sname = m_sizeMethodName;
                String iname = m_iterMethodName;
                if (lname == null) {
                    if (sname != null) {
                        vctx.addWarning("size-method requires load-method; ignoring supplied size-method");
                        sname = null;
                    }
                } else {
                    if (sname == null) {
                        vctx.addWarning("load-method requires size-method; ignoring supplied load-method");
                        lname = null;
                    } else {
                        if (iname != null) {
                            vctx.addWarning("Both load-method and  iter-method supplied; using load-method");
                            iname = null;
                        }
                    }
                }
                if (vctx.isLookupSupported()) {
                    
                    // set defaults based on collection type if needed
                    if (lname == null && iname == null) {
                        if (clas.isSuperclass("java.util.ArrayList") ||
                            clas.isSuperclass("java.util.Vector")) {
                            lname = "get";
                            sname = "size";
                        } else if (clas.isImplements("Ljava/util/Collection;")) {
                            iname = "iterator";
                        }
                    }
                    
                    // postcheck load techniques with defaults set
                    if (lname == null) {
                        if (iname == null && !clas.getName().endsWith("[]")) {
                            vctx.addError("Need load-method and size-method, or iter-method, for output binding");
                        }
                    } else {
                        if (sname == null && iname == null) {
                            vctx.addError("Need load-method and size-method, or iter-method, for output binding");
                        }
                    }
                    
                    // find the actual method information
                    if (lname != null) {
                        m_loadMethodItem = clas.getBestMethod(lname,
                            tname, new String[] { "int" });
                        if (m_loadMethodItem == null) {
                            vctx.addError("load-method " + lname +
                                " not found in class " + clas.getName());
                        }
                    }
                    if (iname != null) {
                        m_iterMethodItem = clas.getBestMethod(iname,
                            "java.util.Iterator", new String[0]);
                        if (m_iterMethodItem == null) {
                            vctx.addError("iter-method " + iname +
                                " not found in class " + clas.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Check that child components are of types compatible with the collection
     * item-type. This method may call itself recursively to process the
     * children of child components which do not themselves set a type. The
     * result is used for recursive checking to detect conditions where an
     * inner structure defines a type but an outer one does not (which causes
     * errors in the current code generation).
     * 
     * @param vctx validation context
     * @param type collection item type
     * @param children list of child components to be checked
     * @return <code>true</code> if only child is a &lt;value> element with
     * type, <code>false</code> if not
     */
    private boolean checkCollectionChildren(ValidationContext vctx,
        IClass type, ArrayList children) {
        boolean valonly = children.size() == 1;
        for (int i = 0; i < children.size(); i++) {
            ElementBase child = (ElementBase)children.get(i);
            if (!vctx.isSkipped(child)) {
                
                // track whether children of this child must be type-checked
                boolean expand = true;
                valonly = valonly && (child instanceof ValueElement);
                if (child instanceof IComponent) {
                    
                    // first make sure a name is present
                    IComponent comp = (IComponent)child;
                    if (vctx.isInBinding() && !comp.hasName() &&
                        comp instanceof ValueElement) {
                        vctx.addFatal("<value> elements within a collection must define element name for unmarshalling", comp);
                    }
                    
                    // find the type associated with this component (if any)
                    IClass ctype = comp.getType();
                    expand = false;
                    if (comp instanceof ContainerElementBase) {
                        ContainerElementBase contain = (ContainerElementBase)comp;
                        if (contain.hasObject()) {
                            ctype = contain.getObjectType();
                        } else {
                            expand = true;
                        }
                    }
                    
                    // see if a type was found (no need to look at children)
                    if (!expand) {
                        
                        // verify that type is compatible with collection
                        if (!ctype.isAssignable(type)) {
                            vctx.addFatal("References to collection items must use compatible types: " +
                                ctype.getName() + " cannot be used as " +
                                type.getName(), child);
                        }
                    }
                }
                
                // recurse on children if necessary for type-checking
                if (expand && child instanceof NestingElementBase) {
                    NestingElementBase nest = (NestingElementBase)child;
                    if (nest.children().size() > 0) {
                        
                        // type check child definitions
                        boolean valchild = checkCollectionChildren(vctx, type,
                            ((NestingElementBase)child).children());
                        
                        // now check for structure element with no type
                        if (!valchild && child instanceof StructureElement) {
                            vctx.addError("Type must be specified on outermost <structure> element within collection", child);
                        }
                    }
                }
            }
        }
        return valonly;
    }
    
    /**
     * Check children of unordered collection for consistency. In an input
     * binding each child element must define a unique qualified name. In an
     * output binding each child element must define a unique type or supply
     * a test method to allow checking when that element should be generated for
     * an object.
     * 
     * @param vctx validation context
     * @param children list of child components
     */
    private void checkUnorderedChildren(ValidationContext vctx,
        ArrayList children) {
        HashMap typemap = new HashMap();
        HashMap namemap = new HashMap();
        for (int i = 0; i < children.size(); i++) {
            ElementBase child = (ElementBase)children.get(i);
            if (child instanceof IComponent && !vctx.isSkipped(child)) {
                IComponent comp = (IComponent)child;
                if (vctx.isInBinding()) {
                    
                    // names must be distinct in input binding
                    String name = comp.getName();
                    String uri = comp.getUri();
                    if (uri == null) {
                        uri = "";
                    }
                    Object value = namemap.get(name);
                    if (value == null) {
                        
                        // first instance of name, store directly
                        namemap.put(name, comp);
                        
                    } else if (value instanceof HashMap) {
                        
                        // multiple instances already found, match on URI
                        HashMap urimap = (HashMap)value;
                        if (urimap.get(uri) != null) {
                            vctx.addError("Duplicate names are not allowed in unordered collection", comp);
                        } else {
                            urimap.put(uri, comp);
                        }
                        
                    } else {
                        
                        // duplicate name, check URI
                        IComponent match = (IComponent)value;
                        if ((uri == null && match.getUri() == null) ||
                            (uri != null && uri.equals(match.getUri()))) {
                            vctx.addError("Duplicate names are not allowed in unordered collection", comp);
                        } else {
                            
                            // multiple namespaces for same name, use map
                            HashMap urimap = new HashMap();
                            urimap.put(uri, comp);
                            String muri = match.getUri();
                            if (muri == null) {
                                muri = "";
                            }
                            urimap.put(muri, match);
                            namemap.put(name, urimap);
                        }
                        
                    }
                }
                if (vctx.isOutBinding()) {
                    
                    // just accumulate lists of each type in this loop
                    String type = comp.getType().getName();
                    Object value = typemap.get(type);
                    if (value == null) {
                        typemap.put(type, comp);
                    } else if (value instanceof ArrayList) {
                        ArrayList types = (ArrayList)value;
                        types.add(comp);
                    } else {
                        ArrayList types = new ArrayList();
                        types.add(value);
                        types.add(comp);
                        typemap.put(type, types);
                    }
                }
            }
        }
        
        // check for duplicate type usage in output binding
        for (Iterator iter = typemap.values().iterator(); iter.hasNext();) {
            Object value = iter.next();
            if (value instanceof ArrayList) {
                
                // multiple instances of type, make sure we can distinguish
                ArrayList types = (ArrayList)value;
                for (int i = 0; i < types.size(); i++) {
                    Object child = types.get(i);
                    if (child instanceof ValueElement) {
                        ValueElement vel = (ValueElement)child;
                        if (vel.getTest() == null) {
                            vctx.addError("test-method needed for multiple instances of same type in unordered collection", vel);
                        }
                    } else if (child instanceof StructureElementBase) {
                        StructureElementBase sel = (StructureElementBase)child;
                        if (sel.getTest() == null) {
                            vctx.addError("test-method needed for multiple instances of same type in unordered collection", sel);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Check children of ordered collection for consistency. In an input binding
     * each child element must use a different qualified name from the preceding
     * child element. In an output binding each child element must define a
     * different type from the preceding child element, or the preceding child
     * element must supply a test method to allow checking when that element
     * should be generated for an object.
     * 
     * @param vctx validation context
     * @param children list of child components
     */
    private void checkOrderedChildren(ValidationContext vctx,
        ArrayList children) {
        IComponent prior = null;
        for (int i = 0; i < children.size(); i++) {
            ElementBase child = (ElementBase)children.get(i);
            if (child instanceof IComponent && !vctx.isSkipped(child)) {
                IComponent comp = (IComponent)child;
                if (prior == null) {
                    prior = comp;
                } else {
                    if (vctx.isInBinding()) {
                        
                        // make sure names are different
                        String uri = comp.getUri();
                        String cname = comp.getName();
                        String pname = prior.getName();
                        if (cname != null && pname != null &&
                            cname.equals(pname) &&
                            ((uri == null && prior.getUri() == null) ||
                            (uri != null && uri.equals(prior.getUri())))) {
                            vctx.addError("Successive elements of collection cannot use duplicate names for unmarshalling", comp);
                        }
                    }
                    if (vctx.isOutBinding()) {
                        
                        // make sure types differ or test method supplied
                        IClass type = comp.getType();
                        if (type.isAssignable(prior.getType())) {
                            IClassItem test = null;
                            if (prior instanceof ValueElement) {
                                test = ((ValueElement)prior).getTest();
                            } else if (prior instanceof StructureElementBase) {
                                test = ((StructureElementBase)prior).getTest();
                            }
                            if (test == null) {
                                vctx.addError("Collection component must specify a test-method to distinguish from next component of compatible type for marshalling", prior);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.jibx.binding.model.ElementBase#validate(org.jibx.binding.model.ValidationContext)
     */
    public void validate(ValidationContext vctx) {
        
        // call base class method first
        super.validate(vctx);
        
        // check for way to determine if named collection present on output
        if (vctx.isOutBinding() && hasName() && !hasProperty() &&
            isOptional() &&  getTest() == null &&
            !(vctx.getParentContainer() instanceof CollectionElement)) {
            vctx.addError
                ("Need test method for optional collection output element");
        }
        
        // TODO: this should really check that item-type on an empty collection
        //  specifies a concrete mapping
        
        // make sure only element components are present
        ArrayList children = children();
        for (int i = 0; i < children.size(); i++) {
            IComponent child = (IComponent)children.get(i);
            if (child.hasAttribute()) {
                vctx.addFatal
                    ("Attributes not allowed as child components of collection",
                    child);
            }
        }
        
        // check each child component
        checkCollectionChildren(vctx, m_itemTypeClass, children);
        
        // make sure child components can be distinguished
        if (children.size() > 1) {
            if (isOrdered()) {
                checkOrderedChildren(vctx, children);
            } else {
                checkUnorderedChildren(vctx, children);
            }
        }
    }
}
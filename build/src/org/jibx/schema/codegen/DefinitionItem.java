/*
 * Copyright (c) 2007-2010, Dennis M. Sosnoski. All rights reserved.
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

package org.jibx.schema.codegen;

import org.apache.log4j.Logger;
import org.jibx.runtime.QName;
import org.jibx.schema.INamed;
import org.jibx.schema.IReference;
import org.jibx.schema.SchemaUtils;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.AttributeGroupElement;
import org.jibx.schema.elements.GroupElement;

/**
 * Information for a global definition.
 */
public class DefinitionItem extends GroupItem
{
    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(DefinitionItem.class.getName());
    
    /** Number of references to this definition. */
    private int m_referenceCount;
    
    /** Inlining not allowed flag. */
    private boolean m_inlineBlocked;
    
    /** Checked flag used by the code generation handling to track which definitions have already been processed. */
    private boolean m_checked;
    
    /** Tracking flag for reference seen, used during checking for inline to detect circular definitions. */
    private boolean m_referenced;
    
    /** Flag for definition structure classified. */
    private boolean m_classified;
    
    /** Type-isomorphic element flag. */
    private boolean m_typeIsomorphic;
    
    /** Qualified name for definition in binding (lazy create, <code>null</code> if not yet set). */
    private QName m_qname;
    
    /**
     * Constructor for new top-level structure. Child structures should always be created using the containing
     * structure's {@link #addGroup(AnnotatedBase)} method.
     * 
     * @param comp schema component
     */
    public DefinitionItem(AnnotatedBase comp) {
        super(comp, null);
    }
    
    /**
     * Constructor from group. This supports replacing an embedded group with a definition, as needed when an embedded
     * group is used in multiple locations and cannot be inlined.
     * 
     * @param group
     */
    DefinitionItem(GroupItem group) {
        super(group, null, null);
    }

    /**
     * Get the number of references to this definition.
     *
     * @return count
     */
    public int getReferenceCount() {
        return m_referenceCount;
    }

    /**
     * Count a reference to this definition.
     */
    public void countReference() {
        m_referenceCount++;
    }

    /**
     * Check if inlining is blocked (due to non-singleton references).
     *
     * @return blocked
     */
    public boolean isInlineBlocked() {
        return m_inlineBlocked;
    }

    /**
     * Set inlining blocked flag.
     *
     * @param blocked
     */
    public void setInlineBlocked(boolean blocked) {
        if (!blocked && isPregenerated()) {
            throw new IllegalStateException("Internal error - inlining forbidden for pregenerated definition");
        }
        m_inlineBlocked = blocked;
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Setting inlining blocked " + blocked + " for " +
                SchemaUtils.describeComponent(getSchemaComponent()));
        }
    }

    /**
     * Check if definition has been processed.
     *
     * @return checked
     */
    public boolean isChecked() {
        return m_checked;
    }

    /**
     * Set definition has been processed flag.
     *
     * @param checked
     */
    public void setChecked(boolean checked) {
        m_checked = checked;
    }

    /**
     * Check if definition has been referenced during inline checking.
     *
     * @return referenced
     */
    public boolean isReferenced() {
        return m_referenced;
    }

    /**
     * Set definition has been referenced during inline checking flag.
     *
     * @param refed
     */
    public void setReferenced(boolean refed) {
        m_referenced = refed;
    }

    /**
     * Check if this is a type-isomorphic element definition. Type-isomorphic elements use the same generation class as
     * the referenced type, but with a separate mapping definition.
     * 
     * @return type-isomorphic
     */
    public boolean isTypeIsomorphic() {
        return m_typeIsomorphic;
    }

    /**
     * Set the type-isomorphic element definition flag.
     * 
     * @param iso type-isomorphic flag
     */
    public void setTypeIsomorphic(boolean iso) {
        m_typeIsomorphic = iso;
    }

    /**
     * Check if definition has been pregenerated.
     *
     * @return pregenerated
     */
    public boolean isPregenerated() {
        TypeData data = super.getGenerateClass();
        return data != null && data.isPregenerated();
    }
    
    /**
     * Check if this definition has a class directly assigned for code generation.
     *
     * @return <code>true</code> if class directly assigned, <code>false</code> if not
     */
    public boolean hasDirectGenerateClass() {
        return super.getGenerateClass() != null;
    }
    
    /**
     * Get information for class to be generated. This override of the base class implementation checks for the case of
     * a definition which has been inlined, as occurs when a global element definition is the only use of a global type
     * definition. If no generate class is available, this throws an exception.
     *
     * @return class
     */
    public TypeData getGenerateClass() {
        TypeData data = super.getGenerateClass();
        if (data == null) {
            if (isInline() || isTypeIsomorphic()) {
                if (getChildCount() == 1) {
                    Item child = getFirstChild();
                    if (child instanceof ReferenceItem) {
                        return ((ReferenceItem)child).getDefinition().getGenerateClass();
                    } else {
                        throw new IllegalStateException("Internal error - no generate class for definition with non-reference child");
                    }
                } else {
                    throw new IllegalStateException("Internal error - no generate class for definition with multiple children");
                }
            } else {
                throw new IllegalStateException("Internal error - no generate class for non-inlined definition");
            }
        } else {
            return data;
        }
    }

    /**
     * Get qualified name for definition in binding.
     *
     * @return qname
     */
    public QName getQName() {
        if (m_qname == null) {
            AnnotatedBase comp = getSchemaComponent();
            QName qname = ((INamed)comp).getQName();
            if (qname == null && comp instanceof IReference) {
                qname = ((IReference)comp).getRef();
            }
            if (comp instanceof AttributeGroupElement) {
                qname = new QName(qname.getUri(), qname.getName() + "-AttributeGroup");
            } else if (comp instanceof GroupElement) {
                qname = new QName(qname.getUri(), qname.getName() + "-Group");
            }
            m_qname = qname;
        }
        return m_qname;
    }

    /**
     * Classify the content of this item as attribute, element, and/or character data content. For a definition item,
     * this checks if the classification has already been done, and if not flags it done and invokes the superclass
     * handling.
     */
    public void classifyContent() {
        if (!m_classified) {
            m_classified = true;
            super.classifyContent();
        }
    }
    
    /**
     * Build a description of the item, including all nested items.
     *
     * @param depth current nesting depth
     * @param classified include classification details flag
     * @return description
     */
    protected String describe(int depth, boolean classified) {
        StringBuffer buff = new StringBuffer(depth + 50);
        buff.append(leadString(depth));
        if (isInline()) {
            buff.append("inlined ");
        }
        if (isEnumeration()) {
            buff.append("enumeration ");
        }
        buff.append("definition with ");
        buff.append(m_referenceCount);
        buff.append(" references");
        if (m_inlineBlocked) {
            buff.append(" (inlining blocked)");
        }
        buff.append(", and class name ");
        buff.append(getClassName());
        if (m_classified) {
            buff.append(isAllOptional() ? " (all items optional)" : " (not all items optional)");
        }
        buff.append(": ");
        buff.append(SchemaUtils.describeComponent(getSchemaComponent()));
        buff.append('\n');
        buff.append(nestedString(depth, classified));
        return buff.toString();
    }
    
    /**
     * Build a description of the definition, including all nested items.
     *
     * @return description
     */
    protected String describe() {
        return describe(0, m_classified);
    }
}
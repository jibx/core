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

package org.jibx.schema.codegen;

import org.jibx.schema.SchemaUtils;
import org.jibx.schema.elements.AnnotatedBase;

/**
 * Information for an xs:any item to be included in code generated from schema.
 * 
 * @author Dennis M. Sosnoski
 */
public class AnyItem extends Item
{
    /**
     * Copy constructor. This creates a copy with a new parent.
     * 
     * @param original
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent
     */
    private AnyItem(AnyItem original, Item ref, GroupItem parent) {
        super(original, ref, original.getComponentExtension(), parent);
    }
    
    /**
     * Constructor.
     * 
     * @param comp schema component extension
     * @param parent containing structure (<code>null</code> if a top-level structure)
     */
    /*package*/ AnyItem(AnnotatedBase comp, GroupItem parent) {
        super(comp, parent);
    }

    /**
     * Copy the item under a different parent.
     *
     * @param ref reference (for overrides to copy; <code>null</code> if none)
     * @param parent
     * @return copy
     */
    protected Item copy(Item ref, GroupItem parent) {
        return new AnyItem(this, ref, parent);
    }
    
    /**
     * Build a description of the item.
     *
     * @param depth current nesting depth
     * @param classified include classification details flag
     * @return description
     */
    protected String describe(int depth, boolean classified) {
        StringBuffer buff = new StringBuffer(depth + 50);
        buff.append(leadString(depth));
        buff.append("any ");
        buff.append(" with value name ");
        buff.append(getName());
        buff.append(": ");
        buff.append(SchemaUtils.describeComponent(getSchemaComponent()));
        buff.append('\n');
        return buff.toString();
    }
}
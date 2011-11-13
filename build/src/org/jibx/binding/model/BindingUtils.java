/*
 * Copyright (c) 2010-2011, Dennis M. Sosnoski. All rights reserved.
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

import java.util.Map;

import org.jibx.runtime.QName;
import org.jibx.util.DummyClassLocator;

/**
 * Utility methods to support working with binding definitions.
 * 
 * @author Dennis M. Sosnoski
 */
public class BindingUtils
{
    /**
     * Find all mapping definitions representing the equivalent of schema type and element definitions. The passed-in
     * maps should initially be empty, and will be populated on return.
     * 
     * @param binding root binding definition
     * @param types map from qualified name to mapping element for global complex type-equivalent
     * @param elems map from qualified name to mapping element for global element
     * @param formats map from qualified name to format definition for global simple type-equivalent
     */
    public static void getDefinitions(BindingElement binding, final Map types, final Map elems, final Map formats) {
        TreeContext ctx = new TreeContext(new DummyClassLocator());
        ModelVisitor visitor = new ModelVisitor() {

            public boolean visit(MappingElement mapping) {
                QName qname = mapping.getTypeQName();
                if (qname != null) {
                    types.put(qname, mapping);
                }
                String name = mapping.getName();
                if (name != null) {
                    qname = new QName(mapping.getNamespace().getUri(), name);
                    elems.put(qname, mapping);
                }
                return false;
            }
            
            public boolean visit(FormatElement format) {
                QName qname = format.getQName();
                if (qname != null) {
                    formats.put(qname, formats);
                }
                return false;
            }
            
        };
        ctx.tourTree(binding, visitor);
    }
}
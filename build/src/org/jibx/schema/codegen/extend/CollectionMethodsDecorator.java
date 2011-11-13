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

package org.jibx.schema.codegen.extend;

import java.util.Iterator;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.jibx.binding.model.ElementBase;
import org.jibx.schema.codegen.IClassHolder;
import org.jibx.schema.codegen.NameUtils;
import org.jibx.util.NameUtilities;

/**
 * Code generation decorator which adds <code>sizeXXX()</code>, <code>addXXX(YYY)</code> <code>getXXX(int)</code>, and
 * <code>clearXXX()</code> methods for each collection value using a <code>java.util.List</code> representation. In a
 * change from the original version of this class, the form of the name used for the <code>sizeXXX()</code> and
 * <code>clearXXX()</code> methods is based on the supplied <code>getXXX()</code> method, which normally uses a plural
 * form of the name.
 */
public class CollectionMethodsDecorator implements ClassDecorator
{
    /** Text for template class. */
    private static final String s_classText = "class Gorph { java.util.List $1; " +
        "/** Get the number of $0 items.\n * @return count\n */\npublic int size$5() { return $1.size(); }" +
        "/** Add a $0 item.\n * @param item\n */\npublic void add$2($3 item) { $1.add(item); }" +
        "/** Get $0 item by position.\n * @return item\n * @param index\n */\npublic $3 get$2(int index) { return $4$1.get(index); }" +
        "/** Remove all $0 items.\n */\npublic void clear$5() { $1.clear(); } }";
    // where $0 is the description text, $1 is the field name, $2 is the value name with initial uppercase character,
    //  $3 is the type, and $4 is a cast if an untyped list is used, or empty if a typed list is used
    
    /** Parser instance used by class. */
    private final ASTParser m_parser = ASTParser.newParser(AST.JLS3);
    
    /**
     * Method called after completing code generation for the target class.
     *
     * @param binding 
     * @param holder
     */
    public void finish(ElementBase binding, IClassHolder holder) {}
    
    /**
     * Method called before starting code generation for the target class.
     *
     * @param holder
     */
    public void start(IClassHolder holder) {}
    
    /**
     * Replace all occurrences of one string with another in a buffer.
     *
     * @param match
     * @param replace
     * @param buff
     */
    private static void replace(String match, String replace, StringBuffer buff) {
        int base = 0;
        while ((base = buff.indexOf(match, base)) >= 0) {
            buff.replace(base, base+match.length(), replace);
        }
    }
    
    /**
     * Method called after adding each data value to class. 
     * 
     * @param basename base name used for data value
     * @param collect repeated value flag
     * @param type value type (item value type, in the case of a repeated value)
     * @param field actual field
     * @param getmeth read access method (<code>null</code> if a flag value)
     * @param setmeth write access method (<code>null</code> if a flag value)
     * @param descript value description text
     * @param holder
     */
    public void valueAdded(String basename, boolean collect, String type, FieldDeclaration field,
        MethodDeclaration getmeth, MethodDeclaration setmeth, String descript, IClassHolder holder) {
        String fieldtype = field.getType().toString();
        if (collect && (fieldtype.startsWith("List") || fieldtype.startsWith("java.util.List"))) {
            
            // make substitutions in template text
            StringBuffer buff = new StringBuffer(s_classText);
            VariableDeclarationFragment vardecl = (VariableDeclarationFragment)field.fragments().get(0);
            replace("$0", descript, buff);
            replace("$1", vardecl.getName().getIdentifier(), buff);
            replace("$2", NameUtilities.depluralize(NameUtils.toNameWord(basename)), buff);
            replace("$3", holder.getTypeName(type), buff);
            String cast = field.getType().isParameterizedType() ? "" : ("(" + type + ")");
            replace("$4", cast, buff);
            replace("$5", getmeth.getName().getIdentifier().substring(3), buff);
            
            // parse the resulting text
            m_parser.setSource(buff.toString().toCharArray());
            CompilationUnit unit = (CompilationUnit)m_parser.createAST(null);
            
            // add all methods from output tree to class under construction
            TypeDeclaration typedecl = (TypeDeclaration)unit.types().get(0);
            for (Iterator iter = typedecl.bodyDeclarations().iterator(); iter.hasNext();) {
                ASTNode node = (ASTNode)iter.next();
                if (node instanceof MethodDeclaration) {
                    holder.addMethod((MethodDeclaration)node);
                }
            }

        }
    }
}
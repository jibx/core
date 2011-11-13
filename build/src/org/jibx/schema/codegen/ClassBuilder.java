/*
 * Copyright (c) 2007-2009, Dennis M. Sosnoski. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.jibx.runtime.Utility;

/**
 * Builder for a class definition. This wraps the AST with convenience methods and added control information.
 */
public class ClassBuilder
{
    /** Source file containing this class. */
    private final SourceBuilder m_source;
    
    /** Type declaration for class. */
    private final ASTNode m_class;
    
    /** Fields added to class. */
    private final ArrayList m_fields;
    
    /** Methods added to class. */
    private final ArrayList m_methods;
    
    /** Builders for inner classes of this class. */
    private final ArrayList m_innerBuilders;
    
    /** Directly-added inner classes of this class. */
    private final ArrayList m_innerClasses;
    
    /**
     * Constructor.
     * 
     * @param clas
     * @param source 
     */
    ClassBuilder(AbstractTypeDeclaration clas, SourceBuilder source) {
        m_source = source;
        m_class = clas;
        m_fields = new ArrayList();
        m_methods = new ArrayList();
        m_innerBuilders = new ArrayList();
        m_innerClasses = new ArrayList();
    }
    
    /**
     * Constructor for an inner class.
     * 
     * @param clas
     * @param outer
     */
    ClassBuilder(AbstractTypeDeclaration clas, ClassBuilder outer) {
        this(clas, outer.m_source);
        outer.m_innerBuilders.add(this);
    }
    
    /**
     * Constructor for an anonymous inner class.
     * 
     * @param clas
     * @param outer
     */
    public ClassBuilder(AnonymousClassDeclaration clas, ClassBuilder outer) {
        m_source = outer.m_source;
        m_class = clas;
        m_fields = null;
        m_methods = null;
        m_innerBuilders = null;
        m_innerClasses = null;
    }
    
    /**
     * AST access for related classes.
     *
     * @return AST
     */
    AST getAST() {
        return m_source.getAST();
    }

    /**
     * Add separately-constructed field declaration.
     *
     * @param field
     */
    public void addField(FieldDeclaration field) {
        m_fields.add(ASTNode.copySubtree(getAST(), field));
    }

    /**
     * Add separately-constructed method declaration.
     *
     * @param method
     */
    public void addMethod(MethodDeclaration method) {
        m_methods.add(ASTNode.copySubtree(getAST(), method));
    }

    /**
     * Add separately-constructed inner class declaration.
     *
     * @param type
     */
    public void addType(TypeDeclaration type) {
        m_innerClasses.add(ASTNode.copySubtree(getAST(), type));
    }
    
    /**
     * Set the superclass for this class.
     *
     * @param name
     */
    public void setSuperclass(String name) {
        if (m_class instanceof TypeDeclaration) {
            ((TypeDeclaration)m_class).setSuperclassType(createType(name));
        } else {
            throw new IllegalStateException("Internal error - can't set superclass on an enum or other special type");
        }
    }
    
    /**
     * Create type name.
     *
     * @param type fully qualified type name
     * @return name
     */
    Name createTypeName(String type) {
        return m_source.createTypeName(type);
    }
    
    /**
     * Clone an AST node. The cloned node will have no parent.
     *
     * @param node
     * @return clone
     */
    public ASTNode clone(ASTNode node) {
        return ASTNode.copySubtree(m_source.getAST(), node);
    }
    
    /**
     * Create type definition. This uses the supplied type name, which may include array suffixes, to construct the
     * actual type definition.
     *
     * @param type fully qualified type name, or primitive type name
     * @return constructed typed definition
     */
    public Type createType(String type) {
        return m_source.createType(type);
    }
    
    /**
     * Create a parameterized type. Both the type itself and the parameter type are given as names in this variation.
     *
     * @param type fully qualified type name
     * @param param fully qualified parameter type name
     * @return type
     */
    public Type createParameterizedType(String type, String param) {
        return m_source.createParameterizedType(type, param);
    }
    
    /**
     * Create a parameterized type. The type itself is given as a name, while the parameter type is an actual type in
     * this variation.
     *
     * @param type fully qualified type name
     * @param param type parameter
     * @return type
     */
    public Type createParameterizedType(String type, Type param) {
        ParameterizedType ptype = getAST().newParameterizedType(createType(type));
        ptype.typeArguments().add(param);
        return ptype;
    }
    
    /**
     * Set source comment for this class.
     *
     * @param text comment text
     */
    public void addSourceComment(String text) {
        if (m_class instanceof AbstractTypeDeclaration) {
            Javadoc javadoc = getAST().newJavadoc();
            TextElement element = getAST().newTextElement();
            element.setText(text);
            TagElement tag = getAST().newTagElement();
            tag.fragments().add(element);
            javadoc.tags().add(tag);
            ((AbstractTypeDeclaration)m_class).setJavadoc(javadoc);
        } else {
            throw new IllegalStateException("Internal error - cannot add JavaDoc to non-class type");
        }
    }
    
    /**
     * Set the abstract flag for this class.
     */
    public void setAbstract() {
        if (m_class instanceof TypeDeclaration) {
            Modifier modifier = m_source.getAST().newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
            ((TypeDeclaration)m_class).modifiers().add(modifier);
        } else {
            // should not be possible, but just in case of added types in future
            throw new IllegalStateException("Internal error - abstract not supported for class type");
        }
    }
    
    /**
     * Get the interfaces implemented by this class.
     *
     * @return interface names
     */
    public String[] getInterfaces() {
        if (m_class instanceof TypeDeclaration) {
            List types = ((TypeDeclaration)m_class).superInterfaceTypes();
            ArrayList names = new ArrayList();
            for (Iterator iter = types.iterator(); iter.hasNext();) {
                Type type = (Type)iter.next();
                names.add(type.toString());
            }
            return (String[])names.toArray(new String[names.size()]);
        } else if (m_class instanceof EnumDeclaration) {
            return Utility.EMPTY_STRING_ARRAY;
        } else {
            // should not be possible, but just in case of added types in future
            throw new IllegalStateException("Internal error - interface not supported for class type");
        }
    }
    
    /**
     * Get the fields defined in this class.
     *
     * @return fields
     */
    public FieldDeclaration[] getFields() {
        return (FieldDeclaration[])m_fields.toArray(new FieldDeclaration[m_fields.size()]);
    }
    
    /**
     * Get the methods defined in this class.
     *
     * @return methods
     */
    public MethodDeclaration[] getMethods() {
        return (MethodDeclaration[])m_methods.toArray(new MethodDeclaration[m_methods.size()]);
    }

    /**
     * Add an interface to this class definition.
     *
     * @param type interface type
     */
    public void addInterface(String type) {
        if (m_class instanceof TypeDeclaration) {
            ((TypeDeclaration)m_class).superInterfaceTypes().add(m_source.createType(type));
        } else if (m_class instanceof EnumDeclaration) {
            ((EnumDeclaration)m_class).superInterfaceTypes().add(m_source.createType(type));
        } else {
            // should not be possible, but just in case of added types in future
            throw new IllegalStateException("Internal error - interface not supported for class type");
        }
    }
    
    /**
     * Add JavaDoc to a declaration.
     *
     * @param doc documentation text, or <code>null</code> if none
     * @param decl
     */
    public void addJavaDoc(String doc, BodyDeclaration decl) {
        if (doc != null) {
            Javadoc javadoc = getAST().newJavadoc();
            TextElement element = getAST().newTextElement();
            element.setText(doc);
            TagElement tag = getAST().newTagElement();
            tag.fragments().add(element);
            javadoc.tags().add(tag);
            decl.setJavadoc(javadoc);
        }
    }

    /**
     * Add a constant to a Java 5 enum definition. This method is used for enums which use the name as the value.
     *
     * @param value 
     * @param doc documentation text, or <code>null</code> if none
     */
    public void addEnumConstant(String value, String doc) {
        if (m_class instanceof EnumDeclaration) {
            EnumConstantDeclaration enumdecl = getAST().newEnumConstantDeclaration();
            enumdecl.setName(getAST().newSimpleName(value));
            addJavaDoc(doc, enumdecl);
            ((EnumDeclaration)m_class).enumConstants().add(enumdecl);
        } else {
            // should not be possible, but just in case of added types in future
            throw new IllegalStateException("Internal error - cannot add constant to class type");
        }
    }

    /**
     * Add a constant to a Java 5 enum definition. This method is used for enums which have a value separate from the
     * name.
     *
     * @param name
     * @param doc documentation text, or <code>null</code> if none
     * @param value
     */
    public void addEnumConstant(String name, String doc, String value) {
        if (m_class instanceof EnumDeclaration) {
            EnumConstantDeclaration enumdecl = getAST().newEnumConstantDeclaration();
            enumdecl.setName(getAST().newSimpleName(name));
            StringLiteral strlit = getAST().newStringLiteral();
            strlit.setLiteralValue(value);
            enumdecl.arguments().add(strlit);
            addJavaDoc(doc, enumdecl);
            ((EnumDeclaration)m_class).enumConstants().add(enumdecl);
        } else {
            // should not be possible, but just in case of added types in future
            throw new IllegalStateException("Internal error - cannot add constant to class type");
        }
    }
    
    /**
     * Create new instance of array type.
     *
     * @param type base type name
     * @return array creation
     */
    public NewArrayBuilder newArrayBuilder(String type) {
        ArrayCreation create = getAST().newArrayCreation();
        create.setType(getAST().newArrayType(m_source.createType(type)));
        return new NewArrayBuilder(this, create);
    }

    /**
     * Build new instance creator of type using a no-argument constructor.
     *
     * @param type actual type
     * @return instance creation
     */
    public NewInstanceBuilder newInstance(Type type) {
        ClassInstanceCreation create = getAST().newClassInstanceCreation();
        create.setType(type);
        return new NewInstanceBuilder(this, create);
    }

    /**
     * Build new instance creator of type using a no-argument constructor.
     *
     * @param type base type name
     * @return instance creation
     */
    public NewInstanceBuilder newInstance(String type) {
        return newInstance(m_source.createType(type));
    }

    /**
     * Build new instance creator of a simple type using a constructor that takes a single string value.
     *
     * @param type simple type name
     * @param value string value to be passed to constructor
     * @return instance creation
     */
    public NewInstanceBuilder newInstanceFromString(String type, String value) {
        ClassInstanceCreation create = getAST().newClassInstanceCreation();
        create.setType(createType(type));
        StringLiteral literal = getAST().newStringLiteral();
        literal.setLiteralValue(value);
        create.arguments().add(literal);
        return new NewInstanceBuilder(this, create);
    }

    /**
     * Build new instance creator of a simple type using a constructor that takes a pair of string values.
     *
     * @param type simple type name
     * @param value1 first string value to be passed to constructor
     * @param value2 second string value to be passed to constructor
     * @return instance creation
     */
    public NewInstanceBuilder newInstanceFromStrings(String type, String value1, String value2) {
        ClassInstanceCreation create = getAST().newClassInstanceCreation();
        create.setType(createType(type));
        StringLiteral literal = getAST().newStringLiteral();
        literal.setLiteralValue(value1);
        create.arguments().add(literal);
        literal = getAST().newStringLiteral();
        literal.setLiteralValue(value2);
        create.arguments().add(literal);
        return new NewInstanceBuilder(this, create);
    }

    /**
     * Add field declaration.
     *
     * @param name field name
     * @param type field type
     * @return field builder
     */
    public FieldBuilder addField(String name, Type type) {
        VariableDeclarationFragment vfrag = getAST().newVariableDeclarationFragment();
        vfrag.setName(getAST().newSimpleName(name));
        FieldDeclaration fdecl = getAST().newFieldDeclaration(vfrag);
        fdecl.setType(type);
        m_fields.add(fdecl);
        return new FieldBuilder(this, fdecl);
    }

    /**
     * Add field declaration.
     *
     * @param name field name
     * @param type type name
     * @return field builder
     */
    public FieldBuilder addField(String name, String type) {
        return addField(name, m_source.createType(type));
    }

    /**
     * Add <code>int</code> field declaration with constant initialization.
     *
     * @param name variable name
     * @param value initial value
     * @return field builder
     */
    public FieldBuilder addIntField(String name, String value) {
        FieldBuilder field = addField(name, "int");
        field.setNumberInitializer(value);
        return field;
    }
    
    /**
     * Add constructor declaration.
     *
     * @param name simple class name
     * @return constructor builder
     */
    public MethodBuilder addConstructor(String name) {
        MethodDeclaration constr = getAST().newMethodDeclaration();
        constr.setName(getAST().newSimpleName(name));
        constr.setConstructor(true);
        m_methods.add(constr);
        return new MethodBuilder(this, constr);
    }
    
    /**
     * Add method declaration.
     *
     * @param name
     * @param type
     * @return method builder
     */
    public MethodBuilder addMethod(String name, Type type) {
        MethodDeclaration meth = getAST().newMethodDeclaration();
        meth.setName(getAST().newSimpleName(name));
        meth.setConstructor(false);
        meth.setReturnType2(type);
        if (m_class instanceof AnonymousClassDeclaration) {
            ((AnonymousClassDeclaration)m_class).bodyDeclarations().add(meth);        
        } else {
            m_methods.add(meth);
        }
        return new MethodBuilder(this, meth);
    }
    
    /**
     * Add method declaration.
     *
     * @param name
     * @param type fully qualified type name or primitive type name, with optional array suffixes
     * @return method builder
     */
    public MethodBuilder addMethod(String name, String type) {
        return addMethod(name, m_source.createType(type));
    }
    
    /**
     * Create internal member method call builder. 
     *
     * @param mname method name
     * @return builder
     */
    public InvocationBuilder createMemberMethodCall(String mname) {
        MethodInvocation methcall = getAST().newMethodInvocation();
        methcall.setName(getAST().newSimpleName(mname));
        return new InvocationBuilder(this, methcall);
    }
    
    /**
     * Create internal static method call builder. 
     *
     * @param mname method name
     * @return builder
     */
    public InvocationBuilder createLocalStaticMethodCall(String mname) {
        MethodInvocation methcall = getAST().newMethodInvocation();
        methcall.setName(getAST().newSimpleName(mname));
        return new InvocationBuilder(this, methcall);
    }
    
    /**
     * Create a static method call builder. 
     *
     * @param cname fully qualified class name
     * @param mname method name
     * @return builder
     */
    public InvocationBuilder createStaticMethodCall(String cname, String mname) {
        MethodInvocation methcall = getAST().newMethodInvocation();
        methcall.setExpression(getAST().newName(cname));
        methcall.setName(getAST().newSimpleName(mname));
        return new InvocationBuilder(this, methcall);
    }
    
    /**
     * Create a static method call builder.
     *
     * @param fname fully-qualified class and method name
     * @return builder
     */
    public InvocationBuilder createStaticMethodCall(String fname) {
        int split = fname.lastIndexOf('.');
        return createStaticMethodCall(fname.substring(0, split), fname.substring(split+1));
    }
    
    /**
     * Create method call builder on a local variable or field value. 
     *
     * @param name local variable or field name
     * @param mname method name
     * @return builder
     */
    public InvocationBuilder createNormalMethodCall(String name, String mname) {
        MethodInvocation methcall = getAST().newMethodInvocation();
        methcall.setExpression(getAST().newSimpleName(name));
        methcall.setName(getAST().newSimpleName(mname));
        return new InvocationBuilder(this, methcall);
    }
    
    /**
     * Create method call builder on the reference result of an expression. 
     *
     * @param expr instance expression
     * @param mname method name
     * @return builder
     */
    public InvocationBuilder createExpressionMethodCall(ExpressionBuilderBase expr, String mname) {
        MethodInvocation methcall = getAST().newMethodInvocation();
        methcall.setExpression(expr.getExpression());
        methcall.setName(getAST().newSimpleName(mname));
        return new InvocationBuilder(this, methcall);
    }
    
    /**
     * Build general infix expression.
     *
     * @param op operator
     * @return expression
     */
    public InfixExpressionBuilder buildInfix(Operator op) {
        InfixExpression infixex = getAST().newInfixExpression();
        infixex.setOperator(op);
        return new InfixExpressionBuilder(this, infixex);
    }
    
    /**
     * Build infix expression involving a local variable or field name as the left operand.
     *
     * @param name local variable or field name
     * @param op operator
     * @return expression
     */
    public InfixExpressionBuilder buildNameOp(String name, Operator op) {
        InfixExpression infixex = getAST().newInfixExpression();
        infixex.setOperator(op);
        return new InfixExpressionBuilder(this, infixex, getAST().newSimpleName(name));
    }
    
    /**
     * Build a string concatenation expression starting from from a string literal.
     *
     * @param text literal text
     * @return string concatenation expression
     */
    public InfixExpressionBuilder buildStringConcatenation(String text) {
        InfixExpression expr = getAST().newInfixExpression();
        StringLiteral strlit = getAST().newStringLiteral();
        strlit.setLiteralValue(text);
        expr.setOperator(Operator.PLUS);
        return new InfixExpressionBuilder(this, expr, strlit);
    }
    
    /**
     * Build a preincrement expression using a local variable or field name as the operand.
     *
     * @param name local variable or field name
     * @return expression
     */
    public PrefixExpressionBuilder buildPreincrement(String name) {
        PrefixExpression prefixex = getAST().newPrefixExpression();
        prefixex.setOperator(PrefixExpression.Operator.INCREMENT);
        return new PrefixExpressionBuilder(this, prefixex, getAST().newSimpleName(name));
    }
    
    /**
     * Build a cast expression.
     *
     * @param type result type
     * @return expression
     */
    public CastBuilder buildCast(Type type) {
        CastExpression castex = getAST().newCastExpression();
        castex.setType(type);
        return new CastBuilder(this, castex);
    }
    
    /**
     * Build a cast expression.
     *
     * @param type result type
     * @return expression
     */
    public CastBuilder buildCast(String type) {
        return buildCast(createType(type));
    }
    
    /**
     * Build array access expression for a named array variable and named index variable.
     *
     * @param aname
     * @param iname
     * @return array access
     */
    public ArrayAccessBuilder buildArrayIndexAccess(String aname, String iname) {
        ArrayAccess access = getAST().newArrayAccess();
        access.setArray(getAST().newSimpleName(aname));
        access.setIndex(getAST().newSimpleName(iname));
        return new ArrayAccessBuilder(this, access);
    }
    
    /**
     * Create a new block.
     *
     * @return block builder
     */
    public BlockBuilder newBlock() {
        return new BlockBuilder(this, getAST().newBlock());
    }
    
    /**
     * Finish building the source file data structures.
     */
    public void finish() {
        if (m_class instanceof AbstractTypeDeclaration) {
            List decls = ((AbstractTypeDeclaration)m_class).bodyDeclarations();
            decls.addAll(m_fields);
            decls.addAll(m_methods);
            for (int i = 0; i < m_innerBuilders.size(); i++) {
                ClassBuilder builder = (ClassBuilder)m_innerBuilders.get(i);
                builder.finish();
                decls.add(builder.m_class);
            }
            decls.addAll(m_innerClasses);
        }
    }
    
    /**
     * Get a sorted array of the field names and types defined in this class.
     *
     * @return sorted pairs
     */
    public StringPair[] getSortedFields() {
        StringPair[] pairs = new StringPair[m_fields.size()];
        for (int i = 0; i < m_fields.size(); i++) {
            FieldDeclaration field = (FieldDeclaration)m_fields.get(i);
            String name = ((VariableDeclarationFragment)field.fragments().get(0)).getName().toString();
            pairs[i] = new StringPair(name, field.getType().toString());
        }
        Arrays.sort(pairs);
        return pairs;
    }
}
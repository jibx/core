/*
 * Copyright (c) 2006-2009, Dennis M. Sosnoski. All rights reserved.
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

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.jibx.binding.model.BindingHolder;
import org.jibx.binding.model.FormatElement;
import org.jibx.schema.codegen.custom.SchemaRootBase;
import org.jibx.schema.codegen.extend.ClassDecorator;
import org.jibx.schema.codegen.extend.NameConverter;
import org.jibx.schema.elements.AnnotatedBase;
import org.jibx.schema.elements.FacetElement;
import org.jibx.schema.elements.FilteredSegmentList;
import org.jibx.schema.elements.SchemaBase;
import org.jibx.schema.elements.SimpleRestrictionElement;
import org.jibx.schema.elements.SimpleTypeElement;
import org.jibx.schema.elements.FacetElement.Enumeration;

/**
 * Information for an enumeration class to be included in code generated from schema.
 * 
 * @author Dennis M. Sosnoski
 */
public class EnumerationClassHolder extends ClassHolder
{
    /** Instance field to hold text value. */
    public static final String INSTANCEVALUE_FIELD = "value";

    /** Static conversion method name, with exception if value not matched. */
    public static final String CONVERTFORCE_METHOD = "fromValue";

    /** Static conversion method name, with null return if value not matched. */
    public static final String CONVERTIF_METHOD = "convert";

    /** Logger for class. */
    private static final Logger s_logger = Logger.getLogger(EnumerationClassHolder.class.getName());
    
    /** Enumeration group defining the class. */
    private ParentNode m_classGroup;
    
    /** Binding definition element for this class. */
    private FormatElement m_bindingFormat;
    
    /**
     * Constructor.
     * 
     * @param name class name
     * @param base base class name
     * @param pack package information
     * @param holder binding holder
     * @param nconv name converter
     * @param decorators class decorators
     * @param inner use inner classes for substructures
     */
    public EnumerationClassHolder(String name, String base, PackageHolder pack, BindingHolder holder,
        NameConverter nconv, ClassDecorator[] decorators, boolean inner) {
        super(name, base, pack, holder, nconv, decorators, inner, true);
    }
    
    /**
     * Constructor for creating a child inner class definition.
     * 
     * @param name class name
     * @param context parent class
     */
    protected EnumerationClassHolder(String name, ClassHolder context) {
        super(name, context, true);
    }
    
    /**
     * Set the binding component linked to this class.
     *
     * @param format binding definition element
     */
    public void setBinding(FormatElement format) {
        m_bindingFormat = format;
    }
    
    /**
     * Convert an item structure to a class representation. This may include creating child classes, where necessary.
     *
     * @param group item group
     * @param bindhold associated binding definition holder
     */
    public void buildDataStructure(GroupItem group, BindingHolder bindhold) {
        super.buildDataStructure(group, bindhold);
        if (group.isEnumeration()) {
            
            // set the basic configuration information
            m_classGroup = new ParentNode(group, null);
            m_classGroup.setDocumentation(extractDocumentation(group.getSchemaComponent()));
            
            // just add String as an import to make sure it doesn't get overridden
            m_importsTracker.addImport("java.lang.String", false);
            
        } else {
            throw new IllegalArgumentException("Internal error - group is not an enumeration");
        }
    }
    
    /**
     * Generate this class.
     * 
     * @param verbose 
     * @param builder class source file builder
     */
    public void generate(boolean verbose, SourceBuilder builder) {
        
        // setup the class builder
        Item item = m_classGroup.getItem();
        ClassBuilder clasbuilder;
        String name = getName();
        boolean java5 = getSchemaCustom().getEnumType() == SchemaRootBase.ENUM_JAVA5;
        if (m_outerClass == null) {
            clasbuilder = builder.newMainClass(name, java5);
        } else {
            clasbuilder = builder.newInnerClass(name, m_outerClass.getBuilder(), java5);
        }
        
        // handle the common initialization
        initClass(verbose, clasbuilder, m_classGroup);
        
        // get the list of facets including enumerations
        String fullname = getFullName();
        if (s_logger.isInfoEnabled()) {
            s_logger.info("Generating enumeration class " + fullname);
        }
        GroupItem group = (GroupItem)item;
        AnnotatedBase comp = group.getFirstChild().getSchemaComponent();
        if (comp.type() == SchemaBase.SIMPLETYPE_TYPE) {
            comp = (AnnotatedBase)((SimpleTypeElement)comp).getDerivation();
        }
        SimpleRestrictionElement restrict = (SimpleRestrictionElement)comp;
        FilteredSegmentList facets = restrict.getFacetsList();
        if (java5) {
            buildJava5Enumeration(name, fullname, facets, clasbuilder);
        } else {
            buildSimpleEnumeration(name, fullname, facets, clasbuilder);
        }
        
        // finish with inner class generation (which might be needed, if enumeration uses inlined type)
        generateInner(verbose, builder);
        finishClass(m_bindingFormat);
   }

    /**
     * Build simple type-safe enumeration class.
     *
     * @param name simple class name
     * @param fullname fully-qualified class name
     * @param facets list of facets (may not all be xs:enumeration facets)
     * @param clasbuilder
     */
    private void buildSimpleEnumeration(String name, String fullname, FilteredSegmentList facets,
        ClassBuilder clasbuilder) {
        
        // create a field to hold the string value
        String fieldname = m_nameConverter.toFieldName("value");
        clasbuilder.addField(fieldname, "java.lang.String").setPrivateFinal();
        
        // add private constructor with field assignment
        MethodBuilder constr = clasbuilder.addConstructor(name);
        constr.createBlock().addAssignVariableToField(INSTANCEVALUE_FIELD, fieldname);
        constr.setPrivate();
        constr.addParameter("value", "java.lang.String");
        
        // create a static instance of class for each enumeration value
        ArrayList enumpairs = new ArrayList();
        for (int i = 0; i < facets.size(); i++) {
            FacetElement facet = (FacetElement)facets.get(i);
            if (facet.type() == SchemaBase.ENUMERATION_TYPE) {
                
                // get value for this enumeration element
                FacetElement.Enumeration enumelem = (Enumeration)facet;
                String value = enumelem.getValue();
                
                // create a field to hold the corresponding instance of class
                String constname = m_nameSet.add(m_nameConverter.toConstantName(value));
                FieldBuilder field = clasbuilder.addField(constname, name);
                field.setPublicStaticFinal();
                field.setInitializer(clasbuilder.newInstanceFromString(name, value));
                field.addSourceComment(extractDocumentation(enumelem));
                
                // track both the value and the corresponding field name
                enumpairs.add(new StringPair(value, constname));
            }
        }
        
        // sort the pairs by value text
        StringPair[] pairs = (StringPair[])enumpairs.toArray(new StringPair[enumpairs.size()]);
        Arrays.sort(pairs);
        
        // create array of sorted text values
        String valuesname = m_nameConverter.toStaticFieldName("values");
        NewArrayBuilder array = clasbuilder.newArrayBuilder("java.lang.String");
        for (int i = 0; i < pairs.length; i++) {
            array.addStringLiteralOperand(pairs[i].getKey());
        }
        FieldBuilder field = clasbuilder.addField(valuesname, "java.lang.String[]");
        field.setInitializer(array);
        field.setPrivateStaticFinal();
        
        // create matching array of instances corresponding to sorted values
        String instsname = m_nameConverter.toStaticFieldName("instances");
        array = clasbuilder.newArrayBuilder(fullname);
        for (int i = 0; i < pairs.length; i++) {
            array.addVariableOperand(pairs[i].getValue());
        }
        field = clasbuilder.addField(instsname, fullname + "[]");
        field.setInitializer(array);
        field.setPrivateStaticFinal();
        
        // add toString method returning string value
        MethodBuilder tostring = clasbuilder.addMethod("toString", "java.lang.String");
        tostring.setPublic();
        tostring.createBlock().addReturnNamed(fieldname);
        
        // add static convert method returning instance for text value, or null if no match
        MethodBuilder convert = clasbuilder.addMethod(CONVERTIF_METHOD, fullname);
        convert.setPublicStatic();
        convert.addParameter("value", "java.lang.String");
        BlockBuilder body = convert.createBlock();
        
        // build code to handle search for text value
        InvocationBuilder invoke = clasbuilder.createStaticMethodCall("java.util.Arrays", "binarySearch");
        invoke.addVariableOperand(valuesname);
        invoke.addVariableOperand("value");
        body.addLocalVariableDeclaration("int", "index", invoke);
        
        // create the return block when value is matched
        BlockBuilder retblock = clasbuilder.newBlock();
        retblock.addReturnExpression(clasbuilder.buildArrayIndexAccess(instsname, "index"));
        
        // create the null return block when value is not matched
        BlockBuilder nullblock = clasbuilder.newBlock();
        nullblock.addReturnNull();
        
        // finish with the if statement that decides which to execute
        InfixExpressionBuilder test = clasbuilder.buildNameOp("index", Operator.GREATER_EQUALS);
        test.addNumberLiteralOperand("0");
        body.addIfElseStatement(test, retblock, nullblock);
        
        // add static valueOf method returning instance for text value
        MethodBuilder valueof = clasbuilder.addMethod(CONVERTFORCE_METHOD, fullname);
        valueof.setPublicStatic();
        valueof.addParameter("text", "java.lang.String");
        body = valueof.createBlock();
        m_bindingFormat.setDeserializerName(getBindingName() + ".fromValue");
        
        // build code to call convert method
        invoke = clasbuilder.createLocalStaticMethodCall(CONVERTIF_METHOD);
        invoke.addVariableOperand("text");
        body.addLocalVariableDeclaration(fullname, "value", invoke);
        
        // create the return block when value is found
        retblock = clasbuilder.newBlock();
        retblock.addReturnNamed("value");
        
        // create the exception thrown when value is not found
        BlockBuilder throwblock = clasbuilder.newBlock();
        InfixExpressionBuilder strcat = clasbuilder.buildStringConcatenation("Value '");
        strcat.addVariableOperand("text");
        strcat.addStringLiteralOperand("' is not allowed");
        throwblock.addThrowException("IllegalArgumentException", strcat);
        
        // finish with the if statement that decides which to execute
        test = clasbuilder.buildNameOp("value", Operator.EQUALS);
        test.addNullOperand();
        body.addIfElseStatement(test, throwblock, retblock);
        s_logger.debug("Defined custom enumeration");
    }

    /**
     * Build Java 5 enumeration class.
     *
     * @param name simple class name
     * @param fullname fully-qualified class name
     * @param facets list of facets (may not all be xs:enumeration facets)
     * @param clasbuilder
     */
    private void buildJava5Enumeration(String name, String fullname, FilteredSegmentList facets,
        ClassBuilder clasbuilder) {
        
        // check names to be used for enumeration values
        boolean isdiff = false;
        ArrayList enums = new ArrayList();
        ArrayList names = new ArrayList();
        for (int i = 0; i < facets.size(); i++) {
            FacetElement facet = (FacetElement)facets.get(i);
            if (facet.type() == SchemaBase.ENUMERATION_TYPE) {
                
                // get value for this enumeration element
                FacetElement.Enumeration enumelem = (Enumeration)facet;
                enums.add(enumelem);
                String value = enumelem.getValue();
                
                // convert to an enum name
                String constname = m_nameSet.add(m_nameConverter.toConstantName(value));
                names.add(constname);
                if (!constname.equals(value)) {
                    isdiff = true;
                }
                
            }
        }
        
        // add methods for conversion
        if (isdiff) {
            
            // create a field to hold the string value
            String fieldname = m_nameConverter.toFieldName("value");
            clasbuilder.addField(fieldname, "java.lang.String").setPrivateFinal();
            
            // add private constructor with field assignment
            MethodBuilder constr = clasbuilder.addConstructor(name);
            constr.createBlock().addAssignVariableToField(INSTANCEVALUE_FIELD, fieldname);
            constr.setPrivate();
            constr.addParameter("value", "java.lang.String");
            
            // add the actual enumeration values
            for (int i = 0; i < names.size(); i++) {
                FacetElement.Enumeration enumelem = (Enumeration)enums.get(i);
                clasbuilder.addEnumConstant((String)names.get(i), extractDocumentation(enumelem),
                    enumelem.getValue());
            }
            
            // add xmlValue method returning string value
            MethodBuilder tostring = clasbuilder.addMethod("xmlValue", "java.lang.String");
            tostring.setPublic();
            tostring.createBlock().addReturnNamed(fieldname);
            m_bindingFormat.setEnumValueName("xmlValue");
            
            // add static convert method returning instance for text value, or null if no match
            MethodBuilder convert = clasbuilder.addMethod(CONVERTIF_METHOD, fullname);
            convert.setPublicStatic();
            convert.addParameter("value", "java.lang.String");
            BlockBuilder body = convert.createBlock();
            
            // create the return block when value is matched
            BlockBuilder retblock = clasbuilder.newBlock();
            retblock.addReturnNamed("inst");
            
            // create the method calls to compare current instance text with value
            InvocationBuilder valueexpr = clasbuilder.createNormalMethodCall("inst", "xmlValue");
            InvocationBuilder equalsexpr = clasbuilder.createExpressionMethodCall(valueexpr, "equals");
            equalsexpr.addVariableOperand("value");
            
            // embed the complete if statement in body block of for loop
            BlockBuilder forblock = clasbuilder.newBlock();
            forblock.addIfStatement(equalsexpr, retblock);
            InvocationBuilder loopexpr = clasbuilder.createLocalStaticMethodCall("values");
            body.addSugaredForStatement("inst", fullname, loopexpr, forblock);
            
            // finish with null return for match not found
            body.addReturnNull();
            s_logger.debug("Defined enum with separate values");
            
        } else {
            
            // add the actual definitions for a simple enum
            for (int i = 0; i < names.size(); i++) {
                FacetElement.Enumeration enumelem = (Enumeration)enums.get(i);
                clasbuilder.addEnumConstant((String)names.get(i), extractDocumentation(enumelem));
            }
            s_logger.debug("Defined simple enum");
        }
    }
}
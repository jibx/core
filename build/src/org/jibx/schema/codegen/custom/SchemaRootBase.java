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

package org.jibx.schema.codegen.custom;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jibx.binding.model.BuiltinFormats;
import org.jibx.binding.model.FormatElement;
import org.jibx.runtime.EnumSet;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;
import org.jibx.schema.codegen.JavaType;
import org.jibx.schema.codegen.extend.ClassDecorator;
import org.jibx.schema.codegen.extend.DefaultNameConverter;
import org.jibx.schema.codegen.extend.NameConverter;
import org.jibx.schema.support.Conversions;
import org.jibx.schema.validation.ProblemLocation;
import org.jibx.schema.validation.ValidationContext;
import org.jibx.util.ChainedMap;
import org.jibx.util.ReflectionUtilities;
import org.jibx.util.StringArray;

/**
 * Base class for possible root customizations.
 * 
 * @author Dennis M. Sosnoski
 */
public abstract class SchemaRootBase extends NestingCustomBase
{
    private static final ClassDecorator[] EMPTY_DECORATORS_ARRAY = new ClassDecorator[0];
    
    /** Enumeration of allowed attribute names */
    public static final StringArray s_allowedAttributes = new StringArray(new String[] { "binding-file-name",
        "binding-per-schema", "delete-annotations", "enumeration-type", "generate-all", "import-docs", "inline-groups",
        "line-width", "package", "prefer-inline", "prefix", "repeated-type", "show-schema", "structure-optional",
        "use-inner" }, NestingCustomBase.s_allowedAttributes);
    
    /** Default converter used if none set. */
    private static final NameConverter s_defaultNameConverter = new DefaultNameConverter();
    
    /** Default format name to definition map. */
    private static final Map s_nameToFormat;
    static {
        s_nameToFormat = new HashMap();
        for (int i = 0; i < BuiltinFormats.s_builtinFormats.length; i++) {
            FormatElement format = BuiltinFormats.s_builtinFormats[i];
            s_nameToFormat.put(format.getLabel(), format);
        }
    }
    
    //
    // Value set information
    
    public static final int REPEAT_ARRAY = 0;
    public static final int REPEAT_LIST = 1;
    public static final int REPEAT_TYPED = 2;
    
    public static final EnumSet s_repeatValues = new EnumSet(REPEAT_ARRAY,
        new String[] { "array", "list", "typed"});
    
    public static final int ENUM_JAVA5 = 0;
    public static final int ENUM_SIMPLE = 1;
    
    public static final EnumSet s_enumValues = new EnumSet(ENUM_JAVA5,
        new String[] { "java5", "simple" });
    
    //
    // Instance data
    
    /** Fully-qualified package name. */
    private String m_package;
    
    /** Generate one binding for each schema flag (binding per namespace if <code>false</code>). */
    private Boolean m_bindingPerSchema;
    
    /** Binding file name (only allowed if single namespace, <code>null</code> if derived from schema name). */
    private String m_bindingFileName;
    
    /** Prefix used for namespace (only allowed if single namespace, <code>null</code> if from schema). */
    private String m_prefix;
    
    /** Generate even unused global definitions. */
    private Boolean m_generateAll;
    
    /** Inline xs:group and xs:attributeGroup definitions by default. */
    private Boolean m_inlineGroups;
    
    /** Prefer inline definitions (separate classes for all if <code>false</code>). */
    private Boolean m_preferInline;
    
    /** Use inner classes for substructures (top-level classes for all if <code>false</code>). */
    private Boolean m_useInner;
    
    /** Delete annotations flag. */
    private Boolean m_deleteAnnotations;
    
    /** Convert schema documentation to JavaDocs in generated code flag. */
    private Boolean m_importDocs;
    
    /** Include schema fragments in generated class JavaDocs flag. */
    private Boolean m_showSchema;
    
    /** Force separate class for collection flag. */
    private Boolean m_nullCollection;
    
    /** Set references as optional structure where possible flag. */
    private Boolean m_structureOptional;
    
    /** Map from schema type name to Java type information (lazy create, <code>null</code> if not used at level). */
    private Map m_schemaTypes;
    
    /** Name converter instance (<code>null</code> if none set at level). */
    private NameConverter m_nameConverter;
    
    /** Inherit code generation class decorators from parent flag. */
    private Boolean m_inheritDecorators;
    
    /** Decorators to be used in code generation (<code>null</code> if none set at level). */
    private List m_classDecorators;
    
    /** Preferred maximum line width for generated code. */
    private Integer m_lineWidth;
    
    /** Code for repeated value representation (<code>-1</code> if not set at level). */
    private int m_repeatCode = -1;
    
    /** Code for enumeration representation (<code>-1</code> if not set at level). */
    private int m_enumCode = -1;
    
    /**
     * Constructor.
     * 
     * @param parent
     */
    public SchemaRootBase(SchemaRootBase parent) {
        super(parent);
    }
    
    /**
     * Get parent customization (which will either be <code>null</code>, or another instance of this class).
     *
     * @return parent, or <code>null</code> if none
     */
    public SchemaRootBase getRootParent() {
        return (SchemaRootBase)getParent();
    }
    
    /**
     * Check if a separate binding should be generated for each schema. The default is <code>false</code> if not
     * overridden at any level.
     *
     * @return generate unused flag
     */
    public boolean isBindingPerSchema() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_bindingPerSchema != null) {
                return root.m_bindingPerSchema.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return false;
    }
    
    /**
     * Check whether unused definitions should be included in code generation. The default is <code>true</code> if not
     * overridden at any level.
     *
     * @return generate unused flag
     */
    public boolean isGenerateAll() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_generateAll != null) {
                return root.m_generateAll.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return true;
    }
    
    /**
     * Check whether xs:group and xs:attributeGroup definitions should be inlined by default. The default is
     * <code>false</code> if not overridden at any level.
     *
     * @return generate unused flag
     */
    public boolean isInlineGroups() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_inlineGroups != null) {
                return root.m_inlineGroups.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return false;
    }
    
    /**
     * Check whether inlining of components is preferred. The default is <code>false</code> if not overridden at any
     * level.
     *
     * @return inline components flag
     */
    public boolean isPreferInline() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_preferInline != null) {
                return root.m_preferInline.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return false;
    }
    
    /**
     * Check whether inner classes are preferred for components used only by one definition. The default is
     * <code>true</code> if not overridden at any level.
     *
     * @return inline components flag
     */
    public boolean isUseInner() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_useInner != null) {
                return root.m_useInner.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return true;
    }
    
    /**
     * Check whether annotations are to be deleted. The default is <code>true</code> if not overridden at any level.
     *
     * @return delete annotations flag
     */
    public boolean isDeleteAnnotations() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_deleteAnnotations != null) {
                return root.m_deleteAnnotations.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return true;
    }
    
    /**
     * Check whether schema documentation is to be used for JavaDocs in the generated code. The default is
     * <code>true</code> if not overridden at any level.
     *
     * @return use schema documentation in JavaDocs flag
     */
    public boolean isJavaDocDocumentation() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_importDocs != null) {
                return root.m_importDocs.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return true;
    }
    
    /**
     * Check whether schema fragments matching a generated class are to be included in the class JavaDocs. The default
     * is <code>true</code> if not overridden at any level.
     *
     * @return schema fragments in class JavaDocs flag
     */
    public boolean isSchemaFragmentDocumentation() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_showSchema != null) {
                return root.m_showSchema.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return true;
    }
    
    /**
     * Check whether collection holder (array, list, etc.) can be <code>null</code>. This is only relevant to
     * collections using an optional wrapper element: If this flag is <code>true</code> a <code>null</code> collection
     * holder indicates the element is missing; otherwise a class is created to wrap the collection holder, and a
     * <code>null</code> for that class indicates the element is missing. The default is <code>true</code> if not
     * overridden at any level.
     * TODO: currently unsupported
     *
     * @return force collection wrapper flag
     */
    public boolean isNullCollectionAllowed() {
//        SchemaRootBase root = this;
//        while (root != null) {
//            if (root.m_nullCollection != null) {
//                return root.m_nullCollection.booleanValue();
//            } else {
//                root = root.getRootParent();
//            }
//        }
        return false;
    }
    
    /**
     * Check whether references to classes with no associated element and all components optional should be made
     * optional in the generated binding. The effect of making such class references optional is that the reference will
     * be set <code>null</code> when unmarshalling if none of the components are present, and will be checked for
     * <code>null</code> when marshalling. The default is <code>true</code> if not overridden at any level.
     *
     * @return prefer structure optional flag
     */
    public boolean isStructureOptional() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_structureOptional != null) {
                return root.m_structureOptional.booleanValue();
            } else {
                root = root.getRootParent();
            }
        }
        return true;
    }
    
    /**
     * Get the preferred maximum line width used for generated classes. The default is 80.
     *
     * @return line width
     */
    public int getLineWidth() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_lineWidth != null) {
                return root.m_lineWidth.intValue();
            } else {
                root = root.getRootParent();
            }
        }
        return 80;
    }
    
    /**
     * Get the repeated value representation type code to be applied for this schema or set of schemas. The default
     * value is {@link #REPEAT_TYPED} if not overridden at any level.
     * 
     * @return code
     */
    public int getRepeatType() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_repeatCode >= 0) {
                return root.m_repeatCode;
            } else {
                root = root.getRootParent();
            }
        }
        return REPEAT_TYPED;
    }
    
    /**
     * Set the repeated value representation type code.
     * 
     * @param code type code, <code>-1</code> if to be unset
     */
    public void setRepeatType(int code) {
        if (code != -1) {
            s_repeatValues.checkValue(code);
        }
        m_repeatCode = code;
    }
    
    /**
     * Get the repeated value representation text value set specifically for this element.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getRepeatedTypeText() {
        if (m_repeatCode >= 0) {
            return s_repeatValues.getName(m_repeatCode);
        } else {
            return null;
        }
    }
    
    /**
     * Set the repeated value representation text value. This method is provided only for use when unmarshalling.
     * 
     * @param text (<code>null</code> if not set)
     * @param ictx
     */
    private void setRepeatedTypeText(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            m_repeatCode = Conversions.convertEnumeration(text, s_repeatValues, "repeat-form", ictx);
        }
    }
    
    /**
     * Get the enumeration representation type code to be applied for this schema or set of schemas. The default
     * value is {@link #ENUM_JAVA5} if not overridden at any level.
     * 
     * @return code
     */
    public int getEnumType() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_enumCode >= 0) {
                return root.m_enumCode;
            } else {
                root = root.getRootParent();
            }
        }
        return ENUM_JAVA5;
    }
    
    /**
     * Set the enumeration representation type code.
     * 
     * @param code type code, <code>-1</code> if to be unset
     */
    public void setEnumType(int code) {
        if (code != -1) {
            s_enumValues.checkValue(code);
        }
        m_enumCode = code;
    }
    
    /**
     * Get the enumeration representation text value set specifically for this element.
     * 
     * @return text (<code>null</code> if not set)
     */
    public String getEnumerationTypeText() {
        if (m_enumCode >= 0) {
            return s_enumValues.getName(m_enumCode);
        } else {
            return null;
        }
    }
    
    /**
     * Set the enumeration representation text value. This method is provided only for use when unmarshalling.
     * 
     * @param text (<code>null</code> if not set)
     * @param ictx
     */
    private void setEnumerationTypeText(String text, IUnmarshallingContext ictx) {
        if (text != null) {
            m_enumCode = Conversions.convertEnumeration(text, s_enumValues, "enum-form", ictx);
        }
    }
    
    /**
     * Get fully-qualified package name. This is inherited by nested schemas if set at any level.
     *
     * @return package (<code>null</code> if none set)
     */
    public String getPackage() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_package != null) {
                return root.m_package;
            } else {
                root = root.getRootParent();
            }
        }
        return null;
    }
    
    /**
     * Set fully-qualified package name. This is inherited by nested schemas if set at any level.
     *
     * @param pack (<code>null</code> if none)
     */
    public void setPackage(String pack) {
        m_package = pack;
    }
    
    /**
     * Get binding definition file name. The binding name may not be set if more than one namespace is used in the
     * schemas represented by this customization.
     *
     * @return name, <code>null</code> if to be derived from schema name
     */
    public String getBindingFileName() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_bindingFileName != null) {
                return root.m_bindingFileName;
            } else {
                root = root.getRootParent();
            }
        }
        return null;
    }

    /**
     * Get prefix used for namespace. The prefix may not be set if more than one namespace is used in the schemas
     * represented by this customization.
     *
     * @return prefix, <code>null</code> if to be found from schema
     */
    public String getPrefix() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_prefix != null) {
                return root.m_prefix;
            } else {
                root = root.getRootParent();
            }
        }
        return null;
    }

    /**
     * Add schema type handling override.
     *
     * @param type
     */
    private void addSchemaType(JavaType type) {
        if (type != null) {
            if (m_schemaTypes == null) {
                m_schemaTypes = new ChainedMap(getSchemaTypes());
            }
            m_schemaTypes.put(type.getSchemaName(), type);
        }
    }

    /**
     * Get map from schema type local name to type information.
     *
     * @return map
     */
    Map getSchemaTypes() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_schemaTypes != null) {
                return root.m_schemaTypes;
            } else {
                root = root.getRootParent();
            }
        }
        return JavaType.getTypeMap();
    }
    
    /**
     * Set name converter to be used.
     *
     * @param nconv
     * @param ictx
     */
    private void setNameConverter(NameConverter nconv, IUnmarshallingContext ictx) {
        if (nconv != null) {
            if (m_nameConverter != null) {
                ValidationContext vctx = (ValidationContext)ictx.getUserContext();
                vctx.addWarning("Repeated 'name-converter' element overrides previous setting", new ProblemLocation(ictx));
            }
            m_nameConverter = nconv;
        }
    }

    /**
     * Get name converter.
     *
     * @return converter (<code>null</code> if none defined)
     */
    NameConverter getNameConverter() {
        SchemaRootBase root = this;
        while (root != null) {
            if (root.m_nameConverter != null) {
                return root.m_nameConverter;
            } else {
                root = root.getRootParent();
            }
        }
        return s_defaultNameConverter;
    }
    
    /**
     * Add a class decorator to the current list.
     *
     * @param decor
     */
    private void addClassDecorator(ClassDecorator decor) {
        if (decor != null) {
            if (m_classDecorators == null) {
                m_classDecorators = new ArrayList();
            }
            m_classDecorators.add(decor);
        }
    }

    /**
     * Get class decorators.
     *
     * @return decorators
     */
    ClassDecorator[] getClassDecorators() {
        SchemaRootBase root = this;
        while (root != null) {
            List decorators = root.m_classDecorators;
            if (decorators != null) {
                return (ClassDecorator[])decorators.toArray(new ClassDecorator[decorators.size()]);
            } else if (root.m_inheritDecorators == null || root.m_inheritDecorators.booleanValue()) {
                root = root.getRootParent();
            } else {
                break;
            }
        }
        return EMPTY_DECORATORS_ARRAY;
    }
    
    /**
     * Create an instance of the appropriate class decorator class, to be used for unmarshalling. This always uses the
     * default constructor for the target class.
     *
     * @param ictx
     * @return class decorator instance, or <code>null</code> if error
     */
    private static ClassDecorator classDecoratorFactory(IUnmarshallingContext ictx) {
        
        // get the class to be used for class decorator instance
        ValidationContext vctx = (ValidationContext)ictx.getUserContext();
        UnmarshallingContext ctx = (UnmarshallingContext)ictx;
        String cname = ctx.attributeText(null, "class", null);
        if (cname == null) {
            vctx.addError("Missing required 'class' attribute", new ProblemLocation(ctx));
        } else {
            try {
                
                // make sure the class implements the required interface
                Class clas = SchemaRootBase.class.getClassLoader().loadClass(cname);
                if (ClassDecorator.class.isAssignableFrom(clas)) {
                    vctx.addError("Class " + cname + " does not implement the required IClassDecorator interface",
                        new ProblemLocation(ictx));
                } else {
                    try {
                        return (ClassDecorator)clas.newInstance();
                    } catch (InstantiationException e) {
                        vctx.addError("Error creating instance of class " + cname + ": " + e.getMessage(),
                            new ProblemLocation(ictx));
                    } catch (IllegalAccessException e) {
                        vctx.addError("Unable to access constructor for class " + cname + ": " + e.getMessage(),
                            new ProblemLocation(ictx));
                    }
                }
                
            } catch (ClassNotFoundException e) {
                vctx.addError("Unable to find class " + cname + " in classpath", new ProblemLocation(ictx));
            }
        }
        return null;
    }

    /**
     * Unmarshaller for schema-type elements.
     */
    public static class SchemaTypeUnmarshaller implements IUnmarshaller
    {
        /** Actual element name used in binding. */
        private static String ELEMENT_NAME = "schema-type";
        
        /** Enumeration of allowed attribute names */
        public static final StringArray s_allowedAttributes = new StringArray(new String[] { "check-method",
            "deserializer", "format-name", "java-class", "serializer", "type-name" });
        
        /**
         * Check for element present.
         *
         * @param ictx
         * @return <code>true</code> if present, <code>false</code> if not
         * @throws JiBXException on error
         */
        public boolean isPresent(IUnmarshallingContext ictx) throws JiBXException {
            return ictx.isAt(null, ELEMENT_NAME);
        }

        /**
         * Unmarshal instance of element.
         *
         * @param obj ignored (new instance always created)
         * @param ictx
         * @return unmarshalled instance
         * @throws JiBXException on error
         */
        public Object unmarshal(Object obj, IUnmarshallingContext ictx) throws JiBXException {
            
            // position to start tag and create instance to unmarshal
            ValidationContext vctx = (ValidationContext)ictx.getUserContext();
            UnmarshallingContext ctx = (UnmarshallingContext)ictx;
            ctx.parseToStartTag(null, ELEMENT_NAME);
            
            // accumulate attribute values for constructor
            String check = ctx.attributeText(null, "check-method", null);
            String dser = ctx.attributeText(null, "deserializer", null);
            String format = ctx.attributeText(null, "format-name", null);
            String jclas = ctx.attributeText(null, "java-class", null);
            String ser = ctx.attributeText(null, "serializer", null);
            String stype = ctx.attributeText(null, "type-name");
            boolean valid = true;
            if (jclas == null && format == null) {
                
                // need either 'java-class' or 'format-name', just report error and skip
                vctx.addError("'java-class' attribute is required unless 'format-name' is used", ctx.getStackTop());
                valid = false;
                
            } else if (format != null) {
                
                // check format for existence and consistency
                FormatElement def = (FormatElement)s_nameToFormat.get(format);
                if (def == null) {
                    vctx.addError('\'' + format + "' is not a valid built-in format name", ctx.getStackTop());
                    valid = false;
                } else {
                    if (jclas == null) {
                        jclas = def.getTypeName();
                    }
                }
            }
            
            // look through all attributes of current element
            for (int i = 0; i < ctx.getAttributeCount(); i++) {
                
                // check if nonamespace attribute is in the allowed set
                String name = ctx.getAttributeName(i);
                if (ctx.getAttributeNamespace(i).length() == 0) {
                    if (s_allowedAttributes.indexOf(name) < 0) {
                        vctx.addWarning("Undefined attribute " + name, ctx.getStackTop());
                    }
                }
            }
            
            // skip content, and create and return object instance
            ctx.skipElement();
            if (valid) {
                return new JavaType(stype, null, jclas, format, ser, dser, check);
            } else {
                return null;
            }
        }
    }

    /**
     * Unmarshaller for extension elements. This expects to find a 'class' attribute giving the name of the class to be
     * created, along with other attributes used to set properties on an instance of the class. The latter attributes
     * are handled by converting the attribute name into a field (with an 'm_' prefix) or method (with a 'set' prefix)
     * name, then storing the value to that field or method.
     */
    public static abstract class ExtensionUnmarshaller implements IUnmarshaller
    {
        private String m_name;
        
        /**
         * Constructor.
         *
         * @param name local name for the element handled
         */
        public ExtensionUnmarshaller(String name) {
            m_name = name;
        }
        
        /**
         * Instance creation method. This is called by the {@link #unmarshal(Object, IUnmarshallingContext)} method to
         * create the actual object instance to be handled. Subclasses must implement this method to create the instance
         * and perform any appropriate initialization.
         *
         * @param cname class name from element (<code>null</code> if not supplied)
         * @param ctx unmarshalling context
         * @return object instance to be unmarshalled, or <code>null</code> if error
         */
        protected abstract Object createInstance(String cname, UnmarshallingContext ctx);
        
        /**
         * Check for element present.
         *
         * @param ictx
         * @return <code>true</code> if present, <code>false</code> if not
         * @throws JiBXException on error
         */
        public boolean isPresent(IUnmarshallingContext ictx) throws JiBXException {
            return ictx.isAt(null, m_name);
        }

        /**
         * Unmarshal instance of element. This ignores the 'class' attribute, if present, since that's intended for use
         * by the subclass.
         *
         * @param obj ignored (new instance always created)
         * @param ictx
         * @return unmarshalled instance
         * @throws JiBXException on error
         */
        public Object unmarshal(Object obj, IUnmarshallingContext ictx) throws JiBXException {
            
            // position to start tag and create instance to unmarshal
            ValidationContext vctx = (ValidationContext)ictx.getUserContext();
            UnmarshallingContext ctx = (UnmarshallingContext)ictx;
            ctx.parseToStartTag(null, m_name);
            obj = createInstance(ctx.attributeText(null, "class", null), ctx);
            if (obj != null) {
                
                // accumulate all the attribute name-value pairs
                Map map = new HashMap();
                for (int i = 0; i < ctx.getAttributeCount(); i++) {
                    String name = ctx.getAttributeName(i);
                    String value = ctx.getAttributeValue(i);
                    if (ctx.getAttributeNamespace(i).length() == 0) {
                        if (!"class".equals(name)) {
                            map.put(name, value);
                        }
                    } else {
                        vctx.addError("Unknown namespaced attribute '" + name + "'", new ProblemLocation(ctx));
                    }
                }
                
                // create and populate values on instance
                try {
                    ReflectionUtilities.applyKeyValueMap(map, obj);
                } catch (IllegalArgumentException e) {
                    vctx.addError(e.getMessage(), new ProblemLocation(ctx));
                }
            }
            
            // skip element and return unmarshalled decorator instance
            ctx.skipElement();
            return obj;
        }
    }
    
    /**
     * Unmarshaller for name converter extension elements.
     */
    public static class NameConverterUnmarshaller extends ExtensionUnmarshaller
    {
        /**
         * Constructor.
         */
        public NameConverterUnmarshaller() {
            super("name-converter");
        }

        /**
         * Create an instance of the appropriate name converter class, to be used for unmarshalling. This first looks
         * for a constructor in the target class which takes an instance of the existing name converter class as an
         * argument, and uses that constructor if found (passing the existing name converter, so that settings can be
         * inherited). If there's no constructor matching the existing name converter class the default constructor is
         * instead used.
         *
         * @param cname class name from element (<code>null</code> if not supplied)
         * @param ctx unmarshalling context
         * @return object
         */
        protected Object createInstance(String cname, UnmarshallingContext ctx) {
            
            // set the class to be used for name converter instance
            ValidationContext vctx = (ValidationContext)ctx.getUserContext();
            if (cname == null) {
                cname = "org.jibx.schema.codegen.extend.DefaultNameConverter";
            }
            try {
                
                // make sure the class implements the required interface
                Class clas = SchemaRootBase.class.getClassLoader().loadClass(cname);
                if (NameConverter.class.isAssignableFrom(clas)) {
                    try {
                        
                        // check for existing name converter instance to pass to constructor
                        SchemaRootBase outer = (SchemaRootBase)ctx.getStackTop();
                        NameConverter prior = outer.getNameConverter();
                        if (prior != null) {
                            
                            // try to create instance of new converter passing old converter
                            try {
                                Constructor cons = clas.getConstructor(new Class[] { prior.getClass() });
                                try {
                                    return cons.newInstance(new Object[] { prior });
                                } catch (IllegalArgumentException e) { /* ignore failure */
                                } catch (InvocationTargetException e) {
                                    vctx.addWarning("Failed passing existing name converter to constructor for class " +
                                        cname + ": " + e.getMessage(), new ProblemLocation(ctx));
                                }
                                
                            } catch (SecurityException e) { /* ignore failure */
                            } catch (NoSuchMethodException e) { /* ignore failure */ }
                        }
                        
                        // just create instance using no-argument constructor
                        return clas.newInstance();
                        
                    } catch (InstantiationException e) {
                        vctx.addError("Error creating instance of class " + cname + ": " + e.getMessage(),
                            new ProblemLocation(ctx));
                    } catch (IllegalAccessException e) {
                        vctx.addError("Unable to access constructor for class " + cname + ": " + e.getMessage(),
                            new ProblemLocation(ctx));
                    }
                } else {
                    vctx.addError("Class " + cname + " does not implement the required NameConverter interface",
                        new ProblemLocation(ctx));
                }
                
            } catch (ClassNotFoundException e) {
                vctx.addError("Unable to find class " + cname + " in classpath", new ProblemLocation(ctx));
            }
            return null;
        }
    }
    
    /**
     * Unmarshaller for class decorator extension elements.
     */
    public static class ClassDecoratorUnmarshaller extends ExtensionUnmarshaller
    {
        /**
         * Constructor.
         */
        public ClassDecoratorUnmarshaller() {
            super("class-decorator");
        }

        /**
         * Create an instance of the appropriate class decorator class, to be used for unmarshalling. This always uses
         * the default constructor for the specified class.
         *
         * @param cname class name from element (<code>null</code> if not supplied)
         * @param ctx unmarshalling context
         * @return object
         */
        protected Object createInstance(String cname, UnmarshallingContext ctx) {
            
            // check class to be used for class decorator instance
            ValidationContext vctx = (ValidationContext)ctx.getUserContext();
            if (cname == null) {
                vctx.addError("Missing required 'class' attribute", new ProblemLocation(ctx));
            } else {
                try {
                    
                    // make sure the class implements the required interface
                    Class clas = SchemaRootBase.class.getClassLoader().loadClass(cname);
                    if (ClassDecorator.class.isAssignableFrom(clas)) {
                        try {
                            return clas.newInstance();
                        } catch (InstantiationException e) {
                            vctx.addError("Error creating instance of class " + cname + ": " + e.getMessage(),
                                new ProblemLocation(ctx));
                        } catch (IllegalAccessException e) {
                            vctx.addError("Unable to access constructor for class " + cname + ": " + e.getMessage(),
                                new ProblemLocation(ctx));
                        }
                    } else {
                        vctx.addError("Class " + cname + " does not implement the required ClassDecorator interface",
                            new ProblemLocation(ctx));
                    }
                    
                } catch (ClassNotFoundException e) {
                    vctx.addError("Unable to find class " + cname + " in classpath", new ProblemLocation(ctx));
                }
            }
            return null;
        }
    }
}
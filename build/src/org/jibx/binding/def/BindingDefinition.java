/*
Copyright (c) 2003-2012, Dennis M. Sosnoski. All rights reserved.

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

package org.jibx.binding.def;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.Type;
import org.jibx.binding.classes.BoundClass;
import org.jibx.binding.classes.BranchWrapper;
import org.jibx.binding.classes.ClassCache;
import org.jibx.binding.classes.ClassFile;
import org.jibx.binding.classes.ClassItem;
import org.jibx.binding.classes.ExceptionMethodBuilder;
import org.jibx.binding.classes.MethodBuilder;
import org.jibx.binding.classes.MungedClass;
import org.jibx.binding.util.ArrayMap;
import org.jibx.binding.util.IntegerCache;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.QName;
import org.jibx.runtime.impl.GrowableStringArray;

/**
 * Binding definition. This is the root of the object graph for a binding.
 *
 * @author Dennis M. Sosnoski
 */
public class BindingDefinition extends BindingBuilder.ContainerBase
implements IContainer
{
    //
    // Miscellaneous static data.
    
    /** Name of object default conversion. */
    static final QName OBJECT_DEFAULT_NAME = new QName("Object.default");

    /** First namespace index available for user definitions. */
    public static final int BASE_USER_NAMESPACE = 3;
    
    /** Current distribution file name. This is filled in by the Ant build
     process to match the current distribution. */
    public static final String CURRENT_VERSION_NAME = "jibx_1_2_5-SNAPSHOT";
    
    /** Default prefix for automatic ID generation. */
    /*package*/ static final String DEFAULT_AUTOPREFIX = "id_";
    
    /** Minimum size to use map for index from type name. */
    private static final int TYPEMAP_MINIMUM_SIZE = 5;
    
    /** Table of defined bindings. */
    private static ArrayList s_bindings;
    
    /** Classes included in any binding. */
    private static ArrayMap s_mappedClasses;
    
    //
    // Static instances of predefined conversions.
    private static final StringConversion s_byteConversion =
        new PrimitiveStringConversion(Byte.TYPE, new Byte((byte)0), "B",
        "serializeByte", "parseByte", "attributeByte", "parseElementByte");
    private static final StringConversion s_charConversion =
        new PrimitiveStringConversion(Character.TYPE, new Character((char)0),
        "C", "serializeChar", "parseChar", "attributeChar", "parseElementChar");
    private static final StringConversion s_doubleConversion =
        new PrimitiveStringConversion(Double.TYPE, new Double(0.0d), "D",
        "serializeDouble", "parseDouble", "attributeDouble",
        "parseElementDouble");
    private static final StringConversion s_floatConversion =
        new PrimitiveStringConversion(Float.TYPE, new Float(0.0f), "F",
        "serializeFloat", "parseFloat", "attributeFloat", "parseElementFloat");
    private static final StringConversion s_intConversion =
        new PrimitiveStringConversion(Integer.TYPE, new Integer(0), "I",
        "serializeInt", "parseInt", "attributeInt", "parseElementInt");
    private static final StringConversion s_longConversion =
        new PrimitiveStringConversion(Long.TYPE, new Long(0L), "J",
        "serializeLong", "parseLong", "attributeLong", "parseElementLong");
    private static final StringConversion s_shortConversion =
        new PrimitiveStringConversion(Short.TYPE, new Short((short)0), "S",
        "serializeShort", "parseShort", "attributeShort", "parseElementShort");
    private static final StringConversion s_booleanConversion =
        new PrimitiveStringConversion(Boolean.TYPE, Boolean.FALSE, "Z",
        "serializeBoolean", "parseBoolean", "attributeBoolean",
        "parseElementBoolean");
    private static final StringConversion s_dateConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.Utility.serializeDateTime", 
        "org.jibx.runtime.Utility.deserializeDateTime", "java.util.Date");
//#!j2me{
    private static final StringConversion s_sqlDateConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.Utility.serializeSqlDate", 
        "org.jibx.runtime.Utility.deserializeSqlDate", "java.sql.Date");
    private static final StringConversion s_sqlTimeConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.Utility.serializeSqlTime", 
        "org.jibx.runtime.Utility.deserializeSqlTime", "java.sql.Time");
    private static final StringConversion s_timestampConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.Utility.serializeTimestamp", 
        "org.jibx.runtime.Utility.deserializeTimestamp", "java.sql.Timestamp");
    private static final StringConversion s_jodaLocalDateConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeLocalDate", 
        "org.jibx.runtime.JodaConvert.deserializeLocalDate",
        "org.joda.time.LocalDate");
    private static final StringConversion s_jodaZonedDateMidnightConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeZonedDateMidnight", 
        "org.jibx.runtime.JodaConvert.deserializeZonedDateMidnight",
        "org.joda.time.DateMidnight");
    private static final StringConversion s_jodaLocalUnzonedDateMidnightConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeUnzonedDateMidnight", 
        "org.jibx.runtime.JodaConvert.deserializeLocalDateMidnight",
        "org.joda.time.DateMidnight");
    private static final StringConversion s_jodaUTCDateMidnightConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeUTCDateMidnight", 
        "org.jibx.runtime.JodaConvert.deserializeUTCDateMidnight",
        "org.joda.time.DateMidnight");
    private static final StringConversion s_jodaUnzonedLocalTimeConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeUnzonedLocalTime", 
        "org.jibx.runtime.JodaConvert.deserializeLocalTime",
        "org.joda.time.LocalTime");
    private static final StringConversion s_jodaUnzonedUTCLocalTimeConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeUTCLocalTime", 
        "org.jibx.runtime.JodaConvert.deserializeLocalTime",
        "org.joda.time.LocalTime");
    private static final StringConversion s_jodaZonedDateTimeConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeZonedDateTime", 
        "org.jibx.runtime.JodaConvert.deserializeZonedDateTime",
        "org.joda.time.DateTime");
    private static final StringConversion s_jodaUTCDateTimeConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeUTCDateTime", 
        "org.jibx.runtime.JodaConvert.deserializeUTCDateTime",
        "org.joda.time.DateTime");
    private static final StringConversion s_jodaLocalDateTimeConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeZonedDateTime", 
        "org.jibx.runtime.JodaConvert.deserializeLocalDateTime",
        "org.joda.time.DateTime");
    private static final StringConversion s_jodaStrictLocalDateTimeConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeZonedDateTime", 
        "org.jibx.runtime.JodaConvert.deserializeStrictLocalDateTime",
        "org.joda.time.DateTime");
    private static final StringConversion s_jodaStrictUTCDateTimeConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.JodaConvert.serializeUTCDateTime", 
        "org.jibx.runtime.JodaConvert.deserializeStrictUTCDateTime",
        "org.joda.time.DateTime");
    private static final StringConversion s_java5DecimalConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.Java5DecimalConvert.serializeDecimal", 
        null,
        "java.math.BigDecimal");
//#j2me}
    private static final StringConversion s_base64Conversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.Utility.serializeBase64", 
        "org.jibx.runtime.Utility.deserializeBase64", "byte[]");
    private static final StringConversion s_wrappedBooleanConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.Utility.serializeBoolean", 
        "org.jibx.runtime.Utility.deserializeBoolean", "java.lang.Boolean");
    private static final StringConversion s_qnameConversion =
        new ObjectStringConversion(null,
        "org.jibx.runtime.QName.serialize", 
        "org.jibx.runtime.QName.deserialize", "org.jibx.runtime.QName");
    
    /*package*/ static final StringConversion s_stringConversion =
        new ObjectStringConversion(null, null, null, "java.lang.String");
    private static final StringConversion s_objectConversion =
        new ObjectStringConversion(null, null, null, "java.lang.Object");
    
    //
    // Constants for code generation
    
    private static final String FACTORY_SUFFIX = "Factory";
    private static final String FACTORY_INTERFACE =
        "org.jibx.runtime.IBindingFactory";
    private static final String FACTORY_BASE =
        "org.jibx.runtime.impl.BindingFactoryBase";
    private static final String[] FACTORY_INTERFACES =
    {
        FACTORY_INTERFACE
    };
    private static final String FACTORY_INSTNAME = "m_inst";
    private static final int PRIVATESTATIC_ACCESS = 
        Constants.ACC_PRIVATE | Constants.ACC_STATIC;
    private static final int PRIVATESTATICFINAL_ACCESS = 
        PRIVATESTATIC_ACCESS | Constants.ACC_FINAL;
    private static final String TYPEMAP_NAME = "m_typeMap";
    private static final String GETINST_METHODNAME = "getInstance";
    private static final String GETVERSION_METHODNAME = "getCompilerVersion";
    private static final String GETDISTRIB_METHODNAME =
        "getCompilerDistribution";
    private static final String GETTYPEINDEX_METHODNAME = "getTypeIndex";
    private static final String STRINGINT_MAPTYPE =
        "org.jibx.runtime.impl.StringIntHashMap";
    private static final String STRINGINTINIT_SIGNATURE = "(I)V";
    private static final String STRINGINTADD_METHOD =
        "org.jibx.runtime.impl.StringIntHashMap.add";
    private static final String STRINGINTADD_SIGNATURE =
        "(Ljava/lang/String;I)I";
    private static final String STRINGINTGET_METHOD =
        "org.jibx.runtime.impl.StringIntHashMap.get";
    private static final String STRINGINTGET_SIGNATURE =
        "(Ljava/lang/String;)I";
    private static final int MAX_STRING_LENGTH = 0x7FFF;
    private static final String CLASSLIST_METHOD_NAME = "getClassList";
    private static final String CLASSLIST_METHOD_SIGNATURE =
        "()Ljava/lang/String;";

    //
    // Actual instance data

    /** Binding name. */
    private final String m_name;
    
    /** Index number of this binding. */
    private final int m_index;

    /** Input binding flag. */
    private final boolean m_isInput;

    /** Output binding flag. */
    private final boolean m_isOutput;

    /** Use global ID values flag. */
    private final boolean m_isIdGlobal;

    /** Support forward references to IDs flag. */
    private final boolean m_isForwards;

    /** Generate souce tracking interface flag. */
    private final boolean m_isTrackSource;

    /** Generate marshaller/unmarshaller classes for top-level non-base abstract
     mappings flag. */
    private final boolean m_isForceClasses;
    
    /** Major version of binding. */
    private final int m_majorVersion;
    
    /** Minor version of binding. */
    private final int m_minorVersion;
    
    /** Add default constructors where needed flag. */
    private boolean m_isAddConstructors;

    /** Package for generated context factory. */
    private String m_targetPackage;
    
    /** File root for generated context factory. */
    private File m_targetRoot;
    
    /** Fully-qualified name of binding factory. */
    private String m_factoryName;
    
    /** Classes using unique (per class) identifiers. This is <code>null</code>
     and unused when using global ID values. */
    private ArrayMap m_uniqueIds;
    
    /** Namespaces URIs included in binding. */
    private ArrayMap m_namespaceUris;
    
    /** Original prefixes for namespaces. */
    private GrowableStringArray m_namespacePrefixes;
    
    /** High mark in prefixes from from precompiled base bindings. */
    private int m_highBasePrefix;
    
    /** Outer definition context with default definitions. */
    private DefinitionContext m_outerContext;
    
    /** Inner definition context constructed for binding. */
    private DefinitionContext m_activeContext;
    
    /** Flag for done assigning indexes to mapped classes. */
    private boolean m_isMappedDone;
    
    /** Flag for schema instance namespace used in binding. */
    private boolean m_isSchemaInstanceUsed;
    
    /** Next index number for marshaller/unmarshaller slots used in-line. */
    private int m_mumIndex;
    
    /** Classes handled by in-line marshaller/unmarshaller references. */
    private ArrayMap m_extraClasses;
    
    /** Marshaller classes used in-line. */
    private GrowableStringArray m_extraMarshallers;
    
    /** Unmarshaller classes used in-line. */
    private GrowableStringArray m_extraUnmarshallers;
    
    /** Precompiled base binding names used by this binding. */
    private GrowableStringArray m_baseBindings;
    
    /** Factory class names for precompiled base bindings (same order as binding
     names). */
    private GrowableStringArray m_baseBindingFactories;
    
    /** Hashes for base binding factories (same order as binding names). */
    private ArrayList m_baseHashes;
    
    /** Namespace index mapping tables for base bindings (same order as binding
     names). */
    private ArrayList m_baseNamespaceTables;
    
    /** Factory classes for base bindings of base bindings. */
    private GrowableStringArray m_closureFactories;
    
    /** Namespace index mapping tables for base bindings of base bindings (same
     order as factories). */
    private ArrayList m_closureNamespaceTables;
    
    /** Generated binding factory class. */
    private ClassFile m_factoryClass;

    /**
     * Constructor. Sets all defaults, including the default name provided, and
     * initializes the definition context for the outermost level of the
     * binding.
     *
     * @param name binding name
     * @param ibind input binding flag
     * @param obind output binding flag
     * @param tpack target package
     * @param glob global IDs flag
     * @param forward support forward referenced IDs flag
     * @param source add source tracking for unmarshalled objects flag
     * @param force create marshaller/unmarshaller classes for top-level
     * non-base mappings
     * @param add add default constructors where necessary flag
     * @param trim trim whitespace from simple values before conversion flag
     * @param major major version number
     * @param minor minor version number
     * @throws JiBXException if error in transformation
     */
    public BindingDefinition(String name, boolean ibind, boolean obind,
        String tpack, boolean glob, boolean forward, boolean source,
        boolean force, boolean add, boolean trim, int major, int minor)
        throws JiBXException {
        
        // handle basic initialization
        super(null);
        m_name = name;
        m_isInput = ibind;
        m_isOutput = obind;
        m_targetPackage = tpack;
        m_isIdGlobal = glob;
        m_isForwards = forward;
        m_isTrackSource = source;
        m_isForceClasses = force;
        m_isAddConstructors = add;
        m_majorVersion = major;
        m_minorVersion = minor;
        m_baseBindings = new GrowableStringArray();
        m_baseBindingFactories = new GrowableStringArray();
        m_baseHashes = new ArrayList();
        m_baseNamespaceTables = new ArrayList();
        m_closureFactories = new GrowableStringArray();
        m_closureNamespaceTables = new ArrayList();
        
        // set base class defaults
        m_styleDefault = ValueChild.ELEMENT_STYLE;
        m_autoLink = BindingBuilder.LINK_FIELDS;
        m_accessLevel = BindingBuilder.ACC_PRIVATE;
        m_nameStyle = BindingBuilder.NAME_HYPHENS;
        
        // initialize the contexts
        m_outerContext = m_activeContext = new DefinitionContext(this);
        m_activeContext = new DefinitionContext(this);
        m_namespaceUris = new ArrayMap();
        m_namespaceUris.findOrAdd("");
        m_namespacePrefixes = new GrowableStringArray();
        m_namespacePrefixes.add("");
        m_outerContext.addNamespace(NamespaceDefinition.buildNamespace
            ("http://www.w3.org/XML/1998/namespace", "xml"));
        getNamespaceUriIndex
            ("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        
        // build the default converters in outer context, with appropriate trim
        String conv = trim ? "org.jibx.runtime.WhitespaceConversions.trim" : null;
        m_outerContext.setDefaultConversion(new QName("byte.default"),
            s_byteConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("char.default"),
            s_charConversion.derive(null, null, conv, null, null));
        StringConversion schar = s_charConversion.derive("char",
            "org.jibx.runtime.Utility.serializeCharString", null,
            "org.jibx.runtime.Utility.parseCharString", null);
        m_outerContext.setNamedConversion(new QName("char.string"), schar);
        m_outerContext.setDefaultConversion(new QName("double.default"),
            s_doubleConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("float.default"),
            s_floatConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("int.default"),
            s_intConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("long.default"),
            s_longConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("short.default"),
            s_shortConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("boolean.default"),
            s_booleanConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("Date.default"),
            s_dateConversion.derive(null, null, conv, null, null));
//#!j2me{
        m_outerContext.setDefaultConversion(new QName("SqlDate.default"),
            s_sqlDateConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("SqlTime.default"),
            s_sqlTimeConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("Timestamp.default"),
            s_timestampConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("LocalDate.default"),
            s_jodaLocalDateConversion.derive(null, null, conv, null, null));
        m_outerContext.setNamedConversion(new QName("DateMidnight.zoned"),
            s_jodaZonedDateMidnightConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("DateMidnight.local"),
            s_jodaLocalUnzonedDateMidnightConversion.derive(null, null, conv, null, null));
        m_outerContext.setNamedConversion(new QName("DateMidnight.UTC"),
            s_jodaUTCDateMidnightConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("LocalTime.local"),
            s_jodaUnzonedLocalTimeConversion.derive(null, null, conv, null, null));
        m_outerContext.setNamedConversion(new QName("LocalTime.UTC"),
            s_jodaUnzonedUTCLocalTimeConversion.derive(null, null, conv, null, null));
        m_outerContext.setNamedConversion(new QName("DateTime.zoned"),
            s_jodaZonedDateTimeConversion.derive(null, null, conv, null, null));
        m_outerContext.setNamedConversion(new QName("DateTime.UTC"),
            s_jodaUTCDateTimeConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("DateTime.local"),
            s_jodaLocalDateTimeConversion.derive(null, null, conv, null, null));
        m_outerContext.setNamedConversion(new QName("DateTime.strict-local"),
            s_jodaStrictLocalDateTimeConversion.derive(null, null, conv, null, null));
        m_outerContext.setNamedConversion(new QName("DateTime.strict-UTC"),
            s_jodaStrictUTCDateTimeConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("BigDecimal.java5"),
            s_java5DecimalConversion.derive(null, null, conv, null, null));
//#j2me}
        m_outerContext.setDefaultConversion(new QName("byte-array.default"),
            s_base64Conversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("Boolean.default"),
            s_wrappedBooleanConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("QName.default"),
            s_qnameConversion.derive(null, null, conv, null, null));
        m_outerContext.setDefaultConversion(new QName("String.default"),
            s_stringConversion);
        m_outerContext.setDefaultConversion(OBJECT_DEFAULT_NAME,
            s_objectConversion.derive(null, null, conv, null, null));
        
        // add this binding to list
        m_index = s_bindings.size();
        s_bindings.add(this);
    }
    
    /**
     * Get class linked to binding element. Implementation of
     * {@link org.jibx.binding.def.IContainer} interface, just returns
     * <code>null</code> in this case.
     *
     * @return information for class linked by binding
     */
    public BoundClass getBoundClass() {
        return null;
    }

    /**
     * Get default style for value expression. Implementation of
     * {@link org.jibx.binding.def.IContainer} interface.
     *
     * @return default style type for values
     */
    public int getStyleDefault() {
        return m_styleDefault;
    }

    /**
     * Set ID property. This parent binding component interface method should
     * never be called for the binding definition, and will throw a runtime
     * exception if it is called.
     *
     * @param child child defining the ID property
     * @return <code>false</code>
     */
    public boolean setIdChild(IComponent child) {
        throw new IllegalStateException("Internal error - setIdChild for root");
    }

    /**
     * Get default package used for code generation.
     *
     * @return default code generation package
     */
    public String getDefaultPackage() {
        return m_targetPackage;
    }

    /**
     * Get root directory for default code generation package.
     *
     * @return root for default code generation
     */
    public File getDefaultRoot() {
        return m_targetRoot;
    }

    /**
     * Set location for binding factory class generation.
     *
     * @param tpack target package for generated context factory
     * @param root target root for generated context factory
     */
    public void setFactoryLocation(String tpack, File root) {
        m_targetPackage = tpack;
        m_targetRoot = root;
    }

    /**
     * Get index number of binding.
     *
     * @return index number for this binding definition
     */
    public int getIndex() {
        return m_index;
    }

    /**
     * Check if binding is defined for unmarshalling.
     *
     * @return <code>true</code> if defined, <code>false</code> if not
     */
    public boolean isInput() {
        return m_isInput;
    }

    /**
     * Check if binding is defined for marshalling.
     *
     * @return <code>true</code> if defined, <code>false</code> if not
     */
    public boolean isOutput() {
        return m_isOutput;
    }

    /**
     * Check if global ids are used by binding.
     *
     * @return <code>true</code> if defined, <code>false</code> if not
     */
    public boolean isIdGlobal() {
        return m_isIdGlobal;
    }

    /**
     * Check if forward ids are supported by unmarshalling binding.
     *
     * @return <code>true</code> if supported, <code>false</code> if not
     */
    public boolean isForwards() {
        return m_isForwards;
    }

    /**
     * Check if source tracking is supported by unmarshalling binding.
     *
     * @return <code>true</code> if defined, <code>false</code> if not
     */
    public boolean isTrackSource() {
        return m_isTrackSource;
    }
    
    /**
     * Check if default constructor generation is enabled.
     *
     * @return <code>true</code> if default constructor generation enabled,
     * <code>false</code> if not
     */
    public boolean isAddConstructors() {
        return m_isAddConstructors;
    }

    /**
     * Get prefix for method or class generation.
     *
     * @return prefix for names created by this binding
     */
    public String getPrefix() {
        return BindingDirectory.GENERATE_PREFIX + m_name;
    }

    /**
     * Add mapping name to binding. If the name is not already included in any
     * binding it is first added to the list of mapping names. This method is
     * intended for use with &lt;mapping&gt; definitions. It is an error to call
     * this method after calling the {@link #getMarshallerUnmarshallerName}
     * method.
     *
     * @param name mapping name (type name if given, otherwise the fully
     * qualified mapped class name)
     */
    public void addMappingName(String name) {
        if (m_isMappedDone) {
            throw new IllegalStateException
                ("Internal error: Call out of sequence");
        } else {
            s_mappedClasses.findOrAdd(name);
        }
    }

    /**
     * Get marshaller/unmarshaller name in binding. The same class may have more
     * than one marshaller/unmarshaller pair defined, so this uses the supplied
     * class name as a base and appends a numeric suffix as necessary to
     * generate a unique name. After the name has been assigned by this method,
     * the {@link #setMarshallerUnmarshallerClasses} method must be used to set
     * the actual class names.
     *
     * @param clas fully qualified name of class handled by
     * marshaller/unmarshaller
     * @return unique name for marshaller/unmarshaller pair
     */
    public String getMarshallerUnmarshallerName(String clas) {
        if (!m_isMappedDone) {
            m_isMappedDone = true;
            m_mumIndex = s_mappedClasses.size();
            if (m_extraClasses == null) {
                m_extraClasses = new ArrayMap();
                m_extraMarshallers = new GrowableStringArray();
                m_extraUnmarshallers = new GrowableStringArray();
            }
        }
        int variant = 0;
        String name = clas;
        do {
            name = clas + '-' + variant++;
        } while (m_extraClasses.find(name) >= 0);
        m_extraClasses.findOrAdd(name);
        m_extraMarshallers.add(null);
        m_extraUnmarshallers.add(null);
        return name;
    }

    /**
     * Set marshaller and unmarshaller class names.
     *
     * @param name assigned marshaller/unmarshaller name
     * @param mclas fully qualified name of marshaller class
     * @param uclas fully qualified name of unmarshaller class
     */
    public void setMarshallerUnmarshallerClasses(String name, String mclas,
        String uclas) {
        int index = m_extraClasses.find(name);
        m_extraMarshallers.set(index, mclas);
        m_extraUnmarshallers.set(index, uclas);
    }

    /**
     * Get index for ID'ed class from binding. If the class is not already
     * included it is first added to the binding. If globally unique IDs are
     * used this always returns <code>0</code>.
     *
     * @param name fully qualified name of ID'ed class
     * @return index number of class
     */
    public int getIdClassIndex(String name) {
        if (m_isIdGlobal) {
            return 0;
        } else {
            if (m_uniqueIds == null) {
                m_uniqueIds = new ArrayMap();
            }
            return m_uniqueIds.findOrAdd(name);
        }
    }

    /**
     * Get index for namespace URI in binding. If the URI is not already
     * included it is first added to the binding. The empty namespace URI
     * is always given index number <code>0</code>.
     *
     * @param uri namespace URI to be included in binding
     * @param prefix prefix used with namespace
     * @return index number of namespace
     */
    public int getNamespaceUriIndex(String uri, String prefix) {
        int index = m_namespaceUris.findOrAdd(uri);
        int size = m_namespacePrefixes.size();
        if (index == size) {
            m_namespacePrefixes.add(prefix);
        } else if (index > size) {
            throw new IllegalStateException("Internal error - prefixes not matched with namespaces");
        }
        return index;
    }

    /**
     * Set flag for schema instance namespace used in binding.
     */
    public void setSchemaInstanceUsed() {
        m_isSchemaInstanceUsed = true;
    }
    
    /**
     * Add a precompiled binding reference to this binding. This records the
     * reference and makes sure that all the namespaces used in the precompiled
     * binding are defined in this binding, also generating a namespace index
     * mapping table if this binding supports output and the indexes differ.
     *
     * @param factory actual binding factory for precompiled binding
     * @param major required major version number
     * @param minor required minor version number
     * @return namespace index translation table (<code>null</code> if none)
     */
    public int[] addPrecompiledBinding(IBindingFactory factory, int major,
        int minor) {
        
        // add basic information for precompiled base binding
        m_baseBindings.add(factory.getBindingName());
        m_baseBindingFactories.add(factory.getClass().getName());
        m_baseHashes.add(new Integer(factory.getHash()));
        
        // add all namespaces from precompiled binding
        String[] namespaces = factory.getNamespaces();
        String[] prefixes = factory.getPrefixes();
        int[] indexes = new int[namespaces.length];
        boolean xlate = false;
        for (int i = 1; i < namespaces.length; i++) {
            int index = getNamespaceUriIndex(namespaces[i], prefixes[i]);
            indexes[i] = index;
            if (i != index) {
                xlate = true;
            }
        }
        m_highBasePrefix = m_namespacePrefixes.size();
        if (xlate) {
            
            // add translation table to list of tables for bindings
            m_baseNamespaceTables.add(indexes);
            
            // generate substitute translation tables for all base bindings
            String[] basefacts = factory.getBaseBindingFactories();
            Map basemap = factory.getNamespaceTranslationTableMap();
            for (int i = 0; i < basefacts.length; i++) {
                String factname = basefacts[i];
                int[] basetable = (int[])basemap.get(factname);
                if (basetable != null) {
                    int[] baseindexes = new int[basetable.length];
                    for (int j = 0; j < basetable.length; j++) {
                        baseindexes[j] = indexes[basetable[j]];
                    }
                    m_closureFactories.add(factname);
                    m_closureNamespaceTables.add(baseindexes);
                }
            }
            return indexes;
            
        } else {
            
            // no need for translation table, just add null to list of tables
            m_baseNamespaceTables.add(null);
            return null;
            
        }
    }
    
    /**
     * Fix the prefixes for namespaces imported from precompiled base bindings.
     * If there are no namespaces from precompiled base bindings, or these
     * namespaces use prefixes which are unique from each other and from those
     * used in this binding, nothing is done. If there are conflicts or
     * namespaces used without prefixes this sets unique prefixes for each
     * namespace.
     */
    private void fixPrefixes() {
        if (m_highBasePrefix > 0) {
            Set prefset = new HashSet();
            prefset.add("");
            for (int i = m_highBasePrefix; i < m_namespacePrefixes.size(); i++) {
                prefset.add((String)m_namespacePrefixes.get(i));
            }
            int genindex = 0;
            for (int i = BASE_USER_NAMESPACE; i < m_highBasePrefix; i++) {
                String prefix = (String)m_namespacePrefixes.get(i);
                while (prefset.contains(prefix)) {
                    StringBuffer buff = new StringBuffer();
                    buff.append((char)('a' + genindex++ % 26));
                    int remain = genindex / 26;
                    while (remain > 0) {
                        int next = remain % 36;
                        if (next < 10) {
                            buff.append((char)('0' + next));
                        } else {
                            buff.append((char)('a' + next));
                        }
                        remain /= 36;
                    }
                    prefix = buff.toString();
                }
                prefset.add(prefix);
                m_namespacePrefixes.set(i, prefix);
            }
        }
    }
    
    /**
     * Get the prefix assigned for a namespace. This is intended mainly for use
     * with precompiled bindings, where the {@link #fixPrefixes()} method may
     * change the initial prefixes (if any) in order to avoid conflicts.
     *
     * @param uri namespace URI
     * @return prefix for namespace
     */
    public String getPrefix(String uri) {
        int index = m_namespaceUris.find(uri);
        if (index >= 0) {
            return (String)m_namespacePrefixes.get(index);
        } else {
            throw new IllegalStateException("Internal error - URI not registered");
        }
    }

    /**
     * Build a class or method name blob from an array of fully-qualified class
     * and/or method names. The returned string consists of compacted
     * fully-qualified names separated by '|' delimiter characters. If some
     * number of package (and potentially class) name levels are the same as the
     * last name, these components are replaced with simple '.' characters (and
     * '$' characters, in the case of class name components) in the compacted
     * name. <code>null</code> values are represented as empty names.
     *
     * @param names fully-qualified class and/or method names list
     * @return compacted name blob
     */
    private static String buildClassNamesBlob(String[] names) {
        StringBuffer buff = new StringBuffer();
        String last = "";
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                buff.append('|');
            }
            String name = names[i];
            if (name != null) {
                int base = 0;
                int limit = Math.min(last.length(), name.length());
                int scan = -1;
                while (++scan < limit) {
                    char chr = last.charAt(scan);
                    if (chr == name.charAt(scan)) {
                        if (chr == '.') {
                            buff.append('.');
                            base = scan + 1;
                        } else if (chr == '$') {
                            buff.append('$');
                            base = scan + 1;
                        }
                    } else {
                        break;
                    }
                }
                if (scan < limit ||
                    (scan == limit && last.length() != name.length())) {
                    buff.append(name.substring(base));
                }
                last = name;
            }
        }
        return buff.toString();
    }
    
    /**
     * Build a class or method name blob from a list of fully-qualified class
     * and/or method names. This just converts the list to an array and
     * delegates to {@link #buildClassNamesBlob(String[])}.
     *
     * @param names fully-qualified class and/or method names list
     * @return compacted name blob
     */
    private static String buildClassNamesBlob(List names) {
        String[] strings = (String[])names.toArray(new String[names.size()]);
        return buildClassNamesBlob(strings);
    }
    
    /**
     * Build a namespace index blob from an array of namespace URIs. The
     * returned string consists of one character per namespace, giving the index
     * of the namespace URI within the array of definitions, biased by +2 to
     * avoid use of null characters (with +1 used for <code>null</code> values).
     *
     * @param uris table of namespaces defined in binding
     * @param nss namespaces for index blob
     * @return index blob
     */
    private static String buildNamespaceIndexBlob(String[] uris, String[] nss) {
        Map indexmap = new HashMap();
        for (int i = 0; i < uris.length; i++) {
            indexmap.put(uris[i], IntegerCache.getInteger(i+2));
        }
        StringBuffer buff = new StringBuffer(nss.length);
        for (int i = 0; i < nss.length; i++) {
            String uri = nss[i];
            if (uri == null) {
                buff.append((char)1);
            } else {
                buff.append((char)((Integer)indexmap.get(uri)).intValue());
            }
        }
        return buff.toString();
    }
    
    /**
     * Build a name blob from an array of names. The returned string consists of
     * names separated by '|' delimiter characters. <code>null</code> values are
     * represented as empty names.
     *
     * @param names names for blob
     * @return name blob
     */
    private static String buildNamesBlob(String[] names) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < names.length; i++) {
            if (i > 0) {
                buff.append('|');
            }
            if (names[i] != null) {
                buff.append(names[i]);
            }
        }
        return buff.toString();
    }
    
    /**
     * Convert an array of int values into a string blob.
     *
     * @param ints
     * @return string with int values as characters
     */
    private static String buildIntsBlob(int[] ints) {
        char[] chars = new char[ints.length];
        for (int i = 0; i < ints.length; i++) {
            int value = ints[i];
            if (value < 0 || value >= 0xFFFF) {
                throw new IllegalArgumentException("Internal error - only 16-bit values supported at present");
            }
            chars[i] = (char)(value + 1);
        }
        return new String(chars);
    }
    
    /**
     * Convert a list of <code>Integer</code> values into a string blob. This is
     * just a convenience wrapper for {@link #buildIntsBlob(int[])}.
     *
     * @param values
     * @return string with int values as characters
     */
    private static String buildIntsBlob(List values) {
        int[] ints = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            ints[i] = ((Integer)values.get(i)).intValue();
        }
        return buildIntsBlob(ints);
    }
    
    /**
     * Generate code to load a string value, which may be longer than the
     * maximum string length. This either loads the string directly (if within
     * the limit) or recreates it by concatenating two or more shorter strings.
     *
     * @param string
     * @param mb
     */
    private static void codegenString(String string, MethodBuilder mb) {
        if (string.length() < MAX_STRING_LENGTH) {
            mb.appendLoadConstant(string);
        } else {
            // build a StringBuffer, then loop loading each component constant
            //  string in turn and appending it; when all strings done, call
            //  StringBuffer.toString()
            mb.appendCreateNew("java.lang.StringBuffer");
            mb.appendDUP();
            mb.appendLoadConstant(string.length());
            mb.appendCallInit("java.lang.StringBuffer", "(I)V");
            int base = 0;
            while (base < string.length()) {
                int end = Math.min(base + MAX_STRING_LENGTH, string.length());
                mb.appendLoadConstant(string.substring(base, end));
                mb.appendCallVirtual("java.lang.StringBuffer.append", 
                    "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
                base = end;
            }
            mb.appendCallVirtual("java.lang.StringBuffer.toString",
                "()Ljava/lang/String;");
        }
    }
    
    /**
     * Get the fully-qualified name of the binding factory class for this
     * binding.
     *
     * @return class name
     */
    public String getFactoryName() {
        return m_factoryName;
    }

    /**
     * Generate code. First sets linkages and executes code generation for
     * each top-level mapping defined in this binding, which in turn propagates
     * the code generation all the way down. Then generates the actual binding
     * factory for this binding.
     *
     * @param verbose1 flag for verbose output from first pass
     * @param verbose2 flag for verbose output from second pass
     * @throws JiBXException if error in code generation
     */
    public void generateCode(boolean verbose1, boolean verbose2) throws JiBXException {
        
        // check schema instance namespace usage
        if (m_isSchemaInstanceUsed) {
            NamespaceDefinition xsins = NamespaceDefinition.buildNamespace
                ("http://www.w3.org/2001/XMLSchema-instance", "xsi");
            ArrayList mappings = m_activeContext.getMappings();
            for (int i = 0; i < mappings.size(); i++) {
                Object mapping = mappings.get(i);
                if (mapping instanceof MappingDefinition) {
                    ((MappingDefinition)mapping).addNamespace(xsins);
                }
            }
        }
        
        // set the class name to be used for binding factory
        if (m_targetPackage.length() == 0) {
            m_factoryName = getPrefix() + FACTORY_SUFFIX;
        } else {
            m_factoryName = m_targetPackage + '.' + getPrefix() + FACTORY_SUFFIX;
        }
        
        // handle basic linkage and child code generation
        BoundClass.setModify(m_targetRoot, m_targetPackage, m_name);
        fixPrefixes();
        m_activeContext.linkMappings();
        m_activeContext.setLinkages();
        m_activeContext.generateCode(verbose1, m_isForceClasses);
        if (verbose2) {
            System.out.println("After linking view of binding " + m_name + ':');
            print();
        }
        
        // build the binding factory class
        ClassFile base = ClassCache.requireClassFile(FACTORY_BASE);
        ClassFile cf = new ClassFile(m_factoryName, m_targetRoot, base,
            Constants.ACC_PUBLIC, FACTORY_INTERFACES);
        
        // add static field for instance
        ClassItem inst = cf.addField(FACTORY_INTERFACE,
            FACTORY_INSTNAME, PRIVATESTATIC_ACCESS);
        
        // add private method to return binding classes blob (replaced later)
        MethodBuilder mb = new ExceptionMethodBuilder(CLASSLIST_METHOD_NAME,
            Type.STRING, new Type[0], cf,
            Constants.ACC_PRIVATE|Constants.ACC_STATIC);
        mb.appendACONST_NULL();
        mb.appendReturn(Type.STRING);
        mb.codeComplete(false);
        ClassItem clasblobmeth = mb.addMethod();
        
        // add the private constructor method
        mb = new ExceptionMethodBuilder("<init>", Type.VOID, new Type[0], cf,
            Constants.ACC_PRIVATE);
        
        // start superclass constructor call with name/versions, classes used
        mb.appendLoadLocal(0);
        mb.appendLoadConstant(m_name);
        mb.appendLoadConstant(m_majorVersion);
        mb.appendLoadConstant(m_minorVersion);
        mb.appendCall(clasblobmeth);
        
        // load count of mapped classes
        int count = s_mappedClasses.size();
        int mcnt = m_extraClasses == null ? count : count +
            m_extraClasses.size();
        
        // create argument blob of mapped class names
        String[] names = new String[mcnt];
        for (int i = 0; i < count; i++) {
            names[i] = (String)s_mappedClasses.get(i);
        }
        for (int i = count; i < mcnt; i++) {
            names[i] = (String)m_extraClasses.get(i-count);
        }
        codegenString(buildClassNamesBlob(names), mb);
        
        // create argument blob of unmarshaller class names
        if (m_isInput) {
            for (int i = 0; i < count; i++) {
                String cname = (String)s_mappedClasses.get(i);
                IMapping map = m_activeContext.getMappingAtLevel(cname);
                if (map != null && map.getUnmarshaller() != null) {
                    names[i] = map.getUnmarshaller().getName();
                } else {
                    names[i] = null;
                }
            }
            for (int i = count; i < mcnt; i++) {
                names[i] = (String)m_extraUnmarshallers.get(i-count);
            }
            codegenString(buildClassNamesBlob(names), mb);
        } else {
            mb.appendACONST_NULL();
        }
        
        // create argument blob of marshaller class names
        if (m_isOutput) {
            for (int i = 0; i < count; i++) {
                String cname = (String)s_mappedClasses.get(i);
                IMapping map = m_activeContext.getMappingAtLevel(cname);
                if (map != null && map.getMarshaller() != null) {
                    names[i] = map.getMarshaller().getName();
                } else {
                    names[i] = null;
                }
            }
            for (int i = count; i < mcnt; i++) {
                names[i] = (String)m_extraMarshallers.get(i-count);
            }
            codegenString(buildClassNamesBlob(names), mb);
        } else {
            mb.appendACONST_NULL();
        }
        
        // create argument array of namespace URIs
        String[] nsuris = new String[m_namespaceUris.size()];
        mb.appendLoadConstant(m_namespaceUris.size());
        mb.appendCreateArray("java.lang.String");
        for (int i = 0; i < m_namespaceUris.size(); i++) {
            mb.appendDUP();
            mb.appendLoadConstant(i);
            String uri = (String)m_namespaceUris.get(i);
            mb.appendLoadConstant(uri);
            mb.appendAASTORE();
            nsuris[i] = uri;
        }
        
        // create argument array of namespace prefixes
        if (m_isOutput) {
            mb.appendLoadConstant(m_namespacePrefixes.size());
            mb.appendCreateArray("java.lang.String");
            for (int i = 0; i < m_namespacePrefixes.size(); i++) {
                mb.appendDUP();
                mb.appendLoadConstant(i);
                mb.appendLoadConstant((String)m_namespacePrefixes.get(i));
                mb.appendAASTORE();
            }
        } else {
            mb.appendACONST_NULL();
        }
        
        // create argument blobs of globally mapped element names and URIs
        names = new String[count];
        String[] namespaces = new String[count];
        for (int i = 0; i < count; i++) {
            String cname = (String)s_mappedClasses.get(i);
            IMapping map = m_activeContext.getMappingAtLevel(cname);
            if (map != null) {
                NameDefinition ndef = map.getName();
                if (ndef != null) {
                    names[i] = ndef.getName();
                    namespaces[i] = ndef.getNamespace();
                }
            }
        }
        codegenString(buildNamesBlob(names), mb);
        mb.appendLoadConstant(buildNamespaceIndexBlob(nsuris, namespaces));
        
        // create argument array of class names with unique IDs
        if (m_uniqueIds != null && m_uniqueIds.size() > 0) {
            mb.appendLoadConstant(m_uniqueIds.size());
            mb.appendCreateArray("java.lang.String");
            for (int i = 0; i < m_uniqueIds.size(); i++) {
                mb.appendDUP();
                mb.appendLoadConstant(i);
                mb.appendLoadConstant((String)m_uniqueIds.get(i));
                mb.appendAASTORE();
            }
        } else {
            mb.appendACONST_NULL();
        }
        
        // create argument blobs of abstract mapping information
        GrowableStringArray allnames = new GrowableStringArray();
        int abmapcnt = 0;
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < count; i++) {
            String tname = (String)s_mappedClasses.get(i);
            IMapping map = m_activeContext.getMappingAtLevel(tname);
            if (map != null && map.isAbstract()) {
                ITypeBinding bind = map.getBinding();
                allnames.add(tname);
                allnames.add(map.getBoundType());
                allnames.add(bind.getCreateMethod());
                allnames.add(bind.getCompleteMethod());
                allnames.add(bind.getPrepareMethod());
                allnames.add(bind.getAttributePresentTestMethod());
                allnames.add(bind.getAttributeUnmarshalMethod());
                allnames.add(bind.getAttributeMarshalMethod());
                allnames.add(bind.getContentPresentTestMethod());
                allnames.add(bind.getContentUnmarshalMethod());
                allnames.add(bind.getContentMarshalMethod());
                abmapcnt++;
                ArrayList nss = map.getNamespaces();
                if (nss == null) {
                    buff.append((char)1);
                } else {
                    buff.append((char)(nss.size()+1));
                    for (int j = 0; j < nss.size(); j++) {
                        NamespaceDefinition nsdef =
                            (NamespaceDefinition)nss.get(j);
                        buff.append((char)(nsdef.getIndex()+1));
                    }
                }
            }
        }
        codegenString(buildClassNamesBlob(allnames.toArray()), mb);
        codegenString(buff.toString(), mb);
        
        // create argument blobs of precompiled base binding names and factories
        int basecount = m_baseBindings.size();
        codegenString(buildNamesBlob(m_baseBindings.toArray()), mb);
        String namesblob = buildClassNamesBlob(m_baseBindingFactories.toArray());
        if (m_closureFactories.size() > 0) {
            namesblob += "|" + buildClassNamesBlob(m_closureFactories.toArray());
        }
        codegenString(namesblob, mb);
        
        // create argument blob for base binding factory hashes
        char[] hashchars = new char[basecount*2];
        for (int i = 0; i < basecount; i++) {
            int hash = ((Integer)m_baseHashes.get(i)).intValue();
            hashchars[i*2] = (char)(hash >> 16);
            hashchars[i*2+1] = (char)hash;
        }
        codegenString(new String(hashchars), mb);
        
        // create array of blobs of base binding namespace translation tables
        mb.appendLoadConstant(basecount + m_closureNamespaceTables.size());
        mb.appendCreateArray("java.lang.String");
        for (int i = 0; i < basecount; i++) {
            int[] table = (int[])m_baseNamespaceTables.get(i);
            if (table != null) {
                mb.appendDUP();
                mb.appendLoadConstant(i);
                mb.appendLoadConstant(buildIntsBlob(table));
                mb.appendAASTORE();
            }
        }
        for (int i = 0; i < m_closureNamespaceTables.size(); i++) {
            int[] table = (int[])m_closureNamespaceTables.get(i);
            mb.appendDUP();
            mb.appendLoadConstant(i+basecount);
            mb.appendLoadConstant(buildIntsBlob(table));
            mb.appendAASTORE();
        }
        
        // call the base class constructor
        mb.appendCallInit(FACTORY_BASE,
            "(Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;" +
            "Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;" +
            "[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;" +
            "[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;" +
            "Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;" +
            "[Ljava/lang/String;)V");
        
        // get class names for types (abstract non-base mappings)
        GrowableStringArray tnames = new GrowableStringArray();
        if (m_isForceClasses) {
            for (int i = 0; i < count; i++) {
                String cname = (String)s_mappedClasses.get(i);
                IMapping map = m_activeContext.getMappingAtLevel(cname);
                if (map != null && map.isAbstract() && !map.isBase()) {
                    String tname = map.getTypeName();
                    if (tname == null) {
                        tname = cname;
                    }
                    tnames.add(tname);
                }
            }
        }
        
        // check if map needed for types
        ClassItem tmap = null;
        if (tnames.size() >= TYPEMAP_MINIMUM_SIZE) {
            
            // create field for map
            tmap = cf.addPrivateField(STRINGINT_MAPTYPE, TYPEMAP_NAME);
            
            // initialize with appropriate size
            mb.appendLoadLocal(0);
            mb.appendCreateNew(STRINGINT_MAPTYPE);
            mb.appendDUP();
            mb.appendLoadConstant(tnames.size());
            mb.appendCallInit(STRINGINT_MAPTYPE, STRINGINTINIT_SIGNATURE);
            
            // add all values to map
            for (int i = 0; i < tnames.size(); i++) {
                int index = s_mappedClasses.find(tnames.get(i));
                if (index >= 0) {
                    mb.appendDUP();
                    mb.appendLoadConstant((String)tnames.get(i));
                    mb.appendLoadConstant(index);
                    mb.appendCallVirtual(STRINGINTADD_METHOD,
                        STRINGINTADD_SIGNATURE);
                    mb.appendPOP();
                }
            }
            mb.appendPutField(tmap);
        }
        
        // finish with return from constructor
        mb.appendReturn();
        mb.codeComplete(false);
        mb.addMethod();
        
        // add the compiler version access method
        mb = new ExceptionMethodBuilder(GETVERSION_METHODNAME,
            Type.INT, new Type[0], cf, Constants.ACC_PUBLIC);
        mb.appendLoadConstant(IBindingFactory.CURRENT_VERSION_NUMBER);
        mb.appendReturn("int");
        mb.codeComplete(false);
        mb.addMethod();
        
        // add the compiler distribution access method
        mb = new ExceptionMethodBuilder(GETDISTRIB_METHODNAME,
            Type.STRING, new Type[0], cf, Constants.ACC_PUBLIC);
        mb.appendLoadConstant(CURRENT_VERSION_NAME);
        mb.appendReturn(Type.STRING);
        mb.codeComplete(false);
        mb.addMethod();
        
        // add the type mapping index lookup method
        mb = new ExceptionMethodBuilder(GETTYPEINDEX_METHODNAME,
            Type.INT, new Type[] { Type.STRING }, cf, Constants.ACC_PUBLIC);
        if (tnames.size() > 0) {
            if (tmap == null) {
                
                // generate in-line compares for mapping
                for (int i = 0; i < tnames.size(); i++) {
                    int index = s_mappedClasses.find(tnames.get(i));
                    if (index >= 0) {
                        mb.appendLoadLocal(1);
                        mb.appendLoadConstant((String)tnames.get(i));
                        mb.appendCallVirtual("java.lang.String.equals",
                            "(Ljava/lang/Object;)Z");
                        BranchWrapper onfail = mb.appendIFEQ(this);
                        mb.appendLoadConstant(index);
                        mb.appendReturn(Type.INT);
                        mb.targetNext(onfail);
                    }
                }
                mb.appendLoadConstant(-1);
                
            } else {
                
                // use map constructed in initializer
                mb.appendLoadLocal(0);
                mb.appendGetField(tmap);
                mb.appendLoadLocal(1);
                mb.appendCallVirtual(STRINGINTGET_METHOD,
                    STRINGINTGET_SIGNATURE);
                
            }
        } else {
            
            // no types to handle, just always return failure
            mb.appendLoadConstant(-1);
            
        }
        mb.appendReturn(Type.INT);
        mb.codeComplete(false);
        mb.addMethod();
        
        // finish with instance creation method
        mb = new ExceptionMethodBuilder(GETINST_METHODNAME,
            ClassItem.typeFromName(FACTORY_INTERFACE), new Type[0], cf,
            (short)(Constants.ACC_PUBLIC | Constants.ACC_STATIC));
        mb.appendGetStatic(inst);
        BranchWrapper ifdone = mb.appendIFNONNULL(this);
        mb.appendCreateNew(cf.getName());
        mb.appendDUP();
        mb.appendCallInit(cf.getName(), "()V");
        mb.appendPutStatic(inst);
        mb.targetNext(ifdone);
        mb.appendGetStatic(inst);
        mb.appendReturn(FACTORY_INTERFACE);
        mb.codeComplete(false);
        mb.addMethod();
        
        // add factory class to generated registry
        cf.codeComplete();
        MungedClass.addModifiedClass(cf);
        m_factoryClass = cf;
        
        // record the binding factory in each top-level mapped class
        ArrayList maps = m_activeContext.getMappings();
        if (maps != null) {
            for (int i = 0; i < maps.size(); i++) {
                IMapping map = (IMapping)maps.get(i);
                if (map instanceof MappingBase) {
                    BoundClass bound = ((MappingBase)map).getBoundClass();
                    if (bound.getClassFile().isModifiable()) {
                        bound.addFactory(m_factoryName);
                    }
                }
            }
        }
    }

    /**
     * Generate code. This version preserves compatibility with the older form
     * of the call, always passing <code>false</code> for the second-pass
     * verbose flag. See {@link #generateCode(boolean, boolean)} for details of
     * processing.
     *
     * @param verbose flag for verbose output from first pass
     * @throws JiBXException if error in code generation
     */
    public void generateCode(boolean verbose) throws JiBXException {
        generateCode(verbose, false);
    }

    /**
     * Add the list of classes used by the binding compiler to the binding
     * factory. This needs to be done as a separate step after the normal
     * binding process has completed in order to make sure that the full set of
     * classes is available. Ugly, but necessary because the class handling
     * doesn't finalize newly-generated class names until they're actually
     * written (too much sophistication, in retrospect).
     * 
     * @param adds classes added by binding
     * @param keeps classes used but kept unchanged by binding
     */
    public void addClassList(ClassFile[] adds, ClassFile[] keeps) {
        
        // build a sorted tree of all the class names used in the binding
        Set tree = new TreeSet();
        int addcount = adds.length;
        for (int i = 0; i < addcount; i++) {
            tree.add(adds[i].getName());
        }
        for (int i = 0; i < keeps.length; i++) {
            tree.add(keeps[i].getName());
        }
        String[] refs = (String[])tree.toArray(new String[tree.size()]);
        
        // replace private method to return binding classes blob
        m_factoryClass.deleteMethod(CLASSLIST_METHOD_NAME,
            CLASSLIST_METHOD_SIGNATURE);
        MethodBuilder mb = new ExceptionMethodBuilder(CLASSLIST_METHOD_NAME,
            Type.STRING, new Type[0], m_factoryClass,
            Constants.ACC_PRIVATE|Constants.ACC_STATIC);
        codegenString(buildClassNamesBlob(refs), mb);
        mb.appendReturn(Type.STRING);
        mb.codeComplete(false);
        mb.addMethod();
        m_factoryClass.codeComplete();
    }

    /**
     * Convenience method to get an name, if the item is defined.
     *
     * @param item information, or <code>null</code> if none
     * @return item name, or <code>null</code> if no item
     */
    private static String methodNameOrNull(ClassItem item) {
        String method;
        if (item == null) {
            method = null;
        } else {
            method = item.getName();
        }
        return method;
    }

    /**
     * Get indexed binding.
     *
     * @param index number of binding to be returned
     * @return binding at the specified index
     */
    public static BindingDefinition getBinding(int index) {
        return (BindingDefinition)s_bindings.get(index);
    }

    /**
     * Discard cached information and reset in preparation for a new binding
     * run.
     */
    public static void reset() {
        s_bindings = new ArrayList();
        s_mappedClasses = new ArrayMap();
    }
    
    //
    // IContainer interface method definitions

    public boolean isContentOrdered() {
        return true;
    }

    public boolean hasNamespaces() {
        return false;
    }

    public BindingDefinition getBindingRoot() {
        return this;
    }

    public DefinitionContext getDefinitionContext() {
        return m_activeContext;
    }
    
    // DEBUG
    private static byte[] s_blanks =
        "                                                   ".getBytes();
    public static void indent(int depth) {
        if (depth < s_blanks.length) {
            System.out.write(s_blanks, 0, depth);
        } else {
            System.out.print(s_blanks);
        }
    }
    public void print() {
        System.out.println("binding " + m_name + ":");
        m_activeContext.print(1);
    }
}
package org.jibx.ws.wsdl;

import org.jibx.ws.wsdl.tools.SignatureParser;

import junit.framework.TestCase;

public class SignatureParserTest extends TestCase
{
    private static final String RETURN_PARAMETERIZED_SIGNATURE =
        "Signature(()Ljava/util/List<Lorg/jibx/binding/generator/DataClass1;>;)";
    private static final String CALL_PARAMETERIZED_SIGNATURE =
        "Signature((Ljava/util/List<Lorg/jibx/binding/generator/DataClass1;>;)V)";
    private static final String COMPLEX_PARAMETERIZED_SIGNATURE =
        "Signature((ILjava/util/List<Lorg/jibx/binding/generator/DataClass1;>;Ljava/util/List<Ljava/lang/String;>;Ljava/lang/Object;Ljava/lang/Integer;ZF)Ljava/util/List<Lorg/jibx/binding/generator/DataClass1;>;)";
    
    public void testReturnParameterized() {
        SignatureParser parse = new SignatureParser(RETURN_PARAMETERIZED_SIGNATURE);
        assertEquals("start method parameters event", SignatureParser.METHOD_PARAMETERS_START_EVENT, parse.next());
        assertEquals("start method parameters state", SignatureParser.METHOD_PARAMETERS_START_EVENT, parse.getEvent());
        assertEquals("end method parameters event", SignatureParser.METHOD_PARAMETERS_END_EVENT, parse.next());
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "java.util.List", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertTrue("type parameterized", parse.isParameterized());
        assertEquals("start type parameters event", SignatureParser.TYPE_PARAMETERS_START_EVENT, parse.next());
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "org.jibx.binding.generator.DataClass1", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        assertEquals("end type parameters event", SignatureParser.TYPE_PARAMETERS_END_EVENT, parse.next());
        assertEquals("end event", SignatureParser.END_EVENT, parse.next());
    }
    
    public void testCallParameterized() {
        SignatureParser parse = new SignatureParser(CALL_PARAMETERIZED_SIGNATURE);
        assertEquals("start method parameters event", SignatureParser.METHOD_PARAMETERS_START_EVENT, parse.next());
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "java.util.List", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertTrue("type parameterized", parse.isParameterized());
        assertEquals("start type parameters event", SignatureParser.TYPE_PARAMETERS_START_EVENT, parse.next());
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "org.jibx.binding.generator.DataClass1", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        assertEquals("end type parameters event", SignatureParser.TYPE_PARAMETERS_END_EVENT, parse.next());
        assertEquals("end method parameters event", SignatureParser.METHOD_PARAMETERS_END_EVENT, parse.next());
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "void", parse.getType());
        assertTrue("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        assertEquals("end event", SignatureParser.END_EVENT, parse.next());
    }
    
    public void testComplexParameterized() {
        SignatureParser parse = new SignatureParser(COMPLEX_PARAMETERIZED_SIGNATURE);
        assertEquals("start method parameters event", SignatureParser.METHOD_PARAMETERS_START_EVENT, parse.next());
        
        // first method parameter is an int
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "int", parse.getType());
        assertTrue("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        
        // second method parameter is a List<DataClass1>
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "java.util.List", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertTrue("type parameterized", parse.isParameterized());
        assertEquals("start type parameters event", SignatureParser.TYPE_PARAMETERS_START_EVENT, parse.next());
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "org.jibx.binding.generator.DataClass1", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        assertEquals("end type parameters event", SignatureParser.TYPE_PARAMETERS_END_EVENT, parse.next());
        
        // third method parameter is a List<String>
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "java.util.List", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertTrue("type parameterized", parse.isParameterized());
        assertEquals("start type parameters event", SignatureParser.TYPE_PARAMETERS_START_EVENT, parse.next());
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "java.lang.String", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        assertEquals("end type parameters event", SignatureParser.TYPE_PARAMETERS_END_EVENT, parse.next());
        
        // fourth method parameter is an Object
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "java.lang.Object", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        
        // fifth method parameter is an Integer
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "java.lang.Integer", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        
        // sixth method parameter is a boolean
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "boolean", parse.getType());
        assertTrue("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        
        // seventh method parameter is a float
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "float", parse.getType());
        assertTrue("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        assertEquals("end method parameters event", SignatureParser.METHOD_PARAMETERS_END_EVENT, parse.next());
        
        // return value is a List<DataClass1>
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "java.util.List", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertTrue("type parameterized", parse.isParameterized());
        assertEquals("start type parameters event", SignatureParser.TYPE_PARAMETERS_START_EVENT, parse.next());
        assertEquals("type event", SignatureParser.TYPE_EVENT, parse.next());
        assertEquals("type value", "org.jibx.binding.generator.DataClass1", parse.getType());
        assertFalse("type primitive", parse.isPrimitive());
        assertFalse("type parameterized", parse.isParameterized());
        assertEquals("end type parameters event", SignatureParser.TYPE_PARAMETERS_END_EVENT, parse.next());
        assertEquals("end event", SignatureParser.END_EVENT, parse.next());
    }
}

package org.jibx.schema.codegen;

import org.jibx.schema.SchemaTestBase;
import org.jibx.schema.codegen.custom.SchemasetCustom;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.validation.ProblemConsoleLister;
import org.jibx.schema.validation.ProblemHandler;

/**
 * Test schema simplification and normalization.
 */
public class TypeReplacementTest extends SchemaTestBase
{
    /**
     * Test the schema simplification handling. This first loads and validates the supplied main schema, which
     * automatically loads and validates any included/imported schemas. It then applies any customizations and finally
     * simplifies the (possibly modified) included/imported schemas and checks that the result matches expectations.
     *
     * @param resolver root schema resolver
     * @param custom customizations document text
     * @param inclname included schema name
     * @param rslttext simplified result schema expected
     * @throws Exception
     */
    private void testSimplification(TestResolver resolver, String custom, String inclname, String rslttext)
        throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(resolver) };
        SchemasetCustom custroot;
        if (custom == null) {
            custroot = new SchemasetCustom((SchemasetCustom)null);
        } else {
            custroot = loadCustomization(custom);
        }
        CodeGen generator = new CodeGen(custroot, m_validationContext);
        ProblemHandler handler = new ProblemConsoleLister();
        assertTrue("Schema customization failure", generator.customizeSchemas("dflt", handler));
        generator.applyAndNormalize();
        generator.pruneDefinitions();
        verifySchema(resolver.getText(), writeSchema(schemas[0]));
        SchemaElement schema = m_validationContext.getSchemaById(inclname);
        verifySchema(rslttext, writeSchema(schema));
    }
    
    public void testSchemaSimplification() throws Exception {
        testSimplification(CodegenData.RESOLVER1, null, "INCLUDED_SCHEMA1", CodegenData.RESULT_SCHEMA1_A);
    }
    
    public void testTypeReplacement() throws Exception {
        testSimplification(CodegenData.RESOLVER1, CodegenData.CUSTOMIZATION1_B, "INCLUDED_SCHEMA1",
            CodegenData.RESULT_SCHEMA1_B);
    }
    
    public void testTypeRemoval() throws Exception {
        testSimplification(CodegenData.RESOLVER1, CodegenData.CUSTOMIZATION1_C, "INCLUDED_SCHEMA1",
            CodegenData.RESULT_SCHEMA1_C);
    }
    
    public void testTypeReplacement2() throws Exception {
        testSimplification(CodegenData.RESOLVER2, CodegenData.CUSTOMIZATION2_A, "INCLUDED_SCHEMA2",
            CodegenData.RESULT_SCHEMA2_A);
    }
    
    public void testCombinedRemovalReplacement() throws Exception {
        testSimplification(CodegenData.RESOLVER2, CodegenData.CUSTOMIZATION2_B, "INCLUDED_SCHEMA2",
            CodegenData.RESULT_SCHEMA2_B);
    }
    
    public void testSimplificationWithEnums() throws Exception {
        testSimplification(CodegenData.RESOLVER3, CodegenData.CUSTOMIZATION3_A, "INCLUDED_SCHEMA3",
            CodegenData.RESULT_SCHEMA3_A);
    }
}

package org.jibx.schema.codegen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jibx.binding.model.BindingElement;
import org.jibx.binding.model.BindingHolder;
import org.jibx.binding.model.BindingOrganizer;
import org.jibx.extras.DocumentComparator;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.schema.SchemaTestBase;
import org.jibx.schema.codegen.custom.SchemasetCustom;
import org.jibx.schema.elements.SchemaElement;
import org.jibx.schema.validation.ProblemConsoleLister;
import org.jibx.schema.validation.ProblemHandler;
import org.jibx.schema.validation.ValidationUtils;

/**
 * Test code generation from schemas.
 */
public class CodeGenerationTest extends SchemaTestBase
{
    static final StringObjectPair[] DATA1 = new StringObjectPair[] {
        new StringObjectPair("something.Extra", new StringPair[] {
            new StringPair("extra", "String")}),
        new StringObjectPair("anything.Simple1", new StringPair[] { 
            new StringPair("male", "boolean"),
            new StringPair("name", "String"),
            new StringPair("rated", "int")}),
        new StringObjectPair("anything.Simple3", new StringPair[] { 
            new StringPair("index", "BigInteger"),
            new StringPair("mixed", "String"),
            new StringPair("rated", "Integer"),
            new StringPair("simple1", "Simple1")}),
    };
    static final StringPair[] BINDINGS1 = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='something.Extra' name='extra'>\n" +
            "    <value style='text' get-method='getExtra' set-method='setExtra'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Simple3' name='element'>\n" +
            "    <structure map-as='ins:simple3'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:simple1' class='anything.Simple1'>\n" +
            "    <value style='element' name='name' get-method='getName' set-method='setName'/>\n" +
            "    <value style='element' name='rated' get-method='getRated' set-method='setRated'/>\n" +
            "    <value style='attribute' name='male' get-method='isMale' set-method='setMale'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Simple1' name='simple1'>\n" +
            "    <structure map-as='ins:simple1'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:simple3' class='anything.Simple3'>\n" +
            "    <structure type='anything.Simple1' get-method='getSimple1' set-method='setSimple1'/>\n" +
            "    <value style='element' name='mixed' get-method='getMixed' set-method='setMixed'/>\n" +
            "    <value style='attribute' name='rated' get-method='getRated' set-method='setRated' usage='optional'/>\n" +
            "    <value style='attribute' name='index' get-method='getIndex' set-method='setIndex' usage='optional'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] DATA3_A = new StringObjectPair[] {
        new StringObjectPair("anything.Enum1", new StringPair[] {}),
        new StringObjectPair("anything.Enum2", new StringPair[] { 
            new StringPair("value", "String")}),
        new StringObjectPair("anything.Enum23Group", new StringPair[] { 
            new StringPair("enum2", "Enum2"),
            new StringPair("enum3", "Enum3")}),
        new StringObjectPair("anything.Enum23Group.Enum3", new StringPair[] { 
            new StringPair("value", "String")}),
        new StringObjectPair("anything.Mixed", new StringPair[] { 
            new StringPair("mixed", "String")}),
        new StringObjectPair("anything.Name", new StringPair[] { 
            new StringPair("name", "String")}),
        new StringObjectPair("anything.NestedType", new StringPair[] { 
            new StringPair("enumList", "List<Enum1>"),
            new StringPair("nameList", "List<Name>")}),
        new StringObjectPair("anything.Rated", new StringPair[] { 
            new StringPair("rated", "Float")}),
        new StringObjectPair("anything.Simple1", new StringPair[] { 
            new StringPair("enum1", "Enum1"),
            new StringPair("enum23Group", "Enum23Group"),
            new StringPair("male", "boolean"),
            new StringPair("name", "Name"),
            new StringPair("rated", "Rated")}),
        new StringObjectPair("anything.Simple3", new StringPair[] { 
            new StringPair("enum1", "Enum1"),
            new StringPair("enum23Group", "Enum23Group"),
            new StringPair("mixed", "Mixed"),
            new StringPair("rated", "Float"),
            new StringPair("simple1", "Simple1")}),
    };
    static final StringPair[] BINDINGS3_A = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <format label='ins:enum1' type='anything.Enum1'/>\n" +
            "  <format label='ins:enum2' type='anything.Enum2' enum-value-method='xmlValue'/>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='anything.Simple3' name='element'>\n" +
            "    <structure map-as='ins:simple3'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:simple1' class='anything.Simple1'>\n" +
            "    <structure type='anything.Name' get-method='getName' set-method='setName'/>\n" +
            "    <structure type='anything.Rated' get-method='getRated' set-method='setRated'/>\n" +
            "    <value style='attribute' name='enum1' get-method='getEnum1' set-method='setEnum1' format='ins:enum1'/>\n" +
            "    <structure map-as='ins:enum23Group-AttributeGroup' get-method='getEnum23Group' set-method='setEnum23Group'/>\n" +
            "    <value style='attribute' name='male' get-method='isMale' set-method='setMale'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Simple1' name='simple1'>\n" +
            "    <structure map-as='ins:simple1'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:enum23Group-AttributeGroup' class='anything.Enum23Group'>\n" +
            "    <format type='anything.Enum23Group$Enum3' enum-value-method='xmlValue'/>\n" +
            "    <value style='attribute' name='enum2' get-method='getEnum2' set-method='setEnum2' format='ins:enum2'/>\n" +
            "    <value style='attribute' name='enum3' get-method='getEnum3' set-method='setEnum3'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Name' name='name'>\n" +
            "    <value style='text' get-method='getName' set-method='setName'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Rated' name='rated'>\n" +
            "    <value style='text' get-method='getRated' set-method='setRated'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Mixed' name='mixed'>\n" +
            "    <value style='text' get-method='getMixed' set-method='setMixed'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:simple3' class='anything.Simple3'>\n" +
            "    <structure ordered='false'>\n" +
            "      <structure type='anything.Simple1' get-method='getSimple1' set-method='setSimple1'/>\n" +
            "      <structure type='anything.Mixed' get-method='getMixed' set-method='setMixed'/>\n" +
            "    </structure>\n" +
            "    <value style='attribute' name='rated' get-method='getRated' set-method='setRated' usage='optional'/>\n" +
            "    <value style='attribute' name='enum1' get-method='getEnum1' set-method='setEnum1' format='ins:enum1'/>\n" +
            "    <structure map-as='ins:enum23Group-AttributeGroup' get-method='getEnum23Group' set-method='setEnum23Group'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:nestedType' class='anything.NestedType'>\n" +
            "    <collection item-type='anything.Name' get-method='getNameList' set-method='setNameList' usage='optional' create-type='java.util.ArrayList'/>\n" +
            "    <collection get-method='getEnumList' set-method='setEnumList' name='enums' create-type='java.util.ArrayList'>\n" +
            "      <structure type='anything.Enum1' name='enum'>\n" +
            "        <value style='attribute' name='enum1' type='anything.Enum1' format='ins:enum1'/>\n" +
            "      </structure>\n" +
            "    </collection>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] DATA3_B = new StringObjectPair[] {
        new StringObjectPair("anything.Enum1", new StringPair[] { 
            new StringPair("A1", "Enum1"),
            new StringPair("B2", "Enum1"),
            new StringPair("instances", "Enum1[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.Enum2", new StringPair[] { 
            new StringPair("A1", "Enum2"),
            new StringPair("B2", "Enum2"),
            new StringPair("instances", "Enum2[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.Enum23Group", new StringPair[] { 
            new StringPair("enum2", "Enum2"),
            new StringPair("enum3", "Enum3")}),
        new StringObjectPair("anything.Enum23Group.Enum3", new StringPair[] { 
            new StringPair("X1", "Enum3"),
            new StringPair("Y2", "Enum3"),
            new StringPair("Z3", "Enum3"),
            new StringPair("instances", "Enum3[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.Simple1", new StringPair[] { 
            new StringPair("enum1", "Enum1"),
            new StringPair("enum23Group", "Enum23Group"),
            new StringPair("male", "boolean"),
            new StringPair("name", "String"),
            new StringPair("rated", "Float")}),
        new StringObjectPair("anything.Simple3", new StringPair[] { 
            new StringPair("enum1", "Enum1"),
            new StringPair("enum23Group", "Enum23Group"),
            new StringPair("mixed", "String"),
            new StringPair("rated", "Float"),
            new StringPair("simple1", "Simple1")}),
    };
    static final StringPair[] BINDINGS3_B = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <format label='ins:enum1' type='anything.Enum1' deserializer='anything.Enum1.fromValue'/>\n" +
            "  <format label='ins:enum2' type='anything.Enum2' deserializer='anything.Enum2.fromValue'/>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='anything.Simple3' name='element'>\n" +
            "    <structure map-as='ins:simple3'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:simple1' class='anything.Simple1'>\n" +
            "    <value style='element' name='name' get-method='getName' set-method='setName'/>\n" +
            "    <value style='element' name='rated' get-method='getRated' set-method='setRated'/>\n" +
            "    <value style='attribute' name='enum1' get-method='getEnum1' set-method='setEnum1' format='ins:enum1'/>\n" +
            "    <structure map-as='ins:enum23Group-AttributeGroup' get-method='getEnum23Group' set-method='setEnum23Group'/>\n" +
            "    <value style='attribute' name='male' get-method='isMale' set-method='setMale'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Simple1' name='simple1'>\n" +
            "    <structure map-as='ins:simple1'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:enum23Group-AttributeGroup' class='anything.Enum23Group'>\n" +
            "    <format type='anything.Enum23Group$Enum3' deserializer='anything.Enum23Group$Enum3.fromValue'/>\n" +
            "    <value style='attribute' name='enum2' get-method='getEnum2' set-method='setEnum2' format='ins:enum2'/>\n" +
            "    <value style='attribute' name='enum3' get-method='getEnum3' set-method='setEnum3'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:simple3' class='anything.Simple3'>\n" +
            "    <structure ordered='false'>\n" +
            "      <structure type='anything.Simple1' get-method='getSimple1' set-method='setSimple1'/>\n" +
            "      <value style='element' name='mixed' get-method='getMixed' set-method='setMixed'/>\n" +
            "    </structure>\n" +
            "    <value style='attribute' name='rated' get-method='getRated' set-method='setRated' usage='optional'/>\n" +
            "    <value style='attribute' name='enum1' get-method='getEnum1' set-method='setEnum1' format='ins:enum1'/>\n" +
            "    <structure map-as='ins:enum23Group-AttributeGroup' get-method='getEnum23Group' set-method='setEnum23Group'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] DATA3_C = new StringObjectPair[] {
        new StringObjectPair("anything.Enum1Group", new StringPair[] { 
            new StringPair("enum1", "Enum1")}),
        new StringObjectPair("anything.Enum1Group.Enum1", new StringPair[] { 
            new StringPair("A1", "Enum1"),
            new StringPair("B2", "Enum1"),
            new StringPair("instances", "Enum1[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.Enum23Group", new StringPair[] { 
            new StringPair("enum2", "Enum2"),
            new StringPair("enum3", "Enum3")}),
        new StringObjectPair("anything.Enum23Group.Enum2", new StringPair[] { 
            new StringPair("A1", "Enum2"),
            new StringPair("B2", "Enum2"),
            new StringPair("instances", "Enum2[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.Enum23Group.Enum3", new StringPair[] { 
            new StringPair("X1", "Enum3"),
            new StringPair("Y2", "Enum3"),
            new StringPair("Z3", "Enum3"),
            new StringPair("instances", "Enum3[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.Simple1", new StringPair[] { 
            new StringPair("enum1Group", "Enum1Group"),
            new StringPair("enum23Group", "Enum23Group"),
            new StringPair("male", "boolean"),
            new StringPair("name", "String"),
            new StringPair("rated", "Float")}),
        new StringObjectPair("anything.Simple3", new StringPair[] { 
            new StringPair("enum1Group", "Enum1Group"),
            new StringPair("enum23Group", "Enum23Group"),
            new StringPair("mixed", "String"),
            new StringPair("rated", "Float"),
            new StringPair("simple1", "Simple1")}),
    };
    static final StringPair[] BINDINGS3_C = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='anything.Simple3' name='element'>\n" +
            "    <structure map-as='ins:simple3'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:simple1' class='anything.Simple1'>\n" +
            "    <value style='element' name='name' get-method='getName' set-method='setName'/>\n" +
            "    <value style='element' name='rated' get-method='getRated' set-method='setRated'/>\n" +
            "    <structure map-as='ins:enum1Group-AttributeGroup' get-method='getEnum1Group' set-method='setEnum1Group'/>\n" +
            "    <structure map-as='ins:enum23Group-AttributeGroup' get-method='getEnum23Group' set-method='setEnum23Group'/>\n" +
            "    <value style='attribute' name='male' get-method='isMale' set-method='setMale'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Simple1' name='simple1'>\n" +
            "    <structure map-as='ins:simple1'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:enum1Group-AttributeGroup' class='anything.Enum1Group'>\n" +
            "    <format type='anything.Enum1Group$Enum1' deserializer='anything.Enum1Group$Enum1.fromValue'/>\n" +
            "    <value style='attribute' name='enum1' get-method='getEnum1' set-method='setEnum1'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:enum23Group-AttributeGroup' class='anything.Enum23Group'>\n" +
            "    <format type='anything.Enum23Group$Enum2' deserializer='anything.Enum23Group$Enum2.fromValue'/>\n" +
            "    <format type='anything.Enum23Group$Enum3' deserializer='anything.Enum23Group$Enum3.fromValue'/>\n" +
            "    <value style='attribute' name='enum2' get-method='getEnum2' set-method='setEnum2'/>\n" +
            "    <value style='attribute' name='enum3' get-method='getEnum3' set-method='setEnum3'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:simple3' class='anything.Simple3'>\n" +
            "    <structure ordered='false'>\n" +
            "      <structure type='anything.Simple1' get-method='getSimple1' set-method='setSimple1'/>\n" +
            "      <value style='element' name='mixed' get-method='getMixed' set-method='setMixed'/>\n" +
            "    </structure>\n" +
            "    <value style='attribute' name='rated' get-method='getRated' set-method='setRated' usage='optional'/>\n" +
            "    <structure map-as='ins:enum1Group-AttributeGroup' get-method='getEnum1Group' set-method='setEnum1Group'/>\n" +
            "    <structure map-as='ins:enum23Group-AttributeGroup' get-method='getEnum23Group' set-method='setEnum23Group'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] DATA3_D = new StringObjectPair[] {
        new StringObjectPair("anything.Simple3", new StringPair[] { 
            new StringPair("enum1", "Enum1"),
            new StringPair("enum3", "Enum3"),
            new StringPair("mixed", "String"),
            new StringPair("rated", "Float")}),
        new StringObjectPair("anything.Simple3.Enum1", new StringPair[] { 
            new StringPair("A1", "Enum1"),
            new StringPair("B2", "Enum1"),
            new StringPair("instances", "Enum1[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.Simple3.Enum3", new StringPair[] { 
            new StringPair("X1", "Enum3"),
            new StringPair("Y2", "Enum3"),
            new StringPair("Z3", "Enum3"),
            new StringPair("instances", "Enum3[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
    };
    static final StringPair[] BINDINGS3_D = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='anything.Simple3' name='element'>\n" +
            "    <structure map-as='ins:simple3'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:simple3' class='anything.Simple3'>\n" +
            "    <format type='anything.Simple3$Enum1' deserializer='anything.Simple3$Enum1.fromValue'/>\n" +
            "    <format type='anything.Simple3$Enum3' deserializer='anything.Simple3$Enum3.fromValue'/>\n" +
            "    <value style='element' name='mixed' get-method='getMixed' set-method='setMixed'/>\n" +
            "    <value style='attribute' name='rated' get-method='getRated' set-method='setRated' usage='optional'/>\n" +
            "    <value style='attribute' name='enum1' get-method='getEnum1' set-method='setEnum1'/>\n" +
            "    <value style='attribute' name='enum3' get-method='getEnum3' set-method='setEnum3'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] OTA_PROFILETYPE_SIMPLIFIED_UNQUALIFIED_DATA_A = new StringObjectPair[] {
        new StringObjectPair("anything.CommonPrefType", new StringPair[] { 
            new StringPair("namePrefList", "List<String>"),
            new StringPair("phonePrefList", "List<PhonePrefType>"),
            new StringPair("privacyGroup", "PrivacyGroup")}),
        new StringObjectPair("anything.FormattedTextTextType", new StringPair[] { 
            new StringPair("formatted", "Boolean"),
            new StringPair("string", "String")}),
        new StringObjectPair("anything.ParagraphType", new StringPair[] { 
            new StringPair("choiceList", "List<Choice>"),
            new StringPair("name", "String"),
            new StringPair("paragraphNumber", "BigInteger")}),
        new StringObjectPair("anything.ParagraphType.Choice", new StringPair[] { 
            new StringPair("IMAGE_CHOICE", "int"),
            new StringPair("LIST_ITEM_CHOICE", "int"),
            new StringPair("TEXT_CHOICE", "int"),
            new StringPair("choiceListSelect", "int"),
            new StringPair("image", "String"),
            new StringPair("listItemFormattedTextTextType", "FormattedTextTextType"),
            new StringPair("listItemListItem", "BigInteger"),
            new StringPair("text", "FormattedTextTextType")}),
        new StringObjectPair("anything.PhonePrefType", new StringPair[] { 
            new StringPair("telephone", "TelephoneInfoGroup")}),
        new StringObjectPair("anything.PreferencesType", new StringPair[] { 
            new StringPair("prefCollectionList", "List<PrefCollection>"),
            new StringPair("privacyGroup", "PrivacyGroup")}),
        new StringObjectPair("anything.PreferencesType.PrefCollection", new StringPair[] { 
            new StringPair("commonPrefList", "List<CommonPrefType>"),
            new StringPair("privacyGroup", "PrivacyGroup"),
            new StringPair("travelPurpose", "String"),
            new StringPair("vehicleRentalPrefList", "List<String>")}),
        new StringObjectPair("anything.PrivacyGroup", new StringPair[] { 
            new StringPair("shareMarketInd", "ShareMarketInd"),
            new StringPair("shareSynchInd", "ShareSynchInd")}),
        new StringObjectPair("anything.PrivacyGroup.ShareMarketInd", new StringPair[] { 
            new StringPair("INHERIT", "ShareMarketInd"),
            new StringPair("NO", "ShareMarketInd"),
            new StringPair("YES", "ShareMarketInd"),
            new StringPair("instances", "ShareMarketInd[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.PrivacyGroup.ShareSynchInd", new StringPair[] { 
            new StringPair("INHERIT", "ShareSynchInd"),
            new StringPair("NO", "ShareSynchInd"),
            new StringPair("YES", "ShareSynchInd"),
            new StringPair("instances", "ShareSynchInd[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.ProfileType", new StringPair[] { 
            new StringPair("RPH", "String"),
            new StringPair("accesses", "String"),
            new StringPair("prefCollections", "PreferencesType")}),
        new StringObjectPair("anything.TelephoneAttributesGroup", new StringPair[] { 
            new StringPair("areaCityCode", "String"),
            new StringPair("countryAccessCode", "String"),
            new StringPair("extension", "String"),
            new StringPair("phoneNumber", "String")}),
        new StringObjectPair("anything.TelephoneGroup", new StringPair[] { 
            new StringPair("privacyGroup", "PrivacyGroup"),
            new StringPair("telephoneAttributesGroup", "TelephoneAttributesGroup")}),
        new StringObjectPair("anything.TelephoneInfoGroup", new StringPair[] { 
            new StringPair("RPH", "String"),
            new StringPair("telephoneGroup", "TelephoneGroup")}),
    };
    static final StringPair[] OTA_PROFILETYPE_SIMPLIFIED_UNQUALIFIED_BINDINGS_A = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' prefix='ns1'/>\n" +
            "  <mapping class='anything.ProfileType' name='element' ns='urn:something'>\n" +
            "    <structure map-as='ins:ProfileType'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <mapping abstract='true' type-name='ins:FormattedTextTextType' class='anything.FormattedTextTextType'>\n" +
            "    <value style='text' get-method='getString' set-method='setString'/>\n" +
            "    <value style='attribute' name='Formatted' get-method='getFormatted' set-method='setFormatted' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:ParagraphType' class='anything.ParagraphType'>\n" +
            "    <collection get-method='getChoiceList' set-method='setChoiceList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "      <structure type='anything.ParagraphType$Choice' ordered='false' choice='true'>\n" +
            "        <structure map-as='ins:FormattedTextTextType' test-method='ifText' get-method='getText' set-method='setText' usage='optional' name='Text'/>\n" +
            "        <value style='element' name='Image' test-method='ifImage' get-method='getImage' set-method='setImage' usage='optional'/>\n" +
            "        <structure test-method='ifListItem' usage='optional' name='ListItem'>\n" +
            "          <structure map-as='ins:FormattedTextTextType' get-method='getListItemFormattedTextTextType' set-method='setListItemFormattedTextTextType'/>\n" +
            "          <value style='attribute' name='ListItem' get-method='getListItemListItem' set-method='setListItemListItem' usage='optional'/>\n" +
            "        </structure>\n" +
            "      </structure>\n" +
            "    </collection>\n" +
            "    <value style='attribute' name='Name' get-method='getName' set-method='setName' usage='optional'/>\n" +
            "    <value style='attribute' name='ParagraphNumber' get-method='getParagraphNumber' set-method='setParagraphNumber' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:PrivacyGroup-AttributeGroup' class='anything.PrivacyGroup'>\n" +
            "    <format type='anything.PrivacyGroup$ShareSynchInd' deserializer='anything.PrivacyGroup$ShareSynchInd.fromValue'/>\n" +
            "    <format type='anything.PrivacyGroup$ShareMarketInd' deserializer='anything.PrivacyGroup$ShareMarketInd.fromValue'/>\n" +
            "    <value style='attribute' name='ShareSynchInd' get-method='getShareSynchInd' set-method='setShareSynchInd' usage='optional'/>\n" +
            "    <value style='attribute' name='ShareMarketInd' get-method='getShareMarketInd' set-method='setShareMarketInd' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:TelephoneAttributesGroup-AttributeGroup' class='anything.TelephoneAttributesGroup'>\n" +
            "    <value style='attribute' name='CountryAccessCode' get-method='getCountryAccessCode' set-method='setCountryAccessCode' usage='optional'/>\n" +
            "    <value style='attribute' name='AreaCityCode' get-method='getAreaCityCode' set-method='setAreaCityCode' usage='optional'/>\n" +
            "    <value style='attribute' name='PhoneNumber' get-method='getPhoneNumber' set-method='setPhoneNumber'/>\n" +
            "    <value style='attribute' name='Extension' get-method='getExtension' set-method='setExtension' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:TelephoneGroup-AttributeGroup' class='anything.TelephoneGroup'>\n" +
            "    <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacyGroup' set-method='setPrivacyGroup' usage='optional'/>\n" +
            "    <structure map-as='ins:TelephoneAttributesGroup-AttributeGroup' get-method='getTelephoneAttributesGroup' set-method='setTelephoneAttributesGroup'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:TelephoneInfoGroup-AttributeGroup' class='anything.TelephoneInfoGroup'>\n" +
            "    <structure map-as='ins:TelephoneGroup-AttributeGroup' get-method='getTelephoneGroup' set-method='setTelephoneGroup'/>\n" +
            "    <value style='attribute' name='RPH' get-method='getRPH' set-method='setRPH' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:PhonePrefType' class='anything.PhonePrefType'>\n" +
            "    <structure get-method='getTelephone' set-method='setTelephone' name='Telephone'>\n" +
            "      <structure map-as='ins:TelephoneInfoGroup-AttributeGroup'/>\n" +
            "    </structure>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:CommonPrefType' class='anything.CommonPrefType'>\n" +
            "    <collection get-method='getNamePrefList' set-method='setNamePrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "      <value style='element' name='NamePref' type='java.lang.String'/>\n" +
            "    </collection>\n" +
            "    <collection get-method='getPhonePrefList' set-method='setPhonePrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "      <structure map-as='ins:PhonePrefType' name='PhonePref'/>\n" +
            "    </collection>\n" +
            "    <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacyGroup' set-method='setPrivacyGroup' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:PreferencesType' class='anything.PreferencesType'>\n" +
            "    <collection get-method='getPrefCollectionList' set-method='setPrefCollectionList' create-type='java.util.ArrayList'>\n" +
            "      <structure type='anything.PreferencesType$PrefCollection' name='PrefCollection'>\n" +
            "        <collection get-method='getCommonPrefList' set-method='setCommonPrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "          <structure map-as='ins:CommonPrefType' name='CommonPref'/>\n" +
            "        </collection>\n" +
            "        <collection get-method='getVehicleRentalPrefList' set-method='setVehicleRentalPrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "          <value style='element' name='VehicleRentalPref' type='java.lang.String'/>\n" +
            "        </collection>\n" +
            "        <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacyGroup' set-method='setPrivacyGroup' usage='optional'/>\n" +
            "        <value style='attribute' name='TravelPurpose' get-method='getTravelPurpose' set-method='setTravelPurpose' usage='optional'/>\n" +
            "      </structure>\n" +
            "    </collection>\n" +
            "    <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacyGroup' set-method='setPrivacyGroup' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:ProfileType' class='anything.ProfileType'>\n" +
            "    <value style='element' name='Accesses' get-method='getAccesses' set-method='setAccesses' usage='optional'/>\n" +
            "    <structure map-as='ins:PreferencesType' get-method='getPrefCollections' set-method='setPrefCollections' usage='optional' name='PrefCollections'/>\n" +
            "    <value style='attribute' name='RPH' get-method='getRPH' set-method='setRPH' usage='optional'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] OTA_PROFILETYPE_SIMPLIFIED_DATA_A = new StringObjectPair[] {
        new StringObjectPair("anything.CommonPrefType", new StringPair[] { 
            new StringPair("namePrefList", "List<String>"),
            new StringPair("phonePrefList", "List<PhonePrefType>"),
            new StringPair("privacyGroup", "PrivacyGroup")}),
        new StringObjectPair("anything.CommonProfileTypeType", new StringPair[] { 
            new StringPair("RPH", "String"),
            new StringPair("accesses", "String"),
            new StringPair("prefCollections", "PreferencesType")}),
        new StringObjectPair("anything.FormattedTextTextType", new StringPair[] { 
            new StringPair("formatted", "Boolean"),
            new StringPair("string", "String")}),
        new StringObjectPair("anything.ParagraphType", new StringPair[] { 
            new StringPair("choiceList", "List<Choice>"),
            new StringPair("name", "String"),
            new StringPair("paragraphNumber", "BigInteger")}),
        new StringObjectPair("anything.ParagraphType.Choice", new StringPair[] { 
            new StringPair("IMAGE_CHOICE", "int"),
            new StringPair("LIST_ITEM_CHOICE", "int"),
            new StringPair("TEXT_CHOICE", "int"),
            new StringPair("choiceListSelect", "int"),
            new StringPair("image", "String"),
            new StringPair("listItemFormattedTextTextType", "FormattedTextTextType"),
            new StringPair("listItemListItem", "BigInteger"),
            new StringPair("text", "FormattedTextTextType")}),
        new StringObjectPair("anything.PhonePrefType", new StringPair[] { 
            new StringPair("telephone", "TelephoneInfoGroup")}),
        new StringObjectPair("anything.PreferencesType", new StringPair[] { 
            new StringPair("prefCollectionList", "List<PrefCollection>"),
            new StringPair("privacyGroup", "PrivacyGroup")}),
        new StringObjectPair("anything.PreferencesType.PrefCollection", new StringPair[] { 
            new StringPair("commonPrefList", "List<CommonPrefType>"),
            new StringPair("privacyGroup", "PrivacyGroup"),
            new StringPair("travelPurpose", "String"),
            new StringPair("vehicleRentalPrefList", "List<String>")}),
        new StringObjectPair("anything.PrivacyGroup", new StringPair[] { 
            new StringPair("shareMarketInd", "ShareMarketInd"),
            new StringPair("shareSynchInd", "ShareSynchInd")}),
        new StringObjectPair("anything.PrivacyGroup.ShareMarketInd", new StringPair[] { 
            new StringPair("INHERIT", "ShareMarketInd"),
            new StringPair("NO", "ShareMarketInd"),
            new StringPair("YES", "ShareMarketInd"),
            new StringPair("instances", "ShareMarketInd[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.PrivacyGroup.ShareSynchInd", new StringPair[] { 
            new StringPair("INHERIT", "ShareSynchInd"),
            new StringPair("NO", "ShareSynchInd"),
            new StringPair("YES", "ShareSynchInd"),
            new StringPair("instances", "ShareSynchInd[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.TelephoneAttributesGroup", new StringPair[] { 
            new StringPair("areaCityCode", "String"),
            new StringPair("countryAccessCode", "String"),
            new StringPair("extension", "String"),
            new StringPair("phoneNumber", "String")}),
        new StringObjectPair("anything.TelephoneGroup", new StringPair[] { 
            new StringPair("privacyGroup", "PrivacyGroup"),
            new StringPair("telephoneAttributesGroup", "TelephoneAttributesGroup")}),
        new StringObjectPair("anything.TelephoneInfoGroup", new StringPair[] { 
            new StringPair("RPH", "String"),
            new StringPair("telephoneGroup", "TelephoneGroup")}),
    };
    static final StringPair[] OTA_PROFILETYPE_SIMPLIFIED_BINDINGS_A = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='anything.CommonProfileTypeType' name='element'>\n" +
            "    <structure map-as='ins:CommonProfileTypeType'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:FormattedTextTextType' class='anything.FormattedTextTextType'>\n" +
            "    <value style='text' get-method='getString' set-method='setString'/>\n" +
            "    <value style='attribute' name='Formatted' get-method='getFormatted' set-method='setFormatted' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:ParagraphType' class='anything.ParagraphType'>\n" +
            "    <collection get-method='getChoiceList' set-method='setChoiceList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "      <structure type='anything.ParagraphType$Choice' ordered='false' choice='true'>\n" +
            "        <structure map-as='ins:FormattedTextTextType' test-method='ifText' get-method='getText' set-method='setText' usage='optional' name='Text'/>\n" +
            "        <value style='element' name='Image' test-method='ifImage' get-method='getImage' set-method='setImage' usage='optional'/>\n" +
            "        <structure test-method='ifListItem' usage='optional' name='ListItem'>\n" +
            "          <structure map-as='ins:FormattedTextTextType' get-method='getListItemFormattedTextTextType' set-method='setListItemFormattedTextTextType'/>\n" +
            "          <value style='attribute' name='ListItem' get-method='getListItemListItem' set-method='setListItemListItem' usage='optional'/>\n" +
            "        </structure>\n" +
            "      </structure>\n" +
            "    </collection>\n" +
            "    <value style='attribute' name='Name' get-method='getName' set-method='setName' usage='optional'/>\n" +
            "    <value style='attribute' name='ParagraphNumber' get-method='getParagraphNumber' set-method='setParagraphNumber' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:PrivacyGroup-AttributeGroup' class='anything.PrivacyGroup'>\n" +
            "    <format type='anything.PrivacyGroup$ShareSynchInd' deserializer='anything.PrivacyGroup$ShareSynchInd.fromValue'/>\n" +
            "    <format type='anything.PrivacyGroup$ShareMarketInd' deserializer='anything.PrivacyGroup$ShareMarketInd.fromValue'/>\n" +
            "    <value style='attribute' name='ShareSynchInd' get-method='getShareSynchInd' set-method='setShareSynchInd' usage='optional'/>\n" +
            "    <value style='attribute' name='ShareMarketInd' get-method='getShareMarketInd' set-method='setShareMarketInd' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:TelephoneAttributesGroup-AttributeGroup' class='anything.TelephoneAttributesGroup'>\n" +
            "    <value style='attribute' name='CountryAccessCode' get-method='getCountryAccessCode' set-method='setCountryAccessCode' usage='optional'/>\n" +
            "    <value style='attribute' name='AreaCityCode' get-method='getAreaCityCode' set-method='setAreaCityCode' usage='optional'/>\n" +
            "    <value style='attribute' name='PhoneNumber' get-method='getPhoneNumber' set-method='setPhoneNumber'/>\n" +
            "    <value style='attribute' name='Extension' get-method='getExtension' set-method='setExtension' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:TelephoneGroup-AttributeGroup' class='anything.TelephoneGroup'>\n" +
            "    <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacyGroup' set-method='setPrivacyGroup' usage='optional'/>\n" +
            "    <structure map-as='ins:TelephoneAttributesGroup-AttributeGroup' get-method='getTelephoneAttributesGroup' set-method='setTelephoneAttributesGroup'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:TelephoneInfoGroup-AttributeGroup' class='anything.TelephoneInfoGroup'>\n" +
            "    <structure map-as='ins:TelephoneGroup-AttributeGroup' get-method='getTelephoneGroup' set-method='setTelephoneGroup'/>\n" +
            "    <value style='attribute' name='RPH' get-method='getRPH' set-method='setRPH' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:PhonePrefType' class='anything.PhonePrefType'>\n" +
            "    <structure get-method='getTelephone' set-method='setTelephone' name='Telephone'>\n" +
            "      <structure map-as='ins:TelephoneInfoGroup-AttributeGroup'/>\n" +
            "    </structure>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:CommonPrefType' class='anything.CommonPrefType'>\n" +
            "    <collection get-method='getNamePrefList' set-method='setNamePrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "      <value style='element' name='NamePref' type='java.lang.String'/>\n" +
            "    </collection>\n" +
            "    <collection get-method='getPhonePrefList' set-method='setPhonePrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "      <structure map-as='ins:PhonePrefType' name='PhonePref'/>\n" +
            "    </collection>\n" +
            "    <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacyGroup' set-method='setPrivacyGroup' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:PreferencesType' class='anything.PreferencesType'>\n" +
            "    <collection get-method='getPrefCollectionList' set-method='setPrefCollectionList' create-type='java.util.ArrayList'>\n" +
            "      <structure type='anything.PreferencesType$PrefCollection' name='PrefCollection'>\n" +
            "        <collection get-method='getCommonPrefList' set-method='setCommonPrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "          <structure map-as='ins:CommonPrefType' name='CommonPref'/>\n" +
            "        </collection>\n" +
            "        <collection get-method='getVehicleRentalPrefList' set-method='setVehicleRentalPrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "          <value style='element' name='VehicleRentalPref' type='java.lang.String'/>\n" +
            "        </collection>\n" +
            "        <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacyGroup' set-method='setPrivacyGroup' usage='optional'/>\n" +
            "        <value style='attribute' name='TravelPurpose' get-method='getTravelPurpose' set-method='setTravelPurpose' usage='optional'/>\n" +
            "      </structure>\n" +
            "    </collection>\n" +
            "    <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacyGroup' set-method='setPrivacyGroup' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:CommonProfileTypeType' class='anything.CommonProfileTypeType'>\n" +
            "    <value style='element' name='Accesses' get-method='getAccesses' set-method='setAccesses' usage='optional'/>\n" +
            "    <structure map-as='ins:PreferencesType' get-method='getPrefCollections' set-method='setPrefCollections' usage='optional' name='PrefCollections'/>\n" +
            "    <value style='attribute' name='RPH' get-method='getRPH' set-method='setRPH' usage='optional'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] OTA_PROFILETYPE_SIMPLIFIED_DATA_B = new StringObjectPair[] {
        new StringObjectPair("anything.Privacy", new StringPair[] { 
            new StringPair("shareMarketInd", "ShareMarketInd"),
            new StringPair("shareSynchInd", "ShareSynchInd")}),
        new StringObjectPair("anything.Privacy.ShareMarketInd", new StringPair[] { 
            new StringPair("INHERIT", "ShareMarketInd"),
            new StringPair("NO", "ShareMarketInd"),
            new StringPair("YES", "ShareMarketInd"),
            new StringPair("instances", "ShareMarketInd[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.Privacy.ShareSynchInd", new StringPair[] { 
            new StringPair("INHERIT", "ShareSynchInd"),
            new StringPair("NO", "ShareSynchInd"),
            new StringPair("YES", "ShareSynchInd"),
            new StringPair("instances", "ShareSynchInd[]"),
            new StringPair("value", "String"),
            new StringPair("values", "String[]")}),
        new StringObjectPair("anything.ProfileType", new StringPair[] { 
            new StringPair("RPH", "String"),
            new StringPair("accesses", "String"),
            new StringPair("prefCollections", "PrefCollections")}),
        new StringObjectPair("anything.ProfileType.PrefCollections", new StringPair[] { 
            new StringPair("prefCollectionList", "List"),
            new StringPair("privacy", "Privacy")}),
        new StringObjectPair("anything.ProfileType.PrefCollections.PrefCollection", new StringPair[] { 
            new StringPair("prefList", "List"),
            new StringPair("privacy", "Privacy"),
            new StringPair("travelPurpose", "String"),
            new StringPair("vehicleRentalPrefList", "List")}),
        new StringObjectPair("anything.ProfileType.PrefCollections.PrefCollection.Pref", new StringPair[] { 
            new StringPair("namePrefList", "List"),
            new StringPair("phonePrefList", "List"),
            new StringPair("privacy", "Privacy")}),
        new StringObjectPair("anything.ProfileType.PrefCollections.PrefCollection.Pref.PhonePref", new StringPair[] { 
            new StringPair("telephoneAreaCityCode", "String"),
            new StringPair("telephoneCountryAccessCode", "String"),
            new StringPair("telephoneExtension", "String"),
            new StringPair("telephonePhoneNumber", "String"),
            new StringPair("telephonePrivacy", "Privacy"),
            new StringPair("telephoneRPH", "String")}),
    };
    static final StringPair[] OTA_PROFILETYPE_SIMPLIFIED_BINDINGS_B = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='anything.ProfileType' name='element'>\n" +
            "    <structure map-as='ins:CommonProfileTypeType'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:PrivacyGroup-AttributeGroup' class='anything.Privacy'>\n" +
            "    <format type='anything.Privacy$ShareSynchInd' deserializer='anything.Privacy$ShareSynchInd.fromValue'/>\n" +
            "    <format type='anything.Privacy$ShareMarketInd' deserializer='anything.Privacy$ShareMarketInd.fromValue'/>\n" +
            "    <value style='attribute' name='ShareSynchInd' get-method='getShareSynchInd' set-method='setShareSynchInd' usage='optional'/>\n" +
            "    <value style='attribute' name='ShareMarketInd' get-method='getShareMarketInd' set-method='setShareMarketInd' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:CommonProfileTypeType' class='anything.ProfileType'>\n" +
            "    <value style='element' name='Accesses' get-method='getAccesses' set-method='setAccesses' usage='optional'/>\n" +
            "    <structure get-method='getPrefCollections' set-method='setPrefCollections' usage='optional' name='PrefCollections'>\n" +
            "      <collection get-method='getPrefCollectionList' set-method='setPrefCollectionList' create-type='java.util.ArrayList'>\n" +
            "        <structure type='anything.ProfileType$PrefCollections$PrefCollection' name='PrefCollection'>\n" +
            "          <collection get-method='getPrefList' set-method='setPrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "            <structure type='anything.ProfileType$PrefCollections$PrefCollection$Pref' name='CommonPref'>\n" +
            "              <collection get-method='getNamePrefList' set-method='setNamePrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "                <value style='element' name='NamePref' type='java.lang.String'/>\n" +
            "              </collection>\n" +
            "              <collection get-method='getPhonePrefList' set-method='setPhonePrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "                <structure type='anything.ProfileType$PrefCollections$PrefCollection$Pref$PhonePref' name='PhonePref'>\n" +
            "                  <structure name='Telephone'>\n" +
            "                    <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getTelephonePrivacy' set-method='setTelephonePrivacy' usage='optional'/>\n" +
            "                    <value style='attribute' name='CountryAccessCode' get-method='getTelephoneCountryAccessCode' set-method='setTelephoneCountryAccessCode' usage='optional'/>\n" +
            "                    <value style='attribute' name='AreaCityCode' get-method='getTelephoneAreaCityCode' set-method='setTelephoneAreaCityCode' usage='optional'/>\n" +
            "                    <value style='attribute' name='PhoneNumber' get-method='getTelephonePhoneNumber' set-method='setTelephonePhoneNumber'/>\n" +
            "                    <value style='attribute' name='Extension' get-method='getTelephoneExtension' set-method='setTelephoneExtension' usage='optional'/>\n" +
            "                    <value style='attribute' name='RPH' get-method='getTelephoneRPH' set-method='setTelephoneRPH' usage='optional'/>\n" +
            "                  </structure>\n" +
            "                </structure>\n" +
            "              </collection>\n" +
            "              <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacy' set-method='setPrivacy' usage='optional'/>\n" +
            "            </structure>\n" +
            "          </collection>\n" +
            "          <collection get-method='getVehicleRentalPrefList' set-method='setVehicleRentalPrefList' usage='optional' create-type='java.util.ArrayList'>\n" +
            "            <value style='element' name='VehicleRentalPref' type='java.lang.String'/>\n" +
            "          </collection>\n" +
            "          <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacy' set-method='setPrivacy' usage='optional'/>\n" +
            "          <value style='attribute' name='TravelPurpose' get-method='getTravelPurpose' set-method='setTravelPurpose' usage='optional'/>\n" +
            "        </structure>\n" +
            "      </collection>\n" +
            "      <structure map-as='ins:PrivacyGroup-AttributeGroup' get-method='getPrivacy' set-method='setPrivacy' usage='optional'/>\n" +
            "    </structure>\n" +
            "    <value style='attribute' name='RPH' get-method='getRPH' set-method='setRPH' usage='optional'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] TYPE_REPLACEMENT_GENERATION_DATA = new StringObjectPair[] {
        new StringObjectPair("something.Extra", new StringPair[] {
            new StringPair("extra", "String")}),
        new StringObjectPair("anything.Simple1", new StringPair[] { 
            new StringPair("male", "boolean"),
            new StringPair("name", "String"),
            new StringPair("rated", "Float")}),
        new StringObjectPair("anything.Simple3", new StringPair[] { 
            new StringPair("index", "Integer"),
            new StringPair("mixed", "String"),
            new StringPair("rated", "Float"),
            new StringPair("simple1", "Simple1")}),
    };
    static final StringPair[] TYPE_REPLACEMENT_GENERATION_BINDINGS = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='something.Extra' name='extra'>\n" +
            "    <value style='text' get-method='getExtra' set-method='setExtra'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Simple3' name='element'>\n" +
            "    <structure map-as='ins:simple3'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:simple1' class='anything.Simple1'>\n" +
            "    <value style='element' name='name' get-method='getName' set-method='setName'/>\n" +
            "    <value style='element' name='rated' get-method='getRated' set-method='setRated'/>\n" +
            "    <value style='attribute' name='male' get-method='isMale' set-method='setMale'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Simple1' name='simple1'>\n" +
            "    <structure map-as='ins:simple1'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:simple3' class='anything.Simple3'>\n" +
            "    <structure type='anything.Simple1' get-method='getSimple1' set-method='setSimple1'/>\n" +
            "    <value style='element' name='mixed' get-method='getMixed' set-method='setMixed'/>\n" +
            "    <value style='attribute' name='rated' get-method='getRated' set-method='setRated' usage='optional'/>\n" +
            "    <value style='attribute' name='index' get-method='getIndex' set-method='setIndex' usage='optional'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    static final StringObjectPair[] COMBINED_REMOVAL_REPLACEMENT_GENERATION_DATA = new StringObjectPair[] {
        new StringObjectPair("anything.DateTime", new StringPair[] { 
            new StringPair("date", "Date")}),
        new StringObjectPair("anything.Simple1", new StringPair[] { 
            new StringPair("altstamp", "Date"),
            new StringPair("male", "boolean"),
            new StringPair("name", "String"),
            new StringPair("rated", "Float"),
            new StringPair("timestamp", "DateTime")}),
        new StringObjectPair("anything.Simple3", new StringPair[] { 
            new StringPair("mixed", "String"),
            new StringPair("rated", "Float"),
            new StringPair("simple1", "Simple1")}),
    };
    static final StringPair[] COMBINED_REMOVAL_REPLACEMENT_GENERATION_BINDINGS = new StringPair[] {
        new StringPair(null,
            "<binding name='binding' package='' trim-whitespace='true'>\n" +
            "  <include path='urn_somethingBinding.xml'/>\n" +
            "  <include path='urn_anythingBinding.xml'/>\n" +
            "</binding>"),
        new StringPair("urn:something",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:something' default='elements'/>\n" +
            "  <mapping class='anything.Simple3' name='element'>\n" +
            "    <structure map-as='ins:simple3'/>\n" +
            "  </mapping>\n" +
            "</binding>"),
        new StringPair("urn:anything",
            "<binding xmlns:ins='urn:anything' trim-whitespace='true'>\n" +
            "  <namespace uri='urn:anything' default='elements' prefix='ins'/>\n" +
            "  <mapping abstract='true' type-name='ins:simple1' class='anything.Simple1'>\n" +
            "    <value style='element' name='name' get-method='getName' set-method='setName'/>\n" +
            "    <value style='element' name='rated' get-method='getRated' set-method='setRated'/>\n" +
            "    <structure type='anything.DateTime' get-method='getTimestamp' set-method='setTimestamp'/>\n" +
            "    <value style='element' name='altstamp' get-method='getAltstamp' set-method='setAltstamp'/>\n" +
            "    <value style='attribute' name='male' get-method='isMale' set-method='setMale'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.Simple1' name='simple1'>\n" +
            "    <structure map-as='ins:simple1'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:simple3' class='anything.Simple3'>\n" +
            "    <structure type='anything.Simple1' get-method='getSimple1' set-method='setSimple1'/>\n" +
            "    <value style='element' name='mixed' get-method='getMixed' set-method='setMixed'/>\n" +
            "    <value style='attribute' name='rated' get-method='getRated' set-method='setRated' usage='optional'/>\n" +
            "  </mapping>\n" +
            "  <mapping abstract='true' type-name='ins:DateTime' class='anything.DateTime'>\n" +
            "    <value style='text' get-method='getDate' set-method='setDate'/>\n" +
            "  </mapping>\n" +
            "  <mapping class='anything.DateTime' name='timestamp'>\n" +
            "    <structure map-as='ins:DateTime'/>\n" +
            "  </mapping>\n" +
            "</binding>")
    };
    
    private static void dumpImage(StringObjectPair[] classdefs) {
        for (int i = 0; i < classdefs.length; i++) {
            StringObjectPair pair = classdefs[i];
            StringPair[] fielddefs = (StringPair[])pair.getValue();
            System.out.print("        new StringObjectPair(\"");
            System.out.print(pair.getKey());
            System.out.print("\", new StringPair[] { ");
            for (int j = 0; j < fielddefs.length; j++) {
                StringPair fielddef = fielddefs[j];
                System.out.println(j > 0 ? "," : "");
                System.out.print("            new StringPair(\"");
                System.out.print(fielddef.getKey());
                System.out.print("\", \"");
                System.out.print(fielddef.getValue());
                System.out.print("\")");
            }
            System.out.println("}),");
        }
    }
    
    private static void writeBinding(BindingHolder holder, OutputStream os) throws JiBXException, IOException {
        IBindingFactory fact = BindingDirectory.getFactory("normal", BindingElement.class);
        IMarshallingContext ictx = fact.createMarshallingContext();
        ictx.setIndent(2);
        ictx.setOutput(os, null);
        ((IMarshallable)holder.getBinding()).marshal(ictx);
        ictx.getXmlWriter().flush();
    }
    
    private static String bindingText(BindingHolder holder) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeBinding(holder, bos);
        return new String(bos.toByteArray(), "utf-8");
    }
    
    private static void verifyBinding(String match, BindingHolder holder) throws Exception {
        StringReader rdr1 = new StringReader(match);
        String gen = bindingText(holder);
        StringReader rdr2 = new StringReader(gen);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream pstr = new PrintStream(bos);
        DocumentComparator comp = new DocumentComparator(pstr);
        boolean same = comp.compare(rdr1, rdr2);
        if (!same) {
            pstr.close();
            fail("Generated binding error:\n" + new String(bos.toByteArray()) + "\nGenerated binding:\n" + gen
                + "\nExpected binding:\n" + match);
        }
    }
    
    private void testDump(SchemasetCustom custom, SchemaElement[] schemas) throws JiBXException, IOException {
        CodeGen generator = new CodeGen(custom, m_validationContext);
        ProblemHandler handler = new ProblemConsoleLister();
        assertTrue("Schema customization failure", generator.customizeSchemas("dflt", handler));
        generator.applyAndNormalize();
        generator.pruneDefinitions();
        ValidationUtils.validateSchemas(schemas, m_validationContext);
        assertFalse("Errors in schema validation", m_validationContext.reportProblems(handler));
        String pack = generator.buildDataModel(false, false, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
        ArrayList packages = generator.getPackageDirectory().getPackages();
        System.out.println("Data model dump:");
        for (int i = 0; i < packages.size(); i++) {
            PackageHolder packhold = (PackageHolder)packages.get(i);
            StringObjectPair[] classdefs = packhold.getClassFields();
            dumpImage(classdefs);
        }
        System.out.println("Bindings dump:");
        BindingOrganizer bindorg = generator.getBindingDirectory();
        bindorg.configureFiles("binding", pack, Collections.EMPTY_LIST);
        for (Iterator iter = bindorg.iterateBindings(); iter.hasNext();) {
            writeBinding((BindingHolder)iter.next(), System.out);
        }
    }
    
    private void testGeneration(SchemasetCustom custom, SchemaElement[] schemas, StringObjectPair[] image,
        StringPair[] bindings) throws Exception {
        CodeGen generator = new CodeGen(custom, m_validationContext);
        ProblemHandler handler = new ProblemConsoleLister();
        assertTrue("Schema customization failure", generator.customizeSchemas("dflt", handler));
        generator.applyAndNormalize();
        generator.pruneDefinitions();
        ValidationUtils.validateSchemas(schemas, m_validationContext);
        assertFalse("Errors in schema validation", m_validationContext.reportProblems(handler));
        String pack = generator.buildDataModel(false, false, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
        StringObjectPair[] classpairs = DataModelUtils.getImage(generator.getPackageDirectory());
        String diff = DataModelUtils.imageDiff(image, classpairs);
        if (diff != null) {
            fail(diff);
        }
        Map bindmap = new HashMap();
        for (int i = 0; i < bindings.length; i++) {
            StringPair bindpair = bindings[i];
            bindmap.put(bindpair.getKey(), bindpair.getValue());
        }
        BindingOrganizer bindorg = generator.getBindingDirectory();
        bindorg.configureFiles("binding", pack, Collections.EMPTY_LIST);
        for (Iterator iter = bindorg.iterateBindings(); iter.hasNext();) {
            BindingHolder holder = (BindingHolder)iter.next();
            if (holder != null) {
                String match = (String)bindmap.get(holder.getNamespace());
                assertNotNull("Found unexpected binding for namespace " + holder.getNamespace() + ": " +
                    bindingText(holder), match);
                verifyBinding(match, holder);
            }
        }
    }
    
    public void testGenerationNoCustomization() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.RESOLVER1) };
        testGeneration(new SchemasetCustom((SchemasetCustom)null), schemas, DATA1, BINDINGS1);
    }
    
    public void testGenerationSimpleTypeReplacement() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.RESOLVER3) };
        SchemasetCustom custom = loadCustomization(CodegenData.CUSTOMIZATION3_A);
        testGeneration(custom, schemas, DATA3_A, BINDINGS3_A);
    }
  
    public void testGenerationOnlyUsed() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.RESOLVER3) };
        SchemasetCustom custom = loadCustomization(CodegenData.CUSTOMIZATION3_B);
        testGeneration(custom, schemas, DATA3_B, BINDINGS3_B);
    }
    
    public void testGenerationOnlyUsedPreferInline() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.RESOLVER3) };
        SchemasetCustom custom = loadCustomization(CodegenData.CUSTOMIZATION3_C);
        testGeneration(custom, schemas, DATA3_C, BINDINGS3_C);
    }
    
    public void testGenerationSimplifiedOnlyUsedPreferInline() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.RESOLVER3) };
        SchemasetCustom custom = loadCustomization(CodegenData.CUSTOMIZATION3_D);
        testGeneration(custom, schemas, DATA3_D, BINDINGS3_D);
    }
    
    public void testGenerationOTAProfileTypeSimplifiedUnqualified() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] {
            loadSchema(CodegenData.OTA_PROFILETYPE_SIMPLIFIED_UNQUALIFIED_RESOLVER) };
        SchemasetCustom custom = loadCustomization(CodegenData.OTA_PROFILETYPE_SIMPLIFIED_CUSTOMIZATION_A);
        testGeneration(custom, schemas, OTA_PROFILETYPE_SIMPLIFIED_UNQUALIFIED_DATA_A,
            OTA_PROFILETYPE_SIMPLIFIED_UNQUALIFIED_BINDINGS_A);
    }
    
    public void testGenerationOTAProfileTypeSimplified() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.OTA_PROFILETYPE_SIMPLIFIED_RESOLVER) };
        SchemasetCustom custom = loadCustomization(CodegenData.OTA_PROFILETYPE_SIMPLIFIED_CUSTOMIZATION_A);
        testGeneration(custom, schemas, OTA_PROFILETYPE_SIMPLIFIED_DATA_A, OTA_PROFILETYPE_SIMPLIFIED_BINDINGS_A);
    }
    
    public void testGenerationOTAProfileTypeSimplifiedNoUnused() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.OTA_PROFILETYPE_SIMPLIFIED_RESOLVER) };
        SchemasetCustom custom = loadCustomization(CodegenData.OTA_PROFILETYPE_SIMPLIFIED_CUSTOMIZATION_B);
        testGeneration(custom, schemas, OTA_PROFILETYPE_SIMPLIFIED_DATA_B, OTA_PROFILETYPE_SIMPLIFIED_BINDINGS_B);
    }
    
    public void testTypeReplacementGeneration() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.RESOLVER1) };
        SchemasetCustom custom = loadCustomization(CodegenData.CUSTOMIZATION1_B);
        testGeneration(custom, schemas, TYPE_REPLACEMENT_GENERATION_DATA, TYPE_REPLACEMENT_GENERATION_BINDINGS);
    }
    
    public void testCombinedRemovalReplacementGeneration() throws Exception {
        SchemaElement[] schemas = new SchemaElement[] { loadSchema(CodegenData.RESOLVER2) };
        SchemasetCustom custom = loadCustomization(CodegenData.CUSTOMIZATION2_B);
        testGeneration(custom, schemas, COMBINED_REMOVAL_REPLACEMENT_GENERATION_DATA,
            COMBINED_REMOVAL_REPLACEMENT_GENERATION_BINDINGS);
    }
}

package simple;

class MyClass4Format
{
    /*package*/ static String yesNoSerializer(boolean flag) {
        return flag ? "yes" : "no";
    }
    
    /*package*/ static boolean yesNoDeserializer(String text) {
        return "yes".equals(text);
    }
}

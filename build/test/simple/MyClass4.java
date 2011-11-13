
package simple;

import java.util.Vector;

class MyClass4 {
	private int a;
	private String b;
	private double c;
	private Boolean d;
	private Integer e;
	private Integer f;
    private String g;
    private boolean h;
    private boolean i;
    private boolean j;
    private byte[] k;
    private byte l;
    private byte m;
    private char n;
    private String o;
//#!j2me{
    private java.sql.Date p;
//#j2me}
    private String q;
    private String r;
    private String s;
	private Vector intsAndStrings;
    
    private boolean testG() {
        return g != null;
    }
    
    private boolean testJ() {
        return j;
    }
    
    private String getJ() {
        return "";
    }
    
    private void setJ(String value) {
        j = value != null;
    }
    
    private void setO(String value) {
        o = value;
    }
    
    private boolean testO() {
        return o != null && !"".equals(o);
    }
	
	private static String serializeInteger(Integer value) {
		return value.toString();
	}
    
    private static String yesNoSerializer(boolean flag) {
        return flag ? "yes" : "no";
    }
    
    private static boolean yesNoDeserializer(String text) {
        return "yes".equals(text);
    }
    
    private void postSet() {
        if (q != null && q.length() == 0) {
            q = null;
        }
        if (r != null && r.length() == 0) {
            r = null;
        }
    }
    
    private static class IntWrapper
    {
        private int ivalue;
    }
    
    protected static String serializeNumber(Number num) {
        return num.toString();
    }
    
    protected static Number deserializeNumber(String value) {
        if (value.indexOf('.') >= 0) {
            return new Float(value);
        } else {
            return new Integer(value);
        }
    }
    
    private static String reverse(String string) {
        StringBuffer buff = new StringBuffer(string.length());
        for (int i = string.length() - 1; i >= 0 ; i--) {
            buff.append(string.charAt(i));
        }
        return buff.toString();
    }
    
    protected static String serializeReverse(Number num) {
        return reverse(serializeNumber(num));
    }
    
    protected static Number deserializeReverse(String value) {
        return deserializeNumber(reverse(value));
    }
}

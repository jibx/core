
package simple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

class MyClass2 {
	private int a;
	private String b;
	private double c;
	private boolean d;
	private Object e;
	private Integer f;
	private Integer g;
    private DecimalFormat format1;
    private DecimalFormat format2;
	private List ints;
    private Set orderedStrings;

	public int getA() { return a; }
	public void setA(String a) { this.a = Integer.parseInt(a); }
	public void setA(int a) { this.a = a; }

	public String getB() { return b; }
	public void setB(String b) { this.b = b; }
	public void setB(int b) { this.b = Integer.toString(b); }
    
    public double getC() { return c; }
    public void setC(double c) { this.c = c; }
	
	private Object getG() {
		return g;
	}
	
	private void setG(Object value) {
		g = (Integer)value;
	}
    
    private List getList() {
        return ints;
    }
    
    private void setList(ArrayList list) {
        ints = list;
    }
    
    private static List listFactory() {
        return new ArrayList();
    }
    
    private static ArrayList arrayListFactory() {
        return new ArrayList();
    }
    
    private static SortedSet sortedSetFactory() {
        return new TreeSet();
    }
}

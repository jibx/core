
package simple;

import java.text.DecimalFormat;
import java.util.ArrayList;

class MyClass1 extends MyClass1Base {
	private int a;
	private String b;
    private DecimalFormat format1;
    private DecimalFormat format2;
	private ArrayList ints;
    
    // force generation of default constructor
    public MyClass1(int a) {
        super(a);
        this.a = a;
    }

	public int getA() { return a; }
	public void setA(int a) { this.a = a; }

	public String getB() { return b; }
	public void setB(String b) { this.b = b; }
}

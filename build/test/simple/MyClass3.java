
package simple;

import java.text.DecimalFormat;
import java.util.ArrayList;

class MyClass3 {
	private int a;
	private String b;
	private double c;
	private boolean d;
    private char e;
    private DecimalFormat format1;
    private DecimalFormat format2;
	private int[] ints;
	private Integer[] integers;
    private long[] longs;
    
    private void addInt(Integer value) {
        if (integers == null) {
            integers = new Integer[1];
            integers[0] = value;
        } else {
            int length = integers.length;
            Integer[] newints = new Integer[length+1];
            System.arraycopy(integers, 0, newints, 0, length);
            newints[length] = value;
            integers = newints;
        }
    }
    private int sizeInts() {
        if (integers == null) {
            return 0;
        } else {
            return integers.length;
        }
    }
    private Integer getInt(int index) {
        return integers[index];
    }
    private boolean hasInts() {
        return integers != null;
    }
}

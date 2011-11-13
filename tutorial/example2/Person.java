
package example2;

public class Person {
    private int customerNumber;
    private String firstName;
    private String lastName;
    protected int getNumber() {
        return customerNumber;
    }
    protected void setNumber(int num) {
        customerNumber = num;
    }
}

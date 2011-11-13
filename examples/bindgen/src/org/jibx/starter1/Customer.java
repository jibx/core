
package org.jibx.starter1;

import java.util.List;

/**
 * Customer information.
 */
public class Customer
{
    private long customerNumber;
    
    /** Personal name. */
    private String firstName;
    
    /** Family name. */
    private String lastName;
    
    /** Middle name(s), if any. */
    private List<String> middleNames;
    
    private transient String a;
    
    private static String b;
    
    private final String c = "C";

    public long getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(long customerId) {
        this.customerNumber = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public List<String> getMiddleNames() {
        return middleNames;
    }
    
    public void setMiddleNames(List<String> middleNames) {
        this.middleNames = middleNames;
    }
}
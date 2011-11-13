
package org.jibx.starter2;

import java.util.List;

/**
 * Customer information.
 */
public class Customer
{
    private long m_customerNumber;
    
    /** Personal name. */
    private String m_firstName;
    
    /** Family name. */
    private String m_lastName;
    
    /** Middle name(s), if any. */
    private List m_middleNames;

    public long getCustomerNumber() {
        return m_customerNumber;
    }

    public void setCustomerNumber(long customerId) {
        m_customerNumber = customerId;
    }

    public String getFirstName() {
        return m_firstName;
    }

    public void setFirstName(String firstName) {
        m_firstName = firstName;
    }

    public String getLastName() {
        return m_lastName;
    }

    public void setLastName(String lastName) {
        m_lastName = lastName;
    }
    
    public List getMiddleNames() {
        return m_middleNames;
    }
    
    public void setMiddleNames(List middleNames) {
        m_middleNames = middleNames;
    }
}
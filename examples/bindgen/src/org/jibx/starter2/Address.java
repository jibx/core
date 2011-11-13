package org.jibx.starter2;

/**
 * Address information.
 */
public class Address
{
    /** First line of street information (required). */
    private String m_street1;
    
    /** Second line of street information (optional). */
    private String m_street2;
    
    private String m_city;
    
    /** State abbreviation (required for the U.S. and Canada, optional otherwise). */
    private String m_state;
    
    /** Postal code (required for the U.S. and Canada, optional otherwise). */
    private String m_postCode;
    
    /** Country name (optional, U.S. assumed if not supplied). */
    private String m_country;

    public String getStreet1() {
        return m_street1;
    }

    public void setStreet1(String street1) {
        m_street1 = street1;
    }

    public String getStreet2() {
        return m_street2;
    }

    public void setStreet2(String street2) {
        m_street2 = street2;
    }

    public String getCity() {
        return m_city;
    }

    public void setCity(String city) {
        m_city = city;
    }

    public String getState() {
        return m_state;
    }

    public void setState(String state) {
        m_state = state;
    }

    public String getPostCode() {
        return m_postCode;
    }

    public void setPostCode(String postCode) {
        m_postCode = postCode;
    }

    public String getCountry() {
        return m_country;
    }

    public void setCountry(String country) {
        m_country = country;
    }
}
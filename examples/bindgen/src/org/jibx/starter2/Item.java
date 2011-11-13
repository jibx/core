package org.jibx.starter2;

/**
 * Order line item information.
 */
public class Item
{
    /** Stock identifier. This is expected to be 12 characters in length, with two
     leading alpha characters followed by ten decimal digits. */
    private String m_id;
    
    /** Text description of item. */
    private String m_description;
    
    /** Number of units ordered. */
    private int m_quantity;
    
    /** Price per unit. */
    private float m_price;

    public String getId() {
        return m_id;
    }

    public void setId(String id) {
        m_id = id;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }

    public int getQuantity() {
        return m_quantity;
    }

    public void setQuantity(int quantity) {
        m_quantity = quantity;
    }

    public float getPrice() {
        return m_price;
    }

    public void setPrice(float price) {
        m_price = price;
    }
}
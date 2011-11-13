package org.jibx.starter1;

/**
 * Order line item information.
 */
public class Item
{
    /** Stock identifier. This is expected to be 12 characters in length, with two
     leading alpha characters followed by ten decimal digits. */
    private String id;
    
    /** Text description of item. */
    private String description;
    
    /** Number of units ordered. */
    private int quantity;
    
    /** Price per unit. */
    private float price;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
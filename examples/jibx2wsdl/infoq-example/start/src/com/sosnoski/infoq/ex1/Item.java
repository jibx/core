package com.sosnoski.infoq.ex1;

public class Item
{
    private String id;
    private int quantity;
    private float price;
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }
    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    /**
     * @return the price
     */
    public float getPrice() {
        return price;
    }
    /**
     * @param price the price to set
     */
    public void setPrice(float price) {
        this.price = price;
    }
}

package org.jibx.starter1;

import java.sql.Date;
import java.util.List;

/**
 * Order information.
 */
public class Order
{
    private long orderNumber;
    
    private Customer customer;
    
    /** Billing address information. */
    private Address billTo;
    
    private Shipping shipping;
    
    /** Shipping address information. If missing, the billing address is also used as the
     shipping address. */
    private Address shipTo;
    
    private List<Item> items;
    
    /** Date order was placed with server. */
    private Date orderDate;
    
    /** Date order was shipped. This will be <code>null</code> if the order has not
     yet shipped. */
    private Date shipDate;
    
    private Float total;

    public long getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(long orderId) {
        this.orderNumber = orderId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getBillTo() {
        return billTo;
    }

    public void setBillTo(Address billTo) {
        this.billTo = billTo;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
    }

    public Address getShipTo() {
        return shipTo;
    }

    public void setShipTo(Address shipTo) {
        this.shipTo = shipTo;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getShipDate() {
        return shipDate;
    }

    public void setShipDate(Date shipDate) {
        this.shipDate = shipDate;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }
}
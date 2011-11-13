package com.sosnoski.infoq.ex1;

import java.sql.Date;
import java.util.List;

/**
 * Order information.
 */
public class Order
{
    /** Unique identifier for this order. This is added to the order information by the service. */
    private String orderId;
    
    /** Customer identifier code. */
    private String customerId;
    
    /** Customer name. */
    private String customerName;
    
    /** Billing address information. */
    private Address billTo;
    
    /** Shipping address information. If missing, the billing address is also used as the shipping address. */
    private Address shipTo;
    
    /** Line items in order. */
    private List items;
    
    /** Date order was placed with server. This is added to the order information by the service. */
    private Date orderDate;
    
    /** Date order was shipped. This is added to the order information by the service. */
    private Date shipDate;

    /**
     * @return the orderId
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * @param orderId the orderId to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * @return the customerId
     */
    public String getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId the customerId to set
     */
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    /**
     * @return the customerName
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @param customerName the customerName to set
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * @return the billTo
     */
    public Address getBillTo() {
        return billTo;
    }

    /**
     * @param billTo the billTo to set
     */
    public void setBillTo(Address billTo) {
        this.billTo = billTo;
    }

    /**
     * @return the shipTo
     */
    public Address getShipTo() {
        return shipTo;
    }

    /**
     * @param shipTo the shipTo to set
     */
    public void setShipTo(Address shipTo) {
        this.shipTo = shipTo;
    }

    /**
     * @return the items
     */
    public List getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List items) {
        this.items = items;
    }

    /**
     * @return the orderDate
     */
    public Date getOrderDate() {
        return orderDate;
    }

    /**
     * @param orderDate the orderDate to set
     */
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * @return the shipDate
     */
    public Date getShipDate() {
        return shipDate;
    }

    /**
     * @param shipDate the shipDate to set
     */
    public void setShipDate(Date shipDate) {
        this.shipDate = shipDate;
    }
}
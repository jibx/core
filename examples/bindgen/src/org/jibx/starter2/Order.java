package org.jibx.starter2;

import java.sql.Date;
import java.util.List;

/**
 * Order information.
 */
public class Order
{
    private long m_orderNumber;
    
    private Customer m_customer;
    
    /** Billing address information. */
    private Address m_billTo;
    
    private Shipping m_shipping;
    
    /** Shipping address information. If missing, the billing address is also used as the
     shipping address. */
    private Address m_shipTo;
    
    private List m_items;
    
    /** Date order was placed with server. */
    private Date m_orderDate;
    
    /** Date order was shipped. This will be <code>null</code> if the order has not
     yet shipped. */
    private Date m_shipDate;
    
    private Float total;

    public long getOrderNumber() {
        return m_orderNumber;
    }

    public void setOrderNumber(long orderId) {
        m_orderNumber = orderId;
    }

    public Customer getCustomer() {
        return m_customer;
    }

    public void setCustomer(Customer customer) {
        m_customer = customer;
    }

    public Address getBillTo() {
        return m_billTo;
    }

    public void setBillTo(Address billTo) {
        m_billTo = billTo;
    }

    public Shipping getShipping() {
        return m_shipping;
    }

    public void setShipping(Shipping shipping) {
        m_shipping = shipping;
    }

    public Address getShipTo() {
        return m_shipTo;
    }

    public void setShipTo(Address shipTo) {
        m_shipTo = shipTo;
    }

    public List getItems() {
        return m_items;
    }

    public void setItems(List items) {
        m_items = items;
    }

    public Date getOrderDate() {
        return m_orderDate;
    }

    public void setOrderDate(Date orderDate) {
        m_orderDate = orderDate;
    }

    public Date getShipDate() {
        return m_shipDate;
    }

    public void setShipDate(Date shipDate) {
        m_shipDate = shipDate;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        total = total;
    }
}
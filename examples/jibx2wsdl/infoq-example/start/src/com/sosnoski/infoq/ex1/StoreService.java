package com.sosnoski.infoq.ex1;

/**
 * Interface for placing orders and checking status.
 */
public interface StoreService
{
    /**
     * Submit a new order.
     * 
     * @param order
     * @return id
     */
    public String placeOrder(Order order);
    
    /**
     * Retrieve order information.
     * 
     * @param id order identifier
     * @return order information
     */
    public Order retrieveOrder(String id);
    
    /**
     * Cancel order. This can only be used for orders which have not been shipped.
     * 
     * @param id order identifier
     * @return <code>true</code> if order cancelled, <code>false</code> if alread shipped
     */
    public boolean cancelOrder(String id);
}

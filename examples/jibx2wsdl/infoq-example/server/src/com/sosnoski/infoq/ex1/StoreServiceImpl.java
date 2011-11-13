package com.sosnoski.infoq.ex1;

import java.util.ArrayList;
import java.sql.Date;
import java.util.List;

/**
 * Simple service implementation for testing.
 */
public class StoreServiceImpl extends StoreServiceSkeleton implements StoreService
{
    private static List orderList = new ArrayList();

    public boolean cancelOrder(String id) {
        int index = Integer.parseInt(id)-1;
        if (index >= 0 && index < orderList.size()) {
            orderList.set(index, null);
            return true;
        }
        return false;
    }

    public String placeOrder(Order order) {
        orderList.add(order);
        String id = Integer.toString(orderList.size());
        order.setOrderId(id);
        order.setOrderDate(new Date(System.currentTimeMillis()));
        return id;
    }

    public Order retrieveOrder(String id) {
        int index = Integer.parseInt(id)-1;
        if (index >= 0 && index < orderList.size()) {
            return (Order)orderList.get(index);
        }
        return null;
    }
}
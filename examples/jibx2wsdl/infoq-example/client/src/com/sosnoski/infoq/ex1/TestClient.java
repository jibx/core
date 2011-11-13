package com.sosnoski.infoq.ex1;

import java.util.ArrayList;
import java.util.List;

public class TestClient
{
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        StoreService client;
        if (args.length > 0) {
            client = new StoreServiceStub(args[0]);
        } else {
            client = new StoreServiceStub();
        }
        Address addr = new Address();
        addr.setCity("Redmond");
        addr.setState("WA");
        addr.setStreet1("13488 NE 187st St.");
        addr.setZip("98034");
        List items = new ArrayList();
        Item item = new Item();
        item.setId("ACN399393");
        item.setPrice(1.25f);
        item.setQuantity(5);
        items.add(item);
        item = new Item();
        item.setId("UHX831348");
        item.setPrice(10.35f);
        item.setQuantity(3);
        items.add(item);
        Order order = new Order();
        order.setBillTo(addr);
        order.setCustomerId("A10101");
        order.setCustomerName("Dennis Sosnoski");
        order.setItems(items);
        String id = client.placeOrder(order);
        order = client.retrieveOrder(id);
    }
}

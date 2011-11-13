/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sosnoski.ws.library.jibx2wsdl;

import java.util.List;

/**
 * Web service client for book server. This runs through a test of the service
 * methods, first retrieving a book, then adding a book, and finally retrieving
 * all books of a particular type.
 */
public class WebServiceClient
{
    public static void main(String[] args) throws Exception {
        
        // allow override of target address
        String host = args.length > 0 ? args[0] : "localhost";
        String port = args.length > 1 ? args[1] : "8080";
        String target = "http://" + host + ":" + port +
            "/axis2/services/LibraryServer1";
        
        // create the server instance
        LibraryServer1Stub stub = new LibraryServer1Stub(target);
        
        // retrieve a book directly
        String isbn = "0061020052";
        Item item = stub.getItem(isbn);
        if (item == null) {
            System.out.println("No item found with ISBN '" + isbn + '\'');
        } else if (item instanceof Book){
            Book book = (Book)item;
            System.out.println("Retrieved '" + book.getTitle() + "' of format " + book.getFormat());
        }
        
        // retrieve the list of types defined
        List<Type> types = stub.getTypes();
        System.out.println("Retrieved " + types.size() + " types:");
        for (int i = 0; i < types.size(); i++) {
            System.out.println(" '" + types.get(i).getName() + "' with " +
                types.get(i).getCount() + " items");
        }
        
        // add a new book
        String title = "The Dragon Never Sleeps";
        isbn = "0445203498";
        try {
            Book book = new Book("scifi", isbn, Book.Format.POCKET_PAPERBACK,
                title, new String[] { "Cook, Glen" });
            stub.addItem(book);
            System.out.println("Added '" + title + '\'');
            title = "This Should Not Work";
            book = new Book("xml", isbn, Book.Format.TRADE_PAPERBACK,
                title, new String[] { "Nobody, Ima" });
            stub.addItem(book);
            System.out.println("Added duplicate book - should not happen!");
        } catch (AddDuplicateFault e) {
            System.out.println("Failed adding '" + title +
                "' with ISBN '" + isbn + "' - matches existing title '" +
                e.getFaultMessage().getItem().getTitle() + '\'');
        }
        
        // add a new dvd
        title = "Lord of the Rings: The Two Towers";
        String id = "B00005JKZV";
        try {
            Dvd dvd = new Dvd("fant", id, title, "Jackson, Peter",
                new String[] { "Baker, Sala", "Blanchett, Cate",
                "Bloom, Orlando", "McKellen, Ian", "Mortensen, Viggo",
                "Tyler, Liv", "Wood, Elijah" });
            stub.addItem(dvd);
            System.out.println("Added '" + title + '\'');
        } catch (AddDuplicateFault e) {
            System.out.println("Failed adding '" + title +
                "' with ID '" + id + "' - matches existing title '" +
                e.getFaultMessage().getItem().getTitle() + '\'');
        }
        
        // create a callback instance
        ItemsByTypeCallback cb = new ItemsByTypeCallback();
        
        // retrieve all books of a type asynchronously
        stub.startgetItemsByType("scifi", cb);
        long start = System.currentTimeMillis();
        synchronized (cb) {
            while (!cb.m_done) {
                try {
                    cb.wait(100L);
                } catch (Exception e) {}
            }
        }
        List<Item> items = cb.m_items;
        System.out.println("Asynchronous operation took " +
            (System.currentTimeMillis()-start) + " millis");
        if (items != null) {
            System.out.println("Retrieved " + items.size() +
                " items of type 'scifi':");
            for (int i = 0; i < items.size(); i++) {
                System.out.println(" '" + items.get(i).getTitle() + '\'');
            }
        } else {
            System.out.println("Returned exception:");
            cb.m_exception.printStackTrace(System.out);
        }
    }
    
    public static class ItemsByTypeCallback extends LibraryServer1CallbackHandler
    {
        private boolean m_done;
        private Exception m_exception;
        private List<Item> m_items;
        
        public void receiveResultgetItemsByType(List rsp) {
            m_items = rsp;
            m_done = true;
        }

        public synchronized void receiveErrorgetItemsByType(Exception e) {
            m_done = true;
        }
    };
}
/*
 * Copyright 2007 The Apache Software Foundation.
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
            "/axis2/services/BookServer1";
        
        // create the server instance
        BookServer1Stub stub = new BookServer1Stub(target);
        
        // retrieve a book directly
        String isbn = "0061020052";
        Book book = stub.getBook(isbn);
        if (book == null) {
            System.out.println("No book found with ISBN '" + isbn + '\'');
        } else {
            System.out.println("Retrieved '" + book.getTitle() + '\'');
        }
        
        // retrieve the list of types defined
        List<Type> types = stub.getTypes();
        System.out.println("Retrieved " + types.size() + " types:");
        for (int i = 0; i < types.size(); i++) {
            System.out.println(" '" + types.get(i).getName() + "' with " +
                types.get(i).getCount() + " books");
        }
        
        // add a new book
        String title = "The Dragon Never Sleeps";
        isbn = "0445203498";
        try {
            book = new Book("scifi", isbn, title, new String[] { "Cook, Glen" });
            stub.addBook(book);
            System.out.println("Added '" + title + '\'');
            title = "This Should Not Work";
            book = new Book("xml", isbn, title, new String[] { "Nobody, Ima" });
            stub.addBook(book);
            System.out.println("Added duplicate book - should not happen!");
        } catch (AddDuplicateFault e) {
            System.out.println("Failed adding '" + title +
                "' with ISBN '" + isbn + "' - matches existing title '" +
                e.getFaultMessage().getBook().getTitle() + '\'');
        }
        
        // create a callback instance
        BooksByTypeCallback cb = new BooksByTypeCallback();
        
        // retrieve all books of a type asynchronously
        stub.startgetBooksByType("scifi", cb);
        long start = System.currentTimeMillis();
        synchronized (cb) {
            while (!cb.m_done) {
                try {
                    cb.wait(100L);
                } catch (Exception e) {}
            }
        }
        List<Book> books = cb.m_books;
        System.out.println("Asynchronous operation took " +
            (System.currentTimeMillis()-start) + " millis");
        if (cb.m_books != null) {
            System.out.println("Retrieved " + books.size() +
                " books of type 'scifi':");
            for (int i = 0; i < books.size(); i++) {
                System.out.println(" '" + books.get(i).getTitle() + '\'');
            }
        } else {
            System.out.println("Returned exception:");
            cb.m_exception.printStackTrace(System.out);
        }
    }
    
    public static class BooksByTypeCallback extends BookServer1CallbackHandler
    {
        private boolean m_done;
        private Exception m_exception;
        private List m_books;
        
        public void receiveResultgetBooksByType(List resp) {
            m_books = resp;
            m_done = true;
        }

        public synchronized void receiveErrorgetBooksByType(Exception e) {
            m_done = true;
        }
    };
}
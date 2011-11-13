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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Book service implementation. This creates an initial library of books when
 * the class is loaded, then supports method calls to access the library
 * information (including adding new books).
 */
public class BookServer1
{
    private static final Type[] m_types = new Type[] {
        newType("java", "About Java"),
        newType("scifi", "Science fiction"),
        newType("xml", "About XML")
    };
    private static final Map m_bookMap;
    private static final List m_bookList;
    static {
        m_bookMap = new HashMap();
        m_bookList = new ArrayList();
        internalAdd(newBook("java", "0136597238", "Thinking in Java",
            new String[] { "Eckel, Bruce" }));
        internalAdd(newBook("xml", "0130655678", "Definitive XML Schema",
            new String[] { "Walmsley, Priscilla" }));
        internalAdd(newBook("scifi", "0061020052", "Infinity Beach",
            new String[] { "McDevitt, Jack" }));
        internalAdd(newBook("scifi", "0812514092", "Aristoi",
            new String[] { "Williams, Walter Jon" }));
        internalAdd(newBook("java", "0201633612", "Design Patterns",
            new String[] { "Gamma, Erich", "Helm, Richard", "Johnson, Ralph",
                "Vlissides, John"}));
        internalAdd(newBook("scifi", "0345253884", "Roadmarks",
            new String[] { "Zelazny, Roger" }));
        internalAdd(newBook("xml", "0596002521", "XML Schema",
            new String[] { "van der Vlist, Eric" }));
        internalAdd(newBook("java", "0079132480",
            "Inside the Java Virtual Machine",
            new String[] { "Venners, Bill" }));
    }
    
    private static Type newType(String name, String desc) {
        return new Type(name, desc);
    }
    
    private static Book newBook(String type, String isbn,
        String title, String[] authors) {
        return new Book(type, isbn, title, authors);
    }

    private static boolean internalAdd(Book book) {
        if (m_bookMap.containsKey(book.getIsbn())) {
            return false;
        } else {
            m_bookMap.put(book.getIsbn(), book);
            m_bookList.add(book);
            for (int i = 0; i < m_types.length; i++) {
                Type type = m_types[i];
                if (book.getType().equals(m_types[i].getName())) {
                    type.setCount(type.getCount()+1);
                    break;
                }
            }
            return true;
        }
    }
    
    /**
     * Get the book with a particular ISBN.
     * 
     * @param isbn
     * @return book
     */
    public synchronized Book getBook(String isbn) {
        return (Book)m_bookMap.get(isbn);
    }
    
    /**
     * Get all books of a particular type.
     * 
     * @param type short name of type
     * @return books
     */
    public synchronized List<Book> getBooksByType(String type) {
        ArrayList<Book> matches = new ArrayList<Book>();
        for (int i = 0; i < m_bookList.size(); i++) {
            Book book = (Book)m_bookList.get(i);
            if (type.equals(book.getType())) {
                matches.add(book);
            }
        }
        return matches;
    }
    
    /**
     * Get information on all types.
     * 
     * @return types
     */
    public List<Type> getTypes() {
        ArrayList<Type> types = new ArrayList<Type>();
        for (int i = 0; i < m_types.length; i++) {
            types.add(m_types[i]);
        }
        return types;
    }
    
    /**
     * Add a new book.
     * 
     * @param book
     * @throws AddDuplicateException
     */
    public synchronized void addBook(Book book) throws AddDuplicateException {
        Book prior = getBook(book.getIsbn());
        if (prior == null) {
            internalAdd(book);
        } else {
            throw new AddDuplicateException(prior);
        }
    }
}
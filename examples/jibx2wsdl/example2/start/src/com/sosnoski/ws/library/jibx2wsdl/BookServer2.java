/*
Copyright (c) 2007, Dennis M. Sosnoski
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.sosnoski.ws.library.jibx2wsdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Book server class. This creates an initial library of books when the class is
 * loaded, then supports method calls to access the library information
 * (including adding new books).
 */
public class BookServer2
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
    
    public synchronized Book getBook(String isbn) {
        return (Book)m_bookMap.get(isbn);
    }
    
    public synchronized Book[] getBooksByType(String type) {
        ArrayList matches = new ArrayList();
        for (int i = 0; i < m_bookList.size(); i++) {
            Book book = (Book)m_bookList.get(i);
            if (type.equals(book.getType())) {
                matches.add(book);
            }
        }
        return (Book[])matches.toArray(new Book[matches.size()]);
    }
    
    public Type[] getTypes() {
        return m_types;
    }
    
    public synchronized void addBook(Book book) throws AddDuplicateException {
        Book prior = getBook(book.getIsbn());
        if (prior == null) {
            internalAdd(book);
        } else {
            throw new AddDuplicateException(prior);
        }
    }
}
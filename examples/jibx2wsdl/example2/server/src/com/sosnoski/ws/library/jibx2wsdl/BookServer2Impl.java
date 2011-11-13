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

/**
 * Implementation class for service. This extends the generated skeleton code,
 * overriding the methods to call the actual service code.
 */
public class BookServer2Impl extends BookServer2Skeleton
{
    private final BookServer2 m_server;
    
    public BookServer2Impl() {
        m_server = new BookServer2();
    }

    /**
     * Auto generated method signature
     * 
     * @param addBook
     * 
     */
    public void addBook(Book book)
    throws AddDuplicateFault {
        try {
            m_server.addBook(book);
        } catch (AddDuplicateException e) {
            AddDuplicateFault fault =
                new AddDuplicateFault(e.getMessage(), e);
            fault.setFaultMessage(e.getData());
            throw fault;
        }
    }

    /**
     * Auto generated method signature
     * 
     * @param getBook
     * 
     */
    public Book getBook(String isbn) {
        return m_server.getBook(isbn);
    }

    /**
     * Auto generated method signature
     * 
     * @param getBooksByType
     * 
     */
    public Book[] getBooksByType(String type) {
        return m_server.getBooksByType(type);
    }

    /**
     * Auto generated method signature
     * 
     * 
     */
    public Type[] getTypes() {
        return m_server.getTypes();
    }
}
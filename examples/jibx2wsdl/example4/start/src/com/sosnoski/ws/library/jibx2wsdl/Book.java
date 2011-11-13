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
 * Information about a book.
 */
public class Book implements Item
{
    public enum Format { HARDCOVER, TRADE_PAPERBACK, POCKET_PAPERBACK }
    
    private String m_isbn;
    private String m_type;
    private String m_title;
    private String[] m_authors;
    private Format m_format;
    
    public Book(String type, String isbn, Format format, String title, String[] authors) {
        m_isbn = isbn;
        m_title = title;
        m_type = type;
        m_format = format;
        m_authors = authors;
    }
    
    public String getId() {
        return m_isbn;
    }

    public String getType() {
        return m_type;
    }
    
    public String getTitle() {
        return m_title;
    }

    public String getIsbn() {
        return m_isbn;
    }
    
    public String[] getAuthors() {
        return m_authors;
    }
    
    public Format getFormat() {
        return m_format;
    }

    public void setAuthors(String[] authors) {
        m_authors = authors;
    }

    public void setFormat(Format format) {
        m_format = format;
    }

    public void setId(String id) {
        m_isbn = id;
    }

    public void setTitle(String title) {
        m_title = title;
    }

    public void setType(String type) {
        m_type = type;
    }
}
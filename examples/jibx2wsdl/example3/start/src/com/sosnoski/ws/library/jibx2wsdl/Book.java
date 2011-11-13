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
public class Book extends Item
{
    public enum Format
    {
        HARDCOVER("Hard bound"), TRADE_PAPERBACK("Large format paperback"),
            POCKET_PAPERBACK("Small format paperback");
        
        private final String m_text;
        
        private Format(String text) {
            m_text = text;
        }
        
        public String getText() {
            return m_text;
        }
    }
    
    private String[] m_authors;
    private Format m_format;
    
    public Book(String type, String isbn, Format format, String title, String[] authors) {
        super(isbn, type, title);
        m_format = format;
        m_authors = authors;
    }

    public String getIsbn() {
        return getId();
    }
    
    public String[] getAuthors() {
        return m_authors;
    }
    
    public Format getFormat() {
        return m_format;
    }
}
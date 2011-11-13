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
 * Library server class. This creates an initial library of books, DVDs, and CDs
 * when the class is loaded, then supports method calls to access the library
 * information (including adding new items).
 */
public class LibraryServer1
{
    private static final Type[] m_types = new Type[] {
        newType("fant", "Fantasy"),
        newType("java", "About Java"),
        newType("scifi", "Science fiction"),
        newType("xml", "About XML")
    };
    private static final Map<String,Item> m_itemMap;
    private static final List<Item> m_itemList;
    static {
        m_itemMap = new HashMap<String,Item>();
        m_itemList = new ArrayList<Item>();
        internalAdd(newBook("java", "0136597238", Book.Format.TRADE_PAPERBACK,
            "Thinking in Java", new String[] { "Eckel, Bruce" }));
        internalAdd(newBook("xml", "0130655678", Book.Format.TRADE_PAPERBACK,
            "Definitive XML Schema", new String[] { "Walmsley, Priscilla" }));
        internalAdd(newBook("scifi", "0061020052", Book.Format.POCKET_PAPERBACK,
            "Infinity Beach", new String[] { "McDevitt, Jack" }));
        internalAdd(newBook("scifi", "0812514092", Book.Format.POCKET_PAPERBACK,
             "Aristoi", new String[] { "Williams, Walter Jon" }));
        internalAdd(newBook("java", "0201633612", Book.Format.HARDCOVER,
            "Design Patterns", new String[] { "Gamma, Erich", "Helm, Richard",
            "Johnson, Ralph", "Vlissides, John"}));
        internalAdd(newBook("scifi", "0345253884", Book.Format.POCKET_PAPERBACK,
             "Roadmarks", new String[] { "Zelazny, Roger" }));
        internalAdd(newBook("xml", "0596002521", Book.Format.TRADE_PAPERBACK,
             "XML Schema", new String[] { "van der Vlist, Eric" }));
        internalAdd(newBook("java", "0079132480", Book.Format.TRADE_PAPERBACK,
            "Inside the Java Virtual Machine",
            new String[] { "Venners, Bill" }));
        internalAdd(newDvd("fant", "B00003CWT6",
            "Lord of the Rings: The Fellowship of the Ring", "Jackson, Peter",
            new String[] { "Appleby, Noel", "Astin, Sean", "Baker, Sala",
            "Bean, Sean", "Blanchett, Cate", "Bloom, Orlando", "McKellen, Ian",
            "Mortensen, Viggo", "Tyler, Liv", "Wood, Elijah"}));
        internalAdd(newDvd("fant", "B00005JKZY",
            "Lord of the Rings: The Return of the King", "Jackson, Peter",
            new String[] { "Astin, Sean", "Rhys-Davies, John", "Dourif, Brad",
            "Holm, Ian", "Bloom, Orlando", "McKellen, Ian", "Mortensen, Viggo",
            "Tyler, Liv", "Wood, Elijah" }));
        internalAdd(newDvd("scifi", "B0000640VR",
            "Imposter", "Fleder, Gary",
            new String[] { "Sinise, Gary", "Stowe, Madeleine"}));
        internalAdd(newDvd("scifi", "0767821629",
            "The Thirteenth Floor", "Rusnak, Josef",
            new String[] { "Bierko, Craig", "Mueller-Stahl, Armin"}));
    }
    
    private static Type newType(String name, String desc) {
        return new Type(name, desc);
    }
    
    private static Book newBook(String type, String isbn, Book.Format format,
        String title, String[] authors) {
        return new Book(type, isbn, format, title, authors);
    }
    
    private static Dvd newDvd(String type, String barcode, String title,
        String director, String[] stars) {
        return new Dvd(type, barcode, title, director, stars);
    }

    private static boolean internalAdd(Item item) {
        if (m_itemMap.containsKey(item.getId())) {
            return false;
        } else {
            m_itemMap.put(item.getId(), item);
            m_itemList.add(item);
            for (int i = 0; i < m_types.length; i++) {
                Type type = m_types[i];
                if (item.getType().equals(m_types[i].getName())) {
                    type.setCount(type.getCount()+1);
                    break;
                }
            }
            return true;
        }
    }
    
    public synchronized Item getItem(String isbn) {
        return m_itemMap.get(isbn);
    }
    
    public synchronized List<Item> getItemsByType(String type) {
        ArrayList<Item> matches = new ArrayList<Item>();
        for (int i = 0; i < m_itemList.size(); i++) {
            Item item = m_itemList.get(i);
            if (type.equals(item.getType())) {
                matches.add(item);
            }
        }
        return matches;
    }
    
    public List<Type> getTypes() {
        ArrayList<Type> types = new ArrayList<Type>();
        for (int i = 0; i < m_types.length; i++) {
            types.add(m_types[i]);
        }
        return types;
    }
    
    public synchronized void addItem(Item item) throws AddDuplicateException {
        Item prior = getItem(item.getId());
        if (prior == null) {
            internalAdd(item);
        } else {
            throw new AddDuplicateException(prior);
        }
    }
}
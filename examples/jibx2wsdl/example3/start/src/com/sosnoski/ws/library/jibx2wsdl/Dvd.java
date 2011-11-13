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

public class Dvd extends Item
{
    private String m_director;
    private String[] m_stars;
    
    public Dvd(String type, String barcode, String title, String director,
        String[] stars) {
        super(barcode, type, title);
        m_director = director;
        m_stars = stars;
    }

    public String getBarcode() {
        return getId();
    }
    
    public String getDirector() {
        return m_director;
    }
    
    public String[] getStars() {
        return m_stars;
    }
}
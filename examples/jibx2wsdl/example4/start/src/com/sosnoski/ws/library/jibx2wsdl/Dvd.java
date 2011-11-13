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

public class Dvd implements Item
{
    private String m_barcode;
    private String m_type;
    private String m_title;
    private String m_director;
    private String[] m_stars;
    
    public Dvd(String type, String barcode, String title, String director,
        String[] stars) {
        m_barcode = barcode;
        m_type = type;
        m_title = title;
        m_director = director;
        m_stars = stars;
    }
    
    public String getId() {
        return m_barcode;
    }

    public String getType() {
        return m_type;
    }
    
    public String getTitle() {
        return m_title;
    }
    
    public String getDirector() {
        return m_director;
    }
    
    public String[] getStars() {
        return m_stars;
    }

    public void setId(String id) {
        m_barcode = id;
    }

    public void setDirector(String director) {
        m_director = director;
    }

    public void setStars(String[] stars) {
        m_stars = stars;
    }

    public void setTitle(String title) {
        m_title = title;
    }

    public void setType(String type) {
        m_type = type;
    }
}
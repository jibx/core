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

package com.sosnoski.ws.library.jibx2wsdl.hd;

import com.sosnoski.ws.library.jibx2wsdl.Item;

/**
 * Information for a BluRay DVD.
 */
public class BluRayDvd extends Dvd implements Item
{
    private int m_releaseYear;
    
    public BluRayDvd(String type, String barcode, String title, int year,
        String director, String[] stars) {
        super(type, barcode, title, director, stars);
        m_releaseYear = year;
    }

    public int getReleaseYear() {
        return m_releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        m_releaseYear = releaseYear;
    }
}

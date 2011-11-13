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

/**
 * Information for an HD-DVD.
 */
public class HdDvd extends Dvd
{
    private String m_studio;
    
    public HdDvd(String type, String barcode, String title, String studio,
        String director, String[] stars) {
        super(type, barcode, title, director, stars);
        m_studio = studio;
    }

    public String getStudio() {
        return m_studio;
    }

    public void setStudio(String studio) {
        m_studio = studio;
    }
}

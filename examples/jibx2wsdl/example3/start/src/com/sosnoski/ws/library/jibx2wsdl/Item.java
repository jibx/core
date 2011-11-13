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

public abstract class Item
{
    private String m_id;
    private String m_type;
    private String m_title;
    
    public Item(String id, String type, String title) {
        m_id = id;
        m_title = title;
        m_type = type;
    }
    
    public String getId() {
        return m_id;
    }

    public String getType() {
        return m_type;
    }
    
    public String getTitle() {
        return m_title;
    }
}
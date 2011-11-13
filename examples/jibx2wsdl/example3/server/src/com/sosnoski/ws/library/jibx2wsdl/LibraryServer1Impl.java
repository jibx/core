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

import java.util.List;

/**
 * Implementation class for service. This extends the generated skeleton code,
 * overriding the methods to call the actual service code.
 */
public class LibraryServer1Impl extends LibraryServer1Skeleton
{
    private final LibraryServer1 m_server;
    
    public LibraryServer1Impl() {
        m_server = new LibraryServer1();
    }

    public void addItem(Item item)
    throws AddDuplicateFault {
        try {
            m_server.addItem(item);
        } catch (AddDuplicateException e) {
            AddDuplicateFault fault =
                new AddDuplicateFault(e.getMessage(), e);
            fault.setFaultMessage(e.getData());
            throw fault;
        }
    }

    public Item getItem(String id) {
        return m_server.getItem(id);
    }

    public List getItemsByType(String type) {
        return m_server.getItemsByType(type);
    }

    public List getTypes() {
        return m_server.getTypes();
    }
}
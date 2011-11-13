/*
Copyright (c) 2007, Dennis M. Sosnoski
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JiBX nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.jibx.ws.wsdl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jibx.custom.classes.GlobalCustom;
import org.jibx.ws.wsdl.tools.custom.OperationCustom;
import org.jibx.ws.wsdl.tools.custom.ServiceCustom;
import org.jibx.ws.wsdl.tools.custom.ValueCustom;
import org.jibx.ws.wsdl.tools.custom.WsdlCustom;

/**
 * Test code for class handling.
 */
public class ServiceCustomTest extends CustomizationTestBase
{
    public static final String SIMPLE_SERVICE_CLASS =
        "<custom property-access='false' force-classes='true'>\n" +
        "  <wsdl>\n" +
        "    <service class='org.jibx.ws.wsdl.Service1'/>\n" +
        "  </wsdl>\n" +
        "  <package property-access='true' name='org.jibx.binding'>\n" +
        "    <class name='generator.DataClass1'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    public static final String CUSTOMIZED_SERVICE_CLASS1 =
        "<custom property-access='false' force-classes='true'>\n" +
        "  <wsdl service-base='http://localhost:8080/jibxsoap/'>\n" +
        "    <service class='org.jibx.ws.wsdl.Service1'\n" +
        "        includes='getTypedList setTypedList getList setList'\n" +
        "        service-name='MyService' port-name='MyPort'\n" +
        "        binding-name='MyBinding' port-type-name='MyPortType'\n" +
        "        wsdl-namespace='urn:a' namespace='urn:b'>\n" +
        "      <operation method-name='getList' request-message='glreq'\n" +
        "          request-wrapper='glreqwrap' response-message='glrsp'\n" +
        "          response-wrapper='glrspwrap' soap-action='urn:gl'>\n" +
        "        <return item-type='org.jibx.binding.generator.DataClass1'/>\n" +
        "      </operation>\n" +
        "      <operation method-name='setList'>\n" +
        "        <parameter name='list' element='data-classes'\n" +
        "            item-type='org.jibx.binding.generator.DataClass1'/>\n" +
        "      </operation>\n" +
        "    </service>\n" +
        "  </wsdl>\n" +
        "  <package property-access='true' name='org.jibx.binding'>\n" +
        "    <class name='generator.DataClass1'/>\n" +
        "  </package>\n" +
        "</custom>";
    
    public void testSimpleServiceClass() throws Exception {
        GlobalCustom custom = readCustom(SIMPLE_SERVICE_CLASS);
        List extens = custom.getExtensionChildren();
        assertEquals("extension count", 1, extens.size());
        Object item = extens.get(0);
        assertTrue("extension child type", item instanceof WsdlCustom);
        WsdlCustom wsdl = (WsdlCustom)item;
        List services = wsdl.getServices();
        assertEquals("service count", 1, services.size());
        ServiceCustom service = (ServiceCustom)services.get(0);
        assertEquals("service name", "Service1", service.getServiceName());
        assertEquals("service port", "Service1Port", service.getPortName());
        assertEquals("service binding", "Service1Binding", service.getBindingName());
        assertEquals("service portType", "Service1PortType", service.getPortTypeName());
        assertEquals("service wsdl namespace", "http://jibx.org/ws/wsdl/Service1", service.getWsdlNamespace());
        assertEquals("service schema namespace", "http://jibx.org/ws/wsdl/Service1", service.getNamespace());
        assertEquals("service address", "http://localhost:8080/axis2/services/Service1", service.getServiceAddress());
        List operations = service.getOperations();
        assertEquals("operation count", 7, operations.size());
        Map opmap = new HashMap();
        for (int i = 0; i < operations.size(); i++) {
            OperationCustom op = (OperationCustom)operations.get(i);
            opmap.put(op.getOperationName(), op);
        }
        
        // getDataClass1 method and operation
        OperationCustom op = (OperationCustom)opmap.get("getDataClass1");
        assertNotNull("getDataClass1 operation", op);
        assertEquals("getDataClass1 request message", "getDataClass1Message", op.getRequestMessageName());
        assertEquals("getDataClass1 request wrapper", "getDataClass1", op.getRequestWrapperName());
        assertEquals("getDataClass1 response message", "getDataClass1ResponseMessage", op.getResponseMessageName());
        assertEquals("getDataClass1 response wrapper", "getDataClass1Response", op.getResponseWrapperName());
        List params = op.getParameters();
        assertEquals("getDataClass1 parameters count", 0, params.size());
        ValueCustom ret = op.getReturn();
        assertEquals("getDataClass1 returned type", "org.jibx.binding.generator.DataClass1", ret.getWorkingType());
        
        // setDataClass1 method and operation
        op = (OperationCustom)opmap.get("setDataClass1");
        assertNotNull("setDataClass1 operation", op);
        assertEquals("setDataClass1 request message", "setDataClass1Message", op.getRequestMessageName());
        assertEquals("setDataClass1 request wrapper", "setDataClass1", op.getRequestWrapperName());
        assertEquals("setDataClass1 response message", "setDataClass1ResponseMessage", op.getResponseMessageName());
        assertEquals("setDataClass1 response wrapper", "setDataClass1Response", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("setDataClass1 parameters count", 1, params.size());
        ValueCustom param = (ValueCustom)params.get(0);
        assertEquals("setDataClass1 parameter type", "org.jibx.binding.generator.DataClass1", param.getWorkingType());
        assertEquals("setDataClass1 returned element name", "data", param.getXmlName());
        ret = op.getReturn();
        assertEquals("setDataClass1 returned type", "void", ret.getWorkingType());
        
        // changeDataClass1 method and operation
        op = (OperationCustom)opmap.get("changeDataClass1");
        assertNotNull("changeDataClass1 operation", op);
        assertEquals("changeDataClass1 request message", "changeDataClass1Message", op.getRequestMessageName());
        assertEquals("changeDataClass1 request wrapper", "changeDataClass1", op.getRequestWrapperName());
        assertEquals("changeDataClass1 response message", "changeDataClass1ResponseMessage", op.getResponseMessageName());
        assertEquals("changeDataClass1 response wrapper", "changeDataClass1Response", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("setDataClass1 parameters count", 2, params.size());
        param = (ValueCustom)params.get(0);
        assertEquals("changeDataClass1 parameter type", "int", param.getWorkingType());
        assertEquals("changeDataClass1 returned element name", "index", param.getXmlName());
        param = (ValueCustom)params.get(1);
        assertEquals("changeDataClass1 parameter type", "org.jibx.binding.generator.DataClass1", param.getWorkingType());
        assertEquals("changeDataClass1 returned element name", "data", param.getXmlName());
        ret = op.getReturn();
        assertEquals("changeDataClass1 returned type", "org.jibx.binding.generator.DataClass1", ret.getWorkingType());
        
        // getTypedList method and operation
        op = (OperationCustom)opmap.get("getTypedList");
        assertNotNull("getTypedList operation", op);
        assertEquals("getTypedList request message", "getTypedListMessage", op.getRequestMessageName());
        assertEquals("getTypedList request wrapper", "getTypedList", op.getRequestWrapperName());
        assertEquals("getTypedList response message", "getTypedListResponseMessage", op.getResponseMessageName());
        assertEquals("getTypedList response wrapper", "getTypedListResponse", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("getTypedList parameters count", 0, params.size());
        ret = op.getReturn();
        assertEquals("getTypedList returned type", "java.util.List", ret.getWorkingType());
        assertEquals("getTypedList returned item type", "org.jibx.binding.generator.DataClass1", ret.getItemType());
        assertEquals("getTypedList returned item element name", "dataClass1", ret.getItemName());
        
        // setTypedList method and operation
        op = (OperationCustom)opmap.get("setTypedList");
        assertNotNull("setTypedList operation", op);
        assertEquals("setTypedList request message", "setTypedListMessage", op.getRequestMessageName());
        assertEquals("setTypedList request wrapper", "setTypedList", op.getRequestWrapperName());
        assertEquals("setTypedList response message", "setTypedListResponseMessage", op.getResponseMessageName());
        assertEquals("setTypedList response wrapper", "setTypedListResponse", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("setTypedList parameters count", 1, params.size());
        param = (ValueCustom)params.get(0);
        assertEquals("setTypedList parameter type", "java.util.List", param.getWorkingType());
        assertEquals("setTypedList parameter item type", "org.jibx.binding.generator.DataClass1", param.getItemType());
        assertEquals("setTypedList parameter element name", "list", param.getXmlName());
        assertEquals("setTypedList parameter item element name", "dataClass1", param.getItemName());
        ret = op.getReturn();
        assertEquals("setTypedList returned type", "void", ret.getWorkingType());
        
        // getList method and operation
        op = (OperationCustom)opmap.get("getList");
        assertNotNull("getList operation", op);
        assertEquals("getList request message", "getListMessage", op.getRequestMessageName());
        assertEquals("getList request wrapper", "getList", op.getRequestWrapperName());
        assertEquals("getList response message", "getListResponseMessage", op.getResponseMessageName());
        assertEquals("getList response wrapper", "getListResponse", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("getList parameters count", 0, params.size());
        ret = op.getReturn();
        assertEquals("getList returned type", "java.util.List", ret.getWorkingType());
        assertEquals("getList returned item type", "java.lang.Object", ret.getItemType());
        assertEquals("getList returned item element name", "item", ret.getItemName());
        
        // setList method and operation
        op = (OperationCustom)opmap.get("setList");
        assertNotNull("setList operation", op);
        assertEquals("setList request message", "setListMessage", op.getRequestMessageName());
        assertEquals("setList request wrapper", "setList", op.getRequestWrapperName());
        assertEquals("setList response message", "setListResponseMessage", op.getResponseMessageName());
        assertEquals("setList response wrapper", "setListResponse", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("setList parameters count", 1, params.size());
        param = (ValueCustom)params.get(0);
        assertEquals("setList parameter type", "java.util.List", param.getWorkingType());
        assertEquals("setList parameter item type", "java.lang.Object", param.getItemType());
        assertEquals("setList parameter element name", "list", param.getXmlName());
        assertEquals("setList parameter item element name", "item", param.getItemName());
        ret = op.getReturn();
        assertEquals("setList returned type", "void", ret.getWorkingType());
    }
    
    public void testCustomizedServiceClass1() throws Exception {
        GlobalCustom custom = readCustom(CUSTOMIZED_SERVICE_CLASS1);
        List extens = custom.getExtensionChildren();
        assertEquals("extension count", 1, extens.size());
        Object item = extens.get(0);
        assertTrue("extension child type", item instanceof WsdlCustom);
        WsdlCustom wsdl = (WsdlCustom)item;
        List services = wsdl.getServices();
        assertEquals("service count", 1, services.size());
        ServiceCustom service = (ServiceCustom)services.get(0);
        assertEquals("service name", "MyService", service.getServiceName());
        assertEquals("service port", "MyPort", service.getPortName());
        assertEquals("service binding", "MyBinding", service.getBindingName());
        assertEquals("service portType", "MyPortType", service.getPortTypeName());
        assertEquals("service wsdl namespace", "urn:a", service.getWsdlNamespace());
        assertEquals("service schema namespace", "urn:b", service.getNamespace());
        assertEquals("service address", "http://localhost:8080/jibxsoap/MyService", service.getServiceAddress());
        List operations = service.getOperations();
        assertEquals("operation count", 4, operations.size());
        Map opmap = new HashMap();
        for (int i = 0; i < operations.size(); i++) {
            OperationCustom op = (OperationCustom)operations.get(i);
            opmap.put(op.getOperationName(), op);
        }
        
        // getTypedList method and operation
        OperationCustom op = (OperationCustom)opmap.get("getTypedList");
        assertNotNull("getTypedList operation", op);
        assertEquals("getTypedList request message", "getTypedListMessage", op.getRequestMessageName());
        assertEquals("getTypedList request wrapper", "getTypedList", op.getRequestWrapperName());
        assertEquals("getTypedList response message", "getTypedListResponseMessage", op.getResponseMessageName());
        assertEquals("getTypedList response wrapper", "getTypedListResponse", op.getResponseWrapperName());
        List params = op.getParameters();
        assertEquals("getTypedList parameters count", 0, params.size());
        ValueCustom ret = op.getReturn();
        assertEquals("getTypedList returned type", "java.util.List", ret.getWorkingType());
        assertEquals("getTypedList returned item type", "org.jibx.binding.generator.DataClass1", ret.getItemType());
        
        // setTypedList method and operation
        op = (OperationCustom)opmap.get("setTypedList");
        assertNotNull("setTypedList operation", op);
        assertEquals("setTypedList request message", "setTypedListMessage", op.getRequestMessageName());
        assertEquals("setTypedList request wrapper", "setTypedList", op.getRequestWrapperName());
        assertEquals("setTypedList response message", "setTypedListResponseMessage", op.getResponseMessageName());
        assertEquals("setTypedList response wrapper", "setTypedListResponse", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("setTypedList parameters count", 1, params.size());
        ValueCustom param = (ValueCustom)params.get(0);
        assertEquals("setTypedList parameter type", "java.util.List", param.getWorkingType());
        assertEquals("setTypedList parameter item type", "org.jibx.binding.generator.DataClass1", param.getItemType());
        assertEquals("setTypedList parameter element name", "list", param.getXmlName());
        assertEquals("setTypedList parameter item element name", "dataClass1", param.getItemName());
        ret = op.getReturn();
        assertEquals("setTypedList returned type", "void", ret.getWorkingType());
        
        // getList method and operation
        op = (OperationCustom)opmap.get("getList");
        assertNotNull("getList operation", op);
        assertEquals("getList request message", "glreq", op.getRequestMessageName());
        assertEquals("getList request wrapper", "glreqwrap", op.getRequestWrapperName());
        assertEquals("getList response message", "glrsp", op.getResponseMessageName());
        assertEquals("getList response wrapper", "glrspwrap", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("getList parameters count", 0, params.size());
        ret = op.getReturn();
        assertEquals("getList returned type", "java.util.List", ret.getWorkingType());
        assertEquals("getList returned item type", "org.jibx.binding.generator.DataClass1", ret.getItemType());
        assertEquals("getList returned item element name", "dataClass1", ret.getItemName());
        
        // setList method and operation
        op = (OperationCustom)opmap.get("setList");
        assertNotNull("setList operation", op);
        assertEquals("setList request message", "setListMessage", op.getRequestMessageName());
        assertEquals("setList request wrapper", "setList", op.getRequestWrapperName());
        assertEquals("setList response message", "setListResponseMessage", op.getResponseMessageName());
        assertEquals("setList response wrapper", "setListResponse", op.getResponseWrapperName());
        params = op.getParameters();
        assertEquals("setList parameters count", 1, params.size());
        param = (ValueCustom)params.get(0);
        assertEquals("setList parameter type", "java.util.List", param.getWorkingType());
        assertEquals("setList parameter item type", "org.jibx.binding.generator.DataClass1", param.getItemType());
        assertEquals("setList parameter element name", "data-classes", param.getXmlName());
        assertEquals("setList parameter item element name", "data-class", param.getItemName());
        ret = op.getReturn();
        assertEquals("setList returned type", "void", ret.getWorkingType());
    }
}
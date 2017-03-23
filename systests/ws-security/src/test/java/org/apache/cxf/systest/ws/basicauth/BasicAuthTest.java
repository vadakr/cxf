/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.systest.ws.basicauth;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.systest.ws.common.SecurityTestUtil;
import org.apache.cxf.testutil.common.AbstractBusClientServerTestBase;
import org.example.contract.doubleit.DoubleItPortType;
import org.junit.BeforeClass;

/**
 * A test for Basic Auth using the WS-SecurityPolicy HttpBasicAuthentication policy. 
 * Note the basic auth credentials are not actually authenticated in this test...we are testing 
 * the WS-SecurityPolicy enforcement of whether the credentials are present or not.
 */
public class BasicAuthTest extends AbstractBusClientServerTestBase {
    static final String PORT = allocatePort(Server.class);

    private static final String NAMESPACE = "http://www.example.org/contract/DoubleIt";
    private static final QName SERVICE_QNAME = new QName(NAMESPACE, "DoubleItService");

    @BeforeClass
    public static void startServers() throws Exception {
        assertTrue(
            "Server failed to launch",
            // run the server in the same process
            // set this to false to fork
            launchServer(Server.class, true)
        );
    }

    @org.junit.AfterClass
    public static void cleanup() throws Exception {
        SecurityTestUtil.cleanup();
        stopAllServers();
    }

    @org.junit.Test
    public void testBasicAuth() throws Exception {

        SpringBusFactory bf = new SpringBusFactory();
        URL busFile = BasicAuthTest.class.getResource("client.xml");

        Bus bus = bf.createBus(busFile.toString());
        SpringBusFactory.setDefaultBus(bus);
        SpringBusFactory.setThreadDefaultBus(bus);

        URL wsdl = BasicAuthTest.class.getResource("DoubleItBasicAuth.wsdl");
        Service service = Service.create(wsdl, SERVICE_QNAME);
        QName portQName = new QName(NAMESPACE, "DoubleItBasicAuthPort");
        DoubleItPortType utPort =
                service.getPort(portQName, DoubleItPortType.class);
        updateAddressPort(utPort, PORT);

        utPort.doubleIt(25);

        ((java.io.Closeable)utPort).close();
        bus.shutdown(true);
    }
    
    @org.junit.Test
    public void testNoBasicAuthCredentials() throws Exception {

        SpringBusFactory bf = new SpringBusFactory();
        URL busFile = BasicAuthTest.class.getResource("client.xml");

        Bus bus = bf.createBus(busFile.toString());
        SpringBusFactory.setDefaultBus(bus);
        SpringBusFactory.setThreadDefaultBus(bus);

        URL wsdl = BasicAuthTest.class.getResource("DoubleItBasicAuth.wsdl");
        Service service = Service.create(wsdl, SERVICE_QNAME);
        QName portQName = new QName(NAMESPACE, "DoubleItBasicAuthPort2");
        DoubleItPortType utPort =
                service.getPort(portQName, DoubleItPortType.class);
        updateAddressPort(utPort, PORT);

        try {
            utPort.doubleIt(25);
            fail("Failure expected on no basic auth creds");
        } catch (javax.xml.ws.soap.SOAPFaultException ex) {
            // expected
        }

        ((java.io.Closeable)utPort).close();
        bus.shutdown(true);
    }
    
}
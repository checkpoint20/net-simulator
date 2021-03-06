/*
NET-Simulator -- Network simulator.
Copyright (C) 2006 Maxim Tereshin <maxim-tereshin@yandex.ru>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
            
This program is distributed in the hope that it will be useful, but 
WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.
            
You should have received a copy of the GNU General Public License along 
with this program; if not, write to the Free Software Foundation, 
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/
package org.netsimulator.net;

import org.junit.*;
import org.netsimulator.term.CommandExecutionCompletedListener;
import org.netsimulator.term.IfconfigCLICommand;
import org.netsimulator.util.IdGenerator;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author max
 */
public class IfconfigCLICommandTest {

    private IP4Router router;
    private IfconfigCLICommand command;
    private StringWriter writer;
    private int res = IfconfigCLICommand.ERROR_RETCODE;
    
    public IfconfigCLICommandTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws TooManyInterfacesException {
        router = new IP4Router(new IdGenerator());
        command = new IfconfigCLICommand(router);
        writer = new StringWriter();
        command.addExecutionCompleteListener(new CommandExecutionCompletedListener() {
            public void executionCompleted(int i) {
                res = i;
            }
        });
        command.setOutputWriter(writer);
    }
    
    @After
    public void tearDown() {
    }


    /**
     * => ifconfig
     */
    @Test
    public void testStatusOnNewDevice() throws IOException {
        command.setInvocationContext(new String[]{}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
    }

    /**
     * => ifconfig eth0 -a
     * 
     * @throws IOException 
     */
    @Test
    public void testStatusOfNotActiveInterface() throws IOException {
        command.setInvocationContext(new String[]{"eth0", "-a"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
    }

    /**
     * => ifconfig invalid_ifs_name
     */
    @Test
    public void testNoSuchInterface() throws IOException {
        command.setInvocationContext(new String[]{"invalid_ifs_name"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.NO_SUCH_INTERFACE_RETCODE, res);
    }

    /**
     * => ifconfig eth0 192.168.1.1
     */
    @Test
    public void testSetAddress() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.1"}, null);        
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.DOWN, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.1"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.1.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.255.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.1.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertTrue(router.getRoutingTable().getRows().isEmpty());
    }

    /**
     * => ifconfig eth0 192.168.1.1 -up
     */
    @Test
    public void testSetAddressAndActivate() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.1", "-up"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.1"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.1.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.255.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.1.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        RoutingTableRow route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.1.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.255.0"), route.getNetmask());
        assertEquals(null, route.getGateway());
        assertEquals("eth0", route.getInterface().getName());
    }

    /**
     * => ifconfig eth0 192.168.1.1 -up
     * => ifconfig eth0 -down
     */
    @Test
    public void testInterfaceGoDown() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.1", "-up"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(1, router.getRoutingTable().getRows().size());
        
        command.setInvocationContext(new String[]{"eth0", "-down"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.DOWN, router.getInterface("eth0").getStatus());
        assertEquals(0, router.getRoutingTable().getRows().size());
    }

    /**
     * => ifconfig eth0 192.168.1.1 -netmask 255.255.0.0 -up
     */
    @Test
    public void testSetAddressNetmaskAndActivate() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.1", "-netmask", "255.255.0.0", "-up"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.1"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.255.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.0.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.0.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        RoutingTableRow route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.0.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.0.0"), route.getNetmask());
        assertEquals(null, route.getGateway());
        assertEquals("eth0", route.getInterface().getName());
    }    
    
    
    /**
     * => ifconfig eth0 192.168.1.1 -netmask 255.255.0.0 -up
     * => ifconfig eth0 -netmask 255.255.255.0
     */
    @Test
    public void testChangeNetmask() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.1", "-netmask", "255.255.0.0", "-up"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.1"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.255.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.0.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.0.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        RoutingTableRow route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.0.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.0.0"), route.getNetmask());
        assertEquals(null, route.getGateway());
        assertEquals("eth0", route.getInterface().getName());
        
        command.setInvocationContext(new String[]{"eth0", "-netmask", "255.255.255.0"}, null);
        command.run();
        printCommandOutput();
        
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.1"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.1.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.255.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.1.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.1.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.255.0"), route.getNetmask());
        assertEquals(null, route.getGateway());
        assertEquals("eth0", route.getInterface().getName());
    }    
    
    /**
     * => ifconfig eth0 192.168.1.1 -netmask 255.255.0.0 -up
     */
    @Test
    public void testSetAddressNetmaskBroadcastAndActivate() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.1", "-netmask", "255.255.0.0", "-broadcast", "192.168.1.255", "-up"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.1"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.1.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.0.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.0.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        RoutingTableRow route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.0.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.0.0"), route.getNetmask());
        assertEquals(null, route.getGateway());
        assertEquals("eth0", route.getInterface().getName());
    }    
    
    /**
     * => ifconfig eth0 192.168.1.1 -netmask 255.255.255.22
     */
    @Test
    public void testSetWrongNetmaskOnInactiveInterface() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.1", "-netmask", "255.255.255.22"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.INVALID_NETMASK_RETCODE, res);
        assertEquals(Interface.DOWN, router.getInterface("eth0").getStatus());
        assertEquals(null, ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(null, ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(null, ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(null, ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(0, router.getRoutingTable().getRows().size());
    }    
    

    /**
     * => ifconfig eth0 192.168.1.1 -netmask 255.255.255.0 -up
     * => ifconfig eth0 -netmask 255.255.255.22
     */
    @Test
    public void testSetWrongNetmaskOnActiveInterface() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.1", "-netmask", "255.255.255.0", "-up"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.1"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("255.255.255.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.1.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        RoutingTableRow route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.1.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.255.0"), route.getNetmask());
        assertEquals("eth0", route.getInterface().getName());

        command.setInvocationContext(new String[]{"eth0", "-netmask", "255.255.255.22"}, null);
        command.run();
        printCommandOutput();
        
        assertEquals(IfconfigCLICommand.INVALID_NETMASK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.1"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.1.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.255.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.1.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.1.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.255.0"), route.getNetmask());
        assertEquals(null, route.getGateway());
        assertEquals("eth0", route.getInterface().getName());
    }    
    

    /**
     * => ifconfig eth0 12.0.0.10 -netmask 255.255.255.0 -up
     * => ifconfig eth0 1.2.3.0 -netmask 255.255.255.0
     */
    @Test
    public void testSetAddressEqualsToNetmaskActiveInterface() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "12.0.0.10", "-netmask", "255.255.255.0", "-up"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("12.0.0.10"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("255.255.255.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        RoutingTableRow route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("12.0.0.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.255.0"), route.getNetmask());
        assertEquals("eth0", route.getInterface().getName());

        command.setInvocationContext(new String[]{"eth0", "1.2.3.0", "-netmask", "255.255.255.0"}, null);
        command.run();
        printCommandOutput();
        
        assertEquals(IfconfigCLICommand.ADDRESS_CANNOT_BE_EQUAL_NETWORK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("12.0.0.10"), ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("255.255.255.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("12.0.0.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("12.0.0.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.255.0"), route.getNetmask());
        assertEquals(null, route.getGateway());
        assertEquals("eth0", route.getInterface().getName());
    }    
    
    
    /**
     * => ifconfig eth0 192.168.1.21 -netmask 255.255.255.0 -up
     * => ifconfig eth0 -netmask 255.255.0.0
     */
    @Test
    public void testBroadcastRecalculationOnAddressUpdate() throws IOException, AddressException {
        command.setInvocationContext(new String[]{"eth0", "192.168.1.21", "-netmask", "255.255.255.0", "-up"}, null);
        command.run();
        printCommandOutput();
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.21"),  ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.1.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.255.0"), ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.1.0"),   ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        RoutingTableRow route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.1.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.255.0"), route.getNetmask());
        assertEquals("eth0", route.getInterface().getName());

        command.setInvocationContext(new String[]{"eth0", "-netmask", "255.255.0.0"}, null);
        command.run();
        printCommandOutput();
        
        assertEquals(IfconfigCLICommand.OK_RETCODE, res);
        assertEquals(Interface.UP, router.getInterface("eth0").getStatus());
        assertEquals(new IP4Address("192.168.1.21"),    ((IP4EnabledInterface)router.getInterface("eth0")).getInetAddress());
        assertEquals(new IP4Address("192.168.255.255"), ((IP4EnabledInterface)router.getInterface("eth0")).getBroadcastAddress());
        assertEquals(new IP4Address("255.255.0.0"),     ((IP4EnabledInterface)router.getInterface("eth0")).getNetmaskAddress());
        assertEquals(new IP4Address("192.168.0.0"),     ((IP4EnabledInterface)router.getInterface("eth0")).getNetworkAddress());
        assertEquals(1, router.getRoutingTable().getRows().size());
       
        route = router.getRoutingTable().getRows().iterator().next();
        assertEquals(new IP4Address("192.168.0.0"), route.getTarget());
        assertEquals(new IP4Address("255.255.0.0"), route.getNetmask());
        assertEquals(null, route.getGateway());
        assertEquals("eth0", route.getInterface().getName());
    }    
    

    
    private void printCommandOutput() {
        writer.flush();
        System.out.println("=> " + writer.toString());
    }
}

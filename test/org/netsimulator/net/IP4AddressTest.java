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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author max
 */
public class IP4AddressTest {
    
    public IP4AddressTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of isNetmaskAddressValid method, of class IP4Address.
     */
    @Test
    public void testIsNetmaskAddressValid() {
        System.out.println("isNetmaskAddressValid");
        
        IP4Address netmaskAddressValid1 = null;
        IP4Address netmaskAddressValid2 = null;
        IP4Address netmaskAddressValid3 = null;
        IP4Address netmaskAddressValid4 = null;
        IP4Address netmaskAddressValid5 = null;
        IP4Address netmaskAddressValid6 = null;
        IP4Address netmaskAddressValid7 = null;
        IP4Address netmaskAddressValid8 = null;

        IP4Address netmaskAddressInvalid1 = null;
        IP4Address netmaskAddressInvalid2 = null;
        
        try {
            netmaskAddressValid1 = new IP4Address("255.255.255.0");
            netmaskAddressValid2 = new IP4Address("255.255.254.0");
            netmaskAddressValid3 = new IP4Address("255.255.252.0");
            netmaskAddressValid4 = new IP4Address("255.255.248.0");
            netmaskAddressValid5 = new IP4Address("255.255.240.0");
            netmaskAddressValid6 = new IP4Address("255.255.224.0");
            netmaskAddressValid7 = new IP4Address("255.255.192.0");
            netmaskAddressValid8 = new IP4Address("255.255.128.0");

            netmaskAddressInvalid1 = new IP4Address("255.255.123.0");
            netmaskAddressInvalid2 = new IP4Address("255.255.56.0");
        } catch (AddressException ex) {
            ex.printStackTrace();
        }
        
        assertTrue(IP4Address.isNetmaskAddressValid(netmaskAddressValid1));
        assertTrue(IP4Address.isNetmaskAddressValid(netmaskAddressValid2));
        assertTrue(IP4Address.isNetmaskAddressValid(netmaskAddressValid3));
        assertTrue(IP4Address.isNetmaskAddressValid(netmaskAddressValid4));
        assertTrue(IP4Address.isNetmaskAddressValid(netmaskAddressValid5));
        assertTrue(IP4Address.isNetmaskAddressValid(netmaskAddressValid6));
        assertTrue(IP4Address.isNetmaskAddressValid(netmaskAddressValid7));
        assertTrue(IP4Address.isNetmaskAddressValid(netmaskAddressValid8));
        
        assertFalse(IP4Address.isNetmaskAddressValid(netmaskAddressInvalid1));
        assertFalse(IP4Address.isNetmaskAddressValid(netmaskAddressInvalid2));
    }
}

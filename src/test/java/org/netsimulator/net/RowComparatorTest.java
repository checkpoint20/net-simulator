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

/*
 * RowComparatorTest.java
 * JUnit based test
 *
 * Created on 31 Декабрь 2006 г., 19:33
 */

package org.netsimulator.net;

import junit.framework.TestCase;
import org.netsimulator.util.IdGenerator;

/**
 *
 * @author maks
 */
public class RowComparatorTest extends TestCase
{
    
    public RowComparatorTest(String testName)
    {
        super(testName);
    }

    /**
     * Test of compare method, of class org.netsimulator.net.RowComparator.
     */
    public void testCompare() throws AddressException, NotAllowedAddressException
    {
        System.out.println("compare");
        int result;
        EthernetInterface eth0 = 
                new EthernetInterface( new IdGenerator(1), new MACAddress("00:00:00:00:00:01"), "eth0" );
        RoutingTableRow o1=null, o2=null, o3=null, o4=null;
        RoutingTableRow.RowComparator instance = new RoutingTableRow.RowComparator();
        
        o1 = new RoutingTableRow(
                new IP4Address("192.168.120.0"),
                new IP4Address("255.255.255.0"),
                null,
                1,
                eth0 ) ;
        o2 = new RoutingTableRow(
                new IP4Address("192.168.120.0"),
                new IP4Address("255.255.255.0"),
                null,
                1,
                eth0 ) ;
        o3 = new RoutingTableRow(
                new IP4Address("192.168.120.0"),
                new IP4Address("255.255.255.255"),
                null,
                1,
                eth0 ) ;
        o4 = new RoutingTableRow(
                new IP4Address("192.168.120.0"),
                new IP4Address("255.255.255.0"),
                null,
                5,
                eth0 ) ;

        System.out.println("Initial values:");
        System.out.println("o1: "+o1);
        System.out.println("o2: "+o2);
        System.out.println("o3: "+o3);
        System.out.println("o4: "+o4);
        System.out.println("");
                
                
        System.out.print("compare(o1, o2) ... ");
        result = instance.compare(o1, o2);
        assertEquals( RoutingTableRow.RowComparator.EQ, result );
        System.out.println(result);
        
        System.out.print("compare(o1, o3) ... ");
        result = instance.compare(o1, o3);
        assertEquals( RoutingTableRow.RowComparator.GT, result );
        System.out.println(result); 

        System.out.print("compare(o3, o1) ... ");
        result = instance.compare(o3, o1);
        assertEquals( RoutingTableRow.RowComparator.LT, result );
        System.out.println(result);

        System.out.print("compare(o1, o4) ... ");
        result = instance.compare(o1, o4);
        assertEquals( RoutingTableRow.RowComparator.LT, result );
        System.out.println(result);

        System.out.print("compare(o4, o1) ... ");
        result = instance.compare(o4, o1);
        assertEquals( RoutingTableRow.RowComparator.GT, result );
        System.out.println(result);

    }
    
}

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
 * RoutingTableTest.java
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
public class RoutingTableTest extends TestCase
{

   
    public void testRoute() throws AddressException, NotAllowedAddressException 
    {
        //////// готовим тестовые данные  ////////
        
        // тестируем эту таблицу
        RoutingTable table = new RoutingTable();
        
        EthernetInterface eth0 = 
                new EthernetInterface( new IdGenerator(1), new MACAddress("00:00:00:00:00:01"), "eth0" );

/* Normal address */
        RoutingTableRow r1 = new RoutingTableRow(
                new IP4Address("192.168.120.0"),
                new IP4Address("255.255.255.0"),
                null,
                1,
                eth0 ) ;
                
/* Duplicate row - should not be included */
        RoutingTableRow r2 = new RoutingTableRow(
                new IP4Address("192.168.120.0"),
                new IP4Address("255.255.255.0"),
                null,
                1,
                eth0 ) ;

/* Strange host route - IMHO should be included and processed */                
        RoutingTableRow r3 = new RoutingTableRow( 
                new IP4Address("192.168.120.0"),
                new IP4Address("255.255.255.255"),
                null,
                1,
                eth0 ) ;

/* Another metrics */                
        RoutingTableRow r4 = new RoutingTableRow(
                new IP4Address("192.168.120.0"),
                new IP4Address("255.255.255.0"),
                null,
                5,
                eth0 ) ;

/* Check overlaped networks. This net contains following one */                
        RoutingTableRow r5 = new RoutingTableRow(
                new IP4Address("192.168.120.160"),
                new IP4Address("255.255.255.224"),
                null,
                1,
                eth0 ) ;
                
/* Prev network contains this one */                
        RoutingTableRow r6 = new RoutingTableRow(
                new IP4Address("192.168.120.176"),
                new IP4Address("255.255.255.248"),
                null,
                3,
                eth0 ) ;
                
/* We will check legal for this subnet zero-ended host address 2.3.5.0 */                
        RoutingTableRow r7 = new RoutingTableRow(
                new IP4Address("2.3.4.0"),
                new IP4Address("255.255.254.0"),
                null,
                2,
                eth0 ) ;
                
/* Biggest possible subnet ? */
        RoutingTableRow r8 = new RoutingTableRow(
                new IP4Address("128.0.0.0"),
                new IP4Address("128.0.0.0"),
                null,
                7,
                eth0 ) ;

/* Another big subnet */
        RoutingTableRow r9 = new RoutingTableRow(
                new IP4Address("64.0.0.0"),
                new IP4Address("192.0.0.0"),
                null,
                1,
                eth0 ) ;
                
/* Longest prefix match check */
        RoutingTableRow r10 = new RoutingTableRow(
                new IP4Address("64.0.0.0"),
                new IP4Address("255.255.255.252"),
                null,
                4,
                eth0 ) ;
                
/* Default route */                
        RoutingTableRow rDefault = new RoutingTableRow(
                new IP4Address("0.0.0.0"),
                new IP4Address("0.0.0.0"),
                null,
                1,
                eth0 ) ;

        
        table.addRoute( r1 );
        table.addRoute( r2 );
        table.addRoute( r3 );
        table.addRoute( r4 );
        table.addRoute( r5 );
        table.addRoute( r6 );
        table.addRoute( r7 );
        table.addRoute( r8 );
        table.addRoute( r9 );
        table.addRoute( r10 );
        table.addRoute( rDefault );
        
        // показываем таблицу так (в таком порядке) 
        // как она будет обрабатываться в процессе маршрутизации
        System.out.println( "--- ROUTING TABLE (in order it will be processed) ---" );
        for( RoutingTableRow row : table.getRows() ) 
        {
            System.out.println( row );
        }
        System.out.println( "-----------------------------------------------------" );

        
        //////// тестируем /////////
        IP4Address dst; 
        RoutingTableRow res;

/* Just check strange host */
        dst= new IP4Address("192.168.120.0");
        res = table.route( dst );
        assertEquals( r3, res );

/* Play with Metrics */
        dst= new IP4Address("192.168.120.1");
        res = table.route( dst );
        assertEquals( r1, res );

/* Play with overlaped networks 1 */
        dst= new IP4Address("192.168.120.161");
        res = table.route( dst );
        assertEquals( r5, res );

/* Play with overlaped networks 2 */
        dst= new IP4Address("192.168.120.177");
        res = table.route( dst );
        assertEquals( r6, res );

/* Play with overlaped networks 3 */
        dst= new IP4Address("192.168.120.186");
        res = table.route( dst );
        assertEquals( r5, res );

/* Play with zero address */
        dst= new IP4Address("2.3.5.0");
        res = table.route( dst );
        assertEquals( r7, res );

/* Play with big networks 1 */
        dst= new IP4Address("128.0.0.1");
        res = table.route( dst );
        assertEquals( r8, res );

/* Play with big networks 2 */
        dst= new IP4Address("128.255.255.255");
        res = table.route( dst );
        assertEquals( r8, res );

/* Play with big networks 3 */
        dst= new IP4Address("100.100.100.100");
        res = table.route( dst );
        assertEquals( r9, res );

/* Longest prefix match check */
        dst= new IP4Address("64.0.0.2");
        res = table.route( dst );
        assertEquals( r10, res );

/* Default gateway */
        dst = new IP4Address("10.167.12.1");
        res = table.route( dst );
        assertEquals( rDefault, res );

    }

    
}

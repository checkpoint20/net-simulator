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
 * RoutingTable.java
 *
 * Created on 6 August 2005, 23:24
 */
package org.netsimulator.net;

import java.util.*;
import java.util.logging.*;

public class RoutingTable {

    private SortedSet<RoutingTableRow> table = null;
    private static final Logger logger = Logger.getLogger( "org.netsimulator.net.RoutingTable" );

    /** Creates a new instance of RoutingTable */
    public RoutingTable() {
        table = Collections.synchronizedSortedSet(
                new TreeSet<RoutingTableRow>( new RoutingTableRow.RowComparator() ) );
    }

    public void addRoute(
            IP4Address target,
            IP4Address netmask,
            IP4Address gateway,
            int metric,
            Interface iface ) throws NotAllowedAddressException {
        RoutingTableRow row = new RoutingTableRow(
                target,
                netmask,
                gateway,
                metric,
                iface );

        addRoute( row );
    }

    public void addRoute( RoutingTableRow row ) {
        if( table.add( row ) ) {
            logger.fine( hashCode() + ": the row:\n" + row + "\n was added to the routing table" );
        } else {
            logger.fine( hashCode() + ": the row:\n" + row + "\n was not added to the routing table" );
        }
    }

    public int deleteRoute(
            IP4Address target,
            IP4Address netmask,
            IP4Address gateway,
            int metric,
            Interface iface ) {
        int deleted = 0;

        synchronized( table ) {
            for( Iterator<RoutingTableRow> i = table.iterator(); i.hasNext();) {
                RoutingTableRow row = i.next();

                if( row.getTarget().equals( target ) &&
                        row.getNetmask().equals( netmask ) &&
                        row.getInterface().getName().equals( iface.getName() ) &&
                        row.getMetric() == metric &&
                        ( ( row.getGateway() != null && row.getGateway().equals( gateway ) ) ||
                        ( row.getGateway() == null && gateway == null ) ) ) {
                    i.remove();
                    deleted++;
                }
            }
        }
        return deleted;
    }

    public int deleteRoute(
            IP4Address target,
            IP4Address netmask,
            IP4Address gateway,
            Interface iface ) {
        int deleted = 0;

        synchronized( table ) {
            for( Iterator<RoutingTableRow> i = table.iterator(); i.hasNext();) {
                RoutingTableRow row = i.next();

                if( row.getTarget().equals( target ) &&
                        row.getNetmask().equals( netmask ) &&
                        row.getInterface().getName().equals( iface.getName() ) &&
                        ( ( row.getGateway() != null && row.getGateway().equals( gateway ) ) ||
                        ( row.getGateway() == null && gateway == null ) ) ) {
                    i.remove();
                    deleted++;
                }
            }
        }
        return deleted;
    }

    public RoutingTableRow route( IP4Address destination ) {
        RoutingTableRow res = null;

        synchronized( table ) {
            for( Iterator<RoutingTableRow> i = table.iterator(); i.hasNext();) {
                RoutingTableRow r = i.next();

                logger.fine( "Processing row: " + r );

                if( r.match( destination ) ) {
                    logger.fine( "the destination " + destination + " matchs the row" );
                    res = r;
                    break;
                } else {
                    logger.fine( "the destination " + destination + " doesn't match the row" );
                }
            }
        }
        return res;
    }

    /**
     * This method is used only in 'route' command. It is not used for
     * supporting routing process in routers.
     * @param destination.
     * @return found route.
     * @see RouteCLICommand
     */
    public RoutingTableRow routeThroghoutStraightConnectedNetworks( IP4Address destination ) {
        RoutingTableRow res = null;

        synchronized( table ) {
            for( Iterator<RoutingTableRow> i = table.iterator(); i.hasNext();) {
                RoutingTableRow r = i.next();

                if( r.getGateway() != null ) {
                    continue;
                }

                if( r.match( destination ) ) {
                    res = r;
                    break;
                }
            }
        }
        return res;
    }

    /**
     * It is imperative that the user manually synchronize on the returned sorted set 
     * when iterating over it or any of its subSet, headSet, or tailSet views.
     */
    public SortedSet<RoutingTableRow> getRows() {
        return table;
    }
}







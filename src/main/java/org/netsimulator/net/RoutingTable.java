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
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoutingTable {

    private static final Logger logger = Logger.getLogger( RoutingTable.class.getName() );
    private List<RoutingTableRow> table = Collections.unmodifiableList(new LinkedList<>());

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

    // Visible for testing
    void addRoute( RoutingTableRow row ) {
        Set<RoutingTableRow> newTable = new HashSet<>(table);
        if(newTable.add(row)) {
            logger.log( Level.FINEST, "{0}: the row:\n{1}\n was added to the routing table", new Object[]{hashCode() + "", row});
            List<RoutingTableRow> l = new LinkedList<>(newTable);
            l.sort(RoutingTableRow.COMPARATOR);
            this.table = Collections.unmodifiableList(l);
        }
    }

    public int deleteRoute(
            IP4Address target,
            IP4Address netmask,
            IP4Address gateway,
            int metric,
            Interface iface ) {
        int deleted = 0;
        List<RoutingTableRow> newTable = new LinkedList<>(table);
        for( Iterator<RoutingTableRow> i = newTable.iterator(); i.hasNext();) {
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
        this.table = Collections.unmodifiableList(newTable);
        return deleted;
    }

    public int deleteRoute(
            IP4Address target,
            IP4Address netmask,
            IP4Address gateway,
            Interface iface ) {
        int deleted = 0;
        List<RoutingTableRow> newTable = new LinkedList<>(table);
        for(Iterator<RoutingTableRow> i = newTable.iterator(); i.hasNext();) {
            RoutingTableRow row = i.next();
            if(row.getTarget().equals(target) &&
                    row.getNetmask().equals( netmask ) &&
                    row.getInterface().getName().equals( iface.getName() ) &&
                    ( ( row.getGateway() != null && row.getGateway().equals( gateway ) ) ||
                    ( row.getGateway() == null && gateway == null ) ) ) {
                i.remove();
                deleted++;
            }
        }
        this.table = Collections.unmodifiableList(newTable);
        return deleted;
    }

    RoutingTableRow route(IP4Address destination) {
        RoutingTableRow res = null;
        for (RoutingTableRow r : table) {
            logger.log( Level.FINE, "Processing row: {0}", r);

            if( r.match( destination ) ) {
                logger.log( Level.FINE, "the destination {0} matchs the row", destination);
                res = r;
                break;
            } else {
                logger.log( Level.FINE, "the destination {0} doesn''t match the row", destination);
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
    public RoutingTableRow routeThroughoutStraightConnectedNetworks(IP4Address destination ) {
        RoutingTableRow res = null;
        for (RoutingTableRow r : table) {
            if( r.getGateway() != null ) {
                continue;
            }

            if( r.match( destination ) ) {
                res = r;
                break;
            }
        }
        return res;
    }

    public List<RoutingTableRow> getRows() {
        return table;
    }

    @Override
    public String toString() {
        StringBuilder curTable = new StringBuilder("Routing table:\n");
        for (RoutingTableRow routingTableRow : table) {
            curTable.append(routingTableRow.toString()).append("\n");
        }
        return curTable.toString();
    }
}







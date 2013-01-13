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

import java.util.logging.*;
import org.netsimulator.util.IdGenerator;

public class NullInterface
        implements IP4EnabledInterface {

    private static Logger logger =
            Logger.getLogger( "org.netsimulator.net.NullInterface" );
    private int id;
    private IdGenerator idGenerator;
    private Router router;
    private IP4Address inetAddress;
    private IP4Address broadcastAddress;
    private IP4Address netmaskAddress;
    private int status;
    private int bandwidth;
    private String encap;
    private String name;

    
    public NullInterface(
            IdGenerator idGenerator,
            String name ) {
        this(
                idGenerator,
                idGenerator.getNextId(),
                name );
    }

    public NullInterface(
            IdGenerator idGenerator,
            int id,
            String name ) {
        this.idGenerator = idGenerator;
        this.id = id;
        this.name = name;
        encap = "Null interface";
        status = Interface.UP;
        inetAddress = new NullIP4Address();
        broadcastAddress = new NullIP4Address();
        netmaskAddress = new NullIP4Address();
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setRouter( Router router ) {
        this.router = router;
    }

    public void setStatus( int status ) throws ChangeInterfacePropertyException {
        // always must be UP
    }

    public int getStatus() {
        return status;
    }

    public void setBandwidth( int bandwidth ) {
        this.bandwidth = bandwidth;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public IP4Address getInetAddress() {
        return inetAddress;
    }

    public IP4Address getBroadcastAddress() {
        return broadcastAddress;
    }

    public IP4Address getNetmaskAddress() {
        return netmaskAddress;
    }

    public void setBroadcastAddress( IP4Address address )
            throws ChangeInterfacePropertyException {
    }

    public void setNetmaskAddress( IP4Address address )
            throws ChangeInterfacePropertyException {
    }

    public void setInetAddress( IP4Address address )
            throws ChangeInterfacePropertyException {
    }

    public String getEncap() {
        return encap;
    }


    public IP4Address getNetworkAddress() {
        return new NullIP4Address();
    }

    public void transmitPacket( IP4Packet packet, IP4Address destination ) {
        
        //////////////////////////////
        //// Do nothing by design ////
        //////////////////////////////
        
        logger.fine("The packet is dropped: '"+packet+"'.");
    }

    public int getId() {
        return id;
    }

}

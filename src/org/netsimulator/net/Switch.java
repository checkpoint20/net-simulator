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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.netsimulator.util.ConfigurableThreadFactory;
import org.netsimulator.util.IdGenerator;

public class Switch implements Concentrator
{
    public static final int MACADDRESS_TABLE_CLEAN_TIMEOUT = 10; // sec
    private static final Logger logger = Logger.getLogger("org.netsimulator.net.Switch");
        
    // 10 threads for all the NetSimulator. 
    // Probaly it makes sense to have 1 thread per switch?
    private static final ScheduledExecutorService macTableCleanExecutorService = 
                                  Executors.newScheduledThreadPool(10, new ConfigurableThreadFactory("MacTableCleanup-"));

    private final int id;
    private final IdGenerator idGenerator;
    private final ArrayList<Port> ports;
    private final MACAddressesTable macTable;


    /** Create new switch with 16 ports.
     * @param idGenerator
    */
    public Switch(IdGenerator idGenerator) {
        this(idGenerator, 16);
    }


    
    /** Create new switch with n ports.
     * @param idGenerator
     * @param n
    */
    public Switch(IdGenerator idGenerator, int n)
    {
        this(idGenerator, n, idGenerator.getNextId());
    }
    
    
    
    /** Create new switch with n ports and id.
     * @param idGenerator
    */
    public Switch(IdGenerator idGenerator, int n, int id)
    {
        this.idGenerator = idGenerator;
        this.id = id;
        ports = new ArrayList<Port>();
        for(int i=0; i!=n; i++)
        {
            ports.add(new Port(idGenerator, this));
        }
        macTable = new MACAddressesTable(MACADDRESS_TABLE_CLEAN_TIMEOUT);
        macTableCleanExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                macTable.clean();
            }
        }, MACADDRESS_TABLE_CLEAN_TIMEOUT, MACADDRESS_TABLE_CLEAN_TIMEOUT, TimeUnit.SECONDS);
        
    }


    
    
    public Port getPortById(int id)
    {
        Port port = null;
        for(Iterator<Port> i = ports.iterator(); i.hasNext(); )
        {
            port = i.next();
            if(port.getId() == id)
            {
                return port;
            }
        }
        return null;        
    }
    
    
    
    
    public NetworkDevice getNetworkDeviceById(int id)
    {
         return getPortById(id);
    }
            
    
    
    
    public int getPortsCount()
    {
        return ports.size();
    }



    public Port getPort(int port)
    {
        return ports.get(port);
    }

    
    public void addPort(Port port)
    {
        ports.add(port);
        port.setConcentrator(this);
    }
    
    
    public Port[] getPorts()
    {
        Port[] array = new Port[ports.size()];
	return ports.toArray(array);        
    }
    
    
    
    public MACAddressesTable getMACAddressesTable()
    {
        return macTable;
    }
    
    
    
    

    public void transportPacket(final Port sourcePort, final Layer2Packet packet)
    {
        MACAddress dstAddress = (MACAddress)packet.getDestinationAddress();
        MACAddress srcAddress = (MACAddress)packet.getSourceAddress();
        int dstPortId = -1;
        int srcPortId = ports.indexOf( sourcePort );
        
        macTable.put( srcAddress, srcPortId );
        if( dstAddress.isBroadcast() )
        {
            sendToAllPorts( srcPortId, packet );
        }else
        {
            dstPortId = macTable.get( dstAddress );
        
            if( dstPortId == srcPortId )
            {
                logger.severe(hashCode()+" Oops! dstPortId: "+
                        dstPortId+" srcPortId: "+srcPortId+" packet: "+packet);
                return;
            }
            
            if( dstPortId >= 0 )
            {
                ports.get( dstPortId ).transmitPacket( packet );
            }else
            {
                sendToAllPorts( srcPortId, packet );
            }
        }
    }

    private void sendToAllPorts(int srcPortId, final Layer2Packet packet)
    {
        Port portsArray[] = new Port[ ports.size() ];
        ports.toArray( portsArray );
        for( int i = 0; i != portsArray.length; i++ )
        {
            if( i != srcPortId )
            {
                portsArray[i].transmitPacket( packet );
            }
        }
    }
    
    
    
    public int getId()
    {
        return id;
    }

    
    public IdGenerator getIdGenerator()
    {
        return idGenerator;
    }
    
}

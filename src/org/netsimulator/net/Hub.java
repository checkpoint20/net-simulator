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
import org.netsimulator.util.IdGenerator;

public class Hub implements Concentrator
{
    private int id;
    private IdGenerator idGenerator;
    private ArrayList<Port> ports;


    /** Create new hub with 16 ports.
    */
    public Hub(IdGenerator idGenerator)
    {
        this(idGenerator, 16);
    }


    /** Create new hub with n ports.
    */
    public Hub(IdGenerator idGenerator, int n)
    {
        this(idGenerator, n, idGenerator.getNextId());
    }

    

    /** Create new hub with n ports and id.
    */
    public Hub(IdGenerator idGenerator, int n, int id)
    {
        this.idGenerator = idGenerator;
        this.id = id;
        ports = new ArrayList<Port>();
        for(int i=0; i!=n; i++)
        {
            ports.add(new Port(idGenerator, this));
        }
    }
    
    
    
    public int getPortsCount()
    {
        return ports.size();
    }



    public Port getPort(int port)
    {
        return ports.get(port);
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
    

    public void transportPacket(Port sourcePort, Layer2Packet packet)
    {
        for(Iterator<Port> i=ports.iterator(); i.hasNext();)
        {
            Port port = i.next();
            if(port != sourcePort)
            {
                port.transmitPacket(packet);
            }
        }
    }
    
    
    public int getId()
    {
        return id;
    }
    

    public void setId(int id)
    {
        this.id = id;
    }
    
    
    public IdGenerator getIdGenerator()
    {
        return idGenerator;
    }
    
    
}

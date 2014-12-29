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
import java.util.logging.*;
import org.netsimulator.util.IdGenerator;

public class Media
{
    private int id;
    private IdGenerator idGenerator;
    
    private ArrayList<NetworkDevice> devs;
    private int n;
    private ArrayList<TransferPacketListener> listenerTrPacket;
    private ArrayList<PhisicalLinkSetUpListener> listenerPhLink;

    private static final Logger logger = 
            Logger.getLogger("org.netsimulator.net.Media");

    
    public Media(IdGenerator idGenerator)
    {
        this(idGenerator, 2, idGenerator.getNextId());
    }


    public Media(IdGenerator idGenerator, int n)
    {
        this(idGenerator, n, idGenerator.getNextId());
    }
    

    public Media(IdGenerator idGenerator, int n, int id)
    {
        this.idGenerator = idGenerator;
        this.id = id;
        devs = new ArrayList<NetworkDevice>();
        listenerTrPacket = new ArrayList<TransferPacketListener>();
        listenerPhLink = new ArrayList<PhisicalLinkSetUpListener>();
        this.n = n;
    }



    public int getPointsCount()
    {
        return n;
    }
    
    
    public void connectToDevice(NetworkDevice device)
        throws TooManyConnectionsException
    {
        if(devs.size() >= n)
            throw new TooManyConnectionsException();

        device.connectMedia(this);
        devs.add(device);

        if(devs.size() > 1)
        {
            for(Iterator<PhisicalLinkSetUpListener> i = listenerPhLink.iterator(); i.hasNext(); )
            {
                i.next().phisicalLinkSetUp();
            }
        }
        logger.fine(hashCode()+": the media was connected to the device: "+device);
    }


    
    public void disconnectFromDevice(NetworkDevice device)
    {
        device.disconnectMedia();
        devs.remove(device);

        if(devs.size() == 1)
        {
            for(Iterator<PhisicalLinkSetUpListener> i = listenerPhLink.iterator(); i.hasNext(); )
            {
                i.next().phisicalLinkBrokenDown();
            }
        }
        logger.fine(hashCode()+": the media was disconnected from the device: "+device);
    }
    
    

    public void transmitPacket(NetworkDevice src_dev, Layer2Packet packet)
    {
        // TODO 
//        Exception in thread "Thread-5" java.util.ConcurrentModificationException
//	at java.util.ArrayList$Itr.checkForComodification(ArrayList.java:819)
//	at java.util.ArrayList$Itr.next(ArrayList.java:791)
//	at org.netsimulator.net.Media.transmitPacket(Media.java:118)
        
        
        NetworkDevice dev;

        for(Iterator<NetworkDevice> i=devs.iterator(); i.hasNext(); )
        {
            dev = i.next();
            if(dev != src_dev)
            {
                dev.recivePacket(packet);
            }
        }
        
        for(Iterator<TransferPacketListener> i = listenerTrPacket.iterator(); i.hasNext(); )
        {
            i.next().packetTransfered(packet);
        }
    }
    
    
    public void addTrnasmitPacketListener(TransferPacketListener listener)
    {
        listenerTrPacket.add(listener);
        logger.fine(hashCode()+": TransmitPacketListener added");
    }
    

    public void removeTrnasmitPacketListener(TransferPacketListener listener)
    {
        listenerTrPacket.remove(listener);
        logger.fine(hashCode()+": TransmitPacketListener removed");
    }
    
    
    
    public void addPhisicalLinkSetUpListener(PhisicalLinkSetUpListener listener)
    {
        listenerPhLink.add(listener);
        logger.fine(hashCode()+": PhisicalLinkSetUpListener added");
    }

    public void removePhisicalLinkSetUpListener(PhisicalLinkSetUpListener listener)
    {
        listenerPhLink.remove(listener);
        logger.fine(hashCode()+": PhisicalLinkSetUpListener removed");
    }

    
    public Iterator<NetworkDevice> getConnectedDevices()
    {
        return devs.iterator();
    }


    public int getCountConnectedDevices()
    {
        return devs.size();
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
    
    
    
    
    public void disconnectAll()
    {
        for(Iterator<NetworkDevice> i=devs.iterator(); i.hasNext(); )
        {
            NetworkDevice dev = i.next();
            dev.disconnectMedia();
            i.remove();
            logger.fine(hashCode()+": the media was disconnected from the device: "+dev);
        }
    }
}

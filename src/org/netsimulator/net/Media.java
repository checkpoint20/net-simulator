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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class Media {
    private static final int MAX_CONNECTED_DEVICES = 2;
    private static final Logger logger = Logger.getLogger("org.netsimulator.net.Media");

    private final int id;
    private final List<NetworkDevice> devs;
    private final List<TransferPacketListener> listenerTrPacket;
    private final List<PhysicalLinkSetUpListener> listenerPhLink;

    public Media(int id) {
        this.id = id;
        devs = new CopyOnWriteArrayList<>();
        listenerTrPacket = new CopyOnWriteArrayList<>();
        listenerPhLink = new CopyOnWriteArrayList<>();
    }

    public int getPointsCount() {
        return MAX_CONNECTED_DEVICES;
    }

    /**
     * It's not thread safe. That's OK provided that it's invoked from UI only.
     * @param device a device to connect.
     * @throws TooManyConnectionsException
     */
    public void connectToDevice(NetworkDevice device)
        throws TooManyConnectionsException {

        if(devs.size() >= MAX_CONNECTED_DEVICES)
            throw new TooManyConnectionsException();

        device.connectMedia(this);
        devs.add(device);

        if(devs.size() > 1) {
            listenerPhLink.forEach(PhysicalLinkSetUpListener::phisicalLinkSetUp);
        }
        logger.fine(hashCode()+": the media was connected to the device: "+device);
    }

    public void disconnectFromDevice(NetworkDevice device) {
        device.disconnectMedia();
        devs.remove(device);

        if(devs.size() == 1) {
            listenerPhLink.forEach(PhysicalLinkSetUpListener::phisicalLinkBrokenDown);
        }
        logger.fine(hashCode()+": the media was disconnected from the device: " + device);
    }

    public void transmitPacket(NetworkDevice srcDev, Layer2Packet packet) {
        devs.stream().filter(dev -> dev != srcDev).forEach(dev -> dev.recivePacket(packet));
        listenerTrPacket.forEach(l -> l.packetTransfered(packet));
    }
    
    public void addTransmitPacketListener(TransferPacketListener listener) {
        listenerTrPacket.add(listener);
        logger.fine(hashCode()+": TransmitPacketListener added");
    }

    public void removeTransmitPacketListener(TransferPacketListener listener) {
        listenerTrPacket.remove(listener);
        logger.fine(hashCode()+": TransmitPacketListener removed");
    }
    
    public void addPhysicalLinkSetUpListener(PhysicalLinkSetUpListener listener) {
        listenerPhLink.add(listener);
        logger.fine(hashCode()+": PhysicalLinkSetUpListener added");
    }

    public void removePhysicalLinkSetUpListener(PhysicalLinkSetUpListener listener) {
        listenerPhLink.remove(listener);
        logger.fine(hashCode()+": PhysicalLinkSetUpListener removed");
    }

    public int getId() {
        return id;
    }

    public void disconnectAll() {
        listenerPhLink.clear();
        listenerTrPacket.clear();
        devs.forEach(dev -> {
            logger.fine(hashCode()+": the media was disconnected from the device: " + dev);
            dev.disconnectMedia();
        });
        devs.clear();
    }
}

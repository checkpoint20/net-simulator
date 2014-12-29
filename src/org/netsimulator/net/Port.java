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

import java.util.*;
import org.netsimulator.util.IdGenerator;

public class Port implements NetworkDevice {

    private int id;
    private IdGenerator idGenerator;

    private Media media;
    private Concentrator concentrator;

    private int rxBytes;
    private int txBytes;
    private int rxPackets;
    private int txPackets;
    private int rxPacketsErrors;
    private int txPacketsErrors;
    private int rxDroped;
    private int txDroped;
    private ArrayList<TransferPacketListener> transferPacketListeners;

    public Port(IdGenerator idGenerator, Concentrator concentrator) {
        this(idGenerator);
        this.concentrator = concentrator;
    }

    public Port(IdGenerator idGenerator) {
        this(idGenerator, idGenerator.getNextId());
    }

    public Port(IdGenerator idGenerator, int id) {
        this.idGenerator = idGenerator;
        this.id = id;

        transferPacketListeners = new ArrayList<TransferPacketListener>();
    }

    public void connectMedia(Media media) {
        this.media = media;
    }

    public void disconnectMedia() {
        this.media = null;
    }

    public void setConcentrator(Concentrator concentrator) {
        this.concentrator = concentrator;
    }

    public void recivePacket(Layer2Packet packet) {

        if (concentrator != null) {
            concentrator.transportPacket(this, packet);

            for (Iterator<TransferPacketListener> i = transferPacketListeners.iterator(); i.hasNext();) {
                TransferPacketListener listener = i.next();
                listener.packetTransfered(packet);
                listener.packetReceived(packet);
            }
        }
    }

    public void transmitPacket(Layer2Packet packet) {
        if (media != null) {
            media.transmitPacket(this, packet);

            for (Iterator<TransferPacketListener> i = transferPacketListeners.iterator(); i.hasNext();) {
                TransferPacketListener listener = i.next();
                listener.packetTransfered(packet);
                listener.packetTransmitted(packet);
            }
        }
    }

    public int getRXBytes() {
        return rxBytes;
    }

    public int getRXDroped() {
        return rxDroped;
    }

    public int getRXPackets() {
        return rxPackets;
    }

    public int getRXPacketsErrors() {
        return rxPacketsErrors;
    }

    public int getTXBytes() {
        return txBytes;
    }

    public int getTXDroped() {
        return txDroped;
    }

    public int getTXPackets() {
        return txPackets;
    }

    public int getTXPacketsErrors() {
        return txPacketsErrors;
    }

    public int getId() {
        return id;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void removeTransferPacketListener(TransferPacketListener listener) {
        transferPacketListeners.remove(listener);
    }

    public void addTransferPacketListener(TransferPacketListener listener) {
        transferPacketListeners.add(listener);
    }

}

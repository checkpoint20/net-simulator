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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class implements pseudo ICMP-packet as it is described in RFC 792.
 *
 * Version 1, 2005-09-25
 *
 * @author Maksim Tereshin
 */
public class ICMPEchoPacket extends IP4Packet {

    public static final int DEFAULT_TTL = 64;

    private static final Logger logger
            = Logger.getLogger(ICMPEchoPacket.class.getName());
    private final int type;
    private final int identifier;
    private final int sequenceNumber;
    private final long timestamp;

    public ICMPEchoPacket(
            int type,
            int identifier,
            int sequenceNumber,
            int totalLength,
            int ttl,
            IP4Address src_address,
            IP4Address dst_address,
            long timestamp,
            Content data
    )
            throws AddressException {
        super(totalLength,
                ttl,
                Protocols.ICMP,
                src_address,
                dst_address,
                data);

        this.type = type;
        this.identifier = identifier;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public int getIdentifier() {
        return identifier;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Generates replay to this packet.
     * @return Replay packet.
     * @throws AddressException 
     */
    public ICMPEchoPacket getICMPReplay() throws AddressException {
        ICMPEchoPacket replay
                = new ICMPEchoPacket(
                        Protocols.ICMPEchoReply,
                        getIdentifier(),
                        getSequenceNumber(),
                        getTotalLength(),
                        DEFAULT_TTL,
                        null,
                        getSourceAddress() == null ? getDestinationAddress() : getSourceAddress(),
                        getTimestamp(),
                        getData()
                );

        logger.log(Level.FINE, "ICMP echo replay packet {identifier: {0}, ttl: {1}, destination: {2}, source: {3}}",
                new Object[]{replay.getIdentifier(), replay.getTTL(), replay.getDestinationAddress(), replay.getSourceAddress()});

        return replay;
    }
}

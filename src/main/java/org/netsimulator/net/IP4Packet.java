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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The class implements pseudo IP-packet as it is described in RFC 791.
 *
 * Version 1, 2004-06-30
 *
 * Some exceptions: a) not implements TOS b) not implements fragmentation c)
 * header checksum is never computed, it is 0 every time d) not implements
 * Options (so we do not need Padding) e) version is always 4 f) Internet Header
 * Length is always 5
 *
 */
public class IP4Packet implements Packet, Content {

    private final IP4Address dstAddress;
    private IP4Address srcAddress;
    private final byte version;
    private final byte ihl;
    private final int totalLength;
    private final AtomicInteger ttl;
    private final int headerChecksum;
    private final int protocol;
    private final Content data;

    public IP4Packet(int totalLength,
            int ttl,
            int protocol,
            IP4Address srcAddress,
            IP4Address dstAddress,
            Content data)
            throws AddressException {
        this.version = 4;
        this.ihl = 5;
        this.headerChecksum = 0;
        this.data = null;

        this.totalLength = totalLength;
        this.ttl = new AtomicInteger(ttl);
        this.protocol = protocol;

        if (srcAddress != null && !(srcAddress instanceof IP4Address)) {
            throw new AddressException("Invalid source IP4 address.");
        } else {
            this.srcAddress = (IP4Address) srcAddress;
        }

        if (dstAddress == null || !(dstAddress instanceof IP4Address)) {
            throw new AddressException("Invalid destination IP4 address.");
        } else {
            this.dstAddress = (IP4Address) dstAddress;
        }

    }

    @Override
    public Content getData() {
        return data;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public int getProtocol() {
        return protocol;
    }

    public int getTTL() {
        return ttl.intValue();
    }

    @Override
    public IP4Address getSourceAddress() {
        return srcAddress;
    }

    @Override
    public byte[] toBytesArray() {
        return null;
    }

    @Override
    public IP4Address getDestinationAddress() {
        return dstAddress;
    }

    public byte getVersion() {
        return version;
    }

    public int getHeaderChecksum() {
        return headerChecksum;
    }

    public byte getIhl() {
        return ihl;
    }

    void decrimentTTL() {
        ttl.decrementAndGet();
    }

    public void setSourceAddress(IP4Address srcAddress) throws AddressException {
        if (srcAddress != null && !(srcAddress instanceof IP4Address)) {
            throw new AddressException("Invalid source IP4 address.");
        } else {
            this.srcAddress = (IP4Address) srcAddress;
        }
    }
}

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


/**
 * The class implements pseudo IP-packet as it is described in RFC 791.
 *
 * Version 1, 2004-06-30
 *
 * Some exceptions:
 * a) not implements TOS
 * b) not implements fragmentation
 * c) header checksum is never computed, it is 0 every time
 * d) not implements Options (so we do not need Padding)
 * e) version is always 4
 * f) Internet Header Length is always 5
 *
 */
public class IP4Packet implements Packet, Content
{
    private IP4Address dst_address;
    private IP4Address src_address;
    private byte version;
    private byte ihl;
    private int totalLength;
    private int ttl;
    private int headerChecksum;
    private int protocol;
    private Content data;





    public IP4Packet(int totalLength,
                     int ttl,
                     int protocol,
                     IP4Address src_address,
                     IP4Address dst_address,
                     Content data)
        throws AddressException
    {
        version = 4;
        ihl = 5;
        headerChecksum = 0;

        setTotalLength(totalLength);
        setTTL(ttl);
        setProtocol(protocol);
        setSourceAddress(src_address);
        setDestinationAddress(dst_address);
    }





    public void setData(Content data)
    {
        this.data = data;
    }


    public Content getData()
    {
        return data;
    }





    public void setTotalLength(int totalLength)
    {
        this.totalLength = totalLength;
    }


    public int getTotalLength()
    {
        return totalLength;
    }



    public void setProtocol(int protocol)
    {
        this.protocol = protocol;
    }


    public int getProtocol()
    {
        return protocol;
    }



    public void setTTL(int ttl)
    {
        this.ttl = ttl;
    }



    public int getTTL()
    {
        return ttl;
    }



    public void setDestinationAddress(Address address)
        throws AddressException
    {
        if(address==null || !(address instanceof IP4Address))
            throw new AddressException("Invalid IP4 address.");

        dst_address = (IP4Address)address;
    }



    public IP4Address getDestinationAddress()
    {
        return dst_address;
    }



    public void setSourceAddress(Address address)
        throws AddressException
    {
        if(address!=null && !(address instanceof IP4Address))
            throw new AddressException("Invalid IP4 address.");

        src_address = (IP4Address)address;
    }


    public IP4Address getSourceAddress()
    {
        return src_address;
    }



    public byte[] toBytesArray()
    {
        return null;
    }
}

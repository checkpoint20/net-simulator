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
 * The class implements pseudo-packet of data link layer of OSI.
 * It is neither ethernet packet nor anything else in real world.
 */
public class Layer2Packet implements Packet, Content
{
    private MACAddress dst_address;
    private MACAddress src_address;
    private int etherType;
    private Content data;



    public Layer2Packet(MACAddress src_address,
                        MACAddress dst_address,
                        int etherType,
                        Content data)
        throws AddressException
    {
        setSourceAddress(src_address);
        setDestinationAddress(dst_address);
        this.etherType = etherType;
        this.data = data;
    }



    public void setDestinationAddress(Address address)
        throws AddressException
    {
        if(address==null || !(address instanceof MACAddress))
            throw new AddressException("Invalid MAC address.");

        dst_address = (MACAddress)address;
    }


    public Address getDestinationAddress()
    {
        return dst_address;
    }


    public void setSourceAddress(Address address)
        throws AddressException
    {
        if(address==null || !(address instanceof MACAddress))
            throw new AddressException("Invalid MAC address.");

        src_address = (MACAddress)address;
    }


    public Address getSourceAddress()
    {
        return src_address;
    }


    public void setData(Content data)
    {
        this.data = data;
    }



    public Content getData()
    {
        return data;
    }

    
    public int getEtherType()
    {
        return etherType;
    }


    public byte[] toBytesArray()
    {
        return null;
    }

    
}

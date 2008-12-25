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
import org.netsimulator.gui.MainFrame;
import org.netsimulator.util.LogFormatter;
import org.netsimulator.util.LoggersConfig;

/**
 * The class implements pseudo ICMP-packet as it is described in RFC 792.
 *
 * Version 1, 2005-09-25
 *
 * @author  Maksim Tereshin
 */
public class ICMPEchoPacket extends IP4Packet
{
    public static final int DEFAULT_TTL = 64;
    
    private static final Logger logger = 
            Logger.getLogger("org.netsimulator.net.EthernetInterface");
    private int type = -1;
    private int identifier = -1;
    private int sequenceNumber = -1;
    private long timestamp = -1;


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
            throws AddressException
    {
        super(  totalLength,
                ttl,
                Protocols.ICMP,
                src_address,
                dst_address,
                data  );
        
        this.type = type;
        this.identifier = identifier;
        this.sequenceNumber = sequenceNumber;
        this.timestamp = timestamp;
    }
    
    
    
    public int getType()
    {
        return  type;
    }
    
    
    
    public int getIdentifier()
    {
        return identifier;
    }
    
    
    public void setSequenceNumber(int sequenceNumber)
    {
        this.sequenceNumber = sequenceNumber;
    }
    
    
    
    public int getSequenceNumber()
    {
        return sequenceNumber;
    }
    
    
    
    public long getTimestamp()
    {
        return timestamp;
    }
    
    
    public ICMPEchoPacket getICMPReplay() throws AddressException
    {
        ICMPEchoPacket replay = 
            new ICMPEchoPacket(
                Protocols.ICMPEchoReply,
                getIdentifier(), 
                getSequenceNumber(),
                getTotalLength(),
                DEFAULT_TTL,
                null,
                getSourceAddress()==null?getDestinationAddress():getSourceAddress(),
                getTimestamp(),
                getData() 
                );
        
        logger.fine(hashCode()+": made the ICMP echo replay packet:\n"+
                "identifier:  "+replay.getIdentifier()+"\n"+
                "ttl:         "+replay.getTTL()+"\n"+
                "destination: "+replay.getDestinationAddress()+"\n"+
                "source:      "+replay.getSourceAddress()+"\n");        
        
        return replay;
    }
}

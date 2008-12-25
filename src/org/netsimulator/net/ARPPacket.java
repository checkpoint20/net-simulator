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
 * The class implements ARP packet. 
 *
 * @author  Maksim Tereshin
 */
public class ARPPacket implements Content
{
    public static final int REQUEST = 1;
    public static final int REPLAY  = 2;
    
    private MACAddress sourceMacAddress;
    private MACAddress resolvedAddress;
    private IP4Address addressToResolve;
    private int operation;

    
    public ARPPacket( MACAddress sourceMacAddress,
                      IP4Address addressToResolve, 
                      int operation)
        throws AddressException
    {
        this.sourceMacAddress = sourceMacAddress;
        this.addressToResolve = addressToResolve;
        this.operation = operation;
    }


    public MACAddress getSourceMacAddress()
    {
        return sourceMacAddress;
    }
    
    public IP4Address getAddressToResolve()
    {
        return addressToResolve;
    }
    
    
    public int getOperation()
    {
        return operation;
    }
    
    
    public void setResolvedAddress(MACAddress reslovedAddress)
    {
        this.resolvedAddress = reslovedAddress;
    }
    
    
    public MACAddress getResolvedAddress()
    {
        return resolvedAddress;
    }
    
    public void setOperation(int operation)
    {
        this.operation = operation;
    }
    
    public byte[] toBytesArray()
    {
        return null;
    }

    
}

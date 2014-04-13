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


import java.util.StringTokenizer;


public class MACAddress implements Address
{
    
    public  static final MACAddress BROADCAST = new MACAddress(0xFFFFFFFFFFFFl);
    
    private long address = 0;
    
    
    public MACAddress(byte[] address)
    {
        fromByteArray(address);
    }
    
    
    
    public MACAddress(MACAddress address)
    {
        if( address != null )
        {
            this.address = address.toLongValue();
        }
    }
    
    
    
    
    public MACAddress( long address )
    {
        this.address = address;
    }
    
    
    
    
    public MACAddress( String address ) throws AddressException
    {
        fromString( address );
    }
    
    
    
    
    public long toLongValue()
    {
        return address;
    }
    
    
    
    @Override
    public byte[] toBytesArray()
    {
        byte buf[] = new byte[6];
        buf[0] = (byte)((address >>> 40) & 0x0000000000FF);
        buf[1] = (byte)((address >>> 32) & 0x0000000000FF);
        buf[2] = (byte)((address >>> 24) & 0x0000000000FF);
        buf[3] = (byte)((address >>> 16) & 0x0000000000FF);
        buf[4] = (byte)((address >>> 8 ) & 0x0000000000FF);
        buf[5] = (byte)(address          & 0x0000000000FF);
        return buf;
    }
    
    
    @Override
    public String toString()
    {
        byte buf[] = toBytesArray();
        return
                Integer.toHexString(buf[0]&0xFF).toUpperCase()+":"+
                Integer.toHexString(buf[1]&0xFF).toUpperCase()+":"+
                Integer.toHexString(buf[2]&0xFF).toUpperCase()+":"+
                Integer.toHexString(buf[3]&0xFF).toUpperCase()+":"+
                Integer.toHexString(buf[4]&0xFF).toUpperCase()+":"+
                Integer.toHexString(buf[5]&0xFF).toUpperCase();
    }
    
    
    
    
    
    @Override
    public final void fromString( String address ) throws AddressException
    {
        StringTokenizer tokenizer = new StringTokenizer( address, ":" );
        int n = tokenizer.countTokens();
        
        if( n != 6 )
        {    
           throw new AddressException("Invalid MAC address!");
        }
        
        byte buf[] = new byte[n];
        
        for(int i=0; i!=n; i++)
        {
            try
            {
                short sh = Short.parseShort(tokenizer.nextToken(), 16);
                if( sh<0 || sh>255 )
                {
                    throw new AddressException("Invalid MAC address!");
                }
                else
                {
                    buf[i] = (byte)sh;
                }
            }
            catch(NumberFormatException nfe)
            {
                throw new AddressException("Invalid MAC address!");
            }
        }
        
        fromByteArray(buf);
    }
    
    
    
    
    
    
     
    public final void fromByteArray( byte address[] )
    {
        if( address!=null && address.length==6 )
        {
            this.address =   (long)address[5]        & 0x0000000000FFl;
            this.address |= ((long)address[4] << 8)  & 0x00000000FF00l;
            this.address |= ((long)address[3] << 16) & 0x000000FF0000l;
            this.address |= ((long)address[2] << 24) & 0x0000FF000000l;
            this.address |= ((long)address[1] << 32) & 0x00FF00000000l;
            this.address |= ((long)address[0] << 40) & 0xFF0000000000l;
        }
    }
    
    
    @Override
    public boolean equals( Object obj )
    {
        return
                obj != null &&
                obj instanceof MACAddress &&
                ((MACAddress)obj).address == this.address;
    }


    @Override
    public int hashCode() 
    {
        int result;
        result = (int) (address ^ (address >>> 32));
        return result;
    } 

    
    public boolean isBroadcast()
    {
        return (address & 0xFFFFFFFFFFFFl) == 0xFFFFFFFFFFFFl ;
    }
    
}


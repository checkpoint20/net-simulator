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

public class IP4Address implements Address {

    private int address = 0;

    public IP4Address() {
    }

    public IP4Address( byte[] address ) {
        fromByteArray( address );
    }

    public IP4Address( IP4Address address ) {
        if( address != null ) {
            this.address = address.toIntValue();
        }
    }

    public IP4Address( int address ) {
        this.address = address;
    }

    public IP4Address( String address ) throws AddressException {
        fromString( address );
    }

    public int toIntValue() {
        return address;
    }

    public byte[] toBytesArray() {
        byte buf[] = new byte[4];
        buf[0] = (byte) ( ( address >>> 24 ) & 0x000000FF );
        buf[1] = (byte) ( ( address >>> 16 ) & 0x000000FF );
        buf[2] = (byte) ( ( address >>> 8 ) & 0x000000FF );
        buf[3] = (byte) ( address & 0x000000FF );
        return buf;
    }

    @Override
    public String toString() {
        byte buf[] = toBytesArray();
        return ( buf[0] & 0xFF ) + "." + ( buf[1] & 0xFF ) + "." + ( buf[2] & 0xFF ) + "." + ( buf[3] & 0xFF );
    }

    public static boolean isStringVilid( String address ) {
        StringTokenizer tokenizer = new StringTokenizer( address, "." );
        int n = tokenizer.countTokens();

        if( n != 4 ) {
            return false;
        }

        byte buf[] = new byte[4];

        for( int i = 0; i != n; i++ ) {
            try {
                short sh = Short.parseShort( tokenizer.nextToken() );
                if( sh < 0 || sh > 255 ) {
                    return false;
                } else {
                    buf[i] = (byte) sh;
                }
            } catch( NumberFormatException nfe ) {
                return false;
            }
        }

        return true;
    }

    public void fromString( String address ) throws AddressException {
        StringTokenizer tokenizer = new StringTokenizer( address, "." );
        int n = tokenizer.countTokens();

        if( n != 4 ) {
            throw new AddressException( "Invalid IP4 address!" );
        }

        byte buf[] = new byte[4];

        for( int i = 0; i != n; i++ ) {
            try {
                short sh = Short.parseShort( tokenizer.nextToken() );
                if( sh < 0 || sh > 255 ) {
                    throw new AddressException( "Invalid IP4 address!" );
                } else {
                    buf[i] = (byte) sh;
                }
            } catch( NumberFormatException nfe ) {
                throw new AddressException( "Invalid IP4 address!" );
            }
        }

        fromByteArray( buf );
    }

    public void fromByteArray( byte address[] ) {
        if( address != null && address.length == 4 ) {
            this.address = address[3] & 0x000000FF;
            this.address |= ( ( address[2] << 8 ) & 0x0000FF00 );
            this.address |= ( ( address[1] << 16 ) & 0x00FF0000 );
            this.address |= ( ( address[0] << 24 ) & 0xFF000000 );
        }
    }

    public boolean isClassA() {
        return address >>> 31 == 0;
    }

    public boolean isClassB() {
        return address >>> 30 == 2;
    }

    public boolean isClassC() {
        return address >>> 29 == 6;
    }

    public boolean isClassD() {
        return address >>> 28 == 14;
    }

    public boolean isClassE() {
        return address >>> 27 == 30;
    }

    @Override
    public int hashCode() {
        return address;
    }

    @Override
    public boolean equals( Object obj ) {
        return obj != null &&
                obj instanceof IP4Address &&
                ( (IP4Address) obj ).address == this.address;
    }
}


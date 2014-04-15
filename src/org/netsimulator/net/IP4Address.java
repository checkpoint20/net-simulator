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
import java.util.logging.Level;
import java.util.logging.Logger;

public class IP4Address implements Address {

    private static final Logger LOGGER = 
            Logger.getLogger(IP4Address.class.getName());
    
    private final int address;

    public IP4Address( byte[] address ) {
        this.address = fromByteArray( address );
    }

    public IP4Address( IP4Address address ) {
        this.address = address.toIntValue();
    }

    public IP4Address( int address ) {
        this.address = address;
    }

    public IP4Address( String address ) throws AddressException {
        this.address = fromString( address );
    }

    public int toIntValue() {
        return address;
    }

    @Override
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
        StringBuilder res = new StringBuilder();
        return res.
                append(buf[0] & 0xFF).append(".").
                append(buf[1] & 0xFF).append(".").
                append(buf[2] & 0xFF).append(".").
                append(buf[3] & 0xFF).toString();
    }


    private int fromString( String address ) throws AddressException {
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

        return fromByteArray( buf );
    }

    private int fromByteArray( byte address[] ) {
        int res = 0;
        if( address != null && address.length == 4 ) {
            res = address[3] & 0x000000FF;
            res |= ( ( address[2] << 8 ) & 0x0000FF00 );
            res |= ( ( address[1] << 16 ) & 0x00FF0000 );
            res |= ( ( address[0] << 24 ) & 0xFF000000 );
        }
        return res;
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
    
    
    /**
     * There are limited amount of netmask addresses.
     * <pre>
     *   Netmask                Address Prefix Length	Hosts / Class C's / Class B's / Class A's
     *   255.255.255.255	/32	1
     *   255.255.255.254	/31	2
     *   255.255.255.252	/30	4
     *   255.255.255.248	/29	8
     *   255.255.255.240	/28	16
     *   255.255.255.224	/27	32
     *   255.255.255.192	/26	64
     *   255.255.255.128	/25	128
     *   255.255.255.0          /24 (Class C)	256 / 1
     *   255.255.254.0          /23	512 / 2
     *   255.255.252.0          /22	1,024 / 4
     *   255.255.248.0          /21	2,048 / 8
     *   255.255.240.0          /20	4,096 / 16
     *   255.255.224.0      	/19	8,192 / 32
     *   255.255.192.0          /18	16,384 / 64
     *   255.255.128.0          /17	32,768 / 128
     *   255.255.0.0            /16 (Class B)	65,536 / 256 / 1
     *   255.254.0.0            /15	131,072 / 512 / 2
     *   255.252.0.0            /14	262,144 / 1024 / 4
     *   255.248.0.0            /13	524,288 / 2048 / 8
     *   255.240.0.0            /12	1,048,576 / 4096 / 16
     *   255.224.0.0            /11	2,097,152 / 8129 / 32
     *   255.192.0.0            /10	4,194,304 / 16,384 / 64
     *   255.128.0.0            /9	8,388,608 / 32,768 / 128
     *   255.0.0.0              /8 (Class A)	16,777,216 / 65,536 / 256 / 1
     *   254.0.0.0              /7	33,554,432 / 131,072 / 512 / 2
     *   252.0.0.0              /6	67,108,864 / 262,144 / 1,024 / 4
     *   248.0.0.0              /5	134,217,728 / 524,288 / 2,048 / 8
     *   240.0.0.0              /4	268,435,456 / 1,048,576 / 4,096 / 16
     *   224.0.0.0              /3	536,870,912 / 2,097,152 / 8,192 / 32
     *   192.0.0.0              /2	1,073,741,824 / 4,194,304 / 16,384 / 64
     *   128.0.0.0              /1	2,147,483,648 / 8,388,608 / 32,768 / 128
     *   0.0.0.0                /0 (The Internet)	4,294,967,296 / 16,777,216 / 65,536 / 256
     *   </pre>
     * @param netmaskAddress
     * @return 
     */
     public static boolean isNetmaskAddressValid(IP4Address netmaskAddress) {
        boolean res = false;
        int maskTemplate = 0x80000000; // 10000000 00000000 00000000 00000000 - 128.0.0.0
        for(int i = 0; i != 32; i++) {
            if( maskTemplate == netmaskAddress.toIntValue() ) {
                res = true;
                break;
            } else {
                maskTemplate >>= 1;
            }
        }
        return res;
    }

     
    public static IP4Address evaluateNetworkAddress(IP4Address inetAddress, IP4Address netmaskAddress) {
        if(inetAddress==null || netmaskAddress==null) {
            return null;
        }else {
            return new IP4Address(inetAddress.toIntValue() & netmaskAddress.toIntValue());
        }
    }

    
    /**
     * Trying to evaluate broadcast for given address and netmask.
     * @param address
     * @param netmask
     * @return 
     */
    public static IP4Address evaluateBroadcastAddress( IP4Address address, IP4Address netmask ) {
        int broadcastInt = address.toIntValue() | ~netmask.toIntValue();
        return new IP4Address( broadcastInt );
    }    
    
    /**
     * Trying to evaluate netmask for given address by its class.
     * Returns null if failed to evaluate netmask address.
     * @param address
     * @return 
     */
    public static IP4Address evaluateNetmaskAddress(IP4Address address) {
        
        IP4Address netmask = null;

        if (address.isClassA()) {
            netmask = new IP4Address(0xFF000000); // 255.0.0.0
            LOGGER.log(Level.FINE, "Address {0} is of class A => netmask is {1}", new Object[]{address, netmask});
        } else if (address.isClassB()) {
            netmask = new IP4Address(0xFFFF0000); // 255.255.0.0
            LOGGER.log(Level.FINE, "Address {0} is of class B => netmask is {1}", new Object[]{address, netmask});
        } else if (address.isClassC()) {
            netmask = new IP4Address(0xFFFFFF00); // 255.255.255.0
            LOGGER.log(Level.FINE, "Address {0} is of class C => netmask is {1}", new Object[]{address, netmask});
        } 

        return netmask;
    }
    
}


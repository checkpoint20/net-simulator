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
 * The class contents constants for some protocols
 * as discribed in RFC 1010 (ASSIGNED NUMBERS).
 */

public class Protocols
{
    //Assigned Internet Protocol Numbers
    public final static int ICMP =  1;
    public final static int TCP  =  6;
    public final static int UDP  = 17;
    
    //EtherType
    public final static int IP   = 2048;
    public final static int ARP  = 2054;
    
    
    // ICMP types
    public static final int ICMPEchoReply = 0;
    public static final int ICMPDestinationUnreachable = 3;
    public static final int ICMPSourceQuench = 4;
    public static final int ICMPRedirect = 5;
    public static final int ICMPEcho = 8;
    public static final int ICMPTimeExceeded = 11;
    public static final int ICMPParameterProblem = 12;
    public static final int ICMPTimestamp = 13;
    public static final int ICMPTimestampReply = 14;
    public static final int ICMPInformationRequest = 15;
    public static final int ICMPInformationReply = 16;    
    
}
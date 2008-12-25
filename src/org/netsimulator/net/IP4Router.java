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
/*
 * Router.java
 *
 * Created on 2004, 10:48
 */
package org.netsimulator.net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.*;
import org.netsimulator.util.IdGenerator;

public class IP4Router implements Router {

    private int id;
    private IdGenerator idGenerator;
    private ArrayList<Interface> interfaces;
    private RoutingTable routingTable;
    private ArrayList<ICMPEchoReplayListener> icmpReplayListeners;
    private static final Logger logger =
            Logger.getLogger( "org.netsimulator.net.IP4Router" );

    /** Create new router with 8 interfaces.
     */
    public IP4Router( IdGenerator idGenerator ) throws TooManyInterfacesException {
        this( idGenerator, 8, idGenerator.getNextId() );
    }

    /** Create new router with n interfaces.
     */
    public IP4Router( IdGenerator idGenerator, int n ) throws TooManyInterfacesException {
        this( idGenerator, n, idGenerator.getNextId() );
    }

    /** Create new router with n interfaces and id
     */
    public IP4Router( IdGenerator idGenerator, int n, int id ) throws TooManyInterfacesException {
        if( n + 1 >= 255 ) {
            throw new TooManyInterfacesException( "You can not create more than 256 interfaces!" );
        }

        this.id = idGenerator.getNextId();
        this.id = id;

        long address_v = this.id;
        address_v = address_v << 8;


        interfaces = new ArrayList<Interface>();

        for( int i = 0; i != n; i++ ) {
            MACAddress addr = new MACAddress( address_v | (long) i );
            EthernetInterface ifs = new EthernetInterface( idGenerator, addr, "eth" + i );
            ifs.setRouter( this );
            interfaces.add( ifs );
        }

        interfaces.add( new NullInterface( idGenerator, "null0" ) );
        
        routingTable = new RoutingTable();
        icmpReplayListeners = new ArrayList<ICMPEchoReplayListener>();

        logger.fine( hashCode() + ": IP4 router instantiated" );
    }

    public void routePacket( Packet packet ) {
        logger.fine( getId() + ": begin routing" );

        IP4Packet ip4packet = null;
        if( packet instanceof IP4Packet ) {
            ip4packet = (IP4Packet) packet;

            if( ip4packet.getSourceAddress() != null ) {
                ip4packet.setTTL( ip4packet.getTTL() - 1 );
            }

            if( ip4packet.getTTL() == 0 ) {
                logger.fine( getId() + ": packet " + packet + " TTL=0 packet dropped" );
                return;
            }
        } else {
            logger.fine( getId() + ": this packet " + packet + " is not IP4 packet, routing aborted" );
            return;
        }

        /*      1.  Check if the packet is targeting to this host. If true go to 2. 
        If else 4.
        2.  Check if it is ICMP packet. If true go to 3. If else go to 55.
        3.  Check if it is ICMP echo request packet. If true make ICMP replay 
        and go to 77. If else check go to 4.
         *
         *      4.  Check if it is ICMP echo replay packet. If true put the packet
         *          to the proper ICMP replay listener. If else .... 
         *
         *      55. Check if it is UDP packet. If true put the packet to the proper 
        receiver according the port number. If else internal error!
         *
         *      77  Route packet. Go through the routing table. If path find make 
         *          leyer 2 packet and send it to the next host. Otherwise make 
        ICMP replay 'No route to host'.
         */

        goThroughRoutingTable( ip4packet );
    }

    public int getId() {
        return id;
    }

    public void setId( int id ) {
        this.id = id;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public Interface[] getInterfaces() {
        Interface[] array = new Interface[interfaces.size()];
        return interfaces.toArray( array );
    }

    public Interface getInterface( int i ) {
        return (Interface) interfaces.get( i );
    }

    public Interface getInterfaceById( int id ) {
        for( Interface ifs : interfaces ) {
            if( ifs.getId() == id ) {
                return ifs;
            }
        }
        throw new NoSuchDeviceException("There is not interface with id '"+id+"'");
    }

    public NetworkDevice getNetworkDeviceById( int id ) {
        Interface ifs = getInterfaceById( id );
        if( ifs instanceof NetworkDevice ) {
            return (NetworkDevice) ifs;
        } 
        throw new NoSuchDeviceException("There is not device with id '"+id+"'");
    }

    public Interface getInterface( String name ) {
        Interface iface = null;

        for( Iterator<Interface> i = interfaces.iterator(); i.hasNext();) {
            Interface buf = i.next();
            if( buf.getName().equals( name ) ) {
                iface = buf;
                break;
            }
        }

        return iface;
    }

    public int getInterfaceCount() {
        return interfaces.size();
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public void addICMPEchoReplayListener( ICMPEchoReplayListener listener ) {
        icmpReplayListeners.add( listener );
    }

    public void removeICMPEchoReplayListener( ICMPEchoReplayListener listener ) {
        icmpReplayListeners.remove( listener );
    }

    public void addInterface( Interface ifs ) {
        ifs.setRouter( this );
        interfaces.add( ifs );
    }

    private void processICMPEchoReplay( ICMPEchoPacket packet ) {
        for( Iterator<ICMPEchoReplayListener> i = icmpReplayListeners.iterator(); i.hasNext();) {
            ICMPEchoReplayListener listener = i.next();
            listener.processICMPEchoReplay( packet );
        }
    }

    private void processIP4Packet( IP4Packet packet ) {

    }

    private void goThroughRoutingTable( IP4Packet packet ) {
        logger.fine( getId() + ": begin going through routing table" );

        RoutingTableRow res = routingTable.route( packet.getDestinationAddress() );

        if( res == null ) {
            // make ICMP replay 'No route to host' here
            logger.info( getId() + " Network is unreachable: " + packet.getDestinationAddress() );
            return;
        }

        // get interface the packet we must pass through
        IP4Address gateway = res.getGateway();
        Interface ifs = res.getInterface();
        IP4EnabledInterface ip4ifs = null;
        if( ifs instanceof IP4EnabledInterface ) {
            ip4ifs = (IP4EnabledInterface) ifs;
        } else {
            logger.info( getId() + "Trying to send IP4Packet throught not IP4 enabled interface " + ifs.getName() );
            return;
        }


        if( ip4ifs.getInetAddress().equals( packet.getDestinationAddress() ) ) {
            // the packet is targeting to this host
            logger.fine( hashCode() + ": the packet " + packet + " is targeting to this host" );
            switch( packet.getProtocol() ) {
                case Protocols.ICMP:
                    ICMPEchoPacket icmppacket = null;
                    if( packet instanceof ICMPEchoPacket ) {
                        icmppacket = (ICMPEchoPacket) packet;
                        switch( icmppacket.getType() ) {
                            case Protocols.ICMPEcho:
                                ICMPEchoPacket replay = null;
                                try {
                                    replay = icmppacket.getICMPReplay();
                                } catch( AddressException ae ) {
                                    System.err.println( "Internal error: can not make ICMPEchoReplay packet" );
                                    ae.printStackTrace();
                                    break;
                                }
                                goThroughRoutingTable( replay );
                                break;
                            case Protocols.ICMPEchoReply:
                                processICMPEchoReplay( icmppacket );
                                break;
                        }
                        break;
                    }
                    logger.severe( "Internal error: don't know what to do with this packet! Unknown ICMP packet " + icmppacket.getClass() );
                    break;
                case Protocols.UDP:
                    processIP4Packet( packet );
                    break;
                default:
                    logger.severe( "Internal error: don't know what to do with this packet! Unknown protokol " + packet.getProtocol() );
                    break;
            }
        } else {
            // the packet is not targeting to this host
            logger.fine( hashCode() + ": the packet " + packet + " is not targeting to this host" );

            if( packet.getSourceAddress() == null ) {
                try {
                    packet.setSourceAddress( ip4ifs.getInetAddress() );
                } catch( AddressException ae ) {
                    System.out.println( "Error: can not set source IP address " + ae.getMessage() );
                    return;
                }
            } 

            if( gateway == null ) {
                ip4ifs.transmitPacket( packet, packet.getDestinationAddress() );
            } else {
                ip4ifs.transmitPacket( packet, gateway );
            }
        }


    }


}

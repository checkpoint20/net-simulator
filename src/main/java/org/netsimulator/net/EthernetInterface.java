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


import org.netsimulator.util.ConfigurableThreadFactory;
import org.netsimulator.util.IdGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EthernetInterface 
        implements IP4EnabledInterface, NetworkDevice
{
    private static final Logger LOGGER = 
            Logger.getLogger(EthernetInterface.class.getName());

    public static final int ARP_CACHE_CLEAN_TIMEOUT = 10; // 10 sec.    
    
    // 10 threads for all the NetSimulator. 
    // Probaly it makes sense to have 1 thread per router?
    private static final ScheduledExecutorService arpCacheCleanExecutorService = 
                                  Executors.newScheduledThreadPool(10, new ConfigurableThreadFactory("ArpCacheCleanup-"));
    
    private int id;
    private IdGenerator idGenerator;   
    private Router router;
    private Media media;
    private MACAddress macAddress;
    private IP4Address inetAddress;
    private IP4Address broadcastAddress;
    private IP4Address netmaskAddress;
    private int status;
    private int bandwidth;
    private String encap;
    private int rxBytes;
    private int txBytes;
    private int rxPackets;
    private int txPackets;
    private int rxPacketsErrors;
    private int txPacketsErrors;
    private int rxDroped;
    private int txDroped;
    private ARPCache arpCache;
    private String name;
    private ArrayList<TransferPacketListener> transferPacketListeners;

    
    public EthernetInterface(
            IdGenerator idGenerator, 
            MACAddress address, 
            String name)
    {
        this(
                idGenerator,
                idGenerator.getNextId(),
                address,
                name
            );
    }


    public EthernetInterface(
            IdGenerator idGenerator, 
            int id, 
            MACAddress address, 
            String name)
    {
        this.idGenerator = idGenerator;
        this.id = id;
        this.macAddress = address;
        this.name = name;
        encap = "Ethernet  HWaddr "+address;
        status = Interface.DOWN;
        
        arpCache = new ARPCache(ARP_CACHE_CLEAN_TIMEOUT);
        
        arpCacheCleanExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                arpCache.clean();
            }
        }, ARP_CACHE_CLEAN_TIMEOUT, ARP_CACHE_CLEAN_TIMEOUT, TimeUnit.SECONDS);
        
        transferPacketListeners = new ArrayList<TransferPacketListener>();
    }
    
    
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    
    public String getName()
    {
        return name;
    }
    
    
    @Override
    public String toString()
    {
        return getName();
    }
        
    
    
    public void setRouter(Router router)
    {
        this.router = router;
    }



    public void connectMedia(Media media)
    {
        this.media = media;
    }


    public void disconnectMedia()
    {
        this.media = null;
    }


    
    
    
    public void recivePacket(Layer2Packet l2packet)
    {
        if(getStatus() == Interface.DOWN){ return; }
        
        if(router == null /*||
           !(packet instanceof Layer2Packet)*/)
        {
            rxDroped++;
            return;
        }

        //Layer2Packet l2packet = (Layer2Packet)packet;

        if(this.macAddress.equals((MACAddress)l2packet.getDestinationAddress()) ||
           ((MACAddress)l2packet.getDestinationAddress()).isBroadcast())
        {
            
            switch(l2packet.getEtherType())
            {
                case Protocols.ARP :
                    ARPPacket arpPacket= (ARPPacket)l2packet.getData();
                    switch(arpPacket.getOperation())        
                    {
                        case ARPPacket.REQUEST :
                            try
                            {        
                                processArpRequest(arpPacket);
                            }catch(AddressException ae)
                            {
                                ae.printStackTrace();
                            }
                            rxPackets++;
                            break;
                        case ARPPacket.REPLAY :
                            try
                            {
                                processArpReplay(arpPacket);
                            }catch(AddressException ae)
                            {
                                ae.printStackTrace();
                            }
                            rxPackets++;
                            break;
                        default :
                            rxPacketsErrors++;
                    }
                    break;
                    
                case Protocols.IP :
                    router.routePacket((Packet)l2packet.getData());
                    rxPackets++;
                    break;

                default :
                    rxPacketsErrors++;
            }
        }
        
        for(Iterator<TransferPacketListener> i = transferPacketListeners.iterator(); i.hasNext(); )
        {
            TransferPacketListener listener = i.next();
            listener.packetTransfered(l2packet);
            listener.packetReceived(l2packet);
        }        
    }


    public void transmitPacket(Layer2Packet packet)
    {
        if(getStatus() == Interface.DOWN){ return; }
        
        if(media != null)
        {
            media.transmitPacket(this, packet);
            txPackets++;
            for(Iterator<TransferPacketListener> i = transferPacketListeners.iterator(); i.hasNext(); )
            {
                TransferPacketListener listener = i.next();
                listener.packetTransfered(packet);
                listener.packetTransmitted(packet);
            }            
        }else
        {
            txPacketsErrors++;
        }
    }


    public void setStatus(int status) throws ChangeInterfacePropertyException
    {
        
        /*
         *  Should we drop counters when the interface is coming down?
         */
  
        if( router instanceof IP4Router )
        {
            switch( status )
            {
                case UP:
                    if( this.status != UP ) 
                    { 
                        LOGGER.log(Level.FINE, "Setting {0} UP, address {1}, mask {2}, broadcast {3}", 
                                new Object[]{getName(), getInetAddress(), getNetmaskAddress(), getBroadcastAddress()});
                        
                        if( getInetAddress() == null ) 
                        {
                            throw new ChangeInterfacePropertyException( "Cannot change interface status, set inet address before." );
                        }

                        if( getNetmaskAddress() == null ) 
                        {
                            setNetmaskAddress( IP4Address.evaluateNetmaskAddress( getInetAddress() ) );
                            if( getNetmaskAddress() == null ) 
                            {
                                throw new ChangeInterfacePropertyException( "Netmask address not specified." );
                            }
                        } 
                        else if(!IP4Address.isNetmaskAddressValid(getNetmaskAddress()))
                        {
                            throw new ChangeInterfacePropertyException( "Invalid netmask." );
                        }

                        if( getInetAddress().equals(getNetworkAddress()) ) 
                        {
                            throw new ChangeInterfacePropertyException( "Inet address cannot be equal network one." );
                        }
                        
                        if( getBroadcastAddress() == null ) 
                        {
                            setBroadcastAddress( IP4Address.evaluateBroadcastAddress( getInetAddress(), getNetmaskAddress() ) );
                        }
                        
                        if( getInetAddress().equals(getBroadcastAddress()) ) 
                        {
                            throw new ChangeInterfacePropertyException( "Inet address can not be equal broadcast one." );
                        }
                        
                        try
                        {
                            ((IP4Router)router).getRoutingTable().addRoute( getNetworkAddress(), 
                                         getNetmaskAddress(),
                                         null,
                                         0, 
                                         this );
                            this.status = status;                            
                        } catch ( NotAllowedAddressException e ) 
                        {
                            throw new ChangeInterfacePropertyException( e.getMessage() );
                        }
                    }
                    break;
                    
                case DOWN:
                    if( this.status != DOWN ) 
                    { 
                        LOGGER.log(Level.FINE, "Setting {0} DOWN, address {1}, mask {2}, broadcast {3}", 
                                new Object[]{getName(), getInetAddress(), getNetmaskAddress(), getBroadcastAddress()});
                    
                        ((IP4Router)router).getRoutingTable().deleteRoute( getNetworkAddress(), 
                                     getNetmaskAddress(),
                                     null,
                                     0, 
                                     this );
                        this.status = status;                        
                    }
                    break;
                    
                default:
                    throw new IllegalStateException("Unknown status.");
            }
        }
    }


    public int getStatus()
    {
        return status;
    }


    public void setBandwidth(int bandwidth)
    {
        this.bandwidth = bandwidth;
    }


    public int getBandwidth()
    {
        return bandwidth;
    }

    public IP4Address getInetAddress()
    {
        return inetAddress;
    }

    public IP4Address getBroadcastAddress()
    {
        return broadcastAddress;
    }

    public IP4Address getNetmaskAddress()
    {
        return netmaskAddress;
    }

    
    public void setBroadcastAddress(IP4Address address) 
            throws ChangeInterfacePropertyException
    {
        if( getStatus() == UP )
        {
            setStatus( DOWN );
            this.broadcastAddress = address;
            setStatus( UP );
        } else
        {
            this.broadcastAddress = address;
        }
    }


    public void setNetmaskAddress(IP4Address address) 
            throws ChangeInterfacePropertyException
    {
        if( getStatus() == UP )
        {
            setStatus( DOWN );
            this.netmaskAddress = address;
            setStatus( UP );
        } else
        {
            this.netmaskAddress = address;
        }
    }

    
    
    public void setInetAddress(IP4Address address) 
            throws ChangeInterfacePropertyException 
    {
        if( getStatus() == UP )
        {
            setStatus( DOWN );
            this.inetAddress = address;
            setStatus( UP );
        } else
        {
            this.inetAddress = address;
        }
    }
    

    public String getEncap()
    {
        return encap;
    }

    public int getRXBytes()
    {
        return rxBytes;
    }

    public int getRXDroped()
    {
        return rxDroped;
    }

    public int getRXPackets()
    {
        return rxPackets;
    }

    public int getRXPacketsErrors()
    {
        return rxPacketsErrors;
    }
    
    
    public int getTXBytes()
    {
        return txBytes;
    }

    public int getTXDroped()
    {
        return txDroped;
    }

    public int getTXPackets()
    {
        return txPackets;
    }

    public int getTXPacketsErrors()
    {
        return txPacketsErrors;
    }
    
    
    public MACAddress getMACAddress()
    {
        return macAddress;
    }

    
    
    public MACAddress resolveAddress(IP4Address ip4address)
    {
        MACAddress macaddress = arpCache.get(ip4address);
        
        if( macaddress == null )
        {
            try
            {
                makeArpRequest(ip4address);
                // Wait the response here?
                Thread.sleep(100);
            }catch(AddressException ae)
            {
                ae.printStackTrace();
            }catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
            macaddress = arpCache.get(ip4address);
        }

        return macaddress;
    }
    
    
    
    
    public void makeArpRequest(IP4Address address)
    throws AddressException
    {
        Layer2Packet arpRequestPacket = 
            new Layer2Packet( macAddress, 
                              MACAddress.BROADCAST,
                              Protocols.ARP, 
                              new ARPPacket(macAddress, address, ARPPacket.REQUEST) );
        
        transmitPacket(arpRequestPacket);
    }



    
    public void makeArpReplay(ARPPacket arpPacket)
    throws AddressException
    {
        arpPacket.setOperation(ARPPacket.REPLAY);
        arpPacket.setResolvedAddress(getMACAddress());
        
        Layer2Packet arpReplayPacket = 
            new Layer2Packet( macAddress, 
                              arpPacket.getSourceMacAddress(),
                              Protocols.ARP, 
                              arpPacket );
        
        transmitPacket(arpReplayPacket);
    }


    
    protected void processArpRequest(ARPPacket arpPacket)
    throws AddressException
    {
        if( inetAddress!=null && 
            arpPacket.getAddressToResolve().equals(inetAddress))
        {
            makeArpReplay(arpPacket);
        }
    }
    
    
    
    protected void processArpReplay(ARPPacket arpPacket)
    throws AddressException
    {
        arpCache.put (arpPacket.getAddressToResolve(), arpPacket.getResolvedAddress());
    }

    
    
    public ARPCache getArpCache()
    {
        return arpCache;
    }
    
    
    
    
    public IP4Address getNetworkAddress()
    {
        return IP4Address.evaluateNetworkAddress(inetAddress, netmaskAddress);
    }
    
    
    
    
    
    public void transmitPacket(IP4Packet packet, IP4Address destination)
    {
        MACAddress macAddress = resolveAddress(destination);
        
        if(macAddress == null)
        {
            LOGGER.info(getId()+" Can not resolv MACAddress for: "+destination);
            return;
        }
        
        Layer2Packet l2packet = null;
        try
        {
            l2packet = 
            new Layer2Packet( getMACAddress(), 
                              macAddress,
                              Protocols.IP, 
                              packet );
        }catch(AddressException ae)
        {
            ae.printStackTrace();
            return;
        }
        
        transmitPacket(l2packet);
    }
   
    
    
    public int getId()
    {
        return id;
    }
    
    public IdGenerator getIdGenerator()
    {
        return idGenerator;
    }

    public void removeTransferPacketListener(TransferPacketListener listener)
    {
        transferPacketListeners.remove(listener);
    }

    public void addTransferPacketListener(TransferPacketListener listener)
    {
        transferPacketListeners.add(listener);
    }

}

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


import java.util.*;

public class ARPCache extends TimerTask
{
    private final Map<IP4Address, ResolvedAddress> cache;
    private class ResolvedAddress
    {
        MACAddress resolvedAddress;
        long resolvedTime;
    }
    
    private int timeout;
    private final Timer timer;
    
    /** Creates a new instance of ARPCache 
     * @param timeout second
     */
    public ARPCache(int timeout)
    {
        cache = Collections.synchronizedMap(new HashMap<IP4Address, ResolvedAddress>());
        this.timeout = timeout;
        
        timer = new Timer(true);
        timer.schedule(this, timeout * 1000, timeout * 1000);
    }
    

    public void put(IP4Address key, MACAddress value)
    {
        ResolvedAddress addr = new ResolvedAddress();
        addr.resolvedAddress = value;
        addr.resolvedTime = System.currentTimeMillis();
        
        cache.put(key, addr);
    }
    
    
    public MACAddress get(IP4Address key)
    {
        ResolvedAddress resolvedAddress = cache.get(key);
        if(resolvedAddress!=null) 
        {
            return resolvedAddress.resolvedAddress;
        }else
        {
            return null; 
        }
    }

    
    /** Clean records when timeout expired
     */
    public void run()
    {
       // System.out.println("'Clean ARP cache' job's running.");

        if(cache.isEmpty()) return;
        
        long curtime = System.currentTimeMillis();
        
        for(Iterator<ResolvedAddress> i = cache.values().iterator(); i.hasNext(); )
        {
            ResolvedAddress addr = i.next();
            
           // System.out.println("curtime="+curtime+" addr.resolvedAddress"+addr.resolvedAddress+" addr.resolvedTime="+addr.resolvedTime);
            
            if((curtime - addr.resolvedTime)*1000 > timeout )
            {
                i.remove();
            }
        }
    }
    
    
    public Set<IP4Address> getAddresses()
    {
        return cache.keySet();
    }
}

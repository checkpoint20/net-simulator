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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ARPCache extends TimerTask {
    
    private static final Logger logger = Logger.getLogger( ARPCache.class.getName() );

    private final Map<IP4Address, ResolvedAddress> cache;

    private static class ResolvedAddress {
        MACAddress resolvedAddress;
        long resolvedTime;
    }

    private final int timeout;
    private final Timer timer;

    /**
     * Creates a new instance of ARPCache
     *
     * @param timeout in seconds.
     */
    public ARPCache(int timeout) {
        cache = Collections.synchronizedMap(new HashMap<IP4Address, ResolvedAddress>());
        this.timeout = timeout;

        timer = new Timer(true);
        timer.schedule(this, timeout * 1000, timeout * 1000);
    }

    public void put(IP4Address key, MACAddress value) {
        ResolvedAddress addr = new ResolvedAddress();
        addr.resolvedAddress = value;
        addr.resolvedTime = System.currentTimeMillis();

        cache.put(key, addr);
    }

    public MACAddress get(IP4Address key) {
        ResolvedAddress resolvedAddress = cache.get(key);
        if (resolvedAddress != null) {
            return resolvedAddress.resolvedAddress;
        } else {
            return null;
        }
    }

    /**
     * Clean records when timeout expired
     */
    @Override
    public void run() {
        logger.finest("Clean ARP cache job is running.");

        if (cache.isEmpty()) {
            return;
        }

        long curtime = System.currentTimeMillis();

        synchronized(cache) {
            for (IP4Address ip4Address: cache.keySet()) {               
                ResolvedAddress addr = cache.get(ip4Address);
                long offset = (curtime - addr.resolvedTime) / 1000;
                if (offset > timeout) {
                    logger.log(Level.FINEST, "{0} -> {1} time sinse has been resolved {2}, going to be removed.", new Object[]{ip4Address, addr.resolvedAddress, offset});
                    cache.remove(ip4Address);
                }
            }
        }
    }

    public Set<IP4Address> getAddresses() {
        return cache.keySet();
    }
}

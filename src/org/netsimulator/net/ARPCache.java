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

public class ARPCache {
    
    private static final Logger logger = Logger.getLogger( ARPCache.class.getName() );
    private final Map<IP4Address, ResolvedAddress> cache;
    
    private static class ResolvedAddress {
        final MACAddress resolvedAddress;
        final long resolvedTime;
        public ResolvedAddress(MACAddress resolvedAddress, long resolvedTime) {
            this.resolvedAddress = resolvedAddress;
            this.resolvedTime = resolvedTime;
        }
    }

    private final int timeout;

    /**
     * Creates a new instance of ARPCache. It is thread safe.
     *
     * @param timeout in seconds. Within this period of time since putting to the cache
     * entities will remain in the cache. If this timeout is exceeded for an entity
     * it become available for purging from the cache during {@link clean()} invocation.
     */
    public ARPCache(int timeout) {
        cache = Collections.synchronizedMap(new HashMap<IP4Address, ResolvedAddress>());
        this.timeout = timeout;
    }

    public void put(IP4Address key, MACAddress value) {
        cache.put(key, new ResolvedAddress(value, System.currentTimeMillis()));
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
     * Clean records
     */
    public void clean() {
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

    /**
     * Return a snapshot of resolved addresses in the cache.
     * @return resolved addresses.
     */    
    public List<IP4Address> getAddresses() {
        synchronized(cache) {
            return new LinkedList<IP4Address>(cache.keySet());
        }
    }
}

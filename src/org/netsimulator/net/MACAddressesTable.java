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
 * Created on February 6, 2006
 */

package org.netsimulator.net;


import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MACAddressesTable {
    private static final Logger logger = 
            Logger.getLogger("org.netsimulator.net.MACAddressesTable");
    private final Map<MACAddress, MappedPort> table;
    private final int timeout;
    private final Lock lock = new ReentrantLock();

    
    private static class MappedPort {
        final int portId;
        final long mappedTime;
        public MappedPort(int portId, long mappedTime) {
            this.portId = portId;
            this.mappedTime = mappedTime;
        }
    }
    
    
    /** Creates a new instance of MACAddressesTable
     * @param timeout seconds
     */
    public MACAddressesTable(int timeout) {
        table = new HashMap<MACAddress, MappedPort>();
        this.timeout = timeout;
    }
    

    public void put(MACAddress key, int value) {
        if(lock.tryLock()) {
            try{
                table.put(key, new MappedPort(value, System.currentTimeMillis()));
            } finally {
                lock.unlock();
            }
        }
    }
    
    
    public int get(MACAddress key) {
        int portId = -1;
        if(lock.tryLock()) {
            try {
                portId = table.get(key) == null ? -1 : table.get(key).portId;
            } finally {
                lock.unlock();
            }
        }
        return portId;
    }

    
    /** Clean records when timeout expired
     */
    public void clean() {
        if(lock.tryLock()) {        
            logger.fine("Clean MACAddress table job is running." );
            try {
                for (MACAddress mac: table.keySet()) {               
                    MappedPort port = table.get(mac);
                    long offset = (System.currentTimeMillis() - port.mappedTime) / 1000;
                    if (offset > timeout) {
                        logger.log(Level.FINEST, "{0} -> {1} time sinse has been mapped {2}, going to be removed.", new Object[]{mac, port.portId, offset});
                        table.remove(mac);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
    
    
    /**
     * Return a snapshot of mapped addresses in the table.
     * @return resolved addresses.
     */      
    public List<MACAddress> getMappedAddresses() {
        List<MACAddress> res = Collections.EMPTY_LIST;
        lock.lock();
        try {
            res = new LinkedList<MACAddress>(table.keySet());
        } finally {
            lock.unlock();
        }
        return res;
    }
    
    
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("=====================================\n");
        for( MACAddress addr : getMappedAddresses() ) {
            buf.append("MACAddr: ").append(addr).append(" Port: ").append(get(addr)).append("\n");
        }
        buf.append("=====================================\n");
        
        return buf.toString();
    }
}

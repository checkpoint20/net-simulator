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
import java.util.logging.Logger;

public class MACAddressesTable extends TimerTask
{
    private static final Logger logger = 
            Logger.getLogger("org.netsimulator.net.MACAddressesTable");
    private final Map<MACAddress, MappedPort> cache;
    private int timeout;
    private final Timer timer;
    
    private class MappedPort
    {
        int portId;
        long mappedTime;

        public String toString()
        {
            return 
                "MappedPort("+hashCode()+"){portId="+portId+
                    ",mappedTime="+mappedTime+"}";
        }
    }
    
    
    /** Creates a new instance of MACAddressesTable
     * @param timeout second
     */
    public MACAddressesTable(int timeout)
    {
        cache = new HashMap<MACAddress, MappedPort>();
        this.timeout = timeout;
        
        timer = new Timer( true );
        timer.schedule( this, timeout * 1000, timeout * 1000 );
    }
    

    public void put(MACAddress key, int value)
    {
        MappedPort port = new MappedPort();
        port.portId = value;
        port.mappedTime = System.currentTimeMillis();
        
        logger.fine( hashCode()+" The port "+cache.put(key, port)+
                " has been replaced by port "+port+
                " for address "+key+" ("+key.hashCode()+")." );
    }
    
    
    public int get(MACAddress key)
    {
        MappedPort mappedPort = cache.get(key);
        logger.fine( hashCode()+" The port "+mappedPort+" has been returned for address "+key+" ("+key.hashCode()+")." );
        return mappedPort != null ? mappedPort.portId : -1 ; 
    }

    
    /** Clean records when timeout expired
     */
    public void run()
    {
        logger.fine( hashCode()+" 'Clean MACAddress table' job's running ... " );

        if( cache.isEmpty() ) 
        { 
            logger.fine( hashCode() + " Clean up done.\n" );
            return;
        }
        
        long curtime = System.currentTimeMillis();
        
        for( Iterator<MACAddress> i = cache.keySet().iterator(); i.hasNext(); )
        {
            MACAddress addr = i.next();
            MappedPort port = cache.get( addr );
            if( (curtime - port.mappedTime) > ( timeout * 1000 ) )
            {
                i.remove();
                logger.fine( hashCode()+" Port "+port+
                        " for address "+addr+" has been removed. Curtime "+curtime+
                        ", timeout "+timeout);
            }
        }
        
        logger.fine( hashCode() + "Clean up done.\n" );
    }
    
    
    public Set<MACAddress> getMappedAddresses()
    {
        return cache.keySet();
    }
    
    
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("=====================================\n");
        for( MACAddress addr : getMappedAddresses() )
        {
            MappedPort port = cache.get(addr);
            buf.append("MACAddr: "+addr+" Port: "+port+"\n");
        }
        buf.append("=====================================\n");
        
        return buf.toString();
    }
}

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
 * RoutingTableRow.java
 *
 * Created on 20 August 2005, 1:19
 */

package org.netsimulator.net;

import java.util.Comparator;
import java.util.logging.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class RoutingTableRow
{
    public static final Comparator COMPARATOR = new RowComparator();
    
    private IP4Address target  = null; 
    private IP4Address netmask = null; 
    private IP4Address gateway = null;
    private int metric         = 0;
    private Interface iface    = null;        

    private static final Logger logger = Logger.getLogger("org.netsimulator.net.RoutingTableRow");

    
    public RoutingTableRow(
            IP4Address target, 
            IP4Address netmask, 
            IP4Address gateway,
            int metric,
            Interface iface) 
    throws NotAllowedAddressException
    {
        setTarget(target);
        setNetmask(netmask);
        setGateway(gateway);
        setMetric(metric);
        setInterface(iface);        
    }
    
    
    
    @Override
    public boolean equals(Object obj)
    {
        if( !(obj instanceof RoutingTableRow) )
        {
            logger.fine("the obj "+obj+" is not an RoutingTableRow\n");
            return false;
        }        
        if( this == obj ) 
        {
            return true;
        }
        RoutingTableRow r = (RoutingTableRow)obj;
        return new EqualsBuilder()
                     .append(target,  r.target)
                     .append(netmask, r.netmask)
                     .append(gateway, r.gateway)
                     .append(metric,  r.metric )
                     .append(iface,   r.iface)
                     .isEquals();
    }


    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(93894543, 85659999).
            append(target).
            append(netmask).
            append(gateway).
            append(metric).
            append(iface).
            toHashCode();
    }
    
    
    public IP4Address getTarget()
    {
        return target;
    }


    public IP4Address getNetmask()
    {
        return netmask;
    }

    
    public IP4Address getGateway()
    {
        return gateway;
    }

    
    public int getMetric()
    {
        return metric;
    }
    
    
    public Interface getInterface()
    {
        return iface;
    }
    
    
    private void setTarget(IP4Address target) throws NotAllowedAddressException 
    {
        if(target == null)
        {
            throw new IllegalArgumentException("The target can't be null");
        }else
        {
            CheckAddressNetmaskCorrespondence(target, getNetmask());
            this.target = target;
        }
    }
    

    
    static void CheckAddressNetmaskCorrespondence(
            IP4Address address, 
            IP4Address netmask)
    throws NotAllowedAddressException
    {
        if(address==null || netmask==null)
        {
            return;
        }
        
        if((address.toIntValue() & netmask.toIntValue()) != address.toIntValue())
        {
            throw new NotAllowedAddressException("The address "+address+
                    " not allowed with the netmask "+netmask);
        }
    }
    
    
    
    private void setNetmask(IP4Address netmask) 
        throws NotAllowedAddressException
    {
        if(netmask == null)
        {
            this.netmask = new IP4Address(0xFFFFFFFF);
        }else
        {
            CheckAddressNetmaskCorrespondence(getTarget(), netmask);
            this.netmask = netmask;
        }
    }
    

    private void setGateway(IP4Address gateway)
    {
        this.gateway = gateway;
    }
    

    private void setMetric(int metric) 
    {
        if(metric < 0)
        {
            throw new IllegalArgumentException("The metric can't be less 0");
        }else
        {
            this.metric = metric;
        }
    }
    

    private void setInterface(Interface iface)
    {
        if(target == null)
        {
            throw new IllegalArgumentException("The interface can't be null");
        }else
        {        
            this.iface = iface;
        }
    }
    

    public boolean match(IP4Address address)
    {
        return (address.toIntValue() & netmask.toIntValue()) == target.toIntValue(); 
    }
    
    
    
    @Override
    public String toString()
    {
        String status = "U";
        
        return getTarget()+"\t"+
               (getGateway()==null?"*":getGateway())+"\t"+
               getNetmask()+"\t"+
               (getGateway()==null?status:status+"G")+"\t"+
               getMetric()+"\t"+
               getInterface();
    }

    

    /**
     * This comparator is intended for control the order 
     * of <code>RoutingTableRow</code>-s in the <code>RoutingTable</code>. 
     * <p>
     * It implements the rules "the route with the longest netmask
     * has the most priority" and "if netmasks are equal the route with least metric 
     * has the most priority".
     * <p>
     * 'Most priority' means such route must be higher in routing table than route 
     * with lower priority. As soon as TreeSet iterator() returns elements in ascending order,
     * route with higher priority must be 'less than' route with lower priority.
     * <p>
     * The order this comparator provide is consistent with <code>equals</code>.
     * @see RoutingTable
     * @see TreeSet
     */
    public static class RowComparator implements Comparator<RoutingTableRow>
    {
        public static final int LT = -1;
        public static final int EQ = 0;
        public static final int GT = 1;
        public static final int ERR = 2;
        
        
        public int compare(RoutingTableRow r1, RoutingTableRow r2)
        {
            logger.fine( hashCode()+": compare rows:\n"+
                    r1+" - " +r1.hashCode()+ "\n"+
                    r2+" - " +r2.hashCode()  );
            int res = ERR;
            if( r1.equals( r2 ) ) 
            {
                res = EQ;
            } else
            {
                if( r1.getNetmask().equals(r2.getNetmask()) ) 
                {
                    if(r1.getMetric() <= r2.getMetric())
                    {
                        res = LT; // r1 has higher priority than r2
                    }else
                    {
                        res = GT; // r1 has lower priority than r2
                    }    
                }
                else if( countNetmaskLength(r1.getNetmask()) >
                         countNetmaskLength(r2.getNetmask()) )
                {
                    res = LT; // r1 has higher priority than r2
                }
                else
                {
                    res = GT; // r1 has lower priority than r2
                }
            }
            
            if(res == ERR) {
                throw new IllegalStateException("Internall error in comparator.");
            }

            if(logger.isLoggable(Level.FINE)) {
                logger.log( Level.FINE, hashCode() + ": res: " + convertResToString(res));
            }
            
            return res;
        }

        private String convertResToString(int res) {
            String str = "ERR";
            switch(res) {
                case EQ: 
                    str = "EQ";
                    break;
                case LT: 
                    str = "LT";
                    break;
                case GT: 
                    str = "GT";
                    break;
            }
            return str;
        }



        private int countNetmaskLength(IP4Address netmask)
        {
            return Integer.bitCount(netmask.toIntValue());

            /*
            int count = 0;
            int addr = netmask.toIntValue();

            for(count=0; count!=32; count++)
            {
               // System.out.println("buf digit : "+Integer.toBinaryString(addr));

                if( (addr | 0) == 0 )
                {
                    break;
                }else
                {
                    addr <<= 1;
                }
            }

            return count;
             */
        }

    }

}
    

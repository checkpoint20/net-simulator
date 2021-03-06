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

package org.netsimulator.term;

import org.apache.commons.cli.*;
import org.netsimulator.net.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;



public class ArpCLICommand extends AbstractCommand
{
    private static final Logger logger = 
            Logger.getLogger(ArpCLICommand.class.getName());    
    
    private Writer writer;
    private static final Options options = new Options();
    private final IP4Router router;

    public ArpCLICommand(IP4Router router)
    {
        this.router = router;
        
        Option help = new Option("h", false, "display this help");
        Option resolv = OptionBuilder.hasArgs(2)
                                     .withDescription("resolv MAC-address for the IP-address "+
                                                      "on the subnet connected to the inerface")
                                     .create("r");
        options.addOption(help);
        options.addOption(resolv);
    }


    public String getName()
    {
        return "arp";
    }

    
    public int go() throws IOException
    {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse( options, argv);
        }catch(MissingArgumentException mae)
        {
            writer.write("Error: Missing arguments\n");
            return -1;
        }catch(ParseException pe)
        {
            logger.log(Level.SEVERE, "Unexpected exception.", pe);
            return -1;
        }
 
        if(cmd.hasOption("h"))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    new PrintWriter(writer),
                    80,
                    "arp [-h] [-r <IP-address> <interface>]",
                    "Shows ARP-table. If -r option is given makes ARP-request for given IP-address befor showing ARP-table.",
                    options,
                    3,
                    2,
                    null,
                    false);
            
            return 0;
        }

        
        if(cmd.hasOption("r"))
        {
            String buf[] = cmd.getOptionValues("r");
            
            if(buf.length!=2)
            {
                writer.write("Error: Missing arguments\n");
                return -1;
            }else
            {
                //writer.write("buf[0]="+buf[0]+" buf[1]="+buf[1]+"\n");
                
                Interface ifs = router.getInterface(buf[1]);
                if( ifs==null )
                {
                    writer.write("Error: Error: No such interface\n");
                    return -1;
                }
                
                if(ifs instanceof EthernetInterface)
                {
                    EthernetInterface eth = (EthernetInterface)ifs;
                    try
                    {
                        eth.makeArpRequest(new IP4Address(buf[0]));
                    }catch(AddressException ae)
                    {
                        writer.write("Error: Invalid IP address\n");
                        return -1;
                    }
                }else
                {
                    writer.write("Error: ARP not implemented on the network\n");
                    return -1;
                }
            }
        }
       
        printArpCache();
        
        return 0;
    }

    
    @Override
    public void setOutputWriter(Writer writer)
    {
        this.writer = writer;
    }
    
    
    private void printArpCache() throws IOException
    {
        writer.write("Address \tHWaddress \tiface\n");
        Interface interfaces[] = router.getInterfaces();
        for(int i=0; i!=interfaces.length; i++)
        {
            if(interfaces[i] instanceof EthernetInterface)
            {
                EthernetInterface eth = (EthernetInterface)interfaces[i];
                for (IP4Address key : eth.getArpCache().getAddresses()) {
                    MACAddress value = eth.getArpCache().get(key);
                    writer.write(key+"\t"+value+"\t"+eth.getName()+"\n");
                }
            }           
        }
    }

    @Override
    public void stop()
    {
    }
    
    @Override
    public void run() {
        try {
            go();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unexpected exception.", ex);
        } finally {
            fireExecutionCompleted(0);
}
    }        
}

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


import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.*;
import org.apache.commons.cli.*;
import org.netsimulator.net.AddressException;
import org.netsimulator.net.ChangeInterfacePropertyException;
import org.netsimulator.net.EthernetInterface;
import org.netsimulator.net.IP4Address;
import org.netsimulator.net.IP4EnabledInterface;
import org.netsimulator.net.IP4Router;
import org.netsimulator.net.Interface;


public class IfconfigCLICommand implements CLICommand
{
    private Writer writer;
    private IP4Router router;
    private static final Options options = new Options();

    private static final Logger LOGGER = 
            Logger.getLogger(IfconfigCLICommand.class.getName());

    
    public IfconfigCLICommand(IP4Router router)
    {
        this.router = router;

        Option help = new Option("h", false, "display this help");
        options.addOption(help);
        Option all = new Option("a", false, "display the status of all interfaces, even those that are down");
        options.addOption(all);
        Option statusUp = new Option("up", false, "this flag causes the interface to be activated");
        Option statusDown = new Option("down", false, "this flag causes the driver for this interface to be shut down");

        /* OptionGroup saved the state of selected option from parsing to parsing.
           There is not a way to clean the state. */
//        OptionGroup status = new OptionGroup();
//        status.addOption(statusUp);
//        status.addOption(statusDown);
//        options.addOptionGroup(status);
        options.addOption(statusUp);
        options.addOption(statusDown);
        
        Option broadcast = OptionBuilder.withArgName( "address" )
                                        .hasArg()
                                        .withDescription( "set the protocol broadcast address for this interface" )
                                        .create( "broadcast" );
        options.addOption(broadcast);
        Option netmask   = OptionBuilder.withArgName( "address" )
                                        .hasArg()
                                        .withDescription( "set the IP network mask for this interface (this value defaults to the usual class A, B or C network mask)" )
                                        .create( "netmask" );
        options.addOption(netmask);
     }


    public String getName()
    {
        return "ifconfig";
    }

    
    public int Go(String argv[], String cl)
    throws IOException    
    {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse( options, argv);
        }catch(MissingArgumentException mae)
        {
            writer.write("Error: Missing arguments\n");
            return -1;
        }catch(UnrecognizedOptionException uoe)
        {
            writer.write("Error: "+uoe.getMessage()+"\n");
            return -1;
        }catch(ParseException pe)
        {
            pe.printStackTrace();
            return -1;
        }
 
        if(cmd.hasOption("h"))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    new PrintWriter(writer),
                    80,
                    "ifconfig [-h] [-a] [<interface>] [<address>] [-broadcast <address>] [-netmask <address>] [-up|-down]",
                    "Configure a network interface. If no arguments are given, "+
                    "ifconfig displays the status of the currently active interfaces. "+
                    "If the only <interface> argument is given, ifconfig displays the status of the interface.",
                    options,
                    3,
                    2,
                    null,
                    false);
            
            return 0;
        }


        String args[] = cmd.getArgs();
        
        Interface curInterface = null;
        IP4Address address = null;
        IP4Address broadcast = null;
        IP4Address netmask = null;
        int updown = Interface.UNKNOWN;

        if(args.length > 0)
        {
            curInterface=router.getInterface(args[0]);
            
            if(curInterface == null)
            {
                writer.write("Error: No such interface\n");
                return -1;
            }
        }    
            
        if(args.length == 2)
        {
            try
            {
                address = new IP4Address(args[1]);
            }catch(AddressException ae)
            {
                ae.printStackTrace();
                writer.write("Error: Invalid address\n");
                return -1;
            }
        }    
            
            

        if(cmd.hasOption("broadcast"))
        {
            try
            {
                broadcast  = new IP4Address(cmd.getOptionValue("broadcast"));
            }catch(AddressException ae)
            {
                LOGGER.log(Level.FINE, "Invalid broadcast address.", ae);
                writer.write("Error: Invalid broadcast address\n");
                return -1;
            }
            if( address.equals(broadcast) ) 
            {
                writer.write("Error: Inet address can not be equal broadcast one.\n");
                return -1;                
            }
        }        


        
        if(cmd.hasOption("netmask"))
        {
            try
            {
                netmask  = new IP4Address(cmd.getOptionValue("netmask"));
            }catch(AddressException ae)
            {
                LOGGER.log(Level.FINE, "Invalid netmask address.", ae);
                writer.write("Error: Invalid netmask address\n");
                return -1;
            }

            if(!IP4Address.isNetmaskAddressValid(netmask)) {
                writer.write("Error: Invalid netmask address\n");
                return -1;
            }
            
            if( address.equals(netmask) ) 
            {
                writer.write("Error: Inet address cannot be equal network one.\n");
                return -1;
            }
        }        

        
        
        if(cmd.hasOption("up") && cmd.hasOption("down"))
        {
            writer.write("Error: '-up' and '-down' are mutually exclusive options\n");
            return -1;
        }else
        {
            if(cmd.hasOption("up"))
            {
                updown = Interface.UP;
            }
            if(cmd.hasOption("down"))
            {
                updown = Interface.DOWN;
            }
        }
        

        
        
        
        if( curInterface != null && ( address != null || netmask != null || 
                broadcast != null || updown != Interface.UNKNOWN ) ) 
        {
            // требуется выполнить какое-то действие с конфигурацией 
            // заданного интерфейса
        
            try
            {
                // всегда останавливаем предварительно интерфейс
                int oldStatusValue = curInterface.getStatus();
                curInterface.setStatus( Interface.DOWN );

                IP4EnabledInterface iface = null;
                if(curInterface instanceof IP4EnabledInterface)
                {
                    iface = (IP4EnabledInterface)curInterface;
                }else
                {
                    writer.write("Error: the interface '"+curInterface+" is not IP4 enabled\n");
                    return -1;
                }

                if( address != null ) { iface.setInetAddress(address); }
                if( netmask != null ) { iface.setNetmaskAddress(netmask); }
                if( broadcast != null ) { iface.setBroadcastAddress(broadcast); }

                // всегда востанавливаем состояние интерфейса после изменения конфигурации
                curInterface.setStatus( oldStatusValue );

                if( updown != Interface.UNKNOWN ) { curInterface.setStatus( updown ); }
                
            } catch ( ChangeInterfacePropertyException e )
            {
                writer.write("Error: "+e.getMessage()+"\n");
                return -1;
            }
            
        } else 
        {
            // менять конфигурацию не требуется, печатаем инфо
        
            if(curInterface!=null && 
               address==null && updown==Interface.UNKNOWN)
            {
                showStatus(curInterface, cmd.hasOption("a"));
            }

            if(curInterface==null)
            {
                Interface interfaces[] = router.getInterfaces();
                for(int i=0; i!=interfaces.length; i++)
                {
                    showStatus(interfaces[i], cmd.hasOption("a"));
                }
            }
        }
        

        //System.out.println("*** end of ifconfig");

        return 1;    
    }

    
    
    public void setOutputWriter(Writer writer)
    {
        this.writer = writer;
    }
    

    
    
    private void showStatus(Interface curInterface, boolean hasOptionAll)
    throws IOException    
    {
        if( curInterface.getStatus()==Interface.DOWN &&
            !hasOptionAll)
        { 
            return; 
        }


        if(curInterface instanceof EthernetInterface)
        {
            EthernetInterface eth = (EthernetInterface)curInterface;

            String text = 
            eth.getName()+"\tLink encap:"+eth.getEncap()+"\n";
            if(eth.getInetAddress()!=null)
            {
                text += "\tinet addr:"+eth.getInetAddress()+"  Bcast:"+
                        eth.getBroadcastAddress()+"  Mask:"+eth.getNetmaskAddress()+"\n";
            }

            String statusStr = "ERROR!";
            if(eth.getStatus()==Interface.UP){ statusStr="UP"; }
            if(eth.getStatus()==Interface.DOWN){ statusStr="DOWN"; }
            text += "\t"+statusStr+"\n";

            text += "\tRX packets:"+eth.getRXPackets()+" errors:"+
                    eth.getRXPacketsErrors()+" dropped:"+eth.getRXDroped()+"\n";
            text += "\tTX packets:"+eth.getTXPackets()+" errors:"+
                    eth.getTXPacketsErrors()+" dropped:"+eth.getTXDroped()+"\n";

            text += "\tRX bytes:"+eth.getRXBytes()+" TX bytes:"+eth.getTXBytes()+"\n";
            text += "\n";

            writer.write(text);
            LOGGER.log(Level.FINE, "router id {0}:\n{1}", new Object[]{router.getId(), text});
        }
    }
    
    

    public void Stop()
    {
    }
}

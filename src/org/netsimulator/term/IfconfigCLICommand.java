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


public class IfconfigCLICommand extends AbstractCommand
{
    public static final int ERROR_RETCODE = -1;
    public static final int MISSING_ARGUMENT_RETCODE = -2;
    public static final int UNRECOGNIZED_OPTION_RETCODE = -3;
    public static final int PARSE_EXCEPTION_RETCODE = -4;
    public static final int NO_SUCH_INTERFACE_RETCODE = -5;
    public static final int INVALID_ADDRESS_RETCODE = -6;
    public static final int INVALID_BROADCAST_RETCODE = -7;
    public static final int ADDRESS_CAN_NOT_BE_EQUAL_BROADCAST_RETCODE = -8;
    public static final int INVALID_NETMASK_RETCODE = -9;
    public static final int ADDRESS_CANNOT_BE_EQUAL_NETWORK_RETCODE = -10;
    public static final int UP_AND_DOWN_ARE_MUTUALLY_EXCLUSIVE_RETCODE = -11;
    public static final int INTERFACE_IS_NOT_IP4_ENABLED_RETCODE = -12;              
    public static final int CHANGE_INTERFACE_PROPERTY_EXCEPTION_RETCODE = -13;                
    public static final int INET_ADDRESS_NOT_SPECIFIED_RETCODE = -14;                
    public static final int OK_RETCODE = 0;
            
    private Writer writer;
    private final IP4Router router;
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


    @Override
    public String getName()
    {
        return "ifconfig";
    }

    
    public int go() throws IOException    
    {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse( options, argv);
        }catch(MissingArgumentException mae)
        {
            writer.write("Error: Missing arguments\n");
            return MISSING_ARGUMENT_RETCODE;
        }catch(UnrecognizedOptionException uoe)
        {
            writer.write("Error: "+uoe.getMessage()+"\n");
            return UNRECOGNIZED_OPTION_RETCODE;
        }catch(ParseException pe)
        {
            LOGGER.log(Level.SEVERE, "Unexpected exception.", pe);
            return PARSE_EXCEPTION_RETCODE;
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
            
            return OK_RETCODE;
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
                return NO_SUCH_INTERFACE_RETCODE;
            }
        }    
            
        if(args.length == 2)
        {
            try
            {
                address = new IP4Address(args[1]);
            }catch(AddressException ae)
            {
                LOGGER.log(Level.SEVERE, "Unexpected exception.", ae);
                writer.write("Error: Invalid address\n");
                return INVALID_ADDRESS_RETCODE;
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
                return INVALID_BROADCAST_RETCODE;
            }
            if( address.equals(broadcast) ) 
            {
                writer.write("Error: Inet address can not be equal broadcast one.\n");
                return ADDRESS_CAN_NOT_BE_EQUAL_BROADCAST_RETCODE;                
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
                return INVALID_NETMASK_RETCODE;
            }

            if(!IP4Address.isNetmaskAddressValid(netmask)) {
                writer.write("Error: Invalid netmask address\n");
                return INVALID_NETMASK_RETCODE;
            }
            
            IP4Address addressToEvaluateNetmask = 
                    address != null ? address : ((IP4EnabledInterface)curInterface).getInetAddress();
            if(addressToEvaluateNetmask == null) 
            {
                writer.write("Error: Inet address not specified\n");
                return INET_ADDRESS_NOT_SPECIFIED_RETCODE;
            } 
            else if( address.equals(IP4Address.evaluateNetworkAddress(address, netmask)) ) 
            {
                writer.write("Error: Inet address cannot be equal network one.\n");
                return ADDRESS_CANNOT_BE_EQUAL_NETWORK_RETCODE;
            }
        }        

        
        
        if(cmd.hasOption("up") && cmd.hasOption("down"))
        {
            writer.write("Error: '-up' and '-down' are mutually exclusive options\n");
            return UP_AND_DOWN_ARE_MUTUALLY_EXCLUSIVE_RETCODE;
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
                    return INTERFACE_IS_NOT_IP4_ENABLED_RETCODE;
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
                return CHANGE_INTERFACE_PROPERTY_EXCEPTION_RETCODE;
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

        return OK_RETCODE;    
    }

    
    
    @Override
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
    
    

    @Override
    public void stop()
    {
    }
    
    @Override
    public void run() {
        try {
            go();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected exception.", ex);
        } finally {
            fireExecutionCompleted(0);
        }
    }    
}

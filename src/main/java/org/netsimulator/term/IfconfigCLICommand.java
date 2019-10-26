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
    public static final int NETMASK_ADDRESS_NOT_SPECIFIED_RETCODE = -15;                
    public static final int BROADCAST_ADDRESS_NOT_SPECIFIED_RETCODE = -16;                
    public static final int OK_RETCODE = 0;
            
    private Writer writer;
    private final IP4Router router;
    private static final Options options = new Options();

    private static final Logger LOGGER = 
            Logger.getLogger(IfconfigCLICommand.class.getName());

    
    public IfconfigCLICommand(IP4Router router) {
        this.router = router;

        Option help = new Option("h", false, "display this help");
        options.addOption(help);
        Option all = new Option("a", false, "display the status of all interfaces, even those that are down");
        options.addOption(all);
        Option statusUp = new Option("up", false, "this flag causes the interface to be activated");
        Option statusDown = new Option("down", false, "this flag causes the driver for this interface to be shut down");
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

    
    protected int go() throws IOException    
    {
        int retCode = OK_RETCODE;
        try {
            CommandLineParser parser = new GnuParser();
            CommandLine cmd = null;
            try {
                cmd = parser.parse( options, argv);
            }catch(MissingArgumentException mae) {
                throw new IfconfigException("Error: Missing arguments", MISSING_ARGUMENT_RETCODE);
            }catch(UnrecognizedOptionException uoe) {
                throw new IfconfigException("Error: "+uoe.getMessage(), UNRECOGNIZED_OPTION_RETCODE);
            }catch(ParseException pe) {
                LOGGER.log(Level.SEVERE, "Unexpected exception", pe);
                throw new IfconfigException("Unexpected exception", PARSE_EXCEPTION_RETCODE);
            }

            if(cmd.hasOption("h")) {
                // Print help.
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(
                        new PrintWriter(writer),
                        80,
                        "ifconfig [-h] [-a] <interface> [<address>] [-broadcast <address>] [-netmask <address>] [-up|-down]",
                        "Configure a network interface. If no arguments are given, "+
                        "ifconfig displays the status of the currently active interfaces. "+
                        "If the only <interface> argument is given, ifconfig displays the status of the interface.",
                        options,
                        3,
                        2,
                        null,
                        false);
            } else {

                // Parameters analysis.
                
                String args[] = cmd.getArgs();

                IP4EnabledInterface curInterface = null;
                IP4Address address = null;
                IP4Address broadcast = null;
                IP4Address netmask = null;
                int updown = Interface.UNKNOWN;
                boolean netmaskRecalcNeeded = false;
                boolean broadcastRecalcNeeded = false;

                if(args.length >= 1) {
                    Interface ifs = router.getInterface(args[0]);
                    if(ifs == null) {
                        throw new IfconfigException("Error: No such interface: " + args[0], NO_SUCH_INTERFACE_RETCODE);
                    } else if (ifs instanceof IP4EnabledInterface) {
                        curInterface = (IP4EnabledInterface) ifs;
                    } else {
                        throw new IfconfigException("Error: the interface '" + ifs.getName() + " is not IP4 enabled", INTERFACE_IS_NOT_IP4_ENABLED_RETCODE);
                    }
                }    

                if (args.length >= 2) {
                    try {
                        address = new IP4Address(args[1]);
                        netmaskRecalcNeeded = true;
                        broadcastRecalcNeeded = true;
                    } catch (AddressException ae) {
                        throw new IfconfigException("Error: Invalid address: " + args[1], INVALID_ADDRESS_RETCODE);
                    }
                }

                if (cmd.hasOption("broadcast")) {
                    try {
                        broadcast = new IP4Address(cmd.getOptionValue("broadcast"));
                    } catch (AddressException ae) {
                        throw new IfconfigException("Error: Invalid broadcast address: " + cmd.getOptionValue("broadcast"), INVALID_BROADCAST_RETCODE);
                    }
                }

                if (cmd.hasOption("netmask")) {
                    try {
                        netmask = new IP4Address(cmd.getOptionValue("netmask"));
                        broadcastRecalcNeeded = true;
                    } catch (AddressException ae) {
                        throw new IfconfigException("Error: Invalid netmask address: " + cmd.getOptionValue("netmask"), INVALID_NETMASK_RETCODE);
                    }

                    if (!IP4Address.isNetmaskAddressValid(netmask)) {
                        throw new IfconfigException("Error: Invalid netmask address: " + cmd.getOptionValue("netmask"), INVALID_NETMASK_RETCODE);
                    }
                }

                if (cmd.hasOption("up") && cmd.hasOption("down")) {
                    throw new IfconfigException("Error: '-up' and '-down' are mutually exclusive options", UP_AND_DOWN_ARE_MUTUALLY_EXCLUSIVE_RETCODE);
                } else {
                    if (cmd.hasOption("up")) {
                        updown = Interface.UP;
                    } else if (cmd.hasOption("down")) {
                        updown = Interface.DOWN;
                    } else {
                        updown = Interface.UNKNOWN;
                    }
                }
    
                if (curInterface == null) {
                    // Print statuses of all interfaces.
                    for (Interface ifs : router.getInterfaces()) {
                        showStatus(ifs, cmd.hasOption("a"));
                    }                    
                } else if ( 
                        address == null 
                     && broadcast == null   
                     && netmask == null   
                     && updown == Interface.UNKNOWN) {
                    // Show interface status.
                    showStatus(curInterface, cmd.hasOption("a"));
                } else {

                    if(address == null) {
                        address = curInterface.getInetAddress();
                        if(address == null) {
                            throw new IfconfigException("Error: address not specified", INET_ADDRESS_NOT_SPECIFIED_RETCODE);
                        }
                    }
    
                    if(netmask == null) {
                        if(netmaskRecalcNeeded) {
                            netmask = IP4Address.evaluateNetmaskAddress(address);
                        } else {
                            netmask = curInterface.getNetmaskAddress();
                        }
                        if(netmask == null) {
                            // Failed to evaluate netmask
                            throw new IfconfigException("Error: netmask not specified", NETMASK_ADDRESS_NOT_SPECIFIED_RETCODE);
                        }
                    } 
                    
                    if(netmask.equals(address) ) {
                        throw new IfconfigException("Error: Inet address cannot be equal network one.", ADDRESS_CANNOT_BE_EQUAL_NETWORK_RETCODE);
                    } 
                    
                    if(broadcast == null) {
                        if(broadcastRecalcNeeded) {
                            broadcast = IP4Address.evaluateBroadcastAddress(address, netmask);
                        } else {
                            broadcast = curInterface.getBroadcastAddress();
                        }
                        if(broadcast == null) {
                            // Failed to evaluate broadcast
                            throw new IfconfigException("Error: broadcast not specified", BROADCAST_ADDRESS_NOT_SPECIFIED_RETCODE);
                        }
                    } 
                    
                    if (broadcast.equals(address)) {
                        throw new IfconfigException("Error: Inet address can not be equal broadcast one.", ADDRESS_CAN_NOT_BE_EQUAL_BROADCAST_RETCODE);
                    }            
                    
                    if( address.equals(IP4Address.evaluateNetworkAddress(address, netmask)) ) {
                        throw new IfconfigException("Error: Inet address cannot be equal network one.", ADDRESS_CANNOT_BE_EQUAL_NETWORK_RETCODE );
                    }
                    
                    // Apply the changes in the configuration.
                    try {
                        // Stop the interface
                        int oldStatusValue = curInterface.getStatus();
                        curInterface.setStatus( Interface.DOWN );

                        curInterface.setInetAddress(address);
                        curInterface.setNetmaskAddress(netmask);
                        curInterface.setBroadcastAddress(broadcast);

                        curInterface.setStatus( updown == Interface.UNKNOWN ? oldStatusValue : updown);

                    } catch ( ChangeInterfacePropertyException e ) {
                        LOGGER.log(Level.SEVERE, "Unexpected exception.", e);
                        throw new IfconfigException("Error: " + e.getMessage(), CHANGE_INTERFACE_PROPERTY_EXCEPTION_RETCODE);
                    }
                }
            }

        } catch (IfconfigException ifce) {
            writer.write(ifce.getMessage() + "\n");
            retCode = ifce.getErrorCode();            
        }
        return retCode;    
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
            fireExecutionCompleted(go());
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Unexpected exception.", ex);
            fireExecutionCompleted(ERROR_RETCODE);
        }
    }     
    
    
    public static class IfconfigException extends Exception {
        private int errorCode = OK_RETCODE;

        public IfconfigException() {
        }

        public IfconfigException(int errorCode) {
            this.errorCode = errorCode;
        }

        public IfconfigException(String message, int errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public IfconfigException(String message, Throwable cause, int errorCode) {
            super(message, cause);
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }
}

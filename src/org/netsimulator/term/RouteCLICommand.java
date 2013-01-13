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


import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.apache.commons.cli.*;
import org.netsimulator.net.AddressException;
import org.netsimulator.net.IP4Address;
import org.netsimulator.net.IP4EnabledInterface;
import org.netsimulator.net.IP4Router;
import org.netsimulator.net.Interface;
import org.netsimulator.net.RoutingTable;
import org.netsimulator.net.RoutingTableRow;


public class RouteCLICommand implements CLICommand
{
    private Terminal term;
    private PrintWriter writer;
    private IP4Router router;
    private static final Options options = new Options();

    private static final Logger logger = 
            Logger.getLogger("org.netsimulator.term.RouteCLICommand");
    
    
    public RouteCLICommand(Terminal term, IP4Router router)
    {
        this.term = term;
        this.router = router;

        Option help = new Option("h", false, "display this help");
        options.addOption(help);
        Option add = new Option("add", false, "add a new route");
        Option del = new Option("del", false, "delete a route");
        options.addOption(add);
        options.addOption(del);
        
        Option gw = OptionBuilder.withArgName( "address" )
                                        .hasArg()
                                        .withDescription( "route packets via a gateway" )
                                        .create( "gw" );
        options.addOption(gw);
        
        Option netmask   = OptionBuilder.withArgName( "address" )
                                        .hasArg()
                                        .withDescription( "when adding a network route, the netmask to be used" )
                                        .create( "netmask" );
        options.addOption(netmask);
        
        Option metric   = OptionBuilder.withArgName( "M" )
                                        .hasArg()
                                        .withDescription( "set the metric field in the routing table to M" )
                                        .create( "metric" );
        options.addOption(metric);
        
        Option dev   = OptionBuilder.withArgName( "If" )
                                        .hasArg()
                                        .withDescription( "force the route to be associated with the specified device" )
                                        .create( "dev" );
        options.addOption(dev);
        
     }


    public String getName()
    {
        return "route";
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
                    writer,
                    80,
                    "route [-h] [{-add|-del} <target> [-netmask <address>] [-gw <address>] [-metric <M>] [-dev <If>]]",
                    "Show/manipulate the IP routing table. If no arguments are given, "+
                    "route displays the current contents of the routing table. "+
                    "When the add or del options are used, route modifies the routing table.",
                    options,
                    3,
                    2,
                    null,
                    false);
            
            return 0;
        }

        String args[] = cmd.getArgs();
        String command = null;
        Interface curInterface = null;
        IP4Address target = null;
        IP4Address gw = null;
        IP4Address netmask = null;
        int metric = -1;
        
        if(cmd.hasOption("del") && cmd.hasOption("add"))
        {
            writer.write("Error: '-add' and '-del' are mutually exclusive options\n");
            return -1;
        }else
        {
            if(cmd.hasOption("add"))
            {
                command="add";
            }
            if(cmd.hasOption("del"))
            {
                command="del";
            }
        }
        
        
        if(command!=null)
        {
            if(args.length < 1)
            {
                writer.write("Error: Invalid target\n");
                return -1;
            }else
            {
                try
                {
                    target = new IP4Address(args[0]);
                }catch(AddressException ae)
                {
                    ae.printStackTrace();
                    writer.write("Error: Invalid address\n");
                    return -1;
                }
            }
            //writer.write("target: "+target+"\n");
            

            if(cmd.hasOption("netmask"))
            {
                try
                {
                    netmask  = new IP4Address(cmd.getOptionValue("netmask"));
                }catch(AddressException ae)
                {
                    ae.printStackTrace();
                    writer.write("Error: Invalid netmask address\n");
                    return -1;
                }
            }else
            {
                netmask = new IP4Address(0xFFFFFFFF);
            }

            
            
            if(cmd.hasOption("gw"))
            {
                try
                {
                    gw  = new IP4Address(cmd.getOptionValue("gw"));
                }catch(AddressException ae)
                {
                    ae.printStackTrace();
                    writer.write("Error: Invalid gateway address\n");
                    return -1;
                }
            }        
            

            if(cmd.hasOption("metric"))
            {
                try
                {
                    metric  = Integer.parseInt(cmd.getOptionValue("metric"));
                }catch(NumberFormatException nfe)
                {
                    nfe.printStackTrace();
                    writer.write("Error: Invalid metric\n");
                    return -1;
                }
            }        
        
            
            if(cmd.hasOption("dev"))
            {
                curInterface=router.getInterface(cmd.getOptionValue("dev"));

                if(curInterface == null)
                {
                    writer.write("Error: No such interface\n");
                    return -1;
                }
            }
            
            logger.fine("\n"+
                "command:   "+command+"\n"+
                "target:    "+target+"\n"+
                "netmask:   "+netmask+"\n"+
                "gateway:   "+gw+"\n"+
                "metric:    "+metric+"\n"+
                "interface: "+curInterface);


            
            
            
            if(command.equals("add") && gw==null)
            {
                if(curInterface==null)
                {
                    writer.write("Error: device not specified\n");
                    return -1;
                }
                
                if( curInterface instanceof IP4EnabledInterface )
                {
                    RoutingTable rt = router.getRoutingTable();
                    try
                    {
                        rt.addRoute( target, 
                                     netmask,
                                     null,
                                     metric==-1 ? 0 : metric, 
                                     curInterface );
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                        writer.write("Error: "+e.getMessage()+"\n");
                        return -1;
                    }
                }else
                {
                    writer.write("Error: the interface "+curInterface+" is not IP4 enabled\n");
                    logger.fine(hashCode()+": the interface "+curInterface+" is not IP4 enabled");
                    return -1;
                }
                
                return 0;
            }

            
            
            
            if(command.equals("add") && gw!=null)
            {
                if(curInterface==null)
                {
                    // searching throughout straight connected networks here
                    RoutingTableRow rtr = router.getRoutingTable().
                            routeThroghoutStraightConnectedNetworks(gw);
                    
                    if(rtr==null)
                    {
                        writer.write("Error: network is unreachable\n");
                        return -1;
                    }else
                    {
                        curInterface = rtr.getInterface();
                    }
                    
                    if(curInterface==null)
                    {
                        writer.write("Error: network is unreachable\n");
                        logger.warning(hashCode()+": Internal error! Something wrong with routing table, the interface in the row is null");
                        return -1;
                    }
                }    
                    
                if( curInterface instanceof IP4EnabledInterface )
                {
                    try
                    {
                        router.getRoutingTable().addRoute( target, 
                                     netmask,
                                     gw,
                                     metric==-1 ? 0 : metric, 
                                     curInterface );
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                        writer.write("Error: "+e.getMessage()+"\n");
                        return -1;
                    }
                }else
                {
                    writer.write("Error: the interface "+curInterface+" is not IP4 enabled\n");
                    logger.fine(hashCode()+": the interface "+curInterface+" is not IP4 enabled");
                    return -1;
                }
                
                return 0;
            }            
            
            
            
            
            if(command.equals("del"))
            {
                int deletedRowsCount = 0;
                
                if(curInterface==null)
                {
                    writer.write("Error: device not specified\n");
                    return -1;
                }
                
                if( curInterface instanceof IP4EnabledInterface )
                {
                    RoutingTable rt = router.getRoutingTable();
                    IP4EnabledInterface iface = (IP4EnabledInterface)curInterface;
                    try
                    {
                        if(metric==-1)
                        {
                            deletedRowsCount = rt.deleteRoute( 
                                        target, 
                                        netmask,
                                        gw,
                                        curInterface );
                        }else
                        {
                            deletedRowsCount = rt.deleteRoute( 
                                        target, 
                                        netmask,
                                        gw,
                                        metric, 
                                        curInterface );
                        }
                    }catch(Exception e)
                    {
                        e.printStackTrace();
                        writer.write("Error: "+e.getMessage()+"\n");
                        return -1;
                    }
                }else
                {
                    writer.write("Error: the interface "+curInterface+" is not IP4 enabled\n");
                    logger.warning(hashCode()+": the interface "+curInterface+" is not IP4 enabled");
                    return -1;
                }
                
                writer.write(deletedRowsCount+" rows were deleted\n");
                return 0;
            }
            
            
        }
        
        
        showRoutingTable();
        return 0;    
    }

    
    
    
    
    
    
    public void setOutputWriter(Writer writer)
    {
        this.writer = new PrintWriter(writer);
    }
    
    
    
    
    
    private void showRoutingTable()
    throws IOException    
    {
        StringWriter stringWriter = new StringWriter();
        
        stringWriter.write("IP routing table\n"+
                "Destination     Gateway         Netmask         Flags Metric Iface\n");
        
        Collection<RoutingTableRow> rows = router.getRoutingTable().getRows();
        synchronized( rows ) 
        {
            for( Iterator<RoutingTableRow> i = rows.iterator(); i.hasNext(); )
            {
                RoutingTableRow row = i.next();

                String status = "U";
                if(row.getGateway()!=null){ status+="G";}
                if(row.getNetmask().toString().equals("255.255.255.255")){ status+="H";}

                new PrintWriter(stringWriter).printf(
                        "%1$-15s %2$-15s %3$-15s %4$-5s %5$-6s %6$-5s\n", 
                        row.getTarget(),
                        row.getGateway()==null?"*":row.getGateway(),
                        row.getNetmask(),
                        status,
                        row.getMetric(),
                        row.getInterface()
                );
            }
        }

        writer.write(stringWriter.toString());
        logger.info("router id "+router.getId()+":\n"+stringWriter.toString());
    }

    
    
    public void Stop()
    {
    }
}

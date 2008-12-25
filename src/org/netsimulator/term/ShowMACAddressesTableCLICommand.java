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

import java.io.Writer;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;
import org.apache.commons.cli.*;
import org.netsimulator.net.MACAddress;
import org.netsimulator.net.MACAddressesTable;
import org.netsimulator.net.Switch;



public class ShowMACAddressesTableCLICommand implements CLICommand
{
    private Terminal term;
    private Writer writer;
    private static final Options options = new Options();
    private Switch _switch_;

    public ShowMACAddressesTableCLICommand(Terminal term, Switch _switch_)
    {
        this.term = term;
        this._switch_ = _switch_;
        
        Option help = new Option("h", false, "display this help");
        options.addOption(help);
    }


    public String getName()
    {
        return "mactable";
    }

    
    public int Go(String argv[], String cl)
    throws IOException
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
            pe.printStackTrace();
            return -1;
        }
 
        if(cmd.hasOption("h"))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    new PrintWriter(writer),
                    80,
                    "mactable[-h]",
                    "Shows the MACAddresses table.",
                    options,
                    3,
                    2,
                    null,
                    false);
            
            return 0;
        }

        printTable();
        
        return 0;
    }

    
    public void setOutputWriter(Writer writer)
    {
        this.writer = writer;
    }
    
    
    
    
    private void printTable() throws IOException
    {
        writer.write( "MACAddress \tport\n" );
        MACAddressesTable table =  _switch_.getMACAddressesTable();
        
        for(Iterator<MACAddress> i = table.getMappedAddresses().iterator(); i.hasNext(); )
        {
            MACAddress key = i.next();
            int port = table.get( key );
            writer.write( key+"\t"+port+"\n" );
        }
    }

    public void Stop()
    {
    }
}

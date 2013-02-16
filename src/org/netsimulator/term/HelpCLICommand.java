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
import java.util.Iterator;
import org.apache.commons.cli.*;


public class HelpCLICommand implements CLICommand
{
    private Terminal term;
    private Writer writer;
    private static final Options options = new Options();

    public HelpCLICommand(Terminal term)
    {
        this.term = term;
        
        options.addOption("h", false, "display this help");
    }


    public String getName()
    {
        return "help";
    }

    public int Go(String argv[], String cl)
    {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try
        {
            cmd = parser.parse( options, argv);
        }catch(ParseException pe)
        {
            pe.printStackTrace();
        }
 
        if(cmd.hasOption("h"))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    new PrintWriter(writer),
                    80,
                    "help",
                    "Display list of available commands.",
                    options,
                    3,
                    2,
                    null,
                    true);
        }else
        {
            printAllCommands();
        }

        return 0;
    }

    
    public void setOutputWriter(Writer writer)
    {
        this.writer = writer;
    }

    public void Stop()
    {
    }
    

    private void printAllCommands()
    {
        for(Iterator i=term.getCommands().keySet().iterator(); i.hasNext(); )
        {
            try
            {
                writer.write(i.next()+"\n");
            }catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }

}

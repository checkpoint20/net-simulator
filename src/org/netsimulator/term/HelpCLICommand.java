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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.*;

public class HelpCLICommand  extends AbstractCommand {

    private static final Logger logger
            = Logger.getLogger("org.netsimulator.term.HelpCLICommand");

    private Terminal term;
    private Writer writer;
    private static final Options options = new Options();

    public HelpCLICommand(Terminal term) {
        this.term = term;

        options.addOption("h", false, "display this help");
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void run() {
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, argv);
            if (cmd.hasOption("h")) {
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
            } else {
                printAllCommands();
            }
        } catch (ParseException pe) {
            logger.log(Level.SEVERE, "Unexpected exception.", pe);
        } finally {
            fireExecutionCompleted(0);
        }

    }

    @Override
    public void setOutputWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void stop() {
    }

    private void printAllCommands() {
        for (Iterator i = term.getCommands().keySet().iterator(); i.hasNext();) {
            try {
                writer.write(i.next() + "\n");
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Unexpected exception.", ioe);
            }
        }
    }

}

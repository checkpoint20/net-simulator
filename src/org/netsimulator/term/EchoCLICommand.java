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
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.*;

public class EchoCLICommand extends AbstractCommand {

    private static final Logger logger
            = Logger.getLogger(EchoCLICommand.class.getName());

    private Writer writer;

    private static final Options options = new Options();

    public EchoCLICommand() {
        Option help = new Option("h", false, "display this help");
        options.addOption(help);
    }

    @Override
    public String getName() {
        return "echo";
    }

    private int go() throws IOException {
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, argv);
        } catch (MissingArgumentException mae) {
            writer.write("Error: Missing arguments\n");
            return -1;
        } catch (UnrecognizedOptionException uoe) {
            writer.write("Error: " + uoe.getMessage() + "\n");
            return -1;
        } catch (ParseException pe) {
            logger.log(Level.SEVERE, "Unexpected exception.", pe);
            return -1;
        }
 
        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    new PrintWriter(writer),
                    80,
                    "echo [-h] [<text>]",
                    "Display a line of text.",
                    options,
                    3,
                    2,
                    null,
                    false);
            
            return 0;
        }
        
        StringTokenizer t = new StringTokenizer(cl, " \t\n\r\f", true);

        if (t.countTokens() >= 2) {
            t.nextToken();
            t.nextToken();

            while (t.hasMoreTokens()) {
                writer.write(t.nextToken());
            }

            writer.write("\n");
            }
        return 0;
    }

    @Override
    public void setOutputWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void stop() {
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

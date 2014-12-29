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
import org.netsimulator.net.ICMPEchoPacket;
import org.netsimulator.net.ICMPEchoReplayListener;
import org.netsimulator.net.IP4Address;
import org.netsimulator.net.IP4Router;
import org.netsimulator.net.Protocols;

public class PingCLICommand extends AbstractCommand implements ICMPEchoReplayListener {

    private static final Logger logger = Logger.getLogger("org.netsimulator.term.PingCLICommand");

    private PrintWriter writer;
    private final IP4Router router;
    private static final Options options = new Options();
    private boolean go = false;
    private ICMPEchoPacket icmpEchoReplayPacket = null;
    private final int identifier;
    private boolean timeoutExpired = false;
    private static final Timer TIMER = new Timer("PingResponceTimeoutThread", true);
    private TimerTaskPing timerTask;


    public PingCLICommand(IP4Router router) {
        this.router = router;

        Option help = new Option("h", false, "display this help");
        options.addOption(help);

        Option ttl = OptionBuilder.withArgName("ttl")
                .hasArg()
                .withDescription("set the IP Time to Live")
                .create("t");
        options.addOption(ttl);

        Option interval = OptionBuilder.withArgName("interval")
                .hasArg()
                .withDescription("wait interval seconds between sending each packet, the default is to wait for one second")
                .create("i");
        options.addOption(interval);
        router.addICMPEchoReplayListener(this);

        identifier = router.hashCode();
    }

    @Override
    public String getName() {
        return "ping";
    }

    protected int go() throws IOException {
        int ttl = ICMPEchoPacket.DEFAULT_TTL;
        int interval = 1;
        int timeout = 5;

        go = true;

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

        if (cmd.hasOption("t")) {
            try {
                ttl = Integer.parseInt(cmd.getOptionValue("t"));
            } catch (NumberFormatException nfe) {
                writer.write("Error: bad time-to-live\n");
                return -1;
            }
        }

        if (cmd.hasOption("i")) {
            try {
                interval = Integer.parseInt(cmd.getOptionValue("i"));
            } catch (NumberFormatException nfe) {
                writer.write("Error: bad timing interval\n");
                return -1;
            }
        }

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    writer,
                    80,
                    "ping [-h] [-i <interval>] [-t <ttl>] <destination>",
                    "Ping uses the ICMP protocol's mandatory ECHO_REQUEST datagram to elicit an ICMP ECHO_RESPONSE from a host or gateway.",
                    options,
                    3,
                    2,
                    null,
                    false);

            return 0;
        }

        String args[] = cmd.getArgs();
        if (args.length > 0) {
            IP4Address dest = null;
            try {
                dest = new IP4Address(args[0]);
            } catch (AddressException ae) {
                writer.write("Error: Unknown host " + args[0] + "\n");
                return 1;
            }

            writeBanner(dest);

            int icmp_seq = 0;
            while (go) {
                ICMPEchoPacket icmpRequest = null;
                try {
                    icmpRequest = new ICMPEchoPacket(
                            Protocols.ICMPEcho,
                            identifier,
                            icmp_seq,
                            ICMPEchoPacket.DEFAULT_TTL,
                            ttl,
                            null,
                            dest,
                            System.currentTimeMillis(),
                            null
                    );
                } catch (AddressException ae) {
                    writer.write("Error: " + ae.getMessage() + "\n");
                    return 1;
                }

                icmpEchoReplayPacket = null;
                router.routePacket(icmpRequest);

                timeoutExpired = false;
                timerTask = new TimerTaskPing(this);
                TIMER.schedule(timerTask, timeout * 1000);
                waitICMPReplay();

                if (icmpEchoReplayPacket == null) {
                    writer.write("icmp_seq=" + icmp_seq + " Destination Host Unreachable\n");
                } else {
                    long rtt = System.currentTimeMillis() - icmpRequest.getTimestamp();
                    writer.write("64 bytes from " + dest + ": icmp_seq=" + icmp_seq + " ttl=" + icmpEchoReplayPacket.getTTL() + " time=" + rtt + " ms\n");
                }

                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException ie) {
                    go = false;
                    logger.log(Level.SEVERE, "Unexpected exception.", ie);                
                    Thread.currentThread().interrupt();
                }

                icmp_seq++;
            }

        } else {
            writer.write("Error: Missing arguments\n");
        }

        go = false;
        return 0;
    }

    @Override
    public void setOutputWriter(Writer writer) {
        this.writer = new PrintWriter(writer);
    }

    @Override
    public void stop() {
        go = false;
    }

    public synchronized void TimeoutExpired(boolean timeoutExpired) {
        this.timeoutExpired = timeoutExpired;
        notifyAll();
    }

    @Override
    public synchronized void processICMPEchoReplay(ICMPEchoPacket packet) {
        logger.log(Level.FINE, "Have gotten an ICMPEchoReplay, checking the identifier ({1}) ...", new Object[]{packet.getIdentifier()});
        if (packet.getIdentifier() == this.identifier) {
            logger.fine("The identifier belongs to us!");
            icmpEchoReplayPacket = packet;
            notifyAll();
        } else {
            logger.log(Level.FINE, "The identifier ({0}) is strange (our is {1})", new Object[]{packet.getIdentifier(), this.identifier});
        }
    }

    private synchronized void waitICMPReplay() {
        while (icmpEchoReplayPacket == null && go && !timeoutExpired) {
            try {
                wait();
            } catch (InterruptedException ie) {
                logger.log(Level.SEVERE, "Unexpected exception.", ie);                
                Thread.currentThread().interrupt();
            }
        }

        timerTask.cancel();
    }

    private void writeBanner(IP4Address dest) {
        writer.write("PING " + dest + "\nPress Ctrl+C то abort.\n");
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

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

package org.netsimulator.gui;


import java.awt.*;
import java.util.logging.*;
import org.netsimulator.util.LogFormatter;

public class Blinker extends Thread
{
    public static final int NOTHING=-1;
    public static final int RX=0;
    public static final int TX=1;

    private static final Logger logger =
            Logger.getLogger("org.netsimulator.gui.Blinker");
    private int whatToBlink = NOTHING;    
    private int timeout;
    private SocketNetworkShape socket;
    private boolean stop;

    
    /** Creates a new instance of PatchcordFader */
    public Blinker(int timeout, SocketNetworkShape socket, int whatToBlink)
    {
        this.timeout = timeout;
        this.socket = socket;
        this.whatToBlink = whatToBlink;
        this.stop = false;
        
        setDaemon(true);
        logger.fine(hashCode()+": Blinker was instantiated");
    }
    
    
    public void run()
    {
        logger.fine(hashCode()+": Blinker was started");
        try
        {
            Thread.sleep(timeout);
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
        
        switch(whatToBlink)
        {
            case RX: 
                socket.SwitchRxLightOn();
                break;
            case TX: 
                socket.SwitchTxLightOn();
                break;
        }
        socket=null;
        logger.fine(hashCode()+": Blinker was stopped");
    }
    
    
    public void cancel()
    {
        socket=null;
        stop=true;
        logger.fine(hashCode()+": Blinker was canceled");
    }
    
}

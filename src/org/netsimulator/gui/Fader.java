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

/*
 * PatchcordFader.java
 *
 * Created on 19 January 2006, 23:59
 */

package org.netsimulator.gui;


import java.awt.*;
import java.util.logging.*;


public class Fader extends Thread
{
    private static final Logger logger = 
            Logger.getLogger("org.netsimulator.gui.Fader");
    private Color from, current;
    private Faderable faderable;
    private boolean stop;
    
    
    /** Creates a new instance of PatchcordFader */
    public Fader(Color from, Faderable faderable)
    {
        this.from = from;
        this.faderable = faderable;
        this.stop = false;
        
        setDaemon(true);
        logger.fine(hashCode()+": Fader was instantiated");
    }
    
    
    public void run()
    {
        logger.fine(hashCode()+": Fader was started");
        current = from;
        int r=0, g=0, b=0;
        int STEP = 8;
        int DELAY = 50;
        while( !(current.equals(Color.BLACK) || stop) )
        {
           logger.fine(hashCode()+": Fader iteration r="+current.getRed()+" g="+current.getGreen()+" b="+current.getBlue());
           
            r = (current.getRed()-STEP) < 0 ? 0 : (current.getRed()-STEP);
            g = (current.getGreen()-STEP) < 0 ? 0 : (current.getGreen()-STEP);
            b = (current.getBlue()-STEP) < 0 ? 0 : (current.getBlue()-STEP);
            current = new Color(r, g, b);
            if(faderable != null)
            {    
                faderable.setCurrentColor(current);
            }else
            {
                break;
            }
            
            try
            {
                Thread.sleep(DELAY);
            }catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }
        faderable=null;
        logger.fine(hashCode()+": Fader was stopped");
    }
    
    
    public void cancel()
    {
        faderable=null;
        stop=true;
        logger.fine(hashCode()+": Fader was canceled");
    }
    
}

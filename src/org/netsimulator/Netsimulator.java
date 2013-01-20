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

package org.netsimulator;

import java.util.logging.Logger;
import org.netsimulator.gui.MainFrame;

public class Netsimulator
{
    private static final Logger logger = Logger.getLogger(Netsimulator.class.getName());

    
    public static void main(String argv[])
    {
        if(argv.length>=1 && argv[0]!=null && argv[0].equals("-h"))
        {
            System.out.println("Locale can be set by arguments <la> <CA>."+
                    "Where la is a valid ISO Language Code, CA is a valid ISO Country Code.");
            System.exit(0);
        }
        
        String lang=null, country=null;
        if(argv.length>=2)
        {
            lang=argv[0];
            country=argv[1];
        }

        logger.info("Starting NetSimulator!");        
        
        new MainFrame(lang, country);
    }
}

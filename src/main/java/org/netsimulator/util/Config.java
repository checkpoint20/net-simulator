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

package org.netsimulator.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Config
{
    private String whatOpenAfterStart;
    private String lastProject;
    private String currentSkin;
    private Color background;
    private HashMap<String, ArrayList<ShapeInfo>> skins;
    private LoggersConfig debug;
    
    public Config ()
    {
    }

    public String getWhatOpenAfterStart ()
    {
        return whatOpenAfterStart;
    }

    public void setWhatOpenAfterStart( String whatOpenAfterStart )
    {
        this.whatOpenAfterStart = whatOpenAfterStart;
    }

    public String getLastProject ()
    {
        return lastProject;
    }

    public void setLastProject( String lastProject )
    {
        this.lastProject = lastProject;
    }

    public String getCurrentSkin ()
    {
        return currentSkin;
    }

    public void setCurrentSkin( String currentSkin )
    {
        this.currentSkin = currentSkin;
    }

    public Color getBackground ()
    {
        return background;
    }

    public void setBackground( Color background )
    {
        this.background = background;
    }

    public HashMap<String, ArrayList<ShapeInfo>> getSkins()
    {
        return skins;
    }

    public void setSkins( HashMap<String, ArrayList<ShapeInfo>> skins )
    {
        this.skins = skins;
    }

    public LoggersConfig getDebug()
    {
        return debug;
    }

    public void setDebug( LoggersConfig debug )
    {
        this.debug = debug;
    }

    public void clear()
    {
        setBackground(null);
        setCurrentSkin(null);
        setDebug(null);
        setLastProject(null);
        setSkins(null);
        setWhatOpenAfterStart(null);
    }

    public boolean isEmpty()
    {
        return
                getBackground() == null &&
                getCurrentSkin() == null &&
                getDebug() == null &&
                getLastProject() == null &&
                getSkins() == null &&
                getWhatOpenAfterStart() == null ;
    }

    public void merge(Config that)
    {
        if( that.background         != null ) { this.background         = that.background; }
        if( that.currentSkin        != null ) { this.currentSkin        = that.currentSkin; }
        if( that.debug              != null ) { this.debug              = that.debug; }
        if( that.lastProject        != null ) { this.lastProject        = that.lastProject; }
        if( that.skins              != null ) { this.skins              = that.skins; }
        if( that.whatOpenAfterStart != null ) { this.whatOpenAfterStart = that.whatOpenAfterStart; }
    }
    
}

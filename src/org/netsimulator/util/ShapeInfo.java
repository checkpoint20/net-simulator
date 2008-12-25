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


public class ShapeInfo
{
    private String name;
    private int socketsX = -1;
    private int socketsY = -1;
    private int socketsStep = -1;
    
    
    
    public ShapeInfo()
    {
        this(null, -1, -1, -1);
    }
    
    
    
    public ShapeInfo(String name, int socketsX, int socketsY, int socketsStep)
    {
        this.name = name;
        this.socketsX = socketsX;
        this.socketsY = socketsY;
        this.socketsStep = socketsStep;
    }
    
    
    public String getName()
    {
        return name;
    }
    
    
    public String toString()
    {
        return getName();
    }
    
    
    public int getSocketsX()
    {
        return socketsX;
    }

    
    public int getSocketsY()
    {
        return socketsY;
    }


    public int getSocketsStep()
    {
        return socketsStep;
    }
    
    
    
    public void setName(String name)
    {
        this.name = name;
    }


    public void setSocketsX(int socketsX)
    {
        this.socketsX = socketsX;
    }


    public void setSocketsY(int socketsY)
    {
        this.socketsY = socketsY;
    }


    public void setSocketsStep(int socketsStep)
    {
        this.socketsStep = socketsStep;
    }
}

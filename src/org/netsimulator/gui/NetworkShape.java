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
import java.awt.geom.*;
import java.awt.event.*;

public abstract class NetworkShape 
        extends Rectangle 
        implements MouseListener, MouseMotionListener
{
    protected int id;
    protected boolean isSelected;
    protected boolean isHilighted;
    protected Color SELECTED_COLOR = Color.BLUE;
    protected Color HILIGHTED_COLOR = Color.YELLOW;
    protected Color COLOR = Color.GREEN;
    
    protected static NetworkShape selectedShape; 

    
    public static NetworkShape getSelectedShape()
    {
        return selectedShape; 
    }

    
    public int getId()
    {
        return id;
    }
    
    
    public static void setSelectedShape(NetworkShape selectedShape)
    {
        NetworkShape.selectedShape = selectedShape;
    }
    
    
    public boolean isHilighted()
    {
        return isHilighted;
    }


    public boolean isSelected()
    {
        return isSelected;
    }


    public void setSelected(boolean flag)
    {
        isSelected = flag;
    }


    public void setHilighted(boolean flag)
    {
        isHilighted = flag;
    }

    
    public abstract void show(Graphics2D g2d);
    
    public void mouseClicked(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}
    public void mouseMoved(MouseEvent e){}
}

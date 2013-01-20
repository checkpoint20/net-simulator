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


public class ScreenWriter extends Writer
{
    private boolean isclosed = false;
    private TextScreen screen = null;


    public ScreenWriter(TextScreen screen)
    {
        super();
        this.screen = screen;
        this.isclosed = false;
    }


    public void write(char[] cbuf, int off, int len)
    throws IOException
    {
        if(isclosed) throw new IOException("Writer was closed!");
        if(cbuf==null ||
           off<0 || len<0 ||
           cbuf.length < off+len) throw new IOException();


        for(int i=off; i!=len; i++)
        {
  //  System.out.println("x="+screen.getX_cur()+" y="+screen.getY_cur()+" ch="+cbuf[i]);
            screen.drawChar(cbuf[i]);
        }

        flush();
    }


    public void flush()
    throws IOException
    {
        if(isclosed) throw new IOException("Writer was closed!");

        screen.repaint();
    }



    public void close()
    throws IOException
    {
        flush();
        isclosed = true;
    }

}
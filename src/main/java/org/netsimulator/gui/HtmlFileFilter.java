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

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class HtmlFileFilter extends FileFilter
{
    private static final String html = "html";
    private static final String htm = "htm";
    
    /** Creates a new instance of XMLFileFilter */
    public HtmlFileFilter()
    {
    }
    

    public boolean accept(File f) 
    {
        if (f.isDirectory()) 
        {
            return true;
        }

        String extension = getExtension(f);
        if(extension!=null &&
           (extension.equals(html) || extension.equals(htm)))
        {
            return true;
    	}else
        {
    	    return false;
    	}
    }
    
    
    
    
    public String getDescription()
    {
        return "HTML";
    }
    
    
    private String getExtension(File f) 
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if(i>0 &&  i<(s.length()-1))
        {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }    
}

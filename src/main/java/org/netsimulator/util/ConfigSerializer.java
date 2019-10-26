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
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class ConfigSerializer
{
    private static final String indent = "\t";
    private Writer writer;
    
    /**
     * Creates a new instance of ConfigSerializer
     */
    public ConfigSerializer()
    {
    }
 
    
    public void serialize(Config config, File toFile)
    throws IOException
    {
            try
            {
                this.writer= new OutputStreamWriter(
                    new FileOutputStream(toFile), XMLHelper.charsetName);
            }catch(UnsupportedEncodingException uee)
            {
                uee.printStackTrace();
            }


            write(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            write(0, "<!DOCTYPE config PUBLIC \""
                    + ResourceEntityResolverFactory.CONFIG_DTD_PUBLIC_ID
                    + "\" \"http://www.net-simulator.org/dtd/1.0/config.dtd\">");
            write(0, "<config>");

            serializeWhatOpenAfterStart(1, config.getWhatOpenAfterStart());
            serializeLastProject(1, config.getLastProject());
            serializeCurrentSkin(1, config.getCurrentSkin());
            serializeBackground(1, config.getBackground());
            serializeSkins(1, config.getSkins());
            serializeDebug(1, config.getDebug());

            write(0, "</config>");
            writer.close();
    }

    
    private void serializeWhatOpenAfterStart(int indentSize, String whatOpenAfterStart)
    throws IOException
    {
        write(indentSize, "<whatOpenAfterStart value=\""+whatOpenAfterStart+"\" />");
    }
    
    
    private void serializeLastProject(int indentSize, String lastProject)
    throws IOException
    {
        write(indentSize, "<lastProject value=\""+lastProject+"\" />");
    }
    

    private void serializeCurrentSkin(int indentSize, String currentSkin)
    throws IOException
    {
        write(indentSize, "<currentSkin name=\""+currentSkin+"\" />");
    }
    

    private void serializeBackground(int indentSize, Color background)
    throws IOException
    {
        String color = 
                Integer.toHexString(background.getRed()) +
                Integer.toHexString(background.getGreen()) +
                Integer.toHexString(background.getBlue()) ;
        write(indentSize, "<background value=\"#"+color+"\" />");
    }
    
    
    private void serializeDebug(int indentSize, LoggersConfig loggersConfig)
    throws IOException
    {
        write(indentSize, "<debug>");
        for(int i=0; i!=loggersConfig.getRowCount(); i++)
        {    
            write(indentSize+1, "<logger name=\""+loggersConfig.getValueAt(i, 0)+"\" logLevel=\""+loggersConfig.getValueAt(i, 1)+"\" />");
        }
        write(indentSize, "</debug>");
    }

    
    
    private void serializeSkins(int indentSize, HashMap<String, ArrayList<ShapeInfo>> skins)
    throws IOException
    {
        write(indentSize, "<skins>");
        for(String skinName : skins.keySet() )
        {   
            ArrayList<ShapeInfo> skin = skins.get(skinName);
            write(indentSize+1, "<skin name=\""+skinName+"\">");
            for( ShapeInfo shape : skin )
            {   
                write(indentSize+2, "<shape name=\""+shape+"\" socketsX=\""+
                        shape.getSocketsX()+"\" socketsY=\""+
                        shape.getSocketsY()+"\" socketsStep=\""+
                        shape.getSocketsStep()+"\" />");
            }
            write(indentSize+1, "</skin>");
        }
        write(indentSize, "</skins>");
    }
    

    private void write(int indentSize, String str) throws IOException {
        for(int i=0; i!=indentSize; i++) {
            writer.write(indent);
        }
        writer.write(str);
        writer.write("\n");
    }
}

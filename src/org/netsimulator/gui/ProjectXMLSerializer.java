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


import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import org.netsimulator.net.EthernetInterface;
import org.netsimulator.net.Hub;
import org.netsimulator.net.IP4Router;
import org.netsimulator.net.Interface;
import org.netsimulator.net.Port;
import org.netsimulator.net.RoutingTable;
import org.netsimulator.net.RoutingTableRow;
import org.netsimulator.net.Switch;
import org.netsimulator.util.Dateutil;
import org.netsimulator.util.XMLHelper;

public class ProjectXMLSerializer implements Runnable
{
    private static final String indent = "\t";
    private Writer writer;
    private NetworkPanel panel;
    private JProgressBar progressBar;
    private File file;
    private SerializingErrorHandler errorHandler;
    private ArrayList<ProjectXMLSerializingCompleteListener> serializingListeners = 
            new ArrayList<ProjectXMLSerializingCompleteListener>();   
    
    /**
     * Saves the project to xml file. You should use an exemplar
     * of ProjectXMLSerializer as an argument of Thread constructor
     * like this:
     *     new Thread(new ProjectXMLSerializer(....)).start();
     *
     * @param panel project to save
     * @param progressBar component to show the saving progress (may be null)
     * @param errorHandler an SerializingErrorHandler exemplar
     */
    public ProjectXMLSerializer(
            NetworkPanel panel,
            JProgressBar progressBar,
            SerializingErrorHandler errorHandler)
    {
        this.panel = panel;
        this.progressBar = progressBar;
        this.errorHandler = errorHandler;
    }
 
    
    
    
    
    
    
    private void write(int indentSize, String str)
    throws IOException
    {
        StringBuffer buf = new StringBuffer();
        for(int i=0; i!=indentSize; i++)
        {
            buf.append(indent);
        }
        buf.append(str);
        buf.append("\n");
        
        writer.write(buf.toString());
    }
    

    
    
    
    private void serializeDesktopNetworkShape(int indentSize, DesktopNetworkShape shape)
    throws IOException
    {
        write(indentSize, "<desktopShape id=\""+shape.getId()+
                "\" name=\""+ nnStr(shape.getName()) +
                "\" description=\""+ nnStr(shape.getComment()) +
                "\" x=\""+(int)shape.getX()+
                "\" y=\""+(int)shape.getY()+"\">");
        IP4Router router = null;
        if(shape.getRouter() instanceof IP4Router)
        {
            router = (IP4Router)shape.getRouter();
            serializeIP4Router(indentSize+1, router);
        }
        serializeSockets(indentSize+1, shape.getSocketShapes());
        write(indentSize, "</desktopShape>");
    }
    
    
    

    private void serializeRouterNetworkShape(int indentSize, RouterNetworkShape shape)
    throws IOException
    {
        write(indentSize, "<routerShape id=\""+shape.getId()+
                "\" name=\""+ nnStr(shape.getName()) +
                "\" description=\""+ nnStr(shape.getComment()) +
                "\" x=\""+(int)shape.getX()+
                "\" y=\""+(int)shape.getY()+"\">");
        IP4Router router = null;
        if(shape.getRouter() instanceof IP4Router)
        {
            router = (IP4Router)shape.getRouter();
            serializeIP4Router(indentSize+1, router);
        }
        serializeSockets(indentSize+1, shape.getSocketShapes());
        write(indentSize, "</routerShape>");
    }


    

    private void serializeIP4Router(int indentSize, IP4Router router)
    throws IOException
    {
        write(indentSize, "<IP4Router id=\""+router.getId()+"\">");
        serializeIntefaces(indentSize+1, router.getInterfaces());
        serializeRoutingTable(indentSize+1, router.getRoutingTable());
        write(indentSize, "</IP4Router>");
    }
    
    
    
    
    private void serializeHubNetworkShape(int indentSize, HubNetworkShape shape)
    throws IOException
    {
        write(indentSize, "<hubShape id=\""+shape.getId()+
                "\" name=\""+nnStr(shape.getName())+
                "\" description=\""+nnStr(shape.getComment())+
                "\" x=\""+(int)shape.getX()+
                "\" y=\""+(int)shape.getY()+"\">");
        serializeHub(indentSize+1, shape.getHub());
        serializeSockets(indentSize+1, shape.getSocketShapes());
        write(indentSize, "</hubShape>");
    }

    
    
    
    private void serializeHub(int indentSize, Hub hub)
    throws IOException
    {
        write(indentSize, "<hub id=\""+hub.getId()+"\">");
        serializePorts(indentSize+1, hub.getPorts());
        write(indentSize, "</hub>");
    }
    
    
    
    
    
    private void serializeSwitchNetworkShape(int indentSize, SwitchNetworkShape shape)
    throws IOException
    {
        write(indentSize, "<switchShape id=\""+shape.getId()+
                "\" name=\""+nnStr(shape.getName())+
                "\" description=\""+nnStr(shape.getComment())+
                "\" x=\""+(int)shape.getX()+
                "\" y=\""+(int)shape.getY()+"\">");
        serializeSwitch(indentSize+1, shape.getSwitch());
        serializeSockets(indentSize+1, shape.getSocketShapes());
        write(indentSize, "</switchShape>");
    }


    
    
    private void serializeSwitch(int indentSize, Switch _switch_)
    throws IOException
    {
        write(indentSize, "<switch id=\""+_switch_.getId()+"\">");
        serializePorts(indentSize+1, _switch_.getPorts());
        write(indentSize, "</switch>");
    }
    
    
    
    
    private void serializeIntefaces(int indentSize, Interface ifs[])
    throws IOException
    {
        for(int i=0; i!=ifs.length; i++)
        {
            if(ifs[i] instanceof EthernetInterface)
            {
                EthernetInterface eth = (EthernetInterface)ifs[i];
                write(indentSize, "<eth id=\""+eth.getId()+
                        "\" name=\""+ nnStr(eth.getName()) +
                        "\" status=\""+ eth.getStatus()+
                        "\" mac=\""+ eth.getMACAddress()+
                        "\" ip4=\""+ nnStr(eth.getInetAddress()) +
                        "\" ip4bcast=\""+ nnStr(eth.getBroadcastAddress()) +
                        "\" ip4mask=\""+ nnStr(eth.getNetmaskAddress()) +
                        "\" bandwidth=\""+ eth.getBandwidth()+"\" />");
            }
        }
    }


    
    private void serializePorts(int indentSize, Port port[])
    throws IOException
    {
        for(int i=0; i!=port.length; i++)
        {
            write(indentSize, "<port id=\""+port[i].getId()+"\" />");
        }
    }
    


    private void serializeSockets(int indentSize, SocketNetworkShape shapes[])
    throws IOException
    {
        for(int i=0; i!=shapes.length; i++)
        {
            write(indentSize, "<socketShape id=\""+shapes[i].getId()+
                    "\" x=\""+(int)shapes[i].getX()+
                    "\" y=\""+(int)shapes[i].getY()+
                    "\" devId=\""+shapes[i].getNetworkDevice().getId()+"\" />");
        }
    }

    
    
    
    private void serializeRoutingTable(int indentSize, RoutingTable table)
    throws IOException
    {
        write(indentSize, "<routingTable>");
        Collection<RoutingTableRow> rows = table.getRows();
        synchronized( rows ) 
        {
            for(Iterator<RoutingTableRow> i=rows.iterator(); i.hasNext(); )
            {
                RoutingTableRow row = i.next();
                write(indentSize+1, "<row target=\""+ row.getTarget() +
                        "\" netmask=\""+ row.getNetmask()+
                        "\" gateway=\""+ nnStr(row.getGateway()) +
                        "\" metric=\""+ row.getMetric()+
                        "\" iface=\""+ row.getInterface()+
                        "\" />");
            }
        }
        write(indentSize, "</routingTable>");
    }

    
    
    private void serializePatchcordNetworkLink(
            int indentSize, 
            PatchcordNetworkLink link)
    throws IOException
    {
        write(indentSize, "<patchcord id=\""+link.getId()+"\">");
        
        write(indentSize+1, "<media id=\""+link.getMedia().getId()+
                "\" pointsCount=\""+link.getMedia().getPointsCount()+"\" />");
        
        SocketNetworkShape socket =null;
        String str = null;
        
        socket = link.getPlug1().getConnectedSocket();
        str = socket==null?"":socket.getId()+"";
        write(indentSize+1, "<plug id=\""+link.getPlug1().getId()+
                "\" point=\""+link.getPlug1().getPoint()+
                "\" x=\""+(int)link.getPlug1().getX()+
                "\" y=\""+(int)link.getPlug1().getY()+
                "\" socket=\""+str+"\" />");

        socket = link.getPlug2().getConnectedSocket();
        str = socket==null?"":socket.getId()+"";
        write(indentSize+1, "<plug id=\""+link.getPlug2().getId()+
                "\" point=\""+link.getPlug2().getPoint()+
                "\" x=\""+(int)link.getPlug2().getX()+
                "\" y=\""+(int)link.getPlug2().getY()+
                "\" socket=\""+str+"\" />");

        write(indentSize, "</patchcord>");
    }
    
    
    
    
    private String nnStr(Object obj)
    {
        if(obj==null)
        {
            return "";
        }else
        {
            return obj.toString();
        }
    }

    
    
    
    
    
    
    
    public void run()
    {
        if(progressBar != null)
        {
            progressBar.setMinimum(0);
            progressBar.setIndeterminate(false);
            progressBar.
                    setMaximum(panel.getDevicesCount()+panel.getLinksCount());
            progressBar.setStringPainted(true);
        }

        try
        {
            try
            {
                this.writer= new OutputStreamWriter(
                    new FileOutputStream(file), XMLHelper.charsetName);
            }catch(UnsupportedEncodingException uee)
            {
                uee.printStackTrace();
            }


            write(0, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            write(0, "<!DOCTYPE project PUBLIC \"NET-Simulator/dtd/netsimulator.dtd\" \"http://www.net-simulator.org/dtd/1.0/netsimulator.dtd\">");
            write(0, "<project author=\""+panel.getAuthor()+
                    "\" description=\""+panel.getDescription()+
                    "\" createDate=\""+panel.getCreateDate()+
                    "\" currentId=\""+panel.getIdGenerator().getCurrentId()+
                    "\">");

            int objectNumber = 0;
            for(NetworkShape obj : panel.getDevicesLayer())
            {
                if(obj instanceof DesktopNetworkShape)
                {
                    serializeDesktopNetworkShape(1, (DesktopNetworkShape) obj);
                }

                if(obj instanceof RouterNetworkShape)
                {
                    serializeRouterNetworkShape(1, (RouterNetworkShape) obj);
                }

                if(obj instanceof HubNetworkShape)
                {
                    serializeHubNetworkShape(1, (HubNetworkShape) obj);
                }

                if(obj instanceof SwitchNetworkShape)
                {
                    serializeSwitchNetworkShape(1, (SwitchNetworkShape) obj);
                }
                
                if(progressBar!=null)
                {
                    progressBar.setValue(++objectNumber);
                }
            }

            for(NetworkLink link : panel.getLinksLayer())
            {
                if(link instanceof PatchcordNetworkLink)
                {
                    serializePatchcordNetworkLink(1, (PatchcordNetworkLink) link);
                }
                progressBar.setValue(++objectNumber);
            }

            write(0, "</project>");
            writer.close();
            
        }catch(IOException ioe)
        {
            errorHandler.fatalError(ioe);
        }finally
        { 
            for(Iterator<ProjectXMLSerializingCompleteListener> i=serializingListeners.iterator(); i.hasNext(); )
            {
                ProjectXMLSerializingCompleteListener l = i.next();
                l.ProjectSerializingComplete();
                i.remove();
            }
        }
    }
    
   /**
     * ProjectSerializerCompleteListener are erased after serializing compliting.
     */ 
    public void addProjectSerializingCompleteListener(ProjectXMLSerializingCompleteListener listener)
    {
        serializingListeners.add(listener);
    }
    
    
   /**
     * ProjectSerializerCompleteListener are erased after serializing compliting.
     */ 
    public void removeProjectSerializingCompleteListener(ProjectXMLSerializingCompleteListener listener)
    {
        serializingListeners.remove(listener);
    }
    
    
    public void setOutputFile(File outputFile)
    {
        this.file = outputFile;
    }

    
}
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
 * ProjectXMLLoader.java
 *
 * Created on 14 May, 2006, 15:58
 */

package org.netsimulator.gui;


import org.netsimulator.net.*;
import org.netsimulator.util.ProjectDTDReferenceResolver;
import org.netsimulator.util.XMLHelper;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectXMLLoader implements ContentHandler, Runnable
{
    private static final Logger LOGGER = Logger.getLogger(ProjectXMLLoader.class.getName());
    
    private XMLReader reader;
    private InputSource inputSource;
    private NetworkPanel panel;
    private Locator locator;
    private ErrorHandler errorHandler;
    private JProgressBar progressBar;
    private int linesCount;
    private ArrayList<ProjectXMLLoadingCompleteListener> loadingListeners = 
            new ArrayList<ProjectXMLLoadingCompleteListener>();
    
    private IP4Router currentRouter;
    private SocketsHolder currentSocketsHolder;
    private RouterHolder currentRouterHolder;
    private RoutingTable currentRoutingTable;
    private Concentrator currentConcentrator;
    private PatchcordNetworkLink currentPatchcord;
    private HubNetworkShape currentHubHolder;
    private SwitchNetworkShape currentSwitchHolder;
    
    
    /** Creates a new instance of ProjectXMLSerializer */
    public ProjectXMLLoader(
            NetworkPanel panel, 
            ErrorHandler errorHandler,
            JProgressBar progressBar)
    {
        this.panel = panel;
        this.errorHandler = errorHandler;
        this.progressBar = progressBar;
    }
 
    
    
    public void setSourceFile(File sourceFile)
    throws SAXException, IOException
    {
        reader = XMLReaderFactory.createXMLReader(XMLHelper.vendorParserClass);
        
        reader.setFeature("http://xml.org/sax/features/validation", true);

        LineNumberReader lnr = new LineNumberReader(new FileReader(sourceFile));
        linesCount = 0;
        int curChar = -1;
        while((curChar = lnr.read()) >= 0 )
        {
            if(curChar == '\n')
            {
                linesCount++;
            }
        }
        lnr.close();
                
        inputSource = new InputSource(new FileInputStream(sourceFile));
        inputSource.setEncoding(XMLHelper.charsetName);

        reader.setContentHandler( this );
        reader.setErrorHandler( errorHandler );
        reader.setEntityResolver( new ProjectDTDReferenceResolver() );

        progressBar.setMinimum(0);
        progressBar.setMaximum(linesCount);
        progressBar.setStringPainted(true);
    }
    
    
    public void skippedEntity(String name) throws SAXException
    {
    }

    public void setDocumentLocator(Locator locator)
    {
        this.locator = locator;
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
    }

    public void startDocument() throws SAXException
    {
    }

    public void processingInstruction(String target, String data) throws SAXException
    {
        LOGGER.log(Level.FINE, "Processing instruction target={0} data={1}", new Object[]{target, data});
    }

    public void endDocument() throws SAXException
    {
        panel.repaint();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
    }
    
    

    
    public void startElement(String uri, String localName, String qName, Attributes atts) 
    throws SAXException
    {
        progressBar.setValue(locator.getLineNumber());
        
        if(qName.equals("project"))
        {
            loadProject(atts);
        }

        if(qName.equals("routerShape"))
        {
            startLoadingRouterShape(atts);
        }
        
        if(qName.equals("IP4Router"))
        {
            startLoadingIP4Router(atts);
        }

        if(qName.equals("hubShape"))
        {
            startLoadingHubShape(atts);
        }
        
        if(qName.equals("hub"))
        {
            startLoadingHub(atts);
        }
        
        if(qName.equals("patchcord"))
        {
            startLoadingPatchcord(atts);
        }
        
        if(qName.equals("media"))
        {
            loadMedia(atts);
        }


        if(qName.equals("plug"))
        {
            loadPlug(atts);
        }
        
        
        if(qName.equals("socketShape"))
        {
            loadSocketShape(atts);
        }

        if(qName.equals("hub"))
        {
            startLoadingHub(atts);
        }

        if(qName.equals("switchShape"))
        {
            startLoadingSwitchShape(atts);
        }
        

        if(qName.equals("switch"))
        {
            startLoadingSwitch(atts);
        }
        
        
        if(qName.equals("desktopShape"))
        {
            startLoadingDesktopShape(atts);
        }

        if(qName.equals("eth"))
        {
            loadEthernet(atts);
        }
        
        if(qName.equals("routingTable"))
        {
            startLoadingRoutingTable(atts);
        }
        
        if(qName.equals("row"))
        {
            loadRoutingTableRow(atts);
        }

        if(qName.equals("port"))
        {
            loadPort(atts);
        }
    }

    
    
    
    
    
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        progressBar.setValue(locator.getLineNumber());
        
        if(qName.equals("routerShape"))
        {
            endLoadingRouterShape();
        }
    
        if(qName.equals("IP4Router"))
        {
            endLoadingIP4Router();
        }
        
        if(qName.equals("hub"))
        {
            endLoadingHub();
        }
    
        if(qName.equals("hubShape"))
        {
            endLoadingHubShape();
        }
        
        
        if(qName.equals("switch"))
        {
            endLoadingSwitch();
        }
    
        if(qName.equals("switchShape"))
        {
            endLoadingSwitchShape();
        }

        if(qName.equals("desktopShape"))
        {
            endLoadingDesktopShape();
        }

        if(qName.equals("routingTable"))
        {
            endLoadingRoutingTable();
        }

        if(qName.equals("patchcord"))
        {
            endLoadingPatchcord();
        }
    
    }

    
    
    

    
    private void startLoadingRouterShape(Attributes atts)
    throws SAXException
    {
        try
        {
            int id=0; 
            int x=20, y=20;
            String description="", name="";
            
            for(int i=0; i<atts.getLength(); i++)
            {
                String attr = atts.getQName(i);

                if(attr.equals("id"))
                {
                    id = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("x"))
                {
                    x = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("y"))
                {
                    y = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("description"))
                {
                    description = atts.getValue(i);
                }
                if(attr.equals("name"))
                {
                    name = atts.getValue(i);
                }
            }
            
            RouterNetworkShape new_shape = null ;
            try
            {
                new_shape = new RouterNetworkShape(panel, id);
            }catch(InterruptedException ie)
            {
                LOGGER.log(Level.SEVERE, "Unexpected exception.", ie);
            }
            panel.putOnDevicesLayer(new_shape);

            new_shape.setName(name);
            new_shape.setComment(description);
            new_shape.setLocation(x, y);

            panel.repaint();            
            
            currentSocketsHolder = new_shape;
            currentRouterHolder = new_shape;
            
            LOGGER.log(Level.FINEST, "Start loading: {0}", currentRouterHolder);
        }catch(Exception e)
        {
            throw new SAXException(e);
        }
    }
    


    
    
    
    
    private void startLoadingDesktopShape(Attributes atts)
    throws SAXException
    {
        try
        {
            int id=0; 
            int x=20, y=20;
            String description="", name="";
            
            for(int i=0; i<atts.getLength(); i++)
            {
                String attr = atts.getQName(i);

                if(attr.equals("id"))
                {
                    id = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("x"))
                {
                    x = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("y"))
                {
                    y = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("description"))
                {
                    description = atts.getValue(i);
                }
                if(attr.equals("name"))
                {
                    name = atts.getValue(i);
                }
            }
            
            DesktopNetworkShape new_shape = null ;
            try
            {
                new_shape = new DesktopNetworkShape(panel, id);
            }catch(InterruptedException ie)
            {
                LOGGER.log(Level.SEVERE, "Unexpected exception.", ie);
            }
            panel.putOnDevicesLayer(new_shape);

            new_shape.setName(name);
            new_shape.setComment(description);
            new_shape.setLocation(x, y);

            panel.repaint();            
            
            currentSocketsHolder = new_shape;
            currentRouterHolder = new_shape;
            
            LOGGER.log(Level.FINEST, "Start loading: {0}", currentRouterHolder);
        }catch(Exception e)
        {
            throw new SAXException(e);
        }
    }
        
    
    
 

    
    private void startLoadingIP4Router(Attributes atts)
    throws SAXException
    {
        int id = -1;
        try
        {
            id = Integer.parseInt(atts.getValue("id"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }    
     
        IP4Router router = null;
        try
        {
            router = new IP4Router(panel.getIdGenerator(), 0, id);
        }catch(TooManyInterfacesException tmie)
        {
            throw new SAXException(tmie);
        }
        
        currentRouterHolder.setRouter(router);
        currentRouter = router;
        LOGGER.log(Level.FINEST, "Start loading: {0}", currentRouter.hashCode() + "");
    }
    
    
    
    
    
    
    private void endLoadingIP4Router()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}", currentRouter.hashCode() + "");
        currentRouter = null;
    }
    
    
    
    

    private void startLoadingPatchcord(Attributes atts)
    throws SAXException
    {
        int id = -1;
        try
        {
            id = Integer.parseInt(atts.getValue("id"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }    
     
        PatchcordNetworkLink link = 
                new PatchcordNetworkLink(panel, id);
        currentPatchcord = link;
        LOGGER.log(Level.FINEST, "Start loading: {0}", currentPatchcord);
    }    
    
    
 
    
    
    private void endLoadingPatchcord()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}", currentPatchcord);
        currentPatchcord = null;
    }
    
    
    
    
    
    
    private void startLoadingSwitchShape(Attributes atts)
    throws SAXException
    {
        try
        {
            int id=0;
            int x=20, y=20;
            String description="", name="";
            
            for(int i=0; i<atts.getLength(); i++)
            {
                String attr = atts.getQName(i);

                if(attr.equals("id"))
                {
                    id = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("x"))
                {
                    x = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("y"))
                {
                    y = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("description"))
                {
                    description = atts.getValue(i);
                }
                if(attr.equals("name"))
                {
                    name = atts.getValue(i);
                }
            }

            SwitchNetworkShape new_shape = null ;
            try
            {
                new_shape = new SwitchNetworkShape(panel, id);
            }catch(InterruptedException ie)
            {
                LOGGER.log(Level.SEVERE, "Unexpected exception.", ie);
            }
            panel.putOnDevicesLayer(new_shape);

            new_shape.setName(name);
            new_shape.setComment(description);
            new_shape.setLocation(x, y);
        
            panel.repaint();                        
            
            currentSocketsHolder = new_shape;
            currentSwitchHolder = new_shape;
            LOGGER.log(Level.FINEST, "Start loading: {0}", currentSwitchHolder);
            
        }catch(Exception e)
        {
            throw new SAXException(e);
        }
    }
    


    
    private void startLoadingSwitch(Attributes atts)
    throws SAXException
    {
        int id = -1;
        try
        {
            id = Integer.parseInt(atts.getValue("id"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }    
     
        Switch _switch_ = null;
        _switch_ = new Switch(panel.getIdGenerator(), 0, id);
        currentSwitchHolder.setSwitch(_switch_);
        currentConcentrator = _switch_;
        LOGGER.log(Level.FINEST, "Start loading: {0}", currentConcentrator);
        
    }
    
     


    
    private void endLoadingSwitch()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}", currentConcentrator);
        currentConcentrator = null;
    }
        
    
    
    
    
    
    private void startLoadingHubShape(Attributes atts)
    throws SAXException
    {
        try
        {
            int id=0;
            int x=20, y=20;
            String description="", name="";
            
            for(int i=0; i<atts.getLength(); i++)
            {
                String attr = atts.getQName(i);

                if(attr.equals("id"))
                {
                    id = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("x"))
                {
                    x = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("y"))
                {
                    y = Integer.parseInt(atts.getValue(i));
                }
                if(attr.equals("description"))
                {
                    description = atts.getValue(i);
                }
                if(attr.equals("name"))
                {
                    name = atts.getValue(i);
                }
            }
            

            HubNetworkShape new_shape = null ;
            try
            {
                new_shape = new HubNetworkShape(panel, id);
            }catch(InterruptedException ie)
            {
                LOGGER.log(Level.SEVERE, "Unexpected exception.", ie);
            }
            panel.putOnDevicesLayer(new_shape);

            new_shape.setName(name);
            new_shape.setComment(description);
            new_shape.setLocation(x, y);
        
            panel.repaint();                        
            
            currentSocketsHolder = new_shape;
            currentHubHolder = new_shape;
            LOGGER.log(Level.FINEST, "Start loading: {0}", currentHubHolder);
            
        }catch(Exception e)
        {
            throw new SAXException(e);
        }
    }
        
    
    
    
    private void endLoadingHubShape()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}", currentHubHolder);
        currentSocketsHolder = null;
        currentHubHolder = null;
    }    
    
    
    
    
    
    private void startLoadingHub(Attributes atts)
    throws SAXException
    {
        int id = -1;
        try
        {
            id = Integer.parseInt(atts.getValue("id"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }    
     
        Hub hub = null;
        hub = new Hub(panel.getIdGenerator(), 0, id);
        currentHubHolder.setHub(hub);
        currentConcentrator = hub;
        LOGGER.log(Level.FINEST, "Start loading: {0}", currentConcentrator);
    }
    
    
    
    
    
    
    private void endLoadingHub()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}", currentConcentrator);
        currentConcentrator = null;
    }
        
    
    
    
    private void startLoadingRoutingTable(Attributes atts)
    {
        currentRoutingTable = currentRouter.getRoutingTable();
        LOGGER.log(Level.FINEST, "Start loading: {0}, current router: {1}", new Object[]{currentRoutingTable.hashCode() + "", currentRouter.hashCode() + ""});
    }
    
    
    private void endLoadingRouterShape()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}", currentRouterHolder);
        currentSocketsHolder = null;
        currentRouterHolder = null;
    }
    

    private void endLoadingDesktopShape()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}", currentRouterHolder);
        currentSocketsHolder = null;
        currentRouterHolder = null;
    }



    private void endLoadingSwitchShape()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}", currentSwitchHolder);
        currentSocketsHolder = null;
        currentSwitchHolder = null;
    }

    
    private void endLoadingRoutingTable()
    {
        LOGGER.log(Level.FINEST, "End loading: {0}, current router: {1}", new Object[]{currentRoutingTable.hashCode() + "", currentRouter.hashCode() + ""});
        currentRoutingTable = null;
    }
    
    
    private void loadProject(Attributes atts)
    throws SAXException
    {
        for(int i=0; i<atts.getLength(); i++)
        {
            String attr = atts.getQName(i);
            
            if(attr.equals("author"))
            {
                panel.setAuthor(atts.getValue(i));
            }

            if(attr.equals("description"))
            {
                panel.setDescription(atts.getValue(i));
            }

            if(attr.equals("createDate"))
            {
                panel.setCreateDate(atts.getValue(i));
            }
            
            if(attr.equals("currentId"))
            {
                int initValue = 0;
                try
                {
                    initValue = Integer.parseInt(atts.getValue(i));
                }catch(NumberFormatException nfe)
                {
                    throw new SAXException(nfe);
                }
                panel.getIdGenerator().setInitValue(initValue);
            }
        }
    }
    
    
    
    
    
    
    private void loadEthernet(Attributes atts)
    throws SAXException
    {
        IP4Address address = null;
        IP4Address netmask = null;
        IP4Address broadcast = null;
        int status = Interface.UNKNOWN;
        int bandwidth = 0;
        String name = atts.getValue("name");

        LOGGER.log(Level.FINEST, "Start loading: {0}, current router: {1}", new Object[]{name, currentRouter.hashCode() + ""});
        
        MACAddress mac = null;
        try {
            mac = new MACAddress(atts.getValue("mac"));
        } catch (AddressException ae) {
            throw new SAXException(ae);
        }

        int id = -1;
        try {
            id = Integer.parseInt(atts.getValue("id"));
        } catch (NumberFormatException nfe) {
            throw new SAXException(nfe);
        }

        EthernetInterface eth = new EthernetInterface(panel.getIdGenerator(), id, mac, name);
        currentRouter.addInterface(eth);
        for (int i = 0; i < atts.getLength(); i++) {
            String attr = atts.getQName(i);

            if (attr.equals("status")) {
                status = Integer.parseInt(atts.getValue(i));
            }

            try {
                if (attr.equals("ip4") && atts.getValue(i).length() != 0) {
                    address = new IP4Address(atts.getValue(i));
                }

                if (attr.equals("ip4bcast") && atts.getValue(i).length() != 0) {
                    broadcast = new IP4Address(atts.getValue(i));
                }

                if (attr.equals("ip4mask") && atts.getValue(i).length() != 0) {
                    netmask = new IP4Address(atts.getValue(i));
                }
            } catch (AddressException ae) {
                throw new SAXException(ae);
            }

            if (attr.equals("bandwidth") && atts.getValue(i).length() != 0) {
                bandwidth = Integer.parseInt(atts.getValue(i));
            }
        }

        try 
        {
            eth.setInetAddress(address);
            eth.setNetmaskAddress(netmask);
            eth.setBroadcastAddress(broadcast);
            eth.setBandwidth(bandwidth);
            if (status != Interface.UNKNOWN) 
            {
                eth.setStatus(status);
            }
        } catch ( ChangeInterfacePropertyException e ) 
        {
            throw new SAXException( e );
        }
        
        LOGGER.log(Level.FINEST, "End loading: {0}, current router: {1}", new Object[]{name, currentRouter.hashCode() + ""});
    }    
    
    
    

 
    
    
    private void loadSocketShape(Attributes atts)
    throws SAXException
    {
        int id = -1, x = -1, y = -1, devId = -1;
        try
        {
            id = Integer.parseInt(atts.getValue("id"));
            x = Integer.parseInt(atts.getValue("x"));
            y = Integer.parseInt(atts.getValue("y"));
            devId = Integer.parseInt(atts.getValue("devId"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }
        
        NetworkDevice dev = 
                currentSocketsHolder.getNetworkDeviceHolder()
                .getNetworkDeviceById(devId);
        
        //NetworkDevice dev = (NetworkDevice)panel.getDeviceById(devId);
        
        SocketNetworkShape socket = null;
        try
        {
            socket = new SocketNetworkShape(dev, panel, id);
        }catch(InterruptedException ie)
        {
            throw new SAXException(ie);
        }

        currentSocketsHolder.addSocket(socket);
    }    
        
    

    
    
    private void loadMedia(Attributes atts)
    throws SAXException
    {
        int id = -1, pointsCount = -1;
        try
        {
            id = Integer.parseInt(atts.getValue("id"));
            pointsCount = Integer.parseInt(atts.getValue("pointsCount"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }
        
        Media media = new Media(id);
        currentPatchcord.setMedia(media);
    }    
        
    
    


    private void loadPlug(Attributes atts)
    throws SAXException
    {
        int 
                id = -1, 
                x = -1, 
                y = -1, 
                point = -1;
        try
        {
            id = Integer.parseInt(atts.getValue("id"));
            x = Integer.parseInt(atts.getValue("x"));
            y = Integer.parseInt(atts.getValue("y"));
            point = Integer.parseInt(atts.getValue("point"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }

        int socket = -1;
        try
        {
            socket = Integer.parseInt(atts.getValue("socket"));
        }catch(NumberFormatException nfe)
        {
            socket = -1;
        }

        PlugNetworkShape plug = new PlugNetworkShape(panel, currentPatchcord, point, id);
        plug.moveInto(x, y);
        
        switch(point)
        {
            case 1: 
                currentPatchcord.setPlug1(plug);
                break;
            case 2:
                currentPatchcord.setPlug2(plug);
                break;
            default: throw new SAXException("Error point value: "+point);
        }

        if(socket >= 0)
        {
            SocketNetworkShape connectedSocket = panel.getSocketById(socket);            
            plug.setConnectedSocket(connectedSocket);
        }
    }    
        
    
    

    
    
    
    
    
    private void loadPort(Attributes atts)
    throws SAXException
    {
        int id = -1;
        try
        {
            id = Integer.parseInt(atts.getValue("id"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }
        
        Port port = new Port(panel.getIdGenerator(), id);
        currentConcentrator.addPort(port);
    }    
    
    
    
    
    
    
    
    
    
    private void loadRoutingTableRow(Attributes atts)
    throws SAXException
    {
        LOGGER.log(Level.FINEST, "Start loading routing table row, current router: {0}, current routing table: {1}", new Object[]{currentRouter.hashCode() + "", currentRoutingTable.hashCode() + ""});
        
        IP4Address target = null;
        try
        {
            target = new IP4Address(atts.getValue("target"));
        }catch(AddressException ae)
        {
            throw new SAXException(ae);
        }

        IP4Address netmask = null;
        try
        {
            netmask = new IP4Address(atts.getValue("netmask"));
        }catch(AddressException ae)
        {
            throw new SAXException(ae);
        }
        
        int metric = -1;
        try
        {
            metric = Integer.parseInt(atts.getValue("metric"));
        }catch(NumberFormatException nfe)
        {
            throw new SAXException(nfe);
        }
        
        String attr = atts.getValue("gateway");
        IP4Address gateway = null;
        if(attr!=null && attr.length()!=0)
        {
            try
            {
                gateway = new IP4Address(attr);
            }catch(AddressException ae)
            {
                throw new SAXException(ae);
            }
        }

        Interface iface = 
                currentRouter.getInterface(atts.getValue("iface"));
        
        if(iface == null) {
            throw new IllegalStateException("Faild to find interface by name: " + atts.getValue("iface"));
        }
        
        try
        {
            currentRoutingTable.addRoute(target, netmask, gateway, metric, iface);
        }catch(Exception e)
        {
            throw new SAXException(e);
        }
        LOGGER.log(Level.FINEST, "End loading routing table row, current router: {0}, current routing table: {1}", new Object[]{currentRouter.hashCode() + "", currentRoutingTable.hashCode() + ""});
    }    

    
    
    public void run()
    {
        try
        {
            reader.parse(inputSource);         
        }catch(IOException ioe)
        {
            LOGGER.log(Level.SEVERE, "Unexpected exception.", ioe);
        }catch(SAXException saxe)
        {
            LOGGER.log(Level.SEVERE, "Unexpected exception.", saxe);
        }finally
        {
            for(Iterator<ProjectXMLLoadingCompleteListener> i=loadingListeners.iterator(); i.hasNext(); )
            {
                ProjectXMLLoadingCompleteListener l = i.next();
                l.ProjectLoadingComplete();
                i.remove();
            }            
        }
    }
    
    
    
    public void addProjectXMLLoadingCompleteListener(ProjectXMLLoadingCompleteListener listener)
    {
        loadingListeners.add(listener);
    }
    
    
    public void removeProjectXMLLoadingCompleteListener(ProjectXMLLoadingCompleteListener listener)
    {
        loadingListeners.remove(listener);
    }
     
}

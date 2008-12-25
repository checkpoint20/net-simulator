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

import org.netsimulator.net.NetworkDevice;
import org.netsimulator.util.IdGenerator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class NetworkPanel
extends JPanel
implements MouseListener, Runnable
{
    private Frame mainFrame;
    private IdGenerator idGenerator;
    private boolean isSaved = false;
    private boolean requestForRepaint = true;
    
    String description = "";
    String author = "";
    String createDate = "";    
    
    ArrayList<NetworkShape>             devicesLayer = new ArrayList<NetworkShape>();
    ArrayList<NetworkLink>              linksLayer   = new ArrayList<NetworkLink>();
    ArrayList<PlugNetworkShape>         plugsLayer   = new ArrayList<PlugNetworkShape>();
    ArrayList<SocketNetworkShape>       socketsLayer = new ArrayList<SocketNetworkShape>();

    
    public NetworkPanel(Frame mainFrame)
    {
        this.mainFrame = mainFrame;
        setBackground(Color.WHITE);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | 
                     AWTEvent.MOUSE_MOTION_EVENT_MASK);
        idGenerator = new IdGenerator();
        
        new Thread(this).start();               
    }


    public void paintComponent(Graphics g)
    {
        super.paintComponent(g); 

        Graphics2D g2d = (Graphics2D) g;

        g2d.addRenderingHints(
                new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                   RenderingHints.VALUE_ANTIALIAS_ON));

        //System.out.println("size="+g2d.getRenderingHints().size());

        try
        {
            for( NetworkShape shape : devicesLayer )
            {
                shape.show(g2d);
            }

            for( NetworkLink link : linksLayer )
            {
                link.show(g2d);
            }

            for( NetworkShape shape : plugsLayer )
            {
                shape.show(g2d);
            }
        }catch(ConcurrentModificationException cme)
        {
            /*
             * The ConcurrentModificationException can rise here
             * during loading a project. When new devices are being added on
             * the panel and the panel is painting them at the same time.
             */ 
        }
    }


    
    
    public void setSaved(boolean state)
    {
        isSaved = state;
    }
    
     
    public boolean isSaved()
    {
        return isSaved;
    }
    

    public void createDesktop()
    {
        int ifsCount = 1;
        
        DesktopNetworkShape new_shape = null ;
        try
        {
            new_shape = new DesktopNetworkShape(this, ifsCount, getIdGenerator().getNextId());
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
        devicesLayer.add(new_shape);
        
        new_shape.setName("Desktop "+devicesLayer.indexOf(new_shape));

        repaint();
        isSaved = false;
    }
    
 

    
    
    public void createHub()
    {
        int portsCount = 8;
        
        HubNetworkShape new_shape = null ;
        try
        {
            new_shape = new HubNetworkShape(
                    this, portsCount, idGenerator.getNextId());
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
        devicesLayer.add(new_shape);
        new_shape.setName("HUB "+devicesLayer.indexOf(new_shape));
        repaint();
        isSaved = false;
    }
 

    
    
    

    public void createSwitch()
    {
        int portsCount = 8;

        SwitchNetworkShape new_shape = null ;
        try
        {
            new_shape = new SwitchNetworkShape(
                    this, portsCount, idGenerator.getNextId());
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
        devicesLayer.add(new_shape);
        new_shape.setName("Switch "+devicesLayer.indexOf(new_shape));
        repaint();
        isSaved = false;
    }    
    

    
    
    
    
    

    public void createRouter()
    {
        int ifsCount = 8;
        
        RouterNetworkShape new_shape = null ;
        try
        {
            new_shape = new RouterNetworkShape(this, ifsCount, getIdGenerator().getNextId());
        }catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
        devicesLayer.add(new_shape);
        
        new_shape.setName("Router "+devicesLayer.indexOf(new_shape));
        repaint();
        isSaved = false;
    }    
        


   
    
    public void deleteDeviceShape(NetworkShape shape)
    {
        devicesLayer.remove(shape);
        isSaved = false;
        repaint();
    }
    
    

    public void deleteSocketShape(SocketNetworkShape shape)
    {
        socketsLayer.remove(shape);
        isSaved = false;
        repaint();
    }
    
    
    
    public void createMedia()
    {
        PatchcordNetworkLink new_link = null ;
        new_link = new PatchcordNetworkLink(this);
        isSaved = false;
    }


    
    public void deleteMedia(NetworkLink link)
    {
        linksLayer.remove(link);
        isSaved = false;
        repaint();
    }

    
    
    
    public void deletePlug(PlugNetworkShape plug)
    {
        plugsLayer.remove(plug);
        isSaved = false;
        repaint();
    }
    
    
    
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}

    
    
    protected void processMouseEvent(MouseEvent e)
    {
        for(Iterator i=plugsLayer.iterator(); i.hasNext(); )
        {
            MouseListener listener = (MouseListener)i.next();
            switch(e.getID())
            {
                case MouseEvent.MOUSE_PRESSED:
                    listener.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    listener.mouseClicked(e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    listener.mouseReleased(e);
                    break;
            }

        }

        for(Iterator i=linksLayer.iterator(); i.hasNext(); )
        {
            MouseListener listener = (MouseListener)i.next();
            switch(e.getID())
            {
                case MouseEvent.MOUSE_PRESSED:
                    listener.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    listener.mouseClicked(e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    listener.mouseReleased(e);
                    break;
            }

        }

        for(Iterator i=devicesLayer.iterator(); i.hasNext(); )
        {
            MouseListener listener = (MouseListener)i.next();

            switch(e.getID())
            {
                case MouseEvent.MOUSE_PRESSED:
                    listener.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    listener.mouseClicked(e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    listener.mouseReleased(e);
                    break;
            }

        }
        
        super.processMouseEvent(e);
        repaint();
    }
     
    
    
    protected void processMouseMotionEvent(MouseEvent e)
    {
        for(Iterator i=plugsLayer.iterator(); i.hasNext(); )
        {
            MouseMotionListener listener = (MouseMotionListener)i.next();
            switch(e.getID())
            {
                case MouseEvent.MOUSE_DRAGGED:
                    listener.mouseDragged(e);
                    break;
                case MouseEvent.MOUSE_MOVED:
                    listener.mouseMoved(e);
                    break;
            }
        }

        for(Iterator i=linksLayer.iterator(); i.hasNext(); )
        {
            MouseMotionListener listener = (MouseMotionListener)i.next();
            switch(e.getID())
            {
                case MouseEvent.MOUSE_DRAGGED:
                    listener.mouseDragged(e);
                    break;
                case MouseEvent.MOUSE_MOVED:
                    listener.mouseMoved(e);
                    break;
            }
        }
        
        for(Iterator i=devicesLayer.iterator(); i.hasNext(); )
        {
            MouseMotionListener listener = (MouseMotionListener)i.next();
            switch(e.getID())
            {
                case MouseEvent.MOUSE_DRAGGED:
                    listener.mouseDragged(e);
                    break;
                case MouseEvent.MOUSE_MOVED:
                    listener.mouseMoved(e);
                    break;
            }
        }
        
        super.processMouseMotionEvent(e);
        repaint();
    }

    
    
  /*  public void addMouseListener(MouseListener mouseListener)
    {
        
    }
   */
    
    
    public Collection<NetworkShape> getDevicesLayer()
    {
        return devicesLayer;
    }


    public void putOnDevicesLayer(NetworkShape shape)
    {
        devicesLayer.add(shape);
    }

        
    public Collection<NetworkLink> getLinksLayer()
    {
        return linksLayer;
    }


    public void putOnLinkLayer(NetworkLink link)
    {
        linksLayer.add(link);
    }

    
    public Collection<PlugNetworkShape> getPlugsLayer()
    {
        return plugsLayer;
    }

    
    public void putOnPlugLayer(PlugNetworkShape shape)
    {
        plugsLayer.add(shape);
    }
    
    
    public Collection<SocketNetworkShape> getSocketsLayer()
    {
        return socketsLayer;
    }
    
    
    public void putOnSocketLayer(SocketNetworkShape shape)
    {
        socketsLayer.add(shape);
    }
    

    public String getDescription()
    {
        return description;
    }


    public String getAuthor()
    {
        return author;
    }


    public String getCreateDate()
    {
        return createDate;
    }
    
    
    public void setAuthor(String author)
    {
        this.author = author;
        isSaved = false;
    }
    
    
    public void setDescription(String description)
    {
        this.description = description;
        isSaved = false;
    }
    

    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
        isSaved = false;
    }
    
    
    
    public IdGenerator getIdGenerator()
    {
        return idGenerator;
    }

    
    
    public SocketNetworkShape getSocketById(int id)
    {
        for(SocketNetworkShape socket : getSocketsLayer())
        {
            if(socket.getId() == id)
            {
                return socket;
            }
        }
        return null;
    }
    
    
    
    public int getDevicesCount()
    {
        return devicesLayer.size();
    }



    public int getLinksCount()
    {
        return linksLayer.size();
    }

    
    public void run()
    {
        while(true)
        {
            if(requestForRepaint)
            {
                super.repaint();
                requestForRepaint = false;
            }
            
            try
            {
                Thread.sleep(20);
            }catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }
    }

    
    public void repaint()
    {
        requestForRepaint = true;
    }
    
    
    public Frame getMainFrame()
    {
        return mainFrame;
    }
    
    
    
    /**
     * Gets the bounds of the content of this panel in the form of  
     * a Rectangle object.
     * @return a rectangle indicating this panel content's bounds
     */
    public Rectangle getContentBounds()
    {
        final int gap = 30;
        int x1=0, y1=0, x2=0, y2=0;
        int x[] = { 0 }, y[] = { 0 };
        Collection<NetworkShape> c = getDevicesLayer();
        
        if( c.size() > 0 )
        {
            x = new int[ c.size() ];
            y = new int[ c.size() ];
        }
        
        int i = 0;
        for( NetworkShape shape : c )
        {
            x[i] = (int)shape.getMinX(); 
            x[i] = (int)shape.getMaxX();
            y[i] = (int)shape.getMinY();
            y[i] = (int)shape.getMaxY();
            i++;
        }
        
        Arrays.sort(x);
        Arrays.sort(y);
        
        x1 = x[0] - gap; x2 = x[ x.length - 1 ] + gap;
        y1 = y[0] - gap; y2 = y[ y.length - 1 ] + gap;
        
        return new Rectangle(x1, y1, x2-x1, y2-y1);
    }

    
    
    @Override
    public Dimension getPreferredSize()
    {
        Rectangle bounds = getContentBounds();
        return new Dimension( bounds.x + bounds.width, bounds.y + bounds.height );
    }

}

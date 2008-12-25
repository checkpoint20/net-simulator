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
import org.netsimulator.net.NetworkDevice;
import org.netsimulator.net.Packet;
import org.netsimulator.net.PhisicalLinkSetUpListener;
import org.netsimulator.net.TooManyConnectionsException;
import org.netsimulator.net.TransferPacketListener;

public class SocketNetworkShape 
        extends NetworkShape
        implements PhisicalLinkSetUpListener, TransferPacketListener
{
    private NetworkDevice device = null;
    
    private static Image image;
    private static Image selectedImage;
    private static Image hilightedImage;

    private static Image lightOnImage;
    private static Image lightOffImage;
    
    
    /**
     * During this timeout in milliseconds the light is dark 
     * when it is blinking.
     */
    private static int blinkTimeout; 
    
    private boolean rxDark = true;
    private boolean txDark = true;
    private MediaTracker tracker;
    private NetworkPanel panel;
    private PlugNetworkShape connectedPlug = null;
    

    static
    {
        blinkTimeout = 100;
        
	image = Toolkit.getDefaultToolkit().getImage("img/socket.gif");
	selectedImage = Toolkit.getDefaultToolkit().getImage("img/socket_s.gif");
	hilightedImage = Toolkit.getDefaultToolkit().getImage("img/socket_h.gif");
	lightOnImage = Toolkit.getDefaultToolkit().getImage("img/light_on.gif");
	lightOffImage = Toolkit.getDefaultToolkit().getImage("img/light_off.gif");
    }

    
    
    
    /** Creates a new instance of Socket */
    public SocketNetworkShape(NetworkDevice device, NetworkPanel panel)
    throws InterruptedException
    {
        this(device, panel, panel.getIdGenerator().getNextId());
    }
    

    
    /** Creates a new instance of Socket */
    public SocketNetworkShape(NetworkDevice device, NetworkPanel panel, int id)
    throws InterruptedException
    {
        this.panel = panel;
        this.id = id;

        tracker = new MediaTracker(panel);
        tracker.addImage(image, 0);
        tracker.addImage(selectedImage, 1);
        tracker.addImage(hilightedImage, 2);
        tracker.addImage(lightOnImage, 3);
        tracker.addImage(lightOffImage, 4);
        tracker.waitForAll();
        
        setSize(image.getWidth(null), image.getHeight(null));
        this.device = device;

        device.addTransferPacketListener(this);
    }
    
   
    
    public int getId()
    {
        return id;
    }
    
    
    
    public void show(Graphics2D g2d)
    {
       // System.out.printf("isSelected %b, isHilighted %b.\n", isSelected, isHilighted);
        if(rxDark)
        {
            g2d.drawImage(lightOffImage, x, y-5, null);
        }else
        {
            g2d.drawImage(lightOnImage, x, y-5, null);
            
        }

        if(txDark)
        {
            g2d.drawImage(lightOffImage, x, y-10, null);
        }else
        {
            g2d.drawImage(lightOnImage, x, y-10, null);
            
        }
        
        if(isSelected){ g2d.drawImage(selectedImage, x, y, null); }
        if(isHilighted){ g2d.drawImage(hilightedImage, x, y, null); }
        if(!isSelected && !isHilighted){ g2d.drawImage(image, x, y, null); }
    }
    
    public NetworkDevice getNetworkDevice()
    {
        return device;
    }
  
    
    public synchronized void connectPlug(PlugNetworkShape plug)
    {
        if(connectedPlug!=null)
        { 
            return; 
        }
        
        connectedPlug = plug;

        connectedPlug.getNetworkLink().getMedia().
                addPhisicalLinkSetUpListener(this);

        try
        {
            connectedPlug.getNetworkLink().getMedia().connectToDevice(getNetworkDevice());
        }catch(TooManyConnectionsException tmce)
        {
            tmce.printStackTrace();
        }

        
   /*     if(connectedPlug.getNetworkLink().getMedia().getCountConnectedDevices()>1)
        {
            rxDark = false;
            txDark = false;
        }else
        {
            rxDark = true;
            txDark = true;
        }
    */
    }
    
    
    
    
    public synchronized void disconnectPlug()
    {
        rxDark = true;
        txDark = true;
        connectedPlug.getNetworkLink().getMedia().
                removePhisicalLinkSetUpListener(this);

        connectedPlug.getNetworkLink().getMedia().
                disconnectFromDevice(getNetworkDevice());

        connectedPlug.disconnectSocket();
        
        connectedPlug = null;
    }

    
    
    
    public void setLocation(int x, int y)
    {
        if(connectedPlug!=null)
        {
            connectedPlug.moveInto(x, y);
        }
        
        super.setLocation(x, y);
    }
    
    
    
    
    public synchronized boolean isConnected()
    {
        return (connectedPlug!=null);
    }
    
    
    
    
    public void BlinkRxLight()
    {
        if(rxDark || !isConnected()){ return; }
        
        rxDark = true;
        (new Blinker(blinkTimeout, this, Blinker.RX)).start();
    }

    
    
    public void BlinkTxLight()
    {
        if(txDark || !isConnected()){ return; }
        
        txDark = true;
        (new Blinker(blinkTimeout, this, Blinker.TX)).start();
    }
    
    
    
    void SwitchRxLightOn()
    {
        rxDark = false;
    }
    

    void SwitchTxLightOn()
    {
        txDark = false;
    }
    

    public void PhisicalLinkSetUp()
    {
        txDark = false;
        rxDark = false;
    }
    
    public void PhisicalLinkBrokenDown()
    {
        txDark = true;
        rxDark = true;
    }

    
    public void PacketTransfered(Packet packet)
    {
    }

    public void PacketTransmitted(Packet packet)
    {
        BlinkTxLight();
    }

    public void PacketReceived(Packet packet)
    {
        BlinkRxLight();
    }
    
}

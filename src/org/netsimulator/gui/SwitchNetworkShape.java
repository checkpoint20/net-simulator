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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.netsimulator.net.NetworkDeviceHolder;
import org.netsimulator.net.Switch;
import org.netsimulator.term.ShowMACAddressesTableCLICommand;
import org.netsimulator.term.TerminalDialog;
import org.netsimulator.util.ShapeInfo;
 


public class SwitchNetworkShape 
        extends NetworkShape
        implements ActionListener, SocketsHolder
{
    private static final DesktopPropertiesDialog propertiesDialog = new DesktopPropertiesDialog(null, true);
    private static final ResourceBundle rsc = ResourceBundle.getBundle("netsimulator", Locale.getDefault());
    private static String skin;
    private static ShapeInfo shapeInfo;
    private static Image image;
    private static Image selectedImage;
    private static Image hilightedImage;
    
    private JPopupMenu popup = new JPopupMenu();
    private JMenuItem properties_menu_item;
    private JMenuItem terminal_menu_item;
    private JMenuItem delete_menu_item;
    private Switch _switch_ = null;
    private ArrayList<SocketNetworkShape> sockets = null;        
    private TerminalDialog terminalDialog = null;    
    private MediaTracker tracker;
    private NetworkPanel panel;
    private String name;
    private String comment;
    private boolean popupShown = false;
    private int popupX;
    private int popupY;
    
    private int last_x=0, last_y=0;


    /**
     * Create Switch shape with n ports and id
     */    
    public SwitchNetworkShape(NetworkPanel panel, int portsCount, int id) 
    throws InterruptedException
    {
        this(panel, id);
        
        _switch_ = new Switch(panel.getIdGenerator(), portsCount);
       
        sockets = new ArrayList<SocketNetworkShape>();
        
        for(int i=0; i!=_switch_.getPortsCount(); i++)
        {
            SocketNetworkShape socket = 
                    new SocketNetworkShape(_switch_.getPort(i), panel);
            sockets.add(socket);
            panel.putOnSocketLayer(socket);
        }        
        
        terminalDialog.getTerminal().
                addCommand(new ShowMACAddressesTableCLICommand(_switch_));        
    }
   


    
    
    /**
     * Create "empty" Switch shape with id
     */    
    public SwitchNetworkShape(NetworkPanel panel, int id) 
    throws InterruptedException
    {
        this.panel = panel;
        this.id = id;
        
        tracker = new MediaTracker(panel);
        tracker.addImage(image, 0);
        tracker.addImage(selectedImage, 1);
        tracker.waitForAll();

        properties_menu_item = new JMenuItem(rsc.getString("Properties")+"...");
        terminal_menu_item = new JMenuItem(rsc.getString("Terminal")+"...");
        delete_menu_item = new JMenuItem(rsc.getString("Delete"));       
        
        properties_menu_item.addActionListener(this);
        popup.add(properties_menu_item);
        terminal_menu_item.addActionListener(this);
        popup.add(terminal_menu_item);
        popup.addSeparator();
        delete_menu_item.addActionListener(this);
        popup.add(delete_menu_item);
        
        setSize(image.getWidth(null), image.getHeight(null));
        setLocation(20, 20);
	
        sockets = new ArrayList<SocketNetworkShape>();
        
        terminalDialog = new TerminalDialog(panel.getMainFrame());
    }
    
 
    
    


    public static void setSkin(String skin, ShapeInfo shapeInfo)
    {
        SwitchNetworkShape.skin = skin;
        SwitchNetworkShape.shapeInfo = shapeInfo;
        
        image = Toolkit.getDefaultToolkit().createImage("img/"+skin+"/switch.gif");
        selectedImage = Toolkit.getDefaultToolkit().createImage("img/"+skin+"/switch_s.gif");
        hilightedImage = Toolkit.getDefaultToolkit().createImage("img/"+skin+"/switch_h.gif");
    }

    
    
    
    
    
    public void setSwitch(Switch _switch_)
    {
        this._switch_ = _switch_;
        
        //terminalDialog = new TerminalDialog(panel.getMainFrame());
        terminalDialog.getTerminal().
                addCommand(new ShowMACAddressesTableCLICommand(_switch_));        
    }
    
    
    
    
    
    
    public void show(Graphics2D g2d)
    {
        if(isSelected) g2d.drawImage(selectedImage, x, y, null);
        if(!isSelected) g2d.drawImage(image, x, y, null);
        
        int j=0;
        for(Iterator<SocketNetworkShape> i = sockets.iterator(); i.hasNext(); )
        {
            SocketNetworkShape buf = i.next();
            buf.setLocation(
                    x+shapeInfo.getSocketsX()+shapeInfo.getSocketsStep()*j++,
                    y+shapeInfo.getSocketsY());
            buf.show(g2d);
        }
        
        g2d.drawString(name, x, y-5);
    }
    
    
    
    
    public void mousePressed(MouseEvent e)
    {
        if(contains(e.getX(), e.getY()) &&
           !e.isConsumed())
        {
            /* Popup menus are triggered differently on different systems.
             * Therefore, isPopupTrigger  should be checked in both 
             * mousePressed  and mouseReleased  for proper cross-platform 
             * functionality. (from JavaDoc)
             */
            if(e.isPopupTrigger())
            {
                processMouseEventWhenPopupTriggerIsTrue(e);
            }
            last_x = x - e.getX();
            last_y = y - e.getY();
            selectedShape = this;
            setSelected(true);
            e.consume();
            return;
        }else
        {
            if(isSelected)
            {
                selectedShape = null;
                setSelected(false);
            }
        }
    }
    
    
    public void mouseDragged(MouseEvent e)
    {
        boolean isIntersects = false;
        
        if(isSelected && selectedShape==this)
        {
            int org_x = x, org_y = y;
            selectedShape.setLocation(e.getX()+last_x,
                                      e.getY()+last_y);

            for(NetworkShape dev : panel.getDevicesLayer())
            {
                if(!this.equals(dev) && intersects(dev))
                {
                    isIntersects = true;
                }
            }
            
            //move shapes to previous position,
            //not permit shapes to intersects
            if(isIntersects)
            {
                selectedShape.setLocation(org_x, org_y);
            }
            
            panel.setSaved(false);            
        }
    }
    

    
    public void mouseClicked(MouseEvent e)
    {
        if( e.getClickCount()==2 && isSelected && selectedShape==this )
        {
            terminalDialog.setLocation( e.getX(), 
                                        e.getY() );
            terminalDialog.setVisible(true);
        }
    }    
    

    
    
    public void mouseReleased(MouseEvent e)
    {
        if(contains(e.getX(), e.getY()) &&
           !e.isConsumed())
        {
            /* Popup menus are triggered differently on different systems.
             * Therefore, isPopupTrigger  should be checked in both 
             * mousePressed  and mouseReleased  for proper cross-platform 
             * functionality. (from JavaDoc)
             */
            if(e.isPopupTrigger())
            {
                processMouseEventWhenPopupTriggerIsTrue(e);
            }       
        }
    }    
    
    
    
    
    public SocketNetworkShape[] getSocketShapes()
    {
        SocketNetworkShape buf[] = new SocketNetworkShape[sockets.size()];
        return sockets.toArray(buf);
    }  

    
    public void setName(String name)
    {
        this.name = name;
        //if(terminalDialog!=null)
        //{
            terminalDialog.setTitle(name);
        //}
    }
    
    
    public String getName()
    {
        return name;
    }

    
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    
    public void addSocket(SocketNetworkShape shape)
    {
        sockets.add(shape);
        panel.putOnSocketLayer(shape);
    }
           
    public Switch getSwitch()
    {
        return _switch_;
    }
    
    public String getComment()
    {
        return comment;
    }

    
    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource()==properties_menu_item && popupShown)
        {   
            propertiesDialog.setName(getName());
            propertiesDialog.setComment(getComment());
            propertiesDialog.setLocation( popupX, 
                                          popupY);
            propertiesDialog.setTitle(rsc.getString("Properties")+" - "+name);
            propertiesDialog.setVisible(true);
            
            if(propertiesDialog.pressedOk())
            {
                setName(propertiesDialog.getName());
                setComment(propertiesDialog.getComment());
                panel.setSaved(false);
            }
            propertiesDialog.clear();
        }

        if(e.getSource()==terminal_menu_item && popupShown)
        {
            terminalDialog.setLocation( popupX, 
                                        popupY);
            terminalDialog.setVisible(true);
        }
        
        
        if(e.getSource()==delete_menu_item && popupShown)
        {
            for(Iterator<SocketNetworkShape> i = sockets.iterator(); i.hasNext(); )
            {
                SocketNetworkShape socket = i.next();
                if(socket.isConnected())
                {
                    socket.disconnectPlug();
                }
                panel.deleteSocketShape(socket);
            }
            panel.deleteDeviceShape(this);
            terminalDialog.setVisible(false);
        }                
        
    }
 
    
    
    
    public NetworkDeviceHolder getNetworkDeviceHolder()
    {
        return _switch_;
    }
    
    
    
    private void processMouseEventWhenPopupTriggerIsTrue(MouseEvent e)
    {
        popup.show(e.getComponent(),
                   e.getX(), e.getY());
        popupShown = true;
        popupX = popup.getLocationOnScreen().x;
        popupY = popup.getLocationOnScreen().y;        
    }    
    
}



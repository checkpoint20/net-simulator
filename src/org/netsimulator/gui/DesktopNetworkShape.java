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
import org.netsimulator.net.IP4Router;
import org.netsimulator.net.Interface;
import org.netsimulator.net.NetworkDevice;
import org.netsimulator.net.NetworkDeviceHolder;
import org.netsimulator.net.Router;
import org.netsimulator.net.TooManyInterfacesException;
import org.netsimulator.term.ArpCLICommand;
import org.netsimulator.term.IfconfigCLICommand;
import org.netsimulator.term.PingCLICommand;
import org.netsimulator.term.RouteCLICommand;
import org.netsimulator.term.TerminalDialog;
import org.netsimulator.util.ShapeInfo;

public class DesktopNetworkShape
        extends NetworkShape
        implements ActionListener, SocketsHolder, RouterHolder {

    private static final DesktopPropertiesDialog propertiesDialog = new DesktopPropertiesDialog( null, true );
    private static final ResourceBundle rsc = ResourceBundle.getBundle( "netsimulator", Locale.getDefault() );
    private static String skin;
    private static ShapeInfo shapeInfo;
    private static Image image;
    private static Image selectedImage;
    private static Image hilightedImage;
    private JPopupMenu popup = new JPopupMenu();
    private JMenuItem properties_menu_item;
    private JMenuItem terminal_menu_item;
    private JMenuItem delete_menu_item;
    private IP4Router router = null;
    private SocketNetworkShape socket = null;
    private TerminalDialog terminalDialog = null;
    private MediaTracker tracker;
    private NetworkPanel panel;
    private String name;
    private String comment;
    private boolean popupShown = false;
    private int popupX;
    private int popupY;
    private int last_x = 0,  last_y = 0;

    public DesktopNetworkShape( NetworkPanel panel, int id )
            throws InterruptedException {
        this.panel = panel;
        this.id = id;

        properties_menu_item = new JMenuItem( rsc.getString( "Properties" ) + "..." );
        terminal_menu_item = new JMenuItem( rsc.getString( "Terminal" ) + "..." );
        delete_menu_item = new JMenuItem( rsc.getString( "Delete" ) );

        properties_menu_item.addActionListener( this );
        popup.add( properties_menu_item );
        terminal_menu_item.addActionListener( this );
        popup.add( terminal_menu_item );
        popup.addSeparator();
        delete_menu_item.addActionListener( this );
        popup.add( delete_menu_item );

        tracker = new MediaTracker( panel );
        tracker.addImage( image, 0 );
        tracker.addImage( selectedImage, 1 );
        tracker.waitForAll();

        setSize( image.getWidth( null ), image.getHeight( null ) );
        setLocation( 20, 20 );

        terminalDialog = new TerminalDialog( panel.getMainFrame() );
    }

    public DesktopNetworkShape( NetworkPanel panel, int ifsCount, int id )
            throws InterruptedException {
        this( panel, id );

        try {
            router = new IP4Router( panel.getIdGenerator(), ifsCount );
        } catch( TooManyInterfacesException tmie ) {
            tmie.printStackTrace();
        }

        for( Interface ifs : router.getInterfaces() ) {
            if( ifs instanceof NetworkDevice ) {
                socket = new SocketNetworkShape( (NetworkDevice) ifs, panel );
                panel.putOnSocketLayer( socket );
                break; // we need only one interface, use the first
            }
        }

        terminalDialog = new TerminalDialog( panel.getMainFrame() );

        terminalDialog.getTerminal().
                addCommand( new ArpCLICommand( terminalDialog.getTerminal(), router ) );
        terminalDialog.getTerminal().
                addCommand( new IfconfigCLICommand( terminalDialog.getTerminal(), router ) );
        terminalDialog.getTerminal().
                addCommand( new RouteCLICommand( terminalDialog.getTerminal(), router ) );
        terminalDialog.getTerminal().
                addCommand( new PingCLICommand( terminalDialog.getTerminal(), router ) );
    }

    public DesktopNetworkShape( NetworkPanel panel, Router router, int id )
            throws InterruptedException {
        this( panel, id );
        setRouter( router );
    }

    /*   
    public static void setResourceBundle(ResourceBundle rsc)
    {
    DesktopNetworkShape.rsc = rsc;
    properties_menu_item = new JMenuItem("Свойства...");
    terminal_menu_item = new JMenuItem("Терминал...");
    JMenuItem delete_menu_item = new JMenuItem("Удалить");       
    properties_menu_item.addActionListener(this);
    popup.add(properties_menu_item);
    terminal_menu_item.addActionListener(this);
    popup.add(terminal_menu_item);
    popup.addSeparator();
    delete_menu_item.addActionListener(this);
    popup.add(delete_menu_item);
    }
     */
    public static void setSkin( String skin, ShapeInfo shapeInfo ) {
        DesktopNetworkShape.skin = skin;
        DesktopNetworkShape.shapeInfo = shapeInfo;

        image = Toolkit.getDefaultToolkit().createImage( "img/" + skin + "/desktop.gif" );
        selectedImage = Toolkit.getDefaultToolkit().createImage( "img/" + skin + "/desktop_s.gif" );
        hilightedImage = Toolkit.getDefaultToolkit().createImage( "img/" + skin + "/desktop_h.gif" );
    }

    public void setRouter( Router router ) {
        this.router = (IP4Router) router;

        terminalDialog.getTerminal().
                addCommand( new ArpCLICommand( terminalDialog.getTerminal(), this.router ) );
        terminalDialog.getTerminal().
                addCommand( new IfconfigCLICommand( terminalDialog.getTerminal(), this.router ) );
        terminalDialog.getTerminal().
                addCommand( new RouteCLICommand( terminalDialog.getTerminal(), this.router ) );
        terminalDialog.getTerminal().
                addCommand( new PingCLICommand( terminalDialog.getTerminal(), this.router ) );

    }

    public IP4Router getRouter() {
        return router;
    }

    public void show( Graphics2D g2d ) {
        if( isSelected ) {
            g2d.drawImage( selectedImage, x, y, null );
        }
        if( !isSelected ) {
            g2d.drawImage( image, x, y, null );
        }

        if( socket != null ) {
            socket.setLocation(
                    x + shapeInfo.getSocketsX(),
                    y + shapeInfo.getSocketsY() );
            socket.show( g2d );
        }

        g2d.drawString( name, x, y - 5 );
    }

    public void mousePressed( MouseEvent e ) {
        if( contains( e.getX(), e.getY() ) &&
                !e.isConsumed() ) {
            /* Popup menus are triggered differently on different systems.
             * Therefore, isPopupTrigger  should be checked in both 
             * mousePressed  and mouseReleased  for proper cross-platform 
             * functionality. (from JavaDoc)
             */
            if( e.isPopupTrigger() ) {
                processMouseEventWhenPopupTriggerIsTrue( e );
            }
            last_x = x - e.getX();
            last_y = y - e.getY();
            selectedShape = this;
            setSelected( true );
            e.consume();
            return;
        } else {
            if( isSelected ) {
                selectedShape = null;
                setSelected( false );
            }
        }
    }

    public void mouseDragged( MouseEvent e ) {
        boolean isIntersects = false;

        if( isSelected && selectedShape == this ) {
            int org_x = x, org_y = y;
            selectedShape.setLocation( e.getX() + last_x,
                    e.getY() + last_y );

            for( NetworkShape dev : panel.getDevicesLayer() ) {
                if( !this.equals( dev ) && intersects( dev ) ) {
                    isIntersects = true;
                }
            }

            //move shapes to previous position,
            //not permit shapes to intersects
            if( isIntersects ) {
                selectedShape.setLocation( org_x, org_y );
            }

            panel.setSaved( false );
        }
    }

    public void mouseClicked( MouseEvent e ) {
        if( e.getClickCount() == 2 && isSelected && selectedShape == this ) {
            terminalDialog.setLocation( e.getX(),
                    e.getY() );
            terminalDialog.setVisible( true );
        }
    }

    public void mouseReleased( MouseEvent e ) {
        if( contains( e.getX(), e.getY() ) &&
                !e.isConsumed() ) {
            /* Popup menus are triggered differently on different systems.
             * Therefore, isPopupTrigger  should be checked in both 
             * mousePressed  and mouseReleased  for proper cross-platform 
             * functionality. (from JavaDoc)
             */
            if( e.isPopupTrigger() ) {
                processMouseEventWhenPopupTriggerIsTrue( e );
            }
        }
    }

    public void addSocket( SocketNetworkShape shape ) {
        socket = shape;
        panel.putOnSocketLayer( shape );
    }

    public SocketNetworkShape getSocketShape() {
        return socket;
    }

    public SocketNetworkShape[] getSocketShapes() {
        SocketNetworkShape buf[] = new SocketNetworkShape[1];
        buf[0] = socket;
        return buf;
    }

    public void setName( String name ) {
        this.name = name;
        terminalDialog.setTitle( name );
    }

    public String getName() {
        return name;
    }

    public void setComment( String comment ) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void actionPerformed( ActionEvent e ) {
        if( e.getSource() == properties_menu_item && popupShown ) {
            propertiesDialog.setName( getName() );
            propertiesDialog.setComment( getComment() );
            propertiesDialog.setLocation( popupX,
                    popupY );
            propertiesDialog.setTitle( rsc.getString( "Properties" ) + " - " + name );
            propertiesDialog.setVisible( true );

            if( propertiesDialog.pressedOk() ) {
                setName( propertiesDialog.getName() );
                setComment( propertiesDialog.getComment() );
                panel.setSaved( false );
            }
            propertiesDialog.clear();
        }

        if( e.getSource() == terminal_menu_item && popupShown ) {
            terminalDialog.setLocation( popupX,
                    popupY );
            terminalDialog.setVisible( true );
            panel.setSaved( false );
        }

        if( e.getSource() == delete_menu_item && popupShown ) {
            if( socket.isConnected() ) {
                socket.disconnectPlug();
            }
            panel.deleteSocketShape( socket );
            panel.deleteDeviceShape( this );
            terminalDialog.setVisible( false );
        }

        popupShown = false;
    }

    public NetworkDeviceHolder getNetworkDeviceHolder() {
        return router;
    }

    private void processMouseEventWhenPopupTriggerIsTrue( MouseEvent e ) {
        popup.show( e.getComponent(),
                e.getX(), e.getY() );
        popupShown = true;
        popupX = popup.getLocationOnScreen().x;
        popupY = popup.getLocationOnScreen().y;
    }
    
    @Override
    public String toString() {
        return "DesktopNetworkShape{name=" + name + '}';
    }
}



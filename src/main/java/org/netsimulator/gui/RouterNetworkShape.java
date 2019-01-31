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

import org.netsimulator.net.*;
import org.netsimulator.term.*;
import org.netsimulator.util.ShapeInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

public class RouterNetworkShape
        extends NetworkShape
        implements ActionListener, SocketsHolder, RouterHolder {

    private static final DesktopPropertiesDialog propertiesDialog = new DesktopPropertiesDialog(null, true);
    private static final ResourceBundle rsc = ResourceBundle.getBundle("netsimulator", Locale.getDefault());
    private static ShapeInfo shapeInfo;
    private static Image image;
    private static Image selectedImage;
    private JPopupMenu popup = new JPopupMenu();
    private JMenuItem properties_menu_item;
    private JMenuItem terminal_menu_item;
    private JMenuItem delete_menu_item;
    private IP4Router router = null;
    private ArrayList<SocketNetworkShape> sockets = null;
    private TerminalDialog terminalDialog = null;
    private NetworkPanel panel;
    private String name;
    private String comment;
    private boolean popupShown = false;
    private int popupX;
    private int popupY;
    private int last_x = 0, last_y = 0;

    public RouterNetworkShape(NetworkPanel panel, int id)
            throws InterruptedException {
        this.panel = panel;
        this.id = id;

        MediaTracker tracker = new MediaTracker(panel);
        tracker.addImage(image, 0);
        tracker.addImage(selectedImage, 1);
        tracker.waitForAll();

        properties_menu_item = new JMenuItem(rsc.getString("Properties") + "...");
        terminal_menu_item = new JMenuItem(rsc.getString("Terminal") + "...");
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

        terminalDialog = new TerminalDialog(panel.getMainFrame());
        sockets = new ArrayList<SocketNetworkShape>();
    }

    public RouterNetworkShape(NetworkPanel panel, int ifsCount, int id)
            throws InterruptedException {
        this(panel, id);

        try {
            router = new IP4Router(panel.getIdGenerator(), ifsCount);
        } catch (TooManyInterfacesException tmie) {
            tmie.printStackTrace();
        }

        SocketNetworkShape socket;
        for (Interface ifs : router.getInterfaces()) {
            if (ifs instanceof NetworkDevice) {
                socket = new SocketNetworkShape((NetworkDevice) ifs, panel);
                sockets.add(socket);
                panel.putOnSocketLayer(socket);
            }
        }

        terminalDialog.getTerminal().
                addCommand(new ArpCLICommand(router));
        terminalDialog.getTerminal().
                addCommand(new IfconfigCLICommand(router));
        terminalDialog.getTerminal().
                addCommand(new RouteCLICommand(router));
        terminalDialog.getTerminal().
                addCommand(new PingCLICommand(router));
    }

    public RouterNetworkShape(NetworkPanel panel, Router router, int id)
            throws InterruptedException {
        this(panel, id);
        setRouter(router);
    }

    public static void setSkin(String skin, ShapeInfo shapeInfo) {
        RouterNetworkShape.shapeInfo = shapeInfo;

        image = createImageFromResource("/img/" + skin + "/router.gif");
        selectedImage = createImageFromResource("/img/" + skin + "/router_s.gif");
    }

    public void setRouter(Router router) {
        this.router = (IP4Router) router;

        terminalDialog.getTerminal().
                addCommand(new ArpCLICommand(this.router));
        terminalDialog.getTerminal().
                addCommand(new IfconfigCLICommand(this.router));
        terminalDialog.getTerminal().
                addCommand(new RouteCLICommand(this.router));
        terminalDialog.getTerminal().
                addCommand(new PingCLICommand(this.router));

    }

    public void show(Graphics2D g2d) {
        if (isSelected) {
            g2d.drawImage(selectedImage, x, y, null);
        }
        if (!isSelected) {
            g2d.drawImage(image, x, y, null);
        }

        int j = 0;
        for (Iterator<SocketNetworkShape> i = sockets.iterator(); i.hasNext(); ) {
            SocketNetworkShape buf = i.next();
            buf.setLocation(
                    x + shapeInfo.getSocketsX() + shapeInfo.getSocketsStep() * j++,
                    y + shapeInfo.getSocketsY());
            buf.show(g2d);
        }

        g2d.drawString(name, x, y - 5);
    }

    public void mousePressed(MouseEvent e) {
        if (contains(e.getX(), e.getY()) &&
                !e.isConsumed()) {
            /* Popup menus are triggered differently on different systems.
             * Therefore, isPopupTrigger  should be checked in both
             * mousePressed  and mouseReleased  for proper cross-platform
             * functionality. (from JavaDoc)
             */
            if (e.isPopupTrigger()) {
                processMouseEventWhenPopupTriggerIsTrue(e);
            }
            last_x = x - e.getX();
            last_y = y - e.getY();
            selectedShape = this;
            setSelected(true);
            e.consume();
            return;
        } else {
            if (isSelected) {
                selectedShape = null;
                setSelected(false);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        boolean isIntersects = false;

        if (isSelected && selectedShape == this) {
            int org_x = x, org_y = y;
            selectedShape.setLocation(e.getX() + last_x,
                    e.getY() + last_y);

            for (NetworkShape dev : panel.getDevicesLayer()) {
                if (!this.equals(dev) && intersects(dev)) {
                    isIntersects = true;
                }
            }

            //move shapes to previous position,
            //not permit shapes to intersects
            if (isIntersects) {
                selectedShape.setLocation(org_x, org_y);
            }
            panel.setSaved(false);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && isSelected && selectedShape == this) {
            terminalDialog.setLocation(e.getX(),
                    e.getY());
            terminalDialog.setVisible(true);
            terminalDialog.toFront();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (contains(e.getX(), e.getY()) &&
                !e.isConsumed()) {
            /* Popup menus are triggered differently on different systems.
             * Therefore, isPopupTrigger  should be checked in both
             * mousePressed  and mouseReleased  for proper cross-platform
             * functionality. (from JavaDoc)
             */
            if (e.isPopupTrigger()) {
                processMouseEventWhenPopupTriggerIsTrue(e);
            }
        }
    }

    public SocketNetworkShape[] getSocketShapes() {
        SocketNetworkShape buf[] = new SocketNetworkShape[sockets.size()];
        return sockets.toArray(buf);
    }

    public void addSocket(SocketNetworkShape shape) {
        sockets.add(shape);
        panel.putOnSocketLayer(shape);
    }

    public void setName(String name) {
        this.name = name;
        terminalDialog.setTitle(name);
    }

    public String getName() {
        return name;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public IP4Router getRouter() {
        return router;
    }

    public String getComment() {
        return comment;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == properties_menu_item && popupShown) {
            propertiesDialog.setName(getName());
            propertiesDialog.setComment(getComment());
            propertiesDialog.setLocation(popupX,
                    popupY);
            propertiesDialog.setTitle(rsc.getString("Properties") + " - " + name);
            propertiesDialog.setVisible(true);

            if (propertiesDialog.pressedOk()) {
                setName(propertiesDialog.getName());
                setComment(propertiesDialog.getComment());
                panel.setSaved(false);
            }
            propertiesDialog.clear();
        }

        if (e.getSource() == terminal_menu_item && popupShown) {
            terminalDialog.setLocation(popupX,
                    popupY);
            terminalDialog.setVisible(true);
        }

        if (e.getSource() == delete_menu_item && popupShown) {
            for (Iterator<SocketNetworkShape> i = sockets.iterator(); i.hasNext(); ) {
                SocketNetworkShape socket = i.next();
                if (socket.isConnected()) {
                    socket.disconnectPlug();
                }
                panel.deleteSocketShape(socket);
            }
            panel.deleteDeviceShape(this);
            terminalDialog.setVisible(false);
        }

        popupShown = false;
    }

    public NetworkDeviceHolder getNetworkDeviceHolder() {
        return router;
    }

    private void processMouseEventWhenPopupTriggerIsTrue(MouseEvent e) {
        popup.show(e.getComponent(),
                e.getX(), e.getY());
        popupShown = true;
        popupX = popup.getLocationOnScreen().x;
        popupY = popup.getLocationOnScreen().y;
    }

    @Override
    public String toString() {
        return "RouterNetworkShape{name=" + name + '}';
    }

}



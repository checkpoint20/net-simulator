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

import org.netsimulator.net.Hub;
import org.netsimulator.net.NetworkDeviceHolder;
import org.netsimulator.util.ShapeInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;


public class HubNetworkShape
        extends NetworkShape
        implements ActionListener, SocketsHolder {

    private static final DesktopPropertiesDialog propertiesDialog = new DesktopPropertiesDialog(null, true);
    private static final ResourceBundle rsc = ResourceBundle.getBundle("netsimulator", Locale.getDefault());
    private static ShapeInfo shapeInfo;
    private static Image image;
    private static Image selectedImage;
    private JPopupMenu popup = new JPopupMenu();
    private JMenuItem properties_menu_item;
    private JMenuItem delete_menu_item;
    private Hub hub = null;
    private ArrayList<SocketNetworkShape> sockets = null;
    private NetworkPanel panel;
    private String name;
    private String comment;
    private boolean popupShown = false;
    private int popupX;
    private int popupY;

    private int last_x = 0, last_y = 0;


    /**
     * Create HUB shape with n ports and id
     */
    public HubNetworkShape(NetworkPanel panel, int portsCount, int id)
            throws InterruptedException {
        this(panel, id);

        hub = new Hub(panel.getIdGenerator(), portsCount);

        for (int i = 0; i != hub.getPortsCount(); i++) {
            SocketNetworkShape socket =
                    new SocketNetworkShape(hub.getPort(i), panel);
            sockets.add(socket);
            panel.putOnSocketLayer(socket);
        }
    }


    /**
     * Create "empty" HUB shape with id
     */
    public HubNetworkShape(NetworkPanel panel, int id)
            throws InterruptedException {
        this.panel = panel;
        this.id = id;

        MediaTracker tracker = new MediaTracker(panel);
        tracker.addImage(image, 0);
        tracker.addImage(selectedImage, 1);
        tracker.waitForAll();

        properties_menu_item = new JMenuItem(rsc.getString("Properties") + "...");
        delete_menu_item = new JMenuItem(rsc.getString("Delete"));

        properties_menu_item.addActionListener(this);
        popup.add(properties_menu_item);
        popup.addSeparator();
        delete_menu_item.addActionListener(this);
        popup.add(delete_menu_item);

        setSize(image.getWidth(null), image.getHeight(null));
        setLocation(20, 20);

        sockets = new ArrayList<>();
    }


    public static void setSkin(String skin, ShapeInfo shapeInfo) {
        HubNetworkShape.shapeInfo = shapeInfo;

        image = createImageFromResource("/img/" + skin + "/hub.gif");
        selectedImage = createImageFromResource("/img/" + skin + "/hub_s.gif");
    }


    public void setHub(Hub hub) {
        this.hub = hub;
    }


    public void show(Graphics2D g2d) {
        if (isSelected) g2d.drawImage(selectedImage, x, y, null);
        if (!isSelected) g2d.drawImage(image, x, y, null);

        int j = 0;
        for (SocketNetworkShape buf : sockets) {
            buf.setLocation(
                    x + shapeInfo.getSocketsX() + shapeInfo.getSocketsStep() * j++,
                    y + shapeInfo.getSocketsY());
            buf.show(g2d);
        }

        g2d.drawString(name, x, y - 5);
    }


    public void mousePressed(MouseEvent e) {
        if (contains(e.getX(), e.getY()) && !e.isConsumed()) {
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
        } else if (isSelected) {
            selectedShape = null;
            setSelected(false);
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


    public void mouseReleased(MouseEvent e) {
        if (contains(e.getX(), e.getY()) && !e.isConsumed()) {
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
    }


    public String getName() {
        return name;
    }


    public void setComment(String comment) {
        this.comment = comment;
    }


    public Hub getHub() {
        return hub;
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

        if (e.getSource() == delete_menu_item && popupShown) {
            for (SocketNetworkShape socket : sockets) {
                if (socket.isConnected()) {
                    socket.disconnectPlug();
                }
                panel.deleteSocketShape(socket);
            }
            panel.deleteDeviceShape(this);
        }

        popupShown = false;
    }


    public NetworkDeviceHolder getNetworkDeviceHolder() {
        return hub;
    }


    private void processMouseEventWhenPopupTriggerIsTrue(MouseEvent e) {
        popup.show(e.getComponent(),
                e.getX(), e.getY());
        popupShown = true;
        popupX = popup.getLocationOnScreen().x;
        popupY = popup.getLocationOnScreen().y;
    }
}



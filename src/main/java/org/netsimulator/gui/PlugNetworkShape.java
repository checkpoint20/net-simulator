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
import java.awt.event.MouseEvent;


public class PlugNetworkShape extends NetworkShape {
    private static Image image;
    private static Image selectedImage;
    private MediaTracker tracker;
    private NetworkPanel panel;
    private NetworkLink link;
    private int point;
    private int last_x = 0, last_y = 0;
    private SocketNetworkShape connectedSocket = null;

    static {
        image = createImageFromResource("/img/plug.gif");
        selectedImage = createImageFromResource("/img/plug_s.gif");
    }


    public PlugNetworkShape(
            NetworkPanel panel,
            NetworkLink link,
            int point,
            int id) {
        this.panel = panel;
        this.link = link;
        this.point = point;
        this.id = id;

        tracker = new MediaTracker(panel);
        tracker.addImage(image, 0);
        tracker.addImage(selectedImage, 1);

        try {
            tracker.waitForAll();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        setSize(image.getWidth(null), image.getHeight(null));

        panel.repaint();
    }


    public void setCenterLocation(int x, int y) {
        setLocation(x - width / 2, y - height / 2);
    }

    public void show(Graphics2D g2d) {
        if (isSelected) g2d.drawImage(selectedImage, x, y, null);
        if (!isSelected) g2d.drawImage(image, x, y, null);
    }


    public void mousePressed(MouseEvent e) {
        if (contains(e.getX(), e.getY()) && !e.isConsumed()) {
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
        if (isSelected && selectedShape == this && connectedSocket == null) {
            setLocation(e.getX() + last_x,
                    e.getY() + last_y);
            switch (point) {
                case 1:
                    link.setP1((int) getCenterX(), (int) getCenterY());
                    break;
                case 2:
                    link.setP2((int) getCenterX(), (int) getCenterY());
                    break;
            }

            for (SocketNetworkShape socket : panel.getSocketsLayer()) {
                //System.out.println("in sokets loop");
                if (intersects(socket) && !socket.isConnected()) {
                    //   System.out.println("intersects!!!");
                    socket.setHighlighted(true);
                    connectedSocket = socket;
                    socket.connectPlug(this);
                    break;
                } else {
                    socket.setHighlighted(false);
                }
            }
            return;
        }


        if (isSelected && selectedShape == this && connectedSocket != null) {
            setLocation(e.getX() + last_x,
                    e.getY() + last_y);
            switch (point) {
                case 1:
                    link.setP1((int) getCenterX(), (int) getCenterY());
                    break;
                case 2:
                    link.setP2((int) getCenterX(), (int) getCenterY());
                    break;
            }

            if (!intersects(connectedSocket)) {
                connectedSocket.setHighlighted(false);
                connectedSocket.disconnectPlug();
                connectedSocket = null;
            }
            return;
        }
    }


    public SocketNetworkShape getConnectedSocket() {
        return connectedSocket;
    }


    public void setConnectedSocket(SocketNetworkShape socket) {
        this.connectedSocket = socket;
        socket.connectPlug(this);
    }


    public void disconnectSocket() {
        this.connectedSocket = null;
    }


    public int getPoint() {
        return point;
    }


    public void moveInto(int x, int y) {
        setLocation(x, y);
        switch (point) {
            case 1:
                link.setP1((int) getCenterX(), (int) getCenterY());
                break;
            case 2:
                link.setP2((int) getCenterX(), (int) getCenterY());
                break;
        }
    }


    public NetworkLink getNetworkLink() {
        return link;
    }
}



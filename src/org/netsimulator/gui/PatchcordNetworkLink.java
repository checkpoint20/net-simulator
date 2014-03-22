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
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import org.netsimulator.net.Media;
import org.netsimulator.net.Packet;
import org.netsimulator.net.TransferPacketListener;

public class PatchcordNetworkLink
        extends NetworkLink
        implements Faderable, TransferPacketListener, ActionListener {

    private static final Executor exec = 
            new ThreadPoolExecutor(20, 1000, 180L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());   
    private static final ResourceBundle rsc = ResourceBundle.getBundle("netsimulator", Locale.getDefault());
    private static BasicStroke stroke;
    private static BasicStroke selectedStroke;

    private NetworkPanel panel;
    private final JPopupMenu popup = new JPopupMenu();
    private JMenuItem delete_menu_item;
    private PlugNetworkShape plug1;
    private PlugNetworkShape plug2;
    private Media media;
    private volatile Color currentColor;
    private Fader fader;
    private boolean popupShown = false;

    private int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;

    static {
        stroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        selectedStroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    /**
     * Creates a new instance of PatchcordLink with 2 plugs
     */
    public PatchcordNetworkLink(NetworkPanel panel) {
        this(panel, panel.getIdGenerator().getNextId());

        setMedia(new Media(panel.getIdGenerator()));

        setPlug1(new PlugNetworkShape(panel, this, 1, panel.getIdGenerator().getNextId()));
        setPlug2(new PlugNetworkShape(panel, this, 2, panel.getIdGenerator().getNextId()));
    }

    /**
     * Creates a new "empty" instance of PatchcordLink
     */
    public PatchcordNetworkLink(NetworkPanel panel, int id) {
        this.panel = panel;
        this.id = id;

        currentColor = COLOR;
        gap = 2;
        setLine(20, 20, 100, 100);

        delete_menu_item = new JMenuItem(rsc.getString("Delete"));
        delete_menu_item.addActionListener(this);
        popup.add(delete_menu_item);

        panel.putOnLinkLayer(this);
        panel.repaint();
    }

    public final void setMedia(Media media) {
        this.media = media;
        this.media.addTrnasmitPacketListener(this);
    }

    public final void setPlug1(PlugNetworkShape plug) {
        plug1 = plug;
        panel.putOnPlugLayer(plug1);
    }

    public final void setPlug2(PlugNetworkShape plug) {
        plug2 = plug;
        panel.putOnPlugLayer(plug2);
    }

    @Override
    public void show(Graphics2D g2d) {
//        g2d.addRenderingHints(
//                new RenderingHints(RenderingHints.KEY_ANTIALIASING,
//                                   RenderingHints.VALUE_ANTIALIAS_ON));

        if (selected) {
            g2d.setStroke(selectedStroke);
        } else {
            g2d.setStroke(stroke);
        }

        g2d.setColor(currentColor);
        g2d.draw(this);

        if (plug1 != null && plug2 != null) {
            plug1.show(g2d);
            plug2.show(g2d);
        }
    }

    @Override
    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
        panel.repaint();
    }

    public PlugNetworkShape getPlug1() {
        return plug1;
    }

    public PlugNetworkShape getPlug2() {
        return plug2;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (plug1.isSelected() || plug2.isSelected()) {
            return;
        }

        if (contains(e.getX(), e.getY()) && !e.isConsumed()) {
            /* Popup menus are triggered differently on different systems.
             * Therefore, isPopupTrigger  should be checked in both 
             * mousePressed  and mouseReleased  for proper cross-platform 
             * functionality. (from JavaDoc)
             */
            if (e.isPopupTrigger()) {
                processMouseEventWhenPopupTriggerIsTrue(e);
            }

            dx1 = e.getX() - x1;
            dy1 = e.getY() - y1;
            dx2 = e.getX() - x2;
            dy2 = e.getY() - y2;

            selectedLink = this;
            setSelected(true);
            e.consume();
        } else {
            if (selected) {
                selectedLink = null;
                setSelected(false);
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (contains(e.getX(), e.getY())
                && !e.isConsumed()) {
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

    @Override
    public void mouseDragged(MouseEvent e) {
        plug1.mouseDragged(e);
        plug2.mouseDragged(e);

        if (plug1.isSelected() || plug2.isSelected()) {
            return;
        }

        if (selected && selectedLink == this) {
            setLine(e.getX() - dx1,
                    e.getY() - dy1,
                    e.getX() - dx2,
                    e.getY() - dy2);
            plug1.setCenterLocation(x1, y1);
            plug2.setCenterLocation(x2, y2);

            panel.setSaved(false);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (contains(e.getX(), e.getY())) {
            highlight();
        } else {
            lowlight();
        }
    }

    @Override
    public void highlight() {
        // Race condition is possible but it does not cause errors so ignore it.
        if(!highlighted) {
            highlighted = true;
            if (fader != null) {
                fader.cancel();
                fader = null;
            }
            currentColor = HILIGHTED_COLOR;
        }
    }
    
    @Override
    public void lowlight() {
        // Race condition is possible but it does not cause errors so ignore it.
        if(highlighted) {
            highlighted = false;
            if (fader != null) {
                fader.cancel();
                fader = null;
            }
            fader = new Fader(currentColor, this);
            exec.execute(fader);
        }
    }

    @Override
    public Media getMedia() {
        return media;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == delete_menu_item && popupShown) {
            media.disconnectAll();
            plug1.disconnectSocket();
            plug2.disconnectSocket();
            panel.deletePlug(plug1);
            panel.deletePlug(plug2);
            panel.deleteMedia(this);
        }

        popupShown = false;
    }

    @Override
    public void PacketTransfered(Packet packet) {
        highlight();
        lowlight();
    }

    @Override
    public void PacketTransmitted(Packet packet) {
    }

    @Override
    public void PacketReceived(Packet packet) {
    }

    private void processMouseEventWhenPopupTriggerIsTrue(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(), e.getY());
        popupShown = true;
    }

}

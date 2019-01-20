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

import java.awt.*;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketNetworkShape
        extends NetworkShape
        implements PhysicalLinkSetUpListener, TransferPacketListener {

    private static final Logger LOGGER
            = Logger.getLogger(Blinker.class.getName());
    
    private static final Timer TIMER = new Timer("BlinkerThread", true);

    private NetworkDevice device = null;

    private static final Image IMAGE = Toolkit.getDefaultToolkit().getImage("img/socket.gif");
    private static final Image SELECTED_IMAGE = Toolkit.getDefaultToolkit().getImage("img/socket_s.gif");
    private static final Image HILIGHTED_IMAGE = Toolkit.getDefaultToolkit().getImage("img/socket_h.gif");

    private static final Image LIGHT_ON_IMAGE = Toolkit.getDefaultToolkit().getImage("img/light_on.gif");
    private static final Image LIGHT_OFF_IMAGE = Toolkit.getDefaultToolkit().getImage("img/light_off.gif");
    
    
    /**
     * During this timeout in milliseconds the light is dark 
     * when it is blinking.
     */
    private static final int BLINK_TIMEOUT = 100;

    private boolean rxDark = true;
    private boolean txDark = true;
    private MediaTracker tracker;
    private PlugNetworkShape connectedPlug = null;

    /**
     * Creates a new instance of Socket
     * @param device
     * @param panel
     * @throws java.lang.InterruptedException
     */
    public SocketNetworkShape(NetworkDevice device, NetworkPanel panel)
            throws InterruptedException {
        this(device, panel, panel.getIdGenerator().getNextId());
    }

    /**
     * Creates a new instance of Socket
     * @param device
     * @param panel
     * @param id
     * @throws java.lang.InterruptedException
     */
    public SocketNetworkShape(NetworkDevice device, NetworkPanel panel, int id)
            throws InterruptedException {
        this.id = id;

        tracker = new MediaTracker(panel);
        tracker.addImage(IMAGE, 0);
        tracker.addImage(SELECTED_IMAGE, 1);
        tracker.addImage(HILIGHTED_IMAGE, 2);
        tracker.addImage(LIGHT_ON_IMAGE, 3);
        tracker.addImage(LIGHT_OFF_IMAGE, 4);
        tracker.waitForAll();

        setSize(IMAGE.getWidth(null), IMAGE.getHeight(null));
        this.device = device;

        device.addTransferPacketListener(this);
    }

    @Override
    public int getId() {
        return id;
    }

    public void show(Graphics2D g2d) {
        // System.out.printf("isSelected %b, isHilighted %b.\n", isSelected, isHilighted);
        if (rxDark) {
            g2d.drawImage(LIGHT_OFF_IMAGE, x, y - 5, null);
        } else {
            g2d.drawImage(LIGHT_ON_IMAGE, x, y - 5, null);

        }

        if (txDark) {
            g2d.drawImage(LIGHT_OFF_IMAGE, x, y - 10, null);
        } else {
            g2d.drawImage(LIGHT_ON_IMAGE, x, y - 10, null);

        }

        if (isSelected) {
            g2d.drawImage(SELECTED_IMAGE, x, y, null);
        }
        if (isHilighted) {
            g2d.drawImage(HILIGHTED_IMAGE, x, y, null);
        }
        if (!isSelected && !isHilighted) {
            g2d.drawImage(IMAGE, x, y, null);
        }
    }

    public NetworkDevice getNetworkDevice() {
        return device;
    }

    public synchronized void connectPlug(PlugNetworkShape plug) {
        if (connectedPlug == null) {
            connectedPlug = plug;
            connectedPlug.getNetworkLink().getMedia().addPhysicalLinkSetUpListener(this);
            try {
                connectedPlug.getNetworkLink().getMedia().connectToDevice(getNetworkDevice());
            } catch (TooManyConnectionsException tmce) {
                LOGGER.log(Level.SEVERE, "Unexpected exception.", tmce);
                connectedPlug.getNetworkLink().getMedia().removePhysicalLinkSetUpListener(this);
            }
        }
    }

    public synchronized void disconnectPlug() {
        rxDark = true;
        txDark = true;
        connectedPlug.getNetworkLink().getMedia().
                removePhysicalLinkSetUpListener(this);

        connectedPlug.getNetworkLink().getMedia().
                disconnectFromDevice(getNetworkDevice());

        connectedPlug.disconnectSocket();

        connectedPlug = null;
    }

    @Override
    public void setLocation(int x, int y) {
        if (connectedPlug != null) {
            connectedPlug.moveInto(x, y);
        }

        super.setLocation(x, y);
    }

    public synchronized boolean isConnected() {
        return (connectedPlug != null);
    }

    private void blinkRxLight() {
        if (!rxDark && isConnected()) {
            rxDark = true;
            TIMER.schedule(new Blinker(this, Blinker.RX), BLINK_TIMEOUT);
        }
    }

    private void blinkTxLight() {
        if (!txDark && isConnected()) {
            txDark = true;
            TIMER.schedule(new Blinker(this, Blinker.TX), BLINK_TIMEOUT);
        }
    }

    void switchRxLightOn() {
        rxDark = false;
    }

    void switchTxLightOn() {
        txDark = false;
    }

    public void phisicalLinkSetUp() {
        txDark = false;
        rxDark = false;
    }

    public void phisicalLinkBrokenDown() {
        txDark = true;
        rxDark = true;
    }

    public void packetTransfered(Packet packet) {
    }

    public void packetTransmitted(Packet packet) {
        blinkTxLight();
    }

    public void packetReceived(Packet packet) {
        blinkRxLight();
    }

}

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
 * TimerTaskPing.java
 *
 * Created on 9 January 2006, 14:56
 */
package org.netsimulator.term;

import java.util.TimerTask;

public class TimerTaskPing extends TimerTask {

    PingCLICommand command;

    /**
     * Creates a new instance of TimerTaskPing
     * @param command
     */
    public TimerTaskPing(PingCLICommand command) {
        this.command = command;
    }

    public synchronized void run() {
        if (command != null) {
            command.TimeoutExpired(true);
        }
    }

    @Override
    public boolean cancel() {
        command = null;
        return super.cancel();
    }

}

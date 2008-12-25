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
package org.netsimulator.net;

public interface Interface {

    static final int UP = 1;
    static final int DOWN = 0;
    static final int UNKNOWN = -1;

    void setRouter( Router router );
    void setStatus( int status ) throws ChangeInterfacePropertyException;
    int getStatus();
    void setBandwidth( int bandwidth );
    int getBandwidth();
    String getEncap();
    String getName();
    void setName( String name );
    int getId();
}

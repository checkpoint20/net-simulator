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
 * LoggersConfig.java
 *
 * Created on 9 July 2006, 1:57
 */

package org.netsimulator.util;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class LoggersConfig implements TableModel
{
    public static final int MODULE = 0;
    public static final int LEVEL = 1;

    private static final ResourceBundle rsc = 
            ResourceBundle.getBundle("netsimulator", Locale.getDefault());
    private ArrayList<TableModelListener> listeners;
    private ArrayList<String> loggers;
    private ArrayList<Level> levels;
    
    
    /** Creates a new instance of LoggersConfig */
    public LoggersConfig()
    {
        listeners = new ArrayList<TableModelListener>();
        loggers = new ArrayList<String>();
        levels  = new ArrayList<Level>();
    }

    
    LoggersConfig(
        ArrayList<TableModelListener> listeners,
        ArrayList<String> loggers,
        ArrayList<Level> levels
    )
    {
        this.listeners = listeners;
        this.loggers = loggers;
        this.levels = levels;
    }
    
    
    public void addTableModelListener(TableModelListener l)
    {
        listeners.add(l);
    }
    

    public void removeTableModelListener(TableModelListener l)
    {
        listeners.remove(l);
    }

    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        switch(columnIndex)
        {
            case MODULE:
                loggers.set(rowIndex, (String)aValue);
                break;
            case LEVEL:
                levels.set(rowIndex, (Level)aValue);
                for(java.util.Iterator<TableModelListener> i = listeners.iterator(); i.hasNext(); )
                {
                    TableModelListener l = i.next();
                    l.tableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
                }
                break;
        }        
    }

    
    public String getColumnName(int columnIndex)
    {
        switch(columnIndex)
        {
            case MODULE:
                return rsc.getString("Module");
            case LEVEL:
                return rsc.getString("Debug level");
            default:
                return null;
        }        
    }

    
    public Class<?> getColumnClass(int columnIndex)
    {
        switch(columnIndex)
        {
            case MODULE:
                return String.class;
            case LEVEL:
                return Level.class;
            default:
                return null;
        }        
    }

        
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        if(columnIndex==LEVEL)
        {
            return true;
        }else
        {
            return false;
        }
    }

    
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        switch(columnIndex)
        {
            case MODULE:
                return loggers.get(rowIndex);
            case LEVEL:
                return levels.get(rowIndex);
            default:
                return null;
        }
    }

    
    public int getRowCount()
    {
        return loggers.size();
    }

    
    public int getColumnCount()
    {
        return 2;
    }
    
    
    public void addRow(String logger, Level level)
    {
        loggers.add(logger);
        levels.add(level);
    }
    
    
    public Level getLevelByLoggerName(String logger)
    {
         Level level = null;
         int index = loggers.indexOf(logger);
         if(index >= 0)
         {
            level = levels.get(index);
         }

         return level;
    }
    
    
    public Collection<String> getLoggerNames()
    {
        return loggers;
    }
    
    
    /**
     * Creates a copy of this LoggersConfig instance. The copy contains 
     * the same data as original instance, except collection 
     * of TableModelListeners.
     *
     *  @return a copy of this LoggersConfig instance.
     */
    public LoggersConfig clone()
    {
        return new LoggersConfig(
                new ArrayList<TableModelListener>(),
                new ArrayList<String>(loggers),
                new ArrayList<Level>(levels)
                );
    }
}

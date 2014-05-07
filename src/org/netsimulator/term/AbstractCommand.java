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

package org.netsimulator.term;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 *
 * @author max
 */
public abstract class AbstractCommand implements CLICommand {
    
    private static final Logger logger = Logger.getLogger(AbstractCommand.class.getName());
    
    protected String[] argv;
    protected String cl;
    private final Collection<CommandExecutionCompletedListener> executionCompletedListeners;

    public AbstractCommand() {
        executionCompletedListeners = new LinkedList<CommandExecutionCompletedListener>();
    }

    @Override
    public void setInvocationContext(String[] argv, String cl) {
        this.argv = argv;
        this.cl = cl;
    }
    
    public void addExecutionCompleteListener(CommandExecutionCompletedListener listener) {
        synchronized(executionCompletedListeners){
            executionCompletedListeners.add(listener);
        }
    }

    public void removeExecutionCompleteListener(CommandExecutionCompletedListener listener) {
        synchronized(executionCompletedListeners){
            executionCompletedListeners.remove(listener);
        }
    }
    
    protected void fireExecutionCompleted(int resutl) {
        Collection<CommandExecutionCompletedListener> localCopy;
        synchronized(executionCompletedListeners){
            localCopy =
                new LinkedList<CommandExecutionCompletedListener>(executionCompletedListeners);
        }
        
        for (CommandExecutionCompletedListener listener : localCopy) {
            listener.executionCompleted(resutl);
        }
    }
   
}

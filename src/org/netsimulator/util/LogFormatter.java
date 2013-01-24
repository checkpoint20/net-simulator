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
 * LogFormatter.java
 * We dont't need timestamps but thread ids are very usefull.
 *
 * Created on 4.12.2005, 0:25
 */

package org.netsimulator.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.*;

public class LogFormatter extends Formatter
{
    public LogFormatter()
    {
    }

    public String format(LogRecord record)
    {
        StringBuilder builder =  new StringBuilder().
            append(record.getLevel()).
            append(": ").
            append(record.getThreadID()).
            append(" ").
            append(record.getLoggerName()).
            append(".").
            append(record.getSourceMethodName()).
            append(": ").
            append(formatMessage(record.getMessage(), record.getParameters())).
            append("\n");

        if(record.getThrown() != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            record.getThrown().printStackTrace(printWriter);
            printWriter.close();
            try {
                stringWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(LogFormatter.class.getName()).log(Level.SEVERE, null, ex);
            }
            builder.append(stringWriter.toString());
        }
                    
         return builder.toString();
    }
    
    
    private String formatMessage(String message, Object parameters[]) {
        
        String res = message;
        
        if (message.indexOf("{0") >= 0 ||
            message.indexOf("{1") >= 0 ||
            message.indexOf("{2") >= 0 || 
            message.indexOf("{3") >= 0) {
            
            res = java.text.MessageFormat.format(message, parameters);
        }
        
        return res;
    }
}

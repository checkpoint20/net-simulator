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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LogFormatter extends Formatter
{
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss.S");
    
    public LogFormatter()
    {
    }

    public String format(LogRecord record)
    {
        StringBuilder builder =  new StringBuilder().
            append(DATE_FORMAT.format(new Date(record.getMillis()))).
            append(" ").
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
        
        if (message.contains("{0") ||
            message.contains("{1") ||
            message.contains("{2") || 
            message.contains("{3")) {
            
            res = java.text.MessageFormat.format(message, parameters);
        }
        
        return res;
    }
}

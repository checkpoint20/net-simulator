/*
NET-Simulator -- Network simulator.
Copyright (C) 2007 Maxim Tereshin <maxim-tereshin@yandex.ru>

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

package org.netsimulator.util;

import java.io.File;
import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class ProjectDTDReferenceResolver implements EntityResolver
{
    public static final String projectDTDPublicId = "NET-Simulator/dtd/netsimulator.dtd";
    private final InputSource projectDTDSrc;
    
    public ProjectDTDReferenceResolver()
    {
        final File NETSIM_HOME = new File( System.getProperty( "user.dir", null ) );
        final File projectDTDLocalURL = new File( NETSIM_HOME, "dtd/netsimulator.dtd" );
        
        projectDTDSrc = new InputSource( projectDTDLocalURL.getAbsolutePath() );
    }

    public InputSource resolveEntity(String publicId, String systemId) 
    throws SAXException, IOException
    {
        return publicId != null && publicId.equals( projectDTDPublicId ) ? projectDTDSrc : null;
    }
    
}

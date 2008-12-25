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
 * ConfigLoader.java
 *
 * Created on 14 May, 2006, 15:58
 */

package org.netsimulator.util;


import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.awt.Color;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;



public class ConfigLoader implements ContentHandler
{
    private XMLReader reader;
    private InputSource inputSource;
    private Locator locator;
    private ErrorHandler errorHandler;
    private LoggersConfig loggersConfig;
    private HashMap<String, ArrayList<ShapeInfo>> skins;
    private ArrayList<ShapeInfo> skin;
    private Config config;
    
    
    /** 
     * Creates a new instance of ConfigLoader. This class
     * is intended for loading a configuration from an xml-file.
     * @param errorHandler the error handler.
     */
    public ConfigLoader( ErrorHandler errorHandler )
    {
        this.errorHandler = errorHandler;
    }
 
    
    /**
     * Starts loading.
     * @param file the file to process.
     */
    public Config load(File fromFile)
    throws IOException, SAXException
    {
        config = new Config();
        
        reader = XMLReaderFactory.createXMLReader(XMLHelper.vendorParserClass);
         
        reader.setFeature("http://xml.org/sax/features/validation", true);

        inputSource = new InputSource(new FileInputStream(fromFile));
        inputSource.setEncoding(XMLHelper.charsetName);

        reader.setContentHandler( this );
        reader.setErrorHandler( errorHandler );
        reader.setEntityResolver( new ConfigDTDReferenceResolver() );
        reader.parse( inputSource );
        
        return config;
    }

    
    
    public void skippedEntity(String name) throws SAXException
    {
    }

    public void setDocumentLocator(Locator locator)
    {
        this.locator = locator;
    }

    public void characters(char[] ch, int start, int length) throws SAXException
    {
    }

    public void startDocument() throws SAXException
    {
    }

    public void processingInstruction(String target, String data) throws SAXException
    {
        System.out.println("PI target="+target+" data="+data);
    }

    public void endDocument() throws SAXException
    {
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
    }

    public void endPrefixMapping(String prefix) throws SAXException
    {
    }
    
    

    
    public void startElement(String uri, String localName, String qName, Attributes atts) 
    throws SAXException
    {
        if(qName.equals("whatOpenAfterStart"))
        {
            loadWhatOpenAfterStart(atts);
        }

        if(qName.equals("lastProject"))
        {
            loadLastProject(atts);
        }

        if(qName.equals("background"))
        {
            loadBackground(atts);
        }
        
        if(qName.equals("shape"))
        {
            loadShape(atts);
        }
        
        if(qName.equals("debug"))
        {
            startLoadingDebug(atts);
        }
        
        if(qName.equals("logger"))
        {
            loadLogger(atts);
        }
        
        if(qName.equals("currentSkin"))
        {
            loadCurrentSkin(atts);
        }

        if(qName.equals("skin"))
        {
            startLoadingSkin(atts);
        }
                
        if(qName.equals("skins"))
        {
            startLoadingSkins(atts);
        }
                
    }

    
    
    
    
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if(qName.equals("debug"))
        {
            endLoadingDebug();
        }

        if(qName.equals("skin"))
        {
            endLoadingSkin();
        }

        if(qName.equals("skins"))
        {
            endLoadingSkin();
        }
    }

    
    
    
    private void loadWhatOpenAfterStart(Attributes atts)
    throws SAXException
    {
        String value = atts.getValue("value");
        if(value==null || 
                (!value.equals("lastProject") &&
                 !value.equals("emptyProject") &&
                 !value.equals("nothing") )
          )
        {
            throw new SAXException("Failed value of attribute 'value'");
        }
        
        config.setWhatOpenAfterStart( value );
    }
    
    
    

    private void loadLastProject(Attributes atts)
    throws SAXException
    {
        String value = atts.getValue("value");
        if(value==null)
        {
            throw new SAXException("Failed value of attribute 'value'");
        }
        
        config.setLastProject( value );
    }
    
    

    private void loadCurrentSkin(Attributes atts)
    throws SAXException
    {
        String name = atts.getValue("name");
        if(name==null)
        {
            throw new SAXException("Failed value of attribute 'name'");
        }
        
        config.setCurrentSkin( name );
    }
    
    

    private void loadBackground(Attributes atts)
    throws SAXException
    {
        String value = atts.getValue("value");
        if(value==null)
        {
            throw new SAXException("Failed value of attribute 'value'");
        }
        
        Color color = null;
        try
        {
            color = Color.decode(value);
        }catch(NumberFormatException nfe)
        {
            new SAXException(nfe);
        }
        
        config.setBackground( color );
    }


    
    private void startLoadingDebug(Attributes atts)
    throws SAXException
    {
        loggersConfig = new LoggersConfig();
        config.setDebug( loggersConfig );
    }

    
    private void endLoadingDebug()
    throws SAXException
    {
        loggersConfig = null;
    }
    
    
    
    private void loadLogger(Attributes atts)
    throws SAXException
    {
        String name = atts.getValue("name");
        Level logLevel = null;
        try
        {
            logLevel = Level.parse(atts.getValue("logLevel"));
        }catch(IllegalArgumentException iae)
        {
            throw new SAXException(iae);
        }

        loggersConfig.addRow(name, logLevel);
    }


    
    private void startLoadingSkins(Attributes atts)
    throws SAXException
    {
        skins = new HashMap<String, ArrayList<ShapeInfo>>();
        config.setSkins( skins );
    }

    
    private void endLoadingSkins()
    throws SAXException
    {
        skins = null;
    }
    
    
    private void startLoadingSkin(Attributes atts)
    throws SAXException
    {
        skin = new ArrayList<ShapeInfo>();
        String name = atts.getValue("name");
        skins.put(name, skin);
    }

    
    private void endLoadingSkin()
    throws SAXException
    {
        skin = null;
    }
    
    

    private void loadShape(Attributes atts)
    throws SAXException
    {
        try
        {
            ShapeInfo shape = new ShapeInfo();
            
            for(int i=0; i<atts.getLength(); i++)
            {
                String attr = atts.getQName(i);

                if(attr.equals("name"))
                {
                    shape.setName(atts.getValue(i));
                }
                if(attr.equals("socketsX"))
                {
                    shape.setSocketsX(Integer.parseInt(atts.getValue(i)));
                }
                if(attr.equals("socketsY"))
                {
                    shape.setSocketsY(Integer.parseInt(atts.getValue(i)));
                }
                if(attr.equals("socketsStep"))
                {
                    shape.setSocketsStep(Integer.parseInt(atts.getValue(i)));
                }
            }
            
            skin.add(shape);
            
        }catch(Exception e)
        {
            throw new SAXException(e);
        }
    }

    
}

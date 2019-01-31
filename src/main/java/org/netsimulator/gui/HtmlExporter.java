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

import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.netsimulator.util.ResourceEntityResolverFactory;
import org.netsimulator.util.XMLHelper;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class HtmlExporter implements Runnable
{
    private static final File TOHTML_XSL = new File("cfg/tohtml.xsl");

    public static final EntityResolver PROJECT_DTD_RESOLVER =
            ResourceEntityResolverFactory.DEFAULT.createForProject();

    private File fileToExport;
    private XMLReader reader;
    private ArrayList<ExportToHtmlStartCompleteListener> exportListeners =
            new ArrayList<ExportToHtmlStartCompleteListener>();
    private Thread threadToJoin;
    
    
    /**
     * Exports project to html report.
     *
     */
    public HtmlExporter(
            File fileToExport,
            File outputFile,
            ErrorListener errorListener,
            String imageFileName )
            throws TransformerConfigurationException
    {
        this.fileToExport = fileToExport;
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        
        // Verify that the TransformerFactory implementation you are using
        // supports SAX input and output (Xalan-Java does!).
        if( !( tFactory.getFeature( SAXSource.FEATURE ) &&
               tFactory.getFeature( SAXResult.FEATURE ) ) )
        {
            throw new TransformerConfigurationException("The TransformerFactory " +
                    "implementation you are using " +
                    "does not support SAX input and output.");
        }
        
        SAXTransformerFactory saxTFactory =
                (SAXTransformerFactory) tFactory;
        
        TemplatesHandler templatesHandler =
                saxTFactory.newTemplatesHandler();
        
        try
        {
            reader = XMLReaderFactory.createXMLReader(XMLHelper.vendorParserClass);
        }
        catch (SAXException ex)
        {
            ex.printStackTrace();
            throw new TransformerConfigurationException("Can not get an XMLReader instance.");
        }
        
        reader.setContentHandler( templatesHandler );
        
        try
        {
            reader.parse(  new InputSource( new FileInputStream( TOHTML_XSL ) ) );
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
            throw new TransformerConfigurationException( "File " + TOHTML_XSL + " not found." );
        }
        catch (SAXException ex)
        {
            ex.printStackTrace();
            throw new TransformerConfigurationException( "Can not parse file " + TOHTML_XSL + "." );
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            throw new TransformerConfigurationException( "I/O error during " + TOHTML_XSL + "reading." );
        }
        
        Templates templates = templatesHandler.getTemplates();
        
        TransformerHandler transformerHandler
                = saxTFactory.newTransformerHandler( templates );
        
        Transformer transformer = transformerHandler.getTransformer();
        transformer.setErrorListener( errorListener );
        transformer.setParameter( "projectFile", fileToExport );
        transformer.setParameter( "generatingDate", org.netsimulator.util.Dateutil.curdatetime() );
        transformer.setParameter("image", imageFileName);
         
        reader.setContentHandler(transformerHandler);
        reader.setEntityResolver(PROJECT_DTD_RESOLVER);
        
        try
        {
            reader.setProperty( "http://xml.org/sax/properties/lexical-handler",
                            transformerHandler );
        } catch (SAXNotRecognizedException ex)
        {
            ex.printStackTrace();
        } catch (SAXNotSupportedException ex)
        {
            ex.printStackTrace();
        }
        
        Serializer serializer = 
                SerializerFactory.getSerializer( 
                OutputPropertiesFactory.getDefaultMethodProperties( "html" ) );
        try
        {
            serializer.setOutputStream( new FileOutputStream( outputFile ) );
        } catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
            throw new TransformerConfigurationException( "Output file " + outputFile + " not found." );
        }
        
        Result result = null;
        try
        {
            result = new SAXResult( serializer.asContentHandler() );
        } catch ( IOException ex )
        {
            ex.printStackTrace();
        }
        transformerHandler.setResult( result );
        
    }
    
    
    
    
    public void run()
    {
        try
        {
            // We mustn't start exporting until saving of the project is not complete.
            if(threadToJoin!=null)
            {
                threadToJoin.join();
            }
            assert reader != null && fileToExport != null;
            reader.parse( new InputSource( new FileInputStream( fileToExport ) ) );
        }
        catch( FileNotFoundException ex )
        {
            ex.printStackTrace();
        }
        catch( SAXException ex )
        {
            ex.printStackTrace();
        }
        catch( IOException ex )
        {
            ex.printStackTrace();
        }
        catch( InterruptedException ie )
        {
            ie.printStackTrace();
        }
        finally
        {
            for(Iterator<ExportToHtmlStartCompleteListener> i=exportListeners.iterator(); i.hasNext(); )
            {
                ExportToHtmlStartCompleteListener l = i.next();
                l.ExportToHtmlComplete();
                i.remove();
            }
        }
        
    }
    
    
    
    /**
     * Sometimes HtmlExporter must wait until another thread
     * (project saving eg.) completes his job.
     */
    public void setThreadToJoin(Thread threadToJoin)
    {
        this.threadToJoin = threadToJoin;
    }
    
    
    public void addExportToHtmlStartCompleteListener(ExportToHtmlStartCompleteListener listener)
    {
        exportListeners.add(listener);
    }
    
    
    public void removeExportToHtmlStartCompleteListener(ExportToHtmlStartCompleteListener listener)
    {
        exportListeners.remove(listener);
    }
    
}



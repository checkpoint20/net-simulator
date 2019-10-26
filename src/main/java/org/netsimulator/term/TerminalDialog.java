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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.ResourceBundle;

public class TerminalDialog
    extends JDialog
    implements ActionListener
{
    private static final ResourceBundle rsc = ResourceBundle.getBundle("netsimulator", Locale.getDefault());
    private static final String titlePrefix = rsc.getString("Terminal")+" - ";
   
    private JMenuBar menuBar;

    private JMenu scheme, font, file;
    private JMenuItem close, scheme1, scheme2, scheme3,
        font1, font2, font3;

    private Terminal term;

    
 
    
    public TerminalDialog(Frame owner)
    {
        this(owner, "");
    }    
    
    
    
    
    public TerminalDialog(Frame owner, String title)
    {
        menuBar = new JMenuBar();
        
        setTitle(title);
        
        file = new JMenu(rsc.getString("File"));
        close = new JMenuItem(rsc.getString("Close"));
        close.addActionListener(this);
        file.add(close);
        menuBar.add(file);

        scheme = new JMenu(rsc.getString("Scheme"));
        scheme1 = new JMenuItem(rsc.getString("Black on white"));
        scheme1.addActionListener(this);
        scheme.add(scheme1);
        scheme2 = new JMenuItem(rsc.getString("White on black"));
        scheme2.addActionListener(this);
        scheme.add(scheme2);
        scheme3 = new JMenuItem(rsc.getString("Green on black"));
        scheme3.addActionListener(this);
        scheme.add(scheme3);
        menuBar.add(scheme);

        font = new JMenu(rsc.getString("Font"));
        font1 = new JMenuItem(rsc.getString("Small"));
        font1.addActionListener(this);
        font.add(font1);
        font2 = new JMenuItem(rsc.getString("Normal"));
        font2.addActionListener(this);
        font.add(font2);
        font3 = new JMenuItem(rsc.getString("Big"));
        font3.addActionListener(this);
        font.add(font3);
        menuBar.add(font);

        setJMenuBar(menuBar);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        term = new Terminal();

        term.addCommand(new HelpCLICommand(term));
        term.addCommand(new EchoCLICommand());

        getContentPane().add(term);
        pack();
    }


    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if(e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            setVisible(false);
        }
    }


    public Terminal getTerminal()
    {
        return term;
    }
    

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource()==close)
        {
            setVisible(false);
        }

        if(e.getSource()==scheme1)
        {
            term.setForeground(Color.BLACK);
            term.setBackground(Color.WHITE);
        }

        if(e.getSource()==scheme2)
        {
            term.setForeground(Color.WHITE);
            term.setBackground(Color.BLACK);
        }

        if(e.getSource()==scheme3)
        {
            term.setForeground(Color.GREEN);
            term.setBackground(Color.BLACK);
        }


        if(e.getSource()==font1)
        {
            term.setFontSize(10);
        }

        if(e.getSource()==font2)
        {
            term.setFontSize(12);
        }

        if(e.getSource()==font3)
        {
            term.setFontSize(14);
        }
    }

    public void setTitle(String title)
    {
        super.setTitle(titlePrefix + title);
    }

    
    
}




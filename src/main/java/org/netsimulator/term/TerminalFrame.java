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

public class TerminalFrame
    extends JFrame
    implements ActionListener
{
    JMenuBar menuBar;

    JMenu scheme, font, file;
    JMenuItem close, scheme1, scheme2, scheme3,
        font1, font2, font3;

    private Terminal term;


    public TerminalFrame()
    {
        setTitle("Virtual Terminal");

        menuBar = new JMenuBar();

        file = new JMenu("Файл");
        close = new JMenuItem("Закрыть");
        close.addActionListener(this);
        file.add(close);
        menuBar.add(file);

        scheme = new JMenu("Схема");
        scheme1 = new JMenuItem("Черный текст на белом фоне");
        scheme1.addActionListener(this);
        scheme.add(scheme1);
        scheme2 = new JMenuItem("Белый текст на черном фоне");
        scheme2.addActionListener(this);
        scheme.add(scheme2);
        scheme3 = new JMenuItem("Зеленый текст на черном фоне");
        scheme3.addActionListener(this);
        scheme.add(scheme3);
        menuBar.add(scheme);

        font = new JMenu("Шрифт");
        font1 = new JMenuItem("Маленький");
        font1.addActionListener(this);
        font.add(font1);
        font2 = new JMenuItem("Нормальный");
        font2.addActionListener(this);
        font.add(font2);
        font3 = new JMenuItem("Большой");
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
        setVisible(true);
    }


    //Overridden so we can exit when window is closed
    protected void processWindowEvent(WindowEvent e)
    {
        super.processWindowEvent(e);
        if(e.getID() == WindowEvent.WINDOW_CLOSING)
        {
            //setVisible(false);
            System.exit(0);
        }
    }



    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource()==close)
        {
            //setVisible(false);
            System.exit(0);
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

}




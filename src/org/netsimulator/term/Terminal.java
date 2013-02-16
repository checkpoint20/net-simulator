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

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.StringTokenizer;
import java.util.logging.*;
import java.awt.event.KeyEvent;
import java.io.IOException;


public class Terminal extends TextScreen implements Runnable
{
    public static final String PS1 = "=>";
    public static final String help =
        "Welcome to console of the NET-Simulator virtual device!\n\n"+
        "Type help for list of commands. To get help for the command\n"+
        "type <command> -h.\n"+
        "Press Ctrl+L to refresh the screen.\n\n";
    private final TreeMap<String, CLICommand> commands = 
            new TreeMap<String, CLICommand>();
    private int x_orig=PS1.length(), y_orig=1; // begin of edit line
    private int x_end=x_orig, y_end=y_orig; // end of edit line
    
    private CLICommand commandToRun = null;
    private String argv[] = null;
    private String cl = null;
    private ArrayList <String>history = new ArrayList<String>();
    private int historyIndex = 0;
    
    private static final Logger logger = 
            Logger.getLogger("org.netsimulator.term.Terminal");
    
    
    public Terminal()
    {
        super();
        
        history.add(0, "");
        
        printHelp();
        printPS1();
    }



    public void addCommand(CLICommand command)
    {
        commands.put(command.getName(), command);
        command.setOutputWriter(writer);
    }


    public void clear()
    {
        super.clear();
     //   clearEditBuf();
        printHelp();
        printPS1();
    }


    public void printHelp()
    {
        try
        {
            writer.write(help);
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }


    private void printPS1()
    {
        if(commandToRun != null){ return; }

        try
        {
            writer.write(PS1);
        }catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        x_end = x_orig = x_cur;
        y_end = y_orig = y_cur;
    }



    
    
    
    private void clearLine()
    {
        for(int j=0; j!=this.width; j++)
        {
            screen[y_cur-1][j] = ' ';
        }        

        goToX(1);
        printPS1();
    }
    
    
    

    void keyTyped(KeyEvent e)
    {
       // System.out.println("mod="+e.getModifiers()+" charCode="+(short)(e.getKeyChar()));
      
        if(e.getModifiers() == KeyEvent.CTRL_MASK) { return; }
        
        switch(e.getKeyChar())
        {
            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_DELETE:
                return;
            case KeyEvent.VK_ENTER:
                StringBuffer cl = new StringBuffer();
                for(int i=y_orig-1; i<y_end; i++)
                {
                    for(int j=x_orig-1; j<x_end; j++)
                    {
                        cl.append(screen[i][j]);
                    }
                }
                drawChar(e.getKeyChar());
                
                String buf = cl.toString().trim();
                if(buf.length()>0)
                {
                    history.add(1, buf);
                    logger.fine("Command "+buf+" added to history list");
                }
                historyIndex = 0;
                parseCommand(buf);
                printPS1();
                repaint();
                break;
            default:
    //System.out.println("x_cur="+x_cur+" x_end="+x_end+" ch="+screen[y_cur-1][x_cur-1]);
                if(x_cur == x_end)
                {
                    drawChar(e.getKeyChar());
                    x_end = x_cur;
                    y_end = y_cur;
                }
                if(x_cur < x_end)
                {
                    stepRight();
                    drawChar(e.getKeyChar());
                   // x_end = x_cur;
                   // y_end = y_cur;
                }
                repaint();
        }
    }


    //delete some char
    private void stepLeft()
    {
        for(int i=y_cur-1; i<y_end; i++)
        {
            for(int j=x_cur-1; j<x_end-1; j++)
            {
                screen[i][j] = screen[i][j+1];
   // System.out.println("i="+i+" j="+j+" ch="+screen[i][j]);
            }
        }

        decrementXend();
    }


    //insert char
    private void stepRight()
    {
        for(int i=y_cur-1; i<y_end; i++)
        {
            for(int j=x_end; j>=x_cur; j--)
            {
                screen[i][j] = screen[i][j-1];
    //System.out.println("i="+i+" j="+j+" ch2="+screen[i][j]);
            }
        }

        incrementXend();
    }





    private void decrementXend()
    {
        int x = x_end-1;

        if(x_end<x_orig)
        {
            if(y_end>y_orig)
            {
                y_end--;
                x_end=width;
            }
        }else
        {
            x_end = x;
        }
    }



    private void incrementXend()
    {
        int x = x_end+1;

        if(x>width)
        {
            x_end=1;
            if(y_end==height)
                push();
            else
                y_end++;
        }else
        {
            x_end = x;
        }
    }




    private void parseCommand(String cl)
    {
        if(commandToRun != null){ return; }
        
        String error = null;
        String command = null;
        String argv[] = null;

//    System.out.println("command string="+cl);


        if(cl!=null && (cl = cl.trim()).length()>0)
        {
            StringTokenizer t = new StringTokenizer(cl);
            command = t.nextToken();

            argv = new String[t.countTokens()];

//System.out.println("command="+command+"<");

            for(int i=0; i!=argv.length; i++)
            {
                argv[i] = t.nextToken();
//System.out.println("argv["+i+"]="+argv[i]+"<");
            }

            if(commands.containsKey(command))
            {
                commandToRun = (CLICommand)(commands.get(command));
                this.argv = argv;
                this.cl = cl;
                new Thread(this).start();
            }
            else
            {
                error="Error: Unknown command. Try help to get list of commands.\n";
            }
        }

        if(error!=null)
        {
            try
            {
                writer.write(error);
            }catch(IOException ioe)
            {
                ioe.printStackTrace();
            }
        }
    }



    void keyReleased(KeyEvent e)
    {
/*        int x=-1;

        switch(e.getModifiers())
        {
            case KeyEvent.CTRL_MASK:
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_L:
                        clear();
                        break;
                    case KeyEvent.VK_C:
                        if(commandToRun != null) { commandToRun.Stop(); }
                        break;
                    default:
                }
                break;
            default:
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_KP_LEFT:
                        x = x_cur-1;
                        if(x >= x_orig)
                        {
                            goToX(x);
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_KP_RIGHT:
                        x = x_cur+1;
                        if(x <= x_end)
                        {
                            goToX(x);
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_HOME:
                        goToX(x_orig);
                        repaint();
                        break;
                    case KeyEvent.VK_END:
                        goToX(x_end);
                        repaint();
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        x = x_cur-1;
                        if(x >= x_orig)
                        {
                            goToX(x);
                            stepLeft();
                            repaint();
                        }
                        break;
                     case KeyEvent.VK_DELETE:
                        if(x_cur < x_end)
                        {
                            stepLeft();
                            repaint();
                        }
                        break;
                   default:
                }
        }
*/    }



    void keyPressed(KeyEvent e)
    {
        int x=-1;

        switch(e.getModifiers())
        {
            case KeyEvent.CTRL_MASK:
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_L:
                        clear();
                        break;
                    case KeyEvent.VK_C:
                        if(commandToRun != null) { commandToRun.Stop(); }
                        break;
                    default:
                }
                break;
            default:
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_KP_UP:
                        if( (historyIndex + 1) < history.size() )
                        {
                            historyIndex++;
                        }
                        //System.out.println("historyIndex="+historyIndex);
                        clearLine();
                        try
                        {
                            writer.write(history.get(historyIndex));
                            x_end = x_cur;
                            y_end = y_cur;                            
                        }catch(IOException ioe)
                        {
                            ioe.printStackTrace();
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_KP_DOWN:
                        if( (historyIndex - 1) >= 0 )
                        {
                            historyIndex--;
                        }
                        //System.out.println("historyIndex="+historyIndex);
                        clearLine();
                        try
                        {
                            writer.write(history.get(historyIndex));
                            x_end = x_cur;
                            y_end = y_cur;                            
                        }catch(IOException ioe)
                        {
                            ioe.printStackTrace();
                        }
                        break;                        
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_KP_LEFT:
                        x = x_cur-1;
                        if(x >= x_orig)
                        {
                            goToX(x);
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_KP_RIGHT:
                        x = x_cur+1;
                        if(x <= x_end)
                        {
                            goToX(x);
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_HOME:
                        goToX(x_orig);
                        repaint();
                        break;
                    case KeyEvent.VK_END:
                        goToX(x_end);
                        repaint();
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        x = x_cur-1;
                        if(x >= x_orig)
                        {
                            goToX(x);
                            stepLeft();
                            repaint();
                        }
                        break;
                     case KeyEvent.VK_DELETE:
                        if(x_cur < x_end)
                        {
                            stepLeft();
                            repaint();
                        }
                        break;
                   default:
                }
        }
    }


    
    public void run()
    {
         try
         {
              commandToRun.Go(argv, cl);
         }catch(IOException ioe)
         {
              ioe.printStackTrace();
         }
         
         commandToRun = null;
         this.argv = null;
         this.cl = null;
         printPS1();
    }

    public TreeMap<String, CLICommand> getCommands() {
        return commands;
    }
    
}

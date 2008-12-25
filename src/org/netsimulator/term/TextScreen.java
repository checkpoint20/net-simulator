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


import org.netsimulator.term.*;
import java.io.Writer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public abstract class TextScreen extends JPanel
{
    private int font_size = 12;
    private Font font;
    private FontMetrics metrics;
    protected int tab;
    protected int width=1, height=1;
    private int ch_width=0,ch_height=0;
    protected char[][] screen;
    protected int x_cur=1, y_cur=1;

    protected ScreenWriter writer;




    public TextScreen()
    {
        super(true);

        setFocusable(true);
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);
        font = new Font("Monospaced", Font.PLAIN, font_size);
        if(font==null)
        {
            System.err.println("Error: Your JVM does not have Monospaced font. Sorry!");
            return;
        }
        setFont(font);
        metrics = getFontMetrics(font);
        ch_width = metrics.charWidth('0');
        ch_height = metrics.getHeight();
        Dimension preferredSize =
            new Dimension(ch_width*80, ch_height*25+metrics.getDescent());
        setPreferredSize(preferredSize);
        setMinimumSize(preferredSize);
        setSize(preferredSize);
        resized();
        addComponentListener(new TextScreenSizeListener(this));
        addKeyListener(new TextScreenKeyListener(this));

        writer = new ScreenWriter(this);
        
        tab = 8;
    }





    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        for(int i=0; i!=this.height; i++)
        {
            for(int j=0; j!=this.width; j++)
            {
                char chs[] = {screen[i][j]};
                g.drawChars(chs, 0, 1, j*ch_width, i*ch_height+ch_height);
            }
        }

        drawCursor(g);

        //System.out.println("width="+this.width);
        //System.out.println("height="+this.height);
    }



    public Writer getWriter()
    {
        return writer;
    }



    private void drawCursor(Graphics g)
    {
        g.setXORMode(getBackground());
        g.setColor(getForeground());
        g.fillRect( (x_cur-1)*ch_width,
                    (y_cur-1)*ch_height+metrics.getDescent(),
                    ch_width,
                    ch_height);
        g.setColor(getBackground());
        g.setPaintMode();
    }




    void resized()
    {
        this.width = getWidth() / ch_width;
        this.height = getHeight() / ch_height;

        if(this.width<=0)  this.width=1;
        if(this.height<=0) this.height=1;

        char[][] buf = screen;
        screen = new char[this.height][this.width];


        // fill screen with space
        for(int i=0; i!=this.height; i++)
        {
            for(int j=0; j!=this.width; j++)
            {
                screen[i][j] = ' ';
            }
        }


        // copy content of old screen into new one
        if(buf!=null)
        {
            for(int i=0; i!=this.height && i!=buf.length; i++)
            {
                for(int j=0; j!=this.width && j!=buf[i].length; j++)
                {
                    screen[i][j] = buf[i][j];
                }
            }
        }

        if(x_cur>this.width)  x_cur=this.width;
        if(y_cur>this.height) y_cur=this.height;
    }






    // This method implements here only for debug.
    // You must override it for real use.
    void keyTyped(KeyEvent e)
    {
        for(int i=1; i<=this.height; i++)
        {
            for(int j=1; j<=this.width; j++)
            {
                drawChar(e.getKeyChar(), j, i);
            }
        }

        goTo(10, 10);
        repaint();
    }


    abstract void keyPressed(KeyEvent e);

    abstract void keyReleased(KeyEvent e);




    // move rows up
    void push()
    {
        for(int i=0; i!=(screen.length-1); i++)
        {
            System.arraycopy(screen[i+1], 0, screen[i], 0, screen[i].length);
        }

        // clear the last row
        for(int j=0; j!=screen[screen.length-1].length; j++)
        {
            screen[screen.length-1][j] = ' ';
        }
    }



    void clear()
    {
        for(int i=0; i!=this.height; i++)
        {
            for(int j=0; j!=this.width; j++)
            {
                screen[i][j] = ' ';
            }
        }

        goTo(1, 1);
    }


    void drawChar(char ch, int x, int y)
    {
        goTo(x, y);
        drawChar(ch);
    }


    void drawChar(char ch)
    {
        switch(ch)
        {
            case '\n': LF(); CR(); 
                break;
            case '\t':
                screen[y_cur-1][x_cur-1] = ' ';
                next();
                for(int i=0; ((x_cur-1) % tab) != 0 ; i++)
                {
                    //System.out.println("(x_cur-1) % tab = "+((x_cur-1) % tab));
                    
                    screen[y_cur-1][x_cur-1] = ' ';
                    next();
                    
                    //if(i==100) break;
                }
                break;
            default:
                screen[y_cur-1][x_cur-1] = ch;
                next();
        }
    }


    void blankChar(int x, int y)
    {
        goTo(x, y);
        screen[y_cur-1][x_cur-1] = ' ';
    }



    void blankChar()
    {
        screen[y_cur-1][x_cur-1] = ' ';
    }




    void LF()
    {
        int y_next = y_cur+1;

        if(y_next>height)
        {
            y_next = height;
            push();
        }

        y_cur = y_next;
    }



    void CR()
    {
        x_cur = 1;
    }




    void next()
    {
        int x_next = x_cur+1;
        int y_next = y_cur;

        if(x_next>width)
        {
            x_next=1;
            y_next+=1;
        }

        if(y_next>height)
        {
            y_next = height;
            push();
        }

        x_cur = x_next;
        y_cur = y_next;
    }


    public void setFontSize(int size)
    {
        font = font.deriveFont((float)size);
        setFont(font);
        metrics = getFontMetrics(font);
        ch_width = metrics.charWidth('0');
        ch_height = metrics.getHeight();

        resized();
    }


    /** Move cursor to new position.
    */
    void goTo(int x, int y)
    {
        if(x>width || y>height) return;

        this.x_cur = x;
        this.y_cur = y;
    }



    /** Move cursor to new position.
    */
    void goToX(int x)
    {
        if(x>width) return;

        this.x_cur = x;
    }




    /** Move cursor to new position.
    */
    void goToY(int y)
    {
        if(y>height) return;

        this.y_cur = y;
    }




    /** Returns current position of cursor */
    int getX_cur()
    {
        return x_cur;
    }

    /** Returns current position of cursor */
    int getY_cur()
    {
        return y_cur;
    }

}




class TextScreenSizeListener extends ComponentAdapter
{
    TextScreen con;

    public TextScreenSizeListener(TextScreen con)
    {
        this.con = con;
    }

    public void componentResized(ComponentEvent e)
    {
        con.resized();
    }
}



class TextScreenKeyListener implements KeyListener
{
    TextScreen con;

    public TextScreenKeyListener(TextScreen con)
    {
        this.con = con;
    }

    public void keyReleased(KeyEvent e)
    {
        con.keyReleased(e);
    }

    public void keyTyped(KeyEvent e)
    {
        con.keyTyped(e);
    }

    public void keyPressed(KeyEvent e)
    {
        con.keyPressed(e);
    }
}


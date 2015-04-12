//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2010  Joe Peterson
//

package com.wildlava.explore;

import android.widget.TextView;
import android.widget.EditText;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class ExpIO
{
   private Explore exp;
   private TextView output;
   private EditText input;
   private String[] screen;
   private int cur_line;
   public boolean wrap = true;
   public boolean silent = false;

   public static final int SCREEN_LINES = 64;
   
   public ExpIO(Explore e, TextView out, EditText in)
   {
      exp = e;
      output = out;
      input = in;

      screen = new String[SCREEN_LINES];
      clearScreen();
   }

   void clearScreen()
   {
      for (int i=0; i<SCREEN_LINES; i++)
      {
         screen[i] = "";
      }

      cur_line = SCREEN_LINES - 1;

      drawScreen();
   }
   
   void print(String s)
   {
      if (!silent)
      {
         if (s.indexOf("\\") != -1)
         {
            String out_str;
            
            if (wrap)
            {
               out_str = s.replace("\\\\", "\n\n");
               out_str = out_str.replace("\\ ", "\n ");
               out_str = out_str.replace("!\\", "!  ");
               out_str = out_str.replace("?\\", "?  ");
               out_str = out_str.replace(".\\", ".  ");
               out_str = out_str.replace("\\", " ");
            }
            else
            {
               out_str = s.replace("\\", "\n");
            }
            
            doOutput(out_str, true);
         }
         else
         {
            doOutput(s, true);
         }
      }
   }

   void printRaw(String s)
   {
      printRaw(s, true);
   }
    
   void printRaw(String s, boolean new_line)
   {
      if (!silent)
      {
         doOutput(s, new_line);
      }
   }
   
   private void doOutput(String s, boolean new_line)
   {
      String lines[] = s.split("\n");
      String old_cur_line_text = screen[cur_line];
      
      for (String line : lines)
      {
         if (!old_cur_line_text.equals(""))
         {
            screen[cur_line] = old_cur_line_text + line;
            old_cur_line_text = "";
         }
         else
         {
            screen[cur_line] = line;
         }
         
         if (++cur_line > (SCREEN_LINES - 1))
         {
            cur_line = 0;
         }

         //drawScreen();
      }
      
      if (new_line)
      {
         screen[cur_line] = "";
         //drawScreen();
      }
      else
      {
         if (--cur_line < 0)
         {
            cur_line = SCREEN_LINES - 1;
         }
      }

      drawScreen();
   }

   void drawScreen()
   {
      StringBuffer screen_buf = new StringBuffer(2048);
      
      //try
      //{
      //   Thread.sleep(30);
      //}
      //catch (InterruptedException x)
      //{
      //}
      
      for (int i=cur_line-(SCREEN_LINES-1); i<=cur_line; i++)
      {
         screen_buf.append(screen[(i + SCREEN_LINES) % SCREEN_LINES]);

         if (i < cur_line)
         {
            screen_buf.append("\n");
         }
      }

      output.setText(screen_buf.toString());
      //output.refreshDrawableState();
   }

   String getScreen()
   {
      StringBuffer screen_buf = new StringBuffer(2048);
      
      for (int i=cur_line-(SCREEN_LINES-1); i<=cur_line; i++)
      {
         screen_buf.append(screen[(i + SCREEN_LINES) % SCREEN_LINES]);

         if (i < cur_line)
         {
            screen_buf.append("\n");
         }
      }

      return screen_buf.toString();
   }

   void setScreen(String screen)
   {
      //clearScreen();
      doOutput(screen, false);
   }
   
   boolean saveSuspendedState(String filename, String state)
   {
      try
      {
         FileOutputStream file = exp.openFileOutput(filename, 0);
         try
         {
            file.write((state + '\n').getBytes());
            file.close();
         }
         catch (IOException x)
         {
            try
            {
               file.close();
            }
            catch (IOException y)
            {
            }
            
            return false;
         }
      }
      catch (FileNotFoundException x)
      {
         return false;
      }

      return true;
   }
   
   String loadSuspendedState(String filename)
   {
      byte buf[];
      
      try
      {
         FileInputStream file = exp.openFileInput(filename);
         try
         {
            int size = file.available();
            buf = new byte[size];

            if (file.read(buf) != size)
            {
               buf = new byte[0];
            }

            file.close();
         }
         catch (IOException x)
         {
            try
            {
               file.close();
            }
            catch (IOException y)
            {
            }
            
            buf = new byte[0];
         }
      }
      catch (FileNotFoundException x)
      {
         return null;
      }

      return (new String(buf)).trim();
   }
}

//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.awt.TextArea;
import java.awt.event.*;

class ExpIO implements KeyListener
{
   private TextArea output;
   private int output_len = 0;
   
   private boolean output_hold = false;
   private StringBuffer held_strings = new StringBuffer(256);

   private StringBuffer buffer = new StringBuffer(256);
   
   ExpIO(TextArea out)
   {
      output = out;

      if (output != null)
      {
         output.addKeyListener(this);
      }
   }
   
   void holdOutput()
   {
      output_hold = true;
   }

   boolean releaseOutput()
   {
      output_hold = false;

      if (held_strings.length() > 0)
      {
         outputString(held_strings.toString());
         held_strings.setLength(0);
         
         return true;
      }
      else
      {
         return false;
      }
   }
    
   void print(String s)
   {
      print(s, true);
   }
    
   void print(String s, boolean new_line)
   {
      if (s.indexOf("\\") != -1)
      {
         String lf_str = s.replace('\\', '\n');
         printRaw(lf_str, new_line);
      }
      else
      {
         printRaw(s, new_line);
      }
   }

   void printRaw(String s)
   {
      printRaw(s, true);
   }
    
   void printRaw(String s, boolean new_line)
   {
      if (output_hold)
      {
         holdString(s);
            
         if (new_line)
         {
            holdString("\n");
         }
      }
      else
      {
         outputString(s);
            
         if (new_line)
         {
            outputString("\n");
         }
      }
   }
    
   void printNow(String s)
   {
      printNow(s, true);
   }
    
   void printNow(String s, boolean new_line)
   {
      outputString(s);
        
      if (new_line)
      {
         outputString("\n");
      }
   }
    
   private void outputString(String s)
   {
      if (output != null)
      {
         appendString(s);
      }
      else
      {
         System.out.print(s);
         System.out.flush();
      }
   }
    
   private void appendString(String s)
   {
      output.append(s);
      output_len += s.length();
   }
   
   private void backspace()
   {
      output.replaceRange("", output_len - 1, output_len);
      --output_len;
   }
   
   private void holdString(String s)
   {
      held_strings.append(s);
   }
        
   synchronized String input()
   {
      String s;
        
      if (output != null)
      {
         output.requestFocus();

         try
         {
            wait();
         }
         catch (InterruptedException e)
         {
         }

         s = getInputString();
      }
      else
      {
         BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));
         
         try
         {
            s = stream.readLine();
         }
         catch (java.io.IOException e)
         {
            s = "";
         }
      }
        
      return s;
   }

   String getInputString()
   {
      String input_str = buffer.toString();
      buffer.setLength(0);
      
      return input_str;
   }

   public synchronized void keyTyped(KeyEvent evt)
   {
      char key = evt.getKeyChar();
      
      if (key == 0x0a || key == 0x0d)
      {
         appendString("\n");
         notifyAll();
      }
      else if (key == 0x08 || key == 0x7f)
      {
         int len = buffer.length();
         if (len > 0)
         {
            buffer.setLength(len - 1);
            backspace();
         }
      }
      else
      {
         buffer.append(key);
         appendString(String.valueOf(key));
      }
   }

   public void keyReleased(KeyEvent evt)
   {
   }
   
   public void keyPressed(KeyEvent evt)
   {
   }
}

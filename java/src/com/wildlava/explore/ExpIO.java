//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2010  Joe Peterson
//

package com.wildlava.explore;

import java.lang.Thread;
import java.lang.InterruptedException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

class ExpIO
{
   public boolean wrap = false;
   public int max_line_length = 79;
   public boolean no_delay = false;

   void print(String s)
   {
      String out_str;

      if (wrap)
      {
         out_str = s.replace("\\\\", "\n\n");
         out_str = out_str.replace("\\ ", "\n ");
         out_str = out_str.replace("\\", " ");
      }
      else
      {
         out_str = s.replace("\\", "\n");
      }

      for (String line : out_str.split("\n", -1))
      {
         while (line.length() > max_line_length)
         {
            int last_space_pos = line.lastIndexOf(' ', max_line_length);
            if (last_space_pos == -1)
            {
               break;
            }
            else
            {
               printRaw(line.substring(0, last_space_pos), true);
               line = line.substring(last_space_pos + 1);
            }
         }

         printRaw(line, true);
      }
   }

   void printRaw(String s)
   {
      printRaw(s, true);
   }

   void printRaw(String s, boolean new_line)
   {
      if (!no_delay)
      {
         try
         {
            Thread.sleep(30);
         }
         catch (InterruptedException x)
         {
         }
      }

      System.out.print(s);

      if (new_line)
      {
         System.out.print("\n");
      }

      System.out.flush();
   }

   synchronized String input()
   {
      String s;

      BufferedReader stream = new BufferedReader(new InputStreamReader(System.in));

      try
      {
         s = stream.readLine();
      }
      catch (java.io.IOException e)
      {
         s = "";
      }

      if (s != null)
      {
         return s;
      }
      else
      {
         return "";
      }
   }

   //boolean saveSuspendedState(String filename, byte[] state)
   //{
   //   return false;
   //}

   //byte[] loadSuspendedState(String filename)
   //{
   //   return null;
   //}

   boolean saveSuspendedState(String filename, String state)
   {
      try
      {
         FileOutputStream file = new FileOutputStream(filename);
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
         FileInputStream file = new FileInputStream(filename);
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

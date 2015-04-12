//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.applet.Applet;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.*;
import java.awt.Label;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.net.URL;

public class Explore extends Applet implements Runnable
{
   private boolean applet = false;
   private ExpIO io = new ExpIO(null);
   
   public void init()
   {
      applet = true;

      TextArea output = new TextArea(null, 16, 64,
                                     TextArea.SCROLLBARS_VERTICAL_ONLY);
      output.setEditable(false);
      output.setBackground(Color.black);
      output.setForeground(Color.green);
      output.setFont(new Font("Dialog", Font.PLAIN, 14));
      add("output", output);

      io = new ExpIO(output);

      Thread thread = new Thread(this, "Explore");
      thread.start();
   }

   /*
   public void destroy()
   {
      io.output.removeKeyListener(io.input);
      io.output = null;

      io.input = null;
      
      io.thread.stop();
      io.thread = null;
   }
   */
   
   public static void main(String args[])
   {
      Explore explore = new Explore();
      Thread thread = new Thread(explore, "Explore");
      thread.start();
   }

   public void run()
   {
      do
      {
         io.print("");
         io.print("");
         io.print("*** EXPLORE ***  ver 4.6");
         io.print("");
         
         World world = new World(io);
         
         String advname;
         
         do
         {
            io.print("Name of adventure: ", false);
            advname = io.input();
         } while (advname == null);
         advname = advname.trim().toLowerCase();
         String filename = advname + ".exp";
         
         BufferedReader file_stream = getAdventureInputStream(filename);
         if (file_stream != null)
         {
            io.print("");
            io.print(advname + " is now being built...");
            
            if (world.load(file_stream))
            {
               try
               {
                  file_stream.close();
               }
               catch (java.io.IOException x)
               {
                  io.print("Error closing adventure file!");
               }

               io.print("");
               io.print("");
               io.print(world.title);
               io.print("");
               
               playAdventure(world);
            }
            else
            {
               try
               {
                  file_stream.close();
               }
               catch (java.io.IOException x)
               {
                  io.print("Error closing adventure file!");
               }
               
               io.print("Error while building adventure!");
            }
         }
         else
         {
            io.print("Sorry, that adventure is not available.");
         }
      } while (applet);
   }

   BufferedReader getAdventureInputStream(String filename)
   {
      BufferedReader file_stream;

      if (applet)
      {
         URL url;
            
         try
         {
            url = new URL(getCodeBase(), filename);
         }
         catch (java.net.MalformedURLException e)
         {
            return null;
         }

         try
         {
            file_stream = new BufferedReader(new InputStreamReader(url.openStream()));
         }
         catch (java.io.IOException e)
         {
            return null;
         }
      }
      else
      {
         try
         {  
            FileInputStream stream = new FileInputStream(filename);
            file_stream = new BufferedReader(new InputStreamReader(stream));
         }
         catch (java.io.FileNotFoundException x)
         {
            return null;
         }
      }
        
      return file_stream;
   }
    
   void playAdventure(World world)
   {
      int result = world.RESULT_DESCRIBE;
      while ((result & world.RESULT_END_GAME) == 0)
      {
         io.holdOutput();
            
         int check_result = world.checkForAuto();
         if (check_result != world.RESULT_NOTHING)
         {
            result = check_result;
            continue;
         }

         if ((result & world.RESULT_DESCRIBE) != 0)
         {
            io.printNow("");

            if (io.releaseOutput())
            {
               io.print("");
            }

            io.print(world.player.current_room.description());
         }
         else
         {
            io.releaseOutput();
         }

         io.print(":", false);
         String wish = io.input();

         wish = ExpUtil.superTrim(wish);
            
         if (wish != null && !wish.equals(""))
         {
            io.holdOutput();
                
            result = world.processCommand(wish, true);
            if ((result & world.RESULT_END_GAME) != 0)
            {
               break;
            }
         }
         else
         {
            result = world.RESULT_NORMAL;
         }
      }

      if ((result & world.RESULT_WIN) != 0 ||
          (result & world.RESULT_DIE) != 0)
      {
         io.printNow("");
      }

      if (io.releaseOutput())
      {
         io.print("");
      }
   }
}

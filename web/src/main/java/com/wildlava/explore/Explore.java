//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2010  Joe Peterson
//

package com.wildlava.explore;

import java.io.FileReader;
import java.io.BufferedReader;

class Explore
{
   static private ExpIO io;
   static private World world;

   static void playOnce(ExploreResponse response,
                        String advname,
                        String command,
                        String resume,
                        String last_suspend)
   {
      boolean trs_compat = false;
      boolean unwrap_lines = false;
      boolean show_title = true;
      boolean quiet = false;

      if (command != null)
      {
         show_title = false;
         quiet = true;
      }

      io = new ExpIO();
      io.unwrap = unwrap_lines && !trs_compat;

      if (!quiet)
      {
         io.print("");
         io.print("");
         io.print("*** EXPLORE ***  ver 4.10");
      }

      world = new World(io, advname);
      world.trs_compat = trs_compat;
      world.suspend_mode = World.SUSPEND_TO_MEMORY;
      world.last_suspend = last_suspend;

      try
      {
         BufferedReader file = new BufferedReader(new FileReader(advname + ".exp"));

         if (!quiet)
         {
            io.print("");
            io.print(advname + " is now being built...");
         }

         try
         {
            if (world.load(file))
            {
               file.close();

               if (show_title)
               {
                  io.print("");
                  io.print("");
                  io.print(world.title);
                  io.print("");
               }

               if (resume != null)
               {
                  if (!world.state(resume))
                  {
                     response.setError("Bad resume code");
                     return;
                  }
               }

               String wish;
               int result;

               if (command != null)
               {
                  wish = ExpUtil.superTrim(command);
                  if (!wish.equals(""))
                  {
                     result = world.processCommand(wish, true);
                  }
                  else
                  {
                     result = World.RESULT_NORMAL;
                  }
               }
               else
               {
                  result = World.RESULT_DESCRIBE;
               }

               if ((result & World.RESULT_NO_CHECK) == 0)
               {
                  int check_result = world.checkForAuto(result);
                  if (check_result != World.RESULT_NORMAL)
                  {
                     result = check_result;
                  }
               }

               if ((result & World.RESULT_DESCRIBE) != 0)
               {
                  io.print("");
                  io.print(world.player.current_room.description());
               }

               if ((result & World.RESULT_END_GAME) != 0)
               {
                  response.setEnd(true);
                  if ((result & World.RESULT_WIN) != 0)
                  {
                     response.setWin(true);
                  }
                  else if ((result & World.RESULT_DIE) != 0)
                  {
                     response.setDie(true);
                  }
               }
               else
               {
                  response.setPrompt(":");
                  response.setState(world.state());

                  if ((result & World.RESULT_SUSPEND) != 0)
                  {
                     response.setSuspend(true);
                  }
               }
            }
            else
            {
               file.close();

               response.setError("Error building adventure");
            }
         }
         catch (java.io.IOException x)
         {
            response.setError("Error building adventure");
         }
      }
      catch (java.io.FileNotFoundException x)
      {
         response.setError("Adventure not found");
      }

      response.setOutput(io.getOutput());
   }
}
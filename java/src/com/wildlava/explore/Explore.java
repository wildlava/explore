//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2010  Joe Peterson
//

package com.wildlava.explore;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Iterator;

public class Explore
{
   public static void main(String args[])
   {
      Game game = new Game();
      String advname = null;
      String input_script = null;
      boolean no_delay = false;
      boolean list_commands = false;
      boolean trs_compat = false;

      for (String arg : args)
      {
         if (arg.startsWith("--script="))
         {
            input_script = arg.substring(9);
            no_delay = true;
         }
         else if (arg.equals("--list-commands"))
         {
            list_commands = true;
            no_delay = true;
         }
         else if (arg.equals("--trs-compat"))
         {
            trs_compat = true;
         }
         else
         {
            advname = arg;
         }
      }

      game.start(advname, input_script, no_delay, list_commands, trs_compat);
   }
}

class Game
{
   private ExpIO io;
   private World world;

   void start(String advname,
              String input_script,
              boolean no_delay,
              boolean list_commands,
              boolean trs_compat)
   {
      io = new ExpIO();
      world = new World(io, advname);

      io.no_delay = no_delay;
      world.list_commands_on_load = list_commands;
      world.trs_compat = trs_compat;

      ArrayList<String> input_script_commands = null;
      Iterator<String> input_script_iter = null;
      if (input_script != null)
      {
         try
         {
            BufferedReader file = new BufferedReader(new FileReader(input_script));

            try
            {
               input_script_commands = new ArrayList<String>();
               while(true)
               {
                  String line = file.readLine();
                  if (line == null)
                     break;

                  input_script_commands.add(line.trim());
               }

               file.close();

               input_script_iter = input_script_commands.iterator();
            }
            catch (java.io.IOException x)
            {
               io.print("Error while reading script file!");
               return;
            }
         }
         catch (java.io.FileNotFoundException x)
         {
            io.print("Script file not found");
            return;
         }
      }

      io.print("");
      io.print("");
      io.print("*** EXPLORE ***  ver 4.10");

      if (advname == null)
      {
         io.print("");
         io.printRaw("Name of adventure: ", false);
         advname = io.input();

         advname = advname.trim().toLowerCase();
      }

      try
      {
         BufferedReader file = new BufferedReader(new FileReader(advname + ".exp"));

         io.print("");
         io.print(advname + " is now being built...");

         try
         {
            if (world.load(file))
            {
               file.close();

               io.print("");
               io.print("");
               io.print(world.title);
               io.print("");

               boolean game_on;
               game_on = play(null);
               while (game_on)
               {
                  if (input_script != null && input_script_iter.hasNext())
                  {
                     String command = input_script_iter.next();
                     io.printRaw(command);
                     game_on = play(command);
                  }
                  else
                  {
                     game_on = play(io.input());
                  }
               }
            }
            else
            {
               file.close();

               io.print("Error while building adventure!");
            }
         }
         catch (java.io.IOException x)
         {
            io.print("Error while building adventure!");
         }
      }
      catch (java.io.FileNotFoundException x)
      {
         io.print("Sorry, that adventure is not available.");
      }
   }

   boolean play(String wish)
   {
      int result;

      if (wish != null)
      {
         wish = ExpUtil.superTrim(wish);

         if (!wish.equals(""))
         {
            result = world.processCommand(wish, true);
         }
         else
         {
            result = world.RESULT_NORMAL;
         }
      }
      else
      {
         result = world.RESULT_DESCRIBE;
      }

      if ((result & world.RESULT_NO_CHECK) == 0)
      {
         int check_result = world.checkForAuto(result);
         if (check_result != world.RESULT_NORMAL)
         {
            result = check_result;
         }
      }

      if ((result & world.RESULT_DESCRIBE) != 0)
      {
         io.print("");
         io.print(world.player.current_room.description());
      }

      //if ((result & world.RESULT_WIN) != 0 ||
      //    (result & world.RESULT_DIE) != 0)
      if ((result & world.RESULT_END_GAME) != 0)
      {
         if ((result & world.RESULT_WIN) != 0)
         {
            io.print("");
            io.print("Nice job! You successfully completed this adventure!");
         }
         else if ((result & world.RESULT_DIE) != 0)
         {
            io.print("");
            io.print("Game over.");
         }

         io.print("");

         return false;
      }
      else
      {
         io.printRaw(":", false);

         return true;
      }
   }
}

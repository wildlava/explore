//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2010  Joe Peterson
//

package com.wildlava.explore;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

public class Explore
{
   public static void main(String args[])
   {
      Game game = new Game();
      game.start();
   }
}

class Game
{
   private ExpIO io;
   private World world;

   void start()
   {
      BufferedReader file = null;
      String advname;

      io = new ExpIO();

      io.print("");
      io.print("");
      io.print("*** EXPLORE ***  ver 4.8.6");

      io.print("");

      io.printRaw("Name of adventure: ", false);
      advname = io.input();

      advname = advname.trim().toLowerCase();

      world = new World(io, advname);

      String filename = advname + ".exp";
      try
      {
         file = new BufferedReader(new FileReader(advname + ".exp"));

         io.print("");
         io.print(advname + " is now being built...");

         if (world.load(file))
         {
            try
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
                  game_on = play(io.input());
               }
            }
            catch (java.io.IOException x)
            {
               io.print("Error while building adventure!");
            }
         }
         else
         {
            try
            {
               file.close();
            }
            catch (java.io.IOException x)
            {
            }

            io.print("Error while building adventure!");
         }
      }
      catch (FileNotFoundException x)
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
            io.print("Congratulations, you have successfully completed this adventure!");
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

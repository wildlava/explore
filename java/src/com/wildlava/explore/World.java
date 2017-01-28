//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;

class World
{
   private ExpIO io;
   private String advname;

   int version = 0;
   String title = "This adventure has no title!";
   Player player;
   HashMap<String, Room> rooms;
   ArrayList<String> room_list;
   ArrayList<Command> commands;
   HashMap<String, String> variables;
   HashMap<String, String> item_descs;

   ArrayList<String> plural_items;
   ArrayList<String> mass_items;

   HashMap<String, String> same_items;
   HashMap<String, String> old_items;
   HashMap<Integer, String> old_versions;
   int suspend_version = 2;
   boolean suspend_interactive = false;

   public static boolean trs_compat = false;
   public static boolean trs_look = false;
   public static boolean use_fixed_objects = false;

   World(ExpIO i, String a)
   {
      io = i;
      if (trs_look)
         io.wrap = false;

      advname = a;

      player = new Player(io, this);

      rooms = new HashMap<String, Room>();
      room_list = new ArrayList<String>();
      commands = new ArrayList<Command>();
      variables = new HashMap<String, String>();
      item_descs = new HashMap<String, String>();

      plural_items = new ArrayList<String>();
      mass_items = new ArrayList<String>();

      same_items = new HashMap<String, String>();
      old_items = new HashMap<String, String>();
      old_versions = new HashMap<Integer, String>();
   }

   public static final int RESULT_NORMAL = 0;
   public static final int RESULT_DESCRIBE = 2;
   public static final int RESULT_WIN = 4;
   public static final int RESULT_DIE = 8;
   public static final int RESULT_END_GAME = 16;
   public static final int RESULT_NO_CHECK = 32;

   boolean evalCondition(Command c)
   {
      if (c.condition == null)
      {
         return true;
      }

      for (String condition : c.condition.split("&", -1))
      {
         if (condition.startsWith("$"))
         {
            int op_pos = condition.indexOf("==");
            if (op_pos != -1)
            {
               String variable_name = condition.substring(1, op_pos);
               String value = condition.substring(op_pos + 2);

               if (variables.containsKey(variable_name))
               {
                  if (!variables.get(variable_name).equals(value))
                  {
                     return false;
                  }
               }
               else
               {
                  return false;
               }
            }
            else
            {
               op_pos = condition.indexOf("!=");
               if (op_pos != -1)
               {
                  String variable_name = condition.substring(1, op_pos);
                  String value = condition.substring(op_pos + 2);

                  if (variables.containsKey(variable_name))
                  {
                     if (variables.get(variable_name).equals(value))
                     {
                        return false;
                     }
                  }
               }
               else
               {
                  return false;
               }
            }
         }
         else
         {
            boolean invert = false;
            if (condition.startsWith("-"))
            {
               invert = true;
            }
            else if (!condition.startsWith("+"))
            {
               return false;
            }

            condition = condition.substring(1);

            boolean player_or_room = false;
            if (condition.startsWith("*"))
            {
               player_or_room = true;
               condition = condition.substring(1);
            }

            boolean has_item = (player.hasItem(condition) ||
                                (player_or_room &&
                                 player.current_room.hasItem(condition)));

            if (invert && has_item)
            {
               return false;
            }
            else if (!invert && !has_item)
            {
               return false;
            }
         }
      }

      return true;
   }

   int checkForAuto(int previous_result)
   {
      int result = RESULT_NORMAL;

      for (Command c : commands)
      {
         if ((c.location == null ||
              c.location.equals(player.current_room.name)) &&
             c.commands == null)
         {
            if (evalCondition(c))
            {
               result |= takeAction(c, true, previous_result);
               if ((result & RESULT_END_GAME) != 0)
               {
                  break;
               }
            }
         }
      }

      return result;
   }

   int processCommand(String wish, boolean acknowledge)
   {
      int result = RESULT_NORMAL;
      boolean player_meets_condition = false;
      boolean player_in_correct_room = false;
      String command = null;
      String argument = null;
      String verbatim_argument = null;

      //
      // Save the argument before processing in case someone needs it.
      //
      int pos = wish.indexOf(" ");
      if (pos != -1)
      {
         verbatim_argument = wish.substring(pos + 1);
      }

      wish = wish.toUpperCase().replaceAll("[^A-Z ]", "");

      Command custom = null;
      for (Command c : commands)
      {
         if (c.commands != null)
         {
            boolean match = false;
            for (int i=0; i<c.commands.length; ++i)
            {
               if (c.commands[i].equals(wish))
               {
                  match = true;
                  break;
               }
            }

            if (match)
            {
               if (c.location == null ||
                   player.current_room.name.equals(c.location))
               {
                  player_in_correct_room = true;
                  custom = c;

                  if (evalCondition(c))
                  {
                     player_meets_condition = true;
                     break;
                  }
                  else if (!c.cont)
                  {
                     break;
                  }
               }
               else if (!player_in_correct_room)
               {
                  custom = c;
               }
            }
         }
      }

      boolean try_builtin = true;
      String action_denied_directive = null;

      if (trs_compat)
      {
         if (custom != null && player_in_correct_room)
         {
            try_builtin = false;
            if (player_meets_condition)
            {
               result = takeAction(custom);
            }
            else
            {
               io.print("You can't do that yet.");
            }
         }
      }
      else
      {
         if (custom != null && player_in_correct_room)
         {
            if (player_meets_condition)
            {
               try_builtin = false;
               result = takeAction(custom);
            }
            else
            {
               if (custom.action != null)
               {
                  int or_pos = custom.action.indexOf("|");
                  if (or_pos != -1)
                  {
                     action_denied_directive =
                        custom.action.substring(or_pos + 1);
                     if (action_denied_directive.startsWith("|"))
                     {
                        try_builtin = false;
                        if (action_denied_directive.startsWith("|:"))
                        {
                           io.print(action_denied_directive.substring(2));
                        }
                        else
                        {
                           io.print(action_denied_directive.substring(1));
                        }
                     }
                  }
               }
            }
         }
      }

      if (try_builtin)
      {
         pos = wish.indexOf(" ");
         if (pos != -1)
         {
            command = wish.substring(0, pos);
            argument = wish.substring(pos + 1);
         }
         else
         {
            command = wish;
         }

         String goto_room = null;
         boolean wants_to_walk = false;

         if (command.equals("GO"))
         {
            if (argument == null)
            {
               io.print("Go where?");
            }
            else if (argument.equals("NORTH") ||
                     argument.equals("N") ||
                     argument.equals("SOUTH") ||
                     argument.equals("S") ||
                     argument.equals("EAST") ||
                     argument.equals("E") ||
                     argument.equals("WEST") ||
                     argument.equals("W") ||
                     argument.equals("UP") ||
                     argument.equals("U") ||
                     argument.equals("DOWN") ||
                     argument.equals("D"))
            {
               io.print("No need to say \"go\" for the simple directions.");
            }
            else
            {
               io.print("I'm not sure how to get there. Try a direction.");
            }
         }
         else if (command.equals("LOOK"))
         {
            if (argument != null)
            {
               io.print("There's really nothing more to see.");
            }

            result |= RESULT_DESCRIBE;
         }
         else if ((command.equals("N") ||
                   command.equals("NORTH")) &&
                  argument == null)
         {
            goto_room = player.current_room.north_room;
            wants_to_walk = true;
         }
         else if ((command.equals("S") ||
                   command.equals("SOUTH")) &&
                  argument == null)
         {
            goto_room = player.current_room.south_room;
            wants_to_walk = true;
         }
         else if ((command.equals("E") ||
                   command.equals("EAST")) &&
                  argument == null)
         {
            goto_room = player.current_room.east_room;
            wants_to_walk = true;
         }
         else if ((command.equals("W") ||
                   command.equals("WEST")) &&
                  argument == null)
         {
            goto_room = player.current_room.west_room;
            wants_to_walk = true;
         }
         else if ((command.equals("U") ||
                   command.equals("UP")) &&
                  argument == null)
         {
            goto_room = player.current_room.up_room;
            wants_to_walk = true;
         }
         else if ((command.equals("D") ||
                   command.equals("DOWN")) &&
                  argument == null)
         {
            goto_room = player.current_room.down_room;
            wants_to_walk = true;
         }
         else if (command.equals("HELP"))
         {
            io.print("");

            if (World.trs_compat)
            {
               io.print("These are some of the commands you may use:");
               io.print("");
               io.print("NORTH or N      (go north)");
               io.print("SOUTH or S      (go south)");
               io.print("EAST or E       (go east)");
               io.print("WEST or W       (go west)");
               io.print("UP or U         (go up)");
               io.print("DOWN or D       (go down)");
               io.print("INVENT          (see your inventory - what you are carrying)");
               io.print("LOOK            (see where you are)");
               io.print("SUSPEND         (save game to finish later)");
               io.print("RESUME          (take up where you left off last time)");
               io.print("QUIT or STOP    (quit game)");
            }
            else
            {
               io.print("Welcome! The object of this game is simple: You just need to escape alive! You will use short commands (usually just one or two words) to do various things like move around, manipulate objects, and interact with your environment. To move, simply type a direction (using the first letter is fine: \"n\" for north, \"d\" for down, etc.). To be reminded of where you are, type \"look\". When you find objects, you can pick them up (\"get bottle\"), drop them (\"drop gold\"), or do other things (\"eat food\", \"wave wand\", etc.). To see what you are carrying, type \"inventory\" (\"invent\" for short). To save your game for later (or in case you think you are about about to do something perilous), type \"suspend\". To resume later, type \"resume\". To end the game, type \"quit\". The key is to use your imagination and just try things (like \"fly\", \"open door\", \"push button\", etc.). If what you are attempting to do does not work, try saying it another way. Have fun, and good luck!");
            }

            io.print("");
         }
         else if ((command.equals("QUIT") ||
                   command.equals("STOP")) &&
                  argument == null)
         {
            if (acknowledge)
            {
               io.print("Ok");
            }

            result |= RESULT_END_GAME;
         }
         else if (command.equals("GET") || command.equals("TAKE"))
         {
            if (argument != null)
            {
               player.getItem(argument, acknowledge);
            }
            else
            {
               io.print(command.substring(0, 1) +
                        command.substring(1).toLowerCase() +
                        " what?");
            }
         }
         else if (command.equals("DROP") || command.equals("THROW"))
         {
            if (argument != null)
            {
               player.dropItem(argument, acknowledge);
            }
            else
            {
               io.print(command.substring(0, 1) +
                        command.substring(1).toLowerCase() +
                        " what?");
            }
         }
         else if ((command.equals("INVENT") || command.equals("INVENTORY")) &&
                  argument == null)
         {
            player.listItems();
         }
         else if ((command.equals("SUSPEND") || command.equals("SAVE")) &&
                  argument == null)
         {
            if (suspend_interactive)
            {
               io.print("");
               io.print("OK, grab the following long line and save it away somewhere. This will be the command you use later to resume your game:");
               io.print("");
               io.printRaw("resume " + state());
               io.print("");
            }
            else
            {
               if (!io.saveSuspendedState(advname + ".sus", state()))
               {
                  io.print("Hmm, for some reason the game cannot be suspended. Sorry.");
               }
               else
               {
                  if (acknowledge)
                  {
                     io.print("Ok");
                  }
               }
            }
         }
         else if ((command.equals("RESUME") || command.equals("RESTORE")) &&
                  (!suspend_interactive && argument == null))
         {
            String old_state = io.loadSuspendedState(advname + ".sus");
            if (old_state == null)
            {
               io.print("Hmm, there seems to be no suspended game information. Sorry.");
            }
            else
            {
               if (!state(old_state))
               {
                  io.print("Hmm, the suspended game information doesn't look valid. Sorry.");
               }
               else
               {
                  result |= (RESULT_DESCRIBE | RESULT_NO_CHECK);
               }
            }
         }
         else if ((command.equals("RESUME") || command.equals("RESTORE")) &&
                  suspend_interactive)
         {
            if (argument == null)
            {
               io.print("Please follow this command with the code you were given when you suspended your game.");
            }
            else
            {
               if (!state(verbatim_argument))
               {
                  io.print("Hmm, that resume code just doesn't seem to make sense! Sorry.");
               }
               else
               {
                  result |= (RESULT_DESCRIBE | RESULT_NO_CHECK);
               }
            }
         }
         else if (custom != null)
         {
            if (trs_compat)
            {
               io.print("You can't do that here.");
            }
            else
            {
               if (!player_in_correct_room)
               {
                  io.print("You can't do that here.");
               }
               else
               {
                  if (action_denied_directive != null)
                  {
                     if (action_denied_directive.startsWith(":"))
                     {
                        io.print(action_denied_directive.substring(1));
                     }
                     else
                     {
                        io.print(action_denied_directive);
                     }
                  }
                  else
                  {
                     io.print("You can't do that yet.");
                  }
               }
            }
         }
         else
         {
            io.print("I don't understand.");
         }

         if (wants_to_walk)
         {
            if (goto_room != null)
            {
               int tilde_pos = goto_room.indexOf("^");
               if (tilde_pos != -1)
               {
                  goto_room = goto_room.substring(0, tilde_pos);
                  if (goto_room.equals(""))
                  {
                     goto_room = null;
                  }
               }
            }

            Room room = rooms.get(goto_room);
            if (room != null)
            {
               player.current_room = room;
               result |= RESULT_DESCRIBE;
            }
            else
            {
               io.print("You can't go that way.");
            }
         }
      }

      return result;
   }

   int takeAction(Command command)
   {
      return takeAction(command, false, RESULT_NORMAL);
   }

   int takeAction(Command command, boolean auto, int previous_result)
   {
      int result = RESULT_NORMAL;
      boolean error = false;

      if (command.action == null || command.action.startsWith("^"))
      {
         if (!auto)
         {
            io.print("Nothing happens.");
         }
      }
      else
      {
         ArrayList<String> messages = new ArrayList<String>();

         String action_str;
         int or_pos = command.action.indexOf("|");
         if (or_pos != -1)
         {
            action_str = command.action.substring(0, or_pos);
         }
         else
         {
            action_str = command.action;
         }

         String[] action_list;
         boolean action_one_shot;
         if (command.action.startsWith("."))
         {
            action_one_shot = true;
            action_list = action_str.substring(1).split(";", -1);
         }
         else
         {
            action_one_shot = false;
            action_list = action_str.split(";", -1);
         }

         for (int i=0; i<action_list.length; ++i)
         {
            String action = null;
            String message = null;

            int colon_pos = action_list[i].indexOf(":");
            if (colon_pos != -1)
            {
               action = action_list[i].substring(0, colon_pos);
               message = action_list[i].substring(colon_pos + 1);
            }
            else
            {
               action = action_list[i];
            }

            if (action.length() == 0)
            {
               action = null;
            }

            if (action != null)
            {
               if (action.startsWith("/"))
               {
                  Room room = rooms.get(action.substring(1));
                  if (room != null)
                  {
                     player.current_room = room;
                     result |= RESULT_DESCRIBE;
                  }
               }
               else if (action.startsWith("!"))
               {
                  io.print("");
                  io.print(action.substring(1));
                  //                        io.print("");
                  //                        io.print("It took you ? moves to win.");
                  result |= RESULT_WIN;
                  result |= RESULT_END_GAME;
               }
               else if (action.startsWith("="))
               {
                  result |= processCommand(action.substring(1), false);
               }
               else if (action.startsWith("%"))
               {
                  int comma_pos = action.indexOf(",");
                  if (comma_pos != -1)
                  {
                     String old_item = action.substring(1, comma_pos);
                     String new_item = action.substring(comma_pos + 1);

                     if (player.removeItem(old_item))
                     {
                        player.addItem(new_item, true);
                     }
                     else if (player.current_room.removeItem(old_item))
                     {
                        player.current_room.addItem(new_item, true);
                     }
                     else
                     {
                        io.print("You can't do that yet.");
                        error = true;
                     }
                  }
               }
               else if (action.startsWith("+"))
               {
                  if (action.substring(1).startsWith("$"))
                  {
                     if (!player.addItem(action.substring(2), false))
                     {
                        io.print("You are carrying too much to do that.");
                        error = true;
                     }
                     else
                     {
                        command.action = "^" + command.action;
                     }
                  }
                  else
                  {
                     player.current_room.addItem(action.substring(1),
                                                 true);

                     command.action = "^" + command.action;
                  }
               }
               else if (action.startsWith("-"))
               {
                  if (!player.removeItem(action.substring(1)))
                  {
                     if (!player.current_room.
                         removeItem(action.substring(1)))
                     {
                        io.print("You can't do that yet.");
                        error = true;
                     }
                  }
               }
               else if (action.startsWith("#"))
               {
                  int arrow_pos = action.indexOf(">");
                  if (arrow_pos != -1)
                  {
                     String room_name = action.substring(1, arrow_pos);
                     String item = action.substring(arrow_pos + 1);

                     if (player.removeItem(item) ||
                         player.current_room.removeItem(item))
                     {
                        Room room = rooms.get(room_name);
                        if (room != null)
                        {
                           room.addItem(item, true);
                        }
                        else
                        {
                           io.print("Wow, I think somthing just left our universe!");
                        }
                     }
                     else
                     {
                        io.print("You can't do that yet.");
                        error = true;
                     }
                  }
               }
               else if (action.startsWith("["))
               {
                  if (action.substring(1).startsWith("$"))
                  {
                     if (action.substring(2).startsWith("N"))
                     {
                        player.current_room.north_room =
                           Room.blockWay(player.current_room.
                                         north_room);
                     }
                     else if (action.substring(2).startsWith("S"))
                     {
                        player.current_room.south_room =
                           Room.blockWay(player.current_room.
                                         south_room);
                     }
                     else if (action.substring(2).startsWith("E"))
                     {
                        player.current_room.east_room =
                           Room.blockWay(player.current_room.
                                         east_room);
                     }
                     else if (action.substring(2).startsWith("W"))
                     {
                        player.current_room.west_room =
                           Room.blockWay(player.current_room.
                                         west_room);
                     }
                     else if (action.substring(2).startsWith("U"))
                     {
                        player.current_room.up_room =
                           Room.blockWay(player.current_room.
                                         up_room);
                     }
                     else if (action.substring(2).startsWith("D"))
                     {
                        player.current_room.down_room =
                           Room.blockWay(player.current_room.
                                         down_room);
                     }
                  }
                  else
                  {
                     if (action.substring(1).startsWith("N"))
                     {
                        player.current_room.north_room =
                           Room.makeWay(player.current_room.
                                        north_room,
                                        action.substring(2));
                     }
                     else if (action.substring(1).startsWith("S"))
                     {
                        player.current_room.south_room =
                           Room.makeWay(player.current_room.
                                        south_room,
                                        action.substring(2));
                     }
                     else if (action.substring(1).startsWith("E"))
                     {
                        player.current_room.east_room =
                           Room.makeWay(player.current_room.
                                        east_room,
                                        action.substring(2));
                     }
                     else if (action.substring(1).startsWith("W"))
                     {
                        player.current_room.west_room =
                           Room.makeWay(player.current_room.
                                        west_room,
                                        action.substring(2));
                     }
                     else if (action.substring(1).startsWith("U"))
                     {
                        player.current_room.up_room =
                           Room.makeWay(player.current_room.
                                        up_room,
                                        action.substring(2));
                     }
                     else if (action.substring(1).startsWith("D"))
                     {
                        player.current_room.down_room =
                           Room.makeWay(player.current_room.
                                        down_room,
                                        action.substring(2));
                     }
                  }

                  command.action = "^" + command.action;
               }
               else if (action.startsWith("*"))
               {
                  if (player.current_room.desc_ctrl != null)
                  {
                     if (action.substring(1).startsWith("+"))
                     {
                        if (!player.current_room.desc_ctrl.
                            endsWith("+"))
                        {
                           player.current_room.desc_ctrl =
                              player.current_room.desc_ctrl.
                              concat("+");
                        }
                     }
                     else
                     {
                        if (player.current_room.desc_ctrl.
                            endsWith("+"))
                        {
                           player.current_room.desc_ctrl =
                              player.current_room.desc_ctrl.
                              substring(0, player.current_room.
                                        desc_ctrl.length() - 1);
                        }
                     }
                  }
               }
               else if (action.startsWith("$"))
               {
                  int equals_pos = action.indexOf("=");
                  if (equals_pos != -1)
                  {
                     String variable_name = action.substring(1, equals_pos);
                     String value = action.substring(equals_pos + 1);

                     variables.put(variable_name, value);
                  }
               }
               else
               {
                  io.print("");
                  io.print(action);
                  result |= RESULT_DIE;
                  result |= RESULT_END_GAME;
               }
            }

            if (error)
            {
               break;
            }

            if (message != null)
            {
               messages.add(message);
            }
         }

         if (action_one_shot)
         {
            if (!command.action.startsWith("^"))
            {
               command.action = "^" + command.action;
            }
         }

         if (!messages.isEmpty())
         {
            if ((result & RESULT_DESCRIBE) != 0 || (!trs_compat && auto && (previous_result & RESULT_DESCRIBE) != 0))
            {
               io.print("");
            }
            for (String message : messages)
            {
               io.print(message);
            }
         }
      }

      /*
      if (error || (auto && result == RESULT_NORMAL))
      {
         return RESULT_NOTHING;
      }
      else
      {
         return result;
      }
      */
      return result;
   }

   boolean load(BufferedReader file_stream)
   {
      String start_room = null;
      Room first_room = null;
      Room new_room = null;
      Command new_command = null;
      String cur_room_name = null;

      while (true)
      {
         String line;
         try
         {
            line = file_stream.readLine();
         }
         catch (java.io.IOException e)
         {
            io.print("Error while reading file!");
            return false;
         }

         if (line == null)
            break;

         line = line.trim();

         if (!trs_look)
         {
            // Remove double spaces after punctuation
            line = line.replace("!  ", "! ");
            line = line.replace("?  ", "? ");
            line = line.replace(".  ", ". ");
         }

         if (line.startsWith("VERSION="))
         {
            version = Integer.parseInt(line.substring(line.indexOf("=") + 1));
         }
         else if (line.startsWith("TITLE="))
         {
            title = line.substring(line.indexOf("=") + 1);
         }
         else if (line.startsWith("START_ROOM="))
         {
            start_room = line.substring(line.indexOf("=") + 1);
         }
         else if (line.startsWith("INVENTORY_LIMIT="))
         {
            player.setItemLimit(Integer.valueOf(line.substring(line.indexOf("=") + 1)).intValue());
         }
         else if (line.startsWith("ROOM="))
         {
            new_room = new Room(this);
            new_room.name = line.substring(line.indexOf("=") + 1);
            rooms.put(new_room.name, new_room);
            room_list.add(new_room.name);
            if (first_room == null)
            {
               first_room = new_room;
            }

            cur_room_name = new_room.name;

            new_command = null;
         }
         else if (line.startsWith("LOCAL="))
         {
            cur_room_name = line.substring(line.indexOf("=") + 1);

            new_room = null;
            new_command = null;
         }
         else if (line.equals("GLOBAL"))
         {
            cur_room_name = null;

            new_room = null;
            new_command = null;
         }
         else if (line.startsWith("COMMAND="))
         {
            new_command = new Command();
            commands.add(new_command);

            String cmd_str = line.substring(line.indexOf("=") + 1);

            if (cmd_str.startsWith("+") ||
                cmd_str.startsWith("-") ||
                cmd_str.startsWith("$"))
            {
               new_command.condition = cmd_str;
            }
            else
            {
               int pos = cmd_str.indexOf(":");
               if (pos != -1)
               {
                  new_command.condition = cmd_str.substring(pos + 1);

                  if (!(new_command.condition.startsWith("+") ||
                        new_command.condition.startsWith("-") ||
                        new_command.condition.startsWith("$")))
                  {
                     new_command.condition = "+" + new_command.condition;
                  }

                  new_command.commands = cmd_str.substring(0, pos).split(",", -1);
               }
               else
               {
                  new_command.commands = cmd_str.split(",", -1);
               }
            }

            if (new_command.condition != null && new_command.condition.endsWith("+"))
            {
               new_command.condition = new_command.condition.replaceFirst(".$", "");
               new_command.cont = true;
            }

            if (cur_room_name != null)
            {
               new_command.location = new String(cur_room_name);
            }
         }
         else if (line.startsWith("ACTION="))
         {
            //
            // If there is no current command, or if there is one,
            // but it already has an action, make a new command.
            //
            if (new_command == null || new_command.action != null)
            {
               new_command = new Command();
               commands.add(new_command);

               if (cur_room_name != null)
               {
                  new_command.location = new String(cur_room_name);
               }
            }

            new_command.action = line.substring(line.indexOf("=") + 1);
         }
         else if (line.startsWith("DESC="))
         {
            if (new_room != null)
            {
               new_room.desc = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("ALT_DESC="))
         {
            if (new_room != null)
            {
               new_room.desc_alt = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("DESC_CONTROL="))
         {
            if (new_room != null)
            {
               new_room.desc_ctrl = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("FIXED_OBJECTS="))
         {
            if (new_room != null)
            {
               new_room.fixed_objects = new ArrayList<String>();
               String[] objects = line.substring(line.indexOf("=") + 1).split(",", -1);
               for (String item : objects)
               {
                  new_room.fixed_objects.add(item);
               }

               use_fixed_objects = true;
            }
         }
         else if (line.startsWith("CONTENTS="))
         {
            if (new_room != null)
            {
               new_room.items = new ArrayList<String>();
               String[] contents = line.substring(line.indexOf("=") + 1).split(",", -1);
               for (String item : contents)
               {
                  new_room.items.add(item);
               }
            }
         }
         else if (line.startsWith("NORTH="))
         {
            if (new_room != null)
            {
               new_room.north_room = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("SOUTH="))
         {
            if (new_room != null)
            {
               new_room.south_room = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("EAST="))
         {
            if (new_room != null)
            {
               new_room.east_room = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("WEST="))
         {
            if (new_room != null)
            {
               new_room.west_room = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("UP="))
         {
            if (new_room != null)
            {
               new_room.up_room = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("DOWN="))
         {
            if (new_room != null)
            {
               new_room.down_room = line.substring(line.indexOf("=") + 1);
            }
         }
         else if (line.startsWith("ITEM DESC "))
         {
            int colon_pos = line.indexOf(":");
            item_descs.put(line.substring(10, colon_pos),
                           line.substring(colon_pos + 1));
         }
         else if (line.startsWith("PLURAL ITEM "))
         {
            plural_items.add(line.substring(12));
         }
         else if (line.startsWith("MASS ITEM "))
         {
            mass_items.add(line.substring(10));
         }
         else if (line.startsWith("SAME ITEM "))
         {
            int equals_pos = line.indexOf("=");
            same_items.put(line.substring(10, equals_pos),
                           line.substring(equals_pos + 1));
         }
         else if (line.startsWith("OLD ITEM "))
         {
            int equals_pos = line.indexOf("=");
            old_items.put(line.substring(9, equals_pos),
                          line.substring(equals_pos + 1));
         }
         else if (line.startsWith("OLD VERSION "))
         {
            String old_version_args = line.substring(12);
            int space_pos = old_version_args.indexOf(" ");
            old_versions.put(Integer.parseInt(old_version_args.substring(0, space_pos)),
                             old_version_args.substring(space_pos + 1));
         }
      }

      // Sort commands so globals are last
      ArrayList<Command> tmp_commands = commands;
      commands = new ArrayList<Command>();

      for (Command c : tmp_commands)
      {
         if (c.location != null)
         {
            commands.add(c);
         }
      }

      for (Command c : tmp_commands)
      {
         if (c.location == null)
         {
            commands.add(c);
         }
      }

      // Set up the starting room
      player.current_room = rooms.get(start_room);
      if (player.current_room == null)
      {
         if (first_room == null)
         {
            player.current_room = new Room(this);
            player.current_room.name = "limbo";
            player.current_room.desc =
               "This adventure has no rooms. You are in limbo!";
         }
         else
         {
            player.current_room = first_room;
         }
      }

      return true;
   }

   String state()
   {
      StringBuffer buf = new StringBuffer();

      //
      // our current room
      //
      buf.append(player.current_room.name);

      buf.append(";");

      //
      // what we're carrying
      //
      for (int i=0; i<player.items.size(); ++i)
      {
         buf.append(player.items.get(i));
         buf.append(",");
      }

      if (buf.charAt(buf.length() - 1) == ',')
      {
         buf.setLength(buf.length() - 1);
      }

      buf.append(";");

      //
      // and the command numbers having actions that have been "done"
      //
      ArrayList<Command> rev_commands = new ArrayList<Command>(commands);
      Collections.reverse(rev_commands);
      for (Command command : rev_commands)
      {
         if (command.action != null && command.action.startsWith("^"))
         {
            buf.append("^");
         }
         else
         {
            buf.append(".");
         }
      }

      buf.append(";");

      //
      // now the room details that have changed
      //
      ArrayList<String> rev_room_list = new ArrayList<String>(room_list);
      Collections.reverse(rev_room_list);
      for (String room_name : rev_room_list)
      {
         Room room = rooms.get(room_name);

         if (room.desc_ctrl != null &&
             room.desc_ctrl.endsWith("+"))
         {
            buf.append("+");
         }
         else
         {
            buf.append(".");
         }

         buf.append(":");

         buf.append(Room.saveWay(room.north_room));

         buf.append(":");

         buf.append(Room.saveWay(room.south_room));

         buf.append(":");

         buf.append(Room.saveWay(room.east_room));

         buf.append(":");

         buf.append(Room.saveWay(room.west_room));

         buf.append(":");

         buf.append(Room.saveWay(room.up_room));

         buf.append(":");

         buf.append(Room.saveWay(room.down_room));

         buf.append(":");

         if (buf.toString().endsWith(".:::::::"))
         {
            buf.setLength(buf.length() - 8);
         }
         else if (buf.toString().endsWith(":::::::"))
         {
            buf.setLength(buf.length() - 6);
         }

         //
         // the items in the room
         //
         for (int i=0; i<room.items.size(); ++i)
         {
            buf.append(room.items.get(i));
            buf.append(",");
         }

         if (buf.charAt(buf.length() - 1) == ',')
         {
            buf.setLength(buf.length() - 1);
         }

         buf.append(";");
      }

      if (buf.charAt(buf.length() - 1) == ';')
      {
         buf.setLength(buf.length() - 1);
      }

      int checksum = 0;
      for (int i=0; i<buf.length(); ++i)
      {
         checksum += (int) buf.charAt(i);
      }

      buf.insert(0, (char) ((checksum & 0x3f) + 0x21));
      buf.insert(0, (char) (((checksum >> 6) & 0x3f) + 0x21));

      //io.print("Raw string: " + String.valueOf(suspend_version) + ":" + String.valueOf(version) + ":" + buf.toString());
      return String.valueOf(suspend_version) + ":" + String.valueOf(version) + ":" + ExpUtil.encrypt(buf.toString());
   }

   boolean state(String s)
   {
      String state_str;
      int saved_suspend_version, saved_adventure_version;

      if (s == null)
      {
         return false;
      }

      int colon_pos = s.indexOf(":");
      if (colon_pos == -1 || s.charAt(0) < '0' || s.charAt(0) > '9')
      {
         return false;
      }
      else
      {
         saved_suspend_version = Integer.parseInt(s.substring(0, colon_pos));

         int next_colon_pos = s.indexOf(":", colon_pos + 1);
         if (next_colon_pos == -1)
         {
            return false;
         }

         saved_adventure_version = Integer.parseInt(s.substring(colon_pos + 1, next_colon_pos));
         state_str = ExpUtil.decrypt(s.substring(next_colon_pos + 1));
      }

      //io.print("Suspend Version = " + String.valueOf(saved_suspend_version));
      //io.print("Adventure Version = " + String.valueOf(saved_adventure_version));

      // Cannot handle suspend versions lower than 1
      if (saved_suspend_version < 1)
      {
         return false;
      }

      if (state_str.length() < 2)
      {
         return false;
      }

      int num_commands_delta = 0;
      if (old_versions.containsKey(saved_adventure_version))
      {
         String[] version_changes = old_versions.get(saved_adventure_version).split(",", -1);
         for (String version_change : version_changes)
         {
            if (version_change.startsWith("NUM_COMMANDS"))
            {
               num_commands_delta = Integer.parseInt(version_change.substring(12));
            }
         }
      }

      int checksum = 0;
      for (int i=2; i<state_str.length(); ++i)
      {
         checksum += (int) state_str.charAt(i);
      }

      int checksum_high = ((checksum >> 6) & 0x3f) + 0x21;
      int checksum_low = (checksum & 0x3f) + 0x21;

      // Fix a problem in which the '`' character gets converted to ' '
      // in the encryption/decryption process.
      if (checksum_high == 0x60)
      {
         checksum_high = 0x20;
      }

      if (checksum_low == 0x60)
      {
         checksum_low = 0x20;
      }

      StringBuffer checksum_str = new StringBuffer();
      checksum_str.append((char) checksum_high);
      checksum_str.append((char) checksum_low);

      if (!checksum_str.toString().equals(state_str.substring(0, 2)))
      {
         return false;
      }

      String[] parts = state_str.substring(2).split(";", -1);

      if (rooms.size() != parts.length - 3)
      {
         return false;
      }

      if (commands.size() != parts[2].length() + num_commands_delta)
      {
         return false;
      }

      //
      // Recover the current room.
      //
      Room prev_room = player.current_room;
      player.current_room = rooms.get(parts[0]);
      if (player.current_room == null)
      {
         player.current_room = prev_room;

         return false;
      }

      //
      // Recover the player's items.
      //
      player.items = new ArrayList<String>();

      if (!parts[1].equals(""))
      {
         String[] saved_items = parts[1].split(",", -1);
         for (String item : saved_items)
         {
            if (old_items.containsKey(item))
            {
               player.items.add(old_items.get(item));
            }
            else
            {
               player.items.add(item);
            }
         }
      }

      //
      // Recover the state of the actions.
      //
      int command_idx = -num_commands_delta;
      ArrayList<Command> rev_commands = new ArrayList<Command>(commands);
      Collections.reverse(rev_commands);
      for (Command command : rev_commands)
      {
         if (command_idx >= 0 && command.action != null)
         {
            if (parts[2].charAt(command_idx) == '^' &&
                !command.action.startsWith("^"))
            {
               command.action = "^" + command.action;
            }
            else if (parts[2].charAt(command_idx) != '^' &&
                     command.action.startsWith("^"))
            {
               command.action = command.action.substring(1);
            }
         }

         ++command_idx;
      }

      //
      // Recover the room details.
      //
      int room_idx = 0;
      ArrayList<String> rev_room_list = new ArrayList<String>(room_list);
      Collections.reverse(rev_room_list);
      for (String room_name : rev_room_list)
      {
         Room room = rooms.get(room_name);

         String[] room_code = parts[room_idx + 3].split(":", -1);
         if (room_code.length != 8)
         {
            String[] old = room_code;
            room_code = new String[8];
            if (old.length == 1)
            {
               room_code[0] = ".";
               room_code[1] = "";
               room_code[2] = "";
               room_code[3] = "";
               room_code[4] = "";
               room_code[5] = "";
               room_code[6] = "";
               room_code[7] = old[0];
            }
            else if (old.length == 2)
            {
               room_code[0] = old[0];
               room_code[1] = "";
               room_code[2] = "";
               room_code[3] = "";
               room_code[4] = "";
               room_code[5] = "";
               room_code[6] = "";
               room_code[7] = old[1];
            }
            else
            {
               io.print("Warning! Error in decoding suspended game.");
               io.print("         The state of your game is inconsistent.");
               io.print("         Please start game over and report this");
               io.print("         problem to the developer.");
               return false;
            }
         }

         //
         // first the description control
         //
         if (room.desc_ctrl != null)
         {
            if (room_code[0].charAt(0) == '+' &&
                !room.desc_ctrl.endsWith("+"))
            {
               room.desc_ctrl = room.desc_ctrl.concat("+");
            }
            else if (room_code[0].charAt(0) != '+' &&
                     room.desc_ctrl.endsWith("+"))
            {
               room.desc_ctrl =
                  room.desc_ctrl.substring(0,
                                           room.desc_ctrl.length() - 1);
            }
         }

         //
         // now the possible directions
         //
         if (room_code[1].equals(""))
         {
            room.north_room = Room.originalWay(room.north_room);
         }
         else
         {
            room.north_room = room_code[1];
         }

         if (room_code[2].equals(""))
         {
            room.south_room = Room.originalWay(room.south_room);
         }
         else
         {
            room.south_room = room_code[2];
         }

         if (room_code[3].equals(""))
         {
            room.east_room = Room.originalWay(room.east_room);
         }
         else
         {
            room.east_room = room_code[3];
         }

         if (room_code[4].equals(""))
         {
            room.west_room = Room.originalWay(room.west_room);
         }
         else
         {
            room.west_room = room_code[4];
         }

         if (room_code[5].equals(""))
         {
            room.up_room = Room.originalWay(room.up_room);
         }
         else
         {
            room.up_room = room_code[5];
         }

         if (room_code[6].equals(""))
         {
            room.down_room = Room.originalWay(room.down_room);
         }
         else
         {
            room.down_room = room_code[6];
         }

         //
         // now the contents
         //
         room.items = new ArrayList<String>();

         if (!room_code[7].equals(""))
         {
            String[] saved_items = room_code[7].split(",", -1);
            for (String item : saved_items)
            {
               if (old_items.containsKey(item))
               {
                  room.items.add(old_items.get(item));
               }
               else
               {
                  room.items.add(item);
               }
            }
         }

         ++room_idx;
      }

      return true;
   }
}

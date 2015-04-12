//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

class Player extends ItemContainer
{
   private ExpIO io;

   Room current_room;

   Player(ExpIO i)
   {
      io = i;
   }
   
   void getItem(String item, boolean acknowledge)
   {
      String full_item_room = current_room.expandItemName(item);
      String full_item_self = expandItemName(item);

      if (!current_room.hasItem(full_item_room))
      {
         if (hasItem(full_item_self))
         {
            io.print("You are already carrying the " + full_item_self + ".");
         }
         else
         {
            if (World.trs_look)
            {
               io.print("I see no " + item + " here that you can pick up.");
            }
            else
            {
               io.print("I see no " + item.toLowerCase() + " here that you can pick up.");
            }
         }
      }
      else if (!addItem(full_item_room, false))
      {
         io.print("Your hands are full - you can't carry any more.");
      }
      else
      {
         current_room.removeItem(full_item_room);
            
         if (acknowledge)
         {
            io.print("Ok");
         }
      }
   }

   void dropItem(String item, boolean acknowledge)
   {
      String full_item = expandItemName(item);
        
      if (!removeItem(full_item))
      {
         String item_lower = item.toLowerCase();
         if (World.trs_look)
         {
            io.print("You are not carrying " +
                     ExpUtil.aOrAn(item_lower) + " " + item + ".");
         }
         else
         {
            io.print("You are not carrying " +
                     ExpUtil.aOrAn(item_lower) + " " + item_lower + ".");
         }
      }
      else
      {
         current_room.addItem(full_item, true);

         if (acknowledge)
         {
            io.print("Ok");
         }
      }
   }

   void listItems()
   {
      if (hasNoItems())
      {
         io.print("You are not carrying anything.");
      }
      else
      {
         io.print("");
         io.print("You are currently holding the following:");
         io.print("");

         for (int i=0; i<items.length; ++i)
         {
            if (items[i] != null)
            {
               if (World.trs_look)
               {
                  io.print("- " + items[i] + " -");
               }
               else
               {
                  io.print("- " + items[i].toLowerCase());
               }
            }
         }

         io.print("");
      }
   }
}

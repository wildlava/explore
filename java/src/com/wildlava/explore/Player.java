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

   Player(ExpIO i, World world)
   {
      super(world);
      
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
            if (World.trs_look)
            {
               io.print("You are already carrying the " + full_item_self + ".");
            }
            else
            {
               io.print("You are already carrying the " + full_item_self.toLowerCase() + ".");
            }
         }
         else
         {
            if (World.trs_compat || !World.use_fixed_objects)
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
            else
            {
               String[] word_list = ExpUtil.parseToArray(item, " ");
               String item_part = word_list[word_list.length - 1];
               boolean found_fixed_object = true;
               
               if (!current_room.hasFixedObject(item_part))
               {
                  if (word_list.length > 1)
                  {
                     item_part = word_list[0];
                     if (!current_room.hasFixedObject(item_part))
                     {
                        found_fixed_object = false;
                     }
                  }
                  else
                  {
                     found_fixed_object = false;
                  }
               }

               if (found_fixed_object)
               {
                  io.print("I see no way to pick up the " + item_part.toLowerCase() + ".");
               }
               else
               {
                  io.print("I see no " + item.toLowerCase() + " here.");
               }
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
         
         if (World.trs_compat)
         {
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
            io.print("You have no " + item_lower + ".");
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

         for (int i=0; i<items.size(); ++i)
         {
            if (World.trs_look)
            {
               io.print("- " + items.get(i) + " -");
            }
            else
            {
               io.print("- " + items.get(i).toLowerCase());
            }
         }

         io.print("");
      }
   }
}

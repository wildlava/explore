//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

import java.util.ArrayList;

class Room extends ItemContainer
{
   String name;
   String desc;
   String desc_alt;
   String desc_ctrl;
   String north_room;
   String south_room;
   String east_room;
   String west_room;
   String up_room;
   String down_room;
    
   Room next;

   String description()
   {
      ArrayList<String> desc_strings = new ArrayList<String>();
      String ctrl = "RC";

      if (desc_ctrl != null)
      {
         int pos = desc_ctrl.indexOf(",");

         if (pos == -1)
         {
            ctrl = desc_ctrl;
         }
         else
         {
            if (desc_ctrl.endsWith("+"))
            {
               ctrl = desc_ctrl.substring(pos + 1);
            }
            else
            {
               ctrl = desc_ctrl.substring(0, pos);
            }
         }
      }

      boolean r = (ctrl.indexOf("R") != -1 && desc != null);
      boolean c = (ctrl.indexOf("C") != -1 && desc_alt != null);

      if (r && c)
      {
         desc_strings.add(desc + "\\" + desc_alt);
      }
      else if (r)
      {
         desc_strings.add(desc);
      }
      else if (c)
      {
         desc_strings.add(desc_alt);
      }

      if (items != null)
      {
         for (int i=0; i<items.length; ++i)
         {
            if (items[i] != null)
            {
               String item_lower = items[i].toLowerCase();
               if (World.trs_look)
               {
                  desc_strings.add("There is " + ExpUtil.aOrAn(item_lower) +
                                   " " + items[i] + " here.");
               }
               else
               {
                  desc_strings.add("There is " + ExpUtil.aOrAn(item_lower) +
                                   " " + item_lower + " here.");
               }
            }
         }
      }            

      return ExpUtil.join(desc_strings, "\n");
   }

   static String blockWay(String dir_room)
   {
      if (dir_room != null)
      {
         int pos = dir_room.indexOf("^");
         if (pos != -1)
         {
            dir_room = dir_room.substring(pos);
         }
         else
         {
            dir_room = "^" + dir_room;
         }
      }
      else
      {
         dir_room = "^";
      }

      return dir_room;
   }                        

   static String makeWay(String dir_room, String new_room)
   {
      return new_room + blockWay(dir_room);
   }

   static String originalWay(String dir_room)
   {
      if (dir_room != null)
      {
         int pos = dir_room.indexOf("^");
         if (pos != -1)
         {
            String orig_way = dir_room.substring(pos + 1);
            if (orig_way.equals(""))
            {
               return null;
            }
            else
            {
               return orig_way;
            }
         }
      }

      return dir_room;
   }

   static String saveWay(String dir_room)
   {
      if (dir_room != null && dir_room.indexOf("^") != -1)
      {
         return dir_room;
      }
      else
      {
         return "";
      }
   }
}
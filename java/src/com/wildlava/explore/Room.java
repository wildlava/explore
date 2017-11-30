//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

class Room extends ItemContainer
{
   String name;
   String desc;
   String desc_alt;
   String desc_ctrl;
   List<String> fixed_objects;
   Map<String, String> neighbors;
   Map<String, String> original_neighbors;

   Room(World world)
   {
      super(world);

      neighbors = new HashMap<>();
      original_neighbors = new HashMap<>();
   }

   String description()
   {
      List<String> desc_strings = new ArrayList<>();
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

      for (int i=0; i<items.size(); ++i)
      {
         String item = items.get(i);
         String item_lower = item.toLowerCase();

         if (World.trs_compat)
         {
            desc_strings.add("There is " + ExpUtil.aOrAn(item_lower) +
                             " " + item + " here.");
         }
         else
         {
            if (world.item_descs.containsKey(item))
            {
               desc_strings.add(world.item_descs.get(item));
            }
            else if (world.plural_items.contains(item))
            {
               desc_strings.add("There are some " + item_lower + " here.");
            }
            else if (world.mass_items.contains(item))
            {
               desc_strings.add("There is some " + item_lower + " here.");
            }
            else
            {
               desc_strings.add("There is " + ExpUtil.aOrAn(item_lower) +
                                " " + item_lower + " here.");
            }
         }
      }

      return ExpUtil.join(desc_strings, "\n");
   }

   boolean hasFixedObject(String item)
   {
      if (fixed_objects != null)
      {
         return fixed_objects.contains(item);
      }
      else
      {
         return false;
      }
   }

   String neighbor(String direction)
   {
      return neighbors.get(direction);
   }

   void initNeighbor(String direction, String room)
   {
      if (room == null)
      {
         neighbors.remove(direction);
         original_neighbors.remove(direction);
      }
      else
      {
         neighbors.put(direction, room);
         original_neighbors.put(direction, room);
      }
   }

   void setNeighbor(String direction, String room)
   {
      if (room == null)
      {
         neighbors.remove(direction);
      }
      else
      {
         neighbors.put(direction, room);
      }
   }

   void revertNeighbor(String direction)
   {
      String original_neighbor = original_neighbors.get(direction);
      if (original_neighbor == null)
      {
         neighbors.remove(direction);
      }
      else
      {
         neighbors.put(direction, original_neighbor);
      }
   }

   void blockWay(String direction)
   {
      setNeighbor(direction, null);
   }

   void makeWay(String direction, String new_room)
   {
      setNeighbor(direction, new_room);
   }

   String neighborSaveString(String direction)
   {
      String neighbor = neighbors.get(direction);
      String original_neighbor = original_neighbors.get(direction);

      if (neighbor == null && original_neighbor == null)
      {
         return "";
      }

      if (neighbor != null && neighbor.equals(original_neighbor))
      {
         return "";
      }

      if (neighbor == null)
      {
         return "^";
      }
      else
      {
         return neighbor;
      }
   }
}

//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

import java.util.ArrayList;

class ItemContainer
{
   World world;

   ArrayList<String> items;
   int item_limit = -1;

   ItemContainer(World world)
   {
      this.world = world;

      items = new ArrayList<String>();
   }

   String expandItemName(String item)
   {
      String same_item = null;

      if (world.same_items.containsKey(item))
      {
         same_item = world.same_items.get(item);
      }

      for (int i=0; i<items.size(); ++i)
      {
         String test_item = items.get(i);

         if (test_item.equals(item))
         {
            return test_item;
         }

         if (same_item != null && test_item.equals(same_item))
         {
            return test_item;
         }

         String[] word_list = ExpUtil.parseToArray(test_item, " ");
         if (word_list.length > 1)
         {
            if (word_list[0].equals(item) ||
                word_list[word_list.length - 1].equals(item))
            {
               return test_item;
            }
         }
      }

      return item;
   }

   boolean hasNoItems()
   {
      return items.isEmpty();
   }

   boolean hasItem(String item)
   {
      return items.contains(item);
   }

   boolean isFull()
   {
      return ((item_limit != -1) && (items.size() >= item_limit));
   }

   boolean addItem(String item, boolean no_limit)
   {
      if (isFull() && !no_limit)
      {
         return false;
      }

      items.add(new String(item));

      return true;
   }

   void setItemLimit(int n)
   {
      item_limit = n;
   }

   boolean removeItem(String item)
   {
      if (items.contains(item))
      {
         items.remove(items.indexOf(item));
         return true;
      }

      return false;
   }
}

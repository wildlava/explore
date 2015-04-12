//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

class ItemContainer
{
   String[] items;

   String expandItemName(String item)
   {
      if (items != null)
      {
         for (int i=0; i<items.length; ++i)
         {
            if (items[i] != null)
            {
               if (items[i].equals(item))
               {
                  return items[i];
               }

               String[] word_list = ExpUtil.parseToArray(items[i], " ");
               if (word_list.length > 1)
               {
                  if (word_list[0].equals(item) ||
                      word_list[word_list.length - 1].equals(item))
                  {
                     return items[i];
                  }
               }
            }
         }
      }
        
      return item;
   }

   boolean hasNoItems()
   {
      if (items != null)
      {
         for (int i=0; i<items.length; ++i)
         {
            if (items[i] != null)
            {
               return false;
            }
         }
      }

      return true;
   }
    
   boolean hasItem(String item)
   {
      if (items != null)
      {
         for (int i=0; i<items.length; ++i)
         {
            if (items[i] != null && items[i].equals(item))
            {
               return true;
            }
         }
      }
        
      return false;
   }

   boolean isFull()
   {
      if (items != null)
      {
         for (int i=0; i<items.length; ++i)
         {
            if (items[i] == null)
            {
               return false;
            }
         }
      }

      return true;
   }
    
   boolean addItem(String item, boolean mayExpand)
   {
      if (items != null)
      {
         for (int i=0; i<items.length; ++i)
         {
            if (items[i] == null)
            {
               items[i] = new String(item);

               return true;
            }
         }
      }
        
      if (!mayExpand)
      {
         return false;
      }

      expandItemList(1);
      items[items.length - 1] = new String(item);

      return true;
   }

   void expandItemList(int n)
   {
      if (items == null)
      {
         items = new String[n];
      }
      else
      {
         String[] old_items = items;
         items = new String[old_items.length + n];
            
         for (int i=0; i<old_items.length; ++i)
         {
            items[i] = old_items[i];
         }
      }
   }
    
   boolean removeItem(String item)
   {
      if (items != null)
      {
         for (int i=0; i<items.length; ++i)
         {
            if (items[i] != null && items[i].equals(item))
            {
               items[i] = null;

               return true;
            }
         }
      }

      return false;
   }
}

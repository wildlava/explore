//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.lang.StringBuilder;

class ExpUtil
{
   static String[] parseToArray(String list, String delim)
   {
      int start;
      int num_items = 1;
      int pos;

      if (list == null)
      {
         return null;
      }

      //
      // First count the number of delimeters in the string.
      //
      start = 0;
      while ((pos = list.substring(start).indexOf(delim)) != -1)
      {
         start += pos + 1;
         ++num_items;
      }

      //
      // Allocate the array to hold the right number of items.
      //
      String[] s = new String[num_items];

      //
      // Copy the items into the array.
      //
      start = 0;
      for (int i=0; i<num_items; ++i)
      {
         pos = list.substring(start).indexOf(delim);
         if (pos == -1)
         {
            s[i] = list.substring(start);
         }
         else
         {
            s[i] = list.substring(start, start + pos);
         }

         start += pos + 1;
      }

      return s;
   }

   static String superTrim(String s)
   {
      if (s == null)
      {
         return null;
      }

      String[] words = parseToArray(s, " ");
      String new_s = "";

      for (int i=0; i<words.length; ++i)
      {
         if (!words[i].equals(""))
         {
            new_s = new_s.concat(words[i].trim() + " ");
         }
      }

      return new_s.trim();
   }

   static String join(AbstractCollection s, String delimiter)
   {
      StringBuilder buffer = new StringBuilder();
      Iterator iter = s.iterator();

      if (iter.hasNext())
      {
         buffer.append(iter.next());
         while (iter.hasNext())
         {
            buffer.append(delimiter);
            buffer.append(iter.next());
         }
      }

      return buffer.toString();
   }

   static String aOrAn(String s)
   {
      if (s == null || s.length() < 1)
      {
         return "a";
      }

      if (s.charAt(0) == 'a' ||
          s.charAt(0) == 'e' ||
          s.charAt(0) == 'i' ||
          s.charAt(0) == 'o' ||
          s.charAt(0) == 'u')
      {
         return "an";
      }
      else
      {
         return "a";
      }
   }

   static String runLengthEncode(String s)
   {
      if (s == null)
      {
         return null;
      }

      StringBuffer result = new StringBuffer();

      int i = 0;
      while (i < s.length())
      {
         if (s.charAt(i) == '/')
         {
            result.append("//");

            ++i;
         }
         else
         {
            int j = i + 1;
            while (j < s.length() &&
                   s.charAt(j) == s.charAt(i))
            {
               ++j;
            }

            if ((j - i) > 4)
            {
               result.append('/');
               result.append(s.charAt(i));
               result.append(j - i);
               result.append('/');

               i = j;
            }
            else
            {
               result.append(s.charAt(i));

               ++i;
            }
         }
      }

      return result.toString();
   }

   static String runLengthDecode(String s)
   {
      if (s == null)
      {
         return null;
      }

      StringBuffer result = new StringBuffer();

      int i = 0;
      while (i < s.length())
      {
         if (s.charAt(i) == '/')
         {
            if (i < (s.length() - 1))
            {
               int pos = s.indexOf('/', i + 1);
               if (pos == -1)
               {
                  //
                  // error!
                  //

                  return null;
               }
               else if (pos == (i + 1))
               {
                  result.append('/');

                  i += 2;
               }
               else
               {
                  try
                  {
                     for (int j=0;
                          j<Integer.valueOf(s.substring(i + 2, pos)).
                             intValue();
                          ++j)
                     {
                        result.append(s.charAt(i + 1));
                     }
                  }
                  catch (java.lang.NumberFormatException x)
                  {
                     return null;
                  }

                  i = pos + 1;
               }
            }
            else
            {
               //
               // error!
               //

               return null;
            }
         }
         else
         {
            result.append(s.charAt(i));

            ++i;
         }
      }

      return result.toString();
   }

   private final static String key = "We were inspired by Steely Dan.";

   static String encrypt(String s)
   {
      StringBuffer buf = new StringBuffer();

      for (int i=0; i<s.length(); ++i)
      {
         char c = s.charAt(i);

         c -= 0x20;
         c &= 0x3f;
         c ^= key.charAt(i % key.length()) & 0x3f;
         c += 0x3b;

         buf.insert(0, c);
      }

      return buf.toString();
   }

   static String decrypt(String s)
   {
      StringBuffer buf = new StringBuffer();

      int i = 0;
      while (i < s.length())
      {
         char c = s.charAt(s.length() - i - 1);

         c -= 0x3b;
         c &= 0x3f;
         c ^= key.charAt(i % key.length()) & 0x3f;
         c += 0x20;

         buf.append(c);

         ++i;
      }

      return buf.toString();
   }
}

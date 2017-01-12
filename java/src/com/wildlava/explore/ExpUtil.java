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
   static String superTrim(String s)
   {
      if (s == null)
      {
         return null;
      }

      String[] words = s.split(" ", -1);
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

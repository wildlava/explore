//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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
      byte[] key_bytes = key.getBytes();
      int key_len = key_bytes.length;

      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      try
      {
         DeflaterOutputStream deflater = new DeflaterOutputStream(stream);
         deflater.write(s.getBytes());
         deflater.close();
      }
      catch (java.io.IOException e) {
         return "Failed";
      }

      byte[] bytes = stream.toByteArray();

      for (int i=0; i<bytes.length; i++)
      {
         bytes[i] ^= (byte) (key_bytes[i % key_len]);
      }

      return ExpIO.encodeBase64(bytes);
   }

   static String decrypt(String s)
   {
      byte[] key_bytes = key.getBytes();
      int key_len = key_bytes.length;

      byte[] bytes = ExpIO.decodeBase64(s);

      for (int i=0; i<bytes.length; i++)
      {
         bytes[i] ^= (byte) (key_bytes[i % key_len]);
      }

      String decompressed_string = "";
      String decompressed_line;
      ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
      try
      {
         InflaterInputStream inflater = new InflaterInputStream(stream);
         InputStreamReader reader = new InputStreamReader(inflater);
         BufferedReader in = new BufferedReader(reader);

         while ((decompressed_line = in.readLine()) != null)
         {
            decompressed_string += decompressed_line;
         }
      }
      catch (java.io.IOException e) {
         return "Decompress failed";
      }

      return decompressed_string;
   }

   static String oldDecrypt(String s)
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

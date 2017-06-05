//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2010  Joe Peterson
//

package com.wildlava.explore;

import java.util.Base64;

class ExpIO
{
   public boolean unwrap = false;
   public int max_line_length = 79;

   private StringBuffer output;

   ExpIO()
   {
      output = new StringBuffer();
   }

   void print(String s)
   {
      String out_str;

      if (unwrap)
      {
         out_str = s.replace("\\\\", "\n\n");
         out_str = out_str.replace("\\ ", "\n ");
         out_str = out_str.replace("\\", " ");
      }
      else
      {
         out_str = s.replace("\\", "\n");
      }

      for (String line : out_str.split("\n", -1))
      {
         while (line.length() > max_line_length)
         {
            int last_space_pos = line.lastIndexOf(' ', max_line_length);
            if (last_space_pos == -1)
            {
               break;
            }
            else
            {
               printRaw(line.substring(0, last_space_pos), true);
               line = line.substring(last_space_pos + 1);
            }
         }

         printRaw(line, true);
      }
   }

   void printRaw(String s)
   {
      printRaw(s, true);
   }

   void printRaw(String s, boolean new_line)
   {
      output.append(s);

      if (new_line)
      {
         output.append("\n");
      }
   }

   String getOutput()
   {
      return output.toString();
   }

   boolean saveSuspendedState(String filename, String state)
   {
      return false;
   }

   String loadSuspendedState(String filename)
   {
      return null;
   }

   static String encodeBase64(byte[] bytes)
   {
      Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
      return encoder.encodeToString(bytes);
   }

   static byte[] decodeBase64(String s)
   {
      Base64.Decoder decoder = Base64.getUrlDecoder();

      if (s.contains("+"))
      {
         s = s.replace("+", "-");
      }

      if (s.contains("/"))
      {
         s = s.replace("/", "_");
      }

      return decoder.decode(s);
   }
}

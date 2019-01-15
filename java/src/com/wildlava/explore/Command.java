//
// Explore
//    - The Adventure Interpreter
//
// Copyright (C) 2006  Joe Peterson
//

package com.wildlava.explore;

class Command
{
   String[] commands;
   String condition;
   String location;
   String[] actions;
   String denied_directive;
   boolean fall_back_to_builtin;
   boolean cont;
   boolean one_shot;
   boolean disabled;

   Command()
   {
      commands = null;
      condition = null;
      location = null;
      actions = null;
      denied_directive = null;
      fall_back_to_builtin = true;
      cont = false;
      one_shot = false;
      disabled = false;
   }
}

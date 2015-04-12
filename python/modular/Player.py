#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

import ExpIO
import ExpUtil
from ItemContainer import *

class Player(ItemContainer):
    def __init__(self, exp_io):
        ItemContainer.__init__(self)

        self.exp_io = exp_io

        self.current_room = None
        
    def get_item(self, item, acknowledge):
        full_item_room = self.current_room.expand_item_name(item)
        full_item_self = self.expand_item_name(item)
            
        if not self.current_room.has_item(full_item_room):
            if self.has_item(full_item_self):
                self.exp_io.tell("You are already carrying the " + full_item_self + ".")
            else:
                self.exp_io.tell("I see no " + item + " here that you can pick up.")
        elif not self.add_item(full_item_room, False):
            self.exp_io.tell("Your hands are full - you can't carry any more.")
        else:
            self.current_room.remove_item(full_item_room)
            
            if acknowledge:
                self.exp_io.tell("Ok")

    def drop_item(self, item, acknowledge):
        full_item = self.expand_item_name(item)
        
        if not self.remove_item(full_item):
            self.exp_io.tell("You are not carrying " + ExpUtil.a_or_an(item) + " " + item + ".")
        else:
            self.current_room.add_item(full_item, True)
            
            if acknowledge:
                self.exp_io.tell("Ok")

    def list_items(self):
        if self.has_no_items():
            self.exp_io.tell("You are not carrying anything.")
        else:
            self.exp_io.tell("")
            self.exp_io.tell("You are currently holding the following:")
            self.exp_io.tell("")
            
            for item in self.items:
                self.exp_io.tell("- " + item + " -")
                
            self.exp_io.tell("")

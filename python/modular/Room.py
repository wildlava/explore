#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

import string

import World
import ExpUtil
from ItemContainer import *

class Room(ItemContainer):
    def __init__(self):
        ItemContainer.__init__(self)
        
        self.name = None
        self.desc = None
        self.desc_alt = None
        self.desc_ctrl = None
        self.neighbors = [None, None, None, None, None, None]
        self.original_neighbors = [None, None, None, None, None, None]
    
    def neighbor(self, direction):
        try:
            return self.neighbors[World.DIRECTIONS.index(direction)]
        except ValueError:
            return None
        
    def init_neighbor(self, direction, room):
        try:
            i = World.DIRECTIONS.index(direction)
        except ValueError:
            return
        
        self.neighbors[i] = room
        self.original_neighbors[i] = room
        
    def set_neighbor(self, direction, room):
        try:
            i = World.DIRECTIONS.index(direction)
        except ValueError:
            return

        self.neighbors[i] = room
        
    def revert_neighbor(self, direction):
        try:
            i = World.DIRECTIONS.index(direction)
        except ValueError:
            return

        self.neighbors[i] = self.original_neighbors[i]
        
    def block_way(self, direction):
        self.set_neighbor(direction, None)
        
    def make_way(self, direction, new_room):
        self.set_neighbor(direction, new_room)
        
    def neighbor_save_string(self, direction):
        try:
            i = World.DIRECTIONS.index(direction)
        except ValueError:
            return ""
        
        if self.neighbors[i] != self.original_neighbors[i]:
            if self.neighbors[i] != None:
                return self.neighbors[i]
            else:
                return "^"
        else:
            return ""
        
    def description(self):
        ctrl = "RC"
        out_desc = []
        
        if self.desc_ctrl != None:
            pos = self.desc_ctrl.find(",")
            if pos == -1:
                ctrl = self.desc_ctrl
            else:
                if self.desc_ctrl[-1] == "+":
                    ctrl = self.desc_ctrl[pos + 1:]
                else:
                    ctrl = self.desc_ctrl[:pos]

        if ctrl.find("R") != -1 and self.desc != None:
            out_desc.append(self.desc)

        if ctrl.find("C") != -1 and self.desc_alt != None:
            out_desc.append(self.desc_alt)

        if len(self.items) > 0:
            for item in self.items:
                out_desc.append("There is " + ExpUtil.a_or_an(item) + " " + item + " here.")

        return string.join(out_desc, '\n')

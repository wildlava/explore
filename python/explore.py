#!/usr/bin/env python

#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2010  Joe Peterson
#

import sys
import os
import time
import string
import zlib
import base64

def a_or_an(s):
    s_lower = s.lower()
        
    if s_lower[0] in ['a', 'e', 'i', 'o', 'u']:
        return "an"
    else:
        return "a"


class ExpIO:
    def __init__(self):
        self.output = []
        self.store_output = False
        self.no_delay = False

    def tell(self, s):
        lines = s.split('\n')

        if self.store_output:
            self.output.extend(lines)
        else:
            for i in range(len(lines)):
                if not self.no_delay:
                    time.sleep(.03)
                print lines[i]

                #if i < (len(lines) - 1):
                #    sys.stdout.write(lines[i] + '\n')
                #else:
                #    sys.stdout.write(lines[i])

                #sys.stdout.flush()

    #def clear_output(self):
    #    if len(self.output) > 0:
    #        self.output = []

    def get_output(self):
        if self.store_output:
            return self.output
        else:
            return None


class Command:
    #location = None
    #commands = []
    #condition = None
    #actions = []
    
    def __init__(self):
        self.location = None
        self.commands = []
        self.condition = None
        self.actions = []
    
    def add_action(self, action):
        self.actions.append(action)


class ItemContainer:
    def __init__(self):
        self.items = []
        self.item_limit = None
        
    def has_no_items(self):
        return len(self.items) == 0
    
    def has_item(self, item):
        return item in self.items

    def is_full(self):
        if self.item_limit == None or len(self.items) < self.item_limit:
            return False
        else:
            return True
    
    def expand_item_name(self, item):
        if item in self.items:
            return item
            
        for test_item in self.items:
            word_list = test_item.split()
            if len(word_list) > 1:
                if word_list[0] == item or word_list[-1] == item:
                    return test_item
            
        return item

    def add_item(self, item, mayExpand):
        if not self.is_full() or mayExpand:
            self.items.append(item)
            return True
        else:
            return False

    def remove_item(self, item):
        if item in self.items:
            self.items.remove(item)
            return True
        else:
            return False


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
            return self.neighbors[DIRECTIONS.index(direction)]
        except ValueError:
            return None
        
    def init_neighbor(self, direction, room):
        try:
            i = DIRECTIONS.index(direction)
        except ValueError:
            return
        
        self.neighbors[i] = room
        self.original_neighbors[i] = room
        
    def set_neighbor(self, direction, room):
        try:
            i = DIRECTIONS.index(direction)
        except ValueError:
            return

        self.neighbors[i] = room
        
    def revert_neighbor(self, direction):
        try:
            i = DIRECTIONS.index(direction)
        except ValueError:
            return

        self.neighbors[i] = self.original_neighbors[i]
        
    def block_way(self, direction):
        self.set_neighbor(direction, None)
        
    def make_way(self, direction, new_room):
        self.set_neighbor(direction, new_room)
        
    def neighbor_save_string(self, direction):
        try:
            i = DIRECTIONS.index(direction)
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
                item_lower = item.lower()
                out_desc.append("There is " + a_or_an(item_lower) + " " + item_lower + " here.")

        return string.join(out_desc, '\n')


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
                self.exp_io.tell("I see no " + item.lower() + " here that you can pick up.")
        elif not self.add_item(full_item_room, False):
            self.exp_io.tell("Your hands are full - you can't carry any more.")
        else:
            self.current_room.remove_item(full_item_room)
            
            if acknowledge:
                self.exp_io.tell("Ok")

    def drop_item(self, item, acknowledge):
        full_item = self.expand_item_name(item)
        
        if not self.remove_item(full_item):
            item_lower = item.lower()
            self.exp_io.tell("You are not carrying " + a_or_an(item_lower) + " " + item_lower + ".")
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
                #self.exp_io.tell("- " + item + " -")
                self.exp_io.tell("- " + item.lower())
                
            self.exp_io.tell("")


RESULT_NOTHING = 0
RESULT_NORMAL = 1
RESULT_DESCRIBE = 2
RESULT_WIN = 4
RESULT_DIE = 8
RESULT_END_GAME = 16
RESULT_NO_CHECK = 32
RESULT_SUSPEND = 64
RESULT_RESUME = 128

SUSPEND_INTERACTIVE = 0
SUSPEND_QUIET = 1

LONG_DIRECTION_COMMANDS = ["NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN"]
DIRECTIONS = []
for dir in LONG_DIRECTION_COMMANDS:
    DIRECTIONS.append(dir[0])
DIRECTION_COMMANDS = DIRECTIONS + LONG_DIRECTION_COMMANDS

class World:
    def __init__(self, exp_io):
        self.exp_io = exp_io

        self.title = "This adventure has no title!"
        self.rooms = {}
        self.commands = []
        self.player = Player(exp_io)
        self.suspend_mode = SUSPEND_INTERACTIVE
        self.last_suspend = None

        self.trs_compat = False
        
    def load(self, filename):
        start_room = None
        first_room = None
        new_room = None
        new_command = None
        cur_room_name = None

        file_stream = file(filename)

        for line in file_stream:
            line = string.translate(string.strip(line), \
                                    string.maketrans('\\', '\n'))

            if line.find("=") != -1:
                keyword, params = line.split("=", 1)
            else:
                keyword = line
                params = None

            if keyword == "TITLE":
                self.title = params[:]

            elif keyword == "START_ROOM":
                start_room = params[:]

            elif keyword == "INVENTORY_LIMIT":
                self.player.item_limit = string.atoi(params)

            elif keyword == "ROOM":
                new_room = Room()
                new_room.name = params[:]
                self.rooms[new_room.name] = new_room
                if first_room == None:
                    first_room = new_room

                cur_room_name = new_room.name

                new_command = None

            elif keyword == "LOCAL":
                cur_room_name = params[:]

                new_room = None
                new_command = None

            elif keyword == "GLOBAL":
                cur_room_name = None

                new_room = None
                new_command = None

            elif keyword == "COMMAND":
                new_command = Command()
                self.commands.append(new_command)

                if cur_room_name != None:
                    new_command.location = cur_room_name[:]

                if params[0] == "+":
                    new_command.condition = params[1:]
                elif params[0] == "-":
                    new_command.condition = params[:]
                else:
                    pos = params.find(":")
                    if pos != -1:
                        if params[pos + 1] == "+":
                            new_command.condition = params[pos + 2:]
                        else:
                            new_command.condition = params[pos + 1:]

                        new_command.commands = params[:pos].split(",")
                    else:
                        new_command.commands = params.split(",")

            elif keyword == "ACTION":
                # If there is no current command, make one.
                if new_command == None:
                    new_command = Command()
                    self.commands.append(new_command)

                    if cur_room_name != None:
                        new_command.location = cur_room_name[:]

                if len(new_command.actions) != 0:
                    self.exp_io.tell("Build warning: extra action ignored!")
                else:    
                    for action in params.split(";"):
                        new_command.add_action(action)
                        #new_command.actions.append(action)

            elif keyword == "DESC":
                if new_room != None:
                    new_room.desc = params[:]

            elif keyword == "ALT_DESC":
                if new_room != None:
                   new_room.desc_alt = params[:]

            elif keyword == "DESC_CONTROL":
                if new_room != None:
                   new_room.desc_ctrl = params[:]

            elif keyword == "CONTENTS":
                if new_room != None:
                   new_room.items = params.split(",")

            elif keyword == "NORTH":
                if new_room != None:
                   new_room.init_neighbor("N", params[:])

            elif keyword == "SOUTH":
                if new_room != None:
                   new_room.init_neighbor("S", params[:])

            elif keyword == "EAST":
                if new_room != None:
                   new_room.init_neighbor("E", params[:])

            elif keyword == "WEST":
                if new_room != None:
                   new_room.init_neighbor("W", params[:])

            elif keyword == "UP":
                if new_room != None:
                   new_room.init_neighbor("U", params[:])

            elif keyword == "DOWN":
                if new_room != None:
                   new_room.init_neighbor("D", params[:])

        if self.rooms.has_key(start_room):
            self.player.current_room = self.rooms[start_room]
        elif first_room != None:
            self.player.current_room = first_room
        else:
            self.player.current_room = Room()
            self.player.current_room.name = "limbo"
            self.player.current_room.desc = \
                "This adventure has no rooms.  You are in limbo!"

        file_stream.close()

    def take_action(self, command, auto):
        result = RESULT_NORMAL
        error = False

        if len(command.actions) == 0 or len(command.actions[0]) == 0 or command.actions[0][0] == "^":
            result = RESULT_NOTHING

            if not auto:
                self.exp_io.tell("Nothing happens.")
        else:
            for action in command.actions:
                if action.find(":") != -1:
                    action, message = action.split(":", 1)
                else:
                    message = None

                if len(action) == 0:
                    action = None

                if action != None:
                    if action[0] == "/":
                        if self.rooms.has_key(action[1:]):
                            room = self.rooms[action[1:]]

                            self.player.current_room = room
                            result |= RESULT_DESCRIBE

                    elif action[0] == "!":
                        self.exp_io.tell("")
                        self.exp_io.tell(action[1:])
                        #self.exp_io.tell("")
                        #self.exp_io.tell("It took you ? moves to win.")
                        result |= RESULT_WIN
                        result |= RESULT_END_GAME

                    elif action[0] == "=":
                        result |= self.process_command(action[1:], False)

                    elif action[0] == "%":
                        if action.find(",") != -1:
                            old_item, new_item = action[1:].split(",", 1)

                            if self.player.remove_item(old_item):
                                self.player.add_item(new_item, True)
                            elif self.player.current_room.remove_item(old_item):
                                self.player.current_room.add_item(new_item, True)
                            else:
                                self.exp_io.tell("You can't do that yet.")
                                error = True

                    elif action[0] == "+":
                        if action[1] == "$":
                            if not self.player.add_item(action[2:], False):
                                self.exp_io.tell("You are carrying too much to do that.")
                                error = True
                            else:
                                command.actions[0] = "^" + command.actions[0]
                        else:
                            self.player.current_room.add_item(action[1:], True)
                            command.actions[0] = "^" + command.actions[0]

                    elif action[0] == "-":
                        if not self.player.remove_item(action[1:]):
                            if not self.player.current_room.remove_item(action[1:]):
                                self.exp_io.tell("You can't do that yet.")
                                error = True

                    elif action[0] == "#":
                        if action.find(">") != -1:
                            room_name, item = action[1:].split(">", 1)

                            if self.player.remove_item(item) or self.player.current_room.remove_item(item):
                                if self.rooms.has_key(room_name):
                                    self.rooms[room_name].add_item(item, True)
                                else:
                                    self.exp_io.tell("Wow, I think somthing just left our universe!")
                            else:
                                self.exp_io.tell("You can't do that yet.")
                                error = True

                    elif action[0] == "[":
                        if action[1] == "$":
                            self.player.current_room.block_way(action[2])
                        else:
                            self.player.current_room.make_way(action[1], action[2:])

                        command.actions[0] = "^" + command.actions[0]

                    elif action[0] == "*":
                        if self.player.current_room.desc_ctrl != None:
                            if action[1] == "+":
                                if not (len(self.player.current_room.desc_ctrl) > 0 and self.player.current_room.desc_ctrl[-1] == "+"):
                                    self.player.current_room.desc_ctrl += "+"
                            else:
                                if len(self.player.current_room.desc_ctrl) > 0 and self.player.current_room.desc_ctrl[-1] == "+":
                                    self.player.current_room.desc_ctrl = self.player.current_room.desc_ctrl[:-1]

                    else:
                        self.exp_io.tell("")
                        self.exp_io.tell(action)
                        result |= RESULT_DIE
                        result |= RESULT_END_GAME

                if error:
                    break

                if message != None:
                    if (result & RESULT_DESCRIBE) != 0:
                        self.exp_io.tell("")
                    self.exp_io.tell(message)

        if error or (auto and result == RESULT_NORMAL):
            return RESULT_NOTHING
        else:
            return result

    def check_for_auto(self):
        result = RESULT_NOTHING

        for c in self.commands:
            if (c.location == None or \
                c.location == self.player.current_room.name) and \
                len(c.commands) == 0:
                if c.condition == None or \
                   (c.condition[0] == "-" and \
                    not self.player.has_item(c.condition[1:])) or \
                   (c.condition != "-" and \
                    self.player.has_item(c.condition)):

                    result |= self.take_action(c, True)

        return result

    def find_custom(self, cmd, r):
        global_candidate = None
        candidate = None

        for c in self.commands:
            if cmd in c.commands:
                # Give priority to commands that are specific to
                # this room (if specified), otherwise remember it
                # as a candidate.
                if r == None or c.location == r.name:
                    return c
                elif c.location == None:
                    global_candidate = c
                else:
                    candidate = c

        if global_candidate != None:
            return global_candidate
        else:
            return candidate

    def process_command(self, wish, acknowledge):
        result = RESULT_NORMAL
        player_meets_condition = False
        player_in_correct_room = False
        command = None
        argument = None
        verbatim_argument = ""

        # Save the argument before case conversion in case someone needs it.
        pos = wish.find(" ")
        if pos != -1:
            verbatim_argument = wish[pos + 1:]

        wish = wish.upper()

        custom = self.find_custom(wish, self.player.current_room)

        if custom != None:
            if custom.condition == None or \
               (custom.condition[0] == "-" and \
                not self.player.has_item(custom.condition[1:])) or \
               (custom.condition != "-" and \
                self.player.has_item(custom.condition)):

                player_meets_condition = True

            if custom.location == None or \
               self.player.current_room.name == custom.location:

                player_in_correct_room = True

        try_builtin = True

        if self.trs_compat:
            if custom != None and player_in_correct_room:
                try_builtin = False
                if player_meets_condition:
                    result = self.take_action(custom, False)
                else:
                    self.exp_io.tell("You can't do that yet.")
        else:
            if custom != None and player_in_correct_room and player_meets_condition:
                try_builtin = False
                result = self.take_action(custom, False)

        if try_builtin:
            if wish.find(" ") != -1:
                command, argument = wish.split(None, 1)
            else:
                command = wish[:]

            wants_to_walk = False
            goto_room = None

            if command == "GO":
                if argument == None:
                    self.exp_io.tell("Go where?")
                elif argument in DIRECTION_COMMANDS:
                     self.exp_io.tell('No need to say "go" for the simple directions.')
                else:
                     self.exp_io.tell("I'm not sure how to get there.  Try a direction.")

            elif command == "LOOK":
                if argument != None:
                    self.exp_io.tell("There's really nothing more to see.")

                result |= RESULT_DESCRIBE

            elif command in DIRECTION_COMMANDS and argument == None:
                wants_to_walk = True
                goto_room = self.player.current_room.neighbor(command[0])

            elif command == "HELP":
                self.exp_io.tell("""
    These are some of the commands you may use:

    NORTH or N      (go north)
    SOUTH or S      (go south)
    EAST or E       (go east)
    WEST or W       (go west)
    UP or U         (go up)
    DOWN or D       (go down)
    INVENT          (see your inventory - what you are carrying)
    LOOK            (see where you are)
    SUSPEND         (save game to finish later)
    RESUME          (take up where you left off last time)
    QUIT or STOP    (quit game)
    """)

            elif (command == "QUIT" or \
                  command == "STOP") and \
                 argument == None:
                if acknowledge:
                    self.exp_io.tell("Ok")

                result |= RESULT_END_GAME

            elif command == "GET" or command == "TAKE":
                if argument != None:
                    self.player.get_item(argument, acknowledge)
                else:
                    self.exp_io.tell("Get what?")

            elif command == "DROP" or command == "THROW":
                if argument != None:
                    self.player.drop_item(argument, acknowledge)
                else:
                    self.exp_io.tell("Drop what?")

            elif (command == "INVENT" or command == "INVENTORY") and \
                 argument == None:
                self.player.list_items()

            elif (command == "SUSPEND" or command == "SAVE") and \
                 argument == None:
                #self.exp_io.tell("Sorry, suspend has not yet been implemented.")
                if self.suspend_mode == SUSPEND_INTERACTIVE:
                    self.exp_io.tell("")
                    self.exp_io.tell("OK, grab the following long line and save it away somewhere.")
                    self.exp_io.tell("This will be the command you use to resume your game:")
                    self.exp_io.tell("")
                    self.exp_io.tell("resume " + self.get_state())
                    self.exp_io.tell("")
                else:
                    if acknowledge:
                        self.exp_io.tell("Ok")
                    result |= RESULT_SUSPEND

            elif (command == "RESUME" or command == "RESTORE") and \
                 (self.suspend_mode != SUSPEND_INTERACTIVE and argument == None):
                if self.last_suspend != None:
                    if not self.set_state(self.last_suspend):
                        self.exp_io.tell("Hmm, the suspended game information doesn't look valid.  Sorry.")
                    else:
                        result |= (RESULT_DESCRIBE | RESULT_NO_CHECK)
                else:
                    self.exp_io.tell("Hmm, there seems to be no suspended game information.  Sorry.")

            elif (command == "RESUME" or command == "RESTORE") and \
                 self.suspend_mode == SUSPEND_INTERACTIVE:
                #self.exp_io.tell("Sorry, resume has not yet been implemented.")
                if argument == None:
                    self.exp_io.tell("Please follow this command with the code you were given")
                    self.exp_io.tell("when you suspended your game.")
                else:
                    if not self.set_state(verbatim_argument):
                        self.exp_io.tell("Hmm, that resume code just doesn't seem to make sense!  Sorry.")
                    else:
                        result |= (RESULT_DESCRIBE | RESULT_NO_CHECK)

            elif custom != None:
                if self.trs_compat:
                    self.exp_io.tell("You can't do that here.")
                else:
                    if not player_in_correct_room:
                        self.exp_io.tell("You can't do that here.")
                    else:
                        self.exp_io.tell("You can't do that yet.")
            else:
                self.exp_io.tell("I don't understand.")

            if wants_to_walk:
                if self.rooms.has_key(goto_room):
                    room = self.rooms[goto_room]
                    self.player.current_room = room
                    result |= RESULT_DESCRIBE
                else:
                    self.exp_io.tell("You can't go that way.")

        return result

    def get_state(self):
        # our current room
        buf = [self.player.current_room.name]

        # what we're carrying
        buf.append(string.join(self.player.items, ','))

        # and the command numbers having actions that have been "done"
        command_buf = []
        for command in self.commands:
            if len(command.actions) > 0 and len(command.actions[0]) > 0 and command.actions[0][0] == "^":
                command_buf.append("^")
            else:
                command_buf.append(".")

        buf.append(string.join(command_buf, ''))

        # now the room details that have changed
        for room_name, room in sorted(self.rooms.iteritems()):
            if room.desc_ctrl != None and len(room.desc_ctrl) > 0 and room.desc_ctrl[-1] == "+":
                room_data_buf = ["+"]
            else:
                room_data_buf = ["."]

            for dir in DIRECTIONS:
                room_data_buf.append(room.neighbor_save_string(dir))

            #if len(room_data_string) > 7 and room_data_string[-8:] == ".:::::::":
            #    buf.append(room_data_string[:-8])
            #elif len(room_data_string) > 6 and room_data_string[-7:] == ":::::::":
            #    buf.append(room_data_string[:-6])
            #else:

            # the items in the room
            room_data_buf.append(string.join(room.items, ','))

            buf.append(string.join(room_data_buf, ':'))

        buf_string = string.join(buf, ';')
        checksum = 0
        for i in range(len(buf_string)):
            checksum += ord(buf_string[i])

        #print "Raw string: " + chr(((checksum >> 6) & 0x3f) + 0x21) + chr((checksum & 0x3f) + 0x21) + buf_string
        return base64.b64encode(zlib.compress(chr(((checksum >> 6) & 0x3f) + 0x21) + chr((checksum & 0x3f) + 0x21) + buf_string))

    def set_state(self, s):
        try:
            state_str = zlib.decompress(base64.b64decode(s))
        except:
            return False

        if len(state_str) < 2:
            return False

        checksum = 0
        for i in range(2, len(state_str)):
            checksum += ord(state_str[i])

        checksum_str = chr(((checksum >> 6) & 0x3f) + 0x21) + chr((checksum & 0x3f) + 0x21)

        if checksum_str != state_str[:2]:
            return False

        parts = state_str[2:].split(';')

        if len(self.rooms) != len(parts) - 3:
            return False

        if len(self.commands) != len(parts[2]):
            return False

        # Recover the current room.
        prev_room = self.player.current_room
        try:
            self.player.current_room = self.rooms[parts[0]]
        except KeyError:
            # If the room name is invalid, recover the previous location and
            # return error status.
            self.player.current_room = prev_room
            return False

        # Recover the player's items.
        if parts[1] == "":
            new_player_items = []
        else:
            new_player_items = parts[1].split(',')

        # If the player now has more than he can carry, which should never
        # happen, recover the previous location and return error status.
        if len(new_player_items) > self.player.item_limit:
            self.player.current_room = prev_room
            return False
        else:
            self.player.items = new_player_items

        # Recover the state of the actions.
        command_idx = 0
        for command in self.commands:
            if len(command.actions) > 0:
                if parts[2][command_idx] == '^' and (len(command.actions[0]) == 0 or command.actions[0][0] != '^'):
                    command.actions[0] = "^" + command.actions[0]
                elif parts[2][command_idx] != '^' and (len(command.actions[0]) > 0 and command.actions[0][0] == '^'):
                    command.actions[0] = command.actions[0][1:]

            command_idx += 1

        # Recover the room details.
        room_idx = 0
        for room_name, room in sorted(self.rooms.iteritems()):
            room_code = parts[room_idx + 3].split(':')
            #if len(room_code) != 8:
            #    old = room_code
            #    room_code = new String[8]
            #    if (old.length == 1)
            #    {
            #       room_code[0] = "."
            #       room_code[1] = ""
            #       room_code[2] = ""
            #       room_code[3] = ""
            #       room_code[4] = ""
            #       room_code[5] = ""
            #       room_code[6] = ""
            #       room_code[7] = old[0]
            #    }
            #    else if (old.length == 2)
            #    {
            #       room_code[0] = old[0]
            #       room_code[1] = ""
            #       room_code[2] = ""
            #       room_code[3] = ""
            #       room_code[4] = ""
            #       room_code[5] = ""
            #       room_code[6] = ""
            #       room_code[7] = old[1]
            #    }
            #    else
            #    {
            #       # How would we recover from this?  For now, don't handle,
            #       # since we do not use this code.
            #       #return False
            #    }
            # }

            # first the description control
            if room.desc_ctrl != None:
                if room_code[0] == '+' and (len(room.desc_ctrl) == 0 or room.desc_ctrl[-1] != "+"):
                    room.desc_ctrl += "+"
                elif room_code[0] != "+" and (len(room.desc_ctrl) > 0 and room.desc_ctrl[-1] == '+'):
                    room.desc_ctrl = room.desc_ctrl[:-1]

            # now the possible directions

            # Remove anything after the "^", since we just want the
            # "current room" (note that this is for backwards compatibility
            # with the old save format, which saved the new room in the
            # following format: "curr_room^orig_room".
            for i in range(1, 7):
                if len(room_code[i]) > 0 and room_code[i][0] != "^":
                    pos = room_code[i].find("^")
                    if pos != -1:
                        room_code[i] = room_code[i][:pos]

                dir = DIRECTIONS[i - 1]

                if room_code[i] == "":
                    room.revert_neighbor(dir)
                elif room_code[i][0] == "^":
                    room.block_way(dir)
                else:
                    room.make_way(dir, room_code[i])

            # now the contents
            if room_code[7] == "":
                room.items = []
            else:
                room.items = room_code[7].split(',')

            room_idx += 1

        return True


def play(filename=None, no_delay=False, trs_compat=False):
    exp_io = ExpIO()
    world = World(exp_io)

    exp_io.no_delay = no_delay
    world.trs_compat = trs_compat
        
    exp_io.tell("")
    exp_io.tell("")
    exp_io.tell("*** EXPLORE ***  ver 4.8")

    if filename == None:
        exp_io.tell("")

        while True:
            advname = raw_input("Name of adventure: ")
            if advname != "":
                break

        advname = advname.strip().lower()
        filename = advname + ".exp"
    else:
        advname = os.path.basename(filename)
        if advname.find(".") != -1:
            advname, extension = advname.split(".", 1)

    exp_io.tell("")
    exp_io.tell(advname + " is now being built...")

    try:
        world.load(filename)
    except IOError:
        try:
            world.load(os.path.join('/usr/share/explore/adventures', filename))
        except IOError:
            print >> sys.stderr, 'Adventure not found'
            sys.exit(1)
            
    exp_io.tell("")
    exp_io.tell("")
    exp_io.tell(world.title)
    exp_io.tell("")

    result = RESULT_DESCRIBE

    while True:
        if (result & RESULT_NO_CHECK) == 0:
            check_result = world.check_for_auto()
            if check_result != RESULT_NOTHING:
                result = check_result

                if (result & RESULT_END_GAME) != 0:
                    break
                else:
                    continue

        if (result & RESULT_DESCRIBE) != 0:
            exp_io.tell("")
            exp_io.tell(world.player.current_room.description())

        wish = raw_input(":")
        wish = string.join(string.split(wish))
        if wish != "":
            result = world.process_command(wish, True)
            if (result & RESULT_END_GAME) != 0:
                if (result & RESULT_WIN) != 0:
                    exp_io.tell("")
                    exp_io.tell("Congratulations, you have successfully completed this adventure!")
                break
        else:
            result = RESULT_NORMAL

    exp_io.tell("")
    

def play_once(filename, command=None, resume=None, last_suspend=None, trs_compat=False, return_output=True, quiet=False, show_title=True, show_title_only=False):
    exp_io = ExpIO()
    world = World(exp_io)

    exp_io.no_delay = True
    world.trs_compat = trs_compat
    world.suspend_mode = SUSPEND_QUIET
    world.last_suspend = last_suspend
    if return_output:
        exp_io.store_output = True
        
    if show_title_only and not show_title:
        exp_io.tell("%ERROR=Incompatible flags")
        return exp_io.get_output()

    if command != None:
        show_title = False
        quiet = True

    if not quiet:
        exp_io.tell("")
        exp_io.tell("")
        exp_io.tell("*** EXPLORE ***  ver 4.8")

    advname = os.path.basename(filename)
    if advname.find(".") != -1:
        advname, extension = advname.split(".", 1)

    if not quiet:
        exp_io.tell("")
        exp_io.tell(advname + " is now being built...")

    world.load(filename)

    if show_title_only:
        exp_io.tell(world.title)
        return exp_io.get_output()

    if show_title:
        exp_io.tell("")
        exp_io.tell("")
        exp_io.tell(world.title)
        exp_io.tell("")

    if resume != None:
        if not world.set_state(resume):
            exp_io.tell("%ERROR=Bad resume code")
            return exp_io.get_output()

    if command != None:
        wish = string.join(string.split(command))
        if wish != "":
            result = world.process_command(wish, True)
        else:
            result = RESULT_NORMAL
    else:
        result = RESULT_DESCRIBE
        
    if (result & RESULT_NO_CHECK) == 0:
        check_result = world.check_for_auto()
        if check_result != RESULT_NOTHING:
            result = check_result

    if (result & RESULT_DESCRIBE) != 0:
        exp_io.tell("")
        exp_io.tell(world.player.current_room.description())
        
    if (result & RESULT_WIN) != 0:
        exp_io.tell("%WIN")
    elif (result & RESULT_DIE):
        exp_io.tell("%DIE")
    elif (result & RESULT_END_GAME):
        exp_io.tell("%END")
    else:
        exp_io.tell("%PROMPT=:")
        exp_io.tell("%STATE=" + world.get_state())
        
        if (result & RESULT_SUSPEND):
            exp_io.tell("%SUSPEND")
        
    return exp_io.get_output()


filename = None
one_shot = False
command = None
resume = None
last_suspend = None
no_delay = False
trs_compat = False

for arg_num in range(len(sys.argv)):
    if sys.argv[arg_num] == "-f":
        if len(sys.argv) > (arg_num + 1) and (len(sys.argv[arg_num + 1]) == 0 or sys.argv[arg_num + 1][0] != '-'):
            filename = sys.argv[arg_num + 1]
        else:
            print >> sys.stderr, "Error: Missing adventure filename"
            sys.exit(1)
    elif sys.argv[arg_num] == "-q":
        quiet = True
    elif sys.argv[arg_num] == "-c":
        if len(sys.argv) > (arg_num + 1) and (len(sys.argv[arg_num + 1]) == 0 or sys.argv[arg_num + 1][0] != '-'):
            command = sys.argv[arg_num + 1]
    elif sys.argv[arg_num] == "-r":
        if len(sys.argv) > (arg_num + 1) and (len(sys.argv[arg_num + 1]) == 0 or sys.argv[arg_num + 1][0] != '-'):
            resume = sys.argv[arg_num + 1]
    elif sys.argv[arg_num] == "-s":
        if len(sys.argv) > (arg_num + 1) and (len(sys.argv[arg_num + 1]) == 0 or sys.argv[arg_num + 1][0] != '-'):
            last_suspend = sys.argv[arg_num + 1]
    elif sys.argv[arg_num] == "--one-shot":
        one_shot = True
    elif sys.argv[arg_num] == "--no-delay":
        no_delay = True
    elif sys.argv[arg_num] == "--trs-compat":
        trs_compat = True
#    elif sys.argv[arg_num] == "--no-title":
#        show_title = False
#    elif sys.argv[arg_num] == "--title-only":
#        show_title_only = True

if one_shot or (command != None) or (resume != None) or (last_suspend != None):
    play_once(filename, command, resume, last_suspend, trs_compat, False)
else:
    play(filename, no_delay, trs_compat)

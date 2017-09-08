#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2010  Joe Peterson
#

import sys
import os
import time
import zlib
import base64
import re

trs_compat = False
use_fixed_objects = False
max_line_length = 79;

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
        s = s.replace('\\', '\n')

        out_lines = []
        for line in s.split('\n'):
            while len(line) > max_line_length:
                last_space_pos = line.rfind(' ', 0, max_line_length + 1);
                if last_space_pos == -1:
                    break
                else:
                    out_lines.append(line[:last_space_pos])
                    line = line[last_space_pos + 1:]

            out_lines.append(line)

        for out_line in out_lines:
            self.tell_raw(out_line)

    def tell_raw(self, s):
        if self.store_output:
            self.output.append(s)
        else:
            print(s)
            if not self.no_delay:
                time.sleep(.03)

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
        self.commands = None
        self.condition = None
        self.action = None
        self.cont = False


class ItemContainer:
    def __init__(self, world):
        self.world = world

        self.fixed_objects = []
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

        if item in self.world.same_items:
            same_item = self.world.same_items[item]
            if same_item in self.items:
                return same_item

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
    def __init__(self, world):
        ItemContainer.__init__(self, world)

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

        neighbor = self.neighbors[i]

        if neighbor == self.original_neighbors[i]:
            return ""

        if neighbor == None:
            return "^"
        else:
            return neighbor

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

        for item in self.items:
            item_lower = item.lower()
            if trs_compat:
                out_desc.append("There is " + a_or_an(item_lower) + " " + item + " here.")
            else:
                if item in self.world.item_descs:
                    out_desc.append(self.world.item_descs[item])
                elif item in self.world.plural_items:
                    out_desc.append("There are some " + item_lower + " here.")
                elif item in self.world.mass_items:
                    out_desc.append("There is some " + item_lower + " here.")
                else:
                    out_desc.append("There is " + a_or_an(item_lower) + " " + item_lower + " here.")

        return '\n'.join(out_desc)

    def has_fixed_object(self, item):
        return item in self.fixed_objects


class Player(ItemContainer):
    def __init__(self, exp_io, world):
        ItemContainer.__init__(self, world)

        self.exp_io = exp_io

        self.current_room = None

    def get_item(self, item, acknowledge):
        full_item_room = self.current_room.expand_item_name(item)
        full_item_self = self.expand_item_name(item)

        if not self.current_room.has_item(full_item_room):
            if self.has_item(full_item_self):
                if trs_compat:
                    self.exp_io.tell("You are already carrying the " + full_item_self + ".")
                else:
                    self.exp_io.tell("You are already carrying the " + full_item_self.lower() + ".")
            else:
                if trs_compat:
                    self.exp_io.tell("I see no " + item + " here that you can pick up.")
                elif not use_fixed_objects:
                    self.exp_io.tell("I see no " + item.lower() + " here that you can pick up.")
                else:
                    found_fixed_object = True
                    word_list = item.split(' ')
                    item_part = word_list[-1]
                    if not self.current_room.has_fixed_object(item_part):
                        if len(word_list) > 1:
                            item_part = word_list[0]
                            if not self.current_room.has_fixed_object(item_part):
                                found_fixed_object = False
                        else:
                            found_fixed_object = False

                    if found_fixed_object:
                        self.exp_io.tell("I see no way to pick up the " + item_part.lower() + ".")
                    else:
                        self.exp_io.tell("I see no " + item.lower() + " here.")
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
            if trs_compat:
                self.exp_io.tell("You are not carrying " + a_or_an(item_lower) + " " + item + ".")
            else:
                self.exp_io.tell("You have no " + item_lower + ".")
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
                if trs_compat:
                    self.exp_io.tell("- " + item + " -")
                else:
                    self.exp_io.tell("- " + item.lower())

            self.exp_io.tell("")


RESULT_NORMAL = 0
RESULT_DESCRIBE = 2
RESULT_WIN = 4
RESULT_DIE = 8
RESULT_END_GAME = 16
RESULT_NO_CHECK = 32
RESULT_SUSPEND = 64

SUSPEND_TO_MEMORY = 0
SUSPEND_INTERACTIVE = 1

LONG_DIRECTION_COMMANDS = ["NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN"]
DIRECTIONS = []
for dir in LONG_DIRECTION_COMMANDS:
    DIRECTIONS.append(dir[0])
DIRECTION_COMMANDS = DIRECTIONS + LONG_DIRECTION_COMMANDS

KEY = b'We were inspired by Steely Dan.'

class World:
    def __init__(self, exp_io):
        self.exp_io = exp_io

        self.version = 0
        self.title = "This adventure has no title!"
        self.player = Player(exp_io, self)
        self.rooms = {}
        self.room_list = []
        self.commands = []
        self.variables = {}
        self.item_descs = {}

        self.plural_items = []
        self.mass_items = []

        self.same_items = {}
        self.old_items = {}
        self.old_versions = {}

        self.action_newline_inserted = False

        self.suspend_version = 2
        self.suspend_mode = SUSPEND_INTERACTIVE
        self.last_suspend = None

    def load(self, filename):
        global use_fixed_objects

        start_room = None
        first_room = None
        new_room = None
        new_command = None
        cur_room_name = None

        file_stream = open(filename, 'r')

        for line in file_stream:
            line = line.strip()

            if not trs_compat:
                # Remove double spaces after punctuation
                line = line.replace('!  ', '! ');
                line = line.replace('?  ', '? ');
                line = line.replace('.  ', '. ');
                line = line.replace(':  ', ': ');

            if line.find("=") != -1:
                keyword, params = line.split("=", 1)
            else:
                keyword = line
                params = None

            if keyword == "VERSION":
                self.version = int(params)

            elif keyword == "TITLE":
                self.title = params[:]

            elif keyword == "START_ROOM":
                start_room = params[:]

            elif keyword == "INVENTORY_LIMIT":
                self.player.item_limit = int(params)

            elif keyword == "ROOM":
                new_room = Room(self)
                new_room.name = params[:]
                self.rooms[new_room.name] = new_room
                self.room_list.append(new_room.name)
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

                if (params[0] == "+" or
                    params[0] == "-" or
                    params[0] == "$"):
                    new_command.condition = params[:]
                else:
                    pos = params.find(":")
                    if pos != -1:
                        new_command.condition = params[pos + 1:]

                        if not (new_command.condition[0] == "+" or
                                new_command.condition[0] == "-" or
                                new_command.condition[0] == "$"):
                            new_command.condition = "+" + new_command.condition

                        new_command.commands = params[:pos].split(",")
                    else:
                        new_command.commands = params.split(",")

                if (new_command.condition != None and new_command.condition.endswith("+")):
                    new_command.condition = new_command.condition[:-1]
                    new_command.cont = True

                if cur_room_name != None:
                    new_command.location = cur_room_name[:]

            elif keyword == "ACTION":
                # If there is no current command, or if there is one,
                # but it already has an action, make a new command.
                if new_command == None or new_command.action != None:
                    new_command = Command()
                    self.commands.append(new_command)

                    if cur_room_name != None:
                        new_command.location = cur_room_name[:]

                new_command.action = params[:]

            elif keyword == "DESC":
                if new_room != None:
                    new_room.desc = params[:]

            elif keyword == "ALT_DESC":
                if new_room != None:
                   new_room.desc_alt = params[:]

            elif keyword == "DESC_CONTROL":
                if new_room != None:
                   new_room.desc_ctrl = params[:]

            elif keyword == "FIXED_OBJECTS":
                if new_room != None:
                   new_room.fixed_objects = params.split(",")
                   use_fixed_objects = True

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

            elif line.startswith("ITEM DESC "):
                item_name, item_desc = line[10:].split(":")
                self.item_descs[item_name] = item_desc

            elif line.startswith("PLURAL ITEM "):
                self.plural_items.append(line[12:])

            elif line.startswith("MASS ITEM "):
                self.mass_items.append(line[10:])

            elif line.startswith("SAME ITEM "):
                equal_item, existing_item = line[10:].split("=")
                self.same_items[equal_item] = existing_item

            elif line.startswith("OLD ITEM "):
                old_item, new_item = line[9:].split("=")
                self.old_items[old_item] = new_item

            elif line.startswith("OLD VERSION "):
                old_version, old_version_changes = line[12:].split(" ", 1)
                self.old_versions[int(old_version)] = old_version_changes

        # Sort commands so globals are last
        tmp_commands = self.commands;
        self.commands = []

        for c in tmp_commands:
            if c.location != None:
                self.commands.append(c);

        for c in tmp_commands:
            if c.location == None:
                self.commands.append(c);

        # Set up the starting room
        if start_room in self.rooms:
            self.player.current_room = self.rooms[start_room]
        elif first_room != None:
            self.player.current_room = first_room
        else:
            self.player.current_room = Room(self)
            self.player.current_room.name = "limbo"
            self.player.current_room.desc = "This adventure has no rooms. You are in limbo!"

        file_stream.close()

    def take_action(self, command, auto=False, previous_result=RESULT_NORMAL):
        result = RESULT_NORMAL
        error = False

        if command.action == None or command.action[0] == "^":
            if not auto:
                self.exp_io.tell("Nothing happens.")
        else:
            messages = []

            or_pos = command.action.find("|")
            if or_pos != -1:
                action_str = command.action[:or_pos]
            else:
                action_str = command.action

            if command.action[0] == ".":
                action_one_shot = True
                action_list = action_str[1:].split(";")
            else:
                action_one_shot = False
                action_list = action_str.split(";")

            for action in action_list:
                if action.find(":") != -1:
                    action, message = action.split(":", 1)
                else:
                    message = None

                if len(action) == 0:
                    action = None

                if action != None:
                    if action[0] == "/":
                        if action[1:] in self.rooms:
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
                                if command.action[0] != "^":
                                    command.action = "^" + command.action
                        else:
                            self.player.current_room.add_item(action[1:], True)
                            if command.action[0] != "^":
                                command.action = "^" + command.action

                    elif action[0] == "-":
                        if not self.player.remove_item(action[1:]):
                            if not self.player.current_room.remove_item(action[1:]):
                                self.exp_io.tell("You can't do that yet.")
                                error = True

                    elif action[0] == "#":
                        if action.find(">") != -1:
                            room_name, item = action[1:].split(">", 1)

                            if self.player.remove_item(item) or self.player.current_room.remove_item(item):
                                if room_name in self.rooms:
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

                        if command.action[0] != "^":
                            command.action = "^" + command.action

                    elif action[0] == "*":
                        if self.player.current_room.desc_ctrl != None:
                            if action[1] == "+":
                                if not (len(self.player.current_room.desc_ctrl) > 0 and self.player.current_room.desc_ctrl[-1] == "+"):
                                    self.player.current_room.desc_ctrl += "+"
                            else:
                                if len(self.player.current_room.desc_ctrl) > 0 and self.player.current_room.desc_ctrl[-1] == "+":
                                    self.player.current_room.desc_ctrl = self.player.current_room.desc_ctrl[:-1]

                    elif action[0] == "$":
                        equals_pos = action.find("=")
                        if equals_pos != -1:
                            variable_name = action[1:equals_pos]
                            value = action[equals_pos + 1:]

                            self.variables[variable_name] = value
                    else:
                        self.exp_io.tell("")
                        self.exp_io.tell(action)
                        result |= RESULT_DIE
                        result |= RESULT_END_GAME

                if error:
                    break

                if message != None:
                    messages.append(message)

            if action_one_shot:
                if command.action[0] != "^":
                    command.action = "^" + command.action

            if len(messages) > 0:
                if ((not self.action_newline_inserted or
                     trs_compat) and
                    ((result & RESULT_DESCRIBE) != 0 or
                     (not trs_compat and auto and
                      (previous_result & RESULT_DESCRIBE) != 0))):
                    self.exp_io.tell("")
                    self.action_newline_inserted = True
                for message in messages:
                    self.exp_io.tell(message)

        #if error or (auto and result == RESULT_NORMAL):
        #    return RESULT_NOTHING
        #else:
        #    return result
        return result

    def eval_condition(self, c):
        if c.condition == None:
            return True

        for condition in c.condition.split("&"):
            if condition.startswith("$"):
                op_pos = condition.find("==")
                if op_pos != -1:
                    variable_name = condition[1:op_pos]
                    value = condition[op_pos + 2:]

                    if variable_name in self.variables:
                        if self.variables[variable_name] != value:
                            return False
                    else:
                        return False
                else:
                    op_pos = condition.find("!=")
                    if op_pos != -1:
                        variable_name = condition[1:op_pos]
                        value = condition[op_pos + 2:]

                        if variable_name in self.variables:
                            if self.variables[variable_name] == value:
                                return False
                    else:
                        return False
            else:
                invert = False
                if condition.startswith("-"):
                    invert = True
                elif not condition.startswith("+"):
                    return False

                condition = condition[1:]

                if condition.startswith("*"):
                    condition = condition[1:]
                    has_item = (self.player.has_item(condition) or
                                self.player.current_room.has_item(condition))
                elif condition.startswith("@"):
                    condition = condition[1:]
                    has_item = self.player.current_room.has_item(condition)
                else:
                    has_item = self.player.has_item(condition)

                if invert and has_item:
                    return False
                elif not invert and not has_item:
                    return False

        return True

    def check_for_auto(self, previous_result):
        result = RESULT_NORMAL

        for c in self.commands:
            if ((c.location == None or
                 c.location == self.player.current_room.name) and
                c.commands == None):
                if self.eval_condition(c):
                    result |= self.take_action(c, True, previous_result)
                    if (result & RESULT_END_GAME) != 0:
                        break

        return result

    def process_command(self, wish, acknowledge):
        result = RESULT_NORMAL
        player_meets_condition = False
        player_in_correct_room = False
        command = None
        argument = None
        verbatim_argument = ""

        # Save the argument before processing in case someone needs it.
        pos = wish.find(" ")
        if pos != -1:
            verbatim_argument = wish[pos + 1:]

        wish = re.sub('[^A-Z ]', '', wish.upper())

        custom = None
        for c in self.commands:
            if c.commands != None:
                if wish in c.commands:
                    if (c.location == None or
                        self.player.current_room.name == c.location):
                        player_in_correct_room = True
                        custom = c

                        if self.eval_condition(c):
                            player_meets_condition = True
                            break
                        elif not c.cont:
                            break
                    elif not player_in_correct_room:
                        custom = c

        try_builtin = True
        action_denied_directive = None

        # Note: this assumes process_command() is called before check_for_auto()
        self.action_newline_inserted = False;

        if trs_compat:
            if custom != None and player_in_correct_room:
                try_builtin = False
                if player_meets_condition:
                    result = self.take_action(custom)
                else:
                    self.exp_io.tell("You can't do that yet.")
        else:
            if custom != None and player_in_correct_room:
                if player_meets_condition:
                    try_builtin = False
                    result = self.take_action(custom)
                else:
                    if custom.action != None:
                        or_pos = custom.action.find("|")
                        if or_pos != -1:
                            action_denied_directive = custom.action[or_pos + 1:]
                            if action_denied_directive.startswith("|"):
                                try_builtin = False
                                if action_denied_directive.startswith("|:"):
                                    self.exp_io.tell(action_denied_directive[2:])
                                else:
                                    self.exp_io.tell(action_denied_directive[1:])

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
                     self.exp_io.tell("I'm not sure how to get there. Try a direction.")

            elif command == "LOOK":
                if argument != None:
                    if not self.action_newline_inserted:
                        self.exp_io.tell("")
                        self.action_newline_inserted = True
                    self.exp_io.tell("There's really nothing more to see.")

                result |= RESULT_DESCRIBE

            elif command in DIRECTION_COMMANDS and argument == None:
                wants_to_walk = True
                goto_room = self.player.current_room.neighbor(command[0])

            elif command == "HELP":
                self.exp_io.tell("")
                if trs_compat:
                    self.exp_io.tell("These are some of the commands you may use:");
                    self.exp_io.tell("");
                    self.exp_io.tell("NORTH or N      (go north)");
                    self.exp_io.tell("SOUTH or S      (go south)");
                    self.exp_io.tell("EAST or E       (go east)");
                    self.exp_io.tell("WEST or W       (go west)");
                    self.exp_io.tell("UP or U         (go up)");
                    self.exp_io.tell("DOWN or D       (go down)");
                    self.exp_io.tell("INVENT          (see your inventory - what you are carrying)");
                    self.exp_io.tell("LOOK            (see where you are)");
                    self.exp_io.tell("SUSPEND         (save game to finish later)");
                    self.exp_io.tell("RESUME          (take up where you left off last time)");
                    self.exp_io.tell("QUIT or STOP    (quit game)");
                else:
                    self.exp_io.tell("Welcome! The object of this game is simple: You just need to escape alive! You will use short commands (usually just one or two words) to do various things like move around, manipulate objects, and interact with your environment. To move, simply type a direction (using the first letter is fine: \"n\" for north, \"d\" for down, etc.). To be reminded of where you are, type \"look\". When you find objects, you can pick them up (\"get bottle\"), drop them (\"drop gold\"), or do other things (\"eat food\", \"wave wand\", etc.). To see what you are carrying, type \"inventory\" (\"invent\" for short). To save your game for later (or in case you think you are about about to do something perilous), type \"suspend\". To resume later, type \"resume\". To end the game, type \"quit\". The key is to use your imagination and just try things (like \"fly\", \"open door\", \"push button\", etc.). If what you are attempting to do does not work, try saying it another way. Have fun, and good luck!")
                self.exp_io.tell("")

            elif ((command == "QUIT" or command == "STOP") and
                  argument == None):
                if acknowledge:
                    self.exp_io.tell("Ok")

                result |= RESULT_END_GAME

            elif command == "GET" or command == "TAKE":
                if argument != None:
                    self.player.get_item(argument, acknowledge)
                else:
                    self.exp_io.tell(command[0] +
                                     command[1:].lower() +
                                     " what?")

            elif command == "DROP" or command == "THROW":
                if argument != None:
                    self.player.drop_item(argument, acknowledge)
                else:
                    self.exp_io.tell(command[0] +
                                     command[1:].lower() +
                                     " what?")

            elif ((command == "INVENT" or command == "INVENTORY") and
                  argument == None):
                self.player.list_items()

            elif ((command == "SUSPEND" or command == "SAVE") and
                  argument == None):
                #self.exp_io.tell("Sorry, suspend has not yet been implemented.")
                if self.suspend_mode == SUSPEND_INTERACTIVE:
                    self.exp_io.tell("")
                    self.exp_io.tell("OK, grab the following long line and save it somewhere. This will be the command you use later to resume your game:")
                    self.exp_io.tell("")
                    self.exp_io.tell_raw("resume " + self.get_state())
                    self.exp_io.tell("")
                else:
                    self.last_suspend = self.get_state()
                    if acknowledge:
                        self.exp_io.tell("Ok")
                    result |= RESULT_SUSPEND

            elif ((command == "RESUME" or command == "RESTORE") and
                  (self.suspend_mode != SUSPEND_INTERACTIVE and
                   argument == None)):
                if self.last_suspend != None:
                    if not self.set_state(self.last_suspend):
                        self.exp_io.tell("Hmm, the suspended game information doesn't look valid. Sorry.")
                    else:
                        result |= (RESULT_DESCRIBE | RESULT_NO_CHECK)
                else:
                    self.exp_io.tell("Hmm, there seems to be no suspended game information. Sorry.")

            elif ((command == "RESUME" or command == "RESTORE") and
                  self.suspend_mode == SUSPEND_INTERACTIVE):
                #self.exp_io.tell("Sorry, resume has not yet been implemented.")
                if argument == None:
                    self.exp_io.tell("Please follow this command with the code you were given when you suspended your game.")
                else:
                    if not self.set_state(verbatim_argument):
                        self.exp_io.tell("Hmm, that resume code just doesn't seem to make sense! Sorry.")
                    else:
                        result |= (RESULT_DESCRIBE | RESULT_NO_CHECK)

            elif custom != None:
                if trs_compat:
                    self.exp_io.tell("You can't do that here.")
                else:
                    if not player_in_correct_room:
                        self.exp_io.tell("You can't do that here.")
                    else:
                        if action_denied_directive != None:
                            if action_denied_directive.startswith(":"):
                                self.exp_io.tell(action_denied_directive[1:])
                            else:
                                self.exp_io.tell(action_denied_directive)
                        else:
                            self.exp_io.tell("You can't do that yet.")
            else:
                self.exp_io.tell("I don't understand.")

            if wants_to_walk:
                if goto_room in self.rooms:
                    room = self.rooms[goto_room]
                    self.player.current_room = room
                    result |= RESULT_DESCRIBE
                else:
                    self.exp_io.tell("You can't go that way.")

        return result

    def encrypt(self, in_str):
        comp_bytes = zlib.compress(in_str.encode('ascii'))

        out = bytearray()
        key_len = len(KEY)
        for i, b in enumerate(comp_bytes):
            out.append(b ^ KEY[i % key_len])

        return base64.urlsafe_b64encode(bytes(out)).decode('ascii').strip('=')

    def decrypt(self, in_str):
        try:
            if '+' in in_str:
                in_str = in_str.replace('+', '-')

            if '/' in in_str:
                in_str = in_str.replace('/', '_')

            comp_bytes = base64.urlsafe_b64decode(in_str +
                                                  ((4 - len(in_str) % 4) & 0x3) * '=')
        except TypeError:
            return "Decrypt failed"

        out = bytearray()
        key_len = len(KEY)
        for i, b in enumerate(comp_bytes):
            out.append(b ^ KEY[i % key_len])

        try:
            return zlib.decompress(out).decode('ascii')
        except zlib.error:
            return "Decrypt failed"

    def old_decrypt(self, in_str):
        out = bytearray()
        in_bytes = in_str.encode('ascii')
        for i in range(len(in_bytes)):
            c = in_bytes[-(i + 1)]

            c -= 0x3b
            c &= 0x3f
            c ^= (KEY[i % len(KEY)]) & 0x3f
            c += 0x20

            out.append(c)

        return out.decode('ascii')

    def get_state(self):
        # our current room
        buf = [self.player.current_room.name]

        # what we're carrying
        buf.append(','.join(self.player.items))

        # and the variables that are set
        buf.append(','.join([variable + "=" + self.variables[variable] for variable in self.variables]))

        # and the state of the actions
        command_buf = []

        for command in self.commands:
            if command.action != None and command.action[0] == "^":
                command_buf.append("^")
            else:
                command_buf.append(".")

        buf.append(''.join(command_buf))

        # now the room details that have changed
        for room_name in self.room_list:
            room = self.rooms[room_name]

            if room.desc_ctrl != None and len(room.desc_ctrl) > 0 and room.desc_ctrl[-1] == "+":
                room_data_buf = ["+"]
            else:
                room_data_buf = ["."]

            for dir in DIRECTIONS:
                room_data_buf.append(room.neighbor_save_string(dir))

            # the items in the room
            room_data_buf.append(','.join(room.items))

            # Compress things a little
            room_data_string = ':'.join(room_data_buf)

            if room_data_string[0:8] == '.:::::::':
                room_data_string = room_data_string[8:]
            elif room_data_string[1:8] == ':::::::':
                room_data_string = room_data_string[0:2] + room_data_string[8:]

            buf.append(room_data_string)

        buf_string = ';'.join(buf)
        checksum = 0
        for i in range(len(buf_string)):
            checksum += ord(buf_string[i])

        buf_string = ("%04x" % (checksum & 0xffff)) + buf_string
        #print "Raw string: " + str(self.suspend_version) + ":" + str(self.version) + ":" + buf_string
        return str(self.suspend_version) + ":" + str(self.version) + ":" + self.encrypt(buf_string)

    def set_state(self, s):
        if not s:
            return False

        colon_pos = s.find(':')
        if colon_pos == -1 or s[0] < '0' or s[0] > '9':
            return False

        try:
            state_parts = s.split(':', 2)
            saved_suspend_version = int(state_parts[0])
            saved_adventure_version = int(state_parts[1])
            state_str = state_parts[2]
        except ValueError:
            return False

        if saved_suspend_version < 2:
            state_str = self.old_decrypt(state_str)
        else:
            state_str = self.decrypt(state_str)

        # Cannot handle suspend versions lower than 1
        if saved_suspend_version < 1:
            return False

        # Cannot work with saved adventure versions higher than
        # version of adventure loaded.
        if saved_adventure_version > self.version:
            return False

        if saved_suspend_version < 2:
            if len(state_str) < 2:
                return False
        else:
            if len(state_str) < 4:
                return False

        num_commands_total_delta = 0
        num_commands_deltas = {}
        if saved_adventure_version in self.old_versions:
            version_changes = self.old_versions[saved_adventure_version].split(',')
            for version_change in version_changes:
                if version_change.startswith('NUM_COMMANDS'):
                    num_commands_arg = version_change[12:]
                    at_pos = num_commands_arg.find("@")
                    if at_pos != -1:
                        delta = int(num_commands_arg[:at_pos])
                        position = int(num_commands_arg[at_pos + 1:])
                        num_commands_deltas[position] = delta
                    else:
                        delta = int(version_change[12:])

                    num_commands_total_delta += delta

                elif version_change == 'INCOMPATIBLE':
                    return False

        checksum = 0
        if saved_suspend_version < 2:
            for i in range(2, len(state_str)):
                checksum += ord(state_str[i])

            # Fix a problem in which the '`' character gets converted to ' '
            # in the encryption/decryption process.
            checksum_str = (chr(((checksum >> 6) & 0x3f) + 0x21) + chr((checksum & 0x3f) + 0x21)).replace('`', ' ')

            if checksum_str != state_str[:2]:
                return False

            parts = state_str[2:].split(';')

            if len(self.rooms) != len(parts) - 3:
                return False

            if len(self.commands) != len(parts[2]) + num_commands_total_delta:
                return False
        else:
            for i in range(4, len(state_str)):
                checksum += ord(state_str[i])

            checksum_str = ("%04x" % (checksum & 0xffff))

            if checksum_str != state_str[:4]:
                return False

            parts = state_str[4:].split(';')

            if len(self.rooms) != len(parts) - 4:
                return False

            if len(self.commands) != len(parts[3]) + num_commands_total_delta:
                return False

        part_num = 0

        # Recover the current room
        prev_room = self.player.current_room
        try:
            self.player.current_room = self.rooms[parts[part_num]]
        except KeyError:
            # If the room name is invalid, recover the previous location and
            # return error status.
            self.player.current_room = prev_room
            return False

        part_num += 1

        # Recover the player's items
        if parts[part_num] == "":
            self.player.items = []
        else:
            self.player.items = parts[part_num].split(',')
            for i, player_item in enumerate(self.player.items):
                if player_item in self.old_items:
                    self.player.items[i] = self.old_items[player_item]

        part_num += 1

        # Recover the variables
        self.variables = {};

        if saved_suspend_version >= 2:
            if parts[part_num] != "":
                saved_variables = parts[part_num].split(",");
                for variable in saved_variables:
                    equals_pos = variable.find("=");
                    if equals_pos != -1:
                        self.variables[variable[:equals_pos]] = variable[equals_pos + 1:]

            part_num += 1;

        # Recover the state of the actions
        num_commands = len(self.commands)
        num_saved_commands = len(parts[part_num])
        if saved_suspend_version < 2:
            command_idx = num_commands - num_commands_total_delta - 1
        else:
            command_idx = 0

        i = 0
        while i < num_commands:
            if i in num_commands_deltas:
                delta = num_commands_deltas[i]
                if delta > 0:
                    i += delta - 1
                    i += 1
                    continue
                else:
                    if saved_suspend_version < 2:
                        command_idx += delta
                    else:
                        command_idx -= delta

            if command_idx >= 0 and command_idx < num_saved_commands:
                command = self.commands[i]

                if command.action != None:
                    if parts[part_num][command_idx] == '^' and command.action[0] != '^':
                        command.action = "^" + command.action
                    elif parts[part_num][command_idx] != '^' and command.action[0] == '^':
                        command.action = command.action[1:]

            if saved_suspend_version < 2:
                command_idx -= 1
            else:
                command_idx += 1

            i += 1

        part_num += 1

        # Recover the room details
        room_idx = 0
        if saved_suspend_version < 2:
            ordered_room_list = reversed(self.room_list)
        else:
            ordered_room_list = self.room_list

        for room_name in ordered_room_list:
            room = self.rooms[room_name]

            room_code = parts[room_idx + part_num].split(':')
            if len(room_code) != 8:
                old = room_code
                room_code = []
                if len(old) == 1:
                    room_code.append(".")
                    room_code.append("")
                    room_code.append("")
                    room_code.append("")
                    room_code.append("")
                    room_code.append("")
                    room_code.append("")
                    room_code.append(old[0])
                elif len(old) == 2:
                    room_code.append(old[0])
                    room_code.append("")
                    room_code.append("")
                    room_code.append("")
                    room_code.append("")
                    room_code.append("")
                    room_code.append("")
                    room_code.append(old[1])
                else:
                   # How would we recover from this?
                   exp_io.tell("Warning! Error in decoding suspended game.")
                   exp_io.tell("         The state of your game is inconsistent.")
                   exp_io.tell("         Please start game over and report this")
                   exp_io.tell("         problem to the developer.")
                   return False

            # first the description control
            if room.desc_ctrl != None:
                if room_code[0] == '+' and (len(room.desc_ctrl) == 0 or room.desc_ctrl[-1] != "+"):
                    room.desc_ctrl += "+"
                elif room_code[0] != "+" and (len(room.desc_ctrl) > 0 and room.desc_ctrl[-1] == '+'):
                    room.desc_ctrl = room.desc_ctrl[:-1]

            # now the possible directions
            for i in range(1, 7):
                if saved_suspend_version <= 2:
                    # Remove "^orig_room" from the end of the string if it
                    # appears after "curr_room". This is for compatibility
                    # with old save formats that either tack this on or not.
                    #
                    # Note that the java version used to depend on "^orig_room".
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
                for i, room_item in enumerate(room.items):
                    if room_item in self.old_items:
                        room.items[i] = self.old_items[room_item]

            room_idx += 1

        return True


def play(filename=None, input_script=None, no_delay=False):
    exp_io = ExpIO()
    world = World(exp_io)

    exp_io.no_delay = no_delay

    if input_script != None:
        input_script_fp = open(input_script, 'r')
        input_script_commands = []
        for line in input_script_fp:
            input_script_commands.append(line.strip())
        input_script_fp.close()
        input_script_iter = iter(input_script_commands)

    exp_io.tell("")
    exp_io.tell("")
    exp_io.tell("*** EXPLORE ***  ver 4.10")

    if filename == None:
        exp_io.tell("")

        while True:
            advname = input("Name of adventure: ")
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
            print('Adventure not found', file=sys.stderr)
            sys.exit(1)

    exp_io.tell("")
    exp_io.tell("")
    exp_io.tell(world.title)
    exp_io.tell("")

    game_started = False

    while True:
        if game_started:
            if input_script != None:
                try:
                    wish = next(input_script_iter)
                    exp_io.tell_raw(":" + wish)
                except StopIteration:
                    wish = input(":")
            else:
                wish = input(":")
            wish = ' '.join(wish.split())
            if wish != "":
                result = world.process_command(wish, True)
            else:
                result = RESULT_NORMAL
        else:
            game_started = True
            result = RESULT_DESCRIBE

        if (result & RESULT_END_GAME) == 0:
            if (result & RESULT_NO_CHECK) == 0:
                result |= world.check_for_auto(result)

        if (result & RESULT_END_GAME) == 0:
            if (result & RESULT_DESCRIBE) != 0:
                exp_io.tell("")
                exp_io.tell(world.player.current_room.description())
        else:
            if (result & RESULT_WIN) != 0:
                exp_io.tell("")
                exp_io.tell("Nice job! You successfully completed this adventure!")
            elif (result & RESULT_DIE) != 0:
                exp_io.tell("")
                exp_io.tell("Game over.")

            exp_io.tell("")
            break


def play_once(filename, command=None, state=None, last_suspend=None, return_output=True, quiet=False, show_title=True, show_title_only=False):
    exp_io = ExpIO()
    world = World(exp_io)

    exp_io.no_delay = True
    world.suspend_mode = SUSPEND_TO_MEMORY
    world.last_suspend = last_suspend
    if return_output:
        exp_io.store_output = True

    if show_title_only and not show_title:
        print("%ERROR=Incompatible flags")
        return exp_io.get_output()

    if command != None:
        show_title = False
        quiet = True

    if not quiet:
        exp_io.tell("")
        exp_io.tell("")
        exp_io.tell("*** EXPLORE ***  ver 4.10")

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

    if state != None:
        if not world.set_state(state):
            print("%ERROR=Bad state code")
            return exp_io.get_output()

    if command != None:
        wish = ' '.join(command.split())
        if wish != "":
            result = world.process_command(wish, True)
        else:
            result = RESULT_NORMAL
    else:
        result = RESULT_DESCRIBE

    if (result & RESULT_END_GAME) == 0:
        if (result & RESULT_NO_CHECK) == 0:
            result |= world.check_for_auto(result)

    if (result & RESULT_END_GAME) == 0:
        if (result & RESULT_DESCRIBE) != 0:
            exp_io.tell("")
            exp_io.tell(world.player.current_room.description())

        print("%PROMPT=:")
        print("%STATE=" + world.get_state())

        if (result & RESULT_SUSPEND) != 0:
            print("%SUSPEND")
    else:
        print("%END")
        if (result & RESULT_WIN) != 0:
            print("%WIN")
        elif (result & RESULT_DIE) != 0:
            print("%DIE")

    return exp_io.get_output()


filename = None
one_shot = False
command = None
state = None
last_suspend = None
no_delay = False
input_script = None

skip_next = False
for arg_num in range(1, len(sys.argv)):
    if skip_next:
        skip_next = False
        continue
    if sys.argv[arg_num] == "-f":
        if len(sys.argv) > (arg_num + 1) and (len(sys.argv[arg_num + 1]) == 0 or sys.argv[arg_num + 1][0] != '-'):
            filename = sys.argv[arg_num + 1]
            skip_next = True
        else:
            print("Error: Missing adventure filename", file=sys.stderr)
            sys.exit(1)
    elif sys.argv[arg_num] == "-q":
        quiet = True
    elif sys.argv[arg_num] == "-c":
        if len(sys.argv) > (arg_num + 1) and (len(sys.argv[arg_num + 1]) == 0 or sys.argv[arg_num + 1][0] != '-'):
            command = sys.argv[arg_num + 1]
            skip_next = True
    elif sys.argv[arg_num] == "-r":
        if len(sys.argv) > (arg_num + 1) and (len(sys.argv[arg_num + 1]) == 0 or sys.argv[arg_num + 1][0] != '-'):
            state = sys.argv[arg_num + 1]
            skip_next = True
    elif sys.argv[arg_num] == "-s":
        if len(sys.argv) > (arg_num + 1) and (len(sys.argv[arg_num + 1]) == 0 or sys.argv[arg_num + 1][0] != '-'):
            last_suspend = sys.argv[arg_num + 1]
            skip_next = True
    elif sys.argv[arg_num] == "--one-shot":
        one_shot = True
    elif sys.argv[arg_num] == "--no-delay":
        no_delay = True
    elif sys.argv[arg_num] == "--trs-compat":
        trs_compat = True
    elif sys.argv[arg_num].startswith("--script="):
        input_script = sys.argv[arg_num][9:]
#    elif sys.argv[arg_num] == "--no-title":
#        show_title = False
#    elif sys.argv[arg_num] == "--title-only":
#        show_title_only = True
    else:
        filename = sys.argv[arg_num] + ".exp"

if one_shot or (command != None) or (state != None) or (last_suspend != None):
    play_once(filename, command, state, last_suspend, False)
else:
    play(filename, input_script, no_delay)

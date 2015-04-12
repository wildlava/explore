#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

import sys
import os
import string

import ExpIO
import World

def play(filename=None, no_delay=False, trs_compat=False):
    exp_io = ExpIO.ExpIO()
    world = World.World(exp_io)

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

    result = World.RESULT_DESCRIBE

    while True:
        if (result & World.RESULT_NO_CHECK) == 0:
            check_result = world.check_for_auto()
            if check_result != World.RESULT_NOTHING:
                result = check_result

                if (result & World.RESULT_END_GAME) != 0:
                    break
                else:
                    continue

        if (result & World.RESULT_DESCRIBE) != 0:
            exp_io.tell("")
            exp_io.tell(world.player.current_room.description())

        wish = raw_input(":")
        wish = string.join(string.split(wish))
        if wish != "":
            result = world.process_command(wish, True)
            if (result & World.RESULT_END_GAME) != 0:
                break
        else:
            result = World.RESULT_NORMAL

    exp_io.tell("")
    
def play_once(filename, command=None, resume=None, last_suspend=None, trs_compat=False, return_output=True, quiet=False, show_title=True, show_title_only=False):
    exp_io = ExpIO.ExpIO()
    world = World.World(exp_io)

    exp_io.no_delay = True
    world.trs_compat = trs_compat
    world.suspend_mode = World.SUSPEND_QUIET
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

    if command != None:
        result = World.RESULT_NORMAL
    else:
        result = World.RESULT_DESCRIBE

    if resume != None:
        if not world.set_state(resume):
            exp_io.tell("%ERROR=Bad resume code")
            return exp_io.get_output()

    if command != None:
        wish = string.join(string.split(command))
        if wish != "":
            result = world.process_command(wish, True)
        else:
            result = World.RESULT_NORMAL
                
    if (result & World.RESULT_NO_CHECK) == 0:
        check_result = world.check_for_auto()
        if check_result != World.RESULT_NOTHING:
            result = check_result

    if (result & World.RESULT_DESCRIBE) != 0:
        exp_io.tell("")
        exp_io.tell(world.player.current_room.description())
        
    if (result & World.RESULT_WIN) != 0:
        exp_io.tell("%WIN")
    elif (result & World.RESULT_DIE):
        exp_io.tell("%DIE")
    elif (result & World.RESULT_END_GAME):
        exp_io.tell("%END")
    else:
        exp_io.tell("%PROMPT=:")
        exp_io.tell("%STATE=" + world.get_state())
        
        if (result & World.RESULT_SUSPEND):
            exp_io.tell("%SUSPEND")
        
    return exp_io.get_output()

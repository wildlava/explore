#!/usr/bin/env python

#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

import sys

import Explore

filename = None
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
    elif sys.argv[arg_num] == "--no-title":
        show_title = False
    elif sys.argv[arg_num] == "--title-only":
        show_title_only = True
    elif sys.argv[arg_num] == "--no-delay":
        no_delay = True
    elif sys.argv[arg_num] == "--trs-compat":
        trs_compat = True

Explore.play(filename, no_delay, trs_compat)

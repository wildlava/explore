#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

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

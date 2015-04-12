#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

import sys
import time

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

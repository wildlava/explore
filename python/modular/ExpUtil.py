#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

def a_or_an(s):
    s_lower = s.lower()
        
    if s_lower[0] in ['a', 'e', 'i', 'o', 'u']:
        return "an"
    else:
        return "a"

#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

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

#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

require "base64"
require "zlib"


class ExpUtil
  def self.a_or_an(s)
    if "aeiou".include? s.downcase[0,1]
      "an"
    else
      "a"
    end
  end
end


class ExpIO
  @no_delay = false

  def self.tell(s)
    if s == ""
      lines = [""]
    else
      lines = s.split("\n", -1)
    end

    lines.each do |line|
      if not @no_delay
        sleep 0.03
      end

      puts line
    end
  end

  class << self
    attr_accessor :no_delay
  end
end


class Command
  def initialize()
    @location = nil
    @commands = []
    @condition = nil
    @actions = []
    # @actions_failable = []
    # @actions_not_failable = []
  end

  #def actions()
  #  @actions_failable + @actions_not_failable
  #end

  #def action_can_fail?(action)
  #  action_prefix1 = action[0,1]
  #  action_prefix2 = action[0,2]
  #  action_prefix1 == "%" or
  #    action_prefix2 == "+$" or
  #    action_prefix1 == "-" or
  #    action_prefix1 == "#"
  #end
  
  #def add_action(action)
  #  if $trs_compat or action_can_fail?(action)
  #    @actions_failable << action.dup
  #  else
  #    @actions_not_failable << action.dup
  #  end
  #end

  def actions()
    @actions
  end

  def add_action(action)
    @actions << action.dup
  end

  attr_accessor :location, :commands, :condition
end
  

class ItemContainer
  def initialize()
    @items = []
    @item_limit = nil
  end
  
  def is_full?()
    @item_limit and @items.length >= @item_limit
  end
  
  def has_no_items?()
    @items.length == 0
  end
  
  def has_item?(item)
    @items.include? item
  end
  
  def expand_item_name(item)
    if has_item?(item)
      return item
    end
    
    @items.each do |test_item|
      word_list = test_item.split
      if word_list.length > 1
        if word_list.first == item or word_list.last == item
          return test_item
        end
      end
    end
    
    item
  end

  def add_item(item, may_expand)
    if not is_full? or may_expand
      @items << item.dup
    end
  end
  
  def remove_item(item)
    if has_item?(item)
      @items.delete item
    end
  end

  attr_accessor :items, :item_limit
end


class Room < ItemContainer
  def initialize()
    super
    
    @name = nil
    @desc = nil
    @desc_alt = nil
    @desc_ctrl = nil
    @neighbors = Array.new(World::DIRECTIONS.length)
    @original_neighbors = Array.new(World::DIRECTIONS.length)
  end

  def neighbor(direction)
    i = World::DIRECTIONS.index direction
    if i
      @neighbors[i]
    end
  end

  def init_neighbor(direction, room)
    i = World::DIRECTIONS.index direction
    if i
      @neighbors[i] = room
      @original_neighbors[i] = room
    end
  end

  def set_neighbor(direction, room)
    i = World::DIRECTIONS.index direction
    if i
      @neighbors[i] = room

      #if not @original_neighbors[i]
      #  @original_neighbors[i] = room
      #end
    end
  end

  #def original_neighbor(direction)
  #  i = World::DIRECTIONS.index direction
  #  if i
  #    @original_neighbors[i]
  #  end
  #end
  
  def revert_neighbor(direction)
    i = World::DIRECTIONS.index direction
    if i
      @neighbors[i] = @original_neighbors[i]
    end
  end

  def block_way(direction)
    set_neighbor(direction, nil)
  end

  def make_way(direction, new_room)
    set_neighbor(direction, new_room)
  end

  def neighbor_save_string(direction)
    i = World::DIRECTIONS.index direction
    if i and @neighbors[i] != @original_neighbors[i]
      # @neighbors[i].to_s + "^" + @original_neighbors[i].to_s
      if @neighbors[i]
        @neighbors[i]
      else
        "^"
      end
    else
      ""
    end
  end
  
  def description()
    ctrl = "RC"
    out_desc = []
    
    if @desc_ctrl
      pos = @desc_ctrl.index ","
      if pos
        if @desc_ctrl[-1,1] == "+"
          ctrl = @desc_ctrl[(pos + 1)..-2]
        else
          ctrl = @desc_ctrl[0...pos]
        end
      else
        ctrl = @desc_ctrl
      end
    end

    if ctrl.include? "R" and @desc
      out_desc << @desc
    end
    
    if ctrl.include? "C" and @desc_alt
      out_desc << @desc_alt
    end
    
    @items.each do |item|
      out_desc << "There is #{ExpUtil.a_or_an(item)} #{item} here."
    end
    
    out_desc.join("\n")
  end

  attr_accessor :name, :desc, :desc_alt, :desc_ctrl
end


class Player < ItemContainer
  def initialize()
    super
    
    @current_room = nil
  end
  
  def get_item(item, acknowledge)
    full_item_room = @current_room.expand_item_name(item)
    full_item_self = expand_item_name(item)
    
    if not @current_room.has_item?(full_item_room)
      if has_item?(full_item_self)
        ExpIO.tell "You are already carrying the #{full_item_self}."
      else
        ExpIO.tell "I see no #{item} here that you can pick up."
      end
    elsif not add_item(full_item_room, false)
      ExpIO.tell"Your hands are full - you can't carry any more."
    else
      @current_room.remove_item(full_item_room)
      
      if acknowledge
        ExpIO.tell "Ok"
      end
    end
  end
  
  def drop_item(item, acknowledge)
    full_item = expand_item_name(item)
    
    if not remove_item(full_item)
      ExpIO.tell "You are not carrying #{ExpUtil.a_or_an(item)} #{item}."
    else
      @current_room.add_item(full_item, true)
      
      if acknowledge
        ExpIO.tell "Ok"
      end
    end
  end

  def list_items()
    if has_no_items?
      ExpIO.tell "You are not carrying anything."
    else
      ExpIO.tell ""
      ExpIO.tell "You are currently holding the following:"
      ExpIO.tell ""
      
      items.each do |item|
        ExpIO.tell "- #{item} -"
      end
      
      ExpIO.tell ""
    end
  end

  attr_accessor :current_room
end


class World
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
  DIRECTIONS = LONG_DIRECTION_COMMANDS.map { |d| d[0,1] }
  DIRECTION_COMMANDS = DIRECTIONS + LONG_DIRECTION_COMMANDS

  def initialize()
    @title = "This adventure has no title!"
    @player = Player.new
    @suspend_mode = SUSPEND_INTERACTIVE
    @last_suspend = nil

    @rooms = {}
    @commands = []
  end
  
  def load_adventure(filename)
    start_room = nil
    first_room = nil
    new_room = nil
    new_command = nil
    cur_room_name = nil
    
    File.open(filename, "r") do |file_stream|
      file_stream.each do |line|
        #puts line
        line.strip!
        if line != ""
          line.gsub!("\\", "\n")
          
          keyword, params = line.split("=", 2)
          if not params and keyword != "GLOBAL"
            abort "Error: no parameter given for #{keyword}"
          end
          
          if keyword == "TITLE"
            @title = params.dup
          elsif keyword == "START_ROOM"
            start_room = params.dup
          elsif keyword == "INVENTORY_LIMIT"
            @player.item_limit = params.to_i
          elsif keyword == "ROOM"
            new_room = Room.new
            new_room.name = params.dup
            @rooms[new_room.name] = new_room
            cur_room_name = new_room.name
            new_command = nil

            if not first_room
              first_room = new_room
            end
          elsif keyword == "LOCAL"
            new_room = nil
            cur_room_name = params.dup
            new_command = nil
          elsif keyword == "GLOBAL"
            new_room = nil
            cur_room_name = nil
            new_command = nil
          elsif keyword == "COMMAND"
            #if not new_command
            new_command = Command.new
            @commands << new_command
            #end
            
            if cur_room_name
              new_command.location = cur_room_name.dup
            end
            
            if params[0,1] == "+"
              new_command.condition = params[1..-1]
            elsif params[0,1] == "-"
              new_command.condition = params.dup
            else
              commands, new_command.condition = params.split(":")
              new_command.commands = commands.split(",")
              if new_command.condition and new_command.condition[0,1] == "+"
                new_command.condition[0,1] = ""
              end
            end
          elsif keyword == "ACTION"
            # If there is no current command, make one.
            if not new_command
              new_command = Command.new
              @commands << new_command

              if cur_room_name
                new_command.location = cur_room_name.dup
              end
            end
            
            if not new_command.actions.empty?
              ExpIO.tell "Build warning: extra action ignored!"
            elsif
              params.split(";").each do |action|
                new_command.add_action(action)
              end
            end
          elsif keyword == "DESC"
            if new_room
              new_room.desc = params.dup
            end
          elsif keyword == "ALT_DESC"
            if new_room
              new_room.desc_alt = params.dup
            end
          elsif keyword == "DESC_CONTROL"
            if new_room
              new_room.desc_ctrl = params.dup
            end
          elsif keyword == "CONTENTS"
            if new_room
              new_room.items = params.split(",")
            end 
          elsif keyword == "NORTH"
            if new_room
              new_room.init_neighbor("N", params.dup)
            end
          elsif keyword == "SOUTH"
            if new_room
              new_room.init_neighbor("S", params.dup)
            end
          elsif keyword == "EAST"
            if new_room
              new_room.init_neighbor("E", params.dup)
            end
          elsif keyword == "WEST"
            if new_room
              new_room.init_neighbor("W", params.dup)
            end
          elsif keyword == "UP"
            if new_room
              new_room.init_neighbor("U", params.dup)
            end
          elsif keyword == "DOWN"
            if new_room
              new_room.init_neighbor("D", params.dup)
            end
          end
        end
      end
    end
    
    if @rooms[start_room]
      @player.current_room = @rooms[start_room]
    elsif first_room
      @player.current_room = first_room
    else
      @player.current_room = Room.new
      @player.current_room.name = "limbo"
      @player.current_room.desc = "This adventure has no rooms.  You are in limbo!"
    end
  end
  
  def take_action(command, auto)
    result = RESULT_NORMAL
    error = false
        
    if command.actions.empty? or command.actions[0].empty? or command.actions[0][0,1] == "^"
      result = RESULT_NOTHING
      
      if not auto
        ExpIO.tell "Nothing happens."
      end
    else
      command.actions.each do |action|
        action, message = action.split(":", 2)
        
        if action.empty?
          action = nil
        end
        
        if action
          if action[0,1] == "/"
            room = @rooms[action[1..-1]]
            if room
              @player.current_room = room
              result |= RESULT_DESCRIBE
            end
          elsif action[0,1] == "!"
            ExpIO.tell ""
            ExpIO.tell action[1..-1]
            #ExpIO.tell ""
            #ExpIO.tell "It took you ? moves to win."
            result |= RESULT_WIN
            result |= RESULT_END_GAME
          elsif action[0,1] == "="
            result |= process_command(action[1..-1], false)
          elsif action[0,1] == "%"
            old_item, new_item = action[1..-1].split(",", 2)
            if new_item
              if @player.remove_item(old_item)
                @player.add_item(new_item, true)
              elsif @player.current_room.remove_item(old_item)
                @player.current_room.add_item(new_item, true)
              else
                ExpIO.tell "You can't do that yet."
                error = true
              end
            end
          elsif action[0,1] == "+"
            if action[1,1] == "$"
              if not @player.add_item(action[2..-1], false)
                ExpIO.tell "You are carrying too much to do that."
                error = true
              else
                if command.actions[0][0,1] != "^"
                  command.actions[0].insert(0, "^")
                end
              end
            else
              @player.current_room.add_item(action[1..-1], true)
              if command.actions[0][0,1] != "^"
                command.actions[0].insert(0, "^")
              end
            end
          elsif action[0,1] == "-"
            if not @player.remove_item(action[1..-1])
              if not @player.current_room.remove_item(action[1..-1])
                ExpIO.tell "You can't do that yet."
                error = true
              end
            end
          elsif action[0,1] == "#"
            room_name, item = action[1..-1].split(">", 2)
            if item
              if @player.remove_item(item) or @player.current_room.remove_item(item)
                room = @rooms[room_name]
                if room
                  room.add_item(item, true)
                else
                  ExpIO.tell "Wow, I think somthing just left our universe!"
                end
              else
                ExpIO.tell "You can't do that yet."
                error = true
              end
            end
          elsif action[0,1] == "["
            if action[1,1] == "$"
              @player.current_room.block_way(action[2,1])
            else
              @player.current_room.make_way(action[1,1], action[2..-1])
            end
            
            if command.actions[0][0,1] != "^"
              command.actions[0].insert(0, "^")
            end
          elsif action[0,1] == "*"
            if @player.current_room.desc_ctrl
              if action[1,1] == "+"
                if @player.current_room.desc_ctrl[-1,1] != "+"
                  @player.current_room.desc_ctrl << "+"
                end
              else
                if @player.current_room.desc_ctrl[-1,1] == "+"
                  @player.current_room.desc_ctrl.chop!
                end
              end
            end
          else
            ExpIO.tell ""
            ExpIO.tell action
            result |= RESULT_DIE
            result |= RESULT_END_GAME
          end
        end
        
        if error
          break
        end

        if message
          if (result & RESULT_DESCRIBE) != 0
            ExpIO.tell ""
          end

          ExpIO.tell message
        end
      end
    end
    
    if error or (auto and result == RESULT_NORMAL)
      RESULT_NOTHING
    else
      result
    end
  end
  
  def check_for_auto()
    result = RESULT_NOTHING
    
    @commands.each do |c|
      if c.commands.length == 0 and (not c.location or c.location == @player.current_room.name)
        if not c.condition or (c.condition[0,1] == "-" and not @player.has_item?(c.condition[1..-1])) or (c.condition != "-" and @player.has_item?(c.condition))
          result |= take_action(c, true)
        end
      end
    end

    result
  end
  
  def find_custom(cmd, r)
    global_candidate = nil
    candidate = nil
    
    @commands.each do |c|
      if c.commands.include? cmd
        # Give priority to commands that are specific to
        # this room (if specified), otherwise remember it
        # as a candidate.
        if not r or c.location == r.name
          return c
        elsif not c.location
          global_candidate = c
        else
          candidate = c
        end
      end
    end
    
    if global_candidate != nil
      global_candidate
    else
      candidate
    end
  end
  
  def process_command(wish, acknowledge)
    result = RESULT_NORMAL
    player_meets_condition = false
    player_in_correct_room = false
    
    # Save argument before converting case for things like "resume"
    pos = wish.index " "
    if pos
      verbatim_argument = wish[pos + 1..-1]
    end

    wish.upcase!

    custom = find_custom(wish, @player.current_room)
    if custom
      if not custom.condition or (custom.condition[0,1] == "-" and not @player.has_item?(custom.condition[1..-1])) or (custom.condition != "-" and @player.has_item?(custom.condition))
        player_meets_condition = true
        end
      
      if not custom.location or @player.current_room.name == custom.location
        player_in_correct_room = true
      end
    end

    try_builtin = true

    if $trs_compat
      if custom and player_in_correct_room
        try_builtin = false
        if player_meets_condition
          result = take_action(custom, false)
        else
          ExpIO.tell "You can't do that yet."
        end
      end
    elsif custom and player_in_correct_room and player_meets_condition
      try_builtin = false
      result = take_action(custom, false)
    end
    
    if try_builtin
      command, argument = wish.split(nil, 2)
      #puts "Wish: #{wish}"
      #puts "Command: #{command}  Argument: #{argument}"
      wants_to_walk = false
      goto_room = nil
      
      if command == "GO"
        if not argument
          ExpIO.tell "Go where?"
        elsif DIRECTION_COMMANDS.include? argument
          ExpIO.tell 'No need to say "go" for the simple directions.'
        else
          ExpIO.tell "I'm not sure how to get there.  Try a direction."
        end
      elsif command == "LOOK"
        if argument
          ExpIO.tell("There's really nothing more to see.")
        end
        
        result |= RESULT_DESCRIBE
      elsif DIRECTION_COMMANDS.include? command and not argument
        wants_to_walk = true
        goto_room = @player.current_room.neighbor command[0,1]
      elsif command == "HELP"
        ExpIO.tell "
These are some of the commands you may use

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
"
      elsif (command == "QUIT" or command == "STOP") and not argument
        if acknowledge
          ExpIO.tell "Ok"
        end
        
        result |= RESULT_END_GAME
      elsif command == "GET" or command == "TAKE"
        if argument
          @player.get_item(argument, acknowledge)
        else
          ExpIO.tell "Get what?"
        end
      elsif command == "DROP" or command == "THROW"
        if argument
          @player.drop_item(argument, acknowledge)
        else
          ExpIO.tell "Drop what?"
        end
      elsif (command == "INVENT" or command == "INVENTORY") and not argument
        @player.list_items
      elsif command == "SUSPEND" or command == "SAVE"
        #ExpIO.tell "Sorry, suspend has not yet been implemented."
        if @suspend_mode == SUSPEND_INTERACTIVE
          ExpIO.tell ""
          ExpIO.tell "OK, grab the following long line and save it away somewhere."
          ExpIO.tell "This will be the command you use to resume your game:"
          ExpIO.tell ""
          ExpIO.tell "resume #{get_state}"
          ExpIO.tell ""
        elsif @suspend_mode == SUSPEND_QUIET
          if acknowledge
            ExpIO.tell "Ok"
          end
          
          result |= RESULT_SUSPEND
        end
      elsif command == "RESUME" or command == "RESTORE"
        #ExpIO.tell "Sorry, resume has not yet been implemented."
        if @suspend_mode == SUSPEND_INTERACTIVE
          if not argument
            ExpIO.tell "Please follow this command with the code you were given"
            ExpIO.tell "when you suspended your game."
          else
            if not set_state verbatim_argument
              ExpIO.tell "Hmm, that resume code just doesn't seem to make sense!  Sorry."
            else
              result |= (RESULT_DESCRIBE | RESULT_NO_CHECK)
            end
          end
        elsif @suspend_mode == SUSPEND_QUIET
          if @last_suspend
            if not set_state @last_suspend
              ExpIO.tell "Hmm, the suspended game information doesn't look valid.  Sorry."
            else
              result |= (RESULT_DESCRIBE | RESULT_NO_CHECK)
            end
          else
            ExpIO.tell "Hmm, there seems to be no suspended game information.  Sorry."
          end
        end
      elsif custom
        if $trs_compat
          ExpIO.tell "You can't do that here."
        else
          if not player_in_correct_room
            ExpIO.tell "You can't do that here."
          else
            ExpIO.tell "You can't do that yet."
          end
        end
      else
        ExpIO.tell "I don't understand."
      end
      
      if wants_to_walk
        room = @rooms[goto_room]
        if room
          @player.current_room = room
          result |= RESULT_DESCRIBE
        else
          ExpIO.tell "You can't go that way."
        end
      end
    end
    
    result
  end
  
  def get_state()
    # our current room
    buf = [@player.current_room.name.dup]

    # what we're carrying
    buf << @player.items.join(",")

    # and the command numbers having actions that have been "done"
    command_buf = []
    @commands.each do |command|
      if not command.actions.empty? and command.actions[0][0,1] == "^"
        command_buf << "^"
      else
        command_buf << "."
      end
    end
    
    buf << command_buf.join

    # now the room details that have changed
    @rooms.sort.each do |room_name, room|
      if room.desc_ctrl and room.desc_ctrl[-1,1] == "+"
        room_data_buf = ["+"]
      else
        room_data_buf = ["."]
      end
      
      World::DIRECTIONS.each do |dir|
        room_data_buf << room.neighbor_save_string(dir)
      end
      
      #if len(room_data_string) > 7 and room_data_string[-8:] == ".:::::::"
      #    buf.append(room_data_string[:-8])
      #elif len(room_data_string) > 6 and room_data_string[-7:] == ":::::::"
      #    buf.append(room_data_string[:-6])
      #else
      
      # the items in the room
      room_data_buf << room.items.join(",")
      
      buf << room_data_buf.join(":")
    end
    
    buf_string = buf.join(";")
    
    checksum = 0
    buf_string.each_byte do |byte|
      checksum += byte
    end
    
    Base64.encode64(Zlib::Deflate.deflate("%c%c" % [((checksum >> 6) & 0x3f) + 0x21, (checksum & 0x3f) + 0x21] + buf_string)).delete("\n")
  end
  
  def set_state(s)
    begin
      state_str = Zlib::Inflate.inflate(Base64.decode64(s))
    rescue
      return false
    end

    if state_str.length < 2
      return false
    end
    
    checksum = 0
    state_str[2..-1].each_byte do |byte|
      checksum += byte
    end
    
    checksum_str = "%c%c" % [((checksum >> 6) & 0x3f) + 0x21, (checksum & 0x3f) + 0x21]
    
    if checksum_str != state_str[0,2]
      return false
    end
    
    parts = state_str[2..-1].split(";", -1)
    
    if @rooms.length != parts.length - 3
      return false
    end
    
    if @commands.length != parts[2].length
      return false
    end

    # Recover the current room,
    # but return error status if the room name is invalid.
    new_current_room = @rooms[parts[0]]
    if not new_current_room
      return false
    end
  
    # Recover the player's items
    if parts[1] == ""
      new_player_items = []
    else
      new_player_items = parts[1].split(",")
    end
    
    # If the player now has more than he can carry, which should never
    # happen, recover the previous location and return error status.
    if new_player_items.length > @player.item_limit
      return false
    end
    
    # We are past the error conditions, so set some new player status
    @player.current_room = new_current_room
    @player.items = new_player_items
        
    # Recover the state of the actions
    command_idx = 0
    @commands.each do |command|
      if not command.actions.empty?
        if parts[2][command_idx,1] == "^" and command.actions[0][0,1] != "^"
          command.actions[0].insert(0, "^")
        elsif parts[2][command_idx,1] != "^" and command.actions[0][0,1] == "^"
          command.actions[0][0,1] = ""
        end
      end

      command_idx += 1
    end
        
    # Recover the room details.
    room_idx = 0
    @rooms.sort.each do |room_name, room|
      room_codes = parts[room_idx + 3].split(":", -1)
      #if len(room_codes) != 8
      #    old = room_codes
      #    room_codes = new String[8]
      #    if (old.length == 1)
      #    {
      #       room_codes[0] = "."
      #       room_codes[1] = ""
      #       room_codes[2] = ""
      #       room_codes[3] = ""
      #       room_codes[4] = ""
      #       room_codes[5] = ""
      #       room_codes[6] = ""
      #       room_codes[7] = old[0]
      #    }
      #    else if (old.length == 2)
      #    {
      #       room_codes[0] = old[0]
      #       room_codes[1] = ""
      #       room_codes[2] = ""
      #       room_codes[3] = ""
      #       room_codes[4] = ""
      #       room_codes[5] = ""
      #       room_codes[6] = ""
      #       room_codes[7] = old[1]
      #    }
      #    else
      #    {
      #       # How would we recover from this?  For now, don't handle,
      #       # since we do not use this code.
      #       #return false
      #    }
      # }
      
      # first the description control
      if room.desc_ctrl
        if room_codes[0] == '+' and room.desc_ctrl[-1,1] != "+"
          room.desc_ctrl << "+"
        elsif room_codes[0] != "+" and room.desc_ctrl[-1,1] == '+'
          room.desc_ctrl.chop!
        end
      end

      # now the possible directions
      room_code_idx = 1
      World::DIRECTIONS.each do |dir|
        room_code = room_codes[room_code_idx].dup

        # Remove anything after the "^", since we just want the
        # "current room" (note that this is for backwards compatibility
        # with the old save format, which saved the new room in the
        # following format: "curr_room^orig_room".
        if room_code.length > 0 and room_code[0,1] != "^"
          room_code.sub!(/\^.*/, "")
        end      
        
        if room_code == ""
          room.revert_neighbor(dir)
        elsif room_code[0,1] == "^"
          room.block_way(dir)
        else
          room.make_way(dir, room_code)
        end
        
        room_code_idx += 1
      end
      
      # now the contents
      if room_codes[7] == ""
        room.items = []
      else
        room.items = room_codes[7].split(",")
      end
      
      room_idx += 1
    end
    
    true
  end

  attr_accessor :title, :player, :suspend_mode, :last_suspend
end


quiet = false
filename = nil
show_title = true
show_title_only = false
command = nil
one_shot = false
resume = nil
last_suspend = nil

$trs_compat = false

while ARGV.length > 0
  arg = ARGV.shift
  
  if arg == "-f"
    if ARGV[0] and ARGV[0][0,1] != "-"
      filename = ARGV.shift
    else
      abort "Error: Missing adventure filename"
    end
  elsif arg == "-q"
    quiet = true
  elsif arg == "-c"
    if ARGV[0] and ARGV[0][0,1] != "-"
      command = ARGV.shift
    end     
  elsif arg == "-r"
    if ARGV[0] and ARGV[0][0,1] != "-"
      resume = ARGV.shift
    end
  elsif arg == "-s"
    if ARGV[0] and ARGV[0][0,1] != "-"
      last_suspend = ARGV.shift
    end
  elsif arg == "--one-shot"
    one_shot = true
  elsif arg == "--no-title"
    show_title = false
  elsif arg == "--title-only"
    show_title_only = true
  elsif arg == "--no-delay"
    ExpIO.no_delay = true
  elsif arg == "--trs-compat"
    $trs_compat = true
  end
end

if show_title_only and not show_title
  abort "Error: Incompatible flags"
end

world = World.new

if command
  one_shot = true
  show_title = false
  quiet = true
  ExpIO.no_delay = true
  world.suspend_mode = World::SUSPEND_QUIET
  world.last_suspend = last_suspend
elsif one_shot
  ExpIO.no_delay = true
  world.suspend_mode = World::SUSPEND_QUIET
  world.last_suspend = last_suspend
end

if not quiet
  ExpIO.tell ""
  ExpIO.tell ""
  ExpIO.tell "*** EXPLORE ***  ver 4.8"
end

if not filename
  ExpIO.tell ""
  
  advname = ""
  while advname.empty?
    print "Name of adventure: "
    advname = gets
  end
  
  advname = advname.strip.downcase
  filename = advname + ".exp"
else
  advname = File.basename(filename, ".exp")
end

if not quiet
  ExpIO.tell ""
  ExpIO.tell "#{advname} is now being built..."
end

world.load_adventure(filename)

if show_title_only
  ExpIO.tell world.title
  exit
end

if show_title
  ExpIO.tell ""
  ExpIO.tell ""
  ExpIO.tell world.title
  ExpIO.tell ""
end

if command
  result = World::RESULT_NORMAL
else
  result = World::RESULT_DESCRIBE
end

if resume
  if not world.set_state resume
    abort "Error: Bad resume code"
  end    
  
  result |= World::RESULT_NO_CHECK
end

loop do
  if (result & World::RESULT_NO_CHECK) == 0
    check_result = world.check_for_auto
    if check_result != World::RESULT_NOTHING
      result = check_result
      
      if (result & World::RESULT_END_GAME) != 0
        break
      else
        next
      end
    end
  end
  
  if (result & World::RESULT_DESCRIBE) != 0
    ExpIO.tell ""
    ExpIO.tell world.player.current_room.description
  end
  
  if command
    wish = command
    command = nil
  elsif one_shot
    puts "%PROMPT=:"
    break
  else
    print ":"
    wish = gets
  end
  
  wish = wish.split.join(" ")
  
  if not wish.empty?
    result = world.process_command(wish, true)
    if (result & World::RESULT_END_GAME) != 0
      break
    end
  else
    result = World::RESULT_NORMAL
  end
end

if one_shot
  if (result & World::RESULT_WIN) != 0
    puts "%WIN"
  elsif (result & World::RESULT_DIE) != 0
    puts "%DIE"
  elsif (result & World::RESULT_END_GAME) != 0
    puts "%END"
  elsif (result & World::RESULT_SUSPEND) != 0
    puts "%STATE=#{world.get_state}"
    puts "%SUSPEND"
  else
    puts "%STATE=#{world.get_state}"
  end
else
  ExpIO.tell ""
end

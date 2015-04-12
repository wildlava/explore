#
# Explore
#    - The Adventure Interpreter
#
# Copyright (C) 2006  Joe Peterson
#

class ExpUtil
  def self.a_or_an(s)
    if "aeiou".include? s.downcase[0]
      "an"
    else
      "a"
    end
  end
end


class ExpIO
  @no_delay = false

  def self.tell(s)
    lines = s.split("\n")
    
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
  @location = nil
  @commands = []
  @condition = nil
  @action = nil
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
    if has_item? item
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
      @items << item
    end
  end
  
  def remove_item(item)
    if has_item? item
      @items.delete! item
    end
  end

  attr_accessor :items, :item_limit
end


class Room < ItemContainer
  NEIGHBOR_DIRS = "nsewud"

  def initialize()
    super
    
    @name = nil
    @desc = nil
    @desc_alt = nil
    @desc_ctrl = nil
    @neighbors = Array.new(NEIGHBOR_DIRS.length)
  end

  def neighbor(direction)
    i = NEIGHBOR_DIRS.index direction.downcase
    if i
      @neighbors[i]
    end
  end

  def set_neighbor(direction, room)
    i = NEIGHBOR_DIRS.index direction.downcase
    if i
      @neighbors[i] = room
    end
  end

  def current_neighbor(direction)
    dir_room = neighbor direction
    pos = dir_room.index "^"
    if pos
      cur_neighbor = dir_room[0...pos]
      if cur_neighbor == ""
        nil
      else
        cur_neighbor
      end
    else
      dir_room
    end
  end

  def original_neighbor(direction)
    dir_room = neighbor direction
    if dir_room
      suffix = dir_room[/\^.*$/]
      if suffix
        orig_way = suffix[1..-1]
        if orig_way == ""
          nil
        else
          orig_way
        end
      else
        dir_room
      end
    end
  end

  def block_way_string(direction)
    dir_room = neighbor direction
    if dir_room
      suffix = dir_room[/\^.*$/]
      if suffix
        suffix
      else
        "^" + dir_room
      end
    else
      "^"
    end
  end

  def block_way(direction)
    set_neighbor(direction, block_way_string(direction))
  end

  def make_way(direction, new_room)
    set_neighbor(direction, new_room + block_way_string(direction))
  end

  def neighbor_save_string(direction)
    dir_room = neighbor direction
    if dir_room and dir_room.index "^"
      dir_room
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
        if @desc_ctrl[-1] == "+"
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
      out_desc << "There is " + ExpUtil.a_or_an(item) + " " + item + " here."
    end
    
    out_desc.join("\n")
  end

  attr_accessor :name, :desc, :desc_alt, :desc_crtl
end


class Player < ItemContainer
  def initialize()
    super
    
    @current_room = nil
  end
end


#ExpIO.no_delay = true
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("Hi there!")
ExpIO.tell("I see " + ExpUtil.a_or_an("Dummy") + " Dummy")


r = Room.new
r.items = ["fan", "lamp"]
r.set_neighbor("N", "room_to_north")

r.items.each { |item| puts "Item in room: #{item}" }
puts "Room to north of room: #{r.current_neighbor("N")}"

puts "Original room to north of room: #{r.original_neighbor("N")}"

r.make_way("N", "a_new_room")
puts "Room to north of room (make_way): #{r.current_neighbor("N")}"

puts "Original room to north of room: #{r.original_neighbor("N")}"

r.block_way("N")
puts "Room to north of room (block_way): #{r.current_neighbor("N")}"

puts "Original room to north of room: #{r.original_neighbor("N")}"

r.make_way("N", "another_new_room")
puts "Room to north of room (make_way): #{r.current_neighbor("N")}"

puts "Original room to north of room: #{r.original_neighbor("N")}"

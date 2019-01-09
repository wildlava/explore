import sys
import explore

exp_io = explore.ExpIO()
world = explore.World(exp_io)

world.load(sys.argv[1] + '.exp')

for room_key in world.rooms:
    room = world.rooms[room_key]

    if (room.desc.lower() + (room.desc_alt.lower() if room.desc_alt else '')).find('wall') != -1:
        if not ('WALL' in room.fixed_objects and 'WALLS' in room.fixed_objects):
            print('-- ' + room.desc[:76])
            print('  * "wall" in description, but WALL and WALLS not in fixed_objects')
            print('    (fixed_objects: ' + ','.join(room.fixed_objects) + ')')
    else:
        if not ('WALL' not in room.fixed_objects and 'WALLS' not in room.fixed_objects):
            print('-- ' + room.desc[:76])
            print('  * "wall" not in description, but WALL and/or WALLS in fixed_objects')
            print('    (fixed_objects: ' + ','.join(room.fixed_objects) + ')')

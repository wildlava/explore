/* Explore program module.                                               */
/* This module performs the specified built-in command.                  */

#include    <stdio.h>
#include    <string.h>
#include    "explore.h"


do_built_in(command, echo)
    char            command[];
int             echo;

{
    char           *item;
    int             i;
    int             has_item;
    FILE           *suspend_file;
    char            suspend_name[16];
    int             loop_room;
    char        saved_name[16];
    char        line[256];

    if (!strcmp("NORTH", command) || !strcmp("SOUTH", command) ||
        !strcmp("EAST", command) || !strcmp("WEST", command) ||
        !strcmp("UP", command) || !strcmp("DOWN", command))
        command[1] = '\0';

    if (!strcmp("N", command))
        if (*NORTH_ROOM(room) != '\0')
        {
            room= find_room(NORTH_ROOM(room));
            return (DESCRIBE);
        }
        else
        {
            print(MSG_BAD_DIR);
            return (NOTHING);
        }

    if (!strcmp("S", command))
        if (*SOUTH_ROOM(room) != '\0')
        {
            room= find_room(SOUTH_ROOM(room));
            return (DESCRIBE);
        }
        else
        {
            print(MSG_BAD_DIR);
            return (NOTHING);
        }

    if (!strcmp("E", command))
        if (*EAST_ROOM(room) != '\0')
        {
            room= find_room(EAST_ROOM(room));
            return (DESCRIBE);
        }
        else
        {
            print(MSG_BAD_DIR);
            return (NOTHING);
        }

    if (!strcmp("W", command))
        if (*WEST_ROOM(room) != '\0')
        {
            room= find_room(WEST_ROOM(room));
            return (DESCRIBE);
        }
        else
        {
            print(MSG_BAD_DIR);
            return (NOTHING);
        }

    if (!strcmp("U", command))
        if (*UP_ROOM(room) != '\0')
        {
            room= find_room(UP_ROOM(room));
            return (DESCRIBE);
        }
        else
        {
            print(MSG_BAD_DIR);
            return (NOTHING);
        }

    if (!strcmp("D", command))
        if (*DOWN_ROOM(room) != '\0')
        {
            room= find_room(DOWN_ROOM(room));
            return (DESCRIBE);
        }
        else
        {
            print(MSG_BAD_DIR);
            return (NOTHING);
        }

    if (!strncmp("GET ", command, 4) || !strncmp("TAKE ", command, 5))
    {
        item = strchr(command, ' ') + 1;
        switch (get_item(item))
        {
            case ERR_ALREADY_CARRYING:
                print("You are already carrying ");
                a_or_an(item);
                print(item);
                print(".\n");
                break;

            case ERR_NOT_HERE:
                print("I see no ");
                print(item);
                print(" here that you can pick up.\n");
                break;

            case ERR_HANDS_FULL:
                print("Your hands are full - you can't carry any more.\n");
                break;

            default:
                if (echo)
                    print(MSG_OK);
                break;
        }
        return (NOTHING);
    }

    if (!strncmp("DROP ", command, 5) || !strncmp("THROW ", command, 6))
    {
        item = strchr(command, ' ') + 1;
        switch (drop_item(item))
        {
            case ERR_NOT_CARRYING:
                print("You are not carrying ");
                a_or_an(item);
                print(item);
                print(".\n");
                break;

            default:
                if (echo)
                    print(MSG_OK);
                break;
        }
        return (NOTHING);
    }

    if (!strncmp("INVENTORY", command, strlen(command)) && (strlen(command) >= 6))
    {
        has_item = 0;
        for (i = 0; i < inventory_limit; ++i)
            if (inventory[i][0] != '\0')
            {
                has_item = 1;
                break;
            }

        if (!has_item)
        {
            print("You are not carrying anything.\n");
            return (NOTHING);
        }

        print("\nYou are currently holding the following:\n\n");
        for (i = 0; i < inventory_limit; ++i)
            if (inventory[i][0] != '\0')
            {
                print("- ");
                print(inventory[i]);
                print(" -\n");
            }

        print("\n");
        return (NOTHING);
    }

    if (!strcmp("LOOK", command))
    {
        describe_location();
        return (NOTHING);
    }

    if (!strcmp("SUSPEND", command))
    {
        strcpy(suspend_name, adventure_name);
        strcat(suspend_name, ".sus");
        if (open_suspend(suspend_name, &suspend_file) == ABORT_OPEN)
        {
            print("It's OK to change your mind!\n");
            return(NOTHING);
        }

        if (suspend_file == NULL)
        {
            print("Problem saving game data.\n");
            return (NOTHING);
        }

        /* write adventure name */
        write_line(suspend_file, adventure_name);
        
        /* write the header information */
        write_num(suspend_file, room);
        write_cr(suspend_file);

        /* write data for each room */
        for (loop_room = 0; loop_room < num_rooms; ++loop_room)
        {
            write_line(suspend_file, ROOM_DESC_CONTROL(loop_room));
            write_line(suspend_file, CONTENTS(loop_room));
            write_line(suspend_file, NORTH_ROOM(loop_room));
            write_line(suspend_file, SOUTH_ROOM(loop_room));
            write_line(suspend_file, EAST_ROOM(loop_room));
            write_line(suspend_file, WEST_ROOM(loop_room));
            write_line(suspend_file, UP_ROOM(loop_room));
            write_line(suspend_file, DOWN_ROOM(loop_room));
            write_line(suspend_file, LOCAL_ACTION(loop_room));
         }

        /* write global commands */
        for (i = 0; i < num_global_commands; ++i)
            write_line(suspend_file, GLOBAL_ACTION(i));

        for (i = 0; i < inventory_limit; ++i)
            write_line(suspend_file, inventory[i]);

        fclose(suspend_file);
        print(MSG_OK);
        return (NOTHING);
    }

    if (!strcmp("RESUME", command))
    {
        strcpy(suspend_name, adventure_name);
        strcat(suspend_name, ".sus");
        if (open_resume(suspend_name, &suspend_file) == ABORT_OPEN)
    {
        print("It's OK to change your mind!\n");
        return(NOTHING);
        }
        
    if (suspend_file == NULL)
        {
            print("Game not previously suspended.\n");
            return (NOTHING);
        }

        /* check adventure name to make sure it matches */
        read_line(suspend_file, saved_name);
        if (strcmp(adventure_name, saved_name))
        {
            print("That game was suspended from a different adventure.\n");
            fclose(suspend_file);
            return(NOTHING);
        }
        
        /* read the header information */
        read_line(suspend_file, line);
    sscanf(line, "%d", &room);

        /* read data for each room */
        for (loop_room = 0; loop_room < num_rooms; ++loop_room)
        {
            read_line(suspend_file, ROOM_DESC_CONTROL(loop_room));
            read_line(suspend_file, CONTENTS(loop_room));
            read_line(suspend_file, NORTH_ROOM(loop_room));
            read_line(suspend_file, SOUTH_ROOM(loop_room));
            read_line(suspend_file, EAST_ROOM(loop_room));
            read_line(suspend_file, WEST_ROOM(loop_room));
            read_line(suspend_file, UP_ROOM(loop_room));
            read_line(suspend_file, DOWN_ROOM(loop_room));
            read_line(suspend_file, LOCAL_ACTION(loop_room));
         }

        for (i = 0; i < num_global_commands; ++i)
            read_line(suspend_file, GLOBAL_ACTION(i));

        for (i = 0; i < inventory_limit; ++i)
            read_line(suspend_file, inventory[i]);

        fclose(suspend_file);
        return (DESCRIBE);
    }

    if (!strcmp("HELP", command))
    {
        print("\nThese are some of the commands you may use:\n\n");
        print("NORTH or N      (go north)\n");
        print("SOUTH or S      (go south)\n");
        print("EAST or E       (go east)\n");
        print("WEST or W       (go west)\n");
        print("UP or U         (go up)\n");
        print("DOWN or D       (go down)\n");
        print("INVENT          (see your inventory - what you are carrying)\n");
        print("LOOK            (see where you are)\n");
        print("SUSPEND         (save game to finish later)\n");
        print("RESUME          (take up where you left off last time)\n");
        print("QUIT or STOP    (quit game)\n\n");
        return (NOTHING);
    }

    if (!strcmp("QUIT", command) || !strcmp("STOP", command))
    {
        quit_adventure = YOU_QUIT;
        if (echo)
            print(MSG_OK);
        return (NOTHING);
    }

    if (token_search(all_commands, command, ','))
    {
        print("You can't do that here.\n");
    }
    else
        print("I don't understand.\n");

    return (NOTHING);
}

find_room(id)
    char *id;
{
    int i;

    for (i=0; i<num_rooms; ++i)
    if (!strcmp(ROOM_ID(i), id))
        return(i);

    printf("Warning: unknown room id\n");
    return(0);
}

/* Explore - an adventure game interpreter by Joe Peterson & Dewitt Crandell *
 *           Translated to C from BASIC by Joe Peterson                      *
 *                                                                           */

#include        <stdio.h>
#include        <string.h>
#define DEFINE_GLOBALS
#include    "explore.h"


main()
{
    char            command[COMMAND_LENGTH];
    int             prompt_mode = DESCRIBE;
    int             i;

    room_id = (char *) malloc(MAX_ROOMS * 256 * sizeof(char));
    room_desc = (char *) malloc(MAX_ROOMS * 256 * sizeof(char));
    room_desc_alt = (char *) malloc(MAX_ROOMS * 256 * sizeof(char));
    room_desc_control = (char *) malloc(MAX_ROOMS * 8 * sizeof(char));
    contents = (char *) malloc(MAX_ROOMS * 128 * sizeof(char));
    north_room = (char *) malloc(MAX_ROOMS * 8 * sizeof(char));
    south_room = (char *) malloc(MAX_ROOMS * 8 * sizeof(char));
    east_room = (char *) malloc(MAX_ROOMS * 8 * sizeof(char));
    west_room = (char *) malloc(MAX_ROOMS * 8 * sizeof(char));
    up_room = (char *) malloc(MAX_ROOMS * 8 * sizeof(char));
    down_room = (char *) malloc(MAX_ROOMS * 8 * sizeof(char));
    local_command = (char *) malloc(MAX_ROOMS * 128 * sizeof(char));
    local_action = (char *) malloc(MAX_ROOMS * 256 * sizeof(char));

    global_command = (char *) malloc(MAX_GLOBALS * 128 * sizeof(char));
    global_action = (char *) malloc(MAX_GLOBALS * 256 * sizeof(char));
    global_location = (char *) malloc(MAX_GLOBALS * 16 * sizeof(char));

    initialize();

    print("\n\n*** EXPLORE ***  ver 4.4C\n\n");
    quit_adventure= load_adventure();

    if (!quit_adventure)
    {
        for (i = 0; i < inventory_limit; ++i)
            inventory[i][0] = '\0';

        printn(title);
        print("\n");
    }
    
    while (!quit_adventure)
    {
        check_for_action();
        if (quit_adventure)
            break;

        if (prompt_mode == DESCRIBE)
            describe_location();

        prompt_player(command);
        prompt_mode = process_command(command);
    }

    shutdown();
    
    free(room_desc);
    free(room_desc_alt);
    free(room_desc_control);
    free(contents);
    free(north_room);
    free(south_room);
    free(east_room);
    free(west_room);
    free(up_room);
    free(down_room);
    free(local_command);
    free(local_action);

    free(global_command);
    free(global_action);
    free(global_location);
}

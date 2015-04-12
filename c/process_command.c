/* Explore program module.                                               */
/* This module determines if a command is custom or possibly built in.   */

#include    <stdio.h>
#include    <string.h>
#include    "explore.h"


process_command(command)
    char           *command;
{
    char           *colon_ptr, *command_ptr;
    int             i, global_room;

    if (*command == '\0')
    return(NOTHING);

    if (token_search(all_commands, command, ',') == NULL)
        return (do_built_in(command, 1));

    if ((*LOCAL_COMMAND(room) != '\0') &&
        (*LOCAL_COMMAND(room) != '+') &&
        (*LOCAL_COMMAND(room) != '-'))
    {
        colon_ptr = strchr(LOCAL_COMMAND(room), ':');
        if (colon_ptr != NULL)
            *colon_ptr = '\0';

        command_ptr = token_search(LOCAL_COMMAND(room), command, ',');

        if (colon_ptr != NULL)
            *colon_ptr = ':';


        if (command_ptr != NULL)
        {
            if ((colon_ptr == NULL) ||
                ((colon_ptr != NULL) && (*(colon_ptr + 1) == '-') && !carrying_item(colon_ptr + 2)) ||
                ((colon_ptr != NULL) && (*(colon_ptr + 1) != '-') && carrying_item(colon_ptr + 1)))
                return (do_custom(LOCAL_ACTION(room), 1, 0));
            else
            {
                print("You can't do that yet.\n");
                return (NOTHING);
            }
        }
    }

    for (i = 0; i < num_global_commands; ++i)
    {
    colon_ptr = strchr(GLOBAL_COMMAND(i), ':');
    if (colon_ptr != NULL)
        *colon_ptr = '\0';
    
    command_ptr = token_search(GLOBAL_COMMAND(i), command, ',');
    
    if (colon_ptr != NULL)
        *colon_ptr = ':';
    
    if (*GLOBAL_LOCATION(i) != '\0')
        global_room= find_room(GLOBAL_LOCATION(i));
    else
        global_room= -1;
    
    if ((command_ptr != NULL) &&
        (room == global_room || global_room == -1))
    {
        if ((colon_ptr == NULL) ||
        ((colon_ptr != NULL) && (*(colon_ptr + 1) == '-') && !carrying_item(colon_ptr + 2)) ||
        ((colon_ptr != NULL) && (*(colon_ptr + 1) != '-') && carrying_item(colon_ptr + 1)))
        {
        return (do_custom(GLOBAL_ACTION(i), 1, 0));
        }
        else
        {
        print("You can't do that yet.\n");
        return (NOTHING);
        }
    }
    }

    return (do_built_in(command, 1));
}

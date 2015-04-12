/* Explore program module.                                               */
/* This module performs the specified local or global command.           */

#include    <stdio.h>
#include    <string.h>
#include    "explore.h"


do_custom(action_list, echo, auto_action)
char           *action_list;
int             echo, auto_action;
{
    char           *action, *colon_ptr, *delim_ptr;
    int             prompt_mode = NOTHING;
    int             l_room;
    int             error = 0;
    int            erase_action= 0;

    if (*action_list == '\0')
    {
    if (echo)
        print("Nothing happens.\n");
    return(prompt_mode);
    }

    action = strtok(action_list, ";");
    do
    {
        colon_ptr = strchr(action, ':');
        if (colon_ptr != NULL)
            *colon_ptr = '\0';
    
        switch (*action)
        {
            case '/':
        room= find_room(action + 1);
        prompt_mode = DESCRIBE;
        break;
        
            case '!':
        print("\n");
        printn(action + 1);
        quit_adventure = YOU_WIN;
        break;
        
            case '=':
        prompt_mode= do_built_in(action + 1, 0);
        break;
        
            case '%':
        delim_ptr = strchr(action + 1, ',');
        if (delim_ptr != NULL)
        {
        *delim_ptr = '\0';
        if (lose_item(action + 1) != ERR_NOT_CARRYING)
            obtain_item(delim_ptr + 1);
        else if (remove_item(action + 1, room) != ERR_NOT_HERE)
            deposit_item(delim_ptr + 1, room);
        else
        {
            error = 1;
            if (echo)
            print("You can't do that yet.\n");
        }
        *delim_ptr = ',';
        }
        break;
        
            case '+':
        if (*(action + 1) == '$')
        {
        if (obtain_item(action + 2) == ERR_HANDS_FULL)
        {
            error = 1;
            if (echo)
            print("You are carrying too much to do that.\n");
        }
        else
            erase_action= 1;
        }
        else
        {
        deposit_item(action + 1, room);
        *action_list = '+';
        *(action_list + 1) = '\0';
        }
        break;
        
            case '-':
        if (lose_item(action + 1) == ERR_NOT_CARRYING)
        if (remove_item(action + 1, room) == ERR_NOT_HERE)
        {
            error = 1;
            if (echo)
            print("You can't do that yet.\n");
        }
        break;
        
            case '#':
        delim_ptr = strchr(action + 1, '>');
        if (delim_ptr != NULL)
        {
        *delim_ptr = '\0';
        l_room= find_room(action + 1);
        if (lose_item(delim_ptr + 1) != ERR_NOT_CARRYING)
            deposit_item(delim_ptr + 1, l_room);
        else if (remove_item(delim_ptr + 1, room) != ERR_NOT_HERE)
            deposit_item(delim_ptr + 1, l_room);
        else
        {
            error = 1;
            if (echo)
            print("You can't do that yet.\n");
        }
        
        *delim_ptr = '>';
        }
        break;
        
            case '[':
        if (*(action + 1) == '$')
        {
        switch (*(action + 2))
        {
            case 'N':
            *NORTH_ROOM(room)= '\0';
            break;
            
            case 'S':
            *SOUTH_ROOM(room)= '\0';
            break;
            
            case 'E':
            *EAST_ROOM(room)= '\0';
            break;
            
            case 'W':
            *WEST_ROOM(room)= '\0';
            break;
            
            case 'U':
            *UP_ROOM(room)= '\0';
            break;
            
            case 'D':
            *DOWN_ROOM(room)= '\0';
            break;
            
            default:
            break;
        }
        }
        else
        {
        switch (*(action + 1))
        {
            case 'N':
            strcpy(NORTH_ROOM(room), action + 2);
            break;
            
            case 'S':
            strcpy(SOUTH_ROOM(room), action + 2);
            break;
            
            case 'E':
            strcpy(EAST_ROOM(room), action + 2);
            break;
            
            case 'W':
            strcpy(WEST_ROOM(room), action + 2);
            break;
            
            case 'U':
            strcpy(UP_ROOM(room), action + 2);
            break;
            
            case 'D':
            strcpy(DOWN_ROOM(room), action + 2);
            break;
            
            default:
            break;
        }
        }
        
        erase_action= 1;
        break;
        
            case '*':
        if (*ROOM_DESC_CONTROL(room) != '\0')
        {
        if (*(action + 1) == '+')
        {
            if (*(ROOM_DESC_CONTROL(room) + strlen(ROOM_DESC_CONTROL(room)) - 1) != '+')
            strcat(ROOM_DESC_CONTROL(room), "+");
        }
        else
        {
            if (*(ROOM_DESC_CONTROL(room) + strlen(ROOM_DESC_CONTROL(room)) - 1) == '+')
            *(ROOM_DESC_CONTROL(room) + strlen(ROOM_DESC_CONTROL(room))) = '\0';
        }
        }
        break;
        
            case '\0':
        break;
        
            default:
        print("\n");
        printn(action);
        quit_adventure = YOU_DIE;
        break;
        }
    
        if (colon_ptr != NULL)
        {
            if (!error)
            {
                if ((prompt_mode == DESCRIBE) || auto_action)
                    print("\n");
                printn(colon_ptr + 1);
            }
            *colon_ptr = ':';
        }
    } while ((action = nexttok(";")) != NULL);
    
    if (erase_action)
    *action_list= '\0';

    return (prompt_mode);
}

/* Explore program module.                                               */
/* This module loads a given adventure into memory and provides some     */
/* common I/O routines.                                                  */

#include    <stdio.h>
#include    <string.h>
#include    "explore.h"


load_adventure()
{
    int             i, cur_room, cur_global;
    FILE           *adventure_file;
    char        line[1024], keyword[1024], value[1024];
    int            scope;
    char        start_room[256];

    if (open_adventure(&adventure_file) == YOU_QUIT)
    return(YOU_QUIT);
    
    print("\n");
    print(adventure_name);
    print(" is now being built...\n\n\n");

    scope= 0;
    all_commands[0] = '\0';
    num_rooms= 0;
    cur_room= -1;
    cur_global= -1;
    room= 0;
    inventory_limit= 0;
    strcpy(title, "This adventure has no title!");

    while (read_line(adventure_file, line) != EOF)
    {
    if (*line == '\0')
        continue;

    parse_adv_line(line, keyword, value);

    if (!strcmp(keyword, "TITLE"))
    {
        strcpy(title, value);
    }
    else if (!strcmp(keyword, "START_ROOM"))
    {
        strcpy(start_room, value);
    }
    else if (!strcmp(keyword, "INVENTORY_LIMIT"))
    {
        inventory_limit= atoi(value);
    }
    else if (!strcmp(keyword, "ROOM"))
    {
        scope= 0;
        ++cur_room;
        strcpy(ROOM_ID(cur_room), value);
        strcpy(ROOM_DESC(cur_room), "");
        strcpy(ROOM_DESC_ALT(cur_room), "");
        strcpy(ROOM_DESC_CONTROL(cur_room), "");
        strcpy(CONTENTS(cur_room), "");
        strcpy(NORTH_ROOM(cur_room), "");
        strcpy(SOUTH_ROOM(cur_room), "");
        strcpy(EAST_ROOM(cur_room), "");
        strcpy(WEST_ROOM(cur_room), "");
        strcpy(UP_ROOM(cur_room), "");
        strcpy(DOWN_ROOM(cur_room), "");
        strcpy(LOCAL_COMMAND(cur_room), "");
        strcpy(LOCAL_ACTION(cur_room), "");

        /* if this is the starting place, initialize room */
        if (!strcmp(start_room, value))
        room= cur_room;
    }
    else if (!strcmp(keyword, "LOCAL"))
    {
        scope= 1;
        ++cur_global;
        strcpy(GLOBAL_LOCATION(cur_global), value);
        strcpy(GLOBAL_COMMAND(cur_global), "");
        strcpy(GLOBAL_ACTION(cur_global), "");
    }
    else if (!strcmp(keyword, "GLOBAL"))
    {
        scope= 1;
        ++cur_global;
        strcpy(GLOBAL_LOCATION(cur_global), "");
        strcpy(GLOBAL_COMMAND(cur_global), "");
        strcpy(GLOBAL_ACTION(cur_global), "");
    }
    else if (!strcmp(keyword, "COMMAND"))
    {
        if (scope == 0 && cur_room >= 0)
        strcpy(LOCAL_COMMAND(cur_room), value);
        else if (cur_global >= 0)
        strcpy(GLOBAL_COMMAND(cur_global), value);
    }
    else if (!strcmp(keyword, "ACTION"))
    {
        if (scope == 0 && cur_room >= 0)
        strcpy(LOCAL_ACTION(cur_room), value);
        else if (cur_global >= 0)
        strcpy(GLOBAL_ACTION(cur_global), value);
    }
    else if (!strcmp(keyword, "DESC"))
    {
        if (cur_room >= 0)
        strcpy(ROOM_DESC(cur_room), value);
    }
    else if (!strcmp(keyword, "ALT_DESC"))
    {
        if (cur_room >= 0)
        strcpy(ROOM_DESC_ALT(cur_room), value);
    }
    else if (!strcmp(keyword, "DESC_CONTROL"))
    {
        if (cur_room >= 0)
        strcpy(ROOM_DESC_CONTROL(cur_room), value);
    }
    else if (!strcmp(keyword, "CONTENTS"))
    {
        if (cur_room >= 0)
        strcpy(CONTENTS(cur_room), value);
    }
    else if (!strcmp(keyword, "NORTH"))
    {
        if (cur_room >= 0)
        strcpy(NORTH_ROOM(cur_room), value);
    }
    else if (!strcmp(keyword, "SOUTH"))
    {
        if (cur_room >= 0)
        strcpy(SOUTH_ROOM(cur_room), value);
    }
    else if (!strcmp(keyword, "EAST"))
    {
        if (cur_room >= 0)
        strcpy(EAST_ROOM(cur_room), value);
    }
    else if (!strcmp(keyword, "WEST"))
    {
        if (cur_room >= 0)
        strcpy(WEST_ROOM(cur_room), value);
    }
    else if (!strcmp(keyword, "UP"))
    {
        if (cur_room >= 0)
        strcpy(UP_ROOM(cur_room), value);
    }
    else if (!strcmp(keyword, "DOWN"))
    {
        if (cur_room >= 0)
        strcpy(DOWN_ROOM(cur_room), value);
    }
    else
        printf("Error: invalid keyword (%s)\n", keyword);
    }

    fclose(adventure_file);
    num_rooms = cur_room + 1;
    num_global_commands = cur_global + 1;

    /* if appropriate, add local commands to master list */
    for (i=0; i<num_rooms; ++i)
    {
        if ((*LOCAL_COMMAND(i) != '\0') &&
            (*LOCAL_COMMAND(i) != '+') &&
            (*LOCAL_COMMAND(i) != '-') &&
            (*LOCAL_ACTION(i) != '\0'))
        {
        add_command(LOCAL_COMMAND(i));
        }
    }

    /* add global commands to master list */
    for (i=0; i<num_global_commands; ++i)
    {
    add_command(GLOBAL_COMMAND(i));
    }

    return(0);
}


add_command(string)
    char *string;
{
    char           *pos, *command;
    
    pos = strchr(string, ':');
    if (pos != NULL)
    *pos = '\0';
    
    command = strtok(string, ",");
    do
    {
    if (token_search(all_commands, command, ',') == NULL)
    {
        if (all_commands[0] != '\0')
        strcat(all_commands, ",");
        strcat(all_commands, command);
    }
    } while ((command = nexttok(",")) != NULL);
    
    if (pos != NULL)
    *pos = ':';
}


parse_adv_line(line, keyword, value)
    char *line, *keyword, *value;
{
    char *delim;

    if (!(delim= strchr(line, '=')))
    {
    strcpy(keyword, line);
    strcpy(value, "");
    return;
    }

    strncpy(keyword, line, delim - line);
    keyword[delim - line]= '\0';
    strcpy(value, delim + 1);
}


read_line(file, string)
    FILE           *file;
    char            string[];

{
    char *ret;
    int len;

    ret= fgets(string, 1023, file);
    len= strlen(string);
    if (len)
    if (string[len-1] == '\n')
        string[len-1]= '\0';

    if (!ret)
    return(EOF);
    else
    return(0);
/*
    int             pos;
    char            c;

    pos = 0;
    while ((c = fgetc(file)) != '\n')
    {
        if (c == EOF)
        {
            string[0] = '\0';
            return (EOF);
        }
        string[pos++] = c;
    }
    string[pos] = '\0';
    return (0);
*/
}


write_line(file, string)
    FILE           *file;
    char           *string;
{
    fprintf(file, "%s\n", string);
}


write_num(file, num)
    FILE           *file;
    int             num;
{
    fprintf(file, "%d ", num);
}


read_til_cr(file)
    FILE           *file;
{
    char dummy[1024];

    fgets(dummy, 1023, file);
/*
    while (fgetc(file) != '\n');
*/
}


write_cr(file)
    FILE           *file;
{
    fputc('\n', file);
}

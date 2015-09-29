/* Explore file converter.
 * This converts old TRS-80 explore files to the new format.
 */

#include    <stdio.h>
#include    <string.h>
#define DEFINE_GLOBALS
#include    "explore.h"

#define ROOM(x, y, z) ((z) * size_x * size_y + (y) * size_x + (x))

main(argc, argv)
int argc;
char *argv[];
{
    int             i, loop_room;
    FILE           *adventure_file;
    char           *pos, *command;
    char            line[1024];
    int size_x, size_y, size_z;
    int start_x, start_y, start_z;
    int x, y, z;
    int valid;
    char action_end[1024], room_str[64];

    if (argc == 1)
        adventure_file= stdin;
    else
    {
        adventure_file= fopen(argv[1], "r");
        if (!adventure_file)
        {
            fprintf(stderr, "Error opening %s\n", argv[1]);
            exit(1);
        }
    }

    room_desc = (char *) malloc(100 * 256 * sizeof(char));
    room_desc_alt = (char *) malloc(100 * 256 * sizeof(char));
    room_desc_control = (char *) malloc(100 * 8 * sizeof(char));
    contents = (char *) malloc(100 * 128 * sizeof(char));
    north_room = (char *) malloc(100 * 8 * sizeof(char));
    south_room = (char *) malloc(100 * 8 * sizeof(char));
    east_room = (char *) malloc(100 * 8 * sizeof(char));
    west_room = (char *) malloc(100 * 8 * sizeof(char));
    up_room = (char *) malloc(100 * 8 * sizeof(char));
    down_room = (char *) malloc(100 * 8 * sizeof(char));
    local_command = (char *) malloc(100 * 128 * sizeof(char));
    local_action = (char *) malloc(100 * 256 * sizeof(char));

    global_command = (char *) malloc(50 * 128 * sizeof(char));
    global_action = (char *) malloc(50 * 256 * sizeof(char));
    global_location = (char *) malloc(50 * 16 * sizeof(char));

    all_commands[0] = '\0';

    /* read the title to be displayed at the beginning of the game */
    read_line(adventure_file, title);
    printf("TITLE=%s\n", title);

    /* read the header information */
    read_line(adventure_file, line);
    sscanf(line, "%d %d %d %d %d %d %d", &size_z, &size_x, &size_y, &start_z, &start_x, &start_y, &inventory_limit);
    size_x++;
    size_y++;
    size_z++;
    printf("START_ROOM=%d,%d,%d\n", start_x, start_y, start_z);
    printf("INVENTORY_LIMIT=%d\n", inventory_limit);

    /* read data for each room */
    for (z=0; z<size_z; ++z)
        for (y=0; y<size_y; ++y)
            for (x=0; x<size_x; ++x)
            {
                loop_room= ROOM(x, y, z);
                read_line(adventure_file, ROOM_DESC(loop_room));
                read_line(adventure_file, ROOM_DESC_ALT(loop_room));
                if (ROOM_DESC(loop_room)[0] == '\0' &&
                    ROOM_DESC_ALT(loop_room)[0] == '\0')
                    valid= 0;
                else
                    valid= 1;
                if (valid)
                    printf("\nROOM=%d,%d,%d\n", x, y, z);
                if (ROOM_DESC(loop_room)[0] != '\0' && valid)
                {
                    fix_text(ROOM_DESC(loop_room), 0);
                    printf("DESC=%s\n", ROOM_DESC(loop_room));
                }
                if (ROOM_DESC_ALT(loop_room)[0] != '\0' && valid)
                {
                    fix_text(ROOM_DESC_ALT(loop_room), 0);
                    printf("ALT_DESC=%s\n", ROOM_DESC_ALT(loop_room));
                }
                read_line(adventure_file, ROOM_DESC_CONTROL(loop_room));
                if (ROOM_DESC_CONTROL(loop_room)[0] != '\0' && valid)
                    printf("DESC_CONTROL=%s\n", ROOM_DESC_CONTROL(loop_room));
                read_line(adventure_file, CONTENTS(loop_room));
                if (CONTENTS(loop_room)[0] != '\0' && valid)
                    printf("CONTENTS=%s\n", CONTENTS(loop_room));
                read_line(adventure_file, line);
                if (line[0] != '.' && valid)
                    printf("NORTH=%d,%d,%d\n", x, y-1, z);
                if (line[1] != '.' && valid)
                    printf("SOUTH=%d,%d,%d\n", x, y+1, z);
                if (line[2] != '.' && valid)
                    printf("EAST=%d,%d,%d\n", x+1, y, z);
                if (line[3] != '.' && valid)
                    printf("WEST=%d,%d,%d\n", x-1, y, z);
                if (line[4] != '.' && valid)
                    printf("UP=%d,%d,%d\n", x, y, z+1);
                if (line[5] != '.' && valid)
                    printf("DOWN=%d,%d,%d\n", x, y, z-1);
                read_line(adventure_file, line);
                if (line[0] != '\0' && valid)
                    printf("COMMAND=%s\n", line);
                read_line(adventure_file, line);
                if (line[0] != '\0' && valid)
                {
                    fix_text(line, ':');

                    for (i=0; i<strlen(line); ++i)
                    {
                        if (line[i] == '[')
                        {
                            switch (line[i+1])
                            {
                                case 'N':
                                strcpy(action_end, line + i+2);
                                sprintf(room_str, "%d,%d,%d", x, y-1, z);
                                break;

                                case 'S':
                                strcpy(action_end, line + i+2);
                                sprintf(room_str, "%d,%d,%d", x, y+1, z);
                                break;

                                case 'E':
                                strcpy(action_end, line + i+2);
                                sprintf(room_str, "%d,%d,%d", x+1, y, z);
                                break;

                                case 'W':
                                strcpy(action_end, line + i+2);
                                sprintf(room_str, "%d,%d,%d", x-1, y, z);
                                break;

                                case 'U':
                                strcpy(action_end, line + i+2);
                                sprintf(room_str, "%d,%d,%d", x, y, z+1);
                                break;

                                case 'D':
                                strcpy(action_end, line + i+2);
                                sprintf(room_str, "%d,%d,%d", x, y, z-1);
                                break;
                            }

                            strcpy(line + i+2, room_str);
                            strcat(line, action_end);
                        }
                    }

                    printf("ACTION=%s\n", line);
                }

                /* if appropriate, add commands to master list */
                if (!valid)
                    continue;

                if ((*LOCAL_COMMAND(loop_room) != '\0') &&
                    (*LOCAL_COMMAND(loop_room) != '+') &&
                    (*LOCAL_COMMAND(loop_room) != '-') &&
                    (*LOCAL_ACTION(loop_room) != '\0'))
                {
                    pos = strchr(LOCAL_COMMAND(loop_room), ':');
                    if (pos != NULL)
                        *pos = '\0';

                    command = strtok(LOCAL_COMMAND(loop_room), ",");
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
            }

    /* read global commands */
    i = 0;
    while (read_line(adventure_file, GLOBAL_COMMAND(i)) != EOF)
    {
#if 0
        if (GLOBAL_COMMAND(i)[0] == '\0')
            continue;
#endif

        read_line(adventure_file, GLOBAL_ACTION(i));
        fix_text(GLOBAL_ACTION(i), ':');
        read_line(adventure_file, GLOBAL_LOCATION(i));

        if (GLOBAL_LOCATION(i)[0] != '\0')
            printf("\nLOCAL=%s\n", GLOBAL_LOCATION(i));
        else
            printf("\nGLOBAL\n");
        printf("COMMAND=%s\n", GLOBAL_COMMAND(i));

#if 0
        for (i=0; i<strlen(GLOBAL_ACTION(i)); ++i)
        {
            if (GLOBAL_ACTION(i)[i] == '[')
            {
                switch (GLOBAL_ACTION(i)[i+1])
                {
                    case 'N':
                    strcpy(action_end, GLOBAL_ACTION(i) + i+2);
                    sprintf(room_str, "%d,%d,%d", x, y-1, z);
                    break;

                    case 'S':
                    strcpy(action_end, GLOBAL_ACTION(i) + i+2);
                    sprintf(room_str, "%d,%d,%d", x, y+1, z);
                    break;

                    case 'E':
                    strcpy(action_end, GLOBAL_ACTION(i) + i+2);
                    sprintf(room_str, "%d,%d,%d", x+1, y, z);
                    break;

                    case 'W':
                    strcpy(action_end, GLOBAL_ACTION(i) + i+2);
                    sprintf(room_str, "%d,%d,%d", x-1, y, z);
                    break;

                    case 'U':
                    strcpy(action_end, GLOBAL_ACTION(i) + i+2);
                    sprintf(room_str, "%d,%d,%d", x, y, z+1);
                    break;

                    case 'D':
                    strcpy(action_end, GLOBAL_ACTION(i) + i+2);
                    sprintf(room_str, "%d,%d,%d", x, y, z-1);
                    break;
                }

                strcpy(GLOBAL_ACTION(i) + i+2, room_str);
                strcat(GLOBAL_ACTION(i), action_end);
            }
        }
#endif

        printf("ACTION=%s\n", GLOBAL_ACTION(i));

        /* add to command list */
        pos = strchr(GLOBAL_COMMAND(i), ':');
        if (pos != NULL)
            *pos = '\0';

        command = strtok(GLOBAL_COMMAND(i), ",");
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
        ++i;
    }
    if (i < 50)
        *GLOBAL_COMMAND(i) = '\0';

    num_global_commands = i;

    fclose(adventure_file);
    return(0);
}


fix_text(string, delim)
    char *string;
    int delim;
{
    char *start, *dpos, *pos;
    char rest[1024];

    if (delim)
        dpos= strrchr(string, delim);
    else
        dpos= NULL;

    if (!dpos)
        start= string;
    else
        start= dpos + 1;

    while (strlen(start) > 64)
    {
        pos= strchr(start, '\\');
        if (!pos || (pos - start) > 64)
        {
            fprintf(stderr, "line too long (truncating with \\):\n%s\n", start);

            start += 64;
            strcpy(rest, start);
            *start++ = '\\';
            strcpy(start, rest);
        }
        else
            start= pos + 1;
    }
}


read_line(file, string)
    FILE           *file;
    char            string[];
{
    int c;
    int len = 0;

    while ((c = fgetc(file)) != EOF && c != '\r')
    {
        if (c == '\n')
            c = '\\';

        string[len++] = c;
    }

    string[len] = '\0';

    if (c == EOF)
        return(EOF);
    else
        return(0);
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

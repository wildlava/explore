/* Explore file converter.
 * This converts the newer interim format explore files to the new format.
 */

#include    <stdio.h>
#include    <string.h>
#define DEFINE_GLOBALS
#include    "explore.h"


main(argc, argv)
    int argc;
    char *argv[];
{
    int             i, loop_room;
    FILE           *adventure_file;
    char           *pos, *command;
    char        line[1024];

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
    sscanf(line, "%d %d %d", &num_rooms, &room, &inventory_limit);
    printf("START_ROOM=%d\n", room);
    printf("INVENTORY_LIMIT=%d\n", inventory_limit);

    /* read data for each room */
    for (loop_room = 0; loop_room < num_rooms; ++loop_room)
    {
    printf("\nROOM=%d\n", loop_room);
        read_line(adventure_file, ROOM_DESC(loop_room));
    if (ROOM_DESC(loop_room)[0] != '\0')
        printf("DESC=%s\n", ROOM_DESC(loop_room));
        read_line(adventure_file, ROOM_DESC_ALT(loop_room));
    if (ROOM_DESC_ALT(loop_room)[0] != '\0')
        printf("ALT_DESC=%s\n", ROOM_DESC_ALT(loop_room));
        read_line(adventure_file, ROOM_DESC_CONTROL(loop_room));
    if (ROOM_DESC_CONTROL(loop_room)[0] != '\0')
        printf("DESC_CONTROL=%s\n", ROOM_DESC_CONTROL(loop_room));
        read_line(adventure_file, CONTENTS(loop_room));
    if (CONTENTS(loop_room)[0] != '\0')
        printf("CONTENTS=%s\n", CONTENTS(loop_room));
        read_line(adventure_file, line);
    if (line[0] != '\0')
        printf("NORTH=%s\n", line);
        read_line(adventure_file, line);
    if (line[0] != '\0')
        printf("SOUTH=%s\n", line);
        read_line(adventure_file, line);
    if (line[0] != '\0')
        printf("EAST=%s\n", line);
        read_line(adventure_file, line);
    if (line[0] != '\0')
        printf("WEST=%s\n", line);
        read_line(adventure_file, line);
    if (line[0] != '\0')
        printf("UP=%s\n", line);
        read_line(adventure_file, line);
    if (line[0] != '\0')
        printf("DOWN=%s\n", line);
        read_line(adventure_file, line);
    if (line[0] != '\0')
        printf("COMMAND=%s\n", line);
        read_line(adventure_file, line);
    if (line[0] != '\0')
        printf("ACTION=%s\n", line);

        /* if appropriate, add commands to master list */
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
        if (*line == '\0')
            break;
    
        read_line(adventure_file, GLOBAL_ACTION(i));
        read_line(adventure_file, GLOBAL_LOCATION(i));

    if (GLOBAL_LOCATION(i)[0] != '\0')
        printf("\nLOCAL=%s\n", GLOBAL_LOCATION(i));
    else
        printf("\nGLOBAL\n");
    printf("COMMAND=%s\n", GLOBAL_COMMAND(i));
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

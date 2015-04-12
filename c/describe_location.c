/* Explore program module.                                               */
/* This module describes the current room and its contents.              */

#include    <stdio.h>
#include    <string.h>
#include    "explore.h"


describe_location()
{
    char            new_control[8];
    char           *comma_pos;
    int             print_room_desc, print_room_desc_alt;
    char           *item;

    /* check which room description should be printed */
    if ((comma_pos = strchr(ROOM_DESC_CONTROL(room), ',')) != NULL)
        if (strchr(ROOM_DESC_CONTROL(room), '+') != NULL)
            strcpy(new_control, comma_pos + 1);
        else
        {
            strncpy(new_control, ROOM_DESC_CONTROL(room),
                    comma_pos - ROOM_DESC_CONTROL(room));
            *(new_control + (comma_pos - ROOM_DESC_CONTROL(room))) = '\0';
        }
    else
        strcpy(new_control, "RC");

    if ((strchr(new_control, 'R') != NULL) && (*ROOM_DESC(room) != '-'))
        print_room_desc = 1;
    else
        print_room_desc = 0;

    if ((strchr(new_control, 'C') != NULL) && (*ROOM_DESC_ALT(room) != '\0'))
        print_room_desc_alt = 1;
    else
        print_room_desc_alt = 0;

    if (print_room_desc || print_room_desc_alt || (*CONTENTS(room) != '\0'))
        print("\n");

    if (print_room_desc)
        printn(ROOM_DESC(room));

    if (print_room_desc_alt)
        printn(ROOM_DESC_ALT(room));

    if (*CONTENTS(room) != '\0')
    {
        item = strtok(CONTENTS(room), ",");
        do
        {
            print("There is ");
            a_or_an(item);
	    print(item);
            print(" here.\n");
        } while ((item = nexttok(",")) != NULL);
    }
}

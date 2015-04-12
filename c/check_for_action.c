/* Explore program module.                                               */
/* This module checks to see if an automatic function is to be           */
/* performed upon entering a room.                                       */

#include    <stdio.h>
#include    "explore.h"


check_for_action()
{
    char           *object_needed;

    if ((*LOCAL_COMMAND(room) == '\0') && (*LOCAL_ACTION(room) != '\0'))
        do_custom(LOCAL_ACTION(room), 0, 1);

    if (((*LOCAL_COMMAND(room) == '+') || (*LOCAL_COMMAND(room) == '-')) &&
        (*LOCAL_ACTION(room) != '\0'))
    {
        object_needed = LOCAL_COMMAND(room) + 1;
        if (((*(object_needed - 1) == '-') && !carrying_item(object_needed)) ||
            ((*(object_needed - 1) != '-') && carrying_item(object_needed)))
            do_custom(LOCAL_ACTION(room), 0, 1);
    }
}

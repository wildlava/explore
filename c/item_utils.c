/* Explore program module.                                               */
/* This module takes care of various tasks such as getting and dropping  */
/* things.  It also supports some custom functions dealing with items.   */

#include    <stdio.h>
#include    <string.h>
#include    "explore.h"


a_or_an(item)
    char   *item;
{
    int                i, v= 0;
    static    char    vowels[]= "AEIOU";
    
    for (i=0; i<5; ++i)
        if (*item == *(vowels + i))
            v= 1;
            
    if (v)
        print("an ");
    else
        print("a ");
}


get_item(item)
    char           *item;
{
    char            full_item[32];

    if (find_item_room(full_item, item, room) == ERR_NOT_HERE)
        if (!find_item_invent(full_item, item))
            return(ERR_ALREADY_CARRYING);
        else
            return (ERR_NOT_HERE);

    if (obtain_item(full_item) == ERR_HANDS_FULL)
        return (ERR_HANDS_FULL);

    remove_item(full_item, room);
    return (0);
}


find_item_room(full_item, item, l_room)
    char           *full_item, *item;
    int             l_room;
{
    char           *ptr, *next_comma;

    ptr = CONTENTS(l_room);
    do
    {
        next_comma = strchr(ptr, ',');

        if (next_comma == NULL)
            strcpy(full_item, ptr);
        else
        {
            strncpy(full_item, ptr, next_comma - ptr);
            *(full_item + (next_comma - ptr)) = '\0';
            ptr = next_comma + 1;
        }

        if (!strcmp(item, full_item) ||
            ((strchr(item, ' ') == NULL) && (token_search(full_item, item, ' ') != NULL)))
        {
            return (0);
        }

    } while (next_comma != NULL);

    return (ERR_NOT_HERE);
}


drop_item(item)
    char           *item;
{
    char            full_item[32];

    if (find_item_invent(full_item, item) == ERR_NOT_CARRYING)
        return (ERR_NOT_CARRYING);

    lose_item(full_item);
    deposit_item(full_item, room);
    return (0);
}


find_item_invent(full_item, item)
    char           *full_item, *item;
{
    int             i;

    for (i = 0; i < inventory_limit; ++i)
    {
        if (!strcmp(item, inventory[i]) ||
            ((strchr(item, ' ') == NULL) && (token_search(inventory[i], item, ' ') != NULL)))
        {
            strcpy(full_item, inventory[i]);
            return (0);
        }
    }
    return (ERR_NOT_CARRYING);
}


remove_item(item, l_room)
    char           *item;
    int             l_room;
{
    char           *start_ptr, *end_ptr;

    if ((start_ptr = token_search(CONTENTS(l_room), item, ',')) == NULL)
        return (ERR_NOT_HERE);

    if ((end_ptr = strchr(start_ptr, ',')) == NULL)
        if (start_ptr == CONTENTS(l_room))
            *start_ptr = '\0';
        else
            *(start_ptr - 1) = '\0';
    else
        shftstr(end_ptr + 1, start_ptr - end_ptr - 1);

    return (0);
}


deposit_item(item, l_room)
    char           *item;
    int             l_room;
{
    if (*CONTENTS(l_room) != '\0')
        strcat(CONTENTS(l_room), ",");
    strcat(CONTENTS(l_room), item);
}


lose_item(item)
    char           *item;
{
    int             i;

    for (i = 0; i < inventory_limit; ++i)
    {
        if (!strcmp(inventory[i], item))
        {
            inventory[i][0] = '\0';
            return (0);
        }
    }
    return (ERR_NOT_CARRYING);
}


obtain_item(item)
    char           *item;
{
    int             i;

    for (i = 0; i < inventory_limit; ++i)
    {
        if (inventory[i][0] == '\0')
        {
            strcpy(inventory[i], item);
            return (0);
        }
    }
    return (ERR_HANDS_FULL);
}


carrying_item(item)
    char           *item;
{
    int             i;

    for (i = 0; i < inventory_limit; ++i)
    {
        if (!strcmp(inventory[i], item))
        {
            return (1);
        }
    }
    return (0);
}

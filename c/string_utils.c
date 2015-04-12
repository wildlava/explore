/* Explore program module.                                               */
/* This module adds some string utilities not found in the standard      */
/* C library.                                                            */

#include    <stdio.h>
#include    <string.h>


/* This is an extention of the strtok function.   */
/* It replaces the previous delimiter with delim, */
/* preserving the original string.                */
char *
nexttok(delim)
    char           *delim;
{
    char           *new;

    new = strtok(NULL, delim);
    if (new != NULL)
        *(new - 1) = *delim;

    return (new);
}


char *
token_search(string, token, delim)
    char           *string, *token, delim;
{
    char           *ptr, *next_delim;

    ptr = string;
    do
    {
        next_delim = strchr(ptr, delim);

        if (next_delim == NULL)
        {
            if (!strcmp(token, ptr))
                return (ptr);
        }
        else
        {
            if (!strncmp(token, ptr, next_delim - ptr)
                && (strlen(token) == (next_delim - ptr)))
                return (ptr);

            ptr = next_delim + 1;
        }

    } while (next_delim != NULL);

    return (NULL);
}


shftstr(string, shift)
    char           *string;
    int             shift;
{
    char           *ptr, *start, *end;

    start = string;
    end = string + strlen(string);

    if (shift == 0)
        return;

    if (shift > 0)
        for (ptr = end; ptr >= start; --ptr)
            *(ptr + shift) = *ptr;
    else
        for (ptr = start; ptr <= end; ++ptr)
            *(ptr + shift) = *ptr;
}

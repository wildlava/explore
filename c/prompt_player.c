/* Explore program module.                                               */
/* This module gets instructions from the player.                        */

#include    <stdio.h>
#include    <string.h>

#define WHITESPACE(c) (((c) < '!') || ((c) > 'z'))


prompt_player(command)
    char           *command;
{
    char           *pos;
    int             len;
    
    input_line(command);

    /* remove excess whitespace from ends */
    while ((*command != '\0') && WHITESPACE(*command))
    shftstr(command + 1, -1);
    while ((*command != '\0') && WHITESPACE(*(command + strlen(command) - 1)))
    *(command + strlen(command) - 1) = '\0';

    /* reduce any whitespace strings to one space */
    for (pos=command; *pos != '\0'; ++pos)
    {
        if (WHITESPACE(*pos))
        {
            len= 0;
            while ((*(pos + 1) != '\0') && WHITESPACE(*(pos + 1)))
            {
                ++len;
                ++pos;
            }
        
            *pos = ' ';

            if (len)
            {
                shftstr(pos, -len);
                pos -= len;
            }
        }
    }
    
    /* convert command to upper case */
    for (pos=command; *pos != '\0'; ++pos)
    if ((*pos >= 'a') && (*pos <= 'z'))
        *pos &= 0xdf;
}

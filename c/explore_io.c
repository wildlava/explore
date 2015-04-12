/* Explore Input/Output routines using standard C methods */

#include        <stdio.h>
#include        <string.h>
#include        "explore.h"


initialize()
{
}


shutdown()
{
}


print(string)
    char           *string;
{
    printf("%s", string);
}


printn(string)
    char           *string;
{
    char           *pos, *old_pos;

    pos = old_pos = string;
    while (pos != NULL)
    {
        pos = strchr(old_pos, '\\');
        if (pos != NULL)
        {
            *pos = '\0';
            printf("%s\n", old_pos);
            *pos = '\\';
            old_pos = pos + 1;
        }
        else
            printf("%s\n", old_pos);
    }
}


input_line(string)
    char           *string;
{
    char           *string_ptr, c;

    string_ptr = string;
    while ((c = getc(stdin)) != '\n')
        if((string_ptr - string) < (COMMAND_LENGTH - 1))
            *string_ptr++ = c;

    *string_ptr = '\0';
}


open_adventure(adventure_file)
    FILE  **adventure_file;
{
    char            adventure_path[64];

    do
    {
        print("Name of adventure:\n");
        input_line(adventure_name);
        strcpy(adventure_path, PATH_FOR_ADV_FILES);
        strcat(adventure_path, adventure_name);
        strcat(adventure_path, ".exp");
        *adventure_file = fopen(adventure_path, "r");
    } while (*adventure_file == NULL);

    return(0);
}


open_suspend(suspend_name, suspend_file)
    char   *suspend_name;
    FILE  **suspend_file;
{
    *suspend_file = fopen(suspend_name, "w");
}


open_resume(suspend_name, suspend_file)
    char   *suspend_name;
    FILE  **suspend_file;
{
    *suspend_file = fopen(suspend_name, "r");
}

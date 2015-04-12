#include <stdio.h>

main()
{
    int c;

    while ((c= getchar()) != EOF)
    {
	if (c == '\n')
	    putchar('\\');
	else if (c == '\r')
            putchar('\n');
        else
            putchar(c);
    }
}

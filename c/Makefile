#   File:       ExploreTool.make
#   Target:     ExploreTool
#   Sources:    check_for_action.c
#               describe_location.c
#               do_built_in.c
#               do_custom.c
#               explore.c
#               explore_io.c
#               item_utils.c
#               load_adventure.c
#               process_command.c
#               prompt_player.c
#               string_utils.c
#   Created:    Thursday, November 17, 1988 11:44:21 PM

CFLAGS=
SOURCES = check_for_action.c describe_location.c do_built_in.c do_custom.c \
explore.c explore_io.c item_utils.c load_adventure.c process_command.c \
prompt_player.c string_utils.c

OBJECTS = $(SOURCES:.c=.o)

explore: $(OBJECTS)
	gcc -o explore $(OBJECTS)

clean:
	rm -f *~ *.o explore

#define MAX_ROOMS 100
#define MAX_GLOBALS 50

#define OFFSET_256(r)                 (256*(r))
#define OFFSET_128(r)                 (128*(r))
#define OFFSET_16(r)                  (16*(r))
#define OFFSET_8(r)                   (8*(r))

#define ROOM_ID(r)                 (room_id + OFFSET_16(r))
#define ROOM_DESC(r)                 (room_desc + OFFSET_256(r))
#define ROOM_DESC_ALT(r)                 (room_desc_alt + OFFSET_256(r))
#define ROOM_DESC_CONTROL(r)         (room_desc_control + OFFSET_8(r))
#define CONTENTS(r)                 (contents + OFFSET_128(r))
#define NORTH_ROOM(r)                 (north_room + OFFSET_16(r))
#define SOUTH_ROOM(r)                 (south_room + OFFSET_16(r))
#define EAST_ROOM(r)                 (east_room + OFFSET_16(r))
#define WEST_ROOM(r)                 (west_room + OFFSET_16(r))
#define UP_ROOM(r)                 (up_room + OFFSET_16(r))
#define DOWN_ROOM(r)                 (down_room + OFFSET_16(r))
#define LOCAL_COMMAND(r)                 (local_command + OFFSET_128(r))
#define LOCAL_ACTION(r)                 (local_action + OFFSET_256(r))

#define GLOBAL_COMMAND(i)                (global_command + OFFSET_128(i))
#define GLOBAL_ACTION(i)                (global_action + OFFSET_256(i))
#define GLOBAL_LOCATION(i)              (global_location + OFFSET_16(i))

#define COMMAND_LENGTH 64

/* path where adventure files are stored */
#define PATH_FOR_ADV_FILES "/home/joe/explore/"

/* return codes */
#define NOTHING 0
#define DESCRIBE 1
#define ABORT_OPEN 1

/* error codes */
#define ERR_ALREADY_CARRYING 1
#define ERR_NOT_HERE 2
#define ERR_HANDS_FULL 3
#define ERR_NOT_CARRYING 1

/* quit codes */
#define YOU_QUIT 1
#define YOU_WIN 2
#define YOU_DIE 3

/* Messages */
#define MSG_OK "Ok\n"
#define MSG_BAD_DIR "You can't go that way.\n"

/* internal functions */
char    *nexttok();
char    *token_search();

#ifdef DEFINE_GLOBALS
#define EXTERN
#else
#define EXTERN extern
#endif

/* internal functions */
EXTERN char    *nexttok();
EXTERN char    *token_search();

/* global variables */
EXTERN char     title[1000];

EXTERN int      num_rooms;
EXTERN int      room;

EXTERN int      inventory_limit;
EXTERN char     inventory[20][32];

EXTERN char    *room_id;
EXTERN char    *room_desc;
EXTERN char    *room_desc_alt;
EXTERN char    *room_desc_control;
EXTERN char    *contents;
EXTERN char       *north_room;
EXTERN char       *south_room;
EXTERN char       *east_room;
EXTERN char       *west_room;
EXTERN char       *up_room;
EXTERN char       *down_room;
EXTERN char    *local_command;
EXTERN char    *local_action;

EXTERN int      num_global_commands;
EXTERN char    *global_command;
EXTERN char    *global_action;
EXTERN char    *global_location;

EXTERN char     all_commands[1000];
EXTERN int      quit_adventure;
EXTERN char     adventure_name[16];

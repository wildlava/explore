/* Explore Input/Output routines using Macintosh methods */

#include    <stdio.h>
#include    <string.h>
#include    <Types.h>
#include    <QuickDraw.h>
#include    <Fonts.h>
#include    <Events.h>
#include    <Windows.h>
#include    <Menus.h>
#include    <TextEdit.h>
#include    <Dialogs.h>
#include    <Desk.h>
#include    <ToolUtils.h>
#include    <SegLoad.h>
#include    <Files.h>
#include    <OSUtils.h>
#include    <Packages.h>
#include    <Files.h>
#include    "explore.h"

/* Alert Box resource ID's */
#define WIN_ALERT 128
#define DIE_ALERT 129

extern _DataInit();

static    WindowPtr        window;
static    WindowRecord    window_record;
static    int                line_height;
static    RgnHandle        update_region;
static    Rect            local_content;
static    Ptr                save_under_bit_image;
static    int                row_bytes;


initialize ()
{
    Rect        wind_rect;
    FontInfo    font_info;

    UnloadSeg(_DataInit);
    InitGraf(&qd.thePort);
    InitFonts();
    InitWindows();
    InitMenus();
    TEInit();
    InitDialogs(nil);
    InitCursor();

    update_region= NewRgn();
    
    wind_rect.top= qd.screenBits.bounds.top + 40;
    wind_rect.left= qd.screenBits.bounds.left + 20;
    wind_rect.bottom= qd.screenBits.bounds.bottom - 20;
    wind_rect.right= qd.screenBits.bounds.right - 20;
    window = newwindow((Ptr) &window_record, &wind_rect, "Explore",
                       1, altDBoxProc, (WindowPtr) -1, 0, 0);
    SetPort(window);

    TextMode(srcOr);
    if ((wind_rect.right - wind_rect.left) >= 600)
    {
        TextFont(20);
        TextSize(18);
    }
    GetFontInfo(&font_info);
    line_height= font_info.ascent + font_info.descent;

    local_content= (**window_record.contRgn).rgnBBox;
    GlobalToLocal((Point *) &local_content.top);
    GlobalToLocal((Point *) &local_content.bottom);

    /* allocate space for save under for file boxes */
    row_bytes= (local_content.right - local_content.left + 7) / 8;
    if (row_bytes%2)
        ++row_bytes;
    save_under_bit_image= (Ptr) malloc(row_bytes *
                                       (local_content.bottom - local_content.top) *
                                       sizeof(char));
    
    MoveTo(5, local_content.bottom - 5);
}


shutdown()
{
    if (quit_adventure == YOU_WIN)
        Alert(WIN_ALERT, nil);
    else
        if (quit_adventure == YOU_DIE)
            Alert(DIE_ALERT, nil);
        
    CloseWindow(window);
    if (save_under_bit_image != NULL)
        free(save_under_bit_image);
    DisposeRgn(update_region);
}


print(string)
    char   *string;
{
    char   *pos, *old_pos;

    pos = old_pos = string;
    while (pos != NULL)
    {
        pos = strchr (old_pos, '\n');
        if (pos != NULL)
        {
            *pos = '\0';
            drawstring(old_pos);
            new_line(1);
            *pos = '\n';
            old_pos = pos + 1;
        }
        else
        {
            drawstring(old_pos);
        }
    }
}


printn (string)
    char   *string;
{
    char   *pos, *old_pos;

    pos = old_pos = string;
    while (pos != NULL)
    {
        pos = strchr (old_pos, '\\');
        if (pos != NULL)
        {
            *pos = '\0';
            drawstring(old_pos);
            new_line(1);
            *pos = '\\';
            old_pos = pos + 1;
        }
        else
        {
            drawstring(old_pos);
            new_line(1);
        }
    }
}


new_line(count)
    int        count;
{
    int        i;

    MoveTo(4, local_content.bottom - 4);
    for (i=0; i<count; ++i)
        ScrollRect(&local_content, 0, -line_height, update_region);
}


input_line(string)
    char   *string;
{
     EventRecord        event;
    char           *string_ptr, c;
    
    string_ptr= string;
    DrawChar('_');
    do
    {
        SystemTask();
        
        if (!GetNextEvent(everyEvent, &event))
            continue;

        switch (event.what)
        {
            case mouseDown:
                break;
                
            case autoKey:
            case keyDown:
                c= event.message & 0xff;
                if (((c == 0x08) || (c == 0x7f)) && (string_ptr > string))
                {
                    --string_ptr;
                    Move(-(CharWidth(*string_ptr) + CharWidth('_')), 0);
                    TextMode(srcBic);
                    DrawChar(*string_ptr);
                    DrawChar('_');
                    TextMode(srcOr);
                    Move(-(CharWidth(*string_ptr) + CharWidth('_')), 0);
                    DrawChar('_');
                }
                else
                    if (((c >= ' ') && (c <= 'z')) &&
                        ((string_ptr - string) < (COMMAND_LENGTH - 1)))
                    {
                        *string_ptr++= c;
                        Move(-CharWidth('_'), 0);
                        TextMode(srcBic);
                        DrawChar('_');
                        TextMode(srcOr);
                        Move(-CharWidth('_'), 0);
                        DrawChar(c);
                        DrawChar('_');
                    }
                break;

            default:
                break;

        }

    } while (c != '\n');
    Move(-CharWidth('_'), 0);
    TextMode(srcBic);
    DrawChar('_');
    TextMode(srcOr);

    *string_ptr= '\0';
    new_line(1);
}


open_adventure(adventure_file)
    FILE   **adventure_file;
{
    Point        box_loc;
    SFTypeList    file_type_list;
    SFReply        reply;

    box_loc.h= box_loc.v= 100;
    file_type_list[0]= 'EXPA';
    file_type_list[1]= 'TEXT';
    sfgetfile(&box_loc, "Name of Adventure:", 0,
              2, file_type_list, 0, &reply);
    if (reply.good)
    {
        pstrcpy(adventure_name, reply.fName);
        SetVol(nil, reply.vRefNum);
        *adventure_file = fopen(adventure_name, "r");
        return(0);
    }
    else
        return(YOU_QUIT);
}


open_suspend(suspend_name, suspend_file)
    char   *suspend_name;
    FILE  **suspend_file;
{
    Point        box_loc;
    SFReply        reply;
    BitMap        save_under;
    
    box_loc.h= box_loc.v= 100;

    if (save_under_bit_image != NULL)
    {
        save_under.baseAddr= save_under_bit_image;
        save_under.rowBytes= row_bytes;
        save_under.bounds= local_content;
    
        CopyBits(&window_record.port.portBits, &save_under,
                 &local_content, &local_content,
                 srcCopy, nil);
    }
    sfputfile(&box_loc, "Suspended game:", suspend_name, nil,
              &reply);
    if (save_under_bit_image != NULL)
    {
        CopyBits(&save_under, &window_record.port.portBits,
                 &local_content, &local_content,
                 srcCopy, nil);
    }
    
    if (reply.good)
    {
        pstrcpy(suspend_name, reply.fName);
        SetVol(nil, reply.vRefNum);
        create(suspend_name, reply.vRefNum, 'EXPL', 'EXPS');
        *suspend_file = fopen(suspend_name, "w");
        return(0);
    }
    else
        return(ABORT_OPEN);
}


open_resume(suspend_name, suspend_file)
    char   *suspend_name;
    FILE  **suspend_file;
{
    Point        box_loc;
    SFTypeList    file_type_list;
    SFReply        reply;
    BitMap        save_under;
    
    *file_type_list= 'EXPS';

    box_loc.h= box_loc.v= 100;

    if (save_under_bit_image != NULL)
    {
        save_under.baseAddr= save_under_bit_image;
        save_under.rowBytes= row_bytes;
        save_under.bounds= local_content;
    
        CopyBits(&window_record.port.portBits, &save_under,
                 &local_content, &local_content,
                 srcCopy, nil);
    }
    sfgetfile(&box_loc, "Suspended game:", nil,
              1, file_type_list, nil, &reply);
    if (save_under_bit_image != NULL)
    {
        CopyBits(&save_under, &window_record.port.portBits,
                 &local_content, &local_content,
                 srcCopy, nil);
    }

    if (reply.good)
    {
        pstrcpy(suspend_name, reply.fName);
        SetVol(nil, reply.vRefNum);
        *suspend_file = fopen(suspend_name, "r");
        return(0);
    }
    else
        return(ABORT_OPEN);
}


pstrcpy(dest, source)
    char   *dest, *source;
{
    int        i, string_len;
    char   *dest_ptr, *source_ptr;
    
    dest_ptr= dest;
    source_ptr= source;
    
    string_len= *source_ptr++;
    for (i=0; i<string_len; ++i)
        *dest_ptr++= *source_ptr++;
    *dest_ptr= '\0';
}
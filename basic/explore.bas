10 REM  ** EXPLORE ver 4.4 **  Copyright (C) 1982
20 REM     by Joe Peterson
30 REM     Peterson Computer Services
40 CLS
50 CLEAR 22000
60 DEFINT A-Z
70 PRINT CHR$(23):PRINT"*** EXPLORE ***  ver 4.4"
80 PRINT
90 ON ERROR GOTO 2940 
100 PRINT:LINE INPUT"Name of adventure: ";FI$
110 CLS
120 OPEN"I",1,FI$+"/EXP"
130 LINE INPUT#1,AT$
140 INPUT#1,DF,DX,DY,F,X,Y,IL
150 IL=IL-1
160 DIM DE$(DF,DX,DY),BL$(DF,DX,DY),PT$(DF,DX,DY),CN$(DF,DX,DY),PD$(DF,DX,DY),NI$(DF,DX,DY),TD$(DF,DX,DY),I$(IL),UC$(50),UT$(50),UD$(50),SU$(9),SL$(9)
170 PRINT@ 448,FI$;" is now being built..."
180 S=0:SL$(0)=","
190 FOR F1=0 TO DF:FOR Y1=0 TO DY:FOR X1=0 TO DX
200 LINE INPUT#1,DE$(F1,X1,Y1):LINE INPUT#1,BL$(F1,X1,Y1):LINE INPUT#1,PT$(F1,X1,Y1):LINE INPUT#1,CN$(F1,X1,Y1):LINE INPUT#1,PD$(F1,X1,Y1):LINE INPUT#1,NI$(F1,X1,Y1):LINE INPUT#1,TD$(F1,X1,Y1)
210 IF NI$(F1,X1,Y1)="" OR LEFT$(NI$(F1,X1,Y1),1)="+" OR LEFT$(NI$(F1,X1,Y1),1)="-" OR TD$(F1,X1,Y1)="" GOTO 270
220 IN=INSTR(NI$(F1,X1,Y1),":"):IF IN=0 THEN IN=LEN(NI$(F1,X1,Y1))+1
230 I2=0
240 I1=I2+1:I2=INSTR(I1,NI$(F1,X1,Y1),","):IF I2=0 THEN I2=IN
250 S$=MID$(NI$(F1,X1,Y1),I1,I2-I1):IF LEN(SL$(S))+LEN(S$)>254 THEN S=S+1:SL$(S)=","
260 SL$(S)=SL$(S)+S$+",":IF I2<>IN GOTO 240  
270 NEXT:NEXT:NEXT
280 IF SL$(S)="," THEN SL$(S)=""
290 S=0:SU$(0)=","
300 FOR U=0 TO 50
310 IF EOF(1) GOTO 400  
320 LINE INPUT#1,UC$(U):LINE INPUT#1,UT$(U):LINE INPUT#1,UD$(U)
330 IN=INSTR(UC$(U),":"):IF IN=0 THEN IN=LEN(UC$(U))+1
340 I2=0
350 I1=I2+1:I2=INSTR(I1,UC$(U),","):IF I2=0 THEN I2=IN
360 S$=MID$(UC$(U),I1,I2-I1):IF LEN(SU$(S))+LEN(S$)>254 THEN S=S+1:SU$(S)=","
370 SU$(S)=SU$(S)+S$+",":IF I2<>IN GOTO 350  
380 NEXT
390 IF SU$(S)="," THEN SU$(S)=""
400 CLOSE
410 CLS
420 PRINT TAB(14) "*** The EXPLORE Adventure Series ***"
430 PRINT:PRINT AT$:PRINT
440 L=-1
450 PRINT"Do you want to resume a suspended game? (Y/N): ";
460 Y$=INKEY$:IF Y$<>"Y" AND Y$<>"y" AND Y$<>"N" AND Y$<>"n" GOTO 460
470 IF Y$="N" OR Y$="n" PRINT"No":GOTO 640 ELSE PRINT"Yes"
480 PRINT"Which suspend file? (0-9) --> ";
490 SF$=INKEY$:IF SF$="" GOTO 490
500 IF ASC(SF$)<48 OR ASC(SF$)>57 GOTO 490
510 PRINT SF$
520 OPEN"I",1,FI$+"/S"+SF$
530 INPUT#1,F,X,Y,L
540 FOR I=0 TO IL:LINE INPUT#1,I$(I):NEXT
550 FOR F1=0 TO DF:FOR Y1=0 TO DY:FOR X1=0 TO DX
560 LINE INPUT#1,PT$:IF PT$="+" AND RIGHT$(PT$(F1,X1,Y1),1)<>"+" THEN PT$(F1,X1,Y1)=PT$(F1,X1,Y1)+"+" ELSE IF RIGHT$(PT$(F1,X1,Y1),1)="+" THEN PT$(F1,X1,Y1)=LEFT$(PT$(F1,X1,Y1),LEN(PT$(F1,X1,Y1))-1)
570 LINE INPUT#1,CN$(F1,X1,Y1):LINE INPUT#1,TD$:IF TD$="" GOTO 590 
580 IF TD$="+" THEN TD$(F1,X1,Y1)="+" ELSE PD$(F1,X1,Y1)=TD$:TD$(F1,X1,Y1)="["
590 NEXT:NEXT:NEXT:FOR U=0 TO 50:IF EOF(1) GOTO 620 
600 LINE INPUT#1,UT$:IF UT$="+" THEN UT$(U)="+"
610 NEXT
620 CLOSE
630 L=L+1
640 D=1
650 IF TD$(F,X,Y)="" OR (NI$(F,X,Y)<>"" AND LEFT$(NI$(F,X,Y),1)<>"+" AND LEFT$(NI$(F,X,Y),1)<>"-") GOTO 1530 
660 CI=0:IF NI$(F,X,Y)="" GOTO 690  
670 N$=RIGHT$(NI$(F,X,Y),LEN(NI$(F,X,Y))-1):FOR I=0 TO IL:IF I$(I)=N$ THEN IF LEFT$(NI$(F,X,Y),1)="-" GOTO 1530 ELSE GOTO 690  
680 NEXT:IF LEFT$(NI$(F,X,Y),1)="+" GOTO 1530 
690 TD$=TD$(F,X,Y):TS=0:GOSUB 2830 :GOTO 1530 
700 IF LEFT$(TD$,1)<>"/" GOTO 740  
710 I1=INSTR(TD$,","):I2=INSTR(I1+1,TD$,","):I3=INSTR(TD$,":")
720 IF I3=0 THEN I3=LEN(TD$)+1 ELSE PRINT:PRINT RIGHT$(TD$,LEN(TD$)-I3)
730 X=VAL(MID$(TD$,2,I1-2)):Y=VAL(MID$(TD$,I1+1,I2-I1-1)):F=VAL(MID$(TD$,I2+1,I3-I2-1)):GOTO 630
740 IF LEFT$(TD$,1)<>"!" GOTO 780  
750 IF L=0 GOTO 1530 
760 PRINT:PRINT STRING$(64,"*");:IF TD$<>"!" PRINT RIGHT$(TD$,LEN(TD$)-1):PRINT
770 PRINT"It took you";L;"moves to win.":PRINT STRING$(64,"*");:GOTO 2990 
780 IF LEFT$(TD$,1)=":" PRINT RIGHT$(TD$,LEN(TD$)-1):RETURN
790 IF LEFT$(TD$,1)<>"=" GOTO 840  
800 IF TD$="=" RETURN
810 M$="":IN=INSTR(TD$,":"):IF IN=0 THEN IN=LEN(TD$)+1 ELSE M$=RIGHT$(TD$,LEN(TD$)-IN)
820 C$=MID$(TD$,2,IN-2):IF M$<>"" PRINT M$
830 GOTO 1680 
840 IF LEFT$(TD$,1)<>"%" GOTO 930  
850 I1=INSTR(TD$,","):IF I1=0 RETURN
860 M$="":I2=INSTR(TD$,":"):IF I2=0 OR I2<I1 THEN I2=LEN(TD$)+1 ELSE M$=RIGHT$(TD$,LEN(TD$)-I2)
870 A$=MID$(TD$,2,I1-2):I2$=MID$(TD$,I1+1,I2-I1-1)
880 FOR I=0 TO IL:IF I$(I)=A$ THEN I$(I)=I2$:GOTO 910  
890 NEXT:GOSUB 2690 :IF I1=0 THEN ER$="You can't do that yet.":GOSUB 1510:RETURN
900 CN$=I2$:GOSUB 2750 :IF CN$="" THEN ER$="The room is too crowded for you to do that.":GOSUB 1510:GOTO 650  ELSE GOSUB 2690 :GOSUB 2780 
910 IF M$<>"" PRINT M$
920 RETURN
930 IF LEFT$(TD$,1)<>"+" GOTO 1030  
940 IF TD$="+" THEN ER$="Nothing happens.":GOSUB 1510:RETURN
950 M$="":IN=INSTR(TD$,":"):IF IN=0 THEN IN=LEN(TD$)+1 ELSE M$=RIGHT$(TD$,LEN(TD$)-IN)
960 IF MID$(TD$,2,1)<>"$" GOTO 990  
970 CN$=MID$(TD$,3,IN-3):FOR I=0 TO IL:IF I$(I)="" THEN I$(I)=CN$:GOTO 1000  
980 NEXT:ER$="You are carrying too much to do that.":GOSUB 1510:RETURN
990 CN$=MID$(TD$,2,IN-2):GOSUB 2750 :IF CN$="" THEN ER$="The room is too crowded for you to do that.":GOSUB 1510:RETURN
1000 TD$="+"
1010 IF M$<>"" PRINT M$
1020 RETURN
1030 IF LEFT$(TD$,1)<>"-" GOTO 1110  
1040 IF TD$="-" RETURN
1050 M$="":IN=INSTR(TD$,":"):IF IN=0 THEN IN=LEN(TD$)+1 ELSE M$=RIGHT$(TD$,LEN(TD$)-IN)
1060 A$=MID$(TD$,2,IN-2)
1070 FOR I=0 TO IL:IF I$(I)=A$ THEN I$(I)="":GOTO 1090  
1080 NEXT:GOSUB 2690 :IF I1=0 THEN ER$="You can't do that yet.":GOSUB 1510:RETURN ELSE GOSUB 2780 
1090 IF M$<>"" PRINT M$
1100 RETURN
1110 IF LEFT$(TD$,1)<>"#" GOTO 1220 
1120 I4=INSTR(TD$,","):I5=INSTR(I4+1,TD$,","):I6=INSTR(TD$,">"):I7=INSTR(TD$,":")
1130 M$="":IF I7=0 OR I7<I6 THEN I7=LEN(TD$)+1 ELSE M$=RIGHT$(TD$,LEN(TD$)-I7)
1140 A$=MID$(TD$,I6+1,I7-I6-1)
1150 II=-1:FOR I=0 TO IL:IF I$(I)=A$ THEN II=I:GOTO 1170  
1160 NEXT:GOSUB 2690 :IF I1=0 THEN ER$="You can't do that yet.":GOSUB 1510:RETURN
1170 X1=X:Y1=Y:F1=F:X=VAL(MID$(TD$,2,I4-2)):Y=VAL(MID$(TD$,I4+1,I5-I4-1)):F=VAL(MID$(TD$,I5+1,I6-I5-1))
1180 CN$=A$:GOSUB 2750 :X=X1:Y=Y1:F=F1:IF CN$="" THEN ER$="That room is too crowded.":GOSUB 1510:RETURN
1190 IF II<>-1 THEN I$(II)="" ELSE GOSUB 2690 :GOSUB 2780 
1200 IF M$<>"" PRINT M$
1210 RETURN
1220 IF LEFT$(TD$,1)<>"[" GOTO 1430 
1230 IF TD$="[" THEN ER$="Nothing happens.":GOSUB 1510:RETURN
1240 M$="":IN=INSTR(TD$,":"):IF IN=0 THEN IN=LEN(TD$)+1 ELSE M$=RIGHT$(TD$,LEN(TD$)-IN)
1250 IF MID$(TD$,2,1)="$" THEN I1$=MID$(TD$,3,IN-3):GOTO 1340 
1260 I1$=MID$(TD$,2,IN-2)
1270 IF INSTR(I1$,"N")<>0 AND Y<>0 THEN MID$(PD$(F,X,Y),1,1)="N"
1280 IF INSTR(I1$,"S")<>0 AND Y<>DY THEN MID$(PD$(F,X,Y),2,1)="S"
1290 IF INSTR(I1$,"E")<>0 AND X<>DX THEN MID$(PD$(F,X,Y),3,1)="E"
1300 IF INSTR(I1$,"W")<>0 AND X<>0 THEN MID$(PD$(F,X,Y),4,1)="W"
1310 IF INSTR(I1$,"U")<>0 AND F<>DF THEN MID$(PD$(F,X,Y),5,1)="U"
1320 IF INSTR(I1$,"D")<>0 AND F<>0 THEN MID$(PD$(F,X,Y),6,1)="D"
1330 GOTO 1400 
1340 IF INSTR(I1$,"N")<>0 THEN MID$(PD$(F,X,Y),1,1)="."
1350 IF INSTR(I1$,"S")<>0 THEN MID$(PD$(F,X,Y),2,1)="."
1360 IF INSTR(I1$,"E")<>0 THEN MID$(PD$(F,X,Y),3,1)="."
1370 IF INSTR(I1$,"W")<>0 THEN MID$(PD$(F,X,Y),4,1)="."
1380 IF INSTR(I1$,"U")<>0 THEN MID$(PD$(F,X,Y),5,1)="."
1390 IF INSTR(I1$,"D")<>0 THEN MID$(PD$(F,X,Y),6,1)="."
1400 TD$="["
1410 IF M$<>"" PRINT M$
1420 RETURN
1430 IF LEFT$(TD$,1)<>"*" GOTO 1500
1440 IF PT$(F,X,Y)="" RETURN
1450 M$="":IN=INSTR(TD$,":"):IF IN<>0 THEN M$=RIGHT$(TD$,LEN(TD$)-IN)
1460 IF MID$(TD$,2,1)="+" THEN IF RIGHT$(PT$(F,X,Y),1)<>"+" THEN PT$(F,X,Y)=PT$(F,X,Y)+"+":GOTO 1480 ELSE 1480
1470 IF RIGHT$(PT$(F,X,Y),1)="+" THEN PT$(F,X,Y)=LEFT$(PT$(F,X,Y),LEN(PT$(F,X,Y))-1)
1480 IF M$<>"" PRINT M$
1490 RETURN
1500 PRINT:PRINT TD$:GOTO 2990 
1510 IF CI=1 PRINT ER$
1520 RETURN
1530 IF D=0 GOTO 1670
1540 IF PT$(F,X,Y)="" THEN DE=1:BL=1:GOTO 1580
1550 P=INSTR(PT$(F,X,Y),","):IF RIGHT$(PT$(F,X,Y),1)="+" THEN PT$=MID$(PT$(F,X,Y),P+1,LEN(PT$(F,X,Y))-P-1) ELSE PT$=LEFT$(PT$(F,X,Y),P-1)
1560 IF INSTR(PT$,"R")<>0 THEN DE=1 ELSE DE=0
1570 IF INSTR(PT$,"C")<>0 THEN BL=1 ELSE BL=0
1580 IF (DE$(F,X,Y)<>"-" AND DE=1) OR (BL$(F,X,Y)<>"" AND BL=1) OR CN$(F,X,Y)<>"" PRINT
1590 IF DE$(F,X,Y)<>"-" AND DE=1 PRINT DE$(F,X,Y)
1600 IF BL$(F,X,Y)<>"" AND BL=1 PRINT BL$(F,X,Y)
1610 IF CN$(F,X,Y)="" GOTO 1670 
1620 IN=LEN(CN$(F,X,Y))+1
1630 I2=0
1640 I1=I2+1:I2=INSTR(I1,CN$(F,X,Y),","):IF I2=0 THEN I2=IN
1650 PRINT"There is a ";MID$(CN$(F,X,Y),I1,I2-I1);" here."
1660 IF I2<>IN GOTO 1640 
1670 D=0:LINE INPUT":";C$
1680 IF LEFT$(C$,1)=" " THEN C$=RIGHT$(C$,LEN(C$)-1):GOTO 1680 
1690 IF RIGHT$(C$,1)=" " THEN C$=LEFT$(C$,LEN(C$)-1):GOTO 1690 
1700 IF C$="" GOTO 1670 
1710 FOR T=1 TO LEN(C$):IF ASC(MID$(C$,T,1))>95 AND ASC(MID$(C$,T,1))<128 THEN MID$(C$,T,1)=CHR$(ASC(MID$(C$,T,1))-32)
1720 NEXT
1730 IF NI$(F,X,Y)="" OR LEFT$(NI$(F,X,Y),1)="+" OR LEFT$(NI$(F,X,Y),1)="-" OR TD$(F,X,Y)="" GOTO 1820 
1740 IN=INSTR(NI$(F,X,Y),":"):IF IN=0 THEN NI$="":IN=LEN(NI$(F,X,Y))+1 ELSE NI$=RIGHT$(NI$(F,X,Y),LEN(NI$(F,X,Y))-IN)
1750 I2=0
1760 I1=I2+1:I2=INSTR(I1,NI$(F,X,Y),","):IF I2=0 THEN I2=IN
1770 IF C$<>MID$(NI$(F,X,Y),I1,I2-I1) THEN IF I2=IN GOTO 1820 ELSE GOTO 1760 
1780 IF NI$="" TS=0:GOTO 1810 
1790 FOR I=0 TO IL:IF I$(I)=NI$ TS=0:GOTO 1810 
1800 NEXT:PRINT"You can't do that yet.":GOTO 1670 
1810 CI=1:GOSUB 2830:GOTO 650
1820 S=0
1830 IF SU$(S)="" GOTO 2010 
1840 IF INSTR(SU$(S),","+C$+",")=0 THEN S=S+1:GOTO 1830 
1850 FOR U=0 TO 50
1860 IF UC$(U)="" GOTO 2010 
1870 IF UC$="-" GOTO 1980 
1880 IN=INSTR(UC$(U),":"):IF IN=0 THEN UC$="":IN=LEN(UC$(U))+1 ELSE UC$=RIGHT$(UC$(U),LEN(UC$(U))-IN)
1890 I2=0
1900 I1=I2+1:I2=INSTR(I1,UC$(U),","):IF I2=0 THEN I2=IN
1910 IF C$<>MID$(UC$(U),I1,I2-I1) THEN IF I2=IN GOTO 1980 ELSE GOTO 1900 
1920 IF UD$(U)="" GOTO 1950
1930 I1=INSTR(UD$(U),","):I2=INSTR(I1+1,UD$(U),",")
1940 IF X<>VAL(LEFT$(UD$(U),I1-1)) OR Y<>VAL(MID$(UD$(U),I1+1,I2-I1-1)) OR F<>VAL(RIGHT$(UD$(U),LEN(UD$(U))-I2)) GOTO 1980
1950 IF UC$="" THEN TD$=UT$(U):TS=1:GOTO 2000 
1960 FOR I=0 TO IL:IF I$(I)=UC$ THEN TD$=UT$(U):TS=1:GOTO 2000 
1970 NEXT:PRINT"You can't do that yet.":GOTO 1670 
1980 NEXT
1990 PRINT"You can't do that here.":GOTO 1670
2000 CI=1:GOSUB 2830:GOTO 650
2010 IF LEFT$(C$,3)="GO " THEN C$=RIGHT$(C$,LEN(C$)-3)
2020 IF C$="NORTH" THEN C$="N"
2030 IF C$="SOUTH" THEN C$="S"
2040 IF C$="EAST" THEN C$="E"
2050 IF C$="WEST" THEN C$="W"
2060 IF C$="UP" THEN C$="U"
2070 IF C$="DOWN" THEN C$="D"
2080 IF LEFT$(C$,5)="TAKE " THEN C$="GET "+RIGHT$(C$,LEN(C$)-5)
2090 IF LEFT$(C$,6)="THROW " THEN C$="DROP "+RIGHT$(C$,LEN(C$)-6)
2100 IF C$<>"N" AND C$<>"S" AND C$<>"E" AND C$<>"W" AND C$<>"U" AND C$<>"D" GOTO 2180 
2110 IF C$="N" AND MID$(PD$(F,X,Y),1,1)="N" THEN Y=Y-1:GOTO 630
2120 IF C$="S" AND MID$(PD$(F,X,Y),2,1)="S" THEN Y=Y+1:GOTO 630
2130 IF C$="E" AND MID$(PD$(F,X,Y),3,1)="E" THEN X=X+1:GOTO 630
2140 IF C$="W" AND MID$(PD$(F,X,Y),4,1)="W" THEN X=X-1:GOTO 630
2150 IF C$="U" AND MID$(PD$(F,X,Y),5,1)="U" THEN F=F+1:GOTO 630
2160 IF C$="D" AND MID$(PD$(F,X,Y),6,1)="D" THEN F=F-1:GOTO 630
2170 PRINT"You can't go that way.":GOTO 1670 
2180 IF LEFT$(C$,4)<>"GET " GOTO 2260 
2190 A$=RIGHT$(C$,LEN(C$)-4)
2200 GOSUB 2690 
2210 IF I1<>0 GOTO 2230 
2220 PRINT"I see no ";A$;" here that you can pick up.":GOTO 1670 
2230 FOR I=0 TO IL:IF I$(I)="" THEN I$(I)=CN$:GOSUB 2780 :PRINT"Ok":GOTO 650
2240 NEXT
2250 PRINT"Your hands are full - you can't carry any more.":GOTO 1670 
2260 IF LEFT$(C$,5)<>"DROP " GOTO 2330
2270 A$=RIGHT$(C$,LEN(C$)-5):AS$=" "+A$+" "
2280 FOR I=0 TO IL
2290 I$=" "+I$(I)+" ":IF INSTR(I$,AS$)<>0 AND (INSTR(I$,AS$)=1 OR INSTR(I$,AS$)=LEN(I$)-LEN(AS$)+1) GOTO 2320 
2300 NEXT
2310 PRINT"You are not carrying a ";A$;".":GOTO 1670 
2320 CN$=I$(I):GOSUB 2750 :IF CN$="" PRINT"There is no place to put it.":GOTO 1670 ELSE I$(I)="":PRINT"Ok":GOTO 650
2330 IF INSTR(C$,"INVENT")=0 GOTO 2420 
2340 FOR I=0 TO IL:IF I$(I)<>"" GOTO 2370 
2350 NEXT
2360 PRINT"You are not carrying anything.":GOTO 1670 
2370 PRINT"You are currently holding the following:"
2380 PRINT
2390 FOR I=0 TO IL:IF I$(I)<>"" PRINT"- ";I$(I);" -"
2400 NEXT
2410 PRINT:GOTO 1670 
2420 IF C$<>"SUSPEND" GOTO 2590
2430 PRINT"Which suspend file? (0-9) --> ";
2440 SF$=INKEY$:IF SF$="" GOTO 2440
2450 IF ASC(SF$)<48 OR ASC(SF$)>57 GOTO 2440
2460 PRINT SF$
2470 OPEN"O",1,FI$+"/S"+SF$
2480 PRINT#1,F,X,Y,L
2490 FOR I=0 TO IL:PRINT#1,I$(I):NEXT
2500 FOR F1=0 TO DF:FOR Y1=0 TO DY:FOR X1=0 TO DX
2510 IF RIGHT$(PT$(F1,X1,Y1),1)="+" PRINT#1,"+" ELSE PRINT#1,""
2520 PRINT#1,CN$(F1,X1,Y1)
2530 IF TD$(F1,X1,Y1)="+" THEN PRINT#1,"+":GOTO 2550 
2540 IF TD$(F1,X1,Y1)="[" THEN PRINT#1,PD$(F1,X1,Y1) ELSE PRINT#1,""
2550 NEXT:NEXT:NEXT:FOR U=0 TO 50:IF UC$(U)="" GOTO 2580 
2560 IF UT$(U)="+" PRINT#1,"+" ELSE PRINT#1,""
2570 NEXT
2580 CLOSE:PRINT"Ok":GOTO 1670
2590 IF C$="LOOK" GOTO 1540
2600 IF C$="QUIT" OR C$="STOP" PRINT"Ok":GOTO 2990 
2610 IF C$<>"HELP" GOTO 2660 
2620 PRINT:PRINT"These are some of the commands you may use:":PRINT:PRINT"NORTH or N      (go north)":PRINT"SOUTH or S      (go south)":PRINT"EAST or E       (go east)":PRINT"WEST or W       (go west)"
2630 PRINT"UP or U         (go up)":PRINT"DOWN or D       (go down)"
2640 PRINT"INVENT          (see your inventory - what you are carrying)":PRINT"LOOK            (see where you are)":PRINT"SUSPEND         (save game to disk)":PRINT"QUIT or STOP    (quit game)"
2650 GOTO 1670 
2660 S=0
2670 IF SL$(S)="" PRINT"I don't understand.":GOTO 1670 
2680 IF INSTR(SL$(S),","+C$+",")=0 THEN S=S+1:GOTO 2670 ELSE PRINT"You can't do that here.":GOTO 1670 
2690 AS$=" "+A$+" ":IN=LEN(CN$(F,X,Y))+1
2700 I2=0
2710 I1=I2+1:I2=INSTR(I1,CN$(F,X,Y),","):IF I2=0 THEN I2=IN
2720 CN$=MID$(CN$(F,X,Y),I1,I2-I1):CS$=" "+CN$+" ":IF INSTR(CS$,AS$)<>0 AND (INSTR(CS$,AS$)=1 OR INSTR(CS$,AS$)=LEN(CS$)-LEN(AS$)+1) GOTO 2740 
2730 IF I2=IN THEN I1=0 ELSE GOTO 2710 
2740 RETURN
2750 IF LEN(CN$(F,X,Y))+LEN(CN$)>254 THEN CN$="":GOTO 2770 
2760 IF CN$(F,X,Y)="" THEN CN$(F,X,Y)=CN$ ELSE CN$(F,X,Y)=CN$(F,X,Y)+","+CN$
2770 RETURN
2780 IF I1=1 AND I2=IN THEN CN$(F,X,Y)="":GOTO 2820 
2790 IF I1=1 THEN CN$(F,X,Y)=RIGHT$(CN$(F,X,Y),LEN(CN$(F,X,Y))-I2):GOTO 2820 
2800 IF I2=IN THEN CN$(F,X,Y)=LEFT$(CN$(F,X,Y),I1-2):GOTO 2820 
2810 CN$(F,X,Y)=LEFT$(CN$(F,X,Y),I1-1)+RIGHT$(CN$(F,X,Y),LEN(CN$(F,X,Y))-I2)
2820 RETURN
2830 TR$="":IF TS=1 THEN TD$=UT$(U) ELSE TD$=TD$(F,X,Y)
2840 I9=LEN(TD$)+1:I8=0
2850 IF TS=1 THEN TD$=UT$(U) ELSE TD$=TD$(F,X,Y)
2860 I0=I8+1:I8=INSTR(I0,TD$,";"):IF I8=0 THEN I8=I9
2870 TD$=MID$(TD$,I0,I8-I0):GOSUB 700  
2880 IF TD$="+" OR TD$="[" THEN TR$=TD$
2890 IF I8<>I9 GOTO 2850 
2900 IF TR$<>"+" AND TR$<>"[" GOTO 2920 
2910 IF TS=1 THEN UT$(U)=TR$ ELSE TD$(F,X,Y)=TR$
2920 RETURN
2930 CR$="Dpqzsjhiu!)D*!2:93!cz!Kpf!Qfufstpo!pg!Qfufstpo!Dpnqvufs!Tfswjdft":FOR X=1 TO LEN(CR$):MID$(CR$,X,1)=CHR$(ASC(MID$(CR$,X,1))-1):NEXT:PRINT CR$:END
2940 IF ERR/2=53 AND ERL=120 CLOSE:PRINT CHR$(23):PRINT"Adventure not found":RESUME 90   
2950 IF ERR/2=53 AND ERL=520 CLOSE:PRINT"*** Game not previously suspended ***":RESUME 450
2960 IF ERR/2=61 CLOSE:KILL FI$+"/S"+SF$:PRINT"Space not available - use different disk.":RESUME 1670 
2970 IF ERR/2=64 CLOSE:PRINT CHR$(23):PRINT"Adventure name not valid":RESUME 90
2980 ON ERROR GOTO 0    
2990 PRINT:CLEAR 50
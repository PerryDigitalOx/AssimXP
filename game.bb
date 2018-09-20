;***********************************************************************************
; This code can be used for any use, private, public, commercial, educational, etc.
;
; I, Perry Robertson, created the original game in DOS in 1992.
; This version I created to learn Blitz Basic.  This is my 1st project in Blitz.
; The reason why the code is not the best it could be is I tried to stick to the 
; original code.  I cut and paste function by function converting from C to Blitz.
; Restrictions I had to work around were having a structure with 2d array inside,
; which blitz can't do.
; The AI had to be redone from scratch.
;
;	If you use, enjoy, learn, whatever, from this game/code, please drop me a line
;   Email: perry@digitalox.ca
;
; Time invested: about 1 week.
;
;***********************************************************************************


AppTitle("Assimilation XP")

Print "ASSIMILATION (C) 1992,2003,V1.0"
Print "by Perry Robertson"
Print
Print "This version is about 99% complete"
Print

Include "start.bb"

Dim boardarr%(7,7)	;global board array
Global boardname$	;global board name

Dim aix(15)
Dim aiy(15)

For i=0 To 15
	Read aix(i)
Next

For i=0 To 15
	Read aiy(i)
Next


;boards in main menu only
Type boardType
	Field e[64]
	Field title$
	Field filename$
End Type

;used in main menu for 2 stage buttons
Type btnType
	Field x
	Field y
	Field frame
	Field frame2
End Type

;where to start drawing tiles in top left
Const TILE_START_X = 20
Const TILE_START_Y = 30

;size of tiles to offset drawing
Const TILE_WIDTH = 52
Const TILE_HEIGHT = 52

Const MAXBOMBS = 5

;these correspond to BMP placement, not actual board values
Const TILE_BLANK = 0
Const TILE_MOVE = 1
Const TILE_BLUE = 2
Const TILE_RED = 3
Const TILE_BLUESEL = 4
Const TILE_REDSEL = 5
Const TILE_INVALID = 6
Const TILE_BOMB = 7
Const TILE_EXPLODE = 8
Const TILE_GRAVE = 9
Const TILE_SWIRL = 10

;these correspond to actual board values
Const EMPTY   = 0   ;an empty board square
Const PLAYERX = 1   ;a piece owned by player "X"
Const PLAYERO = 2   ;a piece owned by player "O"
Const SETBOMB = 5
Const FLUSHED = 6
Const TOMBED  = 7
Const BOMBED  = 8
Const NA      = 9


;these correspond to menu ids
Const EXITTODOS = -1
Const STARTNEWGAME = 1
Const RESUMEGAME = 2
Const INFO = 3
Const CREDITS = 4
Const STOPGAME = 5

;player types
Const COMPUTER = 0
Const HUMAN = 1


Const DELAY1 = 150


Global killers = True
Global bombs_remain = 0

Global playerXtype,playerOtype,playerXdepth,playerOdepth
Global gameinprogress = False
Global show_avail = True

Global topscore
Global capscore
Global bestmove_from_x
Global bestmove_from_y
Global bestmove_to_x
Global bestmove_to_y

Global shotsnd1 = LoadSound("audio\shot1.wav")
Global shotsnd2 = LoadSound("audio\shot2.wav")

Global tiles = LoadAnimImage("graphics\tiles.png",TILE_WIDTH,TILE_HEIGHT,0,11)
Global digits = LoadAnimImage("graphics\digits.png",32,32,0,10)
Global tiles_sm = LoadAnimImage("graphics\tiles_sm.png",16,16,0,11)
Global mouse = LoadImage("graphics\mouse.png")
MaskImage mouse,255,0,255


Global invalidsnd = LoadSound("audio\invalid.wav")
Global menubtnsnd = LoadSound("audio\menu.wav")
Global clickonsnd = LoadSound("audio\clickon.wav")
Global movesnd = LoadSound("audio\move.wav")
Global jumpsnd = LoadSound("audio\jump.wav")
Global capturesndX = LoadSound("audio\capture1.wav")
Global capturesndO = LoadSound("audio\capture2.wav")
Global flushsnd = LoadSound("audio\flush.wav")

Global menubg = LoadImage("graphics\menubg.jpg")
Global click = LoadSound("audio\click.wav")
Global clickarrow = LoadSound("audio\click2.wav")
Global arrow = LoadImage("graphics\arrow.png")
Global btnimg = LoadAnimImage("graphics\menu_btn_spr.png",236,32,0,12)
MaskImage btnimg,255,0,255
MaskImage arrow,255,0,255

Global checksimg = LoadAnimImage("graphics\checks.png",15,14,0,2)
MaskImage checksimg,255,0,255

Global explodesnd = LoadSound("audio\explode.wav")
Global explodeimg = LoadAnimImage("graphics\explode.png",52,52,0,10)
MaskImage explodeimg,255,0,255
Global tombstoneimg = LoadAnimImage("graphics\tombstone.png",52,52,0,11)
MaskImage tombstoneimg,255,0,255
Global flushimg = LoadAnimImage("graphics\flush.png", 52,52,0,16)
MaskImage flushimg,255,0,255

Global bgimg, menubtnimg

intro()

SeedRnd(MilliSecs())

HidePointer

menustate = EXITTODOS
quitapp = False

While quitapp = False

	menustate = mainmenu()

	If menustate = STARTNEWGAME

		hintsused = 0
		board_level = cell
		player = PLAYERX
		curplayertype = playerXtype
		
		board_addkillers()
		
		startsnd = LoadSound("audio\newgame.wav")
		chan = PlaySound(startsnd)

	;	rndcnt = Rand(1,10)
	;	If(master_snd_sys&&rndcnt>=7)
	;	{
	;		While(sounds_in_queue!=0)
	;           v_delay( 15 )
	;		PlaySound(rafdata[STARTSND], raflength[STARTSND])
	;	}

		player = do_game(player)

	Else If menustate = INFO
		img = LoadImage("graphics\info1.jpg")
		DrawBlock img,0,0
		Flip
		img2= LoadImage("graphics\info2.jpg")
		FlushMouse
		WaitMouse
		DrawBlock img2,0,0
		Flip
		FlushMouse
		WaitMouse
		FreeImage(img)
		FreeImage(img2)
		menustate = 0
	Else If menustate = CREDITS
		img = LoadImage("graphics\credits.jpg")
		DrawBlock img,0,0
		Flip
		FlushMouse
		WaitMouse
		FreeImage(img)
	Else If menustate = STOPGAME
		gameinprogress = False
	Else If menustate = RESUMEGAME
		player = do_game(player)
	Else If menustate = EXITTODOS
		alarm = LoadSound("audio\alarm.wav")
		chan = PlaySound(alarm)
		While ChannelPlaying(chan):Wend
		FreeSound(alarm)
		quitapp = True
	EndIf

Wend

extro()

ShowPointer

FreeImage(tiles)
FreeImage(digits)
FreeImage(tiles_sm)
FreeImage(mouse)

FreeImage(arrow)
FreeImage(menubg)
FreeImage(btnimg)

FreeImage(bgimg)
FreeImage(menubtnimg)
FreeImage(checksimg)

FreeImage(explodeimg)
FreeImage(tombstoneimg)
FreeImage(flushimg)

FreeSound(click)
FreeSound(clickarrow)

FreeSound(invalidsnd)
FreeSound(menubtnsnd)
FreeSound(clickonsnd)
FreeSound(movesnd)
FreeSound(jumpsnd)
FreeSound(capturesndX)
FreeSound(capturesndO)
FreeSound(explodesnd)
FreeSound(shotsnd1)
FreeSound(shotsnd2)
FreeSound(startsnd)
FreeSound(flushsnd)

End


Function intro ()
	title3 = LoadImage("graphics\company.jpg")
	title2 = LoadImage("graphics\presents.jpg")
	title = LoadImage("graphics\asstitle.jpg")
	introsnd = LoadSound("audio\intro.wav")
	Cls
	DrawBlock title3,0,0
	Flip
	Delay(1500)
	Cls
	DrawBlock title2,0,0
	Flip
	Delay(1500)
	Cls
	DrawBlock title,0,0
	Flip
	chan = PlaySound(introsnd)
	Delay(2000)
	While ChannelPlaying(chan):Wend

	FreeImage title
	FreeImage title2
	FreeImage title3
	FreeSound introsnd
	Cls
	Flip

End Function



Function mainmenu()

	;draw menu items
	newbtn.btnType = New btnType
	stopbtn.btnType = New btnType
	resumebtn.btnType = New btnType
	howtoplay.btnType = New btnType
	creditsbtn.btnType = New btnType
	quit.btnType = New btnType


	; these are for the arrow positioning
	arr_x = 40
	arr_y1 = 130
	arr_y2 = 256
	arr_yinc = 16
	playerXtype = HUMAN
	playerOtype = COMPUTER
	playerXdepth = 0
	playerOdepth = 1
	x=202
	y=150

	;setup normal state buttons, alternate state is normal state\frame+1
	If gameinprogress = True
		stopbtn\x = x
		stopbtn\y = y
		stopbtn\frame = 10
		stopbtn\frame2 = 11
		y=y+36
		resumebtn\x = x
		resumebtn\y = y
		resumebtn\frame = 2
		resumebtn\frame2 = 3
		y=y+36
	Else
		newbtn\x = x
		newbtn\y = y
		newbtn\frame = 0
		newbtn\frame2 = 1
		y=y+36
	EndIf

	howtoplay\x = x
	howtoplay\y = y
	howtoplay\frame = 4
	howtoplay\frame2 = 5
	y=y+36
	f=f+2
	creditsbtn\x = x
	creditsbtn\y = y
	creditsbtn\frame = 6
	creditsbtn\frame2 = 7
	y=y+36
	f=f+2
	quit\x = x
	quit\y = y
	quit\frame = 8
	quit\frame2 = 9

	FlushKeys

	If Not gameinprogress Then
		;read boards dir
		folder$ = "boards"
		mydir = ReadDir(folder$)
		filecount% = 0
		Repeat
			file$ = NextFile$(mydir)
			If file$="" Then Exit
			If FileType(folder$+"\"+file$)=1 Then
				filecount% = filecount% + 1
				board.boardType = New boardType
				board\filename$ = file$
				board\title = Upper(Left(file$,Len(file$)-4))
				filein = ReadFile(folder$+"\"+file$)
				For i=0 To 7
					theline$ = ReadLine(filein)
	;				DebugLog theline$
					For j=0 To 7
						board\e[i*8+j] = Asc(Mid(theline$,j+1,1))
	;					DebugLog Chr(board\e[i*8+j])
					Next
				Next
				CloseFile(filein)
	;			DebugLog theline$
			EndIf
		Forever
		CloseDir mydir
	EndIf

;	For myboard.boardType = Each boardType
;		Cls
;		Text 0,y,"Name: " + myboard\name$
;		y=y+20
;		y2=100
;		For i=0 To 7
;			x=0
;			For j=0 To 7
;				Text 400+x,y2,Chr(myboard\e[i*8+j])x
;				x=x+20
;			Next
;			y2=y2+20
;		Next
;		flip
;		WaitKey
;	Next


	brdPTR.boardType = First boardType
	update = True
	menustate = 0
	Repeat
		ch = GetKey()
		mbut = GetMouse()
		mx = MouseX()
		my = MouseY()
		mz = MouseZ()

;		If update = True Then	
			DrawBlock menubg,0,0

			If gameinprogress = True
				DrawImage btnimg, stopbtn\x, stopbtn\y, stopbtn\frame
				DrawImage btnimg, resumebtn\x, resumebtn\y, resumebtn\frame
			Else
				DrawImage btnimg, newbtn\x, newbtn\y, newbtn\frame
			EndIf

			DrawImage btnimg, howtoplay\x, howtoplay\y, howtoplay\frame
			DrawImage btnimg, creditsbtn\x, creditsbtn\y, creditsbtn\frame
			DrawImage btnimg, quit\x, quit\y, quit\frame

			If killers = True Then
				DrawImage checksimg, 252,440,0
			Else
				DrawImage checksimg, 252,440,1
			EndIf

			If playerXtype = HUMAN
				DrawImage arrow, arr_x, arr_y1
			Else
				DrawImage arrow, arr_x, arr_y1 + (arr_yinc * (playerXdepth))
			EndIf
		
			If playerOtype = HUMAN 
				DrawImage arrow, arr_x, arr_y2
			Else
				DrawImage arrow, arr_x, arr_y2 + (arr_yinc * (playerOdepth))
			EndIf

			If Not gameinprogress Then
				;if not in game, use from list
				boardname$ = brdPtr\title$
			EndIf
			Text 568,202,boardname$,True

			;show list of boards with hilight
			xpos=0
			For x=0 To 7
				ypos=280
				For y=0 To 7
;					Text 520+x,y,Chr(brdPtr\e[i*8+j])
		
					If Not gameinprogress Then
						frame$ = Chr(brdPtr\e[y*8+x])

						If frame$="#" Then
							f = TILE_BLANK
							g = EMPTY
						ElseIf frame$="X" Then
							f = TILE_BLUE
							g = PLAYERX
						ElseIf frame$="O" Then
							f = TILE_RED
							g = PLAYERO
						ElseIf frame$="*" Then
							f = TILE_INVALID
							g = NA
						Else
							f = TILE_BLANK
							g = EMPTY
						EndIf
	
						boardarr%(x,y) = g	;update global board array
					Else
						f = boardarr%(x,y)
						If f=EMPTY Then
							f = TILE_BLANK
						ElseIf f=PLAYERX Then
							f = TILE_BLUE
						ElseIf f=PLAYERO Then
							f = TILE_RED
						ElseIf f=NA Then
							f = TILE_INVALID
						ElseIf f=BOMBED Then
							f = TILE_EXPLODE
						ElseIf f=TOMBED Then
							f = TILE_GRAVE
						ElseIf f=FLUSHED Then
							f = TILE_SWIRL
						Else
							f = TILE_BLANK
						EndIf
					EndIf
					
					DrawBlock tiles_sm, 503+xpos, ypos, f
					ypos = ypos + 16
				Next
				xpos = xpos + 16
			Next

			update = False
;			flip
;		EndIf ;if update=true

		If gameinprogress = True Then

			If (ch = Asc("s") And (gameinprogress = True)) Or (mbut>0 And (mx>=stopbtn\x) And (mx<=stopbtn\x+320) And (my>=stopbtn\y) And (my<=stopbtn\y+32))
				menustate = STOPGAME
				DrawImage btnimg,stopbtn\x,stopbtn\y,stopbtn\frame2
				Flip
				chan = PlaySound(click)
				Exit
			Else If (ch = Asc("r") And (gameinprogress = True)) Or (mbut>0 And (mx>=resumebtn\x) And (mx<=resumebtn\x+320) And (my>=resumebtn\y) And (my<=resumebtn\y+32))
				menustate = RESUMEGAME
				DrawImage btnimg,resumebtn\x,resumebtn\y,resumebtn\frame2
				Flip
				chan = PlaySound(click)
				Exit
			EndIf

		Else

			If ch = Asc("n") Or (mbut>0 And (mx>=newbtn\x) And (mx<=newbtn\x+320) And (my>=newbtn\y) And (my<=newbtn\y+32))
				menustate = STARTNEWGAME
				DrawImage btnimg,newbtn\x,newbtn\y,newbtn\frame2
				Flip
				Exit
			EndIf

		EndIf ;if gameinprogress=true


		If ch=Asc("h") Or (mbut>0 And (mx>=howtoplay\x) And (mx<=howtoplay\x+320) And (my>=howtoplay\y) And (my<=howtoplay\y+32))
			menustate = INFO
			DrawImage btnimg,howtoplay\x,howtoplay\y,howtoplay\frame2
			Flip
			chan = PlaySound(click)
			Exit
		Else If ch = Asc("c") Or (mbut>0 And (mx>=creditsbtn\x) And (mx<=creditsbtn\x+320) And (my>=creditsbtn\y) And (my<=creditsbtn\y+32))
			menustate = CREDITS
			DrawImage btnimg,creditsbtn\x,creditsbtn\y,creditsbtn\frame2
			Flip
			chan = PlaySound(click)
			Exit
		Else If ch = Asc("x") Or (mbut>0 And (mx>=quit\x) And (mx<=quit\x+320) And (my>=quit\y) And (my<=quit\y+32))
			menustate = EXITTODOS
			DrawImage btnimg,quit\x,quit\y,quit\frame2
			Flip
			chan = PlaySound(click)
			Exit
		EndIf

		;board selection arrows
		If Not gameinprogress Then
			If ch = Asc("k") Or (mbut=1 And mx>272 And mx<382 And my>435 And my<460)
				killers = Not killers
				chan = PlaySound(click)
			EndIf

			If mbut>0 And mx>=556 And mx<=556+26 
				If my>=156 And my<=156+33 	;up arrow
					PlaySound(clickarrow)
					brdPtr = Before(brdPtr)
					If brdPtr = Null Then brdPtr = First boardType
					update = True
				ElseIf my>=230 And my<=230+33	;down arrow
					PlaySound(clickarrow)
					brdPtr = After(brdPtr)
					If brdPtr = Null Then brdPtr = Last boardType
					update = True
				EndIf
			EndIf

			;player levels
			If mbut>0 And mx>=58 And mx<=58+72
				If my>=arr_y1 And my<=arr_y1+arr_yinc
					playerXdepth = 0
					playerXtype = HUMAN
					update = True
				Else If my>=arr_y1+arr_yinc And my<= arr_y1+(arr_yinc*2)
					playerXdepth = 1
					playerXtype = COMPUTER
					update = True
				Else If my>=arr_y1+(arr_yinc*2) And my<= arr_y1+(arr_yinc*3)
					playerXdepth = 2
					playerXtype = COMPUTER
					update = True
				Else If my>=arr_y1+(arr_yinc*3) And my<= arr_y1+(arr_yinc*4)
					playerXdepth = 3
					playerXtype = COMPUTER
					update = True
				EndIf

				If my>=arr_y2 And my<=arr_y2+arr_yinc
					playerOdepth = 0
					playerOtype = HUMAN
					update = True
				Else If my>=arr_y2+arr_yinc And my<= arr_y2+(arr_yinc*2)
					playerOdepth = 1
					playerOtype = COMPUTER
					update = True
				Else If my>=arr_y2+(arr_yinc*2) And my<= arr_y2+(arr_yinc*3)
					playerOdepth = 2
					playerOtype = COMPUTER
					update = True
				Else If my>=arr_y2+(arr_yinc*3) And my<= arr_y2+(arr_yinc*4)
					playerOdepth = 3
					playerOtype = COMPUTER
					update = True
				EndIf

			EndIf
		EndIf

		If update = True PlaySound(clickarrow)

		draw_mouse()
		Flip
	Forever

	While ChannelPlaying(chan):Wend

	Delete(newbtn)
	Delete(stopbtn)
	Delete(resumebtn)
	Delete(howtoplay)
	Delete(creditsbtn)
	Delete(quit)
	Delete Each boardType

	Return menustate

End Function


Function extro()

	img = LoadImage("graphics\extro1.jpg")
	Cls
	DrawBlock img,0,0
	Flip
	Delay(2000)
	WaitMouse
	FreeImage(img)

End Function


Function draw_board()

	If bgimg = 0 Then
		bgimg = LoadImage("graphics\bg1.jpg")
	EndIf

	DrawBlock bgimg,0,0

	;show list of boards with hilight
	Text 222,10,boardname$,True
	xpos = TILE_START_X
	For x=0 To 7
		ypos = TILE_START_Y
		For y=0 To 7
			t = boardarr(x,y)
			Select t
			Case NA
				f=TILE_INVALID
			Case PLAYERX
				f=TILE_BLUE
			Case PLAYERO
				f=TILE_RED
			Case EMPTY
				f=TILE_BLANK
			Case BOMBED
				f=TILE_EXPLODE
			Case TOMBED
				f=TILE_GRAVE
			Case FLUSHED
				f=TILE_SWIRL
			Default
				f=TILE_BLANK
			End Select

;			Case SETBOMB		;this will show the bombs, cheating
;				f=TILE_BOMB

			DrawBlock tiles, xpos, ypos, f
			ypos = ypos + TILE_HEIGHT
		Next
		xpos = xpos + TILE_WIDTH
	Next

	draw_scoreboard()

End Function


Function draw_scoreboard()
	playerXpieces = 0
	playerOpieces = 0
	boardtotal = 64

	For x=0 To 7
		For y=0 To 7
			Select boardarr(x,y)
				Case PLAYERX
					playerXpieces = playerXpieces + 1
				Case PLAYERO
					playerOpieces = playerOpieces + 1
				Case NA
					boardtotal = boardtotal - 1
			End Select
		Next
	Next

	;show player piece icons
	DrawImage tiles, 468, 34,TILE_BLUE
	DrawImage tiles, 468, 34+TILE_HEIGHT+4, TILE_RED

	;show score using bitmap font
	display_digits(playerXpieces,468+TILE_WIDTH+12,44)
	display_digits(playerOpieces,468+TILE_WIDTH+12,44+TILE_HEIGHT+4)
	
	display_digits(bombs_remain,510,190)

End Function


Function checkpieces(player)

	playerXpieces = 0
	playerOpieces = 0
	boardtotal = 64

	avail_move = False
	For x=0 To 7
		For y=0 To 7
			Select boardarr(x,y)
				Case PLAYERX
					playerXpieces = playerXpieces + 1
				Case PLAYERO
					playerOpieces = playerOpieces + 1
				Case NA
					boardtotal = boardtotal - 1
			End Select

			If boardarr(x,y) = player Then
				For d=0 To 15
					newx = x + aix(d)
					newy = y + aiy(d)
					
					If onboard(newx, newy) Then
						If boardarr(newx,newy) = EMPTY Or boardarr(newx,newy) = SETBOMB Then
							avail_move = True
						EndIf
					EndIf
				Next
			EndIf
		Next
	Next


	If Not avail_move Then
		DebugLog "checkpieces returned NO MOVES LEFT for player "+player
	EndIf


	If ((playerXpieces+playerOpieces)>=boardtotal Or playerXpieces=0 Or playerOpieces=0 Or avail_move=False) Then

		gongsnd = LoadSound("audio\gong.wav")		
		chan=PlaySound(gongsnd)
		
		While ChannelPlaying(chan):Wend
		FreeSound(gongsnd)
		
		x=320-(332/2)
		y=240-(172/2)
		gameovergfx = LoadAnimImage("graphics\gameover.png",332,172,0,3)

		If playerXpieces > playerOpieces Then
			DrawBlock gameovergfx, 154, 154, 1
		Else If playerOpieces > playerXpieces Then
			DrawBlock gameovergfx, 154, 154, 2
		Else
			DrawBlock gameovergfx, 154, 154, 0
		EndIf

		Flip
		FreeImage(gameovergfx)

		;if total assimilation		
        If playerXpieces = 0 Or playerOpieces = 0 Then
			totalassim = LoadSound("audio\totalassim.wav")
			chan=PlaySound(totalassim)
			While ChannelPlaying(chan):Wend
			FreeSound(totalassim)

        EndIf

		laughsnd = LoadSound("audio\laf.wav")
		yeahsnd = LoadSound("audio\yeah1.wav")

		If playerXpieces > playerOpieces And playerXtype=COMPUTER And playerOtype=HUMAN Then
			chan=PlaySound(laughsnd)
		Else If playerOpieces > playerXpieces And playerOtype=COMPUTER And playerXtype=HUMAN Then
			chan=PlaySound(laughsnd)
		Else If playerOtype = HUMAN And playerXtype = HUMAN Or playerOtype = COMPUTER And playerXtype = COMPUTER Then
			chan=PlaySound(yeahsnd)
		Else If playerXpieces = playerOpieces Then
			chan=PlaySound(yeahsnd)
		Else
			chan=PlaySound(yeahsnd)
		EndIf

		While ChannelPlaying(chan):Wend

		FreeSound(laughsnd)
		FreeSound(yeahsnd)

		WaitMouse
		avail_move = False
	EndIf

	Return avail_move

End Function


Function display_digits( number, x, y )

	val$ = Str(number)
	If Len(val$)<2 Then
		val$ = "0" + val$
	EndIf
	digit1 = Int(Left(val$,1))
	digit2 = Int(Right(val$,1))

	DrawBlock digits,x,y,digit1
	DrawBlock digits,x+32,y,digit2

End Function


Function do_game(player)

	gameover = False

	While gameover=False		;quit

		If player = PLAYERX     ;Restore players Type And depth For resume game.
			curplayertype = playerXtype
			curplayerdepth = playerXdepth
			DebugLog "playerX"
		Else
			curplayertype = playerOtype
			curplayerdepth = playerOdepth
			DebugLog "playerO"
		EndIf

		If curplayertype = COMPUTER
			computers_turn(player)
		Else If curplayertype = HUMAN
			i = players_turn(player)
			If i = 2 Then
				gameover = True
			EndIf
		EndIf

		If Not gameover Then    ;If game still going on, switch players.
			player = switch_players(player)
		EndIf

		canmove = checkpieces(player)		;check if game over
		If Not canmove Then
			gameover = True
			gameinprogress = False
;		Else
;			i = GetKey()       ;check in between turns For key presses
;			If i = True
;				PutBlock( 277, 178, sprite[MENUBTN].W, sprite[MENUBTN].H, sp_image[MENUBTN] );
;				If(master_snd_sys) PlaySound( rafdata[HITMAINSND], raflength[HITMAINSND] );
;				gameover = True
;			EndIf					
		EndIf

	Wend ;While gameover = false

	Return player

End Function


Function switch_players( player )

	If player = PLAYERX      ; SWITCH PLAYERS
		player = PLAYERO
		curplayertype = playerOtype
		curplayerdepth = playerOdepth
	Else
		player = PLAYERX
		curplayertype = playerXtype
		curplayerdepth = playerXdepth
	EndIf

	Return player
End Function


Function computers_turn(player)

	DebugLog "Computers Turn:"

	gameinprogress = True
	depth = curplayerdepth

	canmove = checkpieces(player)	;check to see if there is a move available.
	If Not canmove Then    	;If Not give control back To other player.
		Return
	EndIf

	aiplay(player)	;calculate move

	Delay(250)
	draw_board()
	chan=PlaySound(clickonsnd)
	put_avail_moves( player, bestmove_from_x, bestmove_from_y )
	draw_menu(0)
	draw_mouse()
	Flip
	Delay(450)
	While ChannelPlaying(chan):Wend

	If boardarr(bestmove_to_x, bestmove_to_y) = SETBOMB Then
		i = Rand(0,2)
		If i=0 Then
			DebugLog "Bombed"
			explode(bestmove_to_x,bestmove_to_y)
		ElseIf i=1 Then
			DebugLog "Tombed"
			tomb(bestmove_to_x,bestmove_to_y)
		ElseIf i=2 Then
			DebugLog "Flushed"
			flush(player, bestmove_to_x, bestmove_to_y)
		EndIf

	Else If ((Abs(bestmove_from_x-bestmove_to_x)=1 And Abs(bestmove_from_y-bestmove_to_y)=1) Or (Abs(bestmove_from_x-bestmove_to_x)=0 And Abs(bestmove_from_y-bestmove_to_y)=1) Or (Abs(bestmove_from_x-bestmove_to_x)=1 And Abs(bestmove_from_y-bestmove_to_y)=0)) Then
		boardarr(bestmove_to_x,bestmove_to_y) = player
		capture( player, bestmove_to_x, bestmove_to_y )
		chan=PlaySound(movesnd)

	Else If ((Abs(bestmove_from_x-bestmove_to_x) = 2 And Abs(bestmove_from_y-bestmove_to_y) = 2) Or (Abs(bestmove_from_x-bestmove_to_x)= 2 And Abs(bestmove_from_y-bestmove_to_y) = 0) Or (Abs(bestmove_from_x-bestmove_to_x) = 0 And Abs(bestmove_from_y-bestmove_to_y) = 2)) Then
		boardarr(bestmove_from_x, bestmove_from_y) = EMPTY
		boardarr(bestmove_to_x,bestmove_to_y) = player
		capture( player, bestmove_to_x, bestmove_to_y )
		chan=PlaySound(jumpsnd)
	EndIf

	draw_board()
	draw_menu(0)
	draw_mouse()
	Flip

	If chan<>0 Then
		While ChannelPlaying(chan):Wend
	EndIf


End Function



Function players_turn(player)

	DebugLog "Players Turn"
	operation = 0
	gameinprogress = True
	waiting_for_move_to = False
	endofturn = False
	
	; LOOP Until HUMAN PLAYER DOES A VALID MOVE Or BUTTON PRESS
	While operation = 0  ;While no operations have been made...

		mx = MouseX()
		my = MouseY()
		mbut = GetMouse()
		ch = GetKey()

		If ch = Asc("m") Or (mbut=1 And mx>448 And mx<448+174 And my>247 And my<247+31) Then
			DrawBlock menubtnimg,448,247,1
			chan=PlaySound(menubtnsnd)
			operation = 2		;set to 2 for go back to menu.
		EndIf

		;if button on tile ...
		If mbut<>0 And (mx > TILE_START_X) And (mx < TILE_START_X+(8*TILE_WIDTH))  And  (my > TILE_START_Y) And (my < TILE_START_Y+(8*TILE_HEIGHT)) Then

			;if this is for second click for move ...
			If waiting_for_move_to = True Then
				waiting_for_move_to = False

				to_x = calc_x( mx )
				to_y = calc_y( my )
;				DebugLog "To:  "+to_x+":"+to_y + " -> " + boardarr(to_x, to_y)

				;this case should never happen
				;check if click is outside of board tile area
				If to_x > 7 Or to_y > 7 Then
					to_x = 0
					to_y = 0
					operation = 0
					waiting_for_move_to = False
				End If

				;did we click on invalid piece or another of my pieces...
				If boardarr(to_x,to_y) = NA Or boardarr(to_x,to_y) = BOMBED Or boardarr(to_x,to_y) = TOMBED Or boardarr(to_x,to_y) = FLUSHED Or boardarr(to_x,to_y) = (3-player) Then
					waiting_for_move_to = False
					chan = PlaySound(invalidsnd)
				Else If boardarr(to_x,to_y) = player Then
					mbut = 0
					waiting_for_move_to = True	;start checking for 2nd click
					chan = PlaySound(clickonsnd)
				; CHECK If PLAYER MOVED 1 SQUARE AWAY
				Else If boardarr(to_x,to_y) = EMPTY Then
					If ((Abs(from_x-to_x)=1 And Abs(from_y-to_y)=1) Or (Abs(from_x-to_x)=0 And Abs(from_y-to_y)=1) Or (Abs(from_x-to_x)=1 And Abs(from_y-to_y)=0)) Then
						DebugLog "Cloned"
						boardarr(to_x, to_y) = player ;not permanent; assign after check for bomb
						operation = 1
						capture( player, to_x, to_y )
						chan = PlaySound(movesnd)
						endofturn=True
					ElseIf ((Abs(from_x-to_x) = 2 And Abs(from_y-to_y) = 2) Or (Abs(from_x-to_x)=2 And Abs(from_y-to_y) = 0) Or (Abs(from_x-to_x)=0 And Abs(from_y-to_y) = 2)) Then
						DebugLog "Jumped"
						boardarr(from_x, from_y) = EMPTY
						boardarr(to_x, to_y) = player
						operation = 1
						capture( player, to_x, to_y )
						chan = PlaySound(jumpsnd)
						endofturn=True
					Else
						waiting_for_move_to = False
					EndIf
					
				Else If boardarr(to_x,to_y) = SETBOMB Then

					i = Rand(0,2)
					If i=0 Then
						DebugLog "Bombed"
						explode(to_x,to_y)
					ElseIf i=1 Then
						DebugLog "Tombed"
						tomb(to_x,to_y)
					ElseIf i=2 Then
						DebugLog "Flushed"
						flush(player,to_x, to_y)
					EndIf
					endofturn=True
					operation = 1

				EndIf

			Else	;if waiting_for_move_to = true (FALSE)
				
				;get tile index
				from_x = calc_x( mx )
				from_y = calc_y( my )

				;if clicked on my piece ...
				If boardarr(from_x, from_y) = player Then
					mbut = 0
					waiting_for_move_to = True	;start checking for 2nd click
					PlaySound(clickonsnd)
				Else ;If boardarr(from_x,from_y) = (3-player)
					waiting_for_move_to = False
					chan = PlaySound(invalidsnd)
					;While ChannelPlaying(chan):Wend
				EndIf

			EndIf	;if waiting_for_move_to = true (ENDIF)

		EndIf	;If USER HIT ICONS Or BOARD

		draw_board()
		If waiting_for_move_to = True Then
			put_avail_moves( player, from_x, from_y )
		EndIf
		draw_menu(operation)
		draw_mouse()
		Flip

		If endofturn = True Then
			waiting_for_move_to = False
		EndIf


		If chan<>0 Then
			While ChannelPlaying(chan):Wend
		EndIf

	Wend   ;While( no valid move )

	Return operation

End Function



Function capture( player, to_x, to_y )

	total=0

	; go around destination square capturing pieces (8)

	For i=0 To 7
		nx = to_x + aix(i)
		ny = to_y + aiy(i)
;		DebugLog "nx="+nx+"  to_x="+to_x+"  i="+i+" aix(i),aiy(i)="+aix(i)+","+aiy(i)
		
		If onboard(nx, ny) Then
			If boardarr(nx, ny) = (3-player) Then
				total=total+1

				boardarr(nx,ny) = player
				If player = PLAYERX
					PlaySound(capturesndX)
				Else
					PlaySound(capturesndO)
				EndIf
	
;				put_piece(player, nx, ny)
			EndIf
		EndIf
	Next

End Function



Function put_piece( player, to_x, to_y )
	If player = PLAYERX
		tile = TILE_BLUE
	Else
		tile = TILE_RED
	EndIf

	DrawBlock tiles, TILE_START_X+(to_x*TILE_WIDTH), TILE_START_Y+(to_y*TILE_HEIGHT), tile

End Function



Function put_avail_moves(player, from_x, from_y )

	If player = PLAYERX
		tile = TILE_BLUESEL
	Else
		tile = TILE_REDSEL
	EndIf
	DrawBlock tiles, TILE_START_X+(from_x*TILE_WIDTH), TILE_START_Y+(from_y*TILE_HEIGHT), tile
;	DebugLog "put_avail_moves: "+from_x+":"+from_y

	If show_avail = True Then
		For i=0 To 15
			nx = from_x + aix(i)
			ny = from_y + aiy(i)

			If onboard(nx, ny) Then
				If boardarr(nx,ny)=EMPTY Or boardarr(nx,ny) = SETBOMB Then
					DrawBlock tiles,TILE_START_X+(nx*TILE_WIDTH), TILE_START_Y+(ny*TILE_HEIGHT), TILE_MOVE
				EndIf
			EndIf
		Next
	EndIf

End Function



Function onboard(x,y)

	onboard = False
	If(x>=0 And x< 8 And y>=0 And y<8) Then
		onboard = True
	EndIf

	Return onboard	

End Function


Function calc_x_rev( to_x )
	x = TILE_START_X + (to_x * TILE_WIDTH)
	Return x
End Function

Function calc_y_rev( to_y )
	y = TILE_START_Y + (to_y * TILE_HEIGHT)
	Return y
End Function

Function calc_x( xx )
	;r-1 so that we return a zero based value which can be used directly with boardarr()
	For r=1 To 8
		If xx < (TILE_START_X+(r*TILE_WIDTH))
			Return r-1
		EndIf
	Next
	
	Return r 
End Function


Function calc_y( yy )
	;d-1 so that we return a zero based value which can be used directly with boardarr()
	For d=1 To 8
		If yy < (TILE_START_Y+(d*TILE_HEIGHT))
			Return d-1
		EndIf
	Next

	Return d
End Function


Function aiplay(player)

	topscore = -1
	capscore = 0
	bestmove_from_x = 0
	bestmove_from_y = 0
	bestmove_to_x = 0
	bestmove_to_y = 0

;find our piece, try all moves pick highest score
;loop until we run out of pieces
;apply move


	;go through all our pieces on the board
	For x=0 To 7
    	For y=0 To 7
			;if our piece?
			If boardarr(x,y) = player Then
					
				;loop through all positions
				For d=0 To 15
					newx = x + aix(d)
					newy = y + aiy(d)

					;make sure its on board
					If onboard(newx,newy) Then
						;if we can move there?
						If boardarr(newx,newy) = EMPTY Or boardarr(newx,newy) = SETBOMB Then
							If d<8 Then	;this means we are cloning...score+1
								capscore = capscore + 1
							EndIf
							;capture around us
							capscore = 0
							For i=0 To 7
								nx = newx + aix(i)
								ny = newy + aiy(i)
;		DebugLog "nx="+nx+"  to_x="+to_x+"  i="+i+" aix(i),aiy(i)="+aix(i)+","+aiy(i)

								;on board still?
								If onboard(nx, ny) Then
									;capture enemy piece
									If boardarr(nx, ny) = (3-player) Then
										;we captured a piece, increment score
										capscore = capscore + 1
									EndIf
								EndIf
							Next
							If capscore>topscore Then
								topscore = capscore
								bestmove_from_x = x
								bestmove_from_y = y
								bestmove_to_x = newx
								bestmove_to_y = newy
							EndIf
						EndIf
					EndIf
				Next
			EndIf
		Next
	Next

End Function


Function draw_mouse()

	mx = MouseX()
	my = MouseY()

	DrawImage mouse,mx,my

End Function




Function draw_menu(op)

	If menubtnimg = 0 Then
		menubtnimg = LoadAnimImage("graphics\menu_game_btn.png",174,31,0,2)
	EndIf
	f=0
	If op = 2 Then f=1
	DrawBlock menubtnimg,448,247,f	;standard state of menu button

End Function

Function board_addkillers()

	bombs_remain = 0
	If killers = True
		j = Rand(1,MAXBOMBS)
		For i=0 To j
			bombset = False
			While bombset = False
				x=Rand(0,7)
				y=Rand(0,7)
				If boardarr(x,y) = EMPTY Then
					boardarr(x,y) = SETBOMB
					bombset = True
				EndIf
			Wend
		Next
		bombs_remain = j
		DebugLog "bombs remaining: "+Str(bombs_remain)
	EndIf

End Function

Function tomb(to_x,to_y)

	x = calc_x_rev(to_x)
	y = calc_y_rev(to_y)

	For f=0 To 2
		draw_board()
		DrawImage tombstoneimg,x,y,f
		draw_menu(0)
		draw_mouse()
		Flip
		If f<2 Then 
			chan=PlaySound(shotsnd1)
		Else
			chan=PlaySound(shotsnd2)
		EndIf

		Delay(200)
		While ChannelPlaying(chan):Wend
	Next

	For f=3 To 10
		draw_board()
		DrawImage tombstoneimg,x,y,f
		draw_menu(0)
		draw_mouse()
		Flip
		Delay (100)
	Next

	boardarr(to_x, to_y) = TOMBED
	bombs_remain = bombs_remain - 1

End Function


Function explode(to_x,to_y)

	chan = PlaySound(explodesnd)

	x = calc_x_rev(to_x)
	y = calc_y_rev(to_y)

	For f=0 To 9
		draw_board()
		DrawImage explodeimg,x,y,f
		draw_menu(0)
		draw_mouse()
		Flip
		Delay (100)
	Next

	boardarr(to_x, to_y) = BOMBED
	bombs_remain = bombs_remain - 1

End Function


Function flush(player,to_x,to_y)

	chan = PlaySound(flushsnd)

	x = calc_x_rev(to_x)
	y = calc_y_rev(to_y)

	fstart=0
	If player = PLAYERX Then fstart=8

	;do spin 5x
	For i=1 To 4
		For f=fstart To fstart+3
			draw_board()
			DrawImage flushimg,x,y,f
			draw_menu(0)
			draw_mouse()
			Flip
			Delay (100)
		Next
	Next

	For f=fstart+3 To fstart+7
		draw_board()
		DrawImage flushimg,x,y,f
		draw_menu(0)
		draw_mouse()
		Flip
		Delay(100)
	Next

	boardarr(to_x, to_y) = FLUSHED
	bombs_remain = bombs_remain - 1

End Function


.squaredata
Data 1, 1,-1,-1, 0, 1, 0,-1, 0, 2, 2, 2, 0,-2,-2,-2
Data 1,-1,-1, 1, 1, 0,-1, 0, 2, 2, 0,-2,-2,-2, 0, 2